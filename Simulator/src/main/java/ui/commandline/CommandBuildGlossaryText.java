/**
 * Copyright 2022 Alexander Herzog
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ui.commandline;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import language.Language;
import mathtools.Table;
import systemtools.commandline.AbstractCommand;
import systemtools.commandline.BaseCommandLineSystem;
import ui.help.Help;

/**
 * Dieser Befehl erstellt ein LaTeX-Glossar aus einer html-Datei
 * @author Alexander Herzog
 * @see AbstractCommand
 * @see CommandLineSystem
 */
public class CommandBuildGlossaryText extends AbstractCommand {
	/** Sprache */
	private String language;
	/** Pfad für die Ausgabedateien */
	private String path;

	/**
	 * Konstruktor der Klasse
	 */
	public CommandBuildGlossaryText() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	@Override
	public String[] getKeys() {
		List<String> list=new ArrayList<>();
		list.addAll(Arrays.asList(Language.trAll("CommandLine.BuildGlossary.Name")));
		for (String s: Language.trOther("CommandLine.BuildCatalogDescriptions.Name")) if (!list.contains(s)) list.add(s);
		return list.toArray(new String[0]);

	}

	@Override
	public String getShortDescription() {
		return Language.tr("CommandLine.BuildGlossary.Description.Short");
	}

	@Override
	public String[] getLongDescription() {
		return Language.tr("CommandLine.BuildGlossary.Description.Long").split("\n");
	}

	@Override
	public String prepare(String[] additionalArguments, InputStream in, PrintStream out) {
		final String error=parameterCountCheck(2,2,additionalArguments);
		if (error!=null) return error;

		language=additionalArguments[0];
		path=additionalArguments[1];

		return null;
	}

	@Override
	public void run(AbstractCommand[] allCommands, InputStream in, PrintStream out) {
		if (!Language.isSupportedLanguage(language)) {
			out.println(String.format(Language.tr("CommandLine.BuildGlossary.Error.Language"),language));
			return;
		}
		language=language.toLowerCase();

		final File outputPath=new File(path);
		if (!outputPath.isDirectory()) {
			out.println(String.format(Language.tr("CommandLine.BuildGlossary.Error.Path"),path));
			return;
		}

		final URL url=Help.class.getResource("pages_"+language+"/Glossary.html");

		final List<String> input=loadFile(url);
		if (input==null) {
			out.print(BaseCommandLineSystem.errorBig+": "+String.format(Language.tr("CommandLine.Error.LoadingFile"),url.toString()));
			return;
		}

		final List<String> lines=getProcessedGlossary(input);

		if (!Table.saveTextToFile(String.join("\n",lines),new File(outputPath,"Glossary-"+language+".tex"))) {
			out.println(BaseCommandLineSystem.errorBig+": "+Language.tr("CommandLine.Error.UnableToSaveLaTeXCode"));
		}
	}

	/**
	 * Lädt den Inhalt einer durch eine URL spezifizierten Textdatei.
	 * @param url	URL der zu ladenden Textdatei
	 * @return	Liefert im Erfolgsfall die Zeilen der Datei, sonst <code>null</code>
	 */
	private List<String> loadFile(final URL url) {
		final List<String> lines=new ArrayList<>();
		try(BufferedReader br=new BufferedReader(new InputStreamReader(url.openStream(),StandardCharsets.UTF_8))) {
			String line=null;
			while ((line=br.readLine())!=null) lines.add(line);
		} catch (IOException e) {
			return null;
		}
		return lines;
	}

	/**
	 * Ersetzt in einer Zeile die html-Entities durch LaTeX-Symbole
	 * @param line	Zeile in der die Veränderungen vorgenommen werden sollen
	 * @return	Veränderte Zeile
	 */
	private String replaceHTMLEntities(String line) {
		line=line.replace("&lambda;","$\\lambda$");
		line=line.replace("&mu;","$\\mu$");
		line=line.replace("&nu;","$\\nu$");
		line=line.replace("&rho;","$\\rho$");

		final int index1=line.indexOf("<b>");
		final int index2=line.indexOf("</b>");
		if (index1>=0 && index2>=0) {
			line=line.substring(0,index1)+"\\textbf{"+line.substring(index1+3,index2)+"}"+line.substring(index2+4);
		}

		return line;
	}

	/**
	 * Verarbeitet die Zeilen innerhalb eines Absatzes.
	 * @param results	Ausgabe-Zeilenliste
	 * @param paragraph	Zu verarbeitende Zeilen
	 */
	private void processParagraph(final List<String> results, final List<String> paragraph) {
		if (paragraph==null) return;

		results.add("\n");

		for (String line: paragraph) {
			String s=line;
			boolean ok=false;
			while (!ok) {
				ok=true;
				if (s.contains("<a")) {
					final int index1=s.indexOf("<a");
					final int index2=s.indexOf(">",index1);
					s=s.substring(0,index1)+s.substring(index2+1);
					ok=false;
					continue;
				}
				if (s.contains("</a")) {
					final int index1=s.indexOf("</a");
					final int index2=s.indexOf(">",index1);
					s=s.substring(0,index1)+s.substring(index2+1);
					ok=false;
					continue;
				}
			}
			s=replaceHTMLEntities(s).trim();
			if (!s.isEmpty()) results.add(s);
		}
	}

	/**
	 * Übersetzt ein html-basiertes Glossar in ein LaTeX-basiertes Dokument.
	 * @param input	Eingabezeilen im html-Format
	 * @return	Ausgabezeile in LaTeX-Code
	 */
	private List<String> getProcessedGlossary(final List<String> input) {
		final List<String> results=new ArrayList<>();

		List<String> paragraph=null;
		for (String line: input) {
			if (paragraph!=null) {
				/* Zeile in Absatz */
				if (line.contains("</p>")) {
					final int index1=line.indexOf("</p>");
					paragraph.add(line.substring(0,index1));
					processParagraph(results,paragraph);
					paragraph=null;
				} else {
					paragraph.add(line);
				}
			} else {
				if (line.contains("<h2>")) {
					/* Überschrift */
					final int index1=line.indexOf("<h2>");
					final int index2=line.indexOf("</h2>");
					results.add("\n\\section*{"+line.substring(index1+4,index2)+"}");
				} else {
					/* Absatz */
					if (line.contains("<p>")) {
						final int index1=line.indexOf("<p>");
						paragraph=new ArrayList<>();
						final String s=line.substring(index1+3);
						if (s.contains("</p>")) {
							final int index2=s.indexOf("</p>");
							paragraph.add(s.substring(0,index2));
							processParagraph(results,paragraph);
							paragraph=null;
						} else {
							paragraph.add(s);
						}
					}
				}
			}
		}
		processParagraph(results,paragraph);

		return results;
	}
}

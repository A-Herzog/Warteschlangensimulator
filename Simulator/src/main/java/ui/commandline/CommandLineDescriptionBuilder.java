/**
 * Copyright 2020 Alexander Herzog
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

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import language.Language;
import tools.SetupData;

/**
 * Erstellt basierend auf den Beschreibungen der einzelnen Befehle eine LaTeX-Dokumentation.
 * @author Alexander Herzog
 * @see CommandLineSystem
 * @see CommandLineDescriptionBuilder#buildAll(PrintStream)
 */
public class CommandLineDescriptionBuilder {
	/**
	 * Konstruktor der Klasse
	 */
	public CommandLineDescriptionBuilder() {
	}

	/**
	 * Ersetzt normale Anführungszeichen durch LaTeX-konforme Anführungszeichen
	 * @param text	Zu bearbeitender Text
	 * @return	Text mit umgewandelten Anführungszeichen
	 * @see #run(File)
	 */
	private String replaceQuotationMarks(final String text) {
		final StringBuilder newText=new StringBuilder();
		for (int i=0;i<text.length();i++) {
			final char c=text.charAt(i);
			if (c=='"') {
				if (i==text.length()-1 || text.charAt(i+1)==' ') newText.append("\"'"); else newText.append("\"`");
			} else {
				newText.append(c);
			}
		}

		return newText.toString();
	}

	/**
	 * Startet die Verarbeitung.
	 * @param output	tex-Ausgabedatei.
	 * @return	Im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung.
	 */
	private String run(final File output) {
		final StringBuilder text=new StringBuilder();

		new CommandLineSystem().getCommands().stream().filter(command->!command.isHidden()).sorted((cmd1,cmd2)->cmd1.getName().compareTo(cmd2.getName())).forEach(command->{
			text.append("\\section{\\texttt{"+command.getName()+"}}\n\n");
			text.append("\\textbf{"+replaceQuotationMarks(command.getShortDescription())+"}\n\n");
			final String[] lines=command.getLongDescription();
			for (int i=0;i<lines.length;i++) {
				text.append(replaceQuotationMarks(lines[i]));
				if (i<lines.length-1) text.append("\\\\\n"); else text.append("\n\n");
			}
		});

		String textUTF8=text.toString();
		textUTF8=new String(textUTF8.getBytes(StandardCharsets.UTF_8));
		try {
			Files.write(Paths.get(output.toURI()),textUTF8.getBytes(),StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			return "Error writing "+output.toString();
		}

		return null;
	}

	/**
	 * Erstellt basierend auf den Sprachdaten eine LaTeX-Dokumentation
	 * für eine bestimmte Sprache.
	 * @param language	Sprache
	 * @param folder	Ausgabeordner
	 * @param out	Ausgabestream für Meldungen (darf nicht <code>null</code> sein)
	 */
	public static void buildDescription(final String language, final String folder, final PrintStream out) {
		if (!new File(folder).isDirectory()) {
			if (!new File(folder).mkdirs()) {
				out.println("error mkdir "+folder);
				return;
			}
		}

		final SetupData setup=SetupData.getSetup();
		final String saveLanguage=setup.language;
		setup.setLanguage(language);

		try {
			final CommandLineDescriptionBuilder builder=new CommandLineDescriptionBuilder();
			String result=builder.run(new File(folder,"Reference.tex"));
			if (result==null) result="ok";
			out.println(language+": "+result);
		} finally {
			setup.setLanguage(saveLanguage);
		}
	}

	/**
	 * Erstellt basierend auf den Sprachdaten eine LaTeX-Dokumentation
	 * auf Deutsch und auf Englisch in den Unterordnern "ReferenzDE" und "ReferenzEN" des Desktop-Ordners.
	 * @param out	Ausgabestream für Meldungen (darf nicht <code>null</code> sein)
	 */
	public static void buildAll(final PrintStream out) {
		out.println("building...");
		buildDescription("de",System.getProperty("user.home")+"\\Desktop\\ReferenzDE",out);
		buildDescription("en",System.getProperty("user.home")+"\\Desktop\\ReferenzEN",out);
		out.println("done.");
	}

	/**
	 * Eigene main-Routine für den Aufruf dieser Klasse über einen Ant-Task.
	 * @param args	Es werden zwei Parameter erwartet: Die Sprach-ID ("de" oder "en") und der Pfad für die Ausgabe.
	 */
	public static void main(String[] args) {
		if (args.length!=2) {
			System.out.println("Needing 2 parameters.");
			return;
		}
		if (!Language.isSupportedLanguage(args[0])) {
			System.out.println("Language "+args[0]+" not supported");
			return;
		}
		if (!new File(args[1]).isDirectory()) {
			System.out.println(args[1]+" is no directory");
			return;
		}

		buildDescription(args[0].toLowerCase(),args[1],System.out);
	}
}

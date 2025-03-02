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
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import language.Language;
import systemtools.commandline.AbstractCommand;
import systemtools.commandline.BaseCommandLineSystem;
import ui.MainFrame;
import ui.modeleditor.ModelElementCatalogDescriptionBuilder;

/**
 * Dieser Befehl erstellt die LaTeX-Dokumentation zu den verfügbaren Stationen
 * @author Alexander Herzog
 * @see ModelElementCatalogDescriptionBuilder
 * @see AbstractCommand
 * @see CommandLineSystem
 */
public class CommandBuildCatalogDescriptions extends AbstractCommand {
	/** Ausgabeformat */
	private enum Mode {
		/** Ausgabeformat: LaTeX */
		LATEX,
		/** Ausgabeformat: HTML */
		HTML
	}

	/**
	 * Konstruktor der Klasse
	 * @param system	Referenz auf das Kommandozeilensystem
	 */
	public CommandBuildCatalogDescriptions(final BaseCommandLineSystem system) {
		super(system);
	}

	/** Sprache */
	private String language;
	/** Pfad für die Ausgabedateien */
	private String path;
	/** Ausgabeformat */
	private Mode mode=Mode.LATEX;

	@Override
	public String[] getKeys() {
		List<String> list=new ArrayList<>();
		list.addAll(Arrays.asList(Language.trAll("CommandLine.BuildCatalogDescriptions.Name")));
		for (String s: Language.trOther("CommandLine.BuildCatalogDescriptions.Name")) if (!list.contains(s)) list.add(s);
		return list.toArray(String[]::new);
	}

	@Override
	public String getShortDescription() {
		return Language.tr("CommandLine.BuildCatalogDescriptions.Description.Short");
	}

	@Override
	public String[] getLongDescription() {
		return Language.tr("CommandLine.BuildCatalogDescriptions.Description.Long").split("\n");
	}

	@Override
	public String prepare(String[] additionalArguments, InputStream in, PrintStream out) {
		final String error=parameterCountCheck(2,3,additionalArguments);
		if (error!=null) return error;

		language=additionalArguments[0];
		path=additionalArguments[1];

		if (additionalArguments.length==3) {
			if (additionalArguments[2].equalsIgnoreCase("html")) mode=Mode.HTML;
		}

		return null;
	}

	@Override
	public void run(final AbstractCommand[] allCommands, final InputStream in, final PrintStream out) {
		if (!Language.isSupportedLanguage(language)) {
			out.println(String.format(Language.tr("CommandLine.BuildCatalogDescriptions.Error.Language"),language));
			return;
		}
		language=language.toLowerCase();

		if (!new File(path).isDirectory()) {
			out.println(String.format(Language.tr("CommandLine.BuildCatalogDescriptions.Error.Path"),path));
			return;
		}

		switch (mode) {
		case LATEX:
			ModelElementCatalogDescriptionBuilder.buildLanguageLaTeX(language,path,out);
			break;
		case HTML:
			String title="Station reference";
			if (language.equals("de")) title="Referenz der Stationen";
			ModelElementCatalogDescriptionBuilder.buildLanguageHTML(language,title,new File(path,MainFrame.PROGRAM_NAME+"-Reference-"+language+".html"),out);
			break;
		}
	}

	@Override
	public boolean isHidden() {
		return true;
	}

}

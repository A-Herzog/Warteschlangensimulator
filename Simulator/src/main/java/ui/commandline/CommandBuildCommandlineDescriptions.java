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

/**
 * Dieser Befehl erstellt die LaTeX-Dokumentation zu Kommandozeilenbefehlen
 * @author Alexander Herzog
 * @see CommandLineDescriptionBuilder
 * @see AbstractCommand
 * @see CommandLineSystem
 */
public class CommandBuildCommandlineDescriptions extends AbstractCommand {
	/** Sprache */
	private String language;
	/** Pfad für die Ausgabedateien */
	private String path;

	/**
	 * Konstruktor der Klasse
	 * @param system	Referenz auf das Kommandozeilensystem
	 */
	public CommandBuildCommandlineDescriptions(final BaseCommandLineSystem system) {
		super(system);
	}

	@Override
	public String[] getKeys() {
		List<String> list=new ArrayList<>();
		list.addAll(Arrays.asList(Language.trAll("CommandLine.BuildCommandlineDescriptions.Name")));
		for (String s: Language.trOther("CommandLine.BuildCommandlineDescriptions.Name")) if (!list.contains(s)) list.add(s);
		return list.toArray(new String[0]);
	}

	@Override
	public String getShortDescription() {
		return Language.tr("CommandLine.BuildCommandlineDescriptions.Description.Short");
	}

	@Override
	public String[] getLongDescription() {
		return Language.tr("CommandLine.BuildCommandlineDescriptions.Description.Long").split("\n");
	}

	@Override
	public String prepare(String[] additionalArguments, InputStream in, PrintStream out) {
		final String error=parameterCountCheck(2,additionalArguments);
		if (error!=null) return error;

		language=additionalArguments[0];
		path=additionalArguments[1];

		return null;
	}

	@Override
	public void run(final AbstractCommand[] allCommands, final InputStream in, final PrintStream out) {
		if (!Language.isSupportedLanguage(language)) {
			out.println(String.format(Language.tr("CommandLine.BuildCommandlineDescriptions.Error.Language"),language));
			return;
		}
		if (!new File(path).isDirectory()) {
			out.println(String.format(Language.tr("CommandLine.BuildCommandlineDescriptions.Error.Path"),path));
			return;
		}

		CommandLineDescriptionBuilder.buildDescription(language.toLowerCase(),path,out);
	}

	@Override
	public boolean isHidden() {
		return true;
	}

}

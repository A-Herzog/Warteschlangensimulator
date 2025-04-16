/**
 * Copyright 2025 Alexander Herzog
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

import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import language.Language;
import systemtools.commandline.AbstractCommand;
import systemtools.commandline.BaseCommandLineSystem;
import tools.SetupData;

/**
 * Ändert die Programmsprache.
 * @author Alexander Herzog
 * @see AbstractCommand
 * @see CommandLineSystem
 */
public class CommandSetLanguage extends AbstractCommand {
	/**
	 * Kürzel für die neue Sprache
	 */
	private String newLanguage=null;

	/**
	 * Konstruktor der Klasse
	 * @param system	Referenz auf das Kommandozeilensystem
	 */
	public CommandSetLanguage(final BaseCommandLineSystem system) {
		super(system);
	}

	@Override
	public String[] getKeys() {
		final List<String> list=new ArrayList<>();
		list.addAll(Arrays.asList(Language.trAll("CommandLine.SetLanguage.Name")));
		for (String s: Language.trOther("CommandLine.SetLanguage.Name")) if (!list.contains(s)) list.add(s);
		return list.toArray(String[]::new);
	}

	@Override
	public String getShortDescription() {
		return Language.tr("CommandLine.SetLanguage.Description.Short");
	}

	@Override
	public String[] getLongDescription() {
		return Language.tr("CommandLine.SetLanguage.Description.Long").split("\n");
	}

	@Override
	public String prepare(String[] additionalArguments, InputStream in, PrintStream out) {
		final String s=parameterCountCheck(1,additionalArguments); if (s!=null) return s;

		for (var language: Language.getLanguages()) if (language.equalsIgnoreCase(additionalArguments[0])) {
			newLanguage=language;
			break;
		}

		if (newLanguage==null) {
			out.println(String.format(Language.tr("CommandLine.SetLanguage.Error.UnknownLanguage"),additionalArguments[0]));
			out.println(Language.tr("CommandLine.SetLanguage.Error.AvailableLanguages"));
			Stream.of(Language.getLanguages()).forEach(out::println);
		}

		return null;
	}

	@Override
	public void run(AbstractCommand[] allCommands, InputStream in, PrintStream out) {
		if (newLanguage!=null) {
			final SetupData setup=SetupData.getSetup();
			out.println(String.format(Language.tr("CommandLine.SetLanguage.Changed"),setup.language,newLanguage));
			setup.language=newLanguage;
			if (!setup.saveSetup()) {
				style.setErrorStyle();
				out.println(BaseCommandLineSystem.errorBig+": "+Language.tr("CommandLine.SetLanguage.Error.CannotSaveSetup"));
				style.setNormalStyle();
			}
		}
	}
}

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
 * Stellt den zu verwendenden Dezimaltrenner ein.
 * @author Alexander Herzog
 * @see AbstractCommand
 * @see CommandLineSystem
 */
public class CommandSetNumberFormat extends AbstractCommand {
	/**
	 * Modus nach dem der Dezimaltrenner bestimmt werden soll
	 */
	private SetupData.NumberFormat newSeparatorMode=null;

	/**
	 * Konstruktor der Klasse
	 * @param system	Referenz auf das Kommandozeilensystem
	 */
	public CommandSetNumberFormat(final BaseCommandLineSystem system) {
		super(system);
	}

	@Override
	public String[] getKeys() {
		final List<String> list=new ArrayList<>();
		list.addAll(Arrays.asList(Language.trAll("CommandLine.SetNumberFormat.Name")));
		for (String s: Language.trOther("CommandLine.SetNumberFormat.Name")) if (!list.contains(s)) list.add(s);
		return list.toArray(String[]::new);
	}

	@Override
	public String getShortDescription() {
		return Language.tr("CommandLine.SetNumberFormat.Description.Short");
	}

	@Override
	public String[] getLongDescription() {
		return Language.tr("CommandLine.SetNumberFormat.Description.Long").split("\n");
	}

	@Override
	public String prepare(String[] additionalArguments, InputStream in, PrintStream out) {
		final String s=parameterCountCheck(1,additionalArguments); if (s!=null) return s;

		for (var mode: SetupData.NumberFormat.values()) if (mode.name.equalsIgnoreCase(additionalArguments[0])) {
			newSeparatorMode=mode;
			break;
		}

		if (newSeparatorMode==null) {
			out.println(String.format(Language.tr("CommandLine.SetNumberFormat.Error.UnknownMode"),additionalArguments[0]));
			out.println(Language.tr("CommandLine.SetNumberFormat.Error.AvailableModes"));
			Stream.of(SetupData.NumberFormat.values()).map(mode->mode.name).forEach(out::println);
		}

		return null;
	}

	@Override
	public void run(AbstractCommand[] allCommands, InputStream in, PrintStream out) {
		if (newSeparatorMode!=null) {
			final SetupData setup=SetupData.getSetup();
			out.println(String.format(Language.tr("CommandLine.SetNumberFormat.Changed"),setup.numberFormat.name,newSeparatorMode.name));
			setup.numberFormat=newSeparatorMode;
			if (!setup.saveSetup()) {
				style.setErrorStyle();
				out.println(BaseCommandLineSystem.errorBig+": "+Language.tr("CommandLine.SetNumberFormat.Error.CannotSaveSetup"));
				style.setNormalStyle();
			}
		}
	}
}

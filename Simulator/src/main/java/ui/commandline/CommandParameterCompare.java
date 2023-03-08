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
import java.util.List;

import language.Language;
import systemtools.commandline.AbstractCommand;
import systemtools.commandline.BaseCommandLineSystem;
import ui.parameterseries.ParameterCompareRunner;
import ui.parameterseries.ParameterCompareSetup;

/**
 * Führt eine Parameterreihen-Simulation aus.
 * @author Alexander Herzog
 * @see AbstractCommand
 * @see CommandLineSystem
 */
public class CommandParameterCompare extends AbstractCommand {
	/** Eingabedatei für das Parameterreihen-Setup */
	private File inFile;
	/** Ausgabedatei für das Parameterreihen-Setup (inkl. Ergebnissen) */
	private File outFile;
	/** Tatsächliches inneres System zur Ausführung der Parameterreihensimulation */
	private ParameterCompareRunner runner;

	/**
	 * Konstruktor der Klasse
	 * @param system	Referenz auf das Kommandozeilensystem
	 */
	public CommandParameterCompare(final BaseCommandLineSystem system) {
		super(system);
	}

	@Override
	public String[] getKeys() {
		final List<String> list=new ArrayList<>();
		list.add(Language.tr("CommandLine.ParameterCompare.Name"));
		for (String s: Language.trOther("CommandLine.ParameterCompare.Name")) if (!list.contains(s)) list.add(s);
		return list.toArray(new String[0]);
	}

	@Override
	public String getShortDescription() {
		return Language.tr("CommandLine.ParameterCompare.Description.Short");
	}

	@Override
	public String[] getLongDescription() {
		return Language.tr("CommandLine.ParameterCompare.Description.Long").split("\n");
	}

	@Override
	public String prepare(String[] additionalArguments, InputStream in, PrintStream out) {
		final String s=parameterCountCheck(2,additionalArguments); if (s!=null) return s;

		inFile=new File(additionalArguments[0]);
		outFile=new File(additionalArguments[1]);

		if (!inFile.isFile()) return String.format(Language.tr("CommandLine.Error.File.InputDoesNotExist"),inFile.toString());
		if (outFile.exists()) return String.format(Language.tr("CommandLine.Error.File.OutputAlreadyExist"),outFile.toString());

		return null;
	}

	@Override
	public void run(AbstractCommand[] allCommands, InputStream in, PrintStream out) {
		String error;
		final ParameterCompareSetup setup=new ParameterCompareSetup(null);

		error=setup.loadFromFile(inFile);
		if (error!=null) {
			style.setErrorStyle();
			out.println(BaseCommandLineSystem.errorBig+": "+error);
			style.setNormalStyle();
			return;
		}

		runner=new ParameterCompareRunner(null,null,log->out.println(log));
		error=runner.check(setup,inFile.getParent());
		if (error!=null) {
			style.setErrorStyle();
			out.println(BaseCommandLineSystem.errorBig+": "+error);
			style.setNormalStyle();
			return;
		}

		runner.start();
		runner.waitForFinish();

		if (!setup.saveToFile(outFile)) {
			style.setErrorStyle();
			out.println(BaseCommandLineSystem.errorBig+": "+Language.tr("CommandLine.Error.UnableToSaveParameterSeriesResults"));
			style.setNormalStyle();
		}

		runner=null;
	}

	@Override
	public void setQuit() {
		if (runner!=null) runner.cancel();
	}
}

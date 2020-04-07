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
import mathtools.Table;
import systemtools.commandline.AbstractCommand;
import systemtools.commandline.BaseCommandLineSystem;
import ui.parameterseries.ParameterCompareSetup;

/**
 * Extrahiert eine Ergebnistabelle aus der Ergebnis-xml-Datei einer Parameterreihen-Simulation.
 * @author Alexander Herzog
 * @see AbstractCommand
 * @see CommandLineSystem
 */
public class CommandParameterCompareTable extends AbstractCommand {
	private File inFile;
	private File outFile;

	@Override
	public String[] getKeys() {
		final List<String> list=new ArrayList<String>();
		list.add(Language.tr("CommandLine.ParameterCompareTable.Name"));
		for (String s: Language.trOther("CommandLine.ParameterCompareTable.Name")) if (!list.contains(s)) list.add(s);
		return list.toArray(new String[0]);
	}

	@Override
	public String getShortDescription() {
		return Language.tr("CommandLine.ParameterCompareTable.Description.Short");
	}

	@Override
	public String[] getLongDescription() {
		return Language.tr("CommandLine.ParameterCompareTable.Description.Long").split("\n");
	}

	@Override
	public String prepare(final String[] additionalArguments, final InputStream in, final PrintStream out) {
		final String s=parameterCountCheck(2,additionalArguments); if (s!=null) return s;

		inFile=new File(additionalArguments[0]);
		outFile=new File(additionalArguments[1]);

		if (!inFile.isFile()) return String.format(Language.tr("CommandLine.Error.File.InputDoesNotExist"),inFile.toString());
		if (outFile.exists()) return String.format(Language.tr("CommandLine.Error.File.OutputAlreadyExist"),outFile.toString());

		return null;
	}

	@Override
	public void run(final AbstractCommand[] allCommands, final InputStream in, final PrintStream out) {
		final ParameterCompareSetup setup=new ParameterCompareSetup(null);
		final String error=setup.loadFromFile(inFile);
		if (error!=null) {
			out.println(BaseCommandLineSystem.errorBig+": "+error);
			return;
		}

		final Table table=setup.getTableData(true,false);
		if (table.save(outFile)) {
			out.println(String.format(Language.tr("CommandLine.ParameterCompareTable.SaveOk"),outFile.toString()));
		} else {
			out.println(BaseCommandLineSystem.errorBig+": "+String.format(Language.tr("ParameterCompare.Toolbar.ProcessResults.ResultsTable.Save.ErrorInfo"),outFile.toString()));
		}
	}
}

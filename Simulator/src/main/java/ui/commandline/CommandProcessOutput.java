/**
 * Copyright 2021 Alexander Herzog
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
import simulator.elements.RunElementDisposeWithTable;
import systemtools.commandline.AbstractCommand;
import systemtools.commandline.BaseCommandLineSystem;
import ui.inputprocessor.ClientOutputTableDialog;
import ui.modeleditor.elements.ModelElementDisposeWithTable;

/**
 * Erlaubt es Tabellen, die an Speichern+Ausgang-Stationen entstanden sind,
 * in normale Tabellen mit Spaltenüberschriften umzuwandeln.
 * @author Alexander Herzog
 * @see ModelElementDisposeWithTable
 * @see RunElementDisposeWithTable
 * @see AbstractCommand
 * @see CommandLineSystem
 */
public class CommandProcessOutput extends AbstractCommand {
	/** Tabelleneingabedatei */
	private File inputFile;
	/** Tabellenausgabedatei */
	private File outputFile;

	@Override
	public String[] getKeys() {
		List<String> list=new ArrayList<>();
		list.add(Language.tr("CommandLine.ProcessOutput.Name"));
		for (String s: Language.trOther("CommandLine.ProcessOutput.Name")) if (!list.contains(s)) list.add(s);
		return list.toArray(new String[0]);
	}

	@Override
	public String getShortDescription() {
		return Language.tr("CommandLine.ProcessOutput.Description.Short");
	}

	@Override
	public String[] getLongDescription() {
		return Language.tr("CommandLine.ProcessOutput.Description.Long").split("\n");
	}

	@Override
	public String prepare(String[] additionalArguments, InputStream in, PrintStream out) {
		String s=parameterCountCheck(2,additionalArguments); if (s!=null) return s;

		inputFile=new File(additionalArguments[0]);
		outputFile=new File(additionalArguments[1]);
		if (!inputFile.isFile()) return String.format(Language.tr("CommandLine.Error.File.InputDoesNotExist"),inputFile.toString());
		if (outputFile.exists()) return String.format(Language.tr("CommandLine.Error.File.OutputAlreadyExist"),outputFile.toString());

		return null;
	}

	@Override
	public void run(AbstractCommand[] allCommands, InputStream in, PrintStream out) {
		final Table inputTable=new Table();
		if (!inputTable.load(inputFile)) {
			out.println(BaseCommandLineSystem.errorBig+": "+Language.tr("CommandLine.ProcessOutput.ErrorLoadingInputTable"));
			return;
		}
		final Table outputTable=new Table();
		ClientOutputTableDialog.process(inputTable,outputTable);
		if (!outputTable.save(outputFile)) {
			out.println(BaseCommandLineSystem.errorBig+": "+Language.tr("CommandLine.ProcessOutput.ErrorSavingOutputTable"));
		}
	}
}

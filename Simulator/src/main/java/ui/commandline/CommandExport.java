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
import simulator.editmodel.EditModel;
import simulator.statistics.Statistics;
import systemtools.commandline.AbstractCommand;
import systemtools.commandline.BaseCommandLineSystem;
import ui.EditorPanel;
import ui.modeleditor.ModelSurfacePanel;

/**
 * Exportiert ein Modell als Bild oder ähnliches aus einer
 * Modell-xml-Datei oder einer Statistikdatei.
 * @author Alexander Herzog
 * @see AbstractCommand
 * @see CommandLineSystem
 */
public class CommandExport extends AbstractCommand {
	/** Modell- oder Statistikdatei */
	private File inputFile;
	/** Ausgabedatei */
	private File outputFile;

	@Override
	public String[] getKeys() {
		List<String> list=new ArrayList<>();
		list.add(Language.tr("CommandLine.Export.Name"));
		for (String s: Language.trOther("CommandLine.Export.Name")) if (!list.contains(s)) list.add(s);
		return list.toArray(new String[0]);
	}

	@Override
	public String getShortDescription() {
		return Language.tr("CommandLine.Export.Description.Short");
	}

	@Override
	public String[] getLongDescription() {
		return Language.tr("CommandLine.Export.Description.Long").split("\n");
	}

	@Override
	public String prepare(String[] additionalArguments, InputStream in, PrintStream out) {
		String s=parameterCountCheck(2,additionalArguments); if (s!=null) return s;

		inputFile=new File(additionalArguments[0]);
		outputFile=new File(additionalArguments[1]);
		if (!inputFile.isFile()) return String.format(Language.tr("CommandLine.Error.File.InputDoesNotExist"),inputFile);
		if (outputFile.exists()) return String.format(Language.tr("CommandLine.Error.File.OutputAlreadyExist"),outputFile);
		return null;
	}

	@Override
	public void run(AbstractCommand[] allCommands, InputStream in, PrintStream out) {
		/* Modell oder Statistik laden */
		EditModel editModel=new EditModel();
		Statistics statistics=null;
		String error=editModel.loadFromFile(inputFile);
		if (error!=null) {
			statistics=new Statistics();
			if (statistics.loadFromFile(inputFile)==null) {
				error=null;
				editModel=statistics.editModel;
			}
		}
		if (error!=null) {
			out.println(BaseCommandLineSystem.errorBig+": "+Language.tr("CommandLine.Error.LoadingModel")+" "+error);
			return;
		}

		/* Modell exportieren */
		final ModelSurfacePanel surfacePanel=new ModelSurfacePanel();
		surfacePanel.setSurface(editModel,editModel.surface,editModel.clientData,editModel.sequences);
		error=EditorPanel.exportModelToFile(editModel,statistics,surfacePanel,outputFile);
		if (error!=null) {
			out.println(BaseCommandLineSystem.errorBig+": "+error);
			return;
		}
	}
}

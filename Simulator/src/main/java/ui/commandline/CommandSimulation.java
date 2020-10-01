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
import simulator.editmodel.EditModel;
import simulator.statistics.Statistics;
import systemtools.commandline.AbstractCommand;
import systemtools.commandline.BaseCommandLineSystem;

/**
 * Führt eine einzelne Simulation aus.
 * @author Alexander Herzog
 * @see AbstractCommand
 * @see AbstractSimulationCommand
 * @see CommandLineSystem
 */
public final class CommandSimulation extends AbstractSimulationCommand {
	/** Zu simulierende Modelldatei */
	private File modelFile;
	/** Statistikausgabedatei */
	private File statisticsFile;

	@Override
	public String[] getKeys() {
		List<String> list=new ArrayList<>();
		list.add(Language.tr("CommandLine.Simulation.Name"));
		for (String s: Language.trOther("CommandLine.Simulation.Name")) if (!list.contains(s)) list.add(s);
		return list.toArray(new String[0]);
	}

	@Override
	public String getShortDescription() {
		return Language.tr("CommandLine.Simulation.Description.Short");
	}

	@Override
	public String[] getLongDescription() {
		return Language.tr("CommandLine.Simulation.Description.Long").split("\n");
	}

	@Override
	public String prepare(String[] additionalArguments, InputStream in, PrintStream out) {
		String s=parameterCountCheck(2,additionalArguments); if (s!=null) return s;
		modelFile=new File(additionalArguments[0]);
		statisticsFile=new File(additionalArguments[1]);
		if (!modelFile.isFile()) return String.format(Language.tr("CommandLine.Error.File.InputDoesNotExist"),modelFile);
		if (!isModelFile(modelFile)) return String.format(Language.tr("CommandLine.Error.File.InputNoValidModelFile"),modelFile);
		if (statisticsFile.exists()) return String.format(Language.tr("CommandLine.Error.File.OutputAlreadyExist"),statisticsFile);
		return null;
	}

	@Override
	public void run(AbstractCommand[] allCommands, InputStream in, PrintStream out) {
		EditModel editModel=new EditModel();
		String s=editModel.loadFromFile(modelFile);
		if (s!=null) {out.println(BaseCommandLineSystem.errorBig+": "+Language.tr("CommandLine.Error.LoadingModel")+" "+s); return;}
		File parentFolder=null;
		if (modelFile!=null) parentFolder=modelFile.getParentFile();
		final EditModel changedEditModel=editModel.modelLoadData.changeModel(editModel,parentFolder);
		if (changedEditModel!=null) {
			outputModelLoadDataWarnings(editModel.modelLoadData.getChangeWarnings(),out);
			editModel=changedEditModel;
		}
		final Statistics statistics=singleSimulation(editModel,false,out);
		if (statistics!=null) saveStatistics(statistics,statisticsFile,out);
	}
}

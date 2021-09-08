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
import mathtools.MultiTable;
import mathtools.NumberTools;
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
public class CommandSimulation extends AbstractSimulationCommand {
	/** Zu simulierende Modelldatei */
	private File modelFile;
	/** Optional zu ladende Tabellendatei */
	private File dataInputFile;
	/** Statistikausgabedatei */
	private File statisticsFile;
	/** Soll die Simulation nach einer vorgegebenen Zeit abgebrochen werden? */
	private final boolean withTimeout;
	/** Abbruchzeit in Sekunden; wird ein negativer Wert übergeben, so gibt es zwar kein Timeout, aber für mit Fehler abgeschlossene Simulationen wird keine Statistik erzeugt */
	private double timeout;

	/**
	 * Konstruktor der Klasse
	 */
	public CommandSimulation() {
		this(false);
	}

	/**
	 * Konstruktor der Klasse
	 * @param withTimeout	 Soll über die Parameter ein Timeout-Wert angenommen werden?
	 */
	protected CommandSimulation(final boolean withTimeout) {
		this.withTimeout=withTimeout;
	}

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
		String s=parameterCountCheck(withTimeout?3:2,withTimeout?4:3,additionalArguments); if (s!=null) return s;

		modelFile=new File(additionalArguments[0]);
		if ((withTimeout && additionalArguments.length==4) || (!withTimeout && additionalArguments.length==3)) {
			dataInputFile=new File(additionalArguments[1]);
			statisticsFile=new File(additionalArguments[2]);
		} else {
			statisticsFile=new File(additionalArguments[1]);
		}
		if (withTimeout) {
			final Double D=NumberTools.getDouble((additionalArguments.length==4)?additionalArguments[3]:additionalArguments[2]);
			if (D==null) return String.format(Language.tr("CommandLine.Error.InvalidTimeout"),additionalArguments[3]);
			timeout=D.doubleValue();
		}

		if (!modelFile.isFile()) return String.format(Language.tr("CommandLine.Error.File.InputDoesNotExist"),modelFile);
		if (!isModelFile(modelFile)) return String.format(Language.tr("CommandLine.Error.File.InputNoValidModelFile"),modelFile);

		if (dataInputFile!=null) {
			if (!dataInputFile.isFile()) return String.format(Language.tr("CommandLine.Error.File.InputDoesNotExist"),dataInputFile);
		}

		if (statisticsFile.exists()) return String.format(Language.tr("CommandLine.Error.File.OutputAlreadyExist"),statisticsFile);
		return null;
	}

	@Override
	public void run(AbstractCommand[] allCommands, InputStream in, PrintStream out) {
		/* Modell laden */
		EditModel editModel=new EditModel();
		final String error=editModel.loadFromFile(modelFile);
		if (error!=null) {
			out.println(BaseCommandLineSystem.errorBig+": "+Language.tr("CommandLine.Error.LoadingModel")+" "+error);
			return;
		}

		/* Externe Daten laden */
		final EditModel changedEditModel;
		if (dataInputFile==null) {
			final File parentFolder=modelFile.getParentFile();
			changedEditModel=editModel.modelLoadData.changeModel(editModel,parentFolder);
		} else {
			final MultiTable table=new MultiTable();
			if (!table.load(dataInputFile)) {
				out.println(BaseCommandLineSystem.errorBig+": "+String.format(Language.tr("CommandLine.Error.LoadingFile"),dataInputFile.toString()));
				return;
			}
			changedEditModel=editModel.modelLoadData.changeModel(editModel,table,dataInputFile.getName(),true);
		}

		/* Wenn externe Daten geladen wurden, ggf. Warnmeldung ausgeben */
		if (changedEditModel!=null) {
			outputModelLoadDataWarnings(editModel.modelLoadData.getChangeWarnings(),out);
			editModel=changedEditModel;
		}

		/* Simulation durchführen, Ergebnisse speichern */
		final Statistics statistics=singleSimulation(editModel,false,Integer.MAX_VALUE,out,withTimeout,timeout);
		if (statistics!=null) saveStatistics(statistics,statisticsFile,out);
	}
}

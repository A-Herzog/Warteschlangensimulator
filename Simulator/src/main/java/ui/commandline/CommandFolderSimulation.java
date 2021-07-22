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

import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import simulator.AnySimulator;
import simulator.editmodel.EditModel;
import simulator.statistics.Statistics;
import systemtools.commandline.AbstractCommand;
import systemtools.commandline.BaseCommandLineSystem;
import ui.parameterseries.ParameterCompareRunner;
import ui.parameterseries.ParameterCompareSetup;
import ui.parameterseries.ParameterCompareSetupModel;
import xml.XMLTools;

/**
 * Simuliert alle Modelle oder Parameterreihen in einem Verzeichnis.
 * @author Alexander Herzog
 * @see AbstractCommand
 * @see CommandLineSystem
 */
public class CommandFolderSimulation extends AbstractCommand {
	/**
	 * Zu verarbeitendes Verzeichnis
	 */
	private File folder;

	/**
	 * Wird von {@link #setQuit()} auf <code>true</code> gesetzt, wenn die Simulation
	 * abgebrochen werden soll.
	 */
	private boolean isQuit;

	/**
	 * Simulator-Objekt über das Einzel-Simulationen durchgeführt werden.
	 */
	private AnySimulator simulator;

	/**
	 * Inneres System zur Ausführung der Parameterreihensimulation
	 */
	private ParameterCompareRunner runner;

	/**
	 * Konstruktor der Klasse
	 */
	public CommandFolderSimulation() {
		super();
	}

	/**
	 * Konstruktor der Klasse
	 * @param folder	Zu verarbeitendes Verzeichnis
	 */
	public CommandFolderSimulation(final File folder) {
		this.folder=folder;
	}

	@Override
	public String[] getKeys() {
		final List<String> list=new ArrayList<>();
		list.add(Language.tr("CommandLine.FolderSimulation.Name"));
		for (String s: Language.trOther("CommandLine.FolderSimulation.Name")) if (!list.contains(s)) list.add(s);
		return list.toArray(new String[0]);
	}

	@Override
	public String getShortDescription() {
		return Language.tr("CommandLine.FolderSimulation.Description.Short");
	}

	@Override
	public String[] getLongDescription() {
		return Language.tr("CommandLine.FolderSimulation.Description.Long").split("\n");
	}

	@Override
	public String prepare(String[] additionalArguments, InputStream in, PrintStream out) {
		final String s=parameterCountCheck(1,additionalArguments); if (s!=null) return s;

		folder=new File(additionalArguments[0]);
		if (!folder.isDirectory()) return String.format(Language.tr("CommandLine.FolderSimulation.ParameterIsNotFolder"),folder.toString());

		return null;
	}

	/**
	 * Ermittelt basierend auf dem Namen der Eingabedatei
	 * einen Namen für eine Ausgabedatei.
	 * @param inputFile	Eingabedatei
	 * @return	Ausgabedatei
	 */
	private File getOutputFile(final File inputFile) {
		final File folder=inputFile.getParentFile();
		String name="Results-"+inputFile.getName();
		final int index=name.lastIndexOf('.');
		if (index<0) {
			name=name+".zip";
		} else {
			name=name.substring(0,index)+".zip";
		}

		return new File(folder,name);
	}

	/**
	 * Simuliert ein einzelnes Modell.
	 * @param file	Eingabedatei
	 * @param model	Zu simulierendes Modell
	 * @param out	Ein {@link PrintStream}-Objekt, über das Texte ausgegeben werden können.
	 * @return	Liefert <code>true</code>, wenn das Modell erfolgreich simuliert und die Ergebnisse gespeichert werden konnten
	 */
	private boolean processModel(final File file, EditModel model, final PrintStream out) {
		/* Externe Daten laden */
		final EditModel changedEditModel=model.modelLoadData.changeModel(model,folder);
		if (changedEditModel!=null) {
			AbstractSimulationCommand.outputModelLoadDataWarnings(model.modelLoadData.getChangeWarnings(),out);
			model=changedEditModel;
		}

		/* Vorbereiten und starten */
		final Object obj=AbstractSimulationCommand.prepare(Integer.MAX_VALUE,model,out);
		if (!(obj instanceof AnySimulator)) return false;
		simulator=(AnySimulator)obj;

		/* Auf Ende der Simulation warten */
		AbstractSimulationCommand.waitForSimulationDone(simulator,false,out);

		/* Nicht versuchen etwas zu speichern, wenn die Simulation abgebrochen wurde */
		if (isQuit) return false;

		/* Statistik zusammenstellen und speichern */
		final Statistics statistics=simulator.getStatistic();
		simulator=null;
		if (statistics==null) {
			out.println(Language.tr("CommandLine.Simulation.NoResults"));
			return false;
		}

		out.println(String.format(Language.tr("CommandLine.Simulation.Done"),NumberTools.formatLong(statistics.simulationData.runTime)));
		if (!statistics.saveToFile(getOutputFile(file))) {
			out.println(BaseCommandLineSystem.errorBig+": "+Language.tr("CommandLine.Error.UnableToSaveStatistic"));
			return false;
		}

		return true;
	}

	/**
	 * Simuliert eine Parameterreihe.
	 * @param file	Eingabedatei
	 * @param setup	Zu simulierende Parameterreihe
	 * @param out	Ein {@link PrintStream}-Objekt, über das Texte ausgegeben werden können.
	 * @return	Liefert <code>true</code>, wenn die Parameterreihe erfolgreich simuliert und die Ergebnisse gespeichert werden konnten
	 */
	private boolean processParameterSeries(final File file, final ParameterCompareSetup setup, final PrintStream out) {
		/* Vorbereiten */
		boolean allDone=true;
		for (ParameterCompareSetupModel model: setup.getModels()) if (!model.isStatisticsAvailable()) {allDone=false; break;}
		if (allDone) {
			out.println(Language.tr("CommandLine.FolderSimulation.ParameterSeriesAlreadyDone"));
			return false;
		}
		runner=new ParameterCompareRunner(null,null,log->out.println(log));
		final String error=runner.check(setup);
		if (error!=null) {
			out.println(BaseCommandLineSystem.errorBig+": "+error);
			return false;
		}

		/* Parameterreihe simulieren */
		runner.start();
		runner.waitForFinish();

		/* Ergebnisse speichern */
		if (!setup.saveToFile(getOutputFile(file))) {
			out.println(BaseCommandLineSystem.errorBig+": "+Language.tr("CommandLine.Error.UnableToSaveParameterSeriesResults"));
			return false;
		}

		return true;
	}

	/**
	 * Prüft, ob eine Datei geladen/verarbeitet werden kann
	 * und führt ggf. die Simulation durch.
	 * @param file	Zu prüfende Datei
	 * @param out	Ein {@link PrintStream}-Objekt, über das Texte ausgegeben werden können.
	 * @return	Liefert <code>true</code>, wenn die Datei erfolgreich verarbeitet werden konnte
	 */
	private boolean processFile(final File file, final PrintStream out) {
		if (file.isDirectory()) return false;
		out.println(file.getName()+":");

		try {
			if (getOutputFile(file).isFile()) {
				out.println(Language.tr("CommandLine.FolderSimulation.OutputFileExists"));
				return false;
			}

			/* Datei laden */
			final XMLTools xml=new XMLTools(file);
			final Element root=xml.load();
			if (root==null) {
				out.println(Language.tr("CommandLine.FolderSimulation.CannotProcessFile"));
				return false;
			}

			/* Als Modell verarbeiten */
			final EditModel model=new EditModel();
			if (model.loadFromXML(root)==null) {
				return processModel(file,model,out);
			}

			/* Als Parameterreihe verarbeiten */
			final ParameterCompareSetup setup=new ParameterCompareSetup(null);
			if (setup.loadFromXML(root)==null) {
				return processParameterSeries(file,setup,out);
			}

			out.println(Language.tr("CommandLine.FolderSimulation.CannotProcessFile"));
			return false;
		} finally {
			out.println("");
		}
	}

	@Override
	public void run(AbstractCommand[] allCommands, InputStream in, PrintStream out) {
		final String[] files=folder.list();
		if (files==null) {
			out.println(String.format(Language.tr("CommandLine.FolderSimulation.NoFilesInFolder"),folder.toString()));
			return;
		}

		Arrays.sort(files);

		int count=0;
		for (String file: files) {
			if (processFile(new File(folder,file),out)) count++;
			if (isQuit) break;
		}

		if (count==1) {
			out.println(String.format(Language.tr("CommandLine.FolderSimulation.ResultCount.Singular"),count));
		} else {
			out.println(String.format(Language.tr("CommandLine.FolderSimulation.ResultCount.Plural"),count));
		}
	}

	@Override
	public void setQuit() {
		isQuit=true;
		if (simulator!=null) simulator.cancel();
		if (runner!=null) runner.cancel();
	}
}
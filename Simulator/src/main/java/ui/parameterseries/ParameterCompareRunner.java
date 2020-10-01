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
package ui.parameterseries;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Consumer;

import language.Language;
import simulator.StartAnySimulator;
import simulator.editmodel.EditModel;
import simulator.simparser.ExpressionCalc;
import tools.SetupData;

/**
 * F�hrt die Verarbeitung der Parameter-Variationsstudien-Modell durch
 * @author Alexander Herzog
 */
public class ParameterCompareRunner {
	/** Wird aufgerufen, wenn Ergebnisse vorliegen und die Tabelle angepasst werden soll */
	private final Consumer<Integer> updateTable;
	/** Wird aufgerufen, wenn die Verarbeitung beendet wurde (sowohl wenn alle Modelle erfolgreich zu Ende simuliert wurden als auch wenn die Verarbeitung abgebrochen wurde) */
	private final Consumer<Boolean> whenDone;
	/** Wird aufgerufen, wenn Logging-Daten ausgegeben werden sollen */
	private final Consumer<String> logOutput;

	private int parallelRuns;
	private ParameterCompareRunnerModel[] modelRunner;
	private boolean canceled;
	private Thread runner;

	/**
	 * Konstruktor der Klasse
	 * @param updateTable	Wird aufgerufen, wenn Ergebnisse vorliegen und die Tabelle angepasst werden soll
	 * @param whenDone	Wird aufgerufen, wenn die Verarbeitung beendet wurde (sowohl wenn alle Modelle erfolgreich zu Ende simuliert wurden als auch wenn die Verarbeitung abgebrochen wurde)
	 * @param logOutput	Wird aufgerufen, wenn Logging-Daten ausgegeben werden sollen
	 */
	public ParameterCompareRunner(final Consumer<Integer> updateTable, final Consumer<Boolean> whenDone, final Consumer<String> logOutput) {
		this.updateTable=updateTable;
		this.whenDone=whenDone;
		this.logOutput=logOutput;
		canceled=false;
	}

	private int getThreadCount(final EditModel model) {
		if (model.getSingleCoreReason().size()==0) return 1; /* Modell als solches kann bereits parallel simuliert werden */
		if (model.repeatCount>1) return 1; /* Modell als solches kann bereits parallel simuliert werden */
		if (!SetupData.getSetup().useMultiCoreSimulation) return 1; /* Multi-Code per Setup verboten */

		final Runtime rt=Runtime.getRuntime();
		final int maxThreadMemory=(int)Math.max(1,(rt.maxMemory())/1024/1024/100);
		final int threadCount=Math.min(SetupData.getSetup().useMultiCoreSimulationMaxCount,Math.max(1,Math.min(rt.availableProcessors(),maxThreadMemory)));

		logOutput(Language.tr("Batch.MultiCoreInfo1"));
		logOutput(String.format(Language.tr("Batch.MultiCoreInfo2"),threadCount));

		return threadCount;
	}

	private synchronized void logOutput(final String text) {
		if (logOutput!=null) logOutput.accept(text);
	}

	private synchronized void done(final boolean runComplete) {
		if (whenDone!=null) whenDone.accept(runComplete);
	}

	private synchronized void modelDone(final ParameterCompareRunnerModel runner) {
		if (updateTable!=null) updateTable.accept(runner.getNr());
	}

	private String checkIntern(final ParameterCompareSetup setup) {
		/* Modell testen */
		final EditModel editModel=setup.getEditModel();
		final String error=StartAnySimulator.testModel(editModel);
		if (error!=null) return error;

		/* Pr�fen, ob Parameterreihen-Modelle vorhanden sind */
		if (setup.getModels().size()==0) return Language.tr("ParameterCompare.Run.Error.NoModels");

		final String[] outputScripts=new String[setup.getOutput().size()];
		for (int i=0;i<outputScripts.length;i++) {
			final ParameterCompareSetupValueOutput output=setup.getOutput().get(i);

			String scriptFile;
			File file;

			switch (output.getMode()) {
			case MODE_XML:
				/* Nichts zu pr�fen */
				break;
			case MODE_SCRIPT_JS:
				/* Pr�fen, ob Skripte, die Ausgabewerte liefern sollen, existieren. */
				scriptFile=output.getTag().trim();
				if (scriptFile.isEmpty()) return String.format(Language.tr("ParameterCompare.Run.Error.NoScriptFile"),output.getName());
				file=new File(scriptFile);
				if (!file.isFile()) return String.format(Language.tr("ParameterCompare.Run.Error.ScriptFileDoesNotExist"),output.getName(),scriptFile);
				try {
					final String[] lines=Files.lines(file.toPath()).toArray(String[]::new);
					outputScripts[i]=String.join("\n",lines);
				} catch (IOException e) {
					return String.format(Language.tr("ParameterCompare.Run.Error.CannotLoadScriptFile"),output.getName(),scriptFile);
				}
				break;
			case MODE_SCRIPT_JAVA:
				/* Pr�fen, ob Skripte, die Ausgabewerte liefern sollen, existieren. */
				scriptFile=output.getTag().trim();
				if (scriptFile.isEmpty()) return String.format(Language.tr("ParameterCompare.Run.Error.NoScriptFile"),output.getName());
				file=new File(scriptFile);
				if (!file.isFile()) return String.format(Language.tr("ParameterCompare.Run.Error.ScriptFileDoesNotExist"),output.getName(),scriptFile);
				try {
					final String[] lines=Files.lines(file.toPath()).toArray(String[]::new);
					outputScripts[i]=String.join("\n",lines);
				} catch (IOException e) {
					return String.format(Language.tr("ParameterCompare.Run.Error.CannotLoadScriptFile"),output.getName(),scriptFile);
				}
				break;
			case MODE_COMMAND:
				final int errorInt=ExpressionCalc.check(output.getTag(),null);
				if (errorInt>=0) return String.format(Language.tr("ParameterCompare.Settings.Output.Mode.Command.ErrorInfo"),output.getTag(),errorInt+1);
				break;
			}
		}

		/* Parallele Threads bestimmen */
		parallelRuns=getThreadCount(editModel);

		/* Modelle anlegen */
		modelRunner=new ParameterCompareRunnerModel[setup.getModels().size()];
		for (int i=0;i<modelRunner.length;i++) {
			final ParameterCompareSetupModel model=setup.getModels().get(i);
			if (model.isStatisticsAvailable()) continue;
			modelRunner[i]=new ParameterCompareRunnerModel(i,r->modelDone(r),s->logOutput(s),setup,outputScripts);
			final String err=modelRunner[i].prepare(editModel,model);
			if (err!=null) return err+" ("+String.format(Language.tr("ParameterCompare.Run.Error.PreparingModel"),i+1,model.getName())+")";
		}

		return null;
	}

	/**
	 * Pr�ft das Modell
	 * @param setup	Parameterreihen-Setup
	 * @return	Gibt im Erfolgsfall <code>null</code> zur�ck, sonst eine Fehlermeldung
	 */
	public String check(final ParameterCompareSetup setup) {
		final String error=checkIntern(setup);
		if (error!=null) logOutput(error);
		return error;
	}

	private void cancelAll() {
		for (ParameterCompareRunnerModel runner: modelRunner) {
			if (runner==null) continue;
			runner.cancel();
			if (updateTable!=null) updateTable.accept(runner.getNr());
		}
		modelRunner=null;
	}

	private void runParameterSeries() {
		final long startTime=System.currentTimeMillis();

		while (true) {
			ParameterCompareRunnerModel nextWaiting=null;
			boolean threadStarted=false;

			int running=0;
			for (ParameterCompareRunnerModel runner: modelRunner) {
				if (runner==null) continue;
				final ParameterCompareRunnerModel.Status status=runner.getStatus();
				if (nextWaiting==null && status==ParameterCompareRunnerModel.Status.STATUS_WAITING) nextWaiting=runner;
				if (status==ParameterCompareRunnerModel.Status.STATUS_RUNNING) running++;
			}

			if (nextWaiting==null && running==0) {
				/* Alles erledigt */
				logOutput(String.format(Language.tr("Batch.Simulation.Done1"),modelRunner.length));
				final double time=(System.currentTimeMillis()-startTime)/1000.0;
				logOutput(String.format(Language.tr("Batch.Simulation.Done2"),Math.round(time),Math.round(time/modelRunner.length)));
				done(true);
				return;
			}

			if (running<parallelRuns && nextWaiting!=null) {
				/* N�chsten starten */
				final String error=nextWaiting.start();
				if (error==null) {
					if (updateTable!=null) updateTable.accept(nextWaiting.getNr());
					logOutput(String.format(Language.tr("Batch.Simulation.RunNoValue"),nextWaiting.getNr()+1,nextWaiting.getName()));
				} else {
					/* Abbruch wegen Fehler */
					logOutput(error);
					logOutput(String.format(Language.tr("Batch.Simulation.ErrorCancelNr"),nextWaiting.getNr()+1,nextWaiting.getName()));
					cancelAll();
					done(true);
					return;
				}

				threadStarted=true;
			}

			if (canceled) {
				/* Nutzerabbruch */
				logOutput(Language.tr("Batch.Simulation.UserCancel"));
				cancelAll();
				done(false);
				return;
			}

			/* Wenn nichts geschehen ist: warten */
			if (!threadStarted)	{
				try {Thread.sleep(150);} catch (InterruptedException e) {}
				if (updateTable!=null) for (ParameterCompareRunnerModel runner: modelRunner) if (runner!=null) updateTable.accept(runner.getNr());
			}
		}
	}

	/**
	 * Startet die Verarbeitung
	 */
	public void start() {
		runner=new Thread(()->runParameterSeries(),"ParameterSeriesRunner");
		runner.start();
	}

	/**
	 * Wartet darauf, dass die Verarbeitung abgeschlossen wurde.<br>
	 * Kann nach {@link #start()} aufgerufen werden.
	 * @return	Gibt an, ob der Thread normal beendet wurde (<code>true</code>) oder beim Warten ein Fehler aufgetreten ist (<code>false</code>).
	 */
	public boolean waitForFinish() {
		if (runner==null) return false;

		try {
			runner.join();
		} catch (InterruptedException e) {
			return false;
		}

		return true;
	}

	/**
	 * Bricht die Verarbeitung ab
	 */
	public void cancel() {
		canceled=true;
	}
}

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

import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Consumer;

import language.Language;
import simulator.StartAnySimulator;
import simulator.editmodel.EditModel;
import simulator.simparser.ExpressionCalc;
import tools.Notifier;
import tools.SetupData;

/**
 * Führt die Verarbeitung der Parameter-Variationsstudien-Modell durch
 * @author Alexander Herzog
 */
public class ParameterCompareRunner {
	/** Übergeordnetes Fenster */
	private final Window parentWindow;

	/** Welches Modell soll simuliert werden? (Werte &lt;0 für alle) */
	private final int modelToSimulate;
	/** Wird aufgerufen, wenn Ergebnisse vorliegen und die Tabelle angepasst werden soll */
	private final Consumer<Integer> updateTable;
	/** Wird aufgerufen, wenn die Verarbeitung beendet wurde (sowohl wenn alle Modelle erfolgreich zu Ende simuliert wurden als auch wenn die Verarbeitung abgebrochen wurde) */
	private final Consumer<Boolean> whenDone;
	/** Wird aufgerufen, wenn Logging-Daten ausgegeben werden sollen */
	private final Consumer<String> logOutput;

	/**
	 * Anzahl an parallelen Parameterreihen-Simulationen<br>
	 * (Ist 1, wenn die Simulationen selbst parallelisiert werden können)
	 * @see #getThreadCount(EditModel)
	 */
	private int parallelRuns;

	/**
	 * Runner für die einzelnen Parameterreihen-Einträgen<br>
	 * (Für jedes Parameterreihen-Modell ein Eintrag, also durchaus mehr als {@link #parallelRuns}.)
	 */
	private ParameterCompareRunnerModel[] modelRunner;

	/**
	 * Wurde die Simulation abgebrochen?
	 * @see #cancelAll()
	 */
	private boolean canceled;

	/**
	 * Thread der die eigentlichen Parameterreihen-Simulationen anstößt.
	 * @see #start()
	 * @see #runParameterSeries()
	 */
	private Thread runner;

	/**
	 * Konstruktor der Klasse
	 * @param parentWindow	Übergeordnetes Fenster
	 * @param updateTable	Wird aufgerufen, wenn Ergebnisse vorliegen und die Tabelle angepasst werden soll
	 * @param whenDone	Wird aufgerufen, wenn die Verarbeitung beendet wurde (sowohl wenn alle Modelle erfolgreich zu Ende simuliert wurden als auch wenn die Verarbeitung abgebrochen wurde)
	 * @param logOutput	Wird aufgerufen, wenn Logging-Daten ausgegeben werden sollen
	 */
	public ParameterCompareRunner(final Window parentWindow, final Consumer<Integer> updateTable, final Consumer<Boolean> whenDone, final Consumer<String> logOutput) {
		this(parentWindow,-1,updateTable,whenDone,logOutput);
	}

	/**
	 * Konstruktor der Klasse
	 * @param parentWindow	Übergeordnetes Fenster
	 * @param modelToSimulate	Welches Modell soll simuliert werden? (Werte &lt;0 für alle)
	 * @param updateTable	Wird aufgerufen, wenn Ergebnisse vorliegen und die Tabelle angepasst werden soll
	 * @param whenDone	Wird aufgerufen, wenn die Verarbeitung beendet wurde (sowohl wenn alle Modelle erfolgreich zu Ende simuliert wurden als auch wenn die Verarbeitung abgebrochen wurde)
	 * @param logOutput	Wird aufgerufen, wenn Logging-Daten ausgegeben werden sollen
	 */
	public ParameterCompareRunner(final Window parentWindow, final int modelToSimulate, final Consumer<Integer> updateTable, final Consumer<Boolean> whenDone, final Consumer<String> logOutput) {
		this.parentWindow=parentWindow;
		this.modelToSimulate=modelToSimulate;
		this.updateTable=updateTable;
		this.whenDone=whenDone;
		this.logOutput=logOutput;
		canceled=false;
	}

	/**
	 * Konstruktor der Klasse
	 * @param updateTable	Wird aufgerufen, wenn Ergebnisse vorliegen und die Tabelle angepasst werden soll
	 * @param whenDone	Wird aufgerufen, wenn die Verarbeitung beendet wurde (sowohl wenn alle Modelle erfolgreich zu Ende simuliert wurden als auch wenn die Verarbeitung abgebrochen wurde)
	 * @param logOutput	Wird aufgerufen, wenn Logging-Daten ausgegeben werden sollen
	 */
	public ParameterCompareRunner(final Consumer<Integer> updateTable, final Consumer<Boolean> whenDone, final Consumer<String> logOutput) {
		this(null,-1,updateTable,whenDone,logOutput);
	}

	/**
	 * Bestimmt wie viele Parameterreihen-Modelle parallel simuliert werden sollen.<br>
	 * (Ist 1, wenn die Simulationen selbst parallelisiert werden können.)
	 * @param model	Ausgangs-Editor-Modell (um prüfen zu können, ob dieses bereits parallel simuliert werden kann)
	 * @return	Anzahl an parallelen Parameterreihen-Simulationen
	 */
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

	/**
	 * Gibt eine Ausgabe über {@link #logOutput} aus.
	 * @param text	Auszugebende Meldung
	 */
	private synchronized void logOutput(final String text) {
		if (logOutput!=null) logOutput.accept(text);
	}

	/**
	 * Wird von {@link #runParameterSeries()} aufgerufen, wenn die
	 * Parameterreihen-Simulation beendet wurde.
	 * @param runComplete	Wurde die Parameterreihen-Simulation erfolgreich beendet?
	 * @see #runParameterSeries()
	 */
	private synchronized void done(final boolean runComplete) {
		if (whenDone!=null) whenDone.accept(runComplete);
	}

	/**
	 * Wird aufgerufen, wenn die Simulation eines einzelnen Modells abgeschlossen wurde.
	 * @param runner	Zugehöriges Runner-Objekt
	 */
	private synchronized void modelDone(final ParameterCompareRunnerModel runner) {
		if (updateTable!=null) updateTable.accept(runner.getNr());
	}

	/**
	 * Prüft das Modell
	 * @param setup	Parameterreihen-Setup
	 * @param editModelPath	Pfad zur zugehörigen Modelldatei (als Basis für relative Pfade in Ausgabeelementen)
	 * @return	Gibt im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung
	 * @see #check(ParameterCompareSetup, String)
	 */
	private String checkIntern(final ParameterCompareSetup setup, final String editModelPath) {
		/* Modell testen */
		final EditModel editModel=setup.getEditModel();
		final StartAnySimulator.PrepareError error=StartAnySimulator.testModel(editModel,editModelPath);
		if (error!=null) return error.error;

		/* Prüfen, ob Parameterreihen-Modelle vorhanden sind */
		if (setup.getModels().size()==0) return Language.tr("ParameterCompare.Run.Error.NoModels");

		final String[] outputScripts=new String[setup.getOutput().size()];
		for (int i=0;i<outputScripts.length;i++) {
			final ParameterCompareSetupValueOutput output=setup.getOutput().get(i);

			String scriptFile;
			File file;

			switch (output.getMode()) {
			case MODE_XML:
				/* Nichts zu prüfen */
				break;
			case MODE_SCRIPT_JS:
				/* Prüfen, ob Skripte, die Ausgabewerte liefern sollen, existieren. */
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
				/* Prüfen, ob Skripte, die Ausgabewerte liefern sollen, existieren. */
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
		if (modelToSimulate>=0) {
			/* Nur ein bestimmtes Modell simulieren */
			final ParameterCompareSetupModel model=setup.getModels().get(modelToSimulate);
			modelRunner[modelToSimulate]=new ParameterCompareRunnerModel(modelToSimulate,true,r->modelDone(r),s->logOutput(s),setup,outputScripts);
			final String err=modelRunner[modelToSimulate].prepare(editModel,editModelPath,model);
			if (err!=null) return err+" ("+String.format(Language.tr("ParameterCompare.Run.Error.PreparingModel"),modelToSimulate+1,model.getName())+")";
		} else {
			/* Alle Modelle simulieren */
			for (int i=0;i<modelRunner.length;i++) {
				final ParameterCompareSetupModel model=setup.getModels().get(i);
				if (model.isStatisticsAvailable()) continue;
				modelRunner[i]=new ParameterCompareRunnerModel(i,false,r->modelDone(r),s->logOutput(s),setup,outputScripts);
				final String err=modelRunner[i].prepare(editModel,editModelPath,model);
				if (err!=null) return err+" ("+String.format(Language.tr("ParameterCompare.Run.Error.PreparingModel"),i+1,model.getName())+")";
			}
		}

		return null;
	}

	/**
	 * Prüft das Modell
	 * @param setup	Parameterreihen-Setup
	 * @param editModelPath	Pfad zur zugehörigen Modelldatei (als Basis für relative Pfade in Ausgabeelementen)
	 * @return	Gibt im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung
	 */
	public String check(final ParameterCompareSetup setup, final String editModelPath) {
		final String error=checkIntern(setup,editModelPath);
		if (error!=null) logOutput(error);
		return error;
	}

	/**
	 * Bricht alle laufenden Simulationen ab.
	 * @see #runParameterSeries()
	 */
	private void cancelAll() {
		for (ParameterCompareRunnerModel runner: modelRunner) {
			if (runner==null) continue;
			runner.cancel();
			if (updateTable!=null) updateTable.accept(runner.getNr());
		}
		modelRunner=null;
	}

	/**
	 * Führt die eigentliche Simulation der Parameterreihen-Modelle durch.
	 * @see #runner
	 */
	private void runParameterSeries() {
		final long startTime=System.currentTimeMillis();

		while (true) {
			ParameterCompareRunnerModel nextWaiting=null;
			boolean threadStarted=false;

			int waiting=0;
			int running=0;
			int done=0;
			for (ParameterCompareRunnerModel runner: modelRunner) {
				if (runner==null) continue;
				final ParameterCompareRunnerModel.Status status=runner.getStatus();
				if (nextWaiting==null && status==ParameterCompareRunnerModel.Status.STATUS_WAITING) nextWaiting=runner;
				switch (status) {
				case STATUS_CANCELED: done++; break;
				case STATUS_DONE: done++; break;
				case STATUS_RUNNING: running++; break;
				case STATUS_WAITING: waiting++; break;
				}
			}

			if (parentWindow!=null) Notifier.setSimulationProgress(parentWindow,(int)Math.round(100.0*(done+0.5*running)/(waiting+running+done)));

			if (nextWaiting==null && running==0) {
				/* Alles erledigt */
				if (parentWindow!=null) Notifier.setSimulationProgress(parentWindow,-1);
				final double time=(System.currentTimeMillis()-startTime)/1000.0;
				if (modelToSimulate>=0) {
					logOutput(String.format(Language.tr("Batch.Simulation.Done1"),1));
					logOutput(String.format(Language.tr("Batch.Simulation.Done2"),Math.round(time),Math.round(time/1)));
				} else {
					logOutput(String.format(Language.tr("Batch.Simulation.Done1"),modelRunner.length));
					logOutput(String.format(Language.tr("Batch.Simulation.Done2"),Math.round(time),Math.round(time/modelRunner.length)));
				}
				done(true);
				return;
			}

			if (running<parallelRuns && nextWaiting!=null) {
				/* Nächsten starten */
				final String error=nextWaiting.start();
				if (error==null) {
					if (updateTable!=null) updateTable.accept(nextWaiting.getNr());
					logOutput(String.format(Language.tr("Batch.Simulation.RunNoValue"),nextWaiting.getNr()+1,nextWaiting.getName()));
				} else {
					/* Abbruch wegen Fehler */
					if (parentWindow!=null) Notifier.setSimulationProgress(parentWindow,-1);
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
				if (parentWindow!=null) Notifier.setSimulationProgress(parentWindow,-1);
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
	 * Prüft, ob die Verarbeitung noch läuft.
	 * @return Liefert <code>true</code> wenn der Thread noch aktiv ist.
	 */
	public boolean isRunning() {
		if (runner==null) return false;
		return runner.isAlive();
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
	 * Bricht die Verarbeitung ab.
	 */
	public void cancel() {
		canceled=true;
	}
}

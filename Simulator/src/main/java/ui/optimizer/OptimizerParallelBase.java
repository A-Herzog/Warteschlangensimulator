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
package ui.optimizer;

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.w3c.dom.Document;

import language.Language;
import mathtools.NumberTools;
import simulator.AnySimulator;
import simulator.StartAnySimulator;
import simulator.editmodel.EditModel;
import simulator.statistics.Statistics;
import tools.SetupData;

/**
 * Dies ist die Basisklasse für alle parallel arbeitenden Optimierer,
 * d.h. für alle Optimierer, die jeweils mehrere Modell parallel simulieren und dann basierend
 * auf den Ergebnissen Änderungen an der Population vornehmen.<br>
 * Die konkrete Arbeit wird an serielle Kernel ({@link OptimizerParallelKernelBase})
 * delegiert.
 * @author Alexander Herzog
 * @see OptimizerBase
 * @see OptimizerParallelKernelBase
 */
public abstract class OptimizerParallelBase extends OptimizerBase {
	/**
	 * Zu verwendender Optimierer-Kernel
	 * @see #getOptimizerKernel()
	 */
	private OptimizerParallelKernelBase kernel;

	/**
	 * Simulatoren für die parallel auszuführenden
	 * Optimierungsschritten
	 * @see #runModelsParallel(EditModel[], String)
	 * @see #runModelsSerial(EditModel[], String)
	 */
	private volatile Object[] simulator;

	/** Startzeit der Simulatoren */
	private volatile long[] simulatorStartMS;

	/**
	 * Welche Simulation läuft gerade?
	 * @see #runModelsSerial(EditModel[], String)
	 */
	private volatile AtomicInteger runningSimulatorNr=new AtomicInteger();

	/**
	 * Timer, der prüft, welche Simulationen laufen und ggf.
	 * weitere Simulationen startet.
	 * @see #runModelsParallel(EditModel[], String)
	 * @see #runModelsSerial(EditModel[], String)
	 * @see #cancel()
	 */
	private volatile Timer timer;

	/**
	 * Speichert Einstellungen (und Ergebnisse) zu bereits durchgeführten
	 * Optimierungsschritten, um so zu verhindern, dass Simulationen mit
	 * bereits verwendeten Einstellungen erneut durchgeführt werden.
	 * @see #getCachedResult(double[])
	 */
	private List<OptimizationResult> resultsCache;

	/**
	 * Runde
	 */
	private int stepNr;

	/**
	 * Bisheriges bestes Ergebnis (kann <code>null</code> sein)
	 */
	private Statistics bestResultStatistics;

	/**
	 * Bisheriger bester Zielfunktionswert (nur gültig, wenn {@link #bestResultStatistics} ungleich <code>null</code> ist)
	 */
	private double bestResultValue;

	/**
	 * Wurde die Optimierung per {@link #cancel()} abgebrochen?
	 */
	private boolean canceled;

	/**
	 * Thread-Pool zum parallelen Erstellen der XML-Daten
	 */
	private ThreadPoolExecutor executorPool;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element (kann <code>null</code> sein, wenn kein solches vorhanden ist)
	 */
	public OptimizerParallelBase(final Component owner) {
		super(owner);
	}

	@Override
	public String check(final EditModel model, final String editModelPath, final OptimizerSetup setup, final Consumer<String> logOutput, final Consumer<Boolean> whenDone, final Runnable whenStepDone) {
		String error;

		error=super.check(model,editModelPath,setup,logOutput,whenDone,whenStepDone);
		if (error!=null) return error;

		kernel=getOptimizerKernel();
		error=kernel.initControlValuesConditions();
		if (error!=null) return error;

		resultsCache=new ArrayList<>();

		return null;
	}

	/**
	 * Liefert den konkret zu verwendenden seriellen Optimierungskernel
	 * @return	Zu verwendender serieller Optimierungskernel
	 */
	protected abstract OptimizerParallelKernelBase getOptimizerKernel();

	@Override
	public void start() {
		if (kernel==null) {
			done(false);
			return;
		}

		stepNr=0;
		initNextRun(null,null);
	}

	/**
	 * Legt Simulator-Starter Objekte in {@link #simulator} für die
	 * Modelle an.
	 * @param model	Modelle deren Start vorbereitet werden soll
	 * @param editModelPath	Pfad zur zugehörigen Modelldatei (als Basis für relative Pfade in Ausgabeelementen)
	 * @return	Liefert im Erfolgsfall <code>true</code>
	 * @see #simulator
	 * @see #runModelsParallel(EditModel[], String)
	 * @see #runModelsSerial(EditModel[], String)
	 */
	private boolean prepareModels(final EditModel[] model, final String editModelPath) {
		simulator=new Object[model.length];
		for (int i=0;i<model.length;i++) {
			if (model[i]==null) continue;
			final StartAnySimulator starter=new StartAnySimulator(model[i],editModelPath);
			final StartAnySimulator.PrepareError error=StartAnySimulator.testModel(model[i],editModelPath);
			if (error!=null) {
				logOutput("  "+Language.tr("Optimizer.Error.ErrorStartingSimulation")+":");
				logOutput("  "+error.error);
				logOutput(Language.tr("Optimizer.OptimizationCanceled"));
				done(false);
				return false;
			}
			simulator[i]=starter;
		}
		simulatorStartMS=new long[model.length];
		return true;
	}

	/**
	 * Simuliert eine Reihe von Modellen.<br>
	 * Da die Simulation der Modelle nicht inherent parallelisiert werden kann, werden stets mehrere Simulation gleichzeitig durchgeführt.
	 * @param model	Zu simulierende Modelle
	 * @param editModelPath	Pfad zur zugehörigen Modelldatei (als Basis für relative Pfade in Ausgabeelementen)
	 * @see #runModels(EditModel[], String)
	 */

	private synchronized void runModelsParallel(final EditModel[] model, final String editModelPath) {
		if (!prepareModels(model,editModelPath)) return;

		final Runtime rt=Runtime.getRuntime();
		final int maxThreadMemory=(int)Math.max(1,(rt.maxMemory())/1024/1024/100);
		int threadCount=Math.min(SetupData.getSetup().useMultiCoreSimulationMaxCount,Math.max(1,Math.min(rt.availableProcessors(),maxThreadMemory)));

		threadCount=Math.min(threadCount,4);
		if (model[0].useFixedSeed) threadCount=1;

		int started=0;
		for (int i=0;i<simulator.length;i++) if (simulator[i] instanceof StartAnySimulator) {
			final StartAnySimulator starter=(StartAnySimulator)simulator[i];
			final StartAnySimulator.PrepareError error=starter.prepare();
			if (error!=null) {
				logOutput("  "+Language.tr("Optimizer.Error.ErrorStartingSimulation")+":");
				logOutput("  "+error.error);
				logOutput(Language.tr("Optimizer.OptimizationCanceled"));
				done(false);
				return;
			}
			if (started<threadCount) {
				simulator[i]=starter.start();
				simulatorStartMS[i]=System.currentTimeMillis();
				started++;
			}
		}

		timer=new Timer("OptimizeProgressCheck");
		timer.schedule(new ParallelTimerTask(threadCount),100,100);
	}

	/**
	 * Wartet auf den Abschluss der parallelen Simulationen.
	 * @see OptimizerParallelBase#runModelsParallel(EditModel[], String)
	 */
	private class ParallelTimerTask extends TimerTask {
		/** Anzahl an parallelen Threads */
		private final int threadCount;

		/** Timeout in Millisekunden */
		private final long timeoutMS;

		/**
		 * Konstruktor der Klasse
		 * @param threadCount	Anzahl an parallelen Threads
		 */
		public ParallelTimerTask(final int threadCount) {
			this.threadCount=threadCount;
			timeoutMS=setup.timeoutSeconds*1000;
		}

		@Override
		public void run() {
			int running=0;
			for (int i=0;i<simulator.length;i++) if (simulator[i] instanceof AnySimulator && ((AnySimulator)simulator[i]).isRunning()) {
				if (timeoutMS>0 && System.currentTimeMillis()>simulatorStartMS[i]+timeoutMS) {
					((AnySimulator)simulator[i]).cancel();
					simulatorStartMS[i]=-1;
				} else {
					running++;
				}
			}
			for (int i=0;i<simulator.length;i++) if (running<threadCount && simulator[i] instanceof StartAnySimulator) {
				simulator[i]=((StartAnySimulator)simulator[i]).start();
				simulatorStartMS[i]=System.currentTimeMillis();
				running++;
			}
			if (running==0) {
				timer.cancel();
				final Statistics[] statistics=new Statistics[simulator.length];
				for (int i=0;i<simulator.length;i++) if (simulator[i] instanceof AnySimulator) {
					if (simulatorStartMS[i]<0) {
						statistics[i]=null;
					} else {
						statistics[i]=((AnySimulator)simulator[i]).getStatistic();
						if (statistics[i]==null) {
							logOutput("  "+Language.tr("Optimizer.Error.NoStatistics"));
							simulator=null;
							done(true);
							return;
						}
					}
				}
				simulator=null;
				runDone(statistics);
			}
		}
	}

	/**
	 * Simuliert eine Reihe von Modellen.<br>
	 * Da die Simulation der Modelle inherent parallelisiert werden kann, wird immer nur eine Simulation gleichzeitig durchgeführt.
	 * @param model	Zu simulierende Modelle
	 * @param editModelPath	Pfad zur zugehörigen Modelldatei (als Basis für relative Pfade in Ausgabeelementen)
	 * @see #runModels(EditModel[], String)
	 */
	private synchronized void runModelsSerial(final EditModel[] model, final String editModelPath) {
		if (!prepareModels(model,editModelPath)) return;

		runningSimulatorNr.set(0);
		while (true) {
			if (simulator[runningSimulatorNr.get()] instanceof StartAnySimulator) {
				final StartAnySimulator starter=(StartAnySimulator)simulator[runningSimulatorNr.get()];
				final StartAnySimulator.PrepareError error=starter.prepare();
				if (error!=null) {
					logOutput("  "+Language.tr("Optimizer.Error.ErrorStartingSimulation")+":");
					logOutput("  "+error.error);
					logOutput(Language.tr("Optimizer.OptimizationCanceled"));
					done(false);
					return;
				}
				final int nr=runningSimulatorNr.get();
				simulator[nr]=starter.start();
				simulatorStartMS[nr]=System.currentTimeMillis();
				break;
			}
			runningSimulatorNr.incrementAndGet();
			if (runningSimulatorNr.get()>=simulator.length) {
				final Statistics[] statistics=new Statistics[simulator.length];
				runDone(statistics);
				return;
			}
		}

		timer=new Timer("OptimizeProgressCheck");
		timer.schedule(new SerialTimerTask(),100,100);
	}

	/**
	 * Wartet auf den Abschluss einzelner Simulationen.
	 * @see OptimizerParallelBase#runModelsSerial(EditModel[], String)
	 */
	private class SerialTimerTask extends TimerTask {
		/** Timeout in Millisekunden */
		private final long timeoutMS;

		/**
		 * Konstruktor der Klasse
		 */
		public SerialTimerTask() {
			timeoutMS=setup.timeoutSeconds*1000;
		}

		@Override
		public void run() {
			int nr=runningSimulatorNr.get();
			final Object obj=simulator[nr];
			final long start=simulatorStartMS[nr];
			if ((obj instanceof AnySimulator) &&  ((AnySimulator)obj).isRunning()) {
				if (timeoutMS<0 || System.currentTimeMillis()<start+timeoutMS) return;
				((AnySimulator)obj).cancel();
				simulatorStartMS[nr]=-1;
			}
			while (runningSimulatorNr.get()<simulator.length-1) {
				runningSimulatorNr.incrementAndGet();
				if (simulator[runningSimulatorNr.get()] instanceof StartAnySimulator) {
					final StartAnySimulator starter=(StartAnySimulator)simulator[runningSimulatorNr.get()];
					final StartAnySimulator.PrepareError error=starter.prepare();
					if (error!=null) {
						logOutput("  "+Language.tr("Optimizer.Error.ErrorStartingSimulation")+":");
						logOutput("  "+error.error);
						logOutput(Language.tr("Optimizer.OptimizationCanceled"));
						timer.cancel();
						done(true);
						return;
					}
					nr=runningSimulatorNr.get();
					simulator[nr]=starter.start();
					simulatorStartMS[nr]=System.currentTimeMillis();
					return;
				}
			}
			timer.cancel();
			final Statistics[] statistics=new Statistics[simulator.length];
			for (int i=0;i<simulator.length;i++) if (simulator[i] instanceof AnySimulator) {
				if (simulatorStartMS[i]<0) {
					statistics[i]=null;
				} else {
					statistics[i]=((AnySimulator)simulator[i]).getStatistic();
					if (statistics[i]==null) {
						logOutput("  "+Language.tr("Optimizer.Error.NoStatistics"));
						simulator=null;
						done(true);
						return;
					}
				}
			}
			simulator=null;
			runDone(statistics);
		}
	}

	/**
	 * Simuliert eine Reihe von Modellen.
	 * @param models	Zu simulierende Modelle
	 * @param editModelPath	Pfad zur zugehörigen Modelldatei (als Basis für relative Pfade in Ausgabeelementen)
	 */
	private synchronized void runModels(final EditModel[] models, final String editModelPath) {
		boolean hasMultiCoreModel=Stream.of(models).filter(model->model!=null && model.getSingleCoreReason().size()==0).findFirst().isPresent();
		for (var model: models) if (model!=null && model.useFixedSeed) {hasMultiCoreModel=true; break;}

		if (hasMultiCoreModel) {
			runModelsSerial(models,editModelPath);
		} else {
			runModelsParallel(models,editModelPath);
		}
	}

	/**
	 * Schließt die Simulationen in einer Optimierungssrunde ab
	 * @param statistics	Simulationsergebnisse in der aktuellen Runde
	 * @see #runModelsParallel(EditModel[], String)
	 * @see #runModelsSerial(EditModel[], String)
	 */
	private synchronized void runDone(final Statistics[] statistics) {
		if (canceled) return;

		final double[] values=new double[statistics.length];
		final boolean[] emergencyShutDown=new boolean[statistics.length];

		/* Paralleles Erstellen der XML-Dokumente */
		final int maxThreads=Math.min(10,Runtime.getRuntime().availableProcessors());
		executorPool=new ThreadPoolExecutor(maxThreads,maxThreads,2,TimeUnit.SECONDS,new LinkedBlockingQueue<>());
		executorPool.allowCoreThreadTimeOut(true);
		final List<Future<Document>> documents=new ArrayList<>();
		for (int i=0;i<statistics.length;i++) if (statistics[i]==null) {
			documents.add(null);
		} else {
			final int nr=i;
			documents.add(executorPool.submit(()->{return statistics[nr].saveToXMLDocument();}));
		}

		for (int i=0;i<statistics.length;i++) {

			logOutput("  "+String.format(Language.tr("Optimizer.Individuum"),i+1));
			outputControlVariables("    ",kernel.getControlVariablesForModel(i));

			File file=null;
			Document doc=null;

			if (statistics[i]==null) {
				final OptimizationResult result=getCachedResult(kernel.getControlVariablesForModel(i));
				if (result==null) {
					values[i]=0;
					emergencyShutDown[i]=true;
					logOutput("    "+Language.tr("Optimizer.Target.CanceledByTimeout"));
				} else {
					values[i]=result.result;
					emergencyShutDown[i]=!result.valueOk;
					logOutput("    "+Language.tr("Optimizer.KnownResult"));
				}
			} else {
				/* Statistik speichern */
				try {
					doc=documents.get(i).get();
				} catch (InterruptedException | ExecutionException e) {done(true); return;}

				if (setup.outputMode==OptimizerSetup.OutputMode.OUTPUT_ALL) {
					file=saveStatistics(doc);
				}

				/* Zielwert prüfen */
				final Double value=checkTarget(doc);
				if (value==null) {
					/* Abbruch der Optimierung wegen Fehler */
					done(true);
					return;
				}
				values[i]=value;
				emergencyShutDown[i]=statistics[i].simulationData.emergencyShutDown;

				/* Ist neues Ergebnis besser als bisheriges bestes Ergebnis? */
				if (bestResultStatistics==null) {
					bestResultStatistics=statistics[i];
					if (setup.targetDirection==0) {
						bestResultValue=Math.max(0,setup.targetRangeMin-value)+Math.max(0,value-setup.targetRangeMax);
					} else {
						bestResultValue=value;
					}
				} else {
					switch (setup.targetDirection) {
					case -1:
						if (value<bestResultValue) {
							bestResultStatistics=statistics[i];
							bestResultValue=value;
						}
						break;
					case 0:
						final double newDelta=Math.max(0,setup.targetRangeMin-value)+Math.max(0,value-setup.targetRangeMax);
						if (newDelta<bestResultValue) {
							bestResultStatistics=statistics[i];
							bestResultValue=newDelta;
						}
						break;
					case 1:
						if (value>bestResultValue) {
							bestResultStatistics=statistics[i];
							bestResultValue=value;
						}
						break;
					}
				}

			}

			/* Zielwert ausgeben */
			logOutput(String.format("    "+Language.tr("Optimizer.ValueOfTheTarget")+": %s",NumberTools.formatNumber(values[i],3)));
			switch (setup.targetDirection) {
			case -1:
				logOutput("    "+Language.tr("Optimizer.Target.Minimize"));
				break;
			case 0:
				logOutput(String.format("    "+Language.tr("Optimizer.Target.Range"),NumberTools.formatNumber(setup.targetRangeMin),NumberTools.formatNumber(setup.targetRangeMax)));
				break;
			case 1:
				logOutput("    "+Language.tr("Optimizer.Target.Maximize"));
				break;
			}

			/* Ziel erreicht ? */
			if (doc!=null && setup.targetDirection==0 && values[i]>=setup.targetRangeMin && values[i]<=setup.targetRangeMax) {
				if (file==null) file=saveStatistics(doc);
				logOutput(String.format(Language.tr("Optimizer.Finished"),file.getName()));
				final double[] v=Arrays.copyOf(values,i+1);
				final boolean[] b=new boolean[v.length]; Arrays.fill(b,false);
				b[b.length-1]=true;
				addOptimizationRunResults(kernel.getControlVariables(),v,b);
				done(true);
				return;
			}

			/* Wert speichern */
			resultsCache.add(new OptimizationResult(kernel.getControlVariablesForModel(i),values[i],!emergencyShutDown[i]));
		}

		executorPool.shutdown();

		/* Nächster Optimierungsschritt */
		initNextRun(values,emergencyShutDown);
	}

	/**
	 * Bereitet die Simulationen der nächsten Modelle vor und startet diese ggf.
	 * @param lastResults	Letzte Ergebniswerte
	 * @param simulationWasEmergencyStopped	Wurden einzelne Simulationen im letzten Schritt abgebrochen?
	 * @see #runModels(EditModel[], String)
	 * @see #done(boolean)
	 */
	private void initNextRun(final double[] lastResults, final boolean[] simulationWasEmergencyStopped) {
		stepNr++;
		logOutput(String.format(Language.tr("Optimizer.Round.Nr"),stepNr));

		final EditModel[] currentModels=kernel.setupNextStep(lastResults,simulationWasEmergencyStopped);
		for (String line: kernel.getMessages()) logOutput(line);

		if (currentModels==null || currentModels.length==0) {
			logOutput("\n"+Language.tr("Optimizer.Round.NoModelsForNextRound"));
			done(true);
		} else {
			if (lastResults==null) {
				/* Erste Runde */
				logOutput(String.format(Language.tr("Optimizer.Round.One"),currentModels.length));
			} else {
				/* Weitere Runde */
				logOutput("\n"+String.format(Language.tr("Optimizer.Round.Next"),currentModels.length));

				int removeCount=0;
				for (int i=0;i<currentModels.length;i++) {
					if (getCachedResult(kernel.getControlVariablesForModel(i))!=null) {currentModels[i]=null; removeCount++;}
				}
				if (removeCount==currentModels.length) {
					logOutput("\n"+Language.tr("Optimizer.Round.NoModelsForNextRound"));
					done(true);
					return;
				}
				if (removeCount>0) logOutput("("+String.format(Language.tr("Optimizer.KnownResult.Info"),removeCount)+")");
			}

			runModels(currentModels,kernel.editModelPath);
		}
	}

	@Override
	public void cancel() {
		canceled=true;
		if (timer!=null) timer.cancel();
		if (simulator!=null && simulator.length>0) for (Object sim: simulator) if (sim instanceof AnySimulator) ((AnySimulator)sim).cancel();
		logOutput(Language.tr("Optimizer.OptimizationCanceled"));
		done(false);
	}

	/**
	 * Liefert zu einem Kontrollvariablen-Setup die zugehörigen Ergebnisse
	 * (oder <code>null</code>, wenn das konkrete Kontrollvariablen-Setup
	 * noch nicht simuliert wurde).
	 * @param control	Kontrollvariablen-Setup
	 * @return	Ergebnisse für das Kontrollvariablen-Setup soll <code>null</code>
	 * @see #resultsCache
	 */
	private OptimizationResult getCachedResult(final double[] control) {
		for (OptimizationResult result: resultsCache) if (result.match(control)) return result;
		return null;
	}

	/**
	 * Ergebnisse eines Optimierungsschritts
	 * @see OptimizerParallelBase#resultsCache
	 * @see OptimizerParallelBase#getCachedResult(double[])
	 */
	private static class OptimizationResult {
		/** Kontrollvariablen-Setup */
		private final double[] control;
		/** Ergebnis der Simulation */
		private final double result;
		/** Handelt es sich um einen gültigen Ergebniswert? */
		private final boolean valueOk;

		/**
		 * Konstruktor der Klasse
		 * @param control	Kontrollvariablen-Setup
		 * @param result	Ergebnis der Simulation
		 * @param valueOk	Handelt es sich um einen gültigen Ergebniswert?
		 */
		public OptimizationResult(final double[] control, final double result, final boolean valueOk) {
			this.control=Arrays.copyOf(control,control.length);
			this.result=result;
			this.valueOk=valueOk;
		}

		/**
		 * Gilt der aktuelle Ergebnisdatensatz für ein bestimmtes Kontrollvariablen-Setup?
		 * @param control	Kontrollvariablen-Setup das mit dem in diesem Objekt hinterlegten Kontrollvariablen-Setup verglichen werden soll
		 * @return	Liefert <code>true</code>, wenn das Kontrollvariablen-Setup in diesem Objekt mit dem übergebenen Kontrollvariablen-Setup übereinstimmt
		 */
		public boolean match(final double[] control) {
			for (int i=0;i<control.length;i++) if (this.control[i]!=control[i]) return false;
			return true;
		}
	}

	@Override
	protected Statistics bestResultSoFar() {
		return bestResultStatistics;
	}
}

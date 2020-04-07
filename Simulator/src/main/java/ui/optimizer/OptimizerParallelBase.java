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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.w3c.dom.Document;

import language.Language;
import mathtools.NumberTools;
import simulator.AnySimulator;
import simulator.StartAnySimulator;
import simulator.editmodel.EditModel;
import simulator.statistics.Statistics;

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
	private OptimizerParallelKernelBase kernel;
	private volatile Object[] simulator;
	private volatile AtomicInteger runningSimulatorNr=new AtomicInteger();
	private volatile Timer timer;
	private List<OptimizationResult> resultsCache;

	@Override
	public String check(final EditModel model, final OptimizerSetup setup, final Consumer<String> logOutput, final Consumer<Boolean> whenDone, final Runnable whenStepDone) {
		String error;

		error=super.check(model,setup,logOutput,whenDone,whenStepDone);
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

		initNextRun(null,null);
	}

	private boolean prepareModels(final EditModel[] model) {
		simulator=new Object[model.length];
		for (int i=0;i<model.length;i++) {
			if (model[i]==null) continue;
			final StartAnySimulator starter=new StartAnySimulator(model[i]);
			final String error=StartAnySimulator.testModel(model[i]);
			if (error!=null) {
				logOutput("  "+Language.tr("Optimizer.Error.ErrorStartingSimulation")+":");
				logOutput("  "+error);
				logOutput(Language.tr("Optimizer.OptimizationCanceled"));
				done(false);
				return false;
			}
			simulator[i]=starter;
		}
		return true;
	}

	private synchronized void runModelsParallel(final EditModel[] model) {
		if (!prepareModels(model)) return;

		/*
		final Runtime rt=Runtime.getRuntime();
		final int maxThreadMemory=(int)Math.max(1,(rt.maxMemory())/1024/1024/100);
		final int threadCount=Math.min(SetupData.getSetup().useMultiCoreSimulationMaxCount,Math.max(1,Math.min(rt.availableProcessors(),maxThreadMemory)));
		 */

		final int threadCount=2;

		int started=0;
		for (int i=0;i<simulator.length;i++) if (simulator[i] instanceof StartAnySimulator) {
			final StartAnySimulator starter=(StartAnySimulator)simulator[i];
			final String error=starter.prepare();
			if (error!=null) {
				logOutput("  "+Language.tr("Optimizer.Error.ErrorStartingSimulation")+":");
				logOutput("  "+error);
				logOutput(Language.tr("Optimizer.OptimizationCanceled"));
				done(false);
				return;
			}
			if (started<threadCount) {
				simulator[i]=starter.start();
				started++;
			}
		}

		timer=new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				int running=0;
				for (Object sim: simulator) if (sim instanceof AnySimulator && ((AnySimulator)sim).isRunning()) running++;
				for (int i=0;i<simulator.length;i++) if (running<threadCount && simulator[i] instanceof StartAnySimulator) {
					simulator[i]=((StartAnySimulator)simulator[i]).start();
					running++;
				}
				if (running==0) {
					timer.cancel();
					final Statistics[] statistics=new Statistics[simulator.length];
					for (int i=0;i<simulator.length;i++) if (simulator[i] instanceof AnySimulator) {
						statistics[i]=((AnySimulator)simulator[i]).getStatistic();
						if (statistics[i]==null) {
							logOutput("  "+Language.tr("Optimizer.Error.NoStatistics"));
							simulator=null;
							done(true);
							return;
						}
					}
					simulator=null;
					runDone(statistics);
				}
			}
		},100,100);
	}

	private synchronized void runModelsSerial(final EditModel[] model) {
		if (!prepareModels(model)) return;

		runningSimulatorNr.set(0);
		while (true) {
			if (simulator[runningSimulatorNr.get()] instanceof StartAnySimulator) {
				final StartAnySimulator starter=(StartAnySimulator)simulator[runningSimulatorNr.get()];
				final String error=starter.prepare();
				if (error!=null) {
					logOutput("  "+Language.tr("Optimizer.Error.ErrorStartingSimulation")+":");
					logOutput("  "+error);
					logOutput(Language.tr("Optimizer.OptimizationCanceled"));
					done(false);
					return;
				}
				simulator[runningSimulatorNr.get()]=starter.start();
				break;
			}
			runningSimulatorNr.incrementAndGet();
			if (runningSimulatorNr.get()>=simulator.length) {
				final Statistics[] statistics=new Statistics[simulator.length];
				runDone(statistics);
				return;
			}
		}

		timer=new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				final Object obj=simulator[runningSimulatorNr.get()];
				if ((obj instanceof AnySimulator) &&  ((AnySimulator)obj).isRunning()) return;
				while (runningSimulatorNr.get()<simulator.length-1) {
					runningSimulatorNr.incrementAndGet();
					if (simulator[runningSimulatorNr.get()] instanceof StartAnySimulator) {
						final StartAnySimulator starter=(StartAnySimulator)simulator[runningSimulatorNr.get()];
						final String error=starter.prepare();
						if (error!=null) {
							logOutput("  "+Language.tr("Optimizer.Error.ErrorStartingSimulation")+":");
							logOutput("  "+error);
							logOutput(Language.tr("Optimizer.OptimizationCanceled"));
							done(true);
							return;
						}
						simulator[runningSimulatorNr.get()]=starter.start();
						return;
					}
				}
				timer.cancel();
				final Statistics[] statistics=new Statistics[simulator.length];
				for (int i=0;i<simulator.length;i++) if (simulator[i] instanceof AnySimulator) {
					statistics[i]=((AnySimulator)simulator[i]).getStatistic();
					if (statistics[i]==null) {
						logOutput("  "+Language.tr("Optimizer.Error.NoStatistics"));
						simulator=null;
						done(true);
						return;
					}
				}
				simulator=null;
				runDone(statistics);
			}
		},100,100);
	}

	private synchronized void runModels(final EditModel[] model) {
		for (int i=0;i<model.length;i++) {
			if (model[i]==null) continue;
			if (model[i].getSingleCoreReason().size()>0) {
				runModelsParallel(model);
			} else {
				runModelsSerial(model);
			}
			return;
		}
	}

	private synchronized void runDone(final Statistics[] statistics) {
		final double[] values=new double[statistics.length];
		final boolean[] emergencyShutDown=new boolean[statistics.length];

		/* Paralleles Erstellen der XML-Dokumente */
		final ThreadPoolExecutor executorPool=new ThreadPoolExecutor(0,10,2,TimeUnit.SECONDS,new ArrayBlockingQueue<Runnable>(1));
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
				values[i]=result.result;
				emergencyShutDown[i]=!result.valueOk;
				logOutput("    "+Language.tr("Optimizer.KnownResult"));
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
				addOptimizationRunResults(v,b);
				done(true);
				return;
			}

			/* Wert speichern */
			resultsCache.add(new OptimizationResult(kernel.getControlVariablesForModel(i),values[i],!emergencyShutDown[i]));
		}

		/* Nächster Optimierungsschritt */
		initNextRun(values,emergencyShutDown);
	}

	private void initNextRun(final double[] lastResults, final boolean[] simulationWasEmergencyStopped) {
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

			runModels(currentModels);
		}
	}

	@Override
	public void cancel() {
		if (timer!=null) timer.cancel();
		if (simulator!=null && simulator.length>0) for (Object sim: simulator) if (sim instanceof AnySimulator) ((AnySimulator)sim).cancel();
		logOutput(Language.tr("Optimizer.OptimizationCanceled"));
		done(false);
	}

	private OptimizationResult getCachedResult(final double[] control) {
		for (OptimizationResult result: resultsCache) if (result.match(control)) return result;
		return null;
	}

	private class OptimizationResult {
		private final double[] control;
		private final double result;
		private final boolean valueOk;

		public OptimizationResult(final double[] control, final double result, final boolean valueOk) {
			this.control=Arrays.copyOf(control,control.length);
			this.result=result;
			this.valueOk=valueOk;
		}

		public boolean match(final double[] control) {
			for (int i=0;i<control.length;i++) if (this.control[i]!=control[i]) return false;
			return true;
		}
	}
}

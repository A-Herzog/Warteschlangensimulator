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
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

import org.w3c.dom.Document;

import language.Language;
import mathtools.NumberTools;
import simulator.AnySimulator;
import simulator.StartAnySimulator;
import simulator.editmodel.EditModel;
import simulator.statistics.Statistics;

/**
 * Dies ist die Basisklasse für alle seriell arbeitenden Optimierer,
 * d.h. für alle Optimierer, die jeweils ein Modell simulieren und dann basierend
 * auf den Ergebnissen Änderungen vornehmen.<br>
 * Die konkrete Arbeit wird an serielle Kernel ({@link OptimizerSerialKernelBase})
 * delegiert.
 * @author Alexander Herzog
 * @see OptimizerBase
 * @see OptimizerSerialKernelBase
 */
public abstract class OptimizerSerialBase extends OptimizerBase {
	private OptimizerSerialKernelBase kernel;
	private volatile AnySimulator simulator;
	private volatile Timer timer;

	@Override
	public String check(final EditModel model, final OptimizerSetup setup, final Consumer<String> logOutput, final Consumer<Boolean> whenDone, final Runnable whenStepDone) {
		String error;

		error=super.check(model,setup,logOutput,whenDone,whenStepDone);
		if (error!=null) return error;

		kernel=getOptimizerKernel();
		error=kernel.initControlValuesConditions();
		if (error!=null) return error;

		return null;
	}

	/**
	 * Liefert den konkret zu verwendenden seriellen Optimierungskernel
	 * @return	Zu verwendender serieller Optimierungskernel
	 */
	protected abstract OptimizerSerialKernelBase getOptimizerKernel();

	@Override
	public void start() {
		if (kernel==null) {
			done(false);
			return;
		}
		initNextRun(0,0,false);
	}

	private synchronized void runModel(final int stepNr, final EditModel model) {
		final StartAnySimulator starter=new StartAnySimulator(model);
		final String error=starter.prepare();
		if (error!=null) {
			logOutput("  "+Language.tr("Optimizer.Error.ErrorStartingSimulation")+":");
			logOutput("  "+error);
			logOutput(Language.tr("Optimizer.OptimizationCanceled"));
			done(false);
		} else {
			simulator=starter.start();
			timer=new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					if (!simulator.isRunning()) {
						timer.cancel();
						final Statistics statistics=simulator.getStatistic();
						simulator=null;
						if (statistics==null) {
							logOutput("  "+Language.tr("Optimizer.Error.NoStatistics"));
							done(true);
							return;
						}

						runDone(stepNr,statistics);
					}
				}
			},100,100);
		}
	}

	private synchronized void runDone(final int stepNr, final Statistics statistics) {
		/* Statistik speichern */
		final Document doc=statistics.saveToXMLDocument();
		File file=null;
		if (setup.outputMode==OptimizerSetup.OutputMode.OUTPUT_ALL) {
			file=saveStatistics(doc);
		}

		/* Zielwert prüfen */
		final Double value=checkTarget(doc);
		if (value==null) {
			/* Abbruch der Optimierung wegen Fehler */
			done(false);
			return;
		}

		/* Zielwert ausgeben */
		logOutput(String.format("  "+Language.tr("Optimizer.ValueOfTheTarget")+": %s",NumberTools.formatNumber(value)));
		switch (setup.targetDirection) {
		case -1:
			logOutput("  "+Language.tr("Optimizer.Target.Minimize"));
			break;
		case 0:
			logOutput(String.format("  "+Language.tr("Optimizer.Target.Range"),NumberTools.formatNumber(setup.targetRangeMin),NumberTools.formatNumber(setup.targetRangeMax)));
			break;
		case 1:
			logOutput("  "+Language.tr("Optimizer.Target.Maximize"));
			break;
		}
		addOptimizationRunResults(value);

		/* Ziel erreicht ? */
		if (setup.targetDirection==0 && value>=setup.targetRangeMin && value<=setup.targetRangeMax) {
			if (file==null) file=saveStatistics(doc);
			logOutput(String.format(Language.tr("Optimizer.Finished"),(file==null)?"":file.getName()));
			done(true);
			return;
		}

		/* Nächster Optimierungsschritt */
		initNextRun(stepNr+1,value,statistics.simulationData.emergencyShutDown);
	}

	private void initNextRun(final int stepNr, final double lastResult, final boolean simulationWasEmergencyStopped) {
		final EditModel currentModel=kernel.setupNextStep(stepNr,lastResult,simulationWasEmergencyStopped);
		for (String line: kernel.getMessages()) logOutput(line);
		outputControlVariables("  ",kernel.controlValues);

		if (currentModel==null) {
			done(true);
		} else {
			runModel(stepNr,currentModel);
		}
	}

	@Override
	public void cancel() {
		if (timer!=null) timer.cancel();
		if (simulator!=null) simulator.cancel();
		logOutput(Language.tr("Optimizer.OptimizationCanceled"));
		done(false);
	}
}

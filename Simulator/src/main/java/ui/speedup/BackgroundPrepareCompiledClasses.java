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
package ui.speedup;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import simulator.Simulator;
import simulator.editmodel.EditModel;
import simulator.examples.EditModelExamples;
import tools.SetupData;

/**
 * Wird diese Klasse instanziert, so wird geprüft, ob im Setup die Hintergrundsimulation
 * aktiv ist und wenn ja wird ein kleines Modell auf einem CPU-Kern simuliert und die
 * Ergebnisse werden verworfen. Ziel ist es, den Kompiler zu motivieren, die Simulator-Kernklassen
 * schon einmal zu übersetzen, so dass die erste echte Simulation dann mit volle Geschwindigkeit
 * laufen kann.
 * @author Alexander Herzog
 */
public class BackgroundPrepareCompiledClasses {
	/** Verzögerung in Millisekunden bevor der Thread startet */
	private static final int START_DELAY=5_000;

	/**
	 * Konstruktor der Klasse
	 */
	private BackgroundPrepareCompiledClasses() {
		final SetupData setup=SetupData.getSetup();
		final SetupData.BackgroundProcessingMode backgroundMode=setup.backgroundSimulation;
		if (!setup.serverUse && (backgroundMode==SetupData.BackgroundProcessingMode.BACKGROUND_SIMULATION || backgroundMode==SetupData.BackgroundProcessingMode.BACKGROUND_SIMULATION_ALWAYS)) {

			final ScheduledExecutorService scheduler=new ScheduledThreadPoolExecutor(1,(ThreadFactory)r-> {
				final Thread t=new Thread(r,"Precompile Simulator classes");
				t.setDaemon(true);
				return t;
			});
			scheduler.schedule(()->work(),START_DELAY,TimeUnit.MILLISECONDS);
			scheduler.shutdown();
		}
	}

	/**
	 * Wurde {@link #run()} noch nie ausgeführt?
	 */
	private static boolean firstRun=true;

	/**
	 * Klassen vorab laden.
	 */
	public static void run() {
		if (!firstRun) return;
		firstRun=false;
		new BackgroundPrepareCompiledClasses();

	}

	/**
	 * Verzögert ausgeführter Thread-Inhalt zum vorab Laden der Klassen.
	 */
	private void work() {
		if (Simulator.isSimulationStarted()) return; /* Keine Verarbeitung, wenn bereits eine Simulation ausgeführt wurde. */

		final EditModel model=EditModelExamples.getExampleByIndex(null,0);
		model.clientCount=250_000;
		model.distributionRecordHours=0;
		model.distributionRecordClientDataValues=0;

		final Simulator simulator=new Simulator(1,model,null,null,Simulator.logTypeFull);
		if (simulator.prepare()==null) {
			simulator.doNotRecordSimulatedClientsToStatistics();
			simulator.start();
			simulator.getStatistic();
		}
	}
}

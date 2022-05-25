/**
 * Copyright 2022 Alexander Herzog
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
package simulator.events;

import language.Language;
import simcore.Event;
import simcore.SimData;
import simulator.elements.RunElementSignal;
import simulator.runmodel.SimulationData;

/**
 * Ereignis zur verzögerten Signalauslösung an einer
 * {@link RunElementSignal}-Station
 * @author Alexander Herzog
 * @see RunElementSignal
 */
public class FireSignalDelayed extends Event {
	/**
	 * Signal-Station von der das Ereignis ausgeht (fürs Logging)
	 */
	public RunElementSignal signalStation;

	/**
	 * Name des auszulösenden Signals
	 */
	public String signalName;

	/**
	 * Konstruktor der Klasse
	 */
	public FireSignalDelayed() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	@Override
	public void run(SimData data) {
		final SimulationData simData=(SimulationData)data;

		/* Logging */
		signalStation.log(simData,Language.tr("Simulation.Log.Signal"),String.format(Language.tr("Simulation.Log.Signal.InfoDelay2"),signalName));

		/* Signal auslösen */
		simData.runData.fireSignal(simData,signalName);
	}
}

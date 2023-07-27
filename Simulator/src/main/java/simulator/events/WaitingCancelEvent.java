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
package simulator.events;

import language.Language;
import simcore.Event;
import simcore.SimData;
import simulator.elements.RunElementProcess;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.SimulationData;

/**
 * Kunde gibt das Warten auf
 * @author Alexander Herzog
 */
public class WaitingCancelEvent extends Event {
	/**
	 * Kunde der das Warten abbricht
	 */
	public RunDataClient client;

	/**
	 * Station an der der Warteabbruch auftritt
	 */
	public RunElementProcess station;

	/**
	 * Konstruktor der Klasse
	 */
	public WaitingCancelEvent() {
		/*
		 * Wird nur ben�tigt, um einen JavaDoc-Kommentar f�r diesen (impliziten) Konstruktor
		 * setzen zu k�nnen, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	@Override
	public void run(SimData data) {
		final SimulationData simData=(SimulationData)data;

		/* Logging */
		if (simData.loggingActive) station.log(simData,Language.tr("Simulation.Log.WaitingCancelation"),String.format(Language.tr("Simulation.Log.WaitingCancelation.Info"),client.logInfo(simData),station.name));

		/* Verarbeitung */
		station.processWaitingCancel(simData,client,0);

		/* System �ber Status-�nderung benachrichtigen */
		simData.runData.fireStateChangeNotify(simData);
	}
}
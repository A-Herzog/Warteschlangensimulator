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
import simulator.runmodel.RunData;
import simulator.runmodel.SimulationData;

/**
 * Beendet die Einschwingphase zeitgesteuert.
 * @author Alexander Herzog
 * @see RunData#initRun(long, SimulationData, boolean)
 */
public class EndWarmUpEvent extends Event {
	/**
	 * Konstruktor der Klasse
	 */
	public EndWarmUpEvent() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	@Override
	public void run(SimData data) {
		final SimulationData simData=(SimulationData)data;

		/* Wurde die Einschwingphase schon beendet? (z.B. über die Anzahl an Ankünften?) */
		if (!simData.runData.isWarmUp) return;

		simData.endWarmUp();

		/* Logging */
		if (simData.loggingActive) simData.logEventExecution(Language.tr("Simulation.Log.WarmUpEnd"),-1,Language.tr("Simulation.Log.WarmUpEnd.Info"));
	}
}

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

import simcore.Event;
import simcore.SimData;
import simulator.runmodel.SimulationData;

/**
 * Benachrichtigung des Systems, dass sich der Systemzustand geändert hat
 * @author Alexander Herzog
 */
public class SystemChangeEvent extends Event {

	@Override
	public void run(SimData data) {
		/* System über Status-Änderung benachrichtigen */
		((SimulationData)data).runData.fireStateChangeNotify((SimulationData)data);
	}

	/**
	 * Legt das nächste Ereignis dieser Art an
	 * @param simData	Simulationsdatenobjekt
	 * @param deltaMS	Ausführungszeitpunkt als Differenz vom aktuellen Zeitpunkt aus
	 */
	public static void triggerEvent(final SimulationData simData, final long deltaMS) {
		if (simData.runData.stopp) return;
		final SystemChangeEvent stateChange=(SystemChangeEvent)simData.getEvent(SystemChangeEvent.class);
		stateChange.init(simData.currentTime+deltaMS);
		simData.eventManager.addEvent(stateChange);
	}
}

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
import simulator.runmodel.RunData;
import simulator.runmodel.SimulationData;

/**
 * Benachrichtigung des Systems nach einer bestimmten Zeit
 * (statt angesteuert durch die Veränderung des Systemzustands)
 * @author Alexander Herzog
 * @see RunData#requestTimedChecks(SimulationData, simulator.elements.StateChangeListener)
 * @see RunData#runTimedCheckNow(SimulationData)
 */
public class TimedCheckEvent extends Event {
	@Override
	public void run(SimData data) {
		final SimulationData simData=(SimulationData)data;
		if (simData.runData.runTimedCheckNow(simData)) scheduleCheck(simData);
	}

	/**
	 * Legt das nächste Ereignis dieser Art an
	 * @param simData	Simulationsdatenobjekt
	 */
	public static void scheduleCheck(final SimulationData simData) {
		final int delta=simData.runModel.timedChecksDelta;
		if (delta<=0) return;
		final TimedCheckEvent checkEvent=(TimedCheckEvent)simData.getEvent(TimedCheckEvent.class);
		checkEvent.init(simData.currentTime+delta);
		simData.eventManager.addEvent(checkEvent);
	}
}
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
package simulator.elements;

import simulator.events.SystemArrivalEvent;
import simulator.runmodel.RunData;
import simulator.runmodel.SimulationData;

/**
 * Alle Quellen m�ssen dieses Interface implementieren.
 * In {@link RunData#initRun(long, SimulationData, boolean)} werden dann alle Quellen initialisiert
 * und das {@link SystemArrivalEvent} benachrichtigt Quellen �ber dieses Interface �ber Ank�nfte.
 * Auch {@link RunData#logStationArrival(long, SimulationData, simulator.coreelements.RunElement, simulator.coreelements.RunElementData, simulator.runmodel.RunDataClient)}
 * identifiziert Quellen �ber dieses Interface und erfasst Ank�nfte an diesen Stationen dann als
 * Systemank�nfte.
 * @author Alexander Herzog
 * @see RunData#initRun(long, SimulationData, boolean)
 * @see SystemArrivalEvent#run(simcore.SimData)
 * @see RunData#logStationArrival(long, SimulationData, simulator.coreelements.RunElement, simulator.coreelements.RunElementData, simulator.runmodel.RunDataClient)
 *
 */
public interface RunSource {
	/**
	 * �ber diese Methode wird die Quelle informiert, dass f�r Sie ein {@link SystemArrivalEvent} aufgetreten ist.
	 * @param simData	Simulationsdatenobjekt
	 * @param scheduleNext	Sollen weitere Ank�nfte eingeplant werden. (Verwendung dieses Parameters ist optional. Es wird hier der Wert {@link SystemArrivalEvent#scheduleNext} durchgereicht.)
	 * @param index	Index des Kundentyps in der Quelle. (Verwendung dieses Parameters ist optional. Es wird hier der Wert {@link SystemArrivalEvent#index} durchgereicht.)
	 */
	void processArrivalEvent(final SimulationData simData, final boolean scheduleNext, final int index);

	/**
	 * Wird zu Beginn von {@link RunData#initRun(long, SimulationData, boolean)} f�r alle Quellen aufgerufen.
	 * @param simData	Simulationsdatenobjekt
	 */
	void scheduleInitialArrivals(final SimulationData simData);
}

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

import java.util.ArrayDeque;
import java.util.Deque;

import simulator.coreelements.RunElement;
import simulator.coreelements.RunElementData;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.SimulationData;

/**
 * Laufzeitdaten eines <code>RunElementPickUp</code>-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementPickUp
 * @see RunElementData
 */
public class RunElementPickUpData extends RunElementData {
	/**
	 * Liste der wartenden Kunden
	 */
	public Deque<RunDataClient> waitingClients;

	/**
	 * Konstruktor der Klasse <code>RunElementPickUpData</code>
	 * @param station	Station zu diesem Datenelement
	 * @param simData	Simulationsdatenobjekt
	 */
	public RunElementPickUpData(final RunElement station, final SimulationData simData) {
		super(station,simData);
		waitingClients=new ArrayDeque<>();
	}
}

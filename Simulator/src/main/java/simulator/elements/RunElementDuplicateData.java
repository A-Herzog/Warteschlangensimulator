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

import simulator.coreelements.RunElement;
import simulator.coreelements.RunElementData;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.SimulationData;

/**
 * Laufzeitdaten eines <code>RunElementDuplicate</code>-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementDuplicate
 * @see RunElementData
 */
public class RunElementDuplicateData extends RunElementData {
	/**
	 * Cache-Array f�r die Verwendung in
	 * {@link RunElementDuplicate#processLeave(simulator.runmodel.SimulationData, RunDataClient)}
	 */
	public final RunDataClient[] newClientsList;

	/**
	 * Konstruktor der Klasse <code>RunElementDuplicateData</code>
	 * @param station	Station f�r die hier Daten vorgehalten werden sollen
	 * @param size	Gr��e des {@link #newClientsList} Arrays
	 * @param simData	Simulationsdatenobjekt
	 */
	public RunElementDuplicateData(final RunElement station, final int size, final SimulationData simData) {
		super(station,simData);
		newClientsList=new RunDataClient[size];
	}
}

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

import simulator.coreelements.RunElementData;

/**
 * Laufzeitdaten eines <code>RunElementDecideByStation</code>-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementDecideByStation
 * @see RunElementData
 */
public class RunElementDecideByStationData extends RunElementData {
	/**
	 * Cache-Array für die Nutzung in
	 * {@link RunElementDecideByStation#processLeave(simulator.runmodel.SimulationData, simulator.runmodel.RunDataClient)}
	 */
	public final int[] values;

	/**
	 * Konstruktor der Klasse <code>RunElementDecideByStationData</code>
	 * @param station	Station für die hier Daten vorgehalten werden sollen
	 * @param valueSize	Größe des {@link #values} Arrays
	 */
	public RunElementDecideByStationData(final RunElementDecideByStation station, final int valueSize) {
		super(station);
		values=new int[valueSize];
	}
}

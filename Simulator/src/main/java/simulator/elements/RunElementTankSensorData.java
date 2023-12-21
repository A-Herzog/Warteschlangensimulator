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
import simulator.runmodel.SimulationData;

/**
 * Laufzeitdaten eines <code>RunElementTankSensor</code>-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementTankSensor
 * @see RunElementData
 */
public class RunElementTankSensorData extends RunElementData {
	/**
	 * Letzter Wert
	 */
	public double lastValue;

	/**
	 * Zeitpunkt der Erfassung von {@link RunElementTankSensorData#lastValue}
	 */
	public long lastTime;

	/**
	 * Konstruktor der Klasse <code>RunElementTankSensorData</code>
	 * @param station	Zu dem Datenobjekt zugeh�riges <code>RunElementSeize</code>-Element
	 * @param simData	Simulationsdatenobjekt
	 */
	public RunElementTankSensorData(final RunElementTankSensor station, final SimulationData simData) {
		super(station,simData);
		lastValue=-1;
		lastTime=-1;
	}
}

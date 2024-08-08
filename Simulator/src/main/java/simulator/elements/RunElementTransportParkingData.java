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
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;

/**
 * Laufzeitdaten eines <code>RunElementTransportParking</code>-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementTransportParking
 * @see RunElementData
 */
public class RunElementTransportParkingData extends RunElementData {
	/**
	 * Anzahl an momentan hier parkenden Transportern.
	 */
	public int count;

	/**
	 * Anzahl an Transportern, die sich momentan zu der Station bewegen.
	 */
	public int moving;

	/**
	 * Priorität zum Anfordern von Transportern
	 */
	public final ExpressionCalc priority;

	/**
	 * Konstruktor der Klasse
	 * @param station	Station zu diesem Datenelement
	 * @param priorityString	Formel zur Berechnung der Priorität zur Anforderung von Transportern
	 * @param variableNames	Systemweite Variablennamen (zum Parsen der Prioritätsformel)
	 * @param simData	Simulationsdatenobjekt
	 */
	public RunElementTransportParkingData(final RunElement station, final String priorityString, final String[] variableNames, final SimulationData simData) {
		super(station,simData);
		count=0;
		moving=0;
		priority=new ExpressionCalc(variableNames,simData.runModel.modelUserFunctions);
		priority.parse(priorityString);
	}
}

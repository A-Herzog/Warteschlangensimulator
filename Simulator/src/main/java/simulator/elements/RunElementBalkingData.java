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
import simulator.simparser.ExpressionMultiEval;

/**
 * Laufzeitdaten eines <code>RunElementBalking</code>-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementBalking
 * @see RunElementData
 */
public class RunElementBalkingData extends RunElementData {
	/**
	 * Zu prüfende Bedingungen
	 */
	public ExpressionMultiEval[] conditions;

	/**
	 * Datenelement der zu prüfenden Bedienstation (zum Auslesen der Anzahl an wartenden Kunden)
	 */
	public RunElementProcessData testStationData;

	/**
	 * Konstruktor der Klasse
	 * @param station	Station zu diesem Datenelement
	 * @param conditions	Bei der Verzweigung von Kunden zu prüfende Bedingungen (zur Umsetzung in ein <code>ExpressionMultiEval</code>-Objekt)
	 * @param variableNames	Liste der global verfügbaren Variablennamen
	 * @param simData	Simulationsdatenobjekt
	 */
	public RunElementBalkingData(final RunElement station, final String[] conditions, final String[] variableNames, final SimulationData simData) {
		super(station,simData);

		this.conditions=new ExpressionMultiEval[conditions.length];
		for (int i=0;i<conditions.length;i++) {
			if (conditions[i]==null || conditions[i].trim().isEmpty()) {
				this.conditions[i]=null;
			} else {
				this.conditions[i]=new ExpressionMultiEval(variableNames);
				this.conditions[i].parse(conditions[i]);
			}
		}
	}
}

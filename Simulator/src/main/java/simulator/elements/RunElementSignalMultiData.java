/**
 * Copyright 2026 Alexander Herzog
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
 * Laufzeitdaten eines {@link RunElementSignalMulti}-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementSignalMulti
 * @see RunElementData
 */
public class RunElementSignalMultiData extends RunElementData {
	/**
	 * Zu prüfende Bedingungen (Einzelne Einträge können <code>null</code> sein)
	 */
	public ExpressionMultiEval[] conditions;

	/**
	 * Konstruktor der Klasse
	 * @param station Station zu diesem Datenelement
	 * @param conditions	Optionale zusätzliche Bedingungen, die für eine Signalauslösung erfüllt sein müssen (Einzelne Einträge können <code>null</code> sein)
	 * @param variableNames	Liste der global verfügbaren Variablennamen
	 * @param simData	Simulationsdatenobjekt
	 */
	public RunElementSignalMultiData(final RunElement station, final String conditions[], final String[] variableNames, final SimulationData simData) {
		super(station,simData);

		this.conditions=new ExpressionMultiEval[conditions.length];

		for (int i=0;i<conditions.length;i++) {
			if (conditions[i]==null || conditions[i].isBlank()) {
				this.conditions[i]=null;
			} else {
				this.conditions[i]=new ExpressionMultiEval(variableNames,simData.runModel.modelUserFunctions);
				this.conditions[i].parse(conditions[i]);
			}
		}
	}
}

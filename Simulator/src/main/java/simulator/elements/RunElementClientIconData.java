/**
 * Copyright 2023 Alexander Herzog
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
 * Laufzeitdaten eines {@link RunElementClientIcon}-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementClientIcon
 * @see RunElementData
 */
public class RunElementClientIconData extends RunElementData {
	/**
	 * Zu prüfende Bedingung (kann <code>null</code> sein)
	 */
	public ExpressionMultiEval condition;

	/**
	 * Konstruktor der Klasse
	 * @param station Station zu diesem Datenelement
	 * @param condition	Optionale zusätzliche Bedingung, die für eine Zuweisung erfüllt sein muss (kann <code>null</code> sein)
	 * @param variableNames	Liste der global verfügbaren Variablennamen
	 * @param simData	Simulationsdatenobjekt
	 */
	public RunElementClientIconData(final RunElement station, final String condition, final String[] variableNames, final SimulationData simData) {
		super(station,simData);

		if (condition==null || condition.trim().isEmpty()) {
			this.condition=null;
		} else {
			this.condition=new ExpressionMultiEval(variableNames);
			this.condition.parse(condition);
		}
	}
}

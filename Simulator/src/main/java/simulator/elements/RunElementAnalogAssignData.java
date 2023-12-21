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
 * Laufzeitdaten eines <code>RunElementAnalogAssign</code>-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementAnalogAssign
 * @see RunElementData
 */
public class RunElementAnalogAssignData extends RunElementData {
	/**
	 * Zu berechnende Ausdrücke
	 */
	public ExpressionCalc[] expressions;

	/**
	 * Konstruktor der Klasse <code>RunElementAnalogAssignData</code>
	 * @param station	Station zu diesem Datenelement
	 * @param expressions	Zu berechnende Ausdrücke
	 * @param variableNames	Liste der global verfügbaren Variablennamen
	 * @param simData	Simulationsdatenobjekt
	 */
	public RunElementAnalogAssignData(final RunElement station, final String[] expressions, final String[] variableNames, final SimulationData simData) {
		super(station,simData);

		this.expressions=new ExpressionCalc[expressions.length];
		for (int i=0;i<expressions.length;i++) {
			this.expressions[i]=new ExpressionCalc(variableNames);
			this.expressions[i].parse(expressions[i]);
		}
	}
}
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
 * Laufzeitdaten eines <code>RunElementRelease</code>-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementRelease
 * @see RunElementData
 */
public class RunElementReleaseData extends RunElementData {
	/**
	 * Rechenausdrücke für die verzögerte Ressourcenfreigabe
	 */
	public final ExpressionCalc[] delayExpression;

	/**
	 * Konstruktor der Klasse <code>RunElementReleaseData</code>
	 * @param station	Station zu diesem Datenelement
	 * @param delayExpressionStrings	Strings, die die Verzögerungsausdrücke (können jeweils <code>null</code> sein) repräsentieren
	 * @param variableNames	Liste der global verfügbaren Variablennamen
	 * @param simData	Simulationsdatenobjekt
	 */
	public RunElementReleaseData(final RunElement station, final String[] delayExpressionStrings, final String[] variableNames, final SimulationData simData) {
		super(station,simData);

		delayExpression=new ExpressionCalc[delayExpressionStrings.length];
		for (int i=0;i<delayExpression.length;i++) if (delayExpressionStrings[i]!=null) {
			delayExpression[i]=new ExpressionCalc(variableNames,simData.runModel.modelUserFunctions);
			delayExpression[i].parse(delayExpressionStrings[i]);
		}
	}
}
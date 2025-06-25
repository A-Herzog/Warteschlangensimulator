/**
 * Copyright 2025 Alexander Herzog
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
 * Laufzeitdaten eines {@link RunElementAssignMultiByCondition}-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementAssignMultiByCondition
 * @see RunElementData
 */
public class RunElementAssignMultiByConditionData extends RunElementAssignMultiBaseData {
	/** Für die verschiedenen Pfade zu prüfenden Bedingungen */
	public ExpressionMultiEval[] conditions;

	/**
	 * Konstruktor der Klasse
	 * @param station	Station zu diesem Datenelement
	 * @param condition	Optionale zusätzliche Bedingung, die für eine Zuweisung erfüllt sein muss (kann <code>null</code> sein)	 *
	 * @param variableNames	Liste der global verfügbaren Variablennamen
	 * @param simData	Simulationsdatenobjekt
	 * @param conditions	Bedingungen gemäß denen die Aufteilung der Kunden erfolgt
	 */
	public RunElementAssignMultiByConditionData(final RunElement station, final String condition, final String[] variableNames, final SimulationData simData, final String[] conditions) {
		super(station,condition,variableNames,simData);

		this.conditions=new ExpressionMultiEval[conditions.length];
		for (int i=0;i<conditions.length;i++) if (conditions[i]!=null) {
			ExpressionMultiEval eval=new ExpressionMultiEval(variableNames,simData.runModel.modelUserFunctions);
			eval.parse(conditions[i]);
			this.conditions[i]=eval;
		}
	}
}

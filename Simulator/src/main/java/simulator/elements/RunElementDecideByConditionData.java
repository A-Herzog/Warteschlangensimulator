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
import simulator.simparser.ExpressionMultiEval;

/**
 * Laufzeitdaten eines <code>RunElementDecideByCondition</code>-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementDecideByCondition
 * @see RunElementData
 */
public class RunElementDecideByConditionData extends RunElementData {
	/** F�r die verschiedenen Pfade zu pr�fenden Bedingungen */
	public ExpressionMultiEval[] conditions;

	/**
	 * Konstruktor der Klasse <code>RunElementDecideByConditionData</code>
	 * @param station	Station zu diesem Datenelement
	 * @param conditions	Bedingungen gem�� denen die Aufteilung der Kunden erfolgt
	 * @param variableNames	Liste der global verf�gbaren Variablennamen
	 */
	public RunElementDecideByConditionData(final RunElement station, final String[] conditions, final String[] variableNames) {
		super(station);

		this.conditions=new ExpressionMultiEval[conditions.length];
		for (int i=0;i<conditions.length;i++) {
			ExpressionMultiEval eval=new ExpressionMultiEval(variableNames);
			eval.parse(conditions[i]);
			this.conditions[i]=eval;
		}
	}
}

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
import simulator.runmodel.RunModel;
import simulator.simparser.ExpressionCalc;
import simulator.simparser.ExpressionMultiEval;
import ui.modeleditor.elements.ModelElementSetRecord;

/**
 * Laufzeitdaten eines <code>RunElementSet</code>-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementSet
 * @see RunElementData
 */
public class RunElementSetData extends RunElementData {
	/**
	 * Wie ist der neue Wert der Variable zu bestimmen?
	 * @author Alexander Herzog
	 * @see RunElementSetData#mode
	 */
	public enum SetMode {
		/** Rechenausdruck ausrechnen */
		MODE_EXPRESSION,
		/** Bisherige Wartezeit des aktuellen Kunden */
		MODE_WAITING_TIME,
		/** Bisherige Transportzeit des aktuellen Kunden */
		MODE_TRANSFER_TIME,
		/** Bisherige Bedienzeit des aktuellen Kunden */
		MODE_PROCESS_TIME,
		/** Bisherige Verweilzeit des aktuellen Kunden */
		MODE_RESIDENCE_TIME
	}

	/**
	 * Ausdrücke, die die neuen Werte für die Variablen beinhalten
	 */
	public ExpressionCalc[] expressions;

	/**
	 * Wie ist der neue Wert der Variable zu bestimmen?
	 * @see SetMode
	 */
	public SetMode[] mode;

	/**
	 * Zu prüfende Bedingung (kann <code>null</code> sein)
	 */
	public ExpressionMultiEval condition;

	/**
	 * Konstruktor der Klasse <code>RunElementSetData</code>
	 * @param station	Zu dem Datenobjekt zugehöriges <code>RunElementSet</code>-Element
	 * @param expressions	Auszuwertende Ausdrücke als String
	 * @param condition	Optionale zusätzliche Bedingung, die für eine Zuweisung erfüllt sein muss (kann <code>null</code> sein)
	 * @param runModel	Laufzeitmodell, dem u.a. die Variablennamen entnommen werden
	 */
	public RunElementSetData(final RunElement station, final String[] expressions, final String condition, final RunModel runModel) {
		super(station);

		this.expressions=new ExpressionCalc[expressions.length];
		this.mode=new SetMode[expressions.length];
		for (int i=0;i<expressions.length;i++) {
			mode[i]=SetMode.MODE_EXPRESSION;
			if (expressions[i].equals(ModelElementSetRecord.SPECIAL_WAITING)) mode[i]=SetMode.MODE_WAITING_TIME;
			if (expressions[i].equals(ModelElementSetRecord.SPECIAL_TRANSFER)) mode[i]=SetMode.MODE_TRANSFER_TIME;
			if (expressions[i].equals(ModelElementSetRecord.SPECIAL_PROCESS)) mode[i]=SetMode.MODE_PROCESS_TIME;
			if (expressions[i].equals(ModelElementSetRecord.SPECIAL_RESIDENCE)) mode[i]=SetMode.MODE_RESIDENCE_TIME;
			if (mode[i]==SetMode.MODE_EXPRESSION) {
				this.expressions[i]=new ExpressionCalc(runModel.variableNames);
				this.expressions[i].parse(expressions[i]);
			}
		}

		if (condition==null || condition.trim().isEmpty()) {
			this.condition=null;
		} else {
			this.condition=new ExpressionMultiEval(runModel.variableNames);
			this.condition.parse(condition);
		}
	}
}
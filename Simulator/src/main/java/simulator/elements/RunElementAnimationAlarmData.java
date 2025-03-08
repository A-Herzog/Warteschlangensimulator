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
 * Laufzeitdaten eines {@link RunElementAnimationAlarm}-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementAnimationAlarm
 * @see RunElementData
 */
public class RunElementAnimationAlarmData extends RunElementData {
	/**
	 * Zu prüfende Bedingung (kann <code>null</code> sein)
	 */
	public ExpressionMultiEval condition;

	/**
	 * Zählung der Ankünfte (und nur Auslösung alle n Ankünfte)
	 */
	public long counter;

	/**
	 * Ist die Sound-Abspiel-Funktion überhaupt noch aktiv?
	 */
	public boolean active;

	/**
	 * Konstruktor der Klasse
	 * @param station	Station zu diesem Datenelement
	 * @param condition	Optionale Bedingung für die Auslösung der Sound-Ausgabe
	 * @param variableNames	Liste der global verfügbaren Variablennamen
	 * @param simData	Simulationsdatenobjekt
	 */
	public RunElementAnimationAlarmData(final RunElement station, final String condition, final String[] variableNames, final SimulationData simData) {
		super(station,simData);

		if (condition==null || condition.isBlank()) {
			this.condition=null;
		} else {
			this.condition=new ExpressionMultiEval(variableNames,simData.runModel.modelUserFunctions);
			this.condition.parse(condition);
		}

		counter=0;
		active=true;
	}
}

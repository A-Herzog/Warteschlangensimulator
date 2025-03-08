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
import statistics.StatisticsDataCollector;

/**
 * Laufzeitdaten eines <code>RunElementRecord</code>-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementRecord
 * @see RunElementData
 */
public class RunElementRecordData extends RunElementData {

	/**
	 * Ausdruck 1 (ist nicht <code>null</code>)
	 */
	public final ExpressionCalc expression1;

	/**
	 * Ausdruck 2 (kann <code>null</code> sein)
	 */
	public final ExpressionCalc expression2;

	/**
	 * Datenaufzeichnungsobjekt für Ausdruck 1<br>
	 * Wird von {@link RunElementRecord#processArrival(simulator.runmodel.SimulationData, simulator.runmodel.RunDataClient)} beim ersten Aufruf mit Wert belegt
	 */
	public StatisticsDataCollector statistics1;

	/**
	 * Datenaufzeichnungsobjekt für Ausdruck 2<br>
	 * Wird von {@link RunElementRecord#processArrival(simulator.runmodel.SimulationData, simulator.runmodel.RunDataClient)} beim ersten Aufruf mit Wert belegt
	 */
	public StatisticsDataCollector statistics2;

	/**
	 * Vermerkt, ob bereits eine Warnung, dass nicht alle Werte erfasst werden, zu der Liste der Warnungen hinzugefügt wurde.
	 */
	public boolean warningDisplayed;

	/**
	 * Konstruktor der Klasse <code>RunElementRecordData</code>
	 * @param station	Station zu diesem Datenelement
	 * @param expression1	Ausdruck 1
	 * @param expression2	Ausdruck 2
	 * @param variableNames	Liste der global verfügbaren Variablennamen
	 * @param simData	Simulationsdatenobjekt
	 */
	public RunElementRecordData(final RunElement station, final String expression1, final String expression2, final String[] variableNames, final SimulationData simData) {
		super(station,simData);

		this.expression1=new ExpressionCalc(variableNames,simData.runModel.modelUserFunctions);
		this.expression1.parse(expression1);

		if (expression2==null || expression2.isBlank()) {
			this.expression2=null;
		} else {
			this.expression2=new ExpressionCalc(variableNames,simData.runModel.modelUserFunctions);
			this.expression2.parse(expression2);
		}

		statistics1=null;
		statistics2=null;
		warningDisplayed=false;
	}
}
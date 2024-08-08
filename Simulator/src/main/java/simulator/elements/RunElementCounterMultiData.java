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
import statistics.StatisticsMultiPerformanceIndicator;
import statistics.StatisticsSimpleCountPerformanceIndicator;

/**
 * Laufzeitdaten eines <code>RunElementCounterMulti</code>-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementCounter
 * @see RunElementData
 */
public class RunElementCounterMultiData extends RunElementData {
	/** Bedingungen für die Mehrfachzähler */
	public final ExpressionMultiEval[] conditions;
	/** Statistikobjekte der Mehrfachzähler */
	public final StatisticsSimpleCountPerformanceIndicator[] statistic;
	/** Statistikobjekt des Mehrfachzählers, der verwendet werden soll, wenn keine der Bedingungen zutrifft */
	public final StatisticsSimpleCountPerformanceIndicator statisticElse;

	/**
	 * Konstruktor der Klasse <code>RunElementCounterData</code>
	 * @param station	Station zu diesem Datenelement
	 * @param groupName	Name der Zählergruppe für die einzelnen Zähler
	 * @param conditions	Bedingungen zur Aktivierung der jeweiligen Zähler
	 * @param counterNames	Namen der Zähler
	 * @param counterNameElse	Name des Zählers für den Fall, dass keine Bedingung erfüllt ist
	 * @param counterStatistic	Statistikobjekt welches alle Zähler enthält
	 * @param variableNames	Liste der global verfügbaren Variablennamen
	 * @param simData	Simulationsdatenobjekt
	 */
	public RunElementCounterMultiData(final RunElement station, final String groupName, final String[] conditions, final String[] counterNames, final String counterNameElse, final StatisticsMultiPerformanceIndicator counterStatistic, final String[] variableNames, final SimulationData simData) {
		super(station,simData);

		final String groupNameExt=groupName.replace('-','_')+"-";
		this.conditions=new ExpressionMultiEval[conditions.length];
		statistic=new StatisticsSimpleCountPerformanceIndicator[conditions.length];

		for (int i=0;i<conditions.length;i++) {
			this.conditions[i]=new ExpressionMultiEval(variableNames,simData.runModel.modelUserFunctions);
			this.conditions[i].parse(conditions[i]);
			statistic[i]=(StatisticsSimpleCountPerformanceIndicator)counterStatistic.get(groupNameExt+counterNames[i]);
		}
		statisticElse=(StatisticsSimpleCountPerformanceIndicator)counterStatistic.get(groupNameExt+counterNameElse);
	}
}

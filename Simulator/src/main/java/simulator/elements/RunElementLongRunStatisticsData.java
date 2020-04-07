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

import language.Language;
import simulator.coreelements.RunElement;
import simulator.coreelements.RunElementData;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import statistics.StatisticsLongRunPerformanceIndicator;

/**
 * Laufzeitdaten eines <code>RunElementSpecialStatistics</code>-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementLongRunStatistics
 * @see RunElementData
 */
public class RunElementLongRunStatisticsData extends RunElementData {
	private final String[] expressionStrings;
	private final ExpressionCalc[] expressions;
	private final StatisticsLongRunPerformanceIndicator[] expressionStatistics;
	private final StatisticsLongRunPerformanceIndicator.Mode[] expressionMode;
	private final long[] lastTime;
	private final double[] lastValue;
	private final long stepWide;

	/**
	 * Konstruktor der Klasse <code>RunElementSpecialStatisticsData</code>
	 * @param station	Zu dem Datenobjekt zugehöriges <code>RunElementSpecialStatistics</code>-Element
	 * @param expresions	Auszuwertende Ausdrücke als String
	 * @param modes	Modi für die Ausdrücke (<code>StatisticsLongRunPerformanceIndicator.MODE_*</code>)
	 * @param stepWide	Schrittweite für die Datenerfassung (in Millisekunden)
	 * @param runModel	Laufzeitmodell, dem u.a. die Variablennamen entnommen werden
	 */
	public RunElementLongRunStatisticsData(final RunElement station, final String[] expresions, final StatisticsLongRunPerformanceIndicator.Mode[] modes, final long stepWide, final RunModel runModel) {
		super(station);
		expressionStrings=new String[expresions.length];
		this.expressions=new ExpressionCalc[expresions.length];
		expressionStatistics=new StatisticsLongRunPerformanceIndicator[expresions.length];
		expressionMode=new StatisticsLongRunPerformanceIndicator.Mode[expresions.length];
		lastTime=new long[expresions.length];
		lastValue=new double[expresions.length];
		for (int i=0;i<expresions.length;i++) {
			expressionStrings[i]=expresions[i];
			this.expressions[i]=new ExpressionCalc(runModel.variableNames);
			this.expressions[i].parse(expresions[i]);
			expressionMode[i]=modes[i];
			switch (expressionMode[i]) {
			case MODE_AVERAGE:
				expressionStrings[i]=expressionStrings[i]+" "+Language.tr("Statistics.Average");
				break;
			case MODE_MIN:
				expressionStrings[i]=expressionStrings[i]+" "+Language.tr("Statistics.Minimum");
				break;
			case MODE_MAX:
				expressionStrings[i]=expressionStrings[i]+" "+Language.tr("Statistics.Maximum");
				break;
			}
			lastValue[i]=Double.MAX_VALUE;
			lastTime[i]=-Long.MAX_VALUE;
		}
		this.stepWide=stepWide;
	}

	/**
	 * Führt einen Statistik-Erfassungsschritt aus
	 * @param simData	Simulationsdaten
	 */
	public void process(final SimulationData simData) {
		final long time=simData.currentTime;
		final double[] variableValues=simData.runData.variableValues;
		for (int i=0;i<expressions.length;i++) {
			final double value;
			if (simData.runModel.stoppOnCalcError) {
				final Double D=expressions[i].calc(variableValues,simData,null);
				if (D==null) simData.calculationErrorStation(expressions[i],this);
				value=(D==null)?0.0:D.doubleValue();
			} else {
				value=expressions[i].calcOrDefault(variableValues,simData,null,0);
			}
			if (value==lastValue[i] && time==lastTime[i]) continue;
			lastValue[i]=value;
			lastTime[i]=time;
			if (expressionStatistics[i]==null) {
				expressionStatistics[i]=(StatisticsLongRunPerformanceIndicator)(simData.statistics.longRunStatistics.get(expressionStrings[i]));
				expressionStatistics[i].init(stepWide,expressionMode[i]);
			}
			expressionStatistics[i].set(time,value);
		}
	}
}
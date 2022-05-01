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
import parser.MathCalcError;
import simulator.coreelements.RunElement;
import simulator.coreelements.RunElementData;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import statistics.StatisticsLongRunPerformanceIndicator;

/**
 * Laufzeitdaten eines {@link RunElementLongRunStatistics}-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementLongRunStatistics
 * @see RunElementData
 */
public class RunElementLongRunStatisticsData extends RunElementData {
	/**
	 * Statistikbezeichner für die Erfassung der Ausdrücke aus
	 * {@link RunElementLongRunStatistics#expressions}
	 */
	private final String[] expressionStrings;

	/**
	 * Rechenobjekte der Ausdrücke aus
	 * {@link RunElementLongRunStatistics#expressions}
	 */
	private final ExpressionCalc[] expressions;

	/**
	 * Zugehörige Statistikobjekte für die Erfassung der Werte in {@link #expressions}
	 */
	private final StatisticsLongRunPerformanceIndicator[] expressionStatistics;

	/**
	 * Modi für die Erfassung der einzelnen Werte in {@link #expressions}
	 */
	private final StatisticsLongRunPerformanceIndicator.Mode[] expressionMode;

	/**
	 * Letzte Zeitpunkte an denen die Ausdrücke {@link #expressions} ausgewertet wurden
	 */
	private final long[] lastTime;

	/**
	 * Letzte Werte bei der Auswertung von {@link #expressions}
	 */
	private final double[] lastValue;

	/**
	 * Schrittweite für die Datenerfassung (in Millisekunden)
	 */
	private final long stepWideMS;

	/**
	 * Sollen zum Simulationsende letzte Intervalle abgeschlossen werden?
	 */
	private final boolean closeLastInterval;

	/**
	 * Konstruktor der Klasse {@link RunElementLongRunStatisticsData}
	 * @param station	Zu dem Datenobjekt zugehöriges <code>RunElementSpecialStatistics</code>-Element
	 * @param expresions	Auszuwertende Ausdrücke als String
	 * @param modes	Modi für die Ausdrücke (<code>StatisticsLongRunPerformanceIndicator.MODE_*</code>)
	 * @param stepWideMS	Schrittweite für die Datenerfassung (in Millisekunden)
	 * @param closeLastInterval	Sollen zum Simulationsende letzte Intervalle abgeschlossen werden?
	 * @param runModel	Laufzeitmodell, dem u.a. die Variablennamen entnommen werden
	 */
	public RunElementLongRunStatisticsData(final RunElement station, final String[] expresions, final StatisticsLongRunPerformanceIndicator.Mode[] modes, final long stepWideMS, final boolean closeLastInterval, final RunModel runModel) {
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
		this.stepWideMS=stepWideMS;
		this.closeLastInterval=closeLastInterval;
	}

	/**
	 * Führt einen Statistik-Erfassungsschritt aus.
	 * @param simData	Simulationsdaten
	 * @param time	Als aktuelle Zeit zu verwendender Wert (der Wert aus den Simulationsdaten wird ignoriert)
	 */
	private void processInt(final SimulationData simData, final long time) {
		final double[] variableValues=simData.runData.variableValues;
		for (int i=0;i<expressions.length;i++) {
			double value;
			try {
				value=expressions[i].calc(variableValues,simData,null);
			} catch (MathCalcError e) {
				simData.calculationErrorStation(expressions[i],this);
				value=0.0;
			}
			if (value==lastValue[i] && time==lastTime[i]) continue;
			lastValue[i]=value;
			lastTime[i]=time;
			if (expressionStatistics[i]==null) {
				expressionStatistics[i]=(StatisticsLongRunPerformanceIndicator)(simData.statistics.longRunStatistics.get(expressionStrings[i]));
				expressionStatistics[i].init(stepWideMS,expressionMode[i]);
			}
			expressionStatistics[i].set(time,value);
		}
	}

	/**
	 * Führt einen Statistik-Erfassungsschritt aus.
	 * @param simData	Simulationsdaten
	 */
	public void process(final SimulationData simData) {
		processInt(simData,simData.currentTime);
	}

	/**
	 * Erfasst zum Simulationende letztmalig die Veränderungen.
	 * @param simData	Simulationsdaten
	 */
	public void doneStatistics(final SimulationData simData) {
		long time=simData.currentTime;
		if (closeLastInterval && simData.currentTime%stepWideMS!=0) time=((simData.currentTime/stepWideMS)+1)*stepWideMS;
		processInt(simData,time);
	}
}
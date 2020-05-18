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

import parser.MathCalcError;
import simulator.coreelements.RunElement;
import simulator.coreelements.RunElementData;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import statistics.StatisticsDataPerformanceIndicator;

/**
 * Laufzeitdaten eines <code>RunElementUserStatistic</code>-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementUserStatistic
 * @see RunElementData
 */
public class RunElementUserStatisticData extends RunElementData {
	private final String[] keys;
	private final boolean[] isTime;
	private final ExpressionCalc[] expressions;
	private final StatisticsDataPerformanceIndicator[] indicators;

	/**
	 * Konstruktor der Klasse <code>RunElementUserStatisticData</code>
	 * @param station	Station zu diesem Datenelement
	 * @param keys	Array der Nutzerdaten-Statistik-Bezeichner unter denen die Werte erfasst werden sollen
	 * @param isTime	Array der Angaben, ob die Nutzerdaten Zeitangaben sind oder nicht
	 * @param expressions	Array der Ausdrücke die ausgewertet und in der Nutzerdaten-Statistik erfasst werden sollen
	 * @param variableNames	Liste der global verfügbaren Variablennamen
	 */
	public RunElementUserStatisticData(final RunElement station, final String[] keys, final boolean[] isTime, final String[] expressions, final String[] variableNames) {
		super(station);

		this.keys=keys;
		this.isTime=isTime;
		this.expressions=new ExpressionCalc[expressions.length];
		for (int i=0;i<expressions.length;i++) {
			this.expressions[i]=new ExpressionCalc(variableNames);
			this.expressions[i].parse(expressions[i]);
		}
		indicators=new StatisticsDataPerformanceIndicator[expressions.length];
	}

	/**
	 * Erfasst die Nutzerdaten-Statistik für einen Kunden, der das Element passiert
	 * @param simData	Simulationsdatenobjekt
	 * @param client	Kunden, der die Verarbeitung ausgelöst hat
	 */
	public void processClient(final SimulationData simData, final RunDataClient client) {
		simData.runData.setClientVariableValues(client);

		for (int i=0;i<keys.length;i++) {
			/* Wert berechnen */
			double value;
			try {
				value=expressions[i].calc(simData.runData.variableValues,simData,client);
			} catch (MathCalcError e) {
				simData.calculationErrorStation(expressions[i],this);
				value=0;
			}

			/* Indikator holen wenn nötig */
			if (indicators[i]==null) indicators[i]=(StatisticsDataPerformanceIndicator)simData.statistics.userStatistics.get(keys[i]);

			/* Wert eintragen */
			indicators[i].add(value);
		}
	}

	/**
	 * Liefert eine Liste der Bezeichner unter deren Namen Nutzerdaten-Statistiken erfasst werden
	 * @return	Liste der Bezeichner
	 */
	public String[] getKeys() {
		return keys;
	}

	/**
	 * Liefert eine Liste der Angaben, ob ein Nutzerdaten-Statistik-Eintrag eine Zeitangabe ist oder nicht
	 * @return	Liste mit den Angaben, ob die Einträge Zeiten sind
	 */
	public boolean[] getIsTime() {
		return isTime;
	}

	/**
	 * Liefert eine Liste der Statistikobjekte, in die Nutzerdaten-Statistikdaten eingetragen werden (einzelne Einträge können <code>null</code> sein, wenn noch keine Datum erfasst wurde)
	 * @return	Liste der Statistikobjekte
	 */
	public StatisticsDataPerformanceIndicator[] getIndicators() {
		return indicators;
	}
}

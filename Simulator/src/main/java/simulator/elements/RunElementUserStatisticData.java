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

import java.util.ArrayList;
import java.util.List;

import parser.MathCalcError;
import simulator.coreelements.RunElement;
import simulator.coreelements.RunElementData;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import statistics.StatisticsDataPerformanceIndicatorWithNegativeValues;
import statistics.StatisticsPerformanceIndicator;
import statistics.StatisticsTimeContinuousPerformanceIndicator;

/**
 * Laufzeitdaten eines <code>RunElementUserStatistic</code>-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementUserStatistic
 * @see RunElementData
 */
public class RunElementUserStatisticData extends RunElementData {
	/** Diskrete Werte: Array der Nutzerdaten-Statistik-Bezeichner unter denen die Werte erfasst werden sollen */
	private final String[] keysDiscrete;
	/** Diskrete Werte: Array der Angaben, ob die Nutzerdaten Zeitangaben sind oder nicht */
	private final boolean[] isTimeDiscrete;
	/** Diskrete Werte: Array der Ausdrücke die ausgewertet und in der Nutzerdaten-Statistik erfasst werden sollen */
	private final ExpressionCalc[] expressionsDiscrete;
	/** Diskrete Werte: Index des Eintrags in der Gesamtliste aller Statistik-Bezeichner */
	private int[] indexDiscrete;

	/** Kontinuierliche Werte: Array der Nutzerdaten-Statistik-Bezeichner unter denen die Werte erfasst werden sollen */
	private final String[] keysContinuous;
	/** Kontinuierliche Werte: Array der Angaben, ob die Nutzerdaten Zeitangaben sind oder nicht */
	private final boolean[] isTimeContinuous;
	/** Kontinuierliche Werte: Array der Ausdrücke die ausgewertet und in der Nutzerdaten-Statistik erfasst werden sollen */
	private final ExpressionCalc[] expressionsContinuous;
	/** Kontinuierliche Werte: Index des Eintrags in der Gesamtliste aller Statistik-Bezeichner */
	private int[] indexContinuous;

	/** Statistikobjekte für die verschiedenen Bezeichner {@link #keysDiscrete} (wird von {@link #processClient(SimulationData, RunDataClient)} nach Bedarf gefüllt) */
	private final StatisticsDataPerformanceIndicatorWithNegativeValues[] indicatorsDiscrete;

	/** Statistikobjekte für die verschiedenen Bezeichner {@link #keysContinuous} (wird von {@link #processClient(SimulationData, RunDataClient)} nach Bedarf gefüllt) */
	private final StatisticsTimeContinuousPerformanceIndicator[] indicatorsContinuous;

	/** Statistikobjekte für alle Bezeichner (wird von {@link #processClient(SimulationData, RunDataClient)} nach Bedarf gefüllt) */
	private final StatisticsPerformanceIndicator[] indicatorsAll;

	/**
	 * Konstruktor der Klasse <code>RunElementUserStatisticData</code>
	 * @param station	Station zu diesem Datenelement
	 * @param keys	Array der Nutzerdaten-Statistik-Bezeichner unter denen die Werte erfasst werden sollen
	 * @param isTime	Array der Angaben, ob die Nutzerdaten Zeitangaben sind oder nicht
	 * @param expressions	Array der Ausdrücke die ausgewertet und in der Nutzerdaten-Statistik erfasst werden sollen
	 * @param isContinuous	Array der Angaben, ob die Nutzerdaten diskret oder kontinuierlich erfasst werden sollen
	 * @param variableNames	Liste der global verfügbaren Variablennamen
	 */
	public RunElementUserStatisticData(final RunElement station, final String[] keys, final boolean[] isTime, final String[] expressions, final boolean[] isContinuous, final String[] variableNames) {
		super(station);

		final List<String> keysDiscrete=new ArrayList<>();
		final List<Boolean> isTimeDiscrete=new ArrayList<>();
		final List<ExpressionCalc> expressionsDiscrete=new ArrayList<>();
		final List<Integer> indexDiscrete=new ArrayList<>();

		final List<String> keysContinuous=new ArrayList<>();
		final List<Boolean> isTimeContinuous=new ArrayList<>();
		final List<ExpressionCalc> expressionsContinuous=new ArrayList<>();
		final List<Integer> indexContinuous=new ArrayList<>();

		for (int i=0;i<keys.length;i++) {
			final ExpressionCalc expression=new ExpressionCalc(variableNames);
			expression.parse(expressions[i]);
			if (isContinuous[i]) {
				keysContinuous.add(keys[i]);
				isTimeContinuous.add(isTime[i]);
				expressionsContinuous.add(expression);
				indexContinuous.add(i);
			} else {
				keysDiscrete.add(keys[i]);
				isTimeDiscrete.add(isTime[i]);
				expressionsDiscrete.add(expression);
				indexDiscrete.add(i);
			}
		}

		this.keysDiscrete=keysDiscrete.toArray(new String[0]);
		this.isTimeDiscrete=new boolean[isTimeDiscrete.size()];
		for (int i=0;i<this.isTimeDiscrete.length;i++) this.isTimeDiscrete[i]=isTimeDiscrete.get(i);
		this.expressionsDiscrete=expressionsDiscrete.toArray(new ExpressionCalc[0]);
		this.indexDiscrete=indexDiscrete.stream().mapToInt(I->I.intValue()).toArray();

		this.keysContinuous=keysContinuous.toArray(new String[0]);
		this.isTimeContinuous=new boolean[isTimeContinuous.size()];
		for (int i=0;i<this.isTimeContinuous.length;i++) this.isTimeContinuous[i]=isTimeContinuous.get(i);
		this.expressionsContinuous=expressionsContinuous.toArray(new ExpressionCalc[0]);
		this.indexContinuous=indexContinuous.stream().mapToInt(I->I.intValue()).toArray();

		indicatorsDiscrete=new StatisticsDataPerformanceIndicatorWithNegativeValues[this.expressionsDiscrete.length];

		indicatorsContinuous=new StatisticsTimeContinuousPerformanceIndicator[this.expressionsContinuous.length];

		indicatorsAll=new StatisticsPerformanceIndicator[this.expressionsDiscrete.length+this.expressionsContinuous.length];
	}

	/**
	 * Multiplikativer Umrechnungsfaktor für die Umrechnung von MS in Sekunden
	 */
	private static final double scaleToSec=1/1.000;

	/**
	 * Erfasst die Nutzerdaten-Statistik für einen Kunden, der das Element passiert
	 * @param simData	Simulationsdatenobjekt
	 * @param client	Kunden, der die Verarbeitung ausgelöst hat
	 */
	public void processClient(final SimulationData simData, final RunDataClient client) {
		simData.runData.setClientVariableValues(client);

		/* Diskrete Werte */

		for (int i=0;i<keysDiscrete.length;i++) {
			/* Wert berechnen */
			double value;
			try {
				value=expressionsDiscrete[i].calc(simData.runData.variableValues,simData,client);
			} catch (MathCalcError e) {
				simData.calculationErrorStation(expressionsDiscrete[i],this);
				value=0;
			}

			if (Double.isNaN(value)) continue;

			/* Indikator holen wenn nötig */
			if (indicatorsDiscrete[i]==null) {
				indicatorsDiscrete[i]=(StatisticsDataPerformanceIndicatorWithNegativeValues)simData.statistics.userStatistics.get(keysDiscrete[i]);
				indicatorsAll[indexDiscrete[i]]=indicatorsDiscrete[i];
			}

			/* Wert eintragen */
			indicatorsDiscrete[i].add(value);
		}

		/* Zeitkontinuierliche Werte */

		for (int i=0;i<keysContinuous.length;i++) {
			/* Wert berechnen */
			double value;
			try {
				value=expressionsContinuous[i].calc(simData.runData.variableValues,simData,client);
			} catch (MathCalcError e) {
				simData.calculationErrorStation(expressionsContinuous[i],this);
				value=0;
			}

			if (Double.isNaN(value)) continue;

			/* Indikator holen wenn nötig */
			if (indicatorsContinuous[i]==null) {
				indicatorsContinuous[i]=(StatisticsTimeContinuousPerformanceIndicator)simData.statistics.userStatisticsContinuous.get(keysContinuous[i]);
				indicatorsAll[indexContinuous[i]]=indicatorsContinuous[i];
			}

			/* Wert eintragen */
			indicatorsContinuous[i].set(simData.currentTime*scaleToSec,value);
		}
	}

	/**
	 * Liefert eine Liste der Bezeichner unter deren Namen Nutzerdaten-Statistiken erfasst werden (diskrete Werte)
	 * @return	Liste der Bezeichner
	 */
	public String[] getKeysDiscrete() {
		return keysDiscrete;
	}

	/**
	 * Liefert eine Liste der Angaben, ob ein Nutzerdaten-Statistik-Eintrag eine Zeitangabe ist oder nicht (diskrete Werte)
	 * @return	Liste mit den Angaben, ob die Einträge Zeiten sind
	 */
	public boolean[] getIsTimeDiscrete() {
		return isTimeDiscrete;
	}

	/**
	 * Liefert eine Liste der Statistikobjekte, in die Nutzerdaten-Statistikdaten eingetragen werden (einzelne Einträge können <code>null</code> sein, wenn noch keine Datum erfasst wurde) (diskrete Werte)
	 * @return	Liste der Statistikobjekte
	 */
	public StatisticsDataPerformanceIndicatorWithNegativeValues[] getIndicatorsDiscrete() {
		return indicatorsDiscrete;
	}

	/**
	 * Liefert eine Liste der Bezeichner unter deren Namen Nutzerdaten-Statistiken erfasst werden (zeitkontinuierliche Werte)
	 * @return	Liste der Bezeichner
	 */
	public String[] getKeysContinuous() {
		return keysContinuous;
	}

	/**
	 * Liefert eine Liste der Angaben, ob ein Nutzerdaten-Statistik-Eintrag eine Zeitangabe ist oder nicht (zeitkontinuierliche Werte)
	 * @return	Liste mit den Angaben, ob die Einträge Zeiten sind
	 */
	public boolean[] getIsTimeContinuous() {
		return isTimeContinuous;
	}

	/**
	 * Liefert eine Liste der Statistikobjekte, in die Nutzerdaten-Statistikdaten eingetragen werden (einzelne Einträge können <code>null</code> sein, wenn noch keine Datum erfasst wurde) (zeitkontinuierliche Werte)
	 * @return	Liste der Statistikobjekte
	 */
	public StatisticsTimeContinuousPerformanceIndicator[] getIndicatorsContinuous() {
		return indicatorsContinuous;
	}

	/**
	 * Liefert eine Liste aller Statistikobjekte
	 * @return	Liste der Statistikobjekte
	 */
	public StatisticsPerformanceIndicator[] getAllIndicators() {
		return indicatorsAll;
	}
}

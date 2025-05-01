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
import java.util.Arrays;
import java.util.List;

import parser.MathCalcError;
import simulator.coreelements.RunElement;
import simulator.coreelements.RunElementData;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import statistics.StatisticsDataCollector;
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
	/**
	 * Maximalanzahl an aufzuzeichnenden Intervallen
	 */
	private static final int MAX_INTERVALS=1_000_000;

	/**
	 * Zeitpunkt an dem die Einschwingphase endete
	 * (für die Intervall-basierte Aufzeichnung)
	 */
	public long startTime;

	/**
	 * Sekunden pro Intervall (Werte &le;0 zur Deaktivierung der Intervall-Erfassung)
	 */
	private final double intervalLengthSeconds;

	/** Erfassung der Statistikdaten global über alle Kundentypen hinweg? */
	private final boolean recordModeGlobal;
	/** Erfassung der Statistikdaten pro Kundentyp? */
	private final boolean recordModeClientType;

	/** Liste der Namen der Kundentypen im System */
	private String[] clientTypes;

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

	/** Aktuelles Intervall in dem diskrete Werte über alle Kundentypen hinweg erfasst werden */
	private int lastIntervalIndex;
	/** Anzahl an diskreten Werten über alle Kundentypen hinweg im aktuellen Intervall */
	private long lastIntervalCount;
	/** Summe der diskreten Werten über alle Kundentypen hinweg im aktuellen Intervall */
	private double lastIntervalSum;

	/** Aktuelles Intervall in dem diskrete Werte für die einzelnen Kundentypen erfasst werden */
	private int[] lastIntervalClientTypeIndex;
	/** Anzahl an diskreten Werten für die einzelnen Kundentypen im aktuellen Intervall */
	private long[] lastIntervalClientTypeCount;
	/** Summe der diskreten Werten für die einzelnen Kundentypen im aktuellen Intervall */
	private double[] lastIntervalClientTypeSum;

	/** Statistikobjekte für die verschiedenen Bezeichner {@link #keysDiscrete} (wird von {@link #processClient(SimulationData, RunDataClient)} nach Bedarf gefüllt) */
	private final StatisticsDataPerformanceIndicatorWithNegativeValues[] indicatorsDiscrete;

	/** Statistikobjekte für die verschiedenen intervallabhängigen Anzahl-Bezeichner {@link #keysDiscrete} (wird von {@link #processClient(SimulationData, RunDataClient)} nach Bedarf gefüllt) */
	private final StatisticsDataCollector[] indicatorsDiscreteIntervalCount;

	/** Statistikobjekte für die verschiedenen intervallabhängigen Mittelwert-Bezeichner {@link #keysDiscrete} (wird von {@link #processClient(SimulationData, RunDataClient)} nach Bedarf gefüllt) */
	private final StatisticsDataCollector[] indicatorsDiscreteIntervalMean;

	/** Statistikobjekte für die verschiedenen Bezeichner pro Kundentyp {@link #keysDiscrete} (wird von {@link #processClient(SimulationData, RunDataClient)} nach Bedarf gefüllt) */
	private final StatisticsDataPerformanceIndicatorWithNegativeValues[][] indicatorsDiscreteClientType;

	/** Statistikobjekte für die verschiedenen intervallabhängigen Anzahl-Bezeichner pro Kundentyp {@link #keysDiscrete} (wird von {@link #processClient(SimulationData, RunDataClient)} nach Bedarf gefüllt) */
	private final StatisticsDataCollector[][] indicatorsDiscreteClientTypeIntervalCount;

	/** Statistikobjekte für die verschiedenen intervallabhängigen Mittelwert-Bezeichner pro Kundentyp {@link #keysDiscrete} (wird von {@link #processClient(SimulationData, RunDataClient)} nach Bedarf gefüllt) */
	private final StatisticsDataCollector[][] indicatorsDiscreteClientTypeIntervalMean;

	/** Statistikobjekte für die verschiedenen Bezeichner {@link #keysContinuous} (wird von {@link #processClient(SimulationData, RunDataClient)} nach Bedarf gefüllt) */
	private final StatisticsTimeContinuousPerformanceIndicator[] indicatorsContinuous;

	/** Statistikobjekte für die verschiedenen Bezeichner pro Kundentyp {@link #keysContinuous} (wird von {@link #processClient(SimulationData, RunDataClient)} nach Bedarf gefüllt) */
	private final StatisticsTimeContinuousPerformanceIndicator[][] indicatorsContinuousClientType;

	/** Statistikobjekte für alle Bezeichner (wird von {@link #processClient(SimulationData, RunDataClient)} nach Bedarf gefüllt) */
	private final StatisticsPerformanceIndicator[] indicatorsAll;

	/**
	 * Konstruktor der Klasse <code>RunElementUserStatisticData</code>
	 * @param station	Station zu diesem Datenelement
	 * @param recordModeGlobal	Erfassung der Statistikdaten global über alle Kundentypen hinweg?
	 * @param recordModeClientType	Erfassung der Statistikdaten pro Kundentyp?
	 * @param intervalLengthSeconds	Sekunden pro Intervall (Werte &le;0 zur Deaktivierung der Intervall-Erfassung)
	 * @param keys	Array der Nutzerdaten-Statistik-Bezeichner unter denen die Werte erfasst werden sollen
	 * @param isTime	Array der Angaben, ob die Nutzerdaten Zeitangaben sind oder nicht
	 * @param expressions	Array der Ausdrücke die ausgewertet und in der Nutzerdaten-Statistik erfasst werden sollen
	 * @param isContinuous	Array der Angaben, ob die Nutzerdaten diskret oder kontinuierlich erfasst werden sollen
	 * @param variableNames	Liste der global verfügbaren Variablennamen
	 * @param clientTypes	Liste der Namen der Kundentypen im System
	 * @param simData	Simulationsdatenobjekt
	 */
	public RunElementUserStatisticData(final RunElement station, final boolean recordModeGlobal, final boolean recordModeClientType, final double intervalLengthSeconds, final String[] keys, final boolean[] isTime, final String[] expressions, final boolean[] isContinuous, final String[] variableNames, final String[] clientTypes, final SimulationData simData) {
		super(station,simData);

		startTime=0;
		this.intervalLengthSeconds=intervalLengthSeconds;

		this.clientTypes=clientTypes;

		this.recordModeGlobal=recordModeGlobal;
		this.recordModeClientType=recordModeClientType;

		final List<String> keysDiscrete=new ArrayList<>();
		final List<Boolean> isTimeDiscrete=new ArrayList<>();
		final List<ExpressionCalc> expressionsDiscrete=new ArrayList<>();
		final List<Integer> indexDiscrete=new ArrayList<>();

		final List<String> keysContinuous=new ArrayList<>();
		final List<Boolean> isTimeContinuous=new ArrayList<>();
		final List<ExpressionCalc> expressionsContinuous=new ArrayList<>();
		final List<Integer> indexContinuous=new ArrayList<>();

		for (int i=0;i<keys.length;i++) {
			final ExpressionCalc expression=new ExpressionCalc(variableNames,simData.runModel.modelUserFunctions);
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

		lastIntervalIndex=-1;
		lastIntervalClientTypeIndex=new int[clientTypes.length];
		Arrays.fill(lastIntervalClientTypeIndex,-1);
		lastIntervalClientTypeCount=new long[clientTypes.length];
		lastIntervalClientTypeSum=new double[clientTypes.length];

		this.keysDiscrete=keysDiscrete.toArray(String[]::new);
		this.isTimeDiscrete=new boolean[isTimeDiscrete.size()];
		for (int i=0;i<this.isTimeDiscrete.length;i++) this.isTimeDiscrete[i]=isTimeDiscrete.get(i);
		this.expressionsDiscrete=expressionsDiscrete.toArray(ExpressionCalc[]::new);
		this.indexDiscrete=indexDiscrete.stream().mapToInt(I->I.intValue()).toArray();

		this.keysContinuous=keysContinuous.toArray(String[]::new);
		this.isTimeContinuous=new boolean[isTimeContinuous.size()];
		for (int i=0;i<this.isTimeContinuous.length;i++) this.isTimeContinuous[i]=isTimeContinuous.get(i);
		this.expressionsContinuous=expressionsContinuous.toArray(ExpressionCalc[]::new);
		this.indexContinuous=indexContinuous.stream().mapToInt(I->I.intValue()).toArray();

		indicatorsDiscrete=new StatisticsDataPerformanceIndicatorWithNegativeValues[this.expressionsDiscrete.length];
		indicatorsDiscreteClientType=new StatisticsDataPerformanceIndicatorWithNegativeValues[this.expressionsDiscrete.length][];
		for (int i=0;i<indicatorsDiscreteClientType.length;i++) indicatorsDiscreteClientType[i]=new StatisticsDataPerformanceIndicatorWithNegativeValues[clientTypes.length];

		if (intervalLengthSeconds>0) {
			indicatorsDiscreteIntervalCount=new StatisticsDataCollector[this.expressionsDiscrete.length];
			indicatorsDiscreteClientTypeIntervalCount=new StatisticsDataCollector[this.expressionsDiscrete.length][];
			for (int i=0;i<indicatorsDiscreteClientTypeIntervalCount.length;i++) indicatorsDiscreteClientTypeIntervalCount[i]=new StatisticsDataCollector[clientTypes.length];
			indicatorsDiscreteIntervalMean=new StatisticsDataCollector[this.expressionsDiscrete.length];
			indicatorsDiscreteClientTypeIntervalMean=new StatisticsDataCollector[this.expressionsDiscrete.length][];
			for (int i=0;i<indicatorsDiscreteClientTypeIntervalMean.length;i++) indicatorsDiscreteClientTypeIntervalMean[i]=new StatisticsDataCollector[clientTypes.length];
		} else {
			indicatorsDiscreteIntervalCount=null;
			indicatorsDiscreteIntervalMean=null;
			indicatorsDiscreteClientTypeIntervalCount=null;
			indicatorsDiscreteClientTypeIntervalMean=null;
		}

		indicatorsContinuous=new StatisticsTimeContinuousPerformanceIndicator[this.expressionsContinuous.length];
		indicatorsContinuousClientType=new StatisticsTimeContinuousPerformanceIndicator[this.expressionsContinuous.length][];
		for (int i=0;i<indicatorsContinuousClientType.length;i++) indicatorsContinuousClientType[i]=new StatisticsTimeContinuousPerformanceIndicator[clientTypes.length];

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

		int currentInterval=-1;
		if (intervalLengthSeconds>0) {
			currentInterval=(int)Math.floor((simData.currentTime-startTime)*simData.runModel.scaleToSeconds/intervalLengthSeconds);
		}

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

			/* Erfassung über alle Kundentypen hinweg */
			if (recordModeGlobal) {
				/* Indikator holen wenn nötig */
				if (indicatorsDiscrete[i]==null) {
					indicatorsDiscrete[i]=(StatisticsDataPerformanceIndicatorWithNegativeValues)simData.statistics.userStatistics.get(keysDiscrete[i]);
					indicatorsAll[indexDiscrete[i]]=indicatorsDiscrete[i];
					if (currentInterval>=0) {
						indicatorsDiscreteIntervalCount[i]=(StatisticsDataCollector)simData.statistics.userStatisticsIntervalCount.get(keysDiscrete[i]);
						indicatorsDiscreteIntervalMean[i]=(StatisticsDataCollector)simData.statistics.userStatisticsIntervalMean.get(keysDiscrete[i]);
					}
				}

				/* Wert eintragen */
				indicatorsDiscrete[i].add(value);

				/* Intervall-Wert eintragen */
				if (currentInterval>=0 && currentInterval<=MAX_INTERVALS) {
					if (lastIntervalIndex!=currentInterval) {
						if (lastIntervalIndex>=0) {
							indicatorsDiscreteIntervalCount[i].add(lastIntervalCount);
							indicatorsDiscreteIntervalMean[i].add((lastIntervalCount==0)?0:(lastIntervalSum/lastIntervalCount));
						}
						lastIntervalIndex++;
						while (lastIntervalIndex<currentInterval) {
							indicatorsDiscreteIntervalCount[i].add(0);
							indicatorsDiscreteIntervalMean[i].add(0);
							lastIntervalIndex++;
						}
						lastIntervalCount=0;
						lastIntervalSum=0;
					}
					lastIntervalCount++;
					lastIntervalSum+=value;
				}
			}

			/* Erfassung pro Kundentyp */
			if (recordModeClientType) {
				/* Indikator holen wenn nötig */
				if (indicatorsDiscreteClientType[i][client.type]==null) {
					indicatorsDiscreteClientType[i][client.type]=(StatisticsDataPerformanceIndicatorWithNegativeValues)simData.statistics.userStatistics.get(keysDiscrete[i]+" "+clientTypes[client.type]);
					if (currentInterval>=0) {
						indicatorsDiscreteClientTypeIntervalCount[i][client.type]=(StatisticsDataCollector)simData.statistics.userStatisticsIntervalCount.get(keysDiscrete[i]+" "+clientTypes[client.type]);
						indicatorsDiscreteClientTypeIntervalMean[i][client.type]=(StatisticsDataCollector)simData.statistics.userStatisticsIntervalMean.get(keysDiscrete[i]+" "+clientTypes[client.type]);
					}
				}

				/* Wert eintragen */
				indicatorsDiscreteClientType[i][client.type].add(value);

				/* Intervall-Wert eintragen */
				if (currentInterval>=0 && currentInterval<=MAX_INTERVALS) {
					if (lastIntervalClientTypeIndex[client.type]!=currentInterval) {
						if (lastIntervalClientTypeIndex[client.type]>=0) {
							indicatorsDiscreteClientTypeIntervalCount[i][client.type].add(lastIntervalClientTypeCount[client.type]);
							indicatorsDiscreteClientTypeIntervalMean[i][client.type].add((lastIntervalClientTypeCount[client.type]==0)?0:(lastIntervalClientTypeSum[client.type]/lastIntervalClientTypeCount[client.type]));
						}
						lastIntervalClientTypeIndex[client.type]++;
						while (lastIntervalClientTypeIndex[client.type]<currentInterval) {
							indicatorsDiscreteClientTypeIntervalCount[i][client.type].add(0);
							indicatorsDiscreteClientTypeIntervalMean[i][client.type].add(0);
							lastIntervalClientTypeIndex[client.type]++;
						}
						lastIntervalClientTypeCount[client.type]=0;
						lastIntervalClientTypeSum[client.type]=0;
					}
					lastIntervalClientTypeCount[client.type]++;
					lastIntervalClientTypeSum[client.type]+=value;
				}
			}
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

			/* Erfassung über alle Kundentypen hinweg */
			if (recordModeGlobal) {
				/* Indikator holen wenn nötig */
				if (indicatorsContinuous[i]==null) {
					indicatorsContinuous[i]=(StatisticsTimeContinuousPerformanceIndicator)simData.statistics.userStatisticsContinuous.get(keysContinuous[i]);
					indicatorsAll[indexContinuous[i]]=indicatorsContinuous[i];
				}

				/* Wert eintragen */
				indicatorsContinuous[i].set(simData.currentTime*scaleToSec,value);
			}

			/* Erfassung pro Kundentyp */
			if (recordModeClientType) {
				/* Indikator holen wenn nötig */
				if (indicatorsContinuousClientType[i][client.type]==null) {
					indicatorsContinuousClientType[i][client.type]=(StatisticsTimeContinuousPerformanceIndicator)simData.statistics.userStatisticsContinuous.get(keysContinuous[i]+" "+clientTypes[client.type]);
				}

				/* Wert eintragen */
				indicatorsContinuousClientType[i][client.type].set(simData.currentTime*scaleToSec,value);
			}
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

/**
 * Copyright 2021 Alexander Herzog
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
import simulator.coreelements.RunElementData;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import simulator.simparser.ExpressionMultiEval;

/**
 * Diese Klasse h�lt die Thread-lokalen Laufzeitdaten zu einem
 * Ankunftsdatensatz {@link RunElementSourceRecord} vor.
 * @author Alexander Herzog
 * @see RunElementSourceRecord
 */
public class RunElementSourceRecordData {
	/** Z�hlung der Anzahl an Ank�nften (k�nnen ganze Batche sein) an dieser Station */
	public long arrivalCount;
	/** Z�hlung der Anzahl an eingetroffenen Kunden (ggf. Z�hlung in den Batchen) an dieser Station */
	public long arrivalClientCount;
	/** Zeitpunkt der letzten Kundenankunft (in Millisekunden) */
	public long arrivalTime;

	/** Ausdruck zur Berechnung der Batch-Gr��en (kann <code>null</code> sein) */
	public final ExpressionCalc batchSize;
	/** Zwischenankunftszeiten-Ausdruck (kann <code>null</code> sein) */
	public final ExpressionCalc expression;
	/** Bedingung zur Kundenfreigabe  (kann <code>null</code> sein) */
	public final ExpressionMultiEval condition;
	/** Minimaler Abstand zwischen zwei Ank�nften (kann <code>null</code> sein) */
	private final ExpressionCalc conditionMinDistance;
	/** Aktueller gem�� {@link #conditionMinDistance} berechneter minimaler Abstand zwischen zwei Ank�nften (in MS) (ist -1, wenn momentan kein g�ltiger Wert vorliegt) */
	private long conditionMinDistanceCalculatedMS;
	/** Ist {@link #conditionMinDistance} ein konstanter Wert? Dann muss {@link #conditionMinDistanceCalculatedMS} nie verworfen und neu berechnet werden. */
	private boolean conditionMinDistanceIsConst;

	/** Ausdr�cke f�r die intervallabh�ngigen Anzahlen an Ank�nften */
	public final ExpressionCalc[] intervalExpressions;
	/** Ausdr�cke f�r die intervallabh�ngigen Zwischenankunftszeiten */
	public final ExpressionCalc[] intervalDistributions;
	/** N�chster zu verwendender Eintrag aus {@link RunElementSourceRecord#arrivalStream} */
	public int arrivalTimeValueNext;

	/** Rechenausdruck f�r den Schwellenwert zur Erzeugung von Ank�nften */
	private final ExpressionCalc threshold;
	/** Vergleichswert f�r den Schwellenwert-Ausdruck {@link #threshold} */
	private final double thresholdValue;
	/** Soll eine Ankunft beim �ber- oder Unterschrreiten des Schwellenwerts ausgel�st werden? */
	private final boolean thresholdIsDirectionUp;
	/** Letzter Wert des Schwellenwert-Ausdrucks */
	private double thresholdLastValue;
	/** Enth�lt {@link #thresholdLastValue} einen g�ltigen Wert (<code>true</code>) oder wurde noch kein Wert aufgezeichnet (<code>false</code>)? */
	private boolean thresholdIsLastValueAvailable;

	/** Zus�tzliche Bedingung (Voraussetzung), damit eine Ankunft tats�chlich ausgef�hrt wird (kann <code>null</code> sein) */
	public final ExpressionMultiEval arrivalCondition;

	/** Ausdr�cke f�r Zuweisungen von Zahlen-Eigenschaften zu neuen Kunden */
	public final RunElementSourceRecord.SourceSetExpressions setData;

	/**
	 * Konstruktor der Klasse <code>RunElementSourceData</code>
	 * @param simData	Simulationsdatenobjekt
	 * @param record	Modellweit g�ltiger (nicht Thread-lokaler) Ankunftsdatensatz
	 * @param variableNames	Liste der global verf�gbaren Variablennamen
	 */
	public RunElementSourceRecordData(final SimulationData simData, final RunElementSourceRecord record, final String[] variableNames) {
		arrivalCount=0;
		arrivalClientCount=0;
		arrivalTime=-1;

		if (record.batchSize==null) {
			this.batchSize=null;
		} else {
			this.batchSize=new ExpressionCalc(variableNames);
			this.batchSize.parse(record.batchSize);
		}

		if (record.expression==null) {
			this.expression=null;
		} else {
			this.expression=new ExpressionCalc(variableNames);
			this.expression.parse(record.expression);
		}

		if (record.condition==null) {
			this.condition=null;
			this.conditionMinDistance=null;
			this.conditionMinDistanceCalculatedMS=-1;
		} else {
			this.condition=new ExpressionMultiEval(variableNames);
			this.condition.parse(record.condition);
			this.conditionMinDistance=new ExpressionCalc(variableNames);
			this.conditionMinDistance.parse(record.conditionMinDistance);
			this.conditionMinDistanceIsConst=this.conditionMinDistance.isConstValue();
			this.conditionMinDistanceCalculatedMS=-1;
		}

		if (record.intervalExpressions==null) {
			this.intervalExpressions=null;
		} else {
			this.intervalExpressions=new ExpressionCalc[record.intervalExpressions.length];
			for (int i=0;i<record.intervalExpressions.length;i++) {
				this.intervalExpressions[i]=new ExpressionCalc(variableNames);
				this.intervalExpressions[i].parse(record.intervalExpressions[i]);
			}
		}

		if (record.intervalDistributions==null) {
			this.intervalDistributions=null;
		} else {
			this.intervalDistributions=new ExpressionCalc[record.intervalDistributions.length];
			for (int i=0;i<record.intervalDistributions.length;i++) {
				this.intervalDistributions[i]=new ExpressionCalc(variableNames);
				this.intervalDistributions[i].parse(record.intervalDistributions[i]);
			}
		}

		if (record.thresholdExpression==null) {
			this.threshold=null;
		} else {
			this.threshold=new ExpressionCalc(variableNames);
			this.threshold.parse(record.thresholdExpression);
		}
		this.thresholdValue=record.thresholdValue;
		this.thresholdIsDirectionUp=record.thresholdDirectionUp;

		if (record.arrivalCondition==null) {
			this.arrivalCondition=null;
		} else {
			this.arrivalCondition=new ExpressionMultiEval(variableNames);
			this.arrivalCondition.parse(record.arrivalCondition);
		}

		this.setData=record.getRuntimeExpressions(simData.runModel.variableNames);

		arrivalTimeValueNext=0;
	}

	/**
	 * Liefert die minimale zul�ssig Zwischenankunftszeit (bei der Bestimmung von Ank�nften gem�� einer Bedingung)
	 * @param simData	Simulationsdatenobjekt
	 * @param stationName	Name der Station
	 * @return	Minimale zul�ssig Zwischenankunftszeit
	 * @see #clearConditionMinDistanceCalculated()
	 */
	public long getConditionMinDistanceMS(final SimulationData simData, final String stationName) {
		if (conditionMinDistanceCalculatedMS<0) {
			try {
				conditionMinDistanceCalculatedMS=Math.round(Math.max(0,conditionMinDistance.calc(simData.runData.variableValues,simData,null)*simData.runModel.scaleToSimTime));
			} catch (MathCalcError e) {
				simData.calculationErrorStation(conditionMinDistance,stationName);
				conditionMinDistanceCalculatedMS=0;
			}
		}
		return conditionMinDistanceCalculatedMS;
	}

	/**
	 * Definiert die aktuell berechnete minimale zul�ssige Zwischenankunftszeit als verwendet.
	 * Beim n�chsten Aufruf von {@link #getConditionMinDistanceMS(SimulationData, String)} wird dann eine neue Zeitdauer berechnet.
	 * @see #getConditionMinDistanceMS(SimulationData, String)
	 */
	public void clearConditionMinDistanceCalculated() {
		if (conditionMinDistanceIsConst) return; /* Wenn die minimale Zwishchenankunftszeit fix ist, brauchen wir diese nicht verwerfen und neu berechnen. */
		conditionMinDistanceCalculatedMS=-1;
	}

	/**
	 * Pr�ft, ob die Schwellenwert-Bedingung seit dem letzten Test erreicht wurde
	 * @param simData	Simulationsdatenobjekt
	 * @param data	Station, an der der Rechenausdruck ausgewertet werden sollte
	 * @return	Gibt <code>true</code> zur�ck, wenn die Schwellenwert-Bedingung seit dem letzten Test erreicht wurde
	 */
	public boolean checkThreshold(final SimulationData simData, final RunElementData data) {
		/* Nach Abbruch ist wirklich Schluss */
		if (simData.runData.stopp) return false;

		/* Schwellenwert gesetzt? */
		if (threshold==null) return false;

		/* Schwellenwert pr�fen */
		simData.runData.setClientVariableValues(null);
		double check=0;
		try {
			check=threshold.calc(simData.runData.variableValues,simData,null);
		} catch (MathCalcError e) {
			simData.calculationErrorStation(threshold,data);
			return false;
		}

		boolean trigger=false;
		if (thresholdIsLastValueAvailable) {
			if (thresholdIsDirectionUp) {
				if (thresholdLastValue<=thresholdValue && check>thresholdValue) trigger=true;
			} else {
				if (thresholdLastValue>=thresholdValue && check<thresholdValue) trigger=true;
			}
		}
		thresholdLastValue=check;
		thresholdIsLastValueAvailable=true;
		return trigger;
	}
}


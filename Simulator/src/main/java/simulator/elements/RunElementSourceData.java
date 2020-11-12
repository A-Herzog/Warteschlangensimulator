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
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import simulator.simparser.ExpressionMultiEval;

/**
 * Laufzeitdaten eines <code>RunElementSource</code>-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementSource
 * @see RunElementData
 */
public class RunElementSourceData extends RunElementData {
	/** Z�hlung der Anzahl an Ank�nften (k�nnen ganze Batche sein) an dieser Station */
	public long arrivalCount;
	/** Z�hlung der Anzahl an eingetroffenen Kunden (ggf. Z�hlung in den Batchen) an dieser Station */
	public long arrivalClientCount;
	/** Zeitpunkt der letzten Kundenankunft (in Millisekunden) */
	public long arrivalTime;
	/** H�chstanzahl an Kundenank�nften �berhaupt; eigentlich wird das Simulationsende an anderer Stelle ermittelt, aber hat allein diese Quelle das doppelte dieses Wertes an Kunden erzeugt, so stellt sie die Arbeit ein. */
	public long maxSystemArrival;
	/** Ausdruck zur Berechnung der Batch-Gr��en (kann <code>null</code> sein) */
	public final ExpressionCalc batchSize;
	/** Zwischenankunftszeiten-Ausdruck (kann <code>null</code> sein) */
	public final ExpressionCalc expression;
	/** Bedingung zur Kundenfreigabe  (kann <code>null</code> sein) */
	public final ExpressionMultiEval condition;

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

	/** Ausdr�cke f�r Zuweisungen von Zahlen-Eigenschaften zu neuen Kunden */
	public final RunElementSourceRecord.SourceSetExpressions setData;

	/**
	 * Konstruktor der Klasse <code>RunElementSourceData</code>
	 * @param station	Station zu diesem Datenelement
	 * @param batchSize	Ausdruck zur Berechnung der Batch-Gr��en (kann <code>null</code> sein)
	 * @param expression	Zwischenankunftszeiten-Ausdruck (kann <code>null</code> sein)
	 * @param condition	Bedingung zur Kundenfreigabe  (kann <code>null</code> sein)
	 * @param threshold	Schwellenwert f�r Kundenfreigabe  (kann <code>null</code> sein)
	 * @param thresholdValue	Wert gegen den gepr�ft werden soll (Schwellenwert)
	 * @param thresholdIsDirectionUp	Ankunft beim unter- (<code>false</code>) oder �berschreiten (<code>true</code>) des Schwellenwertes
	 * @param variableNames	Liste der global verf�gbaren Variablennamen
	 * @param setData	Ausdr�cke f�r Zuweisungen von Zahlen-Eigenschaften zu neuen Kunden
	 */
	public RunElementSourceData(final RunElement station, final String batchSize, final String expression, final String condition, final String threshold, final double thresholdValue, final boolean thresholdIsDirectionUp, final String[] variableNames, final RunElementSourceRecord.SourceSetExpressions setData) {
		super(station);
		arrivalCount=0;
		arrivalClientCount=0;
		arrivalTime=-1;
		maxSystemArrival=0;

		if (batchSize==null) {
			this.batchSize=null;
		} else {
			this.batchSize=new ExpressionCalc(variableNames);
			this.batchSize.parse(batchSize);
		}

		if (expression==null) {
			this.expression=null;
		} else {
			this.expression=new ExpressionCalc(variableNames);
			this.expression.parse(expression);
		}

		if (condition==null) {
			this.condition=null;
		} else {
			this.condition=new ExpressionMultiEval(variableNames);
			this.condition.parse(condition);
		}

		if (threshold==null) {
			this.threshold=null;
		} else {
			this.threshold=new ExpressionCalc(variableNames);
			this.threshold.parse(threshold);
		}
		this.thresholdValue=thresholdValue;
		this.thresholdIsDirectionUp=thresholdIsDirectionUp;

		this.setData=setData;
	}

	/**
	 * Pr�ft, ob die Schwellenwert-Bedingung seit dem letzten Test erreicht wurde
	 * @param simData	Simulationsdatenobjekt
	 * @return	Gibt <code>true</code> zur�ck, wenn die Schwellenwert-Bedingung seit dem letzten Test erreicht wurde
	 */
	public boolean checkThreshold(final SimulationData simData) {
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
			simData.calculationErrorStation(threshold,this);
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
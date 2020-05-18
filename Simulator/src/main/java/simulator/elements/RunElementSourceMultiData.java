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

import java.util.Arrays;

import parser.MathCalcError;
import simulator.coreelements.RunElement;
import simulator.coreelements.RunElementData;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import simulator.simparser.ExpressionMultiEval;

/**
 * Laufzeitdaten eines <code>RunElementSourceMulti</code>-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementSourceMulti
 * @see RunElementData
 */
public class RunElementSourceMultiData extends RunElementData {
	/** Ausdrücke zur Berechnung der Batch-Größen (kann <code>null</code> sein) */
	public final ExpressionCalc[] batchSizes;
	/** Zählung der Anzahl an Ankünften (können ganze Batche sein) an dieser Station */
	public final long[] arrivalCount;
	/** Zählung der Anzahl an eingetroffenen Kunden (ggf. Zählung in den Batchen) an dieser Station */
	public final long[] arrivalClientCount;
	/** Zeitpunkt der letzten Kundenankunft (in Millisekunden) */
	public final long[] arrivalTime;
	/** Zwischenankunftszeiten-Ausdrücke (können <code>null</code> sein) */
	public final ExpressionCalc[] expression;
	/** Bedingungen zur Kundenfreigabe  (können <code>null</code> sein) */
	public final ExpressionMultiEval[] condition;

	private final ExpressionCalc[] threshold;
	private final double[] thresholdValue;
	private final boolean[] thresholdIsDirectionUp;
	private final double[] thresholdLastValue;
	private final boolean[] thresholdIsLastValueAvailable;

	/** Ausdrücke für Zuweisungen von Zahlen-Eigenschaften zu neuen Kunden */
	public final RunElementSourceRecord.SourceSetExpressions[] setData;

	/**
	 * Konstruktor der Klasse <code>RunElementSourceMultiData</code>
	 * @param station	Station zu diesem Datenelement
	 * @param batchSizes	Ausdrücke zur Berechnung der Batch-Größen (kann <code>null</code> sein)
	 * @param expression	Zwischenankunftszeiten-Ausdrücke (können <code>null</code> sein)
	 * @param condition	Bedingungen zur Kundenfreigabe  (können <code>null</code> sein)
	 * @param threshold	Schwellenwert für Kundenfreigabe  (kann <code>null</code> sein)
	 * @param thresholdValue	Wert gegen den geprüft werden soll (Schwellenwert)
	 * @param thresholdIsDirectionUp	Ankunft beim unter- (<code>false</code>) oder überschreiten (<code>true</code>) des Schwellenwertes
	 * @param variableNames	Liste der global verfügbaren Variablennamen
	 * @param setData	Ausdrücke für Zuweisungen von Zahlen-Eigenschaften zu neuen Kunden
	 */
	public RunElementSourceMultiData(final RunElement station, final String[] batchSizes, final String[] expression, final String[] condition, final String[] threshold, final double[] thresholdValue, final boolean[] thresholdIsDirectionUp, final String[] variableNames, final RunElementSourceRecord.SourceSetExpressions[] setData) {
		super(station);

		this.batchSizes=new ExpressionCalc[batchSizes.length];
		for (int i=0;i<batchSizes.length;i++) if (batchSizes[i]==null) {
			this.batchSizes[i]=null;
		} else {
			this.batchSizes[i]=new ExpressionCalc(variableNames);
			this.batchSizes[i].parse(batchSizes[i]);
		}

		arrivalCount=new long[expression.length];
		arrivalClientCount=new long[expression.length];
		arrivalTime=new long[expression.length];
		Arrays.fill(arrivalTime,-1);

		this.expression=new ExpressionCalc[expression.length];
		for (int i=0;i<expression.length;i++) if (expression[i]==null) {
			this.expression[i]=null;
		} else {
			this.expression[i]=new ExpressionCalc(variableNames);
			this.expression[i].parse(expression[i]);
		}

		this.condition=new ExpressionMultiEval[condition.length];
		for (int i=0;i<condition.length;i++) if (condition[i]==null) {
			this.condition[i]=null;
		} else {
			this.condition[i]=new ExpressionMultiEval(variableNames);
			this.condition[i].parse(condition[i]);
		}

		this.threshold=new ExpressionCalc[threshold.length];
		this.thresholdValue=new double[threshold.length];
		this.thresholdIsDirectionUp=new boolean[threshold.length];
		this.thresholdLastValue=new double[threshold.length];
		this.thresholdIsLastValueAvailable=new boolean[threshold.length];
		Arrays.fill(this.thresholdIsLastValueAvailable,false);
		for (int i=0;i<threshold.length;i++) if (threshold[i]==null) {
			this.threshold[i]=null;
		} else {
			this.threshold[i]=new ExpressionCalc(variableNames);
			this.threshold[i].parse(threshold[i]);
			this.thresholdValue[i]=thresholdValue[i];
			this.thresholdIsDirectionUp[i]=thresholdIsDirectionUp[i];
		}

		this.setData=setData;
	}

	/**
	 * Prüft, ob die Schwellenwert-Bedingung seit dem letzten Test erreicht wurde
	 * @param simData	Simulationsdatenobjekt
	 * @param index	Index des Untereintrags, für den die Bedingung geprüft werden soll
	 * @return	Gibt <code>true</code> zurück, wenn die Schwellenwert-Bedingung seit dem letzten Test erreicht wurde
	 */
	public boolean checkThreshold(final SimulationData simData, final int index) {
		/* Nach Abbruch ist wirklich Schluss */
		if (simData.runData.stopp) return false;

		/* Schwellenwert gesetzt? */
		if (threshold==null || index<0 || index>threshold.length || threshold[index]==null) return false;

		/* Schwellenwert prüfen */
		simData.runData.setClientVariableValues(null);
		double check=0;
		try {
			check=threshold[index].calc(simData.runData.variableValues,simData,null);
		} catch (MathCalcError e) {
			simData.calculationErrorStation(threshold[index],this);
			return false;
		}

		boolean trigger=false;
		if (thresholdIsLastValueAvailable[index]) {
			if (thresholdIsDirectionUp[index]) {
				if (thresholdLastValue[index]<=thresholdValue[index] && check>thresholdValue[index]) trigger=true;
			} else {
				if (thresholdLastValue[index]>=thresholdValue[index] && check<thresholdValue[index]) trigger=true;
			}
		}
		thresholdLastValue[index]=check;
		thresholdIsLastValueAvailable[index]=true;
		return trigger;
	}
}
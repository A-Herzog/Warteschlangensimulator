/**
 * Copyright 2025 Alexander Herzog
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

/**
 * Laufzeitdaten eines {@link RunElementAssignMultiByChance}-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementAssignMultiByChance
 * @see RunElementData
 */
public class RunElementAssignMultiByChanceData extends RunElementAssignMultiBaseData {
	/** Für die verschiedenen Pfade zu berechnende Raten */
	private final ExpressionCalc[] rates;
	/** Cache-Objekt für die konkret berechneten Raten */
	private final double[] calculatedRates;

	/**
	 * Konstruktor der Klasse
	 * @param station	Station zu diesem Datenelement
	 * @param condition	Optionale zusätzliche Bedingung, die für eine Zuweisung erfüllt sein muss (kann <code>null</code> sein)	 *
	 * @param variableNames	Liste der global verfügbaren Variablennamen
	 * @param simData	Simulationsdatenobjekt
	 * @param rates	Raten gemäß denen die Aufteilung der Kunden erfolgt
	 */
	public RunElementAssignMultiByChanceData(final RunElement station, final String condition, final String[] variableNames, final SimulationData simData, final String[] rates) {
		super(station,condition,variableNames,simData);

		if (rates==null) {
			this.rates=new ExpressionCalc[0];
			calculatedRates=new double[0];
			return;
		}

		this.rates=new ExpressionCalc[rates.length];
		calculatedRates=new double[rates.length];

		for (int i=0;i<rates.length;i++) {
			final ExpressionCalc rate=new ExpressionCalc(variableNames,simData.runModel.modelUserFunctions);
			rate.parse(rates[i]);
			if (rate.isConstValue()) {
				calculatedRates[i]=rate.getConstValue();
			} else {
				this.rates[i]=rate;
			}
		}
	}

	/**
	 * Berechnet den Index für die Zuweisung gemäß den Rechenausdrücken.
	 * @param simData	Simulationdatenobjekt
	 * @return	Index der Zuweisung
	 */
	public int getIndex(final SimulationData simData) {
		double sum=0.0;
		for (int i=0;i<rates.length;i++) {
			if (rates[i]!=null) {
				/* Nicht-deterministischer Ausdruck => neu berechnen */
				double d=0.0;
				try {
					d=rates[i].calc(simData.runData.variableValues);
				} catch (MathCalcError e) {
					simData.calculationErrorStation(rates[i],station.name);
					d=0;
				}
				if (d<0) d=0;
				calculatedRates[i]=d;
				sum+=d;
			} else {
				/* Konstanten Wert einfach verwenden */
				sum+=calculatedRates[i];
			}
		}
		if (sum==0.0) return 0;

		final double rnd=simData.runData.random.nextDouble()*sum;
		double d=0.0;
		for (int i=0;i<rates.length-1;i++) { /* -1; spart einen Rechenschritt, da wir so oder so einfach den Fallback-Wert verwenden. */
			d+=calculatedRates[i];
			if (d>=rnd) return i;
		}
		return rates.length-1;
	}
}

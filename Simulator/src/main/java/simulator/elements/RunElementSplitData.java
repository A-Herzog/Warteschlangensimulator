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
import simulator.runmodel.RunDataClient;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;

/**
 * Laufzeitdaten eines <code>RunElementSplit</code>-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementSplit
 * @see RunElementData
 */
public class RunElementSplitData extends RunElementData {
	/** Ausdr�cke f�r Zuweisungen von Zahlen-Eigenschaften zu neuen Kunden */
	public final RunElementSourceRecord.SourceSetExpressions[] setData;
	/** Ausdr�cke zur Berechnung der Batch-Gr��en (kann <code>null</code> sein) */
	public final ExpressionCalc[] batchSizes;

	/**
	 * Cache f�r das Array zum �bertragen der Kundendaten
	 * @see RunDataClient#copyUserData(double[])
	 */
	public double[] tempUserData;

	/**
	 * Cache f�r das Array zum �bertragen der Nutzungsflags der Kundendaten
	 * @see RunDataClient#copyUserDataInUse(boolean[])
	 */
	public boolean[] tempUserDataInUse;

	/**
	 * Konstruktor der Klasse <code>RunElementSplitData</code>
	 * @param station	Station zu diesem Datenelement
	 * @param variableNames	Liste der global verf�gbaren Variablennamen
	 * @param setData	Ausdr�cke f�r Zuweisungen von Zahlen-Eigenschaften zu neuen Kunden
	 * @param batchSizes	Ausdr�cke zur Berechnung der Batch-Gr��en (kann <code>null</code> sein)
	 * @param simData	Simulationsdatenobjekt
	 */
	public RunElementSplitData(final RunElement station, final String[] variableNames, final RunElementSourceRecord.SourceSetExpressions[] setData, final String[] batchSizes, final SimulationData simData) {
		super(station,simData);

		this.setData=setData;

		this.batchSizes=new ExpressionCalc[batchSizes.length];
		for (int i=0;i<batchSizes.length;i++) if (batchSizes[i]==null) {
			this.batchSizes[i]=null;
		} else {
			this.batchSizes[i]=new ExpressionCalc(variableNames,simData.runModel.modelUserFunctions);
			this.batchSizes[i].parse(batchSizes[i]);
		}
	}
}

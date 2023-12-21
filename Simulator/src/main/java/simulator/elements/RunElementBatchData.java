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
import simulator.runmodel.RunDataClient;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;

/**
 * Laufzeitdaten eines {@link RunElementBatch}-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementBatch
 * @see RunElementData
 */
public class RunElementBatchData extends RunElementData {
	/**
	 * Minimale Batch-Gr��e<br>
	 */
	public final int batchSizeMin;

	/**
	 * Maximale Batch-Gr��e<br>
	 */
	public final int batchSizeMax;

	/**
	 * Liste der wartenden Kunden<br>
	 * (Wie viele Eintr�ge belegt sind, kann aus <code>waiting</code> ausgelesen werden.)
	 * @see #waiting
	 */
	public final RunDataClient[] clients;

	/**
	 * Liste der Ankunftszeiten der wartenden Kunden<br>
	 * (Wie viele Eintr�ge belegt sind, kann aus <code>waiting</code> ausgelesen werden.)
	 * @see #waiting
	 */
	public final long[] clientAddTime;

	/**
	 * Anzahl der Kunden, die momentan auf die Batch-Bildung warten
	 */
	public int waiting;

	/**
	 * Konstruktor der Klasse <code>RunElementBatchData</code>
	 * @param station	Station zu diesem Datenelement
	 * @param batchSizeMin	Minimale Batch-Gr��e
	 * @param batchSizeMax	Maximale Batch-Gr��e
	 * @param simData	Simulationsdatenobjekt
	 */
	public RunElementBatchData(final RunElement station, final String batchSizeMin, final String batchSizeMax, final SimulationData simData) {
		super(station,simData);

		waiting=0;

		ExpressionCalc calc;
		final Double min, max;

		calc=new ExpressionCalc(simData.runModel.variableNames);
		calc.parse(batchSizeMin);
		try {
			min=calc.calc(simData.runData.variableValues,simData,null);
		} catch (MathCalcError e) {
			this.batchSizeMin=1;
			this.batchSizeMax=1;
			clients=new RunDataClient[this.batchSizeMax];
			clientAddTime=new long[this.batchSizeMax];
			simData.doEmergencyShutDown(String.format(Language.tr("Simulation.Creator.InvalidBatchSize"),station.id));
			return;
		}

		calc=new ExpressionCalc(simData.runModel.variableNames);
		calc.parse(batchSizeMax);
		try {
			max=calc.calc(simData.runData.variableValues,simData,null);
		} catch (MathCalcError e) {
			this.batchSizeMin=1;
			this.batchSizeMax=1;
			clients=new RunDataClient[this.batchSizeMax];
			clientAddTime=new long[this.batchSizeMax];
			simData.doEmergencyShutDown(String.format(Language.tr("Simulation.Creator.InvalidBatchSize"),station.id));
			return;
		}

		final int minInt=min.intValue();
		final int maxInt=max.intValue();
		if (minInt>maxInt) {
			simData.doEmergencyShutDown(String.format(Language.tr("Simulation.Creator.InvalidMaximumBatchSize"),station.id));
			this.batchSizeMin=1;
			this.batchSizeMax=1;
			clients=new RunDataClient[this.batchSizeMax];
			clientAddTime=new long[this.batchSizeMax];
			waiting=0;
			return;
		}

		this.batchSizeMin=minInt;
		this.batchSizeMax=maxInt;
		clients=new RunDataClient[this.batchSizeMax];
		clientAddTime=new long[this.batchSizeMax];
	}

	/**
	 * F�gt einen Kunden zu der Liste der auf die Batch-Erstellung wartenden Kunden hinzu
	 * @param client	Hinzuzuf�gender Kunde
	 * @param time	Zeitpunkt an dem der Kunde an der <code>RunElementBatch</code>-Station eingetroffen ist (zur sp�teren Berechnung der Wartezeit der Kunden)
	 * @return	Gibt 0 zur�ck, wenn die notwendige Batch-Gr��e noch nicht erreicht wurde; 1 wenn die minimale Gr��e erreicht wurde und 2 wenn die maximale Gr��e erreicht wurde
	 */
	public int addClient(final RunDataClient client, final long time) {
		clients[waiting]=client;
		clientAddTime[waiting]=time;
		waiting++;
		if (waiting==clients.length) return 2; /* Maximale Batch-Gr��e erreicht */
		if (waiting>=batchSizeMin) return 1; /* Minimale Batch-Gr��e erreicht */
		return 0; /* Noch nicht genug Kunden eingetroffen */
	}
}

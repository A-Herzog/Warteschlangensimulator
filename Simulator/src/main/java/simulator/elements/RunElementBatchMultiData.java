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
import simulator.coreelements.RunElementData;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;

/**
 * Laufzeitdaten eines {@link RunElementBatchMulti}-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementBatchMulti
 * @see RunElementData
 */
public class RunElementBatchMultiData extends RunElementData {
	/**
	 * Minimale Batch-Gr��en
	 */
	public final int[] batchSizeMin;

	/**
	 * Maximale Batch-Gr��en
	 */
	public final int[] batchSizeMax;

	/**
	 * Liste der wartenden Kunden (pro Kundentyp)<br>
	 * (Wie viele Eintr�ge belegt sind, kann aus <code>waiting</code> ausgelesen werden.)
	 * @see #waiting
	 */
	public RunDataClient[][] clients;

	/**
	 * Liste der Ankunftszeiten der wartenden Kunden (pro Kundentyp)<br>
	 * (Wie viele Eintr�ge belegt sind, kann aus <code>waiting</code> ausgelesen werden.)
	 * @see #waiting
	 */
	public long[][] clientAddTime;

	/**
	 * Anzahl der Kunden, die momentan auf die Batch-Bildung warten (pro Kundentyp)
	 */
	public int[] waiting;

	/**
	 * Gesamtanzahl an Kunden, die momentan auf die Batch-Bildung warten
	 */
	public int waitingTotal;

	/**
	 * Konstruktor der Klasse
	 * @param station	Station zu diesem Datenelement
	 * @param simData	Simulationsdatenobjekt
	 */
	public RunElementBatchMultiData(final RunElementBatchMulti station, final SimulationData simData) {
		super(station);

		batchSizeMin=new int[station.batchMode.length];
		batchSizeMax=new int[station.batchMode.length];
		clients=new RunDataClient[station.batchMode.length][];
		clientAddTime=new long[station.batchMode.length][];
		waiting=new int[station.batchMode.length];
		waitingTotal=0;

		for (int i=0;i<station.batchMode.length;i++) if (station.batchMode[i]!=null) {
			batchSizeMin[i]=1;
			batchSizeMin[i]=2;
			clients[i]=new RunDataClient[1];
			clientAddTime[i]=new long[1];
		}

		for (int i=0;i<station.batchMode.length;i++) if (station.batchMode[i]!=null) {
			ExpressionCalc calc;
			final Double min, max;

			calc=new ExpressionCalc(simData.runModel.variableNames);
			calc.parse(station.batchSizeMin[i]);
			try {
				min=calc.calc(simData.runData.variableValues,simData,null);
			} catch (MathCalcError e) {
				simData.doEmergencyShutDown(String.format(Language.tr("Simulation.Creator.InvalidBatchSize"),station.id));
				return;
			}

			calc=new ExpressionCalc(simData.runModel.variableNames);
			calc.parse(station.batchSizeMax[i]);
			try {
				max=calc.calc(simData.runData.variableValues,simData,null);
			} catch (MathCalcError e) {
				simData.doEmergencyShutDown(String.format(Language.tr("Simulation.Creator.InvalidBatchSize"),station.id));
				return;
			}

			final int minInt=min.intValue();
			final int maxInt=max.intValue();
			if (minInt>maxInt) {
				simData.doEmergencyShutDown(String.format(Language.tr("Simulation.Creator.InvalidMaximumBatchSize"),station.id));
				return;
			}

			batchSizeMin[i]=minInt;
			batchSizeMax[i]=maxInt;
			clients[i]=new RunDataClient[maxInt];
			clientAddTime[i]=new long[maxInt];
		}
	}

	/**
	 * F�gt einen Kunden zu der Liste der auf die Batch-Erstellung wartenden Kunden hinzu
	 * @param client	Hinzuzuf�gender Kunde
	 * @param time	Zeitpunkt an dem der Kunde an der <code>RunElementBatch</code>-Station eingetroffen ist (zur sp�teren Berechnung der Wartezeit der Kunden)
	 * @return	Gibt -1 zur�ck, wenn der Kunde nicht gebatcht werden soll; 0, wenn die notwendige Batch-Gr��e noch nicht erreicht wurde; 1 wenn die minimale Gr��e erreicht wurde und 2 wenn die maximale Gr��e erreicht wurde
	 */
	public int addClient(final RunDataClient client, final long time) {
		final int index=client.type;
		if (clients[index]==null) return -1;

		clients[index][waiting[index]]=client;
		clientAddTime[index][waiting[index]]=time;
		waiting[index]++;
		waitingTotal++;
		if (waiting[index]==clients[index].length) return 2; /* Maximale Batch-Gr��e erreicht */
		if (waiting[index]>=batchSizeMin[index]) return 1; /* Minimale Batch-Gr��e erreicht */
		return 0; /* Noch nicht genug Kunden eingetroffen */
	}
}

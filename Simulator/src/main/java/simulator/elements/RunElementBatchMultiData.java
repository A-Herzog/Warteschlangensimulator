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

import simulator.coreelements.RunElementData;
import simulator.runmodel.RunDataClient;

/**
 * Laufzeitdaten eines {@link RunElementBatchMulti}-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementBatchMulti
 * @see RunElementData
 */
public class RunElementBatchMultiData extends RunElementData {
	private int[] batchSizeMin;

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
	 */
	public RunElementBatchMultiData(final RunElementBatchMulti station) {
		super(station);

		batchSizeMin=new int[station.batchMode.length];
		clients=new RunDataClient[station.batchMode.length][];
		clientAddTime=new long[station.batchMode.length][];
		waiting=new int[station.batchMode.length];

		for (int i=0;i<station.batchMode.length;i++) if (station.batchMode[i]!=null) {
			batchSizeMin[i]=station.batchSizeMin[i];
			clients[i]=new RunDataClient[station.batchSizeMax[i]];
			clientAddTime[i]=new long[station.batchSizeMax[i]];
		}

		waitingTotal=0;
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

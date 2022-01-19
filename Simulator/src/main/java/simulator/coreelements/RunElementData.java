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
package simulator.coreelements;

import java.util.Arrays;

import simulator.runmodel.RunData;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.SimulationData;
import statistics.StatisticsDataPerformanceIndicator;
import statistics.StatisticsTimePerformanceIndicator;

/**
 * Enthält die Laufzeit-Daten einer <code>RunElement</code>-Station<br>
 * (Die <code>RunElement</code>-Objekte werden über alle Threads geteilt, können also keine lokalen Daten enthalten.)
 * @author Alexander Herzog
 * @see RunElement
 * @see RunData#getStationData(RunElement)
 * @see RunData#setStationData(RunElement, RunElementData)
 */
public class RunElementData {
	/**
	 * Die zu dem Datenobjekt gehörige Station.
	 */
	public final RunElement station;

	/**
	 * Speichert, wie viele Kunden (inkl. Warm-up-Kunden) bisher an der Station eingetroffen sind.
	 * @see #clientsNonWarmUp
	 */
	public long clients;

	/**
	 * Speichert, wie viele Kunden sich momentan in der Station aufhalten.
	 */
	public int clientsAtStation;

	/**
	 * Speichert, wie viele Kunden (ohne Warm-up-Kunden) bisher an der Station eingetroffen sind.
	 * @see #clients
	 */
	public long clientsNonWarmUp;

	/**
	 * Speichert, wie viele Kunden sich momentan in der Warteschlange der Station aufhalten.
	 */
	public int clientsAtStationQueue;

	/**
	 * Speichert, wann jeweils zum letzten Mal ein Kunde bei einer bestimmten Station eingetroffen ist.
	 */
	public long lastArrival;

	/**
	 * Speichert, wann jeweils zum letzten Mal ein Kunden-Batch bei einer bestimmten Station eingetroffen ist.
	 */
	public long lastBatchArrival;

	/**
	 * Speichert, wann jeweils zum letzten Mal ein Kunde (pro Kundentyp) bei einer bestimmten Station eingetroffen ist.<br>
	 * Ist initial <code>null</code>, wird erst bei der ersten Ankunft initialisiert.
	 */
	public long[] lastArrivalByClientType;

	/**
	 * Speichert, wann jeweils zum letzten Mal ein Kunde bei eine bestimmte Station verlassen hat.
	 */
	public long lastLeave;

	/**
	 * Speichert, wann jeweils zum letzten Mal ein Kunde (pro Kundentyp) bei eine bestimmte Station verlassen hat.<br>
	 * Ist initial <code>null</code>, wird erst bei der ersten Ankunft initialisiert.
	 */
	public long[] lastLeaveByClientType;

	/**
	 * Vorab angekündigte Kundenankünfte (für {@link #reportedClientsAtStation(SimulationData)})
	 */
	private RunDataClient[] announcedClient;

	/**
	 * Anzahl der vorab angekündigten Kundenankünfte (für {@link #reportedClientsAtStation(SimulationData)})
	 */
	private int announcedClientCount;

	/**
	 * Letztes Änderungsdatum der angekündigten Ankünfte<br>
	 * (Ist der Wert bei einem folgenden Aufruf kleiner, so muss ein neuer Lauf gestartet worden sein; dann wird {@link #announcedClientCount} genullt.)
	 */
	private long announcedClientLastUpdate;

	/**
	 * Ermöglicht den Zugriff auf die Wartezeit-Statistik an der Station.<br>
	 * (Kann <code>null</code> sein, wenn noch keine entsprechende Zeit angefallen ist.)
	 */
	public StatisticsDataPerformanceIndicator statisticWaiting;

	/**
	 * Ermöglicht den Zugriff auf die Transferzeit-Statistik an der Station.<br>
	 * (Kann <code>null</code> sein, wenn noch keine entsprechende Zeit angefallen ist.)
	 */
	public StatisticsDataPerformanceIndicator statisticTransfer;

	/**
	 * Ermöglicht den Zugriff auf die Bedienzeit-Statistik an der Station.<br>
	 * (Kann <code>null</code> sein, wenn noch keine entsprechende Zeit angefallen ist.)
	 */
	public StatisticsDataPerformanceIndicator statisticProcess;

	/**
	 * Ermöglicht den Zugriff auf die Verweilzeit-Statistik an der Station.<br>
	 * (Kann <code>null</code> sein, wenn noch keine entsprechende Zeit angefallen ist.)
	 */
	public StatisticsDataPerformanceIndicator statisticResidence;

	/**
	 * Ermöglicht den Zugriff auf die Wartezeit-Statistik an der Station (nach Kundentypen).<br>
	 * (Kann <code>null</code> sein, wenn noch keine entsprechende Zeit angefallen ist.)
	 */
	public StatisticsDataPerformanceIndicator[] statisticWaitingByClientType;

	/**
	 * Ermöglicht den Zugriff auf die Transferzeit-Statistik an der Station (nach Kundentypen).<br>
	 * (Kann <code>null</code> sein, wenn noch keine entsprechende Zeit angefallen ist.)
	 */
	public StatisticsDataPerformanceIndicator[] statisticTransferByClientType;

	/**
	 * Ermöglicht den Zugriff auf die Bedienzeit-Statistik an der Station (nach Kundentypen).<br>
	 * (Kann <code>null</code> sein, wenn noch keine entsprechende Zeit angefallen ist.)
	 */
	public StatisticsDataPerformanceIndicator[] statisticProcessByClientType;

	/**
	 * Ermöglicht den Zugriff auf die Verweilzeit-Statistik an der Station (nach Kundentypen).<br>
	 * (Kann <code>null</code> sein, wenn noch keine entsprechende Zeit angefallen ist.)
	 */
	public StatisticsDataPerformanceIndicator[] statisticResidenceByClientType;

	/**
	 * Ermöglicht den Zugriff auf die "Anzahl an Kunden an der Station"-Statistik.<br>
	 * (Kann <code>null</code> sein, wenn noch keine entsprechenden Daten angefallen sind.)
	 */
	public StatisticsTimePerformanceIndicator statisticClientsAtStation;

	/**
	 * Erfassung der aktuellen Anzahl an Kunden an der Station pro Kundentyp
	 */
	public int[] statisticClientsAtStationByClientTypeValue;

	/**
	 * Statistikobjekte für die Anzahlen an Kunden an der Station pro Kundentyp
	 */
	public StatisticsTimePerformanceIndicator[] statisticClientsAtStationByClientType;

	/**
	 * Erfassung der aktuellen Anzahl an Kunden in der Warteschlange an der Station pro Kundentyp
	 */
	public int[] statisticClientsAtStationQueueByClientTypeValue;

	/**
	 * Statistikobjekte für die Anzahlen an Kunden in der Warteschlange an der Station pro Kundentyp
	 */
	public StatisticsTimePerformanceIndicator[] statisticClientsAtStationQueueByClientType;

	/**
	 * Ermöglicht den Zugriff auf die "Anzahl an Kunden in der Warteschlange an der Station"-Statistik.<br>
	 * (Kann <code>null</code> sein, wenn noch keine entsprechenden Daten angefallen sind.)
	 */
	public StatisticsTimePerformanceIndicator statisticClientsAtStationQueue;

	/**
	 * Ermöglicht den Zugriff auf die "Zwischenankunftszeiten an der Station"-Statistik.<br>
	 * (Kann <code>null</code> sein, wenn noch keine entsprechenden Daten angefallen sind.)
	 */
	public StatisticsDataPerformanceIndicator statisticStationsInterarrivalTime;

	/**
	 * Ermöglicht den Zugriff auf die "Zwischenankunftszeiten an der Station (Batch)"-Statistik.<br>
	 * (Kann <code>null</code> sein, wenn noch keine entsprechenden Daten angefallen sind.)
	 */
	public StatisticsDataPerformanceIndicator statisticStationsInterarrivalTimeBatch;

	/**
	 * Ermöglicht den Zugriff auf die "Zwischenankunftszeiten an der Station (nach Kundentypen)"-Statistik.<br>
	 * (Kann <code>null</code> sein, wenn noch keine entsprechenden Daten angefallen sind.)
	 */
	public StatisticsDataPerformanceIndicator[] statisticStationsInterarrivalTimeByClientType;

	/**
	 * Ermöglicht den Zugriff auf die "Zwischenabgangszeiten an der Station"-Statistik.<br>
	 * (Kann <code>null</code> sein, wenn noch keine entsprechenden Daten angefallen sind.)
	 */
	public StatisticsDataPerformanceIndicator statisticStationsInterleaveTime;

	/**
	 * Ermöglicht den Zugriff auf die "Zwischenabgangszeiten an der Station (nach Kundentypen)"-Statistik.<br>
	 * (Kann <code>null</code> sein, wenn noch keine entsprechenden Daten angefallen sind.)
	 */
	public StatisticsDataPerformanceIndicator[] statisticStationsInterleaveTimeByClientType;

	/**
	 * Ermöglicht den Zugriff auf die "Zwischenankunftszeiten an der Quellenstation"-Statistik.<br>
	 * (Kann <code>null</code> sein, wenn noch keine entsprechenden Daten angefallen sind.)
	 */
	public StatisticsDataPerformanceIndicator statisticSourceStationsInterarrivalTime;

	/**
	 * Konstruktor der Klasse <code>RunElementData</code>
	 * @param station	Runtime-Station für die dieses Datenelement gelten soll
	 */
	public RunElementData(final RunElement station) {
		this.station=station;
		clients=0;
		clientsNonWarmUp=0;
		lastArrival=-1;
		lastBatchArrival=-1;
		lastArrivalByClientType=null;
		lastLeave=-1;
		lastLeaveByClientType=null;
		clientsAtStation=0;
		clientsAtStationQueue=0;
		statisticClientsAtStationByClientTypeValue=null;
		statisticClientsAtStationQueueByClientTypeValue=null;
		announcedClient=new RunDataClient[10];
		announcedClientCount=0;
	}

	/**
	 * Über diese Methode kann eine von der tatsächliche Anzahl an Kunden an der Station
	 * abweichende Anzahl rückgemeldet werden (für die Anzeige in der Animation und für
	 * Rechenbefehle). In der Statistik hingegen wird immer der
	 * {@link RunElementData#clientsAtStation}-Wert erfasst.
	 * @param simData	Simulationsdatenobjekt
	 * @return	Wert, der für Rechenausdrücke und Animationen verwendet werden soll
	 */
	public int reportedClientsAtStation(final SimulationData simData) {
		return clientsAtStation+announcedClientCount;
	}

	/**
	 * Liefert die Anzahl an Kunden an der Station bezogen auf einen Kundentyp
	 * zur Verwendung in Rechenbefehlen.
	 * @param simData	Simulationsdatenobjekt
	 * @param clientTypeIndex	Index des Kundentyps
	 * @return	Anzahl an Kunden an der Station bezogen auf einen Kundentyp
	 */
	public int reportedClientsAtStation(final SimulationData simData, final int clientTypeIndex) {
		if (statisticClientsAtStationByClientTypeValue==null) return 0;
		if (clientTypeIndex<0) return 0;

		return statisticClientsAtStationByClientTypeValue[clientTypeIndex];
	}

	/**
	 * Kündigt die Ankunft eines Kunden an der Zielstation an (damit {@link RunElementData#reportedClientsAtStation(SimulationData)} bereits den neuen Wert liefern kann)
	 * @param simData	Simulationdatenobjekt
	 * @param client	Kunde, der verschickt wird
	 */
	public void announceClient(final SimulationData simData, final RunDataClient client) {
		if (simData.currentTime<announcedClientLastUpdate) {
			/* Start eines neuen Simulationslaufs */
			Arrays.fill(announcedClient,null);
			announcedClientCount=0;
		}

		/* Kunde in Liste suchen (und nebenbei ersten leeren Eintrag merken) */
		int firstNullIndex=-1;
		int foundClients=0;
		final int length=announcedClient.length;
		for (int i=0;i<length;i++) {
			final RunDataClient record=announcedClient[i];
			if (record==client) return; /* Schon in der Liste. */
			if (firstNullIndex<0 && record==null) firstNullIndex=i;
			foundClients++;
			if (foundClients>=announcedClientCount) break; /* Die nächsten Einträge können nur noch null sein. */
		}

		if (firstNullIndex<0) {
			/* Array erweitern */
			announcedClient=Arrays.copyOf(announcedClient,length*2);
			announcedClient[length]=client;
		} else {
			/* Eintrag einfach nur setzen */
			announcedClient[firstNullIndex]=client;
		}
		announcedClientCount++;
	}

	/**
	 * Trägt den Kunden wieder aus der Liste der angekündigten Ankünfte aus (weil er jetzt wirklich geschickt wird)
	 * @param simData	Simulationdatenobjekt
	 * @param client	Kunde, der verschickt wird
	 */
	public void unannounceClient(final SimulationData simData, final RunDataClient client) {
		if (simData.currentTime<announcedClientLastUpdate) {
			/* Start eines neuen Simulationslaufs */
			Arrays.fill(announcedClient,null);
			announcedClientCount=0;
		}

		/* Kunde austragen */
		final int length=announcedClient.length;
		for (int i=0;i<length;i++) if (announcedClient[i]==client) {
			announcedClient[i]=null;
			announcedClientCount--;
		}

		/* Liste leer und Liste zu groß? */
		if (announcedClientCount==0 && announcedClient.length>10) {
			announcedClient=new RunDataClient[10];
		}
	}
}

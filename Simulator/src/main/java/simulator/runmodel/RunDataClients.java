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
package simulator.runmodel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import language.Language;
import mathtools.NumberTools;
import simulator.statistics.Statistics;
import statistics.StatisticsDataPerformanceIndicator;
import statistics.StatisticsValuePerformanceIndicator;

/**
 * Die <code>RunDataClients</code>-Klasse hält die <code>RunDataClient</code>-Objekte vor
 * (inkl. Caching) und ermöglicht das Erfassen der Statistikdaten, wenn ein Kunde das System verlässt.
 * @author Alexander Herzog
 * @see RunDataClient
 */
public final class RunDataClients {
	/**
	 * Anzahl an Kunden im System, bei deren Überschreitung der Simulationsthread abgebrochen wird
	 * (im Fall einer Multi-Core-Simulation)
	 * @see SimulationData#testMaxAllowedClientsInSystem()
	 */
	public static final int MAX_CLIENTS_IN_SYSTEM_MULTI_CORE=350_000;

	/**
	 * Anzahl an Kunden im System, bei deren Überschreitung der Simulationsthread abgebrochen wird
	 * (im Fall einer Single-Core-Simulation)
	 * @see SimulationData#testMaxAllowedClientsInSystem()
	 */
	public static final int MAX_CLIENTS_IN_SYSTEM_SINGLE_CORE=5_000_000;

	/**
	 * Wie viele Kunden-Objekte sollen gecacht werden?<br>
	 * (Dies sollte in etwa der maximalen Anzahl an Kunden, die sich gleichzeitig im System befinden, entsprechen.)
	 */
	private static final int CLIENT_CACHE_SIZE=5_000;

	private long clientNumber;
	private final RunDataClient[] clientCache;
	private List<RunDataClient> clientsInUse;
	private Set<RunDataClient> clientsInUseFast;
	private int clientCacheUsed;
	private int clientsInSystem;

	private long[] lastInterLeaveByClientType;

	private RunData.IndicatorAccessCacheClientTypes cacheClientsWaitingTimes;
	private RunData.IndicatorAccessCacheClientTypes cacheClientsTransferTimes;
	private RunData.IndicatorAccessCacheClientTypes cacheClientsProcessingTimes;
	private RunData.IndicatorAccessCacheClientTypes cacheClientsResidenceTimes;
	private RunData.IndicatorAccessCacheClientTypes cacheClientsInterleaveTime;

	/**
	 * Konstruktor der Klasse <code>RunDataClients</code>
	 */
	public RunDataClients() {
		clientCache=new RunDataClient[CLIENT_CACHE_SIZE];
		clientsInUse=null;
		clientsInUseFast=null;
		clientCacheUsed=0;
		clientsInSystem=0;
	}

	/**
	 * Liefert ein neues <code>RunDataClient</code>-Objekt (entweder in dem es angelegt wird oder aber aus dem Cache)
	 * @param type	Kundentyp (Index im <code>RunModel.clientTypes</code>-Array)
	 * @param simData	Simulationsdaten
	 * @return	Liefert das neue Kundendaten-Objekt
	 */
	public RunDataClient getClient(final int type, final SimulationData simData) {
		clientNumber++;
		RunDataClient client;
		if (clientCacheUsed>0) {
			client=clientCache[clientCacheUsed-1];
			clientCacheUsed--;
			client.init(type,simData.runData.isWarmUp,clientNumber);
		} else {
			client=new RunDataClient(type,simData.runData.isWarmUp,clientNumber);
		}

		/* Icon festlegen */
		client.icon=simData.runModel.clientTypeIcons[type];
		client.iconLast=client.icon;

		/* Erfassung Anzahl an Kunden im System */
		clientsInSystem++;
		if (!simData.runData.isWarmUp) {
			simData.statistics.clientsInSystem.set(simData.currentTime*scale,clientsInSystem);
		}

		simData.runData.logClientsInSystemChange(simData,type,1);

		if (clientsInUse!=null) clientsInUse.add(client);
		if (clientsInUseFast!=null) clientsInUseFast.add(client);
		return client;
	}

	/**
	 * Erfasst am Simulationsende noch einmal die Anzahl an Kunden im System
	 * (sonst gehen die Daten seit der letzten Änderung der Anzahl an Kunden im System für die Statistik verloren).
	 * @param simData	Simulationsdatenobjekt
	 */
	public void finalizeNumberOfClientsInSystem(final SimulationData simData) {
		if (!simData.runData.isWarmUp) {
			simData.statistics.clientsInSystem.set(simData.currentTime*scale,clientsInSystem);
		}

		for (int type=0;type<simData.runModel.clientTypes.length;type++) simData.runData.logClientsInSystemChange(simData,type,0);
	}

	/**
	 * Kopiert ein bestehendes Kundendatenobjekt (für die Kopie wird dabei bevorzugt ein Objekt aus dem Cache verwendet)
	 * @param client	Zu kopierendes Kundendatenobjekt
	 * @param simData	Simulationsdaten
	 * @return	Neues Kundendatenobjekt
	 */
	public RunDataClient getClone(final RunDataClient client, final SimulationData simData) {
		RunDataClient clone=getClient(client.type,simData);
		clone.copyDataFrom(client,simData,this);
		return clone;
	}

	private static double scale=1.0d/1000.0d;

	private void logClientData(final RunDataClient client, final SimulationData simData) {
		/* Allgemeine Daten zusammenstellen */
		final int clientType=client.type;
		final String name=simData.runModel.clientTypes[clientType];
		final Statistics statistics=simData.statistics;

		/* Berechnung der Zeiten */
		final double waiting=scale*client.waitingTime;
		final double transfer=scale*client.transferTime;
		final double process=scale*client.processTime;
		final double residence=scale*client.residenceTime;

		/* Cache für kundentyp-abhängige Statistikobjekte erstellen */
		if (cacheClientsWaitingTimes==null) {
			cacheClientsWaitingTimes=new RunData.IndicatorAccessCacheClientTypes(simData.statistics.clientsWaitingTimes,simData.runModel.clientTypes);
			cacheClientsTransferTimes=new RunData.IndicatorAccessCacheClientTypes(simData.statistics.clientsTransferTimes,simData.runModel.clientTypes);
			cacheClientsProcessingTimes=new RunData.IndicatorAccessCacheClientTypes(simData.statistics.clientsProcessingTimes,simData.runModel.clientTypes);
			cacheClientsResidenceTimes=new RunData.IndicatorAccessCacheClientTypes(simData.statistics.clientsResidenceTimes,simData.runModel.clientTypes);
		}

		/* Kundentyp-abhängige Werte erfassen */
		((StatisticsDataPerformanceIndicator)cacheClientsWaitingTimes.get(clientType)).add(waiting);
		((StatisticsDataPerformanceIndicator)cacheClientsTransferTimes.get(clientType)).add(transfer);
		((StatisticsDataPerformanceIndicator)cacheClientsProcessingTimes.get(clientType)).add(process);
		((StatisticsDataPerformanceIndicator)cacheClientsResidenceTimes.get(clientType)).add(residence);

		/* Globale Werte erfassen */
		statistics.clientsAllWaitingTimes.add(waiting);
		statistics.clientsAllTransferTimes.add(transfer);
		statistics.clientsAllProcessingTimes.add(process);
		statistics.clientsAllResidenceTimes.add(residence);

		/* Laufzeitstatistik für Wartezeiten */
		if (statistics.clientsAllWaitingTimesCollector!=null) statistics.clientsAllWaitingTimesCollector.add(waiting);

		/* Kosten */
		final double[] costFactors=simData.runModel.clientCosts[clientType];
		final double d1=costFactors[0]*waiting+client.waitingAdditionalCosts;
		if (d1!=0.0) ((StatisticsValuePerformanceIndicator)statistics.clientsCostsWaiting.get(name)).add(d1);
		final double d2=costFactors[1]*transfer+client.transferAdditionalCosts;
		if (d2!=0.0) ((StatisticsValuePerformanceIndicator)statistics.clientsCostsTransfer.get(name)).add(d2);
		final double d3=costFactors[2]*process+client.processAdditionalCosts;
		if (d3!=0.0) ((StatisticsValuePerformanceIndicator)statistics.clientsCostsProcess.get(name)).add(d3);

		/* Werte der Eigenschaften erfassen */
		client.writeUserDataToStatistics(statistics.clientData);

		/* Pfad erfassen */
		client.storePathToStatistics(simData);

		/* Zwischenabgangszeiten erfassen */
		if (cacheClientsInterleaveTime==null) {
			cacheClientsInterleaveTime=new RunData.IndicatorAccessCacheClientTypes(simData.statistics.clientsInterleavingTime,simData.runModel.clientTypes);
			lastInterLeaveByClientType=new long[simData.runModel.clientTypes.length];
		}
		final long last=lastInterLeaveByClientType[clientType];
		final long now=simData.currentTime;
		if (last>0 && last<=now) ((StatisticsDataPerformanceIndicator)cacheClientsInterleaveTime.get(clientType)).add((now-last)*scale);
		lastInterLeaveByClientType[clientType]=now;
	}

	/**
	 * Nimmt das Kundendatenobjekt am Ende seiner Lebensdauer in der Simulation wieder in den Cache auf
	 * und erfasst dabei die gesammelten Daten in der Statistik
	 * @param client	Kundendatenobjekt, welches erfasst werden soll
	 * @param simData	Simulationsdaten
	 */
	public void disposeClient(final RunDataClient client, final SimulationData simData) {
		/* Bereiche benachrichtigen, dass der Kunde sich nicht mehr in diesen befindet. */
		client.leaveAllSections(simData);

		/* Alle noch offenen Logik-Bereichsinformationen löschen. */
		client.leaveAllLogic();

		/* Kenngrößen des Kunden in der Statistik erfassen */
		final List<RunDataClient> batch=client.dissolveBatch();
		if (batch==null) {
			/* Einzelner Kunde */
			if (!client.isWarmUp && client.inStatistics) logClientData(client,simData);
		} else {
			/* Batch */
			for (RunDataClient sub: batch) disposeClient(sub,simData);
		}

		/* Testen, ob Ziel-Batch-Means-Konfidenzradius für die Wartezeiten erreicht ist */
		if (simData.runModel.terminationWaitingTimeConfidenceHalfWidth>0) {
			if (testStopByConfidenceLevel(simData)) {
				if (simData.loggingActive) simData.logEventExecution(Language.tr("Simulation.Log.EndOfSimulation"),-1,String.format(Language.tr("Simulation.Log.EndOfSimulation.Confidence"),NumberTools.formatNumber(simData.runModel.terminationWaitingTimeConfidenceHalfWidth),NumberTools.formatNumber(1-simData.runModel.terminationWaitingTimeConfidenceLevel)));
				simData.doShutDown();
			}
		}

		/* Kundenobjekt freigaben bzw. cachen */
		disposeClientWithoutStatistics(client,simData);
	}

	private long testStopCounter=0;

	private boolean testStopByConfidenceLevel(final SimulationData simData) {
		testStopCounter++;
		if (testStopCounter%500!=0) return false;

		final StatisticsDataPerformanceIndicator indicator=simData.statistics.clientsAllWaitingTimes;
		if (indicator.getBatchCount()<2) return false;

		final double halfWidth=simData.runModel.terminationWaitingTimeConfidenceHalfWidth;
		final double alpha=simData.runModel.terminationWaitingTimeConfidenceLevel;
		if (halfWidth<=0 || alpha<=0 || alpha>=1) return false;

		return indicator.getBatchMeanConfidenceHalfWideWithoutFinalize(1-alpha)<=halfWidth;
	}

	/**
	 * Nimmt das Kundendatenobjekt am Ende seiner Lebensdauer in der Simulation wieder in den Cache auf
	 * erfasst es aber nicht in der Statistik. Dies ist für Batch-Kunden, die vor dem Ende der Lebensdauer
	 * von einem Element zerlegt wurden, von Bedeutung.
	 * @param client	Kundendatenobjekt, welches erfasst werden soll
	 * @param simData	Simulationsdaten
	 */
	public void disposeClientWithoutStatistics(final RunDataClient client, final SimulationData simData) {
		if (clientsInUse!=null) clientsInUse.remove(client);
		if (clientsInUseFast!=null) clientsInUseFast.remove(client);

		/* Client-Objekt evtl. im Cache aufheben */
		if (clientCacheUsed<CLIENT_CACHE_SIZE) {
			clientCache[clientCacheUsed]=client;
			clientCacheUsed++;
		}

		/* Erfassung Anzahl an Kunden im System */
		clientsInSystem--;
		if (!simData.runData.isWarmUp) {
			simData.statistics.clientsInSystem.set(simData.currentTime*scale,clientsInSystem);
		}
		simData.runData.logClientsInSystemChange(simData,client.type,-1);
	}

	/**
	 * Entfernt alle Kunden aus dem Simulation (entspricht dem Aufruf von <code>disposeClient</code> für alle Kunden.
	 * Diese Methode funktioniert nur, wenn zu Beginn der Simulation <code>requestClientsInUseList</code> aufgerufen
	 * wurde; ansonsten gibt es keine globale Liste der aktiven Kunden. Normalerweise wird <code>requestClientsInUseList</code>
	 * nur während der Animation eines Modells aufgerufen.
	 * @param simData	Simulationsdaten
	 */
	public void disposeAll(final SimulationData simData) {
		if (clientsInUse!=null) {
			while (!clientsInUse.isEmpty()) disposeClient(clientsInUse.get(0),simData);
		} else {
			if (clientsInUseFast!=null) {
				final RunDataClient[] list=clientsInUseFast.toArray(new RunDataClient[0]);
				for (RunDataClient client: list) disposeClient(client,simData);
				clientsInUseFast.clear();
			}
		}
	}

	/**
	 * Liefert eine sich laufend aktualisierende Liste mit allen in der Simulation aktiven Kunden.<br>
	 * Diese Liste wird erst ab dem Zeitpunkt des Aufrufs dieser Methode geführt und sollte nur
	 * während der Animation verwendet werden, da sie die Simulation bremst und ansonsten nicht verwendet wird.
	 * @return	Synchronisierte Liste mit allen Kunden im System
	 */
	public List<RunDataClient> requestClientsInUseList() {
		if (clientsInUse==null) clientsInUse=new Vector<>();
		return clientsInUse;
	}

	/**
	 * Erfassung der aktuellen Kunden im System aktivieren, um so
	 * später alle aus dem System entfernen (und in der Statistik
	 * erfassen) zu können.
	 * @see #disposeAll(SimulationData)
	 */
	public void requestFastClientsInUseList() {
		if (clientsInUseFast==null) clientsInUseFast=new HashSet<>();
	}
}
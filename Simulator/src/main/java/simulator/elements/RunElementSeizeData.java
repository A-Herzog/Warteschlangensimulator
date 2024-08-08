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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import language.Language;
import simulator.coreelements.RunElementData;
import simulator.events.SeizeWaitingCancelEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;

/**
 * Laufzeitdaten eines {@link RunElementSeize}-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementSeize
 * @see RunElementData
 */
public class RunElementSeizeData extends RunElementData {
	/** Liste mit den momentan an der Station wartenden Kunden */
	public final Deque<RunDataClient> waitingClients;

	/** Liste der Warteabbruch-Ereignisse (um diese ggf. unbearbeitet löschen zu können) */
	public final Map<RunDataClient,SeizeWaitingCancelEvent> waitingCancelEvents;

	/** Zu dem Datenobjekt zugehöriges {@link RunElementSeize}-Element */
	private final RunElementSeize station;

	/**
	 * Zählung, wie viele Ressourcen momentan durch dieses Element geblockt sind,
	 * damit durch {@link RunElementRelease} nicht mehr freigegeben werden können,
	 * als hier blockiert wurden.
	 */
	public int blockedRessourcesCount;

	/** Formel zur Ermittlung der Ressourcenpriorität dieser Station */
	public final ExpressionCalc resourcePriority;

	/**
	 * Konstruktor der Klasse {@link RunElementSeizeData}
	 * @param station	Zu dem Datenobjekt zugehöriges {@link RunElementSeize}-Element
	 * @param variableNames	Liste der global verfügbaren Variablennamen
	 * @param simData	Simulationsdatenobjekt
	 */
	public RunElementSeizeData(final RunElementSeize station, final String[] variableNames, final SimulationData simData) {
		super(station,simData);
		waitingClients=new ArrayDeque<>();
		waitingCancelEvents=new HashMap<>();
		blockedRessourcesCount=0;

		this.station=station;

		resourcePriority=new ExpressionCalc(variableNames,simData.runModel.modelUserFunctions);
		resourcePriority.parse(station.resourcePriority);
	}

	/**
	 * Fügt einen Kunden zu der Liste der wartenden Kunden hinzu
	 * @param client	Hinzuzufügender Kunde
	 * @param time	Zeitpunkt an dem der Kunde an der <code>RunElementSeize</code>-Station eingetroffen ist (zur späteren Berechnung der Wartezeit der Kunden)
	 * @param simData	Simulationsdatenobjekt
	 */
	public void addClientToQueue(final RunDataClient client, final long time, final SimulationData simData) {
		/* Kunden an Warteschlange anstellen */
		waitingClients.offer(client);
		client.lastWaitingStart=time;

		/* Logging */
		if (simData.loggingActive) station.log(simData,Language.tr("Simulation.Creator.SeizeArrival"),String.format(Language.tr("Simulation.Creator.SeizeArrival.Info"),client.logInfo(simData),station.name));

		/* Statistik */
		simData.runData.logClientEntersStationQueue(simData,station,this,client);
	}

	/**
	 * Entfernt einen Kunden aus der Warteschlange und reduziert den Zähler der belegten Ressourcen.
	 * @param simData	Simulationsdatenobjekt
	 * @return	Gibt das Kundenobjekt zurück
	 */
	public RunDataClient removeClientFromQueue(final SimulationData simData) {
		RunDataClient client=waitingClients.poll();
		if (client==null) return null;

		/* Geblockte Ressourcen hochzählen */
		blockedRessourcesCount++;

		/* Statistik */
		simData.runData.logClientLeavesStationQueue(simData,station,this,client);

		return client;
	}

	/**
	 * Entfernt einen Kunden aus der Warteschlange, der das Warten aufgegeben hat.
	 * @param simData	Simulationsdatenobjekt
	 * @param client	Zu entfernender Kunde
	 * @return	Gibt <code>true</code> zurück, wenn sich der Kunde in der Warteschlange befand und entfernt werden konnte
	 */
	public boolean removeClientFromQueueForCancelation(final SimulationData simData, final RunDataClient client) {
		if (!waitingClients.remove(client)) return false;

		/* Statistik */
		simData.runData.logClientLeavesStationQueue(simData,station,this,client);

		return true;
	}
}

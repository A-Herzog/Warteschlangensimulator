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

import language.Language;
import simulator.coreelements.RunElementData;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;

/**
 * Laufzeitdaten eines <code>RunElementSeize</code>-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementSeize
 * @see RunElementData
 */
public class RunElementSeizeData extends RunElementData {
	/** Liste mit den momentan an der Station wartenden Kunden */
	public final Deque<RunDataClient> waitingClients;

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
	 * Konstruktor der Klasse <code>RunElementSeizeData</code>
	 * @param station	Zu dem Datenobjekt zugehöriges <code>RunElementSeize</code>-Element
	 * @param variableNames	Liste der global verfügbaren Variablennamen
	 */
	public RunElementSeizeData(final RunElementSeize station, final String[] variableNames) {
		super(station);
		waitingClients=new ArrayDeque<>();
		blockedRessourcesCount=0;

		this.station=station;

		resourcePriority=new ExpressionCalc(variableNames);
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
	 * Entfernt einen Kunden aus der Warteschlange
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
}

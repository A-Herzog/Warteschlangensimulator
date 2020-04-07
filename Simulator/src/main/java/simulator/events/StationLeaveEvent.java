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
package simulator.events;

import language.Language;
import simcore.Event;
import simcore.SimData;
import simulator.coreelements.RunElement;
import simulator.coreelements.RunElementData;
import simulator.elements.RunElementSub;
import simulator.elements.RunElementSubConnect;
import simulator.runmodel.RunData;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.SimulationData;

/**
 * Abgang eines Kunden aus einer Station
 * @author Alexander Herzog
 */
public class StationLeaveEvent extends Event {
	/**
	 * Kunde der die Station verlässt
	 */
	public RunDataClient client;

	/**
	 * Station die verlassen wird
	 */
	public RunElement station;

	private boolean statistics;

	@Override
	public void run(SimData data) {
		final SimulationData simData=(SimulationData)data;

		/* Logging */
		if (simData.loggingActive) station.log(simData,Language.tr("Simulation.Log.LeaveStation"),String.format(Language.tr("Simulation.Log.LeaveStation.Info"),client.logInfo(simData),station.name));

		if (statistics) {
			/* Zählung Kunden an Station */
			if (station.isClientCountStation()) simData.runData.logClientLeavesStation(simData,station,null);
		}

		/* Verarbeitung */
		station.processLeave(simData,client);

		/* System über Status-Änderung benachrichtigen */
		simData.runData.fireStateChangeNotify(simData);
	}

	/**
	 * Sendet einen Kunden an die nächste Station<br>
	 * Es wird am Ende der Verarbeitung immer {@link RunData#fireStateChangeNotify(SimulationData)} ausgelöst.
	 * @param simData	Simulationsdatenobjekt
	 * @param client	Kunde der an eine neue Station gesendet werden soll
	 * @param lastStation	Ausgangsstation
	 * @param nextStation	Nächste Station
	 */
	public static void sendToStation(final SimulationData simData, final RunDataClient client, final RunElement lastStation, final RunElement nextStation) {
		sendToStation(simData,client,lastStation,nextStation,true);
	}

	/**
	 * Sendet einen Kunden an die nächste Station
	 * @param simData	Simulationsdatenobjekt
	 * @param client	Kunde der an eine neue Station gesendet werden soll
	 * @param lastStation	Ausgangsstation
	 * @param nextStation	Nächste Station
	 * @param fireChangedEvent	Wird hier <code>true</code> übergeben, so wird am Ende der Verarbeitung {@link RunData#fireStateChangeNotify(SimulationData)} ausgelöst.
	 */
	public static void sendToStation(final SimulationData simData, final RunDataClient client, final RunElement lastStation, final RunElement nextStation, final boolean fireChangedEvent) {
		client.lastStationID=(lastStation==null)?-1:lastStation.id;
		client.nextStationID=nextStation.id;

		final RunData runData=simData.runData;

		/* Logging */
		if (simData.loggingActive) nextStation.log(simData,Language.tr("Simulation.Log.ArriveStation"),String.format(Language.tr("Simulation.Log.ArriveStation.Info"),client.logInfo(simData),nextStation.name));

		/* Zwischenabgangszeiten in der Statistik erfassen */
		runData.logStationLeave(simData.currentTime,simData,lastStation,client);

		/* System über Bewegung des Kunden benachrichtigen */
		runData.fireClientMoveNotify(simData,client,false);

		/* Zwischenankunftszeiten in der Statistik erfassen */
		if (!(lastStation instanceof RunElementSubConnect) || !(nextStation instanceof RunElementSub)) { /* Keine Zwischenankunftszeit usw. wenn der Kunde das Submodell eigentlich gerade wieder verlässt. */
			runData.logStationArrival(simData.currentTime,simData,nextStation,null,client);
		}

		/* Zählung Kunden an Station */
		if (nextStation.isClientCountStation()) runData.logClientEntersStation(simData,nextStation,null);

		/* Verarbeitung */
		client.iconLast=client.icon;
		client.typeLast=client.type;
		client.arrivalProcessedStationID=nextStation.id;
		nextStation.processArrival(simData,client);

		/* System über Status-Änderung benachrichtigen */
		if (fireChangedEvent) runData.fireStateChangeNotify(simData);
	}

	/**
	 * Sendet einen Kunden per Transporter-Objekt an die nächste Station
	 * @param simData	Simulationsdatenobjekt
	 * @param client	Kunde der an eine neue Station gesendet werden soll
	 * @param lastStation	Ausgangsstation
	 * @param nextStation	Nächste Station
	 */
	public static void sendToStationByTransporter(final SimulationData simData, final RunDataClient client, final RunElement lastStation, final RunElement nextStation) {
		client.lastStationID=(lastStation==null)?-1:lastStation.id;
		client.nextStationID=nextStation.id;

		/* Logging */
		if (simData.loggingActive) nextStation.log(simData,Language.tr("Simulation.Log.ArriveStation"),String.format(Language.tr("Simulation.Log.ArriveStation.Info"),client.logInfo(simData),nextStation.name));

		/* Zwischenabgangszeiten in der Statistik erfassen */
		simData.runData.logStationLeave(simData.currentTime,simData,lastStation,client);

		/* System über Bewegung des Kunden benachrichtigen */
		simData.runData.fireClientMoveNotify(simData,client,true);

		/* Zwischenankunftszeiten in der Statistik erfassen */
		if (!(lastStation instanceof RunElementSubConnect) || !(nextStation instanceof RunElementSub)) { /* Keine Zwischenankunftszeit usw. wenn der Kunde das Submodell eigentlich gerade wieder verlässt. */
			simData.runData.logStationArrival(simData.currentTime,simData,nextStation,null,client);
		}

		/* Zählung Kunden an Station */
		if (nextStation.isClientCountStation()) simData.runData.logClientEntersStation(simData,nextStation,null);

		/* Verarbeitung */
		client.iconLast=client.icon;
		client.typeLast=client.type;
		client.arrivalProcessedStationID=nextStation.id;
		nextStation.processArrival(simData,client);

		/* System über Status-Änderung benachrichtigen */
		simData.runData.fireStateChangeNotify(simData);
	}

	/**
	 * Sendet mehrere Kunden an die jeweils nächsten Stationen<br>
	 * Es wird am Ende der Verarbeitung immer {@link RunData#fireStateChangeNotify(SimulationData)} ausgelöst.
	 * @param simData	Simulationsdatenobjekt
	 * @param client	Kunden die an eine neue Station gesendet werden sollen
	 * @param lastStation	Ausgangsstationen
	 * @param nextStation	Nächste Stationen
	 */
	public static void multiSendToStation(final SimulationData simData, final RunDataClient[] client, final RunElement lastStation, final RunElement[] nextStation) {

		/* Erst Move...*/
		for (int i=0;i<client.length;i++) {
			client[i].lastStationID=(lastStation==null)?-1:lastStation.id;
			client[i].nextStationID=nextStation[i].id;

			/* Logging */
			if (simData.loggingActive) nextStation[i].log(simData,Language.tr("Simulation.Log.ArriveStation"),String.format(Language.tr("Simulation.Log.ArriveStation.Info"),client[i].logInfo(simData),nextStation[i].name));

			/* Zwischenabgangszeiten in der Statistik erfassen */
			simData.runData.logStationLeave(simData.currentTime,simData,lastStation,client[i]);

			/* System über Bewegung des Kunden benachrichtigen */
			simData.runData.fireClientMoveNotify(simData,client[i],false);
		}

		/* ...dann Arrival */
		for (int i=0;i<client.length;i++) {
			/* Zwischenankunftszeiten in der Statistik erfassen */
			simData.runData.logStationArrival(simData.currentTime,simData,nextStation[i],null,client[i]);

			/* Zählung Kunden an Station */
			if (nextStation[i].isClientCountStation()) simData.runData.logClientEntersStation(simData,nextStation[i],null);

			/* Verarbeitung */
			client[i].iconLast=client[i].icon;
			client[i].typeLast=client[i].type;
			client[i].arrivalProcessedStationID=nextStation[i].id;
			nextStation[i].processArrival(simData,client[i]);
		}

		/* System über Status-Änderung benachrichtigen */
		simData.runData.fireStateChangeNotify(simData);
	}

	/**
	 * Sendet mehrere Kunden an jeweils eine gemeinsame nächste Station<br>
	 * Es wird am Ende der Verarbeitung immer {@link RunData#fireStateChangeNotify(SimulationData)} ausgelöst.
	 * @param simData	Simulationsdatenobjekt
	 * @param client	Kunden die an eine neue Station gesendet werden sollen
	 * @param lastStation	Ausgangsstation
	 * @param nextStation	Nächste Station
	 */
	public static void multiSendToStation(final SimulationData simData, final RunDataClient[] client, final RunElement lastStation, final RunElement nextStation) {

		/* Erst Move...*/
		for (int i=0;i<client.length;i++) {

			client[i].lastStationID=(lastStation==null)?-1:lastStation.id;
			client[i].nextStationID=nextStation.id;

			/* Logging */
			if (simData.loggingActive) nextStation.log(simData,Language.tr("Simulation.Log.ArriveStation"),String.format(Language.tr("Simulation.Log.ArriveStation.Info"),client[i].logInfo(simData),nextStation.name));

			/* Zwischenabgangszeiten in der Statistik erfassen */
			simData.runData.logStationLeave(simData.currentTime,simData,lastStation,client[i]);

			/* System über Bewegung des Kunden benachrichtigen */
			simData.runData.fireClientMoveNotify(simData,client[i],false);
		}

		/* ...dann Arrival */
		for (int i=0;i<client.length;i++) {
			/* Zwischenankunftszeiten in der Statistik erfassen */
			simData.runData.logStationArrival(simData.currentTime,simData,nextStation,null,client[i]);

			/* Zählung Kunden an Station */
			if (nextStation.isClientCountStation()) simData.runData.logClientEntersStation(simData,nextStation,null);

			/* Verarbeitung */
			client[i].iconLast=client[i].icon;
			client[i].typeLast=client[i].type;
			client[i].arrivalProcessedStationID=nextStation.id;
			nextStation.processArrival(simData,client[i]);
		}

		/* System über Status-Änderung benachrichtigen */
		simData.runData.fireStateChangeNotify(simData);
	}

	/**
	 * Legt ein {@link StationLeaveEvent}-Ereignis an.<br>
	 * Bei der Ausführung des Ereignisses wird {@link RunElement#isClientCountStation()} geprüft und dann ggf. {@link RunData#logClientLeavesStation(SimulationData, RunElement, RunElementData)} aufgerufen.
	 * @param simData	Simulationsdatenobjekt
	 * @param client	Kunde der die Station verlassen soll
	 * @param station	Station die verlassen werden soll
	 * @param timeDelta	Zeitpunkt (in Millisekunden ab der aktuellen Zeit) an der das Ereignis ausgeführt werden soll
	 */
	public static void addLeaveEvent(final SimulationData simData, final RunDataClient client, final RunElement station, final long timeDelta) {
		/* addLeaveEvent(simData,client,station,timeDelta,true); - true = logStationLeave bei Ausführung des Events */

		/* Station-Event-Objekt holen */
		final StationLeaveEvent leaveStation=(StationLeaveEvent)simData.getEvent(StationLeaveEvent.class);

		/* ... und initialisieren */
		leaveStation.init(simData.currentTime+timeDelta);

		/* Konfiguration in Element eintragen */
		leaveStation.station=station;
		leaveStation.client=client;
		leaveStation.statistics=true;

		/* Zur Ereignisliste hinzufügen */
		simData.eventManager.addEvent(leaveStation);
	}

	/**
	 * Legt ein {@link StationLeaveEvent}-Ereignis an.
	 * @param simData	Simulationsdatenobjekt
	 * @param client	Kunde der die Station verlassen soll
	 * @param station	Station die verlassen werden soll
	 * @param timeDelta	Zeitpunkt (in Millisekunden ab der aktuellen Zeit) an der das Ereignis ausgeführt werden soll
	 * @param statistics	Wird hier <code>true</code> übergeben, so wird bei der Ausführung des Ereignisses {@link RunElement#isClientCountStation()} geprüft und dann ggf. {@link RunData#logClientLeavesStation(SimulationData, RunElement, RunElementData)} aufgerufen.
	 */
	public static void addLeaveEvent(final SimulationData simData, final RunDataClient client, final RunElement station, final long timeDelta, final boolean statistics) {
		/* Station-Event-Objekt holen */
		final StationLeaveEvent leaveStation=(StationLeaveEvent)simData.getEvent(StationLeaveEvent.class);

		/* ... und initialisieren */
		leaveStation.init(simData.currentTime+timeDelta);

		/* Konfiguration in Element eintragen */
		leaveStation.station=station;
		leaveStation.client=client;
		leaveStation.statistics=statistics;

		/* Zur Ereignisliste hinzufügen */
		simData.eventManager.addEvent(leaveStation);
	}

	/**
	 * Kündigt die Ankunft eines Kunden an der Zielstation an (damit {@link RunElementData#reportedClientsAtStation(SimulationData)} bereits den neuen Wert liefern kann)
	 * @param simData	Simulationdatenobjekt
	 * @param client	Kunde, der verschickt wird
	 * @param nextStation	Station, an die der Kunde geschickt wird
	 * @see RunElementData#announceClient(SimulationData, RunDataClient)
	 * @see RunElementData#reportedClientsAtStation(SimulationData)
	 */
	public static void announceClient(final SimulationData simData, final RunDataClient client, final RunElement nextStation) {
		final RunElementData data=nextStation.getData(simData);
		data.announceClient(simData,client);
		client.isAnnouncedToStation=true;
	}

	/**
	 * Trägt den Kunden wieder aus der Liste der angekündigten Ankünfte aus (weil er jetzt wirklich geschickt wird)
	 * @param simData	Simulationdatenobjekt
	 * @param client	Kunde, der verschickt wird
	 * @param nextStation	Station, an die der Kunde geschickt wird
	 * @see StationLeaveEvent#announceClient(SimulationData, RunDataClient, RunElement)
	 * @see RunElementData#unannounceClient(SimulationData, RunDataClient)
	 * @see RunElementData#reportedClientsAtStation(SimulationData)
	 */
	public static void unannounceClient(final SimulationData simData, final RunDataClient client, final RunElement nextStation) {
		if (client.isAnnouncedToStation) {
			final RunElementData data=nextStation.getData(simData);
			data.unannounceClient(simData,client);
			client.isAnnouncedToStation=false;
		}
	}
}

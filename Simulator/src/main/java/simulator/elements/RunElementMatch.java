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

import java.util.ArrayList;
import java.util.List;

import language.Language;
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElementPassThrough;
import simulator.editmodel.EditModel;
import simulator.events.StationLeaveEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionMultiEval;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.BatchRecord;
import ui.modeleditor.elements.ModelElementEdge;
import ui.modeleditor.elements.ModelElementMatch;
import ui.modeleditor.elements.ModelElementMatch.MatchPropertyMode;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementMatch</code>
 * @author Alexander Herzog
 * @see ModelElementMatch
 */
public class RunElementMatch extends RunElementPassThrough implements StateChangeListener {
	/**
	 * Array mit den einlaufenden Kanten in der ersten Ebene
	 * und den IDs der Stationen, die über die jeweiligen Pfade
	 * zu der Station führen in der zweiten Ebene.
	 * @see #getClientQueueNumber(RunDataClient)
	 */
	private int[][] connectionIn;

	/**
	 * Von welcher Art ist die Eigenschaft, die zwischen den wartenden Kunden abgeglichen werden soll?
	 */
	private ModelElementMatch.MatchPropertyMode matchPropertyMode;

	/**
	 * Kundendateneigenschaft zum Abgleich im Modus
	 * @see MatchPropertyMode#NUMBER
	 */
	private int matchPropertyNumberIndex;

	/**
	 * Kundendateneigenschaft zum Abgleich im Modus
	 * @see MatchPropertyMode#TEXT
	 */
	private String matchPropertyString;

	/** Batch-Bildungs-Modus */
	private ModelElementMatch.MatchMode batchMode;
	/** Index des neuen Batch-Kundentyps (bei der temporären oder permanenten Batch-Bildung) */
	private int newClientType;

	/** Bedingung, die für eine Weitergabe der Kunden erfüllt sein muss */
	private String condition;

	/** Wie sollen die Zeiten der Einzelkunden bei der Batch-Bildung auf den neuen Batch-Kunden übertragen werden? */
	private BatchRecord.DataTransferMode transferTimes;
	/** Wie sollen die numerischen Datenfelder der Einzelkunden bei der Batch-Bildung auf den neuen Batch-Kunden übertragen werden? */
	private BatchRecord.DataTransferMode transferNumbers;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementMatch(final ModelElementMatch element) {
		super(element,buildName(element,Language.tr("Simulation.Element.Match.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementMatch)) return null;
		final ModelElementMatch matchElement=(ModelElementMatch)element;
		final RunElementMatch match=new RunElementMatch(matchElement);

		/* Auslaufende Kante */
		final String edgeError=match.buildEdgeOut(matchElement);
		if (edgeError!=null) return edgeError;

		/* Einlaufende Kanten */
		final ModelElementEdge[] edges=matchElement.getEdgesIn();
		if (edges.length==0) return String.format(Language.tr("Simulation.Creator.NoEdgeIn"),element.getId());
		if (edges.length<2) return String.format(Language.tr("Simulation.Creator.OnlyOneEdgeIn"),element.getId());
		List<List<Integer>> list=new ArrayList<>();
		for (int i=0;i<edges.length;i++) {
			final List<Integer> connectionInId=findPreviousId(edges[i]);
			if (connectionInId.size()==0) return String.format(Language.tr("Simulation.Creator.InputEdgeWithoutConnection"),i+1,element.getId());
			for (Integer newConnect: connectionInId) for (List<Integer> otherQueue: list) for (Integer otherConnect: otherQueue) if (otherConnect.equals(newConnect)) return String.format(Language.tr("Simulation.Creator.MultiEdgesToSameTarget"),newConnect.intValue(),element.getId());
			list.add(connectionInId);
		}

		match.connectionIn=new int[list.size()][];
		for (int i=0;i<list.size();i++) {
			final List<Integer> sub=list.get(i);
			match.connectionIn[i]=new int[sub.size()];
			for (int j=0;j<sub.size();j++) match.connectionIn[i][j]=sub.get(j);
		}

		/* Abgleich über Eigenschaften */
		match.matchPropertyMode=matchElement.getMatchPropertyMode();
		switch (match.matchPropertyMode) {
		case NONE:
			/* Keine Einstellungen */
			break;
		case NUMBER:
			match.matchPropertyNumberIndex=matchElement.getMatchPropertyNumber();
			if (match.matchPropertyNumberIndex<0) return String.format(Language.tr("Simulation.Creator.InvalidMatchPropertyIndex"),element.getId(),match.matchPropertyNumberIndex);
			break;
		case TEXT:
			match.matchPropertyString=matchElement.getMatchPropertyString();
			if (match.matchPropertyString.trim().isEmpty()) return String.format(Language.tr("Simulation.Creator.InvalidMatchPropertyText"),element.getId());
			break;

		}

		/* Match-Modus */
		match.batchMode=matchElement.getMatchMode();
		if (match.batchMode==ModelElementMatch.MatchMode.MATCH_MODE_COLLECT) {
			match.newClientType=-1;
		} else {
			match.newClientType=runModel.getClientTypeNr(matchElement.getNewClientType());
			if (match.newClientType<0) return String.format(Language.tr("Simulation.Creator.InvalidBatchClientType"),element.getId(),matchElement.getNewClientType());
		}

		/* Bedingung */
		final String condition=matchElement.getCondition();
		if (condition==null || condition.trim().isEmpty()) {
			match.condition=null;
		} else {
			final int error=ExpressionMultiEval.check(condition,runModel.variableNames,runModel.modelUserFunctions);
			if (error>=0) return String.format(Language.tr("Simulation.Creator.MatchCondition"),condition,element.getId(),error+1);
			match.condition=condition;
		}

		/* Daten zusammenführen */
		match.transferTimes=matchElement.getTransferTimes();
		match.transferNumbers=matchElement.getTransferNumbers();

		return match;
	}

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementMatch)) return null;
		final ModelElementMatch matchElement=(ModelElementMatch)element;

		/* Auslaufende Kante */
		final RunModelCreatorStatus edgeError=testEdgeOut(matchElement);
		if (edgeError!=null) return edgeError;

		/* Einlaufende Kanten */
		final ModelElementEdge[] edges=matchElement.getEdgesIn();
		if (edges.length==0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.NoEdgeIn"),element.getId()));
		if (edges.length<2) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.OnlyOneEdgeIn"),element.getId()));
		List<List<Integer>> list=new ArrayList<>();
		for (int i=0;i<edges.length;i++) {
			final List<Integer> connectionInId=findPreviousId(edges[i]);
			if (connectionInId.size()==0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.InputEdgeWithoutConnection"),i+1,element.getId()));
			for (Integer newConnect: connectionInId) for (List<Integer> otherQueue: list) for (Integer otherConnect: otherQueue) if (otherConnect.equals(newConnect)) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.MultiEdgesToSameTarget"),newConnect.intValue(),element.getId()));
			list.add(connectionInId);
		}

		return RunModelCreatorStatus.ok;
	}

	@Override
	public RunElementMatchData getData(final SimulationData simData) {
		RunElementMatchData data;
		data=(RunElementMatchData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementMatchData(this,connectionIn.length,condition,simData.runModel.variableNames,simData);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	/**
	 * Löst die Freigabe von Kunden aus.
	 * Modus: Kunden einfach gemeinsam weiterleiten
	 * @param simData	Simulationsdaten
	 * @param data	Thread-lokales Datenobjekt zu der Station
	 * @param newClient	Aktuell gerade eingetroffener Kunde (kann <code>null</code> sein)
	 * @param newClientQueueNumber	Index der Teilwarteschlange an der der Kunde eingetroffen ist
	 * @param selectQueuedClients	Indices der zu sendenden Kunden in den Teilwarteschlangen (jeweils ein Eintrag pro Teilwarteschlange)
	 * @see ui.modeleditor.elements.ModelElementMatch.MatchMode#MATCH_MODE_COLLECT
	 */
	private void processSendMultipleClients(final SimulationData simData, final RunElementMatchData data, final RunDataClient newClient, final int newClientQueueNumber, final int[] selectQueuedClients) {
		StringBuilder sb=null;
		/* Logging */
		if (simData.loggingActive) {
			sb=new StringBuilder();
			if (newClient!=null) sb.append(Language.tr("Simulation.Log.MatchNewClientID")+": "+newClient.logInfo(simData));
		}

		/* Wenn es sich um eine Ankunft handelt und einer der Kunden überhaupt nicht in der Warteschlange war... */
		if (newClientQueueNumber>=0) {
			data.moveClientsList[newClientQueueNumber]=newClient;

			/* Wartezeit in Statistik */
			simData.runData.logStationProcess(simData,this,newClient,0,0,0,0);

			/* Ist notwendig, damit die Anzahl-Zählung für die Warteschlange stimmt; sonst kann es sein, dass für bestimmte Kundentypen überhaupt keine Daten hinterlegt sind, was beim Zusammenführen der Multi-Thread-Statistik zu Fehlern führen kann. */
			simData.runData.logClientEntersStationQueue(simData,this,data,newClient);
			simData.runData.logClientLeavesStationQueue(simData,this,data,newClient);
		}

		/* Kunden weiterleiten */
		for (int i=0;i<data.waitingClients.length;i++) {
			if (i==newClientQueueNumber) continue;

			/* Kunden aus Warteschlange holen */
			final RunDataClient waitingClient=data.waitingClients[i].remove(selectQueuedClients[i]);

			/* Kunden weiterleiten */
			data.moveClientsList[i]=waitingClient;

			/* Wartezeit in Statistik */
			final long waitingTime=simData.currentTime-waitingClient.lastWaitingStart;
			simData.runData.logStationProcess(simData,this,waitingClient,waitingTime,0,0,waitingTime);
			waitingClient.addStationTime(id,waitingTime,0,0,waitingTime);

			/* Kunden an Station in Statistik */
			simData.runData.logClientLeavesStationQueue(simData,this,data,waitingClient);

			/* Logging */
			if (simData.loggingActive && sb!=null) sb.append(", "+Language.tr("Simulation.Log.MatchWaitingClientID")+": "+waitingClient.logInfo(simData));
		}

		/* Logging */
		if (simData.loggingActive && sb!=null) {
			StringBuilder sb2=new StringBuilder();
			for (List<RunDataClient> queue: data.waitingClients) {if (sb2.length()!=0) sb2.append(" / "); sb2.append(queue.size());}
			log(simData,Language.tr("Simulation.Log.MatchWaitingForward"),String.format(Language.tr("Simulation.Log.MatchWaitingForward.Info"),sb.toString(),name,sb2.toString()));
		}

		/* Für jeden Kunden muss ein Leave-Event generiert werden. Aber im Leave-Event wird dann nur für den letzten Kunden (daher die Sammlung in data.moveClientsList) ein Move-Event generiert. */
		for (RunDataClient client: data.moveClientsList) StationLeaveEvent.addLeaveEvent(simData,client,this,0);
		data.moveNr=0;
	}

	/**
	 * Löst die Freigabe von Kunden aus.
	 * Modus: Temporären Batch bilden
	 * @param simData	Simulationsdaten
	 * @param data	Thread-lokales Datenobjekt zu der Station
	 * @param newClient	Aktuell gerade eingetroffener Kunde (kann <code>null</code> sein)
	 * @param newClientQueueNumber	Index der Teilwarteschlange an der der Kunde eingetroffen ist
	 * @param selectQueuedClients	Indices der zu sendenden Kunden in den Teilwarteschlangen (jeweils ein Eintrag pro Teilwarteschlange)
	 * @see ui.modeleditor.elements.ModelElementMatch.MatchMode#MATCH_MODE_TEMPORARY
	 */
	private void processSendTemporaryBatchedClients(final SimulationData simData, final RunElementMatchData data, final RunDataClient newClient, final int newClientQueueNumber, final int[] selectQueuedClients) {
		/* Neuen Kunden anlegen */
		final RunDataClient batchedClient=simData.runData.clients.getClient(newClientType,simData,id);

		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.MatchNewClient"),String.format(Language.tr("Simulation.Log.MatchNewClient.Info"),batchedClient.logInfo(simData),name));

		StringBuilder sb=null;
		/* Logging */
		if (simData.loggingActive) {
			sb=new StringBuilder();
			if (newClient!=null) sb.append(Language.tr("Simulation.Log.MatchNewClientID")+": "+newClient.logInfo(simData));
		}

		/* Listenobjekt aus Cache holen (wenn es verwendet werden soll) */
		List<RunDataClient> processedClients=null;
		if (transferTimes!=BatchRecord.DataTransferMode.OFF || transferNumbers!=BatchRecord.DataTransferMode.OFF) processedClients=data.processedClientsListCache;

		/* Wenn es sich um eine Ankunft handelt und einer der Kunden überhaupt nicht in der Warteschlange war... */
		if (newClientQueueNumber>=0 && newClient!=null) {
			/* Wartezeit in Statistik */
			simData.runData.logStationProcess(simData,this,newClient,0,0,0,0);

			/* Ist notwendig, damit die Anzahl-Zählung für die Warteschlange stimmt; sonst kann es sein, dass für bestimmte Kundentypen überhaupt keine Daten hinterlegt sind, was beim Zusammenführen der Multi-Thread-Statistik zu Fehlern führen kann. */
			simData.runData.logClientEntersStationQueue(simData,this,data,newClient);
			simData.runData.logClientLeavesStationQueue(simData,this,data,newClient);

			/* Ist der Kunde als "letzter Kunde" markiert? */
			if (newClient.isLastClient) batchedClient.isLastClient=true;

			/* Kunde in Batch aufnehmen */
			batchedClient.addBatchClient(newClient);

			if (processedClients!=null) processedClients.add(newClient);
		}

		for (int i=0;i<data.waitingClients.length;i++) {
			final RunDataClient currentClient;

			if (i!=newClientQueueNumber) {

				/* Kunden aus Warteschlange holen */
				final RunDataClient waitingClient=data.waitingClients[i].remove(selectQueuedClients[i]);
				currentClient=waitingClient;

				/* Wartezeit in Statistik */
				final long waitingTime=simData.currentTime-waitingClient.lastWaitingStart;
				simData.runData.logStationProcess(simData,this,waitingClient,waitingTime,0,0,waitingTime);
				waitingClient.addStationTime(id,waitingTime,0,0,waitingTime);

				/* Kunden an Station in Statistik */
				simData.runData.logClientLeavesStationQueue(simData,this,data,waitingClient);

				/* Ist der Kunde als "letzter Kunde" markiert? */
				if (waitingClient.isLastClient) batchedClient.isLastClient=true;

				/* Logging */
				if (simData.loggingActive && sb!=null) sb.append(", "+Language.tr("Simulation.Log.MatchWaitingClientID")+": "+waitingClient.logInfo(simData));

				/* Kunde in Batch aufnehmen */
				batchedClient.addBatchClient(waitingClient);

				if (processedClients!=null) processedClients.add(newClient);
			} else {
				currentClient=newClient;
			}

			/* Kunde verlässt Station (wird sonst über die Events realisiert) */
			simData.runData.logClientLeavesStation(simData,this,data,currentClient);
			if (parentId>=0) simData.runData.logClientLeavesStation(simData,simData.runModel.elementsFast[parentId],null,currentClient);
		}

		/* Daten von den alten Kunden auf den neuen Batch-Kunden übertragen */
		if (processedClients!=null) {
			RunElementBatch.transferTimes(transferTimes,processedClients.size(),processedClients,batchedClient);
			RunElementBatch.transferNumbers(transferNumbers,processedClients.size(),processedClients,batchedClient);
			processedClients.clear();
		}

		/* Logging */
		if (simData.loggingActive && sb!=null) {
			StringBuilder sb2=new StringBuilder();
			for (List<RunDataClient> queue: data.waitingClients) {if (sb2.length()!=0) sb2.append(" / "); sb2.append(queue.size());}
			log(simData,Language.tr("Simulation.Log.MatchDisposeClient"),String.format(Language.tr("Simulation.Log.MatchDisposeClient.Info"),sb.toString(),name,sb2.toString()));
		}

		/* Kunde betritt Station (wird sonst über die Events realisiert) */
		simData.runData.logClientEntersStation(simData,this,data,batchedClient);
		if (parentId>=0) simData.runData.logClientEntersStation(simData,simData.runModel.elementsFast[parentId],null,batchedClient);

		/* Maximalzahl an Kunden im System eingehalten */
		if (!simData.testMaxAllowedClientsInSystem()) return;

		/* Kunden weiterleiten */
		StationLeaveEvent.addLeaveEvent(simData,batchedClient,this,0);
	}

	/**
	 * Löst die Freigabe von Kunden aus.
	 * Modus: Permanenten Batch bilden
	 * @param simData	Simulationsdaten
	 * @param data	Thread-lokales Datenobjekt zu der Station
	 * @param newClient	Aktuell gerade eingetroffener Kunde (kann <code>null</code> sein)
	 * @param newClientQueueNumber	Index der Teilwarteschlange an der der Kunde eingetroffen ist
	 * @param selectQueuedClients	Indices der zu sendenden Kunden in den Teilwarteschlangen (jeweils ein Eintrag pro Teilwarteschlange)
	 * @see ui.modeleditor.elements.ModelElementMatch.MatchMode#MATCH_MODE_PERMANENT
	 */
	private void processSendPermanentBatchedClients(final SimulationData simData, final RunElementMatchData data, final RunDataClient newClient, final int newClientQueueNumber, final int[] selectQueuedClients) {
		boolean isLastClient=false;

		StringBuilder sb=null;
		/* Logging */
		if (simData.loggingActive) {
			sb=new StringBuilder();
			if (newClient!=null) sb.append(Language.tr("Simulation.Log.MatchNewClientID")+": "+newClient.logInfo(simData));
		}

		/* Listenobjekt aus Cache holen */
		final List<RunDataClient> processedClients=data.processedClientsListCache;

		/* Wenn es sich um eine Ankunft handelt und einer der Kunden überhaupt nicht in der Warteschlange war... */
		if (newClientQueueNumber>=0 && newClient!=null) {

			/* Wartezeit in Statistik */
			simData.runData.logStationProcess(simData,this,newClient,0,0,0,0);

			/* Ist notwendig, damit die Anzahl-Zählung für die Warteschlange stimmt; sonst kann es sein, dass für bestimmte Kundentypen überhaupt keine Daten hinterlegt sind, was beim Zusammenführen der Multi-Thread-Statistik zu Fehlern führen kann. */
			simData.runData.logClientEntersStationQueue(simData,this,data,newClient);
			simData.runData.logClientLeavesStationQueue(simData,this,data,newClient);

			/* Ist der Kunde als "letzter Kunde" markiert? */
			isLastClient=isLastClient || newClient.isLastClient;
		}

		for (int i=0;i<data.waitingClients.length;i++) {
			final RunDataClient currentClient;

			if (i!=newClientQueueNumber) {

				/* Kunden aus Warteschlange holen */
				final RunDataClient waitingClient=data.waitingClients[i].remove(selectQueuedClients[i]);
				currentClient=waitingClient;

				/* Wartezeit in Statistik */
				final long waitingTime=simData.currentTime-waitingClient.lastWaitingStart;
				simData.runData.logStationProcess(simData,this,waitingClient,waitingTime,0,0,waitingTime);
				waitingClient.addStationTime(id,waitingTime,0,0,waitingTime);

				/* Kunden an Station in Statistik */
				simData.runData.logClientLeavesStationQueue(simData,this,data,waitingClient);

				/* Ist der Kunde als "letzter Kunde" markiert? */
				isLastClient=isLastClient || waitingClient.isLastClient;

				/* Logging */
				if (simData.loggingActive && sb!=null) {
					sb.append(", "+Language.tr("Simulation.Log.MatchWaitingClientID")+": "+waitingClient.logInfo(simData));
				}
			} else {
				currentClient=newClient;
			}

			/* Kunde verlässt Station (wird sonst über die Events realisiert) */
			simData.runData.logClientLeavesStation(simData,this,data,currentClient);
			if (parentId>=0) simData.runData.logClientLeavesStation(simData,simData.runModel.elementsFast[parentId],null,currentClient);

			processedClients.add(currentClient);
		}

		/* Neuen Kunden anlegen */
		final RunDataClient batchedClient=simData.runData.clients.getClient(newClientType,simData,id);
		batchedClient.isLastClient=isLastClient;

		/* Daten von den alten Kunden auf den neuen Batch-Kunden übertragen */
		RunElementBatch.transferTimes(transferTimes,processedClients.size(),processedClients,batchedClient);
		RunElementBatch.transferNumbers(transferNumbers,processedClients.size(),processedClients,batchedClient);

		/* Kunde final in Statistik erfassen und Objekt recyceln */
		for (RunDataClient client: processedClients) simData.runData.clients.disposeClient(client,simData);
		processedClients.clear();

		/* Logging */
		if (simData.loggingActive) {
			if (sb!=null) {
				StringBuilder sb2=new StringBuilder();
				for (List<RunDataClient> queue: data.waitingClients) {if (sb2.length()!=0) sb2.append(" / "); sb2.append(queue.size());}
				log(simData,Language.tr("Simulation.Log.MatchDisposeClient"),String.format(Language.tr("Simulation.Log.MatchDisposeClient.Info"),sb.toString(),name,sb2.toString()));
			}
			log(simData,Language.tr("Simulation.Log.MatchNewClient"),String.format(Language.tr("Simulation.Log.MatchNewClient.Info"),batchedClient.logInfo(simData),name));
		}

		/* Kunde betritt Station (wird sonst über die Events realisiert) */
		simData.runData.logClientEntersStation(simData,this,data,batchedClient);
		if (parentId>=0) simData.runData.logClientEntersStation(simData,simData.runModel.elementsFast[parentId],null,batchedClient);

		/* Maximalzahl an Kunden im System eingehalten */
		if (!simData.testMaxAllowedClientsInSystem()) return;

		/* Kunden weiterleiten */
		StationLeaveEvent.addLeaveEvent(simData,batchedClient,this,0);
	}

	/**
	 * Löst die Freigabe von Kunden aus.
	 * @param simData	Simulationsdaten
	 * @param newClient	Aktuell gerade eingetroffener Kunde (kann <code>null</code> sein; dann muss auch die Warteschlangennummer &lt;0 sein)
	 * @param newClientQueueNumber	Index der Teilwarteschlange an der der Kunde eingetroffen ist (kann &lt;0 sein; dann muss auch der Kunde <code>null</code> sein)
	 * @param selectQueuedClients	Indices der zu sendenden Kunden in den Teilwarteschlangen (jeweils ein Eintrag pro Teilwarteschlange)
	 */
	private void processSendClients(final SimulationData simData, final RunDataClient newClient, final int newClientQueueNumber, final int[] selectQueuedClients) {
		final RunElementMatchData data=getData(simData);
		switch (batchMode) {
		case MATCH_MODE_COLLECT:
			processSendMultipleClients(simData,data,newClient,newClientQueueNumber,selectQueuedClients);
			break;
		case MATCH_MODE_TEMPORARY:
			processSendTemporaryBatchedClients(simData,data,newClient,newClientQueueNumber,selectQueuedClients);
			break;
		case MATCH_MODE_PERMANENT:
			processSendPermanentBatchedClients(simData,data,newClient,newClientQueueNumber,selectQueuedClients);
			break;
		}
	}

	/**
	 * Trägt einen Kunden in eine Teilwarteschlange ein.
	 * @param simData	Simulationsdatenobjekt
	 * @param newClient	Neu eingetroffener Kunde
	 * @param newClientQueueNumber	Index der Teilwarteschlange an der der Kunde eingetroffen ist
	 */
	private void addClientToQueue(final SimulationData simData, final RunDataClient newClient, final int newClientQueueNumber) {
		final RunElementMatchData data=getData(simData);

		data.waitingClients[newClientQueueNumber].add(newClient);
		newClient.lastWaitingStart=simData.currentTime;

		/* Kunden an Station in Statistik */
		simData.runData.logClientEntersStationQueue(simData,this,data,newClient);

		/* Logging */
		if (simData.loggingActive) {
			StringBuilder sb=new StringBuilder();
			for (List<RunDataClient> queue: data.waitingClients) {if (sb.length()!=0) sb.append(" / "); sb.append(queue.size());}
			log(simData,Language.tr("Simulation.Log.Matching"),String.format(Language.tr("Simulation.Log.Matching.Info"),newClientQueueNumber+1,newClient.logInfo(simData),name,sb.toString()));
		}
	}

	/**
	 * Überprüft, in welche Teilwarteschlange ein Kunde einsortiert werden soll.
	 * @param client	Neuer Kunde
	 * @return	Index der Teilwarteschlange für den Kunden
	 */
	private int getClientQueueNumber(final RunDataClient client) {
		for (int i=0;i<connectionIn.length;i++) {
			for (int id: connectionIn[i]) if (id==client.lastStationID) return i;
		}
		return 0;
	}

	/**
	 * Gibt es passende Kunden, so dass eine Freigabe erfolgen kann?<br>
	 * Modus: Kein Abgleich von Eigenschaften
	 * @param simData	Simulationsdatenobjekt
	 * @param data	Thread-lokales Datenobjekt zu der Station
	 * @param newClientQueueNumber	Index der Teilwarteschlange an der der Kunde eingetroffen ist (-1, falls es sich um keine Prüfung bei einer Ankunft handelt)
	 * @return	Indices der freizugebenden Kunden (jeweils ein Eintrag pro Teilwarteschlange) oder <code>null</code>, wenn keine Freigabe erfolgen kann
	 * @see #testReadyToSend(SimulationData, RunDataClient, int)
	 */
	private int[] testReadyToSendSimple(final SimulationData simData, final RunElementMatchData data, final int newClientQueueNumber) {
		/* Warten in allen anderen Schlangen Kunden? */
		for (int i=0;i<data.waitingClients.length;i++) {
			if (i==newClientQueueNumber) continue;
			if (data.waitingClients[i].size()==0) return null;
		}
		return data.selectQueuedClients; /* Ist in diesem Fall immer mit 0 gefüllt. */
	}

	/**
	 * Gibt es passende Kunden, so dass eine Freigabe erfolgen kann?<br>
	 * Modus: Abgleich eines Kundendatenfeldes<br>
	 * (Wird bei Ankunft eines neuen Kunden aufgerufen.)
	 * @param simData	Simulationsdatenobjekt
	 * @param data	Thread-lokales Datenobjekt zu der Station
	 * @param newClient	Neu eingetroffener Kunde (<code>null</code>, falls es sich um keine Prüfung bei einer Ankunft handelt)
	 * @param newClientQueueNumber	Index der Teilwarteschlange an der der Kunde eingetroffen ist (-1, falls es sich um keine Prüfung bei einer Ankunft handelt)
	 * @return	Indices der freizugebenden Kunden (jeweils ein Eintrag pro Teilwarteschlange) oder <code>null</code>, wenn keine Freigabe erfolgen kann
	 * @see #testReadyToSend(SimulationData, RunDataClient, int)
	 */
	private int[] testReadyToSendNumberProperty(final SimulationData simData, final RunElementMatchData data, final RunDataClient newClient, final int newClientQueueNumber) {
		final int[] selected=data.selectQueuedClients;
		/* Abgleich eines Zahlenwertes */
		final double newClientValue=newClient.getUserData(matchPropertyNumberIndex);
		for (int i=0;i<data.waitingClients.length;i++) {
			if (i==newClientQueueNumber) continue;
			final List<RunDataClient> queue=data.waitingClients[i];
			boolean ok=false;
			for (int j=0;j<queue.size();j++) {
				final RunDataClient client=queue.get(j);
				if (client.getUserData(matchPropertyNumberIndex)==newClientValue) {
					selected[i]=j;
					ok=true;
					break;
				}
			}
			if (!ok) return null;

		}
		return selected;
	}

	/**
	 * Gibt es passende Kunden, so dass eine Freigabe erfolgen kann?<br>
	 * Modus: Abgleich eines Kundendatenfeldes<br>
	 * (Wird bei Statusänderung ohne Ankunft eines neuen Kunden aufgerufen.)
	 * @param simData	Simulationsdatenobjekt
	 * @param data	Thread-lokales Datenobjekt zu der Station
	 * @return	Indices der freizugebenden Kunden (jeweils ein Eintrag pro Teilwarteschlange) oder <code>null</code>, wenn keine Freigabe erfolgen kann
	 * @see #testReadyToSend(SimulationData, RunDataClient, int)
	 */
	private int[] testReadyToSendNumberProperty(final SimulationData simData, final RunElementMatchData data) {
		if (data.waitingClients[0].size()==0) return null;

		final int[] selected=data.selectQueuedClients;

		final List<RunDataClient> queue0=data.waitingClients[0];
		for (int i=0;i<queue0.size();i++) {
			final double clientReferenceValue=queue0.get(i).getUserData(matchPropertyNumberIndex);
			selected[0]=i;

			boolean ok=false;
			for (int j=1;j<data.waitingClients.length;j++) {
				final List<RunDataClient> queue=data.waitingClients[j];
				ok=false;
				for (int k=0;k<queue.size();k++) {
					final RunDataClient client=queue.get(k);
					if (client.getUserData(matchPropertyNumberIndex)==clientReferenceValue) {
						selected[j]=k;
						ok=true;
						break;
					}
				}
				if (!ok) break;
			}

			if (ok) return selected;
		}

		return null;
	}

	/**
	 * Gibt es passende Kunden, so dass eine Freigabe erfolgen kann?<br>
	 * Modus: Abgleich eines Kundendatentextfeldes<br>
	 * (Wird bei Ankunft eines neuen Kunden aufgerufen.)
	 * @param simData	Simulationsdatenobjekt
	 * @param data	Thread-lokales Datenobjekt zu der Station
	 * @param newClient	Neu eingetroffener Kunde (<code>null</code>, falls es sich um keine Prüfung bei einer Ankunft handelt)
	 * @param newClientQueueNumber	Index der Teilwarteschlange an der der Kunde eingetroffen ist (-1, falls es sich um keine Prüfung bei einer Ankunft handelt)
	 * @return	Indices der freizugebenden Kunden (jeweils ein Eintrag pro Teilwarteschlange) oder <code>null</code>, wenn keine Freigabe erfolgen kann
	 * @see #testReadyToSend(SimulationData, RunDataClient, int)
	 */
	private int[] testReadyToSendTextProperty(final SimulationData simData, final RunElementMatchData data, final RunDataClient newClient, final int newClientQueueNumber) {
		final int[] selected=data.selectQueuedClients;
		/* Abgleich einer Text-Eigenschaft */
		final String newClientValue=newClient.getUserDataString(matchPropertyString);
		for (int i=0;i<data.waitingClients.length;i++) {
			if (i==newClientQueueNumber) continue;
			final List<RunDataClient> queue=data.waitingClients[i];
			boolean ok=false;
			for (int j=0;j<queue.size();j++) {
				final RunDataClient client=queue.get(j);
				if (client.getUserDataString(matchPropertyString).equals(newClientValue)) {
					selected[i]=j;
					ok=true;
					break;
				}
			}
			if (!ok) return null;
		}
		return selected;
	}

	/**
	 * Gibt es passende Kunden, so dass eine Freigabe erfolgen kann?<br>
	 * Modus: Abgleich eines Kundendatentextfeldes<br>
	 * (Wird bei Statusänderung ohne Ankunft eines neuen Kunden aufgerufen.)
	 * @param simData	Simulationsdatenobjekt
	 * @param data	Thread-lokales Datenobjekt zu der Station
	 * @return	Indices der freizugebenden Kunden (jeweils ein Eintrag pro Teilwarteschlange) oder <code>null</code>, wenn keine Freigabe erfolgen kann
	 * @see #testReadyToSend(SimulationData, RunDataClient, int)
	 */
	private int[] testReadyToSendTextProperty(final SimulationData simData, final RunElementMatchData data) {
		if (data.waitingClients[0].size()==0) return null;

		final int[] selected=data.selectQueuedClients;

		final List<RunDataClient> queue0=data.waitingClients[0];
		for (int i=0;i<queue0.size();i++) {
			final String clientReferenceValue=queue0.get(i).getUserDataString(matchPropertyString);
			selected[0]=i;

			boolean ok=false;
			for (int j=1;j<data.waitingClients.length;j++) {
				final List<RunDataClient> queue=data.waitingClients[j];
				ok=false;
				for (int k=0;k<queue.size();k++) {
					final RunDataClient client=queue.get(k);
					if (client.getUserDataString(matchPropertyString).equals(clientReferenceValue)) {
						selected[j]=k;
						ok=true;
						break;
					}
				}
				if (!ok) break;
			}

			if (ok) return selected;
		}

		return null;
	}

	/**
	 * Gibt es passende Kunden, so dass eine Freigabe erfolgen kann?
	 * @param simData	Simulationsdatenobjekt
	 * @param newClient	Neu eingetroffener Kunde (<code>null</code>, falls es sich um keine Prüfung bei einer Ankunft handelt)
	 * @param newClientQueueNumber	Index der Teilwarteschlange an der der Kunde eingetroffen ist (-1, falls es sich um keine Prüfung bei einer Ankunft handelt)
	 * @return	Indices der freizugebenden Kunden (jeweils ein Eintrag pro Teilwarteschlange) oder <code>null</code>, wenn keine Freigabe erfolgen kann
	 */
	private int[] testReadyToSend(final SimulationData simData, final RunDataClient newClient, final int newClientQueueNumber) {
		final RunElementMatchData data=getData(simData);

		switch (matchPropertyMode) {
		case NONE:
			return testReadyToSendSimple(simData,data,newClientQueueNumber);
		case NUMBER:
			if (newClient==null) {
				return testReadyToSendNumberProperty(simData,data);
			} else {
				return testReadyToSendNumberProperty(simData,data,newClient,newClientQueueNumber);
			}
		case TEXT:
			if (newClient==null) {
				return testReadyToSendTextProperty(simData,data);
			} else {
				return testReadyToSendTextProperty(simData,data,newClient,newClientQueueNumber);
			}
		default:
			return testReadyToSendSimple(simData,data,newClientQueueNumber);
		}
	}

	@Override
	public boolean systemStateChangeNotify(final SimulationData simData) {
		final RunElementMatchData data=getData(simData);

		/* Keine Bedingung definiert? -> Dann brauchen wir hier auch nicht zu prüfen. Kunden werden jeweils sofort freigegeben, wenn diese passend eingetroffen sind. */
		if (data.condition==null) return false;

		/* Bedingung erfüllt? */
		simData.runData.setClientVariableValues(null);
		if (!data.condition.eval(simData.runData.variableValues,simData,null)) return false;

		/* Warten passende Kunden? */
		final int[] selectQueuedClients=testReadyToSend(simData,null,-1);
		if (selectQueuedClients==null) return false;

		processSendClients(simData,null,-1,selectQueuedClients);
		return true;
	}

	@Override
	public void processArrival(final SimulationData simData, final RunDataClient client) {
		/* In welcher Schlange ist der Kunde eingetroffen? */
		final int newClientQueueNumber=getClientQueueNumber(client);

		/* Bedingung erfüllt? */
		final RunElementMatchData data=getData(simData);
		if (data.condition!=null) {
			simData.runData.setClientVariableValues(null);
			if (!data.condition.eval(simData.runData.variableValues,simData,null)) {
				/* Neuen Kunden an Schlange anstellen */
				addClientToQueue(simData,client,newClientQueueNumber);
				return;
			}
		}

		/* Passende Kunden in den anderen Schlangen finden */
		final int[] selectQueuedClients=testReadyToSend(simData,client,newClientQueueNumber);

		if (selectQueuedClients!=null) {
			/* Kunden weiterleiten */
			processSendClients(simData,client,newClientQueueNumber,selectQueuedClients);
		} else {
			/* Neuen Kunden an Schlange anstellen */
			addClientToQueue(simData,client,newClientQueueNumber);
		}
	}

	@Override
	public void processLeave(SimulationData simData, RunDataClient client) {
		/* Normalerweise bringt die Basisklasse RunElementPassThrough bereits eine Implementierung mit, hier erfolgt aber Batching usw., daher wird hier eine eigene Implementierung benötigt. */

		if (newClientType<0) {
			/* Einzelne Kunden */
			final RunElementMatchData data=getData(simData);
			if (data.moveNr>=0) {
				data.moveNr++;
				if (data.moveNr==data.moveClientsList.length) StationLeaveEvent.multiSendToStation(simData,data.moveClientsList,this,getNext());
			} else {
				/* Irgendwas schief gelaufen, wir senden doch alle einzeln. */
				StationLeaveEvent.sendToStation(simData,client,this,getNext());
			}
		} else {
			/* Batch */
			StationLeaveEvent.sendToStation(simData,client,this,getNext());
		}
	}

	@Override
	public boolean isInterarrivalByQueueStation(final SimulationData simData) {
		return true;
	}
}
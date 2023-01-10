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

import java.util.List;
import java.util.Map;

import language.Language;
import mathtools.NumberTools;
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElementPassThrough;
import simulator.editmodel.EditModel;
import simulator.events.ProcessWaitingClientsEvent;
import simulator.events.StationLeaveEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.BatchRecord;
import ui.modeleditor.elements.ModelElementBatch;
import ui.modeleditor.elements.ModelElementBatchMulti;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu {@link ModelElementBatch}
 * @author Alexander Herzog
 * @see ModelElementBatchMulti
 */
public class RunElementBatchMulti extends RunElementPassThrough {
	/**
	 * Minimale Batch-Größe pro Kundentyp (Einträge haben nur eine Bedeutung, wenn der zugehörige <code>batchMode</code>-Eintrag ungleich <code>null</code> ist)
	 */
	public String[] batchSizeMin;

	/**
	 * Maximale Batch-Größe pro Kundentyp (Einträge haben nur eine Bedeutung, wenn der zugehörige <code>batchMode</code>-Eintrag ungleich <code>null</code> ist)
	 */
	public String[] batchSizeMax;

	/**
	 * Batch-Modus pro Kundentyp (Einträge können <code>null</code> sein, wenn Kunden des Typs nicht gebatcht werden sollen)
	 */

	public BatchRecord.BatchMode[] batchMode;

	/**
	 * Neuer Kundentyp beim Batchen pro Kundentyp (Einträge haben nur eine Bedeutung, wenn der zugehörige <code>batchMode</code>-Eintrag ungleich <code>null</code> ist)
	 */
	public int[] newClientType;

	/**
	 * Wie sollen die Zeiten der Einzelkunden bei der Batch-Bildung auf den neuen Batch-Kunden übertragen werden?
	 */
	private BatchRecord.DataTransferMode[] transferTimes;

	/**
	 * Wie sollen die numerischen Datenfelder der Einzelkunden bei der Batch-Bildung auf den neuen Batch-Kunden übertragen werden?
	 */
	private BatchRecord.DataTransferMode[] transferNumbers;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementBatchMulti(final ModelElementBatchMulti element) {
		super(element,buildName(element,Language.tr("Simulation.Element.BatchMulti.Name")));
	}
	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementBatchMulti)) return null;
		final ModelElementBatchMulti batchElement=(ModelElementBatchMulti)element;
		final RunElementBatchMulti batch=new RunElementBatchMulti(batchElement);

		/* Auslaufende Kante */
		final String edgeError=batch.buildEdgeOut(batchElement);
		if (edgeError!=null) return edgeError;

		/* Batch-Bildung */

		batch.batchSizeMin=new String[runModel.clientTypes.length];
		batch.batchSizeMax=new String[runModel.clientTypes.length];
		batch.batchMode=new BatchRecord.BatchMode[runModel.clientTypes.length];
		batch.newClientType=new int[runModel.clientTypes.length];
		batch.transferTimes=new BatchRecord.DataTransferMode[runModel.clientTypes.length];
		batch.transferNumbers=new BatchRecord.DataTransferMode[runModel.clientTypes.length];
		for (Map.Entry<String,BatchRecord> entry: batchElement.getBatchRecords().entrySet()) {
			final int index=runModel.getClientTypeNr(entry.getKey());
			if (index<0) continue;

			final BatchRecord batchRecord=entry.getValue();

			switch (batchRecord.getBatchSizeMode()) {

			case FIXED:
				final String valueStr=batchRecord.getBatchSizeFixed();

				final Double D=NumberTools.getDouble(valueStr);
				if (D!=null && Math.round(D)<=0) return String.format(Language.tr("Simulation.Creator.InvalidBatchSize"),element.getId());

				if (ExpressionCalc.check(valueStr,runModel.variableNames)>=0) return String.format(Language.tr("Simulation.Creator.InvalidBatchSize"),element.getId());

				batch.batchSizeMin[index]=valueStr;
				batch.batchSizeMax[index]=valueStr;
				break;
			case RANGE:
				final String minStr=batchRecord.getBatchSizeMin();
				final String maxStr=batchRecord.getBatchSizeMax();

				final Double min=NumberTools.getDouble(minStr);
				final Double max=NumberTools.getDouble(maxStr);
				if (min!=null && Math.round(min)<=0) return String.format(Language.tr("Simulation.Creator.InvalidBatchSize"),element.getId());
				if (min!=null && max!=null && Math.round(max)<Math.round(min)) return String.format(Language.tr("Simulation.Creator.InvalidMaximumBatchSize"),element.getId());

				if (ExpressionCalc.check(minStr,runModel.variableNames)>=0) return String.format(Language.tr("Simulation.Creator.InvalidBatchSize"),element.getId());
				if (ExpressionCalc.check(maxStr,runModel.variableNames)>=0) return String.format(Language.tr("Simulation.Creator.InvalidMaximumBatchSize"),element.getId());

				batch.batchSizeMin[index]=minStr;
				batch.batchSizeMax[index]=maxStr;
				break;
			}

			batch.batchMode[index]=batchRecord.getBatchMode();
			if (batch.batchMode[index]==BatchRecord.BatchMode.BATCH_MODE_COLLECT) {
				batch.newClientType[index]=-1;
			} else {
				batch.newClientType[index]=runModel.getClientTypeNr(batchRecord.getNewClientType());
				if (batch.newClientType[index]<0) return String.format(Language.tr("Simulation.Creator.InvalidBatchClientType"),element.getId(),batchRecord.getNewClientType());
			}

			batch.transferTimes[index]=batchRecord.getTransferTimes();
			batch.transferNumbers[index]=batchRecord.getTransferNumbers();
		}

		return batch;
	}

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementBatchMulti)) return null;
		final ModelElementBatchMulti batchElement=(ModelElementBatchMulti)element;

		/* Auslaufende Kante */
		final RunModelCreatorStatus edgeError=testEdgeOut(batchElement);
		if (edgeError!=null) return edgeError;

		/* Batch-Bildung */
		final List<String> clientTypes=element.getModel().surface.getClientTypes();
		for (Map.Entry<String,BatchRecord> entry: batchElement.getBatchRecords().entrySet()) {
			boolean active=false;
			for (String name: clientTypes) if (name.equalsIgnoreCase(entry.getKey())) {active=true; break;}
			if (!active) continue;

			final BatchRecord batchRecord=entry.getValue();
			switch (batchRecord.getBatchSizeMode()) {
			case FIXED:
				final Double D=NumberTools.getDouble(batchRecord.getBatchSizeMin());
				if (D!=null && Math.round(D)<=0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.InvalidBatchSize"),element.getId()),RunModelCreatorStatus.Status.FIXED_BATCH_SIZE_LOWER_THAN_1);
				break;
			case RANGE:
				final Double min=NumberTools.getDouble(batchRecord.getBatchSizeMin());
				final Double max=NumberTools.getDouble(batchRecord.getBatchSizeMax());
				if (min!=null && Math.round(min)<=0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.InvalidBatchSize"),element.getId()),RunModelCreatorStatus.Status.MIN_BATCH_SIZE_LOWER_THAN_1);
				if (min!=null && max!=null && Math.round(max)<Math.round(min)) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.InvalidMaximumBatchSize"),element.getId()),RunModelCreatorStatus.Status.MAX_BATCH_SIZE_LOWER_THAN_MIN);
				break;
			}
		}

		return RunModelCreatorStatus.ok;
	}

	@Override
	public RunElementBatchMultiData getData(final SimulationData simData) {
		RunElementBatchMultiData data;
		data=(RunElementBatchMultiData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementBatchMultiData(this,simData);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	/**
	 * Die Batch-Größe wurde erreicht, Kunden werden weitergeleitet.<br>
	 * Modus: Kunden einfach gemeinsam weiterleiten
	 * @param simData	Simulationsdaten
	 * @param data	Thread-lokales Datenobjekt zu der Station
	 * @param type	Kundentyp für die aktuelle Batch-Bildung
	 * @param newClient	Aktuell gerade eingetroffener Kunde (kann <code>null</code> sein)
	 * @see ui.modeleditor.elements.BatchRecord.BatchMode#BATCH_MODE_COLLECT
	 */
	private void processSendMultipleClients(final SimulationData simData, final RunElementBatchMultiData data, final int type, final RunDataClient newClient) {
		/* Logging */
		if (simData.loggingActive) {
			final StringBuilder sb=new StringBuilder();
			sb.append("id=");
			sb.append(data.clients[type][0].hashCode());
			for (int i=1;i<data.waiting[type];i++) sb.append(", id="+data.clients[type][i].hashCode());
			if (newClient==null) {
				log(simData,Language.tr("Simulation.Log.BatchedForwarding"),String.format(Language.tr("Simulation.Log.BatchedForwarding.InfoNoNewClient"),name,sb.toString()));
			} else {
				log(simData,Language.tr("Simulation.Log.BatchedForwarding"),String.format(Language.tr("Simulation.Log.BatchedForwarding.Info"),newClient.logInfo(simData),name,sb.toString()));
			}
		}

		for (int i=0;i<data.waiting[type];i++) {
			/* Wartezeit in Statistik */
			final long waitingTime=simData.currentTime-data.clientAddTime[type][i];
			simData.runData.logStationProcess(simData,this,data.clients[type][i],waitingTime,0,0,waitingTime);
			data.clients[type][i].addStationTime(id,waitingTime,0,0,waitingTime);

			/* Kunden an Station in Statistik */
			simData.runData.logClientLeavesStationQueue(simData,this,data,data.clients[type][i]);

			/* Kunde zur nächsten Station leiten */
			StationLeaveEvent.addLeaveEvent(simData,data.clients[type][i],this,0);
		}
		data.waitingTotal-=data.waiting[type];
		data.waiting[type]=0;
	}

	/**
	 * Die Batch-Größe wurde erreicht, Kunden werden weitergeleitet.<br>
	 * Modus: Temporären Batch bilden
	 * @param simData	Simulationsdaten
	 * @param data	Thread-lokales Datenobjekt zu der Station
	 * @param type	Kundentyp für die aktuelle Batch-Bildung
	 * @param newClient	Aktuell gerade eingetroffener Kunde (kann <code>null</code> sein)
	 * @see ui.modeleditor.elements.BatchRecord.BatchMode#BATCH_MODE_TEMPORARY
	 */
	private void processSendTemporaryBatchedClients(final SimulationData simData, final RunElementBatchMultiData data, final int type, final RunDataClient newClient) {
		/* Logging */
		if (simData.loggingActive) {
			final StringBuilder sb=new StringBuilder();
			sb.append("id=");
			sb.append(data.clients[type][0].hashCode());
			for (int i=1;i<data.waiting[type];i++) sb.append(", id="+data.clients[type][i].hashCode());
			if (newClient==null) {
				log(simData,Language.tr("Simulation.Log.BatchingTemporary"),String.format(Language.tr("Simulation.Log.BatchingTemporary.InfoNoNewClient"),name,sb.toString()));
			} else {
				log(simData,Language.tr("Simulation.Log.BatchingTemporary"),String.format(Language.tr("Simulation.Log.BatchingTemporary.Info"),newClient.logInfo(simData),name,sb.toString()));
			}
		}

		/* Neuen Kunden anlegen */
		final RunDataClient batchedClient=simData.runData.clients.getClient(newClientType[type],simData,id);

		for (int i=0;i<data.waiting[type];i++) {
			/* Wartezeit in Statistik */
			final long waitingTime=simData.currentTime-data.clientAddTime[type][i];
			simData.runData.logStationProcess(simData,this,data.clients[type][i],waitingTime,0,0,waitingTime);
			data.clients[type][i].addStationTime(id,waitingTime,0,0,waitingTime);
		}

		/* Daten von den alten Kunden auf den neuen Batch-Kunden übertragen */
		RunElementBatch.transferTimes(transferTimes[type],data.waiting[type],data.clients[type],batchedClient);
		RunElementBatch.transferNumbers(transferNumbers[type],data.waiting[type],data.clients[type],batchedClient);

		for (int i=0;i<data.waiting[type];i++) {
			/* Kunden an Station in Statistik */
			simData.runData.logClientLeavesStationQueue(simData,this,data,data.clients[type][i]);

			/* Ist der Kunde als "letzter Kunde" markiert? */
			if (data.clients[type][i].isLastClient) batchedClient.isLastClient=true;

			/* Kunde in Batch aufnehmen */
			batchedClient.addBatchClient(data.clients[type][i]);

			/* Kunde verlässt Station (wird sonst über die Events realisiert) */
			simData.runData.logClientLeavesStation(simData,this,data,data.clients[type][i]);
			if (parentId>=0) simData.runData.logClientLeavesStation(simData,simData.runModel.elementsFast[parentId],null,data.clients[type][i]);
		}
		data.waitingTotal-=data.waiting[type];
		data.waiting[type]=0;

		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.BatchNewClient"),String.format(Language.tr("Simulation.Log.BatchNewClient.Info"),batchedClient.logInfo(simData),name));

		/* Kunde betritt Station (wird sonst über die Events realisiert) */
		simData.runData.logClientEntersStation(simData,this,data,batchedClient);
		if (parentId>=0) simData.runData.logClientEntersStation(simData,simData.runModel.elementsFast[parentId],null,batchedClient);

		/* Kunden weiterleiten */
		StationLeaveEvent.addLeaveEvent(simData,batchedClient,this,0);
	}

	/**
	 * Die Batch-Größe wurde erreicht, Kunden werden weitergeleitet.<br>
	 * Modus: Permanenten Batch bilden
	 * @param simData	Simulationsdaten
	 * @param data	Thread-lokales Datenobjekt zu der Station
	 * @param type	Kundentyp für die aktuelle Batch-Bildung
	 * @param newClient	Aktuell gerade eingetroffener Kunde (kann <code>null</code> sein)
	 * @see ui.modeleditor.elements.BatchRecord.BatchMode#BATCH_MODE_PERMANENT
	 */
	private void processSendPermanentBatchedClients(final SimulationData simData, final RunElementBatchMultiData data, final int type, final RunDataClient newClient) {
		/* Logging */
		if (simData.loggingActive) {
			final StringBuilder sb=new StringBuilder();
			sb.append("id=");
			sb.append(data.clients[type][0].hashCode());
			for (int i=1;i<data.waiting[type];i++) sb.append(", id="+data.clients[type][i].hashCode());
			if (newClient==null) {
				log(simData,Language.tr("Simulation.Log.BatchingDisposeForOldClients"),String.format(Language.tr("Simulation.Log.BatchingDisposeForOldClients.InfoNoNewClient"),name,sb.toString()));
			} else {
				log(simData,Language.tr("Simulation.Log.BatchingDisposeForOldClients"),String.format(Language.tr("Simulation.Log.BatchingDisposeForOldClients.Info"),newClient.logInfo(simData),name,sb.toString()));
			}
		}

		/* Neuen Kunden anlegen */
		final RunDataClient batchedClient=simData.runData.clients.getClient(newClientType[type],simData,id);

		for (int i=0;i<data.waiting[type];i++) {
			/* Wartezeit in Statistik */
			final long waitingTime=simData.currentTime-data.clientAddTime[type][i];
			simData.runData.logStationProcess(simData,this,data.clients[type][i],waitingTime,0,0,waitingTime);
			data.clients[type][i].addStationTime(id,waitingTime,0,0,waitingTime);
		}

		/* Daten von den alten Kunden auf den neuen Batch-Kunden übertragen */
		RunElementBatch.transferTimes(transferTimes[type],data.waiting[type],data.clients[type],batchedClient);
		RunElementBatch.transferNumbers(transferNumbers[type],data.waiting[type],data.clients[type],batchedClient);

		boolean isLastClient=false;
		for (int i=0;i<data.waiting[type];i++) {
			/* Wartezeit in Statistik */
			final long waitingTime=simData.currentTime-data.clientAddTime[type][i];
			simData.runData.logStationProcess(simData,this,data.clients[type][i],waitingTime,0,0,waitingTime);
			data.clients[type][i].addStationTime(id,waitingTime,0,0,waitingTime);

			/* Kunden an Station in Statistik */
			simData.runData.logClientLeavesStationQueue(simData,this,data,data.clients[type][i]);

			/* Ist der Kunde als "letzter Kunde" markiert? */
			isLastClient=isLastClient || data.clients[type][i].isLastClient;

			/* Kunde final in Statistik erfassen und Objekt recyceln */
			simData.runData.clients.disposeClient(data.clients[type][i],simData);

			/* Kunde verlässt Station (wird sonst über die Events realisiert) */
			simData.runData.logClientLeavesStation(simData,this,data,data.clients[type][i]);
			if (parentId>=0) simData.runData.logClientLeavesStation(simData,simData.runModel.elementsFast[parentId],null,data.clients[type][i]);
		}
		data.waitingTotal-=data.waiting[type];
		data.waiting[type]=0;

		batchedClient.isLastClient=isLastClient;

		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.BatchNewClient"),String.format(Language.tr("Simulation.Log.BatchNewClient.Info"),batchedClient.logInfo(simData),name));

		/* Kunde betritt Station (wird sonst über die Events realisiert) */
		simData.runData.logClientEntersStation(simData,this,data,batchedClient);
		if (parentId>=0) simData.runData.logClientEntersStation(simData,simData.runModel.elementsFast[parentId],null,batchedClient);

		/* Kunden weiterleiten */
		StationLeaveEvent.addLeaveEvent(simData,batchedClient,this,0);
	}

	/**
	 * Die Batch-Größe wurde erreicht, Kunden werden weitergeleitet.<br>
	 * @param simData	Simulationsdaten
	 * @param data	Thread-lokales Datenobjekt zu der Station
	 * @param newClient	Aktuell gerade eingetroffener Kunde (kann <code>null</code> sein)
	 */
	private void processSend(final SimulationData simData, final RunElementBatchMultiData data, final RunDataClient newClient) {
		for (int i=0;i<batchMode.length;i++) if (batchMode[i]!=null && data.waiting[i]>=data.batchSizeMin[i]) {
			switch (batchMode[i]) {
			case BATCH_MODE_COLLECT:
				processSendMultipleClients(simData,data,i,newClient);
				break;
			case BATCH_MODE_TEMPORARY:
				processSendTemporaryBatchedClients(simData,data,i,newClient);
				break;
			case BATCH_MODE_PERMANENT:
				processSendPermanentBatchedClients(simData,data,i,newClient);
				break;
			}
		}
	}

	@Override
	public void processArrival(final SimulationData simData, final RunDataClient client) {
		/* Gibt es für den Kundentyp eine Batch-Regel? Wenn nein, einfach weiterleiten. */
		if (client!=null && batchMode[client.type]==null) {
			StationLeaveEvent.addLeaveEvent(simData,client,this,0);
			return;
		}

		final RunElementBatchMultiData data=getData(simData);

		if (client==null) {
			/* Keine Kundenankunft, sondern Re-Check, ob noch weitere Kunden angekommen sind, die noch in den Batch passen. */
			processSend(simData,data,null);
			return;
		}

		/* Kunden an Station in Statistik */
		simData.runData.logClientEntersStationQueue(simData,this,data,client);

		switch (data.addClient(client,simData.currentTime)) {
		case -1:  /* Keine Batch-Regel für Kunde, sollte eigentlich schon oben ausgefiltert worden sein. */
			simData.runData.logClientLeavesStationQueue(simData,this,data,client);
			StationLeaveEvent.addLeaveEvent(simData,client,this,0);
			break;
		case 0: /* Noch nicht genug Kunden eingetroffen */
			/* Kunde ist durch addClient schon in Warteschlange aufgenommen worden, hier gibt's nicht zu tun, außer: */
			/* Logging */
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.Batching"),String.format(Language.tr("Simulation.Log.Batching.Info"),client.logInfo(simData),name,data.waiting[client.type],data.clients[client.type].length));
			break;
		case 1: /* Minimale Batch-Größe erreicht, aber noch nicht maximale Batch-Größe */
			ProcessWaitingClientsEvent event=(ProcessWaitingClientsEvent)simData.getEvent(ProcessWaitingClientsEvent.class);
			event.init(simData.currentTime+1);
			event.station=this;
			simData.eventManager.addEvent(event);
			/* Logging */
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.Batching"),String.format(Language.tr("Simulation.Log.Batching.MinMaxInfo"),client.logInfo(simData),name,data.waiting[client.type],data.clients[client.type].length));
			break;
		case 2: /* Maximale Batch-Größe erreicht */
			processSend(simData,data,client);
			break;
		}
	}
}

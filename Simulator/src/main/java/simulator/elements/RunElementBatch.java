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
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElementPassThrough;
import simulator.editmodel.EditModel;
import simulator.events.ProcessWaitingClientsEvent;
import simulator.events.StationLeaveEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.BatchRecord;
import ui.modeleditor.elements.ModelElementBatch;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu {@link ModelElementBatch}
 * @author Alexander Herzog
 * @see ModelElementBatch
 */
public class RunElementBatch extends RunElementPassThrough {
	private int batchSizeMin;
	private int batchSizeMax;
	private BatchRecord.BatchMode batchMode;
	private int newClientType;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementBatch(final ModelElementBatch element) {
		super(element,buildName(element,Language.tr("Simulation.Element.Batch.Name")));
		batchSizeMin=1;
		batchSizeMax=1;
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementBatch)) return null;
		final ModelElementBatch batchElement=(ModelElementBatch)element;
		final RunElementBatch batch=new RunElementBatch(batchElement);

		/* Auslaufende Kante */
		final String edgeError=batch.buildEdgeOut(batchElement);
		if (edgeError!=null) return edgeError;

		/* Batch-Bildung */
		final BatchRecord batchRecord=batchElement.getBatchRecord();

		switch (batchRecord.getBatchSizeMode()) {
		case FIXED:
			batch.batchSizeMin=batchRecord.getBatchSizeFixed();
			if (batch.batchSizeMin<=0) return String.format(Language.tr("Simulation.Creator.InvalidBatchSize"),element.getId());
			batch.batchSizeMax=batchRecord.getBatchSizeFixed();
			break;
		case RANGE:
			batch.batchSizeMin=batchRecord.getBatchSizeMin();
			if (batch.batchSizeMin<=0) return String.format(Language.tr("Simulation.Creator.InvalidBatchSize"),element.getId());
			batch.batchSizeMax=batchRecord.getBatchSizeMax();
			if (batch.batchSizeMax<batch.batchSizeMin) return String.format(Language.tr("Simulation.Creator.InvalidMaximumBatchSize"),element.getId());
			break;

		}

		batch.batchMode=batchRecord.getBatchMode();
		if (batch.batchMode==BatchRecord.BatchMode.BATCH_MODE_COLLECT) {
			batch.newClientType=-1;
		} else {
			batch.newClientType=runModel.getClientTypeNr(batchRecord.getNewClientType());
			if (batch.newClientType<0) return String.format(Language.tr("Simulation.Creator.InvalidBatchClientType"),element.getId(),batchRecord.getNewClientType());
		}

		return batch;
	}

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementBatch)) return null;
		final ModelElementBatch batchElement=(ModelElementBatch)element;

		/* Auslaufende Kante */
		final RunModelCreatorStatus edgeError=testEdgeOut(batchElement);
		if (edgeError!=null) return edgeError;

		/* Batch-Bildung */
		switch (batchElement.getBatchRecord().getBatchSizeMode()) {
		case FIXED:
			if (batchElement.getBatchRecord().getBatchSizeFixed()<=0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.InvalidBatchSize"),element.getId()),RunModelCreatorStatus.Status.FIXED_BATCH_SIZE_LOWER_THAN_1);
			break;
		case RANGE:
			if (batchElement.getBatchRecord().getBatchSizeMin()<=0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.InvalidBatchSize"),element.getId()),RunModelCreatorStatus.Status.MIN_BATCH_SIZE_LOWER_THAN_1);
			if (batchElement.getBatchRecord().getBatchSizeMax()<batchElement.getBatchRecord().getBatchSizeMin()) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.InvalidMaximumBatchSize"),element.getId()),RunModelCreatorStatus.Status.MAX_BATCH_SIZE_LOWER_THAN_MIN);
			break;
		}

		return RunModelCreatorStatus.ok;
	}

	@Override
	public RunElementBatchData getData(final SimulationData simData) {
		RunElementBatchData data;
		data=(RunElementBatchData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementBatchData(this,batchSizeMin,batchSizeMax);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	private void processSendMultipleClients(final SimulationData simData, final RunElementBatchData data, final RunDataClient newClient) {
		/* Logging */
		if (simData.loggingActive) {
			final StringBuilder sb=new StringBuilder();
			sb.append("id=");
			sb.append(data.clients[0].hashCode());
			for (int i=1;i<data.waiting;i++) sb.append(", id="+data.clients[i].hashCode());
			if (newClient==null) {
				log(simData,Language.tr("Simulation.Log.BatchedForwarding"),String.format(Language.tr("Simulation.Log.BatchedForwarding.InfoNoNewClient"),name,sb.toString()));
			} else {
				log(simData,Language.tr("Simulation.Log.BatchedForwarding"),String.format(Language.tr("Simulation.Log.BatchedForwarding.Info"),newClient.logInfo(simData),name,sb.toString()));
			}
		}

		for (int i=0;i<data.waiting;i++) {
			/* Wartezeit in Statistik */
			final long waitingTime=simData.currentTime-data.clientAddTime[i];
			simData.runData.logStationProcess(simData,this,data.clients[i],waitingTime,0,0,waitingTime);
			data.clients[i].waitingTime+=waitingTime;
			data.clients[i].residenceTime+=waitingTime;

			/* Kunden an Station in Statistik */
			simData.runData.logClientLeavesStationQueue(simData,this,data,data.clients[i]);

			/* Kunde zur nächsten Station leiten */
			StationLeaveEvent.addLeaveEvent(simData,data.clients[i],this,0);
		}
		data.waiting=0;
	}

	private void processSendTemporaryBatchedClients(final SimulationData simData, final RunElementBatchData data, final RunDataClient newClient) {
		/* Logging */
		if (simData.loggingActive) {
			final StringBuilder sb=new StringBuilder();
			sb.append("id=");
			sb.append(data.clients[0].hashCode());
			for (int i=1;i<data.waiting;i++) sb.append(", id="+data.clients[i].hashCode());
			if (newClient==null) {
				log(simData,Language.tr("Simulation.Log.BatchingTemporary"),String.format(Language.tr("Simulation.Log.BatchingTemporary.InfoNoNewClient"),name,sb.toString()));
			} else {
				log(simData,Language.tr("Simulation.Log.BatchingTemporary"),String.format(Language.tr("Simulation.Log.BatchingTemporary.Info"),newClient.logInfo(simData),name,sb.toString()));
			}
		}

		/* Neuen Kunden anlegen */
		final RunDataClient batchedClient=simData.runData.clients.getClient(newClientType,simData);

		for (int i=0;i<data.waiting;i++) {
			/* Wartezeit in Statistik */
			final long waitingTime=simData.currentTime-data.clientAddTime[i];
			simData.runData.logStationProcess(simData,this,data.clients[i],waitingTime,0,0,waitingTime);
			data.clients[i].waitingTime+=waitingTime;
			data.clients[i].residenceTime+=waitingTime;

			/* Kunden an Station in Statistik */
			simData.runData.logClientLeavesStationQueue(simData,this,data,data.clients[i]);

			/* Ist der Kunde als "letzter Kunde" markiert? */
			if (data.clients[i].isLastClient) batchedClient.isLastClient=true;

			/* Kunde in Batch aufnehmen */
			batchedClient.addBatchClient(data.clients[i]);

			/* Kunde verlässt Station (wird sonst über die Events realisiert) */
			simData.runData.logClientLeavesStation(simData,this,data);
		}
		data.waiting=0;

		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.BatchNewClient"),String.format(Language.tr("Simulation.Log.BatchNewClient.Info"),batchedClient.logInfo(simData),name));

		/* Kunde betritt Station (wird sonst über die Events realisiert) */
		simData.runData.logClientEntersStation(simData,this,data);

		/* Kunden weiterleiten */
		StationLeaveEvent.addLeaveEvent(simData,batchedClient,this,0);
	}

	private void processSendPermanentBatchedClients(final SimulationData simData, final RunElementBatchData data, final RunDataClient newClient) {
		/* Logging */
		if (simData.loggingActive) {
			final StringBuilder sb=new StringBuilder();
			sb.append("id=");
			sb.append(data.clients[0].hashCode());
			for (int i=1;i<data.waiting;i++) sb.append(", id="+data.clients[i].hashCode());
			if (newClient==null) {
				log(simData,Language.tr("Simulation.Log.BatchingDisposeForOldClients"),String.format(Language.tr("Simulation.Log.BatchingDisposeForOldClients.InfoNoNewClient"),name,sb.toString()));
			} else {
				log(simData,Language.tr("Simulation.Log.BatchingDisposeForOldClients"),String.format(Language.tr("Simulation.Log.BatchingDisposeForOldClients.Info"),newClient.logInfo(simData),name,sb.toString()));
			}
		}

		boolean isLastClient=false;

		for (int i=0;i<data.waiting;i++) {
			/* Wartezeit in Statistik */
			final long waitingTime=simData.currentTime-data.clientAddTime[i];
			simData.runData.logStationProcess(simData,this,data.clients[i],waitingTime,0,0,waitingTime);
			data.clients[i].waitingTime+=waitingTime;
			data.clients[i].residenceTime+=waitingTime;

			/* Kunden an Station in Statistik */
			simData.runData.logClientLeavesStationQueue(simData,this,data,data.clients[i]);

			/* Ist der Kunde als "letzter Kunde" markiert? */
			isLastClient=isLastClient || data.clients[i].isLastClient;

			/* Kunde final in Statistik erfassen und Objekt recyceln */
			simData.runData.clients.disposeClient(data.clients[i],simData);

			/* Kunde verlässt Station (wird sonst über die Events realisiert) */
			simData.runData.logClientLeavesStation(simData,this,data);
		}
		data.waiting=0;

		/* Neuen Kunden anlegen */
		final RunDataClient batchedClient=simData.runData.clients.getClient(newClientType,simData);
		batchedClient.isLastClient=isLastClient;

		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.BatchNewClient"),String.format(Language.tr("Simulation.Log.BatchNewClient.Info"),batchedClient.logInfo(simData),name));

		/* Kunde betritt Station (wird sonst über die Events realisiert) */
		simData.runData.logClientEntersStation(simData,this,data);

		/* Kunden weiterleiten */
		StationLeaveEvent.addLeaveEvent(simData,batchedClient,this,0);
	}

	private void processSend(final SimulationData simData, final RunElementBatchData data, final RunDataClient newClient) {
		/* Batch-Größe erreicht, Kunden werden weitergeleitet */
		switch (batchMode) {
		case BATCH_MODE_COLLECT:
			processSendMultipleClients(simData,data,newClient);
			break;
		case BATCH_MODE_TEMPORARY:
			processSendTemporaryBatchedClients(simData,data,newClient);
			break;
		case BATCH_MODE_PERMANENT:
			processSendPermanentBatchedClients(simData,data,newClient);
			break;
		}
	}

	@Override
	public void processArrival(final SimulationData simData, final RunDataClient client) {
		final RunElementBatchData data=getData(simData);

		if (client==null) {
			/* Keine Kundenankunft, sondern Re-Check, ob noch weitere Kunden angekommen sind, die noch in den Batch passen. */
			if (data.waiting<batchSizeMin) return; /* Wurden wohl inzwischen schon weitergeleitet, Event war überflüssig. */
			processSend(simData,data,null);
			return;
		}

		/* Kunden an Station in Statistik */
		simData.runData.logClientEntersStationQueue(simData,this,data,client);

		switch (data.addClient(client,simData.currentTime)) {
		case 0: /* Noch nicht genug Kunden eingetroffen */
			/* Kunde ist durch addClient schon in Warteschlange aufgenommen worden, hier gibt's nicht zu tun, außer: */
			/* Logging */
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.Batching"),String.format(Language.tr("Simulation.Log.Batching.Info"),client.logInfo(simData),name,data.waiting,data.clients.length));
			break;
		case 1: /* Minimale Batch-Größe erreicht, aber noch nicht maximale Batch-Größe */
			ProcessWaitingClientsEvent event=(ProcessWaitingClientsEvent)simData.getEvent(ProcessWaitingClientsEvent.class);
			event.init(simData.currentTime+1);
			event.station=this;
			simData.eventManager.addEvent(event);
			/* Logging */
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.Batching"),String.format(Language.tr("Simulation.Log.Batching.MinMaxInfo"),client.logInfo(simData),name,data.waiting,data.clients.length));
			break;
		case 2: /* Maximale Batch-Größe erreicht */
			processSend(simData,data,client);
			break;
		}
	}
}

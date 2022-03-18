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
import simulator.coreelements.RunElement;
import simulator.coreelements.RunElementPassThrough;
import simulator.editmodel.EditModel;
import simulator.events.StationLeaveEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementBarrier;
import ui.modeleditor.elements.ModelElementBarrierPull;
import ui.modeleditor.elements.ModelElementConveyor;
import ui.modeleditor.elements.ModelElementHold;
import ui.modeleditor.elements.ModelElementHoldJS;
import ui.modeleditor.elements.ModelElementHoldMulti;
import ui.modeleditor.elements.ModelElementPickUp;
import ui.modeleditor.elements.ModelElementProcess;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementPickUp</code>
 * @author Alexander Herzog
 * @see ModelElementPickUp
 */
public class RunElementPickUp extends RunElementPassThrough implements StateChangeListener {
	/** ID der fremden Warteschlange, aus der der jeweils andere Kunde entnommen werden soll */
	private int queueId;
	/** Fremde Station aus deren Warteschlange der jeweils andere Kunde entnommen werden soll (Übersetzung von {@link #queueId}) */
	private RunElement queue;

	/** Kunden notfalls alleine weiterleiten, wenn die entfernte Warteschlange leer ist? */
	private boolean sendAloneIfQueueEmpty;

	/** Batch-Bildungs-Modus */
	private ModelElementPickUp.BatchMode batchMode;
	/** Index des neuen Batch-Kundentyps (bei der temporären oder permanenten Batch-Bildung) */
	private int newClientType;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementPickUp(final ModelElementPickUp element) {
		super(element,buildName(element,Language.tr("Simulation.Element.PickUp.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementPickUp)) return null;
		final ModelElementPickUp pickupElement=(ModelElementPickUp)element;
		final RunElementPickUp pickup=new RunElementPickUp(pickupElement);

		/* Auslaufende Kante */
		final String edgeError=pickup.buildEdgeOut(pickupElement);
		if (edgeError!=null) return edgeError;

		/* Ausleite-Warteschlange */
		pickup.queueId=pickupElement.getQueueID();
		if (pickup.queueId<0) return String.format(Language.tr("Simulation.Creator.NoConnectedQueue"),element.getId());
		ModelElement e=editModel.surface.getById(pickup.queueId);
		if (e==null || !((e instanceof ModelElementProcess) || (e instanceof ModelElementHold) || (e instanceof ModelElementHoldMulti) || (e instanceof ModelElementHoldJS) || (e instanceof ModelElementBarrier) || (e instanceof ModelElementBarrierPull) || (e instanceof ModelElementConveyor))) return String.format(Language.tr("Simulation.Creator.NoQueueAtConnectedElement"),element.getId(),pickup.queueId);

		/* Ggf. alleine weitersenden? */
		pickup.sendAloneIfQueueEmpty=pickupElement.isSendAloneIfQueueEmpty();

		/* Batch-Modus */
		pickup.batchMode=pickupElement.getBatchMode();
		if (pickup.batchMode==ModelElementPickUp.BatchMode.BATCH_MODE_COLLECT) {
			pickup.newClientType=-1;
		} else {
			pickup.newClientType=runModel.getClientTypeNr(pickupElement.getNewClientType());
			if (pickup.newClientType<0) return String.format(Language.tr("Simulation.Creator.InvalidBatchClientType"),element.getId(),pickupElement.getNewClientType());
		}

		return pickup;
	}

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementPickUp)) return null;
		final ModelElementPickUp pickupElement=(ModelElementPickUp)element;

		/* Auslaufende Kante */
		final RunModelCreatorStatus edgeError=testEdgeOut(pickupElement);
		if (edgeError!=null) return edgeError;

		/* Ausleite-Warteschlange */
		if (pickupElement.getQueueID()<0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.NoConnectedQueue"),element.getId()));

		return RunModelCreatorStatus.ok;
	}

	@Override
	public void prepareRun(final RunModel runModel) {
		super.prepareRun(runModel); /* von RunElementPassThrough */
		queue=runModel.elements.get(queueId);
	}

	@Override
	public RunElementPickUpData getData(final SimulationData simData) {
		RunElementPickUpData data;
		data=(RunElementPickUpData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementPickUpData(this);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	/**
	 * Versucht einen Kunden aus der entfernten Warteschlange zu entnehmen
	 * @param simData	Simulationsdatenobjekt
	 * @return	Aus der entfernten Warteschlange entnommener Kunde oder <code>null</code>, wenn dort kein Kunde zur Verfügung stand
	 */
	private RunDataClient getFirstClientFromQueue(final SimulationData simData) {
		if (!(queue instanceof PickUpQueue)) return null;
		final PickUpQueue pickUpQueue=(PickUpQueue)queue;

		final RunDataClient client=pickUpQueue.getClient(simData);

		/* Logging */
		if (client!=null && simData.loggingActive) {
			final String s=String.format(Language.tr("Simulation.Log.PickUp.Info"),queue.name,client.logInfo(simData),name);
			log(simData,Language.tr("Simulation.Log.PickUp"),s);
		}

		return client;
	}

	/**
	 * Startet die Verarbeitung
	 * @param simData	Simulationsdatenobjekt
	 * @param data	Thread-lokales Datenobjekt zu der Station
	 * @param otherClient	Aus der entfernten Warteschlange entnommener Kunde (kann <code>null</code> sein)
	 */
	private void processMatch(final SimulationData simData, final RunElementPickUpData data, final RunDataClient otherClient) {
		/* Kunde aus Warteschlange entfernen und weiterleiten */
		final RunDataClient client=data.waitingClients.poll();

		/* ===== Eigener Kunde ===== */

		/* Wartezeit in Statistik */
		final long waitingTime=simData.currentTime-client.lastWaitingStart;
		simData.runData.logStationProcess(simData,this,client,waitingTime,0,0,waitingTime);
		client.addStationTime(id,waitingTime,0,0,waitingTime);

		/* Kunden an Station in Statistik */
		simData.runData.logClientLeavesStationQueue(simData,this,data,client);

		/* ===== Kunde aus anderer Warteschlange ===== */

		/* Kunde verlässt Station (wird sonst über die Events realisiert) */
		if (otherClient!=null) {
			if (queue.isClientCountStation()) simData.runData.logClientLeavesStation(simData,queue,null,otherClient);
			if (parentId>=0) simData.runData.logClientLeavesStation(simData,simData.runModel.elementsFast[parentId],null,otherClient);
		}

		/* Wartezeit des Kunden usw. wurde bereits bei getClient() verarbeitet */

		if (otherClient==null || batchMode==ModelElementPickUp.BatchMode.BATCH_MODE_COLLECT) {
			/* ===== Einzeln weiterleiten ===== */

			/* Logging */
			if (simData.loggingActive) {
				if (otherClient==null) {
					log(simData,Language.tr("Simulation.Log.PickUpEmptyQueue"),String.format(Language.tr("Simulation.Log.PickUpEmptyQueue.Info"),client.logInfo(simData),name));
				} else {
					log(simData,Language.tr("Simulation.Log.PickUpForwarding"),String.format(Language.tr("Simulation.Log.PickUpForwarding.Info"),client.logInfo(simData),otherClient.logInfo(simData),name));
				}
			}

			StationLeaveEvent.addLeaveEvent(simData,client,this,0);
			if (otherClient!=null) {
				processLeave(simData,otherClient); /* Kunden direkt von dieser Station aus weiterleiten (würde sonst von StationLeaveEvent.addLeaveEvent aufgerufen - aber für die falsche Station */
			}
		} else {
			/* ===== Batch bilden ===== */
			final RunDataClient batchedClient;

			if (batchMode==ModelElementPickUp.BatchMode.BATCH_MODE_TEMPORARY) {
				/* === Temporärer Batch === */

				/* Eigenen Kunden aus Station austragen (wird sonst über die Events realisiert) */
				simData.runData.logClientLeavesStation(simData,this,data,client);
				if (parentId>=0) simData.runData.logClientLeavesStation(simData,simData.runModel.elementsFast[parentId],null,client);

				/* Ist der Kunde als "letzter Kunde" markiert? */
				boolean isLastClient=client.isLastClient || otherClient.isLastClient;

				/* Neuen Kunden anlegen */
				batchedClient=simData.runData.clients.getClient(newClientType,simData);
				batchedClient.isLastClient=isLastClient;

				/* Kunde in Batch aufnehmen */
				batchedClient.addBatchClient(client);
				batchedClient.addBatchClient(otherClient);
			} else {
				/* === Dauerhafter Batch === */

				/* Eigenen Kunden aus Station austragen (wird sonst über die Events realisiert) */
				simData.runData.logClientLeavesStation(simData,this,data,client);
				if (parentId>=0) simData.runData.logClientLeavesStation(simData,simData.runModel.elementsFast[parentId],null,client);

				/* Ist der Kunde als "letzter Kunde" markiert? */
				boolean isLastClient=client.isLastClient || otherClient.isLastClient;

				/* Kunde final in Statistik erfassen und Objekt recyceln */
				simData.runData.clients.disposeClient(client,simData);
				simData.runData.clients.disposeClient(otherClient,simData);

				/* Neuen Kunden anlegen */
				batchedClient=simData.runData.clients.getClient(newClientType,simData);
				batchedClient.isLastClient=isLastClient;
			}

			/* Logging */
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.PickUpNewClient"),String.format(Language.tr("Simulation.Log.PickUpNewClient.Info"),batchedClient.logInfo(simData),name));

			/* Kunde betritt Station (wird sonst über die Events realisiert) */
			simData.runData.logClientEntersStation(simData,this,data,batchedClient);
			if (parentId>=0) simData.runData.logClientEntersStation(simData,simData.runModel.elementsFast[parentId],null,batchedClient);

			/* Kunden weiterleiten */
			StationLeaveEvent.addLeaveEvent(simData,batchedClient,this,0);
		}
	}

	@Override
	public void processArrival(final SimulationData simData, final RunDataClient client) {
		final RunElementPickUpData data=getData(simData);

		/* Kunden in Warteschlange einreihen */
		data.waitingClients.offer(client);
		client.lastWaitingStart=simData.currentTime;

		/* Kunden an Station in Statistik */
		simData.runData.logClientEntersStationQueue(simData,this,data,client);

		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.PickUpArrival"),String.format(Language.tr("Simulation.Log.PickUpArrival.Info"),client.logInfo(simData),name));

		final RunDataClient queueClient=getFirstClientFromQueue(simData);
		if (queueClient==null) {
			if (sendAloneIfQueueEmpty) processMatch(simData,data,null);
		} else {
			processMatch(simData,data,queueClient);
		}
	}

	@Override
	public boolean systemStateChangeNotify(final SimulationData simData) {
		final RunElementPickUpData data=getData(simData);

		/* Warten überhaupt Kunden? */
		if (data.waitingClients.size()==0) return false;

		/* Kunde in anderer Warteschlange angekommen? Dann mit eigenem Kunden zusammen weiterleiten */
		final RunDataClient queueClient=getFirstClientFromQueue(simData);
		if (queueClient!=null) {
			processMatch(simData,data,queueClient);
			return true;
		} else {
			return false;
		}
	}
}
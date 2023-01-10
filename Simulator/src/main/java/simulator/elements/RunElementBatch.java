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
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu {@link ModelElementBatch}
 * @author Alexander Herzog
 * @see ModelElementBatch
 */
public class RunElementBatch extends RunElementPassThrough {
	/** Minimale Batch-Größe */
	private String batchSizeMin;
	/** Maximale Batch-Größe */
	private String batchSizeMax;
	/** Batch-Bildungs-Modus */
	private BatchRecord.BatchMode batchMode;
	/** Index des neuen Batch-Kundentyps (bei der temporären oder permanenten Batch-Bildung) */
	private int newClientType;

	/** Wie sollen die Zeiten der Einzelkunden bei der Batch-Bildung auf den neuen Batch-Kunden übertragen werden? */
	private BatchRecord.DataTransferMode transferTimes;
	/** Wie sollen die numerischen Datenfelder der Einzelkunden bei der Batch-Bildung auf den neuen Batch-Kunden übertragen werden? */
	private BatchRecord.DataTransferMode transferNumbers;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementBatch(final ModelElementBatch element) {
		super(element,buildName(element,Language.tr("Simulation.Element.Batch.Name")));
		batchSizeMin="1";
		batchSizeMax="1";
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
			final String valueStr=batchElement.getBatchRecord().getBatchSizeFixed();

			final Double D=NumberTools.getDouble(valueStr);
			if (D!=null && Math.round(D)<=0) return String.format(Language.tr("Simulation.Creator.InvalidBatchSize"),element.getId());

			if (ExpressionCalc.check(valueStr,runModel.variableNames)>=0) return String.format(Language.tr("Simulation.Creator.InvalidBatchSize"),element.getId());

			batch.batchSizeMin=valueStr;
			batch.batchSizeMax=valueStr;
			break;
		case RANGE:
			final String minStr=batchElement.getBatchRecord().getBatchSizeMin();
			final String maxStr=batchElement.getBatchRecord().getBatchSizeMax();

			final Double min=NumberTools.getDouble(minStr);
			final Double max=NumberTools.getDouble(maxStr);
			if (min!=null && Math.round(min)<=0) return String.format(Language.tr("Simulation.Creator.InvalidBatchSize"),element.getId());
			if (min!=null && max!=null && Math.round(max)<Math.round(min)) return String.format(Language.tr("Simulation.Creator.InvalidMaximumBatchSize"),element.getId());

			if (ExpressionCalc.check(minStr,runModel.variableNames)>=0) return String.format(Language.tr("Simulation.Creator.InvalidBatchSize"),element.getId());
			if (ExpressionCalc.check(maxStr,runModel.variableNames)>=0) return String.format(Language.tr("Simulation.Creator.InvalidMaximumBatchSize"),element.getId());

			batch.batchSizeMin=minStr;
			batch.batchSizeMax=maxStr;
			break;
		}

		batch.batchMode=batchRecord.getBatchMode();
		if (batch.batchMode==BatchRecord.BatchMode.BATCH_MODE_COLLECT) {
			batch.newClientType=-1;
		} else {
			batch.newClientType=runModel.getClientTypeNr(batchRecord.getNewClientType());
			if (batch.newClientType<0) return String.format(Language.tr("Simulation.Creator.InvalidBatchClientType"),element.getId(),batchRecord.getNewClientType());
		}

		/* Daten zusammenführen */
		batch.transferTimes=batchRecord.getTransferTimes();
		batch.transferNumbers=batchRecord.getTransferNumbers();

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
			final Double D=NumberTools.getDouble(batchElement.getBatchRecord().getBatchSizeMin());
			if (D!=null && Math.round(D)<=0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.InvalidBatchSize"),element.getId()),RunModelCreatorStatus.Status.FIXED_BATCH_SIZE_LOWER_THAN_1);
			break;
		case RANGE:
			final Double min=NumberTools.getDouble(batchElement.getBatchRecord().getBatchSizeMin());
			final Double max=NumberTools.getDouble(batchElement.getBatchRecord().getBatchSizeMax());
			if (min!=null && Math.round(min)<=0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.InvalidBatchSize"),element.getId()),RunModelCreatorStatus.Status.MIN_BATCH_SIZE_LOWER_THAN_1);
			if (min!=null && max!=null && Math.round(max)<Math.round(min)) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.InvalidMaximumBatchSize"),element.getId()),RunModelCreatorStatus.Status.MAX_BATCH_SIZE_LOWER_THAN_MIN);
			break;
		}

		return RunModelCreatorStatus.ok;
	}

	@Override
	public RunElementBatchData getData(final SimulationData simData) {
		RunElementBatchData data;
		data=(RunElementBatchData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementBatchData(this,batchSizeMin,batchSizeMax,simData);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	/**
	 * Überträge die erfassten Zeitdauern von den bisherigen Kundenobjekten gemäß der Konfiguration auf das Batch-Objekt.
	 * @param transferTimes	Übertragungsmodus
	 * @param waitingCount	Anzahl an Kunden im <code>clients</code>-Array
	 * @param clients	Array der bisherigen Kunden (es müssen nicht alle Einträge belegt sein; siehe <code>waitingCount</code>)
	 * @param batchedClient	Neues Batch-Kundenobjekt auf das die Zeitdauern übertragen werden sollen
	 */
	public static void transferTimes(final BatchRecord.DataTransferMode transferTimes, final int waitingCount, final RunDataClient[] clients, final RunDataClient batchedClient) {
		if (transferTimes==BatchRecord.DataTransferMode.OFF || waitingCount==0) return;

		long waitingTime=clients[0].waitingTime;
		long transferTime=clients[0].transferTime;
		long processTime=clients[0].processTime;
		long residenceTime=clients[0].residenceTime;

		for (int i=1;i<waitingCount;i++) {
			long waitingTimeClient=clients[i].waitingTime;
			long transferTimeClient=clients[i].transferTime;
			long processTimeClient=clients[i].processTime;
			long residenceTimeClient=clients[i].residenceTime;

			switch (transferTimes) {
			case OFF:
				/* Kann nicht auftreten, haben wir oben schon ausgeschlossen. */
				break;
			case MIN:
				waitingTime=Math.min(waitingTime,waitingTimeClient);
				transferTime=Math.min(transferTime,transferTimeClient);
				processTime=Math.min(processTime,processTimeClient);
				residenceTime=Math.min(residenceTime,residenceTimeClient);
				break;
			case MAX:
				waitingTime=Math.max(waitingTime,waitingTimeClient);
				transferTime=Math.max(transferTime,transferTimeClient);
				processTime=Math.max(processTime,processTimeClient);
				residenceTime=Math.max(residenceTime,residenceTimeClient);
				break;
			case MEAN:
				waitingTime+=waitingTimeClient;
				transferTime+=transferTimeClient;
				processTime+=processTimeClient;
				residenceTime+=residenceTimeClient;
				break;
			case SUM:
				waitingTime+=waitingTimeClient;
				transferTime+=transferTimeClient;
				processTime+=processTimeClient;
				residenceTime+=residenceTimeClient;
				break;
			case MULTIPLY:
				waitingTime*=waitingTimeClient;
				transferTime*=transferTimeClient;
				processTime*=processTimeClient;
				residenceTime*=residenceTimeClient;
				break;
			}
		}

		if (transferTimes==BatchRecord.DataTransferMode.MEAN) {
			waitingTime/=waitingCount;
			transferTime/=waitingCount;
			processTime/=waitingCount;
			residenceTime/=waitingCount;
		}

		batchedClient.waitingTime=waitingTime;
		batchedClient.transferTime=transferTime;
		batchedClient.processTime=processTime;
		batchedClient.residenceTime=residenceTime;
	}

	/**
	 * Überträge die erfassten Zeitdauern von den bisherigen Kundenobjekten gemäß der Konfiguration auf das Batch-Objekt.
	 * @param transferTimes	Übertragungsmodus
	 * @param waitingCount	Anzahl an Kunden im <code>clients</code>-Array
	 * @param clients	Array der bisherigen Kunden (es müssen nicht alle Einträge belegt sein; siehe <code>waitingCount</code>)
	 * @param batchedClient	Neues Batch-Kundenobjekt auf das die Zeitdauern übertragen werden sollen
	 */
	public static void transferTimes(final BatchRecord.DataTransferMode transferTimes, final int waitingCount, final List<RunDataClient> clients, final RunDataClient batchedClient) {
		if (transferTimes==BatchRecord.DataTransferMode.OFF || waitingCount==0) return;

		RunDataClient client;

		client=clients.get(0);
		long waitingTime=client.waitingTime;
		long transferTime=client.transferTime;
		long processTime=client.processTime;
		long residenceTime=client.residenceTime;

		for (int i=1;i<waitingCount;i++) {
			client=clients.get(i);

			long waitingTimeClient=client.waitingTime;
			long transferTimeClient=client.transferTime;
			long processTimeClient=client.processTime;
			long residenceTimeClient=client.residenceTime;

			switch (transferTimes) {
			case OFF:
				/* Kann nicht auftreten, haben wir oben schon ausgeschlossen. */
				break;
			case MIN:
				waitingTime=Math.min(waitingTime,waitingTimeClient);
				transferTime=Math.min(transferTime,transferTimeClient);
				processTime=Math.min(processTime,processTimeClient);
				residenceTime=Math.min(residenceTime,residenceTimeClient);
				break;
			case MAX:
				waitingTime=Math.max(waitingTime,waitingTimeClient);
				transferTime=Math.max(transferTime,transferTimeClient);
				processTime=Math.max(processTime,processTimeClient);
				residenceTime=Math.max(residenceTime,residenceTimeClient);
				break;
			case MEAN:
				waitingTime+=waitingTimeClient;
				transferTime+=transferTimeClient;
				processTime+=processTimeClient;
				residenceTime+=residenceTimeClient;
				break;
			case SUM:
				waitingTime+=waitingTimeClient;
				transferTime+=transferTimeClient;
				processTime+=processTimeClient;
				residenceTime+=residenceTimeClient;
				break;
			case MULTIPLY:
				waitingTime*=waitingTimeClient;
				transferTime*=transferTimeClient;
				processTime*=processTimeClient;
				residenceTime*=residenceTimeClient;
				break;
			}
		}

		if (transferTimes==BatchRecord.DataTransferMode.MEAN) {
			waitingTime/=waitingCount;
			transferTime/=waitingCount;
			processTime/=waitingCount;
			residenceTime/=waitingCount;
		}

		batchedClient.waitingTime=waitingTime;
		batchedClient.transferTime=transferTime;
		batchedClient.processTime=processTime;
		batchedClient.residenceTime=residenceTime;
	}

	/**
	 * Überträge die erfassten Nutzerdaten von den bisherigen Kundenobjekten gemäß der Konfiguration auf das Batch-Objekt.
	 * @param transferNumbers	Übertragungsmodus
	 * @param waitingCount	Anzahl an Kunden im <code>clients</code>-Array
	 * @param clients	Array der bisherigen Kunden (es müssen nicht alle Einträge belegt sein; siehe <code>waitingCount</code>)
	 * @param batchedClient	Neues Batch-Kundenobjekt auf das die Nutzerdaten übertragen werden sollen
	 */
	public static void transferNumbers(final BatchRecord.DataTransferMode transferNumbers, final int waitingCount, final RunDataClient[] clients, final RunDataClient batchedClient) {
		if (transferNumbers==BatchRecord.DataTransferMode.OFF || waitingCount==0) return;

		int maxIndex=clients[0].getMaxUserDataIndex();
		for (int i=1;i<waitingCount;i++) maxIndex=Math.max(maxIndex,clients[i].getMaxUserDataIndex());
		if (maxIndex<0) return;

		double[] userData=new double[maxIndex+1];
		for (int i=0;i<=clients[0].getMaxUserDataIndex();i++) userData[i]=clients[0].getUserData(i);

		double[] userDataClient=new double[maxIndex+1];
		for (int i=1;i<waitingCount;i++) {
			for (int j=0;j<=maxIndex;j++) userDataClient[j]=clients[i].getUserData(j);

			switch (transferNumbers) {
			case OFF:
				/* Kann nicht auftreten, haben wir oben schon ausgeschlossen. */
				return;
			case MIN:
				for (int j=0;j<=maxIndex;j++) userData[j]=Math.min(userData[j],userDataClient[j]);
				break;
			case MAX:
				for (int j=0;j<=maxIndex;j++) userData[j]=Math.max(userData[j],userDataClient[j]);
				break;
			case MEAN:
				for (int j=0;j<=maxIndex;j++) userData[j]+=userDataClient[j];
				break;
			case SUM:
				for (int j=0;j<=maxIndex;j++) userData[j]+=userDataClient[j];
				break;
			case MULTIPLY:
				for (int j=0;j<=maxIndex;j++) userData[j]*=userDataClient[j];
				break;
			}
		}

		if (transferNumbers==BatchRecord.DataTransferMode.MEAN) {
			for (int j=0;j<=maxIndex;j++) userData[j]/=waitingCount;
		}

		final boolean[] userDataInUse=new boolean[maxIndex+1];
		for (int i=0;i<=maxIndex;i++) userDataInUse[i]=(userData[i]!=0.0);
		batchedClient.setUserData(userData,userDataInUse);
	}

	/**
	 * Überträge die erfassten Nutzerdaten von den bisherigen Kundenobjekten gemäß der Konfiguration auf das Batch-Objekt.
	 * @param transferNumbers	Übertragungsmodus
	 * @param waitingCount	Anzahl an Kunden im <code>clients</code>-Array
	 * @param clients	Array der bisherigen Kunden (es müssen nicht alle Einträge belegt sein; siehe <code>waitingCount</code>)
	 * @param batchedClient	Neues Batch-Kundenobjekt auf das die Nutzerdaten übertragen werden sollen
	 */
	public static void transferNumbers(final BatchRecord.DataTransferMode transferNumbers, final int waitingCount, final List<RunDataClient> clients, final RunDataClient batchedClient) {
		if (transferNumbers==BatchRecord.DataTransferMode.OFF || waitingCount==0) return;

		RunDataClient client;

		client=clients.get(0);

		int maxIndex=client.getMaxUserDataIndex();
		for (int i=1;i<waitingCount;i++) maxIndex=Math.max(maxIndex,clients.get(i).getMaxUserDataIndex());
		if (maxIndex<0) return;

		double[] userData=new double[maxIndex+1];
		for (int i=0;i<=client.getMaxUserDataIndex();i++) userData[i]=client.getUserData(i);

		double[] userDataClient=new double[maxIndex+1];
		for (int i=1;i<waitingCount;i++) {
			client=clients.get(i);

			for (int j=0;j<=maxIndex;j++) userDataClient[j]=client.getUserData(j);

			switch (transferNumbers) {
			case OFF:
				/* Kann nicht auftreten, haben wir oben schon ausgeschlossen. */
				return;
			case MIN:
				for (int j=0;j<=maxIndex;j++) userData[j]=Math.min(userData[j],userDataClient[j]);
				break;
			case MAX:
				for (int j=0;j<=maxIndex;j++) userData[j]=Math.max(userData[j],userDataClient[j]);
				break;
			case MEAN:
				for (int j=0;j<=maxIndex;j++) userData[j]+=userDataClient[j];
				break;
			case SUM:
				for (int j=0;j<=maxIndex;j++) userData[j]+=userDataClient[j];
				break;
			case MULTIPLY:
				for (int j=0;j<=maxIndex;j++) userData[j]*=userDataClient[j];
				break;
			}
		}

		if (transferNumbers==BatchRecord.DataTransferMode.MEAN) {
			for (int j=0;j<=maxIndex;j++) userData[j]/=waitingCount;
		}

		final boolean[] userDataInUse=new boolean[maxIndex+1];
		for (int i=0;i<=maxIndex;i++) userDataInUse[i]=(userData[i]!=0.0);
		batchedClient.setUserData(userData,userDataInUse);
	}

	/**
	 * Die Batch-Größe wurde erreicht, Kunden werden weitergeleitet.<br>
	 * Modus: Kunden einfach gemeinsam weiterleiten
	 * @param simData	Simulationsdaten
	 * @param data	Thread-lokales Datenobjekt zu der Station
	 * @param newClient	Aktuell gerade eingetroffener Kunde (kann <code>null</code> sein)
	 * @see ui.modeleditor.elements.BatchRecord.BatchMode#BATCH_MODE_COLLECT
	 */
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
			data.clients[i].addStationTime(id,waitingTime,0,0,waitingTime);

			/* Kunden an Station in Statistik */
			simData.runData.logClientLeavesStationQueue(simData,this,data,data.clients[i]);

			/* Kunde zur nächsten Station leiten */
			StationLeaveEvent.addLeaveEvent(simData,data.clients[i],this,0);
		}
		data.waiting=0;
	}

	/**
	 * Die Batch-Größe wurde erreicht, Kunden werden weitergeleitet.<br>
	 * Modus: Temporären Batch bilden
	 * @param simData	Simulationsdaten
	 * @param data	Thread-lokales Datenobjekt zu der Station
	 * @param newClient	Aktuell gerade eingetroffener Kunde (kann <code>null</code> sein)
	 * @see ui.modeleditor.elements.BatchRecord.BatchMode#BATCH_MODE_TEMPORARY
	 */
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
		final RunDataClient batchedClient=simData.runData.clients.getClient(newClientType,simData,id);

		for (int i=0;i<data.waiting;i++) {
			/* Wartezeit in Statistik */
			final long waitingTime=simData.currentTime-data.clientAddTime[i];
			simData.runData.logStationProcess(simData,this,data.clients[i],waitingTime,0,0,waitingTime);
			data.clients[i].addStationTime(id,waitingTime,0,0,waitingTime);
		}

		/* Daten von den alten Kunden auf den neuen Batch-Kunden übertragen */
		transferTimes(transferTimes,data.waiting,data.clients,batchedClient);
		transferNumbers(transferNumbers,data.waiting,data.clients,batchedClient);

		for (int i=0;i<data.waiting;i++) {
			/* Kunden an Station in Statistik */
			simData.runData.logClientLeavesStationQueue(simData,this,data,data.clients[i]);

			/* Ist der Kunde als "letzter Kunde" markiert? */
			if (data.clients[i].isLastClient) batchedClient.isLastClient=true;

			/* Kunde in Batch aufnehmen */
			batchedClient.addBatchClient(data.clients[i]);

			/* Kunde verlässt Station (wird sonst über die Events realisiert) */
			simData.runData.logClientLeavesStation(simData,this,data,data.clients[i]);
			if (parentId>=0) simData.runData.logClientLeavesStation(simData,simData.runModel.elementsFast[parentId],null,data.clients[i]);
		}
		data.waiting=0;

		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.BatchNewClient"),String.format(Language.tr("Simulation.Log.BatchNewClient.Info"),batchedClient.logInfo(simData),name));

		/* Kunde betritt Station (wird sonst über die Events realisiert) */
		simData.runData.logClientEntersStation(simData,this,data,batchedClient);
		if (parentId>=0) simData.runData.logClientEntersStation(simData,simData.runModel.elementsFast[parentId],null,batchedClient);

		/* Maximalzahl an Kunden im System eingehalten */
		if (!simData.testMaxAllowedClientsInSystem()) return;

		/* Kunden weiterleiten */
		StationLeaveEvent.addLeaveEvent(simData,batchedClient,this,0);
	}

	/**
	 * Die Batch-Größe wurde erreicht, Kunden werden weitergeleitet.<br>
	 * Modus: Permanenten Batch bilden
	 * @param simData	Simulationsdaten
	 * @param data	Thread-lokales Datenobjekt zu der Station
	 * @param newClient	Aktuell gerade eingetroffener Kunde (kann <code>null</code> sein)
	 * @see ui.modeleditor.elements.BatchRecord.BatchMode#BATCH_MODE_PERMANENT
	 */
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

		/* Neuen Kunden anlegen */
		final RunDataClient batchedClient=simData.runData.clients.getClient(newClientType,simData,id);

		for (int i=0;i<data.waiting;i++) {
			/* Wartezeit in Statistik */
			final long waitingTime=simData.currentTime-data.clientAddTime[i];
			simData.runData.logStationProcess(simData,this,data.clients[i],waitingTime,0,0,waitingTime);
			data.clients[i].addStationTime(id,waitingTime,0,0,waitingTime);
		}

		/* Daten von den alten Kunden auf den neuen Batch-Kunden übertragen */
		transferTimes(transferTimes,data.waiting,data.clients,batchedClient);
		transferNumbers(transferNumbers,data.waiting,data.clients,batchedClient);

		boolean isLastClient=false;
		for (int i=0;i<data.waiting;i++) {
			/* Kunden an Station in Statistik */
			simData.runData.logClientLeavesStationQueue(simData,this,data,data.clients[i]);

			/* Ist der Kunde als "letzter Kunde" markiert? */
			isLastClient=isLastClient || data.clients[i].isLastClient;

			/* Kunde final in Statistik erfassen und Objekt recyceln */
			simData.runData.clients.disposeClient(data.clients[i],simData);

			/* Kunde verlässt Station (wird sonst über die Events realisiert) */
			simData.runData.logClientLeavesStation(simData,this,data,data.clients[i]);
			if (parentId>=0) simData.runData.logClientLeavesStation(simData,simData.runModel.elementsFast[parentId],null,data.clients[i]);
		}
		data.waiting=0;

		batchedClient.isLastClient=isLastClient;

		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.BatchNewClient"),String.format(Language.tr("Simulation.Log.BatchNewClient.Info"),batchedClient.logInfo(simData),name));

		/* Kunde betritt Station (wird sonst über die Events realisiert) */
		simData.runData.logClientEntersStation(simData,this,data,batchedClient);
		if (parentId>=0) simData.runData.logClientEntersStation(simData,simData.runModel.elementsFast[parentId],null,batchedClient);

		/* Maximalzahl an Kunden im System eingehalten */
		if (!simData.testMaxAllowedClientsInSystem()) return;

		/* Kunden weiterleiten */
		StationLeaveEvent.addLeaveEvent(simData,batchedClient,this,0);
	}

	/**
	 * Die Batch-Größe wurde erreicht, Kunden werden weitergeleitet.
	 * @param simData	Simulationsdaten
	 * @param data	Thread-lokales Datenobjekt zu der Station
	 * @param newClient	Aktuell gerade eingetroffener Kunde (kann <code>null</code> sein)
	 */
	private void processSend(final SimulationData simData, final RunElementBatchData data, final RunDataClient newClient) {
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
			if (data.waiting<data.batchSizeMin) return; /* Wurden wohl inzwischen schon weitergeleitet, Event war überflüssig. */
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

	@Override
	public boolean isInterarrivalByQueueStation(final SimulationData simData) {
		final RunElementBatchData data=getData(simData);
		return data.batchSizeMax<=50;
	}
}

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
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementSourceRecord;
import ui.modeleditor.elements.ModelElementSplit;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementSplit</code>
 * @author Alexander Herzog
 * @see ModelElementSplit
 */
public class RunElementSplit extends RunElementPassThrough {
	private RunElementSourceRecord[] records;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementSplit(final ModelElementSplit element) {
		super(element,buildName(element,Language.tr("Simulation.Element.Split.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementSplit)) return null;
		final ModelElementSplit splitElement=(ModelElementSplit)element;
		final RunElementSplit split=new RunElementSplit(splitElement);

		/* Auslaufende Kante */
		final String edgeError=split.buildEdgeOut(splitElement);
		if (edgeError!=null) return edgeError;

		/* Neue Kundentypen */
		final List<RunElementSourceRecord> list=new ArrayList<>();
		for (ModelElementSourceRecord editRecord: ((ModelElementSplit)element).getRecords()) {
			final RunElementSourceRecord record=new RunElementSourceRecord();
			final RunModelCreatorStatus error=record.load(editRecord,null,element.getId(),editModel,runModel,list.size());
			if (!error.isOk()) return error.message;
			list.add(record);
		}
		split.records=list.toArray(new RunElementSourceRecord[0]);

		return split;
	}

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementSplit)) return null;
		final ModelElementSplit splitElement=(ModelElementSplit)element;

		/* Auslaufende Kante */
		final RunModelCreatorStatus edgeError=testEdgeOut(splitElement);
		if (edgeError!=null) return edgeError;

		/* Neue Kundentypen */
		for (ModelElementSourceRecord editRecord: ((ModelElementSplit)element).getRecords()) {
			final RunModelCreatorStatus error=RunElementSourceRecord.test(editRecord,null,element.getId());
			if (!error.isOk()) return error;
		}

		return RunModelCreatorStatus.ok;
	}

	@Override
	public RunElementSplitData getData(final SimulationData simData) {
		RunElementSplitData data;
		data=(RunElementSplitData)(simData.runData.getStationData(this));
		if (data==null) {
			final RunElementSourceRecord.SourceSetExpressions[] setData=new RunElementSourceRecord.SourceSetExpressions[records.length];
			final String[] batchSizes=new String[records.length];
			for (int i=0;i<records.length;i++) {
				setData[i]=records[i].getRuntimeExpressions(simData.runModel.variableNames);
				batchSizes[i]=records[i].batchSize;
			}
			data=new RunElementSplitData(this,simData.runModel.variableNames,setData,batchSizes);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	private void disposeOldClient(final SimulationData simData, final RunDataClient client) {
		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.Dispose"),String.format(Language.tr("Simulation.Log.Dispose.Info"),client.logInfo(simData),name));

		/* Simulation beenden */
		if (client.isLastClient) {
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.EndOfSimulation"),Language.tr("Simulation.Log.EndOfSimulation.FinalClientLeftSystem"));
			simData.eventManager.deleteAllEvents();
			simData.runData.stopp=true;
		}

		/* Notify-System über Kundenausgang informieren */
		client.lastStationID=id;
		client.nextStationID=-1;
		simData.runData.fireClientMoveNotify(simData,client,false);

		/* Kunde in Statistik erfassen und Objekt recyceln */
		simData.runData.clients.disposeClient(client,simData);
	}

	private void processArrivalEvent(final SimulationData simData, final int index, final boolean isWarmUpClient, final boolean isLastClient) {
		final RunElementSplitData data=getData(simData);

		final int batchSize;
		if (data.batchSizes[index]!=null) {
			batchSize=(int)Math.round(data.batchSizes[index].calcOrDefault(simData.runData.variableValues,-1));
			if (batchSize<=0) {
				simData.doEmergencyShutDown(String.format(Language.tr("Simulation.Log.InvalidBatchSize"),name));
				return;
			}
		} else {
			batchSize=records[index].getMultiBatchSize(simData);
		}

		for (int i=1;i<=batchSize;i++) {

			/* Kunde anlegen */
			final RunDataClient newClient=simData.runData.clients.getClient(records[index].clientType,simData);
			newClient.isWarmUp=isWarmUpClient;
			newClient.isLastClient=isLastClient;

			/* Zahlen und Strings zuweisen */
			records[index].writeStringsToClient(newClient);
			data.setData[index].writeNumbersToClient(simData,newClient,name);

			/* Notify-System über Kundenankunft informieren */
			newClient.nextStationID=id;
			simData.runData.fireClientMoveNotify(simData,newClient,false);

			/* Logging */
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.SourceArrival"),String.format(Language.tr("Simulation.Log.SourceArrival.Info"),newClient.logInfo(simData),simData.runData.getWarmUpStatus(),name,simData.runData.clientsArrived));

			/* Zwischenankunftszeiten in der Statistik erfassen */
			simData.runData.logStationArrival(simData.currentTime,simData,this,data,newClient);

			/* Kunden zur ersten (echten) Station leiten */
			StationLeaveEvent.addLeaveEvent(simData,newClient,this,0);
		}

		/* System über Status-Änderung benachrichtigen */
		simData.runData.fireStateChangeNotify(simData);

		/* Maximalzahl an Kunden im System eingehalten */
		if (!simData.testMaxAllowedClientsInSystem()) return;
	}

	@Override
	public void processArrival(final SimulationData simData, final RunDataClient client) {
		disposeOldClient(simData,client);
		for (int index=0;index<records.length;index++) processArrivalEvent(simData,index,client.isWarmUp,client.isLastClient);
	}
}

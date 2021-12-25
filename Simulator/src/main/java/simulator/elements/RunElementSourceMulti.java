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

import org.apache.commons.math3.util.FastMath;

import language.Language;
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElement;
import simulator.editmodel.EditModel;
import simulator.events.StationLeaveEvent;
import simulator.events.SystemArrivalEvent;
import simulator.events.SystemChangeEvent;
import simulator.runmodel.RunData;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementSourceMulti;
import ui.modeleditor.elements.ModelElementSourceRecord;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementSourceMulti</code>
 * @author Alexander Herzog
 * @see ModelElementSourceMulti
 */
public class RunElementSourceMulti extends RunElement implements StateChangeListener, SignalListener, RunSource {
	/** ID der Folgestation */
	private int connectionId;
	/** Folgestation (Übersetzung aus {@link #connectionId}) */
	private RunElement connection;

	/** Kundenquelle-Datensätze */
	private RunElementSourceRecord[] records;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementSourceMulti(final ModelElementSourceMulti element) {
		super(element,buildName(element,Language.tr("Simulation.Element.SourceMulti.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementSourceMulti)) return null;

		RunElementSourceMulti run=new RunElementSourceMulti((ModelElementSourceMulti)element);

		final List<RunElementSourceRecord> list=new ArrayList<>();
		for (ModelElementSourceRecord editRecord: ((ModelElementSourceMulti)element).getRecords()) {
			if (!editRecord.isActive()) continue;
			final RunElementSourceRecord record=new RunElementSourceRecord();
			final RunModelCreatorStatus error=record.load(editRecord,null,element.getId(),editModel,runModel,list.size());
			if (!error.isOk()) return error.message;
			list.add(record);
		}
		run.records=list.toArray(new RunElementSourceRecord[0]);

		run.connectionId=findNextId(((ModelElementSourceMulti)element).getEdgeOut());
		if (run.connectionId<0) return String.format(Language.tr("Simulation.Creator.NoEdgeOut"),element.getId());

		return run;
	}

	@Override
	public RunModelCreatorStatus test(ModelElement element) {
		if (!(element instanceof ModelElementSourceMulti)) return null;

		if (findNextId(((ModelElementSourceMulti)element).getEdgeOut())<0) return RunModelCreatorStatus.noEdgeOut(element);

		for (ModelElementSourceRecord editRecord: ((ModelElementSourceMulti)element).getRecords()) {
			if (!editRecord.isActive()) continue;
			final RunModelCreatorStatus error=RunElementSourceRecord.test(editRecord,null,element.getId());
			if (!error.isOk()) return error;
		}

		return RunModelCreatorStatus.ok;
	}


	@Override
	public void prepareRun(final RunModel runModel) {
		connection=runModel.elements.get(connectionId);
	}

	@Override
	public RunElementSourceMultiData getData(final SimulationData simData) {
		RunElementSourceMultiData data;
		data=(RunElementSourceMultiData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementSourceMultiData(this,simData,records,simData.runModel.variableNames);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	@Override
	public void processArrival(SimulationData simData, RunDataClient client) {
		/* Wird nie aufgerufen: Source-Elemente haben keine einlaufenden Kanten bzw. führen keine Verarbeitung von Kunden durch. */
	}

	@Override
	public void processLeave(SimulationData simData, RunDataClient client) {
		/* Wird nie aufgerufen: Source-Elemente haben keine einlaufenden Kanten bzw. führen keine Verarbeitung von Kunden durch. */
	}

	/**
	 * Plant die nächste Kundenankunft (bzw. Batch-Ankunft) ein
	 * @param simData	Simulationsdatenobjekt
	 * @param isFirstArrival	Gibt an, ob es sich um die erste Ankunft durch dieses Element handelt
	 * @param index	0-basierender Index der Teil-Kundenquelle
	 * @return	Gibt an, ob Ankünfte eingeplant wurden
	 */
	public boolean scheduleNextArrival(final SimulationData simData, final boolean isFirstArrival, final int index) {
		final RunElementSourceMultiData data=getData(simData);
		final int count=records[index].scheduleNextArrival(simData,isFirstArrival,data.recordData[index],this,name);
		if (count>0) {
			data.recordData[index].arrivalCount+=count;
			data.recordData[index].arrivalTime=simData.currentTime;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Plant die nächste Kundenankunft (bzw. Batch-Ankunft) an jeder Teil-Quelle ein.<br>
	 * Es handelt sich hierbei immer um die ersten Kunden jeder Teilquelle (also <code>isFirstArrival=true</code>)<br>
	 * Aufruf über das {@link RunData#initRun(long, SimulationData, boolean)} über das {@link RunSource}-Interface
	 * @param simData	Simulationsdatenobjekt
	 */
	@Override
	public void scheduleInitialArrivals(SimulationData simData) {
		for (int i=0;i<records.length;i++) scheduleNextArrival(simData,true,i);
	}

	/**
	 * Dieser Wert wird in {@link #processArrivalEvent(SimulationData, boolean, int)}
	 * einmalig berechnet und hier gespeichert. Es gibt an, nach wie vielen Ankünften
	 * die Simulation abgebrochen wird (weil es offenbar einen Fehler gegeben haben
	 * muss, der ein reguläres Ende verhindert). Es handelt sich um den 1000-fachen
	 * Wert der eigentlich für den Thread geplanten Anzahl an Ankünften.
	 * @see #processArrivalEvent(SimulationData, boolean, int)
	 */
	private long systemMaxArrival=-1;

	/**
	 * Aufruf über das {@link SystemArrivalEvent} über das {@link RunSource}-Interface
	 */
	@Override
	public void processArrivalEvent(final SimulationData simData, final boolean scheduleNext, final int index) {
		final RunElementSourceMultiData data=getData(simData);
		boolean isLastClient=false;

		boolean batchArrivals=true;
		final int batchSize;
		if (data.recordData[index].batchSize!=null) {
			if (data.recordData[index].batchSize.isConstValue()) batchArrivals=(data.recordData[index].batchSize.getConstValue()!=1.0);
			batchSize=(int)Math.round(data.recordData[index].batchSize.calcOrDefault(simData.runData.variableValues,-1));
			if (batchSize<=0) {
				simData.doEmergencyShutDown(String.format(Language.tr("Simulation.Log.InvalidBatchSize"),name));
				return;
			}
		} else {
			batchSize=records[index].getMultiBatchSize(simData);
		}
		data.recordData[index].arrivalClientCount+=batchSize;

		if (batchArrivals) {
			/* Zwischenankunftszeiten auf Batch-Basis in der Statistik erfassen */
			simData.runData.logStationBatchArrival(simData.currentTime,simData,this,data);
		}

		for (int i=1;i<=batchSize;i++) {

			/* Kunde anlegen */
			final RunDataClient newClient=simData.runData.clients.getClient(records[index].clientType,simData);

			/* Zahlen und Strings zuweisen */
			records[index].writeStringsToClient(newClient);
			data.recordData[index].setData.writeNumbersToClient(simData,newClient,name);

			/* Notify-System über Kundenankunft informieren */
			newClient.nextStationID=id;
			simData.runData.fireClientMoveNotify(simData,newClient,false);

			/* Zähler erhöhen, um festzustellen, wann die Simulation beendet werden kann */
			simData.runData.clientsArrived++;

			/* Logging */
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.SourceArrival"),String.format(Language.tr("Simulation.Log.SourceArrival.Info"),newClient.logInfo(simData),simData.runData.getWarmUpStatus(),name,simData.runData.clientsArrived));

			/* Evtl. WarmUp-Zeit beenden */
			if (simData.runData.isWarmUp) {
				/* Warm-Up-Phasenlänge wird nicht durch Threadanzahl geteilt, sondern auf jedem Kern wird die angegebene Anzahl simuliert */
				if (simData.runData.clientsArrived>=FastMath.round(simData.runModel.warmUpTime*simData.runModel.clientCountModel)) {
					simData.runData.isWarmUp=false;
					simData.endWarmUp();
					simData.runData.clientsArrived=0;
					for (int j=0;j<data.recordData.length;j++) {
						data.recordData[j].arrivalCount=0;
						data.recordData[j].arrivalClientCount=0;
					}
					/* Logging */
					if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.WarmUpEnd"),Language.tr("Simulation.Log.WarmUpEnd.Info"));
				}
			}

			/* Zwischenankunftszeiten in der Statistik erfassen */
			simData.runData.logStationArrival(simData.currentTime,simData,this,data,newClient);

			/* Wenn Ziel-Anzahl an Ankünften erreicht: Kunden Marker mitgeben, dass bei seiner Ankunft im Ziel die Simulation endet.*/
			if (simData.runData.nextClientIsLast(simData)) isLastClient=true;
			newClient.isLastClient=isLastClient;

			/* Tatsache, dass Simulation endet, ggf. loggen */
			if (isLastClient && simData.loggingActive) log(simData,Language.tr("Simulation.Log.EndOfSimulation"),Language.tr("Simulation.Log.EndOfSimulation.LastClientGenerated"));

			/* Kunden zur ersten (echten) Station leiten */
			StationLeaveEvent.sendToStation(simData,newClient,this,connection);
		}

		if (scheduleNext) {
			/* Ankunft des nächsten Kunden einplanen */
			if (isLastClient && !simData.runData.isWarmUp && systemMaxArrival<=0 && simData.runModel.clientCount>=0) systemMaxArrival=FastMath.max(1000,FastMath.max(simData.runData.clientsArrived*3/2,2*simData.runModel.clientCount/simData.runModel.clientCountDiv));
			if (simData.runData.isWarmUp || simData.runModel.clientCount<0 || systemMaxArrival<=0 || simData.runData.clientsArrived<systemMaxArrival) {
				boolean done=false;
				if (records[index].maxArrivalCount>=0 && data.recordData[index].arrivalCount>=records[index].maxArrivalCount) {
					done=true;
					if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.ArrivalCount"),String.format(Language.tr("Simulation.Log.ArrivalCount.Info"),name,records[index].maxArrivalCount));
				}
				if (records[index].maxArrivalClientCount>=0 && data.recordData[index].arrivalClientCount>=records[index].maxArrivalClientCount) {
					done=true;
					if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.ArrivalClientCount"),String.format(Language.tr("Simulation.Log.ArrivalClientCount.Info"),name,records[index].maxArrivalCount));
				}
				if (!done) {
					if (records[index].condition==null) {
						/* Einplanung der nächsten Ankunft */
						scheduleNextArrival(simData,false,index);
					} else {
						/* Prüfung der Bedingung zu passender Zeit auslösen */
						SystemChangeEvent.triggerEvent(simData,records[index].conditionMinDistanceMS);
					}
				}
			} else {
				simData.doEmergencyShutDown(Language.tr("Simulation.Log.EndOfSimulation.LastClientStillInSystem"));
			}
		}

		/* System über Status-Änderung benachrichtigen */
		simData.runData.fireStateChangeNotify(simData);

		/* Maximalzahl an Kunden im System eingehalten */
		if (!simData.testMaxAllowedClientsInSystem()) return;
	}

	@Override
	public boolean isClientCountStation() {
		return false;
	}

	@Override
	public boolean systemStateChangeNotify(SimulationData simData) {
		boolean arrivalsScheduled=false;
		RunElementSourceMultiData data=null;
		for (int i=0;i<records.length;i++) {
			if (records[i].condition!=null) {
				if (scheduleNextArrival(simData,false,i)) arrivalsScheduled=true;
			}
			if (records[i].thresholdExpression!=null) {
				if (data==null) data=getData(simData);
				if (data.recordData[i].checkThreshold(simData,data)) {
					final int count=records[i].triggertByThreshold(simData,this,data.recordData[i].arrivalCount,data.recordData[i].arrivalClientCount);
					if (count>0) {
						data.recordData[i].arrivalCount+=count;
						data.recordData[i].arrivalTime=simData.currentTime;
						arrivalsScheduled=true;
					}
				}
			}
		}
		return arrivalsScheduled;
	}

	@Override
	public void signalNotify(SimulationData simData, String signalName) {
		final RunElementSourceMultiData data=getData(simData);
		for (int i=0;i<records.length;i++) {
			final int count=records[i].triggeredBySignal(simData,signalName,this,data.recordData[i].arrivalCount,data.recordData[i].arrivalClientCount);
			if (count>0) {
				data.recordData[i].arrivalCount+=count;
				data.recordData[i].arrivalTime=simData.currentTime;
			}
		}
	}

	@Override
	public RunElement getNext() {
		return connection;
	}
}

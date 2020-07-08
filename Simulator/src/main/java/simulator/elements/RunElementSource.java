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

import org.apache.commons.math3.util.FastMath;

import language.Language;
import mathtools.NumberTools;
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElement;
import simulator.editmodel.EditModel;
import simulator.events.StationLeaveEvent;
import simulator.events.SystemArrivalEvent;
import simulator.events.SystemChangeEvent;
import simulator.runmodel.RunData;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunDataClients;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementSource;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementSource</code>
 * @author Alexander Herzog
 * @see ModelElementSource
 */
public class RunElementSource extends RunElement implements StateChangeListener, SignalListener, RunSource {
	private int connectionId;
	private RunElement connection;

	private RunElementSourceRecord record;

	private long warmUpClients;

	/** Kundentyp name der an dieser Station generierten Kunden */
	public String clientTypeName;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementSource(final ModelElementSource element) {
		super(element,buildName(element,Language.tr("Simulation.Element.Source.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementSource)) return null;

		final RunElementSource run=new RunElementSource((ModelElementSource)element);

		run.record=new RunElementSourceRecord();
		final RunModelCreatorStatus error=run.record.load(((ModelElementSource)element).getRecord(),((ModelElementSource)element).getName(),element.getId(),editModel,runModel,0);
		if (!error.isOk()) return error.message;
		run.clientTypeName=run.record.clientTypeName;

		run.connectionId=findNextId(((ModelElementSource)element).getEdgeOut());
		if (run.connectionId<0) return String.format(Language.tr("Simulation.Creator.NoEdgeOut"),element.getId());

		run.warmUpClients=FastMath.round(runModel.warmUpTime*runModel.clientCount);

		return run;
	}

	@Override
	public RunModelCreatorStatus test(ModelElement element) {
		if (!(element instanceof ModelElementSource)) return null;

		if (findNextId(((ModelElementSource)element).getEdgeOut())<0) return RunModelCreatorStatus.noEdgeOut(element);

		return RunElementSourceRecord.test(((ModelElementSource)element).getRecord(),((ModelElementSource)element).getName(),element.getId());
	}

	@Override
	public void prepareRun(final RunModel runModel) {
		connection=runModel.elements.get(connectionId);
	}

	@Override
	public RunElementSourceData getData(final SimulationData simData) {
		RunElementSourceData data;
		data=(RunElementSourceData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementSourceData(this,record.batchSize,record.expression,record.condition,record.thresholdExpression,record.thresholdValue,record.thresholdDirectionUp,simData.runModel.variableNames,record.getRuntimeExpressions(simData.runModel.variableNames));
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
	 * @return	Gibt an, ob Ankünfte eingeplant wurden
	 */
	private boolean scheduleNextArrival(final SimulationData simData, final boolean isFirstArrival) {
		final RunElementSourceData data=getData(simData);
		final int count=record.scheduleNextArrival(simData,isFirstArrival,data.expression,data.condition,data.arrivalTime,this,name,data.arrivalCount,data.arrivalClientCount);
		if (count>0) {
			data.arrivalCount+=count;
			data.arrivalTime=simData.currentTime;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Aufruf über das {@link RunData#initRun(long, SimulationData, boolean)} über das {@link RunSource}-Interface
	 * @param simData	Simulationsdatenobjekt
	 */
	@Override
	public void scheduleInitialArrivals(SimulationData simData) {
		scheduleNextArrival(simData,true);
	}

	/**
	 * Aufruf über das {@link SystemArrivalEvent} über das {@link RunSource}-Interface
	 */
	@Override
	public void processArrivalEvent(final SimulationData simData, final boolean scheduleNext, final int index) {
		/* "index" wird von diesem Source-Type nicht verwendet */
		final RunElementSourceData data=getData(simData);
		boolean isLastClient=false;

		final int batchSize;
		if (data.batchSize!=null) {
			batchSize=(int)Math.round(data.batchSize.calcOrDefault(simData.runData.variableValues,-1));
			if (batchSize<=0) {
				simData.doEmergencyShutDown(String.format(Language.tr("Simulation.Log.InvalidBatchSize"),name));
				return;
			}
		} else {
			batchSize=record.getMultiBatchSize(simData);
		}
		data.arrivalClientCount+=batchSize;

		for (int i=1;i<=batchSize;i++) {

			/* Kunde anlegen */
			final RunDataClient newClient=simData.runData.clients.getClient(record.clientType,simData);

			/* Zahlen und Strings zuweisen */
			data.setData.writeNumbersToClient(simData,newClient,name);
			record.writeStringsToClient(newClient);

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
				if (simData.runData.clientsArrived>=warmUpClients) {
					simData.runData.isWarmUp=false;
					simData.endWarmUp();
					simData.runData.clientsArrived=0;
					data.arrivalCount=0;
					data.arrivalClientCount=0;
					/* Logging */
					if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.WarmUpEnd"),Language.tr("Simulation.Log.WarmUpEnd.Info"));
				}
			}

			/* Zwischenankunftszeiten in der Statistik erfassen */
			simData.runData.logStationArrival(simData.currentTime,simData,this,data,newClient);

			/* Wenn Ziel-Anzahl an Ankünften erreicht: Kunden Marker mitgeben, dass bei seiner Ankunft im Ziel die Simulation endet.*/
			if (!simData.runData.isWarmUp && simData.runModel.clientCount>0 && simData.runData.clientsArrived>=simData.runModel.clientCount/simData.clientCountDiv) isLastClient=true;
			newClient.isLastClient=isLastClient;

			/* Tatsache, dass Simulation endet, ggf. loggen */
			if (isLastClient && simData.loggingActive) log(simData,Language.tr("Simulation.Log.EndOfSimulation"),Language.tr("Simulation.Log.EndOfSimulation.LastClientGenerated"));

			/* Kunden zur ersten (echten) Station leiten */
			StationLeaveEvent.sendToStation(simData,newClient,this,connection);
		}

		if (scheduleNext) {
			if (data.maxSystemArrival<=0) data.maxSystemArrival=FastMath.max(1000,2*simData.runModel.clientCount/simData.clientCountDiv);
			/* Ankunft des nächsten Kunden einplanen */
			if (simData.runData.isWarmUp || simData.runModel.clientCount<0 || simData.runData.clientsArrived<data.maxSystemArrival) {
				boolean done=false;
				if (record.maxArrivalCount>=0 && data.arrivalCount>=record.maxArrivalCount) {
					done=true;
					if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.ArrivalCount"),String.format(Language.tr("Simulation.Log.ArrivalCount.Info"),name,record.maxArrivalCount));
				}
				if (record.maxArrivalClientCount>=0 && data.arrivalClientCount>=record.maxArrivalClientCount) {
					done=true;
					if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.ArrivalClientCount"),String.format(Language.tr("Simulation.Log.ArrivalClientCount.Info"),name,record.maxArrivalCount));
				}
				if (!done) {
					if (record.condition==null) {
						/* Einplanung der nächsten Ankunft */
						scheduleNextArrival(simData,false);
					} else {
						/* Prüfung der Bedingung zu passender Zeit auslösen */
						SystemChangeEvent.triggerEvent(simData,record.conditionMinDistanceMS);
					}
				}
			} else {
				simData.doEmergencyShutDown(Language.tr("Simulation.Log.EndOfSimulation.LastClientStillInSystem"));
			}
		}

		/* System über Status-Änderung benachrichtigen */
		simData.runData.fireStateChangeNotify(simData);

		final int count=simData.statistics.clientsInSystem.getCurrentState();
		if (count>RunDataClients.MAX_CLIENTS_IN_SYSTEM) {
			simData.doEmergencyShutDown(String.format(Language.tr("Simulation.Log.ToManyClientsInSystem.Info"),NumberTools.formatLong(count)));
		}
	}

	@Override
	public boolean isClientCountStation() {
		return false;
	}

	@Override
	public boolean systemStateChangeNotify(SimulationData simData) {
		if (record.condition==null && record.thresholdExpression==null) {
			/* Ausklinken */
			simData.runData.removeStateChangeListener(this);
			return false;
		}

		boolean arrivalsScheduled=false;

		if (record.condition!=null) {
			if (scheduleNextArrival(simData,false)) arrivalsScheduled=true;
		}

		if (record.thresholdExpression!=null) {
			final RunElementSourceData data=getData(simData);
			if (data.checkThreshold(simData)) {
				final int count=record.triggertByThreshold(simData,this,data.arrivalCount,data.arrivalClientCount);
				if (count>0) {
					data.arrivalCount+=count;
					data.arrivalTime=simData.currentTime;
					arrivalsScheduled=true;
				}
			}
		}

		return arrivalsScheduled;
	}

	@Override
	public void signalNotify(SimulationData simData, String signalName) {
		final RunElementSourceData data=getData(simData);
		final int count=record.triggeredBySignal(simData,signalName,this,data.arrivalCount,data.arrivalClientCount);
		if (count>0) {
			data.arrivalCount+=count;
			data.arrivalTime=simData.currentTime;
		}
	}

	@Override
	public RunElement getNext() {
		return connection;
	}
}

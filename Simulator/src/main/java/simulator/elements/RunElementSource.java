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
import ui.modeleditor.elements.ModelElementSource;
import ui.modeleditor.elements.ModelElementSub;

/**
 * �quivalent zu <code>ModelElementSource</code>
 * @author Alexander Herzog
 * @see ModelElementSource
 */
public class RunElementSource extends RunElement implements StateChangeListener, SignalListener, RunSource {
	/** ID der Folgestation */
	private int connectionId;
	/** Folgestation (�bersetzung aus {@link #connectionId}) */
	private RunElement connection;

	/** Kundenquelle-Datensatz */
	private RunElementSourceRecord record;

	/** Anzahl an Kunden (global �ber alle Quellen), die als Warm-up-Kunden gez�hlt werden sollen (SimulationData#endWarmUp()) */
	private long warmUpClients;

	/** Kundentyp name der an dieser Station generierten Kunden */
	public String clientTypeName;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugeh�riges Editor-Element
	 */
	public RunElementSource(final ModelElementSource element) {
		super(element,buildName(element,Language.tr("Simulation.Element.Source.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementSource)) return null;

		final RunElementSource run=new RunElementSource((ModelElementSource)element);

		run.record=new RunElementSourceRecord();
		final RunModelCreatorStatus error=run.record.load(((ModelElementSource)element).getRecord(),element.getName(),element.getId(),editModel,runModel,0);
		if (!error.isOk()) return error.message;
		run.clientTypeName=run.record.clientTypeName;

		run.connectionId=findNextId(((ModelElementSource)element).getEdgeOut());
		if (run.connectionId<0) return String.format(Language.tr("Simulation.Creator.NoEdgeOut"),element.getId());

		run.warmUpClients=FastMath.round(runModel.warmUpTime*runModel.clientCountModel);

		return run;
	}

	@Override
	public RunModelCreatorStatus test(ModelElement element) {
		if (!(element instanceof ModelElementSource)) return null;

		if (findNextId(((ModelElementSource)element).getEdgeOut())<0) return RunModelCreatorStatus.noEdgeOut(element);

		return RunElementSourceRecord.test(((ModelElementSource)element).getRecord(),element.getName(),element.getId());
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
			data=new RunElementSourceData(this,simData,record,simData.runModel.variableNames);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	@Override
	public void processArrival(SimulationData simData, RunDataClient client) {
		/* Wird nie aufgerufen: Source-Elemente haben keine einlaufenden Kanten bzw. f�hren keine Verarbeitung von Kunden durch. */
	}

	@Override
	public void processLeave(SimulationData simData, RunDataClient client) {
		/* Wird nie aufgerufen: Source-Elemente haben keine einlaufenden Kanten bzw. f�hren keine Verarbeitung von Kunden durch. */
	}

	/**
	 * Plant die n�chste Kundenankunft (bzw. Batch-Ankunft) ein
	 * @param simData	Simulationsdatenobjekt
	 * @param isFirstArrival	Gibt an, ob es sich um die erste Ankunft durch dieses Element handelt
	 * @return	Gibt an, ob Ank�nfte eingeplant wurden
	 */
	private boolean scheduleNextArrival(final SimulationData simData, final boolean isFirstArrival) {
		final RunElementSourceData data=getData(simData);
		final int count=record.scheduleNextArrival(simData,isFirstArrival,data.recordData,this,name);
		if (count>0) {
			data.recordData.arrivalCount+=count;
			data.recordData.arrivalTime=simData.currentTime;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Aufruf �ber das {@link RunData#initRun(long, SimulationData, boolean)} �ber das {@link RunSource}-Interface
	 * @param simData	Simulationsdatenobjekt
	 */
	@Override
	public void scheduleInitialArrivals(SimulationData simData) {
		scheduleNextArrival(simData,true);
	}

	/**
	 * Aufruf �ber das {@link SystemArrivalEvent} �ber das {@link RunSource}-Interface
	 */
	@Override
	public void processArrivalEvent(final SimulationData simData, final boolean scheduleNext, final int index) {
		/* "index" wird von diesem Source-Type nicht verwendet */
		final RunElementSourceData data=getData(simData);
		boolean isLastClient=false;

		if (record.testAdditionalArrivalCondition(simData,data.recordData)) {
			boolean batchArrivals=true;
			final int batchSize;
			if (data.recordData.batchSize!=null) {
				if (data.recordData.batchSize.isConstValue()) batchArrivals=(data.recordData.batchSize.getConstValue()!=1.0);
				batchSize=(int)Math.round(data.recordData.batchSize.calcOrDefault(simData.runData.variableValues,-1));
				if (batchSize<=0) {
					simData.doEmergencyShutDown(String.format(Language.tr("Simulation.Log.InvalidBatchSize"),name));
					return;
				}
			} else {
				batchSize=record.getMultiBatchSize(simData);
			}
			data.recordData.arrivalClientCount+=batchSize;

			if (batchArrivals) {
				/* Zwischenankunftszeiten auf Batch-Basis in der Statistik erfassen */
				simData.runData.logStationBatchArrival(simData.currentTime,simData,this,data);
			}

			for (int i=1;i<=batchSize;i++) {
				/* Evtl. WarmUp-Zeit beenden */
				if (simData.runData.isWarmUp) {
					/* Warm-Up-Phasenl�nge wird nicht durch Threadanzahl geteilt, sondern auf jedem Kern wird die angegebene Anzahl simuliert */
					if (simData.runData.clientsArrived>=warmUpClients && simData.runModel.warmUpTime>0) { /* runModel.warmUpTime>0 bedeutet, dass die Beendigung der Einschwingphase nach Zeit nur dann erfolgt, wenn diese in diesem Modus �berhaupt aktiv ist. */
						simData.endWarmUp();
						/* Logging */
						if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.WarmUpEnd"),Language.tr("Simulation.Log.WarmUpEnd.Info"));
					}
				}

				/* Kunde anlegen */
				final RunDataClient newClient=simData.runData.clients.getClient(record.clientType,simData,id);

				/* Zahlen und Strings zuweisen */
				data.recordData.setData.writeNumbersToClient(simData,newClient,name);
				record.writeStringsToClient(newClient);

				/* Notify-System �ber Kundenankunft informieren */
				newClient.nextStationID=id;
				simData.runData.fireClientMoveNotify(simData,newClient,false);

				/* Z�hler erh�hen, um festzustellen, wann die Simulation beendet werden kann */
				simData.runData.clientsArrived++;

				/* Logging */
				if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.SourceArrival"),String.format(Language.tr("Simulation.Log.SourceArrival.Info"),newClient.logInfo(simData),simData.runData.getWarmUpStatus(),name,simData.runData.clientsArrived));

				/* Zwischenankunftszeiten in der Statistik erfassen */
				simData.runData.logStationArrival(simData.currentTime,simData,this,data,newClient);

				/* Ggf. Kunde in Untermodell eintragen */
				if (parentId>=0) simData.runData.logClientEntersStation(simData,simData.runModel.elementsFast[parentId],null,newClient);

				/* Wenn Ziel-Anzahl an Ank�nften erreicht: Kunden Marker mitgeben, dass bei seiner Ankunft im Ziel die Simulation endet.*/
				if (simData.runData.nextClientIsLast(simData)) isLastClient=true;
				newClient.isLastClient=isLastClient;

				/* Tatsache, dass Simulation endet, ggf. loggen */
				if (isLastClient && simData.loggingActive) log(simData,Language.tr("Simulation.Log.EndOfSimulation"),Language.tr("Simulation.Log.EndOfSimulation.LastClientGenerated"));

				/* Kunden zur ersten (echten) Station leiten */
				StationLeaveEvent.sendToStation(simData,newClient,this,connection);
			}
		} else {
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.SourceArrival"),String.format(Language.tr("Simulation.Log.ArrivalSuppressed"),name));
		}

		if (scheduleNext) {
			if (isLastClient && !simData.runData.isWarmUp && data.maxSystemArrival<=0) data.maxSystemArrival=FastMath.max(1000,FastMath.max(2*simData.runData.clientsArrived,4*simData.runModel.clientCount/simData.runModel.clientCountDiv));
			/* Ankunft des n�chsten Kunden einplanen */
			if (simData.runData.isWarmUp || simData.runModel.clientCount<0 || data.maxSystemArrival<=0 || simData.runData.clientsArrived<data.maxSystemArrival) {
				boolean done=false;
				if (record.maxArrivalCount>=0 && data.recordData.arrivalCount>=record.maxArrivalCount) {
					done=true;
					if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.ArrivalCount"),String.format(Language.tr("Simulation.Log.ArrivalCount.Info"),name,record.maxArrivalCount));
				}
				if (record.maxArrivalClientCount>=0 && data.recordData.arrivalClientCount>=record.maxArrivalClientCount) {
					done=true;
					if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.ArrivalClientCount"),String.format(Language.tr("Simulation.Log.ArrivalClientCount.Info"),name,record.maxArrivalCount));
				}
				if (!done) {
					if (record.condition==null) {
						/* Einplanung der n�chsten Ankunft */
						scheduleNextArrival(simData,false);
					} else {
						/* Pr�fung der Bedingung zu passender Zeit ausl�sen */
						SystemChangeEvent.triggerEvent(simData,data.recordData.getConditionMinDistanceMS(simData,name));
					}
				}
			} else {
				simData.doEmergencyShutDown(Language.tr("Simulation.Log.EndOfSimulation.LastClientStillInSystem"));
			}
		}

		/* System �ber Status-�nderung benachrichtigen */
		simData.runData.fireStateChangeNotify(simData);

		/* Maximalzahl an Kunden im System eingehalten */
		if (!simData.testMaxAllowedClientsInSystem()) return;
	}

	@Override
	public boolean isClientCountStation() {
		return false;
	}

	/**
	 * Wurde bereits gepr�ft, ob die Bedingung
	 * einen deterministischen Wert liefert?
	 * @see #systemStateChangeNotify(SimulationData)
	 */
	private boolean fixedResultConditionTested=false;

	/**
	 * Liefert die Bedingung {@link RunElementSourceRecordData#condition}
	 * deterministisch immer <code>false</code>?
	 * @see #systemStateChangeNotify(SimulationData)
	 */
	private boolean isConstFalse=false;

	@Override
	public boolean systemStateChangeNotify(SimulationData simData) {
		if (record.condition==null && record.thresholdExpression==null) {
			/* Ausklinken */
			simData.runData.removeStateChangeListener(this);
			return false;
		}

		boolean arrivalsScheduled=false;

		if (record.condition!=null) {
			/* Pr�fen, ob die Bedingung immer "falsch" liefert */
			if (!fixedResultConditionTested) {
				fixedResultConditionTested=true;
				final RunElementSourceData data=getData(simData);
				if (data.recordData.condition!=null) isConstFalse=(data.recordData.condition.isConstFalse());
			}
			/* Ausklinken, wenn immer falsch (ggf. mehrfach ausf�hren) */
			if (isConstFalse) {
				/* Ausklinken */
				simData.runData.removeStateChangeListener(this);
				return false;
			}

			if (scheduleNextArrival(simData,false)) arrivalsScheduled=true;
		}

		if (record.thresholdExpression!=null) {
			final RunElementSourceData data=getData(simData);
			if (data.recordData.checkThreshold(simData,data)) {
				final int count=record.triggertByThreshold(simData,this,data.recordData.arrivalCount,data.recordData.arrivalClientCount);
				if (count>0) {
					data.recordData.arrivalCount+=count;
					data.recordData.arrivalTime=simData.currentTime;
					arrivalsScheduled=true;
				}
			}
		}

		return arrivalsScheduled;
	}

	@Override
	public void signalNotify(SimulationData simData, String signalName) {
		final RunElementSourceData data=getData(simData);
		final int count=record.triggeredBySignal(simData,signalName,this,data.recordData.arrivalCount,data.recordData.arrivalClientCount);
		if (count>0) {
			data.recordData.arrivalCount+=count;
			data.recordData.arrivalTime=simData.currentTime;
		}
	}

	@Override
	public RunElement getNext() {
		return connection;
	}

	@Override
	public void endWarmUpNotify(final SimulationData simData) {
		final RunElementSourceData data=getData(simData);
		simData.runData.clientsArrived=0;
		data.recordData.arrivalCount=0;
		data.recordData.arrivalClientCount=0;
	}
}

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
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElement;
import simulator.editmodel.EditModel;
import simulator.events.StationLeaveEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunDataTransporter;
import simulator.runmodel.RunDataTransporters;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementProcess;
import ui.modeleditor.elements.ModelElementSectionStart;
import ui.modeleditor.elements.ModelElementSub;
import ui.modeleditor.elements.ModelElementTransportTransporterSource;

/**
 * Äquivalent zu <code>ModelElementTransportTransporterSource</code>
 * @author Alexander Herzog
 * @see ModelElementTransportTransporterSource
 */
public class RunElementTransportTransporterSource extends RunElement implements TransporterPosition {
	private final RunElementTransportSourceTargets transportTargets;

	private int transporterIndex;
	private int waitingCapacity;
	private String priorityWaitingString;
	private int requestingMinNumber;
	private String priorityRequestingString;
	private String[] priorityClientString;
	private int transporterClientCapacity;

	private RunElementSectionStart section;
	private int sectionID;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementTransportTransporterSource(final ModelElementTransportTransporterSource element) {
		super(element,buildName(element,Language.tr("Simulation.Element.TransportTransporterSource.Name")));
		transportTargets=new RunElementTransportSourceTargets(element);
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {

		if (!(element instanceof ModelElementTransportTransporterSource)) return null;

		final ModelElementTransportTransporterSource sourceElement=(ModelElementTransportTransporterSource)element;
		final RunElementTransportTransporterSource source=new RunElementTransportTransporterSource(sourceElement);

		String error;
		int err;

		/* Transportziele */
		error=source.transportTargets.build(editModel,runModel,sourceElement.getTransportTargetSystem());
		if (error!=null) return error;

		/* Transportertyp */
		source.transporterIndex=runModel.transportersTemplate.getTransporterIndex(sourceElement.getTransporterType());
		if (source.transporterIndex<0) return String.format(Language.tr("Simulation.Creator.UnknownTransporterName"),element.getId(),sourceElement.getTransporterType());

		/* Kapazität */
		source.waitingCapacity=sourceElement.getWaitingCapacity();
		if (source.waitingCapacity<0) return String.format(Language.tr("Simulation.Creator.InvalidParkingCapacity"),element.getId(),source.waitingCapacity);

		/* Parkplatz-Priorität */
		source.priorityWaitingString=sourceElement.getWaitingPriority();
		err=ExpressionCalc.check(source.priorityWaitingString,runModel.variableNames);
		if (err>=0) return String.format(Language.tr("Simulation.Creator.InvalidParkingPriority"),element.getId(),source.priorityWaitingString,err+1);

		/* Mindestanzahl an wartenden Kunden für Transporteranforderung */
		source.requestingMinNumber=sourceElement.getRequestMinWaiting();
		if (source.requestingMinNumber<1) return String.format(Language.tr("Simulation.Creator.InvalidMinRequestNumber"),element.getId(),source.requestingMinNumber);

		/* Anforderungs-Priorität */
		source.priorityRequestingString=sourceElement.getRequestPriority();
		err=ExpressionCalc.check(source.priorityRequestingString,runModel.variableNames);
		if (err>=0) return String.format(Language.tr("Simulation.Creator.InvalidRequestPriority"),element.getId(),source.priorityRequestingString,err+1);

		/* Prioritäten der Kundentypen */
		source.priorityClientString=new String[runModel.clientTypes.length];
		for (int i=0;i<source.priorityClientString.length;i++) {
			String priorityString=sourceElement.getClientPriority(runModel.clientTypes[i]);
			if (priorityString==null || priorityString.trim().isEmpty()) priorityString=ModelElementTransportTransporterSource.DEFAULT_CLIENT_PRIORITY;
			final ExpressionCalc calc=new ExpressionCalc(runModel.variableNames);
			err=calc.parse(priorityString);
			if (err>=0) return String.format(Language.tr("Simulation.Creator.TransporterSourceClientPriority"),element.getId(),runModel.clientTypes[i],priorityString,err+1);
			if (priorityString.equalsIgnoreCase(ModelElementProcess.DEFAULT_CLIENT_PRIORITY)) {
				source.priorityClientString[i]=null; /* Default Priorität als null vermerken */
			} else {
				source.priorityClientString[i]=priorityString;
			}
		}

		/* Wie viele Kunden kann ein Transporter des gewählten Typs transportieren? */
		source.transporterClientCapacity=runModel.transportersTemplate.getTransporterCapacity(source.transporterIndex);

		/* Bereich */
		final String sectionName=sourceElement.getSectionStartName();
		if (!sectionName.isEmpty()) {
			source.sectionID=-1;
			for (ModelElement e1 : editModel.surface.getElements()) {
				if (e1 instanceof ModelElementSectionStart && e1.getName().equals(sectionName)) {source.sectionID=e1.getId(); break;}
				if (e1 instanceof ModelElementSub) {
					for (ModelElement e2 : editModel.surface.getElements()) {
						if (e2 instanceof ModelElementSectionStart && e2.getName().equals(sectionName)) {source.sectionID=e2.getId(); break;}
					}
					if (source.sectionID>=0) break;
				}
			}
			if (source.sectionID<0) return String.format(Language.tr("Simulation.Creator.InvalidSection"),element.getId(),sectionName);
		}

		return source;
	}

	@Override
	public RunModelCreatorStatus test(ModelElement element) {
		if (!(element instanceof ModelElementTransportTransporterSource)) return null;
		final ModelElementTransportTransporterSource sourceElement=(ModelElementTransportTransporterSource)element;

		/* Transportziele */
		final RunModelCreatorStatus error=RunElementTransportSourceTargets.test(element,sourceElement.getTransportTargetSystem());
		if (error!=null) return error;

		/* Kapazität */
		final int capacity=sourceElement.getWaitingCapacity();
		if (capacity<0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.InvalidParkingCapacity"),element.getId(),capacity),RunModelCreatorStatus.Status.TRANSPORTER_SOURCE_NEGATIVE_PARKING_CAPACITY);

		/* Mindestanzahl an wartenden Kunden für Transporteranforderung */
		final int requestingMinNumber=sourceElement.getRequestMinWaiting();
		if (requestingMinNumber<1) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.InvalidMinRequestNumber"),element.getId(),requestingMinNumber),RunModelCreatorStatus.Status.TRANSPORTER_SOURCE_MIN_REQUEST_NUMBER_LESS_OR_EQUAL_0);

		return RunModelCreatorStatus.ok;
	}

	@Override
	public RunElementTransportTransporterSourceData getData(final SimulationData simData) {
		RunElementTransportTransporterSourceData data;
		data=(RunElementTransportTransporterSourceData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementTransportTransporterSourceData(this,priorityWaitingString,priorityRequestingString,priorityClientString,transportTargets.routingExpression,simData.runModel.variableNames);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	/**
	 * Liefert den Index der Transportergruppe, die diese Haltestelle ansteuert.
	 * @return	Index der Transportergruppe, die diese Haltestelle ansteuert
	 * @see RunDataTransporters#getTransporterIndex(String)
	 */
	public int getTransporterIndex() {
		return transporterIndex;
	}

	private void getClientsToMove(final SimulationData simData, final List<RunDataClient> moveList) {
		moveList.clear();
		final RunElementTransportTransporterSourceData data=getData(simData);

		if (data.priorityClientAllFirstComeFirstServe) {
			/* Alle Typen nach Ankunftsreihenfolge */

			/* Ersten Kunden in Liste aufnehmen */
			RunDataClient client=data.removeClientFromQueue(0,simData);
			moveList.add(client);
			final int nextStationID=transportTargets.getDestinationStation(simData,moveList.get(0),data.routingExpresions,true);

			/* Weitere Kunden mit demselben Ziel */
			int nextTest=0;
			while (moveList.size()<transporterClientCapacity) {
				if (nextTest>=data.queue.size()) break;
				final int next=transportTargets.getDestinationStation(simData,moveList.get(nextTest),data.routingExpresions,true);
				if (next==nextStationID) {
					/* Kunde hat dasselbe Ziel */
					moveList.add(data.removeClientFromQueue(nextTest,simData));
				} else {
					/* Kunde hat ein anderes Ziel und kann leider nicht mitgenommen werden */
					nextTest++;
				}
			}
		} else {
			final int count=data.queue.size();

			if (data.cacheListClient==null || data.cacheListClient.length<count) data.cacheListClient=new RunDataClient[count];
			final RunDataClient[] client=data.cacheListClient;
			if (data.cacheListScore==null || data.cacheListScore.length<count) data.cacheListScore=new double[count];
			final double[] score=data.cacheListScore;
			if (data.cacheListTarget==null || data.cacheListTarget.length<count) data.cacheListTarget=new int[count];
			final int[] target=data.cacheListTarget;

			for (int i=0;i<count;i++) {
				final RunDataClient current=data.queue.get(i);

				/* Kunde */
				client[i]=current;

				/* Priorität */
				final ExpressionCalc calc=data.priorityClient[current.type];
				if (calc==null) { /* = Text war "w", siehe RunElementTransportTransporterSourceData()  */
					final double waitingTime=(((double)simData.currentTime)-current.lastWaitingStart)/1000.0;
					score[i]=waitingTime;
				} else {
					simData.runData.setClientVariableValues(simData.currentTime-current.lastWaitingStart,current.transferTime,current.processTime);
					if (simData.runModel.stoppOnCalcError) {
						final Double D=calc.calc(simData.runData.variableValues,simData,current);
						if (D==null) simData.calculationErrorStation(calc,this);
						score[i]=(D==null)?0.0:D.doubleValue();
					} else {
						score[i]=calc.calcOrDefault(simData.runData.variableValues,simData,current,0);
					}
				}

				/* Ziel */
				target[i]=transportTargets.getDestinationStation(simData,current,data.routingExpresions,true);
			}

			/* Kunden in Liste aufnehmen */
			int moveTarget=-1;
			while (moveList.size()<transporterClientCapacity) {
				int bestIndex=-1;
				double bestPriority=-Double.MAX_VALUE;
				for (int i=0;i<count;i++) {
					if (client[i]!=null && score[i]>bestPriority && (moveTarget<0 || target[i]==moveTarget)) {
						bestPriority=score[i];
						bestIndex=i;
					}
				}
				if (bestIndex<0) break;

				data.removeClientFromQueue(client[bestIndex],simData);
				moveList.add(client[bestIndex]);
				client[bestIndex]=null;
			}
		}
	}

	private void processChange(final SimulationData simData) {
		final RunElementTransportTransporterSourceData data=getData(simData);

		/* Schritt 1: Transporte wenn möglich starten */
		while (data.queue.size()>=requestingMinNumber && data.count>0) {
			/* Es sind genug Kunden da, um einen Transport zu starten */

			/* Passenden freien Transporter wählen */
			final RunDataTransporter transporter=simData.runData.transporters.getWaitingTransporter(transporterIndex,id);

			/* Kunden auswählen */
			getClientsToMove(simData,transporter.clients);
			if (transporter.clients.size()==0) break;

			/* Zielstation bestimmen */
			for (int i=0;i<transporter.clients.size();i++) {
				final RunDataClient client=transporter.clients.get(i);
				/* Auch wenn alle dasselbe Ziel haben, muss getDestinationStation doch für jeden Kunden aufgerufen werden, um ggf. den Fertigungsplan-Zähler zu erhöhen usw. */
				client.stationInformationInt=transportTargets.getDestinationStation(simData,client,data.routingExpresions,false);
			}
			final int destinationStation=transporter.clients.get(0).stationInformationInt;

			/* Transporter losschicken */
			final long transferTimeMS=transporter.moveTo(destinationStation,transporter.clients.size(),simData);

			if (section==null && sectionID>=0) section=(RunElementSectionStart)simData.runModel.elementsFast[sectionID];
			for (int i=0;i<transporter.clients.size();i++) {
				final RunDataClient client=transporter.clients.get(i);

				/* Kunde aus Bereich austragen */
				if (section!=null) client.leaveSection(section,simData);

				/* Kunde aus Station austragen */
				final long delayTimeMS=simData.currentTime-client.lastWaitingStart;
				client.waitingTime+=delayTimeMS;
				client.transferTime+=transferTimeMS;
				client.residenceTime+=delayTimeMS+transferTimeMS;
				simData.runData.logStationProcess(simData,this,client,delayTimeMS,transferTimeMS,0,delayTimeMS+transferTimeMS);
				StationLeaveEvent.addLeaveEvent(simData,client,this,0);
			}

			/* Loggen */
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.TransporterStart"),String.format(Language.tr("Simulation.Log.TransporterStart.Start"),name,"\""+simData.runData.transporters.type[transporterIndex]+"\"("+transporter.hashCode()+")",transporter.clients.size()));
		}

		/* Schritt 2: Transporter anfordern wenn nötig */
		if (data.queue.size()>=requestingMinNumber) {
			while ((data.count+data.moving)/transporterClientCapacity<data.queue.size()) {
				final RunDataTransporter transporter=simData.runData.transporters.getWaitingTransporterAtOtherStation(transporterIndex,id,getRequestPriorityInt(simData),simData);
				if (transporter==null) break;
				transporter.moveTo(id,0,simData);
			}
		}

		simData.runData.fireStateChangeNotify(simData);
	}

	@Override
	public void processArrival(SimulationData simData, RunDataClient client) {
		/* Erfassung: Kunde ist da */
		final RunElementTransportTransporterSourceData data=getData(simData);
		data.addClientToQueue(client,simData);

		/* Verarbeitung */
		processChange(simData);
	}

	private void countSub(final int subId, final int delta, final SimulationData simData) {
		if (subId<0) return;

		final RunElement parent=simData.runModel.elementsFast[subId];
		if (!(parent instanceof RunElementSub)) return;
		final RunElementSub sub=(RunElementSub)parent;

		simData.runData.clientsAtStation(simData,sub,null,delta);
	}

	private void fixSubModelCount(final int lastID, final int nextID, final SimulationData simData) {
		final RunElement lastStation=simData.runModel.elementsFast[lastID];
		final RunElement nextStation=simData.runModel.elementsFast[nextID];

		if (lastStation==null || nextStation==null) return;
		if (lastStation.parentId==nextStation.parentId) return;

		countSub(lastStation.parentId,-1,simData);
		countSub(nextStation.parentId,1,simData);
	}

	@Override
	public void processLeave(SimulationData simData, RunDataClient client) {
		/* Wenn Kunde in oder aus Submodell bewegt wurde, muss die Anzahl an Kunden im Submodell angepasst werden. */
		client.lastStationID=id;
		fixSubModelCount(id,client.stationInformationInt,simData);
	}

	@Override
	public void transporterArrival(RunDataTransporter transporter, SimulationData simData) {
		/* Erfassung: Transporter ist da */
		final RunElementTransportTransporterSourceData data=getData(simData);
		data.moving--;
		data.count++;

		/* Logging */
		if (simData.loggingActive) {
			if (data.count==1) {
				log(simData,Language.tr("Simulation.Log.TransporterStart"),String.format(Language.tr("Simulation.Log.TransporterStart.Info.Singular"),name));
			} else {
				log(simData,Language.tr("Simulation.Log.TransporterStart"),String.format(Language.tr("Simulation.Log.TransporterStart.Info"),name,data.count));
			}
		}

		/* Verarbeitung */
		processChange(simData);
	}

	@Override
	public void transporterLeave(RunDataTransporter transporter, SimulationData simData) {
		/* Erfassung: Transporter ist weg */
		final RunElementTransportTransporterSourceData data=getData(simData);
		data.count--;

		/* Logging */
		if (simData.loggingActive) {
			if (data.count==1) {
				log(simData,Language.tr("Simulation.Log.TransporterStart"),String.format(Language.tr("Simulation.Log.TransporterStart.Info.Singular"),name));
			} else {
				log(simData,Language.tr("Simulation.Log.TransporterStart"),String.format(Language.tr("Simulation.Log.TransporterStart.Info"),name,data.count));
			}
		}

		/* Freie Transporter ggf. auf Warteplätze holen */
		if (data.count+data.moving<waitingCapacity) {
			final RunDataTransporter newTransporter=simData.runData.transporters.getWaitingTransporterAtOtherStation(transporterIndex,id,getRequestPriorityInt(simData),simData);
			if (newTransporter!=null) {
				/* Logging */
				if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.TransporterStart"),String.format(Language.tr("Simulation.Log.Parking.Requesting"),name,newTransporter.position));

				/* Transporter anfordern */
				newTransporter.moveTo(id,0,simData);
			}
		}
	}

	@Override
	public void transporterFree(RunDataTransporter transporter, SimulationData simData) {
		if (transporter.type!=transporterIndex) return; /* Wollen wir nicht. */
		if (transporter.position==id) return; /* Haben wir schon. */

		final RunElementTransportTransporterSourceData data=getData(simData);
		if (data.count+data.moving>=waitingCapacity) return; /* Kein Platz mehr. */

		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.TransporterStart"),String.format(Language.tr("Simulation.Log.Parking.Requesting"),name,transporter.position));

		/* Transporter anfordern */
		transporter.moveTo(id,0,simData);
	}

	private double getRequestPriorityInt(final SimulationData simData) {
		final RunElementTransportTransporterSourceData data=getData(simData);

		if (data.queue.size()>=requestingMinNumber) {
			if (simData.runModel.stoppOnCalcError) {
				final Double D=data.priorityRequest.calc(simData.runData.variableValues,simData,null);
				if (D==null) simData.calculationErrorStation(data.priorityRequest,this);
				return (D==null)?0.0:D.doubleValue();
			} else {
				return data.priorityRequest.calcOrDefault(simData.runData.variableValues,simData,null,0);
			}
		} else {
			if (simData.runModel.stoppOnCalcError) {
				final Double D=data.priorityParking.calc(simData.runData.variableValues,simData,null);
				if (D==null) simData.calculationErrorStation(data.priorityParking,this);
				return (D==null)?0.0:D.doubleValue();
			} else {
				return data.priorityParking.calcOrDefault(simData.runData.variableValues,simData,null,0);
			}
		}
	}

	@Override
	public Double requestPriority(RunDataTransporter transporter, SimulationData simData) {
		if (transporter.type!=transporterIndex) return null; /* Wollen wir nicht. */
		if (transporter.position==id) return null; /* Haben wir schon. */

		final RunElementTransportTransporterSourceData data=getData(simData);
		if (data.count+data.moving>=waitingCapacity) return null; /* Kein Platz mehr. */

		if (data.queue.size()>=requestingMinNumber) {
			final Double D=data.priorityRequest.calc(simData.runData.variableValues,simData,null);
			if (D==null) simData.calculationErrorStation(data.priorityRequest,this);
			return D;
		} else {
			final Double D=data.priorityParking.calc(simData.runData.variableValues,simData,null);
			if (D==null) simData.calculationErrorStation(data.priorityParking,this);
			return D;
		}
	}

	@Override
	public Double stayHerePriority(RunDataTransporter transporter, SimulationData simData) {
		if (transporter.type!=transporterIndex) return null; /* Wollen wir nicht. */

		final RunElementTransportTransporterSourceData data=getData(simData);
		if (data.count+data.moving>waitingCapacity) return null; /* Kein Platz mehr, wir wollen den überzähligen Transporter gerne loswerden. */

		final Double D=data.priorityParking.calc(simData.runData.variableValues,simData,null);
		if (D==null) simData.calculationErrorStation(data.priorityParking,this);
		return D;
	}

	@Override
	public void transporterStartsMoving(RunDataTransporter transporter, SimulationData simData) {
		final RunElementTransportTransporterSourceData data=getData(simData);
		data.moving++;
	}
}
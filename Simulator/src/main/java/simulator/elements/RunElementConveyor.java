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
import mathtools.NumberTools;
import mathtools.TimeTools;
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElementPassThrough;
import simulator.editmodel.EditModel;
import simulator.events.StationLeaveEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementConveyor;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu {@link ModelElementConveyor}
 * @author Alexander Herzog
 * @see ModelElementConveyor
 */
public class RunElementConveyor extends RunElementPassThrough implements PickUpQueue {
	/**
	 * Auf dem Fließband verfügbare Kapazität
	 */
	private double capacityAvailable;

	/**
	 * Formel zur Bestimmung des Platzbedarfes pro Kundentyp
	 *  {@link RunElementConveyorData#capacityNeeded}
	 */
	private String[] capacityNeeded;

	/**
	 * Zeit in MS, die notwendig ist, um einen Kunden von der einen zur anderen Seite des Fließbandes zu befördern
	 */
	private long transportTimeMS;

	/**
	 * Statistiktyp als was die Transportzeit erfasst werden soll
	 */
	private ModelElementConveyor.TransportTimeType transportTimeType;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementConveyor(final ModelElementConveyor element) {
		super(element,buildName(element,Language.tr("Simulation.Element.Conveyor.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementConveyor)) return null;
		final ModelElementConveyor conveyorElement=(ModelElementConveyor)element;
		final RunElementConveyor conveyor=new RunElementConveyor(conveyorElement);

		/* Auslaufende Kante */
		final String edgeError=conveyor.buildEdgeOut(conveyorElement);
		if (edgeError!=null) return edgeError;

		/* Verfügbare Kapazität */
		if (conveyorElement.getCapacityAvailable()<=0) return String.format(Language.tr("Simulation.Creator.InvalidCapacityAvailable"),element.getId());
		conveyor.capacityAvailable=conveyorElement.getCapacityAvailable();

		/* Benötigte Kapazität */
		conveyor.capacityNeeded=new String[runModel.clientTypes.length];
		for (int i=0;i<runModel.clientTypes.length;i++) {
			String needed=conveyorElement.getCapacityNeeded(runModel.clientTypes[i]);
			if (needed==null) needed=conveyorElement.getCapacityNeededGlobal();
			final ExpressionCalc calc=new ExpressionCalc(runModel.variableNames);
			final int error=calc.parse(needed);
			if (error>=0) return String.format(Language.tr("Simulation.Creator.InvalidNeededCapacityExpression"),element.getId(),runModel.clientTypes[i],needed,error+1);
			conveyor.capacityNeeded[i]=needed;
		}

		/* Transportzeit */
		if (conveyorElement.getTransportTime()<0) return String.format(Language.tr("Simulation.Creator.InvalidTransportTime"),element.getId());
		double time=conveyorElement.getTransportTime();
		final long timeMS;
		switch (conveyorElement.getTimeBase()) {
		case TIMEBASE_HOURS: timeMS=Math.round(time*3600*1000); break;
		case TIMEBASE_MINUTES: timeMS=Math.round(time*60*1000); break;
		case TIMEBASE_SECONDS: timeMS=Math.round(time*1000); break;
		default: timeMS=Math.round(time*1000); break;
		}
		conveyor.transportTimeMS=timeMS;
		conveyor.transportTimeType=conveyorElement.getTransportTimeType();

		return conveyor;
	}

	@Override
	public RunModelCreatorStatus test(ModelElement element) {
		if (!(element instanceof ModelElementConveyor)) return null;
		final ModelElementConveyor conveyorElement=(ModelElementConveyor)element;

		/* Auslaufende Kante */
		final RunModelCreatorStatus edgeError=testEdgeOut(conveyorElement);
		if (edgeError!=null) return edgeError;

		/* Verfügbare Kapazität */
		if (conveyorElement.getCapacityAvailable()<=0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.InvalidCapacityAvailable"),element.getId()),RunModelCreatorStatus.Status.CONVEYOR_CAPACITY_NEGATIVE);

		/* Transportzeit */
		if (conveyorElement.getTransportTime()<0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.InvalidTransportTime"),element.getId()),RunModelCreatorStatus.Status.CONVEYOR_TIME_NEGATIVE);

		return RunModelCreatorStatus.ok;
	}

	@Override
	public RunElementConveyorData getData(final SimulationData simData) {
		RunElementConveyorData data;
		data=(RunElementConveyorData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementConveyorData(this,capacityNeeded,simData.runModel.variableNames,capacityAvailable,transportTimeMS);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	/**
	 * Prüft, ob die Bedienung von wartenden Kunden gestartet werden kann.
	 * @param simData	Simulationsdatenobjekt
	 */
	private void tryStartProcessing(final SimulationData simData) {
		final RunElementConveyorData data=getData(simData);
		if (data.waitingClients.size()==0) return;

		final RunDataClient client=data.waitingClients.get(0);
		if (data.clientsAtStation-data.clientsAtStationQueue==0 || client.stationInformationDouble<data.freeCapacity) {

			/* Kunden aus der Warteschlange entfernen */
			final long waitingTimeMS=data.removeClientFromQueue(client,0,simData.currentTime,simData);

			/* Startzeit für Animation erfassen */
			client.stationInformationLong=simData.currentTime;

			/* Kapazität anpassen */
			data.freeCapacity-=client.stationInformationDouble;

			/* Logging */
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.ConveyorMoveStart"),String.format(Language.tr("Simulation.Log.ConveyorMoveStart.Info"),client.logInfo(simData),name,TimeTools.formatTime(transportTimeMS/1000),NumberTools.formatNumber(client.stationInformationDouble),NumberTools.formatNumber(data.freeCapacity)));

			/* Bedienzeit in Statistik */
			final long residenceTimeMS=waitingTimeMS+transportTimeMS;
			switch (transportTimeType) {
			case TRANSPORT_TYPE_WAITING: simData.runData.logStationProcess(simData,this,client,residenceTimeMS,0,0,residenceTimeMS); break;
			case TRANSPORT_TYPE_TRANSFER: simData.runData.logStationProcess(simData,this,client,waitingTimeMS,transportTimeMS,0,residenceTimeMS); break;
			case TRANSPORT_TYPE_PROCESS: simData.runData.logStationProcess(simData,this,client,waitingTimeMS,0,transportTimeMS,residenceTimeMS); break;
			}
			client.waitingTime+=waitingTimeMS;
			switch (transportTimeType) {
			case TRANSPORT_TYPE_WAITING: client.waitingTime+=transportTimeMS; break;
			case TRANSPORT_TYPE_TRANSFER: client.transferTime+=transportTimeMS; break;
			case TRANSPORT_TYPE_PROCESS: client.processTime+=transportTimeMS; break;
			}
			client.residenceTime+=(waitingTimeMS+transportTimeMS);

			/* Weiterleitung zu nächster Station nach Bedienzeit-Ende */
			StationLeaveEvent.addLeaveEvent(simData,client,this,transportTimeMS);
		}
	}

	@Override
	public void processArrival(SimulationData simData, RunDataClient client) {
		final RunElementConveyorData data=getData(simData);

		data.queueLockedForPickUp=true;
		try {
			data.addClientToQueue(client,simData.currentTime,simData);
			tryStartProcessing(simData);
		} finally {
			data.queueLockedForPickUp=false;
		}
	}

	@Override
	public void processLeave(SimulationData simData, RunDataClient client) {
		super.processLeave(simData,client);

		final RunElementConveyorData data=getData(simData);
		data.freeCapacity+=client.stationInformationDouble;

		data.queueLockedForPickUp=true;
		try {
			tryStartProcessing(simData);
		} finally {
			data.queueLockedForPickUp=false;
		}
	}

	@Override
	public RunDataClient getClient(SimulationData simData) {
		final RunElementConveyorData data=getData(simData);
		if (data.queueLockedForPickUp) return null;
		if (data.waitingClients.size()==0) return null;

		final RunDataClient client=data.waitingClients.get(0);

		long waitingTime=data.removeClientFromQueue(client,0,simData.currentTime,simData);
		/* Nein, da Kunde an der Station ja nicht bedient wurde: simData.runData.logStationProcess(simData,this,waitingTime,0,0); */
		client.waitingTime+=waitingTime;
		client.residenceTime+=waitingTime;

		return client;
	}

}
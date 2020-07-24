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
import simulator.runmodel.RunDataTransporter;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementSub;
import ui.modeleditor.elements.ModelElementTransportDestination;

/**
 * Äquivalent zu <code>ModelElementTransportDestination</code>
 * @author Alexander Herzog
 * @see ModelElementTransportDestination
 */
public class RunElementTransportDestination extends RunElementPassThrough implements TransporterPosition {
	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementTransportDestination(final ModelElementTransportDestination element) {
		super(element,buildName(element,Language.tr("Simulation.Element.TransportDestination.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementTransportDestination)) return null;
		final ModelElementTransportDestination destinationElement=(ModelElementTransportDestination)element;
		final RunElementTransportDestination destination=new RunElementTransportDestination(destinationElement);

		/* Auslaufende Kante */
		final String edgeError=destination.buildEdgeOut(destinationElement);
		if (edgeError!=null) return edgeError;

		return destination;
	}

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementTransportDestination)) return null;
		final ModelElementTransportDestination destinationElement=(ModelElementTransportDestination)element;

		/* Auslaufende Kante */
		final RunModelCreatorStatus edgeError=testEdgeOut(destinationElement);
		if (edgeError!=null) return edgeError;

		return RunModelCreatorStatus.ok;
	}

	private void countSub(final int subId, final RunDataClient client, final int delta, final SimulationData simData) {
		if (subId<0) return;

		final RunElement parent=simData.runModel.elementsFast[subId];
		if (!(parent instanceof RunElementSub)) return;
		final RunElementSub sub=(RunElementSub)parent;

		simData.runData.clientsAtStation(simData,sub,null,delta);
		simData.runData.clientsAtStationByType(simData,sub,null,client,delta);
	}

	private void fixSubModelCount(final int lastID, final int nextID, final RunDataClient client,final SimulationData simData) {
		final RunElement lastStation=(lastID>=0)?simData.runModel.elementsFast[lastID]:null;
		final RunElement nextStation=(nextID>=0)?simData.runModel.elementsFast[nextID]:null;

		if (lastStation==null || nextStation==null) return;
		if (lastStation.parentId==nextStation.parentId) return;

		countSub(lastStation.parentId,client,-1,simData);
		countSub(nextStation.parentId,client,1,simData);
	}

	@Override
	public void processArrival(final SimulationData simData, final RunDataClient client) {
		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.TransportDestination"),String.format(Language.tr("Simulation.Log.TransportDestination.Info"),client.logInfo(simData),name));

		/* Wenn Kunde in oder aus Submodell bewegt wurde, muss die Anzahl an Kunden im Submodell angepasst werden. */
		fixSubModelCount(client.lastStationID,id,client,simData);

		/* Kunde zur nächsten Station leiten */
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
	}

	@Override
	public void transporterArrival(final RunDataTransporter transporter, final SimulationData simData) {
		/* Kunden ausladen */
		for (int i=0;i<transporter.clients.size();i++) {
			final RunDataClient client=transporter.clients.get(i);
			StationLeaveEvent.sendToStationByTransporter(simData,client,(client.lastStationID>=0)?simData.runModel.elementsFast[client.lastStationID]:null,this);
		}
		transporter.clients.clear();
	}

	@Override
	public void transporterLeave(RunDataTransporter transporter, SimulationData simData) {
		/* Die Zielstation fordert nie Transporter an. Daher ist es ihr auch egal, wenn Transporter hier wegfahren. */
	}

	@Override
	public void transporterFree(RunDataTransporter transporter, SimulationData simData) {
		/* Die Zielstation fordert nie Transporter an. Daher ist es ihr auch egal, wenn Transporter verfügbar werden. */
	}

	@Override
	public Double requestPriority(RunDataTransporter transporter, SimulationData simData) {
		/* Die Zielstation fordert nie Transporter an. */
		return null;
	}

	@Override
	public Double stayHerePriority(RunDataTransporter transporter, SimulationData simData) {
		/* Die Zielstation hat kein Interesse, Transporter zu halten. */
		return null;
	}

	@Override
	public void transporterStartsMoving(RunDataTransporter transporter, SimulationData simData) {
		/* Dass ein Transporter unterwegs ist, interessiert die Zielstation nicht wirklich. */
	}
}

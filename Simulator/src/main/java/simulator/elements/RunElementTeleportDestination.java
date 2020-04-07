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
import ui.modeleditor.elements.ModelElementSub;
import ui.modeleditor.elements.ModelElementTeleportDestination;

/**
 * Äquivalent zu <code>ModelElementTeleportDestination</code>
 * @author Alexander Herzog
 * @see ModelElementTeleportDestination
 */
public class RunElementTeleportDestination extends RunElementPassThrough {
	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementTeleportDestination(final ModelElementTeleportDestination element) {
		super(element,buildName(element,Language.tr("Simulation.Element.TeleportDestination.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementTeleportDestination)) return null;
		final ModelElementTeleportDestination destinationElement=(ModelElementTeleportDestination)element;
		final RunElementTeleportDestination destination=new RunElementTeleportDestination(destinationElement);

		/* Auslaufende Kante */
		final String edgeError=destination.buildEdgeOut(destinationElement);
		if (edgeError!=null) return edgeError;

		return destination;
	}

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementTeleportDestination)) return null;
		final ModelElementTeleportDestination destinationElement=(ModelElementTeleportDestination)element;

		/* Auslaufende Kante */
		final RunModelCreatorStatus edgeError=testEdgeOut(destinationElement);
		if (edgeError!=null) return edgeError;

		return RunModelCreatorStatus.ok;
	}

	private void countSub(final int subId, final int delta, final SimulationData simData) {
		if (subId<0) return;

		final RunElement parent=simData.runModel.elementsFast[subId];
		if (!(parent instanceof RunElementSub)) return;
		final RunElementSub sub=(RunElementSub)parent;

		simData.runData.clientsAtStation(simData,sub,null,delta);
	}

	private void fixSubModelCount(final int lastID, final int nextID, final SimulationData simData) {
		final RunElement lastStation=(lastID>=0)?simData.runModel.elementsFast[lastID]:null;
		final RunElement nextStation=(nextID>=0)?simData.runModel.elementsFast[nextID]:null;

		if (lastStation==null || nextStation==null) return;
		if (lastStation.parentId==nextStation.parentId) return;

		countSub(lastStation.parentId,-1,simData);
		countSub(nextStation.parentId,1,simData);
	}

	@Override
	public void processArrival(final SimulationData simData, final RunDataClient client) {
		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.TeleportDestination"),String.format(Language.tr("Simulation.Log.TeleportDestination.Info"),client.logInfo(simData),name));

		/* Wenn Kunde in oder aus Submodell bewegt wurde, muss die Anzahl an Kunden im Submodell angepasst werden. */
		fixSubModelCount(client.lastStationID,id,simData);

		/* Kunde zur nächsten Station leiten */
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
	}
}

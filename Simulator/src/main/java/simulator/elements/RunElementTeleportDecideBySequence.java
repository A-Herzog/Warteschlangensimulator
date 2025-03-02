/**
 * Copyright 2022 Alexander Herzog
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
import java.util.stream.IntStream;

import language.Language;
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElement;
import simulator.editmodel.EditModel;
import simulator.events.StationLeaveEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementDecide;
import ui.modeleditor.elements.ModelElementDecideAndTeleport;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu {@link ModelElementDecideAndTeleport} (für den Modus "Reihenfolge")
 * @author Alexander Herzog
 * @see ModelElementDecideAndTeleport
 */
public class RunElementTeleportDecideBySequence extends RunElement {
	/** Namen der Teleport-Zielstationen */
	private String[] destinationStrings;
	/** IDs der Teleport-Zielstationen */
	private int[] destinationIDs;
	/** Teleport-Zielstationen (übersetzt aus {@link #destinationIDs}) */
	private RunElement[] destinations;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementTeleportDecideBySequence(final ModelElementDecideAndTeleport element) {
		super(element,buildName(element,Language.tr("Simulation.Element.TeleportDecideBySequence.Name")));
	}

	@Override
	public Object build(EditModel editModel, RunModel runModel, ModelElement element, ModelElementSub parent, boolean testOnly) {
		if (!(element instanceof ModelElementDecideAndTeleport)) return null;
		final ModelElementDecideAndTeleport decideElement=(ModelElementDecideAndTeleport)element;
		if (decideElement.getMode()!=ModelElementDecide.DecideMode.MODE_SEQUENCE) return null;
		final RunElementTeleportDecideBySequence decide=new RunElementTeleportDecideBySequence((ModelElementDecideAndTeleport)element);

		decide.destinationStrings=decideElement.getDestinations().toArray(String[]::new);
		final List<Integer> destinationIDs=new ArrayList<>();
		if (decide.destinationStrings.length==0) return String.format(Language.tr("Simulation.Creator.NoTeleportDestination"),element.getId());

		final List<Integer> edgesMultiplicity=decideElement.getMultiplicity();

		for (int i=0;i<decide.destinationStrings.length;i++) {
			final String destination=decide.destinationStrings[i];
			final int mul=(i>=edgesMultiplicity.size())?1:edgesMultiplicity.get(i).intValue();

			final int destinationID=RunElementTeleportSource.getDestinationID(element.getModel(),destination);
			if (destinationID<0) return String.format(Language.tr("Simulation.Creator.InvalidTeleportDestination"),element.getId(),destination);
			for (int j=0;j<mul;j++) destinationIDs.add(destinationID);
		}
		decide.destinationIDs=destinationIDs.stream().mapToInt(Integer::intValue).toArray();

		return decide;
	}

	@Override
	public RunModelCreatorStatus test(ModelElement element) {
		if (!(element instanceof ModelElementDecideAndTeleport)) return null;
		final ModelElementDecideAndTeleport decideElement=(ModelElementDecideAndTeleport)element;
		if (decideElement.getMode()!=ModelElementDecide.DecideMode.MODE_CONDITION) return null;

		final List<String> destinationStrings=decideElement.getDestinations();
		if (destinationStrings.size()==0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.NoTeleportDestination"),element.getId()),RunModelCreatorStatus.Status.TELEPORT_INVALID_DESTINATION);
		for (String destination: destinationStrings) {
			final int destinationID=RunElementTeleportSource.getDestinationID(element.getModel(),destination);
			if (destinationID<0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.InvalidTeleportDestination"),element.getId(),destination),RunModelCreatorStatus.Status.TELEPORT_INVALID_DESTINATION);
		}

		return RunModelCreatorStatus.ok;
	}

	@Override
	public RunElementTeleportDecideBySequenceData getData(final SimulationData simData) {
		RunElementTeleportDecideBySequenceData data;
		data=(RunElementTeleportDecideBySequenceData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementTeleportDecideBySequenceData(this,simData);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	@Override
	public void prepareRun(final RunModel runModel) {
		destinations=IntStream.of(destinationIDs).mapToObj(id->runModel.elements.get(id)).toArray(RunElement[]::new);
	}

	@Override
	public void processArrival(SimulationData simData, RunDataClient client) {
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
	}

	@Override
	public void processLeave(SimulationData simData, RunDataClient client) {
		final RunElementTeleportDecideBySequenceData data=getData(simData);

		/* Zielstation bestimmen */
		final int nr=data.nextNr;
		data.nextNr++;
		if (data.nextNr>=destinationIDs.length) data.nextNr=0;

		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.TeleportDecideBySequence"),String.format(Language.tr("Simulation.Log.TeleportDecideBySequence.Info"),client.logInfo(simData),name,nr+1,destinations.length));

		/* Kunde zur nächsten Station leiten */
		StationLeaveEvent.sendToStation(simData,client,this,destinations[nr]);
	}
}

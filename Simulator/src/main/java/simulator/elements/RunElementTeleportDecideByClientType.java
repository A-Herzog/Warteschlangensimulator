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
 * Äquivalent zu {@link ModelElementDecideAndTeleport} (für den Modus "Kundentyp")
 * @author Alexander Herzog
 * @see ModelElementDecideAndTeleport
 */
public class RunElementTeleportDecideByClientType extends RunElement {
	/** Namen der Teleport-Zielstationen */
	private String[] destinationStrings;
	/** IDs der Teleport-Zielstationen */
	private int[] destinationIDs;
	/** Teleport-Zielstationen (übersetzt aus {@link #destinationIDs}) */
	private RunElement[] destinations;
	/** Liste der Kundentypen, die angibt, an welchen Ausgang Kunden welchen Typs geleitet werden sollen */
	private int[] clientTypeConnectionIndex;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementTeleportDecideByClientType(final ModelElementDecideAndTeleport element) {
		super(element,buildName(element,Language.tr("Simulation.Element.TeleportDecideByClientType.Name")));
	}

	@Override
	public Object build(EditModel editModel, RunModel runModel, ModelElement element, ModelElementSub parent, boolean testOnly) {
		if (!(element instanceof ModelElementDecideAndTeleport)) return null;
		final ModelElementDecideAndTeleport decideElement=(ModelElementDecideAndTeleport)element;
		if (decideElement.getMode()!=ModelElementDecide.DecideMode.MODE_CLIENTTYPE) return null;
		final RunElementTeleportDecideByClientType decide=new RunElementTeleportDecideByClientType((ModelElementDecideAndTeleport)element);

		decide.destinationStrings=decideElement.getDestinations().toArray(new String[0]);
		decide.clientTypeConnectionIndex=new int[decide.destinationStrings.length];
		decide.destinationIDs=new int[decide.destinationStrings.length];
		int count=0;
		if (decide.destinationStrings.length==0) return String.format(Language.tr("Simulation.Creator.NoTeleportDestination"),element.getId());
		for (String destination: decide.destinationStrings) {
			final int destinationID=RunElementTeleportSource.getDestinationID(element.getModel(),destination);
			if (destinationID<0) return String.format(Language.tr("Simulation.Creator.InvalidTeleportDestination"),element.getId(),decide.destinationStrings[count]);
			decide.destinationIDs[count]=destinationID;
			count++;
		}

		final List<List<String>> clientTypes=decideElement.getClientTypes();
		if (clientTypes.size()<decide.destinationStrings.length-1) return String.format(Language.tr("Simulation.Creator.NotClientTypesForAllDecideConnections"),element.getId());

		/* Array der Verbindungs-Indices erstellen und erstmal alle Kundentypen auf die letzte "Sonst"-Ecke einstellen */
		decide.clientTypeConnectionIndex=new int[runModel.clientTypes.length];
		for (int i=0;i<decide.clientTypeConnectionIndex.length;i++) decide.clientTypeConnectionIndex[i]=decide.destinationStrings.length-1;

		/* Pro Kundentyp korrekte Nummer der Ausgangskante (Index in der Liste, nicht ID) bestimmen */
		for (int i=0;i<decide.destinationStrings.length-1;i++) for (int j=0;j<clientTypes.get(i).size();j++) {
			int index=runModel.getClientTypeNr(clientTypes.get(i).get(j));
			/*
			if (index<0) return String.format(Language.tr("Simulation.Creator.NoClientTypeInDecide"),element.getId(),clientTypes.get(i),i+1);
			Kanten mit Kundentypen, die es nicht gibt (=die z.B. temporär deaktiviert wurden) einfach ignorieren:
			 */
			if (index>=0) decide.clientTypeConnectionIndex[index]=i;
		}

		return decide;
	}

	@Override
	public RunModelCreatorStatus test(ModelElement element) {
		if (!(element instanceof ModelElementDecideAndTeleport)) return null;
		final ModelElementDecideAndTeleport decideElement=(ModelElementDecideAndTeleport)element;
		if (decideElement.getMode()!=ModelElementDecide.DecideMode.MODE_CLIENTTYPE) return null;

		final List<String> destinationStrings=decideElement.getDestinations();
		if (destinationStrings.size()==0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.NoTeleportDestination"),element.getId()),RunModelCreatorStatus.Status.TELEPORT_INVALID_DESTINATION);
		for (String destination: destinationStrings) {
			final int destinationID=RunElementTeleportSource.getDestinationID(element.getModel(),destination);
			if (destinationID<0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.InvalidTeleportDestination"),element.getId(),destination),RunModelCreatorStatus.Status.TELEPORT_INVALID_DESTINATION);
		}

		return RunModelCreatorStatus.ok;
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
		final int nr=clientTypeConnectionIndex[client.type];

		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.TeleportDecideByClientType"),String.format(Language.tr("Simulation.Log.TeleportDecideByClientType.Info"),client.logInfo(simData),name,nr+1,destinations.length));

		/* Kunde zur nächsten Station leiten */
		StationLeaveEvent.sendToStation(simData,client,this,destinations[nr]);
	}
}

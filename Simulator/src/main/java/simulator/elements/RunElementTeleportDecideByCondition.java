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
import simulator.simparser.ExpressionMultiEval;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementDecide;
import ui.modeleditor.elements.ModelElementDecideAndTeleport;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu {@link ModelElementDecideAndTeleport} (für den Modus "Bedingung")
 * @author Alexander Herzog
 * @see ModelElementDecideAndTeleport
 */
public class RunElementTeleportDecideByCondition extends RunElement {
	/** Namen der Teleport-Zielstationen */
	private String[] destinationStrings;
	/** IDs der Teleport-Zielstationen */
	private int[] destinationIDs;
	/** Teleport-Zielstationen (übersetzt aus {@link #destinationIDs}) */
	private RunElement[] destinations;
	/** Bedingungen, die erfüllt sein müssen, damit ein Kunde an einen bestimmten Ausgang geleitet wird (<code>null</code>-Einträge bedeuten: immer erfüllt) */
	private String[] conditions;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementTeleportDecideByCondition(final ModelElementDecideAndTeleport element) {
		super(element,buildName(element,Language.tr("Simulation.Element.TeleportDecideByCondition.Name")));
	}

	@Override
	public Object build(EditModel editModel, RunModel runModel, ModelElement element, ModelElementSub parent, boolean testOnly) {
		if (!(element instanceof ModelElementDecideAndTeleport)) return null;
		final ModelElementDecideAndTeleport decideElement=(ModelElementDecideAndTeleport)element;
		if (decideElement.getMode()!=ModelElementDecide.DecideMode.MODE_CONDITION) return null;
		final RunElementTeleportDecideByCondition decide=new RunElementTeleportDecideByCondition((ModelElementDecideAndTeleport)element);

		decide.destinationStrings=decideElement.getDestinations().toArray(String[]::new);
		final List<String> editConditions=decideElement.getConditions();
		decide.conditions=new String[decide.destinationStrings.length];
		decide.destinationIDs=new int[decide.destinationStrings.length];
		int count=0;
		if (decide.destinationStrings.length==0) return String.format(Language.tr("Simulation.Creator.NoTeleportDestination"),element.getId());
		for (String destination: decide.destinationStrings) {
			final int destinationID=RunElementTeleportSource.getDestinationID(element.getModel(),destination);
			if (destinationID<0) return String.format(Language.tr("Simulation.Creator.InvalidTeleportDestination"),element.getId(),decide.destinationStrings[count]);
			decide.destinationIDs[count]=destinationID;
			if (count<decide.destinationStrings.length-1) {
				String condition=(count>=editConditions.size())?"":editConditions.get(count);
				if (condition==null) condition="";
				final int error=ExpressionMultiEval.check(condition,runModel.variableNames,runModel.modelUserFunctions);
				if (error>=0) return String.format(Language.tr("Simulation.Creator.DecideCondition"),count+1,condition,element.getId(),error+1);
				decide.conditions[count]=condition;
			}
			count++;
		}

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
	public void prepareRun(final RunModel runModel) {
		destinations=IntStream.of(destinationIDs).mapToObj(id->runModel.elements.get(id)).toArray(RunElement[]::new);
	}

	@Override
	public RunElementTeleportDecideByConditionData getData(final SimulationData simData) {
		RunElementTeleportDecideByConditionData data;
		data=(RunElementTeleportDecideByConditionData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementTeleportDecideByConditionData(this,conditions,simData.runModel.variableNames,simData);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	@Override
	public void processArrival(SimulationData simData, RunDataClient client) {
		final RunElementTeleportDecideByConditionData data=getData(simData);

		/* Zielstation bestimmen */
		int nr=-1;
		simData.runData.setClientVariableValues(client);
		for (int i=0;i<data.conditions.length;i++) if (data.conditions[i]==null || data.conditions[i].eval(simData.runData.variableValues,simData,client)) {nr=i; break;}
		if (nr==-1) nr=data.conditions.length-1;

		client.stationInformationInt=nr;

		/* Kunde weiterleiten */
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
		StationLeaveEvent.announceClient(simData,client,destinations[nr]);
	}

	@Override
	public void processLeave(SimulationData simData, RunDataClient client) {
		/* Zielstation bestimmen */
		int nr=client.stationInformationInt;

		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.TeleportDecideByCondition"),String.format(Language.tr("Simulation.Log.TeleportDecideByCondition.Info"),client.logInfo(simData),name,nr+1,destinations.length));

		/* Kunde zur nächsten Station leiten */
		StationLeaveEvent.unannounceClient(simData,client,destinations[nr]);
		StationLeaveEvent.sendToStation(simData,client,this,destinations[nr]);
	}
}

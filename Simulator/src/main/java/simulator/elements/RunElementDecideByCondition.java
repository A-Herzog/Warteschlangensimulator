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
import ui.modeleditor.elements.ModelElementEdge;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementDecide</code>
 * @author Alexander Herzog
 * @see ModelElementDecide
 */
public class RunElementDecideByCondition extends RunElement {
	/** IDs der über die auslaufenden Kanten erreichbaren Folgestationen */
	private List<Integer> connectionIds;
	/** Über die auslaufenden Kanten erreichbaren Folgestationen (aus {@link #connectionIds} abgeleitet) */
	private RunElement[] connections;
	/** Bedingungen, die erfüllt sein müssen, damit ein Kunde an einen bestimmten Ausgang geleitet wird (<code>null</code>-Einträge bedeuten: immer erfüllt) */
	private String[] conditions;

	/** Kundentyp-Zuweisungen an den Ausgängen */
	private String[] clientTypeNames;
	/** IDs der Kundentyp-Zuweisungen an den Ausgängen (kann in Gänze <code>null</code> sein, oder einzelne Einträge können -1 sein) */
	private int[] clientTypeIds;
	/** Icons für die Kundentyp-Zuweisungen an den Ausgängen */
	private String[] clientTypeIcons;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementDecideByCondition(final ModelElementDecide element) {
		super(element,buildName(element,Language.tr("Simulation.Element.DecideByCondition.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementDecide)) return null;
		if (((ModelElementDecide)element).getMode()!=ModelElementDecide.DecideMode.MODE_CONDITION) return null;

		final RunElementDecideByCondition decide=new RunElementDecideByCondition((ModelElementDecide)element);

		decide.connectionIds=new ArrayList<>();
		ModelElementEdge[] edges=((ModelElementDecide)element).getEdgesOut();
		if (edges.length==0) return String.format(Language.tr("Simulation.Creator.NoEdgeOut"),element.getId());
		decide.conditions=new String[edges.length-1];

		final List<String> editConditions=((ModelElementDecide)element).getConditions();
		for (int i=0;i<edges.length;i++) {
			final ModelElementEdge edge=edges[i];
			final int id=findNextId(edge);
			if (id<0) return String.format(Language.tr("Simulation.Creator.EdgeToNowhere"),element.getId(),edge.getId());
			decide.connectionIds.add(id);

			if (i<edges.length-1) {
				String condition=(i>=editConditions.size())?"":editConditions.get(i);
				if (condition==null) condition="";
				final int error=ExpressionMultiEval.check(condition,runModel.variableNames,runModel.modelUserFunctions);
				if (error>=0) return String.format(Language.tr("Simulation.Creator.DecideCondition"),i+1,condition,element.getId(),error+1);
				decide.conditions[i]=condition;
			}
		}

		/* Kundentypzuweisungen */
		decide.clientTypeNames=((ModelElementDecide)element).getChangedClientTypes().toArray(new String[0]);
		decide.clientTypeIcons=new String[decide.clientTypeNames.length];
		for (int i=0;i<decide.clientTypeIcons.length;i++) decide.clientTypeIcons[i]=editModel.clientData.getIcon(decide.clientTypeNames[i]);

		return decide;
	}

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementDecide)) return null;
		if (((ModelElementDecide)element).getMode()!=ModelElementDecide.DecideMode.MODE_CONDITION) return null;

		ModelElementEdge[] edges=((ModelElementDecide)element).getEdgesOut();
		if (edges.length==0) return RunModelCreatorStatus.noEdgeOut(element);
		for (int i=0;i<edges.length;i++) {
			final ModelElementEdge edge=edges[i];
			final int id=findNextId(edge);
			if (id<0) return RunModelCreatorStatus.edgeToNowhere(element,edge);
		}

		return RunModelCreatorStatus.ok;
	}

	@Override
	public void prepareRun(final RunModel runModel) {
		connections=new RunElement[connectionIds.size()];
		for (int i=0;i<connectionIds.size();i++) connections[i]=runModel.elements.get(connectionIds.get(i));

		clientTypeIds=new int[clientTypeNames.length];
		boolean hasData=false;
		for (int i=0;i<clientTypeNames.length;i++) {
			final int nr=runModel.getClientTypeNr(clientTypeNames[i]);
			if (nr>=0) hasData=true;
			clientTypeIds[i]=nr;
		}
		if (!hasData) clientTypeIds=null;
	}

	@Override
	public RunElementDecideByConditionData getData(final SimulationData simData) {
		RunElementDecideByConditionData data;
		data=(RunElementDecideByConditionData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementDecideByConditionData(this,conditions,simData.runModel.variableNames,simData);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	@Override
	public void processArrival(final SimulationData simData, final RunDataClient client) {
		final RunElementDecideByConditionData data=getData(simData);

		/* Zielstation bestimmen */
		int nr=-1;
		simData.runData.setClientVariableValues(client);
		for (int i=0;i<data.conditions.length;i++) if (data.conditions[i]==null || data.conditions[i].eval(simData.runData.variableValues,simData,client)) {nr=i; break;}
		if (nr==-1) nr=data.conditions.length; /* Nicht: length-1, denn conditions sind bereits eine weniger als Ausgänge */

		client.stationInformationInt=nr;

		/* Kunde weiterleiten */
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
		StationLeaveEvent.announceClient(simData,client,connections[nr]);
	}

	@Override
	public void processLeave(final SimulationData simData, final RunDataClient client) {
		/* Zielstation bestimmen */
		int nr=client.stationInformationInt;

		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.DecideByCondition"),String.format(Language.tr("Simulation.Log.DecideByCondition.Info"),client.logInfo(simData),name,nr+1,connections.length));

		/* Kundentypen zuweisen */
		if (clientTypeIds!=null) {
			final int type=clientTypeIds[nr];
			if (type>=0) {
				/* Kundentyp zuweisen */
				final int lastType=client.type;
				client.typeLast=lastType;
				client.type=type;
				client.iconLast=client.icon;
				client.icon=clientTypeIcons[nr];

				/* Logging */
				if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.DecideByCondition"),String.format(Language.tr("Simulation.Log.Decide.AssignInfo"),client.hashCode(),simData.runModel.clientTypes[lastType],simData.runModel.clientTypes[type],name));
			}
		}

		/* Kunde zur nächsten Station leiten */
		StationLeaveEvent.unannounceClient(simData,client,connections[nr]);
		StationLeaveEvent.sendToStation(simData,client,this,connections[nr]);
	}
}

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
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementDecide;
import ui.modeleditor.elements.ModelElementEdge;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementDecide</code>
 * @author Alexander Herzog
 * @see ModelElementDecide
 */
public class RunElementDecideByClientType extends RunElement {
	/** IDs der über die auslaufenden Kanten erreichbaren Folgestationen */
	private List<Integer> connectionIds;
	/** Über die auslaufenden Kanten erreichbaren Folgestationen (aus {@link #connectionIds} abgeleitet) */
	private RunElement[] connections;
	/** Liste der Kundentypen, die angibt, an welchen Ausgang Kunden welchen Typs geleitet werden sollen */
	private int[] clientTypeConnectionIndex;

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
	public RunElementDecideByClientType(final ModelElementDecide element) {
		super(element,buildName(element,Language.tr("Simulation.Element.DecideByClientType.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementDecide)) return null;
		if (((ModelElementDecide)element).getMode()!=ModelElementDecide.DecideMode.MODE_CLIENTTYPE) return null;

		final RunElementDecideByClientType decide=new RunElementDecideByClientType((ModelElementDecide)element);

		decide.connectionIds=new ArrayList<>();
		ModelElementEdge[] edges=((ModelElementDecide)element).getEdgesOut();
		List<List<String>> clientTypes=((ModelElementDecide)element).getClientTypes();
		if (clientTypes.size()<edges.length-1) return String.format(Language.tr("Simulation.Creator.NotClientTypesForAllDecideConnections"),element.getId());

		/* Ausgangskanten erfassen */
		if (edges.length==0) return String.format(Language.tr("Simulation.Creator.NoEdgeOut"),element.getId());
		for (int i=0;i<edges.length;i++) {
			final ModelElementEdge edge=edges[i];
			final int id=findNextId(edge);
			if (id<0) return String.format(Language.tr("Simulation.Creator.EdgeToNowhere"),element.getId(),edge.getId());
			decide.connectionIds.add(id);
		}

		/* Array der Verbindungs-Indices erstellen und erstmal alle Kundentypen auf die letzte "Sonst"-Ecke einstellen */
		decide.clientTypeConnectionIndex=new int[runModel.clientTypes.length];
		for (int i=0;i<decide.clientTypeConnectionIndex.length;i++) decide.clientTypeConnectionIndex[i]=edges.length-1;

		/* Pro Kundentyp korrekte Nummer der Ausgangskante (Index in der Liste, nicht ID) bestimmen */
		for (int i=0;i<edges.length-1;i++) for (int j=0;j<clientTypes.get(i).size();j++) {
			int index=runModel.getClientTypeNr(clientTypes.get(i).get(j));
			/*
			if (index<0) return String.format(Language.tr("Simulation.Creator.NoClientTypeInDecide"),element.getId(),clientTypes.get(i),i+1);
			Kanten mit Kundentypen, die es nicht gibt (=die z.B. temporär deaktiviert wurden) einfach ignorieren:
			 */
			if (index>=0) decide.clientTypeConnectionIndex[index]=i;
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
		if (((ModelElementDecide)element).getMode()!=ModelElementDecide.DecideMode.MODE_CLIENTTYPE) return null;

		ModelElementEdge[] edges=((ModelElementDecide)element).getEdgesOut();
		List<List<String>> clientTypes=((ModelElementDecide)element).getClientTypes();
		if (clientTypes.size()<edges.length-1) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.NotClientTypesForAllDecideConnections"),element.getId()));

		/* Ausgangskanten erfassen */
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
	public void processArrival(final SimulationData simData, final RunDataClient client) {
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
	}

	@Override
	public void processLeave(final SimulationData simData, final RunDataClient client) {
		final int nr=clientTypeConnectionIndex[client.type];

		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.DecideByClientType"),String.format(Language.tr("Simulation.Log.DecideByClientType.Info"),client.logInfo(simData),name,nr+1,connections.length));

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
				if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.DecideByClientType"),String.format(Language.tr("Simulation.Log.Decide.AssignInfo"),client.hashCode(),simData.runModel.clientTypes[lastType],simData.runModel.clientTypes[type],name));
			}
		}

		/* Kunde zur nächsten Station leiten */
		StationLeaveEvent.sendToStation(simData,client,this,connections[nr]);
	}
}

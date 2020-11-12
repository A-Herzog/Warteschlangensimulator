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
import ui.modeleditor.elements.ModelElementDuplicate;
import ui.modeleditor.elements.ModelElementEdge;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementDuplicate</code>
 * @author Alexander Herzog
 * @see ModelElementDuplicate
 */
public class RunElementDuplicate extends RunElement {
	/** IDs der über die auslaufenden Kanten erreichbaren Folgestationen */
	private List<Integer> connectionIds;
	/** Über die auslaufenden Kanten erreichbaren Folgestationen (aus {@link #connectionIds} abgeleitet) */
	private RunElement[] connections;

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
	public RunElementDuplicate(final ModelElementDuplicate element) {
		super(element,buildName(element,Language.tr("Simulation.Element.Duplicate.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementDuplicate)) return null;
		final ModelElementDuplicate duplicateElement=(ModelElementDuplicate)element;
		final RunElementDuplicate duplicate=new RunElementDuplicate(duplicateElement);

		duplicate.connectionIds=new ArrayList<>();
		for (ModelElementEdge edge: duplicateElement.getEdgesOut()) {
			int id=findNextId(edge);
			if (id<0) return String.format(Language.tr("Simulation.Creator.EdgeToNowhere"),element.getId(),edge.getId());
			duplicate.connectionIds.add(id);
		}
		if (duplicate.connectionIds.size()==0) return String.format(Language.tr("Simulation.Creator.NoEdgeOut"),element.getId());

		duplicate.clientTypeNames=duplicateElement.getChangedClientTypes().toArray(new String[0]);
		duplicate.clientTypeIcons=new String[duplicate.clientTypeNames.length];
		for (int i=0;i<duplicate.clientTypeIcons.length;i++) duplicate.clientTypeIcons[i]=editModel.clientData.getIcon(duplicate.clientTypeNames[i]);

		return duplicate;
	}

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementDuplicate)) return null;
		final ModelElementDuplicate duplicateElement=(ModelElementDuplicate)element;

		for (ModelElementEdge edge: duplicateElement.getEdgesOut()) {
			int id=findNextId(edge);
			if (id<0) return RunModelCreatorStatus.edgeToNowhere(element,edge);
		}
		if (duplicateElement.getEdgesOut().length==0) return RunModelCreatorStatus.noEdgeOut(element);

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
	public RunElementDuplicateData getData(final SimulationData simData) {
		RunElementDuplicateData data;
		data=(RunElementDuplicateData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementDuplicateData(this,connections.length);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	@Override
	public void processArrival(final SimulationData simData, final RunDataClient client) {
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
	}

	@Override
	public void processLeave(final SimulationData simData, final RunDataClient client) {
		final RunElementDuplicateData data=getData(simData);

		data.newClientsList[0]=client;

		/* Clone erstellen */
		for (int i=1;i<connections.length;i++) data.newClientsList[i]=simData.runData.clients.getClone(client,simData);

		/* Logging */
		if (simData.loggingActive) {
			final StringBuilder sb=new StringBuilder();
			for (int i=1;i<connections.length;i++) {sb.append(", id="); sb.append(data.newClientsList[i].hashCode());}
			log(simData,Language.tr("Simulation.Log.Duplicate"),String.format(Language.tr("Simulation.Log.Duplicate.Info"),client.hashCode(),sb.toString(),name));
		}

		/* Kundentypen zuweisen */
		if (clientTypeIds!=null) for (int i=0;i<clientTypeIds.length;i++) {
			final int nr=clientTypeIds[i];
			if (nr>=0) {
				/* Kundentyp zuweisen */
				RunDataClient newClient=data.newClientsList[i];
				newClient.changeType(nr,simData);
				newClient.iconLast=newClient.icon;
				newClient.icon=clientTypeIcons[i];

				/* Logging */
				if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.Duplicate"),String.format(Language.tr("Simulation.Log.Duplicate.AssignInfo"),newClient.hashCode(),simData.runModel.clientTypes[newClient.typeLast],simData.runModel.clientTypes[nr],name));
			}
		}

		/* Kunden zu den nächsten Stationen leiten */
		StationLeaveEvent.multiSendToStation(simData,data.newClientsList,this,connections);
	}
}

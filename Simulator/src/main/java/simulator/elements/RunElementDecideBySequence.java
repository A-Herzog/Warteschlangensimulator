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
import ui.modeleditor.elements.DecideRecord;
import ui.modeleditor.elements.ModelElementDecide;
import ui.modeleditor.elements.ModelElementEdge;
import ui.modeleditor.elements.ModelElementSub;

/**
 * �quivalent zu <code>ModelElementDecide</code>
 * @author Alexander Herzog
 * @see ModelElementDecide
 */
public class RunElementDecideBySequence extends RunElement {
	/** IDs der �ber die auslaufenden Kanten erreichbaren Folgestationen */
	private List<Integer> connectionIds;
	/** �ber die auslaufenden Kanten erreichbaren Folgestationen (aus {@link #connectionIds} abgeleitet) */
	private RunElement[] connections;

	/** Kundentyp-Zuweisungen an den Ausg�ngen */
	private String[] clientTypeNames;
	/** IDs der Kundentyp-Zuweisungen an den Ausg�ngen (kann in G�nze <code>null</code> sein, oder einzelne Eintr�ge k�nnen -1 sein) */
	private int[] clientTypeIds;
	/** Icons f�r die Kundentyp-Zuweisungen an den Ausg�ngen */
	private String[] clientTypeIcons;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugeh�riges Editor-Element
	 */
	public RunElementDecideBySequence(final ModelElementDecide element) {
		super(element,buildName(element,Language.tr("Simulation.Element.DecideBySequence.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementDecide)) return null;
		if (((ModelElementDecide)element).getDecideRecord(). getMode()!=DecideRecord.DecideMode.MODE_SEQUENCE) return null;

		final RunElementDecideBySequence decide=new RunElementDecideBySequence((ModelElementDecide)element);

		decide.connectionIds=new ArrayList<>();
		final ModelElementEdge[] edges=((ModelElementDecide)element).getEdgesOut();
		if (edges.length==0) return String.format(Language.tr("Simulation.Creator.NoEdgeOut"),element.getId());

		final List<Integer> edgesMultiplicity=((ModelElementDecide)element).getDecideRecord().getMultiplicity();

		for (int i=0;i<edges.length;i++) {
			final ModelElementEdge edge=edges[i];
			final int mul=(i>=edgesMultiplicity.size())?1:edgesMultiplicity.get(i).intValue();
			final int id=findNextId(edge);
			if (id<0) return String.format(Language.tr("Simulation.Creator.EdgeToNowhere"),element.getId(),edge.getId());
			for (int j=0;j<mul;j++) decide.connectionIds.add(id);
		}

		/* Kundentypzuweisungen */
		decide.clientTypeNames=new String[edgesMultiplicity.stream().mapToInt(Integer::intValue).sum()];
		final List<String> names=((ModelElementDecide)element).getChangedClientTypes();
		int nr=0;
		for (int i=0;i<names.size();i++) {
			final int count=edgesMultiplicity.get(i);
			for (int j=0;j<count;j++) decide.clientTypeNames[nr+j]=names.get(i);
			nr+=count;
		}

		decide.clientTypeIcons=new String[decide.clientTypeNames.length];
		for (int i=0;i<decide.clientTypeIcons.length;i++) decide.clientTypeIcons[i]=editModel.clientData.getIcon(decide.clientTypeNames[i]);

		return decide;
	}

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementDecide)) return null;
		if (((ModelElementDecide)element).getDecideRecord(). getMode()!=DecideRecord.DecideMode.MODE_SEQUENCE) return null;

		ModelElementEdge[] edges=((ModelElementDecide)element).getEdgesOut();
		if (edges.length==0) return RunModelCreatorStatus.noEdgeOut(element);
		for (ModelElementEdge edge : edges) {
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
	public RunElementDecideBySequenceData getData(final SimulationData simData) {
		RunElementDecideBySequenceData data;
		data=(RunElementDecideBySequenceData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementDecideBySequenceData(this,simData);
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
		final RunElementDecideBySequenceData data=getData(simData);

		/* Zielstation bestimmen */
		final int nr=data.nextNr;
		data.nextNr++;
		if (data.nextNr>=connections.length) data.nextNr=0;

		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.DecideBySequence"),String.format(Language.tr("Simulation.Log.DecideBySequence.Info"),client.logInfo(simData),name,nr+1,connections.length));

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
				if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.DecideBySequence"),String.format(Language.tr("Simulation.Log.Decide.AssignInfo"),client.hashCode(),simData.runModel.clientTypes[lastType],simData.runModel.clientTypes[type],name));
			}
		}

		/* Kunde zur n�chsten Station leiten */
		StationLeaveEvent.sendToStation(simData,client,this,connections[nr]);
	}
}

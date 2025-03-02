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

import org.apache.commons.math3.util.FastMath;

import language.Language;
import mathtools.distribution.tools.DistributionRandomNumber;
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElement;
import simulator.editmodel.EditModel;
import simulator.events.StationLeaveEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementEdgeMultiOut;
import ui.modeleditor.coreelements.ModelElementEdgeOut;
import ui.modeleditor.elements.ElementWithDecideData;
import ui.modeleditor.elements.ModelElementDecide;
import ui.modeleditor.elements.ModelElementEdge;
import ui.modeleditor.elements.ModelElementProcess;
import ui.modeleditor.elements.ModelElementSub;
import ui.modeleditor.elements.ModelElementTeleportDestination;
import ui.modeleditor.elements.ModelElementTeleportSource;
import ui.modeleditor.elements.ModelElementVertex;

/**
 * Äquivalent zu {@link ModelElementDecide}
 * @author Alexander Herzog
 * @see ModelElementDecide
 */
public class RunElementDecideByStation extends RunElement {
	/** Wie soll bei Gleichstand zwischen mehreren Ausgängen entschieden werden? */
	private ElementWithDecideData.DecideByStationOnTie decideModeOnTie;
	/** IDs der über die auslaufenden Kanten erreichbaren Folgestationen */
	private List<Integer> connectionIds;
	/** Über die auslaufenden Kanten erreichbaren Folgestationen (aus {@link #connectionIds} abgeleitet) */
	private RunElement[] connections;
	/** IDs der über die jeweiligen Ausgangskanten folgenden relevanten Stationen */
	private List<Integer> nextIds;
	/** Über die jeweiligen Ausgangskanten folgenden relevanten Stationen (Übersetzung der IDs aus {@link #nextIds}) */
	private RunElement[] next;
	/** Verzweigungsmodus (4 Modi: kürzeste Warteschlange/wenigste Kunden an der nächsten Station/Bedienstation) */
	private ModelElementDecide.DecideMode mode;

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
	public RunElementDecideByStation(final ModelElementDecide element) {
		super(element,buildName(element,Language.tr("Simulation.Element.DecideByStation.Name")));
	}

	/**
	 * Liefert die über eine Kante bzw. Folgestation als nächstes erreichbare Station.
	 * @param editModel	Editor-Modell
	 * @param element	Aktuelle Station von der die Suche ausgehen soll
	 * @param processStation	Nächste Station (<code>false</code>) oder nächste Bedienstation (<code>true</code>) suchen?
	 * @return	Liefert  im Erfolgsfall die ID der passenden Folgestation, sonst -1
	 */
	private int getNext(final EditModel editModel, ModelElement element, final boolean processStation) {
		while (element!=null) {

			if (element instanceof ModelElementEdge) {
				element=((ModelElementEdge)element).getConnectionEnd();
				continue;
			}

			if (element instanceof ModelElementVertex) {
				element=((ModelElementVertex)element).getEdgeOut();
				continue;
			}

			if (element instanceof ModelElementTeleportSource) {
				final ModelElementTeleportDestination destination=RunElementTeleportSource.getDestination(element.getModel(),((ModelElementTeleportSource)element).getDestination());
				if (destination==null) return -1;
				element=destination.getEdgeOut();
				continue;
			}

			if (element instanceof ModelElementBox) {
				if (!processStation || (element instanceof ModelElementProcess)) return element.getId();
				if (element instanceof ModelElementEdgeOut) {
					element=((ModelElementEdgeOut)element).getEdgeOut();
					continue;
				}
				if (element instanceof ModelElementEdgeMultiOut) {
					ModelElementEdge[] edges=((ModelElementEdgeMultiOut)element).getEdgesOut();
					if (edges!=null && edges.length==1) {
						element=edges[0];
						continue;
					}
				}
			}
			return -1;
		}
		return -1;
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementDecide)) return null;
		final ModelElementDecide.DecideMode mode=((ModelElementDecide)element).getMode();
		if (mode!=ModelElementDecide.DecideMode.MODE_MIN_CLIENTS_NEXT_STATION && mode!=ModelElementDecide.DecideMode.MODE_MIN_CLIENTS_PROCESS_STATION && mode!=ModelElementDecide.DecideMode.MODE_SHORTEST_QUEUE_NEXT_STATION && mode!=ModelElementDecide.DecideMode.MODE_SHORTEST_QUEUE_PROCESS_STATION) return null;
		final boolean processStation=(mode==ModelElementDecide.DecideMode.MODE_MIN_CLIENTS_PROCESS_STATION || mode==ModelElementDecide.DecideMode.MODE_SHORTEST_QUEUE_PROCESS_STATION);

		RunElementDecideByStation decide=new RunElementDecideByStation((ModelElementDecide)element);
		decide.mode=mode;
		decide.decideModeOnTie=((ModelElementDecide)element).getDecideByStationOnTie();

		decide.connectionIds=new ArrayList<>();
		decide.nextIds=new ArrayList<>();
		ModelElementEdge[] edges=((ModelElementDecide)element).getEdgesOut();
		if (edges.length==0) return String.format(Language.tr("Simulation.Creator.NoEdgeOut"),element.getId());
		for (ModelElementEdge edge : edges) {
			final int id=findNextId(edge);
			if (id<0) return String.format(Language.tr("Simulation.Creator.EdgeToNowhere"),element.getId(),edge.getId());
			decide.connectionIds.add(id);
			final int nextId=getNext(editModel,edge,processStation);
			if (nextId<0) return String.format(Language.tr("Simulation.Creator.EdgeToNoValidNextElement"),element.getId(),edge.getId());
			decide.nextIds.add(nextId);
		}

		/* Kundentypzuweisungen */
		decide.clientTypeNames=((ModelElementDecide)element).getChangedClientTypes().toArray(String[]::new);
		decide.clientTypeIcons=new String[decide.clientTypeNames.length];
		for (int i=0;i<decide.clientTypeIcons.length;i++) decide.clientTypeIcons[i]=editModel.clientData.getIcon(decide.clientTypeNames[i]);

		return decide;
	}

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementDecide)) return null;
		final ModelElementDecide.DecideMode mode=((ModelElementDecide)element).getMode();
		if (mode!=ModelElementDecide.DecideMode.MODE_MIN_CLIENTS_NEXT_STATION && mode!=ModelElementDecide.DecideMode.MODE_MIN_CLIENTS_PROCESS_STATION && mode!=ModelElementDecide.DecideMode.MODE_SHORTEST_QUEUE_NEXT_STATION && mode!=ModelElementDecide.DecideMode.MODE_SHORTEST_QUEUE_PROCESS_STATION) return null;
		final boolean processStation=(mode==ModelElementDecide.DecideMode.MODE_MIN_CLIENTS_PROCESS_STATION || mode==ModelElementDecide.DecideMode.MODE_SHORTEST_QUEUE_PROCESS_STATION);

		ModelElementEdge[] edges=((ModelElementDecide)element).getEdgesOut();
		if (edges.length==0) return RunModelCreatorStatus.noEdgeOut(element);
		for (ModelElementEdge edge : edges) {
			final int id=findNextId(edge);
			if (id<0) return RunModelCreatorStatus.edgeToNowhere(element,edge);
			final int nextId=getNext(element.getModel(),edge,processStation);
			if (nextId<0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.EdgeToNoValidNextElement"),element.getId(),edge.getId()));
		}

		return RunModelCreatorStatus.ok;
	}

	@Override
	public void prepareRun(final RunModel runModel) {
		connections=new RunElement[connectionIds.size()];
		for (int i=0;i<connectionIds.size();i++) connections[i]=runModel.elements.get(connectionIds.get(i));
		next=new RunElement[nextIds.size()];
		for (int i=0;i<nextIds.size();i++) next[i]=runModel.elements.get(nextIds.get(i));

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
	public RunElementDecideByStationData getData(final SimulationData simData) {
		RunElementDecideByStationData data;
		data=(RunElementDecideByStationData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementDecideByStationData(this,next.length,simData);
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
		/* Zielstation bestimmen */
		int bestValue=Integer.MAX_VALUE;
		int[] bestIndices=getData(simData).values;
		int bestIndicesUsed=0;

		for (int i=0;i<next.length;i++) {
			int value=0;
			switch (mode) {
			case MODE_SHORTEST_QUEUE_NEXT_STATION:
			case MODE_SHORTEST_QUEUE_PROCESS_STATION:
				value=next[i].getData(simData).clientsAtStationQueue;
				break;
			case MODE_MIN_CLIENTS_NEXT_STATION:
			case MODE_MIN_CLIENTS_PROCESS_STATION:
				value=next[i].getData(simData).reportedClientsAtStation(simData);
				break;
			default:
				/* Andere Fälle wurden schon in build ausgefiltert. */
				break;
			}
			if (value<bestValue) {
				bestValue=value;
				bestIndices[0]=i;
				bestIndicesUsed=1;
			} else {
				if (value==bestValue) {
					bestIndices[bestIndicesUsed]=i;
					bestIndicesUsed++;
				}
			}
		}

		int nr=0;
		if (bestIndicesUsed==1) {
			/* Eindeutig bestes Ziel gefunden. */
			nr=bestIndices[0];
		}
		if (bestIndicesUsed>1) {
			switch (decideModeOnTie) {
			case FIRST: /* Bei Gleichstand erste Kante wählen */
				nr=bestIndices[0];
				break;
			case LAST:  /* Bei Gleichstand letzte Kante wählen */
				nr=bestIndices[bestIndicesUsed-1];
				break;
			case RANDOM: /* Bei gleichem Wert Ziel zufällig wählen. */
				nr=bestIndices[(int)FastMath.round(FastMath.floor(bestIndicesUsed*DistributionRandomNumber.nextDouble()))];
				break;
			default:
				nr=bestIndices[(int)FastMath.round(FastMath.floor(bestIndicesUsed*DistributionRandomNumber.nextDouble()))];
				break;
			}
		}

		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.DecideByStationData"),String.format(Language.tr("Simulation.Log.DecideByStationData.Info"),client.logInfo(simData),name,nr+1,connections.length));

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
				if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.DecideByStationData"),String.format(Language.tr("Simulation.Log.Decide.AssignInfo"),client.hashCode(),simData.runModel.clientTypes[lastType],simData.runModel.clientTypes[type],name));
			}
		}

		/* Kunde zur nächsten Station leiten */
		StationLeaveEvent.sendToStation(simData,client,this,connections[nr]);
	}
}

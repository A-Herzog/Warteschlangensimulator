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
import ui.modeleditor.elements.ModelElementDecideAndTeleport;
import ui.modeleditor.elements.ModelElementEdge;
import ui.modeleditor.elements.ModelElementProcess;
import ui.modeleditor.elements.ModelElementSub;
import ui.modeleditor.elements.ModelElementTeleportDestination;
import ui.modeleditor.elements.ModelElementTeleportSource;
import ui.modeleditor.elements.ModelElementVertex;

/**
 * Äquivalent zu {@link ModelElementDecideAndTeleport} (für den Modus "Stationseigenschaften")
 * @author Alexander Herzog
 * @see ModelElementDecideAndTeleport
 */
public class RunElementTeleportDecideByStation extends RunElement {
	/** Wie soll bei Gleichstand zwischen mehreren Ausgängen entschieden werden? */
	private ElementWithDecideData.DecideByStationOnTie decideModeOnTie;
	/** Namen der Teleport-Zielstationen */
	private String[] destinationStrings;
	/** IDs der Teleport-Zielstationen */
	private int[] destinationIDs;
	/** Teleport-Zielstationen (übersetzt aus {@link #destinationIDs}) */
	private RunElement[] destinations;

	/** IDs der über die jeweiligen Ausgangskanten folgenden relevanten Stationen */
	private int[] nextIds;
	/** Über die jeweiligen Ausgangskanten folgenden relevanten Stationen (Übersetzung der IDs aus {@link #nextIds}) */
	private RunElement[] next;
	/** Verzweigungsmodus (4 Modi: kürzeste Warteschlange/wenigste Kunden an der nächsten Station/Bedienstation) */
	private ModelElementDecide.DecideMode mode;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementTeleportDecideByStation(final ModelElementDecideAndTeleport element) {
		super(element,buildName(element,Language.tr("Simulation.Element.TeleportDecideByStation.Name")));
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

			if (element instanceof ModelElementTeleportDestination) {
				element=((ModelElementTeleportDestination)element).getEdgeOut();
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
	public Object build(EditModel editModel, RunModel runModel, ModelElement element, ModelElementSub parent, boolean testOnly) {
		if (!(element instanceof ModelElementDecideAndTeleport)) return null;
		final ModelElementDecideAndTeleport decideElement=(ModelElementDecideAndTeleport)element;
		final ModelElementDecide.DecideMode mode=decideElement.getMode();
		if (mode!=ModelElementDecide.DecideMode.MODE_MIN_CLIENTS_NEXT_STATION && mode!=ModelElementDecide.DecideMode.MODE_MIN_CLIENTS_PROCESS_STATION && mode!=ModelElementDecide.DecideMode.MODE_SHORTEST_QUEUE_NEXT_STATION && mode!=ModelElementDecide.DecideMode.MODE_SHORTEST_QUEUE_PROCESS_STATION) return null;
		final boolean processStation=(mode==ModelElementDecide.DecideMode.MODE_MIN_CLIENTS_PROCESS_STATION || mode==ModelElementDecide.DecideMode.MODE_SHORTEST_QUEUE_PROCESS_STATION);
		final RunElementTeleportDecideByStation decide=new RunElementTeleportDecideByStation((ModelElementDecideAndTeleport)element);

		decide.mode=mode;
		decide.decideModeOnTie=decideElement.getDecideByStationOnTie();
		decide.destinationStrings=decideElement.getDestinations().toArray(String[]::new);
		decide.destinationIDs=new int[decide.destinationStrings.length];
		decide.nextIds=new int[decide.destinationStrings.length];
		int count=0;
		if (decide.destinationStrings.length==0) return String.format(Language.tr("Simulation.Creator.NoTeleportDestination"),element.getId());
		for (String destination: decide.destinationStrings) {
			final int destinationID=RunElementTeleportSource.getDestinationID(element.getModel(),destination);
			if (destinationID<0) return String.format(Language.tr("Simulation.Creator.InvalidTeleportDestination"),element.getId(),decide.destinationStrings[count]);
			decide.destinationIDs[count]=destinationID;
			final int nextId=getNext(editModel,editModel.surface.getByIdIncludingSubModels(destinationID),processStation);
			if (nextId<0) return String.format(Language.tr("Simulation.Creator.ExitToNoValidNextElement"),element.getId(),destinationID);
			decide.nextIds[count]=nextId;
			count++;
		}

		return decide;
	}

	@Override
	public RunModelCreatorStatus test(ModelElement element) {
		if (!(element instanceof ModelElementDecideAndTeleport)) return null;
		final ModelElementDecideAndTeleport decideElement=(ModelElementDecideAndTeleport)element;
		final ModelElementDecide.DecideMode mode=decideElement.getMode();
		if (mode!=ModelElementDecide.DecideMode.MODE_MIN_CLIENTS_NEXT_STATION && mode!=ModelElementDecide.DecideMode.MODE_MIN_CLIENTS_PROCESS_STATION && mode!=ModelElementDecide.DecideMode.MODE_SHORTEST_QUEUE_NEXT_STATION && mode!=ModelElementDecide.DecideMode.MODE_SHORTEST_QUEUE_PROCESS_STATION) return null;
		final boolean processStation=(mode==ModelElementDecide.DecideMode.MODE_MIN_CLIENTS_PROCESS_STATION || mode==ModelElementDecide.DecideMode.MODE_SHORTEST_QUEUE_PROCESS_STATION);

		final List<String> destinationStrings=decideElement.getDestinations();
		if (destinationStrings.size()==0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.NoTeleportDestination"),element.getId()),RunModelCreatorStatus.Status.TELEPORT_INVALID_DESTINATION);
		for (String destination: destinationStrings) {
			final int destinationID=RunElementTeleportSource.getDestinationID(element.getModel(),destination);
			if (destinationID<0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.InvalidTeleportDestination"),element.getId(),destination),RunModelCreatorStatus.Status.TELEPORT_INVALID_DESTINATION);
			final int nextId=getNext(element.getModel(),element.getModel().surface.getByIdIncludingSubModels(destinationID),processStation);
			if (nextId<0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.ExitToNoValidNextElement"),element.getId(),destinationID));
		}

		return RunModelCreatorStatus.ok;
	}

	@Override
	public void prepareRun(final RunModel runModel) {
		destinations=IntStream.of(destinationIDs).mapToObj(id->runModel.elements.get(id)).toArray(RunElement[]::new);
		next=IntStream.of(nextIds).mapToObj(id->runModel.elements.get(id)).toArray(RunElement[]::new);
	}

	@Override
	public RunElementTeleportDecideByStationData getData(final SimulationData simData) {
		RunElementTeleportDecideByStationData data;
		data=(RunElementTeleportDecideByStationData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementTeleportDecideByStationData(this,next.length,simData);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	@Override
	public void processArrival(SimulationData simData, RunDataClient client) {
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
	}

	@Override
	public void processLeave(SimulationData simData, RunDataClient client) {
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
				nr=(int)FastMath.round(FastMath.floor(bestIndicesUsed*DistributionRandomNumber.nextDouble()));
				break;
			default:
				nr=bestIndices[(int)FastMath.round(FastMath.floor(bestIndicesUsed*DistributionRandomNumber.nextDouble()))];
				break;
			}
		}

		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.TeleportDecideByStation"),String.format(Language.tr("Simulation.Log.TeleportDecideByStation.Info"),client.logInfo(simData),name,nr+1,destinations.length));

		/* Kunde zur nächsten Station leiten */
		StationLeaveEvent.sendToStation(simData,client,this,destinations[nr]);
	}
}

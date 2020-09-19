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
import simulator.editmodel.EditModel;
import simulator.events.StationLeaveEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementSub;
import ui.modeleditor.elements.ModelElementTeleportDestination;
import ui.modeleditor.elements.ModelElementTeleportSource;

/**
 * Äquivalent zu <code>ModelElementTeleportSource</code>
 * @author Alexander Herzog
 * @see ModelElementTeleportSource
 */
public class RunElementTeleportSource extends RunElement {
	private String destinationString;
	private int destinationID;
	private RunElement destination;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementTeleportSource(final ModelElementTeleportSource element) {
		super(element,buildName(element,Language.tr("Simulation.Element.TeleportSource.Name")));
	}

	/**
	 * Sucht das Teleport-Ziel-Element mit dem angegebenen Namen
	 * @param editModel	Editormodell das alle Elemente enthält
	 * @param name	Name des Teleport-Ziel-Elements
	 * @return	Teleport-Ziel-Element Objekt oder <code>null</code>, wenn kein Teleport-Ziel mit dem angegebenen Namen gefunden wurde
	 */
	public static ModelElementTeleportDestination getDestination(final EditModel editModel, final String name) {
		if (name==null || name.trim().isEmpty()) return null;

		for (ModelElement e1 : editModel.surface.getElements()) {
			if (e1 instanceof ModelElementTeleportDestination && e1.getName().equals(name)) return (ModelElementTeleportDestination)e1;
			if (e1 instanceof ModelElementSub) {
				for (ModelElement e2 : ((ModelElementSub)e1).getSubSurface().getElements()) {
					if (e2 instanceof ModelElementTeleportDestination && e2.getName().equals(name)) return (ModelElementTeleportDestination)e2;
				}
			}
		}
		return null;
	}

	/**
	 * Sucht das Teleport-Ziel-Element mit dem angegebenen Namen und liefert seine ID
	 * @param editModel	Editormodell das alle Elemente enthält
	 * @param name	Name des Teleport-Ziel-Elements
	 * @return	ID des Teleport-Ziel-Element Objekts oder -1, wenn kein Teleport-Ziel mit dem angegebenen Namen gefunden wurde
	 */
	public static int getDestinationID(final EditModel editModel, final String name) {
		final ModelElementTeleportDestination destination=getDestination(editModel,name);
		if (destination==null) return -1;
		return destination.getId();
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementTeleportSource)) return null;

		final ModelElementTeleportSource sourceElement=(ModelElementTeleportSource)element;
		final RunElementTeleportSource source=new RunElementTeleportSource(sourceElement);

		source.destinationString=sourceElement.getDestination();
		if (source.destinationString.trim().isEmpty()) return String.format(Language.tr("Simulation.Creator.NoTeleportDestination"),element.getId());
		source.destinationID=getDestinationID(editModel,source.destinationString);
		if (source.destinationID<0) return String.format(Language.tr("Simulation.Creator.InvalidTeleportDestination"),element.getId(),source.destinationString);

		return source;
	}

	@Override
	public RunModelCreatorStatus test(ModelElement element) {
		if (!(element instanceof ModelElementTeleportSource)) return null;

		final ModelElementTeleportSource sourceElement=(ModelElementTeleportSource)element;

		final String destinationString=sourceElement.getDestination();
		if (destinationString.trim().isEmpty()) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.NoTeleportDestination"),element.getId()),RunModelCreatorStatus.Status.TELEPORT_INVALID_DESTINATION);
		final int destinationID=getDestinationID(element.getModel(),destinationString);
		if (destinationID<0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.InvalidTeleportDestination"),element.getId(),destinationString),RunModelCreatorStatus.Status.TELEPORT_INVALID_DESTINATION);

		return RunModelCreatorStatus.ok;
	}

	@Override
	public void prepareRun(final RunModel runModel) {
		destination=runModel.elements.get(destinationID);
	}

	@Override
	public void processArrival(SimulationData simData, RunDataClient client) {
		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.Teleport"),String.format(Language.tr("Simulation.Log.Teleport.Info"),client.logInfo(simData),name,destinationString));

		/* Kunde zur nächsten Station leiten */
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
	}

	@Override
	public void processLeave(SimulationData simData, RunDataClient client) {
		StationLeaveEvent.sendToStation(simData,client,this,destination);
	}
}

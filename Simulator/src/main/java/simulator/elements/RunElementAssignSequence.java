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
import simulator.coreelements.RunElementPassThrough;
import simulator.editmodel.EditModel;
import simulator.events.StationLeaveEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementAssignSequence;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementAssignSequence</code>
 * @author Alexander Herzog
 * @see ModelElementAssignSequence
 */
public class RunElementAssignSequence extends RunElementPassThrough {
	/**
	 * Name des zuzuweisenden Fertigunsplans
	 */
	private String sequenceName;

	/**
	 * Index des zuzuweisenden Fertigunsplans
	 */
	private int sequenceIndex;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementAssignSequence(final ModelElementAssignSequence element) {
		super(element,buildName(element,Language.tr("Simulation.Element.AssignSequence.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementAssignSequence)) return null;
		final ModelElementAssignSequence assignElement=(ModelElementAssignSequence)element;
		final RunElementAssignSequence assign=new RunElementAssignSequence(assignElement);

		/* Auslaufende Kanten */
		final String edgeError=assign.buildEdgeOut(assignElement);
		if (edgeError!=null) return edgeError;

		/* Fertigungsplan */
		assign.sequenceName=assignElement.getSequence().trim();
		if (assign.sequenceName.isEmpty()) return String.format(Language.tr("Simulation.Creator.NoSequenceSelected"),element.getId());
		assign.sequenceIndex=runModel.getSequenceNr(assign.sequenceName);
		if (assign.sequenceIndex<0) return String.format(Language.tr("Simulation.Creator.SelectedSequenceDoesNotExist"),assign.sequenceName,element.getId());

		return assign;
	}

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementAssignSequence)) return null;
		final ModelElementAssignSequence assignElement=(ModelElementAssignSequence)element;

		/* Auslaufende Kanten */
		final RunModelCreatorStatus edgeError=testEdgeOut(assignElement);
		if (edgeError!=null) return edgeError;

		/* Fertigungsplan */
		final String planName=assignElement.getSequence().trim();
		if (planName.isEmpty()) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.NoSequenceSelected"),element.getId()),RunModelCreatorStatus.Status.NO_SEQUENCE);

		return RunModelCreatorStatus.ok;
	}

	@Override
	public void processArrival(final SimulationData simData, final RunDataClient client) {
		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.AssignSequence"),String.format(Language.tr("Simulation.Log.AssignSequence.Info"),client.hashCode(),sequenceName,name));

		/* Fertigungsplan zuweisen */
		client.sequenceNr=sequenceIndex;
		client.sequenceStep=0;

		/* Kunde zur nächsten Station leiten */
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
	}
}

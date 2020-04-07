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
import simulator.builder.RunModelCreator;
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElement;
import simulator.editmodel.EditModel;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.elements.ModelElementReference;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementReference</code>
 * @author Alexander Herzog
 * @see ModelElementReference
 */
public class RunElementReference extends RunElement {
	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementReference(final ModelElementReference element) {
		super(element,buildName(element,Language.tr("Simulation.Element.Reference.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementReference)) return null;
		final ModelElementReference referenceElement=(ModelElementReference)element;

		if (referenceElement.getReferenceElement()==null) return String.format(Language.tr("Simulation.Creator.ReferenceMissing"),element.getId());
		final ModelElementBox realElement=referenceElement.getReferenceElementCopy();
		if (realElement==null) return String.format(Language.tr("Simulation.Creator.ReferenceCopyNotMatching"),element.getId(),referenceElement.getReferenceElement().getId());

		final RunModelCreator creator=new RunModelCreator(editModel,runModel,testOnly);
		return creator.buildRunElement(realElement,parent);
	}

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementReference)) return null;
		final ModelElementReference referenceElement=(ModelElementReference)element;

		if (referenceElement.getReferenceElement()==null) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.ReferenceMissing"),element.getId()));

		return RunModelCreatorStatus.ok;
	}

	@Override
	public void processArrival(final SimulationData simData, final RunDataClient client) {
		/* Sollte, wenn kein Fehler aufgetreten ist, nie aufgerufen werden. Referenz-Elemente sollten nie Teil des Simulationsmodells sein. */

		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.Reference"),String.format(Language.tr("Simulation.Log.ReferenceElementUsageError.Info"),client.logInfo(simData),name));

		/* Abbruch */
		simData.eventManager.deleteAllEvents();
		simData.runData.stopp=true;
	}

	@Override
	public void processLeave(final SimulationData simData, final RunDataClient client) {
		/* Sollte, wenn kein Fehler aufgetreten ist, nie aufgerufen werden. Referenz-Elemente sollten nie Teil des Simulationsmodells sein. */

		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.Reference"),String.format(Language.tr("Simulation.Log.ReferenceElementUsageError.Info"),client.logInfo(simData),name));

		/* Abbruch */
		simData.eventManager.deleteAllEvents();
		simData.runData.stopp=true;
	}
}
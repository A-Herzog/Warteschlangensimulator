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
import simulator.events.InteractiveCheckboxClickedEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementInteractiveCheckbox;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementInteractiveCheckbox</code>
 * @author Alexander Herzog
 * @see ModelElementInteractiveCheckbox
 */
public class RunElementInteractiveCheckbox extends RunElement {
	private int variableIndex;
	private double valueChecked;
	private double valueUnchecked;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementInteractiveCheckbox(final ModelElementInteractiveCheckbox element) {
		super(element,buildName(element,Language.tr("Simulation.Element.InteractiveCheckbox.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementInteractiveCheckbox)) return null;
		final ModelElementInteractiveCheckbox checkboxElement=(ModelElementInteractiveCheckbox)element;
		final RunElementInteractiveCheckbox checkbox=new RunElementInteractiveCheckbox(checkboxElement);

		/* Variable */
		if (checkboxElement.getVariable().trim().isEmpty()) return String.format(Language.tr("Simulation.Creator.CheckboxNoVariable"),checkboxElement.getId());
		int index=-1;
		for (int j=0;j<runModel.variableNames.length;j++) if (runModel.variableNames[j].equalsIgnoreCase(checkboxElement.getVariable())) {index=j; break;}
		if (index<0) return String.format(Language.tr("Simulation.Creator.SetInternalError"),element.getId());
		checkbox.variableIndex=index;

		/* Bereich */
		checkbox.valueChecked=checkboxElement.getValueChecked();
		checkbox.valueUnchecked=checkboxElement.getValueUnchecked();

		return checkbox;
	}

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementInteractiveCheckbox)) return null;
		final ModelElementInteractiveCheckbox checkboxElement=(ModelElementInteractiveCheckbox)element;

		/* Variable */
		if (checkboxElement.getVariable().trim().isEmpty()) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.CheckboxNoVariable"),checkboxElement.getId()));

		return RunModelCreatorStatus.ok;
	}

	@Override
	public void processArrival(final SimulationData simData, final RunDataClient client) {
		/* Wird nie aufgerufen: Analog-Wert-Elemente haben keine einlaufenden Kanten bzw. führen keine Verarbeitung von Kunden durch. */
	}

	@Override
	public void processLeave(final SimulationData simData, final RunDataClient client) {
		/* Wird nie aufgerufen: Analog-Wert-Elemente haben keine einlaufenden Kanten bzw. führen keine Verarbeitung von Kunden durch. */
	}

	/**
	 * Wird indirekt von {@link #clicked(SimulationData)} über das
	 * {@link InteractiveCheckboxClickedEvent}-Ereignis ausgelöst.
	 * @param simData	Simulationsdatenobjekt
	 */
	public void triggered(final SimulationData simData) {
		boolean oldState=getBoxChecked(simData);
		if (oldState) {
			simData.runData.variableValues[variableIndex]=valueUnchecked;
		} else {
			simData.runData.variableValues[variableIndex]=valueChecked;
		}
		simData.runData.fireStateChangeNotify(simData);
	}

	/**
	 * Wird von {@link ModelElementInteractiveCheckbox} aufgerufen, wenn die
	 * Checkbox angeklickt wird. Es wird dann ein Event generiert, welches
	 * wiederum {@link #triggered(SimulationData)} aufruft.
	 * @param simData	Simulationsdatenobjekt
	 */
	public void clicked(final SimulationData simData) {
		final InteractiveCheckboxClickedEvent event=(InteractiveCheckboxClickedEvent)simData.getEvent(InteractiveCheckboxClickedEvent.class);
		event.init(simData.currentTime);
		event.interactiveCheckbox=this;
		simData.eventManager.addEvent(event);
	}

	/**
	 * Liefert den Status der Checkbox.
	 * @param simData	Simulationsdatenobjekt
	 * @return	Liefert <code>true</code>, wenn die Checkbox markiert ist.
	 */
	public boolean getBoxChecked(final SimulationData simData) {
		return Math.abs(simData.runData.variableValues[variableIndex]-valueChecked)<10E-10;
	}
}
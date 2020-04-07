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

import org.apache.commons.math3.util.FastMath;

import language.Language;
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElement;
import simulator.editmodel.EditModel;
import simulator.events.InteractiveRadiobuttonClickedEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementInteractiveRadiobutton;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementInteractiveRadiobutton</code>
 * @author Alexander Herzog
 * @see ModelElementInteractiveRadiobutton
 */
public class RunElementInteractiveRadiobutton extends RunElement {
	private int variableIndex;
	private double valueChecked;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementInteractiveRadiobutton(final ModelElementInteractiveRadiobutton element) {
		super(element,buildName(element,Language.tr("Simulation.Element.InteractiveRadiobutton.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementInteractiveRadiobutton)) return null;
		final ModelElementInteractiveRadiobutton radiobuttonElement=(ModelElementInteractiveRadiobutton)element;
		final RunElementInteractiveRadiobutton radiobutton=new RunElementInteractiveRadiobutton(radiobuttonElement);

		/* Variable */
		if (radiobuttonElement.getVariable().trim().isEmpty()) return String.format(Language.tr("Simulation.Creator.RadiobuttonNoVariable"),radiobuttonElement.getId());
		int index=-1;
		for (int j=0;j<runModel.variableNames.length;j++) if (runModel.variableNames[j].equalsIgnoreCase(radiobuttonElement.getVariable())) {index=j; break;}
		if (index<0) return String.format(Language.tr("Simulation.Creator.SetInternalError"),element.getId());
		radiobutton.variableIndex=index;

		/* Bereich */
		radiobutton.valueChecked=radiobuttonElement.getValueChecked();

		return radiobutton;
	}

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementInteractiveRadiobutton)) return null;
		final ModelElementInteractiveRadiobutton radiobuttonElement=(ModelElementInteractiveRadiobutton)element;

		/* Variable */
		if (radiobuttonElement.getVariable().trim().isEmpty()) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.RadiobuttonNoVariable"),radiobuttonElement.getId()));

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
	 * {@link InteractiveRadiobuttonClickedEvent}-Ereignis ausgelöst.
	 * @param simData	Simulationsdatenobjekt
	 */
	public void triggered(final SimulationData simData) {
		simData.runData.variableValues[variableIndex]=valueChecked;
		simData.runData.fireStateChangeNotify(simData);
	}

	/**
	 * Wird von {@link ModelElementInteractiveRadiobutton} aufgerufen, wenn das
	 * Radiobutton angeklickt wird. Es wird dann ein Event generiert, welches
	 * wiederum {@link #triggered(SimulationData)} aufruft.
	 * @param simData	Simulationsdatenobjekt
	 */
	public void clicked(final SimulationData simData) {
		final InteractiveRadiobuttonClickedEvent event=(InteractiveRadiobuttonClickedEvent)simData.getEvent(InteractiveRadiobuttonClickedEvent.class);
		event.init(simData.currentTime);
		event.interactiveRadiobutton=this;
		simData.eventManager.addEvent(event);
	}

	/**
	 * Liefert den Status des Radiobuttons.
	 * @param simData	Simulationsdatenobjekt
	 * @return	Liefert <code>true</code>, wenn das Radiobutton markiert ist.
	 */
	public boolean getBoxChecked(final SimulationData simData) {
		return FastMath.abs(simData.runData.variableValues[variableIndex]-valueChecked)<10E-10;
	}
}
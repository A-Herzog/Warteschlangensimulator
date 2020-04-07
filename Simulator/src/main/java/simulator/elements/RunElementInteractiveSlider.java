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
import mathtools.NumberTools;
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElement;
import simulator.editmodel.EditModel;
import simulator.events.InteractiveSliderClickedEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementInteractiveSlider;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementInteractiveSlider</code>
 * @author Alexander Herzog
 * @see ModelElementInteractiveSlider
 */
public class RunElementInteractiveSlider extends RunElement {
	/** Index der Variable, auf die der Slider wirkt, in der Liste aller Variablen {@link RunModel#variableNames} */
	public int variableIndex;

	private double minValue;
	private double maxValue;
	private double step;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementInteractiveSlider(final ModelElementInteractiveSlider element) {
		super(element,buildName(element,Language.tr("Simulation.Element.InteractiveSlider.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementInteractiveSlider)) return null;
		final ModelElementInteractiveSlider sliderElement=(ModelElementInteractiveSlider)element;
		final RunElementInteractiveSlider slider=new RunElementInteractiveSlider(sliderElement);

		/* Variable */
		if (sliderElement.getVariable().trim().isEmpty()) return String.format(Language.tr("Simulation.Creator.SliderNoVariable"),sliderElement.getId());
		int index=-1;
		for (int j=0;j<runModel.variableNames.length;j++) if (runModel.variableNames[j].equalsIgnoreCase(sliderElement.getVariable())) {index=j; break;}
		if (index<0) return String.format(Language.tr("Simulation.Creator.SetInternalError"),element.getId());
		slider.variableIndex=index;

		/* Bereich */
		slider.minValue=sliderElement.getMinValue();
		slider.maxValue=sliderElement.getMaxValue();
		if (slider.minValue>=slider.maxValue) return String.format(Language.tr("Simulation.Creator.SliderMinMaxError"),sliderElement.getId(),NumberTools.formatNumber(slider.minValue),NumberTools.formatNumber(slider.maxValue));

		/* Schrittweite */
		slider.step=sliderElement.getStep();
		if (slider.step<=0) return String.format(Language.tr("Simulation.Creator.SliderStepWideError"),sliderElement.getId(),NumberTools.formatNumber(slider.step));

		return slider;
	}

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementInteractiveSlider)) return null;
		final ModelElementInteractiveSlider sliderElement=(ModelElementInteractiveSlider)element;

		/* Variable */
		if (sliderElement.getVariable().trim().isEmpty()) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.SliderNoVariable"),sliderElement.getId()));

		/* Bereich */
		if (sliderElement.getMinValue()>=sliderElement.getMaxValue()) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.SliderMinMaxError"),sliderElement.getId(),NumberTools.formatNumber(sliderElement.getMinValue()),NumberTools.formatNumber(sliderElement.getMaxValue())),RunModelCreatorStatus.Status.SLIDER_MAX_LOWER_THAN_MIN);

		/* Schrittweite */
		if (sliderElement.getStep()<=0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.SliderStepWideError"),sliderElement.getId(),NumberTools.formatNumber(sliderElement.getStep())),RunModelCreatorStatus.Status.SLIDER_STEP_LESS_OR_EQUAL_0);

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
	 * Wird indirekt von {@link #clicked(SimulationData, double)} über das
	 * {@link InteractiveSliderClickedEvent}-Ereignis ausgelöst.
	 * @param simData	Simulationsdatenobjekt
	 * @param percent	Stelle auf die innerhalb des Sliders geklickt wurde (Wert zwischen 0 und 1)
	 */
	public void triggered(final SimulationData simData, final double percent) {
		/* Prozentwert umrechnen auf Variablenwert */
		double value=percent*(maxValue-minValue);
		value=minValue+Math.round(value/step)*step;
		if (value<minValue) value=minValue;
		if (value>maxValue) value=maxValue;

		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.InteractiveSlider"),String.format(Language.tr("Simulation.Log.InteractiveButton.Info"),name,simData.runModel.variableNames[variableIndex],NumberTools.formatNumber(value)));

		/* Aktion auslösen */
		simData.runData.variableValues[variableIndex]=value;
		simData.runData.fireStateChangeNotify(simData);
	}

	/**
	 * Wird von {@link ModelElementInteractiveSlider} aufgerufen, wenn der
	 * Slider angeklickt wird. Es wird dann ein Event generiert, welches
	 * wiederum {@link #triggered(SimulationData, double)} aufruft.
	 * @param simData	Simulationsdatenobjekt
	 * @param percent	Stelle auf die innerhalb des Sliders geklickt wurde (Wert zwischen 0 und 1)
	 */
	public void clicked(final SimulationData simData, final double percent) {
		final InteractiveSliderClickedEvent event=(InteractiveSliderClickedEvent)simData.getEvent(InteractiveSliderClickedEvent.class);
		event.init(simData.currentTime);
		event.interactiveSlider=this;
		event.percent=percent;
		simData.eventManager.addEvent(event);
	}
}
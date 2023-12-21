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
import simulator.coreelements.RunElementAnalogProcessing;
import simulator.editmodel.EditModel;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementAnalogValue;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementAnalogValue</code>
 * @author Alexander Herzog
 * @see ModelElementAnalogValue
 */
public class RunElementAnalogValue extends RunElementAnalogProcessing {
	/** Minimalwert, den der analoge Wert annehmen kann */
	private double valueMin;
	/** Soll der Minimalwert {@link #valueMin} verwendet werden (oder kann der reale Wert beliebig klein werden)? */
	private boolean valueMinUse;
	/** Maximalwert, den der analoge Wert annehmen kann */
	private double valueMax;
	/** Soll der Maximalwert {@link #valueMax} verwendet werden (oder kann der reale Wert beliebig groß werden)? */
	private boolean valueMaxUse;
	/** Anfängliche Änderungsrate (bezogen auf die Zeiteinheit Sekunde) */
	private double initialRate;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementAnalogValue(final ModelElementAnalogValue element) {
		super(element,buildName(element,Language.tr("Simulation.Element.AnalogValue.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementAnalogValue)) return null;

		final ModelElementAnalogValue analogValueElement=(ModelElementAnalogValue)element;
		final RunElementAnalogValue analogValue=new RunElementAnalogValue(analogValueElement);

		analogValue.initialValue=analogValueElement.getInitialValue();

		analogValue.valueMin=analogValueElement.getValueMin();
		analogValue.valueMinUse=analogValueElement.isValueMinUse();
		analogValue.valueMax=analogValueElement.getValueMax();
		analogValue.valueMaxUse=analogValueElement.isValueMaxUse();
		if (analogValue.valueMinUse && analogValue.valueMin>analogValue.initialValue) return String.format(Language.tr("Simulation.Creator.AnalogInvalidMinValue"),element.getId(),NumberTools.formatNumber(analogValue.valueMin),NumberTools.formatNumber(analogValue.initialValue));
		if (analogValue.valueMaxUse && analogValue.valueMax<analogValue.initialValue) return String.format(Language.tr("Simulation.Creator.AnalogInvalidMaxValue"),element.getId(),NumberTools.formatNumber(analogValue.valueMax),NumberTools.formatNumber(analogValue.initialValue));
		if (analogValue.valueMinUse && analogValue.valueMaxUse && analogValue.valueMin>analogValue.valueMax) return String.format(Language.tr("Simulation.Creator.AnalogInvalidMinMinValue"),element.getId(),NumberTools.formatNumber(analogValue.valueMin),NumberTools.formatNumber(analogValue.valueMax));

		analogValue.initialRate=analogValueElement.getChangeRatePerSecond();

		final String analogNotifyError=analogValue.loadAnalogNotify(analogValueElement.getAnalogNotify(),runModel);
		if (analogNotifyError!=null) return analogNotifyError;

		return analogValue;
	}

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementAnalogValue)) return null;

		final ModelElementAnalogValue analogValueElement=(ModelElementAnalogValue)element;

		if (analogValueElement.isValueMinUse() && analogValueElement.getValueMin()>analogValueElement.getInitialValue()) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.AnalogInvalidMinValue"),element.getId(),NumberTools.formatNumber(analogValueElement.getValueMin()),NumberTools.formatNumber(analogValueElement.getInitialValue())),RunModelCreatorStatus.Status.ANALOG_INITIAL_LOWER_THAN_MIN);
		if (analogValueElement.isValueMaxUse() && analogValueElement.getValueMax()<analogValueElement.getInitialValue()) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.AnalogInvalidMaxValue"),element.getId(),NumberTools.formatNumber(analogValueElement.getValueMax()),NumberTools.formatNumber(analogValueElement.getInitialValue())),RunModelCreatorStatus.Status.ANALOG_INITIAL_HIGHER_THAN_MAX);
		if (analogValueElement.isValueMinUse() && analogValueElement.isValueMaxUse() && analogValueElement.getValueMin()>analogValueElement.getValueMax()) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.AnalogInvalidMaxValue"),element.getId(),NumberTools.formatNumber(analogValueElement.getValueMin()),NumberTools.formatNumber(analogValueElement.getValueMax())),RunModelCreatorStatus.Status.ANALOG_MAX_LOWER_THAN_MIN);

		final RunModelCreatorStatus analogNotifyError=testAnalogNotify(analogValueElement.getAnalogNotify(),element.getId());
		if (analogNotifyError!=null) return analogNotifyError;

		return RunModelCreatorStatus.ok;
	}

	@Override
	public RunElementAnalogValueData getData(final SimulationData simData) {
		RunElementAnalogValueData data;
		data=(RunElementAnalogValueData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementAnalogValueData(this,initialValue,initialRate,valueMin,valueMinUse,valueMax,valueMaxUse,getAnalogStatistics(simData),simData);
			simData.runData.setStationData(this,data);
		}
		return data;
	}
}

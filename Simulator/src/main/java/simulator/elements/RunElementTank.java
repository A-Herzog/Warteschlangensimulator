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

import java.util.List;

import language.Language;
import mathtools.NumberTools;
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElementAnalogProcessing;
import simulator.editmodel.EditModel;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementSub;
import ui.modeleditor.elements.ModelElementTank;

/**
 * Äquivalent zu <code>ModelElementTank</code>
 * @author Alexander Herzog
 * @see ModelElementTank
 */
public class RunElementTank extends RunElementAnalogProcessing implements SignalListener {
	/**
	 * Kapazität
	 */
	private double capacity;

	/**
	 * Ventile mit initialem maximalem Durchfluss (pro Sekunde)
	 */
	private double[] valves;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementTank(final ModelElementTank element) {
		super(element,buildName(element,Language.tr("Simulation.Element.Tank.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementTank)) return null;

		final ModelElementTank tankElement=(ModelElementTank)element;
		final RunElementTank tank=new RunElementTank(tankElement);

		tank.capacity=tankElement.getCapacity();
		if (tank.capacity<=0) return String.format(Language.tr("Simulation.Creator.AnalogInvalidCapacity"),element.getId(),NumberTools.formatNumber(tank.capacity));

		tank.initialValue=tankElement.getInitialValue();
		if (tank.initialValue<0 || tank.initialValue>tank.capacity) return String.format(Language.tr("Simulation.Creator.AnalogInvalidValueRange"),element.getId(),NumberTools.formatNumber(tank.initialValue),NumberTools.formatNumber(tank.capacity));

		final List<ModelElementTank.Valve> valves=tankElement.getValves();
		if (valves.size()==0) return String.format(Language.tr("Simulation.Creator.AnalogNoValves"),element.getId());
		tank.valves=new double[valves.size()];
		for (int i=0;i<valves.size();i++) {
			final ModelElementTank.Valve valve=valves.get(i);
			final double d=valve.getInitialValue();
			if (d<0) return String.format(Language.tr("Simulation.Creator.AnalogInvalidValveValue"),element.getId(),i+1,NumberTools.formatNumber(d));
			switch (valve.getTimeBase()) {
			case TIMEBASE_HOURS: tank.valves[i]=d/3600; break;
			case TIMEBASE_MINUTES: tank.valves[i]=d/60; break;
			case TIMEBASE_SECONDS: tank.valves[i]=d; break;
			default: tank.valves[i]=d; break;
			}
		}

		final String analogNotifyError=tank.loadAnalogNotify(tankElement.getAnalogNotify());
		if (analogNotifyError!=null) return analogNotifyError;

		return tank;
	}

	@Override
	public RunModelCreatorStatus test(ModelElement element) {
		if (!(element instanceof ModelElementTank)) return null;

		final ModelElementTank tankElement=(ModelElementTank)element;

		if (tankElement.getCapacity()<=0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.AnalogInvalidCapacity"),element.getId(),NumberTools.formatNumber(tankElement.getCapacity())),RunModelCreatorStatus.Status.TANK_CAPACITY_LESS_OR_EQUAL_0);

		if (tankElement.getInitialValue()<0 || tankElement.getInitialValue()>tankElement.getCapacity()) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.AnalogInvalidValueRange"),element.getId(),NumberTools.formatNumber(tankElement.getInitialValue()),NumberTools.formatNumber(tankElement.getCapacity())),RunModelCreatorStatus.Status.TANK_INVALID_INITIAL_VALUE);

		final List<ModelElementTank.Valve> valves=tankElement.getValves();
		if (valves.size()==0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.AnalogNoValves"),element.getId()));
		for (int i=0;i<valves.size();i++) {
			final ModelElementTank.Valve valve=valves.get(i);
			if (valve.getInitialValue()<0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.AnalogInvalidValveValue"),element.getId(),i+1,NumberTools.formatNumber(valve.getInitialValue())));
		}

		final RunModelCreatorStatus analogNotifyError=testAnalogNotify(tankElement.getAnalogNotify(),element.getId());
		if (analogNotifyError!=null) return analogNotifyError;

		return RunModelCreatorStatus.ok;
	}

	@Override
	public RunElementTankData getData(final SimulationData simData) {
		RunElementTankData data;
		data=(RunElementTankData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementTankData(this,capacity,valves,getAnalogStatistics(simData));
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	/**
	 * Liefert die Kapazität des Tanks
	 * @return	Kapazität des Tanks
	 */
	public double getCapacity() {
		return capacity;
	}

	@Override
	public void signalNotify(SimulationData simData, String signalName) {
		getData(simData).signalNotify(simData,signalName);
	}
}

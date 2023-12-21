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
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementSub;
import ui.modeleditor.elements.ModelElementTank;
import ui.modeleditor.elements.ModelElementTankSensor;
import ui.modeleditor.elements.ModelElementTankSensor.ThresholdDirection;

/**
 * Äquivalent zu {@link ModelElementTankSensor}
 * @author Alexander Herzog
 * @see ModelElementTankSensor
 */
public class RunElementTankSensor extends RunElement implements StateChangeListener {
	/** Name des auszulösenden Signals */
	private String signalName;
	/** ID des zu überwachenden Tanks */
	private int tankId;
	/** Zu überwachender Tank (übersetzt aus {@link #tankId}) */
	private RunElementTank tank;
	/** Schwellenwert */
	private double threshold;
	/** Handelt es sich bei {@link #threshold} um eine Prozentangabe (<code>true</code>) oder um einen absoluten Füllstand (<code>false</code>)? */
	private boolean thresholdIsPercent;
	/** Soll das Signal beim Unter- oder beim Überschreiten des Schwellenwerts ausgelöst werden? */
	private ThresholdDirection thresholdDirection;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementTankSensor(final ModelElementTankSensor element) {
		super(element,buildName(element,Language.tr("Simulation.Element.TankSensor.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementTankSensor)) return null;
		final ModelElementTankSensor sensorElement=(ModelElementTankSensor)element;
		final RunElementTankSensor sensor=new RunElementTankSensor(sensorElement);

		/* Name */
		if (element.getName().isEmpty()) return String.format(Language.tr("Simulation.Creator.NoName"),element.getId());
		sensor.signalName=element.getName();

		/* Tank */
		final ModelElement tank=element.getModel().surface.getByIdIncludingSubModels(sensorElement.getTankId());
		if (!(tank instanceof ModelElementTank)) return String.format(Language.tr("Simulation.Creator.SensorNoTankID"),sensorElement.getTankId());
		sensor.tankId=sensorElement.getTankId();

		/* Schwellenwert */
		if (sensorElement.getThreshold()<0) return String.format(Language.tr("Simulation.Creator.SensorThreshold"),NumberTools.formatNumber(sensorElement.getThreshold()));
		sensor.threshold=sensorElement.getThreshold();
		sensor.thresholdIsPercent=sensorElement.isThresholdIsPercent();
		sensor.thresholdDirection=sensorElement.getThresholdDirection();

		return sensor;
	}

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementTankSensor)) return null;
		final ModelElementTankSensor sensorElement=(ModelElementTankSensor)element;

		/* Name */
		if (element.getName().isEmpty()) return RunModelCreatorStatus.noName(element);

		/* Tank */
		final ModelElement tank=element.getModel().surface.getByIdIncludingSubModels(sensorElement.getTankId());
		if (!(tank instanceof ModelElementTank)) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.SensorNoTankID"),element.getId(),sensorElement.getTankId()));

		/* Schwellenwert */
		if (sensorElement.getThreshold()<0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.SensorThreshold"),element.getId(),NumberTools.formatNumber(sensorElement.getThreshold())));

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

	@Override
	public void prepareRun(final RunModel runModel) {
		tank=(RunElementTank)runModel.elements.get(tankId);
		if (thresholdIsPercent) threshold=threshold/100.0*tank.getCapacity();
	}

	@Override
	public RunElementTankSensorData getData(final SimulationData simData) {
		RunElementTankSensorData data;
		data=(RunElementTankSensorData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementTankSensorData(this,simData);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	/**
	 * Prüft, ob der Schwellenwert über- oder unterschritten wurde.
	 * @param simData	Simulationsdatenobjekt
	 * @return	Liefert <code>true</code>, wenn das Signal ausgelöst werden soll
	 */
	private boolean testTrigger(final SimulationData simData) {
		final RunElementTankSensorData data=getData(simData);
		final double lastValue=data.lastValue;
		final double value=tank.getData(simData).getValue(simData);

		boolean trigger=false;

		if (lastValue>=0 && data.lastTime>=0 && data.lastTime<=simData.currentTime) {
			switch (thresholdDirection) {
			case DIRECTION_DOWN:
				if (lastValue>=threshold && value<threshold) trigger=true;
				break;
			case DIRECTION_UP:
				if (lastValue<=threshold && value>threshold) trigger=true;
				break;
			}
		}

		data.lastValue=value;
		data.lastTime=simData.currentTime;
		return trigger;
	}

	@Override
	public boolean systemStateChangeNotify(final SimulationData simData) {
		if (testTrigger(simData)) {
			/* Logging */
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.TankSensor"),String.format(Language.tr("Simulation.Log.TankSensor.Info"),name,signalName));

			/* Signal auslösen */
			simData.runData.fireSignal(simData,signalName);

			return true;
		} else {
			return false;
		}
	}
}

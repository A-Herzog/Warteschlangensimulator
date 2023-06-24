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
import ui.modeleditor.elements.ModelElementDifferentialCounter;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementDifferentialCounter</code>
 * @author Alexander Herzog
 * @see ModelElementDifferentialCounter
 */
public class RunElementDifferentialCounter extends RunElementPassThrough {
	/** Name des Zählers */
	private String counterName;
	/** Veränderung des Zählerwertes, wenn ein Kunde diese Station passiert */
	private int change;
	/** Zusätzliche Bedingung, die für die Zählung eines Kunden erfüllt sein muss */
	private RunCounterConditionData condition;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementDifferentialCounter(final ModelElementDifferentialCounter element) {
		super(element,buildName(element,Language.tr("Simulation.Element.DifferentialCounter.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementDifferentialCounter)) return null;
		final ModelElementDifferentialCounter counterElement=(ModelElementDifferentialCounter)element;
		final RunElementDifferentialCounter counter=new RunElementDifferentialCounter(counterElement);

		/* Auslaufende Kante */
		final String edgeError=counter.buildEdgeOut(counterElement);
		if (edgeError!=null) return edgeError;

		/* Name */
		if (counterElement.getName().isEmpty()) return String.format(Language.tr("Simulation.Creator.NoName"),element.getId());
		counter.counterName=element.getName();

		/* Wert */
		counter.change=counterElement.getChange();

		/* Bedingung */
		counter.condition=new RunCounterConditionData(counterElement.getCondition());
		final String conditionError=counter.condition.test(runModel.variableNames,element.getId());
		if (conditionError!=null) return conditionError;

		return counter;
	}

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementDifferentialCounter)) return null;
		final ModelElementDifferentialCounter counterElement=(ModelElementDifferentialCounter)element;

		/* Auslaufende Kante */
		final RunModelCreatorStatus edgeError=testEdgeOut(counterElement);
		if (edgeError!=null) return edgeError;

		/* Name */
		if (counterElement.getName().isEmpty()) return RunModelCreatorStatus.noName(element);

		return RunModelCreatorStatus.ok;
	}

	@Override
	public RunElementDifferentialCounterData getData(final SimulationData simData) {
		RunElementDifferentialCounterData data;
		data=(RunElementDifferentialCounterData)(simData.runData.getStationData(this));
		if (data==null) {
			final RunCounterConditionData conditionData=new RunCounterConditionData(condition);
			conditionData.build(simData.runModel);
			data=new RunElementDifferentialCounterData(this,counterName,change,conditionData,simData.statistics.differentialCounter,simData.runData);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	@Override
	public void processArrival(final SimulationData simData, final RunDataClient client) {
		final RunElementDifferentialCounterData data=getData(simData);

		if (data.condition.isCountThisClient(simData,client)) {
			/* Zählung */
			final int value=data.count(simData.currentTime,simData.runData);

			/* Logging */
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.DifferentialCounter"),String.format(Language.tr("Simulation.Log.DifferentialCounter.Info"),client.logInfo(simData),name,counterName,value));
		}

		/* Kunde zur nächsten Station leiten */
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
	}
}

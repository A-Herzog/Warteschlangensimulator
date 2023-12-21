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
import ui.modeleditor.elements.ModelElementCounter;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementCounter</code>
 * @author Alexander Herzog
 * @see ModelElementCounter
 */
public class RunElementCounter extends RunElementPassThrough {
	/** Name des Zählers */
	private String counterName;
	/** Name der Zählergruppe zu der dieser Zähler gehört */
	private String groupName;
	/** Zusätzliche Bedingung, die für die Zählung eines Kunden erfüllt sein muss */
	private RunCounterConditionData condition;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementCounter(final ModelElementCounter element) {
		super(element,buildName(element,Language.tr("Simulation.Element.Counter.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementCounter)) return null;
		final ModelElementCounter counterElement=(ModelElementCounter)element;
		final RunElementCounter counter=new RunElementCounter(counterElement);

		/* Auslaufende Kante */
		final String edgeError=counter.buildEdgeOut(counterElement);
		if (edgeError!=null) return edgeError;

		/* Name */
		if (counterElement.getName().isEmpty()) return String.format(Language.tr("Simulation.Creator.NoName"),element.getId());
		counter.counterName=element.getName();

		/* Gruppe */
		if (counterElement.getGroupName().isEmpty()) return String.format(Language.tr("Simulation.Creator.NoGroupName"),element.getId());
		counter.groupName=counterElement.getGroupName();

		/* Bedingung */
		counter.condition=new RunCounterConditionData(counterElement.getCondition());
		final String conditionError=counter.condition.test(runModel.variableNames,element.getId());
		if (conditionError!=null) return conditionError;

		return counter;
	}

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementCounter)) return null;
		final ModelElementCounter counterElement=(ModelElementCounter)element;

		/* Auslaufende Kante */
		final RunModelCreatorStatus edgeError=testEdgeOut(counterElement);
		if (edgeError!=null) return edgeError;

		/* Name */
		if (counterElement.getName().isEmpty()) return RunModelCreatorStatus.noName(element);

		/* Gruppe */
		if (counterElement.getGroupName().isEmpty()) return RunModelCreatorStatus.noGroupName(element);

		return RunModelCreatorStatus.ok;
	}

	@Override
	public RunElementCounterData getData(final SimulationData simData) {
		RunElementCounterData data;
		data=(RunElementCounterData)(simData.runData.getStationData(this));
		if (data==null) {
			final RunCounterConditionData conditionData=new RunCounterConditionData(condition);
			conditionData.build(simData.runModel);
			data=new RunElementCounterData(this,counterName,groupName,conditionData,simData.statistics.counter,simData);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	@Override
	public void processArrival(final SimulationData simData, final RunDataClient client) {
		final RunElementCounterData data=getData(simData);

		if (data.condition.isCountThisClient(simData,client)) {
			/* Logging */
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.Counter"),String.format(Language.tr("Simulation.Log.Counter.Info"),client.logInfo(simData),name,counterName,groupName));

			/* Zählung */
			data.statistic.add();
		}

		/* Kunde zur nächsten Station leiten */
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
	}
}
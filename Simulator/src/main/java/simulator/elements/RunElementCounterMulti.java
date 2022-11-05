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

import java.util.ArrayList;
import java.util.List;

import language.Language;
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElementPassThrough;
import simulator.editmodel.EditModel;
import simulator.events.StationLeaveEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionMultiEval;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementCounterMulti;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementCounterMulti</code>
 * @author Alexander Herzog
 * @see ModelElementCounterMulti
 */
public class RunElementCounterMulti extends RunElementPassThrough {
	/** Name der Zählergruppe zu der dieser Zähler gehört */
	private String groupName;
	/** Bedingungen für die Mehrfachzähler */
	public String[] conditions;
	/** Namen der Mehrfachzähler */
	public String[] counterNames;
	/** Name des Mehrfachzählers, der verwendet werden soll, wenn keine der Bedingungen zutrifft */
	public String counterNameElse;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementCounterMulti(final ModelElementCounterMulti element) {
		super(element,buildName(element,Language.tr("Simulation.Element.CounterMulti.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementCounterMulti)) return null;
		final ModelElementCounterMulti counterElement=(ModelElementCounterMulti)element;
		final RunElementCounterMulti counter=new RunElementCounterMulti(counterElement);

		/* Auslaufende Kante */
		final String edgeError=counter.buildEdgeOut(counterElement);
		if (edgeError!=null) return edgeError;

		/* Bedingungen und Namen */
		final int size=Math.min(counterElement.getCounterNames().size(),counterElement.getConditions().size()+1);
		if (size==0) return String.format(Language.tr("Simulation.Creator.InternalErrorNoCounters"),element.getId());
		final List<String> conditions=new ArrayList<>();
		final List<String> counterNames=new ArrayList<>();
		for (int i=0;i<size-1;i++) {
			final String condition=counterElement.getConditions().get(i);
			final int error=ExpressionMultiEval.check(condition,runModel.variableNames);
			if (error>=0) return String.format(Language.tr("Simulation.Creator.InvalidCounterExpression"),element.getId(),i+1,condition,error+1);
			conditions.add(condition);
			final String counterName=counterElement.getCounterNames().get(i);
			if (counterName.trim().isEmpty()) return String.format(Language.tr("Simulation.Creator.EmptyCounterName"),element.getId(),i+1);
			counterNames.add(counterName);
		}
		counter.conditions=conditions.toArray(new String[0]);
		counter.counterNames=counterNames.toArray(new String[0]);

		final String counterName=counterElement.getCounterNames().get(size-1);
		if (counterName.trim().isEmpty()) return String.format(Language.tr("Simulation.Creator.EmptyCounterNameElse"),element.getId());
		counter.counterNameElse=counterName;

		/* Gruppe */
		if (counterElement.getGroupName().isEmpty()) return String.format(Language.tr("Simulation.Creator.NoGroupName"),element.getId());
		counter.groupName=counterElement.getGroupName();

		return counter;
	}

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementCounterMulti)) return null;
		final ModelElementCounterMulti counterElement=(ModelElementCounterMulti)element;

		/* Auslaufende Kante */
		final RunModelCreatorStatus edgeError=testEdgeOut(counterElement);
		if (edgeError!=null) return edgeError;

		/* Namen */
		final int size=Math.min(counterElement.getCounterNames().size(),counterElement.getConditions().size()+1);
		if (size==0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.InternalErrorNoCounters"),element.getId()));
		for (int i=0;i<size-1;i++) {
			final String counterName=counterElement.getCounterNames().get(i);
			if (counterName.trim().isEmpty()) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.EmptyCounterName"),element.getId(),i+1));
		}

		final String counterName=counterElement.getCounterNames().get(size-1);
		if (counterName.trim().isEmpty()) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.EmptyCounterNameElse"),element.getId()));

		/* Gruppe */
		if (counterElement.getGroupName().isEmpty()) return RunModelCreatorStatus.noGroupName(element);

		return RunModelCreatorStatus.ok;
	}

	@Override
	public RunElementCounterMultiData getData(final SimulationData simData) {
		RunElementCounterMultiData data;
		data=(RunElementCounterMultiData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementCounterMultiData(this,groupName,conditions,counterNames,counterNameElse,simData.statistics.counter,simData.runModel.variableNames);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	@Override
	public void processArrival(final SimulationData simData, final RunDataClient client) {
		final RunElementCounterMultiData data=getData(simData);

		/* Zähler bestimmen */
		final double[] variableValues=simData.runData.variableValues;
		simData.runData.setClientVariableValues(client);
		int nr=-1;
		for (int i=0;i<data.conditions.length;i++) {
			if (data.conditions[i].eval(variableValues,simData,client)) {nr=i; break;}
		}

		/* Logging */
		if (simData.loggingActive) {
			if (nr>=0) {
				log(simData,Language.tr("Simulation.Log.CounterMulti"),String.format(Language.tr("Simulation.Log.CounterMulti.Info"),client.logInfo(simData),name,counterNames[nr],groupName));
			} else {
				log(simData,Language.tr("Simulation.Log.CounterMulti"),String.format(Language.tr("Simulation.Log.CounterMulti.Info"),client.logInfo(simData),name,counterNameElse,groupName));
			}
		}

		/* Zählung */
		if (nr>=0) data.statistic[nr].add(); else data.statisticElse.add();

		/* Kunde zur nächsten Station leiten */
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
	}
}
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
import statistics.StatisticsStateTimePerformanceIndicator;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementStateStatistics;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementStateStatistics</code>
 * @author Alexander Herzog
 * @see ModelElementStateStatistics
 */
public class RunElementStateStatistics extends RunElementPassThrough {
	private String stateName;
	private String groupName;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementStateStatistics(final ModelElementStateStatistics element) {
		super(element,buildName(element,Language.tr("Simulation.Element.StateStatistics.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementStateStatistics)) return null;
		final ModelElementStateStatistics counterElement=(ModelElementStateStatistics)element;
		final RunElementStateStatistics counter=new RunElementStateStatistics(counterElement);

		/* Auslaufende Kante */
		final String edgeError=counter.buildEdgeOut(counterElement);
		if (edgeError!=null) return edgeError;

		/* Name */
		if (element.getName().isEmpty()) return String.format(Language.tr("Simulation.Creator.NoName"),element.getId());
		counter.stateName=element.getName();

		/* Gruppe */
		if (counterElement.getGroupName().isEmpty()) return String.format(Language.tr("Simulation.Creator.NoGroupName"),element.getId());
		counter.groupName=counterElement.getGroupName();

		return counter;
	}

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementStateStatistics)) return null;
		final ModelElementStateStatistics counterElement=(ModelElementStateStatistics)element;

		/* Auslaufende Kante */
		final RunModelCreatorStatus edgeError=testEdgeOut(counterElement);
		if (edgeError!=null) return edgeError;

		/* Name */
		if (element.getName().isEmpty()) return RunModelCreatorStatus.noName(element);

		/* Gruppe */
		if (counterElement.getGroupName().isEmpty()) RunModelCreatorStatus.noGroupName(element);

		return RunModelCreatorStatus.ok;
	}

	@Override
	public RunElementStateStatisticsData getData(final SimulationData simData) {
		RunElementStateStatisticsData data;
		data=(RunElementStateStatisticsData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementStateStatisticsData(this,(StatisticsStateTimePerformanceIndicator)simData.statistics.stateStatistics.get(groupName));
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	@Override
	public void processArrival(final SimulationData simData, final RunDataClient client) {
		final RunElementStateStatisticsData data=getData(simData);

		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.StateStatistics"),String.format(Language.tr("Simulation.Log.StateStatistics.Info"),client.logInfo(simData),name,stateName,groupName));

		/* Zählung */
		data.statistic.set(simData.currentTime,stateName);

		/* Kunde zur nächsten Station leiten */
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
	}
}
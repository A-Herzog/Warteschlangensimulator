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
import simulator.coreelements.RunElementPassThrough;
import simulator.editmodel.EditModel;
import simulator.events.StationLeaveEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementSub;
import ui.modeleditor.elements.ModelElementThroughput;

/**
 * Äquivalent zu <code>ModelElementThroughput</code>
 * @author Alexander Herzog
 * @see ModelElementThroughput
 */
public class RunElementThroughput extends RunElementPassThrough {
	private String throughputCounterName;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementThroughput(final ModelElementThroughput element) {
		super(element,buildName(element,Language.tr("Simulation.Element.Throughput.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementThroughput)) return null;
		final ModelElementThroughput throughputElement=(ModelElementThroughput)element;
		final RunElementThroughput throughput=new RunElementThroughput(throughputElement);

		/* Auslaufende Kante */
		final String edgeError=throughput.buildEdgeOut(throughputElement);
		if (edgeError!=null) return edgeError;

		/* Name */
		if (throughputElement.getName().isEmpty()) return String.format(Language.tr("Simulation.Creator.NoName"),element.getId());
		throughput.throughputCounterName=element.getName();

		return throughput;
	}

	@Override
	public RunModelCreatorStatus test(ModelElement element) {
		if (!(element instanceof ModelElementThroughput)) return null;
		final ModelElementThroughput throughputElement=(ModelElementThroughput)element;

		/* Auslaufende Kante */
		final RunModelCreatorStatus edgeError=testEdgeOut(throughputElement);
		if (edgeError!=null) return edgeError;

		/* Name */
		if (throughputElement.getName().isEmpty()) return RunModelCreatorStatus.noName(element);

		return RunModelCreatorStatus.ok;
	}

	@Override
	public RunElementThroughputData getData(final SimulationData simData) {
		RunElementThroughputData data;
		data=(RunElementThroughputData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementThroughputData(this,throughputCounterName,simData.statistics.throughputStatistics);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	@Override
	public void processArrival(SimulationData simData, RunDataClient client) {
		final RunElementThroughputData data=getData(simData);

		/* Zählung */
		data.countClient(simData.currentTime);

		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.Throughput"),String.format(Language.tr("Simulation.Log.Throughput.Info"),client.logInfo(simData),name,NumberTools.formatNumber(data.getValue(true),3)));


		/* Kunde zur nächsten Station leiten */
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
	}
}

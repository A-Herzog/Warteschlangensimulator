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
import ui.modeleditor.elements.ModelElementCounterBatch;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu {@link ModelElementCounterBatch}
 * @author Alexander Herzog
 * @see ModelElementCounterBatch
 */
public class RunElementCounterBatch extends RunElementPassThrough {
	/** Name des Zählers */
	private String counterName;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementCounterBatch(final ModelElementCounterBatch element) {
		super(element,buildName(element,Language.tr("Simulation.Element.CounterBatch.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementCounterBatch)) return null;
		final ModelElementCounterBatch counterElement=(ModelElementCounterBatch)element;
		final RunElementCounterBatch counter=new RunElementCounterBatch(counterElement);

		/* Auslaufende Kante */
		final String edgeError=counter.buildEdgeOut(counterElement);
		if (edgeError!=null) return edgeError;

		/* Name */
		if (counterElement.getName().isEmpty()) return String.format(Language.tr("Simulation.Creator.NoName"),element.getId());
		counter.counterName=element.getName();

		return counter;
	}

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementCounterBatch)) return null;
		final ModelElementCounterBatch counterElement=(ModelElementCounterBatch)element;

		/* Auslaufende Kante */
		final RunModelCreatorStatus edgeError=testEdgeOut(counterElement);
		if (edgeError!=null) return edgeError;

		/* Name */
		if (counterElement.getName().isEmpty()) return RunModelCreatorStatus.noName(element);

		return RunModelCreatorStatus.ok;
	}

	@Override
	public RunElementCounterBatchData getData(final SimulationData simData) {
		RunElementCounterBatchData data;
		data=(RunElementCounterBatchData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementCounterBatchData(this,counterName,simData.statistics.counterBatch);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	@Override
	public void processArrival(final SimulationData simData, final RunDataClient client) {
		final RunElementCounterBatchData data=getData(simData);

		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.CounterBatch"),String.format(Language.tr("Simulation.Log.CounterBatch.Info"),client.logInfo(simData),name,counterName));

		/* Zählung */
		data.logArrival(simData.runData.isWarmUp,simData.currentTime);

		/* Kunde zur nächsten Station leiten */
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
	}
}
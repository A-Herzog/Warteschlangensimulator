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
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElementPassThrough;
import simulator.editmodel.EditModel;
import simulator.events.StationLeaveEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementSeparate;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementSeparate</code>
 * @author Alexander Herzog
 * @see ModelElementSeparate
 */
public class RunElementSeparate extends RunElementPassThrough {
	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementSeparate(final ModelElementSeparate element) {
		super(element,buildName(element,Language.tr("Simulation.Element.Separate.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementSeparate)) return null;
		final ModelElementSeparate separateElement=(ModelElementSeparate)element;
		final RunElementSeparate separate=new RunElementSeparate(separateElement);

		/* Auslaufende Kante */
		final String edgeError=separate.buildEdgeOut(separateElement);
		if (edgeError!=null) return edgeError;

		return separate;
	}

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementSeparate)) return null;
		final ModelElementSeparate separateElement=(ModelElementSeparate)element;

		/* Auslaufende Kante */
		final RunModelCreatorStatus edgeError=testEdgeOut(separateElement);
		if (edgeError!=null) return edgeError;

		return RunModelCreatorStatus.ok;
	}

	@Override
	public void processArrival(final SimulationData simData, final RunDataClient client) {
		final List<RunDataClient> batch=client.dissolveBatch();
		if (batch==null) {
			/* Ist ein einzelner Kunde. */

			/* Logging */
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.Separate"),String.format(Language.tr("Simulation.Log.Separate.SingleClient"),client.logInfo(simData),name));

			/* Kunde zur nächsten Station leiten */
			StationLeaveEvent.addLeaveEvent(simData,client,this,0);
		} else {
			/* Batch */

			/* Kunde verlässt Station (wird sonst über die Events realisiert) */
			simData.runData.logClientLeavesStation(simData,this,null);

			/* Logging */
			if (simData.loggingActive) {
				final StringBuilder sb=new StringBuilder();
				for (RunDataClient c : batch) {
					if (sb.length()>0) sb.append(", ");
					sb.append(c.logInfo(simData));
				}
				log(simData,Language.tr("Simulation.Log.Separate"),String.format(Language.tr("Simulation.Log.Separate.Split"),client.logInfo(simData),sb.toString(),name));
			}

			final int size=batch.size();
			for (int i=0;i<size;i++) {
				final RunDataClient c=batch.get(i);

				/* Kunde betritt Station (wird sonst über die Events realisiert) */
				simData.runData.logClientEntersStation(simData,this,null);

				/* Kunde zur nächsten Station leiten */
				StationLeaveEvent.addLeaveEvent(simData,c,this,0);
			}

			/* Kunde final recyceln */
			simData.runData.clients.disposeClientWithoutStatistics(client,simData);
		}
	}
}

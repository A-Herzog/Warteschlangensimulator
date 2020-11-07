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
import simulator.coreelements.RunElement;
import simulator.editmodel.EditModel;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementDispose;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementDispose</code>
 * @author Alexander Herzog
 * @see ModelElementDispose
 */
public class RunElementDispose extends RunElement {
	/**
	 * Soll die Simulation abgebrochen werden, wenn an dieser Station ein Kunde eintrifft?
	 * @see #processArrival(SimulationData, RunDataClient)
	 */
	private boolean stoppSimulationOnClientArrival;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementDispose(final ModelElementDispose element) {
		super(element,buildName(element,Language.tr("Simulation.Element.Dispose.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementDispose)) return null;
		final ModelElementDispose disposeElement=(ModelElementDispose)element;
		final RunElementDispose dispose=new RunElementDispose(disposeElement);

		dispose.stoppSimulationOnClientArrival=disposeElement.isStoppSimulationOnClientArrival();

		return dispose;
	}

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementDispose)) return null;
		return RunModelCreatorStatus.ok;
	}

	@Override
	public void processArrival(final SimulationData simData, final RunDataClient client) {
		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.Dispose"),String.format(Language.tr("Simulation.Log.Dispose.Info"),client.logInfo(simData),name));

		/* Simulation regulär beenden */
		if (client.isLastClient) {
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.EndOfSimulation"),Language.tr("Simulation.Log.EndOfSimulation.FinalClientLeftSystem"));
			simData.doShutDown();
		}

		/* Notausgangsstation - Simulation abbrechen */
		if (stoppSimulationOnClientArrival) {
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.EndOfSimulation"),Language.tr("Simulation.Log.EndOfSimulation.ClientAtStoppStation"));
			simData.doShutDown();
		}

		/* Notify-System über Kundenausgang informieren */
		client.lastStationID=id;
		client.nextStationID=-1;
		simData.runData.fireClientMoveNotify(simData,client,false);

		/* Kunde in Statistik erfassen und Objekt recyceln */
		simData.runData.clients.disposeClient(client,simData);
	}

	@Override
	public void processLeave(final SimulationData simData, final RunDataClient client) {
		/* Wird nie aufgerufen: Dispose-Elemente haben keine auslaufenden Kanten. */
	}

	@Override
	public boolean isClientCountStation() {
		return false;
	}
}

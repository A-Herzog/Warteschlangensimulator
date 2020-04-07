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
import ui.modeleditor.elements.ModelElementSub;
import ui.modeleditor.elements.ModelElementTankFlowBySignal;

/**
 * Äquivalent zu <code>ModelElementTankFlowBySignal</code>
 * @author Alexander Herzog
 * @see ModelElementTankFlowBySignal
 */
public class RunElementTankFlowBySignal extends RunElement implements SignalListener {
	private String signalName;
	private RunElementTankFlow flowData;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementTankFlowBySignal(final ModelElementTankFlowBySignal element) {
		super(element,buildName(element,Language.tr("Simulation.Element.TankFlowBySignal.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementTankFlowBySignal)) return null;
		final ModelElementTankFlowBySignal flowElement=(ModelElementTankFlowBySignal)element;
		final RunElementTankFlowBySignal flow=new RunElementTankFlowBySignal(flowElement);

		/* Signalname */
		flow.signalName=flowElement.getSignalName();
		if (flow.signalName==null || flow.signalName.trim().isEmpty()) return String.format(Language.tr("Simulation.Creator.AnalogFlow.NoSignalName"),element.getId());

		/* Flussdaten */
		flow.flowData=new RunElementTankFlow(flowElement.getFlowData());
		final RunModelCreatorStatus flowError=flow.flowData.test(element.getId(),flowElement.getModel());
		if (flowError!=null) return flowError.message;

		return flow;
	}

	@Override
	public RunModelCreatorStatus test(ModelElement element) {
		if (!(element instanceof ModelElementTankFlowBySignal)) return null;
		final ModelElementTankFlowBySignal flowElement=(ModelElementTankFlowBySignal)element;

		/* Signalname */
		if (flowElement.getSignalName()==null || flowElement.getSignalName().trim().isEmpty()) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.AnalogFlow.NoSignalName"),element.getId()));

		/* Flussdaten */
		final RunElementTankFlow flowData=new RunElementTankFlow(flowElement.getFlowData());
		final RunModelCreatorStatus flowError=flowData.test(element.getId(),flowElement.getModel());
		if (flowError!=null) return flowError;

		return RunModelCreatorStatus.ok;
	}

	@Override
	public void processArrival(SimulationData simData, RunDataClient client) {
		/* Wird nie aufgerufen: keine ein- und auslaufenden Kanten */

	}

	@Override
	public void processLeave(SimulationData simData, RunDataClient client) {
		/* Wird nie aufgerufen: keine ein- und auslaufenden Kanten */
	}

	@Override
	public RunElementTankFlowBySignalData getData(final SimulationData simData) {
		RunElementTankFlowBySignalData data;
		data=(RunElementTankFlowBySignalData)(simData.runData.getStationData(this));
		if (data==null) {

			RunElementTank source=null;
			if (flowData.sourceID>=0) source=(RunElementTank)simData.runModel.elements.get(flowData.sourceID);
			RunElementTank destination=null;
			if (flowData.destinationID>=0) destination=(RunElementTank)simData.runModel.elements.get(flowData.destinationID);

			data=new RunElementTankFlowBySignalData(this,flowData,source,destination);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	@Override
	public void signalNotify(SimulationData simData, String signalName) {
		if (!signalName.equals(this.signalName)) return;

		/* Fluss starten */
		final RunElementTankFlowBySignalData data=getData(simData);
		final RunElementTankFlow flow=data.getFlow(simData.currentTime);
		if (flow.source!=null) flow.source.getData(simData).addOutgoingFlow(flow,simData);
		if (flow.destination!=null) flow.destination.getData(simData).addIncomingFlow(flow,simData);

		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.TankFlowBySignal"),String.format(Language.tr("Simulation.Log.TankFlowBySignal.Info"),flow.logInfo(),name));
	}
}

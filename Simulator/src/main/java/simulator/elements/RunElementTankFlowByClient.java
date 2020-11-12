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
import ui.modeleditor.elements.ModelElementSub;
import ui.modeleditor.elements.ModelElementTankFlowByClient;

/**
 * Äquivalent zu <code>ModelElementTankFlowByClient</code>
 * @author Alexander Herzog
 * @see ModelElementTankFlowByClient
 */
public class RunElementTankFlowByClient extends RunElementPassThrough {
	/** Laufzeitdaten zu dem auszulösenden Fluss */
	private RunElementTankFlow flowData;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementTankFlowByClient(final ModelElementTankFlowByClient element) {
		super(element,buildName(element,Language.tr("Simulation.Element.TankFlowByClient.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementTankFlowByClient)) return null;
		final ModelElementTankFlowByClient flowElement=(ModelElementTankFlowByClient)element;
		final RunElementTankFlowByClient flow=new RunElementTankFlowByClient(flowElement);

		/* Auslaufende Kante */
		final String edgeError=flow.buildEdgeOut(flowElement);
		if (edgeError!=null) return edgeError;

		/* Flussdaten */
		flow.flowData=new RunElementTankFlow(flowElement.getFlowData());
		final RunModelCreatorStatus flowError=flow.flowData.test(element.getId(),flowElement.getModel());
		if (flowError!=null) return flowError.message;

		return flow;
	}

	@Override
	public RunModelCreatorStatus test(ModelElement element) {
		if (!(element instanceof ModelElementTankFlowByClient)) return null;
		final ModelElementTankFlowByClient flowElement=(ModelElementTankFlowByClient)element;

		/* Auslaufende Kante */
		final RunModelCreatorStatus edgeError=testEdgeOut(flowElement);
		if (edgeError!=null) return edgeError;

		/* Flussdaten */
		final RunElementTankFlow flowData=new RunElementTankFlow(flowElement.getFlowData());
		final RunModelCreatorStatus flowError=flowData.test(element.getId(),flowElement.getModel());
		if (flowError!=null) return flowError;

		return RunModelCreatorStatus.ok;
	}

	@Override
	public RunElementTankFlowByClientData getData(final SimulationData simData) {
		RunElementTankFlowByClientData data;
		data=(RunElementTankFlowByClientData)(simData.runData.getStationData(this));
		if (data==null) {

			RunElementTank source=null;
			if (flowData.sourceID>=0) source=(RunElementTank)simData.runModel.elements.get(flowData.sourceID);
			RunElementTank destination=null;
			if (flowData.destinationID>=0) destination=(RunElementTank)simData.runModel.elements.get(flowData.destinationID);

			data=new RunElementTankFlowByClientData(this,flowData,source,destination);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	@Override
	public void processArrival(SimulationData simData, RunDataClient client) {
		/* Fluss starten */
		final RunElementTankFlowByClientData data=getData(simData);
		final RunElementTankFlow flow=data.getFlow(simData.currentTime);
		if (flow.source!=null) flow.source.getData(simData).addOutgoingFlow(flow,simData);
		if (flow.destination!=null) flow.destination.getData(simData).addIncomingFlow(flow,simData);

		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.TankFlowByClient"),String.format(Language.tr("Simulation.Log.TankFlowByClient.Info"),flow.logInfo(),client.logInfo(simData),name));

		/* Kunde zur nächsten Station leiten */
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
	}
}
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
import ui.modeleditor.elements.ModelElementAssign;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementAssign</code>
 * @author Alexander Herzog
 * @see ModelElementAssign
 */
public class RunElementAssign extends RunElementPassThrough {
	/**
	 * Index des zuzuweisenden Kundentyps
	 */
	private int clientType;

	/**
	 * Icon des zuzuweisenden Kundentyps
	 */
	private String clientIcon;

	/**
	 * Name des zuzuweisenden Kundentyps
	 */
	public String clientTypeName;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementAssign(final ModelElementAssign element) {
		super(element,buildName(element,Language.tr("Simulation.Element.Assign.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementAssign)) return null;
		final ModelElementAssign assignElement=(ModelElementAssign)element;
		final RunElementAssign assign=new RunElementAssign(assignElement);

		/* Auslaufende Kanten */
		final String edgeError=assign.buildEdgeOut(assignElement);
		if (edgeError!=null) return edgeError;

		/* Name und Icon */
		final String name=assignElement.getName();
		if (name==null || name.isEmpty()) return String.format(Language.tr("Simulation.Creator.NoAssignName"),element.getId());
		assign.clientType=runModel.getClientTypeNr(name);
		assign.clientTypeName=name;
		assign.clientIcon=editModel.clientData.getIcon(assign.clientTypeName);

		return assign;
	}

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementAssign)) return null;
		final ModelElementAssign assignElement=(ModelElementAssign)element;

		/* Auslaufende Kanten */
		final RunModelCreatorStatus edgeError=testEdgeOut(assignElement);
		if (edgeError!=null) return edgeError;

		/* Name */
		final String name=assignElement.getName();
		if (name==null || name.isEmpty()) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.NoAssignName"),element.getId()),RunModelCreatorStatus.Status.NO_NAME);

		return RunModelCreatorStatus.ok;
	}

	@Override
	public void processArrival(final SimulationData simData, final RunDataClient client) {
		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.Assign"),String.format(Language.tr("Simulation.Log.Assign.Info"),client.hashCode(),simData.runModel.clientTypes[client.type],simData.runModel.clientTypes[clientType],name));

		/* Kundentyp ändern */
		if (clientType!=client.type) {
			simData.runData.logClientLeavesStation(simData,this,null,client);
			if (parentId>=0) simData.runData.logClientLeavesStation(simData,simData.runModel.elementsFast[parentId],null,client);
			client.changeType(clientType,simData,id);
			simData.runData.logClientEntersStation(simData,this,null,client);
			if (parentId>=0) simData.runData.logClientEntersStation(simData,simData.runModel.elementsFast[parentId],null,client);
		}
		client.iconLast=client.icon;
		client.icon=clientIcon;

		/* Kunde zur nächsten Station leiten */
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
	}
}

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
import simulator.events.StationLeaveEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementSub;
import ui.modeleditor.elements.ModelElementSubConnect;
import ui.modeleditor.elements.ModelElementSubIn;
import ui.modeleditor.elements.ModelElementSubOut;

/**
 * Äquivalent zu den <code>ModelElementSubConnect</code>-Klassen
 * @author Alexander Herzog
 * @see ModelElementSubConnect
 * @see ModelElementSubIn
 * @see ModelElementSubOut
 */
public class RunElementSubConnect extends RunElement {
	/** ID der Folgestation */
	private int connectionId;
	/** Folgestation (aus {@link #connectionId} übersetzt) */
	private RunElement connection;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementSubConnect(final ModelElementSubConnect element) {
		super(element,buildName(element,Language.tr("Simulation.Element.SubConnect.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementSubConnect)) return null;

		RunElementSubConnect connect=new RunElementSubConnect((ModelElementSubConnect)element);

		if (element instanceof ModelElementSubIn) {
			/* In das Untermodell einlaufend */
			connect.connectionId=findNextId(((ModelElementSubIn)element).getEdgeOut());
		} else {
			/* aus dem Untermodell auslaufend */
			if (parent==null) return String.format(Language.tr("Simulation.Creator.NoParentElement"),element.getId());
			connect.connectionId=parent.getId();
		}
		if (connect.connectionId<0) return String.format(Language.tr("Simulation.Creator.NoEdgeOut"),element.getId());

		return connect;
	}

	@Override
	public RunModelCreatorStatus test(ModelElement element) {
		if (!(element instanceof ModelElementSubConnect)) return null;

		if (element instanceof ModelElementSubIn) {
			/* In das Untermodell einlaufend */
			if (findNextId(((ModelElementSubIn)element).getEdgeOut())<0) return RunModelCreatorStatus.noEdgeOut(element);
		} else {
			/* aus dem Untermodell auslaufend */
		}

		return RunModelCreatorStatus.ok;
	}

	@Override
	public void prepareRun(final RunModel runModel) {
		connection=runModel.elements.get(connectionId);
	}

	@Override
	public void processArrival(SimulationData simData, RunDataClient client) {
		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.SubConnect"),String.format(Language.tr("Simulation.Log.SubConnect.Info"),client.logInfo(simData),name));

		/* Weiterleiten */
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
	}

	@Override
	public void processLeave(SimulationData simData, RunDataClient client) {
		StationLeaveEvent.sendToStation(simData,client,this,connection);
	}
}
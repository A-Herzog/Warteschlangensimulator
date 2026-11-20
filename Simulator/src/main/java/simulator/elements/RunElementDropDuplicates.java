/**
 * Copyright 2026 Alexander Herzog
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
import ui.modeleditor.elements.ModelElementDropDuplicates;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu #ModelElementDropDuplicates
 * @author Alexander Herzog
 * @see ModelElementDropDuplicates
 */
public class RunElementDropDuplicates extends RunElementPassThrough {
	/**
	 * Index des Kundendatenfeldes über das der Abgleich erfolgen soll
	 */
	private int clientDataIndex;

	/**
	 * Für wie lange sollen die IDs gespeichert werden? (in Sekunden)
	 */
	private double storageTimeSeconds;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementDropDuplicates(final ModelElementDropDuplicates element) {
		super(element,buildName(element,Language.tr("Simulation.Element.DropDuplicates.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementDropDuplicates)) return null;
		final ModelElementDropDuplicates dropDuplicatesElement=(ModelElementDropDuplicates)element;
		final RunElementDropDuplicates dropDuplicates=new RunElementDropDuplicates(dropDuplicatesElement);

		/* Auslaufende Kanten */
		final String edgeError=dropDuplicates.buildEdgeOut(dropDuplicatesElement);
		if (edgeError!=null) return edgeError;

		final String edge2Error=dropDuplicates.buildEdgeOut2(dropDuplicatesElement);
		if (edge2Error!=null) return edge2Error;

		/* Index des Kundendatenfeldes über das der Abgleich erfolgen soll */
		dropDuplicates.clientDataIndex=dropDuplicatesElement.getClientDataIndex();

		/* Für wie lange sollen die IDs gespeichert werden? */
		dropDuplicates.storageTimeSeconds=dropDuplicatesElement.getStorageTime()*dropDuplicatesElement.getTimeBase().multiply;

		return dropDuplicates;
	}

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementDropDuplicates)) return null;
		final ModelElementDropDuplicates dropDuplicatesElement=(ModelElementDropDuplicates)element;

		/* Auslaufende Kante */
		final RunModelCreatorStatus edgeError=testEdgeOut(dropDuplicatesElement);
		if (edgeError!=null) return edgeError;

		final RunModelCreatorStatus edge2Error=testEdge2Out(dropDuplicatesElement.getEdgeOut2(),dropDuplicatesElement);
		if (edge2Error!=null) return edge2Error;

		return RunModelCreatorStatus.ok;
	}

	@Override
	public RunElementDropDuplicatesData getData(final SimulationData simData) {
		RunElementDropDuplicatesData data;
		data=(RunElementDropDuplicatesData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementDropDuplicatesData(this,storageTimeSeconds,simData);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	@Override
	public void processArrival(final SimulationData simData, final RunDataClient client) {
		/* Kunde zur nächsten Station leiten */
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
	}

	@Override
	public void processLeave(SimulationData simData, RunDataClient client) {
		final RunElementDropDuplicatesData data=getData(simData);

		final double value=client.getUserData(clientDataIndex);
		if (data.isDuplicate(value,simData.currentTime)) {
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.DropDuplicates.Duplicate"),String.format(Language.tr("Simulation.Log.DropDuplicates.DuplicateInfo"),client.logInfo(simData),name,clientDataIndex,NumberTools.formatNumberMax(value)));
			super.processLeaveCancel(simData,client);
		} else {
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.DropDuplicates.First"),String.format(Language.tr("Simulation.Log.DropDuplicates.FirstInfo"),client.logInfo(simData),name,clientDataIndex,NumberTools.formatNumberMax(value)));
			super.processLeave(simData,client);
		}
	}
}

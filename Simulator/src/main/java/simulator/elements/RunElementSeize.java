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
import simulator.simparser.ExpressionCalc;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementSeize;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementSeize</code>
 * @author Alexander Herzog
 * @see ModelElementSeize
 */
public class RunElementSeize extends RunElementPassThrough implements FreeResourcesListener {
	/** Formel-String zur Ermittlung der Ressourcenpriorität dieser Station */
	public String resourcePriority;
	/** Gibt an wie viele Bediener in welcher Bedienergruppe zu belegen sind */
	private int[] resources;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementSeize(final ModelElementSeize element) {
		super(element,buildName(element,Language.tr("Simulation.Element.Seize.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementSeize)) return null;
		final ModelElementSeize seizeElement=(ModelElementSeize)element;
		final RunElementSeize seize=new RunElementSeize((ModelElementSeize)element);

		/* Auslaufende Kante */
		final String edgeError=seize.buildEdgeOut(seizeElement);
		if (edgeError!=null) return edgeError;

		/* Ressourcen-Priorität */
		final int error=ExpressionCalc.check(seizeElement.getResourcePriority(),runModel.variableNames);
		if (error>=0) return String.format(Language.tr("Simulation.Creator.SeizeResourcePriority"),element.getId(),seizeElement.getResourcePriority());
		seize.resourcePriority=seizeElement.getResourcePriority();

		/* Ressourcen */
		seize.resources=runModel.resourcesTemplate.getNeededResourcesRecord(seizeElement.getNeededResources());
		if (seize.resources==null) return String.format(Language.tr("Simulation.Creator.SeizeInvalidResource"),element.getId());

		return seize;
	}

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementSeize)) return null;
		final ModelElementSeize seizeElement=(ModelElementSeize)element;

		/* Auslaufende Kante */
		final RunModelCreatorStatus edgeError=testEdgeOut(seizeElement);
		if (edgeError!=null) return edgeError;

		/* Ressourcen */
		if (seizeElement.getNeededResources().size()==0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.SeizeInvalidResource"),element.getId()));

		return RunModelCreatorStatus.ok;
	}

	@Override
	public RunElementSeizeData getData(final SimulationData simData) {
		RunElementSeizeData data;
		data=(RunElementSeizeData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementSeizeData(this,simData.runModel.variableNames);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	@Override
	public void processArrival(final SimulationData simData, RunDataClient client) {
		final RunElementSeizeData data=getData(simData);

		if (client!=null) {
			/* Kunde an Warteschlange anstellen */
			data.addClientToQueue(client,simData.currentTime,simData);
		}

		/* Warten Kunden? */
		if (data.waitingClients.size()==0) return;

		/* Gibt es freie Bediener? */
		final double additionalTime=simData.runData.resources.tryLockResources(resources,simData,id);
		if (additionalTime<0) return;
		/* additionalTime>0: Zusätzliche Rüstzeit für Bediener, interessiert hier nicht. */

		client=data.removeClientFromQueue(simData); /* daher kann client oben nicht final sein */

		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Creator.SeizeReleaseClient"),String.format(Language.tr("Simulation.Creator.SeizeReleaseClient.Info"),client.logInfo(simData),name));

		/* Bedienzeit in Statistik */
		long waitingTime=simData.currentTime-client.lastWaitingStart;
		simData.runData.logStationProcess(simData,this,client,waitingTime,0,0,waitingTime);
		client.waitingTime+=waitingTime;
		client.residenceTime+=waitingTime;

		/* Weiterleitung zu nächster Station nach Bedienzeit-Ende */
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
	}

	@Override
	public void releasedResourcesNotify(final SimulationData simData) {
		processArrival(simData,null);
	}

	@Override
	public ExpressionCalc getResourcePriority(final SimulationData simData) {
		final RunElementSeizeData data=getData(simData);
		return data.resourcePriority;
	}

	@Override
	public double getSecondaryResourcePriority(SimulationData simData) {
		return 0.0;
	}
}
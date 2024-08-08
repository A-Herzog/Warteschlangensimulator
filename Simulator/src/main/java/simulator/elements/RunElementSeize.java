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
import simulator.events.SeizeWaitingCancelEvent;
import simulator.events.StationLeaveEvent;
import simulator.events.WaitingCancelEvent;
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
public class RunElementSeize extends RunElement implements FreeResourcesListener {
	/** ID der Station an die erfolgreiche Kunden weitergeleitet werden */
	private int connectionIdSuccess;
	/** ID der Station an die Warteabbrecher weitergeleitet werden */
	private int connectionIdCancel;
	/** Station an die erfolgreiche Kunden weitergeleitet werden (Übersetzung von {@link #connectionIdSuccess}) */
	private RunElement connectionSuccess;
	/** Station an die Warteabbrecher weitergeleitet werden (Übersetzung von {@link #connectionIdSuccess}) */
	private RunElement connectionCancel;

	/** Formel-String zur Ermittlung der Ressourcenpriorität dieser Station */
	public String resourcePriority;
	/** Gibt an wie viele Bediener in welcher Bedienergruppe zu belegen sind */
	private int[] resources;

	/** Timeout in MS bevor ein wartender Kunde aufgibt und die Station durch den Ausgang {@link #connectionCancel} verlässt. */
	private long timeOutMS;

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

		/* Auslaufende Kanten */
		seize.connectionIdSuccess=findNextId(seizeElement.getEdgeOutSuccess());
		if (seize.connectionIdSuccess<0) return String.format(Language.tr("Simulation.Creator.NoEdgeOut"),element.getId());
		if (seizeElement.getEdgeOutCancel()==null && seizeElement.getTimeOut()>=0) {
			seize.connectionIdCancel=-1;
			seize.timeOutMS=-1;
		} else {
			seize.connectionIdCancel=findNextId(seizeElement.getEdgeOutCancel());
			seize.timeOutMS=Math.round(seizeElement.getTimeOut()*runModel.scaleToSimTime);
		}

		/* Ressourcen-Priorität */
		final int error=ExpressionCalc.check(seizeElement.getResourcePriority(),runModel.variableNames,runModel.modelUserFunctions);
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

		/* Auslaufende Kanten */
		if (findNextId(seizeElement.getEdgeOutSuccess())<0) return RunModelCreatorStatus.noEdgeOut(element);

		/* Ressourcen */
		if (seizeElement.getNeededResources().size()==0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.SeizeInvalidResource"),element.getId()));

		return RunModelCreatorStatus.ok;
	}

	@Override
	public void prepareRun(final RunModel runModel) {
		connectionSuccess=runModel.elements.get(connectionIdSuccess);
		if (connectionIdCancel>=0) connectionCancel=runModel.elements.get(connectionIdCancel);
	}

	@Override
	public RunElementSeizeData getData(final SimulationData simData) {
		RunElementSeizeData data;
		data=(RunElementSeizeData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementSeizeData(this,simData.runModel.variableNames,simData);
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

			/* Abbruch-Ereignis anlegen */
			if (timeOutMS>=0) {
				client.lastWaitingTimeTolerance=timeOutMS*simData.runModel.scaleToSeconds;
				final SeizeWaitingCancelEvent event=(SeizeWaitingCancelEvent)simData.getEvent(SeizeWaitingCancelEvent.class);
				event.init(simData.currentTime+timeOutMS);
				event.station=this;
				event.client=client;
				simData.eventManager.addEvent(event);
				data.waitingCancelEvents.put(client,event);
			}
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
		client.addStationTime(id,waitingTime,0,0,waitingTime);

		/* Ausgang für "Erfolg" wählen */
		client.lastQueueSuccess=true;

		/* Abbruch-Ereignis löschen */
		if (timeOutMS>=0) {
			final SeizeWaitingCancelEvent event=data.waitingCancelEvents.remove(client);
			if (event!=null) {
				simData.eventManager.deleteEvent(event,simData);
			}
		}

		/* Weiterleitung zu nächster Station nach Bedienzeit-Ende */
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
	}

	@Override
	public void processLeave(SimulationData simData, RunDataClient client) {
		StationLeaveEvent.sendToStation(simData,client,this,client.lastQueueSuccess?connectionSuccess:connectionCancel);
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

	@Override
	public RunElement getNext() {
		return connectionSuccess;
	}

	/**
	 * Wird von {@link WaitingCancelEvent} aufgerufen, wenn die Wartezeittoleranz
	 * eines Kunden erschöpft ist.
	 * @param simData	Simulationsdatenobjekt
	 * @param client	Kunde, der das Warten aufgibt
	 */
	public void processWaitingCancel(final SimulationData simData, final RunDataClient client) {
		final RunElementSeizeData data=getData(simData);

		/* Kunden aus Warteschlange entfernen */
		data.removeClientFromQueueForCancelation(simData,client);

		/* Auch Abbruchereignis aus der Liste entfernen */
		data.waitingCancelEvents.remove(client);

		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.ProcessCancelation"),String.format(Language.tr("Simulation.Log.ProcessCancelation.Info"),client.logInfo(simData),name));

		/* Bedienzeit in Statistik */
		long waitingTime=simData.currentTime-client.lastWaitingStart;
		simData.runData.logStationProcess(simData,this,client,waitingTime,0,0,waitingTime);
		client.addStationTime(id,waitingTime,0,0,waitingTime);

		/* Ausgang für "Abbruch" wählen */
		client.lastQueueSuccess=false;

		/* Weiter zur nächsten Station */
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
	}
}
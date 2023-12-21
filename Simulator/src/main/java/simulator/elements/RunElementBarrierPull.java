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
import simulator.events.ReleaseRecheckEvent;
import simulator.events.StationLeaveEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementBarrierPull;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementBarrierPull</code>
 * @author Alexander Herzog
 * @see ModelElementBarrierPull
 */
public class RunElementBarrierPull extends RunElementPassThrough implements StateChangeListener, PickUpQueue {
	/**
	 * Maximale Anzahl an Kunden im Segment
	 */
	private String maxClients;

	/**
	 * ID des Elements bis zu dem die Anzahl an Kunden überwacht werden soll
	 */
	private int nextControlledID;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementBarrierPull(final ModelElementBarrierPull element) {
		super(element,buildName(element,Language.tr("Simulation.Element.BarrierPull.Name")));
	}

	/**
	 * Liefert die ID einer Station basierend auf ihrem Namen.
	 * @param nextName	Name der Station
	 * @param editModel	Editor-Modell
	 * @return	Liefert im Erfolgsfall die ID und -1, wenn keine Station mit dem Namen gefunden wurde
	 */
	private int getNextID(final String nextName, final EditModel editModel) {
		for (ModelElement element: editModel.surface.getElements()) {
			if (element.getName().equals(nextName)) return element.getId();
			if (element instanceof ModelElementSub) for (ModelElement element2: ((ModelElementSub)element).getSubSurface().getElements()) {
				if (element2.getName().equals(nextName)) return element2.getId();
			}
		}
		return -1;
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementBarrierPull)) return null;
		final ModelElementBarrierPull barrierElement=(ModelElementBarrierPull)element;
		final RunElementBarrierPull barrier=new RunElementBarrierPull(barrierElement);

		/* Auslaufende Kante */
		final String edgeError=barrier.buildEdgeOut(barrierElement);
		if (edgeError!=null) return edgeError;

		/* Freigaberegeln */
		final String nextName=barrierElement.getNextName();
		if (nextName.trim().isEmpty()) return String.format(Language.tr("Simulation.Creator.BarrierPull.NoNext"),element.getId());

		final int error=ExpressionCalc.check(barrierElement.getNextMax(),runModel.variableNames);
		if (error>=0) return String.format(Language.tr("Simulation.Creator.BarrierPull.InvalidNextMax"),element.getId(),barrierElement.getNextMax(),error+1);
		barrier.maxClients=barrierElement.getNextMax();
		barrier.nextControlledID=getNextID(nextName,editModel);
		if (barrier.nextControlledID<0) return String.format(Language.tr("Simulation.Creator.BarrierPull.InvalidNext"),element.getId(),nextName);

		return barrier;
	}

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementBarrierPull)) return null;
		final ModelElementBarrierPull barrierElement=(ModelElementBarrierPull)element;

		/* Auslaufende Kante */
		final RunModelCreatorStatus edgeError=testEdgeOut(barrierElement);
		if (edgeError!=null) return edgeError;

		/* Freigaberegeln */
		final String nextName=barrierElement.getNextName();
		if (nextName.trim().isEmpty()) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.BarrierPull.NoNext"),element.getId()));
		final int nextID=getNextID(nextName,element.getModel());
		if (nextID<0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.BarrierPull.InvalidNext"),element.getId(),nextName));

		return RunModelCreatorStatus.ok;
	}

	@Override
	public RunElementBarrierPullData getData(final SimulationData simData) {
		RunElementBarrierPullData data;
		data=(RunElementBarrierPullData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementBarrierPullData(this,nextControlledID,maxClients,simData.runModel.variableNames,simData);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	@Override
	public void processArrival(final SimulationData simData, final RunDataClient client) {
		final RunElementBarrierPullData data=getData(simData);

		data.queueLockedForPickUp=true;
		try {
			/* Kunden in Warteschlange einreihen */
			data.waitingClients.add(client);
			client.lastWaitingStart=simData.currentTime;

			/* Kunden an Station in Statistik */
			simData.runData.logClientEntersStationQueue(simData,this,data,client);

			/* System über Status-Änderung benachrichtigen */
			simData.runData.fireStateChangeNotify(simData);
		} finally {
			data.queueLockedForPickUp=false;
		}
	}

	@Override
	public RunDataClient getClient(final SimulationData simData) {
		final RunElementBarrierPullData data=getData(simData);
		if (data.queueLockedForPickUp) return null;
		if (data.waitingClients.size()==0) return null;

		final RunDataClient client=data.waitingClients.remove(0);
		final long waitingTime=simData.currentTime-client.lastWaitingStart;
		/* Nein, da Kunde an der Station ja nicht bedient wurde: simData.runData.logStationProcess(simData,this,waitingTime,0,0); */
		client.addStationTime(id,waitingTime,0,0,waitingTime);
		simData.runData.logClientLeavesStationQueue(simData,this,data,client);

		return client;
	}

	/**
	 * Gibt einen wartenden Kunden frei.
	 * @param simData	Simulationsdaten
	 * @param data	Thread-lokales Datenobjekt zu der Station
	 */
	private void releaseClient(final SimulationData simData, final RunElementBarrierPullData data) {
		/* Kunde aus Warteschlange entfernen und weiterleiten */
		RunDataClient client=data.waitingClients.remove(0);
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
		StationLeaveEvent.announceClient(simData,client,getNext());

		/* Bedienzeit in Statistik */
		final long waitingTime=simData.currentTime-client.lastWaitingStart;
		simData.runData.logStationProcess(simData,this,client,waitingTime,0,0,waitingTime);
		client.addStationTime(id,waitingTime,0,0,waitingTime);

		/* Zeit der aktuellen Freigabe festhalten */
		data.lastSendTime=simData.currentTime;

		/* Kunden an Station in Statistik */
		simData.runData.logClientLeavesStationQueue(simData,this,data,client);

		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.ClientReleasedByPull"),String.format(Language.tr("Simulation.Log.ClientReleasedByPull.Info"),client.logInfo(simData),name));
	}

	@Override
	public boolean systemStateChangeNotify(final SimulationData simData) {
		final RunElementBarrierPullData data=getData(simData);

		/* Wartet hier überhaupt irgendwer? */
		if (data.waitingClients.size()==0) return false;

		/* Haben wir gerade einen Kunden losgeschickt und ist dieser evtl. noch nicht an der Zielstation? */
		if (data.lastSendTime==simData.currentTime) {
			if (simData.runData.stopp) return false;
			final ReleaseRecheckEvent event=(ReleaseRecheckEvent)simData.getEvent(ReleaseRecheckEvent.class);
			event.init(simData.currentTime+1);
			event.station=this;
			simData.eventManager.addEvent(event);
			return false;
		}

		/* Dürfen wir einen Kunden freigeben? */
		if (!data.releaseOK(simData)) return false;

		/* Kunden freigeben */
		releaseClient(simData,data);

		return true;
	}
}
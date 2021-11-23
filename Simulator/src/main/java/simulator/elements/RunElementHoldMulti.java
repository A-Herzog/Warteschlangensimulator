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

import java.util.ArrayList;
import java.util.List;

import language.Language;
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElement;
import simulator.editmodel.EditModel;
import simulator.events.StationLeaveEvent;
import simulator.events.SystemChangeEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionMultiEval;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementEdge;
import ui.modeleditor.elements.ModelElementHoldMulti;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementHoldMulti</code>
 * @author Alexander Herzog
 * @see ModelElementHoldMulti
 */
public class RunElementHoldMulti extends RunElement implements StateChangeListener, PickUpQueue {
	/** IDs der über die auslaufenden Kanten erreichbaren Folgestationen */
	private List<Integer> connectionIds;
	/** Über die auslaufenden Kanten erreichbaren Folgestationen (aus {@link #connectionIds} abgeleitet) */
	private RunElement[] connections;
	/** Bedingungen pro auslaufender Kante */
	private String[] conditions;
	/** Regelmäßige Prüfung der Bedingung? */
	private boolean useTimedChecks;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementHoldMulti(final ModelElementHoldMulti element) {
		super(element,buildName(element,Language.tr("Simulation.Element.HoldMulti.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementHoldMulti)) return null;

		final ModelElementHoldMulti holdMultiElement=(ModelElementHoldMulti)element;
		final RunElementHoldMulti holdMulti=new RunElementHoldMulti(holdMultiElement);

		holdMulti.connectionIds=new ArrayList<>();
		ModelElementEdge[] edges=holdMultiElement.getEdgesOut();
		holdMulti.conditions=new String[edges.length];

		for (int i=0;i<edges.length;i++) {
			final ModelElementEdge edge=edges[i];
			final int id=findNextId(edge);
			if (id<0) return String.format(Language.tr("Simulation.Creator.EdgeToNowhere"),element.getId(),edge.getId());
			holdMulti.connectionIds.add(id);

			String condition=holdMultiElement.getConditions().get(edge.getId());
			final int error=ExpressionMultiEval.check(condition,runModel.variableNames);
			if (error>=0) return String.format(Language.tr("Simulation.Creator.HoldMultiCondition"),i+1,condition,element.getId(),error+1);
			holdMulti.conditions[i]=condition;
		}

		if (holdMulti.connectionIds.size()==0) return String.format(Language.tr("Simulation.Creator.NoEdgeOut"),element.getId());

		/* Zeitabhängige Checks */
		holdMulti.useTimedChecks=holdMultiElement.isUseTimedChecks();

		return holdMulti;
	}

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementHoldMulti)) return null;

		ModelElementEdge[] edges=((ModelElementHoldMulti)element).getEdgesOut();
		for (int i=0;i<edges.length;i++) {
			final ModelElementEdge edge=edges[i];
			final int id=findNextId(edge);
			if (id<0) return RunModelCreatorStatus.edgeToNowhere(element,edge);
		}

		if (edges.length==0) return RunModelCreatorStatus.noEdgeOut(element);

		return RunModelCreatorStatus.ok;
	}

	@Override
	public void prepareRun(final RunModel runModel) {
		connections=new RunElement[connectionIds.size()];
		for (int i=0;i<connectionIds.size();i++) connections[i]=runModel.elements.get(connectionIds.get(i));
	}

	@Override
	public RunElementHoldMultiData getData(final SimulationData simData) {
		RunElementHoldMultiData data;
		data=(RunElementHoldMultiData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementHoldMultiData(this,conditions,simData.runModel.variableNames);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	@Override
	public void processArrival(SimulationData simData, RunDataClient client) {
		final RunElementHoldMultiData data=getData(simData);

		data.queueLockedForPickUp=true;
		try {
			/* Kunden in Warteschlange einreihen */
			data.waitingClients.add(client);
			client.lastWaitingStart=simData.currentTime;

			/* Kunden an Station in Statistik */
			simData.runData.logClientEntersStationQueue(simData,this,data,client);

			/* System über Status-Änderung benachrichtigen */
			SystemChangeEvent.triggerEvent(simData,1);

			/* Interesse an zeitabhängigen Prüfungen anmelden */
			if (useTimedChecks) simData.runData.requestTimedChecks(simData,this);
		} finally {
			data.queueLockedForPickUp=false;
		}
	}

	@Override
	public boolean interestedInChangeNotifiesAtTheMoment(final SimulationData simData) {
		final RunElementHoldMultiData data=getData(simData);
		return data.waitingClients.size()>0;
	}

	@Override
	public boolean systemStateChangeNotify(final SimulationData simData) {
		final RunElementHoldMultiData data=getData(simData);

		/* Warten überhaupt Kunden? */
		if (data.waitingClients.size()==0) return false;

		/* Letzte Freigabe zum selben Zeitschritt? */
		if (data.lastRelease==simData.currentTime) return false;

		data.queueLockedForPickUp=true;
		try {
			/* Ist eine Bedingung erfüllt? */
			simData.runData.setClientVariableValues(null);
			final double[] variableValues=simData.runData.variableValues;
			int nr=-1;
			for (int i=0;i<data.conditions.length;i++) if (data.conditions[i].eval(variableValues,simData,null)) {
				nr=i;
				break;
			}
			if (nr<0) return false;

			/* Kunde aus Warteschlange entfernen und weiterleiten */
			final RunDataClient client=data.waitingClients.get(0);
			data.waitingClients.remove(0);
			StationLeaveEvent.addLeaveEvent(simData,client,this,0);
			StationLeaveEvent.announceClient(simData,client,connections[nr]);
			data.lastRelease=simData.currentTime;

			/* Wartezeit in Statistik */
			final long waitingTime=simData.currentTime-client.lastWaitingStart;
			simData.runData.logStationProcess(simData,this,client,waitingTime,0,0,waitingTime);
			client.addStationTime(id,waitingTime,0,0,waitingTime);

			/* Kunden an Station in Statistik */
			simData.runData.logClientLeavesStationQueue(simData,this,data,client);

			/* Logging */
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.HoldMulti"),String.format(Language.tr("Simulation.Log.HoldMulti.InfoRelease"),client.logInfo(simData),name,nr+1,conditions.length,conditions[nr]));

			/* Speichern, wo der Kunde hingeleitet werden soll */
			client.stationInformationInt=nr;

			/* Warten weitere Kunden? - Wenn ja in einer ms ein weiterer Check, ob die Bedingung noch erfüllt ist. */
			/* -> wird bereits durch "return true;" vom Aufrufer erledigt. */

			return true;
		} finally {
			data.queueLockedForPickUp=false;
		}
	}

	@Override
	public RunDataClient getClient(final SimulationData simData) {
		final RunElementHoldMultiData data=getData(simData);
		if (data.queueLockedForPickUp) return null;
		if (data.waitingClients.size()==0) return null;

		final RunDataClient client=data.waitingClients.remove(0);
		final long waitingTime=simData.currentTime-client.lastWaitingStart;
		/* Nein, da Kunde an der Station ja nicht bedient wurde: simData.runData.logStationProcess(simData,this,waitingTime,0,0); */
		client.addStationTime(id,waitingTime,0,0,waitingTime);
		simData.runData.logClientLeavesStationQueue(simData,this,data,client);

		return client;
	}

	@Override
	public void processLeave(SimulationData simData, RunDataClient client) {
		/* Zielstation bestimmen */
		int nr=client.stationInformationInt;

		StationLeaveEvent.unannounceClient(simData,client,connections[nr]);

		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.HoldMulti"),String.format(Language.tr("Simulation.Log.HoldMulti.InfoLeave"),client.logInfo(simData),name,nr+1,connections.length));

		/* Kunde zur nächsten Station leiten */
		StationLeaveEvent.sendToStation(simData,client,this,connections[nr]);
	}
}

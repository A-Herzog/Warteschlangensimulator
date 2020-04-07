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
import simulator.events.SystemChangeEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionMultiEval;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementHold;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementHold</code>
 * @author Alexander Herzog
 * @see ModelElementHold
 */
public class RunElementHold extends RunElementPassThrough implements StateChangeListener, PickUpQueue {
	private String condition;
	private boolean useClientBasedCheck;
	private boolean useTimedChecks;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementHold(final ModelElementHold element) {
		super(element,buildName(element,Language.tr("Simulation.Element.Hold.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementHold)) return null;
		final ModelElementHold holdElement=(ModelElementHold)element;
		final RunElementHold hold=new RunElementHold(holdElement);

		/* Auslaufende Kante */
		final String edgeError=hold.buildEdgeOut(holdElement);
		if (edgeError!=null) return edgeError;

		/* Bedingung */
		final String condition=holdElement.getCondition();
		if (condition==null || condition.trim().isEmpty()) {
			hold.condition=null;
		} else {
			final int error=ExpressionMultiEval.check(condition,runModel.variableNames);
			if (error>=0) return String.format(Language.tr("Simulation.Creator.HoldCondition"),condition,element.getId(),error+1);
			hold.condition=condition;
		}
		hold.useClientBasedCheck=holdElement.isClientBasedCheck();

		/* Zeitabhängige Checks */
		hold.useTimedChecks=holdElement.isUseTimedChecks();

		return hold;
	}

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementHold)) return null;
		final ModelElementHold holdElement=(ModelElementHold)element;

		/* Auslaufende Kante */
		final RunModelCreatorStatus edgeError=testEdgeOut(holdElement);
		if (edgeError!=null) return edgeError;

		return RunModelCreatorStatus.ok;
	}

	@Override
	public RunElementHoldData getData(final SimulationData simData) {
		RunElementHoldData data;
		data=(RunElementHoldData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementHoldData(this,condition,simData.runModel.variableNames);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	@Override
	public void processArrival(final SimulationData simData, final RunDataClient client) {
		final RunElementHoldData data=getData(simData);

		data.queueLockedForPickUp=true;
		try {
			/* Kunden in Warteschlange einreihen */
			data.waitingClients.add(client);
			client.lastWaitingStart=simData.currentTime;

			/* Kunden an Station in Statistik */
			simData.runData.logClientEntersStationQueue(simData,this,data,client);

			/* System über Status-Änderung benachrichtigen */
			simData.runData.fireStateChangeNotify(simData);

			/* Interesse an zeitabhängigen Prüfungen anmelden */
			if (useTimedChecks) simData.runData.requestTimedChecks(simData,this);
		} finally {
			data.queueLockedForPickUp=false;
		}
	}

	@Override
	public boolean interestedInChangeNotifiesAtTheMoment(final SimulationData simData) {
		final RunElementHoldData data=getData(simData);
		return data.waitingClients.size()>0;
	}

	@Override
	public boolean systemStateChangeNotify(final SimulationData simData) {
		final RunElementHoldData data=getData(simData);

		/* Warten überhaupt Kunden? */
		if (data.waitingClients.size()==0) return false;

		data.queueLockedForPickUp=true;
		try {
			int removed=0;

			if (data.lastRelease<simData.currentTime) {
				final int size=data.waitingClients.size();
				final double[] variableValues=simData.runData.variableValues;
				for (int index=0;index<size;index++) {
					final RunDataClient client=data.waitingClients.get(index);

					/* Ist die Bedingung erfüllt? */
					final boolean conditionIsTrue;
					if (useClientBasedCheck) {
						simData.runData.setClientVariableValues(client);
						conditionIsTrue=(data.condition==null || data.condition.eval(variableValues,simData,client));
					} else {
						simData.runData.setClientVariableValues(null);
						conditionIsTrue=(data.condition==null || data.condition.eval(variableValues,simData,null));
					}
					if (!conditionIsTrue) {
						if (useClientBasedCheck) continue; else break;
					}

					/* Kunde aus Warteschlange entfernen und weiterleiten */
					data.waitingClients.remove(index);
					StationLeaveEvent.addLeaveEvent(simData,client,this,0);
					StationLeaveEvent.unannounceClient(simData,client,getNext());
					data.lastRelease=simData.currentTime;

					/* Wartezeit in Statistik */
					final long waitingTime=simData.currentTime-client.lastWaitingStart;
					simData.runData.logStationProcess(simData,this,client,waitingTime,0,0,waitingTime);
					client.waitingTime+=waitingTime;
					client.residenceTime+=waitingTime;

					/* Kunden an Station in Statistik */
					simData.runData.logClientLeavesStationQueue(simData,this,data,client);

					/* Logging */
					if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.Hold"),String.format(Language.tr("Simulation.Log.Hold.Info"),client.logInfo(simData),name));

					/* Erfolg - alles weitere später */
					removed++;
					break;
				}
			} else {
				SystemChangeEvent.triggerEvent(simData,1);
			}

			/* Warten weitere Kunden? - Wenn ja in einer ms ein weiterer Check, ob die Bedingung noch erfüllt ist. */
			/* -> wird bereits durch "return true;" vom Aufrufer erledigt. */

			return removed>0;
		} finally {
			data.queueLockedForPickUp=false;
		}
	}

	@Override
	public RunDataClient getClient(final SimulationData simData) {
		final RunElementHoldData data=getData(simData);
		if (data.queueLockedForPickUp) return null;
		if (data.waitingClients.size()==0) return null;

		final RunDataClient client=data.waitingClients.remove(0);
		final long waitingTime=simData.currentTime-client.lastWaitingStart;
		/* Nein, da Kunde an der Station ja nicht bedient wurde: simData.runData.logStationProcess(simData,this,waitingTime,0,0); */
		client.waitingTime+=waitingTime;
		client.residenceTime+=waitingTime;
		simData.runData.logClientLeavesStationQueue(simData,this,data,client);

		return client;
	}
}

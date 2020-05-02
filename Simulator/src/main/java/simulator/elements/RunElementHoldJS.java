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
import scripting.java.DynamicFactory;
import scripting.java.DynamicRunner;
import scripting.java.DynamicStatus;
import scripting.js.JSRunSimulationData;
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElementPassThrough;
import simulator.editmodel.EditModel;
import simulator.events.StationLeaveEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementHoldJS;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementHoldJS</code>
 * @author Alexander Herzog
 * @see ModelElementHoldJS
 */
public class RunElementHoldJS extends RunElementPassThrough implements StateChangeListener, PickUpQueue {
	private String script;
	private ModelElementHoldJS.ScriptMode mode;
	private boolean useTimedChecks;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementHoldJS(final ModelElementHoldJS element) {
		super(element,buildName(element,Language.tr("Simulation.Element.HoldJS.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementHoldJS)) return null;
		final ModelElementHoldJS holdElement=(ModelElementHoldJS)element;
		final RunElementHoldJS hold=new RunElementHoldJS(holdElement);

		/* Auslaufende Kante */
		final String edgeError=hold.buildEdgeOut(holdElement);
		if (edgeError!=null) return edgeError;

		/* Skript */
		hold.script=holdElement.getScript();

		hold.mode=holdElement.getMode();

		if (hold.mode==ModelElementHoldJS.ScriptMode.Java && !testOnly) {
			final String scriptError=DynamicFactory.getFactory().test(hold.script,true);
			if (scriptError!=null) return scriptError;
		}

		/* Zeitabhängige Checks */
		hold.useTimedChecks=holdElement.isUseTimedChecks();

		return hold;
	}

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementHoldJS)) return null;
		final ModelElementHoldJS holdElement=(ModelElementHoldJS)element;

		/* Auslaufende Kante */
		final RunModelCreatorStatus edgeError=testEdgeOut(holdElement);
		if (edgeError!=null) return edgeError;

		return RunModelCreatorStatus.ok;
	}

	@Override
	public RunElementHoldJSData getData(final SimulationData simData) {
		RunElementHoldJSData data;
		data=(RunElementHoldJSData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementHoldJSData(this,script,mode,simData);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	@Override
	public void processArrival(final SimulationData simData, final RunDataClient client) {
		final RunElementHoldJSData data=getData(simData);

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
			simData.runData.requestTimedChecks(simData,this);
		} finally {
			data.queueLockedForPickUp=false;
		}
	}

	private void releaseClient(final SimulationData simData, final RunElementHoldJSData data, final int clientIndex) {
		/* Kunde aus Warteschlange entfernen und weiterleiten */
		final RunDataClient client=data.waitingClients.remove(clientIndex);
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
		StationLeaveEvent.announceClient(simData,client,getNext());
		data.lastRelease=simData.currentTime;

		/* Wartezeit in Statistik */
		final long waitingTime=simData.currentTime-client.lastWaitingStart;
		simData.runData.logStationProcess(simData,this,client,waitingTime,0,0,waitingTime);
		client.waitingTime+=waitingTime;
		client.residenceTime+=waitingTime;

		/* Kunden an Station in Statistik */
		if (useTimedChecks) simData.runData.logClientLeavesStationQueue(simData,this,data,client);

		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.Hold"),String.format(Language.tr("Simulation.Log.Hold.Info"),client.logInfo(simData),name));
	}

	@Override
	public boolean interestedInChangeNotifiesAtTheMoment(final SimulationData simData) {
		final RunElementHoldJSData data=getData(simData);
		return data.waitingClients.size()>0;
	}

	@Override
	public boolean systemStateChangeNotify(final SimulationData simData) {
		final RunElementHoldJSData data=getData(simData);

		/* Warten überhaupt Kunden? */
		if (data.waitingClients.size()==0) return false;

		data.queueLockedForPickUp=true;
		try {
			int removed=0;

			if (data.lastRelease<simData.currentTime) {
				if (data.jsRunner!=null) {
					/* JS-Mode */
					JSRunSimulationData jsRunner=data.jsRunner;
					jsRunner.setSimulationData(simData,data.waitingClients);
					final String result=jsRunner.runCompiled();
					if (!jsRunner.getLastSuccess() && simData.runModel.canelSimulationOnScriptError) {
						simData.doEmergencyShutDown(result);
						return false;
					}
					logJS(simData,data.script,result); /* Immer ausführen; Entscheidung Erfassen ja/nein erfolgt in logJS */
					final boolean[] release=jsRunner.getSimulationDataClients(); /* Achtung: Release kann länger sein als das Kunden-Array ... */
					final int size=data.waitingClients.size(); /* ... daher nehmen wir die Länge des Kunden-Arrays als Referenz. */
					for (int i=0;i<size;i++) if (release[i]) {
						releaseClient(simData,data,i-removed);
						removed++;
					}
				}
				if (data.javaRunner!=null) {
					/* Java-Mode */
					final DynamicRunner javaRunner=data.javaRunner;
					javaRunner.parameter.clients.setClients(data.waitingClients);
					final Object result=javaRunner.run();
					if (javaRunner.getStatus()!=DynamicStatus.OK && simData.runModel.canelSimulationOnScriptError) {
						simData.doEmergencyShutDown(DynamicFactory.getLongStatusText(javaRunner));
						return false;
					}
					logJS(simData,data.script,result); /* Immer ausführen; Entscheidung Erfassen ja/nein erfolgt in logJS */
					final boolean[] release=javaRunner.parameter.clients.getSimulationData(); /* Achtung: Release kann länger sein als das Kunden-Array ... */
					final int size=data.waitingClients.size(); /* ... daher nehmen wir die Länge des Kunden-Arrays als Referenz. */
					for (int i=0;i<size;i++) if (release[i]) {
						releaseClient(simData,data,i-removed);
						removed++;
					}
				}
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
		final RunElementHoldJSData data=getData(simData);
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
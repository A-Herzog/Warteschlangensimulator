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
import simulator.simparser.ExpressionMultiEval;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementDelay;
import ui.modeleditor.elements.ModelElementHoldJS;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu {@link ModelElementHoldJS}
 * @author Alexander Herzog
 * @see ModelElementHoldJS
 */
public class RunElementHoldJS extends RunElementPassThrough implements StateChangeListener, PickUpQueue {
	/** Optionale zusätzliche Bedingung */
	private String condition;
	/** Skript auf dessen Basis die Kunden weitergeleitet werden sollen */
	private String script;
	/** Skriptsprache für {@link #script} */
	private ModelElementHoldJS.ScriptMode mode;
	/** Regelmäßige Prüfung der Bedingung? */
	private boolean useTimedChecks;
	/** Nur bei Kundenankunft prüfen? */
	private boolean onlyCheckOnArrival;
	/** Art wie die Verzögerung für die Kundenstatistik gezählt werden soll */
	private ModelElementDelay.DelayType delayType;
	/** Bereits in {@link #build(EditModel, RunModel, ModelElement, ModelElementSub, boolean)} vorbereiteter (optionale) Java-Runner */
	private DynamicRunner jRunner;

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

		/* Optionale Bedingung */
		String condition=holdElement.getCondition();
		if (condition==null || condition.trim().isEmpty()) {
			hold.condition=null;
		} else {
			final int error=ExpressionMultiEval.check(condition,runModel.variableNames);
			if (error>=0) return String.format(Language.tr("Simulation.Creator.HoldJSCondition"),condition,element.getId(),error+1);
			hold.condition=condition;
		}

		/* Skript */
		hold.script=holdElement.getScript();

		hold.mode=holdElement.getMode();

		if (hold.mode==ModelElementHoldJS.ScriptMode.Java && !testOnly) {
			final Object runner=DynamicFactory.getFactory().test(hold.script,runModel.javaImports,true);
			if (runner instanceof String) return String.format(Language.tr("Simulation.Creator.ScriptError"),element.getId())+"\n"+runner;
			hold.jRunner=(DynamicRunner)runner;
		}

		/* Zeitabhängige Checks */
		hold.useTimedChecks=holdElement.isUseTimedChecks();

		/* Nur bei Kundenankunft prüfen? */
		hold.onlyCheckOnArrival=holdElement.isOnlyCheckOnArrival();

		/* Art wie die Verzögerung für die Kundenstatistik gezählt werden soll */
		hold.delayType=holdElement.getDelayType();

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
			data=new RunElementHoldJSData(this,condition,script,mode,jRunner,simData);
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

			if (onlyCheckOnArrival) {
				/* Prüfung erfolgt nicht über StateChangeNotify, daher hier direkt */
				runCheck(simData,true);
			}

			/* Interesse an zeitabhängigen Prüfungen anmelden */
			if (useTimedChecks) simData.runData.requestTimedChecks(simData,this);
		} finally {
			data.queueLockedForPickUp=false;
		}
	}

	/**
	 * Gibt einen wartenden Kunden frei.
	 * @param simData	Simulationsdatenobjekt
	 * @param data	Thread-lokales Datenobjekt zu der Station
	 * @param clientIndex	Index des Kundenobjektes in {@link RunElementHoldData#waitingClients}
	 */
	private void releaseClient(final SimulationData simData, final RunElementHoldJSData data, final int clientIndex) {
		/* Kunde aus Warteschlange entfernen und weiterleiten */
		final RunDataClient client=data.waitingClients.remove(clientIndex);
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
		StationLeaveEvent.announceClient(simData,client,getNext());
		data.lastRelease=simData.currentTime;

		/* Wartezeit in Statistik */
		final long waitingTime=simData.currentTime-client.lastWaitingStart;
		switch (delayType) {
		case DELAY_TYPE_WAITING:
			simData.runData.logStationProcess(simData,this,client,waitingTime,0,0,waitingTime);
			client.addStationTime(id,waitingTime,0,0,waitingTime);
			break;
		case DELAY_TYPE_TRANSFER:
			simData.runData.logStationProcess(simData,this,client,0,waitingTime,0,waitingTime);
			client.addStationTime(id,0,waitingTime,0,waitingTime);
			break;
		case DELAY_TYPE_PROCESS:
			simData.runData.logStationProcess(simData,this,client,0,0,waitingTime,waitingTime);
			client.addStationTime(id,0,0,waitingTime,waitingTime);
			break;
		case DELAY_TYPE_NOTHING:
			/* nicht erfassen */
			break;
		}

		/* Kunden an Station in Statistik */
		simData.runData.logClientLeavesStationQueue(simData,this,data,client);

		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.Hold"),String.format(Language.tr("Simulation.Log.Hold.Info"),client.logInfo(simData),name));
	}

	@Override
	public boolean interestedInChangeNotifiesAtTheMoment(final SimulationData simData) {
		final RunElementHoldJSData data=getData(simData);
		return data.waitingClients.size()>0 && !onlyCheckOnArrival;
	}

	/**
	 * Führt die eigentliche Freigabeprüfung durch.
	 * @param simData	Simulationsdatenobjekt
	 * @param force	Freigabeprüfung auch dann ausführen, wenn zum selben Zeitschritt bereits eine Prüfung erfolgt ist?
	 * @return	Liefert <code>true</code>, wenn ein wartender Kunde freigegeben wurde
	 */
	private boolean runCheck(final SimulationData simData, final boolean force) {
		final RunElementHoldJSData data=getData(simData);

		/* Warten überhaupt Kunden? */
		if (data.waitingClients.size()==0) return false;

		/* Zusätzliche Bedingung prüfen */
		if (data.condition!=null) {
			simData.runData.setClientVariableValues(null);
			if (!data.condition.eval(simData.runData.variableValues,simData,null)) return false;
		}

		data.queueLockedForPickUp=true;
		try {
			int removed=0;

			if (data.lastRelease<simData.currentTime || force) {
				if (data.jsRunner!=null) {
					/* JS-Mode */
					JSRunSimulationData jsRunner=data.jsRunner;
					jsRunner.setSimulationData(simData,id,data.waitingClients);
					final String result=jsRunner.runCompiled();
					if (!jsRunner.getLastSuccess() && simData.runModel.cancelSimulationOnScriptError) {
						simData.doEmergencyShutDown(name+": "+result);
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
					if (javaRunner.getStatus()!=DynamicStatus.OK && simData.runModel.cancelSimulationOnScriptError) {
						simData.doEmergencyShutDown(name+": "+DynamicFactory.getLongStatusText(javaRunner));
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
				simData.runData.updateMapValuesForStatistics(simData);
			}

			/* Warten weitere Kunden? - Wenn ja in einer ms ein weiterer Check, ob die Bedingung noch erfüllt ist. */
			/* -> wird bereits durch "return true;" vom Aufrufer erledigt. */

			return removed>0;
		} finally {
			data.queueLockedForPickUp=false;
		}
	}


	@Override
	public boolean systemStateChangeNotify(final SimulationData simData) {
		if (onlyCheckOnArrival) return false;
		return runCheck(simData,false);
	}

	@Override
	public RunDataClient getClient(final SimulationData simData) {
		final RunElementHoldJSData data=getData(simData);
		if (data.queueLockedForPickUp) return null;
		if (data.waitingClients.size()==0) return null;

		final RunDataClient client=data.waitingClients.remove(0);
		final long waitingTime=simData.currentTime-client.lastWaitingStart;
		switch (delayType) {
		case DELAY_TYPE_WAITING:
			/* Nein, da Kunde an der Station ja nicht bedient wurde: simData.runData.logStationProcess(simData,this,waitingTime,0,0,waitingTime); */
			client.addStationTime(id,waitingTime,0,0,waitingTime);
			break;
		case DELAY_TYPE_TRANSFER:
			/* Nein, da Kunde an der Station ja nicht bedient wurde: simData.runData.logStationProcess(simData,this,0,waitingTime,0,waitingTime); */
			client.addStationTime(id,0,waitingTime,0,waitingTime);
			break;
		case DELAY_TYPE_PROCESS:
			/* Nein, da Kunde an der Station ja nicht bedient wurde: simData.runData.logStationProcess(simData,this,0,0,waitingTime,waitingTime); */
			client.addStationTime(id,0,0,waitingTime,waitingTime);
			break;
		case DELAY_TYPE_NOTHING:
			/* nicht erfassen */
			break;
		}

		simData.runData.logClientLeavesStationQueue(simData,this,data,client);

		return client;
	}
}
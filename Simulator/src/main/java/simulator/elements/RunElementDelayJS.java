/**
 * Copyright 2022 Alexander Herzog
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
import java.util.Map;

import language.Language;
import mathtools.TimeTools;
import parser.MathCalcError;
import scripting.java.DynamicFactory;
import scripting.java.DynamicRunner;
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElementPassThrough;
import simulator.editmodel.EditModel;
import simulator.events.StationLeaveEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementDecideJS;
import ui.modeleditor.elements.ModelElementDelay;
import ui.modeleditor.elements.ModelElementDelayJS;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu {@link ModelElementDelayJS}
 * @author Alexander Herzog
 * @see ModelElementDelayJS
 */
public class RunElementDelayJS extends RunElementPassThrough implements DelayWithClientsList {
	/** Auszuführendes Skript */
	private String script;
	/** Skriptspache für {@link #script} */
	private ModelElementDecideJS.ScriptMode mode;
	/** Bereits in {@link #build(EditModel, RunModel, ModelElement, ModelElementSub, boolean)} vorbereiteter (optionale) Java-Runner */
	private DynamicRunner jRunner;
	/** Art wie die Verzögerung für die Kundenstatistik gezählt werden soll */
	private ModelElementDelay.DelayType delayType;
	/** Kosten pro Bedienvorgang */
	private String costs;
	/** Soll eine Liste der Kunden an der Station geführt werden? */
	private boolean hasClientsList;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementDelayJS(final ModelElementDelayJS element) {
		super(element,buildName(element,Language.tr("Simulation.Element.DelayJS.Name")));
	}

	@Override
	public Object build(EditModel editModel, RunModel runModel, ModelElement element, ModelElementSub parent, boolean testOnly) {
		if (!(element instanceof ModelElementDelayJS)) return null;
		final ModelElementDelayJS delayElement=(ModelElementDelayJS)element;
		final RunElementDelayJS delay=new RunElementDelayJS(delayElement);

		/* Auslaufende Kante */
		final String edgeError=delay.buildEdgeOut(delayElement);
		if (edgeError!=null) return edgeError;

		/* Verzögerungstyp */
		delay.delayType=delayElement.getDelayType();

		/* Skript */
		delay.script=delayElement.getScript();
		delay.mode=delayElement.getMode();
		if (delay.mode==ModelElementDecideJS.ScriptMode.Java && !testOnly) {
			final Object runner=DynamicFactory.getFactory().test(delay.script,runModel.javaImports,true);
			if (runner instanceof String) return String.format(Language.tr("Simulation.Creator.ScriptError"),element.getId())+"\n"+runner;
			delay.jRunner=(DynamicRunner)runner;
		}

		/* Kosten */
		final String text=delayElement.getCosts();
		if (text==null || text.trim().isEmpty()  || text.trim().equals("0")) {
			delay.costs=null;
		} else {
			final int error=ExpressionCalc.check(text,runModel.variableNames,runModel.modelUserFunctions);
			if (error>=0) return String.format(Language.tr("Simulation.Creator.CostsErrorDelay"),text,element.getId(),error+1);
			delay.costs=text;
		}

		/* Soll eine Liste der Kunden an der Station geführt werden? */
		delay.hasClientsList=delayElement.hasClientsList();

		return delay;
	}

	@Override
	public RunModelCreatorStatus test(ModelElement element) {
		if (!(element instanceof ModelElementDelayJS)) return null;
		final ModelElementDelayJS delayElement=(ModelElementDelayJS)element;

		/* Auslaufende Kante */
		final RunModelCreatorStatus edgeError=testEdgeOut(delayElement);
		if (edgeError!=null) return edgeError;

		/* Skript */
		if (delayElement.getScript().trim().isEmpty()) {
			return RunModelCreatorStatus.noScript(element);
		}

		return RunModelCreatorStatus.ok;
	}

	@Override
	public RunElementDelayJSData getData(final SimulationData simData) {
		RunElementDelayJSData data;
		data=(RunElementDelayJSData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementDelayJSData(this,script,mode,jRunner,simData,simData.runModel.variableNames,costs,hasClientsList);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	@Override
	public void processArrival(final SimulationData simData, final RunDataClient client) {
		final RunElementDelayJSData data=getData(simData);

		/* Verzögerung bestimmen */
		final double delayTime=Math.max(0,data.getDelayTime(simData,client));
		final long delayTimeMS=(long)(delayTime*simData.runModel.scaleToSimTime+0.5);

		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.DelayJS"),String.format(Language.tr("Simulation.Log.DelayJS.Info"),client.logInfo(simData),name,TimeTools.formatExactTime(delayTime)));

		/* Erfassung der Zeit in der Statistik */
		client.lastWaitingStart=simData.currentTime;

		/* Anzahl an Kunden in Bedienung ändern */
		if (delayType==ModelElementDelay.DelayType.DELAY_TYPE_PROCESS) {
			simData.runData.logClientEntersStationProcess(simData,this,null,client);
		}

		/* Kosten in Statistik erfassen */
		if (costs!=null) {
			simData.runData.setClientVariableValues(client);
			double c=0;
			try {
				c=data.costs.calc(simData.runData.variableValues);
			} catch (MathCalcError e) {
				simData.calculationErrorStation(data.costs,this);
				c=0;
			}
			simData.runData.logStationCosts(simData,this,c);
		}

		/* Kunde zur nächsten Station leiten */
		final StationLeaveEvent event=StationLeaveEvent.addLeaveEvent(simData,client,this,delayTimeMS);

		/* Kunden und Ereignis in Liste der Kunden speichern (sofern wir so eine Liste führen) */
		if (hasClientsList) {
			final Map<RunDataClient,StationLeaveEvent> clientsList=getData(simData).clientsList;
			if (clientsList!=null) clientsList.put(client,event);
		}
	}

	@Override
	public void processLeave(SimulationData simData, RunDataClient client) {
		/* Kunde aus der Liste der Kunden an der Station entfernen (sofern wir so eine Liste führen) */
		if (hasClientsList) {
			final Map<RunDataClient,StationLeaveEvent> clientsList=getData(simData).clientsList;
			if (clientsList!=null) clientsList.remove(client);
		}

		/* Zeitdauer an der Station in der Statistik erfassen */
		final long delayTimeMS=simData.currentTime-client.lastWaitingStart;
		logDelay(simData,client,delayTimeMS);

		/* Anzahl an Kunden in Bedienung ändern */
		if (delayType==ModelElementDelay.DelayType.DELAY_TYPE_PROCESS) {
			simData.runData.logClientLeavesStationProcess(simData,this,null,client);
		}

		super.processLeave(simData,client);
	}

	/**
	 * Erfasst die Verzögerungszeit in der Statistik
	 * @param simData	Simulationsdatenobjekt
	 * @param client	Aktueller Kunde
	 * @param delayTimeMS	Verzögerungszeit in MS
	 */
	public void logDelay(final SimulationData simData, final RunDataClient client, final long delayTimeMS) {
		/* Bedienzeit in Statistik */
		switch (delayType) {
		case DELAY_TYPE_WAITING:
			client.addStationTime(id,delayTimeMS,0,0,delayTimeMS);
			break;
		case DELAY_TYPE_TRANSFER:
			client.addStationTime(id,0,delayTimeMS,0,delayTimeMS);
			break;
		case DELAY_TYPE_PROCESS:
			client.addStationTime(id,0,0,delayTimeMS,delayTimeMS);
			break;
		case DELAY_TYPE_NOTHING:
			/* nicht erfassen */
			break;
		}

		/* Verarbeitungszeit in der Statistik für die Station erfassen */
		switch (delayType) {
		case DELAY_TYPE_WAITING:
			simData.runData.logStationProcess(simData,this,client,delayTimeMS,0,0,delayTimeMS);
			break;
		case DELAY_TYPE_TRANSFER:
			simData.runData.logStationProcess(simData,this,client,0,delayTimeMS,0,delayTimeMS);
			break;
		case DELAY_TYPE_PROCESS:
			simData.runData.logStationProcess(simData,this,client,0,0,delayTimeMS,delayTimeMS);
			break;
		case DELAY_TYPE_NOTHING:
			/* nicht erfassen */
			break;
		}
	}

	/**
	 * Leere Liste
	 * @see #getClientsAtStation(SimulationData)
	 */
	private static final List<RunDataClient> emptyList=new ArrayList<>(1);

	/**
	 * Liefert die Liste der Kunden an dieser Station (sofern eine solche Liste geführt wird)
	 * @param simData	Simulationsdatenobjekt
	 * @return	Liste der Kunden (ist nie <code>null</code>, aber kann leer sein, insbesondere wenn keine solche Liste geführt wird)
	 */
	@Override
	public List<RunDataClient> getClientsAtStation(final SimulationData simData) {
		/* Führen wir eine Kundenliste? */
		if (!hasClientsList) return emptyList;
		final Map<RunDataClient,StationLeaveEvent> clientsList=getData(simData).clientsList;
		if (clientsList==null) return emptyList;

		/* Als Liste ausgeben */
		return new ArrayList<>(clientsList.keySet());
	}

	/**
	 * Gibt einen Kunden an dieser Station sofort frei.
	 * @param simData	Simulationsdatenobjekt
	 * @param client	Freizugebender Kunde
	 * @return	Liefert <code>true</code>, wenn sich der Kunde an der Station befindet und freigegeben werden konnte
	 * @see #getClientsAtStation(SimulationData)
	 */
	@Override
	public boolean releaseClientNow(final SimulationData simData, final RunDataClient client) {
		/* Führen wir eine Kundenliste? */
		if (!hasClientsList) return false;
		final Map<RunDataClient,StationLeaveEvent> clientsList=getData(simData).clientsList;
		if (clientsList==null) return false;

		/* Altes Ereignis finden und löschen */
		final StationLeaveEvent oldEvent=clientsList.get(client);
		if (oldEvent==null) return false;
		simData.eventManager.deleteEvent(oldEvent,simData);

		/* Neues Ereignis anlegen */
		final StationLeaveEvent newEvent=StationLeaveEvent.addLeaveEvent(simData,client,this,0);
		clientsList.put(client,newEvent);
		return true;
	}
}

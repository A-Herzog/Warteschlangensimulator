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
import java.util.Map;

import org.apache.commons.math3.distribution.AbstractRealDistribution;

import language.Language;
import mathtools.TimeTools;
import mathtools.distribution.tools.DistributionRandomNumber;
import mathtools.distribution.tools.DistributionTools;
import parser.MathCalcError;
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElementPassThrough;
import simulator.editmodel.EditModel;
import simulator.events.StationLeaveEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementDelay;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementDelay</code>
 * @author Alexander Herzog
 * @see ModelElementDelay
 */
public class RunElementDelay extends RunElementPassThrough implements DelayWithClientsList {
	/** Multiplikationsfaktor für {@link #distribution} oder {@link #expression} */
	private double timeBaseMultiply;
	/** Verteilung zur Kundentyp-abhängigen Bestimmung der Verzögerung */
	private AbstractRealDistribution[] distribution;
	/** Rechenausdruck zur Kundentyp-abhängigen Bestimmung der Verzögerung; wird nur ausgewertet, wenn der entsprechende {@link #distribution}-Eintrag <code>null</code> ist */
	private String[] expression;
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
	public RunElementDelay(final ModelElementDelay element) {
		super(element,buildName(element,Language.tr("Simulation.Element.Delay.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementDelay)) return null;
		final ModelElementDelay delayElement=(ModelElementDelay)element;
		final RunElementDelay delay=new RunElementDelay(delayElement);

		/* Auslaufende Kante */
		final String edgeError=delay.buildEdgeOut(delayElement);
		if (edgeError!=null) return edgeError;

		/* Zeitbasis */
		delay.timeBaseMultiply=delayElement.getTimeBase().multiply;

		/* Verzögerungstyp */
		delay.delayType=delayElement.getDelayType();

		delay.distribution=new AbstractRealDistribution[runModel.clientTypes.length];
		delay.expression=new String[runModel.clientTypes.length];
		for (int i=0;i<delay.distribution.length;i++) {
			AbstractRealDistribution dist=delayElement.getDelayTime(runModel.clientTypes[i]);
			if (dist!=null) {
				delay.distribution[i]=DistributionTools.cloneDistribution(dist);
				continue;
			}

			String expression=delayElement.getDelayExpression(runModel.clientTypes[i]);
			if (expression!=null) {
				final int error=ExpressionCalc.check(expression,runModel.variableNames,runModel.modelUserFunctions);
				if (error>=0) return String.format(Language.tr("Simulation.Creator.DelayCondition"),expression,element.getId(),error+1);
				delay.expression[i]=expression;
				continue;
			}

			dist=delayElement.getDelayTime();
			if (dist!=null) {
				delay.distribution[i]=DistributionTools.cloneDistribution(dist);
				continue;
			}

			expression=delayElement.getDelayExpression();
			if (expression!=null) {
				final int error=ExpressionCalc.check(expression,runModel.variableNames,runModel.modelUserFunctions);
				if (error>=0) return String.format(Language.tr("Simulation.Creator.DelayCondition"),expression,element.getId(),error+1);
				delay.expression[i]=expression;
				continue;
			}

			return String.format(Language.tr("Simulation.Creator.DelayNoDistributionOrCondition"),element.getId());
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
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementDelay)) return null;
		final ModelElementDelay delayElement=(ModelElementDelay)element;

		/* Auslaufende Kante */
		final RunModelCreatorStatus edgeError=testEdgeOut(delayElement);
		if (edgeError!=null) return edgeError;

		return RunModelCreatorStatus.ok;
	}

	@Override
	public RunElementDelayData getData(final SimulationData simData) {
		RunElementDelayData data;
		data=(RunElementDelayData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementDelayData(this,expression,simData.runModel.variableNames,costs,hasClientsList,simData);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	@Override
	public void processArrival(final SimulationData simData, final RunDataClient client) {
		/* Verzögerung bestimmen */
		double value;
		if (distribution[client.type]!=null) {
			value=DistributionRandomNumber.randomNonNegative(distribution[client.type]);
		} else {
			simData.runData.setClientVariableValues(client);
			try {
				value=getData(simData).expression[client.type].calc(simData.runData.variableValues,simData,client);
				if (value<0) value=0;
			} catch (MathCalcError e) {
				simData.calculationErrorStation(getData(simData).expression[client.type],this);
				value=0;
			}
		}

		final double delayTime=value*timeBaseMultiply;
		final long delayTimeMS=(long)(delayTime*simData.runModel.scaleToSimTime+0.5);

		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.Delay"),String.format(Language.tr("Simulation.Log.Delay.Info"),client.logInfo(simData),name,TimeTools.formatExactTime(delayTime)));

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
				c=getData(simData).costs.calc(simData.runData.variableValues);
			} catch (MathCalcError e) {
				simData.calculationErrorStation(getData(simData).costs,this);
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
	private void logDelay(final SimulationData simData, final RunDataClient client, final long delayTimeMS) {
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
			simData.runData.logStationProcess(simData,this,client,0,0,0,delayTimeMS); /* nicht erfassen */
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

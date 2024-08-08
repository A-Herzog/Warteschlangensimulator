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
import parser.MathCalcError;
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElementPassThrough;
import simulator.editmodel.EditModel;
import simulator.events.ReleaseRecheckEvent;
import simulator.events.StationLeaveEvent;
import simulator.events.SystemChangeEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import simulator.simparser.ExpressionMultiEval;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementDelay;
import ui.modeleditor.elements.ModelElementHold;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementHold</code>
 * @author Alexander Herzog
 * @see ModelElementHold
 */
public class RunElementHold extends RunElementPassThrough implements StateChangeListener, PickUpQueue {
	/** Bedingung, die für eine Weitergabe der Kunden erfüllt sein muss */
	private String condition;
	/** Prioritäts-Rechenausdrücke */
	private String[] priority;
	/** Individuelle kundenbasierende Prüfung? */
	private boolean useClientBasedCheck;
	/** Regelmäßige Prüfung der Bedingung? */
	private boolean useTimedChecks;
	/** Automatische Freigabe nach bestimmter Wartezeit? (Werte &le;0 für aus) */
	private String maxWaitingTime;
	/** Art wie die Verzögerung für die Kundenstatistik gezählt werden soll */
	private ModelElementDelay.DelayType delayType;

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
			final int error=ExpressionMultiEval.check(condition,runModel.variableNames,runModel.modelUserFunctions);
			if (error>=0) return String.format(Language.tr("Simulation.Creator.HoldCondition"),condition,element.getId(),error+1);
			hold.condition=condition;
		}

		/* Prioritäten */
		hold.priority=new String[runModel.clientTypes.length];
		for (int i=0;i<hold.priority.length;i++) {
			String priorityString=holdElement.getPriority(runModel.clientTypes[i]);
			if (priorityString==null || priorityString.trim().isEmpty()) priorityString=ModelElementHold.DEFAULT_CLIENT_PRIORITY;
			if (priorityString.equalsIgnoreCase(ModelElementHold.DEFAULT_CLIENT_PRIORITY)) {
				hold.priority[i]=null; /* Default Priorität als null vermerken */
			} else {
				final ExpressionCalc calc=new ExpressionCalc(runModel.variableNames,runModel.modelUserFunctions);
				final int error=calc.parse(priorityString);
				if (error>=0) return String.format(Language.tr("Simulation.Creator.HoldClientPriority"),element.getId(),runModel.clientTypes[i],priorityString,error+1);
				hold.priority[i]=priorityString;
			}
		}

		/* Individuelle kundenbasierende Prüfung */
		hold.useClientBasedCheck=holdElement.isClientBasedCheck();

		/* Zeitabhängige Checks */
		hold.useTimedChecks=holdElement.isUseTimedChecks();

		/* Freigabe nach bestimmter Wartezeit? */
		if (!holdElement.getMaxWaitingTime().isBlank()) {
			final String maxWaitingTime=holdElement.getMaxWaitingTime();
			final ExpressionCalc calc=new ExpressionCalc(runModel.variableNames,runModel.modelUserFunctions);
			final int error=calc.parse(maxWaitingTime);
			if (error>=0) return String.format(Language.tr("Simulation.Creator.HoldMaxWaitingTime"),maxWaitingTime,element.getId(),error+1);
			hold.maxWaitingTime=maxWaitingTime;
		} else {
			hold.maxWaitingTime=null;
		}

		/* Art wie die Verzögerung für die Kundenstatistik gezählt werden soll */
		hold.delayType=holdElement.getDelayType();

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
			data=new RunElementHoldData(this,condition,priority,maxWaitingTime,simData.runModel.variableNames,simData);
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

			/* Freigabe nach Wartezeit? */
			if (data.maxWaitingTime!=null) {
				long maxWaitingTimeMS=0;
				simData.runData.setClientVariableValues(client);
				try {
					maxWaitingTimeMS=(long)(data.maxWaitingTime.calc(simData.runData.variableValues,simData,client)*1000+0.5);
					if (maxWaitingTimeMS<0) maxWaitingTimeMS=0;
				} catch (MathCalcError e) {
					simData.calculationErrorStation(data.maxWaitingTime,this);
					maxWaitingTimeMS=0;
				}
				final ReleaseRecheckEvent event=(ReleaseRecheckEvent)simData.getEvent(ReleaseRecheckEvent.class);
				event.init(simData.currentTime+maxWaitingTimeMS);
				event.station=this;
				simData.eventManager.addEvent(event);
				client.lastMaxWaitingTime=maxWaitingTimeMS;
			}

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

	/**
	 * Gibt einen einzelnen Kunden frei.
	 * @param simData	Simulationsdatenobjekt
	 * @param data	Thread-lokales Datenobjekt zu der Station
	 * @param client	Kunde
	 * @param clientIndex	Index des Kunden in der Liste der Kunden an der Station
	 */
	private void releaseClient(final SimulationData simData, final RunElementHoldData data, final RunDataClient client, final int clientIndex) {
		/* Kunde aus Warteschlange entfernen und weiterleiten */
		data.waitingClients.remove(clientIndex);
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
		StationLeaveEvent.unannounceClient(simData,client,getNext());
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

	/**
	 * Prüft, ob einer der Kunden freigegeben werden kann.<br>
	 * Dabei wird die Priorität nicht geprüft und es ist auch bereits bekannt, dass es Kunden in der Liste gibt.
	 * @param simData	Simulationsdatenobjekt
	 * @return	Gibt <code>true</code> zurück, wenn ein Kunde freigegeben werden konnte
	 * @param data	Thread-lokales Datenobjekt zu der Station
	 * @see #systemStateChangeNotify(SimulationData)
	 */
	private boolean releaseTestFIFO(final SimulationData simData, final RunElementHoldData data) {
		final int size=data.waitingClients.size();
		final double[] variableValues=simData.runData.variableValues;
		for (int index=0;index<size;index++) {
			final RunDataClient client=data.waitingClients.get(index);

			/* Ist die Bedingung erfüllt? */
			final boolean conditionIsTrue;
			if (useClientBasedCheck) {
				simData.runData.setClientVariableValues(client.waitingTime+(simData.currentTime-client.lastWaitingStart),client.transferTime,client.processTime); /* Auch die bisherige Wartezeit an der aktuellen Station schon mitzählen. */
				conditionIsTrue=(data.condition==null || data.condition.eval(variableValues,simData,client));
			} else {
				simData.runData.setClientVariableValues(null);
				conditionIsTrue=(data.condition==null || data.condition.eval(variableValues,simData,null));
			}
			if (!conditionIsTrue) {
				if (useClientBasedCheck) continue; else break;
			}

			/* Kunde freigeben */
			releaseClient(simData,data,client,index);

			/* Warten weitere Kunden? - Wenn ja in einer ms ein weiterer Check, ob die Bedingung noch erfüllt ist. */
			/* -> wird bereits durch "return true;" vom Aufrufer erledigt. */
			return true;
		}

		return false;
	}

	/**
	 * Berechnet den Score-Wert eines Kunden.
	 * @param simData	Simulationsdatenobjekt
	 * @param holdData	Thread-lokales Datenobjekt zu der Station
	 * @param client	Kunde
	 * @return	Score-Wert des Kunden
	 */
	private double getClientScore(final SimulationData simData, final RunElementHoldData holdData, final RunDataClient client) {
		final ExpressionCalc calc=holdData.priority[client.type];
		if (calc==null) { /* = Text war "w", siehe RunElementProcessData()  */
			return (((double)simData.currentTime)-client.lastWaitingStart)*simData.runModel.scaleToSeconds;
		} else {
			simData.runData.setClientVariableValues(simData.currentTime-client.lastWaitingStart,client.transferTime,client.processTime);
			try {
				return calc.calc(simData.runData.variableValues,simData,client);
			} catch (MathCalcError e) {
				simData.calculationErrorStation(calc,this);
				return 0;
			}
		}
	}

	/**
	 * Gibt den Kunden mit der höchsten Priorität frei.<br>
	 * Dabei wird die Bedingung nicht geprüft und es ist auch bereits bekannt, dass es Kunden in der Liste gibt.
	 * @param simData	Simulationsdatenobjekt
	 * @param data	Thread-lokales Datenobjekt zu der Station
	 */
	private void releaseClientWithHighestPriority(final SimulationData simData, final RunElementHoldData data) {
		final int size=data.waitingClients.size();

		int bestIndex=0;
		RunDataClient bestClient=data.waitingClients.get(0);
		double bestPriority=getClientScore(simData,data,bestClient);

		for (int index=1;index<size;index++) {
			final RunDataClient client=data.waitingClients.get(index);
			final double priority=getClientScore(simData,data,client);
			if (priority>bestPriority) {
				bestIndex=index;
				bestClient=client;
				bestPriority=priority;
			}
		}

		releaseClient(simData,data,bestClient,bestIndex);
	}

	/**
	 * Prüft, ob einer der Kunden freigegeben werden kann.<br>
	 * Die Bedingung und die Priorität werden für jeden Kunden individuell geprüft. Es ist bereits bekannt, dass es Kunden in der Liste gibt.
	 * @param simData	Simulationsdatenobjekt
	 * @param data	Thread-lokales Datenobjekt zu der Station
	 * @return	Gibt <code>true</code> zurück, wenn ein Kunde freigegeben werden konnte
	 */
	private boolean releaseTestPriorityIndividual(final SimulationData simData, final RunElementHoldData data) {
		final int size=data.waitingClients.size();
		final double[] variableValues=simData.runData.variableValues;

		int bestIndex=-1;
		double bestPriority=0;
		RunDataClient bestClient=null;

		for (int index=0;index<size;index++) {
			final RunDataClient client=data.waitingClients.get(index);
			final double priority=getClientScore(simData,data,client);
			if (priority>bestPriority) {
				simData.runData.setClientVariableValues(client.waitingTime+(simData.currentTime-client.lastWaitingStart),client.transferTime,client.processTime); /* Auch die bisherige Wartezeit an der aktuellen Station schon mitzählen. */
				final boolean conditionIsTrue=(data.condition==null || data.condition.eval(variableValues,simData,client));
				if (conditionIsTrue) {
					bestIndex=index;
					bestClient=client;
					bestPriority=priority;
				}
			}
		}

		if (bestClient==null) {
			return false;
		} else {
			releaseClient(simData,data,bestClient,bestIndex);
			return true;
		}
	}

	/**
	 * Prüft, ob einer der Kunden freigegeben werden kann.<br>
	 * Es wird der freigebbare Kunde mit der höchsten Priorität freigegeben. Es ist bereits bekannt, dass es Kunden in der Liste gibt.
	 * @param simData	Simulationsdatenobjekt
	 * @param data	Thread-lokales Datenobjekt zu der Station
	 * @return	Gibt <code>true</code> zurück, wenn ein Kunde freigegeben werden konnte
	 * @see #systemStateChangeNotify(SimulationData)
	 */
	private boolean releaseTestPriority(final SimulationData simData, final RunElementHoldData data) {
		if (useClientBasedCheck) {
			return releaseTestPriorityIndividual(simData,data);
		} else {
			final double[] variableValues=simData.runData.variableValues;
			simData.runData.setClientVariableValues(null);
			final boolean conditionIsTrue=(data.condition==null || data.condition.eval(variableValues,simData,null));
			if (conditionIsTrue) {
				releaseClientWithHighestPriority(simData,data);
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * Prüft, ob einer oder mehrere Kunden aufgrund des Überschreitens der maximalen Wartezeit freigegeben werden sollen.
	 * @param simData	Simulationsdatenobjekt
	 * @param data	Thread-lokales Datenobjekt zu der Station
	 * @return	Gibt <code>true</code> zurück, wenn einer oder mehrere Kunden freigegeben werden konnten
	 */
	private boolean releaseByWaitigTime(final SimulationData simData, final RunElementHoldData data) {
		if (data.maxWaitingTime==null) return false;
		boolean releasedClients=false;
		final long currentTime=simData.currentTime;

		int index=0;
		while (index<data.waitingClients.size()) {
			final RunDataClient client=data.waitingClients.get(index);
			if (client.lastWaitingStart+client.lastMaxWaitingTime<=currentTime) {
				releaseClient(simData,data,client,index);
				releasedClients=true;
			} else {
				index++;
			}
		}

		return releasedClients;
	}

	@Override
	public boolean systemStateChangeNotify(final SimulationData simData) {
		final RunElementHoldData data=getData(simData);

		/* Warten überhaupt Kunden? */
		if (data.waitingClients.size()==0) return false;

		data.queueLockedForPickUp=true;
		try {
			if (data.lastRelease<simData.currentTime) {
				/* Freigabe nach Wartezeit */
				if (data.maxWaitingTime!=null) {
					if (releaseByWaitigTime(simData,data)) return true;
				}
				/* Freigabe gemäß regulärer Bedingung */
				if (data.allPriorityFIFO) {
					return releaseTestFIFO(simData,data);
				} else {
					return releaseTestPriority(simData,data);
				}
			} else {
				SystemChangeEvent.triggerEvent(simData,1);
				return false;
			}

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

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
package simulator.runmodel;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.util.FastMath;

import language.Language;
import mathtools.TimeTools;
import simulator.coreelements.RunElement;
import simulator.elements.TransporterPosition;
import simulator.events.TransporterArrivalEvent;
import simulator.simparser.ExpressionCalc;
import statistics.StatisticsTimePerformanceIndicator;

/**
 * Diese Klasse hält die Laufzeitdaten für einen konkreten Transporter vor.
 * @author Alexander Herzog
 * @see RunDataTransporters
 */
public class RunDataTransporter {
	private final RunDataTransporters list;
	private StatisticsTimePerformanceIndicator statisticUtilization;
	private StatisticsTimePerformanceIndicator statisticDownTime;

	/**
	 * Typ des Transporters
	 * @see RunDataTransporters#type
	 */
	public final int type;

	/**
	 * 0-basierender Index innerhalb der Typs
	 */
	public final int index;

	/**
	 * Icon für die Animation nach rechts (unbeladen)
	 */
	public final String iconEastEmpty;

	/**
	 * Icon für die Animation nach links (unbeladen)
	 */
	public final String iconWestEmpty;

	/**
	 * Icon für die Animation nach rechts (beladen)
	 */
	public final String iconEastLoaded;

	/**
	 * Icon für die Animation nach links (beladen)
	 */
	public final String iconWestLoaded;

	/**
	 * Kapazität an transportierbaren Kunden.
	 */
	public final int capacity;

	/**
	 * Optional Verteilung der Ladezeiten
	 */
	public final AbstractRealDistribution loadDistribution;

	private final String loadExpressionString;

	/**
	 * Optional Ausdruck zur Berechnung der Ladezeiten
	 */
	public final ExpressionCalc loadExpression;

	/**
	 * Optional Verteilung der Entladezeiten
	 */
	public final AbstractRealDistribution unloadDistribution;

	private final String unloadExpressionString;

	/**
	 * Optional Ausdruck zur Berechnung der Entladezeiten
	 */
	public final ExpressionCalc unloadExpression;

	/**
	 * Nur gültig während <code>inTransfer==true</code>. Gibt dann die Start-Station an.
	 */
	public int lastPosition;

	/**
	 * Aktuelle Position des Transporters.<br>
	 * (Ab dem Transportstart steht hier die neue Position.)
	 */
	public int position;

	/**
	 * Gibt an, ob der Transporter sich gerade bewegt.
	 */
	public boolean inTransfer;

	/**
	 * Aktuell transportierte Kunden
	 */
	public List<RunDataClient> clients;

	private final RunDataTransporterFailure[] failures;

	/**
	 * Für Pausenzeiten:
	 * Wann wurde der Transporter verfügbar (für Ausfälle nach Zeitdauer)
	 */
	public long availableStartTime;

	/**
	 * Für Pausenzeiten:
	 * Wann wird der Transporter wieder verfügbar?
	 * onlineAgainAt==0 bedeutet, dass er momentan verfügbar ist (aber evtl. gerade fährt).
	 */
	public long onlineAgainAt;

	/**
	 * Für Pausenzeiten:
	 * Bei Pausen gemäß Zeitabständen: Welcher Failure-Eintrag ist gerade aktiv?
	 */
	int currentPauseIndex;

	/**
	 * Für Pausenzeiten in Statistik:
	 * Anzahl an Transportern dieses Typs, die momentan in Pausenzeit sind
	 */
	private int inDownTime;

	/**
	 * Konstruktor der Klasse
	 * @param type	Typ des Transporters
	 * @param index	0-basierender Index des Transporters innerhalb des Typs
	 * @param capacity	Kapazität an transportierbaren Kunden
	 * @param load	Ausdruck oder Verteilung oder <code>null</code> zur Beschreibung der Ladezeiten
	 * @param unload	Ausdruck oder Verteilung oder <code>null</code> zur Beschreibung der Entladezeiten
	 * @param iconEastEmpty	Icon für die Animation nach rechts (unbeladen)
	 * @param iconWestEmpty	Icon für die Animation nach links (unbeladen)
	 * @param iconEastLoaded	Icon für die Animation nach rechts (beladen)
	 * @param iconWestLoaded	Icon für die Animation nach links (beladen)
	 * @param failures	Liste mit globalen Ausfällen für die Transporter dieses Typs (werden in diesem Objekt als Kopien abgelegt)
	 * @param list	Objekt welches die Daten aller Transporter vorhält
	 * @param variables	Liste mit den Variablennamen
	 */
	public RunDataTransporter(final int type, final int index, final int capacity, final Object load, final Object unload, final String iconEastEmpty, final String iconWestEmpty, final String iconEastLoaded, final String iconWestLoaded, final RunDataTransporterFailure[] failures, final RunDataTransporters list, final String[] variables) {
		this.list=list;
		this.iconEastEmpty=iconEastEmpty;
		this.iconWestEmpty=iconWestEmpty;
		this.iconEastLoaded=iconEastLoaded;
		this.iconWestLoaded=iconWestLoaded;
		this.type=type;
		this.index=index;
		this.capacity=capacity;
		if (load instanceof AbstractRealDistribution) {
			loadDistribution=(AbstractRealDistribution)load;
			loadExpressionString=null;
		} else {
			if (load instanceof String) {
				loadDistribution=null;
				loadExpressionString=(String)load;
			} else {
				loadDistribution=null;
				loadExpressionString=null;
			}
		}
		if (loadExpressionString==null) {
			loadExpression=null;
		} else {
			loadExpression=new ExpressionCalc(variables);
			loadExpression.parse(loadExpressionString);
		}
		if (unload instanceof AbstractRealDistribution) {
			unloadDistribution=(AbstractRealDistribution)load;
			unloadExpressionString=null;
		} else {
			if (unload instanceof String) {
				unloadDistribution=null;
				unloadExpressionString=(String)load;
			} else {
				unloadDistribution=null;
				unloadExpressionString=null;
			}
		}
		if (unloadExpressionString==null) {
			unloadExpression=null;
		} else {
			unloadExpression=new ExpressionCalc(variables);
			unloadExpression.parse(unloadExpressionString);
		}
		clients=new ArrayList<>();
		if (failures==null) {
			this.failures=new RunDataTransporterFailure[0];
		} else {
			this.failures=new RunDataTransporterFailure[failures.length];
			for (int i=0;i<failures.length;i++) {
				this.failures[i]=new RunDataTransporterFailure(failures[i],list,variables);
			}
		}
	}

	/**
	 * Erstellt eine Kopie des Objektes
	 * @param variables	Liste der globalen Variablen
	 * @param list	Liste aller Transporter zu der der kopierte Transporter gehören soll
	 * @return	Kopie des Objektes
	 */
	public RunDataTransporter clone(final String[] variables, final RunDataTransporters list) {
		Object load=null;
		if (loadDistribution!=null) load=loadDistribution;
		if (loadExpressionString!=null) load=loadExpressionString;
		Object unload=null;
		if (unloadDistribution!=null) unload=unloadDistribution;
		if (unloadExpressionString!=null) unload=unloadExpressionString;
		final RunDataTransporter clone=new RunDataTransporter(type,index,capacity,load,unload,iconEastEmpty,iconWestEmpty,iconEastLoaded,iconWestLoaded,failures,list,variables);
		clone.position=position;
		clone.lastPosition=lastPosition;
		return clone;
	}

	/**
	 * Muss zum Zeitpunkt 0 aufgerufen werden, um evtl. Ausfallzeitpunkte einzuplanen
	 * @param simData	Simulationsdatenobjekt
	 * @param logTransporterName	Name der Transportergruppe
	 */
	public void prepareFailureSystem(final SimulationData simData, final String logTransporterName) {
		for (RunDataTransporterFailure failures: failures) failures.scheduleDownTime(simData,simData.currentTime,logTransporterName);
	}

	/**
	 * Löst einen Transfer des Transporters aus
	 * @param stationID	ID der Zielstation
	 * @param clientCount	Anzahl der beförderten Kunden
	 * @param simData	Simulationsdatenobjekt
	 * @return	Transportzeit in MS
	 */
	public long moveTo(final int stationID, final int clientCount, final SimulationData simData) {
		final boolean initialMove;
		final double transferDistance;
		final double transferTime;
		lastPosition=position;
		if (stationID==position) {
			initialMove=true;
			transferDistance=0;
			transferTime=0; /* Keine Bewegung, Transporter kommt nur virtuell bei seiner Startstation an */
		} else {
			initialMove=false;
			transferDistance=list.getTransferDistance(type,position,stationID);
			transferTime=list.getTransferTime(this,transferDistance,clientCount>0,simData);
		}
		final long transferTimeMS=FastMath.round(transferTime*1000);

		/* Start- und Zielstation bestimmen */
		final RunElement lastStation=(position>=0)?simData.runModel.elementsFast[position]:null;
		final RunElement nextStation=(stationID>=0)?simData.runModel.elementsFast[stationID]:null;

		if (!initialMove) {
			/* Logging */
			if (simData.loggingActive && simData.loggingIDs==null) simData.logEventExecution(Language.tr("Simulation.Log.Transporter"),String.format(Language.tr("Simulation.Log.Transporter.Move"),"\""+list.type[type]+"\"("+hashCode()+")",position,stationID,TimeTools.formatExactSystemTime(transferTime)));

			/* Daten in Transporter eintragen */
			position=stationID;
			inTransfer=true;

			/* Ausgangsstation benachrichtigen */
			if (lastStation instanceof TransporterPosition) ((TransporterPosition)lastStation).transporterLeave(this,simData);
		}

		/* Zielstation benachrichtigen, dass ein Transporter unterwegs ist */
		if (nextStation instanceof TransporterPosition) ((TransporterPosition)nextStation).transporterStartsMoving(this,simData);

		/* Ereignis für Ankunft anlegen */
		final TransporterArrivalEvent event=(TransporterArrivalEvent)simData.getEvent(TransporterArrivalEvent.class);
		event.init(simData.currentTime+transferTimeMS);
		event.transporter=this;
		event.station=(nextStation instanceof TransporterPosition)?((TransporterPosition)nextStation):null;
		simData.eventManager.addEvent(event);

		if (!initialMove) {
			/* Statistik */
			if (!simData.runData.isWarmUp) {
				if (statisticUtilization==null) statisticUtilization=(StatisticsTimePerformanceIndicator)simData.statistics.transporterUtilization.get(list.type[type]);
				statisticUtilization.set(simData.currentTime/1000.0,list.getWorkingTransporters(type));
			}
			/* Bewegung für Ausfall-System zählen */

			for (RunDataTransporterFailure failure: failures) failure.countServed(clientCount,transferTimeMS,transferDistance);
		}

		return transferTimeMS;
	}

	/**
	 * Wird durch das Transporter-Ankunfts-Ereignis ausgelöst und dient der
	 * Konfiguration des Transporters und der Statistikerfassung
	 * @param simData	Simulationsdatenobjekt
	 * @param arrivalTime	Ankunftszeit (in ms)
	 * @see TransporterArrivalEvent
	 */
	public void arrival(final SimulationData simData, final long arrivalTime) {
		/* Status: nicht mehr in Bewegung */
		inTransfer=false;

		/* System über Bewegung des Transporters benachrichtigen */
		simData.runData.fireTransporterMoveNotify(simData,this);

		/* Logging */
		if (simData.loggingActive && simData.loggingIDs==null) simData.logEventExecution(Language.tr("Simulation.Log.Transporter"),String.format(Language.tr("Simulation.Log.Transporter.Arrival"),"\""+list.type[type]+"\"("+hashCode()+")",position));

		/* Statistik */
		if (!simData.runData.isWarmUp) {
			if (statisticUtilization==null) statisticUtilization=(StatisticsTimePerformanceIndicator)simData.statistics.transporterUtilization.get(list.type[type]);
			statisticUtilization.set(simData.currentTime/1000.0,list.getWorkingTransporters(type));
		}
	}

	private boolean testStartPause(final SimulationData simData) {
		for (RunDataTransporterFailure failure: failures) {
			if (failure.testStartPause(simData,this)) return true;
		}
		return false;
	}

	/**
	 * Wird durch das Transporter-Ankunfts-Ereignis ausgelöst und dient dazu
	 * dem Transporter mitzuteilen, dass er nun wieder für neue Aufgaben bereit
	 * ist (weil er entladen wurde).
	 * @param simData	Simulationsdatenobjekt
	 */
	public void free(final SimulationData simData) {
		if (failures.length>0) {
			if (testStartPause(simData)) return; /* Doch nicht frei, geht in Pause */
		}

		list.transporterFree(this,simData);
	}

	/**
	 * Erfasst den Beginn einer Pause in der Statistik
	 * @param simData	Simulationsdatenobjekt
	 */
	public void startDownTime(final SimulationData simData) {
		inDownTime++;

		if (!simData.runData.isWarmUp) {
			if (statisticDownTime==null) statisticDownTime=(StatisticsTimePerformanceIndicator)simData.statistics.transporterInDownTime.get(list.type[type]);
			statisticDownTime.set(simData.currentTime/1000.0,inDownTime);
		}
	}

	/**
	 * Erfasst das Ende einer Pause in der Statistik
	 * @param simData	Simulationsdatenobjekt
	 */
	public void endDownTime(final SimulationData simData) {
		inDownTime--;

		if (!simData.runData.isWarmUp) {
			if (statisticDownTime==null) statisticDownTime=(StatisticsTimePerformanceIndicator)simData.statistics.transporterInDownTime.get(list.type[type]);
			statisticDownTime.set(simData.currentTime/1000.0,inDownTime);
		}
	}
}

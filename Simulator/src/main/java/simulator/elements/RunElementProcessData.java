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

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.util.FastMath;

import language.Language;
import mathtools.TimeTools;
import mathtools.distribution.tools.DistributionRandomNumber;
import parser.MathCalcError;
import simulator.coreelements.RunElementData;
import simulator.events.WaitingCancelEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;

/**
 * Laufzeitdaten eines <code>RunElementProcess</code>-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementProcess
 * @see RunElementData
 */
public class RunElementProcessData extends RunElementData {
	private static final int DEFAULT_QUEUE_SIZE=256;

	/** Liste mit den momentan an der Station wartenden Kunden */
	public final List<RunDataClient> waitingClients;

	private final List<WaitingCancelEvent> waitingCancelEvents;
	private final boolean hasWaitingCancelations; /* Wenn hier false steht, ist waitingCancelEvents==null */

	private final AbstractRealDistribution[][] distributionSetup;
	private final AbstractRealDistribution[] distributionProcess;
	private final AbstractRealDistribution[] distributionPostProcess;
	private final AbstractRealDistribution[] distributionCancel;
	private final RunElementProcess station;
	private final int batchMinSize;

	private final ExpressionCalc[][] expressionSetup;
	private final ExpressionCalc[] expressionProcess;
	private final ExpressionCalc[] expressionPostProcess;
	private final ExpressionCalc[] expressionCancel;

	/** Rechenausdruck für die Ressourcenpriorität der Bedienstation (ist nie <code>null</code>) */
	public final ExpressionCalc resourcePriority;
	/** Rechenausdrücke für die Kundenprioritäten an der Bedienstation (einzelne Einträge können <code>null</code> sein; für diese soll dann "w" gelten) */
	public final ExpressionCalc[] priority;

	/** Arbeitet die gesamte Station in Bezug auf die Kunden im FIFO-Modus? Dann brauchen die Kundenprioritäten gar nicht weiter berücksichtigt werden */
	public boolean allFirstComeFirstServe;

	/* Nur als Speicher-Bereich-Cache, damit das nicht immer wieder neu angelegt werden muss. */

	/** Cache-Array für die Score-Wert-Berechnung */
	public double[] score;

	/** Cache-Array für die Bedienzeit-Berechnung */
	public double[] processingTimes;

	/** Cache-Array für die Nachbearbeitungszeit-Berechnung */
	public double[] postProcessingTimes;

	/** Cache-Array für die Auswahl von Kunden für die Batch-Bedienung */
	public List<RunDataClient> globalSelectedForService;

	/** Steht das {@link #globalSelectedForService}-Cache-Array zur Verfügung? */
	public boolean canUseGlobalSelectedForService=true;

	/**
	 * Warteschlange für Zugriff durch PickUp (bzw. {@link RunElementProcess#getClient(SimulationData)}) sperren.
	 */
	public boolean queueLockedForPickUp;

	/**
	 * Kosten pro Bedienung
	 */
	public final ExpressionCalc costs;

	/**
	 * Kosten pro Bediensekunde
	 */
	public final ExpressionCalc costsPerProcessSecond;

	/**
	 * Kosten pro Nachbearbeitungssekunde
	 */
	public final ExpressionCalc costsPerPostProcessSecond;

	/**
	 * Wurden überhaupt Kosten definiert?
	 */
	public final boolean hasCosts;

	private int lastClientIndex=-1;

	/**
	 * Konstruktor der Klasse <code>RunElementProcessData</code>
	 * @param station	Zu dem Datenobjekt zugehöriges <code>RunElementProcess</code>-Element
	 * @param variableNames	Liste der global verfügbaren Variablennamen
	 * @param costs	Kosten pro Bedienvorgang (kann <code>null</code> sein)
	 * @param costsPerProcessSecond	Kosten pro Bediensekunde (kann <code>null</code> sein)
	 * @param costsPerPostProcessSecond	Kosten pro Nachbearbeitungssekunde (kann <code>null</code> sein)
	 */
	public RunElementProcessData(final RunElementProcess station, final String[] variableNames, final String costs, final String costsPerProcessSecond, final String costsPerPostProcessSecond) {
		super(station);
		allFirstComeFirstServe=true;
		queueLockedForPickUp=false;
		waitingClients=new ArrayList<>(DEFAULT_QUEUE_SIZE);
		/* Wird unten initialisiert (nur wenn es überhaupt Abbrüche geben kann): waitingCancelEvents=new ArrayList<>(DEFAULT_QUEUE_SIZE); */

		this.station=station;
		distributionSetup=station.distributionSetup;
		distributionProcess=station.distributionProcess;
		distributionPostProcess=station.distributionPostProcess;
		distributionCancel=station.distributionCancel;
		batchMinSize=station.batchMinSize;

		resourcePriority=new ExpressionCalc(variableNames);
		resourcePriority.parse(station.resourcePriority);

		priority=new ExpressionCalc[station.priority.length];
		for (int i=0;i<priority.length;i++) {
			if (station.priority[i]!=null) { /* Wenn null, war Default Priorität gesetzt (="w"). Dann priority[i] auf Vorgabe null lassen. Dies wird von ModelElementProcess.startProcessing() entsprechend erkannt. */
				priority[i]=new ExpressionCalc(variableNames);
				priority[i].parse(station.priority[i]);
			}
		}

		expressionSetup=new ExpressionCalc[station.expressionSetup.length][];
		for (int i=0;i<expressionSetup.length;i++) {
			expressionSetup[i]=new ExpressionCalc[station.expressionSetup[i].length];
			for (int j=0;j<station.expressionSetup[i].length;j++) if (station.expressionSetup[i][j]!=null) {
				expressionSetup[i][j]=new ExpressionCalc(variableNames);
				expressionSetup[i][j].parse(station.expressionSetup[i][j]);
			}
		}

		expressionProcess=new ExpressionCalc[station.expressionProcess.length];
		for (int i=0;i<expressionProcess.length;i++) if (station.expressionProcess[i]!=null) {
			expressionProcess[i]=new ExpressionCalc(variableNames);
			expressionProcess[i].parse(station.expressionProcess[i]);
		}

		expressionPostProcess=new ExpressionCalc[station.expressionPostProcess.length];
		for (int i=0;i<expressionPostProcess.length;i++) if (station.expressionPostProcess[i]!=null) {
			expressionPostProcess[i]=new ExpressionCalc(variableNames);
			expressionPostProcess[i].parse(station.expressionPostProcess[i]);
		}

		expressionCancel=new ExpressionCalc[station.expressionCancel.length];
		for (int i=0;i<expressionCancel.length;i++) if (station.expressionCancel[i]!=null) {
			expressionCancel[i]=new ExpressionCalc(variableNames);
			expressionCancel[i].parse(station.expressionCancel[i]);
		}

		boolean b=false;
		for (AbstractRealDistribution dist: distributionCancel) if (dist!=null) {b=true; break;}
		if (!b) for (ExpressionCalc expression: expressionCancel) if (expression!=null) {b=true; break;}
		hasWaitingCancelations=b;
		if (hasWaitingCancelations) waitingCancelEvents=new ArrayList<>(DEFAULT_QUEUE_SIZE); else waitingCancelEvents=null;

		if (costs==null || costs.trim().isEmpty()) {
			this.costs=null;
		} else {
			this.costs=new ExpressionCalc(variableNames);
			this.costs.parse(costs);
		}

		if (costsPerProcessSecond==null || costsPerProcessSecond.trim().isEmpty()) {
			this.costsPerProcessSecond=null;
		} else {
			this.costsPerProcessSecond=new ExpressionCalc(variableNames);
			this.costsPerProcessSecond.parse(costsPerProcessSecond);
		}

		if (costsPerPostProcessSecond==null || costsPerPostProcessSecond.trim().isEmpty()) {
			this.costsPerPostProcessSecond=null;
		} else {
			this.costsPerPostProcessSecond=new ExpressionCalc(variableNames);
			this.costsPerPostProcessSecond.parse(costsPerPostProcessSecond);
		}

		hasCosts=(this.costs!=null || this.costsPerProcessSecond!=null || this.costsPerPostProcessSecond!=null);
	}

	/**
	 * Fügt einen Kunden zu der Liste der wartenden Kunden hinzu
	 * @param client	Hinzuzufügender Kunde
	 * @param time	Zeitpunkt an dem der Kunde an der <code>RunElementProcess</code>-Station eingetroffen ist (zur späteren Berechnung der Wartezeit der Kunden)
	 * @param simData	Simulationsdatenobjekt
	 * @return	Gibt <code>true</code> zurück, wenn die minimal zulässige Batch-Größe erreicht ist.
	 */
	public boolean addClientToQueue(final RunDataClient client, final long time, final SimulationData simData) {
		/* Kunden an Warteschlange anstellen */
		waitingClients.add(client);
		client.lastWaitingStart=time;

		/* Logging */
		if (simData.loggingActive) station.log(simData,Language.tr("Simulation.Log.ProcessArrival"),String.format(Language.tr("Simulation.Log.ProcessArrival.Info"),client.logInfo(simData),station.name));

		/* Statistik */
		simData.runData.logClientEntersStationQueue(simData,station,this,client);

		/* Ggf. Warteabbruch einplanen */
		if (distributionCancel[client.type]!=null || expressionCancel[client.type]!=null) {
			double maxWaitingTime;
			if (distributionCancel[client.type]!=null) {
				maxWaitingTime=DistributionRandomNumber.randomNonNegative(distributionCancel[client.type]);
			} else {
				simData.runData.setClientVariableValues(client);
				try {
					maxWaitingTime=expressionCancel[client.type].calc(simData.runData.variableValues,simData,client);
				} catch (MathCalcError e) {
					simData.calculationErrorStation(expressionCancel[client.type],this);
					maxWaitingTime=-1;
				}
			}
			maxWaitingTime=maxWaitingTime*station.timeBaseMultiply;
			if (maxWaitingTime>=0) {
				final WaitingCancelEvent event=(WaitingCancelEvent)simData.getEvent(WaitingCancelEvent.class);
				event.init(time+FastMath.round(maxWaitingTime*1000));
				event.station=station;
				event.client=client;
				simData.eventManager.addEvent(event);
				waitingCancelEvents.add(event);

				/* Logging */
				if (simData.loggingActive) station.log(simData,Language.tr("Simulation.Log.ProcessWaitingTimeToleranceCalculation"),String.format(Language.tr("Simulation.Log.ProcessWaitingTimeToleranceCalculation.Info"),client.logInfo(simData),station.name,TimeTools.formatTime(FastMath.round(maxWaitingTime*1000)),TimeTools.formatTime(FastMath.round(maxWaitingTime*1000))));
			}
		} else {
			if (hasWaitingCancelations) waitingCancelEvents.add(null);
		}

		/* Sind alle Kunden, die bisher hier eingetroffen sind, vom Typ FCFS ? */
		if (priority[client.type]!=null) allFirstComeFirstServe=false;

		return waitingClients.size()>=batchMinSize;
	}

	/**
	 * Entfernt einen Kunden aus der Warteschlange
	 * @param client	Zu entfernender Kunde
	 * @param indexOfClientInQueue	Index des Kunden in der <code>waitingClients</code>-Liste. Kann -1 sein, dann wird der Index gemäß dem <code>client</code>-Objekt selbst ermittelt
	 * @param time	Zeitpunkt, an dem der Kunde entfernt werden soll
	 * @param success	Gibt an, ob der Kunde die Warteschlange erfolgreich durchlaufen hat (oder ob es sich um einen Warteabbrecher handelt)
	 * @param simData	Simulationsdatenobjekt
	 * @return	Gibt die Wartezeit des Kunden zurück
	 */
	public long removeClientFromQueue(final RunDataClient client, final int indexOfClientInQueue, final long time, final boolean success, final SimulationData simData) {
		final int index=(indexOfClientInQueue>=0)?indexOfClientInQueue:waitingClients.indexOf(client);
		if (index<0) return 0;
		final long timeInQueue=time-client.lastWaitingStart;

		/* Ggf. Warteabbruch-Event unbearbeitet löschen */
		if (success) {
			if (hasWaitingCancelations) {
				final WaitingCancelEvent waitingCancelEvent=waitingCancelEvents.get(index);
				if (waitingCancelEvent!=null) simData.eventManager.deleteEvent(waitingCancelEvent,simData);
			}
		}

		/* Eintragen, ob der Kunde die Warteschlange erfolgreich durchlaufen hat (für spätere Weiterleitung des Kunden zur nächsten Station) */
		client.lastQueueSuccess=success;

		/* Kunde aus Warteschlange austragen */
		waitingClients.remove(index);
		if (hasWaitingCancelations) waitingCancelEvents.remove(index);

		/* Statistik */
		simData.runData.logClientLeavesStationQueue(simData,station,this,client);

		/* Wartezeit zurückliefern */
		return timeInQueue;
	}

	/** Umrechnungsfaktor von Millisekunden auf Sekunden, um die Division während der Simulation zu vermeiden */
	private static final double toSecFactor=1.0/1000.0;

	/**
	 * Liefert die Bedienzeit für einen Kunden (über eine Verteilungsfunktion oder durch Auswertung eines Ausdrucks)
	 * @param simData	Simulationsdaten (wird benötigt, falls die Zeit per Auswertung eines Ausdrucks bestimmt werden soll)
	 * @param client	Kunde, für den die Bedienzeit bestimmt werden soll (wird benötigt, falls die Zeit per Auswertung eines Ausdrucks bestimmt werden soll)
	 * @return	Bedienzeit in Sekunden
	 */
	public double getProcessTime(final SimulationData simData, final RunDataClient client) {
		final int type=client.type;
		if (expressionProcess[type]==null) {
			if (distributionProcess[type]==null) return 0.0;
			return DistributionRandomNumber.randomNonNegative(distributionProcess[type])*station.timeBaseMultiply;
		} else {
			final double additionalWaitingTime=(simData.currentTime-client.lastWaitingStart)*toSecFactor;
			simData.runData.setClientVariableValues(client,additionalWaitingTime);
			try {
				final double time=expressionProcess[type].calc(simData.runData.variableValues,simData,client)*station.timeBaseMultiply;
				return (time>=0)?time:0;
			} catch (MathCalcError e) {
				simData.calculationErrorStation(expressionProcess[type],this);
				return 0;
			}
		}
	}

	/**
	 * Liefert die Rüstzeit für einen Kunden (über eine Verteilungsfunktion oder durch Auswertung eines Ausdrucks)
	 * @param simData	Simulationsdaten (wird benötigt, falls die Zeit per Auswertung eines Ausdrucks bestimmt werden soll)
	 * @param client	Kunde, für die die Rüstzeit bestimmt werden soll (der vorherige Kunde ist implizit durch den vorherigen Aufruf dieser Funktion bekannt)
	 * @return	Rüstzeit in Sekunden
	 */
	public double getSetupTime(final SimulationData simData, final RunDataClient client) {
		double time=0.0;
		final int nextClientIndex=client.type;
		if (lastClientIndex>=0) {
			if (expressionSetup[lastClientIndex][nextClientIndex]==null) {
				if (distributionSetup[lastClientIndex][nextClientIndex]!=null) {
					time=DistributionRandomNumber.randomNonNegative(distributionSetup[lastClientIndex][nextClientIndex])*station.timeBaseMultiply;
				}
			} else {
				final double additionalWaitingTime=(simData.currentTime-client.lastWaitingStart)*toSecFactor;
				simData.runData.setClientVariableValues(client,additionalWaitingTime);
				try {
					time=expressionSetup[lastClientIndex][nextClientIndex].calc(simData.runData.variableValues,simData,client)*station.timeBaseMultiply;
				} catch (MathCalcError e) {
					simData.calculationErrorStation(expressionSetup[lastClientIndex][nextClientIndex],this);
					time=0;
				}
			}
		}
		lastClientIndex=nextClientIndex;
		return (time>=0)?time:0;
	}

	/**
	 * Liefert die Nachbearbeitungszeit für einen Kunden (über eine Verteilungsfunktion oder durch Auswertung eines Ausdrucks)
	 * @param simData	Simulationsdaten (wird benötigt, falls die Zeit per Auswertung eines Ausdrucks bestimmt werden soll)
	 * @param client	Kunde, für die die Nachbearbeitungszeit bestimmt werden soll (wird benötigt, falls die Zeit per Auswertung eines Ausdrucks bestimmt werden soll)
	 * @return	Nachbearbeitungszeit in Sekunden
	 */
	public double getPostProcessTime(final SimulationData simData, final RunDataClient client) {
		final int type=client.type;
		if (expressionPostProcess[type]==null) {
			if (distributionPostProcess[type]==null) return 0.0;
			return DistributionRandomNumber.randomNonNegative(distributionPostProcess[type])*station.timeBaseMultiply;
		} else {
			final double additionalWaitingTime=(simData.currentTime-client.lastWaitingStart)*toSecFactor;
			simData.runData.setClientVariableValues(client,additionalWaitingTime);
			try {
				final double time=expressionPostProcess[type].calc(simData.runData.variableValues,simData,client)*station.timeBaseMultiply;
				return (time>=0)?time:0;
			} catch (MathCalcError e) {
				simData.calculationErrorStation(expressionPostProcess[type],this);
				return 0;
			}
		}
	}
}

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
import parser.MathCalcError;
import simulator.coreelements.RunElementData;
import simulator.events.WaitingCancelEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import statistics.StatisticsDataPerformanceIndicator;

/**
 * Laufzeitdaten eines <code>RunElementProcess</code>-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementProcess
 * @see RunElementData
 */
public class RunElementProcessData extends RunElementData implements RunElementDataWithWaitingClients {
	/**
	 * Anf�ngliche Gr��e f�r die Listen zur Speicherung
	 * der wartenden Kunden
	 * @see #waitingClients
	 * @see #waitingCancelEvents
	 */
	private static final int INITIAL_QUEUE_SIZE=256;

	/** Liste mit den momentan an der Station wartenden Kunden */
	public final List<RunDataClient> waitingClients;

	/** Liste der Warteabbruch-Ereignisse (um diese ggf. unbearbeitet l�schen zu k�nnen) */
	private final List<WaitingCancelEvent> waitingCancelEvents;
	/** Liegen begrenzte Wartezeittoleranzen vor? (Wenn hier <code>false</code> steht, ist <code>waitingCancelEvents==null</code>) */
	private final boolean hasWaitingCancelations;

	/** R�stzeitverteilungen */
	private final AbstractRealDistribution[][] distributionSetup;
	/** Bedienzeitenverteilung */
	private final AbstractRealDistribution[] distributionProcess;
	/** Nachbearbeitungszeitenverteilung */
	private final AbstractRealDistribution[] distributionPostProcess;
	/** Wartezeittoleranzenverteilung */
	private final AbstractRealDistribution[] distributionCancel;
	/** Zu dem Datenobjekt zugeh�riges {@link RunElementProcess}-Element */
	private final RunElementProcess station;
	/** Minimale Bedien-Batch-Gr��e */
	private final int batchMinSize;

	/** Rechenausdr�cke f�r R�stzeiten */
	private final ExpressionCalc[][] expressionSetup;
	/** Rechenausdr�cke f�r Bedienzeiten */
	private final ExpressionCalc[] expressionProcess;
	/** Rechenausdr�cke f�r Nachbearbeitungszeiten */
	private final ExpressionCalc[] expressionPostProcess;
	/** Rechenausdr�cke f�r Wartezeittoleranzen */
	private final ExpressionCalc[] expressionCancel;

	/** Rechenausdruck f�r die Ressourcenpriorit�t der Bedienstation (ist nie <code>null</code>) */
	public final ExpressionCalc resourcePriority;
	/** Rechenausdr�cke f�r die Kundenpriorit�ten an der Bedienstation (einzelne Eintr�ge k�nnen <code>null</code> sein; f�r diese soll dann "w" gelten) */
	public final ExpressionCalc[] priority;

	/** Arbeitet die gesamte Station in Bezug auf die Kunden im FIFO-Modus? Dann brauchen die Kundenpriorit�ten gar nicht weiter ber�cksichtigt werden */
	public boolean allFirstComeFirstServe;

	/* Nur als Speicher-Bereich-Cache, damit das nicht immer wieder neu angelegt werden muss. */

	/** Cache-Array f�r die Score-Wert-Berechnung */
	public double[] score;

	/** Cache-Array f�r die Bedienzeit-Berechnung */
	public double[] processingTimes;

	/** Cache-Array f�r die Nachbearbeitungszeit-Berechnung */
	public double[] postProcessingTimes;

	/** Cache-Array f�r die Auswahl von Kunden f�r die Batch-Bedienung */
	public List<RunDataClient> globalSelectedForService;

	/** Steht das {@link #globalSelectedForService}-Cache-Array zur Verf�gung? */
	public boolean canUseGlobalSelectedForService=true;

	/**
	 * Warteschlange f�r Zugriff durch PickUp (bzw. {@link RunElementProcess#getClient(SimulationData)}) sperren.
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
	 * Wurden �berhaupt Kosten definiert?
	 */
	public final boolean hasCosts;

	/**
	 * Statistikobjekt f�r die R�stzeiten (wird erst bei Bedarf initialisiert)
	 */
	public StatisticsDataPerformanceIndicator setupTimes;

	/**
	 * Kundentyp des Kunden dessen Bedienung
	 * zuletzt gestartet wurde (um ggf. R�stzeiten
	 * bestimmen zu k�nnen).
	 * @see #getSetupTime(SimulationData, RunDataClient)
	 */
	public int lastClientIndex=-1;

	/**
	 * Konstruktor der Klasse {@link RunElementProcessData}
	 * @param station	Zu dem Datenobjekt zugeh�riges {@link RunElementProcess}-Element
	 * @param variableNames	Liste der global verf�gbaren Variablennamen
	 * @param costs	Kosten pro Bedienvorgang (kann <code>null</code> sein)
	 * @param costsPerProcessSecond	Kosten pro Bediensekunde (kann <code>null</code> sein)
	 * @param costsPerPostProcessSecond	Kosten pro Nachbearbeitungssekunde (kann <code>null</code> sein)
	 * @param simData	Simulationsdatenobjekt
	 */
	public RunElementProcessData(final RunElementProcess station, final String[] variableNames, final String costs, final String costsPerProcessSecond, final String costsPerPostProcessSecond, final SimulationData simData) {
		super(station,simData);
		allFirstComeFirstServe=true;
		queueLockedForPickUp=false;
		waitingClients=new ArrayList<>(INITIAL_QUEUE_SIZE);
		/* Wird unten initialisiert (nur wenn es �berhaupt Abbr�che geben kann): waitingCancelEvents=new ArrayList<>(DEFAULT_QUEUE_SIZE); */

		this.station=station;
		distributionSetup=station.distributionSetup;
		distributionProcess=station.distributionProcess;
		distributionPostProcess=station.distributionPostProcess;
		distributionCancel=station.distributionCancel;
		batchMinSize=station.batchMinSize;

		resourcePriority=new ExpressionCalc(variableNames,simData.runModel.modelUserFunctions);
		resourcePriority.parse(station.resourcePriority);

		priority=new ExpressionCalc[station.priority.length];
		for (int i=0;i<priority.length;i++) {
			if (station.priority[i]!=null) { /* Wenn null, war Default Priorit�t gesetzt (="w"). Dann priority[i] auf Vorgabe null lassen. Dies wird von ModelElementProcess.startProcessing() entsprechend erkannt. */
				priority[i]=new ExpressionCalc(variableNames,simData.runModel.modelUserFunctions);
				priority[i].parse(station.priority[i]);
			}
		}

		expressionSetup=new ExpressionCalc[station.expressionSetup.length][];
		for (int i=0;i<expressionSetup.length;i++) {
			expressionSetup[i]=new ExpressionCalc[station.expressionSetup[i].length];
			for (int j=0;j<station.expressionSetup[i].length;j++) if (station.expressionSetup[i][j]!=null) {
				expressionSetup[i][j]=new ExpressionCalc(variableNames,simData.runModel.modelUserFunctions);
				expressionSetup[i][j].parse(station.expressionSetup[i][j]);
			}
		}

		expressionProcess=new ExpressionCalc[station.expressionProcess.length];
		for (int i=0;i<expressionProcess.length;i++) if (station.expressionProcess[i]!=null) {
			expressionProcess[i]=new ExpressionCalc(variableNames,simData.runModel.modelUserFunctions);
			expressionProcess[i].parse(station.expressionProcess[i]);
		}

		expressionPostProcess=new ExpressionCalc[station.expressionPostProcess.length];
		for (int i=0;i<expressionPostProcess.length;i++) if (station.expressionPostProcess[i]!=null) {
			expressionPostProcess[i]=new ExpressionCalc(variableNames,simData.runModel.modelUserFunctions);
			expressionPostProcess[i].parse(station.expressionPostProcess[i]);
		}

		expressionCancel=new ExpressionCalc[station.expressionCancel.length];
		for (int i=0;i<expressionCancel.length;i++) if (station.expressionCancel[i]!=null) {
			expressionCancel[i]=new ExpressionCalc(variableNames,simData.runModel.modelUserFunctions);
			expressionCancel[i].parse(station.expressionCancel[i]);
		}

		boolean b=false;
		for (AbstractRealDistribution dist: distributionCancel) if (dist!=null) {b=true; break;}
		if (!b) for (ExpressionCalc expression: expressionCancel) if (expression!=null) {b=true; break;}
		hasWaitingCancelations=b;
		if (hasWaitingCancelations) waitingCancelEvents=new ArrayList<>(INITIAL_QUEUE_SIZE); else waitingCancelEvents=null;

		if (costs==null || costs.isBlank()) {
			this.costs=null;
		} else {
			this.costs=new ExpressionCalc(variableNames,simData.runModel.modelUserFunctions);
			this.costs.parse(costs);
		}

		if (costsPerProcessSecond==null || costsPerProcessSecond.isBlank()) {
			this.costsPerProcessSecond=null;
		} else {
			this.costsPerProcessSecond=new ExpressionCalc(variableNames,simData.runModel.modelUserFunctions);
			this.costsPerProcessSecond.parse(costsPerProcessSecond);
		}

		if (costsPerPostProcessSecond==null || costsPerPostProcessSecond.isBlank()) {
			this.costsPerPostProcessSecond=null;
		} else {
			this.costsPerPostProcessSecond=new ExpressionCalc(variableNames,simData.runModel.modelUserFunctions);
			this.costsPerPostProcessSecond.parse(costsPerPostProcessSecond);
		}

		hasCosts=(this.costs!=null || this.costsPerProcessSecond!=null || this.costsPerPostProcessSecond!=null);
	}

	/**
	 * F�gt einen Kunden zu der Liste der wartenden Kunden hinzu
	 * @param client	Hinzuzuf�gender Kunde
	 * @param time	Zeitpunkt an dem der Kunde an der <code>RunElementProcess</code>-Station eingetroffen ist (zur sp�teren Berechnung der Wartezeit der Kunden)
	 * @param simData	Simulationsdatenobjekt
	 * @return	Gibt <code>true</code> zur�ck, wenn die minimal zul�ssige Batch-Gr��e erreicht ist.
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
				maxWaitingTime=simData.runData.random.randomNonNegative(distributionCancel[client.type]);
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
				client.lastWaitingTimeTolerance=maxWaitingTime;
				if (!simData.runData.stopp)  {
					final WaitingCancelEvent event=(WaitingCancelEvent)simData.getEvent(WaitingCancelEvent.class);
					event.init(time+FastMath.round(maxWaitingTime*simData.runModel.scaleToSimTime));
					event.station=station;
					event.client=client;
					simData.eventManager.addEvent(event);
					waitingCancelEvents.add(event);
				}

				/* Logging */
				if (simData.loggingActive) station.log(simData,Language.tr("Simulation.Log.ProcessWaitingTimeToleranceCalculation"),String.format(Language.tr("Simulation.Log.ProcessWaitingTimeToleranceCalculation.Info"),client.logInfo(simData),station.name,TimeTools.formatExactTime(maxWaitingTime),TimeTools.formatExactTime(time*simData.runModel.scaleToSeconds+maxWaitingTime)));
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
	 * @param indexOfClientInQueue	Index des Kunden in der <code>waitingClients</code>-Liste. Kann -1 sein, dann wird der Index gem�� dem <code>client</code>-Objekt selbst ermittelt
	 * @param time	Zeitpunkt, an dem der Kunde entfernt werden soll
	 * @param success	Gibt an, ob der Kunde die Warteschlange erfolgreich durchlaufen hat (oder ob es sich um einen Warteabbrecher handelt)
	 * @param simData	Simulationsdatenobjekt
	 * @return	Gibt die Wartezeit des Kunden zur�ck
	 */
	public long removeClientFromQueue(final RunDataClient client, final int indexOfClientInQueue, final long time, final boolean success, final SimulationData simData) {
		final int index=(indexOfClientInQueue>=0)?indexOfClientInQueue:waitingClients.indexOf(client);
		if (index<0) return 0;
		final long timeInQueue=time-client.lastWaitingStart;

		/* Ggf. Warteabbruch-Event unbearbeitet l�schen */
		if (success) {
			if (hasWaitingCancelations) {
				final WaitingCancelEvent waitingCancelEvent=waitingCancelEvents.get(index);
				if (waitingCancelEvent!=null) simData.eventManager.deleteEvent(waitingCancelEvent,simData);
			}
		}

		/* Eintragen, ob der Kunde die Warteschlange erfolgreich durchlaufen hat (f�r sp�tere Weiterleitung des Kunden zur n�chsten Station) */
		client.lastQueueSuccess=success;

		/* Kunde aus Warteschlange austragen */
		waitingClients.remove(index);
		if (hasWaitingCancelations) waitingCancelEvents.remove(index);

		/* Statistik */
		simData.runData.logClientLeavesStationQueue(simData,station,this,client);

		/* Wartezeit zur�ckliefern */
		return timeInQueue;
	}

	/**
	 * Ermittelt das Warteabbruch-Ereignis (sofern ein solches vorhanden ist) f�r einen Kunden in der Warteschlange.
	 * @param client	Zu entfernender Kunde
	 * @param indexOfClientInQueue	Index des Kunden in der <code>waitingClients</code>-Liste. Kann -1 sein, dann wird der Index gem�� dem <code>client</code>-Objekt selbst ermittelt
	 * @return	Liefert im Erfolsfall das Warteabbruch-Ereignis sonst <code>null</code>
	 */
	public WaitingCancelEvent getWaitingCancelEvent(final RunDataClient client, final int indexOfClientInQueue) {
		if (!hasWaitingCancelations) return null;

		final int index=(indexOfClientInQueue>=0)?indexOfClientInQueue:waitingClients.indexOf(client);
		if (index<0) return null;

		return waitingCancelEvents.get(index);
	}

	/**
	 * Liefert die Bedienzeit f�r einen Kunden (�ber eine Verteilungsfunktion oder durch Auswertung eines Ausdrucks)
	 * @param simData	Simulationsdaten (wird ben�tigt, falls die Zeit per Auswertung eines Ausdrucks bestimmt werden soll)
	 * @param client	Kunde, f�r den die Bedienzeit bestimmt werden soll (wird ben�tigt, falls die Zeit per Auswertung eines Ausdrucks bestimmt werden soll)
	 * @return	Bedienzeit in Sekunden
	 */
	public double getProcessTime(final SimulationData simData, final RunDataClient client) {
		final int type=client.type;
		if (expressionProcess[type]==null) {
			if (distributionProcess[type]==null) return 0.0;
			return simData.runData.random.randomNonNegative(distributionProcess[type])*station.timeBaseMultiply;
		} else {
			final double additionalWaitingTime=(simData.currentTime-client.lastWaitingStart)*simData.runModel.scaleToSeconds;
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
	 * Liefert die R�stzeit f�r einen Kunden (�ber eine Verteilungsfunktion oder durch Auswertung eines Ausdrucks)
	 * @param simData	Simulationsdaten (wird ben�tigt, falls die Zeit per Auswertung eines Ausdrucks bestimmt werden soll)
	 * @param client	Kunde, f�r die die R�stzeit bestimmt werden soll (der vorherige Kunde ist implizit durch den vorherigen Aufruf dieser Funktion bekannt)
	 * @return	R�stzeit in Sekunden
	 */
	public double getSetupTime(final SimulationData simData, final RunDataClient client) {
		double time=0.0;
		final int nextClientIndex=client.type;
		if (lastClientIndex>=0) {
			if (expressionSetup[lastClientIndex][nextClientIndex]==null) {
				if (distributionSetup[lastClientIndex][nextClientIndex]!=null) {
					time=simData.runData.random.randomNonNegative(distributionSetup[lastClientIndex][nextClientIndex])*station.timeBaseMultiply;
				}
			} else {
				final double additionalWaitingTime=(simData.currentTime-client.lastWaitingStart)*simData.runModel.scaleToSeconds;
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
	 * Liefert die Nachbearbeitungszeit f�r einen Kunden (�ber eine Verteilungsfunktion oder durch Auswertung eines Ausdrucks)
	 * @param simData	Simulationsdaten (wird ben�tigt, falls die Zeit per Auswertung eines Ausdrucks bestimmt werden soll)
	 * @param client	Kunde, f�r die die Nachbearbeitungszeit bestimmt werden soll (wird ben�tigt, falls die Zeit per Auswertung eines Ausdrucks bestimmt werden soll)
	 * @return	Nachbearbeitungszeit in Sekunden
	 */
	public double getPostProcessTime(final SimulationData simData, final RunDataClient client) {
		final int type=client.type;
		if (expressionPostProcess[type]==null) {
			if (distributionPostProcess[type]==null) return 0.0;
			return simData.runData.random.randomNonNegative(distributionPostProcess[type])*station.timeBaseMultiply;
		} else {
			final double additionalWaitingTime=(simData.currentTime-client.lastWaitingStart)*simData.runModel.scaleToSeconds;
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

	@Override
	public List<RunDataClient> getWaitingClients() {
		return waitingClients;
	}
}

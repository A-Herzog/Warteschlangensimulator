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
import java.io.File;
import java.util.Map;

import org.apache.commons.math3.util.FastMath;

import language.Language;
import mathtools.NumberTools;
import scripting.java.ExternalConnect;
import simcore.SimData;
import simcore.eventcache.AssociativeEventCache;
import simcore.eventmanager.LongRunMultiSortedArrayListEventManager;
import simcore.logging.CallbackLogger;
import simcore.logging.SimLogging;
import simulator.Simulator;
import simulator.coreelements.RunElement;
import simulator.coreelements.RunElementData;
import simulator.elements.RunElementThroughput;
import simulator.logging.MultiTypeTextLogger;
import simulator.simparser.ExpressionCalc;
import simulator.statistics.Statistics;
import statistics.StatisticsDataPerformanceIndicator;
import statistics.StatisticsDataPerformanceIndicatorWithNegativeValues;
import statistics.StatisticsLongRunPerformanceIndicator;
import statistics.StatisticsMultiPerformanceIndicator;
import statistics.StatisticsPerformanceIndicator;
import statistics.StatisticsSimpleCountPerformanceIndicator;
import statistics.StatisticsStateTimePerformanceIndicator;
import statistics.StatisticsTimeAnalogPerformanceIndicator;
import statistics.StatisticsTimeContinuousPerformanceIndicator;
import statistics.StatisticsTimePerformanceIndicator;
import statistics.StatisticsValuePerformanceIndicator;
import tools.SetupData;
import ui.modeleditor.coreelements.ModelElement;

/**
 * Diese Klasse enthält alle Daten, die zur Laufzeit der Simulation von einem Simulationsthread verwendet werden.
 * @author Alexander Herzog
 */
public class SimulationData extends SimData {
	/**
	 * Instanz des Laufzeit-Modells
	 * (read-only, da zwischen allen Threads geteilt; dynamische Daten werden in <code>RunData</code>, nicht in <code>RunModel</code> abgelegt)
	 */
	public final RunModel runModel;

	/**
	 * Instanz der dynamischen Daten
	 * (thread-lokal)
	 */
	public RunData runData;

	/**
	 * Simulator-Objekt in dem sich dieses Datenobjekt befindet
	 */
	public final Simulator simulator;

	/**
	 * Statistik-Objekt, welches während der Simulation die Daten sammelt
	 * (thread-lokal, die Ergebnisse werden am Ende zusammengeführt)
	 */
	public Statistics statistics;

	/**
	 * Gibt an, durch wie viel die Anzahl an Kunden für einen Thread geteilt werden soll.<br>
	 * (Dieser Wert wird auch in das RunModel übertragen.)
	 */
	public final int clientCountDiv;

	/**
	 * Aktueller Simulationstag. 0-basierend.
	 */
	private long currentDay;

	/**
	 * Ist das Logging aktiv, so kann hier eingeschränkt werden, dass nur Ereignisse zu bestimmten Stationen erfasst werden sollen. Ist das Feld <code>null</code>, so wird (sofern das Logging aktiv ist) alles erfasst.
	 */
	public boolean[] loggingIDs;

	/**
	 * Erfassung von Ankünften
	 */
	public boolean logArrival;

	/**
	 * Erfassung von Abgängen
	 */
	public boolean logDeparture;

	/**
	 * Erfassung von Stationsinformationen
	 */
	public boolean logInfoStation;

	/**
	 * Erfassung von Systeminformationen
	 */
	public boolean logInfoSystem;

	/**
	 * Thread-übergreifendes System um alle Threads möglichst gleichmäßig mit Kundenankünften zu versorgen
	 * @see RunData
	 */
	private DynamicLoadBalancer dynamicLoadBalancer;

	/**
	 * System zum Aufruf von Methoden in externen Skripten
	 * @see RunModel#pluginsFolder
	 */
	public ExternalConnect pluginsConnect;

	/**
	 * Optionales Callback, welches von Skriptcode aus aufgerufen werden kann,
	 * um eine Animation zu pausieren. (Ist insbesondere dann <code>null</code>,
	 * wenn die Simulation nicht als Animation läuft.
	 */
	public Runnable pauseAnimationCallback;

	/**
	 * Konstruktor der Klasse <code>SimulationData</code>
	 * @param threadNr		Gibt die Nummer des Threads an, für den das <code>SimDat</code>-Objekt erstellt wird.
	 * @param threadCount	Anzahl der Rechenthreads
	 * @param simulator	Simulator-Objekt in dem sich dieses Datenobjekt befindet
	 * @param runModel	Laufzeit-Modell, welches die Basis der Simulation darstellt
	 * @param useStatistics	Wird hier ein Wert ungleich <code>null</code> übergeben, so wird das angegebene Statistikobjekt verwendet. Sonst wird ein neues Statistikobjekt erstellt. Für eine normale Simulation sollte hier stets <code>null</code> übergeben werden.
	 * @param dynamicLoadBalancer	Optional ein Load-Balancer für die Ankünfte über alle Threads (kann <code>null</code> sein)
	 */
	public SimulationData(final int threadNr, final int threadCount, final Simulator simulator, final RunModel runModel, final Statistics useStatistics, final DynamicLoadBalancer dynamicLoadBalancer) {
		/* langsam: super(new PriorityQueueEventManager(),new HashMapEventCache(),threadNr,threadCount); */
		/* schneller: super(new LongRunMultiPriorityQueueEventManager(4),new HashMapEventCache(),threadNr,threadCount); */
		/* ganz schnell: */
		super(new LongRunMultiSortedArrayListEventManager(4),new AssociativeEventCache(128),threadNr,threadCount);

		loggingIDs=null;
		logArrival=true;
		logDeparture=true;
		logInfoStation=true;
		logInfoSystem=true;

		this.simulator=simulator;

		this.runModel=runModel;
		this.runData=new RunData(runModel,dynamicLoadBalancer);
		if (useStatistics!=null) {
			statistics=useStatistics;
		} else {
			statistics=new Statistics(runModel.correlationRange,runModel.correlationMode,runModel.batchMeansSize,runModel.collectWaitingTimes,runModel.distributionRecordHours,runModel.distributionRecordClientDataValues,runModel.useWelford);
		}

		if (runModel.repeatCount>1) {
			int div=1;
			int repeat=runModel.repeatCount;
			if (runModel.repeatAllowSplit) while (repeat<threadCount) {repeat*=2; div*=2;}
			clientCountDiv=div;
			int baseCount=repeat/threadCount;
			int addCount=repeat%threadCount;
			if (threadNr<addCount) {
				simDays=baseCount+1;
				simDaysByOtherThreads=threadNr*(baseCount+1);
			} else {
				simDays=baseCount;
				simDaysByOtherThreads=addCount*(baseCount+1)+(threadNr-addCount)*baseCount;
			}
		} else {
			clientCountDiv=threadCount;
			simDaysByOtherThreads=threadNr;
			simDays=1; /* Wir machen Longrun. */
		}
		runModel.clientCountDiv=clientCountDiv;
		/* System.out.println("Thread-"+(threadNr+1)+": "+simDays+" "+simDaysByOtherThreads); */

		if (runModel.pluginsFolder!=null && !runModel.pluginsFolder.trim().isEmpty()) {
			pluginsConnect=new ExternalConnect(new File(runModel.pluginsFolder));
		}

		this.dynamicLoadBalancer=dynamicLoadBalancer;
	}

	/**
	 * Liefert die Anzahl an Wiederholungen, die simuliert werden sollen.
	 * @return	Anzahl an Wiederholungen
	 */
	public long getRepeatCount() {
		return runModel.repeatCount;
	}

	/**
	 * Liefert die aktuell durch diesen Thread in Bearbeitung befindliche Wiederholung des Modells
	 * (0-basierend).
	 * @return	Aktuelle Wiederholung des Modells
	 */
	public long getCurrentRepeat() {
		return (simDaysByOtherThreads+currentDay)/clientCountDiv;
	}

	/**
	 * Setzt alle Daten (nach dem Ende der Einschwingphase) in einem Statistikobjekt zurück.
	 * @param indicators	Statistikobjekt
	 * @see #endWarmUp()
	 */
	private void resetAllDataPerformanceIndicators(final StatisticsMultiPerformanceIndicator indicators) {
		if (indicators.getTemplateClass()==StatisticsDataPerformanceIndicator.class) {
			for (StatisticsDataPerformanceIndicator indicator: (StatisticsDataPerformanceIndicator[])indicators.getAll(StatisticsDataPerformanceIndicator.class)) indicator.reset();
		}
		if (indicators.getTemplateClass()==StatisticsDataPerformanceIndicatorWithNegativeValues.class) {
			for (StatisticsDataPerformanceIndicatorWithNegativeValues indicator: (StatisticsDataPerformanceIndicatorWithNegativeValues[])indicators.getAll(StatisticsDataPerformanceIndicatorWithNegativeValues.class)) indicator.reset();
		}
	}

	/**
	 * Setzt alle Daten (nach dem Ende der Einschwingphase) in einem Statistikobjekt zurück.
	 * @param indicators	Statistikobjekt
	 * @see #endWarmUp()
	 */
	private void resetAllValuePerformanceIndicators(final StatisticsMultiPerformanceIndicator indicators) {
		for (StatisticsValuePerformanceIndicator indicator: (StatisticsValuePerformanceIndicator[])indicators.getAll(StatisticsValuePerformanceIndicator.class)) indicator.reset();
	}

	/**
	 * Setzt alle Daten (nach dem Ende der Einschwingphase) in einem Statistikobjekt zurück.
	 * @param indicators	Statistikobjekt
	 * @param time	Referenzzeit für den Start in der Erfassung von folgenden Daten in dem Statistikobjekt
	 * @see #endWarmUp()
	 */
	private void resetAllTimePerformanceIndicators(final StatisticsMultiPerformanceIndicator indicators, final double time) {
		for (StatisticsTimePerformanceIndicator indicator: (StatisticsTimePerformanceIndicator[])indicators.getAll(StatisticsTimePerformanceIndicator.class)) {
			indicator.setTime(time);
			if (time==0.0) indicator.set(0.0,0);
		}
	}

	/**
	 * Setzt alle Daten (nach dem Ende der Einschwingphase) in einem Statistikobjekt zurück.
	 * @param indicators	Statistikobjekt
	 * @param time	Referenzzeit für den Start in der Erfassung von folgenden Daten in dem Statistikobjekt
	 * @see #endWarmUp()
	 */
	private void resetAllTimeContinuousPerformanceIndicators(final StatisticsMultiPerformanceIndicator indicators, final double time) {
		for (StatisticsTimeContinuousPerformanceIndicator indicator: (StatisticsTimeContinuousPerformanceIndicator[])indicators.getAll(StatisticsTimeContinuousPerformanceIndicator.class)) indicator.setTime(time);
	}

	/**
	 * Wird beim Erreichen des Endes der Einschwingphase durch eine Kundenquelle aufgerufen.
	 */
	public void endWarmUp() {
		final double time=currentTime/1000.0;
		runData.warmUpEndTime=time;

		resetAllDataPerformanceIndicators(statistics.clientsInterarrivalTime);
		resetAllDataPerformanceIndicators(statistics.stationsInterarrivalTime);
		resetAllDataPerformanceIndicators(statistics.stationsInterarrivalTimeBatch);
		resetAllDataPerformanceIndicators(statistics.stationsInterarrivalTimeByState);
		resetAllDataPerformanceIndicators(statistics.stationsInterarrivalTimeByClientType);
		resetAllDataPerformanceIndicators(statistics.clientsInterleavingTime);
		resetAllDataPerformanceIndicators(statistics.stationsInterleavingTime);
		resetAllDataPerformanceIndicators(statistics.stationsInterleavingTimeBatch);
		resetAllDataPerformanceIndicators(statistics.stationsInterleavingTimeByClientType);
		resetAllDataPerformanceIndicators(statistics.clientsWaitingTimes);
		resetAllDataPerformanceIndicators(statistics.clientsTransferTimes);
		resetAllDataPerformanceIndicators(statistics.clientsProcessingTimes);
		resetAllDataPerformanceIndicators(statistics.clientsResidenceTimes);
		statistics.clientsAllWaitingTimes.reset();
		statistics.clientsAllTransferTimes.reset();
		statistics.clientsAllProcessingTimes.reset();
		statistics.clientsAllResidenceTimes.reset();
		resetAllDataPerformanceIndicators(statistics.stationsWaitingTimes);
		resetAllDataPerformanceIndicators(statistics.stationsTransferTimes);
		resetAllDataPerformanceIndicators(statistics.stationsProcessingTimes);
		resetAllDataPerformanceIndicators(statistics.stationsResidenceTimes);
		resetAllDataPerformanceIndicators(statistics.stationsWaitingTimesByClientType);
		resetAllDataPerformanceIndicators(statistics.stationsTransferTimesByClientType);
		resetAllDataPerformanceIndicators(statistics.stationsProcessingTimesByClientType);
		resetAllDataPerformanceIndicators(statistics.stationsResidenceTimesByClientType);
		statistics.clientsInSystem.setTime(time);
		if (time==0.0) statistics.clientsInSystem.set(0.0,0);
		statistics.clientsInSystemQueues.setTime(time);
		if (time==0.0) statistics.clientsInSystemQueues.set(0.0,0);
		statistics.clientsInSystemProcess.setTime(time);
		if (time==0.0) statistics.clientsInSystemProcess.set(0.0,0);
		resetAllTimePerformanceIndicators(statistics.clientsAtStationByStation,time);
		resetAllTimePerformanceIndicators(statistics.clientsAtStationByStationAndClient,time);
		resetAllTimePerformanceIndicators(statistics.clientsInSystemByClient,time);
		resetAllTimePerformanceIndicators(statistics.clientsAtStationQueueByStation,time);
		resetAllTimePerformanceIndicators(statistics.clientsAtStationQueueByStationAndClient,time);
		resetAllTimePerformanceIndicators(statistics.clientsAtStationQueueByClient,time);
		resetAllTimePerformanceIndicators(statistics.clientsAtStationProcessByStation,time);
		resetAllTimePerformanceIndicators(statistics.clientsAtStationProcessByStationAndClient,time);
		resetAllTimePerformanceIndicators(statistics.clientsAtStationProcessByClient,time);
		resetAllTimePerformanceIndicators(statistics.resourceCount,time);
		resetAllTimePerformanceIndicators(statistics.resourceUtilization,time);
		statistics.resourceUtilizationAll.setTime(time);
		if (time==0.0) statistics.resourceUtilizationAll.set(0.0,0);
		resetAllTimePerformanceIndicators(statistics.resourceInDownTime,time);
		resetAllTimePerformanceIndicators(statistics.transporterUtilization,time);
		resetAllTimePerformanceIndicators(statistics.transporterInDownTime,time);
		for (StatisticsSimpleCountPerformanceIndicator indicator: (StatisticsSimpleCountPerformanceIndicator[])statistics.counter.getAll(StatisticsSimpleCountPerformanceIndicator.class)) indicator.reset();
		resetAllDataPerformanceIndicators(statistics.counterBatch);
		resetAllTimePerformanceIndicators(statistics.differentialCounter,time);
		resetAllValuePerformanceIndicators(statistics.clientsCostsWaiting);
		resetAllValuePerformanceIndicators(statistics.clientsCostsTransfer);
		resetAllValuePerformanceIndicators(statistics.clientsCostsProcess);
		resetAllValuePerformanceIndicators(statistics.stationCosts);
		resetAllValuePerformanceIndicators(statistics.resourceTimeCosts);
		resetAllValuePerformanceIndicators(statistics.resourceWorkCosts);
		resetAllValuePerformanceIndicators(statistics.resourceIdleCosts);
		for (StatisticsLongRunPerformanceIndicator indicator: (StatisticsLongRunPerformanceIndicator[])statistics.longRunStatistics.getAll(StatisticsLongRunPerformanceIndicator.class)) {
			indicator.reset();
			indicator.setTime(currentTime);
		}
		resetAllDataPerformanceIndicators(statistics.userStatistics);
		resetAllTimeContinuousPerformanceIndicators(statistics.userStatisticsContinuous,time);
		resetAllTimeContinuousPerformanceIndicators(statistics.userVariables,time);
		for (int i=0;i<runModel.variableNames.length-3;i++) runData.updateVariableValueForStatistics(this,i);
		for (StatisticsStateTimePerformanceIndicator indicator: (StatisticsStateTimePerformanceIndicator[])statistics.stateStatistics.getAll(StatisticsStateTimePerformanceIndicator.class)) indicator.reset();
		for (StatisticsTimeAnalogPerformanceIndicator indicator: (StatisticsTimeAnalogPerformanceIndicator[])statistics.analogStatistics.getAll(StatisticsTimeAnalogPerformanceIndicator.class)) {
			final double value=indicator.getCurrentState();
			indicator.reset();
			indicator.set(time,value);
		}
		for (RunElement element: runModel.elementsFast) if (element instanceof RunElementThroughput) {
			((RunElementThroughput)element).getData(this).reset(currentTime);
		}

		if (statistics.clientsAllWaitingTimesCollector!=null) statistics.clientsAllWaitingTimesCollector.reset();
	}

	/** Statistikdaten des vorherigen Simulationstages */
	private Statistics lastDaysStatistics=null;

	@Override
	public void initDay(final long day, final long dayGlobal, final boolean backgroundMode) {
		currentDay=day;
		/* System.out.println(Thread.currentThread().getName()+": "+day+" "+dayGlobal); */

		if (day>0) { /* Wenn mehrere Wiederholungen simuliert werden und dies nicht der erste Tag ist, Statistik sichern und RunData neu initialisieren */
			lastDaysStatistics=statistics;
			statistics=new Statistics(runModel.correlationRange,runModel.correlationMode,runModel.batchMeansSize,runModel.collectWaitingTimes,runModel.distributionRecordHours,runModel.distributionRecordClientDataValues,runModel.useWelford);
			runData=new RunData(runModel,dynamicLoadBalancer);
		}

		currentTime=0;
		runData.initRun(day,this,runModel.recordIncompleteClients);
	}

	@Override
	public void terminateCleanUp(final long now) {
		super.terminateCleanUp(now);
		runData.doneRun(now,this,runModel.recordIncompleteClients);

		statisticDayDone(statistics);

		if (lastDaysStatistics!=null) {
			/* Wenn es schon Statistik von Vorgängertagen gibt, diese mit diesem Tag zusammenführen */
			statistics.addData(lastDaysStatistics);
			lastDaysStatistics=null;
		}
	}

	@Override
	public void finalTerminateCleanUp(long eventCount) {
		super.finalTerminateCleanUp(eventCount);
		for (Map.Entry<Integer,RunElement> entry: runModel.elements.entrySet()) entry.getValue().finalCleanUp(this);
	}

	/**
	 * Erfasst den Abschluss eines Teil-Simulationslaufes in der Statistik
	 * für die Berechnung der Konfidenzintervalle auf Basis der unabhängigen
	 * Teil-Simulationsläufe.
	 * @param indicator	Statistik-Teil-Indikator
	 */
	private void statisticDayDone(final StatisticsPerformanceIndicator indicator) {
		if (indicator instanceof StatisticsDataPerformanceIndicator) {
			((StatisticsDataPerformanceIndicator)indicator).finishRun();
			return;
		}

		if (indicator instanceof StatisticsDataPerformanceIndicatorWithNegativeValues) {
			((StatisticsDataPerformanceIndicatorWithNegativeValues)indicator).finishRun();
			return;
		}

		if (indicator instanceof StatisticsTimePerformanceIndicator) {
			((StatisticsTimePerformanceIndicator)indicator).finishRun();
			return;
		}
	}

	/**
	 * Erfasst den Abschluss eines Teil-Simulationslaufes in der Statistik
	 * für die Berechnung der Konfidenzintervalle auf Basis der unabhängigen
	 * Teil-Simulationsläufe.
	 * @param statistics	Statistikobjekt
	 */
	private void statisticDayDone(final Statistics statistics) {
		for (StatisticsPerformanceIndicator indicator: statistics.getAllPerformanceIndicators()) {
			if (indicator instanceof StatisticsMultiPerformanceIndicator) {
				final StatisticsMultiPerformanceIndicator multi=(StatisticsMultiPerformanceIndicator)indicator;
				final StatisticsPerformanceIndicator[] sub=multi.getAll();
				if (sub!=null) for (StatisticsPerformanceIndicator subData: sub) statisticDayDone(subData);

			}

			statisticDayDone(indicator);
		}
	}

	@Override
	protected SimLogging getLogger(final File logFile) {
		return new MultiTypeTextLogger(logFile,true,SetupData.getSetup().singleLineEventLog,true,true,true,new String[]{Language.tr("Simulation.Log.Title")});
	}

	/**
	 * Bricht die Simulation sofort ab und verbucht dies als Fehler.
	 * @param message	Meldung, die in Logdatei und in die Warnungen der Statistik aufgenommen werden soll.
	 */
	public void doEmergencyShutDown(final String message) {
		statistics.simulationData.emergencyShutDown=true;
		addWarning(Language.tr("Simulation.RunTimeError").toUpperCase()+": "+message);
		logEventExecution(Language.tr("Simulation.Log.Abort"),-1,message);
		doShutDown();
	}

	/**
	 * Bricht die Simulation sofort ab und verbucht dies als Fehler.
	 * @param message	Meldung, die in Logdatei und in die Warnungen der Statistik aufgenommen werden soll.
	 * @param station	Station an der der Fehler aufgetreten ist (kann <code>null</code> sein)
	 */
	public void doEmergencyShutDown(final String message, final ModelElement station) {
		if (station==null) doEmergencyShutDown(message); else doEmergencyShutDown("id="+station.getId()+": "+message);
	}

	/**
	 * Erfasst während der Simulation eine Warnungsmeldung
	 * @param message	Neue Warnungsmeldung
	 */
	public void addWarning(final String message) {
		statistics.simulationData.addWarning(message);
	}

	/**
	 * Bricht die Simulation sofort ab, ohne dies als Fehler zu verbuchen.
	 */
	public void doShutDown() {
		if (eventManager!=null) eventManager.deleteAllEvents();
		runData.stopp=true;
	}

	/**
	 * Wenn die Simulation bei einem Rechenfehler abgebrochen werden soll, so wird dies ausgeführt. (Sonst erfolgt keine Verarbeitung.)
	 * @param calc	Rechenausdruck, der ausgewertet werden sollte
	 * @param station	Station, an der der Rechenausdruck ausgewertet werden sollte
	 */
	public void calculationErrorStation(final ExpressionCalc calc, final RunElement station) {
		if (!runModel.stoppOnCalcError) return;
		doEmergencyShutDown(String.format(Language.tr("Simulation.CalcError.Station"),station.name,calc.getText()));
	}

	/**
	 * Wenn die Simulation bei einem Rechenfehler abgebrochen werden soll, so wird dies ausgeführt. (Sonst erfolgt keine Verarbeitung.)
	 * @param calc	Rechenausdruck, der ausgewertet werden sollte
	 * @param stationData	Station, an der der Rechenausdruck ausgewertet werden sollte
	 */
	public void calculationErrorStation(final ExpressionCalc calc, final RunElementData stationData) {
		if (!runModel.stoppOnCalcError) return;
		doEmergencyShutDown(String.format(Language.tr("Simulation.CalcError.Station"),stationData.station.name,calc.getText()));
	}

	/**
	 * Wenn die Simulation bei einem Rechenfehler abgebrochen werden soll, so wird dies ausgeführt. (Sonst erfolgt keine Verarbeitung.)
	 * @param calc	Rechenausdruck, der ausgewertet werden sollte
	 * @param logStationName	Name der Station, an der der Rechenausdruck ausgewertet werden sollte
	 */
	public void calculationErrorStation(final ExpressionCalc calc, final String logStationName) {
		if (!runModel.stoppOnCalcError) return;
		doEmergencyShutDown(String.format(Language.tr("Simulation.CalcError.Station"),logStationName,calc.getText()));
	}

	/**
	 * Wenn die Simulation bei einem Rechenfehler abgebrochen werden soll, so wird dies ausgeführt. (Sonst erfolgt keine Verarbeitung.)
	 * @param calc	Rechenausdruck, der ausgewertet werden sollte
	 * @param transporterName	Name der Transportergruppe, bei der der Rechenausdruck ausgewertet werden sollte
	 */
	public void calculationErrorTransporter(final ExpressionCalc calc, final String transporterName) {
		if (!runModel.stoppOnCalcError) return;
		doEmergencyShutDown(String.format(Language.tr("Simulation.CalcError.Transporter"),transporterName,calc.getText()));
	}

	/**
	 * Wenn die Simulation bei einem Rechenfehler abgebrochen werden soll, so wird dies ausgeführt. (Sonst erfolgt keine Verarbeitung.)
	 * @param calc	Rechenausdruck, der ausgewertet werden sollte
	 * @param resourceName	Name der Ressource, bei der der Rechenausdruck ausgewertet werden sollte
	 */
	public void calculationErrorRessource(final ExpressionCalc calc, final String resourceName) {
		if (!runModel.stoppOnCalcError) return;
		doEmergencyShutDown(String.format(Language.tr("Simulation.CalcError.Resource"),resourceName,calc.getText()));
	}

	/**
	 * Wenn die Simulation bei einem Rechenfehler innerhalb eines Vergleichs abgebrochen werden soll, so wird dies ausgeführt. (Sonst erfolgt keine Verarbeitung.)
	 * @param calc	Rechenausdruck, der im Rahmen eines Vergleichs ausgewertet werden sollte
	 */
	public void calculationErrorEval(final ExpressionCalc calc) {
		if (!runModel.stoppOnCalcError) return;
		doEmergencyShutDown(String.format(Language.tr("Simulation.CalcError.Eval"),calc.getText()));
	}

	@Override
	public void catchException(final String text) {
		doEmergencyShutDown(text);
	}

	@Override
	public void catchOutOfMemory(final String text) {
		doEmergencyShutDown(Language.tr("Simulation.OutOfMemory")+"\n"+text);
	}

	/**
	 * Maximal zulässige Anzahl an Kunden im System.<br>
	 * Wird einmalig von {@link #testMaxAllowedClientsInSystem()} berechnet und
	 * dann nur noch aus diesem Feld ausgelesen.
	 * @see #testMaxAllowedClientsInSystem()
	 */
	private int maxAllowed=-1;

	/**
	 * Prüft, ob die maximal zulässige Anzahl an Kunden im System eingehalten wird.
	 * @return	Liefert im Erfolgsfall <code>true</code>. Im Fehlerfall wird die Simulation per {@link #doEmergencyShutDown(String)} abgebrochen und es wird <code>false</code> zurückgeliefert.
	 */
	public boolean testMaxAllowedClientsInSystem() {
		final int count=runData.clients.getClientsInSystem();
		if (maxAllowed<=0)  {
			maxAllowed=FastMath.max(RunDataClients.MAX_CLIENTS_IN_SYSTEM_MULTI_CORE,RunDataClients.MAX_CLIENTS_IN_SYSTEM_SINGLE_CORE/threadCount);
			if (logging instanceof CallbackLogger) {
				maxAllowed=RunDataClients.MAX_CLIENTS_IN_SYSTEM_ANIMATION;
			}
		}

		if (count>maxAllowed) {
			doEmergencyShutDown(String.format(Language.tr("Simulation.Log.ToManyClientsInSystem.Info"),NumberTools.formatLong(count)));
			return false;
		}

		return true;
	}
}

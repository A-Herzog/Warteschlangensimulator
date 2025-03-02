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
package simulator;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.util.FastMath;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.tools.DistributionRandomNumber;
import mathtools.distribution.tools.SeedableThreadLocalRandomGenerator;
import mathtools.distribution.tools.ThreadLocalRandomGenerator;
import simcore.SimData;
import simcore.SimThread;
import simcore.SimulatorBase;
import simcore.logging.SimLogging;
import simulator.StartAnySimulator.PrepareError;
import simulator.coreelements.RunElement;
import simulator.editmodel.EditModel;
import simulator.runmodel.DynamicLoadBalancer;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import simulator.statistics.Statistics;
import statistics.StatisticsDataPerformanceIndicator;
import statistics.StatisticsSimpleValuePerformanceIndicator;
import tools.SetupData;
import tools.UsageStatistics;
import ui.speedup.BackgroundPrepareCompiledClasses;
import ui.statistics.StatisticViewerOverviewText;

/**
 * Vollständiger Multi-Core-fähiger Simulator
 * @author Alexander Herzog
 */
public class Simulator extends SimulatorBase implements AnySimulator {
	/**
	 * Wurde bereits mindestens einmal {@link #start(boolean)}
	 * oder {@link #start()} aufgerufen?
	 */
	private static boolean simulationStarted=false;

	/**
	 * Wurde bereits mindestens einmal {@link #start(boolean)}
	 * oder {@link #start()} aufgerufen?
	 * @return	Liefert <code>true</code>, wenn bereits eine Simulation gestartet wurde (und das Hintergrund-Vorabladen der Klassen durch {@link BackgroundPrepareCompiledClasses} keinen Sinn mehr ergibt).
	 * @see #simulationStarted
	 * @see BackgroundPrepareCompiledClasses
	 */
	public static boolean isSimulationStarted() {
		return simulationStarted;
	}

	/**
	 * Stellt ein, dass mindestens einmal eine Simulation gestartet wurde.
	 * @see #isSimulationStarted()
	 */
	private static void setSimulationStarted() {
		simulationStarted=true;
	}

	/**
	 * Welche Arten von Ereignissen sollen erfasst werden?
	 * @author Alexander Herzog
	 */
	public enum LogType {
		/** Ankünfte von Kunden an Stationen */
		ARRIVAL,
		/** Abgänge von Kunden von Stationen */
		LEAVE,
		/** Stationsspezifische Informationen */
		STATIONINFO,
		/** Informationen in Bezug auf das Gesamtmodell */
		SYSTEMINFO,
	}

	/**
	 * Erfassung von Ereignissen aller Typen
	 * @see Simulator.LogType
	 */
	public static final Set<LogType> logTypeFull=new HashSet<>(Arrays.asList(LogType.values()));

	/**
	 * Laufzeit-Modell (global für alle Threads)
	 */
	protected RunModel runModel;

	/**
	 * Editor-Modell (wird nur benötigt, da es am Ende mit in die Statistik aufgenommen wird)
	 */
	protected EditModel editModel;

	/**
	 * Pfad zur zugehörigen Modelldatei (als Basis für relative Pfade in Ausgabeelementen)
	 */
	protected String editModelPath;

	/**
	 * Steht hier ein Wert ungleich <code>null</code>, so wird in den Single-Core-Modus geschaltet und der Lauf wird in der angegebenen Log-Datei aufgezeichnet
	 */
	protected final SimLogging logging;

	/**
	 * Steht hier ein Wert ungleich <code>null</code> und ist das Logging ({@link Simulator#logging}) aktiv, so werden nur Daten zu den Stationen, deren IDs hier angegeben sind, erfasst
	 */
	public final int[] loggingIDs;

	/**
	 * Welche Arten von Ereignissen sollen erfasst werden? (<code>null</code> bedeutet: alles erfassen)
	 */
	public final Set<LogType> logType;

	/**
	 * Da die Statistik nur einmal aus den Daten erhoben wird, wird diese für wiederholte Aufrufe von <code>getStatistic()</code> hier aufgehoben
	 * @see #getStatistic()
	 */
	private Statistics statistics=null;

	/**
	 * Erfassung der Anzahl an simulierten Kunden in der Statistik?
	 * @see #doNotRecordSimulatedClientsToStatistics()
	 * @see UsageStatistics
	 */
	private boolean recordSimulatedClientsToStatistics=true;

	/**
	 * Gemeinsamer Load-Balancer für alle Threads
	 * @see #getBalancerInfo()
	 */
	private DynamicLoadBalancer dynamicLoadBalancer;

	/**
	 * Minimale Anzahl an Ankünften pro Thread
	 * (für die Festlegung der Anzahl an parallelen Threads)
	 * @see #getAllowMaxCore(EditModel, boolean, int)
	 */
	private static final int MIN_ARRIVALS_PER_THREAD=200;

	/**
	 * Liefert die Maximalanzahl an zu verwendenden Simulationsthreads
	 * @param editModel	Editor-Modell
	 * @param multiCore	Mehrkern-Simulation laut Setup verwenden?
	 * @param maxCoreCount	Maximal zu verwendende Kernanzahl (laut Setup)
	 * @return	Liefert die laut Modell und Setup maximal zulässige Anzahl an Rechenthreads)
	 */
	private static int getAllowMaxCore(final EditModel editModel, final boolean multiCore, final int maxCoreCount) {
		if (!multiCore) return 1;
		final int a=Math.max(1,SetupData.getSetup().useMultiCoreSimulationMaxCount);
		final int b=Math.max(1,maxCoreCount);
		int threadCount=Math.min(a,b);

		/* Simulation nicht aufteilen, wenn nur sehr wenige Ankünfte geplant sind. */
		if (editModel.useClientCount) {
			threadCount=(int)Math.min(threadCount,Math.max(1,editModel.clientCount/MIN_ARRIVALS_PER_THREAD));
		}

		/* Bei mehreren Wiederholungen auf die Anzahl an Läufen abstimmen. */
		if (!editModel.allowMultiCore()) {
			threadCount=Math.min(threadCount,Runtime.getRuntime().availableProcessors());
			if (threadCount>editModel.repeatCount) threadCount=editModel.repeatCount;
		}
		if (!SetupData.getSetup().useMultiCoreSimulationOnRepeatedSimulations && editModel.repeatCount>1) {
			if (threadCount>editModel.repeatCount) threadCount=editModel.repeatCount;
		}

		return threadCount;
	}

	/**
	 * Datenmodelle für einzelne Threads trennen (benötigt mehr Speicher) um auf NUMA-Systemen mehr Performance zu erreichen.
	 * @return	Liefert <code>true</code>, wenn jeder Thread mit einem eigenen Modell rechnen soll
	 */
	private static boolean getNUMAAware() {
		return SetupData.getSetup().useNUMAMode;
	}

	/**
	 * Konstruktor der Klasse <code>Simulator</code>
	 * @param multiCore	Wird hier <code>true</code> übergeben, so wird auf allen verfügbaren CPU-Kernen gerechnet. (Ausnahme: Wird in <code>logFile</code> ein Wert ungleich <code>null</code> übergeben, so wird stets nur ein Kern verwendet.)
	 * @param editModel	Editor-Modell
	 * @param editModelPath	Pfad zur zugehörigen Modelldatei (als Basis für relative Pfade in Ausgabeelementen)
	 * @param logging	Wird hier ein Wert ungleich <code>null</code> übergeben, so wird der Lauf durch den angegebenen Logger aufgezeichnet; ansonsten erfolgt nur die normale Aufzeichnung in der Statistik
	 * @param loggingIDs	Liste der Stations-IDs deren Ereignisse beim Logging erfasst werden sollen (nur von Bedeutung, wenn das Logging als solches aktiv ist; kann <code>null</code> sein, dann werden die Ereignisse aller Stationen erfasst)
	 * @param logType	Welche Arten von Ereignissen sollen erfasst werden? (<code>null</code> bedeutet: alles erfassen)
	 */
	public Simulator(final boolean multiCore, final EditModel editModel, final String editModelPath, final SimLogging logging, final int[] loggingIDs, final Set<LogType> logType) {
		super(getAllowMaxCore(editModel,multiCore && logging==null,Integer.MAX_VALUE),false,getNUMAAware());
		this.editModel=editModel;
		this.editModelPath=editModelPath;
		this.logging=logging;
		this.loggingIDs=loggingIDs;
		this.logType=logType;
	}

	/**
	 * Konstruktor der Klasse <code>Simulator</code>
	 * @param maxCoreCount	Gibt die maximale Anzahl an zu verwendenden Threads an. (Wird in <code>logFile</code> ein Wert ungleich <code>null</code> übergeben, so wird stets nur ein Kern verwendet.)
	 * @param editModel	Editor-Modell
	 * @param editModelPath	Pfad zur zugehörigen Modelldatei (als Basis für relative Pfade in Ausgabeelementen)
	 * @param logging	Wird hier ein Wert ungleich <code>null</code> übergeben, so wird der Lauf durch den angegebenen Logger aufgezeichnet; ansonsten erfolgt nur die normale Aufzeichnung in der Statistik
	 * @param loggingIDs	Liste der Stations-IDs deren Ereignisse beim Logging erfasst werden sollen (nur von Bedeutung, wenn das Logging als solches aktiv ist; kann <code>null</code> sein, dann werden die Ereignisse aller Stationen erfasst)
	 * @param logType	Welche Arten von Ereignissen sollen erfasst werden? (<code>null</code> bedeutet: alles erfassen)
	 */
	public Simulator(final int maxCoreCount, final EditModel editModel, final String editModelPath, final SimLogging logging, final int[] loggingIDs, final Set<LogType> logType) {
		super(getAllowMaxCore(editModel,logging==null,maxCoreCount),false,getNUMAAware());
		this.editModel=editModel;
		this.editModelPath=editModelPath;
		this.logging=logging;
		this.loggingIDs=loggingIDs;
		this.logType=logType;
	}

	/**
	 * Konstruktor der Klasse <code>Simulator</code>
	 * @param editModel	Editor-Modell
	 * @param editModelPath	Pfad zur zugehörigen Modelldatei (als Basis für relative Pfade in Ausgabeelementen)
	 * @param logging	Wird hier ein Wert ungleich <code>null</code> übergeben, so wird der Lauf durch den angegebenen Logger aufgezeichnet; ansonsten erfolgt nur die normale Aufzeichnung in der Statistik
	 * @param loggingIDs	Liste der Stations-IDs deren Ereignisse beim Logging erfasst werden sollen (nur von Bedeutung, wenn das Logging als solches aktiv ist; kann <code>null</code> sein, dann werden die Ereignisse aller Stationen erfasst)
	 * @param logType	Welche Arten von Ereignissen sollen erfasst werden? (<code>null</code> bedeutet: alles erfassen)
	 */
	public Simulator(final EditModel editModel, final String editModelPath, final SimLogging logging, final int[] loggingIDs, final Set<LogType> logType) {
		this(SetupData.getSetup().useMultiCoreSimulation && (editModel.allowMultiCore() || editModel.repeatCount>1),editModel,editModelPath,logging,loggingIDs,logType);
	}

	/**
	 * Bereitet den globalen Zufallszahlengenerator für die Simulation vor.
	 * @param fixedSeed	Soll ein fester Startwert verwendet werden?
	 */
	private static void prepareStatic(final boolean fixedSeed) {
		if (fixedSeed) {
			DistributionRandomNumber.generator=new SeedableThreadLocalRandomGenerator();
		} else {
			DistributionRandomNumber.generator=new ThreadLocalRandomGenerator();
		}
	}

	/**
	 * Bereitet die Simulation vor
	 * @return	Liefert <code>null</code> zurück, wenn die Simulation erfolgreich vorbereitet werden konnte, sonst eine Fehlermeldung
	 */
	public PrepareError prepare() {
		return prepare(true);
	}

	/**
	 * Bereitet die Simulation vor
	 * @param allowLoadBalancer	Über diesen Parameter kann die Verwendung des Load-Balancers generell unterbunden werden (<code>false</code>). Andernfalls (<code>true</code>) wird gemäß Setup und Modell entschieden.
	 * @return	Liefert <code>null</code> zurück, wenn die Simulation erfolgreich vorbereitet werden konnte, sonst eine Fehlermeldung
	 */
	public PrepareError prepare(final boolean allowLoadBalancer) {
		prepareStatic(editModel.useFixedSeed);

		final Object obj=RunModel.getRunModel(editModel,editModelPath,false,SetupData.getSetup().useMultiCoreSimulation);
		if (obj instanceof StartAnySimulator.PrepareError) return (StartAnySimulator.PrepareError)obj;
		runModel=(RunModel)obj;

		if (SetupData.getSetup().useDynamicThreadBalance && allowLoadBalancer) {
			if (runModel.repeatCount==1 && threadCount>1 && runModel.clientCount>0) dynamicLoadBalancer=new DynamicLoadBalancer(runModel.clientCount,threadCount);
		}

		return null;
	}

	/**
	 * Liefert die maximale relative Abweichung an simulierten Kunden pro Thread (bei der Verwendung einer dynamischen Thread-Balance).
	 * @return	Maximale relative Abweichung an simulierten Kunden pro Thread
	 */
	private double getBalancerInfo() {
		if (dynamicLoadBalancer==null) return 0;

		final long clients0=((SimulationData)threads[0].simData).runData.clientsArrived;
		long min=clients0;
		long max=clients0;
		long sum=clients0;
		for (int i=1;i<threadCount;i++) {
			final long clients=((SimulationData)threads[i].simData).runData.clientsArrived;
			if (clients<min) min=clients;
			if (clients>max) max=clients;
			sum+=clients;
		}
		if (sum==0) return 0;

		return ((double)(max-min)*threadCount)/sum;
	}

	/**
	 * Liefert die Anzahl an Kundenankünften pro Thread (bei der Verwendung einer dynamischen Thread-Balance).
	 * @return	Kundenankünfte pro Thread (oder <code>null</code>, wenn keine Verteilung der Kundenankünfte ermittelt werden konnte)
	 */
	private long[] getBalancerData() {
		if (dynamicLoadBalancer==null) return null;

		long sum=0;
		final long[] results=new long[threadCount];
		for (int i=0;i<threadCount;i++) {
			final long clients=((SimulationData)threads[i].simData).runData.clientsArrived;
			results[i]=clients;
			sum+=clients;
		}
		if (sum==0) return null;

		return results;
	}

	/**
	 * Schreibt am Simulationsende die Basisdaten des Simulationsprozesses
	 * in die Statistik
	 * @param statistics	Statistik in die die Daten geschrieben werden sollen
	 * @see #collectStatistics()
	 */
	private void writeBaseDataToStatistics(final Statistics statistics) {
		statistics.editModel=editModel.clone();
		statistics.editModel.version=EditModel.systemVersion;
		if (statistics.editModel.author==null || statistics.editModel.author.trim().isEmpty()) statistics.editModel.author=EditModel.getDefaultAuthor();

		statistics.simulationData.runUser=EditModel.getDefaultAuthor();
		statistics.simulationData.runTime=runTime;
		statistics.simulationData.runThreads=threadCount;
		statistics.simulationData.runEvents=getEventCount();
		statistics.simulationData.runRepeatCount=editModel.repeatCount;
		statistics.simulationData.numaAwareMode=getNUMAAware();
		statistics.simulationData.threadRunTimes=getThreadRuntimes();
		statistics.simulationData.threadDynamicBalance=getBalancerInfo();
		final long[] balanceData=getBalancerData();
		if (balanceData!=null) statistics.simulationData.threadDynamicBalanceData=balanceData;
	}

	/**
	 * Wird intern verwendet, um die Statistikdaten von den Threads einzusammeln.
	 * Diese Funktion wird von <code>getStatistic</code> aufgerufen. <code>getStatistic</code> speichert die einmal erhobenen
	 * Daten für spätere Abrufe zwischen, so dass <code>collectStatistics</code> nur einmal aufgerufen werden muss.
	 * @return	Liefert eine Statistikobjekt, welches die zusammengefassten Statistikdaten über alle Threads enthält
	 */
	protected Statistics collectStatistics() {
		final Statistics statistics;

		/* Wenn mit nur einem Thread gerechnet wurde, Daten nicht noch mal umkopieren. */
		if (threads.length==1) {
			statistics=((SimulationData)threads[0].simData).statistics;

			/* Basisdaten zum Modell und zum Simulationslauf festhalten */
			writeBaseDataToStatistics(statistics);
		} else {
			statistics=new Statistics(runModel.correlationRange,runModel.correlationMode,runModel.batchMeansSize,runModel.collectWaitingTimes,runModel.distributionRecordHours,runModel.stateRecordSize,runModel.distributionRecordClientDataValues,runModel.useWelford);

			/* Basisdaten zum Modell und zum Simulationslauf festhalten */
			writeBaseDataToStatistics(statistics);

			/* Daten von den Threads einsammeln */
			long count1=0;
			long count2=0;
			double waiting1=0;
			double waiting2=0;
			final List<StatisticsDataPerformanceIndicator> partialWaitingTime=new ArrayList<>();
			for (int i=0;i<threads.length;i++) {
				final Statistics partialStatistics=((SimulationData)threads[i].simData).statistics;
				partialWaitingTime.add(partialStatistics.clientsAllWaitingTimes);
				statistics.addData(partialStatistics);
				final long count=partialStatistics.clientsAllWaitingTimes.getCount();
				final double waiting=partialStatistics.clientsAllWaitingTimes.getSum();
				if (count>0) { /* Daten nur berücksichtigen, wenn in dem Thread überhaupt Ergebnisse angefallen sind */
					if (i<threads.length/2) {count1+=count; waiting1+=waiting;} else {count2+=count; waiting2+=waiting;}
				}
			}

			if (count1+count2>=100_000) { /* Nur wenn die Simulation hinreichend lange gelaufen ist */
				/* Warnung, wenn die mittlere Wartezeit der ersten Hälfte von der zweiten Hälfte abweicht */
				if (count1>0 && count2>0 && waiting1>0 && waiting2>0) {
					if (!editModel.useTerminationCondition && count1>0 && count2>0) {
						final double mean1=waiting1/count1;
						final double mean2=waiting2/count2;
						final double fullMean=(waiting1+waiting2)/(count1+count2);
						if (fullMean>0) {
							final double delta=Math.abs(mean1-mean2)/fullMean;
							/* System.out.println("Delta="+NumberTools.formatPercent(delta,2)); */
							if (delta>0.2) statistics.simulationData.addWarning(String.format(Language.tr("Statistics.Warnings.SimulationRunNotLongEnough"),NumberTools.formatPercent(delta)));
						}
					}
				}
			}

			/* Aufzeichnung der Thread-basierenden Konfidenzniveaus für die Wartezeiten */
			final double[] confidenceLevels=StatisticViewerOverviewText.getConfidenceLevels();
			final double[] halfWidth=StatisticsDataPerformanceIndicator.getConfidenceHalfWideByMultiStatistics(partialWaitingTime.toArray(StatisticsDataPerformanceIndicator[]::new),statistics.clientsAllWaitingTimes,confidenceLevels);
			if (halfWidth!=null) for (int i=0;i<halfWidth.length;i++) {
				((StatisticsSimpleValuePerformanceIndicator)statistics.threadBasedConfidence.get(NumberTools.formatPercent(1-confidenceLevels[i]))).set(halfWidth[i]);
			}
		}

		/* Aufbereitete Daten berechnen */
		statistics.calc();

		/* Nutzungsstatistik erfassen */
		if (recordSimulatedClientsToStatistics) {
			long sum=0;
			for (StatisticsDataPerformanceIndicator indicator: (StatisticsDataPerformanceIndicator[])statistics.clientsInterarrivalTime.getAll(StatisticsDataPerformanceIndicator.class)) sum+=indicator.getCount();
			UsageStatistics.getInstance().addSimulationClients(sum);
		}

		return statistics;
	}

	/**
	 * Liefert einen unvollständigen Statistikdatensatz bezogen auf den
	 * aktuellen Stand einer laufenden Simulation.<br>
	 * (Voraussetzungen: Die Simulation muss im Single-Core-Modus laufen
	 * und sie muss momentan pausiert sein.)
	 * @return	Liefert im Erfolgsfall ein von dem internen Zustand entkoppeltes Statistikobjekt oder <code>null</code>, wenn die Voraussetzungen nicht erfüllt sind
	 */
	public Statistics getIncompleteStatistic() {
		if (!isPaused() || !isRunning()) return null;
		if (threads.length!=1) return null;

		final Statistics statistics=new Statistics(runModel.correlationRange,runModel.correlationMode,runModel.batchMeansSize,runModel.collectWaitingTimes,runModel.distributionRecordHours,runModel.stateRecordSize,runModel.distributionRecordClientDataValues,runModel.useWelford);
		writeBaseDataToStatistics(statistics);
		statistics.addData(((SimulationData)threads[0].simData).statistics);
		statistics.calc();

		return statistics;
	}

	/**
	 * Liefert nach Abschluss der Simulation die Statistikergebnisse zurück.
	 * @return	Statistik-Objekt, welches alle Daten des Simulationslaufs enthält
	 */
	@Override
	public final Statistics getStatistic() {
		finalizeRun();
		if (statistics==null) {
			statistics=collectStatistics();
			for (int i=0;i<threads.length;i++) threads[i]=null;
			runModel=null;
		}
		return statistics;
	}

	@Override
	protected SimData getSimDataForThread(final int threadNr, final int threadCount) {
		final SimData data;
		final RunModel runModel;
		if (numaAware && threadCount>1) {
			final Object obj=RunModel.getRunModel(editModel,editModelPath,false,SetupData.getSetup().useMultiCoreSimulation);
			runModel=(RunModel)obj;
		} else {
			runModel=this.runModel;
		}
		data=new SimulationData(threadNr,threadCount,this,runModel,null,dynamicLoadBalancer);

		if (logging!=null) {
			final SimulationData simData=(SimulationData)data;

			simData.activateLogging(logging);
			if (loggingIDs!=null && loggingIDs.length>0) {
				final boolean[] loggingIDsFast=simData.loggingIDs=new boolean[runModel.elementsFast.length];
				for (int id: loggingIDs) loggingIDsFast[id]=true;
			}
			simData.logArrival=(logType==null || logType.contains(Simulator.LogType.ARRIVAL));
			simData.logDeparture=(logType==null || logType.contains(Simulator.LogType.LEAVE));
			simData.logInfoStation=(logType==null || logType.contains(Simulator.LogType.STATIONINFO));
			simData.logInfoSystem=(logType==null || logType.contains(Simulator.LogType.SYSTEMINFO));
		}
		return data;
	}

	/**
	 * Startet die Simulationssthreads mit normaler Priorität.
	 * @see SimulatorBase#start(boolean)
	 */
	public final void start() {
		start(false);
	}

	/**
	 * Startet die Simulationssthreads mit normaler Priorität.
	 * @param startPaused	Startet die Simulation im Pause/Einzelschritt-Modus
	 * @see SimulatorBase#start(boolean)
	 */
	@Override
	public final void start(final boolean startPaused) {
		setSimulationStarted();
		if (runModel==null) return;
		super.start(!SetupData.getSetup().highPriority,startPaused);
	}

	/**
	 * Liefert die Gesamtanzahl an Wiederholungen in der Simulation.
	 * @return	Anzahl an Wiederholungen (über alle Threads) der Simulation.
	 */
	@Override
	public final long getSimDaysCount() {
		return runModel.clientCount*runModel.repeatCount; /* wird überhaupt nicht verwendet, da wir immer im LongRun-Modus sind */
	}

	/**
	 * Liefert die Gesamtanzahl an zu simulierenden Kundenankünften
	 * @return	Gesamtanzahl an zu simulierenden Kundenankünften
	 */
	@Override
	public final long getCountClients() {
		if (runModel==null) return 0;
		if (runModel.clientCountDiv==0) {
			if (threads==null || threads.length==0) return 0;
			final RunModel threadRunModel;
			if (threads[0]==null || threads[0].simData==null) {
				threadRunModel=runModel;
			} else {
				threadRunModel=((SimulationData)threads[0].simData).runModel;
			}
			if (threadRunModel==null) return 0;
			runModel.clientCountDiv=threadRunModel.clientCountDiv;
		}
		return (runModel.realArrivingClientCount+runModel.clientCountDiv*FastMath.round(runModel.warmUpTime*runModel.realArrivingClientCount))*runModel.repeatCount;
	}

	/**
	 * Liefert die Anzahl an bislang simulierten Kundenankünften
	 * @return	Anzahl an bislang simulierten Kundenankünften
	 */
	@Override
	public final long getCurrentClients() {
		long sum=0;
		for (SimThread thread: threads) {
			if (thread==null) continue;
			final SimulationData data=(SimulationData)(thread.simData);
			if (data==null) continue;
			if (runModel.clientCountDiv==0 && ((SimulationData)thread.simData).runModel!=null) runModel.clientCountDiv=((SimulationData)thread.simData).runModel.clientCountDiv;
			if (runModel.clientCount>0 && thread.currentDay>1) sum+=(thread.currentDay-1)*FastMath.round(runModel.realArrivingClientCount*(1+runModel.warmUpTime)/runModel.clientCountDiv);
			if (!data.runData.isWarmUp && runModel.clientCount>0) sum+=FastMath.round(runModel.warmUpTime*runModel.clientCount);
			sum+=data.runData.clientsArrived;
		}
		return sum;
	}

	/**
	 * Liefert die aktuelle Anzahl an Kunden im System
	 * @return	Aktuelle Anzahl an Kunden im System
	 */
	@Override
	public int getCurrentWIP() {
		int sum=0;
		int threadCount=0;
		for (SimThread thread: threads) {
			if (thread==null) continue;
			final SimulationData data=(SimulationData)(thread.simData);
			if (data==null) continue;
			sum+=data.statistics.clientsInSystem.getCurrentState();
			threadCount++;
		}
		return (threadCount==0)?0:sum/threadCount;
	}

	/**
	 * Liefert das im Konstruktor angegebene Editor-Modell zurück
	 * @return	Editor-Modell
	 */
	public EditModel getEditModel() {
		return editModel;
	}

	/**
	 * Liefert das aktuell verwendete Laufzeit-Modell zurück
	 * @return	Laufzeit-Modell
	 */
	public RunModel getRunModel() {
		return runModel;
	}

	/**
	 * Erstellt einen Simulationdatenobjekt aus einem Statistikobjekt.<br>
	 * So können nach Simulationsende (auf Basis der Statistik) dennoch die Rechnenfunktionen zur Abfrage bestimmter Werte (die eigentlich auf dem Simulationsdatenobjekt operieren) verwendet werden.
	 * @param statistics	Statistikobjekt, aus dem wieder ein Simulationsdatenobjekt gewonnen werden soll
	 * @return	Simulationsdatenobjekt das sich so verhält als wäre es die Basis für das angegebene Statistikobjekt
	 */
	public static SimulationData getSimulationDataFromStatistics(final Statistics statistics) {
		if (statistics==null) return null;
		final Simulator simulator=new Simulator(statistics.editModel,null,null,null,logTypeFull);
		if (simulator.prepare()!=null) return null;
		final SimulationData simData=new SimulationData(0,simulator.threads.length,simulator,simulator.runModel,statistics,null);

		simData.runData.initRun(0,simData,simData.runModel.recordIncompleteClients);
		for (RunElement station: simData.runModel.elementsFast) if (station!=null) simData.runData.explicitInitStatistics(simData,station);

		return simData;
	}

	/**
	 * Deaktiviert die Zählung der simulierten Kunden in der Statistik.<br>
	 * Diese Funktion muss vor {@link #collectStatistics()} aufgerufen werden.
	 */
	public void doNotRecordSimulatedClientsToStatistics() {
		recordSimulatedClientsToStatistics=false;
	}

	/**
	 * Aktive Nanosekunden pro Rechenthread.
	 * @see #getNanos()
	 */
	private long[] nanos;

	/**
	 * Summe der Nanosekunden, die die Rechenthreads aktiv waren.
	 * @return	Mit Simulation verbrachte Nanosekunden über alle Threads zusammen
	 */
	public long getNanos() {
		long sum=0;
		final ThreadMXBean bean=ManagementFactory.getThreadMXBean();
		if (threads!=null) {
			if (nanos==null) nanos=new long[threadCount];
			for (int i=0;i<threadCount;i++) {
				final Thread thread=threads[i];
				if (thread!=null) nanos[i]=FastMath.max(nanos[i],bean.getThreadCpuTime(thread.getId()));
				sum+=nanos[i];
			}
		}
		return sum;
	}

	/**
	 * Liefert, sofern die Simulation nur mit einem Thread arbeitet,
	 * die aktuelle Simulationszeit in Sekunden innerhalb dieses Threads.
	 * @return	Aktuelle Simulationszeit (in ms) oder -1, wenn keine Zeit ermittelt werden konnte.
	 */
	public double getSingleThreadCurrentTime() {
		if (threadCount>1) return -1;
		if (threads==null || threads[0]==null) return -1;
		return threads[0].simData.currentTime*((SimulationData)threads[0].simData).runModel.scaleToSeconds;
	}
}

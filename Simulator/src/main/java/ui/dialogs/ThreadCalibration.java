/**
 * Copyright 2021 Alexander Herzog
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
package ui.dialogs;

import java.util.Arrays;
import java.util.function.Consumer;

import language.Language;
import mathtools.NumberTools;
import mathtools.Table;
import simulator.Simulator;
import simulator.StartAnySimulator;
import simulator.editmodel.EditModel;
import simulator.examples.EditModelExamples;
import simulator.statistics.Statistics;
import tools.SetupData;

/**
 * Ermittelt die Simulationsleistung in Abhängigkeit von der Anzahl der eingesetzten Threads.
 * @author Alexander Herzog
 */
public class ThreadCalibration {
	/**
	 * Globales Setupdaten-Singleton
	 */
	private final SetupData setup;

	/**
	 * Anzahl an verfügbaren logischen CPU-Kernen
	 */
	public static final int coreCount;

	/**
	 * Für die Kalibrierung zu verwendendes Modell
	 */
	private final EditModel model;

	/**
	 * Pfad zur zugehörigen Modelldatei (als Basis für relative Pfade in Ausgabeelementen)
	 */
	private final String editModelPath;

	/**
	 * Ausgabe-Callback für Statusmeldungen (kann <code>null</code> sein)
	 * @see #output(String)
	 */
	private final Consumer<String> output;

	/**
	 * Angestrebte Laufzeit für das Modell bei Verwendung aller CPU-Kerne<br>
	 * (wird verwendet, um die Anzahl an zu simulierenden Ankünften festzulegen)
	 * @see #processInner()
	 */
	private final int baseRunTime;

	/**
	 * Wurde die Verarbeitung abgebrochen?
	 * @see #cancel()
	 */
	private boolean canceled=false;

	/**
	 * Leistung pro Thread-Anzahl
	 */
	private volatile long[] performance;

	/**
	 * Verarbeitungs-Thread
	 * @see #start(Runnable)
	 * @see #process()
	 */
	private volatile Thread thread;

	/**
	 * Optionales Callback, das am Ende der Verarbeitung aufgerufen wird
	 * @see #start(Runnable)
	 * @see #process()
	 */
	private Runnable whenDone;

	/**
	 * Verarbeitungsfortschritt (Wert zwischen 0.0 und 1.0)
	 */
	private volatile double progress;

	static {
		coreCount=Runtime.getRuntime().availableProcessors();
	}

	/**
	 * Konstruktor der Klasse
	 * @param model	Für die Kalibrierung zu verwendendes Modell (wird <code>null</code> übergeben, so wird das Callcenter-Beispielmodell verwendet)
	 * @param editModelPath	Pfad zur zugehörigen Modelldatei (als Basis für relative Pfade in Ausgabeelementen)
	 * @param output	Ausgabe-Callback für Statusmeldungen (kann <code>null</code> sein)
	 * @param baseRunTime	Angestrebte Laufzeit in Sekunden bei Nutzung von allen Kernen
	 */
	public ThreadCalibration(final EditModel model, final String editModelPath, final Consumer<String> output, final int baseRunTime) {
		setup=SetupData.getSetup();
		progress=0;

		if (model==null) {
			final int exampleIndex=EditModelExamples.getExampleIndexFromName(Language.tr("Examples.Callcenter"));
			this.model=EditModelExamples.getExampleByIndex(null,exampleIndex,false);
			this.editModelPath=null;
		} else {
			this.model=model;
			this.editModelPath=editModelPath;
		}

		this.output=output;
		this.baseRunTime=Math.max(1,baseRunTime);
	}

	/**
	 * Konstruktor der Klasse
	 * @param output	Ausgabe-Callback für Statusmeldungen (kann <code>null</code> sein)
	 * @param baseRunTime	Angestrebte Laufzeit in Sekunden bei Nutzung von allen Kernen
	 */
	public ThreadCalibration(final Consumer<String> output, final int baseRunTime) {
		this(null,null,output,baseRunTime);
	}

	/**
	 * Startet die Verarbeitung.<br>
	 * Diese Methode kehrt sofort zurück. Die eigentliche
	 * Verarbeitung erfolgt in einem eigenen Thread.
	 * @param whenDone	Optionales Callback, das am Ende der Verarbeitung aufgerufen wird
	 * @see #isRunning()
	 * @see #getPerformance()
	 */
	public void start(final Runnable whenDone) {
		if (thread!=null) return;
		this.whenDone=whenDone;
		thread=new Thread(()->process(),"Calibration controller");
		thread.start();
	}

	/**
	 * Bricht die Verarbeitung ab.
	 */
	public void cancel() {
		canceled=true;
	}

	/**
	 * Prüft, ob die Verarbeitung gerade läuft.
	 * @return	Liefert <code>true</code>, wenn die Verarbeitung läuft.
	 */
	public boolean isRunning() {
		return thread!=null;
	}

	/**
	 * Gibt eine Meldung über {@link #output} aus.
	 * @param message	Auzugebende Meldung
	 */
	private void output(final String message) {
		if (output!=null && message!=null) output.accept(message);
	}

	/**
	 * Stellt die Anzahl an zu verwendenden Threads ein.
	 * @param count	Anzahl an Simulationssthreads
	 */
	private void setThreads(final int count) {
		setup.useMultiCoreSimulation=true;
		setup.useMultiCoreSimulationMaxCount=count;
		setup.useDynamicThreadBalance=true;
		setup.saveSetup();
	}

	/**
	 * Führt eine Simulation durch.
	 * @param threadCount	Anzahl an Simulationssthreads
	 * @param arrivals	Anzahl an zu simulierenden Ankünften
	 * @return	Laufzeit der Simulation in ms (oder -1, wenn die Simulation abgebrochen wurde)
	 */
	private long simulateModel(final int threadCount, final long arrivals) {
		output(String.format(Language.tr("ThreadCalibration.Status.StartSimulation"),threadCount,NumberTools.formatLong(arrivals)));

		setThreads(threadCount);

		/* Modell vorbereiten */
		final Simulator simulator=new Simulator(setup.useMultiCoreSimulationMaxCount,model,editModelPath,null,null,null);
		final StartAnySimulator.PrepareError error=simulator.prepare();
		if (error!=null) {
			output(error.error);
			return -1;
		}

		/* Simulation starten */
		simulator.start();

		/* Auf Ende der Simulation warten */
		while (simulator.isRunning()) {
			if (!sleep(500)) {
				simulator.cancel();
				return -1;
			}
		}

		/* Laufzeit auslesen */
		final Statistics statistics=simulator.getStatistic();
		if (statistics.simulationData.runThreads!=threadCount) output(String.format(Language.tr("ThreadCalibration.Status.WarningThreadNumberMismatch"),threadCount,statistics.simulationData.runThreads));

		return statistics.simulationData.runTime;
	}

	/**
	 * Wartet eine bestimmte Zeit.
	 * @param ms	Wartezeit in ms
	 * @return	Gibt an, ob die Wartezeit erfolgreich absolviert werden konnte (<code>true</code>) oder ob die Verarbeitung abgebrochen wurde (<code>false</code>)
	 */
	private boolean sleep(final int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {}
		return !canceled;
	}

	/**
	 * Wartet eine vorgegebene Zeit, damit sich die CPU wieder abkühlen kann.
	 * @return	Gibt an, ob die Wartezeit erfolgreich absolviert werden konnte (<code>true</code>) oder ob die Verarbeitung abgebrochen wurde (<code>false</code>)
	 */
	private boolean cooldown() {
		output(String.format(Language.tr("ThreadCalibration.Status.CoolDown"),CPU_COOL_DOWN_MS/1000));

		long remaining=CPU_COOL_DOWN_MS;
		while (remaining>0) {
			if (!sleep(500)) return false;
			remaining-=500;
		}
		return true;
	}

	/**
	 * Führt die eigentliche Verarbeitung durch.<br>
	 * Diese Methode wird in einem eigenständigen Thread aufgerufen.
	 * @see #start(Runnable)
	 */
	private void process() {
		final boolean saveUseMultiCoreSimulation=setup.useMultiCoreSimulation;
		final int saveUseMultiCoreSimulationMaxCount=setup.useMultiCoreSimulationMaxCount;
		final boolean saveUseDynamicThreadBalance=setup.useDynamicThreadBalance;

		output(Language.tr("ThreadCalibration.Status.Start"));
		performance=null;
		boolean success=false;
		try {
			success=processInner();
		} finally {
			setup.useMultiCoreSimulation=saveUseMultiCoreSimulation;
			setup.useMultiCoreSimulationMaxCount=saveUseMultiCoreSimulationMaxCount;
			setup.useDynamicThreadBalance=saveUseDynamicThreadBalance;
			setup.saveSetup();
		}

		thread=null;
		if (success) {
			output(Language.tr("ThreadCalibration.Status.Done"));
		} else {
			output(Language.tr("ThreadCalibration.Status.Canceled"));
			performance=null;
		}
		if (whenDone!=null) whenDone.run();
	}

	/**
	 * Anzahl an zu simulierenden Kunden für den ersten Test
	 * zur Ermittlung der Kundenanzahl-abhängigen Simulationslaufzeiten
	 * @see #processInner()
	 */
	private static final long TEST_CLIENT_NUMBER=100_000;

	/**
	 * Zeitspanne zur Abkühlung der CPU-Kerne nach einer Simulation
	 * @see #cooldown()
	 */
	private static final long CPU_COOL_DOWN_MS=10_000;

	/**
	 * Innere Methode zur Verarbeitung.
	 * @see #process()
	 * @return	Liefert <code>true</code>, wenn die Verarbeitung erfolgreich abgeschlossen wurde
	 */
	private boolean processInner() {
		progress=0.0;
		final double progressSum=coreCount*(coreCount+1)/2+coreCount;

		output(Language.tr("ThreadCalibration.Status.Precompiling"));
		final long singleThreadRuntime=simulateModel(1,TEST_CLIENT_NUMBER);
		if (singleThreadRuntime<0) return false;
		final long clientArrival=TEST_CLIENT_NUMBER*(1000L*baseRunTime)*coreCount/singleThreadRuntime;
		progress=coreCount/progressSum;

		output(String.format(Language.tr("ThreadCalibration.Status.NumberOfArrivals"),NumberTools.formatLong(clientArrival)));

		this.performance=new long[coreCount];
		for (int threadCount=1;threadCount<=coreCount;threadCount++) {

			if (!cooldown()) return false;

			final long runTime=simulateModel(threadCount,clientArrival);
			if (runTime<0) return false;

			final long performance=clientArrival*1000/runTime;
			this.performance[threadCount-1]=performance;
			output(String.format(Language.tr("ThreadCalibration.Status.Runtime"),runTime/1000,NumberTools.formatLong(performance)));
			progress+=(coreCount+1-threadCount)/progressSum;
		}

		progress=1.0;
		return true;
	}

	/**
	 * Liefert den aktuellen Verarbeitungsfortschritt
	 * @return	Verarbeitungsfortschritt (Wert zwischen 0.0 und 1.0)
	 */
	public double getProgress() {
		return progress;
	}

	/**
	 * Liefert nach einem erfolgreichen Abschluss der Verarbeitung die Leistungswerte pro Threadanzahl.
	 * @return	Leistung pro Thread-Anzahl oder <code>null</code>, wenn noch keine Verarbeitung erfolgreich abgeschlossen wurde
	 */
	public long[] getPerformance() {
		if (performance==null) return null;
		return Arrays.copyOf(performance,performance.length);
	}

	/**
	 * Liefert nach einem erfolgreichen Abschluss der Verarbeitung die Leistungswerte pro Threadanzahl als Tabelle.
	 * @return	Leistung pro Thread-Anzahl oder <code>null</code>, wenn noch keine Verarbeitung erfolgreich abgeschlossen wurde
	 */
	public Table getPerformanceTable() {
		if (performance==null) return null;
		final Table table=new Table();
		table.addLine(new String[] {Language.tr("ThreadCalibration.Results.NumberOfThreads"),Language.tr("ThreadCalibration.Results.ClientsPerSecond")});
		for (int i=0;i<performance.length;i++) {
			final String[] line=new String[2];
			line[0]=""+(i+1);
			line[1]=NumberTools.formatLongNoGrouping(performance[i]);
			table.addLine(line);
		}
		return table;
	}
}

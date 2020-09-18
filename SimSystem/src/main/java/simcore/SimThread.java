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
package simcore;

import java.util.function.Supplier;

import simcore.eventmanager.EventManager;

/**
 * Diese Klasse kapselt den eigentlichen Simulations-Thread.<br><br>
 * Diese Klasse wird von der <code>Simulator</code> Klasse verwendet.
 * @author Alexander Herzog
 * @version 1.3
 * @see SimulatorBase
 */
public final class SimThread extends Thread {
	/**
	 * Callback, welches das Simulationsdatenobjekt liefert.<br>
	 * Dem Konstruktor kann alternativ zu dem Simulationsdatenobjekt solch
	 * ein Callback übergeben werden, dann wird das Simulationsdatenobjekt
	 * erst durch den eigentlichen Thread generiert.
	 */
	private final Supplier<SimData> simDataGetter;

	/**
	 * Diese Variable stellt eine Referenz des für diesen Thread gültigen <code>SimData</code>
	 * Objektes zur Verfügung. Nach dem Ende der Simulation können so z.B. aus den lokalen
	 * <code>SimData</code> Objekten die Statistikdaten zusammengefasst werden.
	 */
	public SimData simData;

	/**
	 * Während der Simulation werden in den beiden Variablen <code>eventCount</code> und
	 * <code>eventQueueLength</code> erfasst, wie viele Ereignisse bereits ausgeführt
	 * wurden und wie lang die Ereigniswarteschlange momentan ist. Die Aktualisierung
	 * erfolgt alle 100.000 Ereignisse und am Ende der Simulation.
	 */
	public long eventCount;

	/**
	 * Während der Simulation werden in den beiden Variablen <code>eventCount</code> und
	 * <code>eventQueueLength</code> erfasst, wie viele Ereignisse bereits ausgeführt
	 * wurden und wie lang die Ereigniswarteschlange momentan ist. Die Aktualisierung
	 * erfolgt alle 100.000 Ereignisse und am Ende der Simulation.
	 */
	public int eventQueueLength;

	/**
	 * Während der Simulation kann hier ausgelesen werden, welcher Tag gerade simuliert wird (1 basierend).
	 */
	public long currentDay;

	/**
	 * Zeit in Millisekunden, an der die Simulation (in diesem Thread) gestartet wurde.
	 */
	public long simStartTime;

	/**
	 * Zeit in Millisekunden, an der die Simulation (in diesem Thread) beendet wurde.
	 */
	public long simDoneTime;

	/**
	 * Referenz auf den EventManager<br>
	 * Wird von {@link #run()} aus {@link #simData} übernommen.
	 */
	private EventManager eventManager;

	/**
	 * Soll die Simulation abgebrochen werden?
	 * {@link #directAbortThread()}
	 */
	private volatile boolean abortSimulation;

	/**
	 * Gibt an, ob die Simulation angehalten oder fortgesetzt werden soll.
	 * @see #pauseExecution()
	 * @see #resumeExecution()
	 */
	private volatile boolean pauseSimulation;

	/**
	 * Weist den Simulationsthread an, fortzufahren.
	 * @see #resumeExecution()
	 * @see #stepExecution()
	 */
	private volatile boolean doStepOrContinue;

	/**
	 * Synchronisationsobjekt um das Ende einer Pause
	 * (für normale Fortsetzung oder für die Schrittausführung)
	 * zu signalisieren.
	 * @see #resumeExecution()
	 * @see #stepExecution()
	 */
	private final Object pauseObject=new Object();

	/**
	 * Konstruktor der {@link SimThread}-Klasse
	 * @param simData	Referenz auf das {@link SimData}-Objekt für diesen Thread.
	 * (Achtung: Das Objekt muss sich muss sich bereits auf den Thread beziehen, es wird keine Kopie erstellt.)
	 */
	public SimThread(final SimData simData) {
		super("SimThread-"+(simData.threadNr+1));
		pauseSimulation=false;
		setDaemon(false);
		simDataGetter=null;
		this.simData=simData;
	}

	/**
	 * Konstruktor der {@link SimThread}-Klasse
	 * @param simDataGetter	Callback zur Erzeugung des {@link SimData}-Objektes (wird erst durch den Thread aufgerufen)
	 * @param threadNr	0-basierende Nummer des Threads (wird für die Bildung des Thread-Namens verwendet)
	 */
	public SimThread(final Supplier<SimData> simDataGetter, final int threadNr) {
		super("SimThread-"+(threadNr+1));
		pauseSimulation=false;
		setDaemon(false);
		this.simDataGetter=simDataGetter;
	}

	/**
	 * Führt zu einem direkten Abbruch der Ereignisschleife
	 */
	public void directAbortThread() {
		if (eventManager!=null) {
			eventManager.directAbortThread();
			abortSimulation=true;
		}
	}

	/**
	 * Arbeitsroutine des Simulationsthreads
	 */
	@Override
	public void run() {
		simStartTime=System.currentTimeMillis();

		/* Ggf. Simulationsdaten erst im (NUMA-)Kontext des Threads allokieren. */
		if (simData==null) simData=simDataGetter.get();

		try {
			eventManager=simData.eventManager;
			final long days=simData.simDays;
			final long daysByOtherThreads=simData.simDaysByOtherThreads;
			for (long day=0;day<days;day++) {
				currentDay=day+1;
				simData.initDay(day,daysByOtherThreads+day,getPriority()==MIN_PRIORITY);
				eventManager.resetTime();
				if (isInterrupted()) {
					simData.terminateCleanUp(simData.currentTime);
					simData.finalTerminateCleanUp(eventCount);
					simDoneTime=System.currentTimeMillis();
					return;
				}
				while (eventManager.executeNextEvents(simData,pauseSimulation?1:200000,1000)) {
					if (pauseSimulation) synchronized (pauseObject) {
						try {while (!doStepOrContinue) pauseObject.wait(1000);} catch (InterruptedException e) {}
						doStepOrContinue=false;
					}
					if (abortSimulation || isInterrupted()) {
						simData.terminateCleanUp(simData.currentTime);
						simData.finalTerminateCleanUp(eventCount);
						simDoneTime=System.currentTimeMillis();
						return;
					}
					eventCount=eventManager.eventCount();
					eventQueueLength=eventManager.eventQueueLength();
				}
				eventCount=eventManager.eventCount();
				eventQueueLength=eventManager.eventQueueLength();
				simData.terminateCleanUp(simData.currentTime);
			}
			simData.finalTerminateCleanUp(eventCount);
		} catch (Exception e) {
			simData.catchException(e.getMessage()+traceInfo(e.getStackTrace()));
		} catch (OutOfMemoryError e) {
			simData.catchOutOfMemory(traceInfo(e.getStackTrace()));
		}

		simDoneTime=System.currentTimeMillis();
	}

	/**
	 * Tritt innerhalb der Simulationsthreads eine nicht behandelte
	 * Exception auf, so wird der Stack-Trace über diese Methode
	 * in einen String umgewandelt, so dass {@link #run()} entsprechende
	 * Daten an {@link SimData#catchException(String)} übergeben kann.
	 * @param trace	Stack-Trace
	 * @return	Stack-Trace als String
	 */
	private String traceInfo(StackTraceElement[] trace) {
		if (trace==null) return "";
		final StringBuilder sb=new StringBuilder();
		for (StackTraceElement element: trace) if (element!=null) {
			if (sb.length()>0) sb.append("\n");
			sb.append(element.toString());
		}
		return sb.toString();
	}

	/**
	 * Unterbricht die Verarbeitung des Threads.<br>
	 * Der Thread wird nicht abgebrochen und dann jederzeit fortgesetzt werden.
	 * @see #resumeExecution()
	 * @see #stepExecution()
	 */
	public void pauseExecution() {
		if (pauseSimulation) return;
		doStepOrContinue=false;
		pauseSimulation=true;
		if (eventManager!=null) eventManager.setPause();
	}

	/**
	 * Setzt die unterbrochene Verarbeitung des Threads fort.
	 * @see #pauseExecution()
	 */
	public void resumeExecution() {
		if (!pauseSimulation) return;
		synchronized (pauseObject) {
			pauseSimulation=false;
			doStepOrContinue=true;
			pauseObject.notify();
		}
	}

	/**
	 * Wenn die Verarbeitung des Threads unterbrochen wurde, kann über diese
	 * Methode ein einzelnes Ereignis abgearbeitet werden. Der Pause-Status
	 * der Verarbeitung wird dadurch nicht aufgehoben.
	 * @see #pauseExecution()
	 */
	public void stepExecution() {
		if (!pauseSimulation) return;
		synchronized (pauseObject) {
			doStepOrContinue=true;
			pauseObject.notify();
		}
	}
}
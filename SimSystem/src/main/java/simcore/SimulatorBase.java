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

import java.lang.Thread.State;

/**
 * Abstrakte Basisklasse für die Simulation<br><br>
 * Diese Klasse erzeugt pro CPU-Kern ein <code>SimThread</code>-Objekt
 * und versorgt es über die abstrakte Methode <code>getSimDataForThread</code>
 * mit einem <code>SimData</code>-Objekt. Desweiteren werden Methoden
 * zum Starten, Unterbrechen usw. der Simulation zur Verfügung gestellt.<br><br>
 * In abgeleiteten Klassen muss lediglich die <code>getSimDataForThread</code>-Methode
 * überschrieben werden.
 * @author Alexander Herzog
 * @version 1.4
 */
public abstract class SimulatorBase {
	/**
	 * Liefert <code>true</code> wenn die Simulation läuft oder beendet wurde.
	 * @see #isStarted()
	 */
	private boolean started=false;

	/**
	 * Wurde die Simulation per {@link #pauseExecution()} angehalten?
	 * @see #isPaused()
	 */
	private boolean paused=false;

	/**
	 * Sollen die Simulationsdatenobjekte vorab (<code>false</code>) oder innerhalb der Threads (<code>true</code>) initialisiert werden.
	 */
	protected final boolean numaAware;

	/**
	 * Array aus Referenzen auf die Simulationsthreads<br><br>
	 * Über die Referenz auf das jeweilige <code>SimData</code>-Objekt
	 * innerhalb von <code>SimThread</code> können über dieses Array z.B.
	 * am Ende der Simulation die Statistikdaten zusammengeführt werden.
	 * @see SimThread
	 */
	protected final SimThread[] threads;

	/**
	 * Laufzeiten der einzelnen Threads (in ms)
	 * @see #getThreadRuntimes()
	 */
	private final int[] threadRuntimes;

	/**
	 * In diese Variable wird durch die Methode <code>start</code> die
	 * Startzeit der Simulation (gezählt in ms seit dem Systemstart) eingetragen.
	 * @see #start(boolean, boolean)
	 */
	public long startTime=0;

	/**
	 * In diese Variable wird durch die Methode <code>finalizeRun</code> am
	 * Ende der Simulation die Gesamtlaufzeit des Simulationsprozesses
	 * (gezählt in ms seit dem Systemstart) eingetragen.
	 * @see #finalizeRun()
	 */
	public long runTime=0;

	/**
	 * In dieses Feld trägt der Konstruktor ein, wie viele Simulationsthreads
	 * verwendet werden.
	 */
	public final int threadCount;

	/**
	 * Konstruktor der Klasse {@link SimulatorBase}
	 * (Nutzt wenn vorhanden mehrere CPU-Kerne.)
	 */
	public SimulatorBase() {
		this(Integer.MAX_VALUE,false,false);
	}

	/**
	 * Konstruktor der Klasse <code>Simulator</code>
	 * @param allowMaxCore	Gibt an, wie viele Rechenthreads maximal genutzt werden sollen. (Es werden generell maximal so viele Thread wie Kerne verwendet.)
	 * @param moreThreads	Verwendet maximal einen Thread mehr als CPU-Kerne verfügbar sind.
	 * @param numaAware	Sollen getrennte Datenmodelle erstellt erst in den Threads verwendet werden (<code>true</code>) oder ein gemeinsames Modell für die statischen Daten und die lokalen Daten bereits vor dem Thread erstellt werden (<code>false</code>)
	 */
	public SimulatorBase(final int allowMaxCore, final boolean moreThreads, final boolean numaAware) {
		/* min. 100 MB pro Thread */
		final Runtime runtime=Runtime.getRuntime();
		final int maxThreadsByMemory=(int) Math.max(1,runtime.maxMemory()/1024/1024/100);

		threadCount=Math.max(1,Math.min(Math.min(allowMaxCore,runtime.availableProcessors()+(moreThreads?1:0)),maxThreadsByMemory));

		threads=new SimThread[threadCount];
		threadRuntimes=new int[threadCount];

		this.numaAware=numaAware;
	}

	/**
	 * Erzeugt ein (threadlokales) {@link SimData}-Objekt, welches
	 * beim Erzeugen eines {@link SimThread}-Objektes verwendet wird.<br>
	 * {@link #getSimDataForThread(int, int)} wird für jedes {@link SimThread}-Objekt
	 * aus dem <code>SimThread[]</code>-Array erneut aufgerufen.<br>
	 * @param threadNr	Nummer des Simulationsthreads (von 0 an gezählt)
	 * @param threadCount	Gesamtzahl der verwendeten Simulationsthreads
	 * @return {@link SimData}-Objekt, welches für einen {@link SimThread} verwendet werden kann
	 * @see SimData
	 * @see SimThread
	 */
	protected abstract SimData getSimDataForThread(final int threadNr, final int threadCount);

	/**
	 * Startet die Simulationssthreads.
	 * @param lowPriority	Gibt an, ob die Simulation mit niedriger Priorität im Hintergrund oder mit normaler Priorität (<code>false</code>) starten soll.
	 */
	public void start(final boolean lowPriority) {
		start(lowPriority,false);
	}

	/**
	 * Startet die Simulationssthreads.
	 * @param lowPriority	Gibt an, ob die Simulation mit niedriger Priorität im Hintergrund oder mit normaler Priorität (<code>false</code>) starten soll.
	 * @param startPaused	Startet die Simulation im Pause/Einzelschritt-Modus
	 */
	public void start(final boolean lowPriority, final boolean startPaused) {
		if (started) {
			setPriority(lowPriority);
			return;
		}
		started=true;
		int priority=lowPriority?Thread.MIN_PRIORITY:Thread.NORM_PRIORITY;
		for (int i=0;i<threads.length;i++) {
			if (numaAware) {
				final int nr=i;
				threads[i]=new SimThread(()->getSimDataForThread(nr,threads.length),i);
			} else {
				threads[i]=new SimThread(getSimDataForThread(i,threads.length));
			}
			if (startPaused) threads[i].pauseExecution();
			threads[i].setPriority(priority);
		}
		startTime=System.currentTimeMillis();
		for (Thread t : threads) t.start();
	}

	/**
	 * Gibt an, ob die Simulation gestartet wurde.
	 * @return	Liefert <code>true</code> wenn die Simulation läuft oder beendet wurde.
	 */
	public boolean isStarted() {
		return started;
	}

	/**
	 * Setzt die Priorität der Simulationsthreads
	 * @param low	Wird <code>true</code> übergeben, so werden die Threads of niedrige Priorität gestellt, sonst auf normale Priorität
	 */
	public final void setPriority(final boolean low) {
		int priority=low?Thread.MIN_PRIORITY:Thread.NORM_PRIORITY;
		for (Thread t : threads) if (t!=null) t.setPriority(priority);
	}

	/**
	 * Bricht die Simulation vorzeitig ab.
	 */
	public void cancel() {
		for (SimThread t : threads) if (t!=null) {
			t.interrupt();
			t.directAbortThread();
		}
		try {
			for (SimThread t : threads) if (t!=null)  t.join(5_000);
		} catch (InterruptedException e) {}
	}

	/**
	 * Prüft, ob die Simulationsthreads noch laufen.
	 * @return Liefert <code>true</code> wenn mindestens ein Thread noch aktiv ist.
	 */
	public boolean isRunning() {
		if (!started) return true;
		for (Thread t : threads) if (t!=null && t.isAlive()) return true;
		return false;
	}

	/**
	 * Liefert nach Abschluss der Simulation die Laufzeiten der einzelnen Threads.
	 * @return	Laufzeiten der einzelnen Threads (in ms)
	 * @see #finalizeRun()
	 */
	public int[] getThreadRuntimes() {
		return threadRuntimes;
	}

	/**
	 * Wartet bis alle Simulationsthreads beendet sind und berechnet dann die gesamte Laufzeit.
	 * @return Liefert immer null zurück.
	 */
	public String finalizeRun() {
		if (runTime>0) return null; /* finalizeRun() wurde schon einmal ausgeführt */
		long simDoneTime=0;
		int i=0;

		while (i<threads.length) {
			if (threads[i]==null) {i++; continue;}
			try {
				threads[i].resumeExecution(); /* Stellt sicher, dass der SimThread ein zuvor ggf. ausgelöstes Abbruch-Ereignis auch verarbeiten kann. */
				threads[i].join();
				simDoneTime=Math.max(simDoneTime,threads[i].simDoneTime);
				threadRuntimes[i]=(int)(threads[i].simDoneTime-threads[i].simStartTime);
			} catch (InterruptedException e) {continue;}
			i++;
		}
		if (simDoneTime>0) runTime=simDoneTime-startTime;

		return null;
	}

	/**
	 * Gibt die Summe der in allen Threads bisher simulierten Ereignisse zurück.
	 * @return Anzahl der simulierten Ereignisse in allen Threads
	 */
	public final long getEventCount() {
		long sum=(threads[0]==null)?0:threads[0].eventCount;
		for (int i=1;i<threads.length;i++) if (threads[i]!=null) sum+=threads[i].eventCount;
		return sum;
	}

	/**
	 * Gibt die Summe der momentan in allen Threads wartenden Ereignisse zurück.
	 * @return Anzahl der momentan wartenden Ereignisse in allen Threads
	 */
	public final long getEventQueueLength() {
		long sum=(threads[0]==null)?0:threads[0].eventQueueLength;
		for (int i=1;i<threads.length;i++) sum+=(threads[i]==null)?0:threads[i].eventQueueLength;
		return sum;
	}

	/**
	 * Gibt zurück, wie viele Ereignisse pro Sekunde verarbeitet werden.
	 * @return	Ereignisse pro Sekunde
	 */
	public final int getEventsPerSecond() {
		return (int)(getEventCount()*1000/Math.max(1,System.currentTimeMillis()-startTime));
	}

	/**
	 * Gibt die Nummer des gerade im Simulator in Bearbeitung befindlichen Tages zurück.
	 * @return Gerade in Arbeit befindlicher Tag
	 */
	public final long getSimDayCount() {
		long sum=(threads[0]==null)?0:threads[0].currentDay;
		for (int i=1;i<threads.length;i++) if (threads[i]!=null) sum+=threads[i].currentDay;
		return sum;
	}

	/**
	 * Wurde die Simulation per {@link #pauseExecution()} angehalten?
	 * @return	Ist die Simulation momentan pausiert?
	 * @see #pauseExecution()
	 * @see #resumeExecution()
	 */
	public boolean isPaused() {
		return paused;
	}

	/**
	 * Unterbricht die Simulation.<br>
	 * Die Simulation wird nicht abgebrochen und kann später fortgesetzt werden.
	 * @see #resumeExecution()
	 * @see #stepExecution(boolean)
	 */
	public void pauseExecution() {
		paused=true;
		for (int i=0;i<threads.length;i++) if (threads[i]!=null) threads[i].pauseExecution();
	}

	/**
	 * Unterbricht die Simulation.<br>
	 * Die Simulation wird nicht abgebrochen und kann später fortgesetzt werden.<br>
	 * Im Gegensatz zu {@link #pauseExecution()} kehrt dieser Befehl erst zurück,
	 * wenn die Threads auch wirklich angehalten haben.
	 * @see #resumeExecution()
	 * @see #stepExecution(boolean)
	 */
	public void pauseExecutionAndWait() {
		pauseExecution();
		for (int i=0;i<threads.length;i++) if (threads[i]!=null) threads[i].waitForPause();
	}

	/**
	 * Setzt die unterbrochene Simulation fort.
	 * @see #pauseExecution()
	 */
	public void resumeExecution() {
		paused=false;
		for (int i=0;i<threads.length;i++) if (threads[i]!=null) threads[i].resumeExecution();
	}

	/**
	 * Wurde die Simulation angehalten, so kann über diese Methode ein Einzelschritt ausgeführt werden.
	 * Der Pause-Status der Simulation wird dadurch nicht aufgehoben.
	 * @param wait	Gibt an, ob der Schritt vollständig abgearbeitet werden soll, bevor die Funktion zurückkehrt
	 * @see #pauseExecution()
	 */
	public void stepExecution(final boolean wait) {
		for (int i=0;i<threads.length;i++) if (threads[i]!=null) threads[i].stepExecution();

		/* Warten bis die Threads den Schritt abgearbeitet haben. */
		if (wait) {
			boolean ok=false;
			while (!ok) {
				ok=true;
				for (int i=0;i<threads.length;i++) {
					final State state=threads[i].getState();
					if (state!=Thread.State.TIMED_WAITING && state!=Thread.State.TERMINATED) {ok=false; break;}
				}
				if (!ok) try {Thread.sleep(0);} catch (InterruptedException e) {}
			}
		}
	}

	/**
	 * Pausiert das Logging (sofern ein Logger gesetzt ist).
	 */
	public void pauseLogging() {
		for (int i=0;i<threads.length;i++) if (threads[i]!=null && threads[i].simData!=null) threads[i].simData.loggingActive=false;
	}

	/**
	 * Setzt das Logging fort (sofern ein Logger gesetzt ist).
	 */
	public void continueLogging() {
		for (int i=0;i<threads.length;i++) if (threads[i]!=null && threads[i].simData!=null && threads[i].simData.logging!=null) threads[i].simData.loggingActive=true;
	}

	/**
	 * Kombiniert die Methoden <code>start</code> und <code>finalizeRun</code>.
	 * Optional kann ein einfacher Fortschrittsbalken in der Console ausgegeben werden.<br><br>
	 * Diese Funktion erlaubt kein benutzerseitiges Abbrechen der Simulation und sollte nur
	 * zu Testzwecken verwendet werden.
	 * @param consoleOutput	Bestimmt, ob während der Simulation ein Fortschrittsbalken bestehend aus Punkten in der Console ausgegeben werden soll.
	 */
	@Deprecated
	public final void startAndRunAndFinalizeSimulator(boolean consoleOutput) {
		start(false);
		while (isRunning()) {
			if (consoleOutput) System.out.print(".");
			try {Thread.sleep(consoleOutput?1000:50);} catch (InterruptedException e) {}
		}
		if (consoleOutput) System.out.println("");
		finalizeRun();
	}
}

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

import java.awt.Color;
import java.io.File;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import mathtools.NumberTools;
import simcore.eventcache.EventCache;
import simcore.eventcache.NoEventCache;
import simcore.eventmanager.EventManager;
import simcore.logging.PlainTextLogger;
import simcore.logging.SimLogging;

/**
 * Basisklasse für alle während der Simulation notwendigen Informationen<br><br>
 * Ein Objekt dieser Klasse wird während der Simulation an die <code>run</code>
 * Methode der auszuführenden Ereignisse übergeben.<br>
 * In dieser Klasse müssen alle statischen Daten zum Modell, alle dynamischen Parameter
 * und auch die gesammelten Statistikdaten gespeichert werden.<br>
 * Jeder Thread verfügt über ein eigenes Objekt dieser Klasse.
 * @author Alexander Herzog
 * @version 1.1
 */
public class SimData {
	/**
	 * Referenz auf den zu verwendenden EventCache<br>
	 */
	public EventCache eventCache;

	/**
	 * Anzahl der Wiederholungen des Simulationslaufs
	 * @see #initDay(long, long, boolean)
	 */
	public long simDays=1;

	/**
	 * Anzahl der Tage, die die Threads mit kleinerem Index verarbeiten
	 */
	public long simDaysByOtherThreads=0;

	/**
	 * Referenz auf den EventManager<br><br>
	 * Mit Hilfe dieser Referenz können während der Ausführung von Ereignissen
	 * neue Ereignisse eingefügt oder entfernt werden.
	 */
	public EventManager eventManager;

	/**
	 * Anzahl der Rechenthreads<br><br>
	 * Soll eine bestimmte Anzahl an Ereignissen simuliert werden, so muss während
	 * der Ausführung eines Ereignisses bekannt sein, ob ein weiteres Ereignis generiert
	 * werden soll. Da das System alle CPU-Kerne verwendet, muss diese Anzahl durch die
	 * Anzahl der Threads geteilt werden.
	 */
	public final int threadCount;

	/**
	 * Nummer des Rechenthreads (0 basierend gezählt)
	 */
	public final int threadNr;

	/**
	 * Aktuelle Simulator-Zeit<br><br>
	 * Diese Variable wird vom EventManager jeweils vor dem Aufruf der <code>run</code>
	 * Methode des nächsten Ereignisses gesetzt.
	 */
	public long currentTime=0;

	/**
	 * Gibt an, ob das Event-Logging aktiv ist.<br>
	 * Wenn ja, sollten Events die Methode <code>logEventExecution</code> aufrufen oder das <code>logging</code>-Feld
	 * benutzen, um Statusmeldungen auszugeben.
	 * @see #logEventExecution(String, int, String)
	 * @see #logEventExecution(Color, String, int, String)
	 * @see #activateLogging(SimLogging)
	 * @see #activateLogging(File)
	 * @see SimData#logging
	 */
	public boolean loggingActive=false;

	/**
	 * Gewährt Zugriff auf das Logging-System.<br>
	 * @see #activateLogging(File)
	 * @see #activateLogging(SimLogging)
	 */
	public SimLogging logging=null;

	/**
	 * Constructor der <code>SimData</code> Klasse
	 * @param eventManager	Referenz auf den zu verwendenden EventManager
	 * @param eventCache	Referenz auf den zu verwendenden EventCache
	 * @param threadNr		Gibt die Nummer des Threads an, für den das <code>SimDat</code>-Objekt erstellt wird.
	 * @param threadCount	Anzahl der Rechenthreads
	 */
	public SimData(final EventManager eventManager, final EventCache eventCache, final int threadNr, final int threadCount) {
		this.eventManager=eventManager;
		this.eventCache=(eventCache==null)?(new NoEventCache()):eventCache;
		this.threadNr=threadNr;
		this.threadCount=threadCount;
	}

	/**
	 * Abschlussarbeiten am Ende des Simulationstages durchführen<br><br>
	 * Diese Methode wird von <code>SimThread</code> automatisch aufgerufen.
	 * @param now	Zeit, zu der die Simulation endet
	 */
	public void terminateCleanUp(long now) {}

	/**
	 * Abschlussarbeiten am Ende der gesamten Simulation durchführen<br><br>
	 * Diese Methode wird von <code>SimThread</code> automatisch aufgerufen.
	 * @param eventCount	Anzahl der Ereignisse während der Simulation
	 */
	public void finalTerminateCleanUp(long eventCount) {
		eventCache.clear();
		eventManager.deleteAllEvents();
		eventCache=null;
		eventManager=null;
		if (logging!=null) logging.done();
	}

	/**
	 * Vorbereitung eines Simulationstages (nur notwendig, wenn mehrere Tage simuliert werden sollen)
	 * @param day	Nummer des simulierten Tages (beginnend ab 0)
	 * @param dayGlobal	Nummer der (bezogen auf alle Threads) zu simulierenden Tages (beginnend ab 0)
	 * @param	backgroundMode	Gibt an, ob die Simulation explizit gestartet wurde (false) oder als Hintergrundsimulation ausgeführt wird (true)
	 * @see #simDays
	 */
	public void initDay(long day, long dayGlobal, boolean backgroundMode) {}

	/**
	 * Ereignis in den EventCache aufnehmen<br><br>
	 * Ereignisse sollten nach ihrer Ausführung nicht einfach freigegeben, sondern in
	 * den EventCache übernommen werden. Per <code>getEvent</code> können Ereignisse
	 * aus dem EventCache wieder abgerufen werden. Dabei werden (wenn der Cache leer ist)
	 * auch automatisch neue Ereignisse erzeugt.<br>
	 * Dies verringert die Anzahl an nicht parallelisierbaren Speicherreservierungsanfragen.
	 * @param event	Ereignis, welches nicht mehr benötigt wird und welches in den Cache aufgenommen werden soll
	 * @see #getEvent(Class)
	 */
	public final void recycleEvent(final Event event) {eventCache.put(event);}

	/**
	 * Ereignis aus dem EventCache holen<br><br>
	 * Ereignisse sollten nach ihrer Ausführung nicht einfach freigegeben, sondern per
	 * <code>recycleEvent</code> in den EventCache übernommen werden. Über <code>getEvent</code>
	 * können Ereignisse wieder aus dem Cache abgerufen werden. Gibt es kein passendes Ereignis
	 * im Cache, so wird automatisch ein neues Ereignis erstellt, d.h. <code>getEvent</code>
	 * liefert auf jeden Fall ein Ereignis des gewählten Typs zurück.<br>
	 * Dies verringert die Anzahl an nicht parallelisierbaren Speicherreservierungsanfragen.
	 * @param eventClass	Typ des angeforderten Ereignisses
	 * @return	Referenz auf das Ereignis des gewünschten Typs
	 */
	public final Event getEvent(final Class<? extends Event> eventClass) {return eventCache.get(eventClass);}

	/**
	 * Ereignis aus dem EventCache holen<br><br>
	 * Ereignisse sollten nach ihrer Ausführung nicht einfach freigegeben, sondern per
	 * <code>recycleEvent</code> in den EventCache übernommen werden. Über <code>getEventOrNull</code>
	 * können Ereignisse wieder aus dem Cache abgerufen werden. Gibt es kein passendes Ereignis
	 * im Cache, so wird <code>nulll</code> zurückgeliefert.<br>
	 * Dies verringert die Anzahl an nicht parallelisierbaren Speicherreservierungsanfragen.
	 * @param eventClass	Typ des angeforderten Ereignisses
	 * @return	Referenz auf das Ereignis des gewünschten Typs
	 */
	public final Event getEventOrNull(final Class<? extends Event> eventClass) {return eventCache.getOrNull(eventClass);}

	/**
	 * Gibt an, welches Logging-System verwendet werden soll
	 * @param logFile	Dateiname der Logfile-Datei
	 * @return	Logging-System, welches das Interface <code>SimLogging</code> implementiert
	 * @see #activateLogging(File)
	 * @see #activateLogging(SimLogging)
	 * @see SimLogging
	 */
	protected SimLogging getLogger(final File logFile) {
		return new PlainTextLogger(logFile,false,false,false,logFile.toString().toUpperCase().endsWith(".CSV"));
	}

	/**
	 * Schaltet die Logging-Funktionen ein.
	 * Im Folgenden können Ereignisse Statusmeldungen über die Funktion <code>logEventExecution</code> ausgeben.
	 * @param logFile	Dateiname der Logfile-Datei
	 * @return	Gibt <code>true</code> zurück, wenn das Logging mit dem angegebenen Dateinamen aktiviert werden konnte.
	 * @see #loggingActive
	 * @see #logEventExecution(String, int, String)
	 * @see #logEventExecution(Color, String, int, String)
	 */
	public final boolean activateLogging(File logFile) {
		if (logging!=null) logging.done();
		logging=getLogger(logFile);
		if (logging==null || !logging.ready()) logging=null;
		loggingActive=(logging!=null);
		return loggingActive;
	}

	/**
	 * Schaltet die Logging-Funktionen ein.
	 * Im Folgenden können Ereignisse Statusmeldungen über die Funktion <code>logEventExecution</code> ausgeben.
	 * @param logger	Bereits gestarteter Logger
	 * @return	Gibt <code>true</code> zurück, wenn das Logging mit dem angegebenen Dateinamen aktiviert werden konnte.
	 * @see #loggingActive
	 * @see #logEventExecution(String, int, String)
	 * @see #logEventExecution(Color, String, int, String)
	 */
	public final boolean activateLogging(SimLogging logger) {
		if (logging!=null) logging.done();
		logging=logger;
		if (logging==null || !logging.ready()) logging=null;
		loggingActive=(logging!=null);
		return loggingActive;
	}

	/**
	 * Schaltet die Logging-Funktion aus.
	 */
	public final void disableLogging() {
		if (logging!=null) logging.done();
		logging=null;
		loggingActive=false;
	}

	/**
	 * Speichert Statusausgaben eines Ereignisses.
	 * @param event	Gibt den Namen des Event, das die Logging-Aktion ausgelöst hat, an.
	 * @param id	ID der Station, an der das Ereignis stattfand (Werte kleiner als 0 für "keine Station")
	 * @param info	Enthält eine Beschreibung, die zu dem Logeintrag gespeichert werden soll.
	 * @return	Gibt an, ob das Ergeignis erfolgreich geloggt werden konnte.
	 */
	public final boolean logEventExecution(final String event, final int id, final String info) {
		if (logging==null) return true;
		return logging.log(currentTime,Color.BLACK,event,id,info);
	}

	/**
	 * Speichert Statusausgaben eines Ereignisses.
	 * @param color	Farbe in die die Log-Zeile eingefärbt werden soll (kann Logger-abhängig ignoriert werden)
	 * @param event	Gibt den Namen des Event, das die Logging-Aktion ausgelöst hat, an.
	 * @param id	ID der Station, an der das Ereignis stattfand (Werte kleiner als 0 für "keine Station")
	 * @param info	Enthält eine Beschreibung, die zu dem Logeintrag gespeichert werden soll.
	 * @return	Gibt an, ob das Ergeignis erfolgreich geloggt werden konnte.
	 */
	public final boolean logEventExecution(final Color color, final String event, final int id, final String info) {
		if (logging==null) return true;
		return logging.log(currentTime,color,event,id,info);
	}

	/**
	 * Wird aufgerufen, wenn die Simulation bedingt durch einen Fehler abgebrochen wird.
	 * @param text	Fehlermeldung und Position, an der der Fehler aufgetreten ist
	 */
	public void catchException(final String text) {
		System.err.println(text);
	}

	/**
	 * Wird aufgerufen, wenn die Simulation bedingt durch ungenügenden Speicher abgebrochen wird.
	 * @param text	Position, an der der Fehler aufgetreten ist
	 */
	public void catchOutOfMemory(final String text) {
		System.err.println("Out of Memory");
		System.err.println(text);
	}

	/**
	 * Liefert eine Zeitangabe als String zurück.
	 * @param time	Zeitangabe auf Millisekunden-Basis
	 * @return	Zeitangabe als String
	 */
	public static final String formatSimTime(long time) {
		final StringBuilder sb=new StringBuilder();

		final long d=time/1000/60/60/24;
		if (d>0) {sb.append(d); sb.append(":");}

		final long h=time/1000/60/60%24;
		if (h<10) sb.append("0");
		sb.append(h);

		sb.append(":");

		final long m=(time/1000/60)%60;
		if (m<10) sb.append("0");
		sb.append(m);

		sb.append(":");

		final long s=(time/1000)%60;
		if (s<10) sb.append("0");
		sb.append(s);

		final DecimalFormatSymbols format=new DecimalFormatSymbols(NumberTools.getLocale());
		sb.append(format.getDecimalSeparator());

		final long f=time%1000;
		if (f<100) sb.append("0");
		if (f<10) sb.append("0");
		sb.append(f);

		return sb.toString();

		/*
		langsamer:
		long d=time/1000/60/60/24;
		String h=""+(time/1000/60/60%24); if (h.length()<2) h="0"+h;
		String m=""+((time/1000/60)%60); if (m.length()<2) m="0"+m;
		String s=""+((time/1000)%60); if (s.length()<2) s="0"+s;
		String f=""+(time%1000); while (f.length()<3) f="0"+f;
		if (d==0) return h+":"+m+":"+s+","+f; else return ""+d+":"+h+":"+m+":"+s+","+f;
		 */
	}

	/**
	 * Offset in MS zwischen UTC und der lokalen Zeit.
	 * @see #formatSimDateTime(long)
	 */
	private static final long offset=TimeZone.getDefault().getRawOffset();

	/**
	 * Liefert eine Zeitangabe als Datum und Zeit String zurück.
	 * @param time	Zeitangabe auf Millisekunden-Basis
	 * @return	Zeitangabe als String aus Datum und Zeit
	 */
	public static final String formatSimDateTime(long time) {
		final Date date=new Date();
		date.setTime(time-offset);
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
	}

	/**
	 * Liefert die ID eines Objektes (z.B. für das Logging)
	 * @param o	Objekt, für das die ID ermittelt wer den soll
	 * @return	ID des Objekts als String
	 */
	public static String formatObjectID(final Object o) {
		if (o==null) return "0";
		String s=o.toString();
		return s.substring(s.indexOf('@')+1);
	}
}

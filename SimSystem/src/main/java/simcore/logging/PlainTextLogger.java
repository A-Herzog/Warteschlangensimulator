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
package simcore.logging;

import java.awt.Color;
import java.io.File;

import mathtools.NumberTools;
import simcore.SimData;

/**
 * Gibt Logging-Daten als einfachen Text aus, d.h. die jeweils angegebene Farbe pro Eintrag wird bei der Ausgabe ignoriert.
 * @author Alexander Herzog
 * @see SimLogging
 */
public class PlainTextLogger extends AbstractTextLogger {
	/** Nach Einträgen mit demselben Zeitstempel eine Leerzeile einfügen */
	private final boolean groupSameTimeEvents;
	/** Ereignisse in einer Zeile (Name und Beschreibung durch Tabulator getrennt) oder in mehreren Zeilen ausgeben */
	private final boolean singleLineMode;
	/** Wie sollen Zeitangaben ausgegeben werden? */
	private final PlainTextLoggerTimeMode timeMode;
	/** IDs mit ausgeben */
	private final boolean printIDs;
	/** Klassennamen der Event-Objekte ausgeben? */
	private final boolean printClassNames;
	/** Text im CSV-Modus (<code>true</code>) oder tabulator-getrennt (<code>false</code>) ausgeben */
	private final boolean csvMode;
	/** Zeitpunkt an dem das letzte Ereignis auftrat (für das optionale Gruppieren) */
	private long lastEventTime=-1;

	/** Wiederzuverwendendes {@link StringBuilder}-Objekt in {@link #log(long, Color, String, int, String)} */
	private final StringBuilder lineBuilder;

	/**
	 * Konstruktor der Klasse <code>PlainTextLogger</code>
	 * @param logFile	Dateiname der Logfile-Datei
	 * @param groupSameTimeEvents	Nach Einträgen mit demselben Zeitstempel eine Leerzeile einfügen
	 * @param singleLineMode	Ereignisse in einer Zeile (Name und Beschreibung durch Tabulator getrennt) oder in mehreren Zeilen ausgeben
	 * @param timeMode	Wie sollen Zeitangaben ausgegeben werden?
	 * @param printIDs	IDs mit ausgeben
	 * @param printClassNames	Klassennamen der Event-Objekte ausgeben?
	 * @param csvMode	Text im CSV-Modus (<code>true</code>) oder tabulator-getrennt (<code>false</code>) ausgeben
	 */
	public PlainTextLogger(final File logFile, final boolean groupSameTimeEvents, final boolean singleLineMode, final PlainTextLoggerTimeMode timeMode, final boolean printIDs, final boolean printClassNames, final boolean csvMode) {
		this.groupSameTimeEvents=groupSameTimeEvents;
		this.singleLineMode=singleLineMode;
		this.timeMode=timeMode;
		this.csvMode=csvMode;
		this.printIDs=printIDs;
		this.printClassNames=printClassNames;
		lineBuilder=new StringBuilder();
		init(logFile);
	}

	/**
	 * Konstruktor der Klasse <code>PlainTextLogger</code>
	 * @param logFile	Dateiname der Logfile-Datei
	 * @param groupSameTimeEvents	Nach Einträgen mit demselben Zeitstempel eine Leerzeile einfügen
	 * @param singleLineMode	Ereignisse in einer Zeile (Name und Beschreibung durch Tabulator getrennt) oder in mehreren Zeilen ausgeben
	 * @param printIDs	IDs mit ausgeben
	 * @param printClassNames	Klassennamen der Event-Objekte ausgeben?
	 * @param csvMode	Text im CSV-Modus (<code>true</code>) oder tabulator-getrennt (<code>false</code>) ausgeben
	 */
	public PlainTextLogger(final File logFile, final boolean groupSameTimeEvents, final boolean singleLineMode, final boolean printIDs, final boolean printClassNames, final boolean csvMode) {
		this(logFile,groupSameTimeEvents,singleLineMode,PlainTextLoggerTimeMode.TIME,printIDs,printClassNames,csvMode);
	}

	/**
	 * Wandelt einen Text in eine CSV-Zelle um.
	 * @param cell	Text
	 * @return	CSV-gekappseter Text
	 */
	public static String toCSV(String cell) {
		cell=cell.replace("\"","\"\"");
		if (cell.indexOf('"')!=-1 || cell.indexOf(';')!=-1) cell='"'+cell+'"';
		if (cell.equalsIgnoreCase("id")) cell='"'+cell+'"';
		return cell;
	}

	/**
	 * Erstellt die konkret auszugebende Logging-Zeile aus den Teilangaben.
	 * @param timeMode	Wie sollen Zeitangaben ausgegeben werden?
	 * @param singleLineMode	Ereignisse in einer Zeile (Name und Beschreibung durch Tabulator getrennt) oder in mehreren Zeilen ausgeben
	 * @param printIDs	IDs mit ausgeben
	 * @param printClassNames	Klassennamen der Event-Objekte ausgeben?
	 * @param csvMode	Text im CSV-Modus (<code>true</code>) oder Tabulator-getrennt (<code>false</code>) ausgeben
	 * @param time	Zeitpunkt des Ereignisses
	 * @param color	Farbe in die die Log-Zeile eingefärbt werden soll (kann Logger-abhängig ignoriert werden)
	 * @param event	Gibt den Namen des Event, das die Logging-Aktion ausgelöst hat, an.
	 * @param id	ID der Station, an der das Ereignis stattfand (Werte kleiner als 0 für "keine Station")
	 * @param info	Enthält eine Beschreibung, die zu dem Logeintrag gespeichert werden soll.
	 * @param result	{@link StringBuilder}-Objekt in das das Ergebnis geschrieben werden soll
	 */
	public static void processLine(final PlainTextLoggerTimeMode timeMode, final boolean singleLineMode, final boolean printIDs, final boolean printClassNames, final boolean csvMode, final long time, final Color color, final String event, final int id, final String info, final StringBuilder result) {
		switch (timeMode) {
		case PLAIN: result.append(NumberTools.formatNumber(time/1000.0,3)); break;
		case TIME: result.append(SimData.formatSimTime(time)); break;
		case DATETIME: result.append(SimData.formatSimDateTime(time)); break;
		}
		if (singleLineMode) result.append(csvMode?';':'\t'); else result.append(csvMode?';':' ');

		if (printClassNames) {
			final String eventObject=getCallingEventObject();
			if (eventObject!=null && !eventObject.isEmpty()) {
				if (csvMode) {
					result.append(toCSV(eventObject));
				} else {
					result.append(eventObject);
				}
			}
		}

		if (event!=null && !event.isEmpty()) {
			if (printClassNames) {
				if (singleLineMode) result.append(csvMode?';':'\t'); else result.append(System.lineSeparator());
			}
			if (csvMode) {
				result.append(toCSV(event.replace("\n"," ")));
			} else {
				result.append(event.replace("\n",System.lineSeparator()));
			}
		}

		if (printIDs) {
			if (singleLineMode) result.append(csvMode?';':'\t'); else result.append(System.lineSeparator());
			final String idString=(id>=0)?(""+id):"";
			if (csvMode) {
				result.append(toCSV(idString));
			} else {
				result.append(idString);
			}
		}

		if (info!=null && !info.isEmpty()) {
			if (singleLineMode) result.append(csvMode?';':'\t'); else result.append(System.lineSeparator());
			if (csvMode) {
				result.append(toCSV(info.replace("\n"," ")));
			} else {
				result.append(info.replace("\n",System.lineSeparator()));
			}
		}

		result.append(System.lineSeparator());

	}

	@Override
	public boolean log(final long time, final Color color, final String event, final int id, final String info) {
		lineBuilder.setLength(0);

		if (groupSameTimeEvents && lastEventTime!=time) {
			if (lastEventTime>=0) lineBuilder.append(System.lineSeparator());
			lastEventTime=time;
		}

		processLine(timeMode,singleLineMode,printIDs,printClassNames,csvMode,time,color,event,id,info,lineBuilder);

		if (nextLogger!=null) nextLogger.log(time,color,event,id,info);

		return writeString(lineBuilder.toString());
	}
}

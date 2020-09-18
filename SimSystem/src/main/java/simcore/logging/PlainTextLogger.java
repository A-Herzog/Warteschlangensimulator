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
	/** Text im CSV-Modus (<code>true</code>) oder tabulator-getrennt (<code>false</code>) ausgeben */
	private final boolean csvMode;
	/** Zeitpunkt an dem das letzte Ereignis auftrat (für das optionale Gruppieren) */
	private long lastEventTime=-1;

	/**
	 * Konstruktor der Klasse <code>PlainTextLogger</code>
	 * @param logFile	Dateiname der Logfile-Datei
	 * @param groupSameTimeEvents	Nach Einträgen mit demselben Zeitstempel eine Leerzeile einfügen
	 * @param singleLineMode	Ereignisse in einer Zeile (Name und Beschreibung durch Tabulator getrennt) oder in mehreren Zeilen ausgeben
	 * @param timeMode	Wie sollen Zeitangaben ausgegeben werden?
	 * @param printIDs	IDs mit ausgeben
	 * @param csvMode	Text im CSV-Modus (<code>true</code>) oder tabulator-getrennt (<code>false</code>) ausgeben
	 */
	public PlainTextLogger(final File logFile, final boolean groupSameTimeEvents, final boolean singleLineMode, final PlainTextLoggerTimeMode timeMode, final boolean printIDs, final boolean csvMode) {
		this.groupSameTimeEvents=groupSameTimeEvents;
		this.singleLineMode=singleLineMode;
		this.timeMode=timeMode;
		this.csvMode=csvMode;
		this.printIDs=printIDs;
		init(logFile);
	}

	/**
	 * Konstruktor der Klasse <code>PlainTextLogger</code>
	 * @param logFile	Dateiname der Logfile-Datei
	 * @param groupSameTimeEvents	Nach Einträgen mit demselben Zeitstempel eine Leerzeile einfügen
	 * @param singleLineMode	Ereignisse in einer Zeile (Name und Beschreibung durch Tabulator getrennt) oder in mehreren Zeilen ausgeben
	 * @param printIDs	IDs mit ausgeben
	 * @param csvMode	Text im CSV-Modus (<code>true</code>) oder tabulator-getrennt (<code>false</code>) ausgeben
	 */
	public PlainTextLogger(final File logFile, final boolean groupSameTimeEvents, final boolean singleLineMode, final boolean printIDs, final boolean csvMode) {
		this(logFile,groupSameTimeEvents,singleLineMode,PlainTextLoggerTimeMode.TIME,printIDs,csvMode);
	}

	/**
	 * Wandelt einen Text in eine CSV-Zelle um.
	 * @param cell	Text
	 * @return	CSV-gekappseter Text
	 */
	private String toCSV(String cell) {
		cell=cell.replace("\"","\"\"");
		if (cell.indexOf('"')!=-1 || cell.indexOf(';')!=-1) cell='"'+cell+'"';
		if (cell.equalsIgnoreCase("id")) cell='"'+cell+'"';
		return cell;
	}

	@Override
	public boolean log(final long time, final Color color, final String event, final int id, final String info) {
		final StringBuilder sb=new StringBuilder();

		if (groupSameTimeEvents && lastEventTime!=time) {
			if (lastEventTime>=0) sb.append(System.lineSeparator());
			lastEventTime=time;
		}

		switch (timeMode) {
		case PLAIN: sb.append(NumberTools.formatNumber(time/1000.0)); break;
		case TIME: sb.append(SimData.formatSimTime(time)); break;
		case DATETIME: sb.append(SimData.formatSimDateTime(time)); break;
		}
		if (singleLineMode) sb.append(csvMode?';':'\t'); else sb.append(csvMode?';':' ');

		if (event!=null && !event.isEmpty()) {
			if (csvMode) {
				sb.append(toCSV(event.replace("\n"," ")));
			} else {
				sb.append(event.replace("\n",System.lineSeparator()));
			}
		}

		if (printIDs) {
			if (singleLineMode) sb.append(csvMode?';':'\t'); else sb.append(System.lineSeparator());
			final String idString=(id>=0)?(""+id):"";
			if (csvMode) {
				sb.append(toCSV(idString));
			} else {
				sb.append(idString);
			}
		}

		if (info!=null && !info.isEmpty()) {
			if (singleLineMode) sb.append(csvMode?';':'\t'); else sb.append(System.lineSeparator());
			if (csvMode) {
				sb.append(toCSV(info.replace("\n"," ")));
			} else {
				sb.append(info.replace("\n",System.lineSeparator()));
			}
		}

		sb.append(System.lineSeparator());

		if (nextLogger!=null) nextLogger.log(time,color,event,id,info);

		return writeString(sb.toString());
	}
}

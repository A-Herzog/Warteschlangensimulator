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

import simcore.SimData;

/**
 * Gibt Logging-Daten als einfachen Text aus, d.h. die jeweils angegebene Farbe pro Eintrag wird bei der Ausgabe ignoriert.
 * @author Alexander Herzog
 * @see SimLogging
 */
public class PlainTextLogger extends AbstractTextLogger {
	private final boolean groupSameTimeEvents;
	private final boolean singleLineMode;
	private final boolean useDate;
	private long lastEventTime=-1;

	/**
	 * Konstruktor der Klasse <code>PlainTextLogger</code>
	 * @param logFile	Dateiname der Logfile-Datei
	 * @param groupSameTimeEvents	Nach Einträgen mit demselben Zeitstempel eine Leerzeile einfügen
	 * @param singleLineMode	Ereignisse in einer Zeile (Name und Beschreibung durch Tabulator getrennt) oder in mehreren Zeilen ausgeben
	 */
	public PlainTextLogger(final File logFile, final boolean groupSameTimeEvents, final boolean singleLineMode) {
		this(logFile,groupSameTimeEvents,singleLineMode,false);
	}

	/**
	 * Konstruktor der Klasse <code>PlainTextLogger</code>
	 * @param logFile	Dateiname der Logfile-Datei
	 * @param groupSameTimeEvents	Nach Einträgen mit demselben Zeitstempel eine Leerzeile einfügen
	 * @param singleLineMode	Ereignisse in einer Zeile (Name und Beschreibung durch Tabulator getrennt) oder in mehreren Zeilen ausgeben
	 * @param useDate	Gibt an, ob Zeitangaben mit Datum ausgegeben werden sollen
	 */
	public PlainTextLogger(final File logFile, final boolean groupSameTimeEvents, final boolean singleLineMode, final boolean useDate) {
		this.groupSameTimeEvents=groupSameTimeEvents;
		this.singleLineMode=singleLineMode;
		this.useDate=useDate;
		init(logFile);
	}

	@Override
	public boolean log(final long time, final Color color, final String event, final String info) {
		final StringBuilder sb=new StringBuilder();

		if (groupSameTimeEvents && lastEventTime!=time) {
			if (lastEventTime>=0) sb.append(System.lineSeparator());
			lastEventTime=time;
		}

		sb.append(useDate?SimData.formatSimDateTime(time):SimData.formatSimTime(time));
		if (singleLineMode) sb.append("\t"); else sb.append(" ");

		if (event!=null && !event.isEmpty()) {
			sb.append(event.replace("\n",System.lineSeparator()));
		}

		if (info!=null && !info.isEmpty()) {
			if (singleLineMode) sb.append("\t"); else sb.append(System.lineSeparator());
			sb.append(info.replace("\n",System.lineSeparator()));
		}

		sb.append(System.lineSeparator());

		if (nextLogger!=null) nextLogger.log(time,color,event,info);

		return writeString(sb.toString());
	}
}

/**
 * Copyright 2022 Alexander Herzog
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

/**
 * Gibt Logging-Daten als einfachen Text aus, d.h. die jeweils angegebene Farbe pro Eintrag wird bei der Ausgabe ignoriert.<br>
 * Es werden dabei nur die letzten Zeilen ausgegeben. Alles davor wird verworfen. Die Anzahl an Zeilen kann konfiguriert werden.
 * @author Alexander Herzog
 * @see SimLogging
 */
public class PlainTextLoggerLimited extends AbstractTextLogger {
	/** Maximalgröße des Ringpuffers */
	private static  final int MAX_RECORDS_LIMIT=1_000_000;

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
	/** Maximal im Ringpuffer vorzuhaltende Anzahl an Zeilen */
	private final int maxRecords;
	/** Zeitpunkt an dem das letzte Ereignis auftrat (für das optionale Gruppieren) */
	private long lastEventTime=-1;
	/** Ringpuffer für die Ausgabezeilen */
	private StringBuilder[] lines;
	/** Index des nächsten zu verwendenden Index in {@link #lines} */
	private int nextRecord;

	/**
	 * Konstruktor der Klasse <code>PlainTextLogger</code>
	 * @param logFile	Dateiname der Logfile-Datei
	 * @param groupSameTimeEvents	Nach Einträgen mit demselben Zeitstempel eine Leerzeile einfügen
	 * @param singleLineMode	Ereignisse in einer Zeile (Name und Beschreibung durch Tabulator getrennt) oder in mehreren Zeilen ausgeben
	 * @param timeMode	Wie sollen Zeitangaben ausgegeben werden?
	 * @param printIDs	IDs mit ausgeben
	 * @param csvMode	Text im CSV-Modus (<code>true</code>) oder tabulator-getrennt (<code>false</code>) ausgeben
	 * @param maxRecords	Maximal im Ringpuffer vorzuhaltende Anzahl an Zeilen
	 */
	public PlainTextLoggerLimited(final File logFile, final boolean groupSameTimeEvents, final boolean singleLineMode, final PlainTextLoggerTimeMode timeMode, final boolean printIDs, final boolean csvMode, final int maxRecords) {
		this.groupSameTimeEvents=groupSameTimeEvents;
		this.singleLineMode=singleLineMode;
		this.timeMode=timeMode;
		this.csvMode=csvMode;
		this.printIDs=printIDs;
		this.maxRecords=Math.max(1,Math.min(MAX_RECORDS_LIMIT,maxRecords));
		init(logFile);
	}

	/**
	 * Konstruktor der Klasse <code>PlainTextLogger</code>
	 * @param logFile	Dateiname der Logfile-Datei
	 * @param groupSameTimeEvents	Nach Einträgen mit demselben Zeitstempel eine Leerzeile einfügen
	 * @param singleLineMode	Ereignisse in einer Zeile (Name und Beschreibung durch Tabulator getrennt) oder in mehreren Zeilen ausgeben
	 * @param printIDs	IDs mit ausgeben
	 * @param csvMode	Text im CSV-Modus (<code>true</code>) oder tabulator-getrennt (<code>false</code>) ausgeben
	 * @param maxRecords	Maximal im Ringpuffer vorzuhaltende Anzahl an Zeilen
	 */
	public PlainTextLoggerLimited(final File logFile, final boolean groupSameTimeEvents, final boolean singleLineMode, final boolean printIDs, final boolean csvMode, final int maxRecords) {
		this(logFile,groupSameTimeEvents,singleLineMode,PlainTextLoggerTimeMode.TIME,printIDs,csvMode,maxRecords);
	}

	@Override
	protected void writeHeader() {
		lines=new StringBuilder[maxRecords];
	}

	/**
	 * Liefert das nächste zu verwendende {@link StringBuilder}-Objekt.<br>
	 * Das Objekt wird bei Bedarf angelegt und in {@link #lines} eingefügt
	 * oder aber zurückgesetzt.
	 * @return	Nächstes für Logging-Ausgaben zu verwendendes {@link StringBuilder}-Objekt
	 */
	private StringBuilder getNextLineBuilder() {
		StringBuilder line=lines[nextRecord];
		if (line==null) {
			line=lines[nextRecord]=new StringBuilder();
		} else {
			line.setLength(0);
		}

		nextRecord++;
		if (nextRecord==maxRecords) nextRecord=0;

		return line;
	}

	@Override
	public boolean log(final long time, final Color color, final String event, final int id, final String info) {
		if (groupSameTimeEvents && lastEventTime!=time) {
			if (lastEventTime>=0) getNextLineBuilder().append(System.lineSeparator());
			lastEventTime=time;
		}

		PlainTextLogger.processLine(timeMode,singleLineMode,printIDs,csvMode,time,color,event,id,info,getNextLineBuilder());

		if (nextLogger!=null) nextLogger.log(time,color,event,id,info);

		return true;
	}

	@Override
	protected void writeFooter() {
		int recordNr=nextRecord;
		do {
			if (lines[recordNr]!=null) writeString(lines[recordNr].toString());
			recordNr++;
			if (recordNr==maxRecords) recordNr=0;
		} while (recordNr!=nextRecord);
	}
}

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
package simulator.logging;

import java.awt.Color;
import java.io.File;

import simcore.logging.HTMLLogger;
import simcore.logging.PlainTextLogger;
import simcore.logging.PlainTextLoggerTimeMode;
import simcore.logging.RTFLogger;
import simcore.logging.SimLogging;

/**
 * Der Multi-Type-Logger kann Logdaten in verschiedenen Formaten abhängig vom Dateinamen speichern.
 * Er greift dabei auf die Logging-Backends für die verschiedenen Formate zurück.
 * @author Alexander Herzog
 */
public class MultiTypeTextLogger implements SimLogging {
	/** Konkret zu verwendendes Logging-Backend */
	private SimLogging logger;

	/** Nachgeschalteter zweiter Logger, an den alle Logging-Daten ebenfalls übergeben werden. */
	private SimLogging nextLogger;

	/**
	 * Konstruktor der Klasse <code>MultiTypeTextLogger</code>
	 * @param logFile	Dateiname der Logfile-Datei (bestimmt das Logformat)
	 * @param groupSameTimeEvents	Nach Einträgen mit demselben Zeitstempel eine Leerzeile einfügen
	 * @param singleLineMode	Ereignisse in einer Zeile oder in mehreren Zeilen ausgeben
	 * @param useColors	Bei den Log-Zeilen angegebene Farben berücksichtigen
	 * @param formatedTime	Zeit als HH:MM:SS,s (<code>true</code>) oder als Sekunden-Zahlenwert (<code>false</code>) ausgeben
	 * @param printIDs	IDs mit ausgeben
	 * @param headings	Auszugebende Überschriftzeilen
	 */
	public MultiTypeTextLogger(final File logFile, final boolean groupSameTimeEvents, final boolean singleLineMode, final boolean useColors, final boolean formatedTime, final boolean printIDs, final String[] headings) {
		final String filename=logFile.getName().toUpperCase();

		SimLogging l=null;
		if (filename.endsWith(".RTF")) l=new RTFLogger(logFile,groupSameTimeEvents,singleLineMode,useColors,formatedTime,printIDs,headings);
		if (filename.endsWith(".HTML")) l=new HTMLLogger(logFile,groupSameTimeEvents,singleLineMode,useColors,formatedTime,printIDs,headings);
		if (filename.endsWith(".DOCX")) l=new DOCXLogger(logFile,groupSameTimeEvents,singleLineMode,useColors,formatedTime,printIDs,headings);
		if (filename.endsWith(".ODT")) l=new ODTLogger(logFile,groupSameTimeEvents,singleLineMode,useColors,formatedTime,printIDs,headings);
		if (filename.endsWith(".XLSX")) l=new XLSXLogger(logFile,groupSameTimeEvents,singleLineMode,useColors,formatedTime,printIDs,headings,false);
		if (filename.endsWith(".XLS")) l=new XLSXLogger(logFile,groupSameTimeEvents,singleLineMode,useColors,formatedTime,printIDs,headings,true);
		if (filename.endsWith(".ODS")) l=new ODSLogger(logFile,groupSameTimeEvents,singleLineMode,useColors,formatedTime,printIDs,headings);
		if (filename.endsWith(".PDF")) l=new PDFLogger(logFile,groupSameTimeEvents,singleLineMode,useColors,formatedTime,printIDs,headings);
		if (filename.endsWith(".CSV")) l=new PlainTextLogger(logFile,groupSameTimeEvents,singleLineMode,formatedTime?PlainTextLoggerTimeMode.TIME:PlainTextLoggerTimeMode.PLAIN,printIDs,true);
		if (l==null) l=new PlainTextLogger(logFile,groupSameTimeEvents,singleLineMode,formatedTime?PlainTextLoggerTimeMode.TIME:PlainTextLoggerTimeMode.PLAIN,printIDs,false);
		logger=l;
	}

	@Override
	public boolean ready() {
		return (logger!=null && logger.ready());
	}

	@Override
	public boolean log(final long time, final Color color, final String event, final int id, final String info) {
		if (logger!=null) logger.log(time,color,event,id,info);
		if (nextLogger!=null) nextLogger.log(time,color,event,id,info);
		return true;
	}

	@Override
	public boolean done() {
		if (logger!=null) logger.done();
		if (nextLogger!=null) nextLogger.done();
		return true;
	}

	@Override
	public void setNextLogger(final SimLogging logger) {
		nextLogger=logger;
	}

	@Override
	public SimLogging getNextLogger() {
		return nextLogger;
	}
}
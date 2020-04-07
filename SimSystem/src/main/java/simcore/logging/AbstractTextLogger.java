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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

/**
 * Diese abstrakte Basisklasse ermöglicht das Aufzeichnen von Plain-Text-Logging-Daten in einer Datei
 * @author Alexander Herzog
 */
public abstract class AbstractTextLogger implements SimLogging {
	private OutputStreamWriter logFileWriter=null;

	/**
	 * Optionaler nachgeschalteter weiterer Logger
	 */
	protected SimLogging nextLogger;

	/**
	 * Initialisiert die Aufzeichnung<br>
	 * Im Erfolgfall wird <code>writeHeader</code> aufgerufen, um einen optionalen Dateikopf zu schreiben
	 * @param logFile	Name der Logdatei
	 * @see #writeHeader()
	 */
	public void init(final File logFile) {
		logFileWriter=initOutputStreamWriter(logFile);
		if (logFileWriter!=null) writeHeader();
	}

	private OutputStreamWriter initOutputStreamWriter(final File logFile) {
		if (logFile==null) return null;
		try {
			return new OutputStreamWriter(new FileOutputStream(logFile,true),StandardCharsets.UTF_8);
		} catch (IOException e) {return null;}
	}

	@Override
	public final boolean ready() {
		return (logFileWriter!=null);
	}

	/**
	 * Bietet die optionale Möglichkeit, einen Dateikopf zu schreiben.<br>
	 * Diese Methode wird von <code>AbstractTextLogger</code> nur aufgerufen, wenn das System erfolgreich initialisiert werden konnte.
	 */
	protected void writeHeader() {}

	/**
	 * Bietet die optionale Möglichkeit, einen Fußbereich zu schreiben.<br>
	 * Diese Methode wird von <code>AbstractTextLogger</code> nur aufgerufen, wenn das System erfolgreich initialisiert werden konnte.
	 */
	protected void writeFooter() {}

	/**
	 * Schreibt einen Text in die Logdatei
	 * @param s	Zu schreibender Text (kann mehrere Zeilen umfassen)
	 * @return	Liefert <code>true</code> wenn der Text erfolgreich geschrieben werden konnte oder aber das System überhaupt nicht erfolgreich initialisiert werden konnte
	 */
	protected final boolean writeString(final String s) {
		if (logFileWriter==null) return true;

		try {
			logFileWriter.write(s);
		} catch (IOException e) {return false;}
		return true;

	}

	@Override
	public abstract boolean log(long time, Color color, String event, String info);

	@Override
	public boolean done() {
		if (nextLogger!=null) nextLogger.done();

		if (logFileWriter==null) return false;
		writeFooter();

		try {
			logFileWriter.flush();
			logFileWriter.close();
		} catch (IOException e) {logFileWriter=null; /* Damit SpotBugs zufrieden ist. */ return false;}
		logFileWriter=null;
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
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
import java.util.ArrayList;
import java.util.List;

import simcore.logging.CallbackLogger;

/**
 * Diese Klasse erweitert die Funktionalit�t des Callback-Loggers
 * dahingehend, dass auch Ergebnisse von JS-Code-Ausf�hrungen
 * erfasst und zwischengespeichert werden k�nnen. In Bezug
 * auf die normalen Logging-Funktionen ist dieser Logger identisch
 * zum {@link CallbackLogger}.
 * @author Alexander Herzog
 * @see CallbackLogger
 */
public class CallbackLoggerWithJS extends CallbackLogger {
	/** Maximal vorzuhalten Anzahl an JS-Ergebnissen */
	private static final int JS_LOG_MAX=100;

	/** Erfasste JS-Ergebnisse */
	private final List<JSData> jsLogging=new ArrayList<>();

	/**
	 * Erfasst eine JS-Code-Ausf�hrung
	 * @param time	Zeitpunkt des Ereignisses
	 * @param station	Station an der der JS-Code ausgef�hrt wurde
	 * @param color	Farbe in die die Log-Zeile eingef�rbt werden soll
	 * @param script	Ausgef�hrtes Skript
	 * @param result	Ergebnis der Ausf�hrung
	 */
	public void logJS(final long time, final String station, final Color color, final String script, final String result) {
		final JSData data;
		if (jsLogging.size()>JS_LOG_MAX) {
			data=jsLogging.remove(0);
			while (jsLogging.size()>JS_LOG_MAX) jsLogging.remove(0);
		} else {
			data=new JSData();
		}
		data.init(time,station,color,script,result);
		jsLogging.add(data);
	}

	/**
	 * Liefert die Liste der Ergebnisse der letzten JS-Code-Ausf�hrungen.
	 * @return	Liste der Ergebnisse der letzten JS-Code-Ausf�hrungen
	 */
	public List<JSData> getJSData() {
		return jsLogging;
	}

	/**
	 * Informationen zu einer JS-Code-Ausf�hrung
	 * @author Alexander Herzog
	 */
	public static class JSData {
		/** Zeitpunkt (in Simulationszeit) der Skriptausf�hrung */
		public long time;
		/** Name der Station an der die Skriptausf�hrung veranlasst wurde */
		public String station;
		/** Farbe der Station (f�r farbige Loggingausgaben) */
		public Color color;
		/** Ausgef�hrtes Skript */
		public String script;
		/** R�ckgabewert */
		public String result;

		/**
		 * Konstruktor der Klasse
		 */
		public JSData() {
		}

		/**
		 * Konstruktor der Klasse
		 * @param time	Zeitpunkt (in Simulationszeit) der Skriptausf�hrung
		 * @param station	Name der Station an der die Skriptausf�hrung veranlasst wurde
		 * @param color	Farbe der Station (f�r farbige Loggingausgaben)
		 * @param script	Ausgef�hrtes Skript
		 * @param result	R�ckgabewert
		 */
		public void init(final long time, final String station, final Color color, final String script, final String result) {
			this.time=time;
			this.station=station;
			this.color=color;
			this.script=script;
			this.result=result;
		}
	}
}

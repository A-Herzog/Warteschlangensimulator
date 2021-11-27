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
import java.util.function.Consumer;

import simcore.SimData;

/**
 * Reicht die Logging-Daten an ein übergeordnetes Element per Callback weiter.
 * @author Alexander Herzog
 * @see CallbackLoggerData
 * @see SimData
 */
public class CallbackLogger implements SimLogging {
	/** Callback-Funktion, die bei einem Log-Ereignis aufgerufen werden soll */
	private Consumer<CallbackLoggerData> callback;
	/** Soll der Logger aktiv sein? */
	private boolean active;

	/**
	 * Optionaler nachgeschalteter weiterer Logger
	 */
	protected SimLogging nextLogger;

	/**
	 * Objekt welches mit konkreten Daten befüllt wird und an das Callback weitergegeben wird.
	 * @see #log(long, Color, String, int, String)
	 */
	private final CallbackLoggerData loggerData;

	/**
	 * Konstruktor der Klasse <code>CallbackLogger</code><br>
	 * Initialisiert den Logger im inaktiven Status
	 */
	public CallbackLogger() {
		this(null,false);
	}

	/**
	 * Konstruktor der Klasse <code>CallbackLogger</code><br>
	 * Initialisiert den Logger im aktiven Status
	 * @param callback	Callback-Funktion, die bei einem Log-Ereignis aufgerufen werden soll
	 * @see CallbackLoggerData
	 */
	public CallbackLogger(final Consumer<CallbackLoggerData> callback) {
		this(callback,true);
	}

	/**
	 * Konstruktor der Klasse <code>CallbackLogger</code>
	 * @param callback	Callback-Funktion, die bei einem Log-Ereignis aufgerufen werden soll
	 * @param active Gibt an, ob der Logger von Anfang an aktiv sein soll
	 * @see CallbackLoggerData
	 */
	public CallbackLogger(final Consumer<CallbackLoggerData> callback, final boolean active) {
		this.callback=callback;
		this.active=active;
		loggerData=new CallbackLoggerData(0,Color.BLACK,"",0,"");
	}

	/**
	 * Gibt an, ob der Logger aktiv ist, d.h. Ereignisse über die Callback-Funktion zurückliefert.
	 * @return	Gibt <code>true</code> zurück, wenn der Logger aktiv ist.
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * Stellt ein, ob der Logger aktiv sein soll, d.h. ob er Ereignisse über die Callback-Funktion zurückliefern soll.
	 * @param active	Sollen momentan Ereignisse zum Callback durchgereicht werden?
	 */
	public void setActive(final boolean active) {
		this.active=active;
	}

	/**
	 * Stellt eine neue Callback-Funktion ein.
	 * @param callback	Callback-Funktion, die bei einem Log-Ereignis aufgerufen werden soll
	 */
	public void setCallback(final Consumer<CallbackLoggerData> callback) {
		this.callback=callback;
	}

	@Override
	public boolean ready() {
		return (callback!=null);
	}

	@Override
	public boolean log(final long time, final Color color, final String event, final int id, final String info) {
		if (active && callback!=null) {
			loggerData.init(time,(color==null)?Color.BLACK:color,event,id,info);
			callback.accept(loggerData);
		}
		if (nextLogger!=null) nextLogger.log(time,color,event,id,info);
		return true;
	}

	@Override
	public boolean done() {
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

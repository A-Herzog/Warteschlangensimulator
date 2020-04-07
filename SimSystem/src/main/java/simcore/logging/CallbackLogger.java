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
 * Reicht die Logging-Daten an ein �bergeordnetes Element per Callback weiter.
 * @author Alexander Herzog
 * @see CallbackLoggerData
 * @see SimData
 */
public class CallbackLogger implements SimLogging {
	private Consumer<CallbackLoggerData> callback;
	private boolean active;

	/**
	 * Optionaler nachgeschalteter weiterer Logger
	 */
	protected SimLogging nextLogger;

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
	}

	/**
	 * Gibt an, ob der Logger aktiv ist, d.h. Ereignisse �ber die Callback-Funktion zur�ckliefert.
	 * @return	Gibt <code>true</code> zur�ck, wenn der Logger aktiv ist.
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * Stellt ein, ob der Logger aktiv sein soll, d.h. ob er Ereignisse �ber die Callback-Funktion zur�ckliefern soll.
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
	public boolean log(long time, Color color, String event, String info) {
		if (active && callback!=null) callback.accept(new CallbackLoggerData(time,(color==null)?Color.BLACK:color,event,info));
		if (nextLogger!=null) nextLogger.log(time,color,event,info);
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

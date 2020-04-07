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

import simcore.SimData;

/**
 * Über diese Klasse informiert der <code>CallbackLogger</code> die
 * Callback-Funktion über neue Ereignisse.
 * @author Alexander Herzog
 * @see CallbackLogger
 */
public class CallbackLoggerData {
	/**
	 * Zeitpunkt des Ereignisses (in ms)
	 */
	public final long timeStamp;

	/**
	 * Zeitpunkt des Ereignisses als formatierte Zeichenkette
	 */
	public final String time;

	/**
	 * Farbe, in der das Ereignis dargestellt werden sollte
	 */
	public final Color color;

	/**
	 * Name des Ereignisses
	 */
	public final String event;

	/**
	 * Zusätzliche Beschreibung zu dem Ereignis
	 */
	public final String info;

	/**
	 * Konstruktor der Klasse
	 * @param time	Zeitpunkt des Ereignisses (in ms)
	 * @param color	Farbe, in der das Ereignis dargestellt werden sollte
	 * @param event	Name des Ereignisses
	 * @param info	Zusätzliche Beschreibung zu dem Ereignis
	 */
	public CallbackLoggerData(final long time, final Color color, final String event, final String info) {
		this.timeStamp=time;
		this.time=SimData.formatSimTime(time);
		this.color=color;
		this.event=event;
		this.info=info;
	}
}

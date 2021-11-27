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
	public long timeStamp;

	/**
	 * Zeitpunkt des Ereignisses als formatierte Zeichenkette
	 */
	public String time;

	/**
	 * Farbe, in der das Ereignis dargestellt werden sollte
	 */
	public Color color;

	/**
	 * Name des Ereignisses
	 */
	public String event;

	/**
	 * ID der Station, die das Ereignis ausgelöst hat (Werte kleiner als 0 stehen für "keine Station")
	 */
	public int id;

	/**
	 * Zusätzliche Beschreibung zu dem Ereignis
	 */
	public String info;

	/**
	 * Konstruktor der Klasse
	 * @param time	Zeitpunkt des Ereignisses (in ms)
	 * @param color	Farbe, in der das Ereignis dargestellt werden sollte
	 * @param event	Name des Ereignisses
	 * @param id	ID der Station, an der das Ereignis stattfand (Werte kleiner als 0 für "keine Station")
	 * @param info	Zusätzliche Beschreibung zu dem Ereignis
	 */
	public CallbackLoggerData(final long time, final Color color, final String event, final int id, final String info) {
		this.timeStamp=time;
		this.time=SimData.formatSimTime(time);
		this.color=color;
		this.event=event;
		this.id=id;
		this.info=info;
	}

	/**
	 * Reinitialisiert das Objekt
	 * @param time	Zeitpunkt des Ereignisses (in ms)
	 * @param color	Farbe, in der das Ereignis dargestellt werden sollte
	 * @param event	Name des Ereignisses
	 * @param id	ID der Station, an der das Ereignis stattfand (Werte kleiner als 0 für "keine Station")
	 * @param info	Zusätzliche Beschreibung zu dem Ereignis
	 */
	public void init(final long time, final Color color, final String event, final int id, final String info) {
		this.timeStamp=time;
		this.time=SimData.formatSimTime(time);
		this.color=color;
		this.event=event;
		this.id=id;
		this.info=info;
	}
}

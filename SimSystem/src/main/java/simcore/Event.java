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
package simcore;

import simcore.eventcache.AssociativeEventCache;

/**
 * Basisklasse f�r alle Ereignisses w�hrend der Simulation
 * @author Alexander Herzog
 * @version 1.0
 */
public abstract class Event implements Comparable<Event> {
	/**
	 * Wird von {@link AssociativeEventCache} verwendet.
	 */
	public Class<? extends Event> cacheClass;

	/**
	 * Wird von {@link AssociativeEventCache} verwendet.
	 */
	public int cacheClassHash;

	/**
	 * Ist in <code>addNextEvent</code> ein Ereignisses eingetragen, so wird das
	 * <code>addNextEvent</code> beim Ausf�hren dieses Ereignisses vom EventManager
	 * in die Liste eingetragen.
	 */
	public Event addNextEvent;

	/**
	 * Geplanter Ausf�hrungszeitpunkt des Ereignisses<br><br>
	 * (Wird von <code>init(time)</code> gesetzt.)
	 */
	public long time;

	/**
	 * Wird dieses Feld auf <code>true</code> gesetzt, so wird das Ereignis bei der Bearbeitung �bersprungen.<br>
	 * (Normalerweise werden Ereignisse beim L�schen direkt aus der Ereignisliste entfernt. Wenn es sich jedoch
	 * um ein dort noch gar nicht eingef�gtes, nur verkettet vorhandenes Ereignis handelt, muss dieser Weg gew�hlt werden.)
	 */
	public boolean isDeleted;

	/**
	 * Initialisierung des Ereignisses<br><br>
	 * Sollte in abgeleiteten Klassen �berschrieben werden, um weitere Felder zu initialisieren.<br>
	 * (Da Ereignisse recycled werden, erfolgt die Initialisierung in <code>init</code> und
	 * nicht im Konstruktor.)
	 * @param time	Geplanter Ausf�hrungszeitpunkt des Ereignisses
	 */
	public final void init(final long time) {
		this.time=time;
		isDeleted=false;
		addNextEvent=null;
	}

	/**
	 * Ausf�hren des Ereignisses<br><br>
	 * @param	data	Objekt vom Typ <code>SimData</code> welche alle statischen und dynamischen Daten zur Simulation enth�lt.
	 */
	public abstract void run(final SimData data);

	/**
	 * Compareable-Implementierung, damit der EventManager Ereignisse gem�� der
	 * geplanten Ausf�hrungszeit sortieren kann
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public final int compareTo(final Event o) {
		return Long.signum(time-o.time);
	}
}
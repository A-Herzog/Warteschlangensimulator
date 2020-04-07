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
package simcore.eventcache;

import simcore.Event;

/**
 * Das <code>EventCache</code>-Interface definiert Funktionen zum Zwischenspeichern und
 * Wiederverwenden bereits ausgef�hrter Ereignisobjekte, so dass der (nichtparallelisierbare)
 * Speicherreservierungsaufwand minimiert wird.
 * @author Alexander Herzog
 * @version 1.0
 */
public interface EventCache {
	/**
	 * Speichert das �bergebene Ereignis im Cache. Das Ereignis darf danach nicht mehr verwendet werden.
	 * @param event	Das zu recycelnde Ereignis
	 */
	public void put(final Event event);

	/**
	 * Liefert ein Ereignis des im Parameter angegebenen Typs zur�ck. Wenn m�glich wird das Ereignis aus dem
	 * Cache geholt. Existiert dort kein passendes Ereignis, so wird ein neues Ereignis des Typs angelegt.
	 * Das Ereignis muss danach in jedem Fall per <code>init</code> initialisiert werden.
	 * @param eventClass Typ des gew�nschten Ereignisobjektes
	 * @return Liefert ein Ereignis des gew�nschten Typs
	 */
	public Event get(final Class<? extends Event> eventClass);

	/**
	 * Liefert ein Ereignis des im Parameter angegebenen Typs zur�ck. Das Ereignis wird aus dem
	 * Cache geholt. Existiert dort kein passendes Ereignis, so wird <code>null</code> zur�ckgeliefert.
	 * @param eventClass Typ des gew�nschten Ereignisobjektes
	 * @return Liefert ein Ereignis des gew�nschten Typs
	 */
	public Event getOrNull(final Class<? extends Event> eventClass);

	/**
	 * L�scht den gesamten Cache.
	 */
	public void clear();
}

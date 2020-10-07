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
package simcore.eventmanager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import simcore.Event;

/**
 * Diese in umgekehrter Reihenfolge sortierte Liste wird im
 * <code>LongRunMultiSortedArrayListEventManager</code> zur Speicherung
 * der Ereignisse verwendet.
 * @author Alexander Herzog
 * @see LongRunMultiSortedArrayListEventManager
 */
public class SortedEventArrayList extends ArrayList<Event> {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 2998904945709200525L;

	/**
	 * Callback zum Vergleich von zwei Ereignissen in Bezug auf ihre Ausführungszeitpunkte
	 * @see #add(Event)
	 */
	private static final transient Comparator<Event> comparator;

	/**
	 * Wurde bereits ermittelt, welches Ereignis als nächstes in der Liste steht,
	 * so wird dieses hier bereitgestellt.
	 * @see #peek()
	 */
	transient Event lastPeek;

	/**
	 * Über dieses Feld wird die Größe der Liste mitgeführt, so dass diese nicht aus der ArrayList abgerufen werden muss.
	 */
	transient int fastSize;

	static {
		comparator=(o1,o2)->{
			final long l=o2.time-o1.time;
			return (int) ((l >> 63) | (-l >>> 63));
		};
	}

	/**
	 * Konstruktor der Klasse
	 */
	public SortedEventArrayList() {
		super(1024);
		fastSize=0;
	}

	@Override
	public boolean add(final Event e) {
		if (fastSize==0) {
			super.add(e);
			fastSize++;
			lastPeek=e;
			return true;
		}

		int index=Collections.binarySearch(this,e,comparator);
		if (index<0) index=~index;
		super.add(index,e);
		fastSize++;

		if (lastPeek!=null) {
			/* Ist das neue Element erst später dran, brauchen wir lastPeek nicht verwerfen */
			if (e.compareTo(lastPeek)<0) lastPeek=null;
		}

		return true;
	}

	/**
	 * Liefert das nächste wartende Ereignis, ohne es aus der Liste zu entfernen.
	 * @return	Nächstes Ereignis oder <code>null</code>, wenn keine Ereignisse warten.
	 */
	public Event peek() {
		if (lastPeek!=null) return lastPeek;

		if (fastSize==0) return null;
		lastPeek=get(fastSize-1);
		return lastPeek;
	}

	/**
	 * Liefert das nächste wartende Ereignis und entfernt es dabei aus der Liste.
	 * @return	Nächstes Ereignis oder <code>null</code>, wenn keine Ereignisse warten.
	 */
	public Event poll() {
		if (fastSize==0) return null;
		if (fastSize==1 && lastPeek!=null) {
			final Event e=lastPeek;
			super.clear();
			lastPeek=null;
			fastSize=0;
			return e;
		}
		lastPeek=null;
		/* assert(fastSize==size()); */
		final Event e=remove(fastSize-1);
		fastSize--;
		return e;
	}

	@Override
	public void clear() {
		if (fastSize>0) super.clear();
		lastPeek=null;
		fastSize=0;
	}

	/**
	 * Entfernt ein Ereignis aus der Liste der wartenden Ereignisse.
	 * @param e	Zu entfernendes Ereignis.
	 * @return	Gibt an, ob das Ereignis entfernt werden konnte (<code>true</code>) oder ob es sich gar nicht in der Liste befand (<code>false</code>)
	 */
	public boolean remove(final Event e) {
		lastPeek=null;
		final boolean ok=super.remove(e);
		if (ok) fastSize--;
		return ok;
	}
}

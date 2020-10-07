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

/**
 * Diese in umgekehrter Reihenfolge sortierte Liste wird im
 * <code>LongRunMultiSortedArrayListEventManager</code> zur Speicherung
 * der Ereignisse verwendet.
 * @author Alexander Herzog
 * @param <E> Typ der Array-Einträge
 * @see LongRunMultiSortedArrayListEventManager
 */
public class SortedArrayList<E extends Comparable<E>> extends ArrayList<E> {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -1952570370811962117L;

	/**
	 * Callback zum Vergleich von zwei Ereignissen in Bezug auf ihre Ausführungszeitpunkte
	 * @see #add(Comparable)
	 */
	private final transient Comparator<E> comparator;

	/**
	 * Wurde bereits ermittelt, welches Ereignis als nächstes in der Liste steht,
	 * so wird dieses hier bereitgestellt.
	 * @see #peek()
	 */
	E lastPeek;

	/**
	 * Konstruktor der Klasse
	 */
	public SortedArrayList() {
		super(128);
		comparator=(o1,o2)->o2.compareTo(o1);
	}

	@Override
	public boolean add(final E e) {
		if (isEmpty()) {super.add(e); return true;}

		int index=Collections.binarySearch(this,e,comparator);
		if (index<0) index=~index;
		super.add(index,e);

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
	public E peek() {
		if (lastPeek!=null) return lastPeek;

		final int size=size();
		if (size==0) return null;
		lastPeek=get(size-1);
		return lastPeek;
	}

	/**
	 * Liefert das nächste wartende Ereignis und entfernt es dabei aus der Liste.
	 * @return	Nächstes Ereignis oder <code>null</code>, wenn keine Ereignisse warten.
	 */
	public E poll() {
		final int size=size();
		if (size==0) return null;
		if (size==1 && lastPeek!=null) {
			final E e=lastPeek;
			lastPeek=null;
			super.clear();
			return e;
		}
		lastPeek=null;
		return remove(size-1);
	}

	@Override
	public void clear() {
		lastPeek=null;
		super.clear();
	}

	/**
	 * Entfernt ein Ereignis aus der Liste der wartenden Ereignisse.
	 * @param e	Zu entfernendes Ereignis.
	 * @return	Gibt an, ob das Ereignis entfernt werden konnte (<code>true</code>) oder ob es sich gar nicht in der Liste befand (<code>false</code>)
	 */
	public boolean remove(final E e) {
		lastPeek=null;
		return super.remove(e);
	}
}

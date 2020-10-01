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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import simcore.Event;

/**
 * Die <code>ListEventCache</code>-Klasse implementiert das <code>EventCache</code>-Interfaces.
 * Per <code>put</code> übergebene Ereignisse werden nach Klassentypen getrennt gespeichert und per
 * <code>get</code> wird vorzugsweise ein Ereignis aus dem Cache geliefert. Existiert dort kein
 * Ereignis des gewünschten Typs, so wird ein neues Ereignis angelegt.<br><br>
 * Es werden beliebig viele verschiedene Klassentypen erfasst und pro Klasse per Vorgabe
 * bis zu 2.000 Ereignisse.
 * @author Alexander Herzog
 * @version 1.0
 */
public final class ListEventCache implements EventCache {
	/** Anfängliche Größe der {@link #cacheEventList} Listen */
	private final int initialCacheSizePerClass;
	/** Liste der Klassentypen */
	private final List<Class<? extends Event>> cacheClassList;
	/** Liste der pro Klassentyp gespeicherten Ereignisse */
	private final List<Deque<Event>> cacheEventList;

	/**
	 * Konstruktor für den EventCache
	 * @param initialCacheSizePerClass	Maximal pro Klasse zu cachende Ereignisse
	 */
	public ListEventCache(final int initialCacheSizePerClass) {
		this.initialCacheSizePerClass=initialCacheSizePerClass;
		cacheClassList=new ArrayList<>();
		cacheEventList=new ArrayList<>();
	}

	/**
	 * Konstruktor für den EventCache<br>
	 * Cache pro Klassentyp wird initial auf 2.000 Ereignisse eingestellt.
	 */
	public ListEventCache() {this(2000);}

	@Override
	public Event get(final Class<? extends Event> eventClass) {
		Event event=getOrNull(eventClass);
		if (event!=null) return event;

		try {
			return eventClass.getConstructor().newInstance();
		} catch (Exception e) {return null;}
	}

	@Override
	public void put(final Event event) {
		int i=cacheClassList.indexOf(event.getClass());
		if (i<0) {
			cacheClassList.add(event.getClass());
			cacheEventList.add(new ArrayDeque<>(initialCacheSizePerClass));
			i=cacheClassList.size()-1;
		}
		cacheEventList.get(i).addFirst(event);
	}

	@Override
	public void clear() {
		cacheClassList.clear();
		cacheEventList.clear();
	}

	@Override
	public Event getOrNull(Class<? extends Event> eventClass) {
		final int i=cacheClassList.indexOf(eventClass);
		if (i>=0) {
			Deque<Event> cache=cacheEventList.get(i);
			final Event e=cache.pollFirst();
			if (e!=null) return e;
		}

		return null;
	}
}

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

import java.lang.reflect.Constructor;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Map;

import simcore.Event;

/**
 * Die <code>HashMapEventCache</code>-Klasse implementiert das <code>EventCache</code>-Interfaces.
 * Per <code>put</code> übergebene Ereignisse werden nach Klassentypen getrennt gespeichert und per
 * <code>get</code> wird vorzugsweise ein Ereignis aus dem Cache geliefert. Existiert dort kein
 * Ereignis des gewünschten Typs, so wird ein neues Ereignis angelegt.<br><br>
 * Es werden beliebig viele verschiedene Klassentypen erfasst und pro Klasse beliebig viele Ereignisse.
 * @author Alexander Herzog
 * @version 1.2
 */
public final class HashMapEventCache implements EventCache {
	/** Anfängliche Größe der Listen in {@link #cacheClassList} */
	private final int initialCacheSizePerClass;
	/** Zuordnung von Ereignisse-Klassen und Cache-Listen */
	private final Map<Class<? extends Event>,Deque<Event>> cacheClassList;
	/** Konstruktoren für neue Ereignisse */
	private final Map<Class<? extends Event>,Constructor<Event>> cacheConstructors;

	/**
	 * Konstruktor für den EventCache
	 * @param initialCacheSizePerClass	Maximal pro Klasse zu cachende Ereignisse
	 */
	public HashMapEventCache(final int initialCacheSizePerClass) {
		this.initialCacheSizePerClass=initialCacheSizePerClass;
		/* langsamer, da k1.equals(k2) Vergleiche beim get: cacheClassList=new HashMap<Class<? extends Event>,Deque<Event>>(); */
		cacheClassList=new IdentityHashMap<Class<? extends Event>,Deque<Event>>();
		cacheConstructors=new IdentityHashMap<Class<? extends Event>,Constructor<Event>>();
	}

	/**
	 * Konstruktor für den EventCache<br>
	 * Cache pro Klassentyp wird initial auf 2.000 Ereignisse eingestellt.
	 */
	public HashMapEventCache() {this(2000);}

	@SuppressWarnings("unchecked")
	@Override
	public Event get(final Class<? extends Event> eventClass) {
		Event event=getOrNull(eventClass);
		if (event!=null) return event;

		try {
			Constructor<Event> constructor=cacheConstructors.get(eventClass);
			if (constructor==null) {
				constructor=(Constructor<Event>)eventClass.getConstructor();
				cacheConstructors.put(eventClass,constructor);
			}
			return constructor.newInstance();
		} catch (Exception e) {return null;}
	}

	@Override
	public void put(final Event event) {
		Deque<Event> list=cacheClassList.get(event.getClass());
		if (list==null) cacheClassList.put(event.getClass(),list=new ArrayDeque<>(initialCacheSizePerClass));
		list.push(event);
	}

	@Override
	public void clear() {
		for (Deque<Event> list : cacheClassList.values()) list.clear();
		cacheClassList.clear();
	}

	@Override
	public Event getOrNull(Class<? extends Event> eventClass) {
		final Deque<Event> list=cacheClassList.get(eventClass);
		if (list!=null && !list.isEmpty()) {
			return list.pollFirst(); /* kann auch null sein, macht aber nichts, da wir unten im Falle poll==null auch null zurückgeben würden */
			/* überfüssig: if (e!=null) return e; */
		}

		return null;
	}
}

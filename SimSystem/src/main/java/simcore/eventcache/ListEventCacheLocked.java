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
import java.util.concurrent.Semaphore;

import simcore.Event;

/**
 * Die <code>ListEventCacheLocked</code>-Klasse implementiert das <code>EventCache</code>-Interfaces.
 * Per <code>put</code> übergebene Ereignisse werden nach Klassentypen getrennt gespeichert und per
 * <code>get</code> wird vorzugsweise ein Ereignis aus dem Cache geliefert. Existiert dort kein
 * Ereignis des gewünschten Typs, so wird ein neues Ereignis angelegt.<br><br>
 * Es werden beliebig viele verschiedene Klassentypen erfasst und pro Klasse per Vorgabe
 * bis zu 20.000 Ereignisse.
 * @author Alexander Herzog
 * @version 1.1
 */
public final class ListEventCacheLocked implements EventCache {
	/** Lock-Objekt, damit dieser Cache für alle Threads gemeinsam genutzt werden kann */
	private final Semaphore lock;
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
	public ListEventCacheLocked(int initialCacheSizePerClass) {
		lock=new Semaphore(1);
		this.initialCacheSizePerClass=initialCacheSizePerClass;
		cacheClassList=new ArrayList<>();
		cacheEventList=new ArrayList<>();
	}

	/**
	 * Konstruktor für den EventCache<br>
	 * Cache pro Klassentyp wird initial auf 10.000 Ereignisse eingestellt.
	 */
	public ListEventCacheLocked() {this(20000);}

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
		lock.acquireUninterruptibly();
		try {
			int i=cacheClassList.indexOf(event.getClass());
			if (i<0) {cacheClassList.add(event.getClass()); cacheEventList.add(new ArrayDeque<>(initialCacheSizePerClass)); i=cacheClassList.size()-1;}
			cacheEventList.get(i).push(event);
		} finally {
			lock.release();
		}
	}

	@Override
	public void clear() {
		lock.acquireUninterruptibly();
		try {
			cacheClassList.clear();
			cacheEventList.clear();
		} finally {
			lock.release();
		}
	}

	@Override
	public Event getOrNull(Class<? extends Event> eventClass) {
		lock.acquireUninterruptibly();
		try {
			int i=cacheClassList.indexOf(eventClass);
			if (i>=0) {
				Deque<Event> cache=cacheEventList.get(i);
				if (!cache.isEmpty()) {return cache.pop();}
			}
		} finally {
			lock.release();
		}

		return null;
	}
}

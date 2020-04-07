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
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;

import org.apache.commons.math3.util.FastMath;

import simcore.Event;

/**
 * Die <code>AssociativeEventCache</code>-Klasse implementiert das <code>EventCache</code>-Interfaces.
 * Per <code>put</code> übergebene Ereignisse werden nach Klassentypen getrennt gespeichert und per
 * <code>get</code> wird vorzugsweise ein Ereignis aus dem Cache geliefert. Der Cache nutzt dabei eine
 * eigene assoziative Struktur zur zeiteffizienten Ablage der Objekte. Existiert im Cache kein
 * Ereignis des gewünschten Typs, so wird ein neues Ereignis angelegt.<br><br>
 * Es werden maximal 2.000 verschiedene Klassentypen erfasst und pro Klasse per Vorgabe
 * bis zu 1.000.000 Ereignisse.
 * @author Alexander Herzog
 * @version 1.6
 */
public final class AssociativeEventCache implements EventCache {
	private static final int SUB_LISTS=100;
	private static final int TYPES_PER_SUB_LIST=20;
	private static final int EVENTS_PER_CLASS=1_000_000;

	private final Map<Class<? extends Event>,Constructor<Event>> cacheConstructors;

	private final int capacityPerType;
	private final int[] level2used;
	private final int[][] level3used;
	private final Class<? extends Event>[][] listTypes;
	private final Event[][][] listEvents;

	private long hit;
	private long miss;

	/**
	 * Konstruktor der Klasse
	 * @param capacityPerType	Kapazität an Ereignissen pro Klasse im Cache
	 */
	@SuppressWarnings("unchecked")
	public AssociativeEventCache(int capacityPerType) {
		this.capacityPerType=capacityPerType;
		listTypes=new Class[SUB_LISTS][];
		listEvents=new Event[SUB_LISTS][][];
		level2used=new int[SUB_LISTS];
		level3used=new int[SUB_LISTS][];
		for (int i=0;i<SUB_LISTS;i++) {
			listTypes[i]=new Class[TYPES_PER_SUB_LIST];
			listEvents[i]=new Event[TYPES_PER_SUB_LIST][];
			level3used[i]=new int[TYPES_PER_SUB_LIST];
		}
		cacheConstructors=new IdentityHashMap<Class<? extends Event>,Constructor<Event>>();

		hit=0;
		miss=0;
	}

	/**
	 * Konstruktor der Klasse<br>
	 * Es wird eine Kapazität an Ereignissen pro Klasse im Cache von 1.000.000 Stück angenommen
	 */
	public AssociativeEventCache() {
		this(EVENTS_PER_CLASS);
	}

	@Override
	public void put(final Event event) {
		if (event.cacheClass==null) {
			event.cacheClass=event.getClass();
			event.cacheClassHash=event.cacheClass.hashCode()%1_000_000_000;
		}

		final Class<? extends Event> eventClass=event.cacheClass;

		int level1=event.cacheClassHash;
		if (level1<0) level1=-level1;
		level1=level1%SUB_LISTS;
		int level2=-1;
		final int used=level2used[level1];
		final Class<? extends Event>[] sub=listTypes[level1];
		for (int i=0;i<used;i++) if (sub[i]==eventClass) {level2=i; break;}

		if (level2==-1) {
			level2=level2used[level1];
			if (level2==TYPES_PER_SUB_LIST) return;
			level2used[level1]++;
			listTypes[level1][level2]=eventClass;
			listEvents[level1][level2]=new Event[capacityPerType];
		}

		int level3=level3used[level1][level2];
		if (level3==capacityPerType) return;
		listEvents[level1][level2][level3]=event;
		level3used[level1][level2]++;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Event get(Class<? extends Event> eventClass) {
		Event event=getOrNull(eventClass);
		if (event!=null) {hit++; return event;}
		miss++;

		try {
			Constructor<Event> constructor=cacheConstructors.get(eventClass);
			if (constructor==null) {
				constructor=(Constructor<Event>)eventClass.getConstructor();
				cacheConstructors.put(eventClass,constructor);
			}
			return constructor.newInstance();
		} catch (Exception e) {return null;}
	}

	/**
	 * Liefert Informationen zur Wirksamkeit des Caches als Text
	 * @return	Informationen zur Wirksamkeit des Caches
	 */
	public String getInfo() {
		final StringBuilder sb=new StringBuilder();
		sb.append("Cache hit/miss: "+hit+"/"+miss+"\n");
		for (int i=0;i<SUB_LISTS;i++) {
			if (level2used[i]==0) continue;
			sb.append("List "+i+": ");
			for (int j=0;j<level2used[i];j++) sb.append(""+level3used[i][j]+" ");
			sb.append("\n");
		}
		return sb.toString();
	}

	@Override
	public void clear() {
		/* System.out.println(getInfo()); */

		Arrays.fill(level2used,0);
		for (int i=0;i<SUB_LISTS;i++) Arrays.fill(level3used[i],0);

		hit=0;
		miss=0;
	}

	@Override
	public Event getOrNull(Class<? extends Event> eventClass) {
		int level1=FastMath.max(0,FastMath.abs(eventClass.hashCode()%1_000_000_000))%SUB_LISTS;
		int level2=-1;
		final int used2=level2used[level1];
		final Class<? extends Event>[] types=listTypes[level1];
		for (int i=0;i<used2;i++) if (types[i]==eventClass) {level2=i; break;}

		if (level2>=0) {
			int level3=level3used[level1][level2];
			if (level3>0) {
				level3used[level1][level2]--;
				return listEvents[level1][level2][level3-1];
			}
		}

		return null;
	}
}

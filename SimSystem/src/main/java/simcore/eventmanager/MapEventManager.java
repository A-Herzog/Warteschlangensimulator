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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;

import simcore.Event;

/**
 * Implementierung eines HashMap-basierenden Test-Event-Manager.
 * Dieser Event-Manager ist nicht für den produktiven Einsatz geeignet, da verhältnismäßig langsam ist.
 * @author Alexander Herzog
 * @version 1.0
 * @see EventManager
 */
public class MapEventManager extends EventManagerBase {
	/** Zuordnung in der die Ereignisse gespeichert werden */
	private final Map<Long,List<Event>> map;
	/** Anzahl an Einträgen in der Zuordnung */
	private int size;

	/**
	 * Konstruktor der Klasse
	 */
	public MapEventManager() {
		map=new HashedMap<>();
		size=0;
	}

	@Override
	public void addEvent(Event event) {
		List<Event> list=map.get(event.time);
		if (list==null) map.put(event.time,list=new ArrayList<>());
		list.add(event);
		size++;
	}

	@Override
	protected Event getNextEvent() {
		List<Event> bestList=null;
		long bestTime=Long.MAX_VALUE;
		for (Map.Entry<Long,List<Event>> entry: map.entrySet()) {
			long time=entry.getKey().longValue();
			if (time<bestTime) {
				bestTime=time;
				bestList=entry.getValue();
			}
		}
		if (bestList==null) return null;

		final Event event=bestList.remove(0);
		if (bestList.isEmpty()) map.remove(bestTime);

		size--;
		return event;
	}

	@Override
	protected boolean onlyDeleteEvent(Event event) {
		final List<Event> list=map.get(event.time);
		if (list==null) return false;
		boolean b=list.remove(event);
		if (b) size--;
		if (list.isEmpty()) map.remove(event.time);
		return b;
	}

	@Override
	public int eventQueueLength() {
		return size;
	}

	@Override
	public void deleteAllEvents() {
		map.clear();
		size=0;
	}

	@Override
	public List<Event> getAllEvents() {
		final List<Event> result=new ArrayList<>();
		for (List<Event> queue: map.values()) result.addAll(queue);
		result.sort(null);
		return result;
	}
}

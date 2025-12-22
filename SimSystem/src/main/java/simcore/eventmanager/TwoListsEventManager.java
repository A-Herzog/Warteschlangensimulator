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

import simcore.Event;

/**
 * Implementierung des <code>EventManager</code>-Interface mit Hilfe von zwei
 * <code>ArrayList</code>-Elementen.
 * @author Alexander Herzog
 * @version 1.0
 * @see EventManager
 * @see ArrayList
 * Bessere Version: {@link MultiArrayEventManager}
 */
public final class TwoListsEventManager extends EventManagerBase implements EventManager {
	/** Teilliste 1 */
	private final List<Event> list1;
	/** Teilliste 2 */
	private final List<Event> list2;

	/**
	 * Konstruktor der Klasse
	 */
	public TwoListsEventManager() {
		list1=new ArrayList<>();
		list2=new ArrayList<>();
	}

	@Override
	protected Event getNextEvent() {
		if (list1.isEmpty()) {
			if (list2.isEmpty()) return null;
			return list2.remove(list2.size()-1);
		} else {
			if (list2.isEmpty()) return list1.remove(list1.size()-1);
			if (list1.get(list1.size()-1).time<list2.get(list2.size()-1).time) return list1.remove(list1.size()-1); else return list2.remove(list2.size()-1);
		}
	}

	/**
	 * Fügt das Event in eine bestimmte Liste ein.
	 * @param list	Liste in die das Event eingefügt werden soll.
	 * @param event	Einzufügendes Ereignis
	 * @see #addEvent(Event)
	 */
	private void addToList(List<Event> list, Event event) {
		if (list.isEmpty()) {list.add(event); return;}

		int nr1=0;
		int nr2=list.size()-1;
		int i;

		while (Math.abs(nr1-nr2)>1) {
			i=(nr1+nr2)/2;
			if (list.get(i).time<event.time) nr2=i; else nr1=i;
		}

		Event e2=list.get(nr2);
		if (e2.time>event.time)
		{
			if (nr2==list.size()-1) list.add(event); else list.add(nr2+1,event);
			return;
		}

		for (i=nr2-1;i>=nr1;i--) {
			e2=list.get(i);
			if (e2.time>event.time) {
				list.add(i+1,event); return;
			}
		}

		list.add(nr1,event);
		return;
	}

	@Override
	public void addEvent(Event event) {
		if (list1.size()<list2.size()) addToList(list1,event); else addToList(list2,event);
	}

	@Override
	public int eventQueueLength() {return list1.size()+list2.size();}

	/**
	 * Versucht ein Ereignis aus einer Teiliste zu löschen (ohne es auszuführen).
	 * @param list	Liste aus der das Ereignis gelöscht werden soll
	 * @param event	Ereignis, das aus der Ereignisliste gelöscht werden soll
	 * @return	Gibt <code>true</code> zurück, wenn das Ereignis gelöscht werden konnte
	 * @see #onlyDeleteEvent(Event)
	 */
	private boolean deleteFromList(List<Event> list, Event event) {
		if (list.isEmpty()) return false;

		int nr1=0;
		int nr2=list.size()-1;
		int i;

		while (Math.abs(nr1-nr2)>1) {
			i=(nr1+nr2)/2;
			if (list.get(i).time<event.time) nr2=i; else nr1=i;
		}

		while ((list.get(nr1).time>=event.time) && (nr1>0)) nr1--;

		for (i=nr1;i<=nr2;i++) if (list.get(i)==event) {list.remove(i); return true;}


		return false;
	}

	@Override
	protected boolean onlyDeleteEvent(Event event) {
		if (deleteFromList(list1,event)) return true; else return deleteFromList(list2,event);
	}

	@Override
	public void deleteAllEvents() {
		list1.clear();
		list2.clear();
	}

	@Override
	public List<Event> getAllEvents() {
		final List<Event> result=new ArrayList<>(list1.size()+list2.size());
		result.addAll(list1);
		result.addAll(list2);
		result.sort(null);
		return result;
	}
}
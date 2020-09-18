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
 * Teil-Ereigniswarteschlange<br>
 * Wird in {@link MultiArrayEventManager} und in {@link MultiArrayEventManagerWithHeapSort}
 * verwendet.
 * @author Alexander Herzog
 * @see MultiArrayEventManager
 * @see MultiArrayEventManagerWithHeapSort
 */
public final class EventQueue {
	/** Initiale Länge der Warteschlange */
	private static final int INITIAL_SIZE=50;

	/** Warteschlange */
	private Event[] list;
	/** Genutzte Plätze in der Warteschlange */
	private int used;

	/**
	 * Konstruktor der Klasse
	 */
	public EventQueue() {
		list=new Event[INITIAL_SIZE];
	}

	/**
	 * Erzeugt weiteren Platz in der Warteschlange.
	 * @see #list
	 * @see #add(Event)
	 */
	private void grow() {
		final Event[] newList=new Event[list.length*2];
		System.arraycopy(list,0,newList,0,used);
		list=newList;
	}

	/**
	 * Fügt ein Ereignis an einer bestimmten Stelle in die Warteschlange ein.
	 * @param event	Einzufügendes Ereignis
	 * @param index	Position an der das Ereignis eingefügt werden soll
	 */
	private void add(final Event event, final int index) {
		if (index<used) {
			System.arraycopy(list,index,list,index+1,used-index);
		}
		list[index]=event;
		used++;
	}

	/**
	 * Fügt ein Ereignis an der zeitlich passenden Stelle in die Warteschlange ein.
	 * @param event	Einzufügendes Ereignis
	 */
	public void add(final Event event) {
		if (used==list.length) grow();
		if (used==0) {
			list[0]=event;
			used=1;
		} else {
			final long time=event.time;
			int indexMin=0;
			int indexMax=used-1;
			while (indexMin<indexMax-1) {
				int index=(indexMin+indexMax)/2;
				final long t=list[index].time;
				if (t<=time) {indexMax=index; continue;}
				if (t>time) {indexMin=index; continue;}
			}
			for (int index=indexMin;index<=indexMax;index++) if (list[index].time<=time) {add(event,index); return;}
			add(event,indexMax+1);
		}
	}

	/**
	 * Löscht die Ereigniswarteschlange
	 */
	public void clear() {
		if (used==0) return;
		for (int i=0;i<used;i++) list[i]=null;
		used=0;
	}

	/**
	 * Liefert das zeitlich als nächstes anstehende Ereignis
	 * und entfernt es dabei aus der Warteschlange.
	 * @return	Nächstes Ereignis oder <code>null</code>, wenn die Warteschlange leer ist
	 */
	public Event next() {
		if (used==0) return null;
		used--;
		final Event e=list[used];
		list[used]=null;
		return e;
	}

	/**
	 * Entfernt ein Ereignis aus der Warteschlange.
	 * @param event	Zu entfernendes Ereignis
	 * @return	Gibt an, ob das Ereignis entfernt werden konnte (bzw. ob es zuvor also in der Warteschlange enthalten war)
	 */
	public boolean remove(final Event event) {
		if (used==0) return false;

		final long time=event.time;
		int indexMin=0;
		int indexMax=used-1;
		while (indexMin<indexMax-1) {
			int index=(indexMin+indexMax)/2;
			final long t=list[index].time;
			if (t<time) {indexMax=index; continue;}
			if (t>time) {indexMin=index; continue;}
			break;
		}
		for (int index=indexMin;index<=indexMax;index++) if (event==list[index]) return remove(index);

		return false;
	}

	/**
	 * Entfernt ein Ereignis aus der Warteschlange.
	 * @param index	Index des zu entfernenden Ereignisses
	 * @return	Gibt an, ob das Ereignis entfernt werden konnte (bzw. ob der Index einen zuvor gültigen Eintrag in der Warteschlange repräsentierte)
	 */
	public boolean remove(final int index) {
		if (index<0 || index>=used) return false;
		if (index==used-1) list[index]=null;
		if (index<used-1) System.arraycopy(list,index+1,list,index,used-index-1);
		used--;
		return true;
	}

	/**
	 * Liefert eine Liste mit allen Ereignissen in der Warteschlange.<br>
	 * Die Warteschlange wird dadurch nicht verändert.
	 * @return	Liste mit allen Ereignissen in der Warteschlange
	 */
	public List<Event> getAll() {
		final List<Event> result=new ArrayList<>();
		for (Event event: list) if (event!=null) result.add(event);
		return result;
	}
}

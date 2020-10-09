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
import java.util.PriorityQueue;

import simcore.Event;

/**
 * Implementierung des <code>EventManager</code>-Interface mit Hilfe mehrerer
 * <code>PriorityQueue</code>-Objekte.
 * @author Alexander Herzog
 * @version 1.0
 * @see EventManager
 * @see PriorityQueue
 * @see PriorityQueueEventManager
 */
public final class MultiPriorityQueueEventManager extends EventManagerBase implements EventManager {
	/** Anzahl an Sekunden pro Tag */
	private static final int dayLength=86400;
	/** Anzahl an Teilwarteschlangen f�r die normalen Ereignisse */
	private static final int queueCount=dayLength/4; /* H�here Werte bringen keine Verbesserungen! */
	/** Anzahl an Teilwarteschlangen f�r die initialen Ereignisse */
	private static final int queueCountForInitialEvents=dayLength/4;

	/** Wenn ein Tag {@link #dayLength} Sekunden hat und wir {@link #queueCount} Warteschlangen haben, dann gibt dieser Wert an, f�r wie viele Millisekunden eine Teilwarteschlange zust�ndig ist. */
	private static final int milliSecondsPerQueue=1000*dayLength/queueCount;
	/** Wenn ein Tag {@link #dayLength} Sekunden hat und wir {@link #queueCountForInitialEvents} Warteschlangen f�r die initialen Ereignisse haben, dann gibt dieser Wert an, f�r wie viele Millisekunden eine Teilwarteschlange (f�r die initialen Ereignisse) zust�ndig ist. */
	private static final int milliSecondsPerInitialEventsQueue=1000*dayLength/queueCountForInitialEvents;

	/** Liste aus der als letztes durch {@link #getNextEvent()} ein Ereignis entnommen wurde */
	private int lastList;
	/** Anzahl an momentan verwalteten Ereignissen �ber alle Warteschlangen zusammen (nicht initiale Ereignisse) */
	private int allQueueLength;
	/** Teilwarteschlangen */
	private final PriorityQueue<Event>[] queue;
	/** Teilwarteschlangen f�r die initialen Ereignisse */
	private final ArrayList<Event>[] initialEventsQueue;

	/**
	 * Konstruktor der Klasse
	 */
	@SuppressWarnings("unchecked")
	public MultiPriorityQueueEventManager() {
		queue=new PriorityQueue[queueCount];
		for (int i=0;i<queueCount;i++) queue[i]=new PriorityQueue<>();
		initialEventsQueue=new ArrayList[queueCountForInitialEvents];
		for (int i=0;i<queueCountForInitialEvents;i++) initialEventsQueue[i]=new ArrayList<>();
	}

	@Override
	protected Event getNextEvent() {
		while (lastList<queueCount) {
			final Event event=queue[lastList].poll();
			if (event!=null) {allQueueLength--; return event;}
			lastList++;
		}
		return null;
	}

	@Override
	protected boolean onlyDeleteEvent(final Event event) {
		final int index=(int)Math.min(queueCount-1,event.time/milliSecondsPerQueue);
		final boolean b=queue[index].remove(event);
		if (b) allQueueLength--;
		return b;
	}

	@Override
	public void addEvent(final Event event) {
		allQueueLength++;
		final int index=(int)Math.min(queueCount-1,event.time/milliSecondsPerQueue);
		queue[index].offer(event);
	}

	@Override
	public int eventQueueLength() {return allQueueLength;}

	@Override
	public void resetTime() {lastList=0;}

	@Override
	public void addInitialEvents(final List<? extends Event> events) {
		if (events.isEmpty()) return;

		final int eventsSize=events.size();
		for (int i=0;i<eventsSize;i++) {
			final Event event=events.get(i);
			final int index=(int)Math.min(queueCountForInitialEvents-1,event.time/milliSecondsPerInitialEventsQueue);
			initialEventsQueue[index].add(event);
		}

		Event lastevent=null;

		for (int i=0;i<queueCountForInitialEvents;i++) {
			final ArrayList<Event> queue=initialEventsQueue[i];
			final int size=queue.size();
			if (size==0) continue;
			if (size>1) queue.sort(null);
			for (Event event: queue) {
				if (lastevent==null) addEvent(event); else {
					lastevent.addNextEvent=event;
					assert(event.time>=lastevent.time);
				}
				lastevent=event;
			}
			queue.clear();
		}
	}

	@Override
	public void deleteAllEvents() {
		allQueueLength=0;
		for (int i=0;i<queue.length;i++) queue[i].clear();

		for (int i=0;i<queueCountForInitialEvents;i++) initialEventsQueue[i]=new ArrayList<>(); /* Speicher sparen */
	}

	@Override
	public List<Event> getAllEvents() {
		final List<Event> result=new ArrayList<>();
		for (PriorityQueue<Event> eventQueue: queue) result.addAll(eventQueue);
		for (List<Event> queue: initialEventsQueue) result.addAll(queue);

		result.sort(null);
		return result;
	}
}

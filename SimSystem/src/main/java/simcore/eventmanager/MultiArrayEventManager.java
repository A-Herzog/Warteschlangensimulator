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
 * Implementierung des <code>EventManager</code>-Interface mit Hilfe mehrerer Arrays.
 * @author Alexander Herzog
 * @version 1.0
 * @see EventManager
 */
public class MultiArrayEventManager extends EventManagerBase implements EventManager {
	private static final int dayLength=86400;
	private static final int queueCount=dayLength/8; /* Höhere Werte bringen keine Verbesserungen! */
	private static final int queueCountForInitialEvents=dayLength/8;

	private static final int milliSecondsPerQueue=1000*dayLength/queueCount;
	private static final int milliSecondsPerInitialEventsQueue=1000*dayLength/queueCountForInitialEvents;

	private int lastList;
	private int allQueueLength;
	private final EventQueue[] queue;
	private final ArrayList<Event>[] initialEventsQueue;

	private final EventQueue[] queueCache;
	private int queueCacheUsed=0;

	/**
	 * Konstruktor der Klasse
	 */
	@SuppressWarnings("unchecked")
	public MultiArrayEventManager() {
		queue=new EventQueue[queueCount];
		queueCache=new EventQueue[queueCount];
		initialEventsQueue=new ArrayList[queueCountForInitialEvents];
	}

	@Override
	protected final Event getNextEvent() {
		while (lastList<queueCount) {
			EventQueue currentQueue=queue[lastList];
			if (currentQueue!=null) {
				final Event event=currentQueue.next();
				if (event!=null) {allQueueLength--; return event;}
				/* Schlange ist leer, Objekt cachen */
				queueCache[queueCacheUsed]=currentQueue;
				queueCacheUsed++;
				queue[lastList]=null;
			}
			lastList++;
		}
		return null;
	}

	@Override
	protected final boolean onlyDeleteEvent(final Event event) {
		final int index=(int)Math.min(queueCount-1,event.time/milliSecondsPerQueue);
		EventQueue currentQueue=queue[index];
		if (currentQueue==null) return false;
		final boolean b=currentQueue.remove(event);
		if (b) allQueueLength--;
		return b;
	}

	@Override
	public final void addEvent(final Event event) {
		allQueueLength++;
		final int index=(int)Math.min(queueCount-1,event.time/milliSecondsPerQueue);
		if (queue[index]==null) {
			if (queueCacheUsed==0) {
				queue[index]=new EventQueue();
			} else {
				queueCacheUsed--;
				queue[index]=queueCache[queueCacheUsed];
				queue[index].clear();
			}
		}
		queue[index].add(event);
	}

	@Override
	public final int eventQueueLength() {return allQueueLength;}

	@Override
	public final void resetTime() {lastList=0;}

	@Override
	public final void addInitialEvents(final List<? extends Event> events) {
		if (events.isEmpty()) return;

		for (Event event: events) {
			final int index=(int)Math.min(queueCountForInitialEvents-1,event.time/milliSecondsPerInitialEventsQueue);
			if (initialEventsQueue[index]==null) initialEventsQueue[index]=new ArrayList<>();
			initialEventsQueue[index].add(event);
		}

		Event lastevent=null;

		for (ArrayList<Event> queue : initialEventsQueue) {
			if (queue==null) continue;

			final int size=queue.size();
			if (size==0) continue;
			if (size==2) {
				final Event e1=queue.get(0), e2=queue.get(1);
				if (e1.time>e2.time) {queue.set(0,e2); queue.set(1,e1);}
			}
			if (size==3) {
				final Event e1=queue.get(0), e2=queue.get(1), e3=queue.get(2);
				if (e1.time<e2.time) {
					/* 12 */ if (e2.time<e3.time) {
						/* 123 */
					} else {
						/* 32 */ if (e1.time<e3.time) {/* 132 */ queue.set(1,e3); queue.set(2,e2);} else {/* 312 */ queue.set(0,e3); queue.set(1,e1); queue.set(2,e2);}
					}
				} else {
					/* 21 */ if (e2.time<e3.time) {
						/* 23 */ if (e1.time<e3.time) {/* 213 */ queue.set(0,e2); queue.set(1,e1);} else {/* 231 */ queue.set(0,e2); queue.set(1,e3); queue.set(2,e1);}
					} else {
						/* 321 */ queue.set(0,e3); queue.set(1,e2); queue.set(2,e1);
					}
				}
			}

			if (size>3) {
				boolean needSort=true;
				if (size>100) {
					needSort=false;
					long time=0;
					for (Event event: queue) {long t=event.time; if (t<time) {needSort=true; break;} time=t;}
				}
				if (needSort) queue.sort(null);
			}
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
	public final void deleteAllEvents() {
		allQueueLength=0;
		for (int i=0;i<queue.length;i++) queue[i]=null;
		for (int i=0;i<queueCountForInitialEvents;i++) initialEventsQueue[i]=null; /* Speicher sparen */
		queueCacheUsed=0;
		for (int i=0;i<queueCache.length;i++) queueCache[i]=null;
	}

	@Override
	public List<Event> getAllEvents() {
		final List<Event> result=new ArrayList<>();
		for (EventQueue eventQueue: queue) if (eventQueue!=null) result.addAll(eventQueue.getAll());
		for (List<Event> queue: initialEventsQueue) if (queue!=null) result.addAll(queue);

		result.sort(null);
		return result;
	}
}
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
import java.util.Arrays;
import java.util.List;

import simcore.Event;

/**
 * Implementierung des <code>EventManager</code>-Interface mit Hilfe mehrerer Arrays.
 * @author Alexander Herzog
 * @version 1.0
 * @see EventManager
 */
public final class MultiArrayEventManagerWithHeapSort extends EventManagerBase implements EventManager {
	/** Anzahl an Sekunden pro Tag */
	private static final int dayLength=86400;
	/** Anzahl an Teilwarteschlangen für die normalen Ereignisse */
	private static final int queueCount=dayLength/8; /* Höhere Werte bringen keine Verbesserungen! */
	/** Anzahl an Teilwarteschlangen für die initialen Ereignisse */
	private static final int queueCountForInitialEvents=dayLength/8;

	/** Wenn ein Tag {@link #dayLength} Sekunden hat und wir {@link #queueCount} Warteschlangen haben, dann gibt dieser Wert an, für wie viele Millisekunden eine Teilwarteschlange zuständig ist. */
	private static final int milliSecondsPerQueue=1000*dayLength/queueCount;
	/** Wenn ein Tag {@link #dayLength} Sekunden hat und wir {@link #queueCountForInitialEvents} Warteschlangen für die initialen Ereignisse haben, dann gibt dieser Wert an, für wie viele Millisekunden eine Teilwarteschlange (für die initialen Ereignisse) zuständig ist. */
	private static final int milliSecondsPerInitialEventsQueue=1000*dayLength/queueCountForInitialEvents;

	/** Liste aus der als letztes durch {@link #getNextEvent()} ein Ereignis entnommen wurde */
	private int lastList;
	/** Anzahl an momentan verwalteten Ereignissen über alle Warteschlangen zusammen (nicht initiale Ereignisse) */
	private int allQueueLength;
	/** Teilwarteschlangen */
	private final EventQueue[] queue;
	/** Teilwarteschlangen für die initialen Ereignisse */
	private final Event[][] initialEventsQueue;
	/** Anzahl an Ereignissen in den Initial-Ereignis Teilwarteschlangen */
	private final int[] initialEventsQueueLength;

	/** Cache für die Teilwarteschlangen-Objekte */
	private final EventQueue[] queueCache;
	/** Anzahl an Einträgen in dem Teilwarteschlangen-Objekt-Cache ({@link #queueCache}) */
	private int queueCacheUsed=0;

	/** Heap-Sort-Implementierung aus {@link HeapSort} verwenden (<code>true</code>) oder {@link Arrays#sort(Object[])} verwenden (<code>false</code>). */
	private final boolean useHeapSort;

	/**
	 * Konstruktor der Klasse
	 * @param useHeapSort	Heap-Sort-Implementierung aus {@link HeapSort} verwenden (<code>true</code>) oder {@link Arrays#sort(Object[])} verwenden (<code>false</code>).
	 */
	public MultiArrayEventManagerWithHeapSort(boolean useHeapSort) {
		this.useHeapSort=useHeapSort;
		queue=new EventQueue[queueCount];
		queueCache=new EventQueue[queueCount];
		initialEventsQueue=new Event[queueCountForInitialEvents][];
		initialEventsQueueLength=new int[queueCountForInitialEvents];
	}

	/**
	 * Konstruktor der Klasse<br>
	 * Verwendet die Heap-Sort-Implementierung aus {@link HeapSort}.
	 */
	public MultiArrayEventManagerWithHeapSort() {
		this(true);
	}

	@Override
	protected Event getNextEvent() {
		while (lastList<queueCount) {
			final EventQueue currentQueue=queue[lastList];
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
	protected boolean onlyDeleteEvent(final Event event) {
		final int index=(int)Math.min(queueCount-1,event.time/milliSecondsPerQueue);
		EventQueue currentQueue=queue[index];
		if (currentQueue==null) return false;
		final boolean b=currentQueue.remove(event);
		if (b) allQueueLength--;
		return b;
	}

	@Override
	public void addEvent(final Event event) {
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
	public int eventQueueLength() {return allQueueLength;}

	@Override
	public void resetTime() {lastList=0;}

	@Override
	public void addInitialEvents(final List<? extends Event> events) {
		if (events.isEmpty()) return;

		for (Event event: events) {
			final int index=(int)Math.min(queueCountForInitialEvents-1,event.time/milliSecondsPerInitialEventsQueue);
			if (initialEventsQueue[index]==null) {
				initialEventsQueue[index]=new Event[100];
				initialEventsQueueLength[index]=0;
			}
			if (initialEventsQueue[index].length==initialEventsQueueLength[index]) {
				initialEventsQueue[index]=Arrays.copyOf(initialEventsQueue[index],initialEventsQueue[index].length*2);
			}
			initialEventsQueue[index][initialEventsQueueLength[index]]=event;
			initialEventsQueueLength[index]++;
		}

		Event lastevent=null;

		for (int i=0;i<initialEventsQueue.length;i++) {
			Event[] queue=initialEventsQueue[i];
			int size=initialEventsQueueLength[i];

			if (queue==null) continue;

			if (size==0) continue;
			if (size==2) {
				final Event e1=queue[0], e2=queue[1];
				if (e1.time>e2.time) {queue[0]=e2; queue[1]=e1;}
			}
			if (size==3) {
				final Event e1=queue[0], e2=queue[1], e3=queue[2];
				if (e1.time<e2.time) {
					/* 12 */ if (e2.time<e3.time) {
						/* 123 */
					} else {
						/* 32 */ if (e1.time<e3.time) {/* 132 */ queue[1]=e3; queue[2]=e2;} else {/* 312 */ queue[0]=e3; queue[1]=e1; queue[2]=e2;}
					}
				} else {
					/* 21 */ if (e2.time<e3.time) {
						/* 23 */ if (e1.time<e3.time) {/* 213 */ queue[0]=e2; queue[1]=e1;} else {/* 231 */ queue[0]=e2; queue[1]=e3; queue[2]=e1;}
					} else {
						/* 321 */ queue[0]=e3; queue[1]=e2; queue[2]=e1;
					}
				}
			}

			if (size>3) {
				boolean needSort=true;
				if (size>100) {
					needSort=false;
					long time=0;
					for (int j=0;j<size;j++) {
						Event event=queue[j];
						long t=event.time;
						if (t<time) {needSort=true; break;}
						time=t;
					}
				}
				if (needSort) {
					if (useHeapSort) HeapSort.sort(queue,size); else Arrays.sort(queue,0,size,null);
				}
			}
			for (int j=0;j<size;j++) {
				Event event=queue[j];
				if (lastevent==null) addEvent(event); else {
					lastevent.addNextEvent=event;
					assert(event.time>=lastevent.time);
				}
				lastevent=event;
			}
			initialEventsQueueLength[i]=0;
		}
	}

	@Override
	public void deleteAllEvents() {
		allQueueLength=0;
		for (int i=0;i<queue.length;i++) if (queue[i]!=null) {queue[i].clear();  queue[i]=null;}
		for (int i=0;i<queueCountForInitialEvents;i++) initialEventsQueue[i]=null; /* Speicher sparen */
		queueCacheUsed=0;
		for (int i=0;i<queueCache.length;i++) if (queueCache[i]!=null) {queueCache[i].clear(); queueCache[i]=null;}
	}

	@Override
	public List<Event> getAllEvents() {
		final List<Event> result=new ArrayList<>();
		for (EventQueue eventQueue: queue) if (eventQueue!=null) result.addAll(eventQueue.getAll());
		for (Event[] queue: initialEventsQueue) if (queue!=null) result.addAll(Arrays.asList(queue));

		result.sort(null);
		return result;
	}
}
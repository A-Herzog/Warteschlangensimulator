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
 * <code>PriorityQueue</code>-Objekte
 * @author Alexander Herzog
 * @version 1.1
 * @see EventManager
 * @see PriorityQueue
 */
public class LongRunMultiPriorityQueueEventManager extends EventManagerBase {
	/** Teilwarteschlangen */
	private final PriorityQueue<Event>[] queues;
	/** Anzahl an Teilwarteschlangen */
	private final int queuesLength;
	/** Zwischenspeicher für neue Ereignisse, die zum selben Zeitpunkt wie das aktuelle Ereignis ausgeführt werden sollen (spart so das Einfügen dieser Ereignisse in die eigentliche Ereignisliste) */
	private final Event[] fastBuffer;
	/** Startindex für den Jetzt-Ereignisse-Zwischenspeicher */
	private int fastBufferStart=-1;
	/** Nächste Einfügeposition für den Jetzt-Ereignisse-Zwischenspeicher */
	private int fastBufferNextAdd=0;
	/** Aktuelle Systemzeit, d.h. der Zeitpunkt des Ereignisses, das zuletzt per {@link #getNextEvent()} abgefragt wurde */
	private long lastTime=0;

	/**
	 * Konstruktor der Klasse
	 * @param splitLevel	Anzahl der Teil-Ereigniswarteschlangen
	 */
	@SuppressWarnings("unchecked")
	public LongRunMultiPriorityQueueEventManager(final int splitLevel) {
		queues=new PriorityQueue[splitLevel];
		queuesLength=queues.length;
		for (int i=0;i<queuesLength;i++) queues[i]=new PriorityQueue<>(100);
		fastBuffer=new Event[100];
	}

	@Override
	protected final Event getNextEvent() {
		while (fastBufferStart!=-1) {
			final Event event=fastBuffer[fastBufferStart];
			fastBufferStart++;
			if (fastBufferStart==fastBuffer.length) fastBufferStart=0;
			if (fastBufferStart==fastBufferNextAdd) fastBufferStart=-1;
			if (event!=null) return event;
		}

		int index=-1;
		long minTime=Long.MAX_VALUE;
		for (int i=0;i<queuesLength;i++) if (!queues[i].isEmpty()) {
			final Event event=queues[i].peek();
			if (event.time<minTime) {minTime=event.time; index=i;}
		}
		if (index<0) {
			lastTime=0;
			return null;
		} else {
			final Event event=queues[index].poll();
			lastTime=event.time;
			return event;
		}
	}

	@Override
	public final void addEvent(Event event) {
		if (event.time==lastTime && fastBufferNextAdd!=fastBufferStart) {
			fastBuffer[fastBufferNextAdd]=event;
			if (fastBufferStart==-1) fastBufferStart=fastBufferNextAdd;
			fastBufferNextAdd++;
			if (fastBufferNextAdd==fastBuffer.length) fastBufferNextAdd=0;
			return;
		}

		queues[(int)(event.time%queuesLength)].add(event);
	}

	@Override
	public final int eventQueueLength() {
		int sum=0;
		for (PriorityQueue<Event> queue: queues) sum+=queue.size();
		return sum;
	}

	@Override
	protected final boolean onlyDeleteEvent(Event event) {
		if (event.time==lastTime && fastBufferStart!=-1) {
			int index=fastBufferStart;
			while (index!=fastBufferNextAdd) {
				if (fastBuffer[index]==event) {fastBuffer[index]=null; return true;}
				index++;
				if (index==fastBuffer.length) index=0;
			}
		}

		return queues[(int)(event.time%queuesLength)].remove(event);
	}

	@Override
	public void deleteAllEvents() {
		for (PriorityQueue<Event> queue: queues) queue.clear();
		fastBufferStart=-1;
		fastBufferNextAdd=0;
	}

	@Override
	public List<Event> getAllEvents() {
		final List<Event> result=new ArrayList<>();
		for (PriorityQueue<Event> queue: queues) result.addAll(queue);

		if (fastBufferStart!=-1 && fastBufferStart!=fastBufferNextAdd) {
			int start=fastBufferStart;
			while (start!=-1) {
				final Event event=fastBuffer[start];
				if (event!=null) result.add(event);
				start++;
				if (start==fastBuffer.length) start=0;
				if (start==fastBufferNextAdd) break;
			}
		}

		result.sort(null);
		return result;
	}
}

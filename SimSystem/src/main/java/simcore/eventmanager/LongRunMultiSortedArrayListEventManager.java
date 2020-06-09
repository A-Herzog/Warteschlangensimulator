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
 * Implementierung des <code>EventManager</code>-Interface mit Hilfe mehrerer
 * <code>SortedArrayList</code>-Objekte
 * @author Alexander Herzog
 * @version 1.2
 * @see EventManager
 * @see SortedEventArrayList
 */
public class LongRunMultiSortedArrayListEventManager extends EventManagerBase {
	private final SortedEventArrayList[] queues;
	private final int queuesLength;
	private final Event[] fastBuffer;
	private int fastBufferStart=-1;
	private int fastBufferNextAdd=0;
	private long lastTime=0;

	/**
	 * Konstruktor der Klasse
	 * @param splitLevel	Anzahl der Teil-Ereigniswarteschlangen
	 */
	public LongRunMultiSortedArrayListEventManager(final int splitLevel) {
		queues=new SortedEventArrayList[splitLevel];
		queuesLength=queues.length;
		for (int i=0;i<queuesLength;i++) queues[i]=new SortedEventArrayList();
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
		for (int i=0;i<queuesLength;i++) if (queues[i].fastSize>0) {
			Event event=queues[i].lastPeek;
			if (event==null) event=queues[i].peek();
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
	public final void addEvent(final Event event) {
		final long time=event.time;
		if (time==lastTime && fastBufferNextAdd!=fastBufferStart) {
			fastBuffer[fastBufferNextAdd]=event;
			if (fastBufferStart==-1) fastBufferStart=fastBufferNextAdd;
			fastBufferNextAdd++;
			if (fastBufferNextAdd==fastBuffer.length) fastBufferNextAdd=0;
			return;
		}

		if (time>=0) {
			queues[(int)(time%queuesLength)].add(event);
		} else {
			queues[(int)((-time)%queuesLength)].add(event);
		}
	}

	@Override
	public final int eventQueueLength() {
		int sum=0;
		for (SortedEventArrayList queue: queues) sum+=queue.fastSize;
		return sum;
	}

	@Override
	protected final boolean onlyDeleteEvent(final Event event) {
		final long time=event.time;
		if (time==lastTime && fastBufferStart!=-1) {
			int index=fastBufferStart;
			while (index!=fastBufferNextAdd) {
				if (fastBuffer[index]==event) {fastBuffer[index]=null; return true;}
				index++;
				if (index==fastBuffer.length) index=0;
			}
		}

		if (time>=0) {
			return queues[(int)(time%queuesLength)].remove(event);
		} else {
			return queues[(int)((-time)%queuesLength)].remove(event);
		}
	}

	@Override
	public void deleteAllEvents() {
		for (SortedEventArrayList queue: queues) queue.clear();
		fastBufferStart=-1;
		fastBufferNextAdd=0;
	}

	@Override
	public List<Event> getAllEvents() {
		final List<Event> result=new ArrayList<>();
		for (SortedEventArrayList queue: queues) result.addAll(queue);

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

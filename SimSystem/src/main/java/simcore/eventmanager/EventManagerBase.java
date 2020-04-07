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

import java.util.List;
import java.util.PriorityQueue;

import simcore.Event;
import simcore.SimData;
import simcore.eventcache.EventCache;

/**
 * Abstrakte Implementierung des <code>EventManager</code>-Interfaces.<br><br>
 * In dieser Implementierung werden all die Methoden zur Verfügung gestellt,
 * die keinen direkten Zugriff auf die Ereignisliste selbst benötigen. In
 * abgeleiteten Klassen muss dann lediglich noch die Ereignisliste selbst
 * implementiert werden.
 * @author Alexander Herzog
 * @version 1.1
 * @see EventManager
 */
public abstract class EventManagerBase implements EventManager {
	private long eventExecutionCount;
	private boolean abortSimulation;
	private boolean pauseSimulation;

	/**
	 * Konstruktor der Klasse <code>EventManagerBase</code>
	 */
	public EventManagerBase() {
	}

	/**
	 * Führt zu einem direkten Abbruch der Ereignisschleife
	 */
	@Override
	public void directAbortThread() {
		abortSimulation=true;
	}

	/**
	 * Holt das jeweils nächste Ereignis aus der Ereignisliste.<br><br>
	 * (Wird von <code>executeNextEvent</code> verwendet.)<br>
	 * @return Als nächstes auszuführendes Ereignis oder <code>null</code>, wenn die Liste leer ist.
	 */
	protected abstract Event getNextEvent();

	@Override
	public final boolean executeNextEvents(final SimData simData, final int maxExecuteEvents, final int maxRunTime) {
		pauseSimulation=false;
		final long startTime=System.currentTimeMillis();
		final long endTime=startTime+maxRunTime;
		final EventCache eventCache=simData.eventCache;
		int count=0;
		while (count<maxExecuteEvents) {
			if (pauseSimulation) return true;
			if (abortSimulation) break;
			if (count%1024==0) {
				if (System.currentTimeMillis()>endTime) break;
			}
			Event event=getNextEvent();
			if (event==null) return false;

			simData.currentTime=event.time;
			if (!event.isDeleted) {
				event.run(simData);
				eventExecutionCount++;
				count++;
			}

			if (event.addNextEvent!=null) {
				Event nextEvent=event.addNextEvent;
				event.addNextEvent=null;
				final long time=event.time;
				/* Falls das nächste Event zum selben Zeitpunkt ausgeführt werden soll (z.B. mehrere Agenten, die gleichzeitig zu arbeiten beginnen */
				/* Ereignis nicht in Liste einfügen, sondern sofort ausführen. */
				while (nextEvent.time==time && count<maxExecuteEvents) {
					if (count%1024==0) {
						if (System.currentTimeMillis()>endTime) break;
					}
					eventCache.put(event);
					event=nextEvent;
					if (!event.isDeleted) {
						event.run(simData);
						eventExecutionCount++;
						count++;
					}
					nextEvent=event.addNextEvent;
					if (nextEvent==null) break;
					event.addNextEvent=null;
				}
				if (nextEvent!=null) addEvent(nextEvent);
			}
			eventCache.put(event);
		}

		return true;
	}

	/**
	 * Löscht das übergebene Ereignis aus der Ereignisliste, ohne es in den Cache aufzunehmen.<br><br>
	 * Diese Methode wird von <code>deleteEvent</code> aufgerufen. Beim Aufruf von <code>deleteEvent</code>
	 * wird das übergebene Ereignis zunächst per <code>onlyDeleteEvent</code> aus der Ereignisliste gelöscht.
	 * Wenn dieses erfolgreich verlaufen ist (Rückgabewert von <code>onlyDeleteEvent</code> ist <code>true</code>)
	 * wird das Ereignis von <code>deleteEvent</code> in den Cache aufgenommen.
	 * @param event	Ereignis, das aus der Ereignisliste gelöscht werden soll
	 * @return	Gibt <code>true</code> zurück, wenn das Ereignis gelöscht werden konnte
	 */
	protected abstract boolean onlyDeleteEvent(final Event event);

	@Override
	public final void deleteEvent(final Event event, final SimData simData) {
		if (event==null) return;
		if (onlyDeleteEvent(event)) {
			simData.recycleEvent(event);
		} else {
			event.isDeleted=true;
		}
	}

	@Override
	public abstract void addEvent(final Event event);

	@Override
	public void addInitialEvents(final List<? extends Event> events) {
		if (events.isEmpty()) return;
		final PriorityQueue<Event> queue=new PriorityQueue<>(events);
		Event lastevent=queue.poll(); addEvent(lastevent);
		Event event=queue.poll();
		while (event!=null) {
			lastevent.addNextEvent=event;
			lastevent=event;
			event=queue.poll();
		}
	}

	@Override
	public final long eventCount() {
		return eventExecutionCount;
	}

	@Override
	public abstract int eventQueueLength();

	@Override
	public void resetTime() {}

	@Override
	public final void resetCount() {
		eventExecutionCount=0;
	}

	@Override
	public void setPause() {
		pauseSimulation=true;
	}
}

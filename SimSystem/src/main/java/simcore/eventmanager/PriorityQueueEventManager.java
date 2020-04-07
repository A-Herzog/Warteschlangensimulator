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
 * Implementierung des <code>EventManager</code>-Interface mit Hilfe der
 * <code>PriorityQueue</code>-Klasse
 * @author Alexander Herzog
 * @version 1.0
 * @see EventManager
 * @see PriorityQueue
 */
public final class PriorityQueueEventManager extends EventManagerBase implements EventManager {
	private final PriorityQueue<Event> queue=new PriorityQueue<>();

	@Override
	protected Event getNextEvent() {return queue.poll();}

	@Override
	public void addEvent(Event event) {queue.add(event);}

	@Override
	public int eventQueueLength() {return queue.size();}

	@Override
	protected boolean onlyDeleteEvent(Event event) {
		return queue.remove(event);
	}

	@Override
	public void deleteAllEvents() {queue.clear();}

	@Override
	public List<Event> getAllEvents() {
		final List<Event> result=new ArrayList<>();
		result.addAll(queue);

		result.sort(null);
		return result;
	}
}

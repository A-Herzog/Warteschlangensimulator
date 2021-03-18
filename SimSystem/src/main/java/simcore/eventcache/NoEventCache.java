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
package simcore.eventcache;

import simcore.Event;

/**
 * Die <code>NoEventCache</code>-Klasse ist eine Dummy-Implementierung des <code>EventCache</code>-Interfaces.
 * Es speichert die per <code>put</code> übergebenen Ereignisse nicht und liefert per <code>get</code> stets
 * ein neu angelegtes Ereignis.
 * @author Alexander Herzog
 * @version 1.0
 */
public final class NoEventCache implements EventCache {

	@Override
	public Event get(final Class<? extends Event> eventClass) {try {return eventClass.getConstructor().newInstance();} catch (Exception e) {return null;}}

	@Override
	public void put(final Event event) {}

	@Override
	public void clear() {}

	@Override
	public Event getOrNull(Class<? extends Event> eventClass) {return null;}
}

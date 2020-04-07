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
package simcoretests.simcoreimpl;

import java.util.function.BiFunction;
import java.util.function.Consumer;

import simcore.Event;
import simcore.SimData;
import simcoretests.SimulatorBaseTest;

/**
 * Test-Ereignis, welches von {@link SimulatorBaseTest} zur Prüfung der Funktionsweise
 * der Ereignisverwaltung verwendet wird.
 * @author Alexander Herzog
 * @see SimulatorBaseTest
 */
public class EventTestImpl extends Event {
	/**
	 * ID des Ereignisses<br>
	 * Wird vom Ereignis selbst nicht verwendet; dient dazu, die Ereignisse beim
	 * Testen auseinander halten zu können.
	 */
	public int id;
	private final Consumer<EventTestImpl> executionInfo;
	private final BiFunction<SimData,EventTestImpl,EventTestImpl> addNext;

	/**
	 * Konstruktor der Klasse
	 */
	public EventTestImpl() {
		this(0,0,null);
	}

	/**
	 * Konstruktor der Klasse
	 * @param time	Geplante Ausführungszeit
	 * @param id	ID des Ereignisses
	 * @param executionInfo	Wird, wenn hier eine Wert ungleich <code>null</code> übergeben wird, bei der Ausführng des Ereignisses aufgerufen
	 */
	public EventTestImpl(final long time, final int id, final Consumer<EventTestImpl> executionInfo) {
		this(time,id,executionInfo,null);
	}

	/**
	 * Konstruktor der Klasse
	 * @param time	Geplante Ausführungszeit
	 * @param id	ID des Ereignisses
	 * @param executionInfo	Wird, wenn hier eine Wert ungleich <code>null</code> übergeben wird, bei der Ausführng des Ereignisses aufgerufen
	 * @param addNext	Optionales Nachfolgeereignis, das bei der Ausführung dieses Ereignisses in die Ereignisliste eingefügt wird
	 */
	public EventTestImpl(final long time, final int id, final Consumer<EventTestImpl> executionInfo, final BiFunction<SimData,EventTestImpl,EventTestImpl> addNext) {
		super();
		init(time);
		this.id=id;
		this.executionInfo=executionInfo;
		this.addNext=addNext;
	}

	/**
	 * Liefert eine Kopie des Ereignisses
	 * @param time	Geplante Ausführungszeit des kopierten Ereignisses
	 * @param id	ID des kopierten Ereignisses
	 * @return	Kopie des Ereignisses
	 */
	public EventTestImpl getCopy(final long time, final int id) {
		return new EventTestImpl(time,id,executionInfo,addNext);
	}

	@Override
	public void run(SimData data) {
		if (data.loggingActive) data.logEventExecution(getClass().getName(),"event executed");
		if (executionInfo!=null) executionInfo.accept(this);
		if (addNext!=null) {
			final EventTestImpl next=addNext.apply(data,this);
			if (next!=null) data.eventManager.addEvent(next);
		}
	}
}

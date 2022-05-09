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
package simtest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import simcore.Event;
import simcore.SimData;

/**
 * Debug-Event zum Testen des Simulatorkerns
 * @author Alexander Herzog
 * @version 1.0
 */
public final class TestEvent extends Event {
	/**
	 * Verhaltensweise des Ereignisses: 1er-Bit=Consolen-Ausgabe, 2er-Bit=Nächste Ereignisse einfügen
	 * @see #init(long, int)
	 * @see #init(long, int, TestEvent)
	 */
	private int mode;

	/**
	 * Wird hier ein Ereignis angegeben, so wird <code>deleteTest</code> bei der Ausführung dieses
	 * Ereignisses unausgeführt gelöscht. Außerdem wird beim Anlegen eines weiteren Ereignisses
	 * jeweils wieder ein deleteEvent angelegt.
	 * @see #init(long, int, TestEvent)
	 */
	private TestEvent deleteTest;

	/**
	 * Konstruktor der Klasse
	 */
	public TestEvent() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	/**
	 * Wandelt den Ausführungszeitpunkt {@link Event#time} in eine Zeichenkette um
	 * @return	Ausführungszeitpunkt als Zeichenkette
	 */
	private String getExecutionTime() {
		if (time<1000000000) return Long.toString(time);
		Date d=new Date(time); return d.toString();
	}

	/**
	 * Initialisierung des Ereignisses (nach der Erstellung oder nach dem Holen aus dem Event-Cache)
	 * @param time	Geplante Ausführungszeit
	 * @param mode	Verhaltensweise des Ereignisses: 1er-Bit=Consolen-Ausgabe, 2er-Bit=Nächste Ereignisse einfügen
	 */
	public void init(long time, int mode) {
		super.init(time);
		this.mode=mode;
		deleteTest=null;
		if ((this.mode & 1)==1) System.out.println("SimCoreTestEvent init to be executed at "+getExecutionTime());
	}

	/**
	 * Initialisierung des Ereignisses (nach der Erstellung oder nach dem Holen aus dem Event-Cache)
	 * @param time	Geplante Ausführungszeit
	 * @param mode	Verhaltensweise des Ereignisses: 1er-Bit=Consolen-Ausgabe, 2er-Bit=Nächste Ereignisse einfügen
	 * @param deleteTest	Wird hier ein Ereignis angegeben, so wird <code>deleteTest</code> bei der Ausführung dieses Ereignisses unausgeführt gelöscht. Außerdem wird beim Anlegen eines weiteren Ereignisses jeweils wieder ein deleteEvent angelegt.
	 */
	public void init(long time, int mode, TestEvent deleteTest) {
		init(time,mode);
		this.deleteTest=deleteTest;
	}

	@Override
	public void run(SimData data) {
		if ((mode & 1)==1) System.out.println("SimCoreTestEvent executed at "+getExecutionTime());
		if ((mode & 2)==2) {
			if (deleteTest!=null) {
				data.eventManager.deleteEvent(deleteTest,data);
				TestEvent nextevent1=(TestEvent)data.getEvent(TestEvent.class);
				TestEvent nextevent2=(TestEvent)data.getEvent(TestEvent.class);
				long nexttime=time+Math.round(1000*/* Math.random() */ ThreadLocalRandom.current().nextDouble());
				nextevent1.init(nexttime,mode,nextevent2);
				nextevent2.init(200+nexttime,mode);
				data.eventManager.addEvent(nextevent1);
				data.eventManager.addEvent(nextevent2);
			} else {
				TestEvent nextevent1=(TestEvent)data.getEvent(TestEvent.class);
				long nexttime=time+Math.round(1000*/* Math.random() */ ThreadLocalRandom.current().nextDouble());
				nextevent1.init(nexttime,mode);
				data.eventManager.addEvent(nextevent1);
			}
		}
	}

	/**
	 * Erzeugt eine gewisse Menge von TestEvents der angegebenen Art
	 * @param numberOfEvents	Anzahl der zu erzeugenden Ereignisse
	 * @param simData	<code>simData</code>-Objekt (wird wegen des Event-Caches benötigt)
	 * @param mode	Typ der Ereignisse (1er-Bit=Consolen-Ausgabe, 2er-Bit=Nächste Ereignisse einfügen)
	 * @return	Liste mit Ereignissen der angegebenen Form und Anzahl
	 */
	public static List<Event>createAndAddInitialDummyEvents(int numberOfEvents, SimData simData, int mode) {
		List<Event> eventList=new ArrayList<>();
		for (int i=0;i<numberOfEvents;i++) {
			Event event=simData.getEvent(TestEvent.class);
			((TestEvent)event).init(Math.round(/* Math.random() */ ThreadLocalRandom.current().nextDouble()*1001000),mode);
			eventList.add(event);
		}
		simData.eventManager.addInitialEvents(eventList);
		return eventList;
	}
}

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
package simcoretests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import simcore.Event;
import simcore.SimData;
import simcore.eventcache.AssociativeEventCache;
import simcore.eventcache.EventCache;
import simcore.eventcache.HashMapEventCache;
import simcore.eventcache.ListEventCache;
import simcore.eventcache.ListEventCacheLocked;
import simcore.eventcache.NoEventCache;
import simcore.eventmanager.EventManager;
import simcore.eventmanager.LongRunMultiPriorityQueueEventManager;
import simcore.eventmanager.LongRunMultiSortedArrayListEventManager;
import simcore.eventmanager.MapEventManager;
import simcore.eventmanager.MultiArrayEventManager;
import simcore.eventmanager.MultiArrayEventManagerWithHeapSort;
import simcore.eventmanager.MultiPriorityQueueEventManager;
import simcore.eventmanager.PriorityQueueEventManager;
import simcore.logging.CallbackLogger;
import simcore.logging.HTMLLogger;
import simcore.logging.PlainTextLogger;
import simcore.logging.PlainTextLoggerLimited;
import simcore.logging.RTFLogger;
import simcoretests.simcoreimpl.EventTestImpl;
import simcoretests.simcoreimpl.SimulatorBaseTestImpl;

/**
 * Testet die Ereignisverwaltung usw.
 * @author Alexander Herzog
 */
public /* @SuppressWarnings("deprecation") für den TwoListsEventManager */
class SimulatorBaseTest {
	/**
	 * Logging-Daten für die Tests
	 * @see #logger
	 */
	private static final List<String> log=new ArrayList<>();

	/**
	 * Logger für die Tests
	 * @see #log
	 */
	private static final Consumer<EventTestImpl> logger=e->log.add(e.id+" "+e.time);

	/** Welche Caches und Manager sollen getestet werden? */
	private static final EventCache[] caches=new EventCache[] {
			new ListEventCache(),
			new ListEventCacheLocked(),
			new AssociativeEventCache(),
			new HashMapEventCache(),
			new NoEventCache(),
			null
	};

	/**
	 * Welche {@link EventManager} sollen getestet werden?
	 * @see #initEventManagersSuppliersList()
	 */
	private static final List<Supplier<EventManager>> managers=new ArrayList<>();

	/**
	 * Konstruktor der Klasse
	 */
	public SimulatorBaseTest() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	/**
	 * Initialisiert die Liste der zu prüfenden {@link EventManager}.
	 * @see #managers
	 */
	@BeforeAll
	static void initEventManagersSuppliersList() {
		managers.add(()->new LongRunMultiPriorityQueueEventManager(4));
		managers.add(()->new LongRunMultiSortedArrayListEventManager(4));
		managers.add(()->new MapEventManager());
		managers.add(()->new MultiArrayEventManager());
		managers.add(()->new MultiArrayEventManagerWithHeapSort());
		managers.add(()->new MultiPriorityQueueEventManager());
		managers.add(()->new PriorityQueueEventManager());
		/* managers.add(()->new TwoListsEventManager()); */
	}

	/**
	 * Beispielereignissets anlegen
	 * @param sorting	Sortierungsvariante (Wert von 0 bis 5)
	 * @return	Liefert ein Callback, welches die Ereignisse anlegt
	 */
	private BiFunction<SimData,Integer,List<Event>> buildSortInitialEvents(final int sorting) {
		return (data,nr)->{
			final List<Event> list=new ArrayList<>();
			for (int i=0;i<1000;i++) {
				switch (sorting) {
				case 0:
					list.add(new EventTestImpl(100*(3*i+2),3*i+2,(nr==0)?logger:null));
					list.add(new EventTestImpl(100*(3*i+1),3*i+1,(nr==0)?logger:null));
					list.add(new EventTestImpl(100*(3*i+3),3*i+3,(nr==0)?logger:null));
					break;
				case 1:
					list.add(new EventTestImpl(100*(3*i+3),3*i+3,(nr==0)?logger:null));
					list.add(new EventTestImpl(100*(3*i+2),3*i+2,(nr==0)?logger:null));
					list.add(new EventTestImpl(100*(3*i+1),3*i+1,(nr==0)?logger:null));
					break;
				case 2:
					list.add(new EventTestImpl(100*(3*i+1),3*i+1,(nr==0)?logger:null));
					list.add(new EventTestImpl(100*(3*i+2),3*i+2,(nr==0)?logger:null));
					list.add(new EventTestImpl(100*(3*i+3),3*i+3,(nr==0)?logger:null));
					break;
				case 3:
					list.add(new EventTestImpl(100*(3*i+3),3*i+3,(nr==0)?logger:null));
					list.add(new EventTestImpl(100*(3*i+1),3*i+1,(nr==0)?logger:null));
					list.add(new EventTestImpl(100*(3*i+2),3*i+2,(nr==0)?logger:null));
					break;
				case 4:
					list.add(new EventTestImpl(100*(3*i+1),3*i+1,(nr==0)?logger:null));
					list.add(new EventTestImpl(100*(3*i+3),3*i+3,(nr==0)?logger:null));
					list.add(new EventTestImpl(100*(3*i+2),3*i+2,(nr==0)?logger:null));
					break;
				case 5:
					list.add(new EventTestImpl(100*(3*i+2),3*i+2,(nr==0)?logger:null));
					list.add(new EventTestImpl(100*(3*i+3),3*i+3,(nr==0)?logger:null));
					list.add(new EventTestImpl(100*(3*i+1),3*i+1,(nr==0)?logger:null));
					break;
				}
			}
			return list;
		};
	}

	/**
	 * Erzeugt eine Liste von Ereignissen, die jeweils das nächste Ereignis in die Event-Liste einfügen
	 * @return	Callback, das die Ereignisse anlegt
	 * @see #testAddConnectedEvents
	 */
	private BiFunction<SimData,Integer,List<Event>> buildAddConnectedEvents() {
		return (data,nr)->{
			final List<Event> list=new ArrayList<>();
			list.add(new EventTestImpl(100,1,(nr==0)?logger:null,(d,e)->{
				if (e.id>=100) return null;
				return e.getCopy((e.id+1)*100,e.id+1);
			}));
			return list;
		};
	}

	/**
	 * Erzeugt eine Liste von Ereignissen, die denselben Ausführungszeitpunkt besitzen
	 * @return	Callback, das die Ereignisse anlegt
	 * @see #testAddSameTimeEvents()
	 */
	private BiFunction<SimData,Integer,List<Event>> buildAddSameTimeEvents() {
		return (data,nr)->{
			final List<Event> list=new ArrayList<>();
			for (int i=0;i<100;i++) {
				list.add(new EventTestImpl(100,1,(nr==0)?logger:null,(d,e)->{
					if (e.id>=100) return null;
					return e.getCopy(100,e.id+1);
				}));
			}
			return list;
		};
	}

	/**
	 * Hilfsmethode: Simulator starten
	 * @param simulator	Zu startendes Simulator
	 */
	private void runSimulator(final SimulatorBaseTestImpl simulator) {
		assertTrue(!simulator.isStarted());
		simulator.start(false);
		assertTrue(simulator.isStarted());
		simulator.finalizeRun();
		assertTrue(simulator.isStarted());
		assertTrue(!simulator.isRunning());
	}

	/**
	 * Hilfsmethode: Simulator im Pause-Modus starten
	 * @param simulator	Zu startendes Simulator
	 */
	private void runSimulatorPaused(final SimulatorBaseTestImpl simulator) {
		assertTrue(!simulator.isStarted());
		simulator.start(false,true);
		assertTrue(simulator.isStarted());
		assertTrue(simulator.isRunning());
	}

	/**
	 * Prüfen der Ereignisabarbeitung: Initiale Ereignisse
	 * @param simulator	Simulator, der die Ereignisse abgearbeitet hat
	 * @see #testSortInitialEvents()
	 */
	private void resultsSortInitialEvents(final SimulatorBaseTestImpl simulator) {
		assertEquals(3000,log.size());
		for (int i=0;i<log.size();i++) {
			assertEquals(""+(i+1)+" "+((i+1)*100),log.get(i));
		}

		assertEquals(3000*simulator.threadCount,simulator.getEventCount());
		assertEquals(0,simulator.getEventQueueLength());
	}

	/**
	 * Prüfen der Ereignisabarbeitung: Ereignisse, die jeweils das nächste Ereignis einfügen
	 * @param simulator	Simulator, der die Ereignisse abgearbeitet hat
	 * @see #testAddConnectedEvents()
	 */
	private void resultsAddConnectedEvents(final SimulatorBaseTestImpl simulator) {
		assertEquals(100,log.size());
		for (int i=0;i<log.size();i++) {
			assertEquals(""+(i+1)+" "+((i+1)*100),log.get(i));
		}

		assertEquals(100*simulator.threadCount,simulator.getEventCount());
		assertEquals(0,simulator.getEventQueueLength());
	}

	/**
	 * Prüfen der Ereignisabarbeitung: Ereignisse mit gleicher Ausführungszeit
	 * @param simulator	Simulator, der die Ereignisse abgearbeitet hat
	 * @see #testAddSameTimeEvents()
	 */
	private void resultsAddSameTimeEvents(final SimulatorBaseTestImpl simulator) {
		assertEquals(10000,log.size());
		for (int i=0;i<log.size();i++) {
			final String[] parts=log.get(i).split(" ");
			assertEquals(2,parts.length);
			assertEquals("100",parts[1]);
		}

		assertEquals(10000*simulator.threadCount,simulator.getEventCount());
		assertEquals(0,simulator.getEventQueueLength());
	}

	/* Tests der Ereignisverarbeitung */

	/**
	 * Test: Sortierung der initialen Events
	 */
	@Test
	void testSortInitialEvents() {
		for (int coreCount=1;coreCount<=2;coreCount++) for (Supplier<EventManager> manager: managers) {
			for (int i=0;i<6;i++) {
				log.clear();
				final SimulatorBaseTestImpl simulator=new SimulatorBaseTestImpl(coreCount,false,false,manager,()->new ListEventCache(),buildSortInitialEvents(i));
				assertEquals(coreCount,simulator.threadCount);
				runSimulator(simulator);
				resultsSortInitialEvents(simulator);
			}
		}
	}

	/**
	 * Test: Initiale Ereignisse, die jeweils die nächsten Ereignisse einfügen
	 */
	@Test
	void testAddConnectedEvents() {
		for (int coreCount=1;coreCount<=2;coreCount++) for (Supplier<EventManager> manager: managers) {
			log.clear();
			final SimulatorBaseTestImpl simulator=new SimulatorBaseTestImpl(coreCount,false,false,manager,()->new ListEventCache(),buildAddConnectedEvents());
			assertEquals(coreCount,simulator.threadCount);
			runSimulator(simulator);
			resultsAddConnectedEvents(simulator);
		}
	}

	/**
	 * Test: Ereignisse mit demselben Ausführungszeitpunkt
	 */
	@Test
	void testAddSameTimeEvents() {
		for (int coreCount=1;coreCount<=2;coreCount++) for (Supplier<EventManager> manager: managers) {
			log.clear();
			final SimulatorBaseTestImpl simulator=new SimulatorBaseTestImpl(coreCount,false,false,manager,()->new ListEventCache(),buildAddSameTimeEvents());
			assertEquals(coreCount,simulator.threadCount);
			runSimulator(simulator);
			resultsAddSameTimeEvents(simulator);
		}
	}

	/**
	 * Test: Ereignisse löschen (zu löschendes Ereignis zwischen anderen Ereignissen)
	 */
	@Test
	void testDeleteEvents1() {
		for (int coreCount=1;coreCount<=2;coreCount++) for (Supplier<EventManager> manager: managers) {
			log.clear();
			final SimulatorBaseTestImpl simulator=new SimulatorBaseTestImpl(coreCount,false,false,manager,()->new ListEventCache(),(data,nr)->{
				final List<Event> list=new ArrayList<>();
				final EventTestImpl deleteEvent=new EventTestImpl(200,2,(nr==0)?logger:null);
				list.add(deleteEvent);
				list.add(new EventTestImpl(100,1,(nr==0)?logger:null,(d,e)->{data.eventManager.deleteEvent(deleteEvent,d); return null;}));
				list.add(new EventTestImpl(300,3,(nr==0)?logger:null));
				return list;
			});
			assertEquals(coreCount,simulator.threadCount);
			runSimulator(simulator);

			assertEquals(2,log.size());
			assertEquals("1 100",log.get(0));
			assertEquals("3 300",log.get(1));

			assertEquals(2*simulator.threadCount,simulator.getEventCount());
			assertEquals(0,simulator.getEventQueueLength());
		}
	}

	/**
	 * Test: Ereignisse löschen (zu löschendes Ereignis nach anderen Ereignissen)
	 */
	@Test
	void testDeleteEvents2() {
		for (int coreCount=1;coreCount<=2;coreCount++) for (Supplier<EventManager> manager: managers) {
			log.clear();
			final SimulatorBaseTestImpl simulator=new SimulatorBaseTestImpl(coreCount,false,false,manager,()->new ListEventCache(),(data,nr)->{
				final List<Event> list=new ArrayList<>();
				list.add(new EventTestImpl(100,1,(nr==0)?logger:null,(data2,e)->{
					EventTestImpl deleteEvent;
					data.eventManager.addEvent(deleteEvent=new EventTestImpl(300,3,(nr==0)?logger:null));
					data.eventManager.addEvent(new EventTestImpl(200,2,(nr==0)?logger:null,(data3,e2)->{data.eventManager.deleteEvent(deleteEvent,data); return null;}));
					return null;
				}));
				return list;
			});
			assertEquals(coreCount,simulator.threadCount);
			runSimulator(simulator);

			assertEquals(2,log.size());
			assertEquals("1 100",log.get(0));
			assertEquals("2 200",log.get(1));

			assertEquals(2*simulator.threadCount,simulator.getEventCount());
			assertEquals(0,simulator.getEventQueueLength());
		}
	}

	/* Eventcache */

	/**
	 * Test: Eventcache (Ereignisse einlagern und wieder abrufen)
	 */
	@Test
	void testEventCache() {
		final EventTestImpl event=new EventTestImpl(100,1,null);

		for (EventCache cache: caches) {
			final SimData simData=new SimData(new PriorityQueueEventManager(),cache,0,1,1000);

			assertEquals(null,simData.getEventOrNull(EventTestImpl.class));

			simData.recycleEvent(event);
			if (!(cache instanceof NoEventCache) && cache!=null) {
				assertEquals(event,simData.getEventOrNull(EventTestImpl.class));
			}
			assertEquals(null,simData.getEventOrNull(EventTestImpl.class));

			simData.recycleEvent(event);
			if (!(cache instanceof NoEventCache) && cache!=null) {
				assertEquals(event,simData.getEvent(EventTestImpl.class));
			}
			assertNotSame(event,simData.getEvent(EventTestImpl.class));

			assertNull(simData.getEvent(Event.class));

			simData.recycleEvent(event);
			simData.eventCache.clear();
			assertEquals(null,simData.getEventOrNull(EventTestImpl.class));
		}
	}

	/**
	 * Test: Pause / schrittweise Simulation
	 */
	@Test
	void testStepSimulation() {
		for (int coreCount=1;coreCount<=2;coreCount++) for (Supplier<EventManager> manager: managers) {
			log.clear();
			final SimulatorBaseTestImpl simulator=new SimulatorBaseTestImpl(coreCount,false,false,manager,()->new ListEventCache(),buildSortInitialEvents(2));
			assertEquals(coreCount,simulator.threadCount);
			runSimulatorPaused(simulator);
			assertEquals(0,simulator.getEventCount());
			simulator.stepExecution(true);
			assertEquals(1*simulator.threadCount,simulator.getEventCount());
			simulator.resumeExecution();
			simulator.finalizeRun();
			assertTrue(simulator.isStarted());
			assertTrue(!simulator.isRunning());
			resultsSortInitialEvents(simulator);
		}
	}

	/**
	 * Test: Abfrage der Thread-Laufzeiten
	 */
	@Test
	void testThreadRunTimes() {
		for (int coreCount=1;coreCount<=2;coreCount++) for (Supplier<EventManager> manager: managers) {
			log.clear();
			final SimulatorBaseTestImpl simulator=new SimulatorBaseTestImpl(coreCount,false,false,manager,()->new ListEventCache(),buildSortInitialEvents(2));
			assertEquals(coreCount,simulator.threadCount);
			runSimulator(simulator);
			int[] times=simulator.getThreadRuntimes();
			assertNotNull(times);
			assertEquals(coreCount,times.length);
		}
	}

	/**
	 * Test: Simulation abbrechen
	 */
	@Test
	void testCancelSimulation() {
		for (int coreCount=1;coreCount<=2;coreCount++) for (Supplier<EventManager> manager: managers) {
			log.clear();
			final SimulatorBaseTestImpl simulator=new SimulatorBaseTestImpl(coreCount,false,false,manager,()->new ListEventCache(),buildSortInitialEvents(2));
			assertEquals(coreCount,simulator.threadCount);
			simulator.cancel();
		}

		for (int coreCount=1;coreCount<=2;coreCount++) for (Supplier<EventManager> manager: managers) {
			log.clear();
			final SimulatorBaseTestImpl simulator=new SimulatorBaseTestImpl(coreCount,false,false,manager,()->new ListEventCache(),buildSortInitialEvents(2));
			assertEquals(coreCount,simulator.threadCount);
			runSimulatorPaused(simulator);
			simulator.cancel();
		}

		for (int coreCount=1;coreCount<=2;coreCount++) for (Supplier<EventManager> manager: managers) {
			log.clear();
			final SimulatorBaseTestImpl simulator=new SimulatorBaseTestImpl(coreCount,false,false,manager,()->new ListEventCache(),buildSortInitialEvents(2));
			assertEquals(coreCount,simulator.threadCount);
			runSimulator(simulator);
			simulator.cancel();
		}
	}

	/**
	 * Test: Basisfunktionen aller Event-Manager
	 */
	@Test
	void testEventManager() {
		for (Supplier<EventManager> manager: managers) {
			final EventManager eventManager=manager.get();
			final List<Event> events=buildSortInitialEvents(2).apply(null,0);
			for (Event event: events) eventManager.addEvent(event);
			final List<Event> events2=eventManager.getAllEvents();
			assertEquals(events.size(),events2.size()); /* Reihenfolge kann sich geändert haben */
		}
	}

	/**
	 * Test: Logging
	 */
	@Test
	void testLogging() {

		for (int coreCount=1;coreCount<=2;coreCount++) for (Supplier<EventManager> manager: managers) {
			log.clear();
			final SimulatorBaseTestImpl simulator=new SimulatorBaseTestImpl(coreCount,false,false,manager,()->new ListEventCache(),(data,nr)->{
				data.activateLogging(new CallbackLogger(c->{if (nr==0) log.add(c.info);}));
				assertTrue(data.loggingActive);
				return buildSortInitialEvents(2).apply(data,nr);
			});
			assertEquals(coreCount,simulator.threadCount);
			runSimulator(simulator);
			assertEquals(3000*simulator.threadCount,simulator.getEventCount());
			assertEquals(6000,log.size());
			assertTrue(simulator.getEventsPerSecond()>0);
			assertTrue(simulator.getSimDayCount()>0);
		}

		for (int coreCount=1;coreCount<=2;coreCount++) for (Supplier<EventManager> manager: managers) {
			log.clear();
			final SimulatorBaseTestImpl simulator=new SimulatorBaseTestImpl(coreCount,false,false,manager,()->new ListEventCache(),(data,nr)->{
				data.activateLogging(new HTMLLogger(null,true,true,true,true,false,false,null));
				assertTrue(!data.loggingActive); /* ohne Datei kein Logging */
				return buildSortInitialEvents(2).apply(data,nr);
			});
			assertEquals(coreCount,simulator.threadCount);
			runSimulator(simulator);
			assertEquals(3000*simulator.threadCount,simulator.getEventCount());
			assertEquals(3000,log.size());
			assertTrue(simulator.getEventsPerSecond()>0);
			assertTrue(simulator.getSimDayCount()>0);
		}

		for (int coreCount=1;coreCount<=2;coreCount++) for (Supplier<EventManager> manager: managers) {
			log.clear();
			final SimulatorBaseTestImpl simulator=new SimulatorBaseTestImpl(coreCount,false,false,manager,()->new ListEventCache(),(data,nr)->{
				data.activateLogging(new PlainTextLogger(null,true,true,false,false,false));
				assertTrue(!data.loggingActive); /* ohne Datei kein Logging */
				return buildSortInitialEvents(2).apply(data,nr);
			});
			assertEquals(coreCount,simulator.threadCount);
			runSimulator(simulator);
			assertEquals(3000*simulator.threadCount,simulator.getEventCount());
			assertEquals(3000,log.size());
			assertTrue(simulator.getEventsPerSecond()>0);
			assertTrue(simulator.getSimDayCount()>0);
		}

		for (int coreCount=1;coreCount<=2;coreCount++) for (Supplier<EventManager> manager: managers) {
			log.clear();
			final SimulatorBaseTestImpl simulator=new SimulatorBaseTestImpl(coreCount,false,false,manager,()->new ListEventCache(),(data,nr)->{
				data.activateLogging(new PlainTextLoggerLimited(null,true,true,false,false,false,100));
				assertTrue(!data.loggingActive); /* ohne Datei kein Logging */
				return buildSortInitialEvents(2).apply(data,nr);
			});
			assertEquals(coreCount,simulator.threadCount);
			runSimulator(simulator);
			assertEquals(3000*simulator.threadCount,simulator.getEventCount());
			assertEquals(3000,log.size());
			assertTrue(simulator.getEventsPerSecond()>0);
			assertTrue(simulator.getSimDayCount()>0);
		}

		for (int coreCount=1;coreCount<=2;coreCount++) for (Supplier<EventManager> manager: managers) {
			log.clear();
			final SimulatorBaseTestImpl simulator=new SimulatorBaseTestImpl(coreCount,false,false,manager,()->new ListEventCache(),(data,nr)->{
				data.activateLogging(new RTFLogger(null,true,true,true,true,false,false,null));
				assertTrue(!data.loggingActive); /* ohne Datei kein Logging */
				return buildSortInitialEvents(2).apply(data,nr);
			});
			assertEquals(coreCount,simulator.threadCount);
			runSimulator(simulator);
			assertEquals(3000*simulator.threadCount,simulator.getEventCount());
			assertEquals(3000,log.size());
			assertTrue(simulator.getEventsPerSecond()>0);
			assertTrue(simulator.getSimDayCount()>0);
		}

		for (int coreCount=1;coreCount<=2;coreCount++) for (Supplier<EventManager> manager: managers) {
			log.clear();
			final SimulatorBaseTestImpl simulator=new SimulatorBaseTestImpl(coreCount,false,false,manager,()->new ListEventCache(),(data,nr)->{
				data.activateLogging(new File(""));
				assertTrue(!data.loggingActive); /* ohne Datei kein Logging */
				return buildSortInitialEvents(2).apply(data,nr);
			});
			assertEquals(coreCount,simulator.threadCount);
			runSimulator(simulator);
			assertEquals(3000*simulator.threadCount,simulator.getEventCount());
			assertEquals(3000,log.size());
			assertTrue(simulator.getEventsPerSecond()>0);
			assertTrue(simulator.getSimDayCount()>0);
		}

		for (int coreCount=1;coreCount<=2;coreCount++) for (Supplier<EventManager> manager: managers) {
			log.clear();
			final SimulatorBaseTestImpl simulator=new SimulatorBaseTestImpl(coreCount,false,false,manager,()->new ListEventCache(),(data,nr)->{
				data.activateLogging(new File(""));
				assertTrue(!data.loggingActive); /* ohne Datei kein Logging */
				data.disableLogging();
				return buildSortInitialEvents(2).apply(data,nr);
			});
			assertEquals(coreCount,simulator.threadCount);
			runSimulator(simulator);
			assertEquals(3000*simulator.threadCount,simulator.getEventCount());
			assertEquals(3000,log.size());
			assertTrue(simulator.getEventsPerSecond()>0);
			assertTrue(simulator.getSimDayCount()>0);
		}
	}

	/**
	 * Test: Formatierung der Zeit für die Logging-Ausgabe
	 * @see SimData#formatSimTime(long)
	 */
	@Test
	void testFormatSimTime() {
		assertEquals("00:00:00,000",SimData.formatSimTime(0));
		assertEquals("00:00:00,001",SimData.formatSimTime(1));
		assertEquals("00:00:00,020",SimData.formatSimTime(20));
		assertEquals("00:00:00,321",SimData.formatSimTime(321));
		assertEquals("00:01:02,001",SimData.formatSimTime(62*1000+1));
		assertEquals("00:10:00,001",SimData.formatSimTime(600*1000+1));
		assertEquals("00:01:23,001",SimData.formatSimTime(83*1000+1));
		assertEquals("01:00:00,001",SimData.formatSimTime(3600*1000+1));
		assertEquals("12:00:00,001",SimData.formatSimTime(12*3600*1000+1));
		assertEquals("1:00:00:00,001",SimData.formatSimTime(86400*1000+1));
		assertEquals("1:01:00:00,001",SimData.formatSimTime(86400*1000+3600*1000+1));
	}

	/**
	 * Test: Formatierung von Datum und Zeit für die Logging-Ausgabe
	 * @see SimData#formatSimDateTime(long)
	 */
	@Test
	void testFormatSimDateTime() {
		assertEquals("1970-01-01 00:00:00",SimData.formatSimDateTime(0));
	}

	/**
	 * Test: Formatierung von Objekt-IDs für die Logging-Ausgabe
	 * @see SimData#formatObjectID(Object)
	 */
	@Test
	void testFormatObjectID() {
		assertEquals("0",SimData.formatObjectID(null));
		assertTrue(!SimData.formatObjectID(new Object()).isEmpty());
	}
}
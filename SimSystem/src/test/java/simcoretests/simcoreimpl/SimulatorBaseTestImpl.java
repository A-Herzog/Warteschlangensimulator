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

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import simcore.Event;
import simcore.SimData;
import simcore.SimulatorBase;
import simcore.eventcache.EventCache;
import simcore.eventmanager.EventManager;
import simcoretests.SimulatorBaseTest;

/**
 * Wird von {@link SimulatorBaseTest} zur Prüfung der Funktionsweise
 * der Ereignisverwaltung verwendet.
 * @author Alexander Herzog
 * @see SimulatorBaseTest
 */
public class SimulatorBaseTestImpl extends SimulatorBase {
	private final BiFunction<SimData,Integer,List<Event>> initialEventGetter;
	private final Supplier<EventManager> getEventManager;
	private final Supplier<EventCache> getEventCache;

	/**
	 * Konstruktor der Klasse
	 * @param allowMaxCore	Maximale Anzahl an zu verwendenden Threads
	 * @param moreThreads	Mehr Threads als vorhandene Kerne verwenden?
	 * @param numaAware	NUMA-Speichermodell berücksichtigen? (d.h. Thread-lokale read-only Datenmodelle verwenden?)
	 * @param getEventManager	Callback, welches eine Instanz des zu verwendenden Eventmanagers liefert
	 * @param getEventCache	Callback, welches eine Instanz des zu verwendenden Event-Caches liefert
	 * @param initialEventGetter	Callback, welches die initialen Ereignisse pro Thread liefert
	 */
	public SimulatorBaseTestImpl(final int allowMaxCore, final boolean moreThreads, final boolean numaAware, final Supplier<EventManager> getEventManager, final Supplier<EventCache> getEventCache, final BiFunction<SimData,Integer,List<Event>> initialEventGetter) {
		super(allowMaxCore,moreThreads,numaAware);
		this.getEventManager=getEventManager;
		this.getEventCache=getEventCache;
		this.initialEventGetter=initialEventGetter;
	}

	@Override
	protected SimData getSimDataForThread(int threadNr, int threadCount) {
		final SimData simData=new SimData(getEventManager.get(),getEventCache.get(),threadNr,threadCount);

		final List<Event> initialEvents=initialEventGetter.apply(simData,threadNr);
		simData.eventManager.addInitialEvents(initialEvents);

		return simData;
	}
}

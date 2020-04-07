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

import simcore.SimData;
import simcore.SimulatorBase;
import simcore.eventcache.ListEventCache;
import simcore.eventmanager.PriorityQueueEventManager;

/**
 * Testsystem zum Testen des Simulatorkerns
 * @author Alexander Herzog
 * @version 1.1
 */
public final class TestSimulator extends SimulatorBase {

	private final int initialEvents;

	/**
	 * Konstruktor der Klasse <code>TestSimulator</code>
	 * @param initialevents	Anzahl der zu Anfang anzulegenden Ereignisse
	 */
	public TestSimulator(int initialevents) {this.initialEvents=initialevents;}

	@Override
	protected final SimData getSimDataForThread(int threadNr, int threadCount) {
		SimData simData=new SimData(new PriorityQueueEventManager(),new ListEventCache(),threadNr,threadCount);
		TestEvent.createAndAddInitialDummyEvents(initialEvents,simData,2);
		return simData;
	}

	/**
	 * Arbeitsschleife des Simulators
	 * (Kann nur per Programmabbruch beendet werden)
	 */
	public final void outputLoop() {
		while (true) {
			try {Thread.sleep(1000);} catch (InterruptedException e){}

			long runtime=Math.max(1,System.currentTimeMillis()-startTime);

			String s="Thread 1: ev/ms="+threads[0].eventCount/runtime+" Q="+threads[0].eventQueueLength;
			for (int i=1;i<threads.length;i++) {
				s=s+"  Thread "+(i+1)+": ev/ms="+threads[i].eventCount/runtime+" Q="+threads[i].eventQueueLength;
			}
			System.out.println(s);
		}
	}

	/**
	 * Testroutine, startet einen Endlos-Simulationsprozess mit <code>initialEvents</code> Startevents.
	 * Jedes Ereignis legt bei der Ausführung jeweils wieder ein neues auszuführendes Ereignis sowie ein
	 * weiter vorne liegender Ereignis an, welches bei der Ausführung des ersten Ereignisses unausgeführt
	 * gelöscht wird.<br>
	 * Leistungsdaten werden dabei auf der Console ausgegeben.<br>
	 * (Kann nur per Programmabbruch beendet werden)
	 * @param initialEvents	Anzahl der initial anzulegenden Ereignisse
	 */
	public static void createAndRunSimpleSimulator(int initialEvents) {
		TestSimulator simulator=new TestSimulator(initialEvents);
		simulator.start(false);
		simulator.outputLoop();
	}

	/**
	 * Hauptklasse für Tests
	 * @param args	Kommandozeilenparameter, werden nicht weiter verwendet
	 */
	public static void main(String[] args) {
		TestSimulator.createAndRunSimpleSimulator(500);
	}
}

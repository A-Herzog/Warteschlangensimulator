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
package simulator.events;
import simcore.Event;
import simcore.SimData;
import simulator.coreelements.RunElement;
import simulator.elements.RunSource;
import simulator.runmodel.SimulationData;

/**
 * Kundenankunfts-Event
 * @author Alexander Herzog
 */
public class SystemArrivalEvent extends Event {
	/**
	 * Kundenquelle über die der Kunde eintrifft<br>
	 * (die {@link RunSource#processArrivalEvent(SimulationData, boolean, int)}-Methode
	 * wird hier aufgerufen)
	 */
	public RunElement source;

	/**
	 * Soll seitens der Quelle die nächste Ankunft geplant werden?
	 */
	public boolean scheduleNext;

	/**
	 * Zusätzlicher Index, der der Quelle übergeben wird. (Ist z.B. bei Mehrfach-Quellen von Bedeutung)
	 */
	public int index;

	/**
	 * Konstruktor der Klasse
	 */
	public SystemArrivalEvent() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	@Override
	public void run(final SimData data) {
		if (source instanceof RunSource) ((RunSource)source).processArrivalEvent((SimulationData)data,scheduleNext,index);
	}
}
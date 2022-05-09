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
import simulator.elements.RunElementConveyorData;
import simulator.runmodel.SimulationData;

/**
 * Benachrichtigung des Systems, dass sich der Systemzustand in Bezug auf die Bewegung
 * eines Kunden auf einem Fließband verändert hat.
 * @author Alexander Herzog
 * @see RunElementConveyorData
 */
public class ConveyorSystemChangeEvent extends Event {
	/**
	 * Konstruktor der Klasse
	 */
	public ConveyorSystemChangeEvent() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	/**
	 * Daten-Element des Fließbandes das benachrichtigt werden soll
	 */
	public RunElementConveyorData conveyorData;

	@Override
	public void run(SimData data) {
		conveyorData.updateEvent((SimulationData)data);
	}
}

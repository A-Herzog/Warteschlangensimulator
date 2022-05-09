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
import simulator.elements.StateChangeListener;
import simulator.runmodel.SimulationData;

/**
 * Benachrichtigt lediglich ein Element über eine Pseudo-Änderung
 * des Systemzustands. Dies kann genutzt werden, um nach der Freigabe
 * eines Kunden an diesem einen Element nach einer gewissen Zeit
 * (z.B. einer Millisekunde) noch einmal zu prüfen, ob die Bedingung
 * zur Freigabe weiterer Kunden immer noch erfüllt ist. Es werden
 * dabei nicht alle anderen Stationen benachrichtigt, was Rechenzeit
 * spart.
 * @author Alexander Herzog
 */
public class ReleaseRecheckEvent extends Event {
	/**
	 * Konstruktor der Klasse
	 */
	public ReleaseRecheckEvent() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	/**
	 * Station, die über das normalen state-change-notify benachrichtigt werden soll.
	 */
	public StateChangeListener station;

	@Override
	public void run(SimData data) {
		final SimulationData simData=(SimulationData)data;
		station.systemStateChangeNotify(simData);
	}
}

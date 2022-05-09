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
package simulator.elements;

import simulator.runmodel.RunData;
import simulator.runmodel.SimulationData;

/**
 * Alle Laufzeitelemente, die dieses Interface implementieren, werden benachrichtigt, wenn
 * sich der Zustand des Systems ‰ndert.
 * @author Alexander Herzog
 */
public interface StateChangeListener {
	/**
	 * Benachrichtigung, dass sich der Zustand des Systems evtl. ver‰ndert hat.
	 * @param simData	Simulationsdatenobjekt
	 * @return	Gibt an, ob der Notify-Empf‰nger auf dieser Basis selbst Ver‰nderungen vorgenommen hat
	 */
	boolean systemStateChangeNotify(final SimulationData simData);

	/**
	 * Gibt an, ob die Station momentan daran interessiert ist, in regelm‰ﬂigen Abst‰nden (auch ohne Systemzustands‰nderungen) benachrichtigt zu werden.
	 * @param simData	Simulationsdatenobjekt
	 * @return	Gibt an, ob die Station momentan daran interessiert ist, in regelm‰ﬂigen Abst‰nden (auch ohne Systemzustands‰nderungen) benachrichtigt zu werden.
	 * @see RunData#runTimedCheckNow(SimulationData)
	 */
	default boolean interestedInChangeNotifiesAtTheMoment(final SimulationData simData) {
		return true;
	}
}

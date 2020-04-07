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

import simulator.runmodel.RunDataClient;
import simulator.runmodel.SimulationData;

/**
 * Alle Laufzeitelemente, die dieses Interface implementieren, werden benachrichtigt, wenn
 * sich ein Kunde von einer Station zu einer anderen bewegt.
 * @author Alexander Herzog
 */
public interface ClientMoveListener {
	/**
	 * Benachrichtigung, dass sich ein Kunde bewegt.
	 * @param simData	Simulationsdatenobjekt
	 * @param client	Kundenobjekt, welches an eine andere Station verwiesen wird
	 * @param moveByTransport	Gibt an, ob sich der Kunde entlang der Verbindungskanten (<code>false</code>) oder unsichtbar über einen Transporter (<code>true</code>) bewegt hat
	 */
	void clientMoveNotify(final SimulationData simData, final RunDataClient client, final boolean moveByTransport);
}

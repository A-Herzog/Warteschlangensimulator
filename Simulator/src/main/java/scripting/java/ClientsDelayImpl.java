/**
 * Copyright 2021 Alexander Herzog
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
package scripting.java;

import simulator.elements.RunElementDelay;
import simulator.runmodel.SimulationData;


/**
 * Implementierungsklasse für das Interface {@link ClientsInterface} (für Kunden an einer Verzögerung-Station)
 * @author Alexander Herzog
 * @see SystemInterface#getDelayStationData(int)
 */
public class ClientsDelayImpl extends ClientsBaseImpl {
	/**
	 * Zu dieser Kundenliste gehörende Verzögerung-Station
	 */
	private final RunElementDelay delay;

	/**
	 * Konstruktor der Klasse
	 * @param simData	Simulationsdatenobjekt, dessen Daten bereitgestellt werden sollen
	 * @param delay	Zu dieser Kundenliste gehörende Verzögerung-Station
	 */
	public ClientsDelayImpl(final SimulationData simData, final RunElementDelay delay) {
		super(simData);
		this.delay=delay;
	}

	/**
	 * Aktualisiert die Liste der Kunden in diesem Objekt aus der zu diesem Objekt gehörenden Verzögerung-Station
	 */
	public void updateClients() {
		setClients(delay.getClientsAtStation(simData));
	}

	@Override
	public void release(int index) {
		if (index<0 || index>=count) return;
		delay.releaseClientNow(simData,clients.get(index));
	}
}

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

import simulator.elements.RunElementProcess;
import simulator.runmodel.SimulationData;


/**
 * Implementierungsklasse für das Interface {@link ClientsInterface} (für Kunden in der Warteschlange einer Bedienstation-Station)
 * @author Alexander Herzog
 * @see SystemInterface#getProcessStationQueueData(int)
 */
public class ClientsProcessQueueImpl extends ClientsBaseImpl {
	/**
	 * Zu dieser Kundenliste gehörende Bedienstation
	 */
	private final RunElementProcess process;

	/**
	 * Konstruktor der Klasse
	 * @param simData	Simulationsdatenobjekt, dessen Daten bereitgestellt werden sollen
	 * @param process	Zu dieser Kundenliste gehörende Bedienstation
	 */
	public ClientsProcessQueueImpl(final SimulationData simData, final RunElementProcess process) {
		super(simData);
		this.process=process;
	}

	/**
	 * Aktualisiert die Liste der Kunden in diesem Objekt aus der zu diesem Objekt gehörenden Bedienstation
	 */
	public void updateClients() {
		setClients(process.getClientsInQueue(simData));
	}

	@Override
	public void release(int index) {
		/* Keine Freigabe von Kunden an einer Bedienstation möglich. */
	}
}

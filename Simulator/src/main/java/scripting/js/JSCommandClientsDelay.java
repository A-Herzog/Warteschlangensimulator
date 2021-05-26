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
package scripting.js;

import simulator.elements.RunElementDelay;
import simulator.runmodel.SimulationData;

/**
 * Stellt das "Clients"-Objekt in Javascript-Umgebungen zur Verfügung (für Kunden an einer Verzögerung-Station)
 * @author Alexander Herzog
 */
public final class JSCommandClientsDelay extends JSCommandClientsBase {
	/**
	 * Zu dieser Kundenliste gehörende Verzögerung-Station
	 */
	private RunElementDelay delay;

	/**
	 * Konstruktor der Klasse <code>JSCommandClients</code>
	 */
	public JSCommandClientsDelay() {
		super();
	}

	/**
	 * Stellt die Simulationsdaten und die Liste der wartenden Kunden für die Abfrage durch das Javascript-Verknüpfungs-Objekt ein
	 * @param simData	Simulationsdaten-Objekt (kann auch <code>null</code> sein)
	 * @param delay	Zu dieser Kundenliste gehörende Verzögerung-Station
	 */
	public void setSimulationData(final SimulationData simData, final RunElementDelay delay) {
		this.delay=delay;
		super.setSimulationData(simData,delay.getClientsAtStation(simData));
	}

	/**
	 * Gibt einen wartenden Kunden frei.
	 * @param index	Index des Kunden muss größer oder gleich 0 sein und kleiner als {@link #count()}
	 */
	public void release(final int index) {
		if (index<0 || index>=count) return;
		delay.releaseClientNow(simData,clients.get(index));
	}
}
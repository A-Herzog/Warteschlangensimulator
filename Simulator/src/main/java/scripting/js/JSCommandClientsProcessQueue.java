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

import simulator.elements.RunElementProcess;
import simulator.runmodel.SimulationData;

/**
 * Stellt das "Clients"-Objekt in Javascript-Umgebungen zur Verfügung (für Kunden in der Warteschlange an einer Bedienstation)
 * @author Alexander Herzog
 */
public final class JSCommandClientsProcessQueue extends JSCommandClientsBase {
	/**
	 * Konstruktor der Klasse
	 */
	public JSCommandClientsProcessQueue() {
		super();
	}

	/**
	 * Stellt die Simulationsdaten und die Liste der wartenden Kunden für die Abfrage durch das Javascript-Verknüpfungs-Objekt ein
	 * @param simData	Simulationsdaten-Objekt (kann auch <code>null</code> sein)
	 * @param process	Zu dieser Kundenliste gehörende Verzögerung-Station
	 */
	public void setSimulationData(final SimulationData simData, final RunElementProcess process) {
		super.setSimulationData(simData,process.getClientsInQueue(simData));
	}

	/**
	 * Gibt einen wartenden Kunden frei.
	 * @param index	Index des Kunden muss größer oder gleich 0 sein und kleiner als {@link #count()}
	 */
	public void release(final int index) {
		/* Keine Freigabe von Kunden an einer Bedienstation möglich. */
	}
}
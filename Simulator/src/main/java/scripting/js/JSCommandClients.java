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

import java.util.Arrays;
import java.util.List;

import simulator.runmodel.RunDataClient;
import simulator.runmodel.SimulationData;

/**
 * Stellt das "Clients"-Objekt in Javascript-Umgebungen zur Verf�gung (f�r Kunden an einer Bedingung(Skript)-Station)
 * @author Alexander Herzog
 */
public final class JSCommandClients extends JSCommandClientsBase {
	/** Gibt an, welche Kunden freigegeben werden sollen */
	private boolean[] releaseClients;

	/**
	 * Konstruktor der Klasse <code>JSCommandClients</code>
	 */
	public JSCommandClients() {
		super();
	}

	/**
	 * Stellt die Simulationsdaten und die Liste der wartenden Kunden f�r die Abfrage durch das Javascript-Verkn�pfungs-Objekt ein
	 * @param simData	Simulationsdaten-Objekt (kann auch <code>null</code> sein)
	 * @param clients	Liste der wartenden Kunden
	 */
	@Override
	public void setSimulationData(final SimulationData simData, final List<RunDataClient> clients) {
		super.setSimulationData(simData,clients);
		if (releaseClients!=null && releaseClients.length>=count) {
			Arrays.fill(releaseClients,false);
		} else {
			releaseClients=new boolean[count*2];
		}
	}

	/**
	 * Gibt an, welche der Kunden freigegeben werden sollen.<br>
	 * Achtung: Das Array kann <b>l�nger</b> als das Kundenarray sein!
	 * @return	Array mit Angaben dar�ber, welche Kunden freigegeben werden sollen
	 * @see JSCommandClients#setSimulationData(SimulationData, List)
	 */
	public boolean[] getSimulationData() {
		return releaseClients;
	}

	/**
	 * Gibt einen wartenden Kunden frei.
	 * @param index	Index des Kunden muss gr��er oder gleich 0 sein und kleiner als {@link #count()}
	 */
	public void release(final int index) {
		if (index<0 || index>=count) return;
		releaseClients[index]=true;
	}
}
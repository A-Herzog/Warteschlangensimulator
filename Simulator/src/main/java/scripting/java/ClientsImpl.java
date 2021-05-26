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
package scripting.java;

import java.util.Arrays;
import java.util.List;

import scripting.js.JSCommandClients;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.SimulationData;

/**
 * Implementierungsklasse für das Interface {@link ClientsInterface} (für Kunden an einer Bedingung(Skript)-Station)
 * @author Alexander Herzog
 * @see SimulationInterface
 * @see SimulationImpl
 */
public class ClientsImpl extends ClientsBaseImpl {
	/** Gibt an, welche Kunden freigegeben werden sollen */
	private boolean[] releaseClients;

	/**
	 * Konstruktor der Klasse
	 * @param simData	Simulationsdatenobjekt, dessen Daten bereitgestellt werden sollen
	 */
	public ClientsImpl(final SimulationData simData) {
		super(simData);
	}

	/**
	 * Stellt eine Liste der wartenden Kunden ein.
	 * @param clients	Liste der wartenden Kunden ein
	 */
	@Override
	public void setClients(final List<RunDataClient> clients) {
		super.setClients(clients);
		if (releaseClients!=null && releaseClients.length>=count) {
			Arrays.fill(releaseClients,0,count,false);
		} else {
			releaseClients=new boolean[count*2];
		}
	}

	/**
	 * Gibt an, welche der Kunden freigegeben werden sollen.<br>
	 * Achtung: Das Array kann <b>länger</b> als das Kundenarray sein!
	 * @return	Array mit Angaben darüber, welche Kunden freigegeben werden sollen
	 * @see JSCommandClients#setSimulationData(SimulationData, List)
	 */
	public boolean[] getSimulationData() {
		return releaseClients;
	}

	@Override
	public void release(final int index) {
		if (index<0 || index>=count) return;
		releaseClients[index]=true;
	}
}

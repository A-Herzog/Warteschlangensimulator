/**
 * Copyright 2022 Alexander Herzog
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

import java.util.List;

import scripting.java.ClientsDelayImpl;
import scripting.js.JSCommandClientsDelay;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.SimulationData;

/**
 * Dieses Interface wird von Verzögerungs-Elementen implementiert
 * und erlaubt das Auslesen der Liste der Kunden, die sich aktuell
 * an der Station befinden und die Freigabe von Kunden von anderen
 * Skript-Stationen aus.
 * @author Alexander Herzog
 * @see RunElementDelay
 * @see RunElementDelayJS
 * @see ClientsDelayImpl
 * @see JSCommandClientsDelay
 */
public interface DelayWithClientsList {
	/**
	 * Liefert die Liste der Kunden an dieser Station (sofern eine solche Liste geführt wird)
	 * @param simData	Simulationsdatenobjekt
	 * @return	Liste der Kunden (ist nie <code>null</code>, aber kann leer sein, insbesondere wenn keine solche Liste geführt wird)
	 */
	List<RunDataClient> getClientsAtStation(final SimulationData simData);

	/**
	 * Gibt einen Kunden an dieser Station sofort frei.
	 * @param simData	Simulationsdatenobjekt
	 * @param client	Freizugebender Kunde
	 * @return	Liefert <code>true</code>, wenn sich der Kunde an der Station befindet und freigegeben werden konnte
	 * @see #getClientsAtStation(SimulationData)
	 */
	boolean releaseClientNow(final SimulationData simData, final RunDataClient client);
}

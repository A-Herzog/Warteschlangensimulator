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
 * Den Laufzeitelementen, die dieses Interface implementieren, kann ein Kunde vom PickUp-Element weggenommen werden.<br>
 * Es muss dabei von dem Element geprüft werden, das PickUp, angetriggert duch einen System-Status-Wechsel, nicht mitten
 * in der eigenen Arbeit in die Warteschlange eingereiht.
 * @author Alexander Herzog
 */
public interface PickUpQueue {

	/**
	 * Anfrage, ob es einen wartenden Kunden gibt, den das Objekt hergeben kann.<br>
	 * Das Element muss dabei selbst prüfen, ob die Anfrage zu einem gültigen Zeitpunkt erfolgt.
	 * Das Element muss sich außerdem darum kümmern, den Kunden aus der eigenen Warteschlange auszutragen und die Wartezeit in das Kundenobjekt einzutragen.
	 * @param simData	Simulationsdatenobjekt
	 * @return	Ausgeschleuster Kunde oder <code>null</code>, wenn kein Kunde ausgeschleust werden konnte
	 */
	RunDataClient getClient(SimulationData simData);
}

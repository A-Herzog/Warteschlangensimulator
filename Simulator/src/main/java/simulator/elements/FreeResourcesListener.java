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

import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;

/**
 * Alle Laufzeitelemente, die dieses Interface implementieren, werden benachrichtigt, wenn eine Ressource frei wird.
 * @author Alexander Herzog
 */
public interface FreeResourcesListener {
	/**
	 * Benachrichtigung, dass eine Ressource freigeworden ist.
	 * @param simData	Simulationsdatenobjekt
	 */
	void releasedResourcesNotify(final SimulationData simData);

	/**
	 * Liefert die Priorität, die dieses Objekt bei der Zuweisung von freigewordenen Ressourcen erhalten soll.
	 * @param simData	Simulationsdatenobjekt
	 * @return	Priorität für die Zuweisung von verfügbaren Ressourcen
	 */
	ExpressionCalc getResourcePriority(final SimulationData simData);

	/**
	 * Wenn die Abfrage der Priorität über {@link FreeResourcesListener#getResourcePriority(SimulationData)} Gleichstand
	 * zwischen mehreren Stationen liefert, so kann diese Funktion aufgerufen werden, um eine feinere
	 * Unterteilung zu erhalten. Allerdings darf die Vergleichsfunktion auch alternativ (je nach Nutzereinstellung)
	 * zufällig entscheiden.
	 * @param simData	Simulationsdatenobjekt
	 * @return	Priorität für die Zuweisung von verfügbaren Ressourcen
	 */
	double getSecondaryResourcePriority(final SimulationData simData);
}

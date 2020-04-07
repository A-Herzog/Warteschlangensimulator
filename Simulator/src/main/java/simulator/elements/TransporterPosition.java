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

import simulator.runmodel.RunDataTransporter;
import simulator.runmodel.SimulationData;

/**
 * Laufzeitelemente, die mit Transportern interagieren, m�ssen dieses Interface
 * implementieren, um �ber Ank�nfte und Abg�nge benachrichtigt zu werden
 * @author Alexander Herzog
 */
public interface TransporterPosition {
	/**
	 * Transporter macht sich auf den Weg zur Station
	 * @param transporter	Transporterobjekt
	 * @param simData	Simulationsdatenobjekt
	 */
	void transporterStartsMoving(final RunDataTransporter transporter, final SimulationData simData);

	/**
	 * Transporter ist an Station angekommen
	 * @param transporter	Transporterobjekt
	 * @param simData	Simulationsdatenobjekt
	 */
	void transporterArrival(final RunDataTransporter transporter, final SimulationData simData);

	/**
	 * Transporter hat Station verlassen<br>
	 * (Bei Parkpl�tzen: Pr�fen, ob jetzt evtl. weitere Transporter angefordert werden sollen)
	 * @param transporter	Transporterobjekt
	 * @param simData	Simulationsdatenobjekt
	 */
	void transporterLeave(final RunDataTransporter transporter, final SimulationData simData);

	/**
	 * Es ist ein Transporter frei geworden
	 * @param transporter	Transporterobjekt
	 * @param simData	Simulationsdatenobjekt
	 */
	void transporterFree(final RunDataTransporter transporter, final SimulationData simData);

	/**
	 * Gibt an, welche Priorit�t diese Station in Bezug auf die Zuweisung dieses Transporters haben soll
	 * @param transporter	Transporterobjekt
	 * @param simData	Simulationsdatenobjekt
	 * @return	Priorit�t oder <code>null</code>, wenn diese Station diesen Transporter jetzt nicht haben m�chte
	 */
	Double requestPriority(final RunDataTransporter transporter, SimulationData simData);

	/**
	 * Gibt an, welche Priorit�t diese Station in Bezug auf das Halten dieses Transporters haben soll
	 * @param transporter	Transporterobjekt
	 * @param simData	Simulationsdatenobjekt
	 * @return	Priorit�t oder <code>null</code>, wenn diese Station diesen Transporter jetzt nicht haben m�chte
	 */
	Double stayHerePriority(final RunDataTransporter transporter, SimulationData simData);
}

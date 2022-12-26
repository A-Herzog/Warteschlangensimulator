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
import simulator.runmodel.RunDataTransporter;
import simulator.runmodel.SimulationData;
import ui.modeleditor.elements.ModelElementAnimationConnect;

/**
 * Ein Objekt, welches dieses Interface implementiert, muss bei dem
 * {@link ModelElementAnimationConnect}-Objekt in das Feld
 * {@link ModelElementAnimationConnect#animationViewer} eingetragen
 * werden. Dann wird das Objekt während der Simulation fortlaufend
 * über den Status der Simulation informiert.
 * @author Alexander Herzog
 * @see RunElementAnimationConnect
 */
public interface RunModelAnimationViewer {
	/**
	 * Allgemeiner Hinweis zu Status-Änderungen.<br>
	 * Wird durch {@link RunElementAnimationConnect#systemStateChangeNotify(SimulationData)} aufgerufen.
	 * @param simData	Simulationsdatenobjekt
	 * @return	Möchte der Viewer auch in Zukunft noch benachrichtigt werden? (Bei <code>false</code> folgen keine weiteren Aufrufe dieser Methode mehr.)
	 * @see StateChangeListener
	 */
	boolean updateViewer(final SimulationData simData);

	/**
	 * Hinweis, dass ein Kunde bewegt wurde.<br>
	 * Wird durch {@link RunElementAnimationConnect#clientMoveNotify(SimulationData, RunDataClient, boolean)} aufgerufen.
	 * @param simData	Simulationsdatenobjekt
	 * @param client	Kundenobjekt, welches an eine andere Station verwiesen wird
	 * @param moveByTransport	Gibt an, ob sich der Kunde entlang der Verbindungskanten (<code>false</code>) oder unsichtbar über einen Transporter (<code>true</code>) bewegt hat
	 * @return	Möchte der Viewer auch in Zukunft noch benachrichtigt werden? (Bei <code>false</code> folgen keine weiteren Aufrufe dieser Methode mehr.)
	 * @see ClientMoveListener
	 */
	boolean updateViewer(final SimulationData simData, final RunDataClient client, final boolean moveByTransport);

	/**
	 * Hinweis, dass ein Transporter bewegt wurde.<br>
	 * Wird durch {@link RunElementAnimationConnect#transporterMoveNotify(SimulationData, RunDataTransporter)} aufgerufen.
	 * @param simData	Simulationsdatenobjekt
	 * @param transporter	Transporterobjekt, welches sich bewegt hat
	 * @return	Möchte der Viewer auch in Zukunft noch benachrichtigt werden? (Bei <code>false</code> folgen keine weiteren Aufrufe dieser Methode mehr.)
	 * @see TransporterMoveListener
	 */
	boolean updateViewer(final SimulationData simData, final RunDataTransporter transporter);

	/**
	 * Wird aufgerufen, wenn die Animation beendet wurde.
	 */
	void animationTerminated();

	/**
	 * Wird aufgerufen, wenn vom Modell her die Animation pausiert werden soll.
	 */
	void pauseAnimation();
}

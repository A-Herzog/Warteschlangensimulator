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
package ui.modeleditor.elements;

import simulator.runmodel.SimulationData;

/**
 * Modell-Elemente, die dieses Interface implementieren, werden während der Animation
 * über den Zustand des Laufzeit-Modells informiert, um ggf. Informationen anzeigen
 * zu können.
 * @author Alexander Herzog
 */
public interface ElementWithAnimationDisplay extends ElementWithAnimationEditOptions {
	/**
	 * Diese Methode wird zu Beginn der Animation aufgerufen und ermöglicht es dem Element, sich zu initialisieren.
	 * @param simData	Simulations-Laufzeit-Daten
	 */
	void initAnimation(final SimulationData simData);

	/**
	 * Benachrichtigung über die aktuellen Simulationsdaten
	 * @param simData	Simulations-Laufzeit-Daten
	 * @param isPreview	Wird auf <code>true</code> gesetzt, wenn das <code>SimulationData</code>-Objekt bereits auf einem neueren Stand ist, als es die Zeitangabe angibt
	 * @return	Gibt <code>true</code> zurück, wenn sich der Zustand des Elements verändert hat und ein Neuzeichnen nötig ist.
	 */
	boolean updateSimulationData(final SimulationData simData, final boolean isPreview);
}

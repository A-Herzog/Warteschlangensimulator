/**
 * Copyright 2025 Alexander Herzog
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
package simulator;

import mathtools.distribution.tools.DistributionRandomNumber;
import simulator.editmodel.EditModel;

/**
 * Diese Singleton-Klasse stellt sicher, dass das {@link DistributionRandomNumber}-System
 * nicht gleichzeitig in verschiedenen Modi betrieben wird.
 */
public class DistributionRandomNumberManager {
	/**
	 * Diese Klasse stellt nur statische Methoden zur Verfügung und kann nicht instanziert werden.
	 */
	private DistributionRandomNumberManager() {
	}

	/**
	 * Statische Instanz der Klasse
	 * @see #getInstance()
	 */
	private static final DistributionRandomNumberManager instance=new DistributionRandomNumberManager();

	/**
	 * Liefert die statische Instanz dieser Klasse.
	 * @return	Statische Instanz der Klasse
	 * @see #instance
	 */
	public static DistributionRandomNumberManager getInstance() {
		return instance;
	}

	/**
	 * Anzahl der aktuell laufenden Instanzen
	 */
	private int runningInstances=0;

	/**
	 * Zu verwendender Zufallszahlengenerator
	 */
	private EditModel.RandomMode randomMode;

	/**
	 * Soll ein fester Startwert verwendet werden?
	 */
	private boolean fixedSeed;

	/**
	 * Fragt eine bestimmte Zufallszahlengenerator-Konfiguration an.
	 * @param randomMode	Zu verwendender Zufallszahlengenerator
	 * @param fixedSeed	Soll ein fester Startwert verwendet werden?
	 * @return	Liefert <code>true</code>, wenn eine Simulation mit dem gewählten Modus aktuell möglich ist und blockt die Einstellung dann auch für diesen Modus.
	 * @see #releaseMode()
	 */
	public boolean requestMode(final EditModel.RandomMode randomMode, final boolean fixedSeed) {
		if (runningInstances==0) {
			this.randomMode=randomMode;
			this.fixedSeed=fixedSeed;
			runningInstances=1;
			return true;
		}

		if (randomMode!=this.randomMode) return false;
		if (this.fixedSeed || fixedSeed) return false;
		runningInstances++;

		return true;
	}

	/**
	 * Gibt eine Konfiguration, die zuvor erfolgreich per {@link #requestMode(simulator.editmodel.EditModel.RandomMode, boolean)}
	 * angefragt wurde wieder frei.
	 * @see #requestMode(simulator.editmodel.EditModel.RandomMode, boolean)
	 */
	public void releaseMode() {
		if (runningInstances>0) runningInstances--;
	}
}

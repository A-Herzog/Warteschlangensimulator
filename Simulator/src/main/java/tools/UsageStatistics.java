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
package tools;

import mathtools.NumberTools;

/**
 * Diese Klasse dient der Speicherung der Nutzungsstatistik.<br>
 * F�r die eigentliche Speicherung wird auf ein Datenfeld in {@link SetupData} zur�ckgegriffen.<br>
 * Diese Klasse ist ein Singleton und kann nicht direkt instanziert werden.
 * Stattdessen muss die statische Methode {@link UsageStatistics#getInstance()} verwendet werden.
 * @author Alexander Herzog
 *
 */
public class UsageStatistics {
	private static UsageStatistics instance;
	private final SetupData setup;
	private long clients;

	/**
	 * Konstruktor der Klasse.<br>
	 * Diese Klasse ist ein Singleton und kann nicht direkt instanziert werden.
	 *  * Stattdessen muss die statische Methode {@link UsageStatistics#getInstance()} verwendet werden.
	 */
	private UsageStatistics() {
		clients=0;
		setup=SetupData.getSetup();
		loadFromSetup();
	}

	/**
	 * Liefert die Instanz dieser Singleton-Klasse
	 * @return	Instanz dieser Singleton-Klasse
	 */
	public static synchronized UsageStatistics getInstance() {
		if (instance==null) instance=new UsageStatistics();
		return instance;
	}

	/**
	 * L�dt die Statistikdaten aus dem System-Setup
	 * @see SetupData#usageStatistics
	 */
	public void loadFromSetup() {
		final Long L=NumberTools.getNotNegativeLong(setup.usageStatistics);
		if (L!=null) clients=L;
	}

	private void saveToSetup() {
		setup.usageStatistics=""+clients;
		setup.saveSetup();
	}

	/**
	 * Liefert die Anzahl der bislang simulierten Kundenank�fte
	 * @return	Anzahl der bislang simulierten Kundenank�fte
	 * @see UsageStatistics#addSimulationClients(long)
	 */
	public long getSimulationClients() {
		return clients;
	}

	/**
	 * F�gt eine neue Anzahl an simulierten Kundenank�nften zu dem Z�hler hinzu
	 * @param clients	Neue Anzahl an simulierten Kundenank�nften
	 * @see UsageStatistics#getSimulationClients()
	 */
	public void addSimulationClients(final long clients) {
		if (clients<=0) return;
		this.clients+=clients;
		saveToSetup();
	}
}
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
 * Für die eigentliche Speicherung wird auf ein Datenfeld in {@link SetupData} zurückgegriffen.<br>
 * Diese Klasse ist ein Singleton und kann nicht direkt instanziert werden.
 * Stattdessen muss die statische Methode {@link UsageStatistics#getInstance()} verwendet werden.
 * @author Alexander Herzog
 *
 */
public class UsageStatistics {
	/** Instanz dieser Singleton-Klasse */
	private static UsageStatistics instance;
	/** {@link SetupData}-Instanz aus der die Daten entnommen werden und in die sie geschrieben werden */
	private final SetupData setup;
	/** Anzahl der bislang simulierten Kundenanküfte */
	private long clients;
	/** Volllast-CPU-Sekunden */
	private long cpuSeconds;
	/** Bereits in der Statistik erfasste Volllast-CPU-Sekunden seit Programmstart */
	private long cpuSecondsSinceStart;

	/**
	 * Konstruktor der Klasse.<br>
	 * Diese Klasse ist ein Singleton und kann nicht direkt instanziert werden.
	 *  * Stattdessen muss die statische Methode {@link UsageStatistics#getInstance()} verwendet werden.
	 */
	private UsageStatistics() {
		clients=0;
		cpuSeconds=0;
		cpuSecondsSinceStart=0;
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
	 * Liefert die Anzahl an Volllast-CPU-Sekunden seit Programmstart.
	 * @return	Volllast-CPU-Sekunden seit Programmstart
	 */
	public static double getCPUSeconds() {
		return ProcessHandle.current().info().totalCpuDuration().get().getSeconds()/Runtime.getRuntime().availableProcessors();
	}

	/**
	 * Lädt die Statistikdaten aus dem System-Setup
	 * @see SetupData#usageStatistics
	 */
	public void loadFromSetup() {
		Long L;
		L=NumberTools.getNotNegativeLong(setup.usageStatistics);
		if (L!=null) clients=L;
		L=NumberTools.getNotNegativeLong(setup.usageCPUTime);
		if (L!=null) cpuSeconds=L;
	}

	/**
	 * Speichert die veränderten Daten im Setup.
	 * @see #addSimulationClients(long)
	 */
	private void saveToSetup() {
		setup.usageStatistics=""+clients;
		setup.usageCPUTime=""+cpuSeconds;
		setup.saveSetup();
	}

	/**
	 * Liefert die Anzahl der bislang simulierten Kundenanküfte.
	 * @return	Anzahl der bislang simulierten Kundenanküfte
	 * @see UsageStatistics#addSimulationClients(long)
	 */
	public long getSimulationClients() {
		return clients;
	}

	/**
	 * Liefert die Anzahl an bislang für die Simulation verwendeten Volllast-CPU-Sekunden.
	 * @return	Anzahl an bislang für die Simulation verwendeten Volllast-CPU-Sekunden
	 */
	public long getCPUSeonds() {
		return cpuSeconds;
	}

	/**
	 * Fügt eine neue Anzahl an simulierten Kundenankünften zu dem Zähler hinzu
	 * @param clients	Neue Anzahl an simulierten Kundenankünften
	 * @see UsageStatistics#getSimulationClients()
	 */
	public void addSimulationClients(final long clients) {
		if (clients<=0) return;

		this.clients+=clients;

		final long seconds=(long)Math.floor(getCPUSeconds());
		this.cpuSeconds+=(seconds-cpuSecondsSinceStart);
		cpuSecondsSinceStart=seconds;

		saveToSetup();
	}
}
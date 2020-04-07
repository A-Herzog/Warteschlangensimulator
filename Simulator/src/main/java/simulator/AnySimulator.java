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
package simulator;

import simulator.statistics.Statistics;

/**
 * Interface zur Kommunikation mit einem bereits gestarteten Simulator
 * @author Alexander Herzog
 * @see StartAnySimulator
 */
public interface AnySimulator {
	/**
	 * Bricht die Simulation vorzeitig ab.
	 */
	void cancel();

	/**
	 * Pr�ft, ob die Simulationsthreads noch laufen.
	 * @return Liefert <code>true</code> wenn mindestens ein Thread noch aktiv ist.
	 */
	boolean isRunning();

	/**
	 * Gibt die Summe der in allen Threads bisher simulierten Ereignisse zur�ck.
	 * @return Anzahl der simulierten Ereignisse in allen Threads
	 */
	long getEventCount();

	/**
	 * Gibt zur�ck, wie viele Ereignisse pro Sekunde verarbeitet werden.
	 * @return	Ereignisse pro Sekunde
	 */
	int getEventsPerSecond();

	/**
	 * Liefert die Gesamtanzahl an zu simulierenden Kundenank�nften
	 * @return	Gesamtanzahl an zu simulierenden Kundenank�nften
	 */
	long getCountClients();

	/**
	 * Liefert die Anzahl an bislang simulierten Kundenank�nften
	 * @return	Anzahl an bislang simulierten Kundenank�nften
	 */
	long getCurrentClients();

	/**
	 * Liefert die aktuelle Anzahl an Kunden im System
	 * @return	Aktuelle Anzahl an Kunden im System
	 */
	int getCurrentWIP();

	/**
	 * Gibt die Nummer des gerade im Simulator in Bearbeitung befindlichen Tages zur�ck.
	 * @return Gerade in Arbeit befindlicher Tag
	 */
	long getSimDayCount();

	/**
	 * Liefert die Gesamtanzahl an Wiederholungen in der Simulation.
	 * @return	Anzahl an Wiederholungen (�ber alle Threads) der Simulation.
	 */
	long getSimDaysCount();

	/**
	 * Wartet bis alle Simulationsthreads beendet sind und berechnet dann die gesamte Laufzeit.
	 * @return Liefert immer null zur�ck.
	 */
	String finalizeRun();

	/**
	 * Liefert nach Abschluss der Simulation die Statistikergebnisse zur�ck.
	 * @return	Statistik-Objekt, welches alle Daten des Simulationslaufs enth�lt (oder <code>null</code>, wenn die Simulation - ggf. auch durch den Server - abgebrochen wurde)
	 */
	Statistics getStatistic();
}

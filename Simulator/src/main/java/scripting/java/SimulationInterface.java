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
package scripting.java;

/**
 * Eine Implementierungsinstanz dieses Basis-Interface wird beim Aufruf
 * einer Nutzer-Java-Methode übergeben und ermöglicht so die Kommunikation
 * mit dem Simulator.
 * @author Alexander Herzog
 */
public interface SimulationInterface {
	/**
	 * Interface, welches simulationsunabhängige Laufzeit-Daten liefert.
	 * @return	Laufzeit-Daten-Interface (ist nie <code>null</code>).
	 */
	RuntimeInterface getRuntime();

	/**
	 * Interface, welches Daten zur aktuellen Simulation liefert.
	 * @return	Simulations-Daten-Interface (ist nicht <code>null</code>, wenn das Objekt zur Datenübergabe während der Simulation verwendet wird)
	 */
	SystemInterface getSystem();

	/**
	 * Interface, welches Daten zum aktuellen Kunden liefert.
	 * @return	Kunden-Daten-Interface (ist nicht <code>null</code>, wenn das Objekt zur Datenübergabe im Kontext eines Kunden-Objektes während der Simulation verwendet wird)
	 */
	ClientInterface getClient();

	/**
	 * Interface, welches Daten zu den aktuell wartenden Kunden liefert.
	 * @return	Warteschlangen-Daten-Interface (ist nicht <code>null</code>, wenn das Objekt zur Datenübergabe im Kontext eines Freigabebedingungs-Elements während der Simulation verwendet wird)
	 */
	ClientsInterface getClients();

	/**
	 * Interface, welches den aktuellen Eingabewert liefert.
	 * @return	Eingabe-Wert-Interface (ist nicht <code>null</code>, wenn das Objekt zur Datenübergabe im Kontext eines Eingabe-Elements während der Simulation verwendet wird)
	 */
	InputValueInterface getInputValue();

	/**
	 * Interface über das Werte ausgegeben werden können.
	 * @return	Ausgabe-Interface (ist nicht <code>null</code>, wenn das Objekt zur Datenausgabe im Kontext einer Textausgabe während der oder nach der Simulation verwendet wird)
	 */
	OutputInterface getOutput();

	/**
	 * Interface über das Werte in Dateien ausgegeben werden können.
	 * @return	Datei-Ausgabe-Interface (ist nur dann nicht <code>null</code>, wenn das Objekt innerhalb des Script-Runners verwendet wird)
	 */
	OutputInterface getFileOutput();

	/**
	 * Interface über das Werte aus der Statistik ausgelesen werden können.
	 * @return	Statistik-Interface (ist nicht <code>null</code>, wenn das Objekt zur Filterung von Statistikergebnissen verwendet wird)
	 */
	StatisticsInterface getStatistics();

	/**
	 * Interface über das Veränderungen am Modell vorgenommen werden können und die Simulation gestartet werden kann
	 * @return Modell-Interface (ist nur dann nicht <code>null</code>, wenn das Objekt innerhalb des Script-Runners verwendet wird)
	 */
	ModelInterface getModel();
}


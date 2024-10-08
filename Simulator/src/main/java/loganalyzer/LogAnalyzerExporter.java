/**
 * Copyright 2024 Alexander Herzog
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
package loganalyzer;

/**
 * Allgemeines System zum Exportieren von geladenen Simulations-Log-Dateien.
 * @see LogAnalyzer
 */
public interface LogAnalyzerExporter {
	/**
	 * Wird aufgerufen, bevor der Export startet.
	 * @see LogAnalyzer#export(LogAnalyzerExporter)
	 */
	default void exportStart() {}

	/**
	 * Gibt an, dass für die folgenden Ereignisse eine neue Zeit gilt.
	 * @param time	Ereigniszeitpunkt
	 */
	void setTime(final double time);

	/**
	 * Gibt an, dass für die folgenden Ereignisse ein neuer Kunde gilt.
	 * @param clientNr	Fortlaufende Nummer des Kunden (kann -1 sein, wenn kein Kunde für die folgenden Ereignisse definiert ist)
	 */
	void setClient(final long clientNr);

	/**
	 * Liefert ein zu exportierendes Ereignis.
	 * @param event	Zu exportierendes Ereignis
	 */
	void exportEvent(final LogAnalyzer.Event event);

	/**
	 * Wird aufgerufen, nachdem der Export abgeschlossen wurde.
	 * @see LogAnalyzer#export(LogAnalyzerExporter)
	 */
	default void exportDone() {}
}

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
 * Repräsentiert eine Ereignis-Box in der grafischen Log-Datei-Analyse.
 * @see LogAnalyzerGraphicExporter
 * @see LogAnalyzerGraphicExporterBox
 */
public class LogAnalyzerGraphicExporterEvent extends LogAnalyzerGraphicExporterBox {
	/**
	 * Zeitpunkt der Ereignisausführung
	 */
	public final double time;

	/**
	 * Ereignisname
	 */
	public final String name;

	/**
	 * Fortlaufende Nummer des Kunden (ist -1, wenn das Ereignis keinem konkreten Kunden zugeordnet werden konnte)
	 */
	public final long clientNr;

	/**
	 * ID der Station, an der das Ereignis auftrat (ist -1, wenn das Ereignis keiner konkreten Station zugeordnet werden konnte)
	 */
	public final long stationID;

	/**
	 * Name der Station, an der das Ereignis auftrat (ist <code>null</code>, wenn das Ereignis keiner konkreten Station zugeordnet werden konnte oder aber wenn kein Modell zum Laden der Namen verfügbar war)
	 */
	public final String stationName;

	/**
	 * Beschreibung zum Ereignis
	 */
	public final String description;

	/**
	 * Konstruktor
	 * @param event	Zugehöriges nicht-grafisches Ereignis in der Log-Datei-Analyse
	 * @see LogAnalyzer.Event
	 */
	public LogAnalyzerGraphicExporterEvent(final LogAnalyzer.Event event) {
		this.time=event.time;
		this.name=event.name;
		this.clientNr=event.clientNr;
		this.stationID=event.stationID;
		this.stationName=event.stationName;
		this.description=event.description;
	}
}

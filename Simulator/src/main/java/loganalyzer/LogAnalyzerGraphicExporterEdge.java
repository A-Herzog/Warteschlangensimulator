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
 * Repräsentiert eine Verbindungskante in der grafischen Log-Datei-Analyse.
 * @see LogAnalyzerGraphicExporter
 */
public class LogAnalyzerGraphicExporterEdge {
	/**
	 * Ausgangsbox der Verbindungskante
	 */
	public final LogAnalyzerGraphicExporterEvent event1;

	/**
	 * Zielbox der Verbindungskante
	 */
	public final LogAnalyzerGraphicExporterEvent event2;

	/**
	 * Konstruktor
	 * @param event1	Ausgangsbox der Verbindungskante
	 * @param event2	Zielbox der Verbindungskante
	 */
	public LogAnalyzerGraphicExporterEdge(final LogAnalyzerGraphicExporterEvent event1, final LogAnalyzerGraphicExporterEvent event2) {
		this.event1=event1;
		this.event2=event2;
	}
}

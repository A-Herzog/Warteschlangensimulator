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
 * Repräsentiert eine Kundennummer-Box in der grafischen Log-Datei-Analyse.
 * @see LogAnalyzerGraphicExporter
 * @see LogAnalyzerGraphicExporterBox
 */
public class LogAnalyzerGraphicExporterClient extends LogAnalyzerGraphicExporterBoxGroup {
	/**
	 * Kundennummer (kann -1 für "global" sein)
	 */
	public final long nr;

	/**
	 * Konstruktor
	 * @param nr	Kundennummer (kann -1 für "global" sein)
	 */
	public LogAnalyzerGraphicExporterClient(final long nr) {
		this.nr=nr;
	}
}

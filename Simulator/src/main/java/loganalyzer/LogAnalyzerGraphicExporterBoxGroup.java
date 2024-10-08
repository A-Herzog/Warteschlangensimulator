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

import java.util.ArrayList;
import java.util.List;

/**
 * Repräsentiert eine Box, die ihrerseits in Beziehung zu einer Reihe weiterer Ereignis-Boxen steht, in der grafischen Log-Datei-Analyse.
 * @see LogAnalyzerGraphicExporter
 * @see LogAnalyzerGraphicExporterEvent
 */
public class LogAnalyzerGraphicExporterBoxGroup extends LogAnalyzerGraphicExporterBox {
	/**
	 * Liste der Ereignisse in dieser Gruppe
	 */
	public List<LogAnalyzerGraphicExporterEvent> events;

	/**
	 * Konstruktor
	 */
	public LogAnalyzerGraphicExporterBoxGroup() {
		events=new ArrayList<>();
	}

	/**
	 * Fügt eine Ereignis-Box zu der Liste der dieser Box zugeordneten Ereignisse hinzu
	 * @param event	Ereignis das dieser Gruppe hinzugefügt werden soll
	 */
	public void add(final LogAnalyzerGraphicExporterEvent event) {
		events.add(event);
	}
}

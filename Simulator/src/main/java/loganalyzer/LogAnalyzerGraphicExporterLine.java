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

import java.awt.Point;

/**
 * Repräsentiert eine Linie in der grafischen Log-Datei-Analyse.
 * @see LogAnalyzerGraphicExporter
 */
public class LogAnalyzerGraphicExporterLine {
	/**
	 * Startpunkt der Linie
	 */
	public final Point p1;

	/**
	 * Endpunkt der Linie
	 */
	public final Point p2;

	/**
	 * Konstruktor
	 * @param p1	Startpunkt der Linie
	 * @param p2	Endpunkt der Linie
	 */
	public LogAnalyzerGraphicExporterLine(final Point p1, final Point p2) {
		this.p1=new Point(p1);
		this.p2=new Point(p2);
	}
}

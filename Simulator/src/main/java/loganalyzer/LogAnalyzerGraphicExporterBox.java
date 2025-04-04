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

import java.awt.Dimension;
import java.awt.Point;

/**
 * Repr�sentiert eine Box in der grafischen Log-Datei-Analyse.
 * @see LogAnalyzerGraphicExporter
 */
public class LogAnalyzerGraphicExporterBox {
	/**
	 * Obere linke Ecke der Box
	 */
	public final Point position;

	/**
	 * Gr��e der Box
	 */
	public final Dimension size;

	/**
	 * Konstruktor
	 */
	public LogAnalyzerGraphicExporterBox() {
		position=new Point();
		size=new Dimension();
	}
}

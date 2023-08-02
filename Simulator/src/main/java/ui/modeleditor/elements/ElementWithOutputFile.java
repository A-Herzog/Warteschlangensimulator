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
package ui.modeleditor.elements;

import ui.modeleditor.FilePathHelper;

/**
 * Elemente, die Ausgaben in Dateien vornehmen, implementieren dieses Interface,
 * damit der {@link FilePathHelper} bei Bedarf nach dem Laden den Pfad anpassen kann.
 * @author Alexander Herzog
 */
public interface ElementWithOutputFile {
	/**
	 * Liefert den Dateinamen der Datei, die für die Speicherung der Ausgaben verwendet werden soll.
	 * @return	Dateiname der Datei für die Ausgaben
	 */
	String getOutputFile();

	/**
	 * Stellt den Dateinamen der Datei, die für die Speicherung der Ausgaben verwendet werden soll, ein.
	 * @param outputFile	Dateiname der Datei für die Ausgaben
	 */
	void setOutputFile(final String outputFile);

	/**
	 * Ist die Ausgabe als Ganzes aktiv?
	 * @return	Ausgabe aktiv
	 * @see #setOutputActive(boolean)
	 */
	boolean isOutputActive();

	/**
	 * Stellt ein, ob die Ausgabe aktiv sein soll.
	 * @param outputActive	Ausgabe aktiv
	 * @see #isOutputActive()
	 */
	void setOutputActive(boolean outputActive);
}
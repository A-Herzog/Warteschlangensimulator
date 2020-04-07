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
 * Elemente, die Eingaben aus Dateien entgegennehmen, implementieren dieses Interface,
 * damit der {@link FilePathHelper} bei Bedarf nach dem Laden den Pfad anpassen kann.
 * @author Alexander Herzog
 */
public interface ElementWithInputFile {
	/**
	 * Liefert den Namen der Datei, aus der die Werte gelesen werden sollen
	 * @return	Name der Datei, aus der die Werte gelesen werden sollen
	 */
	String getInputFile();

	/**
	 * Stellt den Namen der Datei, aus der die Werte gelesen werden sollen, ein.
	 * @param inputFile	Name der Datei, aus der die Werte gelesen werden sollen
	 */
	void setInputFile(final String inputFile);
}

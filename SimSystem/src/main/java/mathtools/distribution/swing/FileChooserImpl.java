/**
 * Copyright 2025 Alexander Herzog
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
package mathtools.distribution.swing;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 * Interface für eine konkrete Implementierung eines Dateiauswahldialogs.
 * @see PlugableFileChooser
 * @see FileChooserImplJFileChooser
 */
public interface FileChooserImpl {
	/**
	 * Legt den Dialogtitel fest.
	 * @param title Dialogtitel
	 */
	void setDialogTitle(final String title);

	/**
	 * Fügt einen Dateitypen-Filter zur Liste der Filter hinzu.
	 * @param filter	Dateitypen-Filter
	 */
	void addChoosableFileFilter(final FileFilter filter);

	/**
	 * Liefert den aktuell gewählten Filter.
	 * @return	Aktuell gewählter Filter
	 */
	FileFilter getFileFilter();

	/**
	 * Stellt den ausgewählt anzuzeigenden Filter ein.
	 * @param filter	Aktiver Filter
	 */
	void setFileFilter(final FileFilter filter);

	/**
	 * Stellt ein, ob ein "Alle Dateien"-Filter angezeigt werden soll.
	 * @param b	"Alle Dateien"-Filter anzeigen
	 */
	void setAcceptAllFileFilterUsed(final boolean b);

	/**
	 * Liefert das aktuell gewählte Verzeichnis.
	 * @return	Gewähltes Verzeichnis
	 */
	File getCurrentDirectory();

	/**
	 * Stellt das aktuelle Verzeichnis ein.
	 * @param newCurrentDirectory	Anzuzeigendes Verzeichnis
	 */
	void setCurrentDirectory(final File newCurrentDirectory);

	/**
	 * Zeigt den Datei-Laden-Dialog an.
	 * @param parent	Übergeordnetes Element
	 * @return	Gibt an, wie der Dialog geschlossen wurde (z.B. {@link JFileChooser#APPROVE_OPTION})
	 */
	int showOpenDialog(final Component parent);

	/**
	 * Zeigt den Datei-Speichern-Dialog an.
	 * @param parent	Übergeordnetes Element
	 * @return	Gibt an, wie der Dialog geschlossen wurde (z.B. {@link JFileChooser#APPROVE_OPTION})
	 */
	int showSaveDialog(final Component parent);

	/**
	 * Liefert den Namen der gewählten Datei.
	 * @return	Name der gewählten Datei
	 */
	File getSelectedFile();

	/**
	 * Stellt die gewählte Datei ein.
	 * @param file	Anzuzeigende gewählte Datei
	 */
	void setSelectedFile(final File file);

	/**
	 * Legt den Auswahlmodus fest.
	 * @param mode Auswahlmodus (z.B. {@link JFileChooser#DIRECTORIES_ONLY})
	 */
	void setFileSelectionMode(final int mode);
}


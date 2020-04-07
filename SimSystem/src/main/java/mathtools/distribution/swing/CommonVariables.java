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
package mathtools.distribution.swing;

import java.io.File;

import javax.swing.JFileChooser;

/**
 * Diese Klasse hält als Singleton das jeweils zuletzt in einem
 * Dateiauswahldialog gewählte Verzeichnis vor.
 * @author Alexander Herzog
 * @version 1.0
 */
public final class CommonVariables {
	private static CommonVariables commonVariables=null;

	private CommonVariables() {}

	/**
	 * Liefert das Singleton dieser Klasse
	 * @return	(Einzige) Instanz dieser Klasse
	 */
	public static CommonVariables getCommonVariables() {
		if (commonVariables==null) commonVariables=new CommonVariables();
		return commonVariables;
	}

	/**
	 * Zuletzt verwendetes Verzeichnis (kann, insbesondere zu Anfang, <code>null</code> sein)
	 */
	public File lastFileChooserDirectory=null;

	/**
	 * Trägt das zuletzt verwendete Verzeichnis als initiales Verzeichnis in ein {@link JFileChooser}-Objekt ein
	 * @param fileChooser	Dateiauswahl-Dialog in dem das initiale Verzeichnis eingestellt werden soll
	 */
	public static void initialDirectoryToJFileChooser(final JFileChooser fileChooser) {
		if (getCommonVariables().lastFileChooserDirectory!=null) fileChooser.setCurrentDirectory(getCommonVariables().lastFileChooserDirectory);
	}

	/**
	 * Speichert auf Basis der Auswahl in einem Dateiauswahl-Dialog das zuletzt verwendete Verzeichnis
	 * @param fileChooser	Dateiauswahl-Dialog aus dem das Verzeichnis ausgelesen und gespeichert werden soll
	 */
	public static void initialDirectoryFromJFileChooser(final JFileChooser fileChooser) {
		getCommonVariables().lastFileChooserDirectory=new File(fileChooser.getCurrentDirectory().toURI());
	}

	/**
	 * Speichert auf Basis einer Datei das zuletzt verwendete Verzeichnis
	 * @param file	Datei dessen Verzeichnis als zuletzt verwendetes Verzeichnis gespeichert werden soll
	 */
	public static void setInitialDirectoryFromFile(final File file) {
		getCommonVariables().lastFileChooserDirectory=file.getParentFile();
	}
}

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
package ui.script;

import java.awt.Component;
import java.io.File;

import language.Language;
import mathtools.distribution.swing.PlugableFileChooser;

/**
 * Diese Klasse stellt statische Hilfsroutinen für die
 * Verarbeitung von Skripten.
 * @author Alexander Herzog
 */
public class ScriptTools {

	/**
	 * Konstruktor der Klasse.<br>
	 * Diese Klasse kann nicht instanziert werden, sie enthält nur statische Methoden.
	 */
	private ScriptTools() {}

	/**
	 * Zeigt einen Dialog zur Auswahl eines Ordners an
	 * @param owner	Übergeordnete Komponente
	 * @param dialogTitle	Dialogtitel
	 * @return	Name des Ordners oder <code>null</code>, wenn der Dialog abgebrochen wurde
	 */
	public static String selectFolder(final Component owner, final String dialogTitle) {
		final var fc=new PlugableFileChooser(true);
		fc.setDialogTitle(dialogTitle);
		final File file=fc.showSelectDirectoryDialog(owner);
		if (file==null) return null;
		return file.toString();
	}

	/**
	 * Zeigt einen Dialog zur Auswahl einer Textdatei an.
	 * @param owner	Übergeordnete Komponente
	 * @param dialogTitle	Dialogtitel
	 * @param oldFileName	Bisheriger Dateiname (kann <code>null</code> oder leer sein) zur Vorauswahl eines Verzeichnisses
	 * @return	Name der Datei oder <code>null</code>, wenn der Dialog abgebrochen wurde
	 */
	public static String selectTextFile(final Component owner, final String dialogTitle, final String oldFileName) {
		File initialDirectory=null;
		if (oldFileName!=null && !oldFileName.isEmpty()) initialDirectory=(new File(oldFileName)).getParentFile();
		final var fc=new PlugableFileChooser(initialDirectory,true);
		if (dialogTitle!=null) fc.setDialogTitle(dialogTitle);
		fc.addChoosableFileFilter(Language.tr("FileType.Text")+" (*.txt)","txt");
		fc.setFileFilter("txt");
		final File file=fc.showOpenDialogFileWithExtension(owner);
		if (file==null) return null;
		return file.getAbsolutePath();
	}

	/**
	 * Zeigt einen Dialog zur Auswahl einer Javascript-Datei an
	 * @param owner	Übergeordnete Komponente
	 * @param dialogTitle	Dialogtitel
	 * @param oldFileName	Bisheriger Dateiname (kann <code>null</code> oder leer sein) zur Vorauswahl eines Verzeichnisses
	 * @return	Name der Datei oder <code>null</code>, wenn der Dialog abgebrochen wurde
	 */
	public static String selectJSFile(final Component owner, final String dialogTitle, final String oldFileName) {
		File initialDirectory=null;
		if (oldFileName!=null && !oldFileName.isEmpty()) initialDirectory=(new File(oldFileName)).getParentFile();
		final var fc=new PlugableFileChooser(initialDirectory,true);
		if (dialogTitle!=null) fc.setDialogTitle(dialogTitle);
		fc.addChoosableFileFilter(Language.tr("FileType.JS")+" (*.js)","js");
		fc.setFileFilter("js");
		final File file=fc.showOpenDialogFileWithExtension(owner);
		if (file==null) return null;
		return file.getAbsolutePath();
	}

	/**
	 * Zeigt einen Dialog zur Auswahl einer Java-Datei an
	 * @param owner	Übergeordnete Komponente
	 * @param dialogTitle	Dialogtitel
	 * @param oldFileName	Bisheriger Dateiname (kann <code>null</code> oder leer sein) zur Vorauswahl eines Verzeichnisses
	 * @return	Name der Datei oder <code>null</code>, wenn der Dialog abgebrochen wurde
	 */
	public static String selectJavaFile(final Component owner, final String dialogTitle, final String oldFileName) {
		File initialDirectory=null;
		if (oldFileName!=null && !oldFileName.isEmpty()) initialDirectory=(new File(oldFileName)).getParentFile();
		final var fc=new PlugableFileChooser(initialDirectory,true);
		if (dialogTitle!=null) fc.setDialogTitle(dialogTitle);
		fc.addChoosableFileFilter(Language.tr("FileType.Java")+" (*.java)","java");
		fc.setFileFilter("java");
		final File file=fc.showOpenDialogFileWithExtension(owner);
		if (file==null) return null;
		return file.getAbsolutePath();
	}

	/**
	 * Zeigt einen Dialog zur Auswahl einer Javascript-Datei zum Speichern an
	 * @param owner	Übergeordnete Komponente
	 * @param dialogTitle	Dialogtitel
	 * @param oldFileName	Bisheriger Dateiname (kann <code>null</code> oder leer sein) zur Vorauswahl eines Verzeichnisses
	 * @return	Name der Datei oder <code>null</code>, wenn der Dialog abgebrochen wurde
	 */
	public static String selectJSSaveFile(final Component owner, final String dialogTitle, final String oldFileName) {
		File initialDirectory=null;
		if (oldFileName!=null && !oldFileName.isEmpty()) initialDirectory=(new File(oldFileName)).getParentFile();
		final var fc=new PlugableFileChooser(initialDirectory,true);
		if (dialogTitle!=null) fc.setDialogTitle(dialogTitle);
		fc.addChoosableFileFilter(Language.tr("FileType.JS")+" (*.js)","js");
		fc.setFileFilter("js");
		fc.setAcceptAllFileFilterUsed(false);
		final File file=fc.showSaveDialogFileWithExtension(owner);
		if (file==null) return null;
		return file.getAbsolutePath();
	}

	/**
	 * Zeigt einen Dialog zur Auswahl einer Java-Datei zum Speichern an
	 * @param owner	Übergeordnete Komponente
	 * @param dialogTitle	Dialogtitel
	 * @param oldFileName	Bisheriger Dateiname (kann <code>null</code> oder leer sein) zur Vorauswahl eines Verzeichnisses
	 * @return	Name der Datei oder <code>null</code>, wenn der Dialog abgebrochen wurde
	 */
	public static String selectJavaSaveFile(final Component owner, final String dialogTitle, final String oldFileName) {
		File initialDirectory=null;
		if (oldFileName!=null && !oldFileName.isEmpty()) initialDirectory=(new File(oldFileName)).getParentFile();
		final var fc=new PlugableFileChooser(initialDirectory,true);
		if (dialogTitle!=null) fc.setDialogTitle(dialogTitle);
		fc.addChoosableFileFilter(Language.tr("FileType.Java")+" (*.java)","java");
		fc.setFileFilter("java");
		fc.setAcceptAllFileFilterUsed(false);
		final File file=fc.showSaveDialogFileWithExtension(owner);
		if (file==null) return null;
		return file.getAbsolutePath();
	}

	/**
	 * Zeigt einen Dialog zur Auswahl einer Text-Datei zum Speichern an
	 * @param owner	Übergeordnete Komponente
	 * @param dialogTitle	Dialogtitel
	 * @param oldFileName	Bisheriger Dateiname (kann <code>null</code> oder leer sein) zur Vorauswahl eines Verzeichnisses
	 * @return	Name der Datei oder <code>null</code>, wenn der Dialog abgebrochen wurde
	 */
	public static String selectTextSaveFile(final Component owner, final String dialogTitle, final String oldFileName) {
		File initialDirectory=null;
		if (oldFileName!=null && !oldFileName.isEmpty()) initialDirectory=(new File(oldFileName)).getParentFile();
		final var fc=new PlugableFileChooser(initialDirectory,true);
		if (dialogTitle!=null) fc.setDialogTitle(dialogTitle);
		fc.addChoosableFileFilter(Language.tr("FileType.Text")+" (*.txt)","txt");
		fc.setFileFilter("txt");
		fc.setAcceptAllFileFilterUsed(false);
		final File file=fc.showSaveDialogFileWithExtension(owner);
		if (file==null) return null;
		return file.getAbsolutePath();
	}
}
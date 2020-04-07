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

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import language.Language;
import mathtools.distribution.swing.CommonVariables;

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
		final JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		fc.setDialogTitle(dialogTitle);
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (fc.showSaveDialog(owner)!=JFileChooser.APPROVE_OPTION) return null;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		final File file=fc.getSelectedFile();
		return file.toString();
	}

	/**
	 * Zeigt einen Dialog zur Auswahl einer Textdatei an
	 * @param owner	Übergeordnete Komponente
	 * @param dialogTitle	Dialogtitel
	 * @param oldFileName	Bisheriger Dateiname (kann <code>null</code> oder leer sein) zur Vorauswahl eines Verzeichnisses
	 * @return	Name der Datei oder <code>null</code>, wenn der Dialog abgebrochen wurde
	 */
	public static String selectTextFile(final Component owner, final String dialogTitle, final String oldFileName) {
		JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		if (dialogTitle!=null) fc.setDialogTitle(dialogTitle);
		final FileFilter txt=new FileNameExtensionFilter(Language.tr("FileType.Text")+" (*.txt)","txt");
		fc.addChoosableFileFilter(txt);
		fc.setFileFilter(txt);
		if (oldFileName!=null && !oldFileName.isEmpty()) {
			File oldFile=new File(oldFileName);
			fc.setCurrentDirectory(oldFile.getParentFile());
		}
		if (fc.showOpenDialog(owner)!=JFileChooser.APPROVE_OPTION) return null;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();
		if (file.getName().indexOf('.')<0 && fc.getFileFilter()==txt) file=new File(file.getAbsoluteFile()+".txt");

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
		JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		if (dialogTitle!=null) fc.setDialogTitle(dialogTitle);
		final FileFilter js=new FileNameExtensionFilter(Language.tr("FileType.JS")+" (*.js)","js");
		fc.addChoosableFileFilter(js);
		fc.setFileFilter(js);
		if (oldFileName!=null && !oldFileName.isEmpty()) {
			File oldFile=new File(oldFileName);
			fc.setCurrentDirectory(oldFile.getParentFile());
		}
		if (fc.showOpenDialog(owner)!=JFileChooser.APPROVE_OPTION) return null;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();
		if (file.getName().indexOf('.')<0 && fc.getFileFilter()==js) file=new File(file.getAbsoluteFile()+".js");

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
		JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		if (dialogTitle!=null) fc.setDialogTitle(dialogTitle);
		final FileFilter java=new FileNameExtensionFilter(Language.tr("FileType.Java")+" (*.java)","java");
		fc.addChoosableFileFilter(java);
		fc.setFileFilter(java);
		if (oldFileName!=null && !oldFileName.isEmpty()) {
			File oldFile=new File(oldFileName);
			fc.setCurrentDirectory(oldFile.getParentFile());
		}
		if (fc.showOpenDialog(owner)!=JFileChooser.APPROVE_OPTION) return null;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();
		if (file.getName().indexOf('.')<0 && fc.getFileFilter()==java) file=new File(file.getAbsoluteFile()+".java");

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
		JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		if (dialogTitle!=null) fc.setDialogTitle(dialogTitle);
		final FileFilter js=new FileNameExtensionFilter(Language.tr("FileType.JS")+" (*.js)","js");
		fc.addChoosableFileFilter(js);
		fc.setFileFilter(js);
		if (oldFileName!=null && !oldFileName.isEmpty()) {
			File oldFile=new File(oldFileName);
			fc.setCurrentDirectory(oldFile.getParentFile());
		}
		if (fc.showSaveDialog(owner)!=JFileChooser.APPROVE_OPTION) return null;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();
		if (file.getName().indexOf('.')<0 && fc.getFileFilter()==js) file=new File(file.getAbsoluteFile()+".js");

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
		JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		if (dialogTitle!=null) fc.setDialogTitle(dialogTitle);
		final FileFilter java=new FileNameExtensionFilter(Language.tr("FileType.Java")+" (*.java)","java");
		fc.addChoosableFileFilter(java);
		fc.setFileFilter(java);
		if (oldFileName!=null && !oldFileName.isEmpty()) {
			File oldFile=new File(oldFileName);
			fc.setCurrentDirectory(oldFile.getParentFile());
		}
		if (fc.showSaveDialog(owner)!=JFileChooser.APPROVE_OPTION) return null;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();
		if (file.getName().indexOf('.')<0 && fc.getFileFilter()==java) file=new File(file.getAbsoluteFile()+".java");

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
		JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		if (dialogTitle!=null) fc.setDialogTitle(dialogTitle);
		final FileFilter js=new FileNameExtensionFilter(Language.tr("FileType.Text")+" (*.txt)","txt");
		fc.addChoosableFileFilter(js);
		fc.setFileFilter(js);
		if (oldFileName!=null && !oldFileName.isEmpty()) {
			File oldFile=new File(oldFileName);
			fc.setCurrentDirectory(oldFile.getParentFile());
		}
		if (fc.showSaveDialog(owner)!=JFileChooser.APPROVE_OPTION) return null;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();
		if (file.getName().indexOf('.')<0 && fc.getFileFilter()==js) file=new File(file.getAbsoluteFile()+".txt");

		return file.getAbsolutePath();
	}
}
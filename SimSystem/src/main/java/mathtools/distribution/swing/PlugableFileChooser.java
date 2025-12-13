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
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Helferklasse zur Verwendung von {@link JFileChooser} oder auch anderen Implementierungen.
 */
public class PlugableFileChooser {
	/**
	 * Klasse für die zu verwendende Implementierung
	 */
	public static Class<? extends FileChooserImpl> fileChooserImplClass=FileChooserImplJFileChooser.class;

	/**
	 * Klasse der standardmäßig verwendetem Implementierung
	 * @see #fileChooserImplClass
	 */
	public static final Class<? extends FileChooserImpl> fileChooserImplClassDefault=FileChooserImplJFileChooser.class;

	/**
	 * Dieses Verzeichnis beim Aufruf anzeigen.
	 */
	private File initialDirectory=null;

	/**
	 * Soll das zuletzt verwendete Verzeichnis gespeichert und wiederhergestellt werden?
	 */
	private boolean restoreAndSaveInitialDirectory=false;

	/**
	 * Instanz der zu verwendenden Implementierung
	 * @see #fileChooserImplClass
	 */
	private final FileChooserImpl fileChooser;

	/**
	 * Registrierte Filter
	 */
	private final List<FileFilter> filters;

	/**
	 * Konstruktor der Klasse
	 */
	public PlugableFileChooser() {
		FileChooserImpl fileChooser;
		try {
			if (fileChooserImplClass==null) {
				fileChooser=new FileChooserImplJFileChooser();
			} else {
				fileChooser=fileChooserImplClass.getDeclaredConstructor().newInstance();
			}
		} catch (InstantiationException|IllegalAccessException|IllegalArgumentException|InvocationTargetException|NoSuchMethodException|SecurityException e) {
			fileChooser=new FileChooserImplJFileChooser();
		}
		this.fileChooser=fileChooser;
		filters=new ArrayList<>();
	}

	/**
	 * Konstruktor der Klasse
	 * @param restoreAndSaveInitialDirectory	Soll das zuletzt verwendete Verzeichnis gespeichert und wiederhergestellt werden?
	 */
	public PlugableFileChooser(final boolean restoreAndSaveInitialDirectory) {
		this();
		this.restoreAndSaveInitialDirectory=restoreAndSaveInitialDirectory;
	}

	/**
	 * Konstruktor der Klasse
	 * @param initialDirectory	Dieses Verzeichnis beim Aufruf anzeigen (kann <code>null</code> sein).
	 * @param restoreAndSaveInitialDirectory	Soll das zuletzt verwendete Verzeichnis gespeichert und wiederhergestellt werden?
	 */
	public PlugableFileChooser(final File initialDirectory, final boolean restoreAndSaveInitialDirectory) {
		this();
		this.initialDirectory=initialDirectory;
		this.restoreAndSaveInitialDirectory=restoreAndSaveInitialDirectory;
		if (this.initialDirectory!=null) {
			/* Damit das Verzeichnis bereits vor dem Aufruf des Dialogs abgerufen werden kann. */
			fileChooser.setCurrentDirectory(this.initialDirectory);
		}
	}

	/**
	 * Konstruktor der Klasse
	 * @param initialDirectory	Dieses Verzeichnis beim Aufruf anzeigen (kann <code>null</code> sein).
	 * @param restoreAndSaveInitialDirectory	Soll das zuletzt verwendete Verzeichnis gespeichert und wiederhergestellt werden?
	 */
	public PlugableFileChooser(final String initialDirectory, final boolean restoreAndSaveInitialDirectory) {
		this((initialDirectory==null)?null:new File(initialDirectory),restoreAndSaveInitialDirectory);
	}

	/**
	 * Legt den Dialogtitel fest.
	 * @param title Dialogtitel
	 */
	public void setDialogTitle(final String title) {
		fileChooser.setDialogTitle(title);
	}

	/**
	 * Fügt einen Dateitypen-Filter zur Liste der Filter hinzu.
	 * @param filter	Dateitypen-Filter
	 */
	public void addChoosableFileFilter(final FileFilter filter) {
		fileChooser.addChoosableFileFilter(filter);
		filters.add(filter);
	}

	/**
	 * Fügt mehrere Dateitypen-Filter zur Liste der Filter hinzu.
	 * @param filters	Dateitypen-Filter
	 */
	public void addChoosableFileFilters(final FileFilter... filters) {
		if (filters==null) return;
		for (var filter: filters) addChoosableFileFilter(filter);
	}

	/**
	 * Fügt mehrere Dateitypen-Filter zur Liste der Filter hinzu.
	 * @param description Name des Filters
	 * @param extensions Dateinamenserweiterungen, die zu diesem Filter gehören
	 * @return Neu erstelltes Filter-Objekt
	 */
	public FileNameExtensionFilter addChoosableFileFilter(final String description, String... extensions) {
		final var filter=new FileNameExtensionFilter(description,extensions);
		addChoosableFileFilter(filter);
		return filter;
	}

	/**
	 * Liefert den aktuell gewählten Filter.
	 * @return	Aktuell gewählter Filter
	 */
	public FileFilter getFileFilter() {
		return fileChooser.getFileFilter();
	}

	/**
	 * Stellt den ausgewählt anzuzeigenden Filter ein.
	 * @param filter	Aktiver Filter
	 */

	public void setFileFilter(final FileFilter filter) {
		fileChooser.setFileFilter(filter);
	}

	/**
	 * Stellt den ausgewählt anzuzeigenden Filter ein.
	 * @param filterExtension	Eine der Dateinamenserweiterungen des Filters, der aktiviert werden soll
	 */
	public void setFileFilter(final String filterExtension) {
		for (var filter: filters) if (filter instanceof FileNameExtensionFilter) {
			for (var ext: ((FileNameExtensionFilter)filter).getExtensions()) if (ext.equalsIgnoreCase(filterExtension)) {
				setFileFilter(filter);
				return;
			}
		}
	}

	/**
	 * Liefert das aktuell gewählte Verzeichnis.
	 * @return	Gewähltes Verzeichnis
	 */
	public File getCurrentDirectory() {
		return fileChooser.getCurrentDirectory();
	}

	/**
	 * Stellt das aktuelle Verzeichnis ein.
	 * @param newCurrentDirectory	Anzuzeigendes Verzeichnis
	 */
	public void setCurrentDirectory(final File newCurrentDirectory) {
		fileChooser.setCurrentDirectory(newCurrentDirectory);
	}

	/**
	 * Stellt ein, ob ein "Alle Dateien"-Filter angezeigt werden soll.
	 * @param b	"Alle Dateien"-Filter anzeigen
	 */
	public void setAcceptAllFileFilterUsed(final boolean b) {
		fileChooser.setAcceptAllFileFilterUsed(b);
	}

	/**
	 * Stellt die gewählte Datei ein.
	 * @param file	Anzuzeigende gewählte Datei
	 */
	public void setSelectedFile(final File file) {
		if (file!=null) fileChooser.setSelectedFile(file);
	}

	/**
	 * Stellt die gewählte Datei ein.
	 * @param file	Anzuzeigende gewählte Datei
	 */
	public void setSelectedFile(final String file) {
		if (file!=null) fileChooser.setSelectedFile(new File(file));
	}

	/**
	 * Zeigt den Datei-Laden-Dialog an.
	 * @param parent	Übergeordnetes Element
	 * @return	Gibt an, wie der Dialog geschlossen wurde (z.B. {@link JFileChooser#APPROVE_OPTION})
	 */
	public int showOpenDialog(final Component parent) {
		if (restoreAndSaveInitialDirectory) CommonVariables.initialDirectoryToPlugableFileChooser(this);
		final int result=fileChooser.showOpenDialog(parent);
		if (result==JFileChooser.APPROVE_OPTION && restoreAndSaveInitialDirectory) CommonVariables.initialDirectoryFromPlugableFileChooser(this);
		return result;
	}

	/**
	 * Zeigt den Datei-Speichern-Dialog an.
	 * @param parent	Übergeordnetes Element
	 * @return	Gibt an, wie der Dialog geschlossen wurde (z.B. {@link JFileChooser#APPROVE_OPTION})
	 */
	public int showSaveDialog(final Component parent) {
		if (initialDirectory!=null) {
			fileChooser.setCurrentDirectory(initialDirectory);
		} else {
			if (restoreAndSaveInitialDirectory) CommonVariables.initialDirectoryToPlugableFileChooser(this);
		}
		final int result=fileChooser.showSaveDialog(parent);
		if (result==JFileChooser.APPROVE_OPTION && restoreAndSaveInitialDirectory) CommonVariables.initialDirectoryFromPlugableFileChooser(this);
		return result;
	}

	/**
	 * Liefert den Namen der gewählten Datei.
	 * @return	Name der gewählten Datei
	 */
	public File getSelectedFile() {
		return fileChooser.getSelectedFile();
	}

	/**
	 * Liefert den Namen der gewählten Datei und hängt dabei, wenn nötig, die Dateiendung gemäß des gewählten Filters an.
	 * @return	Name der gewählten Datei
	 */
	public File getSelectedFileWithExtension() {
		File file=getSelectedFile();

		if (file!=null && file.getName().indexOf('.')<0 && filters.contains(fileChooser.getFileFilter()) && (fileChooser.getFileFilter() instanceof FileNameExtensionFilter)) {
			file=new File(file.getAbsoluteFile()+"."+((FileNameExtensionFilter)fileChooser.getFileFilter()).getExtensions()[0]);
		}

		return file;
	}

	/**
	 * Zeigt den Datei-Laden-Dialog an.
	 * @param parent	Übergeordnetes Element
	 * @return	Liefert im Erfolgsfall den Namen der gewählten Datei (oder <code>null</code>, wenn der Dialog abgebrochen wurde); bei Bedarf wird die Dateiendung gemäß des gewählten Filters an den Namen angefügt
	 */
	public File showOpenDialogFileWithExtension(final Component parent) {
		if (showOpenDialog(parent)!=JFileChooser.APPROVE_OPTION) return null;
		return getSelectedFileWithExtension();
	}

	/**
	 * Zeigt den Datei-Speichern-Dialog an.
	 * @param parent	Übergeordnetes Element
	 * @return	Liefert im Erfolgsfall den Namen der gewählten Datei (oder <code>null</code>, wenn der Dialog abgebrochen wurde); bei Bedarf wird die Dateiendung gemäß des gewählten Filters an den Namen angefügt
	 */
	public File showSaveDialogFileWithExtension(final Component parent) {
		if (showSaveDialog(parent)!=JFileChooser.APPROVE_OPTION) return null;
		return getSelectedFileWithExtension();
	}

	/**
	 * Zeigt einen Dialog zur Auswahl eines Verzeichnisses an.
	 * @param parent	Übergeordnetes Element
	 * @return	Liefert im Erfolgsfall den Namen des gewählten Verzeichnisses (oder <code>null</code>, wenn der Dialog abgebrochen wurde)
	 */
	public File showSelectDirectoryDialog(final Component parent) {
		if (initialDirectory!=null) {
			fileChooser.setCurrentDirectory(initialDirectory);
		} else {
			if (restoreAndSaveInitialDirectory) CommonVariables.initialDirectoryToPlugableFileChooser(this);
		}
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (fileChooser.showSaveDialog(parent)!=JFileChooser.APPROVE_OPTION) return null;
		if (restoreAndSaveInitialDirectory) CommonVariables.initialDirectoryFromPlugableFileChooser(this);
		return fileChooser.getSelectedFile();
	}
}

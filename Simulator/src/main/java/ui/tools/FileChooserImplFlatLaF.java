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
package ui.tools;

import java.awt.Component;
import java.io.File;
import java.util.Map;
import java.util.stream.Stream;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.collections4.map.HashedMap;

import com.formdev.flatlaf.util.SystemFileChooser;
import com.formdev.flatlaf.util.SystemInfo;

import mathtools.distribution.swing.FileChooserImpl;
import mathtools.distribution.swing.PlugableFileChooser;

/**
 * Wrapper für eine {@link JFileChooser}-Klasse für die Verwendung in
 * {@link PlugableFileChooser}
 * @see PlugableFileChooser
 */
public class FileChooserImplFlatLaF implements FileChooserImpl {
	/**
	 * Tatsächliche Dateiauswahl-Instanz
	 */
	private final SystemFileChooser fileChooser;

	/**
	 * Zuordnung zwischen {@link FileNameExtensionFilter}-Objekten, die von {@link JFileChooser} verwendet
	 * werden und FileNameExtensionFilter-Objekten gemäß {@link SystemFileChooser},
	 * um nach außen über {@link FileNameExtensionFilter}-Objekte kommunizieren zu können.
	 */
	private final Map<FileNameExtensionFilter,SystemFileChooser.FileFilter> filterMap;

	/**
	 * Konstruktor der Klasse
	 */
	public FileChooserImplFlatLaF() {
		fileChooser=new SystemFileChooser();
		filterMap=new HashedMap<>();
	}

	@Override
	public void setDialogTitle(String title) {
		fileChooser.setDialogTitle(title);
	}

	/**
	 * Der FlatLaF-SystemFileChooser erlaubt keinen "." innerhalb einer Extension.
	 * Daher müssen wir diese leider ausfiltern.
	 * @param description	Beschreibung für den Filter
	 * @param extensions	Liste mit Extensions
	 * @return	Gefilterte Liste mit Extensions
	 */
	private static SystemFileChooser.FileFilter makeExtensionsFlatLaFCompatible(final String description, final String[] extensions) {
		if (Stream.of(extensions).filter(extension->extension.contains(".")).findFirst().isPresent()) {
			/* "." in Erweiterungen müssen über einen PatternFilter behandelt werden - den es aber unter MacOS nicht gibt */
			if (SystemInfo.isMacOS) {
				final String[] extensionsNew=Stream.of(extensions).map(extension->extension.replace(".","-")).toArray(String[]::new);
				return new SystemFileChooser.FileNameExtensionFilter(description,extensionsNew);
			} else {
				final String[] extensionsNew=Stream.of(extensions).map(extension->"*."+extension).toArray(String[]::new);
				return new SystemFileChooser.PatternFilter(description,extensionsNew);
			}
		} else {
			/* Keine besondere Verarbeitung nötig */
			return new SystemFileChooser.FileNameExtensionFilter(description,extensions);
		}
	}


	/**
	 * Wandelt einen Filter gemäß {@link JFileChooser} in einen Filter gemäß {@link SystemFileChooser} um.
	 * @param filter	Filter gemäß {@link JFileChooser}
	 * @return	Filter gemäß {@link SystemFileChooser} (wird bei Bedarf neu angelegt)
	 * @see #filterMap
	 */
	private SystemFileChooser.FileFilter toNewFilter(final FileFilter filter) {
		if (!(filter instanceof FileNameExtensionFilter)) return null;
		final FileNameExtensionFilter oldFilter=(FileNameExtensionFilter)filter;
		return filterMap.computeIfAbsent(oldFilter,old->makeExtensionsFlatLaFCompatible(old.getDescription(),old.getExtensions()));
	}

	/**
	 * Wandelt einen Filter gemäß {@link SystemFileChooser} in einen Filter gemäß {@link JFileChooser} um.
	 * @param filter	Filter gemäß {@link SystemFileChooser}
	 * @return	Filter gemäß {@link JFileChooser} oder <code>null</code>, wenn es keine Zuordnung gibt
	 * @see #filterMap
	 */
	private FileFilter toOldFilter(final SystemFileChooser.FileFilter filter) {
		return filterMap.entrySet().stream().filter(entry->entry.getValue()==filter).findFirst().map(Map.Entry::getKey).orElse(null);
	}

	@Override
	public void addChoosableFileFilter(final FileFilter filter) {
		final var newFilter=toNewFilter(filter);
		if (newFilter!=null) fileChooser.addChoosableFileFilter(newFilter);
	}

	@Override
	public void setFileFilter(final FileFilter filter) {
		final var newFilter=toNewFilter(filter);
		if (newFilter!=null) fileChooser.setFileFilter(newFilter);
	}

	@Override
	public FileFilter getFileFilter() {
		return toOldFilter(fileChooser.getFileFilter());
	}

	@Override
	public int showOpenDialog(final Component parent) {
		return fileChooser.showOpenDialog(parent);
	}

	@Override
	public int showSaveDialog(final Component parent) {
		return fileChooser.showSaveDialog(parent);
	}

	@Override
	public void setAcceptAllFileFilterUsed(boolean b) {
		fileChooser.setAcceptAllFileFilterUsed(b);
	}

	@Override
	public File getCurrentDirectory() {
		return fileChooser.getCurrentDirectory();
	}

	@Override
	public void setCurrentDirectory(File newCurrentDirectory) {
		fileChooser.setCurrentDirectory(newCurrentDirectory);
	}

	@Override
	public File getSelectedFile() {
		return fileChooser.getSelectedFile();
	}

	@Override
	public void setSelectedFile(final File file) {
		fileChooser.setSelectedFile(file);
	}

	@Override
	public void setFileSelectionMode(int mode) {
		fileChooser.setFileSelectionMode(mode);
	}
}

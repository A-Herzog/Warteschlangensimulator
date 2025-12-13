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
 * Wrapper für eine {@link JFileChooser}-Klasse für die Verwendung in
 * {@link PlugableFileChooser}
 * @see PlugableFileChooser
 */
public class FileChooserImplJFileChooser implements FileChooserImpl {
	/**
	 * Tatsächliche Dateiauswahl-Instanz
	 */
	private final JFileChooser fileChooser;

	/**
	 * Konstruktor der Klasse
	 */
	public FileChooserImplJFileChooser() {
		fileChooser=new JFileChooser();
	}

	@Override
	public void setDialogTitle(String title) {
		fileChooser.setDialogTitle(title);
	}

	@Override
	public void addChoosableFileFilter(final FileFilter filter) {
		fileChooser.addChoosableFileFilter(filter);
	}

	@Override
	public void setFileFilter(final FileFilter filter) {
		fileChooser.setFileFilter(filter);
	}

	@Override
	public FileFilter getFileFilter() {
		return fileChooser.getFileFilter();
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
	public void setFileSelectionMode(final int mode) {
		fileChooser.setFileSelectionMode(mode);
	}
}

/**
 * Copyright 2022 Alexander Herzog
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
package ui.statistics;

import java.awt.Component;
import java.awt.Container;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.io.File;

import javax.swing.JButton;

import systemtools.statistics.StatisticViewerSpecialBase;

/**
 * Basisklasse für besondere Statistikseiten, die keine Kopierfunktionen oder
 * ähnliches mitbringen
 * @author Alexander Herzog
 * @see StatisticViewerSpecialBase
 */
public abstract class StatisticViewerSpecialBasePlain extends StatisticViewerSpecialBase {
	/**
	 * Innere Komponente zur Anzeige der Daten
	 * @see #getViewerIntern()
	 */
	protected Container viewer;

	/**
	 * Konstruktor der Klasse
	 */
	public StatisticViewerSpecialBasePlain() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	@Override
	public boolean getCanDo(CanDoAction canDoType) {
		return false;
	}

	@Override
	public Container getViewer(boolean needReInit) {
		if (viewer!=null && !needReInit) return viewer;
		return viewer=getViewerIntern();
	}

	/**
	 * Erzeugt die innere Komponente zur Anzeige der Daten
	 * @return	Interne Komponente zur Anzeige der Daten (wird vom Aufrufer u.a. in {@link #viewer} gespeichert)
	 */
	protected abstract Container getViewerIntern();

	@Override
	public boolean isViewerGenerated() {
		return viewer!=null;
	}

	@Override
	public Transferable getTransferable() {
		return null;
	}

	@Override
	public void copyToClipboard(Clipboard clipboard) {
	}

	@Override
	public boolean print() {
		return false;
	}

	@Override
	public void save(Component owner) {
	}

	@Override
	public void navigation(JButton button) {
	}

	@Override
	public void search(Component owner) {
	}

	@Override
	public boolean save(Component owner, File file) {
		return false;
	}

	@Override
	public boolean hasOwnFileDropListener() {
		return false;
	}
}

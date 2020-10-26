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
package ui;

import java.awt.Component;
import java.io.File;

import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import tools.SetupData;
import ui.dialogs.ModelRestoreDialog;

/**
 * Ermöglicht das automatische Speichern und wieder Laden von Modellen
 * beim Programmende bzw. dem darauf folgenden Programmstart
 * @author Alexander Herzog
 */
public class ModelRestore {
	/** Anfang für alle Dateinamen für die Auto-Wiederherstellung */
	private static final String FILE_PREFIX="AutoStore";
	/** Ende für alle Dateinamen für die Auto-Wiederherstellung */
	private static final String FILE_SUFFIX=".xml";

	/**
	 * Löschen der geladenen Datei erfolgreich
	 * @see #isDeleteOnLoadSuccessful()
	 */
	private static boolean deleteOnLoadSuccessful=true;

	/**
	 * Konstruktor der Klasse.<br>
	 * Diese Klasse kann nicht instanziert werden.
	 * Sie stellt nur statische Methoden zur Verfügung.
	 */
	private ModelRestore() {}

	/**
	 * Liefert eine Liste mit allen wiederherstellbaren Modelldatieen
	 * @return	Liste mit allen wiederherstellbaren Modelldatieen
	 */
	private static File[] getModelFiles() {
		return SetupData.getSetupFolder().listFiles((dir,name)->{
			if (!name.startsWith(FILE_PREFIX)) return false;
			if (!name.endsWith(FILE_SUFFIX)) return false;
			final String middle=name.substring(FILE_PREFIX.length(),name.length()-FILE_SUFFIX.length());
			return NumberTools.getNotNegativeInteger(middle)!=null;
		});
	}

	/**
	 * Liefert einen bislang noch nicht vergebenen Namen für die Auto-Wiederherstellungs-Datei
	 * @param maxFiles	Maximalanzahl an Auto-Wiederherstellungs-Dateien die berücksichtigt werden sollen
	 * @return	Auto-Wiederherstellungs-Datei oder <code>null</code>, wenn die zulässige Maximalanzahl bereits erreicht ist
	 */
	private static File getAvailableModelFileName(final int maxFiles) {
		int nr=-1;
		File file;
		do {
			nr++;
			if (maxFiles<=nr) return null; /* Wir zählen die Dateien ab 0. */
			file=new File(SetupData.getSetupFolder(),FILE_PREFIX+nr+FILE_SUFFIX);
		} while (file.exists());
		return file;
	}

	/**
	 * Lädt und löscht eine Modelldatei
	 * @param file	Zu ladende Datei
	 * @return	Liefert im Erfolgfall das Modell
	 */
	private static EditModel loadAndDeleteFile(final File file) {
		if (file==null) return null;
		final EditModel model=new EditModel();
		try {
			if (model.loadFromFile(file)==null) return model; else return null;
		} finally {
			deleteOnLoadSuccessful=file.delete();
		}
	}

	/**
	 * Wurde ein Modell geladen und danach versucht, die zugehörige Datei zu löschen,
	 * so wird hier angegeben, ob das Löschen erfolgreich war.
	 * @return	Löschen der geladenen Datei erfolgreich
	 */
	public static boolean isDeleteOnLoadSuccessful() {
		return deleteOnLoadSuccessful;
	}

	/**
	 * Zeigt einen Dialog zur Auswahl des wiederherzustellenden Modells an.
	 * @param owner	Übergeordnetes Element
	 * @param files	Liste der zum Wiederherstellen verfügbaren Modellen
	 * @return	Zum Wiederherstellen ausgewählte Datei (oder <code>null</code>, wenn kein Modell ausgewählt wurde)
	 * @see ModelRestoreDialog
	 */
	private static File selectModel(final Component owner, final File[] files) {
		if (files==null  || files.length==0) return null;
		if (files.length==1) return files[0];

		final ModelRestoreDialog dialog=new ModelRestoreDialog(owner,files);
		if (dialog.getClosedBy()!=BaseDialog.CLOSED_BY_OK) return null;
		return dialog.getSelectedModelFile();
	}

	/**
	 * Speichert ein Modell
	 * @param model	Zu speicherndes Modell (darf <code>null</code> sein, dann passiert nichts)
	 * @return	Erfolg der Speicherung
	 */
	public static boolean autoSave(final EditModel model) {
		if (!SetupData.getSetup().autoRestore) return false;

		if (model==null || model.surface.getElementCount()==0) return false;
		final File file=getAvailableModelFileName(20);
		if (file==null) return false;
		return model.saveToFile(file);
	}

	/**
	 * Versucht ein zuvor gespeichertes Modell wieder zu laden.
	 * @param owner	Übergeordnetes Element (für Auswahldialog)
	 * @return	Geladenes Modell oder <code>null</code>, wenn es nichts zu laden gibt.
	 */
	public static EditModel autoRestore(final Component owner) {
		if (!SetupData.getSetup().autoRestore) return null;
		return loadAndDeleteFile(selectModel(owner,getModelFiles()));
	}

	/**
	 * Löscht alle noch verfügbaren weiteren gespeicherten Modelle.
	 * @return	Gibt an, ob das Löschen für alle Dateien erfolgreich war.
	 */
	public static boolean clearAll() {
		boolean allOk=true;
		final File[] files=getModelFiles();
		if (files!=null) for (File file: files) if (!file.delete()) allOk=false;
		return allOk;
	}
}
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
package ui.modeleditor;

import java.io.File;

import simulator.db.DBConnectSetup;
import simulator.db.DBConnectSetups;
import simulator.db.DBSettings;
import simulator.editmodel.EditModel;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ElementWithDB;
import ui.modeleditor.elements.ElementWithInputFile;
import ui.modeleditor.elements.ElementWithOutputFile;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Prüft ob angegebene Dateien existieren und ändert ggf. den Pfad.<br>
 * Diese Klasse stellt nur statische Methoden zur Verfügung und kann nicht instanziert werden.
 * @author Alexander Herzog
 */
public class FilePathHelper {
	/**
	 * Konstruktor der Klasse.<br>
	 * Diese Klasse stellt nur statische Methoden zur Verfügung und kann nicht instanziert werden.
	 */
	private FilePathHelper() {
	}

	/**
	 * Prüft alle Elemente eines Modells in Bezug auf die Pfade der Ein- und Ausgabedateipfade
	 * @param model	Zu prüfendes Modell
	 * @param modelFile	Dateiname der Modelldatei dessen Pfad für Anpassungen verwendet wird
	 * @return	Liefert <code>true</code>, wenn etwas verändert wurde
	 */
	public static boolean checkFilePaths(final EditModel model, final File modelFile) {
		boolean changed=false;

		/* Pfade in den Elementen */
		for (ModelElement element1: model.surface.getElements()) {
			if (checkElement(element1,modelFile)) changed=true;
			if (element1 instanceof ModelElementSub) for (ModelElement element2: ((ModelElementSub)element1).getSubSurface().getElements()) {
				if (checkElement(element2,modelFile)) changed=true;
			}
		}

		/* Pfade im Modell als solches */
		if (model.pluginsFolder!=null && !model.pluginsFolder.isBlank()) {
			final String oldPluginsFolder=model.pluginsFolder.trim();
			final String newPluginsFolder=checkInputFolder(oldPluginsFolder,modelFile);
			if (!newPluginsFolder.equals(oldPluginsFolder)) {
				model.pluginsFolder=newPluginsFolder;
				changed=true;
			}
		}

		return changed;
	}

	/**
	 * Prüft eine Datenbankeinstellung in Bezug auf die Pfade für die Datenbankdatei
	 * @param settings	Zu prüfende Einstellung
	 * @param modelFile	Dateiname der Modelldatei dessen Pfad für Anpassungen verwendet wird
	 * @return	Liefert <code>true</code>, wenn etwas verändert wurde
	 */
	private static boolean checkDB(final DBSettings settings, final File modelFile) {
		final DBConnectSetup setup=DBConnectSetups.getByType(settings.getType());
		if (setup==null) return false;
		if (!setup.selectSource.isFile) return false;

		final String configOld=settings.getConfig();
		final String configNew=checkInputFile(configOld,modelFile);
		if (!configOld.equals(configNew)) {
			settings.setConfig(configNew);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Prüft die Pfadangaben in Bezug auf eine bestimmte Station
	 * @param element	Zu prüfende Station
	 * @param modelFile	Dateiname der Modelldatei dessen Pfad für Anpassungen verwendet wird
	 * @return	Liefert <code>true</code>, wenn etwas verändert wurde
	 */
	private static boolean checkElement(final ModelElement element, final File modelFile) {
		boolean changed=false;

		if (element instanceof ElementWithInputFile) {
			final ElementWithInputFile input=(ElementWithInputFile)element;
			final String inputFileNameOld=input.getInputFile();
			final String inputFileNameNew=checkInputFile(inputFileNameOld,modelFile);
			if (!inputFileNameOld.equals(inputFileNameNew)) {
				input.setInputFile(inputFileNameNew);
				changed=true;
			}
		}
		if (element instanceof ElementWithOutputFile) {
			final ElementWithOutputFile output=(ElementWithOutputFile)element;
			final String outputFileNameOld=output.getOutputFile();
			final String outputFileNameNew=checkOutputFile(outputFileNameOld,modelFile);
			if (!outputFileNameOld.equals(outputFileNameNew)) {
				output.setOutputFile(outputFileNameNew);
				changed=true;
			}
		}
		if (element instanceof ElementWithDB) {
			final ElementWithDB dbElement=(ElementWithDB)element;
			if (checkDB(dbElement.getDb(),modelFile)) changed=true;
		}

		return changed;
	}

	/**
	 * Überprüft die Eingabedatei auf Existenz und ändert ggf. den Pfad gemäß dem Modelldateipfad
	 * @param fileName	Pfad und Name der Eingabedatei die geprüft werden soll
	 * @param modelFile	Pfad und Name der Modelldatei (als Pfad-Basis)
	 * @return	Wurden keine Anpassungen vorgenommen, so wird einfach <code>fileName</code> ausgegeben, sonst der angepasste Pfad.
	 */
	public static String checkInputFile(final String fileName, final File modelFile) {
		if (fileName==null || fileName.isEmpty()) return "";
		if (modelFile==null) return fileName;

		final File file=new File(fileName);
		if (file.isFile()) return fileName;

		final File newPath=modelFile.getParentFile();
		if (newPath==null) return fileName;

		final File newFile=new File(newPath,file.getName());
		if (newFile.isFile()) return newFile.toString();

		return fileName;
	}

	/**
	 * Überprüft die Ausgabedatei auf einen existierenden Pfad und ändert ggf. den Pfad gemäß dem Modelldateipfad
	 * @param fileName	Pfad und Name der Ausgabedatei die geprüft werden soll
	 * @param modelFile	Pfad und Name der Modelldatei (als Pfad-Basis)
	 * @return	Wurden keine Anpassungen vorgenommen, so wird einfach <code>fileName</code> ausgegeben, sonst der angepasste Pfad.
	 */
	public static String checkOutputFile(final String fileName, final File modelFile) {
		if (fileName==null || fileName.isEmpty()) return "";
		if (modelFile==null) return fileName;

		final File file=new File(fileName);
		final File oldPath=file.getParentFile();
		if (oldPath==null) return fileName;
		if (oldPath.isDirectory()) return fileName;

		final File newPath=modelFile.getParentFile();
		if (newPath==null) return fileName;

		final File newFile=new File(newPath,file.getName());
		return newFile.toString();
	}

	/**
	 * Überprüft das Eingabeverzeichnis auf Existenz und ändert ggf. den Pfad gemäß dem Modelldateipfad
	 * @param folderName	Pfad und Name des Eingabeverzeichnisses das geprüft werden soll
	 * @param modelFile	Pfad und Name der Modelldatei (als Pfad-Basis)
	 * @return	Wurden keine Anpassungen vorgenommen, so wird einfach <code>folderName</code> ausgegeben, sonst der angepasste Pfad.
	 */
	public static String checkInputFolder(final String folderName, final File modelFile) {
		if (folderName==null || folderName.isEmpty()) return "";
		if (modelFile==null) return folderName;

		/* Ist das Verzeichnis bereits ok? */
		final File folder=new File(folderName);
		if (folder.isDirectory()) return folderName;

		/* Kann ein Basisverzeichnis ermittelt werden? */
		final File newPath=modelFile.getParentFile();
		if (newPath==null) return folderName;

		/* Verzeichnisname direkt im Basisverzeichnis vorhanden? */
		final File newFolder=new File(newPath,folder.getName());
		if (newFolder.isDirectory()) return newFolder.toString();

		/* Andere Alternative ermitteln */
		File dir=null;
		final File[] files=newPath.listFiles();
		if (files!=null) for (File file: files) if (file.isDirectory()) {
			if (dir!=null) return folderName;
			dir=file;
		}
		if (dir!=null) return dir.toString();

		return folderName;
	}
}

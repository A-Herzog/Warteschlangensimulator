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
	 */
	public static void checkFilePaths(final EditModel model, final File modelFile) {
		for (ModelElement element1: model.surface.getElements()) {
			checkElement(element1,modelFile);
			if (element1 instanceof ModelElementSub) for (ModelElement element2: ((ModelElementSub)element1).getSubSurface().getElements()) {
				checkElement(element2,modelFile);
			}
		}
	}

	/**
	 * Prüft eine Datenbankeinstellung in Bezug auf die Pfade für die Datenbankdatei
	 * @param settings	Zu prüfende Einstellung
	 * @param modelFile	Dateiname der Modelldatei dessen Pfad für Anpassungen verwendet wird
	 */
	private static void checkDB(final DBSettings settings, final File modelFile) {
		final DBConnectSetup setup=DBConnectSetups.getByType(settings.getType());
		if (setup==null) return;
		if (!setup.selectSource.isFile) return;

		final String configOld=settings.getConfig();
		final String configNew=checkInputFile(configOld,modelFile);
		if (!configOld.equals(configNew)) settings.setConfig(configNew);
	}

	/**
	 * Prüft die Pfadangaben in Bezug auf eine bestimmte Station
	 * @param element	Zu prüfende Station
	 * @param modelFile	Dateiname der Modelldatei dessen Pfad für Anpassungen verwendet wird
	 */
	private static void checkElement(final ModelElement element, final File modelFile) {
		if (element instanceof ElementWithInputFile) {
			final ElementWithInputFile input=(ElementWithInputFile)element;
			final String inputFileNameOld=input.getInputFile();
			final String inputFileNameNew=checkInputFile(inputFileNameOld,modelFile);
			if (!inputFileNameOld.equals(inputFileNameNew)) input.setInputFile(inputFileNameNew);
		}
		if (element instanceof ElementWithOutputFile) {
			final ElementWithOutputFile output=(ElementWithOutputFile)element;
			final String outputFileNameOld=output.getOutputFile();
			final String outputFileNameNew=checkOutputFile(outputFileNameOld,modelFile);
			if (!outputFileNameOld.equals(outputFileNameNew)) output.setOutputFile(outputFileNameNew);
		}
		if (element instanceof ElementWithDB) {
			final ElementWithDB dbElement=(ElementWithDB)element;
			checkDB(dbElement.getDb(),modelFile);
		}
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
}

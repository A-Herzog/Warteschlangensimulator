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
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import language.Language;
import mathtools.distribution.swing.CommonVariables;
import simulator.editmodel.EditModel;
import simulator.statistics.Statistics;
import tools.SetupData;
import ui.dialogs.InfoDialog;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ElementWithInputFile;

/**
 * Fasst alle aktuellen Programmdaten zusammen und speichert
 * diese in einer zip-Datei.
 * @author Alexander Herzog
 */
public class ProblemReporter {
	/** Datei in die die Ausgabe erfolgen soll */
	private File file;

	/**
	 * Konstruktor der Klasse
	 * @param file	Datei in die die Ausgabe erfolgen soll
	 */
	public ProblemReporter(final File file) {
		this.file=file;
	}

	/**
	 * Fügt die Daten in einer Zip-Datei ein.
	 * @param zipOutput	Zip-Datei
	 * @return	Gibt an, ob die Ausgabe erfolgreich war.
	 * @throws IOException	Wird ausgelöst, wenn die Ausgabe in die Zip-Datei fehlgeschlagen ist.
	 */
	private boolean processRecords(final ZipOutputStream zipOutput) throws IOException {
		/* Setup */
		zipOutput.putNextEntry(new ZipEntry("setup.xml"));
		SetupData.getSetup().saveToStream(zipOutput);

		/* Letzter Fehler */
		if (SetupData.getSetup().lastError!=null) {
			zipOutput.putNextEntry(new ZipEntry("error.txt"));
			zipOutput.write(SetupData.getSetup().lastError.getBytes(StandardCharsets.UTF_8));
		}

		/* Systeminformationen */
		zipOutput.putNextEntry(new ZipEntry("system.txt"));
		zipOutput.write(String.join("\n",InfoDialog.getSystemInfo().toArray(new String[0])).getBytes(StandardCharsets.UTF_8));

		/* Programmversion */
		zipOutput.putNextEntry(new ZipEntry("version.txt"));
		zipOutput.write(MainPanel.VERSION.getBytes(StandardCharsets.UTF_8));

		/* Java-Version */
		zipOutput.putNextEntry(new ZipEntry("java.txt"));
		final StringBuilder java=new StringBuilder();
		for (Map.Entry<Object,Object> entry: System.getProperties().entrySet()) {
			java.append(entry.getKey()+"="+entry.getValue());
			java.append('\n');
		}
		zipOutput.write(java.toString().getBytes(StandardCharsets.UTF_8));

		/* Umgebungsvariablen */
		zipOutput.putNextEntry(new ZipEntry("environment.txt"));
		final StringBuilder env=new StringBuilder();
		for (Map.Entry<String,String> entry: System.getenv().entrySet()) {
			env.append(entry.getKey()+"="+entry.getValue());
			env.append('\n');
		}
		zipOutput.write(env.toString().getBytes(StandardCharsets.UTF_8));

		/* Für alle offenen Fenster... */
		int countFrame=0;
		for (MainFrame frame: ReloadManager.getMainFrames()) {
			countFrame++;
			final EditModel model=frame.getModel();

			/* Aktuelles Modell */
			zipOutput.putNextEntry(new ZipEntry(String.format("model%d.xml",countFrame)));
			model.saveToStream(zipOutput);

			/* Alle verwendeten Eingabedateien im aktuellen Modell */
			final File[] inputFiles=getInputFiles(model);
			if (inputFiles.length>0) {
				int countInput=0;
				for (File inputFile: inputFiles) {
					countInput++;
					zipOutput.putNextEntry(new ZipEntry(String.format("model%d-input%d.dat",countFrame,countInput)));
					copyFileToOutputStream(inputFile,zipOutput);
				}
				zipOutput.putNextEntry(new ZipEntry(String.format("model%d-input.txt",countFrame)));
				writeInputFileSummary(countFrame,inputFiles,zipOutput);
			}

			/* Aktuelle Statistikdaten */
			final Statistics statistics=frame.getStatistics();
			if (statistics!=null) {
				zipOutput.putNextEntry(new ZipEntry(String.format("statistics%d.xml",countFrame)));
				statistics.saveToStream(zipOutput);
			}

			/* Screenshot des Fensters */
			zipOutput.putNextEntry(new ZipEntry(String.format("window%d.png",countFrame)));
			final Dimension d=frame.getSize();
			final BufferedImage image=new BufferedImage(d.width,d.height,BufferedImage.TYPE_INT_RGB);
			frame.paintAll(image.getGraphics());
			ImageIO.write(image,"png",zipOutput);
		}

		return true;
	}

	/**
	 * Erstellt eine Liste mit allen Eingabedateien des Modells.
	 * @param model	Modell aus dem die Eingabedateien ausgelesen werden sollen
	 * @return	Liste mit Eingabedateien (kann leer sein, ist aber nie <code>null</code>)
	 */
	private File[] getInputFiles(final EditModel model) {
		final List<File> files=new ArrayList<>();
		for (ModelElement element: model.surface.getElementsIncludingSubModels()) if (element instanceof ElementWithInputFile) {
			final File file=new File(((ElementWithInputFile)element).getInputFile());
			if (file.isFile()) files.add(file);
		}
		return files.toArray(new File[0]);
	}

	/**
	 * Kopiert die Daten aus einer Datei in einen zip-Ausgabe-Stream
	 * @param inputFile	Eingabedatei
	 * @param outputStream	zip-Ausgabe-Stream
	 * @return	Liefert im Erfolgsfall <code>true</code>
	 */
	private boolean copyFileToOutputStream(final File inputFile, final ZipOutputStream outputStream) {
		try(final FileInputStream inputStream=new FileInputStream(inputFile)) {
			final byte[] buffer=new byte[1024*1024];
			int len=inputStream.read(buffer);
			while (len!=-1) {
				outputStream.write(buffer,0,len);
				len=inputStream.read(buffer);
			}
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * Schreibt eine Zuordnungsübersicht zwischen den internen Namen für die Eingabedateien und den realen Namen in den zip-Ausgabe-Stream
	 * @param modelNr	Nummer des Modells (bzw. des Fensters)
	 * @param inputFiles	Liste der Eingabedateien
	 * @param outputStream	zip-Ausgabe-Stream
	 * @return	Liefert im Erfolgsfall <code>true</code>
	 */
	private boolean writeInputFileSummary(final int modelNr, final File[] inputFiles, final ZipOutputStream outputStream) {
		final StringBuilder info=new StringBuilder();
		for (int i=0;i<inputFiles.length;i++) {
			info.append(String.format("model%d-input%d.dat",modelNr,i+1));
			info.append(" => ");
			info.append(inputFiles[i]);
			info.append("\n");
		}
		try {
			outputStream.write(info.toString().getBytes(StandardCharsets.UTF_8));
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * Führt die Ausgabe durch.
	 * @return	Gibt an, ob die Ausgabe erfolgreich war.
	 */
	public boolean process() {
		try (BufferedOutputStream fileOutput=new BufferedOutputStream(new FileOutputStream(file));) {
			try (ZipOutputStream zipOutput=new ZipOutputStream(fileOutput)) {
				if (!processRecords(zipOutput)) return false;
			}
		} catch (IOException e) {return false;}
		return true;
	}

	/**
	 * Zeigt einen Dateiauswahldialog zum Speichern der zip-Ausgabe an.
	 * @param owner	Übergeordnetes Element
	 * @return	Gewählte Ausgabedatei oder <code>null</code>, wenn der Dialog abgebrochen wurde
	 */
	public static File selectOutputFile(final Component owner) {
		final JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		fc.setDialogTitle(Language.tr("ProblemReporter.Dialog.Title"));

		final FileFilter zip=new FileNameExtensionFilter(Language.tr("ProblemReporter.Dialog.FileTypeZip")+" (*.zip)","zip");

		fc.addChoosableFileFilter(zip);
		fc.setFileFilter(zip);
		fc.setAcceptAllFileFilterUsed(false);

		if (fc.showSaveDialog(owner)!=JFileChooser.APPROVE_OPTION) return null;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();

		if (file.getName().indexOf('.')<0) {
			if (fc.getFileFilter()==zip) file=new File(file.getAbsoluteFile()+".zip");
		}
		return file;
	}
}

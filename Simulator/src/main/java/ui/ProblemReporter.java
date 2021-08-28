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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
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
	/**
	 * Welche Informationen sollen in den Bericht aufgenommen werden?
	 */
	public enum ReportItem {
		/** Programmeinstellungen */
		SETUP(()->Language.tr("ProblemReporter.Item.Setup")),
		/** Informationen zum zuletzt aufgetretenen Fehler */
		LAST_ERROR(()->Language.tr("ProblemReporter.Item.LastError"),()->{final String error=SetupData.getSetup().lastError; return (error!=null && !error.trim().isEmpty());}),
		/** Systeminformationen */
		SYSTEM_INFO(()->Language.tr("ProblemReporter.Item.SystemInfo")),
		/** Programminformationen */
		PROGRAM_INFO(()->Language.tr("ProblemReporter.Item.ProgramInfo")),
		/** Das aktuelle Modell und ggf. die aktuellen Statistikdaten */
		MODEL(()->Language.tr("ProblemReporter.Item.Model")),
		/** (Wenn vorhanden:) Zusätzliche externe Quellen für das Modell */
		MODEL_SOURCES(()->Language.tr("ProblemReporter.Item.ModelSources"),()->hasInputFilesAllModels()),
		/** (Wenn vorhanden:) Dateien im Plugins-Ordner */
		MODEL_PLUGINS(()->Language.tr("ProblemReporter.Item.ModelPlugins"),()->hasPluginFiles());

		/**
		 * Getter für den Namen des Informationselements
		 * @see #getName()
		 */
		private final Supplier<String> nameGetter;

		/**
		 * Prüft, ob entsprechende Informationen vorhanden sind
		 * @see #isAvailable()
		 */
		private final BooleanSupplier testAvailable;

		/**
		 * Konstruktor der Enum
		 * @param nameGetter	Getter für den Namen des Informationselements
		 */
		ReportItem(final Supplier<String> nameGetter) {
			this(nameGetter,()->true);
		}

		/**
		 * Konstruktor der Enum
		 * @param nameGetter	Getter für den Namen des Informationselements
		 * @param testAvailable	Prüft, ob entsprechende Informationen vorhanden sind
		 */
		ReportItem(final Supplier<String> nameGetter, final BooleanSupplier testAvailable) {
			this.nameGetter=nameGetter;
			this.testAvailable=testAvailable;
		}

		/**
		 * Liefert den Namen des Informationselements.
		 * @return	Namen des Informationselements
		 */
		public String getName() {
			return nameGetter.get();
		}

		/**
		 * Prüft, ob die entsprechenden Informationen vorhanden sind.
		 * @return	Liefert <code>true</code>, wenn passende Daten vorhanden sind
		 */
		public boolean isAvailable() {
			return testAvailable.getAsBoolean();
		}
	}

	/**
	 * Stellt eine Menge mit allen verfügbaren Ausgabe-Informationen zusammen.
	 * @see ReportItem
	 */
	public static Set<ReportItem> FULL_REPORT=new HashSet<>(Arrays.asList(ReportItem.values()));

	/**
	 * Datei in die die Ausgabe erfolgen soll
	 */
	private File file;

	/**
	 * Welche Informationen sollen in den Bericht aufgenommen werden?
	 * @see ReportItem
	 */
	private final Set<ReportItem> reportItems;

	/**
	 * Konstruktor der Klasse
	 * @param file	Datei in die die Ausgabe erfolgen soll
	 * @param reportItems	Welche Informationen sollen in den Bericht aufgenommen werden?
	 * @see ReportItem
	 */
	public ProblemReporter(final File file, final Set<ReportItem> reportItems) {
		this.file=file;
		if (reportItems==null) this.reportItems=FULL_REPORT; else this.reportItems=new HashSet<>(reportItems);
	}

	/**
	 * Konstruktor der Klasse
	 * @param file	Datei in die die Ausgabe erfolgen soll
	 */
	public ProblemReporter(final File file) {
		this(file,FULL_REPORT);
	}

	/**
	 * Prüft, ob in einem Modell ein gültiger Plugins-Ordner eingetragen ist.
	 * @param model	Zu prüfendes Modell
	 * @return	 Liefer <code>true</code>, wenn ein Plugins-Ordner in dem Modell registriert ist und dieser auch existiert.
	 */
	private static boolean testPluginFiles(final EditModel model) {
		return (model.pluginsFolder!=null && !model.pluginsFolder.trim().isEmpty() && new File(model.pluginsFolder).isDirectory());
	}

	/**
	 * Ist in mindestens einem der geladenen Modell ein Plugins-Ordner konfiguriert?
	 * @return	Liefert <code>true</code>, wenn in mindestens einem der geladenen Modell ein Plugins-Ordner konfiguriert ist
	 */
	private static boolean hasPluginFiles() {
		for (MainFrame frame: ReloadManager.getMainFrames()) {
			final EditModel model=frame.getModel();
			if (testPluginFiles(model)) return true;
		}
		return false;
	}

	/**
	 * Fügt die Daten in einer Zip-Datei ein.
	 * @param zipOutput	Zip-Datei
	 * @return	Gibt an, ob die Ausgabe erfolgreich war.
	 * @throws IOException	Wird ausgelöst, wenn die Ausgabe in die Zip-Datei fehlgeschlagen ist.
	 */
	private boolean processRecords(final ZipOutputStream zipOutput) throws IOException {
		if (reportItems.contains(ReportItem.SETUP)) {
			/* Setup */
			zipOutput.putNextEntry(new ZipEntry("setup.xml"));
			SetupData.getSetup().saveToStream(zipOutput);
		}

		if (reportItems.contains(ReportItem.LAST_ERROR)) {
			/* Letzter Fehler */
			if (SetupData.getSetup().lastError!=null) {
				zipOutput.putNextEntry(new ZipEntry("error.txt"));
				zipOutput.write(SetupData.getSetup().lastError.getBytes(StandardCharsets.UTF_8));
			}
		}

		if (reportItems.contains(ReportItem.SYSTEM_INFO)) {
			/* Systeminformationen */
			zipOutput.putNextEntry(new ZipEntry("system.txt"));
			zipOutput.write(String.join("\n",InfoDialog.getSystemInfo().toArray(new String[0])).getBytes(StandardCharsets.UTF_8));

			/* Umgebungsvariablen */
			zipOutput.putNextEntry(new ZipEntry("environment.txt"));
			final StringBuilder env=new StringBuilder();
			for (Map.Entry<String,String> entry: System.getenv().entrySet()) {
				env.append(entry.getKey()+"="+entry.getValue());
				env.append('\n');
			}
			zipOutput.write(env.toString().getBytes(StandardCharsets.UTF_8));

			/* Java-Version */
			zipOutput.putNextEntry(new ZipEntry("java.txt"));
			final StringBuilder java=new StringBuilder();
			for (Map.Entry<Object,Object> entry: System.getProperties().entrySet()) {
				java.append(entry.getKey()+"="+entry.getValue());
				java.append('\n');
			}
			zipOutput.write(java.toString().getBytes(StandardCharsets.UTF_8));
		}

		if (reportItems.contains(ReportItem.PROGRAM_INFO)) {
			/* Programmversion */
			zipOutput.putNextEntry(new ZipEntry("version.txt"));
			zipOutput.write(MainPanel.VERSION.getBytes(StandardCharsets.UTF_8));

			/* Installationsort und -art */
			zipOutput.putNextEntry(new ZipEntry("installation-path.txt"));
			zipOutput.write(SetupData.getProgramFolder().toString().getBytes(StandardCharsets.UTF_8));

			zipOutput.putNextEntry(new ZipEntry("installation-mode.txt"));
			final String mode;
			switch (SetupData.getOperationMode()) {
			case PROGRAM_FOLDER_MODE: mode=Language.tr("InfoDialog.InstallMode.ProgramFolder"); break;
			case USER_FOLDER_MODE: mode=Language.tr("InfoDialog.InstallMode.UserFolder"); break;
			case PORTABLE_MODE: mode=Language.tr("InfoDialog.InstallMode.Portable"); break;
			default: mode=Language.tr("InfoDialog.InstallMode.Unknown"); break;
			}
			zipOutput.write(mode.getBytes(StandardCharsets.UTF_8));
		}

		if (reportItems.contains(ReportItem.MODEL)) {
			/* Für alle offenen Fenster... */
			int countFrame=0;
			for (MainFrame frame: ReloadManager.getMainFrames()) {
				countFrame++;
				final EditModel model=frame.getModel();

				/* Aktuelles Modell */
				zipOutput.putNextEntry(new ZipEntry(String.format("model%d.xml",countFrame)));
				model.saveToStream(zipOutput);

				if (reportItems.contains(ReportItem.MODEL_SOURCES)) {
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
				}

				if (reportItems.contains(ReportItem.MODEL_PLUGINS)) {
					if (testPluginFiles(model)) {
						copyFilesToZip(new File(model.pluginsFolder),zipOutput,new String[] {String.format("plugins%d",countFrame)});
					}
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
		}

		return true;
	}

	/**
	 * Erstellt eine Liste mit allen Eingabedateien des Modells.
	 * @param model	Modell aus dem die Eingabedateien ausgelesen werden sollen
	 * @return	Liste mit Eingabedateien (kann leer sein, ist aber nie <code>null</code>)
	 */
	private static File[] getInputFiles(final EditModel model) {
		final List<File> files=new ArrayList<>();
		for (ModelElement element: model.surface.getElementsIncludingSubModels()) if (element instanceof ElementWithInputFile) {
			final File file=new File(((ElementWithInputFile)element).getInputFile());
			if (file.isFile()) files.add(file);
		}
		return files.toArray(new File[0]);
	}

	/**
	 * Verwendet mindestens eines der geladenen Modell externe Datenquellen?
	 * @return	Liefert <code>true</code>, wenn mindestens eines der geladenen Modell externe Datenquellen verwendet
	 */
	private static boolean hasInputFilesAllModels() {
		for (MainFrame frame: ReloadManager.getMainFrames()) {
			final EditModel model=frame.getModel();
			if (getInputFiles(model).length>0) return true;
		}
		return false;
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
	 * Kopiert alle Dateien aus einem Verzeichnis (ink. seiner Unterverzeichnisse) in eine zip-Datei
	 * @param sourceFolder	Ausgangsverzeichnis
	 * @param outputStream	zip-Ausgabe-Stream
	 * @param parentFolders	Repräsentation dieses Verzeichnisses in der zip-Datei
	 * @throws IOException Wird ausgelöst, wenn es zu einem Fehler beim Hinzufügen eines zip-Datensatzes kommt
	 */
	private void copyFilesToZip(final File sourceFolder, final ZipOutputStream outputStream, final String[] parentFolders) throws IOException {
		final File[] files=sourceFolder.listFiles();
		if (files==null) return;
		for (File file: files) {
			final String[] path=Arrays.copyOf(parentFolders,parentFolders.length+1);
			path[path.length-1]=file.getName();
			if (file.isFile()) {
				outputStream.putNextEntry(new ZipEntry(String.join("/",path)));
				copyFileToOutputStream(file,outputStream);
				continue;
			}
			if (file.isDirectory()) {
				copyFilesToZip(file,outputStream,path);
				continue;
			}
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
		return selectOutputFile(owner,null);
	}

	/**
	 * Zeigt einen Dateiauswahldialog zum Speichern der zip-Ausgabe an.
	 * @param owner	Übergeordnetes Element
	 * @param lastFile	Zuvor gewählte Datei (zur Festlegung des initial anzuzeigenden Verzeichnisses)
	 * @return	Gewählte Ausgabedatei oder <code>null</code>, wenn der Dialog abgebrochen wurde
	 */
	public static File selectOutputFile(final Component owner, final File lastFile) {
		final JFileChooser fc;
		if (lastFile==null || lastFile.getParentFile()==null || !lastFile.getParentFile().isDirectory()) {
			fc=new JFileChooser();
			CommonVariables.initialDirectoryToJFileChooser(fc);
		} else {
			fc=new JFileChooser(lastFile.getParent());
		}

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

/**
 * Copyright 2021 Alexander Herzog
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
package ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.swing.CommonVariables;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.SetupData;
import ui.help.Help;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Erstellt eine Kopie der Warteschlangensimulator-Installation in einem anderen Verzeichnis
 * @author Alexander Herzog
 */
public class CopyInstallationDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-5273096656223867013L;

	/**
	 * Zu kopierende Dateien
	 */
	private final List<CopyItem> copyItems;

	/**
	 * Größe aller zu kopierender Dateien zusammen
	 */
	private final long copySize;

	/**
	 * Eingabefeld für das Zielverzeichnis
	 */
	private final JTextField destinationFolderInput;

	/**
	 * Fortschrittsbalken beim Kopieren
	 */
	private final JProgressBar progress;

	/**
	 * Wird in {@link #checkData()} verwendet, um zu prüfen, ob der
	 * Kopier-Thread erst noch gestartet werden muss (<code>false</code>)
	 * oder ob er bereits fertig ist (<code>true</code>).
	 */
	private boolean operationDone=false;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 */
	@SuppressWarnings("unchecked")
	public CopyInstallationDialog(final Component owner) {
		super(owner,Language.tr("CopyInstallation.Title"));

		/* Zu kopierende Daten zusammenstellen */
		copyItems=(List<CopyItem>)WaitDialog.workObject(owner,()->getCopyItemList(),WaitDialog.Mode.PROCESS_DATA);
		copySize=copyItems.stream().mapToLong(item->item.size).sum();

		/* GUI */
		final JPanel all=createGUI(()->Help.topicModal(this,"CopyInstallationDialog"));
		all.setLayout(new BorderLayout());
		final JPanel content=new JPanel();
		InfoPanel.addTopPanel(all,InfoPanel.globalCopyInstallation);
		all.add(content,BorderLayout.CENTER);
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		JPanel line;

		/* Zielverzeichnis */
		final Object[] data=ModelElementBaseDialog.getInputPanel(Language.tr("CopyInstallation.DestinationFolder")+":","");
		content.add(line=(JPanel)data[0]);
		destinationFolderInput=(JTextField)data[1];
		final JButton button=new JButton(Images.GENERAL_SELECT_FOLDER.getIcon());
		line.add(button,BorderLayout.EAST);
		line.setMaximumSize(new Dimension(10000,25));
		button.setToolTipText(Language.tr("CopyInstallation.DestinationFolder.Select"));
		button.addActionListener(e->commandSelectDestinationFolder());

		/* Benötigter Speicherplatz im Zielverzeichnis */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel(String.format(Language.tr("CopyInstallation.Info"),NumberTools.formatNumber(copySize/1024.0/1024.0))));

		/* Fortschrittsbalken */
		content.add(progress=new JProgressBar(SwingConstants.HORIZONTAL));
		progress.setMinimum(0);
		progress.setMaximum((int)(copySize/1024));
		progress.setStringPainted(true);
		progress.setValue(0);

		/* Dialog starten */
		setMinSizeRespectingScreensize(650,0);
		pack();
		SwingUtilities.invokeLater(()->{
			all.setPreferredSize(new Dimension(Math.min(getWidth(),600),all.getPreferredSize().height+50));
			pack();
			setLocationRelativeTo(owner);
		});
		setVisible(true);
	}

	/**
	 * Liefert eine Liste aller zu kopierender Dateien
	 * @return	Liste aller zu kopierender Dateien
	 */
	private List<CopyItem> getCopyItemList() {
		return getCopyItemList(SetupData.getProgramFolder(),SetupData.getProgramFolder());
	}

	/**
	 * Liefert eine Liste aller zu kopierender Dateien aus einem Verzeichnis und seinen Unterverzeichnissen
	 * @param baseFolder	Basisverzeichnis
	 * @param folder	Aktuelles Verzeichnis
	 * @return	Liste der Dateien im aktuellen Verzeichnis und seinen Unterverzeichnissen
	 */
	private List<CopyItem> getCopyItemList(final File baseFolder, final File folder) {
		final List<CopyItem> list=new ArrayList<>();

		final File[] files=folder.listFiles();
		if (files!=null) for (File file: files) {
			if (file.isFile()) {
				list.add(new CopyItem(baseFolder,file));
				continue;
			}
			if (file.isDirectory()) {
				list.addAll(getCopyItemList(baseFolder,file));
				continue;
			}
		}

		return list;
	}

	/**
	 * Zielverzeichnis auswählen
	 */
	private void commandSelectDestinationFolder() {
		final JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		final String oldFolder=destinationFolderInput.getText().trim();
		if (!oldFolder.isEmpty()) fc.setCurrentDirectory(new File(oldFolder));
		fc.setDialogTitle(Language.tr("CopyInstallation.DestinationFolder.Select"));
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (fc.showSaveDialog(owner)!=JFileChooser.APPROVE_OPTION) return;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		destinationFolderInput.setText(fc.getSelectedFile().toString());
	}

	@Override
	protected boolean checkData() {
		if (operationDone) return true;

		/* Eingaben prüfen */
		final String destinationFolderString=destinationFolderInput.getText().trim();

		if (destinationFolderString.isEmpty()) {
			MsgBox.error(this,Language.tr("CopyInstallation.DestinationFolder.ErrorTitle"),Language.tr("CopyInstallation.DestinationFolder.ErrorInfo"));
			return false;
		}

		final File destinationFolder=new File(destinationFolderString);
		if (!destinationFolder.isDirectory()) {
			destinationFolder.mkdirs();
			if (!destinationFolder.isDirectory()) {
				MsgBox.error(this,Language.tr("CopyInstallation.DestinationFolder.ErrorNotExistantTitle"),String.format(Language.tr("CopyInstallation.DestinationFolder.ErrorNotExistantInfo"),destinationFolderString));
				return false;
			}
		}

		final String[] filesList=destinationFolder.list();
		if (filesList!=null && filesList.length>0) {
			if (!MsgBox.confirm(this,Language.tr("CopyInstallation.DestinationFolder.ConfirmOverwriteTitle"),String.format(Language.tr("CopyInstallation.DestinationFolder.ConfirmOverwriteInfo"),destinationFolderString),Language.tr("CopyInstallation.DestinationFolder.ConfirmOverwriteInfoYes"),Language.tr("CopyInstallation.DestinationFolder.ConfirmOverwriteInfoNo"))) return false;
		}

		/* Verarbeitung durchführen */
		setEnabled(false);
		final Thread thread=new Thread(()->{
			process();
			operationDone=true;
			close(BaseDialog.CLOSED_BY_OK);
		});
		thread.start();

		return false;
	}

	/**
	 * Führt die eigentliche Kopierarbeit aus
	 */
	private void process() {
		final File destinationFolder=new File(destinationFolderInput.getText().trim());

		/* Dateien kopieren */
		final String sumString=NumberTools.formatNumber(copySize/1024.0/1024.0,0);
		long copiedBytes=0;
		for (CopyItem item: copyItems) {
			if (!item.copy(destinationFolder)) {
				try {
					SwingUtilities.invokeAndWait(()->{
						MsgBox.error(this,Language.tr("CopyInstallation.ErrorTitle"),String.format(Language.tr("CopyInstallation.ErrorInfo"),destinationFolder.toString()));
					});
				} catch (InvocationTargetException|InterruptedException ex) {}
				return;
			}
			copiedBytes+=item.size;
			final int copiedKBytesFinal=(int)(copiedBytes/1024);
			SwingUtilities.invokeLater(()->{
				progress.setValue(copiedKBytesFinal);
				progress.setString(String.format(Language.tr("CopyInstallation.Progress"),NumberTools.formatNumber(copiedKBytesFinal/1024.0,0),sumString));
				progress.repaint();
			});
		}

		/* Setupdatei kopieren */
		final File destinationSetupFile=new File(destinationFolder,SetupData.SETUP_FILE_NAME);
		try (FileOutputStream output=new FileOutputStream(destinationSetupFile)) {
			SetupData.getSetup().saveToStream(output);
		} catch (IOException e) {
			try {
				SwingUtilities.invokeAndWait(()->{
					MsgBox.error(this,Language.tr("CopyInstallation.ErrorTitle"),String.format(Language.tr("CopyInstallation.ErrorInfoSetup"),destinationSetupFile.toString()));
				});
			} catch (InvocationTargetException|InterruptedException ex) {}
			return;
		}

		try {
			SwingUtilities.invokeAndWait(()->{
				MsgBox.info(this,Language.tr("CopyInstallation.SuccessTitle"),String.format(Language.tr("CopyInstallation.SuccessInfo"),destinationFolder.toString()));
			});
		} catch (InvocationTargetException|InterruptedException ex) {}
	}

	/**
	 * Datensatz für eine einzelne zu kopierende Datei
	 * @see CopyInstallationDialog#copyItems
	 */
	private static class CopyItem {
		/**
		 * Datei auf die sich dieser Datensatz beziehen soll
		 */
		private final File file;

		/**
		 * Name der Datei (ohne Pfad)
		 */
		public final String name;

		/**
		 * Unterordner relativ vom Basisordner aus
		 * (kann leer sein, ist aber nie <code>null</code>)
		 */
		private final String[] folder;

		/**
		 * Größe der Datei
		 */
		public final long size;

		/**
		 * Konstruktor der Klasse
		 * @param baseFolder	Basisordner von dem aus der Pfad relativ gespeichert werden soll
		 * @param file	Datei auf die sich dieser Datensatz beziehen soll
		 */
		public CopyItem(final File baseFolder, final File file) {
			this.file=file;
			name=file.getName();
			String baseName;
			try {
				baseName=baseFolder.getCanonicalPath();
			} catch (IOException e) {
				baseName=baseFolder.toString();
			}
			String filePath;
			try {
				filePath=file.getParentFile().getCanonicalPath();
			} catch (IOException e) {
				filePath=file.getParent();
			}
			if (!baseName.endsWith(File.separator)) baseName+=File.separator;
			if (!filePath.endsWith(File.separator)) filePath+=File.separator;
			if (baseName.length()==filePath.length()) {
				folder=new String[0];
			} else {
				filePath=filePath.substring(baseName.length());
				filePath=filePath.substring(0,filePath.length()-1);
				if (File.separator.equals("\\")) {
					folder=filePath.split("\\\\");
				} else {
					folder=filePath.split("/");
				}
			}

			size=file.length();
		}

		/**
		 * Bestimmt den Zielordner für eine Kopieraktion aus Basis-Zielverzeichnis und relativem Pfad der Datei.
		 * @param destinationBaseFolder	Basis-Zielverzeichnis
		 * @return	Vollständiger Pfad (ohne Dateiname) der Zieldatei
		 */
		private File buildDestinationFolder(final File destinationBaseFolder) {
			final StringBuilder result=new StringBuilder();
			result.append(destinationBaseFolder);
			if (!destinationBaseFolder.toString().endsWith(File.separator)) result.append(File.separator);
			for (String folderPart: folder) {
				result.append(folderPart);
				result.append(File.separator);
			}
			return new File(result.toString());
		}

		/**
		 * Kopiert die Datei in eine neue Verzeichnissstruktur
		 * @param destinationBaseFolder	Zielverzeichnis welches dem Basisverzeichnis der bisherigen Installation entsprechen soll
		 * @return	Liefert <code>true</code>, wenn die Datei erfolgreich kopiert werden konnte (und notwendigenfalls das Zielverzeichnis angelegt werden konnte)
		 */
		public boolean copy(final File destinationBaseFolder) {
			/* Zielverzeichnis wenn nötig anlegen */
			final File destinationFolder=buildDestinationFolder(destinationBaseFolder);
			if (!destinationFolder.isDirectory()) {
				if (!destinationFolder.mkdirs()) return false;
			}

			/* Datei kopieren */
			Path sourceFile=file.toPath();
			Path destinationFile=new File(destinationFolder,name).toPath();
			try {
				Files.copy(sourceFile,destinationFile,StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				return false;
			}
			return true;
		}
	}
}

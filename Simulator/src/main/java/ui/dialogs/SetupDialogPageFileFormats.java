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
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.Serializable;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.filechooser.FileSystemView;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.swing.CommonVariables;
import systemtools.MsgBox;
import tools.IconListCellRenderer;
import tools.SetupData;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;
import xml.XMLTools;

/**
 * Dialogseite "Dateiformate" im Programmeinstellungen-Dialog
 * @author Alexander Herzog
 * @see SetupData
 */
public class SetupDialogPageFileFormats extends SetupDialogPage {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=1678278783032085207L;

	/* Bereich: Vorgabe-Autorenname für neue Modelle */

	/** Autorenname für neue Modelle */
	private final JTextField defaultUserName;
	/** Autoren-E-Mail-Adresse für neue Modelle */
	private final JTextField defaultUserEMail;

	/* Bereich: Vorgabedateiformat beim Speichern */

	/** Standard-Speicherformat für Modelle */
	private final JComboBox<String> defaultSaveFormatModels;
	/** Standard-Speicherformat für Statistikdaten */
	private final JComboBox<String> defaultSaveFormatStatistics;
	/** Standard-Speicherformat für Parameterreiheneinstellungen */
	private final JComboBox<String> defaultSaveFormatParameterSeries;
	/** Standard-Speicherformat für Optimitereinstellungen */
	private final JComboBox<String> defaultSaveFormatOptimizerSetups;

	/* Bereich: Sicherheit */

	/** Backup-Dateien beim Speichern von Modellen anlegen? */
	private final JCheckBox useBackupFiles;

	/* Bereich: Excel-DDE-Verbindung */

	/** Benutzerdefinierter Zeilenbezeichner */
	private final JTextField excelRow;
	/** Benutzerdefinierter Spaltenbezeichner */
	private final JTextField excelCol;

	/* Bereich: Grafiken */

	/** Bildgröße beim Exportieren von Bildern */
	private final JTextField imageSize;
	/** Speichern von Schnappschüssen während der Animation im Nutzerverzeichnis */
	private final JRadioButton imagesAnimationHome;
	/** Speichern von Schnappschüssen während der Animation in manuell konfiguriertem Verzeichnis */
	private final JRadioButton imagesAnimationFolder;
	/** Verzeichnis zum Speichern von Schnappschüssen während der Animation */
	private final JTextField imagesAnimationFolderEdit;

	/* Bereich: Videos */

	/** Zeitangaben in aufgezeichnete Animationsvideos einbetten */
	private final JCheckBox paintTimeStamp;

	/**
	 * Konstruktor der Klasse
	 */
	public SetupDialogPageFileFormats() {
		JPanel line;
		Object[] data;
		JLabel label;
		JButton button;

		/*
		 * Bereich:
		 * Vorgabe-Autorenname für neue Modelle
		 */
		addHeading(Language.tr("SettingsDialog.Tabs.DefaultUserName"));

		/* Autorenname für neue Modelle */
		data=ModelElementBaseDialog.getInputPanel(Language.tr("SettingsDialog.Tabs.DefaultUserName.Name")+":","");
		add(line=(JPanel)data[0]);
		defaultUserName=(JTextField)data[1];
		line.add(button=new JButton(Language.tr("SettingsDialog.Tabs.DefaultUserName.Reset")),BorderLayout.EAST);
		button.setIcon(Images.MODELPROPERTIES_DESCRIPTION_SET_AUTHOR.getIcon());
		button.setToolTipText(String.format(Language.tr("SettingsDialog.Tabs.DefaultUserName.Reset.Info"),System.getProperty("user.name")));
		button.addActionListener(e->defaultUserName.setText(System.getProperty("user.name")));

		/* Autoren-E-Mail-Adresse für neue Modelle */
		data=ModelElementBaseDialog.getInputPanel(Language.tr("SettingsDialog.Tabs.DefaultUserName.EMail")+":","");
		add(line=(JPanel)data[0]);
		defaultUserEMail=(JTextField)data[1];

		/*
		 * Bereich:
		 * Vorgabedateiformat beim Speichern
		 */
		addHeading(Language.tr("SettingsDialog.Tabs.DefaultFormats"));

		/* Standard-Speicherformat für Modelle */
		line=addLine();
		line.add(label=new JLabel(Language.tr("SettingsDialog.Tabs.DefaultFormats.Models")+":"));
		line.add(defaultSaveFormatModels=new JComboBox<>(new String[]{
				Language.tr("SettingsDialog.Tabs.DefaultFormats.XML"),
				Language.tr("SettingsDialog.Tabs.DefaultFormats.JSON"),
				Language.tr("SettingsDialog.Tabs.DefaultFormats.ZIP_XML"),
				Language.tr("SettingsDialog.Tabs.DefaultFormats.TAR_XML"),
				Language.tr("SettingsDialog.Tabs.DefaultFormats.CRYPT"),

		}));
		defaultSaveFormatModels.setRenderer(new IconListCellRenderer(new Images[]{
				Images.SETUP_PAGE_FILE_FORMATS_XML,
				Images.SETUP_PAGE_FILE_FORMATS_JSON,
				Images.SETUP_PAGE_FILE_FORMATS_ZIP_XML,
				Images.SETUP_PAGE_FILE_FORMATS_TAR_XML,
				Images.SETUP_PAGE_FILE_FORMATS_CRYPT
		}));
		label.setLabelFor(defaultSaveFormatModels);

		/* Standard-Speicherformat für Statistikdaten */
		line=addLine();
		line.add(label=new JLabel(Language.tr("SettingsDialog.Tabs.DefaultFormats.Statistics")+":"));
		line.add(defaultSaveFormatStatistics=new JComboBox<>(new String[]{
				Language.tr("SettingsDialog.Tabs.DefaultFormats.XML"),
				Language.tr("SettingsDialog.Tabs.DefaultFormats.JSON"),
				Language.tr("SettingsDialog.Tabs.DefaultFormats.ZIP_XML"),
				Language.tr("SettingsDialog.Tabs.DefaultFormats.TAR_XML"),
				Language.tr("SettingsDialog.Tabs.DefaultFormats.CRYPT"),

		}));
		defaultSaveFormatStatistics.setRenderer(new IconListCellRenderer(new Images[]{
				Images.SETUP_PAGE_FILE_FORMATS_XML,
				Images.SETUP_PAGE_FILE_FORMATS_JSON,
				Images.SETUP_PAGE_FILE_FORMATS_ZIP_XML,
				Images.SETUP_PAGE_FILE_FORMATS_TAR_XML,
				Images.SETUP_PAGE_FILE_FORMATS_CRYPT
		}));
		label.setLabelFor(defaultSaveFormatStatistics);

		/* Standard-Speicherformat für Parameterreiheneinstellungen */
		line=addLine();
		line.add(label=new JLabel(Language.tr("SettingsDialog.Tabs.DefaultFormats.ParameterSeries")+":"));
		line.add(defaultSaveFormatParameterSeries=new JComboBox<>(new String[]{
				Language.tr("SettingsDialog.Tabs.DefaultFormats.XML"),
				Language.tr("SettingsDialog.Tabs.DefaultFormats.JSON"),
				Language.tr("SettingsDialog.Tabs.DefaultFormats.ZIP_XML"),
				Language.tr("SettingsDialog.Tabs.DefaultFormats.TAR_XML"),
				Language.tr("SettingsDialog.Tabs.DefaultFormats.CRYPT"),

		}));
		defaultSaveFormatParameterSeries.setRenderer(new IconListCellRenderer(new Images[]{
				Images.SETUP_PAGE_FILE_FORMATS_XML,
				Images.SETUP_PAGE_FILE_FORMATS_JSON,
				Images.SETUP_PAGE_FILE_FORMATS_ZIP_XML,
				Images.SETUP_PAGE_FILE_FORMATS_TAR_XML,
				Images.SETUP_PAGE_FILE_FORMATS_CRYPT
		}));
		label.setLabelFor(defaultSaveFormatParameterSeries);

		/* Standard-Speicherformat für Optimitereinstellungen */
		line=addLine();
		line.add(label=new JLabel(Language.tr("SettingsDialog.Tabs.DefaultFormats.OptimizerSetups")+":"));
		line.add(defaultSaveFormatOptimizerSetups=new JComboBox<>(new String[]{
				Language.tr("SettingsDialog.Tabs.DefaultFormats.XML"),
				Language.tr("SettingsDialog.Tabs.DefaultFormats.JSON"),
				Language.tr("SettingsDialog.Tabs.DefaultFormats.ZIP_XML"),
				Language.tr("SettingsDialog.Tabs.DefaultFormats.TAR_XML"),
				Language.tr("SettingsDialog.Tabs.DefaultFormats.CRYPT"),

		}));
		defaultSaveFormatOptimizerSetups.setRenderer(new IconListCellRenderer(new Images[]{
				Images.SETUP_PAGE_FILE_FORMATS_XML,
				Images.SETUP_PAGE_FILE_FORMATS_JSON,
				Images.SETUP_PAGE_FILE_FORMATS_ZIP_XML,
				Images.SETUP_PAGE_FILE_FORMATS_TAR_XML,
				Images.SETUP_PAGE_FILE_FORMATS_CRYPT
		}));
		label.setLabelFor(defaultSaveFormatOptimizerSetups);

		/*
		 * Bereich:
		 * Sicherheit
		 */
		addHeading(Language.tr("SettingsDialog.Tabs.BackupFiles.Heading"));

		/* Backup-Dateien beim Speichern von Modellen anlegen? */
		addLine().add(useBackupFiles=new JCheckBox(Language.tr("SettingsDialog.Tabs.BackupFiles")));

		/*
		 * Bereich:
		 * Excel-DDE-Verbindung
		 */
		addHeading(Language.tr("SettingsDialog.Tabs.ExcelDDEConnect"));

		/* Benutzerdefinierter Zeilenbezeichner */
		data=ModelElementBaseDialog.getInputPanel(Language.tr("SettingsDialog.ExcelDDEConnection.CustomRow")+":","",2);
		add(line=(JPanel)data[0]);
		excelRow=(JTextField)data[1];
		line.add(new JLabel(Language.tr("SettingsDialog.ExcelDDEConnection.CustomRow.Info")));

		/* Benutzerdefinierter Spaltenbezeichner */
		data=ModelElementBaseDialog.getInputPanel(Language.tr("SettingsDialog.ExcelDDEConnection.CustomColumn")+":","",2);
		add(line=(JPanel)data[0]);
		excelCol=(JTextField)data[1];
		line.add(new JLabel(Language.tr("SettingsDialog.ExcelDDEConnection.CustomColumn.Info")));

		/*
		 * Bereich:
		 * Grafiken
		 */
		addHeading(Language.tr("SettingsDialog.Tabs.ExportGraphics"));

		/* Bildgröße beim Exportieren von Bildern */
		data=ModelElementBaseDialog.getInputPanel(Language.tr("SettingsDialog.ImageResolution")+":","",5);
		add((JPanel)data[0]);
		imageSize=(JTextField)data[1];
		imageSize.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {NumberTools.getPositiveLong(imageSize,true);}
			@Override public void keyReleased(KeyEvent e) {NumberTools.getPositiveLong(imageSize,true);}
			@Override public void keyPressed(KeyEvent e) {NumberTools.getPositiveLong(imageSize,true);}
		});

		addLine().add(new JLabel(Language.tr("SettingsDialog.ImageAnimation")+":"));
		/* Speichern von Schnappschüssen während der Animation im Nutzerverzeichnis */
		addLine().add(imagesAnimationHome=new JRadioButton(Language.tr("SettingsDialog.ImageAnimation.Home")+" ("+FileSystemView.getFileSystemView().getHomeDirectory()+")"));
		/* Speichern von Schnappschüssen während der Animation in manuell konfiguriertem Verzeichnis */
		add(line=new JPanel(new BorderLayout()));
		final JPanel part=new JPanel(new FlowLayout(FlowLayout.LEFT));
		line.add(part,BorderLayout.WEST);
		part.add(imagesAnimationFolder=new JRadioButton(Language.tr("SettingsDialog.ImageAnimation.Folder")+":"));
		/* Verzeichnis zum Speichern von Schnappschüssen während der Animation */
		line.add(imagesAnimationFolderEdit=new JTextField(),BorderLayout.CENTER);
		ModelElementBaseDialog.addUndoFeature(imagesAnimationFolderEdit);
		imagesAnimationFolderEdit.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {imagesAnimationFolder.setSelected(true);}
			@Override public void keyReleased(KeyEvent e) {imagesAnimationFolder.setSelected(true);}
			@Override public void keyPressed(KeyEvent e) {imagesAnimationFolder.setSelected(true);}
		});
		/* Verzeichnis zum Speichern von Schnappschüssen während der Animation auswählen */
		final JButton imagesAnimationFolderButton=new JButton("");
		line.add(imagesAnimationFolderButton,BorderLayout.EAST);
		imagesAnimationFolderButton.setToolTipText(Language.tr("SettingsDialog.ImageAnimation.Folder.Select"));
		imagesAnimationFolderButton.setIcon(Images.GENERAL_SELECT_FOLDER.getIcon());
		imagesAnimationFolderButton.addActionListener(e->selectImagesFolder());

		final ButtonGroup buttonGroup=new ButtonGroup();
		buttonGroup.add(imagesAnimationHome);
		buttonGroup.add(imagesAnimationFolder);

		/*
		 * Bereich:
		 * Videos
		 */
		addHeading(Language.tr("SettingsDialog.Tabs.ExportVideos"));

		addLine().add(paintTimeStamp=new JCheckBox(Language.tr("SettingsDialog.Tabs.Simulation.PaintTimeStamp")));
	}

	/**
	 * Dialog zur Auswahl eines Ausgabeverzeichnisses für Animations-Screenshots anzeigen
	 */
	private void selectImagesFolder() {
		final String folder=selectFolder(Language.tr("Batch.Output.Folder.Button.Hint"),imagesAnimationFolderEdit.getText());
		if (folder!=null) {
			imagesAnimationFolderEdit.setText(folder);
			imagesAnimationFolder.setSelected(true);
		}
	}

	/**
	 * Zeigt einen Verzeichnisauswahldialog an
	 * @param title	Titel des Dialogs
	 * @param oldFolder	Zu Beginn auszuwählendes Verzeichnis
	 * @return	Neues Verzeichnis oder <code>null</code>, wenn die Auswahl abgebrochen wurde
	 */
	private String selectFolder(final String title, final String oldFolder) {
		final JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		if (oldFolder!=null && !oldFolder.trim().isEmpty() && new File(oldFolder).isDirectory()) {
			fc.setCurrentDirectory(new File(oldFolder));
		}
		fc.setDialogTitle(title);
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (fc.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION) return null;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		final File file=fc.getSelectedFile();
		return file.toString();
	}

	@Override
	public void loadData() {
		defaultUserName.setText(setup.defaultUserName);
		defaultUserEMail.setText(setup.defaultUserEMail);
		switch (setup.defaultSaveFormatModels) {
		case XML: defaultSaveFormatModels.setSelectedIndex(0); break;
		case JSON: defaultSaveFormatModels.setSelectedIndex(1); break;
		case ZIP_XML: defaultSaveFormatModels.setSelectedIndex(2); break;
		case CRYPT_XML: defaultSaveFormatModels.setSelectedIndex(3); break;
		case TAR_XML: defaultSaveFormatModels.setSelectedIndex(4); break;
		default: defaultSaveFormatModels.setSelectedIndex(0); break;
		}
		switch (setup.defaultSaveFormatStatistics) {
		case XML: defaultSaveFormatStatistics.setSelectedIndex(0); break;
		case JSON: defaultSaveFormatStatistics.setSelectedIndex(1); break;
		case ZIP_XML: defaultSaveFormatStatistics.setSelectedIndex(2); break;
		case CRYPT_XML: defaultSaveFormatStatistics.setSelectedIndex(3); break;
		case TAR_XML: defaultSaveFormatStatistics.setSelectedIndex(4); break;
		default: defaultSaveFormatStatistics.setSelectedIndex(0); break;
		}
		switch (setup.defaultSaveFormatParameterSeries) {
		case XML: defaultSaveFormatParameterSeries.setSelectedIndex(0); break;
		case JSON: defaultSaveFormatParameterSeries.setSelectedIndex(1); break;
		case ZIP_XML: defaultSaveFormatParameterSeries.setSelectedIndex(2); break;
		case CRYPT_XML: defaultSaveFormatParameterSeries.setSelectedIndex(3); break;
		case TAR_XML: defaultSaveFormatParameterSeries.setSelectedIndex(4); break;
		default: defaultSaveFormatParameterSeries.setSelectedIndex(0); break;
		}
		switch (setup.defaultSaveFormatOptimizerSetups) {
		case XML: defaultSaveFormatOptimizerSetups.setSelectedIndex(0); break;
		case JSON: defaultSaveFormatOptimizerSetups.setSelectedIndex(1); break;
		case ZIP_XML: defaultSaveFormatOptimizerSetups.setSelectedIndex(2); break;
		case CRYPT_XML: defaultSaveFormatOptimizerSetups.setSelectedIndex(3); break;
		case TAR_XML: defaultSaveFormatOptimizerSetups.setSelectedIndex(4); break;
		default: defaultSaveFormatOptimizerSetups.setSelectedIndex(0); break;
		}
		useBackupFiles.setSelected(setup.useBackupFiles);

		if (setup.customExcelRowName!=null && !setup.customExcelRowName.trim().isEmpty()) excelRow.setText(setup.customExcelRowName.trim());
		if (setup.customExcelColName!=null && !setup.customExcelColName.trim().isEmpty()) excelCol.setText(setup.customExcelColName.trim());

		imageSize.setText(""+Math.min(5000,Math.max(50,setup.imageSize)));
		if (setup.imagePathAnimation==null || setup.imagePathAnimation.trim().isEmpty()) {
			imagesAnimationHome.setSelected(true);
		} else {
			imagesAnimationFolder.setSelected(true);
			imagesAnimationFolderEdit.setText(setup.imagePathAnimation);
		}
		paintTimeStamp.setSelected(setup.paintTimeStamp);
	}

	@Override
	public boolean checkData() {
		final Long L=NumberTools.getPositiveLong(imageSize,true);
		if (L==null || L<50 || L>5000) {
			MsgBox.error(this,Language.tr("SettingsDialog.ImageResolution.Invalid.Title"),Language.tr("SettingsDialog.ImageResolution.Invalid.Info"));
			return false;
		}

		return true;
	}

	@Override
	public void storeData() {
		setup.defaultUserName=defaultUserName.getText().trim();
		setup.defaultUserEMail=defaultUserEMail.getText().trim();
		switch (defaultSaveFormatModels.getSelectedIndex()) {
		case 0: setup.defaultSaveFormatModels=XMLTools.DefaultSaveFormat.XML; break;
		case 1: setup.defaultSaveFormatModels=XMLTools.DefaultSaveFormat.JSON; break;
		case 2: setup.defaultSaveFormatModels=XMLTools.DefaultSaveFormat.ZIP_XML; break;
		case 3: setup.defaultSaveFormatModels=XMLTools.DefaultSaveFormat.TAR_XML; break;
		case 4: setup.defaultSaveFormatModels=XMLTools.DefaultSaveFormat.CRYPT_XML; break;
		}
		switch (defaultSaveFormatStatistics.getSelectedIndex()) {
		case 0: setup.defaultSaveFormatStatistics=XMLTools.DefaultSaveFormat.XML; break;
		case 1: setup.defaultSaveFormatStatistics=XMLTools.DefaultSaveFormat.JSON; break;
		case 2: setup.defaultSaveFormatStatistics=XMLTools.DefaultSaveFormat.ZIP_XML; break;
		case 3: setup.defaultSaveFormatStatistics=XMLTools.DefaultSaveFormat.TAR_XML; break;
		case 4: setup.defaultSaveFormatStatistics=XMLTools.DefaultSaveFormat.CRYPT_XML; break;
		}
		switch (defaultSaveFormatParameterSeries.getSelectedIndex()) {
		case 0: setup.defaultSaveFormatParameterSeries=XMLTools.DefaultSaveFormat.XML; break;
		case 1: setup.defaultSaveFormatParameterSeries=XMLTools.DefaultSaveFormat.JSON; break;
		case 2: setup.defaultSaveFormatParameterSeries=XMLTools.DefaultSaveFormat.ZIP_XML; break;
		case 3: setup.defaultSaveFormatParameterSeries=XMLTools.DefaultSaveFormat.TAR_XML; break;
		case 4: setup.defaultSaveFormatParameterSeries=XMLTools.DefaultSaveFormat.CRYPT_XML; break;
		}
		switch (defaultSaveFormatOptimizerSetups.getSelectedIndex()) {
		case 0: setup.defaultSaveFormatOptimizerSetups=XMLTools.DefaultSaveFormat.XML; break;
		case 1: setup.defaultSaveFormatOptimizerSetups=XMLTools.DefaultSaveFormat.JSON; break;
		case 2: setup.defaultSaveFormatOptimizerSetups=XMLTools.DefaultSaveFormat.ZIP_XML; break;
		case 3: setup.defaultSaveFormatOptimizerSetups=XMLTools.DefaultSaveFormat.TAR_XML; break;
		case 4: setup.defaultSaveFormatOptimizerSetups=XMLTools.DefaultSaveFormat.CRYPT_XML; break;
		}
		setup.useBackupFiles=useBackupFiles.isSelected();

		setup.customExcelRowName=excelRow.getText().trim().toUpperCase();
		setup.customExcelColName=excelCol.getText().trim().toUpperCase();

		Long L=NumberTools.getPositiveLong(imageSize,true);
		if (L!=null) setup.imageSize=(int)((long)L);
		if (imagesAnimationHome.isSelected()) {
			setup.imagePathAnimation="";
		} else {
			setup.imagePathAnimation=imagesAnimationFolderEdit.getText().trim();
		}
		setup.paintTimeStamp=paintTimeStamp.isSelected();
	}

	@Override
	public void resetSettings() {
		defaultUserName.setText(System.getProperty("user.name"));
		defaultUserEMail.setText("");
		defaultSaveFormatModels.setSelectedIndex(0);
		defaultSaveFormatStatistics.setSelectedIndex(0);
		defaultSaveFormatParameterSeries.setSelectedIndex(2);
		defaultSaveFormatOptimizerSetups.setSelectedIndex(2);
		useBackupFiles.setSelected(false);
		excelRow.setText("");
		excelCol.setText("");
		imageSize.setText("2000");
		imagesAnimationHome.setSelected(true);
		paintTimeStamp.setSelected(true);
	}
}
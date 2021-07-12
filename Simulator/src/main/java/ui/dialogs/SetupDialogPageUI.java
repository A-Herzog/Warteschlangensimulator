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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import language.Language;
import simulator.examples.EditModelExamples;
import systemtools.BaseDialog;
import systemtools.GUITools;
import tools.IconListCellRenderer;
import tools.SetupData;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.infopanel.InfoPanelDialog;
import ui.script.ScriptEditorAreaBuilder;

/**
 * Dialogseite "Benutzeroberfläche" im Programmeinstellungen-Dialog
 * @author Alexander Herzog
 * @see SetupData
 */
public class SetupDialogPageUI extends SetupDialogPage {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=7003188055714042484L;

	/* Bereich: Allgemein */

	/** Programmsprache */
	private final JComboBox<String> languages;
	/** Zu verwendendes Theme */
	private final JComboBox<String> lookAndFeel;
	/** Menü in Titelzeile kombinieren? (Für Flat-Look&amp;Feels unter Windows) */
	private final JCheckBox lookAndFeelCombinedMenu;
	/** Schriftgröße für Programmoberfläche */
	private final JComboBox<String> fontSizes;
	/** Hohe Kontraste verwenden? */
	private final JCheckBox useHighContrasts;
	/** Schriftgröße in Skript-Editorfeldern */
	private final JComboBox<String> scriptFontSize;
	/** Modelle automatisch speichern? */
	private final JComboBox<String> autoSave;
	/** Zuletzt verwendete Dateien merken? */
	private final JCheckBox useLastFiles;
	/** Zuletzt geöffnetes Modell beim nächsten Start wieder laden? */
	private final JCheckBox autoRestore;

	/* Bereich: Programmstart */

	/** Fenstergröße beim Programmstart */
	private final JComboBox<String> programStartWindow;
	/** Vorlagenleiste beim Programmstart einblenden? */
	private final JComboBox<String> templateStartMode;
	/** Beispielmodell beim Programmstart laden? */
	private final JComboBox<String> startModel;

	/* Bereich: Unterstützung und Benachrichtigungen */

	/** Beim Programmstart Hilfe auf der Zeichenfläche anzeigen? */
	private final JComboBox<String> surfaceHelp;
	/** Halbtransparente Informationen auf der Zeichenfläche anzeigen */
	private final JCheckBox surfaceGlassInfos;
	/** Benachrichtigungen im System-Tray zum Simulationsende */
	private final JComboBox<String> notifyMode;
	/** Benachrichtigungen per MQTT zum Simulationsende senden */
	private final JCheckBox notifyMQTT;
	/** Daten zu den Hilfe-Panels */
	private String hintDialogs;

	/**
	 * Konstruktor der Klasse
	 */
	public SetupDialogPageUI() {
		JPanel line;
		JLabel label;
		JButton button;

		/*
		 * Bereich:
		 * Allgemein
		 */
		addHeading(Language.tr("SettingsDialog.Tabs.GUI.General"));

		/* Programmsprache */
		line=addLine();
		line.add(label=new JLabel(Language.tr("SettingsDialog.Languages")+":"));
		line.add(languages=new JComboBox<>(new String[]{Language.tr("SettingsDialog.Languages.English"),Language.tr("SettingsDialog.Languages.German")}));
		languages.setRenderer(new IconListCellRenderer(new Images[]{Images.LANGUAGE_EN,Images.LANGUAGE_DE}));
		languages.setToolTipText(Language.tr("SettingsDialog.Languages.Info"));
		label.setLabelFor(languages);

		/* Zu verwendendes Theme */
		line=addLine();
		line.add(label=new JLabel(Language.tr("SettingsDialog.LookAndFeel")+":"));
		final List<String> lookAndFeels=new ArrayList<>();
		lookAndFeels.add(Language.tr("SettingsDialog.LookAndFeel.System"));
		lookAndFeels.addAll(Arrays.asList(GUITools.listLookAndFeels()));
		line.add(lookAndFeel=new JComboBox<>(lookAndFeels.toArray(new String[0])));
		label.setLabelFor(lookAndFeel);

		/* Menü in Titelzeile kombinieren? (Für Flat-Look&amp;Feels unter Windows) */
		line.add(lookAndFeelCombinedMenu=new JCheckBox(Language.tr("SettingsDialog.LookAndFeel.MenuInWindowTitle")));
		lookAndFeelCombinedMenu.setToolTipText(Language.tr("SettingsDialog.LookAndFeel.MenuInWindowTitle.Tooltip"));

		/* Schriftgröße für Programmoberfläche */
		line=addLine();
		line.add(label=new JLabel(Language.tr("SettingsDialog.FontSizes")+":"));
		line.add(fontSizes=new JComboBox<>(new String[]{
				Language.tr("SettingsDialog.FontSizes.Small")+" (90%)",
				Language.tr("SettingsDialog.FontSizes.Normal")+" (100%)",
				Language.tr("SettingsDialog.FontSizes.Larger")+" (110%)",
				Language.tr("SettingsDialog.FontSizes.VeryLarge")+" (125%)",
				Language.tr("SettingsDialog.FontSizes.Maximum")+" (150%)"
		}));
		fontSizes.setRenderer(new IconListCellRenderer(new Images[]{
				Images.SETUP_FONT_SIZE1,
				Images.SETUP_FONT_SIZE2,
				Images.SETUP_FONT_SIZE3,
				Images.SETUP_FONT_SIZE4,
				Images.SETUP_FONT_SIZE5
		}));
		label.setLabelFor(fontSizes);

		/* Hohe Kontraste verwenden? */
		line.add(useHighContrasts=new JCheckBox(Language.tr("SettingsDialog.HighContrasts")));

		/* Schriftgröße in Skript-Editorfeldern */
		line=addLine();
		line.add(label=new JLabel(Language.tr("SettingsDialog.ScriptFontSize")+":"));
		line.add(scriptFontSize=new JComboBox<>(getScriptFontSizesList()));
		addLine().add(new JLabel("<html><body>("+Language.tr("SettingsDialog.FontSizes.Info")+")</body></html>"));

		/* Modelle automatisch speichern? */
		line=addLine();
		line.add(label=new JLabel(Language.tr("SettingsDialog.AutoSave")+":"));
		line.add(autoSave=new JComboBox<>(new String[]{
				Language.tr("SettingsDialog.AutoSave.Off"),
				Language.tr("SettingsDialog.AutoSave.Simulation"),
				Language.tr("SettingsDialog.AutoSave.Always"),
		}));
		autoSave.setRenderer(new IconListCellRenderer(new Images[]{
				Images.GENERAL_OFF,
				Images.SIMULATION,
				Images.GENERAL_SAVE
		}));
		label.setLabelFor(autoSave);

		/* Zuletzt verwendete Dateien merken? */
		addLine().add(useLastFiles=new JCheckBox(Language.tr("SettingsDialog.UseLastFiles")));

		/* Zuletzt geöffnetes Modell beim nächsten Start wieder laden? */
		addLine().add(autoRestore=new JCheckBox(Language.tr("SettingsDialog.AutoRestore")));

		/*
		 * Bereich:
		 * Programmstart
		 */
		addHeading(Language.tr("SettingsDialog.Tabs.GUI.ProgramStart"));

		/* Fenstergröße beim Programmstart */
		line=addLine();
		line.add(label=new JLabel(Language.tr("SettingsDialog.WindowSizeProgrmStart")+":"));
		line.add(programStartWindow=new JComboBox<>(new String[]{
				Language.tr("SettingsDialog.WindowSizeProgrmStart.Normal"),
				Language.tr("SettingsDialog.WindowSizeProgrmStart.FullScreen"),
				Language.tr("SettingsDialog.WindowSizeProgrmStart.LastSize")
		}));
		programStartWindow.setRenderer(new IconListCellRenderer(new Images[]{
				Images.SETUP_WINDOW_SIZE_DEFAULT,
				Images.SETUP_WINDOW_SIZE_FULL,
				Images.SETUP_WINDOW_SIZE_LAST
		}));
		label.setLabelFor(programStartWindow);

		/* Vorlagenleiste beim Programmstart einblenden? */
		line=addLine();
		line.add(label=new JLabel(Language.tr("SettingsDialog.TemplatesPanel")+":"));
		line.add(templateStartMode=new JComboBox<>(new String[]{
				Language.tr("SettingsDialog.TemplatesPanel.Hide"),
				Language.tr("SettingsDialog.TemplatesPanel.Show"),
				Language.tr("SettingsDialog.TemplatesPanel.LastState")
		}));
		templateStartMode.setRenderer(new IconListCellRenderer(new Images[]{
				Images.SETUP_TEMPLATES_ON_START_HIDE,
				Images.SETUP_TEMPLATES_ON_START_SHOW,
				Images.SETUP_TEMPLATES_ON_START_LAST
		}));
		label.setLabelFor(templateStartMode);

		/* Beispielmodell beim Programmstart laden? */
		line=addLine();
		line.add(label=new JLabel(Language.tr("SettingsDialog.LoadModelOnProgramStart")+":"));
		List<String> models=new ArrayList<>();
		models.add(Language.tr("SettingsDialog.LoadModelOnProgramStart.EmptyModel"));
		models.addAll(Arrays.asList(EditModelExamples.getExamplesList()));
		final List<Images> startModelIcons=new ArrayList<>();
		startModelIcons.add(Images.MODEL_NEW);
		while (startModelIcons.size()<models.size()) startModelIcons.add(Images.MODEL_LOAD);
		line.add(startModel=new JComboBox<>(models.toArray(new String[0])));
		startModel.setRenderer(new IconListCellRenderer(startModelIcons.toArray(new Images[0])));
		label.setLabelFor(startModel);

		/*
		 * Bereich:
		 * Unterstützung und Benachrichtigungen
		 */
		addHeading(Language.tr("SettingsDialog.Tabs.GUI.Support"));

		/* Beim Programmstart Hilfe auf der Zeichenfläche anzeigen? */
		line=addLine();
		line.add(label=new JLabel(Language.tr("SettingsDialog.SurfaceHelp")+":"));
		line.add(surfaceHelp=new JComboBox<>(new String[]{
				Language.tr("SettingsDialog.SurfaceHelp.Never"),
				Language.tr("SettingsDialog.SurfaceHelp.StartOnly"),
				Language.tr("SettingsDialog.SurfaceHelp.Always")
		}));
		surfaceHelp.setRenderer(new IconListCellRenderer(new Images[]{
				Images.GENERAL_OFF,
				Images.SETUP_APPLICATION_START,
				Images.GENERAL_ON
		}));
		label.setLabelFor(surfaceHelp);

		/* Halbtransparente Informationen auf der Zeichenfläche anzeigen */
		addLine().add(surfaceGlassInfos=new JCheckBox(Language.tr("SettingsDialog.SurfaceGlassInfos")));

		/* Benachrichtigungen im System-Tray zum Simulationsende */
		line=addLine();
		line.add(label=new JLabel(Language.tr("SettingsDialog.NotifyMode")+":"));
		line.add(notifyMode=new JComboBox<>(new String[]{
				Language.tr("SettingsDialog.NotifyMode.Off"),
				Language.tr("SettingsDialog.NotifyMode.LongRun"),
				Language.tr("SettingsDialog.NotifyMode.Always")
		}));
		notifyMode.setRenderer(new IconListCellRenderer(new Images[]{
				Images.GENERAL_OFF,
				Images.SETUP_NOTIFY_ON_LONG_RUN,
				Images.GENERAL_ON
		}));
		label.setLabelFor(notifyMode);

		/* Benachrichtigungen per MQTT zum Simulationsende senden */
		line=addLine();
		line.add(notifyMQTT=new JCheckBox(Language.tr("SettingsDialog.NotifyMQTT")));
		line.add(button=new JButton(Language.tr("SettingsDialog.NotifyMQTT.Settings"),Images.SERVER_MQTT.getIcon()));
		button.addActionListener(e->showMQTTSettings());

		/* Dialog zur Konfiguration der Hinweise */
		addLine().add(button=new JButton(Language.tr("SettingsDialog.Tabs.ProgramStart.DialogAdvice"),Images.GENERAL_INFO.getIcon()));
		button.addActionListener(e->showHintsDialog());
	}

	/**
	 * Erzeugt eine Liste mit möglichen Schriftgrößen in Skript-Editor-Feldern.
	 * @return	Liste mit möglichen Schriftgrößen in Skript-Editor-Feldern
	 * @see #scriptFontSize
	 */
	private String[] getScriptFontSizesList() {
		return IntStream.range(6,31).mapToObj(String::valueOf).map(s->s.equals(""+ScriptEditorAreaBuilder.DEFAULT_FONT_SIZE)?(s+" ("+Language.tr("SettingsDialog.ScriptFontSize.Default")+")"):s).toArray(String[]::new);
	}

	/**
	 * Zeigt den Dialog zur Auswahl, in welchen Festern die Info-Texte
	 * angezeigt werden sollen, an.
	 */
	private void showHintsDialog() {
		final InfoPanelDialog dialog=new InfoPanelDialog(this,hintDialogs);
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) hintDialogs=dialog.getData();
	}

	/**
	 * Zeigt einen Dialog an zur Konfiguration der MQTT-Verbindung
	 * (für Benachrichtigungen nach dem Abschluss einer Simulation).
	 */
	private void showMQTTSettings() {
		new SetupDialogMQTTSettings(this);
	}

	@Override
	public void loadData() {
		if (setup.language==null || setup.language.isEmpty() || setup.language.equalsIgnoreCase("de")) languages.setSelectedIndex(1); else languages.setSelectedIndex(0);

		final String[] lookAndFeels=GUITools.listLookAndFeels();
		lookAndFeel.setSelectedIndex(0);
		for (int i=0;i<lookAndFeels.length;i++) if (lookAndFeels[i].equalsIgnoreCase(setup.lookAndFeel)) {
			lookAndFeel.setSelectedIndex(i+1);
			break;
		}
		lookAndFeelCombinedMenu.setSelected(setup.lookAndFeelCombinedMenu);

		fontSizes.setSelectedIndex(1);
		if (setup.scaleGUI<1) fontSizes.setSelectedIndex(0);
		if (setup.scaleGUI>1) fontSizes.setSelectedIndex(2);
		if (setup.scaleGUI>1.1) fontSizes.setSelectedIndex(3);
		if (setup.scaleGUI>1.3) fontSizes.setSelectedIndex(4);
		useHighContrasts.setSelected(setup.useHighContrasts);
		scriptFontSize.setSelectedIndex(ScriptEditorAreaBuilder.getFontSize()-6);
		switch (setup.autoSaveMode) {
		case AUTOSAVE_OFF: autoSave.setSelectedIndex(0); break;
		case AUTOSAVE_SIMULATION: autoSave.setSelectedIndex(1); break;
		case AUTOSAVE_ALWAYS: autoSave.setSelectedIndex(2); break;
		}
		useLastFiles.setSelected(setup.useLastFiles);
		autoRestore.setSelected(setup.autoRestore);
		switch (setup.startSizeMode) {
		case START_MODE_DEFAULT: programStartWindow.setSelectedIndex(0); break;
		case START_MODE_FULLSCREEN: programStartWindow.setSelectedIndex(1); break;
		case START_MODE_LASTSIZE: programStartWindow.setSelectedIndex(2); break;
		}

		switch (setup.startTemplateMode) {
		case START_TEMPLATE_HIDDEN: templateStartMode.setSelectedIndex(0); break;
		case START_TEMPLATE_VISIBLE: templateStartMode.setSelectedIndex(1); break;
		case START_TEMPLATE_LASTSTATE: templateStartMode.setSelectedIndex(2); break;
		}

		startModel.setSelectedIndex(EditModelExamples.getExampleIndexFromName(setup.startModel)+1);

		switch (setup.surfaceHelp) {
		case NEVER: surfaceHelp.setSelectedIndex(0); break;
		case START_ONLY: surfaceHelp.setSelectedIndex(1); break;
		case ALWAYS: surfaceHelp.setSelectedIndex(2); break;
		}

		surfaceGlassInfos.setSelected(setup.surfaceGlassInfos);

		switch (setup.notifyMode) {
		case OFF: notifyMode.setSelectedIndex(0); break;
		case LONGRUN: notifyMode.setSelectedIndex(1); break;
		case ALWAYS: notifyMode.setSelectedIndex(2); break;
		}
		notifyMQTT.setSelected(setup.notifyMQTT);
		hintDialogs=setup.hintDialogs;
	}

	@Override
	public void storeData() {
		setup.language=(languages.getSelectedIndex()==1)?"de":"en";

		if (lookAndFeel.getSelectedIndex()==0) setup.lookAndFeel=""; else setup.lookAndFeel=(String)lookAndFeel.getSelectedItem();
		setup.lookAndFeelCombinedMenu=lookAndFeelCombinedMenu.isSelected();

		switch (fontSizes.getSelectedIndex()) {
		case 0: setup.scaleGUI=0.9; break;
		case 1: setup.scaleGUI=1; break;
		case 2: setup.scaleGUI=1.1; break;
		case 3: setup.scaleGUI=1.25; break;
		case 4: setup.scaleGUI=1.5; break;
		}
		setup.useHighContrasts=useHighContrasts.isSelected();
		setup.scriptFontSize=scriptFontSize.getSelectedIndex()+6;
		switch (autoSave.getSelectedIndex()) {
		case 0: setup.autoSaveMode=SetupData.AutoSaveMode.AUTOSAVE_OFF; break;
		case 1: setup.autoSaveMode=SetupData.AutoSaveMode.AUTOSAVE_SIMULATION; break;
		case 2: setup.autoSaveMode=SetupData.AutoSaveMode.AUTOSAVE_ALWAYS; break;
		}
		setup.useLastFiles=useLastFiles.isSelected();
		setup.autoRestore=autoRestore.isSelected();
		switch (programStartWindow.getSelectedIndex()) {
		case 0: setup.startSizeMode=SetupData.StartSizeMode.START_MODE_DEFAULT; break;
		case 1: setup.startSizeMode=SetupData.StartSizeMode.START_MODE_FULLSCREEN; break;
		case 2: setup.startSizeMode=SetupData.StartSizeMode.START_MODE_LASTSIZE; break;
		}
		switch (templateStartMode.getSelectedIndex()) {
		case 0: setup.startTemplateMode=SetupData.StartTemplateMode.START_TEMPLATE_HIDDEN; break;
		case 1: setup.startTemplateMode=SetupData.StartTemplateMode.START_TEMPLATE_VISIBLE; break;
		case 2: setup.startTemplateMode=SetupData.StartTemplateMode.START_TEMPLATE_LASTSTATE; break;
		}
		if (startModel.getSelectedIndex()==0) setup.startModel=""; else setup.startModel=EditModelExamples.getExamplesList()[startModel.getSelectedIndex()-1];
		switch (surfaceHelp.getSelectedIndex()) {
		case 0: setup.surfaceHelp=SetupData.SurfaceHelp.NEVER; break;
		case 1: setup.surfaceHelp=SetupData.SurfaceHelp.START_ONLY; break;
		case 2: setup.surfaceHelp=SetupData.SurfaceHelp.ALWAYS; break;
		}
		setup.surfaceGlassInfos=surfaceGlassInfos.isSelected();
		switch (notifyMode.getSelectedIndex()) {
		case 0: setup.notifyMode=SetupData.NotifyMode.OFF; break;
		case 1: setup.notifyMode=SetupData.NotifyMode.LONGRUN; break;
		case 2: setup.notifyMode=SetupData.NotifyMode.ALWAYS; break;
		}
		setup.notifyMQTT=notifyMQTT.isSelected();
		setup.hintDialogs=hintDialogs;
		InfoPanel.getInstance().loadSetup(setup.hintDialogs);
	}

	@Override
	public void resetSettings() {
		lookAndFeel.setSelectedIndex(0);
		lookAndFeelCombinedMenu.setSelected(true);
		fontSizes.setSelectedIndex(1);
		useHighContrasts.setSelected(false);
		scriptFontSize.setSelectedIndex(ScriptEditorAreaBuilder.DEFAULT_FONT_SIZE-6);
		autoSave.setSelectedIndex(0);
		useLastFiles.setSelected(true);
		autoRestore.setSelected(false);
		programStartWindow.setSelectedIndex(0);
		templateStartMode.setSelectedIndex(0);
		startModel.setSelectedIndex(0);
		surfaceHelp.setSelectedIndex(1);
		surfaceGlassInfos.setSelected(true);
		notifyMode.setSelectedIndex(1);
		notifyMQTT.setSelected(false);
		hintDialogs="";
	}

}

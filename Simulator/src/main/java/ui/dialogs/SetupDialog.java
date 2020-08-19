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
package ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileSystemView;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.swing.CommonVariables;
import net.calc.ServerStatus;
import scripting.js.JSEngineNames;
import simulator.examples.EditModelExamples;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.IconListCellRenderer;
import tools.SetupData;
import ui.MainPanel;
import ui.UpdateSystem;
import ui.help.Help;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.infopanel.InfoPanelDialog;
import ui.modeleditor.ModelElementBaseDialog;
import ui.quickaccess.JPlaceholderTextField;
import xml.XMLTools;

/**
 * Zeigt den Setup-Dialog unter Verwendung der Daten aus <code>SetupData</code> an.
 * Alle Verarbeitung erfolgt direkt in dem Dialog. Der Dialog muss vom Aufrufer nur per Konstruktor
 * erstellt werden; das Daten Laden, Prüfen und Speichern übernimmt der Dialog selbst.
 * @see SetupData
 * @author Alexander Herzog
 */
public final class SetupDialog extends BaseDialog {
	private static final long serialVersionUID = 8167759839522880144L;

	private final JTabbedPane tabs;

	/* Seite: Benutzeroberfläche */
	private final JComboBox<String> languages;
	private final JComboBox<String> fontSizes;
	private final JCheckBox useHighContrasts;
	private final JComboBox<String> autoSave;
	private final JCheckBox useLastFiles;
	private final JCheckBox autoRestore;
	private final JComboBox<String> programStartWindow;
	private final JComboBox<String> templateStartMode;
	private final JComboBox<String> startModel;
	private final JComboBox<String> modelSecurity;
	private final JComboBox<String> surfaceHelp;
	private final JComboBox<String> notifyMode;
	private String hintDialogs;

	/* Seite: Leistung */
	private final JComboBox<String> backgroundProcessing;
	private final JCheckBox useMultiCoreSimulation;
	private final JCheckBox useMultiCoreAnimation;
	private final JCheckBox highPriority;
	private final JCheckBox useNUMAMode;
	private final JComboBox<String> jsEngine;
	private final JCheckBox canelSimulationOnScriptError;
	private final JCheckBox serverUse;
	private final JTextField serverName;
	private final SpinnerModel serverPort;
	private final JTextField serverKey;

	/* Seite: Animation */
	private final JComboBox<String> animationWarmUpMode;
	private final JCheckBox showStationData;
	private final JCheckBox showSingleStepLogData;
	private final JCheckBox animationStartPaused;
	private final JCheckBox animateResources;
	private final JCheckBox useSlowModeAnimation;

	/* Seite: Statistik */
	private final JCheckBox showQuantils;
	private final JCheckBox showErlangC;
	private final JCheckBox expandAllStatistics;
	private final SpinnerModel statisticsNumberDigits;
	private final SpinnerModel statisticsPercentDigits;
	private final JPlaceholderTextField quantilLevels;
	private final JPlaceholderTextField batchMeansConfidenceLevels;
	private final JCheckBox openWord;
	private final JCheckBox openODT;
	private final JCheckBox openExcel;
	private final JCheckBox openODS;

	/* Seite: Dateiformate */
	private final JTextField defaultUserName;
	private final JComboBox<String> defaultSaveFormatModels;
	private final JComboBox<String> defaultSaveFormatStatistics;
	private final JComboBox<String> defaultSaveFormatParameterSeries;
	private final JComboBox<String> defaultSaveFormatOptimizerSetups;
	private final JTextField excelRow;
	private final JTextField excelCol;
	private final JTextField imageSize;
	private final JRadioButton imagesAnimationHome;
	private final JRadioButton imagesAnimationFolder;
	private final JTextField imagesAnimationFolderEdit;
	private final JButton imagesAnimationFolderButton;
	private final JCheckBox paintTimeStamp;

	/* Seite: Updates */
	private final JCheckBox programStartJavaCheck;
	private final JCheckBox autoUpdate;
	private final JLabel updateInfo;
	private final JButton manualUpdateButton;
	private final JButton updateCheckButton;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param showUpdatesPage	Soll die Updates-Seite direkt beim Aufruf ausgewählt werden?
	 */
	public SetupDialog(final Component owner, final boolean showUpdatesPage) {
		super(owner,Language.tr("SettingsDialog.Title"));

		addUserButton(Language.tr("SettingsDialog.Default"),Language.tr("SettingsDialog.Default.Hint"),Images.EDIT_UNDO.getURL());
		JPanel main=createGUI(()->{Help.topicModal(SetupDialog.this.owner,"Setup");});
		main.setLayout(new BorderLayout());
		main.add(tabs=new JTabbedPane(),BorderLayout.CENTER);

		/* Dialogseiten einrichten */

		JPanel tab, mainarea, p;
		Object[] data;
		JLabel label;
		JButton button;

		/* Seite: Benutzeroberfläche */

		tabs.addTab(Language.tr("SettingsDialog.Tabs.GUI"),tab=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		tab.add(mainarea=new JPanel());
		mainarea.setLayout(new BoxLayout(mainarea,BoxLayout.Y_AXIS));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(new JLabel("<html><body><b>"+Language.tr("SettingsDialog.Tabs.GUI.General")+"</b></body></html>"));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(label=new JLabel(Language.tr("SettingsDialog.Languages")+":"));
		p.add(languages=new JComboBox<String>(new String[]{Language.tr("SettingsDialog.Languages.English"),Language.tr("SettingsDialog.Languages.German")}));
		languages.setRenderer(new IconListCellRenderer(new Images[]{Images.LANGUAGE_EN,Images.LANGUAGE_DE}));
		languages.setToolTipText(Language.tr("SettingsDialog.Languages.Info"));
		label.setLabelFor(languages);

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(label=new JLabel(Language.tr("SettingsDialog.FontSizes")+":"));
		p.add(fontSizes=new JComboBox<String>(new String[]{
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
		p.add(useHighContrasts=new JCheckBox(Language.tr("SettingsDialog.HighContrasts")));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(new JLabel("<html><body>("+Language.tr("SettingsDialog.FontSizes.Info")+")</body></html>"));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(label=new JLabel(Language.tr("SettingsDialog.AutoSave")+":"));
		p.add(autoSave=new JComboBox<String>(new String[]{
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

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(useLastFiles=new JCheckBox(Language.tr("SettingsDialog.UseLastFiles")));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(autoRestore=new JCheckBox(Language.tr("SettingsDialog.AutoRestore")));

		mainarea.add(Box.createVerticalStrut(15));
		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(new JLabel("<html><body><b>"+Language.tr("SettingsDialog.Tabs.GUI.ProgramStart")+"</b></body></html>"));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(label=new JLabel(Language.tr("SettingsDialog.WindowSizeProgrmStart")+":"));
		p.add(programStartWindow=new JComboBox<String>(new String[]{
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

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(label=new JLabel(Language.tr("SettingsDialog.TemplatesPanel")+":"));
		p.add(templateStartMode=new JComboBox<String>(new String[]{
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

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(label=new JLabel(Language.tr("SettingsDialog.LoadModelOnProgramStart")+":"));
		List<String> models=new ArrayList<>();
		models.add(Language.tr("SettingsDialog.LoadModelOnProgramStart.EmptyModel"));
		models.addAll(Arrays.asList(EditModelExamples.getExamplesList()));
		final List<Images> startModelIcons=new ArrayList<>();
		startModelIcons.add(Images.MODEL_NEW);
		while (startModelIcons.size()<models.size()) startModelIcons.add(Images.MODEL_LOAD);
		p.add(startModel=new JComboBox<String>(models.toArray(new String[0])));
		startModel.setRenderer(new IconListCellRenderer(startModelIcons.toArray(new Images[0])));
		label.setLabelFor(startModel);

		mainarea.add(Box.createVerticalStrut(15));
		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(new JLabel("<html><body><b>"+Language.tr("SettingsDialog.Tabs.Security")+"</b></body></html>"));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(label=new JLabel(Language.tr("SettingsDialog.ModellSecurity")+":"));
		p.add(modelSecurity=new JComboBox<String>(new String[]{
				Language.tr("SettingsDialog.ModellSecurity.AllowAll"),
				Language.tr("SettingsDialog.ModellSecurity.Ask"),
				Language.tr("SettingsDialog.ModellSecurity.Strict")
		}));
		modelSecurity.setRenderer(new IconListCellRenderer(new Images[]{
				Images.GENERAL_LOCK_OPEN,
				Images.HELP,
				Images.GENERAL_LOCK_CLOSED
		}));
		label.setLabelFor(modelSecurity);
		final JToolBar infoToolbar=new JToolBar();
		infoToolbar.setFloatable(false);
		p.add(infoToolbar);
		final JButton infoButton=new JButton();
		infoButton.setToolTipText(Language.tr("SettingsDialog.ModellSecurity.Hint"));
		infoButton.setIcon(Images.HELP.getIcon());
		infoButton.addActionListener(e->MsgBox.info(this,Language.tr("SettingsDialog.ModellSecurity"),Language.tr("SettingsDialog.ModellSecurity.Info")));
		infoToolbar.add(infoButton);

		mainarea.add(Box.createVerticalStrut(15));
		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(new JLabel("<html><body><b>"+Language.tr("SettingsDialog.Tabs.GUI.Support")+"</b></body></html>"));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(label=new JLabel(Language.tr("SettingsDialog.SurfaceHelp")+":"));
		p.add(surfaceHelp=new JComboBox<String>(new String[]{
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

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(label=new JLabel(Language.tr("SettingsDialog.NotifyMode")+":"));
		p.add(notifyMode=new JComboBox<String>(new String[]{
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


		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		button=new JButton(Language.tr("SettingsDialog.Tabs.ProgramStart.DialogAdvice"));
		button.setIcon(Images.GENERAL_INFO.getIcon());
		button.addActionListener(e->showHintsDialog());
		p.add(button);

		/* Seite: Leistung */

		tabs.add(Language.tr("SettingsDialog.Tabs.Performance"),tab=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		tab.add(mainarea=new JPanel());
		mainarea.setLayout(new BoxLayout(mainarea,BoxLayout.Y_AXIS));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(new JLabel("<html><body><b>"+Language.tr("SettingsDialog.Tabs.Simulation.General")+"</b></body></html>"));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(label=new JLabel(Language.tr("SettingsDialog.BackgroundProcessing")+":"));
		p.add(backgroundProcessing=new JComboBox<String>(new String[]{
				Language.tr("SettingsDialog.BackgroundProcessing.Off"),
				Language.tr("SettingsDialog.BackgroundProcessing.CheckOnly"),
				Language.tr("SettingsDialog.BackgroundProcessing.Simulate"),
				Language.tr("SettingsDialog.BackgroundProcessing.SimulateAlways")
		}));
		backgroundProcessing.setRenderer(new IconListCellRenderer(new Images[]{
				Images.GENERAL_OFF,
				Images.SIMULATION_CHECK,
				Images.SIMULATION,
				Images.SIMULATION
		}));
		label.setLabelFor(backgroundProcessing);

		mainarea.add(Box.createVerticalStrut(15));
		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(new JLabel("<html><body><b>"+Language.tr("SettingsDialog.Tabs.Simulation.Local")+"</b></body></html>"));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(useMultiCoreSimulation=new JCheckBox(Language.tr("SettingsDialog.Tabs.Simulation.UseMultiCore")));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(useMultiCoreAnimation=new JCheckBox(Language.tr("SettingsDialog.Tabs.Simulation.UseMultiCoreAnimation")));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(highPriority=new JCheckBox(Language.tr("SettingsDialog.Tabs.Simulation.UseHighPriority")));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(useNUMAMode=new JCheckBox(Language.tr("SettingsDialog.Tabs.Simulation.UseNUMA")));
		p.add(Box.createHorizontalStrut(5));
		final JToolBar bar=new JToolBar();
		bar.setFloatable(false);
		p.add(bar);
		bar.add(button=new JButton());
		button.setIcon(Images.HELP.getIcon());
		button.setToolTipText(Language.tr("SettingsDialog.Tabs.Simulation.UseNUMA.InfoShort"));
		button.addActionListener(e->MsgBox.info(this,Language.tr("SettingsDialog.Tabs.Simulation.UseNUMA.InfoShort"),Language.tr("SettingsDialog.Tabs.Simulation.UseNUMA.InfoLong")));

		mainarea.add(Box.createVerticalStrut(15));
		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(new JLabel("<html><body><b>"+Language.tr("SettingsDialog.Tabs.Performance.Scripting")+"</b></body></html>"));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(label=new JLabel(Language.tr("SettingsDialog.JSEngine")+":"));
		p.add(jsEngine=new JComboBox<String>(new String[] {
				Language.tr("SettingsDialog.JSEngine.Automatic"),
				Language.tr("SettingsDialog.JSEngine.Nashorn"),
				Language.tr("SettingsDialog.JSEngine.Rhino"),
				Language.tr("SettingsDialog.JSEngine.GraalJS"),
		}));
		jsEngine.setRenderer(new IconListCellRenderer(new Images[]{
				Images.SETUP_ENGINE_AUTOMATIC,
				Images.SETUP_ENGINE_NASHORN,
				Images.SETUP_ENGINE_RHINO,
				Images.SETUP_ENGINE_GRAAL
		}));
		label.setLabelFor(jsEngine);

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(canelSimulationOnScriptError=new JCheckBox(Language.tr("SettingsDialog.Tabs.Performance.CanelSimulationOnScriptError")));

		mainarea.add(Box.createVerticalStrut(15));
		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(new JLabel("<html><body><b>"+Language.tr("SettingsDialog.Tabs.Simulation.Remote")+"</b></body></html>"));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(serverUse=new JCheckBox(Language.tr("SettingsDialog.Tabs.Simulation.Server.Use")));

		data=ModelElementBaseDialog.getInputPanel(Language.tr("SettingsDialog.Tabs.Simulation.Server.Name")+":","");
		mainarea.add((JPanel)data[0]);
		serverName=(JTextField)data[1];
		serverName.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {serverUse.setSelected(true);}
			@Override public void keyReleased(KeyEvent e) {serverUse.setSelected(true);}
			@Override public void keyPressed(KeyEvent e) {serverUse.setSelected(true);}
		});

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(label=new JLabel(Language.tr("SettingsDialog.Tabs.Simulation.Server.Port")+":"));
		final JSpinner serverPortSpinner=new JSpinner(serverPort=new SpinnerNumberModel(1,1,65535,1));
		final JSpinner.NumberEditor editor=new JSpinner.NumberEditor(serverPortSpinner);
		editor.getFormat().setGroupingUsed(false);
		editor.getTextField().setColumns(6);
		serverPortSpinner.setEditor(editor);
		p.add(serverPortSpinner);
		label.setLabelFor(serverPortSpinner);

		data=ModelElementBaseDialog.getInputPanel(Language.tr("SettingsDialog.Tabs.Simulation.Server.Key")+":","");
		mainarea.add((JPanel)data[0]);
		serverKey=(JTextField)data[1];
		serverKey.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {serverUse.setSelected(true);}
			@Override public void keyReleased(KeyEvent e) {serverUse.setSelected(true);}
			@Override public void keyPressed(KeyEvent e) {serverUse.setSelected(true);}
		});

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		button=new JButton(Language.tr("SettingsDialog.Tabs.Simulation.Server.CheckConnection"));
		button.setIcon(Images.EXTRAS_SERVER.getIcon());
		button.addActionListener(e->{
			final String key=(serverKey.getText().trim().isEmpty())?null:serverKey.getText().trim();
			final int port=((Integer)serverPort.getValue()).intValue();
			new ServerStatus(serverName.getText(),port,true,key).showMessage(SetupDialog.this);
		});
		p.add(button);

		/*
		mainarea.add(Box.createVerticalStrut(15));
		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(new JLabel("<html><body><b>"+Language.tr("SettingsDialog.Tabs.Compiler")+"</b></body></html>"));
		data=ModelElementBaseDialog.getInputPanel(Language.tr("SettingsDialog.Tabs.Compiler.JDKPath")+":","");
		mainarea.add(p2=(JPanel)data[0]);
		javaJDKpath=(JTextField)data[1];
		final JButton selectFolder=new JButton();
		p2.add(selectFolder,BorderLayout.EAST);
		selectFolder.setToolTipText(Language.tr("SettingsDialog.Tabs.Compiler.SelectFolder"));
		selectFolder.setIcon(Images.SELECT_FOLDER.getIcon());
		selectFolder.addActionListener(e->{
			final String folder=selectFolder(Language.tr("SettingsDialog.Tabs.Compiler.SelectFolder"),javaJDKpath.getText());
			if (folder!=null) javaJDKpath.setText(folder);
		});
		 */

		/* Seite: Animation */

		tabs.add(Language.tr("SettingsDialog.Tabs.Animation"),tab=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		tab.add(mainarea=new JPanel());
		mainarea.setLayout(new BoxLayout(mainarea,BoxLayout.Y_AXIS));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(label=new JLabel(Language.tr("SettingsDialog.AnimationWarmUp")+":"));
		p.add(animationWarmUpMode=new JComboBox<String>(new String[]{
				Language.tr("SettingsDialog.AnimationWarmUp.Normal"),
				Language.tr("SettingsDialog.AnimationWarmUp.Ask"),
				Language.tr("SettingsDialog.AnimationWarmUp.SkipIfNeeded"),
				Language.tr("SettingsDialog.AnimationWarmUp.FastForward")
		}));
		animationWarmUpMode.setRenderer(new IconListCellRenderer(new Images[]{
				Images.SETUP_ANIMATION_START_NORMAL,
				Images.SETUP_ANIMATION_START_ASK,
				Images.SETUP_ANIMATION_START_SKIP,
				Images.SETUP_ANIMATION_START_FAST
		}));
		label.setLabelFor(animationWarmUpMode);

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(showStationData=new JCheckBox(Language.tr("SettingsDialog.Tabs.Simulation.ShowStationData")));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(showSingleStepLogData=new JCheckBox(Language.tr("SettingsDialog.Tabs.Simulation.ShowSingleStepLogData")));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(animationStartPaused=new JCheckBox(Language.tr("SettingsDialog.Tabs.Simulation.AnimationStartPaused")));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(animateResources=new JCheckBox(Language.tr("SettingsDialog.Tabs.Simulation.AnimateResources")));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(useSlowModeAnimation=new JCheckBox(Language.tr("SettingsDialog.Tabs.Simulation.UseSlowModeAnimation")));

		/* Seite: Statistik */

		tabs.add(Language.tr("SettingsDialog.Tabs.Statistics"),tab=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		tab.add(mainarea=new JPanel());
		mainarea.setLayout(new BoxLayout(mainarea,BoxLayout.Y_AXIS));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(new JLabel("<html><body><b>"+Language.tr("SettingsDialog.Tabs.Statistics.View")+"</b></body></html>"));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(showQuantils=new JCheckBox(Language.tr("SettingsDialog.Tabs.Statistics.ShowQuantils")));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(showErlangC=new JCheckBox(Language.tr("SettingsDialog.Tabs.Simulation.ShowErlangC")));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(expandAllStatistics=new JCheckBox(Language.tr("SettingsDialog.Tabs.Simulation.ExpandAllStatistics")));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(label=new JLabel(Language.tr("SettingsDialog.Tabs.Statistics.NumberDigits")+":"));
		final JSpinner statisticsNumberDigitsSpinner=new JSpinner(statisticsNumberDigits=new SpinnerNumberModel(1,1,9,1));
		final JSpinner.NumberEditor statisticsNumberDigitsEditor=new JSpinner.NumberEditor(statisticsNumberDigitsSpinner);
		statisticsNumberDigitsEditor.getFormat().setGroupingUsed(false);
		statisticsNumberDigitsEditor.getTextField().setColumns(2);
		statisticsNumberDigitsSpinner.setEditor(statisticsNumberDigitsEditor);
		p.add(statisticsNumberDigitsSpinner);
		label.setLabelFor(statisticsNumberDigitsSpinner);

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(label=new JLabel(Language.tr("SettingsDialog.Tabs.Statistics.PercentDigits")+":"));
		final JSpinner statisticsNumberPercentSpinner=new JSpinner(statisticsPercentDigits=new SpinnerNumberModel(1,1,9,1));
		final JSpinner.NumberEditor statisticsPercentDigitsEditor=new JSpinner.NumberEditor(statisticsNumberPercentSpinner);
		statisticsPercentDigitsEditor.getFormat().setGroupingUsed(false);
		statisticsPercentDigitsEditor.getTextField().setColumns(2);
		statisticsNumberPercentSpinner.setEditor(statisticsPercentDigitsEditor);
		p.add(statisticsNumberPercentSpinner);
		label.setLabelFor(statisticsNumberPercentSpinner);

		mainarea.add(Box.createVerticalStrut(15));
		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(new JLabel("<html><body><b>"+Language.tr("SettingsDialog.Tabs.Statistics.QuantilLevels")+"</b></body></html>"));

		data=ModelElementBaseDialog.getPlaceholderInputPanel(Language.tr("SettingsDialog.Tabs.Statistics.QuantilLevels.Levels")+":",Language.tr("SettingsDialog.Tabs.Statistics.QuantilLevels.Levels.Placeholder"),"");
		mainarea.add((JPanel)data[0]);
		quantilLevels=(JPlaceholderTextField)data[1];

		mainarea.add(Box.createVerticalStrut(15));
		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(new JLabel("<html><body><b>"+Language.tr("SettingsDialog.Tabs.Statistics.BatchMeansConfidenceLevels")+"</b></body></html>"));

		data=ModelElementBaseDialog.getPlaceholderInputPanel(Language.tr("SettingsDialog.Tabs.Statistics.BatchMeansConfidenceLevels.Levels")+":",Language.tr("SettingsDialog.Tabs.Statistics.BatchMeansConfidenceLevels.Levels.Placeholder"),"");
		mainarea.add((JPanel)data[0]);
		batchMeansConfidenceLevels=(JPlaceholderTextField)data[1];

		mainarea.add(Box.createVerticalStrut(15));
		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(new JLabel("<html><body><b>"+Language.tr("SettingsDialog.Tabs.Statistics.OpenExternal")+"</b></body></html>"));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(openWord=new JCheckBox(Language.tr("SettingsDialog.Tabs.Statistics.OpenWord")));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(openODT=new JCheckBox(Language.tr("SettingsDialog.Tabs.Statistics.OpenODT")));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(openExcel=new JCheckBox(Language.tr("SettingsDialog.Tabs.Statistics.OpenExcel")));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(openODS=new JCheckBox(Language.tr("SettingsDialog.Tabs.Statistics.OpenODS")));

		/* Seite: Dateiformate */

		tabs.add(Language.tr("SettingsDialog.Tabs.Exporting"),tab=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		tab.add(mainarea=new JPanel());
		mainarea.setLayout(new BoxLayout(mainarea,BoxLayout.Y_AXIS));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(new JLabel("<html><body><b>"+Language.tr("SettingsDialog.Tabs.DefaultUserName")+"</b></body></html>"));

		data=ModelElementBaseDialog.getInputPanel(Language.tr("SettingsDialog.Tabs.DefaultUserName.Name")+":","");
		mainarea.add(p=(JPanel)data[0]);
		defaultUserName=(JTextField)data[1];
		p.add(button=new JButton(Language.tr("SettingsDialog.Tabs.DefaultUserName.Reset")),BorderLayout.EAST);
		button.setIcon(Images.MODELPROPERTIES_DESCRIPTION_SET_AUTHOR.getIcon());
		button.setToolTipText(String.format(Language.tr("SettingsDialog.Tabs.DefaultUserName.Reset.Info"),System.getProperty("user.name")));
		button.addActionListener(e->defaultUserName.setText(System.getProperty("user.name")));

		mainarea.add(Box.createVerticalStrut(15));
		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(new JLabel("<html><body><b>"+Language.tr("SettingsDialog.Tabs.DefaultFormats")+"</b></body></html>"));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(label=new JLabel(Language.tr("SettingsDialog.Tabs.DefaultFormats.Models")+":"));
		p.add(defaultSaveFormatModels=new JComboBox<String>(new String[]{
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

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(label=new JLabel(Language.tr("SettingsDialog.Tabs.DefaultFormats.Statistics")+":"));
		p.add(defaultSaveFormatStatistics=new JComboBox<String>(new String[]{
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

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(label=new JLabel(Language.tr("SettingsDialog.Tabs.DefaultFormats.ParameterSeries")+":"));
		p.add(defaultSaveFormatParameterSeries=new JComboBox<String>(new String[]{
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

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(label=new JLabel(Language.tr("SettingsDialog.Tabs.DefaultFormats.OptimizerSetups")+":"));
		p.add(defaultSaveFormatOptimizerSetups=new JComboBox<String>(new String[]{
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

		mainarea.add(Box.createVerticalStrut(15));
		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(new JLabel("<html><body><b>"+Language.tr("SettingsDialog.Tabs.ExcelDDEConnect")+"</b></body></html>"));

		data=ModelElementBaseDialog.getInputPanel(Language.tr("SettingsDialog.ExcelDDEConnection.CustomRow")+":","",2);
		mainarea.add(p=(JPanel)data[0]);
		excelRow=(JTextField)data[1];
		p.add(new JLabel(Language.tr("SettingsDialog.ExcelDDEConnection.CustomRow.Info")));

		data=ModelElementBaseDialog.getInputPanel(Language.tr("SettingsDialog.ExcelDDEConnection.CustomColumn")+":","",2);
		mainarea.add(p=(JPanel)data[0]);
		excelCol=(JTextField)data[1];
		p.add(new JLabel(Language.tr("SettingsDialog.ExcelDDEConnection.CustomColumn.Info")));

		mainarea.add(Box.createVerticalStrut(15));
		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(new JLabel("<html><body><b>"+Language.tr("SettingsDialog.Tabs.ExportGraphics")+"</b></body></html>"));

		data=ModelElementBaseDialog.getInputPanel(Language.tr("SettingsDialog.ImageResolution")+":","",5);
		mainarea.add((JPanel)data[0]);
		imageSize=(JTextField)data[1];
		imageSize.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {NumberTools.getPositiveLong(imageSize,true);}
			@Override public void keyReleased(KeyEvent e) {NumberTools.getPositiveLong(imageSize,true);}
			@Override public void keyPressed(KeyEvent e) {NumberTools.getPositiveLong(imageSize,true);}
		});

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(new JLabel(Language.tr("SettingsDialog.ImageAnimation")+":"));
		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(imagesAnimationHome=new JRadioButton(Language.tr("SettingsDialog.ImageAnimation.Home")+" ("+FileSystemView.getFileSystemView().getHomeDirectory()+")"));
		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(imagesAnimationFolder=new JRadioButton(Language.tr("SettingsDialog.ImageAnimation.Folder")+":"));
		mainarea.add(p=new JPanel(new BorderLayout()));
		p.add(imagesAnimationFolderEdit=new JTextField(),BorderLayout.CENTER);
		imagesAnimationFolderEdit.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {imagesAnimationFolder.setSelected(true);}
			@Override public void keyReleased(KeyEvent e) {imagesAnimationFolder.setSelected(true);}
			@Override public void keyPressed(KeyEvent e) {imagesAnimationFolder.setSelected(true);}
		});
		p.add(imagesAnimationFolderButton=new JButton(""),BorderLayout.EAST);
		imagesAnimationFolderButton.setToolTipText(Language.tr("SettingsDialog.ImageAnimation.Folder.Select"));
		imagesAnimationFolderButton.setIcon(Images.GENERAL_SELECT_FOLDER.getIcon());
		imagesAnimationFolderButton.addActionListener(new ButtonListener());

		final ButtonGroup buttonGroup=new ButtonGroup();
		buttonGroup.add(imagesAnimationHome);
		buttonGroup.add(imagesAnimationFolder);

		mainarea.add(Box.createVerticalStrut(15));
		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(new JLabel("<html><body><b>"+Language.tr("SettingsDialog.Tabs.ExportVideos")+"</b></body></html>"));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(paintTimeStamp=new JCheckBox(Language.tr("SettingsDialog.Tabs.Simulation.PaintTimeStamp")));

		/* Seite: Update */

		tabs.add(Language.tr("SettingsDialog.Tabs.Updates"),tab=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		tab.add(mainarea=new JPanel());
		mainarea.setLayout(new BoxLayout(mainarea,BoxLayout.Y_AXIS));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(programStartJavaCheck=new JCheckBox(Language.tr("SettingsDialog.TestJavaVersionOnProgramStart")));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(new JLabel(Language.tr("SettingsDialog.JDK.Info")+": "));
		p.add(label=new JLabel("<html><body><a href=\""+Language.tr("SettingsDialog.JDK.Link")+"\">"+Language.tr("SettingsDialog.JDK.Link")+"</a></body></html>")); // "https://adoptopenjdk.net/"
		label.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
		label.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) {
					try {
						final String url=Language.tr("SettingsDialog.JDK.Link");
						if (!MsgBox.confirmOpenURL(SetupDialog.this,url)) return;
						Desktop.getDesktop().browse(new URI(url));
					} catch (IOException | URISyntaxException e1) {}
				}
			}
		});

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(autoUpdate=new JCheckBox(Language.tr("SettingsDialog.AutoUpdate")));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(updateInfo=new JLabel());

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		button=new JButton(Language.tr("SettingsDialog.ProxySettings"));
		button.setIcon(Images.SETUP_PROXY.getIcon());
		button.addActionListener(e->showProxySettingsDialog());
		p.add(button);

		p.add(manualUpdateButton=new JButton(Language.tr("SettingsDialog.ManualUpdate")));
		manualUpdateButton.setIcon(Images.SETUP_PAGE_UPDATE.getIcon());
		manualUpdateButton.setVisible(false);
		manualUpdateButton.addActionListener(e->showManualUpdateMenu());

		p.add(updateCheckButton=new JButton(Language.tr("SettingsDialog.UpdateCheck")));
		updateCheckButton.setIcon(Images.SETUP_PAGE_UPDATE.getIcon());
		updateCheckButton.setVisible(false);
		updateCheckButton.addActionListener(e->runUpdateCheck());

		/* Icons auf den Tabreitern einfügen */

		tabs.setIconAt(0,Images.SETUP_PAGE_APPLICATION.getIcon());
		tabs.setIconAt(1,Images.SETUP_PAGE_PERFORMANCE.getIcon());
		tabs.setIconAt(2,Images.SETUP_PAGE_ANIMATION.getIcon());
		tabs.setIconAt(3,Images.SETUP_PAGE_STATISTICS.getIcon());
		tabs.setIconAt(4,Images.SETUP_PAGE_FILE_FORMATS.getIcon());
		tabs.setIconAt(5,Images.SETUP_PAGE_UPDATE.getIcon());

		/* Daten in den Dialog laden */

		SetupData setup=SetupData.getSetup();

		/* Seite: Benutzeroberfläche */

		if (setup.language==null || setup.language.isEmpty() || setup.language.equalsIgnoreCase("de")) languages.setSelectedIndex(1); else languages.setSelectedIndex(0);

		fontSizes.setSelectedIndex(1);
		if (setup.scaleGUI<1) fontSizes.setSelectedIndex(0);
		if (setup.scaleGUI>1) fontSizes.setSelectedIndex(2);
		if (setup.scaleGUI>1.1) fontSizes.setSelectedIndex(3);
		if (setup.scaleGUI>1.3) fontSizes.setSelectedIndex(4);
		useHighContrasts.setSelected(setup.useHighContrasts);
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

		switch (setup.modelSecurity) {
		case ALLOWALL: modelSecurity.setSelectedIndex(0); break;
		case ASK: modelSecurity.setSelectedIndex(1); break;
		case STRICT: modelSecurity.setSelectedIndex(2); break;
		}

		switch (setup.surfaceHelp) {
		case NEVER: surfaceHelp.setSelectedIndex(0); break;
		case START_ONLY: surfaceHelp.setSelectedIndex(1); break;
		case ALWAYS: surfaceHelp.setSelectedIndex(2); break;
		}

		switch (setup.notifyMode) {
		case OFF: notifyMode.setSelectedIndex(0); break;
		case LONGRUN: notifyMode.setSelectedIndex(1); break;
		case ALWAYS: notifyMode.setSelectedIndex(2); break;
		}
		hintDialogs=setup.hintDialogs;

		/* Seite: Leistung */

		switch (setup.backgroundSimulation) {
		case BACKGROUND_NOTHING: backgroundProcessing.setSelectedIndex(0); break;
		case BACKGROUND_CHECK_ONLY: backgroundProcessing.setSelectedIndex(1); break;
		case BACKGROUND_SIMULATION: backgroundProcessing.setSelectedIndex(2); break;
		case BACKGROUND_SIMULATION_ALWAYS: backgroundProcessing.setSelectedIndex(3); break;
		}

		useMultiCoreSimulation.setSelected(setup.useMultiCoreSimulation);
		useMultiCoreAnimation.setSelected(setup.useMultiCoreAnimation);
		highPriority.setSelected(setup.highPriority);
		useNUMAMode.setSelected(setup.useNUMAMode);
		JSEngineNames engine=JSEngineNames.fromName(setup.jsEngine);
		canelSimulationOnScriptError.setSelected(setup.cancelSimulationOnScriptError);
		if (engine==null) engine=JSEngineNames.DEFAULT;
		switch (engine) {
		case DEFAULT: jsEngine.setSelectedIndex(0); break;
		case NASHORN: jsEngine.setSelectedIndex(1); break;
		case RHINO: jsEngine.setSelectedIndex(2); break;
		case GRAALJS:
		case GRAALJSNative: jsEngine.setSelectedIndex(3); break;
		}

		serverUse.setSelected(setup.serverUse);
		if (setup.serverData!=null) {
			final String[] parts=setup.serverData.split(":");
			if (parts.length>=2 && parts.length<=3) {
				serverName.setText(parts[0]);
				final Long L=NumberTools.getPositiveLong(parts[1]);
				if (L!=null) serverPort.setValue(L.intValue());
				serverPort.addChangeListener(e->serverUse.setSelected(true));
				if (parts.length==3) serverKey.setText(parts[2]);
			}
		}

		/*
		javaJDKpath.setText(setup.javaJDKPath);
		 */

		/* Seite: Animation */

		switch (setup.animationWarmUpMode) {
		case ANIMATION_WARMUP_NORMAL: animationWarmUpMode.setSelectedIndex(0); break;
		case ANIMATION_WARMUP_ASK: animationWarmUpMode.setSelectedIndex(1); break;
		case ANIMATION_WARMUP_SKIP: animationWarmUpMode.setSelectedIndex(2); break;
		case ANIMATION_WARMUP_FAST: animationWarmUpMode.setSelectedIndex(3); break;
		}
		showStationData.setSelected(setup.showStationRunTimeData);
		showSingleStepLogData.setSelected(setup.showSingleStepLogData);
		animationStartPaused.setSelected(setup.animationStartPaused);
		animateResources.setSelected(setup.animateResources);
		useSlowModeAnimation.setSelected(setup.useSlowModeAnimation);

		/* Seite Statistik */

		showQuantils.setSelected(setup.showQuantils);
		showErlangC.setSelected(setup.showErlangC);
		expandAllStatistics.setSelected(setup.expandAllStatistics);
		statisticsNumberDigits.setValue(setup.statisticsNumberDigits);
		statisticsPercentDigits.setValue(setup.statisticsPercentDigits);
		quantilLevels.setText(setup.quantilLevels);
		batchMeansConfidenceLevels.setText(setup.batchMeansConfidenceLevels);
		openWord.setSelected(setup.openWord);
		openODT.setSelected(setup.openODT);
		openExcel.setSelected(setup.openExcel);
		openODS.setSelected(setup.openODS);

		/* Seite: Dateiformate */

		defaultUserName.setText(setup.defaultUserName);
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

		/* Seite: Updates */

		final UpdateSystem updateSystem=UpdateSystem.getUpdateSystem();
		programStartJavaCheck.setSelected(setup.testJavaVersion);
		autoUpdate.setEnabled(updateSystem.isAutomaticUpdatePossible());
		autoUpdate.setSelected(autoUpdate.isEnabled() && setup.autoUpdate);
		updateInfo.setText("<html><b>"+updateSystem.getInfoString()+"</b></html>");
		if (setup.autoUpdate && updateSystem.isAutomaticUpdatePossible()) {
			runUpdateCheck();
		} else {
			updateCheckButton.setVisible(true);
		}

		/* Dialog anzeigen */

		if (showUpdatesPage) {
			tabs.setSelectedIndex(5);
		}

		setMinSizeRespectingScreensize(650,0);
		pack();
		setLocationRelativeTo(this.owner);
		setVisible(true);
	}

	private void runUpdateCheck() {
		updateInfo.setText("<html><b>"+Language.tr("SettingsDialog.Tabs.Updates.ConnectingServer")+"</b></html>");
		updateCheckButton.setVisible(false);
		final UpdateSystem updateSystem=UpdateSystem.getUpdateSystem();
		new Thread(()->{
			updateSystem.checkUpdateNow(true);
			updateInfo.setText("<html><b>"+updateSystem.getInfoString()+"</b></html>");
			final Timer timer=new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					if (!updateSystem.isLoading()) {
						timer.cancel();
						if (updateSystem.isNewVersionAvailable()==UpdateSystem.NewVersionAvailableStatus.NEW_VERSION_AVAILABLE) {
							manualUpdateButton.setVisible(true);
						}
					}
					updateInfo.setText("<html><b>"+updateSystem.getInfoString()+"</b></html>");
				}
			},1000,1000);
		}).start();
	}

	@Override
	protected boolean checkData() {
		final Long L=NumberTools.getPositiveLong(imageSize,true);
		if (L==null || L<50 || L>5000) {
			MsgBox.error(owner,Language.tr("SettingsDialog.ImageResolution.Invalid.Title"),Language.tr("SettingsDialog.ImageResolution.Invalid.Info"));
			return false;
		}
		return true;
	}

	@Override
	protected void storeData() {
		SetupData setup=SetupData.getSetup();

		/* Seite: Benutzeroberfläche */

		setup.language=(languages.getSelectedIndex()==1)?"de":"en";

		switch (fontSizes.getSelectedIndex()) {
		case 0: setup.scaleGUI=0.9; break;
		case 1: setup.scaleGUI=1; break;
		case 2: setup.scaleGUI=1.1; break;
		case 3: setup.scaleGUI=1.25; break;
		case 4: setup.scaleGUI=1.5; break;
		}
		setup.useHighContrasts=useHighContrasts.isSelected();
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
		switch (modelSecurity.getSelectedIndex()) {
		case 0: setup.modelSecurity=SetupData.ModelSecurity.ALLOWALL; break;
		case 1: setup.modelSecurity=SetupData.ModelSecurity.ASK; break;
		case 2: setup.modelSecurity=SetupData.ModelSecurity.STRICT; break;
		}
		switch (surfaceHelp.getSelectedIndex()) {
		case 0: setup.surfaceHelp=SetupData.SurfaceHelp.NEVER; break;
		case 1: setup.surfaceHelp=SetupData.SurfaceHelp.START_ONLY; break;
		case 2: setup.surfaceHelp=SetupData.SurfaceHelp.ALWAYS; break;
		}
		switch (notifyMode.getSelectedIndex()) {
		case 0: setup.notifyMode=SetupData.NotifyMode.OFF; break;
		case 1: setup.notifyMode=SetupData.NotifyMode.LONGRUN; break;
		case 2: setup.notifyMode=SetupData.NotifyMode.ALWAYS; break;
		}
		setup.hintDialogs=hintDialogs;
		InfoPanel.getInstance().loadSetup(setup.hintDialogs);

		/* Seite: Leistung */

		switch (backgroundProcessing.getSelectedIndex()) {
		case 0: setup.backgroundSimulation=SetupData.BackgroundProcessingMode.BACKGROUND_NOTHING; break;
		case 1: setup.backgroundSimulation=SetupData.BackgroundProcessingMode.BACKGROUND_CHECK_ONLY; break;
		case 2: setup.backgroundSimulation=SetupData.BackgroundProcessingMode.BACKGROUND_SIMULATION; break;
		case 3: setup.backgroundSimulation=SetupData.BackgroundProcessingMode.BACKGROUND_SIMULATION_ALWAYS; break;
		}

		setup.useMultiCoreSimulation=useMultiCoreSimulation.isSelected();
		setup.useMultiCoreAnimation=useMultiCoreAnimation.isSelected();
		setup.highPriority=highPriority.isSelected();
		setup.useNUMAMode=useNUMAMode.isSelected();
		switch (jsEngine.getSelectedIndex()) {
		case 0: setup.jsEngine=""; break;
		case 1: setup.jsEngine=JSEngineNames.NASHORN.name; break;
		case 2: setup.jsEngine=JSEngineNames.RHINO.name; break;
		case 3: setup.jsEngine=JSEngineNames.GRAALJSNative.name; break;
		}
		setup.cancelSimulationOnScriptError=canelSimulationOnScriptError.isSelected();

		setup.serverUse=serverUse.isSelected();
		final StringBuilder sb=new StringBuilder();
		sb.append(serverName.getText().trim());
		sb.append(":");
		sb.append(serverPort.getValue());
		final String key=serverKey.getText().trim();
		if (!key.isEmpty()) {
			sb.append(":");
			sb.append(key);
		}
		setup.serverData=sb.toString();

		/*
		setup.javaJDKPath=javaJDKpath.getText();
		 */

		/* Seite: Animation */

		switch (animationWarmUpMode.getSelectedIndex()) {
		case 0: setup.animationWarmUpMode=SetupData.AnimationMode.ANIMATION_WARMUP_NORMAL; break;
		case 1: setup.animationWarmUpMode=SetupData.AnimationMode.ANIMATION_WARMUP_ASK; break;
		case 2: setup.animationWarmUpMode=SetupData.AnimationMode.ANIMATION_WARMUP_SKIP; break;
		case 3: setup.animationWarmUpMode=SetupData.AnimationMode.ANIMATION_WARMUP_FAST; break;
		}
		setup.showStationRunTimeData=showStationData.isSelected();
		setup.showSingleStepLogData=showSingleStepLogData.isSelected();
		setup.animationStartPaused=animationStartPaused.isSelected();
		setup.animateResources=animateResources.isSelected();
		setup.useSlowModeAnimation=useSlowModeAnimation.isSelected();

		/* Seite: Statistik */

		setup.showQuantils=showQuantils.isSelected();
		setup.showErlangC=showErlangC.isSelected();
		setup.expandAllStatistics=expandAllStatistics.isSelected();
		setup.statisticsNumberDigits=((Integer)statisticsNumberDigits.getValue()).intValue();
		setup.statisticsPercentDigits=((Integer)statisticsPercentDigits.getValue()).intValue();
		setup.quantilLevels=quantilLevels.getText();
		setup.batchMeansConfidenceLevels=batchMeansConfidenceLevels.getText();
		setup.openWord=openWord.isSelected();
		setup.openODT=openODT.isSelected();
		setup.openExcel=openExcel.isSelected();
		setup.openODS=openODS.isSelected();

		/* Seite: Dateiformate */

		setup.defaultUserName=defaultUserName.getText().trim();
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

		/* Seite: Updates */

		setup.testJavaVersion=programStartJavaCheck.isSelected();
		setup.autoUpdate=autoUpdate.isSelected();

		/* Einstellungen speichern */

		setup.saveSetupWithWarning(this);
	}

	private String selectFolder(final String title, final String oldFolder) {
		final JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		if (oldFolder!=null && !oldFolder.trim().isEmpty() && new File(oldFolder).isDirectory()) {
			fc.setCurrentDirectory(new File(oldFolder));
		}
		fc.setDialogTitle(title);
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (fc.showSaveDialog(owner)!=JFileChooser.APPROVE_OPTION) return null;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		final File file=fc.getSelectedFile();
		return file.toString();
	}

	private void showHintsDialog() {
		final InfoPanelDialog dialog=new InfoPanelDialog(this,hintDialogs);
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) hintDialogs=dialog.getData();
	}

	private void showProxySettingsDialog() {
		new ProxyDialog(this);
	}

	private void showManualUpdateMenu() {
		final JPopupMenu menu=new JPopupMenu();
		JMenuItem item;

		menu.add(item=new JMenuItem(Language.tr("SettingsDialog.ManualUpdate.Homepage")));
		item.setIcon(Images.HELP_HOMEPAGE.getIcon());
		item.addActionListener(e->{
			final String network="https://"+MainPanel.WEB_URL;
			try {
				if (!MsgBox.confirmOpenURL(this,network)) return;
				Desktop.getDesktop().browse(new URI(network));
			} catch (IOException | URISyntaxException e1) {
				MsgBox.error(getOwner(),Language.tr("Window.Info.NoInternetConnection"),String.format(Language.tr("Window.Info.NoInternetConnection.Address"),network));
			}
		});
		menu.add(item=new JMenuItem(Language.tr("SettingsDialog.ManualUpdate.Download")));
		item.setIcon(Images.GENERAL_SAVE.getIcon());
		item.addActionListener(e->{
			final JFileChooser fc=new JFileChooser();
			CommonVariables.initialDirectoryToJFileChooser(fc);
			fc.setDialogTitle(Language.tr("SettingsDialog.ManualUpdate.Download.Folder"));
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if (fc.showSaveDialog(owner)!=JFileChooser.APPROVE_OPTION) return;
			CommonVariables.initialDirectoryFromJFileChooser(fc);
			final File file=fc.getSelectedFile();
			if (file==null) return;
			final UpdateSystem updateSystem=UpdateSystem.getUpdateSystem();
			updateSystem.downloadUpdateToFolder(file);
			updateInfo.setText("<html><b>"+updateSystem.getInfoString()+"</b></html>");
			manualUpdateButton.setVisible(false);
			final Timer timer=new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					if (!updateSystem.isLoading()) {
						timer.cancel();
						manualUpdateButton.setVisible(true);
					}
					updateInfo.setText("<html><b>"+updateSystem.getInfoString()+"</b></html>");
				}
			},1000,1000);
		});
		menu.show(manualUpdateButton,0,manualUpdateButton.getHeight());
	}

	@Override
	protected void userButtonClick(final int nr, final JButton button) {
		switch (tabs.getSelectedIndex()) {
		case 0: /* Seite: Benutzeroberfläche */
			fontSizes.setSelectedIndex(1);
			useHighContrasts.setSelected(false);
			autoSave.setSelectedIndex(0);
			autoRestore.setSelected(false);
			programStartWindow.setSelectedIndex(0);
			templateStartMode.setSelectedIndex(0);
			startModel.setSelectedIndex(0);
			modelSecurity.setSelectedIndex(1);
			notifyMode.setSelectedIndex(1);
			hintDialogs="";
			break;
		case 1: /* Seite: Leistung */
			backgroundProcessing.setSelectedIndex(2);
			useMultiCoreSimulation.setSelected(true);
			useMultiCoreAnimation.setSelected(true);
			highPriority.setSelected(false);
			useNUMAMode.setSelected(false);
			jsEngine.setSelectedIndex(0);
			canelSimulationOnScriptError.setSelected(true);
			serverPort.setValue(8183);
			serverUse.setSelected(false);
			break;
		case 2: /* Seite: Animation */
			animationWarmUpMode.setSelectedIndex(2);
			showStationData.setSelected(true);
			showSingleStepLogData.setSelected(true);
			animationStartPaused.setSelected(false);
			animateResources.setSelected(true);
			useSlowModeAnimation.setSelected(true);
			break;
		case 3: /* Seite: Statistik */
			showQuantils.setSelected(true);
			showErlangC.setSelected(true);
			expandAllStatistics.setSelected(false);
			statisticsNumberDigits.setValue(1);
			statisticsPercentDigits.setValue(1);
			quantilLevels.setText("");
			batchMeansConfidenceLevels.setText("");
			openWord.setSelected(true);
			openODT.setSelected(false);
			openExcel.setSelected(true);
			openODS.setSelected(false);
			break;
		case 4: /* Seite: Dateiformate */
			defaultUserName.setText(System.getProperty("user.name"));
			defaultSaveFormatModels.setSelectedIndex(0);
			defaultSaveFormatStatistics.setSelectedIndex(0);
			defaultSaveFormatParameterSeries.setSelectedIndex(2);
			defaultSaveFormatOptimizerSetups.setSelectedIndex(2);
			excelRow.setText("");
			excelCol.setText("");
			imageSize.setText("2000");
			imagesAnimationHome.setSelected(true);
			paintTimeStamp.setSelected(true);
			break;
		case 6: /* Seite: Updates */
			programStartJavaCheck.setSelected(true);
			if (autoUpdate.isEnabled()) autoUpdate.setSelected(true);
			break;
		}
	}

	private class ButtonListener implements ActionListener {
		private void selectImagesFolder() {
			final String folder=selectFolder(Language.tr("Batch.Output.Folder.Button.Hint"),imagesAnimationFolderEdit.getText());
			if (folder!=null) {
				imagesAnimationFolderEdit.setText(folder);
				imagesAnimationFolder.setSelected(true);
			}
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource()==imagesAnimationFolderButton) {
				selectImagesFolder();
				return;
			}
		}
	}
}
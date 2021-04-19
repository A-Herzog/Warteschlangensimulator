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

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.SystemColor;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileSystemView;

import org.w3c.dom.Element;

import gitconnect.GitListDialog;
import gitconnect.GitTools;
import language.Language;
import mathtools.NumberTools;
import mathtools.Table;
import mathtools.Table.SaveMode;
import mathtools.distribution.swing.CommonVariables;
import mathtools.distribution.tools.AbstractDistributionWrapper;
import mathtools.distribution.tools.FileDropperData;
import net.calc.ServerStatus;
import scripting.java.ExternalConnectDialog;
import simcore.logging.SimLogging;
import simulator.AnySimulator;
import simulator.Simulator;
import simulator.StartAnySimulator;
import simulator.db.DBConnectDialog;
import simulator.editmodel.EditModel;
import simulator.editmodel.EditModelDark;
import simulator.examples.EditModelExamples;
import simulator.logging.CallbackLoggerWithJS;
import simulator.runmodel.RunModel;
import simulator.statistics.Statistics;
import systemtools.BaseDialog;
import systemtools.MainFrameBase;
import systemtools.MainPanelBase;
import systemtools.MsgBox;
import systemtools.commandline.CommandLineDialog;
import systemtools.help.HelpBase;
import systemtools.statistics.StatisticsBasePanel;
import tools.ImagePrintable;
import tools.Notifier;
import tools.SetupData;
import tools.URLLoader;
import tools.UsageStatistics;
import ui.calculator.CalculatorWindow;
import ui.calculator.QueueingCalculatorDialog;
import ui.commandline.CommandLineSystem;
import ui.compare.ComparePanel;
import ui.compare.CompareSelectDialog;
import ui.dialogs.AnimationRecordSetupDialog;
import ui.dialogs.DataCheckDialog;
import ui.dialogs.EdgeStyleSetupDialog;
import ui.dialogs.FindBatchSizeDialog;
import ui.dialogs.FindElementDialog;
import ui.dialogs.FitDialog;
import ui.dialogs.HeatMapSelectWindow;
import ui.dialogs.HeatMapSetupDialog;
import ui.dialogs.InfoDialog;
import ui.dialogs.LicenseViewer;
import ui.dialogs.LogSetupDialog;
import ui.dialogs.ModelAnalyticInfoDialog;
import ui.dialogs.ModelDescriptionDialog;
import ui.dialogs.ModelLoadDataWarningsDialog;
import ui.dialogs.NotesDialog;
import ui.dialogs.SelectElementByIdDialog;
import ui.dialogs.SelectExampleDialog;
import ui.dialogs.SetupDialog;
import ui.dialogs.StationStatisticsDialog;
import ui.dialogs.SystemInfoWindow;
import ui.dialogs.VarianceAnalysisDialog;
import ui.dialogs.WaitDialog;
import ui.generator.ModelGeneratorDialog;
import ui.help.AutomaticHelpWindow;
import ui.help.BookData;
import ui.help.BookDataDialog;
import ui.help.Help;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.AnimationImageDialog;
import ui.modeleditor.ModelLoadData;
import ui.modeleditor.ModelLoadDataDialog;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurfacePanel;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.templates.EditTemplateDialog;
import ui.modeleditor.templates.TemplatesListDialog;
import ui.optimizer.OptimizerPanel;
import ui.optimizer.OptimizerPanelPrepareDialog;
import ui.optimizer.OptimizerSetup;
import ui.parameterseries.ParameterComparePanel;
import ui.parameterseries.ParameterCompareSetup;
import ui.parameterseries.ParameterCompareTemplatesDialog;
import ui.quickaccess.JQuickAccess;
import ui.quickaccess.JQuickAccessBuilder;
import ui.quickaccess.JQuickAccessBuilderBook;
import ui.quickaccess.JQuickAccessBuilderCalc;
import ui.quickaccess.JQuickAccessBuilderDistributions;
import ui.quickaccess.JQuickAccessBuilderElementsList;
import ui.quickaccess.JQuickAccessBuilderExamples;
import ui.quickaccess.JQuickAccessBuilderLastFiles;
import ui.quickaccess.JQuickAccessBuilderMenu;
import ui.quickaccess.JQuickAccessBuilderModelProperties;
import ui.quickaccess.JQuickAccessBuilderNewElements;
import ui.quickaccess.JQuickAccessBuilderSettings;
import ui.quickaccess.JQuickAccessBuilderStatistics;
import ui.quickaccess.JQuickAccessRecord;
import ui.quickaccess.JQuickAccessTextField;
import ui.scriptrunner.JSModelRunnerPanel;
import ui.speedup.BackgroundPrepareCompiledClasses;
import ui.speedup.BackgroundSystem;
import ui.statistics.StatisticsPanel;
import ui.statistics.analyticcompare.AnalyticInfo;
import ui.tools.FlatLaFHelper;
import ui.tools.GlassInfo;
import ui.tools.ServerPanel;
import ui.tools.SpecialPanel;
import ui.tools.SwingStartUpWatchDog;
import ui.tools.WaitPanel;
import ui.tutorial.TutorialWindow;
import xml.XMLTools;

/**
 * Diese Klasse stellt den Arbeitsbereich innerhalb des Programmfensters dar.
 *
 * @see MainPanelBase
 * @author Alexander Herzog
 */
public class MainPanel extends MainPanelBase {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 7636118203704616559L;

	/**
	 * Name des Autors
	 */
	public static final String AUTHOR="Alexander Herzog";

	/**
	 * E-Mail-Adresse des Autors
	 */
	public static final String AUTHOR_EMAIL="alexander.herzog@tu-clausthal.de";

	/**
	 * Basis-Homepage-Adresse für das Programm
	 */
	public static final String HOME_URL="a-herzog.github.io";

	/**
	 * Homepage-Adresse für Webbrowseraufrufe
	 */
	public static final String WEB_URL="github.com/A-Herzog/Warteschlangensimulator";

	/**
	 * Basis-Homepage-Adresse für das Updates
	 */
	public static final String UPDATE_URL="github.com/A-Herzog/Warteschlangensimulator/releases/latest/download";

	/**
	 * Adresse zum Abfragen der jeweils neuesten Version
	 */
	public static final String UPDATE_API_URL="api.github.com/repos/A-Herzog/Warteschlangensimulator/releases/latest";

	/**
	 * Programmversion
	 */
	public static final String VERSION="4.8.0";

	/** Aktuelle Unterversionsnummer in der Java 8 Versionsreihe */
	private static final int JAVA8_SECURE_MIN_VERSION=282;
	/** Aktuelle Unterversionsnummer in der Java 9 Versionsreihe */
	private static final int JAVA9_SECURE_MIN_VERSION=4;
	/** Aktuelle Unterversionsnummer in der Java 10 Versionsreihe */
	private static final int JAVA10_SECURE_MIN_VERSION=2;
	/** Aktuelle Unterversionsnummer in der Java 11 Versionsreihe */
	private static final int JAVA11_SECURE_MIN_VERSION=10;
	/** Aktuelle Unterversionsnummer in der Java 12 Versionsreihe */
	private static final int JAVA12_SECURE_MIN_VERSION=2;
	/** Aktuelle Unterversionsnummer in der Java 13 Versionsreihe */
	private static final int JAVA13_SECURE_MIN_VERSION=2;
	/** Aktuelle Unterversionsnummer in der Java 14 Versionsreihe */
	private static final int JAVA14_SECURE_MIN_VERSION=2;
	/** Aktuelle Unterversionsnummer in der Java 15 Versionsreihe */
	private static final int JAVA15_SECURE_MIN_VERSION=2;
	/** Aktuelle Unterversionsnummer in der Java 16 Versionsreihe */
	private static final int JAVA16_SECURE_MIN_VERSION=0;

	/**
	 * Bezeichnung für "ungespeichertes Modell" in der Titelzeile für ein neues Modell, welches noch keinen Namen besitzt
	 */
	public static String UNSAVED_MODEL="ungespeichertes Modell";

	/** Schaltflächen/Menüpunkte, die nur bei Anzeige des Statistik-Viewers aktiviert werden sollen */
	private List<JMenuItem> enabledOnEditorPanel;
	/** Schaltflächen, die nur bei Anzeige des Modelleditors sichtbar sein sollen */
	private List<JButton> visibleOnEditorPanel;
	/** Schaltflächen, die nur bei Anzeige des Statistik-Viewers sichtbar sein sollen */
	private List<JButton> visibleOnStatisticsPanel;
	/** Schaltflächen/Menüpunkte, die bei Anzeige des Modell-Editors markiert dargestellt werden sollen */
	private List<AbstractButton> selectOnEditorPanel;
	/** Schaltflächen/Menüpunkte, die bei Anzeige des Statistik-Viewers markiert dargestellt werden sollen */
	private List<AbstractButton> selectOnStatisticsPanel;
	/** Schaltflächen/Menüpunkte, die bei aktiviert dargestellt werden sollen, wenn Statistikdaten verfügbar sind */
	private List<AbstractButton> enabledOnStatisticsAvailable;

	/** Menüpunkt "Datei" - "Zuletzt verwendet" */
	private JMenu menuFileModelRecentlyUsed;
	/** Menüpunkt "Bearbeiten" - "Rückgängig" */
	private JMenuItem menuEditUndo;
	/** Menüpunkt "Bearbeiten" - "Wiederholen" */
	private JMenuItem menuEditRedo;
	/** Menüpunkt "Bearbeiten" - "Rückgängig/Wiederholen-Schritt auswählen" */
	private JMenuItem menuEditUndoRedoSelect;
	/** Menüpunkt "Bearbeiten" - "Neue Elemente automatisch verbinden" - "Aus" */
	private JRadioButtonMenuItem menuEditAutoConnectOff;
	/** Menüpunkt "Bearbeiten" - "Neue Elemente automatisch verbinden" - "Mit zuletzt markiertem Element verbinden" */
	private JRadioButtonMenuItem menuEditAutoConnectAuto;
	/** Menüpunkt "Bearbeiten" - "Neue Elemente automatisch verbinden" - "Mit nächstgelegenem Element verbinden" */
	private JRadioButtonMenuItem menuEditAutoConnectSmart;
	/** Menüpunkt "Bearbeiten" - "Stationen beim Kopieren umbenennen" - "Aus" */
	private JRadioButtonMenuItem menuEditRenameOnCopyOff;
	/** Menüpunkt "Bearbeiten" - "Stationen beim Kopieren umbenennen" - "Wenn Stationsname mit Zahl endet" */
	private JRadioButtonMenuItem menuEditRenameOnCopySmart;
	/** Menüpunkt "Bearbeiten" - "Stationen beim Kopieren umbenennen" - "Wenn Station einen Namen hat" */
	private JRadioButtonMenuItem menuEditRenameOnCopyAlways;
	/** Menüpunkt "Ansicht" - "Elementenvorlagen Seitenleiste anzeigen" */
	private JCheckBoxMenuItem menuViewTemplatesBar;
	/** Menüpunkt "Ansicht" - "Navigator anzeigen" */
	private JCheckBoxMenuItem menuViewNavigator;
	/** Menüpunkt "Ansicht" - "Lineale anzeigen" */
	private JCheckBoxMenuItem menuViewRulers;
	/** Menüpunkt "Ansicht" - "Raster" - "Kein Raster" */
	private JRadioButtonMenuItem menuViewGridOff;
	/** Menüpunkt "Ansicht" - "Raster" - "Punktraster" */
	private JRadioButtonMenuItem menuViewGridDots;
	/** Menüpunkt "Ansicht" - "Raster" - "Linienraster" */
	private JRadioButtonMenuItem menuViewGridLines;
	/** Menüpunkt "Ansicht" - "Statistik auf Zeichenfläche" - "Statistikinformationen in Tooltips" */
	private JCheckBoxMenuItem menuViewStatisticsTooltips;
	/** Menüpunkt "Ansicht" - "Statistik auf Zeichenfläche" -Heatmap-Modi */
	private List<JRadioButtonMenuItem> menuViewStatisticsHeatMapMode;
	/** Menüpunkt "Ansicht" - "IDs anzeigen" */
	private JCheckBoxMenuItem menuViewShowIDs;
	/** Menüpunkt "Ansicht" - "Stationsbeschreibungen in Tooltips" */
	private JCheckBoxMenuItem menuViewShowStationDescription;
	/** Menüpunkt "Ansicht" - "Farbverläufe verwenden" */
	private JCheckBoxMenuItem menuViewGradients;
	/** Menüpunkt "Ansicht" - "Schatten an den Stationen anzeigen" */
	private JCheckBoxMenuItem menuViewShadows;
	/** Menüpunkt "Modell" - "Externe Modelldaten" - "Modell jeweils vor Simulationsstart aktualisieren" */
	private JCheckBoxMenuItem menuModelLoadExternalDataOnStart;
	/** Menüpunkt "Modell" - "Aktuelles und festgehaltenes Modell vergleichen" */
	private JMenuItem menuModelCompareKept;
	/** Menüpunkt "Modell" - "Zu festgehaltenem Modell zurückkehren" */
	private JMenuItem menuModelCompareReturn;
	/** Menüpunkt "Simulation" - "Animationskonfiguration" - "Animation sofort starten" */
	private JRadioButtonMenuItem menuSimulationAnimationStartModeRun;
	/** Menüpunkt "Simulation" - "Animationskonfiguration" - "Im Pause-Modus starten" */
	private JRadioButtonMenuItem menuSimulationAnimationStartModePause;
	/** Menüpunkt "Simulation" - "Animationskonfiguration" - "Schnelle Animation" */
	private JRadioButtonMenuItem menuSimulationAnimationAnalogValuesFast;
	/** Menüpunkt "Simulation" - "Animationskonfiguration" - "Änderungen exakt anzeigen (langsam)" */
	private JRadioButtonMenuItem menuSimulationAnimationAnalogValuesExact;
	/** Menüpunkt "Simulation" - "Animationskonfiguration" - "Im Nutzerverzeichnis speichern" */
	private JRadioButtonMenuItem menuSimulationAnimationScreenshotModeHome;
	/** Menüpunkt "Simulation" - "Animationskonfiguration" - "Im ausgewählten Verzeichnis speichern" */
	private JRadioButtonMenuItem menuSimulationAnimationScreenshotModeCustom;
	/** Menüpunkt "Simulation" - "Verbindung zum Simulationsserver prüfen" */
	private JMenuItem menuSimulationCheckServerConnection;

	/** Anzeige der Speichernutzung in der Menüzeile (per Konfiguration aktivierbar; im Standardfall aus) */
	private JLabel memoryUsage;
	/** Schnellzugriffs-Eingabefeld in der Menüzeile */
	private JQuickAccessTextField quickAccess;
	/** Schaltfläche zur Modell-Schnellkorrektur (nur sichtbar, wenn über eine Schnellkorrektur behebbare Fehler vorliegen) */
	private JButton fixButton;

	/**
	 * Programmeinstellungen-Singleton
	 */
	private final SetupData setup;

	/**
	 * Callback, welches ein Neuladen des Fensters veranlasst.
	 */
	private Runnable reloadWindow;

	/**
	 * Aktuell sichtbares Panel
	 */
	public JPanel currentPanel;

	/**
	 * Editor-Panel, welches das Modell vorhält
	 */
	public final EditorPanel editorPanel;

	/**
	 * Statistik-Seite
	 */
	public final StatisticsPanel statisticsPanel;

	/**
	 * Willkommen-Seite (wenn gerade aktiv)
	 */
	private JPanel welcomePanel;

	/**
	 * "Simulation läuft"-Seite (wenn gerade aktiv)
	 */
	private final WaitPanel waitPanel;

	/**
	 * Animationsseite (wenn gerade aktiv)
	 */
	private final AnimationPanel animationPanel;

	/**
	 * Aktuell gewähltes {@link SpecialPanel}
	 */
	private SpecialPanel specialPanel;

	/**
	 * Modell für den Vergleich mit einem geänderten Modell festhalten
	 */
	private EditModel pinnedModel;

	/**
	 * Statistikdaten, die sich beim Modellvergleich ergeben haben
	 */
	private Statistics[] compareStatistics=new Statistics[2];

	/**
	 * Konstruktor der Klasse
	 * @param ownerWindow	Übergeordnetes Fenster
	 * @param programName	Name des Programms (wird dann über {@link MainPanelBase#programName} angeboten)
	 * @param isReload	Gibt an, ob es sich bei dem Aufbau des Panels um einen Programmstart (<code>false</code>) oder nur um einen Wiederaufbau z.B. nach dem Ändern der Sprache (<code>true</code>) handelt
	 */
	public MainPanel(final JFrame ownerWindow, final String programName, final boolean isReload) {
		super(ownerWindow,programName);
		setup=SetupData.getSetup();
		setAdditionalTitle(UNSAVED_MODEL);
		initActions();
		initToolbar();
		welcomePanel=null;
		editorPanel=new EditorPanel(this);
		editorPanel.addFileDropListener(e->{if (e.getSource() instanceof FileDropperData) dropFile((FileDropperData)e.getSource());});
		editorPanel.addUndoRedoDoneListener(e->{
			menuEditUndo.setEnabled(editorPanel.canUndo());
			menuEditRedo.setEnabled(editorPanel.canRedo());
			menuEditUndoRedoSelect.setEnabled(editorPanel.canUndo() || editorPanel.canRedo());
		});
		editorPanel.addSelectionListener(e->selectionChanged());
		editorPanel.addLinkListener(link->{switch (link) {
		case INTERACTIVE_TUTORIAL: commandHelpInteractiveTutorial(); break;
		case TUTORIAL: commandHelpTutorial(); break;
		case GENERATOR: commandFileModelGenerator(); break;
		case EXAMPLES: commandFileModeExample(); break;
		case BOOK: commandHelpBook(null); break;
		}});
		editorPanel.addChangedStateListeners(()->{
			setAdditionalTitleChangedMarker(editorPanel.isModelChanged());
			menuModelLoadExternalDataOnStart.setSelected(editorPanel.getModelExternalData().isActive());
			selectionChanged();

		});
		editorPanel.addBuildParameterSeriesListener(template->commandSimulationParameterSeriesNew(template));
		editorPanel.addTemplatesVisibleChangeListener(()->{
			if (menuViewTemplatesBar!=null) menuViewTemplatesBar.setState(editorPanel.isTemplatesVisible());
		});
		editorPanel.addNavigatorVisibleChangeListener(()->{
			if (menuViewNavigator!=null) menuViewNavigator.setState(editorPanel.isNavigatorVisible());
		});
		editorPanel.setElementSearchCallback(()->commandEditFindElement());
		editorPanel.setElementListCallback(()->commandModelListElements());
		editorPanel.addNextActionListener(nextAction->{switch (nextAction) {
		case FIND_BATCH_SIZE: commandSimulationFindBatchSize(); break;
		case NONE: /* keine Aktion */ break;
		default: /* keine Aktion */ break;
		}});

		waitPanel=new WaitPanel();
		statisticsPanel=new StatisticsPanel(()->commandSimulationSimulation(null,null,null,Simulator.logTypeFull,null));
		statisticsPanel.addFileDropListener(e->{if (e.getSource() instanceof FileDropperData) dropFile((FileDropperData)e.getSource());});
		editorPanel.setStatisticsGetter(()->statisticsPanel.getStatistics());
		editorPanel.setShowHeatMapSelectWindowCallback(()->commandViewStatisticsHeatMapSelect());

		animationPanel=new AnimationPanel(ownerWindow);
		specialPanel=null;

		SwingUtilities.invokeLater(()->{
			if (!fileLoadedOnLoad && !isReload) {
				final int index=EditModelExamples.getExampleIndexFromName(setup.startModel);
				if (index<0) commandFileModelNew(); else commandFileModelExample(index);
			}
			if (!isReload) {
				startUpChecks();
			}

			if (InfoPanel.getInstance().isVisible(InfoPanel.globalWelcome) && !fileLoadedOnLoad && !isReload) {
				welcomePanel=Help.infoPanel("welcome",new SpecialLink());
				welcomePanel.setBorder(BorderFactory.createLineBorder(new Color(SystemColor.activeCaptionBorder.getRGB())));
				final InputMap inputMap=welcomePanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
				final KeyStroke stroke=KeyStroke.getKeyStroke("ESCAPE");
				inputMap.put(stroke,"ESCAPE");
				welcomePanel.getActionMap().put("ESCAPE",new AbstractAction() {
					/**
					 * Serialisierungs-ID der Klasse
					 * @see Serializable
					 */
					private static final long serialVersionUID = -8920150696914227106L;
					@Override public void actionPerformed(ActionEvent e) {setCurrentPanel(editorPanel);}
				});
				setCurrentPanel(welcomePanel);
			} else {
				if (!isReload) setCurrentPanel(editorPanel);
			}
			setup.addChangeNotifyListener(()->reloadSetup());
			reloadSetup();

			if (isReload) {
				ServerPanel.updateRunningServers(this);
			} else {
				ServerPanel.autoStartServers(this);
				GitTools.startUpPull(this);
			}

			boolean isAutoRestore=false;
			if (!fileLoadedOnLoad && !isReload && setup.startModel.isEmpty()) {
				final EditModel restore=ModelRestore.autoRestore(this);
				if (restore!=null) {
					commandFileModelExample(restore);
					isAutoRestore=true;
					editorPanel.setModelChanged(true);
				}
			}

			if (!fileLoadedOnLoad && !isReload && setup.startModel.isEmpty() && !isAutoRestore) {
				BackgroundPrepareCompiledClasses.run();
			}

			/* Wird zu früh eine Datei auf das Programmfenster gezogen, so blockiert die Swing Event Queue. Dann kann das Programm nur noch abgebrochen werden. */
			SwingStartUpWatchDog.start(10);
		});
	}

	/**
	 * Reagiert auf Drag&amp;Drop-Aktionen auf das Editor- oder das Statistik-Panel.
	 * @param drop	Drag&amp;Drop-Element
	 * @see #editorPanel
	 * @see #statisticsPanel
	 */
	private void dropFile(final FileDropperData drop) {
		final File file=drop.getFile();
		if (file.isFile()) {
			drop.dragDropConsumed();
			SwingUtilities.invokeLater(()->{
				if (loadAnyFile(file,drop.getDropComponent(),drop.getDropPosition(),true)) {
					CommonVariables.setInitialDirectoryFromFile(file);
				}
			});
		}
	}

	/**
	 * Initiiert die Zuordnung von Aktionsnamen zu {@link Runnable}-Funktionen.
	 */
	private void initActions() {
		/* Hotkeys */
		final InputMap input=getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		input.put(KeyStroke.getKeyStroke('E',InputEvent.CTRL_DOWN_MASK),"ctrlE");
		input.put(KeyStroke.getKeyStroke('3',InputEvent.CTRL_DOWN_MASK),"ctrlE");
		input.put(KeyStroke.getKeyStroke('P',InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_DOWN_MASK),"ctrlE");
		addAction("ctrlE",e->{if (quickAccess!=null && quickAccess.isVisible()) {quickAccess.requestFocus(); quickAccess.selectAll();}});

		/* Datei */
		addAction("FileNew",e->commandFileModelNew());
		addAction("FileNewExample",e->commandFileModeExample());
		addAction("FileNewGenerator",e->commandFileModelGenerator());
		addAction("FileNewWindow",e->commandFileNewWindow());
		addAction("FileLoad",e->commandFileModelLoadAll());
		/* Letzte Modell -> über action(Object) */
		addAction("FileSave",e->commandFileModelSave(false));
		addAction("FileSaveAs",e->commandFileModelSave(true));
		addAction("FileSaveCopyAs",e->commandFileModelSaveCopyAs());
		addAction("FileExport",e->commandFileModelExport());
		addAction("FilePrint",e->commandFileModelPrint());
		addAction("FileStatisticsLoad",e->commandFileStatisticsLoad(null,null));
		addAction("FileStatisticsSave",e->commandFileStatisticsSave());
		addAction("FileSetup",e->commandFileSetup(null));
		addAction("FileGit",e->commandFileGit());
		addAction("FileQuit",e->{
			Container c=getParent();
			while (!(c instanceof MainFrameBase)) c=c.getParent();
			((MainFrameBase)c).quitProgram();
		});

		/* Bearbeiten */
		addAction("EditUndo",e->editorPanel.doUndo());
		addAction("EditRedo",e->editorPanel.doRedo());
		addAction("EditUndoRedoSelect",e->editorPanel.doUnDoRedoByDialog());
		addAction("EditCut",e->editorPanel.cutSelectedElementsToClipboard());
		addAction("EditCopy",e->editorPanel.copySelectedElementsToClipboard());
		addAction("EditCopyModel",e->editorPanel.exportModelToClipboard());
		addAction("EditPaste",e->editorPanel.pasteFromClipboard());
		addAction("EditDelete",e->editorPanel.deleteSelectedElements());
		addAction("EditSelectAll",e->editorPanel.selectAll());
		addAction("EditSendFront",e->editorPanel.moveSelectedElementToFront(true));
		addAction("EditSendForwards",e->editorPanel.moveSelectedElementToFront(false));
		addAction("EditSendBackwards",e->editorPanel.moveSelectedElementToBack(false));
		addAction("EditSendBackground",e->editorPanel.moveSelectedElementToBack(true));
		addAction("EditAlignTop",e->editorPanel.alignSelectedElementsTop());
		addAction("EditAlignMiddle",e->editorPanel.alignSelectedElementsMiddle());
		addAction("EditAlignBottom",e->editorPanel.alignSelectedElementsBottom());
		addAction("EditAlignLeft",e->editorPanel.alignSelectedElementsLeft());
		addAction("EditAlignCenter",e->editorPanel.alignSelectedElementsCenter());
		addAction("EditAlignRight",e->editorPanel.alignSelectedElementsRight());
		addAction("EditFindElement",e->commandEditFindElement());
		addAction("EditQuickAccess",e->{if (quickAccess!=null && quickAccess.isVisible()) quickAccess.requestFocus();});
		addAction("EditAutoConnectOff",e->commandEditToggleAutoConnect(ModelSurfacePanel.ConnectMode.OFF));
		addAction("EditAutoConnectAuto",e->commandEditToggleAutoConnect(ModelSurfacePanel.ConnectMode.AUTO));
		addAction("EditAutoConnectSmart",e->commandEditToggleAutoConnect(ModelSurfacePanel.ConnectMode.SMART));
		addAction("EditRenameOnCopyOff",e->commandEditToggleRenameOnCopy(SetupData.RenameOnCopyMode.OFF));
		addAction("EditRenameOnCopySmart",e->commandEditToggleRenameOnCopy(SetupData.RenameOnCopyMode.SMART));
		addAction("EditRenameOnCopyAlways",e->commandEditToggleRenameOnCopy(SetupData.RenameOnCopyMode.ALWAYS));

		/* Ansicht */
		addAction("ViewEditor",e->setCurrentPanel(editorPanel));
		addAction("ViewStatistics",e->setCurrentPanel(statisticsPanel));
		addAction("ViewTemplatesBar",e->editorPanel.setTemplatesVisible(!editorPanel.isTemplatesVisible(),false));
		addAction("ViewNavigator",e->editorPanel.setNavigatorVisible(!editorPanel.isNavigatorVisible(),false));
		addAction("ViewExplorer",e->editorPanel.showExplorer());
		addAction("ViewRulers",e->commandViewRulers());
		addAction("ViewRasterOff",e->commandViewRaster(ModelSurface.Grid.OFF));
		addAction("ViewRasterDots",e->commandViewRaster(ModelSurface.Grid.DOTS));
		addAction("ViewRasterRaster",e->commandViewRaster(ModelSurface.Grid.LINES));
		addAction("ViewStatisticsInfo",e->commandViewStatisticsInfo());
		for (EditorPanelStatistics.HeatMapMode mode: EditorPanelStatistics.HeatMapMode.values()) {
			final EditorPanelStatistics.HeatMapMode finalMode=mode;
			addAction("ViewStatisticsHeatMapMode"+mode.toString(),e->commandViewStatisticsHeatMap(finalMode));
		}
		addAction("ViewStatisticsHeatMapPreviousMode",e->commandViewStatisticsHeatMapShift(-1));
		addAction("ViewStatisticsHeatMapNextMode",e->commandViewStatisticsHeatMapShift(1));
		addAction("ViewStatisticsHeatMapSelect",e->commandViewStatisticsHeatMapSelect());
		addAction("ViewStatisticsHeatMapSetup",e->commandViewStatisticsHeatMapSetup());
		addAction("ViewShowIDs",e->commandViewIDs());
		addAction("ViewShowStationDescriptions",e->commandViewStationDescriptions());
		addAction("ViewGradients",e->commandViewGradients());
		addAction("ViewShadows",e->commandViewShadows());
		addAction("ViewZoomOut",e->editorPanel.zoomOut());
		addAction("ViewZoomDefault",e->editorPanel.zoomDefault());
		addAction("ViewZoomIn",e->editorPanel.zoomIn());
		addAction("ViewCenterModel",e->editorPanel.centerModel());
		addAction("ViewTop",e->editorPanel.scrollToTop());
		addAction("ViewLayers",e->commandLayers());
		addAction("ViewBackgroundColor",e->commandViewBackgroundColor());
		addAction("ViewEdgeSettings",e->commandViewEdgeSettings());
		addAction("ViewUserDefinedAnimationIcons",e->commandViewUserDefinedAnimationIcons());

		/* Modell */
		addAction("ModelCheck",e->commandModelCheck());
		addAction("ModelCheckData",e->commandModelCheckData());
		addAction("ModelModelProperties",e->editorPanel.showModelPropertiesDialog(null));
		addAction("ModelExternalDataEdit",e->commandModelExternalDataEdit());
		addAction("ModelExternalDataLoadNow",e->commandModelExternalDataLoadNow());
		addAction("ModelExternalDataLoadOnStart",e->commandModelExternalDataLoadOnStart());
		addAction("ModelPluginFolder",e->commandModelPluginFolder());
		addAction("ModelListElements",e->commandModelListElements());
		addAction("ModelModelDescription",e->commandModelDescription());
		addAction("ModelModelNotes",e->commandModelNotes());
		addAction("ModelAnalyticModelCompare",e->commandModelAnalyticCompare());
		addAction("ModelCompareKeep",e->commandModelCompareTwoInit());
		addAction("ModelCompareKept",e->commandModelCompareTwoRun(0));
		addAction("ModelCompareReturn",e->commandModelCompareReturn());
		addAction("ModelTemplates",e->commandModelTemplates());
		addAction("ModelAddTemplate",e->commandModelAddTemplate());

		/* Simulation */
		addAction("SimulationAnimation",e->commandSimulationAnimation(null,false,null,null,Simulator.logTypeFull));
		addAction("SimulationAnimationRecord",e->commandSimulationAnimationRecord());
		addAction("SimulationAnimationLog",e->commandSimulationAnimationLog());
		addAction("SimulationAnimationStartModeRun",e->commandSimulationAnimationStartMode(false));
		addAction("SimulationAnimationStartModePause",e->commandSimulationAnimationStartMode(true));
		addAction("SimulationAnimationAnalogValuesFast",e->commandSimulationAnalogValuesSlow(false));
		addAction("SimulationAnimationAnalogValuesExact",e->commandSimulationAnalogValuesSlow(true));
		addAction("SimulationAnimationScreenshotModeHome",e->commandSimulationAnimationScreenshotModeHome());
		addAction("SimulationAnimationScreenshotModeCustom",e->commandSimulationAnimationScreenshotModeCustom());
		addAction("SimulationSimulation",e->commandSimulationSimulation(null,null,null,Simulator.logTypeFull,null));
		addAction("SimulationSimulationLog",e->commandSimulationSimulationLog());
		addAction("SimulationStatisticsSetup",e->commandSimulationStatisticsSetup());
		addAction("SimulationCheckServerConnection",e->commandSimulationCheckServerConnection());
		addAction("SimulationFindWarmUpSize",e->commandSimulationFindWarmUpSize());
		addAction("SimulationFindBatchSize",e->commandSimulationFindBatchSize());
		addAction("SimulationParameterSeriesNew",e->commandSimulationParameterSeriesNew(null));
		addAction("SimulationParameterSeriesLoad",e->commandSimulationParameterSeriesLoad(null,null));
		addAction("SimulationParameterSeriesVariance",e->commandSimulationParameterSeriesVariance());
		addAction("SimulationScriptRunner",e->commandSimulationScriptRunner());
		addAction("SimulationOptimizer",e->commandSimulationOptimizer());
		addAction("SimulationModelForResults",e->commandSimulationModelForResults());

		/* Extras */
		addAction("ExtrasCompare",e->commandExtrasCompare());
		addAction("ExtrasCalculator",e->commandExtrasCalculator(null,null));
		addAction("ExtrasQueueingCalculator",e->commandExtrasQueueingCalculator());
		addAction("ExtrasFit",e->commandExtrasFit());
		addAction("ExtrasTestDatabaseConnection",e->commandExtrasTestDatabaseConnection());
		addAction("ExtrasExecuteCommand",e->commandExtrasExecuteCommand());
		addAction("ExtrasSimulationServer",e->commandExtrasSimulationServer());
		addAction("ExtrasSystemInfo",e->commandExtrasSystemInfo());

		/* Hilfe */
		addAction("HelpHelp",e->commandHelpHelp());
		addAction("HelpContent",e->commandHelpContent());
		addAction("HelpInteractiveTurorial",e->commandHelpInteractiveTutorial());
		addAction("HelpAutomaticHelpWindow",e->commandHelpAutomaticHelpWindow());
		addAction("HelpTurorial",e->commandHelpTutorial());
		addAction("HelpTurorialSlides",e->commandHelpTutorialSlides());
		addAction("HelpTurorialVideo",e->commandHelpTutorialVideo());
		addAction("HelpScriptingReference",e->commandHelpScriptingReference());
		addAction("HelpElementReference",e->commandHelpElementReference());
		addAction("HelpCommandLineReference",e->commandHelpCommandLineReference());
		addAction("HelpHotkeyReference",e->commandHelpHotkeyReference());
		addAction("HelpDistributionReference",e->commandHelpDistributionReference());
		addAction("HelpTextbook",e->commandHelpBook(null));
		addAction("HelpLiteratureHerzog",e->commandHelpLiterature(0));
		addAction("HelpLiteratureGrossHarris",e->commandHelpLiterature(1));
		addAction("HelpLiteratureBolch",e->commandHelpLiterature(1));
		addAction("HelpLiteratureLawKelton",e->commandHelpLiterature(3));
		addAction("HelpLiteratureWaldmann",e->commandHelpLiterature(4));
		addAction("HelpLiteratureHedtstueck",e->commandHelpLiterature(5));
		addAction("HelpLiteratureGutenschwager",e->commandHelpLiterature(6));
		addAction("HelpSupport",e->commandHelpSupport());
		addAction("HelpSupportData",e->commandHelpSupportData());
		addAction("HelpHomepage",e->commandHelpHomepage());
		addAction("HelpUpdates",e->commandFileSetup(SetupDialog.Page.UPDATES));
		addAction("HelpUsageStatistics",e->commandHelpUsageStatistics());
		addAction("HelpLicense",e->commandHelpLicenseInfo());
		addAction("HelpInfo",e->commandHelpInfo());
		addAction("HelpFix",e->commandHelpFix(fixButton));
	}

	/**
	 * Reagiert auf Link-Klicks in {@link MainPanel#welcomePanel}
	 * @see MainPanel#welcomePanel
	 */
	private class SpecialLink implements Consumer<String> {
		@Override
		public void accept(String href) {
			if (href.equalsIgnoreCase("ModelEditor")) {setCurrentPanel(editorPanel); return;}
			if (href.equalsIgnoreCase("Tutorial")) {setCurrentPanel(editorPanel); commandHelpTutorial(); return;}
			if (href.equalsIgnoreCase("InteractiveTutorial")) {commandHelpInteractiveTutorial(); return;}
			if (href.equalsIgnoreCase("NoWelcomePage")) {
				setCurrentPanel(editorPanel);
				InfoPanel.getInstance().setVisible(InfoPanel.globalWelcome,false);
				return;
			}
		}
	}

	/**
	 * Über diese Methode kann dem Panel ein Callback mitgeteilt werden,
	 * das aufgerufen wird, wenn das Fenster neu geladen werden soll.
	 * @param reloadWindow	Callback, welches ein Neuladen des Fensters veranlasst.
	 */
	public void setReloadWindow(final Runnable reloadWindow) {
		this.reloadWindow=reloadWindow;
	}

	/**
	 * Lädt die Einstellungen neu und benachrichtigt die anderen Fenster.
	 */
	public void reloadSetup() {
		reloadSetup(true);
	}

	/**
	 * Lädt die Einstellungen neu.
	 * @param updateOtherFrames	Sollen die anderen Fenster ebenfalls benachrichtigt werden?
	 */
	public void reloadSetup(final boolean updateOtherFrames) {
		String custom;

		/* Datei - Zuletzt verwendete Modelle */
		updateRecentlyUsedList();

		/* Bearbeiten - Automatisch verbinden */
		menuEditAutoConnectOff.setSelected(setup.autoConnect==ModelSurfacePanel.ConnectMode.OFF);
		menuEditAutoConnectAuto.setSelected(setup.autoConnect==ModelSurfacePanel.ConnectMode.AUTO);
		menuEditAutoConnectSmart.setSelected(setup.autoConnect==ModelSurfacePanel.ConnectMode.SMART);
		editorPanel.setAutoConnect(setup.autoConnect);

		/* Bearbeiten - Stationen beim Kopieren umbenennen */
		menuEditRenameOnCopyOff.setSelected(setup.renameOnCopy==SetupData.RenameOnCopyMode.OFF);
		menuEditRenameOnCopySmart.setSelected(setup.renameOnCopy==SetupData.RenameOnCopyMode.SMART);
		menuEditRenameOnCopyAlways.setSelected(setup.renameOnCopy==SetupData.RenameOnCopyMode.ALWAYS);

		/* Ansicht - Vorlagen */
		menuViewTemplatesBar.setState(editorPanel.isTemplatesVisible());

		/* Ansicht - Navigator */
		menuViewNavigator.setState(editorPanel.isNavigatorVisible());

		/* Ansicht - Lineale */
		menuViewRulers.setSelected(setup.showRulers);
		editorPanel.setRulersVisible(setup.showRulers);

		/* Ansicht - Raster */
		menuViewGridOff.setSelected(setup.grid==ModelSurface.Grid.OFF);
		menuViewGridDots.setSelected(setup.grid==ModelSurface.Grid.DOTS);
		menuViewGridLines.setSelected(setup.grid==ModelSurface.Grid.LINES);
		editorPanel.setRaster(setup.grid);

		/* Ansicht - Statistik - Tooltips anzeigen */
		menuViewStatisticsTooltips.setSelected(setup.statisticInTooltips);
		int heatMapMode=0;
		if (setup.statisticHeatMap!=null) heatMapMode=Arrays.asList(EditorPanelStatistics.HeatMapMode.values()).indexOf(setup.statisticHeatMap);
		for (int i=0;i<menuViewStatisticsHeatMapMode.size();i++) menuViewStatisticsHeatMapMode.get(i).setSelected(heatMapMode==i);
		HeatMapSelectWindow.updateSelection();

		/* Ansicht - IDs */
		menuViewShowIDs.setState(setup.showIDs);

		/* Ansicht - Stationsbeschreibungen */
		menuViewShowStationDescription.setState(setup.showStationDescription);

		/* Ansicht - Gradienten */
		menuViewGradients.setVisible(!setup.useHighContrasts);
		menuViewGradients.setState(setup.useGradients);

		/* Ansicht - Schatten */
		menuViewShadows.setState(setup.useShadows);

		/* Simulation - Animation - Start */
		menuSimulationAnimationStartModeRun.setSelected(!setup.animationStartPaused);
		menuSimulationAnimationStartModePause.setSelected(setup.animationStartPaused);

		/* Simulation - Animation - Analoge Werte */
		menuSimulationAnimationAnalogValuesFast.setSelected(!setup.useSlowModeAnimation);
		menuSimulationAnimationAnalogValuesExact.setSelected(setup.useSlowModeAnimation);

		/* Simulation - Animation - Screenshots */
		menuSimulationAnimationScreenshotModeHome.setSelected(setup.imagePathAnimation==null || setup.imagePathAnimation.trim().isEmpty());
		menuSimulationAnimationScreenshotModeCustom.setSelected(setup.imagePathAnimation!=null && !setup.imagePathAnimation.trim().isEmpty());
		custom="";
		if (setup.imagePathAnimation!=null && !setup.imagePathAnimation.trim().isEmpty()) custom=" ("+setup.imagePathAnimation.trim()+")";
		menuSimulationAnimationScreenshotModeCustom.setText(Language.tr("Main.Menu.AnimationScreenshotMode.Custom")+custom);

		/* Simulation - Serververbindung */
		menuSimulationCheckServerConnection.setVisible(setup.serverUse);

		/* Nutzungsstatistik */
		UsageStatistics.getInstance().loadFromSetup();

		/* Sprache neu laden? */
		if (!setup.language.equals(Language.getCurrentLanguage())) {
			setup.resetLanguageWasAutomatically();
			HelpBase.hideHelpFrame();
			if (reloadWindow!=null) SwingUtilities.invokeLater(reloadWindow);
		} else {
			invalidate();
			if (reloadWindow!=null) SwingUtilities.invokeLater(()->repaint());
		}

		/* Background-System neu einstellen */
		if (setup.backgroundSimulation!=BackgroundSystem.getBackgroundSystem(editorPanel).getLastBackgroundMode()) {
			BackgroundSystem.getBackgroundSystem(editorPanel).stop(); /* Falls Hintergrundsimulation deaktiviert wurde, auch Timer-Thread beenden */
			editorPanel.updateStatusBar(); /* Damit wird die Hintergrundsimulation ggf. neu gestartet. */
		}

		/* Andere JS-Engine gewählt? */
		statisticsPanel.updateFastAccess();

		/* "Öffnen"-Buttons in Statistik */
		StatisticsBasePanel.viewerPrograms.clear();
		if (setup.openWord) StatisticsBasePanel.viewerPrograms.add(StatisticsBasePanel.ViewerPrograms.WORD);
		if (setup.openODT) StatisticsBasePanel.viewerPrograms.add(StatisticsBasePanel.ViewerPrograms.ODT);
		if (setup.openExcel) StatisticsBasePanel.viewerPrograms.add(StatisticsBasePanel.ViewerPrograms.EXCEL);
		if (setup.openODS) StatisticsBasePanel.viewerPrograms.add(StatisticsBasePanel.ViewerPrograms.ODS);
		if (setup.openPDF) StatisticsBasePanel.viewerPrograms.add(StatisticsBasePanel.ViewerPrograms.PDF);

		/* Alles neu zeichnen */
		editorPanel.resetHeatMapSettings();
		final Window ownerWindow=getOwnerWindow();
		if (ownerWindow!=null) repaint();
		editorPanel.repaint();

		/* Ggf. andere Fenster benachrichtigen */
		if (updateOtherFrames) {
			ReloadManager.notify((MainFrame)getOwnerWindow(),MainFrame.ReloadMode.SETUP);
		}
	}

	/**
	 * Stellt das aktuell im {@link MainPanel} sichtbare Kind-Panel ein.
	 * @param visiblePanel	Anzuzeigendes Panel
	 */
	public void setCurrentPanel(final JPanel visiblePanel) {
		if (visiblePanel!=welcomePanel && welcomePanel!=null) {
			mainPanel.remove(welcomePanel);
			welcomePanel=null;
		}
		if (visiblePanel!=editorPanel) mainPanel.remove(editorPanel);
		if (visiblePanel!=waitPanel) mainPanel.remove(waitPanel);
		if (visiblePanel!=statisticsPanel) mainPanel.remove(statisticsPanel);
		if (visiblePanel!=animationPanel) mainPanel.remove(animationPanel);
		if (specialPanel!=null && visiblePanel!=specialPanel) mainPanel.remove(specialPanel);

		boolean isInPanel=false;
		for (Component component : mainPanel.getComponents()) if (component==visiblePanel) {isInPanel=true; break;}
		if (!isInPanel) mainPanel.add(visiblePanel);

		currentPanel=visiblePanel;
		if (currentPanel instanceof SpecialPanel) specialPanel=(SpecialPanel) currentPanel; else specialPanel=null;

		final boolean welcomePanelActive=(visiblePanel==welcomePanel);
		final boolean editorPanelActive=(visiblePanel==editorPanel);
		final boolean statisticsPanelActive=(visiblePanel==statisticsPanel);

		mainPanel.repaint();

		if (editorPanelActive) SwingUtilities.invokeLater(()->{
			editorPanel.setVisible(false);
			editorPanel.resetHeatMapSettings();
			editorPanel.setVisible(true);
			editorPanel.repaint();
		});

		if (enabledOnEditorPanel!=null) for (JMenuItem item: enabledOnEditorPanel) item.setEnabled(editorPanelActive);
		if (selectOnEditorPanel!=null) for (AbstractButton button: selectOnEditorPanel) button.setSelected(editorPanelActive);
		if (selectOnStatisticsPanel!=null) for (AbstractButton button: selectOnStatisticsPanel) button.setSelected(statisticsPanelActive);
		if (visibleOnEditorPanel!=null) for (JButton button: visibleOnEditorPanel) button.setVisible(editorPanelActive || welcomePanelActive);
		if (visibleOnStatisticsPanel!=null) for (JButton button: visibleOnStatisticsPanel) button.setVisible(statisticsPanelActive);
		selectionChanged();
	}

	@Override
	protected URL getResourceURL(final String path) {
		return getClass().getResource(path);
	}

	@Override
	public JToolBar createToolBar() {
		if (visibleOnEditorPanel==null) visibleOnEditorPanel=new ArrayList<>();
		if (visibleOnStatisticsPanel==null) visibleOnStatisticsPanel=new ArrayList<>();
		if (selectOnEditorPanel==null) selectOnEditorPanel=new ArrayList<>();
		if (selectOnStatisticsPanel==null) selectOnStatisticsPanel=new ArrayList<>();
		if (enabledOnStatisticsAvailable==null) enabledOnStatisticsAvailable=new ArrayList<>();

		final JToolBar toolbar=new JToolBar();
		toolbar.setFloatable(false);
		JButton button;

		visibleOnEditorPanel.add(createToolbarButton(toolbar,Language.tr("Main.Toolbar.LoadModel"),Language.tr("Main.Toolbar.LoadModel.Hint")+" ("+keyStrokeToString(KeyStroke.getKeyStroke(KeyEvent.VK_L,InputEvent.CTRL_DOWN_MASK))+")",Images.MODEL_LOAD.getIcon(),"FileLoad"));
		visibleOnEditorPanel.add(createToolbarButton(toolbar,Language.tr("Main.Toolbar.SaveModel"),Language.tr("Main.Toolbar.SaveModel.Hint")+" ("+keyStrokeToString(KeyStroke.getKeyStroke(KeyEvent.VK_S,InputEvent.CTRL_DOWN_MASK))+")",Images.MODEL_SAVE.getIcon(),"FileSave"));
		visibleOnStatisticsPanel.add(createToolbarButton(toolbar,Language.tr("Main.Toolbar.LoadStatistics"),Language.tr("Main.Toolbar.LoadStatistics.Hint")+" ("+keyStrokeToString(KeyStroke.getKeyStroke(KeyEvent.VK_L,InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_DOWN_MASK))+")",Images.STATISTICS_LOAD.getIcon(),"FileStatisticsLoad"));
		button=createToolbarButton(toolbar,Language.tr("Main.Toolbar.SaveStatistics"),Language.tr("Main.Toolbar.SaveStatistics.Hint")+" ("+keyStrokeToString(KeyStroke.getKeyStroke(KeyEvent.VK_S,InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_DOWN_MASK))+")",Images.STATISTICS_SAVE.getIcon(),"FileStatisticsSave");
		visibleOnStatisticsPanel.add(button);
		enabledOnStatisticsAvailable.add(button);
		toolbar.addSeparator();
		selectOnEditorPanel.add(createToolbarButton(toolbar,Language.tr("Main.Toolbar.ShowEditor"),Language.tr("Main.Toolbar.ShowEditor.Hint")+" ("+keyStrokeToString(KeyStroke.getKeyStroke(KeyEvent.VK_F3,0))+")",Images.MODEL.getIcon(),"ViewEditor"));
		selectOnStatisticsPanel.add(createToolbarButton(toolbar,Language.tr("Main.Toolbar.ShowStatistics"),Language.tr("Main.Toolbar.ShowStatistics.Hint")+" ("+keyStrokeToString(KeyStroke.getKeyStroke(KeyEvent.VK_F4,0))+")",Images.STATISTICS.getIcon(),"ViewStatistics"));
		toolbar.addSeparator();
		createToolbarButton(toolbar,Language.tr("Main.Toolbar.StartAnimation"),Language.tr("Main.Toolbar.StartAnimation.Hint")+" ("+keyStrokeToString(KeyStroke.getKeyStroke(KeyEvent.VK_F6,0))+")",Images.ANIMATION.getIcon(),"SimulationAnimation");
		createToolbarButton(toolbar,Language.tr("Main.Toolbar.StartSimulation"),Language.tr("Main.Toolbar.StartSimulation.Hint")+" ("+keyStrokeToString(KeyStroke.getKeyStroke(KeyEvent.VK_F5,0))+")",Images.SIMULATION.getIcon(),"SimulationSimulation");
		createToolbarButton(toolbar,Language.tr("Main.Toolbar.ParameterSeries"),Language.tr("Main.Toolbar.ParameterSeries.Hint")+" ("+keyStrokeToString(KeyStroke.getKeyStroke(KeyEvent.VK_F7,0))+")",Images.PARAMETERSERIES.getIcon(),"SimulationParameterSeriesNew");
		button=createToolbarButton(toolbar,Language.tr("Main.Toolbar.ShowModelForTheseResults"),Language.tr("Main.Toolbar.ShowModelForTheseResults.Hint"),Images.STATISTICS_SHOW_MODEL.getIcon(),"SimulationModelForResults");
		visibleOnStatisticsPanel.add(button);
		enabledOnStatisticsAvailable.add(button);
		toolbar.addSeparator();
		createToolbarButton(toolbar,Language.tr("Main.Toolbar.Help"),Language.tr("Main.Toolbar.Help.Hint")+" ("+keyStrokeToString(KeyStroke.getKeyStroke(KeyEvent.VK_F1,0))+")",Images.HELP.getIcon(),"HelpHelp");

		fixButton=createToolbarButton(toolbar,Language.tr("Main.Toolbar.Fix"),Language.tr("Main.Toolbar.Fix.Hint"),Images.GENERAL_WARNING_BUG.getIcon(),"HelpFix");
		fixButton.setVisible(false);

		/*
		toolbar.add(button=new JButton("Test"));
		button.addActionListener(e->{ });
		 */

		/*
		toolbar.add(button=new JButton("LanguageTest"));
		button.addActionListener(e->EditModelExamples.runLanguageTestAll());
		 */

		return toolbar;
	}

	/**
	 * Wandelt ein Hotkey-Objekt in eine entsprechende Beschreibung um
	 * @param key	Hotkey
	 * @return	Beschreibung als Zeichenkette
	 */
	private String keyStrokeToString(final KeyStroke key) {
		final int modifiers=key.getModifiers();
		final StringBuilder text=new StringBuilder();
		if (modifiers>0) {
			text.append(InputEvent.getModifiersExText(modifiers));
			text.append('+');
		}
		text.append(KeyEvent.getKeyText(key.getKeyCode()));
		return text.toString();
	}

	/**
	 * Erzeugt einen Menüpunkt, der nur einen Text anzeigt und nicht anklickbar ist
	 * @param text	Text für den Menüpunkt
	 * @return	Neuer Menüpunkt
	 */
	private JMenuItem createTextOnlyItem(final String text) {
		final JMenuItem item=new JMenuItem(text);
		Font font=item.getFont();
		font=new Font(font.getName(),Font.BOLD,font.getSize());
		item.setFont(font);
		item.setForeground(Color.BLACK);
		item.setEnabled(false);
		return item;
	}

	/**
	 * Legt einen neuen Radiobutton-Menüpunkt an und verknüpft ihn mit einem Action-Befehl.
	 * @param parent	Übergeordnetes Menü-Element
	 * @param mode	Heatmap-Modus zu dem der Menüpunkt erzeugt werden soll
	 * @param actionCommand	Beim Aufruf zu verwendender Action-Befehl
	 * @return	Neu erstellter Checkbox-Menüpunkt
	 */
	private JRadioButtonMenuItem createRadioButtonMenuItem(final JMenu parent, final EditorPanelStatistics.HeatMapMode mode, final String actionCommand) {
		return createRadioButtonMenuItem(parent,mode.getName(),mode.getNameMnemonic(),actionCommand);
	}

	@Override
	public JMenuBar createMenu() {
		if (enabledOnEditorPanel==null) enabledOnEditorPanel=new ArrayList<>();
		if (selectOnEditorPanel==null) selectOnEditorPanel=new ArrayList<>();
		if (selectOnStatisticsPanel==null) selectOnStatisticsPanel=new ArrayList<>();
		if (enabledOnStatisticsAvailable==null) enabledOnStatisticsAvailable=new ArrayList<>();

		final JMenuBar menubar=new JMenuBar();
		JMenu menu, submenu;
		ButtonGroup buttonGroup;

		/* Datei */
		menubar.add(menu=new JMenu(Language.tr("Main.Menu.File")));
		setMnemonic(menu,Language.tr("Main.Menu.File.Mnemonic"));

		createMenuItemCtrl(menu,Language.tr("Main.Menu.File.New"),Images.MODEL_NEW.getIcon(),Language.tr("Main.Menu.File.New.Mnemonic"),KeyEvent.VK_N,"FileNew");
		menu.add(submenu=new JMenu(Language.tr("Main.Menu.File.LoadExample")));
		setMnemonic(submenu,Language.tr("Main.Menu.File.LoadExample.Mnemonic"));
		createMenuItem(submenu,Language.tr("Main.Menu.File.LoadExample.ByPreview"),Language.tr("Main.Menu.File.LoadExample.ByPreview.Mnemonic"),"FileNewExample");
		EditModelExamples.addToMenu(this,submenu,newModel->commandFileModelExample(newModel));
		createMenuItemCtrlShift(menu,Language.tr("Main.Menu.File.Generator"),Images.MODEL_GENERATOR.getIcon(),Language.tr("Main.Menu.File.Generator.Mnemonic"),KeyEvent.VK_N,"FileNewGenerator");
		createMenuItem(menu,Language.tr("Main.Menu.File.NewWindow"),Images.GENERAL_APPLICATION.getIcon(),Language.tr("Main.Menu.File.NewWindow.Mnemonic"),"FileNewWindow");
		createMenuItemCtrl(menu,Language.tr("Main.Menu.File.Load"),Images.MODEL_LOAD.getIcon(),Language.tr("Main.Menu.File.Load.Mnemonic"),KeyEvent.VK_L,"FileLoad");
		menu.add(menuFileModelRecentlyUsed=new JMenu(Language.tr("Main.Menu.File.RecentlyUsed")));
		setMnemonic(menuFileModelRecentlyUsed,Language.tr("Main.Menu.File.RecentlyUsed.Mnemonic"));
		updateRecentlyUsedList();
		createMenuItemCtrl(menu,Language.tr("Main.Menu.File.Save"),Images.MODEL_SAVE.getIcon(),Language.tr("Main.Menu.File.Save.Mnemonic"),KeyEvent.VK_S,"FileSave");
		createMenuItemCtrl(menu,Language.tr("Main.Menu.File.SaveAs"),Language.tr("Main.Menu.File.SaveAs.Mnemonic"),KeyEvent.VK_U,"FileSaveAs");
		createMenuItem(menu,Language.tr("Main.Menu.File.SaveCopyAs"),Language.tr("Main.Menu.File.SaveCopyAs.Mnemonic"),"FileSaveCopyAs");
		createMenuItem(menu,Language.tr("Main.Menu.File.ExportModel"),Language.tr("Main.Menu.File.ExportModel.Mnemonic"),"FileExport");
		createMenuItemCtrl(menu,Language.tr("Main.Menu.File.PrintModel"),Images.GENERAL_PRINT.getIcon(),Language.tr("Main.Menu.File.PrintModel.Mnemonic"),KeyEvent.VK_P,"FilePrint");
		menu.addSeparator();
		createMenuItemCtrlShift(menu,Language.tr("Main.Menu.File.LoadStatistics"),Images.STATISTICS_LOAD.getIcon(),Language.tr("Main.Menu.File.LoadStatistics.Mnemonic"),KeyEvent.VK_L,"FileStatisticsLoad");
		enabledOnStatisticsAvailable.add(createMenuItemCtrlShift(menu,Language.tr("Main.Menu.File.SaveStatistics"),Images.STATISTICS_SAVE.getIcon(),Language.tr("Main.Menu.File.SaveStatistics.Mnemonic"),KeyEvent.VK_U,"FileStatisticsSave"));

		menu.addSeparator();
		createMenuItemCtrlShift(menu,Language.tr("Main.Menu.File.Settings"),Images.GENERAL_SETUP.getIcon(),Language.tr("Main.Menu.File.Settings.Mnemonic"),KeyEvent.VK_F2,"FileSetup");
		createMenuItem(menu,Language.tr("Main.Menu.File.GitSettings"),Images.GIT.getIcon(),Language.tr("Main.Menu.File.GitSettings.Mnemonic"),"FileGit");
		menu.addSeparator();
		createMenuItemCtrl(menu,Language.tr("Main.Menu.File.Quit"),Images.GENERAL_EXIT.getIcon(),Language.tr("Main.Menu.File.Quit.Mnemonic"),KeyEvent.VK_W,"FileQuit");

		/* Bearbeiten */
		menubar.add(menu=new JMenu(Language.tr("Main.Menu.Edit")));
		setMnemonic(menu,Language.tr("Main.Menu.Edit.Mnemonic"));

		menuEditUndo=createMenuItemCtrl(menu,Language.tr("Main.Menu.Edit.Undo"),Images.EDIT_UNDO.getIcon(),Language.tr("Main.Menu.Edit.Undo.Mnemonic"),KeyEvent.VK_Z,"EditUndo");
		menuEditUndo.setEnabled(false);
		menuEditRedo=createMenuItemCtrl(menu,Language.tr("Main.Menu.Edit.Redo"),Images.EDIT_REDO.getIcon(),Language.tr("Main.Menu.Edit.Redo.Mnemonic"),KeyEvent.VK_Y,"EditRedo");
		menuEditRedo.setEnabled(false);
		menuEditUndoRedoSelect=createMenuItem(menu,Language.tr("Main.Menu.Edit.UndoRedoSelect"),Language.tr("Main.Menu.Edit.UndoRedoSelect.Mnemonic"),"EditUndoRedoSelect");
		menuEditUndoRedoSelect.setEnabled(false);
		menu.addSeparator();
		enabledOnEditorPanel.add(createMenuItemCtrl(menu,Language.tr("Main.Menu.Edit.Cut"),Images.EDIT_CUT.getIcon(),Language.tr("Main.Menu.Edit.Cut.Mnemonic"),KeyEvent.VK_X,"EditCut"));
		enabledOnEditorPanel.add(createMenuItemCtrl(menu,Language.tr("Main.Menu.Edit.Copy"),Images.EDIT_COPY.getIcon(),Language.tr("Main.Menu.Edit.Copy.Mnemonic"),KeyEvent.VK_C,"EditCopy"));
		enabledOnEditorPanel.add(createMenuItemCtrlShift(menu,Language.tr("Main.Menu.Edit.CopyAsImage"),Images.EDIT_COPY_AS_IMAGE.getIcon(),Language.tr("Main.Menu.Edit.CopyAsImage.Mnemonic"),KeyEvent.VK_C,"EditCopyModel"));
		enabledOnEditorPanel.add(createMenuItemCtrl(menu,Language.tr("Main.Menu.Edit.Paste"),Images.EDIT_PASTE.getIcon(),Language.tr("Main.Menu.Edit.Paste.Mnemonic"),KeyEvent.VK_V,"EditPaste"));
		enabledOnEditorPanel.add(createMenuItem(menu,Language.tr("Main.Menu.Edit.Delete"),Images.EDIT_DELETE.getIcon(),Language.tr("Main.Menu.Edit.Delete.Mnemonic"),KeyEvent.VK_DELETE,"EditDelete"));
		enabledOnEditorPanel.add(createMenuItemCtrl(menu,Language.tr("Main.Menu.Edit.SelectAll"),Language.tr("Main.Menu.Edit.SelectAll.Mnemonic"),KeyEvent.VK_A,"EditSelectAll"));
		menu.addSeparator();
		menu.add(submenu=new JMenu(Language.tr("Main.Menu.Edit.Arrange")));
		enabledOnEditorPanel.add(createMenuItemCtrl(submenu,Language.tr("Main.Menu.Edit.MoveFront"),Images.MOVE_FRONT.getIcon(),Language.tr("Main.Menu.Edit.MoveFront.Mnemonic"),KeyEvent.VK_PAGE_UP,"EditSendFront"));
		enabledOnEditorPanel.add(createMenuItem(submenu,Language.tr("Main.Menu.Edit.MoveForwards"),Images.MOVE_FRONT_STEP.getIcon(),Language.tr("Main.Menu.Edit.MoveForwards.Mnemonic"),KeyEvent.VK_PAGE_UP,"EditSendForwards"));
		enabledOnEditorPanel.add(createMenuItem(submenu,Language.tr("Main.Menu.Edit.MoveBackwards"),Images.MOVE_BACK_STEP.getIcon(),Language.tr("Main.Menu.Edit.MoveBackwards.Mnemonic"),KeyEvent.VK_PAGE_DOWN,"EditSendBackwards"));
		enabledOnEditorPanel.add(createMenuItemCtrl(submenu,Language.tr("Main.Menu.Edit.MoveBack"),Images.MOVE_BACK.getIcon(),Language.tr("Main.Menu.Edit.MoveBack.Mnemonic"),KeyEvent.VK_PAGE_DOWN,"EditSendBackground"));
		menu.add(submenu=new JMenu(Language.tr("Main.Menu.Edit.Align")));
		enabledOnEditorPanel.add(createMenuItem(submenu,Language.tr("Main.Menu.Edit.Align.Top"),Images.ALIGN_TOP.getIcon(),Language.tr("Main.Menu.Edit.Align.Top.Mnemonic"),"EditAlignTop"));
		enabledOnEditorPanel.add(createMenuItem(submenu,Language.tr("Main.Menu.Edit.Align.Middle"),Images.ALIGN_MIDDLE.getIcon(),Language.tr("Main.Menu.Edit.Align.Middle.Mnemonic"),"EditAlignMiddle"));
		enabledOnEditorPanel.add(createMenuItem(submenu,Language.tr("Main.Menu.Edit.Align.Bottom"),Images.ALIGN_BOTTOM.getIcon(),Language.tr("Main.Menu.Edit.Align.Bottom.Mnemonic"),"AlignBottom"));
		submenu.addSeparator();
		enabledOnEditorPanel.add(createMenuItem(submenu,Language.tr("Main.Menu.Edit.Align.Left"),Images.ALIGN_LEFT.getIcon(),Language.tr("Main.Menu.Edit.Align.Left.Mnemonic"),"EditAlignLeft"));
		enabledOnEditorPanel.add(createMenuItem(submenu,Language.tr("Main.Menu.Edit.Align.Center"),Images.ALIGN_CENTER.getIcon(),Language.tr("Main.Menu.Edit.Align.Center.Mnemonic"),"EditAlignCenter"));
		enabledOnEditorPanel.add(createMenuItem(submenu,Language.tr("Main.Menu.Edit.Align.Right"),Images.ALIGN_RIGHT.getIcon(),Language.tr("Main.Menu.Edit.Align.Right.Mnemonic"),"EditAlignRight"));
		menu.addSeparator();
		enabledOnEditorPanel.add(createMenuItemCtrl(menu,Language.tr("Main.Menu.View.FindElement"),Images.GENERAL_FIND.getIcon(),Language.tr("Main.Menu.View.FindElement.Mnemonic"),KeyEvent.VK_F,"EditFindElement"));
		createMenuItemCtrl(menu,Language.tr("Main.Menu.View.QuickAccess"),Language.tr("Main.Menu.View.QuickAccess.Mnemonic"),KeyEvent.VK_E,"EditQuickAccess");
		menu.addSeparator();
		menu.add(submenu=new JMenu(Language.tr("Main.Menu.Edit.AutoConnect")));
		submenu.setIcon(Images.EDIT_EDGES_ADD.getIcon());
		enabledOnEditorPanel.add(menuEditAutoConnectOff=createRadioButtonMenuItem(submenu,Language.tr("Main.Menu.Edit.AutoConnect.Off"),Language.tr("Main.Menu.Edit.AutoConnect.Off.Mnemonic"),"EditAutoConnectOff"));
		menuEditAutoConnectOff.setSelected(setup.autoConnect==ModelSurfacePanel.ConnectMode.OFF);
		enabledOnEditorPanel.add(menuEditAutoConnectAuto=createRadioButtonMenuItem(submenu,Language.tr("Main.Menu.Edit.AutoConnect.Auto"),Language.tr("Main.Menu.Edit.AutoConnect.Auto.Mnemonic"),"EditAutoConnectAuto"));
		menuEditAutoConnectAuto.setSelected(setup.autoConnect==ModelSurfacePanel.ConnectMode.AUTO);
		enabledOnEditorPanel.add(menuEditAutoConnectSmart=createRadioButtonMenuItem(submenu,Language.tr("Main.Menu.Edit.AutoConnect.Smart"),Language.tr("Main.Menu.Edit.AutoConnect.Smart.Mnemonic"),"EditAutoConnectSmart"));
		menuEditAutoConnectSmart.setSelected(setup.autoConnect==ModelSurfacePanel.ConnectMode.SMART);
		editorPanel.setAutoConnect(setup.autoConnect);
		menu.add(submenu=new JMenu(Language.tr("Main.Menu.Edit.RenameOnCopy")));
		enabledOnEditorPanel.add(menuEditRenameOnCopyOff=createRadioButtonMenuItem(submenu,Language.tr("Main.Menu.Edit.RenameOnCopy.Off"),Language.tr("Main.Menu.Edit.RenameOnCopy.Off.Mnemonic"),"EditRenameOnCopyOff"));
		menuEditRenameOnCopyOff.setSelected(setup.renameOnCopy==SetupData.RenameOnCopyMode.OFF);
		enabledOnEditorPanel.add(menuEditRenameOnCopySmart=createRadioButtonMenuItem(submenu,Language.tr("Main.Menu.Edit.RenameOnCopy.Smart"),Language.tr("Main.Menu.Edit.RenameOnCopy.Smart.Mnemonic"),"EditRenameOnCopySmart"));
		menuEditRenameOnCopySmart.setSelected(setup.renameOnCopy==SetupData.RenameOnCopyMode.SMART);
		enabledOnEditorPanel.add(menuEditRenameOnCopyAlways=createRadioButtonMenuItem(submenu,Language.tr("Main.Menu.Edit.RenameOnCopy.Always"),Language.tr("Main.Menu.Edit.RenameOnCopy.Always.Mnemonic"),"EditRenameOnCopyAlways"));
		menuEditRenameOnCopyAlways.setSelected(setup.renameOnCopy==SetupData.RenameOnCopyMode.ALWAYS);

		/* Ansicht */
		menubar.add(menu=new JMenu(Language.tr("Main.Menu.View")));
		setMnemonic(menu,Language.tr("Main.Menu.View.Mnemonic"));

		selectOnEditorPanel.add(createCheckBoxMenuItemIcon(menu,Language.tr("Main.Menu.View.ModelEditor"),Images.MODEL.getIcon(),Language.tr("Main.Menu.View.ModelEditor.Mnemonic"),KeyEvent.VK_F3,"ViewEditor"));
		selectOnStatisticsPanel.add(createCheckBoxMenuItemIcon(menu,Language.tr("Main.Menu.View.SimulationResults"),Images.STATISTICS.getIcon(),Language.tr("Main.Menu.View.SimulationResults.Mnemonic"),KeyEvent.VK_F4,"ViewStatistics"));
		menu.addSeparator();
		enabledOnEditorPanel.add(menuViewTemplatesBar=createCheckBoxMenuItemIcon(menu,Language.tr("Main.Menu.View.TemplatesBar"),Images.ELEMENTTEMPLATES.getIcon(),Language.tr("Main.Menu.View.TemplatesBar.Mnemonic"),KeyEvent.VK_F2,"ViewTemplatesBar"));
		menuViewTemplatesBar.setState(editorPanel.isTemplatesVisible());
		enabledOnEditorPanel.add(menuViewNavigator=createCheckBoxMenuItemIcon(menu,Language.tr("Main.Menu.View.NavigatorBar"),Images.NAVIGATOR.getIcon(),Language.tr("Main.Menu.View.NavigatorBar.Mnemonic"),KeyEvent.VK_F12,"ViewNavigator"));
		menuViewNavigator.setState(editorPanel.isNavigatorVisible());
		createMenuItemCtrl(menu,Language.tr("Main.Menu.View.Explorer"),Images.MODE_OVERVIEW.getIcon(),Language.tr("Main.Menu.View.Explorer.Mnemonic"),KeyEvent.VK_F12,"ViewExplorer");
		menu.addSeparator();
		enabledOnEditorPanel.add(menuViewRulers=createCheckBoxMenuItemIcon(menu,Language.tr("Main.Menu.View.ShowRulers"),Images.EDIT_VIEW_RULERS.getIcon(),Language.tr("Main.Menu.View.ShowIDs.Mnemonic"),"ViewRulers"));
		menuViewRulers.setState(setup.showRulers);
		menu.add(submenu=new JMenu(Language.tr("Main.Menu.View.ShowRaster")));
		setMnemonic(submenu,Language.tr("Main.Menu.View.ShowRaster.Mnemonic"));
		submenu.setIcon(Images.EDIT_VIEW_RASTER.getIcon());
		enabledOnEditorPanel.add(menuViewGridOff=createRadioButtonMenuItem(submenu,Language.tr("Main.Menu.View.ShowRaster.Off"),Language.tr("Main.Menu.View.ShowRaster.Off.Mnemonic"),"ViewRasterOff"));
		enabledOnEditorPanel.add(menuViewGridDots=createRadioButtonMenuItem(submenu,Language.tr("Main.Menu.View.ShowRaster.Dots"),Language.tr("Main.Menu.View.ShowRaster.Dots.Mnemonic"),"ViewRasterDots"));
		enabledOnEditorPanel.add(menuViewGridLines=createRadioButtonMenuItem(submenu,Language.tr("Main.Menu.View.ShowRaster.Raster"),Language.tr("Main.Menu.View.ShowRaster.Raster.Mnemonic"),"ViewRasterRaster"));
		menuViewGridOff.setSelected(setup.grid==ModelSurface.Grid.OFF);
		menuViewGridDots.setSelected(setup.grid==ModelSurface.Grid.DOTS);
		menuViewGridLines.setSelected(setup.grid==ModelSurface.Grid.LINES);

		menu.add(submenu=new JMenu(Language.tr("Main.Menu.View.StatisticInfo")));
		setMnemonic(submenu,Language.tr("Main.Menu.View.StatisticInfo.Mnemonic"));
		submenu.setIcon(Images.STATISTIC_INFO.getIcon());
		enabledOnEditorPanel.add(menuViewStatisticsTooltips=createCheckBoxMenuItem(submenu,Language.tr("Main.Menu.View.Statistics.Info"),Language.tr("Main.Menu.View.Statistics.Info.Mnemonic"),"ViewStatisticsInfo"));
		submenu.addSeparator();
		menuViewStatisticsHeatMapMode=new ArrayList<>();
		for (EditorPanelStatistics.HeatMapMode mode: EditorPanelStatistics.HeatMapMode.values()) {
			final JRadioButtonMenuItem item=createRadioButtonMenuItem(submenu,mode,"ViewStatisticsHeatMapMode"+mode.toString());
			menuViewStatisticsHeatMapMode.add(item);
			enabledOnEditorPanel.add(item);
		}
		submenu.addSeparator();
		enabledOnEditorPanel.add(createMenuItemCtrlShift(submenu,Language.tr("Main.Menu.View.Statistics.HeatMapRotatePrevious"),Images.ARROW_UP.getIcon(),Language.tr("Main.Menu.View.Statistics.HeatMapRotatePrevious.Mnemonic"),KeyEvent.VK_G,"ViewStatisticsHeatMapPreviousMode"));
		enabledOnEditorPanel.add(createMenuItemCtrlShift(submenu,Language.tr("Main.Menu.View.Statistics.HeatMapRotateNext"),Images.ARROW_DOWN.getIcon(),Language.tr("Main.Menu.View.Statistics.HeatMapRotateNext.Mnemonic"),KeyEvent.VK_H,"ViewStatisticsHeatMapNextMode"));
		enabledOnEditorPanel.add(createMenuItem(submenu,Language.tr("Main.Menu.View.Statistics.HeatMapSelect"),Language.tr("Main.Menu.View.Statistics.HeatMapSelect.Mnemonic"),"ViewStatisticsHeatMapSelect"));
		submenu.addSeparator();
		enabledOnEditorPanel.add(createMenuItem(submenu,Language.tr("Main.Menu.View.Statistics.HeatMapSetup"),Language.tr("Main.Menu.View.Statistics.HeatMapSetup.Mnemonic"),"ViewStatisticsHeatMapSetup"));
		enabledOnEditorPanel.add(menuViewShowIDs=createCheckBoxMenuItem(menu,Language.tr("Main.Menu.View.ShowIDs"),Language.tr("Main.Menu.View.ShowIDs.Mnemonic"),"ViewShowIDs"));
		menuViewShowIDs.setState(setup.showIDs);
		enabledOnEditorPanel.add(menuViewShowStationDescription=createCheckBoxMenuItem(menu,Language.tr("Main.Menu.View.ShowStationDescriptions"),Language.tr("Main.Menu.View.ShowStationDescriptions.Mnemonic"),"ViewShowStationDescriptions"));
		menuViewShowStationDescription.setState(setup.showStationDescription);
		enabledOnEditorPanel.add(menuViewGradients=createCheckBoxMenuItem(menu,Language.tr("Main.Menu.View.ShowGradients"),Language.tr("Main.Menu.View.ShowGradients.Mnemonic"),"ViewGradients"));
		menuViewGradients.setState(setup.useGradients);
		menuViewGradients.setVisible(!setup.useHighContrasts);
		enabledOnEditorPanel.add(menuViewShadows=createCheckBoxMenuItem(menu,Language.tr("Main.Menu.View.ShowShadows"),Language.tr("Main.Menu.View.ShowShadows.Mnemonic"),"ViewShadows"));
		menuViewShadows.setState(setup.useShadows);
		menu.addSeparator();
		enabledOnEditorPanel.add(createMenuItemCtrl(menu,Language.tr("Main.Menu.View.ZoomOut"),Images.ZOOM_OUT.getIcon(),Language.tr("Main.Menu.View.ZoomOut.Mnemonic"),KeyEvent.VK_SUBTRACT,"ViewZoomOut"));
		enabledOnEditorPanel.add(createMenuItemCtrl(menu,Language.tr("Main.Menu.View.ZoomDefault"),Images.ZOOM.getIcon(),Language.tr("Main.Menu.View.ZoomDefault.Mnemonic"),KeyEvent.VK_MULTIPLY,"ViewZoomDefault"));
		enabledOnEditorPanel.add(createMenuItemCtrl(menu,Language.tr("Main.Menu.View.ZoomIn"),Images.ZOOM_IN.getIcon(),Language.tr("Main.Menu.View.ZoomIn.Mnemonic"),KeyEvent.VK_ADD,"ViewZoomIn"));
		enabledOnEditorPanel.add(createMenuItemCtrl(menu,Language.tr("Main.Menu.View.CenterModel"),Images.ZOOM_CENTER_MODEL.getIcon(),Language.tr("Main.Menu.View.CenterModel.Mnemonic"),KeyEvent.VK_NUMPAD0,"ViewCenterModel"));
		enabledOnEditorPanel.add(createMenuItemCtrl(menu,Language.tr("Main.Menu.View.ScrollTopLeft"),Language.tr("Main.Menu.View.ScrollTopLeft.Mnemonic"),KeyEvent.VK_HOME,"ViewTop"));
		menu.addSeparator();
		enabledOnEditorPanel.add(createMenuItemCtrlShift(menu,Language.tr("Main.Menu.View.Layers"),Images.EDIT_LAYERS.getIcon(),Language.tr("Main.Menu.View.Layers.Mnemonic"),KeyEvent.VK_F9,"ViewLayers"));
		enabledOnEditorPanel.add(createMenuItem(menu,Language.tr("Main.Menu.View.BackgroundColor"),Images.EDIT_BACKGROUND_COLOR.getIcon(),Language.tr("Main.Menu.View.BackgroundColor.Mnemonic"),"ViewBackgroundColor"));
		enabledOnEditorPanel.add(createMenuItem(menu,Language.tr("Main.Menu.View.EdgeSettings"),Images.EDIT_EDGES.getIcon(),Language.tr("Main.Menu.View.EdgeSettings.Mnemonic"),"ViewEdgeSettings"));
		enabledOnEditorPanel.add(createMenuItem(menu,Language.tr("Main.Menu.View.UserDefinedAnimationIcons"),Language.tr("Main.Menu.View.UserDefinedAnimationIcons.Mnemonic"),"ViewUserDefinedAnimationIcons"));

		/* Modell */
		menubar.add(menu=new JMenu(Language.tr("Main.Menu.Model")));
		setMnemonic(menu,Language.tr("Main.Menu.Model.Mnemonic"));

		createMenuItemShift(menu,Language.tr("Main.Menu.CheckModel"),Images.SIMULATION_CHECK.getIcon(),Language.tr("Main.Menu.CheckModel.Mnemonic"),KeyEvent.VK_F5,"ModelCheck");
		createMenuItem(menu,Language.tr("Main.Menu.Model.CheckData"),Images.SIMULATION_CHECK_DATABASE.getIcon(),Language.tr("Main.Menu.Model.CheckData.Mnemonic"),"ModelCheckData");
		menu.addSeparator();
		createMenuItemCtrl(menu,Language.tr("Main.Menu.File.ModelProperties"),Images.MODEL.getIcon(),Language.tr("Main.Menu.File.ModelProperties.Mnemonic"),KeyEvent.VK_F2,"ModelModelProperties");

		menu.add(submenu=new JMenu(Language.tr("Main.Menu.Model.ExternalData")));
		createMenuItem(submenu,Language.tr("Main.Menu.Model.ExternalData.Edit"),Images.GENERAL_SELECT_TABLE_IN_FILE.getIcon(),Language.tr("Main.Menu.Model.ExternalData.Edit.Mnemonic"),"ModelExternalDataEdit");
		createMenuItem(submenu,Language.tr("Main.Menu.Model.ExternalData.LoadNow"),Language.tr("Main.Menu.Model.ExternalData.LoadNow.Mnemonic"),"ModelExternalDataLoadNow");
		menuModelLoadExternalDataOnStart=createCheckBoxMenuItem(submenu,Language.tr("Main.Menu.Model.ExternalData.LoadOnStart"),Language.tr("Main.Menu.Model.ExternalData.LoadOnStart.Mnemonic"),"ModelExternalDataLoadOnStart");

		createMenuItem(menu,Language.tr("Main.Menu.Model.PluginFolder"),Images.MODEL_PLUGINS.getIcon(),Language.tr("Main.Menu.Model.PluginFolder.Mnemonic"),"ModelPluginFolder");

		menu.addSeparator();
		enabledOnEditorPanel.add(createMenuItem(menu,Language.tr("Main.Menu.View.ListElements"),Images.MODEL_LIST_ELEMENTS.getIcon(),Language.tr("Main.Menu.View.ListElements.Mnemonic"),"ModelListElements"));
		enabledOnEditorPanel.add(createMenuItem(menu,Language.tr("Main.Menu.View.ModelDescription"),Images.MODEL_DESCRIPTION.getIcon(),Language.tr("Main.Menu.View.ModelDescription.Mnemonic"),"ModelModelDescription"));
		enabledOnEditorPanel.add(createMenuItem(menu,Language.tr("Main.Menu.Model.Notes"),Images.MODEL_NOTES.getIcon(),Language.tr("Main.Menu.Model.Notes.Mnemonic"),"ModelModelNotes"));
		enabledOnEditorPanel.add(createMenuItem(menu,Language.tr("Main.Menu.View.ModelAnalyticModelCompare"),Images.MODEL_ANALYTIC_COMPARE.getIcon(),Language.tr("Main.Menu.View.ModelAnalyticModelCompare.Mnemonic"),"ModelAnalyticModelCompare"));

		menu.addSeparator();
		createMenuItem(menu,Language.tr("Main.Menu.Extras.KeepModel"),Images.MODEL_COMPARE_KEEP.getIcon(),Language.tr("Main.Menu.Extras.KeepModel.Mnemonic"),"ModelCompareKeep");
		menuModelCompareKept=createMenuItem(menu,Language.tr("Main.Menu.Extras.CompareWithKeptModel"),Images.MODEL_COMPARE_COMPARE.getIcon(),Language.tr("Main.Menu.Extras.CompareWithKeptModel.Mnemonic"),"ModelCompareKept");
		menuModelCompareKept.setEnabled(false);
		menuModelCompareReturn=createMenuItem(menu,Language.tr("Main.Menu.Extras.ReturnToKeptModel"),Images.MODEL_COMPARE_GO_BACK.getIcon(),Language.tr("Main.Menu.Extras.ReturnToKeptModel.Mnemonic"),"ModelCompareReturn");
		menuModelCompareReturn.setEnabled(false);
		menu.addSeparator();
		enabledOnEditorPanel.add(createMenuItem(menu,Language.tr("Main.Menu.Model.Templates"),Images.MODEL_TEMPLATES.getIcon(),Language.tr("Main.Menu.Model.Templates.Mnemonic"),KeyEvent.VK_F9,"ModelTemplates"));
		enabledOnEditorPanel.add(createMenuItemCtrl(menu,Language.tr("Main.Menu.Model.AddTemplate"),Language.tr("Main.Menu.Model.AddTemplate.Mnemonic"),KeyEvent.VK_F9,"ModelAddTemplate"));

		/* Simulation */
		menubar.add(menu=new JMenu(Language.tr("Main.Menu.Simulation")));
		setMnemonic(menu,Language.tr("Main.Menu.Simulation.Mnemonic"));

		createMenuItem(menu,Language.tr("Main.Menu.StartAnimation"),Images.ANIMATION.getIcon(),Language.tr("Main.Menu.StartAnimation.Mnemonic"),KeyEvent.VK_F6,"SimulationAnimation");
		createMenuItem(menu,Language.tr("Main.Menu.RecordAnimation"),Images.ANIMATION_RECORD.getIcon(),Language.tr("Main.Menu.RecordAnimation.Mnemonic"),"SimulationAnimationRecord");
		createMenuItem(menu,Language.tr("Main.Menu.RecordAnimationLog"),Images.ANIMATION_LOG.getIcon(),Language.tr("Main.Menu.RecordAnimationLog.Mnemonic"),"SimulationAnimationLog");

		menu.add(submenu=new JMenu(Language.tr("Main.Menu.AnimationSetup")));

		submenu.add(createTextOnlyItem(Language.tr("Main.Menu.AnimationStartMode")));
		menuSimulationAnimationStartModeRun=createRadioButtonMenuItem(submenu,Language.tr("Main.Menu.AnimationStartMode.Run"),null,"SimulationAnimationStartModeRun");
		menuSimulationAnimationStartModePause=createRadioButtonMenuItem(submenu,Language.tr("Main.Menu.AnimationStartMode.Pause"),null,"SimulationAnimationStartModePause");
		buttonGroup=new ButtonGroup();
		buttonGroup.add(menuSimulationAnimationStartModeRun);
		buttonGroup.add(menuSimulationAnimationStartModePause);
		menuSimulationAnimationStartModeRun.setSelected(!setup.animationStartPaused);
		menuSimulationAnimationStartModePause.setSelected(setup.animationStartPaused);
		submenu.addSeparator();
		submenu.add(createTextOnlyItem(Language.tr("Main.Menu.AnalogValues")));
		menuSimulationAnimationAnalogValuesFast=createRadioButtonMenuItem(submenu,Language.tr("Main.Menu.AnalogValues.Fast"),null,"SimulationAnimationAnalogValuesFast");
		menuSimulationAnimationAnalogValuesExact=createRadioButtonMenuItem(submenu,Language.tr("Main.Menu.AnalogValues.Exact"),null,"SimulationAnimationAnalogValuesExact");
		buttonGroup=new ButtonGroup();
		buttonGroup.add(menuSimulationAnimationAnalogValuesFast);
		buttonGroup.add(menuSimulationAnimationAnalogValuesExact);
		menuSimulationAnimationAnalogValuesFast.setSelected(!setup.useSlowModeAnimation);
		menuSimulationAnimationAnalogValuesExact.setSelected(setup.useSlowModeAnimation);

		submenu.addSeparator();
		submenu.add(createTextOnlyItem(Language.tr("Main.Menu.AnimationScreenshotMode")));
		menuSimulationAnimationScreenshotModeHome=createRadioButtonMenuItem(submenu,Language.tr("Main.Menu.AnimationScreenshotMode.Home")+" ("+FileSystemView.getFileSystemView().getHomeDirectory()+")",null,"SimulationAnimationScreenshotModeHome");
		String custom="";
		if (setup.imagePathAnimation!=null && !setup.imagePathAnimation.trim().isEmpty()) custom=" ("+setup.imagePathAnimation.trim()+")";
		menuSimulationAnimationScreenshotModeCustom=createRadioButtonMenuItem(submenu,Language.tr("Main.Menu.AnimationScreenshotMode.Custom")+custom,null,"SimulationAnimationScreenshotModeCustom");
		buttonGroup=new ButtonGroup();
		buttonGroup.add(menuSimulationAnimationScreenshotModeHome);
		buttonGroup.add(menuSimulationAnimationScreenshotModeCustom);
		menuSimulationAnimationScreenshotModeHome.setSelected(setup.imagePathAnimation==null || setup.imagePathAnimation.trim().isEmpty());
		menuSimulationAnimationScreenshotModeCustom.setSelected(setup.imagePathAnimation!=null && !setup.imagePathAnimation.trim().isEmpty());

		menu.addSeparator();
		createMenuItem(menu,Language.tr("Main.Menu.StartSimulation"),Images.SIMULATION.getIcon(),Language.tr("Main.Menu.StartSimulation.Mnemonic"),KeyEvent.VK_F5,"SimulationSimulation");
		createMenuItem(menu,Language.tr("Main.Menu.RecordSimulation"),Images.SIMULATION_LOG.getIcon(),Language.tr("Main.Menu.RecordSimulation.Mnemonic"),"SimulationSimulationLog");
		createMenuItem(menu,Language.tr("Main.Menu.StatisticsSetup"),Images.SIMULATION_STATISTICS.getIcon(),Language.tr("Main.Menu.StatisticsSetup.Mnemonic"),"SimulationStatisticsSetup");
		menuSimulationCheckServerConnection=createMenuItem(menu,Language.tr("Main.Menu.SimulationCheckServerConnection"),Language.tr("Main.Menu.SimulationCheckServerConnection.Mnemonic"),"SimulationCheckServerConnection");

		menu.addSeparator();
		createMenuItem(menu,Language.tr("Main.Menu.SimulationFindWarmUpSize"),Language.tr("Main.Menu.SimulationFindWarmUpSize.Mnemonic"),"SimulationFindWarmUpSize");
		createMenuItem(menu,Language.tr("Main.Menu.SimulationFindBatchSize"),Language.tr("Main.Menu.SimulationFindBatchSize.Mnemonic"),"SimulationFindBatchSize");
		menu.addSeparator();
		menu.add(submenu=new JMenu(Language.tr("Main.Menu.ParameterSeries")));
		createMenuItem(submenu,Language.tr("Main.Menu.ParameterSeriesNew"),Images.PARAMETERSERIES.getIcon(),Language.tr("Main.Menu.ParameterSeriesNew.Mnemonic"),KeyEvent.VK_F7,"SimulationParameterSeriesNew");
		createMenuItemCtrl(submenu,Language.tr("Main.Menu.ParameterSeriesLoad"),Language.tr("Main.Menu.ParameterSeriesLoad.Mnemonic"),KeyEvent.VK_F7,"SimulationParameterSeriesLoad");
		submenu.addSeparator();
		createMenuItem(submenu,Language.tr("Main.Menu.ParameterSeriesVariance"),Images.PARAMETERSERIES_VARIANCE.getIcon(),Language.tr("Main.Menu.ParameterSeriesVariance.Mnemonic"),"SimulationParameterSeriesVariance");
		createMenuItemShift(menu,Language.tr("Main.Menu.JSRunner"),Images.SCRIPTRUNNER.getIcon(),Language.tr("Main.Menu.JSRunner.Mnemonic"),KeyEvent.VK_F7,"SimulationScriptRunner");
		createMenuItem(menu,Language.tr("Main.Menu.Optimizer"),Images.OPTIMIZER.getIcon(),Language.tr("Main.Menu.Optimizer.Mnemonic"),KeyEvent.VK_F8,"SimulationOptimizer");

		/* Extras */
		menubar.add(menu=new JMenu(Language.tr("Main.Menu.Extras")));
		setMnemonic(menu,Language.tr("Main.Menu.Extras.Mnemonic"));

		createMenuItem(menu,Language.tr("Main.Menu.Extras.CompareModels"),Images.MODEL_COMPARE.getIcon(),Language.tr("Main.Menu.Extras.CompareModels.Mnemonic"),"ExtrasCompare");
		menu.addSeparator();
		createMenuItem(menu,Language.tr("Main.Menu.Extras.Calculator"),Images.EXTRAS_CALCULATOR.getIcon(),Language.tr("Main.Menu.Extras.Calculator.Mnemonic"),KeyEvent.VK_F11,"ExtrasCalculator");
		createMenuItem(menu,Language.tr("Main.Menu.Extras.QueueingCalculator"),Images.EXTRAS_QUEUE.getIcon(),Language.tr("Main.Menu.Extras.QueueingCalculator.Mnemonic"),"ExtrasQueueingCalculator");
		createMenuItem(menu,Language.tr("Main.Menu.Extras.FitDistribution"),Images.EXTRAS_FIT_DISTRIBUTION.getIcon(),Language.tr("Main.Menu.Extras.FitDistribution.Mnemonic"),"ExtrasFit");
		menu.addSeparator();
		createMenuItem(menu,Language.tr("Main.Menu.Extras.TestDatabaseConnection"),Images.EXTRAS_DATABASE_TEST.getIcon(),Language.tr("Main.Menu.Extras.TestDatabaseConnection.Mnemonic"),"ExtrasTestDatabaseConnection");
		createMenuItem(menu,Language.tr("Main.Menu.Extras.ExecuteCommand"),Images.EXTRAS_COMMANDLINE.getIcon(),Language.tr("Main.Menu.Extras.ExecuteCommand.Mnemonic"),"ExtrasExecuteCommand");
		createMenuItem(menu,Language.tr("Main.Menu.Extras.SimulationServer"),Images.EXTRAS_SERVER.getIcon(),Language.tr("Main.Menu.Extras.SimulationServer.Mnemonic"),"ExtrasSimulationServer");
		menu.addSeparator();
		createMenuItem(menu,Language.tr("Main.Menu.Extras.SystemInfo"),Images.EXTRAS_SYSTEM_INFO.getIcon(),Language.tr("Main.Menu.Extras.SystemInfo.Mnemonic"),"ExtrasSystemInfo");

		/* Hilfe */
		menubar.add(menu=new JMenu(Language.tr("Main.Menu.Help")));
		setMnemonic(menu,Language.tr("Main.Menu.Help.Mnemonic"));

		createMenuItem(menu,Language.tr("Main.Menu.Help.Help"),Images.HELP.getIcon(),Language.tr("Main.Menu.Help.Help.Mnemonic"),KeyEvent.VK_F1,"HelpHelp");
		createMenuItemShift(menu,Language.tr("Main.Menu.Help.HelpContent"),Images.HELP_CONTENT.getIcon(),Language.tr("Main.Menu.Help.HelpContent.Mnemonic"),KeyEvent.VK_F1,"HelpContent");
		menu.addSeparator();
		createMenuItem(menu,Language.tr("Main.Menu.Help.InteractiveTutorial"),Images.HELP_TUTORIAL_INTERACTIVE.getIcon(),Language.tr("Main.Menu.Help.InteractiveTutorial.Mnemonic"),"HelpInteractiveTurorial");
		createMenuItem(menu,Language.tr("Main.Menu.Help.AutomaticHelpWindow"),Images.HELP_STATIONS_INTERACTIVE.getIcon(),Language.tr("Main.Menu.Help.AutomaticHelpWindow.Mnemonic"),"HelpAutomaticHelpWindow");
		menu.addSeparator();
		createMenuItem(menu,Language.tr("Main.Menu.Help.Tutorial"),Images.HELP_TUTORIAL.getIcon(),Language.tr("Main.Menu.Help.Tutorial.Mnemonic"),"HelpTurorial");
		createMenuItem(menu,Language.tr("Main.Menu.Help.TutorialSlides"),Images.HELP_TUTORIAL.getIcon(),Language.tr("Main.Menu.Help.TutorialSlides.Mnemonic"),"HelpTurorialSlides");
		createMenuItem(menu,Language.tr("Main.Menu.Help.TutorialVideo"),Images.HELP_TUTORIAL_VIDEO.getIcon(),Language.tr("Main.Menu.Help.TutorialVideo.Mnemonic"),"HelpTurorialVideo");
		menu.add(submenu=new JMenu(Language.tr("Main.Menu.Help.References")));
		createMenuItem(submenu,Language.tr("Main.Menu.Help.ScriptingReference"),Images.HELP_SCRIPTING.getIcon(),Language.tr("Main.Menu.Help.ScriptingReference.Mnemonic"),"HelpScriptingReference");
		createMenuItem(submenu,Language.tr("Main.Menu.Help.ElementReference"),Images.HELP_STATIONS_INTERACTIVE.getIcon(),Language.tr("Main.Menu.Help.ElementReference.Mnemonic"),"HelpElementReference");
		createMenuItem(submenu,Language.tr("Main.Menu.Help.CommandLineReference"),Images.EXTRAS_COMMANDLINE.getIcon(),Language.tr("Main.Menu.Help.CommandLineReference.Mnemonic"),"HelpCommandLineReference");
		createMenuItem(submenu,Language.tr("Main.Menu.Help.HotkeyReference"),Images.HELP_HOTKEY_REFERENCE.getIcon(),Language.tr("Main.Menu.Help.HotkeyReference.Mnemonic"),"HelpHotkeyReference");
		createMenuItem(submenu,Language.tr("Main.Menu.Help.DistributionReference"),Images.EXPRESSION_BUILDER_DISTRIBUTION.getIcon(),Language.tr("Main.Menu.Help.DistributionReference.Mnemonic"),"HelpDistributionReference");
		if (BookData.getInstance().isDataAvailable()) createMenuItemCtrl(menu,Language.tr("Main.Menu.Help.Textbook"),Images.HELP_BOOK.getIcon(),Language.tr("Main.Menu.Help.Textbook.Mnemonic"),KeyEvent.VK_F1,"HelpTextbook");
		menu.add(submenu=new JMenu(Language.tr("Main.Menu.Help.RecommendedLiterature")));
		if (BookData.getInstance().isDataAvailable()) {
			createMenuItem(submenu,Language.tr("Main.Menu.Help.RecommendedLiterature.Herzog"),Images.HELP_BOOK.getIcon(),Language.tr("Main.Menu.Help.RecommendedLiterature.Mnemonic.Herzog"),"HelpLiteratureHerzog");
			submenu.addSeparator();
		}
		createMenuItem(submenu,Language.tr("Main.Menu.Help.RecommendedLiterature.GrossHarris"),Images.HELP_BOOK.getIcon(),Language.tr("Main.Menu.Help.RecommendedLiterature.Mnemonic.GrossHarris"),"HelpLiteratureGrossHarris");
		createMenuItem(submenu,Language.tr("Main.Menu.Help.RecommendedLiterature.Bolch"),Images.HELP_BOOK.getIcon(),Language.tr("Main.Menu.Help.RecommendedLiterature.Mnemonic.LiteratureBolch"),"HelpLiteratureBolch");
		createMenuItem(submenu,Language.tr("Main.Menu.Help.RecommendedLiterature.LawKelton"),Images.HELP_BOOK.getIcon(),Language.tr("Main.Menu.Help.RecommendedLiterature.Mnemonic.LiteratureLawKelton"),"HelpLiteratureLawKelton");
		createMenuItem(submenu,Language.tr("Main.Menu.Help.RecommendedLiterature.Waldmann"),Images.HELP_BOOK.getIcon(),Language.tr("Main.Menu.Help.RecommendedLiterature.Mnemonic.LiteratureWaldmann"),"HelpLiteratureWaldmann");
		createMenuItem(submenu,Language.tr("Main.Menu.Help.RecommendedLiterature.Hedtstueck"),Images.HELP_BOOK.getIcon(),Language.tr("Main.Menu.Help.RecommendedLiterature.Mnemonic.LiteratureHedtstueck"),"HelpLiteratureHedtstueck");
		createMenuItem(submenu,Language.tr("Main.Menu.Help.RecommendedLiterature.Gutenschwager"),Images.HELP_BOOK.getIcon(),Language.tr("Main.Menu.Help.RecommendedLiterature.Mnemonic.LiteratureGutenschwager"),"HelpLiteratureGutenschwager");
		menu.addSeparator();
		createMenuItem(menu,Language.tr("Main.Menu.Help.Support"),Images.HELP_EMAIL.getIcon(),Language.tr("Main.Menu.Help.Support.Mnemonic"),"HelpSupport");
		createMenuItem(menu,Language.tr("Main.Menu.Help.SupportData"),Images.HELP_BUG_REPORT.getIcon(),Language.tr("Main.Menu.Help.SupportData.Mnemonic"),"HelpSupportData");
		createMenuItem(menu,Language.tr("Main.Menu.Help.Homepage"),Images.HELP_HOMEPAGE.getIcon(),Language.tr("Main.Menu.Help.Homepage.Mnemonic"),"HelpHomepage");
		createMenuItem(menu,Language.tr("Main.Menu.Help.Updates"),Images.SETUP_PAGE_UPDATE.getIcon(),Language.tr("Main.Menu.Help.Updates.Mnemonic"),"HelpUpdates");
		menu.addSeparator();
		createMenuItem(menu,Language.tr("Main.Menu.Help.UsageStatistics"),Language.tr("Main.Menu.Help.UsageStatistics.Mnemonic"),"HelpUsageStatistics");
		createMenuItem(menu,Language.tr("Main.Menu.Help.LicenseInformation"),Language.tr("Main.Menu.Help.LicenseInformation.Mnemonic"),"HelpLicense");
		createMenuItemCtrlShift(menu,Language.tr("Main.Menu.Help.ProgramInformation"),Images.GENERAL_INFO.getIcon(),Language.tr("Main.Menu.Help.ProgramInformation.Mnemonic"),KeyEvent.VK_F1,"HelpInfo");

		final SetupData setup=SetupData.getSetup(); /* Diese Methode wird vom Konstruktor aufgerufen, daher ist das Feld "setup" noch nicht gesetzt. */

		SwingUtilities.invokeLater(()->{ /* Das Quick-Access-Eingabefeld darf erst nach dem Konstruktor (nach dem Abarbeiten von initialen Ereignissen) angelegt werden, weil sonst initiale Darg-Over-Ereignisse evtl. das ganze Programm blockieren können. */

			if (setup.showMemoryUsage || setup.showQuickAccess || setup.showFeedbackButton) {
				menubar.add(Box.createHorizontalGlue());
			}

			/* Speicher */
			if (setup.showMemoryUsage) {
				menubar.add(memoryUsage=new JLabel());
				final Font font=memoryUsage.getFont();
				memoryUsage.setFont(new Font(font.getFontName(),0,font.getSize()-2));
				memoryUsage.addMouseListener(new MouseAdapter() {
					@Override public void mousePressed(MouseEvent e) {
						if (e.getClickCount()==2 && SwingUtilities.isRightMouseButton(e)) memoryUsage.setVisible(false); else System.gc();
					}
				});
				final Timer timer=new Timer("MemoryUsage",true);
				timer.schedule(new TimerTask() {
					private long lastTimeStamp=0;
					private Map<Long,Long> lastLoad;
					private final MemoryMXBean memory=ManagementFactory.getMemoryMXBean();
					private final ThreadMXBean threads=ManagementFactory.getThreadMXBean();
					private final int count=Runtime.getRuntime().availableProcessors();
					@Override
					public void run() {
						final long timeStamp=System.currentTimeMillis();
						long sum=0;
						final Map<Long,Long> load=new HashMap<>();
						for (long id: threads.getAllThreadIds()) {
							final long l=threads.getThreadCpuTime(id);
							load.put(id,l);
							sum+=(lastLoad==null)?l:(l-lastLoad.getOrDefault(id,0L));
						}
						lastLoad=load;
						final long delta=(timeStamp-lastTimeStamp)*1_000_000;
						final long factor=Math.min(100,Math.round(((double)sum)/count/delta*100));
						lastTimeStamp=timeStamp;

						final long l1=memory.getHeapMemoryUsage().getUsed();
						final long l2=memory.getNonHeapMemoryUsage().getUsed();
						final long l=(l1+l2)/1024/1024;
						memoryUsage.setText(NumberTools.formatLong(l)+" MB "+NumberTools.formatNumber(factor)+"% ");
					}
				},1000,1000);
			}

			/* QuickAccess */
			if (setup.showQuickAccess) {
				menubar.add(quickAccess=JQuickAccess.buildQuickAccessField(quickAccessText->getCurrentQuickAccessRecords(quickAccessText),false));
				if (FlatLaFHelper.isCombinedMenuBar()) SwingUtilities.invokeLater(()->{
					JQuickAccess.textFieldResizer(quickAccess,5,14);
					final int h=quickAccess.getHeight();
					if (h>25) quickAccess.setMaximumSize(new Dimension(quickAccess.getWidth(),h-2));
				});
				menubar.setFocusTraversalPolicy(JQuickAccess.getMenuTraversalPolicy());
				menubar.setFocusCycleRoot(true);
				editorPanel.requestFocus();
			}

			/* Feedback */
			if (setup.showFeedbackButton && !FlatLaFHelper.isCombinedMenuBar()) {
				final JLabel label;
				menubar.add(label=new JLabel("<html><body><span style=\"color: blue; text-decoration: underline;\">"+Language.tr("Main.Toolbar.Feedback")+"</span></body></html>"));
				label.setToolTipText(Language.tr("Main.Toolbar.Feedback.Hint"));
				label.setBorder(BorderFactory.createEmptyBorder(0,10,0,0));
				label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				label.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(final MouseEvent e) {
						if (SwingUtilities.isLeftMouseButton(e)) commandHelpSupport();
					}
				});
				label.setSize(label.getPreferredSize());
				label.setMaximumSize(label.getPreferredSize());
				menubar.add(Box.createHorizontalStrut(10));
			}

		});

		return menubar;
	}

	/**
	 * Liefert basierend auf einem Suchtext passende Schnellzugriffs-Einträge
	 * @param quickAccessText	Eingegebener Suchtext
	 * @return	Liste mit passenden Schnellzugriffs-Einträgen
	 * @see #quickAccess
	 */
	private List<JQuickAccessRecord> getCurrentQuickAccessRecords(final String quickAccessText) {
		final JQuickAccess quickAccess=new JQuickAccess(quickAccessText);
		final List<JQuickAccessRecord> list=new ArrayList<>();

		for (JQuickAccessBuilder builder: quickAccess.getActiveQuickAccessBuilders()) {

			if (builder instanceof JQuickAccessBuilderNewElements) {
				final JQuickAccessBuilderNewElements builderNewElements=(JQuickAccessBuilderNewElements)builder;
				builderNewElements.work(element->editorPanel.selectTemplateInList(element));
				list.addAll(builderNewElements.getList(5));
			}

			if (builder instanceof JQuickAccessBuilderElementsList) {
				final JQuickAccessBuilderElementsList builderElementsList=(JQuickAccessBuilderElementsList)builder;
				builderElementsList.work(editorPanel.model.surface,Id->editorPanel.selectAndScrollToElement(Id));
				list.addAll(builderElementsList.getList(15));
			}

			if (builder instanceof JQuickAccessBuilderMenu) {
				final JQuickAccessBuilderMenu builderMenu=(JQuickAccessBuilderMenu)builder;
				builderMenu.work(((JFrame)ownerWindow).getJMenuBar());
				list.addAll(builderMenu.getList(5));
			}

			if (builder instanceof JQuickAccessBuilderLastFiles) {
				final JQuickAccessBuilderLastFiles builderLastFiles=(JQuickAccessBuilderLastFiles)builder;
				builderLastFiles.work(file->loadAnyFile(file,null,null,true));
				list.addAll(builderLastFiles.getList());
			}

			if (builder instanceof JQuickAccessBuilderStatistics && statisticsPanel.getStatistics()!=null) {
				final JQuickAccessBuilderStatistics builderStatistics=(JQuickAccessBuilderStatistics)builder;
				builderStatistics.work(statisticsPanel,()->setCurrentPanel(statisticsPanel));
				list.addAll(builderStatistics.getList(5));
			}

			if (builder instanceof JQuickAccessBuilderModelProperties) {
				final JQuickAccessBuilderModelProperties builderModelProperties=(JQuickAccessBuilderModelProperties)builder;
				builderModelProperties.work(initialPage->editorPanel.showModelPropertiesDialog(initialPage));
				list.addAll(builderModelProperties.getList());
			}

			if (builder instanceof JQuickAccessBuilderExamples) {
				final JQuickAccessBuilderExamples builderExamples=(JQuickAccessBuilderExamples)builder;
				builderExamples.work(this,newModel->commandFileModelExample(newModel));
				list.addAll(builderExamples.getList(5));
			}

			if (builder instanceof JQuickAccessBuilderCalc) {
				final JQuickAccessBuilderCalc builderCalc=(JQuickAccessBuilderCalc)builder;
				builderCalc.work(expression->commandExtrasCalculator(expression,null));
				list.addAll(builderCalc.getList());
			}

			if (builder instanceof JQuickAccessBuilderDistributions) {
				final JQuickAccessBuilderDistributions builderDistributions=(JQuickAccessBuilderDistributions)builder;
				builderDistributions.work(distribution->commandExtrasCalculator(null,distribution));
				list.addAll(builderDistributions.getList());
			}

			if (builder instanceof JQuickAccessBuilderSettings) {
				final JQuickAccessBuilderSettings builderSettings=(JQuickAccessBuilderSettings)builder;
				builderSettings.work(page->commandFileSetup(page));
				list.addAll(builderSettings.getList(5));
			}

			if (builder instanceof JQuickAccessBuilderBook) {
				final JQuickAccessBuilderBook builderBook=(JQuickAccessBuilderBook)builder;
				builderBook.work(match->commandHelpBook(match));
				list.addAll(builderBook.getList(10));
			}
		}

		return list;
	}

	@Override
	protected void enableMenuBar(final boolean enable) {
		super.enableMenuBar(enable);
		if (quickAccess!=null) quickAccess.setVisible(enable);
	}

	/**
	 * Aktualisiert die Liste der zuletzt verwendeten Dateien im Menü.
	 * @see #menuFileModelRecentlyUsed
	 */
	private void updateRecentlyUsedList() {
		menuFileModelRecentlyUsed.removeAll();
		menuFileModelRecentlyUsed.setEnabled(setup.useLastFiles && setup.lastFiles!=null && setup.lastFiles.length>0);
		if (!menuFileModelRecentlyUsed.isEnabled()) return;

		for (int i=0;i<setup.lastFiles.length;i++) {
			final JMenuItem sub=new JMenuItem(setup.lastFiles[i]);
			sub.addActionListener(actionListener);
			menuFileModelRecentlyUsed.add(sub);
		}

		if (setup.lastFiles.length>0) {
			menuFileModelRecentlyUsed.addSeparator();
			final JMenuItem sub=new JMenuItem(Language.tr("Main.Menu.File.RecentlyUsed.Delete"));
			sub.setIcon(Images.GENERAL_OFF.getIcon());
			sub.addActionListener(e->{
				setup.lastFiles=new String[0];
				setup.saveSetup();
				updateRecentlyUsedList();
			});
			menuFileModelRecentlyUsed.add(sub);
		}
	}

	/**
	 * Fügt einen Eintrag zu der Liste der zuletzt verwendeten Dateien hinzu,
	 * speichert das Setup und baut das Menü entsprechend neu auf.
	 * @param fileName	Dateiname, der zu der Liste hinzugefügt werden soll (wenn er nicht bereits enthalten ist)
	 */
	private void addFileToRecentlyUsedList(String fileName) {
		final ArrayList<String> files=(setup.lastFiles==null)?new ArrayList<>():new ArrayList<>(Arrays.asList(setup.lastFiles));

		int index=files.indexOf(fileName);
		if (index==0) return; /* Eintrag ist bereits ganz oben in der Liste, nichts zu tun */
		if (index>0) files.remove(index); /* Wenn schon in Liste: Element an alter Position entfernen */
		files.add(0,fileName); /* Element ganz vorne einfügen */
		while (files.size()>5) files.remove(files.size()- 1); /* Maximal die letzten 5 Dateien merken */

		if (setup.useLastFiles) {
			setup.lastFiles=files.toArray(new String[0]);
		} else {
			setup.lastFiles=null;
		}
		setup.saveSetup();
		reloadSetup();
	}

	/**
	 * Darf das aktuelle Modell verworfen werden?
	 * @return	Liefert <code>true</code>, wenn das aktuell im Editor befindliche Modell verworfen werden darf
	 */
	private boolean isDiscardModelOk() {
		if (!editorPanel.isModelChanged()) return true;

		switch (MsgBox.confirmSave(getOwnerWindow(),Language.tr("Window.DiscardConfirmation.Title"),Language.tr("Window.DiscardConfirmation.Info"))) {
		case JOptionPane.YES_OPTION: commandFileModelSave(false); return isDiscardModelOk();
		case JOptionPane.NO_OPTION: return true;
		case JOptionPane.CANCEL_OPTION: return false;
		default: return false;
		}
	}

	@Override
	public boolean allowQuitProgram() {
		if (currentPanel==null) return false;
		if (currentPanel==waitPanel) {waitPanel.abortSimulation(); return false;}
		if (currentPanel==specialPanel) {specialPanel.requestClose(); return false;}
		if (currentPanel==animationPanel) {animationPanel.closeRequest(); return false;}
		if (setup.autoRestore) {
			return true;
		} else {
			return isDiscardModelOk();
		}
	}

	/**
	 * Kann das Panel in seiner aktuellen Form serialisiert und neu geladen werden?
	 * @return	Liefert <code>true</code>, wenn das Panel in seiner aktuellen Form per {@link #getAllData()} gespeichert werden kann
	 */
	public boolean allowReloadWindow() {
		/* Editor oder Statistikansicht aktiv? */
		if (currentPanel!=editorPanel && currentPanel!=statisticsPanel) return false;

		/* Offener modaler Dialog, der von diesem Fenster ausgeht? */
		final Window[] windows=Window.getWindows();
		if(windows!=null) for(Window window: windows) {
			if (!window.isShowing()) continue;
			if (!(window instanceof Dialog)) continue;
			final Dialog dialog=(Dialog)window;
			if (!dialog.isModal()) continue;

			Container c=dialog.getParent();
			while (c!=null) {
				if (c==ownerWindow) return false;
				c=c.getParent();
			}

			return true;
		}

		return true;
	}

	/**
	 * Prüft, ob eine Datei existiert
	 * @param file	Zu prüfende Datei
	 * @param errorMessageOnFail	Soll eine Fehlermeldung ausgegeben werden, wenn die Datei nicht existiert?
	 * @return	Liefert <code>true</code>, wenn die Datei existiert
	 * @see #loadAnyFile(File, Component, Point, boolean)
	 */
	private boolean checkFileExists(File file, boolean errorMessageOnFail) {
		if (file==null) {
			if (errorMessageOnFail)	MsgBox.error(getOwnerWindow(),Language.tr("XML.LoadErrorTitle"),Language.tr("XML.NoFileSelected"));
			return false;
		}
		if (!file.exists()) {
			if (errorMessageOnFail) MsgBox.error(getOwnerWindow(),Language.tr("XML.LoadErrorTitle"),String.format(Language.tr("XML.FileNotFound"),file.toString()));
			return false;
		}

		return true;
	}

	/**
	 * Versucht das Modell oder Statistikdaten aus base64 encodierten Daten zu laden
	 * @param file	Datei der die Daten entstammen
	 * @param base64data	base64 codierte Daten
	 * @return	Liefert <code>true</code>, wenn der Ladevorgang erfolgreich war
	 * @see #tryLoadHTML(File)
	 */
	private boolean processBase64ModelData(final File file, final String base64data) {
		try {
			final ByteArrayInputStream in=new ByteArrayInputStream(Base64.getDecoder().decode(base64data));
			return commandFileModelLoadFromStream(file,in);
		} catch (IllegalArgumentException e) {return false;}
	}

	/**
	 * Versucht das Modell oder Statistikdaten aus HTML-Daten zu laden
	 * @param file	Zu ladende Datei
	 * @return	Liefert <code>true</code>, wenn der Ladevorgang erfolgreich war
	 */
	private boolean tryLoadHTML(final File file) {
		boolean firstLine=true;
		boolean modelDataFollow=false;

		final List<String> lines=Table.loadTextLinesFromFile(file);
		if (lines==null) return false;

		for (String line: lines) {
			if (firstLine) {
				if (!line.trim().equalsIgnoreCase("<!doctype html>")) return false;
			} else {
				if (modelDataFollow) {
					if (!line.trim().startsWith("data:application/xml;base64,")) return false;
					return processBase64ModelData(file,line.trim().substring("data:application/xml;base64,".length()));
				} else {
					if (line.trim().equalsIgnoreCase("QSModel")) modelDataFollow=true;
				}
			}
			firstLine=false;
		}

		return false;
	}

	/**
	 * Versucht das Modell oder Statistikdaten aus HTML-Daten zu laden
	 * @param data	Zu ladende Daten
	 * @return	Liefert <code>true</code>, wenn der Ladevorgang erfolgreich war
	 */
	private boolean tryLoadHTML(final byte[] data) {
		boolean firstLine=true;
		boolean modelDataFollow=false;

		final String text=new String(data,StandardCharsets.UTF_8);
		final String[] lines=text.split("\n");

		for (String line: lines) {
			if (firstLine) {
				if (!line.trim().equalsIgnoreCase("<!doctype html>")) return false;
			} else {
				if (modelDataFollow) {
					if (!line.trim().startsWith("data:application/xml;base64,")) return false;
					return processBase64ModelData(null,line.trim().substring("data:application/xml;base64,".length()));
				} else {
					if (line.trim().equalsIgnoreCase("QSModel")) modelDataFollow=true;
				}
			}
			firstLine=false;
		}

		return false;
	}

	/**
	 * Versucht ein Bild aus einem Byte-Array zu laden
	 * @param data	Zu ladende Daten
	 * @param dropComponent	Komponente auf der die Tabelle abgelegt wurde
	 * @param dropPosition	Position innerhalb der Komponente auf der die Tabelle abgelegt wurde
	 * @return	Liefert <code>true</code>, wenn der Ladevorgang erfolgreich war
	 */
	private boolean tryLoadImage(final byte[] data, final Component dropComponent, final Point dropPosition) {
		try (InputStream inputStream=new ByteArrayInputStream(data)) {
			final BufferedImage image=ImageIO.read(inputStream);
			if (image==null) return false;
			if (!(dropComponent instanceof ModelSurfacePanel)) return false;
			((ModelSurfacePanel)dropComponent).addImageElement(image,dropPosition);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * Reagiert darauf, wenn eine Tabelle per Drag&amp;drop auf dem Programmfenster abgelegt wurde
	 * und versucht daraus eine Tabellenquelle zu generieren.
	 * @param file	Abgelegte Datei
	 * @param dropComponent	Komponente auf der die Tabelle abgelegt wurde
	 * @param dropPosition	Position innerhalb der Komponente auf der die Tabelle abgelegt wurde
	 * @return	Wurde das Ereignis verarbeitet?
	 * @see #loadAnyFile(File, Component, Point, boolean)
	 * @see ModelSurfacePanel#addTableFileBasedElement(File, Point)
	 */
	private boolean tryLoadTable(final File file, final Component dropComponent, final Point dropPosition) {
		boolean ok=false;
		for (SaveMode mode: Table.SaveMode.values()) if (mode.loadable && mode.fileNameMatch(file.toString())) {ok=true; break;}
		if (!ok) return false;

		if (!(dropComponent instanceof ModelSurfacePanel)) return false;
		((ModelSurfacePanel)dropComponent).addTableFileBasedElement(file,dropPosition);
		return true; /* Auch dann true zurückgeben, wenn das Laden vom Nutzer abgebrochen wurde, da ja keine weitere Verarbeitung mehr erfolgen soll. */
	}

	/**
	 * Versucht eine URL-Datei zu laden und zu interpretieren.
	 * @param file	URL-Datei
	 * @param dropComponent	Komponente auf der die Tabelle abgelegt wurde
	 * @param dropPosition	Position innerhalb der Komponente auf der die Tabelle abgelegt wurde
	 * @param errorMessageOnFail		Soll im Falle eines Fehlers eine Meldung ausgegeben werden?
	 * @return	Liefert <code>true</code>, wenn der Ladevorgang erfolgreich war
	 */
	private boolean tryLoadURL(final File file, final Component dropComponent, final Point dropPosition, final boolean errorMessageOnFail) {
		final URLLoader loader=new URLLoader(file);
		final byte[] data=loader.process(this,errorMessageOnFail);
		if (data==null) return false;

		/* Modell aus HTML-Datei laden */
		if (tryLoadHTML(data)) return true;

		/* Bild laden */
		if (tryLoadImage(data,dropComponent,dropPosition)) return true;

		/* XML oder json laden */
		return loadAnyXMLStream(new ByteArrayInputStream(data),errorMessageOnFail);
	}

	@Override
	public boolean loadAnyFile(final File file, final Component dropComponent, final Point dropPosition, final boolean errorMessageOnFail) {
		if (!checkFileExists(file,errorMessageOnFail)) return false;

		if (file.toString().toUpperCase().endsWith(".URL")) {
			return tryLoadURL(file,dropComponent,dropPosition,errorMessageOnFail);
		}

		/* Modell aus HTML-Datei laden */
		if (tryLoadHTML(file)) return true;

		/* Tabelle laden */
		if (dropPosition!=null && currentPanel==editorPanel) {
			if (tryLoadTable(file,dropComponent,dropPosition)) return true;
		}

		/* XML oder json laden */
		return loadAnyXMLFile(file,errorMessageOnFail);
	}

	/**
	 * Versucht ein Modell, eine Parameterreihe usw. aus einer xml-Datei zu laden
	 * @param file	Zu ladende Datei
	 * @param errorMessageOnFail	Soll im Falle eines Fehlers eine Meldung ausgegeben werden?
	 * @return	Gibt <code>true</code> zurück, wenn die Datei erfolgreich geladen werden konnte.
	 */
	private boolean loadAnyXMLFile(final File file, final boolean errorMessageOnFail) {
		final XMLTools xml=new XMLTools(file);
		final Element root=xml.load();
		if (root==null) {
			if (errorMessageOnFail) MsgBox.error(getOwnerWindow(),Language.tr("XML.LoadErrorTitle"),xml.getError());
			return false;
		}

		final String name=root.getNodeName();

		for (String test: new EditModel().getRootNodeNames()) if (name.equalsIgnoreCase(test)) {
			if (currentPanel!=editorPanel && currentPanel!=statisticsPanel && (welcomePanel==null || currentPanel!=welcomePanel)) return false; /* Nur wenn Hauptpanel aktiv */
			return commandFileModelLoad(root,file);
		}
		for (String test: new Statistics().getRootNodeNames()) if (name.equalsIgnoreCase(test)) {
			if (currentPanel!=editorPanel && currentPanel!=statisticsPanel && (welcomePanel==null || currentPanel!=welcomePanel)) return false; /* Nur wenn Hauptpanel aktiv */
			return commandFileStatisticsLoad(root,file);
		}
		for (String test: new OptimizerSetup().getRootNodeNames()) if (name.equalsIgnoreCase(test)) {
			if ((currentPanel instanceof OptimizerPanel)) {
				return ((OptimizerPanel)currentPanel).dragDropLoadFile(file);
			} else {
				if (!MsgBox.confirm(getOwnerWindow(),Language.tr("XML.OptimizerNotActive.Title"),Language.tr("XML.OptimizerNotActive.Info"),Language.tr("XML.OptimizerNotActive.YesInfo"),Language.tr("XML.OptimizerNotActive.NoInfo"))) return false;

				if (!commandSimulationOptimizer()) return false;
				return ((OptimizerPanel)currentPanel).dragDropLoadFile(file);
			}
		}
		for (String test: new ParameterCompareSetup(null).getRootNodeNames()) if (name.equalsIgnoreCase(test)) {
			if ((currentPanel instanceof ParameterComparePanel)) {
				return ((ParameterComparePanel)currentPanel).dragDropLoadFile(this,file);
			} else {
				if (!MsgBox.confirm(getOwnerWindow(),Language.tr("XML.ParameterCompareNotActive.Title"),Language.tr("XML.ParameterCompareNotActive.Info"),Language.tr("XML.ParameterCompareNotActive.YesInfo"),Language.tr("XML.ParameterCompareNotActive.NoInfo"))) return false;
				return commandSimulationParameterSeriesLoad(file,null);
			}
		}

		/* Unbekanntes (XML-)Format */
		if (errorMessageOnFail) MsgBox.error(getOwnerWindow(),Language.tr("XML.LoadErrorTitle"),Language.tr("XML.UnknownFileFormat"));

		return false;
	}

	/**
	 * Versucht ein Modell, eine Parameterreihe usw. aus einem xml-Datenstream zu laden
	 * @param stream	Zu ladender Stream
	 * @param errorMessageOnFail	Soll im Falle eines Fehlers eine Meldung ausgegeben werden?
	 * @return	Gibt <code>true</code> zurück, wenn die Datei erfolgreich geladen werden konnte.
	 */
	private boolean loadAnyXMLStream(final InputStream stream, final boolean errorMessageOnFail) {
		final XMLTools xml=new XMLTools(stream);
		final Element root=xml.load();
		if (root==null) {
			if (errorMessageOnFail) MsgBox.error(getOwnerWindow(),Language.tr("XML.LoadErrorTitle"),xml.getError());
			return false;
		}

		final String name=root.getNodeName();

		for (String test: new EditModel().getRootNodeNames()) if (name.equalsIgnoreCase(test)) {
			if (currentPanel!=editorPanel && currentPanel!=statisticsPanel && (welcomePanel==null || currentPanel!=welcomePanel)) return false; /* Nur wenn Hauptpanel aktiv */
			return commandFileModelLoad(root,null);
		}
		for (String test: new Statistics().getRootNodeNames()) if (name.equalsIgnoreCase(test)) {
			if (currentPanel!=editorPanel && currentPanel!=statisticsPanel && (welcomePanel==null || currentPanel!=welcomePanel)) return false; /* Nur wenn Hauptpanel aktiv */
			return commandFileStatisticsLoad(root,null);
		}

		for (String test: new OptimizerSetup().getRootNodeNames()) if (name.equalsIgnoreCase(test)) {
			if ((currentPanel instanceof OptimizerPanel)) {
				return ((OptimizerPanel)currentPanel).dragDropLoadFile(stream);
			} else {
				if (!MsgBox.confirm(getOwnerWindow(),Language.tr("XML.OptimizerNotActive.Title"),Language.tr("XML.OptimizerNotActive.Info"),Language.tr("XML.OptimizerNotActive.YesInfo"),Language.tr("XML.OptimizerNotActive.NoInfo"))) return false;

				if (!commandSimulationOptimizer()) return false;
				return ((OptimizerPanel)currentPanel).dragDropLoadFile(stream);
			}
		}
		for (String test: new ParameterCompareSetup(null).getRootNodeNames()) if (name.equalsIgnoreCase(test)) {
			if ((currentPanel instanceof ParameterComparePanel)) {
				return ((ParameterComparePanel)currentPanel).dragDropLoadFile(this,stream);
			} else {
				if (!MsgBox.confirm(getOwnerWindow(),Language.tr("XML.ParameterCompareNotActive.Title"),Language.tr("XML.ParameterCompareNotActive.Info"),Language.tr("XML.ParameterCompareNotActive.YesInfo"),Language.tr("XML.ParameterCompareNotActive.NoInfo"))) return false;
				return commandSimulationParameterSeriesLoad(null,stream);
			}
		}

		/* Unbekanntes (XML-)Format */
		if (errorMessageOnFail) MsgBox.error(getOwnerWindow(),Language.tr("XML.LoadErrorTitle"),Language.tr("XML.UnknownFileFormat"));

		return false;
	}

	/**
	 * Liefert die Versionskennung der Java-Laufzeitumgebung
	 * @return	Versionskennung zerlegt in einzelne Teile
	 * @see #startUpChecks()
	 */
	private static int[] getJavaVersion() {
		final String version=System.getProperty("java.version");
		if (version==null) return new int[]{7,0};

		if (version.startsWith("1.8")) {
			final String[] ver=version.split("_");
			if (ver.length!=2 || !ver[0].equals("1.8.0")) return new int[]{8,0};
			int security=0;
			try {security=Integer.parseInt(ver[1]);} catch (Exception e) {security=0;}
			return new int[]{8,security};
		}

		final String[] ver=version.split("\\.");

		int major=0;
		try {major=Integer.parseInt(ver[0]);} catch (Exception e) {major=0;}
		if (major==0) return new int[]{7,0};
		int security=0;
		if (ver.length>1) try {
			final String part=ver[ver.length-1].split("\\-")[0];
			security=Integer.parseInt(part);
		} catch (Exception e) {security=0;}
		return new int[]{major,security};
	}

	/**
	 * Prüft, ob die verwendete Java-Version noch aktuell ist
	 * und zeigt ggf. eine Warnung an.
	 */
	private void startUpChecks() {
		if (setup.languageWasAutomaticallySet()) {
			final JPanel infoPanel=setMessagePanel("",Language.tr("Window.LanguageAutomatic"),MessagePanelIcon.INFO);
			infoPanel.setBackground(new Color(255,255,240));
			new Timer("HideLanguageInfoPanel").schedule(new TimerTask() {@Override public void run() {
				setMessagePanel(null,null,null);
				infoPanel.setBackground(new Color(255,240,0));
			}},7500);
		} else {
			final UpdateSystem update=UpdateSystem.getUpdateSystem();
			if (setup.autoUpdate==SetupData.AutoUpdate.SEARCH || (setup.autoUpdate==SetupData.AutoUpdate.INSTALL && !update.isAutomaticUpdatePossible())) {
				update.checkUpdateNow(true);
				if (update.isNewVersionAvailable()==UpdateSystem.NewVersionAvailableStatus.NEW_VERSION_AVAILABLE) {
					final JPanel panel=setMessagePanel(Language.tr("Dialog.Title.Info"),update.getInfoString(),"https://"+WEB_URL,MessagePanelIcon.INFO);
					panel.setBackground(Color.GREEN);
					new Timer("HideUpdateInfoPanel").schedule(new TimerTask() {@Override public void run() {setMessagePanel(null,null,null);}},7500);
				}
			} else {
				if (!setup.testJavaVersion) return;
				final int[] ver=getJavaVersion();
				boolean ok=true;
				if (ver[0]==8 && ver[1]<JAVA8_SECURE_MIN_VERSION) ok=false;
				if (ver[0]==9 && ver[1]<JAVA9_SECURE_MIN_VERSION) ok=false;
				if (ver[0]==10 && ver[1]<JAVA10_SECURE_MIN_VERSION) ok=false;
				if (ver[0]==11 && ver[1]<JAVA11_SECURE_MIN_VERSION) ok=false;
				if (ver[0]==12 && ver[1]<JAVA12_SECURE_MIN_VERSION) ok=false;
				if (ver[0]==13 && ver[1]<JAVA13_SECURE_MIN_VERSION) ok=false;
				if (ver[0]==14 && ver[1]<JAVA14_SECURE_MIN_VERSION) ok=false;
				if (ver[0]==15 && ver[1]<JAVA15_SECURE_MIN_VERSION) ok=false;
				if (ver[0]==16 && ver[1]<JAVA16_SECURE_MIN_VERSION) ok=false;
				if (ok) return;
				setMessagePanel(Language.tr("Dialog.Title.Warning"),Language.tr("Window.JavaSecurityWarnung"),Language.tr("Window.JavaSecurityWarnung.Link"),MessagePanelIcon.WARNING);
				new Timer("HideSecurityInfoPanel").schedule(new TimerTask() {@Override public void run() {setMessagePanel(null,null,null);}},7500);
			}
		}
	}

	/**
	 * Befehl: Datei - Neu
	 * @return	Liefert <code>true</code>, wenn ein neues Modell angelegt werden konnte
	 */
	private boolean commandFileModelNew() {
		if (!isDiscardModelOk()) return false;
		editorPanel.setModel(null);
		statisticsPanel.setStatistics(null);
		for (AbstractButton button: enabledOnStatisticsAvailable) button.setEnabled(false);
		setAdditionalTitle(UNSAVED_MODEL);
		setCurrentPanel(editorPanel);
		return true;
	}

	/**
	 * Befehl: Datei - Beispiel laden
	 */
	private void commandFileModeExample() {
		final SelectExampleDialog dialog=new SelectExampleDialog(this);
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) commandFileModelExample(dialog.getExample());
	}

	/**
	 * Befehl: Datei - Beispiel laden - Nummer des Beispiels
	 * @param index	Nummer des zu ladenden Beispiels
	 */
	private void commandFileModelExample(final int index) {
		final EditModel newModel=EditModelExamples.getExampleByIndex(getOwnerWindow(),index);
		if (newModel==null) return;
		if (FlatLaFHelper.isDark()) EditModelDark.processModel(newModel,EditModelDark.ColorMode.LIGHT,EditModelDark.ColorMode.DARK);
		commandFileModelExample(newModel);
	}

	/**
	 * Lädt ein Beispiel nach dem Neuladen des Fensters neu
	 * @param newModel	Zu ladendes Beispielmodell
	 */
	private void commandFileModelExample(final EditModel newModel) {
		if (newModel==null) return;
		if (!isDiscardModelOk()) return;
		editorPanel.setModel(newModel);
		statisticsPanel.setStatistics(null);
		for (AbstractButton button: enabledOnStatisticsAvailable) button.setEnabled(false);
		setAdditionalTitle(UNSAVED_MODEL);
		setCurrentPanel(editorPanel);
		editorPanel.scrollToTop();
	}

	/**
	 * Befehl: Datei - Modellgenerator
	 */
	private void commandFileModelGenerator() {
		if (!isDiscardModelOk()) return;
		final ModelGeneratorDialog dialog=new ModelGeneratorDialog(this);
		final EditModel newModel=dialog.getModel();
		if (newModel!=null) {
			editorPanel.setModel(newModel);
			statisticsPanel.setStatistics(null);
			for (AbstractButton button: enabledOnStatisticsAvailable) button.setEnabled(false);
			setAdditionalTitle(UNSAVED_MODEL);
			setCurrentPanel(editorPanel);
			editorPanel.scrollToTop();
		}
	}

	/**
	 * Befehl: Datei - Neues Fenster
	 */
	private void commandFileNewWindow() {
		new MainFrame(null,null);
	}

	/**
	 * Befehl: Modell - Laden
	 * @return	Liefert <code>true</code> wenn ein Modell, die Parameterreihe usw. geladen wurde
	 */
	private boolean commandFileModelLoadAll() {
		if (!isDiscardModelOk()) return true;

		final File file=XMLTools.showLoadDialog(getParent(),EditorPanelBase.LOAD_MODEL);
		if (file==null) return false;

		return loadAnyXMLFile(file,true);
	}

	/**
	 * Befehl: Modell - Laden
	 * @param rootOptional	XML-Root-Element (kann <code>null</code> sein)
	 * @param file	Zu ladende Datei (wird <code>null</code> übergeben, so wird ein Dateiauswahldialog angezeigt)
	 * @return	Liefert <code>true</code> wenn ein Modell geladen wurde
	 */
	private boolean commandFileModelLoad(final Element rootOptional, final File file) {
		if (!isDiscardModelOk()) return true;
		final String error;
		if (rootOptional!=null) {
			error=editorPanel.loadModel(rootOptional,file);
		} else {
			error=editorPanel.loadModel(file);
		}
		if (error==null) {
			statisticsPanel.setStatistics(null);
			for (AbstractButton button: enabledOnStatisticsAvailable) button.setEnabled(false);
			setCurrentPanel(editorPanel);
			editorPanel.smartCenterModel();
		} else {
			MsgBox.error(getOwnerWindow(),Language.tr("XML.LoadErrorTitle"),error);
		}
		if (editorPanel.getLastFile()!=null) {
			addFileToRecentlyUsedList(editorPanel.getLastFile().toString());
			setAdditionalTitle(editorPanel.getLastFile().getName());
			CommonVariables.setInitialDirectoryFromFile(editorPanel.getLastFile());
		}
		return error==null;
	}

	/**
	 * Versucht das Modell oder Statistikdaten aus einem Stream zu laden
	 * @param file	Datei der die Daten entstammen
	 * @param stream	Input-Stream aus dem die Daten geladen werden sollen
	 * @return	Liefert <code>true</code>, wenn der Ladevorgang erfolgreich war
	 * @see #processBase64ModelData(File, String)
	 */
	private boolean commandFileModelLoadFromStream(final File file, final InputStream stream) {
		if (!isDiscardModelOk()) return true;

		final XMLTools xml=new XMLTools(stream);
		final Element root=xml.load();
		if (root==null) {
			MsgBox.error(getOwnerWindow(),Language.tr("XML.LoadErrorTitle"),xml.getError());
			return false;
		}

		final String name=root.getNodeName();

		for (String test: new EditModel().getRootNodeNames()) if (name.equalsIgnoreCase(test)) {
			if (currentPanel!=editorPanel && currentPanel!=statisticsPanel && (welcomePanel==null || currentPanel!=welcomePanel)) return false; /* Nur wenn Hauptpanel aktiv */
			return commandFileModelLoad(root,file);
		}
		for (String test: new Statistics().getRootNodeNames()) if (name.equalsIgnoreCase(test)) {
			if (currentPanel!=editorPanel && currentPanel!=statisticsPanel && (welcomePanel==null || currentPanel!=welcomePanel)) return false; /* Nur wenn Hauptpanel aktiv */
			return commandFileStatisticsLoad(root,file);
		}

		MsgBox.error(getOwnerWindow(),Language.tr("XML.LoadErrorTitle"),Language.tr("XML.UnknownFileFormat"));
		return false;
	}

	/**
	 * Befehl: Datei - Speichern und Speichern unter
	 * @param saveAs	Wird <code>true</code> übergeben, so wird immer nach einem neuen Dateinamen gefragt, sonst nur, wenn noch keine Name festgelegt ist
	 * @return	Liefert <code>true</code>, wenn das Modell gespeichert wurde
	 */
	private boolean commandFileModelSave(final boolean saveAs) {
		File file=(saveAs)?null:editorPanel.getLastFile();
		final String error=editorPanel.saveModel(file);
		if (error!=null) {
			MsgBox.error(getOwnerWindow(),Language.tr("XML.SaveErrorTitle"),error);
		} else {
			if (editorPanel.getLastFile()!=null) {
				addFileToRecentlyUsedList(editorPanel.getLastFile().toString());
				setAdditionalTitle(editorPanel.getLastFile().getName());
			}
		}
		return error==null;
	}

	/**
	 * Befehl: Datei - Kopie speichern unter
	 * @return	Liefert <code>true</code>, wenn das Modell gespeichert wurde
	 */
	private boolean commandFileModelSaveCopyAs() {
		final String error=editorPanel.saveModelCopy();
		if (error!=null) {
			MsgBox.error(getOwnerWindow(),Language.tr("XML.SaveErrorTitle"),error);
		} else {
			if (editorPanel.getLastFile()!=null) {
				addFileToRecentlyUsedList(editorPanel.getLastFile().toString());
			}
		}
		return error==null;
	}

	/**
	 * Befehl: Datei - Modell exportieren
	 * @return Liefert <code>true</code>, wenn das Modell erfolgreich exportiert werden konnte
	 */
	private boolean commandFileModelExport() {
		String error=editorPanel.exportModelToFile(null,false);
		if (error!=null) MsgBox.error(getOwnerWindow(),Language.tr("XML.ExportErrorTitle"),error);
		return error==null;
	}

	/**
	 * Befehl: Datei - Modell drucken
	 * @return Liefert <code>true</code>, wenn das Modell gedruckt werden konnte
	 */
	private boolean commandFileModelPrint() {
		return ImagePrintable.print(editorPanel.getPrintImage(2000));
	}

	/**
	 * Befehl: Datei - Statistik laden
	 * @param rootOptional	XML-Root-Element (kann <code>null</code> sein)
	 * @param file	Zu ladende Datei; wird <code>null</code> übergeben, so wird ein Dateiauswahldialog angezeigt
	 * @return	Liefert <code>true</code>, wenn eine Datei geladen wurde
	 */
	private boolean commandFileStatisticsLoad(final Element rootOptional, final File file) {
		final String error;
		if (rootOptional!=null) {
			error=statisticsPanel.loadStatisticsFromXML(file,rootOptional);
		} else {
			error=statisticsPanel.loadStatistics(file);
		}
		if (error==null) {
			if (statisticsPanel.getStatistics()!=null) { /* Im Fall eines Nutzer-Ladedialog-Abbruchs gibt es keinen Fehler, aber dennoch keine Statistikdaten. */
				for (AbstractButton button: enabledOnStatisticsAvailable) button.setEnabled(true);
				setCurrentPanel(statisticsPanel);
			}
		} else {
			MsgBox.error(getOwnerWindow(),Language.tr("XML.LoadErrorTitle"),error);
		}
		return error==null;
	}

	/**
	 * Befehl: Datei - Statistik speichern unter
	 * @return	Liefert <code>true</code>, wenn die Statistikdaten gespeichert wurden
	 */
	private boolean commandFileStatisticsSave() {
		final String error=statisticsPanel.saveStatistics(null);
		if (error!=null)
			MsgBox.error(getOwnerWindow(),Language.tr("XML.SaveErrorTitle"),error);
		return error==null;
	}

	/**
	 * Befehl: Datei - Einstellungen
	 * @param showPage	Initial anzuzeigende Seite des Einstellungendialogs (darf <code>null</code> sein)
	 */
	private void commandFileSetup(final SetupDialog.Page showPage) {
		TutorialWindow.closeTutorialWindow();

		BackgroundSystem.getBackgroundSystem(editorPanel).stop();
		new SetupDialog(this,showPage);

		reloadSetup();
	}

	/**
	 * Befehl: Datei - Git-Einstellungen
	 */
	private void commandFileGit() {
		new GitListDialog(this);
	}

	/**
	 * Befehl: Bearbeiten - Element über ID oder Namen suchen
	 */
	private void commandEditFindElement() {
		final FindElementDialog dialog=new FindElementDialog(this,editorPanel.getModel().surface);
		dialog.setVisible(true);
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			final int id=dialog.getSelectedId();
			if (id>0) editorPanel.selectAndScrollToElement(id);
		}
	}

	/**
	 * Befehl: Bearbeiten - Neue Elemente automatisch verbinden
	 * @param connectMode	Art der Auto-Verbindung
	 */
	private void commandEditToggleAutoConnect(final ModelSurfacePanel.ConnectMode connectMode) {
		setup.autoConnect=connectMode;
		setup.saveSetup();
		reloadSetup();
	}

	/**
	 * Befehl: Bearbeiten - Stationen beim Kopieren umbenennen
	 * @param renameOnCopy	Art wie Kopien umbenannt werden sollen
	 */
	private void commandEditToggleRenameOnCopy(final SetupData.RenameOnCopyMode renameOnCopy) {
		setup.renameOnCopy=renameOnCopy;
		setup.saveSetup();
		reloadSetup();
	}

	/**
	 * Befehl: Ansicht - Lineale anzeigen
	 */
	private void commandViewRulers() {
		setup.showRulers=!setup.showRulers;
		setup.saveSetup();
		reloadSetup();
	}

	/**
	 * Befehl: Ansicht - Raster anzeigen
	 * @param raster	Raster-Modus
	 * @see ui.modeleditor.ModelSurface.Grid
	 */
	private void commandViewRaster(final ModelSurface.Grid raster) {
		setup.grid=raster;
		setup.saveSetup();
		reloadSetup();
	}

	/**
	 * Befehl: Ansicht - Statistik auf Zeichenfläche - Statistikinformationen in Tooltips
	 */
	private void commandViewStatisticsInfo() {
		setup.statisticInTooltips=!setup.statisticInTooltips;
		setup.saveSetup();
		reloadSetup();
	}

	/**
	 * Befehl: Ansicht - Statistik auf Zeichenfläche - Heatmap
	 * @param mode	Heatmap-Modus
	 */
	private void commandViewStatisticsHeatMap(final EditorPanelStatistics.HeatMapMode mode) {
		setup.statisticHeatMap=mode;
		setup.saveSetup();
		reloadSetup();
	}

	/**
	 * Befehl: Ansicht - Statistik auf Zeichenfläche - Vorheriger/Nächster Heatmap-Modus
	 * @param direction	Veränderungsrichtung (-1 oder 1)
	 */
	private void commandViewStatisticsHeatMapShift(final int direction) {
		final EditorPanelStatistics.HeatMapMode[] values=EditorPanelStatistics.HeatMapMode.values();

		/* Bisher gewählter Index */
		int index=0;
		if (setup.statisticHeatMap!=null) index=Arrays.asList(values).indexOf(setup.statisticHeatMap);

		/* Neuer Index */
		index+=direction;
		if (index<0) index=values.length-1;
		if (index>=values.length) index=0;

		/* Neuen Modus einstellen */
		setup.statisticHeatMap=values[index];
		setup.saveSetup();
		reloadSetup();

		/* Benachrichtigung anzeigen */
		GlassInfo.info(this,values[index].getName(),650,true);
	}

	/**
	 * Befehl: Ansicht - Statistik auf Zeichenfläche - Heatmap-Modus auswählen
	 */
	private void commandViewStatisticsHeatMapSelect() {
		HeatMapSelectWindow.show(this,()->reloadSetup());
	}

	/**
	 * Befehl: Ansicht - Statistik auf Zeichenfläche - Heatmap konfigurieren
	 */
	private void commandViewStatisticsHeatMapSetup() {
		final HeatMapSetupDialog dialog=new HeatMapSetupDialog(this);
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			setup.saveSetup();
			reloadSetup();
		}
	}

	/**
	 * Befehl: Ansicht - IDs anzeigen
	 */
	private void commandViewIDs() {
		setup.showIDs=!setup.showIDs;
		setup.saveSetup();
		reloadSetup();
	}

	/**
	 * Befehl: Ansicht - Stationsbeschreibungen in Tooltips
	 */
	private void commandViewStationDescriptions() {
		setup.showStationDescription=!setup.showStationDescription;
		setup.saveSetup();
		reloadSetup();
	}

	/**
	 * Befehl: Ansicht - Farbverläufe verwenden
	 */
	private void commandViewGradients() {
		setup.useGradients=!setup.useGradients;
		setup.saveSetup();
		reloadSetup();
	}

	/**
	 * Befehl: Ansicht - Schatten an den Stationen anzeigen
	 */
	private void commandViewShadows() {
		setup.useShadows=!setup.useShadows;
		setup.saveSetup();
		reloadSetup();
	}

	/**
	 * Befehl: Ansicht - Ebenen
	 */
	private void commandLayers() {
		editorPanel.showLayersDialog();
	}

	/**
	 * Befehl: Ansicht - Hintergrundfarbe einstellen
	 */
	private void commandViewBackgroundColor() {
		editorPanel.showBackgroundColorDialog();
	}

	/**
	 * Befehl: Ansicht - Verbindungskanten konfigurieren
	 */
	private void commandViewEdgeSettings() {
		final EditModel model=editorPanel.getModel();

		final EdgeStyleSetupDialog dialog=new EdgeStyleSetupDialog(this,model.edgePainterNormal,model.edgePainterSelected,model.edgeLineMode);
		dialog.setVisible(true);
		if (dialog.getClosedBy()!=BaseDialog.CLOSED_BY_OK) return;
		model.edgeLineMode=dialog.getLineMode();
		final File file=editorPanel.getLastFile();
		editorPanel.setModel(model);
		editorPanel.setLastFile(file);
		editorPanel.setModelChanged(true);
	}

	/**
	 * Befehl: Ansicht - Benutzerdefinierte Animationsicons
	 */
	private void commandViewUserDefinedAnimationIcons() {
		final EditModel model=editorPanel.getModel();
		final AnimationImageDialog dialog=new AnimationImageDialog(this,model,false);
		dialog.setVisible(true);
		if (dialog.getClosedBy()!=BaseDialog.CLOSED_BY_OK) return;

		final File file=editorPanel.getLastFile();
		editorPanel.setModel(model);
		editorPanel.setLastFile(file);
		editorPanel.setModelChanged(true);
	}

	/**
	 * Befehl: Modell - Modell prüfen
	 */
	private void commandModelCheck() {
		EditorPanelRepair.autoFix(editorPanel);

		boolean isError=false;
		String status;

		int[] err=editorPanel.getModel().surface.checkDoubleIDs(false);
		if (err.length>0) {
			isError=true;
			StringBuilder sb=new StringBuilder();
			for (int e: err) {
				if (sb.length()>0) sb.append(", ");
				sb.append(e);
			}
			status="<span style=\"color: red\">"+String.format(Language.tr("Window.Check.ErrorDoubleIDs"),sb.toString())+"</span><br>";
			EditModel model=editorPanel.getModel();
			err=model.surface.checkDoubleIDs(true);
			if (err.length==0) {
				final File file=editorPanel.getLastFile();
				editorPanel.setModel(model);
				editorPanel.setLastFile(file);
				status+="<span style=\"color: green\">"+Language.tr("Window.Check.Fixed")+"</span><br>"+Language.tr("Window.Check.PleaseRerun");
			} else {
				status+="<span style=\"color: red\"><b>"+Language.tr("Window.Check.CannotFix")+"</b></span>";
			}
		} else {
			status=StartAnySimulator.testModel(editorPanel.getModel());
			if (status==null) status="<span style=\"color: green;\">"+Language.tr("Window.Check.Ok")+"</span>"; else status=Language.tr("Window.Check.ErrorList")+"<br><span style=\"color: red\">"+status+"</span>";
		}

		if (isError) {
			MsgBox.error(getOwnerWindow(),Language.tr("Window.Check.Title"),"<html><body>"+status+"</body></html>");
		} else {
			MsgBox.info(getOwnerWindow(),Language.tr("Window.Check.Title"),"<html><body>"+status+"</body></html>");
		}
	}

	/**
	 * Befehl: Modell - Verbindungen zu externen Daten prüfen
	 */
	private void commandModelCheckData() {
		final DataCheckDialog dialog=new DataCheckDialog(this,editorPanel.getModel());
		if (dialog.getNumberOfExternalDataSources()==0) {
			MsgBox.info(getOwnerWindow(),Language.tr("CheckData.Title"),Language.tr("CheckData.NoDataElements.Long"));
			return;
		}
		dialog.setVisible(true);
	}

	/**
	 * Befehl: Modell - Externe Modelldaten - Externe Modelldatenquellen bearbeiten
	 */
	private void commandModelExternalDataEdit() {
		final ModelLoadData loadData=editorPanel.getModelExternalData();
		final ModelLoadDataDialog dialog=new ModelLoadDataDialog(this,loadData,editorPanel.getModel());
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			final ModelLoadData newloadData=dialog.getLoadData();
			if (newloadData!=null && !loadData.equalsModelLoadData(newloadData)) {
				loadData.copyDataFrom(newloadData);
				editorPanel.setModelChanged(true);
				menuModelLoadExternalDataOnStart.setSelected(loadData.isActive());
			}
		}
	}

	/**
	 * Befehl: Modell - Externe Modelldaten - Daten jetzt in Modell laden
	 */
	private void commandModelExternalDataLoadNow() {
		final EditModel model=editorPanel.getModel();
		final File folder=(editorPanel.getLastFile()==null)?null:editorPanel.getLastFile().getParentFile();
		final EditModel newModel=model.modelLoadData.changeModel(model,folder,true);
		final List<String> warnings=model.modelLoadData.getChangeWarnings();
		if (!warnings.isEmpty()) {
			final ModelLoadDataWarningsDialog dialog=new ModelLoadDataWarningsDialog(this,warnings);
			if (dialog.getClosedBy()!=BaseDialog.CLOSED_BY_OK) return;
		}

		final File file=editorPanel.getLastFile();
		editorPanel.setModel(newModel);
		editorPanel.setLastFile(file);
		editorPanel.setModelChanged(true);
	}

	/**
	 * Befehl: Modell - Externe Modelldaten - Modell jeweils vor Simulationsstart aktualisieren
	 */
	private void commandModelExternalDataLoadOnStart() {
		final ModelLoadData loadData=editorPanel.getModelExternalData();
		loadData.setActive(!loadData.isActive());
		editorPanel.setModelChanged(true);
	}

	/**
	 * Befehl: Modell - Plugin-Verzeichnis
	 */
	private void commandModelPluginFolder() {
		final EditModel model=editorPanel.getModel();
		final ExternalConnectDialog dialog=new ExternalConnectDialog(this,model.pluginsFolder);
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			final String newPluginsFolder=dialog.getFolder();
			if (!newPluginsFolder.equals(model.pluginsFolder)) {
				model.pluginsFolder=newPluginsFolder;
				final File file=editorPanel.getLastFile();
				editorPanel.setModel(model);
				editorPanel.setLastFile(file);
				editorPanel.setModelChanged(true);
			}
		}
	}

	/**
	 * Befehl: Modell - Elementeliste
	 */
	private void commandModelListElements() {
		final SelectElementByIdDialog dialog=new SelectElementByIdDialog(this,editorPanel.getModel().surface);
		dialog.setVisible(true);
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			final int id=dialog.getSelectedId();
			if (id>0) editorPanel.selectAndScrollToElement(id);
		}
		editorPanel.updateNavigatorSorting();
	}

	/**
	 * Befehl: Modell - Modellbeschreibung
	 */
	private void commandModelDescription() {
		final ModelDescriptionDialog dialog=new ModelDescriptionDialog(this,editorPanel.getModel());
		dialog.setVisible(true);
	}

	/**
	 * Befehl: Modell - Notizen
	 */
	private void commandModelNotes() {
		final EditModel model=editorPanel.getModel();
		final EditModel modelOriginal=model.clone();

		if (NotesDialog.getNotes(model).size()==0) {
			MsgBox.error(this,Language.tr("NotesDialog.NoNotes.ErrorTitle"),Language.tr("NotesDialog.NoNotes.ErrorInfo"));
			return;
		}
		final NotesDialog dialog=new NotesDialog(this,model);
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			final File file=editorPanel.getLastFile();
			editorPanel.setModel(model);
			editorPanel.setLastFile(file);
			editorPanel.setModelChanged(!model.equalsEditModel(modelOriginal));
			return;
		}
	}

	/**
	 * Befehl: Modell - Vergleich mit analytischem Modell
	 */
	private void commandModelAnalyticCompare() {
		final EditModel model=editorPanel.getModel();
		final AnalyticInfo analyticInfo=new AnalyticInfo();
		if (!analyticInfo.build(model)) {
			MsgBox.error(this,Language.tr("AnalyticModelCompare.Error.Title"),Language.tr("AnalyticModelCompare.Error.Info"));
			return;
		}
		AnalyticInfo.SimulationResults simulationResults=null;
		final Statistics statistics=statisticsPanel.getStatistics();
		if (statistics!=null && model.equalsEditModel(statistics.editModel,true)) {
			simulationResults=new AnalyticInfo.SimulationResults(statistics);
		}

		final ModelAnalyticInfoDialog dialog=new ModelAnalyticInfoDialog(this,analyticInfo,simulationResults);
		dialog.setVisible(true);
	}

	/**
	 * Befehl: Modell - Aktuelles Modell für späteren Vergleich festhalten
	 */
	private void commandModelCompareTwoInit() {
		EditModel model=editorPanel.getModel();
		Object obj=RunModel.getRunModel(model,true,setup.useMultiCoreSimulation);
		if (obj instanceof String) {
			MsgBox.error(getOwnerWindow(),Language.tr("Compare.Error.ModelError.Title"),Language.tr("Compare.Error.ModelError.CannotCompare"));
			return;
		}

		if (pinnedModel!=null) {
			if (!MsgBox.confirm(getOwnerWindow(),Language.tr("Compare.ReplaceKeptModel.Title"),Language.tr("Compare.ReplaceKeptModel.Info"),Language.tr("Compare.ReplaceKeptModel.YesInfo"),Language.tr("Compare.ReplaceKeptModel.NoInfo"))) return;
		}

		pinnedModel=model;
		MsgBox.info(getOwnerWindow(),Language.tr("Compare.Kept.Title"),Language.tr("Compare.Kept.Info"));

		menuModelCompareKept.setEnabled(true);
		menuModelCompareReturn.setEnabled(true);
	}

	/**
	 * Befehl Modell - Aktuelles und festgehaltenes Modell vergleichen
	 * @param level	0: festgehaltenes Modell simulieren; 1: aktuelles Modell simulieren; 2: Ergebnisse anzeigen
	 */
	private void commandModelCompareTwoRun(final int level) {
		if (level==0) {
			if (pinnedModel==null) {
				MsgBox.error(getOwnerWindow(),Language.tr("Compare.Error.NoModelKept.Title"),Language.tr("Compare.Error.NoModelKept.Info"));
				return;
			}

			EditModel model=editorPanel.getModel();
			Object obj=RunModel.getRunModel(model,true,setup.useMultiCoreSimulation);
			if (obj instanceof String) {
				MsgBox.error(getOwnerWindow(),Language.tr("Compare.Error.ModelError.Title"),Language.tr("Compare.Error.ModelError.CannotKeep"));
				return;
			}

			if (pinnedModel.equalsEditModel(model)) {
				MsgBox.error(getOwnerWindow(),Language.tr("Compare.Error.IdenticalModels.Title"),Language.tr("Compare.Error.IdenticalModels.Info"));
				return;
			}

			commandSimulationSimulation(pinnedModel,null,null,Simulator.logTypeFull,()->{compareStatistics[0]=statisticsPanel.getStatistics(); commandModelCompareTwoRun(1);});
			return;
		}

		if (level==1) {
			commandSimulationSimulation(null,null,null,Simulator.logTypeFull,()->{compareStatistics[1]=statisticsPanel.getStatistics(); commandModelCompareTwoRun(2);});
			return;
		}

		if (level==2) {
			enableMenuBar(false);
			setCurrentPanel(new ComparePanel(getOwnerWindow(),compareStatistics,new String[] {Language.tr("Compare.Models.Base"),Language.tr("Compare.Models.Changed")},true,()->{
				if (currentPanel instanceof ComparePanel) {
					ComparePanel comparePanel=(ComparePanel) currentPanel;
					EditModel model=comparePanel.getModelForEditor();
					if (model!=null) {
						if (!isDiscardModelOk()) return;
						editorPanel.setModel(model);
					}
				}
				setCurrentPanel(editorPanel);
				enableMenuBar(true);
			}));
			return;
		}
	}

	/**
	 * Befehl: Modell - Zu festgehaltenem Modell zurückkehren
	 */
	private void commandModelCompareReturn() {
		if (pinnedModel==null) {
			MsgBox.error(getOwnerWindow(),Language.tr("Compare.Error.NoModelKept.Title"),Language.tr("Compare.Error.NoModelKept.Info2"));
			return;
		}

		EditModel model=editorPanel.getModel();

		if (pinnedModel.equalsEditModel(model)) {
			MsgBox.error(getOwnerWindow(),Language.tr("Compare.Error.IdenticalModels.Title"),Language.tr("Compare.Error.IdenticalModels.Info"));
			return;
		}

		if (editorPanel.isModelChanged()) {
			if (!MsgBox.confirm(getOwnerWindow(),Language.tr("Compare.ReturnConfirm.Title"),Language.tr("Compare.ReturnConfirm.Info"),Language.tr("Compare.ReturnConfirm.InfoYes"),Language.tr("Compare.ReturnConfirm.InfoNo"))) return;
		}

		editorPanel.setModel(pinnedModel);
	}

	/**
	 * Befehl: Modell - Vorlagen
	 */
	private void commandModelTemplates() {
		final EditModel model=editorPanel.getModel();
		final TemplatesListDialog dialog=new TemplatesListDialog(this,model.getTemplates());
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			editorPanel.setModel(model);
			final ByteArrayInputStream templateData=dialog.getUseTemplate();
			if (templateData!=null) {
				editorPanel.pasteDirect(templateData);
			}
		}
	}

	/**
	 * Befehl: Modell - Vorlage hinzufügen
	 */
	private void commandModelAddTemplate() {
		final ModelSurface originalSurface=editorPanel.getOriginalSurface();
		final EditModel model=editorPanel.getModel();
		final EditTemplateDialog dialog=new EditTemplateDialog(this,originalSurface,model.getTemplates());
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			editorPanel.setModel(model);
		}
	}

	/**
	 * Versucht eine Animation im Pausemodus zu starten.<br>
	 * Es werden dabei keine Meldungsdialoge angegeben, sondern eventuelle Fehlermeldungen als Rückgabewerte geliefert.
	 * @return	Im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung.
	 */
	public String startRemoteControlledAnimation() {
		return commandSimulationAnimation(null,true,null,null,Simulator.logTypeFull);
	}

	/**
	 * Führt, wenn entsprechend konfiguriert, eine automatische Speicherung des Modells durch.
	 */
	private void checkAutoSave() {
		if(setup.autoSaveMode==SetupData.AutoSaveMode.AUTOSAVE_OFF) return;

		if (!editorPanel.isModelChanged()) return;
		commandFileModelSave(false);
	}

	/**
	 * Versucht ein {@link StartAnySimulator}-Objekt basierend auf einem Editor-Modell zu erstellen.
	 * @param editModel	Ausgangs-Editor-Modell
	 * @param logging	Wird hier ein Wert ungleich <code>null</code> übergeben, so wird der Lauf durch den angegebenen Logger aufgezeichnet; ansonsten erfolgt nur die normale Aufzeichnung in der Statistik
	 * @param loggingIDs	Liste der Stations-IDs deren Ereignisse beim Logging erfasst werden sollen (nur von Bedeutung, wenn das Logging als solches aktiv ist; kann <code>null</code> sein, dann werden die Ereignisse aller Stationen erfasst)
	 * @param logType	Welche Arten von Ereignissen sollen erfasst werden? (<code>null</code> bedeutet: alles erfassen)
	 * @return	Liefert im Erfolgsfall den noch nicht gestarteten Simulator, sonst <code>null</code>
	 */
	private StartAnySimulator getSimulator(EditModel editModel, final SimLogging logging, final int[] loggingIDs, final Set<Simulator.LogType> logType) {
		final File folder=(editorPanel.getLastFile()==null)?null:editorPanel.getLastFile().getParentFile();
		final EditModel changedEditModel=editModel.modelLoadData.changeModel(editModel,folder);
		if (changedEditModel!=null) {
			final List<String> warnings=editModel.modelLoadData.getChangeWarnings();
			if (!warnings.isEmpty()) {
				final ModelLoadDataWarningsDialog dialog=new ModelLoadDataWarningsDialog(this,warnings);
				if (dialog.getClosedBy()!=BaseDialog.CLOSED_BY_OK) return null;
			}
			editModel=changedEditModel;
		}

		final StartAnySimulator starter=new StartAnySimulator(editModel,logging,loggingIDs,logType);
		final String error=WaitDialog.workString(this,()->starter.prepare(),WaitDialog.Mode.MODEL_PREPARE);
		if (error!=null) {
			MsgBox.error(getOwnerWindow(),Language.tr("Window.Simulation.ModelIsFaulty"),"<html>"+Language.tr("Window.Simulation.ErrorInitializatingSimulation")+":<br>"+error+"</html>");
			return null;
		}
		return starter;
	}

	/**
	 * Befehl: Simulation - Animation starten
	 * @param recordFile	Datei für Videoaufzeichnung (kann <code>null</code> sein)
	 * @param externalConnect Wird die Animation im Pausemodus gestartet, so wird direkt der erste Schritt ausgeführt. Über diese Funktion kann angegeben werden, dass dieser Schritt im vollständigen Erfassungsmodus durchgeführt werden soll.
	 * @param logging	Wird hier ein Wert ungleich <code>null</code> übergeben, so wird der Lauf durch den angegebenen Logger aufgezeichnet; ansonsten erfolgt nur die normale Aufzeichnung in der Statistik
	 * @param loggingIDs	Liste der Stations-IDs deren Ereignisse beim Logging erfasst werden sollen (nur von Bedeutung, wenn das Logging als solches aktiv ist; kann <code>null</code> sein, dann werden die Ereignisse aller Stationen erfasst)
	 * @param logType	Welche Arten von Ereignissen sollen erfasst werden? (<code>null</code> bedeutet: alles erfassen)
	 * @return	Liefert im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung
	 */
	private String commandSimulationAnimation(final File recordFile, final boolean externalConnect, final SimLogging logging, final int[] loggingIDs, final Set<Simulator.LogType> logType) {
		EditorPanelRepair.autoFix(editorPanel);

		EditModel editModel=editorPanel.getModel().clone();

		if (editModel.repeatCount>1) {
			if (!externalConnect) MsgBox.info(getOwnerWindow(),Language.tr("Animation.NoRepeat.Title"),String.format(Language.tr("Animation.NoRepeat.Info"),editModel.repeatCount));
			editModel.repeatCount=1;
		}

		checkAutoSave();

		final File folder=(editorPanel.getLastFile()==null)?null:editorPanel.getLastFile().getParentFile();
		final EditModel changedEditModel=editModel.modelLoadData.changeModel(editModel,folder);
		if (changedEditModel!=null) {
			final List<String> warnings=editModel.modelLoadData.getChangeWarnings();
			if (!warnings.isEmpty()) {
				final ModelLoadDataWarningsDialog dialog=new ModelLoadDataWarningsDialog(this,warnings);
				if (dialog.getClosedBy()!=BaseDialog.CLOSED_BY_OK) return null;
			}
			editModel=changedEditModel;
		}

		final boolean fastWarmUp=animationPanel.makeAnimationModel(editModel);
		final CallbackLoggerWithJS logger=new CallbackLoggerWithJS();
		if (logging!=null) logger.setNextLogger(logging);
		final Simulator simulator=new Simulator(editModel,logger,loggingIDs,logType);

		String error=WaitDialog.workString(this,()->simulator.prepare(),WaitDialog.Mode.MODEL_PREPARE);
		if (error!=null) {
			if (!externalConnect) MsgBox.error(getOwnerWindow(),Language.tr("Window.Simulation.ModelIsFaulty"),"<html>"+Language.tr("Window.Simulation.ErrorInitializatingSimulation")+":<br>"+error+"</html>");
			return error;
		}

		BackgroundSystem.getBackgroundSystem(editorPanel).stop();

		enableMenuBar(false);

		animationPanel.setSimulator(
				editModel,
				simulator,
				logger,
				recordFile,
				setup.animationFrameScale,
				setup.paintTimeStamp,
				fastWarmUp,
				editorPanel.getZoom(),
				editorPanel.getRaster(),
				editorPanel.getTopPosition(),
				()->{
					editorPanel.setZoom(animationPanel.getZoom());
					editorPanel.setTopPosition(animationPanel.getTopPosition());
					if (animationPanel.isSimulationSuccessful()) {
						statisticsPanel.setStatistics(simulator.getStatistic());
						for (AbstractButton button: enabledOnStatisticsAvailable) button.setEnabled(true);
						setCurrentPanel(statisticsPanel);
					} else {
						setCurrentPanel(editorPanel);
					}
					enableMenuBar(true);
				},
				()->{
					editorPanel.setZoom(animationPanel.getZoom());
					enableMenuBar(false);
					waitPanel.setSimulator(simulator,()->{
						if (waitPanel.isSimulationSuccessful()) {
							statisticsPanel.setStatistics(simulator.getStatistic());
							for (AbstractButton button: enabledOnStatisticsAvailable) button.setEnabled(true);
							setCurrentPanel(statisticsPanel);
						} else {
							setCurrentPanel(editorPanel);
						}
						enableMenuBar(true);
					});
					setCurrentPanel(waitPanel);
				},
				setup.animationStartPaused || externalConnect,
				externalConnect
				);
		setCurrentPanel(animationPanel);
		return null;
	}

	/**
	 * Befehl: Simulation - Animation als Video aufzeichnen
	 */
	private void commandSimulationAnimationRecord() {
		final AnimationRecordSetupDialog dialog=new AnimationRecordSetupDialog(this);
		if (dialog.getClosedBy()!=BaseDialog.CLOSED_BY_OK) return;

		final File file=dialog.getVideoFile();

		commandSimulationAnimation(file,false,null,null,Simulator.logTypeFull);
	}

	/**
	 * Befehl: Simulation - Animation in Logdatei aufzeichnen
	 */
	private void commandSimulationAnimationLog() {
		final EditModel editModel=editorPanel.getModel();
		final StartAnySimulator starter=getSimulator(editModel,null,null,Simulator.logTypeFull);
		if (starter==null) return;

		if (editModel.clientCount>1000 && editModel.useClientCount) {
			if (!MsgBox.confirm(getOwnerWindow(),Language.tr("LogSimulation.Title"),Language.tr("LogSimulation.LargeModelWarning"),Language.tr("LogSimulation.LargeModelWarning.YesInfo"),Language.tr("LogSimulation.LargeModelWarning.NoInfo"))) return;
		}

		final LogSetupDialog dialog=new LogSetupDialog(this);
		dialog.setVisible(true);
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			final SimLogging logger=dialog.getLogger();
			final int[] loggingIDs=dialog.getStationIDs();
			final Set<Simulator.LogType> logType=dialog.getLogType();
			if (logger!=null) commandSimulationAnimation(null,false,logger,loggingIDs,logType);
		}
	}

	/**
	 * Befehl: Simulation - Animationskonfiguration - Animationsstart
	 * @param paused	Animation im Pausemodus starten ?
	 */
	private void commandSimulationAnimationStartMode(final boolean paused) {
		if (setup.animationStartPaused==paused) return;
		setup.animationStartPaused=paused;
		setup.saveSetup();
		reloadSetup();
	}

	/**
	 * Befehl: Simulation - Animationskonfiguration - Analoge Werte in Animation
	 * @param useSlowModeAnimation	Animation langsam und dafür in Bezug auf analoge Werte schöner darstellen
	 */
	private void commandSimulationAnalogValuesSlow(final boolean useSlowModeAnimation) {
		if (setup.useSlowModeAnimation==useSlowModeAnimation) return;
		setup.useSlowModeAnimation=useSlowModeAnimation;
		setup.saveSetup();
		reloadSetup();
	}

	/**
	 * Befehl: Simulation - Animationskonfiguration - Verzeichnis zum Speichern von Bildern - Im Nutzerverzeichnis speichern
	 */
	private void commandSimulationAnimationScreenshotModeHome() {
		setup.imagePathAnimation="";
		setup.saveSetup();
		reloadSetup();
	}

	/**
	 * Befehl: Simulation - Animationskonfiguration - Verzeichnis zum Speichern von Bildern - In ausgewähltem Verzeichnis speichern
	 */
	private void commandSimulationAnimationScreenshotModeCustom() {
		final JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		if (setup.imagePathAnimation!=null && !setup.imagePathAnimation.trim().isEmpty() && new File(setup.imagePathAnimation).isDirectory()) {
			fc.setCurrentDirectory(new File(setup.imagePathAnimation));
		}
		fc.setDialogTitle(Language.tr("Batch.Output.Folder.Button.Hint"));
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (fc.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION) return;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		final File file=fc.getSelectedFile();
		setup.imagePathAnimation=file.toString();
		setup.saveSetup();
		reloadSetup();
	}

	/**
	 * Befehl: Simulation - Simulation starten
	 * @param simModel	Ausgangs-Editor-Modell
	 * @param logging	Wird hier ein Wert ungleich <code>null</code> übergeben, so wird der Lauf durch den angegebenen Logger aufgezeichnet; ansonsten erfolgt nur die normale Aufzeichnung in der Statistik
	 * @param loggingIDs	Liste der Stations-IDs deren Ereignisse beim Logging erfasst werden sollen (nur von Bedeutung, wenn das Logging als solches aktiv ist; kann <code>null</code> sein, dann werden die Ereignisse aller Stationen erfasst)
	 * @param logType	Welche Arten von Ereignissen sollen erfasst werden? (<code>null</code> bedeutet: alles erfassen)
	 * @param whenDone	Callback, das nach Abschluss der Simulation aufgerufen wird
	 */
	private void commandSimulationSimulation(final EditModel simModel, final SimLogging logging, final int[] loggingIDs, final Set<Simulator.LogType> logType, final Runnable whenDone) {
		if (simModel==null) EditorPanelRepair.autoFix(editorPanel);

		EditModel editModel=(simModel==null)?editorPanel.getModel():simModel;

		checkAutoSave();

		final File folder=(editorPanel.getLastFile()==null)?null:editorPanel.getLastFile().getParentFile();
		final EditModel changedEditModel=editModel.modelLoadData.changeModel(editModel,folder);
		if (changedEditModel!=null) {
			final List<String> warnings=editModel.modelLoadData.getChangeWarnings();
			if (!warnings.isEmpty()) {
				final ModelLoadDataWarningsDialog dialog=new ModelLoadDataWarningsDialog(this,warnings);
				if (dialog.getClosedBy()!=BaseDialog.CLOSED_BY_OK) return;
			}
			editModel=changedEditModel;
		}

		final EditModel editModelFinal=editModel;
		final Object obj=WaitDialog.workObject(this,()->BackgroundSystem.getBackgroundSystem(editorPanel).getStartedSimulator(editModelFinal,logging,loggingIDs,logType),WaitDialog.Mode.MODEL_PREPARE);
		if (obj instanceof String) {
			MsgBox.error(getOwnerWindow(),Language.tr("Window.Simulation.ModelIsFaulty"),"<html>"+Language.tr("Window.Simulation.ErrorInitializatingSimulation")+":<br>"+((String)obj)+"</html>");
			return;
		}
		final AnySimulator simulator=(AnySimulator)obj;

		enableMenuBar(false);

		final long simulationStartTime=System.currentTimeMillis();
		waitPanel.setSimulator(simulator,()->{
			if (waitPanel.isSimulationSuccessful()) {
				statisticsPanel.setStatistics(simulator.getStatistic());
				for (AbstractButton button: enabledOnStatisticsAvailable) button.setEnabled(true);
				setCurrentPanel(statisticsPanel);
				Notifier.run(Notifier.Message.SIMULATION_DONE,simulationStartTime);
			} else {
				setCurrentPanel(editorPanel);
			}
			enableMenuBar(true);
			if (whenDone!=null) whenDone.run();
		});
		/* WaitPanel nur anzeigen, wenn die (Hintergrund-)Simulation nicht schon fertig ist. */
		if (simulator.isRunning()) setCurrentPanel(waitPanel);
	}

	/**
	 * Befehl: Simulation - Simulation in Logdatei aufzeichnen
	 */
	private void commandSimulationSimulationLog() {
		final EditModel editModel=editorPanel.getModel();
		final StartAnySimulator starter=getSimulator(editModel,null,null,Simulator.logTypeFull);
		if (starter==null) return;

		if (editModel.clientCount>1000 && editModel.useClientCount) {
			if (!MsgBox.confirm(getOwnerWindow(),Language.tr("LogSimulation.Title"),Language.tr("LogSimulation.LargeModelWarning"),Language.tr("LogSimulation.LargeModelWarning.YesInfo"),Language.tr("LogSimulation.LargeModelWarning.NoInfo"))) return;
		}

		final LogSetupDialog dialog=new LogSetupDialog(this);
		dialog.setVisible(true);
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			final SimLogging logger=dialog.getLogger();
			final int[] loggingIDs=dialog.getStationIDs();
			final Set<Simulator.LogType> logType=dialog.getLogType();
			if (logger!=null) commandSimulationSimulation(null,logger,loggingIDs,logType,null);
		}
	}

	/**
	 * Befehl: Simulation - Statistikaufzeichnung konfigurieren
	 */
	private void commandSimulationStatisticsSetup() {
		final EditModel modelOrig=editorPanel.getModel();
		final EditModel model=modelOrig.clone();
		final StationStatisticsDialog dialog=new StationStatisticsDialog(this,model);
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			if (!modelOrig.equalsEditModel(model)) {
				editorPanel.setModel(model);
				editorPanel.setModelChanged(true);
			}
		}
	}

	/**
	 * Befehl: Simulation - Verbindung zum Simulationsserver prüfen
	 */
	private void commandSimulationCheckServerConnection() {
		final String[] parts=setup.serverData.split(":");
		if (parts.length<2 || parts.length>3) {
			MsgBox.error(getOwnerWindow(),Language.tr("Server.Status.Title"),Language.tr("Server.Status.InvalidConfiguration"));
			return;
		}
		final Long L=NumberTools.getPositiveLong(parts[1]);
		if (L==null) {
			MsgBox.error(getOwnerWindow(),Language.tr("Server.Status.Title"),Language.tr("Server.Status.InvalidConfiguration"));
			return;
		}

		if (parts.length==2 || parts[2].trim().isEmpty()) {
			new ServerStatus(parts[0],L.intValue(),true,null).showMessage(getOwnerWindow());
		} else {
			new ServerStatus(parts[0],L.intValue(),true,parts[2]).showMessage(getOwnerWindow());
		}
	}

	/**
	 * Befehl: Simulation - Einschwingphase bestimmen
	 */
	private void commandSimulationFindWarmUpSize() {
		final EditModel editModel=editorPanel.getModel();
		editModel.warmUpTime=0.0; /* Keine Warm-Up-Phase sind der Simulation; verschiedene Warp-Up-Bereiche werden später in der Rechnung angenommen. */
		editModel.collectWaitingTimes=true;

		BackgroundSystem.getBackgroundSystem(editorPanel).stop(); /* Das Modell wird in der vorherigen Zeile verändert, kann daher ganz sicher nicht per Background gestartet werden. */

		final StartAnySimulator starter=getSimulator(editModel,null,null,Simulator.logTypeFull);
		if (starter==null) return;

		final AnySimulator simulator=starter.start();
		enableMenuBar(false);

		waitPanel.setSimulator(simulator,()->{
			if (waitPanel.isSimulationSuccessful()) {
				final Statistics statistics=simulator.getStatistic();
				if (statistics!=null) {
					final TestWarmUp test=new TestWarmUp(statistics);
					final double d=test.test(true);
					if (d>=1) {
						MsgBox.error(getOwnerWindow(),Language.tr("FindWarmUp.Title"),Language.tr("FindWarmUp.Error"));
					} else {
						final String sNew=NumberTools.formatPercent(d,3);
						final String sOld=NumberTools.formatPercent(editorPanel.getModel().warmUpTime,3);
						if (MsgBox.confirm(getOwnerWindow(),Language.tr("FindWarmUp.Title"),String.format(Language.tr("FindWarmUp.Info"),sNew),String.format(Language.tr("FindWarmUp.Info.Yes"),sNew),String.format(Language.tr("FindWarmUp.Info.No"),sOld))) {
							final EditModel model=editorPanel.getModel();
							if (model.warmUpTime!=d) {
								model.warmUpTime=d;
								final File file=editorPanel.getLastFile();
								editorPanel.setModel(model);
								editorPanel.setModelChanged(true);
								editorPanel.setLastFile(file);
							}
						}
					}
				}
			}
			setCurrentPanel(editorPanel);
			enableMenuBar(true);
		});
		setCurrentPanel(waitPanel);
	}

	/*
	private void commandFindWarmUpSizeSimulation2() {
		final EditModel editModel=editorPanel.getModel();
		editModel.warmUpTime=0.0;
		editModel.collectWaitingTimes=true;

		final Simulator simulator=getSimulator(editModel,null);
		if (simulator==null) return;

		simulator.start();
		enableMenuBar(false);

		waitPanel.setSimulator(simulator,()->{
			if (waitPanel.isSimulationSuccessful()) {
				final TestWarmUp test=new TestWarmUp(simulator.getStatistic());
				System.out.println(test.test(false)+"\t"+test.test(true));
				SwingUtilities.invokeLater(()->commandFindWarmUpSizeSimulation2());
			}
			setCurrentPanel(editorPanel);
			enableMenuBar(true);
		});
		setCurrentPanel(waitPanel);
	}
	 */

	/**
	 * Befehl: Simulation - Statistik-Batch-Größe bestimmen
	 */
	private void commandSimulationFindBatchSize() {
		final EditModel editModel=editorPanel.getModel();
		editModel.correlationMode=Statistics.CorrelationMode.CORRELATION_MODE_FULL;
		editModel.correlationRange=10_000;

		BackgroundSystem.getBackgroundSystem(editorPanel).stop(); /* Das Modell wird in der vorherigen Zeile verändert, kann daher ganz sicher nicht per Background gestartet werden. */

		final StartAnySimulator starter=getSimulator(editModel,null,null,Simulator.logTypeFull);
		if (starter==null) return;

		final AnySimulator simulator=starter.start();
		enableMenuBar(false);

		MsgBox.info(getOwnerWindow(),Language.tr("FindBatchSizeSimulation.Title"),Language.tr("FindBatchSizeSimulation.Info"));

		waitPanel.setSimulator(simulator,()->{
			boolean runNow=false;
			if (waitPanel.isSimulationSuccessful()) {
				final Statistics statistics=simulator.getStatistic();
				if (statistics!=null) {
					final FindBatchSizeDialog dialog=new FindBatchSizeDialog(MainPanel.this,statistics);
					dialog.setVisible(true);
					if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
						int b=dialog.getBatchSize();
						if (b>1) {
							final EditModel model=editorPanel.getModel();
							if (model.batchMeansSize!=b) {
								model.batchMeansSize=b;
								final File file=editorPanel.getLastFile();
								editorPanel.setModel(model);
								editorPanel.setModelChanged(true);
								editorPanel.setLastFile(file);
							}
							runNow=dialog.runSimulationNow();
						}
					}
				}
			}
			setCurrentPanel(editorPanel);
			enableMenuBar(true);
			if (runNow) commandSimulationSimulation(null,null,null,Simulator.logTypeFull,null);
		});
		setCurrentPanel(waitPanel);
	}

	/**
	 * Erstellt für die verschiedenen möglichen Fälle ein Parameterreihen-Panel
	 * @param editModel	Ausgangs-Editor-Modell
	 * @param miniStatistics	Vorab-berechnete Mini-Statisitik (kann <code>null</code> sein)
	 * @param template	Parameterreihen-Vorlage	(kann <code>null</code> sein)
	 * @return	Neues Parameterreihen-Panel
	 * @see #commandSimulationParameterSeriesNew(ui.parameterseries.ParameterCompareTemplatesDialog.TemplateRecord)
	 * @see #commandSimulationParameterSeriesLoad(File, InputStream)
	 */
	private ParameterComparePanel getParameterComparePanel(final EditModel editModel, final Statistics miniStatistics, final ParameterCompareTemplatesDialog.TemplateRecord template) {
		return new ParameterComparePanel(getOwnerWindow(),editModel,miniStatistics,()->{
			if (currentPanel instanceof ParameterComparePanel) {
				final ParameterComparePanel parameterCompare=(ParameterComparePanel)currentPanel;
				final Statistics statistics=parameterCompare.getStatisticsForEditor();
				if (statistics!=null && isDiscardModelOk()) {
					editorPanel.setModel(statistics.editModel);
					statisticsPanel.setStatistics(statistics);
				}
				final EditModel model=parameterCompare.getModelForEditor();
				if (model!=null && isDiscardModelOk()) {
					editorPanel.setModel(model);
					statisticsPanel.setStatistics(null);
				}
				final List<Statistics> stat=parameterCompare.getCompareModels();
				if (stat!=null && !stat.isEmpty()) {
					if (isDiscardModelOk()) {
						commandExtrasCompare(stat.toArray(new Statistics[0]));
						return;
					}
				}
			}
			setCurrentPanel(editorPanel);
			enableMenuBar(true);
			statisticsPanel.updateChartSetupInViewers(SetupData.getSetup().chartSetup);
		},template);
	}

	/**
	 * Erstellt ein Parameterreihen-Panel für die Varianzanalyse
	 * @param editModel	Ausgangs-Editor-Modell
	 * @param miniStatistics	Vorab-berechnete Mini-Statisitik (kann <code>null</code> sein)
	 * @param repeatCount	Anzahl an Wiederholungen
	 * @return	Neues Parameterreihen-Panel für die Varianzanalyse
	 * @see #commandSimulationParameterSeriesVariance()
	 */
	private ParameterComparePanel getParameterComparePanelVariance(final EditModel editModel, final Statistics miniStatistics, final int repeatCount) {
		final ParameterComparePanel panel=new ParameterComparePanel(getOwnerWindow(),editModel,miniStatistics,()->{
			if (currentPanel instanceof ParameterComparePanel) {
				final ParameterComparePanel parameterCompare=(ParameterComparePanel)currentPanel;
				final Statistics statistics=parameterCompare.getStatisticsForEditor();
				if (statistics!=null && isDiscardModelOk()) {
					editorPanel.setModel(statistics.editModel);
					statisticsPanel.setStatistics(statistics);
				}
				final EditModel model=parameterCompare.getModelForEditor();
				if (model!=null && isDiscardModelOk()) {
					editorPanel.setModel(model);
					statisticsPanel.setStatistics(null);
				}
				final List<Statistics> stat=parameterCompare.getCompareModels();
				if (stat!=null && !stat.isEmpty()) {
					if (isDiscardModelOk()) {
						commandExtrasCompare(stat.toArray(new Statistics[0]));
						return;
					}
				}
			}
			setCurrentPanel(editorPanel);
			enableMenuBar(true);
			statisticsPanel.updateChartSetupInViewers(SetupData.getSetup().chartSetup);
		},null);
		panel.setupVarianceAnalysis(repeatCount);
		return panel;
	}

	/**
	 * Befehl: Simulation - Parameterreihe simulieren - Parameterreihe für das aktuelle Modell erstellen
	 * @param template	Optionale Vorlage für die neue Parameterreihe
	 * @return	Liefert <code>true</code>, wenn das Modell in Ordnung ist und die Parameterreihenfunktion aktiviert werden konnte
	 */
	private boolean commandSimulationParameterSeriesNew(final ParameterCompareTemplatesDialog.TemplateRecord template) {
		final EditModel editModel=editorPanel.getModel();

		if (editModel.modelLoadData.willChangeModel()) {
			MsgBox.warning(this,Language.tr("ModelLoadData.IncompatibleWarning.Title"),Language.tr("ModelLoadData.IncompatibleWarning.ParameterSeries"));
		}

		final Statistics miniStatistics=ParameterComparePanel.generateMiniStatistics(this,editModel,statisticsPanel.getStatistics());
		if (miniStatistics==null) return false;

		final ParameterComparePanel parameterComparePanel=getParameterComparePanel(editModel,miniStatistics,template);

		checkAutoSave();
		BackgroundSystem.getBackgroundSystem(editorPanel).stop();

		enableMenuBar(false);
		setCurrentPanel(parameterComparePanel);

		return true;
	}

	/**
	 * Befehl: Simulation - Parameterreihe simulieren - Parameterreihendaten laden
	 * @param file	Dateiname der zu ladenden Parameterreihe (wird <code>null</code> angegbeben, so wird ein Dateiauswahldialog angezeigt)
	 * @param stream	Wird ein Wert ungleich <code>null</code> angegeben, so wird die angegebene Datei ignoriert und die Daten werden aus dem Stream geladen
	 * @return	Liefert <code>true</code>, wenn die Parameterreihenfunktion aktiviert werden konnte
	 */
	private boolean commandSimulationParameterSeriesLoad(File file, final InputStream stream) {
		if (stream==null) {
			if (file==null) file=XMLTools.showLoadDialog(getParent(),Language.tr("ParameterCompare.Settings.Load"));
			if (file==null) return false;
		}

		final ParameterComparePanel parameterComparePanel=getParameterComparePanel(editorPanel.getModel(),null,null);
		if (stream==null) {
			if (!parameterComparePanel.loadSetup(this,file,false)) return false;
		} else {
			if (!parameterComparePanel.loadSetup(this,stream,false)) return false;
		}

		checkAutoSave();
		BackgroundSystem.getBackgroundSystem(editorPanel).stop();

		enableMenuBar(false);
		setCurrentPanel(parameterComparePanel);

		return true;
	}

	/**
	 * Befehl: Simulation - Parameterreihe simulieren - Varianzanalyse für das aktuelle Modell erstellen
	 * @return	Liefert <code>true</code>, wenn das Modell in Ordnung ist und die Parameterreihenfunktion aktiviert werden konnte
	 */
	private boolean commandSimulationParameterSeriesVariance() {
		final EditModel editModel=editorPanel.getModel();

		final Statistics miniStatistics=ParameterComparePanel.generateMiniStatistics(this,editModel,statisticsPanel.getStatistics());
		if (miniStatistics==null) return false;

		final VarianceAnalysisDialog dialog=new VarianceAnalysisDialog(this);
		if (dialog.getClosedBy()!=BaseDialog.CLOSED_BY_OK) return false;

		final ParameterComparePanel parameterComparePanel=getParameterComparePanelVariance(editModel,miniStatistics,dialog.getRepeatCount());

		checkAutoSave();
		BackgroundSystem.getBackgroundSystem(editorPanel).stop();

		enableMenuBar(false);
		setCurrentPanel(parameterComparePanel);

		return true;
	}

	/**
	 * Befehl: Simulation - Skript ausführen
	 */
	private void commandSimulationScriptRunner() {
		final EditModel editModel=editorPanel.getModel();

		final OptimizerPanelPrepareDialog dialog=new OptimizerPanelPrepareDialog(this,editModel,statisticsPanel.getStatistics(),OptimizerPanelPrepareDialog.Mode.MODE_JAVASCRIPT);
		final Statistics miniStatistics=dialog.getMiniStatistics();
		if (miniStatistics==null) {
			MsgBox.error(getOwnerWindow(),Language.tr("JSRunner.PreparationFailed"),dialog.getError());
			return;
		}

		checkAutoSave();

		BackgroundSystem.getBackgroundSystem(editorPanel).stop();

		enableMenuBar(false);
		setCurrentPanel(new JSModelRunnerPanel(getOwnerWindow(),editModel,miniStatistics,()->{
			setCurrentPanel(editorPanel);
			enableMenuBar(true);
		},true));
	}

	/**
	 * Befehl: Simulation - Optimierer
	 * @return	Liefert <code>true</code>, wenn das Modell in Ordnung ist und der Optimierer aktiviert werden konnte
	 */
	private boolean commandSimulationOptimizer() {
		final EditModel editModel=editorPanel.getModel();

		if (editModel.modelLoadData.willChangeModel()) {
			MsgBox.warning(this,Language.tr("ModelLoadData.IncompatibleWarning.Title"),Language.tr("ModelLoadData.IncompatibleWarning.Optimization"));
		}

		final OptimizerPanelPrepareDialog dialog=new OptimizerPanelPrepareDialog(this,editModel,statisticsPanel.getStatistics(),OptimizerPanelPrepareDialog.Mode.MODE_OPTIMIZATION);
		final Statistics miniStatistics=dialog.getMiniStatistics();
		if (miniStatistics==null) {
			MsgBox.error(getOwnerWindow(),Language.tr("Optimizer.PreparationFailed"),dialog.getError());
			return false;
		}

		checkAutoSave();

		BackgroundSystem.getBackgroundSystem(editorPanel).stop();

		enableMenuBar(false);
		setCurrentPanel(new OptimizerPanel(getOwnerWindow(),editModel,miniStatistics,()->{
			setCurrentPanel(editorPanel);
			enableMenuBar(true);
		}));

		return true;
	}

	/**
	 * Befehl: (Toolbar bei Statistikansicht) - Modell zu diesen Ergebnissen
	 */
	private void commandSimulationModelForResults() {
		final Statistics statistics=statisticsPanel.getStatistics();
		if (statistics==null) {
			MsgBox.error(getOwnerWindow(),Language.tr("Window.CannotShowModel.Title"),Language.tr("Window.CannotShowModel.Info"));
			return;
		}

		final ModelViewerFrame viewer=new ModelViewerFrame(getOwnerWindow(),statistics.editModel,statistics,false,()->{
			if (!isDiscardModelOk()) return;
			editorPanel.setModel(statistics.editModel);
			setCurrentPanel(editorPanel);
		});
		viewer.setVisible(true);
	}

	/**
	 * Befehl: Extras - Simulationergebnisse verschiedener Modelle vergleichen
	 */
	private void commandExtrasCompare() {
		CompareSelectDialog dialog=new CompareSelectDialog(getOwnerWindow(),5);
		if (dialog.getClosedBy()!=BaseDialog.CLOSED_BY_OK) return;

		final File[] files=dialog.getSelectedFiles();
		final Statistics[] statistics=ComparePanel.getStatisticFiles(files);
		for (int i=0;i<statistics.length;i++) {
			if (statistics[i]==null) {
				MsgBox.error(getOwnerWindow(),Language.tr("Window.Compare.NotAValidStatisticsFile.Title"),String.format(Language.tr("Window.Compare.NotAValidStatisticsFile.Info"),""+(i+1),files[i].toString()));
				return;
			}
		}

		commandExtrasCompare(statistics);
	}

	/**
	 * Befehl: Extras - Simulationergebnisse verschiedener Modelle vergleichen
	 * @param statistics	Zu vergleichende Statistikdateien
	 * @see #commandExtrasCompare()
	 */
	private void commandExtrasCompare(final Statistics[] statistics) {
		final String[] title=new String[statistics.length];
		for (int i=0;i<statistics.length;i++) title[i]=statistics[i].editModel.name;

		enableMenuBar(false);
		setCurrentPanel(new ComparePanel(getOwnerWindow(),statistics,title,true,()->{
			if (currentPanel instanceof ComparePanel) {
				ComparePanel comparePanel=(ComparePanel) currentPanel;
				EditModel model=comparePanel.getModelForEditor();
				if (model!=null) {
					if (!isDiscardModelOk()) return;
					editorPanel.setModel(model);
				}
			}
			setCurrentPanel(editorPanel);
			enableMenuBar(true);
		}));
	}

	/**
	 * Befehl: Extras - Rechner
	 * @param initialExpression	Initial anzuzeigende Eingabe (kann <code>null</code> sein)
	 * @param initialDistribution	Initial anzuzeigende Wahrscheinlichkeitsverteilung (kann <code>null</code> sein)
	 */
	private void commandExtrasCalculator(final String initialExpression, final AbstractDistributionWrapper initialDistribution) {
		CalculatorWindow.show(this,initialExpression,initialDistribution);
	}

	/**
	 * Befehl: Extras - Warteschlangenrechner
	 */
	private void commandExtrasQueueingCalculator() {
		final QueueingCalculatorDialog dialog=new QueueingCalculatorDialog(this);
		dialog.setVisible(true);
	}

	/**
	 * Befehl: Extras - Verteilung anpassen
	 */
	private void commandExtrasFit() {
		final FitDialog dialog=new FitDialog(this);
		dialog.setVisible(true);
	}

	/**
	 * Befehl: Extras - Datenbankverbindung prüfen
	 */
	private void commandExtrasTestDatabaseConnection() {
		new DBConnectDialog(this);
	}

	/**
	 * Befehl: Extras - Kommandozeilenbefehl ausführen
	 */
	private void commandExtrasExecuteCommand() {
		final CommandLineDialog dialog=new CommandLineDialog(this,stream->new CommandLineSystem(null,stream),window->Help.topicModal(window,"CommandLineDialog"),setup.commandLineDialogParameters);
		setup.commandLineDialogParameters=dialog.getParameters();
		setup.saveSetup();
	}

	/**
	 * Befehl: Extras - Serverdienste
	 */
	private void commandExtrasSimulationServer() {
		enableMenuBar(false);
		setCurrentPanel(new ServerPanel(()->{
			setCurrentPanel(editorPanel);
			enableMenuBar(true);
		},this));
	}

	/**
	 * Befehl: Extras - Systeminformationen
	 */
	private void commandExtrasSystemInfo() {
		SystemInfoWindow.show(this);
	}

	/**
	 * Öffnet eine pdf.<br>
	 * Zunächst wird die pdf im "docs"-Unterverzeichnis des Programmverzeichnisses
	 * gesucht. Ist dort keine passende Datei vorhanden, so wird versucht, die
	 * Datei im Browser auf der Warteschlangensimulator-Homepage zu öffnen.
	 * @param fileName	Dateiname der pdf (ohne weiteren Pfad)
	 */
	private void openPDF(final String fileName) {
		final File local=new File(SetupData.getProgramFolder(),"docs/"+fileName);
		if (local.isFile()) {
			boolean ok=false;
			try {
				Desktop.getDesktop().open(local);
				ok=true;
			} catch (IOException e) {
				ok=false;
			}
			if (ok) return;
		}

		final String network="https://"+HOME_URL+"/Warteschlangensimulator/"+fileName;
		try {
			if (!MsgBox.confirmOpenURL(this,network)) return;
			Desktop.getDesktop().browse(new URI(network));
		} catch (IOException | URISyntaxException e1) {
			MsgBox.error(getOwnerWindow(),Language.tr("Window.Info.NoInternetConnection"),String.format(Language.tr("Window.Info.NoInternetConnection.Address"),network));
		}
	}

	/**
	 * Öffnet eine Webseite
	 * @param url	URL der aufzurufenden Webseite
	 */
	private void openWebpage(final String url) {
		try {
			if (!MsgBox.confirmOpenURL(this,url)) return;
			Desktop.getDesktop().browse(new URI(url));
		} catch (IOException | URISyntaxException e1) {
			MsgBox.error(getOwnerWindow(),Language.tr("Window.Info.NoInternetConnection"),String.format(Language.tr("Window.Info.NoInternetConnection.Address"),url));
		}
	}

	/**
	 * Befehl: Hilfe - Hilfe
	 */
	private void commandHelpHelp() {
		if (currentPanel==editorPanel) {Help.topic(this,"MainEditor"); return;}
		if (currentPanel==statisticsPanel) {Help.topic(this,"MainStatistik"); return;}
		Help.topic(this,"");
	}

	/**
	 * Befehl: Hilfe - Hilfe-Inhalt
	 */
	private void commandHelpContent() {
		Help.topic(this,"");
	}

	/**
	 * Befehl: Hilfe - Interaktive Einführung
	 */
	private void commandHelpInteractiveTutorial() {
		if (!commandFileModelNew()) return;
		new TutorialWindow(this);
	}

	/**
	 * Befehl: Hilfe - Hilfe zu aktuellem Element anzeigen
	 */
	private void commandHelpAutomaticHelpWindow() {
		new AutomaticHelpWindow(this,editorPanel);
	}

	/**
	 * Befehl: Hilfe - Tutorial (pdf)
	 */
	private void commandHelpTutorial() {
		openPDF(Language.tr("Main.Menu.Help.Tutorial.pdf"));
	}

	/**
	 * Befehl: Hilfe - Tutorial (pdf-Folien)
	 */
	private void commandHelpTutorialSlides() {
		openPDF(Language.tr("Main.Menu.Help.TutorialSlides.pdf"));
	}

	/**
	 * Befehl: Hilfe - Tutorial Video
	 */
	private void commandHelpTutorialVideo() {
		openWebpage("https://a-herzog.github.io/Warteschlangensimulator/Videos/Warteschlangensimulator_Tutorial.m4v");
	}

	/**
	 * Befehl: Hilfe - Referenzen - Rechen- und Skriptbefehle
	 */
	private void commandHelpScriptingReference() {
		openPDF(Language.tr("Main.Menu.Help.ScriptingReference.pdf"));
	}

	/**
	 * Befehl: Hilfe - Referenzen - Elementereferenz
	 */
	private void commandHelpElementReference() {
		openPDF(Language.tr("Main.Menu.Help.ElementReference.pdf"));
	}

	/**
	 * Befehl: Hilfe - Lehrbuch
	 * @param match	Direkt zu selektrierender Inhaltsverzeichnis- oder Sachverzeichniseintrag (kann <code>null</code> sein)
	 */
	private void commandHelpBook(final BookData.BookMatch match) {
		new BookDataDialog(this,match);
	}

	/**
	 * Befehl: Hilfe - Referenzen - Kommandozeilenbefehlsreferenz
	 */
	private void commandHelpCommandLineReference() {
		openPDF(Language.tr("Main.Menu.Help.CommandLineReference.pdf"));
	}

	/**
	 * Befehl: Hilfe - Referenzen - Tastenkombinationsreferenz
	 */
	private void commandHelpHotkeyReference() {
		openPDF(Language.tr("Main.Menu.Help.HotkeyReference.pdf"));
	}

	/**
	 * Befehl: Hilfe - Referenzen - Verteilungs-XML-Referenz
	 */
	private void commandHelpDistributionReference() {
		openPDF(Language.tr("Main.Menu.Help.DistributionReference.pdf"));
	}

	/**
	 * Befehl: Hilfe - Empfohlene Literatur
	 * @param index	Nummer des Buches zu dem die Homepage aufgerufen werden soll
	 */
	private void commandHelpLiterature(final int index) {
		String url=null;

		switch (index) {
		case 0:
			/* Herzog: "Simulation mit dem Warteschlangensimulator" */
			url=BookDataDialog.HOMEPAGE;
			break;
		case 1:
			/* Gross/Harris: "Fundamentals of Queueing Theory" */
			url="https://onlinelibrary.wiley.com/doi/book/10.1002/9781119453765";
			break;
		case 2:
			/* Bolch/Greiner/deMeer/Trivedi: "Queueing Networks and Markov Chains" */
			url="https://onlinelibrary.wiley.com/doi/book/10.1002/0471791571";
			break;
		case 3:
			/* Law/Kelton: "Simulation Modeling & Analysis" */
			url="https://www.researchgate.net/publication/239597629_Simulation_Modeling_and_Analysis";
			break;
		case 4: /* Waldmann/Helm: "Simulation stochastischer Systeme" */
			url="https://www.springer.com/de/book/9783662497579";
			break;
		case 5: /* Hedtstück: "Simulation diskreter Prozesse" */
			url="https://link.springer.com/book/10.1007/978-3-642-34871-6";
			break;
		case 6: /* Gutenschwager, Rabe, Spieckermann, Wenzel: "Simulation in Produktion und Logistik" */
			url="https://link.springer.com/book/10.1007/978-3-662-55745-7";
			break;
		default:
			return;
		}

		openWebpage(url);
	}

	/**
	 * Befehl: Hilfe - Support &amp; Unterstützung
	 */
	private void commandHelpSupport() {
		try {
			Desktop.getDesktop().mail(new URI("mailto:"+MainPanel.AUTHOR_EMAIL));
		} catch (IOException | URISyntaxException e1) {
			MsgBox.error(getOwnerWindow(),Language.tr("Window.Info.NoEMailProgram.Title"),String.format(Language.tr("Window.Info.NoEMailProgram.Info"),"mailto:"+MainPanel.AUTHOR_EMAIL));
		}
	}

	/**
	 * Befehl: Hilfe - Problemreport erstellen
	 */
	private void commandHelpSupportData() {
		final File file=ProblemReporter.selectOutputFile(this);
		if (file==null) return;

		final ProblemReporter reporter=new ProblemReporter(file);
		if (!reporter.process()) MsgBox.error(this,Language.tr("ProblemReporter.SaveError.Title"),String.format(Language.tr("ProblemReporter.SaveError.Info"),file.toString()));
	}

	/**
	 * Befehl: Hilfe - Homepage
	 */
	private void commandHelpHomepage() {
		openWebpage("https://"+WEB_URL);
	}

	/**
	 * Befehl: Hilfe - Nutzungsstatistik
	 */
	private void commandHelpUsageStatistics() {
		MsgBox.info(getOwnerWindow(),Language.tr("UsageStatistics.Title"),String.format(Language.tr("UsageStatistics.Info"),NumberTools.formatLong(UsageStatistics.getInstance().getSimulationClients())));
	}

	/**
	 * Befehl: Hilfe - Lizenzinformationen
	 */
	private void commandHelpLicenseInfo() {
		new LicenseViewer(this);
	}

	/**
	 * Befehl: Hilfe - Programminfo
	 */
	private void commandHelpInfo() {
		final String currentLanguage=setup.language;

		final InfoDialog dialog=new InfoDialog(getOwnerWindow(),EditModel.systemVersion);
		dialog.setVisible(true);

		if (!setup.language.equals(currentLanguage)) {
			setup.resetLanguageWasAutomatically();
			HelpBase.hideHelpFrame();
			if (reloadWindow!=null) SwingUtilities.invokeLater(reloadWindow);
		}

		if (dialog.showVersionHistory) Help.topic(this,"ChangeLog");
		if (dialog.showLicenses) commandHelpLicenseInfo();
	}

	/**
	 * Befehl: (Toolbar) - Schnellkorrektur
	 * @param button	Schaltfläche an der das Popupmenü mit den Korrekturvorschlägen angezeigt werden soll
	 */
	private void commandHelpFix(final JButton button) {
		final ModelElement element=editorPanel.getSelectedElementDirectOrArea();
		if (element instanceof ModelElementBox) {
			final JPopupMenu popup=((ModelElementBox)element).getQuickFixPopupMenu();
			if (popup!=null) popup.show(button,0,button.getHeight());
		}
	}

	@Override
	protected void action(final Object sender) {
		/* Datei - Letzte Dokumente */
		final Component[] sub=menuFileModelRecentlyUsed.getMenuComponents();
		for (int i=0;i<sub.length;i++) if (sender==sub[i]) {commandFileModelLoad(null,new File(setup.lastFiles[i])); return;}
	}

	/**
	 * Liefert alle Daten innerhalb dieses Panels als Objekt-Array
	 * um dann das Panel neu laden und die Daten wiederherstellen
	 * zu können.
	 * @return	6-elementiges Objekt-Array mit allen Daten des Panels
	 * @see #setAllData(Object[])
	 */
	public Object[] getAllData() {
		return new Object[]{
				editorPanel.getModel(),
				editorPanel.isModelChanged(),
				editorPanel.isTemplatesVisible(),
				editorPanel.getLastFile(),
				statisticsPanel.getStatistics(),
				Integer.valueOf((currentPanel==statisticsPanel)?1:0)
		};
	}

	/**
	 * Reinitialisiert die Daten in dem Panel wieder aus einem
	 * zuvor erstellten Objekt-Array.
	 * @param data	6-elementiges Objekt-Array mit allen Daten des Panels
	 * @return	Gibt an, ob die Daten aus dem Array erfolgreich geladen werden konnten
	 * @see #getAllData()
	 */
	public boolean setAllData(Object[] data) {
		if (data==null || data.length!=6) return false;
		if (!(data[0] instanceof EditModel)) return false;
		if (!(data[1] instanceof Boolean)) return false;
		if (!(data[2] instanceof Boolean)) return false;
		if (data[3]!=null && !(data[3] instanceof File)) return false;
		if (data[4]!=null && !(data[4] instanceof Statistics)) return false;
		if (!(data[5] instanceof Integer)) return false;

		editorPanel.setModel((EditModel)data[0]);
		editorPanel.setModelChanged((Boolean)data[1]);
		editorPanel.setTemplatesVisible((Boolean)data[2],true);
		editorPanel.setLastFile((File)data[3]); if (data[3]!=null) setAdditionalTitle(((File)data[3]).getName());
		statisticsPanel.setStatistics((Statistics)data[4]);
		if ((Integer)data[5]==1) setCurrentPanel(statisticsPanel); else setCurrentPanel(editorPanel);

		if (editorPanel.getLastFile()==null) {
			/* Wenn das aktuelle Modell ein unverändertes Beispielmodell ist, dieses in der neuen Sprache neu laden. */
			final int index=EditModelExamples.equalsIndex(editorPanel.getModel());
			if (index>=0) {
				final EditModel editModel=EditModelExamples.getExampleByIndex(getOwnerWindow(),index);
				if (FlatLaFHelper.isDark()) EditModelDark.processModel(editModel,EditModelDark.ColorMode.LIGHT,EditModelDark.ColorMode.DARK);
				editorPanel.setModel(editModel);
			}
		}

		return true;
	}

	/**
	 * Wird aufgerufen, wenn ein anderen Element im Editor selektriert wird.
	 * Erlaubt so, jeweils passende Schnellkorektur-Vorschläge anzuzeigen.
	 * @see EditorPanel#addSelectionListener(java.awt.event.ActionListener)
	 */
	private void selectionChanged() {
		if (currentPanel!=editorPanel) {
			fixButton.setVisible(false);
			return;
		}

		final ModelElement element=editorPanel.getSelectedElementDirectOrArea();
		fixButton.setVisible((element instanceof ModelElementBox) && (((ModelElementBox)element).hasQuickFix()));
	}
}
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

import org.apache.commons.math3.util.FastMath;

import language.Language;
import mathtools.NumberTools;
import mathtools.TimeTools;
import mathtools.distribution.swing.CommonVariables;
import mathtools.distribution.tools.DistributionTools;
import simcore.logging.CallbackLoggerData;
import simulator.Simulator;
import simulator.editmodel.EditModel;
import simulator.elements.RunModelAnimationViewer;
import simulator.logging.CallbackLoggerWithJS;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunDataTransporter;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import simulator.statistics.Statistics;
import systemtools.MsgBox;
import systemtools.SetupBase;
import tools.ButtonRotator;
import tools.SetupData;
import tools.UsageStatistics;
import ui.dialogs.AnimationJSInfoDialog;
import ui.dialogs.AnimationPanelBreakPointsDialog;
import ui.dialogs.ExpressionCalculatorDialog;
import ui.dialogs.LayersDialog;
import ui.dialogs.NextEventsViewerDialog;
import ui.images.Images;
import ui.mjpeg.AnimationRecordWaitDialog;
import ui.mjpeg.MJPEGSystem;
import ui.mjpeg.VideoSystem;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurfaceAnimator;
import ui.modeleditor.ModelSurfaceAnimatorBase;
import ui.modeleditor.ModelSurfacePanel;
import ui.modeleditor.SavedViews;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementAnimationInfoDialog;
import ui.modeleditor.coreelements.ModelElementAnimationInfoDialog.ClientInfo;
import ui.modeleditor.elements.ElementWithAnimationDisplay;
import ui.modeleditor.elements.ElementWithAnimationEditOptions;
import ui.modeleditor.elements.ModelElementAnalogValue;
import ui.modeleditor.elements.ModelElementAnimationConnect;
import ui.modeleditor.elements.ModelElementConveyor;
import ui.modeleditor.elements.ModelElementDashboard;
import ui.modeleditor.elements.ModelElementSource;
import ui.modeleditor.elements.ModelElementSourceMulti;
import ui.modeleditor.elements.ModelElementSourceRecord;
import ui.modeleditor.elements.ModelElementSub;
import ui.modeleditor.elements.ModelElementTank;
import ui.modelproperties.ModelPropertiesDialog;
import ui.tools.FlatLaFHelper;
import ui.tools.GlassInfo;
import ui.tools.SoundSystem;

/**
 * Diese Klasse zeigt die Animation der Simulation in einem eingebetteten
 * <code>ModelSurfacePanel</code> an.
 * @author Alexander Herzog
 */
public class AnimationPanel extends JPanel implements RunModelAnimationViewer {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -4834682399637727518L;

	/**
	 * Übergeordnetes Fenster in dem sich das Panel befindet (zur Minimiert-Erkennung, um in diesem Fall bei Fernsteuerung dennoch Animationen auszulösen)
	 */
	private final JFrame window;

	/**
	 * Referenz auf das Setup-Singleton.
	 */
	private final transient SetupData setup;

	/** Statuszeilen-Text zur Anzahl der aufgezeichneten Bilder */
	private final String infoMJPEG;
	/** Statuszeilen-Text mit Informationen zur Simulation (bei unbegrenzter Zeit) */
	private final String infoNoSum;
	/** Statuszeilen-Text mit Informationen zur Simulation (bei begrenzter Simulationszeit) */
	private final String infoSum;

	/** Gibt an, ob die Animation direkt nach der Initialisierung starten soll oder ob sich das System anfänglich im Pausemodus befinden soll */
	private boolean startPaused;
	/** Ist <code>true</code>, wenn die Warm-up-Phase bei der Animation zunächst als Simulation vorab ausgeführt werden soll */
	private boolean fastWarmUp;
	/** Besitzt das Modell Analogwert-Elemente? */
	private boolean hasAnalogElements;
	/** Besitzt das Modell Fließbänder? */
	private boolean hasConveyorElements;
	/** Editor-Modell */
	private transient EditModel model;
	/** Lineale um die Zeichenfläche herum */
	private final RulerPanel rulerPanel;
	/** Zeichenfläche */
	private final ModelSurfacePanel surfacePanel;
	/** Animationssystem */
	private transient ModelSurfaceAnimator surfaceAnimator;
	/** Sichert parallele Zugriffe auf {@link #simulator} ab. */
	private Semaphore simulatorLock=new Semaphore(1);
	/** Simulator für das Modell */
	private transient Simulator simulator;
	/** Vorgelagerter, im Konstruktor übergebener optionaler Logger */
	private transient CallbackLoggerWithJS parentLogger;
	/** Logger, über den die Einzelschritt ausgaben angezeigt werden */
	private transient CallbackLoggerWithJS logger;
	/** Läuft die Animation momentan? */
	private boolean running;
	/** Runnable, das aufgerufen wird, wenn die Simulation beendet wurde */
	private Runnable animationDone;
	/** Runnable, das aufgerufen wird, wenn die Simulation ohne Animation zu Ende geführt werden soll */
	private Runnable sendToSimulation;
	/** System zur Erzeugung einer MJPEG-Videodatei aus einzelnen Animationsbildern */
	private transient VideoSystem encoder;

	/** Ruft regelmäßig {@link #abortRunTest()} auf */
	private transient Timer timer;
	/** Wurde die Simulation erfolgreich beendet? ({@link #isSimulationSuccessful()}) */
	private boolean simulationSuccessful;
	/** Soll die Simulation abgebrochen werden? */
	private boolean abortRun;
	/** Soll die Simulation ohne Animationsausgabe zu Ende geführt werden? ({@link #finishAsSimulation()}) */
	private boolean continueAsSimulation;
	/** Simulationsdatenobjekt */
	private transient SimulationData simData;
	/** Stellt sicher, dass {@link #simulator} nicht auf <code>null</code> gesetzt wird, während {@link #updateViewer(SimulationData)} läuft */
	private Semaphore mutex;
	/** Zeitpunkt (bezogen auf die Simulationszeit) des letzten Aufrufs von {@link #delaySystem(SimulationData, int)} */
	private long lastTimeStep;
	/** Zeit für einen Zeitschritt ({@link #calculateMinimalTimeStep()}) */
	private double delaySystem;
	/** Tatsächliche Verzögerung pro Animationsschritt (0..100); ist im Einzelschrittmodus 100, während {@link #delay} unverändert bleibt */
	private int delayInt;
	/** Verzögerung pro Animationsschritt (0..100) */
	private int delay;
	/** Wird von {@link #animationDelayChanged()} auf <code>true</code> gesetzt und dann von {@link #delaySystem(SimulationData, int)} ausgewertet, wenn sich die Animationsgeschwindigkeit geändert hat. */
	private boolean speedChanged;

	/** Listener für Klicks auf die verschiedenen Symbolleisten-Schaltflächen */
	private final transient ToolBarListener toolbarListener;

	/* Symbolleiste oben */

	/** Schaltfläche "Aufzeichnung starten" */
	private final JButton buttonStartRecording;
	/** Schaltfläche "Beenden" */
	private final JButton buttonAbort;
	/** Schaltfläche "Bild speichern" */
	private final JButton buttonScreenshot;
	/** Schaltfläche "Exportieren" */
	private final JButton buttonExport;
	/** Schaltfläche "Simulation" (Animation als Simulation zu Ende führen) */
	private final JButton buttonSimulation;
	/** Schaltfläche "Einstellungen" (zum Auslösen eines Popupmenüs) */
	private final JButton buttonTools;
	/** Schaltfläche "Start"/"Pause" */
	private final JButton buttonPlayPause;
	/** Schaltfläche "Einzelschritt" */
	private final JButton buttonStep;
	/** Schaltfläche "Geschwindigkeit" (zum Auslösen eines Popupmenüs) */
	private final JButton buttonSpeed;

	/** Popupmenüpunkt "Animationsstart" - "Animation sofort starten" */
	private JMenuItem menuStartModeRun;
	/** Popupmenüpunkt "Animationsstart" - "Im Pause-Modus starten" */
	private JMenuItem menuStartModePause;
	/** Popupmenüpunkt "Animationsende" - "Sofort zur Statistikansicht umschalten" */
	private JMenuItem menuFinishModeRun;
	/** Popupmenüpunkt "Animationsende" - "Animation zunächst pausien" */
	private JMenuItem menuFinishModePause;
	/** Popupmenüpunkt "Analoge Werte in Animation" - "Schnelle Animation" */
	private JMenuItem menuAnalogValuesFast;
	/** Popupmenüpunkt "Analoge Werte in Animation" - "Änderungen exakt anzeigen (langsam)" */
	private JMenuItem menuAnalogValuesExact;
	/** Popupmenüpunkt "Verzeichnis zum Speichern von Bildern" - "Im Nutzerverzeichnis" */
	private JMenuItem menuScreenshotModeHome;
	/** Popupmenüpunkt "Verzeichnis zum Speichern von Bildern" - "Im ausgewählten Verzeichnis" */
	private JMenuItem menuScreenshotModeCustom;
	/** Popupmenüpunkt "Logging-Daten im Einzelschrittmodus anzeigen" */
	private JCheckBoxMenuItem menuShowLog;
	/** Popupmenüpunkt "Animation bei Pause-Skriptanweisung unterbrechen" */
	private JCheckBoxMenuItem menuRespectPauseCommand;

	/* Vertikale Symbolleiste */

	/** Schaltfläche "Modell" */
	private final JButton buttonProperties;
	/** Schaltfläche "Aktuelle Daten" */
	private final JButton buttonCurrentData;
	/** Schaltfläche "Statistik" */
	private final JButton buttonCurrentStatistics;
	/** Schaltfläche "Haltepunkte" */
	private final JButton buttonBreakpoints;

	/* Statusleiste */

	/** Statusinformationen */
	private final JLabel statusBar;
	/** Fortschritt der Animation */
	private final JProgressBar progressBar;
	/** Aktueller Zoomfaktor */
	private JLabel labelZoom;
	/** Schaltfläche für Zoomfaktor verringern */
	private JButton buttonZoomOut;
	/** Schaltfläche für Zoomfaktor vergrößern */
	private JButton buttonZoomIn;
	/** Schaltfläche für Standard-Zoomfaktor */
	private JButton buttonZoomDefault;
	/** Schaltfläche "Modell zentrieren" */
	private JButton buttonFindModel;
	/** Schaltfläche "Dashboard" */
	private JButton buttonDashboard;
	/** Schaltfläche "Ansichten" */
	private JButton buttonViews;

	/* Logging-Bereich unten */

	/** Logging-Ausgabe-Bereich */
	private final JPanel logArea;
	/** Scoll-Bereich, in den {@link #logLabel} eingebettet werden soll */
	private final JScrollPane logScroll;
	/** Textfeld zur Ausgabe der Logging-Ausgaben */
	private final JLabel logLabel;
	/** Aktueller Zeitwert auf den sich die Logging-Ausgaben beziehen */
	private long logTimeStamp;
	/** Wiederverwendbarer {@link StringBuilder} für die Logging-Ausgaben */
	private StringBuilder logText;
	/** Wiederverwendbarer {@link StringBuilder} für die Logging-Ausgaben ohne Formatierung */
	private StringBuilder logTextPlain;
	/** Welcher der verfügbaren Logging-Ausgaben wird gerade angezeigt? (aktuell oder vorherige) */
	private int logTextDisplayIndex;
	/** Liste der vorherigen Logging-Ausgaben */
	private List<String> logTextHistory;
	/** Liste der vorherigen Logging-Ausgaben ohne Formatierung */
	private List<String> logTextHistoryPlain;
	/** Schaltfläche "Vorherige Logging-Ausgabe" */
	private final JButton logPrevious;
	/** Schaltfläche "Nächste Logging-Ausgabe" */
	private final JButton logNext;
	/** Schaltfläche "Aktuelle Logging-Ausgabe" */
	private final JButton logCurrent;
	/** Schaltfläche "Logging-Ausgabe kopieren" */
	private final JButton logCopy;
	/** Schaltfläche "Ausdruck berechnen" */
	private final JButton logExpression;
	/** Schaltfläche "Ergebnisse der Javascript-Skriptausführung" */
	private final JButton logJS;
	/** Schaltfläche "Nächste geplante Ereignisse" */
	private final JButton logEvents;

	/**
	 * Unter-Animator-Element ein, welches ebenfalls bei Animationsschritten benachrichtigt werden soll<br>
	 * (Kann <code>null</code> sein, wenn ein Untermodell-Fenster offen ist)
	 * @see #addSubViewer(RunModelAnimationViewer)
	 * @see #removeSubViewer(RunModelAnimationViewer)
	 */
	private Set<RunModelAnimationViewer> subViewers;

	/**
	 * Erzeugt eine kleine Schaltfläche für die Zoom-Symbolleiste unten rechts in der Statusleiste
	 * @param hint	Zusätzlich anzuzeigender Tooltip für den Symbolleisten-Eintrag (kann <code>null</code> sein, wenn kein Tooltip angezeigt werden soll)
	 * @param icon	Pfad zu dem Icon, das in dem Symbolleisten-Eintrag angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @return	Neue Schaltfläche
	 */
	private JButton createZoomAreaButton(final String hint, final Icon icon) {
		final JButton button=createToolbarButton(null,"",hint,icon);
		button.setPreferredSize(new Dimension(20,20));
		button.setBorderPainted(false);
		button.setFocusPainted(false);
		button.setContentAreaFilled(false);
		return button;
	}

	/**
	 * Konstruktor der Klasse
	 * @param window	Übergeordnetes Fenster in dem sich das Panel befindet (zur Minimiert-Erkennung, um in diesem Fall bei Fernsteuerung dennoch Animationen auszulösen)
	 */
	public AnimationPanel(final JFrame window) {
		super();
		this.window=window;
		setup=SetupData.getSetup();

		infoMJPEG=", "+Language.tr("Animation.ImagesRecorded");
		infoNoSum=Language.tr("Animation.SimulatedTime.Unlimited");
		infoSum=Language.tr("Animation.SimulatedTime.Limited");

		subViewers=new HashSet<>();

		model=null;
		mutex=new Semaphore(1);
		toolbarListener=new ToolBarListener();

		setLayout(new BorderLayout());

		/* Toolbar oben */
		final JToolBar toolBar=new JToolBar();
		add(toolBar,BorderLayout.NORTH);
		toolBar.setFloatable(false);

		buttonStartRecording=createToolbarButton(toolBar,Language.tr("Animation.Toolbar.StartRecording"),Language.tr("Animation.Toolbar.StartRecording.Info"),Images.ANIMATION_RECORD.getIcon());
		buttonStartRecording.setVisible(false);

		buttonAbort=createToolbarButton(toolBar,Language.tr("Animation.Toolbar.Stop"),Language.tr("Animation.Toolbar.Stop.Info")+" ("+keyStrokeToString(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0))+")",Images.GENERAL_CANCEL.getIcon());
		toolBar.addSeparator();

		buttonScreenshot=createToolbarButton(toolBar,Language.tr("Animation.Toolbar.Image"),Language.tr("Animation.Toolbar.Image.Info")+" ("+keyStrokeToString(KeyStroke.getKeyStroke(KeyEvent.VK_S,InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_DOWN_MASK))+")",Images.ANIMATION_SCREENSHOT.getIcon());
		updateScreenshotButtonHint();
		buttonExport=createToolbarButton(toolBar,Language.tr("Animation.Toolbar.Export"),Language.tr("Animation.Toolbar.Export.Info"),Images.ANIMATION_EXPORT.getIcon());

		buttonSimulation=createToolbarButton(toolBar,Language.tr("Animation.Toolbar.Simulation"),Language.tr("Animation.Toolbar.Simulation.Info")+" ("+keyStrokeToString(KeyStroke.getKeyStroke(KeyEvent.VK_F5,0))+")",Images.SIMULATION.getIcon());
		buttonSimulation.setVisible(false);
		buttonTools=createToolbarButton(toolBar,Language.tr("Animation.Toolbar.Tools"),Language.tr("Animation.Toolbar.Tools.Info"),Images.GENERAL_TOOLS.getIcon());
		toolBar.addSeparator();

		buttonPlayPause=createToolbarButton(toolBar,Language.tr("Animation.Toolbar.Pause"),Language.tr("Animation.Toolbar.Pause.Info")+" ("+keyStrokeToString(KeyStroke.getKeyStroke(KeyEvent.VK_F6,0))+")",Images.ANIMATION_PAUSE.getIcon());
		buttonStep=createToolbarButton(toolBar,Language.tr("Animation.Toolbar.Step"),Language.tr("Animation.Toolbar.Step.Info")+" ("+keyStrokeToString(KeyStroke.getKeyStroke(KeyEvent.VK_F7,0))+")",Images.ANIMATION_STEP.getIcon());
		buttonStep.setEnabled(false);
		buttonSpeed=createToolbarButton(toolBar,Language.tr("Animation.Toolbar.Speed"),Language.tr("Animation.Toolbar.Speed.Info")+" ("+keyStrokeToString(KeyStroke.getKeyStroke(KeyEvent.VK_ADD,InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_DOWN_MASK))+"/"+keyStrokeToString(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT,InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_DOWN_MASK))+")",Images.ANIMATION_SPEED.getIcon());

		addUserButtons(toolBar);

		/* Area mit linkem Toolbar und Surface */
		final JPanel content=new JPanel(new BorderLayout());
		add(content,BorderLayout.CENTER);

		/* Toolbar links */
		final JToolBar leftToolBar=new JToolBar(SwingConstants.VERTICAL);
		leftToolBar.setFloatable(false);
		add(leftToolBar,BorderLayout.WEST);

		buttonProperties=createRotatedToolbarButton(leftToolBar,Language.tr("Editor.ModelProperties.Short"),Language.tr("Editor.ModelProperties.Info")+" ("+keyStrokeToString(KeyStroke.getKeyStroke(KeyEvent.VK_F2,InputEvent.CTRL_DOWN_MASK))+")",Images.MODEL.getIcon());
		buttonCurrentData=createRotatedToolbarButton(leftToolBar,Language.tr("Editor.AnimationData.Short"),Language.tr("Editor.AnimationData.Info")+" ("+keyStrokeToString(KeyStroke.getKeyStroke(KeyEvent.VK_F3,0))+")",Images.ANIMATION_EVALUATE_EXPRESSION.getIcon());
		buttonCurrentStatistics=createRotatedToolbarButton(leftToolBar,Language.tr("Editor.Statistics.Short"),Language.tr("Editor.Statistics.Info"),Images.STATISTICS.getIcon());
		buttonBreakpoints=createRotatedToolbarButton(leftToolBar,Language.tr("Editor.Breakpoints.Short"),Language.tr("Editor.Breakpoints.Info"),Images.ANIMATION_BREAKPOINTS.getIcon());

		/* Surface in der Mitte */
		content.add(rulerPanel=new RulerPanel(surfacePanel=new ModelSurfacePanel(true,false),SetupData.getSetup().showRulers),BorderLayout.CENTER);
		surfacePanel.addZoomChangeListener(e->zoomChanged());
		surfacePanel.addShowModelPropertiesListener(e->{
			final String cmd=e.getActionCommand();
			if (cmd.equals(ModelSurfacePanel.PROPERTIES_TYPE_PROPERTIES)) {
				showModelPropertiesDialog(null);
				return;
			}
			if (cmd.equals(ModelSurfacePanel.PROPERTIES_TYPE_PROPERTIES_OPERATORS)) {
				showModelPropertiesDialog(ModelPropertiesDialog.InitialPage.OPERATORS);
				return;
			}
			if (cmd.equals(ModelSurfacePanel.PROPERTIES_TYPE_PROPERTIES_TRANSPORTERS)) {
				showModelPropertiesDialog(ModelPropertiesDialog.InitialPage.TRANSPORTERS);
				return;
			}
			if (cmd.equals(ModelSurfacePanel.PROPERTIES_TYPE_PROPERTIES_SCHEDULES)) {
				showModelPropertiesDialog(ModelPropertiesDialog.InitialPage.SCHEDULES);
				return;
			}
			if (cmd.equals(ModelSurfacePanel.PROPERTIES_TYPE_LAYERS)) {
				final LayersDialog dialog=new LayersDialog(this,model,true);
				dialog.setVisible(true);
			}
		});

		/* Statusbar unten */
		final JPanel statusPanel=new JPanel(new BorderLayout());
		add(statusPanel,BorderLayout.SOUTH);

		final JPanel statusBarOuter=new JPanel(new BorderLayout());
		statusPanel.add(statusBarOuter,BorderLayout.CENTER);
		statusBarOuter.add(statusBar=new JLabel(""),BorderLayout.WEST);
		statusBar.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));
		final JToolBar zoomArea=new JToolBar(SwingConstants.HORIZONTAL);
		zoomArea.setFloatable(false);

		final JPanel progressBarOuter=new JPanel(new BorderLayout());
		statusBarOuter.add(progressBarOuter,BorderLayout.CENTER);
		progressBarOuter.setBorder(BorderFactory.createEmptyBorder(5,10,5,20));
		progressBarOuter.add(progressBar=new JProgressBar(),BorderLayout.CENTER);

		progressBar.setValue(0);

		statusPanel.add(zoomArea,BorderLayout.EAST);
		zoomArea.add(labelZoom=new JLabel("100% "));
		labelZoom.addMouseListener(new MouseAdapter() {
			@Override public void mousePressed(MouseEvent e) {showZoomContextMenu(labelZoom);}
		});
		labelZoom.setToolTipText(Language.tr("Editor.SetupZoom"));

		zoomArea.add(buttonZoomOut=createZoomAreaButton(Language.tr("Main.Menu.View.ZoomOut")+" ("+keyStrokeToString(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT,InputEvent.CTRL_DOWN_MASK))+")",Images.ZOOM_OUT.getIcon()));
		zoomArea.add(buttonZoomIn=createZoomAreaButton(Language.tr("Main.Menu.View.ZoomIn")+" ("+keyStrokeToString(KeyStroke.getKeyStroke(KeyEvent.VK_ADD,InputEvent.CTRL_DOWN_MASK))+")",Images.ZOOM_IN.getIcon()));
		zoomArea.add(buttonZoomDefault=createZoomAreaButton(Language.tr("Main.Menu.View.ZoomDefault")+" ("+keyStrokeToString(KeyStroke.getKeyStroke(KeyEvent.VK_MULTIPLY,InputEvent.CTRL_DOWN_MASK))+")",Images.ZOOM.getIcon()));
		zoomArea.add(buttonFindModel=createZoomAreaButton(Language.tr("Main.Menu.View.CenterModel")+" ("+keyStrokeToString(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD0,InputEvent.CTRL_DOWN_MASK))+")",Images.ZOOM_CENTER_MODEL.getIcon()));
		zoomArea.addSeparator();
		zoomArea.add(buttonDashboard=createZoomAreaButton(Language.tr("Main.Menu.View.Dashboard")+" ("+keyStrokeToString(KeyStroke.getKeyStroke(KeyEvent.VK_F12,InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_DOWN_MASK))+")",Images.MODELEDITOR_ELEMENT_ANIMATION_BAR_CHART.getIcon()));
		zoomArea.add(buttonViews=createZoomAreaButton(Language.tr("Main.Menu.View.Views"),Images.ZOOM_VIEWS.getIcon()));
		buttonViews.addMouseListener(new MouseAdapter() {
			@Override public void mousePressed(final MouseEvent e) {if (SwingUtilities.isRightMouseButton(e)) showViewPopup(buttonViews);}
		});

		statusPanel.add(logArea=new JPanel(new BorderLayout()),BorderLayout.SOUTH);
		logArea.setVisible(false);
		logArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		logArea.setBackground(FlatLaFHelper.isDark()?Color.DARK_GRAY:Color.WHITE);
		logScroll=new JScrollPane(logLabel=new JLabel());
		logArea.add(logScroll,BorderLayout.CENTER);
		logLabel.setBorder(BorderFactory.createEmptyBorder(2,5,2,5));
		logScroll.setMaximumSize(new Dimension(1000,Math.min(175,getHeight()/5)));
		logScroll.setPreferredSize(new Dimension(0,0));
		logScroll.setOpaque(false);
		final JToolBar logToolBar=new JToolBar(SwingConstants.VERTICAL);
		logArea.add(logToolBar,BorderLayout.EAST);
		logToolBar.setFloatable(false);
		logToolBar.add(Box.createVerticalGlue());
		logPrevious=createToolbarButton(logToolBar,"",Language.tr("Animation.Log.Previous"),Images.ARROW_UP.getIcon());
		logNext=createToolbarButton(logToolBar,"",Language.tr("Animation.Log.Next"),Images.ARROW_DOWN.getIcon());
		logCurrent=createToolbarButton(logToolBar,"",Language.tr("Animation.Log.Current"),Images.ARROW_DOWN_END.getIcon());
		logCopy=createToolbarButton(logToolBar,"",Language.tr("Animation.Log.Copy"),Images.EDIT_COPY.getIcon());
		logExpression=createToolbarButton(logToolBar,"",Language.tr("Animation.Log.Expression")+" ("+keyStrokeToString(KeyStroke.getKeyStroke(KeyEvent.VK_F3,0))+")",Images.ANIMATION_EVALUATE_EXPRESSION.getIcon());
		logJS=createToolbarButton(logToolBar,"",Language.tr("Animation.Log.JS"),Images.ANIMATION_EVALUATE_SCRIPT.getIcon());
		logEvents=createToolbarButton(logToolBar,"",Language.tr("Animation.Log.Events"),Images.ANIMATION_LIST_NEXT_EVENTS.getIcon());

		logScroll.setMaximumSize(new Dimension(1000,logToolBar.getHeight()+10));
		logScroll.setPreferredSize(new Dimension(0,logToolBar.getHeight()+10));

		delay=setup.animationDelay*10;
		delayInt=delay;
		animationDelayChanged();

		final InputMap input=getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		input.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0),"keyEscape");
		input.put(KeyStroke.getKeyStroke(KeyEvent.VK_C,InputEvent.CTRL_DOWN_MASK),"keyCtrlC");
		input.put(KeyStroke.getKeyStroke(KeyEvent.VK_S,InputEvent.CTRL_DOWN_MASK),"keyCtrlS");
		input.put(KeyStroke.getKeyStroke(KeyEvent.VK_S,InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_DOWN_MASK),"keyCtrlShiftS");
		input.put(KeyStroke.getKeyStroke(KeyEvent.VK_V,InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_DOWN_MASK),"keyCtrlShiftV");
		input.put(KeyStroke.getKeyStroke(KeyEvent.VK_W,InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_DOWN_MASK),"keyCtrlShiftW");
		input.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2,InputEvent.CTRL_DOWN_MASK),"keyCtrlF2");
		input.put(KeyStroke.getKeyStroke(KeyEvent.VK_F3,0),"keyF3");
		input.put(KeyStroke.getKeyStroke(KeyEvent.VK_F5,0),"keyF5");
		input.put(KeyStroke.getKeyStroke(KeyEvent.VK_F6,0),"keyF6");
		input.put(KeyStroke.getKeyStroke(KeyEvent.VK_F7,0),"keyF7");
		input.put(KeyStroke.getKeyStroke(KeyEvent.VK_F12,InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_DOWN_MASK),"ctrlShiftkeyF12");
		input.put(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS,InputEvent.CTRL_DOWN_MASK),"ctrlPlus");
		input.put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD,InputEvent.CTRL_DOWN_MASK),"ctrlPlus");
		input.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS,InputEvent.CTRL_DOWN_MASK),"ctrlMinus");
		input.put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT,InputEvent.CTRL_DOWN_MASK),"ctrlMinus");
		input.put(KeyStroke.getKeyStroke(KeyEvent.VK_MULTIPLY,InputEvent.CTRL_DOWN_MASK),"ctrlMultiply");
		input.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD0,InputEvent.CTRL_DOWN_MASK),"ctrl0");
		input.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME,InputEvent.CTRL_DOWN_MASK),"ctrlHome");
		input.put(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS,InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_DOWN_MASK),"ctrlShiftPlus");
		input.put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD,InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_DOWN_MASK),"ctrlShiftPlus");
		input.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS,InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_DOWN_MASK),"ctrlShiftMinus");
		input.put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT,InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_DOWN_MASK),"ctrlShiftMinus");

		addAction("keyEscape",e->{if (buttonAbort.isEnabled()) {closeRequest(); buttonAbort.setEnabled(false);}});
		addAction("keyCtrlC",e->commandCopyToClipboard());
		addAction("keyCtrlS",e->saveScreenshotSelectFile());
		addAction("keyCtrlShiftS",e->saveScreenshot());
		addAction("keyCtrlShiftV",e->savedViewSelect(1));
		addAction("keyCtrlShiftW",e->savedViewSelect(-1));
		addAction("keyCtrlF2",e->showModelPropertiesDialog(null));
		addAction("keyF3",e->calcExpression());
		addAction("keyF5",e->finishAsSimulation());
		addAction("keyF6",e->playPause());
		addAction("keyF7",e->step(false));
		addAction("ctrlShiftkeyF12",e->showDashboard());
		addAction("ctrlPlus",e->zoomIn());
		addAction("ctrlMinus",e->zoomOut());
		addAction("ctrlMultiply",e->zoomDefault());
		addAction("ctrl0",e->centerModel());
		addAction("ctrlHome",e->scrollToTop());
		addAction("ctrlShiftPlus",e->speed(1));
		addAction("ctrlShiftMinus",e->speed(-1));
	}

	/**
	 * Generiert basierend auf einem Hotkey die Textbeschreibung für den Hotkey (z.B. zur Anzeige in Symbolleisten-Schaltflächen Tooltips)
	 * @param key	Hotkey
	 * @return	Textbeschreibung für den Hotkey
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
	 * Legt eine Aktion an und fügt diese in die {@link ActionMap} des Panels ein.
	 * @param name	Name der Aktion
	 * @param action	Auszuführendes Callback beim Aufruf der Aktion
	 */
	private void addAction(final String name, final Consumer<ActionEvent> action) {
		getActionMap().put(name,new AbstractAction() {
			private static final long serialVersionUID=-6092283861324716876L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				if (action!=null) action.accept(e);
			}
		});
	}

	/**
	 * Ermöglicht das Hinzufügen weiterer Schaltflächen zur Symbolleiste in abgeleiteten Klassen
	 * @param toolbar	Toolbar des Panels
	 */
	protected void addUserButtons(final JToolBar toolbar) {}

	/**
	 * Erzeugt eine Schaltfläche mit um 90° gegen den Uhrzeigersinn rotierter Beschriftung.
	 * @param toolbar	Symbolleiste in die die neue Schaltfläche eingefügt werden soll (kann <code>null</code> sein, dann wird die Schaltfläche in keine Symbolleiste eingefügt)
	 * @param title	Beschriftung der Schaltfläche (darf nicht leer sein)
	 * @param hint	Tooltip für die Schaltfläche (kann <code>null</code> sein)
	 * @param icon	Icon für die Schaltfläche (kann <code>null</code> sein)
	 * @return	Neue Schaltfläche
	 */
	private JButton createRotatedToolbarButton(final JToolBar toolbar, final String title, final String hint, final Icon icon) {
		ImageIcon rotatedIcon=null;

		if (icon instanceof ImageIcon) {
			final double scale=SetupData.getSetup().scaleGUI;
			if (scale!=1.0) {
				final int w=(int)Math.round(icon.getIconWidth()*scale);
				final int h=(int)Math.round(icon.getIconHeight()*scale);
				final Image temp=((ImageIcon)icon).getImage().getScaledInstance(w,h,Image.SCALE_SMOOTH);
				rotatedIcon=new ImageIcon(temp,"");
			} else {
				rotatedIcon=(ImageIcon)icon;
			}
		} else {
			rotatedIcon=new ImageIcon(new BufferedImage(16,16,BufferedImage.TYPE_4BYTE_ABGR));
		}

		final JButton button=new ButtonRotator.RotatedButton(title,rotatedIcon);

		if (toolbar!=null) toolbar.add(button);
		if (hint!=null) button.setToolTipText(hint);
		button.addActionListener(toolbarListener);

		return button;
	}

	/**
	 * Legt einen neuen Symbolleisten-Eintrag an
	 * @param toolbar	Übergeordnetes Symbolleisten-Element
	 * @param title	Name des neuen Symbolleisten-Eintrags
	 * @param hint	Zusätzlich anzuzeigender Tooltip für den Symbolleisten-Eintrag (kann <code>null</code> sein, wenn kein Tooltip angezeigt werden soll)
	 * @param icon	Pfad zu dem Icon, das in dem Symbolleisten-Eintrag angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @return	Neu erstellter Symbolleisten-Eintrag
	 */
	protected final JButton createToolbarButton(final JToolBar toolbar, final String title, final String hint, final Icon icon) {
		JButton button=new JButton(title);
		if (toolbar!=null) toolbar.add(button);
		if (hint!=null) button.setToolTipText(hint);
		button.addActionListener(toolbarListener);
		if (icon!=null) button.setIcon(icon);
		return button;
	}

	/**
	 * Stellt die linke obere Ecke des sichtbaren Bereichs von {@link #surfacePanel} ein.
	 * @param position	Position der linken oberen Ecke
	 */
	private void setSurfacePosition(final Point position) {
		if (surfacePanel.getParent() instanceof JViewport) {
			JViewport viewport=(JViewport)surfacePanel.getParent();
			viewport.setViewPosition(position);
		}
	}

	/**
	 * Minimaler Zeitschritt aus Sicht einer Kundenquelle
	 * @param record	Kundenquelle
	 * @param oldMin	Bisheriger minimaler Zeitschritt
	 * @return	Neuer minimaler Zeitschritt
	 * @see #calculateMinimalTimeStep()
	 */
	private double calculateMinimalTimeStepFromRecord(final ModelElementSourceRecord record, final double oldMin) {
		final double mean=DistributionTools.getMean(record.getInterarrivalTimeDistribution());
		final long multiply=record.getTimeBase().multiply;
		if (mean>0 && mean*multiply<oldMin) return mean*multiply;
		return oldMin;
	}

	/**
	 * Berechnet einen minimalen Zeitschritt für die Animation.
	 * @see #delaySystem
	 */
	private void calculateMinimalTimeStep() {
		double min=86400;

		for (ModelElement element: model.surface.getElements()) {
			if (element instanceof ModelElementSource) {
				final ModelElementSourceRecord record=((ModelElementSource)element).getRecord();
				min=calculateMinimalTimeStepFromRecord(record,min);
			}
			if (element instanceof ModelElementSourceMulti) for (ModelElementSourceRecord record: ((ModelElementSourceMulti)element).getRecords()) {
				if (record.isActive()) min=calculateMinimalTimeStepFromRecord(record,min);
			}
		}

		delaySystem=min/500;
	}

	/**
	 * Stellt das zu simulierende Modell ein. Der Simulator wird durch diese Methode gestartet, darf
	 * also nicht bereits vorher gestartet worden sein. Vor der Erstellung des Simulator-Objekts muss
	 * außerdem zunächst die <code>makeAnimationModel</code>-Methode auf das Editor-Modell angewandt werden.
	 * @param model	Editor-Modell (für die Animation des Simulationsverlaufs in einem {@link ModelSurfacePanel}-Objekt)
	 * @param simulator	Simulator für das Modell (darf noch nicht gestartet worden sein)
	 * @param logger	Logger, über den die Einzelschritt ausgaben angezeigt werden
	 * @param recordFile	Videodatei, in der die Animation aufgezeichnet werden soll
	 * @param startRecordingImmediately	Aufzeichnung sofort bei Beginn der Animation starten?
	 * @param scaleFrame	Skalierung der Bilder vor dem Übernehmen ins Video (0.01..1)
	 * @param paintTimeStamp	Fügt bei der Aufzeichnung in das Video den jeweils aktuellen Simulationszeit-Wert ein
	 * @param fastWarmUp	Ist <code>true</code>, wenn die Warm-up-Phase bei der Animation zunächst als Simulation vorab ausgeführt werden soll
	 * @param zoom	Zoomfaktor für das Animations-Surface
	 * @param raster	Rasteranzeige auf dem Animations-Surface
	 * @param showRulers	Lineale anzeigen
	 * @param position	Position der linken oberen Ecke des Animations-Surface
	 * @param animationDone	Runnable, das aufgerufen wird, wenn die Simulation beendet wurde
	 * @param sendToSimulation	Runnable, das aufgerufen wird, wenn die Simulation ohne Animation zu Ende geführt werden soll
	 * @param startPaused	Gibt an, ob die Animation direkt nach der Initialisierung starten soll oder ob sich das System anfänglich im Pausemodus befinden soll
	 * @param startFullRecording	Wird die Animation im Pausemodus gestartet, so wird direkt der erste Schritt ausgeführt. Über diese Funktion kann angegeben werden, dass dieser Schritt im vollständigen Erfassungsmodus durchgeführt werden soll.
	 * @see #makeAnimationModel(EditModel)
	 */
	public void setSimulator(final EditModel model, final Simulator simulator, final CallbackLoggerWithJS logger, final File recordFile, final boolean startRecordingImmediately, final double scaleFrame, final boolean paintTimeStamp, final boolean fastWarmUp, final double zoom, final ModelSurface.Grid raster, final boolean showRulers, final Point position, final Runnable animationDone, final Runnable sendToSimulation, final boolean startPaused, final boolean startFullRecording) {
		this.model=model;
		this.startPaused=startPaused;
		this.fastWarmUp=fastWarmUp;

		/* Logging-Daten von ggf. vorheriger Simulation löschen */
		if (logTextHistory!=null) logTextHistory.clear();
		if (logTextHistoryPlain!=null) logTextHistoryPlain.clear();

		/* Geschwindigkeit einstellen */
		delay=setup.animationDelay*10;
		delayInt=delay;
		animationDelayChanged();

		buttonDashboard.setVisible(model.getDiagramsDashboardOrNull()!=null);

		if (!running) {
			simulatorLock.acquireUninterruptibly();
			try {
				this.simulator=null;
			} finally {
				simulatorLock.release();
			}
			playPause();
		}

		hasAnalogElements=hasAnalogElements(model);
		hasConveyorElements=hasConveyorElements(model);
		surfacePanel.setSurface(model,model.surface,model.clientData,model.sequences);
		surfaceAnimator=new ModelSurfaceAnimator(window,surfacePanel,model.animationImages,ModelSurfaceAnimator.AnimationMoveMode.MODE_MULTI,setup.useMultiCoreAnimation,setup.animateResources);

		if (recordFile==null) {
			encoder=null;
		} else {
			/* encoder=new VP8System(recordFile,surfaceAnimator.useAdditionalFrames()); */
			encoder=new MJPEGSystem(recordFile,surfaceAnimator.useAdditionalFrames());
			if (!encoder.isReady()) encoder=null;
			if (!startRecordingImmediately) buttonStartRecording.setVisible(true);
		}
		surfaceAnimator.setRecordSystem(encoder,startRecordingImmediately,scaleFrame,paintTimeStamp);

		surfacePanel.getSurface().setAnimatorPanel(this);
		surfacePanel.setZoom(zoom);
		zoomChanged();
		surfacePanel.setRaster(raster);
		rulerPanel.setRulerVisible(showRulers);
		surfacePanel.setColors(model.surfaceColors);
		surfacePanel.setBackgroundImage(model.surfaceBackgroundImage,model.surfaceBackgroundImageScale,model.surfaceBackgroundImageMode);
		setSurfacePosition(position);


		final Timer positionTimer=new Timer("AnimationPanelLayoutTimer",false);
		positionTimer.schedule(new TimerTask() {@Override public void run() {
			positionTimer.cancel();
			SwingUtilities.invokeLater(()->{
				setSurfacePosition(position);
			});
		}},100,5000);

		surfaceAnimator.calcSurfaceSize();
		calculateMinimalTimeStep();

		this.animationDone=animationDone;
		this.sendToSimulation=sendToSimulation;
		buttonSimulation.setVisible(sendToSimulation!=null);
		simData=null;
		simulatorLock.acquireUninterruptibly();
		try {
			this.simulator=simulator;
		} finally {
			simulatorLock.release();
		}
		lastTimeStep=-1;

		this.parentLogger=logger;
		this.logger=logger;
		if (this.parentLogger!=null) this.parentLogger.setCallback(data->loggerCallback(data));
		logTimeStamp=-1;

		int clientCount=(int)(simulator.getCountClients()/1000);
		if (clientCount<=0) {
			progressBar.setVisible(false); /* Abbruch über Bedingung -> kein Fortschrittsbalken */
		} else {
			progressBar.setVisible(true);
			progressBar.setMaximum(Math.max(1,clientCount));
		}

		buttonAbort.setEnabled(true);
		abortRun=false;
		continueAsSimulation=false;
		simulationSuccessful=false;

		/* Ist nötig, damit die kleinen Kundentyp-Icons auf den Stations-Shapes korrekt angezeigt werden. */
		surfacePanel.getSurface().initAfterLoad();

		if (startPaused && !fastWarmUp) {
			surfaceAnimator.setFullRecording(startFullRecording);
			running=true; /* Wird dann sofort von playPause() abgeschaltet, dann gilt also running=false. */
			playPause();
			if (this.logger!=null) this.logger.setActive(true);
			simulator.start(true);
		} else {
			running=true;
			setPlayPauseButtons(true);
			simulator.start(false);
			if (logger!=null && logger.getNextLogger()==null) simulator.pauseLogging();
			timer=new Timer("AnimationCancelCheck",false);
			timer.schedule(new UpdateInfoTask(),100);
		}

		if (startPaused) {
			SwingUtilities.invokeLater(()->{
				GlassInfo.info(this,Language.tr("Animation.GlassHint"),500);
			});
		}
	}

	/**
	 * Liefert den mit dem Panel verbundenen Simulator
	 * @return	Verbundener Simulator
	 */
	public Simulator getSimulator() {
		return simulator;
	}

	/**
	 * Besitzt das Modell Analogwert-Elemente?
	 * @param model	Modell
	 * @return	Liefert <code>true</code>, wenn das Modell Analogwert-Elemente besitzt
	 * @see #hasAnalogElements
	 */
	private boolean hasAnalogElements(final EditModel model) {
		for (ModelElement element: model.surface.getElements()) {
			if (element instanceof ModelElementAnalogValue) return true;
			if (element instanceof ModelElementTank) return true;
			if (element instanceof ModelElementSub) for (ModelElement sub: ((ModelElementSub)element).getSubSurface().getElements()) {
				if (sub instanceof ModelElementAnalogValue) return true;
				if (sub instanceof ModelElementTank) return true;
			}
		}
		return false;
	}

	/**
	 * Besitzt das Modell Fließbänder?
	 * @param model	Modell
	 * @return	Liefert <code>true</code>, wenn das Modell Fließbänder besitzt
	 * @see #hasConveyorElements
	 */
	private boolean hasConveyorElements(final EditModel model) {
		for (ModelElement element: model.surface.getElements()) {
			if (element instanceof ModelElementConveyor) return true;
			if (element instanceof ModelElementSub) for (ModelElement sub: ((ModelElementSub)element).getSubSurface().getElements()) {
				if (sub instanceof ModelElementConveyor) return true;
			}
		}
		return false;
	}

	/**
	 * Gibt es im Modell Elemente, die auf Animationsdaten reagieren (z.B. um die Darstellung zu aktualisieren)?
	 * @param model	Modell
	 * @return	Liefert <code>true</code>, wenn das Modell Elemente besitzt, die auf Animationsdaten reagieren
	 * @see ElementWithAnimationDisplay
	 */
	private boolean hasAnimationListener(final EditModel model) {
		for (ModelElement element: model.surface.getElements()) {
			if (element instanceof ElementWithAnimationDisplay) return true;
			if (element instanceof ModelElementSub) for (ModelElement sub: ((ModelElementSub)element).getSubSurface().getElements()) if (sub instanceof ElementWithAnimationDisplay) return true;
		}
		return false;
	}

	/**
	 * Fügt ein <code>ModelElementAnimationConnect</code>-Element zum Editor-Modell hinzu, um die Verknüpfung
	 * aus der Simulation zu diesem Panel herzustellen.
	 * @param model	Editor-Modell, welches als Animation simuliert werden soll
	 * @return	Gibt <code>true</code> zurück, wenn die Warm-up-Phase bei der Animation zunächst als Simulation vorab ausgeführt werden soll
	 * @see ModelElementAnimationConnect
	 */
	public boolean makeAnimationModel(final EditModel model) {
		/* Verknüpfung zu Animationssystem hinzufügen */
		final ModelElementAnimationConnect animationConnect=new ModelElementAnimationConnect(model,model.surface);
		animationConnect.animationViewer=this;
		model.surface.add(animationConnect);

		/* Umgang mit der Warm-Up-Phase */
		if (model.warmUpTime<=0.0 && model.warmUpTimeTime<=0) return false; /* Kein Warm-Up definiert, also nix zu tun. */

		final SetupData.AnimationMode warmUpMode=setup.animationWarmUpMode;
		boolean skip=false;
		boolean fast=false;

		switch (warmUpMode) {
		case ANIMATION_WARMUP_NORMAL:
			/* alles so lassen */
			break;
		case ANIMATION_WARMUP_ASK:
			if (hasAnimationListener(model)) {
				final List<String> options=new ArrayList<>();
				final List<String> infos=new ArrayList<>();
				options.add(Language.tr("Animation.SkipWarmUp.OptionDefault"));
				options.add(Language.tr("Animation.SkipWarmUp.OptionSkip"));
				options.add(Language.tr("Animation.SkipWarmUp.OptionFast"));
				infos.add(Language.tr("Animation.SkipWarmUp.OptionDefault.Info"));
				infos.add(Language.tr("Animation.SkipWarmUp.OptionSkip.Info"));
				infos.add(Language.tr("Animation.SkipWarmUp.OptionFast.Info"));
				final int result=MsgBox.options(this,Language.tr("Animation.SkipWarmUp.Title"),String.format("<html><body>"+Language.tr("Animation.SkipWarmUp")+"</body></html>",NumberTools.formatLong(FastMath.round(model.warmUpTime*model.clientCount))),options.toArray(String[]::new),infos.toArray(String[]::new));
				switch (result) {
				case 0:	/* nicht ändern */ break;
				case 1: skip=true; break;
				case 2: fast=true; break;
				}
			}
			break;
		case ANIMATION_WARMUP_SKIP:
			if (hasAnimationListener(model)) skip=true;
			break;
		case ANIMATION_WARMUP_FAST:
			fast=true;
			break;
		}

		if (skip) {
			model.warmUpTime=0.0;
			model.warmUpTimeTime=0;
		}
		return fast;
	}

	/**
	 * Beendet die Simulation
	 * @param successful	War der Abschluss erfolgreich?
	 * @see #abortRunTest()
	 * @see #stepInt(boolean, boolean)
	 */
	private void finalizeSimulation(final boolean successful) {
		if (surfaceAnimator==null) return;
		if (simulator==null) return;

		surfaceAnimator.setRecordSystem(null,false,1.0,false);
		simulationSuccessful=successful;
		simulatorLock.acquireUninterruptibly();
		try {
			if (timer!=null) {timer.cancel(); timer=null;}
			if (simulator!=null) simulator.finalizeRun();
			simulator=null;
		} finally {
			simulatorLock.release();
		}
		if (encoder!=null) {
			encoder.done();
			if (encoder instanceof MJPEGSystem) new AnimationRecordWaitDialog(this,(MJPEGSystem)encoder);
			encoder=null;
		}
		surfaceAnimator=null;
		animationTerminated();
		SoundSystem.getInstance().stopSoundFile();
		if (animationDone!=null) SwingUtilities.invokeLater(animationDone);
	}

	/**
	 * Gibt an, ob die Simulation erfolgreich beendet wurde.
	 * @return	Gibt <code>true</code> zurück, wenn die Simulation erfolgreich beendet wurde
	 */
	public boolean isSimulationSuccessful() {
		return simulationSuccessful;
	}

	/**
	 * Zeitpunkt der letzten Statuszeilen-Aktualisierung
	 * @see #updateStatus(long, boolean)
	 */
	private long lastStatusUpdate;

	/**
	 * Simulationszeit der letzten Statuszeilen-Aktualisierung
	 * @see #updateStatus(long, boolean)
	 */
	private long lastStatusSimTime;

	/**
	 * Anzahl an bislang simulierten Kundenankünften beim letzten Aufruf von {@link #updateStatus(long, boolean)}
	 * @see #updateStatus(long, boolean)
	 */
	private long lastStatusCurrent;

	/**
	 * Letzter Zeichenkettenwert für die bislang simulierten Kundenankünfte
	 * @see #lastStatusCurrent
	 * @see #updateStatus(long, boolean)
	 */
	private String lastStatusCurrentString;

	/**
	 * Gesamtanzahl an zu simulierenden Kundenankünften beim letzten Aufruf von {@link #updateStatus(long, boolean)}
	 * @see #updateStatus(long, boolean)
	 */
	private long lastStatusSum;

	/**
	 * Letzter Zeichenkettenwert für die Gesamtanzahl an zu simulierenden Kundenankünften
	 * @see #updateStatus(long, boolean)
	 */
	private String lastStatusSumString;

	/**
	 * Aktualisiert die Anzeige in der Statuszeile
	 * @param currentTime	Aktuelle Simulationszeit
	 * @param forceUpdate	Aktualisierung erzwingen? (Sonst erfolgt eine Aktualisierung nur, wenn der zeitliche Abstand (in simulierter Zeit gemessen) zwischen letztem Update und aktuellem Wert groß genug ist)
	 */
	private void updateStatus(final long currentTime, final boolean forceUpdate) {
		final long time=System.currentTimeMillis();
		if (time-lastStatusUpdate<20 && (running || (lastStatusSimTime==currentTime)) && !forceUpdate) return;
		lastStatusUpdate=time;
		lastStatusSimTime=currentTime;

		long current=0;
		long sum=0;
		if (simulator!=null) {
			current=simulator.getCurrentClients();
			sum=simulator.getCountClients();
		}

		if (lastStatusCurrent!=current || lastStatusCurrentString==null) lastStatusCurrentString=NumberTools.formatLong(current);
		if (sum>=0) {
			if (lastStatusSum!=sum || lastStatusSumString==null) lastStatusSumString=NumberTools.formatLong(sum);
		}

		final String recordStatus;
		if (encoder!=null) {
			recordStatus=String.format(infoMJPEG,NumberTools.formatLong(encoder.getFrameCount()),NumberTools.formatLong(encoder.getBytesCount()/1024));
		} else {
			recordStatus="";

		}
		final String currentTimeString=TimeTools.formatLongTime(currentTime*simData.runModel.scaleToSeconds);
		if (sum<=0) {
			statusBar.setText(String.format(infoNoSum,currentTimeString,lastStatusCurrentString,recordStatus));
		} else {
			statusBar.setText(String.format(infoSum,currentTimeString,lastStatusCurrentString,lastStatusSumString,NumberTools.formatPercent(((double)current)/sum,0),recordStatus));
			progressBar.setValue((int)(current/1000));
		}
	}

	/**
	 * Führt die Animation und die Verzögerungen aus.
	 * @param simData	Simulationsdatenobjekt
	 * @param timeStepDelay	Verzögerungswert (0..25)
	 */
	private void delaySystem(final SimulationData simData, int timeStepDelay) {
		if (lastTimeStep>0) {
			double seconds=(simData.currentTime-lastTimeStep)*simData.runModel.scaleToSeconds;

			double d=(delaySystem>0)?(0.04/delaySystem):0.2;
			d=FastMath.max(d,0.1);

			int steps;
			long delayMS;
			if (delay>100) {
				delayMS=100;
				steps=(int)Math.round(seconds*10);
				if (steps<1) {
					steps=1;
					delayMS=Math.round(seconds*1000);
				}
			} else {
				if (timeStepDelay>10) timeStepDelay=(int)Math.pow(timeStepDelay,1.2); /* FastMath.pow(...) würde hier new double[2] durchführen! */
				delayMS=FastMath.round(d*timeStepDelay*seconds);
				steps=(int)FastMath.round(delayMS/50.0);
				if (steps<1) steps=1;
				if (steps>40) steps=40;
				delayMS=FastMath.min(FastMath.round(FastMath.sqrt(100.0*delayMS/steps)/100),500);
				if (timeStepDelay>10) delayMS=FastMath.max(delay,timeStepDelay/2);
				if (delayMS>0) delayMS=FastMath.max(delayMS,10);
			}

			final long save_currentTime=simData.currentTime;
			for (int i=1;i<=steps;i++) {
				try {
					final long stepTime=lastTimeStep+i*(save_currentTime-lastTimeStep)/steps;
					simData.currentTime=stepTime;
					updateStatus(stepTime,false);
					surfaceAnimator.updateSurfaceAnimationDisplayElements(simData,true,false);
					Thread.sleep(delayMS);
					if (speedChanged) break;
				} catch (InterruptedException e) {Thread.currentThread().interrupt(); return;}
			}
			simData.currentTime=save_currentTime;
		}
		speedChanged=false;
		lastTimeStep=simData.currentTime;
		updateStatus(simData.currentTime,false);
	}

	@Override
	public boolean updateViewer(final SimulationData simData) {
		synchronized(subViewers) {
			if (subViewers.size()>0) { /* Andernfalls würde auch bei einer leeren SubViewer-Liste jedes Mal ein Iterator erstellt */
				for (RunModelAnimationViewer subViewer: subViewers) subViewer.updateViewer(simData);
			}
		}
		return updateViewer(simData,null,false);
	}

	/**
	 * Letzter Zeitpunkt (in System-Millisekunden) an dem {@link #updateViewer(SimulationData, RunDataClient, boolean)}
	 * aufgerufen wurde.
	 * @see #updateViewer(SimulationData, RunDataClient, boolean)
	 */
	private long lastUpdateStep=0;

	@Override
	public boolean updateViewer(SimulationData simData, final RunDataClient client, final boolean moveByTransport) {
		if (abortRun) return false;
		if (continueAsSimulation) return false;
		if (surfaceAnimator==null) return true;

		surfaceAnimator.setSlowMode(running && delayInt>0 && (hasAnalogElements || hasConveyorElements) && setup.useSlowModeAnimation);

		synchronized(subViewers) {
			if (subViewers.size()>0) { /* Andernfalls würde auch bei einer leeren SubViewer-Liste jedes Mal ein Iterator erstellt */
				for (RunModelAnimationViewer subViewer: subViewers) subViewer.updateViewer(simData,client,moveByTransport);
			}
		}

		surfacePanel.setAnimationSimulationData(simData,surfaceAnimator);

		if (running) {
			if (surfaceAnimator.testBreakPoints(simData,client)) {
				playPause();
				surfaceAnimator.updateSurfaceAnimationDisplayElements(simData,true,false);
				if (!moveByTransport) surfaceAnimator.process(simData,client,FastMath.min(20,delayInt/4));
				surfacePanel.repaint();
			}
		}

		final long currentTime=System.currentTimeMillis();
		if (currentTime<=lastUpdateStep+5 && delayInt==0 && encoder==null) {
			surfaceAnimator.updateSurfaceAnimationDisplayElements(simData,false,true);
			return true;
		}
		lastUpdateStep=currentTime;

		mutex.acquireUninterruptibly();
		try {
			if (simData==null) simData=this.simData;
			if (simData==null) return true;
			if (simData.runData.isWarmUp && fastWarmUp) return true;
			this.simData=simData;
			if (this.simData.pauseAnimationCallback==null) this.simData.pauseAnimationCallback=()->{
				if (setup.respectPauseCommand && isRunning()) playPause();
			};
			if (logger==null || !logger.isActive()) {
				delaySystem(simData,delayInt/4); /* Verzögerungen von einem Ereignis zum nächsten ausschalten im Einzelschrittmodus. */
			} else {
				updateStatus(simData.currentTime,false); /* Aber Statuszeile muss aktualisiert werden. (Passiert sonst in delaySystem.) */
			}
			if (!moveByTransport) surfaceAnimator.process(simData,client,FastMath.min(20,delayInt/4));
			surfacePanel.repaint(); /* Wichtig, sonst wird im Einzelschrittmodus der letzte Schritt nicht korrekt dargestellt (und Zahlenwerte an den Stationen stimmen nicht!) */
		} finally {mutex.release();}

		if (startPaused && fastWarmUp) {
			startPaused=false;
			playPause();
			surfacePanel.repaint();
		}

		return true;
	}

	@Override
	public boolean updateViewer(SimulationData simData, final RunDataTransporter transporter) {
		if (abortRun) return false;
		if (continueAsSimulation) return false;

		surfaceAnimator.setSlowMode(running && delayInt>0);

		synchronized(subViewers) {
			if (subViewers.size()>0) { /* Andernfalls würde auch bei einer leeren SubViewer-Liste jedes Mal ein Iterator erstellt */
				for (RunModelAnimationViewer subViewer: subViewers) subViewer.updateViewer(simData,transporter);
			}
		}

		surfacePanel.setAnimationSimulationData(simData,surfaceAnimator);

		long currentTime=System.currentTimeMillis();
		if (currentTime<=lastUpdateStep+5 && delayInt==0 && encoder==null) {
			surfaceAnimator.updateSurfaceAnimationDisplayElements(simData,false,true);
			return true;
		}
		lastUpdateStep=currentTime;

		mutex.acquireUninterruptibly();
		try {
			if (simData==null) simData=this.simData;
			if (simData==null) return true;
			if (simData.runData.isWarmUp && fastWarmUp) return true;
			this.simData=simData;
			if (logger==null || !logger.isActive()) {
				delaySystem(simData,delayInt/4); /* Verzögerungen von einem Ereignis zum nächsten ausschalten im Einzelschrittmodus. */
			} else {
				updateStatus(simData.currentTime,false); /* Aber Statuszeile muss aktualisiert werden. (Passiert sonst in delaySystem.) */
			}
			surfaceAnimator.process(simData,transporter,FastMath.min(20,delayInt/4));
		} finally {
			mutex.release();
		}
		return true;
	}

	/**
	 * Liefert das Simulationsdatenobjekt (sofern ein solches bereits eingestellt ist).
	 * @return	Simulationsdatenobjekt
	 */
	public SimulationData getSimData() {
		return simData;
	}

	/**
	 * Teil dem Panel mit, dass es geschlossen werden soll.<br>
	 * Dadurch wird ggf. der Abbruch der Simulation ausgelöst.
	 */
	public void closeRequest() {
		abortRun=true;
		if (!running) abortRunTest();
	}

	/**
	 * Bricht die Simulation ab, wenn {@link #abortRun} gesetzt ist.
	 * Reagiert außerdem, wenn sich der Simulator beendet hat.
	 * @return	Liefert <code>true</code>, wenn die Simulation noch läuft
	 */
	private boolean abortRunTest() {
		if (abortRun) {
			if (simulator!=null) {
				UsageStatistics.getInstance().addSimulationClients(simulator.getCurrentClients());
				simulator.cancel();
			}
		}

		if (abortRun || simulator==null || !simulator.isRunning()) {
			mutex.acquireUninterruptibly();
			try {
				if (abortRun || simulator==null || !setup.animationFinishPaused) {
					finalizeSimulation(!abortRun);
				} else {
					playPause();
				}
			} finally {mutex.release();}
			return false;
		}
		return true;
	}

	/**
	 * Prüft in regelmäßigen Abständen ob die Simulation abgebrochen wurde.
	 * @see AnimationPanel#abortRunTest()
	 */
	private class UpdateInfoTask extends TimerTask {
		/**
		 * Konstruktor der Klasse
		 */
		public UpdateInfoTask() {
			/*
			 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
			 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public void run() {
			if (abortRunTest())	{
				simulatorLock.acquireUninterruptibly();
				try {
					if (timer!=null)
						timer.schedule(new UpdateInfoTask(),100);
				} finally {
					simulatorLock.release();
				}
			}
		}
	}

	/**
	 * Wird aufgerufen, wenn die Animationsgeschwindigkeit geändert wurde.
	 * @see #delay
	 * @see #speedChanged
	 */
	private void animationDelayChanged() {
		speedChanged=true;
		if (delay/10!=setup.animationDelay) {
			setup.animationDelay=delay/10;
			setup.saveSetup();
		}
	}

	/**
	 * Aktualisiert die Zoomfaktoranzeige, wenn sich der Zoomfaktor geändert hat.
	 * @see #labelZoom
	 */
	private void zoomChanged() {
		labelZoom.setText(FastMath.round(100*surfacePanel.getZoom())+"% ");
	}

	/**
	 * Verringert den Zoomfaktor.
	 */
	public void zoomOut() {
		if (surfacePanel==null) return;
		surfacePanel.zoomOut();
		labelZoom.setText(Math.round(100*surfacePanel.getZoom())+"% ");
	}

	/**
	 * Vergrößert den Zoomfaktor.
	 */
	public void zoomIn() {
		if (surfacePanel==null) return;
		surfacePanel.zoomIn();
		labelZoom.setText(Math.round(100*surfacePanel.getZoom())+"% ");
	}

	/**
	 * Stellt den Standard-Zoomfaktor wieder her.
	 */
	public void zoomDefault() {
		if (surfacePanel==null) return;
		surfacePanel.zoomDefault();
		labelZoom.setText(Math.round(100*surfacePanel.getZoom())+"% ");
	}

	/**
	 * Liefert den aktuell eingestellten Zoomfaktor
	 * @return Aktueller Zoomfaktor
	 */
	public double getZoom() {
		return surfacePanel.getZoom();
	}

	/**
	 * Zentriert das Modell auf der Zeichenfläche.
	 */
	public void centerModel() {
		if (surfacePanel==null) return;
		surfacePanel.centerModel();
	}

	/**
	 * Scrollt ganz nach oben links.
	 */
	public void scrollToTop() {
		if (surfacePanel==null) return;
		surfacePanel.scrollToTop();
	}

	/**
	 * Liefert die Position der linken oberen Ecke des sichtbaren Bereichs
	 * @return	Position der linken oberen Ecke des sichtbaren Bereichs
	 */
	public Point getTopPosition() {
		if (!(surfacePanel.getParent() instanceof JViewport)) return new Point(0,0);
		return ((JViewport)surfacePanel.getParent()).getViewPosition();
	}

	/**
	 * Zeigt das Kontextmenü zur Auswahl des Zoomfaktors an.
	 * @param parent	Übergeordnetes Element zur Ausrichtung des Popupmenüs.
	 * @see #labelZoom
	 */
	private void showZoomContextMenu(final Component parent) {
		final JPopupMenu popup=new JPopupMenu();

		final int value=Math.max(1,Math.min(20,(int)Math.round(surfacePanel.getZoom()*5)));

		final JSlider slider=new JSlider(SwingConstants.VERTICAL,1,20,value);
		slider.setMinorTickSpacing(1);
		slider.setPaintTicks(true);
		final Dictionary<Integer,JComponent> labels=new Hashtable<>();
		labels.put(1,new JLabel("20%"));
		labels.put(2,new JLabel("40%"));
		labels.put(3,new JLabel("60%"));
		labels.put(4,new JLabel("80%"));
		labels.put(5,new JLabel("100%"));
		labels.put(6,new JLabel("120%"));
		labels.put(10,new JLabel("200%"));
		labels.put(15,new JLabel("300%"));
		labels.put(20,new JLabel("400%"));
		slider.setLabelTable(labels);
		slider.setPaintLabels(true);
		slider.setValue(value);
		slider.addChangeListener(e->{surfacePanel.setZoom(slider.getValue()/5.0); zoomChanged();});
		slider.setPreferredSize(new Dimension(slider.getPreferredSize().width,350));

		popup.add(slider);

		popup.show(parent,0,-popup.getPreferredSize().height);
	}

	/**
	 * Zeigt das Kontextmenü zur Auswahl der gespeicherten Ansichten an.
	 * @param parent	Übergeordnetes Element zur Ausrichtung des Popupmenüs.
	 * @see #buttonViews
	 */
	public void showViewPopup(final Component parent) {
		final JPopupMenu menu=new JPopupMenu();
		JMenuItem item;

		final List<SavedViews.SavedView> views=model.savedViews.getViews();

		/* Ansicht laden */
		for (SavedViews.SavedView view: views) {
			menu.add(item=new JMenuItem(view.getName(),Images.ZOOM_VIEW.getIcon()));
			item.addActionListener(e->view.set(surfacePanel));
		}

		if (views.size()==0) {
			menu.add(item=new JMenuItem(Language.tr("Editor.SavedViews.NoSavedViews")));
			item.setEnabled(false);
		}

		final Dimension size=menu.getPreferredSize();
		menu.show(parent,20-size.width,-size.height);
	}

	/**
	 * Zeigt den Diagramme-Dashboard-Dialog an.
	 */
	private void showDashboard() {
		final ModelElementDashboard dashboard=model.getDiagramsDashboardOrNull();
		if (dashboard==null) return;
		dashboard.showSubEditDialog(AnimationPanel.this,true,false);
	}

	/**
	 * Wechselt die gespeicherte Ansicht.
	 * @param delta	Verschiebung der aktiven Ansicht nach oben (-1) oder unten (1)
	 */
	private void savedViewSelect(final int delta) {
		final List<SavedViews.SavedView> views=model.savedViews.getViews();
		if (views.size()==0) return;

		int selected=-1;
		for (int i=0;i<views.size();i++) if (views.get(i).isSelected()) {selected=i; break;}
		if (selected<0) {
			selected=0;
		} else {
			selected+=delta;
			if (selected<0) selected=views.size()-1;
			if (selected>=views.size()) selected=0;
		}
		final SavedViews.SavedView selectedView=views.get(selected);
		model.savedViews.setSelected(selectedView);
		selectedView.set(surfacePanel);
	}

	/**
	 * Zeigt den Modelleigenschaften-Dialog (im Nur-Lese-Modus) an.
	 * @param initialPage	Beim Aufruf des Dialogs anzuzeigende Seite (darf <code>null</code> sein)
	 * @see ModelPropertiesDialog
	 */
	private void showModelPropertiesDialog(final ModelPropertiesDialog.InitialPage initialPage) {
		if (model==null) return;
		final ModelPropertiesDialog dialog=new ModelPropertiesDialog(this,model,true,initialPage);
		dialog.setVisible(true);
	}

	/**
	 * Zeigt das Popupmenü zur Einstellung der Simulationsgeschwindigkeit an.
	 * @see #buttonSpeed
	 */
	private void animationSpeedPopup() {
		final JPopupMenu popup=new JPopupMenu();

		final JSlider slider=new JSlider(SwingConstants.VERTICAL,0,11,6);
		slider.setMinorTickSpacing(1);
		slider.setPaintTicks(true);
		final Dictionary<Integer,JComponent> labels=new Hashtable<>();
		labels.put(11,new JLabel(Language.tr("Animation.Toolbar.Speed.Maximal")));
		labels.put(10, new JLabel(Language.tr("Animation.Toolbar.Speed.Fast")));
		labels.put(7,new JLabel(Language.tr("Animation.Toolbar.Speed.Normal")));
		labels.put(1,new JLabel(Language.tr("Animation.Toolbar.Speed.Slow")));
		labels.put(0,new JLabel(Language.tr("Animation.Toolbar.Speed.RealTime")));
		slider.setLabelTable(labels);
		slider.setPaintLabels(true);
		slider.setValue(FastMath.min(11,FastMath.max(0,11-delay/10)));
		slider.addChangeListener(e->{
			delay=(11-slider.getValue())*10;
			delayInt=delay;
			animationDelayChanged();
		});

		popup.add(slider);

		popup.show(buttonSpeed,0,buttonSpeed.getHeight());
	}

	/**
	 * Zeigt das Popupmenü zur Konfiguration der Animation an.
	 * @see #buttonTools
	 */
	private void animationToolsPopup() {
		final JPopupMenu popup=new JPopupMenu();

		JMenuItem submenu;
		ButtonGroup buttonGroup;

		popup.add(submenu=new JMenu(Language.tr("Main.Menu.AnimationStartMode")));
		submenu.add(menuStartModeRun=new JRadioButtonMenuItem(Language.tr("Main.Menu.AnimationStartMode.Run")));
		menuStartModeRun.addActionListener(new ToolBarListener());
		submenu.add(menuStartModePause=new JRadioButtonMenuItem(Language.tr("Main.Menu.AnimationStartMode.Pause")));
		menuStartModePause.addActionListener(new ToolBarListener());
		buttonGroup=new ButtonGroup();
		buttonGroup.add(menuStartModeRun);
		buttonGroup.add(menuStartModePause);
		menuStartModeRun.setSelected(!setup.animationStartPaused);
		menuStartModePause.setSelected(setup.animationStartPaused);

		popup.add(submenu=new JMenu(Language.tr("Main.Menu.AnimationFinishMode")));
		submenu.add(menuFinishModeRun=new JRadioButtonMenuItem(Language.tr("Main.Menu.AnimationFinishMode.Run")));
		menuFinishModeRun.addActionListener(new ToolBarListener());
		submenu.add(menuFinishModePause=new JRadioButtonMenuItem(Language.tr("Main.Menu.AnimationFinishMode.Pause")));
		menuFinishModePause.addActionListener(new ToolBarListener());
		buttonGroup=new ButtonGroup();
		buttonGroup.add(menuFinishModeRun);
		buttonGroup.add(menuFinishModePause);
		menuFinishModeRun.setSelected(!setup.animationFinishPaused);
		menuFinishModePause.setSelected(setup.animationFinishPaused);

		popup.add(submenu=new JMenu(Language.tr("Main.Menu.AnalogValues")));
		submenu.add(menuAnalogValuesFast=new JRadioButtonMenuItem(Language.tr("Main.Menu.AnalogValues.Fast")));
		menuAnalogValuesFast.addActionListener(new ToolBarListener());
		submenu.add(menuAnalogValuesExact=new JRadioButtonMenuItem(Language.tr("Main.Menu.AnalogValues.Exact")));
		menuAnalogValuesExact.addActionListener(new ToolBarListener());
		buttonGroup=new ButtonGroup();
		buttonGroup.add(menuAnalogValuesFast);
		buttonGroup.add(menuAnalogValuesExact);
		menuAnalogValuesFast.setSelected(!setup.useSlowModeAnimation);
		menuAnalogValuesExact.setSelected(setup.useSlowModeAnimation);

		if (!SetupBase.memoryOnly) {
			popup.add(submenu=new JMenu(Language.tr("Main.Menu.AnimationScreenshotMode")));
			submenu.add(menuScreenshotModeHome=new JRadioButtonMenuItem(Language.tr("Main.Menu.AnimationScreenshotMode.Home")+" ("+FileSystemView.getFileSystemView().getHomeDirectory()+")"));
			menuScreenshotModeHome.addActionListener(new ToolBarListener());
			String custom="";
			if (setup.imagePathAnimation!=null && !setup.imagePathAnimation.isBlank()) custom=" ("+setup.imagePathAnimation.trim()+")";
			submenu.add(menuScreenshotModeCustom=new JRadioButtonMenuItem(Language.tr("Main.Menu.AnimationScreenshotMode.Custom")+custom));
			menuScreenshotModeCustom.addActionListener(new ToolBarListener());
			buttonGroup=new ButtonGroup();
			buttonGroup.add(menuScreenshotModeHome);
			buttonGroup.add(menuScreenshotModeCustom);
			menuScreenshotModeHome.setSelected(setup.imagePathAnimation==null || setup.imagePathAnimation.isBlank());
			menuScreenshotModeCustom.setSelected(setup.imagePathAnimation!=null && !setup.imagePathAnimation.isBlank());
		}

		popup.add(menuShowLog=new JCheckBoxMenuItem(Language.tr("SettingsDialog.Tabs.Simulation.ShowSingleStepLogData")));
		menuShowLog.addActionListener(new ToolBarListener());
		menuShowLog.setSelected(setup.showSingleStepLogData);

		popup.add(menuRespectPauseCommand=new JCheckBoxMenuItem(Language.tr("SettingsDialog.Tabs.Simulation.RespectPauseCommand")));
		menuRespectPauseCommand.addActionListener(new ToolBarListener());
		menuRespectPauseCommand.setSelected(setup.respectPauseCommand);

		popup.show(buttonTools,0,buttonTools.getHeight());
	}

	/**
	 * Gibt an, ob die Animation momentan läuft.
	 * @return	Läuft die Animation momentan?
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * Befehl: Animation anhalten oder fortsetzen
	 * @see #buttonPlayPause
	 */
	public void playPause() {
		/* Testen: Wurde die Simulation an ihrem Ende angehalten und hat der Nutzer nun auf "Play" geklickt, was natürlich nicht mehr bringt? */
		if (!running && (abortRun || simulator==null || !simulator.isRunning())) {
			finalizeSimulation(true);
			return;
		}

		simulatorLock.acquireUninterruptibly();
		try {
			if (running) {
				/* Pause */
				running=false;
				if (simulator!=null) simulator.pauseExecution();

				if (timer!=null) {timer.cancel(); timer=null;}

				/* Wichtig, weil sonst das Modell intern weiter sein kann, als die Anzeige. Die Anzeige wird dann beim nächste regulären Repaint aktualisiert, was zu Verwirrungen führen kann. */
				if (simData!=null) {
					if (surfaceAnimator!=null) surfaceAnimator.process(simData,(RunDataClient)null,0);
					updateStatus(simData.currentTime,true);
					repaint();
				}
			} else {
				/* Play */
				if (surfaceAnimator!=null) surfaceAnimator.setFullRecording(false);
				if (logger!=null) {
					logger.setActive(false);
					if (simulator!=null && logger.getNextLogger()==null) simulator.pauseLogging();
				}
				delayInt=delay;
				running=true;
				if (simulator!=null) simulator.resumeExecution();

				timer=new Timer("AnimationCancelCheck",false);
				timer.schedule(new UpdateInfoTask(),100);
			}
			setPlayPauseButtons(running);
		} finally {
			simulatorLock.release();
		}
	}

	/**
	 * Konfiguriert nur die Schaltflächen, ohne dabei die
	 * Animation tatsächlich anzuhalten oder zu starten.
	 * @see #playPause()
	 * @param play	Schaltflächen auf Modus Play (<code>true</code>) oder Pause (<code>false</code>) setzen
	 */
	public void setPlayPauseButtons(final boolean play) {
		if (!play) {
			/* Pause */
			buttonExport.setEnabled(true);
			buttonStep.setEnabled(true);
			buttonPlayPause.setText(Language.tr("Animation.Toolbar.Play"));
			buttonPlayPause.setToolTipText(Language.tr("Animation.Toolbar.Play.Info")+" ("+keyStrokeToString(KeyStroke.getKeyStroke(KeyEvent.VK_F6,0))+")");
			buttonPlayPause.setIcon(Images.ANIMATION_PLAY.getIcon());
			buttonCurrentStatistics.setEnabled(true);
			buttonBreakpoints.setEnabled(true);
		} else {
			/* Play */
			logArea.setVisible(false);
			buttonExport.setEnabled(false);
			buttonStep.setEnabled(false);
			buttonPlayPause.setText(Language.tr("Animation.Toolbar.Pause"));
			buttonPlayPause.setToolTipText(Language.tr("Animation.Toolbar.Pause.Info")+" ("+keyStrokeToString(KeyStroke.getKeyStroke(KeyEvent.VK_F6,0))+")");
			buttonPlayPause.setIcon(Images.ANIMATION_PAUSE.getIcon());
			buttonCurrentStatistics.setEnabled(false);
			buttonBreakpoints.setEnabled(false);
		}
	}

	/**
	 * Wurden im letzten Zeitschritt Logging-Daten ergänzt?
	 * @see #multiSingleCoreSteps(boolean)
	 * @see #stepInt(boolean, boolean)
	 * @see #loggerCallback(CallbackLoggerData)
	 */
	private volatile boolean stepLogChanged;

	/**
	 * Wird von {@link #step(boolean)} verwendet,
	 * um sicher zu stellen, dass der vorherige Update-Thread
	 * abgeschlossen ist, bevor der nächste startet.
	 * @see #step(boolean)
	 */
	private Semaphore stepLock=new Semaphore(1);

	/**
	 * Führt einen Animationsschritt im Single-Core-Modus aus.
	 * @param fullRecording	Modus zur vollständigen Erfassung der Animationsdaten
	 * @see #step(boolean)
	 */
	private void multiSingleCoreSteps(final boolean fullRecording) {
		stepInt(false,fullRecording);
		SwingUtilities.invokeLater(()->{
			if (!stepLogChanged && simulator!=null) multiSingleCoreSteps(fullRecording);
		});
	}

	/**
	 * Führt einen Animationsschritt aus.
	 * @param fullRecording	Modus zur vollständigen Erfassung der Animationsdaten.
	 * @see ModelSurfaceAnimatorBase#getAnimationStepInfo(long, simulator.runmodel.RunModel, List, List)
	 */
	public void step(final boolean fullRecording) {
		if (!buttonStep.isEnabled()) return; /* Der Hotkey funktioniert auch, wenn der Button deaktiviert ist. Daher müssen wir das explizit abfangen. */

		final boolean multiCore=setup.useMultiCoreAnimation;

		if (!multiCore) {
			multiSingleCoreSteps(fullRecording);
			/* hier kein "updateViewer(simData);", sonst besteht die Gefahr von Deadlocks */
		} else {
			if (!stepLock.tryAcquire()) return;
			new Thread(()->{
				try {
					stepInt(true,fullRecording);
					updateViewer(simData);
				} finally {
					stepLock.release();
				}
			},"AnimationStepper").start();
		}
	}

	/**
	 * Führt einen Animationsschritt aus.
	 * @param multiCore	Weiteren CPU-Kern für die Animation verwenden (<code>true</code>)?
	 * @param fullRecording	Modus zur vollständigen Erfassung der Animationsdaten
	 * @see #step(boolean)
	 * @see #multiSingleCoreSteps(boolean)
	 */
	private void stepInt(final boolean multiCore, final boolean fullRecording) {
		if (logger!=null) {
			logger.setActive(true);
			simulatorLock.acquireUninterruptibly();
			try {
				if (simulator!=null && logger.getNextLogger()==null) simulator.continueLogging();
			} finally {
				simulatorLock.release();
			}
		}
		if (surfaceAnimator==null) return;
		surfaceAnimator.setFullRecording(fullRecording);
		surfaceAnimator.setSlowMode(false);
		delayInt=100;
		stepLogChanged=false;

		boolean finalize=false;
		while (!stepLogChanged && simulator!=null) { /* So viele Schritte ausführen, bis es einen neuen Log-Eintrag gibt (um nicht für folgenlose Recheck-Events zu stoppen). */
			simulatorLock.acquireUninterruptibly();
			try {
				if (simulator!=null) simulator.stepExecution(multiCore);
				if (simulator==null || !simulator.isRunning()) {finalize=true; break;}
			} finally {
				simulatorLock.release();
			}
			if (!multiCore) break;
		}
		if (finalize) finalizeSimulation(true);
	}

	/**
	 * Verändert die Geschwindigkeit der Animation.
	 * @param delta	"+1" für schneller und "-1" für lansgamer
	 */
	private void speed(final int delta) {
		int value=FastMath.min(11,FastMath.max(0,11-delay/10+delta));
		delay=(11-value)*10;
		delayInt=delay;
		animationDelayChanged();
	}

	/**
	 * Befehl: Simulation ohne Animationsausgabe zu Ende führen
	 * @see #continueAsSimulation
	 */
	private void finishAsSimulation() {
		continueAsSimulation=true;
		if (!running) playPause();

		if (running) { /* Wenn die Animation eigentlich schon zu Ende ist, läuft sich durch playPause() nicht mehr an, dann müssen/können die folgenden beiden Zeile nicht mehr ausgeführt werden. */
			surfaceAnimator.setRecordSystem(null,false,1.0,false);
			if (timer!=null) timer.cancel();
		}
		simulationSuccessful=false;
		if (encoder!=null) {
			encoder.done();
			if (encoder instanceof MJPEGSystem) new AnimationRecordWaitDialog(this,(MJPEGSystem)encoder);
			encoder=null;
		}

		/* Marker, dass es sich um eine Animation handelt (mit der interagiert werden kann) abschalten */
		simulator.getRunModel().isAnimation=false;

		animationTerminated();
		if (animationDone!=null) SwingUtilities.invokeLater(animationDone);
		if (sendToSimulation!=null) SwingUtilities.invokeLater(sendToSimulation);
	}

	/**
	 * Ermittelt den in einem Verzeichnis nächsten verfügbaren Dateinamen für einen Screenshot
	 * @param path	Verzeichnis in dem der Screenshot abgelegt werden soll
	 * @return	Verfügbarer Dateiname
	 * @see #saveScreenshot()
	 */
	private File getNextScreenshotFile(final String path) {
		final File folder;
		if (path==null || path.isBlank()) {
			folder=FileSystemView.getFileSystemView().getHomeDirectory();
		} else {
			folder=new File(path);
		}
		if (!folder.isDirectory()) return null;
		int nr=0;
		File file=null;
		while (nr==0 || (file!=null && file.exists())) {
			nr++;
			file=new File(folder,"Animation-"+nr+".png");
		}
		return file;
	}

	/**
	 * Befehl: Screenshot des aktuellen Modellzustands aufnehmen
	 * @see #buttonScreenshot
	 */
	private void saveScreenshot() {
		final File file=getNextScreenshotFile(setup.imagePathAnimation);
		if (file==null) return;
		surfacePanel.saveImageToFile(file,"png",setup.imageSize,setup.imageSize);
	}

	/**
	 * Zeigt den Modell-Exportieren-Dialog an.
	 * @param parent	Übergeordnetes Element
	 * @param title	Titel des Dateiauswahldialogs
	 * @return	Liefert im Erfolgsfall die gewählte Datei, sonst <code>null</code>
	 */
	private File showExportDialog(Component parent, final String title) {
		final JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		fc.setDialogTitle(title);
		final FileFilter jpg=new FileNameExtensionFilter(Language.tr("FileType.jpeg")+" (*.jpg, *.jpeg)","jpg","jpeg");
		final FileFilter gif=new FileNameExtensionFilter(Language.tr("FileType.gif")+" (*.gif)","gif");
		final FileFilter png=new FileNameExtensionFilter(Language.tr("FileType.png")+" (*.png)","png");
		final FileFilter bmp=new FileNameExtensionFilter(Language.tr("FileType.bmp")+" (*.bmp)","bmp");
		final FileFilter tiff=new FileNameExtensionFilter(Language.tr("FileType.tiff")+" (*.tiff, *.tif)","tiff","tif");
		final FileFilter pdf=new FileNameExtensionFilter(Language.tr("FileType.PDF")+" (*.pdf)","pdf");
		fc.addChoosableFileFilter(png);
		fc.addChoosableFileFilter(jpg);
		fc.addChoosableFileFilter(gif);
		fc.addChoosableFileFilter(bmp);
		fc.addChoosableFileFilter(tiff);
		fc.addChoosableFileFilter(pdf);

		fc.setFileFilter(png);
		fc.setAcceptAllFileFilterUsed(false);

		if (fc.showSaveDialog(parent)!=JFileChooser.APPROVE_OPTION) return null;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();

		if (file.getName().indexOf('.')<0) {
			if (fc.getFileFilter()==jpg) file=new File(file.getAbsoluteFile()+".jpg");
			if (fc.getFileFilter()==gif) file=new File(file.getAbsoluteFile()+".gif");
			if (fc.getFileFilter()==png) file=new File(file.getAbsoluteFile()+".png");
			if (fc.getFileFilter()==bmp) file=new File(file.getAbsoluteFile()+".bmp");
			if (fc.getFileFilter()==tiff) file=new File(file.getAbsoluteFile()+".tiff");
			if (fc.getFileFilter()==pdf) file=new File(file.getAbsoluteFile()+".pdf");
		}

		return file;
	}

	/**
	 * Befehl: Screenshot des aktuellen Modellzustands aufnehmen - dafür Dateiname explizit wählen
	 */
	private void saveScreenshotSelectFile() {
		final Window window=SwingUtilities.getWindowAncestor(getParent());

		final File file=showExportDialog(getParent(),Language.tr("Editor.ExportModel"));
		if (file==null) return;

		if (file.exists()) {
			if (!MsgBox.confirmOverwrite(window,file)) return;
		}

		String format="png";
		if (file.getName().toLowerCase().endsWith(".jpg")) format="jpg";
		if (file.getName().toLowerCase().endsWith(".jpeg")) format="jpg";
		if (file.getName().toLowerCase().endsWith(".gif")) format="gif";
		if (file.getName().toLowerCase().endsWith(".bmp")) format="bmp";
		if (file.getName().toLowerCase().endsWith(".tiff")) format="tiff";
		if (file.getName().toLowerCase().endsWith(".tif")) format="tiff";
		if (file.getName().toLowerCase().endsWith(".pdf")) format="pdf";

		/* Bild */
		if (!surfacePanel.saveImageToFile(file,format,setup.imageSize,setup.imageSize)) {
			MsgBox.error(window,Language.tr("XML.ExportErrorTitle"),Language.tr("Editor.ExportModel.Error"));
		}
	}

	/**
	 * Erstellt einen Screenshot im aktuellen Animationsstatus und liefert diesen als Bild-Objekt zurück
	 * @return	Screenshot im aktuellen Animationsstatus
	 */
	public BufferedImage getScreenshot() {
		return surfacePanel.getImageMaxSize(-1,-1);
	}

	/**
	 * Maximale Größe für einen Logging-Eintrag
	 * @see #loggerCallback(CallbackLoggerData)
	 */
	private static final int MAX_LOG_VIEWER_SIZE=50_000;

	/**
	 * Maximale Anzahl an Logging-Ausgaben pro Zeitschritt
	 * bevor auf den eigenständigen Aktualisierungs-Thread umgeschaltet wird
	 * @see #loggerCallback(CallbackLoggerData)
	 */
	private static final int MAX_LOG_PER_TIME_STEP=25;

	/**
	 * Minimaler Zeitabstand (in MS) zwischen zwei Logging-Ausgaben
	 * @see LogUpdateThread#setLog(String)
	 */
	private static final int MIN_LOG_TIME_DELTA_MS=500;

	/**
	 * Zeitpunkt der letzten Logging-Aktion
	 * @see #loggerCallback(CallbackLoggerData)
	 * @see #MAX_LOG_PER_TIME_STEP
	 */
	private long currentTimeStamp;

	/**
	 * Anzahl der Logging-Aktionen zum aktuellen Zeitpunkt
	 * @see #loggerCallback(CallbackLoggerData)
	 * @see #MAX_LOG_PER_TIME_STEP
	 */
	private int currentTimeStampLogs;

	/**
	 * Eigenständiger Thread zur Aktualisierung der Logging-Meldung
	 */
	private class LogUpdateThread extends Thread {
		/**
		 * Thread beenden?
		 * @see #done()
		 */
		private boolean done=false;

		/**
		 * Objekt zur Aktivierung des Threads
		 * @see #setLog(String)
		 */
		private Object signal=new Object();

		/**
		 * Anzuzeigende Nachricht
		 * @see #setLog(String)
		 */
		private String message;

		/**
		 * Systemzeit an dem {@link #message} übermittelt wurde
		 * @see #setLog(String)
		 */
		private long time;

		/**
		 * Aktuell in dem Label angezeigte Nachricht
		 */
		private String currentMessage;

		/**
		 * Konstruktor der Klasse
		 */
		public LogUpdateThread() {
			super("Log area updater");
			start();
		}

		@Override
		public void run() {
			while (!done) {
				try {
					synchronized(signal) {
						signal.wait();
					}
					if (done) break;
					String message=null;
					while (true) {
						long time=0;
						synchronized(signal) {
							time=this.time;
							message=this.message;
						}
						long current=System.currentTimeMillis();
						if (current<time) Thread.sleep(time-current);
						synchronized(signal) {
							if (time==this.time) break;
						}
					}
					if (message!=null) {
						if (currentMessage==null || !currentMessage.equals(message)) {
							currentMessage=message;
							final String finalMessage=message;
							SwingUtilities.invokeLater(()->{
								logLabel.setText(finalMessage);
								SwingUtilities.invokeLater(()->{ /* Scrollbalken erst zeitversetzt zum Text aktualisieren, daher later in later. */
									final JScrollBar vertical=logScroll.getVerticalScrollBar();
									vertical.setValue(vertical.getMaximum());
								});
							});
						}
					}
				} catch (InterruptedException e) {}
			}
		}

		/**
		 * Beendet den Thread.
		 */
		public void done() {
			done=true;
			synchronized(signal) {
				signal.notify();
			}
		}

		/**
		 * Stellt eine neue Nachricht ein.
		 * @param message	Neue Nachricht
		 */
		public void setLog(final String message) {
			synchronized(signal) {
				this.message=message;
				this.time=System.currentTimeMillis()+MIN_LOG_TIME_DELTA_MS;
				synchronized(signal) {
					signal.notify();
				}
			}
		}
	}

	/**
	 * Eigenständiger Thread zur Aktualisierung der Logging-Meldung
	 * @see #loggerCallback(CallbackLoggerData)
	 */
	private LogUpdateThread logUpdateThread;

	/**
	 * Erfasst Logging-Daten für die Ausgabe im unteren Fensterbereich
	 * @param data	Logging-Daten
	 */
	private void loggerCallback(final CallbackLoggerData data) {
		stepLogChanged=true;

		if (!setup.showSingleStepLogData) return;

		if (logTextHistory==null) logTextHistory=new ArrayList<>();
		if (logTextHistoryPlain==null) logTextHistoryPlain=new ArrayList<>();
		if (!logArea.isVisible()) {
			logTextHistory.clear();
			logTextHistoryPlain.clear();
		}

		logArea.setVisible(true);

		boolean newMessage=false;
		if (logTimeStamp!=data.timeStamp || logText==null) {
			logText=new StringBuilder();
			logTextPlain=new StringBuilder();
			logTimeStamp=data.timeStamp;
			newMessage=true;
		} else {
			if (logTextPlain.length()<MAX_LOG_VIEWER_SIZE) {
				logText.append("<br>");
				logTextPlain.append("\n");
			}
		}
		final String colorCode;
		if (data.color==null || data.color.equals(Color.BLACK)) {
			colorCode="FFFFFF";
		} else {
			colorCode=Integer.toHexString(data.color.getRed())+Integer.toHexString(data.color.getGreen())+Integer.toHexString(data.color.getBlue());
		}
		if (logTextPlain.length()<MAX_LOG_VIEWER_SIZE) {
			if (data.id>=0) {
				logText.append(data.time+": <b><span style=\"background-color: #"+colorCode+"; color: black;\">&nbsp; "+data.event+" (id="+data.id+")"+" &nbsp;</span></b> "+data.info);
			} else {
				logText.append(data.time+": <b><span style=\"background-color: #"+colorCode+"; color: black;\">&nbsp; "+data.event+" &nbsp;</span></b> "+data.info);
			}
			logTextPlain.append(data.time+": "+data.event+" - "+data.info);
		}
		final String message="<html><body>"+logText.toString()+"</body></html>";
		final String messagePlain=logTextPlain.toString();

		boolean updateLogLabelNow=true;
		if (data.timeStamp==currentTimeStamp) {
			currentTimeStampLogs++;
			if (currentTimeStampLogs>MAX_LOG_PER_TIME_STEP) updateLogLabelNow=false;
		} else {
			currentTimeStamp=data.timeStamp;
			currentTimeStampLogs=1;
		}
		if (updateLogLabelNow) {
			SwingUtilities.invokeLater(()->{
				logLabel.setText(message);
				SwingUtilities.invokeLater(()->{ /* Scrollbalken erst zeitversetzt zum Text aktualisieren, daher later in later. */
					final JScrollBar vertical=logScroll.getVerticalScrollBar();
					vertical.setValue(vertical.getMaximum());
				});
			});
		} else {
			if (logUpdateThread==null) logUpdateThread=new LogUpdateThread();
			logUpdateThread.setLog(message);
		}

		if (newMessage || logTextHistory.isEmpty()) {
			logTextHistory.add(message);
			logTextHistoryPlain.add(messagePlain);
			while (logTextHistory.size()>100) logTextHistory.remove(0);
			while (logTextHistoryPlain.size()>100) logTextHistoryPlain.remove(0);
		} else {
			logTextHistory.set(logTextHistory.size()-1,message);
			logTextHistoryPlain.set(logTextHistoryPlain.size()-1,messagePlain);
		}
		logTextDisplayIndex=logTextHistory.size()-1;

		logPrevious.setEnabled(logTextHistory.size()>1);
		logNext.setEnabled(false);
		logCurrent.setEnabled(false);
		logJS.setEnabled(!logger.getJSData().isEmpty());
	}

	/**
	 * Zeigt neuere oder ältere Logging-Nachrichten an
	 * @param move	Verschiebungsrichtung (-1: vorherige Meldung, 0: neuste Meldung, 1: nächste Meldung)
	 * @see #logPrevious
	 * @see #logNext
	 * @see #logCurrent
	 */
	private void displayLogMessage(final int move) {
		switch (move) {
		case -1: /* Vorherige Meldung */
			if (logTextDisplayIndex>0) logTextDisplayIndex--;
			break;
		case 0: /* Aktuelle bzw. neuste Meldung */
			logTextDisplayIndex=logTextHistory.size()-1;
			break;
		case 1: /* Nächste Meldung */
			if (logTextDisplayIndex<logTextHistory.size()-1) logTextDisplayIndex++;
			break;
		}

		logLabel.setText(logTextHistory.get(logTextDisplayIndex));

		logPrevious.setEnabled(logTextDisplayIndex>0);
		logNext.setEnabled(logTextDisplayIndex<logTextHistory.size()-1);
		logCurrent.setEnabled(logTextDisplayIndex<logTextHistory.size()-1);
	}

	/**
	 * Befehl: Logging-Ausgaben in die Zwischenablage kopieren
	 * @see #logCopy
	 */
	private void copyLogMessage() {
		final Clipboard clipboard=Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(new StringSelection(logTextHistoryPlain.get(logTextDisplayIndex)),null);
	}

	/**
	 * Befehl: "Animationsstart" - "Animation sofort starten"/"Im Pause-Modus starten"
	 * @param paused	Animation im Pausemodus starten?
	 * @see #menuStartModePause
	 * @see #menuStartModeRun
	 */
	private void commandAnimationStartMode(final boolean paused) {
		if (setup.animationStartPaused==paused) return;
		setup.animationStartPaused=paused;
		setup.saveSetup();
	}

	/**
	 * Befehl: "Animationsende" - "Sofort zur Statistikansicht umschalten"/"Animation zunächst pausien"
	 * @param paused	Animation am Ende pausieren?
	 * @see #menuFinishModePause
	 * @see #menuFinishModeRun
	 */
	private void commandAnimationFinishMode(final boolean paused) {
		if (setup.animationFinishPaused==paused) return;
		setup.animationFinishPaused=paused;
		setup.saveSetup();
	}

	/**
	 * Befehl: "Analoge Werte in Animation" - "Schnelle Animation"/"Änderungen exakt anzeigen (langsam)"
	 * @param useSlowModeAnimation	Animation langsamer dafür analoge Werte besser darstellen?
	 * @see #menuAnalogValuesFast
	 * @see #menuAnalogValuesExact
	 */
	private void commandAnalogValuesSlow(final boolean useSlowModeAnimation) {
		if (setup.useSlowModeAnimation==useSlowModeAnimation) return;
		setup.useSlowModeAnimation=useSlowModeAnimation;
		setup.saveSetup();
	}

	/**
	 * Befehl: "Verzeichnis zum Speichern von Bildern" - "Im Nutzerverzeichnis"
	 * @see #menuScreenshotModeHome
	 */
	private void commandScreenshotModeHome() {
		setup.imagePathAnimation="";
		setup.saveSetup();
		updateScreenshotButtonHint();
	}

	/**
	 * Befehl: "Verzeichnis zum Speichern von Bildern" - "Im ausgewählten Verzeichnis"
	 * @see #menuScreenshotModeCustom
	 */
	private void commandScreenshotModeCustom() {
		final JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		if (setup.imagePathAnimation!=null && !setup.imagePathAnimation.isBlank() && new File(setup.imagePathAnimation).isDirectory()) {
			fc.setCurrentDirectory(new File(setup.imagePathAnimation));
		}
		fc.setDialogTitle(Language.tr("Batch.Output.Folder.Button.Hint"));
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (fc.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION) return;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		final File file=fc.getSelectedFile();
		setup.imagePathAnimation=file.toString();
		setup.saveSetup();
		updateScreenshotButtonHint();
	}

	/**
	 * Aktualisiert den Tooltip-Text für {@link #buttonScreenshot}
	 * gemäß dem gewählten Ausgabeordner für Screenshots.
	 * @see #buttonScreenshot
	 * @see #commandScreenshotModeHome()
	 * @see #commandScreenshotModeCustom()
	 */
	private void updateScreenshotButtonHint() {
		String folder=FileSystemView.getFileSystemView().getHomeDirectory().toString();
		if (setup.imagePathAnimation!=null && !setup.imagePathAnimation.isBlank()) folder=setup.imagePathAnimation.trim();
		buttonScreenshot.setToolTipText(Language.tr("Animation.Toolbar.Image.Info")+" ("+Language.tr("Animation.Toolbar.Image.Info.Folder")+": "+folder+")"+" ("+keyStrokeToString(KeyStroke.getKeyStroke(KeyEvent.VK_S,InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_DOWN_MASK))+")");
	}

	/**
	 * Befehl: "Logging-Daten im Einzelschrittmodus anzeigen" (an/aus umschalten)
	 * @see #menuShowLog
	 */
	private void toggleShowSingleStepLogData() {
		setup.showSingleStepLogData=!setup.showSingleStepLogData;
		setup.saveSetup();

		if (setup.showSingleStepLogData) {
			logger=parentLogger;
		} else {
			if (logger!=null) {
				logger.setActive(false);
				logger=null;
			}
			if (logArea.isVisible()) logArea.setVisible(false);
		}
	}

	/**
	 * Befehl: "Animation bei Pause-Skriptanweisung unterbrechen" (an/aus umschalten)
	 */
	private void toggleRespectPauseCommand() {
		setup.respectPauseCommand=!setup.respectPauseCommand;
		setup.saveSetup();
	}

	/**
	 * Stellt ein Unter-Animator-Element ein, welches ebenfalls bei Animationsschritten benachrichtigt werden soll
	 * @param subViewer	Unter-Animator-Element (kann auch <code>null</code> sein, wenn kein zusätzliches Element benachrichtigt werden soll)
	 * @see #removeSubViewer(RunModelAnimationViewer)
	 */
	public void addSubViewer(final RunModelAnimationViewer subViewer) {
		synchronized(subViewers) {
			this.subViewers.add(subViewer);
		}
	}

	/**
	 * Trägt ein Unter-Animator-Element aus der Liste der zu benachrichtigenden Elemente aus.
	 * @param subViewer	Nicht mehr zu benachrichtigendes Unter-Animator-Element
	 * @see #addSubViewer(RunModelAnimationViewer)
	 * @return	Gibt an, ob das Unter-Animator-Element aus der Liste ausgetragen werden konnte
	 */
	public boolean removeSubViewer(final RunModelAnimationViewer subViewer) {
		synchronized(subViewers) {
			return subViewers.remove(subViewer);
		}
	}

	/**
	 * Liefert den momentan gewählten Delay-Wert für die Übertragung an Unter-Animator-Elemente
	 * @return	Aktueller Delay-Wert
	 */
	public int getDelayIntern() {
		return delayInt;
	}

	/**
	 * Beim letzten Aufruf von {@link #calcExpression()} zuletzt eingegebener Rechenausdruck.
	 * @see #calcExpression()
	 * @see ExpressionCalculatorDialog
	 */
	private String lastCaluclationExpression=null;

	/**
	 * Beim letzten Aufruf von {@link #calcExpression()} zuletzt eingegebener Javascript-Code.
	 * @see #calcExpression()
	 * @see ExpressionCalculatorDialog
	 */
	private String lastCaluclationJavaScript=null;

	/**
	 * Beim letzten Aufruf von {@link #calcExpression()} zuletzt eingegebener Java-Code.
	 * @see #calcExpression()
	 * @see ExpressionCalculatorDialog
	 */
	private String lastCaluclationJava=null;

	/**
	 * Beim letzten Aufruf von {@link #calcExpression()} zuletzt aktiver Tab.
	 * @see #calcExpression()
	 * @see ExpressionCalculatorDialog
	 */
	private int lastCaluclationTab=0;

	/**
	 * Befehl: Ausdruck berechnen
	 * @see #logExpression
	 * @see ExpressionCalculatorDialog
	 */
	private void calcExpression() {
		final List<String> variables=new ArrayList<>();
		final Map<String,Integer> variableIndices=new HashMap<>();
		for (int i=0;i<simData.runModel.variableNames.length;i++) {
			final String name=simData.runModel.variableNames[i];
			boolean isAdditionalVariable=false;
			for (String additionalVariable: RunModel.additionalVariables) if (name.equalsIgnoreCase(additionalVariable)) {isAdditionalVariable=true; break;}
			if (isAdditionalVariable) continue;
			variables.add(name);
			variableIndices.put(name,i);
		}
		variables.sort(String::compareTo);

		final EditModel editModel=simulator.getEditModel();

		final ExpressionCalculatorDialog dialog;

		if (running) {
			dialog=new ExpressionCalculatorDialog(
					this,
					editModel,
					simData,
					variables.toArray(String[]::new),
					name->simData.runData.variableValues[variableIndices.get(name)],
					null,
					simData.runData.getMapGlobal(),
					null,
					()->getAnimationRunTimeAllClientData(simulator),
					null,
					null,
					null,
					lastCaluclationTab,
					lastCaluclationExpression,
					lastCaluclationJavaScript,
					lastCaluclationJava,
					true
					);
		} else {
			dialog=new ExpressionCalculatorDialog(
					this,
					editModel,
					simData,
					variables.toArray(String[]::new),
					name->simData.runData.variableValues[variableIndices.get(name)],
					(name,value)->{simData.runData.variableValues[variableIndices.get(name)]=value; simData.runData.updateVariableValueForStatistics(simData,variableIndices.get(name));},
					simData.runData.getMapGlobal(),
					s->calculateExpression(s),
					()->getAnimationRunTimeAllClientData(simulator),
					nr->{
						final List<RunDataClient> clientsList=simData.runData.clients.requestClientsInUseList();
						final int size=clientsList.size();
						for (int i=0;i<size;i++) if (clientsList.get(i).clientNumber==nr) return clientsList.get(i);
						return null;
					},
					s->runJavaScript(s),
					s->runJava(s),
					lastCaluclationTab,
					lastCaluclationExpression,
					lastCaluclationJavaScript,
					lastCaluclationJava,
					false
					);
		}

		dialog.setVisible(true);

		lastCaluclationTab=dialog.getLastMode();
		if (!running) {
			lastCaluclationExpression=dialog.getLastExpression();
			lastCaluclationJavaScript=dialog.getLastJavaScript();
			lastCaluclationJava=dialog.getLastJava();
		}

		simData.runData.updateMapValuesForStatistics(simData);
		simData.runData.fireStateChangeNotify(simData);
	}

	/**
	 * Zeigt einen Statistikdialog zu den aktuell vorliegenden Statistikdaten an.
	 */
	private void showStatistics() {
		final Statistics statistics=simulator.getIncompleteStatistic();
		if (statistics==null) {
			MsgBox.error(AnimationPanel.this,Language.tr("Editor.Statistics.ErrorTitle"),Language.tr("Editor.Statistics.ErrorInfo"));
			return;
		}

		final ModelViewerFrame viewer=new ModelViewerFrame(window,model,statistics,true,null);
		viewer.selectStatisticTab();
		viewer.setVisible(true);
	}

	/**
	 * Zeigt einen Dialog zum Bearbeiten der Haltepunkte an.
	 */
	private void showBreakpoints() {
		/* Gibt es überhaupt Haltepunkte / Pause-Stationen? */
		final int breakpointCount=surfaceAnimator.getBreakPoints().size();
		final int pauseStationCount=AnimationPanelBreakPointsDialog.getAllPauseStations(model).size();
		if (breakpointCount==0 && pauseStationCount==0) {
			MsgBox.error(this,Language.tr("Editor.Breakpoints.NoBreakpoints.Title"),Language.tr("Editor.Breakpoints.NoBreakpoints.Info"));
			return;
		}

		/* Dialog zum Bearbeiten von Haltepunkten und zur Anzeige von Pause-Stationen anzeigen */
		new AnimationPanelBreakPointsDialog(this,simData,model,surfaceAnimator);
	}

	/**
	 * Liefert eine Liste der Kunden im System.
	 * Der Abruf erfolgt dabei synchronisiert zu einem möglicherweise laufenden Simulator.
	 * @param simulator	Simulatorinstanz
	 * @return	Liste der Kunden im System
	 */
	private List<ModelElementAnimationInfoDialog.ClientInfo> getAnimationRunTimeAllClientData(final Simulator simulator) {
		if (simulator==null) return new ArrayList<>();
		final boolean wasPaused=simulator.isPaused();
		if (!wasPaused) simulator.pauseExecutionAndWait();
		try {
			return simData.runData.clients.requestClientsInUseList().stream().map(client->new ClientInfo(simulator.getEditModel().animationImages,simData.runModel,client)).collect(Collectors.toList());
		} finally {
			if (!wasPaused) simulator.resumeExecution();
		}
	}

	/**
	 * Befehl: Ergebnisse der Javascript-Skriptausführung anzeigen
	 * @see #logJS
	 * @see AnimationJSInfoDialog
	 */
	private void showJSResults() {
		new AnimationJSInfoDialog(this,logger.getJSData(),simData);
	}

	/**
	 * Befehl: Nächste geplante Ereignisse anzeigen
	 * @see #logEvents
	 * @see NextEventsViewerDialog
	 */
	private void showEventslist() {
		new NextEventsViewerDialog(this,simData.eventManager.getAllEvents(),simData);
	}

	/**
	 * Liefert eine in ein JSON-Array umwandelbare Zuordnung mit Daten über die statischen Icons
	 * (Key: "staticImages"), die bewerten Icons (Key: "movingImages") und die aktuellen
	 * Logging-Ausgaben (Key: "logs").
	 * @return	Zuordnung mit Daten zum aktuellen Animationsschritt
	 */
	public Map<String,Object> getAnimationStepInfo() {
		final List<Map<String,String>> staticElementsList=new ArrayList<>();
		final List<Map<String,Object>> movingElementsList=new ArrayList<>();

		final Map<String,Object> map=new HashMap<>();
		final Map<String,Object> staticElements=new HashMap<>();
		final Map<String,Object> movingElements=new HashMap<>();
		String time="0";

		if (simData!=null) {
			if (surfaceAnimator!=null && simulator!=null) {
				surfaceAnimator.getAnimationStepInfo(simData.currentTime,simulator.getRunModel(),staticElementsList,movingElementsList);

				final Set<String> movingIDs=new HashSet<>();
				for (int i=0;i<movingElementsList.size();i++) {
					final Map<String,Object> movingMap=movingElementsList.get(i);
					final Object obj=movingMap.get("0");
					if (obj!=null) {
						@SuppressWarnings("unchecked")
						final String id=((Map<String,String>)obj).get("id");
						if (id!=null) movingIDs.add(id);
					}
					movingElements.put(""+(i+1),movingMap);
				}

				int nr=0;
				for (int i=0;i<staticElementsList.size();i++) {
					final Map<String,String> staticMap=staticElementsList.get(i);
					if (!movingIDs.contains(staticMap.get("id"))) {
						nr++;
						staticElements.put(""+nr,staticMap);
					}
				}

				map.put("staticImages",staticElements);
				map.put("movingImages",movingElements);
			}
			time=NumberTools.formatSystemNumber(simData.currentTime,3);
		}

		map.put("time",time);
		map.put("staticImages",staticElements);
		map.put("movingImages",movingElements);
		map.put("logs",logText.toString());
		return map;
	}

	/**
	 * Versucht den als Zeichenkette übergebenen Ausdruck im Kontext der Simulationsdaten zu berechnen
	 * @param expression	Zu berechnender Ausdruck
	 * @return	Ergebnis oder im Fehlerfall <code>null</code>
	 */
	public Double calculateExpression(final String expression) {
		return surfaceAnimator.calculateExpression(expression);
	}

	/**
	 * Versucht das als Zeichenkette übergebene Javascript im Kontext der Simulationsdaten auszuführen
	 * @param script	Auszuführendes Javascript
	 * @return	Rückgabewert (Text oder Fehlermeldung oder leere Zeichenkette)
	 */
	public String runJavaScript(final String script) {
		return surfaceAnimator.runJavaScript(script);
	}

	/**
	 * Versucht den als Zeichenkette übergebenen Java-Code im Kontext der Simulationsdaten auszuführen
	 * @param script	Auszuführender Java-Code
	 * @return	Rückgabewert (Text oder Fehlermeldung oder leere Zeichenkette)
	 */
	public String runJava(final String script) {
		return surfaceAnimator.runJava(script);
	}

	/**
	 * Kopiert das aktuelle Animationsbild in die Zwischenablage.
	 */
	private void commandCopyToClipboard() {
		surfacePanel.copyToClipboardAsImage(setup.imageSize,setup.imageSize);
	}

	/**
	 * Zeigt ein Popupmenü mit Funktionen zum Export des Modells an.
	 * @param parent	Übergeordnetes Element zur Ausrichtung des Menüs
	 * @see #buttonExport
	 */
	private void showExportMenu(final JButton parent) {
		final JPopupMenu popupMenu=new JPopupMenu();
		JMenuItem item;

		popupMenu.add(item=new JMenuItem(Language.tr("Animation.Toolbar.Export.Copy"),Images.EDIT_COPY.getIcon()));
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,InputEvent.CTRL_DOWN_MASK));
		item.addActionListener(e->commandCopyToClipboard());

		popupMenu.add(item=new JMenuItem(Language.tr("Animation.Toolbar.Export.Save"),Images.GENERAL_SAVE.getIcon()));
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,InputEvent.CTRL_DOWN_MASK));
		item.addActionListener(e->saveScreenshotSelectFile());

		popupMenu.show(parent,0,parent.getHeight());
	}

	/**
	 * Liefert das Animationssystem-Objekt.<br>
	 * (Ist gedacht für Zugriffe auf die Haltepunkte.)
	 * @return	Animationssystem-Objekt
	 */
	public ModelSurfaceAnimator getAnimator() {
		return surfaceAnimator;
	}

	/**
	 * Listener für Klicks auf die verschiedenen Symbolleisten-Schaltflächen
	 * @see AnimationPanel#toolbarListener
	 */
	private class ToolBarListener implements ActionListener {
		/**
		 * Konstruktor der Klasse
		 */
		public ToolBarListener() {
			/*
			 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
			 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			final Object source=e.getSource();
			if (source==buttonZoomOut) {surfacePanel.zoomOut(); zoomChanged(); return;}
			if (source==buttonZoomIn) {surfacePanel.zoomIn(); zoomChanged(); return;}
			if (source==buttonZoomDefault) {surfacePanel.zoomDefault(); zoomChanged(); return;}
			if (source==buttonFindModel) {surfacePanel.centerModel(); return;}
			if (source==buttonDashboard) {showDashboard(); return;}
			if (source==buttonViews) {showViewPopup(buttonViews); return;}
			if (source==buttonStartRecording) {surfaceAnimator.startRecording(); buttonStartRecording.setVisible(false); return;}
			if (source==buttonAbort) {closeRequest(); buttonAbort.setEnabled(false); return;}
			if (source==buttonScreenshot) {saveScreenshot(); return;}
			if (source==buttonExport) {showExportMenu(buttonExport); return;}
			if (source==buttonSimulation) {finishAsSimulation(); return;}
			if (source==buttonTools) {animationToolsPopup(); return;}
			if (source==buttonPlayPause) {playPause(); return;}
			if (source==buttonStep) {step(false);}
			if (source==buttonSpeed) {animationSpeedPopup(); return;}
			if (source==menuStartModeRun) {commandAnimationStartMode(false); return;}
			if (source==menuStartModePause) {commandAnimationStartMode(true); return;}
			if (source==menuFinishModeRun) {commandAnimationFinishMode(false); return;}
			if (source==menuFinishModePause) {commandAnimationFinishMode(true); return;}
			if (source==menuAnalogValuesFast) {commandAnalogValuesSlow(false); return;}
			if (source==menuAnalogValuesExact) {commandAnalogValuesSlow(true); return;}
			if (source==menuScreenshotModeHome) {commandScreenshotModeHome(); return;}
			if (source==menuScreenshotModeCustom) {commandScreenshotModeCustom(); return;}
			if (source==menuShowLog) {toggleShowSingleStepLogData(); return;}
			if (source==menuRespectPauseCommand) {toggleRespectPauseCommand(); return;}
			if (source==buttonProperties) {showModelPropertiesDialog(null); return;}
			if (source==buttonCurrentData) {calcExpression(); return;}
			if (source==buttonCurrentStatistics) {showStatistics(); return;}
			if (source==buttonBreakpoints) {showBreakpoints(); return;}
			if (source==logPrevious) {displayLogMessage(-1); return;}
			if (source==logNext) {displayLogMessage(1); return;}
			if (source==logCurrent) {displayLogMessage(0); return;}
			if (source==logCopy) {copyLogMessage(); return;}
			if (source==logExpression) {calcExpression(); return;}
			if (source==logJS) {showJSResults(); return;}
			if (source==logEvents) {showEventslist(); return;}
		}
	}

	@Override
	public void animationTerminated() {
		if (logUpdateThread!=null) {
			logUpdateThread.done();
			logUpdateThread=null;
		}

		final HashSet<RunModelAnimationViewer> viewers=new HashSet<>();
		synchronized(subViewers) {
			viewers.addAll(subViewers);
		}
		for (RunModelAnimationViewer subViewer: viewers) subViewer.animationTerminated();
	}

	@Override
	public void pauseAnimation() {
		if (running) playPause();
	}

	/**
	 * Aktualisiert während einer Animation möglicherweise geänderte Diagramm-Element-Daten in dem Editor-Modell.
	 * @param editorSurface	Editor-Zeichenfläche (Ziel für das Kopieren)
	 * @param animationSurface	Animations-Zeichenfläche (von hier aus werden Änderungen übertragen)
	 */
	private void updateEditorModel(final ModelSurface editorSurface, final ModelSurface animationSurface) {
		for (ModelElement element: editorSurface.getElements()) {
			if (element instanceof ElementWithAnimationEditOptions) {
				if (element.getPropertiesSemiEditable(this,null,null)!=null) element.copyDataFrom(animationSurface.getById(element.getId()));
			}
			if (element instanceof ModelElementSub) {
				final ModelSurface editorSubSurface=((ModelElementSub)element).getSubSurface();
				final ModelSurface animationSubSurface=((ModelElementSub)animationSurface.getById(element.getId())).getSubSurface();
				updateEditorModel(editorSubSurface,animationSubSurface);
				((ModelElementSub)element).setSubSurface(editorSubSurface);
			}
		}
	}

	/**
	 * Aktualisiert während einer Animation möglicherweise geänderte Diagramm-Element-Daten in dem Editor-Modell.
	 * @param editorSurface	Hauptzeichenfläche des Editor-Modells
	 */
	public void updateEditorModel(final ModelSurface editorSurface) {
		updateEditorModel(editorSurface,surfacePanel.getSurface());
	}
}
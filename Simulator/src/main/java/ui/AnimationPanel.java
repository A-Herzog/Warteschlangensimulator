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
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
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
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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
import simulator.runmodel.SimulationData;
import systemtools.MsgBox;
import systemtools.SetupBase;
import tools.ButtonRotator;
import tools.SetupData;
import tools.UsageStatistics;
import ui.dialogs.AnimationJSInfoDialog;
import ui.dialogs.ExpressionCalculatorDialog;
import ui.dialogs.NextEventsViewerDialog;
import ui.images.Images;
import ui.mjpeg.AnimationRecordWaitDialog;
import ui.mjpeg.MJPEGSystem;
import ui.mjpeg.VideoSystem;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurfaceAnimator;
import ui.modeleditor.ModelSurfaceAnimatorBase;
import ui.modeleditor.ModelSurfacePanel;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ElementWithAnimationDisplay;
import ui.modeleditor.elements.ModelElementAnalogValue;
import ui.modeleditor.elements.ModelElementAnimationConnect;
import ui.modeleditor.elements.ModelElementConveyor;
import ui.modeleditor.elements.ModelElementSource;
import ui.modeleditor.elements.ModelElementSourceMulti;
import ui.modeleditor.elements.ModelElementSourceRecord;
import ui.modeleditor.elements.ModelElementSub;
import ui.modeleditor.elements.ModelElementTank;
import ui.modelproperties.ModelPropertiesDialog;

/**
 * Diese Klasse zeigt die Animation der Simulation in einem eingebetteten
 * <code>ModelSurfacePanel</code> an.
 * @author Alexander Herzog
 */
public class AnimationPanel extends JPanel implements RunModelAnimationViewer {
	private static final long serialVersionUID = -4834682399637727518L;

	private final JFrame window;

	private final transient SetupData setup;
	private final String infoMJPEG;
	private final String infoNoSum;
	private final String infoSum;

	private boolean startPaused;
	private boolean fastWarmUp;
	private boolean hasAnalogElements;
	private boolean hasConveyorElements;
	private transient EditModel model;
	private ModelSurfacePanel surfacePanel;
	private transient ModelSurfaceAnimator surfaceAnimator;
	private Semaphore simulatorLock=new Semaphore(1);
	private transient Simulator simulator;
	private transient CallbackLoggerWithJS parentLogger;
	private transient CallbackLoggerWithJS logger;
	private boolean running;
	private Runnable animationDone;
	private Runnable sendToSimulation;
	private transient VideoSystem encoder;

	private transient Timer timer;
	private boolean simulationSuccessful;
	private boolean abortRun;
	private boolean continueAsSimulation;
	private transient SimulationData simData;
	private Semaphore mutex;
	private long lastTimeStep;
	private double delaySystem;
	private int delayInt;
	private int delay;
	private boolean speedChanged;

	private final transient ToolBarListener toolbarListener;
	private JLabel labelZoom;
	private final JButton buttonAbort;
	private final JButton buttonScreenshot;
	private final JButton buttonSimulation;
	private final JButton buttonTools;
	private final JButton buttonPlayPause;
	private final JButton buttonStep;
	private final JButton buttonSpeed;
	private final JButton buttonProperties;
	private final JLabel statusBar;
	private final JProgressBar progressBar;
	private final JButton buttonZoomOut;
	private final JButton buttonZoomIn;
	private final JButton buttonZoomDefault;
	private final JButton buttonFindModel;

	private JMenuItem menuStartModeRun;
	private JMenuItem menuStartModePause;
	private JMenuItem menuAnalogValuesFast;
	private JMenuItem menuAnalogValuesExact;
	private JMenuItem menuScreenshotModeHome;
	private JMenuItem menuScreenshotModeCustom;
	private JCheckBoxMenuItem menuShowLog;

	private final JPanel logArea;
	private final JLabel logLabel;
	private long logTimeStamp;
	private StringBuilder logText;
	private StringBuilder logTextPlain;
	private int logTextDisplayIndex;
	private List<String> logTextHistory;
	private List<String> logTextHistoryPlain;
	private final JButton logPrevious;
	private final JButton logNext;
	private final JButton logCurrent;
	private final JButton logCopy;
	private final JButton logExpression;
	private final JButton logJS;
	private final JButton logEvents;

	private RunModelAnimationViewer subViewer;

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

		model=null;
		mutex=new Semaphore(1);
		toolbarListener=new ToolBarListener();

		setLayout(new BorderLayout());

		/* Toolbar oben */
		final JToolBar toolBar=new JToolBar();
		add(toolBar,BorderLayout.NORTH);
		toolBar.setFloatable(false);

		buttonAbort=createToolbarButton(toolBar,Language.tr("Animation.Toolbar.Stop"),Language.tr("Animation.Toolbar.Stop.Info"),Images.GENERAL_CANCEL.getIcon());
		toolBar.addSeparator();

		buttonScreenshot=createToolbarButton(toolBar,Language.tr("Animation.Toolbar.Image"),Language.tr("Animation.Toolbar.Image.Info"),Images.ANIMATION_SCREENSHOT.getIcon());
		updateScreenshotButtonHint();
		buttonSimulation=createToolbarButton(toolBar,Language.tr("Animation.Toolbar.Simulation"),Language.tr("Animation.Toolbar.Simulation.Info"),Images.SIMULATION.getIcon());
		buttonSimulation.setVisible(false);
		buttonTools=createToolbarButton(toolBar,Language.tr("Animation.Toolbar.Tools"),Language.tr("Animation.Toolbar.Tools.Info"),Images.GENERAL_TOOLS.getIcon());
		toolBar.addSeparator();

		buttonPlayPause=createToolbarButton(toolBar,Language.tr("Animation.Toolbar.Pause"),Language.tr("Animation.Toolbar.Pause.Info"),Images.ANIMATION_PAUSE.getIcon());
		buttonStep=createToolbarButton(toolBar,Language.tr("Animation.Toolbar.Step"),Language.tr("Animation.Toolbar.Step.Info"),Images.ANIMATION_STEP.getIcon());
		buttonStep.setEnabled(false);
		buttonSpeed=createToolbarButton(toolBar,Language.tr("Animation.Toolbar.Speed"),Language.tr("Animation.Toolbar.Speed.Info"),Images.ANIMATION_SPEED.getIcon());

		addUserButtons(toolBar);

		/* Area mit linkem Toolbar und Surface */
		final JPanel content=new JPanel(new BorderLayout());
		add(content,BorderLayout.CENTER);

		/* Toolbar links */
		final JToolBar leftToolBar=new JToolBar(SwingConstants.VERTICAL);
		leftToolBar.setFloatable(false);
		add(leftToolBar,BorderLayout.WEST);

		buttonProperties=createRotatedToolbarButton(leftToolBar,Language.tr("Editor.ModelProperties.Short"),Language.tr("Editor.ModelProperties.Info"),Images.MODEL.getIcon());

		/* Surface in der Mitte */
		content.add(new RulerPanel(surfacePanel=new ModelSurfacePanel(true,false),SetupData.getSetup().showRulers),BorderLayout.CENTER);
		surfacePanel.addZoomChangeListener(e->zoomChanged());

		/* Statusbar unten */
		final JPanel statusPanel=new JPanel(new BorderLayout());
		add(statusPanel,BorderLayout.SOUTH);

		final JPanel statusBarOuter=new JPanel(new BorderLayout());
		statusPanel.add(statusBarOuter,BorderLayout.CENTER);
		statusBarOuter.add(statusBar=new JLabel(""),BorderLayout.WEST);
		statusBar.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));
		JPanel zoomArea=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));

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
		zoomArea.add(buttonZoomOut=createToolbarButton(null,"",Language.tr("Main.Menu.View.ZoomOut"),Images.ZOOM_OUT.getIcon()));
		buttonZoomOut.setPreferredSize(new Dimension(20,20));
		buttonZoomOut.setBorderPainted(false);
		buttonZoomOut.setFocusPainted(false);
		buttonZoomOut.setContentAreaFilled(false);
		zoomArea.add(buttonZoomIn=createToolbarButton(null,"",Language.tr("Main.Menu.View.ZoomIn"),Images.ZOOM_IN.getIcon()));
		buttonZoomIn.setPreferredSize(new Dimension(20,20));
		buttonZoomIn.setBorderPainted(false);
		buttonZoomIn.setFocusPainted(false);
		buttonZoomIn.setContentAreaFilled(false);
		zoomArea.add(buttonZoomDefault=createToolbarButton(null,"",Language.tr("Main.Menu.View.ZoomDefault"),Images.ZOOM.getIcon()));
		buttonZoomDefault.setPreferredSize(new Dimension(20,20));
		buttonZoomDefault.setBorderPainted(false);
		buttonZoomDefault.setFocusPainted(false);
		buttonZoomDefault.setContentAreaFilled(false);
		zoomArea.add(buttonFindModel=createToolbarButton(null,"",Language.tr("Main.Menu.View.CenterModel"),Images.ZOOM_CENTER_MODEL.getIcon()));
		buttonFindModel.setPreferredSize(new Dimension(20,20));
		buttonFindModel.setBorderPainted(false);
		buttonFindModel.setFocusPainted(false);
		buttonFindModel.setContentAreaFilled(false);

		statusPanel.add(logArea=new JPanel(new BorderLayout()),BorderLayout.SOUTH);
		logArea.setVisible(false);
		logArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		logArea.setBackground(Color.WHITE);
		logArea.add(logLabel=new JLabel(),BorderLayout.CENTER);
		logLabel.setBorder(BorderFactory.createEmptyBorder(2,5,2,5));
		final JToolBar logToolBar=new JToolBar(SwingConstants.VERTICAL);
		logArea.add(logToolBar,BorderLayout.EAST);
		logToolBar.setFloatable(false);
		logToolBar.add(Box.createVerticalGlue());
		logPrevious=createToolbarButton(logToolBar,"",Language.tr("Animation.Log.Previous"),Images.ARROW_UP.getIcon());
		logNext=createToolbarButton(logToolBar,"",Language.tr("Animation.Log.Next"),Images.ARROW_DOWN.getIcon());
		logCurrent=createToolbarButton(logToolBar,"",Language.tr("Animation.Log.Current"),Images.ARROW_DOWN_END.getIcon());
		logCopy=createToolbarButton(logToolBar,"",Language.tr("Animation.Log.Copy"),Images.EDIT_COPY.getIcon());
		logExpression=createToolbarButton(logToolBar,"",Language.tr("Animation.Log.Expression"),Images.ANIMATION_EVALUATE_EXPRESSION.getIcon());
		logJS=createToolbarButton(logToolBar,"",Language.tr("Animation.Log.JS"),Images.ANIMATION_EVALUATE_SCRIPT.getIcon());
		logEvents=createToolbarButton(logToolBar,"",Language.tr("Animation.Log.Events"),Images.ANIMATION_LIST_NEXT_EVENTS.getIcon());

		delay=setup.animationDelay*10;
		animationDelayChanged();
	}

	/**
	 * Ermöglicht das Hinzufügen weiterer Schaltflächen zur Symbolleiste in abgeleiteten Klassen
	 * @param toolbar	Toolbar des Panels
	 */
	protected void addUserButtons(final JToolBar toolbar) {}

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
		}

		final JButton button=ButtonRotator.getRotatedButton(title,rotatedIcon);

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

	private void setSurfacePosition(final Point position) {
		if (surfacePanel.getParent() instanceof JViewport) {
			JViewport viewport=(JViewport)surfacePanel.getParent();
			viewport.setViewPosition(position);
		}
	}

	private double calculateMinimalTimeStepFromRecord(final ModelElementSourceRecord record, final double oldMin) {
		final double mean=DistributionTools.getMean(record.getInterarrivalTimeDistribution());
		final long multiply=record.getTimeBase().multiply;
		if (mean>0 && mean*multiply<oldMin) return mean*multiply;
		return oldMin;
	}

	private void calculateMinimalTimeStep() {
		double min=86400;

		for (ModelElement element: model.surface.getElements()) {
			if (element instanceof ModelElementSource) {
				final ModelElementSourceRecord record=((ModelElementSource)element).getRecord();
				min=calculateMinimalTimeStepFromRecord(record,min);
			}
			if (element instanceof ModelElementSourceMulti) for (ModelElementSourceRecord record: ((ModelElementSourceMulti)element).getRecords()) {
				min=calculateMinimalTimeStepFromRecord(record,min);
			}
		}

		delaySystem=min/500;
	}

	/**
	 * Stellt das zu simulierende Modell ein. Der Simulator wird durch diese Methode gestartet, darf
	 * also nicht bereits vorher gestartet worden sein. Vor der Erstellung des Simulator-Objekts muss
	 * außerdem zunächst die <code>makeAnimationModel</code>-Methode auf das Editor-Modell angewandt werden.
	 * @param model	Editor-Modell (für die Animation des Simulationsverlaufs in einem <code>ModelSurfacePanel</code>-Objekt)
	 * @param simulator	Simulator für das Modell (darf noch nicht gestartet worden sein)
	 * @param logger	Logger, über den die Einzelschritt ausgaben angezeigt werden
	 * @param recordFile	Videodatei, in der die Animation aufgezeichnet werden soll
	 * @param paintTimeStamp	Fügt bei der Aufzeichnung in das Video den jeweils aktuellen Simulationszeit-Wert ein
	 * @param fastWarmUp	Ist <code>true</code>, wenn die Warm-up-Phase bei der Animation zunächst als Simulation vorab ausgeführt werden soll
	 * @param zoom	Zoomfaktor für das Animations-Surface
	 * @param raster	Rasteranzeige auf dem Animations-Surface
	 * @param position	Position der linken oberen Ecke des Animations-Surface
	 * @param animationDone	Runnable, das aufgerufen wird, wenn die Simulation beendet wurde
	 * @param sendToSimulation	Runnable, das aufgerufen wird, wenn die Simulation ohne Animation zu Ende geführt werden soll
	 * @param startPaused	Gibt an, ob die Animation direkt nach der Initialisierung starten soll oder ob sich das System anfänglich im Pausemodus befinden soll
	 * @param startFullRecording	Wird die Animation im Pausemodus gestartet, so wird direkt der erste Schritt ausgeführt. Über diese Funktion kann angegeben werden, dass dieser Schritt im vollständigen Erfassungsmodus durchgeführt werden soll.
	 * @see #makeAnimationModel(EditModel)
	 */
	public void setSimulator(final EditModel model, final Simulator simulator, final CallbackLoggerWithJS logger, final File recordFile, final boolean paintTimeStamp, final boolean fastWarmUp, final double zoom, final ModelSurface.Grid raster, final Point position, final Runnable animationDone, final Runnable sendToSimulation, final boolean startPaused, final boolean startFullRecording) {
		this.model=model;
		this.startPaused=startPaused;
		this.fastWarmUp=fastWarmUp;

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
		}
		surfaceAnimator.setRecordSystem(encoder,paintTimeStamp);

		surfacePanel.getSurface().setAnimatorPanel(this);
		surfacePanel.setZoom(zoom);
		zoomChanged();
		surfacePanel.setRaster(raster);
		surfacePanel.setColors(model.surfaceColors);
		setSurfacePosition(position);

		final Timer positionTimer=new Timer("AnimationPanelLayoutTimer",false);
		positionTimer.schedule(new TimerTask() {@Override public void run() {positionTimer.cancel(); setSurfacePosition(position);}},50,5000);

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
			running=true; playPause();
			if (this.logger!=null) this.logger.setActive(true);
			simulator.start(true);
		} else {
			simulator.start(false);
			if (logger.getNextLogger()==null) simulator.pauseLogging();
			timer=new Timer("AnimationCancelCheck",false);
			timer.schedule(new UpdateInfoTask(),100);
		}
	}

	/**
	 * Liefert den mit dem Panel verbundenen Simulator
	 * @return	Verbundener Simulator
	 */
	public Simulator getSimulator() {
		return simulator;
	}

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

	private boolean hasConveyorElements(final EditModel model) {
		for (ModelElement element: model.surface.getElements()) {
			if (element instanceof ModelElementConveyor) return true;
			if (element instanceof ModelElementSub) for (ModelElement sub: ((ModelElementSub)element).getSubSurface().getElements()) {
				if (sub instanceof ModelElementConveyor) return true;
			}
		}
		return false;
	}

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
		if (model.warmUpTime<=0) return false; /* Kein Warm-Up definiert, also nix zu tun. */

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
				final int result=MsgBox.options(this,Language.tr("Animation.SkipWarmUp.Title"),String.format("<html><body>"+Language.tr("Animation.SkipWarmUp")+"</body></html>",NumberTools.formatLong(FastMath.round(model.warmUpTime*model.clientCount))),options.toArray(new String[0]),infos.toArray(new String[0]));
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

		if (skip) model.warmUpTime=0;
		return fast;
	}

	private void finalizeSimulation(final boolean successful) {
		if (surfaceAnimator==null) return;
		if (simulator==null) return;

		surfaceAnimator.setRecordSystem(null,false);
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
		if (animationDone!=null) SwingUtilities.invokeLater(animationDone);
	}

	/**
	 * Gibt an, ob die Simulation erfolgreich beendet wurde.
	 * @return	Gibt <code>true</code> zurück, wenn die Simulation erfolgreich beendet wurde
	 */
	public boolean isSimulationSuccessful() {
		return simulationSuccessful;
	}

	private long lastStatusUpdate;
	private long lastStatusCurrent;
	private String lastStatusCurrentString;
	private long lastStatusSum;
	private String lastStatusSumString;

	private void updateStatus(final long currentTime) {
		final long time=System.currentTimeMillis();
		if (time-lastStatusUpdate<20) return;
		lastStatusUpdate=time;

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
		final String currentTimeString=TimeTools.formatLongTime(currentTime/1000);
		if (sum<=0) {
			statusBar.setText(String.format(infoNoSum,currentTimeString,lastStatusCurrentString,recordStatus));
		} else {
			statusBar.setText(String.format(infoSum,currentTimeString,lastStatusCurrentString,lastStatusSumString,NumberTools.formatPercent(((double)current)/sum,0),recordStatus));
			progressBar.setValue((int)(current/1000));
		}
	}

	private void delaySystem(final SimulationData simData, int timeStepDelay) {
		if (lastTimeStep>0) {
			double seconds=(simData.currentTime-lastTimeStep)/1000.0;

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
				if (timeStepDelay>10) timeStepDelay=(int)Math.pow(timeStepDelay,1.2);
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
					updateStatus(stepTime);
					surfaceAnimator.updateSurfaceAnimationDisplayElements(simData,true,false);
					Thread.sleep(delayMS);
					if (speedChanged) break;
				} catch (InterruptedException e) {Thread.currentThread().interrupt(); return;}
			}
			simData.currentTime=save_currentTime;
		}
		speedChanged=false;
		lastTimeStep=simData.currentTime;
		updateStatus(simData.currentTime);
	}

	@Override
	public boolean updateViewer(SimulationData simData) {
		if (subViewer!=null) subViewer.updateViewer(simData);
		return updateViewer(simData,null,false);
	}

	private long lastUpdateStep=0;

	@Override
	public boolean updateViewer(SimulationData simData, RunDataClient client, boolean moveByTransport) {
		if (abortRun) return false;
		if (continueAsSimulation) return false;
		if (surfaceAnimator==null) return true;

		surfaceAnimator.setSlowMode(running && delayInt>0 && (hasAnalogElements || hasConveyorElements) && setup.useSlowModeAnimation);

		if (subViewer!=null) subViewer.updateViewer(simData,client,moveByTransport);

		surfacePanel.setAnimationSimulationData(simData);

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
			if (simData!=null) {
				if (logger==null || !logger.isActive()) {
					delaySystem(simData,delayInt/4); /* Verzögerungen von einem Ereignis zum nächsten ausschalten im Einzelschrittmodus. */
				} else {
					updateStatus(simData.currentTime); /* Aber Statuszeile muss aktualisiert werden. (Passiert sonst in delaySystem.) */
				}
				if (!moveByTransport) surfaceAnimator.process(simData,client,FastMath.min(20,delayInt/4));
				surfacePanel.repaint(); /* Wichtig, sonst wird im Einzelschrittmodus der letzte Schritt nicht korrekt dargestellt (und Zahlenwerte an den Stationen stimmen nicht!) */
			}
		} finally {mutex.release();}

		if (startPaused && fastWarmUp) {
			startPaused=false;
			playPause();
			surfacePanel.repaint();
		}

		return true;
	}

	@Override
	public boolean updateViewer(SimulationData simData, RunDataTransporter transporter) {
		if (abortRun) return false;
		if (continueAsSimulation) return false;

		surfaceAnimator.setSlowMode(running && delayInt>0);

		if (subViewer!=null) subViewer.updateViewer(simData,transporter);

		surfacePanel.setAnimationSimulationData(simData);

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
			if (simData!=null) {
				if (logger==null || !logger.isActive()) {
					delaySystem(simData,delayInt/4); /* Verzögerungen von einem Ereignis zum nächsten ausschalten im Einzelschrittmodus. */
				} else {
					updateStatus(simData.currentTime); /* Aber Statuszeile muss aktualisiert werden. (Passiert sonst in delaySystem.) */
				}
				surfaceAnimator.process(simData,transporter,FastMath.min(20,delayInt/4));
			}
		} finally {mutex.release();}
		return true;
	}

	/**
	 * Teil dem Panel mit, dass es geschlossen werden soll.<br>
	 * Dadurch wird ggf. der Abbruch der Simulation ausgelöst.
	 */
	public void closeRequest() {
		abortRun=true;
		if (!running) abortRunTest();
	}

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
				finalizeSimulation(!abortRun);
			} finally {mutex.release();}
			return false;
		}
		return true;
	}

	private class UpdateInfoTask extends TimerTask {
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

	private void animationDelayChanged() {
		speedChanged=true;
		if (delay/10!=setup.animationDelay) {
			setup.animationDelay=delay/10;
			setup.saveSetup();
		}
	}

	private void zoomChanged() {
		labelZoom.setText(FastMath.round(100*surfacePanel.getZoom())+"% ");
	}

	/**
	 * Liefert den aktuell eingestellten Zoomfaktor
	 * @return Aktueller Zoomfaktor
	 */
	public double getZoom() {
		return surfacePanel.getZoom();
	}

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
		slider.addChangeListener(new ChangeListener() {
			@Override public void stateChanged(ChangeEvent e) {surfacePanel.setZoom(slider.getValue()/5.0); zoomChanged();}
		});
		slider.setPreferredSize(new Dimension(slider.getPreferredSize().width,350));

		popup.add(slider);

		popup.show(parent,0,-350);
	}

	private void showModelPropertiesDialog() {
		if (model==null) return;
		final ModelPropertiesDialog dialog=new ModelPropertiesDialog(this,model,true,null);
		dialog.setVisible(true);
	}

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
		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				delay=(11-slider.getValue())*10;
				delayInt=delay;
				animationDelayChanged();
			}
		});

		popup.add(slider);

		popup.show(buttonSpeed,0,buttonSpeed.getHeight());
	}

	private void animationToolsPopup() {
		final JPopupMenu popup=new JPopupMenu();

		JMenuItem submenu;
		ButtonGroup buttonGroup;

		if (!SetupBase.memoryOnly) {
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
		}

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
			if (setup.imagePathAnimation!=null && !setup.imagePathAnimation.trim().isEmpty()) custom=" ("+setup.imagePathAnimation.trim()+")";
			submenu.add(menuScreenshotModeCustom=new JRadioButtonMenuItem(Language.tr("Main.Menu.AnimationScreenshotMode.Custom")+custom));
			menuScreenshotModeCustom.addActionListener(new ToolBarListener());
			buttonGroup=new ButtonGroup();
			buttonGroup.add(menuScreenshotModeHome);
			buttonGroup.add(menuScreenshotModeCustom);
			menuScreenshotModeHome.setSelected(setup.imagePathAnimation==null || setup.imagePathAnimation.trim().isEmpty());
			menuScreenshotModeCustom.setSelected(setup.imagePathAnimation!=null && !setup.imagePathAnimation.trim().isEmpty());
		}

		popup.add(menuShowLog=new JCheckBoxMenuItem(Language.tr("SettingsDialog.Tabs.Simulation.ShowSingleStepLogData")));
		menuShowLog.addActionListener(new ToolBarListener());
		menuShowLog.setSelected(setup.showSingleStepLogData);

		popup.show(buttonTools,0,buttonTools.getHeight());
	}

	private void playPause() {
		simulatorLock.acquireUninterruptibly();
		try {
			if (running) {
				/* Pause */
				running=false;
				buttonStep.setEnabled(true);
				buttonPlayPause.setText(Language.tr("Animation.Toolbar.Play"));
				buttonPlayPause.setToolTipText(Language.tr("Animation.Toolbar.Play.Info"));
				buttonPlayPause.setIcon(Images.ANIMATION_PLAY.getIcon());
				if (simulator!=null) simulator.pauseExecution();

				if (timer!=null) {timer.cancel(); timer=null;}
			} else {
				/* Play */
				if (surfaceAnimator!=null) surfaceAnimator.setFullRecording(false);
				if (logger!=null) {
					logger.setActive(false);
					if (simulator!=null && logger.getNextLogger()==null) simulator.pauseLogging();
				}
				logArea.setVisible(false);
				delayInt=delay;
				running=true;
				buttonStep.setEnabled(false);
				buttonPlayPause.setText(Language.tr("Animation.Toolbar.Pause"));
				buttonPlayPause.setToolTipText(Language.tr("Animation.Toolbar.Pause.Info"));
				buttonPlayPause.setIcon(Images.ANIMATION_PAUSE.getIcon());
				if (simulator!=null) simulator.resumeExecution();

				timer=new Timer("AnimationCancelCheck",false);
				timer.schedule(new UpdateInfoTask(),100);
			}
		} finally {
			simulatorLock.release();
		}
	}

	private volatile boolean stepLogChanged;

	private Semaphore stepLock=new Semaphore(1);

	private void multiSingleCoreSteps(final boolean fullRecording) {
		stepInt(false,fullRecording);
		SwingUtilities.invokeLater(()->{
			if (!stepLogChanged && simulator!=null) multiSingleCoreSteps(fullRecording);
		});
	}

	/**
	 * Führt einen Animationsschritt aus.
	 * @param fullRecording	Modus zur vollständigen Erfassung der Animationsdaten.
	 * @see ModelSurfaceAnimatorBase#getAnimationStepInfo(long, simulator.RunModel, List, List)
	 */
	public void step(final boolean fullRecording) {
		final boolean multiCore=setup.useMultiCoreAnimation;

		if (!multiCore) {
			multiSingleCoreSteps(fullRecording);
		} else {
			if (!stepLock.tryAcquire()) return;
			new Thread(()->{
				try {stepInt(true,fullRecording);} finally {stepLock.release();}
			},"AnimationStepper").start();
		}
	}

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

	private void finishAsSimulation() {
		continueAsSimulation=true;
		if (!running) playPause();

		surfaceAnimator.setRecordSystem(null,false);
		if (timer!=null) timer.cancel();
		simulationSuccessful=false;
		if (encoder!=null) {
			encoder.done();
			if (encoder instanceof MJPEGSystem) new AnimationRecordWaitDialog(this,(MJPEGSystem)encoder);
			encoder=null;
		}

		if (animationDone!=null) SwingUtilities.invokeLater(animationDone);
		if (sendToSimulation!=null) SwingUtilities.invokeLater(sendToSimulation);
	}

	private File getNextScreenshotFile(final String path) {
		final File folder;
		if (path==null || path.trim().isEmpty()) {
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

	private void saveScreenshot() {
		final File file=getNextScreenshotFile(setup.imagePathAnimation);
		if (file==null) return;
		surfacePanel.saveImageToFile(file,"png",setup.imageSize,setup.imageSize);
	}

	/**
	 * Erstellt einen Screenshot im aktuellen Animationsstatus und liefert diesen als Bild-Objekt zurück
	 * @return	Screenshot im aktuellen Animationsstatus
	 */
	public BufferedImage getScreenshot() {
		return surfacePanel.getImageMaxSize(-1,-1);
	}

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
			logText.append("<br>");
			logTextPlain.append("\n");
		}
		final String colorCode;
		if (data.color==null || data.color.equals(Color.BLACK)) {
			colorCode="FFFFFF";
		} else {
			colorCode=Integer.toHexString(data.color.getRed())+Integer.toHexString(data.color.getGreen())+Integer.toHexString(data.color.getBlue());
		}
		logText.append(data.time+": <b><span style=\"background-color: #"+colorCode+";\">&nbsp; "+data.event+" &nbsp;</span></b> "+data.info);
		logTextPlain.append(data.time+": "+data.event+" - "+data.info);
		final String message="<html><body>"+logText.toString()+"</body></html>";
		final String messagePlain=logTextPlain.toString();
		logLabel.setText(message);

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

	private void copyLogMessage() {
		final Clipboard clipboard=Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(new StringSelection(logTextHistoryPlain.get(logTextDisplayIndex)),null);
	}

	private void commandAnimationStartMode(final boolean paused) {
		if (setup.animationStartPaused==paused) return;
		setup.animationStartPaused=paused;
		setup.saveSetup();
	}

	private void commandAnalogValuesSlow(final boolean useSlowModeAnimation) {
		if (setup.useSlowModeAnimation==useSlowModeAnimation) return;
		setup.useSlowModeAnimation=useSlowModeAnimation;
		setup.saveSetup();
	}

	private void commandScreenshotModeHome() {
		setup.imagePathAnimation="";
		setup.saveSetup();
		updateScreenshotButtonHint();
	}

	private void commandScreenshotModeCustom() {
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
		updateScreenshotButtonHint();
	}

	private void updateScreenshotButtonHint() {
		String folder=FileSystemView.getFileSystemView().getHomeDirectory().toString();
		if (setup.imagePathAnimation!=null && !setup.imagePathAnimation.trim().isEmpty()) folder=setup.imagePathAnimation.trim();
		buttonScreenshot.setToolTipText(Language.tr("Animation.Toolbar.Image.Info")+" ("+Language.tr("Animation.Toolbar.Image.Info.Folder")+": "+folder+")");
	}

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
	 * Stellt ein Unter-Animator-Element ein, welches ebenfalls bei Animationsschritten benachrichtigt werden soll
	 * @param subViewer	Unter-Animator-Element (kann auch <code>null</code> sein, wenn kein zusätzliches Element benachrichtigt werden soll)
	 */
	public void setSubViewer(final RunModelAnimationViewer subViewer) {
		this.subViewer=subViewer;
	}

	/**
	 * Liefert den momentan gewählten Delay-Wert für die Übertragung an Unter-Animator-Elemente
	 * @return	Aktueller Delay-Wert
	 */
	public int getDelayIntern() {
		return delayInt;
	}

	private String lastCaluclationExpression=null;
	private String lastCaluclationJavaScript=null;
	private String lastCaluclationJava=null;
	private int lastCaluclationTab=0;

	private void calcExpression() {
		final ExpressionCalculatorDialog dialog=new ExpressionCalculatorDialog(
				this,
				simulator.getEditModel(),
				s->calculateExpression(s),
				s->runJavaScript(s),
				s->runJava(s),
				lastCaluclationTab,
				lastCaluclationExpression,
				lastCaluclationJavaScript,
				lastCaluclationJava
				);
		dialog.setVisible(true);
		lastCaluclationTab=dialog.getLastMode();
		lastCaluclationExpression=dialog.getLastExpression();
		lastCaluclationJavaScript=dialog.getLastJavaScript();
		lastCaluclationJava=dialog.getLastJava();
	}

	private void showJSResults() {
		new AnimationJSInfoDialog(this,logger.getJSData());
	}

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

	private class ToolBarListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			final Object source=e.getSource();
			if (source==buttonZoomOut) {surfacePanel.zoomOut(); zoomChanged(); return;}
			if (source==buttonZoomIn) {surfacePanel.zoomIn(); zoomChanged(); return;}
			if (source==buttonZoomDefault) {surfacePanel.zoomDefault(); zoomChanged(); return;}
			if (source==buttonFindModel) {surfacePanel.centerModel(); return;}
			if (source==buttonAbort) {closeRequest(); buttonAbort.setEnabled(false); return;}
			if (source==buttonScreenshot) {saveScreenshot(); return;}
			if (source==buttonSimulation) {finishAsSimulation(); return;}
			if (source==buttonTools) {animationToolsPopup(); return;}
			if (source==buttonPlayPause) {playPause(); return;}
			if (source==buttonStep) {step(false);}
			if (source==buttonSpeed) {animationSpeedPopup(); return;}
			if (source==menuStartModeRun) {commandAnimationStartMode(false); return;}
			if (source==menuAnalogValuesFast) {commandAnalogValuesSlow(false); return;}
			if (source==menuAnalogValuesExact) {commandAnalogValuesSlow(true); return;}
			if (source==menuStartModePause) {commandAnimationStartMode(true); return;}
			if (source==menuScreenshotModeHome) {commandScreenshotModeHome(); return;}
			if (source==menuScreenshotModeCustom) {commandScreenshotModeCustom(); return;}
			if (source==menuShowLog) {toggleShowSingleStepLogData(); return;}
			if (source==buttonProperties) {showModelPropertiesDialog(); return;}
			if (source==logPrevious) {displayLogMessage(-1); return;}
			if (source==logNext) {displayLogMessage(1); return;}
			if (source==logCurrent) {displayLogMessage(0); return;}
			if (source==logCopy) {copyLogMessage(); return;}
			if (source==logExpression) {calcExpression(); return;}
			if (source==logJS) {showJSResults(); return;}
			if (source==logEvents) {showEventslist(); return;}
		}
	}
}
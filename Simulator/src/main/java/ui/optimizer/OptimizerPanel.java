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
package ui.optimizer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import gitconnect.GitSetup;
import gitconnect.GitTools;
import language.Language;
import mathtools.NumberTools;
import mathtools.Table;
import mathtools.TableChart;
import mathtools.distribution.swing.CommonVariables;
import mathtools.distribution.tools.FileDropper;
import mathtools.distribution.tools.FileDropperData;
import simulator.editmodel.EditModel;
import simulator.statistics.Statistics;
import systemtools.BaseDialog;
import systemtools.ImageTools;
import systemtools.MsgBox;
import systemtools.statistics.StatisticsBasePanel;
import tools.IconListCellRenderer;
import tools.Notifier;
import tools.SetupData;
import ui.ModelChanger;
import ui.help.Help;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;
import ui.optimizer.OptimizerBase.OptimizationRunResults;
import ui.optimizer.OptimizerSetup.ControlVariable;
import ui.statistics.ListPopup;
import ui.statistics.StatisticViewerFastAccessDialog;
import ui.tools.SpecialPanel;
import xml.XMLTools;

/**
 * Erm�glicht die Konfiguration und Durchf�hrung einer Optimierung
 * @author Alexander Herzog
 */
public class OptimizerPanel extends SpecialPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 2492864288739317550L;

	/** �bergeordnetes Fenster */
	private final Window owner;
	/** Statistikdaten bezogen auf einen kurzen Lauf �ber das angegebene Editor-Modell (zur Auswahl von XML-Elementen als Zielwerte) */
	private final Statistics miniStatistics;
	/** Editor-Modell auf dessen Basis die Optimierung durchgef�hrt werden soll */
	private final EditModel model;
	/** Pfad zur zugeh�rigen Modelldatei (als Basis f�r relative Pfade in Ausgabeelementen) */
	private final String editModelPath;

	/** Aktuelles Optimierer-Setup */
	private OptimizerSetup setup;

	/** Registerreiter f�r die verschiedenen Einstellungsbereiche */
	private JTabbedPane tabs;

	/** "Neu"-Schaltfl�che (um ein neues Optimierer-Setup anzulegen) */
	private final JButton newSetupButton;
	/** "Laden"-Schaltfl�che (um ein Optimierer-Setup zu laden) */
	private final JButton loadSetupButton;
	/** "Speichern"-Schaltfl�che (um das aktuelle Optimierer-Setup zu speichern) */
	private final JButton saveSetupButton;
	/** "Start"-Schaltfl�che */
	private final JButton startButton;
	/** "Hilfe"-Schaltfl�che */
	private final JButton helpButton;

	/** "Hinzuf�gen"-Schaltfl�che (f�r Kontrollvariable) */
	private JButton controlAdd;
	/** "Bearbeiten"-Schaltfl�che (f�r Kontrollvariable) */
	private JButton controlEdit;
	/** "L�schen"-Schaltfl�che (f�r Kontrollvariable) */
	private JButton controlDelete;
	/** "Nach oben"-Schaltfl�che (f�r Kontrollvariable) */
	private JButton controlUp;
	/** "Nach unten"-Schaltfl�che (f�r Kontrollvariable) */
	private JButton controlDown;
	/** "Nebenbedingungen"-Schaltfl�che (f�r Kontrollvariable) */
	private JButton controlConstrains;
	/** Liste mit Nebenbedingungen */
	private List<String> constrains;
	/** Listendarstellung der Kontrollvariablen */
	private JList<ControlVariable> controlList;
	/** Datenmodell f�r die Listendarstellung der Kontrollvariablen */
	private DefaultListModel<ControlVariable> controlListModel;

	/** Auswahl: Zielwert aus Statistik-XML-Element auslesen */
	private JRadioButton targetXMLTagSelect;
	/** Statistik-XML-Element f�r Zielwert */
	private JTextField targetXMLTagEdit;
	/** Statistik-XML-Element f�r Zielwert ausw�hlen */
	private JButton targetXMLTagButton;
	/** Auswahl: Zielwert aus Skript-Ergebnis */
	private JRadioButton targetScriptSelect;
	/** Zielwert-Skript Datei */
	private JTextField targetScriptEdit;
	/** Zielwert-Skript Datei ausw�hlen */
	private JButton targetScriptButton;
	/** Optimierungsziel: Zielwert minimieren */
	private JRadioButton valueMinimize;
	/** Optimierungsziel: Zielwert maximieren */
	private JRadioButton valueMaximize;
	/** Optimierungsziel: Zielwert in bestimmtem Bereich */
	private JRadioButton valueRange;
	/** Untere Grenze f�r den Zielwert-Bereich */
	private JTextField valueRangeMin;
	/** Obere Grenze f�r den Zielwert-Bereich */
	private JTextField valueRangeMax;

	/** Ausgabeverzeichnis f�r Statistikdateien */
	private JTextField statisticsEdit;
	/** Ausgabeverzeichnis f�r Statistikdateien ausw�hlen */
	private JButton statisticsButton;
	/** Optimierer-Kernel konfigurieren */
	private JButton kernelButton;
	/** Sollen alle Statistik-Ergebnisse gespeichert werden oder nur die Statistik des letzten Laufs */
	private JComboBox<String> statisticsSaveMode;
	/** Statusausgaben w�hrend der Optimierung */
	private JTextArea log;
	/** Daten f�r die grafische Anzeige des Optimierungsfortschritts */
	private XYSeriesCollection xydata;
	/** Render f�r die Daten f�r die grafische Anzeige des Optimierungsfortschritts */
	private XYLineAndShapeRenderer xyrenderer;
	/** Diagramm-Element f�r die grafische Anzeige des Optimierungsfortschritts */
	private JFreeChart chart;
	/** Diagramm f�r die grafische Anzeige des Optimierungsfortschritts */
	private XYPlot xyplot;
	/** Soll die y-Achse bei der grafischen Anzeige des Optimierungsfortschritts logarithmisch skaliert werden? */
	private JCheckBox logarithmicAxis;

	/** Name des gew�hlten Optimierungs-Kernels */
	private String optimizerName;
	/** Abbruch der einzelnen Simulationen nach einer bestimmten Anzahl an Sekunden (oder Werte &le;0 f�r kein Timeout) */
	public int timeoutSeconds;
	/** Seriell arbeitender Optimierer-Kernel: �nderungsgeschwindigkeit in Runde 1 */
	private double serialChangeSpeed1;
	/** Seriell arbeitender Optimierer-Kernel: �nderungsgeschwindigkeit in Runde 2 */
	private double serialChangeSpeed2;
	/** Seriell arbeitender Optimierer-Kernel: �nderungsgeschwindigkeit in Runde 3 */
	private double serialChangeSpeed3;
	/** Seriell arbeitender Optimierer-Kernel: �nderungsgeschwindigkeit ab Runde 4 */
	private double serialChangeSpeed4;
	/** Seriell arbeitender Optimierer-Kernel: Tempor�re Verschlechterungen akzeptieren? */
	private boolean serialSimulatedAnnealing;
	/** Genetischer Optimierer: Populationsgr��e */
	private int geneticPopulationSize;
	/** Genetischer Optimierer: Evolution�rer Druck */
	private double geneticEvolutionPressure;
	/** Genetischer Optimierer: Mutationsrate in Runde 1 */
	private double geneticChangeSpeed1;
	/** Genetischer Optimierer: Mutationsrate in Runde 2 */
	private double geneticChangeSpeed2;
	/** Genetischer Optimierer: Mutationsrate in Runde 3 */
	private double geneticChangeSpeed3;
	/** Genetischer Optimierer: Mutationsrate in Runde 4 */
	private double geneticChangeSpeed4;
	/** Genetischer Optimierer: Mutationsrate ab Runde 5 */
	private double geneticChangeSpeed5;

	/** Der eigentliche Optimierer */
	private OptimizerBase optimizer;

	/**
	 * Konstruktor der Klasse {@link OptimizerPanel}
	 * @param owner	�bergeordnetes Fenster
	 * @param model	Editor-Modell auf dessen Basis die Optimierung durchgef�hrt werden soll
	 * @param editModelPath	Pfad zur zugeh�rigen Modelldatei (als Basis f�r relative Pfade in Ausgabeelementen)
	 * @param miniStatistics	Statistikdaten bezogen auf einen kurzen Lauf �ber das angegebene Editor-Modell (zur Auswahl von XML-Elementen als Zielwerte)
	 * @param doneNotify	Wird aufgerufen, wenn sich das Panel schlie�en m�chte
	 */
	public OptimizerPanel(final Window owner, final EditModel model, final String editModelPath, final Statistics miniStatistics, final Runnable doneNotify) {
		super(doneNotify);
		this.owner=owner;
		this.model=model;
		this.editModelPath=editModelPath;
		this.miniStatistics=miniStatistics;

		newSetupButton=addUserButton(Language.tr("Optimizer.Toolbar.New"),Language.tr("Optimizer.Toolbar.New.Hint"),Images.OPTIMIZER_SETUP_NEW.getIcon());
		loadSetupButton=addUserButton(Language.tr("Optimizer.Toolbar.Load"),Language.tr("Optimizer.Toolbar.Load.Hint"),Images.OPTIMIZER_SETUP_LOAD.getIcon());
		saveSetupButton=addUserButton(Language.tr("Optimizer.Toolbar.Save"),Language.tr("Optimizer.Toolbar.Save.Hint"),Images.OPTIMIZER_SETUP_SAVE.getIcon());
		addSeparator();
		startButton=addUserButton(Language.tr("Optimizer.Toolbar.Start"),Language.tr("Optimizer.Toolbar.Start.Hint")+" (F5)",Images.OPTIMIZER_RUN.getIcon());
		addSeparator();
		addCloseButton();
		addSeparator();
		helpButton=addUserButton(Language.tr("Main.Toolbar.Help"),Language.tr("Main.Toolbar.Help.Hint"),Images.HELP.getIcon());

		initGUI();
		loadSetupToGUI(new OptimizerSetup());
		getSetupFromGUI(true);

		/* F1-Hotkey */
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F1"),"actionHelp");
		getActionMap().put("actionHelp",new AbstractAction("actionHelp") {
			/**
			 * Serialisierungs-ID der Klasse
			 * @see Serializable
			 */
			private static final long serialVersionUID = 1738622101739292954L;
			@Override public void actionPerformed(ActionEvent event) {Help.topicModal(OptimizerPanel.this,"Optimizer");}
		});
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F5"),"runOptimization");
		getActionMap().put("runOptimization",new AbstractAction("runOptimization") {
			/**
			 * Serialisierungs-ID der Klasse
			 * @see Serializable
			 */
			private static final long serialVersionUID=-9193841490973101886L;
			@Override public void actionPerformed(ActionEvent event) {userButtonClick(-1,startButton);}
		});
	}

	/**
	 * Initialisiert das Panel mit den Registerreitern darin.
	 */
	private void initGUI() {
		tabs=new JTabbedPane();
		add(tabs,BorderLayout.CENTER);

		Box vbox;
		JPanel tab, box, line, lines, sub;
		ButtonGroup buttonGroup;

		/* Tab "Kontrollvariablen" */

		tabs.add(Language.tr("Optimizer.Tab.ControlVariables"),tab=new JPanel(new BorderLayout()));
		tab=InfoPanel.addTopPanelAndGetNewContent(tab,InfoPanel.optimizerControlVariables);
		JToolBar toolbar=new JToolBar();
		toolbar.setFloatable(false);
		tab.add(toolbar,BorderLayout.NORTH);
		toolbar.add(controlAdd=new JButton(Language.tr("Optimizer.Tab.ControlVariables.Add")));
		controlAdd.setToolTipText(Language.tr("Optimizer.Tab.ControlVariables.Add.Hint"));
		controlAdd.setIcon(Images.EDIT_ADD.getIcon());
		controlAdd.addActionListener(new ButtonListener());
		toolbar.add(controlEdit=new JButton(Language.tr("Optimizer.Tab.ControlVariables.Edit")));
		controlEdit.setToolTipText(Language.tr("Optimizer.Tab.ControlVariables.Edit.Hint"));
		controlEdit.setIcon(Images.GENERAL_SETUP.getIcon());
		controlEdit.addActionListener(new ButtonListener());
		toolbar.add(controlDelete=new JButton(Language.tr("Optimizer.Tab.ControlVariables.Delete")));
		controlDelete.setToolTipText(Language.tr("Optimizer.Tab.ControlVariables.Delete.Hint"));
		controlDelete.setIcon(Images.EDIT_DELETE.getIcon());
		controlDelete.addActionListener(new ButtonListener());
		toolbar.addSeparator();

		toolbar.add(controlUp=new JButton());
		controlUp.setToolTipText(Language.tr("Optimizer.Tab.ControlVariables.MoveUp.Hint"));
		controlUp.setIcon(Images.ARROW_UP.getIcon());
		controlUp.addActionListener(new ButtonListener());

		toolbar.add(controlDown=new JButton());
		controlDown.setToolTipText(Language.tr("Optimizer.Tab.ControlVariables.MoveDown.Hint"));
		controlDown.setIcon(Images.ARROW_DOWN.getIcon());
		controlDown.addActionListener(new ButtonListener());
		toolbar.addSeparator();

		toolbar.add(controlConstrains=new JButton(Language.tr("Optimizer.Tab.ControlVariables.Constrains")));
		controlConstrains.setToolTipText(Language.tr("Optimizer.Tab.ControlVariables.Constrains.Hint"));
		controlConstrains.setIcon(Images.OPTIMIZER_CONSTRAIN.getIcon());
		controlConstrains.addActionListener(new ButtonListener());

		tab.add(new JScrollPane(controlList=new JList<>(controlListModel=new DefaultListModel<>())),BorderLayout.CENTER);
		controlList.setCellRenderer(new ControlListCellRenderer());
		controlList.addMouseListener(new MouseListener() {
			@Override public void mouseReleased(MouseEvent e) {}
			@Override public void mousePressed(MouseEvent e) {if (e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e)) {commandControlEdit(); e.consume(); return;}}
			@Override public void mouseExited(MouseEvent e) {}
			@Override public void mouseEntered(MouseEvent e) {}
			@Override public void mouseClicked(MouseEvent e) {}
		});
		controlList.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {}
			@Override public void keyReleased(KeyEvent e) {}
			@Override public void keyPressed(KeyEvent e) {
				if (e.getKeyCode()==KeyEvent.VK_INSERT) {commandControlAdd(); e.consume(); return;}
				if (e.getKeyCode()==KeyEvent.VK_ENTER) {commandControlEdit(); e.consume(); return;}
				if (e.getKeyCode()==KeyEvent.VK_DELETE) {commandControlDelete(); e.consume(); return;}
				if (e.getKeyCode()==KeyEvent.VK_UP && e.isControlDown()) {commandControlUp(); e.consume(); return;}
				if (e.getKeyCode()==KeyEvent.VK_DOWN && e.isControlDown()) {commandControlDown(); e.consume(); return;}
			}
		});
		controlList.addListSelectionListener(e->commandListSelectionChanged());

		/* Tab "Ziel" */

		tabs.add(Language.tr("Optimizer.Tab.Target"),tab=new JPanel(new BorderLayout()));
		tab=InfoPanel.addTopPanelAndGetNewContent(tab,InfoPanel.optimizerTarget);
		tab.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		tab.add(vbox=Box.createVerticalBox(),BorderLayout.NORTH);

		vbox.add(box=new JPanel());
		box.setLayout(new BoxLayout(box,BoxLayout.PAGE_AXIS));
		box.setBorder(BorderFactory.createTitledBorder(Language.tr("Optimizer.Tab.Target.Type")));

		box.add(line=new JPanel(new BorderLayout()));
		line.add(targetXMLTagSelect=new JRadioButton(Language.tr("Optimizer.Tab.Target.Type.XMLElement")+":"),BorderLayout.WEST);
		line.add(targetXMLTagEdit=new JTextField(),BorderLayout.CENTER);
		targetXMLTagEdit.setEditable(false);
		line.add(targetXMLTagButton=new JButton(Language.tr("Optimizer.Tab.Target.Type.XMLElement.Button")),BorderLayout.EAST);
		targetXMLTagButton.setToolTipText(Language.tr("Optimizer.Tab.Target.Type.XMLElement.Button.Hint"));
		targetXMLTagButton.setIcon(Images.OPTIMIZER_SELECT_XML.getIcon());
		targetXMLTagButton.addActionListener(new ButtonListener());

		box.add(line=new JPanel(new BorderLayout()));
		line.add(targetScriptSelect=new JRadioButton(Language.tr("Optimizer.Tab.Target.Type.Script")+":"),BorderLayout.WEST);
		line.add(targetScriptEdit=new JTextField(),BorderLayout.CENTER);
		ModelElementBaseDialog.addUndoFeature(targetScriptEdit);
		line.add(targetScriptButton=new JButton(""),BorderLayout.EAST);
		targetScriptButton.setToolTipText(Language.tr("Optimizer.Tab.Target.Type.Script.Button.Hint"));
		targetScriptButton.setIcon(Images.GENERAL_SELECT_FILE.getIcon());
		targetScriptButton.addActionListener(new ButtonListener());

		buttonGroup=new ButtonGroup();
		buttonGroup.add(targetXMLTagSelect);
		buttonGroup.add(targetScriptSelect);

		vbox.add(box=new JPanel());
		box.setLayout(new BoxLayout(box,BoxLayout.PAGE_AXIS));
		box.setBorder(BorderFactory.createTitledBorder(Language.tr("Optimizer.Tab.Target.Value")));

		box.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(valueMinimize=new JRadioButton(Language.tr("Optimizer.Tab.Target.Value.Minimize")));

		box.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(valueMaximize=new JRadioButton(Language.tr("Optimizer.Tab.Target.Value.Maximize")));

		box.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(valueRange=new JRadioButton(Language.tr("Optimizer.Tab.Target.Value.RangeA")+" "));
		line.add(valueRangeMin=new JTextField(10));
		ModelElementBaseDialog.addUndoFeature(valueRangeMin);
		valueRangeMin.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {getSetupFromGUI(true); valueRange.setSelected(true);}
			@Override public void keyReleased(KeyEvent e) {getSetupFromGUI(true); valueRange.setSelected(true);}
			@Override public void keyPressed(KeyEvent e) {getSetupFromGUI(true); valueRange.setSelected(true);}
		});
		line.add(new JLabel(" "+Language.tr("Optimizer.Tab.Target.Value.RangeB")+" "));
		line.add(valueRangeMax=new JTextField(10));
		ModelElementBaseDialog.addUndoFeature(valueRangeMax);
		valueRangeMax.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {getSetupFromGUI(true); valueRange.setSelected(true);}
			@Override public void keyReleased(KeyEvent e) {getSetupFromGUI(true); valueRange.setSelected(true);}
			@Override public void keyPressed(KeyEvent e) {getSetupFromGUI(true); valueRange.setSelected(true);}
		});
		line.add(new JLabel(Language.tr("Optimizer.Tab.Target.Value.RanceC")));

		buttonGroup=new ButtonGroup();
		buttonGroup.add(valueMinimize);
		buttonGroup.add(valueMaximize);
		buttonGroup.add(valueRange);

		/* Tab "Optimierung" */

		tabs.add(Language.tr("Optimizer.Tab.Optimization"),tab=new JPanel(new BorderLayout()));
		tab=InfoPanel.addTopPanelAndGetNewContent(tab,InfoPanel.optimizerOptimization);
		final JSplitPane split=new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		tab.add(split,BorderLayout.CENTER);

		final JPanel data=new JPanel(new BorderLayout());
		split.add(data);
		data.add(lines=new JPanel(),BorderLayout.NORTH);
		lines.setLayout(new BoxLayout(lines,BoxLayout.PAGE_AXIS));
		lines.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));

		lines.add(line=new JPanel(new BorderLayout()));
		line.add(new JLabel(Language.tr("Optimizer.Tab.Optimization.Folder")+": "),BorderLayout.WEST);
		line.add(statisticsEdit=new JTextField(),BorderLayout.CENTER);
		ModelElementBaseDialog.addUndoFeature(statisticsEdit);
		new FileDropper(statisticsEdit,new ButtonListener());
		sub=new JPanel();
		sub.setLayout(new BoxLayout(sub,BoxLayout.LINE_AXIS));
		line.add(sub,BorderLayout.EAST);
		sub.add(statisticsButton=new JButton());
		statisticsButton.setToolTipText(Language.tr("Optimizer.Tab.Optimization.Folder.Button.Hint"));
		statisticsButton.setIcon(Images.GENERAL_SELECT_FOLDER.getIcon());
		statisticsButton.addActionListener(new ButtonListener());

		sub.add(statisticsSaveMode=new JComboBox<>(new String[] {
				Language.tr("Optimizer.Tab.Optimization.SaveMode.All"),
				Language.tr("Optimizer.Tab.Optimization.SaveMode.Last")
		}));
		statisticsSaveMode.setRenderer(new IconListCellRenderer(new Images[]{
				Images.OPTIMIZER_SAVE_MODE_ALL,
				Images.OPTIMIZER_SAVE_MODE_LAST
		}));

		sub.add(kernelButton=new JButton(Language.tr("Optimizer.Tab.Optimization.Kernel")));
		kernelButton.setToolTipText(Language.tr("Optimizer.Tab.Optimization.Kernel.Hint"));
		kernelButton.setIcon(Images.OPTIMIZER_KERNEL.getIcon());
		kernelButton.addActionListener(new ButtonListener());

		data.add(new JScrollPane(log=new JTextArea()),BorderLayout.CENTER);
		log.setEditable(false);

		final JPanel graphics=new JPanel(new BorderLayout());
		split.add(graphics);
		initGraphics(graphics);
		graphics.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.SOUTH);
		sub.add(logarithmicAxis=new JCheckBox(Language.tr("Optimizer.Tab.Optimization.yAxisLogarithmic")));
		logarithmicAxis.addActionListener(e->updateDiagram());
		sub.add(Box.createHorizontalStrut(15));
		JButton button;
		sub.add(button=new JButton(Language.tr("Optimizer.Tab.Optimization.Step.Result.Unzoom")));
		button.setToolTipText(Language.tr("Optimizer.Tab.Optimization.Step.Result.Unzoom.Hint"));
		button.setIcon(Images.ZOOM.getIcon());
		button.addActionListener(e->updateDiagram());

		sub.add(button=new JButton(Language.tr("Optimizer.Tab.Optimization.Step.Result.Copy")));
		button.setToolTipText(Language.tr("Optimizer.Tab.Optimization.Step.Result.Copy.Hint"));
		button.setIcon(Images.EDIT_COPY.getIcon());
		button.addActionListener(e->copyPopup((JButton)e.getSource()));

		sub.add(button=new JButton(Language.tr("Optimizer.Tab.Optimization.Step.Result.Save")));
		button.setToolTipText(Language.tr("Optimizer.Tab.Optimization.Step.Result.Save.Hint"));
		button.setIcon(Images.GENERAL_SAVE.getIcon());
		button.addActionListener(e->savePopup((JButton)e.getSource()));

		split.setResizeWeight(0.5);

		/* Icons f�r Tabs setzen */

		tabs.setIconAt(0,Images.OPTIMIZER_PAGE_CONTROL_VARIABLE.getIcon());
		tabs.setIconAt(1,Images.OPTIMIZER_PAGE_TARGET.getIcon());
		tabs.setIconAt(2,Images.OPTIMIZER_PAGE_OPTIMIZATION.getIcon());

		commandListSelectionChanged();
	}

	/**
	 * Initialisiert die Ergebnisdiagramm-Ausgabe.
	 * @param parentPanel	Eltern-Panel in das das Diagramm eingebettet wird
	 * @see #initGUI()
	 */
	private void initGraphics(final JPanel parentPanel) {
		/* Plot */

		xydata=new XYSeriesCollection();

		chart=ChartFactory.createXYLineChart(null,Language.tr("Optimizer.Tab.Optimization.Step"),Language.tr("Optimizer.Tab.Optimization.TargetValue"),xydata,PlotOrientation.VERTICAL,true,false,false);
		xyplot=chart.getXYPlot();

		xyplot.setDomainGridlinesVisible(true);
		xyplot.setRangeGridlinesVisible(true);
		xyplot.setRangeGridlinePaint(Color.black);
		xyplot.setDomainGridlinePaint(Color.black);
		xyplot.setBackgroundPaint(new GradientPaint(1,0,new Color(0xFA,0xFA,0xFF),1,150,new Color(0xEA,0xEA,0xFF)));
		if (xyplot.getDomainAxis() instanceof NumberAxis) ((NumberAxis)xyplot.getDomainAxis()).setTickUnit(new NumberTickUnit(1));
		if (xyplot.getRenderer() instanceof XYLineAndShapeRenderer) xyrenderer=(XYLineAndShapeRenderer)xyplot.getRenderer();

		/* ChartPanel */

		final ChartPanel chartPanel=new ChartPanel(
				chart,
				ChartPanel.DEFAULT_WIDTH,
				ChartPanel.DEFAULT_HEIGHT,
				ChartPanel.DEFAULT_MINIMUM_DRAW_WIDTH,
				ChartPanel.DEFAULT_MINIMUM_DRAW_HEIGHT,
				ChartPanel.DEFAULT_MAXIMUM_DRAW_WIDTH,
				ChartPanel.DEFAULT_MAXIMUM_DRAW_HEIGHT,
				ChartPanel.DEFAULT_BUFFER_USED,
				true,
				false,
				true,
				true,
				true
				);
		chartPanel.setPopupMenu(null);

		chart.setBackgroundPaint(null);

		final Color textBackground=UIManager.getColor("TextField.background");
		final boolean isDark=(textBackground!=null && !textBackground.equals(Color.WHITE));
		if (isDark) {
			ValueAxis axis;
			axis=((XYPlot)chart.getPlot()).getDomainAxis();
			axis.setAxisLinePaint(Color.LIGHT_GRAY);
			axis.setLabelPaint(Color.LIGHT_GRAY);
			axis.setTickLabelPaint(Color.LIGHT_GRAY);
			axis.setTickMarkPaint(Color.LIGHT_GRAY);
			axis=((XYPlot)chart.getPlot()).getRangeAxis();
			axis.setAxisLinePaint(Color.LIGHT_GRAY);
			axis.setLabelPaint(Color.LIGHT_GRAY);
			axis.setTickLabelPaint(Color.LIGHT_GRAY);
			axis.setTickMarkPaint(Color.LIGHT_GRAY);
		}

		final TextTitle t=chart.getTitle();
		if (t!=null) {Font f=t.getFont(); t.setFont(new Font(f.getFontName(),Font.PLAIN,f.getSize()-4));}

		parentPanel.add(chartPanel,BorderLayout.CENTER);
	}

	/**
	 * Ergebnisse der letzten Optimierungsl�ufe f�r die Diagrammdarstellung.
	 * @see #updateDiagram()
	 */
	private List<OptimizationRunResults> lastResults;

	/**
	 * Aktualisiert die Diagrammdarstellung.
	 */
	private void updateDiagram() {
		final List<OptimizationRunResults> results;
		if (optimizer==null) {
			if (lastResults==null) return;
			results=lastResults;
		} else {
			results=lastResults=optimizer.getResults();
		}

		/* Alte Daten entfernen */
		xydata.removeAllSeries();

		/* Neue Daten vorhanden? */
		if (results.size()==0) return;

		/* x-Achse einstellen */
		xyplot.getDomainAxis().setRange(1-0.25,results.size()+0.25);

		/* y-Achse einstellen */
		if (logarithmicAxis.isSelected()) {
			xyplot.setRangeAxis(new LogarithmicAxis(Language.tr("Optimizer.Tab.Optimization.TargetValue")));
		} else {
			xyplot.setRangeAxis(new NumberAxis(Language.tr("Optimizer.Tab.Optimization.TargetValue")));
		}

		/* Maximalzahl an Werten pro Schritt */
		int maxValuesPerStep=0;
		for (OptimizationRunResults result: results) maxValuesPerStep=Math.max(maxValuesPerStep,result.values.length);

		/* Serien anlegen */
		XYSeries[] seriesGood=new XYSeries[maxValuesPerStep];
		XYSeries[] seriesBad=new XYSeries[maxValuesPerStep];
		for (int i=0;i<seriesGood.length;i++) {
			if (maxValuesPerStep==1) {
				seriesGood[i]=new XYSeries(Language.tr("Optimizer.Tab.Optimization.GoodResultSingle"));
				seriesBad[i]=new XYSeries(Language.tr("Optimizer.Tab.Optimization.BadResult"));
			} else {
				seriesGood[i]=new XYSeries(Language.tr("Optimizer.Tab.Optimization.GoodResult")+" "+(i+1));
				seriesBad[i]=new XYSeries(Language.tr("Optimizer.Tab.Optimization.BadResult")+" "+(i+1));
			}
			xydata.addSeries(seriesGood[i]);
			xydata.addSeries(seriesBad[i]);
		}

		/* Darstellung der Serien festlegen */
		if (xyrenderer!=null) for (int i=0;i<seriesGood.length;i++) {
			xyrenderer.setSeriesPaint(2*i,Color.BLUE);
			xyrenderer.setSeriesShapesVisible(2*i,true);
			xyrenderer.setSeriesLinesVisible(2*i,false);
			xyrenderer.setSeriesVisibleInLegend(2*i,false);
			xyrenderer.setSeriesShape(2*i,new Rectangle2D.Double(-5,-5,5,5));

			xyrenderer.setSeriesPaint(2*i+1,Color.RED);
			xyrenderer.setSeriesShapesVisible(2*i+1,true);
			xyrenderer.setSeriesLinesVisible(2*i+1,false);
			xyrenderer.setSeriesVisibleInLegend(2*i+1,false);
			xyrenderer.setSeriesShape(2*i+1,new Rectangle2D.Double(-5,-5,5,5));
		}

		/* Serien mit Daten f�llen */
		boolean goodValues=false;
		boolean badValues=false;
		for (int i=0;i<results.size();i++) {
			int good=0;
			int bad=0;
			final OptimizationRunResults result=results.get(i);
			for (int j=0;j<result.values.length;j++) {
				double value=result.values[j];
				if (logarithmicAxis.isSelected()) value=Math.max(0.001,value);
				if (result.usedForNextStep[j]) {
					goodValues=true;
					seriesGood[good++].add(i+1,value);
				} else {
					badValues=true;
					seriesBad[bad++].add(i+1,value);
				}
			}
		}
		if (xyrenderer!=null && goodValues && badValues) {
			xyrenderer.setSeriesVisibleInLegend(0,true);
			xyrenderer.setSeriesVisibleInLegend(1,true);
		}
	}

	/**
	 * Zeigt das Kopieren-Popupmen� an.
	 * @param button	Kopieren-Schaltfl�che zur Ausrichtung des Men�s.
	 */
	private void copyPopup(final JButton button) {
		final JPopupMenu popup=new JPopupMenu();
		JMenuItem item;

		popup.add(item=new JMenuItem(Language.tr("Optimizer.Tab.Optimization.Step.Result.Copy.Log")));
		item.setIcon(Images.OPTIMIZER_EXPORT_TEXT.getIcon());
		item.addActionListener(e->{
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(log.getText()),null);
		});

		popup.add(item=new JMenuItem(Language.tr("Optimizer.Tab.Optimization.Step.Result.Copy.Chart")));
		item.setIcon(Images.OPTIMIZER_EXPORT_CHART.getIcon());
		item.addActionListener(e->copyDiagram());

		popup.show(button,0,button.getHeight());
	}

	/**
	 * Zeigt das Speichern-Popupmen� an.
	 * @param button	Speichern-Schaltfl�che zur Ausrichtung des Men�s.
	 */
	private void savePopup(final JButton button) {
		final JPopupMenu popup=new JPopupMenu();
		JMenuItem item;

		popup.add(item=new JMenuItem(Language.tr("Optimizer.Tab.Optimization.Step.Result.Save.Log")));
		item.setIcon(Images.OPTIMIZER_EXPORT_TEXT.getIcon());
		item.addActionListener(e->{
			final JFileChooser fc=new JFileChooser();
			CommonVariables.initialDirectoryToJFileChooser(fc);
			fc.setDialogTitle(StatisticsBasePanel.viewersSaveText);
			FileFilter txt=new FileNameExtensionFilter(StatisticsBasePanel.fileTypeTXT+" (*.txt)","txt");
			fc.addChoosableFileFilter(txt);
			fc.setFileFilter(txt);
			fc.setAcceptAllFileFilterUsed(false);

			if (fc.showSaveDialog(owner)!=JFileChooser.APPROVE_OPTION) return;
			CommonVariables.initialDirectoryFromJFileChooser(fc);
			File file=fc.getSelectedFile();

			if (file.getName().indexOf('.')<0) {
				if (fc.getFileFilter()==txt) file=new File(file.getAbsoluteFile()+".txt");
			}

			if (file.exists()) {
				if (!MsgBox.confirmOverwrite(owner,file)) return;
			}

			if (!Table.saveTextToFile(log.getText(),file)) {
				MsgBox.error(this,Language.tr("Optimizer.Tab.Optimization.Step.Result.Save.ErrorTitle"),String.format(Language.tr("Optimizer.Tab.Optimization.Step.Result.Save.ErrorInfo"),file.toString()));
			}
		});

		popup.add(item=new JMenuItem(Language.tr("Optimizer.Tab.Optimization.Step.Result.Save.Table")));
		item.setIcon(Images.OPTIMIZER_EXPORT_TABLE.getIcon());
		item.addActionListener(e->{
			final File file=Table.showSaveDialog(owner,Language.tr("Optimizer.Tab.Optimization.Step.Result.Save.Table.Title"));
			if (file==null) return;

			if (file.exists()) {
				if (!MsgBox.confirmOverwrite(owner,file)) return;
			}

			final Table table=getFullTable();
			if (!table.save(file)) {
				MsgBox.error(this,Language.tr("Optimizer.Tab.Optimization.Step.Result.Save.ErrorTitle"),String.format(Language.tr("Optimizer.Tab.Optimization.Step.Result.Save.ErrorInfo"),file.toString()));
			}
		});

		popup.add(item=new JMenuItem(Language.tr("Optimizer.Tab.Optimization.Step.Result.Save.Chart")));
		item.setIcon(Images.OPTIMIZER_EXPORT_CHART.getIcon());
		item.addActionListener(e->saveDiagram());

		popup.show(button,0,button.getHeight());
	}

	/**
	 * Befehl: Diagramm kopieren
	 * @see #copyPopup(JButton)
	 */
	private void copyDiagram() {
		final int imageSize=Math.max(100,SetupData.getSetup().imageSize);
		final BufferedImage image=chart.createBufferedImage(imageSize,imageSize);
		ImageTools.copyImageToClipboard(image);
	}

	/**
	 * Befehl: Diagramm speichern
	 * @see #savePopup(JButton)
	 */
	private void saveDiagram() {
		final File file=ImageTools.showSaveDialog(this,true);
		if (file==null) return;

		if (!saveDiagram(file)) {
			MsgBox.error(owner,StatisticsBasePanel.viewersSaveImageErrorTitle,String.format(StatisticsBasePanel.viewersSaveImageErrorInfo,file.toString()));
		}
	}

	/**
	 * Extrahiert eine Datentabelle basierend auf den Diagrammdaten.
	 * @return	Datentabelle mit den Optimierungsergebnissen
	 * @see #xydata
	 */
	private Table getTableFromChart() {
		final Table table=new Table();

		final int count=xydata.getSeriesCount();
		final String[] names=new String[count];
		for (int i=0;i<count;i++) names[i]=xydata.getSeries(i).getKey().toString();

		final int itemCount=xydata.getSeries(0).getItemCount();
		final List<String> categories=new ArrayList<>(1+itemCount);
		final List<List<String>> lines=new ArrayList<>();
		final boolean[] lineInUse=new boolean[names.length];
		Arrays.fill(lineInUse,false);

		categories.add("");
		for (int i=0;i<names.length;i++) {
			final List<String> line=new ArrayList<>(1+itemCount);
			line.add((names[i]==null)?"":names[i]);
			lines.add(line);
		}

		for (int i=0;i<itemCount;i++) {
			categories.add(NumberTools.formatNumberMax(xydata.getSeries(0).getDataItem(i).getXValue()));
			for (int j=0;j<count;j++) {
				final XYSeries series=xydata.getSeries(j);
				if (series.getItemCount()>i) {
					lines.get(j).add(NumberTools.formatNumberMax(series.getDataItem(i).getYValue()));
					lineInUse[j]=true;
				} else {
					lines.get(j).add("");
				}
			}
		}

		table.addLine(categories);
		for (int i=0;i<lines.size();i++) if (lineInUse[i]) table.addLine(lines.get(i));

		return table.transpose(true);
	}

	/**
	 * Liefert eine Tabelle mit allen Eingabe- und Ausgabeparametern.
	 * @return	Tabelle mit allen Eingabe- und Ausgabeparametern
	 */
	private Table getFullTable() {
		final Table table=new Table();

		if (lastResults!=null && lastResults.size()>0) {
			final OptimizationRunResults round0=lastResults.get(0);
			final int modelCount=round0.values.length;
			final int inputCount=round0.input[0].length;
			/* �berschrift */
			final List<String> heading=new ArrayList<>();
			heading.add(Language.tr("Optimizer.Tab.Optimization.Round"));
			if (modelCount==1) {
				for (int j=0;j<inputCount;j++) heading.add(Language.tr("Optimizer.Tab.Optimization.ControlVariable")+" "+(j+1));
				heading.add(Language.tr("Optimizer.Tab.Optimization.GoodResultSingle"));
			} else {
				for (int i=0;i<modelCount;i++) {
					for (int j=0;j<inputCount;j++) heading.add(Language.tr("Optimizer.Tab.Optimization.ControlVariable")+" "+(j+1)+" ("+Language.tr("Optimizer.Tab.Optimization.Model")+" "+(i+1)+")");
					heading.add(Language.tr("Optimizer.Tab.Optimization.GoodResultSingle")+" ("+Language.tr("Optimizer.Tab.Optimization.Model")+" "+(i+1)+")");
				}
			}
			table.addLine(heading);
			/* Zeilen f�r die einzelnen Runden */
			for (int i=0;i<lastResults.size();i++) {
				final OptimizationRunResults round=lastResults.get(i);
				final List<String> line=new ArrayList<>();
				line.add(""+(i+1));
				for (int j=0;j<modelCount;j++) {
					for (double inputValue: round.input[j]) line.add(NumberTools.formatNumberMax(inputValue));
					line.add(NumberTools.formatNumberMax(round.values[j]));
				}
				table.addLine(line);
			}
		}

		return table;
	}

	/**
	 * Extrahiert eine Datentabelle mit integriertem Diagramm basierend auf den Diagrammdaten.
	 * @return	Datentabelle mit den Optimierungsergebnissen mit integriertem Diagramm
	 * @see #xydata
	 */
	private TableChart getTableChartFromChart() {
		final TableChart tableChart=new TableChart(getTableFromChart());

		tableChart.setupAxis(xyplot.getDomainAxis().getLabel(),xyplot.getRangeAxis().getLabel());
		tableChart.setupChart(TableChart.ChartMode.LINE);

		return tableChart;
	}

	/**
	 * Befehl: Diagramm speichern
	 * @param file	Ausgabedateiname
	 * @return	Liefert im Erfolgsfall <code>true</code> zur�ck.
	 */
	private boolean saveDiagram(final File file) {
		return ImageTools.saveChart(this,chart,file,SetupData.getSetup().imageSize,()->getTableChartFromChart());
	}

	/**
	 * L�dt die Optimierereinstellungen aus einem Setup-Objekt in die Programmoberfl�che
	 * @param setup	Zu ladende Optimierereinstellungen
	 * @see #getSetupFromGUI(boolean)
	 */
	private void loadSetupToGUI(final OptimizerSetup setup) {
		this.setup=setup;

		constrains=new ArrayList<>();
		constrains.addAll(setup.controlVariableConstrains);

		controlListModel.clear();
		for (ControlVariable controlVariable: setup.controlVariables) controlListModel.addElement(controlVariable.clone());

		switch (setup.targetType) {
		case TARGET_TYPE_XML_TAG:
			targetXMLTagEdit.setText(setup.target);
			targetScriptEdit.setText("");
			targetXMLTagSelect.setSelected(true);
			break;
		case TARGET_TYPE_SCRIPT:
			targetXMLTagEdit.setText("");
			targetScriptEdit.setText(setup.target);
			targetScriptSelect.setSelected(true);
			break;
		}
		switch (setup.targetDirection) {
		case -1:
			valueMinimize.setSelected(true);
			break;
		case 0:
			valueRange.setSelected(true);
			valueRangeMin.setText(NumberTools.formatNumber(setup.targetRangeMin));
			valueRangeMax.setText(NumberTools.formatNumber(setup.targetRangeMax));
			break;
		case 1:
			valueMaximize.setSelected(true);
			break;
		}
		statisticsEdit.setText(setup.outputFolder);
		switch (setup.outputMode) {
		case OUTPUT_ALL: statisticsSaveMode.setSelectedIndex(0); break;
		case OUTPUT_LAST: statisticsSaveMode.setSelectedIndex(1); break;
		}

		optimizerName=setup.optimizerName;
		timeoutSeconds=setup.timeoutSeconds;
		serialChangeSpeed1=setup.serialChangeSpeed1;
		serialChangeSpeed2=setup.serialChangeSpeed2;
		serialChangeSpeed3=setup.serialChangeSpeed3;
		serialChangeSpeed4=setup.serialChangeSpeed4;
		serialSimulatedAnnealing=setup.serialSimulatedAnnealing;
		geneticPopulationSize=setup.geneticPopulationSize;
		geneticEvolutionPressure=setup.geneticEvolutionPressure;
		geneticChangeSpeed1=setup.geneticChangeSpeed1;
		geneticChangeSpeed2=setup.geneticChangeSpeed2;
		geneticChangeSpeed3=setup.geneticChangeSpeed3;
		geneticChangeSpeed4=setup.geneticChangeSpeed4;
		geneticChangeSpeed5=setup.geneticChangeSpeed5;

		commandListSelectionChanged();
	}

	/**
	 * Schreibt die Optimierereinstellungen aus der Programmoberfl�che in ein neues Optimierereinstellungen-Objekt
	 * @param ignoreErrors	Sollen im Fall von ung�ltigen Einstellungen in der Oberfl�che Standardwerte verwendet werden (<code>true</code>) oder soll <code>null</code> zur�ckgeliefert werden (<code>false</code>)
	 * @return	Neues Optimierereinstellungen-Objekt (kann im Fall, wenn Fehler nicht ignoriert werden sollen, <code>null</code> sein)
	 * @see #loadSetupToGUI(OptimizerSetup)
	 */
	private OptimizerSetup getSetupFromGUI(final boolean ignoreErrors) {
		final OptimizerSetup setup=new OptimizerSetup();

		setup.controlVariableConstrains.clear();
		if (constrains!=null) setup.controlVariableConstrains.addAll(constrains);

		for (int i=0;i<controlListModel.size();i++) setup.controlVariables.add(controlListModel.get(i).clone());

		if (targetXMLTagSelect.isSelected()) {
			setup.targetType=OptimizerSetup.TargetType.TARGET_TYPE_XML_TAG;
			setup.target=targetXMLTagEdit.getText();
		} else {
			setup.targetType=OptimizerSetup.TargetType.TARGET_TYPE_SCRIPT;
			setup.target=targetScriptEdit.getText();
		}
		if (valueMinimize.isSelected()) setup.targetDirection=-1;
		if (valueRange.isSelected()) {
			setup.targetDirection=0;
			Double D1=NumberTools.getDouble(valueRangeMin,true);
			Double D2=NumberTools.getDouble(valueRangeMax,true);
			if (D1==null) {
				if (ignoreErrors) {
					D1=setup.targetRangeMin;
				} else {
					MsgBox.error(this,Language.tr("Optimizer.Tab.Target.Value.Range.InvalidRange"),Language.tr("Optimizer.Tab.Target.Value.Range.InvalidRange.LowerBound"));
					return null;
				}
			}
			if (D2==null) {
				if (ignoreErrors) {
					D2=setup.targetRangeMax;
				} else {
					MsgBox.error(this,Language.tr("Optimizer.Tab.Target.Value.Range.InvalidRange"),Language.tr("Optimizer.Tab.Target.Value.Range.InvalidRange.UpperBound"));
					return null;
				}
			}
			double d1=D1;
			double d2=D2;
			if (d2<d1) {double d=d1; d1=d2; d2=d;}
			setup.targetRangeMin=d1;
			setup.targetRangeMax=d2;
		}
		if (valueMaximize.isSelected()) setup.targetDirection=1;

		setup.outputFolder=statisticsEdit.getText();
		switch (statisticsSaveMode.getSelectedIndex()) {
		case 0: setup.outputMode=OptimizerSetup.OutputMode.OUTPUT_ALL; break;
		case 1: setup.outputMode=OptimizerSetup.OutputMode.OUTPUT_LAST; break;
		}

		setup.optimizerName=optimizerName;
		setup.timeoutSeconds=timeoutSeconds;
		setup.serialChangeSpeed1=serialChangeSpeed1;
		setup.serialChangeSpeed2=serialChangeSpeed2;
		setup.serialChangeSpeed3=serialChangeSpeed3;
		setup.serialChangeSpeed4=serialChangeSpeed4;
		setup.serialSimulatedAnnealing=serialSimulatedAnnealing;
		setup.geneticPopulationSize=geneticPopulationSize;
		setup.geneticEvolutionPressure=geneticEvolutionPressure;
		setup.geneticChangeSpeed1=geneticChangeSpeed1;
		setup.geneticChangeSpeed2=geneticChangeSpeed2;
		setup.geneticChangeSpeed3=geneticChangeSpeed3;
		setup.geneticChangeSpeed4=geneticChangeSpeed4;
		setup.geneticChangeSpeed5=geneticChangeSpeed5;

		return setup;
	}

	/**
	 * D�rfen die Einstellungen verworfen werden (ggf. nach Nutzerr�ckfrage)?
	 * @return	Liefert <code>true</code>, wenn die Daten verworfen werden d�rfen
	 */
	private boolean allowDispose() {
		final OptimizerSetup newSetup=getSetupFromGUI(true);
		if (setup.equalsOptimizerSetup(newSetup)) return true;
		switch (MsgBox.confirmSave(this,Language.tr("Optimizer.Settings.Discard.Title"),Language.tr("Optimizer.Settings.Discard"))) {
		case JOptionPane.YES_OPTION:
			return saveSetup(newSetup);
		case JOptionPane.NO_OPTION:
			return true;
		case JOptionPane.CANCEL_OPTION:
			return false;
		default:
			return false;
		}
	}

	@Override
	public void requestClose() {
		if (optimizer!=null) {
			optimizer.cancel();
			return;
		}

		if (!allowDispose()) return;
		close();
	}

	/**
	 * F�gt eine Zeile an die Status-Logging-Ausgabe an.
	 * @param line	Auszugebende Zeile
	 */
	private void addStatusLine(String line) {
		if (log.getText().isEmpty()) log.setText(line); else log.setText(log.getText()+"\n"+line);
		log.setCaretPosition(log.getText().length());
	}

	/**
	 * Schaltet die GUI-Schaltfl�chen aktiv oder inaktiv
	 * je nach dem ob die Optimierung gerade l�uft oder nicht
	 * @param run	L�uft die Optimierung gerade
	 */
	private void setGUIRunMode(final boolean run) {
		if (run) {
			startButton.setText(Language.tr("Optimizer.Toolbar.Stop"));
			startButton.setToolTipText(Language.tr("Optimizer.Toolbar.Stop.Hint")+" (F5)");
			startButton.setIcon(Images.GENERAL_CANCEL.getIcon());
		} else {
			startButton.setText(Language.tr("Optimizer.Toolbar.Start"));
			startButton.setToolTipText(Language.tr("Optimizer.Toolbar.Start.Hint")+" (F5)");
			startButton.setIcon(Images.OPTIMIZER_RUN.getIcon());
		}

		newSetupButton.setEnabled(!run);
		loadSetupButton.setEnabled(!run);
		saveSetupButton.setEnabled(!run);

		controlAdd.setEnabled(!run);
		controlEdit.setEnabled(!run);
		controlDelete.setEnabled(!run);
		controlUp.setEnabled(!run);
		controlDown.setEnabled(!run);
		controlList.setEnabled(!run);

		targetXMLTagSelect.setEnabled(!run);
		targetXMLTagButton.setEnabled(!run);
		targetScriptSelect.setEnabled(!run);
		targetScriptEdit.setEnabled(!run);
		targetScriptButton.setEnabled(!run);
		valueMinimize.setEnabled(!run);
		valueMaximize.setEnabled(!run);
		valueRange.setEnabled(!run);
		valueRangeMin.setEnabled(!run);
		valueRangeMax.setEnabled(!run);

		statisticsEdit.setEnabled(!run);
		statisticsButton.setEnabled(!run);
		statisticsSaveMode.setEnabled(!run);
		kernelButton.setEnabled(!run);

		if (run) {
			tabs.setSelectedIndex(2);
			log.setText("");
		}
		if (!run) optimizer=null;

		setWaitIndicatorVisible(run);
	}

	/**
	 * Befehl: Optimierereinstellungen laden
	 * @return	Liefert <code>true</code>, wenn die Einstellungen geladen werden konnten
	 */
	private boolean loadSetup() {
		final File file=XMLTools.showLoadDialog(getParent(),Language.tr("Optimizer.Settings.Load"));
		if (file==null) return false;

		OptimizerSetup setup=new OptimizerSetup();
		final String error=setup.loadFromFile(file);
		if (error!=null) {
			MsgBox.error(this,Language.tr("Optimizer.Settings.Load.Error"),error);
			return false;
		}

		loadSetupToGUI(setup);
		return true;
	}

	/**
	 * Befehl: Optimierereinstellungen speichern
	 * @param setup	Zu speichernde Optimierereinstellungen
	 * @return	Liefert <code>true</code>, wenn die Einstellungen gespeichert werden konnten
	 */
	private boolean saveSetup(final OptimizerSetup setup) {
		final File file=XMLTools.showSaveDialog(getParent(),Language.tr("Optimizer.Settings.Save"),SetupData.getSetup().defaultSaveFormatOptimizerSetups);
		if (file==null) return false;

		if (file.exists()) {
			if (!MsgBox.confirmOverwrite(owner,file)) return false;
		}

		if (!setup.saveToFile(file)) {
			MsgBox.error(this,Language.tr("Optimizer.Settings.Save.Error.Title"),String.format(Language.tr("Optimizer.Settings.Save.Error.Info"),file.toString()));
			return false;
		}

		GitTools.saveFile(this,model.author,model.authorEMail,file,GitSetup.GitSaveMode.OPTIMIZATION_SETUPS);

		this.setup=setup;
		return true;
	}

	/**
	 * Befehl: Kontrollvariable hinzuf�gen
	 */
	private void commandControlAdd() {
		OptimizerPanelControlVariableDialog dialog=new OptimizerPanelControlVariableDialog(this,model,null,()->Help.topicModal(OptimizerPanel.this,"Optimizer"));
		dialog.setVisible(true);
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			controlListModel.addElement(dialog.getControlVariable());
		}
	}

	/**
	 * Befehl: Kontrollvariable bearbeiten
	 */
	private void commandControlEdit() {
		if (controlList.getSelectedIndex()<0) return;
		OptimizerPanelControlVariableDialog dialog=new OptimizerPanelControlVariableDialog(this,model,controlListModel.get(controlList.getSelectedIndex()),()->Help.topicModal(OptimizerPanel.this,"Optimizer"));
		dialog.setVisible(true);
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			controlListModel.set(controlList.getSelectedIndex(),dialog.getControlVariable());
		}
	}

	/**
	 * Befehl: Kontrollvariable l�schen
	 */
	private void commandControlDelete() {
		if (controlList.getSelectedIndex()<0) return;
		controlListModel.remove(controlList.getSelectedIndex());
	}

	/**
	 * Befehl: Kontrollvariable in der Liste nach oben verschieben
	 */
	private void commandControlUp() {
		if (controlList.getSelectedIndex()<1) return;
		final ControlVariable temp=controlListModel.get(controlList.getSelectedIndex()-1);
		controlListModel.set(controlList.getSelectedIndex()-1,controlListModel.get(controlList.getSelectedIndex()));
		controlListModel.set(controlList.getSelectedIndex(),temp);
		controlList.setSelectedIndex(controlList.getSelectedIndex()-1);
	}

	/**
	 * Befehl: Kontrollvariable in der Liste nach unten verschieben
	 */
	private void commandControlDown() {
		if (controlList.getSelectedIndex()<0 || controlList.getSelectedIndex()==controlListModel.size()-1) return;
		final ControlVariable temp=controlListModel.get(controlList.getSelectedIndex()+1);
		controlListModel.set(controlList.getSelectedIndex()+1,controlListModel.get(controlList.getSelectedIndex()));
		controlListModel.set(controlList.getSelectedIndex(),temp);
		controlList.setSelectedIndex(controlList.getSelectedIndex()+1);
	}

	/**
	 * Befehl: Nebenbedingungen konfigurieren
	 * @see OptimizerPanelConstrainsDialog
	 */
	private void commandControlSetupConstrains() {
		if (constrains==null) constrains=new ArrayList<>();
		new OptimizerPanelConstrainsDialog(this,model,constrains);
	}

	/**
	 * Befehl: XML-Anweisung f�r Zielwert aus Popup ausw�hlen
	 */
	private final void commandSelectXMLPopup() {
		final JPopupMenu popupMenu=new JPopupMenu();

		final JMenuItem item=new JMenuItem(Language.tr("Optimizer.Tab.Target.Type.XMLElement.Button.ByXML"));
		item.setToolTipText(Language.tr("Optimizer.Tab.Target.Type.XMLElement.Button.ByXML.Hint"));
		item.setIcon(Images.OPTIMIZER_SELECT_XML.getIcon());
		item.addActionListener(e->commandSelectXML());

		popupMenu.add(item);
		popupMenu.addSeparator();
		final ListPopup helper=new ListPopup(targetXMLTagButton,null);
		helper.popupCustom(popupMenu,miniStatistics,cmd->{
			targetXMLTagEdit.setText(cmd.xml);
			targetXMLTagSelect.setSelected(true);
		},null);

		popupMenu.show(targetXMLTagButton,0,targetXMLTagButton.getHeight());
	}

	/**
	 * Befehl: XML-Anweisung f�r Zielwert �ber Dialog ausw�hlen
	 * @see #commandSelectXMLPopup()
	 */
	private final void commandSelectXML() {
		final org.w3c.dom.Document xmlDoc=miniStatistics.saveToXMLDocument(); /* Da es hier um eine Abbruch- bzw. Zielbedingung geht, muss die Ergebnis-xml-Datei verwendet werden. */
		if (xmlDoc==null) return;
		final Runnable helpRunnable=()->Help.topicModal(OptimizerPanel.this,"Optimizer");
		final StatisticViewerFastAccessDialog dialog=new StatisticViewerFastAccessDialog(owner,xmlDoc,helpRunnable,true);
		dialog.setVisible(true);
		if (dialog.getClosedBy()!=BaseDialog.CLOSED_BY_OK) return;
		targetXMLTagEdit.setText(dialog.getXMLSelector());
		targetXMLTagSelect.setSelected(true);
	}

	/**
	 * Aktiviert oder deaktiviert Schaltfl�chen je nach dem
	 * ob und wenn ja welcher Eintrag in der Liste der
	 * Kontrollvariablen selektiert ist.
	 * @see #controlList
	 * @see #controlEdit
	 * @see #controlDelete
	 * @see #controlUp
	 * @see #controlDown
	 */
	private final void commandListSelectionChanged() {
		controlEdit.setEnabled(controlList.getSelectedIndex()>=0);
		controlDelete.setEnabled(controlList.getSelectedIndex()>=0);
		controlUp.setEnabled(controlList.getSelectedIndex()>=1);
		controlDown.setEnabled(controlList.getSelectedIndex()>=0 && controlList.getSelectedIndex()<controlListModel.size()-1);
	}

	/**
	 * Versucht eine Datei, die per Drag&amp;Drop auf das Programmfenster gezogen wurde, zu laden
	 * @param file	Zu ladende Datei
	 * @return	Gibt <code>true</code> zur�ck, wenn die Datei geladen werden konnte
	 */
	public final boolean dragDropLoadFile(final File file) {
		if (!allowDispose()) return false;

		OptimizerSetup setup=new OptimizerSetup();
		final String error=setup.loadFromFile(file);
		if (error!=null) {
			MsgBox.error(this,Language.tr("Optimizer.Settings.Load.Error"),error);
			return false;
		}

		loadSetupToGUI(setup);
		return true;
	}

	/**
	 * Versucht eine Datei, die per Drag&amp;Drop auf das Programmfenster gezogen wurde, zu laden
	 * @param stream	Zu ladender Stream
	 * @return	Gibt <code>true</code> zur�ck, wenn die Datei geladen werden konnte
	 */
	public final boolean dragDropLoadFile(final InputStream stream) {
		if (!allowDispose()) return false;

		OptimizerSetup setup=new OptimizerSetup();
		final String error=setup.loadFromStream(stream);
		if (error!=null) {
			MsgBox.error(this,Language.tr("Optimizer.Settings.Load.Error"),error);
			return false;
		}

		loadSetupToGUI(setup);
		return true;
	}

	/**
	 * Zeigt einen Dialog zur Auswahl einer Java- oder Javascript-Datei an
	 * @param dialogTitle	Anzuzeigender Dialogtitel
	 * @param oldFileName	Bisheriger Dateiname (zur Auswahl des initialen Verzeichnisses)
	 * @return	Ausgew�hlte Datei oder <code>null</code>, wenn die Auswahl abgebrochen wurde
	 */
	private String selectJSOrJavaFile(final String dialogTitle, final String oldFileName) {
		JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		fc.setDialogTitle(dialogTitle);
		final FileFilter script=new FileNameExtensionFilter(Language.tr("FileType.JSAndJava")+" (*.js,*.java)","js","java");
		final FileFilter js=new FileNameExtensionFilter(Language.tr("FileType.JS")+" (*.js)","js");
		final FileFilter java=new FileNameExtensionFilter(Language.tr("FileType.Java")+" (*.java)","java");
		fc.addChoosableFileFilter(script);
		fc.addChoosableFileFilter(js);
		fc.addChoosableFileFilter(java);
		fc.setFileFilter(script);
		if (oldFileName!=null && !oldFileName.isEmpty()) {
			File oldFile=new File(oldFileName);
			fc.setCurrentDirectory(oldFile.getParentFile());
		}
		if (fc.showOpenDialog(this)!=JFileChooser.APPROVE_OPTION) return null;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();
		if (file.getName().indexOf('.')<0 && fc.getFileFilter()==js) file=new File(file.getAbsoluteFile()+".js");
		if (file.getName().indexOf('.')<0 && fc.getFileFilter()==java) file=new File(file.getAbsoluteFile()+".java");

		return file.getAbsolutePath();
	}

	/**
	 * Zeigt einen Dialog zur Auswahl eines Ausgabeverzeichnisses an.
	 * @return	Gew�hltes Ausgabeverzeichnis oder <code>null</code>, wenn die Auswahl abgebrochen wurde
	 */
	private final String selectFolder() {
		final JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		fc.setDialogTitle(Language.tr("Optimizer.Tab.Optimization.Folder.Button.Hint"));
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (fc.showSaveDialog(owner)!=JFileChooser.APPROVE_OPTION) return null;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		final File file=fc.getSelectedFile();
		return file.toString();
	}

	@Override
	protected void userButtonClick(int index, JButton button) {
		if (button==newSetupButton) {
			if (allowDispose()) loadSetupToGUI(new OptimizerSetup());
			return;
		}
		if (button==loadSetupButton) {
			if (allowDispose()) loadSetup();
			return;
		}
		if (button==saveSetupButton) {
			final OptimizerSetup newSetup=getSetupFromGUI(true);
			if (newSetup!=null) saveSetup(newSetup);
			return;
		}
		if (button==startButton) {
			if (optimizer!=null) {
				optimizer.cancel();
				return;
			}
			final OptimizerSetup runSetup=getSetupFromGUI(true);
			if (runSetup==null) return;
			optimizer=new OptimizerCatalog(this).getOptimizer(optimizerName);
			final long optimizationStartTime=System.currentTimeMillis();
			String error=optimizer.check(model,editModelPath,runSetup,text->addStatusLine(text),b->{setGUIRunMode(false); if (b) Notifier.run(Notifier.Message.OPTIMIZATION_DONE,optimizationStartTime);},()->updateDiagram());
			if (error!=null) {
				optimizer=null;
				MsgBox.error(OptimizerPanel.this,Language.tr("Optimizer.Error.CouldNotStart.Title"),Language.tr("Optimizer.Error.CouldNotStart")+":\n"+error);
				return;
			}
			setGUIRunMode(true);
			updateDiagram();
			optimizer.start();
			return;
		}
		if (button==helpButton) {
			Help.topicModal(OptimizerPanel.this,"Optimizer");
			return;
		}
	}

	/**
	 * Befehl: Optimierungsmethode w�hlen
	 */
	private void showSelectKernelPopup() {
		final JPopupMenu popup=new JPopupMenu();

		final ButtonGroup group=new ButtonGroup();

		for (String name: new OptimizerCatalog(this).getOptimizerNames()) {
			final JRadioButtonMenuItem option=new JRadioButtonMenuItem(name);
			option.addActionListener(e->{optimizerName=name;});
			option.setSelected(optimizerName.equals(name));
			popup.add(option);
			group.add(option);
		}

		popup.addSeparator();

		JMenuItem settings=new JMenuItem(Language.tr("Optimizer.Tab.Optimization.Kernel.Parameters"));
		popup.add(settings);
		settings.addActionListener(e->setupKernelParameters());

		popup.show(kernelButton,0,kernelButton.getHeight());
	}

	/**
	 * Befehl: Optimierungsmethoden konfigurieren
	 */
	private void setupKernelParameters() {
		final OptimizerSetup tempSetup=getSetupFromGUI(true);
		final OptimizerPanelAlgorithmParametersDialog dialog=new OptimizerPanelAlgorithmParametersDialog(this,tempSetup,()->Help.topicModal(OptimizerPanel.this,"Optimizer"));
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			timeoutSeconds=tempSetup.timeoutSeconds;
			serialChangeSpeed1=tempSetup.serialChangeSpeed1;
			serialChangeSpeed2=tempSetup.serialChangeSpeed2;
			serialChangeSpeed3=tempSetup.serialChangeSpeed3;
			serialChangeSpeed4=tempSetup.serialChangeSpeed4;
			serialSimulatedAnnealing=tempSetup.serialSimulatedAnnealing;
			geneticPopulationSize=tempSetup.geneticPopulationSize;
			geneticEvolutionPressure=tempSetup.geneticEvolutionPressure;
			geneticChangeSpeed1=tempSetup.geneticChangeSpeed1;
			geneticChangeSpeed2=tempSetup.geneticChangeSpeed2;
			geneticChangeSpeed3=tempSetup.geneticChangeSpeed3;
			geneticChangeSpeed4=tempSetup.geneticChangeSpeed4;
			geneticChangeSpeed5=tempSetup.geneticChangeSpeed5;
		}
	}

	/**
	 * Reagiert auf Klicks auf die verschiedenen Schaltfl�chen
	 */
	private class ButtonListener implements ActionListener {
		/**
		 * Konstruktor der Klasse
		 */
		public ButtonListener() {
			/*
			 * Wird nur ben�tigt, um einen JavaDoc-Kommentar f�r diesen (impliziten) Konstruktor
			 * setzen zu k�nnen, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource()==controlAdd) {
				commandControlAdd();
				return;
			}
			if (e.getSource()==controlEdit) {
				commandControlEdit();
				return;
			}
			if (e.getSource()==controlDelete) {
				commandControlDelete();
				return;
			}
			if (e.getSource()==controlUp) {
				commandControlUp();
				return;
			}
			if (e.getSource()==controlDown) {
				commandControlDown();
				return;
			}
			if (e.getSource()==controlConstrains) {
				commandControlSetupConstrains();
				return;
			}
			if (e.getSource()==targetXMLTagButton) {
				commandSelectXMLPopup();
				return;
			}
			if (e.getSource()==targetScriptButton) {
				String s=selectJSOrJavaFile(Language.tr("Optimizer.Tab.Target.Type.Script.Button.Hint"),targetScriptEdit.getText());
				if (s!=null) {
					targetScriptEdit.setText(s);
					targetScriptSelect.setSelected(true);
				}
				return;
			}
			if (e.getSource()==statisticsButton) {
				final String folder=selectFolder();
				if (folder!=null) {
					statisticsEdit.setText(folder);
				}
				return;
			}
			if (e.getSource() instanceof FileDropperData) {
				final FileDropperData data=(FileDropperData)e.getSource();
				File file=data.getFile();
				if (file.isDirectory()) {
					statisticsEdit.setText(file.toString());
					data.dragDropConsumed();
				}
				return;
			}
			if (e.getSource()==kernelButton) {
				showSelectKernelPopup();
				return;
			}
		}
	}

	/**
	 * Renderer f�r die Eintr�ge der Kontrollvariablenliste
	 * @see OptimizerPanel#controlList
	 */
	private class ControlListCellRenderer extends DefaultListCellRenderer {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID = -784953418136588536L;

		/**
		 * Konstruktor der Klasse
		 */
		public ControlListCellRenderer() {
			/*
			 * Wird nur ben�tigt, um einen JavaDoc-Kommentar f�r diesen (impliziten) Konstruktor
			 * setzen zu k�nnen, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index,boolean isSelected, boolean cellHasFocus) {
			Component renderer=super.getListCellRendererComponent(list,value, index, isSelected, cellHasFocus);
			if (renderer instanceof JLabel && value instanceof ControlVariable) {
				final ControlVariable controlVariable=(ControlVariable)value;
				final StringBuilder sb=new StringBuilder();
				sb.append("<html><body><p style=\"margin-top: 3px; margin-bottom: 3px; margin-left: 5px;\">");
				switch (controlVariable.mode) {
				case MODE_RESOURCE:
					sb.append(Language.tr("Optimizer.Tab.ControlVariables.Element.Resource"));
					break;
				case MODE_VARIABLE:
					sb.append(Language.tr("Optimizer.Tab.ControlVariables.Element.Variable"));
					break;
				case MODE_MAP:
					sb.append(Language.tr("Optimizer.Tab.ControlVariables.Element.Map"));
					break;
				case MODE_XML:
					sb.append(Language.tr("Optimizer.Tab.ControlVariables.Element"));
					break;
				}
				sb.append(": <b>");
				sb.append(controlVariable.tag);
				sb.append("</b>");
				if (controlVariable.mode==ModelChanger.Mode.MODE_XML) {
					sb.append(" (");
					if (controlVariable.xmlMode>=0 && controlVariable.xmlMode<ModelChanger.XML_ELEMENT_MODES.length) sb.append(ModelChanger.XML_ELEMENT_MODES[controlVariable.xmlMode]);
					sb.append(")");
				}
				sb.append("<br>"+Language.tr("Optimizer.Tab.ControlVariables.Range")+": [");
				sb.append(NumberTools.formatNumber(controlVariable.rangeFrom));
				sb.append("...");
				sb.append(NumberTools.formatNumber(controlVariable.rangeTo));
				sb.append("]");
				if (controlVariable.mode!=ModelChanger.Mode.MODE_RESOURCE) {
					if (controlVariable.integerValue) sb.append(" "+Language.tr("Optimizer.Tab.ControlVariables.IsInteger"));
				}
				sb.append(", Startwert: ");
				sb.append(NumberTools.formatNumber(controlVariable.start));
				sb.append("</p></body></html>");
				((JLabel)renderer).setText(sb.toString());
				((JLabel)renderer).setIcon(Images.OPTIMIZER_PAGE_CONTROL_VARIABLE.getIcon());
			}
			return renderer;
		}
	}
}
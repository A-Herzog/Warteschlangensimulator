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
import java.util.ArrayList;
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

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
import ui.optimizer.OptimizerBase.OptimizationRunResults;
import ui.optimizer.OptimizerSetup.ControlVariable;
import ui.statistics.ListPopup;
import ui.statistics.StatisticViewerFastAccessDialog;
import ui.tools.SpecialPanel;
import xml.XMLTools;

/**
 * Ermöglicht die Konfiguration und Durchführung einer Optimierung
 * @author Alexander Herzog
 */
public class OptimizerPanel extends SpecialPanel {
	private static final long serialVersionUID = 2492864288739317550L;

	/** Übergeordnetes Fenster */
	private final Window owner;
	/** Statistikdaten bezogen auf einen kurzen Lauf über das angegebene Editor-Modell (zur Auswahl von XML-Elementen als Zielwerte) */
	private final Statistics miniStatistics;
	/** Editor-Modell auf dessen Basis die Optimierung durchgeführt werden soll */
	private final EditModel model;

	/** Aktuelles Optimierer-Setup */
	private OptimizerSetup setup;

	/** Registerreiter für die verschiedenen Einstellungsbereiche */
	private JTabbedPane tabs;

	/** "Neu"-Schaltfläche (um ein neues Optimierer-Setup anzulegen) */
	private final JButton newSetupButton;
	/** "Laden"-Schaltfläche (um ein Optimierer-Setup zu laden) */
	private final JButton loadSetupButton;
	/** "Speichern"-Schaltfläche (um das aktuelle Optimierer-Setup zu speichern) */
	private final JButton saveSetupButton;
	/** "Start"-Schaltfläche */
	private final JButton startButton;
	/** "Hilfe"-Schaltfläche */
	private final JButton helpButton;

	/** "Hinzufügen"-Schaltfläche (für Kontrollvariable) */
	private JButton controlAdd;
	/** "Bearbeiten"-Schaltfläche (für Kontrollvariable) */
	private JButton controlEdit;
	/** "Löschen"-Schaltfläche (für Kontrollvariable) */
	private JButton controlDelete;
	/** "Nach oben"-Schaltfläche (für Kontrollvariable) */
	private JButton controlUp;
	/** "Nach unten"-Schaltfläche (für Kontrollvariable) */
	private JButton controlDown;
	/** "Nebenbedingungen"-Schaltfläche (für Kontrollvariable) */
	private JButton controlConstrains;
	/** Liste mit Nebenbedingungen */
	private List<String> constrains;
	/** Listendarstellung der Kontrollvariablen */
	private JList<ControlVariable> controlList;
	/** Datenmodell für die Listendarstellung der Kontrollvariablen */
	private DefaultListModel<ControlVariable> controlListModel;

	/** Auswahl: Zielwert aus Statistik-XML-Element auslesen */
	private JRadioButton targetXMLTagSelect;
	/** Statistik-XML-Element für Zielwert */
	private JTextField targetXMLTagEdit;
	/** Statistik-XML-Element für Zielwert auswählen */
	private JButton targetXMLTagButton;
	/** Auswahl: Zielwert aus Skript-Ergebnis */
	private JRadioButton targetScriptSelect;
	/** Zielwert-Skript Datei */
	private JTextField targetScriptEdit;
	/** Zielwert-Skript Datei auswählen */
	private JButton targetScriptButton;
	/** Optimierungsziel: Zielwert minimieren */
	private JRadioButton valueMinimize;
	/** Optimierungsziel: Zielwert maximieren */
	private JRadioButton valueMaximize;
	/** Optimierungsziel: Zielwert in bestimmtem Bereich */
	private JRadioButton valueRange;
	/** Untere Grenze für den Zielwert-Bereich */
	private JTextField valueRangeMin;
	/** Obere Grenze für den Zielwert-Bereich */
	private JTextField valueRangeMax;

	/** Ausgabeverzeichnis für Statistikdateien */
	private JTextField statisticsEdit;
	/** Ausgabeverzeichnis für Statistikdateien auswählen */
	private JButton statisticsButton;
	/** Optimierer-Kernel konfigurieren */
	private JButton kernelButton;
	/** Sollen alle Statistik-Ergebnisse gespeichert werden oder nur die Statistik des letzten Laufs */
	private JComboBox<String> statisticsSaveMode;
	/** Statusausgaben während der Optimierung */
	private JTextArea log;
	/** Daten für die grafische Anzeige des Optimierungsfortschritts */
	private XYSeriesCollection xydata;
	/** Render für die Daten für die grafische Anzeige des Optimierungsfortschritts */
	private XYLineAndShapeRenderer xyrenderer;
	/** Diagramm-Element für die grafische Anzeige des Optimierungsfortschritts */
	private JFreeChart chart;
	/** Diagramm für die grafische Anzeige des Optimierungsfortschritts */
	private XYPlot xyplot;
	/** Soll die y-Achse bei der grafischen Anzeige des Optimierungsfortschritts logarithmisch skaliert werden? */
	private JCheckBox logarithmicAxis;

	/** Name des gewählten Optimierungs-Kernels */
	private String optimizerName;
	/** Seriell arbeitender Optimierer-Kernel: Änderungsgeschwindigkeit in Runde 1 */
	private double serialChangeSpeed1;
	/** Seriell arbeitender Optimierer-Kernel: Änderungsgeschwindigkeit in Runde 2 */
	private double serialChangeSpeed2;
	/** Seriell arbeitender Optimierer-Kernel: Änderungsgeschwindigkeit in Runde 3 */
	private double serialChangeSpeed3;
	/** Seriell arbeitender Optimierer-Kernel: Änderungsgeschwindigkeit ab Runde 4 */
	private double serialChangeSpeed4;
	/** Seriell arbeitender Optimierer-Kernel: Temporäre Verschlechterungen akzeptieren? */
	private boolean serialSimulatedAnnealing;
	/** Genetischer Optimierer: Populationsgröße */
	private int geneticPopulationSize;
	/** Genetischer Optimierer: Evolutionärer Druck */
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
	 * @param owner	Übergeordnetes Fenster
	 * @param model	Editor-Modell auf dessen Basis die Optimierung durchgeführt werden soll
	 * @param miniStatistics	Statistikdaten bezogen auf einen kurzen Lauf über das angegebene Editor-Modell (zur Auswahl von XML-Elementen als Zielwerte)
	 * @param doneNotify	Wird aufgerufen, wenn sich das Panel schließen möchte
	 */
	public OptimizerPanel(final Window owner, final EditModel model, final Statistics miniStatistics, final Runnable doneNotify) {
		super(doneNotify);
		this.owner=owner;
		this.model=model;
		this.miniStatistics=miniStatistics;

		newSetupButton=addUserButton(Language.tr("Optimizer.Toolbar.New"),Language.tr("Optimizer.Toolbar.New.Hint"),Images.OPTIMIZER_SETUP_NEW.getURL());
		loadSetupButton=addUserButton(Language.tr("Optimizer.Toolbar.Load"),Language.tr("Optimizer.Toolbar.Load.Hint"),Images.OPTIMIZER_SETUP_LOAD.getURL());
		saveSetupButton=addUserButton(Language.tr("Optimizer.Toolbar.Save"),Language.tr("Optimizer.Toolbar.Save.Hint"),Images.OPTIMIZER_SETUP_SAVE.getURL());
		addSeparator();
		startButton=addUserButton(Language.tr("Optimizer.Toolbar.Start"),Language.tr("Optimizer.Toolbar.Start.Hint"),Images.OPTIMIZER_RUN.getURL());
		addSeparator();
		addCloseButton();
		addSeparator();
		helpButton=addUserButton(Language.tr("Main.Toolbar.Help"),Language.tr("Main.Toolbar.Help.Hint"),Images.HELP.getURL());

		initGUI();
		loadSetupToGUI(new OptimizerSetup());
		getSetupFromGUI(true);

		/* F1-Hotkey */
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F1"),"actionHelp");
		getActionMap().put("actionHelp",new AbstractAction("actionHelp") {
			private static final long serialVersionUID = 1738622101739292954L;
			@Override public void actionPerformed(ActionEvent event) {Help.topicModal(OptimizerPanel.this,"Optimizer");}
		});
	}

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

		tab.add(new JScrollPane(controlList=new JList<>(controlListModel=new DefaultListModel<ControlVariable>())),BorderLayout.CENTER);
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
		controlList.addListSelectionListener(new ListSelectionListener() {
			@Override public void valueChanged(ListSelectionEvent e) {commandListSelectionChanged();}
		});

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
		valueRangeMin.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {getSetupFromGUI(true); valueRange.setSelected(true);}
			@Override public void keyReleased(KeyEvent e) {getSetupFromGUI(true); valueRange.setSelected(true);}
			@Override public void keyPressed(KeyEvent e) {getSetupFromGUI(true); valueRange.setSelected(true);}
		});
		line.add(new JLabel(" "+Language.tr("Optimizer.Tab.Target.Value.RangeB")+" "));
		line.add(valueRangeMax=new JTextField(10));
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

		/* Icons für Tabs setzen */

		tabs.setIconAt(0,Images.OPTIMIZER_PAGE_CONTROL_VARIABLE.getIcon());
		tabs.setIconAt(1,Images.OPTIMIZER_PAGE_TARGET.getIcon());
		tabs.setIconAt(2,Images.OPTIMIZER_PAGE_OPTIMIZATION.getIcon());

		commandListSelectionChanged();
	}

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
		final TextTitle t=chart.getTitle();
		if (t!=null) {Font f=t.getFont(); t.setFont(new Font(f.getFontName(),Font.PLAIN,f.getSize()-4));}

		parentPanel.add(chartPanel,BorderLayout.CENTER);
	}

	private List<OptimizationRunResults> lastResults;

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
			seriesGood[i]=new XYSeries(Language.tr("Optimizer.Tab.Optimization.GoodResult")+((i==0)?"":(""+i)));
			seriesBad[i]=new XYSeries(Language.tr("Optimizer.Tab.Optimization.BadResult")+((i==0)?"":(""+i)));
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

		/* Serien mit Daten füllen */
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

		popup.add(item=new JMenuItem(Language.tr("Optimizer.Tab.Optimization.Step.Result.Save.Chart")));
		item.setIcon(Images.OPTIMIZER_EXPORT_CHART.getIcon());
		item.addActionListener(e->saveDiagram());

		popup.show(button,0,button.getHeight());
	}

	private void copyDiagram() {
		final int imageSize=Math.max(100,SetupData.getSetup().imageSize);
		final BufferedImage image=chart.createBufferedImage(imageSize,imageSize);
		ImageTools.copyImageToClipboard(image);
	}

	private void saveDiagram() {
		final File file=ImageTools.showSaveDialog(this,true);
		if (file==null) return;

		if (!saveDiagram(file)) {
			MsgBox.error(owner,StatisticsBasePanel.viewersSaveImageErrorTitle,String.format(StatisticsBasePanel.viewersSaveImageErrorInfo,file.toString()));
		}
	}

	private Table getTableFromChart() {
		final Table table=new Table();

		final XYSeries series=xydata.getSeries(0);
		final String name=series.getKey().toString();
		final List<String> categories=new ArrayList<>(1+series.getItemCount());
		final List<String> line=new ArrayList<>(1+series.getItemCount());
		line.add((name==null)?"":name);
		if (categories!=null) categories.add("");
		for (Object obj: series.getItems()) if (obj instanceof XYDataItem) {
			final XYDataItem data=(XYDataItem)obj;
			if (categories!=null) categories.add(NumberTools.formatNumberMax(data.getXValue()));
			line.add(NumberTools.formatNumberMax(data.getYValue()));
		}
		if (categories!=null) table.addLine(categories);
		table.addLine(line);

		return table.transpose(true);
	}

	private TableChart getTableChartFromChart() {
		final TableChart tableChart=new TableChart(getTableFromChart());

		tableChart.setupAxis(xyplot.getDomainAxis().getLabel(),xyplot.getRangeAxis().getLabel());
		tableChart.setupChart(TableChart.ChartMode.LINE);

		return tableChart;
	}

	private boolean saveDiagram(final File file) {
		return ImageTools.saveChart(this,chart,file,SetupData.getSetup().imageSize,()->getTableChartFromChart());
	}

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

	private void addStatusLine(String line) {
		if (log.getText().isEmpty()) log.setText(line); else log.setText(log.getText()+"\n"+line);
		log.setCaretPosition(log.getText().length());
	}

	private void setGUIRunMode(final boolean run) {
		if (run) {
			startButton.setText(Language.tr("Optimizer.Toolbar.Stop"));
			startButton.setToolTipText(Language.tr("Optimizer.Toolbar.Stop.Hint"));
			startButton.setIcon(Images.GENERAL_CANCEL.getIcon());
		} else {
			startButton.setText(Language.tr("Optimizer.Toolbar.Start"));
			startButton.setToolTipText(Language.tr("Optimizer.Toolbar.Start.Hint"));
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

		this.setup=setup;
		return true;
	}

	private void commandControlAdd() {
		OptimizerPanelControlVariableDialog dialog=new OptimizerPanelControlVariableDialog(this,model,null,()->Help.topicModal(OptimizerPanel.this,"Optimizer"));
		dialog.setVisible(true);
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			controlListModel.addElement(dialog.getControlVariable());
		}
	}

	private void commandControlEdit() {
		if (controlList.getSelectedIndex()<0) return;
		OptimizerPanelControlVariableDialog dialog=new OptimizerPanelControlVariableDialog(this,model,controlListModel.get(controlList.getSelectedIndex()),()->Help.topicModal(OptimizerPanel.this,"Optimizer"));
		dialog.setVisible(true);
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			controlListModel.set(controlList.getSelectedIndex(),dialog.getControlVariable());
		}
	}

	private void commandControlDelete() {
		if (controlList.getSelectedIndex()<0) return;
		controlListModel.remove(controlList.getSelectedIndex());
	}

	private void commandControlUp() {
		if (controlList.getSelectedIndex()<1) return;
		final ControlVariable temp=controlListModel.get(controlList.getSelectedIndex()-1);
		controlListModel.set(controlList.getSelectedIndex()-1,controlListModel.get(controlList.getSelectedIndex()));
		controlListModel.set(controlList.getSelectedIndex(),temp);
		controlList.setSelectedIndex(controlList.getSelectedIndex()-1);
	}

	private void commandControlDown() {
		if (controlList.getSelectedIndex()<0 || controlList.getSelectedIndex()==controlListModel.size()-1) return;
		final ControlVariable temp=controlListModel.get(controlList.getSelectedIndex()+1);
		controlListModel.set(controlList.getSelectedIndex()+1,controlListModel.get(controlList.getSelectedIndex()));
		controlListModel.set(controlList.getSelectedIndex(),temp);
		controlList.setSelectedIndex(controlList.getSelectedIndex()+1);
	}

	private void commandControlSetupConstrains() {
		if (constrains==null) constrains=new ArrayList<>();
		new OptimizerPanelConstrainsDialog(this,constrains);
	}

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

	private final void commandListSelectionChanged() {
		controlEdit.setEnabled(controlList.getSelectedIndex()>=0);
		controlDelete.setEnabled(controlList.getSelectedIndex()>=0);
		controlUp.setEnabled(controlList.getSelectedIndex()>=1);
		controlDown.setEnabled(controlList.getSelectedIndex()>=0 && controlList.getSelectedIndex()<controlListModel.size()-1);
	}

	/**
	 * Versucht eine Datei, die per Drag&amp;Drop auf das Programmfenster gezogen wurde, zu laden
	 * @param file	Zu ladende Datei
	 * @return	Gibt <code>true</code> zurück, wenn die Datei geladen werden konnte
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

	private String selectJSOrJavaFile(String dialogTitle, String oldFileName) {
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
			optimizer=new OptimizerCatalog().getOptimizer(optimizerName);
			final long optimizationStartTime=System.currentTimeMillis();
			String error=optimizer.check(model,runSetup,text->addStatusLine(text),b->{setGUIRunMode(false); if (b) Notifier.run(Notifier.Message.OPTIMIZATION_DONE,optimizationStartTime);},()->updateDiagram());
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

	private void showSelectKernelPopup() {
		final JPopupMenu popup=new JPopupMenu();

		final ButtonGroup group=new ButtonGroup();

		for (String name: new OptimizerCatalog().getOptimizerNames()) {
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

	private void setupKernelParameters() {
		final OptimizerSetup tempSetup=getSetupFromGUI(true);
		final OptimizerPanelAlgorithmParametersDialog dialog=new OptimizerPanelAlgorithmParametersDialog(this,tempSetup,()->Help.topicModal(OptimizerPanel.this,"Optimizer"));
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
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

	private class ButtonListener implements ActionListener {
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

	private class ControlListCellRenderer extends DefaultListCellRenderer {
		private static final long serialVersionUID = -784953418136588536L;

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
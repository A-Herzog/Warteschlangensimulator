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
package ui.calculator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.swing.JDistributionEditorPanel;
import mathtools.distribution.swing.JDistributionPanel;
import mathtools.distribution.tools.AbstractDistributionWrapper;
import parser.MathCalcError;
import simulator.simparser.ExpressionCalc;
import systemtools.BaseDialog;
import systemtools.SmallColorChooser;
import systemtools.images.SimToolsImages;
import tools.SetupData;
import ui.calculator.PlotterPanel.Graph;
import ui.expressionbuilder.ExpressionBuilder;
import ui.expressionbuilder.ExpressionBuilderAutoComplete;
import ui.help.Help;
import ui.images.Images;
import ui.scriptrunner.JSModelRunnerPanel;
import ui.tools.WindowSizeStorage;

/**
 * Zeigt einen Dialog zur Berechnung von mathematischen Ausdrücken an.<br>
 * Der Konstruktor macht den Dialog direkt sichtbar.
 * @author Alexander Herzog
 * @see ExpressionCalc
 */
public class CalculatorWindow extends JFrame {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -3883480454772212675L;

	/** Übergeordnetes Fenster */
	private Window owner;

	/** Tabs für die einzelnen Programmfunktionen */
	final JTabbedPane tabs;

	/** Eingabezeile */
	private final JTextField inputEdit;
	/** Ergebnisausgabezeile */
	private final JTextField outputEdit;

	/** Funktionsplotter */
	private final PlotterPanel plotter;
	/** Eingabefelder für den Funktionsplotter */
	private final List<JTextField> plotterField;

	/** Wahrscheinlichkeitsverteilungsplotter */
	private final JDistributionPanel distributionPlotter;
	/** Eingabefelder für den Wahrscheinlichkeitsverteilungsplotter */
	private final JDistributionEditorPanel distributionEditor;

	/** Script-Editor */
	private final JSModelRunnerPanel scriptEditor;

	/**
	 * Instanz des Rechnerfensters
	 * @see #show(Component, String, AbstractDistributionWrapper)
	 * @see #closeWindow()
	 */
	private static CalculatorWindow instance;

	/**
	 * Zeigt das Rechnerfenster an.
	 * (Erstellt entweder ein neues Fenster oder holt das aktuelle in den Vordergrund.)
	 * @param owner	Übergeordnetes Element
	 * @param initialExpression	Initial anzuzeigender Ausdruck (kann <code>null</code> sein)
	 * @param initialDistribution	Initial auszuwählende Verteilung (kann <code>null</code> sein)
	 */
	public static void show(final Component owner, final String initialExpression, final AbstractDistributionWrapper initialDistribution) {
		if (instance==null) {
			instance=new CalculatorWindow(owner,initialExpression,initialDistribution);
			instance.setVisible(true);
		} else {
			instance.setExpression(initialExpression);
			instance.setDistribution(initialDistribution);
			if ((instance.getExtendedState() & ICONIFIED)!=0) instance.setState(NORMAL);
			instance.toFront();
		}
	}

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param initialExpression	Initial anzuzeigender Ausdruck (kann <code>null</code> sein)
	 * @param initialDistribution	Initial auszuwählende Verteilung (kann <code>null</code> sein)
	 * @see #show(Component, String, AbstractDistributionWrapper)
	 */
	private CalculatorWindow(final Component owner, final String initialExpression, final AbstractDistributionWrapper initialDistribution) {
		super(Language.tr("CalculatorDialog.Title"));
		setIconImage(Images.EXTRAS_CALCULATOR.getImage());

		/* Übergeordnetes Fenster */
		Component o=owner;
		while (o!=null && !(o instanceof Window)) o=o.getParent();
		this.owner=(Window)o;

		/* Aktionen bei Schließen des Fensters */
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		addWindowListener(new WindowAdapter(){
			@Override public void windowClosing(WindowEvent e){closeWindow();}
		});

		/* Gesamter Inhaltsbereich */
		final Container all=getContentPane();
		all.setLayout(new BorderLayout());
		final JPanel content=new JPanel(new BorderLayout());
		all.add(content,BorderLayout.CENTER);

		/* Fußbereich */
		final JPanel footer=new JPanel(new FlowLayout(FlowLayout.LEFT));
		all.add(footer,BorderLayout.SOUTH);
		final JButton closeButton=new JButton(BaseDialog.buttonTitleClose,SimToolsImages.EXIT.getIcon());
		getRootPane().setDefaultButton(closeButton);
		closeButton.addActionListener(e->dispatchEvent(new WindowEvent(CalculatorWindow.this,WindowEvent.WINDOW_CLOSING)));
		footer.add(closeButton);
		final JButton helpButton=new JButton(BaseDialog.buttonTitleHelp,SimToolsImages.HELP.getIcon());
		helpButton.addActionListener(e->Help.topicModal(this,"Calculator"));
		footer.add(helpButton);

		/* Inhaltsbereich */
		content.add(tabs=new JTabbedPane(),BorderLayout.CENTER);

		JPanel tab, line;
		Object[] data;
		Dimension size;

		/* Tab "Rechner" */
		tabs.addTab(Language.tr("CalculatorDialog.Tab.Calculator"),tab=new JPanel(new BorderLayout()));

		final JPanel lines=new JPanel();
		tab.add(lines,BorderLayout.NORTH);
		lines.setLayout(new BoxLayout(lines,BoxLayout.PAGE_AXIS));

		data=getInputPanel(Language.tr("CalculatorDialog.Expression")+":","",-1);
		lines.add(line=(JPanel)data[0]);
		inputEdit=(JTextField)data[1];
		inputEdit.addKeyListener(new KeyAdapter() {
			@Override public void keyReleased(KeyEvent e) {outputEdit.setText(calc(inputEdit.getText()));}
		});

		final JButton buildButton=new JButton();
		line.add(buildButton,BorderLayout.EAST);
		buildButton.setPreferredSize(new Dimension(26,26));
		buildButton.setIcon(Images.EXPRESSION_BUILDER.getIcon());
		buildButton.setToolTipText(Language.tr("CalculatorDialog.Expression.BuildExpression"));

		data=getInputPanel(Language.tr("CalculatorDialog.Result")+":","",-1);
		lines.add(line=(JPanel)data[0]);
		outputEdit=(JTextField)data[1];
		outputEdit.setEditable(false);

		buildButton.addActionListener(e->{
			final ExpressionBuilder dialog=new ExpressionBuilder(owner,inputEdit.getText(),false,new String[0],null,null,null,false,true,true);
			dialog.setVisible(true);
			if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
				inputEdit.setText(dialog.getExpression());
				outputEdit.setText(calc(inputEdit.getText()));
			}
		});
		ExpressionBuilderAutoComplete.process(new ExpressionBuilder(owner,inputEdit.getText(),false,new String[0],null,null,null,false,true,true),inputEdit);

		if (initialExpression!=null) setExpression(initialExpression);

		final JButton button=new JButton("");
		button.setIcon(Images.EDIT_COPY.getIcon());
		button.setToolTipText(Language.tr("CalculatorDialog.Result.Copy"));
		size=button.getPreferredSize();
		button.setPreferredSize(new Dimension(size.height,size.height));
		button.addActionListener(e->{
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(outputEdit.getText()),null);
		});
		line.add(button,BorderLayout.EAST);

		/* Tab "Funktionsplotter" */
		tabs.addTab(Language.tr("CalculatorDialog.Tab.Plotter"),tab=new JPanel(new BorderLayout()));
		plotterField=new ArrayList<>();
		tab.add(plotter=new PlotterPanel(),BorderLayout.CENTER);
		final JPanel plotterInput=new JPanel();
		tab.add(plotterInput,BorderLayout.SOUTH);
		plotterInput.setLayout(new BoxLayout(plotterInput,BoxLayout.PAGE_AXIS));
		plotterInput.add(getPlotterInputLine(plotter,"10*sin(x)",Color.BLUE));
		plotterInput.add(getPlotterInputLine(plotter,"x^2/5-10",Color.RED));
		plotterInput.add(getPlotterInputLine(plotter,"",Color.GREEN));
		plotter.addRedrawDoneListener(()->{
			final List<PlotterPanel.Graph> graphs=plotter.getGraphs();
			for (int i=0;i<graphs.size();i++) {
				final PlotterPanel.Graph graph=graphs.get(i);
				final JTextField field=plotterField.get(i);
				field.setBackground((field.getText().trim().isEmpty() || graph.isLastPlotOk())?NumberTools.getTextFieldDefaultBackground():Color.RED);
			}
		});
		plotter.reload();

		/* Tab "Wahrscheinlichkeitsverteilungen" */
		tabs.addTab(Language.tr("CalculatorDialog.Tab.Distributions"),tab=new JPanel(new BorderLayout()));
		tab.add(distributionPlotter=new JDistributionPanel(new ExponentialDistribution(100),200,false),BorderLayout.CENTER);
		distributionPlotter.setImageSaveSize(SetupData.getSetup().imageSize);
		distributionPlotter.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		distributionPlotter.setPlotType(JDistributionPanel.BOTH);
		AbstractRealDistribution distribution=null;
		if (initialDistribution!=null) {
			distribution=initialDistribution.getDistribution(100,50);
			if (distribution==null) distribution=initialDistribution.getDefaultDistribution();
		}
		if (distribution==null) distribution=new ExponentialDistribution(100);

		tab.add(distributionEditor=new JDistributionEditorPanel(distribution,1000,e->updateDistribution(),true),BorderLayout.SOUTH);
		updateDistribution();

		/* Tab "Skript" */
		tabs.addTab(Language.tr("CalculatorDialog.Tab.Skript"),tab=new JPanel(new BorderLayout()));
		tab.add(scriptEditor=new JSModelRunnerPanel(this,null,null,null,false));
		scriptEditor.setScript(SetupData.getSetup().scriptCalculator);

		/* Icons auf den Tabs */
		tabs.setIconAt(0,Images.EXTRAS_CALCULATOR.getIcon());
		tabs.setIconAt(1,Images.EXTRAS_CALCULATOR_PLOTTER.getIcon());
		tabs.setIconAt(2,Images.EXTRAS_CALCULATOR_DISTRIBUTION.getIcon());
		tabs.setIconAt(3,Images.EXTRAS_CALCULATOR_SCRIPT.getIcon());

		/* Dialog vorbereiten */
		if (initialDistribution!=null) tabs.setSelectedIndex(2);
		setSize((int)Math.round(800*BaseDialog.windowScaling),(int)Math.round(600*BaseDialog.windowScaling));
		setMinimumSize(new Dimension((int)Math.round(800*BaseDialog.windowScaling),(int)Math.round(600*BaseDialog.windowScaling)));
		setResizable(true);
		setLocationRelativeTo(this.owner);
		WindowSizeStorage.window(this,"calculator");
	}

	/**
	 * Stellt einen anzuzeigenden Rechenausdruck ein.
	 * @param expression	Neuer Rechenausdruck
	 */
	public void setExpression(final String expression) {
		if (expression==null || expression.trim().isEmpty()) return;

		inputEdit.setText(expression);
		SwingUtilities.invokeLater(()->{
			outputEdit.setText(calc(inputEdit.getText()));
			tabs.setSelectedIndex(0);
		});
	}

	/**
	 * Stellt die anzuzeigende Verteilung ein.
	 * @param initialDistribution	Neue Verteilung
	 */
	public void setDistribution(final AbstractDistributionWrapper initialDistribution) {
		if (initialDistribution==null) return;
		final AbstractRealDistribution distribution=initialDistribution.getDistribution(100,50);
		if (distribution==null) return;
		distributionEditor.setDistribution(distribution);
		updateDistribution();
		SwingUtilities.invokeLater(()->{
			tabs.setSelectedIndex(2);
		});
	}

	/**
	 * Veränderte Einstellungen zu der Wahrscheinlichkeitsverteilung an den Plotter übertragen.
	 */
	private void updateDistribution() {
		if (distributionEditor!=null) distributionPlotter.setDistribution(distributionEditor.getDistribution());
	}

	/**
	 * Erzeugt eine Eingabezeile mit zugehörigem Label
	 * @param labelText	Beschriftungstext
	 * @param value	Initialer Text für die Eingabezeile
	 * @param size	Länge der Eingabezeile; wird hier ein Wert &le;0 angegeben, so wird die maximal mögliche Breite verwendet
	 * @return	Array aus: Panel das Beschriftung und Eingabezeile enthält und Eingabezeile selbst
	 */
	private static final Object[] getInputPanel(final String labelText, final String value, final int size) {
		JPanel panel;
		JLabel label;
		JTextField field;

		if (size>0) {
			panel=new JPanel(new FlowLayout(FlowLayout.LEFT));
			panel.add(label=new JLabel(labelText));
			panel.add(field=new JTextField(size));
		} else {
			panel=new JPanel(new BorderLayout(5,0));

			Box box;

			box=Box.createVerticalBox();
			box.add(Box.createVerticalGlue());
			final JPanel panelLeft=new JPanel(new FlowLayout());
			panelLeft.add(label=new JLabel(labelText));
			box.add(panelLeft);
			box.add(Box.createVerticalGlue());
			panel.add(box,BorderLayout.WEST);

			field=new JTextField();
			field.setMaximumSize(new Dimension(field.getMaximumSize().width,field.getPreferredSize().height));
			box=Box.createVerticalBox();
			box.add(Box.createVerticalGlue());
			box.add(field);
			box.add(Box.createVerticalGlue());
			panel.add(box,BorderLayout.CENTER);
		}

		label.setLabelFor(field);
		field.setText(value);
		return new Object[]{panel,field};
	}

	/**
	 * Berechnet einen Ausdruck
	 * @param expression	Zu berechnender Ausdruck
	 * @return	Liefert das Ergebnis als Zeichenkette oder eine Fehlermeldung
	 */
	private String calc(final String expression) {
		final ExpressionCalc calc=new ExpressionCalc(null);
		final int error=calc.parse(expression);
		if (error>=0) return String.format(Language.tr("CalculatorDialog.Expression.ParseError"),error+1);
		double d;
		try {
			d=calc.calc();
		} catch (MathCalcError e) {
			return Language.tr("CalculatorDialog.Expression.CalcError");
		}
		return NumberTools.formatNumberMax(d);
	}

	/**
	 * Erzeugt eine Eingabezeile für den Funktionsplotter
	 * @param plotter	Zugehöriger Plotter
	 * @param expression	Initialer Wert für den Ausdruck
	 * @param color	Initiale Farbe für den entsprechenden Graphen
	 * @return	Array aus: Panel das Beschriftung und Eingabezeile enthält und Eingabezeile selbst
	 * @see #getInputPanel(String, String, int)
	 */
	private JPanel getPlotterInputLine(final PlotterPanel plotter, final String expression, final Color color) {
		final PlotterPanel.Graph graph=new PlotterPanel.Graph(expression,color);
		plotter.getGraphs().add(graph);

		final Object[] data=getInputPanel(null,expression==null?"":expression,-1);
		final JPanel panel=(JPanel)data[0];
		final JTextField field=(JTextField)data[1];
		field.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				graph.expression=field.getText();
				plotter.reload();
			}
		});
		final JPanel buttonsPanel=new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(buttonsPanel,BorderLayout.EAST);
		plotterField.add(field);

		final JButton buildButton=new JButton();
		buttonsPanel.add(buildButton);
		buildButton.setPreferredSize(new Dimension(26,26));
		buildButton.setIcon(Images.EXPRESSION_BUILDER.getIcon());
		buildButton.setToolTipText(Language.tr("CalculatorDialog.Plotter.BuildExpression"));
		buildButton.addActionListener(e->{
			final ExpressionBuilder dialog=new ExpressionBuilder(owner,field.getText(),false,new String[] {"x"},null,null,null,false,true,true);
			dialog.setVisible(true);
			if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
				field.setText(graph.expression=dialog.getExpression());
				plotter.reload();
			}
		});

		final JButton colorButton=new JButton();
		buttonsPanel.add(colorButton);
		colorButton.setPreferredSize(new Dimension(26,26));
		colorButton.setToolTipText(Language.tr("CalculatorDialog.Plotter.SelectColor"));
		setupColorButton(colorButton,graph);
		colorButton.addActionListener(e->selectColor(colorButton,graph));

		final JButton clearButton=new JButton();
		buttonsPanel.add(clearButton);
		clearButton.setPreferredSize(new Dimension(26,26));
		clearButton.setIcon(Images.EXTRAS_CALCULATOR_PLOTTER_CLEAR.getIcon());
		clearButton.setToolTipText(Language.tr("CalculatorDialog.Plotter.ClearPlot"));
		clearButton.addActionListener(e->{field.setText(graph.expression=""); plotter.reload();});

		return panel;
	}

	/**
	 * Überträgt die gewählte Farbe aus einem {@link Graph}-Objekt
	 * auf eine Farbauswahl-Schaltfläche.
	 * @param colorButton	Schaltfläche auf der die Farbe angezeigt werden soll
	 * @param graph	Graph aus dem die Farbe ausgelesen werden soll
	 */
	private void setupColorButton(final JButton colorButton, final PlotterPanel.Graph graph) {
		final BufferedImage image;
		image=new BufferedImage(16,16,BufferedImage.TYPE_4BYTE_ABGR);
		final Graphics g=image.getGraphics();
		g.setColor(graph.color);
		g.fillRect(0,0,15,15);
		g.setColor(Color.DARK_GRAY);
		g.drawRect(0,0,15,15);
		colorButton.setIcon(new ImageIcon(image));
	}

	/**
	 * Zeigt ein  Popup-Menü zur Auswahl der Farbe für einen Graphen an.
	 * @param colorButton	Aufrufendes Button an dem das Popup-Menü ausgerichtet wird (und dessen Farbeinstellung ggf. automatisch aktualisiert wird)
	 * @param graph	Graph-Objekt aus dem die bisherige Farbe ausgelesen wird und in dem auch ggf. die neue Farbe gespeichert wird
	 */
	private void selectColor(final JButton colorButton, final PlotterPanel.Graph graph) {
		final JPopupMenu popupMenu=new JPopupMenu();

		final SmallColorChooser colorChooser=new SmallColorChooser(graph.color);
		colorChooser.addClickListener(e->{
			graph.color=colorChooser.getColor();
			setupColorButton(colorButton,graph);
			plotter.reload();
			popupMenu.setVisible(false);
		});
		popupMenu.add(colorChooser);
		popupMenu.show(colorButton,0,colorButton.getHeight());
	}

	/**
	 * Beim Schließen des Fensters das Skript speichern.
	 */
	private void closeWindow() {
		final SetupData setup=SetupData.getSetup();
		setup.scriptCalculator=scriptEditor.getScript();
		setup.saveSetup();
		instance=null;
	}
}
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
package ui.calculator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import systemtools.BaseDialog;
import systemtools.SmallColorChooser;
import ui.calculator.PlotterPanel.Graph;
import ui.expressionbuilder.ExpressionBuilder;
import ui.images.Images;

/**
 * Funktionsplotter-Tab innerhalb des Rechner-Fensters
 * @author Alexander Herzog
 * @see CalculatorWindow
 */
public class CalculatorWindowPagePlotter extends CalculatorWindowPage {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-3923326872508410436L;

	/** Funktionsplotter */
	private final PlotterPanel plotter;
	/** Graphen im Funktionsplotter */
	private final List<PlotterPanel.Graph> plotterGraphs=new ArrayList<>();
	/** Eingabefelder für den Funktionsplotter */
	private final List<JTextField> plotterField=new ArrayList<>();


	/**
	 * Konstruktor der Klasse
	 * @param tabs	Tabs-Element in das dieses Tab eingefügt werden soll
	 */
	public CalculatorWindowPagePlotter(final JTabbedPane tabs) {
		super(tabs);

		/* Plotter */
		add(plotter=new PlotterPanel(),BorderLayout.CENTER);
		final JPanel plotterInput=new JPanel();

		/* Eingabefelder für den Funktionsplotter */
		add(plotterInput,BorderLayout.SOUTH);
		plotterInput.setLayout(new BoxLayout(plotterInput,BoxLayout.PAGE_AXIS));
		plotterInput.add(getPlotterInputLine(plotter,"10*sin(x)",Color.BLUE));
		plotterInput.add(getPlotterInputLine(plotter,"x^2/5-10",Color.RED));
		plotterInput.add(getPlotterInputLine(plotter,"x",Color.GREEN));
		plotter.addRedrawDoneListener(()->{
			final List<PlotterPanel.Graph> graphs=plotter.getGraphs();
			for (int i=0;i<graphs.size();i++) {
				final PlotterPanel.Graph graph=graphs.get(i);
				final JTextField field=plotterField.get(i);
				field.setBackground((field.getText().isBlank() || graph.isLastPlotOk())?NumberTools.getTextFieldDefaultBackground():Color.RED);
			}
		});

		/* Start */
		plotter.reload(false);
	}

	@Override
	protected String getTabTitle() {
		return Language.tr("CalculatorDialog.Tab.Plotter");
	}

	@Override
	protected Images getTabIcon() {
		return Images.EXTRAS_CALCULATOR_PLOTTER;
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
				plotter.reload(true);
			}
		});
		final JPanel buttonsPanel=new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(buttonsPanel,BorderLayout.EAST);
		plotterField.add(field);
		plotterGraphs.add(graph);

		final JButton buildButton=new JButton();
		buttonsPanel.add(buildButton);
		buildButton.setPreferredSize(new Dimension(26,26));
		buildButton.setIcon(Images.EXPRESSION_BUILDER.getIcon());
		buildButton.setToolTipText(Language.tr("CalculatorDialog.Plotter.BuildExpression"));
		buildButton.addActionListener(e->{
			final ExpressionBuilder dialog=new ExpressionBuilder(this,field.getText(),false,new String[] {"x"},null,null,null,false,true,true);
			dialog.setVisible(true);
			if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
				field.setText(graph.expression=dialog.getExpression());
				plotter.reload(true);
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
		clearButton.addActionListener(e->{
			if ((e.getModifiers() & ActionEvent.SHIFT_MASK)!=0) {
				plotterGraphs.forEach(g->g.expression="");
				plotterField.forEach(f->f.setText(""));
			} else {
				field.setText(graph.expression="");
			}
			plotter.reload(true);
		});

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
			plotter.reload(true);
			popupMenu.setVisible(false);
		});
		popupMenu.add(colorChooser);
		popupMenu.show(colorButton,0,colorButton.getHeight());
	}
}

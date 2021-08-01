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
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

import language.Language;
import mathtools.distribution.tools.AbstractDistributionWrapper;
import simulator.simparser.ExpressionCalc;
import systemtools.BaseDialog;
import systemtools.images.SimToolsImages;
import ui.help.Help;
import ui.images.Images;
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
	private final JTabbedPane tabs;

	/**
	 * Liste aller Tabs
	 */
	private final List<CalculatorWindowPage> pages;

	/**
	 * Rechner-Tab
	 * @see #setExpression(String)
	 */
	private final CalculatorWindowPageCalculator calculator;

	/**
	 * Wahrscheinlichkeitsverteilungsplotter-Tab
	 * @see #setDistribution(AbstractDistributionWrapper)
	 */
	private final CalculatorWindowPageDistributions distributions;

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

		/* F1-Hotkey */
		content.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F1"),"actionHelp");
		content.getActionMap().put("actionHelp",new AbstractAction("actionHelp") {
			private static final long serialVersionUID=5606323868359750430L;
			@Override public void actionPerformed(ActionEvent event) {Help.topicModal(CalculatorWindow.this,"Calculator");}
		});

		/* Inhaltsbereich */
		content.add(tabs=new JTabbedPane(),BorderLayout.CENTER);

		/* Seiten */
		pages=new ArrayList<>();
		pages.add(calculator=new CalculatorWindowPageCalculator(tabs,initialExpression));
		pages.add(new CalculatorWindowPagePlotter(tabs));
		pages.add(distributions=new CalculatorWindowPageDistributions(this,tabs,initialDistribution));
		pages.add(new CalculatorWindowPageScript(this,tabs));

		/* Dialog vorbereiten */
		if (initialDistribution!=null) distributions.setDistribution(initialDistribution);
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
		calculator.setExpression(expression);
	}

	/**
	 * Stellt die anzuzeigende Verteilung ein.
	 * @param initialDistribution	Neue Verteilung
	 */
	public void setDistribution(final AbstractDistributionWrapper initialDistribution) {
		distributions.setDistribution(initialDistribution);
	}

	/**
	 * Beim Schließen des Fensters das Skript speichern.
	 */
	private void closeWindow() {
		for (CalculatorWindowPage page: pages) page.storeData();
		instance=null;
	}
}
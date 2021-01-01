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
import java.awt.Dimension;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;

import language.Language;
import systemtools.BaseDialog;
import ui.help.Help;

/**
 * Warteschlangenrechner gemäß verschiedener analytischer Formeln
 * @author Alexander Herzog
 * @version 2.0
 */
public class QueueingCalculatorDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 1388546809132066439L;

	/** Anzeigebereich für die Erklärungen */
	private final JTextPane help;
	/** Register */
	private final JTabbedPane tabs;
	/** Seiten des Dialogs */
	private final List<QueueingCalculatorTabBase> pages;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 */
	public QueueingCalculatorDialog(final Component owner) {
		super(owner,Language.tr("LoadCalculator.Title"));

		pages=new ArrayList<>();

		showCloseButton=true;
		final JPanel content=createGUI(()->Help.topicModal(this,"QueueingCalculator"));
		content.setLayout(new BorderLayout());

		final JScrollPane scroll=new JScrollPane(help=new JTextPane());
		scroll.setPreferredSize(new Dimension(350,scroll.getPreferredSize().height));
		content.add(scroll,BorderLayout.EAST);
		help.setEditable(false);

		content.add(tabs=new JTabbedPane(),BorderLayout.CENTER);
		createTabs();
		tabs.addChangeListener(e->calc());
		calc();

		pack();
		Dimension d=getSize(); d.width=Math.max(d.width,525); setSize(d);
		setLocationRelativeTo(this.owner);
	}

	/**
	 * Stellt die aktuell im rechten Fensterbereich anzuzeigende Hilfeseite an.
	 * @param pageURL	Anzuzeigende Hilfeseite
	 * @see #calc()
	 * @see QueueingCalculatorTabBase#getHelpPage()
	 */
	private void setHelpPage(final URL pageURL) {
		try {
			help.setPage(pageURL);
		} catch (IOException e1) {
			help.setText("Page "+pageURL.toString()+" not found.");
		}
	}

	/**
	 * Fügt einen Tab zu dem Dialog hinzu
	 * @param tab	Neuer Tab
	 * @see #createTabs()
	 * @see #tabs
	 */
	private void addTab(final QueueingCalculatorTabBase tab) {
		final JPanel outer=new JPanel(new BorderLayout());
		tabs.addTab(tab.getTabName(),outer);
		outer.add(tab,BorderLayout.NORTH);

		final Icon icon=tab.getTabIcon();
		if (icon!=null) tabs.setIconAt(tabs.getTabCount()-1,icon);

		pages.add(tab);
	}

	/**
	 * Berechnet die Daten auf allen Seiten neu.
	 */
	private void calc() {
		setHelpPage(pages.get(tabs.getSelectedIndex()).getHelpPage());
		pages.stream().forEach(page->page.calc());
	}

	/**
	 * Legt die einzelnen Dialogseiten an.
	 */
	private void createTabs() {
		/* Dialogseite "Auslastung" */
		addTab(new QueueingCalculatorTabLoad());

		/* Dialogseite "Erlang B" */
		addTab(new QueueingCalculatorTabErlangB());

		/* Dialogseite "Erlang C" */
		addTab(new QueueingCalculatorTabErlangC());

		/* Dialogseite "Erlang C (erweitert)" */
		addTab(new QueueingCalculatorTabErlangCExt());

		/* Dialogseite "Allen-Cunneen" */
		addTab(new QueueingCalculatorTabAllenCunneen());

		/* Dialogseite "Wartezeittoleranz" */
		addTab(new QueueingCalculatorTabWaitingTimeTolerance());
	}
}
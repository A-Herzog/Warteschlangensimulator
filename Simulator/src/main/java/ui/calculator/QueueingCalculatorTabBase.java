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
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import language.Language;
import mathtools.distribution.swing.JOpenURL;
import ui.images.Images;
import ui.tools.FlatLaFHelper;

/**
 * Die Objekte dieser Klasse stellen die Tabs im {@link QueueingCalculatorDialog} dar.
 * @author Alexander Herzog
 * @see QueueingCalculatorDialog
 */
public abstract class QueueingCalculatorTabBase extends JPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -2890937800572790139L;

	/** Name des Tabs im Dialog */
	private final String tabName;
	/** Text für einen möglichen Info-Link unten im Tab (darf leer oder <code>null</code> sein) */
	private final String infoLinkText;
	/** URL für einen möglichen Info-Link unten im Tab (darf leer oder <code>null</code> sein) */
	private final String infoLinkURL;

	/** Rechenergebnisse als einfacher Text */
	private String resultsPlainText;

	/** Feld für die Rechenergebnisse */
	private JLabel result;

	/** Bezeichner "Zeitdauer" */
	protected final String infoTime;

	/** Bezeichner "Zwischenankunftszeit" */
	protected final String infoInterarrivalTime;

	/** Bezeichner "Rate" */
	protected final String infoRate;

	/** Bezeichner "in Prozent" */
	protected final String unitPercent;

	/** Bezeichner "Sekunden" */
	protected final String unitSeconds;

	/** Bezeichner "1/Sekunden" */
	protected final String unitSecondsInv;

	/** Bezeichner "Minuten" */
	protected final String unitMinutes;

	/** Bezeichner "1/Minuten" */
	protected final String unitMinutesInv;

	/** Bezeichner "Stunden" */
	protected final String unitHours;

	/** Bezeichner "1/Stunden" */
	protected final String unitHoursInv;

	/**
	 * Konstruktor der Klasse
	 * @param tabName	Name des Tabs im Dialog
	 * @param topInfo	Infotext oben im Tab (darf leer oder <code>null</code> sein)
	 * @param infoLinkText	Text für einen möglichen Info-Link unten im Tab (darf leer oder <code>null</code> sein)
	 * @param infoLinkURL	URL für einen möglichen Info-Link unten im Tab (darf leer oder <code>null</code> sein)
	 */
	public QueueingCalculatorTabBase(final String tabName, final String topInfo, final String infoLinkText, final String infoLinkURL) {
		setLayout(new BoxLayout(this,BoxLayout.PAGE_AXIS));
		setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

		this.tabName=tabName;
		if (topInfo!=null && !topInfo.trim().isEmpty()) addTopInfo(this,topInfo);
		this.infoLinkText=infoLinkText;
		this.infoLinkURL=infoLinkURL;

		infoTime=Language.tr("LoadCalculator.Units.Time");
		infoInterarrivalTime=Language.tr("LoadCalculator.Units.InterarrivalTime");
		infoRate=Language.tr("LoadCalculator.Units.Rate");
		unitPercent=Language.tr("LoadCalculator.Units.InPercent");
		unitSeconds=Language.tr("LoadCalculator.Units.Seconds");
		unitSecondsInv="1/"+unitSeconds;
		unitMinutes=Language.tr("LoadCalculator.Units.Minutes");
		unitMinutesInv="1/"+unitMinutes;
		unitHours=Language.tr("LoadCalculator.Units.Hours");
		unitHoursInv="1/"+unitHours;
	}

	/**
	 * Konstruktor der Klasse
	 * @param tabName	Name des Tabs im Dialog
	 * @param topInfo	Infotext oben im Tab (darf leer oder <code>null</code> sein)
	 */
	public QueueingCalculatorTabBase(final String tabName, final String topInfo) {
		this(tabName,topInfo,null,null);
	}

	/**
	 * Fügt eine Überschriftzeile zu dem Panel hinzu.
	 * @param title	Überschrift
	 */
	protected final void addSection(final String title) {
		final JPanel panel=new JPanel(new FlowLayout(FlowLayout.LEFT));
		add(panel);
		panel.add(new JLabel("<html><body><b>"+title+"</b></body></html>"));
	}

	/**
	 * Fügt eine Eingabezeile inkl. Beschriftung zu einem Panel hinzu
	 * @param parent	Panel zu dem die Eingabezeile hinzugefügt werden soll
	 * @param title	Überschrift über der Eingabezeile (wird <code>null</code> übergeben, so wird keine Überschrift ausgegeben)
	 * @param label	Beschriftung des Eingabefeldes
	 * @param defaultValue	Vorgabewert für das Eingabefeld
	 * @return	Neu erstelltes und eingefügtes Eingabefeld
	 */
	protected final JTextField addInputLine(final JPanel parent, final String title, final String label, final String defaultValue) {
		JPanel panel;
		JTextField field;

		if (title!=null && !title.isEmpty()) {
			parent.add(panel=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			panel.add(new JLabel("<html><body>"+title+"</body></html>"));
		}

		parent.add(panel=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		panel.add(new JLabel(label));
		panel.add(field=new JTextField(defaultValue,7));
		field.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {calc();}
			@Override public void keyPressed(KeyEvent e) {calc();}
			@Override public void keyReleased(KeyEvent e) {calc();}
		});

		return field;
	}

	/**
	 * Fügt eine Checkbox zu dem Panel hinzu.
	 * @param label	Beschriftung der Checkbox
	 * @param linkTitle	Name des optionalen Links rechts neben der Beschriftung (kann leer oder <code>null</code> sein)
	 * @param linkHref	Linkziel	des optionalen Links rechts neben der Beschriftung (kann leer oder <code>null</code> sein)
	 * @return	Checkbox (bereits in der übergeordnete Panel eingefügt)
	 */
	protected final JCheckBox addCheckBox(final String label, final String linkTitle, final String linkHref) {
		JPanel panel;
		JCheckBox checkBox;

		add(panel=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		panel.add(checkBox=new JCheckBox(label));
		checkBox.addActionListener(e->calc());
		if (linkTitle!=null && !linkTitle.trim().isEmpty() && linkHref!=null && !linkHref.trim().isEmpty()) {
			panel.add(buildInfoLink(linkTitle,linkHref));
		}

		return checkBox;
	}

	/**
	 * Erstellt ein Multi-Eingabeelement, fügt es aber noch nicht in den Tab ein (kann nämlich auch in Unter-Containern verwendet werden)
	 * @param title	Überschrift des Elements
	 * @param isRate	Handelt es sich bei der Eingabegröße um eine Rate?
	 * @return	Multi-Eingabeelement
	 */
	protected final QueueingCalculatorInputPanel getPanel(final String title, final boolean isRate) {
		return new QueueingCalculatorInputPanel(title,()->calc(),isRate);
	}

	/**
	 * Fügt ein Info-Panel ein.
	 * @param parent	Elternelement in das oben das Info-Panel eingefügt werden soll.
	 * @param text	Im Info-Panel anzuzeigender Text
	 */
	private void addTopInfo(final JPanel parent, final String text) {
		final JPanel line;
		parent.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		final String color;
		if (FlatLaFHelper.isDark()) {
			line.setBackground(Color.GRAY);
			color="white";
		} else {
			line.setBackground(Color.LIGHT_GRAY);
			color="black";
		}
		line.add(new JLabel("<html><body><span color=\""+color+"\"><b>"+text+"</b></span></body></html>"));
		parent.add(Box.createVerticalStrut(10));
	}

	/**
	 * Erzeugt ein anklickbares {@link JLabel}-Element
	 * @param text	Anzuzeigende Beschriftung
	 * @param link	Linkziel
	 * @return	Label
	 */
	private JLabel buildInfoLink(final String text, final String link) {
		final JLabel info=new JLabel("<html><body><a href=\"\">"+text+"</a></body></html>");
		info.addMouseListener(new MouseAdapter() {
			@Override public void mouseClicked(MouseEvent e) {JOpenURL.open(QueueingCalculatorTabBase.this,link);}
		});
		info.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		return info;
	}

	/**
	 * Für auf einem Panel einen Info-Link ein
	 * @param parent	Panel auf dem der Link eingefügt werden soll
	 * @param text	Beschriftung des Links
	 * @param link	Linkziel (es muss sich um einen http(s)-Link handeln)
	 * @see #initResults()
	 */
	private void addInfoLink(final JPanel parent, final String text, final String link) {
		parent.add(Box.createVerticalStrut(10));
		final JPanel line;
		parent.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		final JLabel info=buildInfoLink(text,link);
		line.add(info);
		info.setAlignmentX(0);
		parent.add(Box.createVerticalStrut(5));
		parent.add(Box.createVerticalGlue());
	}

	/**
	 * Legt ein neues TabbedPane an
	 * @param parent	Übergeordnetes Element in das das Tabs-Element eingefügt werden soll
	 * @return	Neues TabbedPane
	 * @see QueueingCalculatorTabBase#addTab(JTabbedPane, String)
	 */
	protected JTabbedPane addTabs(final JPanel parent) {
		final JTabbedPane tabs;

		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		parent.add(line);
		line.add(tabs=new JTabbedPane());
		tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		tabs.setAlignmentX(0);
		tabs.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));

		return tabs;
	}

	/**
	 * Fügt ein neues Tab zu einem TabbedPane hinzu
	 * @param parent	Übergeordnetes Element (das TabbedPane)
	 * @param title	Titel des neuen Tabs
	 * @return	Neues Tab
	 * @see QueueingCalculatorTabBase#addTabs(JPanel)
	 */
	protected JPanel addTab(final JTabbedPane parent, final String title) {
		final JPanel outer;
		final JPanel content;

		parent.addTab(title,outer=new JPanel(new BorderLayout()));
		outer.add(content=new JPanel(),BorderLayout.NORTH);
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));
		content.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

		return content;
	}

	/**
	 * Bereitet das Ausgabefeld {@link #result} vor.
	 * @see #result
	 * @see #setResult(String)
	 */
	private void initResults() {
		if (result!=null) return;

		JPanel line;

		add(Box.createVerticalStrut(10));
		add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(result=new JLabel());

		if (infoLinkText!=null && !infoLinkText.trim().isEmpty() && infoLinkURL!=null && !infoLinkURL.trim().isEmpty()) addInfoLink(this,infoLinkText,infoLinkURL);
	}

	/**
	 * Gibt die Ergebnisse unten im Tab aus aus
	 * @param text	Ergebnisse
	 */
	protected final void setResult(final String text) {
		initResults();
		resultsPlainText=text.replace("<br>","\n");
		result.setText("<html><body><b>"+Language.tr("LoadCalculator.Results")+"</b><br>"+text+"</body></html>");
	}

	/**
	 * Gibt einer nutzerdefinierte Fehlermeldung unten im Tab aus
	 * @param text	Fehlermeldung
	 */
	protected final void setError(final String text) {
		initResults();
		result.setText("<html><body><span color=\"red\">"+text+"</span></body></html>");
	}

	/**
	 * Gibt die Standard-Fehlermeldung unten im Tab aus
	 */
	protected final void setError() {
		setError(Language.tr("LoadCalculator.InvalidInput"));
	}

	/**
	 * Berechnet a^c/c! über eine Schleife, d.h. unter Vermeidung von
	 * Auslöschungen und Überläufen.
	 * @param a	Parameter a
	 * @param c	Parameter c
	 * @return	Ergebnis
	 */
	protected final double powerFactorial(final double a, final long c) {
		/* a^c/c! */
		double result=1;
		for (int i=1;i<=c;i++) result*=(a/i);
		return result;
	}

	/**
	 * Wird aufgerufen, wenn die Ergebnisse neu berechnet werden sollen.
	 */
	public abstract void calc();

	/**
	 * Liefert den Namen des Tabs wie im Konstruktor angegeben
	 * @return	Name des Tabs
	 */
	public String getTabName() {
		return tabName;
	}

	/**
	 * Liefert ein Icon für den Tab
	 * @return	Icon oder <code>null</code>, wenn kein Icon für den Tab angezeigt werden soll
	 */
	public Icon getTabIcon() {
		return Images.EXTRAS_QUEUE_FUNCTION.getIcon();
	}

	/**
	 * Liefert den Namen (ohne Pfad und Erweiterung) einer Hilfeseite mit zusätzlichen Erklärungen zum aktuellen Tab.
	 * @return	Namen einer Hilfeseite mit zusätzlichen Erklärungen zum aktuellen Tab.
	 */
	protected abstract String getHelpPageName();

	/**
	 * Liefert die URL einer Hilfeseite mit zusätzlichen Erklärungen zum aktuellen Tab.
	 * @return	URL einer Hilfeseite mit zusätzlichen Erklärungen zum aktuellen Tab
	 */
	public URL getHelpPage() {
		return QueueingCalculatorTabBase.class.getResource("description_"+Language.getCurrentLanguage()+"/"+getHelpPageName()+".html");
	}

	/**
	 * Kopiert die per {@link #setResult(String)} eingestellten Ergebnisse
	 * als unformatierten Text in die Zwischenablage.
	 * @see #setResult(String)
	 */
	public void copyResultsToClipboard() {
		if (resultsPlainText!=null) Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(resultsPlainText),null);
	}
}
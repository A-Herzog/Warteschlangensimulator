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
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import language.Language;
import systemtools.MsgBox;
import ui.images.Images;

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
	/** Text f�r einen m�glichen Info-Link unten im Tab (darf leer oder <code>null</code> sein) */
	private final String infoLinkText;
	/** URL f�r einen m�glichen Info-Link unten im Tab (darf leer oder <code>null</code> sein) */
	private final String infoLinkURL;

	/** Feld f�r die Rechenergebnisse */
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
	 * @param infoLinkText	Text f�r einen m�glichen Info-Link unten im Tab (darf leer oder <code>null</code> sein)
	 * @param infoLinkURL	URL f�r einen m�glichen Info-Link unten im Tab (darf leer oder <code>null</code> sein)
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
	 * F�gt eine Eingabezeile inkl. Beschriftung zu einem Panel hinzu
	 * @param parent	Panel zu dem die Eingabezeile hinzugef�gt werden soll
	 * @param title	�berschrift �ber der Eingabezeile (wird <code>null</code> �bergeben, so wird keine �berschrift ausgegeben)
	 * @param label	Beschriftung des Eingabefeldes
	 * @param defaultValue	Vorgabewert f�r das Eingabefeld
	 * @return	Neu erstelltes und eingef�gtes Eingabefeld
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
		//panel.setAlignmentX(0);
		field.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {calc();}
			@Override public void keyPressed(KeyEvent e) {calc();}
			@Override public void keyReleased(KeyEvent e) {calc();}
		});

		return field;
	}

	/**
	 * Erstellt ein Multi-Eingabeelement, f�gt es aber noch nicht in den Tab ein (kann n�mlich auch in Unter-Containern verwendet werden)
	 * @param title	�berschrift des Elements
	 * @return	Multi-Eingabeelement
	 */
	protected final QueueingCalculatorInputPanel getPanel(final String title) {
		return new QueueingCalculatorInputPanel(title,()->calc());
	}

	/**
	 * F�gt ein Info-Panel ein.
	 * @param parent	Elternelement in das oben das Info-Panel eingef�gt werden soll.
	 * @param text	Im Info-Panel anzuzeigender Text
	 */
	private void addTopInfo(final JPanel parent, final String text) {
		final JPanel line;
		parent.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.setBackground(Color.LIGHT_GRAY);
		line.add(new JLabel("<html><body><b>"+text+"</b></body></html>"));
		parent.add(Box.createVerticalStrut(10));
	}

	/**
	 * F�r auf einem Panel einen Info-Link ein
	 * @param parent	Panel auf dem der Link eingef�gt werden soll
	 * @param text	Beschriftung des Links
	 * @param link	Linkziel (es muss sich um einen http(s)-Link handeln)
	 * @see #initResults()
	 */
	private void addInfoLink(final JPanel parent, final String text, final String link) {
		parent.add(Box.createVerticalStrut(10));
		final JPanel line;
		parent.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		final JLabel info;
		line.add(info=new JLabel("<html><body><a href=\"\">"+text+"</a></body></html>"));
		info.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (!MsgBox.confirmOpenURL(QueueingCalculatorTabBase.this,link)) return;
				try {Desktop.getDesktop().browse(new URI(link));} catch (IOException | URISyntaxException e1) {
					MsgBox.error(QueueingCalculatorTabBase.this,Language.tr("Window.Info.NoInternetConnection"),String.format(Language.tr("Window.Info.NoInternetConnection.ModelOverview"),link));
				}
			}
		});
		info.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		info.setAlignmentX(0);
		parent.add(Box.createVerticalStrut(5));
		parent.add(Box.createVerticalGlue());
	}

	/**
	 * Legt ein neues TabbedPane an
	 * @param parent	�bergeordnetes Element in das das Tabs-Element eingef�gt werden soll
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
	 * F�gt ein neues Tab zu einem TabbedPane hinzu
	 * @param parent	�bergeordnetes Element (das TabbedPane)
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
	 * Berechnet a^c/c! �ber eine Schleife, d.h. unter Vermeidung von
	 * Ausl�schungen und �berl�ufen.
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
	 * Liefert ein Icon f�r den Tab
	 * @return	Icon oder <code>null</code>, wenn kein Icon f�r den Tab angezeigt werden soll
	 */
	public Icon getTabIcon() {
		return Images.EXTRAS_QUEUE_FUNCTION.getIcon();
	}

	/**
	 * Liefert den Namen (ohne Pfad und Erweiterung) einer Hilfeseite mit zus�tzlichen Erkl�rungen zum aktuellen Tab.
	 * @return	Namen einer Hilfeseite mit zus�tzlichen Erkl�rungen zum aktuellen Tab.
	 */
	protected abstract String getHelpPageName();

	/**
	 * Liefert die URL einer Hilfeseite mit zus�tzlichen Erkl�rungen zum aktuellen Tab.
	 * @return	URL einer Hilfeseite mit zus�tzlichen Erkl�rungen zum aktuellen Tab
	 */
	public URL getHelpPage() {
		return QueueingCalculatorTabBase.class.getResource("description_"+Language.getCurrentLanguage()+"/"+getHelpPageName()+".html");
	}
}
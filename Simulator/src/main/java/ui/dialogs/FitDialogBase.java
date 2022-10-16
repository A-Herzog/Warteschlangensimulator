/**
 * Copyright 2022 Alexander Herzog
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
package ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.Serializable;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.DataDistributionImpl;
import mathtools.distribution.swing.JDataDistributionEditPanel;
import mathtools.distribution.swing.JDataLoader;
import mathtools.distribution.tools.DistributionFitterBase;
import mathtools.distribution.tools.FileDropper;
import mathtools.distribution.tools.FileDropperData;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.help.Help;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.tools.FlatLaFHelper;

/**
 * Basisklasse für alle Arten von Verteilungsanpassungen<br>
 * Diese Klasse stellt einen Basisdialog über den Messreihen geladen
 * und Ergebnisse ausgegeben werden können dar.
 * @author Alexander Herzog
 */
public abstract class FitDialogBase extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-2800607452158465402L;

	/**
	 * HTML-Kopf für HTML-formatierte Ausgaben
	 * @see #htmlHeadDark
	 * @see #htmlFoot
	 */
	private static final String htmlHead=
			"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\n"+
					"<html>\n"+
					"<head>\n"+
					"  <style type=\"text/css\">\n"+
					"  body {font-family: Verdana, Lucida, sans-serif; background-color: #FFFFF3; margin: 2px;}\n"+
					"  ul.big li {margin-bottom: 5px;}\n"+
					"  ol.big li {margin-bottom: 5px;}\n"+
					"  a {text-decoration: none;}\n"+
					"  a.box {margin-top: 10px; margin-botton: 10px; border: 1px solid black; background-color: #DDDDDD; padding: 5px;}\n"+
					"  h2 {margin-bottom: 0px;}\n"+
					"  p.red {color: red;}\n"+
					"  </style>\n"+
					"</head>\n"+
					"<body>\n";

	/**
	 * HTML-Kopf für HTML-formatierte Ausgaben (im dunklen Modus)
	 * @see #htmlHead
	 * @see #htmlFoot
	 */
	private static final String htmlHeadDark=
			"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\n"+
					"<html>\n"+
					"<head>\n"+
					"  <style type=\"text/css\">\n"+
					"  body {font-family: Verdana, Lucida, sans-serif; background-color: #404040; margin: 2px; color: silver;}\n"+
					"  ul.big li {margin-bottom: 5px;}\n"+
					"  ol.big li {margin-bottom: 5px;}\n"+
					"  a {text-decoration: none;}\n"+
					"  a.box {margin-top: 10px; margin-botton: 10px; border: 1px solid black; background-color: #DDDDDD; padding: 5px;}\n"+
					"  h2 {margin-bottom: 0px;}\n"+
					"  p.red {color: red;}\n"+
					"  </style>\n"+
					"</head>\n"+
					"<body>\n";

	/**
	 * HTML-Fußbereich für HTML-formatierte Ausgaben
	 * @see #htmlHead
	 * @see #htmlHeadDark
	 */
	private static final String htmlFoot="</body></html>";

	/** Registerreiter */
	protected JTabbedPane tabs;

	/** Eingabefeld für die Messwerte */
	private JTextPane inputValues;
	/** Maximalwert innerhalb der Messwerte */
	protected double inputValuesMax=0;
	/** Darstellung der Messwerte als empirische Verteilung */
	protected JDataDistributionEditPanel inputDistribution;
	/** Mussten Werte gerundet werden */
	protected boolean hasFloat;
	/** Ausgabefeld für die Ergebnisse der Verteilungsanpassung */
	private JTextPane outputText;

	/**
	 * Hält die Ergebnisse zum späteren Kopieren in die Zwischenablage vor
	 * @see #setOutputText(String, String)
	 */
	private String resultPlain;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param title	Titel des Fensters
	 * @param helpTopic	Bezeichner für die Hilfeseite
	 * @param infoPanelTopic	Bezeichner für den Info-Datensatz (darf <code>null</code> sein)
	 */
	public FitDialogBase(final Component owner, final String title, final String helpTopic, final String infoPanelTopic) {
		super(owner,title,false);

		final JPanel content=createGUI(()->Help.topicModal(FitDialogBase.this,helpTopic));
		content.setLayout(new BorderLayout());
		if (infoPanelTopic!=null) InfoPanel.addTopPanel(content,infoPanelTopic);
		content.add(tabs=new JTabbedPane(),BorderLayout.CENTER);
		createTabs(tabs);

		final Dimension screenSize=Toolkit.getDefaultToolkit().getScreenSize();
		if (screenSize.width>=1920) setSize(1280,904);
		else if (screenSize.width>=1440) setSize(950,671);
		else setSize(850,600);
		setMinimumSize(getSize());
		setLocationRelativeTo(this.owner);
		setResizable(true);
	}

	/**
	 * Erstellt die Tabs innerhalb von {@link #tabs}
	 * @param tabs	Tabs-Elternelement
	 */
	private void createTabs(final JTabbedPane tabs) {
		JPanel p,p2;
		JToolBar toolbar;
		JButton b;
		JScrollPane sp;

		/* Dialogseite "Messwerte" */
		tabs.addTab(Language.tr("FitDialog.Tab.Values"),p=new JPanel(new BorderLayout()));
		p.add(toolbar=new JToolBar(),BorderLayout.NORTH);
		toolbar.setFloatable(false);
		toolbar.setBorder(BorderFactory.createEmptyBorder(5,5,0,5));
		toolbar.add(b=new JButton(Language.tr("FitDialog.PasteValues")));
		b.setToolTipText(Language.tr("FitDialog.PasteValues.Tooltip"));
		b.addActionListener(e->pasteFromClipboard());
		b.setIcon(Images.EDIT_PASTE.getIcon());
		toolbar.add(b=new JButton(Language.tr("FitDialog.LoadValues")));
		b.setToolTipText(Language.tr("FitDialog.LoadValues.Tooltip"));
		b.addActionListener(e->loadFromFile());
		b.setIcon(Images.GENERAL_SELECT_FILE.getIcon());
		p.add(p2=new JPanel(new BorderLayout()),BorderLayout.CENTER);
		p2.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		p2.add(sp=new JScrollPane(inputValues=new JTextPane()),BorderLayout.CENTER);
		sp.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		inputValues.setEditable(false);
		inputValues.setContentType("text/html");
		inputValues.setText((FlatLaFHelper.isDark()?htmlHeadDark:htmlHead)+Language.tr("FitDialog.PasteOrLoadValues")+htmlFoot);

		/* Dialogseite "Empirische Verteilung" */
		tabs.addTab(Language.tr("FitDialog.Tab.EmpiricalDistribution"),p=new JPanel(new BorderLayout()));
		p.add(inputDistribution=new JDataDistributionEditPanel(new DataDistributionImpl(10,10),JDataDistributionEditPanel.PlotMode.PLOT_DENSITY),BorderLayout.CENTER);
		inputDistribution.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));

		/* Dialogseite "Anpassung" */
		tabs.addTab(Language.tr("FitDialog.Tab.Fit"),p=new JPanel(new BorderLayout()));
		p.add(toolbar=new JToolBar(),BorderLayout.NORTH);
		toolbar.setFloatable(false);
		toolbar.setBorder(BorderFactory.createEmptyBorder(5,5,0,5));
		toolbar.add(b=new JButton(Language.tr("FitDialog.CopyResults")));
		b.addActionListener(e->copyResults());
		b.setToolTipText(Language.tr("FitDialog.CopyResults.Tooltip"));
		b.setIcon(Images.EDIT_COPY.getIcon());
		p.add(p2=new JPanel(new BorderLayout()),BorderLayout.CENTER);
		p2.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		p2.add(sp=new JScrollPane(outputText=new JTextPane()),BorderLayout.CENTER);
		sp.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		outputText.setEditable(false);
		outputText.setContentType("text/html");
		outputText.setText((FlatLaFHelper.isDark()?htmlHeadDark:htmlHead)+htmlFoot);

		final JPanel outputDistribution=outputDistributionPanel();

		if (outputDistribution!=null) {
			/* Dialogseite "Angepasste Verteilung" */
			tabs.addTab(Language.tr("FitDalog.Tab.FittedDistribution"),outputDistribution);
		}

		/* Drag&Drop */
		activateDragAndDrop(this);
		activateDragAndDrop(inputValues);
		activateDragAndDrop(outputText);

		/* Icons auf den Tabs */
		tabs.setIconAt(0,Images.FIT_PAGE_VALUES.getIcon());
		tabs.setIconAt(1,Images.FIT_PAGE_EMPIRICAL_DISTRIBUTION.getIcon());
		tabs.setIconAt(2,Images.FIT_PAGE_FIT.getIcon());
		if (outputDistribution!=null) tabs.setIconAt(3,Images.FIT_PAGE_RESULT.getIcon());
	}

	/**
	 * Liefert ein optionales weiteres Ausgabe-Panel (z.B. zur Anzeige einer Verteilung)
	 * @return	Weiteres Ausgabe-Panel oder <code>null</code>
	 */
	protected JPanel outputDistributionPanel() {
		return null;
	}

	/**
	 * Lädt Messwerte aus der Zwischenablage.
	 */
	private void pasteFromClipboard() {
		if (loadValuesFromClipboard()) {
			calcFit();
		} else {
			MsgBox.error(FitDialogBase.this,Language.tr("FitDalog.InvalidDataTitle"),Language.tr("FitDalog.InvalidDataClipboard"));
		}
	}

	/**
	 * Lädt Messwerte aus einer Datei.
	 */
	private void loadFromFile() {
		if (loadValuesFromFile()) {
			calcFit();
		} else {
			MsgBox.error(FitDialogBase.this,Language.tr("FitDalog.InvalidDataTitle"),Language.tr("FitDalog.InvalidDataFile"));
		}
	}

	/**
	 * Lädt die Werte per Drag&amp;drop aus einer Datei.
	 * @param data	Drag&amp;drop-Objekt
	 * @return	Liefert <code>true</code>, wenn die Daten verarbeitet werden konnten.
	 */
	private boolean fileDrop(final FileDropperData data) {
		return loadValuesFromArray(JDataLoader.loadNumbersTwoRowsFromFile(FitDialogBase.this,data.getFile(),1,Integer.MAX_VALUE));
	}

	/**
	 * Registriert eine Komponente als Drag&amp;Drop-Ziel
	 * @param component	Zu registrierende Komponente
	 */
	private void activateDragAndDrop(final Component component) {
		new FileDropper(component,e->{
			if (fileDrop((FileDropperData)(e.getSource()))) {calcFit();}
		});
	}

	/**
	 * Lädt die Werte aus einem Array
	 * @param newValues	Zu ladende Werte
	 * @return	Liefert <code>true</code>, wenn die Daten verarbeitet werden konnten.
	 */
	private boolean loadValuesFromArray(double[][] newValues) {
		if (newValues==null || newValues.length==0 || newValues[0]==null || newValues[0].length==0) return false;

		/* Messwerte-Diagramm füllen */
		final Object[] obj=DistributionFitterBase.dataDistributionFromValues(newValues);
		if (obj==null) return false;
		inputDistribution.setDistribution((DataDistributionImpl)obj[0]);
		double[] density=((DataDistributionImpl)obj[0]).densityData;
		inputValuesMax=density.length-1;
		double inputValueMin=-1;
		for (int i=0;i<density.length;i++) if (density[i]!=0) {inputValueMin=i; break;}
		hasFloat=(Boolean)obj[1];

		/* Messwerte-Liste füllen */
		StringBuilder sb=new StringBuilder();
		sb.append("<h2>"+Language.tr("FitDalog.Loaded.Title")+"</h2>");
		sb.append("<p>");
		sb.append(String.format(Language.tr("FitDalog.Loaded.Info"),newValues[0].length)+"<br>");
		if (inputValueMin>=0) sb.append(String.format(Language.tr("FitDalog.Loaded.Min"),NumberTools.formatNumber(inputValueMin))+"<br>");
		sb.append(String.format(Language.tr("FitDalog.Loaded.Max"),NumberTools.formatNumber(inputValuesMax)));
		sb.append("</p>");
		sb.append("<h2>"+Language.tr("FitDalog.Loaded.List")+"</h2>");
		sb.append("<p>");
		for (int i=0;i<Math.min(100,newValues[0].length);i++) {
			if (newValues.length==1) sb.append(NumberTools.formatNumber(newValues[0][i],2)+"<br>"); else sb.append(NumberTools.formatNumber(newValues[0][i],0)+": "+NumberTools.formatNumber(newValues[1][i],2)+"<br>");
		}
		if (newValues[0].length>=100) sb.append("...<br>\n");
		sb.append("</p>");

		inputValues.setText((FlatLaFHelper.isDark()?htmlHeadDark:htmlHead)+sb.toString()+htmlFoot);
		inputValues.setSelectionStart(0);
		inputValues.setSelectionEnd(0);

		return true;
	}

	/**
	 * Lädt die Werte aus der Zwischenablage.
	 * @return	Liefert <code>true</code>, wenn die Daten verarbeitet werden konnten.
	 */
	private boolean loadValuesFromClipboard() {
		Transferable cont=getToolkit().getSystemClipboard().getContents(this);
		if (cont==null) return false;
		String s=null;
		try {s=(String)cont.getTransferData(DataFlavor.stringFlavor);} catch (Exception ex) {return false;}
		if (s==null) return false;

		return loadValuesFromArray(JDataLoader.loadNumbersTwoRowsFromString(FitDialogBase.this,s,1,Integer.MAX_VALUE));
	}

	/**
	 * Lädt die Werte aus einer Datei.
	 * @return	Liefert <code>true</code>, wenn die Daten verarbeitet werden konnten.
	 */
	private boolean loadValuesFromFile() {
		return loadValuesFromArray(JDataLoader.loadNumbersTwoRows(FitDialogBase.this,Language.tr("FitDialog.LoadValues"),1,Integer.MAX_VALUE));
	}

	/**
	 * Führt die Verteilungsanpassung durch.
	 */
	protected abstract void calcFit();

	/**
	 * Ruft die Verarbeitungsmethode des übergebenen Fitters mit den
	 * geladenen Eingabedaten auf und überträgt die Ergebnisse direkt
	 * in die Ausgabefelder.
	 * @param fitter	Zu verwendender Fitter
	 */
	protected final void calcFitIntern(final DistributionFitterBase fitter) {
		fitter.processDensity(inputDistribution.getDistribution());

		String info="";
		if (hasFloat) info="<h2>"+Language.tr("Dialog.Title.Information")+"</h2><p>"+Language.tr("FitDialog.InfoValuesRounded")+"</p>";
		setOutputText(fitter.getResult(true)+info,fitter.getResult(false));

		tabs.setSelectedIndex(2);
	}

	/**
	 * Stellt einen Ausgabetext ein.
	 * @param textHTML	HTML-formatierter Ausgabetext
	 * @param textPlain	Unformatierter Ausgabetext
	 */
	protected final void setOutputText(final String textHTML, final String textPlain) {
		outputText.setText((FlatLaFHelper.isDark()?htmlHeadDark:htmlHead)+"<h2>"+Language.tr("FitDalog.FittedDistribution")+"</h2>"+textHTML+htmlFoot);
		resultPlain=textPlain;
	}

	/**
	 * Kopiert die Ergebnisse in die Zwischenablage.
	 */
	private void copyResults() {
		getToolkit().getSystemClipboard().setContents(new StringSelection(resultPlain),null);
	}
}

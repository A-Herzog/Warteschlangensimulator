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
import mathtools.MultiTable;
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
	 * @param showGenerateSamplesButton	Soll die Schaltfläche zur Generieren von Testmesswerten angezeigt werden?
	 * @param fitAllDistributions	Handelt es sich um einen Fit gegen alle dafür taugliche Verteilungen (<code>true</code>) oder nur gegen einen bestimmten Verteilungstyp (<code>false</code>)
	 */
	public FitDialogBase(final Component owner, final String title, final String helpTopic, final String infoPanelTopic, final boolean showGenerateSamplesButton, final boolean fitAllDistributions) {
		super(owner,title,false);

		final JPanel content=createGUI(()->Help.topicModal(FitDialogBase.this,helpTopic));
		content.setLayout(new BorderLayout());
		if (infoPanelTopic!=null) InfoPanel.addTopPanel(content,infoPanelTopic);
		content.add(tabs=new JTabbedPane(),BorderLayout.CENTER);
		createTabs(tabs,showGenerateSamplesButton,fitAllDistributions);

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
	 * @param showGenerateSamplesButton	Soll die Schaltfläche zur Generieren von Testmesswerten angezeigt werden?
	 * @param fitAllDistributions	Handelt es sich um einen Fit gegen alle dafür taugliche Verteilungen (<code>true</code>) oder nur gegen einen bestimmten Verteilungstyp (<code>false</code>)
	 */
	private void createTabs(final JTabbedPane tabs, final boolean showGenerateSamplesButton, final boolean fitAllDistributions) {
		JPanel p,p2;
		JToolBar toolbar;
		JButton button;
		JScrollPane scroll;

		/* Dialogseite "Messwerte" */
		tabs.addTab(Language.tr("FitDialog.Tab.Values"),p=new JPanel(new BorderLayout()));
		p.add(toolbar=new JToolBar(),BorderLayout.NORTH);
		toolbar.setFloatable(false);
		toolbar.setBorder(BorderFactory.createEmptyBorder(5,5,0,5));
		toolbar.add(button=new JButton(Language.tr("FitDialog.PasteValues")));
		button.setToolTipText(Language.tr("FitDialog.PasteValues.Tooltip"));
		button.addActionListener(e->pasteFromClipboard());
		button.setIcon(Images.EDIT_PASTE.getIcon());
		toolbar.add(button=new JButton(Language.tr("FitDialog.LoadValues")));
		button.setToolTipText(Language.tr("FitDialog.LoadValues.Tooltip"));
		button.addActionListener(e->loadFromFile());
		button.setIcon(Images.GENERAL_SELECT_FILE.getIcon());
		if (showGenerateSamplesButton) {
			toolbar.add(button=new JButton(Language.tr("FitDialog.GenerateValues")));
			button.setToolTipText(Language.tr("FitDialog.GenerateValues.Tooltip"));
			button.addActionListener(e->generateSamples());
			button.setIcon(Images.EXTRAS_CALCULATOR.getIcon());
		}
		p.add(p2=new JPanel(new BorderLayout()),BorderLayout.CENTER);
		p2.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		p2.add(scroll=new JScrollPane(inputValues=new JTextPane()),BorderLayout.CENTER);
		scroll.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		inputValues.setEditable(false);
		inputValues.setContentType("text/html");
		final String distributionCountInfo=fitAllDistributions?("<br><br>"+String.format(Language.tr("FitDialog.DistributionCountInfo"),DistributionFitterBase.getFitDistributionCount())):"";
		inputValues.setText((FlatLaFHelper.isDark()?htmlHeadDark:htmlHead)+Language.tr("FitDialog.PasteOrLoadValues")+distributionCountInfo+htmlFoot);

		/* Dialogseite "Empirische Verteilung" */
		tabs.addTab(Language.tr("FitDialog.Tab.EmpiricalDistribution"),p=new JPanel(new BorderLayout()));
		p.add(inputDistribution=new JDataDistributionEditPanel(new DataDistributionImpl(10,10),JDataDistributionEditPanel.PlotMode.PLOT_BOTH),BorderLayout.CENTER);
		inputDistribution.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));

		/* Dialogseite "Anpassung" */
		tabs.addTab(Language.tr("FitDialog.Tab.Fit"),p=new JPanel(new BorderLayout()));
		p.add(toolbar=new JToolBar(),BorderLayout.NORTH);
		toolbar.setFloatable(false);
		toolbar.setBorder(BorderFactory.createEmptyBorder(5,5,0,5));
		toolbar.add(button=new JButton(Language.tr("FitDialog.CopyResults")));
		button.addActionListener(e->copyResults());
		button.setToolTipText(Language.tr("FitDialog.CopyResults.Tooltip"));
		button.setIcon(Images.EDIT_COPY.getIcon());
		p.add(p2=new JPanel(new BorderLayout()),BorderLayout.CENTER);
		p2.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		p2.add(scroll=new JScrollPane(outputText=new JTextPane()),BorderLayout.CENTER);
		scroll.setBorder(BorderFactory.createLineBorder(Color.GRAY));
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
		final MultiTable multiTable=JDataLoader.loadTable(owner,Language.tr("FitDialog.LoadValues"));
		if (multiTable==null) return;
		if (loadValuesFromFile(multiTable)) {
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
	 * Reagiert auf einen Klick auf die "Messwerte generieren"-Schaltfläche
	 */
	private void generateSamples() {
		final double[] values=generateSampleValues();
		if (values==null) return;
		if (loadValuesFromArray(new double[][] {values})) {
			calcFit();
		}
	}

	/**
	 * Wird aufgerufen, wenn Testmesswerte generiert werden sollen.<br>
	 * Diese Methode muss von abgeleiteten Klassen überschrieben werden, wenn Testmesswerte bereitgestellt werden sollen.
	 * @return	Liefert im Erfolgsfalls die Testmesswerte, sonst <code>null</code>
	 */
	protected double[] generateSampleValues() {
		return null;
	}

	/**
	 * Lädt die Werte aus einem Array
	 * @param newValues	Zu ladende Werte
	 * @return	Liefert <code>true</code>, wenn die Daten verarbeitet werden konnten.
	 */
	private boolean loadValuesFromArray(double[][] newValues) {
		if (newValues==null || newValues.length==0 || newValues[0]==null || newValues[0].length==0) return false;

		final Object[] obj=DistributionFitterBase.dataDistributionFromValues(newValues);
		if (obj==null) return false;
		final DataDistributionImpl dataDist=(DataDistributionImpl)obj[0];
		inputDistribution.setDistribution(dataDist);
		final double[] density=dataDist.densityData;
		hasFloat=(Boolean)obj[1];

		/* Messwerte-Diagramm füllen */
		inputValuesMax=density.length-1;
		double inputValuesMin=-1;
		for (int i=0;i<density.length;i++) if (density[i]!=0) {inputValuesMin=i; break;}

		inputValuesMax/=dataDist.getArgumentScaleFactor();
		inputValuesMin/=dataDist.getArgumentScaleFactor();

		/* Messwerte-Liste füllen */
		final StringBuilder info=new StringBuilder();
		info.append("<h2>"+Language.tr("FitDalog.Loaded.Title")+"</h2>");
		info.append("<p>");
		info.append(String.format(Language.tr("FitDalog.Loaded.Info"),NumberTools.formatLong(newValues[0].length))+"<br>");
		if (inputValuesMin>=0) info.append(String.format(Language.tr("FitDalog.Loaded.Min"),NumberTools.formatNumber(inputValuesMin))+"<br>");
		info.append(String.format(Language.tr("FitDalog.Loaded.Max"),NumberTools.formatNumber(inputValuesMax)));
		info.append("</p>");
		info.append("<h2>"+Language.tr("FitDalog.Loaded.List")+"</h2>");
		info.append("<p>");
		for (int i=0;i<Math.min(100,newValues[0].length);i++) {
			if (newValues.length==1) info.append(NumberTools.formatNumber(newValues[0][i],2)+"<br>"); else info.append(NumberTools.formatNumber(newValues[0][i],0)+": "+NumberTools.formatNumber(newValues[1][i],2)+"<br>");
		}
		if (newValues[0].length>=100) info.append("...<br>\n");
		info.append("</p>");

		inputValues.setText((FlatLaFHelper.isDark()?htmlHeadDark:htmlHead)+info.toString()+htmlFoot);
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
	 * @param multiTable	Tabellenobjekt mit den zu verarbeitenden Daten
	 * @return	Liefert <code>true</code>, wenn die Daten verarbeitet werden konnten.
	 */
	private boolean loadValuesFromFile(final MultiTable multiTable) {
		return loadValuesFromArray(JDataLoader.loadNumbersTwoRowsFromMultiTable(FitDialogBase.this,multiTable,1,Integer.MAX_VALUE));
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
		outputText.setCaretPosition(0);
		resultPlain=textPlain;
	}

	/**
	 * Kopiert die Ergebnisse in die Zwischenablage.
	 */
	private void copyResults() {
		getToolkit().getSystemClipboard().setContents(new StringSelection(resultPlain),null);
	}
}

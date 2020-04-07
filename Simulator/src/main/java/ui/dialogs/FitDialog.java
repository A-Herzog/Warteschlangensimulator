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
package ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
import mathtools.distribution.swing.JDistributionPanel;
import mathtools.distribution.tools.DistributionFitter;
import mathtools.distribution.tools.FileDropper;
import mathtools.distribution.tools.FileDropperData;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.help.Help;
import ui.images.Images;

/**
 * Diese Klasse stellt einen Dialog zur Anpassung einer Verteilung
 * an aufgezeichnete Messwerte zur Verfügung.
 * @author Alexander Herzog
 * @version 1.0
 * @see DistributionFitter
 */
public class FitDialog extends BaseDialog {
	private static final long serialVersionUID = 8263152374892311273L;

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
	private static final String htmlFoot="</body></html>";

	private JTabbedPane tabs;

	private JTextPane inputValues;
	private double inputValuesMax=0;
	private JDataDistributionEditPanel inputDistribution;
	private boolean hasFloat;
	private String outputReportPlain;
	private String outputReportHTML;
	private JTextPane outputText;
	private JDistributionPanel outputDistribution;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 */
	public FitDialog(final Component owner) {
		super(owner,Language.tr("FitDialog.Title"),false);
		JPanel content=createGUI(()->Help.topicModal(FitDialog.this,"Fit"));
		content.setLayout(new BorderLayout());
		content.add(tabs=new JTabbedPane(),BorderLayout.CENTER);
		createTabs(tabs);
		setSizeRespectingScreensize(725,500);
		setMinSizeRespectingScreensize(725,500);
		setLocationRelativeTo(this.owner);
		setResizable(true);
	}

	private void createTabs(JTabbedPane tabs) {
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
		b.addActionListener(new ButtonListener(0));
		b.setIcon(Images.EDIT_PASTE.getIcon());
		toolbar.add(b=new JButton(Language.tr("FitDialog.LoadValues")));
		b.setToolTipText(Language.tr("FitDialog.LoadValues.Tooltip"));
		b.addActionListener(new ButtonListener(1));
		b.setIcon(Images.GENERAL_SELECT_FILE.getIcon());
		p.add(p2=new JPanel(new BorderLayout()),BorderLayout.CENTER);
		p2.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		p2.add(sp=new JScrollPane(inputValues=new JTextPane()),BorderLayout.CENTER);
		sp.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		inputValues.setEditable(false);
		inputValues.setContentType("text/html");
		inputValues.setText(htmlHead+Language.tr("FitDialog.PasteOrLoadValues")+htmlFoot);

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
		b.addActionListener(new ButtonListener(2));
		b.setToolTipText(Language.tr("FitDialog.CopyResults.Tooltip"));
		b.setIcon(Images.EDIT_COPY.getIcon());
		p.add(p2=new JPanel(new BorderLayout()),BorderLayout.CENTER);
		p2.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		p2.add(sp=new JScrollPane(outputText=new JTextPane()),BorderLayout.CENTER);
		sp.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		outputText.setEditable(false);
		outputText.setContentType("text/html");
		outputText.setText(htmlHead+htmlFoot);

		/* Dialogseite "Angepasste Verteilung" */
		tabs.addTab(Language.tr("FitDalog.Tab.FittedDistribution"),p=new JPanel(new BorderLayout()));
		p.add(outputDistribution=new JDistributionPanel(new DataDistributionImpl(10,10),10,false),BorderLayout.CENTER);
		outputDistribution.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));

		new FileDropper(this,new ButtonListener(3));
		new FileDropper(inputValues,new ButtonListener(3));
		new FileDropper(outputText,new ButtonListener(3));

		tabs.setIconAt(0,Images.FIT_PAGE_VALUES.getIcon());
		tabs.setIconAt(1,Images.FIT_PAGE_EMPIRICAL_DISTRIBUTION.getIcon());
		tabs.setIconAt(2,Images.FIT_PAGE_FIT.getIcon());
		tabs.setIconAt(3,Images.FIT_PAGE_RESULT.getIcon());
	}

	private class ButtonListener implements ActionListener {
		private final int buttonNr;

		public ButtonListener(final int buttonNr) {
			this.buttonNr=buttonNr;
		}

		private boolean loadValuesFromArray(double newValues[][]) {
			if (newValues==null || newValues.length==0 || newValues[0]==null || newValues[0].length==0) return false;

			/* Messwerte-Liste füllen */
			StringBuilder sb=new StringBuilder();
			sb.append("<h2>"+Language.tr("FitDalog.Loaded.Title")+"</h2>");
			sb.append("<p>"+String.format(Language.tr("FitDalog.Loaded.Info"),newValues[0].length)+"</p>");
			sb.append("<h2>"+Language.tr("FitDalog.Loaded.List")+"</h2>");
			sb.append("<p>");
			for (int i=0;i<newValues[0].length;i++) {
				if (newValues.length==1) sb.append(NumberTools.formatNumber(newValues[0][i],2)+"<br>"); else sb.append(NumberTools.formatNumber(newValues[0][i],0)+": "+NumberTools.formatNumber(newValues[1][i],2)+"<br>");
			}
			sb.append("</p>");

			/* Messwerte-Diagramm füllen */
			final Object[] obj=DistributionFitter.dataDistributionFromValues(newValues);
			if (obj==null) return false;
			inputDistribution.setDistribution((DataDistributionImpl)obj[0]);
			inputValuesMax=((DataDistributionImpl)obj[0]).densityData.length;
			hasFloat=(Boolean)obj[1];

			inputValues.setText(htmlHead+sb.toString()+htmlFoot);
			inputValues.setSelectionStart(0);
			inputValues.setSelectionEnd(0);

			return true;
		}

		private boolean loadValuesFromClipboard() {
			Transferable cont=getToolkit().getSystemClipboard().getContents(this);
			if (cont==null) return false;
			String s=null;
			try {s=(String)cont.getTransferData(DataFlavor.stringFlavor);} catch (Exception ex) {return false;}
			if (s==null) return false;

			return loadValuesFromArray(JDataLoader.loadNumbersTwoRowsFromString(FitDialog.this,s,1,Integer.MAX_VALUE));
		}

		private boolean loadValuesFromFile() {
			return loadValuesFromArray(JDataLoader.loadNumbersTwoRows(FitDialog.this,Language.tr("FitDialog.LoadValues"),1,Integer.MAX_VALUE));
		}

		private void calcFit() {
			final DistributionFitter fitter=new DistributionFitter();
			fitter.process(inputDistribution.getDistribution());
			outputReportPlain=fitter.getResult(false);
			outputReportHTML=fitter.getResult(true);
			String info="";
			if (hasFloat) info="<h2>"+Language.tr("Dialog.Title.Information")+"</h2><p>"+Language.tr("FitDialog.InfoValuesRounded")+"</p>";
			outputText.setText(htmlHead+"<h2>"+Language.tr("FitDalog.FittedDistribution")+"</h2>"+outputReportHTML+info+htmlFoot);
			outputDistribution.setMaxXValue(inputValuesMax);
			outputDistribution.setDistribution(fitter.getFitDistribution());
		}

		private void copyResults() {
			getToolkit().getSystemClipboard().setContents(new StringSelection(outputReportPlain),null);
		}

		private boolean fileDrop(final FileDropperData data) {
			return loadValuesFromArray(JDataLoader.loadNumbersTwoRowsFromFile(FitDialog.this,data.getFile(),1,Integer.MAX_VALUE));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			switch (buttonNr) {
			case 0:
				if (loadValuesFromClipboard()) {
					tabs.setSelectedIndex(2); calcFit();
				} else {
					MsgBox.error(FitDialog.this,Language.tr("FitDalog.InvalidDataTitle"),Language.tr("FitDalog.InvalidDataClipboard"));
				}
				break;
			case 1:
				if (loadValuesFromFile()) {
					tabs.setSelectedIndex(2); calcFit();
				} else {
					MsgBox.error(FitDialog.this,Language.tr("FitDalog.InvalidDataTitle"),Language.tr("FitDalog.InvalidDataFile"));
				}
				break;
			case 2: copyResults(); break;
			case 3: if (fileDrop((FileDropperData)(e.getSource()))) {tabs.setSelectedIndex(2); calcFit();} break;
			}
		}
	}
}
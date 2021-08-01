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
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;

import language.Language;
import mathtools.NumberTools;
import mathtools.Table;
import mathtools.distribution.swing.CommonVariables;
import mathtools.distribution.swing.JDistributionEditorPanel;
import mathtools.distribution.swing.JDistributionPanel;
import mathtools.distribution.tools.AbstractDistributionWrapper;
import mathtools.distribution.tools.DistributionRandomNumber;
import statistics.StatisticsDataPerformanceIndicatorWithNegativeValues;
import systemtools.MsgBox;
import tools.SetupData;
import ui.images.Images;

/**
 * Wahrscheinlichkeitsverteilungsplotter-Tab innerhalb des Rechner-Fensters
 * @author Alexander Herzog
 * @see CalculatorWindow
 */
public class CalculatorWindowPageDistributions extends CalculatorWindowPage {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=2829533095789863083L;

	/**
	 * Gesamtes Fenster
	 */
	private final Window window;

	/** Wahrscheinlichkeitsverteilungsplotter */
	private final JDistributionPanel distributionPlotter;
	/** Eingabefelder für den Wahrscheinlichkeitsverteilungsplotter */
	private final JDistributionEditorPanel distributionEditor;

	/**
	 * Anzahl an zu erzeugenden Zufallszahlen
	 * @see #showGenerateRandomNumbersPopup(Component)
	 * @see #randomNumbersIndicators()
	 * @see #randomNumbersCopy()
	 * @see #randomNumbersSave()
	 */
	private long randomNumberCount;

	/**
	 * Konstruktor der Klasse
	 * @param window	Gesamtes Fenster
	 * @param tabs	Tabs-Element in das dieses Tab eingefügt werden soll
	 * @param initialDistribution	Initial auszuwählende Verteilung (kann <code>null</code> sein)
	 */
	public CalculatorWindowPageDistributions(final Window window, final JTabbedPane tabs, final AbstractDistributionWrapper initialDistribution) {
		super(tabs);
		this.window=window;

		randomNumberCount=1_000_000;

		/* Symbolleiste */
		final JToolBar toolbar=new JToolBar(SwingConstants.HORIZONTAL);
		add(toolbar,BorderLayout.NORTH);
		toolbar.setFloatable(false);
		final JButton button=new JButton(Language.tr("CalculatorDialog.Tab.Distributions.GenerateRandomNumbers"),Images.EXTRAS_CALCULATOR.getIcon());
		toolbar.add(button);
		button.addActionListener(e->showGenerateRandomNumbersPopup(button));

		/* Wahrscheinlichkeitsverteilungsplotter */
		add(distributionPlotter=new JDistributionPanel(new ExponentialDistribution(100),200,false),BorderLayout.CENTER);
		distributionPlotter.setImageSaveSize(SetupData.getSetup().imageSize);
		distributionPlotter.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		distributionPlotter.setPlotType(JDistributionPanel.BOTH);
		AbstractRealDistribution distribution=null;
		if (initialDistribution!=null) {
			distribution=initialDistribution.getDistribution(100,50);
			if (distribution==null) distribution=initialDistribution.getDefaultDistribution();
		}
		if (distribution==null) distribution=new ExponentialDistribution(100);

		/* Eingabefelder für den Wahrscheinlichkeitsverteilungsplotter */
		add(distributionEditor=new JDistributionEditorPanel(distribution,1000,e->updateDistribution(),true),BorderLayout.SOUTH);

		/* Start */
		updateDistribution();
	}

	@Override
	protected String getTabTitle() {
		return Language.tr("CalculatorDialog.Tab.Distributions");
	}

	@Override
	protected Images getTabIcon() {
		return Images.EXTRAS_CALCULATOR_DISTRIBUTION;
	}

	/**
	 * Veränderte Einstellungen zu der Wahrscheinlichkeitsverteilung an den Plotter übertragen.
	 */
	private void updateDistribution() {
		if (distributionEditor!=null) distributionPlotter.setDistribution(distributionEditor.getDistribution());
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
		showPage();
	}

	/**
	 * Zeigt ein Popupmenü mit Befehlen zur Erzeugung von Zufallszahlen gemäß der gewählten Verteilung an.
	 * @param invoker	Aufrufer (zur Ausrichtung des Menüs)
	 */
	private void showGenerateRandomNumbersPopup(final Component invoker) {
		final JPopupMenu popup=new JPopupMenu();

		JMenuItem item;

		final JPanel editorPanel=new JPanel();
		editorPanel.setLayout(new BoxLayout(editorPanel,BoxLayout.PAGE_AXIS));
		final JLabel label=new JLabel(Language.tr("CalculatorDialog.Tab.Distributions.GenerateRandomNumbers.Count")+":");
		editorPanel.add(label);
		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		line.setBorder(BorderFactory.createEmptyBorder(0,25,0,0));
		final JTextField editor=new JTextField(""+randomNumberCount,10);
		line.add(editor);
		editorPanel.add(line);
		label.setLabelFor(editor);
		editor.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {processInput(editor);}
			@Override public void keyReleased(KeyEvent e) {processInput(editor);}
			@Override public void keyPressed(KeyEvent e) {processInput(editor);}
		});
		popup.add(editorPanel);

		popup.addSeparator();

		popup.add(item=new JMenuItem(Language.tr("CalculatorDialog.Tab.Distributions.GenerateRandomNumbers.DetermineCharacteristics"),Images.EXTRAS_CALCULATOR.getIcon()));
		item.addActionListener(e->randomNumbersIndicators());

		popup.add(item=new JMenuItem(Language.tr("CalculatorDialog.Tab.Distributions.GenerateRandomNumbers.Copy"),Images.EDIT_COPY.getIcon()));
		item.addActionListener(e->randomNumbersCopy());

		popup.add(item=new JMenuItem(Language.tr("CalculatorDialog.Tab.Distributions.GenerateRandomNumbers.Save"),Images.GENERAL_SAVE.getIcon()));
		item.addActionListener(e->randomNumbersSave());

		popup.show(invoker,0,invoker.getHeight());
	}

	/**
	 * Verarbeitet die Eingaben in dem Eingabefeld zur Festlegung der Anzahl an zu erzeugenden Zufallszahlen
	 * @param input	Eingabefeld zur Festlegung der Anzahl an zu erzeugenden Zufallszahlen
	 * @see #showGenerateRandomNumbersPopup(Component)
	 */
	private void processInput(final JTextField input) {
		final Long L=NumberTools.getPositiveLong(input,true);
		if (L!=null) randomNumberCount=L.longValue();
	}

	/**
	 * Erzeugt eine Reihe von Zufallszahlen, ermittelt die Kenngrößen der Messreihe und zeigt diese an.
	 */
	private void randomNumbersIndicators() {
		final AbstractRealDistribution distribution=distributionEditor.getDistribution();

		final StatisticsDataPerformanceIndicatorWithNegativeValues indicator=new StatisticsDataPerformanceIndicatorWithNegativeValues(null,-1,-1);
		for (int i=0;i<randomNumberCount;i++) {
			indicator.add(DistributionRandomNumber.random(distribution));
		}

		final StringBuilder info=new StringBuilder();
		info.append(String.format(Language.tr("CalculatorDialog.Tab.Distributions.GenerateRandomNumbers.Generated")+": %s",NumberTools.formatLong(indicator.getCount()))+"\n");
		info.append(String.format(Language.tr("Distribution.Mean")+": E=%s",NumberTools.formatNumber(indicator.getMean(),3))+"\n");
		info.append(String.format(Language.tr("Statistics.Variance")+": Var=%s",NumberTools.formatNumber(indicator.getVar(),3))+"\n");
		info.append(String.format(Language.tr("Distribution.StdDev")+": Std=%s",NumberTools.formatNumber(indicator.getSD(),3))+"\n");
		info.append(String.format(Language.tr("Distribution.CV")+": CV=%s",NumberTools.formatNumber(indicator.getCV(),3))+"\n");
		info.append(String.format(Language.tr("Distribution.Skewness")+": Sk=%s",NumberTools.formatNumber(indicator.getSk(),3))+"\n");

		MsgBox.info(this,"Zufallszahlen erzeugen",info.toString());
	}

	/**
	 * Erzeugt eine Reihe von Zufallszahlen und kopiert diese in die Zwischenablage.
	 */
	private void randomNumbersCopy() {
		final AbstractRealDistribution distribution=distributionEditor.getDistribution();

		final StringBuilder result=new StringBuilder();
		for (int i=0;i<randomNumberCount;i++) {
			result.append(NumberTools.formatNumberMax(DistributionRandomNumber.random(distribution)));
		}

		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(result.toString()),null);
	}

	/**
	 * Erzeugt eine Reihe von Zufallszahlen und speichert die als Datei.
	 * @return	Liefert <code>true</code>, wenn die Zufallszahlen gespeichert werden konnten
	 */
	private boolean randomNumbersSave() {
		final JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		fc.setDialogTitle("Zufallszahlen speichern");
		final FileFilter txt=new FileNameExtensionFilter(Table.FileTypeText+" (*.txt, *.tsv)","txt","tsv");
		fc.addChoosableFileFilter(txt);
		fc.setFileFilter(txt);
		fc.setAcceptAllFileFilterUsed(false);
		if (fc.showSaveDialog(window)!=JFileChooser.APPROVE_OPTION) return false;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();
		if (file.getName().indexOf('.')<0) {
			if (fc.getFileFilter()==txt) file=new File(file.getAbsoluteFile()+".txt");
		}

		final AbstractRealDistribution distribution=distributionEditor.getDistribution();
		final String lineSeparator=System.lineSeparator();

		try(OutputStream stream=new FileOutputStream(file)) {
			try (OutputStreamWriter writer=new OutputStreamWriter(stream,StandardCharsets.UTF_8)) {
				try (BufferedWriter bufferedWriter=new BufferedWriter(writer)) {
					for (int i=0;i<randomNumberCount;i++) {
						bufferedWriter.write(NumberTools.formatNumberMax(DistributionRandomNumber.random(distribution)));
						bufferedWriter.write(lineSeparator);
					}
					return true;
				}
			}
		} catch (IOException e) {
			return false;
		}
	}
}

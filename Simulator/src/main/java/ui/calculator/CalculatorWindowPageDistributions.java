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
import javax.swing.Box;
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
import mathtools.TableChart;
import mathtools.distribution.swing.CommonVariables;
import mathtools.distribution.swing.JDistributionEditorPanel;
import mathtools.distribution.swing.JDistributionPanel;
import mathtools.distribution.tools.AbstractDistributionWrapper;
import mathtools.distribution.tools.DistributionRandomNumber;
import mathtools.distribution.tools.DistributionTools;
import mathtools.distribution.tools.DistributionWrapperInfo;
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
		add(distributionEditor=new JDistributionEditorPanel(distribution,200,e->updateDistribution(),true),BorderLayout.SOUTH);

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
		JPanel line;

		final JPanel editorPanel=new JPanel();
		editorPanel.setLayout(new BoxLayout(editorPanel,BoxLayout.PAGE_AXIS));
		editorPanel.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(Box.createHorizontalStrut(20));
		final JLabel label=new JLabel(Language.tr("CalculatorDialog.Tab.Distributions.GenerateRandomNumbers.Count")+":");
		line.add(label);
		editorPanel.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.setBorder(BorderFactory.createEmptyBorder(0,25,0,0));
		final JTextField editor=new JTextField(""+randomNumberCount,10);
		line.add(editor);
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

		popup.add(item=new JMenuItem(Language.tr("CalculatorDialog.Tab.Distributions.GenerateRandomNumbers.SaveExt"),Images.GENERAL_TABLE.getIcon()));
		item.addActionListener(e->randomNumbersSaveExt());

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

		final int distSize=1_000_000;
		final StatisticsDataPerformanceIndicatorWithNegativeValues indicator=new StatisticsDataPerformanceIndicatorWithNegativeValues(null,distSize,distSize);

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
		info.append(String.format(Language.tr("Distribution.Kurt")+": Kurt=%s",NumberTools.formatNumber(indicator.getKurt(),3))+"\n");
		info.append(String.format(Language.tr("Statistics.Minimum")+": Min=%s",NumberTools.formatNumber(indicator.getMin(),3))+"\n");
		info.append(String.format(Language.tr("Statistics.Maximum")+": Max=%s",NumberTools.formatNumber(indicator.getMax(),3))+"\n");
		info.append(String.format(Language.tr("Statistics.Median")+": %s",NumberTools.formatNumber(indicator.getMedian(),3))+"\n");
		final double mode[]=indicator.getDistribution().getMode();
		if (mode.length==1) {
			info.append(String.format(Language.tr("Statistics.Mode")+": %s",NumberTools.formatNumber(mode[0]))+"\n");
		} else {
			if (mode.length>0) {
				final StringBuilder modeBuilder=new StringBuilder();
				modeBuilder.append(NumberTools.formatNumber(mode[0]));
				for (int i=1;i<Math.min(5,mode.length);i++) {
					modeBuilder.append(";");
					modeBuilder.append(NumberTools.formatNumber(mode[i]));
				}
				if (mode.length>5) modeBuilder.append(";...");
				info.append(String.format(Language.tr("Statistics.Mode.Plural")+": %s",modeBuilder.toString())+"\n");
			}
		}

		MsgBox.info(this,Language.tr("CalculatorDialog.Tab.Distributions.GenerateRandomNumbers.Generate"),info.toString());
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
		fc.setDialogTitle(Language.tr("CalculatorDialog.Tab.Distributions.GenerateRandomNumbers.SaveTitle"));
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

	/**
	 * Anzahl der Schritte in der Häufigkeitsverteilung
	 * @see #buildTableChart()
	 */
	private static final int FREQUENCY_DISTRIBUTION_STEPS=50;

	/**
	 * Erstellt eine Zufallszahlen-Tabelle mit eingebettetem Diagramm
	 * @return	Zufallszahlen-Tabelle mit eingebettetem Diagramm oder <code>null</code>, wenn keine Tabelle erstellt werden konnte
	 */
	private TableChart buildTableChart() {
		if (randomNumberCount<16+FREQUENCY_DISTRIBUTION_STEPS) return null;

		/* Tabelle erstellen */

		final Table table=new Table();

		final AbstractRealDistribution distribution=distributionEditor.getDistribution();
		final int count=(int)Math.min(100_000,randomNumberCount);
		final double[] numbers=new double[count];
		double min=Double.MAX_VALUE;
		double max=-Double.MAX_VALUE;
		for (int i=0;i<count;i++) {
			final double value=DistributionRandomNumber.random(distribution);
			numbers[i]=value;
			if (value<min) min=value;
			if (value>max) max=value;
		}
		final DistributionWrapperInfo info=DistributionTools.getWrapper(distribution).getInfo(distribution);
		final String minString=(distribution.getSupportLowerBound()>-1_000_000_000)?NumberTools.formatNumberMax(distribution.getSupportLowerBound()):"";
		final String maxString=(distribution.getSupportUpperBound()<1_000_000_000)?NumberTools.formatNumberMax(distribution.getSupportUpperBound()):"";

		final String range="A2:A"+(count+1);

		int nr=0;
		table.addLine(new String[] {Language.tr("CalculatorDialog.Tab.Distributions.GenerateRandomNumbers.SaveExt.RandomNumbers"),Language.tr("CalculatorDialog.Tab.Distributions.GenerateRandomNumbers.SaveExt.DistributionName")});
		table.addLine(new String[] {NumberTools.formatNumberMax(numbers[nr++]),"",DistributionTools.getDistributionName(distribution)});
		table.addLine(new String[] {NumberTools.formatNumberMax(numbers[nr++])});
		table.addLine(new String[] {NumberTools.formatNumberMax(numbers[nr++]),"",Language.tr("CalculatorDialog.Tab.Distributions.GenerateRandomNumbers.SaveExt.Parameters")});
		table.addLine(new String[] {NumberTools.formatNumberMax(numbers[nr++]),"",info.getLongInfo()});
		table.addLine(new String[] {NumberTools.formatNumberMax(numbers[nr++])});
		table.addLine(new String[] {NumberTools.formatNumberMax(numbers[nr++]),"","",Language.tr("CalculatorDialog.Tab.Distributions.GenerateRandomNumbers.SaveExt.Parameters.Set"),Language.tr("CalculatorDialog.Tab.Distributions.GenerateRandomNumbers.SaveExt.Parameters.Actual")});
		table.addLine(new String[] {NumberTools.formatNumberMax(numbers[nr++]),"",Language.tr("Distribution.Mean"),NumberTools.formatNumberMax(info.E),"=AVERAGE("+range+")"});
		table.addLine(new String[] {NumberTools.formatNumberMax(numbers[nr++]),"",Language.tr("Statistics.Variance"),NumberTools.formatNumberMax(info.Std*info.Std),"=_xlfn.VAR.S("+range+")"});
		table.addLine(new String[] {NumberTools.formatNumberMax(numbers[nr++]),"",Language.tr("Distribution.StdDev"),NumberTools.formatNumberMax(info.Std),"=_xlfn.STDEV.S("+range+")"});
		table.addLine(new String[] {NumberTools.formatNumberMax(numbers[nr++]),"",Language.tr("Distribution.CV"),NumberTools.formatNumberMax(info.Std/info.E),"=_xlfn.STDEV.S("+range+")/AVERAGE("+range+")"});
		table.addLine(new String[] {NumberTools.formatNumberMax(numbers[nr++]),"",Language.tr("Distribution.Skewness"),NumberTools.formatNumberMax(info.Sk),"=SKEW("+range+")"});
		table.addLine(new String[] {NumberTools.formatNumberMax(numbers[nr++]),"",Language.tr("Distribution.Kurt"),"","=KURT("+range+")"});
		table.addLine(new String[] {NumberTools.formatNumberMax(numbers[nr++]),"",Language.tr("Statistics.Minimum"),minString,"=MIN("+range+")"});
		table.addLine(new String[] {NumberTools.formatNumberMax(numbers[nr++]),"",Language.tr("Statistics.Maximum"),maxString,"=MAX("+range+")"});
		table.addLine(new String[] {NumberTools.formatNumberMax(numbers[nr++]),"",Language.tr("Statistics.Median"),"","=MEDIAN("+range+")"});
		table.addLine(new String[] {NumberTools.formatNumberMax(numbers[nr++])});
		table.addLine(new String[] {NumberTools.formatNumberMax(numbers[nr++]),"",Language.tr("Distribution.Range")+" "+Language.tr("Distribution.Range.from"),Language.tr("Distribution.Range")+" "+Language.tr("Distribution.Range.to"),Language.tr("Distribution.Range")+" "+Language.tr("Distribution.Range.center"),Language.tr("Statistics.Frequency")});
		final int frequencyDistributionStartRow=nr+1;
		final double step=(max-min)/FREQUENCY_DISTRIBUTION_STEPS;
		for (int i=0;i<FREQUENCY_DISTRIBUTION_STEPS;i++) {
			final String cmd;
			if (i==0) {
				cmd="=COUNTIF("+range+",CONCATENATE(\"<=\",D"+(nr+2)+"))";
			} else {
				cmd="=COUNTIF("+range+",CONCATENATE(\"<=\",D"+(nr+2)+"))-COUNTIF("+range+",CONCATENATE(\"<=\",C"+(nr+2)+"))";
			}
			table.addLine(new String[] {NumberTools.formatNumberMax(numbers[nr++]),"",NumberTools.formatNumberMax(min+step*i),NumberTools.formatNumberMax(min+step*(i+1)),"=(C"+(nr+1)+"+D"+(nr+1)+")/2",cmd});
		}

		for (int i=nr;i<count;i++) table.addLine(new String[] {NumberTools.formatNumberMax(DistributionRandomNumber.random(distribution))});

		/* Diagramm aufbauen */

		final TableChart tableChart=new TableChart(table);

		tableChart.setupAxis(Language.tr("Statistics.Values"),Language.tr("Statistics.Frequency"));
		tableChart.setupChart(
				TableChart.ChartMode.LINE,
				new TableChart.Range[] {new TableChart.Range(5,frequencyDistributionStartRow,5,frequencyDistributionStartRow+FREQUENCY_DISTRIBUTION_STEPS-1)},
				new Object[]{Language.tr("Statistics.Frequency")},
				new TableChart.Range(4,frequencyDistributionStartRow,4,frequencyDistributionStartRow+FREQUENCY_DISTRIBUTION_STEPS-1)
				);

		return tableChart;
	}

	/**
	 * Erzeugt eine Reihe von Zufallszahlen und speichert diese inkl. weitere Angaben als Datei.
	 * @return	Liefert <code>true</code>, wenn die Zufallszahlen gespeichert werden konnten
	 */
	private boolean randomNumbersSaveExt() {
		final JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		fc.setDialogTitle(Language.tr("CalculatorDialog.Tab.Distributions.GenerateRandomNumbers.SaveTitle"));
		final FileFilter xlsx=new FileNameExtensionFilter(Table.FileTypeExcel+" (*.xlsx)","xlsx");
		fc.addChoosableFileFilter(xlsx);
		fc.setFileFilter(xlsx);
		fc.setAcceptAllFileFilterUsed(false);
		if (fc.showSaveDialog(window)!=JFileChooser.APPROVE_OPTION) return false;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();
		if (file.getName().indexOf('.')<0) {
			if (fc.getFileFilter()==xlsx) file=new File(file.getAbsoluteFile()+".xlsx");
		}

		final TableChart tableChart=buildTableChart();
		if (tableChart==null) return false;
		return tableChart.save(Language.tr("CalculatorDialog.Tab.Distributions.GenerateRandomNumbers.SaveExt.FrequencyDistribution"),file);
	}
}

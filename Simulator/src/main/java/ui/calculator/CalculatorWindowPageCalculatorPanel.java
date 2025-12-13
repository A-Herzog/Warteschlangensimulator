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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyAdapter;
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
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import mathtools.Table;
import mathtools.TableChart;
import mathtools.distribution.swing.PlugableFileChooser;
import mathtools.distribution.tools.DistributionRandomNumberThreadLocal;
import mathtools.distribution.tools.RandomGeneratorMode;
import parser.MathCalcError;
import simulator.simparser.ExpressionCalc;
import statistics.StatisticsDataPerformanceIndicatorWithNegativeValues;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.expressionbuilder.ExpressionBuilder;
import ui.expressionbuilder.ExpressionBuilderAutoComplete;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Einzelne Eingabe-Ausgabe-Zeilenkombination im Rechnerdialog.
 * @author Alexander Herzog
 * @see CalculatorWindowPageCalculator
 */
public class CalculatorWindowPageCalculatorPanel extends JPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-9092704475173212447L;

	/**
	 * Gesamtes Fenster
	 */
	private final Window window;

	/**
	 * Eingabezeile
	 */
	private final JTextField inputEdit;

	/**
	 * Ergebnisausgabezeile
	 */
	private final JTextField outputEdit;

	/**
	 * Anzahl an zu berechnenden Wiederholungen
	 * @see #showToolsPopup(Component)
	 * @see #randomNumbersIndicators(String)
	 * @see #randomNumbersCopy(String)
	 * @see #randomNumbersSave(String)
	 */
	private final CommonCalculationRepeatSetup commonCalculationRepeatSetup;

	/**
	 * Konstruktor der Klasse
	 * @param window	Gesamtes Fenster
	 * @param commonCalculationRepeatSetup Objekt zur Synchronisation der Anzahl an zu berechnenden Wiederholungen zwischen mehreren Panels
	 * @param initialExpression	Initial anzuzeigender Ausdruck (kann <code>null</code> sein)
	 */
	public CalculatorWindowPageCalculatorPanel(final Window window, final CommonCalculationRepeatSetup commonCalculationRepeatSetup, final String initialExpression) {
		this.window=window;
		this.commonCalculationRepeatSetup=commonCalculationRepeatSetup;

		Object[] data;
		JPanel line;

		setLayout(new BoxLayout(this,BoxLayout.PAGE_AXIS));

		/* Eingabezeile */
		data=CalculatorWindowPage.getInputPanel(Language.tr("CalculatorDialog.Expression")+":","",-1);
		add(line=(JPanel)data[0]);
		inputEdit=(JTextField)data[1];
		inputEdit.addKeyListener(new KeyAdapter() {
			@Override public void keyReleased(KeyEvent e) {outputEdit.setText(calc(inputEdit.getText()));}
		});

		/* Bereich rechts neben der Eingabezeile */
		final JPanel buttonsArea=new JPanel(new FlowLayout(FlowLayout.LEFT));
		line.add(buttonsArea,BorderLayout.EAST);

		/* ExpressionBuilder-Button */
		final JButton buildButton=new JButton();
		buttonsArea.add(buildButton);
		buildButton.setPreferredSize(new Dimension(26,26));
		buildButton.setIcon(Images.EXPRESSION_BUILDER.getIcon());
		buildButton.setToolTipText(Language.tr("CalculatorDialog.Expression.BuildExpression"));

		/* Tools-Button */
		final JButton toolsButton=new JButton();
		buttonsArea.add(toolsButton);
		toolsButton.setPreferredSize(new Dimension(26,26));
		toolsButton.setIcon(Images.GENERAL_TOOLS.getIcon());
		toolsButton.setToolTipText(Language.tr("CalculatorDialog.RepeatedProcessResults"));
		toolsButton.addActionListener(e->showToolsPopup(toolsButton));

		/* Ausgabezeile */
		data=CalculatorWindowPage.getInputPanel(Language.tr("CalculatorDialog.Result")+":","",-1);
		add(line=(JPanel)data[0]);
		outputEdit=(JTextField)data[1];
		outputEdit.setEditable(false);

		/* Expression-Builder konfigurieren */
		buildButton.addActionListener(e->{
			final ExpressionBuilder dialog=new ExpressionBuilder(this,inputEdit.getText(),false,new String[0],null,null,null,false,true,true);
			dialog.setVisible(true);
			if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
				inputEdit.setText(dialog.getExpression());
				outputEdit.setText(calc(inputEdit.getText()));
			}
		});
		ExpressionBuilderAutoComplete.process(new ExpressionBuilder(this,inputEdit.getText(),false,new String[0],null,null,null,false,true,true),inputEdit);

		/* Bereich rechts neben der Ausgabezeile */
		final JPanel buttonsAreaOutput=new JPanel(new FlowLayout(FlowLayout.LEFT));
		line.add(buttonsAreaOutput,BorderLayout.EAST);

		/* Kopieren-Button */
		final JButton button=new JButton("");
		button.setIcon(Images.EDIT_COPY.getIcon());
		button.setToolTipText(Language.tr("CalculatorDialog.Result.Copy"));
		final Dimension size=button.getPreferredSize();
		button.setPreferredSize(new Dimension(size.height,size.height));
		button.addActionListener(e->{
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(outputEdit.getText()),null);
		});
		buttonsAreaOutput.add(button,BorderLayout.EAST);

		/* Start */
		if (initialExpression!=null) setExpression(initialExpression);
	}

	/**
	 * Stellt einen anzuzeigenden Rechenausdruck ein.
	 * @param expression	Neuer Rechenausdruck
	 */
	public void setExpression(final String expression) {
		if (expression==null || expression.isBlank()) return;

		inputEdit.setText(expression);
		outputEdit.setText(calc(inputEdit.getText()));

		new Timer().schedule(new TimerTask() {
			@Override public void run() {inputEdit.requestFocus();}
		},100);
	}

	/**
	 * Liefert <code>true</code>, wenn der Rechenausdruck in dem Panel leer ist.
	 * @return	Ist der aktuelle Rechenausdruck leer?
	 */
	public boolean isEmpty() {
		return inputEdit.getText().isBlank();
	}

	/**
	 * Berechnet einen Ausdruck
	 * @param expression	Zu berechnender Ausdruck
	 * @return	Liefert das Ergebnis als Zeichenkette oder eine Fehlermeldung
	 */
	private String calc(final String expression) {
		if (expression==null || expression.isBlank()) return "";

		final ExpressionCalc calc=new ExpressionCalc(null,null);
		final var generator=new DistributionRandomNumberThreadLocal(commonCalculationRepeatSetup.randomMode);
		generator.init();
		calc.setRandomGenerator(generator);
		final int error=calc.parse(expression);
		if (error>=0) return String.format(Language.tr("CalculatorDialog.Expression.ParseError"),error+1);
		double d;
		try {
			d=calc.calc();
		} catch (MathCalcError e) {
			return Language.tr("CalculatorDialog.Expression.CalcError");
		}
		return NumberTools.formatNumberMax(d);
	}

	/**
	 * Verarbeitet die Eingaben in dem Eingabefeld zur Festlegung der Anzahl an zu berechnenden Wiederholungen
	 * @param input	Eingabefeld zur Festlegung der Anzahl an zu berechnenden Wiederholungen
	 * @see #showToolsPopup(Component)
	 */
	private void processRepeatCountInput(final JTextField input) {
		final Long L=NumberTools.getPositiveLong(input,true);
		if (L!=null) commonCalculationRepeatSetup.repeatCount=L.longValue();
	}

	/**
	 * Erzeugt ein Rechenobjekt auf Basis eines Ausdrucks.
	 * @param expression	Zu interpretierender Ausdruck
	 * @return	Liefert im Erfolgsfall ein Rechenobjekt, sonst <code>null</code>
	 */
	private ExpressionCalc buildExpressionCalc(final String expression) {
		final ExpressionCalc calc=new ExpressionCalc(null,null);
		final int error=calc.parse(expression);
		if (error>=0) {
			MsgBox.error(this,Language.tr("CalculatorDialog.Expression.CalcError.Info"),String.format(Language.tr("CalculatorDialog.Expression.ParseError"),error+1));
			return null;
		}
		return calc;
	}

	/**
	 * Einzelner Rechenthread zur Erzeugung von Zufallszahlen
	 * @see CalculatorWindowPageDistributions#randomNumbersIndicators()
	 */
	private static class RandomNumbersIndicatorsThread extends Thread {
		/** Ausdruck, der wiederholt ausgewertet werden soll */
		private final String expression;
		/** Anzahl an Zufallszahlen, die in diesem Thread erzeugt werden sollen */
		private final long count;
		/** Zu verwendender Pseudo-Zufalllszahlengenerator */
		private final RandomGeneratorMode randomMode;
		/** Statistikobjekt zur Erfassung der Daten / zur Ermittlung der Kenngrößen */
		private StatisticsDataPerformanceIndicatorWithNegativeValues indicator;

		/**
		 * Konstruktor
		 * @param expression	Ausdruck, der wiederholt ausgewertet werden soll
		 * @param count	Anzahl an Zufallszahlen, die in diesem Thread erzeugt werden sollen
		 * @param randomMode	Zu verwendender Pseudo-Zufalllszahlengenerator
		 * @param nr	1-basierte Nummer des Threads (zur Definition des Namens des Threads)
		 */
		public RandomNumbersIndicatorsThread(final String expression, final long count, final RandomGeneratorMode randomMode, final int nr) {
			super("Random number generator thread "+nr);
			this.expression=expression;
			this.count=count;
			this.randomMode=randomMode;
			this.indicator=null;
		}

		/**
		 * Grenze zur Aufzeichnung von Histogramm-Daten
		 */
		private static final int distSize=1_000_000;

		@Override
		public void run() {
			final ExpressionCalc calc=new ExpressionCalc(null,null);
			calc.parse(expression);

			final var generator=new DistributionRandomNumberThreadLocal(randomMode);
			generator.init();
			calc.setRandomGenerator(generator);

			indicator=new StatisticsDataPerformanceIndicatorWithNegativeValues(null,distSize,distSize);
			try {
				for (long i=0;i<count;i++) indicator.add(calc.calc());
			} catch (MathCalcError e) {
				indicator=null;
			}
		}

		/**
		 * Liefert das thread-lokale Statistikobjekt zur Erfassung der Daten / zur Ermittlung der Kenngrößen.
		 * @return	Statistikobjekt zur Erfassung der Daten / zur Ermittlung der Kenngrößen
		 */
		public StatisticsDataPerformanceIndicatorWithNegativeValues getIndicator() {
			return indicator;
		}
	}

	/**
	 * Erzeugt eine Reihe von Zufallszahlen bzw. wiederholten Auswertungen des Ausdrucks, ermittelt die Kenngrößen der Messreihe und zeigt diese an.
	 * @param expression	Zu berechnender Ausdruck
	 */
	private void randomNumbersIndicators(final String expression) {
		final ExpressionCalc calc=buildExpressionCalc(expression);
		if (calc==null) return;

		final long randomNumberCount=commonCalculationRepeatSetup.repeatCount;
		final int threadCount=Math.min(32,Runtime.getRuntime().availableProcessors());
		final RandomNumbersIndicatorsThread[] threads=new RandomNumbersIndicatorsThread[threadCount];
		for (int i=0;i<threads.length;i++) {
			final long count=randomNumberCount/threads.length;
			threads[i]=new RandomNumbersIndicatorsThread(expression,(i==threads.length-1)?(randomNumberCount-(threads.length-1)*count):count,commonCalculationRepeatSetup.randomMode,i+1);
		}
		for (RandomNumbersIndicatorsThread thread: threads) thread.start();
		for (RandomNumbersIndicatorsThread thread: threads) try {thread.join();} catch (InterruptedException e) {}
		for (int i=0;i<threads.length;i++) if (threads[i].indicator==null) {
			MsgBox.error(this,Language.tr("CalculatorDialog.Expression.CalcError.Info"),Language.tr("CalculatorDialog.Expression.CalcError"));
			return;
		}
		final StatisticsDataPerformanceIndicatorWithNegativeValues indicator=threads[0].indicator;
		for (int i=1;i<threads.length;i++) indicator.add(threads[i].getIndicator());

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
	 * @param expression	Zu berechnender Ausdruck
	 */
	private void randomNumbersCopy(final String expression) {
		final ExpressionCalc calc=buildExpressionCalc(expression);
		if (calc==null) return;

		final var generator=new DistributionRandomNumberThreadLocal(commonCalculationRepeatSetup.randomMode);
		generator.init();
		calc.setRandomGenerator(generator);

		final StringBuilder result=new StringBuilder();
		try {
			for (int i=0;i<commonCalculationRepeatSetup.repeatCount;i++) {
				result.append(NumberTools.formatNumberMax(calc.calc()));
				result.append("\n");
			}
		} catch (MathCalcError e) {
			MsgBox.error(this,Language.tr("CalculatorDialog.Expression.CalcError.Info"),Language.tr("CalculatorDialog.Expression.CalcError"));
			return;
		}

		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(result.toString()),null);
	}

	/**
	 * Erzeugt eine Reihe von Zufallszahlen und speichert die als Datei.
	 * @param expression	Zu berechnender Ausdruck
	 * @return	Liefert <code>true</code>, wenn die Zufallszahlen gespeichert werden konnten
	 */
	private boolean randomNumbersSave(final String expression) {
		final var fc=new PlugableFileChooser(true);
		fc.setDialogTitle(Language.tr("CalculatorDialog.Tab.Distributions.GenerateRandomNumbers.SaveTitle"));
		fc.addChoosableFileFilter(Table.FileTypeText+" (*.txt, *.tsv)","txt","tsv");
		fc.setFileFilter("txt");
		fc.setAcceptAllFileFilterUsed(false);
		final File file=fc.showSaveDialogFileWithExtension(window);
		if (file==null) return false;

		if (file.exists()) {
			if (!MsgBox.confirmOverwrite(this,file)) return false;
		}

		final ExpressionCalc calc=buildExpressionCalc(expression);
		if (calc==null) return false;

		final var generator=new DistributionRandomNumberThreadLocal(commonCalculationRepeatSetup.randomMode);
		generator.init();
		calc.setRandomGenerator(generator);

		final String lineSeparator=System.lineSeparator();

		try(OutputStream stream=new FileOutputStream(file)) {
			try (OutputStreamWriter writer=new OutputStreamWriter(stream,StandardCharsets.UTF_8)) {
				try (BufferedWriter bufferedWriter=new BufferedWriter(writer)) {
					try {
						for (int i=0;i<commonCalculationRepeatSetup.repeatCount;i++) {
							bufferedWriter.write(NumberTools.formatNumberMax(calc.calc()));
							bufferedWriter.write(lineSeparator);
						}
					} catch (MathCalcError e) {
						MsgBox.error(this,Language.tr("CalculatorDialog.Expression.CalcError.Info"),Language.tr("CalculatorDialog.Expression.CalcError"));
						return false;
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
	 * @see #buildTableChart(String)
	 */
	private static final int FREQUENCY_DISTRIBUTION_STEPS=50;

	/**
	 * Erstellt eine Zufallszahlen-Tabelle mit eingebettetem Diagramm
	 * @param expression	Zu berechnender Ausdruck
	 * @return	Zufallszahlen-Tabelle mit eingebettetem Diagramm oder <code>null</code>, wenn keine Tabelle erstellt werden konnte
	 */
	private TableChart buildTableChart(final String expression) {
		if (commonCalculationRepeatSetup.repeatCount<16+FREQUENCY_DISTRIBUTION_STEPS) return null;

		/* Tabelle erstellen */

		final Table table=new Table();

		final ExpressionCalc calc=buildExpressionCalc(expression);
		if (calc==null) return null;

		final int count=(int)Math.min(100_000,commonCalculationRepeatSetup.repeatCount);
		final var generator=new DistributionRandomNumberThreadLocal(commonCalculationRepeatSetup.randomMode);
		generator.init();
		calc.setRandomGenerator(generator);
		final double[] numbers=new double[count];
		double min=Double.MAX_VALUE;
		double max=-Double.MAX_VALUE;
		try {
			for (int i=0;i<count;i++) {
				final double value=calc.calc();
				numbers[i]=value;
				if (value<min) min=value;
				if (value>max) max=value;
			}
		} catch (MathCalcError e) {
			MsgBox.error(this,Language.tr("CalculatorDialog.Expression.CalcError.Info"),Language.tr("CalculatorDialog.Expression.CalcError"));
			return null;
		}

		final String range="A2:A"+(count+1);

		int nr=0;
		table.addLine(new String[] {Language.tr("CalculatorDialog.Tab.Distributions.GenerateRandomNumbers.SaveExt.RandomNumbers"),Language.tr("CalculatorDialog.Tab.Distributions.GenerateRandomNumbers.SaveExt.DistributionName")});
		table.addLine(new String[] {NumberTools.formatNumberMax(numbers[nr++])});
		table.addLine(new String[] {NumberTools.formatNumberMax(numbers[nr++]),"","",Language.tr("CalculatorDialog.Tab.Distributions.GenerateRandomNumbers.SaveExt.Parameters.Actual")});
		table.addLine(new String[] {NumberTools.formatNumberMax(numbers[nr++]),"",Language.tr("Distribution.Mean"),"=AVERAGE("+range+")"});
		table.addLine(new String[] {NumberTools.formatNumberMax(numbers[nr++]),"",Language.tr("Statistics.Variance"),"=_xlfn.VAR.S("+range+")"});
		table.addLine(new String[] {NumberTools.formatNumberMax(numbers[nr++]),"",Language.tr("Distribution.StdDev"),"=_xlfn.STDEV.S("+range+")"});
		table.addLine(new String[] {NumberTools.formatNumberMax(numbers[nr++]),"",Language.tr("Distribution.CV"),"=_xlfn.STDEV.S("+range+")/AVERAGE("+range+")"});
		table.addLine(new String[] {NumberTools.formatNumberMax(numbers[nr++]),"",Language.tr("Distribution.Skewness"),"=SKEW("+range+")"});
		table.addLine(new String[] {NumberTools.formatNumberMax(numbers[nr++]),"",Language.tr("Statistics.Minimum"),"=MIN("+range+")"});
		table.addLine(new String[] {NumberTools.formatNumberMax(numbers[nr++]),"",Language.tr("Statistics.Maximum"),"=MAX("+range+")"});
		table.addLine(new String[] {NumberTools.formatNumberMax(numbers[nr++]),"",Language.tr("Statistics.Median"),"=MEDIAN("+range+")"});
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

		try {
			for (int i=nr;i<count;i++) table.addLine(new String[] {NumberTools.formatNumberMax(calc.calc())});
		} catch (MathCalcError e) {
			MsgBox.error(this,Language.tr("CalculatorDialog.Expression.CalcError.Info"),Language.tr("CalculatorDialog.Expression.CalcError"));
			return null;
		}

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
	 * @param expression	Zu berechnender Ausdruck
	 * @return	Liefert <code>true</code>, wenn die Zufallszahlen gespeichert werden konnten
	 */
	private boolean randomNumbersSaveExt(final String expression) {
		final var fc=new PlugableFileChooser(true);
		fc.setDialogTitle(Language.tr("CalculatorDialog.Tab.Distributions.GenerateRandomNumbers.SaveTitle"));
		fc.addChoosableFileFilter(Table.FileTypeExcel+" (*.xlsx)","xlsx");
		fc.setFileFilter("xlsx");
		fc.setAcceptAllFileFilterUsed(false);
		final File file=fc.showSaveDialogFileWithExtension(window);
		if (file==null) return false;

		if (file.exists()) {
			if (!MsgBox.confirmOverwrite(this,file)) return false;
		}

		final TableChart tableChart=buildTableChart(expression);
		if (tableChart==null) return false;
		return tableChart.save(Language.tr("CalculatorDialog.Tab.Distributions.GenerateRandomNumbers.SaveExt.FrequencyDistribution"),file);
	}

	/**
	 * Zeigt ein Popupmenü mit Befehlen zur Erzeugung von Messpunkten gemäß dem eingegebenen Ausdruck an.
	 * @param invoker	Aufrufer (zur Ausrichtung des Menüs)
	 */
	private void showToolsPopup(final Component invoker) {
		final JPopupMenu popup=new JPopupMenu();

		JMenuItem item;
		JPanel line;

		popup.add(item=new JMenuItem("<html><body>\n"+Language.tr("CalculatorDialog.RepeatedProcessResults.InfoPopup").replace("\n","<br>\n")+"\n</body></html>"));
		item.setEnabled(false);

		popup.addSeparator();

		final JPanel editorPanel=new JPanel();
		editorPanel.setOpaque(false);
		editorPanel.setLayout(new BoxLayout(editorPanel,BoxLayout.PAGE_AXIS));
		editorPanel.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.setOpaque(false);
		line.add(Box.createHorizontalStrut(20));
		final JLabel label=new JLabel(Language.tr("CalculatorDialog.RepeatedProcessResults.Info")+":");
		line.add(label);
		editorPanel.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.setOpaque(false);
		line.setBorder(BorderFactory.createEmptyBorder(0,25,0,0));
		final JTextField editor=new JTextField(""+commonCalculationRepeatSetup.repeatCount,10);
		ModelElementBaseDialog.addUndoFeature(editor);
		line.add(editor);
		label.setLabelFor(editor);
		editor.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {processRepeatCountInput(editor);}
			@Override public void keyReleased(KeyEvent e) {processRepeatCountInput(editor);}
			@Override public void keyPressed(KeyEvent e) {processRepeatCountInput(editor);}
		});
		popup.add(editorPanel);

		final JMenu generatorSubMenu=new JMenu(Language.tr("CalculatorDialog.Tab.Distributions.GenerateRandomNumbers.Generator"));
		popup.add(generatorSubMenu);
		for (var mode: RandomGeneratorMode.values()) {
			final JRadioButtonMenuItem generatorItem=new JRadioButtonMenuItem(mode.name,mode==commonCalculationRepeatSetup.randomMode);
			generatorSubMenu.add(generatorItem);
			generatorItem.addActionListener(e->commonCalculationRepeatSetup.randomMode=mode);
		}

		popup.addSeparator();

		popup.add(item=new JMenuItem(Language.tr("CalculatorDialog.Tab.Distributions.GenerateRandomNumbers.DetermineCharacteristics"),Images.EXTRAS_CALCULATOR.getIcon()));
		item.addActionListener(e->randomNumbersIndicators(inputEdit.getText()));

		popup.add(item=new JMenuItem(Language.tr("CalculatorDialog.Tab.Distributions.GenerateRandomNumbers.Copy"),Images.EDIT_COPY.getIcon()));
		item.addActionListener(e->randomNumbersCopy(inputEdit.getText()));

		popup.add(item=new JMenuItem(Language.tr("CalculatorDialog.Tab.Distributions.GenerateRandomNumbers.Save"),Images.GENERAL_SAVE.getIcon()));
		item.addActionListener(e->randomNumbersSave(inputEdit.getText()));

		popup.add(item=new JMenuItem(Language.tr("CalculatorDialog.Tab.Distributions.GenerateRandomNumbers.SaveExt"),Images.GENERAL_TABLE.getIcon()));
		item.addActionListener(e->randomNumbersSaveExt(inputEdit.getText()));

		popup.show(invoker,0,invoker.getHeight());
	}

	/**
	 * Klasse zur Synchronisation der Anzahl an zu berechnenden Wiederholungen zwischen mehreren Panels
	 */
	public static class CommonCalculationRepeatSetup {
		/**
		 * Anzahl an zu berechnenden Wiederholungen
		 */
		public long repeatCount;

		/**
		 * Zu verwendender Pseudo-Zufalllszahlengenerator
		 */
		public RandomGeneratorMode randomMode;

		/**
		 * Konstruktor der Klasse
		 */
		public CommonCalculationRepeatSetup() {
			repeatCount=1_000_000;
			randomMode=RandomGeneratorMode.defaultRandomGeneratorMode;
		}
	}
}
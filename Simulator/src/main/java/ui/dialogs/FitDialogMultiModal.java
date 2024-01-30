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
import java.awt.Toolkit;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.Serializable;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import language.Language;
import mathtools.distribution.DataDistributionImpl;
import mathtools.distribution.tools.DistributionFitterMultiModal;
import systemtools.BaseDialog;
import systemtools.ImageTools;
import systemtools.images.SimToolsImages;
import systemtools.statistics.ChartSetup;
import systemtools.statistics.StatisticsBasePanel;
import tools.SetupData;
import ui.images.Images;
import ui.infopanel.InfoPanel;

/**
 * Diese Klasse stellt einen Dialog zur multimodalen Anpassung einer Verteilung
 * an aufgezeichnete Messwerte zur Verfügung.
 * @author Alexander Herzog
 * @see DistributionFitterMultiModal
 */
public class FitDialogMultiModal extends FitDialogBase {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-789754050895837673L;

	/**
	 * Welche Verteilung soll für die Teildichten verwendet werden?
	 */
	private final DistributionFitterMultiModal.FitDistribution fitDistribution;

	/**
	 * Chart-Rahmenobjekt
	 * @see #outputDistributionPanel()
	 * @see #copyPlot()
	 */
	private JFreeChart chart;

	/**
	 * Plot-Objekt zur Ausgabe der Fit-Ergebnisse
	 * @see #outputDistributionPanel()
	 * @see #calcFit()
	 */
	private XYPlot plot;

	/**
	 * Datenreihen innerhalb von {@link #plot}
	 * @see #outputDistributionPanel()
	 * @see #calcFit()
	 */
	private XYSeriesCollection data;

	/**
	 * Einstellungen für den Dialog zum Generieren von Testmesswerten
	 */
	private FitDialogMultiModalGenerate.GenerateSetup generateSetup;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param fitDistribution	Welche Verteilung soll für die Teildichten verwendet werden?
	 */
	public FitDialogMultiModal(final Component owner, final DistributionFitterMultiModal.FitDistribution fitDistribution) {
		super(owner,Language.tr("FitDialogMultiModal.Title"),"Fit",InfoPanel.globalFitMultiModal,true,false);
		this.fitDistribution=fitDistribution;
		generateSetup=new FitDialogMultiModalGenerate.GenerateSetup();
	}

	@Override
	protected JPanel outputDistributionPanel() {
		/* Chart */
		data=new XYSeriesCollection();
		chart=ChartFactory.createXYLineChart(null,null,null,data,PlotOrientation.VERTICAL,true,true,false);
		chart.setBackgroundPaint(null);

		/* Plot im Chart */
		plot=chart.getXYPlot();
		plot.setDomainGridlinesVisible(true);
		plot.setRangeGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.black);
		plot.setDomainGridlinePaint(Color.black);
		plot.setDomainPannable(true);
		plot.setRangePannable(true);

		/* Chart-Panel */
		final ChartPanel chartPanel=new ChartPanel(
				chart,
				ChartPanel.DEFAULT_WIDTH,
				ChartPanel.DEFAULT_HEIGHT,
				ChartPanel.DEFAULT_MINIMUM_DRAW_WIDTH,
				ChartPanel.DEFAULT_MINIMUM_DRAW_HEIGHT,
				ChartPanel.DEFAULT_MAXIMUM_DRAW_WIDTH,
				ChartPanel.DEFAULT_MAXIMUM_DRAW_HEIGHT,
				ChartPanel.DEFAULT_BUFFER_USED,
				true, /* properties */
				false, /* save */
				true, /* print */
				true, /* zoom */
				true  /* tooltips */
				);
		chartPanel.setPopupMenu(null);
		chartPanel.setMouseWheelEnabled(true);
		chartPanel.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));

		/* Einstellungen */
		final ChartSetup setup=new ChartSetup();
		setup.setupAll(chart);

		/* Toolbar */
		JButton button;
		final JToolBar toolbar=new JToolBar(SwingConstants.HORIZONTAL);
		toolbar.setFloatable(false);
		toolbar.setBorder(BorderFactory.createEmptyBorder(5,5,0,5));
		toolbar.add(button=new JButton(StatisticsBasePanel.viewersToolbarZoom));
		button.setToolTipText(StatisticsBasePanel.viewersToolbarZoomHint);
		button.addActionListener(e->unzoomPlot());
		button.setIcon(SimToolsImages.ZOOM.getIcon());
		toolbar.add(button=new JButton(Language.tr("FitDialog.CopyResults")));
		button.addActionListener(e->copyPlot());
		button.setToolTipText(Language.tr("FitDialog.CopyResults.Tooltip"));
		button.setIcon(Images.EDIT_COPY.getIcon());
		toolbar.add(button=new JButton(Language.tr("FitDialog.SaveResults")));
		button.setToolTipText(Language.tr("FitDialog.SaveResults.Tooltip"));
		button.addActionListener(e->savePlot());
		button.setIcon(SimToolsImages.SAVE.getIcon());

		/* Info-Fußzeile */
		final JPanel info=new JPanel(new BorderLayout());
		final JLabel label=new JLabel("<html><body>"+StatisticsBasePanel.viewersToolbarZoomHintPanel+"</body></html>");
		label.setBorder(BorderFactory.createEmptyBorder(10,10,5,10));
		info.add(label);

		/* Äußeres Panel */
		final JPanel panel=new JPanel(new BorderLayout());
		panel.add(toolbar,BorderLayout.NORTH);
		panel.add(chartPanel,BorderLayout.CENTER);
		panel.add(info,BorderLayout.SOUTH);
		return panel;
	}

	/**
	 * Farben für die Teildichten
	 * @see #calcFit()
	 */
	private static final Color[] densityColors=new Color[] {Color.CYAN, Color.PINK, Color.MAGENTA, Color.ORANGE, Color.GREEN};

	/**
	 * Führt die Verteilungsanpassung durch.
	 */
	@Override
	protected void calcFit() {
		final DistributionFitterMultiModal fitter=new DistributionFitterMultiModal(fitDistribution);
		fitter.setShowCalculationCommand(true);
		calcFitIntern(fitter);

		data.removeAllSeries();

		XYSeries series;

		/* Ausgangsdichte */
		series=new XYSeries(Language.tr("FitDialogMultiModal.InitialDensity"));
		final DataDistributionImpl distribution=inputDistribution.getDistribution();
		distribution.normalizeDensity();
		final double[] density=distribution.densityData;
		final double[] residuum=Arrays.copyOf(density,density.length);
		final double[] sum=new double[density.length];

		for (int i=0;i<density.length;i++) series.add(i,density[i]);
		data.addSeries(series);
		plot.getRenderer().setSeriesPaint(data.getSeriesCount()-1,Color.RED);

		/* Teildichten */
		final double[] fractions=fitter.getFitFraction().stream().mapToDouble(Double::doubleValue).toArray();
		final AbstractRealDistribution[] dists=fitter.getDistributions();
		for (int i=0;i<dists.length;i++) {
			final double fraction=fractions[i];
			series=new XYSeries(Language.tr("FitDialogMultiModal.PartialDensity")+" "+(i+1));
			for (int j=0;j<density.length;j++) {
				final double value=dists[i].density(j)*fraction;
				series.add(j,value);
				residuum[j]-=value;
				sum[j]+=value;
			}
			data.addSeries(series);
			plot.getRenderer().setSeriesPaint(data.getSeriesCount()-1,densityColors[i%densityColors.length]);
		}

		/* Gesamte Näherung */
		series=new XYSeries(Language.tr("FitDialogMultiModal.CompleteApproximation"));
		for (int i=0;i<sum.length;i++) series.add(i,Math.abs(sum[i]));
		data.addSeries(series);
		plot.getRenderer().setSeriesPaint(data.getSeriesCount()-1,Color.BLUE);

		/* Residuum */
		series=new XYSeries(Language.tr("FitDialogMultiModal.Residuum"));
		for (int i=0;i<residuum.length;i++) series.add(i,Math.abs(residuum[i]));
		data.addSeries(series);
		plot.getRenderer().setSeriesPaint(data.getSeriesCount()-1,Color.GRAY);
	}

	/**
	 * Stellt den Ausgangszoomfaktor wieder her.
	 */
	private void unzoomPlot() {
		plot.getDomainAxis().setAutoRange(true);
		plot.getRangeAxis().setAutoRange(true);
	}

	/**
	 * Kopiert das Ergebnisdiagramm in die Zwischenablage.
	 */
	private void copyPlot() {
		final int imageSize=SetupData.getSetup().imageSize;

		final ChartSetup chartSetup=new ChartSetup();
		chartSetup.setUserScale(Math.max(1,Math.min(5,imageSize/750)));
		chartSetup.setupAll(chart);
		try {
			final Transferable transferable=ImageTools.getTransferable(ImageTools.drawToImage(chart,imageSize,imageSize));
			if (transferable!=null) Toolkit.getDefaultToolkit().getSystemClipboard().setContents(transferable,null);
		} finally {
			chartSetup.setUserScale(1);
			chartSetup.setupAll(chart);
		}
	}

	/**
	 * Speichert das Ergebnisdiagramm als Bild.
	 */
	private void savePlot() {
		final File file=ImageTools.showLoadDialog(this);
		if (file==null) return;

		final int imageSize=SetupData.getSetup().imageSize;
		ImageTools.saveChart(this,chart,file,imageSize,null);
	}

	@Override
	protected double[] generateSampleValues() {
		final FitDialogMultiModalGenerate dialog=new FitDialogMultiModalGenerate(this,fitDistribution,generateSetup);
		if (dialog.getClosedBy()!=BaseDialog.CLOSED_BY_OK) return null;
		generateSetup=dialog.getNewGenerateSetup();
		return generateSetup.generateValues(fitDistribution);
	}
}

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
package systemtools.statistics;

import java.awt.Paint;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;

import mathtools.NumberTools;
import mathtools.Table;
import mathtools.TableChart;
import mathtools.distribution.DataDistributionImpl;

/**
 * Basisklasse zur Anzeige von {@link JFreeChart}-basierenden Balkendiagrammen
 * @see StatisticViewerJFreeChart
 * @author Alexander Herzog
 * @version 1.1
 */
public class StatisticViewerBarChart extends StatisticViewerJFreeChart {
	/**
	 * Ermöglicht für abgeleitete Klassen einen Zugriff auf das {@link CategoryPlot}-Element
	 */
	protected CategoryPlot plot;

	/**
	 * Zugriff auf die Daten
	 * @see #initBarChart(String)
	 */
	protected DefaultCategoryDataset data;

	/**
	 * Konstruktor der Klasse
	 */
	public StatisticViewerBarChart() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	@Override
	public ViewerImageType getImageType() {
		return ViewerImageType.IMAGE_TYPE_BAR;
	}

	/**
	 * Initialisiert das Diagramm
	 * @param title	Diagrammtitel
	 */
	protected void initBarChart(String title) {
		data=new DefaultCategoryDataset();
		initChart(ChartFactory.createBarChart(title,"x","y",data,PlotOrientation.VERTICAL,true,true,false));
		plot=chart.getCategoryPlot();

		plot.setRenderer(new StackedBarRenderer());
		((BarRenderer)(plot.getRenderer())).setDrawBarOutline(true);
		((BarRenderer)(plot.getRenderer())).setBarPainter(new StandardBarPainter());
	}

	/**
	 * Richtet die Tooltips für die einzelnen Balken ein.<br>
	 * Muss nach dem Einrichten der Datenserien aufgerufen werden.
	 */
	protected void initTooltips() {
		final int count=data.getRowCount();
		final BarRenderer renderer=(BarRenderer)plot.getRenderer();
		for (int i=0;i<count;i++) {
			renderer.setSeriesToolTipGenerator(i,new StandardCategoryToolTipGenerator(StandardCategoryToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT_STRING,NumberFormat.getInstance(NumberTools.getLocale())));
		}
	}

	/**
	 * Konfiguriert den Diagrammrahmen.
	 * @param title	Titel des Diagramms
	 * @param xLabel	Beschriftung der x-Achse
	 * @param yLabel	Beschriftung der y-Achse
	 * @param yPercent	Soll die y-Achse als Prozentwert formatiert werden?
	 */
	protected void setupBarChart(final String title, final String xLabel, final String yLabel, final boolean yPercent) {
		chart.setTitle(title);
		plot.getDomainAxis().setLabel(xLabel);
		plot.getDomainAxis().setMaximumCategoryLabelLines(3);

		if (yPercent) {
			NumberAxis axis=new NumberAxis();
			NumberFormat formater=NumberFormat.getPercentInstance();
			formater.setMinimumFractionDigits(1);
			formater.setMaximumFractionDigits(1);
			axis.setNumberFormatOverride(formater);
			axis.setUpperBound(1);
			axis.setLabel(yLabel);
			plot.setRangeAxis(axis);
		} else {
			plot.getRangeAxis().setLabel(yLabel);
		}

		chartSetup.setupAxis(plot.getDomainAxis());
		chartSetup.setupAxis(plot.getRangeAxis());
	}

	/**
	 * Summiert einen Teil der Datenreihe auf
	 * @param dist	Datenreihe, die aufsummiert werden soll
	 * @param firstValue	Erster zu betrachtender Eintrag
	 * @param lastValue	Letzter zu betrachtender Eintrag
	 * @return	Summe über die gewählten Werte in der Datenreihe
	 */
	protected double sumRange(final DataDistributionImpl dist, final int firstValue, final int lastValue) {
		double sum=0;
		for (int i=Math.max(0,firstValue);i<=Math.min(lastValue,dist.densityData.length-1);i++) sum+=dist.densityData[i];
		return sum;
	}

	/**
	 * Stellt die Umrissfarbe ein
	 * @param paint	Umrissfarbe
	 */
	protected void setOutlineColor(final Paint paint) {
		final CategoryItemRenderer renderer=plot.getRendererForDataset(data);
		for (int i=0;i<data.getRowCount();i++) renderer.setSeriesOutlinePaint(i,paint);
	}

	@Override
	public void unZoom() {
		plot.getRangeAxis().setAutoRange(true);
	}

	@Override
	protected boolean canStoreExcelFile() {
		return true;
	}

	/**
	 * Bereitet die Diagrammdaten als Tabelle auf
	 * @return	Tabelle, die die Diagrammdaten enthält
	 */
	public Table getTableFromChart() {
		final Table table=new Table();

		if (data!=null) {
			List<String> line;

			line=new ArrayList<>(1+data.getColumnCount());
			line.add("");
			for (int j=0;j<data.getColumnCount();j++) line.add(data.getColumnKey(j).toString());
			table.addLine(line);

			for (int i=0;i<data.getRowCount();i++) {
				line=new ArrayList<>(1+data.getColumnCount());
				final Comparable<?> rowKey=data.getRowKey(i);
				line.add(rowKey.toString());
				for (int j=0;j<data.getColumnCount();j++) {
					final Number num=data.getValue(rowKey,data.getColumnKey(j));
					final double d=(num==null)?0.0:num.doubleValue();
					line.add(NumberTools.formatNumberMax(d));
				}
				table.addLine(line);
			}
		}

		return table.transpose(true);
	}

	@Override
	public TableChart getTableChartFromChart() {
		final TableChart tableChart=new TableChart(getTableFromChart());

		tableChart.setupAxis(plot.getDomainAxis().getLabel(),plot.getRangeAxis().getLabel());
		tableChart.setupChart(TableChart.ChartMode.BAR);

		return tableChart;
	}
}

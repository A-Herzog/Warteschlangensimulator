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

import java.awt.Color;
import java.awt.Component;
import java.awt.Paint;
import java.io.File;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import mathtools.NumberTools;
import mathtools.Table;
import mathtools.TableChart;
import mathtools.distribution.DataDistributionImpl;
import mathtools.distribution.swing.CommonVariables;
import systemtools.MsgBox;

/**
 * Basisklasse zur Anzeige von {@link JFreeChart}-basierenden Liniendiagrammen
 * @see StatisticViewerJFreeChart
 * @author Alexander Herzog
 * @version 1.7
 */
public class StatisticViewerLineChart extends StatisticViewerJFreeChart {
	private boolean domainIsDay=false;
	private int smartZoomValue=-1;
	private double scaleFactor=1;

	/**
	 * Ermöglicht für abgeleitete Klassen einen Zugriff auf das {@link XYPlot}-Element
	 */
	protected XYPlot plot;

	/**
	 * Zugriff auf die Datenpunkte
	 * @see #initLineChart(String)
	 * @see #initLineChart(String, String, DataDistributionImpl, boolean)
	 * @see #initLineChart(String, String, String, DataDistributionImpl)
	 * @see #initLineChart(String, String, String, DataDistributionImpl, boolean)
	 */
	protected XYSeriesCollection data;

	/**
	 * Zugriff auf die Datenpunkte des optionalen zweiten Sets (=Daten auf der zweiten y-Achse erfasst)
	 * @see #addSeriesToSecondSet(String, Paint, DataDistributionImpl)
	 */
	protected XYSeriesCollection data2=null;

	@Override
	public ViewerImageType getImageType() {
		return ViewerImageType.IMAGE_TYPE_LINE;
	}

	/**
	 * Default-Konstruktor der Klasse <code>StatisticViewerLineChart</code>
	 */
	public StatisticViewerLineChart() {}

	/**
	 * Konstruktor zur direkten Erzeugung eines Diagramms mit einem Diagramm
	 * (Verzögerte Erzeugung wird nicht verwendet)
	 * @param title	Titel des Diagramms
	 * @param seriesTitle	Name der Datenreihe
	 * @param dist	Darzustellende Verteilung
	 * @param yPercent	Darstellung der y-Achse als Prozentwerte
	 */
	public StatisticViewerLineChart(final String title, final String seriesTitle, final DataDistributionImpl dist, final boolean yPercent) {
		this(title,seriesTitle,yPercent?StatisticsBasePanel.viewersChartPart:StatisticsBasePanel.viewersChartNumber,dist,yPercent);
	}

	/**
	 * Konstruktor zur direkten Erzeugung eines Diagramms mit einem Diagramm
	 * (Verzögerte Erzeugung wird nicht verwendet)
	 * @param title	Titel des Diagramms
	 * @param seriesTitle	Name der Datenreihe
	 * @param yLabel	Bezeichner an der y-Achse
	 * @param dist	Darzustellende Verteilung
	 * @param yPercent	Darstellung der y-Achse als Prozentwerte
	 */
	public StatisticViewerLineChart(final String title, final String seriesTitle, final String yLabel, final DataDistributionImpl dist, final boolean yPercent) {
		initLineChart(title);
		if (yPercent) setupChartDayPercent(title,yLabel); else setupChartDayValue(title,yLabel);
		addSeries(seriesTitle,Color.RED,dist);
		addFillColor(0);
	}

	/**
	 * Initialisiert das Diagramm
	 * @param title	Diagrammtitel
	 */
	protected void initLineChart(final String title) {
		data=new XYSeriesCollection();
		initChart(ChartFactory.createXYLineChart(title,"x","y",data,PlotOrientation.VERTICAL,true,true,false));
		plot=chart.getXYPlot();

		plot.setDomainGridlinesVisible(true);
		plot.setRangeGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.black);
		plot.setDomainGridlinePaint(Color.black);
	}

	/**
	 * Initialisiert das Diagramm
	 * (Als x-Achsen-Titel wird "Uhrzeit" verwendet; insgesamt wird das Diagramm für die Darstellung von Verteilungen über den Tag eingerichtet.)
	 * @param title	Titel des Diagramms
	 * @param seriesTitle	Titel der Datenreihe
	 * @param yLabel	Beschriftung der y-Achse
	 * @param dist	Darzustellende Verteilung
	 */
	protected void initLineChart(final String title, final String seriesTitle, final String yLabel, final DataDistributionImpl dist) {
		initLineChart(title);
		setupChartDayValue(title,yLabel);
		addSeries(seriesTitle,Color.RED,dist);
		addFillColor(0);
	}

	/**
	 * Initialisiert das Diagramm
	 * (Als x-Achsen-Titel wird "Uhrzeit" verwendet; insgesamt wird das Diagramm für die Darstellung von Verteilungen über den Tag eingerichtet.)
	 * @param title	Titel des Diagramms
	 * @param seriesTitle	Titel der Datenreihe
	 * @param dist	Darzustellende Verteilung
	 * @param yPercent	Darstellung der y-Achse als Prozentwerte
	 */
	protected void initLineChart(final String title, final String seriesTitle, final DataDistributionImpl dist, final boolean yPercent) {
		initLineChart(title);
		setupChartDayValue(title,yPercent?StatisticsBasePanel.viewersChartPart:StatisticsBasePanel.viewersChartNumber);
		addSeries(seriesTitle,Color.RED,dist);
		addFillColor(0);
	}

	/**
	 * Initialisiert das Diagramm
	 * (Als x-Achsen-Titel wird "Uhrzeit" verwendet; insgesamt wird das Diagramm für die Darstellung von Verteilungen über den Tag eingerichtet.)
	 * @param title	Titel des Diagramms
	 * @param seriesTitle	Titel der Datenreihe
	 * @param yLabel	Beschriftung der y-Achse
	 * @param dist	Darzustellende Verteilung
	 * @param yPercent	Darstellung der y-Achse als Prozentwerte
	 */
	protected void initLineChart(final String title, final String seriesTitle, final String yLabel, final DataDistributionImpl dist, final boolean yPercent) {
		initLineChart(title);
		if (yPercent) setupChartDayPercent(title,yLabel); else setupChartDayValue(title,yLabel);
		addSeries(seriesTitle,Color.RED,dist);
		addFillColor(0);
	}

	private enum ToolTipFormat {DAY_VALUE, DAY_PERCENT, DEFAULT}

	private ToolTipFormat toolTipFormat=ToolTipFormat.DEFAULT;

	/**
	 * Stellt den Diagrammrahmen für die Darstellung von Verteilungen über den Tag ein.
	 * @param title	Titel des Diagramms
	 * @param yLabel	Beschriftung der y-Achse
	 */
	protected void setupChartDayValue(final String title, final String yLabel) {
		chart.setTitle(title);
		domainIsDay=true;

		final DateAxis axis=new DateAxis();
		final DateFormat formater=new SimpleDateFormat("HH:mm");
		formater.setTimeZone(TimeZone.getTimeZone("GMT"));
		axis.setDateFormatOverride(formater);
		axis.setMinimumDate(new Date(0));
		axis.setMaximumDate(new Date(1000*86400));
		axis.setLabel(StatisticsBasePanel.viewersChartTime);
		plot.setDomainAxis(axis);

		plot.getRangeAxis().setLabel(yLabel);

		toolTipFormat=ToolTipFormat.DAY_VALUE;
	}

	/**
	 * Stellt den Diagrammrahmen für die Darstellung von Verteilungen über den Tag ein.
	 * Außerdem wird die y-Achse in Prozentwerten dargestellt
	 * @param title	Titel des Diagramms
	 * @param yLabel	Beschriftung der y-Achse
	 */
	protected void setupChartDayPercent(final String title, final String yLabel) {
		setupChartDayValue(title,yLabel);

		final NumberAxis axis=new NumberAxis();
		NumberFormat formater=NumberFormat.getPercentInstance();
		formater.setMinimumFractionDigits(1);
		formater.setMaximumFractionDigits(1);
		axis.setNumberFormatOverride(formater);
		axis.setLabel(yLabel);
		plot.setRangeAxis(axis);

		toolTipFormat=ToolTipFormat.DAY_PERCENT;
	}

	/**
	 * Stellt den Diagrammrahmen für die Darstellung von Verteilungen über ein bestimmtes Zeitintervall (z.B. eine Stunde) ein
	 * @param title	Titel des Diagramms
	 * @param xLabel	Beschriftung der x-Achse
	 * @param yLabel	Beschriftung der y-Achse
	 */
	protected void setupChartTimeValue(final String title, final String xLabel, final String yLabel) {
		chart.setTitle(title);

		plot.getDomainAxis().setLabel(xLabel+" ("+StatisticsBasePanel.viewersChartInSeconds+")");
		plot.getRangeAxis().setLabel(yLabel);
	}

	/**
	 * Stellt den Diagrammrahmen für die Darstellung von Verteilungen über ein bestimmtes Zeitintervall (z.B. eine Stunde) ein
	 * @param title	Titel des Diagramms
	 * @param xLabel	Beschriftung der x-Achse
	 * @param yLabel	Beschriftung der y-Achse
	 */
	protected void setupChartTimePercent(final String title, final String xLabel, final String yLabel) {
		chart.setTitle(title);

		final NumberAxis axis=new NumberAxis();
		final NumberFormat formater=NumberFormat.getPercentInstance();
		formater.setMinimumFractionDigits(1);
		formater.setMaximumFractionDigits(1);
		axis.setNumberFormatOverride(formater);
		axis.setLabel(yLabel);
		plot.setRangeAxis(axis);

		plot.getDomainAxis().setLabel(xLabel+" ("+StatisticsBasePanel.viewersChartInSeconds+")");
		plot.getRangeAxis().setLabel(yLabel);
	}

	/**
	 * Stellt den Diagrammrahmen für die Darstellung von Verteilungen über ein bestimmtes Zeitintervall (z.B. eine Stunde) ein
	 * @param title	Titel des Diagramms
	 * @param xLabel	Beschriftung der x-Achse
	 * @param yLabel	Beschriftung der y-Achse
	 */
	protected void setupChartTimePercentInHours(final String title, final String xLabel, final String yLabel) {
		chart.setTitle(title);

		final NumberAxis axis=new NumberAxis();
		final NumberFormat formater=NumberFormat.getPercentInstance();
		formater.setMinimumFractionDigits(1);
		formater.setMaximumFractionDigits(1);
		axis.setNumberFormatOverride(formater);
		axis.setLabel(yLabel);
		plot.setRangeAxis(axis);

		plot.getDomainAxis().setLabel(xLabel+" ("+StatisticsBasePanel.viewersChartInHours+")");
		plot.getRangeAxis().setLabel(yLabel);

		scaleFactor=3600;
	}

	/**
	 * Stellt den Diagrammrahmen für die Darstellung von Verteilungen über ein bestimmtes Zeitintervall (z.B. eine Minute) ein
	 * @param title	Titel des Diagramms
	 * @param xLabel	Beschriftung der x-Achse
	 * @param yLabel	Beschriftung der y-Achse
	 */
	protected void setupChartTimePercentInMinutes(final String title, final String xLabel, final String yLabel) {
		chart.setTitle(title);

		final NumberAxis axis=new NumberAxis();
		final NumberFormat formater=NumberFormat.getPercentInstance();
		formater.setMinimumFractionDigits(1);
		formater.setMaximumFractionDigits(1);
		axis.setNumberFormatOverride(formater);
		axis.setLabel(yLabel);
		plot.setRangeAxis(axis);

		plot.getDomainAxis().setLabel(xLabel+" ("+StatisticsBasePanel.viewersChartInMinutes+")");
		plot.getRangeAxis().setLabel(yLabel);

		scaleFactor=60;
	}

	/**
	 * Stellt den Diagrammrahmen für die Darstellung von Verteilungen über ein bestimmtes Zeitintervall (z.B. eine Sekunde) ein
	 * @param title	Titel des Diagramms
	 * @param xLabel	Beschriftung der x-Achse
	 * @param yLabel	Beschriftung der y-Achse
	 */
	protected void setupChartTimePercentInSeconds(final String title, final String xLabel, final String yLabel) {
		chart.setTitle(title);

		final NumberAxis axis=new NumberAxis();
		final NumberFormat formater=NumberFormat.getPercentInstance();
		formater.setMinimumFractionDigits(1);
		formater.setMaximumFractionDigits(1);
		axis.setNumberFormatOverride(formater);
		axis.setLabel(yLabel);
		plot.setRangeAxis(axis);

		plot.getDomainAxis().setLabel(xLabel+" ("+StatisticsBasePanel.viewersChartInSeconds+")");
		plot.getRangeAxis().setLabel(yLabel);

		scaleFactor=1;
	}

	/**
	 * Stellt den Diagrammrahmen für die Darstellung von Verteilungen über ein bestimmte (Nicht-Zeit-)Werte ein
	 * @param title	Titel des Diagramms
	 * @param xLabel	Beschriftung der x-Achse
	 * @param yLabel	Beschriftung der y-Achse
	 */
	protected void setupChartValuePercent(final String title, final String xLabel, final String yLabel) {
		chart.setTitle(title);

		final NumberAxis axis=new NumberAxis();
		final NumberFormat formater=NumberFormat.getPercentInstance();
		formater.setMinimumFractionDigits(1);
		formater.setMaximumFractionDigits(1);
		axis.setNumberFormatOverride(formater);
		axis.setLabel(yLabel);
		plot.setRangeAxis(axis);

		plot.getDomainAxis().setLabel(xLabel);
		plot.getRangeAxis().setLabel(yLabel);
	}

	/**
	 * Stellt den Diagrammrahmen für die Darstellung von Verteilungen über ein bestimmtes Intervall ein
	 * @param title	Titel des Diagramms
	 * @param xLabel	Beschriftung der x-Achse
	 * @param yLabel	Beschriftung der y-Achse
	 */
	protected void setupChartValue(final String title, final String xLabel, final String yLabel) {
		chart.setTitle(title);

		plot.getDomainAxis().setLabel(xLabel);
		plot.getRangeAxis().setLabel(yLabel);
	}

	/**
	 * Stellt den Diagrammrahmen für die Darstellung von Verteilungen über ein bestimmtes langes Zeitintervall ein
	 * @param title	Titel des Diagramms
	 * @param xLabel	Beschriftung der x-Achse
	 * @param yLabel	Beschriftung der y-Achse
	 */
	protected void setupLongTime(final String title, final String xLabel, final String yLabel) {
		chart.setTitle(title);
		plot.getDomainAxis().setLabel(xLabel+" ("+StatisticsBasePanel.viewersChartInHours+")");
		plot.getRangeAxis().setLabel(yLabel);
	}

	/**
	 * Stellt den Diagrammrahmen für die Darstellung von Prozentwerten über ein bestimmtes langes Zeitintervall ein
	 * @param title	Titel des Diagramms
	 * @param xLabel	Beschriftung der x-Achse
	 * @param yLabel	Beschriftung der y-Achse
	 */
	protected void setupLongTimePercent(final String title, final String xLabel, final String yLabel) {
		chart.setTitle(title);
		plot.getDomainAxis().setLabel(xLabel+" ("+StatisticsBasePanel.viewersChartInHours+")");

		NumberAxis axis=new NumberAxis();
		NumberFormat formater=NumberFormat.getPercentInstance();
		formater.setMinimumFractionDigits(1);
		formater.setMaximumFractionDigits(1);
		axis.setNumberFormatOverride(formater);
		axis.setLabel(yLabel);
		plot.setRangeAxis(axis);
	}

	/**
	 * Richtet die Tooltips für die einzelnen Linien ein.<br>
	 * Muss nach dem Einrichten der Datenserien aufgerufen werden.
	 */
	protected final void initTooltips() {
		final int count=data.getSeriesCount();
		final XYItemRenderer renderer=plot.getRenderer();

		DateFormat formaterDate;
		NumberFormat formater;

		for (int i=0;i<count;i++) switch (toolTipFormat) {
		case DAY_VALUE:
			formaterDate=new SimpleDateFormat("HH:mm");
			formaterDate.setTimeZone(TimeZone.getTimeZone("GMT"));
			renderer.setSeriesToolTipGenerator(i,new StandardXYToolTipGenerator(StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT,formaterDate,NumberFormat.getInstance(NumberTools.getLocale())));
			break;
		case DAY_PERCENT:
			formater=NumberFormat.getPercentInstance();
			formater.setMinimumFractionDigits(1);
			formater.setMaximumFractionDigits(1);
			formaterDate=new SimpleDateFormat("HH:mm");
			formaterDate.setTimeZone(TimeZone.getTimeZone("GMT"));
			renderer.setSeriesToolTipGenerator(i,new StandardXYToolTipGenerator(StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT,formaterDate,formater));
			break;
		case DEFAULT:
			renderer.setSeriesToolTipGenerator(i,new StandardXYToolTipGenerator(StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT,NumberFormat.getInstance(NumberTools.getLocale()),NumberFormat.getInstance(NumberTools.getLocale())));
			break;
		default:
			renderer.setSeriesToolTipGenerator(i,new StandardXYToolTipGenerator(StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT,NumberFormat.getInstance(NumberTools.getLocale()),NumberFormat.getInstance(NumberTools.getLocale())));
		}
	}

	/**
	 * Verkürzt die übergebene Verteilung
	 * @param source	Ausgangsverteilung (wird nicht verändert)
	 * @param steps	Maximale Anzahl an Datenpunkten in der neuen Verteilung (wird ein Wert &le;0 angegeben, so werden keine Werte abgeschnitten)
	 * @return	Abgeschnittene, neue Verteilung
	 */
	protected DataDistributionImpl truncate(final DataDistributionImpl source, int steps) {
		if (steps<=0) return source;
		steps=Math.min(steps,source.densityData.length);
		final DataDistributionImpl result=new DataDistributionImpl(source.upperBound*steps/source.densityData.length,steps);
		for (int i=0;i<result.densityData.length;i++) result.densityData[i]=source.densityData[i];
		return result;
	}

	/**
	 * Fügt eine Datenreihe zu dem Diagramm hinzu
	 * @param title	Titel der Datenreihe
	 * @param paint	Farbe der Datenreihe
	 * @return	Neu erzeugt (und bereits zu <code>plot</code> hinzugefügte) Datenreihe
	 */
	protected XYSeries addSeries(final String title, final Paint paint) {
		final XYSeries series=new XYSeries(title);
		data.addSeries(series);
		plot.getRenderer().setSeriesPaint(data.getSeriesCount()-1,paint);
		return series;
	}

	/**
	 * Fügt eine Datenreihe zu dem Diagramm hinzu
	 * @param title	Titel der Datenreihe
	 * @param paint	Farbe der Datenreihe
	 * @param dist	Darzustellende Verteilung
	 * @param upperBound	Obere Grenze für die Werte
	 * @return	Neu erzeugt (und bereits zu <code>plot</code> hinzugefügte) Datenreihe
	 */
	protected XYSeries addSeries(final String title, final Paint paint, final AbstractRealDistribution dist, final int upperBound) {
		final XYSeries series=new XYSeries(title);
		for (int i=0;i<=1000;i++) {
			series.add(((double)upperBound)*i/1000,dist.density(((double)upperBound)*i/1000),false);
		}
		data.addSeries(series);
		plot.getRenderer().setSeriesPaint(data.getSeriesCount()-1,paint);
		return series;
	}

	/**
	 * Fügt eine Datenreihe zu dem Diagramm hinzu
	 * @param title	Titel der Datenreihe
	 * @param paint	Farbe der Datenreihe
	 * @param dist	Darzustellende Verteilung
	 * @return	Neu erzeugt (und bereits zu <code>plot</code> hinzugefügte) Datenreihe
	 */
	protected XYSeries addSeries(final String title, final Paint paint, final DataDistributionImpl dist) {
		final XYSeries series=new XYSeries(title);
		for (int i=0;i<dist.densityData.length;i++) {
			series.add(dist.upperBound*i/(dist.densityData.length-1)/scaleFactor,dist.densityData[i],false);
			series.fireSeriesChanged();
		}
		data.addSeries(series);
		plot.getRenderer().setSeriesPaint(data.getSeriesCount()-1,paint);

		return series;
	}

	/**
	 * Fügt eine Datenreihe zu dem Diagramm hinzu
	 * @param title	Titel der Datenreihe
	 * @param paint	Farbe der Datenreihe
	 * @param dist	Darzustellende Verteilung
	 * @return	Neu erzeugt (und bereits zu <code>plot</code> hinzugefügte) Datenreihe
	 */
	protected XYSeries addSeries(final String title, final Paint paint, final double[] dist) {
		final XYSeries series=new XYSeries(title);
		for (int i=0;i<dist.length;i++) series.add(i,dist[i],false);
		series.fireSeriesChanged();
		data.addSeries(series);
		plot.getRenderer().setSeriesPaint(data.getSeriesCount()-1,paint);

		return series;
	}

	/**
	 * Fügt eine Datenreihe zu dem Diagramm hinzu
	 * @param title	Titel der Datenreihe
	 * @param paint	Farbe der Datenreihe
	 * @param dist	Darzustellende Verteilung
	 * @param count	Maximale Anzahl an zu verwendenden Werten
	 * @return	Neu erzeugt (und bereits zu <code>plot</code> hinzugefügte) Datenreihe
	 */
	protected XYSeries addSeries(final String title, final Paint paint, final double[] dist, final int count) {
		final XYSeries series=new XYSeries(title);
		for (int i=0;i<Math.min(count,dist.length);i++) series.add(i,dist[i],false);
		series.fireSeriesChanged();
		data.addSeries(series);
		plot.getRenderer().setSeriesPaint(data.getSeriesCount()-1,paint);

		return series;
	}

	/**
	 * Fügt einen Teil einer Datenreihe zu dem Diagramm hinzu
	 * @param title	Titel der Datenreihe
	 * @param paint	Farbe der Datenreihe
	 * @param dist	Darzustellende Verteilung
	 * @param count	Anzahl der Werte, die zu der Datenreihe hinzugefügt werden sollen (vom Anfang der Reihe an gezählt)
	 * @param smoothing	Glättung (0=keine Glättung, 1=Durchschnitt über 3 Werte, 2=Durchschnitt über 5 Werte usw.)
	 * @return	Neu erzeugt (und bereits zu <code>plot</code> hinzugefügte) Datenreihe
	 */
	protected XYSeries addSeriesPart(final String title, final Paint paint, final DataDistributionImpl dist, final int count, final int smoothing) {
		final XYSeries series=new XYSeries(title);
		for (int i=0;i<Math.min(count,dist.densityData.length);i++) {
			double sum=0;
			int values=0;
			for (int j=Math.max(0,i-smoothing);j<=Math.min(dist.densityData.length-1,i+smoothing);j++) {
				values++;
				sum+=dist.densityData[j];
			}
			series.add(dist.upperBound*i/(dist.densityData.length-1),sum/values,false);
		}
		series.fireSeriesChanged();
		data.addSeries(series);
		plot.getRenderer().setSeriesPaint(data.getSeriesCount()-1,paint);
		return series;
	}

	/**
	 * Fügt einen Teil einer Datenreihe zu dem Diagramm hinzu
	 * Die X-Werte der Reihe werden dabei durch 3600 geteilt, d.h. Sekundenwerte werden in Stunden angegeben.
	 * @param title	Titel der Datenreihe
	 * @param paint	Farbe der Datenreihe
	 * @param dist	Darzustellende Verteilung
	 * @param count	Anzahl der Werte, die zu der Datenreihe hinzugefügt werden sollen (vom Anfang der Reihe an gezählt)
	 * @param smoothing	Glättung (0=keine Glättung, 1=Durchschnitt über 3 Werte, 2=Durchschnitt über 5 Werte usw.)
	 * @return	Neu erzeugt (und bereits zu <code>plot</code> hinzugefügte) Datenreihe
	 */
	protected XYSeries addLongSeriesPart(final String title, final Paint paint, final DataDistributionImpl dist, final int count, final int smoothing) {
		final XYSeries series=new XYSeries(title);
		for (int i=0;i<Math.min(count,dist.densityData.length);i++) {
			double sum=0;
			int values=0;
			for (int j=Math.max(0,i-smoothing);j<=Math.min(dist.densityData.length-1,i+smoothing);j++) {
				values++;
				sum+=dist.densityData[j];
			}
			series.add(dist.upperBound*i/(dist.densityData.length-1)/3600,sum/values,false);
		}
		series.fireSeriesChanged();
		data.addSeries(series);
		plot.getRenderer().setSeriesPaint(data.getSeriesCount()-1,paint);
		return series;
	}

	/**
	 * Fügt eine Datenreihe zu dem Diagramm hinzu
	 * @param title	Titel der Datenreihe
	 * @param paint	Farbe der Datenreihe
	 * @param dist	Darzustellende Verteilung
	 * @param steps	Anzahl der Datenpunkte der Verteilung, die verwendet werden sollen (vom Anfang an gezählt)
	 * @return	Neu erzeugt (und bereits zu <code>plot</code> hinzugefügte) Datenreihe
	 */
	protected XYSeries addSeriesTruncated(final String title, final Paint paint, final DataDistributionImpl dist, final int steps) {
		return addSeries(title,paint,truncate(dist,steps));
	}

	/**
	 * Fügt eine Datenreihe zu dem Diagramm zur zweiten y-Achse hinzu
	 * @param title	Titel der Datenreihe
	 * @param paint	Farbe der Datenreihe
	 * @param dist	Darzustellende Verteilung
	 * @return	Neu erzeugt (und bereits zu <code>plot</code> hinzugefügte) Datenreihe
	 */
	protected XYSeries addSeriesToSecondSet(final String title, final Paint paint, final DataDistributionImpl dist) {
		if (data2==null) {
			data2=new XYSeriesCollection();
			plot.setDataset(1,data2);
			plot.setRangeAxis(1,new NumberAxis());
			plot.mapDatasetToRangeAxis(1,1);
			XYLineAndShapeRenderer old=((XYLineAndShapeRenderer)(plot.getRenderer()));
			XYLineAndShapeRenderer renderer=null;
			try {renderer = (XYLineAndShapeRenderer)(old.clone());} catch (CloneNotSupportedException e) {}
			plot.setRenderer(1,renderer);
		}

		final XYSeries series=new XYSeries(title);
		for (int i=0;i<dist.densityData.length;i++) {
			series.add(dist.upperBound*i/(dist.densityData.length-1)*1000*1800,dist.densityData[i],false);
		}
		series.fireSeriesChanged();
		data2.addSeries(series);
		plot.getRendererForDataset(data2).setSeriesPaint(data2.getSeriesCount()-1,paint);
		return series;
	}

	/**
	 * Versieht eine XY-Linien-Serie mit einem halbtransparenten Füllbereich passend zu der Farbe der Linie
	 * @param seriesNumber	Nummer der Datenserie
	 */
	protected void addFillColor(final int seriesNumber) {
		Paint p=plot.getRenderer().getSeriesPaint(seriesNumber);
		if (p instanceof Color) addFillColor(seriesNumber,(Color)p);
	}

	/**
	 * Versieht eine XY-Linien-Serie mit einem halbtransparenten Füllbereich in der angegebenen Farbe
	 * @param seriesNumber	Nummer der Datenserie
	 * @param fillColor	Füllfarbe (Transparenz wird automatisch eingestellt und muss hier nicht angegeben werden)
	 */
	protected void addFillColor(final int seriesNumber, final Color fillColor) {
		XYSeries series=data.getSeries(seriesNumber);

		int nr=plot.getDatasetCount()+1;

		final XYSeriesCollection data2=new XYSeriesCollection();
		final XYSeries series2=new XYSeries(series.getKey()+"Füllbereich");

		for (int i=0;i<series.getItemCount();i++) series2.add(series.getX(i),series.getY(i),false);
		series2.fireSeriesChanged();
		data2.addSeries(series2);

		XYAreaRenderer renderer=new XYAreaRenderer();
		renderer.setSeriesVisibleInLegend(0,false);
		plot.setDataset(nr,data2);
		plot.setRenderer(nr,renderer);

		Color c=new Color(fillColor.getRed(),fillColor.getGreen(),fillColor.getBlue(),20);
		renderer.setSeriesPaint(0,c);
	}

	/**
	 * Stellt den Zoomlevel basierend auf den vorhandenen Daten ein, schließt dabei
	 * aber ein paar Datenpunkte von der Berechnung aus.
	 * @param removeFirst	Anzahl an Datenpunkten (vom Anfang an gezählt), die nicht berücksichtigt werden sollen
	 */
	protected void smartZoom(final int removeFirst) {
		smartZoomValue=Math.max(0,removeFirst);
		double max=0;
		for (int i=0;i<data.getSeriesCount();i++) {
			final XYSeries series=data.getSeries(i);
			final int count=series.getItemCount();
			for (int j=Math.max(0,removeFirst);j<count;j++) max=Math.max(max,series.getDataItem(j).getYValue());
		}

		plot.getRangeAxis().setAutoRange(false);
		plot.getRangeAxis().setRange(0,Math.max(0.1,max*1.1));
	}

	@Override
	public void unZoom() {
		if (domainIsDay) {
			((DateAxis)(plot.getDomainAxis())).setMinimumDate(new Date(0));
			((DateAxis)(plot.getDomainAxis())).setMaximumDate(new Date(1000*86400));
		} else plot.getDomainAxis().setAutoRange(true);
		if (smartZoomValue>=0) smartZoom(smartZoomValue); else plot.getRangeAxis().setAutoRange(true);
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

		boolean isFirstSeries=true;

		if (data!=null) for (int i=0;i<data.getSeriesCount();i++) {
			final XYSeries series=data.getSeries(i);
			final String name=series.getKey().toString();
			final List<String> categories=(isFirstSeries)?new ArrayList<>(1+series.getItemCount()):null;
			final List<String> line=new ArrayList<>(1+series.getItemCount());
			line.add((name==null)?"":name);
			if (categories!=null) categories.add("");
			for (Object obj: series.getItems()) if (obj instanceof XYDataItem) {
				final XYDataItem data=(XYDataItem)obj;
				if (categories!=null) categories.add(NumberTools.formatNumberMax(data.getXValue()));
				line.add(NumberTools.formatNumberMax(data.getYValue()));
			}
			isFirstSeries=false;
			if (categories!=null) table.addLine(categories);
			table.addLine(line);

		}

		if (data2!=null) for (int i=0;i<data2.getSeriesCount();i++) {
			final XYSeries series=data2.getSeries(i);
			final String name=series.getKey().toString();
			final List<String> categories=(isFirstSeries)?new ArrayList<>(1+series.getItemCount()):null;
			final List<String> line=new ArrayList<>(1+series.getItemCount());
			line.add((name==null)?"":name);
			if (categories!=null) categories.add("");
			for (Object obj: series.getItems()) if (obj instanceof XYDataItem) {
				final XYDataItem data=(XYDataItem)obj;
				if (categories!=null) categories.add(NumberTools.formatNumberMax(data.getXValue()));
				line.add(NumberTools.formatNumberMax(data.getYValue()));
			}
			isFirstSeries=false;
			if (categories!=null) table.addLine(categories);
			table.addLine(line);
		}

		return table.transpose(true);
	}

	@Override
	public TableChart getTableChartFromChart() {
		final TableChart tableChart=new TableChart(getTableFromChart());

		tableChart.setupAxis(plot.getDomainAxis().getLabel(),plot.getRangeAxis().getLabel());
		if (!plot.getRangeAxis().isAutoRange()) {
			tableChart.setupAxisMinY(plot.getRangeAxis().getRange().getLowerBound());
			tableChart.setupAxisMaxY(plot.getRangeAxis().getRange().getUpperBound());
		}
		tableChart.setupChart(TableChart.ChartMode.LINE);

		return tableChart;
	}

	@Override
	protected File showSaveDialog(final Component owner) {
		final JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		fc.setDialogTitle(StatisticsBasePanel.viewersSaveImage);
		final FileFilter jpg=new FileNameExtensionFilter(StatisticsBasePanel.fileTypeJPG+" (*.jpg, *.jpeg)","jpg","jpeg");
		final FileFilter gif=new FileNameExtensionFilter(StatisticsBasePanel.fileTypeGIF+" (*.gif)","gif");
		final FileFilter png=new FileNameExtensionFilter(StatisticsBasePanel.fileTypePNG+" (*.png)","png");
		final FileFilter bmp=new FileNameExtensionFilter(StatisticsBasePanel.fileTypeBMP+" (*.bmp)","bmp");
		final FileFilter docx=new FileNameExtensionFilter(StatisticsBasePanel.fileTypeWordWithImage+" (*.docx)","docx");
		final FileFilter pdf=new FileNameExtensionFilter(StatisticsBasePanel.fileTypePDF+" (*.pdf)","pdf");
		final FileFilter sce=new FileNameExtensionFilter(StatisticsBasePanel.fileTypeSCE+" (*.sce)","sce");
		fc.addChoosableFileFilter(png);
		fc.addChoosableFileFilter(jpg);
		fc.addChoosableFileFilter(gif);
		fc.addChoosableFileFilter(bmp);
		fc.addChoosableFileFilter(docx);
		fc.addChoosableFileFilter(pdf);
		fc.addChoosableFileFilter(sce);
		fc.setFileFilter(png);
		fc.setAcceptAllFileFilterUsed(false);

		if (fc.showSaveDialog(owner)!=JFileChooser.APPROVE_OPTION) return null;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();

		if (file.getName().indexOf('.')<0) {
			if (fc.getFileFilter()==jpg) file=new File(file.getAbsoluteFile()+".jpg");
			if (fc.getFileFilter()==gif) file=new File(file.getAbsoluteFile()+".gif");
			if (fc.getFileFilter()==png) file=new File(file.getAbsoluteFile()+".png");
			if (fc.getFileFilter()==bmp) file=new File(file.getAbsoluteFile()+".bmp");
			if (fc.getFileFilter()==docx) file=new File(file.getAbsoluteFile()+".docx");
			if (fc.getFileFilter()==pdf) file=new File(file.getAbsoluteFile()+".pdf");
			if (fc.getFileFilter()==sce) file=new File(file.getAbsoluteFile()+".sce");
		}

		if (file.exists()) {
			if (!MsgBox.confirmOverwrite(owner,file)) return null;
		}

		return file;
	}

	private String saveSCE() {
		final StringBuilder result=new StringBuilder();

		List<Double> dataX=null;
		final Map<String,List<Double>> dataY=new HashMap<>();
		final Map<String,Paint> dataPaint=new HashMap<>();

		boolean isFirstSeries=true;

		if (data!=null) for (int i=0;i<data.getSeriesCount();i++) {
			final XYSeries series=data.getSeries(i);
			final String name=series.getKey().toString();
			if (dataX==null) dataX=new ArrayList<>(1+series.getItemCount());
			final List<Double> line=new ArrayList<>(1+series.getItemCount());
			for (Object obj: series.getItems()) if (obj instanceof XYDataItem) {
				final XYDataItem data=(XYDataItem)obj;
				if (isFirstSeries) dataX.add(data.getXValue());
				line.add(data.getYValue());
			}
			isFirstSeries=false;
			if (name!=null) dataY.put(name,line);
			final Paint paint=plot.getRendererForDataset(data).getSeriesPaint(i);
			if (name!=null && paint!=null) dataPaint.put(name,paint);
		}

		if (data2!=null) for (int i=0;i<data2.getSeriesCount();i++) {
			final XYSeries series=data2.getSeries(i);
			final String name=series.getKey().toString();
			if (dataX==null) dataX=new ArrayList<>(1+series.getItemCount());
			final List<Double> line=new ArrayList<>(1+series.getItemCount());
			for (Object obj: series.getItems()) if (obj instanceof XYDataItem) {
				final XYDataItem data=(XYDataItem)obj;
				if (isFirstSeries) dataX.add(data.getXValue());
				line.add(data.getYValue());
			}
			isFirstSeries=false;
			if (name!=null) dataY.put(name,line);
			final Paint paint=plot.getRendererForDataset(data2).getSeriesPaint(i);
			if (name!=null && paint!=null) dataPaint.put(name,paint);
		}

		int nr;

		result.append("// "+chart.getTitle().getText()+"\n");
		result.append("\n");

		result.append("// Data\n");
		result.append("x=[\n");
		if (dataX!=null) for (double d: dataX) {
			result.append("  "+NumberTools.formatSystemNumber(d)+"\n");
		}
		result.append("];\n");
		result.append("\n");

		nr=1;
		for (String name: dataY.keySet()) {
			result.append("y"+nr+"=[\n");
			final List<Double> list=dataY.get(name);
			for (double d: list) {
				result.append("  "+NumberTools.formatSystemNumber(d)+"\n");
			}
			if (dataX!=null) for (int i=0;i<dataX.size()-list.size();i++) result.append("  0\n");
			result.append("];\n");
			result.append("\n");
			nr++;
		}

		result.append("// Clear\n");
		result.append("clf();\n");
		result.append("\n");
		result.append("// Draw\n");
		result.append("plot(");
		for (int i=1;i<=dataY.size();i++) {
			if (i!=1) result.append(",");
			result.append("x,y"+i);
		}
		result.append(");\n");
		result.append("\n");

		result.append("// Setup frame\n");
		result.append("title(\""+chart.getTitle().getText().replace("\"","'")+"\",\"fontsize\",4);\n");
		result.append("xlabel(\""+plot.getDomainAxis().getLabel().replace("\"","'")+"\",\"fontsize\",3);\n");
		result.append("ylabel(\""+plot.getRangeAxis().getLabel().replace("\"","'")+"\",\"fontsize\",3);\n");
		result.append("legend(");
		boolean first=true;
		for (String name: dataY.keySet()) {
			if (first) first=false; else result.append(",");
			result.append("\"");
			result.append(name.replace("\"","'"));
			result.append("\"");
		}
		result.append(")\n");
		result.append("\n");

		result.append("// Setup background\n");
		result.append("f=gcf();\n");
		result.append("f.background=color(245,245,245);\n");
		result.append("\n");

		result.append("// Setup axis\n");
		result.append("a=gca();\n");
		result.append("a.font_size=2;\n");
		result.append("\n");

		result.append("// Setup series\n");
		result.append("s=a.children(2).children;\n");
		result.append("\n");

		nr=dataY.size();
		for (String name: dataY.keySet()) {
			result.append("// "+name.replace("\"","'")+"\n");
			final Paint paint=dataPaint.get(name);
			if (paint instanceof Color) {
				final Color c=(Color)paint;
				result.append("s("+nr+").foreground=color("+c.getRed()+","+c.getGreen()+","+c.getBlue()+");\n");
			}
			result.append("s("+nr+").thickness=2;\n");
			result.append("\n");
			nr--;
		}

		return result.toString();
	}

	@Override
	public boolean save(Component owner, File file) {
		if (file.toString().toLowerCase().endsWith(".sce")) {
			return Table.saveTextToFile(saveSCE(),file);
		} else {
			return super.save(owner,file);
		}
	}
}
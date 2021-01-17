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
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieToolTipGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

import mathtools.NumberTools;
import mathtools.Table;
import mathtools.TableChart;
import mathtools.distribution.DataDistributionImpl;


/** Basisklasse zur Anzeige von {@link JFreeChart}-basierenden Tortendiagrammen
 * @author Alexander Herzog
 * @see StatisticViewerJFreeChart
 */
public class StatisticViewerPieChart extends StatisticViewerJFreeChart {
	/**
	 * Ermöglicht für abgeleitete Klassen einen Zugriff auf das {@link PiePlot}-Element
	 */
	protected PiePlot<String> pie;

	/**
	 * Ermöglicht für abgeleitete Klassen einen Zugriff auf das {@link DefaultPieDataset}-Objekt,
	 * welches sich im {@link PiePlot} befindet.
	 * @see #initPieChart(String)
	 * @see #addPieSegment(String, double)
	 * @see #addPieSegment(String, double, Paint)
	 */
	protected DefaultPieDataset<String> data;

	@Override
	public ViewerImageType getImageType() {
		return ViewerImageType.IMAGE_TYPE_PIE;
	}

	/**
	 * Initialisiert das Diagramm
	 * @param title	Diagrammtitel
	 */
	@SuppressWarnings("unchecked")
	public void initPieChart(String title) {
		data=new DefaultPieDataset<>();
		initChart(ChartFactory.createPieChart(title,data,true,true,false));
		pie=(PiePlot<String>)chart.getPlot();

		pie.setToolTipGenerator(new StandardPieToolTipGenerator(StandardPieToolTipGenerator.DEFAULT_TOOLTIP_FORMAT,NumberTools.getLocale()));

		chartSetup.setupPiePlot(pie);
	}

	/**
	 * Fügt ein Segment zum Tortendiagramm hinzu
	 * @param name	Names des neuen Segments
	 * @param value	Wert des neuen Segments
	 */
	protected void addPieSegment(String name, double value) {
		data.setValue(name,value);
	}

	/**
	 * Fügt ein Segment zum Tortendiagramm hinzu
	 * @param name	Names des neuen Segments
	 * @param value	Wert des neuen Segments
	 * @param paint	Füllfarbe des neuen Segments
	 */
	protected void addPieSegment(String name, double value, Paint paint) {
		addPieSegment(name,value);
		pie.setSectionPaint(data.getKey(data.getItemCount()-1),paint);
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

	@Override
	public boolean getCanDo(CanDoAction canDoType) {
		if (canDoType==CanDoAction.CAN_DO_UNZOOM) return false;
		return super.getCanDo(canDoType);
	}

	@Override
	public void unZoom() {}

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

		if (data!=null) for (int i=0;i<data.getItemCount();i++) {
			final String key=data.getKey(i);
			final List<String> line=new ArrayList<>(2);
			line.add(key.toString());
			final Number num=data.getValue(key);
			final double d=(num==null)?0.0:num.doubleValue();
			line.add(NumberTools.formatNumberMax(d));
			table.addLine(line);
		}

		return table;
	}

	@Override
	public TableChart getTableChartFromChart() {
		final TableChart tableChart=new TableChart(getTableFromChart());

		tableChart.setupChart(TableChart.ChartMode.PIE);

		return tableChart;
	}
}

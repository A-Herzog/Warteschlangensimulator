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
package systemtools.statistics;

import java.awt.Paint;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.MultiplePiePlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.util.TableOrder;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import mathtools.distribution.DataDistributionImpl;

/** Basisklasse zur Anzeige von {@link JFreeChart}-basierenden Mehrfach-Tortendiagrammen
 * @author Alexander Herzog
 * @see StatisticViewerJFreeChart
 */
public class StatisticViewerPieChartMulti extends StatisticViewerJFreeChart {
	/**
	 * Ermöglicht für abgeleitete Klassen einen Zugriff auf das {@link MultiplePiePlot}-Element
	 */
	protected MultiplePiePlot plot;

	/**
	 * Ermöglicht für abgeleitete Klassen einen Zugriff auf das innere {@link PiePlot}-Element
	 */
	protected PiePlot<?> pie;

	/**
	 * Ermöglicht für abgeleitete Klassen einen Zugriff auf das {@link DefaultPieDataset}-Objekt,
	 * welches sich im {@link MultiplePiePlot} befindet.
	 * @see #initPieChart(String)
	 * @see #addPieSegment(String, String, double)
	 * @see #addPieSegment(String, String, double, Paint)
	 */
	protected DefaultCategoryDataset data;

	@Override
	public ViewerImageType getImageType() {
		return ViewerImageType.IMAGE_TYPE_PIE;
	}

	/**
	 * Initialisiert das Diagramm
	 * @param title	Diagrammtitel
	 */
	public void initPieChart(final String title) {
		data=new DefaultCategoryDataset();
		initChart(ChartFactory.createMultiplePieChart(title,data,TableOrder.BY_ROW,true,true,false));
		plot=(MultiplePiePlot)chart.getPlot();
		pie=(PiePlot<?>)this.plot.getPieChart().getPlot();

		chartSetup.setupChart(pie.getChart());
		plot.setBackgroundPaint(null);
		pie.getChart().setBackgroundPaint(null);
	}

	/**
	 * Fügt ein Segment zum Tortendiagramm hinzu
	 * @param pie	Teildiagramm zu dem das Segment hinzugefügt werden soll
	 * @param name	Names des neuen Segments
	 * @param value	Wert des neuen Segments
	 */
	protected void addPieSegment(final String pie, final String name, final double value) {
		data.addValue(value,pie,name);
	}

	/**
	 * Fügt ein Segment zum Tortendiagramm hinzu
	 * @param pie	Teildiagramm zu dem das Segment hinzugefügt werden soll
	 * @param name	Names des neuen Segments
	 * @param value	Wert des neuen Segments
	 * @param paint	Füllfarbe des neuen Segments
	 */
	protected void addPieSegment(final String pie, final String name, final double value, final Paint paint) {
		data.addValue(value,pie,name);
		this.pie.setSectionPaint(name,paint);
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
}

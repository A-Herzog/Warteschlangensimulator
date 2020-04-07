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
package mathtools;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.poi.ss.usermodel.Sheet;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPlotArea;

/**
 * Diese Klasse ermöglicht das Einfügen eines Diagramms in ein
 * Excel-Sheet ({@link Sheet}), welches Daten basierend auf einer
 * {@link Table} enthält.
 * @author Alexander Herzog
 * @version 1.0
 * @see TableChartBase
 */
public class TableChart extends TableChartBase {
	/**
	 * Art des Diagramms
	 * @author Alexander Herzog
	 */
	public enum ChartMode {
		/** Liniendiagramm */
		LINE,

		/** Balkendiagramm */
		BAR,

		/** Tortendiagramm */
		PIE
	}

	/**
	 * Konstruktor der Klasse
	 * @param table	Tabelle, die die Daten enthält
	 */
	public TableChart(final Table table) {
		super(table);
	}

	@Override
	protected Point getChartPosition(final int chartYIndex) {
		return new Point(getTableSize().width+1,0+24*chartYIndex);
	}

	@Override
	protected Dimension getChartDimension() {
		return new Dimension(12,22);
	}

	private ChartMode setupMode;
	private final List<Range> series=new ArrayList<>();
	private final List<Object> seriesNames=new ArrayList<>();
	private Range categories;

	@Override
	protected void buildChart(final CTPlotArea plotArea, final String sheetName, final int chartYIndex) {
		if (setupMode==null) return;
		switch (setupMode) {
		case LINE:
			buildLineChart(sheetName,series,seriesNames,categories);
			break;
		case BAR:
			buildBarChart(sheetName,series,seriesNames,categories);
			break;
		case PIE:
			buildPieChart(sheetName,series,seriesNames,categories);
			break;
		default:
			break;
		}

		series.clear();
		seriesNames.clear();
		categories=null;
	}

	/**
	 * Konfiguriert das Diagramm.
	 * @param chartMode	Art des Diagramms
	 * @param series	Datenserien für das Diagramm
	 * @param seriesNames	Namen der Datenserien
	 * @param categories	Kategorien für die x-Achse (darf <code>null</code> sein)
	 */
	public final void setupChart(final ChartMode chartMode, final List<Range> series, final List<Object> seriesNames, final Range categories) {
		if (chartMode==null || series==null || seriesNames==null) return;
		setupMode=chartMode;

		this.series.addAll(series.stream().map(range->new Range(range)).collect(Collectors.toList()));
		for (Object obj: seriesNames) {
			if (obj instanceof String) this.seriesNames.add(obj);
			if (obj instanceof Cell) this.seriesNames.add(new Cell((Cell)obj));
		}
		if (categories!=null) this.categories=new Range(categories);
	}

	/**
	 * Konfiguriert das Diagramm.
	 * @param chartMode	Art des Diagramms
	 * @param series	Datenserien für das Diagramm
	 * @param seriesNames	Namen der Datenserien
	 * @param categories	Kategorien für die x-Achse (darf <code>null</code> sein)
	 */
	public final void setupChart(final ChartMode chartMode, final Range[] series, final Object[] seriesNames, final Range categories) {
		if (chartMode==null || series==null || seriesNames==null) return;
		setupMode=chartMode;

		this.series.addAll(Stream.of(series).map(range->new Range(range)).collect(Collectors.toList()));
		for (Object obj: seriesNames) {
			if (obj instanceof String) this.seriesNames.add(obj);
			if (obj instanceof Cell) this.seriesNames.add(new Cell((Cell)obj));
		}
		if (categories!=null) this.categories=new Range(categories);
	}

	/**
	 * Konfiguriert das Diagramm.<br>
	 * Es werden dabei die Daten gemäß einer gewählten Spalte automatisch konfiguriert.
	 * D.h. insbesondere, dass das Diagramm eine Datenreihe enthält.
	 * @param chartMode	Art des Diagramms
	 * @param colNr	0-basierter Index der zu verwendenden Spalte für die Datenreihe
	 */
	public final void setupChart(final ChartMode chartMode, final int colNr) {
		final Dimension size=getTableSize();

		final List<Range> series=new ArrayList<>();
		final List<Object> seriesNames=new ArrayList<>();
		series.add(new Range(colNr,1,colNr,size.height-1));
		seriesNames.add(new Cell(colNr,0));

		setupChart(chartMode,series,seriesNames,new Range(0,1,0,size.height-1));
	}

	/**
	 * Konfiguriert das Diagramm.<br>
	 * Die Daten werden dabei automatisch der Tabelle entnommen.
	 * @param chartMode	Art des Diagramms
	 */
	public final void setupChart(final ChartMode chartMode) {
		final Dimension size=getTableSize();

		final List<Range> series=new ArrayList<>();
		final List<Object> seriesNames=new ArrayList<>();
		final Range categories;
		if (chartMode==ChartMode.PIE) {
			if (size.width>=2) {
				series.add(new Range(1,0,1,size.height-1));
				seriesNames.add("");
			}
			categories=new Range(0,0,0,size.height-1);
		} else {
			for (int i=1;i<size.width;i++) {
				series.add(new Range(i,1,i,size.height-1));
				seriesNames.add(new Cell(i,0));
			}
			categories=new Range(0,1,0,size.height-1);
		}

		setupChart(chartMode,series,seriesNames,categories);
	}
}
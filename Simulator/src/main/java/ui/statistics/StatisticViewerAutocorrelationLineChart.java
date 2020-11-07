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
package ui.statistics;

import java.awt.Color;
import java.util.Map;

import org.jfree.data.xy.XYSeries;

import language.Language;
import simulator.statistics.Statistics;
import statistics.StatisticsDataPerformanceIndicator;
import systemtools.statistics.StatisticViewerLineChart;

/**
 * Dieser Viewer zeigt die Autokorrelation der Wartezeiten als Liniendiagramm an.
 * @see StatisticViewerLineChart
 * @author Alexander Herzog
 */

public class StatisticViewerAutocorrelationLineChart extends StatisticViewerLineChart {
	/** Statistikobjekt, aus dem die anzuzeigenden Daten entnommen werden sollen */
	private final Statistics statistics;
	/** Gibt an, welche Daten genau ausgegeben werden sollen */
	private final Mode mode;

	/**
	 * Wählt die von {@link StatisticViewerAutocorrelationLineChart} auszugebende Information aus.
	 * @author Alexander Herzog
	 * @see StatisticViewerAutocorrelationLineChart#StatisticViewerAutocorrelationLineChart(Statistics, Mode)
	 */
	public enum Mode {
		/** Autokorrelation über alle Wartezeiten */
		MODE_ALL_WAITING,
		/** Autokorrelation über die Wartezeiten nach Kundentypen */
		MODE_BY_CLIENTTYPE_WAITING,
		/** Autokorrelation über die Wartezeiten nach Stationen */
		MODE_BY_STATION_WAITING,
		/** Autokorrelation über die Wartezeiten nach Stationen (zusätzlich ausdifferenziert nach Kundentypen) */
		MODE_BY_STATION_CLIENT_WAITING
	}

	/** Farben für die Diagrammlinien */
	private static final Color[] COLORS=new Color[]{Color.RED,Color.BLUE,Color.GREEN,Color.BLACK};

	/**
	 * Konstruktor der Klasse
	 * @param statistics	Statistikobjekt, aus dem die anzuzeigenden Daten entnommen werden sollen
	 * @param mode	Gibt an, welche Daten genau ausgegeben werden sollen
	 * @see Mode
	 */
	public StatisticViewerAutocorrelationLineChart(final Statistics statistics, final Mode mode) {
		super();
		this.statistics=statistics;
		this.mode=mode;
	}

	/**
	 * Fügt eine Datenreihe zu dem Diagramm hinzu.
	 * @param indicator	Datenreihe
	 * @param name	Name der Datenreihe
	 * @param color	Farbe für die Datenreihe
	 */
	private void addSeries(final StatisticsDataPerformanceIndicator indicator, final String name, final Color color) {
		if (!indicator.isCorrelationAvailable()) return;

		final XYSeries series=addSeries(name,color);

		final double[] data=indicator.getCorrelationData();
		for (int i=0;i<data.length;i++) {
			series.add(i*StatisticsDataPerformanceIndicator.CORRELATION_RANGE_STEPPING,Math.abs(data[i]),false);
		}
		series.fireSeriesChanged();
	}

	@Override
	protected void firstChartRequest() {
		initLineChart(Language.tr("Statistics.ResultsAutocorrelation"));
		setupChartValuePercent(Language.tr("Statistics.ResultsAutocorrelation"),Language.tr("Statistics.ResultsAutocorrelation.Distance"),Language.tr("Statistics.ResultsAutocorrelation.Autocorrelation"));

		int i=0;

		switch (mode) {
		case MODE_ALL_WAITING:
			addSeries(statistics.clientsAllWaitingTimes,Language.tr("Statistics.ResultsAutocorrelation.WaitingTime.AllClients"),Color.BLACK);
			break;
		case MODE_BY_CLIENTTYPE_WAITING:
			final Map<String,Color> colorMap=statistics.editModel.clientData.getStatisticColors(statistics.editModel.surface.getClientTypes());
			for (String name: statistics.clientsWaitingTimes.getNames()) {
				final StatisticsDataPerformanceIndicator indicator=(StatisticsDataPerformanceIndicator)statistics.clientsWaitingTimes.get(name);
				Color c=colorMap.get(name);
				if (c==null) c=COLORS[i++%COLORS.length];
				addSeries(indicator,name,c);
			}
			break;
		case MODE_BY_STATION_WAITING:
			for (String name: statistics.stationsWaitingTimes.getNames()) {
				final StatisticsDataPerformanceIndicator indicator=(StatisticsDataPerformanceIndicator)statistics.stationsWaitingTimes.get(name);
				addSeries(indicator,name,COLORS[i++%COLORS.length]);
			}
			break;
		case MODE_BY_STATION_CLIENT_WAITING:
			for (String name: statistics.stationsWaitingTimesByClientType.getNames()) {
				final StatisticsDataPerformanceIndicator indicator=(StatisticsDataPerformanceIndicator)statistics.stationsWaitingTimesByClientType.get(name);
				addSeries(indicator,name,COLORS[i++%COLORS.length]);
			}
			break;
		}

		smartZoom(1);
		initTooltips();
	}
}

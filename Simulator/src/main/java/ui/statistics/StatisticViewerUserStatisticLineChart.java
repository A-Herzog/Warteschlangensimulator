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
import java.net.URL;

import language.Language;
import mathtools.distribution.DataDistributionImpl;
import simulator.statistics.Statistics;
import statistics.StatisticsDataCollector;
import statistics.StatisticsDataPerformanceIndicatorWithNegativeValues;
import systemtools.statistics.StatisticViewerLineChart;
import ui.help.Help;
import ui.tools.FlatLaFHelper;

/**
 * Dieser Viewer stellt die benutzerdefinierten Statistikdaten als Verteilungsdiagramm dar.
 * @see StatisticViewerLineChart
 * @see Statistics#userStatistics
 * @author Alexander Herzog
 */
public class StatisticViewerUserStatisticLineChart extends StatisticViewerLineChart {
	/** Statistikobjekt, aus dem die anzuzeigenden Daten entnommen werden sollen */
	private final Statistics statistics;
	/** Gibt an, welche Daten genau ausgegeben werden sollen */
	private final Mode mode;

	/**
	 * Wählt die von {@link StatisticViewerUserStatisticLineChart} auszugebende Information aus.
	 * @author Alexander Herzog
	 * @see StatisticViewerUserStatisticLineChart#StatisticViewerUserStatisticLineChart(Statistics, Mode)
	 */
	public enum Mode {
		/** Diagramm mit der Verteilung der Werte */
		MODE_ALL,
		/** Diagramm mit den Anzahlen pro Intervall */
		MODE_INTERVAL_COUNT,
		/** Diagramm mit den Mittelwerten pro Intervall */
		MODE_INTERVAL
	}

	/** Farben für die Diagrammlinien */
	private static final Color[] COLORS=new Color[]{Color.RED,Color.BLUE,Color.GREEN,Color.BLACK};

	/**
	 * Konstruktor der Klasse
	 * @param statistics	Statistikobjekt, aus dem die anzuzeigenden Daten entnommen werden sollen
	 * @param mode	Gibt an, welche Daten genau ausgegeben werden sollen
	 * @see Mode
	 */
	public StatisticViewerUserStatisticLineChart(final Statistics statistics, final Mode mode) {
		super();
		this.statistics=statistics;
		this.mode=mode;
	}

	/**
	 * Zeigt im Fußbereich der Hilfeseite eine "Erklärung einblenden"-Schaltfläche, die,
	 * wenn sie angeklickt wird, eine html-Hilfeseite anzeigt.
	 * @param topic	Hilfe-Thema (wird als Datei in den "description_*"-Ordern gesucht)
	 */
	private void addDescription(final String topic) {
		final URL url=StatisticViewerUserStatisticLineChart.class.getResource("description_"+Language.getCurrentLanguage()+"/"+topic+".html");
		addDescription(url,helpTopic->Help.topic(getViewer(false),helpTopic));
	}

	@Override
	protected String getDescriptionCustomStyles() {
		if (FlatLaFHelper.isDark()) return StatisticsPanel.DARK_MODE_DESACRIPTION_STYLE;
		return null;
	}

	/**
	 * Handelt es sich bei allen Einträgen zum Zeit-Werte?
	 * @return	Liefert <code>true</code>, wenn alle Einträge Zeit-Werte sind
	 */
	private boolean isAllUserStatisticsTime() {
		for (String name: statistics.userStatistics.getNames()) if (StatisticViewerOverviewText.isUserStatisticsTime(statistics,name)!=StatisticViewerOverviewText.UserStatisticsFormat.TIME) return false;
		return true;
	}

	/**
	 * Generiert ein Diagramm mit der Verteilung der Werte.
	 * @see Mode#MODE_ALL
	 */
	private void buildDefaultChart() {
		initLineChart(Language.tr("Statistics.UserStatistics"));
		if (isAllUserStatisticsTime()) {
			setupChartTimeValue(Language.tr("Statistics.UserStatistics"),Language.tr("Statistics.UserStatistics"),Language.tr("Statistics.NumberOfClients"));
		} else {
			setupChartValue(Language.tr("Statistics.UserStatistics"),Language.tr("Statistics.UserStatistics"),Language.tr("Statistics.NumberOfClients"));
		}

		int i=0;
		for (String name: statistics.userStatistics.getNames()) {
			final StatisticsDataPerformanceIndicatorWithNegativeValues indicator=(StatisticsDataPerformanceIndicatorWithNegativeValues)statistics.userStatistics.get(name);
			final DataDistributionImpl dist=indicator.getDistribution();
			if (dist!=null) addSeriesTruncated(name,COLORS[i++%COLORS.length],dist,1800);
		}

		smartZoom(1);
		initTooltips();

		/* Infotext  */
		addDescription("PlotUser");
	}

	/**
	 * Generiert ein Diagramm mit den Anzahlen pro Intervall.
	 * @see Mode#MODE_INTERVAL_COUNT
	 */
	private void buildIntervalCountChart() {
		initLineChart(Language.tr("Statistics.UserStatistics"));

		setupChartValue(Language.tr("Statistics.UserStatistics"),Language.tr("Statistic.Interval"),Language.tr("Statistics.NumberOfClients"));

		int i=0;
		for (String name: statistics.userStatisticsIntervalCount.getNames()) {
			final StatisticsDataCollector indicator=(StatisticsDataCollector)statistics.userStatisticsIntervalCount.get(name);
			addSeriesTruncated(name,COLORS[i++%COLORS.length],indicator.getValuesReadOnly(),10_000);
		}

		smartZoom(0);
		initTooltips();

		/* Infotext  */
		addDescription("PlotUserIntervalCount");
	}

	/**
	 * Generiert ein Diagramm mit den Mittelwerten pro Intervall.
	 * @see Mode#MODE_INTERVAL
	 */
	private void buildIntervalMeanChart() {
		initLineChart(Language.tr("Statistics.UserStatistics"));

		setupChartValue(Language.tr("Statistics.UserStatistics"),Language.tr("Statistic.Interval"),Language.tr("Statistic.MeanPerInterval"));

		int i=0;
		for (String name: statistics.userStatisticsIntervalMean.getNames()) {
			final StatisticsDataCollector indicator=(StatisticsDataCollector)statistics.userStatisticsIntervalMean.get(name);
			addSeriesTruncated(name,COLORS[i++%COLORS.length],indicator.getValuesReadOnly(),10_000);
		}

		smartZoom(0);
		initTooltips();

		/* Infotext  */
		addDescription("PlotUserInterval");
	}

	@Override
	protected void firstChartRequest() {
		switch(mode) {
		case MODE_ALL: buildDefaultChart(); break;
		case MODE_INTERVAL_COUNT: buildIntervalCountChart(); break;
		case MODE_INTERVAL: buildIntervalMeanChart(); break;
		}
	}
}

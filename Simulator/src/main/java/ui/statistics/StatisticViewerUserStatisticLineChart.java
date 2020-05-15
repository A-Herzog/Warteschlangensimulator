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
import statistics.StatisticsDataPerformanceIndicator;
import systemtools.statistics.StatisticViewerLineChart;
import ui.help.Help;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementUserStatistic;

/**
 * Dieser Viewer stellt die benutzerdefinierten Statistikdaten als Verteilungsdiagramm dar.
 * @see StatisticViewerLineChart
 * @see Statistics#userStatistics
 * @author Alexander Herzog
 */
public class StatisticViewerUserStatisticLineChart extends StatisticViewerLineChart {
	private final Statistics statistics;

	private final static Color[] COLORS=new Color[]{Color.RED,Color.BLUE,Color.GREEN,Color.BLACK};

	/**
	 * Konstruktor der Klasse
	 * @param statistics	Statistikobjekt, aus dem die anzuzeigenden Daten entnommen werden sollen
	 */
	public StatisticViewerUserStatisticLineChart(final Statistics statistics) {
		super();
		this.statistics=statistics;
	}

	private void addDescription(final String topic) {
		final URL url=StatisticViewerUserStatisticLineChart.class.getResource("description_"+Language.getCurrentLanguage()+"/"+topic+".html");
		addDescription(url,helpTopic->Help.topic(getViewer(false),helpTopic));
	}

	private boolean isUserStatisticsTime(final String key) {
		for (ModelElement element: statistics.editModel.surface.getElements()) {
			if (element instanceof ModelElementUserStatistic) {
				final Boolean B=((ModelElementUserStatistic)element).getIsTimeForKey(key);
				if (B!=null) return B.booleanValue();
			}
			if (element instanceof ModelElementUserStatistic) {
				for (ModelElement sub: ((ModelElementUserStatistic)element).getSurface().getElements()) {
					if (sub instanceof ModelElementUserStatistic) {
						final Boolean B=((ModelElementUserStatistic)sub).getIsTimeForKey(key);
						if (B!=null) return B.booleanValue();
					}
				}
			}
		}

		return true;
	}

	private boolean isAllUserStatisticsTime() {
		for (String name: statistics.userStatistics.getNames()) if (!isUserStatisticsTime(name)) return false;
		return true;
	}

	@Override
	protected void firstChartRequest() {
		initLineChart(Language.tr("Statistics.UserStatistics"));
		if (isAllUserStatisticsTime()) {
			setupChartTimeValue(Language.tr("Statistics.UserStatistics"),Language.tr("Statistics.UserStatistics"),Language.tr("Statistics.NumberOfClients"));
		} else {
			setupChartValue(Language.tr("Statistics.UserStatistics"),Language.tr("Statistics.UserStatistics"),Language.tr("Statistics.NumberOfClients"));
		}

		int i=0;
		for (String name: statistics.userStatistics.getNames()) {
			final StatisticsDataPerformanceIndicator indicator=(StatisticsDataPerformanceIndicator)statistics.userStatistics.get(name);
			final DataDistributionImpl dist=indicator.getDistribution();
			if (dist!=null) addSeriesTruncated(name,COLORS[i++%COLORS.length],dist,1800);
		}

		smartZoom(1);
		initTooltips();

		/* Infotext  */
		addDescription("PlotUser");
	}
}

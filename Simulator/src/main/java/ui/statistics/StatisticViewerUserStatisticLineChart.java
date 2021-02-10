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
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.swing.SwingUtilities;

import language.Language;
import mathtools.distribution.DataDistributionImpl;
import simulator.statistics.Statistics;
import statistics.StatisticsDataPerformanceIndicator;
import systemtools.statistics.StatisticViewerLineChart;
import ui.MainFrame;
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
	/** Statistikobjekt, aus dem die anzuzeigenden Daten entnommen werden sollen */
	private final Statistics statistics;

	/** Farben für die Diagrammlinien */
	private static final Color[] COLORS=new Color[]{Color.RED,Color.BLUE,Color.GREEN,Color.BLACK};

	/**
	 * Konstruktor der Klasse
	 * @param statistics	Statistikobjekt, aus dem die anzuzeigenden Daten entnommen werden sollen
	 */
	public StatisticViewerUserStatisticLineChart(final Statistics statistics) {
		super();
		this.statistics=statistics;
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

	/**
	 * Handelt es sich bei einem Eintrag um Zeit-Werte?
	 * @param key	Bezeichner des Eintrags
	 * @return	Zeit-Werte (<code>true</code>) oder Zustände (<code>false</code>)
	 */
	private boolean isUserStatisticsTime(final String key) {
		for (ModelElement element: statistics.editModel.surface.getElements()) {
			if (element instanceof ModelElementUserStatistic) {
				final ModelElementUserStatistic.IsTime B=((ModelElementUserStatistic)element).getIsTimeForKey(key);
				if (B!=ModelElementUserStatistic.IsTime.NOT_FOUND) return B.bool;
			}
			if (element instanceof ModelElementUserStatistic) {
				for (ModelElement sub: ((ModelElementUserStatistic)element).getSurface().getElements()) {
					if (sub instanceof ModelElementUserStatistic) {
						final ModelElementUserStatistic.IsTime B=((ModelElementUserStatistic)sub).getIsTimeForKey(key);
						if (B!=ModelElementUserStatistic.IsTime.NOT_FOUND) return B.bool;
					}
				}
			}
		}

		return true;
	}

	/**
	 * Handelt es sich bei allen Einträgen zum Zeit-Werte?
	 * @return	Liefert <code>true</code>, wenn alle Einträge Zeit-Werte sind
	 */
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

	@Override
	protected void openExternalFile(final File file) throws IOException {
		final Window window=SwingUtilities.getWindowAncestor(getViewer(false));
		if (window instanceof MainFrame) ((MainFrame)window).pauseFocusFixer(5);
		super.openExternalFile(file);
	}
}

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
package ui.statistics;

import java.awt.Color;
import java.net.URL;

import language.Language;
import simulator.statistics.Statistics;
import statistics.StatisticsTimePerformanceIndicator;
import systemtools.statistics.StatisticViewerPieChartMulti;
import ui.help.Help;
import ui.modeleditor.ModelResource;

/**
 * Dieser Viewer gibt Werte in Form von mehreren kombinierten Tortendiagrammen aus.
 * @see StatisticViewerPieChartMulti
 * @author Alexander Herzog
 */
public class StatisticViewerMultiPieChart extends StatisticViewerPieChartMulti {
	/** Statistikobjekt, aus dem die anzuzeigenden Daten entnommen werden sollen */
	private final Statistics statistics;
	/** Gibt an, welche Daten genau ausgegeben werden sollen */
	private final Mode mode;

	/**
	 * Wählt die von {@link StatisticViewerMultiPieChart} auszugebende Information aus.
	 * @author Alexander Herzog
	 * @see StatisticViewerMultiPieChart#StatisticViewerMultiPieChart(Statistics, Mode)
	 */
	public enum Mode {
		/** Tortendiagramm zum Vergleich der Auslastungen der Bedienergruppen  */
		MODE_RESOURCE_UTILIZATION
	}

	/**
	 * Konstruktor der Klasse
	 * @param statistics	Statistikobjekt, aus dem die anzuzeigenden Daten entnommen werden sollen
	 * @param mode	Gibt an, welche Daten genau ausgegeben werden sollen
	 * @see Mode
	 */
	public StatisticViewerMultiPieChart(final Statistics statistics, final Mode mode) {
		this.statistics=statistics;
		this.mode=mode;
	}

	/**
	 * Zeigt im Fußbereich der Hilfeseite eine "Erklärung einblenden"-Schaltfläche, die,
	 * wenn sie angeklickt wird, eine html-Hilfeseite anzeigt.
	 * @param topic	Hilfe-Thema (wird als Datei in den "description_*"-Ordern gesucht)
	 */
	private void addDescription(final String topic) {
		final URL url=StatisticViewerTimeBarChart.class.getResource("description_"+Language.getCurrentLanguage()+"/"+topic+".html");
		addDescription(url,helpTopic->Help.topic(getViewer(false),helpTopic));
	}

	/**
	 * Tortendiagramm zum Vergleich der Auslastungen der Bedienergruppen
	 * @see Mode#MODE_RESOURCE_UTILIZATION
	 */
	private void resourceUtilizationChartRequest() {
		initPieChart(Language.tr("Statistics.UtilizationAndFailures"));

		final String[] names=statistics.resourceUtilization.getNames();
		final StatisticsTimePerformanceIndicator[] indicators1=statistics.resourceUtilization.getAll(StatisticsTimePerformanceIndicator.class);
		final StatisticsTimePerformanceIndicator[] indicators2=statistics.resourceInDownTime.getAll(StatisticsTimePerformanceIndicator.class);

		for (int i=0;i<names.length;i++) {
			final double part1=(i<indicators1.length && indicators1[i]!=null)?indicators1[i].getTimeMean():0;
			final double part2=(i<indicators2.length && indicators2[i]!=null)?indicators2[i].getTimeMean():0;
			addPieSegment(names[i],Language.tr("Statistics.UtilizationAndFailures.Utilization"),part1,Color.BLUE);
			if (part2>0) addPieSegment(names[i],Language.tr("Statistics.UtilizationAndFailures.Failure"),part2,Color.RED);
			final ModelResource resource=statistics.editModel.resources.get(names[i]);
			if (resource!=null && resource.getMode()==ModelResource.Mode.MODE_NUMBER && resource.getCount()>0) {
				final double part3=resource.getCount()-part1-part2;
				addPieSegment(names[i],Language.tr("Statistics.UtilizationAndFailures.Idle"),part3,Color.LIGHT_GRAY);
			}
		}
	}

	@Override
	protected void firstChartRequest() {
		switch (mode) {
		case MODE_RESOURCE_UTILIZATION:
			resourceUtilizationChartRequest();
			addDescription("PlotBarCompareUtilizationOperators");
			break;
		}
	}
}

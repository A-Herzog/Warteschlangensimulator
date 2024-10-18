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

import org.apache.commons.math3.util.FastMath;

import language.Language;
import simulator.statistics.Statistics;
import statistics.StatisticsDataPerformanceIndicator;
import systemtools.statistics.StatisticViewerPieChart;
import ui.help.Help;
import ui.tools.FlatLaFHelper;

/**
 * Dieser Viewer gibt das Verhältnis von Warte- zu Bedienzeit in Form eines Tortendiagramms aus.
 * @see StatisticViewerPieChart
 * @author Alexander Herzog
 */
public class StatisticViewerPartsPieChart extends StatisticViewerPieChart {
	/** Statistikobjekt, aus dem die anzuzeigenden Daten entnommen werden sollen */
	private final Statistics statistics;
	/** Gibt an, welche Daten genau ausgegeben werden sollen */
	private final Mode mode;

	/**
	 * Wählt die von {@link StatisticViewerPartsPieChart} auszugebende Information aus.
	 * @author Alexander Herzog
	 * @see StatisticViewerPartsPieChart#StatisticViewerPartsPieChart(Statistics, Mode)
	 */
	public enum Mode {
		/** Ausgabe von Warte-, Transfer- und Bedienzeit über alle Kundengruppen */
		MODE_WAITINGPROCESSING
	}

	/**
	 * Konstruktor der Klasse
	 * @param statistics	Statistikobjekt, aus dem die anzuzeigenden Daten entnommen werden sollen
	 * @param mode	Gibt an, welche Daten genau ausgegeben werden sollen
	 * @see Mode
	 */
	public StatisticViewerPartsPieChart(final Statistics statistics, final Mode mode) {
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
		final URL url=StatisticViewerPartsPieChart.class.getResource("description_"+Language.getCurrentLanguage()+"/"+topic+".html");
		addDescription(url,helpTopic->Help.topic(getViewer(false),helpTopic));
	}

	@Override
	protected String getDescriptionCustomStyles() {
		if (FlatLaFHelper.isDark()) return StatisticsPanel.DARK_MODE_DESACRIPTION_STYLE;
		return null;
	}

	/**
	 * Ausgabe von
	 * Ausgabe von Warte-, Transfer- und Bedienzeit über alle Kundengruppen
	 * @see Mode#MODE_WAITINGPROCESSING
	 */
	private void requestClientsChart() {
		initPieChart(Language.tr("Statistics.DistributionOfTimes"));

		double sumWaiting=0;
		for (StatisticsDataPerformanceIndicator indicator: (StatisticsDataPerformanceIndicator[])statistics.clientsWaitingTimes.getAll(StatisticsDataPerformanceIndicator.class)) sumWaiting+=indicator.getSum();
		double sumTransfer=0;
		for (StatisticsDataPerformanceIndicator indicator: (StatisticsDataPerformanceIndicator[])statistics.clientsTransferTimes.getAll(StatisticsDataPerformanceIndicator.class)) sumTransfer+=indicator.getSum();
		double sumProcessing=0;
		for (StatisticsDataPerformanceIndicator indicator: (StatisticsDataPerformanceIndicator[])statistics.clientsProcessingTimes.getAll(StatisticsDataPerformanceIndicator.class)) sumProcessing+=indicator.getSum();
		double sum=FastMath.max(0.0001,sumWaiting+sumTransfer+sumProcessing);

		if (sumWaiting>0) addPieSegment(Language.tr("Statistics.WaitingTime")+" ("+StatisticTools.formatPercentExt(sumWaiting/sum,false)+")",sumWaiting/sum,Color.RED);
		if (sumTransfer>0) addPieSegment(Language.tr("Statistics.TransferTime")+" ("+StatisticTools.formatPercentExt(sumTransfer/sum,false)+")",sumTransfer/sum,Color.BLUE);
		if (sumProcessing>0) addPieSegment(Language.tr("Statistics.ProcessTime")+" ("+StatisticTools.formatPercentExt(sumProcessing/sum,false)+")",sumProcessing/sum,Color.GREEN);

		/* Infotext  */
		addDescription("PlotPieWaitingProcessing");
	}

	@Override
	protected void firstChartRequest() {
		switch (mode) {
		case MODE_WAITINGPROCESSING:
			requestClientsChart();
			break;
		}
	}
}
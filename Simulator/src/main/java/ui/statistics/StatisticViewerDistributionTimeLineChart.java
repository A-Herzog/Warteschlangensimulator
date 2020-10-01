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
import java.util.Map;

import language.Language;
import mathtools.distribution.DataDistributionImpl;
import simulator.statistics.Statistics;
import statistics.StatisticsDataCollector;
import statistics.StatisticsDataPerformanceIndicator;
import statistics.StatisticsDataPerformanceIndicatorWithNegativeValues;
import statistics.StatisticsLongRunPerformanceIndicator;
import statistics.StatisticsMultiPerformanceIndicator;
import statistics.StatisticsPerformanceIndicator;
import statistics.StatisticsTimePerformanceIndicator;
import systemtools.statistics.StatisticViewerLineChart;
import ui.help.Help;

/**
 * Dieser Viewer gibt die Verteilung der Zwischenankunfts-, Warte- und Bedienzeiten in Form eines Liniendiagramms zu den Simulationsergebnissen aus.
 * @see StatisticViewerLineChart
 * @author Alexander Herzog
 */
public class StatisticViewerDistributionTimeLineChart extends StatisticViewerLineChart {
	private final Statistics statistics;
	private final Mode mode;
	private final String data;

	private static final Color[] COLORS=new Color[]{Color.RED,Color.BLUE,Color.GREEN,Color.BLACK};

	/**
	 * Wählt die von {@link StatisticViewerDistributionTimeLineChart} auszugebende Information aus.
	 * @author Alexander Herzog
	 * @see StatisticViewerDistributionTimeLineChart#StatisticViewerDistributionTimeLineChart(Statistics, Mode)
	 */
	public enum Mode {
		/** Verteilungsdiagramm der Zwischenankunftszeiten der Kunden an den "Quelle"-Stationen */
		MODE_INTERARRIVAL_CLIENTS,
		/** Verteilungsdiagramm der Zwischenankunftszeiten der Kunden an den einzelnen Stationen */
		MODE_INTERARRIVAL_STATION,
		/** Verteilungsdiagramm der Zwischenankunftszeiten der Kunden an den einzelnen Stationen nach Kundentypen weiter ausdifferenziert */
		MODE_INTERARRIVAL_STATION_CLIENTS,
		/** Verteilungsdiagramm der Zwischenankunftszeiten der Kunden an den einzelnen Stationen nach Warteschlangenlänge weiter ausdifferenziert */
		MODE_INTERARRIVAL_STATION_STATES,
		/** Verteilungsdiagramm der Zwischenabgangszeiten der Kunden aus dem System */
		MODE_INTERLEAVE_CLIENTS,
		/** Verteilungsdiagramm der Zwischenabgangszeiten der Kunden bei den einzelnen Stationen */
		MODE_INTERLEAVE_STATION,
		/** Verteilungsdiagramm der Zwischenabgangszeiten der Kunden bei den einzelnen Stationen nach Kundentypen weiter ausdifferenziert */
		MODE_INTERLEAVE_STATION_CLIENTS,
		/** Verteilungsdiagramm der Wartezeiten der Kunden */
		MODE_WAITING_CLIENTS,
		/** Verteilungsdiagramm der Transportzeiten der Kunden */
		MODE_TRANSFER_CLIENTS,
		/** Verteilungsdiagramm der Bedienzeiten der Kunden */
		MODE_PROCESSING_CLIENTS,
		/** Verteilungsdiagramm der Verweilzeiten der Kunden */
		MODE_RESIDENCE_CLIENTS,
		/** Verteilungsdiagramm der an den Stationen aufgetretenen Wartezeiten */
		MODE_WAITING_STATION,
		/** Verteilungsdiagramm der an den Stationen aufgetretenen Transportzeiten */
		MODE_TRANSFER_STATION,
		/** Verteilungsdiagramm der an den Stationen aufgetretenen Bedienzeiten */
		MODE_PROCESSING_STATION,
		/** Verteilungsdiagramm der an den Stationen aufgetretenen Verweilzeiten */
		MODE_RESIDENCE_STATION,
		/** Verteilungsdiagramm der an den Stationen aufgetretenen Wartezeiten (zusätzlich ausdifferenziert nach Kundentypen) */
		MODE_WAITING_STATION_CLIENT,
		/** Verteilungsdiagramm der an den Stationen aufgetretenen Transportzeiten (zusätzlich ausdifferenziert nach Kundentypen) */
		MODE_TRANSFER_STATION_CLIENT,
		/** Verteilungsdiagramm der an den Stationen aufgetretenen Bedienzeiten (zusätzlich ausdifferenziert nach Kundentypen) */
		MODE_PROCESSING_STATION_CLIENT,
		/** Verteilungsdiagramm der an den Stationen aufgetretenen Verweilzeiten (zusätzlich ausdifferenziert nach Kundentypen) */
		MODE_RESIDENCE_STATION_CLIENT,
		/** Verteilungsdiagramm der Anzahl an Kunden an den Stationen */
		MODE_NUMBER_STATION,
		/** Verteilungsdiagramm der Anzahl an Kunden an den Stationen nach Kundentypen */
		MODE_NUMBER_STATION_CLIENT_TYPES,
		/** Verteilungsdiagramm der Anzahl an Kunden im System nach Kundentypen */
		MODE_NUMBER_CLIENT,
		/** Verteilungsdiagramm der Anzahl an Kunden an den Stationswarteschlangen */
		MODE_QUEUE,
		/** Verteilungsdiagramm der Anzahl an Kunden an den Stationswarteschlangen nach Kundentypen */
		MODE_QUEUE_CLIENT_TYPE,
		/** Verteilungsdiagramm der Werte der Laufzeitstatistik */
		MODE_ADDITIONAL_STATISTICS,
		/** Verteilungsdiagramm mit den Werten der Kundendatenfelder */
		MODE_CLIENT_DATA_DISTRIBUTION,
		/** Verteilungsdiagramme der an den Datenaufzeichnung-Stationen erfassten Werten */
		MODE_VALUE_RECORDING,
	}

	/**
	 * Konstruktor der Klasse
	 * @param statistics	Statistikobjekt, aus dem die anzuzeigenden Daten entnommen werden sollen
	 * @param mode	Gibt an, welche Daten genau ausgegeben werden sollen
	 * @see Mode
	 */
	public StatisticViewerDistributionTimeLineChart(final Statistics statistics, final Mode mode) {
		super();
		this.statistics=statistics;
		this.mode=mode;
		this.data=null;
	}

	/**
	 * Konstruktor der Klasse
	 * @param statistics	Statistikobjekt, aus dem die anzuzeigenden Daten entnommen werden sollen
	 * @param mode	Gibt an, welche Daten genau ausgegeben werden sollen
	 * @param data	Wird als Modus {@link Mode#MODE_VALUE_RECORDING} verwendet, so kann hier der Name der Datenaufzeichnung-Station, deren Daten ausgegeben werden sollen, angegeben werden.
	 * @see Mode
	 */
	public StatisticViewerDistributionTimeLineChart(final Statistics statistics, final Mode mode, final String data) {
		super();
		this.statistics=statistics;
		this.mode=mode;
		this.data=data;
	}

	private void addDescription(final String topic) {
		final URL url=StatisticViewerDistributionTimeLineChart.class.getResource("description_"+Language.getCurrentLanguage()+"/"+topic+".html");
		addDescription(url,helpTopic->Help.topic(getViewer(false),helpTopic));
	}

	private void requestDiagrammTimeDistribution(final String title, StatisticsMultiPerformanceIndicator indicator, final String xLabel, final Map<String,Color> colorMap) {
		initLineChart(title);
		setupChartTimePercent(title,xLabel,Language.tr("Statistics.Part"));

		final String[] names=indicator.getNames();
		final StatisticsDataPerformanceIndicator[] indicators=indicator.getAll(StatisticsDataPerformanceIndicator.class);

		for (int i=0;i<names.length;i++) {
			Color color=null;
			if (colorMap!=null) color=colorMap.get(names[i]);
			if (color==null) color=COLORS[i%COLORS.length];

			final DataDistributionImpl dist=indicators[i].getNormalizedDistribution();
			if (dist!=null) addSeriesTruncated(title+" - "+names[i],color,dist,1800);
		}

		smartZoom(1);
	}

	private boolean isDistNull(final DataDistributionImpl dist) {
		if (dist==null) return true;
		if (dist.densityData==null || dist.densityData.length<2) return true;
		for (int i=1;i<dist.densityData.length;i++) if (dist.densityData[i]>0) return false;
		return true;
	}

	private void requestDiagrammStateDistribution(final String title, StatisticsMultiPerformanceIndicator indicator, final StatisticsTimePerformanceIndicator system, final String xLabel, final Map<String,Color> colorMap) {
		initLineChart(title);
		setupChartValuePercent(title,xLabel,Language.tr("Statistics.Part"));

		final String[] names=indicator.getNames();
		final StatisticsPerformanceIndicator[] indicators=indicator.getAll();

		DataDistributionImpl dist;

		if (system!=null) {
			dist=system.getNormalizedDistribution();
			addSeries(Language.tr("Statistics.System"),Color.BLACK,dist);
		}

		int colorIndex=0;
		for (int i=0;i<names.length;i++) {
			Color color=null;
			if (colorMap!=null) color=colorMap.get(names[i]);
			if (color==null) color=COLORS[colorIndex%COLORS.length];
			if (indicators[i] instanceof StatisticsTimePerformanceIndicator) {
				dist=((StatisticsTimePerformanceIndicator)indicators[i]).getNormalizedDistribution();
				if (isDistNull(dist)) continue;
				colorIndex++;
				addSeries(names[i],color,dist);
			}
			if (indicators[i] instanceof StatisticsDataPerformanceIndicator) {
				dist=((StatisticsDataPerformanceIndicator)indicators[i]).getNormalizedDistribution();
				if (dist!=null) {
					if (isDistNull(dist)) continue;
					colorIndex++;
					addSeries(names[i],color,dist);
				}
			}
			if (indicators[i] instanceof StatisticsDataPerformanceIndicatorWithNegativeValues) {
				dist=((StatisticsDataPerformanceIndicatorWithNegativeValues)indicators[i]).getNormalizedDistribution();
				if (dist!=null) {
					if (isDistNull(dist)) continue;
					colorIndex++;
					addSeries(names[i],color,dist);
				}
			}
		}

		smartZoom(1);
	}

	private void requestDiagrammSpecialDistribution(final String title, StatisticsMultiPerformanceIndicator indicator) {
		initLineChart(title);
		setupChartTimeValue(title,Language.tr("Statistic.Viewer.Chart.Time"),Language.tr("Statistic.Viewer.Chart.Value"));

		final String[] names=indicator.getNames();
		final StatisticsLongRunPerformanceIndicator[] indicators=indicator.getAll(StatisticsLongRunPerformanceIndicator.class);

		for (int i=0;i<names.length;i++) {
			Color color=COLORS[i%COLORS.length];
			final DataDistributionImpl dist=indicators[i].getDistribution();
			addSeries(names[i],color,dist);
		}

		smartZoom(1);
	}

	private void requestDiagrammDataCollection(final String title, final String name, final double[] values, final int count) {
		initLineChart(title);
		setupChartTimeValue(title,Language.tr("Statistic.Viewer.Chart.Time"),Language.tr("Statistic.Viewer.Chart.Value"));

		addSeries(name,Color.BLUE,values,count);

		smartZoom(1);
	}

	@Override
	protected void firstChartRequest() {
		Map<String,Color> colorMap;

		switch (mode) {
		case MODE_INTERARRIVAL_CLIENTS:
			requestDiagrammTimeDistribution(Language.tr("Statistics.DistributionOfTheInterArrivalTimes"),statistics.clientsInterarrivalTime,Language.tr("Statistics.Distance"),null);
			addDescription("PlotTimeDistribution");
			break;
		case MODE_INTERARRIVAL_STATION:
			requestDiagrammTimeDistribution(Language.tr("Statistics.DistributionOfTheInterArrivalTimes"),statistics.stationsInterarrivalTime,Language.tr("Statistics.Distance"),null);
			addDescription("PlotTimeDistribution");
			break;
		case MODE_INTERARRIVAL_STATION_CLIENTS:
			requestDiagrammTimeDistribution(Language.tr("Statistics.DistributionOfTheInterArrivalTimes"),statistics.stationsInterarrivalTimeByClientType,Language.tr("Statistics.Distance"),null);
			addDescription("PlotTimeDistribution");
			break;
		case MODE_INTERARRIVAL_STATION_STATES:
			requestDiagrammTimeDistribution(Language.tr("Statistics.DistributionOfTheInterArrivalTimes"),statistics.stationsInterarrivalTimeByState,Language.tr("Statistics.Distance"),null);
			addDescription("PlotTimeDistribution");
			break;
		case MODE_INTERLEAVE_CLIENTS:
			requestDiagrammTimeDistribution(Language.tr("Statistics.DistributionOfTheInterLeaveTimes"),statistics.clientsInterleavingTime,Language.tr("Statistics.Distance"),null);
			addDescription("PlotTimeDistribution");
			break;
		case MODE_INTERLEAVE_STATION:
			requestDiagrammTimeDistribution(Language.tr("Statistics.DistributionOfTheInterLeaveTimes"),statistics.stationsInterleavingTime,Language.tr("Statistics.Distance"),null);
			addDescription("PlotTimeDistribution");
			break;
		case MODE_INTERLEAVE_STATION_CLIENTS:
			requestDiagrammTimeDistribution(Language.tr("Statistics.DistributionOfTheInterLeaveTimes"),statistics.stationsInterleavingTimeByClientType,Language.tr("Statistics.Distance"),null);
			addDescription("PlotTimeDistribution");
			break;
		case MODE_WAITING_CLIENTS:
			colorMap=statistics.editModel.clientData.getStatisticColors(statistics.editModel.surface.getClientTypes());
			requestDiagrammTimeDistribution(Language.tr("Statistics.DistributionOfTheWaitingTimes"),statistics.clientsWaitingTimes,Language.tr("Statistics.WaitingTime"),colorMap);
			addDescription("PlotTimeDistribution");
			break;
		case MODE_TRANSFER_CLIENTS:
			colorMap=statistics.editModel.clientData.getStatisticColors(statistics.editModel.surface.getClientTypes());
			requestDiagrammTimeDistribution(Language.tr("Statistics.DistributionOfTheTransferTimes"),statistics.clientsTransferTimes,Language.tr("Statistics.TransferTime"),colorMap);
			addDescription("PlotTimeDistribution");
			break;
		case MODE_PROCESSING_CLIENTS:
			colorMap=statistics.editModel.clientData.getStatisticColors(statistics.editModel.surface.getClientTypes());
			requestDiagrammTimeDistribution(Language.tr("Statistics.DistributionOfTheProcessTimes"),statistics.clientsProcessingTimes,Language.tr("Statistics.ProcessTime"),colorMap);
			addDescription("PlotTimeDistribution");
			break;
		case MODE_RESIDENCE_CLIENTS:
			colorMap=statistics.editModel.clientData.getStatisticColors(statistics.editModel.surface.getClientTypes());
			requestDiagrammTimeDistribution(Language.tr("Statistics.DistributionOfTheResidenceTimes"),statistics.clientsResidenceTimes,Language.tr("Statistics.ResidenceTime"),colorMap);
			addDescription("PlotTimeDistribution");
			break;
		case MODE_WAITING_STATION:
			requestDiagrammTimeDistribution(Language.tr("Statistics.DistributionOfTheWaitingTimes"),statistics.stationsWaitingTimes,Language.tr("Statistics.WaitingTime"),null);
			addDescription("PlotTimeDistribution");
			break;
		case MODE_TRANSFER_STATION:
			requestDiagrammTimeDistribution(Language.tr("Statistics.DistributionOfTheTransferTimes"),statistics.stationsTransferTimes,Language.tr("Statistics.TransferTime"),null);
			addDescription("PlotTimeDistribution");
			break;
		case MODE_PROCESSING_STATION:
			requestDiagrammTimeDistribution(Language.tr("Statistics.DistributionOfTheProcessTimes"),statistics.stationsProcessingTimes,Language.tr("Statistics.ProcessTime"),null);
			addDescription("PlotTimeDistribution");
			break;
		case MODE_RESIDENCE_STATION:
			requestDiagrammTimeDistribution(Language.tr("Statistics.DistributionOfTheResidenceTimes"),statistics.stationsResidenceTimes,Language.tr("Statistics.ResidenceTime"),null);
			addDescription("PlotTimeDistribution");
			break;
		case MODE_WAITING_STATION_CLIENT:
			requestDiagrammTimeDistribution(Language.tr("Statistics.DistributionOfTheWaitingTimes"),statistics.stationsWaitingTimesByClientType,Language.tr("Statistics.WaitingTime"),null);
			addDescription("PlotTimeDistribution");
			break;
		case MODE_TRANSFER_STATION_CLIENT:
			requestDiagrammTimeDistribution(Language.tr("Statistics.DistributionOfTheTransferTimes"),statistics.stationsTransferTimesByClientType,Language.tr("Statistics.TransferTime"),null);
			addDescription("PlotTimeDistribution");
			break;
		case MODE_PROCESSING_STATION_CLIENT:
			requestDiagrammTimeDistribution(Language.tr("Statistics.DistributionOfTheProcessTimes"),statistics.stationsProcessingTimesByClientType,Language.tr("Statistics.ProcessTime"),null);
			addDescription("PlotTimeDistribution");
			break;
		case MODE_RESIDENCE_STATION_CLIENT:
			requestDiagrammTimeDistribution(Language.tr("Statistics.DistributionOfTheResidenceTimes"),statistics.stationsResidenceTimesByClientType,Language.tr("Statistics.ResidenceTime"),null);
			addDescription("PlotTimeDistribution");
			break;
		case MODE_NUMBER_STATION:
			requestDiagrammStateDistribution(Language.tr("Statistics.DistributionOfNumberOfClientsAtStations")+" ("+Language.tr("Statistics.total")+")",statistics.clientsAtStationByStation,statistics.clientsInSystem,Language.tr("Statistics.ClientsAtStation"),null);
			addDescription("PlotCountDistribution");
			break;
		case MODE_NUMBER_STATION_CLIENT_TYPES:
			requestDiagrammStateDistribution(Language.tr("Statistics.DistributionOfNumberOfClientsAtStationsByClientTypes")+" ("+Language.tr("Statistics.total")+")",statistics.clientsAtStationByStationAndClient,null,Language.tr("Statistics.ClientsAtStation"),null);
			addDescription("PlotCountDistribution");
			break;
		case MODE_NUMBER_CLIENT:
			requestDiagrammStateDistribution(Language.tr("Statistics.DistributionOfNumberOfClientsByType")+" ("+Language.tr("Statistics.total")+")",statistics.clientsInSystemByClient,statistics.clientsInSystem,Language.tr("Statistics.ClientsByType"),null);
			addDescription("PlotCountDistribution");
			break;
		case MODE_QUEUE:
			requestDiagrammStateDistribution(Language.tr("Statistics.DistributionOfNumberOfClientsAtStationQueues"),statistics.clientsAtStationQueueByStation,statistics.clientsInSystemQueues,Language.tr("Statistics.ClientsInQueue"),null);
			addDescription("PlotCountDistribution");
			break;
		case MODE_QUEUE_CLIENT_TYPE:
			requestDiagrammStateDistribution(Language.tr("Statistics.DistributionOfNumberOfClientsAtStationQueuesByClientTypes"),statistics.clientsAtStationQueueByStationAndClient,null,Language.tr("Statistics.ClientsInQueue"),null);
			addDescription("PlotCountDistribution");
			break;
		case MODE_ADDITIONAL_STATISTICS:
			requestDiagrammSpecialDistribution(Language.tr("Statistics.AdditionalStatistics"),statistics.longRunStatistics);
			addDescription("PlotAdditionalStatistics");
			break;
		case MODE_CLIENT_DATA_DISTRIBUTION:
			requestDiagrammStateDistribution(Language.tr("Statistics.ClientData.Distribution")+" ("+Language.tr("Statistics.total")+")",statistics.clientData,null,Language.tr("Statistics.Value"),null);
			plot.getDomainAxis().setLabel(Language.tr("Statistics.Value"));
			addDescription("PlotClientDataDistribution");
			break;
		case MODE_VALUE_RECORDING:
			final StatisticsDataCollector collector=((StatisticsDataCollector)statistics.valueRecording.get(data+"-1"));
			requestDiagrammDataCollection(data,data,collector.getValuesReadOnly(),collector.getCount());
			plot.getDomainAxis().setLabel(Language.tr("Statistics.RecordedValue"));
			addDescription("PlotX");
			break;
		}

		initTooltips();
	}
}
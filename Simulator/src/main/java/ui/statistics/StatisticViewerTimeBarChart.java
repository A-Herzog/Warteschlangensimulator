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
import simulator.statistics.Statistics;
import statistics.StatisticsDataPerformanceIndicator;
import statistics.StatisticsMultiPerformanceIndicator;
import statistics.StatisticsTimePerformanceIndicator;
import systemtools.statistics.StatisticViewerBarChart;
import ui.help.Help;
import ui.modeleditor.ModelResource;
import ui.modeleditor.ModelTransporter;

/**
 * Dieser Viewer gibt die Verteilung der Zwischenankunfts-, Warte- und Bedienzeiten in Form eines Balkendiagramms zu den Simulationsergebnissen aus.
 * @see StatisticViewerBarChart
 * @author Alexander Herzog
 */
public class StatisticViewerTimeBarChart extends StatisticViewerBarChart {
	/** Statistikobjekt, aus dem die anzuzeigenden Daten entnommen werden sollen */
	private final Statistics statistics;
	/** Gibt an, welche Daten genau ausgegeben werden sollen */
	private final Mode mode;

	/** Farben für die Diagrammlinien */
	private static final Color[] COLORS=new Color[]{Color.RED,Color.BLUE,Color.GREEN,Color.BLACK};

	/**
	 * Wählt die von {@link StatisticViewerTimeBarChart} auszugebende Information aus.
	 * @author Alexander Herzog
	 * @see StatisticViewerTimeBarChart#StatisticViewerTimeBarChart(Statistics, Mode)
	 */
	public enum Mode {
		/** Balkendiagramm zum Vergleich der mittleren Zwischenankunftszeiten zwischen den Kundentypen */
		MODE_INTERARRIVAL_CLIENTS,
		/** Balkendiagramm zum Vergleich der mittleren Zwischenankunftszeiten zwischen den Stationen */
		MODE_INTERARRIVAL_STATION,
		/** Balkendiagramm zum Vergleich der mittleren Zwischenankunftszeiten zwischen den Stationen nach Kundentypen weiter ausdifferenziert */
		MODE_INTERARRIVAL_STATION_CLIENTS,
		/** Balkendiagramm zum Vergleich der mittleren Zwischenankunftszeiten zwischen den Stationen nach Warteschlangenlänge weiter ausdifferenziert */
		MODE_INTERARRIVAL_STATION_STATES,
		/** Balkendiagramm zum Vergleich der mittleren Zwischenabgangszeiten zwischen den Kundentypen */
		MODE_INTERLEAVE_CLIENTS,
		/** Balkendiagramm zum Vergleich der mittleren Zwischenabgangszeiten zwischen den Stationen */
		MODE_INTERLEAVE_STATION,
		/** Balkendiagramm zum Vergleich der mittleren Zwischenabgangszeiten zwischen den Stationen nach Kundentypen weiter ausdifferenziert */
		MODE_INTERLEAVE_STATION_CLIENTS,
		/** Balkendiagramm zum Vergleich der mittleren Wartezeiten zwischen den Kundentypen */
		MODE_WAITING_CLIENTS,
		/** Balkendiagramm zum Vergleich der mittleren Transportzeiten zwischen den Kundentypen */
		MODE_TRANSFER_CLIENTS,
		/** Balkendiagramm zum Vergleich der mittleren Bedienzeiten zwischen den Kundentypen */
		MODE_PROCESSING_CLIENTS,
		/** Balkendiagramm zum Vergleich der mittleren Verweilzeiten zwischen den Kundentypen */
		MODE_RESIDENCE_CLIENTS,
		/** Balkendiagramm zum Vergleich der Flussgrade zwischen den Kundentypen */
		MODE_FLOW_FACTOR_CLIENTS,
		/** Balkendiagramm zum Vergleich der mittleren Wartezeiten zwischen den Stationen */
		MODE_WAITING_STATION,
		/** Balkendiagramm zum Vergleich der mittleren Transportzeiten zwischen den Stationen */
		MODE_TRANSFER_STATION,
		/** Balkendiagramm zum Vergleich der mittleren Bedienzeiten zwischen den Stationen */
		MODE_PROCESSING_STATION,
		/** Balkendiagramm zum Vergleich der mittleren Verweilzeiten zwischen den Stationen */
		MODE_RESIDENCE_STATION,
		/** Balkendiagramm zum Vergleich der Flussgrade zwischen den Stationen */
		MODE_FLOW_FACTOR_STATION,
		/** Balkendiagramm zum Vergleich der mittleren Wartezeiten zwischen den Stationen (zusätzlich ausdifferenziert nach Kundentypen) */
		MODE_WAITING_STATION_CLIENT,
		/** Balkendiagramm zum Vergleich der mittleren Transportzeiten zwischen den Stationen (zusätzlich ausdifferenziert nach Kundentypen) */
		MODE_TRANSFER_STATION_CLIENT,
		/** Balkendiagramm zum Vergleich der mittleren Bedienzeiten zwischen den Stationen (zusätzlich ausdifferenziert nach Kundentypen) */
		MODE_PROCESSING_STATION_CLIENT,
		/** Balkendiagramm zum Vergleich der mittleren Verweilzeiten zwischen den Stationen (zusätzlich ausdifferenziert nach Kundentypen) */
		MODE_RESIDENCE_STATION_CLIENT,
		/** Balkendiagramm zum Vergleich der Flussgrade zwischen den Stationen (zusätzlich ausdifferenziert nach Kundentypen) */
		MODE_FLOW_FACTOR_STATION_CLIENT,
		/** Balkendiagramm zum Vergleich der Auslastungen der Bedeinergruppen  */
		MODE_RESOURCE_UTILIZATION,
		/** Balkendiagramm zum Vergleich der Auslastungen der Transportergruppen  */
		MODE_TRANSPORTER_UTILIZATION
	}

	/**
	 * Konstruktor der Klasse
	 * @param statistics	Statistikobjekt, aus dem die anzuzeigenden Daten entnommen werden sollen
	 * @param mode	Gibt an, welche Daten genau ausgegeben werden sollen
	 * @see Mode
	 */
	public StatisticViewerTimeBarChart(final Statistics statistics, final Mode mode) {
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
		final URL url=StatisticViewerTimeBarChart.class.getResource("description_"+Language.getCurrentLanguage()+"/"+topic+".html");
		addDescription(url,helpTopic->Help.topic(getViewer(false),helpTopic));
	}

	/**
	 * Kürzt einen Stationsnamen; gibt, wenn ein in Anführungszeichen
	 * stehender Name definiert ist, diesen zurück.
	 * @param name	Vollständiger Name der Station
	 * @return	Liefert entweder den übergebenen Namen (wenn er nicht gekürzt werden kann) oder eine gekürzte Fassung
	 */
	private String processStationName(final String name) {
		if (name==null || name.isEmpty()) return "";
		int index=name.indexOf('"');
		if (index<0 || index==name.length()-1) return name;
		String s=name.substring(index+1);
		index=s.indexOf('"');
		if (index<0) return name;
		return s.substring(0,index);
	}

	/**
	 * Erstellt ein Balkendiagramm.
	 * @param title	Diagrammtitel
	 * @param type	Type der Einträge (z.B. "Kundentypen")
	 * @param indicator	Darzustellendes Statistikobjekt
	 * @param colorMap	Farbenliste für die Darstellung
	 * @param processStationNames	Sollen die Stationsnamen gekürzt werden?
	 */
	private void chartRequest(final String title, final String type, final StatisticsMultiPerformanceIndicator indicator, final Map<String,Color> colorMap, final boolean processStationNames) {
		initBarChart(title);
		setupBarChart(title,type,title+" ("+Language.tr("Statistics.InSeconds")+")",false);

		final String[] names=indicator.getNames();
		final StatisticsDataPerformanceIndicator[] indicators=indicator.getAll(StatisticsDataPerformanceIndicator.class);

		for (int i=0;i<names.length;i++) {
			Color color=null;
			String name=names[i];
			if (processStationNames) name=processStationName(name);
			if (colorMap!=null) color=colorMap.get(name);
			if (color==null) color=COLORS[i%COLORS.length];
			data.addValue(indicators[i].getMean(),names[i],names[i]);
			plot.getRendererForDataset(data).setSeriesPaint(i,color);

		}

		initTooltips();
		setOutlineColor(Color.BLACK);
	}

	/**
	 * Erstellt ein Balkendiagramm der Flussfaktoren.
	 * @param title	Diagrammtitel
	 * @param type	Type der Einträge (z.B. "Kundentypen")
	 * @param indicatorProcessing	Statistikdaten zu den Bedienzeiten
	 * @param indicatorResidence	Statistikdaten zu den Verweilzeiten
	 * @param colorMap	Farbenliste für die Darstellung
	 * @param processStationNames	Sollen die Stationsnamen gekürzt werden?
	 */
	private void chartRequestFlowFactor(final String title, final String type, final StatisticsMultiPerformanceIndicator indicatorProcessing, final StatisticsMultiPerformanceIndicator indicatorResidence, final Map<String,Color> colorMap, final boolean processStationNames) {
		initBarChart(title);
		setupBarChart(title,type,title,false);

		final String[] names=indicatorProcessing.getNames();

		for (int i=0;i<names.length;i++) {
			Color color=null;
			String name=names[i];
			final StatisticsDataPerformanceIndicator indicator1=(StatisticsDataPerformanceIndicator)indicatorProcessing.get(name);
			final StatisticsDataPerformanceIndicator indicator2=(StatisticsDataPerformanceIndicator)indicatorResidence.get(name);
			if (processStationNames) name=processStationName(name);
			if (colorMap!=null) color=colorMap.get(name);
			if (color==null) color=COLORS[i%COLORS.length];

			final double time1=indicator1.getMean();
			final double time2=indicator2.getMean();
			if (time1>0) {
				data.addValue(time2/time1,names[i],names[i]);
				plot.getRendererForDataset(data).setSeriesPaint(i,color);
			}
		}

		initTooltips();
		setOutlineColor(Color.BLACK);
	}

	/**
	 * Balkendiagramm zum Vergleich der Auslastungen der Bedeinergruppen
	 * @see Mode#MODE_RESOURCE_UTILIZATION
	 */
	private void resourceUtilizationChartRequest() {
		initBarChart(Language.tr("Statistics.UtilizationAndFailures"));
		setupBarChart(Language.tr("Statistics.UtilizationAndFailures"),Language.tr("Statistics.UtilizationAndFailures.Type"),Language.tr("Statistics.UtilizationAndFailures.Mean"),false);

		final String[] names=statistics.resourceUtilization.getNames();
		final StatisticsTimePerformanceIndicator[] indicators1=statistics.resourceUtilization.getAll(StatisticsTimePerformanceIndicator.class);
		final StatisticsTimePerformanceIndicator[] indicators2=statistics.resourceInDownTime.getAll(StatisticsTimePerformanceIndicator.class);

		for (int i=0;i<names.length;i++) {
			final double part1=indicators1[i].getTimeMean();
			final double part2=indicators2[i].getTimeMean();
			data.addValue(part1,Language.tr("Statistics.UtilizationAndFailures.Utilization"),names[i]);
			data.addValue(part2,Language.tr("Statistics.UtilizationAndFailures.Failure"),names[i]);
			final ModelResource resource=statistics.editModel.resources.get(names[i]);
			if (resource!=null && resource.getMode()==ModelResource.Mode.MODE_NUMBER && resource.getCount()>0) {
				final double part3=resource.getCount()-part1-part2;
				data.addValue(part3,Language.tr("Statistics.UtilizationAndFailures.Idle"),names[i]);
			}
		}

		plot.getRendererForDataset(data).setSeriesPaint(0,Color.BLUE);
		plot.getRendererForDataset(data).setSeriesPaint(1,Color.RED);
		plot.getRendererForDataset(data).setSeriesPaint(2,Color.LIGHT_GRAY);
		plot.getRenderer().setSeriesVisibleInLegend(0,true);
		plot.getRenderer().setSeriesVisibleInLegend(1,true);
		plot.getRenderer().setSeriesVisibleInLegend(2,true);

		initTooltips();
		setOutlineColor(Color.BLACK);
	}

	/**
	 * Balkendiagramm zum Vergleich der Auslastungen der Transportergruppen
	 * @see Mode#MODE_TRANSPORTER_UTILIZATION
	 */
	private void transporterUtilizationChartRequest() {
		initBarChart(Language.tr("Statistics.TransporterUtilization"));
		setupBarChart(Language.tr("Statistics.TransporterUtilization"),Language.tr("Statistics.TransporterUtilization.Type"),Language.tr("Statistics.TransporterUtilization.Mean"),false);

		final String[] names=statistics.transporterUtilization.getNames();
		final StatisticsTimePerformanceIndicator[] indicators1=statistics.transporterUtilization.getAll(StatisticsTimePerformanceIndicator.class);
		final StatisticsTimePerformanceIndicator[] indicators2=statistics.transporterInDownTime.getAll(StatisticsTimePerformanceIndicator.class);

		for (int i=0;i<names.length;i++) {
			final double part1=indicators1[i].getTimeMean();
			final double part2=indicators2[i].getTimeMean();
			data.addValue(part1,Language.tr("Statistics.TransporterUtilization.Utilization"),names[i]);
			data.addValue(part2,Language.tr("Statistics.TransporterUtilization.Failure"),names[i]);
			final ModelTransporter transporter=statistics.editModel.transporters.get(names[i]);
			if (transporter!=null) {
				final double part3=transporter.getCountAll()-part1-part2;
				data.addValue(part3,Language.tr("Statistics.TransporterUtilization.Idle"),names[i]);
			}
		}

		plot.getRendererForDataset(data).setSeriesPaint(0,Color.BLUE);
		plot.getRendererForDataset(data).setSeriesPaint(1,Color.RED);
		plot.getRendererForDataset(data).setSeriesPaint(2,Color.LIGHT_GRAY);
		plot.getRenderer().setSeriesVisibleInLegend(0,true);
		plot.getRenderer().setSeriesVisibleInLegend(1,true);
		plot.getRenderer().setSeriesVisibleInLegend(3,true);

		initTooltips();
		setOutlineColor(Color.BLACK);
	}

	@Override
	protected void firstChartRequest() {
		Map<String,Color> colorMap;

		switch (mode) {
		case MODE_INTERARRIVAL_CLIENTS:
			colorMap=statistics.editModel.clientData.getStatisticColors(statistics.editModel.surface.getClientTypes());
			chartRequest(Language.tr("Statistics.InterArrivalTimes"),Language.tr("Statistics.ClientsSource"),statistics.clientsInterarrivalTime,colorMap,true);
			addDescription("PlotBarCompareClients");
			break;
		case MODE_INTERARRIVAL_STATION:
			chartRequest(Language.tr("Statistics.InterArrivalTimes"),Language.tr("Statistics.Station"),statistics.stationsInterarrivalTime,null,true);
			addDescription("PlotBarCompareStations");
			break;
		case MODE_INTERARRIVAL_STATION_CLIENTS:
			chartRequest(Language.tr("Statistics.InterArrivalTimes"),Language.tr("Statistics.StationClient"),statistics.stationsInterarrivalTimeByClientType,null,true);
			addDescription("PlotBarCompareStations");
			break;
		case MODE_INTERARRIVAL_STATION_STATES:
			chartRequest(Language.tr("Statistics.InterArrivalTimes"),Language.tr("Statistics.StationState"),statistics.stationsInterarrivalTimeByState,null,true);
			addDescription("PlotBarCompareStations");
			break;
		case MODE_INTERLEAVE_CLIENTS:
			colorMap=statistics.editModel.clientData.getStatisticColors(statistics.editModel.surface.getClientTypes());
			chartRequest(Language.tr("Statistics.InterLeaveTimes"),Language.tr("Statistics.ClientsSource"),statistics.clientsInterleavingTime,colorMap,true);
			addDescription("PlotBarCompareClients");
			break;
		case MODE_INTERLEAVE_STATION:
			chartRequest(Language.tr("Statistics.InterLeaveTimes"),Language.tr("Statistics.Station"),statistics.stationsInterleavingTime,null,true);
			addDescription("PlotBarCompareStations");
			break;
		case MODE_INTERLEAVE_STATION_CLIENTS:
			chartRequest(Language.tr("Statistics.InterLeaveTimes"),Language.tr("Statistics.StationClient"),statistics.stationsInterleavingTimeByClientType,null,true);
			addDescription("PlotBarCompareStations");
			break;
		case MODE_WAITING_CLIENTS:
			colorMap=statistics.editModel.clientData.getStatisticColors(statistics.editModel.surface.getClientTypes());
			chartRequest(Language.tr("Statistics.WaitingTimes"),Language.tr("Statistics.ClientType"),statistics.clientsWaitingTimes,colorMap,false);
			addDescription("PlotBarCompareClients");
			break;
		case MODE_TRANSFER_CLIENTS:
			colorMap=statistics.editModel.clientData.getStatisticColors(statistics.editModel.surface.getClientTypes());
			chartRequest(Language.tr("Statistics.TransferTimes"),Language.tr("Statistics.ClientType"),statistics.clientsTransferTimes,colorMap,false);
			addDescription("PlotBarCompareClients");
			break;
		case MODE_PROCESSING_CLIENTS:
			colorMap=statistics.editModel.clientData.getStatisticColors(statistics.editModel.surface.getClientTypes());
			chartRequest(Language.tr("Statistics.ProcessTimes"),Language.tr("Statistics.ClientType"),statistics.clientsProcessingTimes,colorMap,false);
			addDescription("PlotBarCompareClients");
			break;
		case MODE_RESIDENCE_CLIENTS:
			colorMap=statistics.editModel.clientData.getStatisticColors(statistics.editModel.surface.getClientTypes());
			chartRequest(Language.tr("Statistics.ResidenceTimes"),Language.tr("Statistics.ClientType"),statistics.clientsResidenceTimes,colorMap,false);
			addDescription("PlotBarCompareClients");
			break;
		case MODE_FLOW_FACTOR_CLIENTS:
			colorMap=statistics.editModel.clientData.getStatisticColors(statistics.editModel.surface.getClientTypes());
			chartRequestFlowFactor(Language.tr("Statistics.FlowFactor"),Language.tr("Statistics.ClientType"),statistics.clientsProcessingTimes,statistics.clientsResidenceTimes,colorMap,false);
			addDescription("PlotBarCompareClients");
			break;
		case MODE_WAITING_STATION:
			chartRequest(Language.tr("Statistics.WaitingTimes"),Language.tr("Statistics.Station"),statistics.stationsWaitingTimes,null,false);
			addDescription("PlotBarCompareStations");
			break;
		case MODE_TRANSFER_STATION:
			chartRequest(Language.tr("Statistics.TransferTimes"),Language.tr("Statistics.Station"),statistics.stationsTransferTimes,null,false);
			addDescription("PlotBarCompareStations");
			break;
		case MODE_PROCESSING_STATION:
			chartRequest(Language.tr("Statistics.ProcessTimes"),Language.tr("Statistics.Station"),statistics.stationsProcessingTimes,null,false);
			addDescription("PlotBarCompareStations");
			break;
		case MODE_RESIDENCE_STATION:
			chartRequest(Language.tr("Statistics.ResidenceTimes"),Language.tr("Statistics.Station"),statistics.stationsResidenceTimes,null,false);
			addDescription("PlotBarCompareStations");
			break;
		case MODE_FLOW_FACTOR_STATION:
			chartRequestFlowFactor(Language.tr("Statistics.FlowFactor"),Language.tr("Statistics.Station"),statistics.stationsProcessingTimes,statistics.stationsResidenceTimes,null,false);
			addDescription("PlotBarCompareStations");
			break;
		case MODE_WAITING_STATION_CLIENT:
			chartRequest(Language.tr("Statistics.WaitingTimes"),Language.tr("Statistics.StationClient"),statistics.stationsWaitingTimesByClientType,null,false);
			addDescription("PlotBarCompareStations");
			break;
		case MODE_TRANSFER_STATION_CLIENT:
			chartRequest(Language.tr("Statistics.TransferTimes"),Language.tr("Statistics.StationClient"),statistics.stationsTransferTimesByClientType,null,false);
			addDescription("PlotBarCompareStations");
			break;
		case MODE_PROCESSING_STATION_CLIENT:
			chartRequest(Language.tr("Statistics.ProcessTimes"),Language.tr("Statistics.StationClient"),statistics.stationsProcessingTimesByClientType,null,false);
			addDescription("PlotBarCompareStations");
			break;
		case MODE_RESIDENCE_STATION_CLIENT:
			chartRequest(Language.tr("Statistics.ResidenceTimes"),Language.tr("Statistics.StationClient"),statistics.stationsResidenceTimesByClientType,null,false);
			addDescription("PlotBarCompareStations");
			break;
		case MODE_FLOW_FACTOR_STATION_CLIENT:
			chartRequestFlowFactor(Language.tr("Statistics.FlowFactor"),Language.tr("Statistics.StationClient"),statistics.stationsProcessingTimesByClientType,statistics.stationsResidenceTimesByClientType,null,false);
			addDescription("PlotBarCompareStations");
			break;
		case MODE_RESOURCE_UTILIZATION:
			resourceUtilizationChartRequest();
			addDescription("PlotBarCompareUtilizationOperators");
			break;
		case MODE_TRANSPORTER_UTILIZATION:
			transporterUtilizationChartRequest();
			addDescription("PlotBarCompareUtilizationTransporters");
			break;
		}
	}
}
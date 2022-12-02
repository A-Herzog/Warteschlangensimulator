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
import java.awt.Paint;
import java.awt.Rectangle;
import java.io.Serializable;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.CategoryDataset;

import language.Language;
import mathtools.NumberTools;
import simulator.statistics.Statistics;
import statistics.StatisticsDataPerformanceIndicator;
import statistics.StatisticsMultiPerformanceIndicator;
import statistics.StatisticsSimpleValuePerformanceIndicator;
import statistics.StatisticsTimePerformanceIndicator;
import systemtools.statistics.StatisticViewerBarChart;
import ui.help.Help;
import ui.modeleditor.ModelResource;
import ui.modeleditor.ModelTransporter;
import ui.modeleditor.fastpaint.BrighterColor;
import ui.modeleditor.fastpaint.GradientFill;

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

	/** Maximalanzahl an anzuzeigenden Balken */
	private static final int MAX_BARS=100;

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
		/** Balkendiagramm zum Vergleich der mittleren Zwischenankunftszeiten auf Batch-Basis zwischen den Stationen */
		MODE_INTERARRIVAL_STATION_BATCH,
		/** Balkendiagramm zum Vergleich der mittleren Zwischenankunftszeiten zwischen den Stationen nach Kundentypen weiter ausdifferenziert */
		MODE_INTERARRIVAL_STATION_CLIENTS,
		/** Balkendiagramm zum Vergleich der mittleren Zwischenankunftszeiten zwischen den Stationen nach Warteschlangenlänge weiter ausdifferenziert */
		MODE_INTERARRIVAL_STATION_STATES,
		/** Balkendiagramm zum Vergleich der mittleren Zwischenabgangszeiten zwischen den Kundentypen */
		MODE_INTERLEAVE_CLIENTS,
		/** Balkendiagramm zum Vergleich der mittleren Zwischenabgangszeiten zwischen den Stationen */
		MODE_INTERLEAVE_STATION,
		/** Balkendiagramm zum Vergleich der mittleren Zwischenabgangszeiten auf Batch-Basis zwischen den Stationen */
		MODE_INTERLEAVE_STATION_BATCH,
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
		/** Balkendiagramm zum Vergleich der mittleren Rüstzeiten zwischen den Stationen */
		MODE_SETUP_STATION,
		/** Balkendiagramm zum Vergleich der mittleren Wartezeiten zwischen den Stationen (Einzelzeiten der Kunden summiert) */
		MODE_WAITING_STATION_TOTAL,
		/** Balkendiagramm zum Vergleich der mittleren Transportzeiten zwischen den Stationen (Einzelzeiten der Kunden summiert) */
		MODE_TRANSFER_STATION_TOTAL,
		/** Balkendiagramm zum Vergleich der mittleren Bedienzeiten zwischen den Stationen (Einzelzeiten der Kunden summiert) */
		MODE_PROCESSING_STATION_TOTAL,
		/** Balkendiagramm zum Vergleich der mittleren Verweilzeiten zwischen den Stationen (Einzelzeiten der Kunden summiert) */
		MODE_RESIDENCE_STATION_TOTAL,
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
		/** Balkendiagramm zum Vergleich der Auslastungen der Bedienergruppen  */
		MODE_RESOURCE_UTILIZATION,
		/** Balkendiagramm zum Vergleich der relativen Auslastungen der Bedienergruppen  */
		MODE_RESOURCE_UTILIZATION_RHO,
		/** Balkendiagramm zum Vergleich der Auslastungen der Transportergruppen  */
		MODE_TRANSPORTER_UTILIZATION,
		/** Ankünfte pro Thread */
		MODE_SYSTEM_INFO_THREAD_BALANCE,
		/** Laufzeiten der Threads */
		MODE_SYSTEM_INFO_THREAD_TIMES
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
	 * Cache für hellere Farbe-Objekte
	 * @see #gradientCache
	 * @see #getGradientPaint(Color)
	 */
	private final BrighterColor brighterColor=new BrighterColor();

	/**
	 * Zuordnung von Farben zu Farbverlaufen
	 * @see #getGradientPaint(Color)
	 */
	private final Map<Color,Paint> gradientCache=new HashMap<>();

	/**
	 * Liefert zu einer Farbe den passenden Farbverlauf
	 * @param color	Ausgangsfarbe
	 * @return	Farbverlauf
	 */
	private Paint getGradientPaint(final Color color) {
		Paint paint=gradientCache.get(color);
		if (paint==null) gradientCache.put(color,paint=GradientFill.build(new Rectangle(100,100,100,100),color,brighterColor.get(color),true));
		return paint;
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

		data.setNotify(false);
		for (int i=0;i<Math.min(names.length,MAX_BARS);i++) {
			Color color=null;
			String name=names[i];
			if (processStationNames) name=processStationName(name);
			if (colorMap!=null) color=colorMap.get(name);
			if (color==null) color=COLORS[i%COLORS.length];
			if (i==names.length-1) data.setNotify(true);
			data.addValue(indicators[i].getMean(),names[i],names[i]);
			plot.getRendererForDataset(data).setSeriesPaint(i,getGradientPaint(color),i==name.length()-1);
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

		data.setNotify(false);
		for (int i=0;i<Math.min(names.length,MAX_BARS);i++) {
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
				if (i==names.length-1) data.setNotify(true);
				data.addValue(time2/time1,names[i],names[i]);
				plot.getRendererForDataset(data).setSeriesPaint(i,getGradientPaint(color),i==name.length()-1);
			}
		}

		initTooltips();
		setOutlineColor(Color.BLACK);
	}

	/**
	 * Balkendiagramm zum Vergleich der Auslastungen der Bedienergruppen
	 * @see Mode#MODE_RESOURCE_UTILIZATION
	 */
	private void resourceUtilizationChartRequest() {
		initBarChart(Language.tr("Statistics.UtilizationAndFailures"));
		setupBarChart(Language.tr("Statistics.UtilizationAndFailures"),Language.tr("Statistics.UtilizationAndFailures.Type"),Language.tr("Statistics.UtilizationAndFailures.Mean"),false);

		final String[] names=statistics.resourceUtilization.getNames();
		final StatisticsTimePerformanceIndicator[] indicators1=statistics.resourceUtilization.getAll(StatisticsTimePerformanceIndicator.class);
		final StatisticsTimePerformanceIndicator[] indicators2=statistics.resourceInDownTime.getAll(StatisticsTimePerformanceIndicator.class);

		for (int i=0;i<names.length;i++) {
			final double part1=(i<indicators1.length && indicators1[i]!=null)?indicators1[i].getTimeMean():0;
			final double part2=(i<indicators2.length && indicators2[i]!=null)?indicators2[i].getTimeMean():0;
			data.addValue(part1,Language.tr("Statistics.UtilizationAndFailures.Utilization"),names[i]);
			data.addValue(part2,Language.tr("Statistics.UtilizationAndFailures.Failure"),names[i]);
			final ModelResource resource=statistics.editModel.resources.get(names[i]);
			if (resource!=null && resource.getMode()==ModelResource.Mode.MODE_NUMBER && resource.getCount()>0) {
				final double part3=resource.getCount()-part1-part2;
				data.addValue(part3,Language.tr("Statistics.UtilizationAndFailures.Idle"),names[i]);
			}
		}

		plot.getRendererForDataset(data).setSeriesPaint(0,getGradientPaint(Color.BLUE));
		plot.getRendererForDataset(data).setSeriesPaint(1,getGradientPaint(Color.RED));
		plot.getRendererForDataset(data).setSeriesPaint(2,getGradientPaint(Color.LIGHT_GRAY));
		plot.getRenderer().setSeriesVisibleInLegend(0,true);
		plot.getRenderer().setSeriesVisibleInLegend(1,true);
		plot.getRenderer().setSeriesVisibleInLegend(2,true);

		initTooltips();
		setOutlineColor(Color.BLACK);
	}

	/**
	 * Balkendiagramm zum Vergleich der relativen Auslastungen der Bedienergruppen
	 * @see Mode#MODE_RESOURCE_UTILIZATION_RHO
	 */
	private void resourceUtilizationRhoChartRequest() {
		initBarChart(Language.tr("Statistics.UtilizationRho"));
		setupBarChart(Language.tr("Statistics.UtilizationRho"),Language.tr("Statistics.UtilizationRho.Type"),Language.tr("Statistics.UtilizationRho.Mean"),true);

		final String[] names=statistics.resourceRho.getNames();
		final StatisticsSimpleValuePerformanceIndicator[] indicators=statistics.resourceRho.getAll(StatisticsSimpleValuePerformanceIndicator.class);

		for (int i=0;i<names.length;i++) {
			final double part1=(i<indicators.length && indicators[i]!=null)?indicators[i].get():0;
			data.addValue(part1,Language.tr("Statistics.UtilizationRho.Rho"),names[i]);
		}

		plot.getRendererForDataset(data).setSeriesPaint(0,getGradientPaint(Color.RED));

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
			final double part1=(i<indicators1.length && indicators1[i]!=null)?indicators1[i].getTimeMean():0;
			final double part2=(i<indicators2.length && indicators2[i]!=null)?indicators2[i].getTimeMean():0;
			data.addValue(part1,Language.tr("Statistics.TransporterUtilization.Utilization"),names[i]);
			data.addValue(part2,Language.tr("Statistics.TransporterUtilization.Failure"),names[i]);
			final ModelTransporter transporter=statistics.editModel.transporters.get(names[i]);
			if (transporter!=null) {
				final double part3=transporter.getCountAll()-part1-part2;
				data.addValue(part3,Language.tr("Statistics.TransporterUtilization.Idle"),names[i]);
			}
		}

		plot.getRendererForDataset(data).setSeriesPaint(0,getGradientPaint(Color.BLUE));
		plot.getRendererForDataset(data).setSeriesPaint(1,getGradientPaint(Color.RED));
		plot.getRendererForDataset(data).setSeriesPaint(2,getGradientPaint(Color.LIGHT_GRAY));
		plot.getRenderer().setSeriesVisibleInLegend(0,true);
		plot.getRenderer().setSeriesVisibleInLegend(1,true);
		plot.getRenderer().setSeriesVisibleInLegend(3,true);

		initTooltips();
		setOutlineColor(Color.BLACK);
	}

	/**
	 * Balkendiagramm zum Vergleich der Anzahl an Ankünften pro Thread
	 * @see Mode#MODE_SYSTEM_INFO_THREAD_BALANCE
	 */
	private void threadBalanceChartRequest() {
		initBarChart(Language.tr("Statistics.SystemData.ThreadBalance"));
		setupBarChart(Language.tr("Statistics.SystemData.ThreadBalance"),Language.tr("Statistics.SystemData.ThreadBalance.Thread"),Language.tr("Statistics.SystemData.ThreadBalance.NumberOfArrivals"),false);

		final long[] data=statistics.simulationData.threadDynamicBalanceData;
		long sum=0;
		for (long value: data) sum+=value;
		final long mean=sum/data.length;
		final long finalSum=sum;

		for (int i=0;i<data.length;i++) {
			this.data.addValue(data[i],Language.tr("Statistics.SystemData.ThreadBalance"),""+(i+1));
		}

		plot.getRendererForDataset(this.data).setSeriesPaint(0,getGradientPaint(Color.BLUE));

		/* Tooltips */
		final int count=this.data.getRowCount();
		final BarRenderer renderer=(BarRenderer)plot.getRenderer();
		for (int i=0;i<count;i++) {
			renderer.setSeriesToolTipGenerator(i,new StandardCategoryToolTipGenerator(StandardCategoryToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT_STRING,NumberFormat.getInstance(NumberTools.getLocale())) {
				/**
				 * Serialisierungs-ID der Klasse
				 * @see Serializable
				 */
				private static final long serialVersionUID=-7177093326504217396L;

				@Override
				public String generateToolTip(CategoryDataset dataset, int row, int column) {
					final StringBuilder info=new StringBuilder();
					info.append(Language.tr("Statistics.SystemData.ThreadBalance.Thread"));
					info.append(" ");
					info.append(column+1);
					info.append(": ");
					info.append(NumberTools.formatLong(data[column]));
					info.append(" (");
					info.append(StatisticTools.formatPercent(((double)data[column])/finalSum));
					info.append(", ");
					info.append(Language.tr("Statistics.SystemData.ThreadBalance.DeviationFromAverage"));
					info.append("=");
					info.append(NumberTools.formatLong(data[column]-mean));
					info.append(")");
					return info.toString();
				}
			});
		}

		setOutlineColor(Color.BLACK);
	}

	/**
	 * Balkendiagramm zum Vergleich der Laufzeiten pro Thread
	 * @see Mode#MODE_SYSTEM_INFO_THREAD_TIMES
	 */
	private void threadTimesChartRequest() {
		initBarChart(Language.tr("Statistics.SystemData.ThreadBalance.ThreadRuntimes"));
		setupBarChart(Language.tr("Statistics.SystemData.ThreadBalance.ThreadRuntimes"),Language.tr("Statistics.SystemData.ThreadBalance.Thread"),Language.tr("Statistics.SystemData.ThreadBalance.Runtime"),false);

		final double[] data=Arrays.stream(statistics.simulationData.threadRunTimes).mapToDouble(i->i/1000.0).toArray();
		double sum=0;
		for (double value: data) sum+=value;
		final double mean=sum/data.length;
		final double finalSum=sum;

		for (int i=0;i<data.length;i++) {
			this.data.addValue(data[i],Language.tr("Statistics.SystemData.ThreadBalance.Thread"),""+(i+1));
		}

		plot.getRendererForDataset(this.data).setSeriesPaint(0,getGradientPaint(Color.BLUE));

		/* Tooltips */
		final int count=this.data.getRowCount();
		final BarRenderer renderer=(BarRenderer)plot.getRenderer();
		for (int i=0;i<count;i++) {
			renderer.setSeriesToolTipGenerator(i,new StandardCategoryToolTipGenerator(StandardCategoryToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT_STRING,NumberFormat.getInstance(NumberTools.getLocale())) {
				/**
				 * Serialisierungs-ID der Klasse
				 * @see Serializable
				 */
				private static final long serialVersionUID=-5857616637740927146L;

				@Override
				public String generateToolTip(CategoryDataset dataset, int row, int column) {
					final StringBuilder info=new StringBuilder();
					info.append(Language.tr("Statistics.SystemData.ThreadBalance.Thread"));
					info.append(" ");
					info.append(column+1);
					info.append(": ");
					info.append(StatisticTools.formatNumber(data[column]));
					info.append(" (");
					info.append(StatisticTools.formatPercent(data[column]/finalSum));
					info.append(", ");
					info.append(Language.tr("Statistics.SystemData.ThreadBalance.DeviationFromAverage"));
					info.append("=");
					info.append(StatisticTools.formatNumber(data[column]-mean));
					info.append(")");
					return info.toString();
				}
			});
		}

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
		case MODE_INTERARRIVAL_STATION_BATCH:
			chartRequest(Language.tr("Statistics.InterArrivalTimesBatch"),Language.tr("Statistics.Station"),statistics.stationsInterarrivalTimeBatch,null,true);
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
		case MODE_INTERLEAVE_STATION_BATCH:
			chartRequest(Language.tr("Statistics.InterLeaveTimesBatch"),Language.tr("Statistics.Station"),statistics.stationsInterleavingTimeBatch,null,true);
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
		case MODE_SETUP_STATION:
			chartRequest(Language.tr("Statistics.SetupTimes"),Language.tr("Statistics.Station"),statistics.stationsSetupTimes,null,false);
			addDescription("PlotBarCompareStations");
			break;
		case MODE_WAITING_STATION_TOTAL:
			chartRequest(Language.tr("Statistics.WaitingTimes"),Language.tr("Statistics.Station"),statistics.stationsTotalWaitingTimes,null,false);
			addDescription("PlotBarCompareStations");
			break;
		case MODE_TRANSFER_STATION_TOTAL:
			chartRequest(Language.tr("Statistics.TransferTimes"),Language.tr("Statistics.Station"),statistics.stationsTotalTransferTimes,null,false);
			addDescription("PlotBarCompareStations");
			break;
		case MODE_PROCESSING_STATION_TOTAL:
			chartRequest(Language.tr("Statistics.ProcessTimes"),Language.tr("Statistics.Station"),statistics.stationsTotalProcessingTimes,null,false);
			addDescription("PlotBarCompareStations");
			break;
		case MODE_RESIDENCE_STATION_TOTAL:
			chartRequest(Language.tr("Statistics.ResidenceTimes"),Language.tr("Statistics.Station"),statistics.stationsTotalResidenceTimes,null,false);
			addDescription("PlotBarCompareStations");
			break;
		case MODE_FLOW_FACTOR_STATION:
			chartRequestFlowFactor(Language.tr("Statistics.FlowFactor"),Language.tr("Statistics.Station"),statistics.stationsProcessingTimes,statistics.stationsResidenceTimes,null,false);
			addDescription("PlotBarCompareStations");
			break;
		case MODE_WAITING_STATION_CLIENT:
			colorMap=statistics.editModel.clientData.getStatisticColors(statistics.editModel.surface.getClientTypes());
			chartRequest(Language.tr("Statistics.WaitingTimes"),Language.tr("Statistics.StationClient"),statistics.stationsWaitingTimesByClientType,colorMap,false);
			addDescription("PlotBarCompareStations");
			break;
		case MODE_TRANSFER_STATION_CLIENT:
			colorMap=statistics.editModel.clientData.getStatisticColors(statistics.editModel.surface.getClientTypes());
			chartRequest(Language.tr("Statistics.TransferTimes"),Language.tr("Statistics.StationClient"),statistics.stationsTransferTimesByClientType,colorMap,false);
			addDescription("PlotBarCompareStations");
			break;
		case MODE_PROCESSING_STATION_CLIENT:
			colorMap=statistics.editModel.clientData.getStatisticColors(statistics.editModel.surface.getClientTypes());
			chartRequest(Language.tr("Statistics.ProcessTimes"),Language.tr("Statistics.StationClient"),statistics.stationsProcessingTimesByClientType,colorMap,false);
			addDescription("PlotBarCompareStations");
			break;
		case MODE_RESIDENCE_STATION_CLIENT:
			colorMap=statistics.editModel.clientData.getStatisticColors(statistics.editModel.surface.getClientTypes());
			chartRequest(Language.tr("Statistics.ResidenceTimes"),Language.tr("Statistics.StationClient"),statistics.stationsResidenceTimesByClientType,colorMap,false);
			addDescription("PlotBarCompareStations");
			break;
		case MODE_FLOW_FACTOR_STATION_CLIENT:
			colorMap=statistics.editModel.clientData.getStatisticColors(statistics.editModel.surface.getClientTypes());
			chartRequestFlowFactor(Language.tr("Statistics.FlowFactor"),Language.tr("Statistics.StationClient"),statistics.stationsProcessingTimesByClientType,statistics.stationsResidenceTimesByClientType,colorMap,false);
			addDescription("PlotBarCompareStations");
			break;
		case MODE_RESOURCE_UTILIZATION:
			resourceUtilizationChartRequest();
			addDescription("PlotBarCompareUtilizationOperators");
			break;
		case MODE_RESOURCE_UTILIZATION_RHO:
			resourceUtilizationRhoChartRequest();
			addDescription("PlotBarCompareUtilizationOperatorsRho");
			break;
		case MODE_TRANSPORTER_UTILIZATION:
			transporterUtilizationChartRequest();
			addDescription("PlotBarCompareUtilizationTransporters");
			break;
		case MODE_SYSTEM_INFO_THREAD_BALANCE:
			threadBalanceChartRequest();
			addDescription("ThreadBalance");
			break;
		case MODE_SYSTEM_INFO_THREAD_TIMES:
			threadTimesChartRequest();
			addDescription("ThreadTimes");
			break;
		}
	}
}
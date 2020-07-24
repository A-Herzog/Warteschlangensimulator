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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.FastMath;

import language.Language;
import mathtools.NumberTools;
import mathtools.Table;
import mathtools.distribution.DataDistributionImpl;
import simulator.statistics.Statistics;
import statistics.StatisticsDataCollector;
import statistics.StatisticsDataPerformanceIndicator;
import statistics.StatisticsDataPerformanceIndicatorWithNegativeValues;
import statistics.StatisticsMultiPerformanceIndicator;
import statistics.StatisticsTimePerformanceIndicator;
import systemtools.statistics.StatisticViewerTable;
import tools.SetupData;
import ui.help.Help;
import ui.modeleditor.ModelResource;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelTransporter;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementRecord;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Dieser Viewer gibt die Verteilung der Zwischenakunfts-, Warte- und Bedienzeiten in Tabellenform zu den Simulationsergebnissen aus.
 * @see StatisticViewerTable
 * @author Alexander Herzog
 */
public class StatisticViewerTimeTable extends StatisticViewerTable {
	private final Statistics statistics;
	private final Mode mode;

	/**
	 * Wählt die von {@link StatisticViewerTimeTable} auszugebende Information aus.
	 * @author Alexander Herzog
	 * @see StatisticViewerTimeTable#StatisticViewerTimeTable(Statistics, Mode)
	 */
	public enum Mode {
		/** Zwischenankunftszeiten am System (Übersichtstabelle) */
		MODE_OVERVIEW_CLIENTS_INTERARRIVAL,
		/** Zwischenabgangszeiten aus dem System (Übersichtstabelle) */
		MODE_OVERVIEW_CLIENTS_INTERLEAVE,
		/** Zwischenankunftszeiten am System (Tabelle mit Verteilung der Werte) */
		MODE_DISTRIBUTION_CLIENTS_INTERARRIVAL,
		/** Zwischenabgangszeiten am System (Tabelle mit Verteilung der Werte) */
		MODE_DISTRIBUTION_CLIENTS_INTERLEAVE,
		/** Tabelle mit den Anzahlen an Ankünften an den Stationen */
		MODE_OVERVIEW_STATIONS_ARRIVAL_COUNT,
		/** Zwischenankunftszeiten an den Stationen (Übersichtstabelle) */
		MODE_OVERVIEW_STATIONS_INTERARRIVAL,
		/** Zwischenankunftszeiten an den Stationen nach Kundentypen weiter ausdifferenziert (Übersichtstabelle) */
		MODE_OVERVIEW_STATIONS_INTERARRIVAL_CLIENTS,
		/** Zwischenabgangszeiten an den Stationen (Übersichtstabelle) */
		MODE_OVERVIEW_STATIONS_INTERLEAVE,
		/** Zwischenabgangszeiten an den Stationen nach Kundentypen weiter ausdifferenziert (Übersichtstabelle) */
		MODE_OVERVIEW_STATIONS_INTERLEAVE_CLIENTS,
		/** Zwischenankunftszeiten an den Stationen (Tabelle mit Verteilung der Werte) */
		MODE_DISTRIBUTION_STATIONS_INTERARRIVAL,
		/** Zwischenankunftszeiten an den Stationen nach Kundentypen weiter ausdifferenziert (Tabelle mit Verteilung der Werte) */
		MODE_DISTRIBUTION_STATIONS_INTERARRIVAL_CLIENTS,
		/** Zwischenabgangszeiten an den Stationen (Tabelle mit Verteilung der Werte) */
		MODE_DISTRIBUTION_STATIONS_INTERLEAVE,
		/** Zwischenabgangszeiten an den Stationen nach Kundentypen weiter ausdifferenziert (Tabelle mit Verteilung der Werte) */
		MODE_DISTRIBUTION_STATIONS_INTERLEAVE_CLIENTS,

		/** Warte- und Bedienzeiten der Kunden (Übersichtstabelle) */
		MODE_OVERVIEW_CLIENTS_WAITINGPROCESSING,
		/** Verteilung der Wartezeiten der Kunden */
		MODE_DISTRIBUTION_CLIENTS_WAITING,
		/** Verteilung der Transportzeiten der Kunden */
		MODE_DISTRIBUTION_CLIENTS_TRANSFER,
		/** Verteilung der Bedienzeiten der Kunden */
		MODE_DISTRIBUTION_CLIENTS_PROCESSING,
		/** Verteilung der Verweilzeiten der Kunden */
		MODE_DISTRIBUTION_CLIENTS_RESIDENCE,
		/** Warte- und Bedienzeiten an den Stationen (Übersichtstabelle) */
		MODE_OVERVIEW_STATIONSMODE_OVERVIEW_CLIENTS,
		/** Warte- und Bedienzeiten an den Stationen nach Kundentypen ausdifferenziert (Übersichtstabelle) */
		MODE_OVERVIEW_STATIONSCLIENTMODE_OVERVIEW_CLIENTS,
		/** Verteilung der Wartezeiten an den Stationen */
		MODE_DISTRIBUTION_STATIONS_WAITING,
		/** Verteilung der Transportzeiten an den Stationen */
		MODE_DISTRIBUTION_STATIONS_TRANSFER,
		/** Verteilung der Bedienzeiten an den Stationen */
		MODE_DISTRIBUTION_STATIONS_PROCESSING,
		/** Verteilung der Verweilzeiten an den Stationen */
		MODE_DISTRIBUTION_STATIONS_RESIDENCE,
		/** Verteilung der Wartezeiten an den Stationen ausdifferenziert nach Kundentypen */
		MODE_DISTRIBUTION_STATIONSCLIENTS_WAITING,
		/** Verteilung der Transportzeiten an den Stationen ausdifferenziert nach Kundentypen */
		MODE_DISTRIBUTION_STATIONSCLIENTS_TRANSFER,
		/** Verteilung der Bedienzeiten an den Stationen ausdifferenziert nach Kundentypen */
		MODE_DISTRIBUTION_STATIONSCLIENTS_PROCESSING,
		/** Verteilung der Verweilzeiten an den Stationen ausdifferenziert nach Kundentypen */
		MODE_DISTRIBUTION_STATIONSCLIENTS_RESIDENCE,

		/** Anzahl an Kunden an den Stationen (Übersichtstabelle) */
		MODE_OVERVIEW_NUMBER,
		/** Anzahl an Kunden an den Stationen nach Kundentypen (Übersichtstabelle) */
		MODE_OVERVIEW_NUMBER_CLIENT_TYPES,
		/** Anzahl an Kunden in den Warteschlangen an den Stationen (Übersichtstabelle) */
		MODE_OVERVIEW_QUEUE,
		/** Anzahl an Kunden in den Warteschlangen an den Stationen nach Kundentypen (Übersichtstabelle) */
		MODE_OVERVIEW_QUEUE_CLIENT_TYPES,
		/** Verteilung der Anzahlen an Kunden an den Stationen */
		MODE_DISTRIBUTION_NUMBER_STATION,
		/** Verteilung der Anzahlen an Kunden an den Stationen nach Kundentypen */
		MODE_DISTRIBUTION_NUMBER_STATION_CLIENT_TYPES,
		/** Verteilung der Anzahlen an Kunden im System (nach Kundentypen) */
		MODE_DISTRIBUTION_NUMBER_CLIENT,
		/** Verteilung der Anzahlen an Kunden in den Warteschlangen an den Stationen */
		MODE_DISTRIBUTION_QUEUE,
		/** Verteilung der Anzahlen an Kunden in den Warteschlangen an den Stationen nach Kundentypen */
		MODE_DISTRIBUTION_QUEUE_CLIENT_TYPE,

		/** Ressourcenauslastung (Übersichtstabelle) */
		MODE_UTILIZATION,
		/** Ausfallzeiten der Ressourcen (Übersichtstabelle) */
		MODE_DOWNTIMES,
		/** Transporterauslastung (Übersichtstabelle) */
		MODE_TRANSPORTER_UTILIZATION,
		/** Ausfallzeiten der Transporter (Übersichtstabelle) */
		MODE_TRANSPORTER_DOWNTIMES,

		/** Kundendatenfelder (Übersichtstabelle) */
		MODE_CLIENT_DATA,
		/** Kundendatenfelder (Tabelle mit Verteilung der Werte) */
		MODE_CLIENT_DATA_DISTRIBUTION,

		/** Tabelle mit den an den Datenaufzeichnung-Stationen erfassten Werten */
		MODE_VALUE_RECORDING
	}

	/**
	 * Konstruktor der Klasse
	 * @param statistics	Statistikobjekt, aus dem die anzuzeigenden Daten entnommen werden sollen
	 * @param mode	Gibt an, welche Daten genau ausgegeben werden sollen
	 * @see Mode
	 */
	public StatisticViewerTimeTable(final Statistics statistics, final Mode mode) {
		this.statistics=statistics;
		this.mode=mode;
	}

	private String fullStationName(final String statisticName) {
		if (statisticName==null || statisticName.trim().isEmpty()) return "";
		return statisticName;
	}

	private String[] getColumnNames(final String col1, final String col2, String info) {
		final List<String> columns=new ArrayList<>();

		if (col1!=null) columns.add(col1);
		if (col2!=null) columns.add(col2);
		if (info==null) info="";
		columns.add("E"+info);
		columns.add("Std"+info);
		columns.add("Var"+info);
		columns.add("CV"+info);
		columns.add("Min"+info);
		columns.add("Max"+info);
		if (SetupData.getSetup().showQuantils) for (double p: StatisticsDataPerformanceIndicator.storeQuantilValues) {
			columns.add(StatisticTools.formatPercent(p)+" "+Language.tr("Statistics.Quantil")+info);
		}

		return columns.toArray(new String[0]);
	}

	private String[] getDataLine(final String col1, final StatisticsDataPerformanceIndicator data) {
		final List<String> line=new ArrayList<>();

		if (col1!=null) line.add(col1);
		line.add(NumberTools.formatLongNoGrouping(data.getCount()));
		line.add(StatisticTools.formatNumber(data.getMean()));
		line.add(StatisticTools.formatNumber(data.getSD()));
		line.add(StatisticTools.formatNumber(data.getVar()));
		line.add(StatisticTools.formatNumber(data.getCV()));
		line.add(StatisticTools.formatNumber(data.getMin()));
		line.add(StatisticTools.formatNumber(data.getMax()));
		if (SetupData.getSetup().showQuantils && data.getDistribution()!=null) for (double p: StatisticsDataPerformanceIndicator.storeQuantilValues) {
			line.add(StatisticTools.formatNumber(data.getQuantil(p)));
		}

		return line.toArray(new String[0]);
	}

	private String[] getDataLine(final String col1, final StatisticsDataPerformanceIndicatorWithNegativeValues data) {
		final List<String> line=new ArrayList<>();

		if (col1!=null) line.add(col1);
		line.add(NumberTools.formatLongNoGrouping(data.getCount()));
		line.add(StatisticTools.formatNumber(data.getMean()));
		line.add(StatisticTools.formatNumber(data.getSD()));
		line.add(StatisticTools.formatNumber(data.getVar()));
		line.add(StatisticTools.formatNumber(data.getCV()));
		line.add(StatisticTools.formatNumber(data.getMin()));
		line.add(StatisticTools.formatNumber(data.getMax()));
		if (SetupData.getSetup().showQuantils) for (double p: StatisticsDataPerformanceIndicatorWithNegativeValues.storeQuantilValues) {
			line.add(StatisticTools.formatNumber(data.getQuantil(p)));
		}

		return line.toArray(new String[0]);
	}

	private String[] getDataLine(final String col1, final StatisticsTimePerformanceIndicator data) {
		final List<String> line=new ArrayList<>();

		if (col1!=null) line.add(col1);
		line.add(StatisticTools.formatNumber(data.getTimeMean()));
		line.add(StatisticTools.formatNumber(data.getTimeSD()));
		line.add(StatisticTools.formatNumber(data.getTimeVar()));
		line.add(StatisticTools.formatNumber(data.getTimeCV()));
		line.add(StatisticTools.formatNumber(data.getTimeMin()));
		line.add(StatisticTools.formatNumber(data.getTimeMax()));
		if (SetupData.getSetup().showQuantils) for (double p: StatisticsDataPerformanceIndicator.storeQuantilValues) {
			line.add(StatisticTools.formatNumber(data.getQuantil(p)));
		}

		return line.toArray(new String[0]);
	}

	private void addDescription(final String topic) {
		final URL url=StatisticViewerTimeTable.class.getResource("description_"+Language.getCurrentLanguage()+"/"+topic+".html");
		addDescription(url,helpTopic->Help.topic(getViewer(false),helpTopic));
	}

	private void buildTimesOverviewTable(final StatisticsMultiPerformanceIndicator indicator1, final StatisticsMultiPerformanceIndicator indicator2, final StatisticsMultiPerformanceIndicator indicator3, final String type1, final String type2, final String type3, final String label, final boolean isStationsList, final boolean isInterArrival) {
		final Table table=new Table();

		final String[] types=indicator1.getNames();

		for (String type : types) {
			StatisticsDataPerformanceIndicator data;
			data=(StatisticsDataPerformanceIndicator)(indicator1.get(type));
			final String typeName=isStationsList?fullStationName(type):type;
			table.addLine(getDataLine(((type1!=null && !type1.isEmpty())?(type1+" "):"")+typeName,data));
			if (indicator2!=null && type2!=null) {
				data=(StatisticsDataPerformanceIndicator)(indicator2.get(type));
				table.addLine(getDataLine(type2+" "+typeName,data));
			}
			if (indicator3!=null && type3!=null) {
				data=(StatisticsDataPerformanceIndicator)(indicator3.get(type));
				table.addLine(getDataLine(type3+" "+typeName,data));
			}
		}

		final String[] columnNames;
		if (isInterArrival) {
			columnNames=getColumnNames(label,Language.tr("Statistics.Number"),"[I]");
		} else {
			columnNames=getColumnNames(label,Language.tr("Statistics.Number"),"[.]");
		}
		setData(table,columnNames);

		/* Infotext  */
		if (isInterArrival) {
			addDescription("TableOverviewArrival");
		} else {
			addDescription("TableOverviewOther");
		}
	}

	private void buildTimesDistributionTable(final StatisticsMultiPerformanceIndicator indicator, final String label) {
		final Table table=new Table();
		final List<String> headers=new ArrayList<>();

		final String[] types=indicator.getNames();

		headers.add(label);
		List<DataDistributionImpl> dists=new ArrayList<>();
		for (String type: types) {
			headers.add(Language.tr("Statistics.NumberOfClients")+" - "+type);
			headers.add(Language.tr("Statistics.PartOfClients")+" - "+type);
			final DataDistributionImpl dist=((StatisticsDataPerformanceIndicator)(indicator.get(type))).getDistribution();
			if (dist!=null) dists.add(dist);
		}

		final List<Double> sum=new ArrayList<>();
		for (DataDistributionImpl dist: dists) sum.add(dist.sum());

		if (dists.size()>0)	for (int i=0;i<dists.get(0).densityData.length;i++) {
			List<String> line=new ArrayList<>();
			line.add(NumberTools.formatLongNoGrouping(i));
			for (int j=0;j<dists.size();j++) {
				double value=dists.get(j).densityData[i];
				line.add(StatisticTools.formatNumber(value));
				line.add(StatisticTools.formatPercent(value/sum.get(j),3));
			}
			table.addLine(line);
		}

		setData(table,headers);

		/* Infotext  */
		addDescription("TableTimeDistribution");
	}

	private void buildCountOverviewTable(final StatisticsMultiPerformanceIndicator indicators, final StatisticsTimePerformanceIndicator system, final String type) {
		final Table table=new Table();

		if (system!=null) {
			table.addLine(getDataLine(Language.tr("Statistics.System"),system));
		}

		for (String station: indicators.getNames()) {
			final StatisticsTimePerformanceIndicator indicator=(StatisticsTimePerformanceIndicator)(indicators.get(station));
			table.addLine(getDataLine(fullStationName(station),indicator));
		}

		setData(table,getColumnNames(Language.tr("Statistics.Station"),null,"["+type+"]"));

		/* Infotext  */
		if (type.equals("N")) addDescription("TableCountOverviewN");
		if (type.equals("NQ")) addDescription("TableCountOverviewNQ");
	}

	private void buildCountDistributionTable(final StatisticsMultiPerformanceIndicator indicators, final StatisticsTimePerformanceIndicator system) {
		final Table table=new Table();
		final List<String> labels=new ArrayList<>();

		int max=0;
		if (system!=null) max=FastMath.max(max,system.getTimeMax());
		for (StatisticsTimePerformanceIndicator indicator : (StatisticsTimePerformanceIndicator[])indicators.getAll(StatisticsTimePerformanceIndicator.class)) max=FastMath.max(max,indicator.getTimeMax());

		labels.add(Language.tr("Statistics.State"));
		List<String> line=new ArrayList<>();
		for (int i=0;i<=max;i++) line.add(NumberTools.formatLongNoGrouping(i));
		table.addLine(line);

		if (system!=null) {
			labels.add(Language.tr("Statistics.System"));
			line=new ArrayList<>();
			final double[] data=system.getReadOnlyDistribution().densityData;
			final double sum=system.getReadOnlyDistribution().sum();
			for (int i=0;i<=max;i++) line.add(StatisticTools.formatPercent((i>=data.length || sum==0)?0.0:data[i]/sum,3));
			table.addLine(line);
		}
		for (String name : indicators.getNames()) {
			labels.add(name);
			StatisticsTimePerformanceIndicator indicator=(StatisticsTimePerformanceIndicator)(indicators.get(name));
			line=new ArrayList<>();
			final double[] data=indicator.getReadOnlyDistribution().densityData;
			final double sum=indicator.getReadOnlyDistribution().sum();
			for (int i=0;i<=max;i++) line.add(StatisticTools.formatPercent((i>=data.length || sum==0)?0.0:data[i]/sum,3));
			table.addLine(line);
		}

		setData(table.transpose(),labels);

		/* Infotext  */
		addDescription("TableCountDistribution");
	}

	private void buildInterarrivalCountTable() {
		final Table table=new Table();

		long arrivalSum=0;
		for (String type : statistics.clientsInterarrivalTime.getNames()) {
			arrivalSum+=((StatisticsDataPerformanceIndicator)(statistics.clientsInterarrivalTime.get(type))).getCount();
		}

		for (String station : statistics.stationsInterarrivalTime.getNames()) {
			final StatisticsDataPerformanceIndicator indicator=(StatisticsDataPerformanceIndicator)(statistics.stationsInterarrivalTime.get(station));
			long count=indicator.getCount();
			String part="";
			if (arrivalSum>0) part=StatisticTools.formatPercent(((double)count)/arrivalSum);
			if (count>0) table.addLine(new String[]{
					fullStationName(station),
					NumberTools.formatLongNoGrouping(count),
					part
			});
		}

		setData(table,new String[]{Language.tr("Statistics.Station"),Language.tr("Statistics.Number"),Language.tr("Statistics.Part")});

		/* Infotext  */
		addDescription("TableInterarrivalCount");
	}

	private void buildUtilizationTable() {
		final Table table=new Table();

		final StatisticsMultiPerformanceIndicator indicators=statistics.resourceUtilization;
		for (String resource : indicators.getNames()) {
			final StatisticsTimePerformanceIndicator indicator=(StatisticsTimePerformanceIndicator)(indicators.get(resource));
			final ModelResource resourceObj=statistics.editModel.resources.get(resource);
			final List<String> line=new ArrayList<>();

			line.add(resource);
			int count=-1;
			if (resourceObj!=null && resourceObj.getMode()==ModelResource.Mode.MODE_NUMBER) {
				count=resourceObj.getCount();
				if (count>0) {
					final StatisticsTimePerformanceIndicator countIndicator=(StatisticsTimePerformanceIndicator)(statistics.resourceCount.getOrNull(resource));
					if (countIndicator==null || countIndicator.getTimeMean()<0.00001) {
						line.add(""+count);
						line.add(StatisticTools.formatPercent(indicator.getTimeMean()/count));
					} else {
						line.add(StatisticTools.formatNumber(countIndicator.getTimeMean()));
						line.add(StatisticTools.formatPercent(indicator.getTimeMean()/countIndicator.getTimeMean()));
					}
				} else {
					line.add(Language.tr("Statistics.infinite"));
					line.add("-");
				}
			} else {
				if (resourceObj!=null && resourceObj.getMode()==ModelResource.Mode.MODE_SCHEDULE) {
					line.add(Language.tr("Statistics.Schedule")+": "+resourceObj.getSchedule());
					line.add("-");
				} else {
					line.add("-");
					line.add("-");
				}
			}
			line.add(StatisticTools.formatNumber(indicator.getTimeMean()));
			line.add(StatisticTools.formatNumber(indicator.getTimeSD()));
			line.add(StatisticTools.formatNumber(indicator.getTimeVar()));
			line.add(StatisticTools.formatNumber(indicator.getTimeCV()));
			line.add(StatisticTools.formatNumber(indicator.getTimeMin()));
			line.add(StatisticTools.formatNumber(indicator.getTimeMax()));
			line.add(StatisticTools.formatPercent(indicator.getTimePartForState(0)));
			if (count>0) {
				line.add(StatisticTools.formatPercent(indicator.getTimePartForState(count)));
			} else {
				line.add("-");
			}
			table.addLine(line);
		}
		setData(table,new String[]{
				Language.tr("Statistics.Resource"),
				Language.tr("Statistics.NumberOfOperators"),
				"rho",
				Language.tr("Statistics.AverageBusy"),
				"Std",
				"Var",
				"CV",
				"Min",
				"Max",
				Language.tr("Statistics.IdleShare"),
				Language.tr("Statistics.FullyBusyShare")
		});

		/* Infotext  */
		addDescription("TableResourceUtilization");
	}

	private void buildDownTimesTable() {
		final Table table=new Table();

		final StatisticsMultiPerformanceIndicator indicators=statistics.resourceInDownTime;
		for (String resource : indicators.getNames()) {
			final StatisticsTimePerformanceIndicator indicator=(StatisticsTimePerformanceIndicator)(indicators.get(resource));
			final ModelResource resourceObj=statistics.editModel.resources.get(resource);
			final List<String> line=new ArrayList<>();

			line.add(resource);
			if (resourceObj!=null && resourceObj.getMode()==ModelResource.Mode.MODE_NUMBER) {
				final int count=resourceObj.getCount();
				line.add((count>0)?(""+count):Language.tr("Statistics.infinite"));
				line.add((count>0)?StatisticTools.formatPercent(indicator.getTimeMean()/count):"-");
			} else {
				if (resourceObj!=null && resourceObj.getMode()==ModelResource.Mode.MODE_SCHEDULE) {
					line.add(Language.tr("Statistics.Schedule")+": "+resourceObj.getSchedule());
					line.add("-");
				} else {
					line.add("-");
					line.add("-");
				}
			}
			line.add(StatisticTools.formatNumber(indicator.getTimeMean()));
			line.add(StatisticTools.formatNumber(indicator.getTimeSD()));
			line.add(StatisticTools.formatNumber(indicator.getTimeVar()));
			line.add(StatisticTools.formatNumber(indicator.getTimeCV()));
			line.add(StatisticTools.formatNumber(indicator.getTimeMin()));
			line.add(StatisticTools.formatNumber(indicator.getTimeMax()));
			table.addLine(line);
		}
		setData(table,new String[]{
				Language.tr("Statistics.Resource"),
				Language.tr("Statistics.NumberOfOperators"),
				Language.tr("Statistics.ShareInDownTime"),
				Language.tr("Distribution.Mean"),
				"Std",
				"Var",
				"CV",
				"Min",
				"Max",
		});

		/* Infotext  */
		addDescription("TableResourceDownTimes");
	}

	private void buildTransporterUtilizationTable() {
		final Table table=new Table();

		final StatisticsMultiPerformanceIndicator indicators=statistics.transporterUtilization;
		for (String transporter : indicators.getNames()) {
			final StatisticsTimePerformanceIndicator indicator=(StatisticsTimePerformanceIndicator)(indicators.get(transporter));
			final ModelTransporter transporterObj=statistics.editModel.transporters.get(transporter);
			final List<String> line=new ArrayList<>();

			line.add(transporter);
			int count=-1;
			if (transporterObj!=null) {
				count=transporterObj.getCountAll();
				line.add(""+count);
				line.add(StatisticTools.formatPercent(indicator.getTimeMean()/count));
			} else {
				line.add("-");
				line.add("-");
			}
			line.add(StatisticTools.formatNumber(indicator.getTimeMean()));
			line.add(StatisticTools.formatNumber(indicator.getTimeSD()));
			line.add(StatisticTools.formatNumber(indicator.getTimeVar()));
			line.add(StatisticTools.formatNumber(indicator.getTimeCV()));
			line.add(StatisticTools.formatNumber(indicator.getTimeMin()));
			line.add(StatisticTools.formatNumber(indicator.getTimeMax()));
			line.add(StatisticTools.formatPercent(indicator.getTimePartForState(0)));
			if (count>0) {
				line.add(StatisticTools.formatPercent(indicator.getTimePartForState(count)));
			} else {
				line.add("-");
			}
			table.addLine(line);
		}
		setData(table,new String[]{
				Language.tr("Statistics.Resource"),
				Language.tr("Statistics.TransporterUtilization.NumberOfTransporters"),
				"rho",
				Language.tr("Statistics.TransporterUtilization.Average"),
				"Std",
				"Var",
				"CV",
				"Min",
				"Max",
				Language.tr("Statistics.TransporterUtilization.IdleShare"),
				Language.tr("Statistics.TransporterUtilization.FullyBusyShare")
		});

		/* Infotext  */
		addDescription("TableTransporterUtilization");
	}

	private void buildTransporterDownTimesTable() {
		final Table table=new Table();

		final StatisticsMultiPerformanceIndicator indicators=statistics.transporterInDownTime;
		for (String transporter : indicators.getNames()) {
			final StatisticsTimePerformanceIndicator indicator=(StatisticsTimePerformanceIndicator)(indicators.get(transporter));
			final ModelTransporter transporterObj=statistics.editModel.transporters.get(transporter);
			final List<String> line=new ArrayList<>();

			line.add(transporter);
			if (transporterObj!=null) {
				final int count=transporterObj.getCountAll();
				line.add((count>0)?(""+count):Language.tr("Statistics.infinite"));
				line.add((count>0)?StatisticTools.formatPercent(indicator.getTimeMean()/count):"-");
			} else {
				line.add("-");
				line.add("-");
			}
			line.add(StatisticTools.formatNumber(indicator.getTimeMean()));
			line.add(StatisticTools.formatNumber(indicator.getTimeSD()));
			line.add(StatisticTools.formatNumber(indicator.getTimeVar()));
			line.add(StatisticTools.formatNumber(indicator.getTimeCV()));
			line.add(StatisticTools.formatNumber(indicator.getTimeMin()));
			line.add(StatisticTools.formatNumber(indicator.getTimeMax()));
			table.addLine(line);
		}
		setData(table,new String[]{
				Language.tr("Statistics.Transporter"),
				Language.tr("Statistics.NumberOfTransporters"),
				Language.tr("Statistics.ShareInDownTime"),
				Language.tr("Distribution.Mean"),
				"Std",
				"Var",
				"CV",
				"Min",
				"Max",
		});

		/* Infotext  */
		addDescription("TableTransporterDownTimes");
	}

	private void buildClientDataTable() {
		final Table table=new Table();

		final StatisticsMultiPerformanceIndicator indicators=statistics.clientData;
		for (String name : indicators.getNames()) {
			final StatisticsDataPerformanceIndicatorWithNegativeValues indicator=(StatisticsDataPerformanceIndicatorWithNegativeValues)(indicators.get(name));
			table.addLine(getDataLine(String.format(Language.tr("Statistics.ClientData.Field"),name),indicator));
		}
		setData(table,getColumnNames(Language.tr("Statistics.ClientData"),null,null));

		/* Infotext  */
		addDescription("TableClientData");
	}

	private void buildClientDataDistributionTable() {
		final Table table=new Table();
		final List<String> headers=new ArrayList<>();

		headers.add(Language.tr("Statistics.Value"));
		List<DataDistributionImpl> dists=new ArrayList<>();
		for (String name: statistics.clientData.getNames()) {
			final String field=String.format(Language.tr("Statistics.ClientData.Field"),name);
			headers.add(Language.tr("Statistics.Number")+" - "+field);
			headers.add(Language.tr("Statistics.Part")+" - "+field);
			final DataDistributionImpl dist=((StatisticsDataPerformanceIndicatorWithNegativeValues)(statistics.clientData.get(name))).getDistribution();
			if (dist!=null) dists.add(dist);
		}

		final List<Double> sum=new ArrayList<>();
		for (DataDistributionImpl dist: dists) sum.add(dist.sum());

		if (dists.size()>0)	for (int i=0;i<dists.get(0).densityData.length;i++) {
			List<String> line=new ArrayList<>();
			line.add(NumberTools.formatLongNoGrouping(i));
			for (int j=0;j<dists.size();j++) {
				double value=dists.get(j).densityData[i];
				line.add(StatisticTools.formatNumber(value));
				line.add(StatisticTools.formatPercent(value/sum.get(j),3));
			}
			table.addLine(line);
		}

		setData(table,headers);

		/* Infotext  */
		addDescription("TableClientDataDistribution");
	}

	private final ModelElementRecord getStation(final ModelSurface surface, final String data) {
		for (ModelElement element: surface.getElements()) {
			if (element instanceof ModelElementRecord && element.getName().equals(data)) return (ModelElementRecord)element;
			if (element instanceof ModelElementSub) {
				final ModelElementRecord record=getStation(((ModelElementSub)element).getSubSurface(),data);
				if (record!=null) return record;
			}
		}
		return null;
	}

	private String getExpressions(final String data) {
		final boolean value2=data.endsWith("-2");
		final String shortData=data.substring(0,data.length()-2);

		final ModelElementRecord record=getStation(statistics.editModel.surface,shortData);
		if (record==null) return null;
		return value2?record.getExpression2():record.getExpression1();
	}

	private void buildValueRecordingTable() {
		final Table table=new Table();
		final List<String> headers=new ArrayList<>();

		headers.add(Language.tr("Statistics.RecordedValue"));
		for (String name: statistics.valueRecording.getNames()) {
			final String info=getExpressions(name);
			headers.add(name+((info==null)?"":(" ("+info+")")));
		}

		int max=0;
		for (StatisticsDataCollector indicator: (StatisticsDataCollector[])statistics.valueRecording.getAll(StatisticsDataCollector.class)) max=Math.max(max,indicator.getCount());
		max=Math.min(max,Table.MAX_EXCEL_ROW_COUNT-1);
		final String[] row=new String[max];
		for (int i=0;i<max;i++) row[i]=NumberTools.formatLongNoGrouping(i+1);
		table.addLine(row);


		for (StatisticsDataCollector indicator: (StatisticsDataCollector[])statistics.valueRecording.getAll(StatisticsDataCollector.class)) {
			final double[] values=indicator.getValuesReadOnly();
			final int count=Math.min(Math.min(indicator.getCount(),values.length),Table.MAX_EXCEL_ROW_COUNT-1);
			for (int i=0;i<count;i++) {
				if (i==Table.MAX_EXCEL_ROW_COUNT-2) row[i]="+ weitere"; else row[i]=StatisticTools.formatNumber(values[i]);
			}
			for (int i=count;i<max;i++) row[i]="";
			table.addLine(row);
		}

		setData(table.transpose(),headers);
	}

	@Override
	protected void buildTable() {
		switch (mode) {
		case MODE_OVERVIEW_CLIENTS_INTERARRIVAL: buildTimesOverviewTable(statistics.clientsInterarrivalTime,null,null,null,null,null,Language.tr("Statistics.Station"),true,true); break;
		case MODE_OVERVIEW_CLIENTS_INTERLEAVE: buildTimesOverviewTable(statistics.clientsInterleavingTime,null,null,null,null,null,Language.tr("Statistics.ClientType"),true,false); break;
		case MODE_DISTRIBUTION_CLIENTS_INTERARRIVAL: buildTimesDistributionTable(statistics.clientsInterarrivalTime,Language.tr("Statistics.DistanceInSeconds")); break;
		case MODE_DISTRIBUTION_CLIENTS_INTERLEAVE: buildTimesDistributionTable(statistics.clientsInterleavingTime,Language.tr("Statistics.DistanceInSeconds")); break;
		case MODE_OVERVIEW_STATIONS_ARRIVAL_COUNT: buildInterarrivalCountTable(); break;
		case MODE_OVERVIEW_STATIONS_INTERARRIVAL: buildTimesOverviewTable(statistics.stationsInterarrivalTime,null,null,null,null,null,Language.tr("Statistics.Station"),true,true); break;
		case MODE_OVERVIEW_STATIONS_INTERARRIVAL_CLIENTS: buildTimesOverviewTable(statistics.stationsInterarrivalTimeByClientType,null,null,null,null,null,Language.tr("Statistics.StationClient"),true,true); break;
		case MODE_OVERVIEW_STATIONS_INTERLEAVE: buildTimesOverviewTable(statistics.stationsInterleavingTime,null,null,null,null,null,Language.tr("Statistics.Station"),true,false); break;
		case MODE_OVERVIEW_STATIONS_INTERLEAVE_CLIENTS: buildTimesOverviewTable(statistics.stationsInterleavingTimeByClientType,null,null,null,null,null,Language.tr("Statistics.StationClient"),true,false); break;
		case MODE_DISTRIBUTION_STATIONS_INTERARRIVAL: buildTimesDistributionTable(statistics.stationsInterarrivalTime,Language.tr("Statistics.DistanceInSeconds")); break;
		case MODE_DISTRIBUTION_STATIONS_INTERARRIVAL_CLIENTS: buildTimesDistributionTable(statistics.stationsInterarrivalTimeByClientType,Language.tr("Statistics.DistanceInSeconds")); break;
		case MODE_DISTRIBUTION_STATIONS_INTERLEAVE: buildTimesDistributionTable(statistics.stationsInterleavingTime,Language.tr("Statistics.DistanceInSeconds")); break;
		case MODE_DISTRIBUTION_STATIONS_INTERLEAVE_CLIENTS: buildTimesDistributionTable(statistics.stationsInterleavingTimeByClientType,Language.tr("Statistics.DistanceInSeconds")); break;
		case MODE_OVERVIEW_CLIENTS_WAITINGPROCESSING: buildTimesOverviewTable(statistics.clientsWaitingTimes,statistics.clientsTransferTimes,statistics.clientsProcessingTimes,Language.tr("Statistics.WaitingTime"),Language.tr("Statistics.TransferTime"),Language.tr("Statistics.ProcessTime"),Language.tr("Statistics.ClientType"),true,false); break;
		case MODE_DISTRIBUTION_CLIENTS_WAITING: buildTimesDistributionTable(statistics.clientsWaitingTimes,Language.tr("Statistics.Seconds")); break;
		case MODE_DISTRIBUTION_CLIENTS_TRANSFER: buildTimesDistributionTable(statistics.clientsTransferTimes,Language.tr("Statistics.Seconds")); break;
		case MODE_DISTRIBUTION_CLIENTS_PROCESSING: buildTimesDistributionTable(statistics.clientsProcessingTimes,Language.tr("Statistics.Seconds")); break;
		case MODE_DISTRIBUTION_CLIENTS_RESIDENCE: buildTimesDistributionTable(statistics.clientsResidenceTimes,Language.tr("Statistics.Seconds")); break;
		case MODE_OVERVIEW_STATIONSMODE_OVERVIEW_CLIENTS: buildTimesOverviewTable(statistics.stationsWaitingTimes,statistics.stationsTransferTimes,statistics.stationsProcessingTimes,Language.tr("Statistics.WaitingTime"),Language.tr("Statistics.TransferTime"),Language.tr("Statistics.ProcessTime"),Language.tr("Statistics.Station"),true,false); break;
		case MODE_OVERVIEW_STATIONSCLIENTMODE_OVERVIEW_CLIENTS: buildTimesOverviewTable(statistics.stationsWaitingTimesByClientType,statistics.stationsTransferTimesByClientType,statistics.stationsProcessingTimesByClientType,Language.tr("Statistics.WaitingTime"),Language.tr("Statistics.TransferTime"),Language.tr("Statistics.ProcessTime"),Language.tr("Statistics.StationClient"),true,false); break;
		case MODE_DISTRIBUTION_STATIONS_WAITING: buildTimesDistributionTable(statistics.stationsWaitingTimes,Language.tr("Statistics.Seconds")); break;
		case MODE_DISTRIBUTION_STATIONS_TRANSFER: buildTimesDistributionTable(statistics.stationsTransferTimes,Language.tr("Statistics.Seconds")); break;
		case MODE_DISTRIBUTION_STATIONS_PROCESSING: buildTimesDistributionTable(statistics.stationsProcessingTimes,Language.tr("Statistics.Seconds")); break;
		case MODE_DISTRIBUTION_STATIONS_RESIDENCE: buildTimesDistributionTable(statistics.stationsResidenceTimes,Language.tr("Statistics.Seconds")); break;
		case MODE_DISTRIBUTION_STATIONSCLIENTS_WAITING: buildTimesDistributionTable(statistics.stationsWaitingTimesByClientType,Language.tr("Statistics.Seconds")); break;
		case MODE_DISTRIBUTION_STATIONSCLIENTS_TRANSFER: buildTimesDistributionTable(statistics.stationsTransferTimesByClientType,Language.tr("Statistics.Seconds")); break;
		case MODE_DISTRIBUTION_STATIONSCLIENTS_PROCESSING: buildTimesDistributionTable(statistics.stationsProcessingTimesByClientType,Language.tr("Statistics.Seconds")); break;
		case MODE_DISTRIBUTION_STATIONSCLIENTS_RESIDENCE: buildTimesDistributionTable(statistics.stationsResidenceTimesByClientType,Language.tr("Statistics.Seconds")); break;
		case MODE_OVERVIEW_NUMBER: buildCountOverviewTable(statistics.clientsAtStationByStation,statistics.clientsInSystem,"N"); break;
		case MODE_OVERVIEW_NUMBER_CLIENT_TYPES: buildCountOverviewTable(statistics.clientsAtStationByStationAndClient,null,"N"); break;
		case MODE_OVERVIEW_QUEUE: buildCountOverviewTable(statistics.clientsAtStationQueueByStation,statistics.clientsInSystemQueues,"NQ"); break;
		case MODE_OVERVIEW_QUEUE_CLIENT_TYPES: buildCountOverviewTable(statistics.clientsAtStationQueueByStationAndClient,null,"NQ"); break;
		case MODE_DISTRIBUTION_NUMBER_STATION: buildCountDistributionTable(statistics.clientsAtStationByStation,statistics.clientsInSystem); break;
		case MODE_DISTRIBUTION_NUMBER_STATION_CLIENT_TYPES: buildCountDistributionTable(statistics.clientsAtStationByStationAndClient,null); break;
		case MODE_DISTRIBUTION_NUMBER_CLIENT: buildCountDistributionTable(statistics.clientsInSystemByClient,statistics.clientsInSystem); break;
		case MODE_DISTRIBUTION_QUEUE: buildCountDistributionTable(statistics.clientsAtStationQueueByStation,statistics.clientsInSystemQueues); break;
		case MODE_DISTRIBUTION_QUEUE_CLIENT_TYPE: buildCountDistributionTable(statistics.clientsAtStationQueueByStationAndClient,null); break;
		case MODE_UTILIZATION: buildUtilizationTable(); break;
		case MODE_DOWNTIMES: buildDownTimesTable(); break;
		case MODE_TRANSPORTER_UTILIZATION: buildTransporterUtilizationTable(); break;
		case MODE_TRANSPORTER_DOWNTIMES: buildTransporterDownTimesTable(); break;
		case MODE_CLIENT_DATA: buildClientDataTable(); break;
		case MODE_CLIENT_DATA_DISTRIBUTION: buildClientDataDistributionTable(); break;
		case MODE_VALUE_RECORDING: buildValueRecordingTable(); break;
		}
	}
}

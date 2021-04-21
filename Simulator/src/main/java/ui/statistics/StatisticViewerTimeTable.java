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
	/** Statistikobjekt, aus dem die anzuzeigenden Daten entnommen werden sollen */
	private final Statistics statistics;
	/** Gibt an, welche Daten genau ausgegeben werden sollen */
	private final Mode mode;

	/**
	 * W�hlt die von {@link StatisticViewerTimeTable} auszugebende Information aus.
	 * @author Alexander Herzog
	 * @see StatisticViewerTimeTable#StatisticViewerTimeTable(Statistics, Mode)
	 */
	public enum Mode {
		/** Zwischenankunftszeiten am System (�bersichtstabelle) */
		MODE_OVERVIEW_CLIENTS_INTERARRIVAL,
		/** Zwischenabgangszeiten aus dem System (�bersichtstabelle) */
		MODE_OVERVIEW_CLIENTS_INTERLEAVE,
		/** Zwischenankunftszeiten am System (Tabelle mit Verteilung der Werte) */
		MODE_DISTRIBUTION_CLIENTS_INTERARRIVAL,
		/** Zwischenabgangszeiten am System (Tabelle mit Verteilung der Werte) */
		MODE_DISTRIBUTION_CLIENTS_INTERLEAVE,
		/** Tabelle mit den Anzahlen an Ank�nften an den Stationen */
		MODE_OVERVIEW_STATIONS_ARRIVAL_COUNT,
		/** Zwischenankunftszeiten an den Stationen (�bersichtstabelle) */
		MODE_OVERVIEW_STATIONS_INTERARRIVAL,
		/** Zwischenankunftszeiten an den Stationen auf Batch-Basis (�bersichtstabelle) */
		MODE_OVERVIEW_STATIONS_INTERARRIVAL_BATCH,
		/** Zwischenankunftszeiten an den Stationen nach Kundentypen weiter ausdifferenziert (�bersichtstabelle) */
		MODE_OVERVIEW_STATIONS_INTERARRIVAL_CLIENTS,
		/** Zwischenankunftszeiten an den Stationen nach Warteschlangenl�nge weiter ausdifferenziert (�bersichtstabelle) */
		MODE_OVERVIEW_STATIONS_INTERARRIVAL_STATES,
		/** Zwischenabgangszeiten an den Stationen (�bersichtstabelle) */
		MODE_OVERVIEW_STATIONS_INTERLEAVE,
		/** Zwischenabgangszeiten an den Stationen nach Kundentypen weiter ausdifferenziert (�bersichtstabelle) */
		MODE_OVERVIEW_STATIONS_INTERLEAVE_CLIENTS,
		/** Zwischenankunftszeiten an den Stationen (Tabelle mit Verteilung der Werte) */
		MODE_DISTRIBUTION_STATIONS_INTERARRIVAL,
		/** Zwischenankunftszeiten an den Stationen auf Batch-Basis (Tabelle mit Verteilung der Werte) */
		MODE_DISTRIBUTION_STATIONS_INTERARRIVAL_BATCH,
		/** Zwischenankunftszeiten an den Stationen nach Kundentypen weiter ausdifferenziert (Tabelle mit Verteilung der Werte) */
		MODE_DISTRIBUTION_STATIONS_INTERARRIVAL_CLIENTS,
		/** Zwischenankunftszeiten an den Stationen nach Warteschlangenl�nge weiter ausdifferenziert (Tabelle mit Verteilung der Werte) */
		MODE_DISTRIBUTION_STATIONS_INTERARRIVAL_STATES,
		/** Zwischenabgangszeiten an den Stationen (Tabelle mit Verteilung der Werte) */
		MODE_DISTRIBUTION_STATIONS_INTERLEAVE,
		/** Zwischenabgangszeiten an den Stationen nach Kundentypen weiter ausdifferenziert (Tabelle mit Verteilung der Werte) */
		MODE_DISTRIBUTION_STATIONS_INTERLEAVE_CLIENTS,

		/** Warte- und Bedienzeiten der Kunden (�bersichtstabelle) */
		MODE_OVERVIEW_CLIENTS_WAITINGPROCESSING,
		/** Verteilung der Wartezeiten der Kunden */
		MODE_DISTRIBUTION_CLIENTS_WAITING,
		/** Verteilung der Transportzeiten der Kunden */
		MODE_DISTRIBUTION_CLIENTS_TRANSFER,
		/** Verteilung der Bedienzeiten der Kunden */
		MODE_DISTRIBUTION_CLIENTS_PROCESSING,
		/** Verteilung der Verweilzeiten der Kunden */
		MODE_DISTRIBUTION_CLIENTS_RESIDENCE,
		/** Warte- und Bedienzeiten an den Stationen (�bersichtstabelle) */
		MODE_OVERVIEW_STATIONSMODE_OVERVIEW_CLIENTS,
		/** Warte- und Bedienzeiten an den Stationen nach Kundentypen ausdifferenziert (�bersichtstabelle) */
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

		/** Anzahl an Kunden an den Stationen (�bersichtstabelle) */
		MODE_OVERVIEW_NUMBER,
		/** Anzahl an Kunden an den Stationen nach Kundentypen (�bersichtstabelle) */
		MODE_OVERVIEW_NUMBER_CLIENT_TYPES,
		/** Anzahl an Kunden in den Warteschlangen an den Stationen (�bersichtstabelle) */
		MODE_OVERVIEW_QUEUE,
		/** Anzahl an Kunden in den Warteschlangen an den Stationen nach Kundentypen (�bersichtstabelle) */
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

		/** Ressourcenauslastung (�bersichtstabelle) */
		MODE_UTILIZATION,
		/** Ausfallzeiten der Ressourcen (�bersichtstabelle) */
		MODE_DOWNTIMES,
		/** Transporterauslastung (�bersichtstabelle) */
		MODE_TRANSPORTER_UTILIZATION,
		/** Ausfallzeiten der Transporter (�bersichtstabelle) */
		MODE_TRANSPORTER_DOWNTIMES,

		/** Kundendatenfelder (�bersichtstabelle) */
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

	/**
	 * Liefert einen Stationsnamen
	 * @param statisticName	Eingabe-Stationsname (kann <code>null</code> sein)
	 * @return	Ausgabe-Stationsname (ist nie <code>null</code>)
	 */
	private String fullStationName(final String statisticName) {
		if (statisticName==null || statisticName.trim().isEmpty()) return "";
		return statisticName;
	}

	/**
	 * Liefert die Spalten�berschriften.
	 * @param col1	Optionale erste Spalte (kann <code>null</code> sein)
	 * @param col2	Optionale zweite Spalte (kann <code>null</code> sein)
	 * @param info	Optionaler Wert, der an die Indikatoren angeh�ngt wird (kann <code>null</code> sein; �blich sowas wie "[X]")
	 * @param confidenceLevels	Konfidenzlevels f�r die Spalten vorgesehen werden solle (kann <code>null</code> sein)
	 * @return	Spalten�berschriften
	 */
	private String[] getColumnNames(final String col1, final String col2, String info, final double[] confidenceLevels) {
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

		if (SetupData.getSetup().showQuantils) {
			final double[] levels=StatisticViewerOverviewText.getQuantilLevels();
			for (double p: levels) {
				columns.add(StatisticTools.formatPercent(p)+" "+Language.tr("Statistics.Quantil")+info);
			}
		}
		if (confidenceLevels!=null) for (double level: confidenceLevels) {
			columns.add(String.format(Language.tr("Statistics.ConfidenceLevel"),StatisticTools.formatPercent(1-level)));
		}

		return columns.toArray(new String[0]);
	}

	/**
	 * Erzeugt eine Datenzeile.
	 * @param col1	Inhalt f�r Spalte 1 (kann <code>null</code> sein)
	 * @param data	Statistikobjekt dem Mittelwert usw. entnommen werden sollen
	 * @param confidenceLevels	Niveaus zu denen Konfidenzintervallgr��en ausgegeben werden sollen (kann <code>null</code> sein)
	 * @return	Datenzeile
	 */
	private String[] getDataLine(final String col1, final StatisticsDataPerformanceIndicator data, final double[] confidenceLevels) {
		final List<String> line=new ArrayList<>();

		if (col1!=null) line.add(col1);

		line.add(NumberTools.formatLongNoGrouping(data.getCount()));
		line.add(StatisticTools.formatNumber(data.getMean()));
		line.add(StatisticTools.formatNumber(data.getSD()));
		line.add(StatisticTools.formatNumber(data.getVar()));
		line.add(StatisticTools.formatNumber(data.getCV()));
		line.add(StatisticTools.formatNumber(data.getMin()));
		line.add(StatisticTools.formatNumber(data.getMax()));

		if (SetupData.getSetup().showQuantils && data.getDistribution()!=null) {
			final double[] levels=StatisticViewerOverviewText.getQuantilLevels();
			for (double p: levels) {
				line.add(StatisticTools.formatNumber(data.getQuantil(p)));
			}
		}

		if (confidenceLevels!=null) {
			final double mean=data.getMean();
			final double[] halfWidth=data.getBatchMeanConfidenceHalfWide(confidenceLevels);
			for (int i=0;i<halfWidth.length;i++) line.add(String.format("[%s;%s]",StatisticTools.formatNumber(mean-halfWidth[i]),StatisticTools.formatNumber(mean+halfWidth[i])));
		}

		return line.toArray(new String[0]);
	}

	/**
	 * Erzeugt eine Datenzeile.
	 * @param col1	Inhalt f�r Spalte 1 (kann <code>null</code> sein)
	 * @param data	Statistikobjekt dem Mittelwert usw. entnommen werden sollen
	 * @return	Datenzeile
	 */
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

	/**
	 * Erzeugt eine Datenzeile.
	 * @param col1	Inhalt f�r Spalte 1 (kann <code>null</code> sein)
	 * @param data	Statistikobjekt dem Mittelwert usw. entnommen werden sollen
	 * @return	Datenzeile
	 */
	private String[] getDataLine(final String col1, final StatisticsTimePerformanceIndicator data) {
		final List<String> line=new ArrayList<>();

		if (col1!=null) line.add(col1);
		line.add(StatisticTools.formatNumber(data.getTimeMean()));
		line.add(StatisticTools.formatNumber(data.getTimeSD()));
		line.add(StatisticTools.formatNumber(data.getTimeVar()));
		line.add(StatisticTools.formatNumber(data.getTimeCV()));
		line.add(StatisticTools.formatNumber(data.getTimeMin()));
		line.add(StatisticTools.formatNumber(data.getTimeMax()));
		if (SetupData.getSetup().showQuantils) {
			final double[] levels=StatisticViewerOverviewText.getQuantilLevels();
			for (double p: levels) {
				line.add(StatisticTools.formatNumber(data.getQuantil(p)));
			}
		}

		return line.toArray(new String[0]);
	}

	/**
	 * Zeigt im Fu�bereich der Hilfeseite eine "Erkl�rung einblenden"-Schaltfl�che, die,
	 * wenn sie angeklickt wird, eine html-Hilfeseite anzeigt.
	 * @param topic	Hilfe-Thema (wird als Datei in den "description_*"-Ordern gesucht)
	 */
	private void addDescription(final String topic) {
		final URL url=StatisticViewerTimeTable.class.getResource("description_"+Language.getCurrentLanguage()+"/"+topic+".html");
		addDescription(url,helpTopic->Help.topic(getViewer(false),helpTopic));
	}

	/**
	 * Pr�ft, ob <b>alle</b> angegebenen Statistikobjekt �ber Konfidenzintervalldaten verf�gen.
	 * @param indicator1	Erstes Statistikobjekt
	 * @param indicator2	Zweites Statistikobjekt (kann <code>null</code> sein, dann gilt die Bedingung f�r dieses Objekt als erf�llt)
	 * @param indicator3	Drittes Statistikobjekt (kann <code>null</code> sein, dann gilt die Bedingung f�r dieses Objekt als erf�llt)
	 * @return	Liefert <code>true</code>, wenn alle �bergebenen (ungleich <code>null</code>) Objekte �ber Konfidenzintervalldaten verf�gen
	 */
	private boolean hasConfidence(final StatisticsMultiPerformanceIndicator indicator1, final StatisticsMultiPerformanceIndicator indicator2, final StatisticsMultiPerformanceIndicator indicator3) {
		String[] names=indicator1.getNames();
		if (names.length==0) return false;
		if (((StatisticsDataPerformanceIndicator)indicator1.get(names[0])).getBatchCount()<2) return false;

		if (indicator2!=null) {
			names=indicator2.getNames();
			if (names.length==0) return false;
			if (((StatisticsDataPerformanceIndicator)indicator2.get(names[0])).getBatchCount()<2) return false;
		}

		if (indicator3!=null) {
			names=indicator3.getNames();
			if (names.length==0) return false;
			if (((StatisticsDataPerformanceIndicator)indicator3.get(names[0])).getBatchCount()<2) return false;
		}

		return true;
	}

	/**
	 * Erstellt eine �bersichtstabelle �ber mehrere Zeit-Kenngr��en
	 * @param indicator1	Statistikobjekt 1
	 * @param indicator2	Statistikobjekt 2 (kann <code>null</code> sein)
	 * @param indicator3	Statistikobjekt 3 (kann <code>null</code> sein)
	 * @param indicator4	Statistikobjekt 4 (kann <code>null</code> sein)
	 * @param type1	Bezeichner f�r Spalte 1 f�r Statistikobjekt 1 (kann <code>null</code> sein)
	 * @param type2	Bezeichner f�r Spalte 1 f�r Statistikobjekt 2 (kann <code>null</code> sein)
	 * @param type3	Bezeichner f�r Spalte 1 f�r Statistikobjekt 3 (kann <code>null</code> sein)
	 * @param type4	Bezeichner f�r Spalte 1 f�r Statistikobjekt 4 (kann <code>null</code> sein)
	 * @param label	Bezeichner f�r Spalte 1 in der �berschriftenzeile
	 * @param isStationsList	Handelt es sich bei den Untereintr�gen der Statistikobjekte um Stationsnamen?
	 * @param isInterArrival	Handelt es sich um Zwischenankunftszeiten?
	 */
	private void buildTimesOverviewTable(final StatisticsMultiPerformanceIndicator indicator1, final StatisticsMultiPerformanceIndicator indicator2, final StatisticsMultiPerformanceIndicator indicator3, final StatisticsMultiPerformanceIndicator indicator4, final String type1, final String type2, final String type3, final String type4, final String label, final boolean isStationsList, final boolean isInterArrival) {
		final Table table=new Table();

		final boolean hasConfidence=hasConfidence(indicator1,indicator2,indicator3);
		final double[] confidenceLevels=StatisticViewerOverviewText.getConfidenceLevels();

		final String[] types=indicator1.getNames();

		for (String type : types) {
			StatisticsDataPerformanceIndicator data;
			data=(StatisticsDataPerformanceIndicator)(indicator1.get(type));
			final String typeName=isStationsList?fullStationName(type):type;
			table.addLine(getDataLine(((type1!=null && !type1.isEmpty())?(type1+" "):"")+typeName,data,confidenceLevels));
			if (indicator2!=null && type2!=null) {
				data=(StatisticsDataPerformanceIndicator)(indicator2.get(type));
				table.addLine(getDataLine(type2+" "+typeName,data,confidenceLevels));
			}
			if (indicator3!=null && type3!=null) {
				data=(StatisticsDataPerformanceIndicator)(indicator3.get(type));
				table.addLine(getDataLine(type3+" "+typeName,data,confidenceLevels));
			}
			if (indicator4!=null && type4!=null) {
				data=(StatisticsDataPerformanceIndicator)(indicator4.get(type));
				table.addLine(getDataLine(type4+" "+typeName,data,confidenceLevels));
			}
		}

		final String[] columnNames;
		if (isInterArrival) {
			columnNames=getColumnNames(label,Language.tr("Statistics.Number"),"[I]",hasConfidence?confidenceLevels:null);
		} else {
			columnNames=getColumnNames(label,Language.tr("Statistics.Number"),"[.]",hasConfidence?confidenceLevels:null);
		}
		setData(table,columnNames);

		/* Infotext  */
		if (isInterArrival) {
			addDescription("TableOverviewArrival");
		} else {
			addDescription("TableOverviewOther");
		}
	}

	/**
	 * Erstellt eine Verteilungstabelle.
	 * @param indicator	Statistikobjekt
	 * @param label	Spalten�berschrift �ber ersten Spalte
	 */
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
				final double s=sum.get(j);
				if (s==0.0) line.add("0%"); else line.add(StatisticTools.formatPercent(value/s,3));
			}
			table.addLine(line);
		}

		setData(table,headers);

		/* Infotext  */
		addDescription("TableTimeDistribution");
	}

	/**
	 * Erstellt eine �bersichtstabelle �ber Z�hlwerte-Verteilungen
	 * @param indicators	Statistikobjekt mit Daten zu mehreren Teil-Verteilungen
	 * @param system	Statistikobjekt f�r die systemweiten Daten (kann <code>null</code> sein)
	 * @param type	Kann "N" oder "NQ" sein und gibt an, welche Informationen unter der Tabelle als Hilfe eingeblendet werden sollen
	 */
	private void buildCountOverviewTable(final StatisticsMultiPerformanceIndicator indicators, final StatisticsTimePerformanceIndicator system, final String type) {
		final Table table=new Table();

		if (system!=null) {
			table.addLine(getDataLine(Language.tr("Statistics.System"),system));
		}

		for (String station: indicators.getNames()) {
			final StatisticsTimePerformanceIndicator indicator=(StatisticsTimePerformanceIndicator)(indicators.get(station));
			table.addLine(getDataLine(fullStationName(station),indicator));
		}

		setData(table,getColumnNames(Language.tr("Statistics.Station"),null,"["+type+"]",null));

		/* Infotext  */
		if (type.equals("N")) addDescription("TableCountOverviewN");
		if (type.equals("NQ")) addDescription("TableCountOverviewNQ");
	}

	/**
	 * Erstellt eine Verteilungstabelle �ber Z�hlwerte-Verteilungen
	 * @param indicators	Statistikobjekt mit Daten zu mehreren Teil-Verteilungen
	 * @param system	Statistikobjekt f�r die systemweiten Daten (kann <code>null</code> sein)
	 */
	private void buildCountDistributionTable(final StatisticsMultiPerformanceIndicator indicators, final StatisticsTimePerformanceIndicator system) {
		final Table table=new Table();
		final List<String> labels=new ArrayList<>();

		int max=0;
		if (system!=null) max=FastMath.max(max,system.getTimeMax());
		for (StatisticsTimePerformanceIndicator indicator : (StatisticsTimePerformanceIndicator[])indicators.getAll(StatisticsTimePerformanceIndicator.class)) max=FastMath.max(max,indicator.getTimeMax());

		labels.add(Language.tr("Statistics.State"));
		List<String> line=new ArrayList<>(max+1);
		for (int i=0;i<=max;i++) line.add(NumberTools.formatLongNoGrouping(i));
		table.addLine(line);

		if (system!=null) {
			labels.add(Language.tr("Statistics.System"));
			line=new ArrayList<>(max+1);
			final DataDistributionImpl dist=system.getReadOnlyDistribution();
			final double[] data=dist.densityData;
			final double sum=dist.sum();
			for (int i=0;i<=max;i++) line.add(StatisticTools.formatPercent((i>=data.length || sum==0)?0.0:data[i]/sum,3));
			table.addLine(line);
		}
		for (String name : indicators.getNames()) {
			labels.add(name);
			final StatisticsTimePerformanceIndicator indicator=(StatisticsTimePerformanceIndicator)(indicators.get(name));
			line=new ArrayList<>(max+1);
			final DataDistributionImpl dist=indicator.getReadOnlyDistribution();
			final double[] data=dist.densityData;
			final double sum=dist.sum();
			for (int i=0;i<=max;i++) line.add(StatisticTools.formatPercent((i>=data.length || sum==0)?0.0:data[i]/sum,3));
			table.addLine(line);
		}

		setData(table.transpose(),labels);

		/* Infotext  */
		addDescription("TableCountDistribution");
	}

	/**
	 * Ausgabe einer
	 * Tabelle mit den Anzahlen an Ank�nften an den Stationen.
	 * @see Mode#MODE_OVERVIEW_STATIONS_ARRIVAL_COUNT
	 */
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

	/**
	 * Ausgabe der
	 * Ressourcenauslastung (�bersichtstabelle)
	 * @see Mode#MODE_UTILIZATION
	 */
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

	/**
	 * Ausgabe von
	 * Ausfallzeiten der Ressourcen (�bersichtstabelle)
	 * @see Mode#MODE_DOWNTIMES
	 */
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

	/**
	 * Ausgabe der
	 * Transporterauslastung (�bersichtstabelle)
	 * @see Mode#MODE_TRANSPORTER_UTILIZATION
	 */
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

	/**
	 * Ausgabe der
	 * Ausfallzeiten der Transporter (�bersichtstabelle)
	 * @see Mode#MODE_TRANSPORTER_DOWNTIMES
	 */
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

	/**
	 * Ausgabe der
	 * Kundendatenfelder (�bersichtstabelle)
	 * @see Mode#MODE_CLIENT_DATA
	 */
	private void buildClientDataTable() {
		final Table table=new Table();

		final StatisticsMultiPerformanceIndicator indicators=statistics.clientData;
		for (String name : indicators.getNames()) {
			final StatisticsDataPerformanceIndicatorWithNegativeValues indicator=(StatisticsDataPerformanceIndicatorWithNegativeValues)(indicators.get(name));
			table.addLine(getDataLine(String.format(Language.tr("Statistics.ClientData.Field"),name),indicator));
		}
		setData(table,getColumnNames(Language.tr("Statistics.ClientData"),null,null,null));

		/* Infotext  */
		addDescription("TableClientData");
	}

	/**
	 * Ausgabe der
	 * Kundendatenfelder (Tabelle mit Verteilung der Werte)
	 * @see Mode#MODE_CLIENT_DATA_DISTRIBUTION
	 */
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

	/**
	 * Liefert eine {@link ModelElementRecord}-Station mit einem bestimmten Namen
	 * @param surface	Zeichenfl�che auf der und auf deren Unterzeichenfl�chen gesucht werden soll
	 * @param data	Name der Station
	 * @return	Liefert im Erfolgsfall das Stationsobjekt, sonst <code>null</code>
	 */
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

	/**
	 * Liefert, wenn vorhanden den y-Werte-Rechenausdruck (sonst x-Werte) aus einer {@link ModelElementRecord}-Station
	 * @param data	Name der Station
	 * @return	Rechenausdruck
	 */
	private String getExpressions(final String data) {
		final boolean value2=data.endsWith("-2");
		final String shortData=data.substring(0,data.length()-2);

		final ModelElementRecord record=getStation(statistics.editModel.surface,shortData);
		if (record==null) return null;
		return value2?record.getExpression2():record.getExpression1();
	}

	/**
	 * Ausgabe der
	 * Tabelle mit den an den Datenaufzeichnung-Stationen erfassten Werten
	 * @see Mode#MODE_VALUE_RECORDING
	 */
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
		case MODE_OVERVIEW_CLIENTS_INTERARRIVAL: buildTimesOverviewTable(statistics.clientsInterarrivalTime,null,null,null,null,null,null,null,Language.tr("Statistics.Station"),true,true); break;
		case MODE_OVERVIEW_CLIENTS_INTERLEAVE: buildTimesOverviewTable(statistics.clientsInterleavingTime,null,null,null,null,null,null,null,Language.tr("Statistics.ClientType"),true,false); break;
		case MODE_DISTRIBUTION_CLIENTS_INTERARRIVAL: buildTimesDistributionTable(statistics.clientsInterarrivalTime,Language.tr("Statistics.DistanceInSeconds")); break;
		case MODE_DISTRIBUTION_CLIENTS_INTERLEAVE: buildTimesDistributionTable(statistics.clientsInterleavingTime,Language.tr("Statistics.DistanceInSeconds")); break;
		case MODE_OVERVIEW_STATIONS_ARRIVAL_COUNT: buildInterarrivalCountTable(); break;
		case MODE_OVERVIEW_STATIONS_INTERARRIVAL: buildTimesOverviewTable(statistics.stationsInterarrivalTime,null,null,null,null,null,null,null,Language.tr("Statistics.Station"),true,true); break;
		case MODE_OVERVIEW_STATIONS_INTERARRIVAL_BATCH: buildTimesOverviewTable(statistics.stationsInterarrivalTimeBatch,null,null,null,null,null,null,null,Language.tr("Statistics.Station"),true,true); break;
		case MODE_OVERVIEW_STATIONS_INTERARRIVAL_CLIENTS: buildTimesOverviewTable(statistics.stationsInterarrivalTimeByClientType,null,null,null,null,null,null,null,Language.tr("Statistics.StationClient"),true,true); break;
		case MODE_OVERVIEW_STATIONS_INTERARRIVAL_STATES: buildTimesOverviewTable(statistics.stationsInterarrivalTimeByState,null,null,null,null,null,null,null,Language.tr("Statistics.StationState"),true,true); break;
		case MODE_OVERVIEW_STATIONS_INTERLEAVE: buildTimesOverviewTable(statistics.stationsInterleavingTime,null,null,null,null,null,null,null,Language.tr("Statistics.Station"),true,false); break;
		case MODE_OVERVIEW_STATIONS_INTERLEAVE_CLIENTS: buildTimesOverviewTable(statistics.stationsInterleavingTimeByClientType,null,null,null,null,null,null,null,Language.tr("Statistics.StationClient"),true,false); break;
		case MODE_DISTRIBUTION_STATIONS_INTERARRIVAL: buildTimesDistributionTable(statistics.stationsInterarrivalTime,Language.tr("Statistics.DistanceInSeconds")); break;
		case MODE_DISTRIBUTION_STATIONS_INTERARRIVAL_BATCH: buildTimesDistributionTable(statistics.stationsInterarrivalTimeBatch,Language.tr("Statistics.DistanceInSeconds")); break;
		case MODE_DISTRIBUTION_STATIONS_INTERARRIVAL_CLIENTS: buildTimesDistributionTable(statistics.stationsInterarrivalTimeByClientType,Language.tr("Statistics.DistanceInSeconds")); break;
		case MODE_DISTRIBUTION_STATIONS_INTERARRIVAL_STATES: buildTimesDistributionTable(statistics.stationsInterarrivalTimeByState,Language.tr("Statistics.DistanceInSeconds")); break;
		case MODE_DISTRIBUTION_STATIONS_INTERLEAVE: buildTimesDistributionTable(statistics.stationsInterleavingTime,Language.tr("Statistics.DistanceInSeconds")); break;
		case MODE_DISTRIBUTION_STATIONS_INTERLEAVE_CLIENTS: buildTimesDistributionTable(statistics.stationsInterleavingTimeByClientType,Language.tr("Statistics.DistanceInSeconds")); break;
		case MODE_OVERVIEW_CLIENTS_WAITINGPROCESSING: buildTimesOverviewTable(statistics.clientsWaitingTimes,statistics.clientsTransferTimes,statistics.clientsProcessingTimes,statistics.clientsResidenceTimes,Language.tr("Statistics.WaitingTime"),Language.tr("Statistics.TransferTime"),Language.tr("Statistics.ProcessTime"),Language.tr("Statistics.ResidenceTime"),Language.tr("Statistics.ClientType"),true,false); break;
		case MODE_DISTRIBUTION_CLIENTS_WAITING: buildTimesDistributionTable(statistics.clientsWaitingTimes,Language.tr("Statistics.Seconds")); break;
		case MODE_DISTRIBUTION_CLIENTS_TRANSFER: buildTimesDistributionTable(statistics.clientsTransferTimes,Language.tr("Statistics.Seconds")); break;
		case MODE_DISTRIBUTION_CLIENTS_PROCESSING: buildTimesDistributionTable(statistics.clientsProcessingTimes,Language.tr("Statistics.Seconds")); break;
		case MODE_DISTRIBUTION_CLIENTS_RESIDENCE: buildTimesDistributionTable(statistics.clientsResidenceTimes,Language.tr("Statistics.Seconds")); break;
		case MODE_OVERVIEW_STATIONSMODE_OVERVIEW_CLIENTS: buildTimesOverviewTable(statistics.stationsWaitingTimes,statistics.stationsTransferTimes,statistics.stationsProcessingTimes,statistics.stationsResidenceTimes,Language.tr("Statistics.WaitingTime"),Language.tr("Statistics.TransferTime"),Language.tr("Statistics.ProcessTime"),Language.tr("Statistics.ResidenceTime"),Language.tr("Statistics.Station"),true,false); break;
		case MODE_OVERVIEW_STATIONSCLIENTMODE_OVERVIEW_CLIENTS: buildTimesOverviewTable(statistics.stationsWaitingTimesByClientType,statistics.stationsTransferTimesByClientType,statistics.stationsProcessingTimesByClientType,statistics.stationsResidenceTimesByClientType,Language.tr("Statistics.WaitingTime"),Language.tr("Statistics.TransferTime"),Language.tr("Statistics.ProcessTime"),Language.tr("Statistics.ResidenceTime"),Language.tr("Statistics.StationClient"),true,false); break;
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

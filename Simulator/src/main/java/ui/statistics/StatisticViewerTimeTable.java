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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import language.Language;
import mathtools.NumberTools;
import mathtools.Table;
import mathtools.distribution.DataDistributionImpl;
import simulator.statistics.Statistics;
import statistics.StatisticsDataCollector;
import statistics.StatisticsDataPerformanceIndicator;
import statistics.StatisticsDataPerformanceIndicatorWithNegativeValues;
import statistics.StatisticsMultiPerformanceIndicator;
import statistics.StatisticsPerformanceIndicator;
import statistics.StatisticsSimpleCountPerformanceIndicator;
import statistics.StatisticsSimpleValueMaxPerformanceIndicator;
import statistics.StatisticsSimpleValuePerformanceIndicator;
import statistics.StatisticsTimeContinuousPerformanceIndicator;
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
import ui.tools.FlatLaFHelper;

/**
 * Dieser Viewer gibt die Verteilung der Zwischenakunfts-, Warte- und Bedienzeiten in Tabellenform zu den Simulationsergebnissen aus.
 * @see StatisticViewerTable
 * @author Alexander Herzog
 */
public class StatisticViewerTimeTable extends StatisticViewerBaseTable {
	/** Statistikobjekt, aus dem die anzuzeigenden Daten entnommen werden sollen */
	private final Statistics statistics;
	/** Gibt an, welche Daten genau ausgegeben werden sollen */
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
		/** Zwischenankunftszeiten an den Stationen auf Batch-Basis (Übersichtstabelle) */
		MODE_OVERVIEW_STATIONS_INTERARRIVAL_BATCH,
		/** Zwischenankunftszeiten an den Stationen nach Kundentypen weiter ausdifferenziert (Übersichtstabelle) */
		MODE_OVERVIEW_STATIONS_INTERARRIVAL_CLIENTS,
		/** Zwischenankunftszeiten an den Stationen nach Warteschlangenlänge weiter ausdifferenziert (Übersichtstabelle) */
		MODE_OVERVIEW_STATIONS_INTERARRIVAL_STATES,
		/** Zwischenabgangszeiten an den Stationen (Übersichtstabelle) */
		MODE_OVERVIEW_STATIONS_INTERLEAVE,
		/** Zwischenabgangszeiten an den Stationen auf Batch-Basis (Übersichtstabelle) */
		MODE_OVERVIEW_STATIONS_INTERLEAVE_BATCH,
		/** Zwischenabgangszeiten an den Stationen nach Kundentypen weiter ausdifferenziert (Übersichtstabelle) */
		MODE_OVERVIEW_STATIONS_INTERLEAVE_CLIENTS,
		/** Zwischenankunftszeiten an den Stationen (Tabelle mit Verteilung der Werte) */
		MODE_DISTRIBUTION_STATIONS_INTERARRIVAL,
		/** Zwischenankunftszeiten an den Stationen auf Batch-Basis (Tabelle mit Verteilung der Werte) */
		MODE_DISTRIBUTION_STATIONS_INTERARRIVAL_BATCH,
		/** Zwischenankunftszeiten an den Stationen nach Kundentypen weiter ausdifferenziert (Tabelle mit Verteilung der Werte) */
		MODE_DISTRIBUTION_STATIONS_INTERARRIVAL_CLIENTS,
		/** Zwischenankunftszeiten an den Stationen nach Warteschlangenlänge weiter ausdifferenziert (Tabelle mit Verteilung der Werte) */
		MODE_DISTRIBUTION_STATIONS_INTERARRIVAL_STATES,
		/** Zwischenabgangszeiten an den Stationen (Tabelle mit Verteilung der Werte) */
		MODE_DISTRIBUTION_STATIONS_INTERLEAVE,
		/** Zwischenabgangszeiten an den Stationen auf Batch-Basis (Tabelle mit Verteilung der Werte) */
		MODE_DISTRIBUTION_STATIONS_INTERLEAVE_BATCH,
		/** Zwischenabgangszeiten an den Stationen nach Kundentypen weiter ausdifferenziert (Tabelle mit Verteilung der Werte) */
		MODE_DISTRIBUTION_STATIONS_INTERLEAVE_CLIENTS,

		/** Warte- und Bedienzeiten der Kunden (Übersichtstabelle) */
		MODE_OVERVIEW_CLIENTS_WAITINGPROCESSING,
		/** Vergleich der Flussgrade zwischen den Kundentypen */
		MODE_FLOW_FACTOR_CLIENTS,
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
		/** Warte- und Bedienzeiten an den Stationen (Einzelzeiten der Kunden summiert, Übersichtstabelle) */
		MODE_OVERVIEW_STATIONSMODE_TOTAL_OVERVIEW_CLIENTS,
		/** Vergleich der Flussgrade zwischen den Stationen */
		MODE_FLOW_FACTOR_STATION,
		/** Warte- und Bedienzeiten an den Stationen nach Kundentypen ausdifferenziert (Übersichtstabelle) */
		MODE_OVERVIEW_STATIONSCLIENTMODE_OVERVIEW_CLIENTS,
		/** Vergleich der Flussgrade zwischen den Stationen (zusätzlich ausdifferenziert nach Kundentypen) */
		MODE_FLOW_FACTOR_STATION_CLIENT,
		/** Verteilung der Wartezeiten an den Stationen */
		MODE_DISTRIBUTION_STATIONS_WAITING,
		/** Verteilung der Transportzeiten an den Stationen */
		MODE_DISTRIBUTION_STATIONS_TRANSFER,
		/** Verteilung der Bedienzeiten an den Stationen */
		MODE_DISTRIBUTION_STATIONS_PROCESSING,
		/** Verteilung der Verweilzeiten an den Stationen */
		MODE_DISTRIBUTION_STATIONS_RESIDENCE,
		/** Verteilung der Rüstzeiten an den Stationen */
		MODE_DISTRIBUTION_STATIONS_SETUP,
		/** Verteilung der Wartezeiten an den Stationen (Einzelzeiten der Kunden summiert) */
		MODE_DISTRIBUTION_STATIONS_WAITING_TOTAL,
		/** Verteilung der Transportzeiten an den Stationen (Einzelzeiten der Kunden summiert) */
		MODE_DISTRIBUTION_STATIONS_TRANSFER_TOTAL,
		/** Verteilung der Bedienzeiten an den Stationen (Einzelzeiten der Kunden summiert) */
		MODE_DISTRIBUTION_STATIONS_PROCESSING_TOTAL,
		/** Verteilung der Verweilzeiten an den Stationen (Einzelzeiten der Kunden summiert) */
		MODE_DISTRIBUTION_STATIONS_RESIDENCE_TOTAL,
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
		/** Anzahl an Kunden im Bedienprozess an den Stationen (Übersichtstabelle) */
		MODE_OVERVIEW_PROCESS,
		/** Anzahl an Kunden im Bedienprozess an den Stationen nach Kundentypen (Übersichtstabelle) */
		MODE_OVERVIEW_PROCESS_CLIENT_TYPES,
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
		/** Verteilung der Anzahlen an Kunden in Bedienung an den Stationen */
		MODE_DISTRIBUTION_PROCESS,
		/** Verteilung der Anzahlen an Kunden in Bedienung an den Stationen nach Kundentypen */
		MODE_DISTRIBUTION_PROCESS_CLIENT_TYPE,

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

		/** Kundendatentextfelder (Übersichtstabelle) */
		MODE_CLIENT_TEXT_DATA,

		/** Tabelle mit den an den Datenaufzeichnung-Stationen erfassten Werten */
		MODE_VALUE_RECORDING,

		/** Statistik über die globalen Variablen */
		MODE_USER_VARIABLES,

		/** Ankünfte pro Thread */
		MODE_SYSTEM_INFO_THREAD_BALANCE
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
	 * Liefert die Spaltenüberschriften.
	 * @param col1	Optionale erste Spalte (kann <code>null</code> sein)
	 * @param col2	Optionale zweite Spalte (kann <code>null</code> sein)
	 * @param info	Optionaler Wert, der an die Indikatoren angehängt wird (kann <code>null</code> sein; üblich sowas wie "[X]")
	 * @param confidenceLevels	Konfidenzlevels für die Spalten vorgesehen werden solle (kann <code>null</code> sein)
	 * @return	Spaltenüberschriften
	 */
	private String[] getColumnNames(final String col1, final String col2, String info, final double[] confidenceLevels) {
		return getColumnNames(col1,col2,null,info,confidenceLevels);
	}

	/**
	 * Liefert die Spaltenüberschriften.
	 * @param col1	Optionale erste Spalte (kann <code>null</code> sein)
	 * @param col2	Optionale zweite Spalte (kann <code>null</code> sein)
	 * @param col3	Optionale dritte Spalte (kann <code>null</code> sein)
	 * @param info	Optionaler Wert, der an die Indikatoren angehängt wird (kann <code>null</code> sein; üblich sowas wie "[X]")
	 * @param confidenceLevels	Konfidenzlevels für die Spalten vorgesehen werden solle (kann <code>null</code> sein)
	 * @return	Spaltenüberschriften
	 */
	private String[] getColumnNames(final String col1, final String col2, final String col3, String info, final double[] confidenceLevels) {
		final List<String> columns=new ArrayList<>();

		if (col1!=null) columns.add(col1);
		if (col2!=null) columns.add(col2);
		if (col3!=null) columns.add(col3);

		if (info==null) info="";
		columns.add("E"+info);
		columns.add("Std"+info);
		columns.add("Var"+info);
		columns.add("CV"+info);
		columns.add("Sk"+info);
		columns.add("Kurt"+info);
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
	 * @param col1	Inhalt für Spalte 1 (kann <code>null</code> sein)
	 * @param data	Statistikobjekt dem Mittelwert usw. entnommen werden sollen
	 * @param confidenceLevels	Niveaus zu denen Konfidenzintervallgrößen ausgegeben werden sollen (kann <code>null</code> sein)
	 * @return	Datenzeile
	 */
	private String[] getDataLine(final String col1, final StatisticsDataPerformanceIndicator data, final double[] confidenceLevels) {
		return getDataLine(col1,null,data,confidenceLevels);
	}

	/**
	 * Erzeugt eine Datenzeile.
	 * @param col1	Inhalt für Spalte 1 (kann <code>null</code> sein)
	 * @param col2	Inhalt für Spalte 2 (kann <code>null</code> sein)
	 * @param data	Statistikobjekt dem Mittelwert usw. entnommen werden sollen
	 * @param confidenceLevels	Niveaus zu denen Konfidenzintervallgrößen ausgegeben werden sollen (kann <code>null</code> sein)
	 * @return	Datenzeile
	 */
	private String[] getDataLine(final String col1, final String col2, final StatisticsDataPerformanceIndicator data, final double[] confidenceLevels) {
		final List<String> line=new ArrayList<>();

		if (col1!=null) line.add(col1);
		if (col2!=null) line.add(col2);

		line.add(NumberTools.formatLongNoGrouping(data.getCount()));
		line.add(StatisticTools.formatNumber(data.getMean()));
		line.add(StatisticTools.formatNumber(data.getSD()));
		line.add(StatisticTools.formatNumber(data.getVar()));
		line.add(StatisticTools.formatNumber(data.getCV()));
		line.add(StatisticTools.formatNumber(data.getSk()));
		line.add(StatisticTools.formatNumber(data.getKurt()));
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
			double[] halfWidth=data.getBatchMeanConfidenceHalfWide(confidenceLevels);
			if (halfWidth.length==0) halfWidth=data.getRunConfidenceHalfWide(confidenceLevels);
			for (int i=0;i<halfWidth.length;i++) line.add(String.format("[%s;%s]",StatisticTools.formatNumber(mean-halfWidth[i]),StatisticTools.formatNumber(mean+halfWidth[i])));
		}

		return line.toArray(new String[0]);
	}

	/**
	 * Erzeugt eine Datenzeile.
	 * @param col1	Inhalt für Spalte 1 (kann <code>null</code> sein)
	 * @param col2	Inhalt für Spalte 2 (kann <code>null</code> sein)
	 * @param data	Statistikobjekt dem Mittelwert usw. entnommen werden sollen
	 * @return	Datenzeile
	 */
	private String[] getDataLine(final String col1, final String col2, final StatisticsDataPerformanceIndicatorWithNegativeValues data) {
		final List<String> line=new ArrayList<>();

		if (col1!=null) line.add(col1);
		if (col2!=null) line.add(col2);
		line.add(NumberTools.formatLongNoGrouping(data.getCount()));
		line.add(StatisticTools.formatNumber(data.getMean()));
		line.add(StatisticTools.formatNumber(data.getSD()));
		line.add(StatisticTools.formatNumber(data.getVar()));
		line.add(StatisticTools.formatNumber(data.getCV()));
		line.add(StatisticTools.formatNumber(data.getSk()));
		line.add(StatisticTools.formatNumber(data.getKurt()));
		line.add(StatisticTools.formatNumber(data.getMin()));
		line.add(StatisticTools.formatNumber(data.getMax()));
		if (SetupData.getSetup().showQuantils) for (double p: StatisticsDataPerformanceIndicatorWithNegativeValues.storeQuantilValues) {
			line.add(StatisticTools.formatNumber(data.getQuantil(p)));
		}

		return line.toArray(new String[0]);
	}

	/**
	 * Erzeugt eine Datenzeile.
	 * @param col1	Inhalt für Spalte 1 (kann <code>null</code> sein)
	 * @param data	Statistikobjekt dem Mittelwert usw. entnommen werden sollen
	 * @param confidenceLevels	Niveaus zu denen Konfidenzintervallgrößen ausgegeben werden sollen (kann <code>null</code> sein)
	 * @return	Datenzeile
	 */
	private String[] getDataLine(final String col1, final StatisticsTimePerformanceIndicator data, final double[] confidenceLevels) {
		final List<String> line=new ArrayList<>();

		if (col1!=null) line.add(col1);
		line.add(StatisticTools.formatNumber(data.getTimeMean()));
		line.add(StatisticTools.formatNumber(data.getTimeSD()));
		line.add(StatisticTools.formatNumber(data.getTimeVar()));
		line.add(StatisticTools.formatNumber(data.getTimeCV()));
		line.add(StatisticTools.formatNumber(data.getTimeSk()));
		line.add(StatisticTools.formatNumber(data.getTimeKurt()));
		line.add(StatisticTools.formatNumber(data.getTimeMin()));
		line.add(StatisticTools.formatNumber(data.getTimeMax()));
		if (SetupData.getSetup().showQuantils) {
			final double[] levels=StatisticViewerOverviewText.getQuantilLevels();
			for (double p: levels) {
				line.add(StatisticTools.formatNumber(data.getQuantil(p)));
			}
		}

		if (confidenceLevels!=null) {
			final double mean=data.getTimeMean();
			final double[] halfWidth=data.getRunConfidenceHalfWide(confidenceLevels);
			for (int i=0;i<halfWidth.length;i++) line.add(String.format("[%s;%s]",StatisticTools.formatNumber(mean-halfWidth[i]),StatisticTools.formatNumber(mean+halfWidth[i])));
		}

		return line.toArray(new String[0]);
	}

	/**
	 * Erzeugt eine Datenzeile.
	 * @param col1	Inhalt für Spalte 1 (kann <code>null</code> sein)
	 * @param data	Statistikobjekt dem Mittelwert usw. entnommen werden sollen
	 * @return	Datenzeile
	 */
	private String[] getDataLine(final String col1, final StatisticsTimeContinuousPerformanceIndicator data) {
		final List<String> line=new ArrayList<>();

		if (col1!=null) line.add(col1);
		line.add(StatisticTools.formatNumber(data.getTimeMean()));
		line.add(StatisticTools.formatNumber(data.getTimeSD()));
		line.add(StatisticTools.formatNumber(data.getTimeVar()));
		line.add(StatisticTools.formatNumber(data.getTimeCV()));
		line.add(StatisticTools.formatNumber(data.getTimeSk()));
		line.add(StatisticTools.formatNumber(data.getTimeKurt()));
		line.add(StatisticTools.formatNumber(data.getTimeMin()));
		line.add(StatisticTools.formatNumber(data.getTimeMax()));

		return line.toArray(new String[0]);
	}

	/**
	 * Zeigt im Fußbereich der Hilfeseite eine "Erklärung einblenden"-Schaltfläche, die,
	 * wenn sie angeklickt wird, eine html-Hilfeseite anzeigt.
	 * @param topic	Hilfe-Thema (wird als Datei in den "description_*"-Ordern gesucht)
	 */
	private void addDescription(final String topic) {
		final URL url=StatisticViewerTimeTable.class.getResource("description_"+Language.getCurrentLanguage()+"/"+topic+".html");
		addDescription(url,helpTopic->Help.topic(getViewer(false),helpTopic));
	}

	@Override
	protected String getDescriptionCustomStyles() {
		if (FlatLaFHelper.isDark()) return StatisticsPanel.DARK_MODE_DESACRIPTION_STYLE;
		return null;
	}

	/**
	 * Prüft, ob <b>alle</b> angegebenen Statistikobjekt über Konfidenzintervalldaten verfügen.
	 * @param indicator1	Erstes Statistikobjekt
	 * @param indicator2	Zweites Statistikobjekt (kann <code>null</code> sein, dann gilt die Bedingung für dieses Objekt als erfüllt)
	 * @param indicator3	Drittes Statistikobjekt (kann <code>null</code> sein, dann gilt die Bedingung für dieses Objekt als erfüllt)
	 * @return	Liefert <code>true</code>, wenn alle übergebenen (ungleich <code>null</code>) Objekte über Konfidenzintervalldaten verfügen
	 */
	private boolean hasConfidence(final StatisticsMultiPerformanceIndicator indicator1, final StatisticsMultiPerformanceIndicator indicator2, final StatisticsMultiPerformanceIndicator indicator3) {
		String[] names=indicator1.getNames();
		if (names.length==0) return false;
		final StatisticsDataPerformanceIndicator dataIndicator1=(StatisticsDataPerformanceIndicator)indicator1.get(names[0]);
		if (dataIndicator1.getBatchCount()<2 && (statistics.simulationData.runRepeatCount<2 || dataIndicator1.getRunCount()<2)) return false;

		if (indicator2!=null) {
			names=indicator2.getNames();
			if (names.length==0) return false;
			final StatisticsDataPerformanceIndicator dataIndicator2=(StatisticsDataPerformanceIndicator)indicator2.get(names[0]);
			if (dataIndicator2.getBatchCount()<2 && (statistics.simulationData.runRepeatCount<2 || dataIndicator2.getRunCount()<2)) return false;
		}

		if (indicator3!=null) {
			names=indicator3.getNames();
			if (names.length==0) return false;
			final StatisticsDataPerformanceIndicator dataIndicator3=(StatisticsDataPerformanceIndicator)indicator3.get(names[0]);
			if (dataIndicator3.getBatchCount()<2 && (statistics.simulationData.runRepeatCount<2 || dataIndicator3.getRunCount()<2)) return false;
		}

		return true;
	}

	/**
	 * Erstellt eine Übersichtstabelle über mehrere Zeit-Kenngrößen
	 * @param indicator1	Statistikobjekt 1
	 * @param indicator2	Statistikobjekt 2 (kann <code>null</code> sein)
	 * @param indicator3	Statistikobjekt 3 (kann <code>null</code> sein)
	 * @param indicator4	Statistikobjekt 4 (kann <code>null</code> sein)
	 * @param type1	Bezeichner für Spalte 1 für Statistikobjekt 1 (kann <code>null</code> sein)
	 * @param type2	Bezeichner für Spalte 1 für Statistikobjekt 2 (kann <code>null</code> sein)
	 * @param type3	Bezeichner für Spalte 1 für Statistikobjekt 3 (kann <code>null</code> sein)
	 * @param type4	Bezeichner für Spalte 1 für Statistikobjekt 4 (kann <code>null</code> sein)
	 * @param label	Bezeichner für Spalte 1 in der Überschriftenzeile
	 * @param isStationsList	Handelt es sich bei den Untereinträgen der Statistikobjekte um Stationsnamen?
	 * @param isInterArrival	Handelt es sich um Zwischenankunftszeiten?
	 * @param addThroughput	Sollen zu den Statistikobjekten Durchsatzwerte ausgegeben werden?
	 */
	private void buildTimesOverviewTable(final StatisticsMultiPerformanceIndicator indicator1, final StatisticsMultiPerformanceIndicator indicator2, final StatisticsMultiPerformanceIndicator indicator3, final StatisticsMultiPerformanceIndicator indicator4, final String type1, final String type2, final String type3, final String type4, final String label, final boolean isStationsList, final boolean isInterArrival, final boolean addThroughput) {
		final Table table=new Table();

		final boolean hasConfidence=hasConfidence(indicator1,indicator2,indicator3);
		final double[] confidenceLevels=StatisticViewerOverviewText.getConfidenceLevels();

		boolean hasMaxThroughput=false;

		final String[] types=indicator1.getNames();

		for (String type : types) {
			StatisticsDataPerformanceIndicator data;
			data=(StatisticsDataPerformanceIndicator)(indicator1.get(type));
			final String typeName=isStationsList?fullStationName(type):type;
			String[] line;
			if (type1!=null && !type1.isEmpty()) {
				line=getDataLine(type1,typeName,data,hasConfidence?confidenceLevels:null);
			} else {
				line=getDataLine(typeName,data,hasConfidence?confidenceLevels:null);
			}
			if (addThroughput) {
				line=Arrays.copyOf(line,line.length+2);
				final String[] throughput=StatisticViewerOverviewText.getThroughputColumns(data.getCount(),statistics);
				line[line.length-2]=throughput[0];
				line[line.length-1]=throughput[1];
				if (isStationsList) {
					final StatisticsSimpleValueMaxPerformanceIndicator maxThroughput=(StatisticsSimpleValueMaxPerformanceIndicator)statistics.stationsMaxThroughput.getOrNull(typeName);
					if (maxThroughput!=null && maxThroughput.get()>0) {
						line=Arrays.copyOf(line,line.length+2);
						final String[] maxThroughputCols=StatisticViewerOverviewText.getMaxThroughputColumns(maxThroughput.get());
						line[line.length-2]=maxThroughputCols[0];
						line[line.length-1]=maxThroughputCols[1];
						hasMaxThroughput=true;
					}
				}
			}
			table.addLine(line);
			if (indicator2!=null && type2!=null) {
				data=(StatisticsDataPerformanceIndicator)(indicator2.get(type));
				line=getDataLine(type2,typeName,data,hasConfidence?confidenceLevels:null);
				if (addThroughput) {
					line=Arrays.copyOf(line,line.length+2);
					final String[] throughput=StatisticViewerOverviewText.getThroughputColumns(data.getCount(),statistics);
					line[line.length-2]=throughput[0];
					line[line.length-1]=throughput[1];
					if (isStationsList) {
						final StatisticsSimpleValueMaxPerformanceIndicator maxThroughput=(StatisticsSimpleValueMaxPerformanceIndicator)statistics.stationsMaxThroughput.getOrNull(typeName);
						if (maxThroughput!=null && maxThroughput.get()>0) {
							line=Arrays.copyOf(line,line.length+2);
							final String[] maxThroughputCols=StatisticViewerOverviewText.getMaxThroughputColumns(maxThroughput.get());
							line[line.length-2]=maxThroughputCols[0];
							line[line.length-1]=maxThroughputCols[1];
							hasMaxThroughput=true;
						}
					}
				}
				table.addLine(line);
			}
			if (indicator3!=null && type3!=null) {
				data=(StatisticsDataPerformanceIndicator)(indicator3.get(type));
				line=getDataLine(type3,typeName,data,hasConfidence?confidenceLevels:null);
				if (addThroughput) {
					line=Arrays.copyOf(line,line.length+2);
					final String[] throughput=StatisticViewerOverviewText.getThroughputColumns(data.getCount(),statistics);
					line[line.length-2]=throughput[0];
					line[line.length-1]=throughput[1];
					if (isStationsList) {
						final StatisticsSimpleValueMaxPerformanceIndicator maxThroughput=(StatisticsSimpleValueMaxPerformanceIndicator)statistics.stationsMaxThroughput.getOrNull(typeName);
						if (maxThroughput!=null && maxThroughput.get()>0) {
							line=Arrays.copyOf(line,line.length+2);
							final String[] maxThroughputCols=StatisticViewerOverviewText.getMaxThroughputColumns(maxThroughput.get());
							line[line.length-2]=maxThroughputCols[0];
							line[line.length-1]=maxThroughputCols[1];
							hasMaxThroughput=true;
						}
					}
				}
				table.addLine(line);
			}
			if (indicator4!=null && type4!=null) {
				data=(StatisticsDataPerformanceIndicator)(indicator4.get(type));
				line=getDataLine(type4,typeName,data,hasConfidence?confidenceLevels:null);
				if (addThroughput) {
					line=Arrays.copyOf(line,line.length+2);
					final String[] throughput=StatisticViewerOverviewText.getThroughputColumns(data.getCount(),statistics);
					line[line.length-2]=throughput[0];
					line[line.length-1]=throughput[1];
					if (isStationsList) {
						final StatisticsSimpleValueMaxPerformanceIndicator maxThroughput=(StatisticsSimpleValueMaxPerformanceIndicator)statistics.stationsMaxThroughput.getOrNull(typeName);
						if (maxThroughput!=null && maxThroughput.get()>0) {
							line=Arrays.copyOf(line,line.length+2);
							final String[] maxThroughputCols=StatisticViewerOverviewText.getMaxThroughputColumns(maxThroughput.get());
							line[line.length-2]=maxThroughputCols[0];
							line[line.length-1]=maxThroughputCols[1];
							hasMaxThroughput=true;
						}
					}
				}
				table.addLine(line);
			}
		}

		String[] columnNames;
		if (isInterArrival) {
			if (type1!=null && !type1.isEmpty()) {
				columnNames=getColumnNames(Language.tr("Statistics.Type"),label,Language.tr("Statistics.Number"),"[I]",hasConfidence?confidenceLevels:null);
			} else {
				columnNames=getColumnNames(label,Language.tr("Statistics.Number"),"[I]",hasConfidence?confidenceLevels:null);
			}
		} else {
			if (type1!=null && !type1.isEmpty()) {
				columnNames=getColumnNames(Language.tr("Statistics.Type"),label,Language.tr("Statistics.Number"),"[.]",hasConfidence?confidenceLevels:null);
			} else {
				columnNames=getColumnNames(label,Language.tr("Statistics.Number"),"[.]",hasConfidence?confidenceLevels:null);
			}
		}
		if (addThroughput) {
			columnNames=Arrays.copyOf(columnNames,columnNames.length+2);
			columnNames[columnNames.length-2]=Language.tr("Statistics.Throughput");
			columnNames[columnNames.length-1]=Language.tr("Statistics.ThroughputUnit");
			if (hasMaxThroughput) {
				columnNames=Arrays.copyOf(columnNames,columnNames.length+2);
				columnNames[columnNames.length-2]=Language.tr("Statistics.Throughput.Maximum");
				columnNames[columnNames.length-1]=Language.tr("Statistics.ThroughputUnit.Maximum");
			}
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
	 * Erstellt eine Tabelle mit Flussgraden (=Verweilzeit/Bedienzeit)
	 * @param indicatorProcessing	Bedienzeiten
	 * @param indicatorResidence	Verweilzeiten
	 * @param label	Beschriftung der Namens-Spalte
	 */
	private void buildFlowFactorTable(final StatisticsMultiPerformanceIndicator indicatorProcessing, final StatisticsMultiPerformanceIndicator indicatorResidence, final String label) {
		final Table table=new Table();
		final String[] types=indicatorProcessing.getNames();

		for (String type : types) {
			final StatisticsDataPerformanceIndicator processing=(StatisticsDataPerformanceIndicator)indicatorProcessing.get(type);
			final StatisticsDataPerformanceIndicator residence=(StatisticsDataPerformanceIndicator)indicatorResidence.get(type);
			if (processing.getMean()==0) continue;
			final double flowFactor=residence.getMean()/processing.getMean();
			table.addLine(new String[] {type,StatisticTools.formatNumber(flowFactor)});
		}

		setData(table,new String[] {label, Language.tr("Statistics.FlowFactor")});

		/* Infotext  */
		addDescription("TableFlowFactor");
	}

	/**
	 * Erstellt eine Verteilungstabelle.
	 * @param indicator	Statistikobjekt
	 * @param label	Spaltenüberschrift über ersten Spalte
	 */
	private void buildTimesDistributionTable(final StatisticsMultiPerformanceIndicator indicator, final String label) {
		final Table table=new Table();
		final List<String> headers=new ArrayList<>();

		final String[] types=indicator.getNames();

		double scale=1;
		headers.add(label);
		List<DataDistributionImpl> dists=new ArrayList<>();
		for (String type: types) {
			headers.add(Language.tr("Statistics.NumberOfClients")+" - "+type);
			headers.add(Language.tr("Statistics.PartOfClients")+" - "+type);
			final DataDistributionImpl dist=((StatisticsDataPerformanceIndicator)(indicator.get(type))).getDistribution();
			if (dist!=null) {
				dists.add(dist);
				scale=dist.upperBound/dist.densityData.length;
			}
		}

		final List<Double> sum=new ArrayList<>();
		for (DataDistributionImpl dist: dists) sum.add(dist.sum());

		if (dists.size()>0)	for (int i=0;i<dists.get(0).densityData.length;i++) {
			List<String> line=new ArrayList<>();
			line.add(NumberTools.formatLongNoGrouping(Math.round(i*scale)));
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
	 * Erstellt eine Übersichtstabelle über Zählwerte-Verteilungen
	 * @param indicators	Statistikobjekt mit Daten zu mehreren Teil-Verteilungen
	 * @param system	Statistikobjekt für die systemweiten Daten (kann <code>null</code> sein)
	 * @param type	Kann "N" oder "NQ" sein und gibt an, welche Informationen unter der Tabelle als Hilfe eingeblendet werden sollen
	 */
	private void buildCountOverviewTable(final StatisticsMultiPerformanceIndicator indicators, final StatisticsTimePerformanceIndicator system, final String type) {
		final Table table=new Table();

		boolean hasConfidence=(statistics.simulationData.runRepeatCount>1);
		if (system!=null) {
			if (system.getRunCount()<2) hasConfidence=false;
		}

		if (hasConfidence) for (StatisticsPerformanceIndicator indicator: indicators.getAll()) {
			if (((StatisticsTimePerformanceIndicator)indicator).getRunCount()<2) {hasConfidence=false; break;}
		}

		final double[] confidenceLevels=StatisticViewerOverviewText.getConfidenceLevels();

		if (system!=null) {
			hasConfidence=(statistics.simulationData.runRepeatCount>1 && system.getRunCount()>1);
			table.addLine(getDataLine(Language.tr("Statistics.System"),system,hasConfidence?confidenceLevels:null));
		}

		for (String station: indicators.getNames()) {
			final StatisticsTimePerformanceIndicator indicator=(StatisticsTimePerformanceIndicator)(indicators.get(station));
			hasConfidence=(statistics.simulationData.runRepeatCount>1 && indicator.getRunCount()>1);
			table.addLine(getDataLine(fullStationName(station),indicator,hasConfidence?confidenceLevels:null));
		}

		setData(table,getColumnNames(Language.tr("Statistics.Station"),null,"["+type+"]",hasConfidence?confidenceLevels:null));

		/* Infotext  */
		if (type.equals("N")) addDescription("TableCountOverviewN");
		if (type.equals("NQ")) addDescription("TableCountOverviewNQ");
		if (type.equals("NS")) addDescription("TableCountOverviewNS");
	}

	/**
	 * Erstellt eine Verteilungstabelle über Zählwerte-Verteilungen
	 * @param indicators	Statistikobjekt mit Daten zu mehreren Teil-Verteilungen
	 * @param system	Statistikobjekt für die systemweiten Daten (kann <code>null</code> sein)
	 */
	private void buildCountDistributionTable(final StatisticsMultiPerformanceIndicator indicators, final StatisticsTimePerformanceIndicator system) {
		final Table table=new Table();
		final List<String> labels=new ArrayList<>();

		int max=0;
		if (system!=null) max=Math.max(max,Math.min(system.getTimeMax(),system.maxState));
		for (StatisticsTimePerformanceIndicator indicator : (StatisticsTimePerformanceIndicator[])indicators.getAll(StatisticsTimePerformanceIndicator.class)) max=Math.max(max,Math.min(indicator.getTimeMax(),indicator.maxState));

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
	 * Tabelle mit den Anzahlen an Ankünften an den Stationen.
	 * @see Mode#MODE_OVERVIEW_STATIONS_ARRIVAL_COUNT
	 */
	private void buildInterarrivalCountTable() {
		final Table table=new Table();

		long arrivalSum=0;
		for (String type : statistics.clientsInterarrivalTime.getNames()) {
			arrivalSum+=((StatisticsDataPerformanceIndicator)(statistics.clientsInterarrivalTime.get(type))).getCount();
		}

		boolean hasMaxThroughput=false;

		for (String station : statistics.stationsInterarrivalTime.getNames()) {
			final StatisticsDataPerformanceIndicator indicator=(StatisticsDataPerformanceIndicator)(statistics.stationsInterarrivalTime.get(station));
			long count=indicator.getCount();
			String part="";
			if (arrivalSum>0) part=StatisticTools.formatPercent(((double)count)/arrivalSum);
			final String[] throughput=StatisticViewerOverviewText.getThroughputColumns(count,statistics);

			if (count<=0) continue;

			final StatisticsSimpleValueMaxPerformanceIndicator maxThroughput=(StatisticsSimpleValueMaxPerformanceIndicator)statistics.stationsMaxThroughput.getOrNull(station);
			if (maxThroughput!=null && maxThroughput.get()>0) {
				final String[] maxThroughputCols=StatisticViewerOverviewText.getMaxThroughputColumns(maxThroughput.get());
				table.addLine(new String[]{
						fullStationName(station),
						NumberTools.formatLongNoGrouping(count),
						part,
						throughput[0],
						throughput[1],
						maxThroughputCols[0],
						maxThroughputCols[1]
				});
				hasMaxThroughput=true;
			} else {
				table.addLine(new String[]{
						fullStationName(station),
						NumberTools.formatLongNoGrouping(count),
						part,
						throughput[0],
						throughput[1]
				});
			}
		}

		final String[] header;
		if (hasMaxThroughput) {
			header=new String[]{Language.tr("Statistics.Station"),Language.tr("Statistics.Number"),Language.tr("Statistics.Part"),Language.tr("Statistics.Throughput"),Language.tr("Statistics.ThroughputUnit"),Language.tr("Statistics.Throughput.Maximum"),Language.tr("Statistics.ThroughputUnit.Maximum")};
		} else {
			header=new String[]{Language.tr("Statistics.Station"),Language.tr("Statistics.Number"),Language.tr("Statistics.Part"),Language.tr("Statistics.Throughput"),Language.tr("Statistics.ThroughputUnit")};
		}

		setData(table,header);

		/* Infotext  */
		addDescription("TableInterarrivalCount");
	}

	/**
	 * Ausgabe der
	 * Ressourcenauslastung (Übersichtstabelle)
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
					final StatisticsSimpleValuePerformanceIndicator rhoObj=(StatisticsSimpleValuePerformanceIndicator)statistics.resourceRho.getOrNull(resource);
					if (rhoObj==null) {
						line.add("-");
					} else {
						line.add(StatisticTools.formatPercent(rhoObj.get()));
					}
				} else {
					line.add("-");
					line.add("-");
				}
			}
			line.add(StatisticTools.formatNumber(indicator.getTimeMean()));
			line.add(StatisticTools.formatNumber(indicator.getTimeSD()));
			line.add(StatisticTools.formatNumber(indicator.getTimeVar()));
			line.add(StatisticTools.formatNumber(indicator.getTimeCV()));
			line.add(StatisticTools.formatNumber(indicator.getTimeSk()));
			line.add(StatisticTools.formatNumber(indicator.getTimeKurt()));
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
				"Sk",
				"Kurt",
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
	 * Ausfallzeiten der Ressourcen (Übersichtstabelle)
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
			line.add(StatisticTools.formatNumber(indicator.getTimeSk()));
			line.add(StatisticTools.formatNumber(indicator.getTimeKurt()));
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
				"Sk",
				"Kurt",
				"Min",
				"Max",
		});

		/* Infotext  */
		addDescription("TableResourceDownTimes");
	}

	/**
	 * Ausgabe der
	 * Transporterauslastung (Übersichtstabelle)
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
			line.add(StatisticTools.formatNumber(indicator.getTimeSk()));
			line.add(StatisticTools.formatNumber(indicator.getTimeKurt()));
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
				"Sk",
				"Kurt",
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
	 * Ausfallzeiten der Transporter (Übersichtstabelle)
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
			line.add(StatisticTools.formatNumber(indicator.getTimeSk()));
			line.add(StatisticTools.formatNumber(indicator.getTimeKurt()));
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
				"Sk",
				"Kurt",
				"Min",
				"Max",
		});

		/* Infotext  */
		addDescription("TableTransporterDownTimes");
	}

	/**
	 * Ausgabe der
	 * Kundendatenfelder (Übersichtstabelle)
	 * @see Mode#MODE_CLIENT_DATA
	 */
	private void buildClientDataTable() {
		final Table table=new Table();

		final StatisticsMultiPerformanceIndicator indicators=statistics.clientData;
		for (String name : indicators.getNames()) {
			final StatisticsDataPerformanceIndicatorWithNegativeValues indicator=(StatisticsDataPerformanceIndicatorWithNegativeValues)(indicators.get(name));
			table.addLine(getDataLine(Language.tr("Statistics.ClientData.global"),String.format(Language.tr("Statistics.ClientData.Field"),name),indicator));
		}

		final StatisticsMultiPerformanceIndicator indicatorsByClientTypes=statistics.clientDataByClientTypes;
		final Set<String> clientTypes=new HashSet<>();
		for (String name : indicatorsByClientTypes.getNames()) {
			final int index=name.indexOf("-");
			if (index>0) clientTypes.add(name.substring(index+1));
		}
		for (String clientName: clientTypes.stream().sorted().toArray(String[]::new)) {
			for (String name : indicatorsByClientTypes.getNames()) {
				final int index=name.indexOf("-");
				if (index<=0) continue;
				final String testName=name.substring(index+1);
				if (testName.equalsIgnoreCase(clientName)) {
					final StatisticsDataPerformanceIndicatorWithNegativeValues indicator=(StatisticsDataPerformanceIndicatorWithNegativeValues)(indicatorsByClientTypes.get(name));
					table.addLine(getDataLine(clientName,String.format(Language.tr("Statistics.ClientData.Field"),name.substring(0,index)),indicator));
				}
			}
		}

		setData(table,getColumnNames(Language.tr("Statistics.ClientType"),Language.tr("Statistics.ClientData"),null,null));

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

		/* Überschriften zusammenstellen */

		double scale=1;

		headers.add(Language.tr("Statistics.Value"));

		List<DataDistributionImpl> dists=new ArrayList<>();

		for (String name: statistics.clientData.getNames()) {
			final String field=String.format(Language.tr("Statistics.ClientData.Field"),name);
			headers.add(Language.tr("Statistics.Number")+" - "+field);
			headers.add(Language.tr("Statistics.Part")+" - "+field);
			final DataDistributionImpl dist=((StatisticsDataPerformanceIndicatorWithNegativeValues)(statistics.clientData.get(name))).getDistribution();
			if (dist!=null) {
				dists.add(dist);
				scale=dist.upperBound/dist.densityData.length;
			}
		}

		final StatisticsMultiPerformanceIndicator indicatorsByClientTypes=statistics.clientDataByClientTypes;
		final Set<String> clientTypes=new HashSet<>();
		for (String name : indicatorsByClientTypes.getNames()) {
			final int index=name.indexOf("-");
			if (index>0) clientTypes.add(name.substring(index+1));
		}

		for (String clientName: clientTypes.stream().sorted().toArray(String[]::new)) {
			for (String name : indicatorsByClientTypes.getNames()) {
				final int index=name.indexOf("-");
				if (index<=0) continue;
				final String testName=name.substring(index+1);
				if (testName.equalsIgnoreCase(clientName)) {

					final String field=clientName+" - "+String.format(Language.tr("Statistics.ClientData.Field"),name.substring(0,index));
					headers.add(Language.tr("Statistics.Number")+" - "+field);
					headers.add(Language.tr("Statistics.Part")+" - "+field);
					final DataDistributionImpl dist=((StatisticsDataPerformanceIndicatorWithNegativeValues)(indicatorsByClientTypes.get(name))).getDistribution();
					if (dist!=null) {
						dists.add(dist);
						scale=dist.upperBound/dist.densityData.length;
					}
				}
			}
		}

		/* Daten ausgeben */

		final List<Double> sum=new ArrayList<>();
		for (DataDistributionImpl dist: dists) sum.add(dist.sum());

		if (dists.size()>0)	for (int i=0;i<dists.get(0).densityData.length;i++) {
			List<String> line=new ArrayList<>();
			line.add(NumberTools.formatLongNoGrouping(Math.round(i*scale)));
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
	 * Ausgabe der
	 * Kundendatentextfelder (Übersichtstabelle)
	 * @see Mode#MODE_CLIENT_TEXT_DATA
	 */
	private void buildClientDataTextTable() {
		final Table table=new Table();

		String[] names;
		Set<String> clientTypes;
		Set<String> keys;

		/* Daten über alles */

		keys=new HashSet<>();
		names=statistics.clientTextData.getNames();
		for (String name: names) {
			final String[] parts=name.split("-",2);
			if (parts.length==2) keys.add(parts[0]);
		}
		for (String key: keys.stream().sorted().toArray(String[]::new)) {
			long sum=0;
			for (String name: names) {
				final String[] parts=name.split("-",2);
				if (parts.length!=2) continue;
				if (!parts[0].equals(key)) continue;
				final StatisticsSimpleCountPerformanceIndicator indicator=(StatisticsSimpleCountPerformanceIndicator)statistics.clientTextData.get(name);
				sum+=indicator.get();
			}
			final String[] line=new String[5];
			line[0]=Language.tr("Statistics.TotalBig");
			line[1]=key;
			for (String name: names) {
				final String[] parts=name.split("-",2);
				if (parts.length!=2) continue;
				if (!parts[0].equals(key)) continue;
				final StatisticsSimpleCountPerformanceIndicator indicator=(StatisticsSimpleCountPerformanceIndicator)statistics.clientTextData.get(name);
				final long value=indicator.get();
				line[2]=parts[1];
				line[3]=NumberTools.formatLongNoGrouping(value);
				line[4]=StatisticTools.formatPercent(((double)value)/sum);
				table.addLine(line);
			}
		}

		/* Daten pro Kundentyp */

		clientTypes=new HashSet<>();
		names=statistics.clientTextDataByClientTypes.getNames();
		for (String name: names) {
			final String[] parts=name.split("-",3);
			if (parts.length==3) clientTypes.add(parts[0]);
		}

		for (String clientType: clientTypes.stream().sorted().toArray(String[]::new)) {
			final String[] line=new String[5];
			line[0]=clientType;

			keys=new HashSet<>();
			for (String name: names) {
				final String[] parts=name.split("-",3);
				if (parts.length==3 && parts[0].equals(clientType)) keys.add(parts[1]);
			}
			for (String key: keys.stream().sorted().toArray(String[]::new)) {
				long sum=0;
				for (String name: names) {
					final String[] parts=name.split("-",3);
					if (parts.length!=3) continue;
					if (!parts[0].equals(clientType)) continue;
					if (!parts[1].equals(key)) continue;
					final StatisticsSimpleCountPerformanceIndicator indicator=(StatisticsSimpleCountPerformanceIndicator)statistics.clientTextDataByClientTypes.get(name);
					sum+=indicator.get();
				}
				line[1]=key;
				for (String name: names) {
					final String[] parts=name.split("-",3);
					if (parts.length!=3) continue;
					if (!parts[0].equals(clientType)) continue;
					if (!parts[1].equals(key)) continue;
					final StatisticsSimpleCountPerformanceIndicator indicator=(StatisticsSimpleCountPerformanceIndicator)statistics.clientTextDataByClientTypes.get(name);
					final long value=indicator.get();
					line[2]=parts[2];
					line[3]=NumberTools.formatLongNoGrouping(value);
					line[4]=StatisticTools.formatPercent(((double)value)/sum);
					table.addLine(line);
				}
			}
		}

		setData(table,new String[] {
				Language.tr("Statistics.ClientType"),
				Language.tr("Statistics.ClientTextData.Key"),
				Language.tr("Statistics.ClientTextData.Value"),
				Language.tr("Statistics.ClientTextData.Frequency.absolute"),
				Language.tr("Statistics.ClientTextData.Frequency.relative")
		});
	}

	/**
	 * Liefert eine {@link ModelElementRecord}-Station mit einem bestimmten Namen
	 * @param surface	Zeichenfläche auf der und auf deren Unterzeichenflächen gesucht werden soll
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

	/**
	 * Ausgabe von
	 * Statistik über die globalen Variablen
	 * @see Mode#MODE_USER_VARIABLES
	 */
	private void buildUserVariablesTable() {
		final Table table=new Table();
		final List<String> headers=new ArrayList<>();
		headers.add(Language.tr("Statistics.XML.StateTime.Name"));
		headers.add("E");
		headers.add("Std");
		headers.add("Var");
		headers.add("CV");
		headers.add("Sk");
		headers.add("Kurt");
		headers.add("Min");
		headers.add("Max");

		for (String name: statistics.userVariables.getNames()) {
			final StatisticsTimeContinuousPerformanceIndicator indicator=(StatisticsTimeContinuousPerformanceIndicator)(statistics.userVariables.get(name));
			table.addLine(getDataLine(name,indicator));
		}

		setData(table,headers);

		/* Infotext  */
		addDescription("Variables");
	}

	/**
	 * Ausgabe von
	 * Ankünfte pro Thread
	 * @see Mode#MODE_SYSTEM_INFO_THREAD_BALANCE
	 */
	private void buildThreadBalanceInfoTable() {
		final long[] data=statistics.simulationData.threadDynamicBalanceData;
		final int[] dataTimes=statistics.simulationData.threadRunTimes;
		final boolean hasTimes=(dataTimes!=null && dataTimes.length>=data.length);

		final Table table=new Table();
		final List<String> headers=new ArrayList<>();
		headers.add(Language.tr("Statistics.SystemData.ThreadBalance.Thread"));
		headers.add(Language.tr("Statistics.SystemData.ThreadBalance.NumberOfArrivals"));
		headers.add(Language.tr("Statistics.Part"));
		headers.add(Language.tr("Statistics.SystemData.ThreadBalance.DeviationFromAverage"));
		if (hasTimes) headers.add(Language.tr("Statistics.SystemData.ThreadBalance.Runtime"));

		long sum=0;
		for (long value: data) sum+=value;
		final long mean=sum/data.length;
		final String[] line=new String[hasTimes?5:4];
		for (int i=0;i<data.length;i++) {
			line[0]=""+(i+1);
			line[1]=NumberTools.formatLongNoGrouping(data[i]);
			line[2]=StatisticTools.formatPercent(((double)data[i])/sum);
			line[3]=NumberTools.formatLongNoGrouping(data[i]-mean);
			if (hasTimes && dataTimes!=null) line[4]=NumberTools.formatNumber(dataTimes[i]/1000.0,3);
			table.addLine(line);
		}

		setData(table,headers);

		/* Infotext  */
		addDescription("ThreadBalance");
	}

	@Override
	protected void buildTable() {
		switch (mode) {
		case MODE_OVERVIEW_CLIENTS_INTERARRIVAL: buildTimesOverviewTable(statistics.clientsInterarrivalTime,null,null,null,null,null,null,null,Language.tr("Statistics.Station"),true,true,false); break;
		case MODE_OVERVIEW_CLIENTS_INTERLEAVE: buildTimesOverviewTable(statistics.clientsInterleavingTime,null,null,null,null,null,null,null,Language.tr("Statistics.ClientType"),true,false,false); break;
		case MODE_DISTRIBUTION_CLIENTS_INTERARRIVAL: buildTimesDistributionTable(statistics.clientsInterarrivalTime,Language.tr("Statistics.DistanceInSeconds")); break;
		case MODE_DISTRIBUTION_CLIENTS_INTERLEAVE: buildTimesDistributionTable(statistics.clientsInterleavingTime,Language.tr("Statistics.DistanceInSeconds")); break;
		case MODE_OVERVIEW_STATIONS_ARRIVAL_COUNT: buildInterarrivalCountTable(); break;
		case MODE_OVERVIEW_STATIONS_INTERARRIVAL: buildTimesOverviewTable(statistics.stationsInterarrivalTime,null,null,null,null,null,null,null,Language.tr("Statistics.Station"),true,true,true); break;
		case MODE_OVERVIEW_STATIONS_INTERARRIVAL_BATCH: buildTimesOverviewTable(statistics.stationsInterarrivalTimeBatch,null,null,null,null,null,null,null,Language.tr("Statistics.Station"),true,true,true); break;
		case MODE_OVERVIEW_STATIONS_INTERARRIVAL_CLIENTS: buildTimesOverviewTable(statistics.stationsInterarrivalTimeByClientType,null,null,null,null,null,null,null,Language.tr("Statistics.StationClient"),true,true,true); break;
		case MODE_OVERVIEW_STATIONS_INTERARRIVAL_STATES: buildTimesOverviewTable(statistics.stationsInterarrivalTimeByState,null,null,null,null,null,null,null,Language.tr("Statistics.StationState"),true,true,false); break;
		case MODE_OVERVIEW_STATIONS_INTERLEAVE: buildTimesOverviewTable(statistics.stationsInterleavingTime,null,null,null,null,null,null,null,Language.tr("Statistics.Station"),true,false,false); break;
		case MODE_OVERVIEW_STATIONS_INTERLEAVE_BATCH: buildTimesOverviewTable(statistics.stationsInterleavingTimeBatch,null,null,null,null,null,null,null,Language.tr("Statistics.Station"),true,false,false); break;
		case MODE_OVERVIEW_STATIONS_INTERLEAVE_CLIENTS: buildTimesOverviewTable(statistics.stationsInterleavingTimeByClientType,null,null,null,null,null,null,null,Language.tr("Statistics.StationClient"),true,false,false); break;
		case MODE_DISTRIBUTION_STATIONS_INTERARRIVAL: buildTimesDistributionTable(statistics.stationsInterarrivalTime,Language.tr("Statistics.DistanceInSeconds")); break;
		case MODE_DISTRIBUTION_STATIONS_INTERARRIVAL_BATCH: buildTimesDistributionTable(statistics.stationsInterarrivalTimeBatch,Language.tr("Statistics.DistanceInSeconds")); break;
		case MODE_DISTRIBUTION_STATIONS_INTERARRIVAL_CLIENTS: buildTimesDistributionTable(statistics.stationsInterarrivalTimeByClientType,Language.tr("Statistics.DistanceInSeconds")); break;
		case MODE_DISTRIBUTION_STATIONS_INTERARRIVAL_STATES: buildTimesDistributionTable(statistics.stationsInterarrivalTimeByState,Language.tr("Statistics.DistanceInSeconds")); break;
		case MODE_DISTRIBUTION_STATIONS_INTERLEAVE: buildTimesDistributionTable(statistics.stationsInterleavingTime,Language.tr("Statistics.DistanceInSeconds")); break;
		case MODE_DISTRIBUTION_STATIONS_INTERLEAVE_BATCH: buildTimesDistributionTable(statistics.stationsInterleavingTimeBatch,Language.tr("Statistics.DistanceInSeconds")); break;
		case MODE_DISTRIBUTION_STATIONS_INTERLEAVE_CLIENTS: buildTimesDistributionTable(statistics.stationsInterleavingTimeByClientType,Language.tr("Statistics.DistanceInSeconds")); break;
		case MODE_OVERVIEW_CLIENTS_WAITINGPROCESSING: buildTimesOverviewTable(statistics.clientsWaitingTimes,statistics.clientsTransferTimes,statistics.clientsProcessingTimes,statistics.clientsResidenceTimes,Language.tr("Statistics.WaitingTime"),Language.tr("Statistics.TransferTime"),Language.tr("Statistics.ProcessTime"),Language.tr("Statistics.ResidenceTime"),Language.tr("Statistics.ClientType"),true,false,false); break;
		case MODE_FLOW_FACTOR_CLIENTS: buildFlowFactorTable(statistics.clientsProcessingTimes,statistics.clientsResidenceTimes,Language.tr("Statistics.ClientType")); break;
		case MODE_DISTRIBUTION_CLIENTS_WAITING: buildTimesDistributionTable(statistics.clientsWaitingTimes,Language.tr("Statistics.Seconds")); break;
		case MODE_DISTRIBUTION_CLIENTS_TRANSFER: buildTimesDistributionTable(statistics.clientsTransferTimes,Language.tr("Statistics.Seconds")); break;
		case MODE_DISTRIBUTION_CLIENTS_PROCESSING: buildTimesDistributionTable(statistics.clientsProcessingTimes,Language.tr("Statistics.Seconds")); break;
		case MODE_DISTRIBUTION_CLIENTS_RESIDENCE: buildTimesDistributionTable(statistics.clientsResidenceTimes,Language.tr("Statistics.Seconds")); break;
		case MODE_OVERVIEW_STATIONSMODE_OVERVIEW_CLIENTS: buildTimesOverviewTable(statistics.stationsWaitingTimes,statistics.stationsTransferTimes,statistics.stationsProcessingTimes,statistics.stationsResidenceTimes,Language.tr("Statistics.WaitingTime"),Language.tr("Statistics.TransferTime"),Language.tr("Statistics.ProcessTime"),Language.tr("Statistics.ResidenceTime"),Language.tr("Statistics.Station"),true,false,false); break;
		case MODE_OVERVIEW_STATIONSMODE_TOTAL_OVERVIEW_CLIENTS: buildTimesOverviewTable(statistics.stationsTotalWaitingTimes,statistics.stationsTotalTransferTimes,statistics.stationsTotalProcessingTimes,statistics.stationsTotalResidenceTimes,Language.tr("Statistics.WaitingTime"),Language.tr("Statistics.TransferTime"),Language.tr("Statistics.ProcessTime"),Language.tr("Statistics.ResidenceTime"),Language.tr("Statistics.Station"),true,false,false); break;
		case MODE_FLOW_FACTOR_STATION: buildFlowFactorTable(statistics.stationsProcessingTimes,statistics.stationsResidenceTimes,Language.tr("Statistics.Station")); break;
		case MODE_OVERVIEW_STATIONSCLIENTMODE_OVERVIEW_CLIENTS: buildTimesOverviewTable(statistics.stationsWaitingTimesByClientType,statistics.stationsTransferTimesByClientType,statistics.stationsProcessingTimesByClientType,statistics.stationsResidenceTimesByClientType,Language.tr("Statistics.WaitingTime"),Language.tr("Statistics.TransferTime"),Language.tr("Statistics.ProcessTime"),Language.tr("Statistics.ResidenceTime"),Language.tr("Statistics.StationClient"),true,false,false); break;
		case MODE_FLOW_FACTOR_STATION_CLIENT: buildFlowFactorTable(statistics.stationsProcessingTimesByClientType,statistics.stationsResidenceTimesByClientType,Language.tr("Statistics.StationClient")); break;
		case MODE_DISTRIBUTION_STATIONS_WAITING: buildTimesDistributionTable(statistics.stationsWaitingTimes,Language.tr("Statistics.Seconds")); break;
		case MODE_DISTRIBUTION_STATIONS_TRANSFER: buildTimesDistributionTable(statistics.stationsTransferTimes,Language.tr("Statistics.Seconds")); break;
		case MODE_DISTRIBUTION_STATIONS_PROCESSING: buildTimesDistributionTable(statistics.stationsProcessingTimes,Language.tr("Statistics.Seconds")); break;
		case MODE_DISTRIBUTION_STATIONS_RESIDENCE: buildTimesDistributionTable(statistics.stationsResidenceTimes,Language.tr("Statistics.Seconds")); break;
		case MODE_DISTRIBUTION_STATIONS_SETUP: buildTimesDistributionTable(statistics.stationsSetupTimes,Language.tr("Statistics.Seconds")); break;
		case MODE_DISTRIBUTION_STATIONS_WAITING_TOTAL: buildTimesDistributionTable(statistics.stationsTotalWaitingTimes,Language.tr("Statistics.Seconds")); break;
		case MODE_DISTRIBUTION_STATIONS_TRANSFER_TOTAL: buildTimesDistributionTable(statistics.stationsTotalTransferTimes,Language.tr("Statistics.Seconds")); break;
		case MODE_DISTRIBUTION_STATIONS_PROCESSING_TOTAL: buildTimesDistributionTable(statistics.stationsTotalProcessingTimes,Language.tr("Statistics.Seconds")); break;
		case MODE_DISTRIBUTION_STATIONS_RESIDENCE_TOTAL: buildTimesDistributionTable(statistics.stationsTotalResidenceTimes,Language.tr("Statistics.Seconds")); break;
		case MODE_DISTRIBUTION_STATIONSCLIENTS_WAITING: buildTimesDistributionTable(statistics.stationsWaitingTimesByClientType,Language.tr("Statistics.Seconds")); break;
		case MODE_DISTRIBUTION_STATIONSCLIENTS_TRANSFER: buildTimesDistributionTable(statistics.stationsTransferTimesByClientType,Language.tr("Statistics.Seconds")); break;
		case MODE_DISTRIBUTION_STATIONSCLIENTS_PROCESSING: buildTimesDistributionTable(statistics.stationsProcessingTimesByClientType,Language.tr("Statistics.Seconds")); break;
		case MODE_DISTRIBUTION_STATIONSCLIENTS_RESIDENCE: buildTimesDistributionTable(statistics.stationsResidenceTimesByClientType,Language.tr("Statistics.Seconds")); break;
		case MODE_OVERVIEW_NUMBER: buildCountOverviewTable(statistics.clientsAtStationByStation,statistics.clientsInSystem,"N"); break;
		case MODE_OVERVIEW_NUMBER_CLIENT_TYPES: buildCountOverviewTable(statistics.clientsAtStationByStationAndClient,null,"N"); break;
		case MODE_OVERVIEW_QUEUE: buildCountOverviewTable(statistics.clientsAtStationQueueByStation,statistics.clientsInSystemQueues,"NQ"); break;
		case MODE_OVERVIEW_QUEUE_CLIENT_TYPES: buildCountOverviewTable(statistics.clientsAtStationQueueByStationAndClient,null,"NQ"); break;
		case MODE_OVERVIEW_PROCESS: buildCountOverviewTable(statistics.clientsAtStationProcessByStation,statistics.clientsInSystemProcess,"NS"); break;
		case MODE_OVERVIEW_PROCESS_CLIENT_TYPES: buildCountOverviewTable(statistics.clientsAtStationProcessByStationAndClient,null,"NS"); break;
		case MODE_DISTRIBUTION_NUMBER_STATION: buildCountDistributionTable(statistics.clientsAtStationByStation,statistics.clientsInSystem); break;
		case MODE_DISTRIBUTION_NUMBER_STATION_CLIENT_TYPES: buildCountDistributionTable(statistics.clientsAtStationByStationAndClient,null); break;
		case MODE_DISTRIBUTION_NUMBER_CLIENT: buildCountDistributionTable(statistics.clientsInSystemByClient,statistics.clientsInSystem); break;
		case MODE_DISTRIBUTION_QUEUE: buildCountDistributionTable(statistics.clientsAtStationQueueByStation,statistics.clientsInSystemQueues); break;
		case MODE_DISTRIBUTION_QUEUE_CLIENT_TYPE: buildCountDistributionTable(statistics.clientsAtStationQueueByStationAndClient,null); break;
		case MODE_DISTRIBUTION_PROCESS: buildCountDistributionTable(statistics.clientsAtStationProcessByStation,statistics.clientsInSystemProcess); break;
		case MODE_DISTRIBUTION_PROCESS_CLIENT_TYPE: buildCountDistributionTable(statistics.clientsAtStationProcessByStationAndClient,null); break;
		case MODE_UTILIZATION: buildUtilizationTable(); break;
		case MODE_DOWNTIMES: buildDownTimesTable(); break;
		case MODE_TRANSPORTER_UTILIZATION: buildTransporterUtilizationTable(); break;
		case MODE_TRANSPORTER_DOWNTIMES: buildTransporterDownTimesTable(); break;
		case MODE_CLIENT_DATA: buildClientDataTable(); break;
		case MODE_CLIENT_DATA_DISTRIBUTION: buildClientDataDistributionTable(); break;
		case MODE_CLIENT_TEXT_DATA: buildClientDataTextTable(); break;
		case MODE_VALUE_RECORDING: buildValueRecordingTable(); break;
		case MODE_USER_VARIABLES: buildUserVariablesTable(); break;
		case MODE_SYSTEM_INFO_THREAD_BALANCE: buildThreadBalanceInfoTable(); break;
		}
	}
}
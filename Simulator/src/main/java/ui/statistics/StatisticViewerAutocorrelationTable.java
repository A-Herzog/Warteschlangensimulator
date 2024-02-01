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
import java.util.List;
import java.util.stream.Collectors;

import language.Language;
import mathtools.NumberTools;
import mathtools.Table;
import simulator.statistics.Statistics;
import statistics.StatisticsDataPerformanceIndicator;
import systemtools.statistics.StatisticViewerTable;
import ui.help.Help;
import ui.tools.FlatLaFHelper;

/**
 * Dieser Viewer stellt die Autokorrelationsdaten als Tabelle dar.
 * @see StatisticViewerTable
 * @author Alexander Herzog
 */
public class StatisticViewerAutocorrelationTable extends StatisticViewerBaseTable {
	/** Statistikobjekt, aus dem die anzuzeigenden Daten entnommen werden sollen */
	private final Statistics statistics;
	/** Gibt an, welche Daten genau ausgegeben werden sollen */
	private final Mode mode;

	/**
	 * W�hlt die von {@link StatisticViewerAutocorrelationTable} auszugebende Information aus.
	 * @author Alexander Herzog
	 * @see StatisticViewerAutocorrelationTable#StatisticViewerAutocorrelationTable(Statistics, Mode)
	 */
	public enum Mode {
		/** Standard-Tabellenausgabe der Autokorrelationen der Wartezeiten */
		MODE_DEFAULT,
		/** Tabellenausgabe der Autokorrelationen der Wartezeiten inkl. Ausgabe des Maximums */
		MODE_DETAILS
	}

	/**
	 * Konstruktor der Klasse
	 * @param statistics	Statistikobjekt, aus dem die anzuzeigenden Daten entnommen werden sollen
	 * @param mode	Gibt an, welche Daten genau ausgegeben werden sollen
	 * @see Mode
	 */
	public StatisticViewerAutocorrelationTable(final Statistics statistics, final Mode mode) {
		this.statistics=statistics;
		this.mode=mode;
	}

	/**
	 * F�gt eine Zeile zu der Autokorrelations-Tabelle hinzu.
	 * @param table	Autokorrelations-Tabelle
	 * @param label	Bezeichnung der Zeile (Wert f�r die erste Spalte)
	 * @param indicator	Statistikobjekt dem die Autokorrelationsdaten entnommen werden sollen
	 * @param maxDistance	Maximale darzustellende Distanz
	 * @return	Liefert <code>true</code>, wenn Autokorrelationsdaten vorlagen
	 * @see #buildDefaultTable()
	 */
	private boolean addDefaultTableRow(final Table table, final String label, final StatisticsDataPerformanceIndicator indicator, final int[] maxDistance) {
		if (!indicator.isCorrelationAvailable()) return false;

		final String[] col=new String[StatisticViewerOverviewText.AUTOCORRELATION_LEVELS.length+1];
		col[0]=label;

		for (int i=0;i<StatisticViewerOverviewText.AUTOCORRELATION_LEVELS.length;i++) {
			final double level=StatisticViewerOverviewText.AUTOCORRELATION_LEVELS[i];
			final int distance=indicator.getCorrelationLevelDistance(level);
			maxDistance[i]=Math.max(maxDistance[i],distance);
			col[i+1]=NumberTools.formatLongNoGrouping(distance);
		}

		table.addLine(col);

		return true;
	}

	/**
	 * Zeigt im Fu�bereich der Hilfeseite eine "Erkl�rung einblenden"-Schaltfl�che, die,
	 * wenn sie angeklickt wird, eine html-Hilfeseite anzeigt.
	 * @param topic	Hilfe-Thema (wird als Datei in den "description_*"-Ordern gesucht)
	 */
	private void addDescription(final String topic) {
		final URL url=StatisticViewerAutocorrelationTable.class.getResource("description_"+Language.getCurrentLanguage()+"/"+topic+".html");
		addDescription(url,helpTopic->Help.topic(getViewer(false),helpTopic));
	}

	@Override
	protected String getDescriptionCustomStyles() {
		if (FlatLaFHelper.isDark()) return StatisticsPanel.DARK_MODE_DESACRIPTION_STYLE;
		return null;
	}

	/**
	 * Standard-Tabellenausgabe der Autokorrelationen der Wartezeiten
	 * @see Mode#MODE_DEFAULT
	 */
	private void buildDefaultTable() {
		int[] maxDistance=new int[StatisticViewerOverviewText.AUTOCORRELATION_LEVELS.length];
		boolean individualData=false;
		StatisticsDataPerformanceIndicator indicator;

		final Table table=new Table();

		/* Autokorrelation �ber die Wartezeit �ber alle Kunden */

		addDefaultTableRow(table,Language.tr("Statistics.ResultsAutocorrelation.WaitingTime.AllClients"),statistics.clientsAllWaitingTimes,maxDistance);

		/* Autokorrelation �ber die Wartezeiten der einzelnen Kundentypen */

		for (String name: statistics.clientsWaitingTimes.getNames()) {
			indicator=(StatisticsDataPerformanceIndicator)statistics.clientsWaitingTimes.get(name);
			if (addDefaultTableRow(table,Language.tr("Statistics.ResultsAutocorrelation.WaitingTime.ByClientTypes")+" - "+name,indicator,maxDistance)) individualData=true;
		}

		/* Autokorrelation �ber die Wartezeiten an den Stationen */

		for (String name: statistics.stationsWaitingTimes.getNames()) {
			indicator=(StatisticsDataPerformanceIndicator)statistics.stationsWaitingTimes.get(name);
			if (addDefaultTableRow(table,Language.tr("Statistics.ResultsAutocorrelation.WaitingTime.ByStations")+" - "+name,indicator,maxDistance)) individualData=true;
		}

		/* Autokorrelation �ber die Wartezeiten an den Stationen ausdifferenziert nach Kundentypen */

		for (String name: statistics.stationsWaitingTimesByClientType.getNames()) {
			indicator=(StatisticsDataPerformanceIndicator)statistics.stationsWaitingTimesByClientType.get(name);
			if (addDefaultTableRow(table,Language.tr("Statistics.ResultsAutocorrelation.WaitingTime.ByStationsAndClientTypes")+" - "+name,indicator,maxDistance)) individualData=true;
		}

		/* Maximum �ber alle Werte (nur wenn individuelle Were vorliegen) */

		if (individualData) {
			final String[] col=new String[maxDistance.length+1];
			col[0]=Language.tr("Statistics.Maximum");
			for (int i=0;i<maxDistance.length;i++) col[i+1]=NumberTools.formatLongNoGrouping(maxDistance[i]);
			table.addLine(col);
		}

		final List<String> cols=new ArrayList<>(Arrays.stream(StatisticViewerOverviewText.AUTOCORRELATION_LEVELS).mapToObj(d->StatisticTools.formatPercent(d)).collect(Collectors.toList()));
		cols.add(0,"");

		setData(table,cols);

		/* Infotext  */
		addDescription("TableAutocorrelationDefault");
	}

	/**
	 * Gibt eine Spalte f�r die Details-Autokorrelations-Tabelle aus.
	 * @param indicator	Statistikobjekt dem die Autokorrelationsdaten entnommen werden sollen
	 * @param name	Bezeichnung der Spalte
	 * @param table	Autokorrelations-Tabelle (wird sp�ter transponiert)
	 * @param cols	Spalten�berschriften
	 */
	private void addDetailsTableRow(final StatisticsDataPerformanceIndicator indicator, final String name, final Table table, final List<String> cols) {
		if (!indicator.isCorrelationAvailable()) return;

		cols.add(name);
		table.addLine(Arrays.stream(indicator.getCorrelationData()).mapToObj(d->StatisticTools.formatPercent(d,3)).collect(Collectors.toList()));
	}

	/**
	 * Tabellenausgabe der Autokorrelationen der Wartezeiten inkl. Ausgabe des Maximums
	 * @see Mode#MODE_DETAILS
	 */
	private void buildDetailsTable() {
		final Table table=new Table();
		final List<String> cols=new ArrayList<>();

		cols.add(Language.tr("Statistics.ResultsAutocorrelation.Distance"));
		final List<String> line=new ArrayList<>();
		if (statistics.clientsAllWaitingTimes.isCorrelationAvailable()) {
			final int length=statistics.clientsAllWaitingTimes.getCorrelationData().length;
			for (int i=0;i<length;i++) line.add(NumberTools.formatLongNoGrouping(i*StatisticsDataPerformanceIndicator.CORRELATION_RANGE_STEPPING));
		}
		table.addLine(line);

		/* Autokorrelation �ber die Wartezeit �ber alle Kunden */

		addDetailsTableRow(statistics.clientsAllWaitingTimes,Language.tr("Statistics.ResultsAutocorrelation.WaitingTime.AllClients"),table,cols);

		/* Autokorrelation �ber die Wartezeiten der einzelnen Kundentypen */

		for (String name: statistics.clientsWaitingTimes.getNames()) {
			StatisticsDataPerformanceIndicator indicator=(StatisticsDataPerformanceIndicator)statistics.clientsWaitingTimes.get(name);
			addDetailsTableRow(indicator,Language.tr("Statistics.ResultsAutocorrelation.WaitingTime.ByClientTypes")+" - "+name,table,cols);
		}

		/* Autokorrelation �ber die Wartezeiten an den Stationen */

		for (String name: statistics.stationsWaitingTimes.getNames()) {
			StatisticsDataPerformanceIndicator indicator=(StatisticsDataPerformanceIndicator)statistics.stationsWaitingTimes.get(name);
			addDetailsTableRow(indicator,Language.tr("Statistics.ResultsAutocorrelation.WaitingTime.ByStations")+" - "+name,table,cols);
		}

		/* Autokorrelation �ber die Wartezeiten an den Stationen ausdifferenziert nach Kundentypen */

		for (String name: statistics.stationsWaitingTimesByClientType.getNames()) {
			StatisticsDataPerformanceIndicator indicator=(StatisticsDataPerformanceIndicator)statistics.stationsWaitingTimesByClientType.get(name);
			addDetailsTableRow(indicator,Language.tr("Statistics.ResultsAutocorrelation.WaitingTime.ByStationsAndClientTypes")+" - "+name,table,cols);
		}

		setData(table.transpose(),cols);

		/* Infotext  */
		addDescription("TableAutocorrelationDetails");
	}

	@Override
	protected void buildTable() {
		switch (mode) {
		case MODE_DEFAULT: buildDefaultTable(); break;
		case MODE_DETAILS: buildDetailsTable(); break;
		}
	}
}
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
import mathtools.distribution.DataDistributionImpl;
import simulator.statistics.Statistics;
import statistics.StatisticsDataCollector;
import statistics.StatisticsDataPerformanceIndicatorWithNegativeValues;
import statistics.StatisticsMultiPerformanceIndicator;
import statistics.StatisticsTimeContinuousPerformanceIndicator;
import systemtools.statistics.StatisticViewerTable;
import tools.SetupData;
import ui.help.Help;
import ui.tools.FlatLaFHelper;

/**
 * Dieser Viewer stellt die benutzerdefinierten Statistikdaten als Tabelle dar.
 * @see StatisticViewerTable
 * @see Statistics#userStatistics
 * @author Alexander Herzog
 */
public class StatisticViewerUserStatisticTable extends StatisticViewerBaseTable {
	/** Statistikobjekt, aus dem die anzuzeigenden Daten entnommen werden sollen */
	private final Statistics statistics;
	/** Gibt an, welche Daten genau ausgegeben werden sollen */
	private final Mode mode;

	/**
	 * Wählt die von {@link StatisticViewerUserStatisticTable} auszugebende Information aus.
	 * @author Alexander Herzog
	 * @see StatisticViewerUserStatisticTable#StatisticViewerUserStatisticTable(Statistics, Mode)
	 */
	public enum Mode {
		/** Übersichtstabelle */
		MODE_DEFAULT,
		/** Tabelle mit Verteilung der Daten */
		MODE_DETAILS,
		/** Tabelle mit den Anzahlen pro Intervall */
		MODE_INTERVAL_COUNT,
		/** Tabelle mit den Mittelwerten pro Intervall */
		MODE_INTERVAL
	}

	/**
	 * Konstruktor der Klasse
	 * @param statistics	Statistikobjekt, aus dem die anzuzeigenden Daten entnommen werden sollen
	 * @param mode	Gibt an, welche Daten genau ausgegeben werden sollen
	 * @see Mode
	 */
	public StatisticViewerUserStatisticTable(final Statistics statistics, final Mode mode) {
		this.statistics=statistics;
		this.mode=mode;
	}

	/**
	 * Zeigt im Fußbereich der Hilfeseite eine "Erklärung einblenden"-Schaltfläche, die,
	 * wenn sie angeklickt wird, eine html-Hilfeseite anzeigt.
	 * @param topic	Hilfe-Thema (wird als Datei in den "description_*"-Ordern gesucht)
	 */
	private void addDescription(final String topic) {
		final URL url=StatisticViewerUserStatisticTable.class.getResource("description_"+Language.getCurrentLanguage()+"/"+topic+".html");
		addDescription(url,helpTopic->Help.topic(getViewer(false),helpTopic));
	}

	@Override
	protected String getDescriptionCustomStyles() {
		if (FlatLaFHelper.isDark()) return StatisticsPanel.DARK_MODE_DESACRIPTION_STYLE;
		return null;
	}

	/**
	 * Besitzt ein Statistik-Indikator Konfidenzintervall-Informationen (auf Batch-Means-Basis)?
	 * @param indicator	Statistik-Indikator
	 * @return	Liegen Konfidenzintervall-Informationen vor?
	 */
	private boolean hasConfidenceBatchMeans(final StatisticsMultiPerformanceIndicator indicator) {
		String[] names=indicator.getNames();
		if (names.length==0) return false;
		if (((StatisticsDataPerformanceIndicatorWithNegativeValues)indicator.get(names[0])).getBatchCount()<2) return false;

		return true;
	}

	/**
	 * Besitzt ein Statistik-Indikator Konfidenzintervall-Informationen (über Teil-Simulationsläufe)?
	 * @param indicator	Statistik-Indikator
	 * @return	Liegen Konfidenzintervall-Informationen vor?
	 */
	private boolean hasConfidenceRun(final StatisticsMultiPerformanceIndicator indicator) {
		String[] names=indicator.getNames();
		if (statistics.simulationData.runRepeatCount<2) return false;
		if (names.length==0) return false;
		if (((StatisticsDataPerformanceIndicatorWithNegativeValues)indicator.get(names[0])).getRunCount()<2) return false;

		return true;
	}

	/**
	 * Erstellt die Übersichtstabelle
	 * @see Mode#MODE_DEFAULT
	 */
	private void buildDefaultTable() {
		final boolean hasConfidenceBatchMeans=hasConfidenceBatchMeans(statistics.userStatistics);
		final boolean hasConfidenceRun=!hasConfidenceBatchMeans && hasConfidenceRun(statistics.userStatistics);
		final double[] confidenceLevels=StatisticViewerOverviewText.getConfidenceLevels();

		final List<String> cols=new ArrayList<>();
		cols.add(Language.tr("Statistics.UserStatistics"));
		cols.add(Language.tr("Statistics.NumberOfClients"));
		cols.add(Language.tr("Statistics.Average"));
		cols.add(Language.tr("Statistics.StdDevUserTime"));
		cols.add(Language.tr("Statistics.VarianceUserTime"));
		cols.add(Language.tr("Statistics.CVUserTime"));
		cols.add(Language.tr("Statistics.Skewness"));
		cols.add(Language.tr("Statistics.Kurt"));
		cols.add(Language.tr("Statistics.Minimum"));
		cols.add(Language.tr("Statistics.Maximum"));
		if (SetupData.getSetup().showQuantils) {
			final double[] levels=StatisticViewerOverviewText.getQuantilLevels();
			for (double p: levels) {
				cols.add(StatisticTools.formatPercent(p)+" "+Language.tr("Statistics.Quantil"));
			}
		}
		if (hasConfidenceBatchMeans) for (double level: confidenceLevels) {
			cols.add(String.format(Language.tr("Statistics.ConfidenceLevel"),StatisticTools.formatPercent(1-level)));
		}
		if (hasConfidenceRun) for (double level: confidenceLevels) {
			cols.add(String.format(Language.tr("Statistics.ConfidenceLevel"),StatisticTools.formatPercent(1-level)));
		}

		final Table table=new Table();

		for (String name: statistics.userStatistics.getNames()) {
			final List<String> row=new ArrayList<>();
			final StatisticsDataPerformanceIndicatorWithNegativeValues indicator=(StatisticsDataPerformanceIndicatorWithNegativeValues)statistics.userStatistics.get(name);
			row.add(name);
			row.add(NumberTools.formatLong(indicator.getCount()));
			if (StatisticViewerOverviewText.isUserStatisticsTime(statistics,name)!=StatisticViewerOverviewText.UserStatisticsFormat.NUMBER) {
				row.add(StatisticTools.formatExactTime(indicator.getMean()));
				row.add(StatisticTools.formatExactTime(indicator.getSD()));
				row.add(StatisticTools.formatExactTime(indicator.getVar()));
				row.add(StatisticTools.formatNumber(indicator.getCV()));
				row.add(StatisticTools.formatNumber(indicator.getSk()));
				row.add(StatisticTools.formatNumber(indicator.getKurt()));
				row.add(StatisticTools.formatExactTime(indicator.getMin()));
				row.add(StatisticTools.formatExactTime(indicator.getMax()));
				if (SetupData.getSetup().showQuantils && indicator.getDistribution()!=null) {
					final double[] levels=StatisticViewerOverviewText.getQuantilLevels();
					for (double p: levels) {
						row.add(StatisticTools.formatNumber(indicator.getQuantil(p)));
					}
				}
				if (hasConfidenceBatchMeans) {
					final double mean=indicator.getMean();
					final double[] halfWidth=indicator.getBatchMeanConfidenceHalfWide(confidenceLevels);
					for (int i=0;i<halfWidth.length;i++) row.add(String.format("[%s;%s]",StatisticTools.formatNumber(mean-halfWidth[i]),StatisticTools.formatNumber(mean+halfWidth[i])));
				}
				if (hasConfidenceRun) {
					final double mean=indicator.getMean();
					final double[] halfWidth=indicator.getRunConfidenceHalfWide(confidenceLevels);
					for (int i=0;i<halfWidth.length;i++) row.add(String.format("[%s;%s]",StatisticTools.formatNumber(mean-halfWidth[i]),StatisticTools.formatNumber(mean+halfWidth[i])));
				}
			} else {
				row.add(StatisticTools.formatNumber(indicator.getMean()));
				row.add(StatisticTools.formatNumber(indicator.getSD()));
				row.add(StatisticTools.formatNumber(indicator.getVar()));
				row.add(StatisticTools.formatNumber(indicator.getCV()));
				row.add(StatisticTools.formatNumber(indicator.getSk()));
				row.add(StatisticTools.formatNumber(indicator.getKurt()));
				row.add(StatisticTools.formatNumber(indicator.getMin()));
				row.add(StatisticTools.formatNumber(indicator.getMax()));
				if (SetupData.getSetup().showQuantils && indicator.getDistribution()!=null) {
					final double[] levels=StatisticViewerOverviewText.getQuantilLevels();
					for (double p: levels) {
						row.add(StatisticTools.formatNumber(indicator.getQuantil(p)));
					}
				}
				if (hasConfidenceBatchMeans) {
					final double mean=indicator.getMean();
					final double[] halfWidth=indicator.getBatchMeanConfidenceHalfWide(confidenceLevels);
					for (int i=0;i<halfWidth.length;i++) row.add(String.format("[%s;%s]",StatisticTools.formatNumber(mean-halfWidth[i]),StatisticTools.formatNumber(mean+halfWidth[i])));
				}
				if (hasConfidenceRun) {
					final double mean=indicator.getMean();
					final double[] halfWidth=indicator.getRunConfidenceHalfWide(confidenceLevels);
					for (int i=0;i<halfWidth.length;i++) row.add(String.format("[%s;%s]",StatisticTools.formatNumber(mean-halfWidth[i]),StatisticTools.formatNumber(mean+halfWidth[i])));
				}
			}
			table.addLine(row);
		}

		for (String name: statistics.userStatisticsContinuous.getNames()) {
			final List<String> row=new ArrayList<>();
			final StatisticsTimeContinuousPerformanceIndicator indicator=(StatisticsTimeContinuousPerformanceIndicator)statistics.userStatisticsContinuous.get(name);
			row.add(name);
			row.add("-");
			if (StatisticViewerOverviewText.isUserStatisticsTime(statistics,name)!=StatisticViewerOverviewText.UserStatisticsFormat.NUMBER) {
				row.add(StatisticTools.formatExactTime(indicator.getTimeMean()));
				row.add(StatisticTools.formatExactTime(indicator.getTimeSD()));
				row.add(StatisticTools.formatExactTime(indicator.getTimeVar()));
				row.add(StatisticTools.formatNumber(indicator.getTimeCV()));
				row.add(StatisticTools.formatNumber(indicator.getTimeSk()));
				row.add(StatisticTools.formatNumber(indicator.getTimeKurt()));
				row.add(StatisticTools.formatExactTime(indicator.getTimeMin()));
				row.add(StatisticTools.formatExactTime(indicator.getTimeMax()));
				if (SetupData.getSetup().showQuantils) {
					final double[] levels=StatisticViewerOverviewText.getQuantilLevels();
					for (int i=0;i<levels.length;i++) row.add("-");
				}
				if (hasConfidenceBatchMeans) {
					for (int i=0;i<confidenceLevels.length;i++) row.add("-");
				}
				if (hasConfidenceRun) {
					for (int i=0;i<confidenceLevels.length;i++) row.add("-");
				}
			} else {
				row.add(StatisticTools.formatNumber(indicator.getTimeMean()));
				row.add(StatisticTools.formatNumber(indicator.getTimeSD()));
				row.add(StatisticTools.formatNumber(indicator.getTimeVar()));
				row.add(StatisticTools.formatNumber(indicator.getTimeCV()));
				row.add(StatisticTools.formatNumber(indicator.getTimeSk()));
				row.add(StatisticTools.formatNumber(indicator.getTimeKurt()));
				row.add(StatisticTools.formatNumber(indicator.getTimeMin()));
				row.add(StatisticTools.formatNumber(indicator.getTimeMax()));
				if (SetupData.getSetup().showQuantils) {
					final double[] levels=StatisticViewerOverviewText.getQuantilLevels();
					for (int i=0;i<levels.length;i++) row.add("-");
				}
				if (hasConfidenceBatchMeans) {
					for (int i=0;i<confidenceLevels.length;i++) row.add("-");
				}
				if (hasConfidenceRun) {
					for (int i=0;i<confidenceLevels.length;i++) row.add("-");
				}
			}
			table.addLine(row);
		}

		setData(table,cols);

		/* Infotext  */
		addDescription("TableUserStatisticDefault");
	}

	/**
	 * Erstellt die Tabelle mit der Verteilung der Daten
	 * @see Mode#MODE_DETAILS
	 */
	private void buildDetailsTable() {
		final Table table=new Table();
		final List<String> headers=new ArrayList<>();

		final String[] types=statistics.userStatistics.getNames();

		double scale=1;
		headers.add(Language.tr("Statistics.Seconds")+"/"+Language.tr("Statistics.Value"));
		List<DataDistributionImpl> dists=new ArrayList<>();
		for (String type: types) {
			headers.add(Language.tr("Statistics.NumberOfClients")+" - "+type);
			headers.add(Language.tr("Statistics.PartOfClients")+" - "+type);
			final DataDistributionImpl dist=((StatisticsDataPerformanceIndicatorWithNegativeValues)(statistics.userStatistics.get(type))).getDistribution();
			if (dist!=null) {
				dists.add(dist);
				scale=dist.upperBound/dist.densityData.length;
			}
		}

		final List<Double> sum=new ArrayList<>();
		for (DataDistributionImpl dist: dists) sum.add(dist.sum());

		if (dists.size()>0)	for (int i=0;i<dists.get(0).densityData.length;i++) {
			final List<String> line=new ArrayList<>();
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
		addDescription("TableUserStatisticDetails");
	}

	/**
	 * Erstellt die Tabelle mit den Anzahlen pro Intervall
	 * @see Mode#MODE_INTERVAL_COUNT
	 */
	private void buildIntervalCount() {
		final Table table=new Table();
		final List<String> headers=new ArrayList<>();

		final List<String> types=Arrays.asList(statistics.userStatisticsIntervalCount.getNames());
		final List<StatisticsDataCollector> data=types.stream().map(name->(StatisticsDataCollector)statistics.userStatisticsIntervalCount.get(name)).collect(Collectors.toList());
		final int count=data.stream().mapToInt(c->c.getCount()).max().orElseGet(()->0);

		headers.add("Intervall");
		headers.addAll(types);

		for (int i=0;i<count;i++) {
			final List<String> line=new ArrayList<>();
			line.add(NumberTools.formatLong(i));
			for (var c: data) {
				if (i>=c.getCount()) {line.add(""); continue;}
				final long value=Math.round(c.getValue(i));
				line.add(NumberTools.formatLong(value));
			}
			table.addLine(line);
		}

		final List<String> line=new ArrayList<>();
		line.add(Language.tr("Statistics.XML.Sum"));
		for (var c: data) {
			line.add(NumberTools.formatLong(Math.round(c.getSum())));
		}
		table.addLine(line);

		setData(table,headers);

		/* Infotext  */
		addDescription("TableUserStatisticIntervalCount");
	}

	/**
	 * Erstellt die Tabelle mit den Mitterlwerten pro Intervall
	 * @see Mode#MODE_INTERVAL
	 */
	private void buildIntervalMean() {
		final Table table=new Table();
		final List<String> headers=new ArrayList<>();

		final List<String> types=Arrays.asList(statistics.userStatisticsIntervalMean.getNames());
		final List<StatisticsDataCollector> data=types.stream().map(name->(StatisticsDataCollector)statistics.userStatisticsIntervalMean.get(name)).collect(Collectors.toList());
		final List<StatisticViewerOverviewText.UserStatisticsFormat> formats=types.stream().map(name->StatisticViewerOverviewText.isUserStatisticsTime(statistics,name)).collect(Collectors.toList());

		headers.add("Intervall");
		headers.addAll(types);

		final int count=data.stream().mapToInt(c->c.getCount()).max().orElseGet(()->0);
		for (int i=0;i<count;i++) {
			final List<String> line=new ArrayList<>();
			line.add(NumberTools.formatLong(i));
			int j=0;
			for (var c: data) {
				if (i>=c.getCount()) {line.add(""); continue;}
				switch (formats.get(j)) {
				case NUMBER:
				case MIXED:
					line.add(StatisticTools.formatNumber(c.getValue(i)));
					break;
				case TIME:
					line.add(StatisticTools.formatExactTime(c.getValue(i)));
					break;
				}
				j++;
			}
			table.addLine(line);
		}

		setData(table,headers);

		/* Infotext  */
		addDescription("TableUserStatisticInterval");
	}

	@Override
	protected void buildTable() {
		switch (mode) {
		case MODE_DEFAULT: buildDefaultTable(); break;
		case MODE_DETAILS: buildDetailsTable(); break;
		case MODE_INTERVAL_COUNT: buildIntervalCount(); break;
		case MODE_INTERVAL: buildIntervalMean(); break;
		}
	}
}

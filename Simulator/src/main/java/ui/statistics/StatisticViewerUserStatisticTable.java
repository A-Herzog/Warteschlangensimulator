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

import language.Language;
import mathtools.NumberTools;
import mathtools.Table;
import mathtools.TimeTools;
import mathtools.distribution.DataDistributionImpl;
import simulator.statistics.Statistics;
import statistics.StatisticsDataPerformanceIndicator;
import systemtools.statistics.StatisticViewerTable;
import tools.SetupData;
import ui.help.Help;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementUserStatistic;

/**
 * Dieser Viewer stellt die benutzerdefinierten Statistikdaten als Tabelle dar.
 * @see StatisticViewerTable
 * @see Statistics#userStatistics
 * @author Alexander Herzog
 */
public class StatisticViewerUserStatisticTable extends StatisticViewerTable {
	private final Statistics statistics;
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
		MODE_DETAILS
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

	private boolean isUserStatisticsTime(final String key) {
		for (ModelElement element: statistics.editModel.surface.getElements()) {
			if (element instanceof ModelElementUserStatistic) {
				final Boolean B=((ModelElementUserStatistic)element).getIsTimeForKey(key);
				if (B!=null) return B.booleanValue();
			}
			if (element instanceof ModelElementUserStatistic) {
				for (ModelElement sub: ((ModelElementUserStatistic)element).getSurface().getElements()) {
					if (sub instanceof ModelElementUserStatistic) {
						final Boolean B=((ModelElementUserStatistic)sub).getIsTimeForKey(key);
						if (B!=null) return B.booleanValue();
					}
				}
			}
		}

		return true;
	}

	private void addDescription(final String topic) {
		final URL url=StatisticViewerUserStatisticTable.class.getResource("description_"+Language.getCurrentLanguage()+"/"+topic+".html");
		addDescription(url,helpTopic->Help.topic(getViewer(false),helpTopic));
	}

	private void buildDefaultTable() {
		final List<String> cols=new ArrayList<>();
		cols.add(Language.tr("Statistics.UserStatistics"));
		cols.add(Language.tr("Statistics.NumberOfClients"));
		cols.add(Language.tr("Statistics.Average"));
		cols.add(Language.tr("Statistics.StdDevUserTime"));
		cols.add(Language.tr("Statistics.VarianceUserTime"));
		cols.add(Language.tr("Statistics.CVUserTime"));
		cols.add(Language.tr("Statistics.Minimum"));
		cols.add(Language.tr("Statistics.Maximum"));
		if (SetupData.getSetup().showQuantils) for (double p: StatisticsDataPerformanceIndicator.storeQuantilValues) {
			cols.add(NumberTools.formatPercent(p)+" "+Language.tr("Statistics.Quantil"));
		}

		final Table table=new Table();
		for (String name: statistics.userStatistics.getNames()) {
			final List<String> row=new ArrayList<>();
			final StatisticsDataPerformanceIndicator indicator=(StatisticsDataPerformanceIndicator)statistics.userStatistics.get(name);
			row.add(name);
			row.add(NumberTools.formatLong(indicator.getCount()));
			if (isUserStatisticsTime(name)) {
				row.add(TimeTools.formatExactTime(indicator.getMean()));
				row.add(TimeTools.formatExactTime(indicator.getSD()));
				row.add(TimeTools.formatExactTime(indicator.getVar()));
				row.add(NumberTools.formatNumber(indicator.getCV()));
				row.add(TimeTools.formatExactTime(indicator.getMin()));
				row.add(TimeTools.formatExactTime(indicator.getMax()));
				if (SetupData.getSetup().showQuantils && indicator.getDistribution()!=null) for (double p: StatisticsDataPerformanceIndicator.storeQuantilValues) {
					row.add(NumberTools.formatNumber(indicator.getQuantil(p)));
				}
			} else {
				row.add(NumberTools.formatNumber(indicator.getMean()));
				row.add(NumberTools.formatNumber(indicator.getSD()));
				row.add(NumberTools.formatNumber(indicator.getVar()));
				row.add(NumberTools.formatNumber(indicator.getCV()));
				row.add(NumberTools.formatNumber(indicator.getMin()));
				row.add(NumberTools.formatNumber(indicator.getMax()));
				if (SetupData.getSetup().showQuantils && indicator.getDistribution()!=null) for (double p: StatisticsDataPerformanceIndicator.storeQuantilValues) {
					row.add(NumberTools.formatNumber(indicator.getQuantil(p)));
				}
			}
			table.addLine(row);
		}

		setData(table,cols);

		/* Infotext  */
		addDescription("TableUserStatisticDefault");
	}

	private void buildDetailsTable() {
		final Table table=new Table();
		final List<String> headers=new ArrayList<>();

		final String[] types=statistics.userStatistics.getNames();

		headers.add(Language.tr("Statistics.Seconds")+"/"+Language.tr("Statistics.Value"));
		List<DataDistributionImpl> dists=new ArrayList<>();
		for (String type: types) {
			headers.add(Language.tr("Statistics.NumberOfClients")+" - "+type);
			headers.add(Language.tr("Statistics.PartOfClients")+" - "+type);
			final DataDistributionImpl dist=((StatisticsDataPerformanceIndicator)(statistics.userStatistics.get(type))).getDistribution();
			if (dist!=null) dists.add(dist);
		}

		List<Double> sum=new ArrayList<>();
		for (DataDistributionImpl dist: dists) sum.add(dist.sum());

		if (dists.size()>0)	for (int i=0;i<dists.get(0).densityData.length;i++) {
			List<String> line=new ArrayList<>();
			line.add(NumberTools.formatLong(i));
			for (int j=0;j<dists.size();j++) {
				double value=dists.get(j).densityData[i];
				line.add(NumberTools.formatNumber(value));
				line.add(NumberTools.formatPercent(value/sum.get(j),3));
			}
			table.addLine(line);
		}

		setData(table,headers);

		/* Infotext  */
		addDescription("TableUserStatisticDetails");
	}

	@Override
	protected void buildTable() {
		switch (mode) {
		case MODE_DEFAULT: buildDefaultTable(); break;
		case MODE_DETAILS: buildDetailsTable(); break;
		}
	}
}

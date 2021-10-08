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

import language.Language;
import mathtools.Table;
import mathtools.TimeTools;
import simulator.statistics.Statistics;
import statistics.StatisticsLongRunPerformanceIndicator;
import systemtools.statistics.StatisticViewerTable;
import ui.help.Help;

/**
 * Dieser Viewer stellt die Laufzeitstatistikdaten als Tabelle dar.
 * @see StatisticViewerTable
 * @author Alexander Herzog
 */
public class StatisticViewerAdditionalTable extends StatisticViewerBaseTable {
	/** Statistikobjekt, aus dem die anzuzeigenden Daten entnommen werden sollen */
	private final Statistics statistics;
	/** Erfassungsschrittweite in Sekunden */
	private final long stepWide;

	/**
	 * Konstruktor der Klasse
	 * @param statistics	Statistikobjekt, aus dem die anzuzeigenden Daten entnommen werden sollen
	 */
	public StatisticViewerAdditionalTable(final Statistics statistics) {
		this.statistics=statistics;
		stepWide=statistics.editModel.longRunStatistics.getStepWideSec();
	}

	/**
	 * Zeigt im Fußbereich der Hilfeseite eine "Erklärung einblenden"-Schaltfläche, die,
	 * wenn sie angeklickt wird, eine html-Hilfeseite anzeigt.
	 * @param topic	Hilfe-Thema (wird als Datei in den "description_*"-Ordern gesucht)
	 */
	private void addDescription(final String topic) {
		final URL url=StatisticViewerAdditionalTable.class.getResource("description_"+Language.getCurrentLanguage()+"/"+topic+".html");
		addDescription(url,helpTopic->Help.topic(getViewer(false),helpTopic));
	}

	/**
	 * Liefert die Anzahl an erfassten Schritten.
	 * @return	Anzahl an erfassten Schritten
	 */
	private int getStepCount() {
		final String[] names=statistics.longRunStatistics.getNames();
		if (names.length==0) return 0;
		final StatisticsLongRunPerformanceIndicator indicator=(StatisticsLongRunPerformanceIndicator)statistics.longRunStatistics.get(names[0]);
		return indicator.getValueCount();
	}

	/**
	 * Liefert das Zeitintervall für einen Erfassungsschritt.
	 * @param index	Erfassungsschritt
	 * @return	Zeitintervall
	 */
	private String getTimeSlotString(final int index) {
		return TimeTools.formatLongTime(stepWide*index)+"-"+TimeTools.formatLongTime(stepWide*(index+1)-1);
	}

	@Override
	protected void buildTable() {
		final Table table=new Table();
		final List<String> cols=new ArrayList<>();
		final String[] names=statistics.longRunStatistics.getNames();
		final StatisticsLongRunPerformanceIndicator[] data=new StatisticsLongRunPerformanceIndicator[names.length];
		for (int i=0;i<names.length;i++) data[i]=(StatisticsLongRunPerformanceIndicator)statistics.longRunStatistics.get(names[i]);

		cols.add(Language.tr("Statistic.Interval"));
		cols.addAll(Arrays.asList(names));

		final String[] line=new String[cols.size()];
		final int stepCount=getStepCount();
		for (int i=0;i<stepCount;i++) {
			line[0]=getTimeSlotString(i);
			for (int j=0;j<names.length;j++) line[j+1]=StatisticTools.formatNumber(data[j].getValue(i));
			table.addLine(line);
		}

		addDescription("TableAdditionalStatistics");

		setData(table,cols);
	}
}

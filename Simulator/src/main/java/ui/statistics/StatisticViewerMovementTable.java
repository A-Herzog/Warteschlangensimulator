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

import javax.swing.JButton;

import language.Language;
import mathtools.NumberTools;
import mathtools.Table;
import simulator.statistics.Statistics;
import statistics.StatisticsDataPerformanceIndicator;
import statistics.StatisticsSimpleCountPerformanceIndicator;
import systemtools.statistics.StatisticViewerTable;
import tools.SetupData;
import ui.help.Help;
import ui.images.Images;

/**
 * Dieser Viewer stellt die Übergänge der Kunden zwischen den Stationen
 * als Tabelle dar und bietet an, diese als Sankey-Diagramm zu exportieren.
 * @author Alexander Herzog
 * @see StatisticViewerTable
 * @see CreateSankey
 */
public class StatisticViewerMovementTable extends StatisticViewerTable {
	/**
	 * Welche Informationen sollen angezeigt werden?
	 * @author Alexander Herzog
	 * @see StatisticViewerMovementTable#StatisticViewerMovementTable(Statistics, Mode)
	 */
	public enum Mode {
		/** Übergänge zwischen Stationen */
		STATION_TRANSITION(true),
		/** Pfade der Kunden */
		CLIENT_PATHS(true),
		/** Pfadlängen */
		CLIENT_PATH_LENGTHS(false),
		/** Häufigkeitsverteilung der Pfadlängen */
		CLIENT_PATH_LENGTHS_DISTRIBUTION(false);

		/** Kann auf Basis dieser Daten ein Sankey-Dialogramm erzeugt werden? */
		public final boolean hasSankey;

		/**
		 * Konstruktor
		 * @param hasSankey	Kann auf Basis dieser Daten ein Sankey-Dialogramm erzeugt werden?
		 */
		Mode(final boolean hasSankey) {
			this.hasSankey=hasSankey;
		}
	}

	private final Statistics statistics;
	private final Mode mode;

	/**
	 * Konstruktor der Klasse
	 * @param statistics	Statistikobjekt, aus dem die anzuzeigenden Daten entnommen werden sollen
	 * @param mode	Welche Informationen sollen angezeigt werden?
	 * @see Mode
	 */
	public StatisticViewerMovementTable(final Statistics statistics, final Mode mode) {
		this.statistics=statistics;
		this.mode=mode;
	}

	private void addDescription(final String topic) {
		final URL url=StatisticViewerMovementTable.class.getResource("description_"+Language.getCurrentLanguage()+"/"+topic+".html");
		addDescription(url,helpTopic->Help.topic(getViewer(false),helpTopic));
	}

	/**
	 * Erstellt eine Tabelle mit den Kundenübergängen.
	 * @param statistics	Statistikobjekt dem die Daten entnommen werden sollen
	 * @return	Tabelle mit den Kundenübergängen
	 */
	public static Table getClientMovementTable(final Statistics statistics) {
		final Table table=new Table();

		for (String name: statistics.stationTransition.getNames()) {
			final String[] line=new String[3];
			final String[] parts=name.split(" -> ");
			if (parts==null || parts.length!=2) continue;
			line[0]=parts[0];
			line[1]=parts[1];
			final StatisticsSimpleCountPerformanceIndicator counter=(StatisticsSimpleCountPerformanceIndicator)statistics.stationTransition.get(name);
			line[2]=NumberTools.formatLongNoGrouping(counter.get());
			table.addLine(line);
		}
		return table;
	}

	/**
	 * Erstellt eine Tabelle mit den Kundenpfaden.
	 * @param statistics	Statistikobjekt dem die Daten entnommen werden sollen
	 * @param addPercentColumn	Zusätzliche Spalte mit den Anteilen für den Pfad anfügen
	 * @return	Tabelle mit den Kundenpfaden
	 */
	public static Table getClientPathsTable(final Statistics statistics, final boolean addPercentColumn) {
		final Table table=new Table();

		long sum=0;
		if (addPercentColumn) for (String name: statistics.clientPaths.getNames()) {
			final StatisticsSimpleCountPerformanceIndicator counter=(StatisticsSimpleCountPerformanceIndicator)statistics.clientPaths.get(name);
			sum+=counter.get();
		}

		for (String name: statistics.clientPaths.getNames()) {
			final String[] line=(addPercentColumn)?new String[3]:new String[2];
			line[0]=name;
			final StatisticsSimpleCountPerformanceIndicator counter=(StatisticsSimpleCountPerformanceIndicator)statistics.clientPaths.get(name);
			final long value=counter.get();
			line[1]=NumberTools.formatLongNoGrouping(value);
			if (addPercentColumn) line[2]=NumberTools.formatPercent(((double)value)/sum);
			table.addLine(line);
		}
		return table;
	}

	/**
	 * Liefert ein Statistikobjekt, welches die Kenngrößen der Pfadlängen enthält.
	 * @param statistics	Statistikobjekt dem die Daten entnommen werden sollen
	 * @return	Informationen zu den Pfadlängen
	 */
	public static StatisticsDataPerformanceIndicator getClientPathLengths(final Statistics statistics) {
		final StatisticsDataPerformanceIndicator result=new StatisticsDataPerformanceIndicator(null,1000,1000);

		for (String name: statistics.clientPaths.getNames()) {
			final int length=name.split(" -> ").length;
			final long count=((StatisticsSimpleCountPerformanceIndicator)statistics.clientPaths.get(name)).get();
			result.add(length,count);
		}

		return result;
	}

	/**
	 * Liefert eine Tabelle, die die Kenngrößen für die Pfadlängen enthält
	 * @param statistics	Statistikobjekt dem die Daten entnommen werden sollen
	 * @return	Tabelle mit den Kenngrößen für die Pfadlängen
	 */
	public static Table getClientPathLengthsTable(final Statistics statistics) {
		final StatisticsDataPerformanceIndicator indicator=getClientPathLengths(statistics);
		final Table table=new Table();
		final List<String> row=new ArrayList<>();
		row.add(NumberTools.formatLong(indicator.getCount()));
		row.add(NumberTools.formatNumber(indicator.getMean()));
		row.add(NumberTools.formatNumber(indicator.getSD()));
		row.add(NumberTools.formatNumber(indicator.getVar()));
		row.add(NumberTools.formatNumber(indicator.getCV()));
		row.add(NumberTools.formatNumber(indicator.getMin()));
		row.add(NumberTools.formatNumber(indicator.getMax()));
		if (SetupData.getSetup().showQuantils) for (double p: StatisticsDataPerformanceIndicator.storeQuantilValues) {
			row.add(NumberTools.formatNumber(indicator.getQuantil(p)));
		}
		table.addLine(row);
		return table;
	}

	/**
	 * Liefert eine Tabelle die eine Häufigkeitsverteilung der Pfadlängen enthält.
	 * @param statistics	Statistikobjekt dem die Daten entnommen werden sollen
	 * @return	Tabelle mit der Häufigkeitsverteilung der Pfadlängen
	 */
	public static Table getClientPathLengthsDistributionTable(final Statistics statistics) {
		final StatisticsDataPerformanceIndicator indicator=getClientPathLengths(statistics);
		final Table table=new Table();

		final double count=indicator.getCount();
		final double[] density=indicator.getDistribution().densityData;
		final int max=Math.min(density.length-1,(int)Math.ceil(indicator.getMax()));
		for (int i=0;i<=max;i++) {
			final List<String> row=new ArrayList<>();
			row.add(NumberTools.formatLongNoGrouping(i));
			row.add(NumberTools.formatLongNoGrouping(Math.round(density[i])));
			row.add(NumberTools.formatPercent(density[i]/count));
			table.addLine(row);
		}
		return table;
	}

	private static String[] getColumnNames() {
		final List<String> columns=new ArrayList<>();
		columns.add(Language.tr("Statistics.ClientPathRecording.Count"));
		columns.add("E");
		columns.add("Std");
		columns.add("Var");
		columns.add("CV");
		columns.add("Min");
		columns.add("Max");
		if (SetupData.getSetup().showQuantils) for (double p: StatisticsDataPerformanceIndicator.storeQuantilValues) {
			columns.add(NumberTools.formatPercent(p)+" "+Language.tr("Statistics.Quantil"));
		}

		return columns.toArray(new String[0]);
	}

	@Override
	protected void buildTable() {
		switch (mode) {
		case STATION_TRANSITION:
			setData(getClientMovementTable(statistics),new String[]{
					Language.tr("Statistics.ClientMovement.From"),
					Language.tr("Statistics.ClientMovement.To"),
					Language.tr("Statistics.ClientMovement.Count")
			});
			addDescription("ClientMovement");
			break;
		case CLIENT_PATHS:
			setData(getClientPathsTable(statistics,true),new String[]{
					Language.tr("Statistics.ClientPathRecording.Path"),
					Language.tr("Statistics.ClientPathRecording.Count"),
					Language.tr("Statistics.ClientPathRecording.Part")
			});
			addDescription("ClientPaths");
			break;
		case CLIENT_PATH_LENGTHS:
			setData(getClientPathLengthsTable(statistics),getColumnNames());
			addDescription("TableClientPathLengths");
			break;
		case CLIENT_PATH_LENGTHS_DISTRIBUTION:
			setData(getClientPathLengthsDistributionTable(statistics),new String[]{
					Language.tr("Statistics.ClientPathRecording.PathLength"),
					Language.tr("Statistics.ClientPathRecording.Count"),
					Language.tr("Statistics.ClientPathRecording.Part")
			});
			addDescription("TableClientPathLengthsDistribution");
			break;
		}
	}

	@Override
	public JButton[] getAdditionalButton() {
		final List<JButton> buttons=new ArrayList<>();
		final JButton[] superButtons=super.getAdditionalButton();
		if (superButtons!=null) buttons.addAll(Arrays.asList(superButtons));

		if (mode.hasSankey) {
			final JButton button=new JButton(Language.tr("Statistics.ClientMovement.Sankey"));
			button.setToolTipText(Language.tr("Statistics.ClientMovement.Sankey.Tooltip"));
			button.setIcon(Images.STATISTICS_DIAGRAM_SANKEY.getIcon());
			button.addActionListener(e->{
				switch (mode) {
				case STATION_TRANSITION:
					new CreateSankey(getViewer(false),getClientMovementTable(statistics),CreateSankey.Mode.STATION_TRANSITION);
					break;
				case CLIENT_PATHS:
					new CreateSankey(getViewer(false),getClientPathsTable(statistics,false),CreateSankey.Mode.CLIENT_PATHS);
					break;
				default:
					break;
				}
			});
			buttons.add(button);
		}

		return buttons.toArray(new JButton[0]);
	}
}

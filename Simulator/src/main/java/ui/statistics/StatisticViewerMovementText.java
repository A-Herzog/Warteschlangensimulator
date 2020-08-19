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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;

import language.Language;
import mathtools.NumberTools;
import mathtools.Table;
import simulator.statistics.Statistics;
import statistics.StatisticsDataPerformanceIndicator;
import systemtools.statistics.StatisticViewerText;
import tools.SetupData;
import ui.help.Help;
import ui.images.Images;

/**
 * Dieser Viewer stellt die Übergänge der Kunden zwischen den Stationen
 * als Text dar und bietet an, diese als Sankey-Diagramm zu exportieren.
 * @author Alexander Herzog
 * @see StatisticViewerText
 * @see CreateSankey
 */
public class StatisticViewerMovementText extends StatisticViewerText {
	/**
	 * Welche Informationen sollen angezeigt werden?
	 * @author Alexander Herzog
	 * @see StatisticViewerMovementText#StatisticViewerMovementText(Statistics, Mode)
	 */
	public enum Mode {
		/** Übergänge zwischen Stationen */
		STATION_TRANSITION(true),
		/** Pfade der Kunden */
		CLIENT_PATHS(true),
		/** Pfadlängen */
		CLIENT_PATH_LENGTHS(false);

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
	public StatisticViewerMovementText(final Statistics statistics, final Mode mode) {
		this.statistics=statistics;
		this.mode=mode;
	}

	private void addDescription(final String topic) {
		final URL url=StatisticViewerMovementText.class.getResource("description_"+Language.getCurrentLanguage()+"/"+topic+".html");
		addDescription(url,helpTopic->Help.topic(getViewer(false),helpTopic));
	}

	private void buildMovementText() {
		addHeading(1,Language.tr("Statistics.ClientMovement"));

		final Table table=StatisticViewerMovementTable.getClientMovementTable(statistics);
		final int rows=table.getSize(0);
		Map<String,Map<String,Long>> stations;

		addHeading(2,Language.tr("Statistics.ClientMovement.Heading.From"));

		stations=new HashMap<>();
		for (int i=0;i<rows;i++) {
			final List<String> row=table.getLine(i);
			if (row==null || row.size()!=3) continue;
			final Long L=NumberTools.getLong(row.get(2));
			if (L==null) continue;
			Map<String,Long> sub=stations.get(row.get(0));
			if (sub==null) stations.put(row.get(0),sub=new HashMap<>());
			sub.put(row.get(1),L);
		}

		for (Map.Entry<String,Map<String,Long>> station: stations.entrySet()) {
			addHeading(3,station.getKey()+" ->");
			beginParagraph();
			final Map<String,Long> subs=station.getValue();
			long sum=Math.max(1,subs.values().stream().mapToLong(Long::longValue).sum());
			for (Map.Entry<String,Long> sub: subs.entrySet()) {
				final long value=sub.getValue();
				addLine(String.format("-> %s (%s, %s)",sub.getKey(),NumberTools.formatLong(value),StatisticTools.formatPercent(((double)value)/sum)));
			}
			endParagraph();
		}

		addHeading(2,Language.tr("Statistics.ClientMovement.Heading.To"));

		stations=new HashMap<>();
		for (int i=0;i<rows;i++) {
			final List<String> row=table.getLine(i);
			if (row==null || row.size()!=3) continue;
			final Long L=NumberTools.getLong(row.get(2));
			if (L==null) continue;
			Map<String,Long> sub=stations.get(row.get(1));
			if (sub==null) stations.put(row.get(1),sub=new HashMap<>());
			sub.put(row.get(0),L);
		}

		for (Map.Entry<String,Map<String,Long>> station: stations.entrySet()) {
			addHeading(3,"-> "+station.getKey());
			beginParagraph();
			final Map<String,Long> subs=station.getValue();
			long sum=Math.max(1,subs.values().stream().mapToLong(Long::longValue).sum());
			for (Map.Entry<String,Long> sub: subs.entrySet()) {
				final long value=sub.getValue();
				addLine(String.format("%s (%s, %s) ->",sub.getKey(),NumberTools.formatLong(value),StatisticTools.formatPercent(((double)value)/sum)));
			}
			endParagraph();
		}

		/* Infotext  */
		addDescription("ClientMovement");
	}

	private final static int MAX_ROWS=2_000;

	private void buildPathText() {
		addHeading(1,Language.tr("Statistics.ClientPathRecording"));

		final Table table=StatisticViewerMovementTable.getClientPathsTable(statistics,true);
		final int rows=table.getSize(0);

		final int showRows=Math.min(MAX_ROWS,rows);
		for (int i=0;i<showRows;i++) {
			final List<String> row=table.getLine(i);
			if (row==null || row.size()!=3) continue;
			beginParagraph();
			addLine(row.get(0));
			final Long count=NumberTools.getLong(row.get(1));
			addLine(String.format("%s, %s",NumberTools.formatLong(count.longValue()),row.get(2)));
			endParagraph();
		}
		if (rows!=showRows) {
			beginParagraph();
			addLine(String.format(Language.tr("Statistics.ClientPathRecording.Info"),NumberTools.formatLong(MAX_ROWS)));
			endParagraph();
		}

		/* Infotext  */
		addDescription("ClientPaths");
	}

	private void buildPathLengths() {
		addHeading(1,Language.tr("Statistics.ClientPathLengths"));

		final StatisticsDataPerformanceIndicator indicator=StatisticViewerMovementTable.getClientPathLengths(statistics);

		beginParagraph();
		addLine(Language.tr("Statistics.CountPathLength")+": "+NumberTools.formatLong(indicator.getCount()));
		addLine(Language.tr("Statistics.AveragePathLength")+": E="+StatisticTools.formatNumber(indicator.getMean()));
		addLine(Language.tr("Statistics.StdDevPathLength")+": Std="+StatisticTools.formatNumber(indicator.getSD()));
		addLine(Language.tr("Statistics.VariancePathLength")+": Var="+StatisticTools.formatNumber(indicator.getVar()));
		addLine(Language.tr("Statistics.CVInterPathLength")+": CV="+StatisticTools.formatNumber(indicator.getCV()));
		addLine(Language.tr("Statistics.MinimalPathLength")+": Min="+StatisticTools.formatNumber(indicator.getMin()));
		addLine(Language.tr("Statistics.MaximalPathLength")+": Max="+StatisticTools.formatNumber(indicator.getMax()));
		endParagraph();

		if (SetupData.getSetup().showQuantils && indicator.getDistribution()!=null) {
			final double upperBound=indicator.getDistribution().upperBound-1;
			beginParagraph();
			boolean hitMax=false;
			final double[] levels=StatisticViewerOverviewText.getQuantilLevels();
			for (double p: levels) {
				final String name=Language.tr("Statistics.Quantil")+"["+StatisticTools.formatPercent(p)+"]=";
				final double value=indicator.getQuantil(p);
				if (value>=upperBound) hitMax=true;
				addLine(name+StatisticTools.formatNumber(value));
			}
			endParagraph();

			if (hitMax) {
				beginParagraph();
				addLine(String.format(Language.tr("Statistics.Quantil.InfoMax"),StatisticTools.formatNumber(upperBound)));
				endParagraph();
			}
		}

		/* Infotext  */
		addDescription("ClientPathLengths");
	}

	@Override
	protected void buildText() {
		switch (mode) {
		case STATION_TRANSITION: buildMovementText(); break;
		case CLIENT_PATHS: buildPathText(); break;
		case CLIENT_PATH_LENGTHS: buildPathLengths(); break;
		}
	}

	@Override
	public JButton[] getAdditionalButton() {
		if (!mode.hasSankey) return null;

		final JButton button=new JButton(Language.tr("Statistics.ClientMovement.Sankey"));
		button.setToolTipText(Language.tr("Statistics.ClientMovement.Sankey.Tooltip"));
		button.setIcon(Images.STATISTICS_DIAGRAM_SANKEY.getIcon());
		button.addActionListener(e->{
			switch (mode) {
			case STATION_TRANSITION:
				new CreateSankey(getViewer(false),StatisticViewerMovementTable.getClientMovementTable(statistics),CreateSankey.Mode.STATION_TRANSITION);
				break;
			case CLIENT_PATHS:
				new CreateSankey(getViewer(false),StatisticViewerMovementTable.getClientPathsTable(statistics,false),CreateSankey.Mode.CLIENT_PATHS);
				break;
			default:
				break;
			}
		});
		return new JButton[]{button};
	}
}

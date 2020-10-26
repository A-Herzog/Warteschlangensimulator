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
package ui.quickaccess;

import java.net.URL;
import java.util.function.Consumer;

import language.Language;
import systemtools.statistics.StatisticNode;
import systemtools.statistics.StatisticTreeCellRenderer;
import ui.images.Images;
import ui.statistics.StatisticsPanel;

/**
 * Erstellt Schnellzugriffeinträge basierend auf der Baumstruktur der Statistik
 * @author Alexander Herzog
 * @see JQuickAccessBuilder
 */
public class JQuickAccessBuilderStatistics extends JQuickAccessBuilder {
	/**
	 * Konstruktor der Klasse
	 * @param quickAccessText	Eingegebener Text
	 */
	public JQuickAccessBuilderStatistics(final String quickAccessText) {
		super(Language.tr("QuickAccess.Statistics"),Language.tr("QuickAccess.Statistics.Hint"),quickAccessText,true);
	}

	/**
	 * Führt die eigentliche Verarbeitung durch.
	 * @param statisticsPanel	Statistikpanel, dem die Daten entnommen werden
	 * @param showStatisticsPanel	Callback zum Aufruf des Statistikpanels
	 */
	public void work(final StatisticsPanel statisticsPanel, final Runnable showStatisticsPanel) {
		if (quickAccessText.length()<2) return;

		final Consumer<JQuickAccessRecord> callback=record->{
			statisticsPanel.selectNode((StatisticNode)record.data);
			showStatisticsPanel.run();
		};

		final StatisticNode root=statisticsPanel.getStatisticNodeRoot();
		if (root!=null) processBranch(root,null,callback);
	}

	/**
	 * Verarbeitet einen Zweig in der Statistik
	 * @param branch	Zweig in der Statistik-Baumstruktur
	 * @param parentPath	Text-Pfad zu dem aktuellen Zweig
	 * @param callback	Aktion beim Anklicken des Suchtreffers
	 * @see #work(StatisticsPanel, Runnable)
	 */
	private void processBranch(final StatisticNode branch, final String parentPath, final Consumer<JQuickAccessRecord> callback) {
		if (branch.getChildCount()==0) {
			URL url=null;
			if (branch.viewer!=null && branch.viewer.length>0) url=StatisticTreeCellRenderer.getStatisticViewerIconURL(branch.viewer[0]);
			if (url==null) {
				test(parentPath,branch.toString(),Images.STATISTICS_DARK.getIcon(),callback,branch);
			} else {
				test(parentPath,branch.toString(),url,callback,branch);
			}
			return;
		}

		final String path=String.join(" - ",branch.getFullName());
		for (int i=0;i<branch.getChildCount();i++) {
			processBranch(branch.getChild(i),path.isEmpty()?null:path,callback);
		}
	}
}

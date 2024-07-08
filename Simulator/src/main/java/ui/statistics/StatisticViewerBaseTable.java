/**
 * Copyright 2021 Alexander Herzog
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

import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

import language.Language;
import systemtools.BaseDialog;
import systemtools.images.SimToolsImages;
import systemtools.statistics.StatisticViewerTable;
import systemtools.statistics.StatisticsBasePanel;
import ui.images.Images;

/**
 * Basisklasse für alle Tabellen-Statistik-Viewer.<br>
 * (Diese Klasse stellt ein Einstellungen-Menü mit der Möglichkeit zur Konfiguration
 * von anzuzeigenden Nachkommastellen usw. an.)
 * @author Alexander Herzog
 * @see StatisticViewerTable
 */
public class StatisticViewerBaseTable extends StatisticViewerTable {
	/**
	 * Konstruktor der Klasse
	 */
	public StatisticViewerBaseTable() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	@Override
	public String[] ownSettingsName() {
		final List<String> names=new ArrayList<>();
		names.add(Language.tr("Statistics.TextSettings.DropdownName"));
		names.add(Language.tr("Statistic.Viewer.Context.Filter")+"...");
		return names.toArray(String[]::new);
	}

	@Override
	public Icon[] ownSettingsIcon() {
		final List<Icon> icons=new ArrayList<>();
		icons.add(Images.GENERAL_NUMBERS.getIcon());
		icons.add(SimToolsImages.STATISTICS_TABLE_FILTER.getIcon());
		return icons.toArray(Icon[]::new);
	}

	@Override
	public boolean ownSettings(final StatisticsBasePanel owner, final int nr) {
		boolean changed=false;
		switch (nr) {
		case 0:
			final BaseDialog dialog=new StatisticViewerOverviewTextDialog(owner);
			changed=(dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK);
			if (changed) owner.recreateViewers();
			break;
		case 1:
			final int col=showColSelectDialog();
			if (col>=0) changed=showColValueSelectFilterDialog(col);
			break;
		}
		if (changed) owner.recreateViewers();
		return changed;
	}
}

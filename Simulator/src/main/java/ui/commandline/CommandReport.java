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
package ui.commandline;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import language.Language;
import systemtools.commandline.AbstractReportCommand;
import ui.statistics.StatisticsPanel;

/**
 * Erstellt basierend auf einer Statistik-Datei einen Report und speichert ihn oder ein Dokument daraus.
 * @author Alexander Herzog
 */
public class CommandReport extends AbstractReportCommand {
	/**
	 * Konstruktor der Klasse
	 */
	public CommandReport() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	@Override
	public String[] getKeys() {
		List<String> list=new ArrayList<>();
		list.add(Language.tr("CommandLine.Report.Name"));
		for (String s: Language.trOther("CommandLine.Report.Name")) if (!list.contains(s)) list.add(s);
		return list.toArray(new String[0]);
	}

	@Override
	public String getShortDescription() {
		return Language.tr("CommandLine.Report.Description.Short");
	}

	@Override
	protected Object getReportCommandConnect(File input) {
		StatisticsPanel panel=new StatisticsPanel(null);
		String s=panel.loadStatistics(input); if (s!=null) return s;
		return panel;
	}
}

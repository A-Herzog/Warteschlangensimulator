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
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import language.Language;
import mathtools.Table;
import scripting.java.DynamicFactory;
import scripting.java.DynamicRunner;
import scripting.java.DynamicStatus;
import scripting.java.OutputImpl;
import scripting.java.StatisticsImpl;
import scripting.js.JSRunDataFilter;
import scripting.js.JSRunDataFilterTools;
import simulator.statistics.Statistics;
import systemtools.commandline.AbstractCommand;
import systemtools.commandline.BaseCommandLineSystem;
import ui.script.ScriptPanel;
import ui.statistics.FilterList;
import ui.statistics.FilterListFormat;
import ui.statistics.FilterListRecord;

/**
 * Extrahiert ein Einzelergebnis aus einer Statistik-xml-Datei.
 * @author Alexander Herzog
 * @see AbstractCommand
 * @see CommandLineSystem
 */
public final class CommandFilter extends AbstractSimulationCommand {
	/** Statistikeingabedatei */
	private File statisticsInputFile;
	/** Filterskript */
	private File filterFile;
	/** Ausgabedatei */
	private File filterResultFile;

	@Override
	public String[] getKeys() {
		List<String> list=new ArrayList<>();
		list.add(Language.tr("CommandLine.Filter.Name"));
		for (String s: Language.trOther("CommandLine.Filter.Name")) if (!list.contains(s)) list.add(s);
		return list.toArray(new String[0]);
	}

	@Override
	public String getShortDescription() {
		return Language.tr("CommandLine.Filter.Description.Short");
	}

	@Override
	public String[] getLongDescription() {
		return Language.tr("CommandLine.Filter.Description.Long").split("\n");
	}

	@Override
	public String prepare(String[] additionalArguments, InputStream in, PrintStream out) {
		String s=parameterCountCheck(3,additionalArguments); if (s!=null) return s;

		statisticsInputFile=new File(additionalArguments[0]);
		filterFile=new File(additionalArguments[1]);
		filterResultFile=new File(additionalArguments[2]);
		if (!statisticsInputFile.isFile()) return String.format(Language.tr("CommandLine.Error.File.InputDoesNotExist"),statisticsInputFile);
		if (!isStatisticFile(statisticsInputFile)) return String.format(Language.tr("CommandLine.Error.File.InputNoValidStatisticFile"),statisticsInputFile);
		if (!filterFile.isFile()) return String.format(Language.tr("CommandLine.Error.File.ConfigDoesNotExist"),filterFile);
		if (filterResultFile.isDirectory()) return String.format(Language.tr("CommandLine.Error.File.OutputFileIsFolder"),filterResultFile);
		return null;
	}

	/**
	 * Führt die eigentliche Verarbeitung durch.
	 * @param statistic	Statistikdaten, auf denen der Filter operieren soll
	 * @param commands	Auszuführender Filter
	 * @param results	Ausgabedatei
	 * @param out	Konsolen-Ausgabe (für Fehlermeldungen usw.)
	 * @return	Liefert <code>true</code>, wenn der Filter erfolgreich angewandt werden konnte und die Ergebnisse gespeichert werden konnten
	 */
	public static boolean runFilter(final Statistics statistic, final String commands, final File results, final PrintStream out) {
		boolean error=false;
		String result=null;

		/* Verarbeitung */

		final FilterList filterList=new FilterList();
		if (filterList.load(commands)) {
			final FilterListFormat format=new FilterListFormat();
			final StringBuilder sb=new StringBuilder();
			final List<FilterListRecord> list=filterList.getList();
			for (int i=0;i<list.size();i++) {
				sb.append(list.get(i).process(statistic,format));
			}
			if (sb.length()>0) result=sb.toString();
		}

		if (result==null) switch (ScriptPanel.getScriptType(commands)) {
		case Javascript:
			final JSRunDataFilter dataFilter=new JSRunDataFilter(statistic.saveToXMLDocument(),statistic.loadedStatistics);
			dataFilter.run(commands);
			result=dataFilter.getResults();
			error=!dataFilter.getLastSuccess();
			break;
		case Java:
			final DynamicRunner runner=DynamicFactory.getFactory().load(commands,null,null);
			if (runner.getStatus()!=DynamicStatus.OK) {
				error=true;
				result=DynamicFactory.getLongStatusText(runner);
			} else {
				final StringBuilder sb=new StringBuilder();
				runner.parameter.output=new OutputImpl(line->sb.append(line),false);
				runner.parameter.statistics=new StatisticsImpl(line->sb.append(line),statistic.saveToXMLDocument(),statistic.loadedStatistics,false);
				runner.run();
				if (runner.getStatus()!=DynamicStatus.OK) {
					error=true;
					result=DynamicFactory.getLongStatusText(runner);
				} else {
					result=sb.toString();
				}
			}
			break;
		}

		/* Ausgabe der Ergebnisse */

		if (error) {
			out.println(Language.tr("CommandLine.Filter.Done.Error")+":");
			out.println(result);
			return false;
		}

		if (result==null) {
			out.println(Language.tr("CommandLine.Filter.Done.Error")+":");
			out.println(Language.tr("CommandLine.Filter.Done.Error.CouldNotProcess"));
			return false;
		}

		if (JSRunDataFilterTools.saveText(result,results,true)) {
			out.println(Language.tr("CommandLine.Filter.Done.Success"));
			return true;
		} else {
			out.println(String.format(Language.tr("CommandLine.Filter.Done.CouldNotSave"),results.toString()));
			return false;
		}
	}

	@Override
	public void run(AbstractCommand[] allCommands, InputStream in, PrintStream out) {
		final Statistics inputStatistics=new Statistics();
		final String error=inputStatistics.loadFromFile(statisticsInputFile);
		if (error!=null) {
			out.println(BaseCommandLineSystem.errorBig+": "+Language.tr("CommandLine.Filter.ErrorLoadingStatistic")+": "+error);
			return;
		}
		inputStatistics.loadedStatistics=statisticsInputFile;

		final String commands=Table.loadTextFromFile(filterFile);
		if (commands==null) {
			out.println(BaseCommandLineSystem.errorBig+": "+Language.tr("CommandLine.Filter.ErrorLoadingFilterConfiguration"));
			return;
		}
		runFilter(inputStatistics,commands,filterResultFile,out);
	}
}

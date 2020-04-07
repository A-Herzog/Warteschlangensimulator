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

import language.Language;
import mathtools.NumberTools;
import mathtools.TimeTools;
import simulator.statistics.Statistics;
import systemtools.statistics.StatisticViewerText;
import ui.help.Help;
import ui.statistics.analyticcompare.AnalyticInfo;

/**
 * Vergleicht die Statistikergebnisse eines Modells mit einem zugehörigen Erlang-C-Modell.
 * @author Alexander Herzog
 */
public class StatisticViewerErlangCCompare extends StatisticViewerText {
	private final Statistics statistics;

	/**
	 * Konstruktor der Klasse
	 * @param statistics	Statistikobjekt, aus dem die anzuzeigenden Daten entnommen werden sollen
	 */
	public StatisticViewerErlangCCompare(final Statistics statistics) {
		this.statistics=statistics;
	}

	private void addDescription(final String topic) {
		final URL url=StatisticViewerOverviewText.class.getResource("description_"+Language.getCurrentLanguage()+"/"+topic+".html");
		addDescription(url,helpTopic->Help.topic(getViewer(false),helpTopic));
	}

	@Override
	protected void buildText() {
		addHeading(1,Language.tr("Statistics.ErlangCCompare"));

		final AnalyticInfo analyticInfo=new AnalyticInfo();
		if (!analyticInfo.build(statistics.editModel)) {buildNotCompareable(); return;}
		final AnalyticInfo.SimulationResults simulationResults=new AnalyticInfo.SimulationResults(statistics);

		buildTextModelInfo(analyticInfo);
		buildTextSimResults(simulationResults);
		buildTextAnalytic(analyticInfo,simulationResults);

		/* Infotext  */
		addDescription("ErlangCCompare");
	}

	private void buildNotCompareable() {
		beginParagraph();
		addLine(Language.tr("Statistics.ErlangCCompare.NotCompareable"));
		endParagraph();
	}

	private void buildTextModelInfo(final AnalyticInfo analyticInfo) {
		addHeading(2,Language.tr("Statistics.ErlangCCompare.SimModel"));

		addHeading(3,Language.tr("Statistics.ErlangCompare.Arrival"));
		beginParagraph();
		addLines(analyticInfo.getSourceInfo());
		endParagraph();

		addHeading(3,Language.tr("Statistics.ErlangCompare.Service"));
		beginParagraph();
		addLines(analyticInfo.getProcessInfo());
		endParagraph();
	}

	private void buildTextSimResults(final AnalyticInfo.SimulationResults simulationResults) {
		addHeading(2,Language.tr("Statistics.ErlangCCompare.SimResults"));

		addHeading(3,Language.tr("Statistics.ErlangCompare.NumberOfClients"));
		beginParagraph();
		addLine("E[NQ]="+NumberTools.formatNumber(simulationResults.ENQ));
		addLine("E[NS]="+NumberTools.formatNumber(simulationResults.ENS));
		addLine("E[N]="+NumberTools.formatNumber(simulationResults.EN));
		beginParagraph();

		addHeading(3,Language.tr("Statistics.ErlangCompare.Times"));
		beginParagraph();
		addLine("E[W]="+NumberTools.formatNumber(simulationResults.EW)+" "+Language.tr("Statistics.Seconds")+" ("+TimeTools.formatExactTime(simulationResults.EW,1)+")");
		addLine("E[V]="+NumberTools.formatNumber(simulationResults.EV)+" "+Language.tr("Statistics.Seconds")+" ("+TimeTools.formatExactTime(simulationResults.EV,1)+")");
		endParagraph();

		if (!simulationResults.resourceInfo.isEmpty()) {
			addHeading(3,Language.tr("Statistics.Utilization"));
			beginParagraph();
			addLines(simulationResults.resourceInfo);
			endParagraph();
		}
	}

	private void outputAnalyticResults(final AnalyticInfo.InfoResult results) {
		if (results==null) return;

		addHeading(3,Language.tr("Statistics.ErlangCCompare.Results.Input"));
		beginParagraph();
		addLines(results.getInput());
		endParagraph();

		final String calculated=results.getCalculated();
		if (!calculated.isEmpty()) {
			addHeading(3,Language.tr("Statistics.ErlangCCompare.Results.Calculated"));
			beginParagraph();
			addLines(calculated);
			endParagraph();
		}

		final String numbers=results.getNumbers();
		if (!numbers.isEmpty()) {
			addHeading(3,Language.tr("Statistics.ErlangCompare.NumberOfClients"));
			beginParagraph();
			addLines(numbers);
			endParagraph();
		}

		final String times=results.getTimes();
		if (!times.isEmpty()) {
			addHeading(3,Language.tr("Statistics.ErlangCompare.Times"));
			beginParagraph();
			addLines(times);
			endParagraph();
		}
	}

	private void buildTextAnalytic(final AnalyticInfo analyticInfo, final AnalyticInfo.SimulationResults simulationResults) {
		addHeading(2,Language.tr("Statistics.ErlangCCompare.Results.ErlangC"));
		outputAnalyticResults(analyticInfo.getErlangC(simulationResults));

		if (analyticInfo.hasCancelTimes()) {
			addHeading(2,Language.tr("Statistics.ErlangCCompare.Results.ErlangCExt"));
			outputAnalyticResults(analyticInfo.getErlangCExt(simulationResults));
		}

		addHeading(2,Language.tr("Statistics.ErlangCCompare.Results.AllenCunneen"));
		outputAnalyticResults(analyticInfo.getAllenCunneen(simulationResults));
	}
}
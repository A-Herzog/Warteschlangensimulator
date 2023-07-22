/**
 * Copyright 2023 Alexander Herzog
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
package simulator.simparser.symbols;

import simulator.coreelements.RunElementData;
import simulator.simparser.coresymbols.CalcSymbolStationData;
import simulator.statistics.Statistics;
import statistics.StatisticsDataPerformanceIndicator;

/**
 * Im Falle von einem Parameter:<br>
 * (a) Liefert den Flussgrad der Kunden, deren Name an Quelle bzw. Namenszuweisung id (1. Parameter) auftritt.<br>
 * (b) Liefert den Flussgrad der Kunden an Station id (1. Parameter).<br>
 * Im Falle von zwei Parameter:<br>
 * Liefert den Flussgrad der Kunden die an der Multiquelle mit der id aus Parameter 1 als Eintrag gemäß Parameter 2 generiert wurden.<br>
 * Im Falle von keinem Parameter:<br>
 * Liefert den Flussgrad über alle Kunden.
 * @author Alexander Herzog
 */
public class CalcSymbolStationFlowFactor extends CalcSymbolStationData {
	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{
			"Flussgrad","FlowFactor"
	};

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolStationFlowFactor() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	@Override
	public String[] getNames() {
		return names;
	}

	@Override
	protected double calc(final RunElementData data) {
		if (data.statisticProcess==null || data.statisticResidence==null) return 0;
		final double serviceTime=data.statisticProcess.getMean();
		if (serviceTime==0.0) return 0.0;
		return data.statisticResidence.getMean()/serviceTime;
	}

	@Override
	protected boolean hasAllData() {
		return true;
	}

	@Override
	protected boolean hasSingleClientData() {
		return true;
	}

	@Override
	protected double calcAll() {
		final Statistics statistics=getSimData().statistics;
		final double serviceTime=statistics.clientsAllProcessingTimes.getMean();
		if (serviceTime==0.0) return 0.0;
		return statistics.clientsAllResidenceTimes.getMean()/serviceTime;
	}

	@Override
	protected double calcSingleClient(final String name) {
		final Statistics statistics=getSimData().statistics;
		final StatisticsDataPerformanceIndicator indicatorService=getClientDataIndicator(name,statistics.clientsProcessingTimes);
		final StatisticsDataPerformanceIndicator indicatorResidence=getClientDataIndicator(name,statistics.clientsResidenceTimes);
		if (indicatorService==null || indicatorResidence==null) return 0.0;
		final double serviceTime=indicatorService.getMean();
		if (serviceTime==0.0) return 0.0;
		return indicatorResidence.getMean()/serviceTime;
	}

	@Override
	protected boolean hasStationAndClientData() {
		return true;
	}

	@Override
	protected double calcStationClient(final RunElementData data, final int clientTypeIndex) {
		if (data.statisticProcessByClientType==null || data.statisticResidenceByClientType==null) return 0.0;
		final double serviceTime=data.statisticProcessByClientType[clientTypeIndex].getMean();
		if (serviceTime==0.0) return 0.0;
		return data.statisticResidenceByClientType[clientTypeIndex].getMean()/serviceTime;
	}
}

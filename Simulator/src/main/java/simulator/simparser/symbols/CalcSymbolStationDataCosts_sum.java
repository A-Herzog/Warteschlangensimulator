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
package simulator.simparser.symbols;

import simulator.coreelements.RunElementData;
import simulator.simparser.coresymbols.CalcSymbolStationData;
import statistics.StatisticsValuePerformanceIndicator;

/**
 * Im Falle von einem Parameter:<br>
 * Liefert die Stationskosten, die bisher in Summe an Station id (1. Parameter) aufgetreten sind.<br>
 * Im Falle von keinem Parameter:<br>
 * Liefert die Stationskosten, die bisher in Summe an allen Stationen aufgetreten sind.
 * @author Alexander Herzog
 */
public class CalcSymbolStationDataCosts_sum extends CalcSymbolStationData {
	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"Costs","Kosten"};

	@Override
	public String[] getNames() {
		return names;
	}

	@Override
	protected boolean hasAllData() {
		return true;
	}

	@Override
	protected double calcAll() {
		double sum=0;

		for (StatisticsValuePerformanceIndicator indicator: (StatisticsValuePerformanceIndicator[])getSimData().statistics.stationCosts.getAll(StatisticsValuePerformanceIndicator.class)) sum+=indicator.getValue();

		return sum;
	}

	@Override
	protected double calc(final RunElementData data) {
		StatisticsValuePerformanceIndicator indicator=(StatisticsValuePerformanceIndicator)getSimData().statistics.stationCosts.get(data.station.name);
		if (indicator==null) return 0.0;
		return indicator.getValue();
	}
}
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
import statistics.StatisticsDataPerformanceIndicator;
import statistics.StatisticsPerformanceIndicator;

/**
 * (a) Liefert die Summe der Wartezeiten der Kunden, deren Name an Quelle bzw. Namenszuweisung id (1. Parameter) auftritt (in Sekunden).<br>
 * (b) Liefert die Summe der an Station id (1. Parameter) bisher angefallenen Wartezeiten (in Sekunden).
 * @author Alexander Herzog
 */
public class CalcSymbolStationDataWaiting_sum extends CalcSymbolStationData {
	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{
			"Wartezeit_sum","Wartezeit_gesamt","Wartezeit_summe",
			"WaitingTime_sum","WaitingTime_gesamt","WaitingTime_summe"
	};

	@Override
	public String[] getNames() {
		return names;
	}

	@Override
	protected double calc(final RunElementData data) {
		if (data.statisticWaiting==null) return 0;
		return data.statisticWaiting.getSum();
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
		return getSimData().statistics.clientsAllWaitingTimes.getSum();
	}

	@Override
	protected double calcSingleClient(final String name) {
		StatisticsPerformanceIndicator indicator=getSimData().statistics.clientsWaitingTimes.get(name);
		if (indicator==null) return 0.0;
		return ((StatisticsDataPerformanceIndicator)indicator).getSum();
	}
}

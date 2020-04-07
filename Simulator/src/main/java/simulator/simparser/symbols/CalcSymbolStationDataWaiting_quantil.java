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
import simulator.simparser.coresymbols.CalcSymbolStationDataQuantil;
import statistics.StatisticsDataPerformanceIndicator;
import statistics.StatisticsPerformanceIndicator;

/**
 * (a) Liefert das Quantil zur Wahrscheinlichkeit p (1. Parameter) der Wartezeiten der Kunden, deren Name an Quelle bzw. Namenszuweisung id (2. Parameter) auftritt (in Sekunden).<br>
 * (b) Liefert das Quantil zur Wahrscheinlichkeit p (1. Parameter) der Wartezeiten der Kunden an Station id (2. Parameter) (in Sekunden).
 * @author Alexander Herzog
 */
public class CalcSymbolStationDataWaiting_quantil extends CalcSymbolStationDataQuantil {
	@Override
	public String[] getNames() {
		return new String[]{"Wartezeit_quantil","WaitingTime_quantil"};
	}

	@Override
	protected double calc(final double p, final RunElementData data) {
		if (data.statisticWaiting==null) return 0;
		return data.statisticWaiting.getQuantil(p);
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
	protected double calcAll(final double p) {
		return getSimData().statistics.clientsAllWaitingTimes.getQuantil(p);
	}

	@Override
	protected double calcSingleClient(final double p, final String name) {
		StatisticsPerformanceIndicator indicator=getSimData().statistics.clientsWaitingTimes.get(name);
		if (indicator==null) return 0.0;
		return ((StatisticsDataPerformanceIndicator)indicator).getQuantil(p);
	}
}

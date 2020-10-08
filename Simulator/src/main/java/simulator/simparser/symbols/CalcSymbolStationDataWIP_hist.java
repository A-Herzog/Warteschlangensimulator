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

import mathtools.distribution.DataDistributionImpl;
import simulator.coreelements.RunElementData;
import simulator.simparser.coresymbols.CalcSymbolStationDataHistogram;
import statistics.StatisticsPerformanceIndicator;
import statistics.StatisticsTimePerformanceIndicator;

/**
 * Im Falle von drei Parametern:<br>
 * (a) Liefert den Anteil der Zeit, in der sich das System in Bezug auf die Anzahl an Kunden, deren Name an Quelle bzw. Namenszuweisung id (1. Parameter) auftritt, in einem Zustand größer als stateA (2. Parameter) und kleiner oder gleich stateB (3. Parameter) befunden hat.<br>
 * (b) Liefert den Anteil der Zeit, in der sich das System in Bezug auf die Anzahl an Kunden an Station id (1. Parameter) in einem Zustand größer als stateA (2. Parameter) und kleiner oder gleich stateB (3. Parameter) befunden hat.<br>
 * Im Falle von zwei Parametern:<br>
 * (a) Liefert den Anteil der Zeit, in der sich das System in Bezug auf die Anzahl an Kunden, deren Name an Quelle bzw. Namenszuweisung id (1. Parameter) auftritt, im Zustand state (2. Parameter) befunden hat.<br>
 * (b) Liefert den Anteil der Zeit, in der sich das System in Bezug auf die Anzahl an Kunden an Station id (1. Parameter) im Zustand state (2. Parameter) befunden hat.
 * @author Alexander Herzog
 */
public class CalcSymbolStationDataWIP_hist extends CalcSymbolStationDataHistogram {
	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"WIP_hist","Station_hist","N_hist"};

	@Override
	public String[] getNames() {
		return names;
	}

	@Override
	protected boolean hasSingleClientData() {
		return true;
	}

	@Override
	protected DataDistributionImpl getDistributionForClientType(final String name) {
		StatisticsPerformanceIndicator indicator=getSimData().statistics.clientsInSystemByClient.get(name);
		if (indicator==null) return null;
		return ((StatisticsTimePerformanceIndicator)indicator).getReadOnlyDistribution();
	}

	@Override
	protected double getDistributionSumForClientType(final String name) {
		StatisticsPerformanceIndicator indicator=getSimData().statistics.clientsInSystemByClient.get(name);
		if (indicator==null) return 0.0;
		return ((StatisticsTimePerformanceIndicator)indicator).getSum();
	}

	@Override
	protected DataDistributionImpl getDistribution(RunElementData data) {
		if (data.statisticClientsAtStation==null) return null;
		return data.statisticClientsAtStation.getReadOnlyDistribution();
	}

	@Override
	protected double getDistributionSum(RunElementData data) {
		if (data.statisticClientsAtStation==null) return 0;
		return data.statisticClientsAtStation.getSum();
	}
}

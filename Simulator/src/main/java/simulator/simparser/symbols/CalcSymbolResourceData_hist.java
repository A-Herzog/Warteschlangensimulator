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

import org.apache.commons.math3.util.FastMath;

import mathtools.distribution.DataDistributionImpl;
import simulator.coreelements.RunElementData;
import simulator.simparser.coresymbols.CalcSymbolStationDataHistogram;
import statistics.StatisticsTimePerformanceIndicator;

/**
 * Im Falle von zwei Parameter:<br>
 * Liefert den Anteil der Zeit, in der state (2. Parameter) Bediener der angegebenen Ressource (1. Parameter) ausgelastet waren.<br>
 * Im Falle von drei Parameter:<br>
 * Liefert den Anteil der Zeit, mehr als stateA (2. Parameter) und höchstens stateB (3. Parameter) Bediener der angegebenen Ressource (1. Parameter) ausgelastet waren.
 * @author Alexander Herzog
 */
public class CalcSymbolResourceData_hist extends CalcSymbolStationDataHistogram {
	@Override
	public String[] getNames() {
		return new String[]{"resource_hist","utilization_hist"};
	}

	@Override
	protected DataDistributionImpl getDistribution(final RunElementData data) {
		return null;
	}

	@Override
	protected double getDistributionSum(RunElementData data) {
		return 0;
	}

	@Override
	protected DataDistributionImpl getDistributionByID(final double id) {
		final StatisticsTimePerformanceIndicator[] statistics=getUsageStatistics();
		if (statistics==null) return null;

		final int idInt=(int)FastMath.round(id)-1;
		if (idInt<0 || idInt>=statistics.length) return null;

		return statistics[idInt].getReadOnlyDistribution();
	}

	@Override
	protected double getDistributionSumByID(final double id) {
		final StatisticsTimePerformanceIndicator[] statistics=getUsageStatistics();
		if (statistics==null) return 0;

		final int idInt=(int)FastMath.round(id)-1;
		if (idInt<0 || idInt>=statistics.length) return 0;

		return statistics[idInt].getSum();
	}
}

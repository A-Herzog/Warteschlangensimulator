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

import simulator.simparser.coresymbols.CalcSymbolResourceData;
import statistics.StatisticsTimePerformanceIndicator;

/**
 * Liefert die Standardabweichung der Anzahl an belegten Bedienern in der angegebenen Ressource (1. Parameter).
 * @author Alexander Herzog
 */
public class CalcSymbolResourceData_std extends CalcSymbolResourceData {
	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{
			"resource_std","resource_sd","utilization_std","utilization_sd",
			"resource_Standardabweichung","utilization_Standardabweichung"
	};

	@Override
	public String[] getNames() {
		return names;
	}

	@Override
	protected double calcSingleResource(final StatisticsTimePerformanceIndicator statistics) {
		return statistics.getTimeSD();
	}
}

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
 * Im Falle von einem Parameter:<br>
 * Liefert die maximale Anzahl an belegten Bedienern in der angegebenen Ressource (1. Parameter).<br>
 * Im Falle von keinem Parameter:<br>
 * Liefert die maximale Anzahl an belegten Bedienern in allen Ressourcen zusammen.
 * @author Alexander Herzog
 */
public class CalcSymbolResourceData_max extends CalcSymbolResourceData {
	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{
			"resource_max","utilization_max",
			"resource_Maximum","utilization_Maximum"
	};

	@Override
	public String[] getNames() {
		return names;
	}

	@Override
	protected boolean hasAllResourceData() {return true;}

	@Override
	protected double calcAllResources(final StatisticsTimePerformanceIndicator[] statistics) {
		double sum=0;
		for (StatisticsTimePerformanceIndicator indicator: statistics) sum+=indicator.getTimeMax();
		return sum;
	}

	@Override
	protected double calcSingleResource(final StatisticsTimePerformanceIndicator statistics) {
		return statistics.getTimeMax();
	}
}

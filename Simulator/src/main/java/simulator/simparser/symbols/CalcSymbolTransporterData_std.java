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

import simulator.simparser.coresymbols.CalcSymbolTransporterData;
import statistics.StatisticsTimePerformanceIndicator;

/**
 * Liefert die Standardabweichung der Anzahl an belegten Transportern in der angegebenen Transportergruppe.
 * @author Alexander Herzog
 */
public class CalcSymbolTransporterData_std extends CalcSymbolTransporterData {
	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{
			"transporter_std","transporter_sd",
			"transporter_utilization_std","transporter_utilization_sd",
			"transporter_Standardabweichung","transporter_utilization_Standardabweichung"
	};

	@Override
	public String[] getNames() {
		return names;
	}

	@Override
	protected double calcSingleTransporterGroup(final StatisticsTimePerformanceIndicator statistics) {
		return statistics.getTimeSD();
	}
}

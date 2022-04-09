/**
 * Copyright 2022 Alexander Herzog
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
import statistics.StatisticsPerformanceIndicator;
import statistics.StatisticsTimePerformanceIndicator;

/**
 * Im Falle von einem Parameter:<br>
 * (a) Liefert den quadrierten Variationskoeffizienten der Anzahl an Kunden in Bedienung, deren Name an Quelle bzw. Namenszuweisung id (1. Parameter) auftritt.<br>
 * (b) Liefert den quadrierten Variationskoeffizienten der Anzahl an Kunden in Bedienung an Station id (1. Parameter).<br>
 * @author Alexander Herzog
 */
public class CalcSymbolStationDataProcessCount_scv extends CalcSymbolStationData {
	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"NS_scv","Process_scv"};

	@Override
	public String[] getNames() {
		return names;
	}

	@Override
	protected boolean hasSingleClientData() {
		return true;
	}

	@Override
	protected double calcSingleClient(final String name) {
		StatisticsPerformanceIndicator indicator=getSimData().statistics.clientsAtStationProcessByClient.get(name);
		if (indicator==null) return 0.0;
		final double cv=((StatisticsTimePerformanceIndicator)indicator).getTimeCV();
		return cv*cv;
	}

	@Override
	protected double calc(final RunElementData data) {
		if (data.statisticClientsAtStationProcess==null) return 0;
		final double cv=data.statisticClientsAtStationProcess.getTimeCV();
		return cv*cv;
	}
}
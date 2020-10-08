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
 * Im Falle von zwei Parametern:<br>
 * (a) Liefert das Quantil zur Wahrscheinlichkeit p (1. Parameter) der Transferzeiten der Kunden, deren Name an Quelle bzw. Namenszuweisung id (2. Parameter) auftritt (in Sekunden).<br>
 * (b) Liefert das Quantil zur Wahrscheinlichkeit p (1. Parameter) der Transferzeiten der Kunden an Station id (2. Parameter) (in Sekunden).<br>
 * Im Falle von einem Parameter:<br>
 * Liefert das Quantil zur Wahrscheinlichkeit p (1. Parameter) der Transferzeiten über alle Kunden (in Sekunden).
 * @author Alexander Herzog
 */
public class CalcSymbolStationDataTransfer_quantil extends CalcSymbolStationDataQuantil {
	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[] {"Transferzeit_quantil","TransferTime_quantil"};

	@Override
	public String[] getNames() {
		return names;
	}

	@Override
	protected double calc(final double p, final RunElementData data) {
		if (data.statisticTransfer==null) return 0;
		return data.statisticTransfer.getQuantil(p);
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
		return getSimData().statistics.clientsAllTransferTimes.getQuantil(p);
	}

	@Override
	protected double calcSingleClient(final double p, final String name) {
		StatisticsPerformanceIndicator indicator=getSimData().statistics.clientsTransferTimes.get(name);
		if (indicator==null) return 0.0;
		return ((StatisticsDataPerformanceIndicator)indicator).getQuantil(p);
	}
}

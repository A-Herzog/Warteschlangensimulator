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

/**
 * Im Falle von drei Parametern:<br>
 * Liefert den Anteil der Kunden, f�r den die Transferzeit an Station id (1. Parameter) mehr als timeA (2. Parameter) und h�chstens timeB (3. Parameter) Sekunden gedauert hat.<br>
 * Im Falle von zwei Parametern:<br>
 * Liefert den Anteil der Kunden, f�r den die Transferzeit an Station id (1. Parameter) time (2. Parameter) Sekunden gedauert hat.
 * @author Alexander Herzog
 */
public class CalcSymbolStationDataTransfer_hist extends CalcSymbolStationDataHistogram {
	@Override
	public String[] getNames() {
		return new String[]{"Transferzeit_hist","TransferTime_hist"};
	}

	@Override
	protected DataDistributionImpl getDistribution(RunElementData data) {
		if (data.statisticTransfer==null) return null;
		return data.statisticTransfer.getDistribution();
	}

	@Override
	protected double getDistributionSum(RunElementData data) {
		if (data.statisticTransfer==null) return 0;
		return data.statisticTransfer.getSum();
	}
}

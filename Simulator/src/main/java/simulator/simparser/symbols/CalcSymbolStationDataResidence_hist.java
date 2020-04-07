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
 * Liefert den Anteil der Kunden, für den die Verweilzeit an Station id (1. Parameter) mehr als timeA (2. Parameter) und höchstens timeB (3. Parameter) Sekunden gedauert hat.<br>
 * Im Falle von zwei Parametern:<br>
 * Liefert den Anteil der Kunden, für den die Verweilzeit an Station id (1. Parameter) time (2. Parameter) Sekunden gedauert hat.
 * @author Alexander Herzog
 */
public class CalcSymbolStationDataResidence_hist extends CalcSymbolStationDataHistogram {
	@Override
	public String[] getNames() {
		return new String[]{"Verweilzeit_hist","ResidenceTime_hist"};
	}

	@Override
	protected DataDistributionImpl getDistribution(RunElementData data) {
		if (data.statisticResidence==null) return null;
		return data.statisticResidence.getDistribution();
	}

	@Override
	protected double getDistributionSum(RunElementData data) {
		if (data.statisticResidence==null) return 0;
		return data.statisticResidence.getSum();
	}
}

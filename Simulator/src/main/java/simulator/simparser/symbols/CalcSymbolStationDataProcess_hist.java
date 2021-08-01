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
 * Im Falle von zwei Parameter:<br>
 * Liefert den Anteil der Kunden, für den die Bedienzeit an Station id (1. Parameter) time (2. Parameter) Sekunden gedauert hat.<br>
 * Im Falle von drei Parameter:<br>
 * Liefert den Anteil der Kunden, für den die Bedienzeit an Station id (1. Parameter) mehr als timeA (2. Parameter) und höchstens timeB (3. Parameter) Sekunden gedauert hat.
 * @author Alexander Herzog
 */
public class CalcSymbolStationDataProcess_hist extends CalcSymbolStationDataHistogram {
	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"Bedienzeit_hist","ProcessTime_hist","ProcessingTime_hist","ServiceTime_hist"};

	@Override
	public String[] getNames() {
		return names;
	}

	@Override
	protected DataDistributionImpl getDistribution(RunElementData data) {
		if (data.statisticProcess==null) return null;
		return data.statisticProcess.getDistribution();
	}

	@Override
	protected double getDistributionSum(RunElementData data) {
		if (data.statisticProcess==null) return 0;
		return data.statisticProcess.getSum();
	}
}

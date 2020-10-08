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
import simulator.coreelements.RunElementDataWithValue;
import simulator.simparser.coresymbols.CalcSymbolStationData;

/**
 * Liefert den Wert des Zählers in Station id.<br>
 * (Kann nur auf "Differenzzähler"-, "Zähler"- und "Durchsatz"-Elemente angewandt werden.)
 * @author Alexander Herzog
 */
public class CalcSymbolSimDataCounter extends CalcSymbolStationData {
	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"Zähler","Counter","Value","Wert"};

	@Override
	public String[] getNames() {
		return names;
	}

	@Override
	protected double calc(final RunElementData data) {
		if (!(data instanceof RunElementDataWithValue)) return 0;
		return ((RunElementDataWithValue)data).getValue(true);
	}
}
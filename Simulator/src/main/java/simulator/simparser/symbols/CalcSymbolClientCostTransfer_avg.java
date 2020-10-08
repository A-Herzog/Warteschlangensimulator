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

import simulator.simparser.coresymbols.CalcSymbolClientCosts;

/**
 * Im Falle von einem Parameter:<br>
 * Liefert die durchschnittlichen Transferzeitkosten der Kunden, deren Name an Quelle bzw. Namenszuweisung id (1. Parameter) auftritt.<br>
 * Im Falle von keinem Parameter:<br>
 * Liefert die durchschnittlichen Transferzeitkosten über alle Kunden.
 * @author Alexander Herzog
 */
public class CalcSymbolClientCostTransfer_avg extends CalcSymbolClientCosts {
	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"Costs_Transfer_avg","Costs_Transfer_average","Kosten_Transferzeit_Mittelwert"};

	@Override
	public String[] getNames() {
		return names;
	}

	@Override
	protected double calcAllClients() {
		return calcAverage(getSimData().statistics.clientsCostsTransfer);
	}

	@Override
	protected double calcSingleClient(final String name) {
		final long count=getCount(name);
		if (count==0) return 0.0;
		return getValue(getSimData().statistics.clientsCostsTransfer.get(name))/count;
	}
}

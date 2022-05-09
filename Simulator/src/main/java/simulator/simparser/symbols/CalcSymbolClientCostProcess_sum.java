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
 * Liefert die Summe der Bedienzeitkosten der Kunden, deren Name an Quelle bzw. Namenszuweisung id (1. Parameter) auftritt.<br>
 * Im Falle von keinem Parameter:<br>
 * Liefert die Summe der Bedienzeitkosten über alle Kunden.
 * @author Alexander Herzog
 */
public class CalcSymbolClientCostProcess_sum extends CalcSymbolClientCosts {
	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"Costs_Process_Sum","Kosten_Bedienzeit_Summe"};

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolClientCostProcess_sum() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	@Override
	public String[] getNames() {
		return names;
	}

	@Override
	protected double calcSingleClient(final String name) {
		return getValue(getSimData().statistics.clientsCostsProcess.get(name));
	}
}

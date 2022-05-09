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
 * Liefert die durchschnittlichen Bedienzeitkosten der Kunden, deren Name an Quelle bzw. Namenszuweisung id auftritt.<br>
 * Im Falle von keinem Parameter:<br>
 * Liefert die durchschnittlichen Bedienzeitkosten �ber alle Kunden.
 * @author Alexander Herzog
 */
public class CalcSymbolClientCostProcess_avg extends CalcSymbolClientCosts {
	/**
	 * Namen f�r das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"Costs_Process_avg","Costs_Process_average","Kosten_Bedienzeit_Mittelwert"};

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolClientCostProcess_avg() {
		/*
		 * Wird nur ben�tigt, um einen JavaDoc-Kommentar f�r diesen (impliziten) Konstruktor
		 * setzen zu k�nnen, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	@Override
	public String[] getNames() {
		return names;
	}

	@Override
	protected double calcAllClients() {
		return calcAverage(getSimData().statistics.clientsCostsProcess);
	}

	@Override
	protected double calcSingleClient(final String name) {
		final long count=getCount(name);
		if (count==0) return 0.0;
		return getValue(getSimData().statistics.clientsCostsProcess.get(name))/count;
	}
}

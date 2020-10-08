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

import parser.MathCalcError;
import simulator.runmodel.RunDataClient;
import simulator.simparser.coresymbols.CalcSymbolSimData;

/**
 * Liefert die 1-basierende fortlaufende Nummer des aktuellen Kunden. Bei der Verwendung von mehreren Simulationsthreads ist diese Zahl thread-lokal.
 * @author Alexander Herzog
 */
public class CalcSymbolClientNumber extends CalcSymbolSimData {
	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"KundeNummer","ClientNumber"};

	@Override
	public String[] getNames() {
		return names;
	}

	@Override
	protected double calc(double[] parameters) throws MathCalcError {
		if (parameters.length!=0) throw error();
		final RunDataClient client=getCurrentClient();
		if (client==null) throw error();
		return client.clientNumber;
	}

	@Override
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		if (parameters.length!=0) return fallbackValue;
		final RunDataClient client=getCurrentClient();
		if (client==null) return 0;
		return client.clientNumber;
	}
}
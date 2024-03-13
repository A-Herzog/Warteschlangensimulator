/**
 * Copyright 2024 Alexander Herzog
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

import simulator.simparser.coresymbols.CalcSymbolResourceDataWithParameter;
import statistics.StatisticsTimePerformanceIndicator;

/**
 * Liefert das Quantil zur Wahrscheinlichkeit p (2. Parameter) der Anzahl an belegten Bedienern in der angegebenen Ressource (1. Parameter)
 * @author Alexander Herzog
 */
public class CalcSymbolResourceData_quantil extends CalcSymbolResourceDataWithParameter {
	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{
			"resource_quantil","utilization_quantil"
	};

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolResourceData_quantil() {
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
	protected boolean hasAllResourceData() {return false;}

	@Override
	protected double calcSingleResource(final StatisticsTimePerformanceIndicator statistics, final double p) {
		return statistics.getQuantil(Math.max(0,Math.min(1,p)));
	}
}

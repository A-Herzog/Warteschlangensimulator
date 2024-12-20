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

import simulator.simparser.coresymbols.CalcSymbolResourceData;
import statistics.StatisticsTimePerformanceIndicator;

/**
 * Im Falle von einem Parameter:<br>
 * Liefert die minimale Anzahl an belegten Bedienern in der angegebenen Ressource (1. Parameter).<br>
 * Im Falle von keinem Parameter:<br>
 * Liefert die minimale Anzahl an belegten Bedienern in allen Ressourcen zusammen.
 * @author Alexander Herzog
 */
public class CalcSymbolResourceData_min extends CalcSymbolResourceData {
	/**
	 * Namen f�r das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{
			"resource_min","utilization_min",
			"resource_Minimum","utilization_Minimum"
	};

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolResourceData_min() {
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
	protected boolean hasAllResourceData() {return true;}

	@Override
	protected double calcAllResources(final StatisticsTimePerformanceIndicator[] statistics) {
		double sum=0;
		for (StatisticsTimePerformanceIndicator indicator: statistics) sum+=indicator.getTimeMin();
		return sum;
	}

	@Override
	protected double calcSingleResource(final StatisticsTimePerformanceIndicator statistics) {
		return statistics.getTimeMin();
	}
}

/**
 * Copyright 2021 Alexander Herzog
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

import simulator.simparser.coresymbols.CalcSymbolUserStatistics;
import statistics.StatisticsDataPerformanceIndicatorWithNegativeValues;
import statistics.StatisticsPerformanceIndicator;
import statistics.StatisticsTimeContinuousPerformanceIndicator;

/**
 * Liefert die Schiefe des Statistikeintrags <code>nr</code> (2. Parameter) (1-basierend) an Statistik-Station id (1. Parameter).
 * @author Alexander Herzog
 */
public class CalcSymbolUserStatistics_sk extends CalcSymbolUserStatistics {
	/**
	 * Namen f�r das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[] {"Statistik_sk","Statistics_sk"};

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolUserStatistics_sk() {
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
	protected double processIndicator(StatisticsPerformanceIndicator indicator) {
		if (indicator instanceof StatisticsDataPerformanceIndicatorWithNegativeValues) return ((StatisticsDataPerformanceIndicatorWithNegativeValues)indicator).getSk();
		if (indicator instanceof StatisticsTimeContinuousPerformanceIndicator) return ((StatisticsTimeContinuousPerformanceIndicator)indicator).getTimeSk();
		return 0.0;
	}
}

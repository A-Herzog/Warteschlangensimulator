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

import org.apache.commons.math3.util.FastMath;

import parser.MathCalcError;
import simulator.coreelements.RunElementData;
import simulator.elements.RunElementUserStatisticData;
import simulator.simparser.coresymbols.CalcSymbolSimData;
import statistics.StatisticsDataPerformanceIndicator;

/**
 * Liefert das Quantil zur Wahrscheinlichkeit p (3. Parameter) des Statistikeintrags <code>nr</code> (2. Parameter) (1-basierend) an Statistik-Station id (1. Parameter).
 * @author Alexander Herzog
 */
public class CalcSymbolUserStatistics_quantil extends CalcSymbolSimData {
	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[] {"Statistik_quantil","Statistics_quantil"};

	@Override
	public String[] getNames() {
		return names;
	}

	@Override
	protected double calc(double[] parameters) throws MathCalcError {
		if (parameters.length!=3) throw error();

		/* Station und Indikator innerhalb der Station wählen */
		final RunElementData data=getRunElementDataForID(parameters[0]);
		if (data==null) throw error();
		if (!(data instanceof RunElementUserStatisticData)) throw error();
		final StatisticsDataPerformanceIndicator[] indicators=((RunElementUserStatisticData)data).getIndicators();
		final int index=(int)FastMath.round(parameters[1]);
		if (index<=0 || indicators.length<index) throw error();
		if (indicators[index-1]==null) return 0.0;
		final StatisticsDataPerformanceIndicator indicator=indicators[index-1];

		/* Wert p für Quantil */
		double p=parameters[2];
		if (p<0) p=0;
		if (p>1) p=1;

		/* Quantil berechnen */
		return indicator.getQuantil(p);
	}

	@Override
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		if (parameters.length!=3) return fallbackValue;

		/* Station und Indikator innerhalb der Station wählen */
		final RunElementData data=getRunElementDataForID(parameters[0]);
		if (data==null) return fallbackValue;
		if (!(data instanceof RunElementUserStatisticData)) return fallbackValue;
		final StatisticsDataPerformanceIndicator[] indicators=((RunElementUserStatisticData)data).getIndicators();
		final int index=(int)FastMath.round(parameters[1]);
		if (index<=0 || indicators.length<index) return fallbackValue;
		if (indicators[index-1]==null) return 0;
		final StatisticsDataPerformanceIndicator indicator=indicators[index-1];

		/* Wert p für Quantil */
		double p=parameters[2];
		if (p<0) p=0;
		if (p>1) p=1;

		/* Quantil berechnen */
		return indicator.getQuantil(p);
	}
}

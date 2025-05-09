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

import mathtools.distribution.DataDistributionImpl;
import simulator.simparser.coresymbols.CalcSymbolUserStatistics;
import statistics.StatisticsDataPerformanceIndicatorWithNegativeValues;
import statistics.StatisticsPerformanceIndicator;

/**
 * Im Falle von drei Parametern:<br>
 * Liefert den Anteil der Zeit, in der sich das System in Bezug auf Statistikeintrags <code>nr</code> (2. Parameter) (1-basierend) an Statistik-Station id (1. Parameter) in einem Zustand gr��er als stateA (3. Parameter) und kleiner oder gleich stateB (4. Parameter) befunden hat.<br>
 * Im Falle von zwei Parametern:<br>
 * Liefert den Anteil der Zeit, in der sich das System in Bezug auf Statistikeintrags <code>nr</code> (2. Parameter) (1-basierend) an Statistik-Station id (1. Parameter) in Zustand state (3. Parameter) befunden hat.
 * @author Alexander Herzog
 */
public class CalcSymbolUserStatistics_hist extends CalcSymbolUserStatistics {
	/**
	 * Namen f�r das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[] {"Statistik_hist","Statistics_hist"};

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolUserStatistics_hist() {
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
	protected boolean isHistogram() {
		return true;
	}

	@Override
	protected double processHistogram(final StatisticsPerformanceIndicator indicator, int value) {
		if (!(indicator instanceof StatisticsDataPerformanceIndicatorWithNegativeValues)) return 0.0;
		final StatisticsDataPerformanceIndicatorWithNegativeValues dataIndicator=(StatisticsDataPerformanceIndicatorWithNegativeValues)indicator;
		final DataDistributionImpl dist=dataIndicator.getDistribution();
		if (dist==null) return 0.0;
		final double scale=dist.densityData.length/dist.upperBound;
		if (scale!=1.0) {
			value=(int)Math.round(value*scale);
		}
		if (value<0 || value>=dist.densityData.length) return 0.0;
		final long sum=dataIndicator.getCount();
		if (sum==0) return 0.0;
		return dist.densityData[value]/sum;
	}

	@Override
	protected double processHistogram(final StatisticsPerformanceIndicator indicator, int value1, int value2) {
		if (!(indicator instanceof StatisticsDataPerformanceIndicatorWithNegativeValues)) return 0.0;
		final StatisticsDataPerformanceIndicatorWithNegativeValues dataIndicator=(StatisticsDataPerformanceIndicatorWithNegativeValues)indicator;
		final DataDistributionImpl dist=dataIndicator.getDistribution();
		if (dist==null) return 0.0;

		final int len=dist.densityData.length;
		final double scale=len/dist.upperBound;
		if (scale!=1.0) {
			value1=(int)Math.round(value1*scale);
			value2=(int)Math.round(value2*scale);
		}

		value1=FastMath.max(-1,value1);
		value2=FastMath.max(0,value2);

		if (value1>=len) return 0.0;
		if (value2>=len) return 0.0;
		if (value2<=value1) return 0.0;

		final long sum=dataIndicator.getCount();
		if (sum==0) return 0.0;

		double[] data=dist.densityData;
		double part=0;
		for (int i=value1+1;i<=value2;i++) part+=data[i];
		return part/sum;
	}

	@Override
	protected double processIndicator(StatisticsPerformanceIndicator indicator) {
		return 0.0;
	}
}

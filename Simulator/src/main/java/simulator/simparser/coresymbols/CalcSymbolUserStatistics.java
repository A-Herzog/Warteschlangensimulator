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
package simulator.simparser.coresymbols;

import org.apache.commons.math3.util.FastMath;

import simulator.coreelements.RunElementData;
import simulator.elements.RunElementUserStatisticData;
import simulator.runmodel.SimulationData;
import statistics.StatisticsDataPerformanceIndicator;

/**
 * Basisklasse für Funktionen, die Kenngrößen von Statistik-Station ausgeben.
 * @author Alexander Herzog
 * @see SimulationData
 * @see CalcSymbolSimData
 */
public abstract class CalcSymbolUserStatistics extends CalcSymbolSimData {
	/**
	 * Führt die eigentlich Ermittlung der konkreten Kenngröße auf Basis des Statistikobjektes der Statistik-Station durch.
	 * @param indicator	Statistikobjekt der Statistik-Station von dem eine Kenngröße ermittelt werden soll
	 * @return	Kenngröße
	 */
	protected abstract double processIndicator(final StatisticsDataPerformanceIndicator indicator);

	/**
	 * Handelt es sich bei der von dieser Funktion auszugebenen Kenngröße um einen Histogramm-Wert?
	 * @return	Wird hier <code>true</code> geliefert, so müssen {@link #processHistogram(StatisticsDataPerformanceIndicator, int)} und {@link #processHistogram(StatisticsDataPerformanceIndicator, int, int)} implementiert werden
	 */
	protected boolean isHistogram() {
		return false;
	}

	/**
	 * Berechnet den Histogramm-Wert für einen konkreten Wert
	 * @param indicator	Statistikobjekt der Statistik-Station von dem eine Kenngröße ermittelt werden soll
	 * @param value	Stelle an der der Histogramm-Wert ermittelt werden soll
	 * @return	Histogramm-Wert
	 */
	protected double processHistogram(final StatisticsDataPerformanceIndicator indicator, final int value) {
		return 0.0;
	}

	/**
	 * Berechnet den Histogramm-Wert für einen Bereich
	 * @param indicator	Statistikobjekt der Statistik-Station von dem eine Kenngröße ermittelt werden soll
	 * @param value1	Untere Grenze des Bereichs
	 * @param value2	Obere Grenze des Bereichs
	 * @return	Histogramm-Wert
	 */
	protected double processHistogram(final StatisticsDataPerformanceIndicator indicator, final int value1, final int value2) {
		return 0.0;
	}

	@Override
	protected Double calc(double[] parameters) {
		if (isHistogram()) {
			if (parameters.length<3 || parameters.length>4) return null;
		} else {
			if (parameters.length!=2) return null;
		}

		final RunElementData data=getRunElementDataForID(parameters[0]);
		if (data==null) return null;
		if (!(data instanceof RunElementUserStatisticData)) return null;
		final StatisticsDataPerformanceIndicator[] indicators=((RunElementUserStatisticData)data).getIndicators();
		final int index=(int)FastMath.round(parameters[1]);
		if (index<=0 || indicators.length<index) return null;
		if (indicators[index-1]==null) return fastBoxedValue(0);
		if (isHistogram()) {
			if (parameters.length==3) {
				return fastBoxedValue(processHistogram(indicators[index-1],(int)FastMath.round(parameters[2])));
			} else {
				return fastBoxedValue(processHistogram(indicators[index-1],(int)FastMath.round(parameters[2]),(int)FastMath.round(parameters[3])));
			}
		} else {
			return fastBoxedValue(processIndicator(indicators[index-1]));
		}
	}

	@Override
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		if (isHistogram()) {
			if (parameters.length<3 || parameters.length>4) return fallbackValue;
		} else {
			if (parameters.length!=2) return fallbackValue;
		}

		final RunElementData data=getRunElementDataForID(parameters[0]);
		if (data==null) return fallbackValue;
		if (!(data instanceof RunElementUserStatisticData)) return fallbackValue;
		final StatisticsDataPerformanceIndicator[] indicators=((RunElementUserStatisticData)data).getIndicators();
		final int index=(int)FastMath.round(parameters[1]);
		if (index<=0 || indicators.length<index) return fallbackValue;
		if (indicators[index-1]==null) return 0;
		if (isHistogram()) {
			if (parameters.length==3) {
				return processHistogram(indicators[index-1],(int)FastMath.round(parameters[2]));
			} else {
				return processHistogram(indicators[index-1],(int)FastMath.round(parameters[2]),(int)FastMath.round(parameters[3]));
			}
		} else {
			return processIndicator(indicators[index-1]);
		}
	}
}

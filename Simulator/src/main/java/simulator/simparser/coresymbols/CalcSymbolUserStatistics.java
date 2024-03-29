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

import parser.MathCalcError;
import simulator.coreelements.RunElementData;
import simulator.elements.RunElementUserStatisticData;
import simulator.runmodel.SimulationData;
import statistics.StatisticsPerformanceIndicator;

/**
 * Basisklasse f�r Funktionen, die Kenngr��en von Statistik-Station ausgeben.
 * @author Alexander Herzog
 * @see SimulationData
 * @see CalcSymbolSimData
 */
public abstract class CalcSymbolUserStatistics extends CalcSymbolSimData {
	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolUserStatistics() {
		/*
		 * Wird nur ben�tigt, um einen JavaDoc-Kommentar f�r diesen (impliziten) Konstruktor
		 * setzen zu k�nnen, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	/**
	 * F�hrt die eigentlich Ermittlung der konkreten Kenngr��e auf Basis des Statistikobjektes der Statistik-Station durch.
	 * @param indicator	Statistikobjekt der Statistik-Station von dem eine Kenngr��e ermittelt werden soll
	 * @return	Kenngr��e
	 */
	protected abstract double processIndicator(final StatisticsPerformanceIndicator indicator);

	/**
	 * Handelt es sich bei der von dieser Funktion auszugebenen Kenngr��e um einen Histogramm-Wert?
	 * @return	Wird hier <code>true</code> geliefert, so m�ssen {@link #processHistogram(StatisticsPerformanceIndicator, int)} und {@link #processHistogram(StatisticsPerformanceIndicator, int, int)} implementiert werden
	 */
	protected boolean isHistogram() {
		return false;
	}

	/**
	 * Berechnet den Histogramm-Wert f�r einen konkreten Wert
	 * @param indicator	Statistikobjekt der Statistik-Station von dem eine Kenngr��e ermittelt werden soll
	 * @param value	Stelle an der der Histogramm-Wert ermittelt werden soll
	 * @return	Histogramm-Wert
	 */
	protected double processHistogram(final StatisticsPerformanceIndicator indicator, final int value) {
		return 0.0;
	}

	/**
	 * Berechnet den Histogramm-Wert f�r einen Bereich
	 * @param indicator	Statistikobjekt der Statistik-Station von dem eine Kenngr��e ermittelt werden soll
	 * @param value1	Untere Grenze des Bereichs
	 * @param value2	Obere Grenze des Bereichs
	 * @return	Histogramm-Wert
	 */
	protected double processHistogram(final StatisticsPerformanceIndicator indicator, final int value1, final int value2) {
		return 0.0;
	}

	@Override
	protected double calc(double[] parameters) throws MathCalcError {
		if (isHistogram()) {
			if (parameters.length<3 || parameters.length>4) throw error();
		} else {
			if (parameters.length!=2) throw error();
		}

		final RunElementData data=getRunElementDataForID(parameters[0]);
		if (data==null) throw error();
		if (!(data instanceof RunElementUserStatisticData)) throw error();
		final StatisticsPerformanceIndicator[] indicators=((RunElementUserStatisticData)data).getAllIndicators();
		final int index=(int)FastMath.round(parameters[1]);
		if (index<=0 || indicators.length<index) throw error();
		if (indicators[index-1]==null) return 0.0;
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
		final StatisticsPerformanceIndicator[] indicators=((RunElementUserStatisticData)data).getAllIndicators();
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

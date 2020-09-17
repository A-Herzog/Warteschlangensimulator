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
package parser.symbols.distributions;

import java.util.Arrays;

import org.apache.commons.math3.util.FastMath;

import mathtools.distribution.tools.DistributionRandomNumber;
import parser.MathCalcError;
import parser.coresymbols.CalcSymbolPreOperator;

/**
 * Abstrakte Basisklasse, die Funktionen zum Zugriff auf
 * diskrete Wahrscheinlichkeitsverteilungen anbietet.
 * @author Alexander Herzog
 * @version 1.0
 */
public abstract class CalcSymbolDiscreteDistribution extends CalcSymbolPreOperator {
	/**
	 * Liefert die Anzahl an Parametern der Wahrscheinlichkeitsverteilung (z.B. 1 für Exp-Verteilung, 2 für Normalverteilung und 4 für Beta-Verteilung)
	 * @return	Anzahl an Parametern der Wahrscheinlichkeitsverteilung selbst
	 * @see CalcSymbolDistribution#getDistribution(double[])
	 */
	protected abstract int getParameterCount();

	/**
	 * Berechnet den Wert der Zähldichte an einer Stelle
	 * @param parameters	Allgemeine Parameter der Verteilung
	 * @param k	Stelle an der der Wert berechnet werden soll (es ist dabei sichergestellt, dass k&ge;0 ist)
	 * @return	Wahrscheinlichkeit oder ein Wert kleiner als 0, wenn die Parameter ungültig sind
	 */
	protected abstract double calcProbability(double[] parameters, int k);

	/** Maximalwert für K (danach werden Summenbildungen abgeschnitten) */
	private final static int MAX_K=1_000_000;

	/**
	 * Berechnet eine Pseudozufallszahl basieren auf der konkreten
	 * diskreten Wahrscheinlichkeitsverteilung.
	 * @param parameters	Parameter für die Verteilung
	 * @return	Pseudozufallszahl gemäß der Verteilung
	 * @see #calcProbability(double[], int)
	 */
	private int getRandomNumber(final double[] parameters) {
		final double d=DistributionRandomNumber.nextDouble();
		double sum=0;
		for (int k=0;k<MAX_K;k++) {
			final double p=calcProbability(parameters,k);
			if (p<0) return -1;
			sum+=p;
			if (sum>=d) return k;
		}
		return MAX_K;
	}

	@Override
	protected final double calc(double[] parameters) throws MathCalcError {
		final int distParameterCount=getParameterCount();

		/* Zufallszahl */
		if (parameters.length==distParameterCount) {
			final int r=getRandomNumber(parameters);
			if (r<0) throw error();
			return r;
		}

		/* Dichte */
		if (parameters.length==distParameterCount+1) {
			if (parameters[0]<0) return 0;
			final int k=(int)FastMath.round(parameters[0]);
			final double[] distParameters=Arrays.copyOfRange(parameters,1,parameters.length);
			final double p=calcProbability(distParameters,k);
			if (p<0) throw error();
			return p;
		}

		throw error();
	}

	@Override
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		int distParameterCount=getParameterCount();

		/* Zufallszahl */
		if (parameters.length==distParameterCount) {
			final int r=getRandomNumber(parameters);
			if (r<0) return fallbackValue;
			return r;
		}

		/* Dichte */
		if (parameters.length==distParameterCount+1) {
			if (parameters[0]<0) return 0;
			final int k=(int)FastMath.round(parameters[0]);
			final double[] distParameters=Arrays.copyOfRange(parameters,1,parameters.length);
			final double p=calcProbability(distParameters,k);
			if (p<0) return fallbackValue;
			return p;
		}
		return fallbackValue;
	}

	@Override
	protected boolean isDeterministic() {
		return false;
	}
}

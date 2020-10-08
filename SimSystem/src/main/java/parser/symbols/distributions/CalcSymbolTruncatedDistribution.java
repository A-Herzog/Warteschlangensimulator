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

import parser.MathCalcError;
import parser.coresymbols.CalcSymbolPreOperator;

/**
 * Abstrakte Basisklasse, die Funktionen zur Erzeugung von Zufallszahlen
 * gemäß bestimmter Verteilungen und mit einem künstlich limitierten Träger
 * bereitstellt.
 * @author Alexander Herzog
 */
public class CalcSymbolTruncatedDistribution extends CalcSymbolPreOperator {
	/** Maximale Anzahl an Versuchen, einen Wert im passenden Bereich zu erhalten */
	private static final int MAX_RND=100;

	/**
	 * Namen für die Verteilung (werden von {@link #getNames()}
	 * aus den Namen von {@link #innerDistribution} abgeleitet)
	 */
	private String[] names;

	/**
	 * Eingebettete Verteilung
	 */
	private final CalcSymbolDistribution innerDistribution;

	/**
	 * Anzahl der Parameter der Verteilung
	 * @see CalcSymbolDistribution#getParameterCount()
	 */
	private final int parameterCount;

	/**
	 * Array für die Verteilungsparameter
	 * (um nicht immer wieder neue Arrays anlegen zu müssen)
	 */
	private final double [] distParameters;

	/**
	 * Konstruktor der Klasse
	 * @param innerDistribution	Eingebettete Verteilung
	 */
	public CalcSymbolTruncatedDistribution(final CalcSymbolDistribution innerDistribution) {
		this.innerDistribution=innerDistribution;
		parameterCount=innerDistribution.getParameterCount();
		distParameters=new double[parameterCount];
	}

	@Override
	public String[] getNames() {
		if (names==null) {
			final String[] innerNames=innerDistribution.getNames();
			names=new String[innerNames.length*2];
			for (int i=0;i<innerNames.length;i++) {
				final String name=innerNames[i];
				names[2*i+0]=name+"Range";
				names[2*i+1]=name+"Bereich";
			}
		}
		return names;
	}

	/**
	 * Liefert eine Pseudozufallszahl gemäß der Verteilung,
	 * die in dem angegebene Bereich liegt. Liegt nach
	 * {@link #MAX_RND} Schritten immer noch kein Wert
	 * vor, so wird <code>(min+max)/2</code> geliefert.
	 * @param min	Minimalwert
	 * @param max	Maximalwert
	 * @return	Pseudozufallszahl
	 * @throws MathCalcError	Reicht Fehler von {@link #innerDistribution} nach außen durch
	 */
	private double getRandom(final double min, final double max) throws MathCalcError {
		int count=0;
		double rnd=innerDistribution.calc(distParameters);
		while (rnd<min || rnd>max) {
			if (count>MAX_RND) return (min+max)*0.5;
			rnd=innerDistribution.calc(distParameters);
			count++;
		}
		return rnd;
	}

	/**
	 * Liefert eine Pseudozufallszahl gemäß der Verteilung,
	 * die in dem angegebene Bereich liegt. Liegt nach
	 * {@link #MAX_RND} Schritten immer noch kein Wert
	 * vor, so wird der angegebene Fallback-Wert geliefert.
	 * @param min	Minimalwert
	 * @param max	Maximalwert
	 * @param fallbackValue	Wert, der geliefert wird, wenn innerhalb von {@link #MAX_RND} Versuchen kein gültiger Wert ermittelt werden konnte
	 * @return	Pseudozufallszahl
	 */
	private double getRandom(final double min, final double max, final double fallbackValue) {
		int count=0;
		double rnd=innerDistribution.calcOrDefault(distParameters,fallbackValue);
		while (rnd<min || rnd>max) {
			if (count>MAX_RND) return (min+max)*0.5;
			rnd=innerDistribution.calcOrDefault(distParameters,fallbackValue);
			count++;
		}
		return rnd;
	}

	@Override
	protected final double calc(double[] parameters) throws MathCalcError {
		/* Zufallszahl */
		if (parameters.length==parameterCount+2) {
			final double min=parameters[0];
			final double max=parameters[1];
			if (min>=max) throw error();
			for (int i=0;i<parameterCount;i++) distParameters[i]=parameters[i+2];
			return getRandom(min,max);
		}

		throw error();
	}

	@Override
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		/* Zufallszahl */
		if (parameters.length==parameterCount+2) {
			final double min=parameters[0];
			final double max=parameters[1];
			if (min>=max) return fallbackValue;
			for (int i=0;i<parameterCount;i++) distParameters[i]=parameters[i+2];
			return getRandom(min,max,fallbackValue);
		}

		return fallbackValue;
	}

	@Override
	protected boolean isDeterministic() {
		return false;
	}
}

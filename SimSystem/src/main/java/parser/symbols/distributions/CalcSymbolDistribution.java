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

import org.apache.commons.math3.distribution.AbstractRealDistribution;

import parser.MathCalcError;
import parser.coresymbols.CalcSymbolPreOperator;

/**
 * Abstrakte Basisklasse, die Funktionen zum Zugriff auf Wahrscheinlichkeitsverteilungen
 * (Werte von Dichte und Verteilung sowie Zufallszahlen) anbietet
 * @author Alexander Herzog
 * @version 1.1
 */
public abstract class CalcSymbolDistribution extends CalcSymbolPreOperator {
	/**
	 * Anzahl an Parametern der Wahrscheinlichkeitsverteilung
	 * @see #getParameterCount()
	 */
	public final int parameterCount;

	/**
	 * Array mit den konkreten Verteilungsparametern
	 * (um das wiederholte Anlegen von Arrays zu vermeiden)
	 */
	private double[] distParameters;

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolDistribution() {
		parameterCount=getParameterCount();
	}

	/**
	 * Liefert die Anzahl an Parametern der Wahrscheinlichkeitsverteilung (z.B. 1 für Exp-Verteilung, 2 für Normalverteilung und 4 für Beta-Verteilung)
	 * @return	Anzahl an Parametern der Wahrscheinlichkeitsverteilung selbst
	 * @see CalcSymbolDistribution#getDistribution(double[])
	 */
	protected abstract int getParameterCount();

	/**
	 * Liefert ein Objekt vom Typ {@link AbstractRealDistribution} für die Wahrscheinlichkeitsverteilung,
	 * die durch diese Klasse repräsentiert werden soll, für die angegebenen Parameter
	 * @param parameters	Parameter der Verteilung (wie viele Werte hier übergeben werden, kann in {@link CalcSymbolDistribution#getParameterCount()} festgelegt werden)
	 * @return	Wahrscheinlichkeitsverteilung vom passenden Typ mit den angegebenen Parametern (oder <code>null</code> wenn keine passende Verteilung konstruiert werden konnte)
	 * @see AbstractRealDistribution
	 * @see CalcSymbolDistribution#getParameterCount()
	 */
	protected abstract AbstractRealDistribution getDistribution(double[] parameters);

	/**
	 * Verteilungsobjekt, welches beim letzten Aufruf von {@link #fastGetDistribution(double[])}
	 * geliefert wurde (um es ggf. später wiederverwenden zu können)
	 * @see #fastGetDistribution(double[])
	 */
	private AbstractRealDistribution lastDistribution;

	/**
	 * Parameter beim letzten Aufruf von {@link #fastGetDistribution(double[])}, um bestimmen
	 * zu können, ob {@link #lastDistribution} wiederverwendet werden kann
	 * @see #fastGetDistribution(double[])
	 */
	private double[] lastParameters;

	/**
	 * Erstellt auf Basis der Parameter ein konkretes Verteilungsobjekt
	 * @param parameters	Parameter der Verteilung
	 * @return	Verteilungsobjekt
	 */
	private AbstractRealDistribution fastGetDistribution(final double[] parameters) {
		boolean needNewDistribution=(lastDistribution==null || lastParameters.length!=parameters.length);
		if (!needNewDistribution) for (int i=0;i<lastParameters.length;i++) if (lastParameters[i]!=parameters[i]) {needNewDistribution=true; break;}

		if (needNewDistribution) {
			lastParameters=Arrays.copyOf(parameters,parameters.length);
			lastDistribution=getDistribution(lastParameters);
		}

		return lastDistribution;
	}

	@Override
	protected final double calc(double[] parameters) throws MathCalcError {
		/* Zufallszahl */
		if (parameters.length==parameterCount) {
			AbstractRealDistribution distribution=fastGetDistribution(parameters);
			if (distribution==null) throw error();
			return calcSystem.getRandomNonNegative(distribution);
		}

		/* Dichte oder Verteilung */
		if (parameters.length==parameterCount+2) {
			double type=parameters[parameters.length-1];
			double[] distParameters=Arrays.copyOfRange(parameters,1,parameters.length-1);
			AbstractRealDistribution distribution=fastGetDistribution(distParameters);
			if (distribution==null) throw error();

			if (Math.abs(type-0)<0.0001) {
				final double d=distribution.density(parameters[0]);
				if (Double.isNaN(d) || Double.isInfinite(d)) return 0.0;
				return d;
			}
			if (Math.abs(type-1)<0.0001) {
				final double d=distribution.cumulativeProbability(parameters[0]);
				if (Double.isNaN(d) || Double.isInfinite(d)) return 0.0;
				return d;
			}
			throw error();
		}
		throw error();
	}

	@Override
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		/* Zufallszahl */
		if (parameters.length==parameterCount) {
			final AbstractRealDistribution distribution=fastGetDistribution(parameters);
			if (distribution==null) return fallbackValue;
			return calcSystem.getRandomNonNegative(distribution);
		}

		/* Dichte oder Verteilung */
		if (parameters.length==parameterCount+2) {
			final double type=parameters[parameters.length-1];
			if (distParameters==null) distParameters=new double[parameterCount];
			for (int i=0;i<parameterCount;i++) distParameters[i]=parameters[i+1];
			final AbstractRealDistribution distribution=fastGetDistribution(distParameters);
			if (distribution==null) return fallbackValue;

			if (Math.abs(type-0)<0.0001) {
				final double d=distribution.density(parameters[0]);
				if (Double.isNaN(d) || Double.isInfinite(d)) return 0.0;
				return d;
			}
			if (Math.abs(type-1)<0.0001) {
				final double d=distribution.cumulativeProbability(parameters[0]);
				if (Double.isNaN(d) || Double.isInfinite(d)) return 0.0;
				return d;
			}
			return fallbackValue;
		}
		return fallbackValue;
	}

	@Override
	protected boolean isDeterministic() {
		return false;
	}
}

/**
 * Copyright 2022 Alexander Herzog
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
package parser.symbols;

import java.util.Map;

import parser.CalcSystem;
import parser.MathCalcError;
import parser.coresymbols.CalcSymbol;
import parser.coresymbols.CalcSymbolConst;
import parser.coresymbols.CalcSymbolPreOperator;

/**
 * Zufallszahlengenerator, der auf beliebigen Verteilungsfunktionen operiert
 * @author Alexander Herzog
 * @see CalcSymbolPreOperatorRandomGeneratorInversX
 */
public class CalcSymbolPreOperatorRandomGeneratorInvers extends CalcSymbolPreOperator {
	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"RandomGenerator"};

	/**
	 * Abbruchkriterium für die Größe des restlichen Suchbereichs für das Bisektionsverfahren
	 * @see #calc(double[])
	 * @see #calcOrDefault(double[], double)
	 */
	private static final double precision=0.01d;

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolPreOperatorRandomGeneratorInvers() {
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
	protected double calc(double[] parameters) throws MathCalcError {
		return 0; /* Wird nicht verwendet, da wir getValue überschreiben. */
	}

	/**
	 * Wurde erkannt, dass die Bereichswerte feste Zahlen sind
	 * und wurden diese bereits berechnet?
	 * @see #calcSupport(CalcSystem)
	 * @see #supportMin
	 * @see #supportMax
	 */
	private boolean supportFixed;

	/**
	 * Untere Grenze des Suchbereichs
	 * @see #calcSupport(CalcSystem)
	 */
	private double supportMin;

	/**
	 * Obere Grenze des Suchbereichs
	 * @see #calcSupport(CalcSystem)
	 */
	private double supportMax;

	/**
	 * Berechnet den Suchbereich und speichert die Grenzen
	 * in {@link #supportMin} und {@link #supportMax}
	 * @param calc	Rechensystem
	 * @throws MathCalcError	Löst eine Exception aus, wenn die angegebenen Ausdrücke für die Grenzen nicht berechnet werden können
	 */
	private void calcSupport(final CalcSystem calc) throws MathCalcError {
		if (supportFixed) return;
		supportMin=symbols[1].getValue(calc);
		supportMax=symbols[2].getValue(calc);
		supportFixed=(symbols[1] instanceof CalcSymbolConst) && (symbols[2] instanceof CalcSymbolConst);
	}

	/**
	 * Cache für die Zuordnung, in die die jeweils zu testenden Werte für x geschrieben werden
	 * @see #getValue(CalcSystem)
	 * @see #getValueOrDefault(CalcSystem, double)
	 * @see CalcSymbolPreOperatorRandomGeneratorInversX#specialValuesIdentifier
	 * @see CalcSystem#getSpecialValues()
	 */
	private Map<String,Double> map;

	/**
	 * Berechnet F(x) für ein vorgegebenes x.
	 * @param x	Wert für den F(x) berechnet werden soll
	 * @param calc	Rechensystem
	 * @return	F(x)
	 * @throws MathCalcError	Löst eine Exception aus, wenn der angegebene Verteilungsausdruck nicht berechnet werden konnte
	 */
	protected final double getCalcValue(final double x, final CalcSystem calc) throws MathCalcError {
		if (map==null) map=calc.getSpecialValues();
		map.put(CalcSymbolPreOperatorRandomGeneratorInversX.specialValuesIdentifier,x);
		return symbols[0].getValue(calc);
	}

	@Override
	public final double getValue(final CalcSystem calc) throws MathCalcError {
		if (symbols==null || symbols.length!=3) throw error();

		calcSupport(calc);

		final double u=Math.random();

		final double xMin=getCalcValue(supportMin,calc);
		if (u<=xMin) return supportMin;
		final double xMax=getCalcValue(supportMax,calc);
		if (u>=xMax) return supportMax;

		/*
		Debug:
		System.out.println(String.format("u=%f",u));
		System.out.println(String.format("[%f;%f] -> [%f;%f]",supportMin,supportMax,xMin,xMax));
		 */

		double max=supportMax;
		double min=supportMin;
		while (max-min>precision) {
			final double middle=(max+min)/2;
			final double x=getCalcValue(middle,calc);
			/*
			Debug:
			System.out.println(String.format("[%f;%f], middle=%f -> %f",min,max,middle,x));
			 */
			if (x>u) max=middle; else min=middle;
		}
		return (max+min)/2;
	}

	/**
	 * Liefert den Wert des Symbols
	 * @param calc	Rechensystem (zum Abfragen der aktuellen Werte von Variablen usw.)
	 * @param fallbackValue	Vorgabewert, der zurückgeliefert werden soll, wenn die Berechnung nicht ausgeführt werden konnte
	 * @return	Aktueller Wert des Symbols oder Vorgabewert, wenn der Wert nicht berechnet werden konnte
	 * @see CalcSymbol#getValue(CalcSystem)
	 * @see CalcSystem#calcOrDefault(double[], double)
	 */
	@Override
	public double getValueOrDefault(final CalcSystem calc, final double fallbackValue) {
		if (symbols==null || symbols.length!=3) return fallbackValue;

		try {
			calcSupport(calc);
		} catch (MathCalcError e) {
			return fallbackValue;
		}

		final double u=Math.random();

		try {
			final double xMin=getCalcValue(supportMin,calc);
			if (u<=xMin) return supportMin;
			final double xMax=getCalcValue(supportMax,calc);
			if (u>=xMax) return supportMax;
		} catch (MathCalcError e) {
			return fallbackValue;
		}

		double max=supportMax;
		double min=supportMin;
		while (max-min>precision) {
			final double middle=(max+min)/2;
			final double x;
			try {
				x=getCalcValue(middle,calc);
			} catch (MathCalcError e) {
				return fallbackValue;
			}
			if (x>u) max=middle; else min=middle;
		}
		return (max+min)/2;
	}

	@Override
	protected boolean isDeterministic() {
		return false;
	}
}

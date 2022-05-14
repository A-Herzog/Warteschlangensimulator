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
import parser.coresymbols.CalcSymbolPreOperator;

/**
 * Pseudo-Variable für {@link CalcSymbolPreOperatorRandomGeneratorInvers}
 * @author Alexander Herzog
 * @see CalcSymbolPreOperatorRandomGeneratorInvers
 */
public class CalcSymbolPreOperatorRandomGeneratorInversX extends CalcSymbolPreOperator {
	/**
	 * Bezeichner in {@link CalcSystem#getSpecialValues()} über den
	 * {@link CalcSymbolPreOperatorRandomGeneratorInvers} dieser Klasse mitteilt,
	 * für welchen Wert x die Berechnung erfolgen soll
	 * @see CalcSystem#getSpecialValues()
	 * @see CalcSymbolPreOperatorRandomGeneratorInvers
	 */
	public static final String specialValuesIdentifier="RandomGeneratorX";

	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"RandomGeneratorX"};

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolPreOperatorRandomGeneratorInversX() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	@Override
	public String[] getNames() {
		return names;
	}

	/**
	 * Cache für die Zuordnung, in die die jeweils zu testenden Werte für x geschrieben werden
	 * @see #calc(double[])
	 * @see #getValueOrDefault(CalcSystem, double)
	 * @see CalcSymbolPreOperatorRandomGeneratorInversX#specialValuesIdentifier
	 * @see CalcSystem#getSpecialValues()
	 */
	private Map<String,Double> map;

	@Override
	protected double calc(double[] parameters) throws MathCalcError {
		if (map==null) map=calcSystem.getSpecialValues();
		final Double D=map.get(specialValuesIdentifier);
		if (D==null) throw error();
		return D.doubleValue();
	}

	@Override
	public double getValueOrDefault(final CalcSystem calc, final double fallbackValue) {
		if (map==null) map=calcSystem.getSpecialValues();
		final Double D=map.get(specialValuesIdentifier);
		if (D==null) return fallbackValue;
		return D.doubleValue();
	}

	@Override
	protected boolean isDeterministic() {
		return false;
	}
}

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
package parser.symbols;

import java.util.Arrays;

import parser.MathCalcError;
import parser.coresymbols.CalcSymbolPreOperator;

/**
 * Median der Datenreihe, die aus den übergebenen Parametern gebildet wird
 * @author Alexander Herzog
 */
public final class CalcSymbolPreOperatorMedian extends CalcSymbolPreOperator {
	/**
	 * Liefert den Median der übergebenen Werte.<br>
	 * Bei einer geraden Anzahl an Werten wird zwischen dem Wert vor und nach der Mitte gemittelt.
	 * @param values	Werte deren Medien ermittelt werden soll
	 * @return	Median der Messreihe
	 */
	private double calcMedian(double[] values) {
		Arrays.sort(values);
		final int m=values.length/2;
		if (values.length%2==1) {
			return values[m];
		} else {
			return (values[m-1]+values[m])*0.5;
		}
	}

	@Override
	protected double calc(double[] parameters) throws MathCalcError {
		if (parameters.length==0) throw error();
		return calcMedian(Arrays.copyOf(parameters,parameters.length));
	}

	@Override
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		if (parameters.length==0) return fallbackValue;
		return calcMedian(Arrays.copyOf(parameters,parameters.length));
	}

	@Override
	public String[] getNames() {
		return new String[]{"Median"};
	}
}

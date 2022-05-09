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
import simulator.simparser.ExpressionCalc;
import simulator.simparser.coresymbols.CalcSymbolSimData;

/**
 * Versucht einen Stationsnamen in eine ID zu übersetzen.<br>
 * Der Parser erstellt bereits eine Übersetzungstabelle von (vom Nutzer angegebenen)
 * Zeichenketten zu Index-Werten. Dieser Index-Wert wird der Funktion als Parameter
 * übergeben. Zur Laufzeit (erst dann können Stationsnamen zu IDs aufgelöst werden)
 * kann dann eine Übersetzung von Index-Werten (und den zugehörigen Strings in der
 * Übersetzungstabelle) zu IDs erfolgen.
 * @see ExpressionCalc#getStationIDFromTranslationIndex(int)
 * @author Alexander Herzog
 *
 */
public class CalcSymbolStationText extends CalcSymbolSimData {
	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"$"};

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolStationText() {
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
		if (parameters.length!=1) throw error();

		final int index=(int)FastMath.round(parameters[0]);

		return ((ExpressionCalc)calcSystem).getStationIDFromTranslationIndex(index);
	}

	@Override
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		if (parameters.length!=1) return fallbackValue;

		final int index=(int)FastMath.round(parameters[0]);

		return ((ExpressionCalc)calcSystem).getStationIDFromTranslationIndex(index);
	}
}

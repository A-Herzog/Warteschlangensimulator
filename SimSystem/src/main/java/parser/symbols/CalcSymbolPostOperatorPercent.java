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

import parser.MathCalcError;
import parser.coresymbols.CalcSymbolPostOperator;

/**
 * Interpretation einer Zahl als Prozentwert (nachgestelltes "%"-Symbol).<br>
 * Praktisch wird der Wert vor diesem Symbol vor der Weiterverarbeitung durch 100 dividiert.
 * @author Alexander Herzog
 */
public final class CalcSymbolPostOperatorPercent extends CalcSymbolPostOperator {
	@Override
	public String[] getNames() {
		return new String[]{"%","percent","prozent","vonhundert"};
	}

	/**
	 * Faktor zur Umrechnung Zahl -> Prozentwert,
	 * um die zeitaufwendige Division zu vermeiden.
	 */
	private final static double factor=1.0/100.0;

	@Override
	protected double calc(final double parameter) throws MathCalcError {
		return parameter*factor;
	}
}

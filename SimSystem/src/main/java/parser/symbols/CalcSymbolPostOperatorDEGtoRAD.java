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

import parser.coresymbols.CalcSymbolPostOperator;

/**
 * Wandelt eine DEG-Grad (Vollkreis=360°) Angabe in eine Bogenmaßzahl um
 * @author Alexander Herzog
 */
public class CalcSymbolPostOperatorDEGtoRAD extends CalcSymbolPostOperator {
	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"°"};

	@Override
	public String[] getNames() {
		return names;
	}

	/**
	 * Vorab berechneter Wert pi/180, um die
	 * eigentlichen Berechnungen zu beschleunigen.
	 */
	private static final double factor=Math.PI/180;

	@Override
	protected double calc(double parameter) {
		return parameter*factor;
	}
}

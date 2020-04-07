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
package parser.coresymbols;

import parser.CalcSystem;

/**
 * Objekte dieser Klasse repräsentieren einen konstanten Wert
 * (entweder definiert durch ein Konstantwert-Zeichen (wie "pi")
 * oder direkt durch eine Zahl
 * @author Alexander Herzog
 * @see CalcSymbol
 */
public abstract class CalcSymbolConst extends CalcSymbol {
	/**
	 * Wert des Objekts
	 */
	private double value;

	@Override
	public final SymbolType getType() {
		return CalcSymbol.SymbolType.TYPE_CONST;
	}

	private Double lastBoxedValue=null;

	@Override
	public final Double getValue(final CalcSystem calc) {
		if (lastBoxedValue==null || lastBoxedValue!=value) lastBoxedValue=value;
		return lastBoxedValue;
	}

	@Override
	public final Object getSimplify() {
		return value;
	}

	@Override
	public final CalcSymbol cloneSymbol() {
		CalcSymbolConst clone=(CalcSymbolConst)super.cloneSymbol();
		clone.value=value;
		return clone;
	}

	/**
	 * Liefert den Wert des Objekts
	 * @return	Wert des Objekts
	 */
	public double getValue() {
		return value;
	}

	/**
	 * Stellt den Wert des Symbols ein.
	 * @param value	Neuer Wert
	 */
	protected void setValue(double value) {
		this.value=value;
	}
}

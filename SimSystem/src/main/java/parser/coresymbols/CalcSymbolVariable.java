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
import parser.CalcSystemBase;

/**
 * Die Objekte dieser Klasse repräsentieren Variablen
 * @author Alexander Herzog
 */
public class CalcSymbolVariable extends CalcSymbol implements CalcSymbolDirectValue {
	/**
	 * Index der Variable in der Liste der Variablen in {@link CalcSystemBase#variables}
	 */
	private int variableIndex;

	/**
	 * Stellt ein, auf welchen Variablenwert sich diese Variable beziehen soll
	 * @param variableIndex	Index der Variable in der Liste der Variablen in {@link CalcSystemBase#variables}
	 */
	public void setData(final int variableIndex) {
		this.variableIndex=variableIndex;
	}

	@Override
	public final SymbolType getType() {
		return CalcSymbol.SymbolType.TYPE_CONST;
	}

	@Override
	public final Double getValue(final CalcSystem calc) {
		if (calc.values==null || variableIndex<0 || variableIndex>=calc.values.length) return null;
		return fastBoxedValue(calc.values[variableIndex]);
	}

	/**
	 * Prüft, ob sich das Variablenobjekt auf einen gültigen Variableneintrag in der Liste
	 * aller Variablenwerte bezieht
	 * @param calc	Rechensystem (zum Abfragen der aktuellen Werte von Variablen usw.)
	 * @return	Liefert <code>true</code> wenn die Variable einen gültigen Wert besitzt
	 */
	@Override
	public boolean getValueDirectOk(final CalcSystem calc) {
		return !(calc.values==null || variableIndex<0 || variableIndex>=calc.values.length);
	}

	/**
	 * Liefert, vorausgesetzt der Wert ist verfügbar ({@link CalcSymbolVariable#getValueDirectOk(CalcSystem)}),
	 * den Variablenwert ohne unnötiges Boxing
	 * @param calc	Rechensystem (zum Abfragen der aktuellen Werte von Variablen usw.)
	 * @return	Variablenwert
	 */
	@Override
	public final double getValueDirect(final CalcSystem calc) {
		if (calc.values==null || variableIndex<0 || variableIndex>=calc.values.length) return 0;
		return calc.values[variableIndex];
	}

	@Override
	public Object getSimplify() {
		return this;
	}

	@Override
	public CalcSymbol cloneSymbol() {
		CalcSymbolVariable clone=(CalcSymbolVariable)super.cloneSymbol();
		clone.variableIndex=variableIndex;
		return clone;
	}

	@Override
	public String[] getNames() {
		return new String[]{};
	}
}

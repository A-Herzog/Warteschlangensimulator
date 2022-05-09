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
import parser.MathCalcError;

/**
 * Die Objekte dieser Klasse repräsentieren Variablen
 * @author Alexander Herzog
 */
public class CalcSymbolVariable extends CalcSymbol {
	/**
	 * Index der Variable in der Liste der Variablen in {@link CalcSystemBase#variables}
	 */
	private int variableIndex;

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolVariable() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

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
	public final double getValue(final CalcSystem calc) throws MathCalcError {
		if (calc.values==null || variableIndex<0 || variableIndex>=calc.values.length) throw error();
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

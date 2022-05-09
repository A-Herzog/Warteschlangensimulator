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
import parser.MathCalcError;

/**
 * Abstrakte Basisklasse für nachgestellte Operatoren (wie "%")
 * @author Alexander Herzog
 */
public abstract class CalcSymbolPostOperator extends CalcSymbolFunction {
	/**
	 * Parameter des Operators
	 */
	protected CalcSymbol sub;

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolPostOperator() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	@Override
	public final SymbolType getType() {
		return CalcSymbol.SymbolType.TYPE_POST_OPERATOR;
	}

	@Override
	public final int getPriority() {
		return (sub==null)?20:0;
	}

	@Override
	public final boolean setParameter(CalcSymbol[] symbols) {
		if (symbols==null || symbols.length!=1) return false;
		sub=symbols[0];
		return true;
	}
	/**
	 * Versucht den Operator zu berechnen, wenn der Zahlenwert des Parameters bekannt ist
	 * @param parameter 	Zahlenwert des Parameters
	 * @return	Liefert im Erfolgsfall das Ergebnis, sonst <code>null</code>
	 * @throws	MathCalcError	Fehler während der Berechnung
	 */
	protected abstract double calc(final double parameter) throws MathCalcError;

	@Override
	public final double getValue(final CalcSystem calc) throws MathCalcError {
		if (sub==null) throw error();
		return calc(sub.getValue(calc));
	}

	@Override
	public final CalcSymbol cloneSymbol() {
		CalcSymbolPostOperator clone=(CalcSymbolPostOperator)super.cloneSymbol();
		if (sub!=null) clone.sub=sub.cloneSymbol();
		return clone;
	}

	/**
	 * Versucht das Unterelement für die Parameter zu vereinfachen.
	 * @return	Liefert im Erfolgsfall das neue Unterelement, sonst <code>null</code>
	 * @see #getSimplify()
	 */
	private Double getSimpleConstSub() {
		if (sub==null) return null;
		final Object subSimple=sub.getSimplify();
		if (subSimple instanceof Double) return (Double)subSimple;
		if (subSimple instanceof CalcSymbolConst) return ((CalcSymbolConst)subSimple).getValue();
		return null;
	}

	@Override
	public Object getSimplify() {
		final Double sub=getSimpleConstSub();
		if (sub==null) return this;
		try {
			return calc(sub);
		} catch (MathCalcError e) {
			return this;
		}
	}
}

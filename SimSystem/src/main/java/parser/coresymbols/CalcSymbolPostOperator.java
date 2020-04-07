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
 * Abstrakte Basisklasse für nachgestellte Operatoren (wie "%")
 * @author Alexander Herzog
 */
public abstract class CalcSymbolPostOperator extends CalcSymbolFunction {
	/**
	 * Parameter des Operators
	 */
	protected CalcSymbol sub;

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
	 */
	protected abstract Double calc(final double parameter);

	@Override
	public final Double getValue(final CalcSystem calc) {

		Double value;
		if (sub instanceof CalcSymbolConst) {
			value=((CalcSymbolConst)sub).getValue();
		} else {
			if (sub instanceof CalcSymbolVariable) {
				if (!((CalcSymbolVariable)sub).getValueDirectOk(calc)) return null;
				value=((CalcSymbolVariable)sub).getValueDirect(calc);
			} else {
				if (sub==null) return null;
				final Double val=sub.getValue(calc);
				if (val==null) return null;
				value=val;
			}
		}

		return calc(value);
	}

	@Override
	public final CalcSymbol cloneSymbol() {
		CalcSymbolPostOperator clone=(CalcSymbolPostOperator)super.cloneSymbol();
		if (sub!=null) clone.sub=sub.cloneSymbol();
		return clone;
	}

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
		final Double result=calc(sub);
		if (result!=null) return result;
		return this;
	}
}

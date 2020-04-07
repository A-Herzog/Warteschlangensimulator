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
 * Abstrakte Basisklasse für zweiwertige, in der Mitte stehende Operatoren (wie z.B. "+")
 * @author Alexander Herzog
 */
public abstract class CalcSymbolMiddleOperator extends CalcSymbolFunction {
	/**
	 * Linker Parameter
	 */
	protected CalcSymbol left;

	/**
	 * Rechter Parameter
	 */
	protected CalcSymbol  right;

	@Override
	public final SymbolType getType() {
		return CalcSymbol.SymbolType.TYPE_MIDDLE_OPERATOR;
	}

	/**
	 * Versucht den Operator zu berechnen, wenn die Zahlenwerte des linken und des rechten
	 * Parameters bekannt sind
	 * @param left	Zahlenwert des linken Parameters
	 * @param right	Zahlenwert des rechten Parameters
	 * @return	Liefert im Erfolgsfall das Ergebnis, sonst <code>null</code>
	 */
	protected abstract Double calc(final double left, final double right);

	/**
	 * Versucht den Operator zu berechnen, wenn die Zahlenwerte des linken und des rechten
	 * Parameters bekannt sind
	 * @param left	Zahlenwert des linken Parameters
	 * @param right	Zahlenwert des rechten Parameters
	 * @param fallbackValue	Vorgabewert, der zurückgeliefert werden soll, wenn die Berechnung nicht ausgeführt werden konnte
	 * @return	Liefert im Erfolgsfall das Ergebnis der Berechnung, sonst den angegebenen Vorgabewert
	 */
	protected abstract double calcOrDefault(final double left, final double right, final double fallbackValue);

	@Override
	public final Double getValue(final CalcSystem calc) {
		if (left==null || right==null) return null;

		double valLeft;
		if (left instanceof CalcSymbolConst) {
			valLeft=((CalcSymbolConst)left).getValue();
		} else {
			if (left instanceof CalcSymbolVariable) {
				if (!((CalcSymbolVariable)left).getValueDirectOk(calc)) return null;
				valLeft=((CalcSymbolVariable)left).getValueDirect(calc);
			} else {
				if (left instanceof CalcSymbolNumber) {
					valLeft=((CalcSymbolNumber)left).getValue();
				} else {
					Double value=left.getValue(calc);
					if (value==null) return null;
					valLeft=value;
				}
			}
		}

		double valRight;
		if (right instanceof CalcSymbolConst) {
			valRight=((CalcSymbolConst)right).getValue();
		} else {
			if (right instanceof CalcSymbolVariable) {
				if (!((CalcSymbolVariable)right).getValueDirectOk(calc)) return null;
				valRight=((CalcSymbolVariable)right).getValueDirect(calc);
			} else {
				if (right instanceof CalcSymbolNumber) {
					valRight=((CalcSymbolNumber)right).getValue();
				} else {
					Double value=right.getValue(calc);
					if (value==null) return null;
					valRight=value;
				}
			}
		}

		/*
		Double l=left.getValue(calc);
		if (l==null) return null;
		Double r=right.getValue(calc);
		if (r==null) return null;
		 */

		return calc(valLeft,valRight);
	}

	/**
	 * Liefert den Wert des Symbols
	 * @param calc	Rechensystem (zum Abfragen der aktuellen Werte von Variablen usw.)
	 * @param fallbackValue	Vorgabewert, der zurückgeliefert werden soll, wenn die Berechnung nicht ausgeführt werden konnte
	 * @return	Aktueller Wert des Symbols oder Vorgabewert, wenn der Wert nicht berechnet werden konnte
	 * @see CalcSymbol#getValue(CalcSystem)
	 * @see CalcSystem#calcOrDefault(double[], double)
	 */
	public double getValueOrDefault(final CalcSystem calc, final double fallbackValue) {
		if (left==null || right==null) return fallbackValue;

		/* Linke Seite */

		boolean okLeft=false;
		double valLeft=0;

		if (left instanceof CalcSymbolConst) {
			valLeft=((CalcSymbolConst)left).getValue();
			okLeft=true;
		}

		if (!okLeft && (left instanceof CalcSymbolVariable)) {
			if (!((CalcSymbolVariable)left).getValueDirectOk(calc)) return fallbackValue;
			valLeft=((CalcSymbolVariable)left).getValueDirect(calc);
			okLeft=true;
		}

		if (!okLeft && (left instanceof CalcSymbolNumber)) {
			valLeft=((CalcSymbolNumber)left).getValue();
			okLeft=true;
		}

		if (!okLeft) {
			Double value=left.getValue(calc);
			if (value==null) return fallbackValue;
			valLeft=value;
		}

		/* Rechte Seite */

		boolean okRight=false;
		double valRight=0;

		if (right instanceof CalcSymbolConst) {
			valRight=((CalcSymbolConst)right).getValue();
			okRight=true;
		}

		if (!okRight && (right instanceof CalcSymbolVariable)) {
			if (!((CalcSymbolVariable)right).getValueDirectOk(calc)) return fallbackValue;
			valRight=((CalcSymbolVariable)right).getValueDirect(calc);
			okRight=true;
		}

		if (!okRight && (right instanceof CalcSymbolNumber)) {
			valRight=((CalcSymbolNumber)right).getValue();
			okRight=true;
		}

		if (!okRight) {
			Double value=right.getValue(calc);
			if (value==null) return fallbackValue;
			valRight=value;
		}

		return calcOrDefault(valLeft,valRight,fallbackValue);
	}

	@Override
	public final boolean setParameter(CalcSymbol[] symbols) {
		if (symbols==null || symbols.length!=2) return false;
		left=symbols[0];
		right=symbols[1];
		return true;
	}

	@Override
	public final Object getSimplify() {
		if (left==null || right==null) return this;
		Object l=left.getSimplify();
		Object r=right.getSimplify();
		if (l instanceof Double && r instanceof Double) return calc((Double)l,(Double)r);
		if (l instanceof Double) {
			CalcSymbolMiddleOperator clone;
			try {clone=(CalcSymbolMiddleOperator)clone();} catch (CloneNotSupportedException e) {return null;}
			CalcSymbolNumber num=new CalcSymbolNumber();
			num.setValue((Double)l);
			clone.left=num;
			clone.right=right.cloneSymbol();
			return clone;
		}
		if (r instanceof Double) {
			CalcSymbolMiddleOperator clone;
			try {clone=(CalcSymbolMiddleOperator)clone();} catch (CloneNotSupportedException e) {return null;}
			CalcSymbolNumber num=new CalcSymbolNumber();
			num.setValue((Double)r);
			clone.left=left.cloneSymbol();
			clone.right=num;
			return clone;
		}
		return this;
	}

	@Override
	public final CalcSymbol cloneSymbol() {
		CalcSymbolMiddleOperator clone=(CalcSymbolMiddleOperator)super.cloneSymbol();
		if (left!=null)	clone.left=left.cloneSymbol();
		if (right!=null) clone.right=right.cloneSymbol();
		return clone;
	}
}

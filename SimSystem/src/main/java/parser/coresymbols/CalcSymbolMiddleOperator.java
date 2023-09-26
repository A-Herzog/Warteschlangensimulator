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

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolMiddleOperator() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	@Override
	public final SymbolType getType() {
		return CalcSymbol.SymbolType.TYPE_MIDDLE_OPERATOR;
	}

	/**
	 * Versucht den Operator zu berechnen, wenn die Zahlenwerte des linken und des rechten
	 * Parameters bekannt sind
	 * @param left	Zahlenwert des linken Parameters
	 * @param right	Zahlenwert des rechten Parameters
	 * @return	Liefert das Ergebnis
	 * @throws	MathCalcError	Fehler während der Berechnung
	 */
	protected abstract double calc(final double left, final double right) throws MathCalcError;

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
	public final double getValue(final CalcSystem calc) throws MathCalcError {
		if (left==null || right==null) throw error();

		final double valLeft=left.getValue(calc);
		final double valRight=right.getValue(calc);

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

		double valLeft;
		try {
			valLeft=left.getValue(calc);
		} catch (MathCalcError e) {
			return fallbackValue;
		}

		double valRight;
		try {
			valRight=right.getValue(calc);
		} catch (MathCalcError e) {
			return fallbackValue;
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
	public Object getSimplify() {
		if (left==null || right==null) return this;
		Object l=left.getSimplify();
		Object r=right.getSimplify();
		if (l instanceof Double && r instanceof Double) {
			try {
				return calc((Double)l,(Double)r);
			} catch (MathCalcError e) {
				return this;
			}
		}
		if (l instanceof Double) {
			CalcSymbolMiddleOperator clone;
			try {clone=(CalcSymbolMiddleOperator)clone();} catch (CloneNotSupportedException e) {return null;}
			CalcSymbolNumber num=new CalcSymbolNumber();
			num.setValue((Double)l);
			clone.left=num;
			clone.right=(r instanceof CalcSymbol)?((CalcSymbol)r):right.cloneSymbol();
			return clone;
		}
		if (r instanceof Double) {
			CalcSymbolMiddleOperator clone;
			try {clone=(CalcSymbolMiddleOperator)clone();} catch (CloneNotSupportedException e) {return null;}
			CalcSymbolNumber num=new CalcSymbolNumber();
			num.setValue((Double)r);
			clone.left=(l instanceof CalcSymbol)?((CalcSymbol)l):left.cloneSymbol();
			clone.right=num;
			return clone;
		}

		if ((l instanceof CalcSymbol) && (r instanceof CalcSymbol)) {
			CalcSymbolMiddleOperator clone;
			try {clone=(CalcSymbolMiddleOperator)clone();} catch (CloneNotSupportedException e) {return null;}
			clone.left=(CalcSymbol)l;
			clone.right=(CalcSymbol)r;
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

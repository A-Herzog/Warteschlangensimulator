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
 * Basis-Symbol für den Formelparser
 * @author Alexander Herzog
 * @version 1.2
 * @see CalcSystem
 */
public abstract class CalcSymbol implements Cloneable {
	/**
	 * Typ des Symbols
	 * @see CalcSymbol#getType()
	 */
	public enum SymbolType {
		/** Symbol ist eine Konstante */
		TYPE_CONST,

		/** Symbol ist eine mittiger binärer Operator (wie "+") */
		TYPE_MIDDLE_OPERATOR,

		/** Symbol ist ein nachgestellter unärer Operator (wie "!") */
		TYPE_POST_OPERATOR,

		/** Symbol ist ein vorangestellter Operator, d.h. eine Funktion (wie "sin()") */
		TYPE_FUNCTION,

		/** Symbol ist eine Reihe von Parametern (einer Funktion) */
		TYPE_SUB
	}

	/**
	 * Position des Symbols in dem ursprünglichen geparsten Text
	 */
	public int position;

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbol() {
		position=-1;
	}

	/**
	 * Liefert eine Liste mit den möglichen Namen des Symbols
	 * @return	Liste mit den möglichen Namen des Symbols
	 */
	public abstract String[] getNames();

	/**
	 * Liefert den Typ des Symbols
	 * @return Typ des Symbols
	 * @see SymbolType#TYPE_CONST
	 * @see SymbolType#TYPE_MIDDLE_OPERATOR
	 * @see SymbolType#TYPE_POST_OPERATOR
	 * @see SymbolType#TYPE_FUNCTION
	 * @see SymbolType#TYPE_SUB
	 */
	public abstract SymbolType getType();

	/**
	 * Liefert den Wert des Symbols
	 * @param calc	Rechensystem (zum Abfragen der aktuellen Werte von Variablen usw.)
	 * @return	Aktueller Wert des Symbols
	 * @throws	MathCalcError	Fehler während der Berechnung
	 */
	public abstract double getValue(final CalcSystem calc) throws MathCalcError;

	/**
	 * Versucht den Ausdruck (und seine Kind-Elemente) zu vereinfachen
	 * @return	Liefert entweder das aktuelle Objekt zurück oder, wenn möglich, eine vereinfachte Variante
	 */
	public Object getSimplify() {return this;}

	/**
	 * Erstellt eine Kopie des Symbols
	 * @return	Kopie des Symbols
	 */
	public CalcSymbol cloneSymbol() {
		try {
			CalcSymbol clone=(CalcSymbol)clone();
			clone.position=position;
			return clone;
		} catch (CloneNotSupportedException e) {return null;}
	}

	/**
	 * Erstellt ein {@link MathCalcError}-Objekt, welches per <code>throw</code> zurückgegeben werden kann.
	 * @return	{@link MathCalcError}-Objekt
	 */
	protected final MathCalcError error() {
		return new MathCalcError(this);
	}
}

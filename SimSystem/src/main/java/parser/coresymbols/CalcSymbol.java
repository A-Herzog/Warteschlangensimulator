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

import org.apache.commons.math3.util.FastMath;

import parser.CalcSystem;

/**
 * Basis-Symbol für den Formelparser
 * @author Alexander Herzog
 * @version 1.1
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
	 * Versucht statt dem automatischen Boxing ein vorgefertigtes <code>Double</code>-Objekt für die <code>long</code>-Zahl zu liefern.
	 * @param value	Zu boxende Zahl
	 * @return	Geboxte Zahl (entweder per automatischem Boxing oder wenn möglich über das Caching-System)
	 */
	protected Double fastBoxedValue(final long value) {
		if (value>0) {
			if (value>=CalcSystem.fastPositiveResults.length) return ((double)value);
			return CalcSystem.fastPositiveResults[(int)value];
		} else {
			if ((-value)>=CalcSystem.fastNegativeResults.length) return ((double)value);
			return CalcSystem.fastNegativeResults[(int)(-value)];
		}
	}

	private Double lastValue=null;

	/**
	 * Versucht statt dem automatischen Boxing ein vorgefertigtes <code>Double</code>-Objekt für die <code>double</code>-Zahl zu liefern.
	 * @param value	Zu boxende Zahl
	 * @return	Geboxte Zahl (entweder per automatischem Boxing oder wenn möglich über das Caching-System)
	 */
	protected Double fastBoxedValue(final double value) {
		if (lastValue!=null && lastValue.doubleValue()==value) return lastValue;

		if (FastMath.floor(value)!=value) {
			if (value>0) {
				final double scaled=value*1000;
				if(scaled%1==0) {
					final long scaledIndex=(long)FastMath.floor(scaled);
					if (scaledIndex<CalcSystem.fastPositiveFractionalResults.length)
						return CalcSystem.fastPositiveFractionalResults[(int)scaledIndex];
				}
			}

			return lastValue=value;
		}

		final long l=(long)value;
		if (l>0) {
			if (l>=CalcSystem.fastPositiveResults.length) return lastValue=value;
			return CalcSystem.fastPositiveResults[(int)l];
		} else {
			if ((-l)>=CalcSystem.fastNegativeResults.length) return lastValue=value;
			return CalcSystem.fastNegativeResults[(int)(-l)];
		}
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
	 */
	public abstract Double getValue(final CalcSystem calc);

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
}

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

/**
 * Abstrakte Basisklasse für alle Symbole, die Funktionen bzw. Operatoren repräsentieren
 * (das können klassische Funktionen f(x), in der Mitte stehende Operatoren wie "+" oder
 * nach gestellte Funktionssymbole wie "%" sein).
 * @author Alexander Herzog
 * @see CalcSymbolPreOperator
 * @see CalcSymbolMiddleOperator
 * @see CalcSymbolPostOperator
 */
public abstract class CalcSymbolFunction extends CalcSymbol {
	/**
	 * Stellt die Parameter für die Funktion ein
	 * @param symbols	Liste mit Parametern
	 * @return	Liefert <code>true</code> wenn die Parameter für das Symbol sinnvoll waren und eingetragen werden konnten.
	 */
	public abstract boolean setParameter(CalcSymbol[] symbols);

	/**
	 * Liefert die Priorität des Funktionssymbols Parameter an sich zu binden.
	 * Auf diese Weise lässt sich z.B. Punkt- vor Strichrechnung abbilden.
	 * @return	Priorität (höhere Werte stellen eine höhere Priorität dar)
	 */
	public abstract int getPriority();
}

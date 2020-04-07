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
package parser;

/**
 * Interface welche die nach außen sichtbaren Funktionen
 * eines Parsers für mathematische Ausdrücke auflistet.
 * Konkrete Parser müssen dieses Interface implementieren.
 * Anwendungen können dieses Interface nutzen, um von der
 * konkreten Implementierung des Parsers unabhängig zu sein.
 * @author Alexa
 * @see CalcSystemBase
 */
public interface MathParser {

	/**
	 * Versucht den übergebenen übergebenen Ausdruck zu interpretieren, dabei werden
	 * Variablennamen erkannt usw., es wird aber noch kein konkreter Wert berechnet.
	 * @param text	Ausdruck, der verarbeitet werden soll
	 * @return	Gibt <code>-1</code> zurück, wenn der Ausdruck verarbeitet werden konnte.
	 */
	public int parse(String text);

	/**
	 * Versucht den im Konstruktor übergebenen Ausdruck zu interpretieren, dabei werden
	 * Variablennamen erkannt usw., es wird aber noch kein konkreter Wert berechnet.
	 * @return	Gibt <code>-1</code> zurück, wenn der Ausdruck verarbeitet werden konnte.
	 */
	public int parse();

	/**
	 * Berechnet den bereits geparsten Ausdruck auf Basis der bekannten Variablennamen und der hier angegebenen Werte.
	 * @param variableValues	Liste mit den Werten der Variablen
	 * @return	Gibt im Fehlerfall <code>null</code> zurück, sonst den Zahlenwert des Ergebnisses.
	 */
	public Double calc(double[] variableValues);

	/**
	 * Berechnet den bereits geparsten Ausdruck auf Basis der bekannten Variablennamen und der hier angegebenen Werte.
	 * @param	variableValues	Liste mit den Werten der Variablen
	 * @param	fallbackValue	Wert, der zurückgegeben werden soll, wenn der Ausdruck nicht berechnet werden konnte
	 * @return	Berechneter Wert oder im Fehlerfall der Vorgabewert
	 */
	public double calcOrDefault(double[] variableValues, double fallbackValue);

	/**
	 * Berechnet den bereits geparsten Ausdruck auf Basis der bekannten Variablennamen und Werte.
	 * @return	Gibt im Fehlerfall <code>null</code> zurück, sonst den Zahlenwert des Ergebnisses.
	 */
	public Double calc();

	/**
	 * Gibt an, ob sich die Formel auf eine konstante Zahl reduzieren lässt (also keine Variablen enthält).
	 * @return Gibt <code>true</code> zurück, wenn es sich um eine konstante Zahl handelt.
	 */
	public boolean isConstValue();

	/**
	 * Handelt es sich bei der Formel um eine konstante Zahl, so kann diese über diese Methode abgefragt werden.
	 * @return	Konstanter Wert der Formel
	 * @see #isConstValue()
	 */
	public double getConstValue();
}
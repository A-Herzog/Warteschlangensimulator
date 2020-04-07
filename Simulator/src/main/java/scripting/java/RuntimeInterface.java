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
package scripting.java;

/**
 * Teil-Interface, damit Nutzer-Java-Codes auf allgemeine, simulationsunabhängige Daten zugreifen kann.
 * @author Alexander Herzog
 * @see SimulationInterface
 */
public interface RuntimeInterface {
	/**
	 * Berechnet einen Ausdruck.
	 * @param expression	Zu berechnender Ausdruck.
	 * @return	Liefert im Erfolgsfall ein {@link Double}-Objekt. Im Fehlerfall eine Fehlermeldung.
	 */
	Object calc(final String expression);

	/**
	 * Liefert die Computerzeit in Millisekunden für Laufzeitmessungen.
	 * @return	Computerzeit in Millisekunden
	 */
	long getTime();

	/**
	 * Lädt einen einzelnen Wert von einer Internetadresse
	 * @param url	Aufzurufende URL
	 * @param errorValue	Wert der im Fehlerfall zurückgeliefert werden soll
	 * @return	Gelesener Wert oder <code>errorValue</code> im Fehlerfall
	 */
	double getInput(final String url, final double errorValue);
}

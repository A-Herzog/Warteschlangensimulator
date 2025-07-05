/**
 * Copyright 2025 Alexander Herzog
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
package simulator.coreelements;

/**
 * Laufzeitdaten-Objekt, welches mehrere konkrete Wert für die Station besitzt
 * (z.B. bei einem Mehrfachzähler)
 * @author Alexander Herzog
 */
public interface RunElementDataWithMultiValues {
	/**
	 * Anzahl der möglichen abfragbaren Werte über {@link #getValue(int, boolean)}.
	 * (Index in {@link #getValue(int, boolean)} läuft dann von 0 bis eins weniger als dieser Wert)
	 * @return	Anzahl der möglichen abfragbaren Werte
	 */
	int getValueCount();

	/**
	 * Liefert den Zählerwert
	 * @param index	Index des abzufragenden Teilzählers (Indizes laufen ab 0)
	 * @param fullValue	Gesamtwert (<code>true</code>) oder Anteil (<code>false</code>; bei Zählergruppen)
	 * @return	Wert des Zählers
	 */
	double getValue(final int index, final boolean fullValue);
}

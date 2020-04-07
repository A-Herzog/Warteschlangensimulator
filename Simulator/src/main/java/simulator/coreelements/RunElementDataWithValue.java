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
package simulator.coreelements;

/**
 * Laufzeitdaten-Objekt, welches einen konkreten Wert f�r die Station besitzt
 * (z.B. bei einem Z�hler)
 * @author Alexander Herzog
 */
public interface RunElementDataWithValue {
	/**
	 * Liefert den Z�hlerwert
	 * @param fullValue	Gesamtwert (<code>true</code>) oder Anteil (<code>false</code>; bei Z�hlergruppen)
	 * @return	Wert des Z�hlers
	 */
	double getValue(final boolean fullValue);
}

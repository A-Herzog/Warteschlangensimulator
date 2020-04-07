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
 * Implementierungsklasse für das Interface {@link InputValueInterface}
 * @author Alexander Herzog
 * @see SimulationInterface
 * @see SimulationImpl
 */
public class InputValueImpl implements InputValueInterface {
	private double inputValue;

	/**
	 * Konstruktor der Klasse
	 */
	public InputValueImpl() {
		inputValue=0.0;
	}

	/**
	 * Stellt eine Eingangsgröße für die Abfrage durch das Java-Verknüpfungs-Objekt ein.
	 * @param value	Eingangsgröße (z.B. aus einer Datei geladener Zahlenwert)
	 */
	public void set(final double value) {
		inputValue=value;
	}

	/**
	 * Liefert den eingestellten aktuellen Eingabewert (z.B. aus einer Eingabedatei).
	 * @return	Eingabewert
	 */
	@Override
	public double get() {
		return inputValue;
	}
}

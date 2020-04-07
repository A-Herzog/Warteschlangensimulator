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
 * Rechensymbole, die dieses Interface implementieren k�nnen gesichert ein Ergebnis
 * zur Verf�gung stellen, ohne dabei evtl. einen <code>null</code>-Wert liefern zu m�ssen.
 * @author Alexander Herzog
 */
public interface CalcSymbolDirectValue {
	/**
	 * Gibt an, ob ein direkter Wert verf�gbar ist.<br>
	 * Wenn kein direkter Wert verf�gbar ist, hei�t das nicht automatisch, dass
	 * {@link CalcSymbol#getValue(CalcSystem)} gleich <code>null</code> ist, sondern nur,
	 * dass dies nicht ausgeschlossen werden kann.
	 * @param calc	Rechensystem
	 * @return	Liefert <code>true</code> wenn garantiert ein direkter Wert, der nicht <code>null</code> ist, verf�gbar ist.
	 */
	public boolean getValueDirectOk(final CalcSystem calc);

	/**
	 * Liefert den direkt verf�gbaren Wert
	 * @param calc	Rechensystem
	 * @return	Direkt verf�gbarer Wert
	 * @see CalcSymbolDirectValue#getValueDirectOk(CalcSystem)
	 */
	public double getValueDirect(final CalcSystem calc);
}

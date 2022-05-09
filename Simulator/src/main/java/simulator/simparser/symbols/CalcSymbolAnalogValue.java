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
package simulator.simparser.symbols;

import simulator.coreelements.RunElementAnalogProcessingData;
import simulator.coreelements.RunElementData;
import simulator.simparser.coresymbols.CalcSymbolStationData;

/**
 * Liefert den aktuellen Wert des "Analoger Wert"-Elements oder des "Tank"-Elements id (1. Parameter).
 * @author Alexander Herzog
 */
public class CalcSymbolAnalogValue extends CalcSymbolStationData {
	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"AnalogWert","AnalogValue"};

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolAnalogValue() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	@Override
	public String[] getNames() {
		return names;
	}

	@Override
	protected double calc(final RunElementData data) {
		if (!(data instanceof RunElementAnalogProcessingData)) return 0.0;
		final RunElementAnalogProcessingData analogData=(RunElementAnalogProcessingData)data;

		return analogData.getValue(getSimData());
	}
}
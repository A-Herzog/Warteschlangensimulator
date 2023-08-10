/**
 * Copyright 2023 Alexander Herzog
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

import simulator.coreelements.RunElementData;
import simulator.elements.RunElementDifferentialCounterData;
import simulator.simparser.coresymbols.CalcSymbolStationData;

/**
 * Liefert die Standardabweichung des Differentz�hlerwertes an Station id.<br>
 * (Kann nur auf "Differenzz�hler"-Elemente angewandt werden.)
 * @author Alexander Herzog
 */
public class CalcSymbolSimDataCounterStd extends CalcSymbolStationData {
	/**
	 * Namen f�r das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"Z�hler_Standardabweichung","Z�hler_Std","Z�hler_SD","Counter_Std","Counter_SD","Value_Std","Value_SD","Wert_Standardabweichung","Wert_Std","Wert_SD"};

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolSimDataCounterStd() {
		/*
		 * Wird nur ben�tigt, um einen JavaDoc-Kommentar f�r diesen (impliziten) Konstruktor
		 * setzen zu k�nnen, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	@Override
	public String[] getNames() {
		return names;
	}

	@Override
	protected double calc(final RunElementData data) {
		if (!(data instanceof RunElementDifferentialCounterData)) return 0;
		return ((RunElementDifferentialCounterData)data).getSD();
	}
}
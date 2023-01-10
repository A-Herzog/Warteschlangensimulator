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

import parser.MathCalcError;
import simulator.runmodel.RunDataClient;
import simulator.simparser.coresymbols.CalcSymbolSimData;

/**
 * Liefert die ID der Station, an der der aktuelle Kunde erzeugt wurde oder an der
 * ihm sein aktueller Typ zugewiesen wurde.
 * @author Alexander Herzog
 */
public class CalcSymbolClientSourceStationID extends CalcSymbolSimData {
	/**
	 * Namen f�r das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"KundeQuelleID","ClientSourceID"};

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolClientSourceStationID() {
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
	protected double calc(double[] parameters) throws MathCalcError {
		final RunDataClient client=getCurrentClient();
		if (client==null) return 0;
		return client.sourceStationID;
	}

	@Override
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		final RunDataClient client=getCurrentClient();
		if (client==null) return 0;
		return client.sourceStationID;
	}
}

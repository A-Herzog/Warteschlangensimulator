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

import parser.MathCalcError;
import simulator.runmodel.RunDataClient;
import simulator.simparser.coresymbols.CalcSymbolSimData;

/**
 * Liefert die ID der Station, an der sich der Kunde vor der aktuellen Station aufgehalten hat.
 * @author Alexander Herzog
 */
public class CalcSymbolClientLastStation extends CalcSymbolSimData {
	/**
	 * Namen f�r das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"PreviousStation","VorherigeStation"};

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolClientLastStation() {
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
		if (parameters.length!=0) throw error();

		final RunDataClient client=getCurrentClient();
		if (client==null) throw error();

		return client.lastStationID;
	}

	@Override
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		if (parameters.length!=0) return fallbackValue;

		final RunDataClient client=getCurrentClient();
		if (client==null) return fallbackValue;

		return client.lastStationID;
	}
}

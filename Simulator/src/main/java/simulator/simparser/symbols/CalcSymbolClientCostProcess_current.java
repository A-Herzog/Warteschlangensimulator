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
 * Liefert die Bedienzeitkosten des aktuellen Kunden.
 * @author Alexander Herzog
 */
public class CalcSymbolClientCostProcess_current extends CalcSymbolSimData {
	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"Costs_Process","Kosten_Bedienzeit"};

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolClientCostProcess_current() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	@Override
	public String[] getNames() {
		return names;
	}

	/** Skalierungsfaktor zur Umrechnung von Millisekunden auf Sekunden (um zur Laufzeit eine Division einzusparen) */
	private static final double scaleFactor=1.0/1000.0;

	@Override
	protected double calc(double[] parameters) throws MathCalcError {
		if (parameters.length!=0) throw error();
		final RunDataClient client=getCurrentClient();
		if (client==null) throw error();

		return (client.processTime*scaleFactor)*getSimData().runModel.clientCosts[client.type][2]+client.processAdditionalCosts;
	}
}

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
import simulator.runmodel.SimulationData;
import simulator.simparser.coresymbols.CalcSymbolSimData;

/**
 * Liefert die Wartezeitkosten des aktuellen Kunden.
 * @author Alexander Herzog
 */
public class CalcSymbolClientCostWaiting_current extends CalcSymbolSimData {
	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"Costs_Waiting","Kosten_Wartezeit"};

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolClientCostWaiting_current() {
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
	protected double calc(double[] parameters) throws MathCalcError {
		if (parameters.length!=0) throw error();

		final SimulationData simData=getSimData();
		if (simData==null) return 0.0;

		final RunDataClient client=getCurrentClient();
		if (client==null) throw error();

		return (client.waitingTime*simData.runModel.scaleToSeconds)*simData.runModel.clientCosts[client.type][0]+client.waitingAdditionalCosts;
	}
}

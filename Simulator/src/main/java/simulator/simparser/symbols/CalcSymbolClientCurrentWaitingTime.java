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
 * Liefert die bisherige Wartezeit des aktuellen Kunden an der aktuellen Station.
 * @author Alexander Herzog
 */
public class CalcSymbolClientCurrentWaitingTime extends CalcSymbolSimData {
	/**
	 * Namen f�r das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"CurrentWaitingTime","AktuelleWartezeit"};

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolClientCurrentWaitingTime() {
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

		final SimulationData simData=getSimData();
		if (simData==null) return 0.0;

		final RunDataClient client=getCurrentClient();
		if (client==null) throw error();

		final long waitingTime=(simData.currentTime-client.lastWaitingStart);
		return waitingTime*simData.runModel.scaleToSeconds;
	}

	@Override
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		if (parameters.length!=0) return fallbackValue;

		final SimulationData simData=getSimData();
		if (simData==null) return 0.0;

		final RunDataClient client=getCurrentClient();
		if (client==null) return fallbackValue;

		final long waitingTime=(simData.currentTime-client.lastWaitingStart);

		return waitingTime*simData.runModel.scaleToSeconds;
	}
}

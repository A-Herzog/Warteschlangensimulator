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

import mathtools.distribution.DataDistributionImpl;
import simulator.simparser.coresymbols.CalcSymbolStationDataAllHistogram;
import simulator.statistics.Statistics;

/**
 * Im Falle von zwei Parametern:<br>
 * Liefert den Anteil der Kunden, für den die Wartezeit mehr als timeA (1. Parameter) und höchstens timeB (2. Parameter) Sekunden gedauert hat.<br>
 * Im Falle von einem Parameter:<br>
 * Liefert den Anteil der Kunden, für den die Wartezeit time (1. Parameter) Sekunden gedauert hat.
 * @author Alexander Herzog
 */
public class CalcSymbolStationDataWaiting_histAll extends CalcSymbolStationDataAllHistogram {
	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"Wartezeit_histAll","WaitingTime_histAll"};

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolStationDataWaiting_histAll() {
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
	protected DataDistributionImpl getDistribution(final Statistics statistics) {
		return statistics.clientsAllWaitingTimes.getDistribution();
	}

	@Override
	protected double getDistributionSum(final Statistics statistics) {
		return statistics.clientsAllWaitingTimes.getCount();
	}
}

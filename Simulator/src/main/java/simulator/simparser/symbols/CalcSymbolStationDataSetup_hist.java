/**
 * Copyright 2022 Alexander Herzog
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
import simulator.coreelements.RunElementData;
import simulator.elements.RunElementProcessData;
import simulator.simparser.coresymbols.CalcSymbolStationDataHistogram;

/**
 * Liefert den Anteil der Werte, für den die Rüstzeit an Station id (1. Parameter) time (2. Parameter) Sekunden gedauert hat.
 * @author Alexander Herzog
 */
public class CalcSymbolStationDataSetup_hist extends CalcSymbolStationDataHistogram {
	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"Rüstzeit_hist","SetupTime_hist"};

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolStationDataSetup_hist() {
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
	protected DataDistributionImpl getDistribution(RunElementData data) {
		if (!(data instanceof RunElementProcessData)) return null;
		final RunElementProcessData processData=(RunElementProcessData)data;
		if (processData.setupTimes==null) return null;
		return processData.setupTimes.getDistribution();
	}

	@Override
	protected double getDistributionSum(RunElementData data) {
		if (!(data instanceof RunElementProcessData)) return 0;
		final RunElementProcessData processData=(RunElementProcessData)data;
		if (processData.setupTimes==null) return 0;
		return processData.setupTimes.getSum();
	}
}

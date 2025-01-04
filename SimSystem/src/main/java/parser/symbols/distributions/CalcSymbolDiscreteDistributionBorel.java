/**
 * Copyright 2025 Alexander Herzog
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
package parser.symbols.distributions;

import mathtools.distribution.DiscreteBorelDistributionImpl;

/**
 * Borel-Verteilung
 * @author Alexander Herzog
 * @version 1.0
 */
public class CalcSymbolDiscreteDistributionBorel extends CalcSymbolDiscreteDistribution {
	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"BorelDist","BorelDistribution","BorelVerteilung"};

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolDiscreteDistributionBorel() {
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
	protected int getParameterCount() {
		return 1;
	}

	@Override
	protected double calcProbability(double[] parameters, int k) {
		final double mu=parameters[0];

		if (mu<0 || mu>1) return -1;

		return DiscreteBorelDistributionImpl.calcCountDensity(mu,k);
	}
}

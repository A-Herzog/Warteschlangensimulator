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
package parser.symbols.distributions;

import org.apache.commons.math3.util.FastMath;

/**
 * Diskrete Gleichverteilung
 * @author Alexander Herzog
 * @version 1.0
 */
public class CalcSymbolDiscreteDistributionUniform extends CalcSymbolDiscreteDistribution {
	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolDiscreteDistributionUniform() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"DiscreteUniformDist","DiscreteUniformDistribution","DiskreteGleichverteilung"};

	@Override
	public String[] getNames() {
		return names;
	}

	@Override
	protected int getParameterCount() {
		return 2;
	}

	@Override
	protected double calcProbability(double[] parameters, int k) {
		final int a=(int)FastMath.round(parameters[0]);
		final int b=(int)FastMath.round(parameters[1]);
		if (a>b) return -1;

		if (k<a || k>b) return 0;
		return 1.0/(b-a+1);
	}
}

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

import org.apache.commons.math3.util.FastMath;

/**
 * Binomial-Verteilung
 * @author Alexander Herzog
 * @version 1.0
 */
public class CalcSymbolDiscreteDistributionBoltzmann extends CalcSymbolDiscreteDistribution {
	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolDiscreteDistributionBoltzmann() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"BoltzmannDist","BoltzmannDistribution","BoltzmannDistribution","BoltzmannDist","BoltzmannVerteilung"};

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
		final double lambda=parameters[0];
		final int N=(int)FastMath.round(parameters[1]);

		if (N<1) return -1;
		if (lambda<=0) return -1;

		if (k<0 || k>=N) return 0;
		return (1-Math.exp(-lambda))/(1-Math.exp(-lambda*N))*Math.exp(-lambda*k);
	}
}

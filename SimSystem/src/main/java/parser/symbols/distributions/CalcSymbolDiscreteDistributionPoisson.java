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
package parser.symbols.distributions;

import org.apache.commons.math3.util.FastMath;

import mathtools.Functions;

/**
 * Poisson-Verteilung
 * @author Alexander Herzog
 * @version 1.0
 */
public class CalcSymbolDiscreteDistributionPoisson extends CalcSymbolDiscreteDistribution {
	@Override
	public String[] getNames() {
		return new String[]{"PoissonDistribution","PoissonDist","PoissonVerteilung"};
	}

	@Override
	protected int getParameterCount() {
		return 1;
	}

	@Override
	protected double calcProbability(double[] parameters, int k) {
		final double lambda=parameters[0];

		if (lambda<=0) return -1;

		return FastMath.pow(lambda,k)/Functions.getFactorial(k)*FastMath.exp(-lambda);
	}
}

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

import org.apache.commons.math3.util.CombinatoricsUtils;
import org.apache.commons.math3.util.FastMath;

/**
 * Binomial-Verteilung
 * @author Alexander Herzog
 * @version 1.0
 */
public class CalcSymbolDiscreteDistributionBinomial extends CalcSymbolDiscreteDistribution {
	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"BinomialDistribution","BinomialDist","BinomDistribution","BinomDist","BinomialVerteilung"};

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
		final int n=(int)FastMath.round(parameters[0]);
		final double p=parameters[1];

		if (n<1) return -1;
		if (p<0 || p>1) return -1;

		try {
			return CombinatoricsUtils.binomialCoefficient(n,k)*FastMath.pow(p,k)*FastMath.pow(1-p,n-k);
		} catch (Exception e) {
			return -1;
		}
	}
}

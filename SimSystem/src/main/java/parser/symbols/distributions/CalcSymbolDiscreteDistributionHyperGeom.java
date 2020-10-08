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
 * Hypergeometrische Verteilung
 * @author Alexander Herzog
 * @version 1.0
 */
public class CalcSymbolDiscreteDistributionHyperGeom extends CalcSymbolDiscreteDistribution {
	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"HypergeometricDistribution","HypergeometricDist","HyperDistribution","HyperDist","HgDistribution","HgDist","HypergeometrischeVerteilung"};

	@Override
	public String[] getNames() {
		return names;
	}

	@Override
	protected int getParameterCount() {
		return 3;
	}

	@Override
	protected double calcProbability(double[] parameters, int k) {
		final int N=(int)FastMath.round(parameters[0]);
		final int K=(int)FastMath.round(parameters[1]);
		final int n=(int)FastMath.round(parameters[2]);
		if (N<1) return -1;
		if (K<0 || K>N) return -1;
		if (n<0 || n>N) return -1;

		if (k<0 || k>n || k>K) return 0;

		try {
			return CombinatoricsUtils.binomialCoefficient(K,k)*CombinatoricsUtils.binomialCoefficient(N-K,n-k)/CombinatoricsUtils.binomialCoefficient(N,n);
		} catch (Exception e) {
			return -1;
		}
	}
}

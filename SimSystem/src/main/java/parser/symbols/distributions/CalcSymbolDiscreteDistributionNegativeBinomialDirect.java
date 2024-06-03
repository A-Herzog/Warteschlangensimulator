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

import parser.symbols.CalcSymbolPreOperatorBinomial;

/**
 * Negative Binomial-Verteilung - die Parameter sind hier Erwartungswert und Standardabweichung
 * @author Alexander Herzog
 * @version 1.0
 */
public class CalcSymbolDiscreteDistributionNegativeBinomialDirect extends CalcSymbolDiscreteDistribution {
	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolDiscreteDistributionNegativeBinomialDirect() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"NegativeBinomialDistDirect","NegativeBinomialDistributionDirect","NegativeBinomDistributionDirect","NegativeBinomDistDirect","NegativeBinomialVerteilungDirekt"};

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
		/* E=r(1-p)/p, Var=r(1-p)/p^2 => p=E/Var, r=E*p/(1-p) */

		final double mean=parameters[0];
		if (parameters[1]<0) return -1;
		final double variance=parameters[1]*parameters[1];

		final double p=mean/variance;
		if (p<=0 || p>1) return -1;
		final int r=(int)Math.round(mean*p/(1-p));

		if (r<=0 || p<0 || p>1) return -1;

		try {
			return CalcSymbolPreOperatorBinomial.binomialCoefficient(k+r-1,k)*Math.pow(p,r)*Math.pow(1-p,k);
		} catch (Exception e) {
			return -1;
		}
	}
}

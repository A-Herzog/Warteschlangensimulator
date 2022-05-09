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
 * Negative Binomial-Verteilung
 * @author Alexander Herzog
 */
public class CalcSymbolDiscreteDistributionNegativeBinomial extends CalcSymbolDiscreteDistribution {
	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolDiscreteDistributionNegativeBinomial() {
		/*
		 * Wird nur ben�tigt, um einen JavaDoc-Kommentar f�r diesen (impliziten) Konstruktor
		 * setzen zu k�nnen, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	/**
	 * Namen f�r das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"NegativeBinomialDistribution","NegativeBinomialDist","NegativeBinomDistribution","NegativeBinomDist","NegativeBinomialVerteilung"};

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
		final int r=(int)Math.round(parameters[0]);
		final double p=parameters[1];
		if (r<=0 || p<0 || p>1) return -1;

		try {
			return CalcSymbolPreOperatorBinomial.binomialCoefficient(k+r-1,k)*Math.pow(p,r)*Math.pow(1-p,k);
		} catch (Exception e) {
			return -1;
		}
	}
}

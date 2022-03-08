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

import parser.symbols.CalcSymbolPreOperatorZeta;

/**
 * Zeta-Verteilung
 * @author Alexander Herzog
 */
public class CalcSymbolDiscreteDistributionZeta extends CalcSymbolDiscreteDistribution {
	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"ZetaDistribution","ZetaDist","ZetaDistribution","ZetaDist","ZetaVerteilung"};

	@Override
	public String[] getNames() {
		return names;
	}

	@Override
	protected int getParameterCount() {
		return 1;
	}

	/**
	 * Argument s der letzten Berechnung von zeta(s)
	 * @see #lastZetaS
	 */
	private double lastS=-1;

	/**
	 * Wert zeta(s) der letzten Berechnung
	 * @see #lastS
	 */
	private double lastZetaS=-1;

	@Override
	protected double calcProbability(double[] parameters, int k) {
		final double s=parameters[0];
		if (s<=1) return -1;

		if (k<=0) return 0;

		synchronized(this) {
			if (s!=lastS) {
				lastS=s;
				lastZetaS=CalcSymbolPreOperatorZeta.zeta(s);
			}
		}

		return Math.pow(k,-s)/lastZetaS;
	}
}

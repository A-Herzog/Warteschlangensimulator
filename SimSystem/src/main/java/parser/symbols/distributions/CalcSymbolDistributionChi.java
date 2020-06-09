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

import org.apache.commons.math3.distribution.AbstractRealDistribution;

import mathtools.distribution.ChiDistributionImpl;

/**
 * Chi-Verteilung (<b>nicht</b> Chi^2)
 * @author Alexander Herzog
 * @see ChiDistributionImpl
 */
public final class CalcSymbolDistributionChi extends CalcSymbolDistribution {
	@Override
	public String[] getNames() {
		return new String[]{"ChiDistribution","ChiDist","ChiVerteilung"};
	}

	@Override
	protected int getParameterCount() {
		return 1;
	}

	@Override
	protected AbstractRealDistribution getDistribution(double[] parameters) {
		return new ChiDistributionImpl((int)Math.round(parameters[0]));
	}
}

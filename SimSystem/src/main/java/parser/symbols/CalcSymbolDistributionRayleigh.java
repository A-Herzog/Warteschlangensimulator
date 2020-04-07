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
package parser.symbols;

import org.apache.commons.math3.distribution.AbstractRealDistribution;

import mathtools.distribution.RayleighDistributionImpl;

/**
 * Rayleigh-Verteilung
 * @author Alexander Herzog
 * @see RayleighDistributionImpl
 */
public final class CalcSymbolDistributionRayleigh extends CalcSymbolDistribution {
	@Override
	public String[] getNames() {
		return new String[]{"RayleighDistribution","RayleighDist","Rayleighverteilung"};
	}

	@Override
	protected int getParameterCount() {
		return 1;
	}

	@Override
	protected AbstractRealDistribution getDistribution(double[] parameters) {
		return new RayleighDistributionImpl(parameters[0]);
	}
}

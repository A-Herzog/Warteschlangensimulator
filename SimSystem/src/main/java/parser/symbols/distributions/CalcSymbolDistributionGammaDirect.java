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
import org.apache.commons.math3.distribution.GammaDistribution;

/**
 * Gamma-Verteilung - die Parameter sind hier Erwartungswert und Standardabweichung
 * @author Alexander Herzog
 * @see GammaDistribution
 */
public final class CalcSymbolDistributionGammaDirect extends CalcSymbolDistribution {
	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"GammaDistributionDirect","GammaDistDirect","GammaVerteilungDirekt"};

	@Override
	public String[] getNames() {
		return names;
	}

	@Override
	protected int getParameterCount() {
		return 2;
	}

	@Override
	protected AbstractRealDistribution getDistribution(double[] parameters) {
		final double mean=parameters[0];
		final double sd=parameters[1];
		final double d2=sd*sd/Math.max(mean,0.000001);
		final double d1=mean/Math.max(d2,0.000001);
		return new GammaDistribution(d1,d2);
	}
}

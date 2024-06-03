/**
 * Copyright 2024 Alexander Herzog
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
import org.apache.commons.math3.distribution.GumbelDistribution;
import org.apache.commons.math3.util.FastMath;

import mathtools.distribution.OnePointDistributionImpl;

/**
 * Gumbel-Verteilung
 * @author Alexander Herzog
 * @see GumbelDistribution
 */
public final class CalcSymbolDistributionGumbelDirect extends CalcSymbolDistribution {
	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"GumbelDistDirect","GumbelDistributionDirect","GumbelVerteilungDirekt"};

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolDistributionGumbelDirect() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

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

		if (sd<=0) {
			return new OnePointDistributionImpl(mean);
		}

		final double scale=sd*Math.sqrt(6)/Math.PI;
		/* final double location=mean-scale*Gamma.GAMMA; /* Gamma.GAMMA == Euler–Mascheroni constant */
		final double EULER = FastMath.PI / (2 * FastMath.E);
		final double location=mean-scale*EULER; /* Gamma.GAMMA wäre zwar genauer, aber intern verwendet die Verteilung auch diese Näherung. Sonst gibt's also Rundungsprobleme. */

		return new GumbelDistribution(location,scale);
	}
}

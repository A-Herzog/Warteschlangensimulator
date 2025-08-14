/**
 * Copyright 2025 Alexander Herzog
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

import mathtools.distribution.InverseGammaDistributionImpl;

/**
 * Inverse Gamma-Verteilung - Parameter sind hier Erwartungswert und Standardabweichung
 * @author Alexander Herzog
 * @see InverseGammaDistributionImpl
 */
public final class CalcSymbolDistributionInverseGammaDirect extends CalcSymbolDistribution {
	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"InverseGammaDistDirect","InverseGammaDistributionDirect","InverseGammaVerteilungDirekt"};

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolDistributionInverseGammaDirect() {
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
		if (parameters[0]<0 || parameters[1]<0) return null;

		final double mean=parameters[0];
		final double sd=parameters[1];
		final double alpha=((sd<=0)?0:(mean*mean/sd/sd))+2;
		final double beta=Math.max(((sd<=0)?0:(mean*mean*mean/sd/sd))+mean,0.0001);

		return new InverseGammaDistributionImpl(alpha,beta);
	}
}

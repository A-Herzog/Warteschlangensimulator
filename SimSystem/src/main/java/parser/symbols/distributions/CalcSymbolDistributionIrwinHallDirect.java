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

import mathtools.distribution.IrwinHallDistribution;

/**
 * Irwin-Hall-Verteilung - der Parameter ist hier der Erwartungswert
 * @author Alexander Herzog
 * @see IrwinHallDistribution
 */
public class CalcSymbolDistributionIrwinHallDirect extends CalcSymbolDistribution {
	/**
	 * Namen f�r das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"IrwinHallDistDirect","IrwinHallDistributionDirect","IrwinHallVerteilungDirekt"};

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolDistributionIrwinHallDirect() {
		/*
		 * Wird nur ben�tigt, um einen JavaDoc-Kommentar f�r diesen (impliziten) Konstruktor
		 * setzen zu k�nnen, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	@Override
	public String[] getNames() {
		return names;
	}

	@Override
	protected int getParameterCount() {
		return 1;
	}

	@Override
	protected AbstractRealDistribution getDistribution(double[] parameters) {
		/* mean=n/2 => 2*mean=n */
		return new IrwinHallDistribution(2*parameters[0]);
	}
}

/**
 * Copyright 2021 Alexander Herzog
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

import mathtools.distribution.SawtoothRightDistribution;
import mathtools.distribution.tools.WrapperBetaDistribution;
import mathtools.distribution.tools.WrapperSawtoothRightDistribution;

/**
 * Rechte Sägezahn-Verteilung - die Parameter sind hier Erwartungswert und Standardabweichung
 * @author Alexander Herzog
 * @see SawtoothRightDistribution
 */
public class CalcSymbolDistributionSawtoothRightDirect extends CalcSymbolDistribution {
	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"RightSawtoothDistributionDirect","RightSawtoothDistDirect","RechteSägezahnVerteilungDirekt"};

	@Override
	public String[] getNames() {
		return names;
	}

	@Override
	protected int getParameterCount() {
		return 2;
	}

	/**
	 * Factory-Objekt für die Verteilungen<br>
	 * Die Methode {@link WrapperBetaDistribution#getDistributionForFit(double, double, double, double)}
	 * hat keine Seiteneffekte, kann also problemlos von mehreren Threads parallel aufgerufen werden.
	 */
	private static final WrapperSawtoothRightDistribution wrapper=new WrapperSawtoothRightDistribution();

	@Override
	protected AbstractRealDistribution getDistribution(double[] parameters) {
		return wrapper.getDistribution(parameters[0],parameters[1]);
	}
}

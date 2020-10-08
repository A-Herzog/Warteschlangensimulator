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

import mathtools.distribution.DataDistributionImpl;
import parser.MathCalcError;
import parser.coresymbols.CalcSymbolPreOperator;

/**
 * Liefert einen Dichte-Wert einer empirischen Verteilungsfunktion
 * @author Alexander Herzog
 */
public class CalcSymbolEmpiricalDistributionPDF extends CalcSymbolPreOperator {
	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"EmpirischeDichte","EmpiricalDensity"};

	@Override
	public String[] getNames() {
		return names;
	}

	@Override
	protected double calc(double[] parameters) throws MathCalcError {
		if (parameters.length<3) throw error();
		final double upper=Math.max(0.00001,parameters[parameters.length-1]);
		final double x=parameters[0];
		if (x<0) return 0;
		if (x>upper) return 0;

		final double[] data=new double[parameters.length-2];
		for (int i=1;i<parameters.length-1;i++) data[i-1]=parameters[i];
		final DataDistributionImpl dist=new DataDistributionImpl(upper,data);
		dist.normalizeDensityOnly();
		return dist.density(x);
	}
}

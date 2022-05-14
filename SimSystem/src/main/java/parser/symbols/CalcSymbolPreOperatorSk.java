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
package parser.symbols;

import parser.MathCalcError;
import parser.coresymbols.CalcSymbolPreOperator;

/**
 * Berechnet die Schiefe einer Messreihe<br>
 * <a href="https://de.wikipedia.org/wiki/Schiefe_(Statistik)">https://de.wikipedia.org/wiki/Schiefe_(Statistik)</a>
 * @author Alexander Herzog
 */
public class CalcSymbolPreOperatorSk extends CalcSymbolPreOperator {
	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"Schiefe","Skewness","Sk"};

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolPreOperatorSk() {
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
	protected double calc(double[] parameters) throws MathCalcError {
		if (parameters.length==0) throw error();
		if (parameters.length<3) return 0.0;

		final double n=parameters.length;
		double sum=0, squaredSum=0, cubicSum=0;
		for (double d:parameters) {sum+=d; squaredSum+=d*d; cubicSum+=d*d*d;}
		final double mean=sum/n;
		final double sd=Math.sqrt(Math.max(0,1/(n-1)*(squaredSum-sum*sum/n)));

		return n/(n-1)/(n-2)/Math.pow(sd,3)*(cubicSum-3*mean*squaredSum+2*n*Math.pow(mean,3));
	}

	@Override
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		if (parameters.length==0) return fallbackValue;
		if (parameters.length<3) return 0.0;

		final double n=parameters.length;
		double sum=0, squaredSum=0, cubicSum=0;
		for (double d:parameters) {sum+=d; squaredSum+=d*d; cubicSum+=d*d*d;}
		final double mean=sum/n;
		final double sd=Math.sqrt(Math.max(0,1/(n-1)*(squaredSum-sum*sum/n)));

		return n/(n-1)/(n-2)/Math.pow(sd,3)*(cubicSum-3*mean*squaredSum+2*n*Math.pow(mean,3));
	}
}

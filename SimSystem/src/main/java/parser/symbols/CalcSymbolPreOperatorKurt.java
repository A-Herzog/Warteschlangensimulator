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
 * Berechnet den Exzess (von der Wölbung abgeleitetes Maß) einer Messreihe<br>
 * <a href="https://en.wikipedia.org/wiki/Kurtosis">https://en.wikipedia.org/wiki/Kurtosis</a>
 * @author Alexander Herzog
 */
public class CalcSymbolPreOperatorKurt extends CalcSymbolPreOperator {
	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"Exzess","Kurtosis","Kurt"};

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolPreOperatorKurt() {
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
		if (parameters.length<4) return 0.0;

		final double n=parameters.length;
		double sum=0, squaredSum=0, cubicSum=0, quarticSum=0;
		for (double d:parameters) {sum+=d; squaredSum+=d*d; cubicSum+=d*d*d; quarticSum+=d*d*d*d;}
		final double mean=sum/n;
		final double sd=Math.sqrt(Math.max(0,1/(n-1)*(squaredSum-sum*sum/n)));

		final double normDistComparision=3*Math.pow(n-1,2)/(n-2)/(n-3);
		return n*(n+1)/(n-1)/(n-2)/(n-3)/Math.pow(sd,4)*(quarticSum-4*mean*cubicSum+6*Math.pow(mean,2)*squaredSum-3*n*Math.pow(mean,4))-normDistComparision;
	}

	@Override
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		if (parameters.length==0) return fallbackValue;
		if (parameters.length<4) return 0.0;

		final double n=parameters.length;
		double sum=0, squaredSum=0, cubicSum=0, quarticSum=0;
		for (double d:parameters) {sum+=d; squaredSum+=d*d; cubicSum+=d*d*d; quarticSum+=d*d*d*d;}
		final double mean=sum/n;
		final double sd=Math.sqrt(Math.max(0,1/(n-1)*(squaredSum-sum*sum/n)));

		final double normDistComparision=3*Math.pow(n-1,2)/(n-2)/(n-3);
		return n*(n+1)/(n-1)/(n-2)/(n-3)/Math.pow(sd,4)*(quarticSum-4*mean*cubicSum+6*Math.pow(mean,2)*squaredSum-3*n*Math.pow(mean,4))-normDistComparision;
	}
}

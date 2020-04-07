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

import parser.coresymbols.CalcSymbolPreOperator;

/**
 * Berechnet den Wert der Allen-Cunneen-Näherungsformel.<br>
 * Die Funktion erwartet 6 Parameter:<br>
 * lambda, mu, cvI, cvS, c, mode<br>
 * Mode kann dabei eine Ganzzahl von 1 bis 4 sein:<br>
 * 1: Liefert E[NQ]<br>
 * 2: Liefert E[N]<br>
 * 3: Liefert E[N]<br>
 * 4: Liefert E[V]
 * @author Alexander Herzog
 */
public class CalcSymbolPreOperatorAllenCunneen extends CalcSymbolPreOperator {
	@Override
	public String[] getNames() {
		return new String[]{"AllenCunneen"};
	}

	private double powerFactorial(double a, long c) {
		/* a^c/c! */
		double result=1;
		for (int i=1;i<=c;i++) result*=(a/i);
		return result;
	}

	@Override
	protected Double calc(double[] parameters) {
		if (parameters.length!=6) return null;

		double lambda=parameters[0]; if (lambda<=0) return null;
		double mu=parameters[1]; if (mu<=0) return null;
		double cvI=parameters[2]; if (cvI<=0) return null;
		double cvS=parameters[3]; if (cvS<=0) return null;
		int c=(int)Math.round(parameters[4]); if (c<=0) return null;
		int mode=(int)Math.round(-parameters[5]); if (mode<1 || mode>4) return null;

		double rho=lambda/mu/c;

		double PC1=powerFactorial(c*rho,c)/(1-rho);
		double PC=0; for(int i=0;i<=c-1;i++) PC+=powerFactorial(c*rho,i);
		PC=PC1/(PC1+PC);

		double ENQ=rho/(1-rho)*PC*(cvI*cvI+cvS*cvS)/2;
		double EN=ENQ+c*lambda/mu;
		double EW=ENQ/lambda;
		double EV=EW+1/mu;

		switch (mode) {
		case 1: return ENQ;
		case 2: return EN;
		case 3: return EW;
		case 4: return EV;
		}

		return null;
	}
}

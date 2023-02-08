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

import org.apache.commons.math3.special.Gamma;

import mathtools.ErlangC;
import parser.MathCalcError;
import parser.coresymbols.CalcSymbolPreOperator;

/**
 * Berechnet einen Wert gemäß der erweiterten Erlang-C-Formel.<br>
 * Die Funktion erwartet 6 Parameter:<br>
 * lambda, mu, nu, c, K, mode<br>
 * Mode kann dabei eine Zahl &ge;0 oder eine Ganzzahl von -5 bis -1 sein:<br>
 * Ist mode&ge;0, so wird P(mode) berechnet<br>
 * -1: Liefert E[NQ]<br>
 * -2: Liefert E[N]<br>
 * -3: Liefert E[N]<br>
 * -4: Liefert E[V]<br>
 * -5: Liefert 1-P(A)<br>
 * @author Alexander Herzog
 */
public class CalcSymbolPreOperatorErlangC extends CalcSymbolPreOperator {
	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"ErlangC"};

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolPreOperatorErlangC() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	@Override
	public String[] getNames() {
		return names;
	}

	/**
	 * lambda beim letzten Aufruf von {@link #calc(double[])}
	 */
	private double lastLambda;

	/**
	 * mu beim letzten Aufruf von {@link #calc(double[])}
	 */
	private double lastMu;

	/**
	 * nu beim letzten Aufruf von {@link #calc(double[])}
	 */
	private double lastNu;

	/**
	 * c beim letzten Aufruf von {@link #calc(double[])}
	 */
	private int lastC;

	/**
	 * K beim letzten Aufruf von {@link #calc(double[])}
	 */
	private int lastK;

	/**
	 * t beim letzten Aufruf von {@link #calc(double[])}
	 */
	private double lastT;

	/**
	 * mode beim letzten Aufruf von {@link #calc(double[])}
	 */
	private int lastMode;

	/**
	 * Rechenergebnis beim letzten Aufruf von {@link #calc(double[])}
	 */
	private double lastResult;

	@Override
	protected double calc(double[] parameters) throws MathCalcError {
		if (parameters.length!=6) throw error();

		double lambda=parameters[0]; if (lambda<=0) throw error();
		double mu=parameters[1]; if (mu<=0) throw error();
		double nu=parameters[2]; if (nu<0) nu=0;
		int c=(int)Math.round(parameters[3]); if (c<=0) throw error();
		int K=(int)Math.round(parameters[4]); if (K<=0) K=100000000;
		double t=0;
		int mode;
		if (parameters[5]>=0) {
			mode=0; t=parameters[5];
		} else {
			mode=(int)Math.round(-parameters[5]); if (mode<1 || mode>5) throw error();
		}

		if (lambda==lastLambda && mu==lastMu && nu==lastNu && c==lastC && K==lastK && t==lastT && mode==lastMode) {
			return lastResult;
		}

		double[] Cn=ErlangC.extErlangCCn(lambda,mu,nu,c,K);
		double pi0=0;
		for (int i=0;i<Cn.length;i++) pi0+=Cn[i];
		pi0=1/pi0;

		double Pt;
		if (pi0==0) Pt=1; else Pt=1-Cn[K]*pi0;
		for (int n=c;n<=K-1;n++) {
			Double g=Gamma.regularizedGammaQ(n-c+1,(c*mu+nu)*t);
			Pt-=pi0*Cn[n]*g;
		}
		if (Double.isNaN(Pt) || Pt<0) Pt=0;

		double ENQ=0; for (int i=c+1;i<Cn.length;i++) ENQ+=(i-c)*Cn[i]*pi0;
		double EN=0; for (int i=1;i<Cn.length;i++) EN+=i*Cn[i]*pi0;
		double EW=ENQ/lambda;
		double EV=EW+1/mu;
		double PA=ENQ*nu/lambda;

		switch (mode) {
		case 0:
			lastLambda=lambda;
			lastMu=mu;
			lastNu=nu;
			lastC=c;
			lastK=K;
			lastT=t;
			lastMode=mode;
			return lastResult=Pt;
		case 1:
			lastLambda=lambda;
			lastMu=mu;
			lastNu=nu;
			lastC=c;
			lastK=K;
			lastT=t;
			lastMode=mode;
			return lastResult=ENQ;
		case 2:
			lastLambda=lambda;
			lastMu=mu;
			lastNu=nu;
			lastC=c;
			lastK=K;
			lastT=t;
			lastMode=mode;
			return lastResult=EN;
		case 3:
			lastLambda=lambda;
			lastMu=mu;
			lastNu=nu;
			lastC=c;
			lastK=K;
			lastT=t;
			lastMode=mode;
			return lastResult=EW;
		case 4:
			lastLambda=lambda;
			lastMu=mu;
			lastNu=nu;
			lastC=c;
			lastK=K;
			lastT=t;
			lastMode=mode;
			return lastResult=EV;
		case 5:
			lastLambda=lambda;
			lastMu=mu;
			lastNu=nu;
			lastC=c;
			lastK=K;
			lastT=t;
			lastMode=mode;
			return lastResult=1-PA;
		default:
			throw error();
		}
	}
}

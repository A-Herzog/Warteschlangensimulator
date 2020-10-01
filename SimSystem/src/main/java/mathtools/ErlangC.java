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
package mathtools;

import org.apache.commons.math3.util.CombinatoricsUtils;
import org.apache.commons.math3.util.FastMath;

/**
 * Bestimmt die mittlere Wartezeit in einem M/M/c/infty oder M/M/c/K+M Warteschlangenmodell gemäß
 * der Erlang C Formel.
 * @author Alexander Herzog
 * @version 1.1
 */
public final class ErlangC {
	/**
	 * Diese Klasse kann nicht instanziert werden.
	 */
	private ErlangC() {
	}

	/**
	 * Liefert die mittlere Wartezeit in einem M/M/c/infty Modell gemäß der Erlang C Formel.
	 * @param lambda	Ankunftsrate
	 * @param mu	Bedienrate
	 * @param c	Anzahl an Bedienern
	 * @return	Mittlere Wartezeit (in derselben Einheit, in der 1/lambda und 1/mu übergeben wurden)
	 */
	public static double waitingTime(final double lambda, final double mu, final int c) {
		if (mu<=0) return Double.POSITIVE_INFINITY;
		double a=lambda/mu;
		double p1=FastMath.pow(a,c)/CombinatoricsUtils.factorial(c)*c/(c-a);

		double s=1;
		for (int n=1;n<=c-1;n++) s+=FastMath.pow(a,n)/CombinatoricsUtils.factorial(n);
		p1=p1/(s+p1);

		return p1/(c*mu-lambda);
	}

	/**
	 * Berechnet C[n] für ein M/M/c/K+M Modell gemäß der Erlang C Formel.<br>
	 * Der Parameter K ist dabei die Systemgröße, d.h. die Summe aus Warteraumgröße und Anzahl an Agenten,
	 * also insbesondere nicht nur die Warteraumgröße.
	 * @param lambda	Ankunftsrate
	 * @param mu	Bedienrate
	 * @param nu	Warteabbruchrate
	 * @param c	Anzahl an Bedienern
	 * @param K	Systemgröße (=Summe aus c und Warteraumgröße)
	 * @return	Array der Länge K+1 mit den Werten für C[n]
	 */
	public static double[] extErlangCCn(final double lambda, final double mu, final double nu, final int c, int K) { /* M/M/c/K+M (K=System size=c+Waiting room size */
		double a;
		if (lambda<=0 || mu<=0) a=0; else a=lambda/mu;

		if (K==Integer.MAX_VALUE) K=Math.min(100*c,5000);

		final double[] Cn=new double[K+1];

		for (int n=0;n<=Math.min(c,K);n++) {
			if (n<=10) {
				Cn[n]=FastMath.pow(a,n)/CombinatoricsUtils.factorial(n);
			} else {
				if (n==11) {
					double m=1; for (int i=1;i<=n;i++) m*=a/i;
					Cn[n]=m;
				} else {
					Cn[n]=Cn[n-1]*a/n;
				}
			}
		}
		double temp=1;
		if (c<=10) {
			temp=FastMath.pow(a,c)/CombinatoricsUtils.factorial(c);
		} else {
			for (int i=1;i<=c;i++) temp*=a/i;
		}
		if (a>0) for (int n=c+1;n<=K;n++) {
			if (Cn[n-1]<=10E-20) {Cn[n]=0; continue;}
			double p=1; for (int i=1;i<=n-c;i++) p*=a/(c+i*nu/mu);
			Cn[n]=temp*p;
		}

		return Cn;
	}

	/**
	 * Liefert die mittlere Wartezeit in einem M/M/c/K+M Modell gemäß der Erlang C Formel.<br>
	 * Der Parameter K ist dabei die Systemgröße, d.h. die Summe aus Warteraumgröße und Anzahl an Agenten,
	 * also insbesondere nicht nur die Warteraumgröße.
	 * @param lambda	Ankunftsrate
	 * @param mu	Bedienrate
	 * @param nu	Warteabbruchrate
	 * @param c	Anzahl an Bedienern
	 * @param K	Systemgröße (=Summe aus c und Warteraumgröße)
	 * @return	Mittlere Wartezeit (in derselben Einheit, in der 1/lambda, 1/mu und 1/nu übergeben wurden)
	 */
	public static double waitingTimeExt(double lambda, double mu, double nu, int c, int K) { /* M/M/c/K+M (K=System size=c+Waiting room size */
		if (lambda<=0.0) return 0.0;
		final double[] Cn=extErlangCCn(lambda,mu,nu,c,K);
		K=Cn.length-1;

		double pi0=0; for (int n=0;n<=K;n++) pi0+=Cn[n];
		if (pi0==0.0) return 0.0;
		pi0=1/pi0;

		double s=0;
		for (int n=c+1;n<=K;n++) s+=(n-c)*Cn[n]*pi0;
		return s/(lambda*(1-Cn[K]*pi0));
	}

}

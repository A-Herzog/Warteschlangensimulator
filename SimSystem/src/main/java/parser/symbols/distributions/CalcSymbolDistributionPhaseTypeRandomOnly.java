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

import parser.MathCalcError;
import parser.coresymbols.CalcSymbolPreOperator;

/**
 * Erzeugt eine Zufallszahl gemäß der Phasentyp-Verteilung<br>
 * Beispiel für Aufruf: PhaseTypeDistRandom(-2;1.0;0;-3;1.0;0.0)<br>
 * Erst s (n*n), dann alpha (n)
 */
public class CalcSymbolDistributionPhaseTypeRandomOnly extends CalcSymbolPreOperator {
	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolDistributionPhaseTypeRandomOnly() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}
	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"PhaseTypeDistRandom", "PhaseTypeDistributionRandom"};

	@Override
	public String[] getNames() {
		return names;
	}

	/**
	 * Größe der Matrix beim letzten Aufruf der Funktion
	 */
	private int lastN=-1;

	/**
	 * Parameter s
	 */
	private double[][] s;

	/**
	 * Parameter alpha
	 */
	private double[] alpha;

	/**
	 * Hilfsgröße: initial Verteilungsfunktion
	 */
	private double[] cumInitial;

	/**
	 * Hilfsgröße: Verweilzeiten
	 */
	private double[] sojourn;

	/**
	 * Hilfsgröße: nextpr
	 */
	private double[][] nextpr;

	@Override
	protected double calc(final double[] parameters) throws MathCalcError {
		/* Parameter: s[n*n], alpha[n] */

		/* n=(sqrt(4*len+1)-1)/2 */
		final double nFloat=(Math.sqrt(4*parameters.length+1)-1)/2.0;
		if (Math.abs(nFloat%1.0)>0.0001) throw error();
		final int n=(int)nFloat;

		/* s */
		if (lastN!=n) s=new double[n][];
		for (int i=0;i<n;i++) {
			final double[] row;
			if (lastN!=n) {
				s[i]=row=new double[n];
			} else {
				row=s[i];
			}
			for (int j=0;j<n;j++) row[j]=parameters[i*n+j];
		}

		/* alpha */
		if (lastN!=n) alpha=new double[n];
		for (int i=0;i<n;i++) alpha[i]=parameters[n*n+i];

		lastN=n;

		/* Alle Einträge von alpha müssen nicht-negative sein */
		for (int i=0;i<n;i++) if (alpha[i]<0) throw error();

		/* Summe über alpha muss 1 sein */
		double sum=0;
		for (int i=0;i<n;i++) sum+=alpha[i];
		if (Math.abs(sum-1.0)>1E-10) throw error();

		for (int i=0;i<n;i++) {
			/* Hauptdiagonaleinträge müssen negativ sein */
			if (s[i][i]>=0) throw error();
			/* Nicht-Hauptdiagonaleinträge müssen nicht-negativ sein */
			for (int j=0;j<n;j++) if (i!=j && s[i][j]<0) throw error();
			/* Zeilensummen müssen kleiner oder gleich 0 sein */
			sum=0;
			for (int j=0;j<n;j++) sum+=s[i][j];
			if (sum>0) throw error();
		}

		/* Verteilungsfunktion vorab berechnen */
		if (cumInitial==null || cumInitial.length!=n) cumInitial=new double[n];
		sum=0;
		for (int i=0;i<n;i++) {
			sum+=alpha[i];
			cumInitial[i]=sum;
		}

		/* Verweilzeiten vorab berechnen */
		if (sojourn==null || sojourn.length!=n) sojourn=new double[n];
		for (int i=0;i<n;i++) sojourn[i]=-1/s[i][i];

		/* Nextpr */
		if (nextpr==null || nextpr.length!=n) nextpr=new double[n][];
		for (int i=0;i<n;i++) {
			final double[] row;
			if (nextpr[i]==null) {
				row=new double[n+1]; /* Die Zeilen sind n+1 Einträge lang! */
				nextpr[i]=row;
			} else {
				row=nextpr[i];
			}
			for (int j=0;j<n;j++) row[j]=s[i][j]*sojourn[i];
		}
		for (int i=0;i<n;i++) nextpr[i][i]=0;
		for (int i=0;i<n;i++) {
			sum=1;
			for (int j=0;j<n;j++) sum-=nextpr[i][j];
			nextpr[i][n]=sum; /* n+1-ter Eintrag pro Zeile */
		}
		for (int i=0;i<n;i++) {
			sum=0;
			for (int j=0;j<n+1;j++) {
				sum+=nextpr[i][j];
				nextpr[i][j]=sum;
			}
		}

		/*
		Tests:
		System.out.println("s=["+s[0][0]+";"+s[0][1]+"];["+s[1][0]+";"+s[1][1]+"]");
		System.out.println("alpha=["+alpha[0]+";"+alpha[1]+"]");
		System.out.println("cumInitial=["+cumInitial[0]+";"+cumInitial[1]+"]");
		System.out.println("sojourn=["+sojourn[0]+";"+sojourn[1]+"]");
		System.out.println("nextpr=["+nextpr[0][0]+";"+nextpr[0][1]+";"+nextpr[0][2]+"];["+nextpr[1][0]+";"+nextpr[1][1]+";"+nextpr[1][2]+"]");
		 */

		/* Startverteilung erstellen */
		int state=0;
		double r=calcSystem.getRandomDouble();
		while (cumInitial[state]<=r) state+=1;

		/* Zustandsübergänge durchführen */
		double time=0;
		while (state<n) {
			time+=-Math.log(calcSystem.getRandomDouble())*sojourn[state];
			r=calcSystem.getRandomDouble();
			int nstate=0;
			while (nextpr[state][nstate]<=r) nstate+=1;
			state=nstate;
		}

		return time;
	}

	@Override
	protected boolean isDeterministic() {
		return false;
	}
}

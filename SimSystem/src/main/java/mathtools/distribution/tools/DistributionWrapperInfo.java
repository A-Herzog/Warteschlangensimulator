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
package mathtools.distribution.tools;

import org.apache.commons.math3.distribution.AbstractRealDistribution;

import mathtools.NumberTools;

/**
 * Liefert zusätzliche Daten für eine Verteilung
 * @author Alexander Herzog
 * @see AbstractDistributionWrapper#getInfo(AbstractRealDistribution)
 */
public class DistributionWrapperInfo {
	/**
	 * Erwartungswert (kann <code>null</code> sein, wenn nicht verfügbar)
	 */
	public final Double E;

	/**
	 * Standardabweichung (kann <code>null</code> sein, wenn nicht verfügbar)
	 */
	public final Double Std;

	/**
	 * Schiefe (kann <code>null</code> sein, wenn nicht verfügbar)
	 */
	public final Double Sk;

	/**
	 * Modalwert (kann <code>null</code> sein, wenn nicht verfügbar)
	 */
	public final Double mode;

	/**
	 * Vorangestellte Informationen (kann <code>null</code> sein)
	 */
	public final String info1;

	/**
	 * Nachgelagerte Informationen (kann <code>null</code> sein)
	 */
	public final String info2;

	/**
	 * Konstruktor der Klasse
	 * @param E	Erwartungswert (kann <code>null</code> sein, wenn nicht verfügbar)
	 * @param Std	Standardabweichung (kann <code>null</code> sein, wenn nicht verfügbar)
	 * @param Sk	Schiefe (kann <code>null</code> sein, wenn nicht verfügbar)
	 * @param mode	Modalwert (kann <code>null</code> sein, wenn nicht verfügbar)
	 * @param info1	Vorangestellte Informationen (kann <code>null</code> sein)
	 * @param info2	Nachgelagerte Informationen (kann <code>null</code> sein)
	 */
	public DistributionWrapperInfo(final Double E, final Double Std, final Double Sk, final Double mode, final String info1, final String info2) {
		this.E=E;
		this.Std=Std;
		this.Sk=Sk;
		this.mode=mode;
		this.info1=info1;
		this.info2=info2;
	}

	/**
	 * Konstruktor der Klasse
	 * @param distribution	Verteilung aus der Erwartungswert und Standardabweichung direkt ausgelesen werden
	 * @param Sk	Schiefe (kann <code>null</code> sein, wenn nicht verfügbar)
	 * @param mode	Modalwert (kann <code>null</code> sein, wenn nicht verfügbar)
	 * @param info1	Vorangestellte Informationen (kann <code>null</code> sein)
	 * @param info2	Nachgelagerte Informationen (kann <code>null</code> sein)
	 */
	public DistributionWrapperInfo(final AbstractRealDistribution distribution, final Double Sk, final Double mode, final String info1, final String info2) {
		if (distribution!=null) {
			E=distribution.getNumericalMean();
			Std=Math.sqrt(distribution.getNumericalVariance());
		} else {
			E=null;
			Std=null;
		}
		this.Sk=Sk;
		this.mode=mode;
		this.info1=info1;
		this.info2=info2;
	}

	/**
	 * Konstruktor der Klasse
	 * @param distribution	Verteilung aus der Erwartungswert und Standardabweichung direkt ausgelesen werden
	 * @param Sk	Schiefe (kann <code>null</code> sein, wenn nicht verfügbar)
	 * @param mode	Modalwert (kann <code>null</code> sein, wenn nicht verfügbar)
	 */
	public DistributionWrapperInfo(final AbstractRealDistribution distribution, final Double Sk, final Double mode) {
		E=distribution.getNumericalMean();
		Std=Math.sqrt(distribution.getNumericalVariance());
		this.Sk=Sk;
		this.mode=mode;
		info1=null;
		info2=null;
	}

	/**
	 * Unicode-Symbol für "Unendlich"
	 */
	private static final String infinity=Character.toString((char)0x221E);

	/**
	 * Liefert die Kenngrößen bzw. Parameter einer Verteilung
	 * @return	Kenngrößen bzw. Parameter einer Verteilung
	 */
	public String getShortInfo() {
		final StringBuilder result=new StringBuilder();

		if (info1!=null) result.append(info1);
		if (E!=null) {
			if (result.length()>0) result.append("; ");
			result.append("E="+(Double.isInfinite(E)?infinity:NumberTools.formatNumber(E,3)));
		}
		if (Std!=null && !Double.isNaN(Std)) {
			if (result.length()>0) result.append("; ");
			result.append("Std="+(Double.isInfinite(Std)?infinity:NumberTools.formatNumber(Std,3)));
		}
		if (E!=null && Std!=null && E>0 && !Double.isNaN(E) && !Double.isNaN(Std) && !Double.isInfinite(E) && !Double.isInfinite(Std)) {
			if (result.length()>0) result.append("; ");
			result.append("CV="+NumberTools.formatNumber(Std/E,3));
		}
		if (Sk!=null && !Double.isNaN(Sk)) {
			if (result.length()>0) result.append("; ");
			result.append("Sk="+(Double.isInfinite(Sk)?infinity:NumberTools.formatNumber(Sk,3)));
		}
		if (mode!=null && !Double.isNaN(mode)) {
			if (result.length()>0) result.append("; ");
			result.append(DistributionTools.DistMode+"="+(Double.isInfinite(mode)?infinity:NumberTools.formatNumber(mode,3)));
		}
		if (info2!=null) {
			if (result.length()>0) result.append("; ");
			result.append(info2);
		}

		return result.toString();
	}

	/**
	 * Liefert die wesentlichen Kenngrößen bzw. Parameter einer Verteilung
	 * @return	Wesentliche Kenngrößen bzw. Parameter einer Verteilung
	 */
	public String getVeryShortInfo() {
		final StringBuilder result=new StringBuilder();

		if (info1!=null) result.append(info1);
		if (E!=null) {
			if (result.length()>0) result.append("; ");
			result.append("E="+(Double.isInfinite(E)?infinity:NumberTools.formatNumber(E,3)));
		}
		if (Std!=null && !Double.isNaN(Std)) {
			if (result.length()>0) result.append("; ");
			result.append("Std="+(Double.isInfinite(Std)?infinity:NumberTools.formatNumber(Std,3)));
		}
		if (E!=null && Std!=null && E>0 && !Double.isNaN(E) && !Double.isNaN(Std) && !Double.isInfinite(E) && !Double.isInfinite(Std)) {
			if (result.length()>0) result.append("; ");
			result.append("CV="+NumberTools.formatNumber(Std/E,3));
		}
		if (info2!=null) {
			if (result.length()>0) result.append("; ");
			result.append(info2);
		}

		return result.toString();
	}

	/**
	 * Liefert die Kenngrößen bzw. Parameter einer Verteilung (in ausgeschriebener Form)
	 * @return	Kenngrößen bzw. Parameter einer Verteilung (in ausgeschriebener Form)
	 */
	public String getLongInfo() {
		final StringBuilder result=new StringBuilder();

		if (info1!=null) result.append(info1);
		if (E!=null) {
			if (result.length()>0) result.append("; ");
			result.append(DistributionTools.DistMean+" E="+NumberTools.formatNumber(E,4));
		}
		if (Std!=null) {
			if (result.length()>0) result.append("; ");
			result.append(DistributionTools.DistStdDev+" Std="+NumberTools.formatNumber(Std,4));
		}
		if (E!=null && Std!=null && E>0) {
			if (result.length()>0) result.append("; ");
			result.append(DistributionTools.DistCV+" CV="+NumberTools.formatNumber(Std/E,4));
		}
		if (Sk!=null) {
			if (result.length()>0) result.append("; ");
			result.append(DistributionTools.DistSkewness+" Sk="+NumberTools.formatNumber(Sk,4));
		}
		if (info2!=null) {
			if (result.length()>0) result.append("; ");
			result.append(info2);
		}

		return result.toString();

	}
}

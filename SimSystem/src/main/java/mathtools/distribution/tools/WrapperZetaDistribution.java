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
package mathtools.distribution.tools;

import org.apache.commons.math3.distribution.AbstractRealDistribution;

import mathtools.NumberTools;
import mathtools.distribution.DiscreteZetaDistributionImpl;
import parser.coresymbols.CalcSymbolPreOperator;
import parser.symbols.CalcSymbolPreOperatorZeta;
import parser.symbols.distributions.CalcSymbolDiscreteDistributionZeta;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link DiscreteZetaDistributionImpl}
 * @author Alexander Herzog
 * @see DiscreteZetaDistributionImpl
 * @see DistributionTools
 */
public class WrapperZetaDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperZetaDistribution() {
		super(DiscreteZetaDistributionImpl.class);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistZeta;
	}

	@Override
	protected String getThumbnailImageName() {
		return "discrete.png";
	}

	@Override
	protected String getWikipediaURL() {
		return DistributionTools.DistZetaWikipedia;
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(AbstractRealDistribution distribution) {
		final DiscreteZetaDistributionImpl dist=(DiscreteZetaDistributionImpl)distribution;

		final Double E;
		final Double std;
		if (dist.s>2) {
			E=dist.mean;
			if (dist.s>3) {
				std=Math.sqrt(dist.variance);
			} else {
				std=null;
			}
		} else {
			E=null;
			std=null;
		}
		final String info="s="+NumberTools.formatNumber(dist.s,3);
		return new DistributionWrapperInfo(E,std,null,1.0,info,null); /* immer Modus=1.0 */
	}

	/**
	 * Berechnet den Erwartungswert der Zeta-Verteilung basierend auf dem Verteilungsparameter s.
	 * @param s	Verteilungsparameter s
	 * @return	Erwartungswert der Zeta-Verteilung
	 */
	private double getMeanFromS(final double s) {
		/* mean=zeta(s-1)/zeta(s) */
		return CalcSymbolPreOperatorZeta.zeta(s-1)/CalcSymbolPreOperatorZeta.zeta(s);
	}

	@Override
	public AbstractRealDistribution getDistribution(double mean, double sd) {
		if (mean<=1 || mean>=2.24) return null;

		double sMin=2.4; /* Für kleinere Werte wird die Berechnung der Zeta-Funktion arg ungenau */
		double sMax=50;

		while (sMax-sMin>0.01) {
			final double sMiddle=(sMin+sMax)/2;
			final double sMiddleMean=getMeanFromS(sMiddle);
			if (sMiddleMean>mean) {
				sMin=sMiddle;
			} else {
				sMax=sMiddle;
			}
		}

		return new DiscreteZetaDistributionImpl((sMin+sMax)/2);
	}

	@Override
	public AbstractRealDistribution getDefaultDistribution() {
		return new DiscreteZetaDistributionImpl(3);
	}

	@Override
	protected AbstractRealDistribution setMeanInt(AbstractRealDistribution distribution, double mean) {
		return null;
	}

	@Override
	protected AbstractRealDistribution setStandardDeviationInt(AbstractRealDistribution distribution, double sd) {
		return null;
	}

	@Override
	protected double getParameterInt(AbstractRealDistribution distribution, int nr) {
		if (nr==1) return ((DiscreteZetaDistributionImpl)distribution).s;
		return 0.0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(AbstractRealDistribution distribution, int nr, double value) {
		if (nr==1) return new DiscreteZetaDistributionImpl(value);
		return null;
	}

	@Override
	protected String getToStringData(AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(((DiscreteZetaDistributionImpl)distribution).s);
	}

	@Override
	public AbstractRealDistribution fromString(String data, double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=1) return null;
		return new DiscreteZetaDistributionImpl(values[0]);

	}

	@Override
	protected AbstractRealDistribution cloneInt(AbstractRealDistribution distribution) {
		return new DiscreteZetaDistributionImpl((DiscreteZetaDistributionImpl)distribution);
	}

	@Override
	protected boolean compareInt(AbstractRealDistribution distribution1, AbstractRealDistribution distribution2) {
		if (Math.abs(((DiscreteZetaDistributionImpl)distribution1).s-((DiscreteZetaDistributionImpl)distribution2).s)>DistributionTools.MAX_ERROR) return false;
		return true;
	}

	@Override
	protected Class<? extends CalcSymbolPreOperator> getCalcSymbolClass() {
		return CalcSymbolDiscreteDistributionZeta.class;
	}
}

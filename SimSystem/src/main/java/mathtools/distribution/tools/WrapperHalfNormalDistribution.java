/**
 * Copyright 2023 Alexander Herzog
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
import mathtools.distribution.HalfNormalDistribution;
import parser.coresymbols.CalcSymbolPreOperator;
import parser.symbols.distributions.CalcSymbolDistributionHalfNormal;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link HalfNormalDistribution}
 * @author Alexander Herzog
 * @see HalfNormalDistribution
 * @see DistributionTools
 */
public class WrapperHalfNormalDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperHalfNormalDistribution() {
		super(HalfNormalDistribution.class,true,false);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistHalfNormal;
	}

	@Override
	protected String getThumbnailImageName() {
		return "halfnormal.png";
	}

	@Override
	protected String getWikipediaURL() {
		return DistributionTools.DistHalfNormalWikipedia;
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(AbstractRealDistribution distribution) {
		final HalfNormalDistribution dist=(HalfNormalDistribution)distribution;
		return new DistributionWrapperInfo(distribution,dist.getSkewness(),dist.getMode());
	}

	@Override
	public AbstractRealDistribution getDistribution(double mean, double sd) {
		return new HalfNormalDistribution(mean);
	}

	@Override
	public AbstractRealDistribution getDefaultDistribution() {
		return new HalfNormalDistribution(100);
	}

	@Override
	public AbstractRealDistribution getDistributionForFit(final double mean, final double sd, final double min, final double max) {
		if (mean<=0) return null;
		return super.getDistributionForFit(mean,sd,min,max);
	}

	@Override
	protected AbstractRealDistribution setMeanInt(AbstractRealDistribution distribution, double mean) {
		return new HalfNormalDistribution(mean);
	}

	/**
	 * Berechnet aus der Standardabweichung der halben Normalverteiltung den Erwartungswert.
	 * @param sd	Standardabweichung
	 * @return	Standardabweichung
	 */
	public static double sdToMean(double sd) {
		/* Var=(pi-2)/(2*theta^2) */
		sd=Math.max(sd,0.0001);
		final double theta=Math.sqrt((Math.PI-2)/(sd*sd)/2);
		return 1/theta;
	}

	@Override
	protected AbstractRealDistribution setStandardDeviationInt(AbstractRealDistribution distribution, double sd) {
		return new HalfNormalDistribution(sdToMean(sd));
	}

	@Override
	protected double getParameterInt(AbstractRealDistribution distribution, int nr) {
		if (nr==1) return ((HalfNormalDistribution)distribution).mean;
		return 0.0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(AbstractRealDistribution distribution, int nr, double value) {
		if (nr==1) return new HalfNormalDistribution(value);
		return null;
	}

	@Override
	protected String getToStringData(AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(((HalfNormalDistribution)distribution).mean);
	}

	@Override
	public AbstractRealDistribution fromString(String data, double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=1) return null;
		return new HalfNormalDistribution(values[0]);
	}

	@Override
	protected AbstractRealDistribution cloneInt(AbstractRealDistribution distribution) {
		return ((HalfNormalDistribution)distribution).clone();
	}

	@Override
	protected boolean compareInt(AbstractRealDistribution distribution1, AbstractRealDistribution distribution2) {
		if (Math.abs(((HalfNormalDistribution)distribution1).mean-((HalfNormalDistribution)distribution2).mean)>DistributionTools.MAX_ERROR) return false;
		return true;
	}

	@Override
	protected Class<? extends CalcSymbolPreOperator> getCalcSymbolClass() {
		return CalcSymbolDistributionHalfNormal.class;
	}
}

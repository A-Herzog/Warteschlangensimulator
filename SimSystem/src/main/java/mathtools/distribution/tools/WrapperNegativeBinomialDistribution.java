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
import mathtools.distribution.DiscreteNegativeBinomialDistributionImpl;
import parser.coresymbols.CalcSymbolPreOperator;
import parser.symbols.distributions.CalcSymbolDiscreteDistributionNegativeBinomial;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link DiscreteNegativeBinomialDistributionImpl}
 * @author Alexander Herzog
 * @see DiscreteNegativeBinomialDistributionImpl
 * @see DistributionTools
 */
public class WrapperNegativeBinomialDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperNegativeBinomialDistribution() {
		super(DiscreteNegativeBinomialDistributionImpl.class,true,true);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistNegativeBinomial;
	}

	@Override
	protected String getThumbnailImageName() {
		return "discrete.png";
	}

	@Override
	protected String getWikipediaURL() {
		return DistributionTools.DistNegativeBinomialWikipedia;
	}

	@Override
	protected String getWebAppDistributionName() {
		return "NegativeBinomial";
	}

	@Override
	protected String getInfoHTML() {
		return null;
	}

	@Override
	protected boolean canSetMeanExact() {
		return false;
	}

	@Override
	protected boolean canSetStandardDeviationExact() {
		return false;
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(AbstractRealDistribution distribution) {
		final DiscreteNegativeBinomialDistributionImpl dist=(DiscreteNegativeBinomialDistributionImpl)distribution;
		final String info="p="+NumberTools.formatNumber(dist.p,3)+"; r="+dist.r;
		final double sk=dist.getSkewness();
		return new DistributionWrapperInfo(distribution,sk,dist.getMode(),info,null);
	}

	@Override
	public AbstractRealDistribution getDistribution(double mean, double sd) {
		if (mean<=0 || sd<=0) return null;
		/* E=r(1-p)/p, Var=r(1-p)/p^2 => p=E/Var, r=E*p/(1-p) */
		final double p=mean/(sd*sd);
		if (p<=0 || p>1) return null;
		final int r=(int)Math.round(mean*p/(1-p));
		return new DiscreteNegativeBinomialDistributionImpl(p,r);
	}

	@Override
	public AbstractRealDistribution getDefaultDistribution() {
		return new DiscreteNegativeBinomialDistributionImpl(0.5,10);
	}

	@Override
	protected AbstractRealDistribution setMeanInt(AbstractRealDistribution distribution, double mean) {
		if (mean<=0) return null;
		/* E=r(1-p)/p, Var=r(1-p)/p^2 => p=E/Var, r=E*p/(1-p) */
		final double variance=((DiscreteNegativeBinomialDistributionImpl)distribution).getNumericalVariance();
		final double p=mean/variance;
		if (p<=0 || p>1) return null;
		final int r=(int)Math.round(mean*p/(1-p));
		return new DiscreteNegativeBinomialDistributionImpl(p,r);
	}

	@Override
	protected AbstractRealDistribution setStandardDeviationInt(AbstractRealDistribution distribution, double sd) {
		if (sd<=0) return null;
		/* E=r(1-p)/p, Var=r(1-p)/p^2 => p=E/Var, r=E*p/(1-p) */
		final double mean=((DiscreteNegativeBinomialDistributionImpl)distribution).getNumericalMean();
		final double p=mean/(sd*sd);
		if (p<=0 || p>1) return null;
		final int r=(int)Math.round(mean*p/(1-p));
		return new DiscreteNegativeBinomialDistributionImpl(p,r);
	}

	@Override
	protected double getParameterInt(AbstractRealDistribution distribution, int nr) {
		if (nr==1) return ((DiscreteNegativeBinomialDistributionImpl)distribution).p;
		if (nr==2) return ((DiscreteNegativeBinomialDistributionImpl)distribution).r;
		return 0.0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(AbstractRealDistribution distribution, int nr, double value) {
		if (nr==1) return new DiscreteNegativeBinomialDistributionImpl(value,((DiscreteNegativeBinomialDistributionImpl)distribution).r);
		if (nr==2) return new DiscreteNegativeBinomialDistributionImpl(((DiscreteNegativeBinomialDistributionImpl)distribution).p,(int)Math.round(value));
		return null;
	}

	@Override
	protected String getToStringData(AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(((DiscreteNegativeBinomialDistributionImpl)distribution).p)+";"+((DiscreteNegativeBinomialDistributionImpl)distribution).r;
	}

	@Override
	public AbstractRealDistribution fromString(String data, double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=2) return null;
		return new DiscreteNegativeBinomialDistributionImpl(values[0],(int)Math.round(values[1]));
	}

	@Override
	protected AbstractRealDistribution cloneInt(AbstractRealDistribution distribution) {
		return new DiscreteNegativeBinomialDistributionImpl((DiscreteNegativeBinomialDistributionImpl)distribution);
	}

	@Override
	protected boolean compareInt(AbstractRealDistribution distribution1, AbstractRealDistribution distribution2) {
		if (Math.abs(((DiscreteNegativeBinomialDistributionImpl)distribution1).p-((DiscreteNegativeBinomialDistributionImpl)distribution2).p)>DistributionTools.MAX_ERROR) return false;
		if (((DiscreteNegativeBinomialDistributionImpl)distribution1).r!=((DiscreteNegativeBinomialDistributionImpl)distribution2).r) return false;
		return true;
	}

	@Override
	protected int[] getDistributionParameterIndicesForCalculationExpression(final int parameterCount) {
		return new int[] {2,1};
	}

	@Override
	protected Class<? extends CalcSymbolPreOperator> getCalcSymbolClass() {
		return CalcSymbolDiscreteDistributionNegativeBinomial.class;
	}
}

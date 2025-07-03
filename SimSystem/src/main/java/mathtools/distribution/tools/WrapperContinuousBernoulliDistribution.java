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
package mathtools.distribution.tools;

import org.apache.commons.math3.distribution.AbstractRealDistribution;

import mathtools.NumberTools;
import mathtools.distribution.ContinuousBernoulliDistribution;
import parser.coresymbols.CalcSymbolPreOperator;
import parser.symbols.distributions.CalcSymbolDistributionContinuousBernoulli;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link ContinuousBernoulliDistribution}
 * @author Alexander Herzog
 * @see ContinuousBernoulliDistribution
 * @see DistributionTools
 */
public class WrapperContinuousBernoulliDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperContinuousBernoulliDistribution() {
		super(ContinuousBernoulliDistribution.class);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistContinuousBernoulli;
	}

	@Override
	protected String getThumbnailImageName() {
		return "continuousbernoulli.png";
	}

	@Override
	protected String getWikipediaURL() {
		return DistributionTools.DistContinuousBernoulliWikipedia;
	}

	@Override
	protected String getWebAppDistributionName() {
		return "ContinuousBernoulli";
	}

	@Override
	protected String getInfoHTML() {
		return null;
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(AbstractRealDistribution distribution) {
		final ContinuousBernoulliDistribution dist=(ContinuousBernoulliDistribution)distribution;
		final String info1=DistributionTools.DistRange+"=["+NumberTools.formatNumber(dist.getSupportLowerBound(),3)+";"+NumberTools.formatNumber(dist.getSupportUpperBound(),3)+"]";
		return new DistributionWrapperInfo(distribution,null,null,info1,null);
	}

	@Override
	public AbstractRealDistribution getDistribution(double mean, double sd) {
		return null;
	}

	@Override
	public AbstractRealDistribution getDefaultDistribution() {
		return new ContinuousBernoulliDistribution(0,100,0.25);
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
		if (nr==1) return ((ContinuousBernoulliDistribution)distribution).a;
		if (nr==2) return ((ContinuousBernoulliDistribution)distribution).b;
		if (nr==3) return ((ContinuousBernoulliDistribution)distribution).lambda;
		return 0.0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(AbstractRealDistribution distribution, int nr, double value) {
		if (nr==1) return new ContinuousBernoulliDistribution(value,((ContinuousBernoulliDistribution)distribution).b,((ContinuousBernoulliDistribution)distribution).lambda);
		if (nr==2) return new ContinuousBernoulliDistribution(((ContinuousBernoulliDistribution)distribution).a,value,((ContinuousBernoulliDistribution)distribution).lambda);
		if (nr==3) return new ContinuousBernoulliDistribution(((ContinuousBernoulliDistribution)distribution).a,((ContinuousBernoulliDistribution)distribution).b,value);
		return null;
	}

	@Override
	protected String getToStringData(AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(((ContinuousBernoulliDistribution)distribution).a)+";"+NumberTools.formatSystemNumber(((ContinuousBernoulliDistribution)distribution).b)+";"+NumberTools.formatSystemNumber(((ContinuousBernoulliDistribution)distribution).lambda);
	}

	@Override
	public AbstractRealDistribution fromString(String data, double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=3) return null;
		return new ContinuousBernoulliDistribution(values[0],values[1],values[2]);
	}

	@Override
	protected AbstractRealDistribution cloneInt(AbstractRealDistribution distribution) {
		return new ContinuousBernoulliDistribution(((ContinuousBernoulliDistribution)distribution).a,((ContinuousBernoulliDistribution)distribution).b,((ContinuousBernoulliDistribution)distribution).lambda);
	}

	@Override
	protected boolean compareInt(AbstractRealDistribution distribution1, AbstractRealDistribution distribution2) {
		if (Math.abs(((ContinuousBernoulliDistribution)distribution1).a-((ContinuousBernoulliDistribution)distribution2).a)>DistributionTools.MAX_ERROR) return false;
		if (Math.abs(((ContinuousBernoulliDistribution)distribution1).b-((ContinuousBernoulliDistribution)distribution2).b)>DistributionTools.MAX_ERROR) return false;
		if (Math.abs(((ContinuousBernoulliDistribution)distribution1).lambda-((ContinuousBernoulliDistribution)distribution2).lambda)>DistributionTools.MAX_ERROR) return false;
		return true;
	}

	@Override
	protected Class<? extends CalcSymbolPreOperator> getCalcSymbolClass() {
		return CalcSymbolDistributionContinuousBernoulli.class;
	}
}

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
import mathtools.distribution.DiscreteBorelDistributionImpl;
import parser.coresymbols.CalcSymbolPreOperator;
import parser.symbols.distributions.CalcSymbolDiscreteDistributionBorel;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link DiscreteBorelDistributionImpl}
 * @author Alexander Herzog
 * @see DiscreteBorelDistributionImpl
 * @see DistributionTools
 */
public class WrapperBorelDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperBorelDistribution() {
		super(DiscreteBorelDistributionImpl.class,true,false);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistBorel;
	}

	@Override
	protected String getThumbnailImageName() {
		return "discrete.png";
	}

	@Override
	protected String getWikipediaURL() {
		return DistributionTools.DistBorelWikipedia;
	}

	@Override
	protected String getWebAppDistributionName() {
		return "Borel";
	}

	@Override
	protected String getInfoHTML() {
		return null;
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(AbstractRealDistribution distribution) {
		final DiscreteBorelDistributionImpl dist=(DiscreteBorelDistributionImpl)distribution;
		final String info="mu="+NumberTools.formatNumber(dist.mu,3);
		return new DistributionWrapperInfo(distribution,null,null,info,null);
	}

	@Override
	public AbstractRealDistribution getDistribution(double mean, double sd) {
		final Double mu=DiscreteBorelDistributionImpl.getMuFromMean(mean);
		if (mu==null) return null;
		return new DiscreteBorelDistributionImpl(mu);
	}

	@Override
	public AbstractRealDistribution getDefaultDistribution() {
		return new DiscreteBorelDistributionImpl(0.5);
	}

	@Override
	protected AbstractRealDistribution setMeanInt(AbstractRealDistribution distribution, double mean) {
		return new DiscreteBorelDistributionImpl(DiscreteBorelDistributionImpl.getMuFromMean(Math.max(1,mean)));
	}

	@Override
	protected AbstractRealDistribution setStandardDeviationInt(AbstractRealDistribution distribution, double sd) {
		return null;
	}

	@Override
	protected double getParameterInt(AbstractRealDistribution distribution, int nr) {
		if (nr==1) return ((DiscreteBorelDistributionImpl)distribution).mu;
		return 0.0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(AbstractRealDistribution distribution, int nr, double value) {
		if (nr==1) return new DiscreteBorelDistributionImpl(value);
		return null;
	}

	@Override
	protected String getToStringData(AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(((DiscreteBorelDistributionImpl)distribution).mu);
	}

	@Override
	public AbstractRealDistribution fromString(String data, double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=1) return null;
		return new DiscreteBorelDistributionImpl(values[0]);
	}

	@Override
	protected AbstractRealDistribution cloneInt(AbstractRealDistribution distribution) {
		return new DiscreteBorelDistributionImpl((DiscreteBorelDistributionImpl)distribution);
	}

	@Override
	protected boolean compareInt(AbstractRealDistribution distribution1, AbstractRealDistribution distribution2) {
		if (Math.abs(((DiscreteBorelDistributionImpl)distribution1).mu-((DiscreteBorelDistributionImpl)distribution2).mu)>DistributionTools.MAX_ERROR) return false;
		return true;
	}

	@Override
	protected Class<? extends CalcSymbolPreOperator> getCalcSymbolClass() {
		return CalcSymbolDiscreteDistributionBorel.class;
	}
}

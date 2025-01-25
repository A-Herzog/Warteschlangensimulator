/**
 * Copyright 2024 Alexander Herzog
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
import mathtools.distribution.DiscreteLogarithmicDistributionImpl;
import parser.coresymbols.CalcSymbolPreOperator;
import parser.symbols.distributions.CalcSymbolDiscreteDistributionLogarithmic;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link DiscreteLogarithmicDistributionImpl}
 * @author Alexander Herzog
 * @see DiscreteLogarithmicDistributionImpl
 * @see DistributionTools
 */
public class WrapperLogarithmicDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperLogarithmicDistribution() {
		super(DiscreteLogarithmicDistributionImpl.class,true,false);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistLogarithmic;
	}

	@Override
	protected String getThumbnailImageName() {
		return "logarithmic.png";
	}

	@Override
	protected String getWikipediaURL() {
		return DistributionTools.DistLogarithmicWikipedia;
	}

	@Override
	protected String getWebAppDistributionName() {
		return "Logarithmic";
	}

	@Override
	protected String getInfoHTML() {
		return null;
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(AbstractRealDistribution distribution) {
		final DiscreteLogarithmicDistributionImpl dist=(DiscreteLogarithmicDistributionImpl)distribution;
		final String info="p="+NumberTools.formatNumber(dist.p,3);
		final double sk=dist.getSkewness();
		return new DistributionWrapperInfo(distribution,sk,null,info,null);
	}

	@Override
	public AbstractRealDistribution getDistribution(double mean, double sd) {
		return new DiscreteLogarithmicDistributionImpl(DiscreteLogarithmicDistributionImpl.getPFromMean(mean));
	}

	@Override
	public AbstractRealDistribution getDefaultDistribution() {
		return new DiscreteLogarithmicDistributionImpl(0.5);
	}

	@Override
	protected AbstractRealDistribution setMeanInt(AbstractRealDistribution distribution, double mean) {
		return new DiscreteLogarithmicDistributionImpl(DiscreteLogarithmicDistributionImpl.getPFromMean(mean));
	}

	@Override
	protected AbstractRealDistribution setStandardDeviationInt(AbstractRealDistribution distribution, double sd) {
		return null;
	}

	@Override
	protected double getParameterInt(AbstractRealDistribution distribution, int nr) {
		if (nr==1) return ((DiscreteLogarithmicDistributionImpl)distribution).p;
		return 0.0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(AbstractRealDistribution distribution, int nr, double value) {
		if (nr==1) return new DiscreteLogarithmicDistributionImpl(value);
		return null;
	}

	@Override
	protected String getToStringData(AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(((DiscreteLogarithmicDistributionImpl)distribution).p);
	}

	@Override
	public AbstractRealDistribution fromString(String data, double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=1) return null;
		return new DiscreteLogarithmicDistributionImpl(values[0]);
	}

	@Override
	protected AbstractRealDistribution cloneInt(AbstractRealDistribution distribution) {
		return new DiscreteLogarithmicDistributionImpl((DiscreteLogarithmicDistributionImpl)distribution);
	}

	@Override
	protected boolean compareInt(AbstractRealDistribution distribution1, AbstractRealDistribution distribution2) {
		if (Math.abs(((DiscreteLogarithmicDistributionImpl)distribution1).p-((DiscreteLogarithmicDistributionImpl)distribution2).p)>DistributionTools.MAX_ERROR) return false;
		return true;
	}

	@Override
	protected Class<? extends CalcSymbolPreOperator> getCalcSymbolClass() {
		return CalcSymbolDiscreteDistributionLogarithmic.class;
	}

	@Override
	protected boolean canSetMeanExact() {
		return false;
	}
}

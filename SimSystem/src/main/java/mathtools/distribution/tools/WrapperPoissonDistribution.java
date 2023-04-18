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
import mathtools.distribution.DiscretePoissonDistributionImpl;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link DiscretePoissonDistributionImpl}
 * @author Alexander Herzog
 * @see DiscretePoissonDistributionImpl
 * @see DistributionTools
 */
public class WrapperPoissonDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperPoissonDistribution() {
		super(DiscretePoissonDistributionImpl.class,true,false);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistPoisson;
	}

	@Override
	protected String getThumbnailImageName() {
		return "discrete.png";
	}

	@Override
	protected String getWikipediaURL() {
		return DistributionTools.DistPoissonWikipedia;
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(AbstractRealDistribution distribution) {
		final DiscretePoissonDistributionImpl dist=(DiscretePoissonDistributionImpl)distribution;
		final String info="lambda="+NumberTools.formatNumber(dist.lambda,3);
		final double sk=dist.getSkewness();
		final double mode=dist.getMode();
		return new DistributionWrapperInfo(distribution,sk,mode,info,null);
	}

	@Override
	public AbstractRealDistribution getDistribution(double mean, double sd) {
		return new DiscretePoissonDistributionImpl(mean);
	}

	@Override
	public AbstractRealDistribution getDefaultDistribution() {
		return new DiscretePoissonDistributionImpl(10);
	}

	@Override
	protected AbstractRealDistribution setMeanInt(AbstractRealDistribution distribution, double mean) {
		return new DiscretePoissonDistributionImpl(mean);
	}

	@Override
	protected AbstractRealDistribution setStandardDeviationInt(AbstractRealDistribution distribution, double sd) {
		return null;
	}

	@Override
	protected double getParameterInt(AbstractRealDistribution distribution, int nr) {
		if (nr==1) return ((DiscretePoissonDistributionImpl)distribution).lambda;
		return 0.0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(AbstractRealDistribution distribution, int nr, double value) {
		if (nr==1) return new DiscretePoissonDistributionImpl(value);
		return null;
	}

	@Override
	protected String getToStringData(AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(((DiscretePoissonDistributionImpl)distribution).lambda);
	}

	@Override
	public AbstractRealDistribution fromString(String data, double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=1) return null;
		return new DiscretePoissonDistributionImpl(values[0]);
	}

	@Override
	protected AbstractRealDistribution cloneInt(AbstractRealDistribution distribution) {
		return new DiscretePoissonDistributionImpl((DiscretePoissonDistributionImpl)distribution);
	}

	@Override
	protected boolean compareInt(AbstractRealDistribution distribution1, AbstractRealDistribution distribution2) {
		if (Math.abs(((DiscretePoissonDistributionImpl)distribution1).lambda-((DiscretePoissonDistributionImpl)distribution2).lambda)>DistributionTools.MAX_ERROR) return false;
		return true;
	}
}

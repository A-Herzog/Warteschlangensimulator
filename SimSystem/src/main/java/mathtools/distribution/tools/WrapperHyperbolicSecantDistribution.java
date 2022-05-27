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
import mathtools.distribution.HyperbolicSecantDistributionImpl;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link HyperbolicSecantDistributionImpl}
 * @author Alexander Herzog
 * @see HyperbolicSecantDistributionImpl
 * @see DistributionTools
 */
public class WrapperHyperbolicSecantDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperHyperbolicSecantDistribution() {
		super(HyperbolicSecantDistributionImpl.class,true,true);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistHyperbolicSecant;
	}

	@Override
	protected String getThumbnailImageName() {
		return "hyperbolicsecant.png";
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(AbstractRealDistribution distribution) {
		final HyperbolicSecantDistributionImpl dist=(HyperbolicSecantDistributionImpl)distribution;
		return new DistributionWrapperInfo(distribution,0.0,dist.mu); /* Schiefe=0 immer */
	}

	@Override
	public AbstractRealDistribution getDistribution(double mean, double sd) {
		return new HyperbolicSecantDistributionImpl(mean,sd);
	}

	@Override
	public AbstractRealDistribution getDefaultDistribution() {
		return getDistribution(100,50);
	}

	@Override
	public AbstractRealDistribution getDistributionForFit(double mean, double sd, final double min, final double max) {
		if (sd<=0) return null;
		return super.getDistributionForFit(mean,sd,min,max);
	}

	@Override
	public double getMean(final AbstractRealDistribution distribution) {
		return ((HyperbolicSecantDistributionImpl)distribution).mu;
	}

	@Override
	protected AbstractRealDistribution setMeanInt(AbstractRealDistribution distribution, double mean) {
		return new HyperbolicSecantDistributionImpl(mean,((HyperbolicSecantDistributionImpl)distribution).sigma);
	}

	@Override
	public double getStandardDeviation(final AbstractRealDistribution distribution) {
		return ((HyperbolicSecantDistributionImpl)distribution).sigma;
	}

	@Override
	protected AbstractRealDistribution setStandardDeviationInt(AbstractRealDistribution distribution, double sd) {
		return new HyperbolicSecantDistributionImpl(((HyperbolicSecantDistributionImpl)distribution).mu,sd);
	}

	@Override
	protected double getParameterInt(AbstractRealDistribution distribution, int nr) {
		if (nr==1) return ((HyperbolicSecantDistributionImpl)distribution).mu;
		if (nr==2) return ((HyperbolicSecantDistributionImpl)distribution).sigma;
		return 0.0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(AbstractRealDistribution distribution, int nr, double value) {
		if (nr==1) return new HyperbolicSecantDistributionImpl(value,((HyperbolicSecantDistributionImpl)distribution).sigma);
		if (nr==2) return new HyperbolicSecantDistributionImpl(((HyperbolicSecantDistributionImpl)distribution).mu,value);
		return null;
	}

	@Override
	protected String getToStringData(AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(((HyperbolicSecantDistributionImpl)distribution).mu)+";"+NumberTools.formatSystemNumber(((HyperbolicSecantDistributionImpl)distribution).sigma);
	}

	@Override
	public AbstractRealDistribution fromString(String data, double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=2) return null;
		return new HyperbolicSecantDistributionImpl(values[0],values[1]);
	}

	@Override
	protected AbstractRealDistribution cloneInt(AbstractRealDistribution distribution) {
		return ((HyperbolicSecantDistributionImpl)distribution).clone();
	}

	@Override
	protected boolean compareInt(AbstractRealDistribution distribution1, AbstractRealDistribution distribution2) {
		if (Math.abs(((HyperbolicSecantDistributionImpl)distribution1).mu-((HyperbolicSecantDistributionImpl)distribution2).mu)>DistributionTools.MAX_ERROR) return false;
		if (Math.abs(((HyperbolicSecantDistributionImpl)distribution1).sigma-((HyperbolicSecantDistributionImpl)distribution2).sigma)>DistributionTools.MAX_ERROR) return false;
		return true;
	}
}

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
import mathtools.distribution.FrechetDistributionImpl;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link FrechetDistributionImpl}
 * @author Alexander Herzog
 * @see FrechetDistributionImpl
 * @see DistributionTools
 */
public class WrapperFrechetDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperFrechetDistribution() {
		super(FrechetDistributionImpl.class);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistFrechet;
	}

	@Override
	protected String getThumbnailImageName() {
		return "frechet.png";
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(AbstractRealDistribution distribution) {
		final FrechetDistributionImpl frechetDist=(FrechetDistributionImpl)distribution;
		final double delta=frechetDist.delta;
		final double beta=frechetDist.beta;
		final double alpha=frechetDist.alpha;
		final String info=DistributionTools.DistLocation+"="+NumberTools.formatNumber(delta,3)+"; "+DistributionTools.DistScale+"="+NumberTools.formatNumber(beta,3)+"; Form="+NumberTools.formatNumber(alpha,3);
		return new DistributionWrapperInfo(distribution,frechetDist.getSkewness(),info,null);
	}

	@Override
	public AbstractRealDistribution getDistribution(double mean, double sd) {
		return null;
	}

	@Override
	public AbstractRealDistribution getDefaultDistribution() {
		return new FrechetDistributionImpl(0,20,1);
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
		if (nr==1) return ((FrechetDistributionImpl)distribution).delta;
		if (nr==2) return ((FrechetDistributionImpl)distribution).beta;
		if (nr==3) return ((FrechetDistributionImpl)distribution).alpha;
		return 0.0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(AbstractRealDistribution distribution, int nr, double value) {
		final FrechetDistributionImpl old=(FrechetDistributionImpl)distribution;
		if (nr==1) return new FrechetDistributionImpl(value,old.beta,old.alpha);
		if (nr==2) return new FrechetDistributionImpl(old.delta,value,old.alpha);
		if (nr==3) return new FrechetDistributionImpl(old.delta,old.beta,value);
		return null;
	}

	@Override
	protected String getToStringData(AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(((FrechetDistributionImpl)distribution).delta)+";"+NumberTools.formatSystemNumber(((FrechetDistributionImpl)distribution).beta)+";"+NumberTools.formatSystemNumber(((FrechetDistributionImpl)distribution).alpha);
	}

	@Override
	public AbstractRealDistribution fromString(String data, double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=3) return null;
		return new FrechetDistributionImpl(values[0],values[1],values[2]);
	}

	@Override
	protected AbstractRealDistribution cloneInt(AbstractRealDistribution distribution) {
		return new FrechetDistributionImpl((FrechetDistributionImpl)distribution);
	}

	@Override
	protected boolean compareInt(AbstractRealDistribution distribution1, AbstractRealDistribution distribution2) {
		if (Math.abs(((FrechetDistributionImpl)distribution1).delta-((FrechetDistributionImpl)distribution2).delta)>DistributionTools.MAX_ERROR) return false;
		if (Math.abs(((FrechetDistributionImpl)distribution1).beta-((FrechetDistributionImpl)distribution2).beta)>DistributionTools.MAX_ERROR) return false;
		if (Math.abs(((FrechetDistributionImpl)distribution1).alpha-((FrechetDistributionImpl)distribution2).alpha)>DistributionTools.MAX_ERROR) return false;
		return true;
	}
}

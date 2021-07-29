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
import mathtools.distribution.RayleighDistributionImpl;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link RayleighDistributionImpl}
 * @author Alexander Herzog
 * @see RayleighDistributionImpl
 * @see DistributionTools
 */
public class WrapperRayleighDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperRayleighDistribution() {
		super(RayleighDistributionImpl.class,true);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistRayleigh;
	}

	@Override
	protected String getThumbnailImageName() {
		return "rayleigh.png";
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(AbstractRealDistribution distribution) {
		final double sigma=((RayleighDistributionImpl)distribution).mean;
		final String info="sigma="+NumberTools.formatNumber(sigma,3);
		return new DistributionWrapperInfo(distribution,0.63111065781894,info,null); /* Schiefe: 2*sqrt(pi)*(pi-3)/(4-pi)^(3/2)=0.63111065781894 */
	}

	@Override
	public AbstractRealDistribution getDistribution(double mean, double sd) {
		return new RayleighDistributionImpl(mean);
	}

	@Override
	public AbstractRealDistribution getDefaultDistribution() {
		return getDistribution(100,50);
	}

	@Override
	public AbstractRealDistribution getDistributionForFit(final double mean, final double sd, final double min, final double max) {
		if (mean<=0) return null;
		return super.getDistributionForFit(mean,sd,min,max);
	}

	@Override
	protected AbstractRealDistribution setMeanInt(AbstractRealDistribution distribution, double mean) {
		return new RayleighDistributionImpl(mean);
	}

	@Override
	protected AbstractRealDistribution setStandardDeviationInt(AbstractRealDistribution distribution, double sd) {
		return null;
	}

	@Override
	protected double getParameterInt(AbstractRealDistribution distribution, int nr) {
		if (nr==1) return ((RayleighDistributionImpl)distribution).mean;
		return 0.0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(AbstractRealDistribution distribution, int nr, double value) {
		if (nr==1) return new RayleighDistributionImpl(value);
		return null;
	}

	@Override
	protected String getToStringData(AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(((RayleighDistributionImpl)distribution).mean);
	}

	@Override
	public AbstractRealDistribution fromString(String data, double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=1) return null;
		return new RayleighDistributionImpl(values[0]);
	}

	@Override
	protected AbstractRealDistribution cloneInt(AbstractRealDistribution distribution) {
		return ((RayleighDistributionImpl)distribution).clone();
	}

	@Override
	protected boolean compareInt(AbstractRealDistribution distribution1, AbstractRealDistribution distribution2) {
		if (Math.abs(((RayleighDistributionImpl)distribution1).sigma-((RayleighDistributionImpl)distribution2).sigma)>DistributionTools.MAX_ERROR) return false;
		return true;
	}
}

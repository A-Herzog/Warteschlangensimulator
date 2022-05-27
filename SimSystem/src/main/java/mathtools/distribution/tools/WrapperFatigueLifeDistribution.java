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
import mathtools.distribution.FatigueLifeDistributionImpl;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link FatigueLifeDistributionImpl}
 * @author Alexander Herzog
 * @see FatigueLifeDistributionImpl
 * @see DistributionTools
 */
public class WrapperFatigueLifeDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperFatigueLifeDistribution() {
		super(FatigueLifeDistributionImpl.class);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistFatigueLife;
	}

	@Override
	protected String getThumbnailImageName() {
		return "fatiguelife.png";
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(AbstractRealDistribution distribution) {
		final FatigueLifeDistributionImpl fatigueDist=(FatigueLifeDistributionImpl)distribution;
		final double mu=fatigueDist.mu;
		final double beta=fatigueDist.beta;
		final double gamma=fatigueDist.gamma;
		final String info=DistributionTools.DistLocation+"="+NumberTools.formatNumber(mu,3)+"; "+DistributionTools.DistScale+"="+NumberTools.formatNumber(beta,3)+"; Form="+NumberTools.formatNumber(gamma,3);
		return new DistributionWrapperInfo(distribution,fatigueDist.getSkewness(),null,info,null);
	}

	@Override
	public AbstractRealDistribution getDistribution(double mean, double sd) {
		return null;
	}

	@Override
	public AbstractRealDistribution getDefaultDistribution() {
		return new FatigueLifeDistributionImpl(0,20,1);
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
		if (nr==1) return ((FatigueLifeDistributionImpl)distribution).mu;
		if (nr==2) return ((FatigueLifeDistributionImpl)distribution).beta;
		if (nr==3) return ((FatigueLifeDistributionImpl)distribution).gamma;
		return 0.0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(AbstractRealDistribution distribution, int nr, double value) {
		final FatigueLifeDistributionImpl old=(FatigueLifeDistributionImpl)distribution;
		if (nr==1) return new FatigueLifeDistributionImpl(value,old.beta,old.gamma);
		if (nr==2) return new FatigueLifeDistributionImpl(old.mu,value,old.gamma);
		if (nr==3) return new FatigueLifeDistributionImpl(old.mu,old.beta,value);
		return null;
	}

	@Override
	protected String getToStringData(AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(((FatigueLifeDistributionImpl)distribution).mu)+";"+NumberTools.formatSystemNumber(((FatigueLifeDistributionImpl)distribution).beta)+";"+NumberTools.formatSystemNumber(((FatigueLifeDistributionImpl)distribution).gamma);
	}

	@Override
	public AbstractRealDistribution fromString(String data, double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=3) return null;
		return new FatigueLifeDistributionImpl(values[0],values[1],values[2]);
	}

	@Override
	protected AbstractRealDistribution cloneInt(AbstractRealDistribution distribution) {
		return new FatigueLifeDistributionImpl((FatigueLifeDistributionImpl)distribution);
	}

	@Override
	protected boolean compareInt(AbstractRealDistribution distribution1, AbstractRealDistribution distribution2) {
		if (Math.abs(((FatigueLifeDistributionImpl)distribution1).mu-((FatigueLifeDistributionImpl)distribution2).mu)>DistributionTools.MAX_ERROR) return false;
		if (Math.abs(((FatigueLifeDistributionImpl)distribution1).beta-((FatigueLifeDistributionImpl)distribution2).beta)>DistributionTools.MAX_ERROR) return false;
		if (Math.abs(((FatigueLifeDistributionImpl)distribution1).gamma-((FatigueLifeDistributionImpl)distribution2).gamma)>DistributionTools.MAX_ERROR) return false;
		return true;
	}
}

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
import mathtools.distribution.DiscreteBinomialDistributionImpl;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link DiscreteBinomialDistributionImpl}
 * @author Alexander Herzog
 * @see DiscreteBinomialDistributionImpl
 * @see DistributionTools
 */
public class WrapperBinomialDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperBinomialDistribution() {
		super(DiscreteBinomialDistributionImpl.class,true,true);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistBinomial;
	}

	@Override
	protected String getThumbnailImageName() {
		return "discrete.png";
	}

	@Override
	protected String getWikipediaURL() {
		return DistributionTools.DistBinomialWikipedia;
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
		final DiscreteBinomialDistributionImpl dist=(DiscreteBinomialDistributionImpl)distribution;
		final double sk=dist.getSkewness();
		final double mode=dist.getMode();
		final String info="p="+NumberTools.formatNumber(dist.p,3)+"; n="+dist.n;
		return new DistributionWrapperInfo(distribution,sk,mode,info,null);
	}

	@Override
	public AbstractRealDistribution getDistribution(double mean, double sd) {
		if (mean<=0 || sd<=0) return null;
		/* E=n*p, Var=n*p*(1-p) => n=E/p, p=1-Var/E */
		final double p=1-sd*sd/mean;
		if (p<=0 || p>1) return null;
		final int n=(int)Math.round(mean/p);
		return new DiscreteBinomialDistributionImpl(p,n);
	}

	@Override
	public AbstractRealDistribution getDefaultDistribution() {
		return new DiscreteBinomialDistributionImpl(0.5,10);
	}

	@Override
	protected AbstractRealDistribution setMeanInt(AbstractRealDistribution distribution, double mean) {
		if (mean<=0) return null;
		/* E=n*p, Var=n*p*(1-p) => n=E/p, p=1-Var/E */
		final double variance=((DiscreteBinomialDistributionImpl)distribution).getNumericalVariance();
		final double p=1-variance/mean;
		if (p<=0 || p>1) return null;
		final int n=(int)Math.round(mean/p);
		return new DiscreteBinomialDistributionImpl(p,n);
	}

	@Override
	protected AbstractRealDistribution setStandardDeviationInt(AbstractRealDistribution distribution, double sd) {
		if (sd<=0) return null;
		/* E=n*p, Var=n*p*(1-p) => n=E/p, p=1-Var/E */
		final double mean=((DiscreteBinomialDistributionImpl)distribution).getNumericalMean();
		final double p=1-sd*sd/mean;
		if (p<=0 || p>1) return null;
		final int n=(int)Math.round(mean/p);
		return new DiscreteBinomialDistributionImpl(p,n);
	}

	@Override
	protected double getParameterInt(AbstractRealDistribution distribution, int nr) {
		if (nr==1) return ((DiscreteBinomialDistributionImpl)distribution).p;
		if (nr==2) return ((DiscreteBinomialDistributionImpl)distribution).n;
		return 0.0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(AbstractRealDistribution distribution, int nr, double value) {
		if (nr==1) return new DiscreteBinomialDistributionImpl(value,((DiscreteBinomialDistributionImpl)distribution).n);
		if (nr==2) return new DiscreteBinomialDistributionImpl(((DiscreteBinomialDistributionImpl)distribution).p,(int)Math.round(value));
		return null;
	}

	@Override
	protected String getToStringData(AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(((DiscreteBinomialDistributionImpl)distribution).p)+";"+((DiscreteBinomialDistributionImpl)distribution).n;
	}

	@Override
	public AbstractRealDistribution fromString(String data, double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=2) return null;
		return new DiscreteBinomialDistributionImpl(values[0],(int)Math.round(values[1]));
	}

	@Override
	protected AbstractRealDistribution cloneInt(AbstractRealDistribution distribution) {
		return new DiscreteBinomialDistributionImpl((DiscreteBinomialDistributionImpl)distribution);
	}

	@Override
	protected boolean compareInt(AbstractRealDistribution distribution1, AbstractRealDistribution distribution2) {
		if (Math.abs(((DiscreteBinomialDistributionImpl)distribution1).p-((DiscreteBinomialDistributionImpl)distribution2).p)>DistributionTools.MAX_ERROR) return false;
		if (((DiscreteBinomialDistributionImpl)distribution1).n!=((DiscreteBinomialDistributionImpl)distribution2).n) return false;
		return true;
	}
}

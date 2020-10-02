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
import mathtools.distribution.InverseGaussianDistributionImpl;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link InverseGaussianDistributionImpl}
 * @author Alexander Herzog
 * @see InverseGaussianDistributionImpl
 * @see DistributionTools
 */
public class WrapperInverseGaussianDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperInverseGaussianDistribution() {
		super(InverseGaussianDistributionImpl.class,true,true);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistInverseGaussian;
	}

	@Override
	protected String getThumbnailImageName() {
		return "inversegaussian.png";
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(AbstractRealDistribution distribution) {
		final double lambda=((InverseGaussianDistributionImpl)distribution).lambda;
		final double mu=((InverseGaussianDistributionImpl)distribution).mu;
		final String info="lambda="+NumberTools.formatNumber(lambda,3)+"; mu="+NumberTools.formatNumber(mu,3);
		return new DistributionWrapperInfo(distribution,info,null);
	}

	@Override
	public AbstractRealDistribution getDistribution(double mean, double sd) {
		return new InverseGaussianDistributionImpl(mean*mean*mean/sd/sd,mean);
	}

	@Override
	public AbstractRealDistribution getDefaultDistribution() {
		return getDistribution(100,50);
	}

	@Override
	public AbstractRealDistribution getDistributionForFit(final double mean, final double sd) {
		if (sd<=0) return null;
		return super.getDistributionForFit(mean,sd);
	}

	@Override
	protected AbstractRealDistribution setMeanInt(AbstractRealDistribution distribution, double mean) {
		double var=((InverseGaussianDistributionImpl)distribution).getNumericalVariance();
		return new InverseGaussianDistributionImpl(mean*mean*mean/var,mean);
	}

	@Override
	protected AbstractRealDistribution setStandardDeviationInt(AbstractRealDistribution distribution, double sd) {
		final double mean=((InverseGaussianDistributionImpl)distribution).mu;
		return new InverseGaussianDistributionImpl(mean*mean*mean/sd/sd,mean);
	}

	@Override
	protected double getParameterInt(AbstractRealDistribution distribution, int nr) {
		if (nr==1) return ((InverseGaussianDistributionImpl)distribution).lambda;
		if (nr==2) return ((InverseGaussianDistributionImpl)distribution).mu;
		return 0.0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(AbstractRealDistribution distribution, int nr, double value) {
		final InverseGaussianDistributionImpl g=(InverseGaussianDistributionImpl)distribution;
		if (nr==1) return new InverseGaussianDistributionImpl(value,g.mu);
		if (nr==2) return new InverseGaussianDistributionImpl(g.lambda,value);
		return null;
	}

	@Override
	protected String getToStringData(AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(((InverseGaussianDistributionImpl)distribution).lambda)+";"+NumberTools.formatSystemNumber(((InverseGaussianDistributionImpl)distribution).mu);
	}

	@Override
	public AbstractRealDistribution fromString(String data, double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=2) return null;
		return new InverseGaussianDistributionImpl(values[0],values[1]);
	}

	@Override
	protected AbstractRealDistribution cloneInt(AbstractRealDistribution distribution) {
		return ((InverseGaussianDistributionImpl)distribution).clone();
	}

	@Override
	protected boolean compareInt(AbstractRealDistribution distribution1, AbstractRealDistribution distribution2) {
		if (Math.abs(((InverseGaussianDistributionImpl)distribution1).lambda-((InverseGaussianDistributionImpl)distribution2).lambda)>DistributionTools.MAX_ERROR) return false;
		if (Math.abs(((InverseGaussianDistributionImpl)distribution1).mu-((InverseGaussianDistributionImpl)distribution2).mu)>DistributionTools.MAX_ERROR) return false;
		return true;
	}
}

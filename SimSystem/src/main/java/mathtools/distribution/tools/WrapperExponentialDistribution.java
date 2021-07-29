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
import org.apache.commons.math3.distribution.ExponentialDistribution;

import mathtools.NumberTools;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link ExponentialDistribution}
 * @author Alexander Herzog
 * @see ExponentialDistribution
 * @see DistributionTools
 */
public class WrapperExponentialDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperExponentialDistribution() {
		super(ExponentialDistribution.class,true,true);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistExp;
	}

	@Override
	protected String getThumbnailImageName() {
		return "exp.png";
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(AbstractRealDistribution distribution) {
		return new DistributionWrapperInfo(distribution,2.0); /* Schiefe=2 immer */
	}

	@Override
	public AbstractRealDistribution getDistribution(double mean, double sd) {
		return new ExponentialDistribution(null,Math.max(mean,0.0001),ExponentialDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
	}

	@Override
	public AbstractRealDistribution getDefaultDistribution() {
		return getDistribution(100,100);
	}

	@Override
	public AbstractRealDistribution getDistributionForFit(final double mean, final double sd, final double min, final double max) {
		if (mean<=0) return null;
		return super.getDistributionForFit(mean,sd,min,max);
	}

	@Override
	public double getMean(final AbstractRealDistribution distribution) {
		return ((ExponentialDistribution)distribution).getMean();
	}

	@Override
	protected AbstractRealDistribution setMeanInt(AbstractRealDistribution distribution, double mean) {
		return new ExponentialDistribution(null,mean,ExponentialDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
	}

	@Override
	public double getStandardDeviation(final AbstractRealDistribution distribution) {
		return ((ExponentialDistribution)distribution).getMean(); /* E=Std */
	}

	@Override
	protected AbstractRealDistribution setStandardDeviationInt(AbstractRealDistribution distribution, double sd) {
		return new ExponentialDistribution(null,sd,ExponentialDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY); /* E=Std */
	}

	@Override
	protected double getParameterInt(AbstractRealDistribution distribution, int nr) {
		if (nr==1) return ((ExponentialDistribution)distribution).getMean();
		return 0.0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(AbstractRealDistribution distribution, int nr, double value) {
		if (nr==1) return new ExponentialDistribution(null,value,ExponentialDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
		return null;
	}

	@Override
	protected String getToStringData(AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(((ExponentialDistribution)distribution).getMean());
	}

	@Override
	public AbstractRealDistribution fromString(String data, double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=1) return null;
		return new ExponentialDistribution(null,values[0],ExponentialDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
	}

	@Override
	protected AbstractRealDistribution cloneInt(AbstractRealDistribution distribution) {
		return new ExponentialDistribution(null,((ExponentialDistribution)distribution).getMean(),ExponentialDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
	}

	@Override
	protected boolean compareInt(AbstractRealDistribution distribution1, AbstractRealDistribution distribution2) {
		return Math.abs(((ExponentialDistribution)distribution1).getMean()-((ExponentialDistribution)distribution2).getMean())<=DistributionTools.MAX_ERROR;
	}
}

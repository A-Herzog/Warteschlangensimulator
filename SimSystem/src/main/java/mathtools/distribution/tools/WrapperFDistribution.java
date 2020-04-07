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
import org.apache.commons.math3.distribution.FDistribution;

import mathtools.NumberTools;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link FDistribution}
 * @author Alexander Herzog
 * @see FDistribution
 * @see DistributionTools
 */
public class WrapperFDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperFDistribution() {
		super(FDistribution.class,true);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistF;
	}

	@Override
	protected String getThumbnailImageName() {
		return "f.png";
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(AbstractRealDistribution distribution) {
		final double m=((FDistribution)distribution).getNumeratorDegreesOfFreedom();
		final double n=((FDistribution)distribution).getDenominatorDegreesOfFreedom();
		final String info=DistributionTools.DistDegreesOfFreedom+"="+NumberTools.formatNumber(m)+"/"+NumberTools.formatNumber(n);
		return new DistributionWrapperInfo(distribution,info,null);
	}

	@Override
	public AbstractRealDistribution getDistribution(double mean, double sd) {
		if (mean>1) {
			final double n=2*mean/(mean-1);
			final double m=2*n*n*(n-2)/(sd*sd*(n-2)*(n-2)*(n-4)-2*n*n);
			if (Math.round(m)<1) return null;
			if (Math.round(n)<3) return null;
			return new FDistribution(Math.max(Math.round(m),1),Math.max(Math.round(n),3));
		} else {
			return null;
		}
	}

	@Override
	public AbstractRealDistribution getDistributionForFit(final double mean, final double sd) {
		if (mean<=1) return null;
		return super.getDistributionForFit(mean,sd);
	}

	@Override
	public double getMean(final AbstractRealDistribution distribution) {
		if (((FDistribution)distribution).getDenominatorDegreesOfFreedom()<2) return 0; else return ((FDistribution)distribution).getDenominatorDegreesOfFreedom()/(((FDistribution)distribution).getDenominatorDegreesOfFreedom()-2);
	}

	@Override
	protected boolean canSetMeanExact() {
		return false;
	}

	@Override
	protected AbstractRealDistribution setMeanInt(AbstractRealDistribution distribution, double mean) {
		final double var=((FDistribution)distribution).getNumericalVariance();
		final double n=2*mean/(mean-1);
		final double m=2*n*n*(n-2)/(var*(n-2)*(n-2)*(n-4)-2*n*n);
		return new FDistribution(Math.max(Math.round(m),1),Math.max(Math.round(n),3));
	}

	@Override
	public double getStandardDeviation(final AbstractRealDistribution distribution) {
		double m=((FDistribution)distribution).getNumeratorDegreesOfFreedom(); double n=((FDistribution)distribution).getDenominatorDegreesOfFreedom();
		if (n<4) return Double.POSITIVE_INFINITY;
		return Math.sqrt(2*n*n*(m+n-2)/(m*(n-2)*(n-2)*(n-4)));
	}

	@Override
	protected AbstractRealDistribution setStandardDeviationInt(AbstractRealDistribution distribution, double sd) {
		return null;
	}

	@Override
	protected double getParameterInt(AbstractRealDistribution distribution, int nr) {
		if (nr==1) return ((FDistribution)distribution).getNumeratorDegreesOfFreedom();
		if (nr==2) return ((FDistribution)distribution).getDenominatorDegreesOfFreedom();
		return 0.0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(AbstractRealDistribution distribution, int nr, double value) {
		if (nr==1) return new FDistribution(null,value,((FDistribution)distribution).getDenominatorDegreesOfFreedom(),FDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
		if (nr==2) return new FDistribution(null,((FDistribution)distribution).getNumeratorDegreesOfFreedom(),value,FDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
		return null;
	}

	@Override
	protected String getToStringData(AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(((FDistribution)distribution).getNumeratorDegreesOfFreedom())+";"+NumberTools.formatSystemNumber(((FDistribution)distribution).getDenominatorDegreesOfFreedom());
	}

	@Override
	public AbstractRealDistribution fromString(String data, double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=2) return null;
		return new FDistribution(null,values[0],values[1],FDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
	}

	@Override
	protected AbstractRealDistribution cloneInt(AbstractRealDistribution distribution) {
		return new FDistribution(null,((FDistribution)distribution).getNumeratorDegreesOfFreedom(),((FDistribution)distribution).getDenominatorDegreesOfFreedom(),FDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
	}

	@Override
	protected boolean compareInt(AbstractRealDistribution distribution1, AbstractRealDistribution distribution2) {
		if (Math.abs(((FDistribution)distribution1).getNumeratorDegreesOfFreedom()-((FDistribution)distribution2).getNumeratorDegreesOfFreedom())>DistributionTools.MAX_ERROR) return false;
		if (Math.abs(((FDistribution)distribution1).getDenominatorDegreesOfFreedom()-((FDistribution)distribution2).getDenominatorDegreesOfFreedom())>DistributionTools.MAX_ERROR) return false;
		return true;
	}
}
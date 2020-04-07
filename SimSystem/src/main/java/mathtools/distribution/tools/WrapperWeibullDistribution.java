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
import org.apache.commons.math3.distribution.WeibullDistribution;
import org.apache.commons.math3.special.Gamma;
import org.apache.commons.math3.util.FastMath;

import mathtools.NumberTools;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link WeibullDistribution}
 * @author Alexander Herzog
 * @see WeibullDistribution
 * @see DistributionTools
 */
public class WrapperWeibullDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperWeibullDistribution() {
		super(WeibullDistribution.class);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistWeibull;
	}

	@Override
	protected String getThumbnailImageName() {
		return "weibull.png";
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(AbstractRealDistribution distribution) {
		final double beta=((WeibullDistribution)distribution).getShape(), lambda=1/((WeibullDistribution)distribution).getScale();
		final String info=DistributionTools.DistScale+"="+NumberTools.formatNumber(lambda,3)+"; Form="+NumberTools.formatNumber(beta,3);
		return new DistributionWrapperInfo(distribution,info,null);
	}

	@Override
	public AbstractRealDistribution getDistribution(double mean, double sd) {
		return null;
	}

	@Override
	public double getMean(final AbstractRealDistribution distribution) {
		final double sh = ((WeibullDistribution)distribution).getShape();
		final double sc = ((WeibullDistribution)distribution).getScale();
		/* Achtung: scale ist hier anderes definiert als z.B. in Wikipedia; dort wäre Vorfaktor 1/sc richtig. */
		return sc * FastMath.exp(Gamma.logGamma(1 + (1 / sh)));
	}

	@Override
	protected AbstractRealDistribution setMeanInt(AbstractRealDistribution distribution, double mean) {
		return null;
	}

	@Override
	public double getStandardDeviation(final AbstractRealDistribution distribution) {
		double beta=((WeibullDistribution)distribution).getShape(); double lambda=1/((WeibullDistribution)distribution).getScale();
		return 1/lambda*Math.sqrt(Math.exp(Gamma.logGamma(1+2/beta))-Math.pow(Math.exp(Gamma.logGamma(1+1/beta)),2));
	}

	@Override
	protected AbstractRealDistribution setStandardDeviationInt(AbstractRealDistribution distribution, double sd) {
		return null;
	}

	@Override
	protected double getParameterInt(AbstractRealDistribution distribution, int nr) {
		if (nr==1) return 1/((WeibullDistribution)distribution).getScale();
		if (nr==2) return ((WeibullDistribution)distribution).getShape();
		return 0.0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(AbstractRealDistribution distribution, int nr, double value) {
		if (nr<1 || nr>2) return null;
		double p1=1/((WeibullDistribution)distribution).getScale();
		double p2=((WeibullDistribution)distribution).getShape();
		if (nr==1) p1=value;
		if (nr==2) p2=value;
		if (p1==0) return null;
		return new WeibullDistribution(null,p2,1/p1,WeibullDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
	}

	@Override
	protected String getToStringData(AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(1/((WeibullDistribution)distribution).getScale())+";"+NumberTools.formatSystemNumber(((WeibullDistribution)distribution).getShape());
	}

	@Override
	public AbstractRealDistribution fromString(String data, double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=2) return null;
		return new WeibullDistribution(null,values[1],1/values[0],WeibullDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
	}

	@Override
	protected AbstractRealDistribution cloneInt(AbstractRealDistribution distribution) {
		return new WeibullDistribution(null,((WeibullDistribution)distribution).getShape(),((WeibullDistribution)distribution).getScale(),WeibullDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
	}

	@Override
	protected boolean compareInt(AbstractRealDistribution distribution1, AbstractRealDistribution distribution2) {
		if (Math.abs(((WeibullDistribution)distribution1).getShape()-((WeibullDistribution)distribution2).getShape())>DistributionTools.MAX_ERROR) return false;
		if (Math.abs(((WeibullDistribution)distribution1).getScale()-((WeibullDistribution)distribution2).getScale())>DistributionTools.MAX_ERROR) return false;
		return true;
	}
}

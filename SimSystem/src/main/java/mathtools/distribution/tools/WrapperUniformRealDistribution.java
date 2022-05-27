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
import org.apache.commons.math3.distribution.UniformRealDistribution;

import mathtools.NumberTools;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link UniformRealDistribution}
 * @author Alexander Herzog
 * @see UniformRealDistribution
 * @see DistributionTools
 */
public class WrapperUniformRealDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperUniformRealDistribution() {
		super(UniformRealDistribution.class,true,true);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistUniform;
	}

	@Override
	protected String getThumbnailImageName() {
		return "uniform.png";
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(final AbstractRealDistribution distribution) {
		return new DistributionWrapperInfo(distribution,0.0,null); /* Schiefe=0 immer */
	}

	@Override
	public AbstractRealDistribution getDistribution(final double mean, final double sd) {
		final double a=mean-sd*Math.sqrt(12)/2;
		double b=2*mean-a;
		if (b<=a) b=b+0.01;
		return new UniformRealDistribution(a,b);
	}

	@Override
	public AbstractRealDistribution getDefaultDistribution() {
		return getDistribution(100,50);
	}

	@Override
	public AbstractRealDistribution getDistributionForFit(final double mean, final double sd, final double min, final double max) {
		if (sd<=0) return null;
		return super.getDistributionForFit(mean,sd,min,max);
	}

	@Override
	public double getMean(final AbstractRealDistribution distribution) {
		return ((UniformRealDistribution)distribution).getNumericalMean();
	}

	@Override
	protected AbstractRealDistribution setMeanInt(final AbstractRealDistribution distribution, final double mean) {
		final UniformRealDistribution oldDistribution=(UniformRealDistribution)distribution;

		final double delta=mean-oldDistribution.getNumericalMean();
		final double a=oldDistribution.getSupportLowerBound();
		final double b=oldDistribution.getSupportUpperBound();
		return new UniformRealDistribution(a+delta,b+delta);
	}

	@Override
	public double getStandardDeviation(final AbstractRealDistribution distribution) {
		return Math.sqrt(((UniformRealDistribution)distribution).getNumericalVariance());
	}

	@Override
	protected AbstractRealDistribution setStandardDeviationInt(final AbstractRealDistribution distribution, final double sd) {
		final UniformRealDistribution oldDistribution=(UniformRealDistribution)distribution;

		final double E=oldDistribution.getNumericalMean();
		final double var=sd*sd;
		final double range=Math.sqrt(var*12);
		final double newA=E-range/2;
		final double newB=E+range/2;
		return new UniformRealDistribution(newA,newB);
	}

	@Override
	protected double getParameterInt(final AbstractRealDistribution distribution, final int nr) {
		if (nr==1) return ((UniformRealDistribution)distribution).getSupportLowerBound();
		if (nr==2) return ((UniformRealDistribution)distribution).getSupportUpperBound();
		return 0.0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(final AbstractRealDistribution distribution, final int nr, final double value) {
		if (nr==1) return new UniformRealDistribution(null,value,((UniformRealDistribution)distribution).getSupportUpperBound());
		if (nr==2) return new UniformRealDistribution(null,((UniformRealDistribution)distribution).getSupportLowerBound(),value);
		return null;
	}

	@Override
	protected String getToStringData(final AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(((UniformRealDistribution)distribution).getSupportLowerBound())+";"+NumberTools.formatSystemNumber(((UniformRealDistribution)distribution).getSupportUpperBound());
	}

	@Override
	public AbstractRealDistribution fromString(final String data, final double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=2) return null;
		return new UniformRealDistribution(null,values[0],values[1]);
	}

	@Override
	protected AbstractRealDistribution cloneInt(final AbstractRealDistribution distribution) {
		return new UniformRealDistribution(null,((UniformRealDistribution)distribution).getSupportLowerBound(),((UniformRealDistribution)distribution).getSupportUpperBound());
	}

	@Override
	protected boolean compareInt(final AbstractRealDistribution distribution1, final AbstractRealDistribution distribution2) {
		if (Math.abs(((UniformRealDistribution)distribution1).getSupportLowerBound()-((UniformRealDistribution)distribution2).getSupportLowerBound())>DistributionTools.MAX_ERROR) return false;
		if (Math.abs(((UniformRealDistribution)distribution1).getSupportUpperBound()-((UniformRealDistribution)distribution2).getSupportUpperBound())>DistributionTools.MAX_ERROR) return false;
		return true;
	}
}

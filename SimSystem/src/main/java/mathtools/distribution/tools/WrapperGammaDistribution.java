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
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.util.FastMath;

import mathtools.NumberTools;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link GammaDistribution}
 * @author Alexander Herzog
 * @see GammaDistribution
 * @see DistributionTools
 */
public class WrapperGammaDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperGammaDistribution() {
		super(GammaDistribution.class,true,true);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistGamma;
	}

	@Override
	protected String getThumbnailImageName() {
		return "gamma.png";
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(AbstractRealDistribution distribution) {
		final GammaDistribution dist=(GammaDistribution)distribution;
		final double alpha=dist.getShape();
		final double beta=dist.getScale();
		final double sk=2/Math.sqrt(alpha);
		final double mode=(alpha>=1)?(alpha-1)*beta:0;
		final String info1="alpha="+NumberTools.formatNumber(alpha,3)+"; beta="+NumberTools.formatNumber(beta,3);
		return new DistributionWrapperInfo(distribution,sk,mode,info1,null);
	}

	@Override
	public AbstractRealDistribution getDistribution(double mean, double sd) {
		final double d2=sd*sd/Math.max(mean,0.000001);
		final double d1=mean/Math.max(d2,0.000001);
		return new GammaDistribution(d1,d2);
	}

	@Override
	public AbstractRealDistribution getDefaultDistribution() {
		return getDistribution(100,50);
	}

	@Override
	public AbstractRealDistribution getDistributionForFit(double mean, double sd, final double min, final double max) {
		if (sd<=0 || mean<=0) return null;
		return super.getDistributionForFit(mean,sd,min,max);
	}

	@Override
	public double getMean(final AbstractRealDistribution distribution) {
		return ((GammaDistribution)distribution).getShape()*((GammaDistribution)distribution).getScale();
	}

	@Override
	protected AbstractRealDistribution setMeanInt(AbstractRealDistribution distribution, double mean) {
		final double shape=((GammaDistribution)distribution).getShape();
		final double scale=((GammaDistribution)distribution).getScale();
		final double var=shape*scale*scale;
		final double shapeNew=mean*mean/Math.max(var,0.00001);
		final double scaleNew=mean/Math.max(shapeNew,0.00001);
		return new GammaDistribution(shapeNew,scaleNew);
	}

	@Override
	public double getStandardDeviation(final AbstractRealDistribution distribution) {
		final double alpha=((GammaDistribution)distribution).getShape(); double beta=((GammaDistribution)distribution).getScale();
		return Math.sqrt(alpha*FastMath.pow(beta,2));
	}

	@Override
	protected AbstractRealDistribution setStandardDeviationInt(AbstractRealDistribution distribution, double sd) {
		final double shape=((GammaDistribution)distribution).getShape();
		final double scale=((GammaDistribution)distribution).getScale();
		final double E=shape*scale;
		final double shapeNew=E*E/(sd*sd);
		final double scaleNew=E/Math.max(shapeNew,0.00001);
		return new GammaDistribution(shapeNew,scaleNew);
	}

	@Override
	protected double getParameterInt(AbstractRealDistribution distribution, int nr) {
		if (nr==1) return ((GammaDistribution)distribution).getShape();
		if (nr==2) return ((GammaDistribution)distribution).getScale();
		return 0.0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(AbstractRealDistribution distribution, int nr, double value) {
		if (nr==1) return new GammaDistribution(null,value,((GammaDistribution)distribution).getScale(),GammaDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
		if (nr==2) return new GammaDistribution(null,((GammaDistribution)distribution).getShape(),value,GammaDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
		return null;
	}

	@Override
	protected String getToStringData(AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(((GammaDistribution)distribution).getShape())+";"+NumberTools.formatSystemNumber(((GammaDistribution)distribution).getScale());
	}

	@Override
	public AbstractRealDistribution fromString(String data, double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=2) return null;
		return new GammaDistribution(null,values[0],values[1],GammaDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
	}

	@Override
	protected AbstractRealDistribution cloneInt(AbstractRealDistribution distribution) {
		return new GammaDistribution(null,((GammaDistribution)distribution).getShape(),((GammaDistribution)distribution).getScale(),GammaDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
	}

	@Override
	protected boolean compareInt(AbstractRealDistribution distribution1, AbstractRealDistribution distribution2) {
		if (Math.abs(((GammaDistribution)distribution1).getShape()-((GammaDistribution)distribution2).getShape())>DistributionTools.MAX_ERROR) return false;
		if (Math.abs(((GammaDistribution)distribution1).getScale()-((GammaDistribution)distribution2).getScale())>DistributionTools.MAX_ERROR) return false;
		return true;
	}
}

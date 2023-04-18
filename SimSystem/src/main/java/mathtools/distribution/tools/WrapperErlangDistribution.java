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
import org.apache.commons.math3.util.FastMath;

import mathtools.NumberTools;
import mathtools.distribution.ErlangDistributionImpl;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link ErlangDistributionImpl}
 * @author Alexander Herzog
 * @see ErlangDistributionImpl
 * @see DistributionTools
 */
public class WrapperErlangDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperErlangDistribution() {
		super(ErlangDistributionImpl.class,true,true);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistErlang;
	}

	@Override
	protected String getThumbnailImageName() {
		return "erlang.png";
	}

	@Override
	protected String getWikipediaURL() {
		return DistributionTools.DistErlangWikipedia;
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(AbstractRealDistribution distribution) {
		final ErlangDistributionImpl dist=(ErlangDistributionImpl)distribution;
		final double n=dist.getShape();
		final double lambda=dist.getScale();
		final double sk=2/Math.sqrt(n);
		final double mode=(n>=1)?(n-1)*lambda:0;
		final String info1="n="+NumberTools.formatNumber(n)+"; lambda="+NumberTools.formatNumber(lambda,3);
		return new DistributionWrapperInfo(distribution,sk,mode,info1,null);
	}

	@Override
	public AbstractRealDistribution getDistribution(double mean, double sd) {
		final double d2=sd*sd/Math.max(Math.round(mean),1.0);
		final double d1=Math.round(mean/Math.max(d2,0.000001));
		return new ErlangDistributionImpl(d1,d2);
	}

	@Override
	public AbstractRealDistribution getDefaultDistribution() {
		return getDistribution(100,50);
	}

	@Override
	public AbstractRealDistribution getDistributionForFit(double mean, double sd, final double min, final double max) {
		final double d2=sd*sd/Math.max(mean,0.000001);
		final double d1=mean/Math.max(d2,0.000001);
		return new ErlangDistributionImpl(Math.round(d1),d2);
	}

	@Override
	public double getMean(final AbstractRealDistribution distribution) {
		return ((ErlangDistributionImpl)distribution).getShape()*((ErlangDistributionImpl)distribution).getScale();
	}

	@Override
	protected boolean canSetMeanExact() {
		return false;
	}

	@Override
	protected AbstractRealDistribution setMeanInt(AbstractRealDistribution distribution, double mean) {
		final double shape=((ErlangDistributionImpl)distribution).getShape();
		final double scale=((ErlangDistributionImpl)distribution).getScale();
		final double var=shape*scale*scale;
		final double shapeNew=mean*mean/Math.max(var,0.00001);
		final double scaleNew=mean/Math.max(shapeNew,0.00001);
		return new ErlangDistributionImpl(Math.max(1,Math.round(shapeNew)),scaleNew);
	}

	@Override
	public double getStandardDeviation(final AbstractRealDistribution distribution) {
		final double n=((ErlangDistributionImpl)distribution).getShape(); double lambda=((ErlangDistributionImpl)distribution).getScale();
		return Math.sqrt(n*FastMath.pow(lambda,2));
	}

	@Override
	protected boolean canSetStandardDeviationExact() {
		return false;
	}

	@Override
	protected AbstractRealDistribution setStandardDeviationInt(AbstractRealDistribution distribution, double sd) {
		final double shape=((ErlangDistributionImpl)distribution).getShape();
		final double scale=((ErlangDistributionImpl)distribution).getScale();
		final double E=shape*scale;
		final double shapeNew=E*E/(sd*sd);
		final double scaleNew=E/Math.max(shapeNew,0.00001);
		return new ErlangDistributionImpl(Math.max(1,Math.round(shapeNew)),scaleNew);
	}

	@Override
	protected double getParameterInt(AbstractRealDistribution distribution, int nr) {
		if (nr==1) return ((ErlangDistributionImpl)distribution).getShape();
		if (nr==2) return ((ErlangDistributionImpl)distribution).getScale();
		return 0.0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(AbstractRealDistribution distribution, int nr, double value) {
		if (nr==1) return new ErlangDistributionImpl(value,((ErlangDistributionImpl)distribution).getScale());
		if (nr==2) return new ErlangDistributionImpl(((ErlangDistributionImpl)distribution).getShape(),value);
		return null;
	}

	@Override
	protected String getToStringData(AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(((ErlangDistributionImpl)distribution).getShape())+";"+NumberTools.formatSystemNumber(((ErlangDistributionImpl)distribution).getScale());
	}

	@Override
	public AbstractRealDistribution fromString(String data, double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=2) return null;
		return new ErlangDistributionImpl(values[0],values[1]);
	}

	@Override
	protected AbstractRealDistribution cloneInt(AbstractRealDistribution distribution) {
		return new ErlangDistributionImpl(((ErlangDistributionImpl)distribution).getShape(),((ErlangDistributionImpl)distribution).getScale());
	}

	@Override
	protected boolean compareInt(AbstractRealDistribution distribution1, AbstractRealDistribution distribution2) {
		if (Math.abs(((ErlangDistributionImpl)distribution1).getShape()-((ErlangDistributionImpl)distribution2).getShape())>DistributionTools.MAX_ERROR) return false;
		if (Math.abs(((ErlangDistributionImpl)distribution1).getScale()-((ErlangDistributionImpl)distribution2).getScale())>DistributionTools.MAX_ERROR) return false;
		return true;
	}
}

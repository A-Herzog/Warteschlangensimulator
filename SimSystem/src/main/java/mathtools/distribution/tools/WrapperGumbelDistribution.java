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
import org.apache.commons.math3.distribution.GumbelDistribution;
import org.apache.commons.math3.util.FastMath;

import mathtools.NumberTools;
import mathtools.distribution.ExtBetaDistributionImpl;

/**
 * Zus�tzliche Daten f�r ein Objekt vom Typ {@link GumbelDistribution}
 * @author Alexander Herzog
 * @see ExtBetaDistributionImpl
 * @see GumbelDistribution
 */
public class WrapperGumbelDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperGumbelDistribution() {
		super(GumbelDistribution.class,true,true);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistGumbel;
	}

	@Override
	protected String getThumbnailImageName() {
		return "gumbel.png";
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(AbstractRealDistribution distribution) {
		final double location=((GumbelDistribution)distribution).getLocation();
		final double scale=((GumbelDistribution)distribution).getScale();
		final String info=DistributionTools.DistLocation+"="+NumberTools.formatNumber(location,3)+"; "+DistributionTools.DistScale+"="+NumberTools.formatNumber(scale,3);
		return new DistributionWrapperInfo(distribution,info,null);
	}

	@Override
	public AbstractRealDistribution getDistribution(double mean, double sd) {
		if (sd<=0) sd=0.0001;
		final double scale=sd*Math.sqrt(6)/Math.PI;
		/* final double location=mean-scale*Gamma.GAMMA; /* Gamma.GAMMA == Euler�Mascheroni constant */
		final double EULER = FastMath.PI / (2 * FastMath.E);
		final double location=mean-scale*EULER; /* Gamma.GAMMA w�re zwar genauer, aber intern verwendet die Verteilung auch diese N�herung. Sonst gibt's also Rundungsprobleme. */
		return new GumbelDistribution(location,scale);
	}

	@Override
	public AbstractRealDistribution getDefaultDistribution() {
		return getDistribution(100,50);
	}

	@Override
	public AbstractRealDistribution getDistributionForFit(double mean, double sd, final double min, final double max) {
		if (sd<=0) return null;
		return super.getDistributionForFit(mean,sd,min,max);
	}

	@Override
	protected AbstractRealDistribution setMeanInt(AbstractRealDistribution distribution, double mean) {
		final double sd=Math.sqrt(((GumbelDistribution)distribution).getNumericalVariance());
		return getDistribution(mean,sd);
	}

	@Override
	protected AbstractRealDistribution setStandardDeviationInt(AbstractRealDistribution distribution, double sd) {
		final double mean=((GumbelDistribution)distribution).getNumericalMean();
		return getDistribution(mean,sd);
	}

	@Override
	protected double getParameterInt(AbstractRealDistribution distribution, int nr) {
		if (nr==1) return ((GumbelDistribution)distribution).getNumericalMean();
		if (nr==2) return Math.sqrt(((GumbelDistribution)distribution).getNumericalVariance());
		return 0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(AbstractRealDistribution distribution, int nr, double value) {
		if (nr==1) return getDistribution(value,Math.sqrt(((GumbelDistribution)distribution).getNumericalVariance()));
		if (nr==2) return getDistribution(((GumbelDistribution)distribution).getNumericalMean(),value);
		return null;
	}

	@Override
	protected String getToStringData(AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(((GumbelDistribution)distribution).getNumericalMean())+";"+NumberTools.formatSystemNumber(Math.sqrt(((GumbelDistribution)distribution).getNumericalVariance()));
	}

	@Override
	public AbstractRealDistribution fromString(String data, double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=2) return null;
		return getDistribution(values[0],values[1]);
	}

	@Override
	protected AbstractRealDistribution cloneInt(AbstractRealDistribution distribution) {
		return new GumbelDistribution(((GumbelDistribution)distribution).getLocation(),((GumbelDistribution)distribution).getScale());
	}

	@Override
	protected boolean compareInt(AbstractRealDistribution distribution1, AbstractRealDistribution distribution2) {
		if (Math.abs(((GumbelDistribution)distribution1).getLocation()-((GumbelDistribution)distribution2).getLocation())>DistributionTools.MAX_ERROR) return false;
		if (Math.abs(((GumbelDistribution)distribution1).getScale()-((GumbelDistribution)distribution2).getScale())>DistributionTools.MAX_ERROR) return false;
		return true;
	}
}

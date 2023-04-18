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
import org.apache.commons.math3.distribution.NormalDistribution;

import mathtools.NumberTools;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link NormalDistribution}
 * @author Alexander Herzog
 * @see NormalDistribution
 * @see DistributionTools
 */
public class WrapperNormalDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperNormalDistribution() {
		super(NormalDistribution.class,true,true);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistNormal;
	}

	@Override
	protected String getThumbnailImageName() {
		return "normal.png";
	}

	@Override
	protected String getWikipediaURL() {
		return DistributionTools.DistNormalWikipedia;
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(AbstractRealDistribution distribution) {
		final double mean=((NormalDistribution)distribution).getMean();
		return new DistributionWrapperInfo(distribution,0.0,mean);  /* Schiefe=0 immer */
	}

	@Override
	public AbstractRealDistribution getDistribution(double mean, double sd) {
		return new NormalDistribution(mean,sd);
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
		return ((NormalDistribution)distribution).getMean();
	}

	@Override
	protected AbstractRealDistribution setMeanInt(AbstractRealDistribution distribution, double mean) {
		return new NormalDistribution(mean,((NormalDistribution)distribution).getStandardDeviation());
	}

	@Override
	public double getStandardDeviation(final AbstractRealDistribution distribution) {
		return ((NormalDistribution)distribution).getStandardDeviation();
	}

	@Override
	protected AbstractRealDistribution setStandardDeviationInt(AbstractRealDistribution distribution, double sd) {
		return new NormalDistribution(((NormalDistribution)distribution).getMean(),sd);
	}

	@Override
	protected double getParameterInt(AbstractRealDistribution distribution, int nr) {
		if (nr==1) return ((NormalDistribution)distribution).getMean();
		if (nr==2) return ((NormalDistribution)distribution).getStandardDeviation();
		return 0.0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(AbstractRealDistribution distribution, int nr, double value) {
		if (nr==1) return new NormalDistribution(null,value,((NormalDistribution)distribution).getStandardDeviation(),NormalDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
		if (nr==2) return new NormalDistribution(null,((NormalDistribution)distribution).getMean(),value,NormalDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
		return null;
	}

	@Override
	protected String getToStringData(AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(((NormalDistribution)distribution).getMean())+";"+NumberTools.formatSystemNumber(((NormalDistribution)distribution).getStandardDeviation());
	}

	@Override
	public AbstractRealDistribution fromString(String data, double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=2) return null;
		return new NormalDistribution(null,values[0],values[1],NormalDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
	}

	@Override
	protected AbstractRealDistribution cloneInt(AbstractRealDistribution distribution) {
		return new NormalDistribution(null,((NormalDistribution)distribution).getMean(),((NormalDistribution)distribution).getStandardDeviation(),NormalDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
	}

	@Override
	protected boolean compareInt(AbstractRealDistribution distribution1, AbstractRealDistribution distribution2) {
		if (Math.abs(((NormalDistribution)distribution1).getMean()-((NormalDistribution)distribution2).getMean())>DistributionTools.MAX_ERROR) return false;
		if (Math.abs(((NormalDistribution)distribution1).getStandardDeviation()-((NormalDistribution)distribution2).getStandardDeviation())>DistributionTools.MAX_ERROR) return false;
		return true;
	}
}

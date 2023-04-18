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
import mathtools.distribution.OnePointDistributionImpl;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link OnePointDistributionImpl}
 * @author Alexander Herzog
 * @see OnePointDistributionImpl
 * @see DistributionTools
 */
public class WrapperOnePointDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperOnePointDistribution() {
		super(OnePointDistributionImpl.class,true);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistPoint;
	}

	@Override
	protected String getThumbnailImageName() {
		return "point.png";
	}

	@Override
	protected String getWikipediaURL() {
		return DistributionTools.DistPointWikipedia;
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(final AbstractRealDistribution distribution) {
		final double point=((OnePointDistributionImpl)distribution).point;
		return new DistributionWrapperInfo(distribution,0.0,point);
	}

	@Override
	public AbstractRealDistribution getDistribution(final double mean, final double sd) {
		return new OnePointDistributionImpl(mean);
	}

	@Override
	public AbstractRealDistribution getDefaultDistribution() {
		return getDistribution(100,100);
	}

	@Override
	public AbstractRealDistribution getDistributionForFit(final double mean, final double sd, final double min, final double max) {
		if (sd>=0.000001) return null;
		return super.getDistributionForFit(mean,sd,min,max);
	}

	@Override
	public double getMean(final AbstractRealDistribution distribution) {
		return ((OnePointDistributionImpl)distribution).point;
	}

	@Override
	protected AbstractRealDistribution setMeanInt(final AbstractRealDistribution distribution, final double mean) {
		return new OnePointDistributionImpl(mean);
	}

	@Override
	public double getStandardDeviation(final AbstractRealDistribution distribution) {
		return 0;
	}

	@Override
	protected AbstractRealDistribution setStandardDeviationInt(final AbstractRealDistribution distribution, final double sd) {
		return null;
	}

	@Override
	protected double getParameterInt(final AbstractRealDistribution distribution, final int nr) {
		if (nr==1) return ((OnePointDistributionImpl)distribution).point;
		return 0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(final AbstractRealDistribution distribution, final int nr, final double value) {
		if (nr==1) return new OnePointDistributionImpl(value);
		return null;
	}

	@Override
	protected String getToStringData(final AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(((OnePointDistributionImpl)distribution).point);
	}

	@Override
	public AbstractRealDistribution fromString(final String data, final double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=1) return null;
		return new OnePointDistributionImpl(values[0]);
	}

	@Override
	protected AbstractRealDistribution cloneInt(final AbstractRealDistribution distribution) {
		return ((OnePointDistributionImpl)distribution).clone();
	}

	@Override
	protected boolean compareInt(final AbstractRealDistribution distribution1, final AbstractRealDistribution distribution2) {
		return Math.abs(((OnePointDistributionImpl)distribution1).point-((OnePointDistributionImpl)distribution2).point)<=DistributionTools.MAX_ERROR;
	}
}

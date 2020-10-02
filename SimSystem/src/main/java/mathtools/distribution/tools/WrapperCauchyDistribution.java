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
import org.apache.commons.math3.distribution.CauchyDistribution;

import mathtools.NumberTools;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link CauchyDistribution}
 * @author Alexander Herzog
 * @see CauchyDistribution
 * @see DistributionTools
 */
public class WrapperCauchyDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperCauchyDistribution() {
		super(CauchyDistribution.class);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistCauchy;
	}

	@Override
	protected String getThumbnailImageName() {
		return "cauchy.png";
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(AbstractRealDistribution distribution) {
		final String info2=DistributionTools.DistScale+"="+NumberTools.formatNumber(((CauchyDistribution)distribution).getScale(),3);
		return new DistributionWrapperInfo(((CauchyDistribution)distribution).getMedian(),null,null,info2);
	}

	@Override
	public AbstractRealDistribution getDistribution(double mean, double sd) {
		return null;
	}

	@Override
	public AbstractRealDistribution getDefaultDistribution() {
		return new CauchyDistribution(100,20);
	}

	@Override
	public AbstractRealDistribution getDistributionForFit(final double mean, final double sd) {
		if (sd<=0) return null;
		return super.getDistributionForFit(mean,sd);
	}

	@Override
	public double getMean(final AbstractRealDistribution distribution) {
		return ((CauchyDistribution)distribution).getMedian(); /* Nicht so ganz exakt, aber immerhin irgendwas */
	}

	@Override
	protected AbstractRealDistribution setMeanInt(AbstractRealDistribution distribution, double mean) {
		return null;
	}

	@Override
	protected AbstractRealDistribution setStandardDeviationInt(AbstractRealDistribution distribution, double sd) {
		return null;
	}

	@Override
	protected double getParameterInt(AbstractRealDistribution distribution, int nr) {
		if (nr==1) return ((CauchyDistribution)distribution).getMedian();
		if (nr==2) return ((CauchyDistribution)distribution).getScale();
		return 0.0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(AbstractRealDistribution distribution, int nr, double value) {
		if (nr==1) return new CauchyDistribution(null,value,((CauchyDistribution)distribution).getScale(),CauchyDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
		if (nr==2) return new CauchyDistribution(null,((CauchyDistribution)distribution).getMedian(),value,CauchyDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
		return null;
	}

	@Override
	protected String getToStringData(AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(((CauchyDistribution)distribution).getMedian())+";"+NumberTools.formatSystemNumber(((CauchyDistribution)distribution).getScale());
	}

	@Override
	public AbstractRealDistribution fromString(String data, double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=2) return null;
		return new CauchyDistribution(null,values[0],values[1],CauchyDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
	}

	@Override
	protected AbstractRealDistribution cloneInt(AbstractRealDistribution distribution) {
		return new CauchyDistribution(null,((CauchyDistribution)distribution).getMedian(),((CauchyDistribution)distribution).getScale(),CauchyDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
	}

	@Override
	protected boolean compareInt(AbstractRealDistribution distribution1, AbstractRealDistribution distribution2) {
		if (Math.abs(((CauchyDistribution)distribution1).getMedian()-((CauchyDistribution)distribution2).getMedian())>DistributionTools.MAX_ERROR) return false;
		if (Math.abs(((CauchyDistribution)distribution1).getScale()-((CauchyDistribution)distribution2).getScale())>DistributionTools.MAX_ERROR) return false;
		return true;
	}
}

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
import mathtools.distribution.SawtoothRightDistribution;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link SawtoothRightDistribution}
 * @author Alexander Herzog
 * @see SawtoothRightDistribution
 * @see DistributionTools
 */
public class WrapperSawtoothRightDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperSawtoothRightDistribution() {
		super(SawtoothRightDistribution.class,true,true);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistSawtoothRight;
	}

	@Override
	protected String getThumbnailImageName() {
		return "sawtooth_right.png";
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(AbstractRealDistribution distribution) {
		final SawtoothRightDistribution sawtooth=(SawtoothRightDistribution)distribution;
		final double A=sawtooth.a;
		final double B=sawtooth.b;
		final String info=DistributionTools.DistRange+"=["+NumberTools.formatNumber(A,3)+";"+NumberTools.formatNumber(B,3)+"]";
		return new DistributionWrapperInfo(distribution,sawtooth.getSkewness(),B,info,null);
	}

	@Override
	public AbstractRealDistribution getDistribution(double mean, double sd) {
		if (sd<=0) return null;
		final double a=mean-2*Math.sqrt(2)*sd;
		final double b=mean+Math.sqrt(2)*sd;
		return new SawtoothRightDistribution(a,b);
	}

	@Override
	public AbstractRealDistribution getDefaultDistribution() {
		return new SawtoothRightDistribution(5,10);
	}

	@Override
	public AbstractRealDistribution getDistributionForFit(final double mean, final double sd, final double min, final double max) {
		if (sd<=0) return null;
		if (min>=max) return null;
		return new SawtoothRightDistribution(min,max);
	}

	@Override
	protected AbstractRealDistribution setMeanInt(AbstractRealDistribution distribution, double mean) {
		return getDistribution(mean,Math.sqrt(((SawtoothRightDistribution)distribution).getNumericalVariance()));
	}

	@Override
	protected AbstractRealDistribution setStandardDeviationInt(AbstractRealDistribution distribution, double sd) {
		return getDistribution(((SawtoothRightDistribution)distribution).getNumericalMean(),sd);
	}

	@Override
	protected double getParameterInt(AbstractRealDistribution distribution, int nr) {
		if (nr==1) return ((SawtoothRightDistribution)distribution).a;
		if (nr==2) return ((SawtoothRightDistribution)distribution).b;
		return 0.0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(AbstractRealDistribution distribution, int nr, double value) {
		final SawtoothRightDistribution dist=(SawtoothRightDistribution)distribution;
		if (nr==1) return new SawtoothRightDistribution(value,dist.b);
		if (nr==2) return new SawtoothRightDistribution(dist.a,value);
		return null;
	}

	@Override
	protected String getToStringData(AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(((SawtoothRightDistribution)distribution).a)+";"+NumberTools.formatSystemNumber(((SawtoothRightDistribution)distribution).b);
	}

	@Override
	public AbstractRealDistribution fromString(String data, double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=2) return null;
		return new SawtoothRightDistribution(values[0],values[1]);
	}

	@Override
	protected AbstractRealDistribution cloneInt(AbstractRealDistribution distribution) {
		return ((SawtoothRightDistribution)distribution).clone();
	}

	@Override
	protected boolean compareInt(AbstractRealDistribution distribution1, AbstractRealDistribution distribution2) {
		if (Math.abs(((SawtoothRightDistribution)distribution1).a-((SawtoothRightDistribution)distribution2).a)>DistributionTools.MAX_ERROR) return false;
		if (Math.abs(((SawtoothRightDistribution)distribution1).b-((SawtoothRightDistribution)distribution2).b)>DistributionTools.MAX_ERROR) return false;
		return true;
	}
}
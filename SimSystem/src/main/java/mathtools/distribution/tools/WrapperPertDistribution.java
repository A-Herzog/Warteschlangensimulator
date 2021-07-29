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
import mathtools.distribution.PertDistributionImpl;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link PertDistributionImpl}
 * @author Alexander Herzog
 * @see PertDistributionImpl
 * @see DistributionTools
 */
public class WrapperPertDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperPertDistribution() {
		super(PertDistributionImpl.class,true);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistPert;
	}

	@Override
	protected String getThumbnailImageName() {
		return "pert.png";
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(AbstractRealDistribution distribution) {
		final PertDistributionImpl pertDist=(PertDistributionImpl)distribution;
		final double A=pertDist.lowerBound;
		final double B=pertDist.mostLikely;
		final double C=pertDist.upperBound;
		final String info=DistributionTools.DistRange+"=["+NumberTools.formatNumber(A,3)+";"+NumberTools.formatNumber(C,3)+"]; "+DistributionTools.DistMostLikely+"="+NumberTools.formatNumber(B,3);
		return new DistributionWrapperInfo(distribution,pertDist.getSkewness(),info,null);
	}

	@Override
	public AbstractRealDistribution getDistribution(double mean, double sd) {
		return new PertDistributionImpl(mean-Math.sqrt(7)*sd,mean,mean+Math.sqrt(7)*sd);
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
	protected AbstractRealDistribution setMeanInt(AbstractRealDistribution distribution, double mean) {
		final double sd=Math.sqrt(((PertDistributionImpl)distribution).getNumericalVariance());
		return new PertDistributionImpl(mean-sd*Math.sqrt(7),mean,mean+sd*Math.sqrt(7));
	}

	@Override
	protected AbstractRealDistribution setStandardDeviationInt(AbstractRealDistribution distribution, double sd) {
		return null;
	}

	@Override
	protected double getParameterInt(AbstractRealDistribution distribution, int nr) {
		if (nr==1) return ((PertDistributionImpl)distribution).lowerBound;
		if (nr==2) return ((PertDistributionImpl)distribution).mostLikely;
		if (nr==3) return ((PertDistributionImpl)distribution).upperBound;
		return 0.0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(AbstractRealDistribution distribution, int nr, double value) {
		final PertDistributionImpl t=(PertDistributionImpl)distribution;
		if (nr==1) return new PertDistributionImpl(value,t.mostLikely,t.upperBound);
		if (nr==2) return new PertDistributionImpl(t.lowerBound,value,t.upperBound);
		if (nr==3) return new PertDistributionImpl(t.lowerBound,t.mostLikely,value);
		return null;
	}

	@Override
	protected String getToStringData(AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(((PertDistributionImpl)distribution).lowerBound)+";"+NumberTools.formatSystemNumber(((PertDistributionImpl)distribution).mostLikely)+";"+NumberTools.formatSystemNumber(((PertDistributionImpl)distribution).upperBound);
	}

	@Override
	public AbstractRealDistribution fromString(String data, double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=3) return null;
		return new PertDistributionImpl(values[0],values[1],values[2]);
	}

	@Override
	protected AbstractRealDistribution cloneInt(AbstractRealDistribution distribution) {
		return ((PertDistributionImpl)distribution).clone();
	}

	@Override
	protected boolean compareInt(AbstractRealDistribution distribution1, AbstractRealDistribution distribution2) {
		if (Math.abs(((PertDistributionImpl)distribution1).lowerBound-((PertDistributionImpl)distribution2).lowerBound)>DistributionTools.MAX_ERROR) return false;
		if (Math.abs(((PertDistributionImpl)distribution1).upperBound-((PertDistributionImpl)distribution2).upperBound)>DistributionTools.MAX_ERROR) return false;
		if (Math.abs(((PertDistributionImpl)distribution1).mostLikely-((PertDistributionImpl)distribution2).mostLikely)>DistributionTools.MAX_ERROR) return false;
		return true;
	}
}
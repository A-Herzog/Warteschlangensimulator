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
import org.apache.commons.math3.distribution.ChiSquaredDistribution;

import mathtools.NumberTools;
import parser.coresymbols.CalcSymbolPreOperator;
import parser.symbols.distributions.CalcSymbolDistributionChiSquare;

/**
 * Zus�tzliche Daten f�r ein Objekt vom Typ {@link ChiSquaredDistribution}
 * @author Alexander Herzog
 * @see ChiSquaredDistribution
 * @see DistributionTools
 */
public class WrapperChiSquaredDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperChiSquaredDistribution() {
		super(ChiSquaredDistribution.class,true,true);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistChiSquare;
	}

	@Override
	protected String getThumbnailImageName() {
		return "chisquare.png";
	}

	@Override
	protected String getWikipediaURL() {
		return DistributionTools.DistChiSquareWikipedia;
	}

	@Override
	protected String getWebAppDistributionName() {
		return "Chi2";
	}

	@Override
	protected String getInfoHTML() {
		return null;
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(AbstractRealDistribution distribution) {
		final ChiSquaredDistribution dist=(ChiSquaredDistribution)distribution;
		final double n=dist.getDegreesOfFreedom();
		final double sk=2*Math.sqrt(2)/Math.sqrt(n);
		final double mode=Math.max(n-2,0);
		final String info1=DistributionTools.DistDegreesOfFreedom+"="+NumberTools.formatNumber(n);
		return new DistributionWrapperInfo(distribution,sk,mode,info1,null);
	}

	@Override
	public AbstractRealDistribution getDistribution(double mean, double sd) {
		return new ChiSquaredDistribution(Math.round(mean));
	}

	@Override
	protected boolean canSetMeanExact() {
		return false;
	}

	@Override
	protected boolean canSetStandardDeviationExact() {
		return false;
	}

	@Override
	public AbstractRealDistribution getDefaultDistribution() {
		return getDistribution(100,100);
	}

	@Override
	public AbstractRealDistribution getDistributionForFit(final double mean, final double sd, final double min, final double max) {
		if (mean<=0) return null;
		return super.getDistributionForFit(mean,sd,min,max);
	}

	@Override
	public double getMean(final AbstractRealDistribution distribution) {
		return ((ChiSquaredDistribution)distribution).getDegreesOfFreedom();
	}

	@Override
	protected AbstractRealDistribution setMeanInt(AbstractRealDistribution distribution, double mean) {
		return new ChiSquaredDistribution(mean);
	}

	@Override
	public double getStandardDeviation(final AbstractRealDistribution distribution) {
		return Math.sqrt(2*((ChiSquaredDistribution)distribution).getDegreesOfFreedom());
	}

	@Override
	protected AbstractRealDistribution setStandardDeviationInt(AbstractRealDistribution distribution, double sd) {
		return new ChiSquaredDistribution(sd*sd/2);
	}

	@Override
	protected double getParameterInt(AbstractRealDistribution distribution, int nr) {
		if (nr==1) return ((ChiSquaredDistribution)distribution).getDegreesOfFreedom();
		return 0.0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(AbstractRealDistribution distribution, int nr, double value) {
		if (nr==1) return new ChiSquaredDistribution(null,value,ChiSquaredDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
		return null;
	}

	@Override
	protected String getToStringData(AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(((ChiSquaredDistribution)distribution).getDegreesOfFreedom());
	}

	@Override
	public AbstractRealDistribution fromString(String data, double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=1) return null;
		return new ChiSquaredDistribution(null,values[0],ChiSquaredDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
	}

	@Override
	protected AbstractRealDistribution cloneInt(AbstractRealDistribution distribution) {
		return new ChiSquaredDistribution(null,((ChiSquaredDistribution)distribution).getDegreesOfFreedom(),ChiSquaredDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
	}

	@Override
	protected boolean compareInt(AbstractRealDistribution distribution1, AbstractRealDistribution distribution2) {
		if (Math.abs(((ChiSquaredDistribution)distribution1).getDegreesOfFreedom()-((ChiSquaredDistribution)distribution2).getDegreesOfFreedom())>DistributionTools.MAX_ERROR) return false;
		return true;
	}

	@Override
	protected Class<? extends CalcSymbolPreOperator> getCalcSymbolClass() {
		return CalcSymbolDistributionChiSquare.class;
	}
}

/**
 * Copyright 2024 Alexander Herzog
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
import mathtools.distribution.ArcsineDistribution;
import parser.coresymbols.CalcSymbolPreOperator;
import parser.symbols.distributions.CalcSymbolDistributionArcsine;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link ArcsineDistribution}
 * @author Alexander Herzog
 * @see ArcsineDistribution
 * @see DistributionTools
 */
public class WrapperArcsineDistribution  extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperArcsineDistribution() {
		super(ArcsineDistribution.class,true,false);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistArcsine;
	}

	@Override
	protected String getThumbnailImageName() {
		return "arcsine.png";
	}

	@Override
	protected String getWikipediaURL() {
		return DistributionTools.DistArcsineWikipedia;
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(final AbstractRealDistribution distribution) {
		return new DistributionWrapperInfo(distribution,0.0,null); /* Schiefe=0 immer */
	}

	@Override
	public AbstractRealDistribution getDistribution(final double mean, final double sd) {
		/*
		mean=(a+b)/2
		sd=1/8*(b-a)

		a=2*mean-b
		sd=1/8*(2*b-2*mean)

		4*sd+mean=b
		 */
		final double b=4.0*sd+mean;
		final double a=2.0*mean-b;
		return new ArcsineDistribution(a,b);
	}

	@Override
	public AbstractRealDistribution getDefaultDistribution() {
		return getDistribution(50,100);
	}

	@Override
	public AbstractRealDistribution getDistributionForFit(final double mean, final double sd, final double min, final double max) {
		return new ArcsineDistribution(min,max);
	}

	@Override
	public double getMean(final AbstractRealDistribution distribution) {
		return ((ArcsineDistribution)distribution).getNumericalMean();
	}

	@Override
	protected AbstractRealDistribution setMeanInt(final AbstractRealDistribution distribution, final double mean) {
		final ArcsineDistribution oldDistribution=(ArcsineDistribution)distribution;

		final double delta=mean-oldDistribution.getNumericalMean();
		final double a=oldDistribution.getSupportLowerBound();
		final double b=oldDistribution.getSupportUpperBound();
		return new ArcsineDistribution(a+delta,b+delta);
	}

	@Override
	public double getStandardDeviation(final AbstractRealDistribution distribution) {
		return Math.sqrt(((ArcsineDistribution)distribution).getNumericalVariance());
	}

	@Override
	protected AbstractRealDistribution setStandardDeviationInt(final AbstractRealDistribution distribution, final double sd) {
		return null;
	}

	@Override
	protected double getParameterInt(final AbstractRealDistribution distribution, final int nr) {
		if (nr==1) return ((ArcsineDistribution)distribution).getSupportLowerBound();
		if (nr==2) return ((ArcsineDistribution)distribution).getSupportUpperBound();
		return 0.0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(final AbstractRealDistribution distribution, final int nr, final double value) {
		if (nr==1) return new ArcsineDistribution(value,((ArcsineDistribution)distribution).getSupportUpperBound());
		if (nr==2) return new ArcsineDistribution(((ArcsineDistribution)distribution).getSupportLowerBound(),value);
		return null;
	}

	@Override
	protected String getToStringData(final AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(((ArcsineDistribution)distribution).getSupportLowerBound())+";"+NumberTools.formatSystemNumber(((ArcsineDistribution)distribution).getSupportUpperBound());
	}

	@Override
	public AbstractRealDistribution fromString(final String data, final double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=2) return null;
		return new ArcsineDistribution(values[0],values[1]);
	}

	@Override
	protected AbstractRealDistribution cloneInt(final AbstractRealDistribution distribution) {
		return new ArcsineDistribution(((ArcsineDistribution)distribution).getSupportLowerBound(),((ArcsineDistribution)distribution).getSupportUpperBound());
	}

	@Override
	protected boolean compareInt(final AbstractRealDistribution distribution1, final AbstractRealDistribution distribution2) {
		if (Math.abs(((ArcsineDistribution)distribution1).getSupportLowerBound()-((ArcsineDistribution)distribution2).getSupportLowerBound())>DistributionTools.MAX_ERROR) return false;
		if (Math.abs(((ArcsineDistribution)distribution1).getSupportUpperBound()-((ArcsineDistribution)distribution2).getSupportUpperBound())>DistributionTools.MAX_ERROR) return false;
		return true;
	}

	@Override
	protected Class<? extends CalcSymbolPreOperator> getCalcSymbolClass() {
		return CalcSymbolDistributionArcsine.class;
	}
}

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
import mathtools.distribution.UQuadraticDistribution;
import parser.coresymbols.CalcSymbolPreOperator;
import parser.symbols.distributions.CalcSymbolDistributionUQuadratic;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link UQuadraticDistribution}
 * @author Alexander Herzog
 * @see UQuadraticDistribution
 * @see DistributionTools
 */
public class WrapperUQuadraticDistribution  extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperUQuadraticDistribution() {
		super(UQuadraticDistribution.class,true,true);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistUQuadratic;
	}

	@Override
	protected String getThumbnailImageName() {
		return "uquadratic.png";
	}

	@Override
	protected String getWikipediaURL() {
		return DistributionTools.DistUQuadraticWikipedia;
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(final AbstractRealDistribution distribution) {
		return new DistributionWrapperInfo(distribution,0.0,null); /* Schiefe=0 immer */
	}

	@Override
	public AbstractRealDistribution getDistribution(final double mean, final double sd) {
		/*
		mean=(a+b)/2
		sd=sqrt(3/20)*(b-a)

		mean*2-a=b
		sd*sqrt(20/3)+a=b

		a=mean-sd*sqrt(20/3)/2
		 */
		final double a=mean-sd*Math.sqrt(20/3)/2;
		double b=2*mean-a;
		if (b<=a) b=b+0.01;
		return new UQuadraticDistribution(a,b);
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
		return ((UQuadraticDistribution)distribution).getNumericalMean();
	}

	@Override
	protected AbstractRealDistribution setMeanInt(final AbstractRealDistribution distribution, final double mean) {
		final UQuadraticDistribution oldDistribution=(UQuadraticDistribution)distribution;

		final double delta=mean-oldDistribution.getNumericalMean();
		final double a=oldDistribution.getSupportLowerBound();
		final double b=oldDistribution.getSupportUpperBound();
		return new UQuadraticDistribution(a+delta,b+delta);
	}

	@Override
	public double getStandardDeviation(final AbstractRealDistribution distribution) {
		return Math.sqrt(((UQuadraticDistribution)distribution).getNumericalVariance());
	}

	@Override
	protected AbstractRealDistribution setStandardDeviationInt(final AbstractRealDistribution distribution, final double sd) {
		final UQuadraticDistribution oldDistribution=(UQuadraticDistribution)distribution;

		final double E=oldDistribution.getNumericalMean();
		final double var=sd*sd;
		final double range=Math.sqrt(var*20/3);
		final double newA=E-range/2;
		final double newB=E+range/2;
		return new UQuadraticDistribution(newA,newB);
	}

	@Override
	protected double getParameterInt(final AbstractRealDistribution distribution, final int nr) {
		if (nr==1) return ((UQuadraticDistribution)distribution).getSupportLowerBound();
		if (nr==2) return ((UQuadraticDistribution)distribution).getSupportUpperBound();
		return 0.0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(final AbstractRealDistribution distribution, final int nr, final double value) {
		if (nr==1) return new UQuadraticDistribution(value,((UQuadraticDistribution)distribution).getSupportUpperBound());
		if (nr==2) return new UQuadraticDistribution(((UQuadraticDistribution)distribution).getSupportLowerBound(),value);
		return null;
	}

	@Override
	protected String getToStringData(final AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(((UQuadraticDistribution)distribution).getSupportLowerBound())+";"+NumberTools.formatSystemNumber(((UQuadraticDistribution)distribution).getSupportUpperBound());
	}

	@Override
	public AbstractRealDistribution fromString(final String data, final double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=2) return null;
		return new UQuadraticDistribution(values[0],values[1]);
	}

	@Override
	protected AbstractRealDistribution cloneInt(final AbstractRealDistribution distribution) {
		return new UQuadraticDistribution(((UQuadraticDistribution)distribution).getSupportLowerBound(),((UQuadraticDistribution)distribution).getSupportUpperBound());
	}

	@Override
	protected boolean compareInt(final AbstractRealDistribution distribution1, final AbstractRealDistribution distribution2) {
		if (Math.abs(((UQuadraticDistribution)distribution1).getSupportLowerBound()-((UQuadraticDistribution)distribution2).getSupportLowerBound())>DistributionTools.MAX_ERROR) return false;
		if (Math.abs(((UQuadraticDistribution)distribution1).getSupportUpperBound()-((UQuadraticDistribution)distribution2).getSupportUpperBound())>DistributionTools.MAX_ERROR) return false;
		return true;
	}

	@Override
	protected Class<? extends CalcSymbolPreOperator> getCalcSymbolClass() {
		return CalcSymbolDistributionUQuadratic.class;
	}
}

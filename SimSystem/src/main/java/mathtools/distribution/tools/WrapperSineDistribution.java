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
import mathtools.distribution.SineDistribution;
import parser.coresymbols.CalcSymbolPreOperator;
import parser.symbols.distributions.CalcSymbolDistributionSine;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link SineDistribution}
 * @author Alexander Herzog
 * @see SineDistribution
 * @see DistributionTools
 */
public class WrapperSineDistribution  extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperSineDistribution() {
		super(SineDistribution.class,true,false);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistSine;
	}

	@Override
	protected String getThumbnailImageName() {
		return "sine.png";
	}

	@Override
	protected String getWikipediaURL() {
		return DistributionTools.DistSineWikipedia;
	}

	@Override
	protected String getWebAppDistributionName() {
		return "Sine";
	}

	@Override
	protected String getInfoHTML() {
		return null;
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(final AbstractRealDistribution distribution) {
		return new DistributionWrapperInfo(distribution,0.0,distribution.getNumericalMean()); /* Schiefe=0 immer */
	}

	@Override
	public AbstractRealDistribution getDistribution(final double mean, final double sd) {
		/*
		mean=(a+b)/2
		sd=sqrt(1/4-2/pi^2)*(b-a)

		a=2*mean-b
		sd=sqrt(1/4-2/pi^2)*(2*b-2*mean)

		sd/sqrt(1/4-2/pi^2)/2+mean=b
		 */
		final double b=sd/Math.sqrt(1.0/4.0-2.0/Math.pow(Math.PI,2))/2.0+mean;
		final double a=2.0*mean-b;
		return new SineDistribution(a,b);
	}

	@Override
	public AbstractRealDistribution getDefaultDistribution() {
		return getDistribution(50,100);
	}

	@Override
	public AbstractRealDistribution getDistributionForFit(final double mean, final double sd, final double min, final double max) {
		return new SineDistribution(min,max);
	}

	@Override
	public double getMean(final AbstractRealDistribution distribution) {
		return ((SineDistribution)distribution).getNumericalMean();
	}

	@Override
	protected AbstractRealDistribution setMeanInt(final AbstractRealDistribution distribution, final double mean) {
		final SineDistribution oldDistribution=(SineDistribution)distribution;

		final double delta=mean-oldDistribution.getNumericalMean();
		final double a=oldDistribution.getSupportLowerBound();
		final double b=oldDistribution.getSupportUpperBound();
		return new SineDistribution(a+delta,b+delta);
	}

	@Override
	public double getStandardDeviation(final AbstractRealDistribution distribution) {
		return Math.sqrt(((SineDistribution)distribution).getNumericalVariance());
	}

	@Override
	protected AbstractRealDistribution setStandardDeviationInt(final AbstractRealDistribution distribution, final double sd) {
		return null;
	}

	@Override
	protected double getParameterInt(final AbstractRealDistribution distribution, final int nr) {
		if (nr==1) return ((SineDistribution)distribution).getSupportLowerBound();
		if (nr==2) return ((SineDistribution)distribution).getSupportUpperBound();
		return 0.0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(final AbstractRealDistribution distribution, final int nr, final double value) {
		if (nr==1) return new SineDistribution(value,((SineDistribution)distribution).getSupportUpperBound());
		if (nr==2) return new SineDistribution(((SineDistribution)distribution).getSupportLowerBound(),value);
		return null;
	}

	@Override
	protected String getToStringData(final AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(((SineDistribution)distribution).getSupportLowerBound())+";"+NumberTools.formatSystemNumber(((SineDistribution)distribution).getSupportUpperBound());
	}

	@Override
	public AbstractRealDistribution fromString(final String data, final double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=2) return null;
		return new SineDistribution(values[0],values[1]);
	}

	@Override
	protected AbstractRealDistribution cloneInt(final AbstractRealDistribution distribution) {
		return new SineDistribution(((SineDistribution)distribution).getSupportLowerBound(),((SineDistribution)distribution).getSupportUpperBound());
	}

	@Override
	protected boolean compareInt(final AbstractRealDistribution distribution1, final AbstractRealDistribution distribution2) {
		if (Math.abs(((SineDistribution)distribution1).getSupportLowerBound()-((SineDistribution)distribution2).getSupportLowerBound())>DistributionTools.MAX_ERROR) return false;
		if (Math.abs(((SineDistribution)distribution1).getSupportUpperBound()-((SineDistribution)distribution2).getSupportUpperBound())>DistributionTools.MAX_ERROR) return false;
		return true;
	}

	@Override
	protected Class<? extends CalcSymbolPreOperator> getCalcSymbolClass() {
		return CalcSymbolDistributionSine.class;
	}
}

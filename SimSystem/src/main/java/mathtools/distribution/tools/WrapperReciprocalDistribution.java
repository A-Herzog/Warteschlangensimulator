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
import mathtools.distribution.ReciprocalDistribution;
import parser.coresymbols.CalcSymbolPreOperator;
import parser.symbols.distributions.CalcSymbolDistributionReciprocal;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link ReciprocalDistribution}
 * @author Alexander Herzog
 * @see ReciprocalDistribution
 * @see DistributionTools
 */
public class WrapperReciprocalDistribution  extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperReciprocalDistribution() {
		super(ReciprocalDistribution.class,false,false);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistReciprocal;
	}

	@Override
	protected String getThumbnailImageName() {
		return "reciprocal.png";
	}

	@Override
	protected String getWikipediaURL() {
		return DistributionTools.DistReciprocalWikipedia;
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(final AbstractRealDistribution distribution) {
		return new DistributionWrapperInfo(distribution,null,((ReciprocalDistribution)distribution).getSupportLowerBound());
	}

	@Override
	public AbstractRealDistribution getDistribution(double mean, double sd) {
		return null;
	}

	@Override
	public AbstractRealDistribution getDefaultDistribution() {
		return new ReciprocalDistribution(50,100);
	}

	@Override
	public AbstractRealDistribution getDistributionForFit(final double mean, final double sd, final double min, final double max) {
		if (mean<=0 || sd<=0 || min<=0 || max<=min) return null;
		return new ReciprocalDistribution(min,max);
	}

	@Override
	public double getMean(final AbstractRealDistribution distribution) {
		return ((ReciprocalDistribution)distribution).getNumericalMean();
	}

	@Override
	protected AbstractRealDistribution setMeanInt(final AbstractRealDistribution distribution, final double mean) {
		return null;
	}

	@Override
	public double getStandardDeviation(final AbstractRealDistribution distribution) {
		return Math.sqrt(((ReciprocalDistribution)distribution).getNumericalVariance());
	}

	@Override
	protected AbstractRealDistribution setStandardDeviationInt(final AbstractRealDistribution distribution, final double sd) {
		return null;
	}

	@Override
	protected double getParameterInt(final AbstractRealDistribution distribution, final int nr) {
		if (nr==1) return ((ReciprocalDistribution)distribution).getSupportLowerBound();
		if (nr==2) return ((ReciprocalDistribution)distribution).getSupportUpperBound();
		return 0.0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(final AbstractRealDistribution distribution, final int nr, final double value) {
		if (nr==1) return new ReciprocalDistribution(value,((ReciprocalDistribution)distribution).getSupportUpperBound());
		if (nr==2) return new ReciprocalDistribution(((ReciprocalDistribution)distribution).getSupportLowerBound(),value);
		return null;
	}

	@Override
	protected String getToStringData(final AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(((ReciprocalDistribution)distribution).getSupportLowerBound())+";"+NumberTools.formatSystemNumber(((ReciprocalDistribution)distribution).getSupportUpperBound());
	}

	@Override
	public AbstractRealDistribution fromString(final String data, final double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=2) return null;
		return new ReciprocalDistribution(values[0],values[1]);
	}

	@Override
	protected AbstractRealDistribution cloneInt(final AbstractRealDistribution distribution) {
		return new ReciprocalDistribution(((ReciprocalDistribution)distribution).getSupportLowerBound(),((ReciprocalDistribution)distribution).getSupportUpperBound());
	}

	@Override
	protected boolean compareInt(final AbstractRealDistribution distribution1, final AbstractRealDistribution distribution2) {
		if (Math.abs(((ReciprocalDistribution)distribution1).getSupportLowerBound()-((ReciprocalDistribution)distribution2).getSupportLowerBound())>DistributionTools.MAX_ERROR) return false;
		if (Math.abs(((ReciprocalDistribution)distribution1).getSupportUpperBound()-((ReciprocalDistribution)distribution2).getSupportUpperBound())>DistributionTools.MAX_ERROR) return false;
		return true;
	}

	@Override
	protected Class<? extends CalcSymbolPreOperator> getCalcSymbolClass() {
		return CalcSymbolDistributionReciprocal.class;
	}
}

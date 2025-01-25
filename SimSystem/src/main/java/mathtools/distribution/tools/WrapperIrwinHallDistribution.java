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

import mathtools.distribution.IrwinHallDistribution;
import parser.coresymbols.CalcSymbolPreOperator;
import parser.symbols.distributions.CalcSymbolDistributionIrwinHall;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link IrwinHallDistribution}
 * @author Alexander Herzog
 * @see IrwinHallDistribution
 * @see DistributionTools
 */
public class WrapperIrwinHallDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperIrwinHallDistribution() {
		super(IrwinHallDistribution.class,true,true);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistIrwinHall;
	}

	@Override
	protected String getThumbnailImageName() {
		return "normal.png";
	}

	@Override
	protected String getWikipediaURL() {
		return DistributionTools.DistIrwinHallWikipedia;
	}

	@Override
	protected String getWebAppDistributionName() {
		return "IrwinHall";
	}

	@Override
	protected String getInfoHTML() {
		return null;
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(final AbstractRealDistribution distribution) {
		Double mode=null;
		final IrwinHallDistribution dist=(IrwinHallDistribution)distribution;
		if (dist.n>1) {
			mode=dist.n/2.0;
		}
		return new DistributionWrapperInfo(distribution,0.0,mode);
	}

	@Override
	public AbstractRealDistribution getDistribution(double mean, double sd) {
		return new IrwinHallDistribution(2*mean);
	}

	@Override
	public AbstractRealDistribution getDefaultDistribution() {
		return new IrwinHallDistribution(5);
	}

	@Override
	public AbstractRealDistribution getDistributionForFit(final double mean, final double sd, final double min, final double max) {
		return new IrwinHallDistribution(2*mean);
	}

	@Override
	public double getMean(final AbstractRealDistribution distribution) {
		return ((IrwinHallDistribution)distribution).getNumericalMean();
	}

	@Override
	protected AbstractRealDistribution setMeanInt(final AbstractRealDistribution distribution, final double mean) {
		return new IrwinHallDistribution(2*mean);
	}

	@Override
	public double getStandardDeviation(final AbstractRealDistribution distribution) {
		return Math.sqrt(((IrwinHallDistribution)distribution).getNumericalVariance());
	}

	@Override
	protected AbstractRealDistribution setStandardDeviationInt(final AbstractRealDistribution distribution, final double sd) {
		return new IrwinHallDistribution(12*sd);
	}

	@Override
	public boolean canSetMeanExact() {
		return false;
	}
	@Override
	public boolean canSetStandardDeviationExact() {
		return false;
	}

	@Override
	protected double getParameterInt(final AbstractRealDistribution distribution, final int nr) {
		if (nr==1) return ((IrwinHallDistribution)distribution).n;
		return 0.0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(final AbstractRealDistribution distribution, final int nr, final double value) {
		if (nr==1) return new IrwinHallDistribution(value);
		return null;
	}

	@Override
	protected String getToStringData(final AbstractRealDistribution distribution) {
		return ""+((IrwinHallDistribution)distribution).n;
	}

	@Override
	public AbstractRealDistribution fromString(final String data, final double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=1) return null;
		return new IrwinHallDistribution(values[0]);
	}

	@Override
	protected AbstractRealDistribution cloneInt(final AbstractRealDistribution distribution) {
		return new IrwinHallDistribution(((IrwinHallDistribution)distribution).n);
	}

	@Override
	protected boolean compareInt(final AbstractRealDistribution distribution1, final AbstractRealDistribution distribution2) {
		if (Math.abs(((IrwinHallDistribution)distribution1).n-((IrwinHallDistribution)distribution2).n)>0) return false;
		return true;
	}

	@Override
	protected Class<? extends CalcSymbolPreOperator> getCalcSymbolClass() {
		return CalcSymbolDistributionIrwinHall.class;
	}
}

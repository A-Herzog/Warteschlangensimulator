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
import mathtools.distribution.ChiDistributionImpl;
import parser.coresymbols.CalcSymbolPreOperator;
import parser.symbols.distributions.CalcSymbolDistributionChi;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link ChiDistributionImpl}
 * @author Alexander Herzog
 * @see ChiDistributionImpl
 * @see DistributionTools
 */
public class WrapperChiDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperChiDistribution() {
		super(ChiDistributionImpl.class);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistChi;
	}

	@Override
	protected String getThumbnailImageName() {
		return "chi.png";
	}

	@Override
	protected String getWikipediaURL() {
		return DistributionTools.DistChiWikipedia;
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(AbstractRealDistribution distribution) {
		final ChiDistributionImpl chiDist=(ChiDistributionImpl)distribution;
		final String info1=DistributionTools.DistDegreesOfFreedom+"="+NumberTools.formatNumber(chiDist.degreesOfFreedom);
		return new DistributionWrapperInfo(distribution,chiDist.getSkewness(),chiDist.getMode(),info1,null);
	}

	@Override
	public AbstractRealDistribution getDistribution(double mean, double sd) {
		return null;
	}

	@Override
	public AbstractRealDistribution getDefaultDistribution() {
		return new ChiDistributionImpl(20);
	}

	@Override
	public double getMean(final AbstractRealDistribution distribution) {
		return ((ChiDistributionImpl)distribution).getNumericalMean();
	}

	@Override
	protected AbstractRealDistribution setMeanInt(AbstractRealDistribution distribution, double mean) {
		return null;
	}

	@Override
	public double getStandardDeviation(final AbstractRealDistribution distribution) {
		return Math.sqrt(((ChiDistributionImpl)distribution).getNumericalVariance());
	}

	@Override
	protected AbstractRealDistribution setStandardDeviationInt(AbstractRealDistribution distribution, double sd) {
		return null;
	}

	@Override
	protected double getParameterInt(AbstractRealDistribution distribution, int nr) {
		if (nr==1) return ((ChiDistributionImpl)distribution).degreesOfFreedom;
		return 0.0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(AbstractRealDistribution distribution, int nr, double value) {
		if (nr==1) return new ChiDistributionImpl((int)Math.round(value));
		return null;
	}

	@Override
	protected String getToStringData(AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(((ChiDistributionImpl)distribution).degreesOfFreedom);
	}

	@Override
	public AbstractRealDistribution fromString(String data, double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=1) return null;
		return new ChiDistributionImpl((int)Math.round(values[0]));
	}

	@Override
	protected AbstractRealDistribution cloneInt(AbstractRealDistribution distribution) {
		return new ChiDistributionImpl(((ChiDistributionImpl)distribution).degreesOfFreedom);
	}

	@Override
	protected boolean compareInt(AbstractRealDistribution distribution1, AbstractRealDistribution distribution2) {
		if (((ChiDistributionImpl)distribution1).degreesOfFreedom!=((ChiDistributionImpl)distribution2).degreesOfFreedom) return false;
		return true;
	}

	@Override
	protected Class<? extends CalcSymbolPreOperator> getCalcSymbolClass() {
		return CalcSymbolDistributionChi.class;
	}
}

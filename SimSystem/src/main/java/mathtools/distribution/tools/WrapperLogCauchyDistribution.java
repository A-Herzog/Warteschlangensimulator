/**
 * Copyright 2025 Alexander Herzog
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
import mathtools.distribution.LogCauchyDistributionImpl;
import parser.coresymbols.CalcSymbolPreOperator;
import parser.symbols.distributions.CalcSymbolDistributionLogCauchy;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link LogCauchyDistributionImpl}
 * @author Alexander Herzog
 * @see LogCauchyDistributionImpl
 * @see DistributionTools
 */
public class WrapperLogCauchyDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperLogCauchyDistribution() {
		super(LogCauchyDistributionImpl.class);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistLogCauchy;
	}

	@Override
	protected String getThumbnailImageName() {
		return "logcauchy.png";
	}

	@Override
	protected String getWikipediaURL() {
		return DistributionTools.DistLogCauchyWikipedia;
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(AbstractRealDistribution distribution) {
		final LogCauchyDistributionImpl dist=(LogCauchyDistributionImpl)distribution;
		final String info1="mu="+NumberTools.formatNumber(dist.mu,3)+"; sigma="+NumberTools.formatNumber(dist.sigma,3);
		return new DistributionWrapperInfo(null,null,null,dist.getMedian(),info1,null);
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
	public double getMean(final AbstractRealDistribution distribution) {
		return ((LogCauchyDistributionImpl)distribution).getMedian(); /* Nicht so ganz exakt, aber immerhin irgendwas */
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
		if (nr==1) return ((LogCauchyDistributionImpl)distribution).mu;
		if (nr==2) return ((LogCauchyDistributionImpl)distribution).sigma;
		return 0.0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(AbstractRealDistribution distribution, int nr, double value) {
		if (nr==1) return new LogCauchyDistributionImpl(value,((LogCauchyDistributionImpl)distribution).sigma);
		if (nr==2) return new LogCauchyDistributionImpl(((LogCauchyDistributionImpl)distribution).mu,value);
		return null;
	}

	@Override
	protected String getToStringData(AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(((LogCauchyDistributionImpl)distribution).mu)+";"+NumberTools.formatSystemNumber(((LogCauchyDistributionImpl)distribution).sigma);
	}

	@Override
	public AbstractRealDistribution fromString(String data, double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=2) return null;
		return new LogCauchyDistributionImpl(values[0],values[1]);
	}

	@Override
	protected AbstractRealDistribution cloneInt(AbstractRealDistribution distribution) {
		return new LogCauchyDistributionImpl(((LogCauchyDistributionImpl)distribution).mu,((LogCauchyDistributionImpl)distribution).sigma);
	}

	@Override
	protected boolean compareInt(AbstractRealDistribution distribution1, AbstractRealDistribution distribution2) {
		if (Math.abs(((LogCauchyDistributionImpl)distribution1).mu-((LogCauchyDistributionImpl)distribution2).mu)>DistributionTools.MAX_ERROR) return false;
		if (Math.abs(((LogCauchyDistributionImpl)distribution1).sigma-((LogCauchyDistributionImpl)distribution2).sigma)>DistributionTools.MAX_ERROR) return false;
		return true;
	}

	@Override
	protected Class<? extends CalcSymbolPreOperator> getCalcSymbolClass() {
		return CalcSymbolDistributionLogCauchy.class;
	}
}

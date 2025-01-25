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
import mathtools.distribution.LogNormalDistributionImpl;
import parser.coresymbols.CalcSymbolPreOperator;
import parser.symbols.distributions.CalcSymbolDistributionLogNormal;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link LogNormalDistributionImpl}
 * @author Alexander Herzog
 * @see LogNormalDistributionImpl
 * @see DistributionTools
 */
public class WrapperLogNormalDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperLogNormalDistribution() {
		super(LogNormalDistributionImpl.class,true,true);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistLogNormal;
	}

	@Override
	protected String getThumbnailImageName() {
		return "lognormal.png";
	}

	@Override
	protected String getWikipediaURL() {
		return DistributionTools.DistLogNormalWikipedia;
	}

	@Override
	protected String getWebAppDistributionName() {
		return "LogNormal";
	}

	@Override
	protected String getInfoHTML() {
		return DistributionTools.DistLogNormalInfo;
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(AbstractRealDistribution distribution) {
		final LogNormalDistributionImpl dist=(LogNormalDistributionImpl)distribution;
		return new DistributionWrapperInfo(distribution,dist.getSkewness(),dist.getMode());
	}

	@Override
	public AbstractRealDistribution getDistribution(double mean, double sd) {
		return new LogNormalDistributionImpl(mean,sd);
	}

	@Override
	public AbstractRealDistribution getDefaultDistribution() {
		return getDistribution(100,50);
	}

	@Override
	public AbstractRealDistribution getDistributionForFit(final double mean, final double sd, final double min, final double max) {
		if (sd<=0 || mean<=0) return null;
		return super.getDistributionForFit(mean,sd,min,max);
	}

	@Override
	public double getMean(final AbstractRealDistribution distribution) {
		return ((LogNormalDistributionImpl)distribution).mean;
	}

	@Override
	protected AbstractRealDistribution setMeanInt(AbstractRealDistribution distribution, double mean) {
		return new LogNormalDistributionImpl(mean,((LogNormalDistributionImpl)distribution).sd);
	}

	@Override
	public double getStandardDeviation(final AbstractRealDistribution distribution) {
		return ((LogNormalDistributionImpl)distribution).sd;
	}

	@Override
	protected AbstractRealDistribution setStandardDeviationInt(AbstractRealDistribution distribution, double sd) {
		return new LogNormalDistributionImpl(((LogNormalDistributionImpl)distribution).mean,sd);
	}

	@Override
	protected double getParameterInt(AbstractRealDistribution distribution, int nr) {
		if (nr==1) return ((LogNormalDistributionImpl)distribution).mean;
		if (nr==2) return ((LogNormalDistributionImpl)distribution).sd;
		return 0.0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(AbstractRealDistribution distribution, int nr, double value) {
		if (nr==1) return new LogNormalDistributionImpl(value,((LogNormalDistributionImpl)distribution).sd);
		if (nr==2) return new LogNormalDistributionImpl(((LogNormalDistributionImpl)distribution).mean,value);
		return null;
	}

	@Override
	protected String getToStringData(AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(((LogNormalDistributionImpl)distribution).mean)+";"+NumberTools.formatSystemNumber(((LogNormalDistributionImpl)distribution).sd);
	}

	@Override
	public AbstractRealDistribution fromString(String data, double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=2) return null;
		return new LogNormalDistributionImpl(values[0],values[1]);
	}

	@Override
	protected AbstractRealDistribution cloneInt(AbstractRealDistribution distribution) {
		return ((LogNormalDistributionImpl)distribution).clone();
	}

	@Override
	protected boolean compareInt(AbstractRealDistribution distribution1, AbstractRealDistribution distribution2) {
		if (Math.abs(((LogNormalDistributionImpl)distribution1).mean-((LogNormalDistributionImpl)distribution2).mean)>DistributionTools.MAX_ERROR) return false;
		if (Math.abs(((LogNormalDistributionImpl)distribution1).sd-((LogNormalDistributionImpl)distribution2).sd)>DistributionTools.MAX_ERROR) return false;
		return true;
	}

	@Override
	protected Class<? extends CalcSymbolPreOperator> getCalcSymbolClass() {
		return CalcSymbolDistributionLogNormal.class;
	}
}

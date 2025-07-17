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
import mathtools.distribution.HalfCauchyDistribution;
import parser.coresymbols.CalcSymbolPreOperator;
import parser.symbols.distributions.CalcSymbolDistributionHalfCauchy;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link HalfCauchyDistribution}
 * @author Alexander Herzog
 * @see HalfCauchyDistribution
 * @see DistributionTools
 */
public class WrapperHalfCauchyDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperHalfCauchyDistribution() {
		super(HalfCauchyDistribution.class);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistHalfCauchy;
	}

	@Override
	protected String getThumbnailImageName() {
		return "halfcauchy.png";
	}

	@Override
	protected String getWikipediaURL() {
		return DistributionTools.DistHalfCauchyWikipedia;
	}

	@Override
	protected String getWebAppDistributionName() {
		return "HalfCauchy";
	}

	@Override
	protected String getInfoHTML() {
		return null;
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(AbstractRealDistribution distribution) {
		final HalfCauchyDistribution dist=(HalfCauchyDistribution)distribution;
		final String info2=DistributionTools.DistLocation+"="+NumberTools.formatNumber(dist.mu,3)+"; "+DistributionTools.DistScale+"="+NumberTools.formatNumber(dist.sigma,3);
		return new DistributionWrapperInfo(null,null,null,dist.mu,null,info2);
	}

	@Override
	public AbstractRealDistribution getDistribution(double mean, double sd) {
		return null;
	}

	@Override
	public AbstractRealDistribution getDefaultDistribution() {
		return new CauchyDistribution(0,5);
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
		if (nr==1) return ((HalfCauchyDistribution)distribution).mu;
		if (nr==2) return ((HalfCauchyDistribution)distribution).sigma;
		return 0.0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(AbstractRealDistribution distribution, int nr, double value) {
		if (nr==1) return new HalfCauchyDistribution(value,((HalfCauchyDistribution)distribution).sigma);
		if (nr==2) return new HalfCauchyDistribution(((HalfCauchyDistribution)distribution).mu,value);
		return null;
	}

	@Override
	protected String getToStringData(AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(((HalfCauchyDistribution)distribution).mu)+";"+NumberTools.formatSystemNumber(((HalfCauchyDistribution)distribution).sigma);
	}

	@Override
	public AbstractRealDistribution fromString(String data, double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=2) return null;
		return new HalfCauchyDistribution(values[0],values[1]);
	}

	@Override
	protected AbstractRealDistribution cloneInt(AbstractRealDistribution distribution) {
		return new HalfCauchyDistribution((HalfCauchyDistribution)distribution);
	}

	@Override
	protected boolean compareInt(AbstractRealDistribution distribution1, AbstractRealDistribution distribution2) {
		if (Math.abs(((HalfCauchyDistribution)distribution1).mu-((HalfCauchyDistribution)distribution2).mu)>DistributionTools.MAX_ERROR) return false;
		if (Math.abs(((HalfCauchyDistribution)distribution1).sigma-((HalfCauchyDistribution)distribution2).sigma)>DistributionTools.MAX_ERROR) return false;
		return true;
	}

	@Override
	protected Class<? extends CalcSymbolPreOperator> getCalcSymbolClass() {
		return CalcSymbolDistributionHalfCauchy.class;
	}
}

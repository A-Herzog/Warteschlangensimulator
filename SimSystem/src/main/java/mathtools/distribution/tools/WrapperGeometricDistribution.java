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
import mathtools.distribution.DiscreteGeometricDistributionImpl;
import parser.coresymbols.CalcSymbolPreOperator;
import parser.symbols.distributions.CalcSymbolDiscreteDistributionGeometric;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link DiscreteGeometricDistributionImpl}
 * @author Alexander Herzog
 * @see DiscreteGeometricDistributionImpl
 * @see DistributionTools
 */
public class WrapperGeometricDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperGeometricDistribution() {
		super(DiscreteGeometricDistributionImpl.class,true,false);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistGeometric;
	}

	@Override
	protected String getThumbnailImageName() {
		return "geometric.png";
	}

	@Override
	protected String getWikipediaURL() {
		return DistributionTools.DistGeometricWikipedia;
	}

	@Override
	protected String getWebAppDistributionName() {
		return "Geometric";
	}

	@Override
	protected String getInfoHTML() {
		return null;
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(AbstractRealDistribution distribution) {
		final DiscreteGeometricDistributionImpl dist=(DiscreteGeometricDistributionImpl)distribution;
		final String info="p="+NumberTools.formatNumber(dist.p,3);
		final double sk=dist.getSkewness();
		final double mode=dist.getMode();
		return new DistributionWrapperInfo(distribution,sk,mode,info,null);
	}

	@Override
	public AbstractRealDistribution getDistribution(double mean, double sd) {
		/* (1-p)/p=mean <=> 1/(1+mean)=p */
		return new DiscreteGeometricDistributionImpl(1/(1+Math.max(0,mean)));
	}

	@Override
	public AbstractRealDistribution getDefaultDistribution() {
		return new DiscreteGeometricDistributionImpl(10);
	}

	@Override
	protected AbstractRealDistribution setMeanInt(AbstractRealDistribution distribution, double mean) {
		/* (1-p)/p=mean <=> 1/(1+mean)=p */
		return new DiscreteGeometricDistributionImpl(1/(1+Math.max(0,mean)));
	}

	@Override
	protected AbstractRealDistribution setStandardDeviationInt(AbstractRealDistribution distribution, double sd) {
		return null;
	}

	@Override
	protected double getParameterInt(AbstractRealDistribution distribution, int nr) {
		if (nr==1) return ((DiscreteGeometricDistributionImpl)distribution).p;
		return 0.0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(AbstractRealDistribution distribution, int nr, double value) {
		if (nr==1) return new DiscreteGeometricDistributionImpl(value);
		return null;
	}

	@Override
	protected String getToStringData(AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(((DiscreteGeometricDistributionImpl)distribution).p);
	}

	@Override
	public AbstractRealDistribution fromString(String data, double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=1) return null;
		return new DiscreteGeometricDistributionImpl(values[0]);
	}

	@Override
	protected AbstractRealDistribution cloneInt(AbstractRealDistribution distribution) {
		return new DiscreteGeometricDistributionImpl((DiscreteGeometricDistributionImpl)distribution);
	}

	@Override
	protected boolean compareInt(AbstractRealDistribution distribution1, AbstractRealDistribution distribution2) {
		if (Math.abs(((DiscreteGeometricDistributionImpl)distribution1).p-((DiscreteGeometricDistributionImpl)distribution2).p)>DistributionTools.MAX_ERROR) return false;
		return true;
	}

	@Override
	protected Class<? extends CalcSymbolPreOperator> getCalcSymbolClass() {
		return CalcSymbolDiscreteDistributionGeometric.class;
	}
}

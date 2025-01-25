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

import mathtools.distribution.DiscreteNegativeHyperGeomDistributionImpl;
import parser.coresymbols.CalcSymbolPreOperator;
import parser.symbols.distributions.CalcSymbolDiscreteDistributionNegativeHyperGeom;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link DiscreteNegativeHyperGeomDistributionImpl}
 * @author Alexander Herzog
 * @see DiscreteNegativeHyperGeomDistributionImpl
 * @see DistributionTools
 */
public class WrapperNegativeHyperGeomDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperNegativeHyperGeomDistribution() {
		super(DiscreteNegativeHyperGeomDistributionImpl.class);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistNegativeHyperGeom;
	}

	@Override
	protected String getThumbnailImageName() {
		return "discrete.png";
	}

	@Override
	protected String getWikipediaURL() {
		return DistributionTools.DistNegativeHyperGeomWikipedia;
	}

	@Override
	protected String getWebAppDistributionName() {
		return "NegativeHypergeometric";
	}

	@Override
	protected String getInfoHTML() {
		return null;
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(AbstractRealDistribution distribution) {
		final DiscreteNegativeHyperGeomDistributionImpl dist=(DiscreteNegativeHyperGeomDistributionImpl)distribution;
		final double mode=dist.getMode();
		final String info1="N="+dist.N+"; K="+dist.K+"; n="+dist.n;
		return new DistributionWrapperInfo(distribution,null,mode,info1,null);
	}

	@Override
	public AbstractRealDistribution getDistribution(double mean, double sd) {
		return null;
	}

	@Override
	public AbstractRealDistribution getDefaultDistribution() {
		return new DiscreteNegativeHyperGeomDistributionImpl(50,20,10);
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
		final DiscreteNegativeHyperGeomDistributionImpl dist=(DiscreteNegativeHyperGeomDistributionImpl)distribution;
		if (nr==1) return dist.N;
		if (nr==2) return dist.K;
		if (nr==3) return dist.n;
		return 0.0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(AbstractRealDistribution distribution, int nr, double value) {
		final DiscreteNegativeHyperGeomDistributionImpl dist=(DiscreteNegativeHyperGeomDistributionImpl)distribution;
		if (nr==1) return new DiscreteNegativeHyperGeomDistributionImpl((int)Math.round(value),dist.K,dist.n);
		if (nr==2) return new DiscreteNegativeHyperGeomDistributionImpl(dist.N,(int)Math.round(value),dist.n);
		if (nr==3) return new DiscreteNegativeHyperGeomDistributionImpl(dist.N,dist.K,(int)Math.round(value));
		return null;
	}

	@Override
	protected String getToStringData(AbstractRealDistribution distribution) {
		final DiscreteNegativeHyperGeomDistributionImpl dist=(DiscreteNegativeHyperGeomDistributionImpl)distribution;
		return ""+dist.N+";"+dist.K+";"+dist.n;
	}

	@Override
	public AbstractRealDistribution fromString(String data, double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=3) return null;
		return new DiscreteNegativeHyperGeomDistributionImpl((int)Math.round(values[0]),(int)Math.round(values[1]),(int)Math.round(values[2]));
	}

	@Override
	protected AbstractRealDistribution cloneInt(AbstractRealDistribution distribution) {
		return new DiscreteNegativeHyperGeomDistributionImpl((DiscreteNegativeHyperGeomDistributionImpl)distribution);
	}

	@Override
	protected boolean compareInt(AbstractRealDistribution distribution1, AbstractRealDistribution distribution2) {
		if (((DiscreteNegativeHyperGeomDistributionImpl)distribution1).N!=((DiscreteNegativeHyperGeomDistributionImpl)distribution2).N) return false;
		if (((DiscreteNegativeHyperGeomDistributionImpl)distribution1).K!=((DiscreteNegativeHyperGeomDistributionImpl)distribution2).K) return false;
		if (((DiscreteNegativeHyperGeomDistributionImpl)distribution1).n!=((DiscreteNegativeHyperGeomDistributionImpl)distribution2).n) return false;
		return true;
	}

	@Override
	protected Class<? extends CalcSymbolPreOperator> getCalcSymbolClass() {
		return CalcSymbolDiscreteDistributionNegativeHyperGeom.class;
	}
}

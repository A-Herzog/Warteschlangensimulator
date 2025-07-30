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

import mathtools.NumberTools;
import mathtools.distribution.DiscreteBoltzmannDistributionImpl;
import parser.coresymbols.CalcSymbolPreOperator;
import parser.symbols.distributions.CalcSymbolDiscreteDistributionBoltzmann;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link DiscreteBoltzmannDistributionImpl}
 * @author Alexander Herzog
 * @see DiscreteBoltzmannDistributionImpl
 * @see DistributionTools
 */
public class WrapperBoltzmannDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperBoltzmannDistribution() {
		super(DiscreteBoltzmannDistributionImpl.class);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistBoltzmann;
	}

	@Override
	protected String getThumbnailImageName() {
		return "boltzmann.png";
	}

	@Override
	protected String getWikipediaURL() {
		return DistributionTools.DistBoltzmannWikipedia;
	}

	@Override
	protected String getWebAppDistributionName() {
		return "Boltzmann";
	}

	@Override
	protected String getInfoHTML() {
		return null;
	}

	@Override
	protected boolean canSetMeanExact() {
		return false;
	}

	@Override
	protected boolean canSetStandardDeviationExact() {
		return false;
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(AbstractRealDistribution distribution) {
		final DiscreteBoltzmannDistributionImpl dist=(DiscreteBoltzmannDistributionImpl)distribution;
		final String info="lambda="+NumberTools.formatNumber(dist.lambda,3)+"; N="+dist.N;
		return new DistributionWrapperInfo(distribution,null,null,info,null);
	}

	@Override
	public AbstractRealDistribution getDistribution(double mean, double sd) {
		return null;
	}

	@Override
	public AbstractRealDistribution getDefaultDistribution() {
		return new DiscreteBoltzmannDistributionImpl(0.5,20);
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
		if (nr==1) return ((DiscreteBoltzmannDistributionImpl)distribution).lambda;
		if (nr==2) return ((DiscreteBoltzmannDistributionImpl)distribution).N;
		return 0.0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(AbstractRealDistribution distribution, int nr, double value) {
		if (nr==1) return new DiscreteBoltzmannDistributionImpl(value,((DiscreteBoltzmannDistributionImpl)distribution).N);
		if (nr==2) return new DiscreteBoltzmannDistributionImpl(((DiscreteBoltzmannDistributionImpl)distribution).lambda,(int)Math.round(value));
		return null;
	}

	@Override
	protected String getToStringData(AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(((DiscreteBoltzmannDistributionImpl)distribution).lambda)+";"+((DiscreteBoltzmannDistributionImpl)distribution).N;
	}

	@Override
	public AbstractRealDistribution fromString(String data, double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=2) return null;
		return new DiscreteBoltzmannDistributionImpl(values[0],(int)Math.round(values[1]));
	}

	@Override
	protected AbstractRealDistribution cloneInt(AbstractRealDistribution distribution) {
		return new DiscreteBoltzmannDistributionImpl((DiscreteBoltzmannDistributionImpl)distribution);
	}

	@Override
	protected boolean compareInt(AbstractRealDistribution distribution1, AbstractRealDistribution distribution2) {
		if (Math.abs(((DiscreteBoltzmannDistributionImpl)distribution1).lambda-((DiscreteBoltzmannDistributionImpl)distribution2).lambda)>DistributionTools.MAX_ERROR) return false;
		if (((DiscreteBoltzmannDistributionImpl)distribution1).N!=((DiscreteBoltzmannDistributionImpl)distribution2).N) return false;
		return true;
	}

	@Override
	protected Class<? extends CalcSymbolPreOperator> getCalcSymbolClass() {
		return CalcSymbolDiscreteDistributionBoltzmann.class;
	}
}

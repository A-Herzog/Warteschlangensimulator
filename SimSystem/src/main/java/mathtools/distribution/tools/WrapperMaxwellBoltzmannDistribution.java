/**
 * Copyright 2022 Alexander Herzog
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
import mathtools.distribution.MaxwellBoltzmannDistribution;
import parser.coresymbols.CalcSymbolPreOperator;
import parser.symbols.distributions.CalcSymbolDistributionMaxwellBoltzmann;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link MaxwellBoltzmannDistribution}
 * @author Alexander Herzog
 * @see MaxwellBoltzmannDistribution
 * @see DistributionTools
 */
public class WrapperMaxwellBoltzmannDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperMaxwellBoltzmannDistribution() {
		super(MaxwellBoltzmannDistribution.class);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistMaxwellBoltzmann;
	}

	@Override
	protected String getThumbnailImageName() {
		return "maxwellboltzmann.png";
	}

	@Override
	protected String getWikipediaURL() {
		return DistributionTools.DistMaxwellBoltzmannWikipedia;
	}

	/**
	 * Konstante Schiefe der Maxwell-Boltzmann-Verteilung
	 * @see #getInfoInt(AbstractRealDistribution)
	 */
	private static final double SK=2*Math.sqrt(2)*(16-5*Math.PI)/Math.pow(3*Math.PI-8,3/2);

	@Override
	protected DistributionWrapperInfo getInfoInt(AbstractRealDistribution distribution) {
		final MaxwellBoltzmannDistribution dist=(MaxwellBoltzmannDistribution)distribution;
		final double E=dist.getNumericalMean();
		final double Std=Math.sqrt(dist.getNumericalVariance());
		final double mode=Math.sqrt(2)*dist.a;

		return new DistributionWrapperInfo(E,Std,SK,mode,null,null);
	}

	@Override
	public AbstractRealDistribution getDistribution(double mean, double sd) {
		final double a=mean/2*Math.sqrt(Math.PI/2);
		return new MaxwellBoltzmannDistribution(a);
	}

	@Override
	public AbstractRealDistribution getDefaultDistribution() {
		return new MaxwellBoltzmannDistribution(10);
	}

	@Override
	protected AbstractRealDistribution setMeanInt(AbstractRealDistribution distribution, double mean) {
		final double a=mean/2*Math.sqrt(Math.PI/2);
		return new MaxwellBoltzmannDistribution(a);
	}

	@Override
	protected AbstractRealDistribution setStandardDeviationInt(AbstractRealDistribution distribution, double sd) {
		final double a=Math.pow(sd*sd*Math.PI/(3*Math.PI-8),1/3);
		return new MaxwellBoltzmannDistribution(a);
	}

	@Override
	protected double getParameterInt(AbstractRealDistribution distribution, int nr) {
		if (nr==1) return ((MaxwellBoltzmannDistribution)distribution).a;
		return 0.0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(AbstractRealDistribution distribution, int nr, double value) {
		if (nr==1) return new MaxwellBoltzmannDistribution(value);
		return null;
	}

	@Override
	protected String getToStringData(AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(((MaxwellBoltzmannDistribution)distribution).a);
	}

	@Override
	public AbstractRealDistribution fromString(String data, double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=1) return null;
		return new MaxwellBoltzmannDistribution(values[0]);
	}

	@Override
	protected AbstractRealDistribution cloneInt(AbstractRealDistribution distribution) {
		return new MaxwellBoltzmannDistribution(((MaxwellBoltzmannDistribution)distribution).a);
	}

	@Override
	protected boolean compareInt(AbstractRealDistribution distribution1, AbstractRealDistribution distribution2) {
		if (Math.abs(((MaxwellBoltzmannDistribution)distribution1).a-((MaxwellBoltzmannDistribution)distribution2).a)>DistributionTools.MAX_ERROR) return false;
		return true;
	}

	@Override
	protected Class<? extends CalcSymbolPreOperator> getCalcSymbolClass() {
		return CalcSymbolDistributionMaxwellBoltzmann.class;
	}
}

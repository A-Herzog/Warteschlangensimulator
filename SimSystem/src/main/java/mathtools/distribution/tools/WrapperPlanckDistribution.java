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
import mathtools.distribution.DiscretePlanckDistributionImpl;
import parser.coresymbols.CalcSymbolPreOperator;
import parser.symbols.distributions.CalcSymbolDiscreteDistributionPlanck;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link DiscretePlanckDistributionImpl}
 * @author Alexander Herzog
 * @see DiscretePlanckDistributionImpl
 * @see DistributionTools
 */
public class WrapperPlanckDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperPlanckDistribution() {
		super(DiscretePlanckDistributionImpl.class,true,false);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistPlanck;
	}

	@Override
	protected String getThumbnailImageName() {
		return "planck.png";
	}

	@Override
	protected String getWikipediaURL() {
		return DistributionTools.DistPlanckWikipedia;
	}

	@Override
	protected String getWebAppDistributionName() {
		return "Planck";
	}

	@Override
	protected String getInfoHTML() {
		return null;
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(AbstractRealDistribution distribution) {
		final DiscretePlanckDistributionImpl dist=(DiscretePlanckDistributionImpl)distribution;
		final String info="lambda="+NumberTools.formatNumber(dist.lambda,3);
		return new DistributionWrapperInfo(distribution,null,null,info,null);
	}

	/**
	 * Berechnet den zu einem Erwartungswert der Planck-Verteilung gehörenden Verteilungsparameter &lambda;
	 * @param mean	Erwartungswert der Planck-Verteilung
	 * @return	Verteilungsparameter &lambda;
	 */
	public static double lambdaFromMean(final double mean) {
		/* mean=1/(exp(lambda)-1) <=> log(1/mean+1)=lambda */
		return Math.log(1/Math.max(mean,0.0001)+1);
	}

	@Override
	public AbstractRealDistribution getDistribution(double mean, double sd) {
		return new DiscretePlanckDistributionImpl(lambdaFromMean(mean));
	}

	@Override
	public AbstractRealDistribution getDefaultDistribution() {
		return new DiscretePlanckDistributionImpl(0.5);
	}

	@Override
	protected AbstractRealDistribution setMeanInt(AbstractRealDistribution distribution, double mean) {
		return new DiscretePlanckDistributionImpl(lambdaFromMean(mean));
	}

	@Override
	protected AbstractRealDistribution setStandardDeviationInt(AbstractRealDistribution distribution, double sd) {
		return null;
	}

	@Override
	protected double getParameterInt(AbstractRealDistribution distribution, int nr) {
		if (nr==1) return ((DiscretePlanckDistributionImpl)distribution).lambda;
		return 0.0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(AbstractRealDistribution distribution, int nr, double value) {
		if (nr==1) return new DiscretePlanckDistributionImpl(value);
		return null;
	}

	@Override
	protected String getToStringData(AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(((DiscretePlanckDistributionImpl)distribution).lambda);
	}

	@Override
	public AbstractRealDistribution fromString(String data, double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=1) return null;
		return new DiscretePlanckDistributionImpl(values[0]);
	}

	@Override
	protected AbstractRealDistribution cloneInt(AbstractRealDistribution distribution) {
		return new DiscretePlanckDistributionImpl((DiscretePlanckDistributionImpl)distribution);
	}

	@Override
	protected boolean compareInt(AbstractRealDistribution distribution1, AbstractRealDistribution distribution2) {
		if (Math.abs(((DiscretePlanckDistributionImpl)distribution1).lambda-((DiscretePlanckDistributionImpl)distribution2).lambda)>DistributionTools.MAX_ERROR) return false;
		return true;
	}

	@Override
	protected Class<? extends CalcSymbolPreOperator> getCalcSymbolClass() {
		return CalcSymbolDiscreteDistributionPlanck.class;
	}
}

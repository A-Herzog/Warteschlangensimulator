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
import mathtools.distribution.LogLogisticDistributionImpl;
import parser.coresymbols.CalcSymbolPreOperator;
import parser.symbols.distributions.CalcSymbolDistributionLogLogistic;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link LogLogisticDistributionImpl}
 * @author Alexander Herzog
 * @see LogLogisticDistributionImpl
 * @see DistributionTools
 */
public class WrapperLogLogisticDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperLogLogisticDistribution() {
		super(LogLogisticDistributionImpl.class);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistLogLogistic;
	}

	@Override
	protected String getThumbnailImageName() {
		return "loglogistic.png";
	}

	@Override
	protected String getWikipediaURL() {
		return DistributionTools.DistLogLogisticWikipedia;
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(AbstractRealDistribution distribution) {
		final LogLogisticDistributionImpl dist=(LogLogisticDistributionImpl)distribution;
		final double alpha=dist.alpha;
		final double beta=dist.beta;
		final String info="alpha="+NumberTools.formatNumber(alpha,3)+"; beta="+NumberTools.formatNumber(beta,3);
		return new DistributionWrapperInfo(distribution,null,dist.getMode(),info,null); /* Schiefe nur schwer berechenbar, siehe https://www.causascientia.org/math_stat/Dists/Compendium.pdf */
	}

	@Override
	public AbstractRealDistribution getDistribution(double mean, double sd) {
		return null;
	}

	@Override
	public AbstractRealDistribution getDefaultDistribution() {
		return new LogLogisticDistributionImpl(100,3);
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
	public AbstractRealDistribution getDistributionForFit(final double mean, final double sd, final double min, final double max) {
		if (mean<=0 || sd<=0  || min<0) return null;

		/*
		factor=mean/(std**2+mean**2)
		beta=pi/acos(alpha*factor)
		 */
		final double factor=mean/(sd*sd+mean*mean);
		double alpha;
		if (factor<1) {
			alpha=Math.floor(1/factor);
			if (alpha>1) alpha--;
		} else {
			alpha=1/factor;
		}
		final double beta=Math.PI/Math.acos(Math.min(1,alpha*factor));

		return new LogLogisticDistributionImpl(alpha,beta);
	}

	@Override
	protected double getParameterInt(AbstractRealDistribution distribution, int nr) {
		if (nr==1) return ((LogLogisticDistributionImpl)distribution).alpha;
		if (nr==2) return ((LogLogisticDistributionImpl)distribution).beta;
		return 0.0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(AbstractRealDistribution distribution, int nr, double value) {
		if (nr==1) return new LogLogisticDistributionImpl(value,((LogLogisticDistributionImpl)distribution).beta);
		if (nr==2) return new LogLogisticDistributionImpl(((LogLogisticDistributionImpl)distribution).alpha,value);
		return null;
	}

	@Override
	protected String getToStringData(AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(((LogLogisticDistributionImpl)distribution).alpha)+";"+NumberTools.formatSystemNumber(((LogLogisticDistributionImpl)distribution).beta);
	}

	@Override
	public AbstractRealDistribution fromString(String data, double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=2) return null;
		return new LogLogisticDistributionImpl(values[0],values[1]);
	}

	@Override
	protected AbstractRealDistribution cloneInt(AbstractRealDistribution distribution) {
		return ((LogLogisticDistributionImpl)distribution).clone();
	}

	@Override
	protected boolean compareInt(AbstractRealDistribution distribution1, AbstractRealDistribution distribution2) {
		if (Math.abs(((LogLogisticDistributionImpl)distribution1).alpha-((LogLogisticDistributionImpl)distribution2).alpha)>DistributionTools.MAX_ERROR) return false;
		if (Math.abs(((LogLogisticDistributionImpl)distribution1).beta-((LogLogisticDistributionImpl)distribution2).beta)>DistributionTools.MAX_ERROR) return false;
		return true;
	}

	@Override
	protected Class<? extends CalcSymbolPreOperator> getCalcSymbolClass() {
		return CalcSymbolDistributionLogLogistic.class;
	}
}

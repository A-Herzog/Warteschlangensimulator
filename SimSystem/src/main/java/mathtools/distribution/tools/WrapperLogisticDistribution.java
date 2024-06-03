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
import mathtools.distribution.LogisticDistributionImpl;
import parser.coresymbols.CalcSymbolPreOperator;
import parser.symbols.distributions.CalcSymbolDistributionLogistic;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link LogisticDistributionImpl}
 * @author Alexander Herzog
 * @see LogisticDistributionImpl
 * @see DistributionTools
 */
public class WrapperLogisticDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperLogisticDistribution() {
		super(LogisticDistributionImpl.class,true,true);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistLogistic;
	}

	@Override
	protected String getThumbnailImageName() {
		return "logistic.png";
	}

	@Override
	protected String getWikipediaURL() {
		return DistributionTools.DistLogisticWikipedia;
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(AbstractRealDistribution distribution) {
		final LogisticDistributionImpl dist=(LogisticDistributionImpl)distribution;
		final double mu=dist.mu;
		final double s=dist.s;
		final String info="mu="+NumberTools.formatNumber(mu,3)+"; s="+NumberTools.formatNumber(s,3);
		return new DistributionWrapperInfo(distribution,0.0,mu,info,null); /* Schiefe=0 immer */
	}

	@Override
	public AbstractRealDistribution getDistribution(double mean, double sd) {
		return new LogisticDistributionImpl(mean,sd*Math.sqrt(3)/Math.PI);
	}

	@Override
	public AbstractRealDistribution getDefaultDistribution() {
		return getDistribution(100,50);
	}

	@Override
	public AbstractRealDistribution getDistributionForFit(final double mean, final double sd, final double min, final double max) {
		if (mean<=0 || sd<=0) return null;
		return super.getDistributionForFit(mean,sd,min,max);
	}

	@Override
	protected AbstractRealDistribution setMeanInt(AbstractRealDistribution distribution, double mean) {
		return new LogisticDistributionImpl(mean,((LogisticDistributionImpl)distribution).s);
	}

	@Override
	protected AbstractRealDistribution setStandardDeviationInt(AbstractRealDistribution distribution, double sd) {
		final double mu=((LogisticDistributionImpl)distribution).mu;
		return new LogisticDistributionImpl(mu,sd*Math.sqrt(3)/Math.PI);
	}

	@Override
	protected double getParameterInt(AbstractRealDistribution distribution, int nr) {
		if (nr==1) return ((LogisticDistributionImpl)distribution).mu;
		if (nr==2) return ((LogisticDistributionImpl)distribution).s;
		return 0.0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(AbstractRealDistribution distribution, int nr, double value) {
		final LogisticDistributionImpl l=(LogisticDistributionImpl)distribution;
		if (nr==1) return new LogisticDistributionImpl(value,l.s);
		if (nr==2) return new LogisticDistributionImpl(l.mu,value);
		return null;
	}

	@Override
	protected String getToStringData(AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(((LogisticDistributionImpl)distribution).mu)+";"+NumberTools.formatSystemNumber(((LogisticDistributionImpl)distribution).s);
	}

	@Override
	public AbstractRealDistribution fromString(String data, double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=2) return null;
		return new LogisticDistributionImpl(values[0],values[1]);
	}

	@Override
	protected AbstractRealDistribution cloneInt(AbstractRealDistribution distribution) {
		return ((LogisticDistributionImpl)distribution).clone();
	}

	@Override
	protected boolean compareInt(AbstractRealDistribution distribution1, AbstractRealDistribution distribution2) {
		if (Math.abs(((LogisticDistributionImpl)distribution1).mu-((LogisticDistributionImpl)distribution2).mu)>DistributionTools.MAX_ERROR) return false;
		if (Math.abs(((LogisticDistributionImpl)distribution1).s-((LogisticDistributionImpl)distribution2).s)>DistributionTools.MAX_ERROR) return false;
		return true;
	}

	@Override
	protected Class<? extends CalcSymbolPreOperator> getCalcSymbolClass() {
		return CalcSymbolDistributionLogistic.class;
	}
}

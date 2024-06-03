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
import mathtools.distribution.LaplaceDistributionImpl;
import parser.coresymbols.CalcSymbolPreOperator;
import parser.symbols.distributions.CalcSymbolDistributionLaplace;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link LaplaceDistributionImpl}
 * @author Alexander Herzog
 * @see LaplaceDistributionImpl
 * @see DistributionTools
 */
public class WrapperLaplaceDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperLaplaceDistribution() {
		super(LaplaceDistributionImpl.class,true,true);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistLaplace;
	}

	@Override
	protected String getThumbnailImageName() {
		return "laplace.png";
	}

	@Override
	protected String getWikipediaURL() {
		return DistributionTools.DistLaplaceWikipedia;
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(AbstractRealDistribution distribution) {
		final double mu=((LaplaceDistributionImpl)distribution).mu;
		final double b=((LaplaceDistributionImpl)distribution).b;
		final String info="mu="+NumberTools.formatNumber(mu,3)+"; "+DistributionTools.DistScale+"="+NumberTools.formatNumber(b,3);
		return new DistributionWrapperInfo(distribution,0.0,mu,info,null); /* Schiefe=0 immer */
	}

	@Override
	public AbstractRealDistribution getDistribution(double mean, double sd) {
		return new LaplaceDistributionImpl(mean,sd/Math.sqrt(2));
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
		return new LaplaceDistributionImpl(mean,((LaplaceDistributionImpl)distribution).b);
	}

	@Override
	protected AbstractRealDistribution setStandardDeviationInt(AbstractRealDistribution distribution, double sd) {
		return new LaplaceDistributionImpl(((LaplaceDistributionImpl)distribution).mu,sd/Math.sqrt(2));
	}

	@Override
	protected double getParameterInt(AbstractRealDistribution distribution, int nr) {
		if (nr==1) return ((LaplaceDistributionImpl)distribution).mu;
		if (nr==2) return ((LaplaceDistributionImpl)distribution).b;
		return 0.0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(AbstractRealDistribution distribution, int nr, double value) {
		final LaplaceDistributionImpl l=(LaplaceDistributionImpl)distribution;
		if (nr==1) return new LaplaceDistributionImpl(value,l.b);
		if (nr==2) return new LaplaceDistributionImpl(l.mu,value);
		return null;
	}

	@Override
	protected String getToStringData(AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(((LaplaceDistributionImpl)distribution).mu)+";"+NumberTools.formatSystemNumber(((LaplaceDistributionImpl)distribution).b);
	}

	@Override
	public AbstractRealDistribution fromString(String data, double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=2) return null;
		return new LaplaceDistributionImpl(values[0],values[1]);
	}

	@Override
	protected AbstractRealDistribution cloneInt(AbstractRealDistribution distribution) {
		return ((LaplaceDistributionImpl)distribution).clone();
	}

	@Override
	protected boolean compareInt(AbstractRealDistribution distribution1, AbstractRealDistribution distribution2) {
		if (Math.abs(((LaplaceDistributionImpl)distribution1).mu-((LaplaceDistributionImpl)distribution2).mu)>DistributionTools.MAX_ERROR) return false;
		if (Math.abs(((LaplaceDistributionImpl)distribution1).b-((LaplaceDistributionImpl)distribution2).b)>DistributionTools.MAX_ERROR) return false;
		return true;
	}

	@Override
	protected Class<? extends CalcSymbolPreOperator> getCalcSymbolClass() {
		return CalcSymbolDistributionLaplace.class;
	}
}

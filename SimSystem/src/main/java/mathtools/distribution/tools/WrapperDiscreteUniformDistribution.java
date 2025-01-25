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

import mathtools.distribution.DiscreteUniformDistributionImpl;
import parser.coresymbols.CalcSymbolPreOperator;
import parser.symbols.distributions.CalcSymbolDiscreteDistributionUniform;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link DiscreteUniformDistributionImpl}
 * @author Alexander Herzog
 * @see DiscreteUniformDistributionImpl
 * @see DistributionTools
 */
public class WrapperDiscreteUniformDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperDiscreteUniformDistribution() {
		super(DiscreteUniformDistributionImpl.class,true,true);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistDiscreteUniform;
	}

	@Override
	protected String getThumbnailImageName() {
		return "discreteUniform.png";
	}

	@Override
	protected String getWikipediaURL() {
		return DistributionTools.DistDiscreteUniformWikipedia;
	}

	@Override
	protected String getWebAppDistributionName() {
		return "DiscreteUniform";
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
		final DiscreteUniformDistributionImpl dist=(DiscreteUniformDistributionImpl)distribution;
		final String info="a="+dist.a+"; b="+dist.b;
		final double sk=dist.getSkewness();
		return new DistributionWrapperInfo(distribution,sk,null,info,null);
	}

	@Override
	public AbstractRealDistribution getDistribution(double mean, double sd) {
		if (mean<=0 || sd<=0) return null;
		/* E=(a+b)/2, Var=((b-a+1)^2-1)/12 => b=sqrt(12Var+1)/2+E-1/2, a=2E-b */
		int b=(int)Math.round(Math.sqrt(12*sd*sd+1)/2+mean-0.5);
		int a=(int)Math.round(2*mean-b);
		if (a<0) a=0;
		if (b<a) b=a+1;
		return new DiscreteUniformDistributionImpl(a,b);
	}

	@Override
	public AbstractRealDistribution getDefaultDistribution() {
		return new DiscreteUniformDistributionImpl(1,5);
	}

	@Override
	protected AbstractRealDistribution setMeanInt(AbstractRealDistribution distribution, double mean) {
		/* E=(a+b)/2, Var=((b-a+1)^2-1)/12 => b=sqrt(12Var+1)/2+E-1/2, a=2E-b */
		final double variance=((DiscreteUniformDistributionImpl)distribution).getNumericalVariance();
		final int b=(int)Math.round(Math.sqrt(12*variance+1)/2+mean-0.5);
		final int a=(int)Math.round(2*mean-b);
		return new DiscreteUniformDistributionImpl(a,b);
	}

	@Override
	protected AbstractRealDistribution setStandardDeviationInt(AbstractRealDistribution distribution, double sd) {
		if (sd<=0) return null;
		/* E=(a+b)/2, Var=((b-a+1)^2-1)/12 => b=sqrt(12Var+1)/2+E-1/2, a=2E-b */
		final double mean=((DiscreteUniformDistributionImpl)distribution).getNumericalMean();
		final int b=(int)Math.round(Math.sqrt(12*sd*sd+1)/2+mean-0.5);
		final int a=(int)Math.round(2*mean-b);
		return new DiscreteUniformDistributionImpl(a,b);
	}

	@Override
	protected double getParameterInt(AbstractRealDistribution distribution, int nr) {
		if (nr==1) return ((DiscreteUniformDistributionImpl)distribution).a;
		if (nr==2) return ((DiscreteUniformDistributionImpl)distribution).b;
		return 0.0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(AbstractRealDistribution distribution, int nr, double value) {
		if (nr==1) return new DiscreteUniformDistributionImpl((int)Math.round(value),((DiscreteUniformDistributionImpl)distribution).b);
		if (nr==2) return new DiscreteUniformDistributionImpl(((DiscreteUniformDistributionImpl)distribution).a,(int)Math.round(value));
		return null;
	}

	@Override
	protected String getToStringData(AbstractRealDistribution distribution) {
		return ((DiscreteUniformDistributionImpl)distribution).a+";"+((DiscreteUniformDistributionImpl)distribution).b;
	}

	@Override
	public AbstractRealDistribution fromString(String data, double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=2) return null;
		return new DiscreteUniformDistributionImpl((int)Math.round(values[0]),(int)Math.round(values[1]));
	}

	@Override
	protected AbstractRealDistribution cloneInt(AbstractRealDistribution distribution) {
		return new DiscreteUniformDistributionImpl((DiscreteUniformDistributionImpl)distribution);
	}

	@Override
	protected boolean compareInt(AbstractRealDistribution distribution1, AbstractRealDistribution distribution2) {
		if (((DiscreteUniformDistributionImpl)distribution1).a!=((DiscreteUniformDistributionImpl)distribution2).a) return false;
		if (((DiscreteUniformDistributionImpl)distribution1).b!=((DiscreteUniformDistributionImpl)distribution2).b) return false;
		return true;
	}

	@Override
	protected Class<? extends CalcSymbolPreOperator> getCalcSymbolClass() {
		return CalcSymbolDiscreteDistributionUniform.class;
	}
}

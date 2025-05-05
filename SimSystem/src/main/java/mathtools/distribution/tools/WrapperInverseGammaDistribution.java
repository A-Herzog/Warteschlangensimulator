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
import mathtools.distribution.InverseGammaDistributionImpl;
import parser.coresymbols.CalcSymbolPreOperator;
import parser.symbols.distributions.CalcSymbolDistributionInverseGamma;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link InverseGammaDistributionImpl}
 * @author Alexander Herzog
 * @see InverseGammaDistributionImpl
 * @see DistributionTools
 */
public class WrapperInverseGammaDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperInverseGammaDistribution() {
		super(InverseGammaDistributionImpl.class,true,true);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistInverseGamma;
	}

	@Override
	protected String getThumbnailImageName() {
		return "inversegamma.png";
	}

	@Override
	protected String getWikipediaURL() {
		return DistributionTools.DistInverseGammaWikipedia;
	}

	@Override
	protected String getWebAppDistributionName() {
		return "InverseGamma";
	}

	@Override
	protected String getInfoHTML() {
		return null;
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(AbstractRealDistribution distribution) {
		final InverseGammaDistributionImpl dist=(InverseGammaDistributionImpl)distribution;
		final double alpha=dist.getShape();
		final double beta=dist.getScale();
		final Double sk=(alpha<=3)?null:(4*Math.sqrt(alpha-2)/(alpha-3));
		final double mode=beta/(alpha+1);
		final String info1="alpha="+NumberTools.formatNumber(alpha,3)+"; beta="+NumberTools.formatNumber(beta,3);
		return new DistributionWrapperInfo(distribution,sk,mode,info1,null);
	}

	@Override
	public AbstractRealDistribution getDistribution(double mean, double sd) {
		mean=Math.max(mean,0);
		sd=Math.max(sd,0);
		final double alpha=((sd<=0)?0:(mean*mean/sd/sd))+2;
		final double beta=Math.max(((sd<=0)?0:(mean*mean*mean/sd/sd))+mean,0.0001);
		return new InverseGammaDistributionImpl(alpha,beta);
	}

	@Override
	public AbstractRealDistribution getDefaultDistribution() {
		return getDistribution(5,5);
	}

	@Override
	protected AbstractRealDistribution setMeanInt(AbstractRealDistribution distribution, double mean) {
		final double oldSD=Math.sqrt(distribution.getNumericalVariance());
		return getDistribution(mean,oldSD);
	}

	@Override
	protected AbstractRealDistribution setStandardDeviationInt(AbstractRealDistribution distribution, double sd) {
		final double oldMean=distribution.getNumericalMean();
		return getDistribution(oldMean,sd);
	}

	@Override
	protected double getParameterInt(AbstractRealDistribution distribution, int nr) {
		if (nr==1) return ((InverseGammaDistributionImpl)distribution).getShape();
		if (nr==2) return ((InverseGammaDistributionImpl)distribution).getScale();
		return 0.0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(AbstractRealDistribution distribution, int nr, double value) {
		if (nr==1) return new InverseGammaDistributionImpl(value,((InverseGammaDistributionImpl)distribution).getScale());
		if (nr==2) return new InverseGammaDistributionImpl(((InverseGammaDistributionImpl)distribution).getShape(),value);
		return null;
	}

	@Override
	protected String getToStringData(AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(((InverseGammaDistributionImpl)distribution).getShape())+";"+NumberTools.formatSystemNumber(((InverseGammaDistributionImpl)distribution).getScale());
	}

	@Override
	public AbstractRealDistribution fromString(String data, double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=2) return null;
		return new InverseGammaDistributionImpl(values[0],values[1]);
	}

	@Override
	protected AbstractRealDistribution cloneInt(AbstractRealDistribution distribution) {
		return new InverseGammaDistributionImpl(((InverseGammaDistributionImpl)distribution).getShape(),((InverseGammaDistributionImpl)distribution).getScale());
	}

	@Override
	protected boolean compareInt(AbstractRealDistribution distribution1, AbstractRealDistribution distribution2) {
		if (Math.abs(((InverseGammaDistributionImpl)distribution1).getShape()-((InverseGammaDistributionImpl)distribution2).getShape())>DistributionTools.MAX_ERROR) return false;
		if (Math.abs(((InverseGammaDistributionImpl)distribution1).getScale()-((InverseGammaDistributionImpl)distribution2).getScale())>DistributionTools.MAX_ERROR) return false;
		return true;
	}

	@Override
	protected Class<? extends CalcSymbolPreOperator> getCalcSymbolClass() {
		return CalcSymbolDistributionInverseGamma.class;
	}
}

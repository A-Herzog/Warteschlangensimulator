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
import org.apache.commons.math3.distribution.WeibullDistribution;
import org.apache.commons.math3.special.Gamma;
import org.apache.commons.math3.util.FastMath;

import mathtools.Functions;
import mathtools.NumberTools;
import parser.coresymbols.CalcSymbolPreOperator;
import parser.symbols.distributions.CalcSymbolDistributionWeibull;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link WeibullDistribution}
 * @author Alexander Herzog
 * @see WeibullDistribution
 * @see DistributionTools
 */
public class WrapperWeibullDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperWeibullDistribution() {
		super(WeibullDistribution.class);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistWeibull;
	}

	@Override
	protected String getThumbnailImageName() {
		return "weibull.png";
	}

	@Override
	protected String getWikipediaURL() {
		return DistributionTools.DistWeibullWikipedia;
	}

	@Override
	protected String getWebAppDistributionName() {
		return "Weibull";
	}

	@Override
	protected String getInfoHTML() {
		return DistributionTools.DistWeibullInfo;
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(AbstractRealDistribution distribution) {
		final WeibullDistribution dist=(WeibullDistribution)distribution;
		final double beta=dist.getShape();
		final double lambda=1/dist.getScale();
		final String info=DistributionTools.DistInverseScale+"="+NumberTools.formatNumber(lambda,3)+"; Form="+NumberTools.formatNumber(beta,3);

		final double mu=distribution.getNumericalMean();
		final double sigma=Math.sqrt(distribution.getNumericalVariance());

		final double sk;
		if (sigma>0) sk=(Functions.getGamma(1+3/beta)/(lambda*lambda*lambda)-3*mu*sigma*sigma-mu*mu*mu)/(sigma*sigma*sigma); else sk=0;

		final double mode;
		if (beta>1) mode=lambda*Math.pow((beta-1)/beta,1/beta); else mode=0;

		return new DistributionWrapperInfo(distribution,sk,mode,info,null);
	}

	/**
	 * Berechnet den quadrierten Variationskoeffizienten für einen vorgegebenen Parameter &alpha;
	 * @param alpha	Verteilungsparameter &alpha; (=Form)
	 * @return	Quadrierter Variationskoeffizient
	 */
	public static double calcSCV(final double alpha) {
		final double gammaAlphaInv=Gamma.gamma(1/alpha);
		return 2*alpha*Gamma.gamma(2/alpha)/gammaAlphaInv/gammaAlphaInv-1;
	}

	@Override
	public AbstractRealDistribution getDistribution(double mean, double sd) {
		if (mean<=0.0) return null;
		final double cv=sd/mean;
		final double scv=cv*cv;

		/* Alpha per Bisektionsverfahren bestimmen */
		double alphaMin=0.08;
		double alphaMax=80;
		double scvAlphaMin=5301247.466455577;
		double scvAlphaMax=2.5244789703471326E-4;
		final double alpha;
		if (scv>=scvAlphaMin) {
			alpha=alphaMin;
		} else if (scv<=scvAlphaMax) {
			alpha=alphaMax;
		} else {
			while (alphaMax-alphaMin>0.00001) {
				double alphaMean=(alphaMin+alphaMax)/2;
				double scvAlphaMean=calcSCV(alphaMean);
				if (scvAlphaMean>scv) {
					alphaMin=alphaMean;
					scvAlphaMin=scvAlphaMean;
				} else {
					alphaMax=alphaMean;
					scvAlphaMax=scvAlphaMean;
				}
			}
			alpha=(alphaMin+alphaMax)/2;
		}

		/* Beta aus der Formel für den Erwartungswert bestimmen */
		final double beta=mean/Gamma.gamma(1+1/alpha);

		return new WeibullDistribution(alpha,beta);
	}

	@Override
	public AbstractRealDistribution getDefaultDistribution() {
		return new WeibullDistribution(2,100);
	}

	@Override
	public double getMean(final AbstractRealDistribution distribution) {
		final double sh = ((WeibullDistribution)distribution).getShape();
		final double sc = ((WeibullDistribution)distribution).getScale();
		/* Achtung: scale ist hier anderes definiert als z.B. in Wikipedia; dort wäre Vorfaktor 1/sc richtig. */
		return sc * FastMath.exp(Gamma.logGamma(1 + (1 / sh)));
	}

	@Override
	protected AbstractRealDistribution setMeanInt(AbstractRealDistribution distribution, double mean) {
		return null;
	}

	@Override
	public double getStandardDeviation(final AbstractRealDistribution distribution) {
		double beta=((WeibullDistribution)distribution).getShape(); double lambda=1/((WeibullDistribution)distribution).getScale();
		return 1/lambda*Math.sqrt(FastMath.exp(Gamma.logGamma(1+2/beta))-FastMath.pow(FastMath.exp(Gamma.logGamma(1+1/beta)),2));
	}

	@Override
	protected AbstractRealDistribution setStandardDeviationInt(AbstractRealDistribution distribution, double sd) {
		return null;
	}

	@Override
	protected double getParameterInt(AbstractRealDistribution distribution, int nr) {
		if (nr==1) return 1/((WeibullDistribution)distribution).getScale();
		if (nr==2) return ((WeibullDistribution)distribution).getShape();
		return 0.0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(AbstractRealDistribution distribution, int nr, double value) {
		if (nr<1 || nr>2) return null;
		double p1=1/((WeibullDistribution)distribution).getScale();
		double p2=((WeibullDistribution)distribution).getShape();
		if (nr==1) p1=value;
		if (nr==2) p2=value;
		if (p1==0) return null;
		return new WeibullDistribution(null,p2,1/p1,WeibullDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
	}

	@Override
	protected String getToStringData(AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(1/((WeibullDistribution)distribution).getScale())+";"+NumberTools.formatSystemNumber(((WeibullDistribution)distribution).getShape());
	}

	@Override
	public AbstractRealDistribution fromString(String data, double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=2) return null;
		return new WeibullDistribution(null,values[1],1/values[0],WeibullDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
	}

	@Override
	protected AbstractRealDistribution cloneInt(AbstractRealDistribution distribution) {
		return new WeibullDistribution(null,((WeibullDistribution)distribution).getShape(),((WeibullDistribution)distribution).getScale(),WeibullDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
	}

	@Override
	protected boolean compareInt(AbstractRealDistribution distribution1, AbstractRealDistribution distribution2) {
		if (Math.abs(((WeibullDistribution)distribution1).getShape()-((WeibullDistribution)distribution2).getShape())>DistributionTools.MAX_ERROR) return false;
		if (Math.abs(((WeibullDistribution)distribution1).getScale()-((WeibullDistribution)distribution2).getScale())>DistributionTools.MAX_ERROR) return false;
		return true;
	}

	@Override
	protected Class<? extends CalcSymbolPreOperator> getCalcSymbolClass() {
		return CalcSymbolDistributionWeibull.class;
	}
}

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

import mathtools.NumberTools;
import mathtools.distribution.KumaraswamyDistribution;
import parser.coresymbols.CalcSymbolPreOperator;
import parser.symbols.distributions.CalcSymbolDistributionKumaraswamy;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link KumaraswamyDistribution}
 * @author Alexander Herzog
 * @see KumaraswamyDistribution
 * @see DistributionTools
 */
public class WrapperKumaraswamyDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperKumaraswamyDistribution() {
		super(KumaraswamyDistribution.class,false,false);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistKumaraswamy;
	}

	@Override
	protected String getThumbnailImageName() {
		return "beta.png";
	}

	@Override
	protected String getWikipediaURL() {
		return DistributionTools.DistKumaraswamyWikipedia;
	}

	@Override
	protected String getWebAppDistributionName() {
		return "Kumaraswamy";
	}

	@Override
	protected String getInfoHTML() {
		return null;
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(final AbstractRealDistribution distribution) {
		Double mode=null;
		final KumaraswamyDistribution dist=(KumaraswamyDistribution)distribution;
		if (dist.a>=1 && dist.b>=1 && (dist.a>1 || dist.b>1)) {
			mode=Math.pow((dist.a-1)/(dist.a*dist.b-1),1/dist.a)*(dist.d-dist.c)+dist.c;
		}
		return new DistributionWrapperInfo(distribution,null,mode);
	}

	@Override
	public AbstractRealDistribution getDistribution(double mean, double sd) {
		return null;
	}

	@Override
	public AbstractRealDistribution getDefaultDistribution() {
		return new KumaraswamyDistribution(1,2,50,100);
	}

	@Override
	public AbstractRealDistribution getDistributionForFit(final double mean, final double sd, final double min, final double max) {
		return null;
	}

	@Override
	public double getMean(final AbstractRealDistribution distribution) {
		return ((KumaraswamyDistribution)distribution).getNumericalMean();
	}

	@Override
	protected AbstractRealDistribution setMeanInt(final AbstractRealDistribution distribution, final double mean) {
		return null;
	}

	@Override
	public double getStandardDeviation(final AbstractRealDistribution distribution) {
		return Math.sqrt(((KumaraswamyDistribution)distribution).getNumericalVariance());
	}

	@Override
	protected AbstractRealDistribution setStandardDeviationInt(final AbstractRealDistribution distribution, final double sd) {
		return null;
	}

	@Override
	protected double getParameterInt(final AbstractRealDistribution distribution, final int nr) {
		if (nr==1) return ((KumaraswamyDistribution)distribution).a;
		if (nr==2) return ((KumaraswamyDistribution)distribution).b;
		if (nr==3) return ((KumaraswamyDistribution)distribution).c;
		if (nr==4) return ((KumaraswamyDistribution)distribution).d;
		return 0.0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(final AbstractRealDistribution distribution, final int nr, final double value) {
		final KumaraswamyDistribution old=(KumaraswamyDistribution)distribution;
		if (nr==1) return new KumaraswamyDistribution(value,old.b,old.c,old.d);
		if (nr==2) return new KumaraswamyDistribution(old.a,value,old.c,old.d);
		if (nr==3) return new KumaraswamyDistribution(old.a,old.b,value,old.d);
		if (nr==4) return new KumaraswamyDistribution(old.a,old.b,old.c,value);
		return null;
	}

	@Override
	protected String getToStringData(final AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(((KumaraswamyDistribution)distribution).a)+";"+NumberTools.formatSystemNumber(((KumaraswamyDistribution)distribution).b)+";"+NumberTools.formatSystemNumber(((KumaraswamyDistribution)distribution).c)+";"+NumberTools.formatSystemNumber(((KumaraswamyDistribution)distribution).d);
	}

	@Override
	public AbstractRealDistribution fromString(final String data, final double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=4) return null;
		return new KumaraswamyDistribution(values[0],values[1],values[2],values[3]);
	}

	@Override
	protected AbstractRealDistribution cloneInt(final AbstractRealDistribution distribution) {
		return new KumaraswamyDistribution(((KumaraswamyDistribution)distribution).a,((KumaraswamyDistribution)distribution).b,((KumaraswamyDistribution)distribution).c,((KumaraswamyDistribution)distribution).d);
	}

	@Override
	protected boolean compareInt(final AbstractRealDistribution distribution1, final AbstractRealDistribution distribution2) {
		if (Math.abs(((KumaraswamyDistribution)distribution1).a-((KumaraswamyDistribution)distribution2).a)>DistributionTools.MAX_ERROR) return false;
		if (Math.abs(((KumaraswamyDistribution)distribution1).b-((KumaraswamyDistribution)distribution2).b)>DistributionTools.MAX_ERROR) return false;
		if (Math.abs(((KumaraswamyDistribution)distribution1).c-((KumaraswamyDistribution)distribution2).c)>DistributionTools.MAX_ERROR) return false;
		if (Math.abs(((KumaraswamyDistribution)distribution1).d-((KumaraswamyDistribution)distribution2).d)>DistributionTools.MAX_ERROR) return false;
		return true;
	}

	@Override
	protected Class<? extends CalcSymbolPreOperator> getCalcSymbolClass() {
		return CalcSymbolDistributionKumaraswamy.class;
	}
}

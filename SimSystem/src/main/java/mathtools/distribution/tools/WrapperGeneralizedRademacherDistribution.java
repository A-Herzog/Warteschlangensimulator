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
import mathtools.distribution.GeneralizedRademacherDistribution;
import parser.coresymbols.CalcSymbolPreOperator;
import parser.symbols.distributions.CalcSymbolGeneralizedRademacherDistribution;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link GeneralizedRademacherDistribution}
 * @author Alexander Herzog
 * @see GeneralizedRademacherDistribution
 * @see DistributionTools
 */
public class WrapperGeneralizedRademacherDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperGeneralizedRademacherDistribution() {
		super(GeneralizedRademacherDistribution.class,true,true);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistGeneralizedRademacher;
	}

	@Override
	protected String getThumbnailImageName() {
		return "generalizedrademacher.png";
	}

	@Override
	protected String getWikipediaURL() {
		return DistributionTools.DistGeneralizedRademacherWikipedia;
	}

	@Override
	protected String getWebAppDistributionName() {
		return "Rademacher";
	}

	@Override
	protected String getInfoHTML() {
		return null;
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(AbstractRealDistribution distribution) {
		final GeneralizedRademacherDistribution dist=(GeneralizedRademacherDistribution)distribution;
		Double mode=null;
		if (dist.pA!=dist.pB) mode=(dist.pA>dist.pB)?dist.a:dist.b;
		final var info=new String[] {
				"a="+NumberTools.formatNumber(dist.a),
				"b="+NumberTools.formatNumber(dist.b),
				"P(a)="+NumberTools.formatNumber(dist.pA),
				"P(b)="+NumberTools.formatNumber(dist.pB)
		};
		return new DistributionWrapperInfo(dist,null,mode,String.join(", ",info),null);
	}

	@Override
	public AbstractRealDistribution getDistribution(double mean, double sd) {
		double pA=0.5;
		double a;
		double b;
		while (true) {
			b=mean+sd*Math.sqrt(pA/(1-pA));
			a=(mean-b*(1-pA))/pA;
			if (a>0) return new GeneralizedRademacherDistribution(a,b,pA);
			pA+=(1-pA)/2;
			if (pA<0.01) return null;
		}
	}

	@Override
	public AbstractRealDistribution getDefaultDistribution() {
		return new GeneralizedRademacherDistribution(50,100,0.5);
	}

	@Override
	protected AbstractRealDistribution setMeanInt(AbstractRealDistribution distribution, double mean) {
		final var dist=(GeneralizedRademacherDistribution)distribution;
		return getDistribution(mean,Math.sqrt(dist.getNumericalVariance()));
	}

	@Override
	protected AbstractRealDistribution setStandardDeviationInt(AbstractRealDistribution distribution, double sd) {
		final var dist=(GeneralizedRademacherDistribution)distribution;
		return getDistribution(dist.getNumericalMean(),sd);
	}

	@Override
	protected double getParameterInt(AbstractRealDistribution distribution, int nr) {
		if (nr==1) return ((GeneralizedRademacherDistribution)distribution).a;
		if (nr==2) return ((GeneralizedRademacherDistribution)distribution).b;
		if (nr==3) return ((GeneralizedRademacherDistribution)distribution).pA;
		return 0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(AbstractRealDistribution distribution, int nr, double value) {
		final var dist=(GeneralizedRademacherDistribution)distribution;
		if (nr==1) new GeneralizedRademacherDistribution(value,dist.b,dist.pA);
		if (nr==2) new GeneralizedRademacherDistribution(dist.a,value,dist.pA);
		if (nr==3) new GeneralizedRademacherDistribution(dist.a,dist.b,value);
		return null;
	}

	@Override
	protected String getToStringData(AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(((GeneralizedRademacherDistribution)distribution).a)+";"+NumberTools.formatSystemNumber(((GeneralizedRademacherDistribution)distribution).b)+";"+NumberTools.formatSystemNumber(((GeneralizedRademacherDistribution)distribution).pA);
	}

	@Override
	public AbstractRealDistribution fromString(String data, double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=3) return null;
		return new GeneralizedRademacherDistribution(values[0],values[1],values[2]);
	}

	@Override
	protected AbstractRealDistribution cloneInt(AbstractRealDistribution distribution) {
		return new GeneralizedRademacherDistribution((GeneralizedRademacherDistribution)distribution);
	}

	@Override
	protected boolean compareInt(AbstractRealDistribution distribution1, AbstractRealDistribution distribution2) {
		if (((GeneralizedRademacherDistribution)distribution1).a!=((GeneralizedRademacherDistribution)distribution2).a) return false;
		if (((GeneralizedRademacherDistribution)distribution1).b!=((GeneralizedRademacherDistribution)distribution2).b) return false;
		if (((GeneralizedRademacherDistribution)distribution1).pA!=((GeneralizedRademacherDistribution)distribution2).pA) return false;
		return true;
	}

	@Override
	protected Class<? extends CalcSymbolPreOperator> getCalcSymbolClass() {
		return CalcSymbolGeneralizedRademacherDistribution.class;
	}
}

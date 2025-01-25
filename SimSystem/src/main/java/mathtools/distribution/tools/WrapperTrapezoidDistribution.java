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
import mathtools.distribution.TrapezoidDistributionImpl;
import parser.coresymbols.CalcSymbolPreOperator;
import parser.symbols.distributions.CalcSymbolDistributionTrapezoid;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link TrapezoidDistributionImpl}
 * @author Alexander Herzog
 * @see TrapezoidDistributionImpl
 * @see DistributionTools
 */
public class WrapperTrapezoidDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperTrapezoidDistribution() {
		super(TrapezoidDistributionImpl.class);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistTrapezoid;
	}

	@Override
	protected String getThumbnailImageName() {
		return "trapezoid.png";
	}

	@Override
	protected String getWikipediaURL() {
		return DistributionTools.DistTrapezoidWikipedia;
	}

	@Override
	protected String getWebAppDistributionName() {
		return "Trapezoid";
	}

	@Override
	protected String getInfoHTML() {
		return null;
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(AbstractRealDistribution distribution) {
		final double a=((TrapezoidDistributionImpl)distribution).a;
		final double b=((TrapezoidDistributionImpl)distribution).b;
		final double c=((TrapezoidDistributionImpl)distribution).c;
		final double d=((TrapezoidDistributionImpl)distribution).d;
		final double mean=((TrapezoidDistributionImpl)distribution).getNumericalMean();
		final double sd=Math.sqrt(((TrapezoidDistributionImpl)distribution).getNumericalVariance());

		final double sk;
		if (d>a && sd>0) {
			sk=TrapezoidDistributionImpl.calcSk(a,b,c,d,mean,sd);
		} else {
			sk=0;
		}
		final String info=DistributionTools.DistRange+"=["+NumberTools.formatNumber(a,3)+";"+NumberTools.formatNumber(d,3)+"]; b="+NumberTools.formatNumber(b,3)+"; c="+NumberTools.formatNumber(c,3);
		return new DistributionWrapperInfo(distribution,sk,null,info,null);
	}

	@Override
	public AbstractRealDistribution getDistribution(double mean, double sd) {
		return getDistributionForFit(mean,sd,mean-2*sd,mean+2*sd);
	}

	@Override
	public AbstractRealDistribution getDefaultDistribution() {
		return getDistribution(100,50);
	}

	@Override
	public AbstractRealDistribution getDistributionForFit(final double mean, double sd, final double min, final double max) {
		if (sd<=0) return null;

		final double a=min;
		final double d=max;

		/* Standardabweichung ist mindestens so groß, wie die der Dreiecksverteilung */
		sd=Math.max(sd,(d-a)/Math.sqrt(24.0));

		/* Standardabweichung ist höchstens so groß, wie bei der Gleichverteilung */
		sd=Math.min(sd,(d-a)/Math.sqrt(12.0));

		double fMin=0;
		double fMax=(d-a)/2;

		while ((fMax-fMin)>(d-a)/1000) {
			final double fMiddle=(fMax+fMin)/2;
			double sdMiddle=Math.sqrt(new TrapezoidDistributionImpl(a,a+fMiddle,d-fMiddle,d).getNumericalVariance());
			if (sdMiddle>sd) {
				fMin=fMiddle;
			} else {
				fMax=fMiddle;
			}
		}

		final double f=(fMax+fMin)/2;
		return new TrapezoidDistributionImpl(a,a+f,d-f,d);
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
	protected double getParameterInt(AbstractRealDistribution distribution, int nr) {
		if (nr==1) return ((TrapezoidDistributionImpl)distribution).a;
		if (nr==2) return ((TrapezoidDistributionImpl)distribution).b;
		if (nr==3) return ((TrapezoidDistributionImpl)distribution).c;
		if (nr==4) return ((TrapezoidDistributionImpl)distribution).d;
		return 0.0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(AbstractRealDistribution distribution, int nr, double value) {
		final TrapezoidDistributionImpl t=(TrapezoidDistributionImpl)distribution;
		if (nr==1) return new TrapezoidDistributionImpl(value,t.b,t.c,t.d);
		if (nr==2) return new TrapezoidDistributionImpl(t.a,value,t.c,t.d);
		if (nr==3) return new TrapezoidDistributionImpl(t.a,t.b,value,t.d);
		if (nr==4) return new TrapezoidDistributionImpl(t.a,t.b,t.c,value);
		return null;
	}

	@Override
	protected String getToStringData(AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(((TrapezoidDistributionImpl)distribution).a)+";"+NumberTools.formatSystemNumber(((TrapezoidDistributionImpl)distribution).b)+";"+NumberTools.formatSystemNumber(((TrapezoidDistributionImpl)distribution).c)+";"+NumberTools.formatSystemNumber(((TrapezoidDistributionImpl)distribution).d);
	}

	@Override
	public AbstractRealDistribution fromString(String data, double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=4) return null;
		return new TrapezoidDistributionImpl(values[0],values[1],values[2],values[3]);
	}

	@Override
	protected AbstractRealDistribution cloneInt(AbstractRealDistribution distribution) {
		return ((TrapezoidDistributionImpl)distribution).clone();
	}

	@Override
	protected boolean compareInt(AbstractRealDistribution distribution1, AbstractRealDistribution distribution2) {
		if (Math.abs(((TrapezoidDistributionImpl)distribution1).a-((TrapezoidDistributionImpl)distribution2).a)>DistributionTools.MAX_ERROR) return false;
		if (Math.abs(((TrapezoidDistributionImpl)distribution1).b-((TrapezoidDistributionImpl)distribution2).b)>DistributionTools.MAX_ERROR) return false;
		if (Math.abs(((TrapezoidDistributionImpl)distribution1).c-((TrapezoidDistributionImpl)distribution2).c)>DistributionTools.MAX_ERROR) return false;
		if (Math.abs(((TrapezoidDistributionImpl)distribution1).d-((TrapezoidDistributionImpl)distribution2).d)>DistributionTools.MAX_ERROR) return false;
		return true;
	}

	@Override
	protected Class<? extends CalcSymbolPreOperator> getCalcSymbolClass() {
		return CalcSymbolDistributionTrapezoid.class;
	}
}

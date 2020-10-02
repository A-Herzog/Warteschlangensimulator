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
import mathtools.distribution.ParetoDistributionImpl;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link ParetoDistributionImpl}
 * @author Alexander Herzog
 * @see ParetoDistributionImpl
 * @see DistributionTools
 */
public class WrapperParetoDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperParetoDistribution() {
		super(ParetoDistributionImpl.class,true,true);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistPareto;
	}

	@Override
	protected String getThumbnailImageName() {
		return "pareto.png";
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(AbstractRealDistribution distribution) {
		final double xmin=((ParetoDistributionImpl)distribution).xmin;
		final double alpha=((ParetoDistributionImpl)distribution).alpha;
		final String info="xmin="+NumberTools.formatNumber(xmin,3)+"; alpha="+NumberTools.formatNumber(alpha,3);
		return new DistributionWrapperInfo(distribution,info,null);
	}

	@Override
	public AbstractRealDistribution getDistribution(double mean, double sd) {
		if (sd>0) {
			double alpha=1+Math.sqrt(1+mean*mean/sd/sd);
			if (alpha<2) alpha=2.01; /* für alpha<=2 ist var=infty */
			return new ParetoDistributionImpl(mean*(alpha-1)/alpha,alpha);
		} else {
			return null;
		}
	}

	@Override
	public AbstractRealDistribution getDefaultDistribution() {
		return getDistribution(100,50);
	}

	@Override
	protected AbstractRealDistribution setMeanInt(AbstractRealDistribution distribution, double mean) {
		double var=((ParetoDistributionImpl)distribution).getNumericalVariance();
		if (var<=0) var=0.0001;
		double alpha=1+Math.sqrt(1+mean*mean/var);
		if (alpha<2) alpha=2.01; /* für alpha<=2 ist var=infty */
		return new ParetoDistributionImpl(mean*(alpha-1)/alpha,alpha);
	}

	@Override
	protected AbstractRealDistribution setStandardDeviationInt(AbstractRealDistribution distribution, double sd) {
		final double mean=((ParetoDistributionImpl)distribution).getNumericalMean();
		double alpha=1+Math.sqrt(1+mean*mean/sd/sd);
		if (alpha<2) alpha=2.01; /* für alpha<=2 ist var=infty */
		return new ParetoDistributionImpl(mean*(alpha-1)/alpha,alpha);
	}

	@Override
	protected double getParameterInt(AbstractRealDistribution distribution, int nr) {
		if (nr==1) return ((ParetoDistributionImpl)distribution).xmin;
		if (nr==2) return ((ParetoDistributionImpl)distribution).alpha;
		return 0.0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(AbstractRealDistribution distribution, int nr, double value) {
		final ParetoDistributionImpl p=(ParetoDistributionImpl)distribution;
		if (nr==1) return new ParetoDistributionImpl(value,p.alpha);
		if (nr==2) return new ParetoDistributionImpl(p.xmin,value);
		return null;
	}

	@Override
	protected String getToStringData(AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(((ParetoDistributionImpl)distribution).xmin)+";"+NumberTools.formatSystemNumber(((ParetoDistributionImpl)distribution).alpha);
	}

	@Override
	public AbstractRealDistribution fromString(String data, double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=2) return null;
		return new ParetoDistributionImpl(values[0],values[1]);
	}

	@Override
	protected AbstractRealDistribution cloneInt(AbstractRealDistribution distribution) {
		return ((ParetoDistributionImpl)distribution).clone();
	}

	@Override
	protected boolean compareInt(AbstractRealDistribution distribution1, AbstractRealDistribution distribution2) {
		if (Math.abs(((ParetoDistributionImpl)distribution1).xmin-((ParetoDistributionImpl)distribution2).xmin)>DistributionTools.MAX_ERROR) return false;
		if (Math.abs(((ParetoDistributionImpl)distribution1).alpha-((ParetoDistributionImpl)distribution2).alpha)>DistributionTools.MAX_ERROR) return false;
		return true;
	}
}

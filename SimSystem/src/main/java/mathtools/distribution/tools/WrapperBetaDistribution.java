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
import mathtools.distribution.ExtBetaDistributionImpl;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link ExtBetaDistributionImpl}
 * @author Alexander Herzog
 * @see ExtBetaDistributionImpl
 * @see DistributionTools
 */
public class WrapperBetaDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperBetaDistribution() {
		super(ExtBetaDistributionImpl.class);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistBeta;
	}

	@Override
	protected String getThumbnailImageName() {
		return "beta.png";
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(AbstractRealDistribution distribution) {
		final ExtBetaDistributionImpl betaDist=(ExtBetaDistributionImpl)distribution;
		final double alpha=betaDist.getAlpha();
		final double beta=betaDist.getBeta();
		final double a=betaDist.getSupportLowerBound();
		final double b=betaDist.getSupportUpperBound();
		final String info="alpha="+NumberTools.formatNumber(alpha,3)+"; beta="+NumberTools.formatNumber(beta,3)+"; "+DistributionTools.DistRange+"=["+NumberTools.formatNumber(a,3)+";"+NumberTools.formatNumber(b,3)+"]";
		return new DistributionWrapperInfo(distribution,info,null);
	}

	@Override
	public AbstractRealDistribution getDistribution(double mean, double sd) {
		return null;
	}

	@Override
	public AbstractRealDistribution getDefaultDistribution() {
		return new ExtBetaDistributionImpl(0,100,1.5,1.5);
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
		if (nr==1) return ((ExtBetaDistributionImpl)distribution).getAlpha();
		if (nr==2) return ((ExtBetaDistributionImpl)distribution).getBeta();
		if (nr==3) return ((ExtBetaDistributionImpl)distribution).getSupportLowerBound();
		if (nr==4) return ((ExtBetaDistributionImpl)distribution).getSupportUpperBound();
		return 0.0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(AbstractRealDistribution distribution, int nr, double value) {
		final double alpha=((ExtBetaDistributionImpl)distribution).getAlpha();
		final double beta=((ExtBetaDistributionImpl)distribution).getBeta();
		final double a=((ExtBetaDistributionImpl)distribution).getSupportLowerBound();
		final double b=((ExtBetaDistributionImpl)distribution).getSupportUpperBound();
		if (nr==1) return new ExtBetaDistributionImpl(a,b,value,beta);
		if (nr==2) return new ExtBetaDistributionImpl(a,b,alpha,value);
		if (nr==3) return new ExtBetaDistributionImpl(value,b,alpha,beta);
		if (nr==4) return new ExtBetaDistributionImpl(a,value,alpha,beta);
		return null;
	}

	@Override
	protected String getToStringData(AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(((ExtBetaDistributionImpl)distribution).getAlpha())+";"+NumberTools.formatSystemNumber(((ExtBetaDistributionImpl)distribution).getBeta())+";"+NumberTools.formatSystemNumber(((ExtBetaDistributionImpl)distribution).getSupportLowerBound())+";"+NumberTools.formatSystemNumber(((ExtBetaDistributionImpl)distribution).getSupportUpperBound());
	}

	@Override
	public AbstractRealDistribution fromString(String data, double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=4) return null;
		return new ExtBetaDistributionImpl(values[2],values[3],values[0],values[1]);
	}

	@Override
	protected AbstractRealDistribution cloneInt(AbstractRealDistribution distribution) {
		return ((ExtBetaDistributionImpl)distribution).clone();
	}

	@Override
	protected boolean compareInt(AbstractRealDistribution distribution1, AbstractRealDistribution distribution2) {
		if (Math.abs(((ExtBetaDistributionImpl)distribution1).getAlpha()-((ExtBetaDistributionImpl)distribution2).getAlpha())>DistributionTools.MAX_ERROR) return false;
		if (Math.abs(((ExtBetaDistributionImpl)distribution1).getBeta()-((ExtBetaDistributionImpl)distribution2).getBeta())>DistributionTools.MAX_ERROR) return false;
		if (Math.abs(((ExtBetaDistributionImpl)distribution1).domainLowerBound-((ExtBetaDistributionImpl)distribution2).domainLowerBound)>DistributionTools.MAX_ERROR) return false;
		if (Math.abs(((ExtBetaDistributionImpl)distribution1).domainUpperBound-((ExtBetaDistributionImpl)distribution2).domainUpperBound)>DistributionTools.MAX_ERROR) return false;
		return true;

	}

}
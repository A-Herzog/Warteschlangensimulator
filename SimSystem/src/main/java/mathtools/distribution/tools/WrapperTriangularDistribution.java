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
import mathtools.distribution.TriangularDistributionImpl;
import parser.coresymbols.CalcSymbolPreOperator;
import parser.symbols.distributions.CalcSymbolDistributionTriangular;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link TriangularDistributionImpl}
 * @author Alexander Herzog
 * @see TriangularDistributionImpl
 * @see DistributionTools
 */
public class WrapperTriangularDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperTriangularDistribution() {
		super(TriangularDistributionImpl.class,true);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistTriangular;
	}

	@Override
	protected String getThumbnailImageName() {
		return "triangular.png";
	}

	@Override
	protected String getWikipediaURL() {
		return DistributionTools.DistTriangularWikipedia;
	}

	@Override
	protected String getWebAppDistributionName() {
		return "Triangular";
	}

	@Override
	protected String getInfoHTML() {
		return DistributionTools.DistTriangularInfo;
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(AbstractRealDistribution distribution) {
		final double A=((TriangularDistributionImpl)distribution).lowerBound;
		final double B=((TriangularDistributionImpl)distribution).upperBound;
		final double Px=((TriangularDistributionImpl)distribution).mostLikelyX;
		final double sk=Math.sqrt(2.0)*(A+B-2*Px)*(2*A-B-Px)*(A-2*B+Px)/5/Math.pow(A*A+B*B+Px*Px-A*B-A*Px-B*Px,3.0/2.0);
		final String info=DistributionTools.DistRange+"=["+NumberTools.formatNumber(A,3)+";"+NumberTools.formatNumber(B,3)+"]; "+DistributionTools.DistMostLikely+"="+NumberTools.formatNumber(Px,3);
		return new DistributionWrapperInfo(distribution,sk,Px,info,null);
	}

	@Override
	public AbstractRealDistribution getDistribution(double mean, double sd) {
		return new TriangularDistributionImpl(mean-Math.sqrt(6)*sd,mean,mean+Math.sqrt(6)*sd);
	}

	@Override
	public AbstractRealDistribution getDefaultDistribution() {
		return getDistribution(100,50);
	}

	@Override
	public AbstractRealDistribution getDistributionForFit(final double mean, final double sd, final double min, final double max) {
		if (sd<=0) return null;
		return super.getDistributionForFit(mean,sd,min,max);
	}

	@Override
	protected AbstractRealDistribution setMeanInt(AbstractRealDistribution distribution, double mean) {
		final double sd=Math.sqrt(((TriangularDistributionImpl)distribution).getNumericalVariance());
		return new TriangularDistributionImpl(mean-sd*Math.sqrt(6),mean,mean+sd*Math.sqrt(6));
	}

	@Override
	protected AbstractRealDistribution setStandardDeviationInt(AbstractRealDistribution distribution, double sd) {
		return null;
	}

	@Override
	protected double getParameterInt(AbstractRealDistribution distribution, int nr) {
		if (nr==1) return ((TriangularDistributionImpl)distribution).lowerBound;
		if (nr==2) return ((TriangularDistributionImpl)distribution).mostLikelyX;
		if (nr==3) return ((TriangularDistributionImpl)distribution).upperBound;
		return 0.0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(AbstractRealDistribution distribution, int nr, double value) {
		final TriangularDistributionImpl t=(TriangularDistributionImpl)distribution;
		if (nr==1) return new TriangularDistributionImpl(value,t.mostLikelyX,t.upperBound);
		if (nr==2) return new TriangularDistributionImpl(t.lowerBound,value,t.upperBound);
		if (nr==3) return new TriangularDistributionImpl(t.lowerBound,t.mostLikelyX,value);
		return null;
	}

	@Override
	protected String getToStringData(AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(((TriangularDistributionImpl)distribution).lowerBound)+";"+NumberTools.formatSystemNumber(((TriangularDistributionImpl)distribution).mostLikelyX)+";"+NumberTools.formatSystemNumber(((TriangularDistributionImpl)distribution).upperBound);
	}

	@Override
	public AbstractRealDistribution fromString(String data, double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=3) return null;
		return new TriangularDistributionImpl(values[0],values[1],values[2]);
	}

	@Override
	protected AbstractRealDistribution cloneInt(AbstractRealDistribution distribution) {
		return ((TriangularDistributionImpl)distribution).clone();
	}

	@Override
	protected boolean compareInt(AbstractRealDistribution distribution1, AbstractRealDistribution distribution2) {
		if (Math.abs(((TriangularDistributionImpl)distribution1).lowerBound-((TriangularDistributionImpl)distribution2).lowerBound)>DistributionTools.MAX_ERROR) return false;
		if (Math.abs(((TriangularDistributionImpl)distribution1).upperBound-((TriangularDistributionImpl)distribution2).upperBound)>DistributionTools.MAX_ERROR) return false;
		if (Math.abs(((TriangularDistributionImpl)distribution1).mostLikelyX-((TriangularDistributionImpl)distribution2).mostLikelyX)>DistributionTools.MAX_ERROR) return false;
		return true;
	}

	@Override
	protected Class<? extends CalcSymbolPreOperator> getCalcSymbolClass() {
		return CalcSymbolDistributionTriangular.class;
	}
}
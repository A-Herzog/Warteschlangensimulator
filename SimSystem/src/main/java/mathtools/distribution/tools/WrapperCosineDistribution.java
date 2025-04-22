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
import mathtools.distribution.CosineDistributionImpl;
import parser.coresymbols.CalcSymbolPreOperator;
import parser.symbols.distributions.CalcSymbolDistributionCosine;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link CosineDistributionImpl}
 * @author Alexander Herzog
 * @see CosineDistributionImpl
 * @see DistributionTools
 */
public class WrapperCosineDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperCosineDistribution() {
		super(CosineDistributionImpl.class,true,true);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistCosine;
	}

	@Override
	protected String getThumbnailImageName() {
		return "cosine.png";
	}

	@Override
	protected String getWikipediaURL() {
		return DistributionTools.DistFrechetWikipedia;
	}

	@Override
	protected String getWebAppDistributionName() {
		return "Cosine";
	}

	@Override
	protected String getInfoHTML() {
		return null;
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(final AbstractRealDistribution distribution) {
		final CosineDistributionImpl cosine=(CosineDistributionImpl)distribution;
		final String info1=DistributionTools.DistRange+"=["+NumberTools.formatNumber(cosine.getSupportLowerBound(),3)+";"+NumberTools.formatNumber(cosine.getSupportUpperBound(),3)+"]";
		return new DistributionWrapperInfo(distribution,0.0,cosine.getNumericalMean(),info1,null); /* Schiefe=0 immer */
	}

	@Override
	public AbstractRealDistribution getDistribution(double mean, double sd) {
		if (sd<=0) return new CosineDistributionImpl(mean,mean);

		final double variance=sd*sd;
		final double range=Math.sqrt(variance*12*Math.PI*Math.PI/(Math.PI*Math.PI-6));
		return new CosineDistributionImpl(mean-range/2,mean+range/2);
	}

	@Override
	public AbstractRealDistribution getDefaultDistribution() {
		return new CosineDistributionImpl(0,1);
	}

	@Override
	protected AbstractRealDistribution setMeanInt(final AbstractRealDistribution distribution, final double mean) {
		final CosineDistributionImpl old=(CosineDistributionImpl)distribution;
		final double oldMean=old.getNumericalMean();
		return new CosineDistributionImpl(old.a+mean-oldMean,old.b+mean-oldMean);
	}

	@Override
	protected AbstractRealDistribution setStandardDeviationInt(final AbstractRealDistribution distribution, final double sd) {
		final CosineDistributionImpl old=(CosineDistributionImpl)distribution;
		final double mean=(old.a+old.b)/2;
		final double variance=sd*sd;
		final double range=Math.sqrt(variance*12*Math.PI*Math.PI/(Math.PI*Math.PI-6));
		return new CosineDistributionImpl(mean-range/2,mean+range/2);
	}

	@Override
	protected double getParameterInt(final AbstractRealDistribution distribution, final int nr) {
		if (nr==1) return ((CosineDistributionImpl)distribution).a;
		if (nr==2) return ((CosineDistributionImpl)distribution).b;
		return 0.0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(final AbstractRealDistribution distribution, final int nr, final double value) {
		final CosineDistributionImpl old=(CosineDistributionImpl)distribution;
		if (nr==1) return new CosineDistributionImpl(value,old.b);
		if (nr==2) return new CosineDistributionImpl(old.a,value);
		return null;
	}

	@Override
	protected String getToStringData(final AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(((CosineDistributionImpl)distribution).a)+";"+NumberTools.formatSystemNumber(((CosineDistributionImpl)distribution).b);
	}

	@Override
	public AbstractRealDistribution fromString(final String data, final double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=2) return null;
		return new CosineDistributionImpl(values[0],values[1]);
	}

	@Override
	protected AbstractRealDistribution cloneInt(final AbstractRealDistribution distribution) {
		return new CosineDistributionImpl((CosineDistributionImpl)distribution);
	}

	@Override
	protected boolean compareInt(final AbstractRealDistribution distribution1, final AbstractRealDistribution distribution2) {
		if (Math.abs(((CosineDistributionImpl)distribution1).a-((CosineDistributionImpl)distribution2).a)>DistributionTools.MAX_ERROR) return false;
		if (Math.abs(((CosineDistributionImpl)distribution1).b-((CosineDistributionImpl)distribution2).b)>DistributionTools.MAX_ERROR) return false;
		return true;
	}

	@Override
	protected Class<? extends CalcSymbolPreOperator> getCalcSymbolClass() {
		return CalcSymbolDistributionCosine.class;
	}
}

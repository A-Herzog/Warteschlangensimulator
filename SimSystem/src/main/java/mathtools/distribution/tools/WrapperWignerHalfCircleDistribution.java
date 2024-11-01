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
import mathtools.distribution.WignerHalfCircleDistributionImpl;
import parser.coresymbols.CalcSymbolPreOperator;
import parser.symbols.distributions.CalcSymbolDistributionWignerHalfCircle;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link WignerHalfCircleDistributionImpl}
 * @author Alexander Herzog
 * @see WignerHalfCircleDistributionImpl
 * @see DistributionTools
 */
public class WrapperWignerHalfCircleDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperWignerHalfCircleDistribution() {
		super(WignerHalfCircleDistributionImpl.class,true,true);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistWignerHalfCircle;
	}

	@Override
	protected String getThumbnailImageName() {
		return "wigner.png";
	}

	@Override
	protected String getWikipediaURL() {
		return DistributionTools.DistWignerHalfCircleWikipedia;
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(AbstractRealDistribution distribution) {
		return new DistributionWrapperInfo(distribution,0.0,((WignerHalfCircleDistributionImpl)distribution).m); /* Schiefe=0 immer */
	}

	@Override
	public AbstractRealDistribution getDistribution(double mean, double sd) {
		/* R^2/4=Var <=> 4Var=R^2 <=> 2sd=R */
		final double R=2*sd;
		return new WignerHalfCircleDistributionImpl(mean,R);
	}

	@Override
	public AbstractRealDistribution getDefaultDistribution() {
		return new WignerHalfCircleDistributionImpl(5,8);
	}

	@Override
	protected AbstractRealDistribution setMeanInt(AbstractRealDistribution distribution, double mean) {
		final double R=((WignerHalfCircleDistributionImpl)distribution).R;
		return new WignerHalfCircleDistributionImpl(mean,R);
	}

	@Override
	protected AbstractRealDistribution setStandardDeviationInt(AbstractRealDistribution distribution, double sd) {
		final double R=2*sd;
		final double m=((WignerHalfCircleDistributionImpl)distribution).m;
		return new WignerHalfCircleDistributionImpl(m,R);
	}

	@Override
	protected double getParameterInt(AbstractRealDistribution distribution, int nr) {
		if (nr==1) return ((WignerHalfCircleDistributionImpl)distribution).m;
		if (nr==2) return ((WignerHalfCircleDistributionImpl)distribution).R;
		return 0.0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(AbstractRealDistribution distribution, int nr, double value) {
		if (nr==1) return new WignerHalfCircleDistributionImpl(value,((WignerHalfCircleDistributionImpl)distribution).R);
		if (nr==2) return new WignerHalfCircleDistributionImpl(((WignerHalfCircleDistributionImpl)distribution).m,value);
		return null;
	}

	@Override
	protected String getToStringData(AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(((WignerHalfCircleDistributionImpl)distribution).m)+";"+NumberTools.formatSystemNumber(((WignerHalfCircleDistributionImpl)distribution).R);
	}

	@Override
	public AbstractRealDistribution fromString(String data, double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=2) return null;
		return new WignerHalfCircleDistributionImpl(values[0],values[1]);
	}

	@Override
	protected AbstractRealDistribution cloneInt(AbstractRealDistribution distribution) {
		return new WignerHalfCircleDistributionImpl(((WignerHalfCircleDistributionImpl)distribution).m,((WignerHalfCircleDistributionImpl)distribution).R);
	}

	@Override
	protected boolean compareInt(AbstractRealDistribution distribution1, AbstractRealDistribution distribution2) {
		if (Math.abs(((WignerHalfCircleDistributionImpl)distribution1).m-((WignerHalfCircleDistributionImpl)distribution2).m)>DistributionTools.MAX_ERROR) return false;
		if (Math.abs(((WignerHalfCircleDistributionImpl)distribution1).R-((WignerHalfCircleDistributionImpl)distribution2).R)>DistributionTools.MAX_ERROR) return false;
		return true;
	}

	@Override
	protected Class<? extends CalcSymbolPreOperator> getCalcSymbolClass() {
		return CalcSymbolDistributionWignerHalfCircle.class;
	}
}

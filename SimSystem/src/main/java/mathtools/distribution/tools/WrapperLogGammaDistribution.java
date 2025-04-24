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
import mathtools.distribution.LogGammaDistributionImpl;
import parser.coresymbols.CalcSymbolPreOperator;
import parser.symbols.distributions.CalcSymbolDistributionLogGamma;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link LogGammaDistributionImpl}
 * @author Alexander Herzog
 * @see LogGammaDistributionImpl
 * @see DistributionTools
 */
public class WrapperLogGammaDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperLogGammaDistribution() {
		super(LogGammaDistributionImpl.class,false,false);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistLogGamma;
	}

	@Override
	protected String getThumbnailImageName() {
		return "loggamma.png";
	}

	@Override
	protected String getWikipediaURL() {
		return DistributionTools.DistLogGammaWikipedia;
	}

	@Override
	protected String getWebAppDistributionName() {
		return "LogGamma";
	}

	@Override
	protected String getInfoHTML() {
		return null;
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(final AbstractRealDistribution distribution) {
		final LogGammaDistributionImpl logGamma=(LogGammaDistributionImpl)distribution;
		final String info1="a="+NumberTools.formatNumberMax(logGamma.a)+", b="+NumberTools.formatNumberMax(logGamma.b);
		return new DistributionWrapperInfo(distribution,null,null,info1,null);
	}

	@Override
	public AbstractRealDistribution getDistribution(double mean, double sd) {
		return null;
	}

	@Override
	public AbstractRealDistribution getDefaultDistribution() {
		return new LogGammaDistributionImpl(1,1);
	}

	@Override
	protected AbstractRealDistribution setMeanInt(final AbstractRealDistribution distribution, final double mean) {
		return null;
	}

	@Override
	protected AbstractRealDistribution setStandardDeviationInt(final AbstractRealDistribution distribution, final double sd) {
		return null;
	}

	@Override
	protected double getParameterInt(final AbstractRealDistribution distribution, final int nr) {
		if (nr==1) return ((LogGammaDistributionImpl)distribution).a;
		if (nr==2) return ((LogGammaDistributionImpl)distribution).b;
		return 0.0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(final AbstractRealDistribution distribution, final int nr, final double value) {
		final LogGammaDistributionImpl old=(LogGammaDistributionImpl)distribution;
		if (nr==1) return new LogGammaDistributionImpl(value,old.b);
		if (nr==2) return new LogGammaDistributionImpl(old.a,value);
		return null;
	}

	@Override
	protected String getToStringData(final AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(((LogGammaDistributionImpl)distribution).a)+";"+NumberTools.formatSystemNumber(((LogGammaDistributionImpl)distribution).b);
	}

	@Override
	public AbstractRealDistribution fromString(final String data, final double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=2) return null;
		return new LogGammaDistributionImpl(values[0],values[1]);
	}

	@Override
	protected AbstractRealDistribution cloneInt(final AbstractRealDistribution distribution) {
		return new LogGammaDistributionImpl((LogGammaDistributionImpl)distribution);
	}

	@Override
	protected boolean compareInt(final AbstractRealDistribution distribution1, final AbstractRealDistribution distribution2) {
		if (Math.abs(((LogGammaDistributionImpl)distribution1).a-((LogGammaDistributionImpl)distribution2).a)>DistributionTools.MAX_ERROR) return false;
		if (Math.abs(((LogGammaDistributionImpl)distribution1).b-((LogGammaDistributionImpl)distribution2).b)>DistributionTools.MAX_ERROR) return false;
		return true;
	}

	@Override
	protected Class<? extends CalcSymbolPreOperator> getCalcSymbolClass() {
		return CalcSymbolDistributionLogGamma.class;
	}
}

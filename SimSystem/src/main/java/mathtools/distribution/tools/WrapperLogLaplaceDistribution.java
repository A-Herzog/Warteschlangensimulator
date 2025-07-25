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
import mathtools.distribution.LogLaplaceDistributionImpl;
import parser.coresymbols.CalcSymbolPreOperator;
import parser.symbols.distributions.CalcSymbolDistributionLogLaplace;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link LogLaplaceDistributionImpl}
 * @author Alexander Herzog
 * @see LogLaplaceDistributionImpl
 * @see DistributionTools
 */
public class WrapperLogLaplaceDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperLogLaplaceDistribution() {
		super(LogLaplaceDistributionImpl.class,false,false);
	}

	@Override
	protected boolean canSetMeanExact() {
		return false;
	}

	@Override
	protected boolean canSetStandardDeviationExact() {
		return false;
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistLogLaplace;
	}

	@Override
	protected String getThumbnailImageName() {
		return "loglaplace.png";
	}

	@Override
	protected String getWikipediaURL() {
		return DistributionTools.DistLogLaplaceWikipedia;
	}

	@Override
	protected String getWebAppDistributionName() {
		return "LogLaplace";
	}

	@Override
	protected String getInfoHTML() {
		return null;
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(final AbstractRealDistribution distribution) {
		final LogLaplaceDistributionImpl logLaplace=(LogLaplaceDistributionImpl)distribution;
		final String info1="c="+NumberTools.formatNumber(logLaplace.c,7)+", s="+NumberTools.formatNumber(logLaplace.s);
		final double mode=logLaplace.s+((logLaplace.c<1)?0:1);
		return new DistributionWrapperInfo(distribution,null,mode,info1,null);
	}

	@Override
	public AbstractRealDistribution getDistribution(double mean, double sd) {
		/* c per binärer Suche bestimmen */
		final double variance=sd*sd;
		double cMin=2.000001;
		double cMax=1E4;
		while (cMax-cMin>0.0001) {
			final double c=(cMin+cMax)/2;
			final double testVariance=c*c/(c-2)/(c+2)-c*c*c*c/(c-1)/(c-1)/(c+1)/(c+1);
			if (testVariance>variance) cMin=c; else cMax=c;
		}
		final double c=(cMin+cMax)/2;

		/* s über die Formel für den Erwartungswert bestimmen */
		final double s=mean-c*c/(c-1)/(c+1);

		return new LogLaplaceDistributionImpl(c,s);
	}

	@Override
	public AbstractRealDistribution getDefaultDistribution() {
		return new LogLaplaceDistributionImpl(2.5,5);
	}

	@Override
	protected AbstractRealDistribution setMeanInt(final AbstractRealDistribution distribution, final double mean) {
		final double variance=distribution.getNumericalVariance();
		if (variance==Double.POSITIVE_INFINITY) return null;
		return getDistribution(mean,Math.sqrt(variance));
	}

	@Override
	protected AbstractRealDistribution setStandardDeviationInt(final AbstractRealDistribution distribution, final double sd) {
		if (sd<=0) return null;
		return getDistribution(distribution.getNumericalMean(),sd);
	}

	@Override
	protected double getParameterInt(final AbstractRealDistribution distribution, final int nr) {
		if (nr==1) return ((LogLaplaceDistributionImpl)distribution).c;
		if (nr==2) return ((LogLaplaceDistributionImpl)distribution).s;
		return 0.0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(final AbstractRealDistribution distribution, final int nr, final double value) {
		final LogLaplaceDistributionImpl old=(LogLaplaceDistributionImpl)distribution;
		if (nr==1) return new LogLaplaceDistributionImpl(value,old.s);
		if (nr==2) return new LogLaplaceDistributionImpl(old.c,value);
		return null;
	}

	@Override
	protected String getToStringData(final AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(((LogLaplaceDistributionImpl)distribution).c)+";"+NumberTools.formatSystemNumber(((LogLaplaceDistributionImpl)distribution).s);
	}

	@Override
	public AbstractRealDistribution fromString(final String data, final double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=2) return null;
		return new LogLaplaceDistributionImpl(values[0],values[1]);
	}

	@Override
	protected AbstractRealDistribution cloneInt(final AbstractRealDistribution distribution) {
		return new LogLaplaceDistributionImpl((LogLaplaceDistributionImpl)distribution);
	}

	@Override
	protected boolean compareInt(final AbstractRealDistribution distribution1, final AbstractRealDistribution distribution2) {
		if (Math.abs(((LogLaplaceDistributionImpl)distribution1).c-((LogLaplaceDistributionImpl)distribution2).c)>DistributionTools.MAX_ERROR) return false;
		if (Math.abs(((LogLaplaceDistributionImpl)distribution1).s-((LogLaplaceDistributionImpl)distribution2).s)>DistributionTools.MAX_ERROR) return false;
		return true;
	}

	@Override
	protected Class<? extends CalcSymbolPreOperator> getCalcSymbolClass() {
		return CalcSymbolDistributionLogLaplace.class;
	}
}

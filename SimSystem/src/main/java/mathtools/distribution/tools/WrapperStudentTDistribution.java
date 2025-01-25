/**
 * Copyright 2023 Alexander Herzog
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
import mathtools.distribution.StudentTDistributionImpl;
import parser.coresymbols.CalcSymbolPreOperator;
import parser.symbols.distributions.CalcSymbolDistributionStudentT;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link StudentTDistributionImpl}
 * @author Alexander Herzog
 * @see StudentTDistributionImpl
 * @see DistributionTools
 */
public class WrapperStudentTDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperStudentTDistribution() {
		super(StudentTDistributionImpl.class);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistStudentT;
	}

	@Override
	protected String getThumbnailImageName() {
		return "studentT.png";
	}

	@Override
	protected String getWikipediaURL() {
		return DistributionTools.DistStudentTWikipedia;
	}

	@Override
	protected String getWebAppDistributionName() {
		return "StudentT";
	}

	@Override
	protected String getInfoHTML() {
		return null;
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(AbstractRealDistribution distribution) {
		final StudentTDistributionImpl dist=(StudentTDistributionImpl)distribution;
		final double E=dist.getNumericalMean();
		final double Std=Math.sqrt(dist.getNumericalVariance());
		final Double Sk=(dist.nu>3)?0.0:null;

		return new DistributionWrapperInfo(E,Std,Sk,E,null,null);
	}

	@Override
	public AbstractRealDistribution getDistribution(double mean, double sd) {
		final double nu;
		if (sd>1) {
			final double variance=sd*sd;
			nu=2*variance/(variance-1);
		} else {
			nu=5;
		}
		return new StudentTDistributionImpl(mean,nu);
	}

	@Override
	public AbstractRealDistribution getDefaultDistribution() {
		return new StudentTDistributionImpl(0,5);
	}

	@Override
	protected AbstractRealDistribution setMeanInt(AbstractRealDistribution distribution, double mean) {
		final StudentTDistributionImpl oldDist=(StudentTDistributionImpl)distribution;
		return new StudentTDistributionImpl(mean,oldDist.nu);
	}

	@Override
	protected AbstractRealDistribution setStandardDeviationInt(AbstractRealDistribution distribution, double sd) {
		final StudentTDistributionImpl oldDist=(StudentTDistributionImpl)distribution;
		final double nu;
		if (sd>1) {
			final double variance=sd*sd;
			nu=2*variance/(variance-1);
		} else {
			nu=5;
		}
		return new StudentTDistributionImpl(oldDist.mu,nu);
	}

	@Override
	protected double getParameterInt(AbstractRealDistribution distribution, int nr) {
		if (nr==1) return ((StudentTDistributionImpl)distribution).mu;
		if (nr==2) return ((StudentTDistributionImpl)distribution).nu;
		return 0.0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(AbstractRealDistribution distribution, int nr, double value) {
		final StudentTDistributionImpl oldDist=(StudentTDistributionImpl)distribution;
		if (nr==1) return new StudentTDistributionImpl(value,oldDist.nu);
		if (nr==2) return new StudentTDistributionImpl(oldDist.mu,value);
		return null;
	}

	@Override
	protected String getToStringData(AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(((StudentTDistributionImpl)distribution).mu)+";"+NumberTools.formatSystemNumber(((StudentTDistributionImpl)distribution).nu);
	}

	@Override
	public AbstractRealDistribution fromString(String data, double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=2) return null;
		return new StudentTDistributionImpl(values[0],values[1]);
	}

	@Override
	protected AbstractRealDistribution cloneInt(AbstractRealDistribution distribution) {
		return new StudentTDistributionImpl(((StudentTDistributionImpl)distribution).mu,((StudentTDistributionImpl)distribution).nu);
	}

	@Override
	protected boolean compareInt(AbstractRealDistribution distribution1, AbstractRealDistribution distribution2) {
		if (Math.abs(((StudentTDistributionImpl)distribution1).mu-((StudentTDistributionImpl)distribution2).mu)>DistributionTools.MAX_ERROR) return false;
		if (Math.abs(((StudentTDistributionImpl)distribution1).nu-((StudentTDistributionImpl)distribution2).nu)>DistributionTools.MAX_ERROR) return false;
		return true;
	}

	@Override
	protected Class<? extends CalcSymbolPreOperator> getCalcSymbolClass() {
		return CalcSymbolDistributionStudentT.class;
	}
}

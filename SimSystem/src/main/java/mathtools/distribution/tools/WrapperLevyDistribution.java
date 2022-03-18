/**
 * Copyright 2022 Alexander Herzog
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
import mathtools.distribution.LevyDistribution;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link LevyDistribution}
 * @author Alexander Herzog
 * @see LevyDistribution
 * @see DistributionTools
 */
public class WrapperLevyDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperLevyDistribution() {
		super(LevyDistribution.class);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistLevy;
	}

	@Override
	protected String getThumbnailImageName() {
		return "levy.png";
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(AbstractRealDistribution distribution) {
		return new DistributionWrapperInfo(null,null,null,null,null);
	}

	@Override
	public AbstractRealDistribution getDistribution(double mean, double sd) {
		return null;
	}

	@Override
	public AbstractRealDistribution getDefaultDistribution() {
		return new LevyDistribution(0,2);
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
		if (nr==1) return ((LevyDistribution)distribution).mu;
		if (nr==2) return ((LevyDistribution)distribution).c;
		return 0.0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(AbstractRealDistribution distribution, int nr, double value) {
		if (nr==1) return new LevyDistribution(value,((LevyDistribution)distribution).c);
		if (nr==2) return new LevyDistribution(((LevyDistribution)distribution).mu,value);
		return null;
	}

	@Override
	protected String getToStringData(AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(((LevyDistribution)distribution).mu)+";"+NumberTools.formatSystemNumber(((LevyDistribution)distribution).c);
	}

	@Override
	public AbstractRealDistribution fromString(String data, double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=2) return null;
		return new LevyDistribution(values[0],values[1]);
	}

	@Override
	protected AbstractRealDistribution cloneInt(AbstractRealDistribution distribution) {
		return ((LevyDistribution)distribution).clone();
	}

	@Override
	protected boolean compareInt(AbstractRealDistribution distribution1, AbstractRealDistribution distribution2) {
		if (Math.abs(((LevyDistribution)distribution1).mu-((LevyDistribution)distribution2).mu)>DistributionTools.MAX_ERROR) return false;
		if (Math.abs(((LevyDistribution)distribution1).c-((LevyDistribution)distribution2).c)>DistributionTools.MAX_ERROR) return false;
		return true;

	}
}

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

import mathtools.distribution.NeverDistributionImpl;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link NeverDistributionImpl}
 * @author Alexander Herzog
 * @see NeverDistributionImpl
 * @see DistributionTools
 */
public class WrapperNeverDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperNeverDistribution() {
		super(NeverDistributionImpl.class);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistNever;
	}

	@Override
	protected String getThumbnailImageName() {
		return null;
	}

	@Override
	public boolean isHiddenInNamesList() {
		return true;
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(final AbstractRealDistribution distribution) {
		return new DistributionWrapperInfo(null,null,null,null,"E="+DistributionTools.DistNever[0],null);
	}

	@Override
	public AbstractRealDistribution getDistribution(final double mean, final double sd) {
		return new NeverDistributionImpl();
	}

	@Override
	public AbstractRealDistribution getDefaultDistribution() {
		return getDistribution(100,100);
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
		return 0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(final AbstractRealDistribution distribution, final int nr, final double value) {
		return null;
	}

	@Override
	protected String getToStringData(final AbstractRealDistribution distribution) {
		return null;
	}

	@Override
	public AbstractRealDistribution fromString(final String data, final double maxXValue) {
		return new NeverDistributionImpl();
	}

	@Override
	protected AbstractRealDistribution cloneInt(final AbstractRealDistribution distribution) {
		return new NeverDistributionImpl();
	}

	@Override
	protected boolean compareInt(final AbstractRealDistribution distribution1, final AbstractRealDistribution distribution2) {
		return true;
	}
}

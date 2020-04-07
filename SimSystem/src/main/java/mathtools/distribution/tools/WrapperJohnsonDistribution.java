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
import mathtools.distribution.JohnsonDistributionImpl;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link JohnsonDistributionImpl}
 * @author Alexander Herzog
 * @see JohnsonDistributionImpl
 * @see DistributionTools
 */
public class WrapperJohnsonDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperJohnsonDistribution() {
		super(JohnsonDistributionImpl.class);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistJohnson;
	}

	@Override
	protected String getThumbnailImageName() {
		return "johnson.png";
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(AbstractRealDistribution distribution) {
		final double gamma=((JohnsonDistributionImpl)distribution).gamma;
		final double xi=((JohnsonDistributionImpl)distribution).xi;
		final double delta=((JohnsonDistributionImpl)distribution).delta;
		final double lambda=((JohnsonDistributionImpl)distribution).lambda;
		final String info="gamma="+NumberTools.formatNumber(gamma)+"; xi="+NumberTools.formatNumber(xi)+"; delta="+NumberTools.formatNumber(delta)+"; lambda="+NumberTools.formatNumber(lambda);
		return new DistributionWrapperInfo(distribution,info,null);
	}

	@Override
	public AbstractRealDistribution getDistribution(double mean, double sd) {
		return null;
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
		if (nr==1) return ((JohnsonDistributionImpl)distribution).gamma;
		if (nr==2) return ((JohnsonDistributionImpl)distribution).xi;
		if (nr==3) return ((JohnsonDistributionImpl)distribution).delta;
		if (nr==4) return ((JohnsonDistributionImpl)distribution).lambda;
		return 0.0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(AbstractRealDistribution distribution, int nr, double value) {
		final JohnsonDistributionImpl j=(JohnsonDistributionImpl)distribution;
		if (nr==1) return new JohnsonDistributionImpl(value,j.xi,j.delta,j.lambda);
		if (nr==2) return new JohnsonDistributionImpl(j.gamma,value,j.delta,j.lambda);
		if (nr==3) return new JohnsonDistributionImpl(j.gamma,j.xi,value,j.lambda);
		if (nr==4) return new JohnsonDistributionImpl(j.gamma,j.xi,j.delta,value);
		return null;
	}

	@Override
	protected String getToStringData(AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(((JohnsonDistributionImpl)distribution).gamma)+";"+NumberTools.formatSystemNumber(((JohnsonDistributionImpl)distribution).xi)+";"+NumberTools.formatSystemNumber(((JohnsonDistributionImpl)distribution).delta)+";"+NumberTools.formatSystemNumber(((JohnsonDistributionImpl)distribution).lambda);
	}

	@Override
	public AbstractRealDistribution fromString(String data, double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=4) return null;
		return new JohnsonDistributionImpl(values[0],values[1],values[2],values[3]);
	}

	@Override
	protected AbstractRealDistribution cloneInt(AbstractRealDistribution distribution) {
		return ((JohnsonDistributionImpl)distribution).clone();
	}

	@Override
	protected boolean compareInt(AbstractRealDistribution distribution1, AbstractRealDistribution distribution2) {
		if (Math.abs(((JohnsonDistributionImpl)distribution1).gamma-((JohnsonDistributionImpl)distribution2).gamma)>DistributionTools.MAX_ERROR) return false;
		if (Math.abs(((JohnsonDistributionImpl)distribution1).xi-((JohnsonDistributionImpl)distribution2).xi)>DistributionTools.MAX_ERROR) return false;
		if (Math.abs(((JohnsonDistributionImpl)distribution1).delta-((JohnsonDistributionImpl)distribution2).delta)>DistributionTools.MAX_ERROR) return false;
		if (Math.abs(((JohnsonDistributionImpl)distribution1).lambda-((JohnsonDistributionImpl)distribution2).lambda)>DistributionTools.MAX_ERROR) return false;
		return true;
	}
}

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
import mathtools.distribution.PowerDistributionImpl;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link PowerDistributionImpl}
 * @author Alexander Herzog
 * @see PowerDistributionImpl
 * @see DistributionTools
 */
public class WrapperPowerDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperPowerDistribution() {
		super(PowerDistributionImpl.class);
	}

	@Override
	protected String[] getNames() {
		return DistributionTools.DistPower;
	}

	@Override
	protected String getThumbnailImageName() {
		return "power.png";
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(AbstractRealDistribution distribution) {
		final PowerDistributionImpl powerDist=(PowerDistributionImpl)distribution;
		final double a=powerDist.a;
		final double b=powerDist.b;
		final double c=powerDist.c;
		final String info=DistributionTools.DistRange+"=["+NumberTools.formatNumber(a,3)+";"+NumberTools.formatNumber(b,3)+"]; c="+NumberTools.formatNumber(c,3);
		return new DistributionWrapperInfo(distribution,null,info,null); /* Schiefe=null */
	}

	@Override
	public AbstractRealDistribution getDistribution(double mean, double sd) {
		return null;
	}

	@Override
	public AbstractRealDistribution getDefaultDistribution() {
		return new PowerDistributionImpl(50,150,5);
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
		if (nr==1) return ((PowerDistributionImpl)distribution).a;
		if (nr==2) return ((PowerDistributionImpl)distribution).b;
		if (nr==3) return ((PowerDistributionImpl)distribution).c;
		return 0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(AbstractRealDistribution distribution, int nr, double value) {
		final PowerDistributionImpl p=(PowerDistributionImpl)distribution;
		if (nr==1) return new PowerDistributionImpl(value,p.b,p.c);
		if (nr==2) return new PowerDistributionImpl(p.a,value,p.c);
		if (nr==3) return new PowerDistributionImpl(p.a,p.b,value);
		return null;
	}

	@Override
	protected String getToStringData(AbstractRealDistribution distribution) {
		return NumberTools.formatSystemNumber(((PowerDistributionImpl)distribution).a)+";"+NumberTools.formatSystemNumber(((PowerDistributionImpl)distribution).b)+";"+NumberTools.formatSystemNumber(((PowerDistributionImpl)distribution).c);
	}

	@Override
	public AbstractRealDistribution fromString(String data, double maxXValue) {
		final double[] values=getDoubleArray(data);
		if (values.length!=3) return null;
		return new PowerDistributionImpl(values[0],values[1],values[2]);
	}

	@Override
	protected AbstractRealDistribution cloneInt(AbstractRealDistribution distribution) {
		return ((PowerDistributionImpl)distribution).clone();
	}

	@Override
	protected boolean compareInt(AbstractRealDistribution distribution1, AbstractRealDistribution distribution2) {
		if (Math.abs(((PowerDistributionImpl)distribution1).a-((PowerDistributionImpl)distribution2).a)>DistributionTools.MAX_ERROR) return false;
		if (Math.abs(((PowerDistributionImpl)distribution1).b-((PowerDistributionImpl)distribution2).b)>DistributionTools.MAX_ERROR) return false;
		if (Math.abs(((PowerDistributionImpl)distribution1).c-((PowerDistributionImpl)distribution2).c)>DistributionTools.MAX_ERROR) return false;
		return true;
	}
}

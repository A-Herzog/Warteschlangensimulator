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
package mathtools.distribution;

import java.io.Serializable;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * @author Alexander Herzog
 * @version 1.0
 */
public class OnePointDistributionImpl extends AbstractRealDistribution implements Serializable, Cloneable, DistributionWithRandom  {
	private static final long serialVersionUID = -8317785247341481553L;

	/**
	 * Punkt, auf dem das gesamte Wahrscheinlichkeitsgewicht liegt<br>
	 * Auf diese Variable kann auch problemlos schreibend zugegriffen werden.
	 */
	public double point;

	/**
	 * Konstruktor der Ein-Punkt-Verteilung
	 * @param point	Punkt, auf dem das gesamte Wahrscheinlichkeitsgewicht liegt
	 */
	public OnePointDistributionImpl(double point) {super(null); this.point=point;}

	/**
	 * Konstruktor der Ein-Punkt-Verteilung
	 */
	public OnePointDistributionImpl() {super(null); this.point=0;}

	@Override
	public double cumulativeProbability(double x) {return (x<point)?0:1;}

	@Override
	public double inverseCumulativeProbability(final double p) {return point;}

	@Override
	public OnePointDistributionImpl clone() {
		try {
			OnePointDistributionImpl dist=(OnePointDistributionImpl)super.clone();
			dist.point=point;
			return dist;
		} catch (CloneNotSupportedException e) {return null;}
	}

	@Override
	public double density(double x) {if (x==point) return Double.MAX_VALUE; else return 0;}

	@Override
	public double getNumericalMean() {return point;}

	@Override
	public double getNumericalVariance() {return 0;}

	@Override
	public double getSupportLowerBound() {return point;}

	@Override
	public double getSupportUpperBound() {return point;}

	@Override
	public boolean isSupportLowerBoundInclusive() {return true;}

	@Override
	public boolean isSupportUpperBoundInclusive() {return false;}

	@Override
	public boolean isSupportConnected() {return true;}

	@Override
	public double random(RandomGenerator generator) {
		return point;
	}
}

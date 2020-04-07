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

import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;

/**
 * Dies ist der standardmäßig von <code>DistributionRandomNumber</code>
 * verwendete Zufallszahlengenerator.<br>
 * Das System ist Thread-Local, d.h. pro Thread wird ein eigener
 * Generator mit eigenem Seed verwendet. Eine Synchronisation ist daher nicht
 * nötig (d.h. das System ist schnell). Intern verwendet der Generator
 * <code>ThreadLocalRandom</code>. Dies hat zur Folge, dass keine Seed
 * gesetzt werden können.
 * @see DistributionRandomNumber
 * @see ThreadLocalRandom
 * @author Alexander Herzog
 * @version 1.1
 */
public class ThreadLocalRandomGenerator implements RandomGenerator {
	private static final double TwoTimesPI=2*Math.PI;

	@Override
	public void setSeed(int seed) {
		/* ThreadLocalRandom.current().setSeed(seed); */
	}

	@Override
	public void setSeed(int[] seed) {
		/* if (seed.length>0) ThreadLocalRandom.current().setSeed(seed[0]); */
	}

	@Override
	public void setSeed(long seed) {
		/* ThreadLocalRandom.current().setSeed(seed); */
	}

	@Override
	public void nextBytes(byte[] bytes) {
		ThreadLocalRandom.current().nextBytes(bytes);
	}

	@Override
	public int nextInt() {
		return ThreadLocalRandom.current().nextInt();
	}

	@Override
	public int nextInt(int n) {
		return ThreadLocalRandom.current().nextInt(n);
	}

	@Override
	public long nextLong() {
		return ThreadLocalRandom.current().nextLong();
	}

	@Override
	public boolean nextBoolean() {
		return ThreadLocalRandom.current().nextBoolean();
	}

	@Override
	public float nextFloat() {
		return ThreadLocalRandom.current().nextFloat();
	}

	@Override
	public double nextDouble() {
		return ThreadLocalRandom.current().nextDouble();
	}

	@Override
	public double nextGaussian() {
		return FastMath.cos(TwoTimesPI*nextDouble())*StrictMath.sqrt(-2*Math.log(nextDouble())); /* StrictMath.log ist schneller als FastMath. Math.log laut Code StrictMath.log auf, aber in Wirklichkeit scheint hier der Compiler Magic zu machen, so dass Math.log schneller ist. */
	}
}

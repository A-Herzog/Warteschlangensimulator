/**
 * Copyright 2026 Alexander Herzog
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

import org.apache.commons.math3.random.RandomGenerator;

/**
 * Kapselt einen AES-Counter-Generator.
 */
public class AESCounterRandom implements RandomGenerator {

	/**
	 * Internes AESCounter-Objekt
	 */
	private AESCounterCore rng;

	/**
	 * Konstruktor
	 * @param seed	Seed-Wert
	 */
	public AESCounterRandom(final int[] seed) {
		setSeed(seed);
	}

	/**
	 * Konstruktor
	 * @param seed	Seed-Wert
	 */
	public AESCounterRandom(final byte[] seed) {
		setSeed(seed);
	}

	/**
	 * Konstruktor
	 * @param seed	Seed-Wert
	 */
	public AESCounterRandom(final long seed) {
		setSeed(seed);
	}

	/**
	 * Konstruktor
	 */
	public AESCounterRandom() {
		setSeed(Thread.currentThread().getId()+System.currentTimeMillis());
	}

	@Override
	public void setSeed(final int seed) {
		setSeed((long)seed);
	}

	@Override
	public void setSeed(final int[] seed) {
		if (seed==null || seed.length==0) {
			setSeed(0L);
			return;
		}
		/* einfache Mischung eines int-Arrays in ein long */
		long s = 0x9E3779B97F4A7C15L;
		for (int v : seed) {
			s ^= v + 0x9E3779B97F4A7C15L + (s << 6) + (s >>> 2);
		}
		setSeed(s);
	}


	/**
	 * Sets the seed of the underlying random number generator using an
	 * <code>byte</code> array seed.
	 * <p>Sequences of values generated starting with the same seeds
	 * should be identical.
	 * </p>
	 * @param seed the seed value
	 */
	public void setSeed(final byte[] seed) {
		rng=new AESCounterCore(seed);
	}

	@Override
	public void setSeed(final long seed) {
		rng=new AESCounterCore(seed);
	}

	@Override
	public void nextBytes(final byte[] bytes) {
		rng.nextBytes(bytes);
	}

	@Override
	public int nextInt() {
		return rng.nextInt();
	}

	@Override
	public int nextInt(int n) {
		// Commons Math spezifiziert Verhalten für n>0
		if (n <= 0) throw new IllegalArgumentException("n must be positive");
		// einfache Variante wie Random.nextInt(n)
		int bits, val;
		do {
			bits = rng.nextInt() >>> 1;
			val = bits % n;
		} while (bits - val + (n - 1) < 0);
		return val;
	}

	@Override
	public long nextLong() {
		return rng.nextLong();
	}

	@Override
	public boolean nextBoolean() {
		return rng.nextBoolean();
	}

	@Override
	public float nextFloat() {
		return rng.nextFloat();
	}

	@Override
	public double nextDouble() {
		return rng.nextDouble();
	}

	/**
	 * Es werden immer zwei Pseudozufallszahlen gleichzeitig generiert.
	 * Steht eine zweite Zahl direkt zur Verfügung?
	 * @see #nextRandom
	 * @see #nextGaussian()
	 */
	private boolean randomAvailable=false;

	/**
	 * Es werden immer zwei Pseudozufallszahlen gleichzeitig generiert.
	 * Wenn eine zweite zur Verfügung steht, so wird sie hier angeboten.
	 * @see #randomAvailable
	 * @see #nextGaussian()
	 */
	private double nextRandom;

	@Override
	public double nextGaussian() {
		if (randomAvailable) {
			randomAvailable=false;
			return nextRandom;
		}

		double q=10, u=0, v=0;
		while (q==0 || q>=1) {
			u=2*nextDouble()-1;
			v=2*nextDouble()-1;
			q=u*u+v*v;
		}
		final double p=StrictMath.sqrt(-2 * StrictMath.log(q)/q);
		nextRandom=v*p;
		randomAvailable=true;
		return u*p;
	}
}

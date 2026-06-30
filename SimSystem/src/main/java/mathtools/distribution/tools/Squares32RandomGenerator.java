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
 * Squares 32 pseudorandom number generator<br><br>
 * Widynski, Bernard. "Squares: a fast counter-based RNG." arXiv preprint arXiv:2004.06278 (2020).<br>
 * <a href="https://arxiv.org/pdf/2004.06278">https://arxiv.org/pdf/2004.06278</a>
 */
public class Squares32RandomGenerator implements RandomGenerator {
	/**
	 * Schlüssel
	 */
	private long key;

	/**
	 * Zähler
	 */
	private long ctr;

	/**
	 * Konstruktor
	 */
	public Squares32RandomGenerator() {
		this(Thread.currentThread().getId()+System.currentTimeMillis());
	}

	/**
	 * Konstruktor
	 * @param seed	Seed-Wert
	 */
	public Squares32RandomGenerator(final long seed) {
		setSeed(seed);
	}

	/**
	 * Mische den Seed in einen Key um.
	 * @param seed	Zähler-Seed-Wert
	 * @return	Schlüssel-Seed-Wert
	 */
	private static long mixKey(final long seed) {
		long z = seed + 0x9E3779B97F4A7C15L;
		z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
		z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
		z = z ^ (z >>> 31);
		if (z == 0L) z=0x1L; /* Schlüssel darf nicht 0 sein. */
		return z;
	}

	@Override
	public void setSeed(final int seed) {
		setSeed(seed & 0xffffffffL);
	}

	@Override
	public void setSeed(final int[] seed) {
		long s=0L;
		if (seed!=null) {
			for (int v : seed) {
				s=31L*s+(v & 0xffffffffL);
			}
		}
		setSeed(s);
	}

	@Override
	public void setSeed(final long seed) {
		this.ctr=0L;
		this.key=mixKey(seed);
	}

	@Override
	public void nextBytes(final byte[] bytes) {
		int i=0;
		while (i<bytes.length) {
			int rnd=nextInt();
			for (int b=0;b<4 && i<bytes.length; b++) {
				bytes[i++]=(byte)(rnd & 0xFF);
				rnd >>>= 8;
			}
		}
	}

	@Override
	public int nextInt() {
		final int r=squares32(ctr,key);
		ctr++;
		return r;
	}

	@Override
	public int nextInt(final int n) {
		if (n<=0) throw new IllegalArgumentException("n must be positive");

		if ((n & -n) == n) return (int)((n*(long)(nextInt() & 0x7fffffff)) >> 31);

		int bits, val;
		do {
			bits=nextInt() & 0x7fffffff;
			val=bits % n;
		} while (bits-val+(n-1)<0);
		return val;
	}

	@Override
	public long nextLong() {
		long hi=nextInt() & 0xffffffffL;
		long lo=nextInt() & 0xffffffffL;
		return (hi<<32) | lo;
	}

	@Override
	public boolean nextBoolean() {
		return (nextInt() & 1)!=0;
	}

	@Override
	public float nextFloat() {
		return (nextInt() >>> 8) * (1.0f/(1 << 24));
	}

	@Override
	public double nextDouble() {
		long hi=nextInt() & 0xffffffffL;
		long lo=nextInt() & 0xffffffffL;
		long bits=((hi << 21) ^ (lo >>> 11)) & ((1L << 53) - 1);
		return bits*(1.0/(1L << 53));
	}

	/**
	 * Eigentlicher Squares32-Algorithmus.
	 * @param ctr	Aktueller Wert des Zählers
	 * @param key	Schlüssel
	 * @return	Pseudo-Zufallszahl
	 */
	public static int squares32(final long ctr, final long key) {
		long x;
		final long y=x=ctr*key;
		final long z=y+key;

		x=x*x+y;
		x=Long.rotateRight(x,32);

		x=x*x+z;
		x=Long.rotateRight(x,32);

		x=x*x+y;
		x=Long.rotateRight(x,32);

		return (int)((x*x+z) >>> 32);
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

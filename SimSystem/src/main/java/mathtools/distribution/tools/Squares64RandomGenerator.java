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
 * Squares 64 pseudorandom number generator<br><br>
 * Widynski, Bernard. "Squares: a fast counter-based RNG." arXiv preprint arXiv:2004.06278 (2020).<br>
 * <a href="https://arxiv.org/pdf/2004.06278">https://arxiv.org/pdf/2004.06278</a>
 */
public class Squares64RandomGenerator implements RandomGenerator {
	/**
	 * Schlüssel<br>
	 * (sollte "unregelmäßiges" Bitmuster haben, siehe Paper)
	 */
	private long key;

	/**
	 * Zähler
	 */
	private long ctr;

	/**
	 * Puffer mit erzeugten Zufallsbits
	 * @see #bufferBitsLeft
	 */
	private long buffer;

	/**
	 * Anzahl an im Puffer noch verfügbaren Bits
	 * @see #buffer
	 */
	private int bufferBitsLeft;

	/**
	 * Konstruktor
	 */
	public Squares64RandomGenerator() {
		this(Thread.currentThread().getId()+System.currentTimeMillis());
	}

	/**
	 * Konstruktor
	 * @param seed	Seed-Wert
	 */
	public Squares64RandomGenerator(final long seed) {
		setSeed(seed);
	}

	/**
	 * Konstruktor
	 * @param ctrSeed	Seed-Wert für den Zähler
	 * @param keySeed	Seed-Wert für den Schlüssel
	 */
	public Squares64RandomGenerator(final long ctrSeed, final long keySeed) {
		this.ctr=ctrSeed;
		this.key=mixKey(keySeed);
		this.bufferBitsLeft=0;
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
		this.bufferBitsLeft=0;
	}

	@Override
	public void nextBytes(final byte[] bytes) {
		int i=0;
		while (i<bytes.length) {
			long rnd=nextLong();
			for (int b=0; b<8 && i<bytes.length; b++) {
				bytes[i++]=(byte)(rnd & 0xFF);
				rnd >>>= 8;
			}
		}
	}

	@Override
	public int nextInt() {
		if (bufferBitsLeft<32) {
			buffer=nextLong();
			bufferBitsLeft=64;
		}
		int result=(int)buffer;
		buffer >>>= 32;
		bufferBitsLeft-=32;
		return result;
	}

	@Override
	public int nextInt(final int n) {
		if (n<=0) throw new IllegalArgumentException("n must be positive");

		if ((n & -n) == n) { /* Potenz von 2 */
			return (int)((n*(long)(nextInt() & 0x7fffffff)) >> 31);
		}

		int bits, val;
		do {
			bits=nextInt() & 0x7fffffff;
			val=bits%n;
		} while (bits-val+(n-1)<0);
		return val;
	}

	@Override
	public long nextLong() {
		final long result=squares64(ctr,key);
		ctr++;
		return result;
	}

	@Override
	public boolean nextBoolean() {
		if (bufferBitsLeft==0) {
			buffer=nextLong();
			bufferBitsLeft=64;
		}
		boolean result=(buffer & 1L) != 0L;
		buffer >>>= 1;
		bufferBitsLeft--;
		return result;
	}

	@Override
	public float nextFloat() {
		return (nextInt() >>> 8)*(1.0f/(1 << 24));
	}

	@Override
	public double nextDouble() {
		final long l=nextLong() >>> 11;
		return l*(1.0/(1L << 53));
	}

	/**
	 * Eigentlicher Squares64-Algorithmus.
	 * @param ctr	Aktueller Wert des Zählers
	 * @param key	Schlüssel
	 * @return	Pseudo-Zufallszahl
	 */
	public static long squares64(final long ctr, final long key) {
		long x;
		final long y=x=ctr*key;
		final long z=y+key;

		x=x*x+y;
		x=Long.rotateRight(x,32);

		x=x*x+z;
		x=Long.rotateRight(x,32);

		x=x*x+y;
		x=Long.rotateRight(x,32);

		final long t=x=x*x+z;
		x=Long.rotateRight(x,32);

		return t ^ ((x*x+y) >>> 32);
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

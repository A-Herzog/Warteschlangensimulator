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

import java.util.Arrays;

import org.apache.commons.math3.random.RandomGenerator;

/**
 * SFC64 pseudorandom number generator<br><br>
 * Matsumoto, Makoto, and Takuji Nishimura. "Dynamic creation of pseudorandom number generators."
 * Monte-Carlo and Quasi-Monte Carlo Methods 1998: Proceedings of a Conference held at the Claremont Graduate University, Claremont, California, USA,
 * June 22–26, 1998. Berlin, Heidelberg: Springer Berlin Heidelberg, 2000.
 */
public class SFC64 implements RandomGenerator {
	/**
	 * Interner Zustand: a
	 */
	private long a;

	/**
	 * Interner Zustand: b
	 */
	private long b;

	/**
	 * Interner Zustand: c
	 */
	private long c;

	/**
	 * Interner Zustand: Zähler
	 */
	private long counter;

	/**
	 * Konstruktor
	 */
	public SFC64() {
		setSeed(Thread.currentThread().getId()+System.currentTimeMillis());
	}

	/**
	 * Konstruktor
	 * @param seed	Seed-Wert
	 */
	public SFC64(final long seed) {
		setSeed(seed);
	}

	@Override
	public long nextLong() {
		final long t=a^(a<<11);
		a=b;
		b=c;
		c=c+(c>>>19)+(t^(t>>>8));
		counter++;
		return c+counter;
	}

	@Override
	public int nextInt() {
		return (int) (nextLong() >>> 32);
	}

	@Override
	public double nextDouble() {
		/* Generates a double in [0.0, 1.0) */
		return (nextLong() >>> 11) * 0x1.0p-53;
	}

	@Override
	public void setSeed(int seed) {
		setSeed((long)seed);
	}

	@Override
	public void setSeed(int[] seed) {
		if (seed==null) {
			seed=new int[4];
			for (int i=0;i<4;i++) if (seed[i]==0) seed[i]=(int)((System.nanoTime()+i) & 0xffffffffL);
		}
		if (seed.length<2) {
			seed=Arrays.copyOf(seed,2);
			for (int i=0;i<2;i++) if (seed[i]==0) seed[i]=(i==0)?1:(seed[i-1]+1);
		}

		final long l1=seed[0];
		final long l2=seed[1];
		setSeed(l1 | l2 << 32);
	}

	@Override
	public void setSeed(long seed) {
		this.a=seed;
		this.b=seed;
		this.c=seed;
		this.counter = 1;
		/* Warm up the generator to mix the state properly */
		for (int i=0;i<12;i++) nextLong();
	}

	@Override
	public void nextBytes(byte[] bytes) {
		final int len=bytes.length;
		final int indexLoopLimit=(len & 0x7ffffff8);

		/* Start filling in the byte array, 8 bytes at a time. */
		int index = 0;
		while (index < indexLoopLimit) {
			final long random = nextLong();
			bytes[index++] = (byte) random;
			bytes[index++] = (byte) (random >>> 8);
			bytes[index++] = (byte) (random >>> 16);
			bytes[index++] = (byte) (random >>> 24);
			bytes[index++] = (byte) (random >>> 32);
			bytes[index++] = (byte) (random >>> 40);
			bytes[index++] = (byte) (random >>> 48);
			bytes[index++] = (byte) (random >>> 56);
		}

		/* Fill in the remaining bytes. */
		final int indexLimit=len;
		if (index < indexLimit) {
			long random = nextLong();
			for (;;) {
				bytes[index++] = (byte) random;
				if (index == indexLimit) {
					break;
				}
				random >>>= 8;
			}
		}
	}

	/** 2^32. */
	private static final long POW_32 = 1L << 32;

	@Override
	public int nextInt(int n) {
		/* Lemire (2019): Fast Random Integer Generation in an Interval, https://arxiv.org/abs/1805.10941 */
		long m = (nextInt() & 0xffffffffL) * n;
		long l = m & 0xffffffffL;
		if (l < n) {
			// 2^32 % n
			final long t = POW_32 % n;
			while (l < t) {
				m = (nextInt() & 0xffffffffL) * n;
				l = m & 0xffffffffL;
			}
		}
		return (int) (m >>> 32);
	}

	@Override
	public boolean nextBoolean() {
		return nextInt() < 0;
	}

	@Override
	public float nextFloat() {
		return (nextInt() >>> 8) * 0x1.0p-24f;
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

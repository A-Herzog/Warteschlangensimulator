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
 * Kapselt einen Collatz-Weyl 128-Bit-Generator.<br>
 * <a href="https://arxiv.org/pdf/2312.17043">https://arxiv.org/pdf/2312.17043</a>
 */
public final class CWG128Random implements RandomGenerator {
	/**
	 * Internes Collatz-Weyl 128-Bit-Objekt
	 */
	private CWG128Core core;

	/**
	 * Seed-Generator, liefert 64-Bit-Werte
	 */
	private static final class SplitMix64 {
		/**
		 * Interner Zustand
		 */
		private long state;

		/**
		 * Konstruktor
		 * @param seed	Initialer Seed
		 */
		private SplitMix64(final long seed) {
			this.state=seed;
		}

		/**
		 * Nächster Teil-Seed-Value
		 * @return	64-bittiger Teil-Seed-Value
		 */
		private long nextLong() {
			long z = (state += 0x9e3779b97f4a7c15L);
			z = (z ^ (z >>> 30)) * 0xbf58476d1ce4e5b9L;
			z = (z ^ (z >>> 27)) * 0x94d049bb133111ebL;
			return z ^ (z >>> 31);
		}
	}

	/**
	 * Seed-Generator, liefert 63-Bit-Werte
	 */
	private static final class SplitMix63 {
		/**
		 * Interner Zustand
		 */
		private long state;

		/**
		 * Konstruktor
		 * @param seed	Initialer Seed
		 */
		private SplitMix63(final long seed) {
			this.state=seed;
		}

		/**
		 * Nächster Teil-Seed-Value
		 * @return	63-bittiger Teil-Seed-Value
		 */
		private long next63() {
			long z = (state += 0x9e3779b97f4a7c15L) & 0x7fff_ffff_ffff_ffffL;
			z = ((z ^ (z >>> 30)) * 0xbf58476d1ce4e5b9L) & 0x7fff_ffff_ffff_ffffL;
			z = ((z ^ (z >>> 27)) * 0x94d049bb133111ebL) & 0x7fff_ffff_ffff_ffffL;
			return z ^ (z >>> 31);
		}
	}

	/**
	 * Cache für temporäres Objekt in {@link #refill()}
	 * @see #refill()
	 */
	private final UInt128 block=new UInt128();

	/**
	 *  Anzahl noch nicht verbrauchter 32-Bit-Teile im aktuellen Block (Werte 3 bis 0)
	 *  @see #block
	 */
	private int remaining32=0;

	/**
	 * Index des nächsten Wertes im Puffer (Werte 0 bis 3)
	 * @see #block
	 */
	private int index32=0;

	/**
	 * Puffer für bis zu 128 Bit aus dem Zufallswerte entnommen werden
	 * @see #refill()
	 */
	private final int[] buf32 = new int[4];

	/**
	 * Füllt den 128-Bit-Puffer wieder auf.
	 * @see #block
	 */
	private void refill() {
		core.nextBlock(block);
		final long hi=block.hi;
		final long lo=block.lo;
		/* Zerlegen in 4x32 Bit (z.B. little endian) */
		buf32[0] = (int) (lo & 0xffff_ffffL);
		buf32[1] = (int) (lo >>> 32);
		buf32[2] = (int) (hi & 0xffff_ffffL);
		buf32[3] = (int) (hi >>> 32);
		remaining32=4;
		index32=0;
	}

	/**
	 * Konstruktor
	 */
	public CWG128Random() {
		setSeed(Thread.currentThread().getId()+System.currentTimeMillis());
	}

	@Override
	public int nextInt() {
		if (remaining32 == 0) {
			refill();
		}
		remaining32--;
		return buf32[index32++];
	}

	@Override
	public int nextInt(int n) {
		/* Default-Implementierung, ähnlich wie in Commons RNG: */
		if (n <= 0) {
			throw new IllegalArgumentException("n must be positive");
		}
		if ((n & -n) == n) { // Potenz von 2
			return (int) ((n * (long) (nextInt() >>> 1)) >> 31);
		}
		int bits, val;
		do {
			bits = nextInt() >>> 1;
			val = bits % n;
		} while (bits - val + (n - 1) < 0);
		return val;
	}

	@Override
	public long nextLong() {
		/* Zwei int-Werte zu einem long kombinieren */
		long hi = (nextInt() & 0xffff_ffffL);
		long lo = (nextInt() & 0xffff_ffffL);
		return (hi << 32) | lo;
	}

	@Override
	public boolean nextBoolean() {
		return (nextInt() & 1) != 0;
	}

	@Override
	public float nextFloat() {
		// 24 signifikante Bits
		return (nextInt() >>> 8) * 0x1.0p-24f;
	}

	@Override
	public double nextDouble() {
		// 53 signifikante Bits
		long l = (nextLong() >>> 11);
		return l * 0x1.0p-53;
	}

	@Override
	public void nextBytes(final byte[] bytes) {
		nextBytes(bytes, 0, bytes.length);
	}

	/**
	 * Generiert eine Reihe von zufälligen Bytes und trägt diese in ein bestehendes Array ein.
	 * @param bytes	Array in das die zufälligen Bytes eingetragen werden sollen
	 * @param start	Erste Array-Index, der verändert werden soll
	 * @param len	Anzahl an einzutragenden Bytes
	 */
	public void nextBytes(final byte[] bytes, final int start, final int len) {
		int index = start;
		int end = start + len;
		while (index < end) {
			int rnd = nextInt();
			for (int i = 0; i < 4 && index < end; i++) {
				bytes[index++] = (byte) rnd;
				rnd >>>= 8;
			}
		}
	}

	@Override
	public void setSeed(int seed) {
		setSeed((long)seed);
	}

	@Override
	public void setSeed(int[] seed) {
		if (seed==null) {
			seed=new int[2];
			for (int i=0;i<2;i++) if (seed[i]==0) seed[i]=(int)((System.nanoTime()+i) & 0xffffffffL);
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
		final SplitMix64 sm64 = new SplitMix64(seed);
		final SplitMix63 sm63 = new SplitMix63(~seed); /* invertierter Seed */

		final UInt128 c1 = new UInt128(0L, sm64.nextLong());

		final long hi0 = sm64.nextLong();
		final long lo0 = (sm63.next63() << 1) | 1L;  // garantiert ungerade
		final UInt128 c0 = new UInt128(hi0, lo0);

		// Für c2 und c3 verwenden wir weitere SplitMix64-Ausgaben
		final UInt128 c2 = new UInt128(sm64.nextLong(), sm64.nextLong());
		final UInt128 c3 = new UInt128(sm64.nextLong(), sm64.nextLong());

		this.core=new CWG128Core(c0,c1,c2,c3);
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

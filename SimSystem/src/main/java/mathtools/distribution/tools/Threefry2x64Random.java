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
 * Java-Implementierung des Treefry-Algorithmus in der 2x64-Variante.
 */
public class Threefry2x64Random implements RandomGenerator {
	/**
	 * Algorithmus-Konstante NW_THREEFRY
	 */
	private static final int NW_THREEFRY = 2;

	/**
	 * Algorithmus-Konstante C240
	 */
	private static final long C240 = 0x1BD11BDAA9FC1A22L;

	/**
	 * Algorithmus-Konstante N_ROUNDS_THREEFRY
	 */
	private static final int N_ROUNDS_THREEFRY = 20;

	/**
	 * Algorithmus-Konstante MASK
	 */
	private static final long MASK = 0xFFFFFFFFFFFFFFFFL;

	/**
	 * Algorithmus-Konstante R_THREEFRY
	 */
	private static final int[] R_THREEFRY = {16, 42, 12, 31, 16, 32, 24, 21};

	/**
	 * Schlüssel 0
	 */
	private long k0;

	/**
	 * Schlüssel 1
	 */
	private long k1;

	/**
	 * Schlüssel 2<br>
	 * (K[2] = C240 ^ K[0] ^ K[1])
	 */
	private long k2;

	/**
	 * Kombination von k0, k1 und k2
	 */
	private final long[] K=new long[3];

	/**
	 * Counter 0 (2 x 64 Bit) – Eingangswert für Threefry
	 */
	private long ctr0=0L;

	/**
	 * Counter 1 (2 x 64 Bit) – Eingangswert für Threefry
	 */
	private long ctr1=0L;

	/**
	 * Erster von {@link #generateBlock()} erzeugter Block.<br>
	 * (Ein Threefry-Aufruf liefert 128 Bit = 2 x 64.)
	 * @see #generateBlock()
	 * @see #nextLong()
	 * @see #usedSecond
	 */
	private long out0;

	/**
	 * Zweiter von {@link #generateBlock()} erzeugter Block.<br>
	 * (Ein Threefry-Aufruf liefert 128 Bit = 2 x 64.)
	 * @see #generateBlock()
	 * @see #nextLong()
	 * @see #usedSecond
	 */
	private long out1;

	/**
	 * Wurde der zweite Block {@link #out1} bereits verwendet?
	 * Wenn ja, müssen beim nächsten Aufruf von {@link #nextLong()}
	 * neue Blöcke generiert werden. Andernfalls wird beim nächsten
	 * Aufruf {@link #out1} ausgeliefert.
	 * @see #out0
	 * @see #out1
	 * @see #generateBlock()
	 * @see #nextLong()
	 */
	private boolean usedSecond=true;

	/**
	 * Konstruktor mit 2x64-Bit-Schlüssel.
	 * @param key0	Schlüssel 0 (als Teil des Seeds)
	 * @param key1	Schlüssel 1 (als Teil des Seeds)
	 */
	public Threefry2x64Random(final long key0, long key1) {
		setKey(key0,key1);
	}

	/**
	 * Einfacher Seed-Konstruktor (komprimiert einen long in 2 64-Bit-Werte).
	 * @param seed	Seed
	 */
	public Threefry2x64Random(final long seed) {
		/* einfache, aber deterministische Aufteilung */
		final long k0 = seed;
		final long k1 = seed * 0x9E3779B97F4A7C15L; /* Goldener Schnitt o.ä. */
		setKey(k0,k1);
	}

	/**
	 * Konstruktor mit automatischem Seed
	 */
	public Threefry2x64Random() {
		this(Thread.currentThread().getId()+System.currentTimeMillis());
	}

	/**
	 * Stellt einen neuen Schlüssel ein.
	 * @param key0	Schlüssel 0 (als Teil des Seeds)
	 * @param key1	Schlüssel 1 (als Teil des Seeds)
	 */
	private void setKey(final long key0, final long key1) {
		this.k0=key0 & MASK;
		this.k1=key1 & MASK;
		this.k2=C240 ^ this.k0 ^ this.k1;
		this.K[0]=this.k0;
		this.K[1]=this.k1;
		this.K[2]=this.k2;
		this.ctr0=0L;
		this.ctr1=0L;
		this.usedSecond=true;
	}

	/**
	 * Schlüsselvariation
	 * @param K	Schlüssel
	 * @param s	Wert s
	 * @param ksi	Ausgabe (Inhalt wird überschrieben)
	 */
	private void keySchedule(final long[] K, final int s, final long[] ksi) {
		/* K hat Länge 3: K[0], K[1], K[2] */
		int idx0 =  s      % (NW_THREEFRY + 1);
		int idx1 = (s + 1) % (NW_THREEFRY + 1);
		ksi[0] = K[idx0] & MASK;
		ksi[1] = (K[idx1] + s) & MASK;
	}

	/**
	 * Rotationsfunktion: Linksrotation einer 64-Bit-Zahl
	 * @param x	Zu rotierender Wert
	 * @param r	Rotation
	 * @return	Rotierter Wert
	 */
	private static long rotl(final long x, final int r) {
		return (x << r) | (x >>> (64 - r));
	}

	/**
	 * Mix-Schritt (entsprechend Threefry2x64-Spezifikation)
	 * @param x0	Eingangwert x0
	 * @param x1	Eingangswert x1
	 * @param r	Rotation
	 * @param mixed	Ausgabe (Inhalt wird überschrieben)
	 */
	private static void mix(long x0, long x1, final int r, final long[] mixed) {
		x0 = (x0 + x1) & MASK;
		x1 = rotl(x1, r) ^ x0;
		mixed[0]=x0;
		mixed[1]=x1;
	}

	/**
	 * Cache für Hilfsvariable in {@link #threefry(long, long)}
	 */
	private long[] ksi=new long[2];

	/**
	 * Cache für Hilfsvariable in {@link #threefry(long, long)}
	 */
	private long[] mixed=new long[2];

	/**
	 * Threefry(p, K)
	 * @param p0	Counter 0
	 * @param p1	Counter 1
	 */
	private void threefry(final long p0, final long p1) {
		long v0 = p0;
		long v1 = p1;

		for (int r = 0; r < N_ROUNDS_THREEFRY; r++) {
			long e0, e1;
			if (r % 4 == 0) {
				keySchedule(K, r / 4, ksi);
				e0 = (v0 + ksi[0]) & MASK;
				e1 = (v1 + ksi[1]) & MASK;
			} else {
				e0 = v0;
				e1 = v1;
			}

			int rot = R_THREEFRY[r % 8];
			mix(e0, e1, rot, mixed);
			v0 = mixed[0];
			v1 = mixed[1];
		}

		keySchedule(K, N_ROUNDS_THREEFRY / 4, ksi);
		out0 = (v0 + ksi[0]) & MASK;
		out1 = (v1 + ksi[1]) & MASK;
	}

	/**
	 * Erzeugt einen neuen 128-Bit-Block und legt ihn im Puffer ab.
	 */
	private void generateBlock() {
		threefry(ctr0,ctr1);

		/* 128-Bit-Zähler inkrementieren: (ctr0, ctr1)++ */
		ctr0++;
		if (ctr0==0L) {
			ctr1++;
		}
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

	@Override
	public void setSeed(final long seed) {
		/* Re-initialisiert Key und Zähler */
		long k0=seed;
		long k1=seed * 0xD1342543DE82EF95L; /* andere Konstante für Entkopplung */
		setKey(k0,k1);
	}

	@Override
	public void nextBytes(final byte[] bytes) {
		int i=0;
		final int len=bytes.length;
		while (i<len) {
			long x=nextLong();
			for (int j=0;j<8 && i<len; j++) {
				bytes[i++]=(byte)(x & 0xFF);
				x >>>= 8;
			}
		}
	}

	@Override
	public int nextInt() {
		return (int)(nextLong() >>> 32);
	}

	@Override
	public int nextInt(final int n) {
		if (n<=0) throw new IllegalArgumentException("n must be positive");

		/* Standard "rejection sampling" um Bias zu vermeiden */
		int bits, val;
		do {
			bits=nextInt() >>> 1;
			val=bits % n;
		} while (bits-val+(n-1)<0);

		return val;
	}

	@Override
	public long nextLong() {
		if (usedSecond) {
			generateBlock();
			usedSecond=false; /* out0 wird jetzt genutzt */
			return out0;
		} else {
			usedSecond=true; /* nächstes Mal neuen Block generieren */
			return out1;
		}
	}

	@Override
	public boolean nextBoolean() {
		return (nextLong() & 1L) != 0L;
	}

	@Override
	public float nextFloat() {
		/* 24 signifikante Bits */
		return (nextInt() >>> 8) * (1.0f / (1 << 24));
	}

	@Override
	public double nextDouble() {
		/* 53 signifikante Bits */
		return (nextLong() >>> 11) * (1.0 / (1L << 53));
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

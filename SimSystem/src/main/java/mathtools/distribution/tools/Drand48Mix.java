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

/**
 * Minimal, compatible implementation of C's drand48/srand48 and erand48 in Java.
 *
 * Uses the POSIX/standard 48-bit LCG:
 *    X_{n+1} = (a * X_n + c) mod 2^48
 * with a = 0x5DEECE66DL, c = 0xB.
 *
 * - srand48(seedval) sets the internal 48-bit state to ((seedval &lt;&lt; 16) + 0x330E) &amp; mask
 * - drand48() advances state and returns state / 2^48 as a double in [0,1)
 * - erand48(xsubi) advances the state stored in the provided 3-element short[] and returns the double
 *
 * Notes about Java types: C uses unsigned short for xsubi; Java shorts are signed,
 * so we mask with 0xFFFF when converting to/from the short[].
 */
public final class Drand48Mix implements Drand48Interface {
	/** LCG parameter (48-bit) MULTIPLIER */
	private static final long MULTIPLIER = 0x5DEECE66DL;
	/** LCG parameter (48-bit) ADDEND */
	private static final long ADDEND = 0xBL;
	/** LCG parameter (48-bit) MASK48 */
	private static final long MASK48 = (1L << 48) - 1;

	/** internal 48-bit state (only low 48 bits used) */
	private long state;

	/**
	 * Create and seed the generator with given seedval (like srand48).
	 * @param seedval seed value (any long; only low bits used similarly to C)
	 */
	public Drand48Mix(final long seedval) {
		srand48(seedval);
	}

	/**
	 * Create and seed the generator with given seedval (like srand48).
	 */
	public Drand48Mix() {
		srand48(0);
	}

	/**
	 * 128-bittige Mix-Funktion
	 * @param x	Eingangswert
	 * @return Ausgangswert nach Mix
	 * @see L32X64Mix#lea32(int)
	 */
	public static long mixLong(final long x) {
		int low = (int) (x & 0xFFFFFFFFL); /* untere 32 Bit */
		int high = (int) ((x >>> 32) & 0xFFFFFFFFL); /* obere 32 Bit */

		/* Schritt 2: Mix-Funktion auf beide Teile anwenden */
		int mixedLow = L32X64Mix.lea32(low);
		int mixedHigh = L32X64Mix.lea32(high);

		/* Schritt 3: Wieder zusammenführen zu einem long */
		/* Beachte: mixedHigh ist jetzt 32 Bit, aber als int (signed).
		 * Wir müssen sicherstellen, dass es als unsigned 32-Bit interpretiert wird. */
		return ((long) mixedHigh << 32) | (mixedLow & 0xFFFFFFFFL);
	}

	/**
	 * Seed the generator (like C's srand48).
	 * state := ((seedval &lt;&lt; 16) + 0x330E) &amp; ((1&lt;&lt;48)-1)
	 * @param seedval seed value
	 */
	@Override
	public void srand48(final long seedval) {
		state = ((seedval << 16) + 0x330EL) & MASK48;
	}

	/**
	 * Advance the internal state and return a double in [0.0, 1.0).
	 * Equivalent to C's drand48().
	 * @return uniformly distributed double in [0,1)
	 */
	@Override
	public double drand48() {
		state = (state * MULTIPLIER + ADDEND) & MASK48;
		return (state / (double) (1L << 48));
	}

	/**
	 * Return a 31-bit non-negative integer like C's lrand48 (optional helper).
	 * @return int in [0, 2^31)
	 */
	@Override
	public int lrand48() {
		// advance state and return high-order 31 bits
		state = (state * MULTIPLIER + ADDEND) & MASK48;
		return (int) (mixLong(state) >>> (48 - 31));
	}

	/**
	 * erand48: updates the provided seed array in-place and returns a double in [0,1).
	 * The input array must have at least 3 elements. Each element represents an
	 * unsigned 16-bit word; Java's short is signed, so mask with 0xFFFF when reading.
	 *
	 * Array layout:
	 *   xsubi[0] = low 16 bits
	 *   xsubi[1] = middle 16 bits
	 *   xsubi[2] = high 16 bits (most significant)
	 *
	 * This mirrors the C POSIX definition.
	 *
	 * @param xsubi short[3] (modified in-place)
	 * @return double in [0,1)
	 * @throws IllegalArgumentException if xsubi.length &lt; 3
	 */
	public static double erand48(final short[] xsubi) {
		if (xsubi == null || xsubi.length < 3) {
			throw new IllegalArgumentException("xsubi must be a short[3] (or longer)");
		}
		// assemble 48-bit state from 3 unsigned 16-bit words (xsubi[0] = least significant)
		long s0 = xsubi[0] & 0xFFFFL;
		long s1 = xsubi[1] & 0xFFFFL;
		long s2 = xsubi[2] & 0xFFFFL;
		long state = (s2 << 32) | (s1 << 16) | s0;
		// update
		state = (state * MULTIPLIER + ADDEND) & MASK48;
		// write back into xsubi (low..high)
		xsubi[0] = (short) (state & 0xFFFFL);
		xsubi[1] = (short) ((state >>> 16) & 0xFFFFL);
		xsubi[2] = (short) ((state >>> 32) & 0xFFFFL);
		return mixLong(state) / (double) (1L << 48);
	}

	/**
	 * Convenience overload that accepts int[] - each int must be in 0..65535.
	 * Useful because Java's short is signed.
	 * @param xsubi int[3] (modified in-place)
	 * @return double in [0,1)
	 * @throws IllegalArgumentException if xsubi.length &lt; 3
	 */
	public static double erand48FromInts(final int[] xsubi) {
		if (xsubi == null || xsubi.length < 3) {
			throw new IllegalArgumentException("xsubi must be an int[3] (or longer)");
		}
		long s0 = xsubi[0] & 0xFFFFL;
		long s1 = xsubi[1] & 0xFFFFL;
		long s2 = xsubi[2] & 0xFFFFL;
		long state = (s2 << 32) | (s1 << 16) | s0;
		state = (state * MULTIPLIER + ADDEND) & MASK48;
		xsubi[0] = (int) (state & 0xFFFFL);
		xsubi[1] = (int) ((state >>> 16) & 0xFFFFL);
		xsubi[2] = (int) ((state >>> 32) & 0xFFFFL);
		return mixLong(state) / (double) (1L << 48);
	}
}


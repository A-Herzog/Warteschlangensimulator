/**
 * Copyright 2025 Alexander Herzog
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

import java.io.Serializable;

/**
 * L32X64Mix Pseudo-Zufallszahlengenerator
 * @see <a href="https://doi.org/10.1145/3485525">Steele &amp; Vigna (2021) Proc. ACM Programming Languages 5, 1-31</a>
 */
public class L32X64Mix extends BitsStreamGenerator4States {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=8519835769970191222L;

	/** 32-bit LCG multiplier. Note: (M % 8) = 5. */
	private static final int M=0xadb4a92d;

	/**
	 * Konstruktor
	 */
	public L32X64Mix() {
		super();
	}

	/**
	 * Perform a 32-bit mixing function using Doug Lea's 32-bit mix constants and shifts.
	 * @param x the input value
	 * @return the output value
	 */
	private static int lea32(int x) {
		x = (x ^ (x >>> 16)) * 0xd36d884b;
		x = (x ^ (x >>> 16)) * 0xd36d884b;
		return x ^ (x >>> 16);
	}

	@Override
	public void setSeed(int[] seed) {
		super.setSeed(seed);
		state0=state0 | 1;
	}

	@Override
	protected int next(final int bits) {
		final int s0=state2;
		final int s=state1;

		/* Mix */
		final int z=lea32(s + s0);

		/* LCG update */
		state1=M*s+state0;

		/* XBG update */
		int s1=state3;

		s1 ^= s0;
		state2= Integer.rotateLeft(s0, 26) ^ s1 ^ (s1 << 9);
		state3= Integer.rotateLeft(s1, 13);

		return z >>> (32-bits);
	}
}

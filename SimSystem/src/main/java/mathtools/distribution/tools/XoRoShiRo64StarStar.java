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

import org.apache.commons.math3.random.BitsStreamGenerator;

/**
 * XoRoShiRo64** Pseudo-Zufallszahlengenerator
 * @see <a href="http://xoshiro.di.unimi.it/">xorshiro / xoroshiro generators</a>
 */
public class XoRoShiRo64StarStar extends BitsStreamGenerator {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-3889548551285552347L;

	/** State 0 of the generator. */
	protected int state0;
	/** State 1 of the generator. */
	protected int state1;

	/**
	 * Konstruktor
	 */
	public XoRoShiRo64StarStar() {
		setSeed(null);
	}

	@Override
	public void setSeed(final int seed) {
		setSeed(new int[] {seed});
	}

	@Override
	public void setSeed(int[] seed) {
		if (seed==null) {
			seed=new int[2];
			for (int i=0;i<2;i++) seed[i]=(int)((System.nanoTime()+i) & 0xffffffffL);
		}

		if (seed.length==1) {
			final int old=seed[0];
			seed=new int[] {old, old+1};
		}

		state0=seed[0];
		state1=seed[1];
	}

	@Override
	public void setSeed(final long seed) {
		final int[] seedArr=new int[2];
		seedArr[0]=(int)(seed >> 32);
		seedArr[1]=(int)(seed & 0xffffffffL);
		setSeed(seedArr);
	}

	@Override
	protected int next(final int bits) {
		final int result=Integer.rotateLeft(state0*0x9e3779bb,5)*5;

		final int s0=state0;
		int s1=state1;

		s1^=s0;
		state0=Integer.rotateLeft(s0,26) ^ s1 ^ (s1<<9);
		state1=Integer.rotateLeft(s1,13);

		return result >>> (32-bits);
	}
}

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
 * XoRoShiRo128++ Pseudo-Zufallszahlengenerator
 * * @see <a href="http://xoshiro.di.unimi.it/xoroshiro64starstar.c">Original source code</a>
 * @see <a href="http://xoshiro.di.unimi.it/">xorshiro / xoroshiro generators</a>
 */
public class XoRoShiRo128StarStar extends BitsStreamGenerator4States {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-1181483572791004065L;

	/**
	 * Konstruktor
	 */
	public XoRoShiRo128StarStar() {
		super();
	}

	@Override
	protected int next(final int bits) {
		final int result=Integer.rotateLeft(state0*0x9e3779bb,5)*5;

		final int t=state1 << 9;

		state2 ^= state0;
		state3 ^= state1;
		state1 ^= state2;
		state0 ^= state3;
		state2 ^= t;
		state3=Integer.rotateLeft(state3,11);

		return result >>> (32-bits);
	}
}

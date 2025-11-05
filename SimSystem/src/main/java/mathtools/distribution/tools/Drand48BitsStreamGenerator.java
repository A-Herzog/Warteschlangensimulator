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
 * Kapselt ein {@link Drand48}-Objekt in einen {@link BitsStreamGenerator}.
 * @see Drand48
 */
public class Drand48BitsStreamGenerator extends BitsStreamGenerator {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=2532529764908724608L;

	/**
	 * Internes Generatorobjekt
	 */
	private Drand48 generator;

	/**
	 * Konstruktor
	 */
	public Drand48BitsStreamGenerator() {
		setSeed(System.currentTimeMillis());
	}

	@Override
	public void setSeed(int seed) {
		setSeed((long)seed);
	}

	@Override
	public void setSeed(int[] seed) {
		if (seed==null || seed.length==0) {
			setSeed(System.currentTimeMillis());
		} else {
			setSeed(seed[0]);
		}
	}

	@Override
	public void setSeed(long seed) {
		generator=new Drand48(seed);
	}

	@Override
	protected int next(int bits) {
		final int z=generator.lrand48();
		return z >>> (32-bits);
	}

	@Override
	public double nextDouble() {
		return generator.drand48();
	}
}

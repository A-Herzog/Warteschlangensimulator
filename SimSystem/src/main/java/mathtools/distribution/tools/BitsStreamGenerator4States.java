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
import java.util.Arrays;

import org.apache.commons.math3.random.BitsStreamGenerator;

/**
 * Abstrakte Basisklasse für die Pseudo-Zufallszahlengeneratoren mit 4 internen int-Zustandswerten.
 */
public abstract class BitsStreamGenerator4States extends BitsStreamGenerator {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-8222963407838763855L;

	/** State 0 of the generator. */
	protected int state0;
	/** State 1 of the generator. */
	protected int state1;
	/** State 2 of the generator. */
	protected int state2;
	/** State 3 of the generator. */
	protected int state3;

	/**
	 * Konstruktor
	 */
	public BitsStreamGenerator4States() {
		setSeed(null);
	}

	@Override
	public void setSeed(final int seed) {
		setSeed(new int[] {seed});
	}

	@Override
	public void setSeed(int[] seed) {
		if (seed==null) {
			seed=new int[4];
			for (int i=0;i<4;i++) if (seed[i]==0) seed[i]=(int)((System.nanoTime()+i) & 0xffffffffL);
		}
		if (seed.length<4) {
			seed=Arrays.copyOf(seed,4);
			for (int i=0;i<4;i++) if (seed[i]==0) seed[i]=(i==0)?1:(seed[i-1]+1);
		}

		state0=seed[0];
		state1=seed[1];
		state2=seed[2];
		state3=seed[3];
	}

	@Override
	public void setSeed(final long seed) {
		final int[] seedArr=new int[2];
		seedArr[0]=(int)(seed >> 32);
		seedArr[1]=(int)(seed & 0xffffffffL);
		setSeed(seedArr);
	}
}

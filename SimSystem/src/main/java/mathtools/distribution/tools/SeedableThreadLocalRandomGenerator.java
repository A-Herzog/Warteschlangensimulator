/**
 * Copyright 2020 Alexander Herzog
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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * Dies ist ein Zufallszahlengenerator, der von <code>DistributionRandomNumber</code>
 * verwendet werden kann.<br>
 * Das System ist Thread-Local, d.h. pro Thread wird ein eigener
 * Generator mit eigenem Seed verwendet. Eine Synchronisation ist daher nicht
 * nötig, aber da nicht <code>ThreadLocalRandom</code> verwendet werden kann,
 * ist das System trotzdem nicht sehr schnell. Der Vorteil dieser Implementierung
 * ist, dass ein Seed (pro Thread) gesetzt werden kann.
 * @see DistributionRandomNumber
 * @author Alexander Herzog
 */
public class SeedableThreadLocalRandomGenerator implements RandomGenerator {
	/**
	 * Vorabberechneter Wert 2*pi, um in {@link #nextGaussian()} Zeit zu sparen.
	 */
	private static final double TwoTimesPI=2*Math.PI;

	/**
	 * Zuordnung von Threads zu {@link RandomGenerator}-Objekten,
	 * damit jeder Thread seinen eigenen Generator verwendet.
	 * @see #getGenerator()
	 */
	private final Map<Thread,RandomGenerator> map;

	/**
	 * Konstruktor der Klasse
	 */
	public SeedableThreadLocalRandomGenerator() {
		map=new HashMap<>();
	}

	/**
	 * Liefert den Thread-abhängigen Pseudozufallszahlengenerator
	 * @return	Pseudozufallszahlengenerator für den aktuellen Thread
	 */
	private synchronized RandomGenerator getGenerator() {
		final Thread key=Thread.currentThread();
		RandomGenerator generator=map.get(key);
		if (generator==null) {
			generator=new JDKRandomGenerator();
			map.put(key,generator);
		}
		return generator;
	}

	@Override
	public void setSeed(int seed) {
		getGenerator().setSeed(seed);
	}

	@Override
	public void setSeed(int[] seed) {
		getGenerator().setSeed(seed);
	}

	@Override
	public void setSeed(long seed) {
		getGenerator().setSeed(seed);
	}

	@Override
	public void nextBytes(byte[] bytes) {
		getGenerator().nextBytes(bytes);
	}

	@Override
	public int nextInt() {
		return getGenerator().nextInt();
	}

	@Override
	public int nextInt(int n) {
		return getGenerator().nextInt(n);
	}

	@Override
	public long nextLong() {
		return getGenerator().nextLong();
	}

	@Override
	public boolean nextBoolean() {
		return getGenerator().nextBoolean();
	}

	@Override
	public float nextFloat() {
		return getGenerator().nextFloat();
	}

	@Override
	public double nextDouble() {
		return getGenerator().nextDouble();
	}

	@Override
	public double nextGaussian() {
		return Math.cos(TwoTimesPI*nextDouble())*StrictMath.sqrt(-2*Math.log(nextDouble()));
	}
}

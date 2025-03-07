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

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * Dies ist ein Zufallszahlengenerator, der von {@link DistributionRandomNumber}
 * verwendet werden kann.<br>
 * Das System ist Thread-Local, d.h. pro Thread wird ein eigener
 * Generator mit eigenem Seed verwendet. Eine Synchronisation ist daher nicht
 * nötig, aber da nicht {@link ThreadLocalRandom} verwendet werden kann,
 * ist das System trotzdem nicht sehr schnell. Der Vorteil dieser Implementierung
 * ist, dass ein Seed (pro Thread) gesetzt werden kann.
 * @see DistributionRandomNumber
 * @author Alexander Herzog
 */
public class SeedableThreadLocalRandomGenerator extends AbstractSeedableThreadLocalRandomGenerator {
	/**
	 * Callback zur Erzeugung eines {@link RandomGenerator}-Objektes für einen Thread
	 */
	private final Supplier<RandomGenerator> factory;

	/**
	 * Zuordnung von Threads zu {@link RandomGenerator}-Objekten,
	 * damit jeder Thread seinen eigenen Generator verwendet.
	 * @see #getGenerator()
	 */
	private final ThreadLocal<RandomGenerator> generators;

	/**
	 * Konstruktor der Klasse
	 * @param factory	Callback zur Erzeugung eines {@link RandomGenerator}-Objektes für einen Thread
	 */
	public SeedableThreadLocalRandomGenerator(final Supplier<RandomGenerator> factory) {
		generators=new ThreadLocal<>();
		this.factory=factory;
	}

	/**
	 * Konstruktor der Klasse
	 */
	public SeedableThreadLocalRandomGenerator() {
		this(()->new JDKRandomGenerator());
	}

	@Override
	protected synchronized RandomGenerator getGenerator() {
		RandomGenerator generator=generators.get();
		if (generator==null) {
			generator=factory.get();
			generators.set(generator);
		}
		return generator;
	}
}

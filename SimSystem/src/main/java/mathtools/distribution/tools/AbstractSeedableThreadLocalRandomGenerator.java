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

import org.apache.commons.math3.random.RandomGenerator;

/**
 * Dies ist die Basisklasse für Zufallszahlengeneratoren, die von {@link DistributionRandomNumber}
 * verwendet werden können.<br>
 * Das System ist Thread-Local, d.h. pro Thread wird ein eigener
 * Generator mit eigenem Seed verwendet. Im Gegensatz zu {@link ThreadLocalRandom}
 * kann ein individueller Seed (pro Thread) gesetzt werden kann. Außerdem kann
 * der zu verwendende Algorithmus gewählt werden.
 * @see DistributionRandomNumber
 * @author Alexander Herzog
 */
public abstract class AbstractSeedableThreadLocalRandomGenerator implements RandomGenerator {
	/**
	 * Vorabberechneter Wert 2*pi, um in {@link #nextGaussian()} Zeit zu sparen.
	 */
	private static final double TwoTimesPI=2*Math.PI;

	/**
	 * Liefert den Thread-abhängigen Pseudozufallszahlengenerator
	 * @return	Pseudozufallszahlengenerator für den aktuellen Thread
	 */
	protected abstract RandomGenerator getGenerator();

	/**
	 * Konstruktor der Klasse
	 */
	public AbstractSeedableThreadLocalRandomGenerator() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
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

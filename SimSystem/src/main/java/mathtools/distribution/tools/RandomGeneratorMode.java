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

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.commons.math3.random.ISAACRandom;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well1024a;
import org.apache.commons.math3.random.Well19937a;
import org.apache.commons.math3.random.Well19937c;
import org.apache.commons.math3.random.Well44497a;
import org.apache.commons.math3.random.Well44497b;
import org.apache.commons.math3.random.Well512a;

/**
 * Stellt ein, welcher Pseudo-Zufallszahlen-Generator in {@link DistributionRandomNumberThreadLocal}
 * zum Einsatz kommen soll.
 * @see DistributionRandomNumberThreadLocal
 */
public enum RandomGeneratorMode {
	/** {@link ThreadLocalRandom} verwenden */
	THREAD_LOCAL_RANDOM("ThreadLocalRandom",useSeed->useSeed?new JDKRandomGenerator():new LightweightThreadLocalRandomWrapper(ThreadLocalRandom.current())),
	/** Pro Thread gekapselte Version von {@link Random} verwenden */
	RANDOM("Random",useSeed->new JDKRandomGenerator()),
	/** Pro Thread gekapselte Version von {@link Well512a} verwenden */
	WELL512A("Well512a",useSeed->new Well512a()),
	/** Pro Thread gekapselte Version von {@link Well1024a} verwenden */
	WELL1024A("Well1024a",useSeed->new Well1024a()),
	/** Pro Thread gekapselte Version von {@link Well19937a} verwenden */
	WELL19937A("Well19937a",useSeed->new Well19937a()),
	/** Pro Thread gekapselte Version von {@link Well19937c} verwenden */
	WELL19937C("Well19937c",useSeed->new Well19937c()),
	/** Pro Thread gekapselte Version von {@link Well44497a} verwenden */
	WELL44497A("Well44497a",useSeed->new Well44497a()),
	/** Pro Thread gekapselte Version von {@link Well44497b} verwenden */
	WELL44497B("Well44497b",useSeed->new Well44497b()),
	/** Pro Thread gekapselte Version von {@link MersenneTwister} verwenden */
	MERSENNE_TWISTER("MersenneTwister",useSeed->new MersenneTwister()),
	/** Pro Thread gekapselte Version von {@link ISAACRandom} verwenden */
	ISAAC("ISAAC",useSeed->new ISAACRandom()),
	/** Pro Thread gekapselte Version von {@link XoRoShiRo128PlusPlus} verwenden */
	XOROSHIRO128PLUSPLUS("XoRoShiRo128++",useSeed->new XoRoShiRo128PlusPlus()),
	/** Pro Thread gekapselte Version von {@link XoRoShiRo128StarStar} verwenden */
	XOROSHIRO128STARSTAR("XoRoShiRo128**",useSeed->new XoRoShiRo128StarStar()),
	/** Pro Thread gekapselte Version von {@link XoRoShiRo64StarStar} verwenden */
	XOROSHIRO64STARSTAR("XoRoShiRo64**",useSeed->new XoRoShiRo64StarStar()),
	/** Pro Thread gekapselte Version von {@link L32X64Mix} verwenden */
	L32X64MIX("L32X64Mix",useSeed->new L32X64Mix());

	/**
	 * Standardmäßig zu verwendender Modus
	 */
	public static RandomGeneratorMode defaultRandomGeneratorMode=THREAD_LOCAL_RANDOM;

	/**
	 * Name des Zufallszahlengenerators (zum Speichern der Auswahl als Zeichenkette)
	 */
	public final String name;

	/**
	 * Callback zur Erzeugung eines Generator gemäß des Typs
	 */
	private final Function<Boolean,RandomGenerator> getterCallback;

	/**
	 * Konstruktor des Enum
	 * @param name	Name des Zufallszahlengenerators (zum Speichern der Auswahl als Zeichenkette)
	 * @param getterCallback	Callback zur Erzeugung eines Generator gemäß des Typs
	 */
	RandomGeneratorMode(final String name, final Function<Boolean,RandomGenerator> getterCallback) {
		this.name=name;
		this.getterCallback=getterCallback;
	}

	/**
	 * Liefert den Zufallszahlengenerator-Modus basierend auf einem Namen.
	 * @param name	Name zu dem der passende Zufallszahlengenerator-Modus geliefert werden soll
	 * @return	Zufallszahlengenerator-Modus (basierend auf dem Namen) oder {@link RandomGeneratorMode#defaultRandomGeneratorMode}, wenn kein passender Eintrag gefunden wurde
	 */
	public static RandomGeneratorMode fromName(final String name) {
		return Stream.of(values()).filter(randomGeneratorMode->randomGeneratorMode.name.equalsIgnoreCase(name)).findFirst().orElseGet(()->defaultRandomGeneratorMode);
	}

	/**
	 * Liefert den Zufallszahlengenerator-Modus basierend seinem Index in der Liste aller Modi.
	 * @param index	Index zu dem der Zufallszahlengenerator-Modus geliefert werden soll
	 * @return	Zufallszahlengenerator-Modus (basierend auf dem Index) oder {@link RandomGeneratorMode#defaultRandomGeneratorMode}, wenn der Index außerhalb des zulässigen Bereichs liegt
	 * @see #getIndex(RandomGeneratorMode)
	 */
	public static RandomGeneratorMode fromIndex(final int index) {
		if (index<0 || index>=values().length) return defaultRandomGeneratorMode;
		return values()[index];
	}

	/**
	 * Liefert eine Liste aller Zufallszahlengenerator-Modi.
	 * @return	Liste aller Zufallszahlengenerator-Modi
	 */
	public static String[] getAllNames() {
		return Stream.of(values()).map(randomMode->randomMode.name).toArray(String[]::new);
	}

	/**
	 * Liefert der Index eines Zufallszahlengenerator-Modus in der Liste aller Modi.
	 * @param randomGeneratorMode	Zufallszahlengenerator-Modus
	 * @return	Index des Zufallszahlengenerator-Modus in der Liste aller Modi
	 * @see #fromIndex(int)
	 */
	public static int getIndex(final RandomGeneratorMode randomGeneratorMode) {
		int index=0;
		int defaultIndex=0;
		for (var testRandomGeneratorMode: values()) {
			if (testRandomGeneratorMode==randomGeneratorMode) return index;
			if (testRandomGeneratorMode==defaultRandomGeneratorMode) defaultIndex=index;
			index++;
		}
		return defaultIndex;
	}

	/**
	 * Erzeugt einen Generator gemäß des Typs
	 * @param useSeed	Muss es möglich sein, den für den Generator einen Seed zu setzen
	 * @return	Neues Generator-Objekt
	 */
	public RandomGenerator getGenerator(final boolean useSeed) {
		return getterCallback.apply(useSeed);
	}

	/**
	 * Sorgt dafür, dass {@link ThreadLocalRandom} über ein {@link RandomGenerator}-Interface angesprochen werden kann.
	 */
	private static class LightweightThreadLocalRandomWrapper implements RandomGenerator {
		/**
		 * {@link ThreadLocalRandom}-Objekt
		 */
		private final ThreadLocalRandom random;

		/**
		 * Konstruktor
		 * @param random	{@link ThreadLocalRandom}-Objekt
		 */
		private LightweightThreadLocalRandomWrapper(final ThreadLocalRandom random) {
			this.random=random;
		}

		@Override
		public void setSeed(int seed) {
			/* Nicht seedable */
		}

		@Override
		public void setSeed(int[] seed) {
			/* Nicht seedable */
		}

		@Override
		public void setSeed(long seed) {
			/* Nicht seedable */
		}

		@Override
		public void nextBytes(byte[] bytes) {
			random.nextBytes(bytes);
		}

		@Override
		public int nextInt() {
			return random.nextInt();
		}

		@Override
		public int nextInt(int n) {
			return random.nextInt(n);
		}

		@Override
		public long nextLong() {
			return random.nextLong();
		}

		@Override
		public boolean nextBoolean() {
			return random.nextBoolean();
		}

		@Override
		public float nextFloat() {
			return random.nextFloat();
		}

		@Override
		public double nextDouble() {
			return random.nextDouble();
		}

		@Override
		public double nextGaussian() {
			return random.nextGaussian();
		}
	}
}

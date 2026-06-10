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

import java.security.SecureRandom;
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
import org.apache.commons.rng.core.BaseProvider;
import org.apache.commons.rng.core.source64.PcgRxsMXs64;
import org.apache.commons.rng.core.source64.Philox4x64;
import org.apache.commons.rng.core.source64.XoRoShiRo1024PlusPlus;
import org.apache.commons.rng.core.source64.XoRoShiRo1024Star;
import org.apache.commons.rng.core.source64.XoRoShiRo1024StarStar;

/**
 * Stellt ein, welcher Pseudo-Zufallszahlen-Generator in {@link DistributionRandomNumberThreadLocal}
 * zum Einsatz kommen soll.
 * @see DistributionRandomNumberThreadLocal
 */
public enum RandomGeneratorMode {
	/** {@link ThreadLocalRandom} verwenden */
	THREAD_LOCAL_RANDOM("ThreadLocalRandom",useSeed->useSeed?new JDKRandomGenerator():new LightweightThreadLocalRandomWrapper(ThreadLocalRandom.current())),
	/** {@link ThreadLocalRandom} verwenden - dabei jedes Mal über die Thread-Map gehen */
	THREAD_LOCAL_RANDOM_SLOW("ThreadLocalRandomSlow",useSeed->useSeed?new JDKRandomGenerator():new LightweightSlowThreadLocalRandomWrapper(),true,true,true),
	/** Pro Thread gekapselte Version von {@link Random} verwenden */
	RANDOM("Random",useSeed->new JDKRandomGenerator()),
	/** Pro Thread gekapselte Version von {@link SecureRandom} verwenden */
	SECURE_RANDOM("SecureRandom",useSeed->new SecureRandomWrapper()),
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
	/** Pro Thread gekapselte Version von {@link SFC64} verwenden */
	SFC64("SFC64",useSeed->new SFC64()),
	/** Pro Thread gekapselte Version von {@link ISAACRandom} verwenden */
	ISAAC("ISAAC",useSeed->new ISAACRandom()),
	/** Pro Thread gekapselte Version von {@link XoRoShiRo128PlusPlus} verwenden */
	XOROSHIRO128PLUSPLUS("XoRoShiRo128++",useSeed->new XoRoShiRo128PlusPlus()),
	/** Pro Thread gekapselte Version von {@link XoRoShiRo128StarStar} verwenden */
	XOROSHIRO128STARSTAR("XoRoShiRo128**",useSeed->new XoRoShiRo128StarStar()),
	/** Pro Thread gekapselte Version von {@link XoRoShiRo64StarStar} verwenden */
	XOROSHIRO64STARSTAR("XoRoShiRo64**",useSeed->new XoRoShiRo64StarStar()),
	/** Pro Thread gekapselte Version von Xoshiro256PlusPlus verwenden (nur in Java 17 oder höher verfügbar) */
	XOROSHIRO256PLUSPLUS("XoRoShiRo256++",useSeed->RandomGeneratorsByReflection.getByName("Xoshiro256PlusPlus"),true,RandomGeneratorsByReflection.areJava17GeneratorsAvailable()),
	/** Pro Thread gekapselte Version von {@link XoRoShiRo1024PlusPlus} verwenden */
	XOROSHIRO1024PLUSPLUS("XoRoShiRo1024++",useDeed->new CommonsRNGWrapper(seed->new XoRoShiRo1024PlusPlus(new long[] {seed}))),
	/** Pro Thread gekapselte Version von {@link XoRoShiRo1024Star} verwenden */
	XOROSHIRO1024STAR("XoRoShiRo1024*",useDeed->new CommonsRNGWrapper(seed->new XoRoShiRo1024Star(new long[] {seed}))),
	/** Pro Thread gekapselte Version von {@link XoRoShiRo1024StarStar} verwenden */
	XOROSHIRO1024STARSTAR("XoRoShiRo1024**",useDeed->new CommonsRNGWrapper(seed->new XoRoShiRo1024StarStar(new long[] {seed}))),
	/** Pro Thread gekapselte Version von {@link L32X64Mix} verwenden */
	L32X64MIX("L32X64Mix",useSeed->new L32X64Mix()),
	/** Pro Thread gekapselte Version von L64X128MixRandom verwenden (nur in Java 17 oder höher verfügbar) */
	L64X128MIX("L64X128Mix",useSeed->RandomGeneratorsByReflection.getByName("L64X128MixRandom"),true,RandomGeneratorsByReflection.areJava17GeneratorsAvailable()),
	/** Pro Thread gekapselte Version von L64X128StarStarRandom verwenden (nur in Java 17 oder höher verfügbar) */
	L64X128STARSTAR("L64X128**",useSeed->RandomGeneratorsByReflection.getByName("L64X128StarStarRandom"),true,RandomGeneratorsByReflection.areJava17GeneratorsAvailable()),
	/** Pro Thread gekapselte Version von L64X256MixRandom verwenden (nur in Java 17 oder höher verfügbar) */
	L64X256MIX("L64X256Mix",useSeed->RandomGeneratorsByReflection.getByName("L64X256MixRandom"),true,RandomGeneratorsByReflection.areJava17GeneratorsAvailable()),
	/** Pro Thread gekapselte Version von L64X1024MixRandom verwenden (nur in Java 17 oder höher verfügbar) */
	L64X1024MIX("L64X1024Mix",useSeed->RandomGeneratorsByReflection.getByName("L64X1024MixRandom"),true,RandomGeneratorsByReflection.areJava17GeneratorsAvailable()),
	/** Pro Thread gekapselte Version von L128X128MixRandom verwenden (nur in Java 17 oder höher verfügbar) */
	L128X128MIX("L128X128Mix",useSeed->RandomGeneratorsByReflection.getByName("L128X128MixRandom"),true,RandomGeneratorsByReflection.areJava17GeneratorsAvailable()),
	/** Pro Thread gekapselte Version von L128X256MixRandom verwenden (nur in Java 17 oder höher verfügbar) */
	L128X256MIX("L128X256Mix",useSeed->RandomGeneratorsByReflection.getByName("L128X256MixRandom"),true,RandomGeneratorsByReflection.areJava17GeneratorsAvailable()),
	/** Pro Thread gekapselte Version von L128X1024MixRandom verwenden (nur in Java 17 oder höher verfügbar) */
	L128X1024MIX("L128X1024Mix",useSeed->RandomGeneratorsByReflection.getByName("L128X1024MixRandom"),true,RandomGeneratorsByReflection.areJava17GeneratorsAvailable()),
	/** Pro Thread gekapselte Version von {@link PcgRxsMXs64} verwenden */
	PCGRXSMXS64("PcgRxsMXs64",useSeed->new CommonsRNGWrapper(seed->new PcgRxsMXs64(seed))),
	/** Pro Thread gekapselte Version von {@link Philox4x64} verwenden */
	PHILOX4X64("Philox4x64",useSeed->new CommonsRNGWrapper(seed->new Philox4x64(new long[] {seed,seed+1,seed+2,2*seed,2*seed+1,2*seed+2}))),
	/** Pro Thread gekapselte Version von {@link CWG128Random} verwenden */
	CWG128("CWG128",useSeed->new CWG128Random()),
	/** Pro Thread gekapselte Version von {@link Threefry2x64Random} verwenden */
	THREEFRY2X64("ThreeFry2X64",useSeed->new Threefry2x64Random()),
	/** Pro Thread gekapselte Version von {@link Drand48BitsStreamGenerator} mit innerem {@link Drand48} verwenden */
	DRAND48("Drand48",useSeed->new Drand48BitsStreamGenerator(new Drand48()),false),
	/** Pro Thread gekapselte Version von {@link Drand48BitsStreamGenerator} mit innerem {@link Drand48Mix} verwenden */
	DRAND48MIX("Drand48Mix",useSeed->new Drand48BitsStreamGenerator(new Drand48Mix()),false);

	/**
	 * Standardmäßig zu verwendender Modus
	 */
	public static RandomGeneratorMode defaultRandomGeneratorMode=THREAD_LOCAL_RANDOM;

	/**
	 * Name des Zufallszahlengenerators (zum Speichern der Auswahl als Zeichenkette)
	 */
	public final String name;

	/**
	 * Ist der Generator für Simulationen geeignet?
	 */
	public final boolean isGoodForSimulation;

	/**
	 * Ist der Generator im aktuellen Umfeld (z.B. im verwendeten JDK) verfügbar?
	 */
	public final boolean isAvailable;

	/**
	 * Generator in der Liste anzeigen?
	 */
	public final boolean isHidden;

	/**
	 * Callback zur Erzeugung eines Generator gemäß des Typs
	 */
	private final Function<Boolean,RandomGenerator> getterCallback;

	/**
	 * Konstruktor des Enum
	 * @param name	Name des Zufallszahlengenerators (zum Speichern der Auswahl als Zeichenkette)
	 * @param getterCallback	Callback zur Erzeugung eines Generator gemäß des Typs
	 * @param isGoodForSimulation	Ist der Generator für Simulationen geeignet?
	 * @param isAvailable	Ist der Generator im aktuellen Umfeld (z.B. im verwendeten JDK) verfügbar?
	 * @param isHidden	Soll der Generator in der Auswahlliste angezeigt werden?
	 */
	RandomGeneratorMode(final String name, final Function<Boolean,RandomGenerator> getterCallback, final boolean isGoodForSimulation, final boolean isAvailable, final boolean isHidden) {
		this.name=name;
		this.getterCallback=getterCallback;
		this.isGoodForSimulation=isGoodForSimulation;
		this.isAvailable=isAvailable;
		this.isHidden=isHidden;
	}

	/**
	 * Konstruktor des Enum
	 * @param name	Name des Zufallszahlengenerators (zum Speichern der Auswahl als Zeichenkette)
	 * @param getterCallback	Callback zur Erzeugung eines Generator gemäß des Typs
	 * @param isGoodForSimulation	Ist der Generator für Simulationen geeignet?
	 * @param isAvailable	Ist der Generator im aktuellen Umfeld (z.B. im verwendeten JDK) verfügbar?
	 */
	RandomGeneratorMode(final String name, final Function<Boolean,RandomGenerator> getterCallback, final boolean isGoodForSimulation, final boolean isAvailable) {
		this(name,getterCallback,isGoodForSimulation,isAvailable,false);
	}

	/**
	 * Konstruktor des Enum
	 * @param name	Name des Zufallszahlengenerators (zum Speichern der Auswahl als Zeichenkette)
	 * @param getterCallback	Callback zur Erzeugung eines Generator gemäß des Typs
	 * @param isGoodForSimulation	Ist der Generator für Simulationen geeignet?
	 */
	RandomGeneratorMode(final String name, final Function<Boolean,RandomGenerator> getterCallback, final boolean isGoodForSimulation) {
		this(name,getterCallback,isGoodForSimulation,true,false);
	}

	/**
	 * Konstruktor des Enum
	 * @param name	Name des Zufallszahlengenerators (zum Speichern der Auswahl als Zeichenkette)
	 * @param getterCallback	Callback zur Erzeugung eines Generator gemäß des Typs
	 */
	RandomGeneratorMode(final String name, final Function<Boolean,RandomGenerator> getterCallback) {
		this(name,getterCallback,true,true,false);
	}

	/**
	 * Liefert einen Stream der tatsächlich verfügbaren Generatoren.
	 * @return	Stream der tatsächlich verfügbaren Generatoren
	 */
	public static Stream<RandomGeneratorMode> listStream() {
		return Stream.of(values()).filter(mode->mode.isAvailable && !mode.isHidden);
	}

	/**
	 * Liefert eine Liste der tatsächlich verfügbaren Generatoren.
	 * @return	Liste der tatsächlich verfügbaren Generatoren
	 */
	public static RandomGeneratorMode[] list() {
		return listStream().toArray(RandomGeneratorMode[]::new);
	}

	/**
	 * Liefert den Zufallszahlengenerator-Modus basierend auf einem Namen.
	 * @param name	Name zu dem der passende Zufallszahlengenerator-Modus geliefert werden soll
	 * @return	Zufallszahlengenerator-Modus (basierend auf dem Namen) oder {@link RandomGeneratorMode#defaultRandomGeneratorMode}, wenn kein passender Eintrag gefunden wurde
	 */
	public static RandomGeneratorMode fromName(final String name) {
		return Stream.of(values()).filter(randomGeneratorMode->randomGeneratorMode.name.equalsIgnoreCase(name) && randomGeneratorMode.isAvailable).findFirst().orElseGet(()->defaultRandomGeneratorMode);
	}

	/**
	 * Liefert den Zufallszahlengenerator-Modus basierend seinem Index in der Liste aller Modi.
	 * @param index	Index zu dem der Zufallszahlengenerator-Modus geliefert werden soll
	 * @return	Zufallszahlengenerator-Modus (basierend auf dem Index) oder {@link RandomGeneratorMode#defaultRandomGeneratorMode}, wenn der Index außerhalb des zulässigen Bereichs liegt
	 * @see #getIndex(RandomGeneratorMode)
	 * @see #list()
	 */
	public static RandomGeneratorMode fromIndex(final int index) {
		final var list=list();
		if (index<0 || index>=list.length) return defaultRandomGeneratorMode;
		return list[index];
	}

	/**
	 * Liefert eine Liste der Namen aller Zufallszahlengenerator-Modi.
	 * @return	Liste der Namen aller Zufallszahlengenerator-Modi
	 * @see #list()
	 */
	public static String[] getAllNames() {
		return listStream().map(randomMode->randomMode.name).toArray(String[]::new);
	}

	/**
	 * Liefert eine Liste der Qualität aller Zufallszahlengenerator-Modi.
	 * @return	Liste der Qualität aller Zufallszahlengenerator-Modi
	 * @see #list()
	 */
	public static Boolean[] getAllIsGoodForSimulation() {
		return listStream().map(randomMode->randomMode.isGoodForSimulation).toArray(Boolean[]::new);
	}

	/**
	 * Liefert der Index eines Zufallszahlengenerator-Modus in der Liste aller Modi.
	 * @param randomGeneratorMode	Zufallszahlengenerator-Modus
	 * @return	Index des Zufallszahlengenerator-Modus in der Liste aller Modi
	 * @see #fromIndex(int)
	 * @see #list()
	 */
	public static int getIndex(final RandomGeneratorMode randomGeneratorMode) {
		int index=0;
		int defaultIndex=0;
		for (var testRandomGeneratorMode: list()) {
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
		 * @param random	 {@link ThreadLocalRandom}-Objekt
		 */
		public LightweightThreadLocalRandomWrapper(final ThreadLocalRandom random) {
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

		/**
		 * Es werden immer zwei Pseudozufallszahlen gleichzeitig generiert.
		 * Steht eine zweite Zahl direkt zur Verfügung?
		 * @see #nextRandom
		 * @see #nextGaussian()
		 */
		private boolean randomAvailable=false;

		/**
		 * Es werden immer zwei Pseudozufallszahlen gleichzeitig generiert.
		 * Wenn eine zweite zur Verfügung steht, so wird sie hier angeboten.
		 * @see #randomAvailable
		 * @see #nextGaussian()
		 */
		private double nextRandom;

		@Override
		public double nextGaussian() {
			if (randomAvailable) {
				randomAvailable=false;
				return nextRandom;
			}

			double q=10, u=0, v=0;
			while (q==0 || q>=1) {
				u=2*nextDouble()-1;
				v=2*nextDouble()-1;
				q=u*u+v*v;
			}
			final double p=StrictMath.sqrt(-2 * StrictMath.log(q)/q);
			nextRandom=v*p;
			randomAvailable=true;
			return u*p;
		}
	}

	/**
	 * Sorgt dafür, dass {@link ThreadLocalRandom} über ein {@link RandomGenerator}-Interface angesprochen werden kann.<br>
	 * Bei jedem Aufruf wird dabei {@link ThreadLocalRandom}
	 */
	private static class LightweightSlowThreadLocalRandomWrapper implements RandomGenerator {
		/**
		 * Konstruktor
		 */
		private LightweightSlowThreadLocalRandomWrapper() {
			/* Keine Verarbeitung */
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
			ThreadLocalRandom.current().nextBytes(bytes);
		}

		@Override
		public int nextInt() {
			return ThreadLocalRandom.current().nextInt();
		}

		@Override
		public int nextInt(int n) {
			return ThreadLocalRandom.current().nextInt(n);
		}

		@Override
		public long nextLong() {
			return ThreadLocalRandom.current().nextLong();
		}

		@Override
		public boolean nextBoolean() {
			return ThreadLocalRandom.current().nextBoolean();
		}

		@Override
		public float nextFloat() {
			return ThreadLocalRandom.current().nextFloat();
		}

		@Override
		public double nextDouble() {
			return ThreadLocalRandom.current().nextDouble();
		}

		/**
		 * Es werden immer zwei Pseudozufallszahlen gleichzeitig generiert.
		 * Steht eine zweite Zahl direkt zur Verfügung?
		 * @see #nextRandom
		 * @see #nextGaussian()
		 */
		private boolean randomAvailable=false;

		/**
		 * Es werden immer zwei Pseudozufallszahlen gleichzeitig generiert.
		 * Wenn eine zweite zur Verfügung steht, so wird sie hier angeboten.
		 * @see #randomAvailable
		 * @see #nextGaussian()
		 */
		private double nextRandom;

		@Override
		public double nextGaussian() {
			if (randomAvailable) {
				randomAvailable=false;
				return nextRandom;
			}

			double q=10, u=0, v=0;
			while (q==0 || q>=1) {
				u=2*nextDouble()-1;
				v=2*nextDouble()-1;
				q=u*u+v*v;
			}
			final double p=StrictMath.sqrt(-2 * StrictMath.log(q)/q);
			nextRandom=v*p;
			randomAvailable=true;
			return u*p;
		}
	}

	/**
	 * Sorgt dafür, dass {@link SecureRandom} über ein {@link RandomGenerator}-Interface angesprochen werden kann.
	 */
	private static class SecureRandomWrapper implements RandomGenerator {
		/**
		 * Konstruktor der Klasse
		 */
		public SecureRandomWrapper() {
			/*
			 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
			 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		/**
		 * Internes {@link SecureRandom}-Objekt
		 */
		private final SecureRandom secureRandom=new SecureRandom();

		@Override
		public void setSeed(int seed) {
			secureRandom.setSeed(seed);
		}

		@Override
		public void setSeed(int[] seed) {
			if (seed==null || seed.length==0) return;
			final byte[] seedBytes=new byte[seed.length*4];
			for (int i=0;i<seed.length;i++) {
				final int value=seed[i];
				seedBytes[4*i+0]=(byte)(value >>> 24);
				seedBytes[4*i+1]=(byte)(value >>> 16);
				seedBytes[4*i+2]=(byte)(value >>> 8);
				seedBytes[4*i+3]=(byte)value;
			}
			secureRandom.setSeed(seedBytes);
		}

		@Override
		public void setSeed(long seed) {
			secureRandom.setSeed(seed);
		}

		@Override
		public void nextBytes(byte[] bytes) {
			secureRandom.nextBytes(bytes);
		}

		@Override
		public int nextInt() {
			return secureRandom.nextInt();
		}

		@Override
		public int nextInt(int n) {
			return secureRandom.nextInt(n);
		}

		@Override
		public long nextLong() {
			return secureRandom.nextLong();
		}

		@Override
		public boolean nextBoolean() {
			return secureRandom.nextBoolean();
		}

		@Override
		public float nextFloat() {
			return secureRandom.nextFloat();
		}

		@Override
		public double nextDouble() {
			return secureRandom.nextDouble();
		}

		@Override
		public double nextGaussian() {
			return secureRandom.nextGaussian();
		}
	}

	/**
	 * Sorgt dafür, dass {@link BaseProvider} über ein {@link RandomGenerator}-Interface angesprochen werden können.
	 */
	private static class CommonsRNGWrapper implements RandomGenerator {
		/**
		 * Generiert einen neuen {@link BaseProvider} mit vorgegebenem Seed
		 */
		private final Function<Long,BaseProvider> factory;

		/**
		 * Internes {@link BaseProvider}-Objekt
		 */
		private BaseProvider provider;

		/**
		 * Erzeugt einen neuen, zufälligen Seed-Wert.
		 * @return	Seed-Wert
		 */
		public static long getSeed() {
			return Thread.currentThread().getId()+System.currentTimeMillis();
		}

		/**
		 * Konstruktor
		 * @param factory	Generiert einen neuen {@link BaseProvider} mit vorgegebenem Seed
		 */
		public CommonsRNGWrapper(final Function<Long,BaseProvider> factory) {
			this.factory=factory;
			init(getSeed());
		}

		/**
		 * Initialisiert den Generator
		 * @param seed	Seed-Wert
		 */
		private void init(long seed) {
			provider=factory.apply(seed);
		}

		@Override
		public void setSeed(int seed) {
			init(seed);
		}

		@Override
		public void setSeed(int[] seed) {
			if (seed.length==0) {
				init(getSeed());
			} else {
				init(seed[0]);
			}
		}

		@Override
		public void setSeed(long seed) {
			init(seed);
		}

		@Override
		public void nextBytes(byte[] bytes) {
			provider.nextBytes(bytes);
		}

		@Override
		public int nextInt() {
			return provider.nextInt();
		}

		@Override
		public int nextInt(int n) {
			return provider.nextInt(n);
		}

		@Override
		public long nextLong() {
			return provider.nextLong();
		}

		@Override
		public boolean nextBoolean() {
			return provider.nextBoolean();
		}

		@Override
		public float nextFloat() {
			return provider.nextFloat();
		}

		@Override
		public double nextDouble() {
			return provider.nextDouble();
		}

		/**
		 * Es werden immer zwei Pseudozufallszahlen gleichzeitig generiert.
		 * Steht eine zweite Zahl direkt zur Verfügung?
		 * @see #nextRandom
		 * @see #nextGaussian()
		 */
		private boolean randomAvailable=false;

		/**
		 * Es werden immer zwei Pseudozufallszahlen gleichzeitig generiert.
		 * Wenn eine zweite zur Verfügung steht, so wird sie hier angeboten.
		 * @see #randomAvailable
		 * @see #nextGaussian()
		 */
		private double nextRandom;

		@Override
		public double nextGaussian() {
			if (randomAvailable) {
				randomAvailable=false;
				return nextRandom;
			}

			double q=10, u=0, v=0;
			while (q==0 || q>=1) {
				u=2*nextDouble()-1;
				v=2*nextDouble()-1;
				q=u*u+v*v;
			}
			final double p=StrictMath.sqrt(-2 * StrictMath.log(q)/q);
			nextRandom=v*p;
			randomAvailable=true;
			return u*p;
		}
	}
}

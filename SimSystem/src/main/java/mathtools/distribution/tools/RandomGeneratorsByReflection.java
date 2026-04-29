/**
 * Copyright 2026 Alexander Herzog
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.math3.random.RandomGenerator;

/**
 * Ermöglicht den Zugriff auf die Pseudo-Zufallszahlengeneratoren
 * aus Java 17 (indirekt per Reflection über "RandomGeneratorFactory")
 * @eee {@link #getByName(String)}
 */
public class RandomGeneratorsByReflection implements RandomGenerator {
	/**
	 * Factory-Klasse "RandomGeneratorFactory" (von Java 17)
	 */
	private final Object factory;

	/**
	 * Internes "RandomGenerator"-Objekt
	 * @see #create()
	 * @see #create(long)
	 * @see #create(byte[])
	 */
	private Object generator;

	/**
	 * Reflection-Zugriff auf die "nextBytes(byte[])"-Methode des Generators
	 * @see #generator
	 * @see #loadMethods()
	 */
	private Method methodNextBytes;

	/**
	 * Reflection-Zugriff auf die "nextInt()"-Methode des Generators
	 * @see #generator
	 * @see #loadMethods()
	 */
	private Method methodNextInt;

	/**
	 * Reflection-Zugriff auf die "nextInt(int)"-Methode des Generators
	 * @see #generator
	 * @see #loadMethods()
	 */
	private Method methodNextIntBound;

	/**
	 * Reflection-Zugriff auf die "nextBoolean()"-Methode des Generators
	 * @see #generator
	 * @see #loadMethods()
	 */
	private Method methodNextBoolean;

	/**
	 * Reflection-Zugriff auf die "nextLong()"-Methode des Generators
	 * @see #generator
	 * @see #loadMethods()
	 */
	private Method methodNextLong;

	/**
	 * Reflection-Zugriff auf die "nextFloat()"-Methode des Generators
	 * @see #generator
	 * @see #loadMethods()
	 */
	private Method methodNextFloat;

	/**
	 * Reflection-Zugriff auf die "nextDouble()"-Methode des Generators
	 * @see #generator
	 * @see #loadMethods()
	 */
	private Method methodNextDouble;

	/**
	 * Reflection-Zugriff auf die "nextGaussian()"-Methode des Generators
	 * @see #generator
	 * @see #loadMethods()
	 */
	private Method methodNextGaussian;

	/**
	 * Liste der verfügbaren Pseudo-Zufallszahlengeneratoren (über "RandomGeneratorFactory" in Java 17)
	 */
	public static final String[] generators={
			"L64X128MixRandom",
			"L64X128StarStarRandom",
			"L64X256MixRandom",
			"L64X1024MixRandom",
			"L128X128MixRandom",
			"L128X256MixRandom",
			"L128X1024MixRandom",
			"Xoshiro256PlusPlus"
	};

	/**
	 * Status der Verfügbarkeitsprüfung der Java-17-Generatoren
	 * (0=noch nicht durchgeführt, 1=ja, -1=nein)
	 * @see #areJava17GeneratorsAvailable()
	 */
	private static int availableStatus=0;

	/**
	 * Prüft, ob die Java-17-Generatoren per Reflection erreichbar sind.
	 * @return	Liefert <code>true</code>, wenn die Java-17-Generatoren per Reflection erreichbar sind
	 */
	public synchronized static boolean areJava17GeneratorsAvailable() {
		if (availableStatus==0) {
			if (getByName(generators[0])==null) availableStatus=-1; else availableStatus=1;
		}
		return availableStatus>0;
	}

	/**
	 * Konstruktor der Klasse<br>
	 * Kann nur indirekt über {@link #getByName(String)} aufgerufen werden.
	 * @param factory	Factory-Klasse "RandomGeneratorFactory" (von Java 17)
	 */
	private RandomGeneratorsByReflection(final Object factory) {
		this.factory=factory;
		create();
	}

	/**
	 * Erzeugt auf Basis des Factory-Objektes einen Generator und initialisiert diesen.
	 * @see #factory
	 * @see #generator
	 * @see #loadMethods()
	 */
	private void create() {
		try {
			final Method createMethod=factory.getClass().getMethod("create");
			generator=createMethod.invoke(factory);
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
		loadMethods();
	}

	/**
	 * Erzeugt auf Basis des Factory-Objektes einen Generator mit Startwert und initialisiert diesen.
	 * @param seed	Startwert
	 * @see #factory
	 * @see #generator
	 * @see #loadMethods()
	 */
	private void create(final long seed) {
		try {
			final Method createMethod=factory.getClass().getMethod("create",long.class);
			generator=createMethod.invoke(factory,seed);
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
		loadMethods();
	}

	/**
	 * Erzeugt auf Basis des Factory-Objektes einen Generator mit Startwert und initialisiert diesen.
	 * @param seed	Startwert
	 * @see #factory
	 * @see #generator
	 * @see #loadMethods()
	 */
	private void create(final byte[] seed) {
		try {
			final Method createMethod=factory.getClass().getMethod("create",byte[].class);
			generator=createMethod.invoke(factory,seed);
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
		loadMethods();
	}

	/**
	 * Initialisiert per Refelction die Liste der Methoden zum Aufruf
	 * der Zufallszahlengenerator-Funktionen.
	 */
	private void loadMethods() {
		try {
			methodNextBytes=generator.getClass().getMethod("nextBytes",byte[].class);
			methodNextInt=generator.getClass().getMethod("nextInt");
			methodNextIntBound=generator.getClass().getMethod("nextInt",int.class);
			methodNextBoolean=generator.getClass().getMethod("nextBoolean");
			methodNextLong=generator.getClass().getMethod("nextLong");
			methodNextFloat=generator.getClass().getMethod("nextFloat");
			methodNextDouble=generator.getClass().getMethod("nextDouble");
			methodNextGaussian=generator.getClass().getMethod("nextGaussian");
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Liefert eine Instanz dieser Klasse für einen bestimmten Generator.
	 * @param name	Name des Pseudo-Zufallszahlengenerators
	 * @return	Instanz oder <code>null</code>, wenn die Java-17-Generatoren nicht zur Verfügung stehen
	 * @see #generators
	 */
	public static RandomGeneratorsByReflection getByName(final String name) {
		try {
			final Class<?> factoryClass=Class.forName("java.util.random.RandomGeneratorFactory");
			final Method ofMethod=factoryClass.getMethod("of",String.class);
			return new RandomGeneratorsByReflection(ofMethod.invoke(null,name));
		} catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			return null;
		}
	}

	/*
		"L64X128MixRandom"
		"L64X128StarStarRandom"
		"L64X256MixRandom"
        "L64X1024MixRandom"
        "L128X128MixRandom"
        "L128X256MixRandom"
        "L128X1024MixRandom"
        "Xoshiro256PlusPlus"
	 */

	@Override
	public void setSeed(int seed) {
		create(seed);
	}

	@Override
	public void setSeed(int[] seed) {
		final byte[] byteSeed=new byte[seed.length*4];
		for (int i=0;i<seed.length;i++) {
			final int value=seed[i];
			byteSeed[i*4]=(byte)(value>>24);
			byteSeed[i*4+1]=(byte)(value>>16);
			byteSeed[i*4+2]=(byte)(value>>8);
			byteSeed[i*4+3]=(byte)value;
		}
		create(byteSeed);
	}

	@Override
	public void setSeed(long seed) {
		create(seed);
	}

	@Override
	public void nextBytes(byte[] bytes) {
		try {
			methodNextBytes.invoke(generator,bytes);
		} catch (IllegalAccessException|InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int nextInt() {
		try {
			return (int)methodNextInt.invoke(generator);
		} catch (IllegalAccessException|InvocationTargetException e) {
			e.printStackTrace();
			return 0;
		}
	}

	@Override
	public int nextInt(int n) {
		try {
			return (int)methodNextIntBound.invoke(generator,n);
		} catch (IllegalAccessException|InvocationTargetException e) {
			e.printStackTrace();
			return 0;
		}
	}

	@Override
	public long nextLong() {
		try {
			return (long)methodNextLong.invoke(generator);
		} catch (IllegalAccessException|InvocationTargetException e) {
			e.printStackTrace();
			return 0;
		}
	}

	@Override
	public boolean nextBoolean() {
		try {
			return (boolean)methodNextBoolean.invoke(generator);
		} catch (IllegalAccessException|InvocationTargetException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public float nextFloat() {
		try {
			return (float)methodNextFloat.invoke(generator);
		} catch (IllegalAccessException|InvocationTargetException e) {
			e.printStackTrace();
			return 0;
		}
	}

	@Override
	public double nextDouble() {
		try {
			return (double)methodNextDouble.invoke(generator);
		} catch (IllegalAccessException|InvocationTargetException e) {
			e.printStackTrace();
			return 0;
		}
	}

	@Override
	public double nextGaussian() {
		try {
			return (double)methodNextGaussian.invoke(generator);
		} catch (IllegalAccessException|InvocationTargetException e) {
			e.printStackTrace();
			return 0;
		}
	}
}

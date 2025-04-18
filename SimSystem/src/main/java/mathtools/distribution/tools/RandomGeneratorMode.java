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
import java.util.stream.Stream;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.Well19937c;

/**
 * Stellt ein, welcher Pseudo-Zufallszahlen-Generator in {@link DistributionRandomNumberThreadLocal}
 * zum Einsatz kommen soll.
 * @see DistributionRandomNumberThreadLocal
 */
public enum RandomGeneratorMode {
	/** {@link ThreadLocalRandom} verwenden */
	THREAD_LOCAL_RANDOM("ThreadLocalRandom"),
	/** Pro Thread gekapselte Version von {@link Random} verwenden */
	RANDOM("Random"),
	/** Pro Thread gekapselte Version von {@link Well19937c} verwenden */
	WELL19937C("Well19937c"),
	/** Pro Thread gekapselte Version von {@link MersenneTwister} verwenden */
	MERSENNE_TWISTER("MersenneTwister");

	/**
	 * Standardm‰ﬂig zu verwendender Modus
	 */
	public static RandomGeneratorMode defaultRandomGeneratorMode=THREAD_LOCAL_RANDOM;

	/**
	 * Name des Zufallszahlengenerators (zum Speichern der Auswahl als Zeichenkette)
	 */
	public final String name;

	/**
	 * Konstruktor des Enum
	 * @param name	Name des Zufallszahlengenerators (zum Speichern der Auswahl als Zeichenkette)
	 */
	RandomGeneratorMode(final String name) {
		this.name=name;
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
	 * @return	Zufallszahlengenerator-Modus (basierend auf dem Index) oder {@link RandomGeneratorMode#defaultRandomGeneratorMode}, wenn der Index auﬂerhalb des zul‰ssigen Bereichs liegt
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
}

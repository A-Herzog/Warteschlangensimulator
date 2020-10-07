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
package mathtoolstests.distributiontests;

import java.util.Arrays;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * Der Dummy-Zufallszahlengenerator wird nur für Unittests verwendet.
 * Er liefert zuvor einstellbare Zahlenwerte, um so die Bestimmung
 * von Zufallszahlen gemäß bestimmten Verteilungen testen zu können.
 * @author Alexander Herzog
 * @see DataDistributionImplTest
 * @see DistributionTests
 */
public class DummyRandomGenerator implements RandomGenerator {
	/**
	 * Index der als nächstes zu liefernden Zufallszahl in {@link #next}
	 * @see #next
	 */
	public int index;

	/**
	 * Folge der zu verwendenden Zufallszahlen
	 */
	public double[] next;

	/**
	 * Standardnormalverteilung
	 * @see #nextGaussian()
	 */
	private final NormalDistribution stdNormal=new NormalDistribution(0,1);

	/**
	 * Konstruktor der Klasse
	 * @param next	Zufallszahl, die immer wieder geliefert werden soll
	 */
	public DummyRandomGenerator(final double next) {this(new double[]{next});}

	/**
	 * Konstruktor der Klasse
	 * @param next	Folge von Zufallszahlen, die fortwährend wiederholt geliefert werden sollen
	 */
	public DummyRandomGenerator(final double[] next) {index=0; if (next==null || next.length==0) this.next=new double[]{0.0}; else this.next=Arrays.copyOf(next,next.length);}

	@Override public void setSeed(long seed) {}
	@Override public void setSeed(int[] seed) {}
	@Override public void setSeed(int seed) {}
	@Override public long nextLong() {return nextInt();}
	@Override public int nextInt(int n) {return (int)Math.round(nextDouble()*2*n-n);}
	@Override public int nextInt() {return nextInt(1_000_000);}
	@Override public double nextGaussian() {return stdNormal.inverseCumulativeProbability(nextDouble());}
	@Override public float nextFloat() {return (float)nextDouble();}
	@Override public double nextDouble() {if (index>=next.length) index=0; return next[index++];}
	@Override public void nextBytes(byte[] bytes) {}
	@Override public boolean nextBoolean() {return nextDouble()<0.5;}
}
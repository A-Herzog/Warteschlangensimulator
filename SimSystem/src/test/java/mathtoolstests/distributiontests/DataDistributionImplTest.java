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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import mathtools.NumberTools;
import mathtools.distribution.DataDistributionImpl;

/**
 * Prüft die Funktionsweise von {@link DataDistributionImpl}
 * @author Alexander Herzog
 * @see DataDistributionImpl
 */
class DataDistributionImplTest {
	private final static double[] exampleDataDouble=new double[] {3,4,2,0,1};
	private final static int[] exampleDataInt=new int[] {3,4,2,0,1};
	private final static double[] exampleDataDouble2=new double[] {3,4,2,0,1.5};

	/**
	 * Einstellung der Sprache für {@link NumberTools} um
	 * unabhängig vom System immer vergleichbare Ergebnisse
	 * zu erhalten.
	 */
	@BeforeAll
	static void init() {
		NumberTools.setLocale(Locale.GERMANY);
	}

	private boolean arraysApproxEqual(final double[] d1, final double[] d2) {
		if (d1==null && d2==null) return true;
		if (d1==null || d2==null) return false;
		if (d1.length!=d2.length) return false;
		for (int i=0;i<d1.length;i++) if (Math.abs(d1[i]-d2[i])>1E-10) return false;
		return true;
	}

	/**
	 * Test: Verschiedene Konstruktor-Varianten
	 */
	@Test
	void testConstructor() {
		DataDistributionImpl dist;

		/* Nur Anzahl an Steps */
		dist=new DataDistributionImpl(10.0,exampleDataDouble.length);
		assertEquals(10,dist.upperBound);
		assertEquals(exampleDataDouble.length,dist.densityData.length);
		assertNotSame(exampleDataDouble,dist.densityData);

		/* double[] */
		dist=new DataDistributionImpl(10.0,exampleDataDouble);
		assertEquals(10,dist.upperBound);
		assertEquals(exampleDataDouble.length,dist.densityData.length);
		assertTrue(Objects.deepEquals(exampleDataDouble,dist.densityData));
		assertNotSame(exampleDataDouble,dist.densityData);

		dist=new DataDistributionImpl(10.0,exampleDataDouble,false);
		assertEquals(10,dist.upperBound);
		assertEquals(exampleDataDouble.length,dist.densityData.length);
		assertTrue(Objects.deepEquals(exampleDataDouble,dist.densityData));
		assertNotSame(exampleDataDouble,dist.densityData);

		dist=new DataDistributionImpl(10.0,exampleDataDouble,true);
		assertEquals(10,dist.upperBound);
		assertEquals(exampleDataDouble.length,dist.densityData.length);
		assertTrue(Objects.deepEquals(exampleDataDouble,dist.densityData));
		assertSame(exampleDataDouble,dist.densityData);

		/*  int[] */
		dist=new DataDistributionImpl(10.0,exampleDataInt);
		assertEquals(10,dist.upperBound);
		assertEquals(exampleDataDouble.length,dist.densityData.length);
		assertTrue(Objects.deepEquals(exampleDataDouble,dist.densityData));

		/* Double[] */
		dist=new DataDistributionImpl(10.0,DoubleStream.of(exampleDataDouble).mapToObj(Double::valueOf).toArray(Double[]::new));
		assertEquals(10,dist.upperBound);
		assertEquals(exampleDataDouble.length,dist.densityData.length);
		assertTrue(Objects.deepEquals(exampleDataDouble,dist.densityData));

		/* Integer[] */
		dist=new DataDistributionImpl(10.0,IntStream.of(exampleDataInt).mapToObj(Integer::valueOf).toArray(Integer[]::new));
		assertEquals(10,dist.upperBound);
		assertEquals(exampleDataDouble.length,dist.densityData.length);
		assertTrue(Objects.deepEquals(exampleDataDouble,dist.densityData));

		/* List<Double> */
		dist=new DataDistributionImpl(10.0,DoubleStream.of(exampleDataDouble).mapToObj(Double::valueOf).collect(Collectors.toList()));
		assertEquals(10,dist.upperBound);
		assertEquals(exampleDataDouble.length,dist.densityData.length);
		assertTrue(Objects.deepEquals(exampleDataDouble,dist.densityData));
	}

	private void testFactor(final DataDistributionImpl dist, final double upperBound, final int steps) {
		try {
			final Field field=dist.getClass().getDeclaredField("argumentScaleFactor");
			field.setAccessible(true);
			assertEquals(steps/upperBound,(Double)(field.get(dist)),0.0000001);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			fail();
		}
	}

	/**
	 * Test: Skalierung der Array-Werte auf den Ziel-Wertebereich
	 */
	@Test
	void testAgrumentScaleFactor() {
		DataDistributionImpl dist;

		dist=new DataDistributionImpl(12345,1000);
		testFactor(dist,12345,1000);
		dist=new DataDistributionImpl(86400,1000);
		testFactor(dist,86400,1000);
		dist=new DataDistributionImpl(86399,1000);
		testFactor(dist,86400,1000); /* Nur bei 86399 wird intern zu 86400 gerundet. */

		dist=new DataDistributionImpl(12345,new double[1000]);
		testFactor(dist,12345,1000);
		dist=new DataDistributionImpl(86400,new double[1000]);
		testFactor(dist,86400,1000);
		dist=new DataDistributionImpl(86399,new double[1000]);
		testFactor(dist,86400,1000); /* Nur bei 86399 wird intern zu 86400 gerundet. */

		dist=new DataDistributionImpl(12345,new double[1000],true);
		testFactor(dist,12345,1000);
		dist=new DataDistributionImpl(86400,new double[1000],true);
		testFactor(dist,86400,1000);
		dist=new DataDistributionImpl(86399,new double[1000],true);
		testFactor(dist,86400,1000); /* Nur bei 86399 wird intern zu 86400 gerundet. */

		dist=new DataDistributionImpl(12345,new double[1000],false);
		testFactor(dist,12345,1000);
		dist=new DataDistributionImpl(86400,new double[1000],false);
		testFactor(dist,86400,1000);
		dist=new DataDistributionImpl(86399,new double[1000],false);
		testFactor(dist,86400,1000); /* Nur bei 86399 wird intern zu 86400 gerundet. */

		dist=new DataDistributionImpl(12345,new int[1000]);
		testFactor(dist,12345,1000);
		dist=new DataDistributionImpl(86400,new int[1000]);
		testFactor(dist,86400,1000);
		dist=new DataDistributionImpl(86399,new int[1000]);
		testFactor(dist,86400,1000); /* Nur bei 86399 wird intern zu 86400 gerundet. */

		final Integer[] intArray=new Integer[1000];
		for (int i=0;i<intArray.length;i++) intArray[i]=123;

		dist=new DataDistributionImpl(12345,intArray);
		testFactor(dist,12345,1000);
		dist=new DataDistributionImpl(86400,intArray);
		testFactor(dist,86400,1000);
		dist=new DataDistributionImpl(86399,intArray);
		testFactor(dist,86400,1000); /* Nur bei 86399 wird intern zu 86400 gerundet. */

		final Double[] doubleArray=new Double[1000];
		for (int i=0;i<doubleArray.length;i++) doubleArray[i]=123.0;

		dist=new DataDistributionImpl(12345,doubleArray);
		testFactor(dist,12345,1000);
		dist=new DataDistributionImpl(86400,doubleArray);
		testFactor(dist,86400,1000);
		dist=new DataDistributionImpl(86399,doubleArray);
		testFactor(dist,86400,1000); /* Nur bei 86399 wird intern zu 86400 gerundet. */
	}

	/**
	 * Test: Berechnung der empirischen Verteilungsfunktion aus der empirischen Dichte
	 */
	@Test
	void testCumulativeDensityArray() {
		DataDistributionImpl dist;

		/* Keine Daten */
		dist=new DataDistributionImpl(10.0,exampleDataDouble.length);
		assertEquals(10,dist.upperBound);
		assertEquals(null,dist.cumulativeDensity);
		dist.updateCumulativeDensity();
		assertNotNull(dist.cumulativeDensity);
		assertEquals(5,dist.cumulativeDensity.length);

		/* Daten vorbereiten */
		final double sum=DoubleStream.of(exampleDataDouble).sum();
		final double[] exampleDataDoubleNormalized=DoubleStream.of(exampleDataDouble).map(d->d/sum).toArray();
		final double[] exampleCumulativeDataDouble=new double[exampleDataDoubleNormalized.length];
		for (int i=0;i<exampleCumulativeDataDouble.length;i++) {
			exampleCumulativeDataDouble[i]=exampleDataDoubleNormalized[i];
			if (i>0) exampleCumulativeDataDouble[i]+=exampleCumulativeDataDouble[i-1];
		}

		/* double[] */
		dist=new DataDistributionImpl(10.0,exampleDataDouble);
		assertEquals(10,dist.upperBound);
		assertEquals(exampleDataDouble.length,dist.densityData.length);
		assertTrue(arraysApproxEqual(exampleCumulativeDataDouble,dist.cumulativeDensity));
	}

	/**
	 * Test: Lesen und schreiben von Werten der Dichte
	 */
	@Test
	void testDensityArray() {
		DataDistributionImpl dist;

		/* Keine Daten */
		dist=new DataDistributionImpl(10.0,exampleDataDouble.length);
		assertTrue(dist.sumIsZero());

		dist=new DataDistributionImpl(10,new double[0]);
		assertEquals(0,dist.density(5));
		assertEquals(0,dist.cumulativeProbability(5));

		/* Daten vorbereiten */
		final double sum=DoubleStream.of(exampleDataDouble).sum();
		final double[] exampleDataDoubleNormalized=DoubleStream.of(exampleDataDouble).map(d->d/sum).toArray();

		/* double[] */
		dist=new DataDistributionImpl(10.0,exampleDataDouble);
		assertTrue(!dist.sumIsZero());
		dist.normalizeDensity();
		assertTrue(arraysApproxEqual(dist.densityData,exampleDataDoubleNormalized));

		/* Werte setzen */
		dist.clearDensityData();
		assertTrue(dist.sumIsZero());

		dist.setToValue(5);
		assertEquals(25,dist.sum());
		assertEquals(25,dist.sumAsStoredAsString());

		/* Skalieren */
		dist=new DataDistributionImpl(10.0,exampleDataDouble);
		dist.stretchToValueCount(10);
		assertTrue(Objects.deepEquals(new double[]{3,3,4,4,2,2,0,0,1,1},dist.densityData));

		dist=new DataDistributionImpl(10.0,exampleDataDouble);
		dist.stretchToValueCount(3);
		assertTrue(Objects.deepEquals(new double[]{3,4,0},dist.densityData));
	}

	/**
	 * Test: Zusammenführen von verschiedenen empirischen Verteilungen
	 */
	@Test
	void testCombineDists() {
		DataDistributionImpl dist1, dist2, dist3;

		dist1=new DataDistributionImpl(10.0,exampleDataDouble);
		dist2=new DataDistributionImpl(10.0,5);

		/* min */
		dist3=dist1.min(2);
		assertTrue(Objects.deepEquals(dist3.densityData,DoubleStream.of(exampleDataDouble).map(d->Math.min(d,2)).toArray()));

		dist2.setToValue(2);
		dist3=dist1.min(dist2);
		assertTrue(Objects.deepEquals(dist3.densityData,DoubleStream.of(exampleDataDouble).map(d->Math.min(d,2)).toArray()));

		/* max */
		dist3=dist1.max(2);
		assertTrue(Objects.deepEquals(dist3.densityData,DoubleStream.of(exampleDataDouble).map(d->Math.max(d,2)).toArray()));

		dist2.setToValue(2);
		dist3=dist1.max(dist2);
		assertTrue(Objects.deepEquals(dist3.densityData,DoubleStream.of(exampleDataDouble).map(d->Math.max(d,2)).toArray()));

		/* add */
		dist3=dist1.add(0);
		assertTrue(Objects.deepEquals(dist3.densityData,dist1.densityData));

		dist3=dist1.add(5);
		assertTrue(Objects.deepEquals(dist3.densityData,DoubleStream.of(exampleDataDouble).map(d->d+5).toArray()));

		dist2.setToValue(5);
		dist3=dist1.add(dist2);
		assertTrue(Objects.deepEquals(dist3.densityData,DoubleStream.of(exampleDataDouble).map(d->d+5).toArray()));

		/* sub */
		dist3=dist1.sub(0);
		assertTrue(Objects.deepEquals(dist3.densityData,dist1.densityData));

		dist3=dist1.sub(5);
		assertTrue(Objects.deepEquals(dist3.densityData,DoubleStream.of(exampleDataDouble).map(d->d-5).toArray()));

		dist2.setToValue(5);
		dist3=dist1.sub(dist2);
		assertTrue(Objects.deepEquals(dist3.densityData,DoubleStream.of(exampleDataDouble).map(d->d-5).toArray()));

		/* multiply */
		dist3=dist1.multiply(5);
		assertTrue(Objects.deepEquals(dist3.densityData,DoubleStream.of(exampleDataDouble).map(d->d*5).toArray()));

		dist2.setToValue(5);
		dist3=dist1.multiply(dist2);
		assertTrue(Objects.deepEquals(dist3.densityData,DoubleStream.of(exampleDataDouble).map(d->d*5).toArray()));

		/* divide */
		dist3=dist1.divide(5);
		assertTrue(Objects.deepEquals(dist3.densityData,DoubleStream.of(exampleDataDouble).map(d->d/5.0).toArray()));

		dist2.setToValue(5);
		dist3=dist1.divide(dist2);
		assertTrue(Objects.deepEquals(dist3.densityData,DoubleStream.of(exampleDataDouble).map(d->d/5.0).toArray()));

		dist3=dist1.divide(0);
		assertTrue(Objects.deepEquals(dist3.densityData,dist1.densityData));

		dist3=dist1.divide(dist1);
		for (int i=0;i<exampleDataDouble.length;i++) assertEquals(dist3.densityData[i],((exampleDataDouble[i]==0.0)?0.0:1.0));

		/* Runden */
		dist3=dist1.divide(2).round();
		assertTrue(Objects.deepEquals(dist3.densityData,DoubleStream.of(exampleDataDouble).map(d->Math.round(d/2.0)).toArray()));

		dist3=dist1.divide(2).floor();
		assertTrue(Objects.deepEquals(dist3.densityData,DoubleStream.of(exampleDataDouble).map(d->Math.floor(d/2.0)).toArray()));

		dist3=dist1.divide(2).ceil();
		assertTrue(Objects.deepEquals(dist3.densityData,DoubleStream.of(exampleDataDouble).map(d->Math.ceil(d/2.0)).toArray()));
	}

	/**
	 * Test: Zusammenführen von empirischen Verteilungen verschiedener Größen (bezogen auf die Werte-Arrays)
	 */
	@Test
	void testCombineDistsDifferentSizes() {
		DataDistributionImpl dist1, dist2;

		dist1=new DataDistributionImpl(10.0,new double[] {1,2,3,4});
		dist2=new DataDistributionImpl(10.0,new double[] {2,4});
		assertArrayEquals(new double[] {3,4,7,8},dist1.add(dist2).densityData,0.0001);

		dist1=new DataDistributionImpl(10.0,new double[] {1,2,3,4});
		dist2=new DataDistributionImpl(10.0,new double[] {2,4});
		assertArrayEquals(new double[] {3,4,7,8},dist2.add(dist1).densityData,0.0001);

		dist1=new DataDistributionImpl(10.0,new double[] {1,2,3,4});
		dist2=new DataDistributionImpl(10.0,new double[] {2,4});
		dist1.addToThis(dist2);
		assertArrayEquals(new double[] {3,4,7,8},dist1.densityData,0.0001);

		dist1=new DataDistributionImpl(10.0,new double[] {1,2,3,4});
		dist2=new DataDistributionImpl(10.0,new double[] {2,4});
		dist2.addToThis(dist1);
		assertArrayEquals(new double[] {3,4,7,8},dist2.densityData,0.0001);
	}

	/**
	 * Test: Zugriff auf die Werte der Dichte und der Verteilungsfunktion
	 * @see DataDistributionImpl#density(double)
	 * @see DataDistributionImpl#getSupportLowerBound()
	 * @see DataDistributionImpl#getSupportUpperBound()
	 * @see DataDistributionImpl#cumulativeProbability(double)
	 * @see DataDistributionImpl#inverseCumulativeProbability(double)
	 * @see DataDistributionImpl#inverseCumulativeProbabilityWithOutThrowsAndChecks(double)
	 * @see DataDistributionImpl#isSupportConnected()
	 * @see DataDistributionImpl#isSupportLowerBoundInclusive()
	 * @see DataDistributionImpl#isSupportUpperBoundInclusive()
	 */
	@Test
	void testDensityFunctions() {
		DataDistributionImpl dist;

		/* Dichte */

		dist=new DataDistributionImpl(10.0,new double[]{1});
		assertEquals(0.0,dist.density(-0.1));
		assertEquals(1.0,dist.density(1));
		assertEquals(1.0,dist.density(10));
		assertEquals(0.0,dist.density(20));
		assertEquals(0.0,dist.getSupportLowerBound());
		assertEquals(10.0,dist.getSupportUpperBound());

		dist=new DataDistributionImpl(10.0,new double[]{1,1});
		assertEquals(0.0,dist.density(-0.1));
		assertEquals(1.0,dist.density(1));
		assertEquals(1.0,dist.density(10));
		assertEquals(0.0,dist.density(20));
		assertEquals(0.0,dist.getSupportLowerBound());
		assertEquals(10.0,dist.getSupportUpperBound());

		dist=new DataDistributionImpl(10.0,new double[]{1,1,1});
		assertEquals(0.0,dist.density(-0.1));
		assertEquals(1.0,dist.density(1));
		assertEquals(1.0,dist.density(10));
		assertEquals(0.0,dist.density(20));
		assertEquals(0.0,dist.getSupportLowerBound());
		assertEquals(10.0,dist.getSupportUpperBound());

		/* Verteilungsfunktion */

		dist=new DataDistributionImpl(10.0,new double[]{1});
		assertEquals(0.0,dist.cumulativeProbability(-0.1));
		assertEquals(0.0,dist.cumulativeProbability(0));
		assertEquals(0.5,dist.cumulativeProbability(5));
		assertEquals(1.0,dist.cumulativeProbability(10));
		assertEquals(1.0,dist.cumulativeProbability(15));

		/* Inverse Verteilungsfunktion */

		dist=new DataDistributionImpl(10.0,new double[]{1});
		assertEquals(0.0,dist.inverseCumulativeProbability(-1));
		assertEquals(0.0,dist.inverseCumulativeProbability(0));
		assertEquals(10.0,dist.inverseCumulativeProbability(1));
		assertEquals(10.0,dist.inverseCumulativeProbability(2));
		assertEquals(0.0,dist.inverseCumulativeProbabilityWithOutThrowsAndChecks(0));
		assertEquals(10.0,dist.inverseCumulativeProbabilityWithOutThrowsAndChecks(1));

		dist=new DataDistributionImpl(10.0,new double[]{1,2,1});
		assertEquals(0.0,dist.inverseCumulativeProbability(-1));
		assertEquals(0.0,dist.inverseCumulativeProbability(0));
		assertEquals(10.0,dist.inverseCumulativeProbability(1));
		assertEquals(10.0,dist.inverseCumulativeProbability(2));
		assertEquals(0.0,dist.inverseCumulativeProbabilityWithOutThrowsAndChecks(0));
		assertEquals(10.0,dist.inverseCumulativeProbabilityWithOutThrowsAndChecks(1));

		dist=new DataDistributionImpl(10.0,new double[]{1,2,3,4,3,2,1});
		assertEquals(0.0,dist.inverseCumulativeProbability(-1));
		assertEquals(0.0,dist.inverseCumulativeProbability(0));
		assertEquals(10.0,dist.inverseCumulativeProbability(1));
		assertEquals(10.0,dist.inverseCumulativeProbability(2));
		assertEquals(0.0,dist.inverseCumulativeProbabilityWithOutThrowsAndChecks(0));
		assertEquals(10.0,dist.inverseCumulativeProbabilityWithOutThrowsAndChecks(1));

		/* Standardantworten */

		dist=new DataDistributionImpl(10.0,exampleDataDouble);
		assertTrue(dist.isSupportLowerBoundInclusive());
		assertTrue(dist.isSupportUpperBoundInclusive());
		assertTrue(dist.isSupportConnected());
	}

	/**
	 * Test: Berechnung der Kenngrößen
	 * @see DataDistributionImpl#getMin()
	 * @see DataDistributionImpl#getMax()
	 * @see DataDistributionImpl#getNumericalMean()
	 * @see DataDistributionImpl#getStandardDeviation()
	 * @see DataDistributionImpl#getNumericalVariance()
	 */
	@Test
	void testPerformanceIndicatorValues() {
		DataDistributionImpl dist;

		final double min=DoubleStream.of(exampleDataDouble).min().getAsDouble();
		final double max=DoubleStream.of(exampleDataDouble).max().getAsDouble();
		final double sum=DoubleStream.of(exampleDataDouble).sum();
		double m=0, m2=0;
		for (int i=0;i<exampleDataDouble.length;i++) {
			double x=i*(10.0/exampleDataDouble.length);
			m+=exampleDataDouble[i]*x;
			m2+=exampleDataDouble[i]*x*x;
		}
		m/=sum;
		m2/=sum;

		/* Minimum */

		dist=new DataDistributionImpl(10.0,exampleDataDouble);
		assertEquals(min,dist.getMin());

		/* Maximum */

		dist=new DataDistributionImpl(10.0,exampleDataDouble);
		assertEquals(max,dist.getMax());

		/* Mittelwert */

		dist=new DataDistributionImpl(10.0,new double[0]);
		assertEquals(0.0,dist.getNumericalMean());
		assertEquals(0.0,dist.getMean());

		dist=new DataDistributionImpl(10.0,exampleDataDouble);
		assertEquals(m,dist.getNumericalMean());
		assertEquals(m,dist.getMean());

		/* Standardabweichung */

		dist=new DataDistributionImpl(10.0,new double[0]);
		assertEquals(0.0,dist.getStandardDeviation());

		dist=new DataDistributionImpl(10.0,exampleDataDouble);
		assertTrue(Math.abs(Math.sqrt(m2-m*m)-dist.getStandardDeviation())<1E-10);

		/* Varianz */

		dist=new DataDistributionImpl(10.0,exampleDataDouble);
		assertTrue(Math.abs((m2-m*m)-dist.getNumericalVariance())<1E-10);

		/* Median */

		assertEquals(2,dist.getMedian());

		/* Quantile */

		assertEquals(0,dist.getQuantil(0.2));
		assertEquals(2,dist.getQuantil(0.4));
		assertEquals(2,dist.getQuantil(0.6));
		assertEquals(4,dist.getQuantil(0.8));
	}

	/**
	 * Test: Werte als {@link String} speichern
	 */
	@Test
	void testStringProcessingStore() {
		DataDistributionImpl dist;

		dist=new DataDistributionImpl(10.0,new double[0]);
		assertEquals("",dist.getDensityString());

		dist=new DataDistributionImpl(10.0,exampleDataDouble2);
		assertEquals("3;4;2;0;1,5",dist.getDensityString());

		dist=new DataDistributionImpl(10.0,new double[0]);
		assertEquals("",dist.getDensityStringPercent());

		dist=new DataDistributionImpl(10.0,exampleDataDouble2);
		assertEquals("300%;400%;200%;0%;150%",dist.getDensityStringPercent());

		dist=new DataDistributionImpl(10.0,new double[0]);
		assertEquals("",dist.storeToString());

		dist=new DataDistributionImpl(10.0,exampleDataDouble2);
		assertEquals("3;4;2;0;1.5",dist.storeToString());

		dist=new DataDistributionImpl(10.0,new double[0]);
		assertEquals("",dist.storeToLocalString());

		dist=new DataDistributionImpl(10.0,exampleDataDouble2);
		assertEquals("3;4;2;0;1,5",dist.storeToLocalString());

		dist=new DataDistributionImpl(10.0,new double[0]);
		assertEquals("",dist.storeToString("#"));

		dist=new DataDistributionImpl(10.0,exampleDataDouble2);
		assertEquals("3#4#2#0#1.5",dist.storeToString("#"));

		dist=new DataDistributionImpl(10.0,new double[0]);
		assertEquals("",dist.storeToLocalString("#"));

		dist=new DataDistributionImpl(10.0,exampleDataDouble2);
		assertEquals("3#4#2#0#1,5",dist.storeToLocalString("#"));
	}

	/**
	 * Test: Werte aus {@link String} auslesen
	 */
	@Test
	void testStringProcessingLoad() {
		DataDistributionImpl dist;

		dist=DataDistributionImpl.createFromString(null,10.0);
		assertEquals(0,dist.densityData.length);

		dist=DataDistributionImpl.createFromString("",10.0);
		assertEquals(0,dist.densityData.length);

		dist=DataDistributionImpl.createFromString("3;4;2;0;1,5",10.0);
		assertTrue(Objects.deepEquals(exampleDataDouble2,dist.densityData));

		dist=DataDistributionImpl.createFromString("3;4;2;0;1.5",10.0);
		assertTrue(Objects.deepEquals(exampleDataDouble2,dist.densityData));

		dist=DataDistributionImpl.createFromString("3;4;2;0;150%",10.0);
		assertTrue(Objects.deepEquals(exampleDataDouble2,dist.densityData));

		dist=DataDistributionImpl.createFromArray(null,10.0,true);
		assertEquals(0,dist.densityData.length);

		dist=DataDistributionImpl.createFromArray(new String[]{"3","4","2","0","1,5"},10.0,true);
		assertTrue(Objects.deepEquals(exampleDataDouble2,dist.densityData));

		dist=DataDistributionImpl.createFromAnyString(null,10.0);
		assertEquals(0,dist.densityData.length);

		dist=DataDistributionImpl.createFromAnyString("",10.0);
		assertEquals(0,dist.densityData.length);

		dist=DataDistributionImpl.createFromAnyString("3\t4\n2;0;150%",10.0);
		assertTrue(Objects.deepEquals(exampleDataDouble2,dist.densityData));
	}

	/**
	 * Test: Verteilung aus Messwerten bestimmen
	 */
	@Test
	void testStringProcessingLoadSamples() {
		DataDistributionImpl dist;

		final double sum=DoubleStream.of(exampleDataDouble).sum();
		final double[] exampleDataDoubleNormalized=DoubleStream.of(exampleDataDouble).map(d->d/sum).toArray();
		final int[] samples=new int[]{0,0,0,1,1,1,1,2,2,4};
		final String[] samplesString=IntStream.of(samples).mapToObj(i->NumberTools.formatLongNoGrouping(i)).toArray(String[]::new);

		dist=DataDistributionImpl.createFromSamplesArray(samples,true);
		assertTrue(Objects.deepEquals(exampleDataDoubleNormalized,dist.densityData));

		dist=DataDistributionImpl.createFromSamplesArray(samplesString,true);
		assertTrue(Objects.deepEquals(exampleDataDoubleNormalized,dist.densityData));

		dist=DataDistributionImpl.createFromSamplesArray(new int[][]{samples},true);
		assertTrue(Objects.deepEquals(exampleDataDoubleNormalized,dist.densityData));

		dist=DataDistributionImpl.createFromSamplesArray(new String[][]{samplesString},true);
		assertTrue(Objects.deepEquals(exampleDataDoubleNormalized,dist.densityData));

		final int[][] samplesDoubleArray=new int[][]{new int[]{0,1,2,3,4},exampleDataInt};
		dist=DataDistributionImpl.createFromSamplesArray(samplesDoubleArray,true);
		assertTrue(Objects.deepEquals(exampleDataDoubleNormalized,dist.densityData));

		dist=DataDistributionImpl.createFromAnySamplesString(null,true);
		assertEquals(0,dist.densityData.length);

		dist=DataDistributionImpl.createFromAnySamplesString("",true);
		assertEquals(0,dist.densityData.length);

		dist=DataDistributionImpl.createFromAnySamplesString(String.join("\t",samplesString),true);
		assertTrue(Objects.deepEquals(exampleDataDoubleNormalized,dist.densityData));
	}

	/**
	 * Test: Erzeugung von Zufallszahlen
	 * @see DataDistributionImpl#random(org.apache.commons.math3.random.RandomGenerator)
	 */
	@Test
	void testRandom() {
		final DataDistributionImpl dist=new DataDistributionImpl(10.0,new double[]{1,3});

		assertEquals(0.0,dist.random(new DummyRandomGenerator(0)));
		assertEquals(2.5,dist.random(new DummyRandomGenerator(0.125)));
		assertEquals(5.0,dist.random(new DummyRandomGenerator(0.25)));
		assertEquals(7.5,dist.random(new DummyRandomGenerator(0.25+0.75/2)));
		assertEquals(10.0,dist.random(new DummyRandomGenerator(1)));
	}
}
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import javax.swing.ImageIcon;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.junit.jupiter.api.Test;

import mathtools.distribution.DataDistributionImpl;
import mathtools.distribution.DiscreteBinomialDistributionImpl;
import mathtools.distribution.DiscreteNegativeBinomialDistributionImpl;
import mathtools.distribution.DiscretePoissonDistributionImpl;
import mathtools.distribution.OnePointDistributionImpl;
import mathtools.distribution.RayleighDistributionImpl;
import mathtools.distribution.tools.AbstractDistributionWrapper;
import mathtools.distribution.tools.DistributionTools;
import mathtools.distribution.tools.WrapperBinomialDistribution;

/**
 * Prüft die Funktionsweise von {@link DistributionTools}
 * @author Alexander Herzog
 * @see DistributionTools
 */
class DistributionToolsTests {
	/**
	 * Test: Konstruktor ist privat? - Klasse stellt nur statische Methoden zur Verfügung und soll nicht initialisierbar sein
	 * @throws NoSuchMethodException	Konstruktor konnte nicht gefunden werden
	 * @throws IllegalAccessException	Zugriff verweigert
	 * @throws InvocationTargetException	Aufruf des Konstruktor möglich, wenn per Reflection angepasst?
	 * @throws InstantiationException	Aufruf des Konstruktor möglich, wenn per Reflection angepasst?
	 */
	@Test
	void testConstructorIsPrivate() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
		final Constructor<DistributionTools> constructor=DistributionTools.class.getDeclaredConstructor();
		assertTrue(Modifier.isPrivate(constructor.getModifiers()));
		constructor.setAccessible(true);
		constructor.newInstance();
	}

	/**
	 * Test: Abfrage der Liste der Verteilungsnamen und der Icons
	 * @see DistributionTools#getDistributionNames()
	 * @see DistributionTools#getThumbnailImageForDistributionName(String)
	 */
	@Test
	void testNames() {
		final String[] names=DistributionTools.getDistributionNames();
		assertNotNull(names);
		for (String name: names) {
			assertNotNull(name);
		}
		for (String name: names) {
			final ImageIcon icon=DistributionTools.getThumbnailImageForDistributionName(name);
			assertNotNull(icon);
		}
		assertNull(DistributionTools.getThumbnailImageForDistributionName(null));
		assertNull(DistributionTools.getThumbnailImageForDistributionName(""));
		assertNull(DistributionTools.getThumbnailImageForDistributionName("äöü"));
	}

	/**
	 * Test: Verteilungen per Name erstellen und wieder Name ermitteln
	 * @see DistributionTools#getDistributionInfo(AbstractRealDistribution)
	 * @see DistributionTools#getDistributionName(AbstractRealDistribution)
	 */
	@Test
	void testDistributions() {
		final String[] names=DistributionTools.getDistributionNames();
		assertNotNull(names);
		for (String name: names) {
			final AbstractRealDistribution dist=DistributionTools.getDistributionFromInfo(name,3,2);
			if (dist!=null) assertEquals(name,DistributionTools.getDistributionName(dist));
		}
	}

	/**
	 * Test: Finden der Wrapper auf Basis von Namen
	 * @see DistributionTools#getWrapper(String)
	 */
	@Test
	void testWrapper() {
		final String str=null;
		assertNull(DistributionTools.getWrapper(str));

		final AbstractRealDistribution dist=null;
		assertNull(DistributionTools.getWrapper(dist));

		final String[] names=DistributionTools.getDistributionNames();
		assertNotNull(names);
		for (String name: names) {
			final AbstractDistributionWrapper wrapper=DistributionTools.getWrapper(name);
			assertNotNull(wrapper);
			assertEquals(name,wrapper.getName());
			assertNotNull(wrapper.getThumbnailImage());

			assertNotNull(wrapper.getDefaultDistribution());
			if (wrapper.canBuildDistributionFromMeanAndSD) {
				if (wrapper instanceof WrapperBinomialDistribution) {
					assertNotNull(wrapper.getDistribution(100,5),"Verteilung: "+name);
					continue;
				}
				assertNotNull(wrapper.getDistribution(100,50),"Verteilung: "+name);
			}
		}
	}

	/**
	 * Test: Verteilungsdaten in String speichern und wieder lesen
	 * @see DistributionTools#distributionToString(AbstractRealDistribution)
	 * @see DistributionTools#distributionFromString(String, double)
	 */
	@Test
	void testLoadStore() {
		assertNull(DistributionTools.getDistributionFromInfo(null,3,2));
		assertNull(DistributionTools.getDistributionFromInfo("",3,2));

		final String[] names=DistributionTools.getDistributionNames();
		assertNotNull(names);
		for (String name: names) {
			final AbstractRealDistribution dist1=DistributionTools.getDistributionFromInfo(name,3,2);
			if (dist1==null) continue;
			final String store=DistributionTools.distributionToString(dist1);
			final AbstractRealDistribution dist2=DistributionTools.distributionFromString(store,1000);
			assertTrue(DistributionTools.compare(dist1,dist2));
		}
	}

	/**
	 * Test: Mittelwerte ermitteln und einstellen
	 */
	@Test
	void testMean() {
		assertFalse(DistributionTools.canSetMean(null));
		assertFalse(DistributionTools.canSetMeanExact(null));
		assertNull(DistributionTools.setMean(null,123));

		final String[] names=DistributionTools.getDistributionNames();
		assertNotNull(names);
		for (String name: names) {
			final AbstractRealDistribution dist1=DistributionTools.getDistributionFromInfo(name,3,2);
			if (dist1==null) continue;
			assertEquals(3.0,DistributionTools.getMean(dist1),0.00001,"Verteilung: "+name);
			boolean exactStdAvailable=true;
			if (dist1 instanceof OnePointDistributionImpl) exactStdAvailable=false;
			if (dist1 instanceof ExponentialDistribution) exactStdAvailable=false;
			if (dist1 instanceof ChiSquaredDistribution) exactStdAvailable=false;
			if (dist1 instanceof RayleighDistributionImpl) exactStdAvailable=false;
			if (dist1 instanceof DiscreteBinomialDistributionImpl) exactStdAvailable=false;
			if (dist1 instanceof DiscretePoissonDistributionImpl) exactStdAvailable=false;
			if (dist1 instanceof DiscreteNegativeBinomialDistributionImpl) exactStdAvailable=false;
			if (exactStdAvailable) {
				assertEquals(2,DistributionTools.getStandardDeviation(dist1),10E-10,"Verteilung: "+name);
				assertEquals(2.0/3.0,DistributionTools.getCV(dist1),10E-10,"Verteilung: "+name);
			}
			if (DistributionTools.canSetMean(dist1)) {
				final double newVar;
				if (dist1 instanceof DiscreteNegativeBinomialDistributionImpl) {
					/* So hohe Varianz geht nicht */
					newVar=1.5;
				} else {
					newVar=5;
				}
				final AbstractRealDistribution dist2=DistributionTools.setMean(dist1,newVar);
				assertNotNull(dist2,"Verteilung: "+name);
				if (DistributionTools.canSetMeanExact(dist2)) {
					assertEquals(newVar,DistributionTools.getMean(dist2),0.00001,"Verteilung: "+name);
				}
			} else {
				final AbstractRealDistribution dist2=DistributionTools.setMean(dist1,5);
				assertNull(dist2,"Verteilung: "+name);
			}
		}
	}

	/**
	 * Test: Standardabweichungen ermitteln und einstellen
	 */
	@Test
	void testStandardDeviation() {
		assertFalse(DistributionTools.canSetStandardDeviation(null));
		assertFalse(DistributionTools.canSetStandardDeviationExact(null));
		assertNull(DistributionTools.setStandardDeviation(null,123));

		final String[] names=DistributionTools.getDistributionNames();
		assertNotNull(names);
		for (String name: names) {
			final AbstractRealDistribution dist1=DistributionTools.getDistributionFromInfo(name,3,2);
			if (dist1==null) continue;
			boolean exactStdAvailable=true;
			if (dist1 instanceof OnePointDistributionImpl) exactStdAvailable=false;
			if (dist1 instanceof ExponentialDistribution) exactStdAvailable=false;
			if (dist1 instanceof ChiSquaredDistribution) exactStdAvailable=false;
			if (dist1 instanceof RayleighDistributionImpl) exactStdAvailable=false;
			if (dist1 instanceof DiscreteBinomialDistributionImpl) exactStdAvailable=false;
			if (dist1 instanceof DiscretePoissonDistributionImpl) exactStdAvailable=false;
			if (dist1 instanceof DiscreteNegativeBinomialDistributionImpl) exactStdAvailable=false;
			if (exactStdAvailable) {
				assertEquals(2,DistributionTools.getStandardDeviation(dist1),10E-10,"Verteilung: "+name);
				assertEquals(2.0/3.0,DistributionTools.getCV(dist1),10E-10,"Verteilung: "+name);
			}
			if (DistributionTools.canSetStandardDeviation(dist1)) {
				final AbstractRealDistribution dist2=DistributionTools.setStandardDeviation(dist1,5);
				assertNotNull(dist2,"Verteilung: "+name);
				if (DistributionTools.canSetStandardDeviationExact(dist2)) {
					assertEquals(5.0,DistributionTools.getStandardDeviation(dist2),0.00001,"Verteilung: "+name);
				}
			} else {
				final AbstractRealDistribution dist2=DistributionTools.setStandardDeviation(dist1,5);
				assertNull(dist2,"Verteilung: "+name);
			}
		}
	}

	/**
	 * Test: Verteilungsinformationen abrufen
	 * @see DistributionTools#getDistributionInfo(AbstractRealDistribution)
	 * @see DistributionTools#getDistributionLongInfo(AbstractRealDistribution)
	 */
	@Test
	void testDistributionInfo() {
		final String[] names=DistributionTools.getDistributionNames();
		assertNotNull(names);
		for (String name: names) {
			final AbstractRealDistribution dist=DistributionTools.getDistributionFromInfo(name,3,2);
			if (dist==null) continue;
			final String s1=DistributionTools.getDistributionInfo(dist);
			final String s2=DistributionTools.getDistributionLongInfo(dist);
			assertNotNull(s1);
			assertTrue(!s1.isEmpty());
			assertNotNull(s2);
			assertTrue(!s2.isEmpty());
		}
	}

	/**
	 * Test: Parameter einstellen und auslesen
	 * @see DistributionTools#getParameter(AbstractRealDistribution, int)
	 * @see DistributionTools#setParameter(AbstractRealDistribution, int, double)
	 */
	@Test
	void testParameter() {
		final NormalDistribution sourceDist=new NormalDistribution(10,3);
		assertEquals(0,DistributionTools.getParameter(sourceDist,0),0.0001);
		assertEquals(10,DistributionTools.getParameter(sourceDist,1),0.0001);
		assertEquals(3,DistributionTools.getParameter(sourceDist,2),0.0001);
		assertEquals(0,DistributionTools.getParameter(sourceDist,3),0.0001);
		assertEquals(0,DistributionTools.getParameter(sourceDist,4),0.0001);
		assertEquals(0,DistributionTools.getParameter(sourceDist,5),0.0001);

		AbstractRealDistribution newDist;

		newDist=DistributionTools.setParameter(sourceDist,1,7);
		assertTrue(newDist instanceof NormalDistribution);
		assertEquals(7,((NormalDistribution)newDist).getMean(),0.0001);

		newDist=DistributionTools.setParameter(sourceDist,2,5);
		assertTrue(newDist instanceof NormalDistribution);
		assertEquals(5,((NormalDistribution)newDist).getStandardDeviation(),0.0001);

		assertNull(DistributionTools.setParameter(sourceDist,0,123));
		assertNull(DistributionTools.setParameter(sourceDist,3,123));
		assertNull(DistributionTools.setParameter(sourceDist,4,123));
		assertNull(DistributionTools.setParameter(sourceDist,5,123));
	}

	/**
	 * Test: Verteilungen kopieren
	 * @see DistributionTools#cloneDistribution(AbstractRealDistribution)
	 */
	@Test
	void testClone() {
		final ExponentialDistribution expDist=new ExponentialDistribution(17);
		final DataDistributionImpl dataDist=new DataDistributionImpl(10,new double[]{1,2,3});
		AbstractRealDistribution dist;

		/* Clone */

		dist=DistributionTools.cloneDistribution(expDist);
		assertTrue(DistributionTools.compare(expDist,dist));
		assertFalse(DistributionTools.compare(dataDist,dist));

		dist=DistributionTools.cloneDistribution(dataDist);
		assertFalse(DistributionTools.compare(expDist,dist));
		assertTrue(DistributionTools.compare(dataDist,dist));

		/* Normalize */

		dist=DistributionTools.normalizeDistribution(expDist);
		assertSame(dist,expDist);

		dist=DistributionTools.normalizeDistribution(dataDist);
		assertNotSame(dist,dataDist);
		assertTrue(dist instanceof DataDistributionImpl);
		assertEquals(3,((DataDistributionImpl)dist).densityData.length);
		assertEquals(1.0/6.0,((DataDistributionImpl)dist).densityData[0],0.0001);
		assertEquals(2.0/6.0,((DataDistributionImpl)dist).densityData[1],0.0001);
		assertEquals(3.0/6.0,((DataDistributionImpl)dist).densityData[2],0.0001);

		/* Clone & Normalize */

		dist=DistributionTools.cloneAndNormalizeDistribution(expDist);
		assertNotSame(dist,expDist);
		assertTrue(DistributionTools.compare(expDist,dist));

		dist=DistributionTools.cloneAndNormalizeDistribution(dataDist);
		assertNotSame(dist,dataDist);
		assertTrue(dist instanceof DataDistributionImpl);
		assertEquals(3,((DataDistributionImpl)dist).densityData.length);
		assertEquals(1.0/6.0,((DataDistributionImpl)dist).densityData[0],0.0001);
		assertEquals(2.0/6.0,((DataDistributionImpl)dist).densityData[1],0.0001);
		assertEquals(3.0/6.0,((DataDistributionImpl)dist).densityData[2],0.0001);
	}
}
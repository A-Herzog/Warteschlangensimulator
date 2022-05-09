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
package statisticstests;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import mathtools.distribution.DataDistributionImpl;
import statistics.StatisticsDataPerformanceIndicatorWithNegativeValues;

/**
 * Testet die Funktionsweise von {@link StatisticsDataPerformanceIndicatorWithNegativeValues}
 * @author Alexander Herzog
 * @see StatisticsDataPerformanceIndicatorWithNegativeValues
 */
class StatisticsDataPerformanceIndicatorWithNegativeValuesTest {
	/**
	 * Konstruktor der Klasse
	 */
	public StatisticsDataPerformanceIndicatorWithNegativeValuesTest() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	/**
	 * Test: Konstruktor-Varianten
	 */
	@Test
	void constructorTest() {
		StatisticsDataPerformanceIndicatorWithNegativeValues collector;

		collector=new StatisticsDataPerformanceIndicatorWithNegativeValues(new String[]{"xml"},100,100);
		assertEquals(0,collector.getCount());
		assertEquals(0,collector.getSum());
		assertEquals(0,collector.getMean());
	}

	/**
	 * Test: Werte direkt hinzufügen, Objekte zusammenführen
	 */
	@Test
	void addTest() {
		StatisticsDataPerformanceIndicatorWithNegativeValues collector, collector2;

		/* Normale add-Funktion */

		collector=new StatisticsDataPerformanceIndicatorWithNegativeValues(new String[]{"xml"},100,100);
		collector.add(0);
		collector.add(1);
		collector.add(2);
		collector.add(3);
		collector.add(4);
		collector.add(-5);
		assertEquals(6,collector.getCount());
		assertEquals(5,collector.getSum());
		assertEquals(5.0/6.0,collector.getMean());
		assertEquals(-5,collector.getLastAddedValue());

		/* Normale add-Funktion: Reihenfolge */

		collector=new StatisticsDataPerformanceIndicatorWithNegativeValues(new String[]{"xml"},100,100);
		collector.add(3);
		collector.add(0);
		collector.add(1);
		collector.add(-5);
		collector.add(2);
		collector.add(4);
		assertEquals(6,collector.getCount());
		assertEquals(5,collector.getSum());
		assertEquals(5.0/6.0,collector.getMean());

		/* Weitere Collector-Objekte hinzufügen: null */

		collector=new StatisticsDataPerformanceIndicatorWithNegativeValues(new String[]{"xml"},100,100);
		collector.add(0);
		collector.add(1);
		collector.add(2);
		collector.add(3);
		collector.add(4);
		collector.add(null);
		assertEquals(5,collector.getCount());
		assertEquals(10,collector.getSum());
		assertEquals(2,collector.getMean());

		/* Weitere Collector-Objekte hinzufügen: leer */

		collector=new StatisticsDataPerformanceIndicatorWithNegativeValues(new String[]{"xml"},100,100);
		collector.add(0);
		collector.add(1);
		collector.add(2);
		collector.add(3);
		collector.add(4);
		collector.add(new StatisticsDataPerformanceIndicatorWithNegativeValues(new String[]{"xml"},100,100));
		assertEquals(5,collector.getCount());
		assertEquals(10,collector.getSum());
		assertEquals(2,collector.getMean());

		/* Weitere Collector-Objekte hinzufügen: weitere Daten */

		collector2=new StatisticsDataPerformanceIndicatorWithNegativeValues(new String[]{"xml"},100,100);
		collector2.add(0);
		collector2.add(1);

		collector=new StatisticsDataPerformanceIndicatorWithNegativeValues(new String[]{"xml"},100,100);
		collector.add(2);
		collector.add(3);
		collector.add(4);
		collector.add(collector2);
		assertEquals(5,collector.getCount());
		assertEquals(10,collector.getSum());
		assertEquals(2,collector.getMean());

		/* Weitere Collector-Objekte hinzufügen: weitere Daten in leeren Collector */

		collector2=new StatisticsDataPerformanceIndicatorWithNegativeValues(new String[]{"xml"},100,100);
		collector2.add(0);
		collector2.add(1);
		collector2.add(2);
		collector2.add(3);
		collector2.add(4);

		collector=new StatisticsDataPerformanceIndicatorWithNegativeValues(new String[]{"xml"},100,100);
		collector.add(collector2);
		assertEquals(5,collector.getCount());
		assertEquals(10,collector.getSum());
		assertEquals(2,collector.getMean());

		/* Werte außerhalb des Zielbereichs */

		collector=new StatisticsDataPerformanceIndicatorWithNegativeValues(new String[]{"xml"},100,100);
		collector.add(-1);
		collector.add(150);
		assertEquals(2,collector.getCount());
		assertEquals(149,collector.getSum()); /* Werte größer als maxDist werden in der Summe durchaus passend erfasst */
		assertEquals(149.0/2.0,collector.getMean());
	}

	/**
	 * Test: Mehrere gleiche Werte hinzufügen, Objekte zusammenführen
	 */
	@Test
	void addMultiTest() {
		StatisticsDataPerformanceIndicatorWithNegativeValues collector;

		collector=new StatisticsDataPerformanceIndicatorWithNegativeValues(new String[]{"xml"},100,100);
		collector.add(0,4);
		collector.add(10,1);
		assertEquals(5,collector.getCount());
		assertEquals(10,collector.getSum());
		assertEquals(2,collector.getMean());
		assertEquals(10,collector.getLastAddedValue());

		collector=new StatisticsDataPerformanceIndicatorWithNegativeValues(new String[]{"xml"},100,100);
		collector.add(10,1);
		collector.add(0,4);
		assertEquals(5,collector.getCount());
		assertEquals(10,collector.getSum());
		assertEquals(2,collector.getMean());
		assertEquals(0,collector.getLastAddedValue());

		collector=new StatisticsDataPerformanceIndicatorWithNegativeValues(new String[]{"xml"},100,100);
		collector.add(10,0);
		collector.add(10,-3);
		assertEquals(0,collector.getCount());
	}

	/**
	 * Test: Erfassung zurücksetzen/löschen
	 */
	@Test
	void resetTest() {
		StatisticsDataPerformanceIndicatorWithNegativeValues collector;

		collector=new StatisticsDataPerformanceIndicatorWithNegativeValues(new String[]{"xml"},100,100);
		collector.add(0);
		collector.add(1);
		collector.add(2);
		collector.add(3);
		collector.add(4);
		collector.reset();
		assertEquals(0,collector.getCount());
		assertEquals(0,collector.getSum());
		assertEquals(0,collector.getMean());
	}

	/**
	 * Test: Objekt kopieren
	 */
	@Test
	void cloneTest() {
		StatisticsDataPerformanceIndicatorWithNegativeValues collector;

		collector=new StatisticsDataPerformanceIndicatorWithNegativeValues(new String[]{"xml"},100,100);
		collector.add(0);
		collector.add(1);
		collector.add(2);
		collector.add(3);
		collector.add(4);
		collector=collector.clone();
		assertEquals(5,collector.getCount());
		assertEquals(10,collector.getSum());
		assertEquals(2,collector.getMean());
		assertEquals(2.5,collector.getVar());
		assertEquals(Math.sqrt(2.5),collector.getSD());
		assertEquals(Math.sqrt(2.5)/2,collector.getCV());
		assertEquals(0,collector.getSk(),0.00001);
		assertEquals(-1.2,collector.getKurt(),0.00001);
		assertEquals(0,collector.getMin());
		assertEquals(4,collector.getMax());

		collector=new StatisticsDataPerformanceIndicatorWithNegativeValues(new String[]{"xml"},100,100);
		collector=collector.clone();
		assertEquals(0,collector.getCount());
		assertEquals(0,collector.getSum());
		assertEquals(0,collector.getMean());

		collector=new StatisticsDataPerformanceIndicatorWithNegativeValues(new String[]{"xml"},100,100);
		collector.add(0);
		collector.add(1);
		collector.add(2);
		collector=collector.cloneEmpty();
		assertEquals(0,collector.getCount());
	}

	/**
	 * Test: Werte in die Erfassung laden und dann Kenngrößen auslesen
	 */
	@Test
	void calcTest() {
		StatisticsDataPerformanceIndicatorWithNegativeValues collector;

		collector=new StatisticsDataPerformanceIndicatorWithNegativeValues(new String[]{"xml"},100,100);
		assertEquals(0,collector.getCount());
		assertEquals(0,collector.getSum());
		assertEquals(0,collector.getMean());
		assertEquals(0,collector.getVar());
		assertEquals(0,collector.getSD());
		assertEquals(0,collector.getCV());
		assertEquals(0,collector.getMin());
		assertEquals(0,collector.getMax());

		collector=new StatisticsDataPerformanceIndicatorWithNegativeValues(new String[]{"xml"},100,100);
		collector.add(0);
		collector.add(1);
		collector.add(2);
		collector.add(3);
		collector.add(4);

		assertEquals(5,collector.getCount());
		assertEquals(10,collector.getSum());
		assertEquals(2,collector.getMean());
		assertEquals(2.5,collector.getVar());
		assertEquals(Math.sqrt(2.5),collector.getSD());
		assertEquals(Math.sqrt(2.5)/2,collector.getCV());
		assertEquals(0,collector.getMin());
		assertEquals(4,collector.getMax());

		assertEquals(0,collector.getQuantil(0.2));
		assertEquals(1,collector.getQuantil(0.4));
		assertEquals(2,collector.getQuantil(0.6));
		assertEquals(3,collector.getQuantil(0.8));
		assertArrayEquals(new double[] {0,1,2,3},collector.getQuantil(new double[]{0.2,0.4,0.6,0.8}),0.0001);
		assertEquals(2,collector.getMedian());

		final double w1=collector.getConfidenceHalfWide(0.05);
		final double w2=collector.getConfidenceHalfWide(0.1);
		final double w3=collector.getConfidenceHalfWide(0.2);
		assertTrue(w1>0);
		assertTrue(w2>0);
		assertTrue(w3>0);
		assertTrue(w2<=w1);
		assertTrue(w3<=w2);
		assertArrayEquals(collector.getConfidenceHalfWide(new double[] {0.05,0.1,0.2}),new double[] {w1,w2,w3},0.0001);
	}

	/**
	 * Test: Abrufen der Verteilung die auf Basis der geladenen Werte generiert wurde
	 */
	@Test
	void distributionTest() {
		StatisticsDataPerformanceIndicatorWithNegativeValues collector;
		DataDistributionImpl dist1, dist2;

		collector=new StatisticsDataPerformanceIndicatorWithNegativeValues(new String[]{"xml"},100,100);
		collector.add(0);
		collector.add(1);
		collector.add(2);
		collector.add(3);
		collector.add(4);

		double[] data=new double[100];
		data[0]=1;
		data[1]=1;
		data[2]=1;
		data[3]=1;
		data[4]=1;

		dist1=collector.getDistribution();
		assertArrayEquals(data,dist1.densityData,0.000001);

		dist2=collector.getNormalizedDistribution();
		dist1.normalizeDensity();
		assertArrayEquals(dist1.densityData,dist2.densityData,0.000001);
	}

	/**
	 * Test: Daten in xml-Dokument schreiben und aus diesem wieder laden
	 */
	@Test
	void loadTest() {
		StatisticsDataPerformanceIndicatorWithNegativeValues collector;

		collector=new StatisticsDataPerformanceIndicatorWithNegativeValues(new String[]{"xml"},100,100);
		collector.add(0);
		collector.add(1);
		collector.add(2);
		collector.add(3);
		collector.add(4);

		final DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {db=dbf.newDocumentBuilder();} catch (ParserConfigurationException e) {assertTrue(false); return;}
		Document doc=db.newDocument();
		final Element root=doc.createElement("root"); doc.appendChild(root);

		Element xmlNode=collector.addToXML(doc,root,null);
		assertNotNull(xmlNode);

		collector=new StatisticsDataPerformanceIndicatorWithNegativeValues(new String[]{"xml"},100,100);
		assertNull(collector.loadFromXML(xmlNode));
		assertEquals(5,collector.getCount());
		assertEquals(10,collector.getSum());
		assertEquals(2,collector.getMean());
	}
}

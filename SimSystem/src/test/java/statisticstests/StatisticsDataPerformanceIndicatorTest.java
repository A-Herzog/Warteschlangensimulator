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
import statistics.StatisticsDataPerformanceIndicator;

/**
 * Testet die Funktionsweise von {@link StatisticsDataPerformanceIndicator}
 * @author Alexander Herzog
 * @see StatisticsDataPerformanceIndicator
 */
class StatisticsDataPerformanceIndicatorTest {
	/**
	 * Test: Konstruktor-Varianten
	 */
	@Test
	void constructorTest() {
		StatisticsDataPerformanceIndicator collector;

		collector=new StatisticsDataPerformanceIndicator(new String[]{"xml"},100,100);
		assertEquals(0,collector.getCount());
		assertEquals(0,collector.getSum());
		assertEquals(0,collector.getMean());
	}

	/**
	 * Test: Werte direkt hinzufügen, Objekte zusammenführen
	 */
	@Test
	void addTest() {
		StatisticsDataPerformanceIndicator collector, collector2;

		/* Normale add-Funktion */

		collector=new StatisticsDataPerformanceIndicator(new String[]{"xml"},100,100);
		collector.add(0);
		collector.add(1);
		collector.add(2);
		collector.add(3);
		collector.add(4);
		assertEquals(5,collector.getCount());
		assertEquals(10,collector.getSum());
		assertEquals(2,collector.getMean());
		assertEquals(4,collector.getLastAddedValue());

		/* Normale add-Funktion: Reihenfolge */

		collector=new StatisticsDataPerformanceIndicator(new String[]{"xml"},100,100);
		collector.add(3);
		collector.add(0);
		collector.add(1);
		collector.add(2);
		collector.add(4);
		assertEquals(5,collector.getCount());
		assertEquals(10,collector.getSum());
		assertEquals(2,collector.getMean());

		/* Weitere Collector-Objekte hinzufügen: null */

		collector=new StatisticsDataPerformanceIndicator(new String[]{"xml"},100,100);
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

		collector=new StatisticsDataPerformanceIndicator(new String[]{"xml"},100,100);
		collector.add(0);
		collector.add(1);
		collector.add(2);
		collector.add(3);
		collector.add(4);
		collector.add(new StatisticsDataPerformanceIndicator(new String[]{"xml"},100,100));
		assertEquals(5,collector.getCount());
		assertEquals(10,collector.getSum());
		assertEquals(2,collector.getMean());

		/* Weitere Collector-Objekte hinzufügen: weitere Daten */

		collector2=new StatisticsDataPerformanceIndicator(new String[]{"xml"},100,100);
		collector2.add(0);
		collector2.add(1);

		collector=new StatisticsDataPerformanceIndicator(new String[]{"xml"},100,100);
		collector.add(2);
		collector.add(3);
		collector.add(4);
		collector.add(collector2);
		assertEquals(5,collector.getCount());
		assertEquals(10,collector.getSum());
		assertEquals(2,collector.getMean());

		/* Weitere Collector-Objekte hinzufügen: weitere Daten in leeren Collector */

		collector2=new StatisticsDataPerformanceIndicator(new String[]{"xml"},100,100);
		collector2.add(0);
		collector2.add(1);
		collector2.add(2);
		collector2.add(3);
		collector2.add(4);

		collector=new StatisticsDataPerformanceIndicator(new String[]{"xml"},100,100);
		collector.add(collector2);
		assertEquals(5,collector.getCount());
		assertEquals(10,collector.getSum());
		assertEquals(2,collector.getMean());

		/* Werte außerhalb des Zielbereichs */

		collector=new StatisticsDataPerformanceIndicator(new String[]{"xml"},100,100);
		collector.add(-3);
		collector.add(150);
		assertEquals(2,collector.getCount());
		assertEquals(150,collector.getSum()); /* Werte größer als maxDist werden in der Summe durchaus passend erfasst */
		assertEquals(75,collector.getMean());
	}

	/**
	 * Test: Mehrere gleiche Werte hinzufügen, Objekte zusammenführen
	 */
	@Test
	void addMultiTest() {
		StatisticsDataPerformanceIndicator collector;

		collector=new StatisticsDataPerformanceIndicator(new String[]{"xml"},100,100);
		collector.add(0,4);
		collector.add(10,1);
		assertEquals(5,collector.getCount());
		assertEquals(10,collector.getSum());
		assertEquals(2,collector.getMean());
		assertEquals(10,collector.getLastAddedValue());

		collector=new StatisticsDataPerformanceIndicator(new String[]{"xml"},100,100);
		collector.add(10,1);
		collector.add(0,4);
		assertEquals(5,collector.getCount());
		assertEquals(10,collector.getSum());
		assertEquals(2,collector.getMean());
		assertEquals(0,collector.getLastAddedValue());

		collector=new StatisticsDataPerformanceIndicator(new String[]{"xml"},100,100);
		collector.add(10,0);
		collector.add(10,-3);
		assertEquals(0,collector.getCount());
	}

	/**
	 * Test: Erfassung zurücksetzen/löschen
	 */
	@Test
	void resetTest() {
		StatisticsDataPerformanceIndicator collector;

		collector=new StatisticsDataPerformanceIndicator(new String[]{"xml"},100,100);
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
		StatisticsDataPerformanceIndicator collector;

		collector=new StatisticsDataPerformanceIndicator(new String[]{"xml"},100,100);
		collector.add(0);
		collector.add(1);
		collector.add(2);
		collector.add(3);
		collector.add(4);
		collector=collector.clone();
		assertEquals(5,collector.getCount());
		assertEquals(10,collector.getSum());
		assertEquals(2,collector.getMean());

		collector=new StatisticsDataPerformanceIndicator(new String[]{"xml"},100,100);
		collector=collector.clone();
		assertEquals(0,collector.getCount());
		assertEquals(0,collector.getSum());
		assertEquals(0,collector.getMean());

		collector=new StatisticsDataPerformanceIndicator(new String[]{"xml"},100,100);
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
		StatisticsDataPerformanceIndicator collector;

		collector=new StatisticsDataPerformanceIndicator(new String[]{"xml"},100,100);
		assertEquals(0,collector.getCount());
		assertEquals(0,collector.getSum());
		assertEquals(0,collector.getMean());
		assertEquals(0,collector.getVar());
		assertEquals(0,collector.getSD());
		assertEquals(0,collector.getCV());
		assertEquals(0,collector.getMin());
		assertEquals(0,collector.getMax());

		collector=new StatisticsDataPerformanceIndicator(new String[]{"xml"},100,100);
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
		assertEquals(0,collector.getSk(),0.00001);
		assertEquals(-1.2,collector.getKurt(),0.00001);
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
		StatisticsDataPerformanceIndicator collector;
		DataDistributionImpl dist1, dist2;

		collector=new StatisticsDataPerformanceIndicator(new String[]{"xml"},100,100);
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
	 * Test: Berechnung von Konfidenzintervallen auf Basis von Batch-Means
	 */
	@Test
	void batchTest() {
		StatisticsDataPerformanceIndicator collector, collector2;
		double d;

		collector2=new StatisticsDataPerformanceIndicator(new String[]{"xml"},100,100,0,3);
		collector2.add(0);
		collector2.add(1);
		collector2.add(2);
		collector2.add(3);
		collector2.add(4);
		collector2.add(5);
		collector2.add(6);
		collector2=collector2.clone();
		collector=new StatisticsDataPerformanceIndicator(new String[]{"xml"},100,100,0,3);
		collector.add(collector2);

		assertTrue(!collector2.isCorrelationAvailable());
		assertTrue(!collector.isCorrelationAvailable());

		assertEquals(3,collector.getBatchSize());
		assertEquals(2,collector.getBatchCount());

		d=collector.getBatchVar();
		assertEquals(Math.sqrt(d),collector.getBatchSD()); /* nein, rechnen wir nicht nach */

		d=collector.getVarByBatch();
		assertEquals(Math.sqrt(d),collector.getSDByBatch()); /* nein, rechnen wir nicht nach */

		assertTrue(collector.getBatchMeanConfidenceHalfWide(0.05)>0);
		assertEquals(collector.getBatchMeanConfidenceHalfWide(0.05),collector.getBatchMeanConfidenceHalfWide(new double[] {0.05})[0]);
	}

	/**
	 * Test: Berechnung der Autokorrelation der Werte
	 */
	@Test
	void correlationTest() {
		StatisticsDataPerformanceIndicator collector, collector2;

		collector2=new StatisticsDataPerformanceIndicator(new String[]{"xml"},100,100,100,0);
		for (int i=0;i<100;i++) {
			for (int j=0;j<100;j++) collector2.add(j);
			for (int j=100;j>0;j--) collector2.add(j);
		}
		collector2=collector2.clone();
		collector=new StatisticsDataPerformanceIndicator(new String[]{"xml"},100,100,100,0);
		collector.add(collector2);

		assertTrue(collector2.isCorrelationAvailable());
		assertTrue(collector.isCorrelationAvailable());

		double[] data=collector.getCorrelationData();
		assertNotNull(data);
		assertEquals(11,data.length);
		assertEquals(1,data[0]);
		assertTrue(data[1]<1);
		assertTrue(data[1]>0);

		assertTrue(collector.isCorrelationAvailable());
		assertTrue(collector.getCorrelationLevelDistance(0.9)>0);
		assertTrue(collector.getCorrelationLevelDistance(0.9)<collector.getCorrelationLevelDistance(0.1));
	}

	/**
	 * Test: Daten in xml-Dokument schreiben und aus diesem wieder laden
	 */
	@Test
	void loadTest() {
		StatisticsDataPerformanceIndicator collector;

		collector=new StatisticsDataPerformanceIndicator(new String[]{"xml"},100,100,100,2);
		collector.add(0);
		collector.add(1);
		collector.add(2);
		collector.add(3);
		collector.add(4);
		assertTrue(collector.isCorrelationAvailable());
		assertEquals(2,collector.getBatchCount());

		final DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {db=dbf.newDocumentBuilder();} catch (ParserConfigurationException e) {assertTrue(false); return;}
		Document doc=db.newDocument();
		final Element root=doc.createElement("root"); doc.appendChild(root);

		Element xmlNode=collector.addToXML(doc,root,null);
		assertNotNull(xmlNode);

		collector=new StatisticsDataPerformanceIndicator(new String[]{"xml"},100,100);
		assertNull(collector.loadFromXML(xmlNode));
		assertEquals(5,collector.getCount());
		assertEquals(10,collector.getSum());
		assertEquals(2,collector.getMean());
		assertTrue(collector.isCorrelationAvailable());
		assertEquals(2,collector.getBatchCount());
	}
}

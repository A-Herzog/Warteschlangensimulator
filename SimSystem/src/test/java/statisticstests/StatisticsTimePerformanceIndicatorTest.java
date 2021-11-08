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
import statistics.StatisticsTimePerformanceIndicator;

/**
 * Testet die Funktionsweise von {@link StatisticsTimePerformanceIndicator}
 * @author Alexander Herzog
 * @see StatisticsTimePerformanceIndicator
 */
class StatisticsTimePerformanceIndicatorTest {
	/**
	 * Test: Konstruktor-Varianten
	 */
	@Test
	void constructorTest() {
		StatisticsTimePerformanceIndicator collector;

		collector=new StatisticsTimePerformanceIndicator(new String[]{"xml"});
		assertEquals(0,collector.getSum());
		assertEquals(0,collector.getTimeMin());
		assertEquals(0,collector.getTimeMax());
	}

	/**
	 * Test: Werte direkt hinzufügen, Objekte zusammenführen
	 */
	@Test
	void addTest() {
		StatisticsTimePerformanceIndicator collector, collector2;

		/* Normale add-Funktion */

		collector=new StatisticsTimePerformanceIndicator(new String[]{"xml"});
		collector.setTime(0);
		collector.set(20,1);
		collector.set(40,2);
		collector.set(60,3);
		assertEquals(3,collector.getCurrentState());
		assertEquals(40,collector.getSum());
		assertEquals(1,collector.getTimeMin());
		assertEquals(2,collector.getTimeMax());

		/* Weitere Collector-Objekte hinzufügen: null */

		collector=new StatisticsTimePerformanceIndicator(new String[]{"xml"});
		collector.setTime(0);
		collector.set(20,1);
		collector.set(40,2);
		collector.set(60,3);
		collector.add(null);
		assertEquals(3,collector.getCurrentState());
		assertEquals(40,collector.getSum());
		assertEquals(1,collector.getTimeMin());
		assertEquals(2,collector.getTimeMax());

		/* Weitere Collector-Objekte hinzufügen: leer */

		collector=new StatisticsTimePerformanceIndicator(new String[]{"xml"});
		collector.setTime(0);
		collector.set(20,1);
		collector.set(40,2);
		collector.set(60,3);
		collector.add(new StatisticsTimePerformanceIndicator(new String[]{"xml"}));
		assertEquals(3,collector.getCurrentState());
		assertEquals(40,collector.getSum());
		assertEquals(1,collector.getTimeMin());
		assertEquals(2,collector.getTimeMax());

		/* Weitere Collector-Objekte hinzufügen: weitere Daten */

		collector2=new StatisticsTimePerformanceIndicator(new String[]{"xml"});
		collector2.setTime(0);
		collector2.set(20,1);
		collector2.set(40,2);
		collector2.set(60,3);

		collector=new StatisticsTimePerformanceIndicator(new String[]{"xml"});
		collector.setTime(0);
		collector.set(20,1);
		collector.set(40,2);
		collector.set(60,3);
		collector.add(collector2);
		assertEquals(3,collector.getCurrentState());
		assertEquals(80,collector.getSum());
		assertEquals(1,collector.getTimeMin());
		assertEquals(2,collector.getTimeMax());
	}

	/**
	 * Test: Erfassung zurücksetzen/löschen
	 */
	@Test
	void resetTest() {
		StatisticsTimePerformanceIndicator collector;

		collector=new StatisticsTimePerformanceIndicator(new String[]{"xml"});
		collector.setTime(0);
		collector.set(20,1);
		collector.set(40,2);
		collector.set(60,3);
		collector.reset();
		assertEquals(0,collector.getSum());
	}

	/**
	 * Test: Objekt kopieren
	 */
	@Test
	void cloneTest() {
		StatisticsTimePerformanceIndicator collector;

		collector=new StatisticsTimePerformanceIndicator(new String[]{"xml"});
		collector.setTime(0);
		collector.set(20,1);
		collector.set(40,2);
		collector.set(60,3);
		collector=collector.clone();
		assertEquals(3,collector.getCurrentState());
		assertEquals(40,collector.getSum());
		assertEquals(1,collector.getTimeMin());
		assertEquals(2,collector.getTimeMax());

		collector=new StatisticsTimePerformanceIndicator(new String[]{"xml"});
		collector.setTime(0);
		collector.set(20,1);
		collector.set(40,2);
		collector.set(60,3);
		collector=collector.cloneEmpty();
		assertEquals(0,collector.getSum());
	}

	/**
	 * Test: Werte in die Erfassung laden und dann Kenngrößen auslesen
	 */
	@Test
	void calcTest() {
		StatisticsTimePerformanceIndicator collector;

		collector=new StatisticsTimePerformanceIndicator(new String[]{"xml"});
		collector.setTime(0);
		collector.set(20,1);
		collector.set(40,2);
		collector.set(60,3);
		collector.set(80,2);

		assertEquals(2,collector.getCurrentState());
		assertEquals(60,collector.getSum());
		assertEquals(60,collector.getTimeSum());
		assertEquals(1,collector.getTimeMin());
		assertEquals(3,collector.getTimeMax());

		assertEquals(2,collector.getTimeMean());
		assertEquals(2,collector.getTimeMean()); /* Cache */
		final double d=collector.getTimeVar();
		assertEquals(Math.sqrt(d),collector.getTimeSD());
		assertEquals(Math.sqrt(d)/2,collector.getTimeCV());
		assertEquals(0,collector.getTimeSk());
		assertTrue(collector.getTimeKurt()<0);

		assertEquals(1.0/3.0,collector.getTimePartForState(1));
		assertEquals(1.0/3.0,collector.getTimePartForState(2));
		assertEquals(1.0/3.0,collector.getTimePartForState(3));

		assertEquals(1,collector.getQuantil(0.2));
		assertEquals(2,collector.getQuantil(0.4));
		assertEquals(2,collector.getQuantil(0.6));
		assertEquals(3,collector.getQuantil(0.8));
		assertArrayEquals(new int[] {1,2,2,3},collector.getQuantil(new double[] {0.2,0.4,0.6,0.8}));
		assertEquals(2,collector.getTimeMedian());
	}

	/**
	 * Test: Abrufen der Verteilung die auf Basis der geladenen Werte generiert wurde
	 */
	@Test
	void distributionTest() {
		StatisticsTimePerformanceIndicator collector;

		collector=new StatisticsTimePerformanceIndicator(new String[]{"xml"});
		collector.setTime(0);
		collector.set(20,1);
		collector.set(40,2);
		collector.set(60,3);
		collector.set(80,2);

		DataDistributionImpl dist=collector.getDistribution();
		assertNotNull(dist);
		assertTrue(dist.densityData.length>=4);
		assertEquals(0,dist.densityData[0]);
		assertEquals(20,dist.densityData[1]);
		assertEquals(20,dist.densityData[2]);
		assertEquals(20,dist.densityData[3]);

		assertArrayEquals(dist.densityData,collector.getReadOnlyDistribution().densityData,0.00001);

		dist.normalizeDensity();
		assertArrayEquals(dist.densityData,collector.getNormalizedDistribution().densityData,0.00001);
	}

	/**
	 * Test: Daten in xml-Dokument schreiben und aus diesem wieder laden
	 */
	@Test
	void loadTest() {
		StatisticsTimePerformanceIndicator collector;

		collector=new StatisticsTimePerformanceIndicator(new String[]{"xml"});
		collector.setTime(0);
		collector.set(20,1);
		collector.set(40,2);
		collector.set(60,3);

		final DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {db=dbf.newDocumentBuilder();} catch (ParserConfigurationException e) {assertTrue(false); return;}
		Document doc=db.newDocument();
		final Element root=doc.createElement("root"); doc.appendChild(root);

		Element xmlNode=collector.addToXML(doc,root,null);
		assertNotNull(xmlNode);

		collector=new StatisticsTimePerformanceIndicator(new String[]{"xml"});
		assertNull(collector.loadFromXML(xmlNode));
		assertEquals(40,collector.getSum());
		assertEquals(1,collector.getTimeMin());
		assertEquals(2,collector.getTimeMax());
	}
}
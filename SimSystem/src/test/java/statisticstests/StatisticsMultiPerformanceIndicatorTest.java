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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import statistics.StatisticsMultiPerformanceIndicator;
import statistics.StatisticsSimpleCountPerformanceIndicator;

/**
 * Testet die Funktionsweise von {@link StatisticsMultiPerformanceIndicator}
 * @author Alexander Herzog
 * @see StatisticsMultiPerformanceIndicator
 */
class StatisticsMultiPerformanceIndicatorTest {
	/**
	 * Test: Konstruktor-Varianten
	 */
	@Test
	void constructorTest() {
		StatisticsMultiPerformanceIndicator collector;

		collector=new StatisticsMultiPerformanceIndicator(new String[]{"xml"},new StatisticsSimpleCountPerformanceIndicator(new String[]{"xmlSub"}));
		assertEquals(0,collector.size());
	}

	/**
	 * Test: Objekte zusammenführen
	 */
	@Test
	void addTest() {
		StatisticsMultiPerformanceIndicator collector;
		StatisticsSimpleCountPerformanceIndicator simple1, simple2;

		collector=new StatisticsMultiPerformanceIndicator(new String[]{"xml"},new StatisticsSimpleCountPerformanceIndicator(new String[]{"xmlSub"}));

		simple1=(StatisticsSimpleCountPerformanceIndicator)collector.get("A");
		simple2=(StatisticsSimpleCountPerformanceIndicator)collector.get("B");

		assertArrayEquals(new String[]{"A","B"},collector.getNames());
		assertSame(simple1,collector.get("A"));
		assertSame(simple2,collector.get("B"));
		assertArrayEquals(new StatisticsSimpleCountPerformanceIndicator[]{simple1,simple2},collector.getAll());
	}


	/**
	 * Test: Objekte zusammenführen (und Werte später auslesen)
	 */
	@Test
	void addTest2() {
		StatisticsMultiPerformanceIndicator collector1, collector2;
		StatisticsSimpleCountPerformanceIndicator simple1, simple2;

		collector1=new StatisticsMultiPerformanceIndicator(new String[]{"xml"},new StatisticsSimpleCountPerformanceIndicator(new String[]{"xmlSub"}));
		simple1=(StatisticsSimpleCountPerformanceIndicator)collector1.get("A");
		simple1.add(); simple1.add(); simple1.add();
		collector2=new StatisticsMultiPerformanceIndicator(new String[]{"xml"},new StatisticsSimpleCountPerformanceIndicator(new String[]{"xmlSub"}));
		simple2=(StatisticsSimpleCountPerformanceIndicator)collector2.get("B");
		simple2.add(); simple2.add(); simple2.add(); simple2.add(); simple2.add();

		collector1.add(collector2);

		assertArrayEquals(new String[]{"A","B"},collector1.getNames());
		assertEquals(3.0,((StatisticsSimpleCountPerformanceIndicator)collector1.get("A")).get());
		assertEquals(5.0,((StatisticsSimpleCountPerformanceIndicator)collector1.get("B")).get());
	}

	/**
	 * Test: Daten auslesen
	 */
	@Test
	void calcTest() {
		StatisticsMultiPerformanceIndicator collector;
		StatisticsSimpleCountPerformanceIndicator simple1, simple2;

		collector=new StatisticsMultiPerformanceIndicator(new String[]{"xml"},new StatisticsSimpleCountPerformanceIndicator(new String[]{"xmlSub"}));
		simple1=(StatisticsSimpleCountPerformanceIndicator)collector.get("A");
		simple1.add(); simple1.add(); simple1.add();

		simple2=(StatisticsSimpleCountPerformanceIndicator)collector.get("B");
		simple2.add(); simple2.add(); simple2.add(); simple2.add(); simple2.add();

		assertFalse(collector.contains(new StatisticsSimpleCountPerformanceIndicator(new String[] {"xml"})));
		assertTrue(collector.contains(simple1));
		assertTrue(collector.contains(simple2));

		assertNull(collector.getName(new StatisticsSimpleCountPerformanceIndicator(new String[] {"xml"})));
		assertEquals("A",collector.getName(simple1));
		assertEquals("B",collector.getName(simple2));

		assertEquals(2,collector.getAll().length);
	}

	/**
	 * Test: Objekt kopieren
	 */
	@Test
	void cloneTest() {
		StatisticsMultiPerformanceIndicator collector;
		StatisticsSimpleCountPerformanceIndicator simple1, simple2;

		collector=new StatisticsMultiPerformanceIndicator(new String[]{"xml"},new StatisticsSimpleCountPerformanceIndicator(new String[]{"xmlSub"}));

		simple1=(StatisticsSimpleCountPerformanceIndicator)collector.get("A");
		simple1.add(); simple1.add(); simple1.add();
		simple2=(StatisticsSimpleCountPerformanceIndicator)collector.get("B");
		simple2.add(); simple2.add(); simple2.add(); simple2.add(); simple2.add();

		collector=collector.clone();

		assertArrayEquals(new String[]{"A","B"},collector.getNames());
		assertEquals(3.0,((StatisticsSimpleCountPerformanceIndicator)collector.get("A")).get());
		assertEquals(5.0,((StatisticsSimpleCountPerformanceIndicator)collector.get("B")).get());

		collector=collector.cloneEmpty();
		assertEquals(0,collector.getNames().length);
	}

	/**
	 * Test: Daten in xml-Dokument schreiben und aus diesem wieder laden
	 */
	@Test
	void loadTest() {
		StatisticsMultiPerformanceIndicator collector;
		StatisticsSimpleCountPerformanceIndicator simple1, simple2;

		collector=new StatisticsMultiPerformanceIndicator(new String[]{"xml"},new StatisticsSimpleCountPerformanceIndicator(new String[]{"xmlSub"}));

		simple1=(StatisticsSimpleCountPerformanceIndicator)collector.get("A");
		simple1.add(); simple1.add(); simple1.add();
		simple2=(StatisticsSimpleCountPerformanceIndicator)collector.get("B");
		simple2.add(); simple2.add(); simple2.add(); simple2.add(); simple2.add();

		final DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {db=dbf.newDocumentBuilder();} catch (ParserConfigurationException e) {assertTrue(false); return;}
		Document doc=db.newDocument();
		final Element root=doc.createElement("root"); doc.appendChild(root);

		Element xmlNode=collector.addToXML(doc,root,null);
		assertNotNull(xmlNode);

		collector=new StatisticsMultiPerformanceIndicator(new String[]{"xml"},new StatisticsSimpleCountPerformanceIndicator(new String[]{"xmlSub"}));
		assertNull(collector.loadFromXML(xmlNode));

		assertArrayEquals(new String[]{"A","B"},collector.getNames());
		assertEquals(3.0,((StatisticsSimpleCountPerformanceIndicator)collector.get("A")).get());
		assertEquals(5.0,((StatisticsSimpleCountPerformanceIndicator)collector.get("B")).get());
	}
}

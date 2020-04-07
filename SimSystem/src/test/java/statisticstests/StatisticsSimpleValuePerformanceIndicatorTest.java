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

import statistics.StatisticsSimpleValuePerformanceIndicator;

/**
 * Testet die Funktionsweise von {@link StatisticsSimpleValuePerformanceIndicator}
 * @author Alexander Herzog
 * @see StatisticsSimpleValuePerformanceIndicator
 */
public class StatisticsSimpleValuePerformanceIndicatorTest {
	/**
	 * Test: Konstruktor-Varianten
	 */
	@Test
	void constructorTest() {
		StatisticsSimpleValuePerformanceIndicator collector;

		collector=new StatisticsSimpleValuePerformanceIndicator(new String[]{"xml"});
		assertEquals(0,collector.get());
	}

	/**
	 * Test: Werte direkt hinzufügen, Objekte zusammenführen
	 */
	@Test
	void addTest() {
		StatisticsSimpleValuePerformanceIndicator collector1, collector2;

		collector1=new StatisticsSimpleValuePerformanceIndicator(new String[]{"xml"});
		collector1.set(123);
		assertEquals(123,collector1.get());

		collector2=new StatisticsSimpleValuePerformanceIndicator(new String[]{"xml"});
		collector2.set(5);
		collector1.add(collector2);
		assertEquals(128,collector1.get());
	}

	/**
	 * Test: Erfassung zurücksetzen/löschen
	 */
	@Test
	void resetTest() {
		StatisticsSimpleValuePerformanceIndicator collector;

		collector=new StatisticsSimpleValuePerformanceIndicator(new String[]{"xml"});
		collector.set(123);
		collector.reset();
		assertEquals(0,collector.get());
	}

	/**
	 * Test: Objekt kopieren
	 */
	@Test
	void cloneTest() {
		StatisticsSimpleValuePerformanceIndicator collector;

		collector=new StatisticsSimpleValuePerformanceIndicator(new String[]{"xml"});
		collector.set(123);
		collector=collector.clone();
		assertEquals(123,collector.get());

		collector=collector.cloneEmpty();
		assertEquals(0,collector.get());
	}

	/**
	 * Test: Daten in xml-Dokument schreiben und aus diesem wieder laden
	 */
	@Test
	void loadTest() {
		StatisticsSimpleValuePerformanceIndicator collector;

		collector=new StatisticsSimpleValuePerformanceIndicator(new String[]{"xml"});
		collector.set(123);

		final DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {db=dbf.newDocumentBuilder();} catch (ParserConfigurationException e) {assertTrue(false); return;}
		Document doc=db.newDocument();
		final Element root=doc.createElement("root"); doc.appendChild(root);

		Element xmlNode=collector.addToXML(doc,root);
		assertNotNull(xmlNode);

		collector=new StatisticsSimpleValuePerformanceIndicator(new String[]{"xml"});
		assertNull(collector.loadFromXML(xmlNode));
		assertEquals(123,collector.get());
	}
}

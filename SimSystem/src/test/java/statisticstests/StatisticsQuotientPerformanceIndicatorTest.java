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

import statistics.StatisticsQuotientPerformanceIndicator;

/**
 * Testet die Funktionsweise von {@link StatisticsQuotientPerformanceIndicator}
 * @author Alexander Herzog
 * @see StatisticsQuotientPerformanceIndicator
 */
class StatisticsQuotientPerformanceIndicatorTest {
	/**
	 * Test: Konstruktor-Varianten
	 */
	@Test
	void constructorTest() {
		StatisticsQuotientPerformanceIndicator collector;

		collector=new StatisticsQuotientPerformanceIndicator(new String[]{"xml"});
		assertEquals(0,collector.getNumerator());
		assertEquals(0,collector.getDenominator());
		assertEquals(0,collector.getQuotient());
	}

	/**
	 * Test: Werte direkt hinzufügen, Objekte zusammenführen
	 */
	@Test
	void addTest() {
		StatisticsQuotientPerformanceIndicator collector, collector2;

		/* Normale add-Funktion */

		collector=new StatisticsQuotientPerformanceIndicator(new String[]{"xml"});
		collector.set(3,5);
		assertEquals(3,collector.getNumerator());
		assertEquals(5,collector.getDenominator());
		assertEquals(3.0/5.0,collector.getQuotient());

		/* Weitere Collector-Objekte hinzufügen: null */

		collector=new StatisticsQuotientPerformanceIndicator(new String[]{"xml"});
		collector.set(3,5);
		collector.add(null);
		assertEquals(3,collector.getNumerator());
		assertEquals(5,collector.getDenominator());
		assertEquals(3.0/5.0,collector.getQuotient());

		/* Weitere Collector-Objekte hinzufügen: leer */

		collector=new StatisticsQuotientPerformanceIndicator(new String[]{"xml"});
		collector.set(3,5);
		collector.add(new StatisticsQuotientPerformanceIndicator(new String[]{"xml"}));
		assertEquals(3,collector.getNumerator());
		assertEquals(5,collector.getDenominator());
		assertEquals(3.0/5.0,collector.getQuotient());

		/* Weitere Collector-Objekte hinzufügen: weitere Daten */

		collector2=new StatisticsQuotientPerformanceIndicator(new String[]{"xml"});
		collector2.set(4,3);

		collector=new StatisticsQuotientPerformanceIndicator(new String[]{"xml"});
		collector.set(3,5);
		collector.add(collector2);
		assertEquals(7,collector.getNumerator());
		assertEquals(8,collector.getDenominator());
		assertEquals(7.0/8.0,collector.getQuotient());
	}

	/**
	 * Test: Erfassung zurücksetzen/löschen
	 */
	@Test
	void resetTest() {
		StatisticsQuotientPerformanceIndicator collector;

		collector=new StatisticsQuotientPerformanceIndicator(new String[]{"xml"});
		collector.set(3,5);
		collector.reset();
		assertEquals(0,collector.getNumerator());
		assertEquals(0,collector.getDenominator());
		assertEquals(0,collector.getQuotient());
	}

	/**
	 * Test: Objekt kopieren
	 */
	@Test
	void cloneTest() {
		StatisticsQuotientPerformanceIndicator collector;

		collector=new StatisticsQuotientPerformanceIndicator(new String[]{"xml"});
		collector.set(3,5);
		collector=collector.clone();
		assertEquals(3,collector.getNumerator());
		assertEquals(5,collector.getDenominator());
		assertEquals(3.0/5.0,collector.getQuotient());

		collector=collector.cloneEmpty();
		assertEquals(0,collector.getNumerator());
		assertEquals(0,collector.getDenominator());
		assertEquals(0,collector.getQuotient());

		collector=new StatisticsQuotientPerformanceIndicator(new String[]{"xml"});
		collector=collector.clone();
		assertEquals(0,collector.getNumerator());
		assertEquals(0,collector.getDenominator());
		assertEquals(0,collector.getQuotient());
	}

	/**
	 * Test: Daten in xml-Dokument schreiben und aus diesem wieder laden
	 */
	@Test
	void loadTest() {
		StatisticsQuotientPerformanceIndicator collector;

		collector=new StatisticsQuotientPerformanceIndicator(new String[]{"xml"});
		collector.set(3,5);

		final DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {db=dbf.newDocumentBuilder();} catch (ParserConfigurationException e) {assertTrue(false); return;}
		Document doc=db.newDocument();
		final Element root=doc.createElement("root"); doc.appendChild(root);

		Element xmlNode=collector.addToXML(doc,root,null);
		assertNotNull(xmlNode);

		collector=new StatisticsQuotientPerformanceIndicator(new String[]{"xml"});
		assertNull(collector.loadFromXML(xmlNode));
		assertEquals(3,collector.getNumerator());
		assertEquals(5,collector.getDenominator());
		assertEquals(3.0/5.0,collector.getQuotient());
	}
}

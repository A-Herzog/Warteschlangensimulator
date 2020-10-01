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

import statistics.StatisticsValuePerformanceIndicator;

/**
 * Testet die Funktionsweise von {@link StatisticsValuePerformanceIndicator}
 * @author Alexander Herzog
 * @see StatisticsValuePerformanceIndicator
 */
class StatisticsValuePerformanceIndicatorTest {
	/**
	 * Test: Konstruktor-Varianten
	 */
	@Test
	void constructorTest() {
		StatisticsValuePerformanceIndicator collector;

		collector=new StatisticsValuePerformanceIndicator(new String[]{"xml"});
		assertEquals(0,collector.getValue());
	}

	/**
	 * Test: Werte direkt hinzufügen, Objekte zusammenführen
	 */
	@Test
	void addTest() {
		StatisticsValuePerformanceIndicator collector, collector2;

		/* Normale add-Funktion */

		collector=new StatisticsValuePerformanceIndicator(new String[]{"xml"});
		collector.add(2);
		collector.add(3);
		collector.add(5);
		collector.add(7);
		collector.add(11);
		assertEquals(28,collector.getValue());

		/* Weitere Collector-Objekte hinzufügen: null */

		collector=new StatisticsValuePerformanceIndicator(new String[]{"xml"});
		collector.add(2);
		collector.add(3);
		collector.add(5);
		collector.add(7);
		collector.add(11);
		collector.add(null);
		assertEquals(28,collector.getValue());

		/* Weitere Collector-Objekte hinzufügen: leer */

		collector=new StatisticsValuePerformanceIndicator(new String[]{"xml"});
		collector.add(2);
		collector.add(3);
		collector.add(5);
		collector.add(7);
		collector.add(11);
		collector.add(new StatisticsValuePerformanceIndicator(new String[]{"xml"}));
		assertEquals(28,collector.getValue());

		/* Weitere Collector-Objekte hinzufügen: weitere Daten */

		collector2=new StatisticsValuePerformanceIndicator(new String[]{"xml"});
		collector2.add(1);

		collector=new StatisticsValuePerformanceIndicator(new String[]{"xml"});
		collector.add(2);
		collector.add(3);
		collector.add(5);
		collector.add(7);
		collector.add(11);
		collector.add(collector2);
		assertEquals(29,collector.getValue());
	}

	/**
	 * Test: Erfassung zurücksetzen/löschen
	 */
	@Test
	void resetTest() {
		StatisticsValuePerformanceIndicator collector;

		collector=new StatisticsValuePerformanceIndicator(new String[]{"xml"});
		collector.add(2);
		collector.add(3);
		collector.add(5);
		collector.add(7);
		collector.add(11);
		collector.reset();
		assertEquals(0,collector.getValue());
	}

	/**
	 * Test: Objekt kopieren
	 */
	@Test
	void cloneTest() {
		StatisticsValuePerformanceIndicator collector;

		collector=new StatisticsValuePerformanceIndicator(new String[]{"xml"});
		collector.add(2);
		collector.add(3);
		collector.add(5);
		collector.add(7);
		collector.add(11);
		collector=collector.clone();
		assertEquals(28,collector.getValue());

		collector=collector.cloneEmpty();
		assertEquals(0,collector.getValue());
	}

	/**
	 * Test: Daten in xml-Dokument schreiben und aus diesem wieder laden
	 */
	@Test
	void loadTest() {
		StatisticsValuePerformanceIndicator collector;

		collector=new StatisticsValuePerformanceIndicator(new String[]{"xml"});
		collector.add(2);
		collector.add(3);
		collector.add(5);
		collector.add(7);
		collector.add(11);

		final DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {db=dbf.newDocumentBuilder();} catch (ParserConfigurationException e) {assertTrue(false); return;}
		Document doc=db.newDocument();
		final Element root=doc.createElement("root"); doc.appendChild(root);

		Element xmlNode=collector.addToXML(doc,root,null);
		assertNotNull(xmlNode);

		collector=new StatisticsValuePerformanceIndicator(new String[]{"xml"});
		assertNull(collector.loadFromXML(xmlNode));
		assertEquals(28,collector.getValue());
	}
}
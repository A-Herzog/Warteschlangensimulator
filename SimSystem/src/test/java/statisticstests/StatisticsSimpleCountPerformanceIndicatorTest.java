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

import statistics.StatisticsSimpleCountPerformanceIndicator;

/**
 * Testet die Funktionsweise von {@link StatisticsSimpleCountPerformanceIndicator}
 * @author Alexander Herzog
 * @see StatisticsSimpleCountPerformanceIndicator
 */
public class StatisticsSimpleCountPerformanceIndicatorTest {
	/**
	 * Test: Konstruktor-Varianten
	 */
	@Test
	void constructorTest() {
		StatisticsSimpleCountPerformanceIndicator collector;

		collector=new StatisticsSimpleCountPerformanceIndicator(new String[]{"xml"});
		assertEquals(0,collector.get());
	}

	/**
	 * Test: Werte direkt hinzufügen, Objekte zusammenführen
	 */
	@Test
	void addTest() {
		StatisticsSimpleCountPerformanceIndicator collector, collector2;

		/* Normale add-Funktion */

		collector=new StatisticsSimpleCountPerformanceIndicator(new String[]{"xml"});
		collector.add();
		collector.add();
		collector.add();
		assertEquals(3,collector.get());

		/* Weitere Collector-Objekte hinzufügen: null */

		collector=new StatisticsSimpleCountPerformanceIndicator(new String[]{"xml"});
		collector.add();
		collector.add();
		collector.add();
		collector.add(null);
		assertEquals(3,collector.get());

		/* Weitere Collector-Objekte hinzufügen: leer */

		collector=new StatisticsSimpleCountPerformanceIndicator(new String[]{"xml"});
		collector.add();
		collector.add();
		collector.add();
		collector.add(new StatisticsSimpleCountPerformanceIndicator(new String[]{"xml"}));
		assertEquals(3,collector.get());

		/* Weitere Collector-Objekte hinzufügen: weitere Daten */

		collector2=new StatisticsSimpleCountPerformanceIndicator(new String[]{"xml"});
		collector2.add();

		collector=new StatisticsSimpleCountPerformanceIndicator(new String[]{"xml"});
		collector.add();
		collector.add();
		collector.add();
		collector.add(collector2);
		assertEquals(4,collector.get());
	}

	/**
	 * Test: Erfassung zurücksetzen/löschen
	 */
	@Test
	void resetTest() {
		StatisticsSimpleCountPerformanceIndicator collector;

		collector=new StatisticsSimpleCountPerformanceIndicator(new String[]{"xml"});
		collector.add();
		collector.add();
		collector.add();
		collector.reset();
		assertEquals(0,collector.get());
	}

	/**
	 * Test: Objekt kopieren
	 */
	@Test
	void cloneTest() {
		StatisticsSimpleCountPerformanceIndicator collector;

		collector=new StatisticsSimpleCountPerformanceIndicator(new String[]{"xml"});
		collector.add();
		collector.add();
		collector.add();
		collector=collector.clone();
		assertEquals(3,collector.get());

		collector=new StatisticsSimpleCountPerformanceIndicator(new String[]{"xml"});
		collector=collector.clone();
		assertEquals(0,collector.get());
	}

	/**
	 * Test: Daten in xml-Dokument schreiben und aus diesem wieder laden
	 */
	@Test
	void loadTest() {
		StatisticsSimpleCountPerformanceIndicator collector;

		collector=new StatisticsSimpleCountPerformanceIndicator(new String[]{"xml"});
		collector.add();
		collector.add();
		collector.add();

		final DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {db=dbf.newDocumentBuilder();} catch (ParserConfigurationException e) {assertTrue(false); return;}
		Document doc=db.newDocument();
		final Element root=doc.createElement("root"); doc.appendChild(root);

		Element xmlNode=collector.addToXML(doc,root,null);
		assertNotNull(xmlNode);

		collector=new StatisticsSimpleCountPerformanceIndicator(new String[]{"xml"});
		assertNull(collector.loadFromXML(xmlNode));
		assertEquals(3,collector.get());
	}
}
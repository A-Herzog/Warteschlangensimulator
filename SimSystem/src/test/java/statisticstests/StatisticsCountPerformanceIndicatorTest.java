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

import statistics.StatisticsCountPerformanceIndicator;

/**
 * Testet die Funktionsweise von {@link StatisticsCountPerformanceIndicator}
 * @author Alexander Herzog
 * @see StatisticsCountPerformanceIndicator
 */
public class StatisticsCountPerformanceIndicatorTest {
	/**
	 * Test: Konstruktor-Varianten
	 */
	@Test
	void constructorTest() {
		StatisticsCountPerformanceIndicator collector;

		collector=new StatisticsCountPerformanceIndicator(new String[]{"xml"});
		assertEquals(0,collector.getAll());
		assertEquals(0,collector.getSuccess());
		assertEquals(0,collector.getNoSuccess());
	}

	/**
	 * Test: Werte direkt hinzufügen, Objekte zusammenführen
	 */
	@Test
	void addTest() {
		StatisticsCountPerformanceIndicator collector, collector2;

		/* Normale add-Funktion */

		collector=new StatisticsCountPerformanceIndicator(new String[]{"xml"});
		collector.add(true);
		collector.add(true);
		collector.add(true);
		collector.add(false);
		collector.add(false);
		assertEquals(5,collector.getAll());
		assertEquals(3,collector.getSuccess());
		assertEquals(2,collector.getNoSuccess());

		/* Weitere Collector-Objekte hinzufügen: null */

		collector=new StatisticsCountPerformanceIndicator(new String[]{"xml"});
		collector.add(true);
		collector.add(true);
		collector.add(true);
		collector.add(false);
		collector.add(false);
		collector.add(null);
		assertEquals(5,collector.getAll());
		assertEquals(3,collector.getSuccess());
		assertEquals(2,collector.getNoSuccess());

		/* Weitere Collector-Objekte hinzufügen: leer */

		collector=new StatisticsCountPerformanceIndicator(new String[]{"xml"});
		collector.add(true);
		collector.add(true);
		collector.add(true);
		collector.add(false);
		collector.add(false);
		collector.add(new StatisticsCountPerformanceIndicator(new String[]{"xml"}));
		assertEquals(5,collector.getAll());
		assertEquals(3,collector.getSuccess());
		assertEquals(2,collector.getNoSuccess());

		/* Weitere Collector-Objekte hinzufügen: weitere Daten */

		collector2=new StatisticsCountPerformanceIndicator(new String[]{"xml"});
		collector2.add(true);
		collector2.add(false);

		collector=new StatisticsCountPerformanceIndicator(new String[]{"xml"});
		collector.add(true);
		collector.add(true);
		collector.add(false);
		collector.add(collector2);
		assertEquals(5,collector.getAll());
		assertEquals(3,collector.getSuccess());
		assertEquals(2,collector.getNoSuccess());
	}

	/**
	 * Test: Erfassung zurücksetzen/löschen
	 */
	@Test
	void resetTest() {
		StatisticsCountPerformanceIndicator collector;

		collector=new StatisticsCountPerformanceIndicator(new String[]{"xml"});
		collector.add(true);
		collector.add(true);
		collector.add(true);
		collector.add(false);
		collector.add(false);
		collector.reset();
		assertEquals(0,collector.getAll());
		assertEquals(0,collector.getSuccess());
		assertEquals(0,collector.getNoSuccess());
	}

	/**
	 * Test: Objekt kopieren
	 */
	@Test
	void cloneTest() {
		StatisticsCountPerformanceIndicator collector;

		collector=new StatisticsCountPerformanceIndicator(new String[]{"xml"});
		collector.add(true);
		collector.add(true);
		collector.add(true);
		collector.add(false);
		collector.add(false);
		collector=collector.clone();
		assertEquals(5,collector.getAll());
		assertEquals(3,collector.getSuccess());
		assertEquals(2,collector.getNoSuccess());

		collector=collector.cloneEmpty();
		assertEquals(0,collector.getAll());
		assertEquals(0,collector.getSuccess());
		assertEquals(0,collector.getNoSuccess());

		collector=new StatisticsCountPerformanceIndicator(new String[]{"xml"});
		collector=collector.clone();
		assertEquals(0,collector.getAll());
		assertEquals(0,collector.getSuccess());
		assertEquals(0,collector.getNoSuccess());
	}

	/**
	 * Test: Werte in die Erfassung laden und dann Kenngrößen auslesen
	 */
	@Test
	void calcTest() {
		StatisticsCountPerformanceIndicator collector;

		collector=new StatisticsCountPerformanceIndicator(new String[]{"xml"});
		collector.add(true);
		collector.add(true);
		collector.add(true);
		collector.add(false);
		collector.add(false);
		assertEquals(3.0/5.0,collector.getSuccessPart());
	}

	/**
	 * Test: Daten in xml-Dokument schreiben und aus diesem wieder laden
	 */
	@Test
	void loadTest() {
		StatisticsCountPerformanceIndicator collector;

		collector=new StatisticsCountPerformanceIndicator(new String[]{"xml"});
		collector.add(true);
		collector.add(true);
		collector.add(true);
		collector.add(false);
		collector.add(false);

		final DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {db=dbf.newDocumentBuilder();} catch (ParserConfigurationException e) {assertTrue(false); return;}
		Document doc=db.newDocument();
		final Element root=doc.createElement("root"); doc.appendChild(root);

		Element xmlNode=collector.addToXML(doc,root);
		assertNotNull(xmlNode);

		collector=new StatisticsCountPerformanceIndicator(new String[]{"xml"});
		assertNull(collector.loadFromXML(xmlNode));
		assertEquals(3.0/5.0,collector.getSuccessPart());
	}
}

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

import statistics.StatisticsTimeAnalogPerformanceIndicator;

/**
 * Testet die Funktionsweise von {@link StatisticsTimeAnalogPerformanceIndicator}
 * @author Alexander Herzog
 * @see StatisticsTimeAnalogPerformanceIndicator
 */
public class StatisticsTimeAnalogPerformanceIndicatorTest {
	/**
	 * Test: Konstruktor-Varianten
	 */
	@Test
	void constructorTest() {
		StatisticsTimeAnalogPerformanceIndicator collector;

		collector=new StatisticsTimeAnalogPerformanceIndicator(new String[]{"xml"});
		assertEquals(0,collector.getCurrentState());
		assertEquals(0,collector.getMin());
		assertEquals(0,collector.getMax());
		assertEquals(0,collector.getMean());
	}

	/**
	 * Test: Werte direkt hinzufügen, Objekte zusammenführen
	 */
	@Test
	void addTest() {
		StatisticsTimeAnalogPerformanceIndicator collector, collector2;

		/* Normale add-Funktion */

		collector=new StatisticsTimeAnalogPerformanceIndicator(new String[]{"xml"});
		collector.set(10,5);
		collector.set(20,7);
		collector.set(30,3);
		assertEquals(3,collector.getCurrentState());
		assertEquals(3,collector.getMin());
		assertEquals(7,collector.getMax());
		assertEquals(5.5,collector.getMean());

		/* Weitere Collector-Objekte hinzufügen: null */

		collector=new StatisticsTimeAnalogPerformanceIndicator(new String[]{"xml"});
		collector.set(10,5);
		collector.set(20,7);
		collector.set(30,3);
		collector.add(null);
		assertEquals(3,collector.getCurrentState());
		assertEquals(3,collector.getMin());
		assertEquals(7,collector.getMax());
		assertEquals(5.5,collector.getMean());

		/* Weitere Collector-Objekte hinzufügen: leer */

		collector=new StatisticsTimeAnalogPerformanceIndicator(new String[]{"xml"});
		collector.set(10,5);
		collector.set(20,7);
		collector.set(30,3);
		collector.add(new StatisticsTimeAnalogPerformanceIndicator(new String[]{"xml"}));
		assertEquals(3,collector.getCurrentState());
		assertEquals(0,collector.getMin());
		assertEquals(7,collector.getMax());

		/* Weitere Collector-Objekte hinzufügen: weitere Daten */

		collector2=new StatisticsTimeAnalogPerformanceIndicator(new String[]{"xml"});
		collector2.set(10,5);
		collector2.set(20,7);
		collector2.set(30,3);

		collector=new StatisticsTimeAnalogPerformanceIndicator(new String[]{"xml"});
		collector.set(10,5);
		collector.set(20,7);
		collector.set(30,3);
		collector.add(collector2);
		assertEquals(3,collector.getCurrentState());
		assertEquals(3,collector.getMin());
		assertEquals(7,collector.getMax());
		assertEquals(5.5,collector.getMean());
	}

	/**
	 * Test: Erfassung zurücksetzen/löschen
	 */
	@Test
	void resetTest() {
		StatisticsTimeAnalogPerformanceIndicator collector;

		collector=new StatisticsTimeAnalogPerformanceIndicator(new String[]{"xml"});
		collector.set(10,5);
		collector.set(20,7);
		collector.set(30,3);
		collector.reset();
		assertEquals(0,collector.getCurrentState());
		assertEquals(0,collector.getMin());
		assertEquals(0,collector.getMax());
		assertEquals(0,collector.getMean());
	}

	/**
	 * Test: Objekt kopieren
	 */
	@Test
	void cloneTest() {
		StatisticsTimeAnalogPerformanceIndicator collector;

		collector=new StatisticsTimeAnalogPerformanceIndicator(new String[]{"xml"});
		collector.set(10,5);
		collector.set(20,7);
		collector.set(30,3);
		collector=collector.clone();
		assertEquals(3,collector.getCurrentState());
		assertEquals(3,collector.getMin());
		assertEquals(7,collector.getMax());
		assertEquals(5.5,collector.getMean());

		collector=collector.cloneEmpty();
		assertEquals(0,collector.getCurrentState());
	}

	/**
	 * Test: Daten in xml-Dokument schreiben und aus diesem wieder laden
	 */
	@Test
	void loadTest() {
		StatisticsTimeAnalogPerformanceIndicator collector;

		collector=new StatisticsTimeAnalogPerformanceIndicator(new String[]{"xml"});
		collector.set(10,5);
		collector.set(20,7);
		collector.set(30,3);

		final DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {db=dbf.newDocumentBuilder();} catch (ParserConfigurationException e) {assertTrue(false); return;}
		Document doc=db.newDocument();
		final Element root=doc.createElement("root"); doc.appendChild(root);

		Element xmlNode=collector.addToXML(doc,root,null);
		assertNotNull(xmlNode);

		collector=new StatisticsTimeAnalogPerformanceIndicator(new String[]{"xml"});
		assertNull(collector.loadFromXML(xmlNode));
		assertEquals(3,collector.getMin());
		assertEquals(7,collector.getMax());
		assertEquals(5.5,collector.getMean());
	}
}

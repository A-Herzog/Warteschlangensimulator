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

import statistics.StatisticsStateTimePerformanceIndicator;

/**
 * Testet die Funktionsweise von {@link StatisticsStateTimePerformanceIndicator}
 * @author Alexander Herzog
 * @see StatisticsStateTimePerformanceIndicator
 */
public class StatisticsStateTimePerformanceIndicatorTest {
	/**
	 * Test: Konstruktor-Varianten
	 */
	@Test
	void constructorTest() {
		StatisticsStateTimePerformanceIndicator collector;

		collector=new StatisticsStateTimePerformanceIndicator(new String[]{"xml"});
		assertEquals(0,collector.getSum());
	}

	/**
	 * Test: Werte direkt hinzufügen, Objekte zusammenführen
	 */
	@Test
	void addTest() {
		StatisticsStateTimePerformanceIndicator collector, collector2;

		/* Normale add-Funktion */

		collector=new StatisticsStateTimePerformanceIndicator(new String[]{"xml"});
		collector.setTime(0);
		collector.set(20,"A");
		collector.set(40,"B");
		collector.set(60,"C");
		assertEquals("C",collector.getCurrentState());
		assertEquals(40,collector.getSum());
		assertEquals(2,collector.get().size());

		/* Weitere Collector-Objekte hinzufügen: null */

		collector=new StatisticsStateTimePerformanceIndicator(new String[]{"xml"});
		collector.setTime(0);
		collector.set(20,"A");
		collector.set(40,"B");
		collector.set(60,"C");
		collector.add(null);
		assertEquals("C",collector.getCurrentState());
		assertEquals(40,collector.getSum());
		assertEquals(2,collector.get().size());

		/* Weitere Collector-Objekte hinzufügen: leer */

		collector=new StatisticsStateTimePerformanceIndicator(new String[]{"xml"});
		collector.setTime(0);
		collector.set(20,"A");
		collector.set(40,"B");
		collector.set(60,"C");
		collector.add(new StatisticsStateTimePerformanceIndicator(new String[]{"xml"}));
		assertEquals("C",collector.getCurrentState());
		assertEquals(40,collector.getSum());
		assertEquals(2,collector.get().size());

		/* Weitere Collector-Objekte hinzufügen: weitere Daten */

		collector2=new StatisticsStateTimePerformanceIndicator(new String[]{"xml"});
		collector2.setTime(0);
		collector2.set(20,"A");
		collector2.set(40,"D");
		collector2.set(60,"C");

		collector=new StatisticsStateTimePerformanceIndicator(new String[]{"xml"});
		collector.setTime(0);
		collector.set(20,"A");
		collector.set(40,"B");
		collector.set(60,"C");
		collector.add(collector2);
		assertEquals("C",collector.getCurrentState());
		assertEquals(80,collector.getSum());
		assertEquals(3,collector.get().size());
	}

	/**
	 * Test: Verwendung von leeren Zustandsnamen
	 */
	@Test
	void emptyTest() {
		StatisticsStateTimePerformanceIndicator collector;

		collector=new StatisticsStateTimePerformanceIndicator(new String[]{"xml"});
		collector.setTime(0);
		collector.set(20,"A");
		collector.set(40,null);
		collector.set(60,"C");
		assertEquals("C",collector.getCurrentState());
		assertEquals(40,collector.getSum());
		assertEquals(2,collector.get().size());
	}

	/**
	 * Test: Erfassung zurücksetzen/löschen
	 */
	@Test
	void resetTest() {
		StatisticsStateTimePerformanceIndicator collector;

		collector=new StatisticsStateTimePerformanceIndicator(new String[]{"xml"});
		collector.setTime(0);
		collector.set(20,"A");
		collector.set(40,"B");
		collector.set(60,"C");
		collector.reset();
		assertEquals(0,collector.getSum());
		assertEquals(0,collector.get().size());
	}

	/**
	 * Test: Objekt kopieren
	 */
	@Test
	void cloneTest() {
		StatisticsStateTimePerformanceIndicator collector;

		collector=new StatisticsStateTimePerformanceIndicator(new String[]{"xml"});
		collector.setTime(0);
		collector.set(20,"A");
		collector.set(40,"B");
		collector.set(60,"C");
		collector=collector.clone();
		assertEquals("C",collector.getCurrentState());
		assertEquals(40,collector.getSum());
		assertEquals(2,collector.get().size());
	}

	/**
	 * Test: Daten in xml-Dokument schreiben und aus diesem wieder laden
	 */
	@Test
	void loadTest() {
		StatisticsStateTimePerformanceIndicator collector;

		collector=new StatisticsStateTimePerformanceIndicator(new String[]{"xml"});
		collector.setTime(0);
		collector.set(20,"A");
		collector.set(40,"B");
		collector.set(60,"C");

		final DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {db=dbf.newDocumentBuilder();} catch (ParserConfigurationException e) {assertTrue(false); return;}
		Document doc=db.newDocument();
		final Element root=doc.createElement("root"); doc.appendChild(root);

		Element xmlNode=collector.addToXML(doc,root);
		assertNotNull(xmlNode);

		collector=new StatisticsStateTimePerformanceIndicator(new String[]{"xml"});
		assertNull(collector.loadFromXML(xmlNode));
		assertEquals(40,collector.getSum());
		assertEquals(2,collector.get().size());
	}
}

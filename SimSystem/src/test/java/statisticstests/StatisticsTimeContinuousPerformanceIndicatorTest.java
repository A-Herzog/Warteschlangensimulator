/**
 * Copyright 2021 Alexander Herzog
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

import statistics.StatisticsTimeContinuousPerformanceIndicator;

/**
 * Testet die Funktionsweise von {@link StatisticsTimeContinuousPerformanceIndicator}
 * @author Alexander Herzog
 * @see StatisticsTimeContinuousPerformanceIndicator
 */
class StatisticsTimeContinuousPerformanceIndicatorTest {
	/**
	 * Test: Konstruktor-Varianten
	 */
	@Test
	void constructorTest() {
		StatisticsTimeContinuousPerformanceIndicator collector;

		collector=new StatisticsTimeContinuousPerformanceIndicator(new String[]{"xml"});
		assertEquals(0,collector.getSum());
		assertEquals(0,collector.getTimeMin());
		assertEquals(0,collector.getTimeMax());
	}

	/**
	 * Test: Werte direkt hinzufügen, Objekte zusammenführen
	 */
	@Test
	void addTest() {
		StatisticsTimeContinuousPerformanceIndicator collector, collector2;

		/* Normale add-Funktion */

		collector=new StatisticsTimeContinuousPerformanceIndicator(new String[]{"xml"});
		collector.setTime(0);
		collector.set(20,1);
		collector.set(40,2.5);
		collector.set(60,3);
		assertEquals(3,collector.getCurrentState());
		assertEquals(40,collector.getSum());
		assertEquals(1,collector.getTimeMin());
		assertEquals(2.5,collector.getTimeMax());

		/* Weitere Collector-Objekte hinzufügen: null */

		collector=new StatisticsTimeContinuousPerformanceIndicator(new String[]{"xml"});
		collector.setTime(0);
		collector.set(20,1);
		collector.set(40,2.5);
		collector.set(60,3);
		collector.add(null);
		assertEquals(3,collector.getCurrentState());
		assertEquals(40,collector.getSum());
		assertEquals(1,collector.getTimeMin());
		assertEquals(2.5,collector.getTimeMax());

		/* Weitere Collector-Objekte hinzufügen: leer */

		collector=new StatisticsTimeContinuousPerformanceIndicator(new String[]{"xml"});
		collector.setTime(0);
		collector.set(20,1);
		collector.set(40,2.5);
		collector.set(60,3);
		collector.add(new StatisticsTimeContinuousPerformanceIndicator(new String[]{"xml"}));
		assertEquals(3,collector.getCurrentState());
		assertEquals(40,collector.getSum());
		assertEquals(1,collector.getTimeMin());
		assertEquals(2.5,collector.getTimeMax());

		/* Weitere Collector-Objekte hinzufügen: weitere Daten */

		collector2=new StatisticsTimeContinuousPerformanceIndicator(new String[]{"xml"});
		collector2.setTime(0);
		collector2.set(20,1);
		collector2.set(40,2.5);
		collector2.set(60,3);

		collector=new StatisticsTimeContinuousPerformanceIndicator(new String[]{"xml"});
		collector.setTime(0);
		collector.set(20,1);
		collector.set(40,2);
		collector.set(60,3);
		collector.add(collector2);
		assertEquals(3,collector.getCurrentState());
		assertEquals(80,collector.getSum());
		assertEquals(1,collector.getTimeMin());
		assertEquals(2.5,collector.getTimeMax());
	}

	/**
	 * Test: Erfassung zurücksetzen/löschen
	 */
	@Test
	void resetTest() {
		StatisticsTimeContinuousPerformanceIndicator collector;

		collector=new StatisticsTimeContinuousPerformanceIndicator(new String[]{"xml"});
		collector.setTime(0);
		collector.set(20,1);
		collector.set(40,2.5);
		collector.set(60,3);
		collector.reset();
		assertEquals(0,collector.getSum());
	}

	/**
	 * Test: Objekt kopieren
	 */
	@Test
	void cloneTest() {
		StatisticsTimeContinuousPerformanceIndicator collector;

		collector=new StatisticsTimeContinuousPerformanceIndicator(new String[]{"xml"});
		collector.setTime(0);
		collector.set(20,1);
		collector.set(40,2.5);
		collector.set(60,3);
		collector=collector.clone();
		assertEquals(3,collector.getCurrentState());
		assertEquals(40,collector.getSum());
		assertEquals(1,collector.getTimeMin());
		assertEquals(2.5,collector.getTimeMax());
		assertEquals(0,collector.getTimeSk());
		assertTrue(collector.getTimeKurt()<0);

		collector=new StatisticsTimeContinuousPerformanceIndicator(new String[]{"xml"});
		collector.setTime(0);
		collector.set(20,1);
		collector.set(40,2.5);
		collector.set(60,3);
		collector=collector.cloneEmpty();
		assertEquals(0,collector.getSum());
	}

	/**
	 * Test: Werte in die Erfassung laden und dann Kenngrößen auslesen
	 */
	@Test
	void calcTest() {
		StatisticsTimeContinuousPerformanceIndicator collector;

		collector=new StatisticsTimeContinuousPerformanceIndicator(new String[]{"xml"});
		collector.setTime(0);
		collector.set(20,1);
		collector.set(40,2);
		collector.set(60,3);
		collector.set(80,2);

		assertEquals(2,collector.getCurrentState());
		assertEquals(60,collector.getSum());
		assertEquals(1,collector.getTimeMin());
		assertEquals(3,collector.getTimeMax());

		assertEquals(2,collector.getTimeMean());
		assertEquals(2,collector.getTimeMean()); /* Cache */
		final double d=collector.getTimeVar();
		assertEquals(Math.sqrt(d),collector.getTimeSD());
		assertEquals(Math.sqrt(d)/2,collector.getTimeCV());
	}

	/**
	 * Test: Daten in xml-Dokument schreiben und aus diesem wieder laden
	 */
	@Test
	void loadTest() {
		StatisticsTimeContinuousPerformanceIndicator collector;

		collector=new StatisticsTimeContinuousPerformanceIndicator(new String[]{"xml"});
		collector.setTime(0);
		collector.set(20,1);
		collector.set(40,2.5);
		collector.set(60,3);

		final DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {db=dbf.newDocumentBuilder();} catch (ParserConfigurationException e) {assertTrue(false); return;}
		Document doc=db.newDocument();
		final Element root=doc.createElement("root"); doc.appendChild(root);

		Element xmlNode=collector.addToXML(doc,root,null);
		assertNotNull(xmlNode);

		collector=new StatisticsTimeContinuousPerformanceIndicator(new String[]{"xml"});
		assertNull(collector.loadFromXML(xmlNode));
		assertEquals(40,collector.getSum());
		assertEquals(1,collector.getTimeMin());
		assertEquals(2.5,collector.getTimeMax());
	}
}
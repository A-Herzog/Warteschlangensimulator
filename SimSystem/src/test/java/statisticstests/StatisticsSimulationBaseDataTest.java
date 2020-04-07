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

import statistics.StatisticsSimulationBaseData;

/**
 * Testet die Funktionsweise von {@link StatisticsSimulationBaseData}
 * @author Alexander Herzog
 * @see StatisticsSimulationBaseData
 */
public class StatisticsSimulationBaseDataTest {
	/**
	 * Test: Konstruktor-Varianten
	 */
	@Test
	void constructorTest() {
		StatisticsSimulationBaseData collector;

		collector=new StatisticsSimulationBaseData(new String[]{"xml"});
		assertEquals(0,collector.runTime);
	}

	/**
	 * Test: Objekte zusammenführen
	 */
	@Test
	void addTest() {
		StatisticsSimulationBaseData collector, collector2;

		collector=new StatisticsSimulationBaseData(new String[]{"xml"});
		collector.runEvents=5;
		collector.add(null);
		assertEquals(5,collector.runEvents);

		collector=new StatisticsSimulationBaseData(new String[]{"xml"});
		collector.runEvents=7;
		collector.emergencyShutDown=true;
		collector.warnings=new String[]{"Test1"};
		collector2=new StatisticsSimulationBaseData(new String[]{"xml"});
		collector2.runEvents=5;
		collector2.warnings=new String[]{"Test2"};
		collector.add(collector2);
		assertEquals(12,collector.runEvents);
		assertTrue(collector.emergencyShutDown);
		assertArrayEquals(new String[]{"Test1","Test2"},collector.warnings);
	}

	/**
	 * Test: Objekt kopieren
	 */
	@Test
	void cloneTest() {
		StatisticsSimulationBaseData collector;

		collector=new StatisticsSimulationBaseData(new String[]{"xml"});
		collector.runEvents=5;
		collector.emergencyShutDown=true;
		collector.warnings=new String[]{"Test"};
		collector=collector.clone();
		assertEquals(5,collector.runEvents);
		assertTrue(collector.emergencyShutDown);
		assertArrayEquals(new String[]{"Test"},collector.warnings);

		collector=new StatisticsSimulationBaseData(new String[]{"xml"});
		collector.runEvents=5;
		collector=collector.cloneEmpty();
		assertEquals(0,collector.runEvents);
	}

	/**
	 * Test: Werte in die Erfassung laden und dann Kenngrößen auslesen
	 */
	@Test
	void calcTest() {
		StatisticsSimulationBaseData collector;

		collector=new StatisticsSimulationBaseData(new String[]{"xml"});
		collector.runEvents=5;
		collector.runTime=2;
		collector.addWarning("Warning1");
		collector.addWarning("Warning2");
		assertEquals(5.0/2.0*1000,collector.getEventsPerSec());
		assertArrayEquals(new String[] {"Warning1","Warning2"},collector.warnings);
	}

	/**
	 * Test: Daten in xml-Dokument schreiben und aus diesem wieder laden
	 */
	@Test
	void loadTest() {
		StatisticsSimulationBaseData collector;

		collector=new StatisticsSimulationBaseData(new String[]{"xml"});
		collector.runEvents=5;
		collector.runThreads=1;
		collector.add(null);

		final DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {db=dbf.newDocumentBuilder();} catch (ParserConfigurationException e) {assertTrue(false); return;}
		Document doc=db.newDocument();
		final Element root=doc.createElement("root"); doc.appendChild(root);

		Element xmlNode=collector.addToXML(doc,root);
		assertNotNull(xmlNode);

		collector=new StatisticsSimulationBaseData(new String[]{"xml"});
		assertNull(collector.loadFromXML(xmlNode));
		assertEquals(5,collector.runEvents);
		assertEquals(1,collector.runThreads);
	}
}

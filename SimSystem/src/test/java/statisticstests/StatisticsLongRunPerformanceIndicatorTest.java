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

import statistics.StatisticsLongRunPerformanceIndicator;

/**
 * Testet die Funktionsweise von {@link StatisticsLongRunPerformanceIndicator}
 * @author Alexander Herzog
 * @see StatisticsLongRunPerformanceIndicator
 */
public class StatisticsLongRunPerformanceIndicatorTest {
	/**
	 * Test: Konstruktor-Varianten
	 */
	@Test
	void constructorTest() {
		StatisticsLongRunPerformanceIndicator collector;

		collector=new StatisticsLongRunPerformanceIndicator(new String[]{"xml"});
		assertEquals(0,collector.getValueCount());
	}

	/**
	 * Test: Werte direkt hinzufügen, Objekte zusammenführen
	 */
	@Test
	void addTest() {
		StatisticsLongRunPerformanceIndicator collector, collector2;

		/* Normale add-Funktion */

		collector=new StatisticsLongRunPerformanceIndicator(new String[]{"xml"});
		collector.init(10,StatisticsLongRunPerformanceIndicator.Mode.MODE_MAX);
		collector.setTime(0);
		collector.set(20,5);
		collector.set(40,0);
		collector.set(60,15);
		assertEquals(6,collector.getValueCount());

		/* Weitere Collector-Objekte hinzufügen: null */

		collector=new StatisticsLongRunPerformanceIndicator(new String[]{"xml"});
		collector.init(10,StatisticsLongRunPerformanceIndicator.Mode.MODE_MAX);
		collector.setTime(0);
		collector.set(20,5);
		collector.set(40,0);
		collector.set(60,15);
		collector.add(null);
		assertEquals(6,collector.getValueCount());

		/* Weitere Collector-Objekte hinzufügen: leer */

		collector=new StatisticsLongRunPerformanceIndicator(new String[]{"xml"});
		collector.init(10,StatisticsLongRunPerformanceIndicator.Mode.MODE_MAX);
		collector.setTime(0);
		collector.set(20,5);
		collector.set(40,0);
		collector.set(60,15);
		collector.add(new StatisticsLongRunPerformanceIndicator(new String[]{"xml"}));
		assertEquals(6,collector.getValueCount());

		/* Weitere Collector-Objekte hinzufügen: weitere Daten in leeren Collector */

		collector2=new StatisticsLongRunPerformanceIndicator(new String[]{"xml"});
		collector2.init(10,StatisticsLongRunPerformanceIndicator.Mode.MODE_MAX);
		collector2.setTime(0);
		collector2.set(20,5);
		collector2.set(40,0);
		collector2.set(60,15);
		collector=new StatisticsLongRunPerformanceIndicator(new String[]{"xml"});
		collector.add(collector2);
		assertEquals(6,collector.getValueCount());
	}

	/**
	 * Test: Erfassung zurücksetzen/löschen
	 */
	@Test
	void resetTest() {
		StatisticsLongRunPerformanceIndicator collector;

		collector=new StatisticsLongRunPerformanceIndicator(new String[]{"xml"});
		collector.init(10,StatisticsLongRunPerformanceIndicator.Mode.MODE_MAX);
		collector.setTime(0);
		collector.set(20,5);
		collector.set(40,0);
		collector.set(60,15);
		collector.reset();
		assertEquals(0,collector.getValueCount());
	}

	/**
	 * Test: Objekt kopieren
	 */
	@Test
	void cloneTest() {
		StatisticsLongRunPerformanceIndicator collector;

		collector=new StatisticsLongRunPerformanceIndicator(new String[]{"xml"});
		collector.init(10,StatisticsLongRunPerformanceIndicator.Mode.MODE_MAX);
		collector.setTime(0);
		collector.set(20,5);
		collector.set(40,0);
		collector.set(60,15);
		collector=collector.clone();
		assertEquals(6,collector.getValueCount());

		collector=new StatisticsLongRunPerformanceIndicator(new String[]{"xml"});
		collector.init(10,StatisticsLongRunPerformanceIndicator.Mode.MODE_MAX);
		collector.setTime(0);
		collector.set(20,5);
		collector=collector.cloneEmpty();
		assertEquals(0,collector.getValueCount());
	}

	/**
	 * Test: Werte in die Erfassung laden und dann wieder abrufen
	 */
	@Test
	void valueTest() {
		StatisticsLongRunPerformanceIndicator collector;

		collector=new StatisticsLongRunPerformanceIndicator(new String[]{"xml"});
		collector.init(10,StatisticsLongRunPerformanceIndicator.Mode.MODE_MAX);
		collector.setTime(0);
		collector.set(20,5);
		collector.set(40,0);
		collector.set(60,15);

		assertEquals(0,collector.getValue(-1));
		assertEquals(0,collector.getValue(100));

		assertEquals(0,collector.getValue(0));
		assertEquals(0,collector.getValue(1));
		assertEquals(5,collector.getValue(2));
		assertEquals(5,collector.getValue(3));
		assertEquals(0,collector.getValue(4));
		assertEquals(0,collector.getValue(5));

		collector=new StatisticsLongRunPerformanceIndicator(new String[]{"xml"});
		collector.init(10,StatisticsLongRunPerformanceIndicator.Mode.MODE_MAX);
		collector.setData(new double[]{1,2,3});
		assertEquals(1,collector.getValue(0));
		assertEquals(2,collector.getValue(1));
		assertEquals(3,collector.getValue(2));
		assertArrayEquals(new double[] {1,2,3},collector.getValues());
	}

	/**
	 * Test: Werte in die Erfassung laden und dann Kenngrößen auslesen
	 */
	@Test
	void calcTest() {
		StatisticsLongRunPerformanceIndicator collector;

		collector=new StatisticsLongRunPerformanceIndicator(new String[]{"xml"});
		collector.init(10,StatisticsLongRunPerformanceIndicator.Mode.MODE_MAX);
		collector.setTime(0);
		collector.set(20,5);
		collector.set(40,0);
		collector.set(60,15);
		assertArrayEquals(new double[]{0,0,5,5,0,0},collector.getDistribution().densityData,0.00001);

		collector=new StatisticsLongRunPerformanceIndicator(new String[]{"xml"});
		collector.init(10,StatisticsLongRunPerformanceIndicator.Mode.MODE_MIN);
		collector.setTime(0);
		collector.set(20,5);
		collector.set(40,0);
		collector.set(60,15);
		assertArrayEquals(new double[]{0,0,5,5,0,0},collector.getDistribution().densityData,0.00001);

		collector=new StatisticsLongRunPerformanceIndicator(new String[]{"xml"});
		collector.init(10,StatisticsLongRunPerformanceIndicator.Mode.MODE_AVERAGE);
		collector.setTime(0);
		collector.set(20,5);
		collector.set(40,0);
		collector.set(60,15);
		assertArrayEquals(new double[]{0,0,5,5,0,0},collector.getDistribution().densityData,0.00001);
		assertEquals(0,collector.getMin(),0.00001);
		assertEquals(5,collector.getMax(),0.00001);
		assertEquals(6,collector.getValueCount(),0.00001);
	}

	/**
	 * Test: Daten in xml-Dokument schreiben und aus diesem wieder laden
	 */
	@Test
	void loadTest() {
		StatisticsLongRunPerformanceIndicator collector;

		collector=new StatisticsLongRunPerformanceIndicator(new String[]{"xml"});
		collector.init(10,StatisticsLongRunPerformanceIndicator.Mode.MODE_MAX);
		collector.setTime(0);
		collector.set(20,5);
		collector.set(40,0);
		collector.set(60,15);
		assertArrayEquals(new double[]{0,0,5,5,0,0},collector.getDistribution().densityData,0.00001);

		final DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {db=dbf.newDocumentBuilder();} catch (ParserConfigurationException e) {assertTrue(false); return;}
		Document doc=db.newDocument();
		final Element root=doc.createElement("root"); doc.appendChild(root);

		Element xmlNode=collector.addToXML(doc,root);
		assertNotNull(xmlNode);

		collector=new StatisticsLongRunPerformanceIndicator(new String[]{"xml"});
		assertNull(collector.loadFromXML(xmlNode));
		assertArrayEquals(new double[]{0,0,5,5,0,0},collector.getDistribution().densityData,0.00001);
	}
}

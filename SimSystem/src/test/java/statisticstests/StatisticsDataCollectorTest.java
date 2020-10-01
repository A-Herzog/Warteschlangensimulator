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

import statistics.StatisticsDataCollector;

/**
 * Testet die Funktionsweise von {@link StatisticsDataCollector}
 * @author Alexander Herzog
 * @see StatisticsDataCollector
 */
class StatisticsDataCollectorTest {
	/**
	 * Test: Konstruktor-Varianten
	 */
	@Test
	void constructorTest() {
		StatisticsDataCollector collector;

		collector=new StatisticsDataCollector(new String[]{"xml"});
		assertEquals(0,collector.getCount());
		assertNotNull(collector.getValues());
		assertEquals(0,collector.getValues().length);
		assertNotNull(collector.getValuesReadOnly());
		assertEquals(0,collector.getValuesReadOnly().length);
		assertNotNull(collector.getCenteredValues());
		assertEquals(0,collector.getCenteredValues().length);
		assertEquals(0,collector.getValue(0));

		collector=new StatisticsDataCollector(new String[]{"xml"},new double[]{1,2,3});
		assertEquals(3,collector.getCount());
		assertArrayEquals(new double[]{1,2,3},collector.getValues(),0.00001);
	}

	/**
	 * Test: Werte direkt hinzufügen, Objekte zusammenführen
	 */
	@Test
	void addTest() {
		StatisticsDataCollector collector;

		/* Normale add-Funktion */

		collector=new StatisticsDataCollector(new String[]{"xml"});
		collector.add(1);
		collector.add(2);
		collector.add(3);
		assertEquals(3,collector.getCount());
		assertArrayEquals(new double[]{1,2,3},collector.getValues(),0.00001);

		/* Weitere Collector-Objekte hinzufügen: null */

		collector=new StatisticsDataCollector(new String[]{"xml"});
		collector.add(1);
		collector.add(2);
		collector.add(3);
		collector.add(null);
		assertEquals(3,collector.getCount());
		assertArrayEquals(new double[]{1,2,3},collector.getValues(),0.00001);

		/* Weitere Collector-Objekte hinzufügen: leer */

		collector=new StatisticsDataCollector(new String[]{"xml"});
		collector.add(1);
		collector.add(2);
		collector.add(3);
		collector.add(new StatisticsDataCollector(new String[]{"xml"}));
		assertEquals(3,collector.getCount());
		assertArrayEquals(new double[]{1,2,3},collector.getValues(),0.00001);

		/* Weitere Collector-Objekte hinzufügen: weitere Daten */

		collector=new StatisticsDataCollector(new String[]{"xml"});
		collector.add(1);
		collector.add(new StatisticsDataCollector(new String[]{"xml"},new double[]{2,3}));
		assertEquals(3,collector.getCount());
		assertArrayEquals(new double[]{1,2,3},collector.getValues(),0.00001);

		/* Weitere Collector-Objekte hinzufügen: weitere Daten */

		collector=new StatisticsDataCollector(new String[]{"xml"});
		collector.add(new StatisticsDataCollector(new String[]{"xml"},new double[]{1,2,3}));
		assertEquals(3,collector.getCount());
		assertArrayEquals(new double[]{1,2,3},collector.getValues(),0.00001);

		/* Array vergrößern */

		collector=new StatisticsDataCollector(new String[]{"xml"});
		for (int i=1;i<=12_000;i++) collector.add(i);
		assertEquals(12_000,collector.getCount());
		final double[] values=collector.getValues();
		for (int i=1;i<=12_000;i++) assertEquals(i,values[i-1]);
	}

	/**
	 * Test: Erfassung zurücksetzen/löschen
	 */
	@Test
	void resetTest() {
		StatisticsDataCollector collector;

		collector=new StatisticsDataCollector(new String[]{"xml"},new double[]{1,2,3});
		collector.reset();
		assertEquals(0,collector.getCount());
		assertNotNull(collector.getValues());
		assertEquals(0,collector.getValues().length);
	}

	/**
	 * Test: Objekt kopieren
	 */
	@Test
	void cloneTest() {
		StatisticsDataCollector collector;

		collector=new StatisticsDataCollector(new String[]{"xml"});
		collector=collector.clone();
		assertEquals(0,collector.getCount());
		assertNotNull(collector.getValues());
		assertEquals(0,collector.getValues().length);

		collector=new StatisticsDataCollector(new String[]{"xml"},new double[]{1,2,3});
		collector=collector.clone();
		assertEquals(3,collector.getCount());
		assertArrayEquals(new double[]{1,2,3},collector.getValues(),0.00001);
	}

	/**
	 * Test: Abrufen der erfassten Werte
	 */
	@Test
	void valuesTest() {
		StatisticsDataCollector collector;

		collector=new StatisticsDataCollector(new String[]{"xml"},new double[]{1,2,3,4,5});

		assertEquals(5,collector.getCount());
		assertArrayEquals(new double[]{1,2,3,4,5},collector.getValues(),0.00001);
		assertArrayEquals(new double[]{1,2,3,4,5},collector.getValuesReadOnly(),0.00001);
		assertArrayEquals(new double[]{-2,-1,0,1,2},collector.getCenteredValues(),0.00001);
		assertEquals(0,collector.getValue(-1));
		for (int i=0;i<5;i++) assertEquals(i+1,collector.getValue(i));
		assertEquals(0,collector.getValue(5));
	}

	/**
	 * Test: Werte in die Erfassung laden und dann Kenngrößen auslesen
	 */
	@Test
	void calcTest() {
		StatisticsDataCollector collector;

		/* Summe und Teilsumme */

		collector=new StatisticsDataCollector(new String[]{"xml"});
		assertEquals(0,collector.getSum());
		assertEquals(0,collector.getSum(0,5));

		collector=new StatisticsDataCollector(new String[]{"xml"},new double[]{1,2,3,4,5});
		assertEquals(15,collector.getSum());
		assertEquals(15,collector.getSum());

		assertEquals(15,collector.getSum(0,4));
		assertEquals(15,collector.getSum(0,4));
		assertEquals(9,collector.getSum(1,3));
		assertEquals(14,collector.getSum(1,4));

		collector=new StatisticsDataCollector(new String[]{"xml"},new double[]{1,2,3,4,5});
		assertEquals(15,collector.getSum(0,4));

		/* Gewichtete Summe */

		collector=new StatisticsDataCollector(new String[]{"xml"});
		assertEquals(0,collector.getWeightedSum(i->(double)i));

		collector=new StatisticsDataCollector(new String[]{"xml"},new double[]{1,2,3,4,5});
		assertEquals(0,collector.getWeightedSum(null));
		assertEquals(40,collector.getWeightedSum(i->(double)i));

		collector=new StatisticsDataCollector(new String[]{"xml"});
		assertEquals(0,collector.getWeightedSum(1,3,i->(double)i));

		collector=new StatisticsDataCollector(new String[]{"xml"},new double[]{1,2,3,4,5});
		assertEquals(0,collector.getWeightedSum(1,3,null));
		assertEquals(20,collector.getWeightedSum(1,3,i->(double)i));

		/* Verarbeitete Summe */

		collector=new StatisticsDataCollector(new String[]{"xml"});
		assertEquals(0,collector.getProcessedSum((i,d)->i*d));

		collector=new StatisticsDataCollector(new String[]{"xml"},new double[]{1,2,3,4,5});
		assertEquals(0,collector.getProcessedSum(null));
		assertEquals(40,collector.getProcessedSum((i,d)->i*d));

		collector=new StatisticsDataCollector(new String[]{"xml"});
		assertEquals(0,collector.getProcessedSum(1,3,(i,d)->i*d));

		collector=new StatisticsDataCollector(new String[]{"xml"},new double[]{1,2,3,4,5});
		assertEquals(0,collector.getProcessedSum(1,3,null));
		assertEquals(20,collector.getProcessedSum(1,3,(i,d)->i*d));

		/* Mittelwert */

		collector=new StatisticsDataCollector(new String[]{"xml"});
		assertEquals(0,collector.getMean());

		collector=new StatisticsDataCollector(new String[]{"xml"},new double[]{1,2,3,4,5});
		assertEquals(3,collector.getMean());
		assertEquals(3,collector.getMean());

		/* Teil-Mittelwert */

		collector=new StatisticsDataCollector(new String[]{"xml"});
		assertEquals(0,collector.getMean(1,3));

		collector=new StatisticsDataCollector(new String[]{"xml"},new double[]{1,2,3,4,5});
		assertEquals(3,collector.getMean(1,3));
		assertEquals(3,collector.getMean(1,3));

		/* Teilmessreihe */

		collector=new StatisticsDataCollector(new String[]{"xml"},new double[]{1,2,3,4,5});
		collector=collector.getPart(1,3);
		assertEquals(3,collector.getCount());
		assertArrayEquals(new double[]{2,3,4},collector.getValues(),0.00001);

		collector=new StatisticsDataCollector(new String[]{"xml"},new double[]{1,2,3,4,5});
		collector=collector.getPart(3,1);
		assertEquals(0,collector.getCount());
		assertNotNull(collector.getValues());
	}

	/**
	 * Test: Daten in xml-Dokument schreiben und aus diesem wieder laden
	 */
	@Test
	void loadTest() {
		StatisticsDataCollector collector;

		collector=new StatisticsDataCollector(new String[]{"xml"},new double[]{1,2,3});

		final DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {db=dbf.newDocumentBuilder();} catch (ParserConfigurationException e) {assertTrue(false); return;}
		Document doc=db.newDocument();
		final Element root=doc.createElement("root"); doc.appendChild(root);

		Element xmlNode=collector.addToXML(doc,root,null);
		assertNotNull(xmlNode);

		collector=new StatisticsDataCollector(new String[]{"xml"});
		assertNull(collector.loadFromXML(xmlNode));
		assertEquals(3,collector.getCount());
		assertArrayEquals(new double[]{1,2,3},collector.getValues(),0.00001);
	}
}
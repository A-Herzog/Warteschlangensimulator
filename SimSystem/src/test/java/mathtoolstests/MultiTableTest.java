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
package mathtoolstests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.Test;

import mathtools.MultiTable;
import mathtools.Table;
import mathtools.TableXLSXReader;

/**
 * Prüft die Funktionsweise von {@link MultiTable}
 * @author Alexander Herzog
 * @see MultiTable
 */
class MultiTableTest {
	/**
	 * Konstruktor der Klasse
	 */
	public MultiTableTest() {

		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	/**
	 * Einfache Tests zum Erstellen von Mehrfachtabellen und wieder Auslesen der einzelnen Blätter
	 */
	@Test
	void basicTests() {
		MultiTable multi;
		Table table, table2;

		multi=new MultiTable();
		assertNotNull(table=multi.add("Test"));

		assertEquals(1,multi.size());

		assertSame(multi.get(0),table);
		assertNull(multi.get(-1));
		assertNull(multi.get(1));
		assertSame(multi.get("Test"),table);
		assertNull(multi.get("abc"));

		assertEquals("Test",multi.getName(0));
		assertNull(multi.getName(-1));
		assertNull(multi.getName(1));

		final List<Table> content=multi.getTables();
		assertNotNull(content);
		assertEquals(1,content.size());
		assertEquals(table,content.get(0));

		final List<String> list=multi.getNames();
		assertNotNull(list);
		assertEquals(1,list.size());
		assertEquals("Test",list.get(0));

		assertEquals(-1,multi.indexOf(null));
		assertEquals(0,multi.indexOf("")); /* Kein Name==erste Tabelle */
		assertEquals(-1,new MultiTable().indexOf("")); /* ... aber -1, wenn es keine erste Tabelle gibt */
		assertEquals(0,multi.indexOf("Test"));
		assertEquals(-1,multi.indexOf("abc"));
		assertEquals(-1,multi.indexOf("0"));
		assertEquals(0,multi.indexOf("1"));
		assertEquals(-1,multi.indexOf("2"));

		table2=new Table();
		assertTrue(!multi.set(-1,table2));
		assertTrue(multi.set(0,table2));
		assertTrue(!multi.set(1,table2));
		assertSame(multi.get(0),table2);

		assertTrue(!multi.set(null,table));
		assertTrue(multi.set("Test",table));
		assertTrue(!multi.set("abc",table));
		assertSame(multi.get(0),table);

		assertTrue(!multi.remove(-1));
		assertTrue(!multi.remove(1));
		assertTrue(multi.remove(0));
		assertTrue(!multi.remove(0));
		assertEquals(0,multi.size());

		multi.add("Test",table);
		assertTrue(!multi.remove(null));
		assertTrue(!multi.remove("abc"));
		assertTrue(multi.remove("Test"));
		assertTrue(!multi.remove("Test"));
		assertEquals(0,multi.size());
	}

	/**
	 * Komplexe Tests (Daten auf komplexe Weise auslesen)
	 */
	@Test
	void advancedTests() {
		MultiTable multi;
		Table table;
		String[] line;
		double[] numbers;
		double[][] twolines;

		multi=new MultiTable();
		assertNotNull(table=multi.add("Test"));
		table.addLine(new String[]{"1","2","3"});
		table.addLine(new String[]{"4","5","6"});
		assertEquals(null,multi.getDataLine(3));

		multi=new MultiTable();
		assertNotNull(table=multi.add("Test"));
		table.addLine(new String[]{"1","2","3"});
		assertNotNull(line=multi.getDataLine(3));
		assertEquals(3,line.length);
		assertEquals("1",line[0]);
		assertEquals("2",line[1]);
		assertEquals("3",line[2]);

		multi=new MultiTable();
		assertNotNull(table=multi.add("Test"));
		table.addLine(new String[]{"1","2","3"});
		table.addLine(new String[]{"4","5","6"});
		assertEquals(null,multi.getDataLine(2,3));

		multi=new MultiTable();
		assertNotNull(table=multi.add("Test"));
		table.addLine(new String[]{"1","2","3"});
		assertNotNull(line=multi.getDataLine(2,3));
		assertEquals(3,line.length);
		assertEquals("1",line[0]);
		assertEquals("2",line[1]);
		assertEquals("3",line[2]);

		multi=new MultiTable();
		assertNotNull(table=multi.add("Test"));
		table.addLine(new String[]{"1","2","3"});
		table.addLine(new String[]{"4","5","6"});
		assertEquals(null,multi.getNumbersLine(3));

		multi=new MultiTable();
		assertNotNull(table=multi.add("Test"));
		table.addLine(new String[]{"1","2","3"});
		assertNotNull(numbers=multi.getNumbersLine(3));
		assertEquals(3,line.length);
		assertEquals(1.0,numbers[0]);
		assertEquals(2.0,numbers[1]);
		assertEquals(3.0,numbers[2]);

		multi=new MultiTable();
		assertNotNull(table=multi.add("Test"));
		table.addLine(new String[]{"1","2","3"});
		table.addLine(new String[]{"4","5","6"});
		assertEquals(null,multi.getNumbersLine(2,3));

		multi=new MultiTable();
		assertNotNull(table=multi.add("Test"));
		table.addLine(new String[]{"1","2","3"});
		assertNotNull(numbers=multi.getNumbersLine(2,3));
		assertEquals(3,line.length);
		assertEquals(1.0,numbers[0]);
		assertEquals(2.0,numbers[1]);
		assertEquals(3.0,numbers[2]);

		multi=new MultiTable();
		assertNotNull(table=multi.add("Test"));
		table.addLine(new String[]{"1","2","3"});
		table.addLine(new String[]{"4","5","6"});
		table.addLine(new String[]{"7","8","9"});
		assertNotNull(twolines=multi.getNumbersTwoLines(2,3));
		assertEquals(1,twolines.length);
		assertEquals(null,twolines[0]);

		multi=new MultiTable();
		assertNotNull(table=multi.add("Test"));
		table.addLine(new String[]{"1","2","3"});
		table.addLine(new String[]{"4","5","6"});
		assertNotNull(twolines=multi.getNumbersTwoLines(2,3));
		assertEquals(2,twolines.length);
		assertEquals(3,twolines[0].length);
		assertEquals(1.0,twolines[0][0]);
		assertEquals(2.0,twolines[0][1]);
		assertEquals(3.0,twolines[0][2]);
		assertEquals(3,twolines[1].length);
		assertEquals(4.0,twolines[1][0]);
		assertEquals(5.0,twolines[1][1]);
		assertEquals(6.0,twolines[1][2]);
	}

	/**
	 * Test: Basistests zum Laden und Speichern
	 */
	@Test
	void fileTests() {
		MultiTable multi;
		multi=new MultiTable();

		assertFalse(multi.load(null));
		assertFalse(multi.load(new File("")));
		assertFalse(multi.load(new File(""),Table.SaveMode.SAVEMODE_XLSX));
		assertFalse(multi.load(new File(""),Table.SaveMode.SAVEMODE_XLS));
		assertFalse(multi.load(new File(""),Table.SaveMode.SAVEMODE_ODS));
		assertFalse(multi.load(new File(""),Table.SaveMode.SAVEMODE_SQLITE));

		assertFalse(multi.save(null));
		assertFalse(multi.save(new File("")));
		assertFalse(multi.save(new File(""),Table.SaveMode.SAVEMODE_XLSX));
		assertFalse(multi.save(new File(""),Table.SaveMode.SAVEMODE_XLS));
		assertFalse(multi.save(new File(""),Table.SaveMode.SAVEMODE_ODS));
		assertFalse(multi.save(new File(""),Table.SaveMode.SAVEMODE_HTML));
		assertFalse(multi.save(new File(""),Table.SaveMode.SAVEMODE_DOCX));

		final TableXLSXReader reader=new TableXLSXReader(null);
		assertNotNull(reader.getTable());
		assertFalse(reader.process());
		assertFalse(reader.processFirstTableOnly(null));
	}
}

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
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;
import org.odftoolkit.simple.SpreadsheetDocument;

import mathtools.Table;
import mathtools.TableChart;
import mathtools.TableChartBase.Range;

/**
 * Prüft die Funktionsweise von {@link Table}
 * @author Alexander Herzog
 * @see Table
 */
class TableTest {
	/**
	 * Konstruktor der Klasse
	 */
	public TableTest() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	/**
	 * Wird intern verwendet, um zu prüfen, ob die Tabelle den
	 * vorgegebenen Text-Inhalt besitzt.
	 * @param table	Zu prüfende Tabelle (Annahme: Indizierungsmodus ist Zeilen zu erst)
	 * @see #defaultTableContentTest(Table, mathtools.Table.IndexMode)
	 * @see #constructorTest()
	 * @see #loadTest()
	 * @see #setValueTest()
	 * @see #generalUtilityFunctionsTest()
	 */
	private void defaultTableContentTest(final Table table) {
		defaultTableContentTest(table,Table.IndexMode.ROWS);
	}

	/**
	 * Wird intern verwendet, um zu prüfen, ob die Tabelle den
	 * vorgegebenen Text-Inhalt besitzt.
	 * @param table	Zu prüfende Tabelle
	 * @param indexMode	Indizierungsmodus
	 * @see #defaultTableContentTest(Table)
	 * @see #generalUtilityFunctionsTest()
	 */
	private void defaultTableContentTest(final Table table, final Table.IndexMode indexMode) {
		assertNotNull(table);
		assertSame(indexMode,table.getMode());

		/* Größe abfragen */
		assertEquals(2,table.getSize(0));
		assertEquals(3,table.getSize(1));
		assertEquals(0,table.getSize(2));

		/* Daten direkt abfragen */

		assertEquals("Cell11",table.getValue(0,0));
		assertEquals("Cell12",table.getValue(0,1));
		assertEquals("Cell13",table.getValue(0,2));
		assertEquals("",table.getValue(0,3));

		assertEquals("Cell21",table.getValue(1,0));
		assertEquals("Cell22",table.getValue(1,1));
		assertEquals("Cell23",table.getValue(1,2));
		assertEquals("",table.getValue(1,3));

		assertEquals("",table.getValue(2,0));
		assertEquals("",table.getValue(-1,0));
		assertEquals("",table.getValue(0,-1));

		/* Daten als Liste abfragen */

		final List<List<String>> list=table.getData();
		assertNotNull(list);
		assertEquals(2,list.size());
		assertNotNull(list.get(0));
		assertEquals(3,list.get(0).size());
		assertEquals("Cell11",list.get(0).get(0));
		assertEquals("Cell12",list.get(0).get(1));
		assertEquals("Cell13",list.get(0).get(2));
		assertNotNull(list.get(1));
		assertEquals(3,list.get(1).size());
		assertEquals("Cell21",list.get(1).get(0));
		assertEquals("Cell22",list.get(1).get(1));
		assertEquals("Cell23",list.get(1).get(2));

		/* Daten als Array abfragen */

		final String[][] array=table.getDataArray();
		assertNotNull(array);
		assertEquals(2,array.length);
		assertNotNull(array[0]);
		assertEquals(3,array[0].length);
		assertEquals("Cell11",array[0][0]);
		assertEquals("Cell12",array[0][1]);
		assertEquals("Cell13",array[0][2]);
		assertNotNull(array[1]);
		assertEquals(3,array[1].length);
		assertEquals("Cell21",array[1][0]);
		assertEquals("Cell22",array[1][1]);
		assertEquals("Cell23",array[1][2]);

		/* Zeilenweise */

		List<String> line;
		assertNotNull(line=table.getLine(0));
		assertEquals(3,line.size());
		assertEquals("Cell11",line.get(0));
		assertEquals("Cell12",line.get(1));
		assertEquals("Cell13",line.get(2));
		assertNotNull(line=table.getLine(1));
		assertEquals(3,line.size());
		assertEquals("Cell21",line.get(0));
		assertEquals("Cell22",line.get(1));
		assertEquals("Cell23",line.get(2));
		assertEquals(null,table.getLine(2));
		assertEquals(null,table.getLine(-1));
	}

	/**
	 * Wird intern verwendet, um zu prüfen, ob die Tabelle den
	 * vorgegebenen Zahlen-Inhalt besitzt.
	 * @param table	Zu prüfende Tabelle
	 * @see #loadTest()
	 */
	private void defaultNumericTableContentTest(final Table table) {
		assertNotNull(table);
		assertSame(Table.IndexMode.ROWS,table.getMode());

		/* Größe abfragen */
		assertEquals(2,table.getSize(0));
		assertEquals(3,table.getSize(1));
		assertEquals(0,table.getSize(2));

		/* Daten direkt abfragen */

		assertEquals("1",table.getValue(0,0));
		assertEquals("2",table.getValue(0,1));
		assertEquals("3",table.getValue(0,2));
		assertEquals("",table.getValue(0,3));

		assertEquals("4",table.getValue(1,0));
		assertEquals("5",table.getValue(1,1));
		assertEquals("6",table.getValue(1,2));
		assertEquals("",table.getValue(1,3));

		assertEquals("",table.getValue(2,0));
		assertEquals("",table.getValue(-1,0));
		assertEquals("",table.getValue(0,-1));

		/* Daten als Liste abfragen */

		final List<List<String>> list=table.getData();
		assertNotNull(list);
		assertEquals(2,list.size());
		assertNotNull(list.get(0));
		assertEquals(3,list.get(0).size());
		assertEquals("1",list.get(0).get(0));
		assertEquals("2",list.get(0).get(1));
		assertEquals("3",list.get(0).get(2));
		assertNotNull(list.get(1));
		assertEquals(3,list.get(1).size());
		assertEquals("4",list.get(1).get(0));
		assertEquals("5",list.get(1).get(1));
		assertEquals("6",list.get(1).get(2));

		/* Daten als Array abfragen */

		final String[][] array=table.getDataArray();
		assertNotNull(array);
		assertEquals(2,array.length);
		assertNotNull(array[0]);
		assertEquals(3,array[0].length);
		assertEquals("1",array[0][0]);
		assertEquals("2",array[0][1]);
		assertEquals("3",array[0][2]);
		assertNotNull(array[1]);
		assertEquals(3,array[1].length);
		assertEquals("4",array[1][0]);
		assertEquals("5",array[1][1]);
		assertEquals("6",array[1][2]);

		/* Zeilenweise */

		List<String> line;
		assertNotNull(line=table.getLine(0));
		assertEquals(3,line.size());
		assertEquals("1",line.get(0));
		assertEquals("2",line.get(1));
		assertEquals("3",line.get(2));
		assertNotNull(line=table.getLine(1));
		assertEquals(3,line.size());
		assertEquals("4",line.get(0));
		assertEquals("5",line.get(1));
		assertEquals("6",line.get(2));
		assertEquals(null,table.getLine(2));
		assertEquals(null,table.getLine(-1));
	}

	/**
	 * Test: Verschiedene Konstruktor-Varianten
	 */
	@Test
	void constructorTest() {
		Table table;

		table=new Table();
		assertSame(Table.IndexMode.ROWS,table.getMode());
		assertEquals(0,table.getSize(0));
		assertEquals(0,table.getSize(1));
		assertEquals(0,table.getSize(2));

		table=new Table(Table.IndexMode.ROWS);
		assertSame(Table.IndexMode.ROWS,table.getMode());

		table=new Table(Table.IndexMode.COLS);
		assertSame(Table.IndexMode.COLS,table.getMode());

		Table.IndexMode mode=null;
		table=new Table(mode);
		assertSame(Table.IndexMode.ROWS,table.getMode());

		table=new Table(Table.IndexMode.ROWS,new String[][] {new String[] {"Cell11","Cell12","Cell13"},new String[] {"Cell21","Cell22","Cell23"}});
		defaultTableContentTest(table);

		table=new Table(Table.IndexMode.ROWS,Arrays.asList(Arrays.asList("Cell11","Cell12","Cell13"),Arrays.asList("Cell21","Cell22","Cell23")));
		defaultTableContentTest(table);
	}

	/**
	 * Test: Daten auf verschiedene Arten in die Tabelle laden
	 * @see Table#load(String)
	 * @see Table#load(String[][])
	 * @see Table#setLine(int, List)
	 * @see Table#setLine(int, String[])
	 * @see Table#addLine(double[])
	 * @see Table#addLine(int[])
	 * @see Table#addLine(List)
	 * @see Table#addLine(long[])
	 * @see Table#addLine(String[])
	 * @see Table#addLines(List)
	 * @see Table#addLines(String[][])
	 */
	@Test
	void loadTest() {
		Table table;

		table=new Table();
		table.load(new String[][] {new String[] {"X","Y","Z"},new String[] {"1","2","3"}});
		table.load(new String[][] {new String[] {"Cell11","Cell12","Cell13"},new String[] {"Cell21","Cell22","Cell23"}});
		defaultTableContentTest(table);
		table.addLine((String[])null);
		assertEquals(3,table.getSize(0));
		table.addLine((List<String>)null);
		assertEquals(4,table.getSize(0));

		table=new Table();
		table.load(new String[][] {new String[] {"X","Y","Z"},new String[] {"1","2","3"}});
		table.load(Arrays.asList(Arrays.asList("Cell11","Cell12","Cell13"),Arrays.asList("Cell21","Cell22","Cell23")));
		defaultTableContentTest(table);

		table=new Table();
		table.load(Arrays.asList(Arrays.asList("Cell11","Cell12","Cell13"),Arrays.asList("X","Y","Z")));
		table.setLine(1,new String[]{"Cell21","Cell22","Cell23"});
		defaultTableContentTest(table);

		table=new Table();
		table.load(Arrays.asList(Arrays.asList("Cell11","Cell12","Cell13"),Arrays.asList("X","Y","Z")));
		table.setLine(1,Arrays.asList("Cell21","Cell22","Cell23"));
		defaultTableContentTest(table);

		table=new Table();
		table.load(Arrays.asList(Arrays.asList("Cell11","Cell12","Cell13")));
		table.setLine(1,new String[]{"Cell21","Cell22","Cell23"});
		defaultTableContentTest(table);

		table=new Table();
		table.load(Arrays.asList(Arrays.asList("Cell11","Cell12","Cell13")));
		table.setLine(1,Arrays.asList("Cell21","Cell22","Cell23"));
		defaultTableContentTest(table);

		table=new Table();
		table.load(Arrays.asList(Arrays.asList("Cell11","Cell12","Cell13"),Arrays.asList("Cell21","Cell22","Cell23")));
		table.setLine(-1,new String[]{"X","Y","Z"});
		defaultTableContentTest(table);

		table=new Table();
		table.load(Arrays.asList(Arrays.asList("Cell11","Cell12","Cell13"),Arrays.asList("Cell21","Cell22","Cell23")));
		table.setLine(-1,Arrays.asList("X","Y","Z"));
		defaultTableContentTest(table);

		table=new Table();
		table.addLines(new String[][] {new String[] {"Cell11","Cell12","Cell13"},new String[] {"Cell21","Cell22","Cell23"}});
		defaultTableContentTest(table);

		table=new Table();
		table.addLines(Arrays.asList(Arrays.asList("Cell11","Cell12","Cell13"),Arrays.asList("Cell21","Cell22","Cell23")));
		defaultTableContentTest(table);

		table=new Table();
		table.load(new String[][] {new String[] {"Cell21","Cell22","Cell23"}});
		table.insertLine(new String[] {"Cell11","Cell12","Cell13"},0);
		defaultTableContentTest(table);

		table=new Table();
		table.load(new String[][] {new String[] {"Cell11","Cell12","Cell13"}});
		table.insertLine(new String[] {"Cell21","Cell22","Cell23"},1);
		defaultTableContentTest(table);

		table=new Table();
		table.addLine(new double[]{1,2,3});
		table.addLine(new double[]{4,5,6});
		defaultNumericTableContentTest(table);
		table.addLine((double[])null);
		assertEquals(3,table.getSize(0));

		table=new Table();
		table.addLine(new long[]{1,2,3});
		table.addLine(new long[]{4,5,6});
		defaultNumericTableContentTest(table);
		table.addLine((long[])null);
		assertEquals(3,table.getSize(0));

		table=new Table();
		table.addLine(new int[]{1,2,3});
		table.addLine(new int[]{4,5,6});
		defaultNumericTableContentTest(table);
		table.addLine((int[])null);
		assertEquals(3,table.getSize(0));

		table=new Table();
		table.setData("1\t2\t3\t4\n\n5\t6\t7\t8");
		assertEquals(3,table.getSize(0));
		assertEquals(4,table.getSize(1));
	}

	/**
	 * Test: Einstellen einzelner Werte
	 * @see Table#setValue(int, int, String)
	 */
	@Test
	void setValueTest() {
		Table table;

		table=new Table();
		table.setValue(0,0,"Cell11");
		table.setValue(0,1,"Cell12");
		table.setValue(0,2,"Cell13");
		table.setValue(1,0,"Cell21");
		table.setValue(1,1,"Cell22");
		table.setValue(1,2,"Cell23");
		table.setValue(-1,0,"X");
		table.setValue(0,-1,"Y");
		defaultTableContentTest(table);
	}

	/**
	 * Test: Lesen und Schreiben von CSV-Daten
	 */
	@Test
	void csvTest() {
		Table table;

		final String scvTestString="1;Test;\"abc;def\"\n\"123\"\"456\"";
		final String scvTestString2="1\tTest\t\"abc;def\"\n\"123\"\"456\"";

		table=new Table(scvTestString);
		assertEquals(2,table.getSize(0));
		assertEquals(3,table.getSize(1));
		assertEquals("1",table.getValue(0,0));
		assertEquals("Test",table.getValue(0,1));
		assertEquals("abc;def",table.getValue(0,2));
		assertEquals("123\"456",table.getValue(1,0));

		assertEquals("1\tTest\tabc;def\n123\"456\t\t",table.toString());
		assertEquals("1;Test;\"abc;def\"\n\"123\"\"456\";;",table.toStringCSV());
		assertEquals("1,Test,\"abc;def\"\n\"123\"\"456\",,",table.toStringCSVR());

		final StringBuilder LaTeX=new StringBuilder();
		LaTeX.append("\\begin{table}[H]\n");
		LaTeX.append("\\begin{center}\n");
		LaTeX.append("\\begin{tabular}{r|r|r}\n");
		LaTeX.append("1&Test&abc;def\\\\\n");
		LaTeX.append("123\"456&&\n");
		LaTeX.append("\\end{tabular}\n");
		LaTeX.append("\\end{center}\n");
		LaTeX.append("\\caption{Simulator}\n");
		LaTeX.append("%\\label{LabelForTable}\n");
		LaTeX.append("\\end{table}\n");
		assertEquals(LaTeX.toString(),table.saveToLaTeX());

		table=new Table();
		table.load(scvTestString);
		assertEquals(2,table.getSize(0));
		assertEquals(3,table.getSize(1));
		assertEquals("1",table.getValue(0,0));
		assertEquals("Test",table.getValue(0,1));
		assertEquals("abc;def",table.getValue(0,2));
		assertEquals("123\"456",table.getValue(1,0));

		table=new Table(Table.IndexMode.COLS);
		table.load(scvTestString);
		assertEquals(3,table.getSize(0));
		assertEquals(2,table.getSize(1));
		assertEquals("1",table.getValue(0,0));
		assertEquals("Test",table.getValue(1,0));
		assertEquals("abc;def",table.getValue(2,0));
		assertEquals("123\"456",table.getValue(0,1));

		table=new Table(scvTestString2);
		assertEquals(2,table.getSize(0));
		assertEquals(3,table.getSize(1));
		assertEquals("1",table.getValue(0,0));
		assertEquals("Test",table.getValue(0,1));
		assertEquals("abc;def",table.getValue(0,2));
		assertEquals("123\"456",table.getValue(1,0));

		table=new Table("ABC");
		assertEquals(1,table.getSize(0));
		assertEquals(1,table.getSize(1));
		assertEquals("ABC",table.getValue(0,0));

		table=new Table("ABC;");
		assertEquals(1,table.getSize(0));
		assertEquals(1,table.getSize(1));
		assertEquals("ABC",table.getValue(0,0));

		table=new Table(";");
		assertEquals(0,table.getSize(0));
	}

	/**
	 * Test: Hilfsfunktionen
	 * @see Table#clone()
	 * @see Table#transpose()
	 * @see Table#toString()
	 */
	@Test
	void generalUtilityFunctionsTest() {
		Table table;

		/* Clone */

		table=new Table();
		table.load(new String[][] {new String[] {"Cell11","Cell12","Cell13"},new String[] {"Cell21","Cell22","Cell23"}});
		defaultTableContentTest(table.clone());

		/* Transpose */

		table=new Table(Table.IndexMode.COLS);
		table.load(new String[][] {new String[] {"Cell11","Cell21"},new String[] {"Cell12","Cell22"},new String[] {"Cell13","Cell23"}});
		defaultTableContentTest(table.transpose());

		table=new Table();
		table.load(new String[][] {new String[] {"Cell11","Cell12","Cell13"},new String[] {"Cell21","Cell22","Cell23"}});
		defaultTableContentTest(table.transpose().transpose());

		table=new Table(Table.IndexMode.COLS);
		table.load(new String[][] {new String[] {"Cell11","Cell21"},new String[] {"Cell12","Cell22"},new String[] {"Cell13","Cell23"}});
		defaultTableContentTest(table.transpose(true),Table.IndexMode.COLS);

		table=new Table();
		table.load(new String[][] {new String[] {"Cell11","Cell12","Cell13"},new String[] {"Cell21","Cell22","Cell23"}});
		defaultTableContentTest(table.transpose(true).transpose(true));

		/* toString */

		table=new Table();
		table.load(new String[][] {new String[] {"Cell11","Cell12","Cell13"},new String[] {"Cell21","Cell22","Cell23"}});
		assertEquals("Cell11\tCell12\tCell13\nCell21\tCell22\tCell23",table.toString());

		table=new Table(Table.IndexMode.COLS);
		table.load(new String[][] {new String[] {"Cell11","Cell12","Cell13"},new String[] {"Cell21","Cell22","Cell23"}});
		assertEquals("Cell11\tCell21\nCell12\tCell22\nCell13\tCell23",table.toString());
	}

	/**
	 * Test: Hilfsfunktionen für den Umgang mit Dateien
	 * @see Table#getSaveModeFromFileName(File, boolean, boolean)
	 * @see Table#isTableFileName(File)
	 */
	@Test
	void fileUtilityFunctionsTest() {

		assertSame(Table.SaveMode.SAVEMODE_TABS,Table.getSaveModeFromFileName(null,false,false));

		assertSame(Table.SaveMode.SAVEMODE_TABS,Table.getSaveModeFromFileName(new File("Test.abc"),false,false));

		assertSame(Table.SaveMode.SAVEMODE_TABS,Table.getSaveModeFromFileName(new File("Test.tsv"),false,false));
		assertSame(Table.SaveMode.SAVEMODE_TABS,Table.getSaveModeFromFileName(new File("Test.txt"),false,false));
		assertSame(Table.SaveMode.SAVEMODE_CSV,Table.getSaveModeFromFileName(new File("Test.csv"),false,false));
		assertSame(Table.SaveMode.SAVEMODE_XLSX,Table.getSaveModeFromFileName(new File("Test.xlsx"),false,false));
		assertSame(Table.SaveMode.SAVEMODE_XLS,Table.getSaveModeFromFileName(new File("Test.xls"),false,false));

		assertSame(Table.SaveMode.SAVEMODE_DOCX,Table.getSaveModeFromFileName(new File("Test.docx"),false,false));
		assertSame(Table.SaveMode.SAVEMODE_HTML,Table.getSaveModeFromFileName(new File("Test.html"),false,false));
		assertSame(Table.SaveMode.SAVEMODE_HTML,Table.getSaveModeFromFileName(new File("Test.htm"),false,false));

		assertSame(Table.SaveMode.SAVEMODE_TABS,Table.getSaveModeFromFileName(new File("Test.docx"),true,false));
		assertSame(Table.SaveMode.SAVEMODE_TABS,Table.getSaveModeFromFileName(new File("Test.html"),true,false));
		assertSame(Table.SaveMode.SAVEMODE_TABS,Table.getSaveModeFromFileName(new File("Test.htm"),true,false));

		assertSame(Table.SaveMode.SAVEMODE_DIF,Table.getSaveModeFromFileName(new File("Test.dif"),true,false));

		assertSame(Table.SaveMode.SAVEMODE_SQLITE,Table.getSaveModeFromFileName(new File("Test.sqlite3"),false,false));
		assertSame(Table.SaveMode.SAVEMODE_SQLITE,Table.getSaveModeFromFileName(new File("Test.sqlite"),false,false));
		assertSame(Table.SaveMode.SAVEMODE_SQLITE,Table.getSaveModeFromFileName(new File("Test.db"),false,false));
		assertSame(Table.SaveMode.SAVEMODE_SQLITE,Table.getSaveModeFromFileName(new File("Test.db3"),false,false));

		assertSame(Table.SaveMode.SAVEMODE_TABS,Table.getSaveModeFromFileName(new File("Test.sqlite3"),false,true));
		assertSame(Table.SaveMode.SAVEMODE_TABS,Table.getSaveModeFromFileName(new File("Test.sqlite"),false,true));
		assertSame(Table.SaveMode.SAVEMODE_TABS,Table.getSaveModeFromFileName(new File("Test.db"),false,true));
		assertSame(Table.SaveMode.SAVEMODE_TABS,Table.getSaveModeFromFileName(new File("Test.db3"),false,true));

		assertTrue(Table.isTableFileName(new File("Test.tsv")));
		assertTrue(Table.isTableFileName(new File("Test.txt")));
		assertTrue(Table.isTableFileName(new File("Test.csv")));
		assertTrue(Table.isTableFileName(new File("Test.xlsx")));
		assertTrue(Table.isTableFileName(new File("Test.xls")));
		assertFalse(Table.isTableFileName(new File("Test.abc")));
	}

	/**
	 * Test: Hilfsfunktionen zum Umgang mit einzelnen Zellen (Bezeichnern und Werten)
	 * @see Table#convertToNumeric(String)
	 * @see Table#numberFromColumnName(String)
	 * @see Table#columnNameFromNumber(int)
	 * @see Table#cellIDFromNumber(int[])
	 * @see Table#cellIDToNumbers(String)
	 */
	@Test
	void cellUtilityFunctionsTest() {
		assertEquals(Double.valueOf(123.456),Table.convertToNumeric("123,456"));
		assertEquals(Double.valueOf(123.456),Table.convertToNumeric("123.456"));
		assertEquals(Double.valueOf(1.23456),Table.convertToNumeric("123,456%"));
		assertEquals(Double.valueOf(1.23456),Table.convertToNumeric("123,456 %"));
		assertEquals(null,Table.convertToNumeric("äöü"));

		assertEquals(0,Table.numberFromColumnName("A"));
		assertEquals(25,Table.numberFromColumnName("Z"));
		assertEquals(26,Table.numberFromColumnName("AA"));
		assertEquals(51,Table.numberFromColumnName("AZ"));
		assertEquals(52,Table.numberFromColumnName("BA"));
		assertEquals(26*26+25,Table.numberFromColumnName("ZZ"));
		assertEquals(1+26*26+25,Table.numberFromColumnName("AAA"));
		assertEquals(-1,Table.numberFromColumnName("@"));
		assertEquals(-1,Table.numberFromColumnName("ä"));
		assertEquals(-1,Table.numberFromColumnName(""));
		assertEquals(-1,Table.numberFromColumnName(null));

		assertEquals("A",Table.columnNameFromNumber(0));
		assertEquals("Z",Table.columnNameFromNumber(25));
		assertEquals("AA",Table.columnNameFromNumber(26));
		assertEquals("AZ",Table.columnNameFromNumber(51));
		assertEquals("BA",Table.columnNameFromNumber(52));
		assertEquals("ZZ",Table.columnNameFromNumber(26*26+25));
		assertEquals("AAA",Table.columnNameFromNumber(1+26*26+25));
		assertEquals("",Table.columnNameFromNumber(-1));

		assertTrue(Objects.deepEquals(new int[]{0,0},Table.cellIDToNumbers("A1")));
		assertTrue(Objects.deepEquals(new int[]{1,0},Table.cellIDToNumbers("A2")));
		assertTrue(Objects.deepEquals(new int[]{0,25},Table.cellIDToNumbers("Z1")));
		assertTrue(Objects.deepEquals(new int[]{1,25},Table.cellIDToNumbers("Z2")));
		assertTrue(Objects.deepEquals(new int[]{0,26},Table.cellIDToNumbers("AA1")));
		assertTrue(Objects.deepEquals(new int[]{1,26},Table.cellIDToNumbers("AA2")));
		assertTrue(Objects.deepEquals(new int[]{0,51},Table.cellIDToNumbers("AZ1")));
		assertTrue(Objects.deepEquals(new int[]{1,51},Table.cellIDToNumbers("AZ2")));
		assertTrue(Objects.deepEquals(new int[]{0,52},Table.cellIDToNumbers("BA1")));
		assertTrue(Objects.deepEquals(new int[]{1,52},Table.cellIDToNumbers("BA2")));
		assertTrue(Objects.deepEquals(new int[]{0,26*26+25},Table.cellIDToNumbers("ZZ1")));
		assertTrue(Objects.deepEquals(new int[]{1,26*26+25},Table.cellIDToNumbers("ZZ2")));
		assertTrue(Objects.deepEquals(new int[]{0,1+26*26+25},Table.cellIDToNumbers("AAA1")));
		assertTrue(Objects.deepEquals(new int[]{1,1+26*26+25},Table.cellIDToNumbers("AAA2")));
		assertEquals(null,Table.cellIDToNumbers("ä"));
		assertEquals(null,Table.cellIDToNumbers("@"));
		assertEquals(null,Table.cellIDToNumbers(""));
		assertEquals(null,Table.cellIDToNumbers(null));
		assertEquals(null,Table.cellIDToNumbers("A"));
		assertEquals(null,Table.cellIDToNumbers("A-3"));
		assertEquals(null,Table.cellIDToNumbers("A3,5"));
		assertEquals(null,Table.cellIDToNumbers("A0"));

		assertEquals("A1",Table.cellIDFromNumber(new int[]{0,0}));
		assertEquals("A2",Table.cellIDFromNumber(new int[]{1,0}));
		assertEquals("Z1",Table.cellIDFromNumber(new int[]{0,25}));
		assertEquals("Z2",Table.cellIDFromNumber(new int[]{1,25}));
		assertEquals("AA1",Table.cellIDFromNumber(new int[]{0,26}));
		assertEquals("AA2",Table.cellIDFromNumber(new int[]{1,26}));
		assertEquals("AZ1",Table.cellIDFromNumber(new int[]{0,51}));
		assertEquals("AZ2",Table.cellIDFromNumber(new int[]{1,51}));
		assertEquals("ZZ1",Table.cellIDFromNumber(new int[]{0,26*26+25}));
		assertEquals("ZZ2",Table.cellIDFromNumber(new int[]{1,26*26+25}));
		assertEquals("AAA1",Table.cellIDFromNumber(new int[]{0,1+26*26+25}));
		assertEquals("AAA2",Table.cellIDFromNumber(new int[]{1,1+26*26+25}));
		assertEquals("",Table.cellIDFromNumber(null));
		assertEquals("",Table.cellIDFromNumber(new int[]{0}));
		assertEquals("",Table.cellIDFromNumber(new int[]{0,0,0}));
	}

	/**
	 * Test: Hilfsfunktionen zum Umgang mit Zellbereichen
	 * @see Table#getNumberArea(String, String)
	 * @see Table#getDataArea(String, String)
	 */
	@Test
	void rangeUtilityFunctionsTest() {
		Table table;

		table=new Table(Table.IndexMode.ROWS,new String[][]{new String[]{"1","2","3"},new String[]{"4","5","6"},new String[]{"7","8","9"}});

		/* Zahlenbereich */

		assertTrue(Objects.deepEquals(new double[]{2,3},table.getNumberArea("B1","C1")));
		assertEquals(null,table.getAreaError());

		assertTrue(Objects.deepEquals(new double[]{5,8},table.getNumberArea("B2","B3")));
		assertEquals(null,table.getAreaError());

		assertTrue(Objects.deepEquals(new double[]{3,2},table.getNumberArea("C1","B1")));
		assertEquals(null,table.getAreaError());

		assertTrue(Objects.deepEquals(new double[]{8,5},table.getNumberArea("B3","B2")));
		assertEquals(null,table.getAreaError());

		assertEquals(null,table.getNumberArea("A1","C3"));
		assertNotNull(table.getAreaError());

		assertEquals(null,table.getNumberArea("#","C3"));
		assertNotNull(table.getAreaError());

		assertEquals(null,table.getNumberArea("A1","#"));
		assertNotNull(table.getAreaError());

		assertEquals(null,table.getNumberArea("B1","D1"));
		assertNotNull(table.getAreaError());

		assertEquals(null,table.getNumberArea("D1","B1"));
		assertNotNull(table.getAreaError());

		assertEquals(null,table.getNumberArea("B2","B4"));
		assertNotNull(table.getAreaError());

		assertEquals(null,table.getNumberArea("B4","B2"));
		assertNotNull(table.getAreaError());

		table.setValue(1,1,"ä");

		assertEquals(null,table.getNumberArea("B2","C2"));
		assertNotNull(table.getAreaError());

		assertEquals(null,table.getNumberArea("B2","B3"));
		assertNotNull(table.getAreaError());

		/* Datenbereich */

		assertTrue(Objects.deepEquals(new String[]{"2","3"},table.getDataArea("B1","C1")));
		assertEquals(null,table.getAreaError());

		assertTrue(Objects.deepEquals(new String[]{"ä","8"},table.getDataArea("B2","B3")));
		assertEquals(null,table.getAreaError());

		assertTrue(Objects.deepEquals(new String[]{"3","2"},table.getDataArea("C1","B1")));
		assertEquals(null,table.getAreaError());

		assertTrue(Objects.deepEquals(new String[]{"8","ä"},table.getDataArea("B3","B2")));
		assertEquals(null,table.getAreaError());

		assertNull(table.getDataArea("A1","C3"));
		assertNotNull(table.getAreaError());

		assertNull(table.getDataArea("#","C3"));
		assertNotNull(table.getAreaError());

		assertNull(table.getDataArea("A1","#"));
		assertNotNull(table.getAreaError());

		assertNull(table.getDataArea("B1","D1"));
		assertNotNull(table.getAreaError());

		assertNull(table.getDataArea("D1","B1"));
		assertNotNull(table.getAreaError());

		assertNull(table.getDataArea("B2","B4"));
		assertNotNull(table.getAreaError());

		assertNull(table.getDataArea("B4","B2"));
		assertNotNull(table.getAreaError());
	}

	/**
	 * Test: Basistests für die Datei Ein- und Ausgabe
	 */
	@Test
	void fileTest() {
		Table table, table2;
		File file;
		/*
		@SuppressWarnings("resource")
		InputStream stream;
		 */

		/* HTML-Tabellen */

		table=new Table();
		table.load(new String[][]{new String[]{"Cell11","Cell12","Cell13"},new String[]{"Cell21","Cell22","Cell23"}});
		assertEquals("<table>\n  <tr><td>Cell11</td><td>Cell12</td><td>Cell13</td></tr>\n  <tr><td>Cell21</td><td>Cell22</td><td>Cell23</td></tr>\n</table>\n",table.saveToHTML());

		table=new Table(Table.IndexMode.COLS);
		table.load(new String[][]{new String[]{"Cell11","Cell21"},new String[]{"Cell12","Cell22"},new String[]{"Cell13","Cell23"}});
		assertEquals("<table>\n  <tr><td>Cell11</td><td>Cell12</td><td>Cell13</td></tr>\n  <tr><td>Cell21</td><td>Cell22</td><td>Cell23</td></tr>\n</table>\n",table.saveToHTML());

		/* Allgemeine Lade- und Speicherfunktionen */

		file=null;
		table=new Table();
		assertFalse(table.load(file));
		assertFalse(table.load(new File("")));
		for (Table.SaveMode mode: Table.SaveMode.values()) assertFalse(table.load(new File(""),mode));

		file=null;
		table=new Table(Table.IndexMode.COLS);
		assertFalse(table.load(file));
		assertFalse(table.load(new File("")));
		for (Table.SaveMode mode: Table.SaveMode.values()) assertFalse(table.load(new File(""),mode));

		file=null;
		table=new Table();
		assertFalse(table.save(file));
		assertFalse(table.save(new File("")));
		for (Table.SaveMode mode: Table.SaveMode.values()) assertFalse(table.save(new File(""),mode));

		/* Excel-Tabellen */

		try (XWPFDocument doc=new XWPFDocument()) {
			table=new Table();
			table.addLine(new String[]{"1","2","3"});
			table.addLine(new String[]{"a","b","c"});
			table.saveToDOCX(doc);
		} catch (IOException e) {assertTrue(false);}

		try (XWPFDocument doc=new XWPFDocument()) {
			table=new Table(Table.IndexMode.COLS);
			table.addLine(new String[]{"1","2","3"});
			table.addLine(new String[]{"a","b","c"});
			table.saveToDOCX(doc);
		} catch (IOException e) {assertTrue(false);}

		table=new Table();
		table.addLine(new String[]{"1","2","3"});
		table.addLine(new String[]{"a","b","c"});
		table.addLine(new String[]{"5%","1,2","3.4"});
		try (Workbook wb=new XSSFWorkbook()) {
			final Sheet sheet=wb.createSheet("Tabelle");
			table.saveToSheet(wb,sheet);
			table=new Table();
			table.loadFromSheet(sheet);
			assertEquals("1",table.getValue(0,0));
			assertEquals("2",table.getValue(0,1));
			assertEquals("3",table.getValue(0,2));
			assertEquals("a",table.getValue(1,0));
			assertEquals("b",table.getValue(1,1));
			assertEquals("c",table.getValue(1,2));
			assertEquals("0,05",table.getValue(2,0));
			assertEquals("1,2",table.getValue(2,1));
			assertEquals("3,4",table.getValue(2,2));
		} catch (IOException e) {assertTrue(false);}

		table=table.transpose();
		try (Workbook wb=new XSSFWorkbook()) {
			final Sheet sheet=wb.createSheet("Tabelle");
			table.saveToSheet(wb,sheet);
			table=new Table();
			table.loadFromSheet(sheet);
			assertEquals("1",table.getValue(0,0));
			assertEquals("2",table.getValue(0,1));
			assertEquals("3",table.getValue(0,2));
			assertEquals("a",table.getValue(1,0));
			assertEquals("b",table.getValue(1,1));
			assertEquals("c",table.getValue(1,2));
			assertEquals("0,05",table.getValue(2,0));
			assertEquals("1,2",table.getValue(2,1));
			assertEquals("3,4",table.getValue(2,2));
		} catch (IOException e) {assertTrue(false);}

		table=table.transpose();
		try (Workbook wb=new XSSFWorkbook()) {
			final Sheet sheet=wb.createSheet("Tabelle");
			table.saveToSheet(wb,sheet);
			table=new Table();
			table.loadFromSheet(sheet);
			assertEquals("1",table.getValue(0,0));
			assertEquals("2",table.getValue(0,1));
			assertEquals("3",table.getValue(0,2));
			assertEquals("a",table.getValue(1,0));
			assertEquals("b",table.getValue(1,1));
			assertEquals("c",table.getValue(1,2));
			assertEquals("0,05",table.getValue(2,0));
			assertEquals("1,2",table.getValue(2,1));
			assertEquals("3,4",table.getValue(2,2));
		} catch (IOException e) {assertTrue(false);}

		/* OpenOffice(LibreOffice-Tabellen */

		try (SpreadsheetDocument ods=SpreadsheetDocument.newSpreadsheetDocument()) {
			table=new Table();
			table.addLine(new String[]{"1","2","3"});
			table.addLine(new String[]{"a","b","c"});
			table.saveToSheet(ods.addTable());
		} catch (Exception e) {assertTrue(false);}

		try (SpreadsheetDocument ods=SpreadsheetDocument.newSpreadsheetDocument()) {
			table=new Table(Table.IndexMode.COLS);
			table.addLine(new String[]{"1","2","3"});
			table.addLine(new String[]{"a","b","c"});
			table.saveToSheet(ods.addTable());
		} catch (Exception e) {assertTrue(false);}

		table=new Table();
		table.addLine(new String[]{"1","2","3"});
		table.addLine(new String[]{"a","b","c"});
		table.addLine(new String[]{"5%","1,2","3.4"});
		try (SpreadsheetDocument ods=SpreadsheetDocument.newSpreadsheetDocument()) {
			final org.odftoolkit.simple.table.Table odsTable=ods.addTable();
			table.saveToSheet(odsTable);
			table=new Table();
			table.loadFromSheet(odsTable);
			assertEquals("1",table.getValue(0,0));
			assertEquals("2",table.getValue(0,1));
			assertEquals("3",table.getValue(0,2));
			assertEquals("a",table.getValue(1,0));
			assertEquals("b",table.getValue(1,1));
			assertEquals("c",table.getValue(1,2));
			assertEquals("5%",table.getValue(2,0)); /* "5%" wird beibehalten, nicht in 0,05 umgewandelt */
			assertEquals("1,2",table.getValue(2,1));
			assertEquals("3,4",table.getValue(2,2));
		} catch (Exception e) {assertTrue(false);}

		table=table.transpose();
		try (SpreadsheetDocument ods=SpreadsheetDocument.newSpreadsheetDocument()) {
			final org.odftoolkit.simple.table.Table odsTable=ods.addTable();
			table.saveToSheet(odsTable);
			table=new Table();
			table.loadFromSheet(odsTable);
			assertEquals("1",table.getValue(0,0));
			assertEquals("2",table.getValue(0,1));
			assertEquals("3",table.getValue(0,2));
			assertEquals("a",table.getValue(1,0));
			assertEquals("b",table.getValue(1,1));
			assertEquals("c",table.getValue(1,2));
			assertEquals("5%",table.getValue(2,0)); /* "5%" wird beibehalten, nicht in 0,05 umgewandelt */
			assertEquals("1,2",table.getValue(2,1));
			assertEquals("3,4",table.getValue(2,2));
		} catch (Exception e) {assertTrue(false);}

		table=table.transpose();
		try (SpreadsheetDocument ods=SpreadsheetDocument.newSpreadsheetDocument()) {
			final org.odftoolkit.simple.table.Table odsTable=ods.addTable();
			table.saveToSheet(odsTable);
			table=new Table();
			table.loadFromSheet(odsTable);
			assertEquals("1",table.getValue(0,0));
			assertEquals("2",table.getValue(0,1));
			assertEquals("3",table.getValue(0,2));
			assertEquals("a",table.getValue(1,0));
			assertEquals("b",table.getValue(1,1));
			assertEquals("c",table.getValue(1,2));
			assertEquals("5%",table.getValue(2,0)); /* "5%" wird beibehalten, nicht in 0,05 umgewandelt */
			assertEquals("1,2",table.getValue(2,1));
			assertEquals("3,4",table.getValue(2,2));
		} catch (Exception e) {assertTrue(false);}

		/* DBF-Tabellen */

		table=new Table();
		file=null;
		assertFalse(table.loadDBF(file));

		table=new Table();
		assertFalse(table.loadDBF(new File("")));

		table=new Table();
		InputStream stream=null;
		assertFalse(table.loadDBF(stream));

		/* DIF- und SYLK-Tabellen */

		table=new Table();
		table.load(new String[][]{new String[]{"Cell11","Cell12","Cell13"},new String[]{"Cell21","Cell22","Cell23"}});

		assertEquals("TABLE\n0,1\n\"JUnit\"\nVECTORS\n0,3\n\"\"\nTUPLES\n0,2\n\"\"\nDATA\n0,0\n\"\"\n-1,0\nBOT\n1,0\n\"Cell11\"\n1,0\n\"Cell12\"\n1,0\n\"Cell13\"\n-1,0\nBOT\n1,0\n\"Cell21\"\n1,0\n\"Cell22\"\n1,0\n\"Cell23\"\n-1,0\nEOD\n",table.saveToDIF("JUnit"));
		assertEquals("ID;P\nB;Y2;X3\nC;Y1;X1;K\"Cell11\"\nC;Y1;X2;K\"Cell12\"\nC;Y1;X3;K\"Cell13\"\nC;Y2;X1;K\"Cell21\"\nC;Y2;X2;K\"Cell22\"\nC;Y2;X3;K\"Cell23\"\nE\n",table.saveToSYLK());

		table2=new Table();
		table2.loadDIF(Arrays.asList(table.saveToDIF("JUnit").split("\\n")));
		assertEquals("Cell11;Cell12;Cell13\nCell21;Cell22;Cell23",table2.toStringCSV());

		table2=new Table();
		table2.loadSYLK(Arrays.asList(table.saveToSYLK().split("\\n")));
		assertEquals("Cell11;Cell12;Cell13\nCell21;Cell22;Cell23",table2.toStringCSV());
	}

	/**
	 * Test: Tabelle zusammen mit Chart in Excel-Datei einbetten
	 */
	@Test
	void chartTest() {
		final Table table=new Table();
		table.addLine(new String[]{"1","2","3"});
		table.addLine(new String[]{"a","b","c"});
		table.addLine(new String[]{"5%","1,2","3.4"});

		TableChart tableChart;

		/* Fehlkonfigurationen */

		tableChart=new TableChart(table);
		try (Workbook wb=new XSSFWorkbook()) {
			final Sheet sheet=wb.createSheet("Tabelle");
			tableChart.build("Titel",0,sheet,"sheetName");
		} catch (IOException e) {assertTrue(false);}

		tableChart=new TableChart(table);
		tableChart.setupChart(null);
		try (Workbook wb=new XSSFWorkbook()) {
			final Sheet sheet=wb.createSheet("Tabelle");
			tableChart.build("Titel",0,sheet,"sheetName");
		} catch (IOException e) {assertTrue(false);}

		/* Diagrammtypen, Vorgabespalte */

		tableChart=new TableChart(table);
		tableChart.setupChart(TableChart.ChartMode.BAR);
		try (Workbook wb=new XSSFWorkbook()) {
			final Sheet sheet=wb.createSheet("Tabelle");
			tableChart.build("Titel",0,sheet,"sheetName");
		} catch (IOException e) {assertTrue(false);}

		tableChart=new TableChart(table);
		tableChart.setupChart(TableChart.ChartMode.LINE);
		try (Workbook wb=new XSSFWorkbook()) {
			final Sheet sheet=wb.createSheet("Tabelle");
			tableChart.build("Titel",0,sheet,"sheetName");
		} catch (IOException e) {assertTrue(false);}

		tableChart=new TableChart(table);
		tableChart.setupChart(TableChart.ChartMode.PIE);
		try (Workbook wb=new XSSFWorkbook()) {
			final Sheet sheet=wb.createSheet("Tabelle");
			tableChart.build("Titel",0,sheet,"sheetName");
		} catch (IOException e) {assertTrue(false);}

		/* Diagrammtypen, Spalte vorgeben */

		tableChart=new TableChart(table);
		tableChart.setupChart(TableChart.ChartMode.BAR,0);
		try (Workbook wb=new XSSFWorkbook()) {
			final Sheet sheet=wb.createSheet("Tabelle");
			tableChart.build("Titel",0,sheet,"sheetName");
		} catch (IOException e) {assertTrue(false);}

		tableChart=new TableChart(table);
		tableChart.setupChart(TableChart.ChartMode.LINE,0);
		try (Workbook wb=new XSSFWorkbook()) {
			final Sheet sheet=wb.createSheet("Tabelle");
			tableChart.build("Titel",0,sheet,"sheetName");
		} catch (IOException e) {assertTrue(false);}

		tableChart=new TableChart(table);
		tableChart.setupChart(TableChart.ChartMode.PIE,0);
		try (Workbook wb=new XSSFWorkbook()) {
			final Sheet sheet=wb.createSheet("Tabelle");
			tableChart.build("Titel",0,sheet,"sheetName");
		} catch (IOException e) {assertTrue(false);}

		/* Diagrammtypen, alles manuell */

		final TableChart.Range[] series=new TableChart.Range[] {new TableChart.Range(0,0,0,10),new TableChart.Range(1,0,1,10),new TableChart.Range(2,0,2,10)};
		final Object[] seriesNames=new Object[] {"A","B","C"};
		final Range categories=new TableChart.Range(0,11,2,11);

		tableChart=new TableChart(table);
		tableChart.setupChart(TableChart.ChartMode.BAR,series,seriesNames,categories);
		try (Workbook wb=new XSSFWorkbook()) {
			final Sheet sheet=wb.createSheet("Tabelle");
			tableChart.build("Titel",0,sheet,"sheetName");
		} catch (IOException e) {assertTrue(false);}

		tableChart=new TableChart(table);
		tableChart.setupChart(TableChart.ChartMode.LINE,series,seriesNames,categories);
		try (Workbook wb=new XSSFWorkbook()) {
			final Sheet sheet=wb.createSheet("Tabelle");
			tableChart.build("Titel",0,sheet,"sheetName");
		} catch (IOException e) {assertTrue(false);}

		tableChart=new TableChart(table);
		tableChart.setupChart(TableChart.ChartMode.PIE,series,seriesNames,categories);
		try (Workbook wb=new XSSFWorkbook()) {
			final Sheet sheet=wb.createSheet("Tabelle");
			tableChart.build("Titel",0,sheet,"sheetName");
		} catch (IOException e) {assertTrue(false);}
	}

	/**
	 * Test: Code direkt in dem Speicher-Modi-Enum
	 */
	@Test
	void saveModeTest() {
		for (Table.SaveMode mode: Table.SaveMode.values()) {
			assertFalse(mode.fileNameMatch(null));
			assertFalse(mode.fileNameMatch("not.ourFileFormat"));
		}
	}
}

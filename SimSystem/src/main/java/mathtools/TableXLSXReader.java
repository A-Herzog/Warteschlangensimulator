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
package mathtools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.binary.XSSFBSheetHandler.SheetContentsHandler;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Diese Klasse ließt eine Exceltabelle in eine {@link Table} oder eine {@link MultiTable}
 * im Streaming-Modus (d.h. mit geringem Speicherverbrauch) ein.
 * @author Alexander Herzog
 * @see Table
 * @see MultiTable
 * @version 1.1
 */
public class TableXLSXReader {
	/** Zu lesende Tabellendatei */
	private final File file;
	/** Ergebnisarbeitsmappe */
	private final MultiTable multiTable;

	/**
	 * Konstruktor der Klasse
	 * @param file	Zu lesende Tabellendatei
	 */
	public TableXLSXReader(final File file) {
		this.file=file;
		multiTable=new MultiTable();
	}

	/**
	 * Liefert im Falle der Verarbeitung durch {@link TableXLSXReader#process()} die Ergebnisarbeitsmappe.
	 * @return	Geladene Arbeitsmappe
	 */
	public MultiTable getTable() {
		return multiTable;
	}

	/**
	 * Verarbeitet ein einzelnes Tabellenblatt
	 * @param name	Name des Tabellenblatts
	 * @param styles	Arbeitsmappenweite Styles (für {@link XSSFSheetXMLHandler})
	 * @param strings	Geteilte Strings (für {@link XSSFSheetXMLHandler})
	 * @param stream	Eingabestream
	 * @param useTable	Wird hier ein Wert ungleich <code>null</code> übergeben, so werden die Daten nicht in {@link #multiTable} geschrieben, sondern in diese Tabelle
	 * @return	Liefert im Erfolgsfall <code>true</code>
	 */
	private boolean processSheet(final String name, final StylesTable styles, final ReadOnlySharedStringsTable strings, InputStream stream, Table useTable) {
		final InputSource sheetSource=new InputSource(stream);
		final Table table=(useTable==null)?new Table():useTable;

		try {
			final XMLReader sheetParser=XMLHelper.newXMLReader();

			DateFormat formatter;

			formatter=DateFormat.getDateInstance(DateFormat.MEDIUM,Locale.getDefault());
			styles.putNumberFormat((short)14,((SimpleDateFormat)formatter).toPattern());

			formatter=DateFormat.getDateInstance(DateFormat.LONG,Locale.getDefault());
			styles.putNumberFormat((short)15,((SimpleDateFormat)formatter).toPattern());

			final ContentHandler handler=new XSSFSheetXMLHandler(styles,null,strings,new SheetToTable(table),new CachedDataFormatter(),false);

			sheetParser.setContentHandler(handler);
			sheetParser.parse(sheetSource);
		} catch(ParserConfigurationException | SAXException | IOException e) {
			return false;
		}

		if (useTable==null) multiTable.add(name,table);
		return true;
	}

	/**
	 * Verarbeitung des OPC-Datenobjektes, das die xlsx-Arbeitsmappe enthält
	 * @param xlsxPackage	OPC-Datenobjekt, das die xlsx-Arbeitsmappe enthält
	 * @param table	Wird hier eine Tabelle übergeben, so wird das erste Tabellenblatt aus der Arbeitsmappe in diese Tabelle kopiert; sonst werden alle Tabellenblätter in die {@link #multiTable} übernommen
	 * @return	Liefert im Erfolgsfall <code>true</code>
	 */
	private boolean processOPCPacakge(final OPCPackage xlsxPackage, final Table table) {
		try {
			final CachedReadOnlySharedStringsTable strings=new CachedReadOnlySharedStringsTable(xlsxPackage);
			final XSSFReader xssfReader=new XSSFReader(xlsxPackage);
			final StylesTable styles=xssfReader.getStylesTable();
			final XSSFReader.SheetIterator iter=(XSSFReader.SheetIterator) xssfReader.getSheetsData();
			while (iter.hasNext()) try (InputStream stream=iter.next()) {
				if (!processSheet(iter.getSheetName(),styles,strings,stream,table)) return false;
				if (table!=null) return true;
			}
			return true;

		} catch (IOException | SAXException | OpenXML4JException e1) {
			return false;
		}
	}

	/**
	 * Verarbeitet die gesamte Arbeitsmappe.
	 * Das Ergebnis kann im Erfolgsfall über die Funktion {@link TableXLSXReader#getTable()} abgerufen werden.
	 * @return	Liefert im Erfolgsfall <code>true</code>.
	 */
	public boolean process() {
		if (file==null || !file.isFile()) return false;

		try (OPCPackage xlsxPackage=OPCPackage.open(file,PackageAccess.READ)) {
			return processOPCPacakge(xlsxPackage,null);
		} catch (InvalidFormatException | IOException e) {
			return false;
		}
	}

	/**
	 * Lädt nur die erste Tabelle der Datei.
	 * @param table	{@link Table}-Objekt in das die Daten geladen werden sollen.
	 * @return	Liefert im Erfolgsfall <code>true</code>.
	 */
	public boolean processFirstTableOnly(final Table table) {
		if (file==null || !file.isFile() || table==null) return false;

		try (OPCPackage xlsxPackage=OPCPackage.open(file,PackageAccess.READ)) {
			return processOPCPacakge(xlsxPackage,table);
		} catch (InvalidFormatException | IOException e) {
			return false;
		}
	}

	/**
	 * Transformation eines XLSX-Tabellenblattes in eine {@link Table}-basierte Tabelle
	 * (über einen {@link SheetContentsHandler})
	 * @see TableXLSXReader#processSheet(String, StylesTable, ReadOnlySharedStringsTable, InputStream, Table)
	 * @see SheetContentsHandler
	 */
	private static class SheetToTable implements SheetContentsHandler {
		/** Ziel-Tabelle */
		private final Table table;
		/** Aktuell in Verarbeitung befindliche Zeile */
		private final List<String> row;

		/**
		 * Konstruktor der Klasse
		 * @param table	Ziel-Tabelle
		 */
		private SheetToTable(final Table table) {
			this.table=table;
			row=new ArrayList<>();
		}

		/**
		 * Fügt ggf. in der XLSX-Tabelle nicht vorhandene Zeilen in die Ziel-Tabelle ein.
		 * @param number	Anzahl an einzufügenden leeren Zeilen
		 * @see #startRow(int)
		 */
		private void outputMissingRows(int number) {
			for (int i=0;i<number;i++) table.addLine(new ArrayList<>());
		}

		@Override
		public void startRow(int rowNum) {
			endRow(-1);
			final int missedRows=rowNum-table.getSize(0);
			if (missedRows>0) outputMissingRows(missedRows);
		}

		@Override
		public void endRow(int rowNum) {
			if (row.size()>0) {
				table.addLine(row);
				row.clear();
			}
		}

		@Override
		public void cell(String cellReference, String formattedValue, XSSFComment comment) {
			if (cellReference==null) cellReference=new CellAddress(table.getSize(0),row.size()).formatAsString();

			final int thisCol=Table.numberFromColumnNameIgnoreRowNumbers(cellReference);
			/* Speicherintensiver: final int thisCol=new CellReference(cellReference).getCol(); */
			final int missedCols=thisCol-row.size();
			for (int i=0;i<missedCols;i++) row.add("");

			row.add(formattedValue);
		}

		@Override
		public void hyperlinkCell(String cellReference, String formattedValue, String arg2, String arg3, XSSFComment comment) {
			cell(cellReference,formattedValue,comment);
		}
	}

	/**
	 * Klasse zur Formatierung von Zahlen; baut auf {@link DataFormatter} auf und
	 * versucht so viele Aufrufe des eigentlichen Formaters durch Cacheing usw. zu vermeiden.
	 */
	private class CachedDataFormatter extends DataFormatter {
		/** Format-Index für normale Zahlen ohne besondere Formatierung (nur diese werden gecacht) */
		private static final int CACHE_FORMAT_INDEX=0;
		/** Format-Zeichenkette für normale Zahlen ohne besondere Formatierung (nur diese werden gecacht) */
		private static final String CACHE_FORMAT_STRING="General";
		/** Maximale Ganzzahl, die gecacht wird. */
		private static final int MAX_CACHE=1_000_000;

		/** Cache für die Ganzzahlen von 0 bis einschließlich {@link #MAX_CACHE} */
		private final String[] cache=new String[MAX_CACHE+1];

		/**
		 * Konstruktor der Klasse
		 */
		public CachedDataFormatter() {
			/*
			 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
			 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public String formatRawCellContents(double value, int formatIndex, String formatString) {
			if (formatIndex!=CACHE_FORMAT_INDEX || !formatString.equals(CACHE_FORMAT_STRING)) return super.formatRawCellContents(value,formatIndex,formatString);

			if ((value%1)!=0) return super.formatRawCellContents(value,formatIndex,formatString);
			final int i=(int)value;

			if (i>=0) {
				if (i<=MAX_CACHE) {
					String formated=cache[i];
					if (formated==null) formated=cache[i]=Integer.toString(i); /* formated=super.formatRawCellContents(value,formatIndex,formatString); */
					return formated;
				} else {
					return Integer.toString(i); /* formated=super.formatRawCellContents(value,formatIndex,formatString); */
				}
			} else {
				return super.formatRawCellContents(value,formatIndex,formatString);
			}
		}
	}

	/**
	 * Diese Klasse baut auf {@link ReadOnlySharedStringsTable} auf und versucht Ergebnisse
	 * zwischenzuspeichern, um Aufrufe von {@link #getItemAt} der Basisklasse zu vermeiden.
	 */
	private class CachedReadOnlySharedStringsTable extends ReadOnlySharedStringsTable {
		/**
		 * Konstruktor der Klasse
		 * @param pkg The {@link OPCPackage} to use as basis for the shared-strings table.
		 * @throws IOException If reading the data from the package fails.
		 * @throws SAXException if parsing the XML data fails.
		 */
		public CachedReadOnlySharedStringsTable(final OPCPackage pkg) throws IOException, SAXException {
			super(pkg);
		}

		/**
		 * Cache
		 */
		private final Map<Integer,RichTextString> cache=new HashMap<>();

		@Override
		public RichTextString getItemAt(int idx) {
			RichTextString result=cache.get(idx);
			if (result==null) cache.put(idx,result=super.getItemAt(idx));
			return result;
		}
	}
}

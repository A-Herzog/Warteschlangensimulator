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

import java.awt.Component;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.odftoolkit.simple.SpreadsheetDocument;
import org.odftoolkit.simple.TextDocument;

import com.linuxense.javadbf.DBFException;
import com.linuxense.javadbf.DBFReader;

import mathtools.distribution.swing.CommonVariables;

/**
 * Die Klasse {@link Table} kapselt eine Tabelle aus {@link String}-Objekten.
 * Die Klasse stellt Methoden zum Lesen und Schreiben von Tabellen-Dateien zur Verfügung.
 * @author Alexander Herzog
 * @version 4.5
 */
public final class Table implements Cloneable {
	/** Bezeichner beim Speichern für "wahr" */
	public static String BoolTrue="WAHR";
	/** Bezeichner beim Speichern für "falsch" */
	public static String BoolFalse="FALSCH";
	/** Name für Tabelle innerhalb einer Excel-Arbeitsmappe */
	public static String TableFileTableName="Daten";
	/** Bezeichner für Dateiformat "Alle Tabellendateien" (im Dateiauswahldialog) */
	public static String FileTypeAll="Alle Tabellendateien";
	/** Bezeichner für Dateiformat "Textdateien" (im Dateiauswahldialog) */
	public static String FileTypeText="Textdateien";
	/** Bezeichner für Dateiformat csv (im Dateiauswahldialog) */
	public static String FileTypeCSV="csv-Dateien";
	/** Bezeichner für Dateiformat csvr (im Dateiauswahldialog) */
	public static String FileTypeCSVR="csv-Dateien (für R)";
	/** Bezeichner für Dateiformat dif (im Dateiauswahldialog) */
	public static String FileTypeDIF="dif-Dateien";
	/** Bezeichner für Dateiformat sylk (im Dateiauswahldialog) */
	public static String FileTypeSYLK="sylk-Dateien";
	/** Bezeichner für Dateiformat dbf (im Dateiauswahldialog) */
	public static String FileTypeDBF="dbf-Datenbanken";
	/** Bezeichner für Dateiformat xls (im Dateiauswahldialog) */
	public static String FileTypeExcelOld="Excel 2003-Tabellen";
	/** Bezeichner für Dateiformat ods (im Dateiauswahldialog) */
	public static String FileTypeODS="OpenOffice/LibreOffice-Tabellen";
	/** Bezeichner für Dateiformat xlsx (im Dateiauswahldialog) */
	public static String FileTypeExcel="Excel-Tabellen";
	/** Bezeichner für Dateiformat SQLite (im Dateiauswahldialog) */
	public static String FileTypeSQLite="SQLite-Datenbanken";
	/** Bezeichner für Dateiformat docx (im Dateiauswahldialog) */
	public static String FileTypeWord="Word-Text mit eingebetteter Tabelle";
	/** Bezeichner für Dateiformat html (im Dateiauswahldialog) */
	public static String FileTypeHTML="html-Dateien";
	/** Bezeichner für Dateiformat tex (im Dateiauswahldialog) */
	public static String FileTypeTex="LaTeX-Dateien";
	/** Fehlermeldung "Die angegebene Startzelle \"%s\" ist ungültig." */
	public static String LoadErrorFirstCellInvalid="Die angegebene Startzelle \"%s\" ist ungültig.";
	/** Fehlermeldung "Die angegebene Endzelle \"%s\" ist ungültig." */
	public static String LoadErrorLastCellInvalid="Die angegebene Endzelle \"%s\" ist ungültig.";
	/** Fehlermeldung "Der durch %s und %s eingegrenzte Bereich muss entweder eine Zeile oder eine Spalte sein." */
	public static String LoadErrorCellRangeInvalid="Der durch %s und %s eingegrenzte Bereich muss entweder eine Zeile oder eine Spalte sein.";
	/** Fehlermeldung "Die Zelle %s liegt außerhalb der Tabelle. Die Tabelle geht nur bis %s." */
	public static String LoadErrorCellNotInTable="Die Zelle %s liegt außerhalb der Tabelle. Die Tabelle geht nur bis %s.";
	/** Fehlermeldung "Der Wert der Zelle %s \"%s\" ist keine gültige Zahl." */
	public static String LoadErrorCellValueNaN="Der Wert der Zelle %s \"%s\" ist keine gültige Zahl.";
	/** Titel der Tabelle beim Speichern im html- und im LaTeX-Format  */
	public static String ExportTitle="Simulator";

	/**
	 * Maximale Anzahl an Zeilen beim Export als Excel-Datei.
	 */
	public static final int MAX_EXCEL_ROW_COUNT=524_288; /* =2^19. Excel kann 2^20=1_048_576, aber das ist für POI zu viel. */

	/**
	 * Speicherhaltung in Zeilen- oder in Spaltenform
	 * @author Alexander Herzog
	 */
	public enum IndexMode {
		/** Der erste Index gibt die Zeile an. */
		ROWS,

		/** Der erste Index gibt die Spalte an. */
		COLS
	}

	/**
	 * Speichermodus
	 * @author Alexander Herzog
	 */
	public enum SaveMode {
		/** Tabulator-getrennte Werte */
		SAVEMODE_TABS(true,true,new String[]{".TXT",".TSV"}),

		/** Comma Separated Values (CSV) Excel-Variante */
		SAVEMODE_CSV(true,true,new String[]{".CSV"}),

		/** Comma Separated Values (CSV) R-Variante */
		SAVEMODE_CSVR(true,true,new String[]{".CSVR"}),

		/** Excel-XLSX-Format */
		SAVEMODE_XLSX(true,true,new String[]{".XLSX"}),

		/** Altes Excel-XLS-Format */
		SAVEMODE_XLS(true,true,new String[]{".XLS"}),

		/** Open/LibreOffice-ODS-Tabellenformat */
		SAVEMODE_ODS(true,true,new String[]{".ODS"}),

		/** In eine DOCX-Datei eingebettete Tabelle */
		SAVEMODE_DOCX(false,true,new String[]{".DOCX"}),

		/** In eine ODT-Datei eingebettete Tabelle */
		/* SAVEMODE_ODT(false,true,new String[]{".ODT"}), - funktioniert bei mehreren Tabellen nicht richtig */

		/** Tabelle in HTML-Datei */
		SAVEMODE_HTML(false,true,new String[]{".HTML",".HTM"}),

		/** SQLite-Datenbanktabelle */
		SAVEMODE_SQLITE(true,false,new String[]{".SQLITE3",".SQLITE",".S3DB",".DB",".DB3"}),

		/** dBase-Datenbanken (DBF) */
		SAVEMODE_DBF(true,false,new String[]{".DBF"}),

		/** Data Interchange Format (DIF) */
		SAVEMODE_DIF(true,true,new String[]{".DIF"}),

		/** SYmbolic LinK (SYLK) */
		SAVEMODE_SYLK(true,true,new String[]{".SLK",".SYLK"}),

		/** LaTeX-Datei */
		SAVEMODE_TEX(false,true,new String[]{".TEX"}),

		/** Tabelle als CSV-Datei oder als Tabulator-getrennte Werte speichern in Abhängigkeit vom Dateinamen. */
		SAVEMODE_BYFILENAME(true,true,null);

		/**
		 * Können Tabellen in diesem Form geladen werden?
		 */
		public final boolean loadable;

		/**
		 * Können Tabellen in diesem Form gespeichert werden?
		 */
		public final boolean saveable;

		/**
		 * Ist <code>true</code>, wenn es sich nicht um ein durch eine Dateiendung definiertes Format handelt.
		 */
		public final boolean metaFormat;

		/**
		 * Namen der zugehörigen Dateinamenserweiterungen (jeweils inkl. ".") in Großbuchstaben
		 */
		private final String[] extensionsUpper;

		/**
		 * Konstruktor der Klasse
		 * @param loadable	Können Tabellen in diesem Form geladen werden?
		 * @param saveable	Können Tabellen in diesem Form gespeichert werden?
		 * @param extensionsUpper	Namen der zugehörigen Dateinamenserweiterungen (jeweils inkl. ".") in Großbuchstaben
		 */
		SaveMode(final boolean loadable, final boolean saveable, final String[] extensionsUpper) {
			this.loadable=loadable;
			this.saveable=saveable;
			metaFormat=(extensionsUpper==null);
			this.extensionsUpper=extensionsUpper;
		}

		/**
		 * Prüft, ob das aktuelle Format zu dem Dateinamen passt.
		 * @param fileName	Dateiname, bei dem geprüft werden soll, ob dieser zu dem Format gehört.
		 * @return	Gibt an, ob der Dateiname zu dem aktuellen Format passt.
		 */
		public boolean fileNameMatch(final String fileName) {
			if (metaFormat) return false;
			if (fileName==null) return false;
			final String fileNameUpper=fileName.trim().toUpperCase();
			for (String test: extensionsUpper) if (fileNameUpper.endsWith(test)) return true;
			return false;
		}
	}

	/**
	 * Gibt an, ob der erste Index die Spalten oder die Zeilen bezeichnen soll.
	 * @see Table.IndexMode
	 */
	private final IndexMode mode;

	/**
	 * Inhalt der Tabelle
	 * @see #mode
	 */
	private final List<List<String>> data;

	/**
	 * Constructor der Klasse <code>Table</code>
	 * (Es wird angenommen, dass sich der erste Index auf die Zeilen beziehen soll, d.h. der
	 * Aufruf dieses Constructors ist gleichwertig zu <code>Table(Table.ROWS);</code>.)
	 */
	public Table() {
		this(IndexMode.ROWS);
	}

	/**
	 * Constructor der Klasse <code>Table</code>
	 * @param mode Gibt an, ob der erste Index die Spalten oder die Zeilen bezeichnen soll.
	 */
	public Table(final IndexMode mode) {
		this.mode=(mode!=null)?mode:IndexMode.ROWS;
		data=new ArrayList<>();
	}

	/**
	 * Constructor der Klasse <code>Table</code>
	 * @param mode	Gibt an, ob der erste Index die Spalten oder die Zeilen bezeichnen soll.
	 * @param data	Daten, die in die Tabelle kopiert werden sollen.
	 */
	public Table(final IndexMode mode, final List<List<String>> data) {
		this(mode);
		setData(data);
	}

	/**
	 * Constructor der Klasse <code>Table</code>
	 * @param mode	Gibt an, ob der erste Index die Spalten oder die Zeilen bezeichnen soll.
	 * @param data	Daten, die in die Tabelle kopiert werden sollen.
	 */
	public Table(final IndexMode mode, final String[][] data) {
		this(mode);
		setData(data);
	}

	/**
	 * Constructor der Klasse <code>Table</code>
	 * @param data	Daten im csv-Format, die in die Tabelle kopiert werden sollen.
	 */
	public Table(final String data) {
		this(IndexMode.ROWS);
		setData(data);
	}

	/**
	 * Gibt an, ob der erste Index die Zeilen oder die Spalten repräsentiert.
	 * @return	ROWS oder COLS.
	 */
	public IndexMode getMode() {
		return mode;
	}

	/**
	 * Liefert die Größe der Tabelle.
	 * @param level	Erste (level=0) oder zweite (level=1) Index-Ebene abfragen.
	 * @return	Anzahl der Zeilen oder Anzahl der Spalten.
	 */
	public int getSize(final int level) {
		switch (level) {
		case 0: return data.size();
		case 1: if (data.isEmpty()) return 0; else return data.get(0).size();
		default: return 0;
		}
	}

	/**
	 * Liefert die kompletten Tabellendaten zurück.
	 * @return	<code>List</code>-Objekt, welches die Daten enthält.
	 * @see #setData(List)
	 * @see #setData(String)
	 * @see #setData(String[][])
	 * @see #getDataArray()
	 */
	public List<List<String>> getData() {
		return data;
	}

	/**
	 * Liefert die kompletten Tabellendaten zurück.
	 * @return Array-Objekt, welches die Daten enthält.
	 * @see #setData(List)
	 * @see #setData(String)
	 * @see #setData(String[][])
	 * @see #getData()
	 */
	public String[][] getDataArray() {
		String[][] s=new String[data.size()][];
		for (int i=0;i<data.size();i++) {
			List<String> v=data.get(i);
			s[i]=new String[v.size()];
			for (int j=0;j<v.size();j++) s[i][j]=v.get(j);
		}
		return s;
	}

	/**
	 * Ersetzt die bisherigen Tabellendaten durch die neuen Daten
	 * @param newData	Neuer Tabelleninhalt
	 * @see #getData()
	 * @see #load(String[][])
	 */
	public void setData(final List<List<String>> newData) {
		clear();
		for (int i=0;i<newData.size();i++) addLine(newData.get(i));
		makeSquare();
	}

	/**
	 * Ersetzt die bisherigen Tabellendaten durch die neuen Daten
	 * @param newData	Neuer Tabelleninhalt
	 * @see #getData()
	 * @see #load(String[][])
	 */
	public void setData(final String[][] newData) {
		clear();
		for (int i=0;i<newData.length;i++) addLine(newData[i]);
		makeSquare();
	}

	/**
	 * Ersetzt die bisherigen Tabellendaten durch die neuen Daten
	 * @param newData	Neuer Tabelleninhalt im csv-Format
	 * @see #getData()
	 * @see #load(String)
	 */
	public void setData(final String newData) {
		if (newData==null) return;

		clear();

		if (mode==IndexMode.COLS) {
			final Table t=new Table(newData);
			final Table t2=t.transpose();
			load(t2.getData());
			return;
		}

		char divider=';';
		if (newData.indexOf('\t')>=0) divider='\t';

		int from=0;
		int pos=0;
		final int size=newData.length();
		while (pos<size) {
			if (newData.charAt(pos)=='\n') {
				if (from==pos) data.add(new ArrayList<>()); else {
					data.add(fromCSVDirect(newData,from,pos,divider));
				}
				from=pos+1;
			}
			pos++;
		}
		if (from<size) data.add(fromCSVDirect(newData,from,size,divider));

		/*
		 * Benötigt mehr Speicher:
		final String[] lines=newData.split("\n");
		final int size=lines.length;
		for (int i=0;i<size;i++) data.add(fromCSV(lines[i]));
		 */

		makeSquare();
	}

	/**
	 * Liefert eine Datenzeile; ob dies eine Spalte oder eine Zeile ist, hängt vom Typ der Tabelle ab.
	 * @param index	Index der Datenzeile.
	 * @return	<code>List</code>, der die Datenzeile enthält.
	 * @see #setLine(int, List)
	 * @see #setLine(int, String[])
	 */
	public List<String> getLine(final int index) {
		if (index<0 || index>=data.size()) return null; else return data.get(index);
	}

	/**
	 * Ersetzt den Inhalt einer Datenzeile durch eine neue; ob dies eine Spalte oder eine Zeile ist, hängt vom Typ der Tabelle ab.
	 * @param index	Index der Datenzeile.
	 * @param newLine	Neue Datenzeile.
	 */
	public void setLine(final int index, final List<String> newLine) {
		if (index<0) return;
		while (index>=data.size()) data.add(new ArrayList<>());
		final List<String> list=new ArrayList<>(newLine);
		data.set(index,list);
	}

	/**
	 * Ersetzt den Inhalt einer Datenzeile durch eine neue; ob dies eine Spalte oder eine Zeile ist, hängt vom Typ der Tabelle ab.
	 * @param index	Index der Datenzeile.
	 * @param newLine	Neue Datenzeile.
	 */
	public void setLine(final int index, final String[] newLine) {
		if (index<0) return;
		while (index>=data.size()) data.add(new ArrayList<>());
		final List<String> v=new ArrayList<>(newLine.length);
		for (int i=0;i<newLine.length;i++) v.add(newLine[i]);
		data.set(index,v);
	}

	/**
	 * Fügt eine Datenzeile an die Tabelle an.
	 * Je nach Tabellenmodus kann es sich dabei um eine Zeile oder eine Spalte handeln.
	 * @param newLine	Anzufügende Datenzeile.
	 */
	public void addLine(final List<String> newLine) {
		final List<String> list=new ArrayList<>((newLine!=null)?newLine.size():0);
		if (newLine!=null) list.addAll(newLine);
		data.add(list);
	}

	/**
	 * Fügt eine Datenzeile an die Tabelle an.
	 * Je nach Tabellenmodus kann es sich dabei um eine Zeile oder eine Spalte handeln.
	 * @param newLine	Anzufügende Datenzeile.
	 */
	public void addLine(final String[] newLine) {
		final List<String> v=new ArrayList<>((newLine!=null)?newLine.length:0);
		if (newLine!=null) for (int i=0;i<newLine.length;i++) v.add(newLine[i]);
		data.add(v);
	}

	/**
	 * Fügt eine Datenzeile an die Tabelle an.
	 * Je nach Tabellenmodus kann es sich dabei um eine Zeile oder eine Spalte handeln.
	 * @param newLine	Anzufügende Datenzeile.
	 */
	public void addLine(final double[] newLine) {
		final List<String> line=new ArrayList<>();

		if (newLine!=null) for (double value: newLine) line.add(NumberTools.formatNumberMax(value));
		data.add(line);
	}

	/**
	 * Fügt eine Datenzeile an die Tabelle an.
	 * Je nach Tabellenmodus kann es sich dabei um eine Zeile oder eine Spalte handeln.
	 * @param newLine	Anzufügende Datenzeile.
	 */
	public void addLine(final long[] newLine) {
		final List<String> line=new ArrayList<>();

		final StringBuilder sb=new StringBuilder();
		if (newLine!=null) for (long value: newLine) line.add(NumberTools.formatLongNoGrouping(value,sb));
		data.add(line);
	}

	/**
	 * Fügt eine Datenzeile an die Tabelle an.
	 * Je nach Tabellenmodus kann es sich dabei um eine Zeile oder eine Spalte handeln.
	 * @param newLine	Anzufügende Datenzeile.
	 */
	public void addLine(final int[] newLine) {
		final List<String> line=new ArrayList<>();

		final StringBuilder sb=new StringBuilder();
		if (newLine!=null) for (int value: newLine) line.add(NumberTools.formatLongNoGrouping(value,sb));
		data.add(line);
	}

	/**
	 * Fügt mehrere Datenzeilen an die Tabelle an.
	 * Je nach Tabellenmodus kann es sich dabei um Zeilen oder Spalten handeln.
	 * @param newLines	Anzufügende Datenzeilen.
	 */
	public void addLines(final List<List<String>> newLines) {
		final int size=newLines.size();
		for (int i=0;i<size;i++) addLine(newLines.get(i));
	}

	/**
	 * Fügt mehrere Datenzeilen an die Tabelle an.
	 * Je nach Tabellenmodus kann es sich dabei um Zeilen oder Spalten handeln.
	 * @param newLines	Anzufügende Datenzeilen.
	 */
	public void addLines(final String[][] newLines) {
		for (int i=0;i<newLines.length;i++) addLine(newLines[i]);
	}

	/**
	 * Fügt eine Datenzeile am angegebenen Index in die Tabelle ein.
	 * Je nach Tabellenmodus kann es sich dabei um eine Zeile oder eine Spalte handeln.
	 * @param newLine	Einzufügende Datenzeile.
	 * @param index	Position, an der die Datenzeile eingefügt werden soll.
	 */
	public void insertLine(final String[] newLine, final int index) {
		final List<String> v=new ArrayList<>(newLine.length);
		for (int i=0;i<newLine.length;i++) v.add(newLine[i]);
		data.add(index,v);
	}

	/**
	 * Liefert den Wert einer einzelnen Zelle
	 * @param index1	Erste Tabellenindex
	 * @param index2	Zweiter Tabellenindex
	 * @return	Wert der angegebenen Zelle
	 * @see #setValue(int, int, String)
	 */
	public String getValue(final int index1, final int index2) {
		if (index1<0 || index1>=data.size()) return "";
		if (index2<0 || index2>=data.get(index1).size()) return "";
		return data.get(index1).get(index2);
	}

	/**
	 * Setzt den Wert einer Zelle
	 * @param index1	Erster Tabellenindex
	 * @param index2	Zweiter Tabellenindex
	 * @param value	Neuer Wert für die entsprechende Zelle
	 * @see #getValue(int, int)
	 */
	public void setValue(final int index1, final int index2, final String value) {
		if (index1<0 || index2<0) return;

		while (data.size()<=index1) data.add(new ArrayList<>());

		final List<String> v=data.get(index1);
		while (v.size()<=index2) v.add("");

		v.set(index2,value);
	}

	@Override
	public String toString() {
		return toStringTabs();
	}

	/**
	 * Gibt die Tabelle als Tabulator-getrennte Spalten aus
	 * @param output	Ausgabeobjekt
	 * @throws IOException	Reicht Exceptions des Ausgabeobjektes durch
	 */
	public void toStringTabs(final Appendable output) throws IOException {
		final List<List<String>> data;
		if (mode==IndexMode.COLS) {
			Table t=transpose();
			data=t.data;
		} else {
			data=this.data;
		}

		final int size1=data.size();
		for (int i=0;i<size1;i++) {
			final List<String> v=data.get(i);
			final int size2=v.size();
			for (int j=0;j<size2;j++) {
				output.append(v.get(j));
				if (j<size2-1) output.append("\t");
			}
			if (i<size1-1) output.append("\n");
		}
	}

	/**
	 * Liefert die gesamte Tabelle als Tabulator-getrennte Spalten zurück
	 * @return	Tabelle als Text
	 */
	public String toStringTabs() {
		final StringBuilder result=new StringBuilder();
		try {
			toStringTabs(result);
		} catch (IOException e) {
			return "";
		}
		return result.toString();
	}

	/**
	 * Gibt die Tabelle im CSV-Format (im Excel-Format) aus
	 * @param output	Ausgabeobjekt
	 * @throws IOException	Reicht Exceptions des Ausgabeobjektes durch
	 */
	public void toStringCSV(final Appendable output) throws IOException {
		final int size=data.size();
		int maxCols=0;
		for (int i=0;i<size;i++) maxCols=Math.max(maxCols,data.get(i).size());

		final StringBuilder line=new StringBuilder();
		for (int i=0;i<size;i++) {
			output.append(toCSV(data.get(i),maxCols,line));
			if (i<size-1) output.append('\n');
		}
	}

	/**
	 * Liefert die gesamte Tabelle im CSV-Format (im Excel-Format) zurück
	 * @return	Tabelle als Text
	 */
	public String toStringCSV() {
		final StringBuilder result=new StringBuilder();
		try {
			toStringCSV(result);
		} catch (IOException e) {
			return "";
		}
		return result.toString();
	}

	/**
	 * Gibt die Tabelle im CSV-Format (im R-Format) aus
	 * @param output	Ausgabeobjekt
	 * @throws IOException	Reicht Exceptions des Ausgabeobjektes durch
	 */
	public void toStringCSVR(final Appendable output) throws IOException {
		final int size=data.size();
		int maxCols=0;
		for (int i=0;i<size;i++) maxCols=Math.max(maxCols,data.get(i).size());

		for (int i=0;i<size;i++) {
			output.append(toCSVR(data.get(i),maxCols));
			if (i<size-1) output.append("\n");
		}
	}

	/**
	 * Liefert die gesamte Tabelle im CSV-Format (im R-Format) zurück
	 * @return	Tabelle als Text
	 */
	public String toStringCSVR() {
		final StringBuilder result=new StringBuilder();
		try {
			toStringCSVR(result);
		} catch (IOException e) {
			return "";
		}
		return result.toString();
	}

	@Override
	public Table clone() {
		return new Table(mode,data);
	}

	/**
	 * Liefert eine transponierte Fassung der Tabelle.<br>
	 * Dabei wird auch <code>mode</code> geändert, d.h. die Tabelle als solches
	 * wird nicht transponiert, sondern nur ihre Darstellung im Speicher.
	 * @return	Transponierte Fassung der Tabelle.
	 */
	public Table transpose() {
		return transpose(false);
	}

	/**
	 * Liefert eine transponierte Fassung der Tabelle.
	 * Dabei wird optional auch <code>mode</code> geändert, d.h. in diesem Fall wird die Tabelle als solches
	 * wird nicht transponiert, sondern nur ihre Darstellung im Speicher.
	 * @param full	Tabelle tatsächlich transponieren (<code>true</code>) und nicht nur die Darstellung im Speicher verändern (<code>false</code>)
	 * @return	Transponierte Fassung der Tabelle.
	 */
	public Table transpose(final boolean full) {
		final Table t;
		if (mode==IndexMode.COLS) {
			t=new Table(full?IndexMode.COLS:IndexMode.ROWS);
		} else {
			t=new Table(full?IndexMode.ROWS:IndexMode.COLS);
		}

		final int size1=data.size();
		for (int i=0;i<size1;i++) {
			final List<String> row=data.get(i);
			final int size2=row.size();
			for (int j=0;j<size2;j++) t.setValue(j,i,row.get(j));
		}
		return t;
	}

	/**
	 * Löscht den Inhalt der Tabelle;
	 */
	public void clear() {
		data.clear();
	}

	/**
	 * Lädt den Inhalt der Tabelle aus einem Workbook-Sheet
	 * @param sheet	Tabellen-Sheet, aus dem die Daten geladen werden sollen
	 */
	public void loadFromSheet(final Sheet sheet) {
		clear();

		final int firstRowNum=sheet.getFirstRowNum();
		final int lastRowNum=sheet.getLastRowNum();

		((ArrayList<List<String>>)data).ensureCapacity(firstRowNum);
		for (int i=0;i<firstRowNum;i++) data.add(new ArrayList<>());

		for (int i=firstRowNum;i<=lastRowNum;i++) {
			final Row row=sheet.getRow(i);
			final ArrayList<String> newRow=new ArrayList<>();
			if (row!=null) {
				final int lastCellNum=row.getLastCellNum();
				final int firstCellNum=row.getFirstCellNum();
				newRow.ensureCapacity(lastCellNum+1);
				for (int j=0;j<=lastCellNum;j++) {
					if (j<firstCellNum) {newRow.add(""); continue;}
					final Cell cell=row.getCell(j);
					if (cell==null) {newRow.add(""); continue;}

					final CellType type=cell.getCellType();

					if (type==CellType.STRING) {
						newRow.add(cell.getStringCellValue());
					}

					if (type==CellType.NUMERIC) {
						if (DateUtil.isCellDateFormatted(cell)) {
							newRow.add(DateFormat.getDateTimeInstance().format(cell.getDateCellValue()));
						} else {
							newRow.add(NumberTools.formatNumberMax(cell.getNumericCellValue()));
						}
					}

					if (type==CellType.BOOLEAN) {
						if (cell.getBooleanCellValue()) newRow.add(BoolTrue); else newRow.add(BoolFalse);
					}

					if (type==CellType.FORMULA) {
						try {
							if (DateUtil.isCellDateFormatted(cell)) {
								newRow.add(DateFormat.getDateTimeInstance().format(cell.getDateCellValue()));
							} else {
								newRow.add(NumberTools.formatNumberMax(cell.getNumericCellValue()));
							}
						} catch (Exception e) {
							newRow.add(cell.getCellFormula());
						}
					}

				}
			}
			data.add(newRow);
		}

		makeSquare();
	}

	/**
	 * Lädt den Inhalt der Tabelle aus einer ODS-Tabelle
	 * @param table	Tabelle, aus der die Daten geladen werden sollen
	 */
	public void loadFromSheet(org.odftoolkit.simple.table.Table table) {
		clear();

		final List<org.odftoolkit.simple.table.Row> rows=table.getRowList();

		final int rowCount=rows.size();
		final int colCount=table.getColumnCount();

		((ArrayList<List<String>>)data).ensureCapacity(rowCount);

		for (int i=0;i<rowCount;i++) {
			final org.odftoolkit.simple.table.Row row=rows.get(i);
			final ArrayList<String> newRow=new ArrayList<>();
			newRow.ensureCapacity(colCount);
			for (int j=0;j<colCount;j++) {
				final org.odftoolkit.simple.table.Cell cell=row.getCellByIndex(j);
				if (cell==null) {newRow.add(""); continue;}
				final String type=cell.getValueType();
				if (type==null) {newRow.add(""); continue;}

				if (type.equals("float")) {
					newRow.add(NumberTools.formatNumberMax(cell.getDoubleValue()));
					continue;
				}

				if (type.equals("string")) {
					newRow.add(cell.getStringValue());
					continue;
				}

				if (type.equals("percentage")) {
					newRow.add(NumberTools.formatPercent(cell.getPercentageValue(),3));
					continue;
				}

				if (type.equals("boolean")) {
					if (cell.getBooleanValue()) newRow.add(BoolTrue); else newRow.add(BoolFalse);
					continue;
				}

				if (type.equals("currency")) {
					newRow.add(NumberTools.formatNumberMax(cell.getCurrencyValue()));
					continue;
				}

				if (type.equals("date")) {
					newRow.add(DateFormat.getDateTimeInstance().format(cell.getDateValue()));
					continue;
				}

				if (type.equals("time")) {
					newRow.add(DateFormat.getDateTimeInstance().format(cell.getTimeValue()));
					continue;
				}
			}
			data.add(newRow);
		}

		makeSquare();
	}

	/**
	 * Kopiert den angegebenen Inhalt in die Tabelle.
	 * @param newData	Neuer Inhalt der Tabelle.
	 * @see #setData(List)
	 * @see #getData()
	 */
	public void load(final List<List<String>> newData) {
		setData(newData);
	}

	/**
	 * Kopiert den angegebenen Inhalt in die Tablle.
	 * @param newData	Neuer Inhalt der Tabelle.
	 * @see #setData(String[][])
	 * @see #getData()
	 */
	public void load(final String[][] newData) {
		setData(newData);
	}

	/**
	 * Kopiert den angegebenen Inhalt in die Tablle.
	 * @param newData	Neuer Inhalt der Tabelle im csv-Format.
	 * @see #setData(String)
	 * @see #getData()
	 */
	public void load(final String newData) {
		setData(newData);
	}

	/*
	private final List<String> fromCSVClassic(String line) {
		final List<String> csvCells=new ArrayList<String>();

		String divider=";";
		if (line.indexOf('\t')>=0) divider="\t";

		while (!line.isEmpty()) {
			String cell="";
			if (line.length()>0 && line.charAt(0)=='"') {
				line=line.substring(1);
				int endBorder=-1;
				boolean lastWasBorder=false;
				final int len=line.length();
				for (int i=0;i<len;i++) {
					char c=line.charAt(i);
					if (c=='"') {lastWasBorder=!lastWasBorder; continue;}
					if (lastWasBorder) {endBorder=i-1; break;}
				}
				if (endBorder==-1) {
					if (lastWasBorder) cell=line.substring(0,len-1); else cell=line;
					cell=cell.replace("\"\"","\"");
					line="";
				} else {
					cell=line.substring(0,endBorder); line=line.substring(endBorder+1);
					if (line.startsWith(divider)) {
						if (len==1) line=""; else line=line.substring(1);
					}
				}
			} else {
				int i=line.indexOf(divider);
				if (i<0) {
					cell=line;
					line="";
				} else {
					final int len=line.length();
					if (i==len-1) {
						cell=(len>1)?line.substring(0,len-1):"";
						line="";
					} else {
						if (i==0) {
							cell="";
							line=(len>1)?line.substring(1):"";
						} else {
							cell=line.substring(0,i);
							line=line.substring(i+1);
						}
					}
				}
			}
			csvCells.add(cell);
		}
		return csvCells;
	}
	 */

	/**
	 * Caches eines {@link StringBuilder}, um diesen nicht wieder neu anlegen zu müssen.
	 * @see #fromCSVDirect(String, int, int, char)
	 */
	private StringBuilder csvLoadBuilder;

	/*
	private final List<String> fromCSVSemiDirect(final String line, final int lineFrom, final int lineTo, final char divider) {
		final List<String> csvCells=new ArrayList<String>();
		if (csvLoadBuilder==null) csvLoadBuilder=new StringBuilder();
		csvLoadBuilder.setLength(0);

		int pos=lineFrom;
		boolean inText=false;
		while (pos<lineTo) {
			char c=line.charAt(pos);

			if (c=='"') {
				if (inText) {
					if (pos<lineTo-1 && line.charAt(pos+1)=='"') {csvLoadBuilder.append(c); pos++;} else inText=false;
				} else {
					inText=true;
				}
				pos++;
				continue;
			}

			if (c==divider && !inText) {
				if (csvLoadBuilder.length()>0) csvCells.add(csvLoadBuilder.toString()); else csvCells.add("");
				csvLoadBuilder.setLength(0);
				pos++;
				continue;
			}

			csvLoadBuilder.append(c);
			pos++;
		}
		if (csvLoadBuilder.length()>0) csvCells.add(csvLoadBuilder.toString());

		return csvCells;
	}
	 */

	/**
	 * Zerlegt eine CSV-Zeile in die einzelnen Zellen.
	 * Das Trennzeichen muss dabei vorgegeben werden.
	 * @param line	CSV-Zeile
	 * @param lineFrom	Zeichen (inklusive) ab dem die Verarbeitung beginnen soll
	 * @param lineTo	Zeichen (exklusive) bis zu dem die Verarbeitung erfolgen soll
	 * @param divider	CSV-Trennerzeichen (z.B. Semikollon oder Tabulator)
	 * @return	Liste mit den einzelnen Zellen der Zeile
	 */
	private List<String> fromCSVDirect(final String line, final int lineFrom, final int lineTo, final char divider) {
		final List<String> csvCells=new ArrayList<>();

		StringBuilder sb=null;
		int pos=lineFrom;
		boolean inText=false;
		int cellStart=pos;

		while (pos<lineTo) {
			char c=line.charAt(pos);

			if (c=='"') {
				if (inText) {
					if (pos<lineTo-1 && line.charAt(pos+1)=='"') {
						if (sb!=null) sb.append(c);
						pos++;
					} else {
						inText=false;
					}
				} else {
					inText=true;
					if (csvLoadBuilder==null) csvLoadBuilder=new StringBuilder();
					sb=csvLoadBuilder;
					sb.setLength(0);
					if (cellStart<pos) sb.append(line.substring(Math.max(0,cellStart),pos));
					cellStart=-1;
				}
				pos++;
				continue;
			}

			if (c==divider && !inText) {
				if (cellStart>=0) {
					if (cellStart<pos) csvCells.add(line.substring(cellStart,pos)); else csvCells.add("");
				} else {
					if (sb!=null && sb.length()>0) csvCells.add(sb.toString());
					sb=null;
				}
				pos++;
				cellStart=pos;
				continue;
			}

			if (sb!=null) sb.append(c);
			pos++;
		}

		if (cellStart>=0) {
			if (cellStart<lineTo) csvCells.add(line.substring(cellStart,lineTo)); else csvCells.add("");
		} else {
			if (sb!=null && sb.length()>0) csvCells.add(sb.toString());
		}

		return csvCells;
	}

	/**
	 * Wandelt eine CSV-Zeile in eine Tabellenzeile um
	 * @param line	CSV-Zeile
	 * @return	Tabellenzeile
	 */
	private List<String> fromCSV(final String line) {
		char divider=';';
		if (line.indexOf('\t')>=0) divider='\t';
		return fromCSVDirect(line,0,line.length(),divider);
	}

	/**
	 * Wandelt eine CSV-Zeile (aus R) in eine Tabellenzeile um
	 * @param line	CSV-Zeile (aus R)
	 * @return	Tabellenzeile
	 */
	private List<String> fromCSVR(final String line) {
		char divider=',';
		if (line.indexOf('\t')>=0) divider='\t';
		return fromCSVDirect(line,0,line.length(),divider);
	}
	/**
	 * Liefert den Dateityp der Tabellendatei gemäß der Dateiendung
	 * @param file	Datei, von der der Typ bestimmt werden soll
	 * @param onlyLoadableFormats	Gibt nur Formate zurück, die auch geladen werden können
	 * @param onlySaveableFormats	Gibt nur Formate zurück, in denen auch gespeichert werden kann
	 * @return	Dateityp (eine der <code>SAVEMODE_*</code>-Konstanten)
	 */
	public static SaveMode getSaveModeFromFileName(final File file, final boolean onlyLoadableFormats, final boolean onlySaveableFormats) {
		if (file==null) return SaveMode.SAVEMODE_TABS;

		final String fileName=file.getName();
		for (SaveMode mode: SaveMode.values()) {
			if (!mode.loadable && onlyLoadableFormats) continue;
			if (!mode.saveable && onlySaveableFormats) continue;
			if (mode.fileNameMatch(fileName)) return mode;
		}

		return SaveMode.SAVEMODE_TABS;
	}

	/**
	 * Listet alle in der SQLite-Datenbank enthaltenen Tabellen auf.
	 * @param dbStatement	Statement, welches die Datenbank repräsentiert
	 * @return	String-Array, welches die Namen der Tabellen enthält
	 */
	public static String[] getSQLiteTables(final Statement dbStatement) {
		try {
			try (ResultSet result=dbStatement.executeQuery("SELECT tbl_name FROM sqlite_master WHERE type='table';")) {
				List<String> tables=new ArrayList<>();
				while (result.next()) tables.add(result.getString(1));
				return tables.toArray(new String[0]);
			}
		} catch (SQLException e) {return new String[0];}
	}

	/**
	 * Versucht eine Tabelle aus einer SQL-Datenbank zu laden
	 * @param dbStatement	Datenbankverbindung
	 * @param tableName	Name der zu ladenden Tabelle
	 * @return	Liefert im Erfolgsfall <code>true</code>
	 */
	public boolean load(final Statement dbStatement, final String tableName) {
		try {
			try (ResultSet result=dbStatement.executeQuery("SELECT * FROM "+tableName)) {

				final ResultSetMetaData meta=result.getMetaData();
				final int colCount=meta.getColumnCount();

				while (result.next()) {
					final List<String> row=new ArrayList<>();
					for (int i=0;i<colCount;i++) row.add(result.getString(i+1));
					addLine(row);
				}
			}
			return true;
		} catch (SQLException e) {return false;}
	}

	/**
	 * Entfernt Escape-Kommandos aus einer DIF-Text-Zelle
	 * @param text	DIF-Text-Zelle
	 * @return	Enthaltener Text
	 */
	private String unescapeDIFText(String text) {
		if (text==null) return "";
		if (text.isEmpty()) return "";
		if (text.charAt(0)=='"' && text.charAt(text.length()-1)=='"') {
			text=text.substring(1,text.length()-1);
		}
		return text.replaceAll("\"\"","\"");
	}

	/**
	 * Versucht eine Tabelle aus einer DIF-Datei zu laden
	 * @param lines	Zeilen der DIF-Datei
	 * @return	Liefert im Erfolgsfall <code>true</code>
	 */
	public boolean loadDIF(final List<String> lines) {
		List<String> row=null;
		int skip=0;
		boolean dataMode=false;
		boolean dataLineOne=true;
		int cellType=-1;
		double cellValue=0;
		boolean stopProcessing=false;
		boolean justStarted=true;

		for (String line: lines) {

			if (skip>0) {skip--; continue;}
			final String cmd=line.toUpperCase().trim();

			if (!dataMode) {
				/* Header */
				if (cmd.equals("TABLE") || cmd.equals("VECTORS") || cmd.equals("TUPLES")) {skip=2; continue;}
				if (cmd.equals("DATA")) {dataMode=true; skip=2; continue;}
			} else {
				/* Datenbereich */
				if (dataLineOne) {
					int index=line.indexOf(',');
					if (index<1 || index==line.length()-1) continue;
					final String cmd1=line.substring(0,index);
					final String cmd2=line.substring(index+1);
					final Integer I=NumberTools.getInteger(cmd1);
					final Double D=NumberTools.getDouble(cmd2);
					if (I==null || D==null) continue;
					cellType=I.intValue();
					cellValue=D.doubleValue();
					dataLineOne=false;
				} else {
					switch (cellType) {
					case -1:
						/* Befehl */
						if (cmd.equals("BOT")) {
							if (row==null) {
								if (justStarted) {
									justStarted=false;
								} else {
									data.add(new ArrayList<>());
								}
							} else {
								data.add(row);
							}
							row=null;
						}
						if (cmd.equals("EOD")) {
							stopProcessing=true;
						}
						break;
					case 0:
						/* Zahl */
						if (row==null) row=new ArrayList<>();
						row.add(NumberTools.formatNumberMax(cellValue));
						break;
					case 1:
						/* Text */
						if (row==null) row=new ArrayList<>();
						row.add(unescapeDIFText(line));
						break;
					}
					dataLineOne=true;
				}
			}
			if (stopProcessing) break;
		}

		if (row!=null && !row.isEmpty()) data.add(row);
		makeSquare();

		return true;
	}

	/**
	 * Verarbeitet eine SYLK-Tabellenzelle
	 * @param parts	Elemente einer C-Zelle nach "C;"
	 */
	private void processSYLKCell(final String[] parts) {
		if (parts==null || parts.length<3) return;

		/* Zeile und Spalte */

		int col=-1;
		int row=-1;

		parts[0]=parts[0].trim();
		if (parts[0].length()>1) {
			final Long L=NumberTools.getPositiveLong(parts[0].substring(1));
			if (L!=null) {
				if (parts[0].toUpperCase().startsWith("X")) col=L.intValue()-1;
				if (parts[0].toUpperCase().startsWith("Y")) row=L.intValue()-1;
			}
		}

		if (parts[1].length()>1) {
			final Long L=NumberTools.getPositiveLong(parts[1].substring(1));
			if (L!=null) {
				if (parts[1].toUpperCase().startsWith("X")) col=L.intValue()-1;
				if (parts[1].toUpperCase().startsWith("Y")) row=L.intValue()-1;
			}
		}

		if (col<0 || row<0) return;

		/* Zelleninhalt */

		String cell;
		if (parts.length>3) {
			final StringBuilder sb=new StringBuilder();
			sb.append(parts[2]);
			for (int i=3;i<parts.length;i++) {
				sb.append(';');
				sb.append(parts[i]);
			}
			cell=sb.toString();
		} else {
			cell=parts[2];
		}

		cell=cell.trim();
		if (!cell.toUpperCase().startsWith("K")) return;
		cell=cell.substring(1).trim();

		if (cell.startsWith("\"") && cell.endsWith("\"") && cell.length()>=2) {
			/* String */
			cell=cell.substring(1,cell.length()-1).trim();
		} else {
			/* Zahl */
			final Double D=NumberTools.getDouble(NumberTools.systemNumberToLocalNumber(cell));
			if (D!=null) cell=NumberTools.formatNumberMax(D.doubleValue());
		}

		/* Wert in Tabelle eintragen */

		setValue(row,col,cell);
	}

	/**
	 * Versucht eine Tabelle aus einer SYLK-Datei zu laden
	 * @param lines	Zeilen der SYLK-Datei
	 * @return	Liefert im Erfolgsfall <code>true</code>
	 */
	public boolean loadSYLK(final List<String> lines) {
		if (lines!=null) for (String line: lines) {
			line=line.trim();
			if (line.toUpperCase().startsWith("ID;")) continue;
			if (line.toUpperCase().startsWith("E")) break;
			if (line.toUpperCase().startsWith("C;") && line.length()>2) {
				final String[] parts=line.substring(2).split(";");
				if (parts.length>=3) processSYLKCell(parts);
			}
		}

		makeSquare();
		return true;
	}

	/**
	 * Versucht eine Tabelle aus einer DBF-Datenbank-Datei zu laden
	 * @param stream	Eingabe-Stream der die Daten enthält
	 * @return	Liefert im Erfolgsfall <code>true</code>
	 */
	public boolean loadDBF(final InputStream stream) {
		if (stream==null) return false;
		try {
			try(final DBFReader reader=new DBFReader(stream)) {

				Object[] rowObjects;
				while ((rowObjects=reader.nextRecord())!=null) {
					final String[] row=new String[rowObjects.length];
					for (int i=0;i<rowObjects.length;i++) {
						if (rowObjects[i]==null) {row[i]="0"; continue;}
						if (rowObjects[i] instanceof String) {row[i]=((String)rowObjects[i]).trim(); continue;}
						if (rowObjects[i] instanceof Double) {row[i]=NumberTools.formatNumberMax((Double)rowObjects[i]); continue;}
						if (rowObjects[i] instanceof BigDecimal) {row[i]=NumberTools.formatNumberMax(((BigDecimal)rowObjects[i]).doubleValue()); continue;}
						if (rowObjects[i] instanceof Boolean) {row[i]=(((Boolean)rowObjects[i]).booleanValue())?BoolTrue:BoolFalse; continue;}
						if (rowObjects[i] instanceof Date) {row[i]=DateFormat.getDateTimeInstance().format((Date)rowObjects[i]); continue;}
						if (rowObjects[i] instanceof byte[]) {row[i]=new String((byte[])rowObjects[i]); continue;}
						row[i]=rowObjects[i].toString();
					}
					addLine(row);
				}

				makeSquare();
			}
		} catch (DBFException e) {return false;}
		return true;
	}

	/**
	 * Versucht eine Tabelle aus einer DBF-Datenbank-Datei zu laden
	 * @param file	Eingabedatei
	 * @return	Liefert im Erfolgsfall <code>true</code>
	 */
	public boolean loadDBF(final File file) {
		if (file==null) return false;
		try (final InputStream stream=new FileInputStream(file)) {
			return loadDBF(stream);
		} catch (IOException e) {return false;}
	}

	/**
	 * Lädt den Inhalt der Tabelle aus einer Datei.
	 * @param file	Dateiname der Tabellendatei.
	 * @param saveMode	Gibt an, ob die Datei als CSV-Datei, mit Tabulatoren getrennt oder als Excel-Datei geladen werden soll.
	 * @return	Liefert <code>true</code> zurück, wenn die Tabelle erfolgreich geladen wurde.
	 * @see #save(File, SaveMode)
	 * @see #save(File)
	 */
	public boolean load(final File file, SaveMode saveMode) {
		if (file==null) return false;

		if (mode==IndexMode.COLS) {
			Table t=new Table(IndexMode.ROWS); if (!t.load(file,saveMode)) return false;
			Table t2=t.transpose();	load(t2.getData());	return true;
		}

		clear();

		if (saveMode==SaveMode.SAVEMODE_BYFILENAME) saveMode=getSaveModeFromFileName(file,true,false);

		if (saveMode==SaveMode.SAVEMODE_XLSX) {
			/*
			Verbraucht viel mehr Speicher:
			try {
				try (InputStream in=new BufferedInputStream(new FileInputStream(file))) {
					try (XSSFWorkbook wb=new XSSFWorkbook(in)) {
						if (wb.getNumberOfSheets()<1) return false;
						loadFromSheet(wb.getSheetAt(0));
					}
				}
			} catch (Exception e) {return false;}
			return true;
			 */
			final TableXLSXReader reader=new TableXLSXReader(file);
			return reader.processFirstTableOnly(this);
		}

		if (saveMode==SaveMode.SAVEMODE_XLS) {
			try {
				try (InputStream in=new BufferedInputStream(new FileInputStream(file))) {
					try (HSSFWorkbook wb=new HSSFWorkbook(in)) {
						if (wb.getNumberOfSheets()<1) return false;
						loadFromSheet(wb.getSheetAt(0));
					}
				}
			} catch (Exception e) {return false;}
			return true;
		}

		if (saveMode==SaveMode.SAVEMODE_ODS) {
			try (SpreadsheetDocument ods=SpreadsheetDocument.loadDocument(file)) {
				if (ods.getSheetCount()<1) return false;
				loadFromSheet(ods.getSheetByIndex(0));
			} catch (Exception e) {return false;}
			return true;
		}

		if (saveMode==SaveMode.SAVEMODE_SQLITE) {
			try {
				boolean b=false;
				try (Connection con=DriverManager.getConnection("jdbc:sqlite:"+file.toString())) {
					try (Statement dbStatement=con.createStatement()) {
						if (dbStatement==null) return false;
						final String[] tables=getSQLiteTables(dbStatement);
						if (tables==null) return false;
						if (tables.length>0) b=load(dbStatement,tables[0]);
					}
				}
				return b;
			} catch (SQLException e) {return false;}
		}

		if (saveMode==SaveMode.SAVEMODE_DIF) {
			final List<String> lines=loadTextLinesFromFile(file);
			if (lines==null) return false;
			return loadDIF(lines);
		}

		if (saveMode==SaveMode.SAVEMODE_SYLK) {
			final List<String> lines=loadTextLinesFromFile(file);
			if (lines==null) return false;
			return loadSYLK(lines);
		}

		if (saveMode==SaveMode.SAVEMODE_CSVR) {
			if (!loadTextLinesFromFile(file,line->data.add(fromCSVR(line)))) return false;
			makeSquare();
			return true;
		}

		/* saveMode==SaveMode.SAVEMODE_CSV + Fallback */
		if (!loadTextLinesFromFile(file,line->data.add(fromCSV(line)))) return false;
		makeSquare();
		return true;
	}

	/**
	 * Lädt einen Text aus einer UTF-8-formatierten Datei.
	 * @param input	Quelldatei aus der der Text geladen werden soll
	 * @return	Liefert im Erfolgsfall den Text, sonst <code>null</code>
	 */
	public static List<String> loadTextLinesFromFile(final File input) {
		try {
			return Files.readAllLines(input.toPath(),StandardCharsets.UTF_8);
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Lädt einen Text aus einer UTF-8-formatierten Datei.
	 * @param input	Quelldatei aus der der Text geladen werden soll
	 * @param lineProcessor	Callback zur direkten Verarbeitung der einzelnen Zeilen
	 * @return	Gibt an, ob das Laden erfolgreich war
	 */
	public static boolean loadTextLinesFromFile(final File input, final Consumer<String> lineProcessor) {
		try (InputStream stream=new FileInputStream(input)) {
			try (InputStreamReader reader=new InputStreamReader(stream,StandardCharsets.UTF_8)) {
				try (BufferedReader bufferedReader=new BufferedReader(reader)) {
					while (bufferedReader.ready()) {
						lineProcessor.accept(bufferedReader.readLine());
					}
					return true;
				}
			}
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * Lädt den Inhalt der Tabelle aus einer Datei.
	 * Der Dateityp wird gemäß der Dateiendung festgelegt.
	 * @param file	Dateiname der Tabellendatei.
	 * @return	Liefert <code>true</code> zurück, wenn die Tabelle erfolgreich geladen wurde.
	 * @see #save(File)
	 */
	public boolean load(final File file) {
		return load(file,SaveMode.SAVEMODE_BYFILENAME);
	}

	/**
	 * Verlängert einzelne Zeilen der Tabelle, so dass alle dieselbe Länge haben.
	 */
	public void makeSquare() {
		if (data==null || data.isEmpty()) return;

		/* Alle Zeilen auf dieselbe Länge bringen */
		int maxLen=0;
		for (List<String> row : data) maxLen=Math.max(maxLen,row.size());
		for (List<String> row : data) while (row.size()<maxLen) row.add("");

		/* Leere Zeilen am Ende entfernen */
		while (!data.isEmpty()) {
			final List<String> row=data.get(data.size()-1);
			boolean rowIsEmpty=true;
			for (String s : row) if (s!=null && !s.trim().isEmpty()) {rowIsEmpty=false; break;}
			if (!rowIsEmpty) break;
			data.remove(data.size()-1);
		}

		/* Leere Spalten am Ende entfernen */
		if (!data.isEmpty()) while (!data.get(0).isEmpty()) {
			boolean colIsEmpty=true;
			for (List<String> row : data) {String s=row.get(row.size()-1); if (s!=null && !s.trim().isEmpty()) {colIsEmpty=false; break;}}
			if (!colIsEmpty) break;
			for (List<String> row : data) row.remove(row.size()-1);
		}
	}

	/**
	 * Wandelt eine Tabellenzeile in einen CSV-String um
	 * @param line	Tabellenzeile
	 * @param maxCols	Anzahl an Spalten (enthält die Tabellenzeile weniger Spalten, so werden leere Spalten angefügt)
	 * @param recycleStringBuilder	Wiederzuverwendendes {@link StringBuilder}-Objekt (kann <code>null</code> sein, dann wird ein lokaler Builder angelegt)
	 * @return Tabellenzeile als CSV-String
	 */
	private static String toCSV(final List<String> line, final int maxCols, final StringBuilder recycleStringBuilder) {
		final StringBuilder sb;
		if (recycleStringBuilder==null) {
			sb=new StringBuilder();
		} else {
			sb=recycleStringBuilder;
			sb.setLength(0);
		}

		final int size=line.size();
		for (int i=0;i<size;i++) {
			final String cell=line.get(i);
			if (cell.indexOf('"')!=-1 || cell.indexOf(';')!=-1) {
				sb.append('"');
				sb.append(cell.replace("\"","\"\""));
				sb.append('"');
			} else {
				if (cell.equalsIgnoreCase("id")) {
					sb.append('"');
					sb.append(cell);
					sb.append('"');
				} else {
					sb.append(cell);
				}
			}
			if (i<size-1) sb.append(";");
		}

		for (int i=size+1;i<=maxCols;i++) sb.append(';');
		return sb.toString();
	}

	/**
	 * Wandelt eine Tabellenzeile in einen R-tauglichen CSV-String um
	 * @param line	Tabellenzeile
	 * @param maxCols	Anzahl an Spalten (enthält die Tabellenzeile weniger Spalten, so werden leere Spalten angefügt)
	 * @return Tabellenzeile als R-tauglicher CSV-String
	 */
	private static String toCSVR(final List<String> line, final int maxCols) {
		final StringBuilder sb=new StringBuilder();

		final int size=line.size();
		for (int i=0;i<size;i++) {
			String cell=line.get(i);
			cell=NumberTools.percentToFraction(cell);
			cell=NumberTools.localNumberToSystemNumber(cell);
			cell=TimeTools.localTimeToSystemTime(cell);
			cell=cell.replace("\"","\"\"");
			if (cell.indexOf('"')!=-1 || cell.indexOf(';')!=-1) cell='"'+cell+'"';
			if (cell.equalsIgnoreCase("id")) cell='"'+cell+'"';
			sb.append(cell);
			if (i<size-1) sb.append(",");
		}

		for (int i=size+1;i<=maxCols;i++) sb.append(",");
		return sb.toString();
	}

	/**
	 * Statische Hilfsroutine, die versucht, eine Zeichenkette
	 * beliebiger Form als Zahlenwert zu interpretieren.
	 * @param cell	Zu interpretierende Zeichenkette
	 * @return	Zahlenwert oder <code>null</code>, wenn eine Umwandlung nicht möglich war
	 */
	public static Double convertToNumeric(String cell) {
		boolean isPercent=false;

		if (cell.endsWith("%")) {
			isPercent=true;
			if (cell.endsWith(" %")) {
				cell=cell.substring(0,cell.length()-2);
			} else {
				cell=cell.substring(0,cell.length()-1);
			}
		}

		final Double D=NumberTools.getPlainDouble(cell);
		if (D==null) return null;
		if (isPercent) return D/100; else return D;
	}

	/**
	 * Speichert den Inhalt der aktuellen Tabelle als Workbook-Sheet
	 * @param workbook	Übergeordnete Arbeitsmappe
	 * @param sheet	Tabellen-Sheet, in der die Daten gespeichert werden sollen
	 */
	public void saveToSheet(final Workbook workbook, final Sheet sheet) {
		if (mode==IndexMode.COLS) {Table t=transpose(); t.saveToSheet(workbook,sheet); return;}

		final CellStyle stylePercent=workbook.createCellStyle();
		stylePercent.setDataFormat(workbook.createDataFormat().getFormat("0.0%"));

		final int rowCount=Math.min(data.size(),MAX_EXCEL_ROW_COUNT);
		for (int i=0;i<rowCount;i++) {
			final Row row=sheet.createRow(i);
			List<String> r=data.get(i);
			final int colCount=r.size();
			for (int j=0;j<colCount;j++) {
				final Cell cell=row.createCell(j);
				final String cellData=r.get(j);
				if (cellData.startsWith("=")) {
					cell.setCellFormula(cellData.substring(1));
				} else {
					final Double cellDataNumber=convertToNumeric(cellData);
					if (cellDataNumber==null) {
						cell.setCellValue(cellData);
					} else {
						cell.setCellValue(cellDataNumber);
						if (cellData.endsWith("%")) cell.setCellStyle(stylePercent);
					}
				}
			}
		}
	}

	/**
	 * Speichert den Inhalt der aktuellen Tabelle als ODS-Tabelle
	 * @param table	Tabelle innerhalb der ODS-Arbeitsmappe, in der die Daten gespeichert werden sollen
	 */
	public void saveToSheet(org.odftoolkit.simple.table.Table table) {
		if (mode==IndexMode.COLS) {Table t=transpose(); t.saveToSheet(table); return;}

		final int oldRows=table.getRowCount();
		if (oldRows>0) table.removeRowsByIndex(0,oldRows);

		final int rowCount=Math.min(data.size(),MAX_EXCEL_ROW_COUNT);
		final int colCountTable=(data.size()==0)?1:data.stream().map(row->row.size()).max(Integer::compare).get();
		final List<org.odftoolkit.simple.table.Row> firstRow=table.appendRows(1);
		table.appendColumns(colCountTable);
		final List<org.odftoolkit.simple.table.Row> rows=table.appendRows(rowCount-1);
		rows.add(0,firstRow.get(0));

		for (int i=0;i<rowCount;i++) {
			final List<String> r=data.get(i);
			final int colCount=r.size();
			final org.odftoolkit.simple.table.Row row=rows.get(i);

			for (int j=0;j<colCount;j++) {
				final org.odftoolkit.simple.table.Cell cell=row.getCellByIndex(j);
				final String s=r.get(j);
				if (s.startsWith("=")) {
					cell.setFormula(s.substring(1));
				} else {
					final Double d=convertToNumeric(s);
					if (d==null) {
						cell.setStringValue(s);
					} else {
						if (s.endsWith("%")) {
							cell.setPercentageValue(d);
						} else {
							cell.setDoubleValue(d);
						}
					}
				}
			}
		}
	}

	/**
	 * Speichert den Inhalt der aktuellen Tabelle als XWPFDocument, in das eine Tabelle eingebettet ist
	 * @param doc	Dokument, in das die Tabelle eingefügt werden soll
	 */
	public void saveToDOCX(final XWPFDocument doc) {
		if (mode==IndexMode.COLS) {Table t=transpose(); t.saveToDOCX(doc); return;}

		final XWPFTable table=doc.createTable();

		for (int i=0;i<data.size();i++) {
			XWPFTableRow row;
			if (table.getRows().size()>i) row=table.getRows().get(i); else row=table.createRow();
			List<String> r=data.get(i);
			for (int j=0;j<r.size();j++) {
				XWPFTableCell cell;
				if (row.getTableCells().size()>j) cell=row.getTableCells().get(j); else cell=row.addNewTableCell();
				cell.setText(r.get(j));
			}
		}
	}

	/**
	 * Speichert den Inhalt der aktuellen Tabelle als TextDocument, in das eine Tabelle eingebettet ist
	 * @param odt	Dokument, in das die Tabelle eingefügt werden soll
	 */
	public void saveToODT(final TextDocument odt) {
		if (mode==IndexMode.COLS) {Table t=transpose(); t.saveToODT(odt); return;}

		final org.odftoolkit.simple.table.Table table=odt.addTable();
		saveToSheet(table);
	}

	/**
	 * Speichert die Tabelle als &lt;table&gt; HTML-Element (d.h. ohne Dokumentenrumpf)
	 * @return	Tabelle als &lt;table&gt; HTML-Element
	 */
	public String saveToHTML() {
		if (mode==IndexMode.COLS) {Table t=transpose(); return t.saveToHTML();}

		final StringBuilder sb=new StringBuilder();

		sb.append("<table>\n");
		for (int i=0;i<data.size();i++) {
			sb.append("  <tr>");
			for(String cell : data.get(i)) sb.append("<td>"+cell+"</td>");
			sb.append("</tr>\n");
		}
		sb.append("</table>\n");

		return sb.toString();
	}

	/**
	 * Speichert die Tabelle als LaTeX-Table-Objekt
	 * @return	Tabelle als LaTeX-Table-Objekt
	 */
	public String saveToLaTeX() {
		if (mode==IndexMode.COLS) {Table t=transpose(); return t.saveToLaTeX();}

		if (data.isEmpty()) return "";
		final int lines=data.size();
		final int cols=data.get(0).size();

		final StringBuilder sb=new StringBuilder();

		sb.append("\\begin{table}[H]\n");
		sb.append("\\begin{center}\n");
		sb.append("\\begin{tabular}{r");
		for (int i=1;i<cols;i++) sb.append("|r");
		sb.append("}\n");

		for (int i=0;i<lines;i++) {
			final List<String> line=data.get(i);
			for (int j=0;j<cols;j++) {
				sb.append(line.get(j).replace(",","{,}").replace("%","\\%"));
				if (j<cols-1) sb.append("&");
			}
			if (i<lines-1) sb.append("\\\\\n"); else sb.append("\n");
		}

		sb.append("\\end{tabular}\n");
		sb.append("\\end{center}\n");
		sb.append("\\caption{"+ExportTitle+"}\n");
		sb.append("%\\label{LabelForTable}\n");
		sb.append("\\end{table}\n");

		return sb.toString();
	}

	/**
	 * Speichert die Tabelle als DIF-Datei
	 * @param generator	Angabe zum Generator für den Dateikopf
	 * @return	Tabelle als DIF-Datei-Zeichenkette
	 */
	public String saveToDIF(final String generator) {
		if (mode==IndexMode.COLS) {Table t=transpose(); return t.saveToDIF(generator);}

		final StringBuilder sb=new StringBuilder();

		/* https://en.wikipedia.org/wiki/Data_Interchange_Format */
		sb.append("TABLE\n");
		sb.append("0,1\n");
		sb.append("\""+generator+"\"\n");
		sb.append("VECTORS\n");
		sb.append("0,"+((data.isEmpty())?0:data.get(0).size())+"\n");
		sb.append("\"\"\n");
		sb.append("TUPLES\n");
		sb.append("0,"+data.size()+"\n");
		sb.append("\"\"\n");
		sb.append("DATA\n");
		sb.append("0,0\n");
		sb.append("\"\"\n");

		for (List<String> line: data) {
			sb.append("-1,0\n");
			sb.append("BOT\n");
			for (String cell: line) {
				final Double D=NumberTools.getDouble(cell);
				if (D==null) {
					sb.append("1,0\n");
					final String s=cell.replace("\"","\"\"");
					sb.append("\""+s+"\"\n");
				} else {
					sb.append("0,"+NumberTools.formatNumber(D)+"\n"); /* Achtung: Dif verwendet lokales Format! */
					sb.append("V\n");
				}
			}
		}

		sb.append("-1,0\n");
		sb.append("EOD\n");

		return sb.toString();
	}

	/**
	 * Speichert die Tabelle als SYLK-Datei
	 * @return	Tabelle als SYLK-Datei-Zeichenkette
	 */
	public String saveToSYLK() {
		if (mode==IndexMode.COLS) {Table t=transpose(); return t.saveToSYLK();}

		final StringBuilder sb=new StringBuilder();

		/* https://en.wikipedia.org/wiki/SYmbolic_LinK_(SYLK) */
		sb.append("ID;P\n");
		sb.append("B;Y"+data.size()+";X"+((data.isEmpty())?0:data.get(0).size())+"\n");
		for (int i=0;i<data.size();i++) {
			final List<String> line=data.get(i);
			for (int j=0;j<line.size();j++) {
				final String cell=line.get(j);
				final Double D=NumberTools.getDouble(cell);
				final String val;
				if (D==null) {
					val="\""+cell.replaceAll("\"","\\\"")+"\"";
				} else {
					val=NumberTools.formatSystemNumber(D.doubleValue());
				}
				sb.append("C;Y"+(i+1)+";X"+(j+1)+";K"+val+"\n");
			}
		}
		sb.append("E\n");

		return sb.toString();
	}

	/**
	 * Speichert den Inhalt der Tabelle in einem Stream.
	 * @param stream	Ausgabestream
	 * @param saveMode	Gibt an, ob der Stream als CSV-Datei, mit Tabulatoren getrennt oder als Excel-Datei gespeichert werden soll.
	 * @return	Liefert <code>true</code> zurück, wenn die Tabelle erfolgreich gespeichert wurde.
	 */
	public boolean save(final OutputStream stream, SaveMode saveMode) {
		if (stream==null) return false;

		if (mode==IndexMode.COLS) {final Table t=transpose(); return t.save(stream,saveMode);}

		if (saveMode==SaveMode.SAVEMODE_TABS) {
			return saveTextToOutputStream(toStringTabs(),stream);
		}

		if (saveMode==SaveMode.SAVEMODE_CSV) {
			int maxCols=0;
			for (int i=0;i<data.size();i++) maxCols=Math.max(maxCols,data.get(i).size());
			return saveTextToOutputStream(toStringCSV(),stream);
		}

		if (saveMode==SaveMode.SAVEMODE_CSVR) {
			int maxCols=0;
			for (int i=0;i<data.size();i++) maxCols=Math.max(maxCols,data.get(i).size());
			return saveTextToOutputStream(toStringCSVR(),stream);
		}

		if (saveMode==SaveMode.SAVEMODE_ODS) {
			try (SpreadsheetDocument ods=SpreadsheetDocument.newSpreadsheetDocument()) {
				final org.odftoolkit.simple.table.Table table;
				if (ods.getTableList().isEmpty()) {
					table=ods.appendSheet(TableFileTableName);
				} else {
					table=ods.getSheetByIndex(0);
				}
				saveToSheet(table);
				ods.save(stream);
			} catch (Exception ex) {return false;}
			return true;
		}

		if (saveMode==SaveMode.SAVEMODE_XLSX) {
			try (Workbook wb=new XSSFWorkbook()) {
				final Sheet sheet=wb.createSheet(TableFileTableName);
				saveToSheet(wb,sheet);
				wb.write(stream);
			} catch (IOException e) {return false;}
			return true;
		}

		if (saveMode==SaveMode.SAVEMODE_XLS) {
			try (Workbook wb=new HSSFWorkbook()) {
				final Sheet sheet=wb.createSheet(TableFileTableName);
				saveToSheet(wb,sheet);
				wb.write(stream);
			} catch (IOException e) {return false;}
			return true;
		}

		if (saveMode==SaveMode.SAVEMODE_DOCX) {
			try (XWPFDocument doc=new XWPFDocument()) {
				saveToDOCX(doc);
				doc.write(stream);
			} catch (IOException e) {return false;}
			return true;
		}

		/*
		if (saveMode==SaveMode.SAVEMODE_ODT) {
			try(TextDocument odt=TextDocument.newTextDocument()) {
				saveToODT(odt);
				odt.save(file);
			} catch (Exception e) {return false;}
		}
		 */

		if (saveMode==SaveMode.SAVEMODE_HTML) {
			final StringBuilder html=new StringBuilder();
			html.append("<!DOCTYPE html>\n");
			html.append("<html>\n");
			html.append("<head>\n");
			html.append("  <meta charset=\"utf-8\">\n");
			html.append("  <title>"+ExportTitle+"</title>\n");
			html.append("  <meta name=\"author\" content=\"Alexander Herzog\">\n");
			html.append("  <style type=\"text/css\">\n");
			html.append("    body {font-family: Verdana, Lucida, sans-serif;}\n");
			html.append("    table {border: 1px solid black; border-collapse: collapse;}\n");
			html.append("    td {border: 1px solid black; padding: 2px 5px;}\n");
			html.append("  </style>\n");
			html.append("</head>\n");
			html.append("<body>\n");
			html.append(saveToHTML());
			html.append("</body>\n</html>\n");
			return saveTextToOutputStream(html.toString(),stream);
		}

		if (saveMode==SaveMode.SAVEMODE_TEX) {
			return saveTextToOutputStream(saveToLaTeX(),stream);
		}

		if (saveMode==SaveMode.SAVEMODE_DIF) {
			return saveTextToOutputStream(saveToDIF(ExportTitle),stream);
		}

		if (saveMode==SaveMode.SAVEMODE_SYLK) {
			return saveTextToOutputStream(saveToSYLK(),stream);
		}

		return false;
	}

	/**
	 * Speichert den Inhalt der Tabelle in einer Datei.
	 * @param file	Dateiname der Datei.
	 * @param saveMode	Gibt an, ob die Datei als CSV-Datei, mit Tabulatoren getrennt oder als Excel-Datei gespeichert werden soll.
	 * @return	Liefert <code>true</code> zurück, wenn die Tabelle erfolgreich gespeichert wurde.
	 * @see #load(File)
	 * @see #load(File, SaveMode)
	 */
	public boolean save(final File file, SaveMode saveMode) {
		if (file==null) return false;

		if (mode==IndexMode.COLS) {final Table t=transpose(); return t.save(file,saveMode);}

		if (saveMode==SaveMode.SAVEMODE_BYFILENAME) {
			saveMode=getSaveModeFromFileName(file,false,true);
		}

		if (saveMode==SaveMode.SAVEMODE_TABS) {
			try(OutputStream stream=new FileOutputStream(file)) {
				try (OutputStreamWriter writer=new OutputStreamWriter(stream,StandardCharsets.UTF_8)) {
					try (BufferedWriter bufferedWriter=new BufferedWriter(writer)) {
						bufferedWriter.write('\ufeff'); /* BOM - brauchen wir, damit Excel die UTF8-Datei auch als solche erkennt */
						toStringTabs(bufferedWriter);
						return true;
					}
				}
			} catch (IOException e) {
				return false;
			}
		}

		if (saveMode==SaveMode.SAVEMODE_CSV) {
			try(OutputStream stream=new FileOutputStream(file)) {
				try (OutputStreamWriter writer=new OutputStreamWriter(stream,StandardCharsets.UTF_8)) {
					try (BufferedWriter bufferedWriter=new BufferedWriter(writer)) {
						bufferedWriter.write('\ufeff'); /* BOM - brauchen wir, damit Excel die UTF8-Datei auch als solche erkennt */
						toStringCSV(bufferedWriter);
						return true;
					}
				}
			} catch (IOException e) {
				return false;
			}
		}

		if (saveMode==SaveMode.SAVEMODE_CSVR) {
			try(OutputStream stream=new FileOutputStream(file)) {
				try (OutputStreamWriter writer=new OutputStreamWriter(stream,StandardCharsets.UTF_8)) {
					try (BufferedWriter bufferedWriter=new BufferedWriter(writer)) {
						bufferedWriter.write('\ufeff'); /* BOM - brauchen wir, damit Excel die UTF8-Datei auch als solche erkennt */
						toStringCSVR(bufferedWriter);
						return true;
					}
				}
			} catch (IOException e) {
				return false;
			}
		}

		if (saveMode==SaveMode.SAVEMODE_ODS) {
			try (SpreadsheetDocument ods=SpreadsheetDocument.newSpreadsheetDocument()) {
				final org.odftoolkit.simple.table.Table table;
				if (ods.getTableList().isEmpty()) {
					table=ods.appendSheet(TableFileTableName);
				} else {
					table=ods.getSheetByIndex(0);
				}
				saveToSheet(table);
				ods.save(file);
			} catch (Exception ex) {return false;}
			return true;
		}

		if (saveMode==SaveMode.SAVEMODE_XLSX) {
			try (Workbook wb=new SXSSFWorkbook(100)) { /* Streaming-Variante */
				final Sheet sheet=wb.createSheet(TableFileTableName);
				saveToSheet(wb,sheet);
				try (FileOutputStream fo=new FileOutputStream(file)) {wb.write(fo);}
				if (wb instanceof SXSSFWorkbook) ((SXSSFWorkbook)wb).dispose();
			} catch (IOException e) {return false;}
			return true;
		}

		if (saveMode==SaveMode.SAVEMODE_XLS) {
			try (Workbook wb=new HSSFWorkbook()) {
				final Sheet sheet=wb.createSheet(TableFileTableName);
				saveToSheet(wb,sheet);
				try (FileOutputStream fo=new FileOutputStream(file)) {wb.write(fo);}
			} catch (IOException e) {return false;}
			return true;
		}

		if (saveMode==SaveMode.SAVEMODE_DOCX) {
			try (XWPFDocument doc=new XWPFDocument()) {
				saveToDOCX(doc);
				try(FileOutputStream out=new FileOutputStream(file)) {doc.write(out);} catch (FileNotFoundException e) {return false;}
			} catch (IOException e) {return false;}
			return true;
		}

		/*
		if (saveMode==SaveMode.SAVEMODE_ODT) {
			try(TextDocument odt=TextDocument.newTextDocument()) {
				saveToODT(odt);
				odt.save(file);
			} catch (Exception e) {return false;}
		}
		 */

		if (saveMode==SaveMode.SAVEMODE_HTML) {
			final StringBuilder html=new StringBuilder();
			html.append("<!DOCTYPE html>\n");
			html.append("<html>\n");
			html.append("<head>\n");
			html.append("  <meta charset=\"utf-8\">\n");
			html.append("  <title>"+ExportTitle+"</title>\n");
			html.append("  <meta name=\"author\" content=\"Alexander Herzog\">\n");
			html.append("  <style type=\"text/css\">\n");
			html.append("    body {font-family: Verdana, Lucida, sans-serif;}\n");
			html.append("    table {border: 1px solid black; border-collapse: collapse;}\n");
			html.append("    td {border: 1px solid black; padding: 2px 5px;}\n");
			html.append("  </style>\n");
			html.append("</head>\n");
			html.append("<body>\n");
			html.append(saveToHTML());
			html.append("</body>\n</html>\n");
			return saveTextToFile(html.toString(),file);
		}

		if (saveMode==SaveMode.SAVEMODE_TEX) {
			return saveTextToFile(saveToLaTeX(),file);
		}

		if (saveMode==SaveMode.SAVEMODE_DIF) {
			return saveTextToFile(saveToDIF(ExportTitle),file);
		}

		if (saveMode==SaveMode.SAVEMODE_SYLK) {
			return saveTextToFile(saveToSYLK(),file);
		}

		return false;
	}

	/**
	 * Lädt einen Text aus einer UTF-8-formatierten Datei.
	 * @param input	Quelldatei aus der der Text geladen werden soll
	 * @return	Liefert im Erfolgsfall den Text, sonst <code>null</code>
	 */
	public static String loadTextFromFile(final File input) {
		final StringBuilder text=new StringBuilder();
		try (BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(input),StandardCharsets.UTF_8))) {
			String line=null;
			while ((line=br.readLine())!=null) {
				text.append(line);
				text.append('\n');
			}
			return text.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Speichert einen Text in einer Datei im UTF-8-Format
	 * @param text	Zu speichernder Text
	 * @param file	Zieldatei (wird ggf. überschrieben)
	 * @return	Gibt an, ob das Speichern erfolgreich war.
	 */
	public static boolean saveTextToFile(final String text, final File file) {
		try {
			Files.write(file.toPath(),text.getBytes(StandardCharsets.UTF_8),StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	/**
	 * Speichert einen Text in einem UTF-8 formatierten Stream
	 * @param text	Zu speichernder Text
	 * @param stream	Ausgabestream
	 * @return	Gibt an, ob das Speichern erfolgreich war.
	 */
	public static boolean saveTextToOutputStream(final String text, final OutputStream stream) {
		try {
			stream.write(text.getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	/**
	 * Speichert den Inhalt der Tabelle in einer Datei.
	 * Ob die Daten Tabulator-getrennt, als CSV-Datei oder als Excel-Datei gespeichert wird, wird gemäß der
	 * Dateiendung entschieden.
	 * @param file	Dateiname der Datei.
	 * @return	Liefert <code>true</code> zurück, wenn die Tabelle erfolgreich gespeichert wurde.
	 * @see #load(File)
	 */
	public boolean save(final File file) {
		return save(file,SaveMode.SAVEMODE_BYFILENAME);
	}

	/**
	 * Speichert den Inhalt der Tabelle in einer Datei.
	 * Ob die Daten Tabulator-getrennt, als CSV-Datei oder als Excel-Datei gespeichert wird, wird gemäß der
	 * Dateiendung entschieden.
	 * @param file	Dateiname der Datei.
	 * @return	Liefert <code>true</code> zurück, wenn die Tabelle erfolgreich gespeichert wurde.
	 * @see #load(File)
	 */
	public boolean save(final String file) {
		return save((file==null)?null:new File(file),SaveMode.SAVEMODE_BYFILENAME);
	}

	/**
	 * Wird von {@link #getNumberArea(String, String)} und {@link #getDataArea(String, String)} ggf. mit einem
	 * Wert belegt, wenn ein Fehler aufgetreten ist. Kann dann über {@link #getAreaError()} ausgelesen werden.
	 */
	private String areaError=null;

	/**
	 * Liefert eine Beschreibung zu dem während des Aufrufs von <code>getNumberArea</code> aufgetretenen Fehlers.
	 * @return	Fehlerbeschreibung oder <code>null</code>, wenn <code>getNumberArea</code> erfolgreich ausgeführt wurde.
	 * @see #getNumberArea(String, String)
	 */
	public String getAreaError() {
		return areaError;
	}

	/**
	 * Wandelt eine Spaltenbezeichnung wie A in einen 0-basierenden wert um.
	 * @param colName	Spaltenbezeichner A,B,...,Y,Z,AA,AB,...
	 * @return	0-basierter Index der Spalte oder -1 im Falle eines Fehlers
	 */
	public static int numberFromColumnName(final String colName) {
		if (colName==null || colName.trim().isEmpty()) return -1;
		final char[] c=colName.trim().toUpperCase().toCharArray();
		int i=0,col=0;
		while (i<c.length) {
			if (c[i]<'A' || c[i]>'Z') break;
			col*=26;
			col+=(c[i]-'A')+1;
			i++;
		}
		if (i!=c.length) return -1;
		return col-1;
	}

	/**
	 * Wandelt eine Spaltenbezeichnung wie A in einen 0-basierenden wert um.
	 * @param colName	Spaltenbezeichner A,B,...,Y,Z,AA,AB,...
	 * @return	0-basierter Index der Spalte oder -1 im Falle eines Fehlers
	 */
	public static int numberFromColumnNameIgnoreRowNumbers(String colName) {
		if (colName==null) return -1;
		colName=colName.trim();
		if (colName.isEmpty()) return -1;
		final char[] c=colName.toCharArray();
		int i=0,col=0;
		while (i<c.length) {
			if (c[i]>='A' && c[i]<='Z') {
				col*=26;
				col+=(c[i]-'A')+1;
				i++;
				continue;
			}
			if (c[i]>='a' && c[i]<='z') {
				col*=26;
				col+=(c[i]-'a')+1;
				i++;
				continue;
			}
			break;
		}
		/* if (i!=c.length) return -1; - Zahlen am Ende werden ignoriert */
		return col-1;
	}

	/**
	 * Wandelt eine Zellenbeschreibung wie A1 in ein Array aus Zeilen- und Spaltennummer (jeweils 0-basierend) um.
	 * @param cellID	Bezeichner der Zelle
	 * @return	Zweielementiges Array aus Zeilen- und Spaltennummer (jeweils 0-basierend) oder <code>null</code> im Fehlerfall
	 */
	public static int[] cellIDToNumbers(final String cellID) {
		if (cellID==null || cellID.trim().isEmpty()) return null;
		final char[] c=cellID.trim().toUpperCase().toCharArray();
		int i=0,col=0;
		while (i<c.length) {
			if (c[i]<'A' || c[i]>'Z') break;
			col*=26;
			col+=(c[i]-'A')+1;
			i++;
		}
		if (i==0 || i==c.length) return null;

		final Integer row=NumberTools.getNotNegativeInteger(cellID.trim().substring(i));
		if (row==null || row<=0) return null;

		return new int[]{row-1,col-1};
	}

	/**
	 * Wandelt eine 0-basierende Spaltennummer in einen Bezeichner um.
	 * @param column	0-basierende Spaltennumer
	 * @return	Bezeichner der Spalte
	 */
	public static String columnNameFromNumber(final int column) {
		if (column<0) return "";
		if (column==0) return "A";
		String col="";
		int c=column;
		while (c!=0) {
			if (col.length()>0) c--;
			byte b=(byte)(c%26);
			char ch=(char)(b+'A');
			col=ch+col;
			c=c/26;
		}
		return col;
	}

	/**
	 * Wandelt ein Array aus Zeilen- und Spaltennummer (jeweils 0-basierend) in einen Zellenbezeichner um.
	 * @param cell	Zweielementiges Array aus Zeilen- und Spaltennummer (jeweils 0-basierend)
	 * @return	Bezeichner der Zelle
	 */
	public static String cellIDFromNumber(final int[] cell) {
		if (cell==null || cell.length!=2) return "";
		return columnNameFromNumber(cell[1])+(cell[0]+1);
	}

	/**
	 * Extrahiert einen Datenbereich aus der Tabelle.
	 * Der Bereich muss entweder entweder die Form einer Zeile oder die einer Spalte haben. Die Einträge müssen gültige Zahlen sein.
	 * @param cellStart	Anfangszelle
	 * @param cellEnd	Endzelle
	 * @return	Liefert im Erfolgsfall den Datenbereich zurück, sonst <code>null</code>. Im Fehlerfall kann über <code>getAreaError</code> eine Fehlerbeschreibung ermittelt werden.
	 * @see #getAreaError()
	 */
	public double[] getNumberArea(final String cellStart, final String cellEnd) {
		areaError=null;
		final int[] cell1=cellIDToNumbers(cellStart);
		final int[] cell2=cellIDToNumbers(cellEnd);
		if (cell1==null) {areaError=String.format(LoadErrorFirstCellInvalid,cellStart); return null;}
		if (cell2==null) {areaError=String.format(LoadErrorLastCellInvalid,cellEnd); return null;}
		if (cell1[0]!=cell2[0] && cell1[1]!=cell2[1]) {areaError=String.format(LoadErrorCellRangeInvalid,cellStart,cellEnd); return null;}

		if (cell1[0]==cell2[0]) {
			double[] data=new double[Math.abs(cell2[1]-cell1[1])+1];
			int dir=(cell2[1]>cell1[1])?1:-1;
			int nr=0;
			for (int i=cell1[1];;i+=dir) {
				if (getSize(0)<=cell1[0] || getSize(1)<=i) {areaError=String.format(LoadErrorCellNotInTable,cellIDFromNumber(new int[]{cell1[0],i}),cellIDFromNumber(new int[]{getSize(0)-1,getSize(1)-1})); return null;}
				final List<String> line=getLine(cell1[0]);
				String s=(line==null)?"":line.get(i);
				s=NumberTools.systemNumberToLocalNumber(s);
				Double d=NumberTools.getExtProbability(s);
				if (d==null) d=NumberTools.getDouble(s);
				if (d==null) {areaError=String.format(LoadErrorCellValueNaN,cellIDFromNumber(new int[]{cell1[0],i}),s); return null;}
				data[nr]=d;
				nr++;
				if (i==cell2[1]) break;
			}
			return data;
		} else {
			double[] data=new double[Math.abs(cell2[0]-cell1[0])+1];
			int dir=(cell2[0]>cell1[0])?1:-1;
			int nr=0;
			for (int i=cell1[0];;i+=dir) {
				if (getSize(0)<=i || getSize(1)<=cell1[1]) {areaError=String.format(LoadErrorCellNotInTable,cellIDFromNumber(new int[]{i,cell1[1]}),cellIDFromNumber(new int[]{getSize(0)-1,getSize(1)-1})); return null;}
				String s=getLine(i).get(cell1[1]);
				s=NumberTools.systemNumberToLocalNumber(s);
				Double d=NumberTools.getExtProbability(s);
				if (d==null) d=NumberTools.getDouble(s);
				if (d==null) {areaError=String.format(LoadErrorCellValueNaN,cellIDFromNumber(new int[]{i,cell1[1]}),s); return null;}
				data[nr]=d;
				nr++;
				if (i==cell2[0]) break;
			}
			return data;
		}
	}

	/**
	 * Extrahiert einen Datenbereich aus der Tabelle.
	 * Der Bereich muss entweder entweder die Form einer Zeile oder die einer Spalte haben. Die Einträge müssen gültige Zahlen sein.
	 * @param cellStart	Anfangszelle
	 * @param cellEnd	Endzelle
	 * @return	Liefert im Erfolgsfall den Datenbereich zurück, sonst <code>null</code>. Im Fehlerfall kann über <code>getAreaError</code> eine Fehlerbeschreibung ermittelt werden.
	 * @see #getAreaError()
	 */
	public String[] getDataArea(final String cellStart, final String cellEnd) {
		areaError=null;
		final int[] cell1=cellIDToNumbers(cellStart);
		final int[] cell2=cellIDToNumbers(cellEnd);
		if (cell1==null) {areaError=String.format(LoadErrorFirstCellInvalid,cellStart); return null;}
		if (cell2==null) {areaError=String.format(LoadErrorLastCellInvalid,cellEnd); return null;}
		if (cell1[0]!=cell2[0] && cell1[1]!=cell2[1]) {areaError=String.format(LoadErrorCellRangeInvalid,cellStart,cellEnd); return null;}

		if (cell1[0]==cell2[0]) {
			final String[] data=new String[Math.abs(cell2[1]-cell1[1])+1];
			int dir=(cell2[1]>cell1[1])?1:-1;
			int nr=0;
			for (int i=cell1[1];;i+=dir) {
				if (getSize(0)<=cell1[0] || getSize(1)<=i) {areaError=String.format(LoadErrorCellNotInTable,cellIDFromNumber(new int[]{cell1[0],i}),cellIDFromNumber(new int[]{getSize(0)-1,getSize(1)-1})); return null;}
				final List<String> line=getLine(cell1[0]);
				if (line!=null) data[nr]=line.get(i);
				nr++;
				if (i==cell2[1]) break;
			}
			return data;
		} else {
			final String[] data=new String[Math.abs(cell2[0]-cell1[0])+1];
			int dir=(cell2[0]>cell1[0])?1:-1;
			int nr=0;
			for (int i=cell1[0];;i+=dir) {
				if (getSize(0)<=i || getSize(1)<=cell1[1]) {areaError=String.format(LoadErrorCellNotInTable,cellIDFromNumber(new int[]{i,cell1[1]}),cellIDFromNumber(new int[]{getSize(0)-1,getSize(1)-1})); return null;}
				String s=getLine(i).get(cell1[1]);
				data[nr]=NumberTools.systemNumberToLocalNumber(s);
				nr++;
				if (i==cell2[0]) break;
			}
			return data;
		}
	}

	/**
	 * Zeigt einen Auswahldialog zum Laden von Tabellen an.
	 * @param parent	Elternkomponente des Dialogs
	 * @param title	Anzuzeigender Titel im Auswahldialog
	 * @return	Im Erfolgsfall wird der Dateiname zurückgegeben, sonst <code>null</code>
	 */
	public static File showLoadDialog(final Component parent, final String title) {
		return showLoadDialog(parent,title,null);
	}

	/**
	 * Zeigt einen Auswahldialog zum Laden von Tabellen an.
	 * @param parent	Elternkomponente des Dialogs
	 * @param title	Anzuzeigender Titel im Auswahldialog
	 * @param initialDirectory	Pfad, der anfänglich im Dialog ausgewählt sein soll (kann auch <code>null</code> sein)
	 * @return	Im Erfolgsfall wird der Dateiname zurückgegeben, sonst <code>null</code>
	 */
	public static File showLoadDialog(final Component parent, final String title, final File initialDirectory) {
		final JFileChooser fc;
		if (initialDirectory!=null) fc=new JFileChooser(initialDirectory.toString()); else {
			fc=new JFileChooser();
			CommonVariables.initialDirectoryToJFileChooser(fc);
		}
		fc.setDialogTitle(title);

		final FileFilter table=new FileNameExtensionFilter(FileTypeAll,"xlsx","xls","ods","txt","tsv","csv","csvr","sqlite3","sqlite","db","db3","s3db","dbf","dif");
		final FileFilter xlsx=new FileNameExtensionFilter(FileTypeExcel+" (*.xlsx)","xlsx");
		final FileFilter xls=new FileNameExtensionFilter(FileTypeExcelOld+" (*.xls)","xls");
		final FileFilter ods=new FileNameExtensionFilter(FileTypeODS+" (*.ods)","ods");
		final FileFilter txt=new FileNameExtensionFilter(FileTypeText+" (*.txt, *.tsv)","txt","tsv");
		final FileFilter csv=new FileNameExtensionFilter(FileTypeCSV+" (*.csv)","csv");
		final FileFilter csvr=new FileNameExtensionFilter(FileTypeCSVR+" (*.csvr)","csvr");
		final FileFilter sqlite=new FileNameExtensionFilter(FileTypeSQLite+" (*.sqlite3, *.sqlite, *.db, *.db3, *.s3db)","sqlite3","sqlite","db","db3","s3db");
		final FileFilter dbf=new FileNameExtensionFilter(FileTypeDBF+" (*.dbf)","dbf");
		final FileFilter dif=new FileNameExtensionFilter(FileTypeDIF+" (*.dif)","dif");
		final FileFilter sylk=new FileNameExtensionFilter(Table.FileTypeSYLK+" (*.slk, *.sylk)","slk","sylk");

		fc.addChoosableFileFilter(table);
		fc.addChoosableFileFilter(xlsx);
		fc.addChoosableFileFilter(xls);
		fc.addChoosableFileFilter(ods);
		fc.addChoosableFileFilter(txt);
		fc.addChoosableFileFilter(csv);
		fc.addChoosableFileFilter(csvr);
		fc.addChoosableFileFilter(sqlite);
		fc.addChoosableFileFilter(dbf);
		fc.addChoosableFileFilter(dif);
		fc.addChoosableFileFilter(sylk);

		fc.setFileFilter(table);
		if (fc.showOpenDialog(parent)!=JFileChooser.APPROVE_OPTION) return null;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();
		if (file.getName().indexOf('.')<0) {
			if (fc.getFileFilter()==xlsx) file=new File(file.getAbsoluteFile()+".xlsx");
			if (fc.getFileFilter()==xls) file=new File(file.getAbsoluteFile()+".xls");
			if (fc.getFileFilter()==ods) file=new File(file.getAbsoluteFile()+".ods");
			if (fc.getFileFilter()==txt) file=new File(file.getAbsoluteFile()+".txt");
			if (fc.getFileFilter()==csv) file=new File(file.getAbsoluteFile()+".csv");
			if (fc.getFileFilter()==csvr) file=new File(file.getAbsoluteFile()+".csvr");
			if (fc.getFileFilter()==sqlite) file=new File(file.getAbsoluteFile()+".sqlite3");
			if (fc.getFileFilter()==dbf) file=new File(file.getAbsoluteFile()+".dbf");
			if (fc.getFileFilter()==dif) file=new File(file.getAbsoluteFile()+".dif");
			if (fc.getFileFilter()==sylk) file=new File(file.getAbsoluteFile()+".sylk");
		}
		return file;
	}

	/**
	 * Zeigt einen Auswahldialog zum Speichern von Tabellen an.
	 * @param parent	Elternkomponente des Dialogs
	 * @param title	Anzuzeigender Titel im Auswahldialog
	 * @return	Im Erfolgsfall wird der Dateiname zurückgegeben, sonst <code>null</code>
	 */
	public static File showSaveDialog(final Component parent, final String title) {
		return showSaveDialog(parent,title,null,null,null);
	}

	/**
	 * Zeigt einen Auswahldialog zum Speichern von Tabellen an.
	 * @param parent	Elternkomponente des Dialogs
	 * @param title	Anzuzeigender Titel im Auswahldialog
	 * @param initialDirectory	Pfad, der anfänglich im Dialog ausgewählt sein soll (kann auch <code>null</code> sein)
	 * @return	Im Erfolgsfall wird der Dateiname zurückgegeben, sonst <code>null</code>
	 */
	public static File showSaveDialog(final Component parent, final String title, final File initialDirectory) {
		return showSaveDialog(parent,title,initialDirectory,null,null);
	}

	/**
	 * Zeigt einen Auswahldialog zum Speichern von Tabellen an.
	 * @param parent	Elternkomponente des Dialogs
	 * @param title	Anzuzeigender Titel im Auswahldialog
	 * @param initialDirectory	Pfad, der anfänglich im Dialog ausgewählt sein soll (kann auch <code>null</code> sein)
	 * @param customFilterName Zusätzlicher Eintrag in der Liste der möglichen Dateiformate (kann <code>null</code> sein)
	 * @param customFilterExt Dateinamenserweiterung für den zusätzlichen Eintrag in der Liste der möglichen Dateiformate (darf nicht <code>null</code> sein, wenn <code>customFilterName</code> nicht gleich <code>null</code> ist)
	 * @return	Im Erfolgsfall wird der Dateiname zurückgegeben, sonst <code>null</code>
	 */
	public static File showSaveDialog(final Component parent, final String title, final File initialDirectory, final String customFilterName, final String customFilterExt) {
		final JFileChooser fc;
		if (initialDirectory!=null) fc=new JFileChooser(initialDirectory.toString()); else {
			fc=new JFileChooser();
			CommonVariables.initialDirectoryToJFileChooser(fc);
		}
		fc.setDialogTitle(title);

		final FileFilter xlsx=new FileNameExtensionFilter(FileTypeExcel+" (*.xlsx)","xlsx");
		final FileFilter xls=new FileNameExtensionFilter(FileTypeExcelOld+" (*.xls)","xls");
		final FileFilter ods=new FileNameExtensionFilter(FileTypeODS+" (*.ods)","ods");
		final FileFilter txt=new FileNameExtensionFilter(FileTypeText+" (*.txt, *.tsv)","txt","tsv");
		final FileFilter csv=new FileNameExtensionFilter(FileTypeCSV+" (*.csv)","csv");
		final FileFilter csvr=new FileNameExtensionFilter(FileTypeCSVR+" (*.csvr)","csvr");
		final FileFilter dif=new FileNameExtensionFilter(FileTypeDIF+" (*.dif)","dif");
		final FileFilter sylk=new FileNameExtensionFilter(Table.FileTypeSYLK+" (*.slk, *.sylk)","slk","sylk");
		final FileFilter docx=new FileNameExtensionFilter(FileTypeWord+" (*.docx)","docx");
		final FileFilter html=new FileNameExtensionFilter(FileTypeHTML+" (*.html, *.htm)","html","htm");
		final FileFilter tex=new FileNameExtensionFilter(Table.FileTypeTex+" (*.tex)","tex");
		FileFilter custom=null; if (customFilterName!=null) custom=new FileNameExtensionFilter(customFilterName,customFilterExt);

		fc.addChoosableFileFilter(xlsx);
		fc.addChoosableFileFilter(xls);
		fc.addChoosableFileFilter(ods);
		fc.addChoosableFileFilter(txt);
		fc.addChoosableFileFilter(csv);
		fc.addChoosableFileFilter(csvr);
		fc.addChoosableFileFilter(dif);
		fc.addChoosableFileFilter(sylk);
		fc.addChoosableFileFilter(docx);
		fc.addChoosableFileFilter(html);
		fc.addChoosableFileFilter(tex);
		if (custom!=null) fc.addChoosableFileFilter(custom);

		fc.setFileFilter(xlsx);
		fc.setAcceptAllFileFilterUsed(false);

		if (fc.showSaveDialog(parent)!=JFileChooser.APPROVE_OPTION) return null;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();

		if (file.getName().indexOf('.')<0) {
			if (fc.getFileFilter()==xlsx) file=new File(file.getAbsoluteFile()+".xlsx");
			if (fc.getFileFilter()==xls) file=new File(file.getAbsoluteFile()+".xls");
			if (fc.getFileFilter()==ods) file=new File(file.getAbsoluteFile()+".ods");
			if (fc.getFileFilter()==txt) file=new File(file.getAbsoluteFile()+".txt");
			if (fc.getFileFilter()==csv) file=new File(file.getAbsoluteFile()+".csv");
			if (fc.getFileFilter()==csvr) file=new File(file.getAbsoluteFile()+".csvr");
			if (fc.getFileFilter()==dif) file=new File(file.getAbsoluteFile()+".dif");
			if (fc.getFileFilter()==sylk) file=new File(file.getAbsoluteFile()+".sylk");
			if (fc.getFileFilter()==docx) file=new File(file.getAbsoluteFile()+".docx");
			if (fc.getFileFilter()==html) file=new File(file.getAbsoluteFile()+".html");
			if (fc.getFileFilter()==tex) file=new File(file.getAbsoluteFile()+".tex");
			if (custom!=null && fc.getFileFilter()==custom) file=new File(file.getAbsoluteFile()+"."+customFilterExt);
		}
		return file;
	}

	/**
	 * Zeigt einen Auswahldialog zum Speichern von Tabellen an, in dem nur das xlsx-Format angeboten wird.
	 * @param parent	Elternkomponente des Dialogs
	 * @param title	Anzuzeigender Titel im Auswahldialog
	 * @return	Im Erfolgsfall wird der Dateiname zurückgegeben, sonst <code>null</code>
	 */
	public static File showSaveDialogXLSXonly(final Component parent, final String title) {
		return showSaveDialog(parent,title,null,null,null);
	}

	/**
	 * Zeigt einen Auswahldialog zum Speichern von Tabellen an, in dem nur das xlsx-Format angeboten wird.
	 * @param parent	Elternkomponente des Dialogs
	 * @param title	Anzuzeigender Titel im Auswahldialog
	 * @param initialDirectory	Pfad, der anfänglich im Dialog ausgewählt sein soll (kann auch <code>null</code> sein)
	 * @return	Im Erfolgsfall wird der Dateiname zurückgegeben, sonst <code>null</code>
	 */
	public static File showSaveDialogXLSXonly(final Component parent, final String title, final File initialDirectory) {

		final JFileChooser fc;
		if (initialDirectory!=null) fc=new JFileChooser(initialDirectory.toString()); else {
			fc=new JFileChooser();
			CommonVariables.initialDirectoryToJFileChooser(fc);
		}
		fc.setDialogTitle(title);

		final FileFilter xlsx=new FileNameExtensionFilter(FileTypeExcel+" (*.xlsx)","xlsx");

		fc.addChoosableFileFilter(xlsx);
		fc.setFileFilter(xlsx);
		fc.setAcceptAllFileFilterUsed(false);

		if (fc.showSaveDialog(parent)!=JFileChooser.APPROVE_OPTION) return null;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();

		if (file.getName().indexOf('.')<0) {
			if (fc.getFileFilter()==xlsx) file=new File(file.getAbsoluteFile()+".xlsx");
		}
		return file;
	}

	/**
	 * Prüft, ob es sich bei dem Dateinamen gemäß Dateiendung um eine Tabellendatei, die gelesen werden kann, handelt
	 * @param file	Name der zu untersuchenden Datei
	 * @return	Liefert <code>true</code> zurück, wenn die Dateiendung auf eine Tabellendatei schließen lässt.
	 */
	public static boolean isTableFileName(final File file) {
		final String fileName=file.getName();
		for (SaveMode mode: SaveMode.values()) {
			if (mode.loadable && mode.fileNameMatch(fileName)) return true;
		}
		return false;
	}
}
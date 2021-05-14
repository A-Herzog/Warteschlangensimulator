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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.odftoolkit.simple.SpreadsheetDocument;

import mathtools.distribution.swing.CommonVariables;

/**
 * Diese Klasse ermöglichst das Laden und Speichern mehrerer Tabellen in einer Exceldatei
 * @author Alexander Herzog
 * @version 1.5
 */
public final class MultiTable {
	/**
	 * Namen der enthaltenen Tabellen
	 * @see #size()
	 * @see #getNames()
	 */
	private final List<String> names=new ArrayList<>();

	/**
	 * Daten der enthaltenen Tabellen
	 * @see #size()
	 * @see #getTables()
	 */
	private final List<Table> tables=new ArrayList<>();

	/**
	 * Gibt an, wie viele Tabellen sich in der Arbeitsmappe befinden
	 * @return	Anzahl der Tabellen in der Arbeitsmappe
	 */
	public int size() {
		return names.size();
	}

	/**
	 * Liefert eine bestimmte Tabelle zurück
	 * @param nr	Nummer der Tabelle (0-basierend)
	 * @return	<code>Table</code>-Objekt oder <code>null</code>, wenn der Index außerhalb des gültigen Bereiches lag.
	 */
	public Table get(final int nr) {
		return (nr<0 || nr>=tables.size())?null:(tables.get(nr));
	}

	/**
	 * Liefert eine bestimmte Tabelle zurück
	 * @param name	Name der gesuchten Tabelle
	 * @return	<code>Table</code>-Objekt oder <code>null</code>, wenn es keine Tabelle mit dem angegebenen Namen gibt.
	 */
	public Table get(final String name) {
		final int nr=indexOf(name);
		return (nr<0)?null:(tables.get(nr));
	}

	/**
	 * Liefert den Namen einer bestimmten Tabelle zurück
	 * @param nr	Nummer der Tabelle
	 * @return	Name der Tabelle oder <code>null</code>, wenn der Index außerhalb des gültigen Bereiches lag.
	 */
	public String getName(final int nr) {
		return (nr<0 || nr>=names.size())?null:(names.get(nr));
	}

	/**
	 * Liefert die Namen aller Tabellen in der Arbeitsmappe zurück
	 * @return	Liste mit den Namen aller Tabellen; die Liste ist eine Kopie und kann beliebig verändert werden
	 */
	public List<String> getNames() {
		return new ArrayList<>(names);
	}

	/**
	 * Liefert alle Tabellen in der Arbeitsmappe zurück
	 * @return	Liste mit allen Tabellen; die Liste ist eine Kopie und kann beliebig verändert werden (die Tabellen darin beziehen sich aber auf die Originale in diesem Objekt)
	 */
	public List<Table> getTables() {
		return new ArrayList<>(tables);
	}

	/**
	 * Liefert die Nummer der Tabelle mit dem angegebenen Namen.
	 * @param name	Tabellenname, zu der die Nummer bestimmt werden soll.
	 * @return	Liefert die Nummer der Tabelle oder -1, wenn es keine Tabelle mit dem angegebenen Namen in der Arbeitsmappe gibt.
	 */
	public int indexOf(final String name) {
		if (name==null) return -1;
		if (name.isEmpty() && !tables.isEmpty()) return 0;
		for (int i=0;i<names.size();i++) if (names.get(i).equals(name)) return i;

		/* Auch Zahlenwerte (1-basierend) als Tabellenbezeichnungen erkennen. */
		final Long L=NumberTools.getPositiveLong(name);
		if (L!=null && L.intValue()<=names.size()) return (int)(L-1);

		return -1;
	}

	/**
	 * Legt eine neue Tabelle für eine bestimmte Tabellennummer fest.
	 * @param nr	Tabellennummer, unter der die neue Tabelle abgelegt werden soll
	 * @param table	Neue Tabelle
	 * @return	Gibt <code>true</code> zurück, wenn die Tabelle unter der angegebenen Nummer abgelegt werden konnte, oder <code>false</code>, wenn der Index außerhalb des gültigen Bereiches lag.
	 */
	public boolean set(final int nr, final Table table) {
		if (nr<0 || nr>=tables.size()) return false;
		tables.set(nr,table);
		return true;
	}

	/**
	 * Legt eine neue Tabelle unter einem bestehenden Namen ab.
	 * @param name	Name, unter der die neue Tabelle abgelegt werden soll
	 * @param table	Neue Tabelle
	 * @return	Gibt <code>true</code> zurück, wenn die Tabelle unter dem angegebenen Namen abgelegt werden konnte, oder <code>false</code>, wenn es keine Tabelle mit dem angegebenen Namen gibt.
	 */
	public boolean set(final String name, final Table table) {
		final int nr=indexOf(name);
		return (nr<0)?false:(set(nr,table));
	}

	/**
	 * Fügt eine Tabelle zu der Arbeitsmappe hinzu
	 * @param name	Name der Tabelle
	 * @param table	Tabellenobjeckt
	 */
	public void add(final String name, final Table table) {
		names.add(name);
		tables.add(table);
	}

	/**
	 * Fügt eine Tabelle zu der Arbeitsmappe hinzu
	 * @param name	Name der Tabelle
	 * @return	Neues Tabellenobjeckt
	 */
	public Table add(final String name) {
		final Table table=new Table();
		add(name,table);
		return table;
	}

	/**
	 * Entfernt eine Tabelle aus der Arbeitsmappe
	 * @param nr	Nummer der Tabelle, die entfernt werden soll
	 * @return	Gibt <code>true</code> zurück, wenn die Tabelle entfernt werden konnte, oder <code>false</code>, wenn der Index außerhalb des gültigen Bereiches lag.
	 */
	public boolean remove(final int nr) {
		if (nr<0 || nr>=tables.size()) return false;
		tables.remove(nr);
		names.remove(nr);
		return true;
	}

	/**
	 * Entfernt eine Tabelle aus der Arbeitsmappe
	 * @param name	Name der Tabelle, die entfernt werden soll
	 * @return Gibt <code>true</code> zurück, wenn die Tabelle entfernt werden konnte, oder <code>false</code>, wenn es keine Tabelle mit dem angegebenen Namen gibt.
	 */
	public boolean remove(final String name) {
		int nr=indexOf(name);
		return (nr<0)?false:(remove(nr));
	}

	/**
	 * Lädt den Inhalt der Tabelle aus einer Datei.
	 * @param file	Dateiname der Tabellendatei.
	 * @param saveMode	Gibt an, ob die Datei als CSV-Datei, mit Tabulatoren getrennt oder als Excel/OpenOffice-Datei geladen werden soll.
	 * @return	Liefert <code>true</code> zurück, wenn die Tabelle erfolgreich geladen wurde.
	 * @see #save(File)
	 * @see #save(File, mathtools.Table.SaveMode)
	 */
	public boolean load(final File file, Table.SaveMode saveMode) {
		if (file==null) return false;

		if (saveMode==Table.SaveMode.SAVEMODE_BYFILENAME) saveMode=Table.getSaveModeFromFileName(file,true,false);

		if (saveMode==Table.SaveMode.SAVEMODE_TABS || saveMode==Table.SaveMode.SAVEMODE_CSV) {
			Table table=new Table();
			if (!table.load(file,saveMode)) return false;
			add("",table);
			return true;
		}

		if (saveMode==Table.SaveMode.SAVEMODE_XLSX) {
			try {
				try (InputStream in=new FileInputStream(file)) {
					try (XSSFWorkbook wb=new XSSFWorkbook(in)) {
						if (wb.getNumberOfSheets()<1) return false;
						for (int i=0;i<wb.getNumberOfSheets();i++) {
							Table table=new Table();
							table.loadFromSheet(wb.getSheetAt(i));
							add(wb.getSheetName(i),table);
						}
					}
				}
			} catch (Exception e) {return false;}
			return true;
		}

		if (saveMode==Table.SaveMode.SAVEMODE_XLS) {
			try {
				try (InputStream in=new FileInputStream(file)) {
					try (HSSFWorkbook wb=new HSSFWorkbook(in)) {
						if (wb.getNumberOfSheets()<1) return false;
						for (int i=0;i<wb.getNumberOfSheets();i++) {
							Table table=new Table();
							table.loadFromSheet(wb.getSheetAt(i));
							add(wb.getSheetName(i),table);
						}
					}
				}
			} catch (Exception e) {return false;}
			return true;
		}

		/*
		if (saveMode==Table.SaveMode.SAVEMODE_ODS) {
			try {
				SpreadSheet spreadSheet=SpreadSheet.createFromFile(file);
				if (spreadSheet.getSheetCount()<1) return false;
				for (int i=0;i<spreadSheet.getSheetCount();i++) {
					Table table=new Table();
					table.loadFromSheet(spreadSheet.getSheet(i));
					add(spreadSheet.getSheet(i).getName(),table);
				}
			} catch (IOException e) {return false;}
			return true;
		}
		 */

		if (saveMode==Table.SaveMode.SAVEMODE_SQLITE) {
			try {
				try (Connection con=DriverManager.getConnection("jdbc:sqlite:"+file.toString())) {
					try (Statement dbStatement=con.createStatement()) {
						if (dbStatement==null) return false;
						final String[] tables=Table.getSQLiteTables(dbStatement);
						if (tables==null) return false;
						if (tables.length==0) return false;
						for (int i=0;i<tables.length;i++) {
							Table table=new Table();
							if (table.load(dbStatement,tables[i])) add(tables[i],table);
						}
					}
				}
				return true;
			} catch (SQLException e) {return false;}
		}

		return false;
	}

	/**
	 * Lädt den Inhalt der Arbeitsmappe aus einer Datei.
	 * Der Dateityp wird gemäß der Dateiendung festgelegt.
	 * @param file	Dateiname der Tabellendatei.
	 * @return	Liefert <code>true</code> zurück, wenn die Arbeitsmappe erfolgreich geladen wurde.
	 * @see #save(File)
	 * @see #save(File, mathtools.Table.SaveMode)
	 */
	public boolean load(final File file) {
		return load(file,Table.SaveMode.SAVEMODE_BYFILENAME);
	}

	/**
	 * Versucht eine Tabelle aus einem {@link ByteArrayInputStream} zu laden.<br>
	 * Unterstützt werden dabei Plain-Text-Formate und xlsx-Dateien.
	 * @param stream	Eingabestream
	 * @return	Liefert im Erfolgsfall <code>true</code> zurück
	 */
	private boolean loadByteArray(final ByteArrayInputStream stream) {
		if (stream.available()<2) return false;

		final byte[] start=new byte[2];
		try {
			if (stream.read(start)!=2) return false;
		} catch (IOException e) {
			return false;
		}
		stream.reset();

		if (start[0]=='P' && start[1]=='K') {
			/* xlsx */
			try {
				try (XSSFWorkbook wb=new XSSFWorkbook(stream)) {
					if (wb.getNumberOfSheets()<1) return false;
					for (int i=0;i<wb.getNumberOfSheets();i++) {
						Table table=new Table();
						table.loadFromSheet(wb.getSheetAt(i));
						add(wb.getSheetName(i),table);
					}
				}
			} catch (Exception e) {return false;}
			return true;
		} else {
			/* Plain */
			final int size=stream.available();
			final byte[] data=new byte[size];
			try {
				if (stream.read(data)!=size) return false;
			} catch (IOException e) {
				return false;
			}
			final String tableTest=new String(data);
			final Table table=new Table();
			table.load(tableTest);
			add("",table);
			return true;
		}
	}

	/**
	 * Versucht eine Tabelle aus einem Stream zu laden.<br>
	 * Unterstützt werden dabei Plain-Text-Formate und xlsx-Dateien.
	 * @param stream	Eingabestream
	 * @return	Liefert im Erfolgsfall <code>true</code> zurück
	 */
	public boolean loadStream(final InputStream stream) {
		if (stream instanceof ByteArrayInputStream) return loadByteArray((ByteArrayInputStream)stream);

		try {
			final int size=stream.available();
			final byte[] data=new byte[size];
			int read=0;
			while (read<size) read+=stream.read(data,read,size-read);
			try (ByteArrayInputStream byteArray=new ByteArrayInputStream(data)) {
				return loadByteArray(byteArray);
			}
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * Speichert den Inhalt der Arbeitsmappe in einer html-Datei.
	 * @param file	Dateiname der Datei.
	 * @return	Liefert <code>true</code> zurück, wenn die Arbeitsmappe erfolgreich gespeichert wurde.
	 * @see #save(File, mathtools.Table.SaveMode)
	 */
	private boolean saveHTML(final File file) {
		if (file==null) return false;

		/* Tabellendokument anlegen */
		final StringBuilder html=new StringBuilder();

		html.append("<!DOCTYPE html>\n");
		html.append("<html>\n");
		html.append("<head>\n");
		html.append("  <meta charset=\"utf-8\">");
		html.append("  <title>"+Table.ExportTitle+"</title>\n");
		html.append("  <meta name=\"author\" content=\"Alexander Herzog\">\n");
		html.append("  <style type=\"text/css\">\n");
		html.append("    body {font-family: Verdana, Lucida, sans-serif;}\n");
		html.append("    table {border: 1px solid black; border-collapse: collapse;}\n");
		html.append("    td {border: 1px solid black; padding: 2px 5px;}\n");
		html.append("  </style>\n");
		html.append("</head>\n");
		html.append("<body>\n");

		/* Tabellendokument füllen */
		List<String> usedNames=new ArrayList<>();

		for (int i=0;i<names.size();i++) {
			String nameOrig=names.get(i);
			if (nameOrig.length()>31) nameOrig=nameOrig.substring(0,31);
			String name=nameOrig;
			int nr=0;
			while (true) {
				if (usedNames.indexOf(name.toUpperCase())<0) break;
				nr++; String add=" ("+nr+")";
				if ((name+add).length()>31) name=nameOrig.substring(0,31-add.length())+add; else name=nameOrig+add;
			}

			html.append("<h1>"+names.get(i)+"</h1>\n");
			html.append(tables.get(i).saveToHTML());

			usedNames.add(name.toUpperCase());
		}

		/* In Datei schreiben */
		html.append("</body>\n</html>\n");
		return Table.saveTextToFile(html.toString(),file);
	}

	/**
	 * Speichert den Inhalt der Arbeitsmappe in einer xls-Datei.
	 * @param file	Dateiname der Datei.
	 * @return	Liefert <code>true</code> zurück, wenn die Arbeitsmappe erfolgreich gespeichert wurde.
	 * @see #save(File, mathtools.Table.SaveMode)
	 */
	private boolean saveXLS(final File file) {
		if (file==null) return false;

		/* Tabellendokument anlegen */
		try (Workbook wb=new HSSFWorkbook()) {

			/* Tabellendokument füllen */
			List<String> usedNames=new ArrayList<>();

			for (int i=0;i<names.size();i++) {
				String nameOrig=names.get(i);
				if (nameOrig.length()>31) nameOrig=nameOrig.substring(0,31);
				String name=nameOrig;
				int nr=0;
				while (true) {
					if (usedNames.indexOf(name.toUpperCase())<0) break;
					nr++; String add=" ("+nr+")";
					if ((name+add).length()>31) name=nameOrig.substring(0,31-add.length())+add; else name=nameOrig+add;
				}

				Sheet sheet=wb.createSheet(name);
				tables.get(i).saveToSheet(wb,sheet);

				usedNames.add(name.toUpperCase());
			}

			/* In Datei schreiben */
			try (FileOutputStream fo=new FileOutputStream(file)) {
				wb.write(fo);
			}
			return true;

		} catch (IOException e) {return false;}
	}

	/**
	 * Speichert den Inhalt der Arbeitsmappe in einer ods-Datei.
	 * @param file	Dateiname der Datei.
	 * @return	Liefert <code>true</code> zurück, wenn die Arbeitsmappe erfolgreich gespeichert wurde.
	 * @see #save(File, mathtools.Table.SaveMode)
	 */
	private boolean saveODS(final File file) {
		if (file==null) return false;

		/* Tabellendokument anlegen */
		try (SpreadsheetDocument ods=SpreadsheetDocument.newSpreadsheetDocument()) {
			while (ods.getSheetCount()>0) ods.removeSheet(0);

			/* Tabellendokument füllen */
			List<String> usedNames=new ArrayList<>();

			for (int i=0;i<names.size();i++) {
				String nameOrig=names.get(i);
				if (nameOrig.length()>31) nameOrig=nameOrig.substring(0,31);
				String name=nameOrig;
				int nr=0;
				while (true) {
					if (usedNames.indexOf(name.toUpperCase())<0) break;
					nr++; String add=" ("+nr+")";
					if ((name+add).length()>31) name=nameOrig.substring(0,31-add.length())+add; else name=nameOrig+add;
				}

				final Table table=tables.get(i);
				final org.odftoolkit.simple.table.Table sheet=ods.appendSheet(name);
				table.saveToSheet(sheet);

				usedNames.add(name.toUpperCase());
			}

			/* In Datei schreiben */
			ods.save(file);
			return true;
		} catch (Exception ex) {return false;}
	}

	/**
	 * Speichert den Inhalt der Arbeitsmappe in einer xlsx-Datei.
	 * @param file	Dateiname der Datei.
	 * @return	Liefert <code>true</code> zurück, wenn die Arbeitsmappe erfolgreich gespeichert wurde.
	 * @see #save(File, mathtools.Table.SaveMode)
	 */
	private boolean saveXLSX(final File file) {
		if (file==null) return false;

		/* Tabellendokument anlegen */
		try (Workbook wb=new SXSSFWorkbook()) { /* Streaming-Variante */

			/* Tabellendokument füllen */
			List<String> usedNames=new ArrayList<>();

			for (int i=0;i<names.size();i++) {
				String nameOrig=names.get(i);
				if (nameOrig.length()>31) nameOrig=nameOrig.substring(0,31);
				String name=nameOrig;
				int nr=0;
				while (true) {
					if (usedNames.indexOf(name.toUpperCase())<0) break;
					nr++; String add=" ("+nr+")";
					if ((name+add).length()>31) name=nameOrig.substring(0,31-add.length())+add; else name=nameOrig+add;
				}

				final Sheet sheet=wb.createSheet(name);
				tables.get(i).saveToSheet(wb,sheet);

				usedNames.add(name.toUpperCase());
			}

			/* In Datei schreiben */
			try (FileOutputStream fo=new FileOutputStream(file)) {wb.write(fo);}
			if (wb instanceof SXSSFWorkbook) ((SXSSFWorkbook)wb).dispose();
			return true;

		} catch (IOException e) {return false;}
	}

	/**
	 * Speichert den Inhalt der Arbeitsmappe in einer docx-Datei.
	 * @param file	Dateiname der Datei.
	 * @return	Liefert <code>true</code> zurück, wenn die Arbeitsmappe erfolgreich gespeichert wurde.
	 * @see #save(File, mathtools.Table.SaveMode)
	 */
	private boolean saveDOCX(final File file) {
		if (file==null) return false;

		/* Tabellendokument anlegen */
		try(XWPFDocument doc=new XWPFDocument()) {

			/* Tabellendokument füllen */
			List<String> usedNames=new ArrayList<>();

			for (int i=0;i<names.size();i++) {
				String nameOrig=names.get(i);
				if (nameOrig.length()>31) nameOrig=nameOrig.substring(0,31);
				String name=nameOrig;
				int nr=0;
				while (true) {
					if (usedNames.indexOf(name.toUpperCase())<0) break;
					nr++; String add=" ("+nr+")";
					if ((name+add).length()>31) name=nameOrig.substring(0,31-add.length())+add; else name=nameOrig+add;
				}

				XWPFParagraph p=doc.createParagraph();
				XWPFRun r=p.createRun();
				r.setFontSize(18);
				r.setBold(true);
				r.setText(names.get(i));
				tables.get(i).saveToDOCX(doc);

				usedNames.add(name.toUpperCase());
			}

			/* In Datei schreiben */
			try (FileOutputStream out=new FileOutputStream(file)) {doc.write(out);}
			return true;

		} catch (IOException e) {return false;}
	}

	/*
	private boolean saveODT(final File file) {
		if (file==null) return false;

		/* Tabellendokument anlegen *
		try(TextDocument odt=TextDocument.newTextDocument()) {

			/* Tabellendokument füllen *
			List<String> usedNames=new ArrayList<>();

			for (int i=0;i<names.size();i++) {
				String nameOrig=names.get(i);
				if (nameOrig.length()>31) nameOrig=nameOrig.substring(0,31);
				String name=nameOrig;
				int nr=0;
				while (true) {
					if (usedNames.indexOf(name.toUpperCase())<0) break;
					nr++; String add=" ("+nr+")";
					if ((name+add).length()>31) name=nameOrig.substring(0,31-add.length())+add; else name=nameOrig+add;
				}

				final org.odftoolkit.simple.text.Paragraph paragraph=odt.addParagraph(names.get(i));
				paragraph.getFont().setSize(18);
				paragraph.getFont().setFontStyle(FontStyle.BOLD);

				tables.get(i).saveToODT(odt);

				usedNames.add(name.toUpperCase());
			}

			/* In Datei schreiben *
			odt.save(file);
			return true;

		} catch (Exception e) {return false;}
	}
	 */

	/**
	 * Speichert den Inhalt der Arbeitsmappe in einer Datei.
	 * @param file	Dateiname der Datei.
	 * @param saveMode	Gibt an, in welchem Format die Daten gespeichert werden sollen (xls, xlsx oder ods).
	 * @return	Liefert <code>true</code> zurück, wenn die Arbeitsmappe erfolgreich gespeichert wurde.
	 * @see #load(File)
	 * @see #load(File, mathtools.Table.SaveMode)
	 */
	public boolean save(final File file, Table.SaveMode saveMode) {
		if (file==null) return false;

		if (saveMode==Table.SaveMode.SAVEMODE_BYFILENAME) saveMode=Table.getSaveModeFromFileName(file,false,true);
		if (saveMode!=Table.SaveMode.SAVEMODE_XLS && saveMode!=Table.SaveMode.SAVEMODE_XLSX && saveMode!=Table.SaveMode.SAVEMODE_ODS && saveMode!=Table.SaveMode.SAVEMODE_DOCX && saveMode!=Table.SaveMode.SAVEMODE_HTML) saveMode=Table.SaveMode.SAVEMODE_XLSX;

		if (saveMode==Table.SaveMode.SAVEMODE_XLSX) return saveXLSX(file);
		if (saveMode==Table.SaveMode.SAVEMODE_XLS) return saveXLS(file);
		if (saveMode==Table.SaveMode.SAVEMODE_HTML) return saveHTML(file);
		if (saveMode==Table.SaveMode.SAVEMODE_DOCX) return saveDOCX(file);
		/* if (saveMode==Table.SaveMode.SAVEMODE_ODT) return saveODT(file); */
		if (saveMode==Table.SaveMode.SAVEMODE_ODS) return saveODS(file);

		return false;
	}

	/**
	 * Speichert den Inhalt der Arbeitsmappe in einer Datei.
	 * (Ob die Daten als xls-, xlsx- oder ods-Datei gespeichert werden, wird gemäß der
	 * Dateiendung entschieden.)
	 * @param file	Dateiname der Datei.
	 * @return	Liefert <code>true</code> zurück, wenn die Arbeitsmappe erfolgreich gespeichert wurde.
	 */
	public boolean save(final File file) {
		return save(file,Table.SaveMode.SAVEMODE_BYFILENAME);
	}

	/**
	 * Liefert eine Reihe aus einer bestimmten Minimal- und Maximal-Anzahl an (Zeichenketten-)Werten zurück.
	 * @param minValues	Minimale Anzahl an Werte in einer Spalte oder Zeile
	 * @param maxValues	Maximale Anzahl an Werte in einer Spalte oder Zeile
	 * @param table	Tabelle der die Daten entnommen werden sollen
	 * @return	Liefert im Erfolgsfall ein Array aus den Werten zurück oder sonst <code>null</code>.
	 * @see #getDataLine(int, int, Table)
	 */
	private String[] getDataLineRow(final int minValues, final int maxValues, final Table table) {
		int rowNr=-1;
		int colStart=0, colEnd=0;

		for (int i=0;i<table.getSize(0);i++) {
			/* pro Zeile */
			List <String> row=table.getLine(i);

			/* Bereich mit Daten finden */
			int start=-1;
			for (int j=0;j<row.size();j++) if (!row.get(j).trim().isEmpty()) {start=j; break;}
			if (start==-1) continue;
			int end=row.size()-1;
			for (int j=start+1;j<row.size();j++) if (row.get(j).trim().isEmpty()) {end=j-1; break;}

			/* Später noch andere Daten? */
			boolean valid=true;
			for (int j=end+1;j<row.size();j++) if (!row.get(j).trim().isEmpty()) {valid=false; break;}
			if (!valid) continue;

			/* Passende Menge an Daten? */
			if (end-start+1<minValues || end-start+1>maxValues) continue;

			/* Bereits anderen Datenbereich gefunden => nicht eindeutig */
			if (rowNr>=0) return null;

			rowNr=i;
			colStart=start;
			colEnd=end;
		}

		if (rowNr==-1) return null;

		/* Ergebnisse zusammenstellen */
		String[] result=new String[colEnd-colStart+1];
		List <String> row=table.getLine(rowNr);
		for (int i=0;i<result.length;i++) result[i]=row.get(colStart+i);
		return result;
	}

	/**
	 * Liefert eine Reihe aus (Zeichenketten-)Werten zurück.
	 * @param table	Tabelle der die Daten entnommen werden sollen
	 * @return	Liefert im Erfolgsfall ein Array aus den Werten zurück oder sonst <code>null</code>.
	 * @see #getDataLine(int, int, Table)
	 */
	private String[] getSimpleData(final Table table) {
		Table t=table.clone();

		if (t.getSize(0)>1 && t.getSize(1)==1) t=t.transpose();

		if (t.getSize(0)!=1 || t.getSize(1)<1) return null;
		List<String> line=t.getLine(0);

		int start=-1;
		for (int i=0;i<line.size();i++) if (!line.get(i).trim().isEmpty()) {start=i; break;}
		if (start==-1) return null;

		List<String> data=new ArrayList<>();
		for (int i=start;i<line.size();i++) {
			if (line.get(i).trim().isEmpty()) break;
			data.add(line.get(i));
		}
		return data.toArray(new String[0]);
	}

	/**
	 * Liefert eine Reihe aus einer bestimmten Minimal- und Maximal-Anzahl an (Zeichenketten-)Werten zurück.
	 * @param minValues	Minimale Anzahl an Werte in einer Spalte oder Zeile
	 * @param maxValues	Maximale Anzahl an Werte in einer Spalte oder Zeile
	 * @param table	Tabelle der die Daten entnommen werden sollen
	 * @return	Liefert im Erfolgsfall ein Array aus den Werten zurück oder sonst <code>null</code>.
	 * @see #getDataLine(int, int)
	 */
	private String[] getDataLine(int minValues, int maxValues, Table table) {
		String[] simple=getSimpleData(table);
		if (simple!=null && simple.length>=minValues && simple.length<=maxValues) return simple;

		String[] dataRow=getDataLineRow(minValues,maxValues,table);
		String[] dataCol=getDataLineRow(minValues,maxValues,table.transpose());
		if (dataRow==null) {
			return dataCol;
		} else {
			return (dataCol==null)?dataRow:null;
		}
	}

	/**
	 * Liefert eine Reihe aus einer bestimmten Minimal- und Maximal-Anzahl an Werten zurück,
	 * sofern diese eindeutig bestimmbar sind, d.h. es sich um eine einfach aufgebaute Tabelle handelt.
	 * @param minValues	Minimale Anzahl an Werte in einer Spalte oder Zeile
	 * @param maxValues	Maximale Anzahl an Werte in einer Spalte oder Zeile
	 * @return	Liefert im Erfolgsfall ein Array aus den Werten zurück oder sonst <code>null</code> wenn es keinen eindeutig passenden Bereich in der Arbeitsmappe gibt
	 */
	public String[] getDataLine(final int minValues, final int maxValues) {
		String[] record=null; /* noch nichts gefunden */

		for (int i=0;i<tables.size();i++) {
			String[] newRecord=getDataLine(minValues,maxValues,tables.get(i));
			if (record!=null && newRecord!=null) return null; /* nicht eindeutig */
			record=newRecord;
		}

		return record;
	}

	/**
	 * Liefert eine Reihe aus einer bestimmten Anzahl an Werten zurück,
	 * sofern diese eindeutig bestimmbar sind, d.h. es sich um eine einfach aufgebaute Tabelle handelt.
	 * @param valueCount	Gewünschte Anzahl an Werte in einer Spalte oder Zeile
	 * @return	Liefert im Erfolgsfall ein Array aus den Werten zurück oder sonst <code>null</code> wenn es keinen eindeutig passenden Bereich in der Arbeitsmappe gibt
	 */
	public String[] getDataLine(int valueCount) {
		return getDataLine(valueCount,valueCount);
	}

	/**
	 * Liefert eine Reihe aus einer bestimmten Minimal- und Maximal-Anzahl an Werten zurück.
	 * @param minValues	Minimale Anzahl an Werte in einer Spalte oder Zeile
	 * @param maxValues	Maximale Anzahl an Werte in einer Spalte oder Zeile
	 * @param table	Tabelle der die Daten entnommen werden sollen
	 * @return	Liefert im Erfolgsfall ein Array aus den Werten zurück oder sonst <code>null</code>.
	 * @see #getNumbersLine(int, int, Table)
	 * @see #getNumbersLines(int, int, Table)
	 */
	private double[] getNumbersLineRow(final int minValues, final int maxValues, final Table table) {
		int rowNr=-1;
		int colStart=0, colEnd=0;

		for (int i=0;i<table.getSize(0);i++) {
			/* pro Zeile */
			final List <String> row=table.getLine(i);

			/* Bereich mit Daten finden */
			int start=-1;
			for (int j=0;j<row.size();j++) if (!row.get(j).trim().isEmpty()) {start=j; break;}
			if (start==-1) continue;
			int end=row.size()-1;
			for (int j=start+1;j<row.size();j++) if (row.get(j).trim().isEmpty()) {end=j-1; break;}

			/* Später noch andere Zahlen? */
			boolean valid=true;
			for (int j=end+1;j<row.size();j++) if (!row.get(j).trim().isEmpty()) {valid=false; break;}
			if (!valid) continue;

			/* Passende Menge an Daten? */
			if (end-start+1<minValues || end-start+1>maxValues) continue;

			/* Zahlen? */
			valid=true;
			for (int j=start;j<=end;j++) {
				Double D=NumberTools.getExtProbability(row.get(j));
				if (D==null) {valid=false; break;}
			}
			if (!valid) continue;

			/* Bereits anderen Datenbereich gefunden => nicht eindeutig */
			if (rowNr>=0) return null;

			rowNr=i;
			colStart=start;
			colEnd=end;
		}

		if (rowNr==-1) return null;

		/* Ergebnisse zusammenstellen */
		double[] result=new double[colEnd-colStart+1];
		List <String> row=table.getLine(rowNr);
		for (int i=0;i<result.length;i++) {
			Double D=NumberTools.getExtProbability(row.get(colStart+i));
			result[i]=D;
		}
		return result;
	}

	/**
	 * Liefert eine Reihe mit einfachen Zahlenwerten zurück.
	 * @param table	Tabelle der die Daten entnommen werden sollen
	 * @return	Liefert im Erfolgsfall ein Array aus den Werten zurück oder sonst <code>null</code>.
	 * @see #getNumbersLine(int, int, Table)
	 * @see #getNumbersLines(int, int, Table)
	 */
	private double[] getSimpleNumbers(final Table table) {
		Table t=table.clone();

		if (t.getSize(0)>1 && t.getSize(1)==1) t=t.transpose();

		if (t.getSize(0)!=1 || t.getSize(1)<1) return null;
		List<String> line=t.getLine(0);

		int start=-1;
		for (int i=0;i<line.size();i++) {
			if (line.get(i).trim().isEmpty()) continue;
			Double D=NumberTools.getExtProbability(line.get(i));
			if (D!=null) {start=i; break;}
		}
		if (start==-1) return null;

		List<Double> data=new ArrayList<>();
		for (int i=start;i<line.size();i++) {
			if (line.get(i).trim().isEmpty()) break;
			Double D=NumberTools.getExtProbability(line.get(i));
			if (D==null) break;
			data.add(D);
		}

		double[] result=new double[data.size()];
		for (int i=0;i<data.size();i++) result[i]=data.get(i);
		return result;
	}

	/**
	 * Liefert zwei Reihen aus einer bestimmten Minimal- und Maximal-Anzahl an einfachen Zahlenwerten zurück.
	 * @param table	Tabelle der die Daten entnommen werden sollen
	 * @return	Liefert im Erfolgsfall ein Array aus den Werten zurück oder sonst <code>null</code>.
	 * @see #getNumbersLines(int, int, Table)
	 */
	private double[][] getTwoLinedSimpleNumbers(final Table table) {
		Table t=table.clone();

		if (t.getSize(0)>2 && t.getSize(1)>=1 && t.getSize(1)<=2) t=t.transpose();

		if (t.getSize(0)<1 || t.getSize(0)>2 || t.getSize(1)<2) return null;
		List<String> line1, line2=null;
		if (t.getSize(0)==2) {
			line1=t.getLine(0);
			line2=t.getLine(1);
		} else {
			line1=t.getLine(0);
		}

		int start=-1;
		int upperIndexBound=(line2==null)?line1.size():Math.min(line1.size(),line2.size());
		for (int i=0;i<upperIndexBound;i++) {
			if (line1.get(i).trim().isEmpty()) continue;
			if (line2!=null && line2.get(i).trim().isEmpty()) continue;
			Double D1=NumberTools.getExtProbability(line1.get(i));
			Double D2=null;
			if (line2!=null) D2=NumberTools.getExtProbability(line2.get(i));
			if (D1!=null && (line2==null || D2!=null)) {start=i; break;}
		}
		if (start==-1) return null;

		List<Double> data1=new ArrayList<>();
		List<Double> data2=new ArrayList<>();
		for (int i=start;i<upperIndexBound;i++) {
			if (line1.get(i).trim().isEmpty()) break;
			if (line2!=null && line2.get(i).trim().isEmpty()) break;
			Double D1=NumberTools.getExtProbability(line1.get(i));
			Double D2=null;
			if (line2!=null) D2=NumberTools.getExtProbability(line2.get(i));
			if (D1==null || (line2!=null && D2==null)) break;
			data1.add(D1);
			if (line2!=null) data2.add(D2);
		}

		double[][] result;
		if (line2==null) {
			result=new double[1][];
			result[0]=new double[data1.size()];
		} else {
			result=new double[2][];
			result[0]=new double[data1.size()];
			result[1]=new double[data1.size()];
		}
		for (int i=0;i<data1.size();i++) {
			result[0][i]=data1.get(i);
			if (line2!=null) result[1][i]=data2.get(i);
		}
		return result;
	}

	/**
	 * Liefert eine Reihe aus einer bestimmten Minimal- und Maximal-Anzahl an Werten zurück.
	 * @param minValues	Minimale Anzahl an Werte in einer Spalte oder Zeile
	 * @param maxValues	Maximale Anzahl an Werte in einer Spalte oder Zeile
	 * @param table	Tabelle der die Daten entnommen werden sollen
	 * @return	Liefert im Erfolgsfall ein Array aus den Werten zurück oder sonst <code>null</code>.
	 * @see #getNumbersLine(int, int)
	 */
	private double[] getNumbersLine(final int minValues, final int maxValues, final Table table) {
		double[] simple=getSimpleNumbers(table);
		if (simple!=null && simple.length>=minValues && simple.length<=maxValues) return simple;

		double[] dataRow=getNumbersLineRow(minValues,maxValues,table);
		double[] dataCol=getNumbersLineRow(minValues,maxValues,table.transpose());
		if (dataRow==null) {
			return dataCol;
		} else {
			return (dataCol==null)?dataRow:null;
		}
	}

	/**
	 * Liefert zwei Reihen aus einer bestimmten Minimal- und Maximal-Anzahl an Werten zurück.
	 * @param minValues	Minimale Anzahl an Werte in einer Spalte oder Zeile
	 * @param maxValues	Maximale Anzahl an Werte in einer Spalte oder Zeile
	 * @param table	Tabelle der die Daten entnommen werden sollen
	 * @return	Liefert im Erfolgsfall ein Array aus den Werten zurück oder sonst <code>null</code>.
	 * @see #getTwoLinedSimpleNumbers(Table)
	 */
	private double[][] getNumbersLines(final int minValues, final int maxValues, final Table table) {
		double[][] simple=getTwoLinedSimpleNumbers(table);
		if (simple!=null && simple[0].length>=minValues && simple[0].length<=maxValues) return simple;
		simple=new double[][]{getSimpleNumbers(table)};
		if (simple[0]!=null && simple[0].length>=minValues && simple[0].length<=maxValues) return simple;

		double[] dataRow=getNumbersLineRow(minValues,maxValues,table);
		double[] dataCol=getNumbersLineRow(minValues,maxValues,table.transpose());
		if (dataRow==null) {
			return new double[][]{dataCol};
		} else {
			return (dataCol==null)?new double[][]{dataRow}:null;
		}

	}

	/**
	 * Liefert eine Reihe aus einer bestimmten Minimal- und Maximal-Anzahl an Werten zurück,
	 * sofern diese eindeutig bestimmbar sind, d.h. es sich um eine einfach aufgebaute Tabelle handelt.
	 * @param minValues	Minimale Anzahl an Werte in einer Spalte oder Zeile
	 * @param maxValues	Maximale Anzahl an Werte in einer Spalte oder Zeile
	 * @return	Liefert im Erfolgsfall ein Array aus den Werten zurück oder sonst <code>null</code> wenn es keinen eindeutig passenden Bereich in der Arbeitsmappe gibt
	 */
	public double[] getNumbersLine(final int minValues, final int maxValues) {
		double[] record=null; /* noch nichts gefunden */

		for (int i=0;i<tables.size();i++) {
			double[] newRecord=getNumbersLine(minValues,maxValues,tables.get(i));
			if (record!=null && newRecord!=null) return null; /* nicht eindeutig */
			if (newRecord!=null) record=newRecord;
		}

		return record;
	}

	/**
	 * Liefert zwei Reihen aus einer bestimmten Minimal- und Maximal-Anzahl an Werten zurück,
	 * sofern diese eindeutig bestimmbar sind, d.h. es sich um eine einfach aufgebaute Tabelle handelt.
	 * @param minValues	Minimale Anzahl an Werte in einer Spalte oder Zeile
	 * @param maxValues	Maximale Anzahl an Werte in einer Spalte oder Zeile
	 * @return	Liefert im Erfolgsfall ein Array aus den Werten zurück oder sonst <code>null</code> wenn es keinen eindeutig passenden Bereich in der Arbeitsmappe gibt
	 */
	public double[][] getNumbersTwoLines(final int minValues, final int maxValues) {
		double[][] record=null; /* noch nichts gefunden */

		for (int i=0;i<tables.size();i++) {
			double[][] newRecord=getNumbersLines(minValues,maxValues,tables.get(i));
			if (record!=null && newRecord!=null && record.length==1) return null; /* nicht eindeutig */
			if (newRecord!=null) record=newRecord;
		}

		return record;
	}

	/**
	 * Liefert eine Reihe aus einer bestimmten Anzahl an Werten zurück,
	 * sofern diese eindeutig bestimmbar sind, d.h. es sich um eine einfach aufgebaute Tabelle handelt.
	 * @param valueCount	Gewünschte Anzahl an Werte in einer Spalte oder Zeile
	 * @return	Liefert im Erfolgsfall ein Array aus den Werten zurück oder sonst <code>null</code> wenn es keinen eindeutig passenden Bereich in der Arbeitsmappe gibt
	 */
	public double[] getNumbersLine(final int valueCount) {
		return getNumbersLine(valueCount,valueCount);
	}

	/**
	 * Zeigt einen Auswahldialog zum Laden von Tabellen an.
	 * (Diese Funktion ist identisch mit <code>Table.showLoadDialog</code>, genauer ruft sie intern diese
	 * sogar einfach nur auf. Diese Funktion ist hier nur aus Symmetriegründen vorhanden.)
	 * @param parent	Elternkomponente des Dialogs
	 * @param title	Anzuzeigender Titel im Auswahldialog
	 * @return	Im Erfolgsfall wird der Dateiname zurückgegeben, sonst <code>null</code>
	 * @see Table#showLoadDialog(Component, String)
	 * @see Table#showLoadDialog(Component, String, File)
	 */
	public static File showLoadDialog(final Component parent, final String title) {
		return Table.showLoadDialog(parent,title);
	}

	/**
	 * Zeigt einen Auswahldialog zum Laden von Tabellen an.
	 * (Diese Funktion ist identisch mit <code>Table.showLoadDialog</code>, genauer ruft sie intern diese
	 * sogar einfach nur auf. Diese Funktion ist hier nur aus Symmetriegründen vorhanden.)
	 * @param parent	Elternkomponente des Dialogs
	 * @param title	Anzuzeigender Titel im Auswahldialog
	 * @param initialDirectory	Pfad, der anfänglich im Dialog ausgewählt sein soll (kann auch <code>null</code> sein)
	 * @return	Im Erfolgsfall wird der Dateiname zurückgegeben, sonst <code>null</code>
	 * @see Table#showLoadDialog(Component, String)
	 * @see Table#showLoadDialog(Component, String, File)
	 */
	public static File showLoadDialog(final Component parent, final String title, final File initialDirectory) {
		return Table.showLoadDialog(parent,title,initialDirectory);
	}

	/**
	 * Zeigt einen Auswahldialog zum Speichern von Excel-Tabellen an.
	 * @param parent	Elternkomponente des Dialogs
	 * @param title	Anzuzeigender Titel im Auswahldialog
	 * @param initialDirectory	Pfad, der anfänglich im Dialog ausgewählt sein soll (kann auch <code>null</code> sein)
	 * @return	Im Erfolgsfall wird der Dateiname zurückgegeben, sonst <code>null</code>
	 */
	public static File showSaveDialog(final Component parent, final String title, final File initialDirectory) {
		JFileChooser fc;
		if (initialDirectory!=null) fc=new JFileChooser(initialDirectory.toString()); else {
			fc=new JFileChooser();
			CommonVariables.initialDirectoryToJFileChooser(fc);
		}
		fc.setDialogTitle(title);
		FileFilter xlsx=new FileNameExtensionFilter(Table.FileTypeExcel+" (*.xlsx)","xlsx");
		FileFilter xls=new FileNameExtensionFilter(Table.FileTypeExcelOld+" (*.xls)","xls");
		FileFilter ods=new FileNameExtensionFilter(Table.FileTypeODS+" (*.ods)","ods");
		FileFilter docx=new FileNameExtensionFilter(Table.FileTypeWord+" (*.docx)","docx");
		FileFilter html=new FileNameExtensionFilter(Table.FileTypeHTML+" (*.html, *.htm)","html","htm");
		fc.addChoosableFileFilter(xlsx);
		fc.addChoosableFileFilter(xls);
		fc.addChoosableFileFilter(ods);
		fc.addChoosableFileFilter(docx);
		fc.addChoosableFileFilter(html);
		fc.setFileFilter(xlsx);
		fc.setAcceptAllFileFilterUsed(false);

		if (fc.showSaveDialog(parent)!=JFileChooser.APPROVE_OPTION) return null;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();

		if (file.getName().indexOf('.')<0) {
			if (fc.getFileFilter()==xlsx) file=new File(file.getAbsoluteFile()+".xlsx");
			if (fc.getFileFilter()==xls) file=new File(file.getAbsoluteFile()+".xls");
			if (fc.getFileFilter()==ods) file=new File(file.getAbsoluteFile()+".ods");
			if (fc.getFileFilter()==docx) file=new File(file.getAbsoluteFile()+".docx");
			if (fc.getFileFilter()==html) file=new File(file.getAbsoluteFile()+".html");
		}
		return file;
	}

	/**
	 * Zeigt einen Auswahldialog zum Speichern von Excel-Tabellen an.
	 * @param parent	Elternkomponente des Dialogs
	 * @param title	Anzuzeigender Titel im Auswahldialog
	 * @return	Im Erfolgsfall wird der Dateiname zurückgegeben, sonst <code>null</code>
	 */
	public static File showSaveDialog(final Component parent, final String title) {
		return showSaveDialog(parent,title,null);
	}
}

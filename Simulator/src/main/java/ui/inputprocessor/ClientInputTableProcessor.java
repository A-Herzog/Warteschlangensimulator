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
package ui.inputprocessor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import language.Language;
import mathtools.NumberTools;
import mathtools.Table;
import simulator.simparser.symbols.CalcSymbolClientUserData;

/**
 * Wandelt eine einfache Tabelle in eine Tabelle, die von einer Tabellenquelle verarbeitet werden kann, um.
 * @author Alexander Herzog
 */
public class ClientInputTableProcessor {
	/**
	 * Eingangstabelle
	 */
	private Table table;

	/**
	 * Konfigurationen der zu verarbeitenden Spalten
	 */
	private ColumnSetup[] columns;

	/**
	 * Konstruktor der Klasse
	 */
	public ClientInputTableProcessor() {
	}

	/**
	 * Versucht eine Tabelle zu laden.
	 * @param file	Zu ladende Tabelle
	 * @return	Liefert <code>true</code>, wenn die Tabelle erfolgreich geladen werden konnte
	 */
	public boolean loadTable(final File file) {
		/* Datei laden */
		final Table table=new Table();
		if (!table.load(file)) return false;
		if (table.getSize(0)<2 || table.getSize(1)<2) return false;
		this.table=table;

		/* Spalten verarbeiten */
		final Table cols=table.transpose();
		final List<List<String>> colData=cols.getData();
		final List<ColumnSetup> columns=new ArrayList<>();
		int indexCounter=0;
		for (int i=2;i<colData.size();i++) {
			final List<String> columnData=colData.get(i);
			if (columnData==null || columnData.size()<2) continue;
			if (columnData.get(0).trim().isEmpty()) continue;
			final ColumnSetup column=new ColumnSetup(columnData,i);
			column.index=indexCounter;
			indexCounter++;
			columns.add(column);
		}
		this.columns=columns.toArray(new ColumnSetup[0]);

		return true;
	}

	/**
	 * Liefert die Konfigurationen der zu verarbeitenden Spalten.
	 * @return	Konfigurationen der zu verarbeitenden Spalten
	 */
	public ColumnSetup[] getColumns() {
		return columns;
	}

	/**
	 * Führt die Verarbeitung gemäß der Konfigurationen der zu verarbeitenden Spalten durch.
	 * @return	Ergebnistabelle (oder im Fehlerfall <code>null</code>)
	 */
	public Table process() {
		if (table==null) return null;

		final Table results=new Table();
		final List<List<String>> data=table.getData();
		final int rows=table.getSize(0);
		for (int i=1;i<rows;i++) results.addLine(processRow(data.get(i)));
		return results;
	}

	/**
	 * Verarbeitet eine einzelne Tabellenzeile
	 * @param row	Zu verarbeitende Tabellenzeile
	 * @return	Neue Tabellenzeile
	 * @see #process()
	 */
	private List<String> processRow(final List<String> row) {
		final List<String> line=new ArrayList<>();

		if (row.size()>=2) {
			line.add(row.get(0));
			line.add(row.get(1));
		}

		for (ColumnSetup setup: columns) {
			if (setup.colNr>=row.size()) continue;
			final String value=row.get(setup.colNr).trim();
			if (value.isEmpty()) continue;
			switch (setup.mode) {
			case OFF:
				/* Keine Verarbeitung der Spalte */
				break;
			case NUMBER:
				line.add(CalcSymbolClientUserData.CLIENT_DATA_COMMANDS[0]+"("+setup.index+")="+value);
				break;
			case TEXT:
				line.add(CalcSymbolClientUserData.CLIENT_DATA_COMMANDS[0]+"(\""+setup.name+"\")="+value);
				break;
			}
		}

		return line;
	}

	/**
	 * Wie soll die Tabellenspalte verwendet werden?
	 */
	public enum ColumnMode {
		/** Tabellenspalte nicht verwenden */
		OFF,
		/** Tabellenspalte als numerische Werte verwenden */
		NUMBER,
		/** Tabellenspalte als Textwerte verwenden */
		TEXT
	}

	/**
	 * Datensatz für eine Spalte
	 * @see ClientInputTableProcessor#getColumns()
	 */
	public static class ColumnSetup {
		/**
		 * 0-basierte Nummer der Tabellenspalte
		 */
		public final int colNr;

		/**
		 * Name der Tabellenspalte
		 */
		public final String name;

		/**
		 * Enthält die Spalte ausschließlich numerische Werte?
		 */
		public final boolean isNumeric;

		/**
		 * Zu verwendender Kundendaten-Index
		 */
		public int index;

		/**
		 * Wie soll die Spalte verwendet werden?
		 * @see ColumnMode
		 */
		public ColumnMode mode;

		/**
		 * Konstruktor der Klasse
		 * @param col	Zugehörige Datenspalte (inkl. Überschrift im ersten Eintrag)
		 * @param nr	0-basierte Nummer der Tabellenspalte
		 */
		public ColumnSetup(final List<String> col, final int nr) {
			colNr=nr;
			name=col.get(0).trim();

			boolean isNumeric=true;
			final int size=col.size();
			for (int i=1;i<size;i++) if (NumberTools.getDouble(col.get(i))==null) {isNumeric=false; break;}
			this.isNumeric=isNumeric;

			if (isNumeric) mode=ColumnMode.NUMBER; else mode=ColumnMode.TEXT;
		}

		/**
		 * Liefert eine html-Beschreibung der Konfiguration für die aktuelle Spalte
		 * @return	html-Beschreibung der Konfiguration für die aktuelle Spalte
		 */
		public String getHTMLInfo() {
			final StringBuilder result=new StringBuilder();
			result.append("<html><body>\n");

			result.append(Language.tr("BuildClientSourceTable.Setup.Column"));
			result.append(": <b>");
			result.append(Table.columnNameFromNumber(colNr));
			result.append("</b>, ");
			result.append(Language.tr("BuildClientSourceTable.Setup.ColumnName"));
			result.append(": \"<b>");
			result.append(name);
			result.append("</b>\"<br>");

			switch (mode) {
			case OFF:
				result.append("<span color=\"red\">");
				result.append(Language.tr("BuildClientSourceTable.Setup.Mode.Off"));
				result.append("</span>");
				break;
			case NUMBER:
				result.append(Language.tr("BuildClientSourceTable.Setup.Mode.Number"));
				result.append(": <tt>");
				result.append(CalcSymbolClientUserData.CLIENT_DATA_COMMANDS[0]);
				result.append("(");
				result.append(index);
				result.append(")=...</tt>");
				break;
			case TEXT:
				result.append(Language.tr("BuildClientSourceTable.Setup.Mode.Text"));
				result.append(": <tt>");
				result.append(CalcSymbolClientUserData.CLIENT_DATA_COMMANDS[0]);
				result.append("(\"");
				result.append(name);
				result.append("\")=...</tt>");
				break;
			}

			result.append("</body></html>");
			return result.toString();
		}
	}
}

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

import javax.swing.Icon;

import language.Language;
import mathtools.NumberTools;
import mathtools.Table;
import simulator.simparser.symbols.CalcSymbolClientUserData;
import ui.images.Images;

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

		List<String> columnData;

		/* Ankunftszeitpunkte (Spalte A) */
		columnData=colData.get(0);
		if (columnData==null || columnData.size()<2) return false;
		columns.add(new ColumnSetup(columnData,0,ColumnMode.ARRIVALS));

		/* Kundentypen (Spalte B) */
		columnData=colData.get(1);
		if (columnData==null || columnData.size()<2) return false;
		columns.add(new ColumnSetup(columnData,1,ColumnMode.CLIENT_TYPES));

		/* Weitere Datenspalten */
		int indexCounter=0;
		for (int i=2;i<colData.size();i++) {
			columnData=colData.get(i);
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

		/* Ankunftszeiten- und Kundentyp-Spalte bestimmen */
		int colArrival=-1;
		int colType=-1;
		for (ColumnSetup setup: columns) {
			if (setup.mode==ColumnMode.ARRIVALS) colArrival=setup.colNr;
			if (setup.mode==ColumnMode.CLIENT_TYPES) colType=setup.colNr;
			if (colArrival>=0 && colType>=0) break;
		}
		if (colArrival<0 || colType<0) return null;

		/* Verarbeitung der Tabelle */
		final Table results=new Table();
		final List<List<String>> data=table.getData();
		final int rows=table.getSize(0);
		for (int i=1;i<rows;i++) results.addLine(processRow(data.get(i),colArrival,colType));
		return results;
	}

	/**
	 * Verarbeitet eine einzelne Tabellenzeile
	 * @param row	Zu verarbeitende Tabellenzeile
	 * @param colArrival	Ankunftszeiten-Spalte
	 * @param colType	Kundentyp-Spalte
	 * @return	Neue Tabellenzeile
	 * @see #process()
	 */
	private List<String> processRow(final List<String> row, final int colArrival, final int colType) {
		final List<String> line=new ArrayList<>();

		/* Ankunftszeit */
		if (colArrival>=0 && row.size()>colArrival) {
			line.add(row.get(colArrival));
		} else {
			line.add("");
		}

		/* Kundentyp */
		if (colType>=0 && row.size()>colType) {
			line.add(row.get(colType));
		} else {
			line.add("");
		}

		/* Datenspalten */
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
			default:
				/* Spalte ist Zeiten- oder Kundentypenspalte */
				break;
			}
		}

		return line;
	}

	/**
	 * Wandelt die Zeichen "&amp;", "&lt;" und "&gt;" in ihre entsprechenden
	 * HTML-Entitäten um.
	 * @param line	Umzuwandelnder Text
	 * @return	Umgewandelter Text
	 */
	private static String encodeHTML(final String line) {
		if (line==null) return "";
		String result;
		result=line.replaceAll("&","&amp;");
		result=result.replaceAll("<","&lt;");
		result=result.replaceAll(">","&gt;");
		return result;
	}

	/**
	 * Wie soll die Tabellenspalte verwendet werden?
	 */
	public enum ColumnMode {
		/** Tabellenspalte nicht verwenden */
		OFF,
		/** Ankunftszeitpunkte */
		ARRIVALS,
		/** Kundentypen */
		CLIENT_TYPES,
		/** Tabellenspalte als numerische Werte verwenden */
		NUMBER,
		/** Tabellenspalte als Textwerte verwenden */
		TEXT
	}

	/**
	 * Datensatz für eine Spalte
	 * @see ClientInputTableProcessor#getColumns()
	 */
	public class ColumnSetup {
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
		 * Automatisch voreingestellter Modus
		 * @see #mode
		 */
		public final ColumnMode initialMode;

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
			initialMode=mode;
		}

		/**
		 * Konstruktor der Klasse
		 * @param col	Zugehörige Datenspalte (inkl. Überschrift im ersten Eintrag)
		 * @param nr	0-basierte Nummer der Tabellenspalte
		 * @param mode	Wie soll die Spalte verwendet werden?
		 */
		public ColumnSetup(final List<String> col, final int nr, final ColumnMode mode) {
			colNr=nr;
			name=col.get(0).trim();

			boolean isNumeric=true;
			final int size=col.size();
			for (int i=1;i<size;i++) if (NumberTools.getDouble(col.get(i))==null) {isNumeric=false; break;}
			this.isNumeric=isNumeric;

			this.mode=mode;
			initialMode=mode;
		}

		/**
		 * Liefert die ersten Werte nach der Überschrift in einer Spalte
		 * @param col	Spaltennummer (0-basierend)
		 * @param maxRows	Maximalanzahl an zu liefernden Werten
		 * @return	Werte in den Zeilen unterhalb der Überschrift (kann leer sein, ist aber nie <code>null</code>)
		 */
		private String[] getDataForCol(final int col, final int maxRows) {
			final List<String> results=new ArrayList<>();
			for (int i=1;i<=Math.min(maxRows,table.getSize(0)-1);i++) {
				final List<String> line=table.getLine(i);
				if (line.size()<=col) break;
				results.add(encodeHTML(line.get(col)));
			}
			return results.toArray(new String[0]);
		}

		/**
		 * Speichert in {@link #getIcon()} generierte Daten.
		 * @see #getInfo()
		 */
		private String storedInfo;

		/**
		 * Liefert die ersten drei Einträge in der aktuellen Spalte.
		 * @return	Erste Einträge (ohne Überschrift) in der aktuellen Spalte
		 */
		public String getInfo() {
			if (storedInfo==null) {
				final String[] data=getDataForCol(colNr,3);
				storedInfo=String.join(", ",data);
				if (!storedInfo.isEmpty()) storedInfo+=", ";
				storedInfo+="...";
			}
			return storedInfo;
		}

		/**
		 * Liefert eine html-Beschreibung der Konfiguration für die aktuelle Spalte.
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
				result.append(" (");
				result.append(getInfo());
				result.append(")");
				break;
			case ARRIVALS:
				result.append("<span color=\"blue\"><b>");
				result.append(Language.tr("BuildClientSourceTable.Setup.Mode.Arrivals"));
				result.append("</b></span>");
				result.append(" (");
				result.append(getInfo());
				result.append(")");
				break;
			case CLIENT_TYPES:
				result.append("<span color=\"blue\"><b>");
				result.append(Language.tr("BuildClientSourceTable.Setup.Mode.ClientTypes"));
				result.append("</b></span>");
				result.append(" (");
				result.append(getInfo());
				result.append(")");
				break;
			case NUMBER:
				result.append(Language.tr("BuildClientSourceTable.Setup.Mode.Number"));
				result.append(": <tt>");
				result.append(CalcSymbolClientUserData.CLIENT_DATA_COMMANDS[0]);
				result.append("(");
				result.append(index);
				result.append(")=");
				result.append(getInfo());
				result.append("</tt>");
				break;
			case TEXT:
				result.append(Language.tr("BuildClientSourceTable.Setup.Mode.Text"));
				result.append(": <tt>");
				result.append(CalcSymbolClientUserData.CLIENT_DATA_COMMANDS[0]);
				result.append("(\"");
				result.append(name);
				result.append("\")=");
				result.append(getInfo());
				result.append("</tt>");
				break;
			}

			result.append("</body></html>");
			return result.toString();
		}

		/**
		 * Liefert das Icon der Konfiguration für die aktuelle Spalte.
		 * @return	Icon der Konfiguration für die aktuelle Spalte
		 */
		public Icon getIcon() {
			switch (mode) {
			case OFF: return Images.GENERAL_OFF.getIcon();
			case ARRIVALS: return Images.GENERAL_TIME.getIcon();
			case CLIENT_TYPES: return Images.MODELPROPERTIES_CLIENTS.getIcon();
			case NUMBER: return Images.GENERAL_NUMBERS.getIcon();
			case TEXT: return Images.GENERAL_FONT.getIcon();
			default: return Images.GENERAL_TABLE.getIcon();
			}
		}
	}
}
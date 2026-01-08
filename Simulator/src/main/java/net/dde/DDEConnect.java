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
package net.dde;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.pretty_tools.dde.DDEException;
import com.pretty_tools.dde.client.DDEClientConversation;

import mathtools.NumberTools;
import tools.SetupData;

/**
 * Diese Klasse erlaubt den DDE-Zugriff auf Excel
 * @author Alexander Herzog
 * @version 1.2
 */
public class DDEConnect {
	/**
	 * Schaltet das DDE-System global aus.
	 * @see #available()
	 */
	public static boolean deactivated=false;

	/**
	 * Ergebnis der Prüfung, ob DDE zur Verfügung steht.
	 * @see #available()
	 */
	private enum ActivationMode {
		/** Noch nicht geprüft */
		NOT_TESTED,
		/** Verfügbar */
		AVAILABLE,
		/** Nicht verfügbar */
		NOT_AVAILABLE
	}

	/**
	 * Ergebnis der Prüfung, ob DDE zur Verfügung steht.
	 * @see #available()
	 */
	private static ActivationMode activationMode=ActivationMode.NOT_TESTED;

	/**
	 * Sprachvorgabe-Bezeichner für die Zeilenangaben in Excel
	 */
	public static String EXCEL_LANGUAGE_DEFAULT_ROW_IDENTIFIER="R";

	/**
	 * Sprachvorgabe-Bezeichner für die Spaltenangaben in Excel
	 */
	public static String EXCEL_LANGUAGE_DEFAULT_COL_IDENTIFIER="C";

	/** Referenz auf das Setup-Singleton */
	private final SetupData setup;

	/**
	 * Recycled das {@link StringBuilder}-Objekt
	 * für {@link #getCellName(int, int)}.
	 * @see #getCellName(int, int)
	 */
	private final StringBuilder nameBuilderCache;



	/**
	 * Konstruktor der Klasse
	 */
	public DDEConnect() {
		setup=SetupData.getSetup();
		nameBuilderCache=new StringBuilder();
	}

	/**
	 * Listet alle verfügbaren Arbeitsmappen und die Tabellennamen darin auf
	 * @return	Zuordnung von Arbeitsmappennamen zu jeweils Listen mit den Tabellennamen in der jeweiligen Arbeitsmappe
	 */
	public Map<String,List<String>> listTables() {
		final Map<String,List<String>> map=new HashMap<>();

		DDEClientConversation conversation;

		conversation=getConversation();
		if (conversation!=null) try {
			conversation.connect("Excel","System");
			try {
				for (final String topic: conversation.request("Topics").split("\t")) {
					if (!topic.startsWith("[")) continue;
					final String[] parts=topic.substring(1).split("]");
					if (parts.length!=2) continue;
					if (parts[0].toUpperCase().endsWith(".XLAM")) continue;
					if (parts[0].equals(":") || parts[1].equals(":")) continue;

					final List<String> list=map.getOrDefault(parts[0],new ArrayList<>());
					list.add(parts[1]);
					map.putIfAbsent(parts[0],list);
				}
			} finally {
				conversation.disconnect();
			}
		} catch (DDEException e) {}

		/*
		Der von LibreOffice verwendete 2-Byte-Zeichensatz (UTF-16LE) wird leider von der DDE-DLL nicht verstanden. Daher werden nur die Hälfte der Zeichen übermittelt.
		conversation=getConversation();
		if (conversation!=null) try {
			conversation.connect("soffice","System");
			try {
				final byte[] b=conversation.request("Topics",ClipboardFormat.CF_TEXT.getNativeCode());
				for (final String topic: new String(b,Charset.forName("UTF-16LE")).split("\t")) {
					if (!topic.startsWith("[")) continue;
					final String[] parts=topic.substring(1).split("]");
					if (parts.length!=2) continue;
					if (parts[0].toUpperCase().endsWith(".XLAM")) continue;
					if (parts[0].equals(":") || parts[1].equals(":")) continue;

					final List<String> list=map.getOrDefault(parts[0],new ArrayList<>());
					list.add(parts[1]);
					map.putIfAbsent(parts[0],list);
				}
			} finally {
				conversation.disconnect();
			}
		} catch (DDEException e) {}
		Zugriff auf LibreOffice-Zellen: topic="c:\office\document\data1.ods", item="sheet1.A1"
		 */

		return map;
	}

	/**
	 * Liefert den Item-Namen für eine Zelle in einer Tabelle
	 * @param row	0-basierende Angabe der Zeile (Zeile 0 ist Excel-Zeile 1)
	 * @param column	0-basierende Angabe der Spalte (Spalte 0 ist Excel-Spalte A)
	 * @return	Bezeichner in Excel-DDE-Item-Notation
	 */
	private String getCellName(final int row, final int column) {
		nameBuilderCache.setLength(0);

		if (setup.customExcelRowName!=null && !setup.customExcelRowName.isBlank()) {
			nameBuilderCache.append(setup.customExcelRowName.trim());
		} else {
			nameBuilderCache.append(EXCEL_LANGUAGE_DEFAULT_ROW_IDENTIFIER);
		}

		nameBuilderCache.append(row+1);

		if (setup.customExcelColName!=null && !setup.customExcelColName.isBlank()) {
			nameBuilderCache.append(setup.customExcelColName.trim());
		} else {
			nameBuilderCache.append(EXCEL_LANGUAGE_DEFAULT_COL_IDENTIFIER);
		}

		nameBuilderCache.append(column+1);

		return nameBuilderCache.toString();
	}

	/**
	 * Erstellt wenn möglich ein DDE-Verbindungs-Objekt.
	 * @return	Liefert im Erfolgsfall das DDE-Verbindungs-Objekt. Wenn keine DDE-Verbindung möglich ist, wird <code>null</code> geliefert.
	 */
	private DDEClientConversation getConversation() {
		try {
			return new DDEClientConversation();
		} catch (UnsatisfiedLinkError | NoClassDefFoundError e) {return null;} /* DLL nicht gefunden */
	}

	/**
	 * Gibt an, ob DDE generell verfügbar ist.
	 * @return	Gibt <code>true</code> zurück, wenn DDE verfügbar ist.
	 */
	public static synchronized boolean available() {
		if (deactivated) return false;

		final boolean available;

		switch (activationMode) {
		case AVAILABLE: return true;
		case NOT_AVAILABLE: return false;
		case NOT_TESTED:
			available=(new DDEConnect().getConversation()!=null);
			if (available) activationMode=ActivationMode.AVAILABLE; else activationMode=ActivationMode.NOT_AVAILABLE;
			return available;
		default:
			available=(new DDEConnect().getConversation()!=null);
			if (available) activationMode=ActivationMode.AVAILABLE; else activationMode=ActivationMode.NOT_AVAILABLE;
			return available;
		}
	}

	/**
	 * Stellt eine DDE-Verbindung mit einer Arbeitsmappe und einer Tabelle darin her.
	 * @param workbook	Arbeitsmappe
	 * @param sheet	Tabelle
	 * @return	Liefert im Erfolgsfall die DDE-Verbindung, sonst <code>null</code>
	 * @see #disconnect(DDEClientConversation)
	 */
	private DDEClientConversation connect(final String workbook, final String sheet) {
		if (workbook==null || sheet==null || workbook.isBlank() || sheet.isBlank()) return null;

		final DDEClientConversation conversation=getConversation();
		if (conversation==null) return null;

		try {
			conversation.connect("Excel","["+workbook+"]"+sheet);
			return conversation;
		} catch (DDEException e) {
			return null;
		}
	}

	/**
	 * Liest eine Datenzelle über eine bestehende DDE-Verbindung
	 * @param conversation	DDE-Verbindung
	 * @param row	Zeilenindex der zu lesenden Zelle
	 * @param column	Spaltenindex der zu lesenden Zelle
	 * @return	Liefert im Erfolgsfall den Zelleninhalt, sonst <code>null</code>
	 */
	private String readCell(final DDEClientConversation conversation, final int row, final int column) {
		if (conversation==null) return null;
		try {
			final String result=conversation.request(getCellName(row,column));
			if (result==null) return null;
			return result.trim();
		} catch (DDEException e) {
			return null;
		}
	}

	/**
	 * Beendet eine per {@link #connect(String, String)} hergestellte Verbindung.
	 * @param conversation	Zu beendende Verbindung
	 */
	private void disconnect(final DDEClientConversation conversation) {
		if (conversation==null) return;
		try {conversation.disconnect();} catch (DDEException e) {}
	}

	/**
	 * Liest Zeichenketten aus einer Spalte beginnend ab einer bestimmten Zelle
	 * @param workbook	Arbeitsmappe
	 * @param sheet	Tabelle innerhalb der Arbeitsmappe aus der die Zeichenketten gelesen werden sollen
	 * @param startRow	0-basierende Nummer der erste Zeile aus der gelesen werden soll
	 * @param column	0-basierende Nummer der Spalte aus der gelesen werden soll
	 * @return	Array der Zeichenketten (es wird so lange gelesen, bis eine leere Zelle auftritt)
	 */
	public String[] getStringsColumn(final String workbook, final String sheet, final int startRow, final int column) {
		if (startRow<0 || column<0) return new String[0];
		final DDEClientConversation conversation=connect(workbook,sheet);
		if (conversation==null) return new String[0];

		final List<String> list=new ArrayList<>();

		try {
			int row=startRow;
			while (true) {
				final String result=readCell(conversation,row,column);
				if (result==null || result.isBlank()) break;
				list.add(result);
				row++;
			}
		} finally {
			disconnect(conversation);
		}

		return list.toArray(String[]::new);
	}

	/**
	 * Liest Zahlenwerte aus einer Spalte beginnend ab einer bestimmten Zelle
	 * @param workbook	Arbeitsmappe
	 * @param sheet	Tabelle innerhalb der Arbeitsmappe aus der die Zahlenwerte gelesen werden sollen
	 * @param startRow	0-basierende Nummer der erste Zeile aus der gelesen werden soll
	 * @param column	0-basierende Nummer der Spalte aus der gelesen werden soll
	 * @return	Array der Zahlen (es wird so lange gelesen, bis eine leere bzw. Nicht-Zahlen-Zelle auftritt)
	 */
	public double[] getNumbersColumn(final String workbook, final String sheet, final int startRow, final int column) {
		if (startRow<0 || column<0) return new double[0];
		final DDEClientConversation conversation=connect(workbook,sheet);
		if (conversation==null) return new double[0];

		final List<Double> list=new ArrayList<>();

		try {
			int row=startRow;
			while (true) {
				final String result=readCell(conversation,row,column);
				if (result==null || result.isBlank()) break;
				final Double D=NumberTools.getDouble(result);
				if (D==null) break;
				list.add(D);
				row++;
			}
		} finally {
			disconnect(conversation);
		}

		final double[] arr=new double[list.size()];
		for (int i=0;i<list.size();i++) arr[i]=list.get(i);
		return arr;
	}

	/**
	 * Liefert einen Iterator, der Zahlen aus einer Spalte und Daten aus den Spalten rechts nebnen liefert
	 * @param workbook	Arbeitsmappe
	 * @param sheet	Tabelle innerhalb der Arbeitsmappe aus der die Zahlenwerte gelesen werden sollen
	 * @param startRow	0-basierende Nummer der erste Zeile aus der gelesen werden soll
	 * @param column	0-basierende Nummer der Spalte aus der gelesen werden soll
	 * @return	Iterator, der die Zahlen und die Werte in den Spalten rechts deneben liefert
	 * @see DDEConnect.DataIterator
	 */
	public DataIterator getData(final String workbook, final String sheet, final int startRow, final int column) {
		if (startRow<0 || column<0) return new DataIterator(null,startRow,column);
		return new DataIterator(connect(workbook,sheet),startRow,column);
	}

	/**
	 * Schreibt eine Datenzelle über eine bestehende DDE-Verbindung
	 * @param conversation	DDE-Verbindung
	 * @param row	Zeilenindex der zu schreibenden Zelle
	 * @param column	Spaltenindex der zu schreibenden Zelle
	 * @param value	Zu schreibender Wert
	 * @return	Liefert im Erfolgsfall <code>true</code>
	 */
	private boolean writeCell(final DDEClientConversation conversation, final int row, final int column, final String value) {
		if (conversation==null) return false;
		try {
			conversation.poke(getCellName(row,column),value);
			return true;
		} catch (DDEException e) {
			return false;
		}
	}

	/**
	 * DDE-Verbindung zum Schreiben von Daten
	 * @see #setData(String, String, int, int, String)
	 * @see #closeSetDataConnection()
	 */
	private DDEClientConversation putDataConversation;

	/**
	 * Schreibt einen Wert in eine Zelle
	 * @param workbook	Arbeitsmappe
	 * @param sheet	Tabelle innerhalb der Arbeitsmappe in die der Wert geschrieben werden soll
	 * @param row	0-basierende Nummer der erste Zeile in die geschrieben werden soll
	 * @param column	0-basierende Nummer der Spalte in die geschrieben werden soll
	 * @param value	Wert der in die Zelle geschrieben werden soll
	 * @return	Gibt <code>true</code> zurück, wenn der Wert geschrieben werden konnte
	 * @see DDEConnect#closeSetDataConnection()
	 */
	public boolean setData(final String workbook, final String sheet, final int row, final int column, final String value) {
		if (row<0 || column<0) return false;
		if (putDataConversation==null) putDataConversation=connect(workbook,sheet);
		if (putDataConversation==null) return false;
		return writeCell(putDataConversation,row,column,value);
	}

	/**
	 * Beim Lesen einer (oder mehrerer Spalten) aus einer Excel-Tabelle wird die DDE-Verbindung automatisch geschlossen.
	 * Wurden jedoch Daten per {@link DDEConnect#setData(String, String, int, int, String)} geschrieben, so muss
	 * die Verbindung über den Aufruf dieser Methode explizit geschlossen werden. Ist der Verbindungsaufbau fehlgeschlagen,
	 * wurden noch keine Daten geschrieben oder wurde diese Methode bereits zuvor aufgerufen, so ist der Aufruf folgenlos.
	 */
	public void closeSetDataConnection() {
		if (putDataConversation!=null) {
			disconnect(putDataConversation);
			putDataConversation=null;
		}
	}

	/**
	 * Liefert die Zahlenwerte in einer Spalte in Excel und außerdem optional
	 * Textwerte aus den Spalten rechts daneben
	 * @author Alexander Herzog
	 * @see DDEConnect#getData(String, String, int, int)
	 */
	public class DataIterator implements Iterator<Double> {
		/** DDE-Verbindung */
		private DDEClientConversation conversation;
		/** Index der als nächstes zu lesenden Zeile */
		private int nextRow;
		/** Spalte deren Zellen gelesen werden sollen */
		private final int column;
		/** Aktuelle Zusatzdaten aus den weiteren Spalten */
		private List<String> currentData;
		/** Zusatzdaten aus den weiteren Spalten */
		private List<String> nextData;
		/** Zusatzdaten aus den weiteren Spalten für die nächste Abfrage */
		private Double next;

		/**
		 * Konstruktor der Klasse
		 * @param conversation	DDE-Verbindung
		 * @param startRow	Startzeile für die Iteration
		 * @param column	Spalte deren Zellen gelesen werden sollen
		 */
		private DataIterator(final DDEClientConversation conversation, final int startRow, final int column) {
			this.conversation=conversation;
			nextRow=startRow;
			this.column=column;
			gotoNext();
		}

		/**
		 * Lädt die nächste Zeile
		 */
		private void gotoNext() {
			/* Verbindung schon geschlossen? Dann restliche Daten löschen. */
			if (conversation==null) {
				nextData=null;
				next=null;
				return;
			}

			/* Nächste Zeile verfügbar? */
			final String result=readCell(conversation,nextRow,column);
			if (result==null || result.isBlank()) {disconnect(); return;}
			final Double D=NumberTools.getDouble(result);
			if (D==null) {disconnect(); return;}
			next=D;

			/* Weitere Daten laden */
			currentData=nextData;
			nextData=new ArrayList<>();
			int col=column+1;
			while (true) {
				try {
					final String cell=conversation.request(getCellName(nextRow,col));
					if (cell==null || cell.isBlank()) break;
					nextData.add(cell.trim());
					col++;
				} catch (DDEException e) {break;}
			}

			/* Zeilenzeiger weiter schieben */
			nextRow++;
		}

		@Override
		public boolean hasNext() {
			return next!=null;
		}

		@Override
		public Double next() {
			final Double current=next;
			gotoNext();
			return current;
		}

		/**
		 * Liefert die Texte in den Spalten rechts neben der Zahlenspalte.
		 * Die Texte stammen aus der Zeile, dessen Zahlenwerte gerade per
		 * {@link DDEConnect.DataIterator#next()} abgefragt wurde.
		 * @return	Liste mit den Texten in der aktuellen Zeile in den Spalten rechts neben der Zahlenspalte
		 */
		public List<String> data() {
			if (currentData==null) return new ArrayList<>();
			return new ArrayList<>(currentData);
		}

		/**
		 * Wird die Spalte bis zu Ende (bis {@link DDEConnect.DataIterator#hasNext()} <code>false</code> liefert,
		 * so wird die Verbindung automatisch geschlossen. Wird die Verarbeitung vorher abgebrochen, so sollte die
		 * Verbindung über diese Methode explizit beendet werden.
		 */
		public void disconnect() {
			DDEConnect.this.disconnect(conversation);
			conversation=null;
			gotoNext();
		}
	}

	/**
	 * Baut eine Verbindung auf, liest den Wert einer Zelle aus und schließt die Verbindung wieder
	 * @param workbook	Arbeitsmappe
	 * @param sheet	Tabelle innerhalb der Arbeitsmappe aus der der Wert gelesen werden soll
	 * @param row	0-basierende Nummer der Zeile der zu lesenden Zelle
	 * @param column	0-basierende Nummer der Spalte der zu lesenden Zelle
	 * @return	Wert der Zelle oder <code>null</code>, wenn die Zelle nicht gelesen werden konnte
	 */
	public String readCell(final String workbook, final String sheet, final int row, final int column) {
		final DDEClientConversation conversation=connect(workbook,sheet);
		if (conversation==null) return null;
		try {
			return readCell(conversation,row,column);
		} finally {
			disconnect(conversation);
		}
	}
}
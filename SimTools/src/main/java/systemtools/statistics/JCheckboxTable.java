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
package systemtools.statistics;

import java.io.Serializable;
import java.util.Arrays;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;

/**
 * Hilfsklasse zur Anzeige von Checkbox-Listboxen im Reportgenerator
 * Diese Klasse kann nur innerhalb dieses Package verwendet werden.
 * @see StatisticViewerReport
 * @author Alexander Herzog
 */
public class JCheckboxTable extends JTable {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -5386423466149360971L;

	/** Array mit den in der Liste anzuzeigenden Texten (darf nicht <code>null</code> sein) */
	private final String[] keys;

	/** Aktuell selektierte Einträge */
	private final boolean[] select;

	/**
	 * Konstruktor der Klasse
	 * @param keys	Array mit den in der Liste anzuzeigenden Texten (darf nicht <code>null</code> sein)
	 * @param select	Gibt an, welche der Texte initial ausgewählt sein sollen (muss von der Länge <code>keys</code> entsprechen)
	 * @param caption	Optionale Überschrift über der Liste (darf <code>null</code> sein)
	 */
	public JCheckboxTable(String[] keys, boolean[] select, String caption) {
		setModel(new JCheckboxTableModel());

		this.keys=Arrays.copyOf(keys,keys.length);
		if (select==null) {
			this.select=new boolean[keys.length];
			Arrays.fill(this.select,false);
		} else {
			this.select=new boolean[select.length];
			for (int i=0;i<select.length;i++) this.select[i]=select[i];
		}

		if (caption==null || caption.isEmpty()) {
			setTableHeader(null);
		} else {
			JTableHeader tableHeader=createDefaultTableHeader();
			tableHeader.getColumnModel().getColumn(0).setHeaderValue("");
			tableHeader.getColumnModel().getColumn(1).setHeaderValue(caption);
			setTableHeader(tableHeader);
		}
		getColumnModel().getColumn(0).setPreferredWidth(25);
		getColumnModel().getColumn(0).setMaxWidth(25);
	}

	/**
	 * Konstruktor der Klasse
	 * @param keys	Array mit den in der Liste anzuzeigenden Texten (darf nicht <code>null</code> sein)
	 * @param select	Gibt an, welche der Texte initial ausgewählt sein sollen (muss von der Länge <code>keys</code> entsprechen)
	 */
	public JCheckboxTable(String[] keys, boolean[] select) {
		this(keys,select,null);
	}

	/**
	 * Konstruktor der Klasse<br>
	 * Es werden initial keine Einträge selektiert.
	 * @param keys	Array mit den in der Liste anzuzeigenden Texten (darf nicht <code>null</code> sein)
	 */
	public JCheckboxTable(String[] keys) {
		this(keys,null,null);
	}

	/**
	 * Listet auf, welche Einträge vom Nutzer selektiert wurden.
	 * @return	Selektierte Einträge
	 */
	public final boolean[] getSelected() {
		final boolean[] result=new boolean[select.length];
		for (int i=0;i<select.length;i++) result[i]=select[i];
		return result;
	}

	/**
	 * Stellt ein, welche Einträge selektiert sein sollen.
	 * @param newSetup	Zu selektierende Einträge
	 */
	public final void setSelected(final boolean[] newSetup) {
		if (newSetup==null) return;
		for (int i=0;i<Math.min(newSetup.length,select.length);i++) select[i]=newSetup[i];
		selectChanged();
		repaint();
	}

	@Override
	public final void selectAll() {
		for (int i=0;i<select.length;i++) select[i]=true;
		selectChanged();
		repaint();
	}

	/**
	 * Hebt die Selektion aller Einträge auf.
	 */
	public final void selectNone() {
		for (int i=0;i<select.length;i++) select[i]=false;
		selectChanged();
		repaint();
	}

	/**
	 * Selektiert die Einträge in einem bestimmten (0-basierenden) Bereich
	 * @param from	Erster zu selektierender Eintrag
	 * @param to	Letzter zu selektierender Eintrag
	 */
	public final void selectRange(int from, int to) {
		for (int i=0;i<select.length;i++) select[i]=(i>=from && i<=to);
		selectChanged();
		repaint();
	}

	/**
	 * Erstellt aus der Selektion eine Zeichenkette, die im Setup gespeichert werden kann
	 * @return	Zeichenkette basierend auf den selektierten Einträgen
	 */
	public final String getSetup() {
		final StringBuilder s=new StringBuilder();
		for (int i=0;i<select.length;i++) s.append((select[i])?"X":"-");
		return s.toString();
	}

	/**
	 * Stellt die Selektion der Einträge in der Liste basierend auf einer Zeichenkette (die aus dem Setup geladen werden kann) ein.
	 * @param newSelect	Zeichenkette auf deren Basis die Einträge selektiert werden sollen
	 */
	public final void setSetup(String newSelect) {
		if (newSelect==null) newSelect="";
		while (newSelect.length()<select.length) newSelect+="X";
		for (int i=0;i<select.length;i++) select[i]=(newSelect.charAt(i)=='X');
		repaint();
	}

	/**
	 * Wird aufgerufen, wenn sich die Selektion programmseitig verändert,
	 * so dass abgeleitete Klassen darauf reagieren können.
	 */
	protected void selectChanged() {}

	/**
	 * Tabellendaten für die CheckBox-Tabelle
	 * @see JCheckboxTable#JCheckboxTable(String[])
	 * @see JCheckboxTable#JCheckboxTable(String[], boolean[])
	 * @see JCheckboxTable#JCheckboxTable(String[], boolean[], String)
	 */
	private final class JCheckboxTableModel extends AbstractTableModel {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID = 7961524422591759713L;

		/**
		 * Konstruktor der Klasse
		 */
		public JCheckboxTableModel() {
			/*
			 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
			 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override public int getRowCount() {return keys.length;}
		@Override public int getColumnCount() {return 2;}
		@Override public Object getValueAt(int rowIndex, int columnIndex) {return (columnIndex==0)?select[rowIndex]:keys[rowIndex];}
		@Override public Class<?> getColumnClass(int columnIndex) {return (columnIndex==0)?Boolean.class:String.class;}
		@Override public boolean isCellEditable(int rowIndex, int columnIndex) {return columnIndex==0;}
		@Override public void setValueAt(Object aValue, int rowIndex, int columnIndex) {if (columnIndex==0) {select[rowIndex]=(Boolean)aValue; selectChanged();}}
	}
}

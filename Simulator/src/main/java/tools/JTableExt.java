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
package tools;

import java.awt.Component;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 * {@link JTableExt} stellt eine erweiterte Variante von {@link JTable} dar.
 * Durch den Aufruf von {@link #setIsPanelCellTable()} wird die Tabelle in einen
 * Modus geschaltet, in dem auch {@link JPanel} als Tabelleneinträge akzeptiert
 * werden. Steuerelemente auf den {@link JPanel} sind dabei nutzbar und die
 * Höhe passt sich automatisch an.
 * @see JTableExtAbstractTableModel
 * @author Alexander Herzog
 * @version 1.1
 */
public final class JTableExt extends JTable {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -4675269108501016811L;

	/**
	 * Anzahl an Spalten beim letzten Aufruf von
	 * {@link #tableChanged(TableModelEvent)}, um
	 * {@link #setPanelRendererAndEditor()} möglichst selten aufzurufen.
	 * @see #tableChanged(TableModelEvent)
	 */
	private int lastColCount=0;

	/**
	 * Anzahl an Zeilen beim letzten Aufruf von
	 * {@link #tableChanged(TableModelEvent)}, um
	 * {@link #editingStopped(javax.swing.event.ChangeEvent)} möglichst selten aufzurufen.
	 * @see #tableChanged(TableModelEvent)
	 */
	private int lastRowCount=0;

	/**
	 * Spalten, in denen {@link JPanel}-Elemente verwendet werden können.
	 * @see #setIsPanelCellTable(int)
	 * @see #setIsPanelCellTable()
	 */
	private final List<Integer> panelCellColumns=new ArrayList<>();

	/**
	 * Konstruktor der Klasse
	 */
	public JTableExt() {
		super();
		prepare();
	}

	/**
	 * Konstruktor der Klasse
	 * @param dm	Zu verwendendes Tabellenmodell
	 */
	public JTableExt(TableModel dm) {
		super(dm);
		prepare();
	}

	/**
	 * Konstruktor der Klasse
	 * @param dm	Zu verwendendes Tabellenmodell
	 * @param cm	Zu verwendendes Spaltenmodell
	 */
	public JTableExt(TableModel dm, TableColumnModel cm) {
		super(dm,cm);
		prepare();
	}

	/**
	 * Konstruktor der Klasse
	 * @param dm	Zu verwendendes Tabellenmodell
	 * @param cm	Zu verwendendes Spaltenmodell
	 * @param sm	Zu verwendendes Selektionsmodell
	 */
	public JTableExt(TableModel dm, TableColumnModel cm, ListSelectionModel sm) {
		super(dm,cm,sm);
		prepare();
	}

	/**
	 * Weitere Konfiguration der Tabelle
	 * (wird vom Konstruktor aufgerufen)
	 */
	private void prepare() {
		putClientProperty("terminateEditOnFocusLost",Boolean.TRUE);
		getTableHeader().setReorderingAllowed(false);
	}

	/**
	 * Aktiviert die Nutzbarkeit von {@link JPanel}-Elementen im {@link TableModel}.
	 */
	public void setIsPanelCellTable() {
		for (int i=0;i<100;i++) panelCellColumns.add(i);
		setPanelRendererAndEditor();
	}

	/**
	 * Aktiviert die Nutzbarkeit von {@link JPanel}-Elementen im {@link TableModel}.
	 * @param column Spalte, in der {@link JPanel}-Elementen eingesetzt werden sollen. (Fehlt dieses Argument, so werden alle Spalten freigegeben.)
	 */
	public void setIsPanelCellTable(int column) {
		if (panelCellColumns.indexOf(column)<0) panelCellColumns.add(column);
		setPanelRendererAndEditor();
	}

	/**
	 * Stellt den Zellen-Renderer und den Zellen-Editor ein.
	 * @see CellRendererExt
	 * @see CellEditorExt
	 */
	private void setPanelRendererAndEditor() {
		for (int col=0;col<getColumnCount();col++) if (panelCellColumns.indexOf(col)>=0) {
			getColumnModel().getColumn(col).setCellRenderer(new CellRendererExt());
			getColumnModel().getColumn(col).setCellEditor(new CellEditorExt());
		}
	}

	/**
	 * Höhe einer Zeile anpassen.
	 * @param rowIndex	0-basierter Zeilenindex
	 * @param margin	Zusätzlicher Abstand
	 * @return	Liefert die neue Zeilenhöhe
	 */
	private int getPreferredRowHeight(int rowIndex, int margin) {
		int height=0;

		for (int c=0; c<getColumnCount(); c++) {
			TableCellRenderer renderer=getCellRenderer(rowIndex,c);
			Component comp=prepareRenderer(renderer,rowIndex,c);
			int h=comp.getPreferredSize().height+2*margin;
			height=Math.max(height,h);
		}
		return height;
	}

	/**
	 * Höhe aller Zeilen anpassen.
	 * @param margin	Zusätzlicher Abstand
	 */
	private void setPreferredRowHeight(int margin) {
		for (int row=0;row<getRowCount();row++) {
			int newHeight=getPreferredRowHeight(row,margin);
			if (newHeight!=getRowHeight(row)) setRowHeight(row,newHeight);
		}
	}

	/**
	 * Wurde bereits ein Changed-Ereignis ausgelöst
	 * (und das zugehörige Neuzeichnen noch nicht abgearbeitet)?
	 * @see #tableChangeTriggered
	 */
	private volatile boolean tableChangeTriggered=false;

	@Override
	public void tableChanged(TableModelEvent e) {
		if (!tableChangeTriggered) {
			tableChangeTriggered=true;

			/* TableExt-Verarbeitung */
			SwingUtilities.invokeLater(()->{
				tableChangeTriggered=false;

				/* Höhe der Zeilen anpassen */
				setPreferredRowHeight(0);

				/* ggf. CellRenderer und Editoren setzen */
				if (getColumnCount()!=lastColCount && !panelCellColumns.isEmpty()) {
					lastColCount=getColumnCount();
					setPanelRendererAndEditor();
				}

				/* nach Zeile hinzu/weg alles neu aufbauen */
				if (getRowCount()!=lastRowCount) {
					lastRowCount=getRowCount();
					editingStopped(null);
				}
			});
		}

		/* Normale Verarbeitung des Events */
		super.tableChanged(e);
	}

	/**
	 * Renderer für die Zellen
	 * @see JTableExt#setPanelRendererAndEditor()
	 *
	 */
	private final class CellRendererExt extends DefaultTableCellRenderer {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID = -6047418281147307757L;
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			if (value instanceof JComponent) return (JComponent)value;
			return super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
		}
	}

	/**
	 * Editor für die Zellen
	 * @see JTableExt#setPanelRendererAndEditor()
	 */
	private static final class CellEditorExt extends AbstractCellEditor implements TableCellEditor {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID = 6362443731642362983L;
		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			return (value instanceof JComponent)?(JComponent)value:null;
		}
		@Override public Object getCellEditorValue() {return null;}
		@Override public boolean isCellEditable(EventObject anEvent) {return true;}
		@Override public boolean shouldSelectCell(EventObject anEvent) {return false;}
	}
}

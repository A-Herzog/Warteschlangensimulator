/**
 * Copyright 2024 Alexander Herzog
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
package ui.modeleditor.elements;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;

import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import tools.JTableExt;
import tools.JTableExtAbstractTableModel;

/**
 * Tabellenmodell zum Bearbeiten von {@link ModelElementAnimationTable} Tabellendaten
 * @author Alexander Herzog
 * @see ModelElementAnimationTable
 */
public class AnimationTableModel extends JTableExtAbstractTableModel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-1200965405567341342L;

	/** Zugehörige Tabelle (um das Update der Tabelle veranlassen zu können, wenn sich die Daten verändert haben) */
	private final JTableExt table;
	/** Inhalte der Tabellenzellen */
	private final List<List<ModelElementAnimationTable.Cell>> cells;

	/**
	 * Konstruktor
	 * @param table	Zugehörige Tabelle (um das Update der Tabelle veranlassen zu können, wenn sich die Daten verändert haben)
	 * @param cells	Inhalte der Tabellenzellen
	 * @param readOnly	Nur-Lese-Status
	 * @param editModel	Zugehöriges Modell (für den Expression Builder)
	 * @param helpRunnable	Hilfe-Callback
	 */
	private AnimationTableModel(final JTableExt table, final List<List<ModelElementAnimationTable.Cell>> cells, final boolean readOnly, final EditModel editModel, final Runnable helpRunnable) {
		this.table=table;
		this.cells=cells;

		if (!readOnly) table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e)) {
					final int row=table.rowAtPoint(e.getPoint());
					final int column=table.columnAtPoint(e.getPoint());
					final AnimationTableModelDialog dialog=new AnimationTableModelDialog(table,cells.get(row).get(column),editModel,helpRunnable);
					if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) updateTable();
				}
			}
		});
	}

	/**
	 * Aktualisiert die Tabellendarstellung
	 */
	private void updateTable() {
		fireTableStructureChanged();
		fireTableDataChanged();
		TableCellEditor cellEditor=table.getCellEditor();
		if (cellEditor!=null) cellEditor.stopCellEditing();
	}

	/**
	 * Stellt eine neue Größe für die Tabelle ein.
	 * @param rowCount	Anzahl an Zeilen
	 * @param colCount	Anzahl an Spalten
	 */
	public void setSize(int rowCount, int colCount) {
		rowCount=Math.max(1,Math.min(10,rowCount));
		colCount=Math.max(1,Math.min(10,colCount));

		/* Zeilenanzahl anpassen */
		while (cells.size()<rowCount) {
			final List<ModelElementAnimationTable.Cell> row=new ArrayList<>();
			for (int i=0;i<cells.get(0).size();i++) row.add(new ModelElementAnimationTable.Cell());
			cells.add(row);
		}
		while (cells.size()>rowCount) {
			cells.remove(cells.size()-1);
		}

		/* Spaltenanzahl anpassen */
		while (cells.get(0).size()<colCount) {
			for (var row: cells) row.add(new ModelElementAnimationTable.Cell());
		}
		while (cells.get(0).size()>colCount) {
			for (var row: cells) row.remove(row.size()-1);
		}

		updateTable();
	}

	/**
	 * Liefert die aktuellen Tabellendaten.
	 * @return	Aktuelle Tabellendaten
	 */
	public List<List<ModelElementAnimationTable.Cell>> getCells() {
		return cells;
	}

	@Override
	public int getRowCount() {
		return cells.size();
	}

	@Override
	public int getColumnCount() {
		return cells.get(0).size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex<0 || rowIndex>=cells.size()) return "";
		if (columnIndex<0 || columnIndex>=cells.get(0).size()) return "";
		return cells.get(rowIndex).get(columnIndex).text;
	}

	/**
	 * Erstellt Tabelle und Tabellenmodell zum Bearbeiten der Daten
	 * @param cells	Tabellendaten, die bearbeitet werden sollen
	 * @param readOnly	Nur-Lese-Status
	 * @param helpRunnable	Hilfe-Runnable
	 * @param editModel	Zugehöriges Modell (für den Expression Builder)
	 * @return	2-elementiges Array aus <code>JScrollPane</code> (welches die Tabelle enthält) und dem Tabellenmodell
	 */
	public static Object[] buildTable(final List<List<ModelElementAnimationTable.Cell>> cells, final boolean readOnly, final EditModel editModel, final Runnable helpRunnable) {
		final JTableExt table=new JTableExt();
		final AnimationTableModel model;

		table.setModel(model=new AnimationTableModel(table,cells,readOnly,editModel,helpRunnable));
		table.setIsPanelCellTable(0);
		table.setEnabled(!readOnly);

		return new Object[]{new JScrollPane(table),model};
	}
}

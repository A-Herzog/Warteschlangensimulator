/**
 * Copyright 2026 Alexander Herzog
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

import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.JScrollPane;
import javax.swing.table.TableCellEditor;

import language.Language;
import mathtools.NumberTools;
import tools.JTableExt;
import tools.JTableExtAbstractTableModel;
import ui.images.Images;

/**
 * Diese Klasse hält ein Tabellenmodell für die Verwendung
 * in {@link ModelElementSignalMultiDialog} vor.
 * @author Alexander Herzog
 * @see ModelElementSignalMultiTableModel#buildTable(ModelElementSignalMulti, boolean)
 */
public class ModelElementSignalMultiTableModel extends JTableExtAbstractTableModel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-6813297564378913052L;

	/** Tabelle, in der das Modell verwendet werden soll */
	private final JTableExt table;
	/** Editormodell, aus dem die Daten entnommen und in die die Daten zurückgeschrieben werden sollen */
	private final ModelElementSignalMulti element;
	/** Nur-Lese-Status */
	private final boolean readOnly;
	/** Liste der Teilsignale */
	private List<ModelElementSignalMultiRecord> records;

	/**
	 * Konstruktor der Klasse
	 * @param table	Tabelle, in der das Modell verwendet werden soll
	 * @param element	Editormodell, aus dem die Daten entnommen und in die die Daten zurückgeschrieben werden sollen
	 * @param readOnly	Nur-Lese-Status
	 */
	public ModelElementSignalMultiTableModel(final JTableExt table, final ModelElementSignalMulti element, final boolean readOnly) {
		super();
		this.table=table;
		this.element=element;
		this.readOnly=readOnly;

		records=element.getRecords().stream().map(ModelElementSignalMultiRecord::new).collect(Collectors.toList());

		updateTable();
	}

	/**
	 * Aktualisiert die Tabellendarstellung
	 */
	private void updateTable() {
		fireTableDataChanged();
		TableCellEditor cellEditor=table.getCellEditor();
		if (cellEditor!=null) cellEditor.stopCellEditing();
	}

	@Override
	public int getRowCount() {
		return records.size()+1;
	}

	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0: return Language.tr("Surface.SignalMulti.Table.Heading.SignalName");
		case 1: return Language.tr("Surface.SignalMulti.Table.Heading.Settings");
		case 2: return "";
		default: return super.getColumnName(column);
		}
	}

	/**
	 * Liefert einen Zelleneintrag für die letzte Zeile.
	 * @param columnIndex	Spalte
	 * @return	Zelleneintrag für die letzte Zeile
	 */
	private Object getValueAtLastRow(int columnIndex) {
		if (columnIndex<2) {
			return "";
		} else {
			return makeButtonPanel(
					new String[] {""},
					new String[] {Language.tr("Surface.SignalMulti.Table.Heading.Add")},
					new  Icon[] {Images.EDIT_ADD.getIcon()},
					new ActionListener[] {e->addSignal()});
		}
	}

	/**
	 * Liefert den Wert einer Zelle
	 * @param rowIndex	Zeilenindex
	 * @param columnIndex	Spaltenindex
	 * @param record	Anzuzeigender Datensatz
	 * @return	Wert der Zelle
	 */
	private Object getValueAt(int rowIndex, int columnIndex, final ModelElementSignalMultiRecord record) {
		if (columnIndex==0) {
			return record.getName();
		}

		if (columnIndex==1) {
			final var info=new StringBuilder();
			if (record.getSignalDelay()>0) info.append(Language.tr("Surface.SignalMulti.Delay")+": "+NumberTools.formatNumberMax(record.getSignalDelay())+" "+Language.tr("Statistics.Seconds"));
			if (!record.getCondition().isBlank()) {
				if (info.length()>0) info.append(", ");
				info.append(Language.tr("Surface.SignalMulti.Condition")+": "+record.getCondition());
			}
			return info.toString();
		}

		if (columnIndex==2) {
			final List<String> titles=new ArrayList<>();
			final List<String> tooltips=new ArrayList<>();
			final List<Icon> icons=new ArrayList<>();
			final List<ActionListener> listeners=new ArrayList<>();

			titles.add("");
			tooltips.add(Language.tr("Surface.SignalMulti.Table.Heading.Edit"));
			icons.add(Images.GENERAL_EDIT.getIcon());
			listeners.add(e->editSignal(rowIndex));

			titles.add("");
			tooltips.add(Language.tr("Surface.SignalMulti.Table.Heading.Delete"));
			icons.add(Images.EDIT_DELETE.getIcon());
			listeners.add(e->deleteSignal(rowIndex));

			if (rowIndex>0) {
				titles.add("");
				tooltips.add(Language.tr("Surface.SignalMulti.Table.Heading.MoveUp"));
				icons.add(Images.ARROW_UP.getIcon());
				listeners.add(e->moveUp(rowIndex));
			}

			if (rowIndex<this.records.size()-1) {
				titles.add("");
				tooltips.add(Language.tr("Surface.SignalMulti.Table.Heading.MoveDown"));
				icons.add(Images.ARROW_DOWN.getIcon());
				listeners.add(e->moveDown(rowIndex));
			}

			return makeButtonPanel(
					titles.toArray(String[]::new),
					tooltips.toArray(String[]::new),
					icons.toArray(Icon[]::new),
					listeners.toArray(ActionListener[]::new));
		}

		return "";
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex==records.size()) return getValueAtLastRow(columnIndex);
		return getValueAt(rowIndex,columnIndex,records.get(rowIndex));
	}

	/**
	 * Erzeugt Tabelle und Tabellenmodell
	 * @param element	Editormodell, aus dem die Daten entnommen und in die die Daten zurückgeschrieben werden sollen
	 * @param readOnly	Nur-Lese-Status
	 * @return	Array aus zwei Elementen: JScrollPane das die Tabelle enthält und Tabellenmodell
	 */
	public static Object[] buildTable(final ModelElementSignalMulti element, final boolean readOnly) {
		final JTableExt table=new JTableExt();
		final ModelElementSignalMultiTableModel model;

		table.setModel(model=new ModelElementSignalMultiTableModel(table,element,readOnly));

		table.getColumnModel().getColumn(0).setMaxWidth(150);
		table.getColumnModel().getColumn(0).setMinWidth(150);
		table.getColumnModel().getColumn(2).setMaxWidth(100);
		table.getColumnModel().getColumn(2).setMinWidth(100);
		table.setIsPanelCellTable(2);
		table.setEnabled(!readOnly);

		return new Object[]{new JScrollPane(table),model};
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return !readOnly && columnIndex==2;
	}

	/**
	 * Fügt eine Zeile zu der Tabelle hinzu
	 */
	private void addSignal() {
		records.add(new ModelElementSignalMultiRecord("Signal "+(records.size()+1)));
		updateTable();
	}

	/**
	 * Öffnet den Bearbeitendialog für eine Zeile.
	 * @param index	Zu bearbeitende Zeile
	 */
	private void editSignal(final int index) {
		new ModelElementSignalMultiTableModelDialog(table,records.get(index),element.getModel());
	}

	/**
	 * Löscht einen Eintrag in der Tabelle
	 * @param index	Zu löschende Zeile
	 */
	private void deleteSignal(final int index) {
		records.remove(index);
		updateTable();
	}

	/**
	 * Verschiebt einen Eintrag um eine Position in der Liste nach oben
	 * @param index	Nach oben zu verschiebende Zeile
	 */
	private void moveUp(final int index) {
		final ModelElementSignalMultiRecord record=records.get(index-1);
		records.set(index-1,records.get(index));
		records.set(index,record);

		updateTable();
	}

	/**
	 * Verschiebt einen Eintrag um eine Position in der Liste nach unten
	 * @param index	Nach unten zu verschiebende Zeile
	 */
	private void moveDown(final int index) {
		final ModelElementSignalMultiRecord record=records.get(index+1);
		records.set(index+1,records.get(index));
		records.set(index,record);

		updateTable();
	}

	/**
	 * Prüft die Eingaben
	 * @param showErrorMessage	Fehlermeldung anzeigen?
	 * @return	Sind alle Eingaben ok?
	 */
	public boolean checkData(final boolean showErrorMessage) {
		return true;
	}

	/**
	 * Schreibt die Eingaben in das im Konstruktor angegebene {@link ModelElementCounterMulti}-Objekt zurück.
	 */
	public void storeData() {
		if (readOnly) return;

		final List<ModelElementSignalMultiRecord> elementRecords=element.getRecords();
		elementRecords.clear();
		elementRecords.addAll(records);
	}
}
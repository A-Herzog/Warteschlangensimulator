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
package ui.modeleditor.elements;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JScrollPane;
import javax.swing.table.TableCellEditor;

import language.Language;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.JTableExt;
import tools.JTableExtAbstractTableModel;
import ui.images.Images;

/**
 * Tabellenmodell zum Bearbeiten von {@link ModelElementAssignStringRecord}-Textzuweisungen
 * @author Alexander Herzog
 * @see ModelElementAssignStringRecord
 * @see ModelElementAssignStringDialog
 * @see ModelElementAssign
 * @see ModelElementSourceRecordPanel
 */
public class VariablesTextsTableModel extends JTableExtAbstractTableModel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 2614695330555791582L;

	/** Zugehörige Tabelle (um das Update der Tabelle veranlassen zu können, wenn sich die Daten verändert haben) */
	private final JTableExt table;
	/** Objekt welches die Textzuweisungen vorhält */
	private final ModelElementAssignStringRecord record;
	/** Nur-Lese-Status */
	private final boolean readOnly;
	/** Hilfe-Callback */
	private final Runnable helpRunnable;

	/** Liste aller Schlüssel */
	private final List<String> keys;
	/** Liste aller Werte */
	private final List<String> values;

	/**
	 * Konstruktor der Klasse
	 * @param table	Zugehörige Tabelle (um das Update der Tabelle veranlassen zu können, wenn sich die Daten verändert haben)
	 * @param record	Objekt welches die Textzuweisungen vorhält
	 * @param readOnly	Nur-Lese-Status
	 * @param helpRunnable	Hilfe-Callback
	 */
	public VariablesTextsTableModel(final JTableExt table, final ModelElementAssignStringRecord record, final boolean readOnly, final Runnable helpRunnable) {
		super();
		this.table=table;
		this.record=record;
		this.readOnly=readOnly;
		this.helpRunnable=helpRunnable;

		keys=new ArrayList<>(record.getKeys());
		values=new ArrayList<>(record.getValues());

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
		return keys.size()+1;
	}

	@Override
	public int getColumnCount() {
		return 1;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex==keys.size()) {
			return makeButtonPanel(new String[]{Language.tr("Surface.AssignString.Table.Add")},new Icon[]{Images.EDIT_ADD.getIcon()},new ActionListener[]{new AddButtonListener()});
		}

		final String text=keys.get(rowIndex)+":="+values.get(rowIndex);
		final List<Icon> icons=new ArrayList<>();
		icons.add(Images.GENERAL_SETUP.getIcon());
		icons.add(Images.EDIT_DELETE.getIcon());
		final List<String> hints=new ArrayList<>();
		hints.add(Language.tr("Surface.AssignString.Table.Edit"));
		hints.add(Language.tr("Surface.AssignString.Table.Delete"));
		final List<ActionListener> listeners=new ArrayList<>();
		listeners.add(new EditButtonListener(0,rowIndex));
		listeners.add(new DeleteButtonListener(rowIndex));

		if (rowIndex>0) {
			icons.add(Images.ARROW_UP.getIcon());
			hints.add(Language.tr("Surface.AssignString.Table.Up"));
			listeners.add(new EditButtonListener(-1,rowIndex));
		}

		if (rowIndex<keys.size()-1) {
			icons.add(Images.ARROW_DOWN.getIcon());
			hints.add(Language.tr("Surface.AssignString.Table.Down"));
			listeners.add(new EditButtonListener(1,rowIndex));
		}

		return makeEditPanelSmallBorder(
				text,
				icons.toArray(Icon[]::new),
				hints.toArray(String[]::new),
				listeners.toArray(ActionListener[]::new));
	}

	@Override
	public final boolean isCellEditable(int rowIndex, int columnIndex) {
		return !readOnly;
	}

	/**
	 * Reagiert auf Klicks auf die Hinzufügen-Schaltfläche
	 */
	private class AddButtonListener implements ActionListener {
		/**
		 * Konstruktor der Klasse
		 */
		public AddButtonListener() {
			/*
			 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
			 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (readOnly) return;

			final VariablesTextsTableModelDialog dialog=new VariablesTextsTableModelDialog(table,helpRunnable);
			if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
				keys.add(dialog.getKey());
				values.add(dialog.getValue());
				updateTable();
			}
		}
	}

	/**
	 * Reagiert auf Klicks auf die Bearbeiten- und Verschieben-Schaltflächen
	 * in der Tabelle
	 */
	private class EditButtonListener implements ActionListener {
		/** Auszuführender Befehl (0: Eintrag bearbeiten, -1: Eintrag nach oben verschieben, 1: Eintrag nach unten verschieben) */
		private final int col;
		/** Zeilennummer */
		private final int row;

		/**
		 * Konstruktor der Klasse
		 * @param col	Auszuführender Befehl (0: Eintrag bearbeiten, -1: Eintrag nach oben verschieben, 1: Eintrag nach unten verschieben)
		 * @param row	Zeilennummer
		 */
		public EditButtonListener(final int col, final int row) {
			this.col=col;
			this.row=row;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (readOnly) return;
			String s;

			switch (col) {
			case 0:
				final VariablesTextsTableModelDialog dialog=new VariablesTextsTableModelDialog(table,keys.get(row),values.get(row),helpRunnable);
				if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
					keys.set(row,dialog.getKey());
					values.set(row,dialog.getValue());
					updateTable();
				}
				break;
			case -1:
				if (row>0) {
					s=keys.get(row); keys.set(row,keys.get(row-1)); keys.set(row-1,s);
					s=values.get(row); values.set(row,values.get(row-1)); values.set(row-1,s);
					updateTable();
				}
				break;
			case 1:
				if (row<keys.size()-1) {
					s=keys.get(row); keys.set(row,keys.get(row+1)); keys.set(row+1,s);
					s=values.get(row); values.set(row,values.get(row+1)); values.set(row+1,s);
					updateTable();
				}
				break;
			}
		}
	}

	/**
	 * Reagiert auf Klicks auf die Löschen-Schaltflächen
	 */
	private class DeleteButtonListener implements ActionListener {
		/** Zeilennummer */
		private final int row;

		/**
		 * Konstruktor der Klasse
		 * @param row	Zeilennummer
		 */
		public DeleteButtonListener(final int row) {
			this.row=row;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (readOnly) return;

			final String name=keys.get(row)+":="+values.get(row);
			if ((e.getModifiers() & ActionEvent.SHIFT_MASK)==0) {
				if (!MsgBox.confirm(table,Language.tr("Surface.AssignString.Table.Delete.Confirm.Title"),String.format(Language.tr("Surface.AssignString.Table.Delete.Confirm.Info"),name),Language.tr("Surface.AssignString.Table.Delete.Confirm.YesInfo"),Language.tr("Surface.AssignString.Table.Delete.Confirm.NoInfo"))) return;
			}

			keys.remove(row);
			values.remove(row);
			updateTable();
		}
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0: return Language.tr("Surface.AssignString.Table.Heading");
		default: return super.getColumnName(column);
		}
	}

	/**
	 * Schreibt die Daten aus der Tabelle in das im Konstruktor
	 * angegebene {@link ModelElementAssignStringRecord}-Objekt zurück.
	 */
	public void storeData() {
		if (readOnly) return;

		if (record!=null) {
			record.getKeys().clear();
			record.getKeys().addAll(keys);
			record.getValues().clear();
			record.getValues().addAll(values);
		}
	}

	/**
	 * Erstellt Tabelle und Tabellenmodell zum Bearbeiten von Zuweisungen
	 * @param record	Zuordnungsdatensatz, der bearbeitet werden soll
	 * @param readOnly	Nur-Lese-Status
	 * @param helpRunnable	Hilfe-Runnable
	 * @return	2-elementiges Array aus <code>JScrollPane</code> (welches die Tabelle enthält) und dem Tabellenmodell
	 */
	public static Object[] buildTable(final ModelElementAssignStringRecord record, final boolean readOnly, final Runnable helpRunnable) {
		final JTableExt table=new JTableExt();
		final VariablesTextsTableModel varModel;

		table.setModel(varModel=new VariablesTextsTableModel(table,record,readOnly,helpRunnable));
		table.setIsPanelCellTable(0);
		table.setEnabled(!readOnly);

		return new Object[]{new JScrollPane(table),varModel};
	}
}
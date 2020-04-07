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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.table.TableCellEditor;

import language.Language;
import tools.JTableExt;
import tools.JTableExtAbstractTableModel;
import ui.images.Images;
import ui.modeleditor.ModelSurface;

/**
 * Diese Tabelle wird innerhalb eines {@link ModelElementSourceRecordPanel} verwendet,
 * um Signale zu definieren bei deren Auslösen Ankünfte generiert werden sollen.
 * @author Alexander Herzog
 * @see ModelElementSourceRecordPanel
 */
public class ModelElementSourceRecordSignalTableModel extends JTableExtAbstractTableModel {
	private static final long serialVersionUID = 1037435442061965707L;

	private final JTableExt table;
	private final boolean readOnly;
	private final List<String> signalNames;
	private final List<String> data;

	/**
	 * Konstruktor der klasse
	 * @param table	Zugehörige Tabelle (um diese zu einem Update zu veranlassen, wenn sich die Daten geändert haben)
	 * @param mainSurface	Haupt-Zeichenfläche (aus dieser werden die Namen aller Signale ausgelesen, {@link ModelSurface#getAllSignalNames()})
	 * @param readOnly	Nur-Lese-Status
	 */
	public ModelElementSourceRecordSignalTableModel(final JTableExt table, final ModelSurface mainSurface, final boolean readOnly) {
		super();
		this.table=table;
		this.readOnly=readOnly;
		data=new ArrayList<>();
		signalNames=mainSurface.getAllSignalNames();
	}

	private void updateTable() {
		fireTableDataChanged();
		TableCellEditor cellEditor=table.getCellEditor();
		if (cellEditor!=null) cellEditor.stopCellEditing();
	}

	@Override
	public final boolean isCellEditable(int rowIndex, int columnIndex) {
		return !readOnly;
	}

	/**
	 * Stellt die initiale Liste der Signale zur Kundengenerierung ein.
	 * @param signals	Initiale Liste der Signale zur Kundengenerierung ein
	 * @see #getData()
	 */
	public void setData(final List<String> signals) {
		data.clear();
		if (signals!=null) data.addAll(signals);
	}

	/**
	 * Liefert die neue Liste der Signale zur Kundengenerierung.
	 * @return	Neue Liste der Signale zur Kundengenerierung
	 * @see #setData(List)
	 */
	public List<String> getData() {
		return data;
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0: return Language.tr("Surface.Source.Dialog.Signals.TableCol.SignalName");
		case 1: return Language.tr("Surface.Source.Dialog.Signals.TableCol.Control");
		default: return super.getColumnName(column);
		}
	}

	@Override
	public int getRowCount() {
		return data.size()+1;
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	private JComboBox<String> getSignalCombo(final int rowIndex, final String signal) {
		final JComboBox<String> combo=new JComboBox<>(signalNames.toArray(new String[0]));

		int selIndex=signalNames.indexOf(signal);
		if (selIndex<0 && signalNames.size()>0) selIndex=0;
		if (selIndex>=0) combo.setSelectedIndex(selIndex);
		combo.addActionListener(e->{
			data.set(rowIndex,(String)combo.getSelectedItem());
		});

		return combo;
	}


	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex==data.size()) {
			if (columnIndex==0) return makeButtonPanel(new String[]{Language.tr("Surface.Source.Dialog.Signals.Table.Add")},new URL[]{Images.EDIT_ADD.getURL()},new ActionListener[]{new TableButtonListener(ActionIndex.ACTION_ADD)});
			return "";
		}

		switch (columnIndex) {
		case 0:
			return getSignalCombo(rowIndex,data.get(rowIndex));
		case 1:
			final List<URL> icons=new ArrayList<>();
			final List<String> hints=new ArrayList<>();
			final List<ActionListener> listeners=new ArrayList<>();
			icons.add(Images.EDIT_DELETE.getURL());
			hints.add(Language.tr("Surface.Source.Dialog.Signals.Table.Delete"));
			listeners.add(new TableButtonListener(rowIndex,ActionIndex.ACTION_DELETE));
			return makeButtonPanel(null,hints.toArray(new String[0]),icons.toArray(new URL[0]),listeners.toArray(new ActionListener[0]));
		}

		return null;
	}

	private enum ActionIndex {ACTION_ADD, ACTION_DELETE}

	private class TableButtonListener implements ActionListener {
		final int row;
		final ActionIndex actionIndex;

		public TableButtonListener(final int row, final ActionIndex actionIndex) {
			this.row=row;
			this.actionIndex=actionIndex;
		}

		public TableButtonListener(final ActionIndex actionIndex) {
			this(0,actionIndex);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (readOnly) return;
			switch (actionIndex) {
			case ACTION_ADD:
				if (signalNames.size()>0) data.add(signalNames.get(0));
				updateTable();
				break;
			case ACTION_DELETE:
				data.remove(row);
				updateTable();
				break;
			}
		}
	}
}

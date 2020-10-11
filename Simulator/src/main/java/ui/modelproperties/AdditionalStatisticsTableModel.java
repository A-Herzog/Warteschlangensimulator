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
package ui.modelproperties;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.TableCellEditor;

import language.Language;
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.JTableExt;
import tools.JTableExtAbstractTableModel;
import ui.images.Images;
import ui.modeleditor.ModelLongRunStatisticsElement;

/**
 * Diese Klasse hält das Tabellenmodell für die Anzeige von Laufzeitstatistikdaten
 * im Modelleigenschaften-Dialog vor.
 * @author Alexander Herzog
 * @see ModelPropertiesDialog
 */
public class AdditionalStatisticsTableModel extends JTableExtAbstractTableModel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 3634658121731799874L;

	/** Vollständiges Editor-Modell (wird für den Expression-Builder benötigt) */
	private final EditModel model;
	/** Zu dem Tabellenmodell gehörenden Tabellenobjekt (wird sowohl zum Ausrichten von Dialogen als auch um ein Update der Tabelle anzustoßen benötigt) */
	private final JTableExt table;
	/** Nur-Lese-Status */
	private final boolean readOnly;
	/** Hilfe-Callback */
	private final Runnable help;
	/** Liste mit den Daten für die Anzeige */
	private final List<ModelLongRunStatisticsElement> data;

	/**
	 * Konstruktor der Klasse
	 * @param model	Vollständiges Editor-Modell (wird für den Expression-Builder benötigt)
	 * @param table	Zu dem Tabellenmodell gehörenden Tabellenobjekt (wird sowohl zum Ausrichten von Dialogen als auch um ein Update der Tabelle anzustoßen benötigt)
	 * @param readOnly	Nur-Lese-Status
	 * @param help	Hilfe-Callback
	 */
	public AdditionalStatisticsTableModel(final EditModel model, final JTableExt table, final boolean readOnly, final Runnable help) {
		super();
		this.model=model;
		this.table=table;
		this.readOnly=readOnly;
		this.help=help;
		data=new ArrayList<>();
		for (ModelLongRunStatisticsElement element: model.longRunStatistics.getData()) data.add(element.clone());
		updateTable();
	}

	/**
	 * Veranlasst die Aktualisierung der Tabellendarstellung.
	 */
	private void updateTable() {
		fireTableDataChanged();
		TableCellEditor cellEditor=table.getCellEditor();
		if (cellEditor!=null) cellEditor.stopCellEditing();
	}

	@Override
	public int getRowCount() {
		return data.size()+1;
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex==data.size()) {
			switch (columnIndex) {
			case 0:	return makeButtonPanel(new String[]{Language.tr("Editor.Dialog.Tab.RunTimeStatistics.Table.Add")},new URL[]{Images.MODELPROPERTIES_RUNTIME_STATISTICS.getURL()},new ActionListener[]{new AddButtonListener()});
			case 1: return "";
			}
		}

		switch (columnIndex) {
		case 0:
			final List<URL> urls=new ArrayList<>();
			final List<String> hints=new ArrayList<>();
			final List<ActionListener> actions=new ArrayList<>();

			urls.add(Images.GENERAL_SETUP.getURL());
			hints.add(Language.tr("Editor.Dialog.Tab.RunTimeStatistics.Table.Edit"));
			actions.add(new EditButtonListener(0,rowIndex));
			urls.add(Images.EDIT_DELETE.getURL());
			hints.add(Language.tr("Editor.Dialog.Tab.RunTimeStatisticsTable.Delete"));
			actions.add(new DeleteButtonListener(rowIndex));
			if (rowIndex>0) {
				urls.add(Images.ARROW_UP.getURL());
				hints.add(Language.tr("Editor.Dialog.Tab.RunTimeStatistics.Table.MoveUp"));
				actions.add(new EditButtonListener(1,rowIndex));
			}
			if (rowIndex<data.size()-1) {
				urls.add(Images.ARROW_DOWN.getURL());
				hints.add(Language.tr("Editor.Dialog.Tab.RunTimeStatistics.Table.MoveDown"));
				actions.add(new EditButtonListener(2,rowIndex));
			}
			return makeEditPanelSmallBorder(
					Images.MODELPROPERTIES_RUNTIME_STATISTICS.getURL(),
					data.get(rowIndex).expression,
					urls.toArray(new URL[0]),hints.toArray(new String[0]),actions.toArray(new ActionListener[0])
					);
		case 1:
			return makePanel(data.get(rowIndex).getModeInfo(),null);
		default:
			return null;
		}
	}

	@Override
	public final boolean isCellEditable(int rowIndex, int columnIndex) {
		return !readOnly;
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0: return Language.tr("Editor.Dialog.Tab.RunTimeStatistics.Table.Expressions");
		case 1: return Language.tr("Editor.Dialog.Tab.RunTimeStatistics.Table.Type");
		default: return super.getColumnName(column);
		}
	}

	private class AddButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (readOnly) return;
			AdditionalStatisticsTableModelDialog dialog=new AdditionalStatisticsTableModelDialog(table,null,model,help);
			dialog.setVisible(true);
			if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
				final ModelLongRunStatisticsElement element=dialog.getElement();
				if (element!=null) {data.add(element); updateTable();}
			}
		}
	}

	private class EditButtonListener implements ActionListener {
		private final int col;
		private final int row;

		public EditButtonListener(final int col, final int row) {
			this.col=col;
			this.row=row;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (readOnly) return;

			ModelLongRunStatisticsElement element;

			switch (col) {
			case 0:
				final AdditionalStatisticsTableModelDialog dialog=new AdditionalStatisticsTableModelDialog(table,data.get(row),model,help);
				dialog.setVisible(true);
				if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
					element=dialog.getElement();
					if (element!=null) {data.set(row,element); updateTable();}
				}
				break;
			case 1:
				element=data.get(row);
				data.set(row,data.get(row-1));
				data.set(row-1,element);
				updateTable();
				break;
			case 2:
				element=data.get(row);
				data.set(row,data.get(row+1));
				data.set(row+1,element);
				updateTable();
				break;
			}
		}
	}

	private class DeleteButtonListener implements ActionListener {
		private final int row;

		public DeleteButtonListener(final int row) {
			this.row=row;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (readOnly) return;
			final String name=data.get(row).expression;
			if (!MsgBox.confirm(table,Language.tr("Editor.Dialog.Tab.RunTimeStatisticsTable.Delete.ConfirmTitle"),String.format(Language.tr("Editor.Dialog.Tab.RunTimeStatisticsTable.Delete.ConfirmText"),name),Language.tr("Editor.Dialog.Tab.RunTimeStatisticsTable.Delete.YesInfo"),Language.tr("Editor.Dialog.Tab.RunTimeStatisticsTable.Delete.NoInfo"))) return;
			data.remove(row);
			updateTable();
		}
	}

	/**
	 * Liefert alle momentan in dem Datenmodell enthaltenen Statistikdatensätze
	 * @return	Liste mit den Statistikdatensätzen
	 */
	public List<ModelLongRunStatisticsElement> getData() {
		return data;
	}
}

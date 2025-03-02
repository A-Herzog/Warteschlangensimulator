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
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
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
			case 0:	return makeButtonPanel(new String[]{Language.tr("Editor.Dialog.Tab.RunTimeStatistics.Table.Add")},new Icon[]{Images.MODELPROPERTIES_RUNTIME_STATISTICS.getIcon()},new ActionListener[]{new AddButtonListener()});
			case 1: return "";
			}
		}

		switch (columnIndex) {
		case 0:
			final List<Icon> icons=new ArrayList<>();
			final List<String> hints=new ArrayList<>();
			final List<ActionListener> actions=new ArrayList<>();

			icons.add(Images.GENERAL_SETUP.getIcon());
			hints.add(Language.tr("Editor.Dialog.Tab.RunTimeStatistics.Table.Edit"));
			actions.add(new EditButtonListener(0,rowIndex));
			icons.add(Images.EDIT_DELETE.getIcon());
			hints.add(Language.tr("Editor.Dialog.Tab.RunTimeStatisticsTable.Delete"));
			actions.add(new DeleteButtonListener(rowIndex));
			if (rowIndex>0) {
				icons.add(Images.ARROW_UP.getIcon());
				hints.add(Language.tr("Editor.Dialog.Tab.RunTimeStatistics.Table.MoveUp"));
				actions.add(new EditButtonListener(1,rowIndex));
			}
			if (rowIndex<data.size()-1) {
				icons.add(Images.ARROW_DOWN.getIcon());
				hints.add(Language.tr("Editor.Dialog.Tab.RunTimeStatistics.Table.MoveDown"));
				actions.add(new EditButtonListener(2,rowIndex));
			}
			return makeEditPanelSmallBorderIcon(
					Images.MODELPROPERTIES_RUNTIME_STATISTICS.getIcon(),
					data.get(rowIndex).expression,
					icons.toArray(Icon[]::new),hints.toArray(String[]::new),actions.toArray(ActionListener[]::new)
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
			AdditionalStatisticsTableModelDialog dialog=new AdditionalStatisticsTableModelDialog(table,null,model,help);
			dialog.setVisible(true);
			if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
				final ModelLongRunStatisticsElement element=dialog.getElement();
				if (element!=null) {data.add(element); updateTable();}
			}
		}
	}

	/**
	 * Reagiert auf Klicks auf die Bearbeiten- und Verschieben-Schaltflächen
	 */
	private class EditButtonListener implements ActionListener {
		/** Spalte der Schaltfläche (0 : bearbeiten, 1: nach oben, 2: nach unten) */
		private final int col;
		/** Zeilennummer */
		private final int row;

		/**
		 * Konstruktor der Klasse
		 * @param col	Spalte der Schaltfläche (0 : bearbeiten, 1: nach oben, 2: nach unten)
		 * @param row	Zeilennummer
		 */
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
			final String name=data.get(row).expression;
			if ((e.getModifiers() & ActionEvent.SHIFT_MASK)==0) {
				if (!MsgBox.confirm(table,Language.tr("Editor.Dialog.Tab.RunTimeStatisticsTable.Delete.ConfirmTitle"),String.format(Language.tr("Editor.Dialog.Tab.RunTimeStatisticsTable.Delete.ConfirmText"),name),Language.tr("Editor.Dialog.Tab.RunTimeStatisticsTable.Delete.YesInfo"),Language.tr("Editor.Dialog.Tab.RunTimeStatisticsTable.Delete.NoInfo"))) return;
			}
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

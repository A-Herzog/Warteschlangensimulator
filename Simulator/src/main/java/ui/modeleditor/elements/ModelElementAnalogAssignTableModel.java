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

import javax.swing.table.TableCellEditor;

import language.Language;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.JTableExt;
import tools.JTableExtAbstractTableModel;
import ui.images.Images;
import ui.modeleditor.coreelements.ModelElement;

/**
 * Diese Tabelle hält die Datensätze für eine Station zum Zuweisen von
 * analogen Werte bzw. Änderungsraten ({@link ModelElementAnalogAssign}) vor.
 * @author Alexander Herzog
 * @see ModelElementAnalogAssign
 */
public class ModelElementAnalogAssignTableModel extends JTableExtAbstractTableModel {
	private static final long serialVersionUID = 2606544913680450121L;

	private final JTableExt table;
	private final Runnable help;
	private final ModelElementAnalogAssign element;
	private final List<Integer> changeID;
	private final List<ModelElementAnalogAssign.ChangeMode> changeMode;
	private final List<String> changeExpression;
	private final boolean readOnly;

	/**
	 * Konstruktor der Klasse
	 * @param table	Zugehörige Tabelle (um diese Anweisen zu können, sich zu aktualisieren)
	 * @param element	{@link ModelElementAnalogAssign}-Element welches die Zuweisungen enthält
	 * @param readOnly	Nur-Lese-Status
	 * @param help	Hilfe-Callback
	 */
	public ModelElementAnalogAssignTableModel(final JTableExt table, final ModelElementAnalogAssign element, final boolean readOnly, final Runnable help) {
		super();

		this.table=table;
		this.help=help;
		this.element=element;
		this.readOnly=readOnly;

		changeID=new ArrayList<>(element.getChangeID());
		changeMode=new ArrayList<>(element.getChangeMode());
		changeExpression=new ArrayList<>(element.getChangeExpression());

		updateTable();
	}

	private void updateTable() {
		fireTableDataChanged();
		TableCellEditor cellEditor=table.getCellEditor();
		if (cellEditor!=null) cellEditor.stopCellEditing();
	}

	@Override
	public int getRowCount() {
		return changeID.size()+1;
	}

	@Override
	public int getColumnCount() {
		return 4;
	}

	private String getChangeID(final int index) {
		final int id=changeID.get(index);
		final ModelElement elementID=element.getSurface().getByIdIncludingSubModels(id);
		final String name=elementID.getName();
		if (name==null || name.trim().isEmpty()) {
			return String.format(Language.tr("Surface.AnalogAssign.Dialog.ID.NoName"),id);
		} else {
			return String.format(Language.tr("Surface.AnalogAssign.Dialog.ID.Name"),id,name);
		}
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex==changeID.size()) {
			switch (columnIndex) {
			case 3:	return makeButtonPanel(new String[]{Language.tr("Surface.AnalogAssign.Dialog.Add")},new URL[]{Images.MODELEDITOR_ELEMENT_ANALOG_ASSIGN.getURL()},new ActionListener[]{new EditButtonListener(0,-1)});
			default: return "";
			}
		}

		switch (columnIndex) {
		case 0:
			return getChangeID(rowIndex);
		case 1:
			switch (changeMode.get(rowIndex)) {
			case CHANGE_MODE_VALUE: return Language.tr("Surface.AnalogAssign.Dialog.ModeValue");
			case CHANGE_MODE_RATE: return Language.tr("Surface.AnalogAssign.Dialog.ModeRate");
			}
			return null;
		case 2:
			return changeExpression.get(rowIndex);
		case 3:
			final List<String> title=new ArrayList<>();
			final List<String> tooltip=new ArrayList<>();
			final List<URL> iconURL=new ArrayList<>();
			final List<ActionListener> listener=new ArrayList<>();

			title.add("");
			tooltip.add(Language.tr("Surface.AnalogAssign.Dialog.Edit.Hint"));
			iconURL.add(Images.GENERAL_SETUP.getURL());
			listener.add(new EditButtonListener(0,rowIndex));

			title.add("");
			tooltip.add(Language.tr("Surface.AnalogAssign.Dialog.Delete.Hint"));
			iconURL.add(Images.EDIT_DELETE.getURL());
			listener.add(new EditButtonListener(1,rowIndex));

			if (rowIndex>0) {
				title.add("");
				tooltip.add(Language.tr("Surface.AnalogAssign.Dialog.Up.Hint"));
				iconURL.add(Images.ARROW_UP.getURL());
				listener.add(new EditButtonListener(2,rowIndex));
			}

			if (rowIndex<changeID.size()-1) {
				title.add("");
				tooltip.add(Language.tr("Surface.AnalogAssign.Dialog.Down.Hint"));
				iconURL.add(Images.ARROW_DOWN.getURL());
				listener.add(new EditButtonListener(3,rowIndex));
			}
			return makeButtonPanel(title.toArray(new String[0]),tooltip.toArray(new String[0]),iconURL.toArray(new URL[0]),listener.toArray(new ActionListener[0]));
		default:
			return null;
		}
	}

	@Override
	public final boolean isCellEditable(int rowIndex, int columnIndex) {
		return !readOnly && (columnIndex==3);
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0: return Language.tr("Surface.AnalogAssign.Dialog.Columns.IDs");
		case 1: return Language.tr("Surface.AnalogAssign.Dialog.Columns.Modes");
		case 2: return Language.tr("Surface.AnalogAssign.Dialog.Columns.Expressions");
		case 3: return Language.tr("Surface.AnalogAssign.Dialog.Columns.Edit");
		default: return super.getColumnName(column);
		}
	}

	/**
	 * Schreibt die Zuweisungen aus der Tabelle in das im Konstruktor
	 * übergebene {@link ModelElementAnalogAssign}-Element zurück.
	 * @see #ModelElementAnalogAssignTableModel(JTableExt, ModelElementAnalogAssign, boolean, Runnable)
	 */
	public void storeData() {
		element.getChangeID().clear();
		element.getChangeID().addAll(changeID);
		element.getChangeMode().clear();
		element.getChangeMode().addAll(changeMode);
		element.getChangeExpression().clear();
		element.getChangeExpression().addAll(changeExpression);
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

			Integer id;
			ModelElementAnalogAssign.ChangeMode mode;
			String expr;

			switch (col) {
			case 0:
				final ModelElementAnalogAssignTableModelDialog dialog;
				if (row<0) {
					/* Neu */
					dialog=new ModelElementAnalogAssignTableModelDialog(table,-1,ModelElementAnalogAssign.ChangeMode.CHANGE_MODE_VALUE,"",element.getSurface(),element.getModel(),help);
				} else {
					/* Bearbeiten */
					dialog=new ModelElementAnalogAssignTableModelDialog(table,changeID.get(row),changeMode.get(row),changeExpression.get(row),element.getSurface(),element.getModel(),help);
				}
				dialog.setVisible(true);
				if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
					if (row>=0) {
						changeID.set(row,dialog.getID());
						changeMode.set(row,dialog.getMode());
						changeExpression.set(row,dialog.getExpression());
					} else {
						changeID.add(dialog.getID());
						changeMode.add(dialog.getMode());
						changeExpression.add(dialog.getExpression());
					}
					updateTable();
				}
				break;
			case 1: /* Löschen */
				if (row<0) return;
				if (!MsgBox.confirm(table,Language.tr("Surface.AnalogAssign.Dialog.Delete.ConfirmTitle"),Language.tr("Surface.AnalogAssign.Dialog.Delete.ConfirmInfo"),Language.tr("Surface.AnalogAssign.Dialog.Delete.ConfirmInfoYes"),Language.tr("Surface.AnalogAssign.Dialog.Delete.ConfirmInfoNo"))) return;
				changeID.remove(row);
				changeMode.remove(row);
				changeExpression.remove(row);
				updateTable();
				break;
			case 2: /* Nach oben */
				id=changeID.get(row); changeID.set(row,changeID.get(row-1)); changeID.set(row-1,id);
				mode=changeMode.get(row); changeMode.set(row,changeMode.get(row-1)); changeMode.set(row-1,mode);
				expr=changeExpression.get(row); changeExpression.set(row,changeExpression.get(row-1)); changeExpression.set(row-1,expr);
				updateTable();
				break;
			case 3: /* Nach unten */
				id=changeID.get(row); changeID.set(row,changeID.get(row+1)); changeID.set(row+1,id);
				mode=changeMode.get(row); changeMode.set(row,changeMode.get(row+1)); changeMode.set(row+1,mode);
				expr=changeExpression.get(row); changeExpression.set(row,changeExpression.get(row+1)); changeExpression.set(row+1,expr);
				updateTable();
				break;
			}
		}
	}
}
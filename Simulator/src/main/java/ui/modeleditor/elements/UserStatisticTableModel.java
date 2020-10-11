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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.TableCellEditor;

import language.Language;
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import tools.JTableExt;
import tools.JTableExtAbstractTableModel;
import ui.images.Images;
import ui.modeleditor.ModelSurface;

/**
 * Diese Tabelle erlaubt die Konfiguration der in einem
 * {@link ModelElementUserStatistic}-Element zu erfassenden
 * Kenngrößen.
 * @author Alexander Herzog
 * @see ModelElementUserStatistic
 * @see ModelElementUserStatisticDialog
 */
public class UserStatisticTableModel extends JTableExtAbstractTableModel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -5314140428056671403L;

	private final Runnable help;
	private final boolean readOnly;
	private final List<String> keys;
	private final List<Boolean> isTime;
	private final List<String> expressions;
	private final EditModel model;
	private final ModelSurface surface;
	private final JTableExt table;

	/**
	 * Konstruktor der Klasse
	 * @param table	Zugehörige Tabelle (um das Update der Tabelle veranlassen zu können, wenn sich die Daten verändert haben)
	 * @param help	Hilfe-Callback
	 * @param keys	Bezeichner für die Kenngrößen
	 * @param isTime	Handelt es sich jeweils im Zeitangaben (<code>true</code>) oder Zahlen (<code>false</code>)
	 * @param expressions	Zu erfassende Kenngrößen-Ausdrücke
	 * @param model	Gesamtes Modell (für den Expression-Builder)
	 * @param surface	Haupt-Zeichenfläche (für den Expression-Builder)
	 * @param readOnly	Nur-Lese-Status
	 */
	public UserStatisticTableModel(final JTableExt table, final Runnable help, final List<String> keys, final List<Boolean> isTime, final List<String> expressions, final EditModel model, final ModelSurface surface, final boolean readOnly) {
		super();
		this.help=help;
		this.readOnly=readOnly;
		this.table=table;
		this.keys=new ArrayList<>(); this.keys.addAll(keys);
		this.isTime=new ArrayList<>(); this.isTime.addAll(isTime);
		this.expressions=new ArrayList<>(); this.expressions.addAll(expressions);
		this.model=model;
		this.surface=surface;
		updateTable();
	}

	/**
	 * Liefert die neue Liste mit den Bezeichnern für die Kenngrößen.
	 * @return	Neue Liste mit den Bezeichnern für die Kenngrößen
	 */
	public List<String> getKeys() {
		return keys;
	}

	/**
	 * Liefert die Informationen ob es sich bei den Kenngrößen um Zeitangaben oder um Zahlen handelt.
	 * @return	Neue Liste mit den Informationen ob es sich bei den Kenngrößen um Zeitangaben (<code>true</code>) oder um Zahlen (<code>false</code>) handelt
	 */
	public List<Boolean> getIsTime() {
		return isTime;
	}

	/**
	 * Liefert die neue Liste mit den Rechenausdrücken zur Erhebung der Kenngrößen.
	 * @return	Neue Liste mit den Rechenausdrücken zur Erhebung der Kenngrößen
	 */
	public List<String> getExpressions() {
		return expressions;
	}

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
		return 2;
	}

	@Override
	public Object getValueAt(final int rowIndex, final int columnIndex) {
		if (rowIndex==keys.size()) {
			switch (columnIndex) {
			case 0: return "";
			case 1:	return makeButtonPanel(new String[]{Language.tr("Surface.UserStatistic.Table.Add")},new URL[]{Images.EDIT_ADD.getURL()},new ActionListener[]{new TableButtonListener(ActionIndex.ACTION_ADD)});
			}
		}

		switch (columnIndex) {
		case 0:
			return makePanel(keys.get(rowIndex),Images.MODELEDITOR_ELEMENT_USER_STATISTICS.getURL());
		case 1:
			final List<URL> icons=new ArrayList<>();
			final List<String> hints=new ArrayList<>();
			final List<ActionListener> listeners=new ArrayList<>();
			icons.add(Images.GENERAL_SETUP.getURL());
			hints.add(Language.tr("Surface.UserStatistic.Table.Edit"));
			listeners.add(new TableButtonListener(rowIndex,ActionIndex.ACTION_EDIT));
			icons.add(Images.EDIT_DELETE.getURL());
			hints.add(Language.tr("Surface.UserStatistic.Table.Delete"));
			listeners.add(new TableButtonListener(rowIndex,ActionIndex.ACTION_DELETE));
			if (rowIndex>0) {
				icons.add(Images.ARROW_UP.getURL());
				hints.add(Language.tr("Surface.UserStatistic.Table.MoveUp"));
				listeners.add(new TableButtonListener(rowIndex,ActionIndex.ACTION_UP));
			}
			if (rowIndex<keys.size()-1) {
				icons.add(Images.ARROW_DOWN.getURL());
				hints.add(Language.tr("Surface.UserStatistic.Table.MoveDown"));
				listeners.add(new TableButtonListener(rowIndex,ActionIndex.ACTION_DOWN));
			}
			return makeEditPanelSmallBorder(expressions.get(rowIndex),icons.toArray(new URL[0]),hints.toArray(new String[0]),listeners.toArray(new ActionListener[0]));
		}
		return null;
	}

	@Override
	public final boolean isCellEditable(int rowIndex, int columnIndex) {
		return !readOnly;
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0: return Language.tr("Surface.UserStatistic.Table.HeaderKey");
		case 1: return Language.tr("Surface.UserStatistic.Table.HeaderExpression");
		default: return super.getColumnName(column);
		}
	}

	private enum ActionIndex {ACTION_ADD, ACTION_UP, ACTION_DOWN, ACTION_DELETE, ACTION_EDIT}

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
			UserStatisticTableModelDialog dialog;
			String s;
			boolean b;
			switch (actionIndex) {
			case ACTION_ADD:
				dialog=new UserStatisticTableModelDialog(table,help,"",true,"",model,surface);
				if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
					keys.add(dialog.getKey());
					isTime.add(dialog.getIsTime());
					expressions.add(dialog.getExpression());
					updateTable();
				}
				break;
			case ACTION_UP:
				if (row>0) {
					s=keys.get(row-1);
					keys.set(row-1,keys.get(row));
					keys.set(row,s);
					b=isTime.get(row-1);
					isTime.set(row-1,isTime.get(row));
					isTime.set(row,b);
					s=expressions.get(row-1);
					expressions.set(row-1,expressions.get(row));
					expressions.set(row,s);
				}
				updateTable();
				break;
			case ACTION_DOWN:
				if (row<keys.size()-1) {
					s=keys.get(row+1);
					keys.set(row+1,keys.get(row));
					keys.set(row,s);
					b=isTime.get(row+1);
					isTime.set(row+1,isTime.get(row));
					isTime.set(row,b);
					s=expressions.get(row+1);
					expressions.set(row+1,expressions.get(row));
					expressions.set(row,s);
				}
				updateTable();
				break;
			case ACTION_DELETE:
				keys.remove(row);
				isTime.remove(row);
				expressions.remove(row);
				updateTable();
				break;
			case ACTION_EDIT:
				dialog=new UserStatisticTableModelDialog(table,help,keys.get(row),isTime.get(row),expressions.get(row),model,surface);
				if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
					keys.set(row,dialog.getKey());
					isTime.set(row,dialog.getIsTime());
					expressions.set(row,dialog.getExpression());
					updateTable();
				}
				break;
			}
		}
	}
}

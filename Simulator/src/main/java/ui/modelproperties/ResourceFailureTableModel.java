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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.TableCellEditor;

import language.Language;
import mathtools.NumberTools;
import mathtools.TimeTools;
import mathtools.distribution.tools.DistributionTools;
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.JTableExt;
import tools.JTableExtAbstractTableModel;
import ui.expressionbuilder.ExpressionBuilder;
import ui.images.Images;
import ui.modeleditor.ModelResource;
import ui.modeleditor.ModelResourceFailure;
import ui.modeleditor.ModelSurface;

/**
 * Tabellenmodell zur Anzeige und Bearbeitung von Ausfall-Ereignissen
 * @author Alexander Herzog
 * @see ModelResourceFailure
 * @see ResourceTableModelDialog
 */
public class ResourceFailureTableModel extends JTableExtAbstractTableModel {
	private static final long serialVersionUID = 3015995665203750590L;

	private final EditModel model;
	private final ModelSurface surface;
	private final JTableExt table;
	private final boolean readOnly;
	private final Runnable help;

	private final List<ModelResourceFailure> list;

	/**
	 * Konstruktor der Klasse
	 * @param resource	Ressource, deren Ausfälle dargestellt werden sollen (kann <code>null</code> sein)
	 * @param model	Editor-Modell (für {@link ExpressionBuilder})
	 * @param surface	Zeichenoberfläche (für {@link ExpressionBuilder})
	 * @param table	Zu diesem Tabellenmodell gehörende Tabelle
	 * @param readOnly	Nur-Lese-Status
	 * @param help	Hilfe-Callback
	 */
	public ResourceFailureTableModel(final ModelResource resource, final EditModel model, final ModelSurface surface, final JTableExt table, final boolean readOnly, final Runnable help) {
		super();

		this.model=model;
		this.surface=surface;
		this.table=table;
		this.readOnly=readOnly;
		this.help=help;

		list=new ArrayList<>();
		if (resource!=null) for (ModelResourceFailure failure: resource.getFailures()) list.add(failure.clone());

		updateTable();
	}

	/**
	 * Liefert die Liste der Ausfälle (zum Speichern beim Schließen des Dialoges)
	 * @return	Liste der Ausfälle
	 */
	public List<ModelResourceFailure> getFailures() {
		return list;
	}

	private void updateTable() {
		fireTableDataChanged();
		TableCellEditor cellEditor=table.getCellEditor();
		if (cellEditor!=null) cellEditor.stopCellEditing();
	}

	private String getFailureInfo(final ModelResourceFailure failure) {
		final StringBuilder sb=new StringBuilder();
		sb.append("<html><body>");

		switch (failure.getFailureMode()) {
		case FAILURE_BY_NUMBER:
			sb.append(Language.tr("Resources.Group.EditName.Dialog.Failure.Number")+" <b>"+NumberTools.formatLong(failure.getFailureNumber())+"</b>");
			break;
		case FAILURE_BY_AVAILABLE_TIME:
			sb.append(Language.tr("Resources.Group.EditName.Dialog.Failure.Available")+" <b>"+TimeTools.formatExactTime(failure.getFailureTime())+"</b>");
			break;
		case FAILURE_BY_WORKING_TIME:
			sb.append(Language.tr("Resources.Group.EditName.Dialog.Failure.Working")+" <b>"+TimeTools.formatExactTime(failure.getFailureTime())+"</b>");
			break;
		case FAILURE_BY_DISTRIBUTION:
			sb.append(Language.tr("Resources.Group.EditName.Dialog.Failure.Distribution")+": <b>");
			sb.append(DistributionTools.getDistributionName(failure.getFailureDistribution()));
			sb.append("</b>");
			break;
		case FAILURE_BY_EXPRESSION:
			sb.append(Language.tr("Resources.Group.EditName.Dialog.Failure.Expression")+": <b>");
			sb.append(failure.getFailureExpression());
			sb.append("</b>");
			break;
		}

		sb.append("<br>");

		final String downTimeExpression=failure.getDownTimeExpression();
		sb.append(Language.tr("Resources.Group.EditName.Dialog.DownTime")+" ");
		if (downTimeExpression==null) {
			/* Ausfallzeit: Verteilung */
			sb.append(Language.tr("Resources.Group.EditName.Dialog.DownTime.Distribution")+": <b>");
			sb.append(DistributionTools.getDistributionName(failure.getDownTimeDistribution()));
			sb.append("</b>");
		} else {
			/* Ausfallzeit: Ausdruck */
			sb.append(Language.tr("Resources.Group.EditName.Dialog.DownTime.Expression")+": <b>"+downTimeExpression+"</b>");
		}

		sb.append("</body></html>");
		return sb.toString();
	}

	@Override
	public int getRowCount() {
		return list.size()+1;
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0: return Language.tr("Resources.Failure.RowTitle.Information");
		case 1: return Language.tr("Resources.Failure.RowTitle.Buttons");
		default: return super.getColumnName(column);
		}
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex==list.size()) {
			switch (columnIndex) {
			case 0:	return makeButtonPanel(new String[]{Language.tr("Resources.Failure.Add")},new URL[]{Images.MODELPROPERTIES_OPERATORS_ADD.getURL()},new ActionListener[]{new AddButtonListener()});
			case 1: return "";
			}
		}

		switch (columnIndex) {
		case 0: return makePanel(getFailureInfo(list.get(rowIndex)),Images.MODELPROPERTIES_OPERATORS_FAILURES.getURL());
		case 1:
			final List<String> title=new ArrayList<>();
			final List<URL> iconURL=new ArrayList<>();
			final List<ActionListener> listener=new ArrayList<>();
			title.add(Language.tr("Resources.Failure.Edit"));
			iconURL.add(Images.GENERAL_SETUP.getURL());
			listener.add(new EditButtonListener(0,rowIndex));
			title.add(Language.tr("Resources.Failure.Delete"));
			iconURL.add(Images.EDIT_DELETE.getURL());
			listener.add(new DeleteButtonListener(rowIndex));
			if (rowIndex>0) {
				title.add(Language.tr("Resources.Failure.MoveUp"));
				iconURL.add(Images.ARROW_UP.getURL());
				listener.add(new EditButtonListener(1,rowIndex));
			}
			if (rowIndex<list.size()-1) {
				title.add(Language.tr("Resources.Failure.MoveDown"));
				iconURL.add(Images.ARROW_DOWN.getURL());
				listener.add(new EditButtonListener(2,rowIndex));
			}
			return makeButtonPanel(null,title.toArray(new String[0]),iconURL.toArray(new URL[0]),listener.toArray(new ActionListener[0]));
		default: return null;
		}
	}

	@Override
	public final boolean isCellEditable(int rowIndex, int columnIndex) {
		return !readOnly;
	}

	private class AddButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (readOnly) return;

			final ResourceFailureDialog dialog=new ResourceFailureDialog(table,null,model,surface,help);
			if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
				list.add(dialog.getFailure());
				updateTable();
			}
		}
	}

	private class EditButtonListener implements ActionListener {
		private final int mode;
		private final int row;

		public EditButtonListener(final int mode, final int row) {
			this.mode=mode;
			this.row=row;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (readOnly) return;

			switch (mode) {
			case 0:
				final ResourceFailureDialog dialog=new ResourceFailureDialog(table,list.get(row),model,surface,help);
				if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
					list.set(row,dialog.getFailure());
					updateTable();
				}
				break;
			case 1:
				if (row>0) {
					ModelResourceFailure failure=list.get(row);
					list.set(row,list.get(row-1));
					list.set(row-1,failure);
					updateTable();
				}
				break;
			case 2:
				if (row<list.size()-1) {
					ModelResourceFailure failure=list.get(row);
					list.set(row,list.get(row+1));
					list.set(row+1,failure);
					updateTable();
				}
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

			if (!MsgBox.confirm(table,Language.tr("Resources.Group.EditName.Dialog.Failure.Delete"),Language.tr("Resources.Group.EditName.Dialog.Failure.Delete.Info"),Language.tr("Resources.Group.EditName.Dialog.Failure.Delete.YesInfo"),Language.tr("Resources.Group.EditName.Dialog.Failure.Delete.NoInfo"))) return;

			list.remove(row);
			updateTable();
		}
	}
}

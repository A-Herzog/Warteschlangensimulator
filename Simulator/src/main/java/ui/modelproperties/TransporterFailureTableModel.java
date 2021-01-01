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
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelTransporter;
import ui.modeleditor.ModelTransporterFailure;

/**
 * Tabellenmodell zur Anzeige und Bearbeitung von Ausfall-Ereignissen für Transporter
 * @author Alexander Herzog
 * @see ModelTransporterFailure
 * @see TransporterTableModelDialog
 */
public class TransporterFailureTableModel extends JTableExtAbstractTableModel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -2166512071599966482L;

	/** Editor-Modell (für {@link ExpressionBuilder}) */
	private final EditModel model;
	/** Zeichenoberfläche (für {@link ExpressionBuilder}) */
	private final ModelSurface surface;
	/** Zu diesem Tabellenmodell gehörende Tabelle */
	private final JTableExt table;
	/** Nur-Lese-Status */
	private final boolean readOnly;
	/** Hilfe-Callback */
	private final Runnable help;

	/**
	 * Liste der Ausfall-Ereignisse
	 */
	private final List<ModelTransporterFailure> list;

	/**
	 * Konstruktor der Klasse
	 * @param transporter	Transporter, dessen Ausfälle dargestellt werden sollen (kann <code>null</code> sein)
	 * @param model	Editor-Modell (für {@link ExpressionBuilder})
	 * @param surface	Zeichenoberfläche (für {@link ExpressionBuilder})
	 * @param table	Zu diesem Tabellenmodell gehörende Tabelle
	 * @param readOnly	Nur-Lese-Status
	 * @param help	Hilfe-Callback
	 */
	public TransporterFailureTableModel(final ModelTransporter transporter, final EditModel model, final ModelSurface surface, final JTableExt table, final boolean readOnly, final Runnable help) {
		super();

		this.model=model;
		this.surface=surface;
		this.table=table;
		this.readOnly=readOnly;
		this.help=help;

		list=new ArrayList<>();
		if (transporter!=null) for (ModelTransporterFailure failure: transporter.getFailures()) list.add(failure.clone());

		updateTable();
	}

	/**
	 * Liefert die Liste der Ausfälle (zum Speichern beim Schließen des Dialoges)
	 * @return	Liste der Ausfälle
	 */
	public List<ModelTransporterFailure> getFailures() {
		return list;
	}

	/**
	 * Aktualisiert die Tabellendarstellung
	 */
	private void updateTable() {
		fireTableDataChanged();
		TableCellEditor cellEditor=table.getCellEditor();
		if (cellEditor!=null) cellEditor.stopCellEditing();
	}

	/**
	 * Liefert eine Text-Beschreibung zu einem Ausfallereignis
	 * @param failure	Ausfallereignis
	 * @return	Beschreibung
	 * @see #getValueAt(int, int)
	 */
	private String getFailureInfo(final ModelTransporterFailure failure) {
		final StringBuilder sb=new StringBuilder();
		sb.append("<html><body>");

		switch (failure.getFailureMode()) {
		case FAILURE_BY_NUMBER:
			sb.append(Language.tr("Transporter.Group.Edit.Dialog.Failure.Number")+" <b>"+NumberTools.formatLong(failure.getFailureNumber())+"</b>");
			break;
		case FAILURE_BY_DISTANCE:
			sb.append(Language.tr("Transporter.Group.Edit.Dialog.Failure.Distance")+" <b>"+NumberTools.formatNumber(failure.getFailureTimeOrDistance())+"</b>");
			break;
		case FAILURE_BY_WORKING_TIME:
			sb.append(Language.tr("Transporter.Group.Edit.Dialog.Failure.Working")+" <b>"+TimeTools.formatExactTime(failure.getFailureTimeOrDistance())+"</b>");
			break;
		case FAILURE_BY_DISTRIBUTION:
			sb.append(Language.tr("Transporter.Group.Edit.Dialog.Failure.Distribution")+": <b>");
			sb.append(DistributionTools.getDistributionName(failure.getFailureDistribution()));
			sb.append("</b>");
			break;
		case FAILURE_BY_EXPRESSION:
			sb.append(Language.tr("Transporter.Group.Edit.Dialog.Failure.Expression")+": <b>");
			sb.append(failure.getFailureExpression());
			sb.append("</b>");
			break;
		}

		sb.append("<br>");

		final String downTimeExpression=failure.getDownTimeExpression();
		sb.append(Language.tr("Transporter.Group.Edit.Dialog.DownTime")+" ");
		if (downTimeExpression==null) {
			/* Ausfallzeit: Verteilung */
			sb.append(Language.tr("Transporter.Group.Edit.Dialog.DownTime.Distribution")+": <b>");
			sb.append(DistributionTools.getDistributionName(failure.getDownTimeDistribution()));
			sb.append("</b>");
		} else {
			/* Ausfallzeit: Ausdruck */
			sb.append(Language.tr("Transporter.Group.Edit.Dialog.DownTime.Expression")+": <b>"+downTimeExpression+"</b>");
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
		case 0: return Language.tr("Transporter.Failure.RowTitle.Information");
		case 1: return Language.tr("Transporter.Failure.RowTitle.Buttons");
		default: return super.getColumnName(column);
		}
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex==list.size()) {
			switch (columnIndex) {
			case 0:	return makeButtonPanel(new String[]{Language.tr("Transporter.Failure.Add")},new Icon[]{Images.MODELPROPERTIES_TRANSPORTERS_ADD.getIcon()},new ActionListener[]{new AddButtonListener()});
			case 1: return "";
			}
		}

		switch (columnIndex) {
		case 0: return makePanelIcon(getFailureInfo(list.get(rowIndex)),Images.MODELPROPERTIES_TRANSPORTERS_FAILURE.getIcon());
		case 1:
			final List<String> title=new ArrayList<>();
			final List<Icon> icons=new ArrayList<>();
			final List<ActionListener> listener=new ArrayList<>();
			title.add(Language.tr("Transporter.Failure.Edit"));
			icons.add(Images.GENERAL_SETUP.getIcon());
			listener.add(new EditButtonListener(0,rowIndex));
			title.add(Language.tr("Transporter.Failure.Delete"));
			icons.add(Images.EDIT_DELETE.getIcon());
			listener.add(new DeleteButtonListener(rowIndex));
			if (rowIndex>0) {
				title.add(Language.tr("Transporter.Failure.MoveUp"));
				icons.add(Images.ARROW_UP.getIcon());
				listener.add(new EditButtonListener(1,rowIndex));
			}
			if (rowIndex<list.size()-1) {
				title.add(Language.tr("Transporter.Failure.MoveDown"));
				icons.add(Images.ARROW_DOWN.getIcon());
				listener.add(new EditButtonListener(2,rowIndex));
			}
			return makeButtonPanel(null,title.toArray(new String[0]),icons.toArray(new Icon[0]),listener.toArray(new ActionListener[0]));
		default: return null;
		}
	}

	@Override
	public final boolean isCellEditable(int rowIndex, int columnIndex) {
		return !readOnly;
	}

	/**
	 * Reagiert auf Klicks auf die Hinzufügen-Schaltfläche
	 */
	private class AddButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (readOnly) return;

			final TransporterFailureDialog dialog=new TransporterFailureDialog(table,null,model,surface,help);
			if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
				list.add(dialog.getFailure());
				updateTable();
			}
		}
	}

	/**
	 * Reagiert auf Klicks auf die Bearbeiten und Verschieben-Schaltflächen
	 */
	private class EditButtonListener implements ActionListener {
		/** Auszuführender Befehl (0: Bearbeiten, 1: In der Liste nach oben schieben, 2: In der Liste nach unten schieben) */
		private final int mode;
		/** Zeilennummer */
		private final int row;

		/**
		 * Konstruktor der Klasse
		 * @param mode	Auszuführender Befehl (0: Bearbeiten, 1: In der Liste nach oben schieben, 2: In der Liste nach unten schieben)
		 * @param row	Zeilennummer
		 */
		public EditButtonListener(final int mode, final int row) {
			this.mode=mode;
			this.row=row;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (readOnly) return;

			switch (mode) {
			case 0:
				final TransporterFailureDialog dialog=new TransporterFailureDialog(table,list.get(row),model,surface,help);
				if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
					list.set(row,dialog.getFailure());
					updateTable();
				}
				break;
			case 1:
				if (row>0) {
					ModelTransporterFailure failure=list.get(row);
					list.set(row,list.get(row-1));
					list.set(row-1,failure);
					updateTable();
				}
				break;
			case 2:
				if (row<list.size()-1) {
					ModelTransporterFailure failure=list.get(row);
					list.set(row,list.get(row+1));
					list.set(row+1,failure);
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

			if (!MsgBox.confirm(table,Language.tr("Transporter.Group.Edit.Dialog.Failure.Delete"),Language.tr("Transporter.Group.Edit.Dialog.Failure.Delete.Info"),Language.tr("Transporter.Group.Edit.Dialog.Failure.Delete.YesInfo"),Language.tr("Transporter.Group.Edit.Dialog.Failure.Delete.NoInfo"))) return;

			list.remove(row);
			updateTable();
		}
	}
}

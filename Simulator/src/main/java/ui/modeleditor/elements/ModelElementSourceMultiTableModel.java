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
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.table.TableCellEditor;

import language.Language;
import mathtools.NumberTools;
import mathtools.TimeTools;
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.JTableExt;
import tools.JTableExtAbstractTableModel;
import ui.images.Images;
import ui.modeleditor.AnimationImageSource;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurfaceAnimatorBase;
import ui.modeleditor.coreelements.ModelElement;

/**
 * Stellt ein Tabellenmodell zur Anzeige und Bearbeitung der Kunden-Teil-Quellen zur Verfügung.
 * @author Alexander Herzog
 * @see ModelElementSourceMultiDialog
 */
public class ModelElementSourceMultiTableModel extends JTableExtAbstractTableModel {
	private static final long serialVersionUID = -8059012715680468181L;

	private final boolean hasOwnArrivals;
	private final JTableExt table;
	private final Runnable help;
	private final boolean readOnly;
	private final EditModel model;
	private final ModelSurface surface;
	private final ModelElement element;
	private ModelClientData clientData;
	private final AnimationImageSource imageSource;

	private final List<ModelElementSourceRecord> records;

	/**
	 * Konstruktor der Klasse <code>ModelElementSourceMultiTableModel</code>
	 * @param table	Tabelle in der das Datenmodell zum Einsatz kommen soll
	 * @param records	Datensätze, die bearbeitet werden sollen
	 * @param hasOwnArrivals	Gibt an, ob die Quelle selbst Kunden erzeugen können soll oder ob sie jeweils von außen angestoßen wird
	 * @param element	Element, dessen Zuweisungen bearbeitet werden sollen (für den ExpressionBuilder und um die Variablenliste zusammenzustellen)
	 * @param model	Element vom Typ <code>EditModel</code> (wird benötigt, um die Liste der globalen Variablen zu laden)
	 * @param surface	Zeichenoberfläche
	 * @param readOnly	Gibt an, ob die Daten bearbeitet werden dürfen (<code>false</code>) oder nicht (<code>true</code>).
	 * @param help	Hilfe-Callback welches aufgerufen wird, wenn in einem der untergeordneten Dialoge auf die "Hilfe"-Schaltfläche geklickt wird.
	 */
	public ModelElementSourceMultiTableModel(final JTableExt table, final List<ModelElementSourceRecord> records, final boolean hasOwnArrivals, final ModelElement element, final EditModel model, final ModelSurface surface, final boolean readOnly, final Runnable help) {
		super();
		this.help=help;
		this.table=table;
		this.readOnly=readOnly;
		this.model=model;
		this.surface=surface;
		this.element=element;
		this.hasOwnArrivals=hasOwnArrivals;

		imageSource=new AnimationImageSource();

		this.records=new ArrayList<>();
		for (ModelElementSourceRecord record: records) this.records.add(record.clone());

		updateTable();
	}

	/**
	 * Übergibt die Kundendaten an das Tabellen-Datenmodell
	 * @param clientData	Kundendaten
	 */
	public void setClientData(final ModelClientData clientData) {
		this.clientData=clientData;
	}

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
		return 2;
	}

	private String getInfoText(final ModelElementSourceRecord record) {
		final StringBuilder sb=new StringBuilder();

		if (!hasOwnArrivals && record.hasName()) {
			sb.append(Language.tr("Surface.MultiSourceTable.Info.ClientTypeName")+": "+record.getName());
		}

		if (hasOwnArrivals) switch (record.getNextMode()) {
		case NEXT_DISTRIBUTION:
			sb.append(Language.tr("Surface.MultiSourceTable.Info.NextDistribution"));
			break;
		case NEXT_EXPRESSION:
			sb.append(Language.tr("Surface.MultiSourceTable.Info.NextExpression"));
			sb.append(" (");
			sb.append(record.getInterarrivalTimeExpression());
			sb.append(")");
			break;
		case NEXT_SCHEDULE:
			sb.append(Language.tr("Surface.MultiSourceTable.Info.NextSchedule"));
			sb.append(" (");
			sb.append(record.getInterarrivalTimeSchedule());
			sb.append(")");
			break;
		case NEXT_CONDITION:
			sb.append(Language.tr("Surface.MultiSourceTable.Info.NextCondition"));
			sb.append(" (");
			sb.append(record.getArrivalCondition());
			sb.append(", "+Language.tr("Surface.MultiSourceTable.Info.NextCondition.MinDistance"));
			sb.append(": ");
			sb.append(TimeTools.formatExactTime(record.getArrivalConditionMinDistance()));
			sb.append(")");
			break;
		case NEXT_THRESHOLD:
			sb.append(Language.tr("Surface.MultiSourceTable.Info.NextThreshold"));
			sb.append(": ");
			sb.append(record.getThresholdExpression());
			sb.append(" ");
			if (record.isThresholdDirectionUp()) Language.tr("Surface.MultiSourceTable.Info.NextThreshold.Up"); else Language.tr("Surface.MultiSourceTable.Info.NextThreshold.Down");
			sb.append(" ");
			sb.append(NumberTools.formatNumber(record.getThresholdValue()));
			break;
		case NEXT_SIGNAL:
			sb.append(Language.tr("Surface.MultiSourceTable.Info.NextSignal"));
			sb.append(" (");
			sb.append(String.join(", ",record.getArrivalSignalNames().toArray(new String[0])));
			sb.append(")");
			break;
		}

		if (record.getBatchSize()==null || !record.getBatchSize().trim().equals("1")) {
			sb.append(", ");
			sb.append(Language.tr("Surface.MultiSourceTable.Info.BatchMode"));
			if (record.getBatchSize()!=null) {
				sb.append(" (");
				sb.append(record.getBatchSize());
				sb.append(")");
			}
		}

		if (hasOwnArrivals && record.getMaxArrivalCount()>=0) {
			sb.append(", ");
			sb.append(Language.tr("Surface.MultiSourceTable.Info.ArrivalCount"));
			sb.append(": ");
			sb.append(record.getMaxArrivalCount());
			sb.append(" ");
		}

		if (hasOwnArrivals && record.getMaxArrivalClientCount()>=0) {
			sb.append(", ");
			sb.append(Language.tr("Surface.MultiSourceTable.Info.ArrivalClientCount"));
			sb.append(": ");
			sb.append(record.getMaxArrivalClientCount());
			sb.append(" ");
		}

		if (hasOwnArrivals && record.getArrivalStart()>0) {
			sb.append(", ");
			sb.append(Language.tr("Surface.MultiSourceTable.Info.ArrivalStart"));
			sb.append(": ");
			sb.append(NumberTools.formatNumber(record.getArrivalStart()));
			sb.append(" ");
		}

		return sb.toString();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex==records.size()) {
			if (columnIndex>0) return "";
			return makeButtonPanel(new String[]{Language.tr("Surface.MultiSourceTable.Add")},new URL[]{Images.EDIT_ADD.getURL()},new ActionListener[]{new EditButtonListener(0,-1)});
		}

		final ModelElementSourceRecord record=records.get(rowIndex);

		switch (columnIndex) {
		case 0:
			String icon=clientData.getIcon(record.getName());
			if (icon==null || icon.trim().isEmpty()) icon=ModelSurfaceAnimatorBase.DEFAULT_CLIENT_ICON_NAME;
			final BufferedImage image=imageSource.get(icon,model.animationImages,16);
			return makeEditPanelSmallBorderIcon(
					new ImageIcon(image),
					record.getName(),
					new URL[]{Images.GENERAL_SETUP.getURL(),Images.EDIT_DELETE.getURL()},
					new String[]{Language.tr("Surface.MultiSourceTable.Edit"),Language.tr("Surface.MultiSourceTable.Delete")},
					new ActionListener[]{new EditButtonListener(0,rowIndex),new EditButtonListener(1,rowIndex)}
					);
		case 1:
			return makePanel(getInfoText(record),null);
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
		case 0: return Language.tr("Surface.MultiSourceTable.ColumnClientType");
		case 1: return Language.tr("Surface.MultiSourceTable.ColumnData");
		default: return super.getColumnName(column);
		}
	}

	/**
	 * Schreibt die Daten aus der Tabelle in das Element zurück
	 * @param element	Station, in die die Kundenankunftsdatensätze zurück geschrieben werden sollen
	 */
	public void storeData(final ModelElementSourceMulti element) {
		element.getRecords().clear();
		for (ModelElementSourceRecord record: records) {
			if (!element.getSurface().getClientTypes().contains(record.saveName)) {
				ModelElementSourceRecordPanel.renameClients(record.saveName,record.getName(),clientData,surface);
			}
			element.addRecord(record);
		}
	}

	/**
	 * Schreibt die Daten aus der Tabelle in das Element zurück
	 * @param element	Station, in die die Kundenankunftsdatensätze zurück geschrieben werden sollen
	 */
	public void storeData(final ModelElementSplit element) {
		element.getRecords().clear();
		for (ModelElementSourceRecord record: records) {
			if (!element.getSurface().getClientTypes().contains(record.saveName)) {
				ModelElementSourceRecordPanel.renameClients(record.saveName,record.getName(),clientData,surface);
			}
			element.addRecord(record);
		}
	}

	private class EditButtonListener implements ActionListener {
		private final int nr;
		private final int row;

		public EditButtonListener(final int nr, final int row) {
			this.nr=nr;
			this.row=row;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (readOnly) return;

			if (nr==0) {
				/* Add / Edit */
				ModelElementSourceRecord record=(row<0)?new ModelElementSourceRecord(true,hasOwnArrivals):records.get(row);
				ModelElementSourceMultiTableModelDialog dialog=new ModelElementSourceMultiTableModelDialog(table,record,element,model,surface,clientData,help);
				dialog.setVisible(true);
				if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
					if (row<0) records.add(record);
					updateTable();
				}
			} else {
				/* Delete */
				final String name=records.get(row).getName();
				if (!MsgBox.confirm(table,Language.tr("Surface.MultiSourceTable.Delete.Confirmation.Title"),String.format(Language.tr("Surface.MultiSourceTable.Delete.Confirmation.Info"),name),Language.tr("Surface.MultiSourceTable.Delete.Confirmation.YesInfo"),Language.tr("Surface.MultiSourceTable.Delete.Confirmation.NoInfo"))) return;
				records.remove(row);
				updateTable();
			}
		}
	}
}

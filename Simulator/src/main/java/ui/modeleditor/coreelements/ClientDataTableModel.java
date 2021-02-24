/**
 * Copyright 2021 Alexander Herzog
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
package ui.modeleditor.coreelements;

import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.table.TableCellEditor;

import language.Language;
import mathtools.NumberTools;
import simulator.runmodel.RunDataClient;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.JTableExt;
import tools.JTableExtAbstractTableModel;
import ui.images.Images;

/**
 * Datenmodell für die Kundendatentabellen in {@link ModelElementAnimationEditClientDialog}
 * @author Alexander Herzog
 * @see ModelElementAnimationEditClientDialog
 */
public class ClientDataTableModel extends JTableExtAbstractTableModel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=8373790998995987844L;

	/**
	 * Zu dem Tabellenmodell gehörenden Tabellenobjekt
	 * (wird sowohl zum Ausrichten von Dialogen als auch um ein Update der Tabelle anzustoßen benötigt)
	 */
	private final JTableExt table;

	/**
	 * Zu bearbeitender Kundendatensatz
	 */
	private final RunDataClient client;

	/**
	 * Sollen die numerischen Datenfelder (<code>true</code>) oder die textbasierten Datenfelder (<code>false</code>) bearbeitet werden?
	 */
	private final boolean numbersMode;

	/**
	 * Daten für die Tabelle
	 */
	private final List<Record> records;

	/**
	 * Konstruktor der Klasse
	 * @param table	Zu dem Tabellenmodell gehörenden Tabellenobjekt (wird sowohl zum Ausrichten von Dialogen als auch um ein Update der Tabelle anzustoßen benötigt)
	 * @param client	Zu bearbeitender Kundendatensatz
	 * @param numbersMode	Sollen die numerischen Datenfelder (<code>true</code>) oder die textbasierten Datenfelder (<code>false</code>) bearbeitet werden?
	 */
	public ClientDataTableModel(final JTableExt table, final RunDataClient client, final boolean numbersMode) {
		this.table=table;
		this.client=client;
		this.numbersMode=numbersMode;

		records=new ArrayList<>();

		if (numbersMode) {
			final int max=client.getMaxUserDataIndex();
			for (int i=0;i<=max;i++) {
				final double value=client.getUserData(i);
				if (value!=0.0) records.add(new Record(""+i,NumberTools.formatNumberMax(value)));
			}
		} else {
			final Set<String> keys=client.getUserDataStringKeys();
			for (String key: keys) {
				final String value=client.getUserDataString(key);
				if (value!=null && !value.trim().isEmpty()) records.add(new Record(key,value));
			}
		}

		updateTable();
	}

	/**
	 * Weist das Datenmodell an, die Tabelle zu aktualisieren.
	 */
	public void updateTable() {
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

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0: return Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.FieldTable.ColumnKey");
		case 1: return Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.FieldTable.ColumnValue");
		default: return super.getColumnName(column);
		}
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex==records.size()) {
			switch (columnIndex) {
			case 0:	return makeButtonPanel(new String[]{Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.FieldTable.Add")},new Icon[]{Images.EDIT_ADD.getIcon()},new ActionListener[]{e->edit(-1)});
			case 1: return "";
			}
		}

		switch (columnIndex) {
		case 0:
			return makeEditPanelSmallBorder(
					records.get(rowIndex).key,
					new Icon[0],new String[0],new ActionListener[0]
					);
		case 1:
			return makeEditPanelSmallBorder(
					records.get(rowIndex).value,
					new Icon[]{Images.GENERAL_SETUP.getIcon(),Images.EDIT_DELETE.getIcon()},
					new String[]{Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.FieldTable.Edit"),Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.FieldTable.Delete")},
					new ActionListener[]{e->edit(rowIndex),e->delete(rowIndex)}
					);
		default:
			return null;
		}
	}

	@Override
	public final boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}

	/**
	 * Befehl: Datensatz bearbeiten oder neuen Datensatz anlegen
	 * @param index	Index des zu bearbeitenden Datensatzes (-1 für neuen Datensatz)
	 */
	private void edit(final int index) {
		final ClientDataTableModelDialog dialog=new ClientDataTableModelDialog(table,numbersMode,records,index);
		if (dialog.getClosedBy()!=BaseDialog.CLOSED_BY_OK) return;

		if (numbersMode) {
			records.sort((r1,r2)->{
				final Integer i1=NumberTools.getInteger(r1.key);
				final Integer i2=NumberTools.getInteger(r2.key);
				if (i1==null || i2==null) return 0;
				return i1.intValue()-i2.intValue();
			});
		} else {
			records.sort((r1,r2)->r1.key.compareTo(r2.key));
		}

		updateTable();
	}

	/**
	 * Befehl: Datensatz löschen
	 * @param index	Index des zu löschenden Datensatzes
	 */
	private void delete(final int index) {
		if (index<0) return;
		if (!MsgBox.confirm(table,Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.FieldTable.Delete"),String.format(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.FieldTable.Delete.Info"),records.get(index).key),Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.FieldTable.Delete.InfoYes"),Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.FieldTable.Delete.InfoNo"))) return;

		records.remove(index);
		updateTable();
	}

	/**
	 * Schreibt die veränderten Daten in das Kundenobjekt zurück.
	 */
	public void storeData() {
		if (numbersMode) {
			final Map<Integer,Double> map=new HashMap<>();
			int maxKey=-1;
			for (Record record: records) {
				final Integer key=NumberTools.getInteger(record.key);
				map.put(key,NumberTools.getDouble(record.value));
				final int i=key.intValue();
				if (i>maxKey) maxKey=i;
			}
			maxKey=Math.max(maxKey,client.getMaxUserDataIndex());
			for (int i=0;i<=maxKey;i++) {
				final Double value=map.get(i);
				client.setUserData(i,(value==null)?0.0:value);
			}
		} else {
			final Map<String,String> map=new HashMap<>();
			for (Record record: records) {
				final String key=record.key;
				map.put(key,record.value);
			}

			client.setUserDataStringsReplace(map);
		}
	}

	/**
	 * Datensatz für eine Zeile
	 * @see ClientDataTableModel#records
	 */
	public static class Record {
		/** Schlüssel (Spalte 1) */
		public String key;
		/** Wert (Spalte 2) */
		public String value;

		/**
		 * Konstruktor der Klasse
		 * @param key	Schlüssel
		 * @param value	Wert
		 */
		public Record(final String key, final String value) {
			this.key=key;
			this.value=value;
		}
	}
}

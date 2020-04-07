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

import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;

import language.Language;
import mathtools.NumberTools;
import systemtools.MsgBox;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelTransporter;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.elements.ModelElementSub;
import ui.modeleditor.elements.ModelElementTransportParking;
import ui.modeleditor.elements.ModelElementTransportTransporterSource;

/**
 * Tabellenmodelle zur Festlegung der Startstationen der Transporter
 * @author Alexander Herzog
 * @see TransporterTableModelDialog
 */
public class TransporterStationsTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 5779238132434380386L;

	private final Component owner;
	private final JTable table;
	private final ModelTransporter transporter;
	private final List<String> stations;
	private final List<String> stationsLong;
	private final String[] count;
	private final boolean readOnly;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element (zur Platzierung von Dialogen)
	 * @param table	Zu diesem Tabellenmodell gehörige Tabelle
	 * @param transporter	Transporter-Objekt aus dem die Anzahl-Werte ausgelesen werden sollen und in das sie später ggf. zurückgeschrieben werden sollen
	 * @param surface	Zeichenfläche (zur Ermittlung der Namen der Stationen)
	 * @param readOnly	Nur-Lese-Status
	 */
	public TransporterStationsTableModel(final Component owner, final JTable table, final ModelTransporter transporter, final ModelSurface surface, final boolean readOnly) {
		super();
		this.owner=owner;
		this.table=table;
		this.transporter=transporter;
		this.readOnly=readOnly;

		/* Liste der Stationen zusammenstellen */
		stations=new ArrayList<>();
		stationsLong=new ArrayList<>();
		for (ModelElement element: surface.getElements()) {
			if (element instanceof ModelElementSub) for (ModelElement element2: ((ModelElementSub)element).getSubSurface().getElements()) {
				addStation(element2,element);
			}
			addStation(element,null);
		}
		count=new String[this.stations.size()];
		Arrays.fill(count,"0");

		/* Daten aus Transporter-Objekt auslesen */
		for (Map.Entry<String,Integer> entry: transporter.getCount().entrySet()) {
			final int index=stations.indexOf(entry.getKey());
			if (index>=0) count[index]=""+entry.getValue().intValue();
		}
	}

	private void updateTable() {
		fireTableDataChanged();
		TableCellEditor cellEditor=table.getCellEditor();
		if (cellEditor!=null) cellEditor.stopCellEditing();
	}

	private void addStation(final ModelElement element, final ModelElement parent) {
		if ((element instanceof ModelElementTransportTransporterSource) || (element instanceof ModelElementTransportParking)) {
			final String name=element.getName();
			if (!name.isEmpty()) {
				stations.add(name);
				if (parent!=null) {
					String parentName=parent.getName().trim();
					if (parentName.isEmpty() && parent instanceof ModelElementBox) parentName=((ModelElementBox)parent).getTypeName();
					stationsLong.add(name+" (id="+element.getId()+") in "+parentName+" (id="+parent.getId()+")");
				} else {
					stationsLong.add(name+" (id="+element.getId()+")");
				}
			}
		}
	}

	@Override
	public int getRowCount() {
		return count.length+1;
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex==1 && !readOnly && rowIndex<count.length;
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0: return Language.tr("Transporters.Group.Edit.Dialog.Count.Station");
		case 1: return Language.tr("Transporters.Group.Edit.Dialog.Count.Count");
		default: return super.getColumnName(column);
		}
	}

	private String getSum() {
		int sum=0;
		for (int i=0;i<count.length;i++) {
			Integer I=NumberTools.getNotNegativeInteger(count[i]);
			if (I==null) return String.format(Language.tr("Transporters.Group.Edit.Dialog.Count.Count.Sum.Error"),i+1);
			sum+=I.intValue();
		}
		return ""+sum;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex==count.length) {
			switch (columnIndex) {
			case 0: return Language.tr("Transporters.Group.Edit.Dialog.Count.Count.Sum");
			case 1: return getSum();
			default: return null;
			}
		}

		switch (columnIndex) {
		case 0: return stationsLong.get(rowIndex);
		case 1: return count[rowIndex];
		default: return null;
		}
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (columnIndex!=1) return;
		if (!(aValue instanceof String)) return;
		count[rowIndex]=(String)aValue;
		SwingUtilities.invokeLater(()->updateTable());
	}

	/**
	 * Überprüft die Eingaben
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird im Fehlerfall eine Fehlermeldung ausgegeben
	 * @return	Gibt <code>true</code> zurück, wenn die Eingaben in Ordnung sind
	 */
	public boolean checkInput(final boolean showErrorMessage) {
		for (int i=0;i<count.length;i++) {
			final Integer I=NumberTools.getNotNegativeInteger(count[i]);
			if (I==null) {
				if (showErrorMessage) MsgBox.error(owner,Language.tr("Transporters.Group.Edit.Dialog.Count.ErrorTitle"),String.format(Language.tr("Transporters.Group.Edit.Dialog.Count.ErrorInfo"),stationsLong.get(i),count[i]));
				return false;
			}
		}
		return true;
	}

	/**
	 * Schreibt die Einstellungen in das im Konstruktor übergebene Transporter-Objekt zurück.
	 */
	public void storeData() {
		final Map<String,Integer> map=transporter.getCount();
		map.clear();
		for (int i=0;i<count.length;i++) {
			final Integer I=NumberTools.getNotNegativeInteger(count[i]);
			if (I!=null) map.put(stations.get(i),I);
		}
	}
}
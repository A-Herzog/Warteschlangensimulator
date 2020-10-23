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

import javax.swing.JComboBox;
import javax.swing.table.TableCellEditor;

import language.Language;
import mathtools.NumberTools;
import tools.JTableExt;
import tools.JTableExtAbstractTableModel;
import ui.images.Images;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;

/**
 * Tabellenmodell zur Auswahl der Daten für einen Wegpunkt
 * @author Alexander Herzog
 * @see ModelElementWayPoint
 */
public class WayPointTableModel extends JTableExtAbstractTableModel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 6666152316668456454L;

	/** Zugehörige Tabelle (um das Update der Tabelle veranlassen zu können, wenn sich die Daten verändert haben) */
	private final JTableExt table;
	private final List<WayPointRecord> recordsOriginal;
	private final List<WayPointRecord> records;
	/** Nur-Lese-Status */
	private final boolean readOnly;
	private final String[] stations;

	/**
	 * Konstruktor der Klasse
	 * @param table	Zugehörige Tabelle (um das Update der Tabelle veranlassen zu können, wenn sich die Daten verändert haben)
	 * @param records	Liste mit den Einträge auf welchen Routen der betreffende Wegpunkt angesteuert werden soll
	 * @param mainSurface	Zeichenfläche die alle Wegpunkte und Stationen enthält
	 * @param readOnly	Nur-Lese-Status
	 */
	public WayPointTableModel(final JTableExt table, final List<WayPointRecord> records, final ModelSurface mainSurface, final boolean readOnly) {
		super();
		this.table=table;
		this.recordsOriginal=records;
		this.records=new ArrayList<>();
		for (WayPointRecord wayPointRecord: records) this.records.add(new WayPointRecord(wayPointRecord));
		this.readOnly=readOnly;
		stations=getStationsList(mainSurface);
	}

	private String[] getStationsList(final ModelSurface mainSurface) {
		final List<String> stations=new ArrayList<>();

		stations.add("<"+Language.tr("Surface.WayPoint.All")+">");

		for (ModelElement element1: mainSurface.getElements()) {
			final String name1=element1.getName();
			if (!name1.isEmpty() && !stations.contains(name1)) {
				if (element1 instanceof ModelElementTransportSource) stations.add(name1);
				if (element1 instanceof ModelElementTransportTransporterSource) stations.add(name1);
				if (element1 instanceof ModelElementTransportParking) stations.add(name1);
				if (element1 instanceof ModelElementTransportDestination) stations.add(name1);
			}
			if (element1 instanceof ModelElementSub) for (ModelElement element2: ((ModelElementSub)element1).getSubSurface().getElements()) {
				final String name2=element2.getName();
				if (!name2.isEmpty() && !stations.contains(name2)) {
					if (element2 instanceof ModelElementTransportSource) stations.add(name2);
					if (element2 instanceof ModelElementTransportTransporterSource) stations.add(name2);
					if (element2 instanceof ModelElementTransportParking) stations.add(name2);
					if (element2 instanceof ModelElementTransportDestination) stations.add(name2);
				}
			}
		}

		return stations.toArray(new String[0]);
	}

	@Override
	public final boolean isCellEditable(int rowIndex, int columnIndex) {
		return !readOnly;
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0: return Language.tr("Surface.WayPoint.Table.StationFrom");
		case 1: return Language.tr("Surface.WayPoint.Table.StationTo");
		case 2: return Language.tr("Surface.WayPoint.Table.Index");
		case 3: return Language.tr("Surface.WayPoint.Table.Control");
		default: return super.getColumnName(column);
		}
	}

	/**
	 * Aktualisiert die Tabellendarstellung
	 */
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
		return 4;
	}

	private JComboBox<String> getStationCombo(final int index, final boolean dest) {
		final JComboBox<String> combo=new JComboBox<>(stations);

		int selIndex=-1;
		final String name;
		if (dest) name=records.get(index).getStationB(); else name=records.get(index).getStationA();
		if (name.equals("*")) {
			selIndex=0;
		} else {
			for (int i=0;i<stations.length;i++) if (name.equals(stations[i])) {selIndex=i; break;}
			if (selIndex<0 && stations.length>0) selIndex=0;
		}
		if (selIndex>=0) {
			combo.setSelectedIndex(selIndex);
			if (dest) {
				if (combo.getSelectedIndex()==0) records.get(index).setStationB("*"); else records.get(index).setStationB((String)combo.getSelectedItem());
			} else {
				if (combo.getSelectedIndex()==0) records.get(index).setStationA("*"); else records.get(index).setStationA((String)combo.getSelectedItem());
			}
		}

		combo.addActionListener(e->{
			if (dest) {
				if (combo.getSelectedIndex()==0) records.get(index).setStationB("*"); else records.get(index).setStationB((String)combo.getSelectedItem());
			} else {
				if (combo.getSelectedIndex()==0) records.get(index).setStationA("*"); else records.get(index).setStationA((String)combo.getSelectedItem());
			}
		});

		return combo;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex==records.size()) {
			if (columnIndex==3) return makeButtonPanel(new String[]{Language.tr("Surface.WayPoint.Table.Add")},new URL[]{Images.EDIT_ADD.getURL()},new ActionListener[]{new TableButtonListener(ActionIndex.ACTION_ADD)});
			return "";
		}

		switch (columnIndex) {
		case 0:
			return getStationCombo(rowIndex,false);
		case 1:
			return getStationCombo(rowIndex,true);
		case 2:
			return ""+records.get(rowIndex).getIndex();
		case 3:
			final List<URL> icons=new ArrayList<>();
			final List<String> hints=new ArrayList<>();
			final List<ActionListener> listeners=new ArrayList<>();
			icons.add(Images.EDIT_DELETE.getURL());
			hints.add(Language.tr("Surface.WayPoint.Table.Delete"));
			listeners.add(new TableButtonListener(rowIndex,ActionIndex.ACTION_DELETE));
			if (rowIndex>0) {
				icons.add(Images.ARROW_UP.getURL());
				hints.add(Language.tr("Surface.WayPoint.Table.MoveUp"));
				listeners.add(new TableButtonListener(rowIndex,ActionIndex.ACTION_UP));
			}
			if (rowIndex<records.size()-1) {
				icons.add(Images.ARROW_DOWN.getURL());
				hints.add(Language.tr("Surface.WayPoint.Table.MoveDown"));
				listeners.add(new TableButtonListener(rowIndex,ActionIndex.ACTION_DOWN));
			}
			return makeButtonPanel(null,hints.toArray(new String[0]),icons.toArray(new URL[0]),listeners.toArray(new ActionListener[0]));
		}

		return null;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (columnIndex!=2 || rowIndex<0 || rowIndex>=records.size()) return;
		if (!(aValue instanceof String)) return;
		final Integer I=NumberTools.getNotNegativeInteger((String)aValue);
		if (I!=null) records.get(rowIndex).setIndex(I.intValue());

	}

	/**
	 * Schreibt die Daten aus der Tabelle in die Wegpunkt-Informations-Liste zurück.
	 */
	public void storeData() {
		recordsOriginal.clear();
		recordsOriginal.addAll(records);
	}

	private enum ActionIndex {ACTION_ADD, ACTION_UP, ACTION_DOWN, ACTION_DELETE}

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
			WayPointRecord wayPointRecord;
			switch (actionIndex) {
			case ACTION_ADD:
				records.add(new WayPointRecord());
				updateTable();
				break;
			case ACTION_UP:
				if (row>0) {
					wayPointRecord=records.get(row-1);
					records.set(row-1,records.get(row));
					records.set(row,wayPointRecord);
				}
				updateTable();
				break;
			case ACTION_DOWN:
				if (row<records.size()-1) {
					wayPointRecord=records.get(row+1);
					records.set(row+1,records.get(row));
					records.set(row,wayPointRecord);
				}
				updateTable();
				break;
			case ACTION_DELETE:
				records.remove(row);
				updateTable();
				break;
			}
		}
	}
}
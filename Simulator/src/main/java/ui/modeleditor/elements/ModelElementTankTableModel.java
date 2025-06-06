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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.table.TableCellEditor;

import language.Language;
import mathtools.NumberTools;
import systemtools.MsgBox;
import tools.JTableExt;
import tools.JTableExtAbstractTableModel;
import ui.images.Images;
import ui.modeleditor.ModelSurface;

/**
 * In dieser Tabelle k�nnen die Ventile eines Tanks innerhalb
 * eines {@link ModelElementTankDialog}-Elements bearbeitet
 * werden.
 * @author Alexander Herzog
 * @see ModelElementTankDialog
 * @see ModelElementTank
 */
public class ModelElementTankTableModel extends JTableExtAbstractTableModel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -61897197344899292L;

	/** Zugeh�rige Tabelle (um diese anweisen zu k�nnen, sich neu aufzubauen, wenn die Daten ver�ndert wurden) */
	private final JTableExt table;
	/** Im Konstruktor �bergebenes Ventile-Objekt welches in {@link #storeData()} aktualisiert wird */
	private final List<ModelElementTank.Valve> valvesOriginal;
	/** Tempor�re Arbeitskopie der Ventil-Daten */
	private final List<ModelElementTank.Valve> valves;
	/** Nur-Lese-Status */
	private final boolean readOnly;

	/**
	 * Konstruktor der Klasse
	 * @param table	Zugeh�rige Tabelle (um diese anweisen zu k�nnen, sich neu aufzubauen, wenn die Daten ver�ndert wurden)
	 * @param valves	Liste mit allen Ventilen des Tanks
	 * @param readOnly	Nur-Lese-Status
	 */
	public ModelElementTankTableModel(final JTableExt table, final List<ModelElementTank.Valve> valves, final boolean readOnly) {
		super();
		this.table=table;
		this.valvesOriginal=valves;
		this.readOnly=readOnly;
		this.valves=valvesOriginal.stream().map(ModelElementTank.Valve::new).collect(Collectors.toList());
		updateTable();
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
		return valves.size()+1;
	}

	@Override
	public int getColumnCount() {
		return 4;
	}

	@Override
	public final boolean isCellEditable(final int rowIndex, final int columnIndex) {
		if (readOnly) return false;
		if (rowIndex==valves.size()) {
			return columnIndex==3;
		} else {
			return columnIndex>0;
		}
	}

	@Override
	public String getColumnName(final int column) {
		switch (column) {
		case 0: return Language.tr("Surface.Tank.Dialog.Valve.Nr");
		case 1: return Language.tr("Surface.Tank.Dialog.Valve.InitialFlow");
		case 2: return Language.tr("Surface.Tank.Dialog.Valve.TimeBase");
		case 3: return Language.tr("Surface.Tank.Dialog.Valve.Control");
		default: return super.getColumnName(column);
		}
	}

	/**
	 * Liefert die Auswahlbox f�r die Zeiteinheit in Spalte 3
	 * @param valve	Ventil f�r das die Zeiteinheit eingestellt werden soll
	 * @return	Auswahlbox f�r die Zeiteinheit
	 */
	private JComboBox<String> getCombo(final ModelElementTank.Valve valve) {
		final JComboBox<String> combo=new JComboBox<>(new String[]{
				Language.tr("Surface.Tank.Dialog.Unit.DeltaSeconds"),
				Language.tr("Surface.Tank.Dialog.Unit.DeltaMinutes"),
				Language.tr("Surface.Tank.Dialog.Unit.DeltaHours")
		});

		switch (valve.getTimeBase()) {
		case TIMEBASE_HOURS: combo.setSelectedIndex(2); break;
		case TIMEBASE_MINUTES: combo.setSelectedIndex(1); break;
		case TIMEBASE_SECONDS: combo.setSelectedIndex(0); break;
		default: combo.setSelectedIndex(0); break;
		}

		combo.addActionListener(e->{
			switch (combo.getSelectedIndex()) {
			case 0: valve.setTimeBase(ModelSurface.TimeBase.TIMEBASE_SECONDS); break;
			case 1: valve.setTimeBase(ModelSurface.TimeBase.TIMEBASE_MINUTES); break;
			case 2: valve.setTimeBase(ModelSurface.TimeBase.TIMEBASE_HOURS); break;
			}
		});

		return combo;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex==valves.size()) {
			if (columnIndex<3) return "";
			return makeButtonPanel(new String[]{Language.tr("Surface.Tank.Dialog.Valve.Add")},new Icon[]{Images.EDIT_ADD.getIcon()},new ActionListener[]{new ButtonListener(-1)});
		}

		switch (columnIndex) {
		case 0:
			return ""+(rowIndex+1);
		case 1:
			return NumberTools.formatNumberMax(valves.get(rowIndex).getInitialValue());
		case 2:
			return getCombo(valves.get(rowIndex));
		case 3:
			final List<String> title=new ArrayList<>();
			final List<String> tooltip=new ArrayList<>();
			final List<Icon> icons=new ArrayList<>();
			final List<ActionListener> listener=new ArrayList<>();

			if (rowIndex>0) {
				title.add("");
				tooltip.add(Language.tr("Surface.Tank.Dialog.Valve.Up"));
				icons.add(Images.ARROW_UP.getIcon());
				listener.add(new ButtonListener(rowIndex,true));
			}

			if (rowIndex<valves.size()-1) {
				title.add("");
				tooltip.add(Language.tr("Surface.Tank.Dialog.Valve.Down"));
				icons.add(Images.ARROW_DOWN.getIcon());
				listener.add(new ButtonListener(rowIndex,false));
			}

			title.add("");
			tooltip.add(Language.tr("Surface.Tank.Dialog.Valve.Delete"));
			icons.add(Images.EDIT_DELETE.getIcon());
			listener.add(new ButtonListener(rowIndex));

			return makeButtonPanel(title.toArray(String[]::new),tooltip.toArray(String[]::new),icons.toArray(Icon[]::new),listener.toArray(ActionListener[]::new));
		default:
			return "";
		}
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (columnIndex!=1 || rowIndex<0 || rowIndex>=valves.size()) return;
		if (!(aValue instanceof String)) return;
		final String text=(String)aValue;
		final Double D=NumberTools.getNotNegativeDouble(text);
		if (D==null) return;
		valves.get(rowIndex).setInitialValue(D.doubleValue());
	}

	/**
	 * Weist die Tabelle an, die neuen Daten in die im
	 * Konstruktor �bergeben Liste zur�ck zu schreiben.
	 */
	public void storeData() {
		valvesOriginal.clear();
		valvesOriginal.addAll(valves);
	}

	/**
	 * Reagiert auf Klicks auf die Verschieben-Schaltfl�chen
	 * und die L�schen-Schaltfl�chen in der Tabelle
	 */
	private class ButtonListener implements ActionListener {
		/** Zeilennummer */
		private final int row;
		/** Verschieberichtung (-1 oder 1) oder L�schen (0) */
		private final int move;

		/**
		 * Konstruktor der Klasse
		 * @param row	Zeilennummer
		 * @param moveUp	Verschiebung nach oben (<code>true</code>) oder nach unten (<code>false</code>)
		 */
		public ButtonListener(final int row, final boolean moveUp) {
			this.row=row;
			if (moveUp) move=-1; else move=1;
		}

		/**
		 * Konstruktor der Klasse<br>
		 * (Modus: L�schen)
		 * @param row	Zeilennummer
		 */
		public ButtonListener(final int row) {
			this.row=row;
			move=0;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (row<0) {
				/* Add */
				updateTable();
				valves.add(new ModelElementTank.Valve());
				updateTable();
			} else {
				switch (move) {
				case -1:
					/* Move up */
					if (row>0) {
						updateTable();
						ModelElementTank.Valve valve=valves.get(row);
						valves.set(row,valves.get(row-1));
						valves.set(row-1,valve);
						updateTable();
					}
					break;
				case 1:
					/* Move down */
					if (row<valves.size()-1) {
						updateTable();
						ModelElementTank.Valve valve=valves.get(row);
						valves.set(row,valves.get(row+1));
						valves.set(row+1,valve);
						updateTable();
					}
					break;
				case 0:
					/* Delete */
					if (row>=0) {
						if ((e.getModifiers() & ActionEvent.SHIFT_MASK)==0) {
							if (!MsgBox.confirm(table,Language.tr("Surface.Tank.Dialog.Valve.Delete.Title"),String.format(Language.tr("Surface.Tank.Dialog.Valve.Delete.Info"),row+1),Language.tr("Surface.Tank.Dialog.Valve.Delete.YesInfo"),Language.tr("Surface.Tank.Dialog.Valve.Delete,NoInfo"))) return;
						}
						updateTable();
						valves.remove(row);
						updateTable();
					}
				}
			}
		}
	}
}
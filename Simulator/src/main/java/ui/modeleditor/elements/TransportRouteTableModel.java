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

import javax.swing.Icon;
import javax.swing.table.TableCellEditor;

import language.Language;
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.JTableExt;
import tools.JTableExtAbstractTableModel;
import ui.images.Images;
import ui.modeleditor.ModelSurface;

/**
 * Diese Tabelle erlaubt die Konfiguration von Routing-Zielen für ein
 * {@link TransportTargetSystemPanel}-Element.
 * @author Alexander Herzog
 * @see TransportTargetSystemPanel
 */
public class TransportRouteTableModel extends JTableExtAbstractTableModel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -6468064552683722803L;

	/** Zugehörige Tabelle (um das Update der Tabelle veranlassen zu können, wenn sich die Daten verändert haben) */
	private final JTableExt table;
	/** Hilfe-Callback */
	private final Runnable help;
	/** Liste mit allen Routing-Zielen */
	private final List<TransportTargetRecord> routes;
	/** Liste mit allen Kundentypennamen (für Kundentyp-abhängiges Routing) */
	private final String[] clientTypes;
	/** Liste mit möglichen Zielstationen */
	private final String[] stations;
	/** Liste mit den globalen Variablen (zur Prüfung der Ausdrücke) */
	private final String[] variables;
	/** Gesamtes Modell (für den Expression-Builder) */
	private final EditModel model;
	/** Haupt-Zeichenfläche (für den Expression-Builder) */
	private final ModelSurface surface;
	/** Nur-Lese-Status */
	private final boolean readOnly;

	/**
	 *
	 * @param table	Zugehörige Tabelle (um das Update der Tabelle veranlassen zu können, wenn sich die Daten verändert haben)
	 * @param routes	Liste mit allen Routing-Zielen
	 * @param clientTypes	Liste mit allen Kundentypennamen (für Kundentyp-abhängiges Routing)
	 * @param stations	Liste mit möglichen Zielstationen
	 * @param variables	Liste mit den globalen Variablen (zur Prüfung der Ausdrücke)
	 * @param model	Gesamtes Modell (für den Expression-Builder)
	 * @param surface	Haupt-Zeichenfläche (für den Expression-Builder)
	 * @param readOnly	Nur-Lese-Status
	 * @param help	Hilfe-Callback
	 */
	public TransportRouteTableModel(final JTableExt table, final List<TransportTargetRecord> routes, final String[] clientTypes, final String[] stations, final String[] variables, final EditModel model, final ModelSurface surface, final boolean readOnly, final Runnable help) {
		super();

		this.help=help;
		this.table=table;
		this.readOnly=readOnly;
		this.routes=new ArrayList<>();
		for (TransportTargetRecord route: routes) this.routes.add(route.clone());
		this.clientTypes=clientTypes;
		this.stations=stations;
		this.variables=variables;
		this.model=model;
		this.surface=surface;

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
		return routes.size()+1;
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex==routes.size()) {
			switch (columnIndex) {
			case 0:	return makeButtonPanel(new String[]{Language.tr("Surface.TransportSource.Dialog.Tab.RoutingTargets.Add")},new Icon[]{Images.EDIT_ADD.getIcon()},new ActionListener[]{new EditButtonListener(0,-1)});
			case 1: return "";
			}
		}

		switch (columnIndex) {
		case 0:
			return makeEditPanelSmallBorderIcon(
					Images.MODELEDITOR_ELEMENT_TRANSPORT_TARGET.getIcon(),
					routes.get(rowIndex).getConditionText(),
					new Icon[]{Images.GENERAL_SETUP.getIcon(),Images.EDIT_DELETE.getIcon()},
					new String[]{
							Language.tr("Surface.TransportSource.Dialog.Tab.RoutingTargets.Edit"),
							Language.tr("Surface.TransportSource.Dialog.Tab.RoutingTargets.Delete")
					},

					new ActionListener[]{new EditButtonListener(0,rowIndex),new DeleteButtonListener(rowIndex)}
					);
		case 1:
			if (rowIndex==0 && rowIndex==routes.size()-1) {
				/* kein hoch oder runter */
				return makePanel(routes.get(rowIndex).station,null);
			}
			if (rowIndex==0) {
				/* kein Nach-Oben-Button */
				return makeEditPanelSmallBorder(
						routes.get(rowIndex).station,
						new Icon[]{Images.ARROW_DOWN.getIcon()},
						new String[]{Language.tr("Surface.TransportSource.Dialog.Tab.RoutingTargets.Down")},
						new ActionListener[]{new EditButtonListener(2,rowIndex)});
			}
			if (rowIndex==routes.size()-1) {
				/* kein Nach-Unten-Button */
				return makeEditPanelSmallBorder(
						routes.get(rowIndex).station,
						new Icon[]{Images.ARROW_UP.getIcon()},
						new String[]{Language.tr("Surface.TransportSource.Dialog.Tab.RoutingTargets.Up")},
						new ActionListener[]{new EditButtonListener(1,rowIndex)});
			}
			return makeEditPanelSmallBorder(
					routes.get(rowIndex).station,
					new Icon[]{Images.ARROW_UP.getIcon(),Images.ARROW_DOWN.getIcon()},
					new String[]{
							Language.tr("Surface.TransportSource.Dialog.Tab.RoutingTargets.Up"),
							Language.tr("Surface.TransportSource.Dialog.Tab.RoutingTargets.Down")
					},
					new ActionListener[]{new EditButtonListener(1,rowIndex),new EditButtonListener(2,rowIndex)});


		default: return null;
		}
	}

	@Override
	public final boolean isCellEditable(int rowIndex, int columnIndex) {
		return !readOnly;
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0: return Language.tr("Surface.TransportSource.Dialog.Tab.RoutingTargets.Condition");
		case 1: return Language.tr("Surface.TransportSource.Dialog.Tab.RoutingTargets.DestinationStation");
		default: return super.getColumnName(column);
		}
	}

	/**
	 * Überträgt die Liste mit den Routing-Zielen aus der Tabelle
	 * in das angegeben Listen-Objekt.
	 * @param routes	Listen-Objekt zu dem die Routing-Ziele aus der Tabelle hinzugefügt werden sollen (bestehende Einträge in der Liste werden dabei <b>nicht</b> gelöscht)
	 */
	public void storeData(final List<TransportTargetRecord> routes) {
		for (TransportTargetRecord route: this.routes) routes.add(route.clone());
	}

	/**
	 * Reagiert auf Klicks auf die Bearbeiten und Verschieben-Schaltflächen
	 */
	private class EditButtonListener implements ActionListener {
		/** Auszuführender Befehl (0: Bearbeiten, 1: In der Liste nach oben schieben, 2: In der Liste nach unten schieben) */
		private final int col;
		/** Zeilennummer */
		private final int row;

		/**
		 * Konstruktor der Klasse
		 * @param col	Auszuführender Befehl (0: Bearbeiten, 1: In der Liste nach oben schieben, 2: In der Liste nach unten schieben)
		 * @param row	Zeilennummer
		 */
		public EditButtonListener(final int col, final int row) {
			this.col=col;
			this.row=row;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (readOnly) return;

			TransportTargetRecord route;

			switch (col) {
			case 0:
				TransportRouteTableModelDialog dialog=new TransportRouteTableModelDialog(table,help,(row>=0)?routes.get(row):null,clientTypes,stations,variables,model,surface);
				dialog.setVisible(true);
				if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
					if (row<0) {
						routes.add(dialog.getRoute());
					} else {
						routes.set(row,dialog.getRoute());
					}
					updateTable();
				}
				break;
			case 1:
				if (row>0) {
					route=routes.get(row); routes.set(row,routes.get(row-1)); routes.set(row-1,route);
					updateTable();
				}
				break;
			case 2:
				if (row<routes.size()-1) {
					route=routes.get(row); routes.set(row,routes.get(row+1)); routes.set(row+1,route);
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
			final String name=routes.get(row).getConditionText();
			if ((e.getModifiers() & ActionEvent.SHIFT_MASK)==0) {
				if (!MsgBox.confirm(table,Language.tr("Surface.TransportSource.Dialog.Tab.RoutingTargets.Delete.Confirm.Title"),String.format(Language.tr("Surface.TransportSource.Dialog.Tab.RoutingTargets.Delete.Confirm.Info"),name),Language.tr("Surface.TransportSource.Dialog.Tab.RoutingTargets.Delete.Confirm.YesInfo"),Language.tr("Surface.TransportSource.Dialog.Tab.RoutingTargets.Delete.Confirm.NoInfo"))) return;
			}
			routes.remove(row);
			updateTable();
		}
	}
}
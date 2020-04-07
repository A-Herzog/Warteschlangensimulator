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
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.JTableExt;
import tools.JTableExtAbstractTableModel;
import ui.images.Images;
import ui.modeleditor.ModelSurface;

/**
 * Diese Tabelle erlaubt die Konfiguration von Routing-Zielen f�r ein
 * {@link TransportTargetSystemPanel}-Element.
 * @author Alexander Herzog
 * @see TransportTargetSystemPanel
 */
public class TransportRouteTableModel extends JTableExtAbstractTableModel {
	private static final long serialVersionUID = -6468064552683722803L;

	private final JTableExt table;
	private final Runnable help;
	private final List<TransportTargetRecord> routes;
	private final String[] clientTypes;
	private final String[] stations;
	private final String[] variables;
	private final EditModel model;
	private final ModelSurface surface;
	private final boolean readOnly;

	/**
	 *
	 * @param table	Zugeh�rige Tabelle (um das Update der Tabelle veranlassen zu k�nnen, wenn sich die Daten ver�ndert haben)
	 * @param routes	Liste mit allen Routing-Zielen
	 * @param clientTypes	Liste mit allen Kundentypennamen (f�r Kundentyp-abh�ngiges Routing)
	 * @param stations	Liste mit m�glichen Zielstationen
	 * @param variables	Liste mit den globalen Variablen (zur Pr�fung der Ausdr�cke)
	 * @param model	Gesamtes Modell (f�r den Expression-Builder)
	 * @param surface	Haupt-Zeichenfl�che (f�r den Expression-Builder)
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
			case 0:	return makeButtonPanel(new String[]{Language.tr("Surface.TransportSource.Dialog.Tab.RoutingTargets.Add")},new URL[]{Images.EDIT_ADD.getURL()},new ActionListener[]{new EditButtonListener(0,-1)});
			case 1: return "";
			}
		}

		switch (columnIndex) {
		case 0:
			return makeEditPanelSmallBorder(
					Images.MODELEDITOR_ELEMENT_TRANSPORT_TARGET.getURL(),
					routes.get(rowIndex).getConditionText(),
					new URL[]{Images.GENERAL_SETUP.getURL(),Images.EDIT_DELETE.getURL()},
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
						new URL[]{Images.ARROW_DOWN.getURL()},
						new String[]{Language.tr("Surface.TransportSource.Dialog.Tab.RoutingTargets.Down")},
						new ActionListener[]{new EditButtonListener(2,rowIndex)});
			}
			if (rowIndex==routes.size()-1) {
				/* kein Nach-Unten-Button */
				return makeEditPanelSmallBorder(
						routes.get(rowIndex).station,
						new URL[]{Images.ARROW_UP.getURL()},
						new String[]{Language.tr("Surface.TransportSource.Dialog.Tab.RoutingTargets.Up")},
						new ActionListener[]{new EditButtonListener(1,rowIndex)});
			}
			return makeEditPanelSmallBorder(
					routes.get(rowIndex).station,
					new URL[]{Images.ARROW_UP.getURL(),Images.ARROW_DOWN.getURL()},
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
	 * �bertr�gt die Liste mit den Routing-Zielen aus der Tabelle
	 * in das angegeben Listen-Objekt.
	 * @param routes	Listen-Objekt zu dem die Routing-Ziele aus der Tabelle hinzugef�gt werden sollen (bestehende Eintr�ge in der Liste werden dabei <b>nicht</b> gel�scht)
	 */
	public void storeData(final List<TransportTargetRecord> routes) {
		for (TransportTargetRecord route: this.routes) routes.add(route.clone());
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

	private class DeleteButtonListener implements ActionListener {
		private final int row;

		public DeleteButtonListener(final int row) {
			this.row=row;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (readOnly) return;
			final String name=routes.get(row).getConditionText();
			if (!MsgBox.confirm(table,Language.tr("Surface.TransportSource.Dialog.Tab.RoutingTargets.Delete.Confirm.Title"),String.format(Language.tr("Surface.TransportSource.Dialog.Tab.RoutingTargets.Delete.Confirm.Info"),name),Language.tr("Surface.TransportSource.Dialog.Tab.RoutingTargets.Delete.Confirm.YesInfo"),Language.tr("Surface.TransportSource.Dialog.Tab.RoutingTargets.Delete.Confirm.NoInfo"))) return;
			routes.remove(row);
			updateTable();
		}
	}
}
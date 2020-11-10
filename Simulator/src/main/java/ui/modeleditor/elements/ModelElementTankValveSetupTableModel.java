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
import java.util.stream.Collectors;

import javax.swing.table.TableCellEditor;

import language.Language;
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.JTableExt;
import tools.JTableExtAbstractTableModel;
import ui.images.Images;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementTankValveSetup.ValveSetup;

/**
 * In dieser Tabelle können Veränderungen der Ventil-Einstellungen für Tank-Elemente
 * innerhalb eines {@link ModelElementTankValveSetupDialog}-Elements bearbeitet werden.
 * @author Alexander Herzog
 * @see ModelElementTankValveSetupDialog
 * @see ModelElementTankValveSetup
 */
public class ModelElementTankValveSetupTableModel extends JTableExtAbstractTableModel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 4122007587514525620L;

	/** Zugehörige Tabelle (um diese anweisen zu können, sich neu aufzubauen, wenn die Daten verändert wurden) */
	private final JTableExt table;
	/** Temporäre Arbeitskopie der Ventil-Einstellungen */
	private final List<ModelElementTankValveSetup.ValveSetup> valveSetups;
	/** Original Ventil-Einstellungen (wird in {@link #storeData()} aktualisiert) */
	private final List<ModelElementTankValveSetup.ValveSetup> valveSetupsOriginal;
	/** Haupt-Zeichenfläche (für den Expression-Builder) */
	private final ModelSurface mainSurface;
	/** Gesamtes Editor-Modell (für den Expression-Builder) */
	private final EditModel model;
	/** Nur-Lese-Status */
	private final boolean readOnly;
	/** Hilfe-Callback */
	private final Runnable helpRunnable;

	/**
	 * Konstruktor der Klasse
	 * @param table	Zugehörige Tabelle (um diese anweisen zu können, sich neu aufzubauen, wenn die Daten verändert wurden)
	 * @param valveSetups	Liste der Ventil-Konfigurationen, die in der Tabelle dargestellt werden sollen
	 * @param mainSurface	Haupt-Zeichenfläche (für den Expression-Builder)
	 * @param model	Gesamtes Editor-Modell (für den Expression-Builder)
	 * @param readOnly	Nur-Lese-Status
	 * @param helpRunnable	Hilfe-Callback
	 */
	public ModelElementTankValveSetupTableModel(final JTableExt table, final List<ModelElementTankValveSetup.ValveSetup> valveSetups, final ModelSurface mainSurface, final EditModel model, final boolean readOnly, final Runnable helpRunnable) {
		super();
		this.table=table;
		this.valveSetupsOriginal=valveSetups;
		this.valveSetups=new ArrayList<>(valveSetupsOriginal.stream().map(ModelElementTankValveSetup.ValveSetup::new).collect(Collectors.toList()));
		this.mainSurface=mainSurface;
		this.model=model;
		this.readOnly=readOnly;
		this.helpRunnable=helpRunnable;
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
	public final boolean isCellEditable(int rowIndex, int columnIndex) {
		return !readOnly && columnIndex==1;
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0: return Language.tr("Surface.TankValveSetup.Dialog.TableCol.ValveSetup");
		case 1: return Language.tr("Surface.TankValveSetup.Dialog.TableCol.Control");
		default: return super.getColumnName(column);
		}
	}

	@Override
	public int getRowCount() {
		return valveSetups.size()+1;
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	/**
	 * Wandelt die Zeichen "&amp;", "&lt;" und "&gt;" in ihre entsprechenden
	 * HTML-Entitäten um.
	 * @param line	Umzuwandelnder Text
	 * @return	Umgewandelter Text
	 */
	private String encodeHTMLentities(final String line) {
		if (line==null) return "";
		String result;
		result=line.replaceAll("&","&amp;");
		result=result.replaceAll("<","&lt;");
		result=result.replaceAll(">","&gt;");
		return result;
	}

	/**
	 * Liefert eine Beschreibung für eine Ventil-Einstellung.
	 * @param valveSetup	Ventil-Einstellung
	 * @return	Beschreibung der Ventil-Einstellung
	 */
	private String getValveSetupText(final ModelElementTankValveSetup.ValveSetup valveSetup) {
		final StringBuilder sb=new StringBuilder();
		final ModelElement element=mainSurface.getById(valveSetup.tankId);
		final String tankName=(element==null)?"":"\""+element.getName()+"\" (id="+element.getId()+")";
		sb.append("<html><body>");
		sb.append(encodeHTMLentities(String.format(Language.tr("Surface.TankValveSetup.Table.DisplayTank"),tankName,valveSetup.valveNr+1)));
		sb.append("<br>");
		sb.append(encodeHTMLentities(String.format(Language.tr("Surface.TankValveSetup.Table.MaxFlow"),valveSetup.maxFlow)));
		sb.append("</body></html>");

		return sb.toString();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex==valveSetups.size()) {
			if (columnIndex==1) return makeButtonPanel(new String[]{Language.tr("Surface.TankValveSetup.Table.Add")},new URL[]{Images.EDIT_ADD.getURL()},new ActionListener[]{new TableButtonListener()});
			return "";
		}

		switch (columnIndex) {
		case 0:
			return makePanel(getValveSetupText(valveSetups.get(rowIndex)),Images.MODELEDITOR_ELEMENT_TANK_VALVE.getURL());
		case 1:
			final List<URL> icons=new ArrayList<>();
			final List<String> hints=new ArrayList<>();
			final List<ActionListener> listeners=new ArrayList<>();
			if (rowIndex>0) {
				icons.add(Images.ARROW_UP.getURL());
				hints.add(Language.tr("Surface.TankValveSetup.Table.MoveUp"));
				listeners.add(new TableButtonListener(rowIndex,ActionIndex.ACTION_MOVE_UP));
			}
			if (rowIndex<valveSetups.size()-1) {
				icons.add(Images.ARROW_DOWN.getURL());
				hints.add(Language.tr("Surface.TankValveSetup.Table.MoveDown"));
				listeners.add(new TableButtonListener(rowIndex,ActionIndex.ACTION_MOVE_DOWN));
			}
			icons.add(Images.GENERAL_SETUP.getURL());
			hints.add(Language.tr("Surface.TankValveSetup.Table.Edit"));
			listeners.add(new TableButtonListener(rowIndex,ActionIndex.ACTION_EDIT));
			icons.add(Images.EDIT_DELETE.getURL());
			hints.add(Language.tr("Surface.TankValveSetup.Table.Delete"));
			listeners.add(new TableButtonListener(rowIndex,ActionIndex.ACTION_DELETE));
			return makeButtonPanel(null,hints.toArray(new String[0]),icons.toArray(new URL[0]),listeners.toArray(new ActionListener[0]));
		}

		return null;
	}

	/**
	 * Weist die Tabelle an, die neuen Daten in die im
	 * Konstruktor übergeben Liste zurück zu schreiben.
	 */
	public void storeData() {
		valveSetupsOriginal.clear();
		valveSetupsOriginal.addAll(valveSetups);
	}

	/**
	 * Auszuführende Aktion
	 * @see TableButtonListener
	 */
	private enum ActionIndex {
		/** Eintrag hinzufügen */
		ACTION_ADD,
		/** Eintrag bearbeiten */
		ACTION_EDIT,
		/** Eintrag löschen */
		ACTION_DELETE,
		/** Eintrag in der Liste nach oben verschieben */
		ACTION_MOVE_UP,
		/** Eintrag in der Liste nach unten verschieben */
		ACTION_MOVE_DOWN
	}

	/**
	 * Reagiert auf Klicks auf die Schaltflächen in den Tabellenzellen
	 */
	private class TableButtonListener implements ActionListener {
		/** Zeile */
		final int row;
		/** Auszuführende Aktion */
		final ActionIndex actionIndex;

		/**
		 * Konstruktor der Klasse
		 * @param row	Zeile
		 * @param actionIndex	Auszuführende Aktion
		 */
		public TableButtonListener(final int row, final ActionIndex actionIndex) {
			this.row=row;
			this.actionIndex=actionIndex;
		}

		/**
		 * Konstruktor der Klasse<br>
		 * Modus: Hinzufügen
		 */
		public TableButtonListener() {
			this(0,ActionIndex.ACTION_ADD);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (readOnly) return;

			ModelElementTankValveSetup.ValveSetup temp;
			ModelElementTankValveSetupTableModelDialog dialog;

			switch (actionIndex) {
			case ACTION_ADD:
				temp=new ValveSetup();
				dialog=new ModelElementTankValveSetupTableModelDialog(table,temp,mainSurface,model,helpRunnable);
				if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
					valveSetups.add(temp);
					updateTable();
				}
				break;
			case ACTION_EDIT:
				dialog=new ModelElementTankValveSetupTableModelDialog(table,valveSetups.get(row),mainSurface,model,helpRunnable);
				if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
					updateTable();
				}
				break;
			case ACTION_DELETE:
				if (!MsgBox.confirm(table,Language.tr("Surface.TankValveSetup.Table.Delete.ConfirmTitle"),String.format(Language.tr("Surface.TankValveSetup.Table.Delete.ConfirmInfo"),valveSetups.get(row).tankId,valveSetups.get(row).valveNr+1),Language.tr("Surface.TankValveSetup.Table.Delete.ConfirmYesInfo"),Language.tr("Surface.TankValveSetup.Table.Delete.ConfirmNoInfo"))) return;
				valveSetups.remove(row);
				updateTable();
				break;
			case ACTION_MOVE_DOWN:
				temp=valveSetups.get(row);
				valveSetups.set(row,valveSetups.get(row+1));
				valveSetups.set(row+1,temp);
				updateTable();
				break;
			case ACTION_MOVE_UP:
				temp=valveSetups.get(row);
				valveSetups.set(row,valveSetups.get(row-1));
				valveSetups.set(row-1,temp);
				updateTable();
				break;
			}
		}
	}
}
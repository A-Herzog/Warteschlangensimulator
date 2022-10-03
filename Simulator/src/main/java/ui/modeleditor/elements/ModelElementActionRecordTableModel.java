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
import mathtools.NumberTools;
import mathtools.TimeTools;
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.JTableExt;
import tools.JTableExtAbstractTableModel;
import ui.images.Images;
import ui.modeleditor.ModelSurface;

/**
 * Tabelle die mögliche Aktionen, die ein {@link ModelElementAction}-Element
 * oder ein {@link ModelElementInteractiveButton}-Element verwenden können,
 * enthält.
 * @author Alexander Herzog
 * @see ModelElementActionDialog
 * @see ModelElementAction
 * @see ModelElementInteractiveButton
 * @see ModelElementInteractiveButtonDialog
 */
public class ModelElementActionRecordTableModel extends JTableExtAbstractTableModel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -1341554699421029347L;

	/** Sollen nur Aktionen oder Trigger und Aktionen erstellt werden */
	private final ModelElementActionRecord.ActionMode actionMode;
	/** Zugehörige Tabelle (um diese Anweisen zu können, sich zu aktualisieren) */
	private final JTableExt table;
	/** Haupt-Zeichenfläche (für Expression-Builder) */
	private final ModelSurface mainSurface;
	/** Vollständiges Modell (für Expression-Builder) */
	private  final EditModel model;
	/** Hilfe-Callback */
	private final Runnable help;
	/** Originaldatensatz dem im Konstruktor übergeben wird und in den durch {@link #storeData()} die veränderten Daten zurückgeschrieben werden */
	private final List<ModelElementActionRecord> recordsOriginal;
	/** Arbeitskopie von {@link #recordsOriginal} zur Anzeige in der Tabelle und zur Veränderung */
	private final List<ModelElementActionRecord> records;
	/** Nur-Lese-Status */
	private final boolean readOnly;

	/**
	 * Konstruktor der Klasse
	 * @param table	Zugehörige Tabelle (um diese Anweisen zu können, sich zu aktualisieren)
	 * @param records	Liste der anzuzeigenden und ggf. zu bearbeitenden Datensätze
	 * @param actionMode	Sollen nur Aktionen oder Trigger und Aktionen erstellt werden
	 * @param surface	Haupt-Zeichenfläche (für Expression-Builder)
	 * @param model	Vollständiges Modell (für Expression-Builder)
	 * @param readOnly	Nur-Lese-Status
	 * @param help	Hilfe-Callback
	 * @see ModelElementActionRecord.ActionMode
	 */
	public ModelElementActionRecordTableModel(final JTableExt table, final List<ModelElementActionRecord> records, final ModelElementActionRecord.ActionMode actionMode, final ModelSurface surface, final EditModel model, final boolean readOnly, final Runnable help) {
		super();

		this.actionMode=actionMode;
		this.table=table;
		this.help=help;
		this.mainSurface=(surface.getParentSurface()==null)?surface:surface.getParentSurface();
		this.model=model;
		this.recordsOriginal=records;
		this.records=new ArrayList<>();
		for (ModelElementActionRecord record: records) this.records.add(new ModelElementActionRecord(record));
		this.readOnly=readOnly;

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
		return records.size()+1;
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	/**
	 * Liefert den Beschreibungstext für die erste Spalte für einen Aktionsdatensatz
	 * @param record	Aktionsdatensatz
	 * @return	Beschreibungstext für die erste Spalte der Tabelle
	 * @see #getValueAt(int, int)
	 */
	private String getActionText(final ModelElementActionRecord record) {
		final StringBuilder result=new StringBuilder();

		result.append("<html><body>");

		if (!record.isActive()) {
			result.append("<span style=\"color: red\"><b>"+Language.tr("Surface.Action.Dialog.Info.IsDeactivated")+"</b></span>");
			result.append("<br>");
		}

		if (actionMode==ModelElementActionRecord.ActionMode.TRIGGER_AND_ACTION) {
			switch (record.getConditionType()) {
			case CONDITION_TIME:
				result.append(Language.tr("Surface.Action.Dialog.Info.Time")+": ");
				if (record.getTimeRepeatCount()>0) {
					if (record.getTimeRepeatCount()==1) {
						result.append("<b>"+TimeTools.formatExactLongTime(record.getTimeInitial())+"</b>");
					} else {
						result.append("<b>"+TimeTools.formatExactLongTime(record.getTimeInitial())+"</b> +n*<b>"+TimeTools.formatExactLongTime(record.getTimeRepeat())+"</b> ("+record.getTimeRepeatCount()+" "+Language.tr("Surface.Action.Dialog.Info.Time.Repeat")+")");
					}
				} else {
					result.append("<b>"+TimeTools.formatExactLongTime(record.getTimeInitial())+"</b> +n*<b>"+TimeTools.formatExactLongTime(record.getTimeRepeat())+"</b>");
				}
				break;
			case CONDITION_CONDITION:
				result.append(Language.tr("Surface.Action.Dialog.Info.Condition")+": ");
				result.append("<b>"+record.getCondition()+"</b> ");
				result.append("("+Language.tr("Surface.Action.Dialog.Info.Condition.MinDistance")+": "+NumberTools.formatNumber(record.getConditionMinDistance())+" "+Language.tr("Surface.Action.Dialog.Info.Condition.MinDistance.Seconds")+")");
				break;
			case CONDITION_THRESHOLD:
				result.append(Language.tr("Surface.Action.Dialog.Info.ThresholdCondition")+": ");
				result.append("<b>"+record.getThresholdExpression()+"</b> ");
				switch (record.getThresholdDirection()) {
				case THRESHOLD_DOWN:
					result.append(" ("+String.format(Language.tr("Surface.Action.Dialog.Info.ThresholdCondition.Down"),NumberTools.formatNumber(record.getThresholdValue()))+")");
					break;
				case THRESHOLD_UP:
					result.append(" ("+String.format(Language.tr("Surface.Action.Dialog.Info.ThresholdCondition.Up"),NumberTools.formatNumber(record.getThresholdValue()))+")");
					break;
				}
				break;
			case CONDITION_SIGNAL:
				result.append(Language.tr("Surface.Action.Dialog.Info.Signal")+": ");
				result.append("<b>"+record.getConditionSignal()+"</b> ");
				break;
			}
			result.append("<br>");
		}

		switch (record.getActionType()) {
		case ACTION_ANALOG_VALUE:
			result.append(String.format(Language.tr("Surface.Action.Dialog.Info.ActionAnalog"),record.getAnalogID(),record.getAnalogValue()));
			break;
		case ACTION_ASSIGN:
			result.append(String.format(Language.tr("Surface.Action.Dialog.Info.ActionAssign"),record.getAssignVariable(),record.getAssignExpression()));
			break;
		case ACTION_SCRIPT:
			switch (record.getScriptMode()) {
			case Javascript: result.append(Language.tr("Surface.Action.Dialog.Info.ActionJS")); break;
			case Java: result.append(Language.tr("Surface.Action.Dialog.Info.ActionJava")); break;
			default: result.append(Language.tr("Surface.Action.Dialog.Info.ActionJS")); break;
			}
			break;
		case ACTION_SIGNAL:
			result.append(String.format(Language.tr("Surface.Action.Dialog.Info.ActionSignal"),record.getSignalName()));
			break;
		case ACTION_STOP:
			result.append(Language.tr("Surface.Action.Dialog.Info.EndSimulation"));
			break;
		case ACTION_SOUND:
			result.append(String.format(Language.tr("Surface.Action.Dialog.Info.PlaySound"),record.getSound()));
			break;
		}

		result.append("</body></html>");

		return result.toString();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex==records.size()) {
			if (columnIndex==1) return makeButtonPanel(new String[]{Language.tr("Surface.Action.Dialog.Add")},new Icon[]{Images.EDIT_ADD.getIcon()},new ActionListener[]{new EditButtonListener(0,Command.CMD_ADD)});
			return "";
		}

		final ModelElementActionRecord record=records.get(rowIndex);

		if (columnIndex==0) {
			/* Action anzeigen */
			return getActionText(record);
		} else {
			/* Steuerbuttons anzeigen */
			final List<String> tooltip=new ArrayList<>();
			final List<Icon> icons=new ArrayList<>();
			final List<ActionListener> listener=new ArrayList<>();

			if (record.isActive()) {
				tooltip.add(Language.tr("Surface.Action.Dialog.Disable"));
				icons.add(Images.GENERAL_OFF.getIcon());
				listener.add(new EditButtonListener(rowIndex,Command.CMD_DISABLE));
			} else {
				tooltip.add(Language.tr("Surface.Action.Dialog.Enable"));
				icons.add(Images.GENERAL_ON.getIcon());
				listener.add(new EditButtonListener(rowIndex,Command.CMD_ENABLE));
			}

			tooltip.add(Language.tr("Surface.Action.Dialog.Edit"));
			icons.add(Images.GENERAL_SETUP.getIcon());
			listener.add(new EditButtonListener(rowIndex,Command.CMD_EDIT));

			if (rowIndex>0) {
				tooltip.add(Language.tr("Surface.Action.Dialog.MoveUp"));
				icons.add(Images.ARROW_UP.getIcon());
				listener.add(new EditButtonListener(rowIndex,Command.CMD_MOVE_UP));
			}

			if (rowIndex<records.size()-1) {
				tooltip.add(Language.tr("Surface.Action.Dialog.MoveDown"));
				icons.add(Images.ARROW_DOWN.getIcon());
				listener.add(new EditButtonListener(rowIndex,Command.CMD_MOVE_DOWN));
			}

			tooltip.add(Language.tr("Surface.Action.Dialog.Delete"));
			icons.add(Images.EDIT_DELETE.getIcon());
			listener.add(new EditButtonListener(rowIndex,Command.CMD_DELETE));

			return makeButtonPanel(null,tooltip.toArray(new String[0]),icons.toArray(new Icon[0]),listener.toArray(new ActionListener[0]));
		}
	}

	@Override
	public final boolean isCellEditable(int rowIndex, int columnIndex) {
		return !readOnly && (columnIndex==1);
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0: return Language.tr("Surface.Action.Dialog.Columns.Actions");
		case 1: return Language.tr("Surface.Action.Dialog.Columns.Edit");
		default: return super.getColumnName(column);
		}
	}

	/**
	 * Aktualisiert die im Konstruktor übergebene Liste mit Datensätzen
	 * gemäß den Einträgen in der Tabelle.
	 * @see #ModelElementActionRecordTableModel(JTableExt, List, ui.modeleditor.elements.ModelElementActionRecord.ActionMode, ModelSurface, EditModel, boolean, Runnable)
	 */
	public void storeData() {
		recordsOriginal.clear();
		recordsOriginal.addAll(records);
	}

	/**
	 * Befehle für die Schaltflächen
	 * @see EditButtonListener
	 */
	private enum Command {
		/** Eintrag aktivieren */
		CMD_ENABLE,
		/** Eintrag Deaktivieren */
		CMD_DISABLE,
		/** Eintrag hinzufügen */
		CMD_ADD,
		/** Eintrag bearbeiten */
		CMD_EDIT,
		/** Eintrag löschen */
		CMD_DELETE,
		/** Eintrag in der Liste nach oben verschieben */
		CMD_MOVE_UP,
		/** Eintrag in der Liste nach unten verschieben */
		CMD_MOVE_DOWN
	}

	/**
	 * Reagiert auf Klicks auf die verschiedenen Schaltflächen
	 */
	private class EditButtonListener implements ActionListener {
		/** Zeilennummer */
		private final int row;
		/** Auszuführender Befehl */
		private final Command command;

		/**
		 * Konstruktor der Klasse
		 * @param row	Zeilennummer
		 * @param command	Auszuführender Befehl
		 */
		public EditButtonListener(final int row, final Command command) {
			this.row=row;
			this.command=command;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (readOnly) return;

			ModelElementActionRecord record;
			ModelElementActionRecordTableModelDialog dialog;

			switch (command) {
			case CMD_ENABLE:
				records.get(row).setActive(true);
				updateTable();
				break;
			case CMD_DISABLE:
				records.get(row).setActive(false);
				updateTable();
				break;
			case CMD_ADD:
				updateTable();
				record=new ModelElementActionRecord(actionMode);
				dialog=new ModelElementActionRecordTableModelDialog(table,record,mainSurface,model,help);
				if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
					records.add(record);
					updateTable();
				}
				break;
			case CMD_EDIT:
				if (row>=0) {
					updateTable();
					record=records.get(row);
					dialog=new ModelElementActionRecordTableModelDialog(table,record,mainSurface,model,help);
					if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) updateTable();
				}
				break;
			case CMD_DELETE:
				if (row>=0) {
					updateTable();
					if ((e.getModifiers() & ActionEvent.SHIFT_MASK)!=0 || MsgBox.confirm(table,Language.tr("Surface.Action.Dialog.Delete.ConfirmTitle"),Language.tr("Surface.Action.Dialog.Delete.ConfirmInfo"),Language.tr("Surface.Action.Dialog.Delete.ConfirmInfoYes"),Language.tr("Surface.Action.Dialog.Delete.ConfirmInfoNo"))) {
						records.remove(row);
						updateTable();
					}
				}
				break;
			case CMD_MOVE_UP:
				updateTable();
				if (row>0) {
					record=records.get(row);
					records.set(row,records.get(row-1));
					records.set(row-1,record);
					updateTable();
				}
				break;
			case CMD_MOVE_DOWN:
				updateTable();
				if (row<records.size()-1) {
					record=records.get(row);
					records.set(row,records.get(row+1));
					records.set(row+1,record);
					updateTable();
				}
				break;
			}
		}
	}
}

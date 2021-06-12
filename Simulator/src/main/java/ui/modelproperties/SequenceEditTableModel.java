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

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellEditor;

import language.Language;
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.JTableExt;
import tools.JTableExtAbstractTableModel;
import ui.images.Images;
import ui.modeleditor.ModelSequenceStep;

/**
 * Diese Klasse stellt ein Tabellenmodell für eine Fertigungsplan-Tabelle dar.
 * @author Alexander Herzog
 * @see SequenceEditDialog
 */
public class SequenceEditTableModel extends JTableExtAbstractTableModel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 3931731505896370273L;

	/** Zugehörige Tabelle */
	private final JTableExt table;
	/** Liste der Fertigungsschritte (wird bei der Bearbeitung direkt geändert) */
	private final List<ModelSequenceStep> steps;
	/** Liste mit den verfügbaren Zielstationen */
	private final String[] destinations;
	/** Hilfe-Runnable */
	private final Runnable help;
	/** Editor-Model (für den Expression-Builder-Dialog) */
	private final EditModel model;

	/**
	 * Konstruktor der Klasse
	 * @param table	Zugehörige Tabelle
	 * @param steps	Liste der Fertigungsschritte (wird bei der Bearbeitung direkt geändert)
	 * @param destinations	Liste mit den verfügbaren Zielstationen
	 * @param help	Hilfe-Runnable
	 * @param model	Editor-Model (für den Expression-Builder-Dialog)
	 */
	public SequenceEditTableModel(final JTableExt table, final List<ModelSequenceStep> steps, final String[] destinations, final Runnable help, final EditModel model) {
		super();
		this.table=table;
		this.steps=steps;
		this.destinations=destinations;
		this.help=help;
		this.model=model;
	}

	/**
	 * Aktualisiert die Tabelle, nach dem Änderungen an den Einstellungen vorgenommen wurden.
	 */
	public void updateTable() {
		fireTableDataChanged();
		TableCellEditor cellEditor=table.getCellEditor();
		if (cellEditor!=null) cellEditor.stopCellEditing();
	}

	@Override
	public final boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex==1 || columnIndex==2 || columnIndex==4;
	}

	@Override
	public int getRowCount() {
		return steps.size()+1;
	}

	@Override
	public int getColumnCount() {
		return 5;
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0: return Language.tr("Editor.Dialog.Sequences.Edit.Column.Nr");
		case 1: return Language.tr("Editor.Dialog.Sequences.Edit.Column.Destination");
		case 2: return Language.tr("Editor.Dialog.Sequences.Edit.Column.Next");
		case 3: return Language.tr("Editor.Dialog.Sequences.Edit.Column.Assignments");
		case 4: return Language.tr("Editor.Dialog.Sequences.Edit.Column.Control");
		default: return "";
		}
	}

	/**
	 * Erstellt eine neue Schaltfläche
	 * @param title	Beschriftung der Schaltfläche
	 * @param hint	Tooltip für die Schaltfläche
	 * @param icon	Icon für die Schaltfläche
	 * @param command	Beim Anklicken der Schaltfläche auszuführender Befehl
	 * @return	Neu erstellte Schaltfläche
	 */
	private JButton getButton(final String title, final String hint, final Icon icon, final Runnable command) {
		final JButton button=new JButton(title);
		button.setToolTipText(hint);
		button.addActionListener(e->command.run());
		button.setIcon(icon);
		button.setOpaque(false);
		return button;
	}

	/**
	 * Liefert einen Eintrag für die letzte Zeile
	 * @param columnIndex	Spaltenindex
	 * @return	Eintrag für eine Zelle
	 * @see #getValueAt(int, int)
	 */
	private Object getValueAtLastRow(int columnIndex) {
		if (columnIndex!=1 && columnIndex!=2) return "";

		final JPanel panel=new JPanel(new BorderLayout());
		panel.setOpaque(false);

		if (columnIndex==1) {
			final JToolBar toolbar=new JToolBar(SwingConstants.HORIZONTAL);
			toolbar.setFloatable(false);
			toolbar.setOpaque(false);
			panel.add(toolbar,BorderLayout.CENTER);
			toolbar.add(getButton(Language.tr("Editor.Dialog.Sequences.Edit.Add"),Language.tr("Editor.Dialog.Sequences.Edit.Add.Hint"),Images.EDIT_ADD.getIcon(),()->commandAdd()));
		}

		return panel;
	}

	/**
	 * Liefert die Ziel-Dropdownbox für einen Fertigungsschritt
	 * @param index	Index des Fertigungsschritts
	 * @return	Ziel-Dropdownbox
	 */
	private JComboBox<String> getDestinationCombo(final int index) {
		final JComboBox<String> combo=new JComboBox<>(destinations);

		int selIndex=-1;
		for (int i=0;i<destinations.length;i++) if (steps.get(index).getTarget().equals(destinations[i])) {selIndex=i; break;}
		if (selIndex<0 && destinations.length>0) {
			selIndex=0;
			steps.get(index).setTarget(destinations[0]);
		}
		combo.setSelectedIndex(selIndex);

		combo.addActionListener(e->{steps.get(index).setTarget((String)combo.getSelectedItem());});

		return combo;
	}

	/**
	 * Liefert die Dropdownbox zur Auswahl des nächsten Fertigungsschritts nach einem bestimmten Schritt
	 * @param index	Index des Ausgangsfertigungsschritts
	 * @return	Dropdownbox für den nächsten Fertigungsschritt
	 */

	private JComboBox<String> getNextCombo(final int index) {
		final List<String> next=new ArrayList<>();
		next.add(Language.tr("Editor.Dialog.Sequences.Edit.Column.Next.Default"));
		for (int i=0;i<steps.size();i++) next.add(String.format(Language.tr("Editor.Dialog.Sequences.Edit.Column.Next.Step"),i+1));
		final JComboBox<String> combo=new JComboBox<>(next.toArray(new String[0]));

		int selIndex=Math.max(0,steps.get(index).getNext()+1);
		if (selIndex>=next.size()) {
			selIndex=0;
			steps.get(index).setNext(-1);
		}
		combo.setSelectedIndex(selIndex);

		combo.addActionListener(e->{steps.get(index).setNext(combo.getSelectedIndex()-1);});

		return combo;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex==steps.size()) return getValueAtLastRow(columnIndex);

		switch (columnIndex) {
		case 0:
			return ""+(rowIndex+1);
		case 1:
			return getDestinationCombo(rowIndex);
		case 2:
			return getNextCombo(rowIndex);
		case 3:
			final int assignmentsCount=steps.get(rowIndex).getAssignments().size();
			if (assignmentsCount==1) return Language.tr("Editor.Dialog.Sequences.AssignmentsInfo.One"); else return String.format(Language.tr("Editor.Dialog.Sequences.AssignmentsInfo"),assignmentsCount);
		case 4:
			final List<String> title=new ArrayList<>();
			final List<Icon> icons=new ArrayList<>();
			final List<ActionListener> listener=new ArrayList<>();
			title.add(Language.tr("Editor.Dialog.Sequences.Edit.EditHint"));
			icons.add(Images.GENERAL_SETUP.getIcon());
			listener.add(e->commandEdit(rowIndex));
			title.add(Language.tr("Editor.Dialog.Sequences.Edit.DeleteHint"));
			icons.add(Images.EDIT_DELETE.getIcon());
			listener.add(e->commandDelete(rowIndex));
			if (rowIndex>0) {
				title.add(Language.tr("Editor.Dialog.Sequences.Edit.MoveUpHint"));
				icons.add(Images.ARROW_UP.getIcon());
				listener.add(e->commandMoveUp(rowIndex));
			}
			if (rowIndex<steps.size()-1) {
				title.add(Language.tr("Editor.Dialog.Sequences.Edit.MoveDownHint"));
				icons.add(Images.ARROW_DOWN.getIcon());
				listener.add(e->commandMoveDown(rowIndex));
			}
			return makeButtonPanel(null,title.toArray(new String[0]),icons.toArray(new Icon[0]),listener.toArray(new ActionListener[0]));
		default:
			return null;
		}
	}

	/**
	 * Befehl: Hinzufügen
	 */
	private void commandAdd() {
		steps.add(new ModelSequenceStep());
		updateTable();
	}

	/**
	 * Befehl: Bearbeiten
	 * @param index	Index des ausgewählten Listeneintrags
	 */
	private void commandEdit(final int index) {
		final SequenceEditAssignmentsDialog dialog=new SequenceEditAssignmentsDialog(table,steps.get(index).getAssignments(),help,model);
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) updateTable();
	}

	/**
	 * Befehl: Löschen
	 * @param index	Index des ausgewählten Listeneintrags
	 */
	private void commandDelete(final int index) {
		if (!MsgBox.confirm(table,Language.tr("Editor.Dialog.Sequences.Edit.DeleteConfirm.Title"),String.format(Language.tr("Editor.Dialog.Sequences.Edit.DeleteConfirm.Info"),index+1),Language.tr("Editor.Dialog.Sequences.Edit.DeleteConfirm.InfoYes"),Language.tr("Editor.Dialog.Sequences.Edit.DeleteConfirm.InfoNo"))) return;
		steps.remove(index);
		updateTable();
	}

	/**
	 * Befehl: Eintrag nach oben verschieben
	 * @param index	Index des ausgewählten Listeneintrags
	 */
	private void commandMoveUp(final int index) {
		final ModelSequenceStep temp=steps.get(index);
		steps.set(index,steps.get(index-1));
		steps.set(index-1,temp);
		updateTable();
	}

	/**
	 * Befehl: Eintrag nach unten verschieben
	 * @param index	Index des ausgewählten Listeneintrags
	 */
	private void commandMoveDown(final int index) {
		final ModelSequenceStep temp=steps.get(index);
		steps.set(index,steps.get(index+1));
		steps.set(index+1,temp);
		updateTable();
	}
}
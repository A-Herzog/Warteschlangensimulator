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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.swing.JScrollPane;
import javax.swing.table.TableCellEditor;

import language.Language;
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.JTableExt;
import tools.JTableExtAbstractTableModel;
import ui.expressionbuilder.ExpressionBuilder;
import ui.images.Images;
import ui.modeleditor.coreelements.ModelElement;

/**
 * Tabellenmodell zum Bearbeiten von Variablenzuweisungen
 * @author Alexander Herzog
 * @see ModelElementSetRecord
 */
public class VariablesTableModel extends JTableExtAbstractTableModel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 2614695330555791582L;

	/** Zugehörige Tabelle (um das Update der Tabelle veranlassen zu können, wenn sich die Daten verändert haben) */
	private final JTableExt table;
	private final EditModel model; /* Entweder model oder record sind null. */
	/** Zu bearbeitender Zuweisungsdatensatz */
	private final ModelElementSetRecord record; /* Entweder model oder record sind null. */
	/** Modell-Element über das Modell und Zeichenfläche abgefragt werden (für den Expression-Builder) */
	private final ModelElement element; /* Ist genau dann !=null, wenn record!=null ist. */
	/** Nur-Lese-Status */
	private final boolean readOnly;
	/** Hilfe-Callback */
	private final Runnable helpRunnable;

	private final List<String> variables;
	private final Map<String,String> initialVariableValues;
	private final List<String> expressions;
	private final Map<Integer,String> stationIDs;
	private final Map<Integer,String> stationNameIDs;

	/**
	 * Konstruktor der Klasse
	 * @param table	Zugehörige Tabelle (um das Update der Tabelle veranlassen zu können, wenn sich die Daten verändert haben)
	 * @param record	Zu bearbeitender Zuweisungsdatensatz
	 * @param element	Modell-Element über das Modell und Zeichenfläche abgefragt werden (für den Expression-Builder)
	 * @param readOnly	Nur-Lese-Status
	 * @param helpRunnable	Hilfe-Callback
	 */
	public VariablesTableModel(final JTableExt table, final ModelElementSetRecord record, final ModelElement element, final boolean readOnly, final Runnable helpRunnable) {
		super();
		this.table=table;
		this.model=null;
		this.record=record;
		this.element=element;
		this.stationIDs=ExpressionBuilder.getStationIDs(element.getSurface());
		this.stationNameIDs=ExpressionBuilder.getStationNameIDs(element.getSurface());
		this.readOnly=readOnly;
		this.helpRunnable=helpRunnable;

		variables=new ArrayList<>(Arrays.asList(record.getVariables()));
		initialVariableValues=element.getModel().getInitialVariablesWithValues();
		expressions=new ArrayList<>(Arrays.asList(record.getExpressions()));
		updateTable();
	}

	private VariablesTableModel(final JTableExt table, final EditModel model, final boolean readOnly, final Runnable helpRunnable) {
		super();
		this.table=table;
		this.model=model;
		this.record=null;
		this.element=null;
		this.stationIDs=ExpressionBuilder.getStationIDs(model.surface);
		this.stationNameIDs=ExpressionBuilder.getStationNameIDs(model.surface);
		this.readOnly=readOnly;
		this.helpRunnable=helpRunnable;

		variables=new ArrayList<>(model.globalVariablesNames);
		initialVariableValues=model.getInitialVariablesWithValues();
		expressions=new ArrayList<>(model.globalVariablesExpressions);
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
		return variables.size()+1;
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	private String expressionDisplayName(final String expression) {
		if (expression.equals(ModelElementSetRecord.SPECIAL_WAITING)) return Language.tr("Surface.Set.Table.Special.WaitingTime");
		if (expression.equals(ModelElementSetRecord.SPECIAL_TRANSFER)) return Language.tr("Surface.Set.Table.Special.TransferTime");
		if (expression.equals(ModelElementSetRecord.SPECIAL_PROCESS)) return Language.tr("Surface.Set.Table.Special.ProcessingTime");
		if (expression.equals(ModelElementSetRecord.SPECIAL_RESIDENCE)) return Language.tr("Surface.Set.Table.Special.ResidenceTime");
		return expression;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex==variables.size()) {
			switch (columnIndex) {
			case 0: return "";
			case 1:	return makeButtonPanel(new String[]{Language.tr("Surface.Set.Table.Add")},new URL[]{Images.EDIT_ADD.getURL()},new ActionListener[]{new AddButtonListener()});
			}
		}

		switch (columnIndex) {
		case 0:
			return makeEditPanelSmallBorder(
					variables.get(rowIndex)+" :=",
					new URL[]{Images.GENERAL_SETUP.getURL(),Images.EDIT_DELETE.getURL()},
					new String[]{
							Language.tr("Surface.Set.Table.Edit"),
							Language.tr("Surface.Set.Table.Delete")
					},
					new ActionListener[]{new EditButtonListener(0,rowIndex),new DeleteButtonListener(rowIndex)});

		case 1:
			if (rowIndex==0 && rowIndex==variables.size()-1) {
				/* kein hoch oder runter */
				return makeEditPanelSmallBorder(
						expressionDisplayName(expressions.get(rowIndex)),
						new URL[]{Images.GENERAL_SETUP.getURL()},
						new String[]{Language.tr("Surface.Set.Table.Edit")},
						new ActionListener[]{new EditButtonListener(0,rowIndex)});
			}
			if (rowIndex==0) {
				/* kein Nach-Oben-Button */
				return makeEditPanelSmallBorder(
						expressionDisplayName(expressions.get(rowIndex)),
						new URL[]{Images.ARROW_DOWN.getURL(),Images.GENERAL_SETUP.getURL()},
						new String[]{
								Language.tr("Surface.Set.Table.Down"),
								Language.tr("Surface.Set.Table.Edit")
						},
						new ActionListener[]{new EditButtonListener(2,rowIndex),new EditButtonListener(0,rowIndex)});
			}
			if (rowIndex==variables.size()-1) {
				/* kein Nach-Unten-Button */
				return makeEditPanelSmallBorder(
						expressionDisplayName(expressions.get(rowIndex)),
						new URL[]{Images.ARROW_UP.getURL(),Images.GENERAL_SETUP.getURL()},
						new String[]{
								Language.tr("Surface.Set.Table.Up"),
								Language.tr("Surface.Set.Table.Edit")
						},
						new ActionListener[]{new EditButtonListener(1,rowIndex),new EditButtonListener(0,rowIndex)});
			}
			return makeEditPanelSmallBorder(
					expressionDisplayName(expressions.get(rowIndex)),
					new URL[]{Images.ARROW_UP.getURL(),Images.ARROW_DOWN.getURL(),Images.GENERAL_SETUP.getURL()},
					new String[]{
							Language.tr("Surface.Set.Table.Up"),
							Language.tr("Surface.Set.Table.Down"),
							Language.tr("Surface.Set.Table.Edit")
					},
					new ActionListener[]{new EditButtonListener(1,rowIndex),new EditButtonListener(2,rowIndex),new EditButtonListener(0,rowIndex)});

		default: return null;
		}
	}

	@Override
	public final boolean isCellEditable(int rowIndex, int columnIndex) {
		return !readOnly;
	}

	private class AddButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (readOnly) return;
			VariablesTableModelDialog dialog=null;
			if (element!=null) {
				dialog=new VariablesTableModelDialog(table,helpRunnable,"","",getVariableNames(false,variables.toArray(new String[0])),initialVariableValues,stationIDs,stationNameIDs,true);
			}
			if (model!=null) {
				dialog=new VariablesTableModelDialog(table,helpRunnable,"","",model.surface.getVariableNames(variables.toArray(new String[0])),initialVariableValues,stationIDs,stationNameIDs,false);
			}
			if (dialog==null) return;
			dialog.setVisible(true);
			if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
				variables.add(dialog.getVariable());
				expressions.add(dialog.getExpression());
				updateTable();
			}
		}
	}

	private class EditButtonListener implements ActionListener {
		private final int col;
		/** Zeilennummer */
		private final int row;

		public EditButtonListener(final int col, final int row) {
			this.col=col;
			this.row=row;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (readOnly) return;
			String s;
			switch (col) {
			case 0:
				VariablesTableModelDialog dialog=null;
				if (element!=null) {
					dialog=new VariablesTableModelDialog(table,helpRunnable,variables.get(row),expressions.get(row),getVariableNames(false,variables.toArray(new String[0])),initialVariableValues,stationIDs,stationNameIDs,true);
				}
				if (model!=null) {
					dialog=new VariablesTableModelDialog(table,helpRunnable,variables.get(row),expressions.get(row),model.surface.getVariableNames(variables.toArray(new String[0])),initialVariableValues,stationIDs,stationNameIDs,false);
				}
				if (dialog==null) return;
				dialog.setVisible(true);
				if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
					variables.set(row,dialog.getVariable());
					expressions.set(row,dialog.getExpression());
					updateTable();
				}
				break;
			case 1:
				if (row>0) {
					s=variables.get(row); variables.set(row,variables.get(row-1)); variables.set(row-1,s);
					s=expressions.get(row); expressions.set(row,expressions.get(row-1)); expressions.set(row-1,s);
					updateTable();
				}
				break;
			case 2:
				if (row<variables.size()-1) {
					s=variables.get(row); variables.set(row,variables.get(row+1)); variables.set(row+1,s);
					s=expressions.get(row); expressions.set(row,expressions.get(row+1)); expressions.set(row+1,s);
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

			final String name=variables.get(row);
			if (!MsgBox.confirm(table,Language.tr("Surface.Set.Table.Delete.Error.Title"),String.format(Language.tr("Surface.Set.Table.Delete.Error.Info"),name),Language.tr("Surface.Set.Table.Delete.Error.YesInfo"),Language.tr("Surface.Set.Table.Delete.Error.NoInfo"))) return;
			variables.remove(row);
			expressions.remove(row);
			updateTable();
		}
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0: return Language.tr("Surface.Set.Table.Heading.Variable");
		case 1: return Language.tr("Surface.Set.Table.Heading.Expression");
		default: return super.getColumnName(column);
		}
	}

	/**
	 * Schreibt die Daten in das im Konstruktor angegebene
	 * {@link ModelElementSetRecord}-Objekt oder in das Modell
	 * (bei Verwendung über den Modelleigenschaftendialog) zurück.
	 */
	public void storeData() {
		if (readOnly) return;

		if (record!=null) {
			record.setData(variables.toArray(new String[0]),expressions.toArray(new String[0]));
		}

		if (model!=null) {
			model.globalVariablesNames.clear();
			model.globalVariablesExpressions.clear();
			model.globalVariablesNames.addAll(variables);
			model.globalVariablesExpressions.addAll(expressions);
		}
	}

	private String[] getVariableNames(final boolean includeOwnVariables, String[] addToList) {
		if (element==null || element.getModel()==null) return new String[0];

		List<String> variables=new ArrayList<>();

		for (ModelElement e1: element.getModel().surface.getElements()) {
			if (e1 instanceof ElementWithNewVariableNames) {
				if (!includeOwnVariables && e1==element) continue;
				for (String var: ((ElementWithNewVariableNames)e1).getVariables()) {
					boolean inList=false;
					for (String s: variables) if (s.equalsIgnoreCase(var)) {inList=true; break;}
					if (!inList) variables.add(var);
				}
			}
			if (element instanceof ModelElementSub) for (ModelElement e2: ((ModelElementSub)e1).getSubSurface().getElements()) {
				if (e2 instanceof ElementWithNewVariableNames) {
					if (!includeOwnVariables && e2==element) continue;
					for (String var: ((ElementWithNewVariableNames)e2).getVariables()) {
						boolean inList=false;
						for (String s: variables) if (s.equalsIgnoreCase(var)) {inList=true; break;}
						if (!inList) variables.add(var);
					}
				}
			}
		}

		if (addToList!=null) for (String var: addToList) {
			boolean inList=false;
			for (String s: variables) if (s.equalsIgnoreCase(var)) {inList=true; break;}
			if (!inList) variables.add(var);
		}

		return variables.toArray(new String[0]);
	}

	/**
	 * Erstellt Tabelle und Tabellenmodell zum Bearbeiten von Zuweisungen
	 * @param record	Zuordnungsdatensatz, der bearbeitet werden soll
	 * @param element	Element, aus dem die Daten kommen
	 * @param readOnly	Nur-Lese-Status
	 * @param helpRunnable	Hilfe-Runnable
	 * @return	2-elementiges Array aus <code>JScrollPane</code> (welches die Tabelle enthält) und dem Tabellenmodell
	 */
	public static Object[] buildTable(final ModelElementSetRecord record, final ModelElement element, final boolean readOnly, final Runnable helpRunnable) {
		final JTableExt table=new JTableExt();
		final VariablesTableModel varModel;

		table.setModel(varModel=new VariablesTableModel(table,record,element,readOnly,helpRunnable));

		table.getColumnModel().getColumn(0).setMaxWidth(175);
		table.getColumnModel().getColumn(0).setMinWidth(175);
		table.setIsPanelCellTable(0);
		table.setIsPanelCellTable(1);
		table.setEnabled(!readOnly);

		return new Object[]{new JScrollPane(table),varModel};
	}

	/**
	 * Erstellt Tabelle und Tabellenmodell zum Bearbeiten von Zuweisungen
	 * @param model	Modell, dessen Zuordnungsdaten bearbeitet werden sollen
	 * @param readOnly	Nur-Lese-Status
	 * @param helpRunnable	Hilfe-Runnable
	 * @return	2-elementiges Array aus <code>JScrollPane</code> (welches die Tabelle enthält) und dem Tabellenmodell
	 */
	public static Object[] buildTable(final EditModel model, final boolean readOnly, final Runnable helpRunnable) {
		final JTableExt table=new JTableExt();
		final VariablesTableModel varModel;

		table.setModel(varModel=new VariablesTableModel(table,model,readOnly,helpRunnable));

		table.getColumnModel().getColumn(0).setMaxWidth(175);
		table.getColumnModel().getColumn(0).setMinWidth(175);
		table.setIsPanelCellTable(0);
		table.setIsPanelCellTable(1);
		table.setEnabled(!readOnly);

		return new Object[]{new JScrollPane(table),varModel};
	}
}
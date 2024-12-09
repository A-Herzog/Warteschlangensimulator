/**
 * Copyright 2024 Alexander Herzog
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
package ui.dialogs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.table.TableCellEditor;

import language.Language;
import simulator.editmodel.EditModel;
import simulator.editmodel.GlobalVariable;
import simulator.runmodel.RunModel;
import simulator.simparser.ExpressionCalc;
import simulator.simparser.ExpressionCalcModelUserFunctions;
import systemtools.MsgBox;
import tools.JTableExt;
import tools.JTableExtAbstractTableModel;

/**
 * Tabellenmodell für die Tabelle zur Bearbeitung der initialen Werte
 * der globalen Variablen in {@link AskForVariableValuesDialog}
 * @see AskForVariableValuesDialog
 */
public class AskForVariableValuesTableModel extends JTableExtAbstractTableModel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=1661779989687834706L;

	/**
	 * Zugehörige Tabelle (um das Update der Tabelle veranlassen zu können, wenn sich die Daten verändert haben)
	 */
	private final JTableExt table;

	/**
	 * Editormodell dessen globale Variable angepasst werden sollen
	 */
	private final EditModel model;

	/**
	 * Liste der globalen Variablen, deren Werte aktualisiert werden sollen
	 */
	private final List<GlobalVariable> globalVariables;

	/**
	 * Liste der geänderten initialen Variablenwerte
	 */
	private final List<String> expressions;

	/**
	 * Konstruktor der Klasse
	 * @param table	Zugehörige Tabelle (um das Update der Tabelle veranlassen zu können, wenn sich die Daten verändert haben)
	 * @param model	Editormodell dessen globale Variable angepasst werden sollen
	 */
	public AskForVariableValuesTableModel(final JTableExt table, final EditModel model) {
		this.table=table;
		this.model=model;
		this.globalVariables=model.globalVariables.stream().filter(variable->variable.isAskForValueOnStart()).collect(Collectors.toList());
		this.expressions=new ArrayList<>(this.globalVariables.stream().map(variable->variable.getExpression()).collect(Collectors.toList()));
	}

	@Override
	public int getRowCount() {
		return globalVariables.size();
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case 0: return globalVariables.get(rowIndex).getName();
		case 1: return expressions.get(rowIndex);
		default: return "";
		}
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (columnIndex!=1) return;
		if (rowIndex<0 || rowIndex>=expressions.size()) return;
		expressions.set(rowIndex,aValue.toString());
	}

	@Override
	public final boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex==1;
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
	 * Prüft die neuen eingegebenen Rechenausdrücke für die Variablen
	 * @return	Liefert <code>true</code>, wenn alle Rechenausdrücke korrekt sind
	 */
	public boolean checkData() {
		final Object varList=RunModel.getAllVariableNames(model);
		if (varList instanceof String) {
			MsgBox.error(table,Language.tr("Editor.Dialog.Tab.InitialVariableValues.UpdateValues"),(String)varList);
			return false;
		}
		@SuppressWarnings("unchecked")
		final String[] variables=((List<String>)varList).toArray(String[]::new);

		final var modelUserFunctions=new ExpressionCalcModelUserFunctions(model.userFunctions);

		for (int i=0;i<globalVariables.size();i++) {
			final String name=globalVariables.get(i).getName();
			final String expression=expressions.get(i);
			final ExpressionCalc calc=new ExpressionCalc(variables,modelUserFunctions);
			final int error=calc.parse(expression);
			if (error>=0) {
				MsgBox.error(table,Language.tr("Editor.Dialog.Tab.InitialVariableValues.UpdateValues"),String.format(Language.tr("Simulation.Creator.InvalidInitialVariableExpression"),name,expression,error+1));
				return false;
			}
		}
		return true;
	}

	/**
	 * Schreibt die geänderten initialen Variablenwerte in die im
	 * Konstruktor übergebene Liste der globalen Variablen zurück.
	 */
	public void storeData() {
		final TableCellEditor cellEditor=table.getCellEditor();
		if (cellEditor!=null) cellEditor.stopCellEditing();
		for (int i=0;i<expressions.size();i++) globalVariables.get(i).setExpression(expressions.get(i));
	}
}

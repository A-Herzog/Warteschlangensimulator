/**
 * Copyright 2021 Alexander Herzog
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
import java.util.function.BiConsumer;
import java.util.function.Function;

import javax.swing.JOptionPane;
import javax.swing.table.TableCellEditor;

import language.Language;
import mathtools.NumberTools;
import mathtools.Table;
import systemtools.MsgBox;
import tools.JTableExt;
import tools.JTableExtAbstractTableModel;
import ui.images.Images;

/**
 * Datenmodell für die Darstellung der Simulations-Variablen
 * in {@link ExpressionCalculatorDialog}.
 * @author Alexander Herzog
 * @see ExpressionCalculatorDialog
 */
public class ExpressionCalculatorDialogVariablesTableModel extends JTableExtAbstractTableModel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-5030019793042901066L;

	/**
	 * Zugehörige Tabelle
	 */
	private final JTableExt table;

	/**
	 * Namen der globalen Variablen
	 */
	private final String[] variableNames;

	/**
	 * Callback zum Abrufen eines Variablenwertes
	 */
	private final Function<String,Double> getVariable;

	/**
	 * Callback zum Einstellen eines Variablenwertes
	 */
	private final BiConsumer<String,Double> setVariable;

	/**
	 * Konstruktor der Klasse
	 * @param table	Zugehörige Tabelle
	 * @param variableNames	Darzustellende und zu verändernde Zuordnung
	 * @param getVariable	Callback zum Abrufen eines Variablenwertes
	 * @param setVariable	Callback zum Einstellen eines Variablenwertes
	 */
	public ExpressionCalculatorDialogVariablesTableModel(final JTableExt table, final String[] variableNames, final Function<String,Double> getVariable, final BiConsumer<String,Double> setVariable) {
		this.table=table;
		this.variableNames=variableNames;
		this.getVariable=getVariable;
		this.setVariable=setVariable;
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
		return (columnIndex==1) && (setVariable!=null);
	}

	@Override
	public int getRowCount() {
		return variableNames.length;
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0: return Language.tr("ExpressionCalculator.Tab.Variables.Column.Name");
		case 1: return Language.tr("ExpressionCalculator.Tab.Variables.Column.Value");
		default: return "";
		}
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case 0:
			return variableNames[rowIndex];
		case 1:
			return makeEditPanelSmallBorder(NumberTools.formatNumberMax(getVariable.apply(variableNames[rowIndex])),Images.GENERAL_SETUP.getIcon(),"Wert bearbeiten",e->edit(variableNames[rowIndex]));
		default:
			return "";
		}
	}

	/**
	 * Zeigt einen Dialog zur Eingabe eines neuen Wertes für eine Variable an.
	 * @param name	Name der Variable
	 */
	private void edit(final String name) {
		final String result=JOptionPane.showInputDialog(table,String.format(Language.tr("ExpressionCalculator.Tab.Variables.Edit.Label"),name),NumberTools.formatNumberMax(getVariable.apply(name)));
		if (result==null) return;
		final Double D=NumberTools.getDouble(result);
		if (D==null) {
			MsgBox.error(table,Language.tr("ExpressionCalculator.Tab.Variables.Edit.Error.Title"),String.format(Language.tr("ExpressionCalculator.Tab.Variables.Edit.Error.Info"),result,name));
			return;
		}
		setVariable.accept(name,D);
		updateTable();
	}

	/**
	 * Liefert die angezeigten Tabellendaten als {@link Table}-Objekt.
	 * @return	{@link Table}-Objekt, welches die Daten zu den Variablen enthält
	 */
	public Table getTableData() {
		final Table table=new Table();
		table.addLine(new String[] {
				Language.tr("ExpressionCalculator.Tab.Variables.Column.Name"),
				Language.tr("ExpressionCalculator.Tab.Variables.Column.Value")
		});
		for (int i=0;i<variableNames.length;i++) table.addLine(new String[] {
				variableNames[i],
				NumberTools.formatNumberMax(getVariable.apply(variableNames[i]))
		});
		return table;
	}
}

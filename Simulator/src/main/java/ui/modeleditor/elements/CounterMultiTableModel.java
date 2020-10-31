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

import java.awt.event.ActionListener;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.table.TableCellEditor;

import language.Language;
import simulator.editmodel.EditModel;
import simulator.simparser.ExpressionMultiEval;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.JTableExt;
import tools.JTableExtAbstractTableModel;
import ui.expressionbuilder.ExpressionBuilder;
import ui.images.Images;
import ui.modeleditor.ModelSurface;

/**
 * Diese Klasse hält ein Tabellenmodell für die Verwendung
 * in {@link ModelElementCounterMultiDialog} vor.
 * @author Alexander Herzog
 * @see CounterMultiTableModel#buildTable(ModelElementCounterMulti, boolean)
 */
public final class CounterMultiTableModel extends JTableExtAbstractTableModel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 4363552821558486303L;

	/** Tabelle, in der das Modell verwendet werden soll */
	private final JTableExt table;
	/** Editormodell, aus dem die Daten entnommen und in die die Daten zurückgeschrieben werden sollen */
	private final ModelElementCounterMulti element;
	/** Nur-Lese-Status */
	private final boolean readOnly;
	/** Liste aller globalen Variablen in dem Modell */
	private String[] variables;

	/** Liste der Bedingungen */
	private List<String> condition;
	/** Liste der zu den Bedingungen gehörenden Zähler-Namen */
	private List<String> counter;
	/** Name des Zähler für "keine Bedingung trifft zu" */
	private String counterElse;

	/**
	 * Konstruktor der Klasse
	 * @param table	Tabelle, in der das Modell verwendet werden soll
	 * @param element	Editormodell, aus dem die Daten entnommen und in die die Daten zurückgeschrieben werden sollen
	 * @param readOnly	Nur-Lese-Status
	 */
	public CounterMultiTableModel(final JTableExt table, final ModelElementCounterMulti element, final boolean readOnly) {
		super();
		this.table=table;
		this.element=element;
		this.readOnly=readOnly;

		variables=element.getSurface().getMainSurfaceVariableNames(element.getModel().getModelVariableNames(),true);

		condition=new ArrayList<>();
		counter=new ArrayList<>();

		final int size=Math.min(element.getCounterNames().size(),element.getConditions().size()+1);
		for (int i=0;i<size-1;i++) {
			counter.add(element.getCounterNames().get(i));
			condition.add(element.getConditions().get(i));
		}
		if (size>0) counterElse=element.getCounterNames().get(size-1); else counterElse="";

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
		return counter.size()+1;
	}

	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0: return Language.tr("Surface.CounterMulti.Table.Heading.Condition");
		case 1: return Language.tr("Surface.CounterMulti.Table.Heading.CounterName");
		case 2: return "";
		default: return super.getColumnName(column);
		}
	}

	/**
	 * Liefert einen Zelleneintrag für die letzte Zeile.
	 * @param columnIndex	Spalte
	 * @return	Zelleneintrag für die letzte Zeile
	 */
	private Object getValueAtLastRow(int columnIndex) {
		if (columnIndex==0) {
			return Language.tr("Surface.CounterMulti.Table.Heading.Condition.Else");
		}

		if (columnIndex==1) {
			return counterElse;
		}

		if (columnIndex==2) {
			return makeButtonPanel(
					new String[] {""},
					new String[] {Language.tr("Surface.CounterMulti.Table.Heading.Condition.Add")},
					new  URL[] {Images.EDIT_ADD.getURL()},
					new ActionListener[] {e->addCounter()});
		}

		return null;
	}

	/**
	 * Liefert den Wert einer Zelle
	 * @param rowIndex	Zeilenindex
	 * @param columnIndex	Spaltenindex
	 * @param condition	Zähler-Bedingung für die Zeile
	 * @param counter	Zähler-Name für die Zeile
	 * @return	Wert der Zelle
	 */
	private Object getValueAt(int rowIndex, int columnIndex, final String condition, final String counter) {
		if (columnIndex==0) {
			return condition;
		}

		if (columnIndex==1) {
			return counter;
		}

		if (columnIndex==2) {
			final List<String> titles=new ArrayList<>();
			final List<String> tooltips=new ArrayList<>();
			final List<URL> icons=new ArrayList<>();
			final List<ActionListener> listeners=new ArrayList<>();

			titles.add("");
			tooltips.add(Language.tr("Surface.CounterMulti.Table.Heading.Condition.Edit"));
			icons.add(Images.EXPRESSION_BUILDER.getURL());
			listeners.add(e->expressionBuilder(rowIndex));

			titles.add("");
			tooltips.add(Language.tr("Surface.CounterMulti.Table.Heading.Condition.Delete"));
			icons.add(Images.EDIT_DELETE.getURL());
			listeners.add(e->deleteCounter(rowIndex));

			if (rowIndex>0) {
				titles.add("");
				tooltips.add(Language.tr("Surface.CounterMulti.Table.Heading.Condition.MoveUp"));
				icons.add(Images.ARROW_UP.getURL());
				listeners.add(e->moveUp(rowIndex));
			}

			if (rowIndex<this.counter.size()-1) {
				titles.add("");
				tooltips.add(Language.tr("Surface.CounterMulti.Table.Heading.Condition.MoveDown"));
				icons.add(Images.ARROW_DOWN.getURL());
				listeners.add(e->moveDown(rowIndex));
			}

			return makeButtonPanel(
					titles.toArray(new String[0]),
					tooltips.toArray(new String[0]),
					icons.toArray(new URL[0]),
					listeners.toArray(new ActionListener[0]));
		}

		return "";
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex==counter.size()) return getValueAtLastRow(columnIndex);
		return getValueAt(rowIndex,columnIndex,condition.get(rowIndex),counter.get(rowIndex));
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (!(aValue instanceof String)) return;
		final String text=(String)aValue;

		if (rowIndex==counter.size()) {
			if (columnIndex==1) {
				counterElse=text;
			}
			return;
		}

		if (columnIndex==0) {
			condition.set(rowIndex,text);
		}

		if (columnIndex==1) {
			counter.set(rowIndex,text);
		}
	}

	/**
	 * Erzeugt Tabelle und Tabellenmodell
	 * @param element	Editormodell, aus dem die Daten entnommen und in die die Daten zurückgeschrieben werden sollen
	 * @param readOnly	Nur-Lese-Status
	 * @return	Array aus zwei Elementen: JScrollPane das die Tabelle enthält und Tabellenmodell
	 */
	public static Object[] buildTable(final ModelElementCounterMulti element, final boolean readOnly) {
		final JTableExt table=new JTableExt();
		final CounterMultiTableModel model;

		table.setModel(model=new CounterMultiTableModel(table,element,readOnly));

		table.getColumnModel().getColumn(1).setMaxWidth(150);
		table.getColumnModel().getColumn(1).setMinWidth(150);
		table.getColumnModel().getColumn(2).setMaxWidth(100);
		table.getColumnModel().getColumn(2).setMinWidth(100);
		table.setIsPanelCellTable(2);
		table.setEnabled(!readOnly);

		return new Object[]{new JScrollPane(table),model};
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return !readOnly && (rowIndex<counter.size() || columnIndex>0);
	}

	/**
	 * Fügt eine Zeile zu der Tabelle hinzu
	 */
	private void addCounter() {
		condition.add("");
		counter.add(Language.tr("Surface.CounterMulti.Table.Heading.CounterName.Default"));
		updateTable();
	}

	/**
	 * Öffnet den Expression-Builder-Dialog für eine Zähler-Bedingung
	 * @param index	Index des Eintrags in {@link #condition} für die der Expression-Builder-Dialog aufgerufen werden soll
	 * @see #condition
	 */
	private void expressionBuilder(final int index) {
		final EditModel model=element.getModel();
		final ModelSurface surface=model.surface;
		final List<String> variables=new ArrayList<>(Arrays.asList(surface.getMainSurfaceVariableNames(model.getModelVariableNames(),true)));
		final ExpressionBuilder dialog=new ExpressionBuilder(table,condition.get(index),true,variables.toArray(new String[0]),model.getInitialVariablesWithValues(),ExpressionBuilder.getStationIDs(surface),ExpressionBuilder.getStationNameIDs(surface),true,false,false);

		dialog.setVisible(true);
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			condition.set(index,dialog.getExpression());
			updateTable();
		}
	}

	/**
	 * Löscht einen Eintrag in der Tabelle
	 * @param index	Zu löschende Zeile
	 */
	private void deleteCounter(final int index) {
		condition.remove(index);
		counter.remove(index);
		updateTable();
	}

	/**
	 * Verschiebt einen Eintrag um eine Position in der Liste nach oben
	 * @param index	Nach oben zu verschiebende Zeile
	 */
	private void moveUp(final int index) {
		String s;

		s=condition.get(index-1);
		condition.set(index-1,condition.get(index));
		condition.set(index,s);

		s=counter.get(index-1);
		counter.set(index-1,counter.get(index));
		counter.set(index,s);

		updateTable();
	}

	/**
	 * Verschiebt einen Eintrag um eine Position in der Liste nach unten
	 * @param index	Nach unten zu verschiebende Zeile
	 */
	private void moveDown(final int index) {
		String s;

		s=condition.get(index+1);
		condition.set(index+1,condition.get(index));
		condition.set(index,s);

		s=counter.get(index+1);
		counter.set(index+1,counter.get(index));
		counter.set(index,s);

		updateTable();
	}

	/**
	 * Prüft die Eingaben
	 * @param showErrorMessage	Fehlermeldung anzeigen?
	 * @return	Sind alle Eingaben ok?
	 */
	public boolean checkData(final boolean showErrorMessage) {
		boolean ok=true;

		for (int i=0;i<condition.size();i++) {
			final int error=ExpressionMultiEval.check(condition.get(i),variables);
			if (error>=0) {
				ok=false;
				if (showErrorMessage) {
					MsgBox.error(table,Language.tr("Surface.CounterMulti.Table.Heading.Condition.ErrorTitle"),String.format(Language.tr("Surface.CounterMulti.Table.Heading.Condition.ErrorInfo"),i+1,condition.get(i),error+1));
					return false;
				}
			}

			if (counter.get(i).trim().isEmpty()) {
				ok=false;
				if (showErrorMessage) {
					MsgBox.error(table,Language.tr("Surface.CounterMulti.Table.Heading.CounterName.ErrorTitle"),String.format(Language.tr("Surface.CounterMulti.Table.Heading.CounterName.ErrorInfo"),i+1));
					return false;
				}
			}
		}

		if (counterElse.trim().isEmpty()) {
			ok=false;
			if (showErrorMessage) {
				MsgBox.error(table,Language.tr("Surface.CounterMulti.Table.Heading.Condition.Else.ErrorTitle"),Language.tr("Surface.CounterMulti.Table.Heading.Condition.Else.ErrorInfo"));
				return false;
			}
		}

		return ok;
	}

	/**
	 * Schreibt die Eingaben in das im Konstruktor angegebene {@link ModelElementCounterMulti}-Objekt zurück.
	 */
	public void storeData() {
		if (readOnly) return;

		element.getConditions().clear();
		element.getCounterNames().clear();
		element.getConditions().addAll(condition);
		element.getCounterNames().addAll(counter);
		element.getCounterNames().add(counterElse);
	}
}
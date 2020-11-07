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
import java.util.Map;

import javax.swing.table.TableCellEditor;

import language.Language;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.JTableExt;
import tools.JTableExtAbstractTableModel;
import ui.expressionbuilder.ExpressionBuilder;
import ui.images.Images;

/**
 * Stellt ein Tabellenmodell zur Anzeige und Bearbeitung der Ausdrücke in einem
 * <code>ModelElementAnimationTextSelect</code>-Element zur Verfügung.
 * @author Alexander Herzog
 * @see ModelElementAnimationTextSelect
 */
public class ExpressionTableModelText extends JTableExtAbstractTableModel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 5403863802156664107L;

	/** Tabelle in der das Datenmodell zum Einsatz kommen soll */
	private final JTableExt table;
	/** Hilfe-Callback welches aufgerufen wird, wenn in einem der untergeordneten Dialoge auf die "Hilfe"-Schaltfläche geklickt wird. */
	private final Runnable help;
	/** Nur-Lese-Status */
	private final boolean readOnly;

	/**
	 * Liste der globalen Variablen (zum Prüfen von Ausdrücken)
	 */
	private final String[] variableNames;

	/**
	 * Liste mit allen globalen Variablen und ihren Startwerten
	 */
	private final Map<String,String> initialVariableValues;

	/**
	 * Zuordnung von Stations-IDs zu Stationsbeschreibungen
	 */
	private final Map<Integer,String> stationIDs;

	/**
	 * Zuordnung von Stations-IDs zu Stationsnamen
	 */
	private final Map<Integer,String> stationNameIDs;

	/**
	 * 	In der Tabelle anzuzeigende Formel-Bedingungen
	 */
	private final List<String> expressions=new ArrayList<>();

	/**
	 * In der Tabelle anzuzeigende Werte, die während der Animation angezeigt werden sollen, wenn die jeweilige Formel-Bedingung erfüllt ist
	 */
	private final List<String> values=new ArrayList<>();

	/**
	 * Konstruktor der Klasse <code>ExpressionTableModelText</code>
	 * @param table	Tabelle in der das Datenmodell zum Einsatz kommen soll
	 * @param element	Modell-Element aus dem die Daten geladen werden sollen
	 * @param readOnly	Gibt an, ob die Daten bearbeitet werden dürfen (<code>false</code>) oder nicht (<code>true</code>).
	 * @param help	Hilfe-Callback welches aufgerufen wird, wenn in einem der untergeordneten Dialoge auf die "Hilfe"-Schaltfläche geklickt wird.
	 */
	public ExpressionTableModelText(final JTableExt table, final ModelElementAnimationTextSelect element, final boolean readOnly, final Runnable help) {
		super();
		this.help=help;
		this.table=table;
		this.readOnly=readOnly;
		variableNames=element.getSurface().getMainSurfaceVariableNames(element.getModel().getModelVariableNames(),false);
		initialVariableValues=element.getModel().getInitialVariablesWithValues();
		stationIDs=ExpressionBuilder.getStationIDs(element.getSurface());
		stationNameIDs=ExpressionBuilder.getStationNameIDs(element.getSurface());

		expressions.addAll(element.getTextExpressions());
		values.addAll(element.getTextValues());
		while (expressions.size()>values.size()) expressions.remove(expressions.size()-1);
		while (values.size()>expressions.size()) values.remove(values.size()-1);

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
		return expressions.size()+1;
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex==expressions.size()) {
			if (columnIndex>0) return "";
			return makeButtonPanel(new String[]{Language.tr("Surface.ExpressionTableModelText.Add")},new URL[]{Images.EDIT_ADD.getURL()},new ActionListener[]{new EditButtonListener(0,-1)});
		}

		switch (columnIndex) {
		case 0:
			return makeEditPanelSmallBorder(
					Images.MODE_EXPRESSION.getURL(),
					expressions.get(rowIndex),
					new URL[]{Images.GENERAL_SETUP.getURL(),Images.EDIT_DELETE.getURL()},
					new String[]{Language.tr("Surface.ExpressionTableModelText.Edit"),Language.tr("Surface.ExpressionTableModelText.Delete")},
					new ActionListener[]{new EditButtonListener(0,rowIndex),new DeleteButtonListener(rowIndex)}
					);
		case 1:
			List<URL> icons=new ArrayList<>();
			List<String> hints=new ArrayList<>();
			List<ActionListener> actions=new ArrayList<>();
			icons.add(Images.GENERAL_SETUP.getURL());
			hints.add(Language.tr("Surface.ExpressionTableModelText.Edit"));
			actions.add(new EditButtonListener(1,rowIndex));
			if (rowIndex>0) {
				icons.add(Images.ARROW_UP.getURL());
				hints.add(Language.tr("Surface.ExpressionTableModelText.MoveUp"));
				actions.add(new EditButtonListener(3,rowIndex));
			}
			if (rowIndex<values.size()-1) {
				icons.add(Images.ARROW_DOWN.getURL());
				hints.add(Language.tr("Surface.ExpressionTableModelText.MoveDown"));
				actions.add(new EditButtonListener(4,rowIndex));
			}
			return makeEditPanelSmallBorder(
					values.get(rowIndex),
					icons.toArray(new URL[0]),
					hints.toArray(new String[0]),
					actions.toArray(new ActionListener[0])
					);
		default:
			return null;
		}
	}

	@Override
	public final boolean isCellEditable(int rowIndex, int columnIndex) {
		return !readOnly;
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0: return Language.tr("Surface.ExpressionTableModelText.ColumnExpression");
		case 1: return Language.tr("Surface.ExpressionTableModelText.ColumnText");
		default: return super.getColumnName(column);
		}
	}

	/**
	 * Schreibt die geänderten Datenreihen in das <code>ModelElementAnimationTextSelect</code>-Element zurück
	 * @param element	Objekt, in das die Daten zur Darstellung geschrieben werden sollen
	 */
	public void storeData(ModelElementAnimationTextSelect element) {
		element.getTextExpressions().clear();
		element.getTextExpressions().addAll(expressions);
		element.getTextValues().clear();
		element.getTextValues().addAll(values);
	}

	/**
	 * Reagiert auf Klicks auf die Bearbeiten und Verschieben-Schaltflächen
	 */
	private class EditButtonListener implements ActionListener {
		/** Auszuführender Befehl (0: Ausdruck bearbeiten oder hinzufügen, 1: Wert ändern, 2: Nach oben verschieben, 3: Nach unten verschieben) */
		private final int nr;
		/** Zeilennummer */
		private final int row;

		/**
		 * Konstruktor der Klasse
		 * @param nr	Auszuführender Befehl (0: Ausdruck bearbeiten oder hinzufügen, 1: Wert ändern, 2: Nach oben verschieben, 3: Nach unten verschieben)
		 * @param row	Zeilennummer
		 */
		public EditButtonListener(final int nr, final int row) {
			this.nr=nr;
			this.row=row;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (readOnly) return;

			String s,t;
			ExpressionTableModelTextDialog dialog;

			switch (nr) {
			case 0: /* Ausdruck bearbeiten (auch neuen Eintrag anlegen) */
				s=(row<0)?"":expressions.get(row);
				t=(row<0)?"":values.get(row);
				dialog=new ExpressionTableModelTextDialog(table,s,t,variableNames,initialVariableValues,stationIDs,stationNameIDs,help);
				if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
					if (row<0) {
						expressions.add(dialog.getExpression());
						values.add(dialog.getValue());
					} else {
						expressions.set(row,dialog.getExpression());
						values.set(row,dialog.getValue());
					}
					updateTable();
				}
				break;
			case 1: /* Wert */
				dialog=new ExpressionTableModelTextDialog(table,expressions.get(row),values.get(row),variableNames,initialVariableValues,stationIDs,stationNameIDs,help);
				if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
					if (row<0) {
						expressions.add(dialog.getExpression());
						values.add(dialog.getValue());
					} else {
						expressions.set(row,dialog.getExpression());
						values.set(row,dialog.getValue());
					}
					updateTable();
				}
				break;
			case 3: /* Nach oben */
				if (row>0) {
					s=expressions.get(row); expressions.set(row,expressions.get(row-1)); expressions.set(row-1,s);
					s=values.get(row); values.set(row,values.get(row-1)); values.set(row-1,s);
					updateTable();
				}
				break;
			case 4: /* Nach unten */
				if (row<expressions.size()-1) {
					s=expressions.get(row); expressions.set(row,expressions.get(row+1)); expressions.set(row+1,s);
					s=values.get(row); values.set(row,values.get(row+1)); values.set(row+1,s);
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

			final String name=expressions.get(row);
			if (!MsgBox.confirm(table,Language.tr("Surface.ExpressionTableModelText.Delete.Confirmation.Title"),String.format(Language.tr("Surface.ExpressionTableModelText.Delete.Confirmation.Info"),name),Language.tr("Surface.ExpressionTableModelText.Delete.Confirmation.YesInfo"),Language.tr("Surface.ExpressionTableModelText.Delete.Confirmation.NoInfo"))) return;
			expressions.remove(row);
			values.remove(row);
			updateTable();
		}
	}
}
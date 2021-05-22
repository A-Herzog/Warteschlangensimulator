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

import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.table.TableCellEditor;

import language.Language;
import mathtools.NumberTools;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.JTableExt;
import tools.JTableExtAbstractTableModel;
import ui.images.Images;

/**
 * Datenmodell für die Darstellung der Simulations-Scripting-Wertezuordnungen
 * in {@link ExpressionCalculatorDialog}.
 * @author Alexander Herzog
 * @see ExpressionCalculatorDialog
 */
public class ExpressionCalculatorDialogTableModel extends JTableExtAbstractTableModel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=5161347540429062182L;

	/**
	 * Zugehörige Tabelle
	 */
	private final JTableExt table;

	/**
	 * Darzustellende und zu verändernde Zuordnung
	 */
	private final Map<String,Object> map;

	/**
	 * Sortierte Liste der Schlüssel in {@link #map}
	 */
	private String[] keys;

	/**
	 * Konstruktor der Klasse
	 * @param table	Zugehörige Tabelle
	 * @param map	Darzustellende und zu verändernde Zuordnung
	 */
	public ExpressionCalculatorDialogTableModel(final JTableExt table, final Map<String,Object> map) {
		this.table=table;
		this.map=map;
		updateKeySet();
	}

	/**
	 * Aktualisiert die sortierte Liste der Schlüssel ({@link #keys}) gemäß der Zuordnung {@link #map}.
	 * @see #map
	 * @see #keys
	 */
	private void updateKeySet() {
		keys=map.keySet().stream().sorted().toArray(String[]::new);
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
		return columnIndex==3;
	}

	@Override
	public int getRowCount() {
		return map.size()+1;
	}

	@Override
	public int getColumnCount() {
		return 4;
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0: return Language.tr("ExpressionCalculator.Tab.Map.Column.Key");
		case 1: return Language.tr("ExpressionCalculator.Tab.Map.Column.Value");
		case 2: return Language.tr("ExpressionCalculator.Tab.Map.Column.Type");
		default: return "";
		}
	}

	/**
	 * Wandelt die Zeichen "&amp;", "&lt;" und "&gt;" in ihre entsprechenden
	 * HTML-Entitäten um.
	 * @param line	Umzuwandelnder Text
	 * @return	Umgewandelter Text
	 */
	private static String encodeHTML(final String line) {
		if (line==null) return "";
		String result;
		result=line.replaceAll("&","&amp;");
		result=result.replaceAll("<","&lt;");
		result=result.replaceAll(">","&gt;");
		if (result.length()>50) result=result.substring(0,45)+"...";
		return result;
	}

	/**
	 * Liefert den Zeichenketten-Wert für ein Objekt (mit encodierten html-Entities).
	 * @param obj	Ausgangsobjekt
	 * @return	Zeichenkette zu dem Objekt
	 */
	public static String processValue(final Object obj) {
		if (obj instanceof Double) return NumberTools.formatNumber((Double)obj);
		if (obj instanceof Integer) return NumberTools.formatLong((Integer)obj);
		if (obj instanceof Long) return NumberTools.formatLong((Long)obj);
		if (obj instanceof Number) return ((Number)obj).toString();
		if (obj instanceof String) return encodeHTML((String)obj);
		if (obj instanceof Object[]) return encodeHTML(Arrays.toString(((Object[])obj)));
		return encodeHTML(""+obj);
	}

	/**
	 * Liefert einen Eintrag für die letzte Zeile
	 * @param columnIndex	Spaltenindex (0 bis 3)
	 * @return	Inhalt der Zelle
	 */
	private Object getValueAtLastRow(final int columnIndex) {
		if (columnIndex==3) {
			return makeButtonPanel(new String[] {Language.tr("ExpressionCalculator.Tab.Map.Add")},new Icon[] {Images.EDIT_ADD.getIcon()},new ActionListener[]{e->add()});
		} else {
			return "";
		}
	}

	@Override
	public Object getValueAt(final int rowIndex, final int columnIndex) {
		if (rowIndex==map.size()) return getValueAtLastRow(columnIndex);

		switch (columnIndex) {
		case 0:
			return "<html><body>"+encodeHTML(keys[rowIndex])+"</body></html>";
		case 1:
			return "<html><body>"+processValue(map.get(keys[rowIndex]))+"</body></html>";
		case 2:
			return "<html><body>"+encodeHTML(map.get(keys[rowIndex]).getClass().getSimpleName())+"</body></html>";
		case 3:
			return makeButtonPanel(new String[] {"",""},new String[] {Language.tr("ExpressionCalculator.Tab.Map.Edit"),Language.tr("ExpressionCalculator.Tab.Map.Delete")},new Icon[] {Images.GENERAL_SETUP.getIcon(),Images.EDIT_DELETE.getIcon()},new ActionListener[]{e->edit(rowIndex),e->delete(rowIndex)});
		default:
			return "";
		}
	}

	/**
	 * Befehl: Neuen Eintrag hinzufügen
	 */
	private void add() {
		final ExpressionCalculatorDialogTableModelDialog dialog=new ExpressionCalculatorDialogTableModelDialog(table,null,null,keys);
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			map.put(dialog.getNewKey(),dialog.getNewValue());
			updateKeySet();
			updateTable();
		}
	}

	/**
	 * Befehl: Eintrag bearbeiten
	 * @param index	Index des Eintrags
	 */
	private void edit(final int index) {
		final String oldKey=keys[index];
		final Object oldValue=map.get(oldKey);
		final ExpressionCalculatorDialogTableModelDialog dialog=new ExpressionCalculatorDialogTableModelDialog(table,oldKey,oldValue,keys);
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			map.remove(oldKey);
			map.put(dialog.getNewKey(),dialog.getNewValue());
			updateKeySet();
			updateTable();
		}
	}

	/**
	 * Befehl: Eintrag löschen
	 * @param index	Index des Eintrags
	 */
	private void delete(final int index) {
		if (!MsgBox.confirm(table,Language.tr("ExpressionCalculator.Tab.Map.Delete.ConfirmTitle"),String.format(Language.tr("ExpressionCalculator.Tab.Map.Delete.ConfirmInfo"),keys[index]),Language.tr("ExpressionCalculator.Tab.Map.Delete.ConfirmInfoYes"),Language.tr("ExpressionCalculator.Tab.Map.Delete.ConfirmInfoNo"))) return;
		map.remove(keys[index]);
		updateKeySet();
		updateTable();
	}
}

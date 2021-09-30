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
package ui.parameterseries;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.TableCellEditor;

import language.Language;
import mathtools.NumberTools;
import systemtools.MsgBox;
import tools.JTableExt;
import tools.JTableExtAbstractTableModel;
import ui.ModelChanger;

/**
 * Tabellenmodell zur Verwendung innerhalb von {@link ParameterCompareSetupModelDialog}
 * @author Alexander Herzog
 * @see ParameterCompareSetupModelDialog
 */
public class ParameterCompareSetupModelTableModel extends JTableExtAbstractTableModel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -5863646810585281556L;

	/** Zugehörige Tabelle */
	private final JTableExt table;
	/** Liste der Eingabeparameter-Einstellungen */
	private final List<ParameterCompareSetupValueInput> input;
	/** Werte der Eingabeparameter */
	private final List<Double> data;

	/**
	 * Konstruktor der Klasse
	 * @param table	Zugehörige Tabelle
	 * @param model	Modell innerhalb der Parameter-Vergleichs-Funktion, das bearbeitet werden soll
	 * @param input	Liste der Eingabeparameter-Einstellungen
	 */
	public ParameterCompareSetupModelTableModel(final JTableExt table, final ParameterCompareSetupModel model, final List<ParameterCompareSetupValueInput> input) {
		super();
		this.table=table;
		this.input=input;
		data=new ArrayList<>();
		for (int i=0;i<input.size();i++) data.add(model.getInput().get(input.get(i).getName()));
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
		return columnIndex==1;
	}

	@Override
	public int getRowCount() {
		return input.size();
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0: return Language.tr("ParameterCompare.Settings.Model.Data.Column.Parameter");
		case 1: return Language.tr("ParameterCompare.Settings.Model.Data.Column.Value");
		default: return "";
		}
	}

	/**
	 * Liefert den Beschreibungstext für einen Input-Parameter
	 * @param record	Input-Parameter Datensatz
	 * @return	Beschreibungstext
	 */
	private String getInputInfo(final ParameterCompareSetupValueInput record) {
		switch (record.getMode()) {
		case MODE_RESOURCE:
			return String.format(Language.tr("ParameterCompare.Settings.Input.List.InfoResource"),record.getTag());
		case MODE_VARIABLE:
			return String.format(Language.tr("ParameterCompare.Settings.Input.List.InfoVariable"),record.getTag());
		case MODE_MAP:
			return String.format(Language.tr("ParameterCompare.Settings.Input.List.InfoMap"),record.getTag());
		case MODE_XML:
			String type=ModelChanger.XML_ELEMENT_MODES[Math.max(0,Math.min(ModelChanger.XML_ELEMENT_MODES.length-1,record.getXMLMode()))];
			return String.format(Language.tr("ParameterCompare.Settings.Input.List.InfoXML"),record.getTag(),type);
		default:
			return "";
		}
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex==0) {
			final ParameterCompareSetupValueInput record=input.get(rowIndex);
			final StringBuilder sb=new StringBuilder();
			sb.append("<html><body>");
			sb.append("<b>"+record.getName()+"</b><br>");
			sb.append(getInputInfo(record));
			sb.append("</body></html>");
			return sb.toString();
		}

		final Double D=data.get(rowIndex);
		if (D==null) return "";
		return NumberTools.formatNumberMax(D.doubleValue());
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (columnIndex!=1) return;
		if (!(aValue instanceof String)) return;

		final Double D=NumberTools.getDouble((String)aValue);
		if (D==null) {
			final String name=input.get(rowIndex).getName();
			MsgBox.error(table,Language.tr("ParameterCompare.Settings.Model.Data.ValueError.Title"),String.format(Language.tr("ParameterCompare.Settings.Model.Data.ValueError.NoNumber"),name,rowIndex+1,aValue));
			return;
		}
		data.set(rowIndex,D);
	}

	/**
	 * Prüft, ob alle Eingaben in Ordnung sind
	 * @return	Gibt im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung
	 */
	public String checkData() {
		for (int i=0;i<data.size();i++) {
			final String name=input.get(i).getName();
			final Double D=data.get(i);
			if (D==null) return String.format(Language.tr("ParameterCompare.Settings.Model.Data.ValueError.MissingValue"),name,i+1);
			final double d=D.doubleValue();
			if (input.get(i).getMode()==ModelChanger.Mode.MODE_RESOURCE && (d-Math.floor(d)!=0.0)) return String.format(Language.tr("ParameterCompare.Settings.Model.Data.ValueError.NotInteger"),name,i+1);
		}
		return null;
	}

	/**
	 * Schreibt die Einstellungen in ein Modell zurück
	 * @param model	Modell, in das die Daten geschrieben werden sollen
	 */
	public void storeData(final ParameterCompareSetupModel model) {
		model.getInput().clear();
		for (int i=0;i<data.size();i++) {
			final String name=input.get(i).getName();
			double d=data.get(i);
			if (input.get(i).getMode()==ModelChanger.Mode.MODE_RESOURCE) d=Math.round(d);
			model.getInput().put(name,d);
		}
	}
}
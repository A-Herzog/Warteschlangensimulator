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
import java.util.Arrays;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.table.TableCellEditor;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import systemtools.MsgBox;
import tools.JTableExt;
import tools.JTableExtAbstractTableModel;
import ui.ModelChanger;
import ui.modeleditor.ModelResource;

/**
 * Tabellenmodell zur Verwendung in dem {@link ParameterCompareAssistantDialog}-Dialog
 * @author Alexander Herzog
 * @see ParameterCompareAssistantDialog
 */
public class ParameterCompareAssistantTableModel extends JTableExtAbstractTableModel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 287938555035804166L;

	/** Zugehörige Tabelle */
	private final JTableExt table;
	/** Parameter-Vergleichs-Einstellungen */
	private final ParameterCompareSetup setup;
	/** Ausgangs-Editor-Modell */
	private final EditModel model;
	/** Verfügbare Eingabeparameter */
	private final List<ParameterCompareSetupValueInput> input;

	/** Eingabeparameter aktiv ? */
	private final JCheckBox[] active;
	/** Minimalwert für Eingabeparameter */
	private final double[] valueMin;
	/** Maximalwert für Eingabeparameter */
	private final double[] valueMax;
	/** Schrittweite für Eingabeparameter */
	private final double[] valueStep;
	/** Muss der Eingabeparameter eine Ganzzahl sein? */
	private final JCheckBox[] integers;

	/**
	 * Konstruktor der Klasse
	 * @param table	Zugehörige Tabelle
	 * @param setup	Parameter-Vergleichs-Einstellungen
	 */
	public ParameterCompareAssistantTableModel(final JTableExt table, final ParameterCompareSetup setup) {
		super();
		this.table=table;
		this.setup=setup;
		model=setup.getEditModel();
		input=setup.getInput();

		final int count=input.size();

		active=new JCheckBox[count];
		valueMin=new double[count];
		valueMax=new double[count];
		valueStep=new double[count];
		integers=new JCheckBox[count];

		for (int i=0;i<count;i++) {
			active[i]=new JCheckBox();
			final double[] values=getRange(input.get(i));
			valueMin[i]=values[0];
			valueMax[i]=values[1];
			valueStep[i]=values[2];
			integers[i]=new JCheckBox();
			if (input.get(i).getMode()==ModelChanger.Mode.MODE_RESOURCE) {
				integers[i].setSelected(true);
				integers[i].setEnabled(false);
			}
		}
	}

	/**
	 * Ermittelt Vorschlagswerte für Minimum, Maximum und Schrittweite für den Iterationsbereich über einen Eingangsparameter
	 * @param record	Eingangsparameter für den die Vorschlagswerte ermittelt werden sollen
	 * @return	Array mit Vorschlagswerten für Minimum, Maximum und Schrittweite
	 */
	private double[] getRange(final ParameterCompareSetupValueInput record) {
		double min=1;
		double max=10;
		double step=1;
		String testString=null;

		switch (record.getMode()) {
		case MODE_RESOURCE:
			final ModelResource resource=model.resources.get(record.getTag());
			if (resource!=null) {
				final int count=resource.getCount();
				min=Math.max(1,Math.round(count*0.5));
				max=count*2;
			}
			break;
		case MODE_VARIABLE:
			final int i=model.globalVariablesNames.indexOf(record.getTag());
			if (i>=0) testString=model.globalVariablesExpressions.get(i);
			break;
		case MODE_XML:
			final String value=ModelChanger.getValue(model,record.getTag(),record.getXMLMode());
			if (value!=null) testString=value;
			break;
		}

		if (testString!=null) {
			final Double D=NumberTools.getDouble(testString);
			if (D!=null && D.doubleValue()!=0.0) {
				min=Math.max(0,D.doubleValue()/2);
				max=D.doubleValue()*2;
				if (Math.abs(max-min)<2) step=0.1;
			}
		}

		return new double[]{min,max,step};
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
		return columnIndex!=1 && columnIndex!=2;
	}

	@Override
	public int getRowCount() {
		return input.size();
	}

	@Override
	public int getColumnCount() {
		return 7;
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0: return Language.tr("ParameterCompare.Table.AddModelByAssistant.Column.Active");
		case 1: return Language.tr("ParameterCompare.Table.AddModelByAssistant.Column.Parameter");
		case 2: return Language.tr("ParameterCompare.Table.AddModelByAssistant.Column.InBaseModel");
		case 3: return Language.tr("ParameterCompare.Table.AddModelByAssistant.Column.Min");
		case 4: return Language.tr("ParameterCompare.Table.AddModelByAssistant.Column.Max");
		case 5: return Language.tr("ParameterCompare.Table.AddModelByAssistant.Column.Step");
		case 6: return Language.tr("ParameterCompare.Table.AddModelByAssistant.Column.Integer");
		default: return "";
		}
	}

	/**
	 * Liefert den Namen eines Eingangsparameter
	 * @param index	Index des Eingangsparameters
	 * @return	Name des Parameters
	 * @see #getValueAt(int, int)
	 */
	private String getParameterName(final int index) {
		final ParameterCompareSetupValueInput record=input.get(index);

		final StringBuilder sb=new StringBuilder();
		sb.append("<html><body>");
		sb.append("<b>"+record.getName()+"</b><br>");

		switch (record.getMode()) {
		case MODE_RESOURCE:
			sb.append(String.format(Language.tr("ParameterCompare.Settings.Input.List.InfoResource"),record.getTag()));
			break;
		case MODE_VARIABLE:
			sb.append(String.format(Language.tr("ParameterCompare.Settings.Input.List.InfoVariable"),record.getTag()));
			break;
		case MODE_XML:
			String type=ModelChanger.XML_ELEMENT_MODES[Math.max(0,Math.min(ModelChanger.XML_ELEMENT_MODES.length-1,record.getXMLMode()))];
			sb.append(String.format(Language.tr("ParameterCompare.Settings.Input.List.InfoXML"),record.getTag(),type));
			break;
		}

		sb.append("</body></html>");
		return sb.toString();
	}

	/**
	 * Liefert eine Beschreibung für einen Eingangsparameter
	 * @param index	Index des Eingangsparameters
	 * @return	Beschreibung
	 * @see #getValueAt(int, int)
	 */
	private String getDefaultParameterValue(final int index) {
		final ParameterCompareSetupValueInput record=input.get(index);

		switch (record.getMode()) {
		case MODE_RESOURCE:
			final ModelResource resource=model.resources.get(record.getTag());
			if (resource!=null) return ""+resource.getCount();
			break;
		case MODE_VARIABLE:
			final int i=model.globalVariablesNames.indexOf(record.getTag());
			if (i>=0) return model.globalVariablesExpressions.get(i);
			break;
		case MODE_XML:
			final String value=ModelChanger.getValue(model,record.getTag(),record.getXMLMode());
			if (value!=null) return value;
			break;
		}
		return "";
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case 0: return active[rowIndex];
		case 1: return getParameterName(rowIndex);
		case 2: return getDefaultParameterValue(rowIndex);
		case 3: return NumberTools.formatNumberMax(valueMin[rowIndex]);
		case 4: return NumberTools.formatNumberMax(valueMax[rowIndex]);
		case 5: return NumberTools.formatNumberMax(valueStep[rowIndex]);
		case 6: return integers[rowIndex];
		default: return null;
		}
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (columnIndex<3 || columnIndex>5) return;
		if (!(aValue instanceof String)) return;

		final Double D=NumberTools.getDouble((String)aValue);
		if (D==null) {
			final String name=input.get(rowIndex).getName();
			MsgBox.error(table,Language.tr("ParameterCompare.Table.AddModelByAssistant.Error"),String.format(Language.tr("ParameterCompare.Table.AddModelByAssistant.Error.NoNumber"),name,rowIndex+1,aValue));
			return;
		}

		active[rowIndex].setSelected(true);
		final double d=D.doubleValue();
		switch (columnIndex) {
		case 3:
			if (valueMin[rowIndex]==d) return;
			valueMin[rowIndex]=d;
			break;
		case 4:
			if (valueMax[rowIndex]==d) return;
			valueMax[rowIndex]=d;
			break;
		case 5:
			if (valueStep[rowIndex]==d) return;
			valueStep[rowIndex]=d;
			break;
		}
		updateTable();
	}

	/**
	 * Überprüft die Eingaben und gibt ggf. eine Fehlermeldung zurück
	 * @return	Fehlermeldung oder <code>null</code>, wenn alles in Ordnung ist
	 */
	public String checkData() {
		boolean b=false;
		for (JCheckBox checkBox: active) if (checkBox.isSelected()) {b=true; break;}
		if (!b) return Language.tr("ParameterCompare.Table.AddModelByAssistant.Error.NoParameterActive");

		for (int i=0;i<input.size();i++) {
			if (!active[i].isSelected()) continue;
			if (!integers[i].isSelected()) continue;
			if (valueMin[i]-Math.floor(valueMin[i])>0) return String.format(Language.tr("ParameterCompare.Table.AddModelByAssistant.Error.NeedInteger.MinValue"),input.get(i).getName(),i+1);
			if (valueMax[i]-Math.floor(valueMax[i])>0) return String.format(Language.tr("ParameterCompare.Table.AddModelByAssistant.Error.NeedInteger.MaxValue"),input.get(i).getName(),i+1);
			if (valueStep[i]-Math.floor(valueStep[i])>0) return String.format(Language.tr("ParameterCompare.Table.AddModelByAssistant.Error.NeedInteger.StepValue"),input.get(i).getName(),i+1);
		}

		return null;
	}

	/**
	 * Liefert die Nummer des als nächstes anzupassenden Eingabeparameters
	 * @param lastLevel	Zuvor angepasster Parameter
	 * @return	Nummer des als nächstes anzupassenden Eingabeparameters
	 */
	private int nextLevel(final int lastLevel) {
		for (int i=Math.max(-1,lastLevel)+1;i<active.length;i++) if (active[i].isSelected()) return i;
		return -1;
	}

	/**
	 * Erstellt einen Text aus den Werten der Eingabeparameter
	 * @param values	Werte der Eingabeparameter
	 * @return	Werten der Eingabeparameter als Text
	 */
	private String valuesToString(final Double[] values) {
		final StringBuilder sb=new StringBuilder();
		for (Double value: values) if (value!=null) {
			if (sb.length()>0) sb.append("; ");
			sb.append(NumberTools.formatNumber(value.doubleValue()));
		}
		return sb.toString();
	}

	/**
	 * Liefert die nächste verfügbare Modellnummer
	 * @return	Nächste verfügbare Modellnummer
	 */
	private int getNextFreeModelNumber() {
		int nr=1;
		while (true) {
			boolean ok=true;
			final String testName=String.format(Language.tr("ParameterCompare.Table.AddModelByAssistant.ModelName"),nr);
			for (ParameterCompareSetupModel model: setup.getModels()) {
				if (model.getName().startsWith(testName)) {ok=false; break;}
			}
			if (ok) return nr;
			nr++;
		}
	}

	/**
	 * Fügt ein Modell zu der Parameterreihen-Modellliste hinzu
	 * @param values	Werte für die Eingabeparameter
	 * @param nr	Nummer des Modells
	 * @param parametersInNames	Stellt ein, ob bei der Erstellung der Modelle die gewählten Parmeterwerte in die Modellnamen aufgenommen werden sollen
	 */
	private void addModel(final Double[] values, final int nr, final boolean parametersInNames) {
		final ParameterCompareSetupModel model=new ParameterCompareSetupModel();
		String addOn="";
		if (parametersInNames) addOn=" ("+valuesToString(values)+")";
		model.setName(String.format(Language.tr("ParameterCompare.Table.AddModelByAssistant.ModelName"),nr)+addOn);
		for (int i=0;i<values.length;i++) if (values[i]!=null) model.getInput().put(input.get(i).getName(),values[i]);
		setup.getModels().add(model);
	}

	/**
	 * Erstellt die Modelle für die Parameterreihe.
	 * @param values	Schon belegte Werte
	 * @param level	Anzupassender Parameter
	 * @param parametersInNames	Stellt ein, ob bei der Erstellung der Modelle die gewählten Parmeterwerte in die Modellnamen aufgenommen werden sollen
	 * @see #addModel(Double[], int, boolean)
	 * @see #storeData(boolean, boolean)
	 */
	private void build(final Double[] values, final int level, final boolean parametersInNames) {
		double min=valueMin[level];
		double max=valueMax[level];
		double step=valueStep[level];

		double value=min;
		while (value<=max) {
			values[level]=value;
			final int nextLevel=nextLevel(level);
			if (nextLevel==-1) {
				addModel(values,getNextFreeModelNumber(),parametersInNames);
			} else {
				build(values,nextLevel,parametersInNames);
			}
			value+=step;
		}
	}

	/**
	 * Erstellt in dem im Konstruktor übergebenen Modell eine Parameterreihe gemäß den
	 * in diesen Tabellendaten angegebenen Einstellungen (im Modus "Alle Variationen").
	 * @param parametersInNames	Stellt ein, ob bei der Erstellung der Modelle die gewählten Parmeterwerte in die Modellnamen aufgenommen werden sollen
	 */
	private void storeDataMulti(final boolean parametersInNames) {
		for (int i=0;i<valueMax.length;i++) {
			if (valueMax[i]<valueMin[i]) {
				final double d=valueMax[i]; valueMax[i]=valueMin[i]; valueMin[i]=d;
			}
			valueStep[i]=Math.abs(valueStep[i]);
		}

		build(new Double[valueMax.length],nextLevel(-1),parametersInNames);
	}

	/**
	 * Erstellt ein einzelnes Modell im Modus "nur Parameter im Gleichlauf".
	 * @param values	Werte für das Modell
	 * @param parametersInNames	Stellt ein, ob bei der Erstellung der Modelle die gewählten Parmeterwerte in die Modellnamen aufgenommen werden sollen
	 */
	private void buildSimple(final double[] values, final boolean parametersInNames) {
		final Double[] values2=new Double[values.length];
		for (int i=0;i<values.length;i++) if (integers[i].isSelected()) values2[i]=Math.abs(values[i]); else values2[i]=values[i];
		addModel(values2,getNextFreeModelNumber(),parametersInNames);
	}

	/**
	 * Erstellt in dem im Konstruktor übergebenen Modell eine Parameterreihe gemäß den
	 * in diesen Tabellendaten angegebenen Einstellungen (im Modus "nur Parameter im Gleichlauf").
	 * @param parametersInNames	Stellt ein, ob bei der Erstellung der Modelle die gewählten Parmeterwerte in die Modellnamen aufgenommen werden sollen
	 */
	private void storeDataSimple(final boolean parametersInNames) {
		final double[] values=Arrays.copyOf(valueMin,valueMin.length);

		boolean done=false;
		while (!done) {
			buildSimple(values,parametersInNames);
			done=true;
			for (int i=0;i<values.length;i++) if (values[i]<valueMax[i]) {
				done=false;
				values[i]+=valueStep[i];
			}
		}
	}

	/**
	 * Erstellt in dem im Konstruktor übergebenen Modell eine Parameterreihe gemäß den
	 * in diesen Tabellendaten angegebenen Einstellungen.
	 * @param parametersInNames	Stellt ein, ob bei der Erstellung der Modelle die gewählten Parmeterwerte in die Modellnamen aufgenommen werden sollen
	 * @param fullMultiMode	Alle Variationen erstellen (<code>true</code>) oder nur Parameter im Gleichlauf verändern
	 */
	public void storeData(final boolean parametersInNames, final boolean fullMultiMode) {
		if (fullMultiMode || valueMax.length==1) {
			storeDataMulti(parametersInNames);
		} else {
			storeDataSimple(parametersInNames);
		}
	}
}
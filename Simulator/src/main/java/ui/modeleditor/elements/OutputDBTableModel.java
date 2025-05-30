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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.table.TableCellEditor;

import language.Language;
import simulator.simparser.ExpressionCalc;
import simulator.simparser.ExpressionCalcModelUserFunctions;
import systemtools.MsgBox;
import tools.JTableExt;
import tools.JTableExtAbstractTableModel;
import ui.images.Images;

/**
 * Diese Klasse h�lt die Daten f�r eine Tabelle zur Konfiguration von Ausgaben �ber
 * Datenbank-Ausgabe-Stationen vor.
 * @author Alexander Herzog
 * @see ModelElementOutputDBDialog
 * @see ModelElementOutputDB
 */
public class OutputDBTableModel extends JTableExtAbstractTableModel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 4147425861546783704L;

	/** Nur-Lese-Status */
	private final boolean readOnly;
	/** Liste mit den Ausgabemodi */
	private final List<ModelElementOutputDB.OutputMode> mode;
	/** Liste mit den Datenbankspalten in die die Ausgaben erfolgen sollen */
	private final List<String> column;
	/** Liste mit den je nach Ausgabemodus notwendigen zus�tzlichen Daten */
	private final List<String> data;
	/** Liste mit allen Variablennamen */
	private final String[] variableNames;
	/** Modellspezifische nutzerdefinierte Funktionen */
	private final ExpressionCalcModelUserFunctions userFunctions;
	/** Zugeh�rige Tabelle (um das Update der Tabelle veranlassen zu k�nnen, wenn sich die Daten ver�ndert haben) */
	private final JTableExt table;
	/** Liste mit zur Auswahl bereitstehenden Datenbankspaltennamen */
	private final List<String> dbColumnNames;

	/**
	 * Konstruktor der Klasse
	 * @param table	Zugeh�rige Tabelle (um das Update der Tabelle veranlassen zu k�nnen, wenn sich die Daten ver�ndert haben)
	 * @param mode	Liste mit den Ausgabemodi
	 * @param column	Liste mit den Datenbankspalten in die die Ausgaben erfolgen sollen
	 * @param data	Liste mit den je nach Ausgabemodus notwendigen zus�tzlichen Daten
	 * @param variableNames	Liste mit allen Variablennamen
	 * @param userFunctions	Modellspezifische nutzerdefinierte Funktionen
	 * @param readOnly	Nur-Lese-Status
	 */
	public OutputDBTableModel(final JTableExt table, final List<ModelElementOutputDB.OutputMode> mode, final List<String> column, final List<String> data, final String[] variableNames, final ExpressionCalcModelUserFunctions userFunctions, final boolean readOnly) {
		super();
		this.readOnly=readOnly;
		this.table=table;
		this.mode=new ArrayList<>(); this.mode.addAll(mode);
		this.column=new ArrayList<>(); this.column.addAll(column);
		this.data=new ArrayList<>(); this.data.addAll(data);
		this.variableNames=variableNames;
		this.userFunctions=userFunctions;
		dbColumnNames=new ArrayList<>();
	}

	/**
	 * Liefert die m�glicherweise ver�nderte Liste mit den Ausgabemodi
	 * @return	Neue Liste mit den Ausgabemodi
	 */
	public List<ModelElementOutputDB.OutputMode> getModes() {
		return mode;
	}

	/**
	 * Liefert die m�glicherweise ver�nderte Liste mit den Datenbankspalten in die die Ausgaben erfolgen sollen
	 * @return	Neue Liste mit den Datenbankspalten in die die Ausgaben erfolgen sollen
	 */
	public List<String> getColumns() {
		return column;
	}

	/**
	 * Liefert die m�glicherweise ver�nderte Liste mit den neuen Zusatzdaten zu den Ausgabemodi
	 * @return	Neue Liste mit den Zusatzdaten zu den Ausgabemodi
	 */
	public List<String> getData() {
		return data;
	}

	/**
	 * Aktualisiert die Tabellendarstellung
	 */
	private void updateTable() {
		fireTableDataChanged();
		TableCellEditor cellEditor=table.getCellEditor();
		if (cellEditor!=null) cellEditor.stopCellEditing();
	}

	/**
	 * Aktualisiert die zur Auswahl hinterlegte Liste mit den m�glichen Datenbankspaltennamen
	 * @param names	Neue Liste mit zur Auswahl bereitstehenden Datenbankspaltennamen
	 */
	public void updateDBColumnNames(final List<String> names) {
		dbColumnNames.clear();
		if (names!=null) dbColumnNames.addAll(names);
	}

	@Override
	public int getRowCount() {
		return mode.size()+1;
	}

	@Override
	public int getColumnCount() {
		return 3;
	}

	/**
	 * Wandelt einen Ausgabemodus in einen Index f�r die Auswahlbox um.
	 * @param mode	Ausgabemodus
	 * @return	Index f�r die Auswahlbox
	 */
	private int modeToInt(final ModelElementOutputDB.OutputMode mode) {
		switch (mode) {
		case MODE_TIMESTAMP: return 0;
		case MODE_TEXT: return  1;
		case MODE_EXPRESSION: return 2;
		case MODE_CLIENT: return 3;
		case MODE_WAITINGTIME_NUMBER: return 4;
		case MODE_WAITINGTIME_TIME: return 5;
		case MODE_TRANSFERTIME_NUMBER: return 6;
		case MODE_TRANSFERTIME_TIME: return 7;
		case MODE_PROCESSTIME_NUMBER: return 8;
		case MODE_PROCESSTIME_TIME: return 9;
		case MODE_RESIDENCETIME_NUMBER: return 10;
		case MODE_RESIDENCETIME_TIME: return 11;
		case MODE_STRING: return 12;
		}
		return 0;
	}

	/**
	 * Wandelt einen Auswahlbox-Index in einen Ausgabemodus um.
	 * @param index	Auswahlbox-Index
	 * @return	Ausgabemodus
	 */
	private ModelElementOutputDB.OutputMode intToMode(final int index) {
		switch (index) {
		case 0: return ModelElementOutputDB.OutputMode.MODE_TIMESTAMP;
		case 1: return ModelElementOutputDB.OutputMode.MODE_TEXT;
		case 2: return ModelElementOutputDB.OutputMode.MODE_EXPRESSION;
		case 3: return ModelElementOutputDB.OutputMode.MODE_CLIENT;
		case 4: return ModelElementOutputDB.OutputMode.MODE_WAITINGTIME_NUMBER;
		case 5: return ModelElementOutputDB.OutputMode.MODE_WAITINGTIME_TIME;
		case 6: return ModelElementOutputDB.OutputMode.MODE_TRANSFERTIME_NUMBER;
		case 7: return ModelElementOutputDB.OutputMode.MODE_TRANSFERTIME_TIME;
		case 8: return ModelElementOutputDB.OutputMode.MODE_PROCESSTIME_NUMBER;
		case 9: return ModelElementOutputDB.OutputMode.MODE_PROCESSTIME_TIME;
		case 10: return ModelElementOutputDB.OutputMode.MODE_RESIDENCETIME_NUMBER;
		case 11: return ModelElementOutputDB.OutputMode.MODE_RESIDENCETIME_TIME;
		case 12: return ModelElementOutputDB.OutputMode.MODE_STRING;
		}
		return ModelElementOutputDB.OutputMode.MODE_TIMESTAMP;
	}

	/**
	 * Erstellt ein Panel zur Auswahl des Ausgabemodus
	 * @param rowIndex	Zeile in der Tabelle f�r das Panel
	 * @param selectedMode	Initial auszuw�hlender Ausgabemodus
	 * @return	Panel zur Auswahl des Ausgabemodus
	 */
	private JPanel makeModePanel(final int rowIndex, final ModelElementOutputDB.OutputMode selectedMode) {
		final JPanel panel=new JPanel(new BorderLayout());

		final JComboBox<String> comboBox=new JComboBox<>(new ModelElementOutputDB(null,null).getModeNameDescriptions());
		comboBox.setSelectedIndex(modeToInt(selectedMode));
		comboBox.addActionListener(new TableButtonListener(rowIndex,ActionIndex.ACTION_COMBO_MODE_CHANGE,comboBox));
		Box box=Box.createVerticalBox();
		box.add(Box.createVerticalGlue());
		box.add(comboBox);
		box.add(Box.createVerticalGlue());
		panel.setBackground(Color.WHITE);
		panel.add(box,BorderLayout.CENTER);

		final JPanel rightPanel=new JPanel(new FlowLayout(FlowLayout.LEFT));
		rightPanel.setBackground(Color.WHITE);
		JButton button;
		if (rowIndex>0) {
			rightPanel.add(button=new JButton());
			button.setIcon(Images.ARROW_UP.getIcon());
			button.setToolTipText(Language.tr("Surface.OutputDB.Table.Up"));
			button.addActionListener(new TableButtonListener(rowIndex,ActionIndex.ACTION_UP));
		}
		if (rowIndex<mode.size()-1) {
			rightPanel.add(button=new JButton());
			button.setIcon(Images.ARROW_DOWN.getIcon());
			button.setToolTipText(Language.tr("Surface.OutputDB.Table.Down"));
			button.addActionListener(new TableButtonListener(rowIndex,ActionIndex.ACTION_DOWN));
		}
		rightPanel.add(button=new JButton());
		button.setIcon(Images.EDIT_DELETE.getIcon());
		button.setToolTipText(Language.tr("Surface.OutputDB.Table.Delete"));
		button.addActionListener(new TableButtonListener(rowIndex,ActionIndex.ACTION_DELETE));
		panel.add(rightPanel,BorderLayout.EAST);

		return panel;
	}

	/**
	 * Erstellt das Panel zur Auswahl der Datenbank-Tabellenspalte in die der Datensatz geschrieben werden soll.
	 * @param rowIndex	Zeile der Tabelle bzw. Index des Eintrags
	 * @param selectedColumnName	Aktuell ausgew�hlte Tabellenspalte
	 * @return	Panel zur Auswahl der Datenbank-Tabellenspalte
	 */
	private JPanel makeColumnPanel(final int rowIndex, final String selectedColumnName) {
		final JPanel panel=new JPanel(new BorderLayout());

		final String sel=column.get(rowIndex);
		final List<String> cols=new ArrayList<>(dbColumnNames);
		int index=-1;
		if (sel.isBlank()) {
			if (cols.size()>0) {
				index=0;
				column.set(rowIndex,cols.get(0));
			}
		} else {
			for (int i=0;i<cols.size();i++) if (cols.get(i).equalsIgnoreCase(sel)) {index=i; break;}
			if (index<0) {cols.add(sel); index=cols.size()-1;}
		}
		final JComboBox<String> comboBox=new JComboBox<>(cols.toArray(String[]::new));
		if (index>=0) comboBox.setSelectedIndex(index);
		comboBox.addActionListener(new TableButtonListener(rowIndex,ActionIndex.ACTION_COMBO_COLUMN_CHANGE,comboBox));
		Box box=Box.createVerticalBox();
		box.add(Box.createVerticalGlue());
		box.add(comboBox);
		box.add(Box.createVerticalGlue());
		panel.setBackground(Color.WHITE);
		panel.add(box,BorderLayout.CENTER);

		return panel;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex==mode.size()) {
			switch (columnIndex) {
			case 0:	return makeButtonPanel(new String[]{Language.tr("Surface.OutputDB.Table.Add")},new Icon[]{Images.EDIT_ADD.getIcon()},new ActionListener[]{new TableButtonListener()});
			case 1: return "";
			case 2: return "";
			}
		}

		switch (columnIndex) {
		case 0:
			return makeModePanel(rowIndex,mode.get(rowIndex));
		case 1:
			if (mode.get(rowIndex)==ModelElementOutputDB.OutputMode.MODE_TEXT || mode.get(rowIndex)==ModelElementOutputDB.OutputMode.MODE_EXPRESSION || mode.get(rowIndex)==ModelElementOutputDB.OutputMode.MODE_STRING) {
				return makeEditPanel(data.get(rowIndex),Images.GENERAL_SETUP.getIcon(),new TableButtonListener(rowIndex,ActionIndex.ACTION_EDIT_DATA));
			} else {
				final JPanel panel=new JPanel(new FlowLayout(FlowLayout.LEFT,5,5));
				panel.setBackground(Color.WHITE);
				panel.add(new JLabel(Language.tr("Surface.OutputDB.Table.NoAdditionalData")));
				return panel;
			}
		case 2:
			return makeColumnPanel(rowIndex,column.get(rowIndex));
		}
		return null;
	}

	@Override
	public final boolean isCellEditable(int rowIndex, int columnIndex) {
		return !readOnly;
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0: return Language.tr("Surface.OutputDB.Table.HeaderOutput");
		case 1: return Language.tr("Surface.OutputDB.Table.HeaderAdditionalData");
		case 2: return Language.tr("Surface.OutputDB.Table.HeaderColumn");
		default: return super.getColumnName(column);
		}
	}

	/**
	 * Auszuf�hrende Aktion
	 * @see TableButtonListener
	 */
	private enum ActionIndex {
		/** Eintrag hinzuf�gen */
		ACTION_ADD,
		/** Stellt den ge�nderten Ausgabemodus ein */
		ACTION_COMBO_MODE_CHANGE,
		/** Stellt eine ge�nderte Datenbank-Tabellenspalte ein */
		ACTION_COMBO_COLUMN_CHANGE,
		/** Eintrag nach oben verschieben */
		ACTION_UP,
		/** Eintrag nach unten verschieben */
		ACTION_DOWN,
		/** Eintrag l�schen */
		ACTION_DELETE,
		/** Zus�tzliche Daten zu dem Ausgabe-Eintrag bearbeiten */
		ACTION_EDIT_DATA
	}

	/**
	 * Reagiert auf Eingaben und die Schaltfl�chen in der Tabelle
	 */
	private class TableButtonListener implements ActionListener {
		/** Tabellenzeile */
		private final int row;
		/** Auszuf�hrende Aktion */
		private final ActionIndex actionIndex;
		/** Combobox auf die sich die Einstellungen beziehen */
		private final JComboBox<String> combo;

		/**
		 * Konstruktor der Klasse
		 * @param row	Zeile
		 * @param actionIndex	Auszuf�hrende Aktion
		 * @param combo	Combobox auf die sich die Einstellungen beziehen
		 */
		public TableButtonListener(final int row, final ActionIndex actionIndex, final JComboBox<String> combo) {
			this.row=row;
			this.actionIndex=actionIndex;
			this.combo=combo;
		}

		/**
		 * Konstruktor der Klasse
		 * @param row	Zeile
		 * @param actionIndex	Auszuf�hrende Aktion
		 */
		public TableButtonListener(final int row, final ActionIndex actionIndex) {
			this(row,actionIndex,null);
		}

		/**
		 * Konstruktor der Klasse<br>
		 * (Modus: Hinzuf�gen)
		 */
		public TableButtonListener() {
			this(0,ActionIndex.ACTION_ADD,null);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			String s;
			switch (actionIndex) {
			case ACTION_ADD:
				mode.add(ModelElementOutputDB.OutputMode.MODE_TIMESTAMP);
				column.add("");
				data.add("");
				updateTable();
				break;
			case ACTION_COMBO_MODE_CHANGE:
				mode.set(row,intToMode(combo.getSelectedIndex()));
				updateTable();
				break;
			case ACTION_COMBO_COLUMN_CHANGE:
				final Object obj=combo.getSelectedItem();
				if (obj instanceof String) column.set(row,(String)obj);
				updateTable();
				break;
			case ACTION_UP:
				if (row>0) {
					ModelElementOutputDB.OutputMode i=mode.get(row-1);
					String s1=column.get(row-1);
					String s2=data.get(row-1);
					mode.set(row-1,mode.get(row));
					column.set(row-1,column.get(row));
					data.set(row-1,data.get(row));
					mode.set(row,i);
					column.set(row,s1);
					data.set(row,s2);
				}
				updateTable();
				break;
			case ACTION_DOWN:
				if (row<mode.size()-1) {
					ModelElementOutputDB.OutputMode i=mode.get(row+1);
					String s1=column.get(row+1);
					String s2=data.get(row+1);
					mode.set(row+1,mode.get(row));
					column.set(row+1,column.get(row));
					data.set(row+1,data.get(row));
					mode.set(row,i);
					column.set(row,s1);
					data.set(row,s2);
				}
				updateTable();
				break;
			case ACTION_DELETE:
				mode.remove(row);
				column.remove(row);
				data.remove(row);
				updateTable();
				break;
			case ACTION_EDIT_DATA:
				final ModelElementOutputDB.OutputMode m=mode.get(row);
				if (m==ModelElementOutputDB.OutputMode.MODE_TEXT) {
					s=JOptionPane.showInputDialog(table,Language.tr("Surface.OutputDB.Table.EditCustomText"),data.get(row));
					if (s!=null) data.set(row,s);
				}
				if (m==ModelElementOutputDB.OutputMode.MODE_STRING) {
					s=JOptionPane.showInputDialog(table,Language.tr("Surface.OutputDB.Table.EditKey"),data.get(row));
					if (s!=null) data.set(row,s);
				}
				if (m==ModelElementOutputDB.OutputMode.MODE_EXPRESSION) {
					s=data.get(row);
					while (true) {
						s=JOptionPane.showInputDialog(table,Language.tr("Surface.OutputDB.Table.EditExpression"),s);
						if (s==null) break;
						if (s.isBlank()) {data.set(row,""); break;}
						int error=ExpressionCalc.check(s,variableNames,userFunctions);
						if (error<0) {data.set(row,s); break;}
						MsgBox.error(table,Language.tr("Surface.OutputDB.Table.ExpressionError.Title"),String.format(Language.tr("Surface.OutputDB.Table.ExpressionError.Info"),s,error+1));
					}
				}
				updateTable();
				break;
			}
		}
	}
}

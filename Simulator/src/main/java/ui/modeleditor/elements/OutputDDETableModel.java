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
 * Diese Klasse hält die Daten für eine Tabelle zur Konfiguration von Ausgaben über
 * DDE-Ausgabe-Stationen vor.
 * @author Alexander Herzog
 * @see ModelElementOutputDDEDialog
 * @see ModelElementOutputDDE
 */
public class OutputDDETableModel extends JTableExtAbstractTableModel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -7559702535582420139L;

	/** Nur-Lese-Status */
	private final boolean readOnly;
	/** Liste mit den Ausgabemodi */
	private final List<ModelElementOutputDDE.OutputMode> mode;
	/** Liste mit den je nach Ausgabemodus notwendigen zusätzlichen Daten */
	private final List<String> data;
	/** Liste mit allen Variablennamen */
	private final String[] variableNames;
	/** Modellspezifische nutzerdefinierte Funktionen */
	private final ExpressionCalcModelUserFunctions userFunctions;
	/** Zugehörige Tabelle (um das Update der Tabelle veranlassen zu können, wenn sich die Daten verändert haben) */
	private final JTableExt table;

	/**
	 * Konstruktor der Klasse
	 * @param table	Zugehörige Tabelle (um das Update der Tabelle veranlassen zu können, wenn sich die Daten verändert haben)
	 * @param mode	Liste mit den Ausgabemodi
	 * @param data	Liste mit den je nach Ausgabemodus notwendigen zusätzlichen Daten
	 * @param variableNames	Liste mit allen Variablennamen
	 * @param userFunctions	Modellspezifische nutzerdefinierte Funktionen
	 * @param readOnly	Nur-Lese-Status
	 */
	public OutputDDETableModel(final JTableExt table, final List<ModelElementOutputDDE.OutputMode> mode, final List<String> data, final String[] variableNames, final ExpressionCalcModelUserFunctions userFunctions, final boolean readOnly) {
		super();
		this.readOnly=readOnly;
		this.table=table;
		this.mode=new ArrayList<>(); this.mode.addAll(mode);
		this.data=new ArrayList<>(); this.data.addAll(data);
		this.variableNames=variableNames;
		this.userFunctions=userFunctions;
	}

	/**
	 * Liefert die möglicherweise veränderte Liste mit den Ausgabemodi
	 * @return	Neue Liste mit den Ausgabemodi
	 */
	public List<ModelElementOutputDDE.OutputMode> getModes() {
		return mode;
	}

	/**
	 * Liefert die möglicherweise veränderte Liste mit den neuen Zusatzdaten zu den Ausgabemodi
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

	@Override
	public int getRowCount() {
		return mode.size()+1;
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	/**
	 * Wandelt einen Ausgabemodus in einen Index für die Auswahlbox um.
	 * @param mode	Ausgabemodus
	 * @return	Index für die Auswahlbox
	 */
	private int modeToInt(final ModelElementOutputDDE.OutputMode mode) {
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
	private ModelElementOutputDDE.OutputMode intToMode(final int index) {
		switch (index) {
		case 0: return ModelElementOutputDDE.OutputMode.MODE_TIMESTAMP;
		case 1: return ModelElementOutputDDE.OutputMode.MODE_TEXT;
		case 2: return ModelElementOutputDDE.OutputMode.MODE_EXPRESSION;
		case 3: return ModelElementOutputDDE.OutputMode.MODE_CLIENT;
		case 4: return ModelElementOutputDDE.OutputMode.MODE_WAITINGTIME_NUMBER;
		case 5: return ModelElementOutputDDE.OutputMode.MODE_WAITINGTIME_TIME;
		case 6: return ModelElementOutputDDE.OutputMode.MODE_TRANSFERTIME_NUMBER;
		case 7: return ModelElementOutputDDE.OutputMode.MODE_TRANSFERTIME_TIME;
		case 8: return ModelElementOutputDDE.OutputMode.MODE_PROCESSTIME_NUMBER;
		case 9: return ModelElementOutputDDE.OutputMode.MODE_PROCESSTIME_TIME;
		case 10: return ModelElementOutputDDE.OutputMode.MODE_RESIDENCETIME_NUMBER;
		case 11: return ModelElementOutputDDE.OutputMode.MODE_RESIDENCETIME_TIME;
		case 12: return ModelElementOutputDDE.OutputMode.MODE_STRING;
		}
		return ModelElementOutputDDE.OutputMode.MODE_TIMESTAMP;
	}

	/**
	 * Erstellt ein Panel zur Auswahl des Ausgabemodus
	 * @param rowIndex	Zeile in der Tabelle für das Panel
	 * @param selectedMode	Initial auszuwählender Ausgabemodus
	 * @return	Panel zur Auswahl des Ausgabemodus
	 */
	private JPanel makeModePanel(final int rowIndex, final ModelElementOutputDDE.OutputMode selectedMode) {
		final JPanel panel=new JPanel(new BorderLayout());

		final JComboBox<String> comboBox=new JComboBox<>(new ModelElementOutputDDE(null,null).getModeNameDescriptions());
		comboBox.setSelectedIndex(modeToInt(selectedMode));
		comboBox.addActionListener(new TableButtonListener(rowIndex,ActionIndex.ACTION_COMBO_CHANGE,comboBox));
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
			button.setToolTipText(Language.tr("Surface.OutputDDE.Table.Up"));
			button.addActionListener(new TableButtonListener(rowIndex,ActionIndex.ACTION_UP));
		}
		if (rowIndex<mode.size()-1) {
			rightPanel.add(button=new JButton());
			button.setIcon(Images.ARROW_DOWN.getIcon());
			button.setToolTipText(Language.tr("Surface.OutputDDE.Table.Down"));
			button.addActionListener(new TableButtonListener(rowIndex,ActionIndex.ACTION_DOWN));
		}
		rightPanel.add(button=new JButton());
		button.setIcon(Images.EDIT_DELETE.getIcon());
		button.setToolTipText(Language.tr("Surface.OutputDDE.Table.Delete"));
		button.addActionListener(new TableButtonListener(rowIndex,ActionIndex.ACTION_DELETE));
		panel.add(rightPanel,BorderLayout.EAST);

		return panel;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex==mode.size()) {
			switch (columnIndex) {
			case 0: return "";
			case 1:	return makeButtonPanel(new String[]{Language.tr("Surface.OutputDDE.Table.Add")},new Icon[]{Images.EDIT_ADD.getIcon()},new ActionListener[]{new TableButtonListener()});
			}
		}

		switch (columnIndex) {
		case 0: return makeModePanel(rowIndex,mode.get(rowIndex));
		case 1:
			if (mode.get(rowIndex)==ModelElementOutputDDE.OutputMode.MODE_TEXT || mode.get(rowIndex)==ModelElementOutputDDE.OutputMode.MODE_EXPRESSION || mode.get(rowIndex)==ModelElementOutputDDE.OutputMode.MODE_STRING) {
				return makeEditPanel(data.get(rowIndex),Images.GENERAL_SETUP.getIcon(),new TableButtonListener(rowIndex,ActionIndex.ACTION_EDIT_DATA));
			} else {
				final JPanel panel=new JPanel(new FlowLayout(FlowLayout.LEFT,5,5));
				panel.setBackground(Color.WHITE);
				panel.add(new JLabel(Language.tr("Surface.OutputDDE.Table.NoAdditionalData")));
				return panel;
			}
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
		case 0: return Language.tr("Surface.OutputDDE.Table.HeaderOutput");
		case 1: return Language.tr("Surface.OutputDDE.Table.HeaderAdditionalData");
		default: return super.getColumnName(column);
		}
	}

	/**
	 * Auszuführende Aktion
	 * @see TableButtonListener
	 */
	private enum ActionIndex {
		/** Eintrag hinzufügen */
		ACTION_ADD,
		/** Stellt den geänderten Ausgabemodus ein */
		ACTION_COMBO_CHANGE,
		/** Eintrag nach oben verschieben */
		ACTION_UP,
		/** Eintrag nach unten verschieben */
		ACTION_DOWN,
		/** Eintrag löschen */
		ACTION_DELETE,
		/** Zusätzliche Daten zu dem Ausgabe-Eintrag bearbeiten */
		ACTION_EDIT_DATA
	}

	/**
	 * Reagiert auf Eingaben und die Schaltflächen in der Tabelle
	 */
	private class TableButtonListener implements ActionListener {
		/** Tabellenzeile */
		private final int row;
		/** Auszuführende Aktion */
		private final ActionIndex actionIndex;
		/** Combobox auf die sich die Einstellungen beziehen */
		private final JComboBox<String> combo;

		/**
		 * Konstruktor der Klasse
		 * @param row	Zeile
		 * @param actionIndex	Auszuführende Aktion
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
		 * @param actionIndex	Auszuführende Aktion
		 */
		public TableButtonListener(final int row, final ActionIndex actionIndex) {
			this(row,actionIndex,null);
		}

		/**
		 * Konstruktor der Klasse<br>
		 * (Modus: Hinzufügen)
		 */
		public TableButtonListener() {
			this(0,ActionIndex.ACTION_ADD,null);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			switch (actionIndex) {
			case ACTION_ADD:
				mode.add(ModelElementOutputDDE.OutputMode.MODE_TIMESTAMP);
				data.add("");
				updateTable();
				break;
			case ACTION_COMBO_CHANGE:
				mode.set(row,intToMode(combo.getSelectedIndex()));
				updateTable();
				break;
			case ACTION_UP:
				if (row>0) {
					ModelElementOutputDDE.OutputMode i=mode.get(row-1);
					String s=data.get(row-1);
					mode.set(row-1,mode.get(row));
					data.set(row-1,data.get(row));
					mode.set(row,i);
					data.set(row,s);
				}
				updateTable();
				break;
			case ACTION_DOWN:
				if (row<mode.size()-1) {
					ModelElementOutputDDE.OutputMode i=mode.get(row+1);
					String s=data.get(row+1);
					mode.set(row+1,mode.get(row));
					data.set(row+1,data.get(row));
					mode.set(row,i);
					data.set(row,s);
				}
				updateTable();
				break;
			case ACTION_DELETE:
				mode.remove(row);
				data.remove(row);
				updateTable();
				break;
			case ACTION_EDIT_DATA:
				final ModelElementOutputDDE.OutputMode m=mode.get(row);
				if (m==ModelElementOutputDDE.OutputMode.MODE_TEXT) {
					String s=JOptionPane.showInputDialog(table,Language.tr("Surface.OutputDDE.Table.EditCustomText"),data.get(row));
					if (s!=null) data.set(row,s);
				}
				if (m==ModelElementOutputDDE.OutputMode.MODE_STRING) {
					String s=JOptionPane.showInputDialog(table,Language.tr("Surface.OutputDDE.Table.EditKey"),data.get(row));
					if (s!=null) data.set(row,s);
				}
				if (m==ModelElementOutputDDE.OutputMode.MODE_EXPRESSION) {
					String s=data.get(row);
					while (true) {
						s=JOptionPane.showInputDialog(table,Language.tr("Surface.OutputDDE.Table.EditExpression"),s);
						if (s==null) break;
						if (s.isBlank()) {data.set(row,""); break;}
						int error=ExpressionCalc.check(s,variableNames,userFunctions);
						if (error<0) {data.set(row,s); break;}
						MsgBox.error(table,Language.tr("Surface.OutputDDE.Table.ExpressionError.Title"),String.format(Language.tr("Surface.OutputDDE.Table.ExpressionError.Info"),s,error+1));
					}
				}
				updateTable();
				break;
			}
		}
	}
}

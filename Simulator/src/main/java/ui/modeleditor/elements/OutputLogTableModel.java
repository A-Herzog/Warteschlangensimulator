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
import simulator.editmodel.EditModel;
import simulator.simparser.ExpressionCalc;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.JTableExt;
import tools.JTableExtAbstractTableModel;
import ui.expressionbuilder.ExpressionBuilder;
import ui.images.Images;

/**
 * Diese Klasse h�lt die Daten f�r eine Tabelle zur Konfiguration von Ausgaben �ber
 * Ausgabe(Log)-Stationen vor.
 * @author Alexander Herzog
 * @see ModelElementOutputLogDialog
 * @see ModelElementOutputLog
 */
public class OutputLogTableModel extends JTableExtAbstractTableModel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-6974302930915795021L;

	/** Nur-Lese-Status */
	private final boolean readOnly;
	/** Liste mit den Ausgabemodi */
	private final List<ModelElementOutputLog.OutputMode> mode;
	/** Vollst�ndiges Editor-Modell (f�r den Expression-Builder) */
	private final EditModel model;
	/** Liste mit den je nach Ausgabemodus notwendigen zus�tzlichen Daten */
	private final List<String> data;
	/** Liste mit allen Variablennamen */
	private final String[] variableNames;
	/** Zugeh�rige Tabelle (um das Update der Tabelle veranlassen zu k�nnen, wenn sich die Daten ver�ndert haben) */
	private final JTableExt table;

	/**
	 * Konstruktor der Klasse
	 * @param table	Zugeh�rige Tabelle (um das Update der Tabelle veranlassen zu k�nnen, wenn sich die Daten ver�ndert haben)
	 * @param model	Vollst�ndiges Editor-Modell (f�r den Expression-Builder)
	 * @param mode	Liste mit den Ausgabemodi
	 * @param data	Liste mit den je nach Ausgabemodus notwendigen zus�tzlichen Daten
	 * @param variableNames	Liste mit allen Variablennamen
	 * @param readOnly	Nur-Lese-Status
	 */
	public OutputLogTableModel(final JTableExt table, final EditModel model, final List<ModelElementOutputLog.OutputMode> mode, final List<String> data, final String[] variableNames, final boolean readOnly) {
		super();
		this.readOnly=readOnly;
		this.table=table;
		this.model=model;
		this.mode=new ArrayList<>(); this.mode.addAll(mode);
		this.data=new ArrayList<>(); this.data.addAll(data);
		this.variableNames=variableNames;
	}

	/**
	 * Liefert die m�glicherweise ver�nderte Liste mit den Ausgabemodi
	 * @return	Neue Liste mit den Ausgabemodi
	 */
	public List<ModelElementOutputLog.OutputMode> getModes() {
		return mode;
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

	@Override
	public int getRowCount() {
		return mode.size()+1;
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	/**
	 * Wandelt einen Ausgabemodus in einen Index f�r die Auswahlbox um.
	 * @param mode	Ausgabemodus
	 * @return	Index f�r die Auswahlbox
	 */
	private int modeToInt(final ModelElementOutputLog.OutputMode mode) {
		switch (mode) {
		case MODE_TIMESTAMP: return 0;
		case MODE_TEXT: return  1;
		case MODE_TABULATOR: return 2;
		case MODE_NEWLINE: return 3;
		case MODE_EXPRESSION: return 4;
		case MODE_CLIENT: return 5;
		case MODE_WAITINGTIME_NUMBER: return 6;
		case MODE_WAITINGTIME_TIME: return 7;
		case MODE_TRANSFERTIME_NUMBER: return 8;
		case MODE_TRANSFERTIME_TIME: return 9;
		case MODE_PROCESSTIME_NUMBER: return 10;
		case MODE_PROCESSTIME_TIME: return 11;
		case MODE_RESIDENCETIME_NUMBER: return 12;
		case MODE_RESIDENCETIME_TIME: return 13;
		case MODE_STRING: return 14;
		}
		return 0;
	}

	/**
	 * Wandelt einen Auswahlbox-Index in einen Ausgabemodus um.
	 * @param index	Auswahlbox-Index
	 * @return	Ausgabemodus
	 */
	private ModelElementOutputLog.OutputMode intToMode(final int index) {
		switch (index) {
		case 0: return ModelElementOutputLog.OutputMode.MODE_TIMESTAMP;
		case 1: return ModelElementOutputLog.OutputMode.MODE_TEXT;
		case 2: return ModelElementOutputLog.OutputMode.MODE_TABULATOR;
		case 3: return ModelElementOutputLog.OutputMode.MODE_NEWLINE;
		case 4: return ModelElementOutputLog.OutputMode.MODE_EXPRESSION;
		case 5: return ModelElementOutputLog.OutputMode.MODE_CLIENT;
		case 6: return ModelElementOutputLog.OutputMode.MODE_WAITINGTIME_NUMBER;
		case 7: return ModelElementOutputLog.OutputMode.MODE_WAITINGTIME_TIME;
		case 8: return ModelElementOutputLog.OutputMode.MODE_TRANSFERTIME_NUMBER;
		case 9: return ModelElementOutputLog.OutputMode.MODE_TRANSFERTIME_TIME;
		case 10: return ModelElementOutputLog.OutputMode.MODE_PROCESSTIME_NUMBER;
		case 11: return ModelElementOutputLog.OutputMode.MODE_PROCESSTIME_TIME;
		case 12: return ModelElementOutputLog.OutputMode.MODE_RESIDENCETIME_NUMBER;
		case 13: return ModelElementOutputLog.OutputMode.MODE_RESIDENCETIME_TIME;
		case 14: return ModelElementOutputLog.OutputMode.MODE_STRING;
		}
		return ModelElementOutputLog.OutputMode.MODE_TIMESTAMP;
	}

	/**
	 * Erstellt ein Panel zur Auswahl des Ausgabemodus
	 * @param rowIndex	Zeile in der Tabelle f�r das Panel
	 * @param selectedMode	Initial auszuw�hlender Ausgabemodus
	 * @return	Panel zur Auswahl des Ausgabemodus
	 */
	private JPanel makeModePanel(final int rowIndex, final ModelElementOutputLog.OutputMode selectedMode) {
		final JPanel panel=new JPanel(new BorderLayout());

		final JComboBox<String> comboBox=new JComboBox<>(new ModelElementOutputLog(null,null).getModeNameDescriptions());
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
			button.setToolTipText(Language.tr("Surface.Output.Table.Up"));
			button.addActionListener(new TableButtonListener(rowIndex,ActionIndex.ACTION_UP));
		}
		if (rowIndex<mode.size()-1) {
			rightPanel.add(button=new JButton());
			button.setIcon(Images.ARROW_DOWN.getIcon());
			button.setToolTipText(Language.tr("Surface.Output.Table.Down"));
			button.addActionListener(new TableButtonListener(rowIndex,ActionIndex.ACTION_DOWN));
		}
		rightPanel.add(button=new JButton());
		button.setIcon(Images.EDIT_DELETE.getIcon());
		button.setToolTipText(Language.tr("Surface.Output.Table.Delete"));
		button.addActionListener(new TableButtonListener(rowIndex,ActionIndex.ACTION_DELETE));
		panel.add(rightPanel,BorderLayout.EAST);

		return panel;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex==mode.size()) {
			switch (columnIndex) {
			case 0: return "";
			case 1:	return makeButtonPanel(new String[]{Language.tr("Surface.Output.Table.Add")},new Icon[]{Images.EDIT_ADD.getIcon()},new ActionListener[]{new TableButtonListener()});
			}
		}

		switch (columnIndex) {
		case 0: return makeModePanel(rowIndex,mode.get(rowIndex));
		case 1:
			if (mode.get(rowIndex)==ModelElementOutputLog.OutputMode.MODE_TEXT || mode.get(rowIndex)==ModelElementOutputLog.OutputMode.MODE_EXPRESSION || mode.get(rowIndex)==ModelElementOutputLog.OutputMode.MODE_STRING) {
				return makeEditPanel(data.get(rowIndex),Images.GENERAL_SETUP.getIcon(),new TableButtonListener(rowIndex,ActionIndex.ACTION_EDIT_DATA,variableNames));
			} else {
				final JPanel panel=new JPanel(new FlowLayout(FlowLayout.LEFT,5,5));
				panel.setBackground(Color.WHITE);
				panel.add(new JLabel(Language.tr("Surface.Output.Table.NoAdditionalData")));
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
		case 0: return Language.tr("Surface.Output.Table.HeaderOutput");
		case 1: return Language.tr("Surface.Output.Table.HeaderAdditionalData");
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
		ACTION_COMBO_CHANGE,
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
		final int row;
		/** Auszuf�hrende Aktion */
		final ActionIndex actionIndex;
		/** Zus�tzliche Daten f�r die jeweilige Aktion */
		final Object object;

		/**
		 * Konstruktor der Klasse
		 * @param row	Zeile
		 * @param actionIndex	Auszuf�hrende Aktion
		 * @param object	Zus�tzliche Daten f�r die jeweilige Aktion
		 */
		public TableButtonListener(final int row, final ActionIndex actionIndex, final Object object) {
			this.row=row;
			this.actionIndex=actionIndex;
			this.object=object;
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
			switch (actionIndex) {
			case ACTION_ADD:
				mode.add(ModelElementOutputLog.OutputMode.MODE_TIMESTAMP);
				data.add("");
				updateTable();
				break;
			case ACTION_COMBO_CHANGE:
				mode.set(row,intToMode(((JComboBox<?>)object).getSelectedIndex()));
				updateTable();
				break;
			case ACTION_UP:
				if (row>0) {
					ModelElementOutputLog.OutputMode i=mode.get(row-1);
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
					ModelElementOutputLog.OutputMode i=mode.get(row+1);
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
				final ModelElementOutputLog.OutputMode m=mode.get(row);
				if (m==ModelElementOutputLog.OutputMode.MODE_TEXT) {
					String s=JOptionPane.showInputDialog(table,Language.tr("Surface.Output.Table.EditCustomText"),data.get(row));
					if (s!=null) data.set(row,s);
				}
				if (m==ModelElementOutputLog.OutputMode.MODE_STRING) {
					String s=JOptionPane.showInputDialog(table,Language.tr("Surface.Output.Table.EditKey"),data.get(row));
					if (s!=null) data.set(row,s);
				}
				if (m==ModelElementOutputLog.OutputMode.MODE_EXPRESSION) {
					String s=data.get(row);
					while (true) {
						final String[] variables=(String[])object;
						final ExpressionBuilder dialog=new ExpressionBuilder(table,s,false,variableNames,model.getInitialVariablesWithValues(),ExpressionBuilder.getStationIDs(model.surface),ExpressionBuilder.getStationNameIDs(model.surface),true,false,false,model.userFunctions);
						dialog.setVisible(true);
						if (dialog.getClosedBy()!=BaseDialog.CLOSED_BY_OK) break;
						s=dialog.getExpression();
						if (s==null) break;
						if (s.isBlank()) {data.set(row,""); break;}
						int error=ExpressionCalc.check(s,variables,model.userFunctions);
						if (error<0) {data.set(row,s); break;}
						MsgBox.error(table,Language.tr("Surface.Output.Table.ExpressionError.Title"),String.format(Language.tr("Surface.Output.Table.ExpressionError.Info"),s,error+1));
					}
				}
				updateTable();
				break;
			}
		}
	}
}

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
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
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
 * Diese Klasse hält die Daten für eine Tabelle zur Konfiguration von Ausgaben über
 * Ausgabe-Stationen vor.
 * @author Alexander Herzog
 * @see ModelElementOutputDialog
 * @see ModelElementOutput
 */
public class OutputTableModel extends JTableExtAbstractTableModel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -2042717077111221170L;

	/** Nur-Lese-Status */
	private final boolean readOnly;
	/** Liste mit den Ausgabeelementen */
	private final List<ModelElementOutput.OutputRecord> output;
	/** Vollständiges Editor-Modell (für den Expression-Builder) */
	private final EditModel model;
	/** Liste mit allen Variablennamen */
	private final String[] variableNames;
	/** Zugehörige Tabelle (um das Update der Tabelle veranlassen zu können, wenn sich die Daten verändert haben) */
	private final JTableExt table;

	/**
	 * Konstruktor der Klasse
	 * @param table	Zugehörige Tabelle (um das Update der Tabelle veranlassen zu können, wenn sich die Daten verändert haben)
	 * @param model	Vollständiges Editor-Modell (für den Expression-Builder)
	 * @param output	Liste mit den Ausgabeelemente
	 * @param variableNames	Liste mit allen Variablennamen
	 * @param readOnly	Nur-Lese-Status
	 */
	public OutputTableModel(final JTableExt table, final EditModel model, final List<ModelElementOutput.OutputRecord> output, final String[] variableNames, final boolean readOnly) {
		super();
		this.readOnly=readOnly;
		this.table=table;
		this.model=model;
		this.output=output.stream().map(record->new ModelElementOutput.OutputRecord(record)).collect(Collectors.toList());
		this.variableNames=variableNames;

		table.setIsPanelCellTable(0);
		table.setIsPanelCellTable(1);
		SwingUtilities.invokeLater(()->{
			table.getColumnModel().getColumn(1).setMaxWidth(300);
			table.getColumnModel().getColumn(1).setMinWidth(300);
		});
		table.setEnabled(!readOnly);
	}

	/**
	 * Liefert die möglicherweise veränderte Liste mit den Ausgabemodi
	 * @return	Neue Liste mit den Ausgabemodi
	 */
	public List<ModelElementOutput.OutputRecord> getOutput() {
		return output;
	}

	/**
	 * Stellt eine neue Liste mit Ausgabedaten ein.
	 * @param output	Neue Liste mit Ausgabedaten
	 */
	public void setOutput(final List<ModelElementOutput.OutputRecord> output) {
		this.output.clear();
		this.output.addAll(output.stream().map(record->new ModelElementOutput.OutputRecord(record)).collect(Collectors.toList()));
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
		return output.size()+1;
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
	private int modeToInt(final ModelElementOutput.OutputMode mode) {
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
	private ModelElementOutput.OutputMode intToMode(final int index) {
		switch (index) {
		case 0: return ModelElementOutput.OutputMode.MODE_TIMESTAMP;
		case 1: return ModelElementOutput.OutputMode.MODE_TEXT;
		case 2: return ModelElementOutput.OutputMode.MODE_TABULATOR;
		case 3: return ModelElementOutput.OutputMode.MODE_NEWLINE;
		case 4: return ModelElementOutput.OutputMode.MODE_EXPRESSION;
		case 5: return ModelElementOutput.OutputMode.MODE_CLIENT;
		case 6: return ModelElementOutput.OutputMode.MODE_WAITINGTIME_NUMBER;
		case 7: return ModelElementOutput.OutputMode.MODE_WAITINGTIME_TIME;
		case 8: return ModelElementOutput.OutputMode.MODE_TRANSFERTIME_NUMBER;
		case 9: return ModelElementOutput.OutputMode.MODE_TRANSFERTIME_TIME;
		case 10: return ModelElementOutput.OutputMode.MODE_PROCESSTIME_NUMBER;
		case 11: return ModelElementOutput.OutputMode.MODE_PROCESSTIME_TIME;
		case 12: return ModelElementOutput.OutputMode.MODE_RESIDENCETIME_NUMBER;
		case 13: return ModelElementOutput.OutputMode.MODE_RESIDENCETIME_TIME;
		case 14: return ModelElementOutput.OutputMode.MODE_STRING;
		}
		return ModelElementOutput.OutputMode.MODE_TIMESTAMP;
	}

	/**
	 * Erstellt ein Panel zur Auswahl des Ausgabemodus
	 * @param rowIndex	Zeile in der Tabelle für das Panel
	 * @param selectedMode	Initial auszuwählender Ausgabemodus
	 * @return	Panel zur Auswahl des Ausgabemodus
	 */
	private JPanel makeModePanel(final int rowIndex, final ModelElementOutput.OutputMode selectedMode) {
		final JPanel panel=new JPanel(new BorderLayout());

		final JComboBox<String> comboBox=new JComboBox<>(new ModelElementOutput(null,null).getModeNameDescriptions());
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
		if (rowIndex<output.size()-1) {
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
		if (rowIndex==output.size()) {
			switch (columnIndex) {
			case 0: return "";
			case 1:	return makeButtonPanel(new String[]{Language.tr("Surface.Output.Table.Add")},new Icon[]{Images.EDIT_ADD.getIcon()},new ActionListener[]{new TableButtonListener()});
			}
		}

		final ModelElementOutput.OutputRecord record=output.get(rowIndex);

		switch (columnIndex) {
		case 0: return makeModePanel(rowIndex,record.mode);
		case 1:
			if (record.mode.hasData) {
				return makeEditPanel(record.data,Images.GENERAL_SETUP.getIcon(),new TableButtonListener(rowIndex,ActionIndex.ACTION_EDIT_DATA,variableNames));
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
		final int row;
		/** Auszuführende Aktion */
		final ActionIndex actionIndex;
		/** Zusätzliche Daten für die jeweilige Aktion */
		final Object object;

		/**
		 * Konstruktor der Klasse
		 * @param row	Zeile
		 * @param actionIndex	Auszuführende Aktion
		 * @param object	Zusätzliche Daten für die jeweilige Aktion
		 */
		public TableButtonListener(final int row, final ActionIndex actionIndex, final Object object) {
			this.row=row;
			this.actionIndex=actionIndex;
			this.object=object;
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
				output.add(new ModelElementOutput.OutputRecord());
				updateTable();
				break;
			case ACTION_COMBO_CHANGE:
				output.set(row,new ModelElementOutput.OutputRecord(intToMode(((JComboBox<?>)object).getSelectedIndex()),output.get(row).data));
				updateTable();
				break;
			case ACTION_UP:
				if (row>0) {
					final ModelElementOutput.OutputRecord temp=output.get(row-1);
					output.set(row-1,output.get(row));
					output.set(row,temp);
					updateTable();
				}
				break;
			case ACTION_DOWN:
				if (row<output.size()-1) {
					final ModelElementOutput.OutputRecord temp=output.get(row+1);
					output.set(row+1,output.get(row));
					output.set(row,temp);
					updateTable();
				}
				break;
			case ACTION_DELETE:
				output.remove(row);
				updateTable();
				break;
			case ACTION_EDIT_DATA:
				final ModelElementOutput.OutputMode m=output.get(row).mode;
				if (m==ModelElementOutput.OutputMode.MODE_TEXT) {
					String s=JOptionPane.showInputDialog(table,Language.tr("Surface.Output.Table.EditCustomText"),output.get(row).data);
					if (s!=null) {
						output.set(row,new ModelElementOutput.OutputRecord(m,s));
					} else {
						return;
					}
				}
				if (m==ModelElementOutput.OutputMode.MODE_STRING) {
					String s=JOptionPane.showInputDialog(table,Language.tr("Surface.Output.Table.EditKey"),output.get(row).data);
					if (s!=null) {
						output.set(row,new ModelElementOutput.OutputRecord(m,s));
					} else {
						return;
					}
				}
				if (m==ModelElementOutput.OutputMode.MODE_EXPRESSION) {
					String s=output.get(row).data;
					while (true) {
						final String[] variables=(String[])object;
						final ExpressionBuilder dialog=new ExpressionBuilder(table,s,false,variableNames,model.getInitialVariablesWithValues(),ExpressionBuilder.getStationIDs(model.surface),ExpressionBuilder.getStationNameIDs(model.surface),true,false,false,model.userFunctions);
						dialog.setVisible(true);
						if (dialog.getClosedBy()!=BaseDialog.CLOSED_BY_OK) return;
						s=dialog.getExpression();
						if (s==null) return;
						if (s.trim().isEmpty()) {output.set(row,new ModelElementOutput.OutputRecord(m,"")); break;}
						int error=ExpressionCalc.check(s,variables,model.userFunctions);
						if (error<0) {output.set(row,new ModelElementOutput.OutputRecord(m,s)); break;}
						MsgBox.error(table,Language.tr("Surface.Output.Table.ExpressionError.Title"),String.format(Language.tr("Surface.Output.Table.ExpressionError.Info"),s,error+1));
					}
				}
				updateTable();
				break;
			}
		}
	}
}

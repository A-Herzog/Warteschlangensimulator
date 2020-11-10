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

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.SystemColor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import simulator.db.DBConnect;
import simulator.db.DBSettingsPanel;
import simulator.simparser.ExpressionCalc;
import simulator.simparser.symbols.CalcSymbolClientUserData;
import systemtools.MsgBox;
import tools.IconListCellRenderer;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementInputDB}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementInputDB
 */
public class ModelElementInputDBDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -7980566588068167525L;

	/**
	 * Panel zur Konfiguration der Datenbankverbindung
	 */
	private DBSettingsPanel db;

	/**
	 * Zeigt eine Warnung an, wenn keine Datenbankverbindung hergestellt werden konnte.
	 * @see #dbSettingsChanged()
	 */
	private JLabel errorLabel;

	/**
	 * Auswahlbox für die Tabelle in der Datenbank
	 */
	private JComboBox<String> comboTable;

	/**
	 * Auswahlbox für in der Tabelle zu ladende Spalte
	 */
	private JComboBox<String> comboLoad;

	/**
	 * Auswahlbox für die Sortier-Spalte
	 */
	private JComboBox<String> comboSort;

	/**
	 * Auswahlbox für den Sortiermodus in der Sortier-Spalte
	 */
	private JComboBox<String> comboSortMode;

	/** Option: Nach Tabellenende keine Zuweisungen mehr durchführen */
	private JRadioButton optionSkip;
	/** Option: Vorgabewert nach Ende der Tabelle für Zuweisungen verwenden */
	private JRadioButton optionDefaultValue;
	/** Option: Tabelle nach Ende erneut von vorne einlesen */
	private JRadioButton optionLoop;
	/** Simulation beim Erreichen des Tabellenendes beenden */
	private JRadioButton optionTerminate;
	/** Vorgabewert für den Fall {@link #optionDefaultValue} */
	private JTextField defaultValueEdit;

	/**
	 * Eingabefeld für den Variablennamen an die die Zuweisung gerichtet werden soll
	 */
	private JTextField variableEdit;

	/**
	 * Zeigt wenn nötig eine Warnung zu der dem angegebenen Variablennamen an.
	 * @see #variableEdit
	 */
	private JLabel warningLabel;

	/**
	 * Zuordnung aller Tabellennamen zu allen jeweiligen Spaltennamen
	 */
	private Map<String,List<String>> columns;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementInputDB}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementInputDBDialog(final Component owner, final ModelElementInputDB element, final boolean readOnly) {
		super(owner,Language.tr("Surface.InputDB.Dialog.Title"),element,"ModelElementInputDB",readOnly);
	}

	/**
	 * Stellt die Größe des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(800,0);
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationInputDB;
	}

	@Override
	protected JComponent getContentPanel() {
		final ModelElementInputDB input=(ModelElementInputDB)this.element;

		JPanel line;
		JLabel label;
		Object[] data;

		/* Datenbank */

		final JPanel content=new JPanel();
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));
		content.add(db=new DBSettingsPanel(input.getDb(),readOnly));
		db.addChangeListener(()->dbSettingsChanged());

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(errorLabel=new JLabel(Language.tr("Surface.InputDB.Dialog.NoConnection")));
		errorLabel.setVisible(false);

		/* Tabelle */

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Surface.InputDB.Dialog.Table")+":"));
		line.add(comboTable=new JComboBox<>(new String[]{input.getTable()}));
		label.setLabelFor(comboTable);
		comboTable.addActionListener(e->tableChanged());

		/* Spaltenangaben */

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Surface.InputDB.Dialog.ColumnLoad")+":"));
		line.add(comboLoad=new JComboBox<>(new String[]{input.getLoadColumn()}));
		label.setLabelFor(comboLoad);

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Surface.InputDB.Dialog.ColumnSort")+":"));
		line.add(comboSort=new JComboBox<>(new String[]{input.getSortColumn()}));
		label.setLabelFor(comboSort);

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Surface.InputDB.Dialog.ColumnSortMode")+":"));
		line.add(comboSortMode=new JComboBox<>(new String[]{
				Language.tr("Surface.InputDB.Dialog.ColumnSortMode.Ascending"),
				Language.tr("Surface.InputDB.Dialog.ColumnSortMode.Descending")
		}));
		comboSortMode.setRenderer(new IconListCellRenderer(new Images[]{
				Images.ARROW_UP,
				Images.ARROW_DOWN
		}));
		switch (input.getSortMode()) {
		case ASCENDING: comboSortMode.setSelectedIndex(0); break;
		case DESCENDING: comboSortMode.setSelectedIndex(1); break;
		default: comboSortMode.setSelectedIndex(0); break;
		}
		label.setLabelFor(comboSortMode);

		/* EOF-Modus (& Default-Value) */

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionSkip=new JRadioButton(Language.tr("Surface.InputDB.Dialog.Mode.Skip"),input.getEofMode()==ModelElementInputDB.EofModes.EOF_MODE_SKIP));
		optionSkip.setEnabled(!readOnly);
		optionSkip.addActionListener(e->checkData(false));

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionDefaultValue=new JRadioButton(Language.tr("Surface.InputDB.Dialog.Mode.DefaultValue")+":",input.getEofMode()==ModelElementInputDB.EofModes.EOF_MODE_DEFAULT_VALUE));
		optionDefaultValue.setEnabled(!readOnly);
		optionDefaultValue.addActionListener(e->checkData(false));
		line.add(defaultValueEdit=new JTextField(input.getDefaultValue(),10));
		defaultValueEdit.setEditable(!readOnly);
		defaultValueEdit.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {optionDefaultValue.setSelected(true); checkData(false);}
			@Override public void keyReleased(KeyEvent e) {optionDefaultValue.setSelected(true); checkData(false);}
			@Override public void keyPressed(KeyEvent e) {optionDefaultValue.setSelected(true); checkData(false);}
		});

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionLoop=new JRadioButton(Language.tr("Surface.InputDB.Dialog.Mode.Loop"),input.getEofMode()==ModelElementInputDB.EofModes.EOF_MODE_LOOP));
		optionLoop.setEnabled(!readOnly);
		optionLoop.addActionListener(e->checkData(false));

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionTerminate=new JRadioButton(Language.tr("Surface.InputDB.Dialog.Mode.Terminate"),input.getEofMode()==ModelElementInputDB.EofModes.EOF_MODE_TERMINATE));
		optionTerminate.setEnabled(!readOnly);
		optionTerminate.addActionListener(e->checkData(false));

		final ButtonGroup buttonGroup=new ButtonGroup();
		buttonGroup.add(optionSkip);
		buttonGroup.add(optionDefaultValue);
		buttonGroup.add(optionLoop);
		buttonGroup.add(optionTerminate);

		/* Variable */

		data=getInputPanel(Language.tr("Surface.InputDB.Dialog.Variable")+":",input.getVariable());
		content.add(line=(JPanel)data[0]);
		variableEdit=(JTextField)data[1];
		variableEdit.setEditable(!readOnly);
		variableEdit.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(warningLabel=new JLabel(""));
		warningLabel.setVisible(false);

		/* Start */

		dbSettingsChanged();

		return content;
	}

	/**
	 * Trägt neue Einträge in eine Auswahlbox ein
	 * und versucht den zuvor gewählten Eintrag
	 * wieder auszuwählen.
	 * @param combo	Auswahlbox
	 * @param items	Neue Einträge (kann <code>null</code> sein)
	 */
	private void changeComboAndRestore(final JComboBox<String> combo, final String[] items) {
		String last=null;
		if (combo.getSelectedIndex()>=0) last=(String)combo.getSelectedItem();
		if (items==null) {
			combo.setModel(new DefaultComboBoxModel<>(new String[0]));
		} else {
			combo.setModel(new DefaultComboBoxModel<>(items));
			int index=-1;
			if (last!=null) for (int i=0;i<items.length;i++) if (items[i].equalsIgnoreCase(last)) {index=i; break;}
			if (index<0 && items.length>0) index=0;
			if (index>=0) combo.setSelectedIndex(index);
		}
	}

	/**
	 * Wird aufgerufen, wenn die Verbindungseinstellungen
	 * zu der Datenbank verändert wurden.
	 * @see #db
	 */
	private void dbSettingsChanged() {
		try (DBConnect connect=new DBConnect(db.storeToCopy(),false)) {
			if (connect.getInitError()!=null) {
				errorLabel.setVisible(true);
				comboTable.setEnabled(false);
				columns=null;
			} else {
				errorLabel.setVisible(false);
				final String[] tables=connect.listTables();
				columns=connect.listAll();
				changeComboAndRestore(comboTable,tables);
				comboTable.setEnabled(!readOnly);
			}
		}

		tableChanged();
	}

	/**
	 * Wird aufgerufen, wenn in {@link #comboTable}
	 * eine andere Tabelle gewählt wurde.
	 * @see #comboTable
	 */
	private void tableChanged() {
		List<String> cols=new ArrayList<>();
		if (columns!=null) {
			if (comboTable.getModel().getSize()>0 && comboTable.getSelectedIndex()>=0) cols=columns.get(comboTable.getSelectedItem());
		}

		changeComboAndRestore(comboLoad,cols.toArray(new String[0]));
		comboLoad.setEnabled(!readOnly && cols.size()>0);

		final List<String> cols2=new ArrayList<>(cols);
		cols2.add(0,"<"+Language.tr("Surface.InputDB.Dialog.ColumnSort.DoNotUse")+">");
		changeComboAndRestore(comboSort,cols2.toArray(new String[0]));
		comboSort.setEnabled(!readOnly);

		comboSortMode.setEnabled(!readOnly);
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessages	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessages) {
		boolean ok=true;

		/* Tabelle */

		if (!comboTable.isEnabled() || comboTable.getSelectedIndex()<0) {
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.InputDB.Dialog.Table.ErrorTitle"),Language.tr("Surface.InputDB.Dialog.Table.ErrorInfo"));
				return false;
			}
		}

		/* Zu ladende Spalte */

		if (comboLoad.getModel().getSize()==0 || comboLoad.getSelectedIndex()<0) {
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.InputDB.Dialog.ColumnLoad.ErrorTitle"),Language.tr("Surface.InputDB.Dialog.ColumnLoad.ErrorInfo"));
				return false;
			}
		}

		/* Vorgabewert */

		if (optionDefaultValue.isSelected() && CalcSymbolClientUserData.testClientDataString(variableEdit.getText())==null) {
			final Double D=NumberTools.getDouble(defaultValueEdit,true);
			if (D==null) {
				ok=false;
				if (showErrorMessages) {
					MsgBox.error(this,Language.tr("Surface.InputDB.Dialog.DefaultValue.ErrorTitle"),String.format(Language.tr("Surface.InputDB.Dialog.DefaultValue.ErrorInfo"),defaultValueEdit.getText()));
					return false;
				}
			}
		} else {
			defaultValueEdit.setBackground(SystemColor.text);
		}

		/* Variable */

		final String variable=variableEdit.getText();
		boolean varNameOk=true;
		if (CalcSymbolClientUserData.testClientData(variable)>=0) {
			warningLabel.setVisible(false);
			/* varNameOk bleibt true */
		} else {
			if (CalcSymbolClientUserData.testClientDataString(variable)!=null) {
				warningLabel.setVisible(false);
				/* varNameOk bleibt true */
			} else {
				varNameOk=ExpressionCalc.checkVariableName(variable);
				String warning=null;
				if (variable.trim().equalsIgnoreCase("w")) warning=Language.tr("Surface.InputDB.Dialog.Variable.WaitingTime");
				if (variable.trim().equalsIgnoreCase("t")) warning=Language.tr("Surface.InputDB.Dialog.Variable.TransferTime");
				if (variable.trim().equalsIgnoreCase("p")) warning=Language.tr("Surface.InputDB.Dialog.Variable.ProcessTime");
				if (warning!=null) warningLabel.setText(warning);
				warningLabel.setVisible(warning!=null);
			}
		}
		pack();

		if (varNameOk) {
			variableEdit.setBackground(SystemColor.text);
		} else {
			variableEdit.setBackground(Color.red);
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.InputDB.Dialog.Variable.ErrorTitle"),String.format(Language.tr("Surface.InputDB.Dialog.Variable.ErrorInfo"),variable));
				return false;
			}
			ok=false;
		}

		return ok;
	}

	@Override
	protected boolean checkData() {
		if (readOnly) return false;
		return checkData(true);
	}

	@Override
	protected void storeData() {
		super.storeData();

		final ModelElementInputDB input=(ModelElementInputDB)this.element;

		/* Datenbank */

		db.storeData();

		/* Tabelle und Spalte */

		input.setTable((String)comboTable.getSelectedItem());
		input.setLoadColumn((String)comboLoad.getSelectedItem());

		if (comboSort.getSelectedIndex()==0) {
			input.setSortColumn("");
		} else {
			input.setSortColumn((String)comboSort.getSelectedItem());
		}

		switch (comboSortMode.getSelectedIndex()) {
		case 0: input.setSortMode(DBConnect.SortMode.ASCENDING); break;
		case 1: input.setSortMode(DBConnect.SortMode.DESCENDING); break;
		}

		/* EOF-Modus (& Default-Value) */

		if (optionSkip.isSelected()) {
			input.setEofMode(ModelElementInputDB.EofModes.EOF_MODE_SKIP);
		}
		if (optionDefaultValue.isSelected()) {
			input.setEofMode(ModelElementInputDB.EofModes.EOF_MODE_DEFAULT_VALUE);
			input.setDefaultValue(defaultValueEdit.getText());
		}
		if (optionLoop.isSelected()) {
			input.setEofMode(ModelElementInputDB.EofModes.EOF_MODE_LOOP);
		}
		if (optionTerminate.isSelected()) {
			input.setEofMode(ModelElementInputDB.EofModes.EOF_MODE_TERMINATE);
		}

		/* Variable */

		input.setVariable(variableEdit.getText());
	}
}

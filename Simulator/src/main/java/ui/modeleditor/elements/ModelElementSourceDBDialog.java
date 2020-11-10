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
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import language.Language;
import simulator.db.DBConnect;
import simulator.db.DBSettingsPanel;
import systemtools.MsgBox;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementSourceDB}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementSourceDB
 */
public class ModelElementSourceDBDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 5793850421024606739L;

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
	 * Auswahlbox für in der Tabelle zu ladende Spalte (mit Ankunftszeiten)
	 */
	private JComboBox<String> comboLoad;

	/**
	 * Auswahlbox für die Kundentypen-Spalte in der Tabelle
	 */
	private JComboBox<String> comboClientType;

	/**
	 * Auswahlbox für die Kundendatenspalte-Spalte in der Tabelle
	 */
	private JComboBox<String> comboInfo;

	/**
	 * Eingabebereich für die Namen der zu ladenden Kundentypen
	 */
	private JTextArea clientsEdit;

	/**
	 * Zuordnung aller Tabellennamen zu allen jeweiligen Spaltennamen
	 */
	private Map<String,List<String>> columns;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementSourceDB}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementSourceDBDialog(final Component owner, final ModelElementSourceDB element, final boolean readOnly) {
		super(owner,Language.tr("Surface.SourceDB.Dialog.Title"),element,"ModelElementSourceDB",readOnly);
	}

	/**
	 * Stellt die Größe des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(550,650);
		pack();
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationSourceDB;
	}

	@Override
	protected JComponent getContentPanel() {
		final ModelElementSourceDB source=(ModelElementSourceDB)this.element;

		final JPanel content=new JPanel(new BorderLayout());

		JPanel line;
		JLabel label;

		/* Oben: Datenbank */

		final JPanel top=new JPanel();
		content.add(top,BorderLayout.NORTH);
		top.setLayout(new BoxLayout(top,BoxLayout.PAGE_AXIS));
		top.add(db=new DBSettingsPanel(source.getDb(),readOnly));
		db.addChangeListener(()->dbSettingsChanged());

		top.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(errorLabel=new JLabel(Language.tr("Surface.SourceDB.Dialog.NoConnection")));
		errorLabel.setVisible(false);

		top.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Surface.SourceDB.Dialog.Table")+":"));
		line.add(comboTable=new JComboBox<>(new String[]{source.getTable()}));
		label.setLabelFor(comboTable);
		comboTable.addActionListener(e->tableChanged());

		top.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Surface.SourceDB.Dialog.ColumnLoad")+":"));
		line.add(comboLoad=new JComboBox<>(new String[]{source.getLoadColumn()}));
		label.setLabelFor(comboLoad);

		top.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Surface.SourceDB.Dialog.ColumnClientType")+":"));
		line.add(comboClientType=new JComboBox<>(new String[]{source.getClientTypeColumn()}));
		label.setLabelFor(comboClientType);

		top.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Surface.SourceDB.Dialog.ColumnInfo")+":"));
		line.add(comboInfo=new JComboBox<>(new String[]{source.getInfoColumn()}));
		label.setLabelFor(comboInfo);

		/* Center: Kundentypnamen */

		top.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel(Language.tr("Surface.SourceDB.Dialog.ClientTypes")+":"));

		content.add(new JScrollPane(clientsEdit=new JTextArea("")),BorderLayout.CENTER);
		clientsEdit.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkClients(false);}
			@Override public void keyReleased(KeyEvent e) {checkClients(false);}
			@Override public void keyPressed(KeyEvent e) {checkClients(false);}
		});
		clientsEdit.setText(String.join("\n",source.getNewClientTypes()).trim());
		clientsEdit.setEditable(!readOnly);
		addUndoFeature(clientsEdit);

		/* Start */

		dbSettingsChanged();
		checkClients(false);

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

		changeComboAndRestore(comboClientType,cols.toArray(new String[0]));
		comboClientType.setEnabled(!readOnly && cols.size()>0);

		final List<String> cols2=new ArrayList<>(cols);
		cols2.add(0,"<"+Language.tr("Surface.SourceDB.Dialog.ColumnInfo.DoNotUse")+">");
		changeComboAndRestore(comboInfo,cols2.toArray(new String[0]));
		comboInfo.setEnabled(!readOnly);
	}

	/**
	 * Prüft die Eingaben in {@link #clientsEdit} auf Gültigkeit.
	 * @param showErrorMessages	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkClients(final boolean showErrorMessages) {
		if (clientsEdit.getText().trim().isEmpty()) {
			clientsEdit.setBackground(Color.RED);
			if (showErrorMessages) MsgBox.error(this,Language.tr("Surface.SourceDB.Dialog.ClientTypes.ErrorTitle"),Language.tr("Surface.SourceDB.Dialog.ClientTypes.ErrorInfo"));
			return false;
		} else {
			clientsEdit.setBackground(SystemColor.text);
			return true;
		}
	}

	@Override
	protected boolean checkData() {
		if (readOnly) return false;

		if (!comboTable.isEnabled() || comboTable.getSelectedIndex()<0) {
			MsgBox.error(this,Language.tr("Surface.SourceDB.Dialog.Table.ErrorTitle"),Language.tr("Surface.SourceDB.Dialog.Table.ErrorInfo"));
			return false;
		}

		if (comboLoad.getModel().getSize()==0 || comboLoad.getSelectedIndex()<0) {
			MsgBox.error(this,Language.tr("Surface.SourceDB.Dialog.ColumnLoad.ErrorTitle"),Language.tr("Surface.SourceDB.Dialog.ColumnLoad.ErrorInfo"));
			return false;
		}

		if (comboClientType.getModel().getSize()==0 || comboClientType.getSelectedIndex()<0) {
			MsgBox.error(this,Language.tr("Surface.SourceDB.Dialog.ColumnClientType.ErrorTitle"),Language.tr("Surface.SourceDB.Dialog.ColumnClientType.ErrorInfo"));
			return false;
		}

		return checkClients(true);
	}

	@Override
	protected void storeData() {
		super.storeData();

		final ModelElementSourceDB source=(ModelElementSourceDB)this.element;

		db.storeData();

		source.setTable((String)comboTable.getSelectedItem());
		source.setLoadColumn((String)comboLoad.getSelectedItem());
		source.setClientTypeColumn((String)comboClientType.getSelectedItem());
		if (comboInfo.getSelectedIndex()==0) {
			source.setInfoColumn("");
		} else {
			source.setInfoColumn((String)comboInfo.getSelectedItem());
		}

		final String s=clientsEdit.getText().trim();
		final String[] lines=s.split("\n");
		source.getClientTypeNames().clear();
		for (String line: lines) if (!line.trim().isEmpty()) source.getClientTypeNames().add(line.trim());
	}
}
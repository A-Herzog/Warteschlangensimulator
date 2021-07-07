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
import java.awt.Component;
import java.awt.FlowLayout;
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

import language.Language;
import simulator.db.DBConnect;
import simulator.db.DBSettingsPanel;
import systemtools.MsgBox;
import tools.JTableExt;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementOutputDB}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementOutputDB
 */
public class ModelElementOutputDBDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -4202107646574026037L;

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
	 * Zuordnung aller Tabellennamen zu allen jeweiligen Spaltennamen
	 */
	private Map<String,List<String>> columns;

	/**
	 * Tabelle zur Konfiguration der auszugebenden Daten
	 */
	private OutputDBTableModel tableModel;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementOutputDB}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementOutputDBDialog(final Component owner, final ModelElementOutputDB element, final boolean readOnly) {
		super(owner,Language.tr("Surface.OutputDB.Dialog.Title"),element,"ModelElementOutputDB",readOnly);
	}

	/**
	 * Stellt die Größe des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(800,600);
		pack();
		setResizable(true);
		setMaxSizeRespectingScreensize(1024,768);
	}

	/**
	 * Stellt die Größe des Dialogfensters unmittelbar vor dem Sicherbarmachen ein.
	 */
	@Override
	protected void setDialogSizeLater() {
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationOutputDB;
	}

	@Override
	protected JComponent getContentPanel() {
		final JPanel content=new JPanel(new BorderLayout());

		if (element instanceof ModelElementOutputDB) {
			final JPanel upperPanel=new JPanel();
			content.add(upperPanel,BorderLayout.NORTH);
			upperPanel.setLayout(new BoxLayout(upperPanel,BoxLayout.PAGE_AXIS));

			JPanel line;
			JLabel label;

			/* Datenbank */

			upperPanel.add(db=new DBSettingsPanel(((ModelElementOutputDB)element).getDb(),readOnly));
			db.addChangeListener(()->dbSettingsChanged());

			upperPanel.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			line.add(errorLabel=new JLabel(Language.tr("Surface.OutputDB.Dialog.NoConnection")));
			errorLabel.setVisible(false);

			/* Datenbanktabelle */

			upperPanel.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			line.add(label=new JLabel(Language.tr("Surface.OutputDB.Dialog.Table")+":"));
			line.add(comboTable=new JComboBox<>(new String[]{((ModelElementOutputDB)element).getTable()}));
			label.setLabelFor(comboTable);
			comboTable.addActionListener(e->tableChanged());

			/* Main-Area */

			final JTableExt table=new JTableExt();

			table.setModel(tableModel=new OutputDBTableModel(table,((ModelElementOutputDB)element).getModes(),((ModelElementOutputDB)element).getColumns(),((ModelElementOutputDB)element).getData(),element.getSurface().getMainSurfaceVariableNames(element.getModel().getModelVariableNames(),true),readOnly));
			table.setIsPanelCellTable(0);
			table.setIsPanelCellTable(1);
			table.setIsPanelCellTable(2);
			table.getColumnModel().getColumn(1).setMaxWidth(300);
			table.getColumnModel().getColumn(1).setMinWidth(300);
			table.getColumnModel().getColumn(2).setMaxWidth(150);
			table.getColumnModel().getColumn(2).setMinWidth(150);
			table.setEnabled(!readOnly);
			content.add(new JScrollPane(table),BorderLayout.CENTER);

			/* Start */

			dbSettingsChanged();
		}

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

		tableModel.updateDBColumnNames(cols);
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
			MsgBox.error(this,Language.tr("Surface.InputDB.Dialog.Table.ErrorTitle"),Language.tr("Surface.InputDB.Dialog.Table.ErrorInfo"));
			return false;
		}

		return ok;
	}

	@Override
	protected boolean checkData() {
		if (readOnly) return false;
		return checkData(true);
	}

	/**
	 * Speichert die Dialog-Daten in dem zugehörigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see #checkData()
	 */
	@Override
	protected void storeData() {
		super.storeData();

		if (element instanceof ModelElementOutputDB) {

			/* Datenbank */

			db.storeData();

			/* Tabelle */

			((ModelElementOutputDB)element).setTable((String)comboTable.getSelectedItem());

			/* Elemente */

			final List<ModelElementOutputDB.OutputMode> modes=((ModelElementOutputDB)element).getModes();
			modes.clear();
			modes.addAll(tableModel.getModes());
			final List<String> column=((ModelElementOutputDB)element).getColumns();
			column.clear();
			column.addAll(tableModel.getColumns());
			final List<String> data=((ModelElementOutputDB)element).getData();
			data.clear();
			data.addAll(tableModel.getData());
			while (data.size()<modes.size()) data.add("");
			while (column.size()<modes.size()) column.add("");
		}
	}
}

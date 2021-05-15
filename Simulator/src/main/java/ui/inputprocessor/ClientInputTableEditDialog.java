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
package ui.inputprocessor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import mathtools.Table;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.IconListCellRenderer;
import ui.help.Help;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * In diesem Dialog kann die Konfiguration für eine einzelne zu verarbeitende
 * Spalte beim Vorbereiten einer Tabelle für die Nutzung in einer Tabellenquelle
 * geändert werden.
 * @author Alexander Herzog
 * @see ClientInputTableDialog
 */
public class ClientInputTableEditDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-1624800560040584731L;

	/**
	 * Zu bearbeitende Spaltenkonfiguration
	 */
	private final ClientInputTableProcessor.ColumnSetup column;

	/**
	 * Wie soll die Spalte verwendet werden?
	 */
	private final JComboBox<String> mode;

	/**
	 * Panel welches das Eingabefeld {@link #index} und das zugehörige Label enthält
	 * @see #modeChanged()
	 */
	private final JPanel indexPanel;

	/**
	 * Zu verwendender Kundendaten-Index
	 */
	private final JTextField index;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param column	Zu bearbeitende Spaltenkonfiguration (wird beim Schließen mit "Ok" direkt geändert)
	 */
	public ClientInputTableEditDialog(final Component owner, final ClientInputTableProcessor.ColumnSetup column) {
		super(owner,Language.tr("BuildClientSourceTable.Edit.Title"));
		this.column=column;

		JPanel line;

		/* GUI */
		final JPanel all=createGUI(()->Help.topicModal(this,"ProcessClientTable"));
		all.setLayout(new BorderLayout());

		final JPanel content=new JPanel();
		all.add(content,BorderLayout.NORTH);
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		/* Informationen */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel("<html><body>"+Language.tr("BuildClientSourceTable.Edit.Column")+": <b>"+Table.columnNameFromNumber(column.colNr)+"</b></body></html>"));

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel("<html><body>"+Language.tr("BuildClientSourceTable.Edit.Name")+": <b>"+column.name+"</b></body></html>"));

		/* Modus */
		final String[] modes;
		final Images[] modesIcons;

		if (column.isNumeric) {
			modes=new String[] {
					Language.tr("BuildClientSourceTable.Edit.Mode.Off"),
					Language.tr("BuildClientSourceTable.Edit.Mode.Arrivals"),
					Language.tr("BuildClientSourceTable.Edit.Mode.ClientType"),
					Language.tr("BuildClientSourceTable.Edit.Mode.Number"),
					Language.tr("BuildClientSourceTable.Edit.Mode.Text"),
			};
			modesIcons=new Images[] {
					Images.GENERAL_OFF,
					Images.GENERAL_TIME,
					Images.MODELPROPERTIES_CLIENTS,
					Images.GENERAL_NUMBERS,
					Images.GENERAL_FONT
			};
		} else {
			modes=new String[] {
					Language.tr("BuildClientSourceTable.Edit.Mode.Off"),
					Language.tr("BuildClientSourceTable.Edit.Mode.Arrivals"),
					Language.tr("BuildClientSourceTable.Edit.Mode.ClientType"),
					Language.tr("BuildClientSourceTable.Edit.Mode.Text")
			};
			modesIcons=new Images[] {
					Images.GENERAL_OFF,
					Images.GENERAL_TIME,
					Images.MODELPROPERTIES_CLIENTS,
					Images.GENERAL_FONT
			};
		}

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		final JLabel label=new JLabel(Language.tr("BuildClientSourceTable.Edit.Mode")+":");
		line.add(label);
		line.add(mode=new JComboBox<>(modes));
		mode.setRenderer(new IconListCellRenderer(modesIcons));
		label.setLabelFor(mode);
		switch (column.mode) {
		case OFF: mode.setSelectedIndex(0); break;
		case ARRIVALS: mode.setSelectedIndex(1); break;
		case CLIENT_TYPES: mode.setSelectedIndex(2); break;
		case NUMBER: mode.setSelectedIndex(3); break;
		case TEXT: mode.setSelectedIndex(column.isNumeric?2:1); break;
		}
		mode.addActionListener(e->modeChanged());

		/* Index wählen */
		final Object[] data=ModelElementBaseDialog.getInputPanel(Language.tr("BuildClientSourceTable.Edit.Index")+":",""+column.index,5);
		content.add(indexPanel=(JPanel)data[0]);
		index=(JTextField)data[1];
		index.addKeyListener(new KeyAdapter() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		/* Dialog starten */
		pack();
		modeChanged();
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	/**
	 * Wird aufgerufen, wenn sich die Auswahl in {@link #mode} ändert.
	 * @see #mode
	 */
	private void modeChanged() {
		indexPanel.setVisible(column.isNumeric && mode.getSelectedIndex()==3);
		checkData(false);
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessage) {
		if (!column.isNumeric || mode.getSelectedIndex()!=1) return true;

		boolean ok=true;

		final Long L=NumberTools.getNotNegativeLong(index,true);
		if (L==null) {
			ok=false;
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("BuildClientSourceTable.Edit.Index.Error.Title"),String.format(Language.tr("BuildClientSourceTable.Edit.Index.Error.Info"),index.getText()));
				return false;
			}
		}

		return ok;
	}

	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	@Override
	protected void storeData() {
		switch (mode.getSelectedIndex()) {
		case 0: column.mode=ClientInputTableProcessor.ColumnMode.OFF; break;
		case 1: column.mode=ClientInputTableProcessor.ColumnMode.ARRIVALS; break;
		case 2: column.mode=ClientInputTableProcessor.ColumnMode.CLIENT_TYPES; break;
		case 3: column.mode=column.isNumeric?ClientInputTableProcessor.ColumnMode.NUMBER:ClientInputTableProcessor.ColumnMode.TEXT; break;
		case 4: column.mode=ClientInputTableProcessor.ColumnMode.TEXT; break;
		}

		if (column.isNumeric && mode.getSelectedIndex()==3) {
			column.index=NumberTools.getNotNegativeLong(index,true).intValue();
		}
	}
}

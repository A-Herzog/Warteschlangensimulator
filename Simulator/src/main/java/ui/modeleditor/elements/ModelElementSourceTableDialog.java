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
import java.io.File;
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import language.Language;
import mathtools.Table;
import systemtools.MsgBox;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementSourceTable}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementSourceTable
 */
public class ModelElementSourceTableDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 6808289592022606483L;

	/**
	 * Eingabefeld für den Dateinamen der Tabelle
	 */
	private JTextField tableEdit;

	/**
	 * Option: Zeitangabenspalte beschreibt Zwischenankunftszeiten (Alternative: feste Zeitpunkte)
	 */
	private JRadioButton optionDistances;

	/**
	 * Eingabebereich für die Namen der zu ladenden Kundentypen
	 */
	private JTextArea clientsEdit;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementSourceTable}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementSourceTableDialog(final Component owner, final ModelElementSourceTable element, final boolean readOnly) {
		super(owner,Language.tr("Surface.SourceTable.Dialog.Title"),element,"ModelElementSourceTable",readOnly);
	}

	/**
	 * Stellt die Größe des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(550,450);
		pack();
		if (getHeight()>750) setSize(getWidth(),750);
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationSourceTable;
	}

	@Override
	protected JComponent getContentPanel() {
		final ModelElementSourceTable source=(ModelElementSourceTable)this.element;

		final JPanel content=new JPanel(new BorderLayout());
		JPanel line;

		/* Top-Area */

		final JPanel top=new JPanel();
		content.add(top,BorderLayout.NORTH);
		top.setLayout(new BoxLayout(top,BoxLayout.PAGE_AXIS));

		/* Tabellendatei */

		final Object[] data=getInputPanel(Language.tr("Surface.SourceTable.Dialog.Table")+":",source.getInputFile());
		top.add((JPanel)data[0]);
		tableEdit=(JTextField)data[1];
		tableEdit.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		tableEdit.setEditable(!readOnly);
		final JButton button=new JButton();
		((JPanel)data[0]).add(button,BorderLayout.EAST);
		button.setToolTipText(Language.tr("Surface.SourceTable.Dialog.Table.Tooltip"));
		button.setIcon(Images.GENERAL_SELECT_FILE.getIcon());
		button.addActionListener(e->{
			final File newTable=Table.showLoadDialog(this,Language.tr("Surface.SourceTable.Dialog.Table.Tooltip"));
			if (newTable!=null) {
				tableEdit.setText(newTable.toString());
				checkData(false);
			}
		});

		/* Radiobuttons für Tabellenspalte 1 */

		final JRadioButton optionTimeStamps;
		line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		top.add(line);
		line.add(optionTimeStamps=new JRadioButton(Language.tr("Surface.SourceTable.Dialog.ColumnOne.TimeStamps"),!source.isNumbersAreDistances()));
		optionTimeStamps.setEnabled(!readOnly);

		line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		top.add(line);
		line.add(optionDistances=new JRadioButton(Language.tr("Surface.SourceTable.Dialog.ColumnOne.Distances"),source.isNumbersAreDistances()));
		optionDistances.setEnabled(!readOnly);

		final ButtonGroup buttonGroup=new ButtonGroup();
		buttonGroup.add(optionTimeStamps);
		buttonGroup.add(optionDistances);

		/* Infotext über Center-Area */

		line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		top.add(line);
		line.add(new JLabel(Language.tr("Surface.SourceTable.Dialog.ClientTypes")+":"));

		/* Center-Area */

		content.add(new JScrollPane(clientsEdit=new JTextArea("")),BorderLayout.CENTER);
		clientsEdit.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		clientsEdit.setText(String.join("\n",source.getNewClientTypes()).trim());
		clientsEdit.setEditable(!readOnly);
		addUndoFeature(clientsEdit);

		checkData(false);

		return content;
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessages	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessages) {
		boolean ok=true;

		if (tableEdit.getText().trim().isEmpty()) {
			tableEdit.setBackground(Color.RED);
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.SourceTable.Dialog.Table.ErrorTitle"),Language.tr("Surface.SourceTable.Dialog.Table.ErrorInfo"));
				return false;
			}
		} else {
			tableEdit.setBackground(SystemColor.text);
		}

		if (clientsEdit.getText().trim().isEmpty()) {
			clientsEdit.setBackground(Color.RED);
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.SourceTable.Dialog.ClientTypes.ErrorTitle"),Language.tr("Surface.SourceTable.Dialog.ClientTypes.ErrorInfo"));
				return false;
			}
		} else {
			clientsEdit.setBackground(SystemColor.text);
		}

		return ok;
	}

	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	@Override
	protected void storeData() {
		super.storeData();

		final ModelElementSourceTable source=(ModelElementSourceTable)this.element;
		source.setInputFile(tableEdit.getText().trim());
		source.setNumbersAreDistances(optionDistances.isSelected());
		final String s=clientsEdit.getText().trim();
		final String[] lines=s.split("\n");
		source.getClientTypeNames().clear();
		for (String line: lines) if (!line.trim().isEmpty()) source.getClientTypeNames().add(line.trim());
	}
}

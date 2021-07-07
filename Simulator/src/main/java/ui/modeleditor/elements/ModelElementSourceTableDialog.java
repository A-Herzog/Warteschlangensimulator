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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import mathtools.NumberTools;
import mathtools.Table;
import mathtools.distribution.tools.FileDropper;
import systemtools.MsgBox;
import ui.dialogs.WaitDialog;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.inputprocessor.ClientInputTableDialog;
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
		setMinSizeRespectingScreensize(550,750);
		pack();
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

		/* Hinweis zu Einschwingphase */

		final JPanel warmUpAdvice=getWarmUpInfoPanel();
		if (warmUpAdvice!=null) top.add(warmUpAdvice);

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

		final JPanel tools=new JPanel(new FlowLayout(FlowLayout.LEFT));
		((JPanel)data[0]).add(tools,BorderLayout.EAST);
		JButton button;

		tools.add(button=new JButton());
		button.setToolTipText(Language.tr("Surface.SourceTable.Dialog.Table.Tooltip"));
		button.setIcon(Images.GENERAL_SELECT_FILE.getIcon());
		button.addActionListener(e->{
			final File newTable=Table.showLoadDialog(this,Language.tr("Surface.SourceTable.Dialog.Table.Tooltip"));
			if (newTable!=null) {
				tableEdit.setText(newTable.toString());
				checkData(false);
			}
		});

		tools.add(button=new JButton());
		button.setToolTipText(Language.tr("Surface.SourceTable.Dialog.Table.Process"));
		button.setIcon(Images.GENERAL_TOOLS.getIcon());
		button.addActionListener(e->	new ClientInputTableDialog(ModelElementSourceTableDialog.this));

		FileDropper.addFileDropper(this,tableEdit);

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

		/* Button: Kundentypen aus Tabelle laden */

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.SOUTH);
		line.add(button=new JButton(Language.tr("Surface.SourceTable.Dialog.ClientTypes.LoadButton"),Images.MODELPROPERTIES_CLIENTS.getIcon()));
		button.setToolTipText(Language.tr("Surface.SourceTable.Dialog.ClientTypes.LoadButton.Hint"));
		button.addActionListener(e->commandLoadClientTypes());

		/* Start */

		checkData(false);

		return content;
	}

	/**
	 * Lädt die Kundentypen aus der angegebenen Tabelle und ersetzt damit
	 * (nach Bestätigungsrückfrage) die bisherige Kundentypenliste.
	 */
	private void commandLoadClientTypes() {
		/* Tabelle laden */
		final String tableName=tableEdit.getText().trim();
		if (tableName.isEmpty()) {
			MsgBox.error(this,Language.tr("Surface.SourceTable.Dialog.ClientTypes.LoadButton"),Language.tr("Surface.SourceTable.Dialog.ClientTypes.LoadButton.ErrorNoTable"));
			return;
		}
		final File tableFile=new File(tableName);
		if (!tableFile.isFile()) {
			MsgBox.error(this,Language.tr("Surface.SourceTable.Dialog.ClientTypes.LoadButton"),Language.tr("Surface.SourceTable.Dialog.ClientTypes.LoadButton.ErrorTableNotFound"));
			return;
		}
		final Table table=new Table();
		if (!WaitDialog.workBoolean(this,()->table.load(tableFile),WaitDialog.Mode.LOAD_DATA)) {
			MsgBox.error(this,Language.tr("Surface.SourceTable.Dialog.ClientTypes.LoadButton"),Language.tr("Surface.SourceTable.Dialog.ClientTypes.LoadButton.ErrorTableLoad"));
			return;
		}

		/* Kundentypen zusammenstellen */
		final Set<String> clientTypes=new HashSet<>();
		final int size=table.getSize(0);
		for (int i=0;i<size;i++) {
			final List<String> line=table.getLine(i);
			if (line==null || line.size()<2) continue;
			String cell=line.get(1);
			if (cell==null) continue;
			cell=cell.trim();
			if (cell.isEmpty()) continue;
			clientTypes.add(cell);
		}

		/* Bisherige Kundentypen ersetzen */
		if (clientTypes.size()==0) return;
		if (!clientsEdit.getText().trim().isEmpty()) {
			if (!MsgBox.confirm(this,Language.tr("Surface.SourceTable.Dialog.ClientTypes.LoadButton"),String.format(Language.tr("Surface.SourceTable.Dialog.ClientTypes.LoadButton.ReplaceConfirm"),clientTypes.size()),Language.tr("Surface.SourceTable.Dialog.ClientTypes.LoadButton.ReplaceConfirm.InfoYes"),Language.tr("Surface.SourceTable.Dialog.ClientTypes.LoadButton.ReplaceConfirm.InfoNo"))) return;
		}
		final StringBuilder text=new StringBuilder();
		for (String clientType: clientTypes) {
			if (text.length()>0) text.append("\n");
			text.append(clientType);
		}
		clientsEdit.setText(text.toString());
		checkData(false);
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
			tableEdit.setBackground(NumberTools.getTextFieldDefaultBackground());
		}

		if (clientsEdit.getText().trim().isEmpty()) {
			clientsEdit.setBackground(Color.RED);
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.SourceTable.Dialog.ClientTypes.ErrorTitle"),Language.tr("Surface.SourceTable.Dialog.ClientTypes.ErrorInfo"));
				return false;
			}
		} else {
			clientsEdit.setBackground(NumberTools.getTextFieldDefaultBackground());
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

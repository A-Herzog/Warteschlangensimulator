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

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import systemtools.MsgBox;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementMatch}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementMatch
 */
public class ModelElementMatchDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -2581147688988297708L;

	private JRadioButton optionForward;
	private JRadioButton optionTemporary;
	private JTextField tempTypeField;
	private JRadioButton optionNewType;
	private JTextField newTypeField;

	private JRadioButton optionPropertyNone;
	private JRadioButton optionPropertyNumber;
	private JRadioButton optionPropertyText;
	private JTextField optionPropertyNumberField;
	private JTextField optionPropertyTextField;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementMatch}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementMatchDialog(final Component owner, final ModelElementMatch element, final boolean readOnly) {
		super(owner,Language.tr("Surface.Match.Dialog.Title"),element,"ModelElementMatch",readOnly);
	}

	/**
	 * Stellt die Größe des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(600,0);
		pack();
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationMatch;
	}

	/**
	 * Erstellt und liefert das Panel, welches im Content-Bereich des Dialogs angezeigt werden soll
	 * @return	Panel mit den Dialogelementen
	 */
	@Override
	protected JComponent getContentPanel() {
		final JPanel content=new JPanel();
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		JPanel line;
		ButtonGroup buttonGroup;

		/* Batch-Bildungsoptionen */

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel("<html><body><b>"+Language.tr("Surface.Match.Dialog.OptionsBatching")+"</b></body></html>"));

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionForward=new JRadioButton(Language.tr("Surface.Match.Dialog.OptionJustForward")));
		optionForward.setEnabled(!readOnly);
		optionForward.addActionListener(e->checkData(false));

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionTemporary=new JRadioButton(Language.tr("Surface.Match.Dialog.SendTemporaryBatched")+":"));
		optionTemporary.setEnabled(!readOnly);
		optionTemporary.addActionListener(e->checkData(false));
		line.add(tempTypeField=new JTextField(25));
		tempTypeField.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {optionTemporary.setSelected(true);}
			@Override public void keyReleased(KeyEvent e) {optionTemporary.setSelected(true);}
			@Override public void keyPressed(KeyEvent e) {optionTemporary.setSelected(true);}
		});
		tempTypeField.setEditable(!readOnly);

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionNewType=new JRadioButton(Language.tr("Surface.Match.Dialog.OptionBatch")+":"));
		optionNewType.setEnabled(!readOnly);
		optionNewType.addActionListener(e->checkData(false));
		line.add(newTypeField=new JTextField(25));
		newTypeField.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {optionNewType.setSelected(true);}
			@Override public void keyReleased(KeyEvent e) {optionNewType.setSelected(true);}
			@Override public void keyPressed(KeyEvent e) {optionNewType.setSelected(true);}
		});
		newTypeField.setEditable(!readOnly);

		buttonGroup=new ButtonGroup();
		buttonGroup.add(optionForward);
		buttonGroup.add(optionTemporary);
		buttonGroup.add(optionNewType);

		/* Eigenschaftsabgleich */

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel("<html><body><b>"+Language.tr("Surface.Match.Dialog.OptionsPropertyMatch")+"</b></body></html>"));

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionPropertyNone=new JRadioButton(Language.tr("Surface.Match.Dialog.OptionProperty.Off")));
		optionPropertyNone.setEnabled(!readOnly);
		optionPropertyNone.addActionListener(e->checkData(false));

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionPropertyNumber=new JRadioButton(Language.tr("Surface.Match.Dialog.OptionProperty.Number")+":"));
		optionPropertyNumber.setEnabled(!readOnly);
		optionPropertyNumber.addActionListener(e->checkData(false));
		line.add(optionPropertyNumberField=new JTextField(5));
		optionPropertyNumberField.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {optionPropertyNumber.setSelected(true); checkData(false);}
			@Override public void keyReleased(KeyEvent e) {optionPropertyNumber.setSelected(true); checkData(false);}
			@Override public void keyPressed(KeyEvent e) {optionPropertyNumber.setSelected(true); checkData(false);}
		});
		optionPropertyNumberField.setEditable(!readOnly);

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionPropertyText=new JRadioButton(Language.tr("Surface.Match.Dialog.OptionProperty.Text")+":"));
		optionPropertyText.setEnabled(!readOnly);
		optionPropertyText.addActionListener(e->checkData(false));
		line.add(optionPropertyTextField=new JTextField(25));
		optionPropertyTextField.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {optionPropertyText.setSelected(true); checkData(false);}
			@Override public void keyReleased(KeyEvent e) {optionPropertyText.setSelected(true); checkData(false);}
			@Override public void keyPressed(KeyEvent e) {optionPropertyText.setSelected(true); checkData(false);}
		});
		optionPropertyTextField.setEditable(!readOnly);

		buttonGroup=new ButtonGroup();
		buttonGroup.add(optionPropertyNone);
		buttonGroup.add(optionPropertyNumber);
		buttonGroup.add(optionPropertyText);

		/* Daten laden */

		switch (((ModelElementMatch)element).getMatchMode()) {
		case MATCH_MODE_COLLECT:
			optionForward.setSelected(true);
			break;
		case MATCH_MODE_TEMPORARY:
			optionTemporary.setSelected(true);
			tempTypeField.setText(((ModelElementMatch)element).getNewClientType());
			break;
		case MATCH_MODE_PERMANENT:
			optionNewType.setSelected(true);
			newTypeField.setText(((ModelElementMatch)element).getNewClientType());
			break;
		}

		switch (((ModelElementMatch)element).getMatchPropertyMode()) {
		case NONE:
			optionPropertyNone.setSelected(true);
			break;
		case NUMBER:
			optionPropertyNumber.setSelected(true);
			optionPropertyNumberField.setText(""+((ModelElementMatch)element).getMatchPropertyNumber());
			break;
		case TEXT:
			optionPropertyText.setSelected(true);
			optionPropertyTextField.setText(((ModelElementMatch)element).getMatchPropertyString());
			break;
		}

		return content;
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessage) {
		boolean ok=true;

		if (optionNewType.isSelected() && newTypeField.getText().isEmpty()) {
			newTypeField.setBackground(Color.RED);
			ok=false;
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.Match.Dialog.OptionBatch.Error.Title"),Language.tr("Surface.Match.Dialog.OptionBatch.Error.Info"));
				return false;
			}
		} else {
			newTypeField.setBackground(SystemColor.text);
		}

		if (optionTemporary.isSelected() && tempTypeField.getText().isEmpty()) {
			tempTypeField.setBackground(Color.RED);
			ok=false;
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.Match.Dialog.SendTemporaryBatched.Error.Title"),Language.tr("Surface.Match.Dialog.SendTemporaryBatched.Error.Info"));
				return false;
			}
		} else {
			tempTypeField.setBackground(SystemColor.text);
		}

		if (optionPropertyNumber.isSelected()) {
			final Integer I=NumberTools.getNotNegativeInteger(optionPropertyNumberField,true);
			if (I==null) {
				ok=false;
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Match.Dialog.OptionProperty.Number.Error.Title"),Language.tr("Surface.Match.Dialog.OptionProperty.Number.Error.Info"));
					return false;
				}
			}
		}

		if (optionPropertyText.isSelected() && optionPropertyTextField.getText().isEmpty()) {
			optionPropertyTextField.setBackground(Color.RED);
			ok=false;
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.Match.Dialog.OptionProperty.Text.Error.Title"),Language.tr("Surface.Match.Dialog.OptionProperty.Text.Error.Info"));
				return false;
			}
		} else {
			optionPropertyTextField.setBackground(SystemColor.text);
		}

		return ok;
	}

	/**
	 * Wird beim Klicken auf "Ok" aufgerufen, um zu prüfen, ob die Daten in der aktuellen Form
	 * in Ordnung sind und gespeichert werden können.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	@Override
	protected boolean checkData() {
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
		final ModelElementMatch match=(ModelElementMatch)element;

		if (optionForward.isSelected()) match.setMatchMode(ModelElementMatch.MatchMode.MATCH_MODE_COLLECT);
		if (optionTemporary.isSelected()) {
			match.setMatchMode(ModelElementMatch.MatchMode.MATCH_MODE_TEMPORARY);
			match.setNewClientType(tempTypeField.getText());
		}
		if (optionNewType.isSelected()) {
			match.setMatchMode(ModelElementMatch.MatchMode.MATCH_MODE_PERMANENT);
			match.setNewClientType(newTypeField.getText());
		}

		if (optionPropertyNone.isSelected()) match.setMatchPropertyMode(ModelElementMatch.MatchPropertyMode.NONE);
		if (optionPropertyNumber.isSelected()) {
			match.setMatchPropertyMode(ModelElementMatch.MatchPropertyMode.NUMBER);
			match.setMatchPropertyNumber(NumberTools.getNotNegativeInteger(optionPropertyNumberField,true));
		}
		if (optionPropertyText.isSelected()) {
			match.setMatchPropertyMode(ModelElementMatch.MatchPropertyMode.TEXT);
			match.setMatchPropertyString(optionPropertyTextField.getText());
		}
	}
}

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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import simulator.simparser.ExpressionCalc;
import systemtools.MsgBox;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementInteractiveRadiobutton}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementInteractiveRadiobutton
 */
public class ModelElementInteractiveRadiobuttonDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -8196275879384809537L;

	/**
	 * Eingabefeld für den Namen der Variable, die mit dem Radiobutton verknüpft werden soll
	 */
	private JTextField editVariable;

	/**
	 * Eingabefeld für den Wert auf den die Variable beim Wählen des Radiobutton gesetzt werden soll
	 */
	private JTextField editValueChecked;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementInteractiveRadiobutton}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementInteractiveRadiobuttonDialog(final Component owner,final ModelElementInteractiveRadiobutton element, final boolean readOnly) {
		super(owner,Language.tr("Surface.InteractiveRadiobutton.Dialog.Title"),element,"ModelElementInteractiveRadiobutton",readOnly);
	}

	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(600,0);
		pack();
		doLayout();
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationInteractiveRadiobutton;
	}

	@Override
	protected JComponent getContentPanel() {
		final JPanel content=new JPanel();
		content.setLayout(new BorderLayout());
		JPanel setup=new JPanel();
		setup.setLayout(new BoxLayout(setup,BoxLayout.PAGE_AXIS));
		content.add(setup,BorderLayout.NORTH);

		final ModelElementInteractiveRadiobutton radiobutton=(ModelElementInteractiveRadiobutton)element;
		Object[] data;

		data=getInputPanel(Language.tr("Surface.InteractiveRadiobutton.Dialog.Variable")+":",radiobutton.getVariable());
		setup.add((JPanel)data[0]);
		editVariable=(JTextField)data[1];
		editVariable.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		data=getInputPanel(Language.tr("Surface.InteractiveRadiobutton.Dialog.ValueChecked")+":",NumberTools.formatNumber(radiobutton.getValueChecked()));
		setup.add((JPanel)data[0]);
		editValueChecked=(JTextField)data[1];
		editValueChecked.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		checkData(false);

		return content;
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessage) {
		boolean ok=true;

		if (!ExpressionCalc.checkVariableName(editVariable.getText().trim())) {
			editVariable.setBackground(Color.RED);
			ok=false;
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.InteractiveRadiobutton.Dialog.Variable.ErrorTitle"),Language.tr("Surface.InteractiveRadiobutton.Dialog.Variable.ErrorInfo"));
				return false;
			}
		} else {
			editVariable.setBackground(NumberTools.getTextFieldDefaultBackground());
		}

		final Double valueChecked=NumberTools.getDouble(editValueChecked,true);
		if (valueChecked==null) {
			ok=false;
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.InteractiveRadiobutton.Dialog.ValueChecked.ErrorTitle"),String.format(Language.tr("Surface.InteractiveRadiobutton.Dialog.ValueChecked.ErrorInfo"),editValueChecked.getText()));
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
		super.storeData();
		final ModelElementInteractiveRadiobutton radiobutton=(ModelElementInteractiveRadiobutton)element;
		radiobutton.setVariable(editVariable.getText().trim());
		radiobutton.setValueChecked(NumberTools.getDouble(editValueChecked,true));
	}
}

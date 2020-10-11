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
import java.awt.SystemColor;
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
 * Dialog, der Einstellungen für ein {@link ModelElementInteractiveCheckbox}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementInteractiveCheckbox
 */
public class ModelElementInteractiveCheckboxDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 5160508922050000799L;

	private JTextField editVariable;
	private JTextField editValueChecked;
	private JTextField editValueUnchecked;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementInteractiveCheckbox}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementInteractiveCheckboxDialog(final Component owner, final ModelElementInteractiveCheckbox element, final boolean readOnly) {
		super(owner,Language.tr("Surface.InteractiveCheckbox.Dialog.Title"),element,"ModelElementInteractiveCheckbox",readOnly);
	}

	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(600,0);
		pack();
		doLayout();
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationInteractiveCheckbox;
	}

	@Override
	protected JComponent getContentPanel() {
		final JPanel content=new JPanel();
		content.setLayout(new BorderLayout());
		JPanel setup=new JPanel();
		setup.setLayout(new BoxLayout(setup,BoxLayout.PAGE_AXIS));
		content.add(setup,BorderLayout.NORTH);

		final ModelElementInteractiveCheckbox checkbox=(ModelElementInteractiveCheckbox)element;
		Object[] data;

		data=getInputPanel(Language.tr("Surface.InteractiveCheckbox.Dialog.Variable")+":",checkbox.getVariable());
		setup.add((JPanel)data[0]);
		editVariable=(JTextField)data[1];
		editVariable.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		data=getInputPanel(Language.tr("Surface.InteractiveCheckbox.Dialog.ValueChecked")+":",NumberTools.formatNumber(checkbox.getValueChecked()));
		setup.add((JPanel)data[0]);
		editValueChecked=(JTextField)data[1];
		editValueChecked.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		data=getInputPanel(Language.tr("Surface.InteractiveCheckbox.Dialog.ValueUnchecked")+":",NumberTools.formatNumber(checkbox.getValueChecked()));
		setup.add((JPanel)data[0]);
		editValueUnchecked=(JTextField)data[1];
		editValueUnchecked.addKeyListener(new KeyListener() {
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
				MsgBox.error(this,Language.tr("Surface.InteractiveCheckbox.Dialog.Variable.ErrorTitle"),Language.tr("Surface.InteractiveCheckbox.Dialog.Variable.ErrorInfo"));
				return false;
			}
		} else {
			editVariable.setBackground(SystemColor.text);
		}

		final Double valueChecked=NumberTools.getDouble(editValueChecked,true);
		if (valueChecked==null) {
			ok=false;
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.InteractiveCheckbox.Dialog.ValueChecked.ErrorTitle"),String.format(Language.tr("Surface.InteractiveCheckbox.Dialog.ValueChecked.ErrorInfo"),editValueChecked.getText()));
				return false;
			}
		}

		final Double valueUnchecked=NumberTools.getDouble(editValueChecked,true);
		if (valueUnchecked==null) {
			ok=false;
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.InteractiveCheckbox.Dialog.ValueUnchecked.ErrorTitle"),String.format(Language.tr("Surface.InteractiveCheckbox.Dialog.ValueUnchecked.ErrorInfo"),editValueUnchecked.getText()));
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
		final ModelElementInteractiveCheckbox checkbox=(ModelElementInteractiveCheckbox)element;
		checkbox.setVariable(editVariable.getText().trim());
		checkbox.setValueChecked(NumberTools.getDouble(editValueChecked,true));
		checkbox.setValueUnchecked(NumberTools.getDouble(editValueUnchecked,true));
	}
}

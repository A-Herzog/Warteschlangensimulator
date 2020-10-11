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

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import simulator.simparser.ExpressionMultiEval;
import systemtools.MsgBox;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementHold}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementHold
 */
public class ModelElementHoldDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 5587091033538325111L;

	private JTextField condition;
	private JCheckBox clientBasedCheck;
	private JCheckBox useTimedChecks;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementHold}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementHoldDialog(final Component owner, final ModelElementHold element, final boolean readOnly) {
		super(owner,Language.tr("Surface.Hold.Dialog.Title"),element,"ModelElementHold",readOnly);
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
		return InfoPanel.stationHold;
	}

	@Override
	protected JComponent getContentPanel() {
		final JPanel content=new JPanel();
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		final Object[] data=getInputPanel(Language.tr("Surface.Hold.Dialog.Condition")+":",((ModelElementHold)element).getCondition());
		condition=(JTextField)data[1];
		condition.setEditable(!readOnly);
		condition.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		checkData(false);
		((JPanel)data[0]).add(getExpressionEditButton(this,condition,true,true,element.getModel(),element.getSurface()),BorderLayout.EAST);
		content.add((JPanel)data[0]);

		JPanel line;

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(clientBasedCheck=new JCheckBox(Language.tr("Surface.Hold.Dialog.ClientBasedCheck"),((ModelElementHold)element).isClientBasedCheck()));
		clientBasedCheck.setEnabled(!readOnly);

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(useTimedChecks=new JCheckBox(Language.tr("Surface.Hold.Dialog.TimeBasedCheck"),((ModelElementHold)element).isUseTimedChecks()));
		useTimedChecks.setEnabled(!readOnly);

		return content;
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessage) {
		if (readOnly) return false;

		final String text=condition.getText();
		if (!text.trim().isEmpty()) {
			final int error=ExpressionMultiEval.check(text,element.getSurface().getMainSurfaceVariableNames(element.getModel().getModelVariableNames(),false));
			if (error>=0) {
				condition.setBackground(Color.red);
				if (showErrorMessage) MsgBox.error(this,Language.tr("Surface.Hold.Dialog.Condition.Error.Title"),String.format(Language.tr("Surface.Hold.Dialog.Condition.Error.Info"),text,error+1));
				return false;
			}
		}
		condition.setBackground(SystemColor.text);
		return true;
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
		((ModelElementHold)element).setCondition(condition.getText());
		((ModelElementHold)element).setClientBasedCheck(clientBasedCheck.isSelected());
		((ModelElementHold)element).setUseTimedChecks(useTimedChecks.isSelected());
	}
}

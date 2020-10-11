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
import simulator.simparser.ExpressionCalc;
import systemtools.MsgBox;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementRecord}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementRecord
 */
public class ModelElementRecordDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 3703533906857752715L;

	private JTextField expression1;
	private JTextField expression2;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementRecord}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementRecordDialog(final Component owner, final ModelElementRecord element, final boolean readOnly) {
		super(owner,Language.tr("Surface.Record.Dialog.Title"),element,"ModelElementRecord",readOnly);
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
		return InfoPanel.stationRecord;
	}

	private JTextField addTextField(final JPanel parent, final String label, final String value) {
		final Object[] data=getInputPanel(label+":",value);
		final JTextField expression=(JTextField)data[1];
		expression.setEditable(!readOnly);
		expression.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		((JPanel)data[0]).add(getExpressionEditButton(this,expression,false,true,element.getModel(),element.getSurface()),BorderLayout.EAST);
		parent.add((JPanel)data[0]);
		return expression;
	}

	@Override
	protected JComponent getContentPanel() {
		final JPanel content=new JPanel();
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		expression1=addTextField(content,Language.tr("Surface.Record.Dialog.Expression1"),((ModelElementRecord)element).getExpression1());
		expression2=addTextField(content,Language.tr("Surface.Record.Dialog.Expression2"),((ModelElementRecord)element).getExpression2());

		checkData(false);

		return content;
	}

	private boolean checkInput(final JTextField expression, final boolean showErrorMessage, final String errorExpression, final String errorEmpty) {
		final String text=expression.getText();
		if (!text.trim().isEmpty()) {
			final int error=ExpressionCalc.check(text,element.getSurface().getMainSurfaceVariableNames(element.getModel().getModelVariableNames(),true));
			if (error>=0) {
				expression.setBackground(Color.red);
				if (showErrorMessage) MsgBox.error(this,Language.tr("Surface.Record.Dialog.Expression.Error.Title"),String.format(errorExpression,text,error+1));
				return false;
			}
		} else {
			if (errorEmpty!=null) {
				expression.setBackground(Color.red);
				if (showErrorMessage) MsgBox.error(this,Language.tr("Surface.Record.Dialog.Expression.Error.Title"),errorEmpty);
				return false;
			}
		}
		expression.setBackground(SystemColor.text);
		return true;
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessage) {
		if (readOnly) return false;
		boolean ok=true;

		if (!checkInput(expression1,showErrorMessage,Language.tr("Surface.Record.Dialog.Expression1.Error.Info"),Language.tr("Surface.Record.Dialog.Expression1.Error.InfoEmpty"))) {
			if (showErrorMessage) return false;
			ok=false;
		}

		if (!checkInput(expression2,showErrorMessage,Language.tr("Surface.Record.Dialog.Expression2.Error.Info"),null)) {
			if (showErrorMessage) return false;
			ok=false;
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
		((ModelElementRecord)element).setExpression1(expression1.getText().trim());
		((ModelElementRecord)element).setExpression2(expression2.getText().trim());
	}
}

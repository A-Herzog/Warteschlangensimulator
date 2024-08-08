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
package ui.modeleditor.coreelements;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import simulator.simparser.ExpressionMultiEval;
import systemtools.MsgBox;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein <code>ModelElementLogicWithCondition</code>-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementLogicWithCondition
 */
public class ModelElementLogicWithConditionDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -5924170933747046706L;

	/** Eingabefeld für die Bedingung */
	private JTextField condition;

	/**
	 * Konstruktor der Klasse <code>ModelElementLogicWithConditionDialog</code>
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes <code>ModelElement</code>
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 * @param title	Titel des Fensters
	 * @param helpName	Name des Hilfethemas mit dem die Hilfeschaltfläche verknüpft werden soll
	 * @param infoPanelID	ID für einen Infotext oben im Dialog zurück
	 */
	public ModelElementLogicWithConditionDialog(Component owner, ModelElementLogicWithCondition element, boolean readOnly, final String title, final String helpName, final String infoPanelID) {
		super(owner,title,element,helpName,infoPanelID,readOnly);
	}

	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(600,0);
	}

	@Override
	protected JComponent getContentPanel() {
		final Object[] data=getInputPanel(Language.tr("Surface.Logic.Dialog.Condition")+":",((ModelElementLogicWithCondition)element).getCondition());
		final JPanel content=(JPanel)data[0];
		condition=(JTextField)data[1];
		condition.setEditable(!readOnly);
		condition.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		content.add(getExpressionEditButton(this,condition,true,true,element.getModel(),element.getSurface()),BorderLayout.EAST);
		condition.setEnabled(!readOnly);

		checkData(false);

		return content;
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessage) {
		if (readOnly) return false;

		final int error=ExpressionMultiEval.check(condition.getText(),element.getSurface().getMainSurfaceVariableNames(element.getModel().getModelVariableNames(),true),element.getModel().userFunctions);
		if (error>=0) {
			condition.setBackground(Color.red);
			if (showErrorMessage) MsgBox.error(this,Language.tr("Surface.Logic.Dialog.Condition.Error.Title"),String.format(Language.tr("Surface.Logic.Dialog.Condition.Error.Info"),condition.getText(),error+1));
			return false;
		}

		condition.setBackground(NumberTools.getTextFieldDefaultBackground());
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
		((ModelElementLogicWithCondition)element).setCondition(condition.getText());
	}
}

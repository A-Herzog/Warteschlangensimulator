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
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import simulator.simparser.ExpressionMultiEval;
import systemtools.MsgBox;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelDataRenameListener;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modelproperties.ModelPropertiesDialogPageClients;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementAssign}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementAssign
 */
public class ModelElementAssignDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 491234735571463778L;

	/**
	 * Name der Station beim Aufrufen des Dialogs<br>
	 * (um ggf. beim Schließen des Dialogs das Modell zu benachrichtigen, dass sich der Signalname verändert hat)
	 */
	private final String oldName;

	/**
	 * Kundendatenelement aus dem Modell (zum Auslesen von Icons und ggf. um Kundentypen umzubenennen)
	 */
	private final ModelClientData clientData;

	/**
	 * Schaltfläche "Kundentypeigenschaften bearbeiten"
	 * @see #initUserButtons()
	 * @see #editClientDataButton
	 */
	private JButton editClientDataButton;

	/**
	 * Checkbox: Soll die Bedingung verwendet werden?
	 */
	private JCheckBox useCondition;

	/**
	 * Eingabefeld für die Bedingung zur Auslösung der Aktion
	 */
	private JTextField condition;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementAssign}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 * @param clientData	Kundendatenelement aus dem Modell (zum Auslesen von Icons und ggf. um Kundentypen umzubenennen)
	 */
	public ModelElementAssignDialog(final Component owner, final ModelElementAssign element, final boolean readOnly, final ModelClientData clientData) {
		super(owner,Language.tr("Surface.Assign.Dialog.Title"),element,"ModelElementAssign",readOnly,false);
		oldName=element.getName();
		this.clientData=clientData;
		setVisible(true);
	}

	@Override
	protected void initUserNameFieldButtons(final JPanel panel) {
		panel.add(editClientDataButton=new JButton());
		setClientIcon(element.getName(),editClientDataButton,element.getModel());
		editClientDataButton.setToolTipText(Language.tr("Surface.Source.Dialog.ClientTypeSettings"));
		editClientDataButton.addActionListener(e->editClientData());
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationAssign;
	}

	/**
	 * Erstellt und liefert das Panel, welches im Content-Bereich des Dialogs angezeigt werden soll
	 * @return	Panel mit den Dialogelementen
	 */
	@Override
	protected JComponent getContentPanel() {
		final ModelElementAssign assign=(ModelElementAssign)element;
		final EditModel model=assign.getModel();

		final JPanel content=new JPanel(new BorderLayout());

		JPanel line;

		/* Optionale Bedingung */
		final JPanel bottomArea=new JPanel();
		bottomArea.setLayout(new BoxLayout(bottomArea,BoxLayout.PAGE_AXIS));
		content.add(bottomArea,BorderLayout.NORTH);

		bottomArea.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(useCondition=new JCheckBox(Language.tr("Surface.Assign.Dialog.Condition.UseCondition")+":",!assign.getCondition().isEmpty()));
		useCondition.addActionListener(e->checkData(false));
		useCondition.setEnabled(!readOnly);

		final Object[] data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.Assign.Dialog.Condition.Condition")+":",assign.getCondition());
		bottomArea.add(line=(JPanel)data[0]);
		condition=(JTextField)data[1];
		condition.setEnabled(!readOnly);
		condition.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false); useCondition.setSelected(true);}
			@Override public void keyReleased(KeyEvent e) {checkData(false); useCondition.setSelected(true);}
			@Override public void keyPressed(KeyEvent e) {checkData(false); useCondition.setSelected(true);}
		});
		line.add(ModelElementBaseDialog.getExpressionEditButton(this,condition,true,true,model,model.surface),BorderLayout.EAST);

		return content;
	}

	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(600,0);
		pack();
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessage) {
		if (readOnly) return false;

		final EditModel model=element.getModel();

		boolean ok=true;

		final String conditionString=condition.getText().trim();
		if (!useCondition.isSelected() || conditionString.isEmpty()) {
			condition.setBackground(NumberTools.getTextFieldDefaultBackground());
		} else {
			final int error=ExpressionMultiEval.check(conditionString,model.surface.getMainSurfaceVariableNames(model.getModelVariableNames(),false),model.userFunctions);
			if (error>=0) {
				condition.setBackground(Color.RED);
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Assign.Dialog.Condition.Error.Title"),String.format(Language.tr("Surface.Assign.Dialog.Condition.Error.Info"),condition,error+1));
					return false;
				}
				ok=false;
			} else {
				condition.setBackground(NumberTools.getTextFieldDefaultBackground());
			}
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

		if (!oldName.equals(element.getName())) {
			if (clientData!=null) clientData.copyDataIfNotExistent(oldName,element.getName());
			element.getSurface().objectRenamed(oldName,element.getName(),ModelDataRenameListener.RenameType.RENAME_TYPE_CLIENT_TYPE,true);
		}

		if (useCondition.isSelected()) ((ModelElementAssign)element).setCondition(condition.getText().trim()); else ((ModelElementAssign)element).setCondition("");
	}

	/**
	 * Öffnet den Kundentypeigenschaften-Dialog.
	 * @see #editClientDataButton
	 * @see ModelPropertiesDialogPageClients#editClientData(Component, Runnable, simulator.editmodel.EditModel, String, boolean)
	 */
	private void editClientData() {
		final String name=(oldName.isEmpty())?getElementName():oldName;
		if (name.isEmpty()) {
			MsgBox.error(this,Language.tr("Surface.Source.Dialog.ClientTypeSettings.NoNameError.Title"),Language.tr("Surface.Source.Dialog.ClientTypeSettings.NoNameError.Info"));
			return;
		}

		if (ModelPropertiesDialogPageClients.editClientData(this,helpRunnable,element.getModel(),name,readOnly)) setClientIcon(name,editClientDataButton,element.getModel());
	}
}
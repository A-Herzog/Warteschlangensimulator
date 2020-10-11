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
import java.awt.SystemColor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import language.Language;
import systemtools.MsgBox;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementCounter}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementCounter
 */
public class ModelElementCounterDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 8074769774808895018L;

	private JComboBox<String> groupName;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementCounter}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementCounterDialog(final Component owner, final ModelElementCounter element, final boolean readOnly) {
		super(owner,Language.tr("Surface.Counter.Dialog.Title"),element,"ModelElementCounter",readOnly);
	}

	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(600,0);
		pack();
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationCounter;
	}

	private Set<String> getCounterGroupNames(final ModelSurface surface) {
		final Set<String> groupNames=new HashSet<>();
		for (ModelElement element: surface.getElements()) {
			if (element instanceof ModelElementCounter) {
				final String group=((ModelElementCounter)element).getGroupName().trim();
				if (!group.isEmpty()) groupNames.add(group);
			}
			if (element instanceof ModelElementSub) {
				groupNames.addAll(getCounterGroupNames(((ModelElementSub)element).getSubSurface()));
			}
		}
		return groupNames;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected JComponent getContentPanel() {
		final Object[] data=getComboBoxPanel(Language.tr("Surface.Counter.Dialog.GroupName")+":",((ModelElementCounter)element).getGroupName(),getCounterGroupNames(element.getModel().surface));
		groupName=(JComboBox<String>)data[1];
		groupName.setEnabled(!readOnly);
		groupName.getEditor().getEditorComponent().setEnabled(!readOnly);
		groupName.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		checkData(false);
		return (JPanel)data[0];
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessage) {
		if (readOnly) return false;

		final String text=((String)groupName.getEditor().getItem()).trim();
		if (text.isEmpty()) {
			groupName.setBackground(Color.red);
			if (showErrorMessage) MsgBox.error(this,Language.tr("Surface.Counter.Dialog.GroupName.Error.Title"),Language.tr("Surface.Counter.Dialog.GroupName.Error.Info"));
			return false;
		}
		groupName.setBackground(SystemColor.text);
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
		final String text=((String)groupName.getEditor().getItem()).trim();
		((ModelElementCounter)element).setGroupName(text);
	}
}
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
import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import language.Language;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementAssignString}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementAssignString
 */
public class ModelElementAssignStringDialog extends ModelElementBaseDialog {
	private static final long serialVersionUID = 3177924513955998727L;

	private VariablesTextsTableModel model;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementAssignString}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementAssignStringDialog(final Component owner, final ModelElementAssignString element, final boolean readOnly) {
		super(owner,Language.tr("Surface.AssignString.Dialog.Title"),element,"ModelElementAssignString",readOnly);
	}

	@Override
	protected void setDialogSize() {
		setSizeRespectingScreensize(800,600);
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationAssignString;
	}

	@Override
	protected JComponent getContentPanel() {
		final JPanel content=new JPanel(new BorderLayout());
		final ModelElementAssignString assign=(ModelElementAssignString)element;
		final Object[] data=VariablesTextsTableModel.buildTable(assign.getRecord(),readOnly,helpRunnable);
		content.add((JScrollPane)data[0],BorderLayout.CENTER);
		model=(VariablesTextsTableModel)data[1];

		return content;
	}

	/**
	 * Speichert die Dialog-Daten in dem zugehörigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see #checkData()
	 */
	@Override
	protected void storeData() {
		super.storeData();
		model.storeData();
	}
}
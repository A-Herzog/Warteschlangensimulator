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
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;

import language.Language;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementDuplicate}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementDuplicate
 */
public class ModelElementDuplicateDialog extends ModelElementBaseDialog {
	private static final long serialVersionUID = -86922871601132368L;

	private NewClientTypesPanel newClientTypes;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementDuplicate}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementDuplicateDialog(final Component owner, final ModelElementDuplicate element, final boolean readOnly) {
		super(owner,Language.tr("Surface.Duplicate.Dialog.Title"),element,"ModelElementDuplicate",readOnly);
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
		return InfoPanel.stationDuplicate;
	}

	/**
	 * Erstellt und liefert das Panel, welches im Content-Bereich des Dialogs angezeigt werden soll
	 * @return	Panel mit den Dialogelementen
	 */
	@Override
	protected JComponent getContentPanel() {
		final JPanel content=new JPanel(new BorderLayout());
		final List<String> newClientTypeNames=(element instanceof ModelElementDuplicate)?((ModelElementDuplicate)element).getChangedClientTypes():null;
		content.add(newClientTypes=new NewClientTypesPanel(newClientTypeNames,readOnly));
		return content;
	}

	@Override
	protected void storeData() {
		super.storeData();

		if (element instanceof ModelElementDuplicate) {
			((ModelElementDuplicate)element).setChangedClientTypes(newClientTypes.getNewClientTypeNames());
		}
	}
}

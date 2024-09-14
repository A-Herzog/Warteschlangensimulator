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

import java.awt.Component;
import java.io.Serializable;

import javax.swing.JComponent;
import javax.swing.JPanel;

import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen f�r ein <code>ModelElementLogic</code>-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementLogic
 */
public class ModelElementLogicDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 5843668286411942111L;

	/**
	 * Konstruktor der Klasse <code>ModelElementLogicDialog</code>
	 * @param owner	�bergeordnetes Fenster
	 * @param element	Zu bearbeitendes <code>ModelElement</code>
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfl�che deaktiviert
	 * @param title	Titel des Fensters
	 * @param helpName	Name des Hilfethemas mit dem die Hilfeschaltfl�che verkn�pft werden soll
	 * @param infoPanelID	ID f�r einen Infotext oben im Dialog zur�ck
	 */
	public ModelElementLogicDialog(Component owner, ModelElementLogic element, boolean readOnly, final String title, final String helpName, final String infoPanelID) {
		super(owner,title,element,helpName,infoPanelID,readOnly);
	}

	/**
	 * Stellt die Gr��e des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(800,0);
		setMaxSizeRespectingScreensize(800,1000);
		pack();
	}

	/**
	 * Stellt die Gr��e des Dialogfensters unmittelbar vor dem Sicherbarmachen ein.
	 */
	@Override
	protected void setDialogSizeLater() {
		setMaxSizeRespectingScreensize(800,1000);
		setSize(getWidth(),getHeight()+(int)Math.round(45*windowScaling));
	}

	/**
	 * Erstellt und liefert das Panel, welches im Content-Bereich des Dialogs angezeigt werden soll
	 * @return	Panel mit den Dialogelementen
	 */
	@Override
	protected JComponent getContentPanel() {
		return new JPanel();
	}
}

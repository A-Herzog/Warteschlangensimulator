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
import java.io.Serializable;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import language.Language;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementSet}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementSet
 */
public class ModelElementSetDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -9185897343104852730L;

	/**
	 * Tabelle zur Konfiguration der Zuweisungen, die in der Station vorgenommen werden sollen
	 */
	private VariablesTableModel model;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementSet}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementSetDialog(final Component owner, final ModelElementSet element, final boolean readOnly) {
		super(owner,Language.tr("Surface.Set.Dialog.Title"),element,"ModelElementSet",readOnly);
	}

	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(800,600);
		pack();
		setMaxSizeRespectingScreensize(1024,768);
	}

	/**
	 * Stellt die Größe des Dialogfensters unmittelbar vor dem Sicherbarmachen ein.
	 */
	@Override
	protected void setDialogSizeLater() {
	}


	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationSet;
	}

	@Override
	protected JComponent getContentPanel() {
		final JPanel content=new JPanel(new BorderLayout());
		final ModelElementSet set=(ModelElementSet)element;
		final Object[] data=VariablesTableModel.buildTable(set.getRecord(),set,readOnly,helpRunnable,false);
		content.add((JScrollPane)data[0],BorderLayout.CENTER);
		model=(VariablesTableModel)data[1];

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

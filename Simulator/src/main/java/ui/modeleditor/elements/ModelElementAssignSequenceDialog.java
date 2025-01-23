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

import java.awt.Component;
import java.awt.FlowLayout;
import java.io.Serializable;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import language.Language;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelSequence;
import ui.modeleditor.ModelSequences;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementAssignSequence}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementAssignSequence
 */
public class ModelElementAssignSequenceDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -7609471043565949709L;

	/**
	 * Auswahlbox zur Auswahl des zuzuweisenden Fertigungsplans
	 */
	private JComboBox<String> sequenceCombo;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementAssignSequence}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 * @param sequences	Objekt das modellweit die Fertigungspläne vorhält
	 */
	public ModelElementAssignSequenceDialog(final Component owner, final ModelElementAssignSequence element, final boolean readOnly, final ModelSequences sequences) {
		super(owner,Language.tr("Surface.AssignSequence.Dialog.Title"),element,"ModelElementAssignSequence",readOnly,false);

		final DefaultComboBoxModel<String> model=new DefaultComboBoxModel<>();
		for (ModelSequence sequence: sequences.getSequences()) model.addElement(sequence.getName());
		sequenceCombo.setModel(model);

		final String sequenceName=element.getSequence();
		int index=-1;
		for (int i=0;i<sequences.getSequences().size();i++) if (sequences.getSequences().get(i).getName().equalsIgnoreCase(sequenceName)) {index=i; break;}
		if (index<0 && sequenceCombo.getModel().getSize()>0) index=0;
		if (index>=0) sequenceCombo.setSelectedIndex(index);

		setVisible(true);
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationAssignSequence;
	}

	/**
	 * Erstellt und liefert das Panel, welches im Content-Bereich des Dialogs angezeigt werden soll
	 * @return	Panel mit den Dialogelementen
	 */
	@Override
	protected JComponent getContentPanel() {
		final JPanel content=new JPanel(new FlowLayout(FlowLayout.LEFT));

		final JLabel label=new JLabel(Language.tr("Surface.AssignSequence.Dialog.Sequence")+":");
		content.add(label);
		content.add(sequenceCombo=new JComboBox<>());
		sequenceCombo.setEnabled(!readOnly);
		label.setLabelFor(sequenceCombo);

		return content;
	}

	/**
	 * Stellt die Größe des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(600,0);
		setMaxSizeRespectingScreensize(600,1000);
		pack();
	}

	/**
	 * Stellt die Größe des Dialogfensters unmittelbar vor dem Sicherbarmachen ein.
	 */
	@Override
	protected void setDialogSizeLater() {
		setMaxSizeRespectingScreensize(600,1000);
		setSize(getWidth(),getHeight()+(int)Math.round(30*windowScaling));
	}

	/**
	 * Speichert die Dialog-Daten in dem zugehörigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see #checkData()
	 */
	@Override
	protected void storeData() {
		super.storeData();

		if (element instanceof ModelElementAssignSequence) {
			if (sequenceCombo.getSelectedIndex()<0) {
				((ModelElementAssignSequence)element).setSequence("");
			} else {
				((ModelElementAssignSequence)element).setSequence((String)sequenceCombo.getSelectedItem());
			}
		}
	}
}
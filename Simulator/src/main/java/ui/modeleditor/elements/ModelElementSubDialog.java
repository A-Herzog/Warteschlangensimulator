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
import java.awt.FlowLayout;
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import language.Language;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.coreelements.ModelElement;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementSub}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementSub
 */
public class ModelElementSubDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 2643627718628007295L;

	/**
	 * Eingabefeld für die Anzahl an einlaufenden Kanten
	 */
	private SpinnerModel inputField;

	/**
	 * Eingabefeld für die Anzahl an auslaufenden Kanten
	 */
	private SpinnerModel outputField;

	/**
	 * Beschreibung für das Untermodell
	 */
	private JTextArea description;

	/**
	 * Liefert <code>true</code>, wenn nach dem Schließen dieses Dialogs der Untermodell-Editor geöffnet werden soll
	 * @see #getOpenEditor()
	 */
	private boolean openEditor;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementSub}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementSubDialog(final Component owner, final ModelElementSub element, final boolean readOnly) {
		super(owner,Language.tr("Surface.Sub.Dialog.Title"),element,"ModelElementSub",readOnly,false);
		setVisible(true);
	}

	@Override
	protected void initUserButtons() {
		addUserButton(Language.tr("Surface.Sub.Dialog.OkAndOpenEditor"),Language.tr("Surface.Sub.Dialog.OkAndOpenEditor.Tooltip"),Images.MODELEDITOR_ELEMENT_SUB_EDIT.getURL());
	}

	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(600,600);
		setResizable(true);
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationSub;
	}

	@Override
	protected JComponent getContentPanel() {
		openEditor=false;

		final JPanel content=new JPanel();
		content.setLayout(new BorderLayout());

		if (element instanceof ModelElementSub) {
			JPanel top, main, sub;
			JLabel label;

			content.add(top=new JPanel(),BorderLayout.NORTH);
			top.setLayout(new BoxLayout(top,BoxLayout.PAGE_AXIS));

			top.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));

			sub.add(label=new JLabel(Language.tr("Surface.Sub.Dialog.NumberInput")+":"));
			final JSpinner inputSpinner=new JSpinner(inputField=new SpinnerNumberModel(1,1,99,1));
			final JSpinner.NumberEditor inputEditor=new JSpinner.NumberEditor(inputSpinner);
			inputEditor.getFormat().setGroupingUsed(false);
			inputEditor.getTextField().setColumns(2);
			inputSpinner.setEditor(inputEditor);
			sub.add(inputSpinner);
			label.setLabelFor(inputSpinner);
			inputSpinner.setValue(((ModelElementSub)element).getInputCount());

			top.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));

			sub.add(label=new JLabel(Language.tr("Surface.Sub.Dialog.NumberOutput")+":"));
			final JSpinner outputSpinner=new JSpinner(outputField=new SpinnerNumberModel(1,1,99,1));
			final JSpinner.NumberEditor outputEditor=new JSpinner.NumberEditor(outputSpinner);
			outputEditor.getFormat().setGroupingUsed(false);
			outputEditor.getTextField().setColumns(2);
			outputSpinner.setEditor(outputEditor);
			sub.add(outputSpinner);
			label.setLabelFor(outputSpinner);
			outputSpinner.setValue(((ModelElementSub)element).getOutputCount());

			content.add(main=new JPanel(new BorderLayout()),BorderLayout.CENTER);
			main.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);
			sub.add(new JLabel(Language.tr("Surface.Sub.Dialog.Description")+":"));

			main.add(new JScrollPane(description=new JTextArea(((ModelElementSub)element).getDescription())),BorderLayout.CENTER);
			description.setEditable(!readOnly);
			addUndoFeature(description);
		}
		return content;
	}

	/**
	 * Versucht basierend auf einer auslaufenden Kante das nächste Element zu finden
	 * @param connection	Aus dem Start-Element auslaufende Kante (<code>null</code> wird erkannt und als Fehler gemeldet)
	 * @return	Liefert die ID des nächsten Elements oder -1, wenn es kein Ziel gibt
	 */
	protected final int findNextId(final ModelElementEdge connection) {
		if (connection==null) return -1;
		ModelElement element=connection.getConnectionEnd();
		if (element==null) return -1;
		return element.getId();
	}

	/**
	 * Versucht basierend auf einer einlaufenden Kante das vorherige Element zu finden
	 * @param connection	In das Start-Element einlaufende Kante (<code>null</code> wird erkannt und als Fehler gemeldet)
	 * @return	Liefert die ID des vorherigen Elements oder -1, wenn es keine Quelle gibt
	 */
	protected final int findPreviousId(final ModelElementEdge connection) {
		if (connection==null) return -1;
		ModelElement element=connection.getConnectionStart();
		if (element==null) return -1;
		return element.getId();
	}

	/**
	 * Speichert die Dialog-Daten in dem zugehörigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see #checkData()
	 */
	@Override
	protected void storeData() {
		super.storeData();

		if (element instanceof ModelElementSub) {
			((ModelElementSub)element).setInputCount((Integer)inputField.getValue());
			((ModelElementSub)element).setOutputCount((Integer)outputField.getValue());
			((ModelElementSub)element).setDescription(description.getText());
		}
	}

	@Override
	protected void userButtonClick(int nr, JButton button) {
		openEditor=true;
		SwingUtilities.invokeLater(()->close(CLOSED_BY_OK));
	}

	/**
	 * Gibt an, ob der Nutzer ausgewählt hat, dass nach dem Schließen dieses Dialogs der Untermodell-Editor geöffnet werden soll.
	 * @return	Liefert <code>true</code>, wenn nach dem Schließen dieses Dialogs der Untermodell-Editor geöffnet werden soll.
	 */
	public boolean getOpenEditor() {
		return openEditor;
	}

}
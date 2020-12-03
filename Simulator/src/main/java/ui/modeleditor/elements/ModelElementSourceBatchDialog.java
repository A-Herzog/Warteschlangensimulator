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
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import language.Language;
import systemtools.BaseDialog;
import ui.help.Help;

/**
 * Dieser Dialog erm�glicht das Konfigurieren von Batch-Gr��en
 * bei Kundenank�nften.
 * @author Alexander Herzog
 * @see ModelElementSourceRecordPanel
 */
public class ModelElementSourceBatchDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 4182770820151357513L;

	/**
	 * Tabs f�r die verschiedenen Editoren
	 */
	private final JTabbedPane tabs;

	/**
	 * Liste der verschiedenen Editoren
	 */
	private final List<ModelElementSourceBatchEditorAbstract> editors;

	/**
	 * Konstruktor der Klasse
	 * @param owner	�bergeordnetes Element
	 * @param helpTopic	Hilfe-Callback
	 * @param readOnly	Nur-Lese-Status
	 * @param distribution	Anf�ngliche Raten f�r die Ankunfts-Batch-Gr��en
	 */
	public ModelElementSourceBatchDialog(final Component owner, final String helpTopic, final boolean readOnly, final double[] distribution) {
		super(owner,Language.tr("Surface.Source.DialogBatchSize.Title"),readOnly);

		editors=new ArrayList<>();
		final JPanel contentPanel=createGUI(()->Help.topicModal(this,helpTopic));
		contentPanel.setLayout(new BorderLayout());
		contentPanel.add(tabs=new JTabbedPane(),BorderLayout.CENTER);

		addEditor(new ModelElementSourceBatchEditor1(readOnly),distribution);
		addEditor(new ModelElementSourceBatchEditor2(readOnly),distribution);

		setMinSizeRespectingScreensize(450,450);
		pack();
		if (getHeight()>750) setSize(getWidth(),750);
		setLocationRelativeTo(this.owner);
		setVisible(true);
	}

	/**
	 * F�gt einen weiteren Editor in einem weiteren Tab hinzu.
	 * @param editor	Neuer Editor
	 * @param distribution	Initiale Daten f�r den Editor
	 */
	private void addEditor(final ModelElementSourceBatchEditorAbstract editor, final double[] distribution) {
		editor.setDistribution(distribution);
		tabs.addTab(editor.getEditorName(),editor);
		editors.add(editor);
		editor.addChangeListener(sender->changeListener(sender));
	}

	/**
	 * L�uft gerade ein Benachrichtigungsvorgang? Wenn ja, keine weiteren
	 * Benachrichtigungen ausl�sen (sollte sowieso nicht passieren, aber
	 * so ist es abgesichert).
	 * @see #changeListener(ModelElementSourceBatchEditor)
	 */
	private boolean changeListenerRunning=false;

	/**
	 * Benachrichtigt alle anderen Editoren, dass in einem Editor die Daten ver�ndert wurden.
	 * @param sender	Editor, der die Benachrichtigung ausgel�st hat
	 */
	private void changeListener(final ModelElementSourceBatchEditor sender) {
		if (changeListenerRunning) return;
		final double[] newDistribution=sender.getDistribution();
		changeListenerRunning=true;
		try {
			for (ModelElementSourceBatchEditorAbstract editor: editors) if (editor!=sender) editor.setDistribution(newDistribution);

		} finally {
			changeListenerRunning=false;
		}
	}

	@Override
	protected boolean checkData() {
		final ModelElementSourceBatchEditorAbstract editor=editors.get(tabs.getSelectedIndex());
		return editor.checkData();
	}

	/**
	 * Liefert im Falle, dass der Dialog per "Ok" geschlossen wurde,
	 * die Raten mit denen die jeweiligen Ankunfts-Batch-Gr��en
	 * gew�hlt werden sollen.
	 * @return	Raten f�r die Ankunfts-Batch-Gr��en
	 */
	public double[] getBatchRates() {
		final ModelElementSourceBatchEditorAbstract editor=editors.get(tabs.getSelectedIndex());
		return editor.getDistribution();
	}
}

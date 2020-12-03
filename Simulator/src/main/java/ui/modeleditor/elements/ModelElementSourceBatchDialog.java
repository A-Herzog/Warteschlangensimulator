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
 * Dieser Dialog ermöglicht das Konfigurieren von Batch-Größen
 * bei Kundenankünften.
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
	 * Tabs für die verschiedenen Editoren
	 */
	private final JTabbedPane tabs;

	/**
	 * Liste der verschiedenen Editoren
	 */
	private final List<ModelElementSourceBatchEditorAbstract> editors;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param helpTopic	Hilfe-Callback
	 * @param readOnly	Nur-Lese-Status
	 * @param distribution	Anfängliche Raten für die Ankunfts-Batch-Größen
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
	 * Fügt einen weiteren Editor in einem weiteren Tab hinzu.
	 * @param editor	Neuer Editor
	 * @param distribution	Initiale Daten für den Editor
	 */
	private void addEditor(final ModelElementSourceBatchEditorAbstract editor, final double[] distribution) {
		editor.setDistribution(distribution);
		tabs.addTab(editor.getEditorName(),editor);
		editors.add(editor);
		editor.addChangeListener(sender->changeListener(sender));
	}

	/**
	 * Läuft gerade ein Benachrichtigungsvorgang? Wenn ja, keine weiteren
	 * Benachrichtigungen auslösen (sollte sowieso nicht passieren, aber
	 * so ist es abgesichert).
	 * @see #changeListener(ModelElementSourceBatchEditor)
	 */
	private boolean changeListenerRunning=false;

	/**
	 * Benachrichtigt alle anderen Editoren, dass in einem Editor die Daten verändert wurden.
	 * @param sender	Editor, der die Benachrichtigung ausgelöst hat
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
	 * die Raten mit denen die jeweiligen Ankunfts-Batch-Größen
	 * gewählt werden sollen.
	 * @return	Raten für die Ankunfts-Batch-Größen
	 */
	public double[] getBatchRates() {
		final ModelElementSourceBatchEditorAbstract editor=editors.get(tabs.getSelectedIndex());
		return editor.getDistribution();
	}
}

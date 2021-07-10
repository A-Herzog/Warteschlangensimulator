/**
 * Copyright 2021 Alexander Herzog
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
package ui.modelproperties;

import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import language.Language;
import simulator.editmodel.EditModel;

/**
 * Dialogseite "Pfadaufzeichnung"
 * @author Alexander Herzog
 * @see ModelPropertiesDialog
 * @see ModelPropertiesDialogPage
 */
public class ModelPropertiesDialogPagePathRecording extends ModelPropertiesDialogPage {
	/** Option "Stationsübergänge zählen" */
	private JCheckBox pathRecordingStationTransitions;
	/** Option "Pfade der Kunden aufzeichnen" */
	private JCheckBox pathRecordingClientPaths;

	/**
	 * Konstruktor der Klasse
	 * @param dialog	Dialog in dem sich diese Seite befindet
	 * @param model	Modell aus dem die Daten entnommen und in das die Daten geschrieben werden sollen
	 * @param readOnly	Nur-Lese-Status
	 * @param help	Hilfe-Callback
	 */
	public ModelPropertiesDialogPagePathRecording(ModelPropertiesDialog dialog, EditModel model, boolean readOnly, Runnable help) {
		super(dialog,model,readOnly,help);
	}

	@Override
	public void build(JPanel content) {
		JPanel sub;

		content.setLayout(new FlowLayout(FlowLayout.LEFT));
		JPanel lines;
		content.add(lines=new JPanel());
		content.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

		lines.setLayout(new BoxLayout(lines,BoxLayout.PAGE_AXIS));

		lines.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(pathRecordingStationTransitions=new JCheckBox(Language.tr("Editor.Dialog.Tab.PathRecording.StationTransitions"),model.recordStationTransitions));
		pathRecordingStationTransitions.setEnabled(!readOnly);

		lines.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(pathRecordingClientPaths=new JCheckBox(Language.tr("Editor.Dialog.Tab.PathRecording.ClientPaths"),model.recordClientPaths));
		pathRecordingClientPaths.setEnabled(!readOnly);
	}

	@Override
	public void storeData() {
		model.recordStationTransitions=pathRecordingStationTransitions.isSelected();
		model.recordClientPaths=pathRecordingClientPaths.isSelected();
	}
}

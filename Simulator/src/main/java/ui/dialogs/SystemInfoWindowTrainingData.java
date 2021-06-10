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
package ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.io.Serializable;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import language.Language;
import simulator.editmodel.EditModelProcessor;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.images.Images;

/**
 * In diesem Dialog werden die Trainingsdaten der Nächst-Station-Vorschlagsfunktion angezeigt.
 * @author Alexander Herzog
 * @see EditModelProcessor
 * @see SystemInfoWindow
 */
public class SystemInfoWindowTrainingData extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-2842184698434710992L;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 */
	public SystemInfoWindowTrainingData(final Component owner) {
		super(owner,Language.tr("SystemInfo.Tools.NextStationTraining.ShowData.Title"));

		/* GUI */
		addUserButton(Language.tr("SystemInfo.Tools.NextStationTraining.DeleteData.Short"),Images.EDIT_DELETE.getIcon());
		showCloseButton=true;
		final JPanel content=createGUI(null);
		content.setLayout(new BorderLayout());

		/* Info */
		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		content.add(line,BorderLayout.NORTH);
		line.add(new JLabel(Language.tr("SystemInfo.Tools.NextStationTraining.ShowData.Info")));

		/* Anzuzeigender Text */
		final JTextArea text=new JTextArea(String.join("\n",EditModelProcessor.getInstance().getTrainingData()));
		content.add(new JScrollPane(text),BorderLayout.CENTER);
		text.setEditable(false);

		/* Dialog anzeigen */
		setSizeRespectingScreensize(800,600);
		setMinSizeRespectingScreensize(800,600);
		setResizable(true);
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	@Override
	protected void userButtonClick(final int nr, final JButton button) {
		if (MsgBox.confirm(this,Language.tr("SystemInfo.Tools.NextStationTraining.DeleteData.Title"),Language.tr("SystemInfo.Tools.NextStationTraining.DeleteData.Info"),Language.tr("SystemInfo.Tools.NextStationTraining.DeleteData.InfoYes"),Language.tr("SystemInfo.Tools.NextStationTraining.DeleteData.InfoNo"))) {
			EditModelProcessor.getInstance().reset();
			close(BaseDialog.CLOSED_BY_OK);
		}
	}
}

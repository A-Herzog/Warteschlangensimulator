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

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import language.Language;
import simulator.StartAnySimulator;
import simulator.editmodel.EditModel;

/**
 * Dialogseite "Simulationssystem"
 * @author Alexander Herzog
 * @see ModelPropertiesDialog
 * @see ModelPropertiesDialogPage
 */
public class ModelPropertiesDialogPageInfo extends ModelPropertiesDialogPage {
	/**
	 * Konstruktor der Klasse
	 * @param dialog	Dialog in dem sich diese Seite befindet
	 * @param model	Modell aus dem die Daten entnommen und in das die Daten geschrieben werden sollen
	 * @param readOnly	Nur-Lese-Status
	 * @param help	Hilfe-Callback
	 */
	public ModelPropertiesDialogPageInfo(ModelPropertiesDialog dialog, EditModel model, boolean readOnly, Runnable help) {
		super(dialog,model,readOnly,help);
	}

	@Override
	public void build(JPanel content) {
		final StartAnySimulator.PrepareError error=StartAnySimulator.testModel(model);
		final StringBuilder sb=new StringBuilder();
		sb.append("<html><body style=\"margin: 10px;\">");
		if (error!=null) {
			sb.append("<p style=\"margin-bottom: 10px\">"+Language.tr("Editor.Dialog.Tab.SimulationSystem.Error")+"</p>");
			sb.append("<p>"+Language.tr("Editor.Dialog.Tab.SimulationSystem.ErrorInfo")+":<br><b>"+error.error+"</b></p>");
		} else {
			sb.append("<p style=\"margin-bottom: 10px\">"+Language.tr("Editor.Dialog.Tab.SimulationSystem.Ok")+"</p>");

			java.util.List<String> infoSingleCore=model.getSingleCoreReason();
			if (infoSingleCore==null || infoSingleCore.size()==0) {
				sb.append("<p>"+Language.tr("Editor.Dialog.Tab.SimulationSystem.MultiCoreOk")+"</p>");
			} else {
				sb.append("<p>"+Language.tr("Editor.Dialog.Tab.SimulationSystem.SingleCoreOnly")+":</p>");
				sb.append("<ul>");
				for (String line: infoSingleCore) sb.append("<li>"+line+"</li>");
				sb.append("</ul>");
			}
		}

		if (model.repeatCount>1) {
			final String infoNoRepeat=model.getNoRepeatReason();
			if (infoNoRepeat==null) {
				sb.append("<p>"+String.format(Language.tr("Editor.Dialog.Tab.SimulationSystem.RepeatOk"),model.repeatCount)+"</p>");
			} else {
				sb.append("<p>"+String.format(Language.tr("Editor.Dialog.Tab.SimulationSystem.RepeatNotOk"),model.repeatCount)+"<br>"+infoNoRepeat+"</p>");
			}
		}

		sb.append("</body></html>");

		content.setLayout(new BorderLayout(10,10));
		content.add(new JLabel(sb.toString()),BorderLayout.NORTH);
	}
}

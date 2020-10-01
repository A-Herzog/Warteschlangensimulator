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
package ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import language.Language;
import systemtools.BaseDialog;
import ui.help.Help;
import ui.modeleditor.ModelLoadData;

/**
 * Zeigt mögliche Warnungen, die beim Laden der externen Daten in das
 * Modell aufgetreten sind, an. Der Dialog soll nur verwendet werden,
 * wenn auch tatsächlich Warnungen aufgetreten sind.
 * @author Alexander Herzog
 * @see ModelLoadData
 * @see ModelLoadData#changeModel(simulator.editmodel.EditModel, java.io.File)
 * @see ModelLoadData#changeModel(simulator.editmodel.EditModel, java.io.File, boolean)
 */
public class ModelLoadDataWarningsDialog extends BaseDialog {
	private static final long serialVersionUID=-8517430290293338838L;

	private static final String HTML_HEAD="<html><body><p style='padding: 2px 5px;'>";
	private static final String HTML_FOOT="</p></body></html>";

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param warnings	Liste mit den Warnungen (die Liste muss ungleich <code>null</code> sein und sollte nicht leer sein)
	 */
	public ModelLoadDataWarningsDialog(final Component owner, final List<String> warnings) {
		super(owner,Language.tr("ModelLoadData.ProcessError.DialogTitle"));

		final JPanel main=createGUI(()->Help.topicModal(this,"ModelLoadDataWarnings"));
		main.setLayout(new BorderLayout());

		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		line.add(new JLabel(HTML_HEAD+Language.tr("ModelLoadData.ProcessError.DialogInfo").replace("\n","<br>")+HTML_FOOT));
		main.add(line,BorderLayout.NORTH);

		final String[] displayWarnings=warnings.stream().map(s->HTML_HEAD+s.replace("\n","<br>")+HTML_FOOT).toArray(String[]::new);

		final JList<String> list=new JList<>(displayWarnings);
		main.add(new JScrollPane(list),BorderLayout.CENTER);

		/* Dialog starten */
		setMinSizeRespectingScreensize(600,400);
		setSizeRespectingScreensize(600,400);
		setResizable(true);
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}
}

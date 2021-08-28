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
import java.io.File;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.ProblemReporter;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Zeigt einen Dialog zur Auswahl der Element zur Erstellung eines
 * Fehlerberichts an und ermöglicht auch gleich die Erstellung dieses
 * Berichts.
 * @author Alexander Herzog
 * @see ProblemReporter
 */
public class ProblemReporterDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=4062065223537807453L;

	/**
	 * Eingabezeile für den Dateinamen der Ausgabedatei
	 */
	private final JTextField inputEdit;

	/**
	 * Auswahl der Ausgabe-Optionen
	 */
	private final JCheckBox[] options;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 */
	public ProblemReporterDialog(final Component owner) {
		super(owner,Language.tr("ProblemReporter.Dialog.SettingsTitle"));

		/* GUI */
		final JPanel content=createGUI(null);
		content.setLayout(new BorderLayout());

		JPanel line;

		/* Dateiauswahl */
		final Object[] data=ModelElementBaseDialog.getInputPanel(Language.tr("ProblemReporter.Dialog.OutputFile")+":",System.getProperty("user.home")+"\\Desktop\\ProblemReport.zip");
		content.add(line=(JPanel)data[0],BorderLayout.NORTH);
		inputEdit=(JTextField)data[1];
		final JButton button=new JButton(Images.GENERAL_SELECT_FILE.getIcon());
		line.add(button,BorderLayout.EAST);
		button.setToolTipText(Language.tr("ProblemReporter.Dialog.Title"));
		button.addActionListener(e->selectFile());

		/* Optionen */
		final JPanel optionsArea=new JPanel();
		content.add(optionsArea,BorderLayout.CENTER);
		optionsArea.setLayout(new BoxLayout(optionsArea,BoxLayout.PAGE_AXIS));
		optionsArea.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel(Language.tr("ProblemReporter.Dialog.Items")+":"));
		options=new JCheckBox[ProblemReporter.ReportItem.values().length];
		for (int i=0;i<options.length;i++) {
			final ProblemReporter.ReportItem item=ProblemReporter.ReportItem.values()[i];
			optionsArea.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			line.add(options[i]=new JCheckBox(item.getName()));
			if (item.isAvailable()) {
				options[i].setSelected(true);
			} else {
				options[i].setEnabled(false);
				options[i].setToolTipText(Language.tr("ProblemReporter.Dialog.ItemNotAvailable"));
			}
		}

		/* Dialog starten */
		setMinSizeRespectingScreensize(640,0);
		pack();
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	/**
	 * Zeigt einen Dialog zur Auswahl der Ausgabedatei an.
	 */
	private void selectFile() {
		final File lastFile;
		if (inputEdit.getText().trim().isEmpty()) lastFile=null; else lastFile=new File(inputEdit.getText());
		final File newFile=ProblemReporter.selectOutputFile(this,lastFile);
		if (newFile!=null) inputEdit.setText(newFile.toString());
	}

	@Override
	protected boolean checkData() {
		if (inputEdit.getText().trim().isEmpty()) {
			MsgBox.error(this,Language.tr("ProblemReporter.Dialog.OutputFile.ErrorTitle"),Language.tr("ProblemReporter.Dialog.OutputFile.ErrorInfo"));
			return false;
		}

		for (int i=0;i<options.length;i++) if (options[i].isSelected()) return true;
		MsgBox.error(this,Language.tr("ProblemReporter.Dialog.NoItemsErrorTitle"),Language.tr("ProblemReporter.Dialog.NoItemsErrorInfo"));
		return false;
	}

	@Override
	protected void storeData() {
		final File file=new File(inputEdit.getText());
		final Set<ProblemReporter.ReportItem> items=new HashSet<>();
		for (int i=0;i<options.length;i++) if (options[i].isSelected()) items.add(ProblemReporter.ReportItem.values()[i]);
		final ProblemReporter reporter=new ProblemReporter(file,items);
		if (!reporter.process()) MsgBox.error(this,Language.tr("ProblemReporter.SaveError.Title"),String.format(Language.tr("ProblemReporter.SaveError.Info"),file.toString()));
	}

}

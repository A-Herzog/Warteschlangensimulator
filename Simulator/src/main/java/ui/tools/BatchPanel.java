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
package ui.tools;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.PrintStream;
import java.io.Serializable;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import language.Language;
import mathtools.distribution.swing.CommonVariables;
import mathtools.distribution.tools.FileDropper;
import mathtools.distribution.tools.FileDropperData;
import systemtools.JTextAreaOutputStream;
import systemtools.MsgBox;
import systemtools.commandline.AbstractCommand;
import systemtools.images.SimToolsImages;
import tools.IconListCellRenderer;
import tools.Notifier;
import tools.SetupData;
import ui.commandline.CommandFolderFilter;
import ui.commandline.CommandFolderSimulation;
import ui.commandline.CommandLineSystem;
import ui.help.Help;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Stellt die Funktionen der Kommandozeilen-Befehle
 * "Verzeichnis" und "VerzeichnisFilter" über die
 * grafische Oberfläche zur Verfügung.
 * @author Alexander Herzog
 */
public class BatchPanel extends SpecialPanel {
	/** Setupdaten-Singleton */
	private final SetupData setupData;

	/** "Verarbeitung starten"/"Verarbeitung abbrechen"-Schaltfläche */
	private final JButton startStopButton;
	/** "Hilfe"-Schaltfläche */
	private final JButton helpButton;

	/** Eingabezeile für das Verzeichnis */
	private final JTextField folderEdit;
	/** Schaltfläche zur Auswahl des Verzeichnisses */
	private final JButton folderButton;
	/** Auswahlfeld für den Verarbeitungsmodus (Simulation oder Ergebnisse filtern) */
	private final JComboBox<String> modeSelect;
	/** Eingabezeile für das Filterskript */
	private final JTextField scriptEdit;
	/** Schaltfläche zur Auswahl des Filterskripts */
	private final JButton scriptButton;
	/** Eingabezeile für die Ausgabedatei */
	private final JTextField resultsEdit;
	/** Schaltfläche zur Auswahl der Ausgabedatei */
	private final JButton resultsButton;

	/** Ausgabebereich */
	private final JTextArea output;

	/** Thread für die Verarbeitung */
	private Thread thread;
	/** In {@link #thread} auszuführender Befehl */
	private AbstractCommand threadCommand;

	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-6493970997178389105L;

	/**
	 * Konstruktor der Klasse
	 * @param doneNotify	Runnable, das aufgerufen wird, wenn das Panel geschlossen werden soll
	 */
	public BatchPanel(final Runnable doneNotify) {
		super(doneNotify);

		setupData=SetupData.getSetup();
		thread=null;

		startStopButton=addUserButton("","",null);
		addCloseButton();
		helpButton=addUserButton(Language.tr("Main.Toolbar.Help"),Language.tr("Main.Toolbar.Help.Hint"),Images.HELP.getIcon());

		/* Mainpanel */

		final JPanel content=new JPanel(new BorderLayout());
		add(content,BorderLayout.CENTER);

		JPanel line;
		JLabel label;
		Object[] data;

		/* Einstellungsbereich */

		final JPanel setup=new JPanel();
		content.add(setup,BorderLayout.NORTH);
		setup.setLayout(new BoxLayout(setup,BoxLayout.PAGE_AXIS));

		/* Verzeichnis */

		data=ModelElementBaseDialog.getInputPanel(Language.tr("BatchPanel.Directory")+":","");
		setup.add(line=(JPanel)data[0]);
		folderEdit=(JTextField)data[1];
		line.add(folderButton=new JButton(Images.GENERAL_SELECT_FOLDER.getIcon()),BorderLayout.EAST);
		folderButton.addActionListener(e->commandSelectFolder());
		folderButton.setToolTipText(Language.tr("BatchPanel.Directory.Hint"));
		if (setupData.batchFolder.trim().isEmpty()) folderEdit.setText(System.getProperty("user.home")+"\\Desktop"); else folderEdit.setText(setupData.batchFolder);

		/* Modus */

		setup.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("BatchPanel.Mode")+":"));
		line.add(modeSelect=new JComboBox<>(new String[] {
				Language.tr("BatchPanel.Mode.Simulation"),
				Language.tr("BatchPanel.Mode.Filter")
		}));
		label.setLabelFor(modeSelect);
		modeSelect.setRenderer(new IconListCellRenderer(new Icon[] {
				Images.SIMULATION.getIcon(),
				SimToolsImages.STATISTICS_SPECIAL.getIcon()
		}));
		switch (setupData.batchMode) {
		case SIMULATION: modeSelect.setSelectedIndex(0); break;
		case FILTER: modeSelect.setSelectedIndex(1); break;
		}
		modeSelect.addActionListener(e->updateGUI());

		/* Filterskript */

		data=ModelElementBaseDialog.getInputPanel(Language.tr("BatchPanel.Script")+":","");
		setup.add(line=(JPanel)data[0]);
		scriptEdit=(JTextField)data[1];
		line.add(scriptButton=new JButton(Images.GENERAL_SELECT_FILE.getIcon()),BorderLayout.EAST);
		scriptButton.addActionListener(e->commandSelectScript());
		scriptButton.setToolTipText(Language.tr("BatchPanel.Script.Hint"));
		scriptEdit.setText(setupData.batchFilterScript);

		/* Ausgabedatei */

		data=ModelElementBaseDialog.getInputPanel(Language.tr("BatchPanel.ResultsFile")+":","");
		setup.add(line=(JPanel)data[0]);
		resultsEdit=(JTextField)data[1];
		line.add(resultsButton=new JButton(Images.GENERAL_SELECT_FILE.getIcon()),BorderLayout.EAST);
		resultsButton.addActionListener(e->commandSelectResultsFile());
		resultsButton.setToolTipText(Language.tr("BatchPanel.ResultsFile.Hint"));
		resultsEdit.setText(setupData.batchOutputFile);

		/* Ausgabebereich */

		content.add(new JScrollPane(output=new JTextArea()),BorderLayout.CENTER);
		output.setEditable(false);

		/* F1-Hotkey */

		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F1"),"actionHelp");
		getActionMap().put("actionHelp",new AbstractAction("actionHelp") {
			private static final long serialVersionUID=8849520419758038750L;
			@Override public void actionPerformed(ActionEvent event) {commandHelp();}
		});

		/* Drag&Drop */

		new FileDropper(folderEdit,e->{
			final FileDropperData dropData=(FileDropperData)e.getSource();
			if (dropData.getFile()!=null) folderEdit.setText(dropData.getFile().toString());
			dropData.dragDropConsumed();
		});

		new FileDropper(scriptEdit,e->{
			final FileDropperData dropData=(FileDropperData)e.getSource();
			if (dropData.getFile()!=null) scriptEdit.setText(dropData.getFile().toString());
			dropData.dragDropConsumed();
		});

		new FileDropper(resultsEdit,e->{
			final FileDropperData dropData=(FileDropperData)e.getSource();
			if (dropData.getFile()!=null) resultsEdit.setText(dropData.getFile().toString());
			dropData.dragDropConsumed();
		});

		/* Starten */

		updateGUI();
	}

	/**
	 * Aktualisiert den Status der Eingabefelder und Schaltflächen
	 */
	private void updateGUI() {
		if (thread!=null) {
			startStopButton.setIcon(Images.GENERAL_CANCEL.getIcon());
			startStopButton.setText(Language.tr("BatchPanel.ButtonCancel"));
			startStopButton.setToolTipText(Language.tr("BatchPanel.ButtonCancel.Hint"));
		} else {
			startStopButton.setIcon(Images.PARAMETERSERIES_RUN.getIcon());
			startStopButton.setText(Language.tr("BatchPanel.ButtonStart"));
			startStopButton.setToolTipText(Language.tr("BatchPanel.ButtonStart.Hint"));
		}

		folderEdit.setEnabled(thread==null);
		folderButton.setEnabled(thread==null);
		modeSelect.setEnabled(thread==null);
		scriptEdit.setEnabled(thread==null && modeSelect.getSelectedIndex()==1);
		scriptButton.setEnabled(thread==null && modeSelect.getSelectedIndex()==1);
		resultsEdit.setEnabled(thread==null && modeSelect.getSelectedIndex()==1);
		resultsButton.setEnabled(thread==null && modeSelect.getSelectedIndex()==1);
	}

	/**
	 * Befehl: Verzeichnis auswählen
	 */
	private void commandSelectFolder() {
		final JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		final String oldFolder=folderEdit.getText().trim();
		if (!oldFolder.trim().isEmpty() && new File(oldFolder).isDirectory()) fc.setCurrentDirectory(new File(oldFolder));
		fc.setDialogTitle(Language.tr("BatchPanel.Directory.Caption"));
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (fc.showOpenDialog(this)!=JFileChooser.APPROVE_OPTION) return;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		folderEdit.setText(fc.getSelectedFile().toString());
	}

	/**
	 * Befehl: Filterskript auswählen
	 */
	private void commandSelectScript() {
		final JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		final String oldFileName=scriptEdit.getText();
		if (oldFileName!=null && !oldFileName.isEmpty()) fc.setCurrentDirectory(new File(oldFileName).getParentFile());
		fc.setDialogTitle(Language.tr("BatchPanel.Script.Caption"));
		final FileFilter script=new FileNameExtensionFilter(Language.tr("FileType.JSAndJava")+" (*.js,*.java)","js","java");
		final FileFilter js=new FileNameExtensionFilter(Language.tr("FileType.JS")+" (*.js)","js");
		final FileFilter java=new FileNameExtensionFilter(Language.tr("FileType.Java")+" (*.java)","java");
		fc.addChoosableFileFilter(script);
		fc.addChoosableFileFilter(js);
		fc.addChoosableFileFilter(java);
		fc.setFileFilter(script);
		if (fc.showOpenDialog(this)!=JFileChooser.APPROVE_OPTION) return;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();
		if (file.getName().indexOf('.')<0 && fc.getFileFilter()==js) file=new File(file.getAbsoluteFile()+".js");
		if (file.getName().indexOf('.')<0 && fc.getFileFilter()==java) file=new File(file.getAbsoluteFile()+".java");
		scriptEdit.setText(file.getAbsolutePath());
	}

	/**
	 * Befehl: Ausgabedatei auswählen
	 */
	private void commandSelectResultsFile() {
		final JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		final String oldFileName=resultsEdit.getText();
		if (oldFileName!=null && !oldFileName.isEmpty()) fc.setCurrentDirectory(new File(oldFileName).getParentFile());
		fc.setDialogTitle(Language.tr("BatchPanel.ResultsFile.Caption"));
		final FileFilter text=new FileNameExtensionFilter(Language.tr("FileType.Text")+" (*.txt)","txt");
		fc.addChoosableFileFilter(text);
		fc.setFileFilter(text);
		if (fc.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION) return;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();
		if (file.getName().indexOf('.')<0 && fc.getFileFilter()==text) file=new File(file.getAbsoluteFile()+".txt");
		resultsEdit.setText(file.getAbsolutePath());
	}

	/**
	 * Befehl: Verarbeitung starten / abbrechen
	 */
	private void commandStartStop() {
		if (thread==null) {
			threadCommand=getCommand();
			if (threadCommand==null) return;
			output.setText("");
			thread=new Thread(()->{
				final long startTime=System.currentTimeMillis();
				try (PrintStream stream=new PrintStream(new JTextAreaOutputStream(output,true))) {
					threadCommand.run(new AbstractCommand[0],null,stream);
				}
				thread=null;
				SwingUtilities.invokeLater(()->{
					if (threadCommand instanceof CommandFolderSimulation) Notifier.run(Notifier.Message.SIMULATION_DONE,startTime);
					updateGUI();
				});
			});
			thread.start();
			updateGUI();
		} else {
			if (threadCommand!=null) threadCommand.setQuit();
			thread=null;
			updateGUI();
		}
	}

	/**
	 * Liefert den zu den Einstellungen passenden Kommandozeilenbefehl
	 * @return	Kommandozeilenbefehl oder <code>null</code>, wenn die Einstellungen ungültig sind
	 */
	private AbstractCommand getCommand() {
		/* Verzeichnis */
		final File folder=new File(folderEdit.getText().trim());
		if (!folder.isDirectory()) {
			MsgBox.error(this,Language.tr("BatchPanel.Directory.ErrorTitle"),String.format(Language.tr("BatchPanel.Directory.ErrorInfo"),folderEdit.getText()));
			return null;
		}

		/* Skriptdatei */
		final File scriptFile;
		final File resultsFile;
		if (modeSelect.getSelectedIndex()==1) {
			scriptFile=new File(scriptEdit.getText().trim());
			if (!scriptFile.isFile()) {
				MsgBox.error(this,Language.tr("BatchPanel.Script.ErrorTitle"),String.format(Language.tr("BatchPanel.Script.ErrorInfo"),scriptEdit.getText()));
				return null;
			}
			if (resultsEdit.getText().trim().isEmpty()) {
				MsgBox.error(this,Language.tr("BatchPanel.ResultsFile.ErrorTitle"),Language.tr("BatchPanel.ResultsFile.ErrorInfo"));
				return null;
			}
			resultsFile=new File(resultsEdit.getText().trim());
		} else {
			scriptFile=null;
			resultsFile=null;
		}

		/* Befehl erstellen */
		final CommandLineSystem cli=new CommandLineSystem(null,null);
		if (scriptFile==null) {
			return new CommandFolderSimulation(cli,folder);
		} else {
			return new CommandFolderFilter(cli,folder,scriptFile,resultsFile);
		}
	}

	/**
	 * Befehl: Hilfe
	 */
	private void commandHelp() {
		Help.topicModal(BatchPanel.this,"Batch");
	}

	@Override
	protected void userButtonClick(int index, JButton button) {
		if (button==startStopButton) {commandStartStop(); return;}
		if (button==helpButton) {commandHelp(); return;}
	}

	@Override
	public void requestClose() {
		if (thread!=null) commandStartStop();

		setupData.batchFolder=folderEdit.getText().trim();
		switch (modeSelect.getSelectedIndex()) {
		case 0:
			setupData.batchMode=SetupData.BatchMode.SIMULATION;
			break;
		case 1:
			setupData.batchMode=SetupData.BatchMode.FILTER;
			setupData.batchFilterScript=scriptEdit.getText().trim();
			setupData.batchOutputFile=resultsEdit.getText().trim();
			break;
		}
		setupData.saveSetup();

		close();
	}
}

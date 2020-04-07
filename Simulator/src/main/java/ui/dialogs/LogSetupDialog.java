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
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import language.Language;
import mathtools.distribution.swing.CommonVariables;
import net.dde.DDEConnect;
import simcore.logging.SimLogging;
import simulator.logging.DDELogger;
import simulator.logging.MultiTypeTextLogger;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.IconListCellRenderer;
import tools.SetupData;
import ui.help.Help;
import ui.images.Images;
import ui.modeleditor.elements.DDEEditPanel;

/**
 * Zeigt einen Dialog zur Auswahl einer Logdatei und der Logging-Optionen an.
 * @author Alexander Herzog
 */
public class LogSetupDialog extends BaseDialog {
	private static final long serialVersionUID = -6600117003814706688L;

	private final JComboBox<String> logMode;
	private final JPanel page;
	private final CardLayout pageLayout;

	private final JTextField logFileEdit;
	private final JButton logFileButton;
	private final JCheckBox optionMultiLine;
	private final JCheckBox optionGroup;
	private final JCheckBox optionColor;

	private final DDEEditPanel editDDE;

	/**
	 * Konstruktor der Klasse <code>LogSetupDialog</code>
	 * @param owner	Übergeordnetes Element
	 */
	public LogSetupDialog(final Component owner) {
		super(owner,Language.tr("LogSimulation.Title"));

		JPanel card;
		JPanel line;
		JLabel label;

		final Runnable helpRunnable=()->Help.topicModal(LogSetupDialog.this,"LogSimulation");
		final JPanel content=createGUI(helpRunnable);
		content.setLayout(new BorderLayout());

		/* Auswahl der Ausgabemöglichkeiten */

		if (new DDEConnect().available()) {
			content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);
			line.add(label=new JLabel(Language.tr("LogSimulation.Mode")+": "));
			line.add(logMode=new JComboBox<>(new String[] {
					Language.tr("LogSimulation.Mode.File"),
					Language.tr("LogSimulation.Mode.Excel")
			}));
			logMode.setRenderer(new IconListCellRenderer(new Images[]{
					Images.SIMULATION_LOG_MODE_FILE,
					Images.SIMULATION_LOG_MODE_TABLE
			}));
		} else {
			logMode=null;
		}

		content.add(page=new JPanel(pageLayout=new CardLayout()));
		page.setBorder(BorderFactory.createEmptyBorder(15,5,5,5));

		/* Seite "Datei" */

		page.add(card=new JPanel(),"0");
		card.setLayout(new BoxLayout(card,BoxLayout.PAGE_AXIS));

		card.add(line=new JPanel(new BorderLayout()));
		line.add(label=new JLabel(Language.tr("LogSimulation.LogFile")+": "),BorderLayout.WEST);
		line.add(logFileEdit=new JTextField(),BorderLayout.CENTER);
		label.setLabelFor(logFileEdit);
		line.add(logFileButton=new JButton(),BorderLayout.EAST);
		logFileButton.setIcon(Images.GENERAL_SELECT_FILE.getIcon());
		logFileButton.setPreferredSize(new Dimension(24,24));
		logFileButton.setToolTipText(Language.tr("LogSimulation.LogFile.Select"));
		logFileButton.addActionListener(new ButtonListener());

		card.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionMultiLine=new JCheckBox(Language.tr("LogSimulation.OptionMultiLine")));

		card.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionGroup=new JCheckBox(Language.tr("LogSimulation.OptionGroup")));

		card.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionColor=new JCheckBox(Language.tr("LogSimulation.OptionColor")));
		optionColor.setToolTipText(Language.tr("LogSimulation.OptionColor.Info"));

		/* Seite "DDE" */

		if (logMode!=null) {
			page.add(card=new JPanel(),"1");
			card.setLayout(new BorderLayout());
			card.add(editDDE=new DDEEditPanel(this,"","",readOnly,helpRunnable),BorderLayout.NORTH);
		} else {
			editDDE=null;
		}

		/* Daten in Dialog laden */

		final SetupData setup=SetupData.getSetup();

		logFileEdit.setText(setup.lastLogFile);
		optionMultiLine.setSelected(!setup.singleLineEventLog);
		optionGroup.setSelected(setup.logGrouped);
		optionColor.setSelected(setup.logColors);
		if (logMode==null) {
			pageLayout.show(page,"0");
		} else {
			editDDE.setWorkbook(setup.logDDEworkbook);
			editDDE.setTable(setup.logDDEsheet);
			logMode.addActionListener(e->pageLayout.show(page,""+logMode.getSelectedIndex()));
			switch (setup.logMode) {
			case FILE: logMode.setSelectedIndex(0); break;
			case DDE: logMode.setSelectedIndex(1); break;
			}
		}

		/* Dialog starten */

		pack();
		setLocationRelativeTo(this.owner);
	}

	private class ButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {

			JFileChooser fc=new JFileChooser();
			CommonVariables.initialDirectoryToJFileChooser(fc);
			fc.setDialogTitle(Language.tr("LogSimulation.LogFile.Select"));
			FileFilter docx=new FileNameExtensionFilter(Language.tr("FileType.Word")+" (*.docx)","docx");
			FileFilter rtf=new FileNameExtensionFilter(Language.tr("FileType.RTF")+" (*.rtf)","rtf");
			FileFilter html=new FileNameExtensionFilter(Language.tr("FileType.HTML")+" (*.html, *.htm)","html","htm");
			FileFilter txt=new FileNameExtensionFilter(Language.tr("FileType.Text")+" (*.txt)","txt");
			FileFilter pdf=new FileNameExtensionFilter(Language.tr("FileType.PDF")+" (*.pdf)","pdf");
			FileFilter xlsx=new FileNameExtensionFilter(Language.tr("FileType.Excel")+" (*.xlsx)","xlsx");
			FileFilter xls=new FileNameExtensionFilter(Language.tr("FileType.ExcelOld")+" (*.xls)","xls");
			FileFilter ods=new FileNameExtensionFilter(Language.tr("FileType.FileTypeODS")+" (*.ods)","ods");
			FileFilter odt=new FileNameExtensionFilter(Language.tr("FileType.FileTypeODT")+" (*.odt)","odt");
			fc.addChoosableFileFilter(xlsx);
			fc.addChoosableFileFilter(xls);
			fc.addChoosableFileFilter(ods);
			fc.addChoosableFileFilter(txt);
			fc.addChoosableFileFilter(docx);
			fc.addChoosableFileFilter(rtf);
			fc.addChoosableFileFilter(html);
			fc.addChoosableFileFilter(pdf);
			fc.addChoosableFileFilter(odt);
			fc.setFileFilter(txt);

			if (fc.showSaveDialog(owner)!=JFileChooser.APPROVE_OPTION) return;
			CommonVariables.initialDirectoryFromJFileChooser(fc);
			File file=fc.getSelectedFile();

			if (file.getName().indexOf('.')<0) {
				if (fc.getFileFilter()==xlsx) file=new File(file.getAbsoluteFile()+".xlsx");
				if (fc.getFileFilter()==xls) file=new File(file.getAbsoluteFile()+".xls");
				if (fc.getFileFilter()==ods) file=new File(file.getAbsoluteFile()+".ods");
				if (fc.getFileFilter()==docx) file=new File(file.getAbsoluteFile()+".docx");
				if (fc.getFileFilter()==rtf) file=new File(file.getAbsoluteFile()+".rtf");
				if (fc.getFileFilter()==html) file=new File(file.getAbsoluteFile()+".html");
				if (fc.getFileFilter()==txt) file=new File(file.getAbsoluteFile()+".txt");
				if (fc.getFileFilter()==pdf) file=new File(file.getAbsoluteFile()+".pdf");
				if (fc.getFileFilter()==odt) file=new File(file.getAbsoluteFile()+".odt");
			}

			logFileEdit.setText(file.toString());
		}
	}

	@Override
	protected boolean checkData() {
		final int mode=(logMode==null)?0:logMode.getSelectedIndex();


		if (mode==0) {
			final File file=new File(logFileEdit.getText());
			if (file.exists()) {
				if (!MsgBox.confirmOverwrite(this,file)) return false;
			}
			File path=file.getParentFile();
			if (path==null || !path.exists()) {
				MsgBox.error(this,Language.tr("Dialog.InvalidFile.Title"),String.format(Language.tr("Dialog.InvalidFile.Info"),file.toString()));
				return false;
			}
			return true;
		}

		if (mode==1) {
			if (editDDE!=null) return editDDE.checkData(true);
			return true;
		}

		return false;
	}

	@Override
	protected void storeData() {
		final SetupData setup=SetupData.getSetup();

		if (logMode!=null) {
			switch (logMode.getSelectedIndex()) {
			case 0: setup.logMode=SetupData.LogMode.FILE; break;
			case 1: setup.logMode=SetupData.LogMode.DDE; break;
			}
		}

		setup.lastLogFile=logFileEdit.getText();
		setup.singleLineEventLog=!optionMultiLine.isSelected();
		setup.logGrouped=optionGroup.isSelected();
		setup.logColors=optionColor.isSelected();

		if (logMode!=null) {
			setup.logDDEworkbook=editDDE.getWorkbook();
			setup.logDDEsheet=editDDE.getTable();
		}

		setup.saveSetup();
	}

	/**
	 * Liefert auf Basis der Einstellungen in dem Dialog einen entsprechenden Logger zurück.
	 * @return	Logger, der gemäß den Einstellungen aus dem Dialog Daten aufzeichnet (oder im Fehlerfall <code>null</code>
	 */
	public SimLogging getLogger() {
		final int mode=(logMode==null)?0:logMode.getSelectedIndex();

		if (mode==0) {
			final File file=new File(logFileEdit.getText());
			if (file.exists()) {
				if (!file.delete()) {
					MsgBox.error(this,Language.tr("LogSimulation.ErrorDelete.Title"),String.format(Language.tr("LogSimulation.ErrorDelete.Info"),file.toString()));
					return null;
				}
			}
			return new MultiTypeTextLogger(file,optionGroup.isSelected(),!optionMultiLine.isSelected(),optionColor.isSelected(),new String[]{Language.tr("LogSimulation.Heading")});
		}

		if (mode==1) {
			if (editDDE!=null) return new DDELogger(editDDE.getWorkbook(),editDDE.getTable());
		}

		return null;
	}
}

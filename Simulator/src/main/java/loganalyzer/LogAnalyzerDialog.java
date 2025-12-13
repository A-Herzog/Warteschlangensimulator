/**
 * Copyright 2024 Alexander Herzog
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
package loganalyzer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.Serializable;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.swing.PlugableFileChooser;
import mathtools.distribution.tools.FileDropper;
import mathtools.distribution.tools.FileDropperData;
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.EditorPanelBase;
import ui.help.Help;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;
import xml.XMLTools;

/**
 * Zeigt einen Dialog zur Analyse einer Simulations-Log-Datei.
 * @see LogAnalyzer
 */
public class LogAnalyzerDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=2091286122935234099L;

	/**
	 * Aktuelles Modell im Editor
	 */
	private EditModel currentModel;

	/**
	 * Dateiname der Loggingdatei
	 */
	private final JTextField logFileEdit;

	/**
	 * Option: Keine Modellinformationen bei der Interpretation der Logging-Daten verwenden
	 */
	private final JRadioButton optionNoModel;

	/**
	 * Option: Aktuelles Modell bei der Interpretation der Logging-Daten verwenden
	 * @see #currentModel
	 */
	private final JRadioButton optionCurrentModel;

	/**
	 * Option: Modellinformationen aus externer Datei bei der Interpretation der Logging-Daten verwenden
	 * @see #modelFileEdit
	 */
	private final JRadioButton optionModelFromFile;

	/**
	 * Optional zu verwendende Modelldatei
	 * @see #optionModelFromFile
	 */
	private final JTextField modelFileEdit;

	/**
	 * Ausgabedatei für die Log-Datei-Analyse
	 */
	private final JTextField outputFileEdit;

	/**
	 * Analyse-System für Log-Dateien
	 * (wird von {@link #checkData(boolean)} befüllt und von {@link #storeData()} verwendet)
	 * @see #checkData(boolean)
	 * @see #storeData()
	 */
	private LogAnalyzer analyzer;

	/**
	 * Konstruktor
	 * @param owner	Übergeordnete Komponente
	 * @param model	Aktuelles Modell im Editor
	 */
	public LogAnalyzerDialog(final Component owner, final EditModel model) {
		super(owner,Language.tr("LogAnalyzer.Title"));
		this.currentModel=model;

		/* GUI */
		final JPanel content=createGUI(600,400,()->Help.topicModal(this,"LogAnalyzer"));
		content.setLayout(new BorderLayout());

		final JPanel setup=new JPanel();
		content.add(setup,BorderLayout.NORTH);
		setup.setLayout(new BoxLayout(setup,BoxLayout.PAGE_AXIS));

		/* Logdatei */
		logFileEdit=addFileEditor(setup,Language.tr("LogAnalyzer.LogFile"),Language.tr("LogAnalyzer.LogFile.Select"),()->selectLogFile(),true,-1);

		setup.add(Box.createVerticalStrut(10));

		final boolean currentModelIsEmpty=(model.surface.count()==0);

		/* Modell */
		JPanel line;
		setup.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel(Language.tr("LogAnalyzer.UseModel")+":"));
		setup.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionNoModel=new JRadioButton(Language.tr("LogAnalyzer.UseModel.Off"),currentModelIsEmpty));
		optionNoModel.addActionListener(e->checkData(false));
		setup.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionCurrentModel=new JRadioButton(Language.tr("LogAnalyzer.UseModel.FromEditor"),!currentModelIsEmpty));
		optionCurrentModel.addActionListener(e->checkData(false));
		setup.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionModelFromFile=new JRadioButton(Language.tr("LogAnalyzer.UseModel.FromFile")+":"));
		optionModelFromFile.addActionListener(e->checkData(false));
		modelFileEdit=addFileEditor(line,null,null,()->selectModelFile(),true,40);
		modelFileEdit.addKeyListener(new KeyAdapter() {
			@Override public void keyReleased(KeyEvent e) {optionModelFromFile.setSelected(true); checkData(false);}
		});

		final ButtonGroup buttonGroup=new ButtonGroup();
		buttonGroup.add(optionNoModel);
		buttonGroup.add(optionCurrentModel);
		buttonGroup.add(optionModelFromFile);

		setup.add(Box.createVerticalStrut(10));

		/* Ausgabedatei */
		outputFileEdit=addFileEditor(setup,Language.tr("LogAnalyzer.OutputFile"),Language.tr("LogAnalyzer.OutputFile.Tooltip"),()->selectOutputFile(),false,-1);

		/* Start */
		checkData(false);
		pack();
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	/**
	 * Fügt eine Dateiauswahlelement ein.
	 * @param parent	Übergeordnetes Element
	 * @param labelText	Beschriftung für das Eingabefeld
	 * @param tooltipSelect	Tooltip für die Auswahlbox hinter der Eingabezeile
	 * @param selectCallback	Funktion die beim Anklicken der Auswahl-Schaltfläche aufgerufen werden soll
	 * @param dragDropLoad	Soll ein Drag&amp;Drop-Laden ermöglicht werden?
	 * @param inputWidth	Breite der Eingabezeile (Werte &le;0 für "verfügbare Breite")
	 * @return	Neue Eingabezeile
	 */
	private JTextField addFileEditor(final JPanel parent, final String labelText, final String tooltipSelect, final Runnable selectCallback, final boolean dragDropLoad, final int inputWidth) {
		final JPanel line;
		JPanel area;
		final JLabel label;
		final JTextField field;
		final JButton button;

		parent.add(line=new JPanel(new BorderLayout()));
		if (labelText!=null && !labelText.isBlank()) {
			line.add(area=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.WEST);
			area.add(label=new JLabel(labelText+": "));
		} else {
			label=null;
		}
		if (inputWidth>0) {
			line.add(field=new JTextField(inputWidth),BorderLayout.CENTER);
		} else {
			line.add(field=new JTextField(),BorderLayout.CENTER);
		}
		ModelElementBaseDialog.addUndoFeature(field);
		if (label!=null) label.setLabelFor(field);
		line.add(area=new JPanel(new FlowLayout(FlowLayout.LEFT,5,0)),BorderLayout.EAST);
		area.setPreferredSize(new Dimension(30,24));
		area.add(button=new JButton());
		button.setIcon(Images.GENERAL_SELECT_FILE.getIcon());
		button.setPreferredSize(new Dimension(24,24));
		button.setToolTipText(tooltipSelect);
		button.addActionListener(e->selectCallback.run());

		if (dragDropLoad) {
			new FileDropper(field,e->{
				final FileDropperData data=(FileDropperData)e.getSource();
				field.setText(data.getFile().toString());
				checkData(false);
				data.dragDropConsumed();
			});
		}

		field.addKeyListener(new KeyAdapter() {
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
		});

		return field;
	}

	/**
	 * Wird aufgerufen, wenn der Nutzer die Schaltfläche zur Dateiauswahl für die Log-Datei anklickt.
	 */
	public void selectLogFile() {
		final var fc=new PlugableFileChooser(true);
		fc.setDialogTitle(Language.tr("LogSimulation.LogFile.Select"));
		fc.addChoosableFileFilter(Language.tr("FileType.Text")+" (*.txt)","txt");
		fc.setFileFilter("txt");
		fc.setAcceptAllFileFilterUsed(true);

		final File file=fc.showOpenDialogFileWithExtension(owner);
		if (file==null) return;

		logFileEdit.setText(file.toString());
		checkData(false);
	}

	/**
	 * Wird aufgerufen, wenn der Nutzer die Schaltfläche zur Dateiauswahl für die Modelldatei anklickt.
	 */
	public void selectModelFile() {
		final File file=XMLTools.showLoadDialog(getParent(),EditorPanelBase.LOAD_MODEL);
		if (file==null) return;

		optionModelFromFile.setSelected(true);
		modelFileEdit.setText(file.toString());
		checkData(false);
	}

	/**
	 * Wird aufgerufen, wenn der Nutzer die Schaltfläche zur Dateiauswahl für die Ausgabedatei anklickt.
	 */
	public void selectOutputFile() {
		final var fc=new PlugableFileChooser(true);
		fc.setDialogTitle(Language.tr("LogAnalyzer.OutputFile.Select"));
		fc.addChoosableFileFilter(Language.tr("FileType.PDF")+" (*.pdf)","pdf");
		fc.addChoosableFileFilter(Language.tr("FileType.svg")+" (*.svg)","svg");
		fc.addChoosableFileFilter(Language.tr("FileType.eps")+" (*.eps)","eps");
		fc.addChoosableFileFilter(Language.tr("FileType.drawio")+" (*.drawio)","drawio");
		fc.addChoosableFileFilter(Language.tr("FileType.Text")+" (*.txt)","txt");
		fc.setFileFilter("pdf");
		fc.setAcceptAllFileFilterUsed(false);

		final File file=fc.showSaveDialogFileWithExtension(this);
		if (file==null) return;

		outputFileEdit.setText(file.toString());
		checkData(false);
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessage) {
		boolean ok=true;

		/* Log-Datei */
		final String logFileString=logFileEdit.getText().trim();
		if (logFileString.isBlank() || !new File(logFileString).isFile()) {
			logFileEdit.setBackground(Color.RED);
			ok=false;
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("LogAnalyzer.LogFile.ErrorTitle"),Language.tr("LogAnalyzer.LogFile.ErrorInfo"));
				return false;
			}
		} else {
			logFileEdit.setBackground(NumberTools.getTextFieldDefaultBackground());
		}

		/* Modelldatei */
		if (optionModelFromFile.isSelected()) {
			final String modelFileString=modelFileEdit.getText().trim();
			if (modelFileString.isBlank() || !new File(modelFileString).isFile()) {
				modelFileEdit.setBackground(Color.RED);
				ok=false;
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("LogAnalyzer.UseModel.ErrorTitle"),Language.tr("LogAnalyzer.UseModel.ErrorInfo"));
					return false;
				}
			} else {
				modelFileEdit.setBackground(NumberTools.getTextFieldDefaultBackground());
			}
		} else {
			modelFileEdit.setBackground(NumberTools.getTextFieldDefaultBackground());
		}

		/* Ausgabedatei */
		if (outputFileEdit.getText().isBlank()) {
			outputFileEdit.setBackground(Color.RED);
			ok=false;
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("LogAnalyzer.OutputFile.ErrorTitle"),Language.tr("LogAnalyzer.OutputFile.ErrorInfo"));
				return false;
			}
		} else {
			outputFileEdit.setBackground(NumberTools.getTextFieldDefaultBackground());
		}

		/* Weiterführende Tests und Vorbereitungen, wenn tatsächlich "Ok" angeklickt wurde */

		if (showErrorMessage && ok) {
			if (optionNoModel.isSelected()) analyzer=new LogAnalyzer();
			if (optionCurrentModel.isSelected()) analyzer=new LogAnalyzer(currentModel);
			if (optionModelFromFile.isSelected()) analyzer=new LogAnalyzer(modelFileEdit.getText());
			final String logFileName=logFileEdit.getText();
			if (!analyzer.load(logFileName)) {
				MsgBox.error(this,Language.tr("LogAnalyzer.ErrorTitle"),String.format(Language.tr("LogAnalyzer.ErrorInfo"),logFileName));
				return false;
			}
		}

		if (showErrorMessage && ok) {
			final File file=new File(outputFileEdit.getText());
			if (file.isFile()) {
				if (!MsgBox.confirmOverwrite(this,file)) return false;
			}
		}

		return ok;
	}

	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	@Override
	protected void storeData() {
		final File outputFile=new File(outputFileEdit.getText());
		final String outputFileName=outputFile.toString().toUpperCase();
		boolean done=false;
		boolean result=false;

		if (outputFileName.endsWith(".DRAWIO")) {
			done=true;
			final var exporter=new LogAnalyzerGraphicExporter(new Dimension(500,75),new Dimension(100,50));
			analyzer.export(exporter);
			final var writer=new LogAnalyzerGraphicExporterWriterDrawIO(exporter,outputFile);
			result=writer.save();
		}

		if (outputFileName.endsWith(".PDF")) {
			done=true;
			final var exporter=new LogAnalyzerGraphicExporter(new Dimension(500,75),new Dimension(100,50));
			analyzer.export(exporter);
			final var writer=new LogAnalyzerGraphicExporterWriterPDF(this,exporter,outputFile);
			result=writer.save();
		}

		if (outputFileName.endsWith(".SVG")) {
			done=true;
			final var exporter=new LogAnalyzerGraphicExporter(new Dimension(500,75),new Dimension(100,50));
			analyzer.export(exporter);
			final var writer=new LogAnalyzerGraphicExporterWriterVector(exporter,outputFile,LogAnalyzerGraphicExporterWriterVector.VectorFormat.SVG);
			result=writer.save();
		}

		if (outputFileName.endsWith(".EPS")) {
			done=true;
			final var exporter=new LogAnalyzerGraphicExporter(new Dimension(500,75),new Dimension(100,50));
			analyzer.export(exporter);
			final var writer=new LogAnalyzerGraphicExporterWriterVector(exporter,outputFile,LogAnalyzerGraphicExporterWriterVector.VectorFormat.EPS);
			result=writer.save();
		}

		if (!done) {
			done=true;
			final var exporter=new LogAnalyzerTextExporter();
			analyzer.export(exporter);
			result=exporter.save(outputFile);
		}

		if (!result) {
			MsgBox.error(this,Language.tr("LogAnalyzer.ErrorTitle"),String.format(Language.tr("LogAnalyzer.ErrorInfo"),outputFile.toString()));
		}
	}
}

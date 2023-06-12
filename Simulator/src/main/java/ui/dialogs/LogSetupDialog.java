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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.Box;
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
import mathtools.NumberTools;
import mathtools.distribution.swing.CommonVariables;
import net.dde.DDEConnect;
import simcore.logging.SimLogging;
import simulator.Simulator;
import simulator.logging.DDELogger;
import simulator.logging.MultiTypeTextLogger;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.IconListCellRenderer;
import tools.SetupData;
import ui.help.Help;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.elements.DDEEditPanel;
import ui.quickaccess.JPlaceholderTextField;

/**
 * Zeigt einen Dialog zur Auswahl einer Logdatei und der Logging-Optionen an.
 * @author Alexander Herzog
 */
public class LogSetupDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -6600117003814706688L;

	/** Auswahl der Logging-Art: Datei oder DDE */
	private final JComboBox<String> logMode;
	/** Panel, das die Seiten für Dateiauswahl und DDE-Einstellungen aufnimmt */
	private final JPanel page;
	/** Layout für das Panel, das die Seiten für Dateiauswahl und DDE-Einstellungen aufnimmt */
	private final CardLayout pageLayout;

	/* Seite "Datei" */

	/** Dateiname der Loggingdatei */
	private final JTextField logFileEdit;
	/** Zu erfassende Stations-IDs (Datei-Logging) */
	private final JPlaceholderTextField stationIDsFileEdit;
	/** Stationsankünfte erfassen (Datei-Logging) */
	private final JCheckBox optionFileTypeArrival;
	/** Stationsabgänge erfassen (Datei-Logging) */
	private final JCheckBox optionFileTypeLeave;
	/** Stations-Ereignisse erfassen (Datei-Logging) */
	private final JCheckBox optionFileTypeStation;
	/** System-Ereignisse erfassen (Datei-Logging) */
	private final JCheckBox optionFileTypeSystem;
	/** Mehrere Zeilen pro Ereignis? (Datei-Logging) */
	private final JCheckBox optionMultiLine;
	/** Ereignisse mit selbem Zeitpunkt gruppieren? (Datei-Logging) */
	private final JCheckBox optionGroup;
	/** Farben verwenden? (Datei-Logging) */
	private final JCheckBox optionColor;
	/** Zeiten formatiert ausgeben? (Datei-Logging) */
	private final JCheckBox optionFormatedTime;
	/** Stations-IDs in eigener Spalte ausgeben? (Datei-Logging) */
	private final JCheckBox optionPrintIDs;
	/** Anzahl an Einträgen begrenzen? */
	private final JCheckBox optionMaxRecords;
	/** Maximale Anzahl an Einträgen */
	private final JTextField maxRecords;

	/* Seite "DDE" */

	/** DDE-Einstellungen */
	private final DDEEditPanel editDDE;
	/** Zu erfassende Stations-IDs (DDE-Logging) */
	private final JPlaceholderTextField stationIDsDDEEdit;
	/** Stationsankünfte erfassen (DDE-Logging) */
	private final JCheckBox optionDDETypeArrival;
	/** Stationsabgänge erfassen (DDE-Logging) */
	private final JCheckBox optionDDETypeLeave;
	/** Stations-Ereignisse erfassen (DDE-Logging) */
	private final JCheckBox optionDDETypeStation;
	/** System-Ereignisse erfassen (DDE-Logging) */
	private final JCheckBox optionDDETypeSystem;

	/**
	 * Konstruktor der Klasse <code>LogSetupDialog</code>
	 * @param owner	Übergeordnetes Element
	 */
	public LogSetupDialog(final Component owner) {
		super(owner,Language.tr("LogSimulation.Title"));

		JPanel card;
		JPanel outer;
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
			label.setLabelFor(logMode);
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

		logFileEdit=addFileEditor(card,Language.tr("LogSimulation.LogFile"),Language.tr("LogSimulation.LogFile.Select"));

		addHeading(card,Language.tr("LogSimulation.Restrictions"),true);

		stationIDsFileEdit=addPlaceholderEdit(card,Language.tr("LogSimulation.StationIDs"),Language.tr("LogSimulation.StationIDs.Info"),Language.tr("LogSimulation.StationIDs.Placeholder"));
		optionFileTypeArrival=addOption(card,Language.tr("LogSimulation.Mode.Arrival"));
		optionFileTypeLeave=addOption(card,Language.tr("LogSimulation.Mode.Leave"));
		optionFileTypeStation=addOption(card,Language.tr("LogSimulation.Mode.InfoStation"));
		optionFileTypeSystem=addOption(card,Language.tr("LogSimulation.Mode.InfoSystem"));

		addHeading(card,Language.tr("LogSimulation.OutputFormat"),true);

		optionMultiLine=addOption(card,Language.tr("LogSimulation.OptionMultiLine"));
		optionGroup=addOption(card,Language.tr("LogSimulation.OptionGroup"));
		optionColor=addOption(card,Language.tr("LogSimulation.OptionColor"),Language.tr("LogSimulation.OptionColor.Info"),null);
		optionFormatedTime=addOption(card,Language.tr("LogSimulation.FormatTime"),Language.tr("LogSimulation.FormatTime.Info"),null);
		optionPrintIDs=addOption(card,Language.tr("LogSimulation.PrintIDs"));
		optionMaxRecords=addOption(card,Language.tr("LogSimulation.LimitRecords")+":",Language.tr("LogSimulation.LimitRecords.Info"),maxRecords=new JTextField(6));
		ModelElementBaseDialog.addUndoFeature(maxRecords);
		optionMaxRecords.addActionListener(e->checkData(false));
		maxRecords.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);	}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		/* Seite "DDE" */

		if (logMode!=null) {
			page.add(outer=new JPanel(),"1");
			outer.setLayout(new BorderLayout());

			outer.add(card=new JPanel(),BorderLayout.NORTH);
			card.setLayout(new BoxLayout(card,BoxLayout.PAGE_AXIS));

			card.add(editDDE=new DDEEditPanel(this,"","",readOnly,helpRunnable));

			addHeading(card,Language.tr("LogSimulation.Restrictions"),true);

			stationIDsDDEEdit=addPlaceholderEdit(card,Language.tr("LogSimulation.StationIDs"),Language.tr("LogSimulation.StationIDs.Info"),Language.tr("LogSimulation.StationIDs.Placeholder"));
			optionDDETypeArrival=addOption(card,Language.tr("LogSimulation.Mode.Arrival"));
			optionDDETypeLeave=addOption(card,Language.tr("LogSimulation.Mode.Leave"));
			optionDDETypeStation=addOption(card,Language.tr("LogSimulation.Mode.InfoStation"));
			optionDDETypeSystem=addOption(card,Language.tr("LogSimulation.Mode.InfoSystem"));

		} else {
			editDDE=null;
			stationIDsDDEEdit=null;
			optionDDETypeArrival=null;
			optionDDETypeLeave=null;
			optionDDETypeStation=null;
			optionDDETypeSystem=null;
		}

		/* Daten in Dialog laden */

		final SetupData setup=SetupData.getSetup();

		logFileEdit.setText(setup.lastLogFile);
		stationIDsFileEdit.setText(setup.logStationIDs);
		optionFileTypeArrival.setSelected(setup.logTypeArrival);
		optionFileTypeLeave.setSelected(setup.logTypeLeave);
		optionFileTypeStation.setSelected(setup.logTypeInfoStation);
		optionFileTypeSystem.setSelected(setup.logTypeInfoSystem);
		optionMultiLine.setSelected(!setup.singleLineEventLog);
		optionGroup.setSelected(setup.logGrouped);
		optionColor.setSelected(setup.logColors);
		optionFormatedTime.setSelected(setup.logFormatedTime);
		optionPrintIDs.setSelected(setup.logPrintIDs);
		optionMaxRecords.setSelected(setup.logMaxRecords>0);
		maxRecords.setText((setup.logMaxRecords>0)?(""+setup.logMaxRecords):"1000");

		if (logMode==null) {
			pageLayout.show(page,"0");
		} else {
			editDDE.setWorkbook(setup.logDDEworkbook);
			editDDE.setTable(setup.logDDEsheet);
			stationIDsDDEEdit.setText(setup.logStationIDs);
			optionDDETypeArrival.setSelected(setup.logTypeArrival);
			optionDDETypeLeave.setSelected(setup.logTypeLeave);
			optionDDETypeStation.setSelected(setup.logTypeInfoStation);
			optionDDETypeSystem.setSelected(setup.logTypeInfoSystem);

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

	/**
	 * Fügt eine Überschrift ein.
	 * @param parent	Übergeordnetes Element
	 * @param label	Anzuzeigende Überschrift
	 * @param marginAbove	Abstand über der Überschrift?
	 */
	private void addHeading(final JPanel parent, final String label, final boolean marginAbove) {
		final JPanel line;

		if (marginAbove) parent.add(Box.createVerticalStrut(15));
		parent.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel("<html><body><b>"+label+"</b></body></html>"));
	}

	/**
	 * Fügt eine Checkbox ein
	 * @param parent	Übergeordnetes Element
	 * @param label	Beschriftung der Checkbox
	 * @return	Neue Checkbox
	 */
	private JCheckBox addOption(final JPanel parent, final String label) {
		return addOption(parent,label,null,null);
	}

	/**
	 * Fügt eine Checkbox ein
	 * @param parent	Übergeordnetes Element
	 * @param label	Beschriftung der Checkbox
	 * @param tooltip	Tooltip für die Checkbox (optional, kann <code>null</code> sein)
	 * @param field	Unter der Checkbox anzuzeigendes Eingabefeld (optional, kann <code>null</code> sein)
	 * @return	Neue Checkbox
	 */
	private JCheckBox addOption(final JPanel parent, final String label, final String tooltip, final JTextField field) {
		final JPanel line;
		final JCheckBox option;

		parent.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(option=new JCheckBox(label));
		if (tooltip!=null && !tooltip.trim().isEmpty()) {
			option.setToolTipText(tooltip);
		}
		if (field!=null) line.add(field);

		return option;
	}

	/**
	 * Fügt eine Dateiauswahlelement ein.
	 * @param parent	Übergeordnetes Element
	 * @param labelText	Beschriftung für das Eingabefeld
	 * @param tooltipSelect	Tooltip für die Auswahlbox hinter der Eingabezeile
	 * @return	Neue Eingabezeile
	 */
	private JTextField addFileEditor(final JPanel parent, final String labelText, final String tooltipSelect) {
		final JPanel line;
		final JLabel label;
		final JTextField field;
		final JButton button;

		parent.add(line=new JPanel(new BorderLayout()));
		line.add(label=new JLabel(labelText+": "),BorderLayout.WEST);
		line.add(field=new JTextField(),BorderLayout.CENTER);
		ModelElementBaseDialog.addUndoFeature(field);
		label.setLabelFor(field);
		line.add(button=new JButton(),BorderLayout.EAST);
		button.setIcon(Images.GENERAL_SELECT_FILE.getIcon());
		button.setPreferredSize(new Dimension(24,24));
		button.setToolTipText(tooltipSelect);
		button.addActionListener(e->selectFile());

		return field;
	}

	/**
	 * Fügt eine Eingabezeile mit Platzhaltertext ein.
	 * @param parent	Übergeordnetes Element
	 * @param labelText	Beschriftung für das Eingabefeld
	 * @param tooltip	Tooltip für die Eingabezeile
	 * @param placeholder	Platzhaltertext, der in der Eingabezeile angezeigt wird so lange diese leer ist
	 * @return	Neue Eingabezeile
	 */
	private JPlaceholderTextField addPlaceholderEdit(final JPanel parent, final String labelText, final String tooltip, final String placeholder) {
		final JPanel line;
		final JLabel label;
		final JPlaceholderTextField field;

		parent.add(line=new JPanel(new BorderLayout()));
		line.add(label=new JLabel(labelText+": "),BorderLayout.WEST);
		label.setBorder(BorderFactory.createEmptyBorder(0,8,0,0));
		line.add(field=new JPlaceholderTextField(),BorderLayout.CENTER);
		if (placeholder!=null && !placeholder.trim().isEmpty()) field.setPlaceholder(placeholder);
		label.setLabelFor(field);
		if (tooltip!=null && !tooltip.trim().isEmpty()) {
			label.setToolTipText(tooltip);
			field.setToolTipText(tooltip);
		}

		return field;
	}

	/**
	 * Wird aufgerufen, wenn der Nutzer die Schaltfläche zur Dateiauswahl anklickt.
	 */
	public void selectFile() {
		JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		fc.setDialogTitle(Language.tr("LogSimulation.LogFile.Select"));
		FileFilter docx=new FileNameExtensionFilter(Language.tr("FileType.Word")+" (*.docx)","docx");
		FileFilter rtf=new FileNameExtensionFilter(Language.tr("FileType.RTF")+" (*.rtf)","rtf");
		FileFilter html=new FileNameExtensionFilter(Language.tr("FileType.HTML")+" (*.html, *.htm)","html","htm");
		FileFilter txt=new FileNameExtensionFilter(Language.tr("FileType.Text")+" (*.txt)","txt");
		FileFilter pdf=new FileNameExtensionFilter(Language.tr("FileType.PDF")+" (*.pdf)","pdf");
		FileFilter csv=new FileNameExtensionFilter(Language.tr("FileType.CSV")+" (*.csv)","cslv");
		FileFilter xlsx=new FileNameExtensionFilter(Language.tr("FileType.Excel")+" (*.xlsx)","xlsx");
		FileFilter xls=new FileNameExtensionFilter(Language.tr("FileType.ExcelOld")+" (*.xls)","xls");
		FileFilter ods=new FileNameExtensionFilter(Language.tr("FileType.FileTypeODS")+" (*.ods)","ods");
		FileFilter odt=new FileNameExtensionFilter(Language.tr("FileType.FileTypeODT")+" (*.odt)","odt");
		fc.addChoosableFileFilter(xlsx);
		fc.addChoosableFileFilter(xls);
		fc.addChoosableFileFilter(csv);
		fc.addChoosableFileFilter(ods);
		fc.addChoosableFileFilter(txt);
		fc.addChoosableFileFilter(docx);
		fc.addChoosableFileFilter(rtf);
		fc.addChoosableFileFilter(html);
		fc.addChoosableFileFilter(pdf);
		fc.addChoosableFileFilter(odt);
		fc.setFileFilter(txt);
		fc.setAcceptAllFileFilterUsed(false);

		if (fc.showSaveDialog(owner)!=JFileChooser.APPROVE_OPTION) return;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();

		if (file.getName().indexOf('.')<0) {
			if (fc.getFileFilter()==xlsx) file=new File(file.getAbsoluteFile()+".xlsx");
			if (fc.getFileFilter()==xls) file=new File(file.getAbsoluteFile()+".xls");
			if (fc.getFileFilter()==csv) file=new File(file.getAbsoluteFile()+".csv");
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

	/**
	 * Prüft, ob die Liste der eingegebenen IDs gültig ist.
	 * @param field	Textfeld das die IDs enthält.
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Liefert <code>true</code>, wenn die Liste nur gültige IDs enthält.
	 * @see #stationIDsFileEdit
	 * @see #stationIDsDDEEdit
	 * @see #checkData()
	 */
	private boolean checkIDs(final JTextField field, final boolean showErrorMessage) {
		if (field.getText().trim().isEmpty()) {
			field.setBackground(NumberTools.getTextFieldDefaultBackground());
			return true;
		}

		final String[] ids=field.getText().trim().split(";");
		for (String id: ids) {
			if (NumberTools.getNotNegativeInteger(id)==null) {
				field.setBackground(Color.RED);
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Dialog.InvalidID.Title"),String.format(Language.tr("Dialog.InvalidID.Info"),id));
				}
				return false;
			}
		}

		field.setBackground(NumberTools.getTextFieldDefaultBackground());
		return true;
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessage) {
		final int mode=getLogMode();

		if (mode==0) {
			boolean ok=true;
			if (showErrorMessage) {
				final File file=new File(logFileEdit.getText());
				if (file.exists()) {
					if (!MsgBox.confirmOverwrite(this,file)) return false;
				}
				File path=file.getParentFile();
				if (path==null || !path.exists()) {
					MsgBox.error(this,Language.tr("Dialog.InvalidFile.Title"),String.format(Language.tr("Dialog.InvalidFile.Info"),file.toString()));
					return false;
				}
			}

			if (!checkIDs(stationIDsFileEdit,showErrorMessage )) {
				ok=false;
				if (showErrorMessage) return false;
			}

			if (!optionFileTypeArrival.isSelected() && !optionFileTypeLeave.isSelected() && !optionFileTypeStation.isSelected() && !optionFileTypeSystem.isSelected()) {
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("LogSimulation.Mode.ErrorTitle"),Language.tr("LogSimulation.Mode.ErrorInfo"));
					return false;
				}
				ok=false;
			}

			if (optionMaxRecords.isSelected()) {
				if (NumberTools.getPositiveLong(maxRecords,true)==null) {
					ok=false;
					if (showErrorMessage) {
						MsgBox.error(this,Language.tr("LogSimulation.LimitRecords.ErrorTitle"),Language.tr("LogSimulation.LimitRecords.ErrorInfo"));
						return false;
					}
				}
			} else {
				maxRecords.setBackground(NumberTools.getTextFieldDefaultBackground());
			}

			return ok;
		}

		if (mode==1) {
			boolean ok=true;
			if (editDDE!=null) {
				if (!editDDE.checkData(showErrorMessage)) {
					ok=false;
					if (showErrorMessage) return false;
				}
			}

			if (!checkIDs(stationIDsFileEdit,showErrorMessage )) {
				ok=false;
				if (showErrorMessage) return false;
			}

			if (!optionDDETypeArrival.isSelected() && !optionDDETypeLeave.isSelected() && !optionDDETypeStation.isSelected() && !optionDDETypeSystem.isSelected()) {
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("LogSimulation.Mode.ErrorTitle"),Language.tr("LogSimulation.Mode.ErrorInfo"));
					return false;
				}
				ok=false;
			}

			return ok;
		}

		return false;
	}

	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	@Override
	protected void storeData() {
		final SetupData setup=SetupData.getSetup();

		if (logMode!=null) {
			switch (logMode.getSelectedIndex()) {
			case 0:
				setup.logMode=SetupData.LogMode.FILE;
				setup.logStationIDs=stationIDsFileEdit.getText();
				setup.logTypeArrival=optionFileTypeArrival.isSelected();
				setup.logTypeLeave=optionFileTypeLeave.isSelected();
				setup.logTypeInfoStation=optionFileTypeStation.isSelected();
				setup.logTypeInfoSystem=optionFileTypeSystem.isSelected();
				setup.logMaxRecords=optionMaxRecords.isSelected()?NumberTools.getPositiveLong(maxRecords,true).intValue():-1;
				break;
			case 1:
				setup.logMode=SetupData.LogMode.DDE;
				setup.logStationIDs=stationIDsDDEEdit.getText();
				setup.logTypeArrival=optionDDETypeArrival.isSelected();
				setup.logTypeLeave=optionDDETypeLeave.isSelected();
				setup.logTypeInfoStation=optionDDETypeStation.isSelected();
				setup.logTypeInfoSystem=optionDDETypeSystem.isSelected();
				break;
			}
		} else {
			setup.logMode=SetupData.LogMode.FILE;
		}

		setup.lastLogFile=logFileEdit.getText();

		setup.singleLineEventLog=!optionMultiLine.isSelected();
		setup.logGrouped=optionGroup.isSelected();
		setup.logColors=optionColor.isSelected();
		setup.logFormatedTime=optionFormatedTime.isSelected();
		setup.logPrintIDs=optionPrintIDs.isSelected();

		if (logMode!=null) {
			setup.logDDEworkbook=editDDE.getWorkbook();
			setup.logDDEsheet=editDDE.getTable();
		}

		setup.saveSetup();
	}

	/**
	 * Liefert den gewählten Logging-Modus.
	 * @return	Logging-Modus (0: Datei, 1: DDE)
	 */
	private int getLogMode() {
		return (logMode==null)?0:logMode.getSelectedIndex();
	}

	/**
	 * Liefert auf Basis der Einstellungen in dem Dialog einen entsprechenden Logger zurück.
	 * @return	Logger, der gemäß den Einstellungen aus dem Dialog Daten aufzeichnet (oder im Fehlerfall <code>null</code>
	 */
	public SimLogging getLogger() {
		final int mode=getLogMode();

		if (mode==0) {
			final File file=new File(logFileEdit.getText());
			if (file.exists()) {
				if (!file.delete()) {
					MsgBox.error(this,Language.tr("LogSimulation.ErrorDelete.Title"),String.format(Language.tr("LogSimulation.ErrorDelete.Info"),file.toString()));
					return null;
				}
			}
			return new MultiTypeTextLogger(
					file,
					optionGroup.isSelected(),
					!optionMultiLine.isSelected(),
					optionColor.isSelected(),
					optionFormatedTime.isSelected(),
					optionPrintIDs.isSelected(),
					new String[]{Language.tr("LogSimulation.Heading")},
					optionMaxRecords.isSelected()?NumberTools.getPositiveLong(maxRecords,true).intValue():-1
					);
		}

		if (mode==1) {
			if (editDDE!=null) return new DDELogger(editDDE.getWorkbook(),editDDE.getTable(),optionPrintIDs.isSelected());
		}

		return null;
	}

	/**
	 * Liefert die gewählten Stations-IDs, die beim Logging berücksichtigt werden sollen.
	 * @return	Gewählte Stations-IDs, die beim Logging berücksichtigt werden sollen, oder <code>null</code>, wenn alle Daten aufgezeichnet werden sollen.
	 */
	public int[] getStationIDs() {
		final int mode=getLogMode();

		if (mode==0) {
			final String idsLine=stationIDsFileEdit.getText().trim();
			if (idsLine.isEmpty()) return null;
			final String[] ids=idsLine.split(";");
			return Stream.of(ids).map(id->NumberTools.getNotNegativeInteger(id)).mapToInt(I->I.intValue()).toArray();
		}

		if (mode==1) {
			final String idsLine=stationIDsDDEEdit.getText().trim();
			if (idsLine.isEmpty()) return null;
			final String[] ids=idsLine.split(";");
			return Stream.of(ids).map(id->NumberTools.getNotNegativeInteger(id)).mapToInt(I->I.intValue()).toArray();
		}

		return null;
	}

	/**
	 * Liefert die gewählten Einstellung, welche Ereignistypen beim Loggen erfasst werden sollen.
	 * @return	Ereignistypen die beim Loggen erfasst werden sollen
	 */
	public Set<Simulator.LogType> getLogType() {
		final Set<Simulator.LogType> set=new HashSet<>();

		final int mode=getLogMode();

		if (mode==0) {
			if (optionFileTypeArrival.isSelected()) set.add(Simulator.LogType.ARRIVAL);
			if (optionFileTypeLeave.isSelected()) set.add(Simulator.LogType.LEAVE);
			if (optionFileTypeStation.isSelected()) set.add(Simulator.LogType.STATIONINFO);
			if (optionFileTypeSystem.isSelected()) set.add(Simulator.LogType.SYSTEMINFO);
		}

		if (mode==1) {
			if (optionDDETypeArrival.isSelected()) set.add(Simulator.LogType.ARRIVAL);
			if (optionDDETypeLeave.isSelected()) set.add(Simulator.LogType.LEAVE);
			if (optionDDETypeStation.isSelected()) set.add(Simulator.LogType.STATIONINFO);
			if (optionDDETypeSystem.isSelected()) set.add(Simulator.LogType.SYSTEMINFO);
		}

		return set;
	}
}

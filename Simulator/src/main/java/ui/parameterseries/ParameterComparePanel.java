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
package ui.parameterseries;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;
import javax.swing.SwingUtilities;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import language.Language;
import mathtools.NumberTools;
import mathtools.Table;
import mathtools.TableChart;
import mathtools.TableChartBase;
import mathtools.TimeTools;
import mathtools.distribution.tools.FileDropper;
import mathtools.distribution.tools.FileDropperData;
import parser.MathCalcError;
import scripting.java.DynamicFactory;
import scripting.java.DynamicRunner;
import scripting.java.DynamicStatus;
import scripting.java.OutputImpl;
import scripting.java.StatisticsImpl;
import scripting.js.JSRunDataFilter;
import simulator.editmodel.EditModel;
import simulator.simparser.ExpressionCalc;
import simulator.statistics.Statistics;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import systemtools.images.SimToolsImages;
import systemtools.statistics.StatisticsBasePanel;
import tools.Notifier;
import tools.SetupData;
import ui.MainFrame;
import ui.ModelChanger;
import ui.ModelViewerFrame;
import ui.help.Help;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.optimizer.OptimizerPanelPrepareDialog;
import ui.parameterseries.ParameterCompareTemplatesDialog.TemplateMode;
import ui.tools.SpecialPanel;
import xml.XMLTools;

/**
 * Ermöglicht die Durchführung von Parameter-Variationsstudien
 * @author Alexander Herzog
 */
public class ParameterComparePanel extends SpecialPanel {
	private static final long serialVersionUID = 1080219911724293243L;

	private final ParameterCompareSetup setup;
	private final ParameterCompareSetup setupOriginal;
	private final EditModel modelFromEditor;
	private Statistics miniStatistics;

	private final Window owner;
	private final JButton newSetupButton;
	private final JButton loadSetupButton;
	private final JButton saveSetupButton;
	private final JButton templatesButton;
	private final JButton showBaseModel;
	private final JButton setupInput;
	private final JButton setupOutput;
	private final JButton processResults;
	private final JButton startButton;
	private final JButton helpButton;

	private final ParameterCompareTable table;
	private final JTextArea logOutput;

	private ParameterCompareRunner runner;
	private EditModel loadModelIntoEditor=null;
	private Statistics loadStatisticsIntoEditor=null;
	private List<Statistics> compareModels=null;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param modelFromEditor	Editor-Modell auf dessen Basis die Parameterreihe erstellt werden soll
	 * @param miniStatistics	Statistikdaten bezogen auf einen kurzen Lauf über das angegebene Editor-Modell (zur Auswahl von XML-Elementen als Zielwerte)
	 * @param doneNotify	Wird aufgerufen, wenn sich das Panel schließen möchte
	 * @param template	Wird ein Wert ungleich <code>null</code> übergeben, so wird direkt auf Basis des Templates eine Parameterreihen-Konfiguration erstellt
	 */
	public ParameterComparePanel(final Window owner, final EditModel modelFromEditor, final Statistics miniStatistics, final Runnable doneNotify, final ParameterCompareTemplatesDialog.TemplateRecord template) {
		super(doneNotify);
		this.owner=owner;
		this.modelFromEditor=modelFromEditor;
		this.miniStatistics=miniStatistics;
		setup=new ParameterCompareSetup(modelFromEditor);
		setupOriginal=new ParameterCompareSetup(modelFromEditor);

		newSetupButton=addUserButton(Language.tr("ParameterCompare.Toolbar.New"),Language.tr("ParameterCompare.Toolbar.New.Hint"),Images.PARAMETERSERIES_SETUP_NEW.getURL());
		loadSetupButton=addUserButton(Language.tr("ParameterCompare.Toolbar.Load"),Language.tr("ParameterCompare.Toolbar.Load.Hint"),Images.PARAMETERSERIES_SETUP_LOAD.getURL());
		saveSetupButton=addUserButton(Language.tr("ParameterCompare.Toolbar.Save"),Language.tr("ParameterCompare.Toolbar.Save.Hint"),Images.PARAMETERSERIES_SETUP_SAVE.getURL());
		addSeparator();
		templatesButton=addUserButton(Language.tr("ParameterCompare.Toolbar.Templates"),Language.tr("ParameterCompare.Toolbar.Templates.Hint"),Images.PARAMETERSERIES_SETUP_TEMPLATES.getURL());
		addSeparator();
		showBaseModel=addUserButton(Language.tr("ParameterCompare.Toolbar.ShowBaseModel"),Language.tr("ParameterCompare.Toolbar.ShowBaseModel.Hint"),Images.PARAMETERSERIES_SETUP_SHOW_BASE_MODEL.getURL());
		setupInput=addUserButton(Language.tr("ParameterCompare.Toolbar.SetupInput"),Language.tr("ParameterCompare.Toolbar.SetupInput.Hint"),Images.PARAMETERSERIES_SETUP_INPUT.getURL());
		setupOutput=addUserButton(Language.tr("ParameterCompare.Toolbar.SetupOutput"),Language.tr("ParameterCompare.Toolbar.SetupOutput.Hint"),Images.PARAMETERSERIES_SETUP_OUTPUT.getURL());
		startButton=addUserButton(Language.tr("ParameterCompare.Toolbar.Start"),Language.tr("ParameterCompare.Toolbar.Start.Hint"),Images.PARAMETERSERIES_RUN.getURL());
		processResults=addUserButton(Language.tr("ParameterCompare.Toolbar.ProcessResults"),Language.tr("ParameterCompare.Toolbar.ProcessResults.Hint"),Images.PARAMETERSERIES_PROCESS_RESULTS.getURL());
		addSeparator();
		addCloseButton();
		addSeparator();
		helpButton=addUserButton(Language.tr("Main.Toolbar.Help"),Language.tr("Main.Toolbar.Help.Hint"),Images.HELP.getURL());

		JPanel content=new JPanel(new BorderLayout());
		add(content,BorderLayout.CENTER);
		content=InfoPanel.addTopPanelAndGetNewContent(content,InfoPanel.globalParameterCompare);

		final JSplitPane split=new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		content.add(split,BorderLayout.CENTER);
		split.setTopComponent(table=new ParameterCompareTable(
				setup,
				()->commandHelp(),
				m->commandLoadToEditor(m),
				()->commandPopupCompareStatistics(),
				()->commandSetupInput(),
				()->commandSetupOutput()));
		table.setDisplayDigits(SetupData.getSetup().parameterSeriesTableDigits);
		split.setBottomComponent(new JScrollPane(logOutput=new JTextArea()));
		logOutput.setEditable(false);
		split.setResizeWeight(0.75);

		/* F1-Hotkey */
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F1"),"actionHelp");
		getActionMap().put("actionHelp",new AbstractAction("actionHelp") {
			private static final long serialVersionUID = 1738622101739292954L;
			@Override public void actionPerformed(ActionEvent event) {commandHelp();}
		});

		/* Start der Verarbeitung */
		loadSetupToGUI(setup);

		/* Optional direkt Template verarbeiten */
		if (template!=null) {
			SwingUtilities.invokeLater(()->{
				processTemplateMenuClick(template);
			});
		}

		/* Drag&Drop auch für Ausgabe-Bereich */
		new FileDropper(logOutput,e->{
			final FileDropperData dropper=(FileDropperData)e.getSource();
			dropper.getDropComponent();
			dragDropLoadFile(logOutput,dropper.getFile());
		});
	}

	/**
	 * Konfiguriert die Parameterreihe als Varianzanalyse
	 * (d.h. zur mehrfachen Wiederholung desselben Modells ohne sich verändernde Eingabeparameter)
	 * @param repeatCount	Anzahl an Wiederholungen
	 */
	public void setupVarianceAnalysis(final int repeatCount) {
		setup.setupVarianceAnalysis(repeatCount);

		/* Tabelle aktualisieren */
		table.updateTable();
	}

	private void prepareSetup() {
		if (setup.getModels().size()==0) {
			final ParameterCompareSetupModel simModel=new ParameterCompareSetupModel();
			simModel.setName(Language.tr("ParameterCompare.Settings.BaseModel"));
			setup.getModels().add(simModel);
		}
	}

	private void loadSetupToGUI(final ParameterCompareSetup setup) {
		this.setup.transferFrom(setup);
		prepareSetup();
		setupOriginal.copyFrom(this.setup);
		table.updateTable();
	}

	private void commandSetupInput() {
		final ParameterCompareSetupValueInputListDialog dialog=new ParameterCompareSetupValueInputListDialog(this,setup.getEditModel(),setup.getInput(),()->commandHelp());
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			setup.clearAllOutputs();
			setup.updateInputValuesInModels();
			table.updateTable();
		}
	}

	private String getScript(final String fileName) {
		final File file=new File(fileName);
		if (!file.isFile()) return null;
		try {
			final String[] lines=Files.lines(file.toPath()).toArray(String[]::new);
			return String.join("\n",lines);
		} catch (IOException e) {
			return null;
		}
	}

	private Double recalcResult(final Statistics statistics, final ParameterCompareSetupValueOutput output) {
		String s;
		String script;

		switch (output.getMode()) {
		case MODE_XML:
			s=ModelChanger.getStatisticValue(statistics,output.getTag());
			if (s==null) return null;
			return NumberTools.getDouble(s);
		case MODE_SCRIPT_JS:
			script=getScript(output.getTag());
			if (script==null) return null;
			final JSRunDataFilter filter=new JSRunDataFilter(statistics.saveToXMLDocument());
			filter.run(script);
			if (!filter.getLastSuccess()) return null;
			s=filter.getResults();
			return NumberTools.getDouble(s);
		case MODE_SCRIPT_JAVA:
			script=getScript(output.getTag());
			if (script==null) return null;
			final DynamicRunner runner=DynamicFactory.getFactory().load(script);
			if (runner.getStatus()!=DynamicStatus.OK) return null;

			final StringBuilder results=new StringBuilder();
			runner.parameter.output=new OutputImpl(line->results.append(line),false);
			runner.parameter.statistics=new StatisticsImpl(line->results.append(line),statistics.saveToXMLDocument(),false);
			runner.run();
			if (runner.getStatus()!=DynamicStatus.OK) return null;
			return NumberTools.getDouble(results.toString().trim());
		case MODE_COMMAND:
			ExpressionCalc calc=new ExpressionCalc(null);
			if (calc.parse(output.getTag())>=0) return null;
			try {
				return calc.calc(statistics);
			} catch (MathCalcError e) {
				return null;
			}
		default:
			return null;
		}
	}

	private void recalcResults(final List<ParameterCompareSetupValueOutput> oldOutput) {
		final boolean[] needUpdate=new boolean[setup.getOutput().size()];
		boolean needAnyUpdate=false;
		for (int i=0;i<needUpdate.length;i++) {
			needUpdate[i]=(oldOutput.size()<=i || !oldOutput.get(i).equals(setup.getOutput().get(i)));
			if (needUpdate[i]) needAnyUpdate=true;
		}
		if (!needAnyUpdate) return;

		for (ParameterCompareSetupModel model: setup.getModels()) if (model.isStatisticsAvailable()) {
			final Statistics statistics=model.getStatistics();
			for (int i=0;i<needUpdate.length;i++) if (needUpdate[i]) {
				final ParameterCompareSetupValueOutput output=setup.getOutput().get(i);
				final String key=output.getName();
				model.getOutput().put(key,recalcResult(statistics,output));
			}
		}
	}

	private void commandSetupOutput() {
		final List<ParameterCompareSetupValueOutput> oldOutput=new ArrayList<>();
		for (ParameterCompareSetupValueOutput output: setup.getOutput()) oldOutput.add(output.clone());

		if (miniStatistics==null) {
			MsgBox.error(this,Language.tr("ParameterCompare.ErrorNoMiniStatistics.Title"),Language.tr("ParameterCompare.ErrorNoMiniStatistics.Info"));
			return;
		}

		final ParameterCompareSetupValueOutputListDialog dialog=new ParameterCompareSetupValueOutputListDialog(this,setup.getEditModel(),miniStatistics,setup.getOutput(),()->commandHelp());
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			recalcResults(oldOutput);
			table.updateTable();
		}
	}

	private void commandStart() {
		if (runner!=null) {
			runner.cancel();
			return;
		}

		boolean allDone=true;
		for (ParameterCompareSetupModel model: setup.getModels()) if (!model.isStatisticsAvailable()) {allDone=false; break;}
		if (allDone) {
			if (!MsgBox.confirm(this,Language.tr("ParameterCompare.ReRunConfirm.Title"),Language.tr("ParameterCompare.ReRunConfirm.Info"),Language.tr("ParameterCompare.ReRunConfirm.InfoYes"),Language.tr("ParameterCompare.ReRunConfirm.InfoNo"))) return;
			setup.clearAllOutputs();
		}

		logOutput.setText("");

		final long simulationStartTime=System.currentTimeMillis();
		runner=new ParameterCompareRunner((row)->table.updateTableContentOnly(row),b->{setGUIRunMode(false); if (b) Notifier.run(Notifier.Message.PARAMETER_SERIES_DONE,simulationStartTime);},s->logOutput(s));
		String error=runner.check(setup);
		if (error!=null) {
			runner=null;
			MsgBox.error(ParameterComparePanel.this,Language.tr("ParameterCompare.Error.CouldNotStart.Title"),Language.tr("ParameterCompare.Error.CouldNotStart")+":\n"+error);
			return;
		}
		setGUIRunMode(true);
		runner.start();
	}

	private void logOutput(final String line) {
		final String s=logOutput.getText();
		if (s.isEmpty()) logOutput.setText(line); else logOutput.setText(s+"\n"+line);
		logOutput.setCaretPosition(logOutput.getDocument().getLength());
	}

	private void setGUIRunMode(final boolean run) {
		if (run) {
			startButton.setText(Language.tr("ParameterCompare.Toolbar.Stop"));
			startButton.setToolTipText(Language.tr("ParameterCompare.Toolbar.Stop.Hint"));
			startButton.setIcon(Images.GENERAL_CANCEL.getIcon());
		} else {
			startButton.setText(Language.tr("ParameterCompare.Toolbar.Start"));
			startButton.setToolTipText(Language.tr("ParameterCompare.Toolbar.Start.Hint"));
			startButton.setIcon(Images.PARAMETERSERIES_RUN.getIcon());
		}

		newSetupButton.setEnabled(!run);
		loadSetupButton.setEnabled(!run);
		saveSetupButton.setEnabled(!run);
		templatesButton.setEnabled(!run);
		setupInput.setEnabled(!run);
		setupOutput.setEnabled(!run);
		processResults.setEnabled(!run);

		table.setTableEnabled(!run);

		if (!run) runner=null;
	}

	private void commandHelp() {
		Help.topicModal(ParameterComparePanel.this,"ParameterSeries");
	}

	private void commandPopupClearStatistics() {
		boolean hasResults=false;
		for (ParameterCompareSetupModel model: setup.getModels()) if (model.isStatisticsAvailable()) {hasResults=true; break;}
		if (!hasResults) return;

		if (!MsgBox.confirm(this,Language.tr("ParameterCompare.Toolbar.ProcessResults.ClearStatistics"),Language.tr("ParameterCompare.Toolbar.ProcessResults.ClearStatistics.Info"),Language.tr("ParameterCompare.Toolbar.ProcessResults.ClearStatistics.InfoYes"),Language.tr("ParameterCompare.Toolbar.ProcessResults.ClearStatistics.InfoNo"))) return;

		setup.clearAllOutputs();
		table.updateTable();
	}

	private File getNextStatisticsFile(final File folder) {
		if (folder==null) return null;

		int i=0;
		while (true) {
			i++;
			if (i>9999) return null;
			final File file=new File(folder,String.format(Language.tr("Batch.Output.Folder.FileNameFormat"),i));
			if (!file.exists()) return file;
		}
	}

	private void commandPopupSaveStatistics() {
		final ParameterCompareStatisticSelectDialog dialog=new ParameterCompareStatisticSelectDialog(this,setup,()->commandHelp(),ParameterCompareStatisticSelectDialog.Mode.MODE_STORE);
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			final File folder=dialog.getFolder();
			for (ParameterCompareSetupModel model: dialog.getSelected()) {
				final Statistics statistics=model.getStatistics();
				final File file=getNextStatisticsFile(folder);
				if (statistics.saveToFile(file)) {
					logOutput(String.format(Language.tr("ParameterCompare.Toolbar.ProcessResults.SaveStatistics.Log.Success"),model.getName(),file.getName()));
				} else {
					logOutput(String.format(Language.tr("ParameterCompare.Toolbar.ProcessResults.SaveStatistics.Log.Failed"),model.getName(),file.getName()));
				}
			}
		}
	}

	private void commandPopupCompareStatistics() {
		final ParameterCompareStatisticSelectDialog dialog=new ParameterCompareStatisticSelectDialog(this,setup,()->commandHelp(),ParameterCompareStatisticSelectDialog.Mode.MODE_COMPARE);
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			if (!allowDispose()) return;
			compareModels=new ArrayList<>();
			for (ParameterCompareSetupModel model: dialog.getSelected()) compareModels.add(model.getStatistics());
			close();
		}
	}

	private void commandPopupRunScript() {
		new ParameterCompareScriptDialog(this,setup,()->commandHelp());
	}

	private void commandPopupTableCopy() {
		final Table table=setup.getTableData(true,false);
		final String text=table.toString();

		final StringSelection stringSelection=new StringSelection(text);
		final Clipboard clipboard=Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection,null);
	}

	private void commandPopupTableSave() {
		final Table table=setup.getTableData(true,false);
		final File file=Table.showSaveDialog(this,Language.tr("ParameterCompare.Toolbar.ProcessResults.ResultsTable.Save"),null);
		if (file==null) return;

		if (file.exists()) {
			if (!MsgBox.confirmOverwrite(this,file)) return;
		}

		if (!table.save(file)) {
			MsgBox.error(this,Language.tr("ParameterCompare.Toolbar.ProcessResults.ResultsTable.Save.ErrorTitle"),String.format(Language.tr("ParameterCompare.Toolbar.ProcessResults.ResultsTable.Save.ErrorInfo"),file.toString()));
		}
	}

	private void commandPopupTableExcel() {
		final Table table=setup.getTableData(true,false);
		try {
			final File file=File.createTempFile(StatisticsBasePanel.viewersToolbarExcelPrefix+"_",".xlsx");
			if (table.save(file)) {
				file.deleteOnExit();
				Desktop.getDesktop().open(file);
			}
		} catch (IOException e1) {
			MsgBox.error(this,StatisticsBasePanel.viewersToolbarExcelSaveErrorTitle,StatisticsBasePanel.viewersToolbarExcelSaveErrorInfo);
		}
	}

	private void commandPopupTableODS() {
		final Table table=setup.getTableData(true,false);
		try {
			final File file=File.createTempFile(StatisticsBasePanel.viewersToolbarExcelPrefix+"_",".ods");
			if (table.save(file)) {
				file.deleteOnExit();
				Desktop.getDesktop().open(file);
			}
		} catch (IOException e1) {
			MsgBox.error(this,StatisticsBasePanel.viewersToolbarExcelSaveErrorTitle,StatisticsBasePanel.viewersToolbarExcelSaveErrorInfo);
		}
	}

	private boolean buildCharts(final Table table, final File file) {
		final int rowCount=setup.getModels().size();
		final int inputCount=setup.getInput().size();
		final int outputCount=setup.getOutput().size();

		try (Workbook wb=new XSSFWorkbook()) {
			final TableChart tableChart=new TableChart(table);

			final String sheetName=Table.TableFileTableName;
			final Sheet sheet=wb.createSheet(sheetName);
			table.saveToSheet(wb,sheet);

			final TableChartBase.Range categories=new TableChartBase.Range(1,2,1,2+rowCount-1);
			final String xLabel=table.getValue(1,1);
			final List<TableChartBase.Range> series=new ArrayList<>(outputCount);
			final List<Object> seriesNames=new ArrayList<>(outputCount);
			for (int i=0;i<outputCount;i++) {
				series.add(new TableChartBase.Range(1+inputCount+i,2,1+inputCount+i,2+rowCount-1));
				seriesNames.add(new TableChartBase.Cell(1+inputCount+i,1));
			}

			/* Gesamtdiagramm */

			tableChart.setupAxis(xLabel,null);
			tableChart.setupChart(TableChart.ChartMode.LINE,series,seriesNames,categories);
			tableChart.build(Language.tr("Statistics.TotalBig"),0,sheet,sheetName);

			/* Einzeldiagramme */

			for (int i=0;i<outputCount;i++) {
				tableChart.setupAxis(xLabel,null);
				tableChart.setupChart(TableChart.ChartMode.LINE,new TableChartBase.Range[]{series.get(i)},new Object[]{seriesNames.get(i)},categories);
				tableChart.build(table.getValue(1,1+inputCount+i),i+1,sheet,sheetName);
			}

			try (FileOutputStream fo=new FileOutputStream(file)) {wb.write(fo);}
		} catch (IOException e) {return false;}
		return true;
	}

	private void commandPopupChartsSave() {
		final Table table=setup.getTableData(true,true);
		final File file=Table.showSaveDialogXLSXonly(this,Language.tr("ParameterCompare.Toolbar.ProcessResults.ResultsTable.Save"),null);
		if (file==null) return;

		if (file.exists()) {
			if (!MsgBox.confirmOverwrite(this,file)) return;
		}

		if (!buildCharts(table,file)) {
			MsgBox.error(this,Language.tr("ParameterCompare.Toolbar.ProcessResults.ResultsTable.Save.ErrorTitle"),String.format(Language.tr("ParameterCompare.Toolbar.ProcessResults.ResultsTable.Save.ErrorInfo"),file.toString()));
		}
	}

	private void commandLoadToEditor(final Statistics statistics) {
		if (!allowDispose()) return;
		loadStatisticsIntoEditor=statistics;
		close();
	}

	private void commandLoadToEditor(final EditModel editModel, final boolean newWindow) {
		if (newWindow) {
			final MainFrame frame=new MainFrame(null,editModel);
			SwingUtilities.invokeLater(()->frame.toFront());
		} else {
			if (!allowDispose()) return;
			loadModelIntoEditor=editModel;
			close();
		}
	}

	private void commandLoadFromEditor() {
		if (modelFromEditor==null) return;

		final Statistics miniStatistics=ParameterComparePanel.generateMiniStatistics(this,modelFromEditor,null);
		if (miniStatistics==null) return;
		this.miniStatistics=miniStatistics;

		setup.setEditModel(modelFromEditor);
		table.updateTable();
	}

	private void addMenuItem(final MenuElement menu, final String title, final Icon icon, final ActionListener listener) {
		final JMenuItem item=new JMenuItem(title);
		if (icon!=null) item.setIcon(icon);
		if (listener!=null) item.addActionListener(listener);

		if (menu instanceof JPopupMenu) ((JPopupMenu)menu).add(item);
		if (menu instanceof JMenu) ((JMenu)menu).add(item);
	}

	private void commandProcessResults() {
		final JPopupMenu menu=new JPopupMenu();
		JMenu sub;
		JRadioButtonMenuItem radioItem;

		boolean hasResults=false;
		for (ParameterCompareSetupModel model: setup.getModels()) if (model.isStatisticsAvailable()) {hasResults=true; break;}
		if (hasResults) {
			addMenuItem(menu,Language.tr("ParameterCompare.Toolbar.ProcessResults.ClearStatistics"),Images.PARAMETERSERIES_PROCESS_RESULTS_CLEAR.getIcon(),e->commandPopupClearStatistics());
			addMenuItem(menu,Language.tr("ParameterCompare.Toolbar.ProcessResults.SaveStatistics"),Images.GENERAL_SAVE.getIcon(),e->commandPopupSaveStatistics());
			addMenuItem(menu,Language.tr("ParameterCompare.Toolbar.ProcessResults.CompareStatistics"),Images.PARAMETERSERIES_PROCESS_RESULTS_COMPARE.getIcon(),e->commandPopupCompareStatistics());
			addMenuItem(menu,Language.tr("ParameterCompare.Toolbar.ProcessResults.RunScript"),Images.PARAMETERSERIES_PROCESS_RESULTS_SCRIPT.getIcon(),e->commandPopupRunScript());
			menu.addSeparator();
		}

		menu.add(sub=new JMenu(Language.tr("ParameterCompare.Toolbar.ProcessResults.ResultsTable.Setup")));
		sub.setIcon(Images.GENERAL_SETUP.getIcon());
		final ButtonGroup buttonGroup=new ButtonGroup();
		final int digits=SetupData.getSetup().parameterSeriesTableDigits;
		sub.add(radioItem=new JRadioButtonMenuItem(Language.tr("ParameterCompare.Toolbar.ProcessResults.ResultsTable.Setup.Digit1"),digits==1));
		radioItem.addActionListener(e->setupTableDigits(1));
		buttonGroup.add(radioItem);
		sub.add(radioItem=new JRadioButtonMenuItem(Language.tr("ParameterCompare.Toolbar.ProcessResults.ResultsTable.Setup.Digit3"),digits==3));
		radioItem.addActionListener(e->setupTableDigits(3));
		buttonGroup.add(radioItem);
		sub.add(radioItem=new JRadioButtonMenuItem(Language.tr("ParameterCompare.Toolbar.ProcessResults.ResultsTable.Setup.DigitFull"),digits!=1 && digits!=3));
		radioItem.addActionListener(e->setupTableDigits(9));
		buttonGroup.add(radioItem);

		addMenuItem(menu,Language.tr("ParameterCompare.Toolbar.ProcessResults.ResultsTable.Copy"),Images.EDIT_COPY.getIcon(),e->commandPopupTableCopy());
		addMenuItem(menu,Language.tr("ParameterCompare.Toolbar.ProcessResults.ResultsTable.Save"),Images.GENERAL_SAVE.getIcon(),e->commandPopupTableSave());
		if (StatisticsBasePanel.viewerPrograms.contains(StatisticsBasePanel.ViewerPrograms.EXCEL)) {
			addMenuItem(menu,Language.tr("ParameterCompare.Toolbar.ProcessResults.ResultsTable.Excel"),SimToolsImages.SAVE_TABLE_EXCEL.getIcon(),e->commandPopupTableExcel());
		}
		if (StatisticsBasePanel.viewerPrograms.contains(StatisticsBasePanel.ViewerPrograms.ODS)) {
			addMenuItem(menu,Language.tr("ParameterCompare.Toolbar.ProcessResults.ResultsTable.ODS"),SimToolsImages.SAVE_TABLE.getIcon(),e->commandPopupTableODS());
		}

		if (hasResults && setup.getOutput().size()>0) {
			menu.add(sub=new JMenu(Language.tr("ParameterCompare.Toolbar.ProcessResults.ResultsChart")));
			sub.setIcon(Images.PARAMETERSERIES_PROCESS_RESULTS_CHARTS.getIcon());
			for (final ParameterCompareSetupValueOutput output: setup.getOutput()) {
				addMenuItem(sub,output.getName(),Images.PARAMETERSERIES_PROCESS_RESULTS_CHARTS.getIcon(),e->commandShowResultsChart(output));
			}
			addMenuItem(menu,Language.tr("ParameterCompare.Toolbar.ProcessResults.ResultsChartSave"),null,e->commandPopupChartsSave());
		}

		menu.show(processResults,0,processResults.getHeight());
	}

	private void setupTableDigits(final int digits) {
		final SetupData setup=SetupData.getSetup();
		setup.parameterSeriesTableDigits=digits;
		setup.saveSetup();
		table.setDisplayDigits(digits);
	}

	private Map<String,double[]> buildChartData() {
		final Map<String,double[]> results=new HashMap<>();
		final Table table=setup.getTableData(false,false).transpose(true);

		for (int index=0;index<setup.getOutput().size();index++) {
			final List<String> col=table.getLine(1+setup.getInput().size()+index);
			final String heading=col.remove(0);
			final boolean isTime=setup.getOutput().get(index).getIsTime();

			final double[] data;
			if (isTime) {
				data=col.stream().mapToDouble(cell->{final Double D=TimeTools.getExactTime(cell); return (D==null)?0.0:D.doubleValue();}).toArray();
			} else {
				data=col.stream().mapToDouble(cell->{final Double D=NumberTools.getDouble(cell); return (D==null)?0.0:D.doubleValue();}).toArray();
			}

			results.put(heading,data);
		}

		return results;
	}

	private void commandShowResultsChart(final ParameterCompareSetupValueOutput output) {
		final int index=setup.getOutput().indexOf(output);
		final List<String> col=setup.getTableData(false,false).transpose(true).getLine(1+setup.getInput().size()+index);
		final String heading=col.get(0);

		new ParameterCompareChartDialog(this,heading,buildChartData());
	}

	/**
	 * Versucht eine Datei, die per Drag&amp;Drop auf das Programmfenster gezogen wurde, zu laden
	 * @param owner	Übergeordnetes Element (zum Ausrichten von Fehlermeldungen)
	 * @param file	Zu ladende Datei
	 * @return	Gibt <code>true</code> zurück, wenn die Datei geladen werden konnte
	 */
	public final boolean dragDropLoadFile(final Component owner, final File file) {
		if (!allowDispose()) return false;
		return loadSetup(owner,file,true);
	}

	private boolean allowDispose() {
		if (setupOriginal.equalsParameterCompareSetup(setup)) return true;
		switch (MsgBox.confirmSave(this,Language.tr("ParameterCompare.Settings.Discard.Title"),Language.tr("ParameterCompare.Settings.Discard"))) {
		case JOptionPane.YES_OPTION:
			return saveSetup(setup);
		case JOptionPane.NO_OPTION:
			return true;
		case JOptionPane.CANCEL_OPTION:
			return false;
		default:
			return false;
		}
	}

	/**
	 * Versucht eine Parameterreihenkonfiguration zu laden
	 * @param owner	Übergeordnetes Element (zum Ausrichten von Fehlermeldungen)
	 * @param file	Zu ladende Datei
	 * @param forceLoad	Wird hier <code>true</code> übergeben, so wird das Modell auch dann geladen, wenn es kein simulierbares Basismodell enthält.
	 * @return	Gibt an, ob das Laden erfolgreich war.
	 */
	public boolean loadSetup(final Component owner, final File file, final boolean forceLoad) {
		final ParameterCompareSetup setup=new ParameterCompareSetup(null);
		final String error=setup.loadFromFile(file);
		if (error!=null) {
			MsgBox.error(this,Language.tr("ParameterCompare.Settings.Load.Error"),error);
			return false;
		}

		final Statistics miniStatistics;
		if (setup.getEditModel().surface.getElementCount()==0) {
			miniStatistics=ParameterComparePanel.generateMiniStatistics(this,modelFromEditor,null);
			if (miniStatistics!=null) setup.setEditModel(modelFromEditor.clone());
		} else {
			miniStatistics=generateMiniStatistics(owner,setup.getEditModel(),null);
		}

		if (miniStatistics==null) {
			if (!forceLoad) return false;
		}
		this.miniStatistics=miniStatistics;

		loadSetupToGUI(setup);
		return true;
	}

	private boolean loadSetup() {
		final File file=XMLTools.showLoadDialog(getParent(),Language.tr("ParameterCompare.Settings.Load"));
		if (file==null) return false;
		return loadSetup(this,file,true);
	}

	private boolean saveSetup(final ParameterCompareSetup setup) {
		final File file=XMLTools.showSaveDialog(getParent(),Language.tr("ParameterCompare.Settings.Save"),SetupData.getSetup().defaultSaveFormatParameterSeries);
		if (file==null) return false;

		if (file.exists()) {
			if (!MsgBox.confirmOverwrite(owner,file)) return false;
		}

		if (!setup.saveToFile(file)) {
			MsgBox.error(this,Language.tr("ParameterCompare.Settings.Save.Error.Title"),String.format(Language.tr("ParameterCompare.Settings.Save.Error.Info"),file.toString()));
			return false;
		}

		this.setup.transferFrom(setup);
		setupOriginal.copyFrom(setup);
		table.updateTable();
		return true;
	}

	@Override
	public void requestClose() {
		if (runner!=null) {
			runner.cancel();
			return;
		}

		if (!allowDispose()) return;
		close();
	}

	private void showTemplatesMenu(final Component parent) {
		final JPopupMenu popup=new JPopupMenu();

		final Map<TemplateMode,List<ParameterCompareTemplatesDialog.TemplateRecord>> map=ParameterCompareTemplatesDialog.getTemplates(setup.getEditModel());

		for (TemplateMode mode: ParameterCompareTemplatesDialog.TemplateMode.values()) {
			final List<ParameterCompareTemplatesDialog.TemplateRecord> list=map.get(mode);
			if (list!=null && list.size()>0) {
				final JMenu sub=new JMenu(ParameterCompareTemplatesDialog.getTemplateModeName(mode));
				final ImageIcon icon=ParameterCompareTemplatesDialog.getTemplateModeIcon(mode);
				if (icon!=null) sub.setIcon(icon);
				popup.add(sub);
				for (ParameterCompareTemplatesDialog.TemplateRecord record: list) {
					final JMenuItem item=new JMenuItem(record.menuName);
					item.addActionListener(e->processTemplateMenuClick(record));
					sub.add(item);
				}
			}
		}

		popup.show(parent,0,parent.getHeight());
	}

	private void processTemplateMenuClick(final ParameterCompareTemplatesDialog.TemplateRecord record) {
		final ParameterCompareTemplatesDialog dialog=new ParameterCompareTemplatesDialog(this,setup.getEditModel(),record,()->commandHelp());
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			if (allowDispose()) loadSetupToGUI(dialog.buildParameterCompareSetup());
		}
	}

	private Window getOwnerWindow() {
		Container c=getParent();
		while (c!=null) {
			if (c instanceof Window) return (Window)c;
			c=c.getParent();
		}
		return null;
	}

	@Override
	protected void userButtonClick(int index, JButton button) {
		if (button==newSetupButton) {if (allowDispose()) loadSetupToGUI(new ParameterCompareSetup(modelFromEditor)); return;}
		if (button==loadSetupButton) {if (allowDispose()) loadSetup(); return;}
		if (button==saveSetupButton) {saveSetup(setup); return;}
		if (button==templatesButton) {showTemplatesMenu(templatesButton); return;}
		if (button==processResults) {commandProcessResults(); return;}
		if (button==showBaseModel) {
			final JPopupMenu popup=new JPopupMenu();
			JMenuItem item;

			popup.add(item=new JMenuItem(Language.tr("ParameterCompare.Toolbar.ShowBaseModel.Show")));
			item.setToolTipText(Language.tr("ParameterCompare.Toolbar.ShowBaseModel.Show.Tooltip"));
			item.setIcon(Images.PARAMETERSERIES_SETUP_SHOW_BASE_MODEL.getIcon());
			item.addActionListener(e->{
				final ModelViewerFrame viewer=new ModelViewerFrame(getOwnerWindow(),setup.getEditModel(),null,()->commandLoadToEditor(setup.getEditModel(),false));
				viewer.setVisible(true);
			});

			popup.addSeparator();

			popup.add(item=new JMenuItem(Language.tr("ParameterCompare.Toolbar.ShowBaseModel.LoadToEditor")));
			item.setToolTipText(Language.tr("ParameterCompare.Toolbar.ShowBaseModel.LoadToEditor.Tooltip"));
			item.setIcon(Images.PARAMETERSERIES_SETUP_SHOW_BASE_MODEL_LOAD_TO_EDITOR.getIcon());
			item.addActionListener(e->commandLoadToEditor(setup.getEditModel(),false));

			popup.add(item=new JMenuItem(Language.tr("ParameterCompare.Toolbar.ShowBaseModel.LoadToEditor.NewWindow")));
			item.setToolTipText(Language.tr("ParameterCompare.Toolbar.ShowBaseModel.LoadToEditor.NewWindow.Tooltip"));
			item.setIcon(Images.GENERAL_APPLICATION.getIcon());
			item.addActionListener(e->commandLoadToEditor(setup.getEditModel(),true));

			if (modelFromEditor!=null) {
				popup.add(item=new JMenuItem(Language.tr("ParameterCompare.Toolbar.ShowBaseModel.LoadFromEditor")));
				item.setToolTipText(Language.tr("ParameterCompare.Toolbar.ShowBaseModel.LoadFromEditorr.Tooltip"));
				if (setup.getEditModel().equalsEditModel(modelFromEditor)) {
					item.setToolTipText(Language.tr("ParameterCompare.Toolbar.ShowBaseModel.LoadFromEditorr.TooltipEqual"));
					item.setEnabled(false);
				}
				item.addActionListener(e->commandLoadFromEditor());
			}

			popup.show(showBaseModel,0,showBaseModel.getHeight());

			return;
		}
		if (button==setupInput) {commandSetupInput(); return;}
		if (button==setupOutput) {commandSetupOutput(); return;}
		if (button==startButton) {commandStart(); return;}
		if (button==helpButton) {commandHelp(); return;}
	}

	/**
	 * Gibt an, ob beim Schließen der Vergleichsansicht eine Modell/Statistikdaten-Kombination in den Editor geladen werden soll.
	 * @return	Zu ladende Daten oder <code>null</code> wenn nichts geladen werden soll.
	 */
	public Statistics getStatisticsForEditor() {
		return loadStatisticsIntoEditor;
	}

	/**
	 * Gibt an, ob beim Schließen der Vergleichsansicht ein Modell (ohne Statistik) in den Editor geladen werden soll.
	 * @return	Zu ladende Daten oder <code>null</code> wenn nichts geladen werden soll.
	 */
	public EditModel getModelForEditor() {
		return loadModelIntoEditor;
	}

	/**
	 * Gibt an, ob beim Schließen der Vergleichsansicht die Funktion zum Vergleich der Ergebnisse
	 * bestimmter Modell geöffnet werden soll.
	 * @return	Statistikdaten zum Vergleichen oder <code>null</code> wenn nichts verglichen werden soll.
	 */
	public List<Statistics> getCompareModels() {
		return compareModels;
	}

	private static Object generateMiniStatisticsIntern(final Component owner, final EditModel editModel, final Statistics alreadyAvailableStatistics) {
		final OptimizerPanelPrepareDialog dialog=new OptimizerPanelPrepareDialog(owner,editModel,alreadyAvailableStatistics,OptimizerPanelPrepareDialog.Mode.MODE_PARAMETER_COMPARE);
		final Statistics miniStatistics=dialog.getMiniStatistics();
		if (miniStatistics==null) return dialog.getError(); else return miniStatistics;
	}

	/**
	 * Versucht eine Minimalstatistik auf Basis eines Modells zu erstellen
	 * @param owner	Übergeordnetes Element (zur Ausrichtung des Wartedialogs)
	 * @param editModel	Editormodell zu dem Minimalstatistik erstellt werden soll
	 * @param alreadyAvailableStatistics	Schon verfügbare Statistik (kann <code>null</code> sein). Passt diese zu dem Modell, so wird sie direkt zurückgeliefert.
	 * @return	Minimalstatistik für das Modell oder <code>null</code>, wenn diese nicht berechnet werden konnte (eine Fehlermeldung wurde dann schon ausgegeben)
	 */
	public static Statistics generateMiniStatistics(final Component owner, final EditModel editModel, final Statistics alreadyAvailableStatistics) {
		final Object result=generateMiniStatisticsIntern(owner,editModel,alreadyAvailableStatistics);
		if (result instanceof String) {
			MsgBox.error(owner,Language.tr("ParameterCompare.PreparationFailed"),(String)result);
			return null;
		}

		return (Statistics)result;
	}
}
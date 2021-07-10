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
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import simulator.statistics.Statistics;
import systemtools.MsgBox;
import tools.IconListCellRenderer;
import tools.SetupData;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modelproperties.ModelPropertiesDialog.NextAction;

/**
 * Dialogseite "Ausgabeanalyse"
 * @author Alexander Herzog
 * @see ModelPropertiesDialog
 * @see ModelPropertiesDialogPage
 */
public class ModelPropertiesDialogPageOutputAnalysis extends ModelPropertiesDialogPage {
	/** Auswahlfeld "Autokorrelation der Wartezeiten erfassen" */
	private JComboBox<String> correlationMode;
	/** Eingabefeld "Maximaler Kundenabstand für Korrelationserfassung" */
	private JTextField correlationRange;
	/** Eingabefeld "Batch-Größe" */
	private JTextField batchMeansSize;
	/** Option Beenden der Simulation beim Erreichen eines Batch-Means-Konfidenzradius"" */
	private JCheckBox useFinishConfidence;
	/** Eingabefeld "Konfidenzradius für die Wartezeiten über alle Kunden (in Sekunden)" */
	private JTextField finishConfidenceHalfWidth;
	/** Eingabefeld "Konfidenzniveau für das Konfidenzintervall" */
	private JTextField finishConfidenceLevel;
	/** Warnung bei ungünstigen Parametern für die Korrelationserfassung, siehe {@link #testCorrelationWarning(long)} */
	private JLabel correlationWarning;

	/**
	 * Konstruktor der Klasse
	 * @param dialog	Dialog in dem sich diese Seite befindet
	 * @param model	Modell aus dem die Daten entnommen und in das die Daten geschrieben werden sollen
	 * @param readOnly	Nur-Lese-Status
	 * @param help	Hilfe-Callback
	 */
	public ModelPropertiesDialogPageOutputAnalysis(ModelPropertiesDialog dialog, EditModel model, boolean readOnly, Runnable help) {
		super(dialog,model,readOnly,help);
	}

	@Override
	public void build(JPanel content) {
		JPanel sub;
		Object[] data;
		JButton button;

		content.setLayout(new FlowLayout(FlowLayout.LEFT));
		JPanel lines;
		content.add(lines=new JPanel());
		content.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

		lines.setLayout(new BoxLayout(lines,BoxLayout.PAGE_AXIS));

		/* Erfassung der Autokorrelation der Wartezeiten */

		lines.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(new JLabel("<html><b>"+Language.tr("Editor.Dialog.Tab.OutputAnalysis.RecordAutocorrelation")+"</b></html>"));

		lines.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(correlationMode=new JComboBox<>(new String[]{
				Language.tr("Editor.Dialog.Tab.OutputAnalysis.RecordAutocorrelation.Off"),
				Language.tr("Editor.Dialog.Tab.OutputAnalysis.RecordAutocorrelation.Fast"),
				Language.tr("Editor.Dialog.Tab.OutputAnalysis.RecordAutocorrelation.Full")
		}));
		correlationMode.setRenderer(new IconListCellRenderer(new Images[]{
				Images.GENERAL_OFF,
				Images.MODELPROPERTIES_OUTPUT_ANALYSIS_AUTOCORRELATION_FAST,
				Images.MODELPROPERTIES_OUTPUT_ANALYSIS_AUTOCORRELATION_FULL
		}));
		correlationMode.setEnabled(!readOnly);
		switch (model.correlationMode) {
		case CORRELATION_MODE_OFF: correlationMode.setSelectedIndex(0); break;
		case CORRELATION_MODE_FAST: correlationMode.setSelectedIndex(1); break;
		case CORRELATION_MODE_FULL: correlationMode.setSelectedIndex(2); break;
		}
		int range=model.correlationRange;
		if (range<=0) range=1000;
		data=ModelElementBaseDialog.getInputPanel(Language.tr("Editor.Dialog.Tab.OutputAnalysis.RecordAutocorrelation.Range")+":",""+range,10);
		lines.add((JPanel)data[0]);
		correlationRange=(JTextField)data[1];
		correlationRange.setEditable(!readOnly);
		addKeyListener(correlationRange,()->{
			NumberTools.getPositiveLong(correlationRange,true);
			if (correlationMode.getSelectedIndex()==0) correlationMode.setSelectedIndex(1);
			dialog.testCorrelationWarning();
		});

		lines.add(Box.createVerticalStrut(25));

		/* Batch-Means */

		lines.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(new JLabel("<html><b>"+Language.tr("Editor.Dialog.Tab.OutputAnalysis.BatchMeans")+"</b></html>"));

		int size=model.batchMeansSize;
		if (size<=0) size=1;
		data=ModelElementBaseDialog.getInputPanel(Language.tr("Editor.Dialog.Tab.OutputAnalysis.BatchMeans.Size")+":",""+size,10);
		lines.add(sub=(JPanel)data[0]);
		batchMeansSize=(JTextField)data[1];
		batchMeansSize.setEditable(!readOnly);
		addKeyListener(batchMeansSize,()->NumberTools.getPositiveLong(batchMeansSize,true));
		sub.add(button=new JButton(Language.tr("Editor.Dialog.Tab.OutputAnalysis.BatchMeans.Size.Auto"),Images.MSGBOX_OK.getIcon()));
		button.setToolTipText(Language.tr("Editor.Dialog.Tab.OutputAnalysis.BatchMeans.Size.Auto.Hint"));
		button.setEnabled(!readOnly);
		button.addActionListener(e->dialog.doClose(NextAction.FIND_BATCH_SIZE));

		lines.add(Box.createVerticalStrut(25));

		lines.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(useFinishConfidence=new JCheckBox("<html><b>"+Language.tr("Editor.Dialog.Tab.OutputAnalysis.FinishConfidence")+"</b></html>",model.useFinishConfidence));
		useFinishConfidence.setEnabled(!readOnly);

		lines.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(new JLabel("("+Language.tr("Editor.Dialog.Tab.OutputAnalysis.FinishConfidence.Hint")+")"));

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Editor.Dialog.Tab.OutputAnalysis.FinishConfidence.HalfWidth")+":",NumberTools.formatNumberMax(model.finishConfidenceHalfWidth),10);
		lines.add((JPanel)data[0]);
		finishConfidenceHalfWidth=(JTextField)data[1];
		finishConfidenceHalfWidth.setEditable(!readOnly);
		addKeyListener(finishConfidenceHalfWidth,()->{
			useFinishConfidence.setEnabled(true);
			NumberTools.getPositiveDouble(finishConfidenceHalfWidth,true);
		});

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Editor.Dialog.Tab.OutputAnalysis.FinishConfidence.Level")+":",NumberTools.formatPercent(model.finishConfidenceLevel),10);
		lines.add((JPanel)data[0]);
		finishConfidenceLevel=(JTextField)data[1];
		finishConfidenceLevel.setEditable(!readOnly);
		addKeyListener(finishConfidenceLevel,()->{
			useFinishConfidence.setEnabled(true); NumberTools.getPositiveDouble(finishConfidenceLevel,true);
		});

		lines.add(Box.createVerticalStrut(25));

		/* Warnung bei zu langer Autokorrelationsaufzeichnung */

		lines.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(correlationWarning=new JLabel());
		correlationWarning.setIcon(Images.GENERAL_WARNING.getIcon());
		dialog.testCorrelationWarning();
	}

	/**
	 * Zeigt eine Warnung aus, wenn die gewählte Länge für die Korrelationsaufzeichnung
	 * oberhalb bzw. nah bei der Anzahl an insgesamt zu simulierenden Kunden liegt und
	 * damit eine vollständige Erfassung nicht möglich ist.
	 * @param terminationClientCount	Anzahl an Kunden bis zum Simulationsende
	 * @see #correlationRange
	 */
	public void testCorrelationWarning(final long terminationClientCount) {
		if (correlationMode.getSelectedIndex()==0) {correlationWarning.setVisible(false); return;}

		final Long L=NumberTools.getPositiveLong(correlationRange,true);
		if (L==null) {correlationWarning.setVisible(false); return;}
		final int correlationRange=L.intValue();

		if (terminationClientCount<=0) {correlationWarning.setVisible(false); return;}

		final SetupData setup=SetupData.getSetup();
		int threadCount=1;
		if (setup.useMultiCoreSimulation) threadCount=Math.min(setup.useMultiCoreSimulationMaxCount,Runtime.getRuntime().availableProcessors());

		final long clientsPerThread=terminationClientCount/threadCount;
		if (clientsPerThread/2<correlationRange) {
			if (clientsPerThread>correlationRange) {
				correlationWarning.setText("<html><b>"+String.format(Language.tr("Editor.Dialog.Tab.OutputAnalysis.RecordAutocorrelation.Warning.Near"),NumberTools.formatLong(correlationRange),NumberTools.formatLong(clientsPerThread))+"</b></html>");
			} else {
				correlationWarning.setText("<html><b>"+String.format(Language.tr("Editor.Dialog.Tab.OutputAnalysis.RecordAutocorrelation.Warning.Over"),NumberTools.formatLong(correlationRange),NumberTools.formatLong(clientsPerThread))+"</b></html>");
			}
			correlationWarning.setVisible(true);
		} else {
			correlationWarning.setVisible(false);
		}
	}

	@Override
	public boolean checkData() {
		if (correlationMode.getSelectedIndex()>0) {
			Long L=NumberTools.getPositiveLong(correlationRange,true);
			if (L==null) {
				MsgBox.error(dialog,Language.tr("Dialog.Title.Error"),String.format(Language.tr("Editor.Dialog.Tab.OutputAnalysis.RecordAutocorrelation.Range.Error"),correlationRange.getText()));
				return false;
			}
		}
		Long L=NumberTools.getPositiveLong(batchMeansSize,true);
		if (L==null) {
			MsgBox.error(dialog,Language.tr("Dialog.Title.Error"),String.format(Language.tr("Editor.Dialog.Tab.OutputAnalysis.BatchMeans.Size.Error"),batchMeansSize.getText()));
			return false;
		}

		if (useFinishConfidence.isSelected()) {
			Double D;
			D=NumberTools.getPositiveDouble(finishConfidenceHalfWidth,true);
			if (D==null) {
				MsgBox.error(dialog,Language.tr("Dialog.Title.Error"),String.format(Language.tr("Editor.Dialog.Tab.OutputAnalysis.FinishConfidence.HalfWidth.Error"),finishConfidenceHalfWidth.getText()));
				return false;
			}
			D=NumberTools.getProbability(finishConfidenceLevel,true);
			if (D==null) {
				MsgBox.error(dialog,Language.tr("Dialog.Title.Error"),String.format(Language.tr("Editor.Dialog.Tab.OutputAnalysis.FinishConfidence.Level.Error"),finishConfidenceLevel.getText()));
				return false;
			}
		}

		return true;
	}

	@Override
	public void storeData() {
		switch (correlationMode.getSelectedIndex()) {
		case 0: model.correlationMode=Statistics.CorrelationMode.CORRELATION_MODE_OFF; break;
		case 1: model.correlationMode=Statistics.CorrelationMode.CORRELATION_MODE_FAST; break;
		case 2: model.correlationMode=Statistics.CorrelationMode.CORRELATION_MODE_FULL; break;
		}
		if (model.correlationMode==Statistics.CorrelationMode.CORRELATION_MODE_OFF) {
			model.correlationRange=-1;
		} else {
			Long L;
			L=NumberTools.getPositiveLong(correlationRange,true);
			if (L!=null) model.correlationRange=L.intValue();
		}
		model.batchMeansSize=NumberTools.getPositiveLong(batchMeansSize,true).intValue();

		model.useFinishConfidence=useFinishConfidence.isSelected();
		final Double halfWidth=NumberTools.getPositiveDouble(finishConfidenceHalfWidth,true);
		final Double level=NumberTools.getProbability(finishConfidenceLevel,true);
		if (halfWidth!=null) model.finishConfidenceHalfWidth=halfWidth;
		if (level!=null) model.finishConfidenceLevel=level;
	}
}

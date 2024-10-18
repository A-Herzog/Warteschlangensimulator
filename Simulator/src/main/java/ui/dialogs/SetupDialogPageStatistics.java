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

import java.io.Serializable;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import language.Language;
import tools.IconListCellRenderer;
import tools.SetupData;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;
import ui.quickaccess.JPlaceholderTextField;

/**
 * Dialogseite "Statistik" im Programmeinstellungen-Dialog
 * @author Alexander Herzog
 * @see SetupData
 */
public class SetupDialogPageStatistics extends SetupDialogPage {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-1495513317837465600L;

	/* Bereich: Ansicht */

	/** Quantile in der Statistik ausgeben */
	private final JCheckBox showQuantils;
	/** Erlang-C-Vergleichswerte in der Statistik ausgeben */
	private final JCheckBox showErlangC;
	/** Ergebnishinweiseseite in der Statistik ausgeben */
	private final JCheckBox showRemarks;
	/** Statistikbaum immer sofort vollständig ausklappen */
	private final JCheckBox expandAllStatistics;
	/** Anzahl an anzuzeigenden Nachkommastellen in der Statistik für Zahlen */
	private final SpinnerModel statisticsNumberDigits;
	/** Anzahl an anzuzeigenden Nachkommastellen in der Statistik für Prozentwerte */
	private final SpinnerModel statisticsPercentDigits;
	/** Soll in Statistiktexten vor gerundeten Null-Werten, die jedoch nicht exakt Null sind, ein Ungefähr-Zeichen angezeigt werden? */
	private final JCheckBox showApproxSignOnValuesNearZero;

	/* Bereich: Anzeige von Quantilwerten */

	/** Levels zu denen Quantile ausgegeben werden sollen */
	private final JPlaceholderTextField quantilLevels;

	/* Bereich: Konfidenzniveaus für Batch-Means-Konfidenzintervalle */

	/** Levels zu denen Konfidenzintervallgrößen ausgegeben werden sollen */
	private final JPlaceholderTextField batchMeansConfidenceLevels;

	/* Bereich: Ergebnisse in externen Anwendungen öffnen */

	/** Anbieten, Statistik-Texte in Word zu öffnen? */
	private final JCheckBox openWord;
	/** Anbieten, Statistik-Texte in Open/LibreOffice zu öffnen? */
	private final JCheckBox openODT;
	/** Anbieten, Statistik-Tabellen in Excel zu öffnen? */
	private final JCheckBox openExcel;
	/** Anbieten, Statistik-Tabellen in Open/LibreOffice zu öffnen? */
	private final JCheckBox openODS;
	/** Anbieten, Statistik-Ergebnisse als pdf zu öffnen? */
	private final JCheckBox openPDF;

	/* Bereich: Unvollständige Statistik */

	/** Wie soll nach dem Abbruch einer Animation mit den unvollständigen Statistikdaten umgegangen werden? */
	private final JComboBox<String> canceledAnimationStatistics;
	/** Wie soll nach dem Abbruch einer Simulation mit den unvollständigen Statistikdaten umgegangen werden? */
	private final JComboBox<String> canceledSimulationStatistics;

	/**
	 * Konstruktor der Klasse
	 */
	public SetupDialogPageStatistics() {
		JPanel line;
		JLabel label;
		Object[] data;

		/*
		 * Bereich:
		 * Ansicht
		 */
		addHeading(Language.tr("SettingsDialog.Tabs.Statistics.View"));

		/* Quantile in der Statistik ausgeben */
		addLine().add(showQuantils=new JCheckBox(Language.tr("SettingsDialog.Tabs.Statistics.ShowQuantils")));

		/* Erlang-C-Vergleichswerte in der Statistik ausgeben */
		addLine().add(showErlangC=new JCheckBox(Language.tr("SettingsDialog.Tabs.Simulation.ShowErlangC")));

		/* Ergebnishinweiseseite in der Statistik ausgeben */
		addLine().add(showRemarks=new JCheckBox(Language.tr("SettingsDialog.Tabs.Simulation.ShowRemarks")));

		/* Statistikbaum immer sofort vollständig ausklappen */
		addLine().add(expandAllStatistics=new JCheckBox(Language.tr("SettingsDialog.Tabs.Simulation.ExpandAllStatistics")));

		/* Anzahl an anzuzeigenden Nachkommastellen in der Statistik für Zahlen */
		line=addLine();
		line.add(label=new JLabel(Language.tr("SettingsDialog.Tabs.Statistics.NumberDigits")+":"));
		final JSpinner statisticsNumberDigitsSpinner=new JSpinner(statisticsNumberDigits=new SpinnerNumberModel(1,1,9,1));
		final JSpinner.NumberEditor statisticsNumberDigitsEditor=new JSpinner.NumberEditor(statisticsNumberDigitsSpinner);
		statisticsNumberDigitsEditor.getFormat().setGroupingUsed(false);
		statisticsNumberDigitsEditor.getTextField().setColumns(2);
		statisticsNumberDigitsSpinner.setEditor(statisticsNumberDigitsEditor);
		line.add(statisticsNumberDigitsSpinner);
		label.setLabelFor(statisticsNumberDigitsSpinner);

		/* Anzahl an anzuzeigenden Nachkommastellen in der Statistik für Prozentwerte */
		line=addLine();
		line.add(label=new JLabel(Language.tr("SettingsDialog.Tabs.Statistics.PercentDigits")+":"));
		final JSpinner statisticsNumberPercentSpinner=new JSpinner(statisticsPercentDigits=new SpinnerNumberModel(1,1,9,1));
		final JSpinner.NumberEditor statisticsPercentDigitsEditor=new JSpinner.NumberEditor(statisticsNumberPercentSpinner);
		statisticsPercentDigitsEditor.getFormat().setGroupingUsed(false);
		statisticsPercentDigitsEditor.getTextField().setColumns(2);
		statisticsNumberPercentSpinner.setEditor(statisticsPercentDigitsEditor);
		line.add(statisticsNumberPercentSpinner);
		label.setLabelFor(statisticsNumberPercentSpinner);

		/* Soll in Statistiktexten vor gerundeten Null-Werten, die jedoch nicht exakt Null sind, ein Ungefähr-Zeichen angezeigt werden? */
		addLine().add(showApproxSignOnValuesNearZero=new JCheckBox(Language.tr("SettingsDialog.Tabs.Statistics.UseApproxSign")));

		/*
		 * Bereich:
		 * Anzeige von Quantilwerten
		 */
		addHeading(Language.tr("SettingsDialog.Tabs.Statistics.QuantilLevels"));

		/** Levels zu denen Quantile ausgegeben werden sollen */
		data=ModelElementBaseDialog.getPlaceholderInputPanel(Language.tr("SettingsDialog.Tabs.Statistics.QuantilLevels.Levels")+":",Language.tr("SettingsDialog.Tabs.Statistics.QuantilLevels.Levels.Placeholder"),"");
		add((JPanel)data[0]);
		quantilLevels=(JPlaceholderTextField)data[1];

		/*
		 * Bereich:
		 * Konfidenzniveaus für Batch-Means-Konfidenzintervalle
		 */
		addHeading(Language.tr("SettingsDialog.Tabs.Statistics.BatchMeansConfidenceLevels"));

		/* Levels zu denen Konfidenzintervallgrößen ausgegeben werden sollen */
		data=ModelElementBaseDialog.getPlaceholderInputPanel(Language.tr("SettingsDialog.Tabs.Statistics.BatchMeansConfidenceLevels.Levels")+":",Language.tr("SettingsDialog.Tabs.Statistics.BatchMeansConfidenceLevels.Levels.Placeholder"),"");
		add((JPanel)data[0]);
		batchMeansConfidenceLevels=(JPlaceholderTextField)data[1];

		/*
		 * Bereich:
		 * Ergebnisse in externen Anwendungen öffnen
		 */
		addHeading(Language.tr("SettingsDialog.Tabs.Statistics.OpenExternal"));

		/* Anbieten, Statistik-Texte in Word zu öffnen? */
		line=addLine();
		line.add(openWord=new JCheckBox(Language.tr("SettingsDialog.Tabs.Statistics.OpenWord")));

		/* Anbieten, Statistik-Texte in Open/LibreOffice zu öffnen? */
		line.add(openODT=new JCheckBox(Language.tr("SettingsDialog.Tabs.Statistics.OpenODT")));

		/* Anbieten, Statistik-Tabellen in Excel zu öffnen? */
		line=addLine();
		line.add(openExcel=new JCheckBox(Language.tr("SettingsDialog.Tabs.Statistics.OpenExcel")));

		/* Anbieten, Statistik-Tabellen in Open/LibreOffice zu öffnen? */
		line.add(openODS=new JCheckBox(Language.tr("SettingsDialog.Tabs.Statistics.OpenODS")));

		/* Anbieten, Statistik-Ergebnisse als pdf zu öffnen? */
		addLine().add(openPDF=new JCheckBox(Language.tr("SettingsDialog.Tabs.Statistics.OpenPDF")));

		/*
		 * Bereich:
		 * Unvollständige Statistik
		 */

		addHeading(Language.tr("SettingsDialog.Tabs.Statistics.IncompleteStatistics"));

		/* Wie soll nach dem Abbruch einer Animation mit den unvollständigen Statistikdaten umgegangen werden? */
		line=addLine();
		line.add(label=new JLabel(Language.tr("SettingsDialog.Tabs.Statistics.IncompleteStatistics.Animation")+":"));
		line.add(canceledAnimationStatistics=new JComboBox<>(new String[] {
				Language.tr("SettingsDialog.Tabs.Statistics.IncompleteStatistics.Mode.Off"),
				Language.tr("SettingsDialog.Tabs.Statistics.IncompleteStatistics.Mode.Ask"),
				Language.tr("SettingsDialog.Tabs.Statistics.IncompleteStatistics.Mode.Show")
		}));
		canceledAnimationStatistics.setRenderer(new IconListCellRenderer(new Images[]{
				Images.GENERAL_CANCEL,
				Images.HELP,
				Images.STATISTICS
		}));
		label.setLabelFor(canceledAnimationStatistics);

		/* Wie soll nach dem Abbruch einer Simulation mit den unvollständigen Statistikdaten umgegangen werden? */
		line=addLine();
		line.add(label=new JLabel(Language.tr("SettingsDialog.Tabs.Statistics.IncompleteStatistics.Simulation")+":"));
		line.add(canceledSimulationStatistics=new JComboBox<>(new String[] {
				Language.tr("SettingsDialog.Tabs.Statistics.IncompleteStatistics.Mode.Off"),
				Language.tr("SettingsDialog.Tabs.Statistics.IncompleteStatistics.Mode.Ask"),
				Language.tr("SettingsDialog.Tabs.Statistics.IncompleteStatistics.Mode.Show")
		}));
		canceledSimulationStatistics.setRenderer(new IconListCellRenderer(new Images[]{
				Images.GENERAL_CANCEL,
				Images.HELP,
				Images.STATISTICS
		}));
		label.setLabelFor(canceledSimulationStatistics);
	}

	@Override
	public void loadData() {
		showQuantils.setSelected(setup.showQuantils);
		showErlangC.setSelected(setup.showErlangC);
		showRemarks.setSelected(setup.showRemarks);
		expandAllStatistics.setSelected(setup.expandAllStatistics);
		statisticsNumberDigits.setValue(setup.statisticsNumberDigits);
		statisticsPercentDigits.setValue(setup.statisticsPercentDigits);
		showApproxSignOnValuesNearZero.setSelected(setup.showApproxSignOnValuesNearZero);
		quantilLevels.setText(setup.quantilLevels);
		batchMeansConfidenceLevels.setText(setup.batchMeansConfidenceLevels);
		openWord.setSelected(setup.openWord);
		openODT.setSelected(setup.openODT);
		openExcel.setSelected(setup.openExcel);
		openODS.setSelected(setup.openODS);
		openPDF.setSelected(setup.openPDF);
		switch (setup.canceledAnimationStatistics) {
		case OFF: canceledAnimationStatistics.setSelectedIndex(0); break;
		case ASK: canceledAnimationStatistics.setSelectedIndex(1); break;
		case SHOW: canceledAnimationStatistics.setSelectedIndex(2); break;
		}
		switch (setup.canceledSimulationStatistics) {
		case OFF: canceledSimulationStatistics.setSelectedIndex(0); break;
		case ASK: canceledSimulationStatistics.setSelectedIndex(1); break;
		case SHOW: canceledSimulationStatistics.setSelectedIndex(2); break;
		}
	}

	@Override
	public void storeData() {
		setup.showQuantils=showQuantils.isSelected();
		setup.showErlangC=showErlangC.isSelected();
		setup.showRemarks=showRemarks.isSelected();
		setup.expandAllStatistics=expandAllStatistics.isSelected();
		setup.statisticsNumberDigits=((Integer)statisticsNumberDigits.getValue()).intValue();
		setup.statisticsPercentDigits=((Integer)statisticsPercentDigits.getValue()).intValue();
		setup.showApproxSignOnValuesNearZero=showApproxSignOnValuesNearZero.isSelected();
		setup.quantilLevels=quantilLevels.getText();
		setup.batchMeansConfidenceLevels=batchMeansConfidenceLevels.getText();
		setup.openWord=openWord.isSelected();
		setup.openODT=openODT.isSelected();
		setup.openExcel=openExcel.isSelected();
		setup.openODS=openODS.isSelected();
		setup.openPDF=openPDF.isSelected();
		switch (canceledAnimationStatistics.getSelectedIndex()) {
		case 0: setup.canceledAnimationStatistics=SetupData.CanceledSimulationStatistics.OFF; break;
		case 1: setup.canceledAnimationStatistics=SetupData.CanceledSimulationStatistics.ASK; break;
		case 2: setup.canceledAnimationStatistics=SetupData.CanceledSimulationStatistics.SHOW; break;
		}
		switch (canceledSimulationStatistics.getSelectedIndex()) {
		case 0: setup.canceledSimulationStatistics=SetupData.CanceledSimulationStatistics.OFF; break;
		case 1: setup.canceledSimulationStatistics=SetupData.CanceledSimulationStatistics.ASK; break;
		case 2: setup.canceledSimulationStatistics=SetupData.CanceledSimulationStatistics.SHOW; break;
		}
	}

	@Override
	public void resetSettings() {
		showQuantils.setSelected(true);
		showErlangC.setSelected(true);
		showRemarks.setSelected(true);
		expandAllStatistics.setSelected(false);
		statisticsNumberDigits.setValue(1);
		statisticsPercentDigits.setValue(1);
		showApproxSignOnValuesNearZero.setSelected(true);
		quantilLevels.setText("");
		batchMeansConfidenceLevels.setText("");
		openWord.setSelected(true);
		openODT.setSelected(false);
		openExcel.setSelected(true);
		openODS.setSelected(false);
		openPDF.setSelected(false);
		canceledAnimationStatistics.setSelectedIndex(0);
		canceledSimulationStatistics.setSelectedIndex(1);
	}
}
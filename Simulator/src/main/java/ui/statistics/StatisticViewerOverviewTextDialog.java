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
package ui.statistics;

import java.awt.Component;
import java.awt.FlowLayout;
import java.io.Serializable;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import language.Language;
import systemtools.BaseDialog;
import tools.SetupData;
import ui.help.Help;
import ui.modeleditor.ModelElementBaseDialog;
import ui.quickaccess.JPlaceholderTextField;

/**
 * Zeigt einen Dialog zur Konfiguration der Anzeige auf den Text-Statistik-Seiten an.
 * @author Alexander Herzog
 * @see StatisticViewerOverviewText
 */
public class StatisticViewerOverviewTextDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=907534531141109986L;

	/** Globales Einstellungen-Objekt */
	private final SetupData setup;

	/** Quantile in der Statistik ausgeben */
	private final JCheckBox showQuantils;
	/** Anzahl an anzuzeigenden Nachkommastellen in der Statistik für Zahlen */
	private final SpinnerModel statisticsNumberDigits;
	/** Anzahl an anzuzeigenden Nachkommastellen in der Statistik für Prozentwerte */
	private final SpinnerModel statisticsPercentDigits;
	/** Levels zu denen Quantile ausgegeben werden sollen */
	private final JPlaceholderTextField quantilLevels;
	/** Levels zu denen Konfidenzintervallgrößen ausgegeben werden sollen */
	private final JPlaceholderTextField batchMeansConfidenceLevels;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 */
	public StatisticViewerOverviewTextDialog(final Component owner) {
		super(owner,Language.tr("Statistics.TextSettings.Title"));

		setup=SetupData.getSetup();

		JPanel p;
		JLabel label;
		Object[] data;

		/* GUI */

		final JPanel content=createGUI(()->Help.topicModal(this,"MainStatistik"));
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		/* Bereich "Ansicht" */

		content.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(new JLabel("<html><body><b>"+Language.tr("SettingsDialog.Tabs.Statistics.View")+"</b></body></html>"));

		content.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(showQuantils=new JCheckBox(Language.tr("SettingsDialog.Tabs.Statistics.ShowQuantils")));

		content.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(label=new JLabel(Language.tr("SettingsDialog.Tabs.Statistics.NumberDigits")+":"));
		final JSpinner statisticsNumberDigitsSpinner=new JSpinner(statisticsNumberDigits=new SpinnerNumberModel(1,1,9,1));
		final JSpinner.NumberEditor statisticsNumberDigitsEditor=new JSpinner.NumberEditor(statisticsNumberDigitsSpinner);
		statisticsNumberDigitsEditor.getFormat().setGroupingUsed(false);
		statisticsNumberDigitsEditor.getTextField().setColumns(2);
		statisticsNumberDigitsSpinner.setEditor(statisticsNumberDigitsEditor);
		p.add(statisticsNumberDigitsSpinner);
		label.setLabelFor(statisticsNumberDigitsSpinner);

		content.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(label=new JLabel(Language.tr("SettingsDialog.Tabs.Statistics.PercentDigits")+":"));
		final JSpinner statisticsNumberPercentSpinner=new JSpinner(statisticsPercentDigits=new SpinnerNumberModel(1,1,9,1));
		final JSpinner.NumberEditor statisticsPercentDigitsEditor=new JSpinner.NumberEditor(statisticsNumberPercentSpinner);
		statisticsPercentDigitsEditor.getFormat().setGroupingUsed(false);
		statisticsPercentDigitsEditor.getTextField().setColumns(2);
		statisticsNumberPercentSpinner.setEditor(statisticsPercentDigitsEditor);
		p.add(statisticsNumberPercentSpinner);
		label.setLabelFor(statisticsNumberPercentSpinner);

		/* Bereich "Quantile" */

		content.add(Box.createVerticalStrut(15));
		content.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(new JLabel("<html><body><b>"+Language.tr("SettingsDialog.Tabs.Statistics.QuantilLevels")+"</b></body></html>"));

		data=ModelElementBaseDialog.getPlaceholderInputPanel(Language.tr("SettingsDialog.Tabs.Statistics.QuantilLevels.Levels")+":",Language.tr("SettingsDialog.Tabs.Statistics.QuantilLevels.Levels.Placeholder"),"");
		content.add((JPanel)data[0]);
		quantilLevels=(JPlaceholderTextField)data[1];

		/* Bereich "Konfidenzintervalle" */

		content.add(Box.createVerticalStrut(15));
		content.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(new JLabel("<html><body><b>"+Language.tr("SettingsDialog.Tabs.Statistics.BatchMeansConfidenceLevels")+"</b></body></html>"));

		data=ModelElementBaseDialog.getPlaceholderInputPanel(Language.tr("SettingsDialog.Tabs.Statistics.BatchMeansConfidenceLevels.Levels")+":",Language.tr("SettingsDialog.Tabs.Statistics.BatchMeansConfidenceLevels.Levels.Placeholder"),"");
		content.add((JPanel)data[0]);
		batchMeansConfidenceLevels=(JPlaceholderTextField)data[1];

		/* Daten laden */

		showQuantils.setSelected(setup.showQuantils);
		statisticsNumberDigits.setValue(setup.statisticsNumberDigits);
		statisticsPercentDigits.setValue(setup.statisticsPercentDigits);
		quantilLevels.setText(setup.quantilLevels);
		batchMeansConfidenceLevels.setText(setup.batchMeansConfidenceLevels);

		/* Dialog starten */
		pack();
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	@Override
	protected void storeData() {
		setup.showQuantils=showQuantils.isSelected();
		setup.statisticsNumberDigits=((Integer)statisticsNumberDigits.getValue()).intValue();
		setup.statisticsPercentDigits=((Integer)statisticsPercentDigits.getValue()).intValue();
		setup.quantilLevels=quantilLevels.getText();
		setup.batchMeansConfidenceLevels=batchMeansConfidenceLevels.getText();
		setup.saveSetupWithWarning(this);
	}
}
